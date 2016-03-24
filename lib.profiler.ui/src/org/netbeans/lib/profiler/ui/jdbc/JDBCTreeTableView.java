/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2014 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.lib.profiler.ui.jdbc;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.results.CCTNode;
import org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode;
import org.netbeans.lib.profiler.results.jdbc.JdbcCCTProvider;
import org.netbeans.lib.profiler.results.jdbc.JdbcResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode;
import org.netbeans.lib.profiler.ui.swing.ExportUtils;
import org.netbeans.lib.profiler.ui.swing.ProfilerTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTableContainer;
import org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTreeTableModel;
import org.netbeans.lib.profiler.ui.swing.renderer.HideableBarRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.LabelRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.McsTimeRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.NumberPercentRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.NumberRenderer;

/**
 *
 * @author Jiri Sedlacek
 */
abstract class JDBCTreeTableView extends JDBCView {
    
    private JDBCTreeTableModel treeTableModel;
    private ProfilerTreeTable treeTable;
    
    
    public JDBCTreeTableView(Set<ClientUtils.SourceCodeSelection> selection, boolean reverse) {
        initUI();
    }
    
    
    void setData(final JdbcResultsSnapshot newData, final Map<Integer, ClientUtils.SourceCodeSelection> newIdMap, final int aggregation, final Collection<Integer> selectedThreads, final boolean mergeThreads, final boolean _sampled, final boolean _diff) {
//        final boolean diff = snapshot instanceof AllocMemoryResultsDiff;
        final boolean diff = false;
        
        String[] _names = newData.getSelectNames();
        long[] _nTotalAllocObjects = newData.getInvocationsPerSelectId();
        long[] _totalAllocObjectsSize = newData.getTimePerSelectId();
        
        List<PresoObjAllocCCTNode> nodes = new ArrayList();
        
        long totalObjects = 0;
        long _totalObjects = 0;
        long totalBytes = 0;
        long _totalBytes = 0;
        
        for (int i = 1; i < _names.length; i++) {
            if (diff) {
//                totalObjects = Math.max(totalObjects, _nTotalAllocObjects[i]);
//                _totalObjects = Math.min(_totalObjects, _nTotalAllocObjects[i]);
//                totalBytes = Math.max(totalBytes, _totalAllocObjectsSize[i]);
//                _totalBytes = Math.min(_totalBytes, _totalAllocObjectsSize[i]);
            } else {
                totalObjects += _nTotalAllocObjects[i];
                totalBytes += _totalAllocObjectsSize[i];
            }
            
            final int _i = i;
            
//            class Node extends PresoObjAllocCCTNode {
//                Node(String className, long nTotalAllocObjects, long totalAllocObjectsSize) {
//                    super(className, nTotalAllocObjects, totalAllocObjectsSize);
//                }
//                public CCTNode[] getChildren() {
//                    if (children == null) {
//                        PresoObjAllocCCTNode root = newData.createPresentationCCT(_i, false);
//                        setChildren(root == null ? new PresoObjAllocCCTNode[0] :
//                                    (PresoObjAllocCCTNode[])root.getChildren());
//                    }
//                    return children;
//                }
//                public boolean isLeaf() {
//                    if (children == null) return /*includeEmpty ? nCalls == 0 :*/ false;
//                    else return super.isLeaf();
//                }   
//                public int getChildCount() {
//                    if (children == null) getChildren();
//                    return super.getChildCount();
//                }
//            }
            SQLQueryNode node = new SQLQueryNode(_names[i], _nTotalAllocObjects[i], _totalAllocObjectsSize[i], newData.getTypeForSelectId()[i], newData.getCommandTypeForSelectId()[i], newData.getTablesForSelectId()[i]) {
                PresoObjAllocCCTNode computeChildren() { return newData.createPresentationCCT(_i, false); }
            };
            nodes.add(node);
        }
        
        final long __totalBytes = !diff ? totalBytes :
                Math.max(Math.abs(totalBytes), Math.abs(_totalBytes));
        final long __totalObjects = !diff ? totalObjects :
                Math.max(Math.abs(totalObjects), Math.abs(_totalObjects));
        final PresoObjAllocCCTNode root = PresoObjAllocCCTNode.rootNode(nodes.toArray(new PresoObjAllocCCTNode[0]));
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                renderers[0].setMaxValue(__totalBytes);
                renderers[1].setMaxValue(__totalObjects);
                renderers[0].setDiffMode(diff);
                renderers[1].setDiffMode(diff);
                treeTableModel.setRoot(root);
            }
        });
    }
    
    public void resetData() {
        final PresoObjAllocCCTNode root = PresoObjAllocCCTNode.rootNode(new PresoObjAllocCCTNode[0]);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                renderers[0].setMaxValue(0);
                renderers[1].setMaxValue(0);
                renderers[0].setDiffMode(false);
                renderers[1].setDiffMode(false);
                
                treeTableModel.setRoot(root);
            }
        });
    }
    
    
    public void showSelectionColumn() {
        treeTable.setColumnVisibility(0, true);
    }
    
    public void refreshSelection() {
        treeTableModel.dataChanged();
    }
    
    
    ExportUtils.ExportProvider[] getExportProviders() {
        final String name = EXPORT_FORWARD_CALLS;
        return treeTable.getRowCount() == 0 ? null : new ExportUtils.ExportProvider[] {
            new ExportUtils.CSVExportProvider(treeTable),
            new ExportUtils.HTMLExportProvider(treeTable, name),
            new ExportUtils.XMLExportProvider(treeTable, name),
            new ExportUtils.PNGExportProvider(treeTable)
        };
    }
    
    
    protected abstract void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue);
    
    protected void popupShowing() {};
    
    protected void popupHidden()  {};
    
    
    private HideableBarRenderer[] renderers;
    
    private void initUI() {
        treeTableModel = new JDBCTreeTableModel(PrestimeCPUCCTNode.EMPTY);
        
        treeTable = new ProfilerTreeTable(treeTableModel, true, true, new int[] { 0 }) {
            public ClientUtils.SourceCodeSelection getUserValueForRow(int row) {
                return JDBCTreeTableView.this.getUserValueForRow(row);
            }
            protected void populatePopup(JPopupMenu popup, Object value, Object userValue) {
                JDBCTreeTableView.this.populatePopup(popup, value, (ClientUtils.SourceCodeSelection)userValue);
            }
            protected void popupShowing() {
                JDBCTreeTableView.this.popupShowing();
            }
            protected void popupHidden() {
                JDBCTreeTableView.this.popupHidden();
            }
        };
        
        setToolTips();
        
        treeTable.providePopupMenu(true);
        installDefaultAction();
        
        treeTable.setRootVisible(false);
        treeTable.setShowsRootHandles(true);
        treeTable.makeTreeAutoExpandable(2);
        
        treeTable.setMainColumn(0);
        treeTable.setFitWidthColumn(0);
        
        treeTable.setSortColumn(1);
        treeTable.setDefaultSortOrder(1, SortOrder.DESCENDING);
        
        renderers = new HideableBarRenderer[2];
        renderers[0] = new HideableBarRenderer(new NumberPercentRenderer(new McsTimeRenderer()));
        renderers[1] = new HideableBarRenderer(new NumberRenderer());
        
        long refTime = 123456;
        renderers[0].setMaxValue(refTime);
        renderers[1].setMaxValue(refTime);
        
        treeTable.setTreeCellRenderer(new JDBCJavaNameRenderer());
        treeTable.setColumnRenderer(1, renderers[0]);
        treeTable.setColumnRenderer(2, renderers[1]);
        
        treeTable.setDefaultColumnWidth(1, renderers[0].getOptimalWidth());
        treeTable.setDefaultColumnWidth(2, renderers[1].getMaxNoBarWidth());
        
        // Debug columns
        LabelRenderer lr = new LabelRenderer();
        lr.setHorizontalAlignment(LabelRenderer.TRAILING);
        lr.setValue("XStatement TypeX", -1);
        
        treeTable.setColumnRenderer(3, lr);
        treeTable.setDefaultSortOrder(3, SortOrder.ASCENDING);
        treeTable.setDefaultColumnWidth(3, lr.getPreferredSize().width);
        treeTable.setColumnVisibility(3, false);
        
        treeTable.setColumnRenderer(4, lr);
        treeTable.setDefaultSortOrder(4, SortOrder.ASCENDING);
        treeTable.setDefaultColumnWidth(4, lr.getPreferredSize().width);
        treeTable.setColumnVisibility(4, false);
        
        treeTable.setColumnRenderer(5, lr);
        treeTable.setDefaultSortOrder(5, SortOrder.ASCENDING);
        treeTable.setDefaultColumnWidth(5, lr.getPreferredSize().width);
        treeTable.setColumnVisibility(5, false);
        
        ProfilerTableContainer tableContainer = new ProfilerTableContainer(treeTable, false, null);
        
        setLayout(new BorderLayout());
        add(tableContainer, BorderLayout.CENTER);
    }
    
    private void setToolTips() {
        treeTable.setColumnToolTips(new String[] {
                                        NAME_COLUMN_TOOLTIP,
                                        TOTAL_TIME_COLUMN_TOOLTIP,
                                        INVOCATIONS_COLUMN_TOOLTIP,
                                        "SQL Statement Type",
                                        "SQL Command Type",
                                        "Database Tables"
                                    });
    }
    
    
//    protected RowFilter getExcludesFilter() {
//        return new RowFilter() { // Do not filter threads and self time nodes
//            public boolean include(RowFilter.Entry entry) {
//                PrestimeCPUCCTNode node = (PrestimeCPUCCTNode)entry.getIdentifier();
//                return node.isThreadNode() || node.isSelfTimeNode();
//            }
//        };
//    }
    
    protected ProfilerTable getResultsComponent() {
        return treeTable;
    }
    
    
//    private long getMaxValue(int row, int val) {
//        TreePath path = treeTable.getPathForRow(row);
//        if (path == null) return Long.MIN_VALUE; // TODO: prevents NPE from export but doesn't provide the actual value!
//        if (path.getPathCount() < 2) return 1;
//        
//        PresoObjAllocCCTNode node = (PresoObjAllocCCTNode)path.getPathComponent(1);
//        if (val == 0) return Math.abs(node.getTotalTime0());
//        else if (val == 1) return Math.abs(node.getTotalTime1());
//        else return Math.abs(node.getNCalls());
//    }
    
    protected ClientUtils.SourceCodeSelection getUserValueForRow(int row) {
        PresoObjAllocCCTNode node = (PresoObjAllocCCTNode)treeTable.getValueForRow(row);
        if (node == null || isSQL(node)) return null;
        String[] name = node.getMethodClassNameAndSig();
        return new ClientUtils.SourceCodeSelection(name[0], name[1], name[2]);
    }
    
//    private ClientUtils.SourceCodeSelection selectionForId(int methodId) {
//        ProfilingSessionStatus sessionStatus = client.getStatus();
//        sessionStatus.beginTrans(false);
//        try {
//            String className = sessionStatus.getInstrMethodClasses()[methodId];
//            String methodName = sessionStatus.getInstrMethodNames()[methodId];
//            String methodSig = sessionStatus.getInstrMethodSignatures()[methodId];
//            return new ClientUtils.SourceCodeSelection(className, methodName, methodSig);
//        } finally {
//            sessionStatus.endTrans();
//        }
//    }
    
    static boolean isSQL(PresoObjAllocCCTNode node) {
//        CCTNode p = node.getParent();
//        if (p != null && p.getParent() == null) return true;
//        return false;
        return node instanceof SQLQueryNode;
    }
    
    static boolean isSelectable(PresoObjAllocCCTNode node) {
        if (isSQL(node)) return false;
        if (node.getMethodClassNameAndSig()[1].endsWith("[native]")) return false; // NOI18N
        return true;
    }
    
    
    private class JDBCTreeTableModel extends ProfilerTreeTableModel.Abstract {
        
        JDBCTreeTableModel(TreeNode root) {
            super(root);
        }
        
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return COLUMN_NAME;
            } else if (columnIndex == 1) {
                return COLUMN_TOTALTIME;
            } else if (columnIndex == 2) {
                return COLUMN_INVOCATIONS;
            }  else if (columnIndex == 3) {
                return "Statement Type";
            } else if (columnIndex == 4) {
                return "Command Type";
            } else if (columnIndex == 5) {
                return "Tables";
            }
            return null;
        }

        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return JTree.class;
//            } else if (columnIndex == 1) {
//                return Long.class;
//            } else if (columnIndex == 2) {
//                return Integer.class;
            } else if (columnIndex == 3) {
                return String.class;
            } else if (columnIndex == 4) {
                return String.class;
            } else if (columnIndex == 5) {
                return String.class;
            }
            return Long.class;
//            return null;
        }

        public int getColumnCount() {
            return 6;
        }

        public Object getValueAt(TreeNode node, int columnIndex) {
            PresoObjAllocCCTNode jdbcNode = (PresoObjAllocCCTNode)node;
            if (columnIndex == 0) {
                return jdbcNode.getNodeName();
            } else if (columnIndex == 1) {
                return jdbcNode.totalObjSize;
            } else if (columnIndex == 2) {
                return jdbcNode.nCalls;
            } else if (columnIndex == 3) {
                if (jdbcNode instanceof SQLQueryNode) {
                    switch (((SQLQueryNode)jdbcNode).getStatementType()) {
                        case JdbcCCTProvider.SQL_PREPARED_STATEMENT: return "prepared";
                        case JdbcCCTProvider.SQL_CALLABLE_STATEMENT: return "callable";
                        default: return "reqular";
                    }
                } else {
                    return "-";
                }
            } else if (columnIndex == 4) {
                if (jdbcNode instanceof SQLQueryNode) {
                    switch (((SQLQueryNode)jdbcNode).getCommandType()) {
                        case JdbcCCTProvider.SQL_COMMAND_ALTER: return "ALTER";
                        case JdbcCCTProvider.SQL_COMMAND_CREATE: return "CREATE";
                        case JdbcCCTProvider.SQL_COMMAND_DELETE: return "DELETE";
                        case JdbcCCTProvider.SQL_COMMAND_DESCRIBE: return "DESCRIBE";
                        case JdbcCCTProvider.SQL_COMMAND_INSERT: return "INSERT";
                        case JdbcCCTProvider.SQL_COMMAND_SELECT: return "SELECT";
                        case JdbcCCTProvider.SQL_COMMAND_SET: return "SET";
                        case JdbcCCTProvider.SQL_COMMAND_UPDATE: return "UPDATE";
                        default: return "other statement";
                    }
                } else {
                    return "-";
                }
            } else if (columnIndex == 5) {
                if (jdbcNode instanceof SQLQueryNode) {
                    return Arrays.toString(((SQLQueryNode)jdbcNode).getTables());
                } else {
                    return "-";
                }
            }
            return null;
        }
        
        public void setValueAt(Object aValue, TreeNode node, int columnIndex) {}

        public boolean isCellEditable(TreeNode node, int columnIndex) {
            return false;
        }
        
    }
    
    abstract class SQLQueryNode extends PresoObjAllocCCTNode {
        String htmlName;
        private final int statementType;
        private final int commandType;
        private final String[] tables;
        SQLQueryNode(String className, long nTotalAllocObjects, long totalAllocObjectsSize, int statementType, int commandType, String[] tables) {
            super(className, nTotalAllocObjects, totalAllocObjectsSize);
            this.statementType = statementType;
            this.commandType = commandType;
            this.tables = tables;
        }
        public CCTNode[] getChildren() {
            if (children == null) {
                PresoObjAllocCCTNode root = computeChildren();
                setChildren(root == null ? new PresoObjAllocCCTNode[0] :
                            (PresoObjAllocCCTNode[])root.getChildren());
            }
            return children;
        }
        public boolean isLeaf() {
            if (children == null) return /*includeEmpty ? nCalls == 0 :*/ false;
            else return super.isLeaf();
        }   
        public int getChildCount() {
            if (children == null) getChildren();
            return super.getChildCount();
        }
        abstract PresoObjAllocCCTNode computeChildren();
        int getStatementType() { return statementType; }
        int getCommandType() { return commandType; }
        String[] getTables() { return tables; }
    }
    
}
