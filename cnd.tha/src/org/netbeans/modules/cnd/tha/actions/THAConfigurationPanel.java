/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

/*
 * THAConfigurationPanel.java
 *
 * Created on Aug 25, 2009, 3:24:56 PM
 */
package org.netbeans.modules.cnd.tha.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.cnd.tha.support.THAConfigurationImpl;
import org.netbeans.modules.dlight.perfan.tha.api.THAConfiguration;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author mt154047
 */
final class THAConfigurationPanel extends javax.swing.JPanel {

    /** Creates new form THAConfigurationPanel */
    THAConfigurationPanel() {
        initComponents();
        // combo box contents set here to allow NOI18N
        startCollectingComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
            NbBundle.getMessage(THAConfigurationPanel.class, "THAConfigurationPanel.startCollectingComboBox.startup"),
            NbBundle.getMessage(THAConfigurationPanel.class, "THAConfigurationPanel.startCollectingComboBox.manually")})); // NOI18N
        startCollectingComboBox.setSelectedIndex(0);
        collectComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
            NbBundle.getMessage(THAConfigurationPanel.class, "THAConfigurationPanel.collectComboBox.deadlocks"),
            NbBundle.getMessage(THAConfigurationPanel.class, "THAConfigurationPanel.collectComboBox.races")})); // NOI18N
        collectComboBox.setSelectedIndex(0);
        startCollectingComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                NbPreferences.forModule(THAConfigurationPanel.this.getClass()).putInt("StartMode", startCollectingComboBox.getSelectedIndex());//NOI18N
            }
        });
        startCollectingComboBox.setSelectedIndex(NbPreferences.forModule(getClass()).getInt("StartMode", 0)); // NOI18N
        collectComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                NbPreferences.forModule(THAConfigurationPanel.this.getClass()).putInt("CollectedData", collectComboBox.getSelectedIndex());//NOI18N
                setProgressBarValue();
            }
        });
        collectComboBox.setSelectedIndex(NbPreferences.forModule(getClass()).getInt("CollectedData", 0)); // NOI18N
        overheadProgressBar.setMinimum(0);
        overheadProgressBar.setMaximum(10);
        setProgressBarValue();

    }


    private void setProgressBarValue(){
        int comboBoxIndex = collectComboBox.getSelectedIndex();
        overheadProgressBar.setValue(comboBoxIndex == 0 ? 7 : 10);
        overheadProgressBar.setString(comboBoxIndex == 0 ?
            NbBundle.getMessage(THAConfigurationPanel.class, "THAConfigurationPanel.overheadProgressBar.medium") :
            NbBundle.getMessage(THAConfigurationPanel.class, "THAConfigurationPanel.overheadProgressBar.high"));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        collectComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        startCollectingComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        overheadProgressBar = new javax.swing.JProgressBar();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(THAConfigurationPanel.class, "THAConfigurationPanel.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(THAConfigurationPanel.class, "THAConfigurationPanel.jLabel2.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(THAConfigurationPanel.class, "THAConfigurationPanel.jLabel3.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(jLabel1)
                    .add(jLabel3))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(overheadProgressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 129, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(startCollectingComboBox, 0, 190, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(collectComboBox, 0, 190, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(collectComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(startCollectingComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(32, 32, 32)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(jLabel3, 0, 0, Short.MAX_VALUE)
                    .add(overheadProgressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox collectComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JProgressBar overheadProgressBar;
    private javax.swing.JComboBox startCollectingComboBox;
    // End of variables declaration//GEN-END:variables

    THAConfiguration getTHAConfiguration() {
        return THAConfigurationImpl.create(startCollectingComboBox.getSelectedIndex() == 0, collectComboBox.getSelectedIndex() != 0);
    }
}
