/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.profiler.nbimpl.actions;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.common.SessionSettings;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.utils.MiscUtils;
import org.netbeans.modules.profiler.NetBeansProfiler;
import org.netbeans.modules.profiler.api.ProfilerIDESettings;
import org.netbeans.modules.profiler.utils.IDEUtils;
import org.netbeans.spi.project.ui.support.MainProjectSensitiveActions;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;
import javax.swing.Action;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.lib.profiler.common.CommonUtils;
import org.netbeans.lib.profiler.global.Platform;
import org.netbeans.modules.profiler.HeapDumpWatch;
import org.netbeans.modules.profiler.actions.JavaPlatformSelector;
import org.netbeans.modules.profiler.actions.ProfilingSupport;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.JavaPlatform;
import org.netbeans.modules.profiler.api.java.JavaProfilerSource;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;
import org.netbeans.modules.profiler.api.project.ProjectStorage;
import org.netbeans.modules.profiler.api.project.AntProjectSupport;
import org.netbeans.modules.profiler.api.project.ProjectProfilingSupport;
import org.netbeans.modules.profiler.projectsupport.utilities.ProjectUtilities;


/**
 * 4.0 Ant-style actions
 *
 * @author Tomas Hurka
 * @author Ian Formanek
 */
public final class AntActions {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String FILE_TEST_NOT_FOUND_MSG = NbBundle.getMessage(AntActions.class, "AntActions_FileTestNotFoundMsg"); // NOI18N
    private static final String FAILED_DETERMINE_JAVA_PLATFORM_MSG = NbBundle.getMessage(AntActions.class,
                                                                                         "AntActions_FailedDetermineJavaPlatformMsg"); // NOI18N
    private static final String FAILED_DETERMINE_PROJECT_BUILDSCRIPT_MSG = NbBundle.getMessage(AntActions.class,
                                                                                               "AntActions_FailedDetermineProjectBuildScriptMsg"); // NOI18N
    private static final String INCORRECT_JAVA_SPECVERSION_DIALOG_CAPTION = NbBundle.getMessage(AntActions.class,
                                                                                                "AntActions_IncorrectJavaSpecVersionDialogCaption"); // NOI18N
    private static final String INCORRECT_JAVA_SPECVERSION_DIALOG_MSG = NbBundle.getMessage(AntActions.class,
                                                                                            "AntActions_IncorrectJavaSpecVersionDialogMsg"); // NOI18N
    private static final String LINUX_THREAD_TIMER_KEY = "-XX:+UseLinuxPosixThreadCPUClocks"; // NOI18N
    private static final String UNSUPPORTED_PROJECT_TYPE_MSG = NbBundle.getMessage(AntActions.class,
                                                                                   "AntActions_UnsupportedProjectTypeMsg"); // NOI18N                                                                                                                            
    private static final String INVALID_JAVAPLATFORM_MSG = NbBundle.getMessage(AntActions.class,
                                                                                   "AntActions_InvalidJavaplatformMsg"); // NOI18N
    private static final String INVALID_PLATFORM_PROJECT_MSG = NbBundle.getMessage(AntActions.class,
                                                                                   "AntActions_InvalidPlatformProjectMsg"); // NOI18N
    private static final String INVALID_PLATFORM_PROFILER_MSG = NbBundle.getMessage(AntActions.class,
                                                                                    "AntActions_InvalidPlatformProfilerMsg"); // NOI18N

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Default constructor to avoid creating instances */
    private AntActions() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * @return An Action to invoke profiling of the main project in the IDE
     */
    public static Action profileMainProject() {
        final Action a = MainProjectSensitiveActions.mainProjectSensitiveAction(new ProjectActionPerformer() {
                public boolean enable(final Project project) {
                    // Check if the Profiler is initialized correctly
                    if (!NetBeansProfiler.isInitialized()) {
                        return false;
                    }

                    // No projects opened => disable action
                    if (OpenProjects.getDefault().getOpenProjects().length == 0) {
                        return false;
                    }

                    // No main project set => enable action (see Issue 116619)
                    if (project == null) {
                        return true;
                    }

                    // Check if project type is supported, eventually return null
                    return org.netbeans.modules.profiler.nbimpl.project.ProjectUtilities.isProjectTypeSupported(project);
                }

                public void perform(final Project project) {
                    if (org.netbeans.modules.profiler.nbimpl.project.ProjectUtilities.isProjectTypeSupported(project)) {
                        doProfileProject(project, null, NbBundle.getMessage(AntActions.class, "LBL_ProfileProject"));
                    } else {
                        ProfilerDialogs.displayError(UNSUPPORTED_PROJECT_TYPE_MSG);
                    }
                }
            }, NbBundle.getMessage(AntActions.class, "LBL_ProfileMainProjectAction"), // NOI18N
                                                                                null);
        a.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(AntActions.class, "HINT_ProfileMainProjectAction" // NOI18N
        ));
        a.putValue("iconBase", Icons.getResource(ProfilerIcons.PROFILE));
        a.putValue(Action.SMALL_ICON, Icons.getIcon(ProfilerIcons.PROFILE));

        return a;
    }

    /**
     * @return An Action to invoke profiling of a selected project in the IDE
     */
    public static Action profileProject() {
        final Action a = ProjectSensitiveAction.projectSensitiveAction(new ProjectSensitiveAction.ProfilerProjectActionPerformer() {
                public boolean enable(final Project project, final Lookup context, final boolean lightweightOnly) {
                    if (!NetBeansProfiler.isInitialized()) {
                        return false;
                    }

                    if (project == null) {
                        return false;
                    }

                    return org.netbeans.modules.profiler.nbimpl.project.ProjectUtilities.isProjectTypeSupported(project);
                }

                public void perform(final Project project, final Lookup context) {
                    doProfileProject(project, null, NbBundle.getMessage(AntActions.class, "LBL_ProfileProject"));
                }
            }, NbBundle.getMessage(AntActions.class, "LBL_ProfileProjectAction40_General"), // NOI18N
                                                                       NbBundle.getMessage(AntActions.class,
                                                                                           "LBL_ProfileProjectAction40"), // NOI18N
                                                                       null);
        a.putValue("noIconInMenu", Boolean.TRUE); //NOI18N

        return a;
    }

    /**
     * @return An Action to invoke profiling of a selected project in the IDE, does not have project name in display name
     */
    public static Action profileProjectPopup() {
        final Action a = ProjectSensitiveAction.projectSensitiveAction(new ProjectSensitiveAction.ProfilerProjectActionPerformer() {
                public boolean enable(final Project project, final Lookup context, final boolean lightweightOnly) {
                    if (!NetBeansProfiler.isInitialized()) {
                        return false;
                    }

                    if (project == null) {
                        return false;
                    }

                    return org.netbeans.modules.profiler.nbimpl.project.ProjectUtilities.isProjectTypeSupported(project);
                }

                public void perform(final Project project, final Lookup context) {
                    doProfileProject(project, null, NbBundle.getMessage(AntActions.class, "LBL_ProfileProject"));
                }
            }, NbBundle.getMessage(AntActions.class, "LBL_ProfileProjectActionPopup"), // NOI18N
                                                                       NbBundle.getMessage(AntActions.class,
                                                                                           "LBL_ProfileProjectActionPopup"), // NOI18N
                                                                       null);
        a.putValue("noIconInMenu", Boolean.TRUE); //NOI18N

        return a;
    }

    /**
     * @return An Action to invoke profiling of a single class in the IDE
     */
    public static Action profileSingle() {
        final Action a = FileSensitiveAction.fileSensitiveAction(new ProjectSensitiveAction.ProfilerProjectActionPerformer() {
                public boolean enable(final Project project, final Lookup context, final boolean lightweightOnly) {
                    if (!NetBeansProfiler.isInitialized()) {
                        return false;
                    }

                    if (project == null) {
                        return false;
                    }

                    final FileObject[] fos = ProjectSensitiveAction.ActionsUtil.getFilesFromLookup(context, project);

                    if (fos.length != 1) {
                        return false;
                    }

//                    final ProjectTypeProfiler ptp = org.netbeans.modules.profiler.utils.ProjectUtilities.getProjectTypeProfiler(project);
//                    if (!lightweightOnly) {
//                        if (!ptp.isFileObjectSupported(project, fos[0])) {
//                            return ProjectUtilities.hasAction(project, "profile-single"); //NOI18N
//                        }
//                    } else {
//                        return ptp.isProfilingSupported(project) || ProjectUtilities.hasAction(project, "profile-single"); //NOI18N
//                    }
                    ProjectProfilingSupport support = ProjectProfilingSupport.get(project);
                    if (!lightweightOnly) {
                        if (!support.isFileObjectSupported(fos[0])) {
                            return ProjectUtilities.hasAction(project, "profile-single"); //NOI18N
                        }
                    } else {
                        return support.isProfilingSupported() || ProjectUtilities.hasAction(project, "profile-single"); //NOI18N
                    }
                    
                    return true;
                }

                public void perform(final Project project, final Lookup context) {
                    final FileObject[] fos = ProjectSensitiveAction.ActionsUtil.getFilesFromLookup(context, project);

                    if (fos.length != 1) {
                        throw new IllegalStateException();
                    }

                    doProfileProject(project, JavaProfilerSource.createFrom(fos[0]), NbBundle.getMessage(AntActions.class, "LBL_ProfileFile"));
                }
            }, NbBundle.getMessage(AntActions.class, "LBL_ProfileSingleAction40_General"), // NOI18N
                                                                 NbBundle.getMessage(AntActions.class, "LBL_ProfileSingleAction40"), // NOI18N
                                                                 null);
        a.putValue("noIconInMenu", Boolean.TRUE); //NOI18N

        return a;
    }

    /**
     * @return An Action to invoke profiling of a single class in the IDE. Does not have file name in its display name
     */
    public static Action profileSinglePopup() {
        final Action a = FileSensitiveAction.fileSensitiveAction(new ProjectSensitiveAction.ProfilerProjectActionPerformer() {
                public boolean enable(final Project project, final Lookup context, final boolean lightweightOnly) {
                    if (!NetBeansProfiler.isInitialized()) {
                        return false;
                    }

                    if (project == null) {
                        return false;
                    }

                    final FileObject[] fos = ProjectSensitiveAction.ActionsUtil.getFilesFromLookup(context, project);

                    if (fos.length != 1) {
                        return false;
                    }

//                    final ProjectTypeProfiler ptp = org.netbeans.modules.profiler.utils.ProjectUtilities.getProjectTypeProfiler(project);
//                    if (!lightweightOnly) {
//                        if (!ptp.isFileObjectSupported(project, fos[0])) {
//                            return ProjectUtilities.hasAction(project, "profile-single"); //NOI18N
//                        }
//                    } else {
//                        return ptp.isProfilingSupported(project) || ProjectUtilities.hasAction(project, "profile-single"); //NOI18N
//                    }
                    ProjectProfilingSupport support = ProjectProfilingSupport.get(project);
                    if (!lightweightOnly) {
                        if (!support.isFileObjectSupported( fos[0])) {
                            return ProjectUtilities.hasAction(project, "profile-single"); //NOI18N
                        }
                    } else {
                        return support.isProfilingSupported() || ProjectUtilities.hasAction(project, "profile-single"); //NOI18N
                    }

                    return true;
                }

                public void perform(final Project project, final Lookup context) {
                    final FileObject[] fos = ProjectSensitiveAction.ActionsUtil.getFilesFromLookup(context, project);

                    if (fos.length != 1) {
                        throw new IllegalStateException();
                    }

//                    final ProjectTypeProfiler ptp = org.netbeans.modules.profiler.utils.ProjectUtilities.getProjectTypeProfiler(project);
//                    if (!ptp.isFileObjectSupported(project, fos[0]) && !ProjectUtilities.hasAction(project, "profile-single")) { //NOI18N
//                        throw new IllegalStateException();
//                    }
                    ProjectProfilingSupport support = ProjectProfilingSupport.get(project);
                    if (!support.isFileObjectSupported( fos[0]) && !ProjectUtilities.hasAction(project, "profile-single")) { //NOI18N
                        throw new IllegalStateException();
                    }

                    doProfileProject(project, JavaProfilerSource.createFrom(fos[0]) ,NbBundle.getMessage(AntActions.class, "LBL_ProfileFile"));
                }
            }, NbBundle.getMessage(AntActions.class, "LBL_ProfileSingleActionPopup"), // NOI18N
                                                                 NbBundle.getMessage(AntActions.class,
                                                                                     "LBL_ProfileSingleActionPopup"), // NOI18N
                                                                 null);
        a.putValue("noIconInMenu", Boolean.TRUE); //NOI18N

        return a;
    }

    /**
     * @return An Action to invoke profiling of a single class in the IDE
     */
    public static Action profileTest() {
        final Action a = FileSensitiveAction.fileSensitiveAction(new ProjectSensitiveAction.ProfilerProjectActionPerformer() {
                public boolean enable(final Project project, final Lookup context, final boolean lightweightOnly) {
                    if (!NetBeansProfiler.isInitialized()) {
                        return false;
                    }

                    if (project == null) {
                        return false;
                    }

                    final FileObject[] fos = ProjectSensitiveAction.ActionsUtil.getFilesFromLookup(context, project);

                    if (fos.length != 1) {
                        return false;
                    }

                    final FileObject fo = ProjectUtilities.findTestForFile(fos[0]);

                    if (fo == null) {
                        return false; // not a test and test for it does not exist
                    }

//                    final ProjectTypeProfiler ptp = org.netbeans.modules.profiler.utils.ProjectUtilities.getProjectTypeProfiler(project);
//                    if (!lightweightOnly) {
//                        return (ptp.isFileObjectSupported(project, fo));
//                    } else {
//                        return ptp.isProfilingSupported(project);
//                    }
                    ProjectProfilingSupport support = ProjectProfilingSupport.get(project);
                    if (!lightweightOnly) {
                        return (support.isFileObjectSupported(fo));
                    } else {
                        return support.isProfilingSupported();
                    }
                }

                public void perform(final Project project, final Lookup context) {
                    final FileObject[] fos = ProjectSensitiveAction.ActionsUtil.getFilesFromLookup(context, project);

                    if (fos.length != 1) {
                        throw new IllegalStateException();
                    }

                    final FileObject fo = ProjectUtilities.findTestForFile(fos[0]);

                    if (fo == null) {
                        throw new IllegalStateException(FILE_TEST_NOT_FOUND_MSG);
                    }

                    if (!ProjectProfilingSupport.get(project).isFileObjectSupported(fo)) {
                        throw new IllegalStateException();
                    }

                    doProfileProject(project, JavaProfilerSource.createFrom(fo), NbBundle.getMessage(AntActions.class, "LBL_ProfileTest"));
                }
            }, NbBundle.getMessage(AntActions.class, "LBL_ProfileTestAction_General"), // NI18N
                                                                 NbBundle.getMessage(AntActions.class, "LBL_ProfileTestAction"), // NOI18N
                                                                 null);
        a.putValue("noIconInMenu", Boolean.TRUE); //NOI18N

        return a;
    }

    public static Action unintegrateProfiler() {
        final Action a = ProjectSensitiveAction.projectSensitiveAction(new ProjectSensitiveAction.ProfilerProjectActionPerformer() {
                public boolean enable(final Project project, final Lookup context, final boolean lightweightOnly) {
                    if (!NetBeansProfiler.isInitialized()) {
                        return false;
                    }

                    if (project == null) {
                        return false;
                    }

//                    final ProjectTypeProfiler ptp = org.netbeans.modules.profiler.utils.ProjectUtilities.getProjectTypeProfiler(project);
//
//                    return ptp.supportsUnintegrate(project);
                    ProjectProfilingSupport support = ProjectProfilingSupport.get(project);
                    return support.supportsUnintegrate();
                }

                public void perform(final Project project, final Lookup context) {
//                    final ProjectTypeProfiler ptp = org.netbeans.modules.profiler.utils.ProjectUtilities.getProjectTypeProfiler(project);
                    ProjectProfilingSupport support = ProjectProfilingSupport.get(project);
                    try {
                        support.unintegrateProfiler();
                    } catch (Exception e) {
                        ProfilerLogger.log(e);
                    }
                }
            }, NbBundle.getMessage(AntActions.class, "LBL_UnintegrateProfilerAction"), // NOI18N
                                                                       NbBundle.getMessage(AntActions.class,
                                                                                           "LBL_UnintegrateProfilerAction"), // NOI18N
                                                                       null);
        a.putValue("noIconInMenu", Boolean.TRUE); //NOI18N

        return a;
    }

    private static String getHeapDumpPath(ProfilerIDESettings gps, Project project) {
        int oomeDetectionMode = gps.getOOMDetectionMode();

        switch (oomeDetectionMode) {
            case ProfilerIDESettings.OOME_DETECTION_TEMPDIR:
                return System.getProperty("java.io.tmpdir"); // NOI18N
            case ProfilerIDESettings.OOME_DETECTION_PROJECTDIR:

                try {
                    return FileUtil.toFile(ProjectStorage.getSettingsFolder(project, true)).getAbsolutePath();
                } catch (IOException e) {
                    ErrorManager.getDefault().annotate(e, "Cannot resolve project settings directory:\n" + e.getMessage());
                    ErrorManager.getDefault().notify(ErrorManager.ERROR, e);

                    return null;
                }
            case ProfilerIDESettings.OOME_DETECTION_CUSTOMDIR:
                return gps.getCustomHeapdumpPath();
        }

        return null;
    }

    private static void activateLinuxPosixThreadTime(ProfilingSettings ps, Properties props, Project project) {
        if (ps.getThreadCPUTimerOn()) {
            props.setProperty("profiler.info.jvmargs", LINUX_THREAD_TIMER_KEY + " " + props.getProperty("profiler.info.jvmargs")); // NOI18N
            ProfilerLogger.log("Profiler.UseLinuxPosixThreadCPUClocks: Enabled"); // NOI18N
        }
    }

    private static void activateOOMProtection(ProfilerIDESettings gps, Properties props, Project project) {
        if (gps.isOOMDetectionEnabled()) {
            String oldArgs = props.getProperty("profiler.info.jvmargs");
            oldArgs = (oldArgs != null) ? oldArgs : "";

            StringBuilder oomArgsBuffer = new StringBuilder(oldArgs);
            String heapDumpPath = getHeapDumpPath(gps, project);

            if ((heapDumpPath != null) && (heapDumpPath.length() > 0)) {
                // used for filesystem listener
                props.setProperty(HeapDumpWatch.OOME_PROTECTION_ENABLED_KEY, "yes");
                props.setProperty(HeapDumpWatch.OOME_PROTECTION_DUMPPATH_KEY, heapDumpPath);

                // used as an argument for starting java process
                if (heapDumpPath.contains(" ")) {
                    heapDumpPath = "\"" + heapDumpPath + "\"";
                }

                oomArgsBuffer.append(" -XX:+HeapDumpOnOutOfMemoryError"); // NOI18N
                oomArgsBuffer.append(" -XX:HeapDumpPath=").append(heapDumpPath).append(" "); // NOI18N

                ProfilerLogger.log("Profiler.OutOfMemoryDetection: Enabled"); // NOI18N
            }

            props.setProperty("profiler.info.jvmargs", oomArgsBuffer.toString()); // NOI18N
        }
    }

    // -- Private implementation -----------------------------------------------------------------------------------------

    /**
     * Performs profiling of the selected project using either the supplied class (in case of profile single) or the
     * project's main class.
     *
     * @param project           The project to profile
     * @param profiledClassFile In case profiledClass is not null, this is the FileObject representing the specified class
     */
    private static void doProfileProject(final Project project, final JavaProfilerSource profiledClassFile, final String actionName) {
        final boolean isTest = profiledClassFile != null ? profiledClassFile.isTest() : false;
        final FileObject fo = profiledClassFile != null ? profiledClassFile.getFile() : null;
        
        if (ProfilingSupport.getDefault().isProfilingActionInvoked()) {
            return;
        }

        ProfilingSupport.getDefault().setProfilingActionInvoked(true);

        final Runnable actionPerform = new Runnable() {
            public void run() {
                try {
                    // 1. if there is profiling in progress, ask the user and possibly cancel
                    if (ProfilingSupport.checkProfilingInProgress()) {
                        return;
                    }

//                        final ProjectTypeProfiler ptp = org.netbeans.modules.profiler.utils.ProjectUtilities.getProjectTypeProfiler(project);
                    ProjectProfilingSupport support = ProjectProfilingSupport.get(project);
                    if (!support.isProfilingSupported()) {
                        // Branch A: not supported project with profile action in the action provider

                        // as of now, the profile-tests will neve be used
                        ProjectUtilities.invokeAction(project, isTest ? "profile-tests" : "profile"); //NOI18N
                    } else {
                        // Branch B: project profiling directly supported via ProjectTypeProfiler
                        // 2. check if the project has been modified for profiling
                        if (!support.checkProjectIsModifiedForProfiler()) {
                            return; // something failed - has already been reported to the user
                        }

                        // 3. determine Java platform to use
                        final JavaPlatform platform = initPlatform(project, support);

                        if (platform == null) {
                            return; // user already notified
                        }

                        // 3. check if the project is properly setup to be profiled (e.g. main class has a main method)
                        // FIXME - probably it would be better to pass around JavaProfilerSource instead of FileObject
                        if (!support.checkProjectCanBeProfiled(fo)) {
                            return;
                        }

                        // 5. get session settings from the project context
                        final ProfilerIDESettings gps = ProfilerIDESettings.getInstance();

                        final String javaFile = platform.getPlatformJavaFile();

                        if (javaFile == null) {
                            if (ProfilerIDESettings.getInstance().getJavaPlatformForProfiling() == null) {
                                // used platform defined for project
                                ProfilerDialogs.displayError(MessageFormat.format(INVALID_PLATFORM_PROJECT_MSG,
                                                                           new Object[] { platform.getDisplayName() }));
                            } else {
                                // used platform defined in Options / Profiler
                                ProfilerDialogs.displayError(MessageFormat.format(INVALID_PLATFORM_PROFILER_MSG,
                                                                           new Object[] { platform.getDisplayName() }));
                            }
                            return;
                        }

                        final String javaVersion = platform.getPlatformJDKVersion();

                        if (javaVersion == null) {
                            ProfilerDialogs.displayError(MessageFormat.format(FAILED_DETERMINE_JAVA_PLATFORM_MSG,
                                                                       new Object[] { platform.getDisplayName() }));

                            return;
                        }

                        final SessionSettings ss = new SessionSettings();
                        ss.setJavaExecutable(javaFile);
                        ss.setJavaVersionString(javaVersion);
                        ss.setSystemArchitecture(platform.getPlatformArchitecture());
                        ss.setPortNo(gps.getPortNo());
                        support.setupProjectSessionSettings(ss);

                        boolean settingsAccepted = false;
                        ProfilingSettings pSettings = null;

                        while (!settingsAccepted) {
                            // 6. show SelectTaskPanel and let the user choose the profiling type
                            pSettings = ProfilingSupport.getDefault()
                                                        .selectTaskForProfiling(project, ss, fo,
                                                                                support.supportsSettingsOverride());

                            if (pSettings == null) {
                                return; // cancelled
                            }

                            // Here was a check for enormous profiling overhead when profiling Web Projects.
                            // Generally, this is the right place to give ProjectTypeProfiler a chance to
                            // accept/reject current profiling settings before starting new profiling session.
                            settingsAccepted = true;
                        }

                        final ProfilingSettings profilingSettings = pSettings;
                        final Properties props = new Properties();

                        // 7. store things into properties to be passed to Ant
                        profilingSettings.store(props); // Profiling settings
                        ss.store(props); // Session settings

                        // Auxiliary internal profiler information:
                        String projectDir = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
                        props.setProperty("profiler.info.project.dir", projectDir); // NOI18N // TODO: create constant

                        if (fo != null) {
                            String singleFile = FileUtil.toFile(fo).getAbsolutePath();
                            props.setProperty("profiler.info.single.file", singleFile); // NOI18N // TODO: create constant
                        }

                        String usedJavaExecutable = ss.getJavaExecutable();
                        String usedJvmArgs = ss.getJVMArgs();

                        if (profilingSettings.getOverrideGlobalSettings()) {
                            String javaPlatformName = profilingSettings.getJavaPlatformName();
                            JavaPlatform jp;

                            if (javaPlatformName != null) {
                                usedJavaExecutable = Profiler.getDefault().getPlatformJavaFile(javaPlatformName);
                                jp = JavaPlatform.getJavaPlatformById(javaPlatformName);

                                if (jp == null) {
                                    // selected platform does not exist, use 
                                    String text = MessageFormat.format(INVALID_JAVAPLATFORM_MSG,new Object[] {javaPlatformName});
                                    ProfilerDialogs.displayWarning(text);
                                    jp = platform;
                                }
                            } else { 
                                // javaPlatformName == null -> do not override java platform, use platform from global settings
                                jp = platform;
                            }
                            // added to support nbstartprofiledserver
                            props.setProperty("profiler.info.javaPlatform",
                                              jp.getPlatformId());
                            usedJvmArgs = profilingSettings.getJVMArgs();
                        } else {
                            // added to support nbstartprofiledserver
                            props.setProperty("profiler.info.javaPlatform",
                                              platform.getPlatformId());
                        }

                        props.setProperty("profiler.info.jvm", usedJavaExecutable); // NOI18N
                        props.setProperty("profiler.info.jvmargs", usedJvmArgs); // NOI18N

                        if (javaVersion.equals(CommonConstants.JDK_15_STRING)) {
                            // JDK 1.5 used
                            props.setProperty("profiler.info.jvmargs.agent", // NOI18N
                                              IDEUtils.getAntProfilerStartArgument15(ss.getPortNo(),
                                                                                     ss.getSystemArchitecture())
                            );

                            if (platform.getPlatformJDKMinor() >= 7) {
                                activateOOMProtection(gps, props, project);
                            } else {
                                ProfilerLogger.log("Profiler.OutOfMemoryDetection: Disabled. Not supported JVM. Use at least 1.4.2_12 or 1.5.0_07"); // NOI18N
                            }
                        } else if (javaVersion.equals(CommonConstants.JDK_16_STRING)) {
                            // JDK 1.6 used
                            props.setProperty("profiler.info.jvmargs.agent", // NOI18N
                                              IDEUtils.getAntProfilerStartArgument16(ss.getPortNo(),
                                                                                     ss.getSystemArchitecture())
                            );
                            activateOOMProtection(gps, props, project);
                        } else if (javaVersion.equals(CommonConstants.JDK_17_STRING)) {
                            props.setProperty("profiler.info.jvmargs.agent", // NOI18N
                                              IDEUtils.getAntProfilerStartArgument17(ss.getPortNo(),
                                                                                     ss.getSystemArchitecture())
                            );
                            activateOOMProtection(gps, props, project);
                        } else if (javaVersion.equals(CommonConstants.JDK_18_STRING)) {
                            props.setProperty("profiler.info.jvmargs.agent", // NOI18N
                                              IDEUtils.getAntProfilerStartArgument18(ss.getPortNo(),
                                                                                     ss.getSystemArchitecture())
                            );
                            activateOOMProtection(gps, props, project);
                        } else {
                            throw new IllegalArgumentException("Unsupported JDK " + javaVersion); // NOI18N
                        }

                        if (Platform.isLinux() && javaVersion.equals(CommonConstants.JDK_16_STRING)) {
                            activateLinuxPosixThreadTime(pSettings, props, project);
                        }

                        if (!support.startProfilingSession(fo, isTest, props)) { // Used for Maven - ProjectTypeProfiler itself controls starting profiling session

                            // 8. determine the build script and target to run
                            AntProjectSupport antSupport = AntProjectSupport.get(project);
                            final FileObject buildScriptFO = antSupport.getProjectBuildScript();

                            if (buildScriptFO == null) {
                                ProfilerDialogs.displayError(MessageFormat.format(FAILED_DETERMINE_PROJECT_BUILDSCRIPT_MSG,
                                                                           new Object[] {
                                                                               ProjectUtils.getInformation(project).getName()
                                                                           }));

                                return;
                            }

                            // determine which type fo target shoudl be called, and request its name
                            int type;

                            if (isTest) {
                                type = (fo == null) ? AntProjectSupport.TARGET_PROFILE_TEST
                                                                   : AntProjectSupport.TARGET_PROFILE_TEST_SINGLE;
                            } else {
                                type = (fo == null) ? AntProjectSupport.TARGET_PROFILE
                                                                   : AntProjectSupport.TARGET_PROFILE_SINGLE;
                            }

                            final String profileTarget = antSupport.getProfilerTargetName(buildScriptFO, type, fo);

                            if (profileTarget == null) {
                                return; // already notified the user or user's choice
                            }

                            // 9. final ability of the ProjectTypeProfiler to influence the properties passed to Ant
                            support.configurePropertiesForProfiling(props, fo);

                            // 10. Run the target
                            ((org.netbeans.modules.profiler.nbimpl.NetBeansProfiler)Profiler.getDefault()).runTarget(buildScriptFO, profileTarget, props);

                        }


                    }
                } finally {
                    ProfilingSupport.getDefault().setProfilingActionInvoked(false);
                }
            }
        };
        
        CommonUtils.runInEventDispatchThread(new Runnable() {
            @Override
            public void run() {
                if (ScanDialog.runWhenScanFinished(new Runnable() {
                    @Override
                    public void run() {
                        RequestProcessor.getDefault().post(actionPerform);
                    }
                }, actionName)) {
                    ProfilingSupport.getDefault().setProfilingActionInvoked(false);
                }
            }
        });
    }

    private static JavaPlatform initPlatform(Project project, ProjectProfilingSupport pps) {
        // 1. check if we have a Java platform to use for profiling
        final ProfilerIDESettings gps = ProfilerIDESettings.getInstance();
        JavaPlatform platform = JavaPlatform.getJavaPlatformById(gps.getJavaPlatformForProfiling());
        JavaPlatform projectPlatform = pps.getProjectJavaPlatform();

        if (platform == null) { // should use the one defined in project
            platform = projectPlatform;

            if ((platform == null) || !MiscUtils.isSupportedJVM(platform.getSystemProperties())) {
                platform = JavaPlatformSelector.getDefault().selectPlatformToUse();

                if (platform == null) {
                    return null;
                }
            }
        }

        if (projectPlatform != null) { // check that the project platform is not newer than platform to use

            while (true) {
                if (projectPlatform.getVersion().compareTo(platform.getVersion()) > 0) {
                    Boolean ret = ProfilerDialogs.displayCancellableConfirmation(
                            INCORRECT_JAVA_SPECVERSION_DIALOG_MSG,
                            INCORRECT_JAVA_SPECVERSION_DIALOG_CAPTION);

                    if (Boolean.TRUE.equals(ret)) {
                        break;
                    } else if (Boolean.FALSE.equals(ret)) {
                        platform = JavaPlatformSelector.getDefault().selectPlatformToUse();

                        if (platform == null) {
                            return null; // cancelled by the user
                        }
                    } else { // cancelled

                        return null;
                    }
                } else {
                    break; // version comparison OK.
                }
            }
        }

        return platform;
    }
    
//    private static boolean isProjectTypeSupported(final Project project) {
//        ProjectTypeProfiler ptp = org.netbeans.modules.profiler.utils.ProjectUtilities.getProjectTypeProfiler(project);
//
//        if (ptp.isProfilingSupported(project)) {
//            return true;
//        }
//
//        return ProjectUtilities.hasAction(project, "profile"); //NOI18N
//    }
}
