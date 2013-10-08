/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2me.project.api;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.j2me.project.J2MEProject;
import org.netbeans.modules.j2me.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Tomas Zezula
 */
public final class J2MEProjectBuilder {

    private static final Logger LOG = Logger.getLogger(J2MEProjectBuilder.class.getName());
    private static final String DEFAULT_MAIN_TEMPLATE = "Templates/j2me/Midlet.java";  //NOI18N
    private static final SpecificationVersion VERSION_8 = new SpecificationVersion("8.0"); //NOI18N
    
    private final File projectDirectory;
    private final String name;
    private final JavaPlatform platform;
    private final Collection<File> sourceRoots;
    private final Collection<Library> compileLibraries;
    private final Collection<Library> runtimeLibraries;
    private boolean hasDefaultRoots;    
    private String librariesDefinition;
    private String buildXmlName;
    private String distFolder;
    private String mainMIDlet;
    private String mainMIDletTemplate;
    private String manifest;

    private J2MEProjectBuilder(
            @NonNull final File projectDirectory,
            @NonNull final String name,
            @NonNull final JavaPlatform platform) {
        Parameters.notNull("projectDirectory", projectDirectory);   //NOI18N
        Parameters.notNull("name", name);   //NOI18N
        Parameters.notNull("platform", platform);   //NOI18N
        validatePlatform(platform);

        this.projectDirectory = projectDirectory;
        this.name = name;
        this.platform = platform;
        this.sourceRoots = new LinkedHashSet<>();
        this.compileLibraries = new LinkedHashSet<>();
        this.runtimeLibraries = new LinkedHashSet<>();
    }

    @NonNull
    public J2MEProjectBuilder addDefaultSourceRoots() {
        this.hasDefaultRoots = true;
        return this;
    }

    @NonNull
    public J2MEProjectBuilder addSourceRoots(@NonNull final File... folders) {
        Parameters.notNull("folder", folders);   //NOI18N
        for (File f : folders) {
            if (sourceRoots.contains(f)) {
                throw new IllegalArgumentException("The folder: " + f +" is already included in sources."); //NOI18N
            }
        }
        Collections.addAll(sourceRoots, folders);
        return this;
    }

    public J2MEProjectBuilder setLibrariesDefinitionFile (@NullAllowed final String librariesDefinition) {
        this.librariesDefinition = librariesDefinition;
        return this;
    }

    @NonNull
    public J2MEProjectBuilder setMainMIDLetName(@NullAllowed final String midletName) {
        this.mainMIDlet = midletName;
        return this;
    }

    @NonNull
    public J2MEProjectBuilder setMainMIDLetTemplate(@NullAllowed final String template) {
        this.mainMIDletTemplate = template;
        return this;
    }


    public AntProjectHelper build() throws IOException {
        assert projectDirectory != null;
        assert sourceRoots != null;
        final FileObject dirFO = FileUtil.createFolder(this.projectDirectory);
        final AntProjectHelper[] h = new AntProjectHelper[1];
        dirFO.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                final SpecificationVersion sourceLevel = getSourceLevel();
                h[0] = createProject(
                        dirFO,
                        name,
                        sourceLevel,
                        hasDefaultRoots ? "src" : null,     //NOI18N
                        buildXmlName,
                        distFolder,
                        mainMIDlet,
                        manifest,
                        librariesDefinition,
                        toClassPathElements(compileLibraries),
                        toClassPathElements(
                            runtimeLibraries,
                            "${"+ ProjectProperties.JAVAC_CLASSPATH+"}:",   //NOI18N
                            "${"+ProjectProperties.BUILD_CLASSES_DIR+ "}"), //NOI18N
                        platform.getProperties().get(J2MEProjectProperties.PLATFORM_ANT_NAME));
                final J2MEProject p = (J2MEProject) ProjectManager.getDefault().findProject(dirFO);
                ProjectManager.getDefault().saveProject(p);
                final ReferenceHelper refHelper = p.getReferenceHelper();
                try {
                    ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                        @Override
                        public Void run() throws Exception {
                            registerRoots(h[0], refHelper, sourceRoots);
                            ProjectManager.getDefault().saveProject (p);
                            final List<Library> libsToCopy = new ArrayList<Library>();
                            libsToCopy.addAll(compileLibraries);
                            libsToCopy.addAll(runtimeLibraries);
                            copyRequiredLibraries(h[0], refHelper, libsToCopy);
                            ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                            return null;
                        }
                    });
                } catch (MutexException ex) {
                    Exceptions.printStackTrace(ex.getException());
                }

                FileObject srcFolder = null;
                if (hasDefaultRoots) {
                    srcFolder = dirFO.createFolder("src") ; // NOI18N
                } else if (!sourceRoots.isEmpty()) {
                    srcFolder = FileUtil.toFileObject(sourceRoots.iterator().next());
                }
                if (srcFolder != null && mainMIDlet != null) {
                    createMainMIDLet(srcFolder, mainMIDlet, mainMIDletTemplate);
                }
            }
        });
        return h[0];
    }

    public static J2MEProjectBuilder forDirectory(
            @NonNull final File projectDirectory,
            @NonNull final String name,
            @NonNull final JavaPlatform platform) {
        return new J2MEProjectBuilder(projectDirectory, name, platform);
    }

    private static DataObject createMainMIDLet(
            final @NonNull FileObject srcFolder,
            final @NonNull String mainMIDLetName,
            @NullAllowed String mainMIDLetTemplate) throws IOException {

        int lastDotIdx = mainMIDLetName.lastIndexOf( '.' );
        String mName, pName;
        if ( lastDotIdx == -1 ) {
            mName = mainMIDLetName.trim();
            pName = null;
        }
        else {
            mName = mainMIDLetName.substring( lastDotIdx + 1 ).trim();
            pName = mainMIDLetName.substring( 0, lastDotIdx ).trim();
        }

        if ( mName.length() == 0 ) {
            return null;
        }

        if (mainMIDLetTemplate == null) {
            mainMIDLetTemplate = DEFAULT_MAIN_TEMPLATE;
        }

        final FileObject mainTemplate = FileUtil.getConfigFile(mainMIDLetTemplate);

        if ( mainTemplate == null ) {
            LOG.log(
                Level.WARNING,
                "Template {0} not found!",  //NOI18N
                mainMIDLetTemplate);
            return null; // Don't know the template
        }

        DataObject mt = DataObject.find( mainTemplate );

        FileObject pkgFolder = srcFolder;
        if ( pName != null ) {
            String fName = pName.replace( '.', '/' ); // NOI18N
            pkgFolder = FileUtil.createFolder( srcFolder, fName );
        }
        DataFolder pDf = DataFolder.findFolder( pkgFolder );
        DataObject res = mt.createFromTemplate( pDf, mName );
        if (res == null || !res.isValid()) {
            LOG.log(
                Level.WARNING,
                "Template {0} created an invalid DataObject in folder {1}!",  //NOI18N
                new Object[] {
                    mainMIDLetTemplate,
                    FileUtil.getFileDisplayName(pkgFolder)
                });
            return null;
        }
        return res;
    }

    @NonNull
    private SpecificationVersion getSourceLevel() {
        final SpecificationVersion specVersion = platform.getSpecification().getVersion();
        if (VERSION_8.compareTo(specVersion) <= 0) {
            return new SpecificationVersion("1.8"); //NOI18N
        } else {
            return new SpecificationVersion("1.3"); //NOI18N
        }
    }
    
        private static void registerRoots(
            final AntProjectHelper helper,
            final ReferenceHelper refHelper,
            final Collection<? extends File> sourceFolders) {
        if (sourceFolders.isEmpty()) {
            //Nothing to do.
            return;
        }
        final Element data = helper.getPrimaryConfigurationData(true);
        final Document doc = data.getOwnerDocument();
        NodeList nl = data.getElementsByTagNameNS(
            J2MEProject.PROJECT_CONFIGURATION_NAMESPACE,
            "source-roots");
        assert nl.getLength() == 1;
        final Element sourceRoots = (Element) nl.item(0);
        boolean first = true;
        for (File sourceFolder : sourceFolders) {
            String name;
            if (first) {
                //Name the first src root src.dir to be compatible with NB 4.0
                name = "src";               //NOI18N
                first = false;
            } else {
                name = sourceFolder.getName();
            }
            String propName = name + ".dir";    //NOI18N
            int rootIndex = 1;
            EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            while (props.containsKey(propName)) {
                rootIndex++;
                propName = name + rootIndex + ".dir";   //NOI18N
            }
            String srcReference = refHelper.createForeignFileReference(sourceFolder, JavaProjectConstants.SOURCES_TYPE_JAVA);
            Element root = doc.createElementNS (J2MEProject.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute ("id",propName);   //NOI18N
            sourceRoots.appendChild(root);
            props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            props.put(propName,srcReference);
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
        }                                                            
        helper.putPrimaryConfigurationData(data,true);
    }

    private static void copyRequiredLibraries(
            final AntProjectHelper h,
            final ReferenceHelper rh,
            final Collection<? extends Library> libraries) throws IOException {
        if (!h.isSharableProject()) {
            return;
        }
        for (Library library : libraries) {
            final String libName = library.getName();
            if (rh.getProjectLibraryManager().getLibrary(libName) == null
                && LibraryManager.getDefault().getLibrary(libName) != null) {
                rh.copyLibrary(LibraryManager.getDefault().getLibrary(libName)); // NOI18N
            }
        }
    }


    @NonNull
    private static String[] toClassPathElements(
            final @NonNull Collection<? extends Library> libraries,
            final @NonNull String... additionalEntries) {
        final String[] result = new String[libraries.size() + additionalEntries.length];
        final Iterator<? extends Library> it = libraries.iterator();
        for (int i=0; it.hasNext(); i++) {
            final Library lib = it.next();
            result[i] = "${libs." + lib.getName() + ".classpath}" + (it.hasNext() || additionalEntries.length != 0 ? ":":"");    //NOI18N
        }
        System.arraycopy(additionalEntries, 0, result, libraries.size(), additionalEntries.length);
        return result;
    }

    private static void validatePlatform(@NonNull final JavaPlatform platform) {
        final Specification spec = platform.getSpecification();
        if (!"j2me".equals(spec.getName())) {   //NOI18N
            throw new IllegalArgumentException("Invalid Java Platform type: " + spec.getName());    //NOI18N
        }
        if (VERSION_8.compareTo(spec.getVersion()) > 0) {   //NOI18N
            throw new IllegalArgumentException("Invalid Specification Version: " + spec.getVersion());    //NOI18N
        }
    }

    private static AntProjectHelper createProject(
            @NonNull final FileObject dirFO,
            @NonNull final String name,
            @NonNull final SpecificationVersion sourceLevel,
            @NullAllowed String srcRoot,
            @NullAllowed String buildXmlName,
            @NullAllowed String distFolder,
            @NullAllowed String startUpClass,
            @NullAllowed String manifestFile,
            @NullAllowed String librariesDefinition,
            @NonNull String[] compileClassPath,
            @NonNull String[] runtimeClassPath,
            @NonNull final String platformId
            ) throws IOException {

        AntProjectHelper h = ProjectGenerator.createProject(dirFO, J2MEProject.TYPE, librariesDefinition);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        //Project name
        Element nameEl = doc.createElementNS(J2MEProject.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        // Default project source root
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        Element sourceRoots = doc.createElementNS(J2MEProject.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
        if (srcRoot != null) {
            Element root = doc.createElementNS (J2MEProject.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute ("id","src.dir");   //NOI18N
            sourceRoots.appendChild(root);
            ep.setProperty("src.dir", srcRoot); // NOI18N
        }
        data.appendChild (sourceRoots);
        Element testRoots = doc.createElementNS(J2MEProject.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
        data.appendChild (testRoots);
        h.putPrimaryConfigurationData(data, true);        
        //Dist folder
        ep.setProperty(J2MEProjectProperties.DIST_DIR, distFolder != null ? distFolder : "dist"); // NOI18N
        ep.setComment(J2MEProjectProperties.DIST_DIR, new String[] {"# " + NbBundle.getMessage(J2MEProjectBuilder.class, "COMMENT_dist.dir")}, false); // NOI18N
        ep.setProperty(J2MEProjectProperties.DIST_JAR, "${"+J2MEProjectProperties.DIST_DIR+"}/" + PropertyUtils.getUsablePropertyName(name) + ".jar"); // NOI18N
        //Classpaths
        ep.setProperty(ProjectProperties.JAVAC_CLASSPATH, compileClassPath); // NOI18N
        ep.setProperty(ProjectProperties.JAVAC_PROCESSORPATH, new String[] {"${"+ProjectProperties.JAVAC_CLASSPATH+"}"}); // NOI18N
        ep.setProperty(ProjectProperties.RUN_CLASSPATH, runtimeClassPath);
        ep.setProperty(J2MEProjectProperties.DEBUG_CLASSPATH, new String[] { // NOI18N
            "${"+ProjectProperties.RUN_CLASSPATH+"}", // NOI18N
        });
        ep.setComment(J2MEProjectProperties.DEBUG_CLASSPATH, new String[] { // NOI18N
            "# " + NbBundle.getMessage(J2MEProjectBuilder.class, "COMMENT_debug.transport"),    //NOI18N
            "#debug.transport=dt_socket"    //NOI18N
        }, false);
        //Jar options
        ep.setProperty(J2MEProjectProperties.JAR_COMPRESS, "false"); // NOI18N
        //Main class
        ep.setProperty(
            ProjectProperties.MAIN_CLASS,
            startUpClass != null ?
                startUpClass :
                "");    //NOI18N
        //Javac options
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_ENABLED, "true"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR, "false"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, "true"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ""); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT, "${build.generated.sources.dir}/ap-source-output"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS, ""); // NOI18N
        ep.setProperty(J2MEProjectProperties.JAVAC_COMPILERARGS, ""); // NOI18N
        ep.setComment(J2MEProjectProperties.JAVAC_COMPILERARGS, new String[] {
            "# " + NbBundle.getMessage(J2MEProjectBuilder.class, "COMMENT_javac.compilerargs"), // NOI18N
        }, false);
        ep.setProperty(J2MEProjectProperties.JAVAC_SOURCE, sourceLevel.toString());
        ep.setProperty(J2MEProjectProperties.JAVAC_TARGET, sourceLevel.toString());
        ep.setProperty(J2MEProjectProperties.JAVAC_DEPRECATION, "false"); // NOI18N

        //Build properties
        ep.setProperty("build.generated.dir", "${build.dir}/generated"); // NOI18N
        ep.setProperty("meta.inf.dir", "${src.dir}/META-INF"); // NOI18N
        ep.setProperty(ProjectProperties.BUILD_DIR, "build"); // NOI18N
        ep.setComment(ProjectProperties.BUILD_DIR, new String[] {"# " + NbBundle.getMessage(J2MEProjectBuilder.class, "COMMENT_build.dir")}, false); // NOI18N
        ep.setProperty(ProjectProperties.BUILD_CLASSES_DIR, "${"+ProjectProperties.BUILD_DIR+"}/classes"); // NOI18N
        ep.setProperty(J2MEProjectProperties.BUILD_GENERATED_SOURCES_DIR, "${"+ProjectProperties.BUILD_DIR+"}/generated-sources"); // NOI18N
        ep.setProperty(J2MEProjectProperties.BUILD_CLASSES_EXCLUDES, "**/*.java,**/*.form"); // NOI18N

        //Platform
        ep.setProperty(J2MEProjectProperties.PLATFORM_ACTIVE, platformId); // NOI18N

        //Javadoc Properties
        ep.setProperty("dist.javadoc.dir", "${dist.dir}/javadoc"); // NOI18N
        ep.setProperty(J2MEProjectProperties.JAVADOC_PRIVATE, "false"); // NOI18N
        ep.setProperty(J2MEProjectProperties.JAVADOC_NO_TREE, "false"); // NOI18N
        ep.setProperty(J2MEProjectProperties.JAVADOC_USE, "true"); // NOI18N
        ep.setProperty(J2MEProjectProperties.JAVADOC_NO_NAVBAR, "false"); // NOI18N
        ep.setProperty(J2MEProjectProperties.JAVADOC_NO_INDEX, "false"); // NOI18N
        ep.setProperty(J2MEProjectProperties.JAVADOC_SPLIT_INDEX, "true"); // NOI18N
        ep.setProperty(J2MEProjectProperties.JAVADOC_AUTHOR, "false"); // NOI18N
        ep.setProperty(J2MEProjectProperties.JAVADOC_VERSION, "false"); // NOI18N
        ep.setProperty(J2MEProjectProperties.JAVADOC_WINDOW_TITLE, ""); // NOI18N
        ep.setProperty(J2MEProjectProperties.JAVADOC_ENCODING, "${"+J2MEProjectProperties.SOURCE_ENCODING+"}"); // NOI18N
        ep.setProperty(J2MEProjectProperties.JAVADOC_ADDITIONALPARAM, ""); // NOI18N
        Charset enc = FileEncodingQuery.getDefaultEncoding();
        ep.setProperty(J2MEProjectProperties.SOURCE_ENCODING, enc.name());

        //Manifest file
        if (manifestFile != null) {
            ep.setProperty("manifest.file", manifestFile); // NOI18N
        }
        if (buildXmlName != null) {
            ep.put(J2MEProjectProperties.BUILD_SCRIPT, buildXmlName);
        }

        ep.setProperty(J2MEProjectProperties.DIST_ARCHIVE_EXCLUDES,""); //NOI18N
        ep.setComment(J2MEProjectProperties.DIST_ARCHIVE_EXCLUDES,
                new String[] {
                    "# " + NbBundle.getMessage(J2MEProjectBuilder.class, "COMMENT_dist.archive.excludes") //NOI18N
                },
                false);
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.setProperty(ProjectProperties.COMPILE_ON_SAVE, "true"); // NOI18N
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        logUsage();
        return h;
    }


    private static final String loggerName = "org.netbeans.ui.metrics.j2se"; // NOI18N
    private static final String loggerKey = "USG_PROJECT_CREATE_J2ME_EMBEDDED"; // NOI18N

    // http://wiki.netbeans.org/UsageLoggingSpecification
    private static void logUsage() {
        LogRecord logRecord = new LogRecord(Level.INFO, loggerKey);
        logRecord.setLoggerName(loggerName);
        Logger.getLogger(loggerName).log(logRecord);
    }

}
