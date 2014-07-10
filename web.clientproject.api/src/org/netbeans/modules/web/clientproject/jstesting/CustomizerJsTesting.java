/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.clientproject.jstesting;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProvider;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProviders;
import org.netbeans.modules.web.clientproject.util.WebCommonUtils;
import org.netbeans.modules.web.common.api.UsageLogger;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

class CustomizerJsTesting extends JPanel {

    private final ProjectCustomizer.Category category;
    private final Project project;
    private final JsTestingProvider originalProvider;
    private final UsageLogger usageLogger = new UsageLogger.Builder(WebCommonUtils.USAGE_LOGGER_NAME)
            .message(UsageLogger.class, "USG_TEST_CONFIG_JS") // NOI18N
            .create();

    volatile JsTestingProvider selectedProvider;


    CustomizerJsTesting(ProjectCustomizer.Category category, Project project) {
        assert EventQueue.isDispatchThread();
        assert category != null;
        assert project != null;

        this.category = category;
        this.project = project;
        originalProvider = JsTestingProviders.getDefault().getJsTestingProvider(project, false);
        selectedProvider = originalProvider;

        initComponents();
        init();
    }

    private void init() {
        providerComboBox.addItem(null);
        for (JsTestingProvider provider : JsTestingProviders.getDefault().getJsTestingProviders()) {
            providerComboBox.addItem(provider);
        }
        providerComboBox.setSelectedItem(originalProvider);
        providerComboBox.setRenderer(new JsTestingProviderRenderer());
        // listeners
        providerComboBox.addActionListener(new ProviderActionListener());
        category.setStoreListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                storeData();
            }
        });
    }

    @NbBundle.Messages("CustomizerJsTesting.warning.restart.needed=Confirm and reopen this dialog to apply the changes.")
    void validateData() {
        String message;
        if (Objects.equals(originalProvider, selectedProvider)) {
            message = null;
        } else {
            message = Bundle.CustomizerJsTesting_warning_restart_needed();
        }
        category.setErrorMessage(message);
        category.setValid(true);
    }

    void storeData() {
        assert !EventQueue.isDispatchThread();
        if (Objects.equals(originalProvider, selectedProvider)) {
            // no change
            return;
        }
        usageLogger.log(project.getClass().getName(), selectedProvider == null ? "" : selectedProvider.getIdentifier());
        if (originalProvider != null) {
            JsTestingProviderAccessor.getDefault().notifyEnabled(originalProvider, project, false);
        }
        if (selectedProvider != null) {
            JsTestingProviderAccessor.getDefault().notifyEnabled(selectedProvider, project, true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        providerLabel = new JLabel();
        providerComboBox = new JComboBox<JsTestingProvider>();

        providerLabel.setLabelFor(providerComboBox);
        Mnemonics.setLocalizedText(providerLabel, NbBundle.getMessage(CustomizerJsTesting.class, "CustomizerJsTesting.providerLabel.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(providerLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(providerComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(providerLabel)
                .addComponent(providerComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox<JsTestingProvider> providerComboBox;
    private JLabel providerLabel;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class ProviderActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            selectedProvider = (JsTestingProvider) providerComboBox.getSelectedItem();
            validateData();
        }

    }

}
