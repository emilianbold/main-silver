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
package org.netbeans.modules.cnd.discovery.projectimport;

import java.util.concurrent.ExecutionException;
import org.netbeans.modules.cnd.builds.ImportUtils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.actions.CMakeAction;
import org.netbeans.modules.cnd.actions.MakeAction;
import org.netbeans.modules.cnd.actions.QMakeAction;
import org.netbeans.modules.cnd.actions.ShellRunAction;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.builds.CMakeExecSupport;
import org.netbeans.modules.cnd.builds.QMakeExecSupport;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.wizard.ConsolidationStrategyPanel;
import org.netbeans.modules.cnd.discovery.wizard.DiscoveryWizardDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.SelectConfigurationPanel;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.api.FileConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.api.ProjectConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.bridge.DiscoveryProjectGenerator;
import org.netbeans.modules.cnd.discovery.wizard.bridge.ProjectBridge;
import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.netbeans.modules.cnd.execution.ExecutionSupport;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.api.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.ProjectSupport;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.netbeans.modules.cnd.makeproject.api.wizards.WizardConstants;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Simon
 */
public class ImportProject implements PropertyChangeListener {

    private static boolean TRACE = Boolean.getBoolean("cnd.discovery.trace.projectimport"); // NOI18N
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.cnd.discovery.projectimport.ImportProject"); // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(ImportProject.class.getName(), 2);
    private String nativeProjectPath;
    private FileObject nativeProjectFO;
    private File projectFolder;
    private String projectName;
    private String makefileName = "Makefile";  // NOI18N
    private String makefilePath;
    private String configurePath;
    private String configureRunFolder;
    private String configureArguments;
    private boolean runConfigure = false;
    private boolean manualCA = false;
    private boolean buildArifactWasAnalyzed = false;
    private boolean setAsMain;
    private final String hostUID;
    private final ExecutionEnvironment executionEnvironment;
    private final ExecutionEnvironment fileSystemExecutionEnvironment;
    private final boolean fullRemote;
    private final MakeProjectOptions.PathMode pathMode;
    private CompilerSet toolchain;
    private boolean defaultToolchain;
    private String workingDir;
    private String buildCommand = "${MAKE} -f Makefile";  // NOI18N
    private String cleanCommand = "${MAKE} -f Makefile clean";  // NOI18N
    private String buildResult = "";  // NOI18N
    private Project makeProject;
    private boolean runMake;
    private String includeDirectories = ""; // NOI18N
    private String macros = ""; // NOI18N
    private String consolidationStrategy = ConsolidationStrategyPanel.FILE_LEVEL;
    private Iterator<SourceFolderInfo> sources;
    private Iterator<SourceFolderInfo> tests;
    private String sourceFoldersFilter = null;
    private FileObject configureFileObject;
    private Map<Step, State> importResult = new EnumMap<Step, State>(Step.class);

    public ImportProject(WizardDescriptor wizard) {
        if (TRACE) {
            logger.setLevel(Level.ALL);
        }
        Boolean b = (Boolean) wizard.getProperty(WizardConstants.PROPERTY_FULL_REMOTE);
        fullRemote = (b == null) ? false : b.booleanValue();
        pathMode = fullRemote ? MakeProjectOptions.PathMode.ABS : MakeProjectOptions.getPathMode();
        if (Boolean.TRUE.equals(wizard.getProperty(WizardConstants.PROPERTY_SIMPLE_MODE))) { // NOI18N
            simpleSetup(wizard);
        } else {
            customSetup(wizard);
        }
        hostUID = (String) wizard.getProperty(WizardConstants.PROPERTY_HOST_UID); // NOI18N
        if (hostUID == null) {
            executionEnvironment = ServerList.getDefaultRecord().getExecutionEnvironment();
        } else {
            executionEnvironment = ExecutionEnvironmentFactory.fromUniqueID(hostUID);
        }
        fileSystemExecutionEnvironment = (fullRemote) ? executionEnvironment : ExecutionEnvironmentFactory.getLocal();
        assert nativeProjectPath != null;
    }

    private void simpleSetup(WizardDescriptor wizard) {
        projectFolder = (File) wizard.getProperty(WizardConstants.PROPERTY_PROJECT_FOLDER); 
        nativeProjectPath = (String) wizard.getProperty(WizardConstants.PROPERTY_NATIVE_PROJ_DIR);
        nativeProjectFO = (FileObject) wizard.getProperty(WizardConstants.PROPERTY_NATIVE_PROJ_FO);
        projectName = projectFolder.getName();
        workingDir = nativeProjectPath;
        configurePath = (String) wizard.getProperty(WizardConstants.PROPERTY_CONFIGURE_SCRIPT_PATH); 
        if (configurePath != null) {
            configureArguments = (String) wizard.getProperty("realFlags");  // NOI18N
            runConfigure = true;
            // the best guess
            makefilePath = (String) wizard.getProperty(WizardConstants.PROPERTY_USER_MAKEFILE_PATH); 
            if (makefilePath == null) {
                makefilePath = nativeProjectPath + "/Makefile"; // NOI18N;
            }
            configureRunFolder = (String) wizard.getProperty(WizardConstants.PROPERTY_CONFIGURE_RUN_FOLDER);
        } else {
            makefilePath = (String) wizard.getProperty(WizardConstants.PROPERTY_USER_MAKEFILE_PATH); 
        }
        if (fullRemote) {
            makefileName = (makefilePath == null) ? "Makefile" : CndPathUtilitities.getBaseName(makefilePath); //NOI18N
        } else {
            makefileName = "Makefile-" + projectName + ".mk"; // NOI18N
        }
        runMake = Boolean.TRUE.equals(wizard.getProperty("buildProject"));  // NOI18N
        setAsMain = Boolean.TRUE.equals(wizard.getProperty("setMain"));  // NOI18N
        toolchain = (CompilerSet)wizard.getProperty(WizardConstants.PROPERTY_TOOLCHAIN);
        defaultToolchain = Boolean.TRUE.equals(wizard.getProperty(WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT));
        
        List<SourceFolderInfo> list = new ArrayList<SourceFolderInfo>();
        list.add(new SourceFolderInfo() {

            @Override
            public FileObject getFileObject() {
                return nativeProjectFO;
            }

            @Override
            public String getFolderName() {
                return nativeProjectFO.getNameExt();
            }

            @Override
            public boolean isAddSubfoldersSelected() {
                return true;
            }
        });
        sources = list.iterator();
        sourceFoldersFilter = MakeConfigurationDescriptor.DEFAULT_IGNORE_FOLDERS_PATTERN_EXISTING_PROJECT;
    }

    private void customSetup(WizardDescriptor wizard) {
        projectFolder = (File) wizard.getProperty(WizardConstants.PROPERTY_PROJECT_FOLDER); 
        nativeProjectPath = (String) wizard.getProperty(WizardConstants.PROPERTY_NATIVE_PROJ_DIR); 
        nativeProjectFO = (FileObject) wizard.getProperty(WizardConstants.PROPERTY_NATIVE_PROJ_FO); 
        projectFolder = (File) wizard.getProperty(WizardConstants.PROPERTY_PROJECT_FOLDER);
        projectName = (String) wizard.getProperty(WizardConstants.PROPERTY_NAME);
        workingDir = (String) wizard.getProperty(WizardConstants.PROPERTY_WORKING_DIR);
        buildCommand = (String) wizard.getProperty(WizardConstants.PROPERTY_BUILD_COMMAND);
        cleanCommand = (String) wizard.getProperty(WizardConstants.PROPERTY_CLEAN_COMMAND);
        buildResult = (String) wizard.getProperty(WizardConstants.PROPERTY_BUILD_RESULT); 
        includeDirectories = (String) wizard.getProperty(WizardConstants.PROPERTY_INCLUDES); 
        macros = (String) wizard.getProperty(WizardConstants.PROPERTY_MACROS); 
        makefilePath = (String) wizard.getProperty(WizardConstants.PROPERTY_USER_MAKEFILE_PATH); 
        if (fullRemote) {
            makefileName = (makefilePath == null) ? "Makefile" : CndPathUtilitities.getBaseName(makefilePath); //NOI18N
        } else {
            makefileName = (String) wizard.getProperty(WizardConstants.PROPERTY_GENERATED_MAKEFILE_NAME); 
        }
        configurePath = (String) wizard.getProperty(WizardConstants.PROPERTY_CONFIGURE_SCRIPT_PATH);
        configureRunFolder = (String) wizard.getProperty(WizardConstants.PROPERTY_CONFIGURE_RUN_FOLDER);
        configureArguments = (String) wizard.getProperty(WizardConstants.PROPERTY_CONFIGURE_SCRIPT_ARGS);
        runConfigure = "true".equals(wizard.getProperty(WizardConstants.PROPERTY_RUN_CONFIGURE)); // NOI18N
        consolidationStrategy = (String) wizard.getProperty(WizardConstants.PROPERTY_CONSOLIDATION_LEVEL); 
        @SuppressWarnings("unchecked")
        Iterator<SourceFolderInfo> it = (Iterator<SourceFolderInfo>) wizard.getProperty(WizardConstants.PROPERTY_SOURCE_FOLDERS); 
        sources = it;
        @SuppressWarnings("unchecked")
        Iterator<SourceFolderInfo> it2 = (Iterator<SourceFolderInfo>) wizard.getProperty(WizardConstants.PROPERTY_TEST_FOLDERS); 
        tests = it2;
        sourceFoldersFilter = (String) wizard.getProperty(WizardConstants.PROPERTY_SOURCE_FOLDERS_FILTER);
        runConfigure = "true".equals(wizard.getProperty(WizardConstants.PROPERTY_RUN_CONFIGURE)); // NOI18N
        if (runConfigure) {
            runMake = true;
        } else {
            runMake = "true".equals(wizard.getProperty(WizardConstants.PROPERTY_RUN_REBUILD)); // NOI18N
        }
        manualCA = "true".equals(wizard.getProperty(WizardConstants.PROPERTY_MANUAL_CODE_ASSISTANCE)); // NOI18N
        setAsMain = Boolean.TRUE.equals(wizard.getProperty(WizardConstants.PROPERTY_SET_AS_MAIN));
        toolchain = (CompilerSet)wizard.getProperty(WizardConstants.PROPERTY_TOOLCHAIN);
        defaultToolchain = Boolean.TRUE.equals(wizard.getProperty(WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT));
    }

//    private String normalizeAbsolutePath(String path) {
//        ExecutionEnvironment fileSystemEnv = fullRemote ? executionEnvironment : ExecutionEnvironmentFactory.getLocal();
//        return RemoteFileUtil.normalizeAbsolutePath(path, fileSystemEnv);
//    }

    public Set<FileObject> create() throws IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();
        projectFolder = CndFileUtils.normalizeFile(projectFolder); // always local
        MakeConfiguration extConf = new MakeConfiguration(projectFolder.getPath(), "Default", MakeConfiguration.TYPE_MAKEFILE, hostUID, toolchain, defaultToolchain); // NOI18N
        String workingDirRel;
        if (fullRemote) { //XXX:fullRemote {
            workingDirRel = nativeProjectFO.getPath();
        } else {
            workingDirRel = ProjectSupport.toProperPath(projectFolder.getPath(), CndPathUtilitities.naturalizeSlashes(workingDir), pathMode);
        }
        workingDirRel = CndPathUtilitities.normalizeSlashes(workingDirRel);
        extConf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(workingDirRel);
        extConf.getMakefileConfiguration().getBuildCommand().setValue(buildCommand);
        extConf.getMakefileConfiguration().getCleanCommand().setValue(cleanCommand);
        // Build result
        if (buildResult != null && buildResult.length() > 0) {
            buildResult = ProjectSupport.toProperPath(projectFolder.getPath(), CndPathUtilitities.naturalizeSlashes(buildResult), pathMode);
            buildResult = CndPathUtilitities.normalizeSlashes(buildResult);
            extConf.getMakefileConfiguration().getOutput().setValue(buildResult);
        }
        // Include directories
        if (includeDirectories != null && includeDirectories.length() > 0) {
            StringTokenizer tokenizer = new StringTokenizer(includeDirectories, ";"); // NOI18N
            List<String> includeDirectoriesVector = new ArrayList<String>();
            while (tokenizer.hasMoreTokens()) {
                String includeDirectory = tokenizer.nextToken();
                includeDirectory = CndPathUtilitities.toRelativePath(projectFolder.getPath(), CndPathUtilitities.naturalizeSlashes(includeDirectory));
                includeDirectory = CndPathUtilitities.normalizeSlashes(includeDirectory);
                includeDirectoriesVector.add(includeDirectory);
            }
            extConf.getCCompilerConfiguration().getIncludeDirectories().setValue(includeDirectoriesVector);
            extConf.getCCCompilerConfiguration().getIncludeDirectories().setValue(new ArrayList<String>(includeDirectoriesVector));
        }
        // Macros
        if (macros != null && macros.length() > 0) {
            StringTokenizer tokenizer = new StringTokenizer(macros, "; "); // NOI18N
            ArrayList<String> list = new ArrayList<String>();
            while (tokenizer.hasMoreTokens()) {
                list.add(tokenizer.nextToken());
            }
            // FIXUP
            extConf.getCCompilerConfiguration().getPreprocessorConfiguration().getValue().addAll(list);
            extConf.getCCCompilerConfiguration().getPreprocessorConfiguration().getValue().addAll(list);
        }
        // Add makefile and configure script to important files
        ArrayList<String> importantItems = new ArrayList<String>();
        if (makefilePath != null && makefilePath.length() > 0) {
            if (fullRemote) {
                CndUtils.assertAbsolutePathInConsole(makefilePath);
                // we have to switch to file objects; but can't remember it right here since it may not exist
                // makeFileObject = RemoteFileUtil.getFileObject(makefilePath, executionEnvironment);
            } else {
                // see comment above
                // makeFileObject = CndFileUtils.toFileObject(CndPathUtilitities.toAbsolutePath(projectFolder.getAbsolutePath(), makefilePath));
                makefilePath = ProjectSupport.toProperPath(projectFolder.getPath(), CndPathUtilitities.naturalizeSlashes(makefilePath), pathMode);
                makefilePath = CndPathUtilitities.normalizeSlashes(makefilePath);
            }
            importantItems.add(makefilePath);
        }
        if (configurePath != null && configurePath.length() > 0) {
            String normPath = RemoteFileUtil.normalizeAbsolutePath(configurePath, fileSystemExecutionEnvironment);
            configureFileObject = RemoteFileUtil.getFileObject(normPath, executionEnvironment);
            configurePath = ProjectSupport.toProperPath(projectFolder.getPath(), CndPathUtilitities.naturalizeSlashes(configurePath), pathMode);
            configurePath = CndPathUtilitities.normalizeSlashes(configurePath);
            importantItems.add(configurePath);
        }
        Iterator<String> importantItemsIterator = importantItems.iterator();
        if (!importantItemsIterator.hasNext()) {
            importantItemsIterator = null;
        }
        ProjectGenerator.ProjectParameters prjParams = new ProjectGenerator.ProjectParameters(projectName, projectFolder);
        prjParams.setMakefileName(makefileName).setConfiguration(extConf);
        prjParams.setSourceFolders(sources).setSourceFoldersFilter(sourceFoldersFilter);
        prjParams.setTestFolders(tests);
        prjParams.setImportantFiles(importantItemsIterator);
        prjParams.setFullRemote(fullRemote);
        prjParams.setHostUID(hostUID);

        makeProject = ProjectGenerator.createProject(prjParams);
        FileObject dir = CndFileUtils.toFileObject(projectFolder);
        importResult.put(Step.Project, State.Successful);
        switchModel(false);
        resultSet.add(dir);
        OpenProjects.getDefault().addPropertyChangeListener(this);
        return resultSet;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS)) {
            if (evt.getNewValue() instanceof Project[]) {
                Project[] projects = (Project[])evt.getNewValue();
                if (projects.length == 0) {
                    return;
                }
                OpenProjects.getDefault().removePropertyChangeListener(this);
                //if (setAsMain) {
                //    OpenProjects.getDefault().setMainProject(makeProject);
                //}
                RP.post(new Runnable() {

                    @Override
                    public void run() {
                        doWork();
                    }
                });
            }
        }
    }

    boolean isProjectOpened() {
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            if (p == makeProject) {
                return true;
            }
        }
        return false;
    }

    private void doWork() {
        try {
            //OpenProjects.getDefault().open(new Project[]{makeProject}, false);
            //if (setAsMain) {
            //    OpenProjects.getDefault().setMainProject(makeProject);
            //}
            if (makeProject instanceof Runnable) {
                ((Runnable)makeProject).run();
            }
            ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
            pdp.getConfigurationDescriptor();
            if (pdp.gotDescriptor()) {
                if (runConfigure && configurePath != null && configurePath.length() > 0 && 
                        configureFileObject != null && configureFileObject.isValid()) {
                    postConfigure();
                } else {
                    if (runMake) {
                        makeProject(true, null);
                    } else {
                        discovery(0, null);
                    }
                }
            } else {
                isFinished = true;
            }
        } catch (Throwable ex) {
            isFinished = true;
            Exceptions.printStackTrace(ex);
        }
    }

//    private void parseConfigureLog(File configureLog){
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(configureLog));
//            while (true) {
//                String line;
//                line = reader.readLine();
//                if (line == null) {
//                    break;
//                }
//            }
//            reader.close();
//        } catch (FileNotFoundException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//    }
    private File createTempFile(String prefix) {
        try {
            File file = File.createTempFile(prefix, ".log"); // NOI18N
            file.deleteOnExit();
            return file;
        } catch (IOException ex) {
            return null;
        }
    }

    private void postConfigure() {
        try {
            if (!isProjectOpened()) {
                isFinished = true;
                return;
            }
            if (configureLog == null) {
                configureLog = createTempFile("configure"); // NOI18N
            }
            Writer outputListener = null;
            try {
                outputListener = new BufferedWriter(new FileWriter(configureLog));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            DataObject dObj = DataObject.find(configureFileObject);
            Node node = dObj.getNodeDelegate();
            String mime = FileUtil.getMIMEType(configureFileObject);
            // Add arguments to configure script?
            if (configureArguments != null) {
                if (MIMENames.SHELL_MIME_TYPE.equals(mime)){
                    ShellExecSupport ses = node.getCookie(ShellExecSupport.class);
                    try {
                        // Keep user arguments as is in args[0]
                        ses.setArguments(new String[]{configureArguments});
                        // duplicate configure variables in environment
                        List<String> vars = ImportUtils.parseEnvironment(configureArguments);
                        ses.setEnvironmentVariables(vars.toArray(new String[vars.size()]));
                        if (configureRunFolder != null) {
                            FileObject createdFolder = mkDir(configureFileObject.getParent(), configureRunFolder);
                            if (createdFolder != null) {
                                ses.setRunDirectory(createdFolder.getPath());
                            }
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else if (MIMENames.CMAKE_MIME_TYPE.equals(mime)){
                    CMakeExecSupport ses = node.getCookie(CMakeExecSupport.class);
                    try {
                        // extract configure variables in environment
                        List<String> vars = ImportUtils.parseEnvironment(configureArguments);
                        for (String s : ImportUtils.quoteList(vars)) {
                            int i = configureArguments.indexOf(s);
                            if (i >= 0){
                                configureArguments = configureArguments.substring(0, i) + configureArguments.substring(i + s.length());
                            }
                        }
                        ses.setArguments(new String[]{configureArguments});
                        ses.setEnvironmentVariables(vars.toArray(new String[vars.size()]));
                        if (configureRunFolder != null) {
                            FileObject createdFolder = mkDir(configureFileObject.getParent(), configureRunFolder);
                            if (createdFolder != null) {
                                ses.setRunDirectory(createdFolder.getPath());
                            }
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else if (MIMENames.QTPROJECT_MIME_TYPE.equals(mime)){
                    QMakeExecSupport ses = node.getCookie(QMakeExecSupport.class);
                    try {
                        ses.setArguments(new String[]{configureArguments});
                        if (configureRunFolder != null) {
                            FileObject createdFolder = mkDir(configureFileObject.getParent(), configureRunFolder);
                            if (createdFolder != null) {
                                ses.setRunDirectory(createdFolder.getPath());
                            }
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            // If no makefile, create empty one so it shows up in Interesting Files
            //if (!makefileFile.exists()) {
            //    makefileFile.createNewFile();
            //}
            //final File configureLog = createTempFile("configure");
            ExecutionListener listener = new ExecutionListener() {

                @Override
                public void executionStarted(int pid) {
                }

                @Override
                public void executionFinished(int rc) {
                    if (rc == 0) {
                        importResult.put(Step.Configure, State.Successful);
                    } else {
                        importResult.put(Step.Configure, State.Fail);
                    }
                    if (runMake && rc == 0) {
                        //parseConfigureLog(configureLog);
                        // when run scripts we do full "clean && build" to
                        // remove old build artifacts as well
                        makeProject(true, configureLog);
                    } else {
                        switchModel(true);
                        postModelDiscovery(true);
                    }
                }
            };
            if (TRACE) {
                logger.log(Level.INFO, "#{0} {1}", new Object[]{configureFileObject, configureArguments}); // NOI18N
            }
            if (MIMENames.SHELL_MIME_TYPE.equals(mime)){
                Future<Integer> task = ShellRunAction.performAction(node, listener, outputListener, makeProject, null);
                if (task == null) {
                    throw new Exception("Cannot execute configure script"); // NOI18N
                }
            } else if (MIMENames.CMAKE_MIME_TYPE.equals(mime)){
                Future<Integer> task = CMakeAction.performAction(node, listener, null, makeProject, null);
                if (task == null) {
                    throw new Exception("Cannot execute cmake"); // NOI18N
                }
            } else if (MIMENames.QTPROJECT_MIME_TYPE.equals(mime)){
                Future<Integer> task = QMakeAction.performAction(node, listener, null, makeProject, null);
                if (task == null) {
                    throw new Exception("Cannot execute qmake"); // NOI18N
                }
            } else {
                if (TRACE) {
                    logger.log(Level.INFO, "#Configure script does not supported"); // NOI18N
                }
                importResult.put(Step.Configure, State.Fail);
                importResult.put(Step.MakeClean, State.Skiped);
                switchModel(true);
                postModelDiscovery(true);
            }
        } catch (DataObjectNotFoundException e) {
            logger.log(Level.INFO, "Cannot configure project", e); // NOI18N
            isFinished = true;
        } catch (Throwable e) {
            logger.log(Level.INFO, "Cannot configure project", e); // NOI18N
            isFinished = true;
        }
    }

    private FileObject mkDir(FileObject parent, String relative) {
         if (relative != null) {
            try {
                relative = relative.replace('\\', '/'); // NOI18N
                for(String segment : relative.split("/")) { // NOI18N
                    if (parent == null) {
                        return null;
                    }
                    if (segment.isEmpty()) {
                        continue;
                    } else if (".".equals(segment)) { // NOI18N
                        continue;
                    } else if ("..".equals(segment)) { // NOI18N
                        parent = parent.getParent();
                    } else {
                        parent = parent.createFolder(segment);
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
             return parent;
         }
         return null;
    }

    private void downloadRemoteFile(File file){
        if (file != null && !file.exists()) {
            if (executionEnvironment.isRemote()) {
                String remoteFile = HostInfoProvider.getMapper(executionEnvironment).getRemotePath(file.getAbsolutePath());
                try {
                    if (HostInfoUtils.fileExists(executionEnvironment, remoteFile)){
                        Future<Integer> task = CommonTasksSupport.downloadFile(remoteFile, executionEnvironment, file.getAbsolutePath(), null);
                        if (TRACE) {
                            logger.log(Level.INFO, "#download file {0}", file.getAbsolutePath()); // NOI18N
                        }
                        /*int rc =*/ task.get();
                    }
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (Throwable ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    private static final String configureCteatePattern = " creating "; // NOI18N
    private void scanConfigureLog(File logFile){
        if (logFile != null && logFile.exists() && logFile.canRead()){
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(logFile));
                while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    int i = line.indexOf(configureCteatePattern);
                    if (i > 0) {
                        String f = line.substring(i+configureCteatePattern.length()).trim();
                        if (f.endsWith(".h")) { // NOI18N
                            downloadRemoteFile(CndFileUtils.createLocalFile(projectFolder, f)); // NOI18N
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void makeProject(boolean doClean, File logFile) {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        FileObject makeFileObject = null;
        if (makefilePath != null && makefilePath.length() > 0) {
            if (fullRemote) {
                CndUtils.assertAbsolutePathInConsole(makefilePath);
                makeFileObject = RemoteFileUtil.getFileObject(makefilePath, executionEnvironment);
            } else {
                makeFileObject = CndFileUtils.toFileObject(CndPathUtilitities.toAbsolutePath(projectFolder.getAbsolutePath(), makefilePath));
            }
        }
        if (!fullRemote) {
            downloadRemoteFile(FileUtil.toFile(makeFileObject)); // FileUtil.toFile SIC! - always local
        }
        scanConfigureLog(logFile);
        if (makeFileObject != null && makeFileObject.isValid()) {
            DataObject dObj;
            try {
                dObj = DataObject.find(makeFileObject);
                Node node = dObj.getNodeDelegate();
                if (doClean) {
                    postClean(node);
                } else {
                    postMake(node);
                }
            } catch (DataObjectNotFoundException ex) {
                isFinished = true;
            }
        } else {
            String path = nativeProjectPath;
            File file = new File(path + "/Makefile"); // NOI18N
            if (file.exists() && file.isFile() && file.canRead()) {
                makefilePath = file.getAbsolutePath();
            } else {
                file = new File(path + "/makefile"); // NOI18N
                if (file.exists() && file.isFile() && file.canRead()) {
                    makefilePath = file.getAbsolutePath();
                }
            }
            switchModel(true);
            postModelDiscovery(true);
        }
    }

    private void postClean(final Node node) {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        ExecutionListener listener = new ExecutionListener() {

            @Override
            public void executionStarted(int pid) {
            }

            @Override
            public void executionFinished(int rc) {
                if (rc == 0) {
                    importResult.put(Step.MakeClean, State.Successful);
                } else {
                    importResult.put(Step.MakeClean, State.Fail);
                }
                postMake(node);
            }
        };
        String arguments = ""; // NOI18N
        if (cleanCommand != null){
            arguments = getArguments(cleanCommand);
        }
        if (arguments.length()==0) {
            arguments = "clean"; // NOI18N
        }
        if (TRACE) {
            logger.log(Level.INFO, "#make {0}", arguments); // NOI18N
        }
        try {
            Future<Integer> task = MakeAction.execute(node, arguments, listener, null, makeProject, null, null);
            if (task == null) {
                logger.log(Level.INFO, "Cannot execute make clean"); // NOI18N
                isFinished = true;
            }
        } catch (Throwable ex) {
            isFinished = true;
            Exceptions.printStackTrace(ex);
        }
    }

    private void postMake(Node node) {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        if (makeLog == null) {
            makeLog = createTempFile("make"); // NOI18N
        }
        ExecutionListener listener = new ExecutionListener() {

            @Override
            public void executionStarted(int pid) {
            }

            @Override
            public void executionFinished(int rc) {
                if (rc == 0) {
                    importResult.put(Step.Make, State.Successful);
                } else {
                    importResult.put(Step.Make, State.Fail);
                }
                discovery(rc, makeLog);
            }
        };
        Writer outputListener = null;
        if (makeLog != null) {
            try {
                outputListener = new BufferedWriter(new FileWriter(makeLog));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        String arguments = ""; // NOI18N
        if (buildCommand != null){
            arguments = getArguments(buildCommand);
        }
        ExecutionSupport ses = node.getCookie(ExecutionSupport.class);
        List<String> vars = ImportUtils.parseEnvironment(configureArguments);
        if (ses != null) {
            try {
                ses.setEnvironmentVariables(vars.toArray(new String[vars.size()]));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (TRACE) {
            logger.log(Level.INFO, "#make {0}", arguments); // NOI18N
        }
        try {
            Future<Integer> task = MakeAction.execute(node, arguments, listener, outputListener, makeProject, vars, null);
            if (task == null) {
                logger.log(Level.INFO, "Cannot execute make"); // NOI18N
                isFinished = true;
            }
        } catch (Throwable ex) {
            isFinished = true;
            Exceptions.printStackTrace(ex);
        }
    }

    private String getArguments(String command){
        String arguments = _getArguments(command);
        int i = arguments.indexOf("-f "); // NOI18N
        if (i >= 0) {
            String res = arguments.substring(0, i);
            arguments = arguments.substring(i+3).trim();
            int j = arguments.indexOf(' ');
            if (j < 0) {
                return res;
            } else {
                return res+arguments.substring(j).trim();
            }
        }
        return arguments;
    }

    private String _getArguments(String command){
        if (command.startsWith("\"")) { // NOI18N
            int i = command.indexOf('"', 1); // NOI18N
            if (i > 0) {
                return command.substring(i).trim();
            }
            return ""; // NOI18N
        } else if (command.startsWith("\'")) { // NOI18N
            int i = command.indexOf('\'', 1); // NOI18N
            if (i > 0) {
                return command.substring(i).trim();
            }
            return ""; // NOI18N
        }
        int i = command.indexOf(' '); // NOI18N
        if (i > 0) {
            return command.substring(i).trim();
        }
        return ""; // NOI18N
    }

    private DiscoveryProvider getProvider(String id) {
        Lookup.Result<DiscoveryProvider> providers = Lookup.getDefault().lookup(new Lookup.Template<DiscoveryProvider>(DiscoveryProvider.class));
        for (DiscoveryProvider provider : providers.allInstances()) {
            provider.clean();
            if (id.equals(provider.getID())) {
                return provider;
            }
        }
        return null;
    }

    private void waitConfigurationDescriptor() {
        // Discovery require a fully completed project
        // Make sure that descriptor was stored and readed
        ConfigurationDescriptorProvider provider = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        provider.getConfigurationDescriptor(true);
    }

    private void discovery(int rc, File makeLog) {
        try {
            if (!isProjectOpened()) {
                isFinished = true;
                return;
            }
            waitConfigurationDescriptor();
            boolean done = false;
            if (!manualCA) {
                final DiscoveryExtensionInterface extension = (DiscoveryExtensionInterface) Lookup.getDefault().lookup(IteratorExtension.class);
                if (rc == 0) {
                    if (extension != null) {
                        final Map<String, Object> map = new HashMap<String, Object>();
                        map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, nativeProjectPath);
                        map.put(DiscoveryWizardDescriptor.CONSOLIDATION_STRATEGY, consolidationStrategy);
                        if (extension.canApply(map, makeProject)) {
                            DiscoveryProvider provider = (DiscoveryProvider) map.get(DiscoveryWizardDescriptor.PROVIDER);
                            if (provider != null && "make-log".equals(provider.getID())) { // NOI18N
                                if (TRACE) {
                                    logger.log(Level.INFO, "#start discovery by log file {0}", provider.getProperty("make-log-file").getValue()); // NOI18N
                                }
                            } else {
                                if (TRACE) {
                                    logger.log(Level.INFO, "#start discovery by object files"); // NOI18N
                                }
                            }
                            try {
                                done = true;
                                extension.apply(map, makeProject);
                                if (provider != null && "make-log".equals(provider.getID())) { // NOI18N
                                    importResult.put(Step.DiscoveryLog, State.Successful);
                                } else {
                                    importResult.put(Step.DiscoveryDwarf, State.Successful);
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            if (TRACE) {
                                logger.log(Level.INFO, "#no dwarf information found in object files"); // NOI18N
                            }
                        }
                        buildArifactWasAnalyzed = true;
                    }
                }
                if (!done && makeLog != null) {
                    if (extension != null) {
                        final Map<String, Object> map = new HashMap<String, Object>();
                        map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, nativeProjectPath);
                        map.put(DiscoveryWizardDescriptor.LOG_FILE, makeLog.getAbsolutePath());
                        map.put(DiscoveryWizardDescriptor.CONSOLIDATION_STRATEGY, consolidationStrategy);
                        if (extension.canApply(map, makeProject)) {
                            if (TRACE) {
                                logger.log(Level.INFO, "#start discovery by log file {0}", makeLog.getAbsolutePath()); // NOI18N
                            }
                            try {
                                done = true;
                                extension.apply(map, makeProject);
                                importResult.put(Step.DiscoveryLog, State.Successful);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            if (TRACE) {
                                logger.log(Level.INFO, "#discovery cannot be done by log file {0}", makeLog.getAbsolutePath()); // NOI18N
                            }
                        }
                    }
                } else if (done && makeLog != null) {
                    if (!isProjectOpened()) {
                        return;
                    }
                    if (extension != null) {
                        final Map<String, Object> map = new HashMap<String, Object>();
                        map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, nativeProjectPath);
                        map.put(DiscoveryWizardDescriptor.LOG_FILE, makeLog.getAbsolutePath());
                        map.put(DiscoveryWizardDescriptor.CONSOLIDATION_STRATEGY, consolidationStrategy);
                        if (extension.canApply(map, makeProject)) {
                            if (TRACE) {
                                logger.log(Level.INFO, "#start fix macros by log file {0}", makeLog.getAbsolutePath()); // NOI18N
                            }
                            @SuppressWarnings("unchecked")
                            List<ProjectConfiguration> confs = (List<ProjectConfiguration>) map.get(DiscoveryWizardDescriptor.CONFIGURATIONS);
                            fixMacros(confs);
                            importResult.put(Step.FixMacros, State.Successful);
                        } else {
                            if (TRACE) {
                                logger.log(Level.INFO, "#fix macros cannot be done by log file {0}", makeLog.getAbsolutePath()); // NOI18N
                            }
                        }
                    }
                }
            }
            switchModel(true);
            if (!done) {
                postModelDiscovery(true);
            } else {
                postModelDiscovery(false);
            }
        } catch (Throwable ex) {
            isFinished = true;
            Exceptions.printStackTrace(ex);
        }
    }

    private void fixMacros(List<ProjectConfiguration> confs) {
        for (ProjectConfiguration conf : confs) {
            List<FileConfiguration> files = conf.getFiles();
            for (FileConfiguration fileConf : files) {
                if (fileConf.getUserMacros().size() > 0) {
                    Item item = findByNormalizedName(new File(fileConf.getFilePath()));
                    if (item != null) {
                        if (TRACE) {
                            logger.log(Level.FINE, "#fix macros for file {0}", fileConf.getFilePath()); // NOI18N
                        }
                        ProjectBridge.fixFileMacros(fileConf.getUserMacros(), item);
                    }
                }
            }
        }
        saveMakeConfigurationDescriptor(null);
    }

    private void saveMakeConfigurationDescriptor(final ProjectBase p) {
        if (p != null) {
            p.enableProjectListeners(false);
        }
        try {
            ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
            final MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
            makeConfigurationDescriptor.setModified();
            makeConfigurationDescriptor.save();
            makeConfigurationDescriptor.checkForChangedItems(makeProject, null, null);
        } finally {
            if (p != null) {
                p.enableProjectListeners(true);
            }
        }
        if (TRACE) {
            logger.log(Level.INFO, "#save configuration descriptor"); // NOI18N
        }
    }

    private void postModelDiscovery(final boolean isFull) {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        CsmModel model = CsmModelAccessor.getModel();
        if (model instanceof ModelImpl && makeProject != null) {
            final NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            final CsmProject p = model.getProject(np);
            if (p == null) {
                if (TRACE) {
                    logger.log(Level.INFO, "#discovery cannot be done by model"); // NOI18N
                }
                isFinished = true;
                return;
            }
            CsmProgressListener listener = new CsmProgressAdapter() {

                @Override
                public void projectParsingFinished(CsmProject project) {
                    if (project.equals(p)) {
                        try {
                            ImportProject.listeners.remove(p);
                            CsmListeners.getDefault().removeProgressListener(this); // ignore java warning "usage of this in anonymous class"
                            if (TRACE) {
                                logger.log(Level.INFO, "#model ready, explore model"); // NOI18N
                            }
                            if (isFull) {
                                modelDiscovery();
                            } else {
                                fixExcludedHeaderFiles();
                            }
                            showFollwUp(np);
                        } catch (Throwable ex) {
                            isFinished = true;
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            };
            CsmListeners.getDefault().addProgressListener(listener);
            ImportProject.listeners.put(p, listener);
        } else {
            isFinished = true;
        }
    }

    private boolean isFinished = false;
    public boolean isFinished(){
        return isFinished;
    }

    public Map<Step, State> getState(){
        return new EnumMap<Step, State>(importResult);
    }

    public Project getProject(){
        return makeProject;
    }

    private boolean isUILessMode = false;
    public void setUILessMode(){
        isUILessMode = true;
    }

    private File configureLog = null;
    public void setConfigureLog(File configureLog) {
        this.configureLog = configureLog;
    }

    private File makeLog = null;
    public void setMakeLog(File makeLog) {
        this.makeLog = makeLog;
    }

    private void showFollwUp(final NativeProject project) {
        isFinished = true;
        if (isUILessMode) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                FollowUp.showFollowUp(ImportProject.this, project);
            }
        });
    }

    Project getMakeProject() {
        return makeProject;
    }

    Map<Step, State> getImportResult() {
        return importResult;
    }

    // remove wrong "exclude from project" flags
    private void fixExcludedHeaderFiles() {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        if (TRACE) {
            logger.log(Level.INFO, "#start fixing excluded header files by model"); // NOI18N
        }
        ImportExecutable.fixExcludedHeaderFiles(makeProject, TRACE ? logger : null);
        importResult.put(Step.FixExcluded, State.Successful);
    }

    private Map<String,Item> normalizedItems;
    private Item findByNormalizedName(File file){
        if (normalizedItems == null) {
            normalizedItems = initNormalizedNames(makeProject);
        }
        String path = CndFileUtils.normalizeFile(file).getAbsolutePath();
        return normalizedItems.get(path);
    }

    static HashMap<String,Item> initNormalizedNames(Project makeProject) {
        HashMap<String,Item> normalizedItems = new HashMap<String,Item>();
        ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp != null) {
            MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
            if (makeConfigurationDescriptor != null) {
                for(Item item : makeConfigurationDescriptor.getProjectItems()){
                    normalizedItems.put(item.getNormalizedPath(),item);
                }
            }
        }
        return normalizedItems;
    }


    private void modelDiscovery() {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, nativeProjectPath);
        map.put(DiscoveryWizardDescriptor.INVOKE_PROVIDER, Boolean.TRUE);
        map.put(DiscoveryWizardDescriptor.CONSOLIDATION_STRATEGY, consolidationStrategy);
        boolean does = false;
        if (!manualCA && !buildArifactWasAnalyzed) {
            DiscoveryExtensionInterface extension = (DiscoveryExtensionInterface) Lookup.getDefault().lookup(IteratorExtension.class);
            if (extension != null) {
                if (extension.canApply(map, makeProject)) {
                    if (TRACE) {
                        logger.log(Level.INFO, "#start discovery by object files"); // NOI18N
                    }
                    try {
                        extension.apply(map, makeProject);
                        importResult.put(Step.DiscoveryDwarf, State.Successful);
                        does = true;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    if (TRACE) {
                        logger.log(Level.INFO, "#no dwarf information found in object files"); // NOI18N
                    }
                }
            }
        }
        if (!does) {
            if (!isProjectOpened() || !isModelAvaliable()) {
                isFinished = true;
                return;
            }
            if (TRACE) {
                logger.log(Level.INFO, "#start discovery by model"); // NOI18N
            }
            map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, nativeProjectPath);
            DiscoveryProvider provider = getProvider("model-folder"); // NOI18N
            provider.getProperty("folder").setValue(nativeProjectPath); // NOI18N
            if (manualCA) {
                provider.getProperty("prefer-local").setValue(Boolean.TRUE); // NOI18N
            }
            map.put(DiscoveryWizardDescriptor.PROVIDER, provider);
            map.put(DiscoveryWizardDescriptor.INVOKE_PROVIDER, Boolean.TRUE);
            DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(map);
            descriptor.setProject(makeProject);
            SelectConfigurationPanel.buildModel(descriptor);
            try {
                DiscoveryProjectGenerator generator = new DiscoveryProjectGenerator(descriptor);
                generator.makeProject();
                importResult.put(Step.DiscoveryModel, State.Successful);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void switchModel(boolean state) {
        CsmModel model = CsmModelAccessor.getModel();
        if (model instanceof ModelImpl && makeProject != null) {
            NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            if (state) {
                if (TRACE) {
                    logger.log(Level.INFO, "#enable model for {0}", np.getProjectDisplayName()); // NOI18N
                }
                ((ModelImpl) model).enableProject(np);
            } else {
                if (TRACE) {
                    logger.log(Level.INFO, "#disable model for {0}", np.getProjectDisplayName()); // NOI18N
                }
                ((ModelImpl) model).disableProject(np);
            }
        }
    }

    private boolean isModelAvaliable(){
        CsmModel model = CsmModelAccessor.getModel();
        if (model != null && makeProject != null) {
            return CsmModelAccessor.getModel().getProject(makeProject) != null;
        }
        return false;
    }

    private static final Map<CsmProject, CsmProgressListener> listeners = new WeakHashMap<CsmProject, CsmProgressListener>();

    public static enum State {

        Successful, Fail, Skiped
    }

    public static enum Step {

        Project, Configure, MakeClean, Make, DiscoveryDwarf, DiscoveryLog, FixMacros, DiscoveryModel, FixExcluded
    }
}
