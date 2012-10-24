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
package org.netbeans.modules.web.clientproject.ui.customizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.web.clientproject.ClientSideConfigurationProvider;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectConfigurationImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer;
import org.netbeans.modules.web.clientproject.ui.customizer.ClientSideProjectProperties.ProjectServer;
import org.netbeans.modules.web.common.api.WebServer;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author david
 */
public class RunPanel extends JPanel implements DocumentListener, ItemListener, HelpCtx.Provider {

    private static final long serialVersionUID = 98712411454L;

    private final ClientSideProject project;
    private final ComboBoxModel webServerModel;
    private final ProjectCustomizer.Category category;
    private final ClientSideProjectProperties uiProperties;


    public RunPanel(ProjectCustomizer.Category category, ClientSideProjectProperties uiProperties) {
        assert category != null;
        assert uiProperties != null;

        this.category = category;
        this.uiProperties = uiProperties;
        project = uiProperties.getProject();
        webServerModel = new DefaultComboBoxModel(ClientSideProjectProperties.ProjectServer.values());

        initComponents();
        init();
        initListeners();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.web.clientproject.ui.customizer.RunPanel"); // NOI18N
    }

    @Override
    public void addNotify() {
        super.addNotify();
        FileObject siteRoot = getSiteRoot();
        String info;
        if (siteRoot != null) {
            info = NbBundle.getMessage(RunPanel.class, "URL_DESCRIPTION", FileUtil.getFileDisplayName(siteRoot));
        } else {
            info = " "; // NOI18N
        }
        jProjectURLDescriptionLabel.setText(info);
        jFileToRunTextField.setEnabled(siteRoot != null);
        jBrowseButton.setEnabled(siteRoot != null);
        validateData();
    }

    private void init() {
        // config
        ClientSideConfigurationProvider configProvider = project.getProjectConfigurations();
        jConfigurationComboBox.setRenderer(new ConfigRenderer(jConfigurationComboBox.getRenderer()));
        jConfigurationComboBox.setModel(new DefaultComboBoxModel(configProvider.getConfigurations().toArray()));
        jConfigurationComboBox.setSelectedItem(uiProperties.getActiveConfiguration());
        updateConfigurationCustomizer();
        // start file
        jFileToRunTextField.setText(uiProperties.getStartFile());
        // server
        jServerComboBox.setModel(webServerModel);
        jServerComboBox.setRenderer(new ServerRenderer(jServerComboBox.getRenderer()));
        jServerComboBox.setSelectedItem(uiProperties.getProjectServer());
        //jServerComboBox.setSelectedIndex(cfg.isUseServer() ? 1 : 0); // XXX: indexes are obsolete, use enums directly
        // url
        jProjectURLTextField.setText(uiProperties.getProjectUrl());
        // web root
        jWebRootTextField.setText(uiProperties.getWebRoot());
        updateWebRootEnablement();
    }

    private void initListeners() {
        // config
        jConfigurationComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validateAndStore();
            }
        });
        // start file
        jFileToRunTextField.getDocument().addDocumentListener(this);
        // server
        jServerComboBox.addItemListener(this);
        // url
        jProjectURLTextField.getDocument().addDocumentListener(this);
        // web root
        jWebRootTextField.getDocument().addDocumentListener(this);
    }

    void validateAndStore() {
        validateData();
        storeData();
    }

    private void validateData() {
        // start file
        String error = validateStartFile();
        if (error != null) {
            category.setErrorMessage(error);
            category.setValid(false);
            return;
        }
        // project url
        error = validateProjectUrl();
        if (error != null) {
            category.setErrorMessage(error);
            category.setValid(false);
            return;
        }
        // all ok
        category.setErrorMessage(" "); // NOI18N
        category.setValid(true);
    }

    @NbBundle.Messages({
        "RunPanel.error.siteRoot.invalid=Invalid Site Root, fix it in Sources category.",
        "RunPanel.error.startFile.invalid=Start File must be a valid file.",
        "RunPanel.error.startFile.notUnderSiteRoot=Start File must be underneath Site Root directory."
    })
    private String validateStartFile() {
        FileObject siteRoot = getSiteRoot();
        if (siteRoot == null) {
            return Bundle.RunPanel_error_siteRoot_invalid();
        }
        File startFile = getResolvedStartFile();
        if (startFile == null || !startFile.isFile()) {
            return Bundle.RunPanel_error_startFile_invalid();
        }
        if (!FileUtil.isParentOf(siteRoot, FileUtil.toFileObject(startFile))) {
            return Bundle.RunPanel_error_startFile_notUnderSiteRoot();
        }
        return null;
    }

    @NbBundle.Messages({
        "RunPanel.error.projectUrl.missing=Project URL is missing.",
        "RunPanel.error.projectUrl.invalidProtocol=Project URL must start with http(s):// or file://.",
        "RunPanel.error.projectUrl.invalid=Project URL is invalid."
    })
    private String validateProjectUrl() {
        if (!jProjectURLTextField.isVisible()) {
            return null;
        }
        String projectUrl = getProjectUrl();
        if (projectUrl.isEmpty()) {
            return Bundle.RunPanel_error_projectUrl_missing();
        }
        if (!projectUrl.startsWith("http://") // NOI18N
                && !projectUrl.startsWith("https://") // NOI18N
                && !projectUrl.startsWith("file://")) { // NOI18N
            return Bundle.RunPanel_error_projectUrl_invalidProtocol();
        }
        try {
            URL url = new URL(projectUrl);
            String host = url.getHost();
            if (host == null || host.isEmpty()) {
                return Bundle.RunPanel_error_projectUrl_invalid();
            }
        } catch (MalformedURLException ex) {
            return Bundle.RunPanel_error_projectUrl_invalid();
        }
        return null;
    }

    private void storeData() {
        uiProperties.setActiveConfiguration(getActiveConfiguration());
        uiProperties.setStartFile(getStartFile());
        uiProperties.setProjectServer(getProjectServer());
        uiProperties.setProjectUrl(getProjectUrl());
        uiProperties.setWebRoot(getWebRoot());
    }

    private void updateConfigurationCustomizer() {
        jConfigurationPlaceholder.removeAll();
        ClientProjectConfigurationImplementation selectedConfiguration = getActiveConfiguration();
        if (selectedConfiguration != null) {
            ProjectConfigurationCustomizer customizerPanel = selectedConfiguration.getProjectConfigurationCustomizer();
            if (customizerPanel != null) {
                jConfigurationPlaceholder.add(customizerPanel.createPanel(), BorderLayout.CENTER);
            }
        }
        validate();
        repaint();
    }

    private ClientProjectConfigurationImplementation getActiveConfiguration() {
        return (ClientProjectConfigurationImplementation) jConfigurationComboBox.getSelectedItem();
    }

    @CheckForNull
    private FileObject getSiteRoot() {
        File siteRoot = uiProperties.getResolvedSiteRootFolder();
        if (siteRoot == null) {
            return null;
        }
        return FileUtil.toFileObject(siteRoot);
    }

    private String getStartFile() {
        return jFileToRunTextField.getText();
    }

    @CheckForNull
    private File getResolvedStartFile() {
        String startFile = getStartFile();
        if (startFile == null) {
            return null;
        }
        File directFile = new File(startFile);
        if (directFile.isAbsolute()) {
            return directFile;
        }
        FileObject siteRoot = getSiteRoot();
        if (siteRoot == null) {
            return null;
        }
        FileObject fo = siteRoot.getFileObject(startFile);
        if (fo == null) {
            return null;
        }
        return FileUtil.toFile(fo);
    }

    private ClientSideProjectProperties.ProjectServer getProjectServer() {
        return (ProjectServer) jServerComboBox.getSelectedItem();
    }

    private String getProjectUrl() {
        return jProjectURLTextField.getText();
    }

    private String getWebRoot() {
        return jWebRootTextField.getText();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jFileToRunTextField = new javax.swing.JTextField();
        jBrowseButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jServerComboBox = new javax.swing.JComboBox();
        jWebRootLabel = new javax.swing.JLabel();
        jWebRootTextField = new javax.swing.JTextField();
        jWebRootExampleLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jConfigurationComboBox = new javax.swing.JComboBox();
        jProjectURLLabel = new javax.swing.JLabel();
        jProjectURLTextField = new javax.swing.JTextField();
        jConfigurationPlaceholder = new javax.swing.JPanel();
        jProjectURLDescriptionLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jLabel1.text")); // NOI18N

        jFileToRunTextField.setText(org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jFileToRunTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jBrowseButton, org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jBrowseButton.text")); // NOI18N
        jBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBrowseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jWebRootLabel, org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jWebRootLabel.text")); // NOI18N

        jWebRootTextField.setText(org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jWebRootTextField.text")); // NOI18N

        jWebRootExampleLabel.setFont(jWebRootExampleLabel.getFont().deriveFont(jWebRootExampleLabel.getFont().getSize()-1f));
        org.openide.awt.Mnemonics.setLocalizedText(jWebRootExampleLabel, org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jWebRootExampleLabel.text")); // NOI18N
        jWebRootExampleLabel.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jLabel3.text")); // NOI18N

        jConfigurationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jConfigurationComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jProjectURLLabel, org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jProjectURLLabel.text")); // NOI18N

        jProjectURLTextField.setText(org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jProjectURLTextField.text")); // NOI18N

        jConfigurationPlaceholder.setLayout(new java.awt.BorderLayout());

        jProjectURLDescriptionLabel.setFont(jProjectURLDescriptionLabel.getFont().deriveFont(jProjectURLDescriptionLabel.getFont().getSize()-1f));
        org.openide.awt.Mnemonics.setLocalizedText(jProjectURLDescriptionLabel, org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jProjectURLDescriptionLabel.text")); // NOI18N
        jProjectURLDescriptionLabel.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jWebRootLabel)
                    .addComponent(jProjectURLLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jWebRootExampleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jFileToRunTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBrowseButton))
                    .addComponent(jConfigurationComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jServerComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jProjectURLTextField)
                    .addComponent(jWebRootTextField)
                    .addComponent(jConfigurationPlaceholder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jProjectURLDescriptionLabel)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jConfigurationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jConfigurationPlaceholder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jFileToRunTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBrowseButton))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jServerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jProjectURLLabel)
                    .addComponent(jProjectURLTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProjectURLDescriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jWebRootLabel)
                    .addComponent(jWebRootTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jWebRootExampleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 2, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages({
        "RunPanel.browse.startFile.title=Select Start File",
        "RunPanel.browse.startFile.filter.html=HTML Documents"
    })
    private void jBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBrowseButtonActionPerformed
        FileObject siteRootFolder = getSiteRoot();
        assert siteRootFolder != null;
        File workDir;
        File startFile = getResolvedStartFile();
        if (startFile != null && startFile.exists()) {
            workDir = startFile.getParentFile();
        } else {
            workDir = FileUtil.toFile(siteRootFolder);
        }
        File file = new FileChooserBuilder(SourcesPanel.class)
                .setTitle(Bundle.RunPanel_browse_startFile_title())
                .setFilesOnly(true)
                .setDefaultWorkingDirectory(workDir)
                .forceUseOfDefaultWorkingDirectory(true)
                .addFileFilter(new FileNameExtensionFilter(Bundle.RunPanel_browse_startFile_filter_html(), "html", "htm")) // NOI18N
                .showOpenDialog();
        if (file == null) {
            return;
        }
        String filePath = FileUtil.getRelativePath(siteRootFolder, FileUtil.toFileObject(file));
        if (filePath == null) {
            // path cannot be relativized
            filePath = file.getAbsolutePath();
        }
        jFileToRunTextField.setText(filePath);
    }//GEN-LAST:event_jBrowseButtonActionPerformed

    private void jConfigurationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jConfigurationComboBoxActionPerformed
        updateConfigurationCustomizer();
    }//GEN-LAST:event_jConfigurationComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBrowseButton;
    private javax.swing.JComboBox jConfigurationComboBox;
    private javax.swing.JPanel jConfigurationPlaceholder;
    private javax.swing.JTextField jFileToRunTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jProjectURLDescriptionLabel;
    private javax.swing.JLabel jProjectURLLabel;
    private javax.swing.JTextField jProjectURLTextField;
    private javax.swing.JComboBox jServerComboBox;
    private javax.swing.JLabel jWebRootExampleLabel;
    private javax.swing.JLabel jWebRootLabel;
    private javax.swing.JTextField jWebRootTextField;
    // End of variables declaration//GEN-END:variables

    private void updateWebRooExample() {
        if (!jWebRootTextField.isVisible()) {
            return;
        }
        if (!jWebRootTextField.isEnabled()) {
            jWebRootExampleLabel.setText(" "); //NOI18N
            return;
        }
        StringBuilder s = new StringBuilder();
        s.append(WebServer.getWebserver().getPort());
        String ctx = jWebRootTextField.getText();
        if (ctx.trim().length() == 0) {
            s.append("/"); //NOI18N
        } else {
            if (!ctx.startsWith("/")) { //NOI18N
                s.append("/"); //NOI18N
            }
            s.append(ctx);
        }
        jWebRootExampleLabel.setText(NbBundle.getMessage(RunPanel.class, "RunPanel.jWebRootExampleLabel.text", s.toString()));
    }

    private boolean isEmbeddedServer() {
        return jServerComboBox.getSelectedItem() == ClientSideProjectProperties.ProjectServer.INTERNAL;
    }

    private void updateWebRootEnablement() {
        jWebRootTextField.setVisible(isEmbeddedServer());
        jWebRootLabel.setVisible(isEmbeddedServer());
        jWebRootExampleLabel.setVisible(isEmbeddedServer());
        jProjectURLLabel.setVisible(!isEmbeddedServer());
        jProjectURLTextField.setVisible(!isEmbeddedServer());
        jProjectURLDescriptionLabel.setVisible(!isEmbeddedServer());
        updateWebRooExample();
        validateAndStore();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateWebRooExample();
        validateAndStore();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateWebRooExample();
        validateAndStore();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        updateWebRooExample();
        validateAndStore();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        updateWebRootEnablement();
    }

    //~ Inner classes

    private static final class ConfigRenderer implements ListCellRenderer {

        private final ListCellRenderer original;

        public ConfigRenderer(ListCellRenderer original) {
            this.original = original;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof ProjectConfiguration) {
                value = ((ProjectConfiguration) value).getDisplayName();
            }
            return original.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }

    }

    private static final class ServerRenderer implements ListCellRenderer {

        private final ListCellRenderer original;


        public ServerRenderer(ListCellRenderer original) {
            this.original = original;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            value = ((ClientSideProjectProperties.ProjectServer) value).getTitle();
            return original.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }

    }

}
