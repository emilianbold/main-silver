/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.build2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.ui.UiUtils;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

public final class BuildContextVisual extends JPanel {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    /**
     * Creates new form BuildVisualPanel1
     */
    public BuildContextVisual() {
        initComponents();

        DefaultDocumentListener listener = new DefaultDocumentListener();
        buildContextTextField.getDocument().addDocumentListener(listener);
        ((JTextComponent) repositoryComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(listener);
        tagTextField.getDocument().addDocumentListener(listener);

        repositoryComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeSupport.fireChange();
            }
        });
        
        //UiUtils.loadRepositories(instance, repositoryComboBox);
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public String getBuildContext() {
        return UiUtils.getValue(buildContextTextField);
    }

    public void setBuildContext(String buildContext) {
        buildContextTextField.setText(buildContext);
    }

    public String getRepository() {
        return UiUtils.getValue(repositoryComboBox);
    }

    public void setRepository(String repository) {
        repositoryComboBox.getEditor().setItem(repository);
    }

    public String getTag() {
        return UiUtils.getValue(tagTextField);
    }

    public void setTag(String tag) {
        tagTextField.setText(tag);
    }

    @NbBundle.Messages("LBL_BuildContext=Build Context")
    @Override
    public String getName() {
        return Bundle.LBL_BuildContext();
    }

    private class DefaultDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            changeSupport.fireChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changeSupport.fireChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            changeSupport.fireChange();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buldContextLabel = new javax.swing.JLabel();
        buildContextTextField = new javax.swing.JTextField();
        buildContextButton = new javax.swing.JButton();
        repositoryLabel = new javax.swing.JLabel();
        repositoryComboBox = new javax.swing.JComboBox<>();
        tagLabel = new javax.swing.JLabel();
        tagTextField = new javax.swing.JTextField();

        buldContextLabel.setLabelFor(buildContextTextField);
        org.openide.awt.Mnemonics.setLocalizedText(buldContextLabel, org.openide.util.NbBundle.getMessage(BuildContextVisual.class, "BuildContextVisual.buldContextLabel.text")); // NOI18N

        buildContextTextField.setColumns(10);

        org.openide.awt.Mnemonics.setLocalizedText(buildContextButton, org.openide.util.NbBundle.getMessage(BuildContextVisual.class, "BuildContextVisual.buildContextButton.text")); // NOI18N
        buildContextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buildContextButtonActionPerformed(evt);
            }
        });

        repositoryLabel.setLabelFor(repositoryComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(repositoryLabel, org.openide.util.NbBundle.getMessage(BuildContextVisual.class, "BuildContextVisual.repositoryLabel.text")); // NOI18N

        repositoryComboBox.setEditable(true);

        tagLabel.setLabelFor(tagTextField);
        org.openide.awt.Mnemonics.setLocalizedText(tagLabel, org.openide.util.NbBundle.getMessage(BuildContextVisual.class, "BuildContextVisual.tagLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buldContextLabel)
                    .addComponent(repositoryLabel)
                    .addComponent(tagLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(buildContextTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buildContextButton))
                    .addComponent(repositoryComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tagTextField)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buldContextLabel)
                    .addComponent(buildContextTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buildContextButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(repositoryLabel)
                    .addComponent(repositoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tagLabel)
                    .addComponent(tagTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void buildContextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buildContextButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String buildText = UiUtils.getValue(buildContextTextField);
        if (buildText != null) {
            chooser.setSelectedFile(new File(buildText));
        }
        if (chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
            buildContextTextField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_buildContextButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buildContextButton;
    private javax.swing.JTextField buildContextTextField;
    private javax.swing.JLabel buldContextLabel;
    private javax.swing.JComboBox<String> repositoryComboBox;
    private javax.swing.JLabel repositoryLabel;
    private javax.swing.JLabel tagLabel;
    private javax.swing.JTextField tagTextField;
    // End of variables declaration//GEN-END:variables
}
