/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.NbMavenProjectFactory;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.spi.queries.ForeignClassBundler;
import org.netbeans.spi.java.project.support.JavadocAndSourceRootDetection;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * SourceForBinaryQueryImplementation implementation
 * for items in the maven2 repository. It checks the artifact and
 * looks for the same artifact but of type "sources.jar".
 * 
 * @author  Milos Kleint
 */
@ServiceProviders({
    @ServiceProvider(service=SourceForBinaryQueryImplementation.class, position=68),
    @ServiceProvider(service=SourceForBinaryQueryImplementation2.class, position=68),
    @ServiceProvider(service=JavadocForBinaryQueryImplementation.class, position=68)
})
public class RepositoryForBinaryQueryImpl extends AbstractMavenForBinaryQueryImpl {
    
    private final Map<URL, WeakReference<SrcResult>> srcCache = Collections.synchronizedMap(new HashMap<URL, WeakReference<SrcResult>>());
    private final Map<URL, WeakReference<JavadocResult>> javadocCache = Collections.synchronizedMap(new HashMap<URL, WeakReference<JavadocResult>>());
    
    //http://maven.apache.org/guides/mini/guide-attached-tests.html
    //issue 219453
    private static final String CLASSIFIER_TESTS = "tests";
    
    private static final RequestProcessor RP = new RequestProcessor("Maven Repository SFBQ result change");
    private static final Logger LOG = Logger.getLogger(SrcResult.class.getName());

    @Override
    public synchronized Result findSourceRoots2(URL url) {
        if (!"jar".equals(url.getProtocol())) { //NOI18N
            // null for directories.
            return null;
         }
        
        WeakReference<SrcResult> cached = srcCache.get(url);
        if (cached != null) {
            SrcResult result = cached.get();
            if (result != null) {
                return result;
            }
        }
        
        //#223841 at least one project opened is a stronger condition, embedder gets sometimes reset.
        //once we have the project loaded, not loaded embedder doesn't matter anymore, we have to process.
        // sometimes the embedder is loaded even though a maven project is not yet loaded, it doesn't hurt to proceed then.
        if (!NbMavenProjectFactory.isAtLeastOneMavenProjectAround() || !EmbedderFactory.isProjectEmbedderLoaded()) { 
            return null;
        }
        
        File jarFile = FileUtil.archiveOrDirForURL(url);
        if (jarFile != null) {
//                String name = jarFile.getName();
            File parent = jarFile.getParentFile();
            if (parent != null) {
                File parentParent = parent.getParentFile();
                if (parentParent != null) {
                    // each repository artifact should have this structure
                    String artifact = parentParent.getName();
                    String version = parent.getName();
//                        File pom = new File(parent, artifact + "-" + version + ".pom");
//                        // maybe this condition is already overkill..
//                        if (pom.exists()) {
                    String start = artifact + "-" + version;
                    if (jarFile.getName().startsWith(start)) { //one last perf check before calling the embedder
                        URI localRepo = Utilities.toURI(EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile());
                        URI rel = localRepo.relativize(Utilities.toURI(parentParent.getParentFile()));
                        if (!rel.isAbsolute()) {
                            String groupId = rel.getPath();
                            if (groupId != null && !groupId.equals("")) {
                                groupId = groupId.replace("/", ".");
                                if (groupId.endsWith(".")) {
                                    groupId = groupId.substring(0, groupId.length() - 1);
                                }
                                String classifier = null;
                                if (jarFile.getName().startsWith(start + "-")) {
                                    //we have classifier here..
                                    String end = jarFile.getName().substring((start + "-").length());
                                    if (end.indexOf('.') > -1) {
                                        classifier = end.substring(0, end.indexOf('.'));
                                    }
                                }
                                File srcs = new File(parent, start + (classifier != null ? ("-" + classifier) : "") + "-sources.jar"); //NOI18N
                                SrcResult result = new SrcResult(groupId, artifact, version, classifier, FileUtil.getArchiveFile(url), srcs);
                                srcCache.put(url, new WeakReference<SrcResult>(result));
                                return result;
                            }
                        }

                    }
//                        }
                }
            }
        }
        return null;
                
    }
    
    
    @Override
    public JavadocForBinaryQuery.Result findJavadoc(URL url) {
        URL binRoot;
        
        if ("jar".equals(url.getProtocol())) { //NOI18N
            binRoot = FileUtil.getArchiveFile(url);
        } else {
            // null for directories.
            return null;
        }
        
        //hack for javaee6 jar docs which we ship with netbeans and which are not in any maven repository
        if (binRoot.getPath().endsWith("/javax/javaee-api/6.0/javaee-api-6.0.jar")
         || binRoot.getPath().endsWith("/javax/javaee-web-api/6.0/javaee-web-api-6.0.jar")) { //NOI18N
            return new Javaee6Result();
        }
        
        WeakReference<JavadocResult> cached = javadocCache.get(url);
        if (cached != null) {
            JavadocResult result = cached.get();
            if (result != null) {
                return result;
            }
        }
        
        
        File jarFile = FileUtil.archiveOrDirForURL(url);
        if (jarFile != null) {
            File parent = jarFile.getParentFile();
            if (parent != null) {
                File parentParent = parent.getParentFile();
                if (parentParent != null) {
                    // each repository artifact should have this structure
                    String artifact = parentParent.getName();
                    String version = parent.getName();
                    String start = artifact + "-" + version;
                    if (jarFile.getName().startsWith(start)) { //one last perf check before calling the embedder
                        URI localRepo = Utilities.toURI(EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile());
                        URI rel = localRepo.relativize(Utilities.toURI(parentParent.getParentFile()));
                        if (!rel.isAbsolute()) {
                            String groupId = rel.getPath();
                            if (groupId != null && !groupId.equals("")) {
                                groupId = groupId.replace("/", ".");
                                if (groupId.endsWith(".")) {
                                    groupId = groupId.substring(0, groupId.length() - 1);
                                }
                                String classifier = null;
                                if (jarFile.getName().startsWith(start + "-")) {
                                    //we have classifier here..
                                    String end = jarFile.getName().substring((start + "-").length());
                                    if (end.indexOf('.') > -1) {
                                        classifier = end.substring(end.indexOf('.'));
                                    }
                                }
                                File javadoc = new File(parent, start + (classifier != null ? ("-" + classifier) : "") + "-javadoc.jar"); //NOI18N
                                JavadocResult result = new JavadocResult(groupId, artifact, version, classifier, binRoot, javadoc);
                                javadocCache.put(url, new WeakReference<JavadocResult>(result));
                                return result;
                            }
                        }
                    }
                }
            }
        }
        return null;
                
    }
    
    private static class SrcResult implements SourceForBinaryQueryImplementation2.Result  {

        private static final String ATTR_PATH = "lastRootCheckPath"; //NOI18N
        private static final String ATTR_STAMP = "lastRootCheckStamp"; //NOI18N
        private final File sourceJarFile;
        private final ChangeSupport support;
        private final ChangeListener mfoListener;
        private final PropertyChangeListener projectListener;
        private final FileChangeListener sourceJarChangeListener;
        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String classifier;
        private final URL binary;
        
        private Project currentProject;
        private FileObject[] cached;
        
        SrcResult(String groupId, String artifactId, String version, String classifier, URL binary, File sourceJar) {
            sourceJarFile = sourceJar;
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.binary = binary;
            this.classifier = classifier;
            
            support = new ChangeSupport(this);
            mfoListener = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    //external root in local repository changed..
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            checkChanges(true);
                        }
                    });
                }
            };
            projectListener = new PropertyChangeListener() {
                public @Override
                void propertyChange(PropertyChangeEvent event) {
                    if (NbMavenProject.PROP_PROJECT.equals(event.getPropertyName())) {
                        //project could have changed source roots..
                        RP.post(new Runnable() {
                            @Override
                            public void run() {
                                checkChanges(true);
                            }
                        });
                    }
                }
            };
            sourceJarChangeListener = new FileChangeAdapter(){
                @Override
                public void fileDataCreated(FileEvent fe) {
                    //source jar was created..
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            checkChanges(true);
                        }
                    });
                }
 
            };
            checkChanges(false);
            
            MavenFileOwnerQueryImpl.getInstance().addChangeListener(
                    WeakListeners.create(ChangeListener.class, mfoListener, MavenFileOwnerQueryImpl.getInstance()));
         
            FileUtil.addFileChangeListener(FileUtil.weakFileChangeListener(sourceJarChangeListener, sourceJar));
        }
        
        private void checkChanges(boolean fireChanges) {
            //getRoots will fire change in the result if the old cached value is different from the newly generated one
            FileObject[] ch;
            FileObject[] toRet;
            // use MFOQI to determine what is the current project owning our coordinates in local repository.
            Project owner = MavenFileOwnerQueryImpl.getInstance().getOwner(groupId, artifactId, version);
            if (owner != null && owner.getLookup().lookup(NbMavenProject.class) == null) {
                owner = null;
            }
            synchronized (this) {
                ch = cached;
                if (currentProject != null && !currentProject.equals(owner)) {
                    currentProject.getLookup().lookup(NbMavenProject.class).removePropertyChangeListener(projectListener);
                }
                if (owner != null && !owner.equals(currentProject)) {
                    owner.getLookup().lookup(NbMavenProject.class).addPropertyChangeListener(projectListener);
                }
                currentProject = owner;
                toRet = getRoots();
            }
            
            if (fireChanges && !Arrays.equals(ch, toRet)) {
                support.fireChange();
            }
            
        }
        
        @Override 
        public void addChangeListener(ChangeListener changeListener) {
            support.addChangeListener(changeListener);
        }
        
        @Override 
        public void removeChangeListener(ChangeListener changeListener) {
            support.removeChangeListener(changeListener);
        }
        
        @Override 
        public FileObject[] getRoots() {
            FileObject[] toRet;
            Project prj;
            synchronized (this) {
                prj = currentProject;
            }
            if (prj != null && classifier == null) {
                toRet = getProjectSrcRoots(prj);
            } else if (prj != null && CLASSIFIER_TESTS.equals(classifier)) {
                toRet = getProjectTestSrcRoots(prj);
            } else {
                File[] f = SourceJavadocByHash.find(binary, false);
                if (f != null) {
                    List<FileObject> accum = new ArrayList<FileObject>();
                    for (File ff : f) {
                        FileObject[] fo = getSourceJarRoot(ff);
                        if (fo != null) {
                            accum.addAll(Arrays.asList(fo));
                        }
                    }
                    toRet = accum.toArray(new FileObject[0]);
                }
                else if (sourceJarFile.exists()) {
                    toRet = getSourceJarRoot(sourceJarFile);
                } else {
                    toRet = checkShadedMultiJars();
                }
            }
            synchronized (this) {
                cached = toRet;
            }
            return toRet;
        }
        
        private String checkPath(FileObject jarRoot, FileObject fo) {
            String toRet = null;
            FileObject root = JavadocAndSourceRootDetection.findSourceRoot(jarRoot);
            try {
                if (root != null && !root.equals(jarRoot)) {
                    toRet = FileUtil.getRelativePath(jarRoot, root);
                    fo.setAttribute(ATTR_PATH, toRet);
                }
                fo.setAttribute(ATTR_STAMP, new Date());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return toRet;
        }        
        
        private FileObject[] getSourceJarRoot(File sourceJar) {
            FileObject fo = FileUtil.toFileObject(sourceJar);
            if (fo != null) {
                FileObject jarRoot = FileUtil.getArchiveRoot(fo);
                if (jarRoot != null) { //#139894 it seems that sometimes it can return null.
                                  // I suppose it's in the case when the jar/zip file in repository exists
                                  // but is corrupted (not zip, eg. when downloaded from a wrongly
                                  //setup repository that returns html documents on missing jar files.

                    //try detecting the source path root, in case the source jar has the sources not in root.
                    Date date = (Date) fo.getAttribute(ATTR_STAMP);
                    String path = (String) fo.getAttribute(ATTR_PATH);
                    if (date == null || fo.lastModified().after(date)) {
                        path = checkPath(jarRoot, fo);
                    }

                    FileObject[] fos = new FileObject[1];
                    if (path != null) {
                        fos[0] = jarRoot.getFileObject(path);
                    }
                    if (fos[0] == null) {
                        fos[0] = jarRoot;
                    }
                    return fos;
                }
            }
            return new FileObject[0];
        }
        
        @Override
        public boolean preferSources() {
            Project prj;
            synchronized (this) {
                prj = currentProject;
            }
            if (prj != null && classifier == null) {
                if (!NbMavenProject.isErrorPlaceholder(prj.getLookup().lookup(NbMavenProject.class).getMavenProject())) {
                    return prj.getLookup().lookup(ForeignClassBundler.class).preferSources();
                }
            } else if (prj != null && CLASSIFIER_TESTS.equals(classifier)) {
                return true;
            }
            
            return false;
        }

        private synchronized FileObject[] checkShadedMultiJars() {
            try {
                List<Coordinates> coordinates = getShadedCoordinates(Utilities.toFile(binary.toURI()));
                File lrf = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile();
                List<FileObject> fos = new ArrayList<FileObject>();
                if (coordinates != null) {
                    for (Coordinates coord : coordinates) {
                            File sourceJar = new File(lrf, coord.groupId.replace(".", File.separator) + File.separator + coord.artifactId + File.separator + coord.version + File.separator + coord.artifactId + "-" + coord.version + "-sources.jar");
                            FileObject[] fo = getSourceJarRoot(sourceJar);
                            if (fo.length == 1) {
                                fos.add(fo[0]);
                            }
                    }
                }
                if (fos.size() > 1) {
                    FileObject[] shaded = fos.toArray(new FileObject[0]);
                    return shaded;
                }
            } catch (Exception ex) {
                LOG.log(Level.INFO, "error while examining binary " + binary, ex);
            }
            return new FileObject[0];
        }
        
    }  
    
    public static class Coordinates {
        public final String groupId;
        public final String artifactId;
        public final String version;

        private Coordinates(String groupId, String artifactId, String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }
        
    }
    
    public static List<Coordinates> getShadedCoordinates(File binaryFile) {
            if (binaryFile == null || !binaryFile.exists() || !binaryFile.isFile()) {
                return null;
            }
            ZipFile zip = null;
            try {
                List<Coordinates> toRet = new ArrayList<Coordinates>();
                zip = new ZipFile(binaryFile);
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry ent = entries.nextElement();
                    String name = ent.getName();
                    if (name.startsWith("META-INF") && name.endsWith("pom.properties")) {
                        Properties p = new Properties();
                        p.load(zip.getInputStream(ent));
                        String groupId = p.getProperty("groupId");
                        String artifactId = p.getProperty("artifactId");
                        String version = p.getProperty("version");
                        if (groupId != null && artifactId != null && version != null) {
                            toRet.add(new Coordinates(groupId, artifactId, version));
                        }
                    }
                }
                if (toRet.size() > 1) {
                    return toRet;
                }
            } catch (Exception ex) {
                LOG.log(Level.INFO, "error while examining binary " + binaryFile, ex);
            } finally {
                if (zip != null) {
                    try {
                        zip.close();
                    } catch (IOException ex) {
//                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            return null;
        
    }
    
    private static class JavadocResult implements JavadocForBinaryQuery.Result {
        private final File javadocJarFile;
        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String classifier;
        private final URL binary;
        private final String gav;
        private final ChangeSupport support;
        private final PropertyChangeListener projectListener;
        private final FileChangeAdapter javadocJarChangeListener;
        private Project currentProject;
        private URL[] cached;
        private static final String ATTR_PATH = "lastRootCheckPath"; //NOI18N
        private static final String ATTR_STAMP = "lastRootCheckStamp"; //NOI18N
        private final ChangeListener mfoListener;
        
        JavadocResult(String groupId, String artifactId, String version, String classifier, URL binary, File javadocJar) {
            javadocJarFile = javadocJar;
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.binary = binary;
            this.classifier = classifier;
            this.gav = MavenFileOwnerQueryImpl.cacheKey(groupId, artifactId, version);
            
            support = new ChangeSupport(this);
            mfoListener = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    //external root in local repository changed..
                    checkChanges();
                }
            };
            projectListener = new PropertyChangeListener() {
                public @Override
                void propertyChange(PropertyChangeEvent event) {
                    if (NbMavenProject.PROP_PROJECT.equals(event.getPropertyName())) {
                        checkChanges();
                    }
                }
            };
            javadocJarChangeListener = new FileChangeAdapter(){
                @Override
                public void fileDataCreated(FileEvent fe) {
                    //source jar was created..
                    checkChanges();
                }

            };
            MavenFileOwnerQueryImpl.getInstance().addChangeListener(
                    WeakListeners.create(ChangeListener.class, mfoListener, MavenFileOwnerQueryImpl.getInstance()));
        
            FileUtil.addFileChangeListener(javadocJarChangeListener, javadocJar);
        }   
        
        @Override
        public synchronized URL[] getRoots() {
            URL[] toRet;
            checkCurrentProject();
            Project prj = currentProject;
            if (prj != null) {
                toRet = new URL[0];
            } else {
                File[] f = SourceJavadocByHash.find(binary, true);
                if (f != null) {
                    List<URL> accum = new ArrayList<URL>();
                    for (File ff : f) {
                        URL[] url = getJavadocJarRoot(ff);
                        if (url != null) {
                            accum.addAll(Arrays.asList(url));
                        }
                    }
                    toRet = accum.toArray(new URL[0]);
                }
                else if (javadocJarFile.exists()) {
                    toRet = getJavadocJarRoot(javadocJarFile);
                } else {
                    toRet = checkShadedMultiJars();
                }
            }
            if (!Arrays.equals(cached, toRet)) {
                //how to figure otherwise that something changed, possibly multiple people hold the result instance
                // and one asks the roots, later we get event from outside, but then the cached value already updated..
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        support.fireChange();
                    }
                });
            }
            cached = toRet;
            return toRet;
        }
        
        @Override
        public void addChangeListener(ChangeListener l) {
            support.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            support.removeChangeListener(l);
        }
        
        private void checkChanges() {
            //getRoots will fire change in the result if the old cached value is different from the newly generated one
            getRoots();
        }
        /**
         * use MFOQI to determine what is the current project owning our coordinates in local repository.
         */
        private void checkCurrentProject() {
            Project owner = MavenFileOwnerQueryImpl.getInstance().getOwner(groupId, artifactId, version);
            if (owner != null && owner.getLookup().lookup(NbMavenProject.class) == null) {
                owner = null;
            }
            if (currentProject != null && !currentProject.equals(owner)) {
                currentProject.getLookup().lookup(NbMavenProject.class).removePropertyChangeListener(projectListener);
            }
            if (owner != null && !owner.equals(currentProject)) {
                owner.getLookup().lookup(NbMavenProject.class).addPropertyChangeListener(projectListener);
            }
            currentProject = owner;
        }         

        private URL[] getJavadocJarRoot(File file) {
            try {
                if (file.exists()) {
                    FileObject fo = FileUtil.toFileObject(file);
                    if (!FileUtil.isArchiveFile(fo)) {
                        //#124175  ignore any jar files that are not jar files (like when downloaded file is actually an error html page).
                        Logger.getLogger(RepositoryForBinaryQueryImpl.class.getName()).log(Level.INFO, "javadoc in repository is not really a JAR: {0}", file);
                        return new URL[0];
                    }
                    //try detecting the source path root, in case the source jar has the sources not in root.
                    Date date = (Date) fo.getAttribute(ATTR_STAMP);
                    String path = (String) fo.getAttribute(ATTR_PATH);
                    if (date == null || fo.lastModified().after(date)) {
                        path = checkPath(FileUtil.getArchiveRoot(fo), fo);
                    }
                    
                    URL[] url;
                    if (path != null) {
                        url = new URL[1];
                        URL root = FileUtil.getArchiveRoot(Utilities.toURI(file).toURL());
                        if (!path.endsWith("/")) { //NOI18N
                            path = path + "/"; //NOI18N
                        }
                        url[0] = new URL(root, path);
                    } else {
                         url = new URL[1];
                        url[0] = FileUtil.getArchiveRoot(Utilities.toURI(file).toURL());
                    }
                    return url;
                }
            } catch (MalformedURLException exc) {
                ErrorManager.getDefault().notify(exc);
            }
            return new URL[0];
        }
        

        private String checkPath(FileObject jarRoot, FileObject fo) {
            String toRet = null;
            FileObject root = JavadocAndSourceRootDetection.findJavadocRoot(jarRoot);
            try {
                if (root != null && !root.equals(jarRoot)) {
                    toRet = FileUtil.getRelativePath(jarRoot, root);
                    fo.setAttribute(ATTR_PATH, toRet);
                }
                fo.setAttribute(ATTR_STAMP, new Date());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return toRet;
        }
        
        private synchronized URL[] checkShadedMultiJars() {
            try {
                List<Coordinates> coordinates = getShadedCoordinates(Utilities.toFile(binary.toURI()));
                File lrf = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile();
                List<URL> urls = new ArrayList<URL>();
                if (coordinates != null) {
                    for (Coordinates coord : coordinates) {
                            File javadocJar = new File(lrf, coord.groupId.replace(".", File.separator) + File.separator + coord.artifactId + File.separator + coord.version + File.separator + coord.artifactId + "-" + coord.version + "-javadoc.jar");
                            URL[] fo = getJavadocJarRoot(javadocJar);
                            if (fo.length == 1) {
                                urls.add(fo[0]);
                            }
                    }
                }
                if (urls.size() > 1) {
                    URL[] shaded = urls.toArray(new URL[0]);
                    return shaded;
                }
            } catch (Exception ex) {
                LOG.log(Level.INFO, "error while examining binary " + binary, ex);
            }
            return new URL[0];
        }        
        
    }    
    

    private static class Javaee6Result implements JavadocForBinaryQuery.Result {

        Javaee6Result() {
        }

        @Override
        public void addChangeListener(ChangeListener changeListener) {
        }

        @Override
        public void removeChangeListener(ChangeListener changeListener) {
        }

        @Override
        public URL[] getRoots() {
            try {
                File j2eeDoc = InstalledFileLocator.getDefault().locate("docs/javaee6-doc-api.zip", "org.netbeans.modules.j2ee.platform", false); // NOI18N
                if (j2eeDoc != null) {
                    URL url = FileUtil.getArchiveRoot(Utilities.toURI(j2eeDoc).toURL());
                    url = new URL(url + "docs/api/"); //NOI18N
                    return new URL[]{url};
                }
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
            return new URL[0];
        }
    }
}