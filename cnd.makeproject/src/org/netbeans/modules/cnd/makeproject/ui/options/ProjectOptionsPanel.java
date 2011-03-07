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
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject.ui.options;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;

/**
 * Replaces the old project system options panel.
 */
public class ProjectOptionsPanel extends JPanel {

    private boolean changed;
    private boolean listen = false;
    private ArrayList<PropertyChangeListener> propertyChangeListeners = new ArrayList<PropertyChangeListener>();
    private DocumentListener documentListener;

    /** Creates new form ProjectOptionsPanel */
    public ProjectOptionsPanel() {
        initComponents();
        // Accessible Description
        reuseCheckBox.getAccessibleContext().setAccessibleDescription(getString("REUSE_CHECKBOX_AD"));
        saveCheckBox.getAccessibleContext().setAccessibleDescription(getString("SAVE_CHECKBOX_AD"));
        dependencyCheckingCheckBox.getAccessibleContext().setAccessibleDescription(getString("DEPENDENCY_CHECKBOX_AD"));
        rebuildPropsChangedCheckBox.getAccessibleContext().setAccessibleDescription(getString("REBUILD_PROP_CHANGED_AD"));
//        platformComboBox.getAccessibleContext().setAccessibleDescription(getString("DEFAULT_PLATFORM_AD"));
        filePathcomboBox.getAccessibleContext().setAccessibleDescription(getString("FILE_PATH_AD"));
        makeOptionsTextField.getAccessibleContext().setAccessibleDescription(getString("MAKE_OPTIONS_AD"));
        filePathTxt.getAccessibleContext().setAccessibleDescription(getString("FILE_PATH_TXT_AD"));
        filePathTxt.getAccessibleContext().setAccessibleName(getString("FILE_PATH_TXT_AN"));


        documentListener = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                validateFields();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateFields();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validateFields();
            }
        };

        makeOptionsTextField.getDocument().addDocumentListener(documentListener);
        setName("TAB_ProjectsTab"); // NOI18N (used as a pattern...)

        if ("Windows".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
            setOpaque(false);
        } else {
            Color c = getBackground();
            Color cc = new Color(c.getRed(), c.getGreen(), c.getBlue());
            filePathTxt.setBackground(cc);
        }
    }

    public void update() {
        listen = false;
        MakeOptions makeOptions = MakeOptions.getInstance();
        dependencyCheckingCheckBox.setSelected(makeOptions.getDepencyChecking());
        rebuildPropsChangedCheckBox.setSelected(makeOptions.getRebuildPropChanged());
        makeOptionsTextField.setText(makeOptions.getMakeOptions());
        filePathcomboBox.removeAllItems();
        for (MakeProjectOptions.PathMode pathMode : MakeProjectOptions.PathMode.values()) {
            filePathcomboBox.addItem(pathMode);
        }
        filePathcomboBox.setSelectedItem(makeOptions.getPathMode());
        saveCheckBox.setSelected(makeOptions.getSave());
        reuseCheckBox.setSelected(makeOptions.getReuse());
        viewBinaryFilesCheckBox.setSelected(makeOptions.getViewBinaryFiles());
        showProfilerCheckBox.setSelected(makeOptions.getShowProfiling());
        showConfigurationWarningCheckBox.setSelected(makeOptions.getShowConfigurationWarning());
        fullFileIndexer.setSelected(makeOptions.isFullFileIndexer());
        fixUnresolvedInclude.setSelected(makeOptions.isFixUnresolvedInclude());
        useBuildTrace.setSelected(makeOptions.isUseBuildTrace());

        listen = true;
        changed = false;
    }

    /** Apply changes */
    public void applyChanges() {
        MakeOptions makeOptions = MakeOptions.getInstance();

        makeOptions.setDepencyChecking(dependencyCheckingCheckBox.isSelected());
        makeOptions.setRebuildPropChanged(rebuildPropsChangedCheckBox.isSelected());
        makeOptions.setMakeOptions(makeOptionsTextField.getText());
        makeOptions.setPathMode((MakeProjectOptions.PathMode) filePathcomboBox.getSelectedItem());
        makeOptions.setSave(saveCheckBox.isSelected());
        makeOptions.setReuse(reuseCheckBox.isSelected());
        makeOptions.setViewBinaryFiles(viewBinaryFilesCheckBox.isSelected());
        makeOptions.setShowProfiling(showProfilerCheckBox.isSelected());
        makeOptions.setShowConfigurationWarning(showConfigurationWarningCheckBox.isSelected());
        makeOptions.setFullFileIndexer(fullFileIndexer.isSelected());
        makeOptions.setFixUnresolvedInclude(fixUnresolvedInclude.isSelected());
        makeOptions.setUseBuildTrace(useBuildTrace.isSelected());

        changed = false;
    }

    /** What to do if user cancels the dialog (nothing) */
    public void cancel() {
        changed = false;
    }

    /**
     * Lets NB know if the data in the panel is valid and OK should be enabled
     * 
     * @return Returns true if all data is valid
     */
    public boolean dataValid() {
        return true;
    }

    /**
     * Lets caller know if any data has been changed.
     * 
     * @return True if anything has been changed
     */
    public boolean isChanged() {
        return changed;
    }

    private void validateFields() {
        PropertyChangeEvent pce = new PropertyChangeEvent(this, OptionsPanelController.PROP_VALID, this, this);
        firePropertyChange(pce);
    }

    public void firePropertyChange(PropertyChangeEvent evt) {
        ArrayList<PropertyChangeListener> newList = new ArrayList<PropertyChangeListener>();
        newList.addAll(propertyChangeListeners);
        for (PropertyChangeListener listener : newList) {
            listener.propertyChange(evt);
        }
    }

    private static String getString(String key) {
        return NbBundle.getMessage(ProjectOptionsPanel.class, key);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        makeOptionsLabel = new javax.swing.JLabel();
        makeOptionsTextField = new javax.swing.JTextField();
        makeOptionsTxt = new javax.swing.JLabel();
        filePathLabel = new javax.swing.JLabel();
        filePathcomboBox = new javax.swing.JComboBox();
        filePathTxt = new javax.swing.JTextArea();
        saveCheckBox = new javax.swing.JCheckBox();
        reuseCheckBox = new javax.swing.JCheckBox();
        dependencyCheckingCheckBox = new javax.swing.JCheckBox();
        viewBinaryFilesCheckBox = new javax.swing.JCheckBox();
        showProfilerCheckBox = new javax.swing.JCheckBox();
        showConfigurationWarningCheckBox = new javax.swing.JCheckBox();
        fullFileIndexer = new javax.swing.JCheckBox();
        fixUnresolvedInclude = new javax.swing.JCheckBox();
        rebuildPropsChangedCheckBox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        useBuildTrace = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        makeOptionsLabel.setLabelFor(makeOptionsTextField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(makeOptionsLabel, bundle.getString("MAKE_OPTIONS")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(makeOptionsLabel, gridBagConstraints);

        makeOptionsTextField.setColumns(45);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 6);
        add(makeOptionsTextField, gridBagConstraints);

        makeOptionsTxt.setText(bundle.getString("MAKE_OPTIONS_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 0, 12);
        add(makeOptionsTxt, gridBagConstraints);

        filePathLabel.setLabelFor(filePathcomboBox);
        org.openide.awt.Mnemonics.setLocalizedText(filePathLabel, bundle.getString("FILE_PATH")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(filePathLabel, gridBagConstraints);

        filePathcomboBox.setMinimumSize(new java.awt.Dimension(75, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 4, 0, 6);
        add(filePathcomboBox, gridBagConstraints);

        filePathTxt.setEditable(false);
        filePathTxt.setLineWrap(true);
        filePathTxt.setText(bundle.getString("FILE_PATH_MODE_TXT")); // NOI18N
        filePathTxt.setWrapStyleWord(true);
        filePathTxt.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 10.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(filePathTxt, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(saveCheckBox, org.openide.util.NbBundle.getMessage(ProjectOptionsPanel.class, "SAVE_CHECKBOX_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 6, 6);
        add(saveCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(reuseCheckBox, bundle.getString("REUSE_CHECKBOX_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(reuseCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(dependencyCheckingCheckBox, bundle.getString("DEPENDENCY_CHECKING_TXT")); // NOI18N
        dependencyCheckingCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dependencyCheckingCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(dependencyCheckingCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(viewBinaryFilesCheckBox, org.openide.util.NbBundle.getMessage(ProjectOptionsPanel.class, "DISPLAY_BINARY_FILES_TXT")); // NOI18N
        viewBinaryFilesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewBinaryFilesCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(viewBinaryFilesCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(showProfilerCheckBox, org.openide.util.NbBundle.getMessage(ProjectOptionsPanel.class, "SHOW_PROFILER_CHECKBOX_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(showProfilerCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(showConfigurationWarningCheckBox, org.openide.util.NbBundle.getMessage(ProjectOptionsPanel.class, "SHOW_WARNING_ABOUT_MISMATCHED_CONFIGURATIONS.TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(showConfigurationWarningCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(fullFileIndexer, org.openide.util.NbBundle.getMessage(ProjectOptionsPanel.class, "FullFileIndexerName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(fullFileIndexer, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(fixUnresolvedInclude, org.openide.util.NbBundle.getMessage(ProjectOptionsPanel.class, "fixUnresolvedInclude")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(fixUnresolvedInclude, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(rebuildPropsChangedCheckBox, org.openide.util.NbBundle.getMessage(ProjectOptionsPanel.class, "REBUILD_PROP_CHANGED_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(rebuildPropsChangedCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jSeparator1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(useBuildTrace, org.openide.util.NbBundle.getMessage(ProjectOptionsPanel.class, "BuildTraceAvaliable")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(useBuildTrace, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void dependencyCheckingCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dependencyCheckingCheckBoxActionPerformed
// TODO add your handling code here:

        PropertyChangeEvent pce = new PropertyChangeEvent(this, OptionsPanelController.PROP_VALID, this, this);
        firePropertyChange(pce);
//        pce = new PropertyChangeEvent(this, "buran" + OptionsPanelController.PROP_VALID, this, this);
//        firePropertyChange(pce);
    }//GEN-LAST:event_dependencyCheckingCheckBoxActionPerformed

    private void viewBinaryFilesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewBinaryFilesCheckBoxActionPerformed
        PropertyChangeEvent pce = new PropertyChangeEvent(this, OptionsPanelController.PROP_VALID, this, this);
        firePropertyChange(pce);
    }//GEN-LAST:event_viewBinaryFilesCheckBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox dependencyCheckingCheckBox;
    private javax.swing.JLabel filePathLabel;
    private javax.swing.JTextArea filePathTxt;
    private javax.swing.JComboBox filePathcomboBox;
    private javax.swing.JCheckBox fixUnresolvedInclude;
    private javax.swing.JCheckBox fullFileIndexer;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel makeOptionsLabel;
    private javax.swing.JTextField makeOptionsTextField;
    private javax.swing.JLabel makeOptionsTxt;
    private javax.swing.JCheckBox rebuildPropsChangedCheckBox;
    private javax.swing.JCheckBox reuseCheckBox;
    private javax.swing.JCheckBox saveCheckBox;
    private javax.swing.JCheckBox showConfigurationWarningCheckBox;
    private javax.swing.JCheckBox showProfilerCheckBox;
    private javax.swing.JCheckBox useBuildTrace;
    private javax.swing.JCheckBox viewBinaryFilesCheckBox;
    // End of variables declaration//GEN-END:variables
}
