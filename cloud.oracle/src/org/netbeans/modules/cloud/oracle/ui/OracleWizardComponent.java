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

package org.netbeans.modules.cloud.oracle.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */
public class OracleWizardComponent extends javax.swing.JPanel implements DocumentListener {

    private ChangeListener l;
    private static final String ADMIN_URL = "https://wlsservices.cloud.oracle.com"; // NOI18N
    
    static final boolean SHOW_CLOUD_URLS = true; // XXXXXX  //Boolean.getBoolean("oracle.cloud.dev");
    
    /** Creates new form OracleWizardComponent */
    public OracleWizardComponent() {
        initComponents();
        
        // no SDK for now:
        sdkLabel.setVisible(false);
        sdkComboBox.setVisible(false);
        
        // not needed anymore as Application.getApplicationUrls returns full URL
        instanceLabel.setVisible(false);
        instanceURLTextField.setVisible(false);

        // just hardcode this; URL is life internally now
        cloudLabel.setVisible(false);
        cloudURLTextField.setVisible(false);
        cloudURLTextField.setText("http://cloud.oracle.com");
        
        adminLabel.setVisible(SHOW_CLOUD_URLS);
        adminURLTextField.setVisible(SHOW_CLOUD_URLS);
        
        if (SHOW_CLOUD_URLS) {
            serviceGroupTextField.setText("oracle"); // NOI18N
            serviceInstanceTextField.setText("localhost"); // NOI18N
            userNameTextField.setText("system");
            passwordField.setText("welcome1");
            adminURLTextField.setText("http://10.242.22.43:7003/");
            instanceURLTextField.setText("http://10.242.22.43:7013/");
        }
        
        setName(NbBundle.getBundle(OracleWizardComponent.class).getString("LBL_Name")); // NOI18N

        if (!SHOW_CLOUD_URLS) {
            adminURLTextField.setText(ADMIN_URL); // NOI18N
            cloudURLTextField.setText("https://cloud.oracle.com"); // NOI18N
        }
        adminURLTextField.getDocument().addDocumentListener(this);
        instanceURLTextField.getDocument().addDocumentListener(this);
        cloudURLTextField.getDocument().addDocumentListener(this);
        passwordField.getDocument().addDocumentListener(this);
        userNameTextField.getDocument().addDocumentListener(this);
        serviceGroupTextField.getDocument().addDocumentListener(this);
        serviceInstanceTextField.getDocument().addDocumentListener(this);
    }

    void disableModifications(boolean disable) {
        adminURLTextField.setEditable(!disable);
        cloudURLTextField.setEditable(!disable);
        instanceURLTextField.setEditable(!disable);
        passwordField.setEditable(!disable);
        sdkComboBox.setEditable(!disable);
        serviceGroupTextField.setEditable(!disable);
        serviceInstanceTextField.setEditable(!disable);
        userNameTextField.setEditable(!disable);
    }
    
    public void attachSingleListener(ChangeListener l) {
        this.l = l;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        adminLabel = new javax.swing.JLabel();
        adminURLTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        userNameTextField = new javax.swing.JTextField();
        passwordField = new javax.swing.JPasswordField();
        jLabel7 = new javax.swing.JLabel();
        sdkLabel = new javax.swing.JLabel();
        sdkComboBox = new javax.swing.JComboBox();
        serviceInstanceTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        instanceURLTextField = new javax.swing.JTextField();
        instanceLabel = new javax.swing.JLabel();
        serviceGroupTextField = new javax.swing.JTextField();
        cloudLabel = new javax.swing.JLabel();
        cloudURLTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

        jLabel3.setText(org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.jLabel3.text")); // NOI18N

        adminLabel.setText(org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.adminLabel.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.jLabel2.text")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.jLabel4.text")); // NOI18N

        jLabel7.setText(org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.jLabel5.text")); // NOI18N

        sdkLabel.setText(org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.sdkLabel.text")); // NOI18N

        sdkComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TBD", "Version 1.0 [Bundled with IDE]", "Add a new one ..." }));

        jLabel5.setText(org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.jLabel7.text")); // NOI18N

        instanceLabel.setText(org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.instanceLabel.text")); // NOI18N

        cloudLabel.setText(org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.cloudLabel.text")); // NOI18N

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getSize()-2f));
        jLabel1.setForeground(java.awt.Color.blue);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.jLabel1.text")); // NOI18N
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(adminLabel)
                    .addComponent(jLabel5)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4)
                    .addComponent(sdkLabel)
                    .addComponent(instanceLabel)
                    .addComponent(cloudLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(instanceURLTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                    .addComponent(adminURLTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                    .addComponent(sdkComboBox, 0, 229, Short.MAX_VALUE)
                    .addComponent(passwordField, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                    .addComponent(userNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(serviceGroupTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                    .addComponent(serviceInstanceTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                    .addComponent(cloudURLTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serviceGroupTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addGap(0, 0, 0)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serviceInstanceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sdkComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sdkLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(adminLabel)
                    .addComponent(adminURLTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(instanceLabel)
                    .addComponent(instanceURLTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cloudLabel)
                    .addComponent(cloudURLTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked
        try {
            URLDisplayer.getDefault().showURL(new URL(getCloudUrl()));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
}//GEN-LAST:event_jLabel1MouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel adminLabel;
    private javax.swing.JTextField adminURLTextField;
    private javax.swing.JLabel cloudLabel;
    private javax.swing.JTextField cloudURLTextField;
    private javax.swing.JLabel instanceLabel;
    private javax.swing.JTextField instanceURLTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JComboBox sdkComboBox;
    private javax.swing.JLabel sdkLabel;
    private javax.swing.JTextField serviceGroupTextField;
    private javax.swing.JTextField serviceInstanceTextField;
    private javax.swing.JTextField userNameTextField;
    // End of variables declaration//GEN-END:variables

    public String getAdminUrl() {
        return adminURLTextField.getText();
    }
    
    public String getInstanceUrl() {
        return instanceURLTextField.getText();
    }
    
    public String getCloudUrl() {
        return cloudURLTextField.getText();
    }
    
    public String getUserName() {
        return userNameTextField.getText();
    }
    
    public String getPassword() {
        return new String(passwordField.getPassword());
    }
    
    public String getServiceGroup() {
        return serviceGroupTextField.getText();
    }

    public String getServiceInstance() {
        return serviceInstanceTextField.getText();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        update(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        update(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        update(e);
    }
    
    private void update(DocumentEvent e) {
        if (l != null) {
            l.stateChanged(new ChangeEvent(this));
        }
        if (ADMIN_URL.equals(adminURLTextField.getText()) && 
                (e.getDocument() == serviceInstanceTextField.getDocument() ||
                 e.getDocument() == serviceGroupTextField.getDocument())) {
            instanceURLTextField.setText(MessageFormat.format("https://{0}.{1}.wls.cloud.oracle.com", getServiceInstance(), getServiceGroup()));
        }
    }
}
