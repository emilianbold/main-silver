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

package org.netbeans.modules.tasklist.todo.settings;

import java.awt.Component;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author S. Aubrecht
 */
@OptionsPanelController.Keywords(keywords={"#KW_ToDo", "action", "items"}, location=OptionsDisplayer.ADVANCED, tabTitle="#LBL_Options")
class ToDoCustomizer extends javax.swing.JPanel implements DocumentListener{
    
    private boolean changed = false;
    private boolean isUpdating = false;
    private List<MimeIdentifier> mimeIdentifiers;
    private List<ExtensionIdentifier> extensionIdentifiers;
    private int selectedIndex = -1;
    private IdentifierModel identifierModel;
    private static final String ICON_NOT_VALID_PATH = "org/netbeans/modules/tasklist/todo/settings/error.png"; //NOI18N
    private static final Icon NOT_VALID_ICON = ImageUtilities.loadImageIcon(ICON_NOT_VALID_PATH, true);
    private static final Icon EMPTY_ICON = new EmptyIcon();
    private boolean detailsValid;
    
    /** Creates new form ToDoCustomizer */
    public ToDoCustomizer() {
        initComponents();
        lblError.setVisible(false);
        table.getSelectionModel().setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        table.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                enableButtons();
            }
        });
        jScrollPane1.getViewport().setOpaque( false );
        enableButtons();
    }
    
    private void enableButtons() {
        int selIndex = table.getSelectedRow();
        btnChange.setEnabled( selIndex >= 0 );
        btnRemove.setEnabled( selIndex >= 0 );
    }
    
    void update() {
        isUpdating = true;
        Collection<String> patterns = Settings.getDefault().getPatterns();
        table.setModel( createModel( patterns ) );
        table.setTableHeader( null );
        checkScanCommentsOnly.setSelected( Settings.getDefault().isScanCommentsOnly() );
        initList();
        changed = false;
        isUpdating = false;
    }
    
    void applyChanges() {
        final TableCellEditor cellEditor = table.getCellEditor();
        if (cellEditor != null) {
            cellEditor.stopCellEditing();
        }
        DefaultTableModel model = (DefaultTableModel)table.getModel();
        ArrayList<String> patterns = new ArrayList<String>( model.getRowCount() );
        for( int i=0; i<model.getRowCount(); i++ ) {
            Object value = model.getValueAt(i, 0);
            if (value == null) {
                continue;
            }
            String pattern = value.toString();
            //remove empty patterns
            if (!pattern.trim().isEmpty() && !pattern.trim().equals(getDummyPattern())) {
                patterns.add(pattern);
            }
        }
        Settings.getDefault().setPatterns( patterns );
        Settings.getDefault().setScanCommentsOnly( checkScanCommentsOnly.isSelected() );

        Settings.getDefault().setIdentifiers(mimeIdentifiers, extensionIdentifiers);
    }
    
    boolean isDataValid() {
        boolean listValid = isListValid();
        listIdentifiers.repaint();
        lblError.setVisible(!listValid);
        return table.getRowCount() > 0 && listValid;
    }
    
    boolean isChanged() {
        return changed;
    }

    private boolean isListValid() {
        saveDetails(selectedIndex);
        for (MimeIdentifier mimeIdentifier : mimeIdentifiers) {
            if (!mimeIdentifier.isValid()) {
                return false;
            }
        }
        for (ExtensionIdentifier extensionIdentifier : extensionIdentifiers) {
            if (!extensionIdentifier.isValid()) {
                return false;
            }
        }
        return true;
    }
    
    private DefaultTableModel createModel( Collection<String> patterns ) {
        DefaultTableModel model = new DefaultTableModel( 
                new Object[] { NbBundle.getMessage( ToDoCustomizer.class, "ToDoCustomizer.TableHeader" ) }, patterns.size() ); //NOI18N
        int row = 0;
        for( String p : patterns ) {
            model.setValueAt( p, row++, 0 );
        }
        return model;
    }

    private void initList() {
        mimeIdentifiers = Settings.getDefault().getMimeIdentifiers();
        extensionIdentifiers = Settings.getDefault().getExtensionIdentifiers();
        listIdentifiers.setCellRenderer(new IdentifierRenderer());
        updateListModel();
        listIdentifiers.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int index = listIdentifiers.getSelectedIndex();
                btnRemoveMime.setEnabled(index != -1 && index != 0 && index != mimeIdentifiers.size() + 1);
                updateMimeDetails(index);
            }
        });
    }

    private void updateMimeDetails(int index) {
        if (index == selectedIndex) {
            return;
        }
        removeDocumentListeners();
        saveDetails(selectedIndex);
        loadDetails(index);
        addDocumentListeners();
        selectedIndex = index;
    }

    /*Save details to the object and return true if data is valid*/
    private boolean saveDetails(int index) {
        if (index == -1 || index == identifierModel.getSize()) {
            return false;
        }
        Object elementAt = identifierModel.getElementAt(index);
        if (elementAt instanceof FileIdentifier) {
            FileIdentifier identifier = (FileIdentifier) elementAt;
            CommentTags tag = identifier.getCommentTags();
            tag.setLineComment(txtLine.getText().trim());
            tag.setLineCommentEnabled(chbLine.isSelected());

            tag.setBlockCommentStart(txtBlockStart.getText().trim());
            tag.setBlockCommentEnd(txtBlockEnd.getText().trim());
            tag.setBlockCommentEnabled(chbBlock.isSelected());
            return identifier.isValid();
        }
        return false;
    }

    private void loadDetails(int index) {
        if (index == -1 || !(identifierModel.getElementAt(index) instanceof FileIdentifier)) {
            disableDetails();
            return;
        }
        FileIdentifier identifier = (FileIdentifier) identifierModel.getElementAt(index);
        CommentTags tag = identifier.getCommentTags();
        lblMimeName.setText(identifier.getDisplayName() + " - " + NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.lblMimeName.text"));
        chbLine.setEnabled(true);
        chbLine.setSelected(identifier.isValid() ? !tag.getLineComment().isEmpty() : true);
        updateEnableLine();
        txtLine.setText(tag.getLineComment());

        chbBlock.setEnabled(true);
        chbBlock.setSelected(identifier.isValid() ? !tag.getBlockCommentStart().isEmpty() : true);
        updateEnableBlock();
        txtBlockStart.setText(tag.getBlockCommentStart());
        txtBlockEnd.setText(tag.getBlockCommentEnd());
    }

    private void disableDetails() {
        lblMimeName.setText(NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.lblMimeName.text"));
        chbLine.setSelected(false);
        chbLine.setEnabled(false);
        updateEnableLine();
        txtLine.setText("");

        chbBlock.setSelected(false);
        chbBlock.setEnabled(false);
        updateEnableBlock();
        txtBlockStart.setText("");
        txtBlockEnd.setText("");
    }

    private void updateListModel() {
        int index = mimeIdentifiers.isEmpty() ? -1 : 0;
        updateListModel(index);
    }

    private void updateListModel(int indextToSelect) {
        identifierModel = new IdentifierModel();
        listIdentifiers.setModel(identifierModel);
        listIdentifiers.setSelectedIndex(indextToSelect);
        listIdentifiers.ensureIndexIsVisible(indextToSelect);
    }

    private void addSelectedToModel(List<FileIdentifier> selectedIdentifiers) {
        boolean isMime = true;
        for (FileIdentifier fileIdentifier : selectedIdentifiers) {
        isMime = fileIdentifier.getType().equals(FileIdentifier.Type.MIME);
            if (isMime) {
                mimeIdentifiers.add((MimeIdentifier) fileIdentifier);
            } else {
                extensionIdentifiers.add((ExtensionIdentifier) fileIdentifier);
            }
        }
        int indexToSelect = isMime ? mimeIdentifiers.size() : mimeIdentifiers.size() + extensionIdentifiers.size() + 1;
        updateListModel(indexToSelect);

        boolean oldChanged = changed;
        changed = true;
        firePropertyChange(OptionsPanelController.PROP_CHANGED, oldChanged, true);
        firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    private List<MimeIdentifier> loadMimeTypes() {
        Collection<String> mimeTypes = readMimeTypes();
        List<MimeIdentifier> mimeItems = new ArrayList<MimeIdentifier>(mimeTypes.size());
        for (String mimeType : mimeTypes) {
            MimeIdentifier mimeItem = new MimeIdentifier(mimeType, getLoaderDisplayName(mimeType));
            mimeItems.add(mimeItem);
        }
        Collections.sort(mimeItems);
        return mimeItems;
    }

    private Collection<String> readMimeTypes() {
        List<String> mimeTypes = new ArrayList<String>();
        FileObject[] children = FileUtil.getConfigFile("Loaders").getChildren();  //NOI18N
        for (int i = 0; i < children.length; i++) {
            FileObject child = children[i];
            String mime1 = child.getNameExt();
            FileObject[] subchildren = child.getChildren();
            for (int j = 0; j < subchildren.length; j++) {
                FileObject subchild = subchildren[j];
                FileObject factoriesFO = subchild.getFileObject("Factories");  //NOI18N
                if (factoriesFO != null && factoriesFO.getChildren().length > 0) {
                    // add only MIME types where some loader exists
                    mimeTypes.add(mime1 + "/" + subchild.getNameExt()); //NOI18N
                }
            }
        }
        mimeTypes.remove("content/unknown"); //NOI18N
        return mimeTypes;
    }

    private static String getLoaderDisplayName(String mimeType) {
        FileSystem filesystem = null;
        try {
            filesystem = FileUtil.getConfigRoot().getFileSystem();
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
        FileObject factoriesFO = FileUtil.getConfigFile("Loaders/" + mimeType + "/Factories");  //NOI18N
        if (factoriesFO != null) {
            FileObject[] children = factoriesFO.getChildren();
            for (FileObject child : children) {
                String childName = child.getNameExt();
                String displayName = filesystem.getStatus().annotateName(childName, Collections.singleton(child));
                if (!childName.equals(displayName)) {
                    return displayName;
                }
            }
        }
        return null;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblExtensions1 = new javax.swing.JLabel();
        sepExtension1 = new javax.swing.JSeparator();
        btnAdd = new javax.swing.JButton();
        btnChange = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new MyTable();
        checkScanCommentsOnly = new javax.swing.JCheckBox();
        btnRemove = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        listIdentifiers = new javax.swing.JList();
        lblExtensions = new javax.swing.JLabel();
        sepExtension = new javax.swing.JSeparator();
        lblTodo = new javax.swing.JLabel();
        sepTodo = new javax.swing.JSeparator();
        btnAddMime = new javax.swing.JButton();
        btnRemoveMime = new javax.swing.JButton();
        pnlExtensionDetails = new javax.swing.JPanel();
        lblBlockEnd = new javax.swing.JLabel();
        lblBlockStart = new javax.swing.JLabel();
        txtBlockEnd = new javax.swing.JTextField();
        chbLine = new javax.swing.JCheckBox();
        chbBlock = new javax.swing.JCheckBox();
        txtLine = new javax.swing.JTextField();
        txtBlockStart = new javax.swing.JTextField();
        lblMimeName = new javax.swing.JLabel();
        sepExtension2 = new javax.swing.JSeparator();
        lblLine = new javax.swing.JLabel();
        lblError = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(lblExtensions1, org.openide.util.NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.lblExtensions1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnAdd, org.openide.util.NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.btnAdd.text")); // NOI18N
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnChange, org.openide.util.NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.btnChange.text")); // NOI18N
        btnChange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangeActionPerformed(evt);
            }
        });

        jScrollPane1.setOpaque(false);

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        table.setOpaque(false);
        table.setTableHeader(null);
        jScrollPane1.setViewportView(table);

        org.openide.awt.Mnemonics.setLocalizedText(checkScanCommentsOnly, org.openide.util.NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.checkScanCommentsOnly.text")); // NOI18N
        checkScanCommentsOnly.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        checkScanCommentsOnly.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkScanCommentsOnly.setOpaque(false);
        checkScanCommentsOnly.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                scanCommentsOnlyChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnRemove, org.openide.util.NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.btnRemove.text")); // NOI18N
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        listIdentifiers.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1aaaaa", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        listIdentifiers.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listIdentifiers.setMinimumSize(new java.awt.Dimension(120, 80));
        jScrollPane2.setViewportView(listIdentifiers);

        org.openide.awt.Mnemonics.setLocalizedText(lblExtensions, org.openide.util.NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.lblExtensions.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblTodo, org.openide.util.NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.lblTodo.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnAddMime, NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.btnAddMime.text")); // NOI18N
        btnAddMime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddMimeActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnRemoveMime, NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.btnRemoveMime.text")); // NOI18N
        btnRemoveMime.setEnabled(false);
        btnRemoveMime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveMimeActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblBlockEnd, NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.lblBlockEnd.text")); // NOI18N
        lblBlockEnd.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(lblBlockStart, NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.lblBlockStart.text")); // NOI18N
        lblBlockStart.setEnabled(false);

        txtBlockEnd.setEnabled(false);
        txtBlockEnd.setMinimumSize(new java.awt.Dimension(100, 20));

        org.openide.awt.Mnemonics.setLocalizedText(chbLine, NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.chbLine.text")); // NOI18N
        chbLine.setEnabled(false);
        chbLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chbLineActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(chbBlock, NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.chbBlock.text")); // NOI18N
        chbBlock.setEnabled(false);
        chbBlock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chbBlockActionPerformed(evt);
            }
        });

        txtLine.setEnabled(false);
        txtLine.setMinimumSize(new java.awt.Dimension(100, 20));

        txtBlockStart.setEnabled(false);
        txtBlockStart.setMinimumSize(new java.awt.Dimension(100, 20));

        lblMimeName.setFont(lblMimeName.getFont());
        org.openide.awt.Mnemonics.setLocalizedText(lblMimeName, NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.lblMimeName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblLine, NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.lblLine.text")); // NOI18N
        lblLine.setEnabled(false);

        javax.swing.GroupLayout pnlExtensionDetailsLayout = new javax.swing.GroupLayout(pnlExtensionDetails);
        pnlExtensionDetails.setLayout(pnlExtensionDetailsLayout);
        pnlExtensionDetailsLayout.setHorizontalGroup(
            pnlExtensionDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlExtensionDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlExtensionDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlExtensionDetailsLayout.createSequentialGroup()
                        .addComponent(chbLine)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(pnlExtensionDetailsLayout.createSequentialGroup()
                        .addComponent(chbBlock)
                        .addGap(243, 243, 243))
                    .addGroup(pnlExtensionDetailsLayout.createSequentialGroup()
                        .addComponent(lblMimeName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sepExtension2))
                    .addGroup(pnlExtensionDetailsLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(pnlExtensionDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblBlockStart)
                            .addComponent(lblBlockEnd)
                            .addComponent(lblLine))
                        .addGap(18, 18, 18)
                        .addGroup(pnlExtensionDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtBlockStart, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtLine, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtBlockEnd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        pnlExtensionDetailsLayout.setVerticalGroup(
            pnlExtensionDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlExtensionDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlExtensionDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(sepExtension2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMimeName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chbLine)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlExtensionDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtLine, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblLine))
                .addGap(18, 18, 18)
                .addComponent(chbBlock)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlExtensionDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBlockStart)
                    .addComponent(txtBlockStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlExtensionDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBlockEnd)
                    .addComponent(txtBlockEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblError.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/tasklist/todo/settings/error.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblError, NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.lblError.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(lblExtensions)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sepExtension))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(lblTodo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sepTodo))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btnChange, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnAdd, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnRemove, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane2)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(checkScanCommentsOnly)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(btnAddMime, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnRemoveMime)))
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pnlExtensionDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(lblError, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(sepTodo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTodo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnChange)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemove))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkScanCommentsOnly)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(sepExtension, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblExtensions))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlExtensionDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAddMime)
                            .addComponent(btnRemoveMime))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblError))
        );
    }// </editor-fold>//GEN-END:initComponents

private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
    TableCellEditor editor = table.getCellEditor();
    if( null != editor )
        editor.cancelCellEditing();
    
    boolean wasValid = isDataValid();
    
    int selRow = table.getSelectedRow();
    if( selRow < 0 )
        return;
    DefaultTableModel model = (DefaultTableModel)table.getModel();
    model.removeRow( selRow );
    if( selRow > model.getRowCount()-1 )
        selRow--;
    if( selRow >= 0 )
        table.getSelectionModel().setSelectionInterval( selRow, selRow );
    
    boolean wasChanged = changed;
    changed = true;
    firePropertyChange( OptionsPanelController.PROP_CHANGED, new Boolean(wasChanged), Boolean.TRUE);
    
    firePropertyChange( OptionsPanelController.PROP_VALID, new Boolean(wasValid), new Boolean(isDataValid()));
}//GEN-LAST:event_btnRemoveActionPerformed

private void btnChangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangeActionPerformed
    int selRow = table.getSelectedRow();
    if( selRow < 0 )
        return;
    final boolean wasChanged = changed;
    table.editCellAt( selRow, 0 );
    final TableCellEditor editor = table.getCellEditor();
    editor.addCellEditorListener( new CellEditorListener() {
        public void editingStopped(ChangeEvent e) {
            editor.removeCellEditorListener( this );
            changed = true;
            firePropertyChange( OptionsPanelController.PROP_CHANGED, new Boolean(wasChanged), Boolean.TRUE);
            firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
        }

        public void editingCanceled(ChangeEvent e) {
            editor.removeCellEditorListener( this );
        }
    });
}//GEN-LAST:event_btnChangeActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        DefaultTableModel model = (DefaultTableModel)table.getModel();
        model.addRow( new Object[] { getDummyPattern( ) } ); //NOI18N
        table.getSelectionModel().setSelectionInterval( model.getRowCount()-1, model.getRowCount()-1 );
        final boolean wasChanged = changed;
        table.editCellAt( model.getRowCount()-1, 0 );
        final TableCellEditor editor = table.getCellEditor();
        editor.addCellEditorListener( new CellEditorListener() {
            public void editingStopped(ChangeEvent e) {
                editor.removeCellEditorListener( this );
                changed = true;
                firePropertyChange( OptionsPanelController.PROP_CHANGED, new Boolean(wasChanged), Boolean.TRUE);
                firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
            }

            public void editingCanceled(ChangeEvent e) {
                editor.removeCellEditorListener( this );
            }
        });
}//GEN-LAST:event_btnAddActionPerformed

private void scanCommentsOnlyChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_scanCommentsOnlyChanged
    if( isUpdating )
        return;
    boolean wasChanged = changed;
    changed = true;
    firePropertyChange( OptionsPanelController.PROP_CHANGED, new Boolean(wasChanged), Boolean.TRUE);
}//GEN-LAST:event_scanCommentsOnlyChanged

    private void btnAddMimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddMimeActionPerformed
        List<MimeIdentifier> allMimeItems = loadMimeTypes();
        allMimeItems.removeAll(mimeIdentifiers);
        IdentifierPickerPanel picker = new IdentifierPickerPanel(allMimeItems, extensionIdentifiers);
        final NotifyDescriptor extensionDialog = new NotifyDescriptor(
                picker,
                NbBundle.getMessage(ToDoCustomizer.class, "LBL_PickMime"), //NOI18N
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE,
                null,
                NotifyDescriptor.OK_OPTION);
        
        picker.addValidityListener(extensionDialog);
        if (DialogDisplayer.getDefault().notify(extensionDialog) == NotifyDescriptor.OK_OPTION) {
            addSelectedToModel(picker.getSelectedMimeTypes());
        }
    }//GEN-LAST:event_btnAddMimeActionPerformed

    private void btnRemoveMimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveMimeActionPerformed
        int selected = listIdentifiers.getSelectedIndex();
        if (selected == 0 || selected == mimeIdentifiers.size() + 1) {
            return;
        } else if (selected < mimeIdentifiers.size() + 1) {
            mimeIdentifiers.remove(selected - 1);
        } else {
            extensionIdentifiers.remove(selected - mimeIdentifiers.size() - 2);
        }
        int totalSize = mimeIdentifiers.size() + extensionIdentifiers.size() + 2;
        updateListModel(selected < totalSize ? selected : totalSize - 1);
        boolean oldChanged = changed;
        changed = true;
        firePropertyChange(OptionsPanelController.PROP_CHANGED, oldChanged, true);
        firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }//GEN-LAST:event_btnRemoveMimeActionPerformed

    private void chbLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chbLineActionPerformed
        updateEnableLine();
        validateDetails();
    }//GEN-LAST:event_chbLineActionPerformed

    private void chbBlockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chbBlockActionPerformed
        updateEnableBlock();
        validateDetails();
    }//GEN-LAST:event_chbBlockActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnAddMime;
    private javax.swing.JButton btnChange;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnRemoveMime;
    private javax.swing.JCheckBox chbBlock;
    private javax.swing.JCheckBox chbLine;
    private javax.swing.JCheckBox checkScanCommentsOnly;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblBlockEnd;
    private javax.swing.JLabel lblBlockStart;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblExtensions;
    private javax.swing.JLabel lblExtensions1;
    private javax.swing.JLabel lblLine;
    private javax.swing.JLabel lblMimeName;
    private javax.swing.JLabel lblTodo;
    private javax.swing.JList listIdentifiers;
    private javax.swing.JPanel pnlExtensionDetails;
    private javax.swing.JSeparator sepExtension;
    private javax.swing.JSeparator sepExtension1;
    private javax.swing.JSeparator sepExtension2;
    private javax.swing.JSeparator sepTodo;
    private javax.swing.JTable table;
    private javax.swing.JTextField txtBlockEnd;
    private javax.swing.JTextField txtBlockStart;
    private javax.swing.JTextField txtLine;
    // End of variables declaration//GEN-END:variables

    @Override
    public void insertUpdate(DocumentEvent e) {
        validateDetails();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        validateDetails();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        validateDetails();
    }

    private void validateDetails() {
        boolean oldValid = detailsValid;
        boolean oldChanged = changed;
        detailsValid = saveDetails(selectedIndex);
        changed = true;
        if (!oldChanged) {
            firePropertyChange(OptionsPanelController.PROP_CHANGED, oldChanged, true);
        }
        if (oldValid != detailsValid) {
            firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
        }
    }

    private void updateEnableBlock() {
        txtBlockStart.setEnabled(chbBlock.isSelected());
        lblBlockStart.setEnabled(chbBlock.isSelected());
        txtBlockEnd.setEnabled(chbBlock.isSelected());
        lblBlockEnd.setEnabled(chbBlock.isSelected());
    }

    private void updateEnableLine() {
        txtLine.setEnabled(chbLine.isSelected());
        lblLine.setEnabled(chbLine.isSelected());
    }

    private void addDocumentListeners() {
        txtLine.getDocument().addDocumentListener(this);
        txtBlockStart.getDocument().addDocumentListener(this);
        txtBlockEnd.getDocument().addDocumentListener(this);
    }

    private void removeDocumentListeners() {
        txtLine.getDocument().removeDocumentListener(this);
        txtBlockStart.getDocument().removeDocumentListener(this);
        txtBlockEnd.getDocument().removeDocumentListener(this);
    }

    private String getDummyPattern() {
        return NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.DefaultPattern");
    }

    private static class MyTable extends JTable {

        @Override
        public Component prepareEditor(TableCellEditor editor, int row, int column) {
            Component res = super.prepareEditor( editor, row, column );
            if( res instanceof JTextComponent ) {
                final JTextComponent txt = (JTextComponent)res;
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        txt.selectAll();
                        txt.requestFocusInWindow();
                    }
                });
            }
            return res;
        }
        
    }

    private class IdentifierModel extends AbstractListModel {

        @Override
        public int getSize() {
            return mimeIdentifiers.size() + extensionIdentifiers.size() + 2;
        }

        @Override
        public Object getElementAt(int index) {
            if (index == 0) {
                return "---------- " + FileIdentifier.Type.MIME.getDisplayName() + " ----------"; //NOI18N
            }
            if (index < mimeIdentifiers.size() + 1) {
                return mimeIdentifiers.get(index - 1);
            }
            if (index == mimeIdentifiers.size() + 1) {
                return "---------- " + FileIdentifier.Type.EXTENSION.getDisplayName() + " ----------"; //NOI18N
            }
            return extensionIdentifiers.get(index - mimeIdentifiers.size() - 2);
        }

    }

    private static class IdentifierRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (comp instanceof JLabel) {
                JLabel jLabel = (JLabel) comp;
                if (value instanceof FileIdentifier) {
                    FileIdentifier identifier = (FileIdentifier) value;
                    if (identifier.isValid()) {
                        jLabel.setIcon(EMPTY_ICON);
                    } else {
                        jLabel.setIcon(NOT_VALID_ICON);
                    }
                } else if (!isSelected) {
                    ((JLabel) comp).setForeground(UIManager.getColor("Label.disabledForeground"));
                }
            }
            return comp;
        }
    }

    private static class EmptyIcon implements Icon {

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
        }

        @Override
        public int getIconWidth() {
            return NOT_VALID_ICON.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return NOT_VALID_ICON.getIconHeight();
        }
    }
}
