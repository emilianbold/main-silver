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

package org.netbeans.modules.php.project.connections.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.UIResource;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.connections.ConfigManager.Configuration;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.RemoteConnections;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.connections.spi.RemoteConfigurationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 * @author Tomas Mysik
 */
public final class RemoteConnectionsPanel extends JPanel implements ChangeListener, HelpCtx.Provider {
    private static final long serialVersionUID = -286975118754121236L;

    private static final RequestProcessor TEST_CONNECTION_RP = new RequestProcessor("Test Remote Connection", 1); // NOI18N

    private final ConfigListModel configListModel = new ConfigListModel();
    private final RemoteConnections remoteConnections;
    private final ConfigManager configManager;
    private final Map<Configuration, RemoteConfigurationPanel> configPanels = new HashMap<Configuration, RemoteConfigurationPanel>();

    private RemoteConfigurationPanel configurationPanel = new EmptyConfigurationPanel();
    private DialogDescriptor descriptor = null;
    private NotificationLineSupport notificationLineSupport = null;
    private RequestProcessor.Task testConnectionTask = null;

    public RemoteConnectionsPanel(RemoteConnections remoteConnections, ConfigManager configManager) {
        this.remoteConnections = remoteConnections;
        this.configManager = configManager;

        initComponents();

        // init
        configList.setModel(configListModel);
        configList.setCellRenderer(new ConfigListRenderer());

        setEnabledRemoveButton();

        // listeners
        registerListeners();
    }

    public void setConfigurations(List<Configuration> configurations) {
        configListModel.setElements(configurations);
    }

    public boolean open(final RemoteConfiguration remoteConfiguration) {
        testConnectionTask = TEST_CONNECTION_RP.create(new Runnable() {
            @Override
            public void run() {
                testConnection();
            }
        }, true);
        descriptor = new DialogDescriptor(
                this,
                NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_ManageRemoteConnections"),
                true,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.OK_OPTION,
                null);
        notificationLineSupport = descriptor.createNotificationLineSupport();
        testConnectionTask.addTaskListener(new TaskListener() {
            @Override
            public void taskFinished(Task task) {
                enableTestConnection();
            }
        });
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        try {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (getConfigurations().isEmpty()) {
                        // no config available => show add config dialog
                        addConfig();
                    } else {
                        // this would need to implement hashCode() and equals() for RemoteConfiguration.... hmm, probably not needed
                        //assert getConfigurations().contains(remoteConfiguration) : "Unknow remote configration: " + remoteConfiguration;
                        if (remoteConfiguration != null) {
                            // select config
                            selectConfiguration(remoteConfiguration.getName());
                        } else {
                            // select the first one
                            selectConfiguration(0);
                        }
                    }
                }
            });
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
        }
        return descriptor.getValue() == NotifyDescriptor.OK_OPTION;
    }

    private void registerListeners() {
        configList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                setEnabledRemoveButton();
                selectCurrentConfig();
            }
        });
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addConfig();
            }
        });
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeConfig();
            }
        });
    }

    void testConnection() {
        testConnectionButton.setEnabled(false);

        Configuration selectedConfiguration = getSelectedConfiguration();
        assert selectedConfiguration != null;
        RemoteConfiguration remoteConfiguration = remoteConnections.getRemoteConfiguration(selectedConfiguration);
        assert remoteConfiguration != null : "Cannot find remote configuration for config manager configuration " + selectedConfiguration.getName();

        String configName = selectedConfiguration.getDisplayName();
        String progressTitle = NbBundle.getMessage(RemoteConnectionsPanel.class, "MSG_TestingConnection", configName);
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(progressTitle);
        RemoteClient client = new RemoteClient(remoteConfiguration);
        RemoteException exception = null;
        try {
            progressHandle.start();
            client.connect();
        } catch (RemoteException ex) {
            exception = ex;
        } finally {
            try {
                client.disconnect();
            } catch (RemoteException ex) {
                // ignored
            }
            progressHandle.finish();
        }

        // notify user
        String msg = null;
        int msgType = 0;
        if (exception != null) {
            if (exception.getRemoteServerAnswer() != null) {
                msg = NbBundle.getMessage(RemoteConnectionsPanel.class, "MSG_TestConnectionFailedServerAnswer", exception.getMessage(), exception.getRemoteServerAnswer());
            } else if (exception.getCause() != null) {
                msg = NbBundle.getMessage(RemoteConnectionsPanel.class, "MSG_TestConnectionFailedCause", exception.getMessage(), exception.getCause().getMessage());
            } else {
                msg = exception.getMessage();
            }
            msgType = NotifyDescriptor.ERROR_MESSAGE;
        } else {
            msg = NbBundle.getMessage(RemoteConnectionsPanel.class, "MSG_TestConnectionSucceeded");
            msgType = NotifyDescriptor.INFORMATION_MESSAGE;
        }
        DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                    msg,
                    configName,
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    msgType,
                    new Object[] {NotifyDescriptor.OK_OPTION},
                    NotifyDescriptor.OK_OPTION));
    }

    void enableTestConnection() {
        assert testConnectionTask != null;

        Configuration cfg = getSelectedConfiguration();
        testConnectionButton.setEnabled(testConnectionTask.isFinished() && cfg != null && cfg.isValid());
    }

    private void addConfiguration(ConfigManager.Configuration configuration) {
        addConfiguration(configuration, true);
    }

    private void addConfiguration(ConfigManager.Configuration configuration, boolean select) {
        assert configListModel.indexOf(configuration) == -1 : "Configuration already in the list: " + configuration;
        configListModel.addElement(configuration);
        if (select) {
            configList.setSelectedValue(configuration, true);
            switchConfigurationPanel();
            descriptor.setValid(false);
        }
    }

    private void selectConfiguration(int index) {
        configList.setSelectedIndex(index);
        switchConfigurationPanel();
    }

    private void selectConfiguration(String configName) {
        configList.setSelectedValue(configListModel.getElement(configName), true);
        switchConfigurationPanel();
    }

    private void readActiveConfig(Configuration cfg) {
        configurationPanel.read(cfg);
    }

    private void storeActiveConfig(Configuration cfg) {
        configurationPanel.store(cfg);
    }

    private void switchConfigurationPanel() {
        configurationPanel.removeChangeListener(this);

        String name = null;
        String type = null;
        Configuration configuration = (Configuration) configList.getSelectedValue();
        if (configuration != null) {
            type = remoteConnections.getConfigurationType(configuration);
            name = configuration.getDisplayName();

            configurationPanel = getConfigurationPanel(configuration);
            assert configurationPanel != null : "Panel must be provided for configuration " + configuration.getName();
            readActiveConfig(configuration);
            configManager.markAsCurrentConfiguration(configuration.getName());
        } else {
            configurationPanel = new EmptyConfigurationPanel();
        }

        configurationPanel.addChangeListener(this);

        resetFields();

        if (configuration != null) {
            assert name != null : "Name must be found for config " + configuration.getDisplayName();
            assert type != null : "Type must be found for config " + configuration.getDisplayName();

            nameTextField.setText(NbBundle.getMessage(RemoteConnectionsPanel.class, "TXT_NameType", name, type));
        }
        Component innerPanel = configurationPanel.getComponent();
        configurationPanelHolder.setPreferredSize(innerPanel.getPreferredSize());
        configurationPanelHolder.add(innerPanel, BorderLayout.CENTER);
        configurationPanelHolder.revalidate();
        configurationPanelHolder.repaint();
    }

    private RemoteConfigurationPanel getConfigurationPanel(Configuration configuration) {
        RemoteConfigurationPanel panel = configPanels.get(configuration);
        if (panel == null) {
            panel = remoteConnections.getConfigurationPanel(configuration);
            configPanels.put(configuration, panel);
        }
        return panel;
    }

    private ConfigManager.Configuration getSelectedConfiguration() {
        return (Configuration) configList.getSelectedValue();
    }

    private List<Configuration> getConfigurations() {
        return configListModel.getElements();
    }

    private void removeConfiguration(ConfigManager.Configuration configuration) {
        assert configListModel.indexOf(configuration) != -1 : "Configuration not in the list: " + configuration;
        // select another config if possible
        int toSelect = -1;
        int idx = configListModel.indexOf(configuration);
        if (idx + 1 < configListModel.getSize()) {
            // select the next element
            toSelect = idx;
        } else if (configListModel.getSize() > 1) {
            // select the previous element
            toSelect = idx - 1;
        }
        configListModel.removeElement(configuration);
        if (toSelect != -1) {
            configList.setSelectedIndex(toSelect);
            switchConfigurationPanel();
        }
    }

    private void resetFields() {
        nameTextField.setText(" "); // NOI18N

        configurationPanelHolder.removeAll();
        configurationPanelHolder.revalidate();
        configurationPanelHolder.repaint();
        setError(null);
        setWarning(null);
    }

    private boolean isValidConfiguration() {
        return configurationPanel.isValidConfiguration();
    }

    private String getError() {
        return configurationPanel.getError();
    }

    private String getWarning() {
        return configurationPanel.getWarning();
    }

    private void setError(String msg) {
        assert descriptor != null;
        assert notificationLineSupport != null;

        if (StringUtils.hasText(msg)) {
            notificationLineSupport.setErrorMessage(msg);
            descriptor.setValid(false);
        } else {
            notificationLineSupport.clearMessages();
            descriptor.setValid(true);
        }

        enableTestConnection();
    }

    private void setWarning(String msg) {
        assert descriptor != null;
        assert notificationLineSupport != null;

        if (StringUtils.hasText(msg)) {
            notificationLineSupport.setWarningMessage(msg);
        }
    }

    private void refreshConfigList() {
        configList.repaint();
    }

    void setEnabledRemoveButton() {
        setEnabledRemoveButton(configList.getSelectedIndex() != -1);
    }

    private void setEnabledRemoveButton(boolean enabled) {
        removeButton.setEnabled(enabled);
    }

    void validateActiveConfig() {
        boolean valid = isValidConfiguration();
        String error = getError();
        Configuration cfg = getSelectedConfiguration();
        cfg.setErrorMessage(error);
        setError(error);

        if (!valid) {
            return;
        }

        setWarning(getWarning());

        // check whether all the configs are errorless
        checkAllConfigs();
    }

    private void checkAllConfigs() {
        for (Configuration cfg : getConfigurations()) {
            assert cfg != null;
            if (!cfg.isValid()) {
                setError(NbBundle.getMessage(RemoteConnectionsPanel.class, "MSG_InvalidConfiguration", cfg.getDisplayName()));
                assert descriptor != null;
                descriptor.setValid(false);
                return;
            }
        }
    }

    void addConfig() {
        NewRemoteConnectionPanel panel = new NewRemoteConnectionPanel(configManager);
        if (panel.open()) {
            String config = panel.getConfigName();
            String name = panel.getConnectionName();
            String type = panel.getConnectionType();
            assert config != null;
            assert name != null;
            assert type != null;

            Configuration cfg = configManager.createNew(config, name);
            RemoteConfiguration remoteConfiguration = remoteConnections.createRemoteConfiguration(type, cfg);
            assert remoteConfiguration != null : "No remote configuration created for type: " + type;
            addConfiguration(cfg);
            configManager.markAsCurrentConfiguration(config);
        }
    }

    void removeConfig() {
        Configuration cfg = getSelectedConfiguration();
        assert cfg != null;
        configManager.configurationFor(cfg.getName()).delete();
        removeConfiguration(cfg); // this will change the current selection in the list => selectCurrentConfig() is called
    }

    void selectCurrentConfig() {
        Configuration cfg = getSelectedConfiguration();
        if (cfg != null) {
            switchConfigurationPanel();
            // validate fields only if there's valid config
            validateActiveConfig();
        } else {
            resetFields();
            checkAllConfigs();
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

        configScrollPane = new javax.swing.JScrollPane();
        configList = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        separator = new javax.swing.JSeparator();
        configurationPanelScrollPane = new javax.swing.JScrollPane();
        configurationPanelHolder = new javax.swing.JPanel();
        testConnectionButton = new javax.swing.JButton();

        configList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        configScrollPane.setViewportView(configList);
        configList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.configList.AccessibleContext.accessibleName")); // NOI18N
        configList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.configList.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_Add")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_Remove")); // NOI18N

        nameLabel.setLabelFor(nameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_Name")); // NOI18N

        nameTextField.setEditable(false);

        configurationPanelScrollPane.setBorder(null);

        configurationPanelHolder.setLayout(new java.awt.BorderLayout());
        configurationPanelScrollPane.setViewportView(configurationPanelHolder);
        configurationPanelHolder.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.detailsPanel.AccessibleContext.accessibleName")); // NOI18N
        configurationPanelHolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.detailsPanel.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(testConnectionButton, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_TestConnection")); // NOI18N
        testConnectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton))
                    .addComponent(configScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(separator, javax.swing.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE))
                    .addComponent(testConnectionButton)
                    .addComponent(configurationPanelScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addButton, removeButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(configScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nameLabel)
                            .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(configurationPanelScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(removeButton)
                    .addComponent(testConnectionButton))
                .addContainerGap())
        );

        configScrollPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.configScrollPane.AccessibleContext.accessibleName")); // NOI18N
        configScrollPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.configScrollPane.AccessibleContext.accessibleDescription")); // NOI18N
        addButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.addButton.AccessibleContext.accessibleName")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.addButton.AccessibleContext.accessibleDescription")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.removeButton.AccessibleContext.accessibleName")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.removeButton.AccessibleContext.accessibleDescription")); // NOI18N
        nameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.nameLabel.AccessibleContext.accessibleName")); // NOI18N
        nameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.nameLabel.AccessibleContext.accessibleDescription")); // NOI18N
        nameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.nameTextField.AccessibleContext.accessibleName")); // NOI18N
        nameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.nameTextField.AccessibleContext.accessibleDescription")); // NOI18N
        separator.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.separator.AccessibleContext.accessibleName")); // NOI18N
        separator.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.separator.AccessibleContext.accessibleDescription")); // NOI18N
        configurationPanelScrollPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.configurationPanelScrollPane.AccessibleContext.accessibleName")); // NOI18N
        configurationPanelScrollPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.configurationPanelScrollPane.AccessibleContext.accessibleDescription")); // NOI18N
        testConnectionButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.testConnectionButton.AccessibleContext.accessibleName")); // NOI18N
        testConnectionButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.testConnectionButton.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void testConnectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionButtonActionPerformed
        testConnectionTask.schedule(0);
    }//GEN-LAST:event_testConnectionButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JList configList;
    private javax.swing.JScrollPane configScrollPane;
    private javax.swing.JPanel configurationPanelHolder;
    private javax.swing.JScrollPane configurationPanelScrollPane;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton removeButton;
    private javax.swing.JSeparator separator;
    private javax.swing.JButton testConnectionButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public void stateChanged(ChangeEvent e) {
        Configuration cfg = getSelectedConfiguration();
        if (cfg != null) {
            // no config selected
            validateActiveConfig();
            storeActiveConfig(cfg);
        }

        // because of correct coloring of list items (invalid configurations)
        refreshConfigList();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(RemoteConnectionsPanel.class);
    }

    public static class ConfigListRenderer extends JLabel implements ListCellRenderer, UIResource {
        private static final long serialVersionUID = 3196531352192214602L;

        public ConfigListRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setName("ComboBox.listRenderer"); // NOI18N
            Color errorColor = UIManager.getColor("nb.errorForeground"); // NOI18N
            boolean cfgValid = true;
            if (value != null) {
                assert value instanceof ConfigManager.Configuration;
                ConfigManager.Configuration cfg = (ConfigManager.Configuration) value;
                setText(cfg.getDisplayName());
                cfgValid = cfg.isValid();
            }
            setIcon(null);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(cfgValid ? list.getSelectionForeground() : errorColor);
            } else {
                setBackground(list.getBackground());
                setForeground(cfgValid ? list.getForeground() : errorColor);
            }
            return this;
        }

        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name; // NOI18N
        }
    }

    public class ConfigListModel extends AbstractListModel {
        private static final long serialVersionUID = -1945188556310432557L;

        private final List<Configuration> data = new ArrayList<Configuration>();

        @Override
        public int getSize() {
            return data.size();
        }

        @Override
        public Configuration getElementAt(int index) {
            return data.get(index);
        }

        public boolean addElement(Configuration configuration) {
            assert configuration != null;
            if (!data.add(configuration)) {
                return false;
            }
            Collections.sort(data, ConfigManager.getConfigurationComparator());
            int idx = indexOf(configuration);
            fireIntervalAdded(this, idx, idx);
            return true;
        }

        public int indexOf(Configuration configuration) {
            return data.indexOf(configuration);
        }

        public boolean removeElement(Configuration configuration) {
            int idx = indexOf(configuration);
            if (idx == -1) {
                return false;
            }
            boolean result = data.remove(configuration);
            assert result;
            fireIntervalRemoved(this, idx, idx);
            return true;
        }

        public List<Configuration> getElements() {
            return Collections.unmodifiableList(data);
        }

        public void setElements(List<Configuration> configurations) {
            int size = data.size();
            data.clear();
            if (size > 0) {
                fireIntervalRemoved(this, 0, size - 1);
            }
            if (configurations.size() > 0) {
                data.addAll(configurations);
                Collections.sort(data, ConfigManager.getConfigurationComparator());
                fireIntervalAdded(this, 0, data.size() - 1);
            }
        }

        public Configuration getElement(String configName) {
            assert configName != null;
            for (Configuration configuration : data) {
                if (configName.equals(configuration.getName())) {
                    return configuration;
                }
            }
            return null;
        }
    }

    private static final class EmptyConfigurationPanel implements RemoteConfigurationPanel {
        private static final JPanel PANEL = new JPanel();

        @Override
        public void addChangeListener(ChangeListener listener) {
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
        }

        @Override
        public JComponent getComponent() {
            return PANEL;
        }

        @Override
        public boolean isValidConfiguration() {
            return true;
        }

        @Override
        public String getError() {
            return null;
        }

        @Override
        public String getWarning() {
            return null;
        }

        @Override
        public void read(Configuration configuration) {
        }

        @Override
        public void store(Configuration configuration) {
        }
    }
}
