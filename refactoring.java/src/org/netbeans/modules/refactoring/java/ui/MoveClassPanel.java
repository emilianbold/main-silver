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

package org.netbeans.modules.refactoring.java.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.UIResource;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.*;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.awt.Mnemonics;
import org.openide.explorer.view.NodeRenderer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Asks where to move a class to.
 * @author Jan Becicka, Jesse Glick
 */
public final class MoveClassPanel extends JPanel implements ActionListener, DocumentListener,CustomRefactoringPanel {
  
    private final ListCellRenderer GROUP_CELL_RENDERER = new GroupCellRenderer();
    private final ListCellRenderer PROJECT_CELL_RENDERER = new ProjectCellRenderer();
    
    private Project project;
    private ChangeListener parent;
    private FileObject fo;
    private SourceGroup[] groups;
    private String startPackage;
    private String newName;
    private String bypassLine;
    
    public MoveClassPanel(final ChangeListener parent, String startPackage, String headLine, String bypassLine, FileObject f, boolean disable, Vector nodes) {
        this(parent, startPackage, headLine, bypassLine, f);
        setCombosEnabled(!disable);
        JList list = new JList(nodes);
        list.setCellRenderer(new NodeRenderer()); 
        list.setVisibleRowCount(5);
        JScrollPane pane = new JScrollPane(list);
        bottomPanel.setBorder(new EmptyBorder(8,0,0,0));
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(pane, BorderLayout.CENTER);
        JLabel listOf = new JLabel();
        Mnemonics.setLocalizedText(listOf, NbBundle.getMessage(MoveClassesUI.class, "LBL_ListOfClasses"));
        bottomPanel.add(listOf, BorderLayout.NORTH);
    }
    
    public MoveClassPanel(final ChangeListener parent, String startPackage, String headLine, String bypassLine, FileObject f) {
        this(parent, startPackage, headLine, bypassLine, f, null);
    }
    
    public MoveClassPanel(final ChangeListener parent, String startPackage, String headLine, String bypassLine, FileObject f, String newName) {
        this.fo = f;
        this.parent = parent;
        this.newName = newName;
        this.bypassLine = bypassLine;
        initComponents();
        setCombosEnabled(true);
        
        labelHeadLine.setText(headLine);
        
        rootComboBox.setRenderer(GROUP_CELL_RENDERER);
        packageComboBox.setRenderer(PackageView.listRenderer());
        projectsComboBox.setRenderer(PROJECT_CELL_RENDERER);
        Project fileOwner = fo != null ? FileOwnerQuery.getOwner(fo) : null;
        project = fileOwner != null ? fileOwner : OpenProjects.getDefault().getOpenProjects()[0];
        this.startPackage = startPackage;
        
        if(newName != null) {
            labelHeadLine.setVisible(false);
        } else {
            labelNewName.setVisible(false);
            newNameField.setVisible(false);
        }
    }

    private String getBypassLine() {
        return bypassLine;
    }
    
    private boolean initialized = false;
    @Override
    public void initialize() {
        if (initialized) {
            return ;
        }
        //put initialization code here
        initValues(startPackage);
        
        if (newName != null) {
            FileObject fob;
            do {
                fob = fo.getFileObject(newName + ".java"); //NOI18N
                if (fob != null) {
                    newName += "1"; // NOI18N
                }
            } while (fob != null);
            newNameField.setText(newName);
            newNameField.setSelectionStart(0);
            newNameField.setSelectionEnd(newNameField.getText().length());
        }
        rootComboBox.addActionListener( this );
        packageComboBox.addActionListener( this );
        projectsComboBox.addActionListener( this );
        Object textField = packageComboBox.getEditor().getEditorComponent();
        if (textField instanceof JTextField) {
            ((JTextField) textField).getDocument().addDocumentListener(this); 
        }
        newNameField.getDocument().addDocumentListener(this);
        initialized = true;
    }
    
    public void initValues(String preselectedFolder ) {
        
        Project openProjects[] = OpenProjects.getDefault().getOpenProjects();
        Arrays.sort( openProjects, new ProjectByDisplayNameComparator());
        DefaultComboBoxModel projectsModel = new DefaultComboBoxModel( openProjects );
        projectsComboBox.setModel( projectsModel );                
        projectsComboBox.setSelectedItem( project );
        
        updateRoots();
        updatePackages(); 
        if (preselectedFolder != null) {
            packageComboBox.setSelectedItem(preselectedFolder);
        }
        // Determine the extension
    }
    
    @Override
    public void requestFocus() {
        packageComboBox.requestFocus();
    }
    
    public FileObject getRootFolder() {
        return ((SourceGroup) rootComboBox.getSelectedItem()).getRootFolder();
    }
    
    public String getPackageName() {
        String packageName = packageComboBox.getEditor().getItem().toString();
        return packageName; // NOI18N
    }
    
    private void fireChange() {
        parent.stateChanged(null);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        labelProject = new javax.swing.JLabel();
        projectsComboBox = new javax.swing.JComboBox();
        labelLocation = new javax.swing.JLabel();
        rootComboBox = new javax.swing.JComboBox();
        labelPackage = new javax.swing.JLabel();
        packageComboBox = new javax.swing.JComboBox();
        bottomPanel = new javax.swing.JPanel();
        bypassRefactoringCheckBox = new javax.swing.JCheckBox();
        labelHeadLine = new javax.swing.JLabel();
        labelNewName = new javax.swing.JLabel();
        newNameField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        labelProject.setLabelFor(projectsComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(labelProject, org.openide.util.NbBundle.getMessage(MoveClassPanel.class, "LBL_Project")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(labelProject, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(projectsComboBox, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/java/ui/Bundle"); // NOI18N
        projectsComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_projectsCombo")); // NOI18N

        labelLocation.setLabelFor(rootComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(labelLocation, org.openide.util.NbBundle.getMessage(MoveClassPanel.class, "LBL_Location")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(labelLocation, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(rootComboBox, gridBagConstraints);
        rootComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_rootCombo")); // NOI18N

        labelPackage.setLabelFor(packageComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(labelPackage, org.openide.util.NbBundle.getMessage(MoveClassPanel.class, "LBL_ToPackage")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(labelPackage, gridBagConstraints);

        packageComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(packageComboBox, gridBagConstraints);
        packageComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MoveClassPanel.class, "MoveClassPanel.packageComboBox.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(bottomPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bypassRefactoringCheckBox, getBypassLine());
        bypassRefactoringCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 0, 4));
        bypassRefactoringCheckBox.setMargin(new java.awt.Insets(2, 2, 0, 2));
        bypassRefactoringCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                bypassRefactoringCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(bypassRefactoringCheckBox, gridBagConstraints);
        bypassRefactoringCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MoveClassPanel.class, "MoveClassPanel.updateReferencesCheckBox.AccessibleContext.accessibleDescription")); // NOI18N

        labelHeadLine.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 6, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(labelHeadLine, gridBagConstraints);

        labelNewName.setLabelFor(newNameField);
        org.openide.awt.Mnemonics.setLocalizedText(labelNewName, org.openide.util.NbBundle.getMessage(MoveClassPanel.class, "LBL_NewName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(labelNewName, gridBagConstraints);

        newNameField.setText(org.openide.util.NbBundle.getMessage(MoveClassPanel.class, "CopyClassPanel.newNameTextField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(newNameField, gridBagConstraints);
        newNameField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MoveClassPanel.class, "CopyClassPanel.newNameTextField.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void bypassRefactoringCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_bypassRefactoringCheckBoxItemStateChanged
    parent.stateChanged(null);
}//GEN-LAST:event_bypassRefactoringCheckBoxItemStateChanged

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JPanel bottomPanel;
    private javax.swing.JCheckBox bypassRefactoringCheckBox;
    private javax.swing.JLabel labelHeadLine;
    private javax.swing.JLabel labelLocation;
    private javax.swing.JLabel labelNewName;
    private javax.swing.JLabel labelPackage;
    private javax.swing.JLabel labelProject;
    private javax.swing.JTextField newNameField;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JComboBox projectsComboBox;
    private javax.swing.JComboBox rootComboBox;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
        
    @Override
    public void actionPerformed(ActionEvent e) {
        if (projectsComboBox == e.getSource()) {
            project = (Project) projectsComboBox.getSelectedItem();
            updateRoots();
            updatePackages();
        } else 
        if ( rootComboBox == e.getSource() ) {            
            updatePackages();
        }
        else if ( packageComboBox == e.getSource() ) {
        }
    }    
    
    // DocumentListener implementation -----------------------------------------
    
    @Override
    public void changedUpdate(DocumentEvent e) {                
        fireChange();        
    }    
    
    @Override
    public void insertUpdate(DocumentEvent e) {
        fireChange();        
    }
    
    @Override
    public void removeUpdate(DocumentEvent e) {
        fireChange();        
    }
    
    // Private methods ---------------------------------------------------------
        
    private void updatePackages() {
        SourceGroup g = (SourceGroup) rootComboBox.getSelectedItem();
        packageComboBox.setModel(g != null
                ? PackageView.createListView(g)
                : new DefaultComboBoxModel());
    }
    
    void setCombosEnabled(boolean enabled) {
        packageComboBox.setEnabled(enabled);
        rootComboBox.setEnabled(enabled);
        projectsComboBox.setEnabled(enabled);
        bypassRefactoringCheckBox.setVisible(!enabled);
        this.setEnabled(enabled);
    }

    public boolean isRefactoringBypassRequired() {
        return bypassRefactoringCheckBox.isVisible() && bypassRefactoringCheckBox.isSelected();
    }
    
    private void updateRoots() {
        Sources sources = ProjectUtils.getSources(project);
        groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        // XXX why?? This is probably wrong. If the project has no Java groups,
        // you cannot move anything into it.
        if (groups.length == 0) {
            groups = sources.getSourceGroups( Sources.TYPE_GENERIC ); 
        }

        int preselectedItem = 0;
        for( int i = 0; i < groups.length; i++ ) {
            if (fo!=null) {
                try {
                    if (groups[i].contains(fo)) {
                        preselectedItem = i;
                    }
                } catch (IllegalArgumentException e) {
                    // XXX this is a poor abuse of exception handling
                }
            }
        }
                
        // Setup comboboxes 
        rootComboBox.setModel(new DefaultComboBoxModel(groups));
        if(groups.length > 0) {
            rootComboBox.setSelectedIndex(preselectedItem);
        }
    }

    public String getNewName() {
        return newNameField.getText();
    }
    
    private abstract static class BaseCellRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public BaseCellRenderer () {
            setOpaque(true);
        }
        
        // #89393: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    }
    
    /** Groups combo renderer, used also in CopyClassPanel */
    static class GroupCellRenderer extends BaseCellRenderer {
        
        @Override
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            if (value instanceof SourceGroup) {
                SourceGroup g = (SourceGroup) value;
                setText(g.getDisplayName());
                setIcon(g.getIcon(false));
            } else {
                setText(""); // NOI18N
                setIcon(null);
            }
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
    }
    
    /** Projects combo renderer, used also in CopyClassPanel */
    static class ProjectCellRenderer extends BaseCellRenderer {
        
        @Override
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            if ( value != null ) {
                ProjectInformation pi = ProjectUtils.getInformation((Project)value);
                setText(pi.getDisplayName());
                setIcon(pi.getIcon());
            }
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
    }
    //Copy/pasted from OpenProjectList
    //remove this code as soon as #68827 is fixed.
    private static class ProjectByDisplayNameComparator implements Comparator {
        
        private static Comparator COLLATOR = Collator.getInstance();
        
        @Override
        public int compare(Object o1, Object o2) {
            
            if ( !( o1 instanceof Project ) ) {
                return 1;
            }
            if ( !( o2 instanceof Project ) ) {
                return -1;
            }
            
            Project p1 = (Project)o1;
            Project p2 = (Project)o2;
            
            return COLLATOR.compare(ProjectUtils.getInformation(p1).getDisplayName(), ProjectUtils.getInformation(p2).getDisplayName());
        }
    }    

    @Override
    public Component getComponent() {
        return this;
    }
}
