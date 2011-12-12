/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

/*
 * CustomizerCommon.java
 *
 * Created on Jul 27, 2011, 11:55:54 AM
 */
package org.netbeans.modules.coherence.server.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.server.properties.InstanceProperties;
import org.netbeans.modules.coherence.library.LibraryUtils;
import org.netbeans.modules.coherence.server.CoherenceModuleProperties;
import org.netbeans.modules.coherence.server.CoherenceProperties;
import org.netbeans.modules.coherence.server.util.ClasspathPropertyUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * Panel for setup base (common) Coherence instance properties.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class CustomizerCommon extends javax.swing.JPanel implements ChangeListener {

    private DefaultListModel listModel;
    private InstanceProperties instanceProperties;
    private JFileChooser fileChooser = new JFileChooser();

    private ChangeSupport changeSupport = new ChangeSupport(this);

    /**
     * Creates new {@code CustomizerCommon} panel.
     *
     * @param instanceProperties properties for which will be form initialized
     */
    public CustomizerCommon(InstanceProperties instanceProperties) {
        initComponents();
        this.instanceProperties = instanceProperties;

        init();
    }

    /**
     * Initialization of the panel values.
     */
    private void init() {
        coherenceLocationTextField.setText(instanceProperties.getString(CoherenceModuleProperties.PROP_LOCATION, ""));
        javaFlagsTextField.setText(instanceProperties.getString(CoherenceModuleProperties.PROP_JAVA_FLAGS, ""));
        customPropertiesTextField.setText(instanceProperties.getString(CoherenceModuleProperties.PROP_CUSTOM_PROPERTIES, ""));

        listModel = new DefaultListModel();
        for (String cp : ClasspathPropertyUtils.classpathFromStringToArray(instanceProperties.getString(CoherenceModuleProperties.PROP_CLASSPATH, ""))) {
            if (!ClasspathPropertyUtils.isCoherenceServerJar(cp, true)) {
                listModel.addElement(cp);
            }
        }
        classpathList.setModel(listModel);

        changeSupport.addChangeListener(this);
        coherenceLocationTextField.getDocument().addDocumentListener(new SaveDocumentListener());
        javaFlagsTextField.getDocument().addDocumentListener(new SaveDocumentListener());
        customPropertiesTextField.getDocument().addDocumentListener(new SaveDocumentListener());
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        savePanel();
    }

    private class SaveDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            fireChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            fireChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            fireChange();
        }

        private void fireChange() {
            changeSupport.fireChange();
        }
    }

    /**
     * Storing values from this panel into {@link InstanceProperties}.
     */
    private void savePanel() {
        instanceProperties.putString(CoherenceModuleProperties.PROP_JAVA_FLAGS, javaFlagsTextField.getText());
        instanceProperties.putString(CoherenceModuleProperties.PROP_CUSTOM_PROPERTIES, customPropertiesTextField.getText());

        // update classpath property
        List<String> cpEntries = new ArrayList<String>();
        for (int i = 0; i < classpathList.getModel().getSize(); i++) {
            cpEntries.add((String) classpathList.getModel().getElementAt(i));
        }
        ClasspathPropertyUtils.updateClasspathProperty(
                instanceProperties,
                cpEntries.toArray(new String[cpEntries.size()]),
                null);
    }

    private void setEnabledRemoveButton(boolean setEnabled) {
        removeClasspathButton.setEnabled(setEnabled);
    }

    private void addElementToClasspathList(String element) {
        listModel.addElement(element);
    }

    /**
     * Shows the fileChooser.
     */
    private void showFileChooser() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }

        // set the chooser's properties
        fileChooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                String fileName = f.getName();
                String fileExt = fileName.substring(
                        fileName.lastIndexOf(".") + 1, //NOI18N
                        fileName.length());
                if (f.isDirectory()) {
                    return true;
                } else if (f.isFile() && "jar".equalsIgnoreCase(fileExt)) { //NOI18N
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return NbBundle.getMessage(CustomizerCommon.class, "DESC_AddJarToClasspath"); //NOI18N
            }
        });
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // wait for the user to choose the file and if he clicked OK button add
        // the selected JAR into the classpath list
        if (fileChooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
            addElementToClasspathList(fileChooser.getSelectedFile().getPath());
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

        classpathLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        classpathList = new javax.swing.JList();
        addClasspathButton = new javax.swing.JButton();
        removeClasspathButton = new javax.swing.JButton();
        javaFlagsLabel = new javax.swing.JLabel();
        javaFlagsTextField = new javax.swing.JTextField();
        customPropertiesLabel = new javax.swing.JLabel();
        customPropertiesTextField = new javax.swing.JTextField();
        coherenceLocationTextField = new javax.swing.JTextField();
        coherenceLocationLabel = new javax.swing.JLabel();
        createLibraryButton = new javax.swing.JButton();

        setName(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "TITLE_Common")); // NOI18N

        classpathLabel.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.classpathLabel.text")); // NOI18N
        classpathLabel.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CoherenceCommonTab.additionalClasspathLabel.desc")); // NOI18N

        classpathList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        classpathList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                classpathListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(classpathList);

        addClasspathButton.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.addClasspathButton.text")); // NOI18N
        addClasspathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addClasspathButtonActionPerformed(evt);
            }
        });

        removeClasspathButton.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.removeClasspathButton.text")); // NOI18N
        removeClasspathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeClasspathButtonActionPerformed(evt);
            }
        });

        javaFlagsLabel.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.javaFlagsLabel.text")); // NOI18N
        javaFlagsLabel.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CoherenceCommonTab.javaFlagsLabel.desc")); // NOI18N

        javaFlagsTextField.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.javaFlagsTextField.text")); // NOI18N

        customPropertiesLabel.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.customPropertiesLabel.text")); // NOI18N
        customPropertiesLabel.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CoherenceCommonTab.customPropertiesLabel.desc")); // NOI18N

        customPropertiesTextField.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.customPropertiesTextField.text")); // NOI18N

        coherenceLocationTextField.setEditable(false);

        coherenceLocationLabel.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.coherenceLocationLabel.text")); // NOI18N

        createLibraryButton.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.createLibraryButton.text")); // NOI18N
        createLibraryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createLibraryButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(javaFlagsLabel)
                            .addComponent(customPropertiesLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(javaFlagsTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                            .addComponent(customPropertiesTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(coherenceLocationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(coherenceLocationTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE))
                    .addComponent(classpathLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addClasspathButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(removeClasspathButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(createLibraryButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(coherenceLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(coherenceLocationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(classpathLabel)
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addClasspathButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeClasspathButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(javaFlagsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(javaFlagsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(customPropertiesLabel)
                    .addComponent(customPropertiesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(createLibraryButton))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addClasspathButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addClasspathButtonActionPerformed
        showFileChooser();
        changeSupport.fireChange();
    }//GEN-LAST:event_addClasspathButtonActionPerformed

    private void removeClasspathButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeClasspathButtonActionPerformed
        if (classpathList.getSelectedIndex() == -1) {
            return;
        }
        listModel.remove(classpathList.getSelectedIndex());
        changeSupport.fireChange();
    }//GEN-LAST:event_removeClasspathButtonActionPerformed

private void classpathListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_classpathListValueChanged
    if (classpathList.getSelectedValue() == null) {
        return;
    }
}//GEN-LAST:event_classpathListValueChanged

    private void createLibraryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createLibraryButtonActionPerformed
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(CustomizerCommon.class, "MSG_ConfirmationForLibraryCreation", //NOI18N
                        instanceProperties.getString(CoherenceModuleProperties.PROP_DISPLAY_NAME, "")), //NOI18N
                NbBundle.getMessage(CustomizerCommon.class, "TIT_LibraryCreationDialog"), //NOI18N
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE);
        if (DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION) {
            File location = new File(instanceProperties.getString(CoherenceModuleProperties.PROP_LOCATION, ""));
            LibraryUtils.createCoherenceLibrary(location);
        }
    }//GEN-LAST:event_createLibraryButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addClasspathButton;
    private javax.swing.JLabel classpathLabel;
    private javax.swing.JList classpathList;
    private javax.swing.JLabel coherenceLocationLabel;
    private javax.swing.JTextField coherenceLocationTextField;
    private javax.swing.JButton createLibraryButton;
    private javax.swing.JLabel customPropertiesLabel;
    private javax.swing.JTextField customPropertiesTextField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel javaFlagsLabel;
    private javax.swing.JTextField javaFlagsTextField;
    private javax.swing.JButton removeClasspathButton;
    // End of variables declaration//GEN-END:variables
}
