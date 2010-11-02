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

package org.netbeans.modules.profiler.j2ee;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.client.ClientUtils.SourceCodeSelection;
import org.netbeans.lib.profiler.common.SessionSettings;
import org.netbeans.lib.profiler.common.integration.IntegrationUtils;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.marker.MethodMarker;
import org.netbeans.lib.profiler.marker.Mark;
import org.netbeans.lib.profiler.utils.MiscUtils;
import org.netbeans.lib.profiler.utils.formatting.Formattable;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.profiler.AbstractProjectTypeProfiler;
import org.netbeans.modules.profiler.NetBeansProfiler;
import org.netbeans.modules.profiler.ProfilerIDESettings;
import org.netbeans.modules.profiler.actions.JavaPlatformSelector;
import org.netbeans.modules.profiler.ui.ProfilerDialogs;
import org.netbeans.modules.profiler.ui.stp.DefaultSettingsConfigurator;
import org.netbeans.modules.profiler.ui.stp.SelectProfilingTask;
import org.netbeans.modules.profiler.utils.IDEUtils;
import org.netbeans.spi.project.support.ant.*;
import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.w3c.dom.Document;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.profiler.projectsupport.utilities.SourceUtils;
import org.netbeans.modules.profiler.utils.ProjectUtilities;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;


/**
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 */
@ProjectServiceProvider(service=org.netbeans.modules.profiler.spi.ProjectTypeProfiler.class, 
                        projectTypes={
                            @ProjectType(id="org-netbeans-modules-j2ee-ejbjarproject"), 
                            @ProjectType(id="org-netbeans-modules-j2ee-earproject"), 
                            @ProjectType(id="org-netbeans-modules-web-project")
                        }
)
public final class J2EEProjectTypeProfiler extends AbstractProjectTypeProfiler {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class JSPNameFormatter implements org.netbeans.lib.profiler.utils.formatting.MethodNameFormatter {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public Formattable formatMethodName(final SourceCodeSelection sourceCodeSelection) {
            return new Formattable() {
                    public String toFormatted() {
                        String name = WebProjectUtils.getJSPPath(sourceCodeSelection);

                        return name;
                    }
                };
        }

        public Formattable formatMethodName(final String className, final String methodName, final String signature) {
            return new Formattable() {
                    public String toFormatted() {
                        ClientUtils.SourceCodeSelection tmpSelection = new ClientUtils.SourceCodeSelection(className, methodName,
                                                                                                           signature);
                        String name = WebProjectUtils.getJSPPath(tmpSelection);

                        return name;
                    }
                };
        }
    }

    private static class MyPropertyProvider implements PropertyProvider {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private Properties props;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        private MyPropertyProvider(Properties props) {
            this.props = props;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public Map /*<String,String>*/ getProperties() {
            return props;
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants                                                                                     "J2EEProjectTypeProfiler_ModifyBuildScriptManuallyMsg"); // NOI18N
    private static final String PROFILING_NOT_SUPPORTED_MSG = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                                  "J2EEProjectTypeProfiler_ProfilingNotSupportedMsg"); // NOI18N
    private static final String SKIP_BUTTON_NAME = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                       "J2EEProjectTypeProfiler_SkipButtonName"); // NOI18N
    private static final String NO_SERVER_FOUND_MSG = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                          "J2EEProjectTypeProfiler_NoServerFoundMsg"); // NOI18N                                                                                                                       // -----
    public static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.profiler.j2ee"); // NOI18N

    // not very clean, consider implementing differently!
    // stores last generated agent ID
    private static int lastAgentID = -1;

    // stores last used agent port
    private static int lastAgentPort = 5140;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private SelectProfilingTask.SettingsConfigurator configurator;
    private LoadGenPanel loadGenConfig = null;

    private String loadGenPath = null;
    private PropertyChangeListener pcl = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(LoadGenPanel.PATH)) {
                loadGenPath = (String) evt.getNewValue();
            }
        }
    };


    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public J2EEProjectTypeProfiler() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static int getLastAgentID() {
        return lastAgentID;
    }

    public static int getLastAgentPort() {
        return lastAgentPort;
    }

    public static String getServerInstanceID(final Project project) {
        J2eeModuleProvider serverInstanceModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);

        if (serverInstanceModuleProvider == null) {
            return null;
        }

        return serverInstanceModuleProvider.getServerInstanceID();
    }

    // --- ProjectTypeProfiler implementation ------------------------------------------------------------------------------

    public String getProfilerTargetName(final Project project, final FileObject buildScript, final int type,
                                        final FileObject profiledClass) {
        switch (type) {
            case TARGET_PROFILE:
                return "profile"; // NOI18N
            case TARGET_PROFILE_TEST:
                return null; // not currently supported // "profile-test"; // NOI18N
            case TARGET_PROFILE_TEST_SINGLE:
                return "profile-test-single"; // NOI18N
            default:
                return null;
        }
    }

    public boolean isProfilingSupported(final Project project) {
        return true;
    }

    public JavaPlatform getProjectJavaPlatform(Project project) {
        String serverInstanceID = getServerInstanceID(project);

        if (serverInstanceID == null) {
            return null;
        }

        return getServerJavaPlatform(serverInstanceID);
    }

    public static JavaPlatform getServerJavaPlatform(String serverInstanceID) {
        J2eePlatform j2eePlatform = getJ2eePlatform(serverInstanceID);

        if (j2eePlatform == null) {
            return null;
        }

        return j2eePlatform.getJavaPlatform();
    }

    public JComponent getAdditionalConfigurationComponent(Project project) {
        if (loadGenConfig == null) {
            //      Set<String> extSet = new HashSet<String>();
            //      extSet.add("jmx");
            loadGenConfig = new LoadGenPanel();
            //      loadGenConfig.setStartDir(FileUtil.toFile(project.getProjectDirectory()));
            //      loadGenConfig.setSupportedExtensions(extSet);
            loadGenConfig.addPropertyChangeListener(LoadGenPanel.PATH, WeakListeners.propertyChange(pcl, loadGenConfig));

            //      loadGenPath = loadGenConfig.getSelectedScript();
        }

        loadGenConfig.attach(project);

        return loadGenConfig;
    }

    public SelectProfilingTask.SettingsConfigurator getSettingsConfigurator() {
        if (configurator == null) {
            configurator = new DefaultSettingsConfigurator() {
                    public LoadGenPanel getCustomSettingsPanel() {
                        if (isAttach() || isModify()) {
                            return null; // TODO: would be better to show LoadGenPanel disabled for Modify Profile
                        }

                        if (loadGenConfig == null) {
                            loadGenConfig = new LoadGenPanel();
                            loadGenConfig.addPropertyChangeListener(LoadGenPanel.PATH,
                                                                    WeakListeners.propertyChange(pcl, loadGenConfig));
                        }

                        loadGenConfig.attach(getProject());

                        return loadGenConfig;
                    }

                    public void loadCustomSettings(Properties properties) {
                        if (loadGenConfig != null) {
                            loadGenConfig.loadCustomSettings(properties);
                        }
                    }
                    ;
                    public void storeCustomSettings(Properties properties) {
                        if (loadGenConfig != null) {
                            loadGenConfig.storeCustomSettings(properties);
                        }
                    }
                    ;
                };
        }

        return configurator;
    }

    public boolean checkProjectCanBeProfiled(final Project project, final FileObject profiledClassFile) {
        // Unsupported project type
        if (!isProfilingSupported(project)) {
            return false;
        }

        // Check if server supports profiling
        J2eePlatform j2eePlatform = getJ2eePlatform(project);

        if (j2eePlatform == null) {
            NetBeansProfiler.getDefaultNB().displayError(NO_SERVER_FOUND_MSG);

            return false;
        }

        if (!j2eePlatform.supportsProfiling()) {
            // Server doesn't support profiling
            ProfilerDialogs.notify(new NotifyDescriptor.Message(PROFILING_NOT_SUPPORTED_MSG, NotifyDescriptor.WARNING_MESSAGE));

            return false;
        }

        // Web Project running on supported server
        if (profiledClassFile == null) {
            // Profile project
            return true;
        } else {
            // Profile single
            return isFileObjectSupported(project, profiledClassFile);
        }
    }

    public void configurePropertiesForProfiling(final Properties props, final Project project, final FileObject profiledClassFile) {
        initAntPlatform(project, props);
        // set forceRestart
        props.setProperty("profiler.j2ee.serverForceRestart", "true"); // NOI18N
                                                                       // set timeout

        props.setProperty("profiler.j2ee.serverStartupTimeout", "300000"); // NOI18N
                                                                           // set agent id

        props.setProperty("profiler.j2ee.agentID", "-Dnbprofiler.agentid=" + new Integer(generateAgentID()).toString()); // NOI18N // sets lastAgentID
                                                                                                                         // redirect profiler.info.jvmargs to profiler.info.jvmargs.extra
                                                                                                                         // NOTE: disabled as a workaround for Issue 102323, needs to be fixed in order to restore the OOME detection functionality!

        String jvmArgs = props.getProperty("profiler.info.jvmargs"); // NOI18N

        if ((jvmArgs != null) && (jvmArgs.trim().length() > 0)) {
            props.setProperty("profiler.info.jvmargs.extra", jvmArgs);
        }

        // fix agent startup arguments
        JavaPlatform javaPlatform = getJavaPlatformFromAntName(project, props);
        props.setProperty("profiler.info.javaPlatform", javaPlatform.getProperties().get("platform.ant.name")); // set the used platform ant property

        String javaVersion = IDEUtils.getPlatformJDKVersion(javaPlatform);
        String localPlatform = IntegrationUtils.getLocalPlatform(IDEUtils.getPlatformArchitecture(javaPlatform));

        if (javaVersion.equals(CommonConstants.JDK_15_STRING)) {
            // JDK 1.5 used
            props.setProperty("profiler.info.jvmargs.agent", // NOI18N
                              IntegrationUtils.getProfilerAgentCommandLineArgs(localPlatform, IntegrationUtils.PLATFORM_JAVA_50,
                                                                               false,
                                                                               ProfilerIDESettings.getInstance().getPortNo()));
        } else {
            // JDK 1.6 or later used
            props.setProperty("profiler.info.jvmargs.agent", // NOI18N
                              IntegrationUtils.getProfilerAgentCommandLineArgs(localPlatform, IntegrationUtils.PLATFORM_JAVA_60,
                                                                               false,
                                                                               ProfilerIDESettings.getInstance().getPortNo()));
        }

        generateAgentPort(); // sets lastAgentPort

        if (loadGenPath != null) {
            props.setProperty("profiler.loadgen.path", loadGenPath); // TODO factor out "profiler.loadgen.path" to a constant
        }

        if (profiledClassFile == null) {
            return;
        }

        if (WebProjectUtils.isJSP(profiledClassFile)) {
            props.put("client.urlPart", WebProjectUtils.getJSPFileContext(project, profiledClassFile, false)); // NOI18N
        } else if (WebProjectUtils.isHttpServlet(profiledClassFile)) {
            String servletAddress = null;
            Collection<Document> ddos = WebProjectUtils.getDeploymentDescriptorDocuments(project, true);

            for (Document dd : ddos) {
                String mapping = WebProjectUtils.getServletMapping(profiledClassFile, dd);

                if ((mapping != null) && (mapping.length() > 0)) {
                    servletAddress = mapping;

                    break;
                }
            }

            if (servletAddress != null) {
                ServletUriPanel uriPanel = new ServletUriPanel(servletAddress);
                DialogDescriptor desc = new DialogDescriptor(uriPanel,
                                                             org.openide.util.NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                                                  "TTL_setServletExecutionUri"),
                                                             true, // NOI18N
                                                             new Object[] {
                                                                 DialogDescriptor.OK_OPTION,
                                                                 new javax.swing.JButton(SKIP_BUTTON_NAME) {
                        public java.awt.Dimension getPreferredSize() {
                            return new java.awt.Dimension(super.getPreferredSize().width + 16, super.getPreferredSize().height);
                        }
                    }
                                                             }, DialogDescriptor.OK_OPTION, DialogDescriptor.BOTTOM_ALIGN, null,
                                                             null);
                Object res = ProfilerDialogs.notify(desc);

                if (res.equals(NotifyDescriptor.YES_OPTION)) {
                    servletAddress = uriPanel.getServletUri();
                }

                props.put("client.urlPart", servletAddress); // NOI18N
            }
        }
        String profiledClass = SourceUtils.getToplevelClassName(profiledClassFile);
        props.setProperty("profile.class", profiledClass); //NOI18N
        // include it in javac.includes so that the compile-single picks it up
        final String clazz = FileUtil.getRelativePath(ProjectUtilities.getRootOf(
                ProjectUtilities.getSourceRoots(project),profiledClassFile), 
                profiledClassFile);
        props.setProperty("javac.includes", clazz); //NOI18N
    }

    // --- Profiler SPI support --------------------------------------------------------------------------------------------
    public static int generateAgentID() {
        int newAgentID = generateAgentNumber(); // generate new agent ID

        while (newAgentID == lastAgentID) {
            newAgentID = generateAgentNumber(); // ensure that it's different from previous ID
        }

        lastAgentID = newAgentID; // assign new agent ID

        return getLastAgentID();
    }

    public static JavaPlatform generateAgentJavaPlatform(String serverInstanceID) {
        JavaPlatform platform = IDEUtils.getJavaPlatformByName(ProfilerIDESettings.getInstance().getJavaPlatformForProfiling());
        JavaPlatform projectPlatform = getServerJavaPlatform(serverInstanceID);

        if (platform == null) { // should use the one defined in project
            platform = projectPlatform;

            if ((platform == null) || !MiscUtils.isSupportedJVM(platform.getSystemProperties())) {
                platform = JavaPlatformSelector.getDefault().selectPlatformToUse();

                if (platform == null) {
                    return null;
                }
            }
        }

        return platform;
    }

    public static int generateAgentPort() {
        lastAgentPort = ProfilerIDESettings.getInstance().getPortNo(); // should be reimplemented, may return different port than the passed by AntActions to target JVM

        return getLastAgentPort();
    }

    public void setupProjectSessionSettings(final Project project, final SessionSettings ss) {
        // settings required for code fragment profiling
        final PropertyEvaluator pp = getProjectProperties(project);
        ss.setMainClass(""); // NOI18N

        String appArgs = pp.getProperty("application.args"); // NOI18N
        ss.setMainArgs((appArgs != null) ? appArgs : ""); // NOI18N

        String runCP = pp.getProperty("build.classes.dir"); // NOI18N
        ss.setMainClassPath((runCP != null)
                            ? MiscUtils.getAbsoluteFilePath(runCP,
                                                            FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath()) : ""); // NOI18N
        ss.setJVMArgs(""); // NOI18N
        ss.setWorkingDir(""); // NOI18N
    }

    public boolean supportsSettingsOverride() {
        return true; // supported for J2EE project
    }

    public boolean supportsUnintegrate(Project project) {
        return true;
    }

    public void unintegrateProfiler(Project project) {
        ProjectUtilities.unintegrateProfiler(project);
    }

    private static J2eePlatform getJ2eePlatform(final Project project) {
        String serverInstanceID = getServerInstanceID(project);

        if (serverInstanceID == null) {
            return null;
        }

        return getJ2eePlatform(serverInstanceID);
    }

    private static J2eePlatform getJ2eePlatform(String serverInstanceID) {
        return Deployment.getDefault().getJ2eePlatform(serverInstanceID);
    }

    private static JavaPlatform getJavaPlatformFromAntName(Project project, Properties props) {
        String javaPlatformAntName = props.getProperty("profiler.info.javaPlatform"); // NOI18N
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();

        if (javaPlatformAntName.equals("default_platform")) {
            return jpm.getDefaultPlatform();
        }

        JavaPlatform[] platforms = jpm.getPlatforms(null, new Specification("j2se", null)); //NOI18N

        for (int i = 0; i < platforms.length; i++) {
            JavaPlatform platform = platforms[i];
            String antName = platform.getProperties().get("platform.ant.name"); // NOI18N

            if (antName.equals(javaPlatformAntName)) {
                return platform;
            }
        }

        return null;
    }

    // --- Private methods -------------------------------------------------------------------------------------------------
    private static int generateAgentNumber() {
        return (int) (Math.random() * (float) Integer.MAX_VALUE);
    }

    private static void initAntPlatform(Project project, Properties props) {
        String javaPlatformAntName = props.getProperty("profiler.info.javaPlatform"); // NOI18N

        if (javaPlatformAntName == null) {
            JavaPlatform platform = null;
            J2eePlatform j2eepf = getJ2eePlatform(project); // try to get the J2EE Platform

            if (j2eepf == null) {
                platform = JavaPlatformManager.getDefault().getDefaultPlatform(); // no J2EE Platform sepcified; use the IDE default JVM platform
            } else {
                platform = j2eepf.getJavaPlatform(); // use the J2EE Platform specified JVM platform
            }

            props.setProperty("profiler.info.javaPlatform", platform.getProperties().get("platform.ant.name")); // set the used platform ant property
        }
    }

    private PropertyEvaluator getProjectProperties(final Project project) {
        final Properties privateProps = new Properties();
        final Properties projectProps = new Properties();
        final Properties userPropsProps = new Properties();

        final FileObject privatePropsFile = project.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        final FileObject projectPropsFile = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        final File userPropsFile = InstalledFileLocator.getDefault().locate("build.properties", null, false); // NOI18N

        ProjectManager.mutex().readAccess(new Runnable() {
                public void run() {
                    // the order is 1. private, 2. project, 3. user to reflect how Ant handles property definitions (immutable, once set property value cannot be changed)
                    if (privatePropsFile != null) {
                        try {
                            final InputStream is = privatePropsFile.getInputStream();

                            try {
                                privateProps.load(is);
                            } finally {
                                is.close();
                            }
                        } catch (IOException e) {
                            err.notify(ErrorManager.INFORMATIONAL, e);
                        }
                    }

                    if (projectPropsFile != null) {
                        try {
                            final InputStream is = projectPropsFile.getInputStream();

                            try {
                                projectProps.load(is);
                            } finally {
                                is.close();
                            }
                        } catch (IOException e) {
                            err.notify(ErrorManager.INFORMATIONAL, e);
                        }
                    }

                    if (userPropsFile != null) {
                        try {
                            final InputStream is = new BufferedInputStream(new FileInputStream(userPropsFile));

                            try {
                                userPropsProps.load(is);
                            } finally {
                                is.close();
                            }
                        } catch (IOException e) {
                            err.notify(ErrorManager.INFORMATIONAL, e);
                        }
                    }
                }
            });

        PropertyEvaluator pe = PropertyUtils.sequentialPropertyEvaluator(null,
                                                                         new PropertyProvider[] {
                                                                             new J2EEProjectTypeProfiler.MyPropertyProvider(privateProps),
                                                                             new J2EEProjectTypeProfiler.MyPropertyProvider(userPropsProps),
                                                                             new J2EEProjectTypeProfiler.MyPropertyProvider(projectProps)
                                                                         });

        return pe;
    }

    private void addJspMarker(MethodMarker marker, Mark mark, Project project) {
        ClientUtils.SourceCodeSelection[] jspmethods = WebProjectUtils.getJSPRootMethods(project, true);

        if (jspmethods != null) {
            for (int i = 0; i < jspmethods.length; i++) {
                marker.addMethodMark(jspmethods[i].getClassName(), jspmethods[i].getMethodName(),
                                     jspmethods[i].getMethodSignature(), mark);
            }
        }
    }
}
