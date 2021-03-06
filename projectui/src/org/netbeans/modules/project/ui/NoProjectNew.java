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

package org.netbeans.modules.project.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.event.DocumentListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;


/**
 *
 * @author  phrebejk
 */
public class NoProjectNew extends javax.swing.JPanel implements  ActionListener, DocumentListener {
    
    public static final int TYPE_FILE = 0;
    public static final int TYPE_FOLDER = 1;
    
    private static final String FILE_NAME = NbBundle.getMessage( NoProjectNew.class, "LBL_NonProject_File_Name" ); // NOI18N
    private static final String FILE_TITLE = NbBundle.getMessage( NoProjectNew.class, "LBL_NonProject_File_Title" ); // NOI18N;
    private static final String FILE_TEXT_FIELD_ACD = NbBundle.getMessage( NoProjectNew.class, "ACD_NonProject_File_TextField" ); // NOI18N;
    private static final String FILE_DIALOG_ACD = NbBundle.getMessage( NoProjectNew.class, "ACD_NonProject_File_Dialog" ); // NOI18N;
    private static final String FOLDER_NAME = NbBundle.getMessage( NoProjectNew.class, "LBL_NonProject_Folder_Name" ); // NOI18N;
    private static final String FOLDER_TITLE = NbBundle.getMessage( NoProjectNew.class, "LBL_NonProject_Folder_Title" ); // NOI18N;    
    private static final String FOLDER_TEXT_FIELD_ACD = NbBundle.getMessage( NoProjectNew.class, "ACD_NonProject_Folder_TextField" ); // NOI18N;
    private static final String FOLDER_DIALOG_ACD = NbBundle.getMessage( NoProjectNew.class, "ACD_NonProject_Folder_Dialog" ); // NOI18N;
    
    public static final String COMMAND_OK = "OK";
    public static final String COMMAND_CANCEL = "CANCEL";
        
    private static DataObject[] templates;
    
    private int type;
    private DataFolder targetFolder;
    private String result;
    private JButton okOption;
    
    /** Creates new form BrowseFolders */
    public NoProjectNew( int type, DataFolder targetFolder, JButton okOption ) {
        initComponents();
        nameTextField.getDocument().addDocumentListener( this );
                
        this.type = type;        
        this.targetFolder = targetFolder;
        this.okOption = okOption;
        
        switch ( type ) { 
            case TYPE_FILE:
                org.openide.awt.Mnemonics.setLocalizedText(nameLabel, FILE_NAME);
                nameTextField.getAccessibleContext().setAccessibleDescription(FILE_TEXT_FIELD_ACD);
                getAccessibleContext().setAccessibleDescription(FILE_DIALOG_ACD);
                break;
            case TYPE_FOLDER:    
                org.openide.awt.Mnemonics.setLocalizedText(nameLabel, FOLDER_NAME);
                nameTextField.getAccessibleContext().setAccessibleDescription(FOLDER_TEXT_FIELD_ACD);
                getAccessibleContext().setAccessibleDescription(FOLDER_DIALOG_ACD);
                break;        
            }
                
        this.okOption.setEnabled( false );
        
    }        
            
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        nameLabel.setLabelFor(nameTextField);
        nameLabel.setText("Folders:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(nameLabel, gridBagConstraints);

        nameTextField.setColumns(25);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(nameTextField, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel nameLabel;
    public javax.swing.JTextField nameTextField;
    // End of variables declaration//GEN-END:variables
        
    public static void showDialog( DataObject template, DataFolder targetFolder ) {
        
        int type;
        if ( template.getPrimaryFile().getName().equals( "file") ) {
            type = TYPE_FILE;
        }
        else {
            type = TYPE_FOLDER;
        }
        
        JButton options[] = new JButton[] { 
            new JButton( NbBundle.getMessage( NoProjectNew.class, "LBL_NonProject_OK_Button") ), // NOI18N
            new JButton( NbBundle.getMessage( NoProjectNew.class, "LBL_NonProject_Cancel_Button") ), // NOI18N
        };
        
        NoProjectNew npn = new NoProjectNew( type, targetFolder, options[0] );
                               
        options[ 0 ].setActionCommand( COMMAND_OK );
        options[ 0 ].addActionListener( npn );
        options[ 1 ].setActionCommand( COMMAND_CANCEL );
        options[ 1 ].addActionListener( npn );    
        
        DialogDescriptor dialogDescriptor = new DialogDescriptor( 
            npn,                                            // innerPane
            type == TYPE_FILE ? FILE_TITLE : FOLDER_TITLE,   // displayName
            true,                                           // modal
            options,                                        // options
            options[ 0 ],                                   // initial value
            DialogDescriptor.BOTTOM_ALIGN,                  // options align
            null,                                           // helpCtx
            null );                                         // listener 

        dialogDescriptor.setClosingOptions( new Object[] { options[ 0 ], options[ 1 ] } );
            
        Dialog dialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );
        dialog.setVisible(true);
        npn.createFile();        
                
    }
    
    public static DataObject[] getTemplates() {
        
        if ( templates == null ) {
            
            ArrayList<DataObject> tList = new ArrayList<DataObject>( 2 );
            DataObject template;
            
            template = findTemplate( "Templates/Other/file" );
            if ( template != null ) {
                tList.add( template );
            }
                        
            template = findTemplate( "Templates/Other/Folder" ); 
            if ( template != null ) {
                tList.add( template );
            }
        
            templates = new DataObject[tList.size()]; 
            tList.toArray( templates );
        }
        return templates;
    }

    // ActionListener implementation -------------------------------------------
    
    @Override
    public void actionPerformed( ActionEvent e ) {
        result = COMMAND_OK.equals( e.getActionCommand() ) ? getFileName() : null;        
    }
    
    // Document listener implementation ----------------------------------------
    
    @Override
    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        changedUpdate( e );
    }    
    
    @Override
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        changedUpdate( e );
    }    
    
    @Override
    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        
        String fileName = getFileName();
        
        if ( fileName.length() == 0  ) {
            okOption.setEnabled( false );
            return;
        }
                
        FileObject fo = targetFolder.getPrimaryFile().getFileObject( fileName );
        if ( fo != null ) {
            okOption.setEnabled( false );
            return;
        }
        
        okOption.setEnabled( true );        
    }
        
    // Private methods ---------------------------------------------------------
    
    private static DataObject findTemplate( String name ) {
        FileObject tFo = FileUtil.getConfigFile( name );
        if ( tFo == null ) {
            return null;
        }
        try {
            return DataObject.find( tFo );
        }
        catch ( DataObjectNotFoundException e ) {
            return null;
        }
        
    }
    
    private String getFileName() {        
        String name = nameTextField.getText().trim();
        return name.replace( File.separatorChar, '/' ); // NOI18N
    }
    
    private void createFile() {
        if ( result != null && result.indexOf('\\') == -1 ) { // NOI18N
            
            if ( !targetFolder.getPrimaryFile().canWrite() ) {
                return;
            }
            
            DataObject dObj = null;
            
            try {
                FileObject fo = type == TYPE_FILE ? 
                    FileUtil.createData( targetFolder.getPrimaryFile(), result ) :
                    FileUtil.createFolder( targetFolder.getPrimaryFile(), result );
                if ( fo != null ) {
                    dObj = DataObject.find( fo );
                }
            }
            catch ( DataObjectNotFoundException e ) {
                // No data object no open
            }            
            catch ( IOException e ) {
                // XXX
            }
            
            if ( result != null ) {
                // handle new template in SystemFileSystem
                DataObject rootDO = findTemplate ("/Templates"); // NOI18N
                if (rootDO != null && dObj != null) {
                    if (FileUtil.isParentOf (rootDO.getPrimaryFile (), dObj.getPrimaryFile ())) {
                        try {
                            dObj.setTemplate (true);
                        } catch (IOException e) {
                            // can ignore
                        }
                    }
                }
            }
            if (dObj != null) {
                ProjectUtilities.openAndSelectNewObject( dObj );
            }
        }        
    }
    
}
