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
package org.netbeans.modules.cordova.platforms.android;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.platforms.Device;
import org.netbeans.modules.cordova.platforms.MobilePlatformsSetup;
import org.netbeans.modules.cordova.platforms.PlatformManager;
import org.netbeans.modules.cordova.platforms.PropertyProvider;
import org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Becicka
 */
public class AndroidConfigurationPanel extends javax.swing.JPanel {

    
    private PropertyProvider config;
    final RequestProcessor RP = new RequestProcessor(AndroidConfigurationPanel.class);

    private void initControls() {
        initComponents();
        String device = config.getProperty(Device.DEVICE_PROP); //NOI18N
        if (Device.DEVICE.equals(device)) { //NOI18N
            deviceCombo.setSelectedIndex(Device.DEVICE.equals(device)?1:0); //NOI18N
        }
        setAVDComboVisible(!Device.DEVICE.equals(device)); //NOI18N
        debuggerCheckBox.setVisible(false);
        deviceLabel.setVisible(false);
        deviceCombo.setVisible(false);
        
        String property = config.getProperty(Device.BROWSER_PROP);
        if (property!=null && property.equals(Browser.CHROME.getName())) {
            browserCombo.setSelectedIndex(1);
        }
        
        validate();
    }

    public static class AndroidConfigurationCustomizer implements ProjectConfigurationCustomizer {
        
        private final Project p;
        private final PropertyProvider config;

        public AndroidConfigurationCustomizer(Project p, PropertyProvider config) {
            this.p = p;
            this.config = config;
        }

        @Override
        public JPanel createPanel() {
            return new AndroidConfigurationPanel(config);
        }
    }
    
    private static class DeviceRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof AVD) {
                setText(((AVD) value).getName());
            }
            return this;
        }
    }
    
    /**
     * Creates new form AndroidConfigurationPanel
     */
    public AndroidConfigurationPanel(final PropertyProvider config) {
        assert config != null;
        this.config = config;
        if (!PlatformManager.getPlatform(PlatformManager.ANDROID_TYPE).isReady()) {
            setLayout(new BorderLayout());
            add(new MobilePlatformsSetup(), BorderLayout.CENTER);
            validate();
            PlatformManager.getPlatform(PlatformManager.ANDROID_TYPE).addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (PlatformManager.getPlatform(PlatformManager.ANDROID_TYPE).isReady()) {
                        removeAll();
                        initControls();
                        validate();
                    }
                }
            });
        } else {
            initControls();
        }
    }

    @NbBundle.Messages("LBL_PleaseWait=Please Wait...")
    private void setAVDComboVisible(boolean visible) {
        if (visible) {
            avdCombo.setModel(new DefaultComboBoxModel(new Object[]{Bundle.LBL_PleaseWait()}));
            avdCombo.setEnabled(false);
            RP.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Collection<? extends Device> avDs = PlatformManager.getPlatform(PlatformManager.ANDROID_TYPE).getVirtualDevices();
                        refreshCombo(avDs);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }        
        avdLabel.setVisible(visible);
        avdCombo.setVisible(visible);
        manageButton.setVisible(visible);
    }
    
    private void refreshCombo(final Collection<? extends Device> avDs) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                avdCombo.setEnabled(true);
                avdCombo.setRenderer(new DeviceRenderer());
                final AVD[] avds = (AVD[]) avDs.toArray(new AVD[avDs.size()]);
                avdCombo.setModel(new DefaultComboBoxModel(avds));
                for (AVD avd : avds) {
                    if (avd.getName().equals(config.getProperty(Device.DEVICE_PROP))) {//NOI18N
                        avdCombo.setSelectedItem(avd);
                        break;
                    }
                }

            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        deviceLabel = new javax.swing.JLabel();
        manageButton = new javax.swing.JButton();
        deviceCombo = new javax.swing.JComboBox();
        avdCombo = new javax.swing.JComboBox();
        avdLabel = new javax.swing.JLabel();
        debuggerCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        browserCombo = new javax.swing.JComboBox();

        deviceLabel.setLabelFor(deviceCombo);
        org.openide.awt.Mnemonics.setLocalizedText(deviceLabel, org.openide.util.NbBundle.getMessage(AndroidConfigurationPanel.class, "AndroidConfigurationPanel.deviceLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(manageButton, org.openide.util.NbBundle.getMessage(AndroidConfigurationPanel.class, "AndroidConfigurationPanel.manageButton.text")); // NOI18N
        manageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageButtonActionPerformed(evt);
            }
        });

        deviceCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Emulator", "Connected Device" }));
        deviceCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deviceComboActionPerformed(evt);
            }
        });

        avdCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Please Wait..." }));
        avdCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                avdComboActionPerformed(evt);
            }
        });

        avdLabel.setLabelFor(avdCombo);
        org.openide.awt.Mnemonics.setLocalizedText(avdLabel, org.openide.util.NbBundle.getMessage(AndroidConfigurationPanel.class, "AndroidConfigurationPanel.avdLabel.text")); // NOI18N

        debuggerCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(debuggerCheckBox, org.openide.util.NbBundle.getMessage(AndroidConfigurationPanel.class, "AndroidConfigurationPanel.debuggerCheckBox.text")); // NOI18N
        debuggerCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                debuggerCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AndroidConfigurationPanel.class, "AndroidConfigurationPanel.jLabel1.text")); // NOI18N

        browserCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default Browser", "Chrome" }));
        browserCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browserComboActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(0, 0, 0)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(debuggerCheckBox)
                        .add(0, 0, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel1)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(deviceLabel)
                                .add(avdLabel)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(deviceCombo, 0, 174, Short.MAX_VALUE)
                            .add(avdCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(browserCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(manageButton)))
                .add(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(0, 0, 0)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(deviceLabel)
                    .add(deviceCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(avdCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(manageButton)
                    .add(avdLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(browserCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(debuggerCheckBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void manageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageButtonActionPerformed
        manageButton.setEnabled(false);
        RP.post(new Runnable() {

            @Override
            public void run() {
                PlatformManager.getPlatform(PlatformManager.ANDROID_TYPE).manageDevices();
                Collection<? extends Device> avDs;
                try {
                    avDs = PlatformManager.getPlatform(PlatformManager.ANDROID_TYPE).getVirtualDevices();
                    refreshCombo(avDs);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        manageButton.setEnabled(true);
                    }
                });
            }
        });
    }//GEN-LAST:event_manageButtonActionPerformed

    private void avdComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_avdComboActionPerformed
        config.putProperty("avd", ((AVD)avdCombo.getSelectedItem()).getName()); //NOI18N
    }//GEN-LAST:event_avdComboActionPerformed

    private void deviceComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deviceComboActionPerformed
        if (deviceCombo.getSelectedIndex() == 0) {
            config.putProperty(Device.DEVICE_PROP, Device.EMULATOR); //NOI18N
            setAVDComboVisible(true);
        } else {
            config.putProperty(Device.DEVICE_PROP, Device.DEVICE); //NOI18N
            setAVDComboVisible(false);
        }
    }//GEN-LAST:event_deviceComboActionPerformed

    private void debuggerCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_debuggerCheckBoxActionPerformed
        config.putProperty("debug.enabled", Boolean.toString(debuggerCheckBox.isSelected())); //NOI18N
    }//GEN-LAST:event_debuggerCheckBoxActionPerformed

    private void browserComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browserComboActionPerformed
        config.putProperty(Device.BROWSER_PROP, browserCombo.getSelectedIndex() == 0 ? Browser.DEFAULT.getName():Browser.CHROME.getName());
    }//GEN-LAST:event_browserComboActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox avdCombo;
    private javax.swing.JLabel avdLabel;
    private javax.swing.JComboBox browserCombo;
    private javax.swing.JCheckBox debuggerCheckBox;
    private javax.swing.JComboBox deviceCombo;
    private javax.swing.JLabel deviceLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton manageButton;
    // End of variables declaration//GEN-END:variables
}
