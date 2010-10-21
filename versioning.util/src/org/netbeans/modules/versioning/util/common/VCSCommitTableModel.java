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

package org.netbeans.modules.versioning.util.common;

import org.openide.util.NbBundle;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.io.File;

/**
 * Table model for the Commit dialog table.
 *
 * @author Tomas Stupka
 */
public class VCSCommitTableModel extends AbstractTableModel {

    public static final String COLUMN_NAME_COMMIT  = "commit"; // NOI18N
    public static final String COLUMN_NAME_NAME    = "name"; // NOI18N
    public static final String COLUMN_NAME_STATUS  = "status"; // NOI18N
    public static final String COLUMN_NAME_ACTION  = "action"; // NOI18N
    public static final String COLUMN_NAME_PATH    = "path"; // NOI18N
    public static final String COLUMN_NAME_BRANCH  = "branch"; // NOI18N

    public static String [] COMMIT_COLUMNS = new String [] {
                                            VCSCommitTableModel.COLUMN_NAME_COMMIT,
                                            VCSCommitTableModel.COLUMN_NAME_NAME,
                                            VCSCommitTableModel.COLUMN_NAME_STATUS,
                                            VCSCommitTableModel.COLUMN_NAME_ACTION,
                                            VCSCommitTableModel.COLUMN_NAME_PATH
                                        };

    private VCSCommitOptions[] createDefaultCommitOptions() {
        VCSCommitOptions[] co = new VCSCommitOptions[getNodes().length];
        for (int i = 0; i < getNodes().length; i++) {
            co[i] = VCSCommitOptions.COMMIT;
            
            VCSFileNode node = getNode(i);
            File file = node.getFile();
            VCSFileInformation info = (VCSFileInformation) node.getInformation();
            co[i] = info.getDefaultCommitOption(file);            
        }
        return co;
    }

    private class RootFile {
        String repositoryPath;
        String rootLocalPath;
    }
    private RootFile rootFile;

    /**
     * Defines labels for Versioning view table columns.
     */ 
    private static final Map<String, String[]> columnLabels = new HashMap<String, String[]>(4);   

    {
        ResourceBundle loc = NbBundle.getBundle(VCSCommitTableModel.class);
        columnLabels.put(COLUMN_NAME_COMMIT, new String [] {
                                          loc.getString("CTL_CommitTable_Column_Commit"),  // NOI18N
                                          loc.getString("CTL_CommitTable_Column_Description")}); // NOI18N
        columnLabels.put(COLUMN_NAME_NAME, new String [] {
                                          loc.getString("CTL_CommitTable_Column_File"),  // NOI18N
                                          loc.getString("CTL_CommitTable_Column_File")}); // NOI18N
        columnLabels.put(COLUMN_NAME_BRANCH, new String [] { 
                                          loc.getString("CTL_CommitTable_Column_Branch"),  // NOI18N
                                          loc.getString("CTL_CommitTable_Column_Branch")}); // NOI18N
        columnLabels.put(COLUMN_NAME_STATUS, new String [] {
                                          loc.getString("CTL_CommitTable_Column_Status"),  // NOI18N
                                          loc.getString("CTL_CommitTable_Column_Status")}); // NOI18N
        columnLabels.put(COLUMN_NAME_ACTION, new String [] {
                                          loc.getString("CTL_CommitTable_Column_Action"),  // NOI18N
                                          loc.getString("CTL_CommitTable_Column_Action")}); // NOI18N
        columnLabels.put(COLUMN_NAME_PATH, new String [] {
                                          loc.getString("CTL_CommitTable_Column_Folder"),  // NOI18N
                                          loc.getString("CTL_CommitTable_Column_Folder")}); // NOI18N
    }
    
    private VCSCommitOptions []    commitOptions;
    private VCSFileNode []      nodes;
    
    private String [] columns;

    /**
     * Create stable with name, status, action and path columns
     * and empty nodes {@link #setNodes model}.
     */
    public VCSCommitTableModel() {
        setColumns(COMMIT_COLUMNS);
        setNodes(new VCSFileNode[0]);
    }

    protected void setNodes(VCSFileNode [] nodes) {
        this.nodes = nodes;
        commitOptions = createDefaultCommitOptions();
        fireTableDataChanged();
    }
    
    protected void setColumns(String [] cols) {
        if (Arrays.equals(cols, columns)) return;
        columns = cols;
        fireTableStructureChanged();
    }

    /**
     * @return Map&lt;HgFileNode, CommitOptions>
     */
    public Map<VCSFileNode, VCSCommitOptions> getCommitFiles() {
        Map<VCSFileNode, VCSCommitOptions> ret = new HashMap<VCSFileNode, VCSCommitOptions>(nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            ret.put(nodes[i], commitOptions[i]);
        }
        return ret;
    }
    
    @Override
    public String getColumnName(int column) {
        return columnLabels.get(columns[column])[0];
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public int getRowCount() {
        return nodes.length;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        String col = columns[columnIndex];
        if (col.equals(COLUMN_NAME_COMMIT)) {
            return Boolean.class;
        } else if (col.equals(COLUMN_NAME_ACTION)) {
            return VCSCommitOptions.class;
        } else {
            return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        String col = columns[columnIndex];
        return col.equals(COLUMN_NAME_COMMIT);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        VCSFileNode node;
        String col = columns[columnIndex];
        if (col.equals(COLUMN_NAME_COMMIT)) {
            return commitOptions[rowIndex] != VCSCommitOptions.EXCLUDE;
        } else if (col.equals(COLUMN_NAME_NAME)) {
            return nodes[rowIndex].getName();
        // TODO deal with branch?
        //} else if (col.equals(COLUMN_NAME_BRANCH)) {
        //    String branch = HgUtils.getCopy(nodes[rowIndex].getFile());
        //    return branch == null ? "" : branch; // NOI18N
        } else if (col.equals(COLUMN_NAME_STATUS)) {
            node = nodes[rowIndex];
            VCSFileInformation finfo =  node.getInformation();
            return finfo.getStatusText();
        } else if (col.equals(COLUMN_NAME_ACTION)) {
            return commitOptions[rowIndex];
        } else if (col.equals(COLUMN_NAME_PATH)) {
            String shortPath = null;
            // XXX this is a mess
            if(rootFile != null) {
                // must convert from native separators to slashes
                String relativePath = nodes[rowIndex].getFile().getAbsolutePath().substring(rootFile.rootLocalPath.length());
                shortPath = rootFile.repositoryPath + relativePath.replace(File.separatorChar, '/');
            } else {
                shortPath = nodes[rowIndex].getRelativePath();
                if (shortPath == null) {
                    shortPath = org.openide.util.NbBundle.getMessage(VCSCommitTableModel.class, "CTL_CommitForm_NotInRepository"); // NOI18N
                }
            }
            return shortPath;
        }
        throw new IllegalArgumentException("Column index out of range: " + columnIndex); // NOI18N
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String col = columns[columnIndex];
        if (col.equals(COLUMN_NAME_ACTION)) {
            commitOptions[rowIndex] = (VCSCommitOptions) aValue;
        } else if (col.equals(COLUMN_NAME_COMMIT)) {
            VCSFileNode node = nodes[rowIndex];
            VCSCommitOptions options = node.getInformation().getCommitOptions(node.getFile());
            commitOptions[rowIndex] = ((Boolean) aValue) ? options : VCSCommitOptions.EXCLUDE;
        } else {
            throw new IllegalArgumentException("Column index out of range: " + columnIndex); // NOI18N
        }
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    protected VCSFileNode[] getNodes() {
        return nodes;
    }
    
    public VCSFileNode getNode(int row) {
        return nodes[row];
    }

    public VCSCommitOptions getOption(int row) {
        return commitOptions[row];
    }

    protected void setRootFile(String repositoryPath, String rootLocalPath) {
        rootFile = new RootFile();
        rootFile.repositoryPath = repositoryPath;
        rootFile.rootLocalPath = rootLocalPath;
    }

    protected void setIncluded (int[] rows, boolean include) {
        for (int rowIndex : rows) {
            VCSFileNode node = nodes[rowIndex];
            VCSCommitOptions options = node.getInformation().getCommitOptions(node.getFile());
            commitOptions[rowIndex] = include ? options : VCSCommitOptions.EXCLUDE;
        }
        fireTableRowsUpdated(0, getRowCount() - 1);
    }

}
