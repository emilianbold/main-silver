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
 * ToolsManagerPanel.java
 *
 * Created on Aug 8, 2009, 1:20:21 PM
 */
package org.netbeans.modules.dlight.toolsui.api;

import org.netbeans.modules.dlight.toolsui.*;
import java.awt.Dialog;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.dlight.api.tool.DLightConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightConfigurationManager;
import org.netbeans.modules.dlight.api.tool.DLightTool;
import org.netbeans.modules.dlight.api.tool.impl.DLightConfigurationManagerAccessor;
import org.netbeans.modules.dlight.api.tool.impl.DLightConfigurationSupport;
import org.netbeans.modules.dlight.toolsui.DLightConfigurationUIWrapper;
import org.netbeans.modules.dlight.toolsui.DLightConfigurationUIWrapperProvider;
import org.netbeans.modules.dlight.toolsui.DLightToolUIWrapper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author thp
 */
public class ToolsManagerPanel extends javax.swing.JPanel {

    private List<DLightConfigurationUIWrapper> dLightConfigurations = null;
    private List<DLightTool> allDLightTools = null;
    private static String manageConfigurations = getString("ManageConfiurations");
    private int lastSelectedIndex = 0;
    private ToolsTable toolsTable = null;

    /** Creates new form ToolsManagerPanel */
    public ToolsManagerPanel() {
//        initComponents();
//        DLightConfigurationManagerAccessor accessor = DLightConfigurationManagerAccessor.getDefault();
//        DLightConfigurationManager manager = DLightConfigurationManager.getInstance();
//        allDLightTools = accessor.getDefaultConfiguration(manager).getToolsSet();
//        initDialog(DLightConfigurationUIWrapperProvider.getInstance().getDLightConfigurationUIWrappers(), null);
//        setPreferredSize(new Dimension(700, 400));
        this(null);
    }

    public ToolsManagerPanel(String preferredConfigurationName) {
        initComponents();
        DLightConfigurationManagerAccessor accessor = DLightConfigurationManagerAccessor.getDefault();
        DLightConfigurationManager manager = DLightConfigurationManager.getInstance();
        allDLightTools = accessor.getDefaultConfiguration(manager).getToolsSet();
        initDialog(DLightConfigurationUIWrapperProvider.getInstance().getDLightConfigurationUIWrappers(), preferredConfigurationName);
        setPreferredSize(new Dimension(700, 400));
    }

    private void initDialog(List<DLightConfigurationUIWrapper> list, String preferredConfigurationName) {
        // profile configuration combobox
        profileConfigurationComboBox.removeAllItems();
        dLightConfigurations = list;
        DLightConfigurationUIWrapper preferredConfiguration = null;
        for (DLightConfigurationUIWrapper dlightConfigurationWrapper : dLightConfigurations) {
            profileConfigurationComboBox.addItem(dlightConfigurationWrapper);
            if (preferredConfiguration != null && dlightConfigurationWrapper.getDisplayName().equals(preferredConfigurationName)) {
                preferredConfiguration = dlightConfigurationWrapper;
            }
        }
        profileConfigurationComboBox.addItem(manageConfigurations);
        if (preferredConfiguration != null) {
            profileConfigurationComboBox.setSelectedItem(preferredConfiguration);
        }
        else {
            profileConfigurationComboBox.setSelectedIndex(0);
        }
    }

    private void initConfigurationPanel(DLightConfigurationUIWrapper dlightConfigurationUIWrapper) {
        DLightConfiguration gizmoConfiguration = dlightConfigurationUIWrapper.getDLightConfiguration();
        assert gizmoConfiguration != null;
//        defaultDataProviderComboBox.removeAllItems();
//        defaultDataProviderComboBox.addItem("SunStudio"); // NOI18N
//        defaultDataProviderComboBox.addItem("DTrace"); // NOI18N
        // FIXUP: should be moved to tool
//        String dataProvider;
//        if (gizmoConfiguration.getDisplayedName().indexOf("DTrace") >= 0) {
//            dataProvider = "DTrace"; // NOI18N
//        }
//        else if (gizmoConfiguration.getDisplayedName().indexOf("Studio") >= 0) {
//            dataProvider = "Sun Studio"; // NOI18N
//        }
//        else {
//            dataProvider = "Simple (indicators only)"; // NOI18N
//        }
        //dataProviderLabel2.setText(dataProvider);
        toolsTable = new ToolsTable(dlightConfigurationUIWrapper, dlightConfigurationUIWrapper.getTools(), new MySelectionListener());
        toolsList.setViewportView(toolsTable);
        toolsTable.initSelection();//getSelectionModel().setSelectionInterval(0, 0);
        toolsLabel.setLabelFor(toolsTable);
    }

    private DLightConfigurationUIWrapper inList(String name, List<DLightConfigurationUIWrapper> list) {
        for (DLightConfigurationUIWrapper wrapper : list) {
            if (name.equals(wrapper.getName())) {
                return wrapper;
            }
        }
        return null;
    }

    public boolean apply() {
        // Delete deleted configurations
        List<DLightConfigurationUIWrapper> oldDLightConfiguration = DLightConfigurationUIWrapperProvider.getInstance().getDLightConfigurationUIWrappers();
        List<DLightConfigurationUIWrapper> toBeDeleted = new ArrayList<DLightConfigurationUIWrapper>();
        for (DLightConfigurationUIWrapper wrapper : oldDLightConfiguration) {
            DLightConfigurationUIWrapper c = inList(wrapper.getName(), dLightConfigurations);
            if (c == null || c.getCopyOf() != null) {
                toBeDeleted.add(wrapper);
            }
        }
        for (DLightConfigurationUIWrapper conf : toBeDeleted) {
            DLightConfigurationSupport.getInstance().removeConfiguration(conf.getName());
        }
        
        // Rename renamed configurations
        for (DLightConfigurationUIWrapper configuration : dLightConfigurations) {
            if (configuration.isCustom() && configuration.getCopyOf() == null) {
                if (!configuration.getName().equals(configuration.getDLightConfiguration().getConfigurationName())) {
                    DLightConfiguration origDLightConfiguration = configuration.getDLightConfiguration();
                    String category = origDLightConfiguration.getCategoryName();
                    List<String> platforms = origDLightConfiguration.getPlatforms();
                    String collector = origDLightConfiguration.getCollectorProviders();
                    List<String> indicators = origDLightConfiguration.getIndicatorProviders();
                    DLightConfigurationSupport.getInstance().removeConfiguration(origDLightConfiguration.getConfigurationName());
                    DLightConfiguration dlightConfiguration = DLightConfigurationSupport.getInstance().registerConfiguration(configuration.getName(), configuration.getDisplayName(), category, platforms, collector, indicators);
                    configuration.setDLightConfiguration(dlightConfiguration);
                    for (DLightToolUIWrapper toolUI : configuration.getTools()) {
                        toolUI.setModified(true);
                    }
                }
            }
        }

        // create duplicates
        for (DLightConfigurationUIWrapper configuration : dLightConfigurations) {
            if (configuration.isCustom() && configuration.getCopyOf() != null) {
                DLightConfiguration origDLightConfiguration = configuration.getCopyOf();
                String category = origDLightConfiguration.getCategoryName();
                List<String> platforms = origDLightConfiguration.getPlatforms();
                String collector = origDLightConfiguration.getCollectorProviders();
                List<String> indicators = origDLightConfiguration.getIndicatorProviders();
                DLightConfiguration dlightConfiguration = DLightConfigurationSupport.getInstance().registerConfiguration(configuration.getName(), configuration.getDisplayName(), category, platforms, collector, indicators);
                configuration.setDLightConfiguration(dlightConfiguration);
                for (DLightToolUIWrapper toolUI : configuration.getTools()) {
                    toolUI.setModified(true);
                }
                configuration.setCopyOf(null);
            }
        }

        // save all changes to tools
        for (DLightConfigurationUIWrapper configuration : dLightConfigurations) {
            for (DLightToolUIWrapper toolUI : configuration.getTools()) {
                if (toolUI.isModified()) {
                    //if it was disabled and now enabled: should register
                    if (!toolUI.isEnabled()){
                        DLightConfigurationSupport.getInstance().deleteTool(configuration.getName(), toolUI.getDLightTool());
                    }else{
                        DLightConfigurationSupport.getInstance().registerTool(configuration.getName(), toolUI.getDLightTool().getID(), true);
                    }
                }
            }
        }

        // save wrappers
        DLightConfigurationUIWrapperProvider.getInstance().setDLightConfigurationUIWrappers(dLightConfigurations);
        return true;
    }

    private DLightToolUIWrapper getSelectedDLightToolWrapper() {
        int row = toolsTable.getSelectedRow();
        DLightConfigurationUIWrapper dLightConfigurationWrapper = (DLightConfigurationUIWrapper) profileConfigurationComboBox.getSelectedItem();
        DLightToolUIWrapper tool = dLightConfigurationWrapper.getTools().get(row);
        return tool;
    }

    class MySelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            DLightToolUIWrapper tool = getSelectedDLightToolWrapper();
//            toolNameLabelField.setText(tool.getDLightTool().getName());
//            onByDefaultCheckBox.setSelected(tool.isOnByDefault());
            detailsLabel.setText(tool.getDLightTool().getDetailedName());
        }
    }

    private static String getString(String key, String... params) {
        return NbBundle.getMessage(ToolsManagerPanel.class, key, params);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        profileConfigurationLabel = new javax.swing.JLabel();
        profileConfigurationComboBox = new javax.swing.JComboBox();
        toolsPanel = new javax.swing.JPanel();
        toolsList = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        toolsLabel = new javax.swing.JLabel();
        toolPropertyPanel = new javax.swing.JPanel();
        detailsLabel = new javax.swing.JLabel();
        fillLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        profileConfigurationLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/dlight/toolsui/api/Bundle").getString("ProfilerConfiguration_MN").charAt(0));
        profileConfigurationLabel.setLabelFor(profileConfigurationComboBox);
        profileConfigurationLabel.setText(org.openide.util.NbBundle.getMessage(ToolsManagerPanel.class, "ToolsManagerPanel.profileConfigurationLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(profileConfigurationLabel, gridBagConstraints);

        profileConfigurationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileConfigurationComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 4, 0, 12);
        add(profileConfigurationComboBox, gridBagConstraints);

        toolsPanel.setLayout(new java.awt.GridBagLayout());

        toolsList.setViewportView(jList1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        toolsPanel.add(toolsList, gridBagConstraints);

        toolsLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/dlight/toolsui/api/Bundle").getString("TOOLS_MN").charAt(0));
        toolsLabel.setText(org.openide.util.NbBundle.getMessage(ToolsManagerPanel.class, "ToolsManagerPanel.toolsLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        toolsPanel.add(toolsLabel, gridBagConstraints);

        toolPropertyPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        toolPropertyPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        toolPropertyPanel.add(detailsLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 12);
        toolsPanel.add(toolPropertyPanel, gridBagConstraints);

        fillLabel.setText(org.openide.util.NbBundle.getMessage(ToolsManagerPanel.class, "ToolsManagerPanel.fillLabel.text")); // NOI18N
        fillLabel.setMaximumSize(new java.awt.Dimension(300, 5));
        fillLabel.setMinimumSize(new java.awt.Dimension(300, 5));
        fillLabel.setPreferredSize(new java.awt.Dimension(300, 5));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        toolsPanel.add(fillLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(toolsPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void profileConfigurationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileConfigurationComboBoxActionPerformed
        Object item = profileConfigurationComboBox.getSelectedItem();
        if (item instanceof String && ((String) item).equals(manageConfigurations)) {
            MyListEditorPanel listEditorPanel = new MyListEditorPanel(dLightConfigurations);

            DialogDescriptor descriptor = new DialogDescriptor(listEditorPanel, getString("TXT_ToolsCustomizer"));
            Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            try {
                dlg.setVisible(true);
                if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                    List<DLightConfigurationUIWrapper> newList = listEditorPanel.getListData();
                    List<DLightConfigurationUIWrapper> oldList = DLightConfigurationUIWrapperProvider.getInstance().getDLightConfigurationUIWrappers();
                    oldList.clear();
                    oldList.addAll(newList);
                    initDialog(newList, null);
                } else {
                    profileConfigurationComboBox.setSelectedIndex(lastSelectedIndex);
                }
            } finally {
                dlg.dispose();
            }

        } else if (item instanceof DLightConfigurationUIWrapper) {
            DLightConfigurationUIWrapper dlightConfigurationUIWrapper = (DLightConfigurationUIWrapper) item;
            initConfigurationPanel(dlightConfigurationUIWrapper);
        } else {
            assert false;
        }
        lastSelectedIndex = profileConfigurationComboBox.getSelectedIndex();
    }//GEN-LAST:event_profileConfigurationComboBoxActionPerformed

    class MyListEditorPanel extends ListEditorPanel<DLightConfigurationUIWrapper> {

        public MyListEditorPanel(List<DLightConfigurationUIWrapper> list) {
            super(list);
            setPreferredSize(new Dimension(400, 300));
            getAddButton().setVisible(false);
        }

//        @Override
//        public DLightConfigurationUIWrapper addAction() {
//            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EDIT_DIALOG_LABEL_TXT"), getString("EDIT_DIALOG_TITLE_TXT"));
//            notifyDescriptor.setInputText(getString("NewConfigurationName"));
//            DialogDisplayer.getDefault().notify(notifyDescriptor);
//            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
//                return null;
//            }
//            String newS = notifyDescriptor.getInputText();
//            return new DLightConfigurationUIWrapper(newS, newS, allDLightTools);
//        }

        private String makeNameUnique(String suggestedName) {
            if (findDLightConfigurationUIWrapper(suggestedName) == null) {
                return suggestedName;
            }
            else {
                for (int i = 1;; i++) {
                    String newName = suggestedName + "_" + i; // NOI18N
                    if (findDLightConfigurationUIWrapper(newName) == null) {
                        return newName;
                    }
                }
            }
        }

        private String makeNameLegal(String suggestedName) {
            String newName = suggestedName;
            newName = newName.replace("/", "_FSLASH_"); // NOI18N
            newName = newName.replace("\\", "_BSLASH_"); // NOI18N
            newName = newName.replace(".", "_DOT_"); // NOI18N
            return newName;
        }

        private DLightConfigurationUIWrapper findDLightConfigurationUIWrapper(String name) {
            for (DLightConfigurationUIWrapper dlightConfigurationUIWrapper : getListData()) {
                if (dlightConfigurationUIWrapper.getDisplayName().equals(name)) {
                    return dlightConfigurationUIWrapper;
                }
            }
            return null;
        }

        @Override
        public DLightConfigurationUIWrapper copyAction(DLightConfigurationUIWrapper o) {
            String newDisplayName = makeNameUnique(getString("CopyOf", o.getDisplayName()));
            String newName = makeNameLegal(newDisplayName);
            DLightConfigurationUIWrapper copy = new DLightConfigurationUIWrapper(newName, newDisplayName, allDLightTools); // NOI18N
            List<DLightToolUIWrapper> tools = o.getTools();
            List<DLightToolUIWrapper> copyTools = copy.getTools();
            int i = 0;
            for (DLightToolUIWrapper tool : tools) {
                DLightToolUIWrapper copyTool = copyTools.get(i++);
                copyTool.setEnabled(tool.isEnabled());
                copyTool.setCanEnable(tool.canEnable());
            }
            copy.setCopyOf(o.getDLightConfiguration());
            return copy;
        }

        @Override
        public void editAction(DLightConfigurationUIWrapper o) {
            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EDIT_DIALOG_LABEL_TXT"), getString("EDIT_DIALOG_TITLE_TXT"));
            notifyDescriptor.setInputText(o.getDisplayName());
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
                return;
            }
            String newDisplayName = notifyDescriptor.getInputText();
            if (newDisplayName.length() == 0) {
                newDisplayName = o.getDisplayName();
            }
            else {
                newDisplayName = makeNameUnique(newDisplayName);
            }
            o.setDisplayName(newDisplayName);
            String newName = makeNameLegal(newDisplayName);
            o.setName(newName);
        }

        @Override
        protected void checkSelection(int i) {
            super.checkSelection(i);
            DLightConfigurationUIWrapper dLightConfigurationWrapper = getListData().elementAt(i);
            getEditButton().setEnabled(dLightConfigurationWrapper.isCustom());
            getRemoveButton().setEnabled(dLightConfigurationWrapper.isCustom());
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel detailsLabel;
    private javax.swing.JLabel fillLabel;
    private javax.swing.JList jList1;
    private javax.swing.JComboBox profileConfigurationComboBox;
    private javax.swing.JLabel profileConfigurationLabel;
    private javax.swing.JPanel toolPropertyPanel;
    private javax.swing.JLabel toolsLabel;
    private javax.swing.JScrollPane toolsList;
    private javax.swing.JPanel toolsPanel;
    // End of variables declaration//GEN-END:variables
    }
