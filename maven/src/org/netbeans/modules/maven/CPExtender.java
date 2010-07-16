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

package org.netbeans.modules.maven;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Repository;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 * an implementation of ProjectClassPathModifierImplementation that tried to match 
 * maven dependencies to the way classpath items are added through this api.
 * @author mkleint
 */
@SuppressWarnings("deprecation")
public class CPExtender extends ProjectClassPathModifierImplementation implements org.netbeans.spi.java.project.classpath.ProjectClassPathExtender {

    private NbMavenProjectImpl project;
    private static final String POM_XML = "pom.xml"; //NOI18N
    
    /**
     * ClassPath for compiling only, but not running. In practice this means that scope
     * for the artefacts with this classpath will be set to <code>provided</code>
     * if added to a source group, and <code>test</code> if added to test source group.
     * This constant is in practice a friend API, even if it is hardcoded in the
     * consuming module (see e.g. bug 186221).
     */
    public static final String CLASSPATH_COMPILE_ONLY = "classpath/compile_only";

    /** Creates a new instance of CPExtender */
    public CPExtender(NbMavenProjectImpl project) {
        this.project = project;
    }
    
    public boolean addLibrary(final Library library) throws IOException {
        final Boolean[] added = new Boolean[1];
        ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            public void performOperation(POMModel model) {
                try {
                    added[0] = addLibrary(library, model, null);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    added[0] = Boolean.FALSE;
                }
            }
        };
        FileObject pom = project.getProjectDirectory().getFileObject(POM_XML);//NOI18N
        org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
        //TODO is the manual reload necessary if pom.xml file is being saved?
//                NbMavenProject.fireMavenProjectReload(project);
        if (added[0]) {
            project.getLookup().lookup(NbMavenProject.class).triggerDependencyDownload();
        }

        return added[0];
    }
    
    public boolean addArchiveFile(FileObject arch) throws IOException {
        final FileObject file = FileUtil.getArchiveFile(arch);
        if (file.isFolder()) {
            throw new IOException("Cannot add folders to Maven projects as dependencies: " + file.getURL()); //NOI18N
        }

        final Boolean[] added = new Boolean[1];
        ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            public void performOperation(POMModel model) {
                try {
                    added[0] = addArchiveFile(file, model, null);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    added[0] = Boolean.FALSE;
                }
            }
        };
        FileObject pom = project.getProjectDirectory().getFileObject(POM_XML);//NOI18N
        org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
        //TODO is the manual reload necessary if pom.xml file is being saved?
//                NbMavenProject.fireMavenProjectReload(project);
        if (added[0]) {
            project.getLookup().lookup(NbMavenProject.class).triggerDependencyDownload();
        }
        return added[0];
    }

    private boolean addLibrary(Library library, POMModel model, String scope) throws IOException {
        boolean added = checkLibraryForPoms(library, model, scope);
        if (!added) {
            List<URL> urls = library.getContent("classpath"); //NOI18N
            added = urls.size() > 0;
            assert model != null;
            for (URL url : urls) {
                FileObject fo = URLMapper.findFileObject(FileUtil.getArchiveFile(url));
                assert fo != null;
                if (fo.isFolder()) {
                    throw new IOException("Cannot add folders to Maven projects as dependencies: " + fo.getURL()); //NOI18N
                }
                added = added && addArchiveFile(fo, model, scope);
            }
        }
        return added;
    }
    
    private boolean addArchiveFile(FileObject file, POMModel mdl, String scope) throws IOException {
            
        String[] dep = checkRepositoryIndices(FileUtil.toFile(file));
        //if not found anywhere, add to a custom file:// based repository structure within the project's directory.
        if (dep == null || "unknown.binary".equals(dep[0])) {  //NOI18N
            //also go this route when the artifact was found in local repo but is of unknown.binary groupId.
            dep = new String[3];
            dep[0] = "unknown.binary"; //NOI18N
            dep[1] = file.getName();
            dep[2] = "SNAPSHOT"; //NOI18N
            addJarToPrivateRepo(file, mdl, dep);
        }
        if (dep != null) {
            Dependency dependency = ModelUtils.checkModelDependency(mdl, dep[0], dep[1], true);
            dependency.setVersion(dep[2]);
            if (scope != null) {
                dependency.setScope(scope);
            }
            return true;
        }
        return false;
    }
    
    private String[] checkRepositoryIndices(File file) {
        List<NBVersionInfo> lst = RepositoryQueries.findBySHA1(file);
        for (NBVersionInfo elem : lst) {
            String[] dep = new String[3];
            dep[0] = elem.getGroupId();
            dep[1] = elem.getArtifactId();
            dep[2] = elem.getVersion();
            return dep;
        }
        return null;
    }
    
    /**
     */
    private boolean checkLibraryForPoms(Library library, POMModel model, String scope) {
        if (!"j2se".equals(library.getType())) {//NOI18N
            //only j2se library supported for now..
            return false;
        }
        List<URL> poms = library.getContent("maven-pom"); //NOI18N
        boolean added = false;
        if (poms != null && poms.size() > 0) {
            for (URL pom : poms) {
                Set<String> repos = RepositoryPreferences.getInstance().getKnownRepositoryUrls();
                ModelUtils.LibraryDescriptor result = ModelUtils.checkLibrary(pom, repos);
                if (result != null) {
                    added = true;
                    //set dependency
                    Dependency dep = ModelUtils.checkModelDependency(model, result.getGroupId(), result.getArtifactId(), true);
                    dep.setVersion(result.getVersion());
                    if (scope != null) {
                        dep.setScope(scope);
                    }
                    if (result.getClassifier() != null) {
                        dep.setClassifier(result.getClassifier());
                    }
                    //set repository
                    org.netbeans.modules.maven.model.pom.Repository reposit = ModelUtils.addModelRepository(
                            project.getOriginalMavenProject(), model, result.getRepoRoot());
                    if (reposit != null) {
                        reposit.setId(library.getName());
                        reposit.setLayout(result.getRepoType());
                        reposit.setName("Repository for library " + library); //NOI18N - content coming into the pom.xml file
                    }
                }
            }
        }
        return added;
    }
    
    static Pattern DEFAULT = Pattern.compile("(.+)[/]{1}(.+)[/]{1}(.+)[/]{1}(.+)\\.pom"); //NOI18N
    static Pattern LEGACY = Pattern.compile("(.+)[/]{1}poms[/]{1}([a-zA-Z0-9_]+[a-zA-Z\\-_]+)[\\-]{1}([0-9]{1}.+)\\.pom"); //NOI18N
    /**
     * @returns [0] type - default/legacy
     *          [1] repo root
     *          [2] groupId
     *          [3] artifactId
     *          [4] version
     *          [5] classifier (optional, not part of path, but url's ref)
     */ 
        
    public boolean addAntArtifact(AntArtifact arg0, URI arg1) throws IOException {
        throw new IOException("Cannot add Ant based projects as subprojecs to Maven projects."); //NOI18N
    }
    
    public SourceGroup[] getExtensibleSourceGroups() {
        Sources s = this.project.getLookup().lookup(Sources.class);
        assert s != null;
        List<SourceGroup> grps = new ArrayList<SourceGroup>();
        SourceGroup[] java = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (java != null) {
            grps.addAll(Arrays.asList(java));
        }
        SourceGroup[] res = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
        if (res != null) {
            grps.addAll(Arrays.asList(res));
        }
        SourceGroup[] web = s.getSourceGroups("doc_root");
        if (web != null) {
            grps.addAll(Arrays.asList(web));
        }
        
        return grps.toArray(new SourceGroup[0]);
    }
    
    @Override
    public String[] getExtensibleClassPathTypes(SourceGroup arg0) {
        return new String[] {
            ClassPath.COMPILE,
            ClassPath.EXECUTE,
            CLASSPATH_COMPILE_ONLY,
            JavaClassPathConstants.PROCESSOR_PATH
        };
    }

    public boolean addLibraries(final Library[] libraries, SourceGroup grp, String type) throws IOException {
        final Boolean[] added = new Boolean[1];
        added[0] = libraries.length > 0;
        String scope = ClassPath.EXECUTE.equals(type) ? Artifact.SCOPE_RUNTIME : null; //NOI18N
        //figure if we deal with test or regular sources.
        String name = grp.getName();
        if (MavenSourcesImpl.NAME_TESTSOURCE.equals(name)) {
            scope = "test"; //NOI18N
        }
        if (scope == null && 
            (CLASSPATH_COMPILE_ONLY.equals(type) || JavaClassPathConstants.PROCESSOR_PATH.equals(type))) {
            scope = Artifact.SCOPE_PROVIDED;
        }
        final String fScope = scope;
        ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            public void performOperation(POMModel model) {
                for (Library library : libraries) {
                    try {
                        added[0] = added[0] && addLibrary(library, model, fScope);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                        added[0] = Boolean.FALSE;
                    }
                }
            }
        };
        FileObject pom = project.getProjectDirectory().getFileObject(POM_XML);//NOI18N
        org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
        //TODO is the manual reload necessary if pom.xml file is being saved?
//                NbMavenProject.fireMavenProjectReload(project);
        if (added[0]) {
            project.getLookup().lookup(NbMavenProject.class).triggerDependencyDownload();
        }
        return added[0];
    }
    
    public boolean removeLibraries(Library[] arg0, SourceGroup arg1,
                                      String arg2) throws IOException,
                                                          UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported in maven projects.");//NOI18N
    }
    
    public boolean addRoots(final URL[] urls, SourceGroup grp, String type) throws IOException {
        final Boolean[] added = new Boolean[1];
        added[0] = urls.length > 0;
        String scope = ClassPath.EXECUTE.equals(type) ? Artifact.SCOPE_RUNTIME : null;//NOI18N
        //figure if we deal with test or regular sources.
        String name = grp.getName();
        if (MavenSourcesImpl.NAME_TESTSOURCE.equals(name)) {
            scope = "test"; //NOI18N
        }
        if (scope == null && JavaClassPathConstants.PROCESSOR_PATH.equals(type)) {
            scope = Artifact.SCOPE_PROVIDED;
        }
        final String fScope = scope;
        ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            public void performOperation(POMModel model) {
                for (URL url : urls) {
                    URL fileUrl = FileUtil.getArchiveFile(url);
                    if (fileUrl != null) {
                        FileObject fo  = URLMapper.findFileObject(fileUrl);
                        assert fo != null;
                        try {
                            added[0] = added[0] && addArchiveFile(fo, model, fScope);
                        } catch (IOException ex) {
                            added[0] = Boolean.FALSE;
                            Exceptions.printStackTrace(ex);
                        }
                    } else {
                        Logger.getLogger(CPExtender.class.getName()).info("Adding non-jar root to Maven projects makes no sense. (" + url + ")"); //NOI18N
                    }
                }
            }
        };
        FileObject pom = project.getProjectDirectory().getFileObject(POM_XML);//NOI18N
        org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
        //TODO is the manual reload necessary if pom.xml file is being saved?
//                NbMavenProject.fireMavenProjectReload(project);
        if (added[0]) {
            project.getLookup().lookup(NbMavenProject.class).triggerDependencyDownload();
        }
        return added[0];
    }

    @Override
    protected boolean addProjects(final Project[] projects, SourceGroup sg, String classPathType) throws IOException, UnsupportedOperationException {
        final Boolean[] added = new Boolean[2];

        added[0] = false;
        added[1] = false;
        String scope = ClassPath.EXECUTE.equals(classPathType) ? Artifact.SCOPE_RUNTIME : null;//NOI18N
        //figure if we deal with test or regular sources.
        String name = sg.getName();
        if (MavenSourcesImpl.NAME_TESTSOURCE.equals(name)) {
            scope = "test"; //NOI18N
        }
        if (scope == null && JavaClassPathConstants.PROCESSOR_PATH.equals(classPathType)) {
            scope = Artifact.SCOPE_PROVIDED;
        }
        final String fScope = scope;
        ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            public void performOperation(POMModel model) {
                Set<Artifact> arts = project.getOriginalMavenProject().getArtifacts();
                for (Project prj: projects) {
                    NbMavenProject nbprj = prj.getLookup().lookup(NbMavenProject.class);
                    if (nbprj != null) {
                        MavenProject mp = nbprj.getMavenProject();
                        String id = mp.getArtifact().getId();
                        for (Artifact a : arts) {
                            if (a.getId().equals(id)) {
                                //already there..
                                continue;
                            }
                        }
                        Dependency dependency = ModelUtils.checkModelDependency(model, mp.getGroupId(), mp.getArtifactId(), true);
                        dependency.setVersion(mp.getVersion());
                        if (fScope != null) {
                            dependency.setScope(fScope);
                        } else {
                            if (NbMavenProject.TYPE_EJB.equals(nbprj.getPackagingType()) ||
                                NbMavenProject.TYPE_WAR.equals(nbprj.getPackagingType())) {
                                dependency.setScope(Artifact.SCOPE_PROVIDED);
                            }
                        }
                        added[0] = true;
                    } else {
                        // unsupported usecase, not a maven project
                        added[1] = true;
                    }
                }
            }
        };
        FileObject pom = project.getProjectDirectory().getFileObject(POM_XML);//NOI18N
        org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
        if (added[1]) {
            //throw late to prevent the pom model to go bust eventually
            throw new UnsupportedOperationException("Attempting to add a non-Maven project dependency to a Maven project, not supported."); //NOI18N
        }

        return added[0];

    }

    public boolean removeRoots(URL[] arg0, SourceGroup arg1, String arg2) throws IOException,
                                                                                    UnsupportedOperationException {
        throw new UnsupportedOperationException("Removing binary dependencies is not supported by Maven projects.");
    }
    
    public boolean addAntArtifacts(AntArtifact[] arg0, URI[] arg1,
                                      SourceGroup arg2, String arg3) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot add Ant based projects as subprojects to Maven projects.");//NOI18N
    }
    
    public boolean removeAntArtifacts(AntArtifact[] arg0, URI[] arg1,
                                         SourceGroup arg2, String arg3) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot remove Ant based projects as subprojects from Maven projects.");//NOI18N
    }
    
    private void addJarToPrivateRepo(FileObject file, POMModel mdl, String[] dep) throws IOException {
        //first add the local repo to
        List<Repository> repos = mdl.getProject().getRepositories();
        boolean found = false;
        String path = null;
        if (repos != null) {
            for (Repository repo : repos) {
                if ("unknown-jars-temp-repo".equals(repo.getId())) { //NOI18N
                    found = true;
                    String url = repo.getUrl();
                    if (url.startsWith("file:${project.basedir}/")) { //NOI18N
                        path = url.substring("file:${project.basedir}/".length()); //NOI18N
                    } else {
                        path = "lib"; //NOI18N
                    }
                    break;
                }
            }
        }
        if (!found) {
            Repository repo = mdl.getFactory().createRepository();
            repo.setId("unknown-jars-temp-repo"); //NOI18N
            repo.setName("A temporary repository created by NetBeans for libraries and jars it could not identify. Please replace the dependencies in this repository with correct ones and delete this repository."); //NOI18N
            repo.setUrl("file:${project.basedir}/lib"); //NOI18N
            mdl.getProject().addRepository(repo);
            path = "lib"; //NOI18N
        }
        assert path != null;
        FileObject root = FileUtil.createFolder(project.getProjectDirectory(), path);
        FileObject grp = FileUtil.createFolder(root, dep[0].replace('.', '/')); //NOI18N
        FileObject art = FileUtil.createFolder(grp, dep[1]);
        FileObject ver = FileUtil.createFolder(art, dep[2]);
        String name = dep[1] + "-" + dep[2];//NOI18N
        if (ver.getFileObject(name, file.getExt()) == null) { //#160803
            FileUtil.copyFile(file, ver, dep[1] + "-" + dep[2], file.getExt()); //NOI18N
        }
    }
}
