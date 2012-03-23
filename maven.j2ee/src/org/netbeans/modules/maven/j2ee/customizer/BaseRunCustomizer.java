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
package org.netbeans.modules.maven.j2ee.customizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.customizer.support.CheckBoxUpdater;
import org.netbeans.modules.maven.api.customizer.support.ComboBoxUpdater;
import org.netbeans.modules.maven.j2ee.ExecutionChecker;
import org.netbeans.modules.maven.j2ee.MavenJavaEEConstants;
import org.netbeans.modules.maven.j2ee.SessionContent;
import org.netbeans.modules.maven.j2ee.Wrapper;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;

/**
 *
 * @author Martin Janicek
 */
public abstract class BaseRunCustomizer extends JPanel implements ApplyChangesCustomizer {
    
    protected Project project;
    protected ModelHandle2 handle;
    protected CheckBoxUpdater deployOnSaveUpdater;
    protected ComboBoxUpdater<Wrapper> serverModelUpdater;
    

    public BaseRunCustomizer(ModelHandle2 handle, Project project) {
        this.handle = handle;
        this.project = project;
    }
    
    //mkleint: this method should only be run from within the ApplyChangesCustomizer.applyChanges() method
    protected void changeServer(JComboBox selectedServerComboBox) {
        SessionContent sc = project.getLookup().lookup(SessionContent.class);
        if (serverModelUpdater.getValue() != null) {
            sc.setServerInstanceId(null);
        }
        
        Wrapper selectedServer = (Wrapper) selectedServerComboBox.getSelectedItem();
        // User is trying to set <No Server> option
        if (ExecutionChecker.DEV_NULL.equals(selectedServer.getServerInstanceID())) {
            MavenProjectSupport.setServerID(project, null);
            MavenProjectSupport.setServerInstanceID(project, null);
            MavenProjectSupport.setOldServerInstanceID(project, null);
        }
        
        MavenProjectSupport.changeServer(project, false);
    }
    
    protected void initDeployOnSaveComponent(final JCheckBox dosCheckBox, final JLabel dosDescription) {
        deployOnSaveUpdater = new CheckBoxUpdater(dosCheckBox) {
            @Override
            public Boolean getValue() {
                String s = handle.getRawAuxiliaryProperty(MavenJavaEEConstants.HINT_DEPLOY_ON_SAVE, true);
                if (s != null) {
                    return Boolean.valueOf(s);
                } else {
                    return null;
                }
            }

            @Override
            public void setValue(Boolean value) {
                handle.setRawAuxiliaryProperty(MavenJavaEEConstants.HINT_DEPLOY_ON_SAVE, 
                        value == null ? null : Boolean.toString(value), true);
            }

            @Override
            public boolean getDefaultValue() {
                return true;
            }
        };
        
        addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent event) {
                updateDoSEnablement(dosCheckBox, dosDescription);
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
    }
    
    protected void initServerComponent(JComboBox serverComboBox, JLabel serverLabel) {
        serverModelUpdater = Wrapper.createComboBoxUpdater(handle, serverComboBox, serverLabel);
    }
    
    private void updateDoSEnablement(JCheckBox dosCheckBox, JLabel dosDescription) {
        String cos = handle.getRawAuxiliaryProperty(Constants.HINT_COMPILE_ON_SAVE, true);
        boolean enabled = cos == null || "all".equalsIgnoreCase(cos) || "app".equalsIgnoreCase(cos); // NOI18N
        dosCheckBox.setEnabled(enabled);
        dosDescription.setEnabled(enabled);
    }
    
    protected void loadServerModel(JComboBox serverModel, J2eeModule.Type type, Profile profile) {
        String[] ids = Deployment.getDefault().getServerInstanceIDs(Collections.singleton(type), profile);
        Collection<Wrapper> col = new ArrayList<Wrapper>();

        SessionContent sc = project.getLookup().lookup(SessionContent.class);
        if (sc != null && sc.getServerInstanceId() != null) {
            col.add(new Wrapper(ExecutionChecker.DEV_NULL, sc.getServerInstanceId()));
        } else {
            col.add(new Wrapper(ExecutionChecker.DEV_NULL));
        }
        for (String id : ids) {
            col.add(new Wrapper(id));
        }
        serverModel.setModel(new DefaultComboBoxModel(col.toArray()));
    }
}
