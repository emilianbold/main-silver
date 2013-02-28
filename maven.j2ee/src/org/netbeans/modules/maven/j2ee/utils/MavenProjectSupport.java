/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.j2ee.utils;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.j2ee.common.ui.BrokenServerLibrarySupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerManager;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.api.problem.ProblemReport;
import org.netbeans.modules.maven.api.problem.ProblemReporter;
import org.netbeans.modules.maven.j2ee.MavenJavaEEConstants;
import org.netbeans.modules.maven.j2ee.SessionContent;
import org.netbeans.modules.maven.j2ee.ear.EarModuleProviderImpl;
import org.netbeans.modules.maven.j2ee.ejb.EjbModuleProviderImpl;
import org.netbeans.modules.maven.j2ee.web.WebModuleImpl;
import org.netbeans.modules.maven.j2ee.web.WebModuleProviderImpl;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Provides a various methods to help with typical Maven Projects requirements
 * For example changing server for given project, changing pom.xml, creating de
 * 
 * @author Martin Janicek
 */
public class MavenProjectSupport {

    private MavenProjectSupport() {
    }
    
    
    /**
     * Change server for given project according to the project lookup values
     * 
     * @param project for which we want to change server
     * @param initContextPath true if we want to initiate context path (f.e. when creating new project), false otherwise
     */
    public static synchronized void changeServer(Project project, boolean initContextPath) {
        if (project == null) {
            return;
        }
        String[] ids = obtainServerIds(project);
        String instanceID = ids[0];
        String serverID = ids[1];
        
        ProblemReporter problems = project.getLookup().lookup(ProblemReporter.class);

        // We know server instance which should be assigned to the project
        if (instanceID != null && serverID == null) {
            assignServer(project, instanceID, initContextPath);
            
        // We don't know anything which means we want to assign <No Server> value to the project
        } else if (instanceID == null && serverID == null) {
            assignServer(project, null, initContextPath);

        // We don't know server instance - inform user about that
        } else if (instanceID == null && serverID != null) {
            problems.addReport(createMissingServerReport(project, serverID));
        }
        
        J2eeModuleProvider moduleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (moduleProvider != null) {
            if (!BrokenServerLibrarySupport.getMissingServerLibraries(project).isEmpty()) {
                problems.addReport(createBrokenLibraryReport(project));
                BrokenServerLibrarySupport.fixOrShowAlert(project, null);
            }
            if (RunUtils.hasApplicationCompileOnSaveEnabled(project)) {
                Deployment.getDefault().enableCompileOnSaveSupport(moduleProvider);
            }
        }
    }
    
    /**
     * Assign specified server to given project
     * 
     * @param project for we want to change server
     * @param instanceID server instance which should be assigned
     * @param initContextPath true if context path should be initialized to non-empty value
     */
    private static void assignServer(Project project, String instanceID, boolean initContextPath) {
        setServer(project, project.getLookup().lookup(WebModuleProviderImpl.class), instanceID);
        setServer(project, project.getLookup().lookup(EjbModuleProviderImpl.class), instanceID);
        setServer(project, project.getLookup().lookup(EarModuleProviderImpl.class), instanceID);

        if (initContextPath) {
            initContextPath(project);
        }
    }
    
    private static void setServer(Project project, J2eeModuleProvider moduleProvider, String serverID) {
        if (moduleProvider != null) {
            if (J2eeModule.Type.WAR.equals(moduleProvider.getJ2eeModule().getType())) {
                MavenProjectSupport.createDDIfRequired(project, serverID);
            }
            
            moduleProvider.setServerInstanceID(serverID);
            moduleProvider.getConfigSupport().ensureConfigurationReady();
        }
    }
    
    /*
     * Setup context path to a non-empty value (typically project artifactID) 
     * Should be used f.e. when creating new project
     */
    private static void initContextPath(Project project) {
        NbMavenProject mavenProject = project.getLookup().lookup(NbMavenProject.class);
        WebModuleProviderImpl webModuleProvider = project.getLookup().lookup(WebModuleProviderImpl.class);
        
        if (NbMavenProject.TYPE_WAR.equals(mavenProject.getPackagingType()) == false || webModuleProvider == null) {
            return; // We want to set context path only for Web projects
        }
        
        WebModuleImpl webModuleImpl = webModuleProvider.getModuleImpl();
        String contextPath = webModuleImpl.getContextPath();
        
        if (contextPath == null || "".equals(contextPath)) {
            webModuleImpl.setContextPath("/" + mavenProject.getMavenProject().getArtifactId()); //NOI18N
        }
    }
    
    private static ProblemReport createMissingServerReport(Project project, String serverID) {
        String serverName = Deployment.getDefault().getServerDisplayName(serverID);
        if (serverName == null) {
            serverName = serverID;
        }
        ProblemReport serverProblem = new ProblemReport(ProblemReport.SEVERITY_HIGH, 
                NbBundle.getMessage(MavenProjectSupport.class, "MSG_AppServer", serverName),
                NbBundle.getMessage(MavenProjectSupport.class, "HINT_AppServer"),
                new AddServerAction(project));
        return serverProblem;
    }
    
    private static ProblemReport createBrokenLibraryReport(Project project) {
        ProblemReport libProblem =  new ProblemReport(ProblemReport.SEVERITY_HIGH,
                NbBundle.getMessage(MavenProjectSupport.class, "MSG_LibProblem"),
                NbBundle.getMessage(MavenProjectSupport.class, "MSG_LibProblem_Description"),
                new ServerLibraryAction(project));
        return libProblem;
    }
    
    public static boolean isWebSupported(Project project, String packaging) {
        if ("war".equals(packaging) || isBundlePackaging(project, packaging)) { // NOI18N
            return true;
        }
        return false;
    }
    
    // #179584
    // if it is bundle packaging type but a valid "src/main/webapp" exists
    // then provide lookup content as for web application so that code
    // completion etc. works
    public static boolean isBundlePackaging(Project project, String packaging) {
        NbMavenProject proj = project.getLookup().lookup(NbMavenProject.class);
        
        boolean isBundlePackaging = "bundle".equals(packaging); // NOI18N
        boolean webAppDirExists = org.openide.util.Utilities.toFile(proj.getWebAppDirectory()).exists();
        
        if (isBundlePackaging && webAppDirExists) {
            return true;
        }
        return false;
    }
    
    /**
     * Return server instance ID if set (that is concrete server instance) and if not available
     * try to return at least server ID
     * @return always array of two String values - first one is server instance ID and 
     *    second one server ID; both can be null
     */
    public static String[] obtainServerIds (Project project) {
        SessionContent sc = project.getLookup().lookup(SessionContent.class);
        if (sc != null && sc.getServerInstanceId() != null) {
            return new String[] {sc.getServerInstanceId(), null};
        }
        AuxiliaryProperties props = project.getLookup().lookup(AuxiliaryProperties.class);
        // XXX should this first look up HINT_DEPLOY_J2EE_SERVER_ID in project (profile, ...) properties? Cf. Wrapper.createComboBoxUpdater.getDefaultValue
        String serverID = props.get(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_ID, false);
        if (serverID != null) {
            return new String[] {serverID, null};
        }
        String serverType = props.get(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER, true);
        if (serverType == null) {
            serverType = props.get(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_OLD, true);
        }
        return new String[]{null, serverType};
    }
    
    /**
     * For given project returns server name
     * 
     * @param project for which we want to get server name
     * @return server name or <code>null</code> if the assigned server instance were removed during the processing
     */
    public static String obtainServerName (Project project) {
        String id = obtainServerIds(project)[0];

        if (id != null) {
            ServerInstance si = Deployment.getDefault().getServerInstance(id);
            if (si != null) {
                try {
                    return si.getDisplayName();
                } catch (InstanceRemovedException ex) {
                    Logger.getLogger(MavenProjectSupport.class.getName()).log(Level.FINE, "", ex);
                }
            }
        }

        return null;
    }

    /**
     * For the given server instance ID returns serverID
     * @param serverInstanceID instance ID
     * @return server ID or <code>null</code> if the assigned server instance were removed during the processing
     */
    public static String obtainServerID(String serverInstanceID) {
        ServerInstance si = Deployment.getDefault().getServerInstance(serverInstanceID);
        try {
            return si.getServerID();
        } catch (InstanceRemovedException ex) {
            return null;
        }
    }
    
    /**
     * Store given property pair <name, value> to pom.xml file of the given project
     * 
     * @param projectFile project to which pom.xml should be updated
     * @param name property name
     * @param value property value
     */
    public static void storeSettingsToPom(Project project, final String name, final String value) {
        storeSettingsToPom(project.getProjectDirectory(), name, value);
    }
    
    public static void storeSettingsToPom(FileObject projectFile, final String name, final String value) {
        final ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {

            @Override
            public void performOperation(POMModel model) {
                Properties props = model.getProject().getProperties();
                if (props == null) {
                    props = model.getFactory().createProperties();
                    model.getProject().setProperties(props);
                }
                props.setProperty(name, value);
            }
        };
        final FileObject pom = projectFile.getFileObject("pom.xml"); //NOI18N
        try {
            pom.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                @Override
                public void run() throws IOException {
                    Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
                }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public static void createDDIfRequired(Project project) {
        createDDIfRequired(project, null);
    }
    
    /**
     * Creates web.xml deployment descriptor if it's required for given project (this method was created as a
     * workaround for issue #204572 and probably won't be needed when WebLogic issue will be fixed)
     * 
     * @param project project for which should DD be generated
     * @param serverID server ID of given project
     */
    public static void createDDIfRequired(Project project, String serverID) {
        if (serverID == null) {
            serverID = readServerID(project);
        }
        // TODO change condition to use ConfigSupportImpl.isDescriptorRequired
        if (serverID != null && serverID.contains("WebLogic")) { //NOI18N
            createDD(project);
        }
    }
    
    private static void createDD(Project project) {
        WebModuleProviderImpl webModule = project.getLookup().lookup(WebModuleProviderImpl.class);
        
        if (webModule != null) {
            WebModuleImpl webModuleImpl = webModule.getModuleImpl();
            try {
                FileObject webInf = webModuleImpl.getWebInf();
                if (webInf == null) {
                    webInf = webModuleImpl.createWebInf();
                    if (webInf == null) {
                        return;
                    }
                }
                
                FileObject webXml = webModuleImpl.getDeploymentDescriptor();
                if (webXml == null) {
                    String j2eeVersion = readJ2eeVersion(project);
                    webXml = DDHelper.createWebXml(Profile.fromPropertiesString(j2eeVersion), webInf);
    
                    // this should never happend if valid j2eeVersion has been parsed - see also issue #214600
                    assert webXml != null : "DDHelper wasn't able to create deployment descriptor for the J2EE version: " + j2eeVersion
                            + ", Profile.fromPropertiesString(j2eeVersion) returns: " + Profile.fromPropertiesString(j2eeVersion);
                }

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    /**
     * Read server ID for the given project
     * 
     * @param project project for which we want to get server ID
     * @return server ID
     */
    public static String readServerID(Project project) {
        return readSettings(project, MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER, true);
    }
    
    /**
     * Read server instance ID for the given project
     * 
     * @param project project for which we want to get server ID
     * @return server ID
     */
    public static String readServerInstanceID(Project project) {
        return readSettings(project, MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_ID, false);
    }

    /**
     * Read J2EE version for the given project
     * 
     * @param projectfor which we want to get J2EE version
     * @return J2EE version
     */
    public static String readJ2eeVersion(Project project)  {
        return readSettings(project, MavenJavaEEConstants.HINT_J2EE_VERSION, true);
    }
    
    private static String readSettings(Project project, String propertyName, boolean shared) {
        return project.getLookup().lookup(AuxiliaryProperties.class).get(propertyName, shared);
    }
    
    
    
    public static void setJ2eeVersion(Project project, String value) {
        setSettings(project, MavenJavaEEConstants.HINT_J2EE_VERSION, value, true);
    }
    
    public static void setServerID(Project project, String value) {
        setSettings(project, MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER, value, true);
    }
    
    public static void setOldServerInstanceID(Project project, String value) {
        setSettings(project, MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_OLD, value, true);
    }
    
    public static void setServerInstanceID(Project project, String value) {
        setSettings(project, MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_ID, value, false);
    }
    
    private static void setSettings(Project project, String key, String value, boolean shared) {
        AuxiliaryProperties props = project.getLookup().lookup(AuxiliaryProperties.class);
        props.put(key, value, shared);
    }
    
    private static class AddServerAction extends AbstractAction {
        private Project prj;
        private AddServerAction(Project project) {
            prj = project;
            putValue(Action.NAME, NbBundle.getMessage(MavenProjectSupport.class, "TXT_Add_Server")); //NOI18N
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final String newOne = ServerManager.showAddServerInstanceWizard();
                    final String serverType = newOne != null ? obtainServerID(newOne) : null;
                    Utilities.performPOMModelOperations(prj.getProjectDirectory().getFileObject("pom.xml"), Collections.singletonList(new ModelOperation<POMModel>() { //NOI18N
                        @Override public void performOperation(POMModel model) {
                            if (newOne != null) {
                                Properties props = model.getProject().getProperties();
                                if (props == null) {
                                    props = model.getFactory().createProperties();
                                    model.getProject().setProperties(props);
                                }
                                props.setProperty(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER, serverType);
                            } else {
                                Properties props = model.getProject().getProperties();
                                if (props != null) {
                                    props.setProperty(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER, null);
                                }
                            }
                        }
                    }));
                    prj.getLookup().lookup(AuxiliaryProperties.class).put(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_ID, newOne, false);
                }
            });
        }
    }

    private static class ServerLibraryAction extends AbstractAction {

        private Project project;
        public ServerLibraryAction(Project project) {
            putValue(NAME, NbBundle.getMessage(MavenProjectSupport.class, "LBL_LibProblem_ActionName")); //NOI18N
            this.project = project;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BrokenServerLibrarySupport.fixServerLibraries(project, new Runnable() {
                @Override
                public void run() {
                    NbMavenProject.fireMavenProjectReload(project);
                }
            });
        }
    }
}
