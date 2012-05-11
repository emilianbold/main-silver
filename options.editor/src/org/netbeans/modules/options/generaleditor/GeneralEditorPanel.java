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

package org.netbeans.modules.options.generaleditor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public class GeneralEditorPanel extends JPanel implements ActionListener {

    private boolean         changed = false;
    private boolean         listen = false;
    
    /** 
     * Creates new form GeneralEditorPanel.
     */
    public GeneralEditorPanel () {
        initComponents ();

        loc (lCodeFolding, "Code_Folding");
        loc (lUseCodeFolding, "Code_Folding_Section");
        loc (lCollapseByDefault, "Fold_by_Default");
            
        loc (cbUseCodeFolding, "Use_Folding");
        loc (cbFoldMethods, "Fold_Methods");
        loc (cbFoldInnerClasses, "Fold_Classes");
        loc (cbFoldImports, "Fold_Imports");
        loc (cbFoldJavadocComments, "Fold_JavaDoc");
        loc (cbFoldInitialComments, "Fold_Licence");
        loc (cbFoldTags, "Fold_Tags");

        loc (lCamelCaseBehavior, "Camel_Case_Behavior");
        loc (cbCamelCaseBehavior, "Enable_Camel_Case_In_Java");
        loc (lCamelCaseBehaviorExample, "Camel_Case_Behavior_Example");

        loc (lWhenSavingFiles, "When_Saving_Files");
        loc (lRemoveTrailingWhitespace, "Remove_Trailing_Whitespace");
        loc (cboRemoveTrailingWhitespace, "Remove_Trailing_Whitespace");
        loc (lSearch, "Search");
        loc (lEditorSearchType, "Editor_Search_Type");
        loc (cboEditorSearchType, "Editor_Search_Type");
                
        cbUseCodeFolding.setMnemonic(NbBundle.getMessage (GeneralEditorPanel.class, "MNEMONIC_Use_Folding").charAt(0));
        cboRemoveTrailingWhitespace.setRenderer(new RemoveTrailingWhitespaceRenderer(cboRemoveTrailingWhitespace.getRenderer()));
        cboRemoveTrailingWhitespace.setModel(new DefaultComboBoxModel(new Object [] { "never", "always", "modified-lines" })); //NOI18N
        cboEditorSearchType.setRenderer(new EditorSearchTypeRenderer(cboEditorSearchType.getRenderer()));
        cboEditorSearchType.setModel(new DefaultComboBoxModel(new Object [] { "default", "closing"})); //NOI18N
        cboEditorSearchType.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (cboEditorSearchType.getSelectedItem().equals("default"))
                    Mnemonics.setLocalizedText(lSearchtypeTooltip,  NbBundle.getMessage (GeneralEditorPanel.class, "Editor_Search_Type_Tooltip_default"));
                else
                    Mnemonics.setLocalizedText(lSearchtypeTooltip,  NbBundle.getMessage (GeneralEditorPanel.class, "Editor_Search_Type_Tooltip_closing"));
            }
        });
        Mnemonics.setLocalizedText(lSearchtypeTooltip,  NbBundle.getMessage (GeneralEditorPanel.class, "Editor_Search_Type_Tooltip_closing"));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lCodeFolding = new javax.swing.JLabel();
        lUseCodeFolding = new javax.swing.JLabel();
        lCollapseByDefault = new javax.swing.JLabel();
        cbUseCodeFolding = new javax.swing.JCheckBox();
        cbFoldMethods = new javax.swing.JCheckBox();
        cbFoldInnerClasses = new javax.swing.JCheckBox();
        cbFoldImports = new javax.swing.JCheckBox();
        cbFoldJavadocComments = new javax.swing.JCheckBox();
        cbFoldInitialComments = new javax.swing.JCheckBox();
        cbFoldTags = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        lCamelCaseBehavior = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        cbCamelCaseBehavior = new javax.swing.JCheckBox();
        lCamelCaseBehaviorExample = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        lWhenSavingFiles = new javax.swing.JLabel();
        lRemoveTrailingWhitespace = new javax.swing.JLabel();
        cboRemoveTrailingWhitespace = new javax.swing.JComboBox();
        lEditorSearchType = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        lSearch = new javax.swing.JLabel();
        cboEditorSearchType = new javax.swing.JComboBox();
        lSearchtypeTooltip = new javax.swing.JLabel();

        setForeground(new java.awt.Color(99, 130, 191));

        lCodeFolding.setText("Code Folding");

        lUseCodeFolding.setLabelFor(cbUseCodeFolding);
        lUseCodeFolding.setText("Use Code Folding:");

        lCollapseByDefault.setText("Collapse by Default:");

        cbFoldMethods.setText("Methods");

        cbFoldInnerClasses.setText("Inner Classes");

        cbFoldImports.setText("Imports");

        cbFoldJavadocComments.setText("Javadoc Comments");

        cbFoldInitialComments.setText("Initial Comments");

        cbFoldTags.setText("Tags and Other Code Blocks");

        lCamelCaseBehavior.setText("Camel Case  Behavior");

        cbCamelCaseBehavior.setText("Enable Camel Case Navigation");

        lCamelCaseBehaviorExample.setText("<html>Example: Caret stops at J, T, N in \"JavaTypeName\"<br>when using next/previous word acctions</html>");

        lWhenSavingFiles.setText("When Saving Files");

        lRemoveTrailingWhitespace.setLabelFor(cboRemoveTrailingWhitespace);
        lRemoveTrailingWhitespace.setText("Remove Trailing Whitespace:");

        cboRemoveTrailingWhitespace.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lEditorSearchType.setLabelFor(cboRemoveTrailingWhitespace);
        lEditorSearchType.setText("Editor Search Type:");

        lSearch.setText("Search");

        cboEditorSearchType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lSearchtypeTooltip.setText("<html>In Closing type Enter accepts search match, Esc jumps to start. Both close searchbar. <br /> Default type closes searchbar by Esc or button. Enter means find a new instance.</html>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lWhenSavingFiles)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4))
            .addGroup(layout.createSequentialGroup()
                .addComponent(lCamelCaseBehavior)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3))
            .addGroup(layout.createSequentialGroup()
                .addComponent(lCodeFolding)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(lSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator5))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lUseCodeFolding)
                                    .addComponent(lCollapseByDefault)
                                    .addComponent(lRemoveTrailingWhitespace))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lCamelCaseBehaviorExample, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cboRemoveTrailingWhitespace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGap(155, 155, 155)
                                .addComponent(cbCamelCaseBehavior))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGap(155, 155, 155)
                                .addComponent(cbFoldImports)
                                .addGap(18, 18, 18)
                                .addComponent(cbFoldTags))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGap(155, 155, 155)
                                .addComponent(cbFoldInnerClasses)
                                .addGap(18, 18, 18)
                                .addComponent(cbFoldInitialComments))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGap(155, 155, 155)
                                .addComponent(cbFoldMethods)
                                .addGap(18, 18, 18)
                                .addComponent(cbFoldJavadocComments))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGap(155, 155, 155)
                                .addComponent(cbUseCodeFolding)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap(24, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lEditorSearchType)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lSearchtypeTooltip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboEditorSearchType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cbFoldImports, cbFoldInitialComments, cbFoldInnerClasses, cbFoldJavadocComments, cbFoldMethods});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lCodeFolding)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lUseCodeFolding)
                    .addComponent(cbUseCodeFolding))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lCollapseByDefault)
                    .addComponent(cbFoldMethods)
                    .addComponent(cbFoldJavadocComments))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbFoldInnerClasses)
                            .addComponent(cbFoldInitialComments))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbFoldImports)
                            .addComponent(cbFoldTags))
                        .addGap(18, 18, 18)
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lCamelCaseBehavior))
                .addGap(2, 2, 2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cbCamelCaseBehavior)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lCamelCaseBehaviorExample, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lWhenSavingFiles))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lRemoveTrailingWhitespace)
                    .addComponent(cboRemoveTrailingWhitespace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lSearch)
                    .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lEditorSearchType)
                    .addComponent(cboEditorSearchType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(lSearchtypeTooltip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbCamelCaseBehavior;
    private javax.swing.JCheckBox cbFoldImports;
    private javax.swing.JCheckBox cbFoldInitialComments;
    private javax.swing.JCheckBox cbFoldInnerClasses;
    private javax.swing.JCheckBox cbFoldJavadocComments;
    private javax.swing.JCheckBox cbFoldMethods;
    private javax.swing.JCheckBox cbFoldTags;
    private javax.swing.JCheckBox cbUseCodeFolding;
    private javax.swing.JComboBox cboEditorSearchType;
    private javax.swing.JComboBox cboRemoveTrailingWhitespace;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JLabel lCamelCaseBehavior;
    private javax.swing.JLabel lCamelCaseBehaviorExample;
    private javax.swing.JLabel lCodeFolding;
    private javax.swing.JLabel lCollapseByDefault;
    private javax.swing.JLabel lEditorSearchType;
    private javax.swing.JLabel lRemoveTrailingWhitespace;
    private javax.swing.JLabel lSearch;
    private javax.swing.JLabel lSearchtypeTooltip;
    private javax.swing.JLabel lUseCodeFolding;
    private javax.swing.JLabel lWhenSavingFiles;
    // End of variables declaration//GEN-END:variables
    
    
    private static String loc (String key) {
        return NbBundle.getMessage (GeneralEditorPanel.class, key);
    }
    
    private static void loc (Component c, String key) {
        if (!(c instanceof JLabel)) {
            c.getAccessibleContext ().setAccessibleName (loc ("AN_" + key));
            c.getAccessibleContext ().setAccessibleDescription (loc ("AD_" + key));
        }
        if (c instanceof AbstractButton) {
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc ("CTL_" + key)
            );
        } else if (c instanceof JLabel) {
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc ("CTL_" + key)
            );
        }
    }
    
    private Model model;
    
    void update () {
        listen = false;
        if (model == null) {
            model = new Model ();
            cbUseCodeFolding.addActionListener (this);
            cbFoldMethods.addActionListener (this);
            cbFoldInnerClasses.addActionListener (this);
            cbFoldImports.addActionListener (this);
            cbFoldJavadocComments.addActionListener (this);
            cbFoldInitialComments.addActionListener (this);
            cbCamelCaseBehavior.addActionListener (this);
            cbFoldTags.addActionListener (this);
            cboRemoveTrailingWhitespace.addActionListener(this);
            cboEditorSearchType.addActionListener(this);
        }
        
        // init code folding
        cbUseCodeFolding.setSelected (model.isShowCodeFolding ());
        cbFoldImports.setSelected (model.isFoldImports ());
        cbFoldInitialComments.setSelected (model.isFoldInitialComment ());
        cbFoldInnerClasses.setSelected (model.isFoldInnerClasses ());
        cbFoldJavadocComments.setSelected (model.isFoldJavaDocComments ());
        cbFoldMethods.setSelected (model.isFoldMethods ());
        cbFoldTags.setSelected (model.isFoldTag());

        // Java Camel Case Navigation
        Boolean ccJava = model.isCamelCaseJavaNavigation();
        if ( ccJava == null ) {
            cbCamelCaseBehavior.setEnabled(false);
            cbCamelCaseBehavior.setSelected(false);            
        }
        else {
            cbCamelCaseBehavior.setEnabled(true);
            cbCamelCaseBehavior.setSelected(ccJava);
        }

        // when saving files section
        cboRemoveTrailingWhitespace.setSelectedItem(model.getRemoveTrailingWhitespace());
        
        cboEditorSearchType.setSelectedItem(model.getEditorSearchType());

        updateEnabledState ();
        
        listen = true;
    }
    
    void applyChanges () {
        
        if (model == null || !changed) return;
        
        // code folding options
        model.setFoldingOptions (
            cbUseCodeFolding.isSelected (),
            cbFoldImports.isSelected (),
            cbFoldInitialComments.isSelected (),
            cbFoldInnerClasses.isSelected (),
            cbFoldJavadocComments.isSelected (),
            cbFoldMethods.isSelected (),
            cbFoldTags.isSelected ()
        );
        
        // java camel case navigation
        model.setCamelCaseNavigation(cbCamelCaseBehavior.isSelected());
        
        // when saving files section
        model.setRemoveTrailingWhitespace((String)cboRemoveTrailingWhitespace.getSelectedItem());
        
        model.setEditorSearchType((String)cboEditorSearchType.getSelectedItem());

        changed = false;
    }
    
    void cancel () {
        changed = false;
    }
    
    boolean dataValid () {
        return true;
    }
    
    boolean isChanged () {
        return changed;
    }
    
    @Override
    public void actionPerformed (ActionEvent e) {
        if (!listen) return;
        if (e.getSource () == cbUseCodeFolding) {
            updateEnabledState ();
        }
        changed = true;
    }
    
    
    // other methods ...........................................................
    
    private void updateEnabledState () {
        boolean useCodeFolding = cbUseCodeFolding.isSelected ();
        cbFoldImports.setEnabled (useCodeFolding);
        cbFoldInitialComments.setEnabled (useCodeFolding);
        cbFoldInnerClasses.setEnabled (useCodeFolding);
        cbFoldJavadocComments.setEnabled (useCodeFolding);
        cbFoldMethods.setEnabled (useCodeFolding);
        cbFoldTags.setEnabled(useCodeFolding);
    }

    private static final class RemoveTrailingWhitespaceRenderer implements ListCellRenderer {

        private final ListCellRenderer defaultRenderer;

        public RemoveTrailingWhitespaceRenderer(ListCellRenderer defaultRenderer) {
            this.defaultRenderer = defaultRenderer;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return defaultRenderer.getListCellRendererComponent(
                    list,
                    NbBundle.getMessage(GeneralEditorPanel.class, "RTW_" + value), //NOI18N
                    index,
                    isSelected,
                    cellHasFocus);
        }

    } // End of RemoveTrailingWhitespaceRendererRenderer class
    
    private static final class EditorSearchTypeRenderer implements ListCellRenderer {

        private final ListCellRenderer defaultRenderer;

        public EditorSearchTypeRenderer(ListCellRenderer defaultRenderer) {
            this.defaultRenderer = defaultRenderer;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return defaultRenderer.getListCellRendererComponent(
                    list,
                    NbBundle.getMessage(GeneralEditorPanel.class, "EST_" + value), //NOI18N
                    index,
                    isSelected,
                    cellHasFocus);
        }

    }
}
