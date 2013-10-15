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
package org.netbeans.modules.bugtracking.tasks;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import org.openide.util.NbBundle;

/**
 *
 * @author jpeska
 */
public class SortAttributePanel extends javax.swing.JPanel {

    private final List<TaskAttribute> attributes;
    private TaskAttribute selected;
    private final int index;
    private ArrayList<TaskAttribute> availableAttributes;
    private final List<ActionListener> changeListeners = new ArrayList<ActionListener>();
    private final ActionListener comboActionListener = new ComboActionListener();
    private final ItemListener ascendingListener = new AscendingListener();

    /**
     * Creates new form SortAttributePanel
     */
    public SortAttributePanel(List<TaskAttribute> attributes, TaskAttribute selected, int index) {
        this.attributes = attributes;
        this.selected = selected;
        this.index = index;
        initComponents();
        initCombo();
    }

    public void addSortingChangeListener(ActionListener listener) {
        changeListeners.add(listener);
    }

    public void removeSortingChangeListener(ActionListener listener) {
        changeListeners.remove(listener);
    }

    public TaskAttribute getSelectedAttribute() {
        return selected;
    }

    public int getIndex() {
        return index;
    }

    public void updateModel(TaskAttribute selected) {
        this.selected = selected;
        initCombo();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        comboAttributes = new javax.swing.JComboBox();
        rbAscending = new javax.swing.JRadioButton();
        rbDescending = new javax.swing.JRadioButton();

        comboAttributes.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        buttonGroup1.add(rbAscending);
        org.openide.awt.Mnemonics.setLocalizedText(rbAscending, NbBundle.getMessage(SortAttributePanel.class, "SortAttributePanel.rbAscending.text")); // NOI18N

        buttonGroup1.add(rbDescending);
        org.openide.awt.Mnemonics.setLocalizedText(rbDescending, NbBundle.getMessage(SortAttributePanel.class, "SortAttributePanel.rbDescending.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(comboAttributes, 0, 204, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(rbAscending)
                .addGap(18, 18, 18)
                .addComponent(rbDescending, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(comboAttributes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(rbAscending)
                .addComponent(rbDescending))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox comboAttributes;
    private javax.swing.JRadioButton rbAscending;
    private javax.swing.JRadioButton rbDescending;
    // End of variables declaration//GEN-END:variables

    private void initCombo() {
        comboAttributes.removeActionListener(comboActionListener);
        rbAscending.removeItemListener(ascendingListener);
        availableAttributes = new ArrayList<TaskAttribute>();

        for (TaskAttribute attribute : attributes) {
            if (attribute.getRank() >= index) {
                availableAttributes.add(attribute);
            }
        }
        String[] values = new String[attributes.size() + 1];
        values[0] = "<" + NbBundle.getMessage(SortAttributePanel.class, "LBL_NoCategory") + ">"; //NOI18N
        for (int i = 0; i < availableAttributes.size(); i++) {
            values[i + 1] = availableAttributes.get(i).getDisplayName();
        }
        comboAttributes.setModel(new DefaultComboBoxModel(values));
        comboAttributes.setSelectedIndex(availableAttributes.indexOf(selected) + 1);
        if (selected != null) {
            rbAscending.setEnabled(true);
            rbDescending.setEnabled(true);
            if (selected.isAsceding()) {
                rbAscending.setSelected(true);
            } else {
                rbDescending.setSelected(true);
            }
        } else {
            rbAscending.setEnabled(false);
            rbDescending.setEnabled(false);
        }
        rbAscending.addItemListener(ascendingListener);
        comboAttributes.addActionListener(comboActionListener);
    }

    void setComponentsEnabled(boolean enabled) {
        comboAttributes.setEnabled(enabled);
        rbAscending.setEnabled(enabled);
        rbDescending.setEnabled(enabled);
    }

    private class ComboActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            TaskAttribute lastSelected = selected;
            int i = comboAttributes.getSelectedIndex() - 1;
            selected = i == -1 ? null : availableAttributes.get(i);
            if (lastSelected != null) {
                lastSelected.setRank(selected != null ? selected.getRank() : TaskAttribute.NO_RANK);
            }
            if (selected != null) {
                selected.setRank(index);
            }
            fireEvent(e);
        }

        private void fireEvent(ActionEvent e) {
            for (ActionListener listener : changeListeners) {
                listener.actionPerformed(e);
            }
        }
    }

    private class AscendingListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            selected.setAsceding(rbAscending.isSelected());
        }

    }
}