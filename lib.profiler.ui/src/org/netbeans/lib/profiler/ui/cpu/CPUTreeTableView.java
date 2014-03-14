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

package org.netbeans.lib.profiler.ui.cpu;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.global.ProfilingSessionStatus;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode;
import org.netbeans.lib.profiler.ui.swing.ProfilerTableContainer;
import org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTreeTableModel;
import org.netbeans.lib.profiler.ui.swing.renderer.CheckBoxRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.HideableBarRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.McsTimeRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.NumberPercentRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.NumberRenderer;

/**
 *
 * @author Jiri Sedlacek
 */
public class CPUTreeTableView extends JPanel {
    
    private final ProfilerClient client;
    
    private CPUTreeTableModel treeTableModel;
    private ProfilerTreeTable treeTable;
    
    private Map<Integer, ClientUtils.SourceCodeSelection> selections;
    
    
    public CPUTreeTableView(ProfilerClient client) {
        this.client = client;
        initUI();
    }
    
    
    void setData(final CPUResultsSnapshot newData) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (treeTableModel != null) {
                    PrestimeCPUCCTNode root = newData == null ? PrestimeCPUCCTNode.EMPTY :
                                              newData.getRootNode(CPUResultsSnapshot.METHOD_LEVEL_VIEW);
                    
                    if (newData == null) selections = null; // reset data
                    else if (selections == null) selections = new HashMap();
                    
                    treeTableModel.setRoot(root);
                }
            }
        });
    }
    
    public void resetData() {
        setData(null);
    }
    
    public boolean hasSelection() {
        return selections != null && !selections.isEmpty();
    }
    
    public Set<ClientUtils.SourceCodeSelection[]> getSelections() {
        return !hasSelection() ? Collections.EMPTY_SET :
                new HashSet(selections.values());
    }
    
    
    private HideableBarRenderer[] renderers;
    
    private void initUI() {
        treeTableModel = new CPUTreeTableModel(PrestimeCPUCCTNode.EMPTY);
        
        treeTable = new ProfilerTreeTable(treeTableModel, true, true, new int[] { 0 });
        treeTable.setRootVisible(false);
        treeTable.setShowsRootHandles(true);
        treeTable.setColumnVisibility(3, false);
        treeTable.setColumnVisibility(4, false);
        treeTable.setSortColumn(1);
        
        renderers = new HideableBarRenderer[3];
        
        renderers[0] = new HideableBarRenderer(new NumberPercentRenderer(new McsTimeRenderer())) {
            public void setValue(Object value, int row) {
                super.setMaxValue(getMaxValue(row, false));
                super.setValue(value, row);
            }
        };
        renderers[1] = new HideableBarRenderer(new NumberPercentRenderer(new McsTimeRenderer())) {
            public void setValue(Object value, int row) {
                super.setMaxValue(getMaxValue(row, true));
                super.setValue(value, row);
            }
        };
        renderers[2] = new HideableBarRenderer(new NumberRenderer());
        
        long refTime = 123456;
        renderers[0].setMaxValue(refTime);
        renderers[1].setMaxValue(refTime);
        renderers[2].setMaxValue(refTime);
        
        treeTable.setTreeCellRenderer(new CPUJavaNameRenderer());
        treeTable.setColumnRenderer(1, renderers[0]);
        treeTable.setColumnRenderer(2, renderers[1]);
        treeTable.setColumnRenderer(3, renderers[2]);
        treeTable.setColumnRenderer(4, new CheckBoxRenderer());
        
        treeTable.setDefaultColumnWidth(1, renderers[0].getOptimalWidth());
        treeTable.setDefaultColumnWidth(2, renderers[1].getNoBarWidth());
        treeTable.setDefaultColumnWidth(3, renderers[2].getNoBarWidth());
        
        ProfilerTableContainer tableContainer = new ProfilerTableContainer(treeTable, false, null);
        
        setLayout(new BorderLayout());
        add(tableContainer, BorderLayout.CENTER);
    }
    
    private long getMaxValue(int row, boolean secondary) {
        TreePath path = treeTable.getPathForRow(row);
        if (path.getPathCount() < 2) return 1;
        
        PrestimeCPUCCTNode node = (PrestimeCPUCCTNode)path.getPathComponent(1);
        return secondary ? node.getTotalTime1() : node.getTotalTime0();
    }
    
    private ClientUtils.SourceCodeSelection selectionForId(int methodId) {
        ProfilingSessionStatus sessionStatus = client.getStatus();
        sessionStatus.beginTrans(false);
        try {
            String className = sessionStatus.getInstrMethodClasses()[methodId];
            String methodName = sessionStatus.getInstrMethodNames()[methodId];
            String methodSig = sessionStatus.getInstrMethodSignatures()[methodId];
            return new ClientUtils.SourceCodeSelection(className, methodName, methodSig);
        } finally {
            sessionStatus.endTrans();
        }
    }
    
    
    private class CPUTreeTableModel extends ProfilerTreeTableModel.Abstract {
        
        CPUTreeTableModel(TreeNode root) {
            super(root);
        }
        
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return "Name";
            } else if (columnIndex == 1) {
                return "Total Time";
            } else if (columnIndex == 2) {
                return "Total Time (CPU)";
            } else if (columnIndex == 3) {
                return "Samples";
            } else if (columnIndex == 4) {
                return "Selected";
            }
            return null;
        }

        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return JTree.class;
            } else if (columnIndex == 3) {
                return Integer.class;
            } else if (columnIndex == 4) {
                return Boolean.class;
            } else {
                return Long.class;
            }
        }

        public int getColumnCount() {
            return 5;
        }

        public Object getValueAt(TreeNode node, int columnIndex) {
            PrestimeCPUCCTNode cpuNode = (PrestimeCPUCCTNode)node;
            
            if (columnIndex == 0) {
                return cpuNode.getNodeName();
            } else if (columnIndex == 1) {
                return cpuNode.getTotalTime0();
            } else if (columnIndex == 2) {
                return cpuNode.getTotalTime1();
            } else if (columnIndex == 3) {
                return cpuNode.getNCalls();
            } else if (columnIndex == 4) {
                return selections.containsKey(cpuNode.getMethodId());
            }

            return null;
        }
        
        public void setValueAt(Object aValue, TreeNode node, int columnIndex) {
            if (columnIndex == 4) {
                PrestimeCPUCCTNode cpuNode = (PrestimeCPUCCTNode)node;
                int methodId = cpuNode.getMethodId();
                if (Boolean.TRUE.equals(aValue)) {
                    selections.put(methodId, selectionForId(methodId));
                } else {
                    selections.remove(methodId);
                }
                treeTable.repaint(); // Should invoke fireTableDataChanged()
            }
        }

        public boolean isCellEditable(TreeNode node, int columnIndex) {
            return columnIndex == 4;
        }
        
    }
    
}
