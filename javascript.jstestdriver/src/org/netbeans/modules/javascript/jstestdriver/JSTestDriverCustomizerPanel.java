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
package org.netbeans.modules.javascript.jstestdriver;

import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.WebBrowsers;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;

/**
 *
 */
public class JSTestDriverCustomizerPanel extends javax.swing.JPanel implements DocumentListener {

    private static final String LOCATION = "location";
    private static final String USE_BROWSER = "use.browser.";
    private static final String PORT = "port";
    private static final String STRICT_MODE = "strict.mode";

    private DialogDescriptor descriptor;
    
    /**
     * Creates new form JSTestDriverCustomizerPanel
     */
    public JSTestDriverCustomizerPanel() {
        initComponents();
        String l = getPersistedLocation();
        jLocationTextField.setText(l != null ? l : "");
        jLocationTextField.getDocument().addDocumentListener(this);
        jBrowsersTable.setModel(new BrowsersTableModel());
        jBrowsersTable.setDefaultRenderer(TableRow.class, new TableRowCellRenderer());
        initTableVisualProperties(jBrowsersTable);
    }
    
    private void initTableVisualProperties(JTable table) {
        table.setRowSelectionAllowed(true);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setTableHeader(null);
        table.setRowHeight(jBrowsersTable.getRowHeight() + 4);        
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));        
        // set the color of the table's JViewport
        table.getParent().setBackground(table.getBackground());
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.getColumnModel().getColumn(0).setMaxWidth(30);
    }
    
    
    private static String getPersistedLocation() {
        return NbPreferences.forModule(JSTestDriverCustomizerPanel.class).get(LOCATION, null);
    }

    private void setDescriptor(DialogDescriptor descriptor) {
        this.descriptor = descriptor;
        updateValidity();
    }
    
    private void updateValidity() {
        descriptor.setValid(isValidJSTestDriverJar(jLocationTextField.getText()));
    }

    private static boolean isValidJSTestDriverJar(String s) {
        if (s == null) {
            return false;
        }
        File f = new File(s);
        return (f.exists() && isValidFileName(f));
    }
    
    private static boolean isValidFileName(File f) {
        return (f.getName().toLowerCase().startsWith("jstestdriver") &&
                f.getName().toLowerCase().endsWith(".jar"));
    }

    public static boolean showCustomizer() {
        JSTestDriverCustomizerPanel panel = new JSTestDriverCustomizerPanel();
        DialogDescriptor descriptor = new DialogDescriptor(panel, 
                "Configure JSTestDriver installation and its startup");
        panel.setDescriptor(descriptor);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setModal(true);
        dialog.setVisible(true);
        dialog.dispose();
        if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
            Preferences prefs = NbPreferences.forModule(JSTestDriverCustomizerPanel.class);
            prefs.put(LOCATION, panel.jLocationTextField.getText());
            int port = 42442;
            try {
                port = Integer.parseInt(panel.jPortTextField.getText());
            } catch (NumberFormatException ex) {
                // ignore
            }
            prefs.putInt(PORT, port);
            prefs.putBoolean(STRICT_MODE, panel.jStrictCheckBox.isSelected());
            for (TableRow row : ((BrowsersTableModel)panel.jBrowsersTable.getModel()).model) {
                prefs.putBoolean(USE_BROWSER+row.getBrowser().getId(), row.isSelected());
            }
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isConfiguredProperly() {
        String l = getPersistedLocation();
        return isValidJSTestDriverJar(l);
    }
    
    public static String getJSTestDriverJar() {
        return getPersistedLocation();
    }

    public static String getServerURL() {
        return "http://localhost:" + getPort();
    }

    public static boolean isStricModel() {
        return NbPreferences.forModule(JSTestDriverCustomizerPanel.class).getBoolean(STRICT_MODE, false);
    }

    public static int getPort() {
        return NbPreferences.forModule(JSTestDriverCustomizerPanel.class).getInt(PORT, 42442);
    }

    private static List<TableRow> createModel() {
        List<TableRow> model = new ArrayList<TableRow>();
        for (WebBrowser browser : WebBrowsers.getInstance().getAll(false)) {
            if (browser.isEmbedded()) {
                continue;
            }
            model.add(new TableRow(browser, 
                NbPreferences.forModule(JSTestDriverCustomizerPanel.class).getBoolean(USE_BROWSER+browser.getId(), true)));
        }
        return model;
    }
    
    public static List<WebBrowser> getBrowsers() {
        List<TableRow> model = createModel();
        List<WebBrowser> res = new ArrayList<WebBrowser>();
        for (TableRow row : model) {
            if (row.isSelected()) {
                res.add(row.getBrowser());
            }
        }
        return res;
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
        jLocationTextField = new javax.swing.JTextField();
        jBrowseButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jBrowsersTable = new javax.swing.JTable();
        jStrictCheckBox = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jPortTextField = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(JSTestDriverCustomizerPanel.class, "JSTestDriverCustomizerPanel.jLabel1.text")); // NOI18N

        jLocationTextField.setText(org.openide.util.NbBundle.getMessage(JSTestDriverCustomizerPanel.class, "JSTestDriverCustomizerPanel.jLocationTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jBrowseButton, org.openide.util.NbBundle.getMessage(JSTestDriverCustomizerPanel.class, "JSTestDriverCustomizerPanel.jBrowseButton.text")); // NOI18N
        jBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBrowseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(JSTestDriverCustomizerPanel.class, "JSTestDriverCustomizerPanel.jLabel2.text")); // NOI18N

        jScrollPane1.setViewportView(jBrowsersTable);

        org.openide.awt.Mnemonics.setLocalizedText(jStrictCheckBox, org.openide.util.NbBundle.getMessage(JSTestDriverCustomizerPanel.class, "JSTestDriverCustomizerPanel.jStrictCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(JSTestDriverCustomizerPanel.class, "JSTestDriverCustomizerPanel.jLabel3.text")); // NOI18N

        jPortTextField.setText(org.openide.util.NbBundle.getMessage(JSTestDriverCustomizerPanel.class, "JSTestDriverCustomizerPanel.jPortTextField.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLocationTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBrowseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jStrictCheckBox)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(50, 50, 50)
                                .addComponent(jPortTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jPortTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jStrictCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBrowseButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return !f.isFile() || isValidFileName(f);
            }
            @Override
            public String getDescription() {
                return "JsTestDriver*.jar";
            }
        });
        File file = new File(jLocationTextField.getText());
        if (jLocationTextField.getText().length() > 0 && file.exists()) {
            chooser.setSelectedFile(file);
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File selected = FileUtil.normalizeFile(chooser.getSelectedFile());
            jLocationTextField.setText(selected.getAbsolutePath());
        }
    }//GEN-LAST:event_jBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBrowseButton;
    private javax.swing.JTable jBrowsersTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField jLocationTextField;
    private javax.swing.JTextField jPortTextField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox jStrictCheckBox;
    // End of variables declaration//GEN-END:variables

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateValidity();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateValidity();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        updateValidity();
    }

    private static class TableRowCellRenderer extends DefaultTableCellRenderer {
        
        public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column ) {
            if (value instanceof TableRow) {
                TableRow item = (TableRow) value;
                return super.getTableCellRendererComponent(table, item.getBrowser().getName(), isSelected, false, row, column);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
        }
        
    }

    /** 
     * Implements a TableModel.
     */
    private static final class BrowsersTableModel extends AbstractTableModel {

        private List<TableRow> model;
        
        public BrowsersTableModel() {
            model = createModel();
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public int getRowCount() {
            return model.size();
        }
        
        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0)
                return Boolean.class;
            else
                return TableRow.class;
        }
        
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (columnIndex == 0);
        }
        
        public Object getValueAt(int row, int column) {
            TableRow item = getItem(row);
            switch (column) {
                case 0: return item.isSelected();
                case 1: return item;
            }
            return "";
        }
        
        public void setValueAt(Object value, int row, int column) {
            TableRow item = getItem(row);
            switch (column) {
                case 0: item.setSelected((Boolean) value);break;
            }
            fireTableCellUpdated(row, column);
        }
        
        private TableRow getItem(int index) {
            return (TableRow) model.get(index);
        }
        
    }

    private static final class TableRow {
        private WebBrowser browser;
        private boolean selected;

        public TableRow(WebBrowser browser, boolean selected) {
            this.browser = browser;
            this.selected = selected;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public WebBrowser getBrowser() {
            return browser;
        }
        
        
    }
    
}
