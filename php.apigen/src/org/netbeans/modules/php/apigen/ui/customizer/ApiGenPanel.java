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

package org.netbeans.modules.php.apigen.ui.customizer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.apigen.ApiGenProvider;
import org.netbeans.modules.php.apigen.commands.ApiGenScript;
import org.netbeans.modules.php.apigen.ui.ApiGenPreferences;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

final class ApiGenPanel extends JPanel implements HelpCtx.Provider {

    private static final long serialVersionUID = -54768321324347L;

    private static final String SEPARATOR = ","; // NOI18N

    private final Category category;
    private final PhpModule phpModule;


    ApiGenPanel(Category category, PhpModule phpModule) {
        assert category != null;
        assert phpModule != null;

        this.category = category;
        this.phpModule = phpModule;

        this.category.setStoreListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                storeData();
            }
        });

        initComponents();
        init();
    }

    @NbBundle.Messages("ApiGenPanel.info.csv=Comma (\",\") separated values.")
    private void init() {
        // info
        charsetsInfoLabel.setText(Bundle.ApiGenPanel_info_csv());
        excludesInfoLabel.setText(Bundle.ApiGenPanel_info_csv());

        // values
        targetTextField.setText(ApiGenPreferences.getTarget(phpModule, false));
        titleTextField.setText(ApiGenPreferences.get(phpModule, ApiGenPreferences.TITLE));
        configTextField.setText(ApiGenPreferences.get(phpModule, ApiGenPreferences.CONFIG));
        charsetsTextField.setText(StringUtils.implode(ApiGenPreferences.getMore(phpModule, ApiGenPreferences.CHARSETS), SEPARATOR));
        excludesTextField.setText(StringUtils.implode(ApiGenPreferences.getMore(phpModule, ApiGenPreferences.EXCLUDES), SEPARATOR));
        Set<String> accessLevels = new HashSet<String>(ApiGenPreferences.getMore(phpModule, ApiGenPreferences.ACCESS_LEVELS));
        accessLevelPublicCheckBox.setSelected(accessLevels.contains(ApiGenScript.ACCESS_LEVEL_PUBLIC));
        accessLevelProtectedCheckBox.setSelected(accessLevels.contains(ApiGenScript.ACCESS_LEVEL_PROTECTED));
        accessLevelPrivateCheckBox.setSelected(accessLevels.contains(ApiGenScript.ACCESS_LEVEL_PRIVATE));
        internalCheckBox.setSelected(ApiGenPreferences.getBoolean(phpModule, ApiGenPreferences.INTERNAL));
        phpCheckBox.setSelected(ApiGenPreferences.getBoolean(phpModule, ApiGenPreferences.PHP));
        treeCheckBox.setSelected(ApiGenPreferences.getBoolean(phpModule, ApiGenPreferences.TREE));
        deprecatedCheckBox.setSelected(ApiGenPreferences.getBoolean(phpModule, ApiGenPreferences.DEPRECATED));
        todoCheckBox.setSelected(ApiGenPreferences.getBoolean(phpModule, ApiGenPreferences.TODO));
        downloadCheckBox.setSelected(ApiGenPreferences.getBoolean(phpModule, ApiGenPreferences.DOWNLOAD));
        sourceCodeCheckBox.setSelected(ApiGenPreferences.getBoolean(phpModule, ApiGenPreferences.SOURCE_CODE));

        // listeners
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        ActionListener defaultActionListener = new DefaultActionListener();
        targetTextField.getDocument().addDocumentListener(defaultDocumentListener);
        titleTextField.getDocument().addDocumentListener(defaultDocumentListener);
        configTextField.getDocument().addDocumentListener(defaultDocumentListener);
        charsetsTextField.getDocument().addDocumentListener(defaultDocumentListener);
        excludesTextField.getDocument().addDocumentListener(defaultDocumentListener);
        accessLevelPublicCheckBox.addActionListener(defaultActionListener);
        accessLevelProtectedCheckBox.addActionListener(defaultActionListener);
        accessLevelPrivateCheckBox.addActionListener(defaultActionListener);

        // validate
        validateData();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.apigen.ui.customizer.ApiGen"); // NOI18N
    }

    private String getTarget() {
        return targetTextField.getText().trim();
    }

    private String getTitle() {
        return titleTextField.getText().trim();
    }

    private String getConfig() {
        return configTextField.getText().trim();
    }

    private List<String> getCharsets() {
        String charsets = charsetsTextField.getText().trim();
        if (StringUtils.hasText(charsets)) {
            return StringUtils.explode(charsets, SEPARATOR);
        }
        return Collections.emptyList();
    }

    private List<String> getExcludes() {
        String excludes = excludesTextField.getText().trim();
        if (StringUtils.hasText(excludes)) {
            return StringUtils.explode(excludes, SEPARATOR);
        }
        return Collections.emptyList();
    }

    private List<String> getAccessLevels() {
        List<String> levels = new ArrayList<String>(3);
        if (accessLevelPublicCheckBox.isSelected()) {
            levels.add(ApiGenScript.ACCESS_LEVEL_PUBLIC);
        }
        if (accessLevelProtectedCheckBox.isSelected()) {
            levels.add(ApiGenScript.ACCESS_LEVEL_PROTECTED);
        }
        if (accessLevelPrivateCheckBox.isSelected()) {
            levels.add(ApiGenScript.ACCESS_LEVEL_PRIVATE);
        }
        return levels;
    }

    private boolean getInternal() {
        return internalCheckBox.isSelected();
    }

    private boolean getPhp() {
        return phpCheckBox.isSelected();
    }

    private boolean getTree() {
        return treeCheckBox.isSelected();
    }

    private boolean getDeprecated() {
        return deprecatedCheckBox.isSelected();
    }

    private boolean getTodo() {
        return todoCheckBox.isSelected();
    }

    private boolean getDownload() {
        return downloadCheckBox.isSelected();
    }

    private boolean getSourceCode() {
        return sourceCodeCheckBox.isSelected();
    }

    @NbBundle.Messages({
        "ApiGenPanel.error.relativeTarget=Absolute path for target directory must be provided.",
        "ApiGenPanel.error.invalidTitle=Title must be provided.",
        "ApiGenPanel.error.invalidCharsets=Charsets must be provided.",
        "ApiGenPanel.error.invalidAccessLevels=Access levels must be provided.",
        "ApiGenPanel.warn.nbWillAskForDir=NetBeans will ask for the directory before generating documentation.",
        "ApiGenPanel.warn.targetDirWillBeCreated=Target directory will be created.",
        "# {0} - encoding",
        "ApiGenPanel.warn.missingCharset=Project encoding ''{0}'' nout found within specified charsets.",
        "ApiGenPanel.warn.configNotNeon=Neon file is expected for configuration."
    })
    void validateData() {
        // errors
        // target
        String target = getTarget();
        if (StringUtils.hasText(target)) {
            File targetDir = new File(target);
            if (targetDir.exists()) {
                String error = FileUtils.validateDirectory(target, true);
                if (error != null) {
                    category.setErrorMessage(error);
                    category.setValid(false);
                    return;
                }
            } else {
                if (!targetDir.isAbsolute()) {
                    category.setErrorMessage(Bundle.ApiGenPanel_error_relativeTarget());
                    category.setValid(false);
                    return;
                }
            }
        }
        // title
        if (!StringUtils.hasText(getTitle())) {
            category.setErrorMessage(Bundle.ApiGenPanel_error_invalidTitle());
            category.setValid(false);
            return;
        }
        // config
        String config = getConfig();
        if (StringUtils.hasText(config)) {
            String error = FileUtils.validateFile(config, false);
            if (error != null) {
                category.setErrorMessage(error);
                category.setValid(false);
                return;
            }
        }
        // charsets
        if (getCharsets().isEmpty()) {
            category.setErrorMessage(Bundle.ApiGenPanel_error_invalidCharsets());
            category.setValid(false);
            return;
        }
        // access levels
        if (!accessLevelPublicCheckBox.isSelected()
                && !accessLevelProtectedCheckBox.isSelected()
                && !accessLevelPrivateCheckBox.isSelected()) {
            category.setErrorMessage(Bundle.ApiGenPanel_error_invalidAccessLevels());
            category.setValid(false);
            return;
        }

        // warnings
        // charsets
        String defaultCharset = ApiGenPreferences.CHARSETS.getDefaultValue(phpModule);
        if (getCharsets().indexOf(defaultCharset) == -1) {
            category.setErrorMessage(Bundle.ApiGenPanel_warn_missingCharset(defaultCharset));
            category.setValid(true);
            return;
        }
        // target
        if (!StringUtils.hasText(target)) {
            category.setErrorMessage(Bundle.ApiGenPanel_warn_nbWillAskForDir());
            category.setValid(true);
            return;
        }
        if (!new File(target).exists()) {
            category.setErrorMessage(Bundle.ApiGenPanel_warn_targetDirWillBeCreated());
            category.setValid(true);
            return;
        }
        // config
        if (StringUtils.hasText(config)) {
            File configFile = new File(config);
            if (!configFile.getName().endsWith(".neon")) { // NOI18N
                category.setErrorMessage(Bundle.ApiGenPanel_warn_configNotNeon());
                category.setValid(true);
                return;
            }
        }

        // everything ok
        category.setErrorMessage(null);
        category.setValid(true);
    }

    void storeData() {
        ApiGenPreferences.putTarget(phpModule, getTarget());
        ApiGenPreferences.put(phpModule, ApiGenPreferences.TITLE, getTitle());
        ApiGenPreferences.put(phpModule, ApiGenPreferences.CONFIG, getConfig());
        ApiGenPreferences.putMore(phpModule, ApiGenPreferences.CHARSETS, getCharsets());
        ApiGenPreferences.putMore(phpModule, ApiGenPreferences.EXCLUDES, getExcludes());
        ApiGenPreferences.putMore(phpModule, ApiGenPreferences.ACCESS_LEVELS, getAccessLevels());
        ApiGenPreferences.putBoolean(phpModule, ApiGenPreferences.INTERNAL, getInternal());
        ApiGenPreferences.putBoolean(phpModule, ApiGenPreferences.PHP, getPhp());
        ApiGenPreferences.putBoolean(phpModule, ApiGenPreferences.TREE, getTree());
        ApiGenPreferences.putBoolean(phpModule, ApiGenPreferences.DEPRECATED, getDeprecated());
        ApiGenPreferences.putBoolean(phpModule, ApiGenPreferences.TODO, getTodo());
        ApiGenPreferences.putBoolean(phpModule, ApiGenPreferences.DOWNLOAD, getDownload());
        ApiGenPreferences.putBoolean(phpModule, ApiGenPreferences.SOURCE_CODE, getSourceCode());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        targetLabel = new JLabel();
        targetTextField = new JTextField();
        targetButton = new JButton();
        titleLabel = new JLabel();
        titleTextField = new JTextField();
        configLabel = new JLabel();
        configTextField = new JTextField();
        configButton = new JButton();
        charsetsLabel = new JLabel();
        charsetsTextField = new JTextField();
        charsetsInfoLabel = new JLabel();
        excludesLabel = new JLabel();
        excludesTextField = new JTextField();
        excludesInfoLabel = new JLabel();
        accessLevelLabel = new JLabel();
        accessLevelPublicCheckBox = new JCheckBox();
        accessLevelProtectedCheckBox = new JCheckBox();
        accessLevelPrivateCheckBox = new JCheckBox();
        internalCheckBox = new JCheckBox();
        phpCheckBox = new JCheckBox();
        treeCheckBox = new JCheckBox();
        deprecatedCheckBox = new JCheckBox();
        todoCheckBox = new JCheckBox();
        downloadCheckBox = new JCheckBox();
        sourceCodeCheckBox = new JCheckBox();

        targetLabel.setLabelFor(targetTextField);
        Mnemonics.setLocalizedText(targetLabel, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.targetLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(targetButton, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.targetButton.text")); // NOI18N
        targetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                targetButtonActionPerformed(evt);
            }
        });

        titleLabel.setLabelFor(titleTextField);
        Mnemonics.setLocalizedText(titleLabel, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.titleLabel.text")); // NOI18N

        configLabel.setLabelFor(configTextField);
        Mnemonics.setLocalizedText(configLabel, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.configLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(configButton, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.configButton.text")); // NOI18N
        configButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configButtonActionPerformed(evt);
            }
        });

        charsetsLabel.setLabelFor(charsetsTextField);

        Mnemonics.setLocalizedText(charsetsLabel, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.charsetsLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(charsetsInfoLabel, "INFO"); // NOI18N
        Mnemonics.setLocalizedText(excludesLabel, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.excludesLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(excludesInfoLabel, "INFO"); // NOI18N

        accessLevelLabel.setLabelFor(accessLevelPublicCheckBox);
        Mnemonics.setLocalizedText(accessLevelLabel, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.accessLevelLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(accessLevelPublicCheckBox, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.accessLevelPublicCheckBox.text")); // NOI18N
        Mnemonics.setLocalizedText(accessLevelProtectedCheckBox, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.accessLevelProtectedCheckBox.text")); // NOI18N
        Mnemonics.setLocalizedText(accessLevelPrivateCheckBox, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.accessLevelPrivateCheckBox.text")); // NOI18N
        Mnemonics.setLocalizedText(internalCheckBox, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.internalCheckBox.text")); // NOI18N
        Mnemonics.setLocalizedText(phpCheckBox, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.phpCheckBox.text")); // NOI18N
        Mnemonics.setLocalizedText(treeCheckBox, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.treeCheckBox.text")); // NOI18N
        Mnemonics.setLocalizedText(deprecatedCheckBox, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.deprecatedCheckBox.text")); // NOI18N
        Mnemonics.setLocalizedText(todoCheckBox, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.todoCheckBox.text")); // NOI18N
        Mnemonics.setLocalizedText(downloadCheckBox, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.downloadCheckBox.text")); // NOI18N
        Mnemonics.setLocalizedText(sourceCodeCheckBox, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.sourceCodeCheckBox.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(targetLabel)
                    .addComponent(titleLabel)
                    .addComponent(configLabel)
                    .addComponent(charsetsLabel)
                    .addComponent(excludesLabel)
                    .addComponent(accessLevelLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(configTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(configButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(targetTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(targetButton))
                    .addComponent(titleTextField)
                    .addComponent(charsetsTextField)
                    .addComponent(excludesTextField)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(accessLevelPublicCheckBox)
                                .addGap(18, 18, 18)
                                .addComponent(accessLevelProtectedCheckBox)
                                .addGap(18, 18, 18)
                                .addComponent(accessLevelPrivateCheckBox))
                            .addComponent(excludesInfoLabel)
                            .addComponent(charsetsInfoLabel))
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(internalCheckBox)
                    .addComponent(phpCheckBox)
                    .addComponent(treeCheckBox)
                    .addComponent(deprecatedCheckBox)
                    .addComponent(todoCheckBox)
                    .addComponent(downloadCheckBox)
                    .addComponent(sourceCodeCheckBox))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {configButton, targetButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(targetLabel)
                    .addComponent(targetTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(targetButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(titleLabel)
                    .addComponent(titleTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(configLabel)
                    .addComponent(configTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(configButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(charsetsLabel)
                    .addComponent(charsetsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(charsetsInfoLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(excludesLabel)
                    .addComponent(excludesTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(excludesInfoLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(accessLevelLabel)
                    .addComponent(accessLevelPublicCheckBox)
                    .addComponent(accessLevelProtectedCheckBox)
                    .addComponent(accessLevelPrivateCheckBox))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(internalCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(phpCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(treeCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(deprecatedCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(todoCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(downloadCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(sourceCodeCheckBox))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("ApiGenPanel.target.title=Select directory for documentation")
    private void targetButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_targetButtonActionPerformed
        File target = new FileChooserBuilder(ApiGenProvider.lastDirFor(phpModule))
                .setTitle(Bundle.ApiGenPanel_target_title())
                .setDirectoriesOnly(true)
                .setFileHiding(true)
                .setDefaultWorkingDirectory(FileUtil.toFile(phpModule.getSourceDirectory()))
                .showOpenDialog();
        if (target != null) {
            target = FileUtil.normalizeFile(target);
            targetTextField.setText(target.getAbsolutePath());
        }
    }//GEN-LAST:event_targetButtonActionPerformed

    @NbBundle.Messages("ApiGenPanel.config.title=Select configuration for documentation")
    private void configButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_configButtonActionPerformed
        File config = new FileChooserBuilder(ApiGenProvider.lastDirFor(phpModule))
                .setTitle(Bundle.ApiGenPanel_config_title())
                .setFilesOnly(true)
                .setDefaultWorkingDirectory(FileUtil.toFile(phpModule.getSourceDirectory()))
                .showOpenDialog();
        if (config != null) {
            config = FileUtil.normalizeFile(config);
            configTextField.setText(config.getAbsolutePath());
        }
    }//GEN-LAST:event_configButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel accessLevelLabel;
    private JCheckBox accessLevelPrivateCheckBox;
    private JCheckBox accessLevelProtectedCheckBox;
    private JCheckBox accessLevelPublicCheckBox;
    private JLabel charsetsInfoLabel;
    private JLabel charsetsLabel;
    private JTextField charsetsTextField;
    private JButton configButton;
    private JLabel configLabel;
    private JTextField configTextField;
    private JCheckBox deprecatedCheckBox;
    private JCheckBox downloadCheckBox;
    private JLabel excludesInfoLabel;
    private JLabel excludesLabel;
    private JTextField excludesTextField;
    private JCheckBox internalCheckBox;
    private JCheckBox phpCheckBox;
    private JCheckBox sourceCodeCheckBox;
    private JButton targetButton;
    private JLabel targetLabel;
    private JTextField targetTextField;
    private JLabel titleLabel;
    private JTextField titleTextField;
    private JCheckBox todoCheckBox;
    private JCheckBox treeCheckBox;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class DefaultDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processChange();
        }

        private void processChange() {
            validateData();
        }

    }

    private final class DefaultActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            validateData();
        }

    }

}
