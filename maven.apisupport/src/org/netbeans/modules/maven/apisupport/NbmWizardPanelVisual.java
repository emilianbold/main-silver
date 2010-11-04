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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.apisupport;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.maven.api.MavenValidators;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationListener;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 */
public class NbmWizardPanelVisual extends javax.swing.JPanel {

    private static final String SEARCHING = NbBundle.getMessage(NbmWizardPanelVisual.class, "NbmWizardPanelVisual.wait");
    private final NbmWizardPanel panel;
    private ValidationGroup vg = ValidationGroup.create();
    boolean isApp = false;
    boolean isSuite = false;

    /** Creates new form NbmWizardPanelVisual */
    public NbmWizardPanelVisual(NbmWizardPanel panel) {
        this.panel = panel;
        initComponents();
        isApp = NbmWizardIterator.NB_APP_ARCH.equals(panel.getArchetype());
        isSuite = NbmWizardIterator.NB_SUITE_ARCH.equals(panel.getArchetype());
        if (isApp || isSuite) {
            vg.add(txtAddModule, Validators.merge(true,
                    MavenValidators.createArtifactIdValidators(),
                    Validators.REQUIRE_VALID_FILENAME
                    ));
            txtAddModule.putClientProperty(ValidationListener.CLIENT_PROP_NAME, "NetBeans Module ArtifactId");
        } else {
            cbAddModule.setVisible(false);
            txtAddModule.setVisible(false);
        }
        final RepositoryInfo info = RepositoryPreferences.getInstance().getRepositoryInfoById("netbeans"); // NOI18N
        if (info != null) {
            versionCombo.setModel(new DefaultComboBoxModel(new Object[] {SEARCHING}));
            RequestProcessor.getDefault().post(new Runnable() {
                public @Override void run() {
                    final List<String> versions = new ArrayList<String>();
                    for (NBVersionInfo version : RepositoryQueries.getVersions("org.netbeans.cluster", "platform", info)) { // NOI18N
                        versions.add(version.getVersion());
                    }
                    EventQueue.invokeLater(new Runnable()  {
                        public @Override void run() {
                            versionCombo.setModel(new DefaultComboBoxModel(versions.toArray()));
                        }
                    });
                }
            });
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbOsgiDeps = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        txtAddModule = new javax.swing.JTextField();
        cbAddModule = new javax.swing.JCheckBox();
        versionLabel = new javax.swing.JLabel();
        versionCombo = new javax.swing.JComboBox();

        org.openide.awt.Mnemonics.setLocalizedText(cbOsgiDeps, org.openide.util.NbBundle.getMessage(NbmWizardPanelVisual.class, "NbmWizardPanelVisual.cbOsgiDeps.text")); // NOI18N
        cbOsgiDeps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbOsgiDepsActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(NbmWizardPanelVisual.class, "NbmWizardPanelVisual.jLabel1.text")); // NOI18N

        txtAddModule.setEnabled(false);

        cbAddModule.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cbAddModule, org.openide.util.NbBundle.getMessage(NbmWizardPanelVisual.class, "NbmWizardPanelVisual.cbAddModule.text")); // NOI18N
        cbAddModule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbAddModuleActionPerformed(evt);
            }
        });

        versionLabel.setLabelFor(versionCombo);
        org.openide.awt.Mnemonics.setLocalizedText(versionLabel, NbBundle.getMessage(NbmWizardPanelVisual.class, "NbmWizardPanelVisual.versionLabel.text")); // NOI18N

        versionCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "RELEASE123" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jLabel1))
                    .addComponent(cbOsgiDeps)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cbAddModule)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtAddModule, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(versionLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(versionCombo, 0, 391, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbOsgiDeps)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtAddModule, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbAddModule))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(versionLabel)
                    .addComponent(versionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(163, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbOsgiDepsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbOsgiDepsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbOsgiDepsActionPerformed

    private void cbAddModuleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbAddModuleActionPerformed
        // TODO add your handling code here:
        txtAddModule.setEnabled(cbAddModule.isSelected());
        vg.validateAll();
}//GEN-LAST:event_cbAddModuleActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbAddModule;
    private javax.swing.JCheckBox cbOsgiDeps;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField txtAddModule;
    private javax.swing.JComboBox versionCombo;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables


     void store(WizardDescriptor d) {
        d.putProperty(NbmWizardIterator.OSGIDEPENDENCIES, Boolean.valueOf(cbOsgiDeps.isSelected()));
         File parent = (File) d.getProperty("projdir");
         if (isApp || isSuite) {
             if (cbAddModule.isSelected()) {
                 d.putProperty(NbmWizardIterator.NBM_ARTIFACTID, txtAddModule.getText().trim());
             } else {
                 d.putProperty(NbmWizardIterator.NBM_ARTIFACTID, null);
             }
         }
         String version = (String) versionCombo.getSelectedItem();
         if (!version.equals(SEARCHING)) {
             d.putProperty(NbmWizardIterator.NB_VERSION, version);
         }
         if (isApp || isSuite) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    panel.getValidationGroup().removeValidationGroup(vg);
                }
            });
         }
    }

    void read(WizardDescriptor d) {
        Boolean b = (Boolean) d.getProperty(NbmWizardIterator.OSGIDEPENDENCIES);
        if (b != null) {
            cbOsgiDeps.setSelected(b.booleanValue());
        }
        if (isApp || isSuite) {
            String artifId = (String) d.getProperty("artifactId");
            String val = (String) d.getProperty(NbmWizardIterator.NBM_ARTIFACTID);
            cbAddModule.setSelected(val != null);
            if (val == null) {
                val = artifId + "-sample";
            }
            txtAddModule.setText(val);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    panel.getValidationGroup().addValidationGroup(vg, true);
                }
            });
        }
        String version = (String) d.getProperty(NbmWizardIterator.NB_VERSION);
        if (version != null) {
            versionCombo.setSelectedItem(version);
        }
    }
}
