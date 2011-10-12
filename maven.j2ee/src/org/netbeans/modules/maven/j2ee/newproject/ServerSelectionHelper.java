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
package org.netbeans.modules.maven.j2ee.newproject;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerManager;
import org.netbeans.modules.maven.j2ee.ExecutionChecker;
import org.netbeans.modules.maven.j2ee.MavenJavaEEConstants;
import org.netbeans.modules.maven.j2ee.Wrapper;
import org.openide.WizardDescriptor;

/**
 * This class wraps all code which need to be use when creating new Maven project with Server selection.
 * It could be used for all types of projects (Ear, Ejb, War, Client App)
 * 
 * @author Martin Janicek
 */
public class ServerSelectionHelper {
    
    private final Deployment deployment;
    private final JComboBox serverModel;
    private final JComboBox j2eeVersion;
    private final J2eeModule.Type projectType;

    /**
     * Creates new Helper instance for specific project type
     * It also initiate server model and platform version model combo boxes so they are filled only with supported options
     * 
     * @param serverModel combo box for all possible servers
     * @param j2eeVersion combo box for J2ee version specification
     * @param projectType project type
     */
    public ServerSelectionHelper(JComboBox serverModel, JComboBox j2eeVersion, J2eeModule.Type projectType) {
        this.deployment = Deployment.getDefault();
        this.serverModel = serverModel;
        this.projectType = projectType;
        
        this.j2eeVersion = j2eeVersion;
        this.j2eeVersion.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                return super.getListCellRendererComponent(list, ((Profile) value).getDisplayName(), index, isSelected, cellHasFocus);
            }
            
            
        });
        initServerModel();
        updatePlatformVersionModel();
    }
    
    /**
     * Initiate servers in comboBox (adds all valid servers plus <No Server> option)
     */
    private void initServerModel() {
        serverModel.removeAllItems();
        
        // Iterate trought all registered servers
        for (String instanceID : deployment.getServerInstanceIDs()) {
            // We want to add only servers with support for defined projectType
            if (isServerInstanceValid(instanceID)) {
                serverModel.addItem(new Wrapper(instanceID));
            }
        }
        
        // We want to provide Maven project without server
        serverModel.addItem(new Wrapper(ExecutionChecker.DEV_NULL));
        
        // And we need to change J2eeVersion comboBox when changing Server selection
        serverModel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updatePlatformVersionModel();
            }
        });
    }
    
    /**
     * Update the platform profile model in dependence of server selection
     * If <No Server Selected> option was chosen then we fill all possible profiles in.
     * If any server was chosen then we fill supported profiles only
     */
    private void updatePlatformVersionModel() {
        Profile lastSelectedProfile = getSelectedProfile();
        String serverInstance = getSelectedServer().getServerInstanceID();
        Set<Profile> profiles = new TreeSet<Profile>(Profile.UI_COMPARATOR);
        
        // If <No Server> option was selected, show all supported profiles
        if (ExecutionChecker.DEV_NULL.equals(serverInstance)) {
            if (J2eeModule.Type.WAR.equals(projectType)) {
                profiles.add(Profile.JAVA_EE_6_WEB);
            } else {
                profiles.add(Profile.JAVA_EE_6_FULL);
            }
            profiles.add(Profile.JAVA_EE_5);
            profiles.add(Profile.J2EE_14);
        } else {
            try {
                profiles.addAll(deployment.getServerInstance(serverInstance).getJ2eePlatform().getSupportedProfiles(projectType));
            } catch (InstanceRemovedException ex) {
                // If selected instance was removed during the process we can easily refresh Server model list and update versions again
                initServerModel();
            }
            
            // We don't support J2EE 1.3 anymore
            if (profiles.contains(Profile.J2EE_13)) {
                profiles.remove(Profile.J2EE_13);
            }
            // We want to have Java EE 6 Full profile for all project types except Web project
            if (J2eeModule.Type.WAR.equals(projectType)) {
                profiles.remove(Profile.JAVA_EE_6_FULL);
            } else {
                profiles.remove(Profile.JAVA_EE_6_WEB);
            }
        }
        
        j2eeVersion.setModel(new DefaultComboBoxModel(profiles.toArray()));
        if (lastSelectedProfile != null && profiles.contains(lastSelectedProfile)) {
            j2eeVersion.setSelectedItem(lastSelectedProfile);
        }
    }
    
    /**
     * Handles situation when we need to Add new server into servers comboBox
     */
    public void addServerButtonPressed() {                                          
        Wrapper selectedServer = getSelectedServer();
        Profile j2eeProfile = getSelectedProfile();
        
        String selectedServerInstanceID = null;
        if (selectedServer != null) {
            selectedServerInstanceID = selectedServer.getServerInstanceID();
        }
        
        // If new server were added then we want to set it as selected
        String addedServerInstanceID = ServerManager.showAddServerInstanceWizard();
        if (addedServerInstanceID != null) {
            selectedServerInstanceID = addedServerInstanceID;
            j2eeProfile = null;
            j2eeVersion.setSelectedItem(null);
        } else {
            return;
        }

        // We need to refresh the list of servers because we've just added one
        initServerModel();
        selectAddedServerInModel(selectedServerInstanceID);
        if (j2eeProfile != null) {
            j2eeVersion.setSelectedItem(j2eeProfile);
        }
    }
    
    public Profile getSelectedProfile() {
        return (Profile) j2eeVersion.getSelectedItem();
    }
    
    /**
     * Store all necessary Server settings and J2EE Version into the WizardDescriptor
     * @param d descriptor used for storing values
     */
    public void storeServerSettings(WizardDescriptor d) {
        Wrapper wrapper = getSelectedServer();
        String instanceID = wrapper.getServerInstanceID();
        String serverID = wrapper.getServerID();
        String version = getSelectedProfile().toPropertiesString();
        
        if (ExecutionChecker.DEV_NULL.equals(instanceID)) {
            instanceID = null;
            serverID = null;
        }
        d.putProperty(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_ID, instanceID);
        d.putProperty(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER, serverID);
        d.putProperty(MavenJavaEEConstants.HINT_J2EE_VERSION, version);
    }
    
    private Wrapper getSelectedServer() {
        return (Wrapper) serverModel.getSelectedItem();
    }
    
    /**
     * Select specified server in servers comboBox
     * @param instanceID server ID
     * @return true if valid server was selected
     */
    private boolean selectAddedServerInModel(String instanceID) {
        if (isServerInstanceValid(instanceID)) {
            serverModel.setSelectedItem(new Wrapper(instanceID));
            return true;
        } else {
            if (serverModel.getItemCount() > 0) {
                serverModel.setSelectedItem(new Wrapper(ExecutionChecker.DEV_NULL));
            }
        }
        return false;
    }
    
    /**
     * @param instance which need to be validated
     * @return true if the server instance is valid and supports EAR projects, otherwise returns false
     */
    private boolean isServerInstanceValid(String instanceID) {
        ServerInstance instance = deployment.getServerInstance(instanceID);
        
        try {
            if (instance != null && 
                instance.getDisplayName() != null && 
                instance.getJ2eePlatform().getSupportedTypes().contains(projectType)) {
                
                return true;
            }
        } catch (InstanceRemovedException ex) {
            return false;
        }
        return false;
    }
    
    private class J2eeVersion {
        
    }
}
