/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.rest.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.Project;

import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.wizard.AbstractPanel.Settings;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.Utilities;

/**
 *
 */
public class JaxRsConfigurationPanel extends javax.swing.JPanel implements ChangeListener, Settings {
    
    private static final long serialVersionUID = 5841706512529345806L;
    private String parentPackageName = null;
    
    public JaxRsConfigurationPanel( SourcePanel sourcePanel ) {
        initComponents();
        listeners = new ArrayList<ChangeListener>(1);
        this.sourcePanel = sourcePanel;
        updatePackageName();
        
        useJersey.addActionListener( new ActionListener() {
            
            @Override
            public void actionPerformed( ActionEvent event ) {
                if ( useJersey.isSelected()){
                    setEnabledAppConfig(false);
                }
                else {
                    setEnabledAppConfig(true);
                }
                fireChange();
            }
        });
        restAppPackage.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                fireChange();
            }
        });
        
        restAppPackage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                fireChange();
            }
        });
        
        restAppClass.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                fireChange();
            }
        });
    }
    
    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    @Override
    public void stateChanged( ChangeEvent event ) {
        SourceGroup group = getSourceGroup();
        if ( group != null && !group.equals( sourceGroup)){
            sourceGroup = group;
            updateSourceGroupPackages();
        }
        updatePackageName();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.rest.wizard.AbstractPanel.Settings#read(org.openide.WizardDescriptor)
     */
    @Override
    public void read( WizardDescriptor wizard ) {
        sourceGroup = getSourceGroup();
        
        restAppPackage.setRenderer(PackageView.listRenderer());
        updateSourceGroupPackages();
        
        Object property = wizard.getProperty( WizardProperties.USE_JERSEY);
        if ( property!= null && property.toString().equals("true") ){       //
            useJersey.setSelected( true );
        }
        
        Project project = Templates.getProject(wizard);
        final RestSupport restSupport = project.getLookup().
                lookup(RestSupport.class);
        // do not show Jersey option for Jersey 2.0 and/or EE7:
        boolean hideJerseyChoice = restSupport.isEE7() || restSupport.hasJersey2();
        useJersey.setVisible(!hideJerseyChoice);
        useJersey.setSelected(false);

        // in case of EE7 and/or Jersey2 it is not necessary to ask user for
        // Application subclass name and a package - just use default values:
        boolean useDefaultConfiguration = restSupport.isEE7() || restSupport.hasJersey2();
        if (useDefaultConfiguration) {
            jSeparator1.setVisible(false);
            restAppClass.setVisible(false);
            restAppClassLbl.setVisible(false);
            restAppPackage.setVisible(false);
            restAppPckgLbl.setVisible(false);
        }
        String appPackage = (String) wizard.getProperty(
                WizardProperties.APPLICATION_PACKAGE);
        if (appPackage != null) {
            ((JTextComponent) restAppPackage.getEditor().getEditorComponent()).
                setText(appPackage);
        }
        String appClass = (String) wizard.getProperty(
                WizardProperties.APPLICATION_CLASS);
        if ( appClass != null ){
            restAppClass.setText( appClass );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.rest.wizard.AbstractPanel.Settings#store(org.openide.WizardDescriptor)
     */
    @Override
    public void store( WizardDescriptor wizard ) {
        wizard.putProperty(WizardProperties.APPLICATION_PACKAGE, getPackage());
        wizard.putProperty(WizardProperties.APPLICATION_CLASS, 
                restAppClass.getText().trim());
        wizard.putProperty( WizardProperties.USE_JERSEY, useJersey.isSelected());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.rest.wizard.AbstractPanel.Settings#valid(org.openide.WizardDescriptor)
     */
    @Override
    public boolean valid( WizardDescriptor wizard ) {
        AbstractPanel.clearErrorMessage(wizard);
        if ( useJersey.isSelected()){
            return true;
        }
        
        String packageName = getPackage();
        String className = restAppClass.getText().trim();
        
        if (className.length() == 0 || ! Utilities.isJavaIdentifier(className)) {
            AbstractPanel.setErrorMessage(wizard, "MSG_InvalidApplicationClassName");   // NOI18N
            return false;
        }
        else if (! Util.isValidPackageName(packageName)) {
            AbstractPanel.setErrorMessage(wizard, "MSG_InvalidPackageName");            // NOI18N
            return false;
        }
        
        return true;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.rest.wizard.AbstractPanel.Settings#addChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void addChangeListener( ChangeListener listener ) {
        listeners.add(listener);
    }
    
    public double getRenderedHeight(){
        return restAppClass.getLocation().getY()+restAppClass.getHeight()+getGap();
    }
    
    private double getGap(){
        double gap = restAppClass.getLocation().getY();
        gap = gap - (restAppPackage.getLocation().getY() +restAppPackage.getHeight());
        return gap;
    }
    
    private SourceGroup getSourceGroup(){
        return sourcePanel.getSourceGroup();
    }
    
    private void updateSourceGroupPackages() {
        SourceGroup sg = getSourceGroup();
        if (sg != null) {
            ComboBoxModel model = PackageView.createListView(sg);
            if (model.getSelectedItem()!= null && model.getSelectedItem().toString().startsWith("META-INF")
                    && model.getSize() > 1) { // NOI18N
                model.setSelectedItem(model.getElementAt(1));
            }
            String oldValue = ((JTextComponent)restAppPackage.getEditor().getEditorComponent()).getText();
            restAppPackage.setModel(model);
            if (oldValue.length() > 0) {
                ((JTextComponent)restAppPackage.getEditor().getEditorComponent()).setText(oldValue);
            }
        }
    }
    
    private String getPackage() {
        return ((JTextComponent)restAppPackage.getEditor().getEditorComponent()).getText();
    }
    
    private void setEnabledAppConfig(boolean enable){
        restAppClass.setEnabled(enable);
        restAppClassLbl.setEnabled(enable);
        restAppPackage.setEnabled(enable);
        restAppPckgLbl.setEnabled(enable);
    }
    
    private void fireChange() {
        ChangeEvent event =  new ChangeEvent(this);
        
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator1 = new javax.swing.JSeparator();
        useJersey = new javax.swing.JCheckBox();
        restAppPckgLbl = new javax.swing.JLabel();
        restAppPackage = new javax.swing.JComboBox();
        restAppClassLbl = new javax.swing.JLabel();
        restAppClass = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(450, 115));

        org.openide.awt.Mnemonics.setLocalizedText(useJersey, org.openide.util.NbBundle.getMessage(JaxRsConfigurationPanel.class, "LBL_UseJersey")); // NOI18N
        useJersey.setActionCommand(org.openide.util.NbBundle.getMessage(JaxRsConfigurationPanel.class, "JaxRsConfigurationPanel.useJersey.actionCommand")); // NOI18N

        restAppPckgLbl.setLabelFor(restAppPackage);
        org.openide.awt.Mnemonics.setLocalizedText(restAppPckgLbl, org.openide.util.NbBundle.getMessage(JaxRsConfigurationPanel.class, "LBL_AppConfigPackage")); // NOI18N

        restAppPackage.setEditable(true);

        restAppClassLbl.setLabelFor(restAppClass);
        org.openide.awt.Mnemonics.setLocalizedText(restAppClassLbl, org.openide.util.NbBundle.getMessage(JaxRsConfigurationPanel.class, "LBL_AppConfigClass")); // NOI18N

        restAppClass.setText(org.openide.util.NbBundle.getMessage(JaxRsConfigurationPanel.class, "JaxRsConfigurationPanel.restAppClass.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(useJersey)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(restAppPckgLbl)
                        .addGap(25, 25, 25)
                        .addComponent(restAppPackage, 0, 235, Short.MAX_VALUE)))
                .addGap(0, 10, 10))
            .addGroup(layout.createSequentialGroup()
                .addComponent(restAppClassLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(restAppClass, javax.swing.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useJersey)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(restAppPckgLbl)
                    .addComponent(restAppPackage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(restAppClassLbl)
                    .addComponent(restAppClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        useJersey.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JaxRsConfigurationPanel.class, "ACSN_UseJersey")); // NOI18N
        useJersey.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JaxRsConfigurationPanel.class, "ACSD_UseJersey")); // NOI18N
        restAppPckgLbl.getAccessibleContext().setAccessibleName(restAppPackage.getAccessibleContext().getAccessibleName());
        restAppPckgLbl.getAccessibleContext().setAccessibleDescription(restAppPackage.getAccessibleContext().getAccessibleDescription());
        restAppPackage.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JaxRsConfigurationPanel.class, "ACSN_AppConfigPackage")); // NOI18N
        restAppPackage.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JaxRsConfigurationPanel.class, "ACSD_AppConfigPackage")); // NOI18N
        restAppClassLbl.getAccessibleContext().setAccessibleName(restAppClass.getAccessibleContext().getAccessibleName());
        restAppClassLbl.getAccessibleContext().setAccessibleDescription(restAppClass.getAccessibleContext().getAccessibleDescription());
        restAppClass.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JaxRsConfigurationPanel.class, "ACSN_AppConfigClass")); // NOI18N
        restAppClass.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JaxRsConfigurationPanel.class, "ACSD_AppConfigClass")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField restAppClass;
    private javax.swing.JLabel restAppClassLbl;
    private javax.swing.JComboBox restAppPackage;
    private javax.swing.JLabel restAppPckgLbl;
    private javax.swing.JCheckBox useJersey;
    // End of variables declaration//GEN-END:variables
    
    private SourcePanel sourcePanel;
    private SourceGroup sourceGroup;
    private List<ChangeListener> listeners;

    private void updatePackageName() {
        String pkg = sourcePanel.getPackageName();
        if (parentPackageName == null || !parentPackageName.equals(pkg)) {
            parentPackageName = pkg;
            ((JTextComponent)restAppPackage.getEditor().getEditorComponent()).setText(pkg);
        }
    }
}
