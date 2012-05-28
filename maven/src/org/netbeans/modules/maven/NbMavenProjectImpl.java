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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.cli.MavenCli;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.VisibilityQuery;
import static org.netbeans.modules.maven.Bundle.*;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.execute.ActiveJ2SEPlatformProvider;
import org.netbeans.modules.maven.api.problem.ProblemReport;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.ProjectProfileHandlerImpl;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.execute.AbstractMavenExecutor;
import org.netbeans.modules.maven.problems.ProblemReporterImpl;
import org.netbeans.modules.maven.spi.queries.JavaLikeRootProvider;
import org.netbeans.spi.java.project.support.LookupMergerSupport;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * A Maven-based project.
 */
public final class NbMavenProjectImpl implements Project {

    //TODO remove
    public static final String PROP_PROJECT = "MavenProject"; //NOI18N
    //TODO remove
    public static final String PROP_RESOURCE = "RESOURCES"; //NOI18N

    private static final Logger LOG = Logger.getLogger(NbMavenProjectImpl.class.getName());
    public static final RequestProcessor RELOAD_RP = new RequestProcessor("Maven project reloading", 1); //NOI18N
    private FileObject fileObject;
    private FileObject folderFileObject;
    private final File projectFile;
    private final Lookup basicLookup;
    private final Lookup completeLookup;
    private final Lookup lookup;
    private final Updater projectFolderUpdater;
    private final Updater userFolderUpdater;
    private Reference<MavenProject> project;
    private ProblemReporterImpl problemReporter;
    private final @NonNull NbMavenProject watcher;
    private final M2ConfigProvider configProvider;
    private final @NonNull MavenProjectPropsImpl auxprops;
    private ProjectProfileHandlerImpl profileHandler;
    @org.netbeans.api.annotations.common.SuppressWarnings("MS_SHOULD_BE_FINAL")
    public static WatcherAccessor ACCESSOR = null;

    static {
        // invokes static initializer of ModelHandle.class
        // that will assign value to the ACCESSOR field above
        Class<?> c = NbMavenProject.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "very wrong, very wrong, yes indeed", ex);
        }
    }

    public static abstract class WatcherAccessor {

        public abstract NbMavenProject createWatcher(NbMavenProjectImpl proj);

        public abstract void doFireReload(NbMavenProject watcher);
    }

    /**
     * Creates a new instance of MavenProject, should never be called by user code.
     * but only by MavenProjectFactory!!!
     */
    NbMavenProjectImpl(FileObject folder, FileObject projectFO, ProjectState projectState) {
        this.projectFile = FileUtil.normalizeFile(FileUtil.toFile(projectFO));
        fileObject = projectFO;
        folderFileObject = folder;
        lookup = Lookups.proxy(new Lookup.Provider() {
            @Override
            public Lookup getLookup() {
                if (completeLookup == null) {
                    //not fully initialized constructor
                    LOG.log(Level.FINE, "accessing project's lookup before the instance is fully initialized", new Exception());
                    assert basicLookup != null;
                    return basicLookup;
                } else {
                    return completeLookup;
                }
            }
        });
        watcher = ACCESSOR.createWatcher(this);
        projectFolderUpdater = new Updater("nb-configuration.xml", "pom.xml"); //NOI18N
        userFolderUpdater = new Updater("settings.xml");//NOI18N
        problemReporter = new ProblemReporterImpl(this);
        M2AuxilaryConfigImpl auxiliary = new M2AuxilaryConfigImpl(projectFO, problemReporter);
        auxprops = new MavenProjectPropsImpl(auxiliary, this);
        profileHandler = new ProjectProfileHandlerImpl(this, auxiliary);
        configProvider = new M2ConfigProvider(this, auxiliary, profileHandler);
        // @PSP's and the like, and PackagingProvider impls, may check project lookup for e.g. NbMavenProject, so init lookup in two stages:
        basicLookup = createBasicLookup(projectState, auxiliary);
        //here we akways load the MavenProject instance because we need to touch the packaging from pom.
        completeLookup = LookupProviderSupport.createCompositeLookup(new PackagingTypeDependentLookup(watcher, basicLookup), "Projects/org-netbeans-modules-maven/Lookup");//NOI18N
    }

    public File getPOMFile() {
        return projectFile;
    }

    public @NonNull NbMavenProject getProjectWatcher() {
        return watcher;
    }

    public ProblemReporterImpl getProblemReporter() {
        return problemReporter;
    }

    /**
     * load a project with properties and profiles other than the current ones.
     * @param embedder embedder to use
     * @param activeProfiles
     * @param properties
     * @return
     */
    public @NonNull MavenProject loadMavenProject(MavenEmbedder embedder, List<String> activeProfiles, Properties properties) {
        try {
            MavenExecutionRequest req = embedder.createMavenExecutionRequest();
            req.addActiveProfiles(activeProfiles);
            req.setPom(projectFile);
            req.setNoSnapshotUpdates(true);
            req.setUpdateSnapshots(false);
            Properties props = createSystemPropsForProjectLoading();
            if (properties != null) {
                props.putAll(properties);
            }
            req.setUserProperties(props);
            //MEVENIDE-634 i'm wondering if this fixes the issue
            req.setInteractiveMode(false);
            req.setOffline(true);
            // recursive == false is important to avoid checking all submodules for extensions
            // that will not be used in current pom anyway..
            // #135070
            req.setRecursive(false);
            MavenExecutionResult res = embedder.readProjectWithDependencies(req, true);
            if (!res.hasExceptions()) {
                return res.getProject();
            } else {
                List<Throwable> exc = res.getExceptions();
                for (Throwable ex : exc) {
                    LOG.log(Level.FINE, "Exception thrown while loading maven project at " + getProjectDirectory(), ex); //NOI18N
                }
            }
        } catch (RuntimeException exc) {
            //guard against exceptions that are not processed by the embedder
            //#136184 NumberFormatException
            LOG.log(Level.INFO, "Runtime exception thrown while loading maven project at " + getProjectDirectory(), exc); //NOI18N
        }
        return getFallbackProject();
    }

    public List<String> getCurrentActiveProfiles() {
        List<String> toRet = new ArrayList<String>();
        toRet.addAll(configProvider.getActiveConfiguration().getActivatedProfiles());
        return toRet;
    }
    private static final Properties statics = new Properties();

    private static Properties cloneStaticProps() {
        synchronized (statics) {
            if (statics.isEmpty()) { // not yet initialized
                // Now a misnomer, but available to activate profiles only during NB project parse:
                statics.setProperty("netbeans.execution", "true"); // NOI18N
                EmbedderFactory.fillEnvVars(statics);
                statics.putAll(AbstractMavenExecutor.excludeNetBeansProperties(System.getProperties()));
            }
            Properties toRet = new Properties();
            toRet.putAll(statics);
            return toRet;
        }
    }

    //#158700
    private Properties createSystemPropsForProjectLoading() {
        Properties props = cloneStaticProps();
        props.putAll(configProvider.getActiveConfiguration().getProperties());
        //TODO the properties for java.home and maybe others shall be relevant to the project setup not ide setup.
        // we got a chicken-egg situation here, the jdk used in project can be defined in the pom.xml file.
        return props;
    }

    //#172952 for property expression resolution we need this to include
    // the properties of the platform to properly resolve stuff like com.sun.boot.class.path
    public Map<? extends String,? extends String> createSystemPropsForPropertyExpressions() {
        Map<String,String> props = NbCollections.checkedMapByCopy(cloneStaticProps(), String.class, String.class, true);
        ActiveJ2SEPlatformProvider platformProvider = getLookup().lookup(ActiveJ2SEPlatformProvider.class);
        if (platformProvider != null) { // may be null inside PackagingProvider
            props.putAll(platformProvider.getJavaPlatform().getSystemProperties());
        }
        props.putAll(configProvider.getActiveConfiguration().getProperties());
        return props;
    }

    /**
     * getter for the maven's own project representation.. this instance is cached but gets reloaded
     * when one the pom files have changed.
     */
    public @NonNull synchronized MavenProject getOriginalMavenProject() {
        MavenProject mp = project == null ? null : project.get();
        if (mp == null) {
            mp = loadOriginalMavenProject();
        }
        project = new SoftReference<MavenProject>(mp);
        return mp;
    }
    
    /**
     * a marginally unreliable, non blocking method for figuring if the model is loaded or not.
     * @return 
     */
    public boolean isMavenProjectLoaded() {
        Reference<MavenProject> prj = project;
        if (prj != null) {
            return prj.get() != null;
        }
        return false;
    }

    @Messages({
        "TXT_RuntimeException=RuntimeException occurred in Apache Maven embedder while loading",
        "TXT_RuntimeExceptionLong=RuntimeException occurred in Apache Maven embedder while loading the project. \n"
            + "This is preventing the project model from loading properly. \n"
            + "Please file a bug report with details about your project and the IDE's log file.\n\n"
    })
    private @NonNull MavenProject loadOriginalMavenProject() {
        long startLoading = System.currentTimeMillis();
        MavenProject newproject = null;
        try {
//                ProgressTransferListener.setAggregateHandle(hndl);
//                hndl.start();
           final  MavenExecutionRequest req = getEmbedder().createMavenExecutionRequest();
                
            //#172526 have the modellineage cache reset at the same time the project cache resets
            profileHandler.clearLineageCache();
            req.addActiveProfiles(getCurrentActiveProfiles());
            req.setPom(projectFile);
            req.setNoSnapshotUpdates(true);
            req.setUpdateSnapshots(false);
            //MEVENIDE-634 i'm wondering if this fixes the issue
            req.setInteractiveMode(false);
            // recursive == false is important to avoid checking all submodules for extensions
            // that will not be used in current pom anyway..
            // #135070
            req.setRecursive(false);
            req.setOffline(true);
            req.setUserProperties(createSystemPropsForProjectLoading());
             MavenExecutionResult res = getEmbedder().readProjectWithDependencies(req, true);
             newproject = res.getProject();
            if (res.hasExceptions()) {
                problemReporter.reportExceptions(res);
            } else {
                problemReporter.doArtifactChecks(newproject);
            }
        } catch (RuntimeException exc) {
            //guard against exceptions that are not processed by the embedder
            //#136184 NumberFormatException
            LOG.log(Level.INFO, "Runtime exception thrown while loading maven project at " + getProjectDirectory(), exc); //NOI18N
            StringWriter wr = new StringWriter();
            PrintWriter pw = new PrintWriter(wr);
            exc.printStackTrace(pw);
            pw.flush();

            ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_HIGH,
                    TXT_RuntimeException(),
                    TXT_RuntimeExceptionLong() + wr.toString(), null);
            problemReporter.addReport(report);

        } finally {
            if (newproject == null) {
                newproject = getFallbackProject();
            }
            long endLoading = System.currentTimeMillis();
            LOG.log(Level.FINE, "Loaded project in {0} msec at {1}", new Object[] {endLoading - startLoading, getProjectDirectory().getPath()});
            if (LOG.isLoggable(Level.FINE) && SwingUtilities.isEventDispatchThread()) {
                LOG.log(Level.FINE, "Project " + getProjectDirectory().getPath() + " loaded in AWT event dispatching thread!", new RuntimeException());
            }
        }
        assert newproject != null;
        return newproject;
    }

    @Messages({
        "LBL_Incomplete_Project_Name=<partially loaded Maven project>",
        "LBL_Incomplete_Project_Desc=Partially loaded Maven project; try building it."
    })
    private MavenProject getFallbackProject() throws AssertionError {
        MavenProject newproject = new MavenProject();
        newproject.setGroupId("error");
        newproject.setArtifactId("error");
        newproject.setVersion("0");
        newproject.setPackaging("pom");
        newproject.setName(LBL_Incomplete_Project_Name());
        newproject.setDescription(LBL_Incomplete_Project_Desc());
        newproject.setFile(projectFile);
        return newproject;
    }

    public void fireProjectReload() {
        //#149566 prevent project firing squads to execute under project mutex.
        if (ProjectManager.mutex().isReadAccess()
                || ProjectManager.mutex().isWriteAccess()
                || SwingUtilities.isEventDispatchThread()) {
            RELOAD_RP.post(new Runnable() {

                @Override
                public void run() {
                    fireProjectReload();
                }
            });
            return;
        }
        problemReporter.clearReports(); //#167741 -this will trigger node refresh?
        MavenProject prj = loadOriginalMavenProject();
        synchronized (this) {
            project = new SoftReference<MavenProject>(prj);
        }
        ACCESSOR.doFireReload(watcher);
        problemReporter.doIDEConfigChecks();
    }

    public static void refreshLocalRepository(NbMavenProjectImpl project) {
        File file = project.getEmbedder().getLocalRepositoryFile();
        FileUtil.refreshFor(file);
    }

    /** Begin listening to pom.xml changes. */
    void attachUpdater() {
        projectFolderUpdater.attachAll(getProjectDirectory());
        userFolderUpdater.attachAll(getHomeDirectory());
    }
   void detachUpdater() {
        projectFolderUpdater.detachAll();
        userFolderUpdater.detachAll();
    }

    /**
     * The root directory of the project where the POM resides.
     */
    @Override
    public FileObject getProjectDirectory() {
        return folderFileObject;
    }

    public FileObject getHomeDirectory() {
        File homeFile = MavenCli.userMavenConfigurationHome;

        FileObject home = null;
        try {
            home = FileUtil.createFolder(homeFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (home == null) {
            //TODO this is a problem, probably UNC path on windows - MEVENIDE-380
            // some functionality won't work
            LOG.log(Level.WARNING, "Cannot convert home dir to FileObject, some functionality won''t work. It''s usually the case on Windows and UNC paths. The path is {0}", homeFile);
        }
        return home;
    }

    public @CheckForNull String getArtifactRelativeRepositoryPath() {
        Artifact artifact = getOriginalMavenProject().getArtifact();
        if (artifact == null) {
            return null;
        }
        return getArtifactRelativeRepositoryPath(artifact);
    }

    /**
     * path of test artifact in local repository
     * @return
     */
    public @CheckForNull String getTestArtifactRelativeRepositoryPath() {
        Artifact main = getOriginalMavenProject().getArtifact();
        if (main == null) {
            return null;
        }

        ArtifactHandlerManager artifactHandlerManager = getEmbedder().lookupComponent(ArtifactHandlerManager.class);
        assert artifactHandlerManager != null : "ArtifactHandlerManager component not found in maven";

        Artifact test = new DefaultArtifact(main.getGroupId(), main.getArtifactId(), main.getVersionRange(),
                Artifact.SCOPE_TEST, "test-jar", "tests", artifactHandlerManager.getArtifactHandler("test-jar"));
        return getArtifactRelativeRepositoryPath(test);

    }

    public String getArtifactRelativeRepositoryPath(@NonNull Artifact artifact) {
        return getEmbedder().getLocalRepository().pathOf(artifact);
    }

    public MavenEmbedder getEmbedder() {
        return EmbedderFactory.getProjectEmbedder();
    }

    public @NonNull MavenProjectPropsImpl getAuxProps() {
        return auxprops;
    }

    public URI[] getSourceRoots(boolean test) {
        List<URI> uris = new ArrayList<URI>();
        for (String root : test ? getOriginalMavenProject().getTestCompileSourceRoots() : getOriginalMavenProject().getCompileSourceRoots()) {
            uris.add(FileUtilities.convertStringToUri(root));
        }
        for (JavaLikeRootProvider rp : getLookup().lookupAll(JavaLikeRootProvider.class)) {
            // XXX for a few purposes (listening) it is desirable to list these even before they exist, but usually it is just noise (cf. #196414 comment #2)
            FileObject root = getProjectDirectory().getFileObject("src/" + (test ? "test" : "main") + "/" + rp.kind());
            if (root != null && root.isFolder()) {
                uris.add(root.toURI());
            }
        }
        return uris.toArray(new URI[uris.size()]);
    }

    public URI[] getGeneratedSourceRoots(boolean test) {
        URI uri = FileUtilities.getDirURI(getProjectDirectory(), test ? "target/generated-test-sources" : "target/generated-sources"); //NOI18N
        Set<URI> uris = new HashSet<URI>();
        File[] roots = new File(uri).listFiles();
        if (roots != null) {
            for (File root : roots) {
                if (!test && root.getName().startsWith("test-")) {
                    continue;
                }
                File[] kids = root.listFiles();
                if (kids != null && /* #190626 */kids.length > 0) {
                    uris.add(root.toURI());
                } else {
                    watcher.addWatchedPath(root.toURI());
                }
            }
        }
        if (test) { // MCOMPILER-167
            roots = new File(FileUtilities.getDirURI(getProjectDirectory(), "target/generated-sources")).listFiles();
            if (roots != null) {
                for (File root : roots) {
                    if (root.getName().startsWith("test-")) {
                        File[] kids = root.listFiles();
                        if (kids != null && kids.length > 0) {
                            uris.add(root.toURI());
                        } else {
                            watcher.addWatchedPath(root.toURI());
                        }
                    }
                }
            }
        }

        String[] buildHelpers = PluginPropertyUtils.getPluginPropertyList(this,
                "org.codehaus.mojo", //NOI18N
                "build-helper-maven-plugin", "sources", "source", test ? "add-test-source" : "add-source"); //NOI18N
        if (buildHelpers != null && buildHelpers.length > 0) {
            File root = FileUtil.toFile(getProjectDirectory());
            for (String helper : buildHelpers) {
                uris.add(FileUtilities.getDirURI(root, helper));
            }
        }

        return uris.toArray(new URI[uris.size()]);
    }

    public URI getWebAppDirectory() {
        //TODO hack, should be supported somehow to read this..
        String prop = PluginPropertyUtils.getPluginProperty(this, Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_WAR, //NOI18N
                "warSourceDirectory", //NOI18N
                "war"); //NOI18N

        prop = prop == null ? "src/main/webapp" : prop; //NOI18N

        return FileUtilities.getDirURI(getProjectDirectory(), prop);
    }

    public URI getSiteDirectory() {
        //TODO hack, should be supported somehow to read this..
        String prop = PluginPropertyUtils.getPluginProperty(this, Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_SITE, //NOI18N
                "siteDirectory", //NOI18N
                "site"); //NOI18N

        prop = prop == null ? "src/site" : prop; //NOI18N

        return FileUtilities.getDirURI(getProjectDirectory(), prop);
    }

    public URI getEarAppDirectory() {
        //TODO hack, should be supported somehow to read this..
        String prop = PluginPropertyUtils.getPluginProperty(this, Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_EAR, //NOI18N
                "earSourceDirectory", //NOI18N
                "ear"); //NOI18N

        prop = prop == null ? "src/main/application" : prop; //NOI18N

        return FileUtilities.getDirURI(getProjectDirectory(), prop);
    }

    public URI[] getResources(boolean test) {
        List<URI> toRet = new ArrayList<URI>();
        List<Resource> res = test ? getOriginalMavenProject().getTestResources() : getOriginalMavenProject().getResources();
        for (Resource elem : res) {
            String dir = elem.getDirectory();
            if (dir == null) {
                continue; // #191742
            }
            if (elem.getTargetPath() != null) {
                continue; // #195928
            }
            URI uri = FileUtilities.getDirURI(getProjectDirectory(), dir);
//            if (new File(uri).exists()) {
            toRet.add(uri);
//            }
        }
        return toRet.toArray(new URI[toRet.size()]);
    }

    public File[] getOtherRoots(boolean test) {
        URI uri = FileUtilities.getDirURI(getProjectDirectory(), test ? "src/test" : "src/main"); //NOI18N
        Set<File> toRet = new HashSet<File>();
        File fil = new File(uri);
        if (fil.exists()) {
            File[] fls = fil.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    //TODO most probably a performance bottleneck of sorts..
                    if ("java".equalsIgnoreCase(name) ||
                            // XXX this cannot continue in a modular system! rethink "other root" system
                            "webapp".equalsIgnoreCase(name)) {
                        return false;
                    }
                    for (JavaLikeRootProvider rp : getLookup().lookupAll(JavaLikeRootProvider.class)) {
                        if (rp.kind().equalsIgnoreCase(name)) {
                            return false;
                        }
                    }
                    return VisibilityQuery.getDefault().isVisible(new File(dir, name));
                }
            });
            if (fls != null) { //#166709 listFiles() shall not return null for existing folders
                // but somehow it does, maybe IO problem? do a proper null check.
                toRet.addAll(Arrays.asList(fls));
            }
        }
        URI[] res = getResources(test);
        for (URI rs : res) {
            File fl = new File(rs);
            //in node view we need only the existing ones, if anything else needs all,
            // a new method is probably necessary..
            if (fl.exists()) {
                toRet.add(fl);
            }
        }
        return toRet.toArray(new File[0]);
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    private static class PackagingTypeDependentLookup extends ProxyLookup implements PropertyChangeListener {

        private final NbMavenProject watcher;
        private final Lookup lookup;
        private String packaging;

        @SuppressWarnings("LeakingThisInConstructor")
        PackagingTypeDependentLookup(NbMavenProject watcher, Lookup lookup) {
            this.watcher = watcher;
            this.lookup = lookup;
            check();
            watcher.addPropertyChangeListener(this);
        }

        private void check() {
            String newPackaging = watcher.getPackagingType();
            if (newPackaging == null) {
                newPackaging = NbMavenProject.TYPE_JAR;
            }
            if (!newPackaging.equals(packaging)) {
                packaging = newPackaging;
                setLookups(LookupProviderSupport.createCompositeLookup(lookup, "Projects/org-netbeans-modules-maven/" + packaging + "/Lookup"));
            }
        }

        public @Override void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProjectImpl.PROP_PROJECT.equals(evt.getPropertyName())) {
                check();
            }
        }
    }

    private Lookup createBasicLookup(ProjectState state, M2AuxilaryConfigImpl auxiliary) {
        return Lookups.fixed(
                    this,
                    fileObject,
                    auxiliary,
                    auxprops,
                    new MavenProjectPropsImpl.Merger(auxprops),
                    profileHandler,
                    configProvider,
                    problemReporter,
                    watcher,
                    state,
                    UILookupMergerSupport.createProjectOpenHookMerger(null),
                    UILookupMergerSupport.createPrivilegedTemplatesMerger(),
                    UILookupMergerSupport.createRecommendedTemplatesMerger(),
                    LookupProviderSupport.createSourcesMerger(),
                    ProjectClassPathModifier.extenderForModifier(this),
                    LookupMergerSupport.createClassPathModifierMerger());
    }

    //MEVENIDE-448 seems to help against creation of duplicate project instances
    // no idea why, it's supposed to be ProjectManager job.. maybe related to
    // maven impl of SubProjectProvider or FileOwnerQueryImplementation
    //TODO need to investigate why it's like that..
    @Override
    public int hashCode() {
        return getProjectDirectory().hashCode() * 13;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Project) {
            return getProjectDirectory().equals(((Project) obj).getProjectDirectory());
        }
        return false;
    }

    @Override
    public String toString() {
        return "Maven[" + fileObject.getPath() + "]"; //NOI18N

    }

    private class Updater implements FileChangeListener {

        private String[] filesToWatch;
        private long lastTime = 0;
        private FileObject folder;
        private final FileChangeListener listener = FileUtil.weakFileChangeListener(this, null);

        /** Relative file paths to watch. */
        Updater(String... toWatch) {
            Arrays.sort(toWatch);
            filesToWatch = toWatch;
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fileAttributeEvent) {
        }

        @Override
        public void fileChanged(FileEvent fileEvent) {
            if (!fileEvent.getFile().isFolder()) {
                String nameExt = fileEvent.getFile().getNameExt();
                if (Arrays.binarySearch(filesToWatch, nameExt) != -1 && lastTime < fileEvent.getTime()) {
                    lastTime = System.currentTimeMillis();
//                    System.out.println("fired based on " + fileEvent.getFile() + fileEvent.getTime());
                    NbMavenProject.fireMavenProjectReload(NbMavenProjectImpl.this);
                }
            }
        }

        @Override
        public void fileDataCreated(FileEvent fileEvent) {
            //TODO shall also include the parent of the pom if available..
            if (fileEvent.getFile().isFolder()) {
                String nameExt = fileEvent.getFile().getNameExt();
                if (Arrays.binarySearch(filesToWatch, nameExt) != -1 && lastTime < fileEvent.getTime()) {
                    lastTime = System.currentTimeMillis();
//                    System.out.println("fired based on " + fileEvent.getFile() + fileEvent.getTime());
                    fileEvent.getFile().addFileChangeListener(this);
                    NbMavenProject.fireMavenProjectReload(NbMavenProjectImpl.this);
                }
            }
        }

        @Override
        public void fileDeleted(FileEvent fileEvent) {
            if (!fileEvent.getFile().isFolder()) {
                lastTime = System.currentTimeMillis();
                fileEvent.getFile().removeFileChangeListener(this);
                NbMavenProject.fireMavenProjectReload(NbMavenProjectImpl.this);
            }
        }

        @Override
        public void fileFolderCreated(FileEvent fileEvent) {
            //TODO possibly remove this fire.. watch for actual path..
//            NbMavenProject.fireMavenProjectReload(NbMavenProjectImpl.this);
        }

        @Override
        public void fileRenamed(FileRenameEvent fileRenameEvent) {
        }

        void attachAll(FileObject fo) {
            if (fo != null) {
                folder = fo;
                fo.addFileChangeListener(listener);
                for (String file : filesToWatch) {
                    FileObject fobj = fo.getFileObject(file);
                    if (fobj != null) {
                        fobj.addFileChangeListener(listener);
                    }
                }
            }
        }

        void detachAll() {
            if (folder != null) {
                folder.removeFileChangeListener(listener);
                for (String file : filesToWatch) {
                    FileObject fobj = folder.getFileObject(file);
                    if (fobj != null) {
                        fobj.removeFileChangeListener(listener);
                    }
                }
                folder = null;
            }
        }
    }

}
