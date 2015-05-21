/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2015 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.lib.profiler.ui.memory;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.results.CCTNode;
import org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode;
import org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsDiff;
import org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.MemoryCCTManager;
import org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode;
import org.netbeans.lib.profiler.results.memory.PresoObjLivenessCCTNode;
import org.netbeans.lib.profiler.ui.Formatters;
import org.netbeans.lib.profiler.ui.swing.ExportUtils;
import org.netbeans.lib.profiler.ui.swing.ProfilerTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTableContainer;
import org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTreeTableModel;
import org.netbeans.lib.profiler.ui.swing.renderer.CheckBoxRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.HideableBarRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.NumberPercentRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.NumberRenderer;
import org.netbeans.lib.profiler.utils.StringUtils;
import org.netbeans.lib.profiler.utils.Wildcards;

/**
 *
 * @author Jiri Sedlacek
 */
abstract class LivenessTreeTableView extends MemoryView {
    
    private LivenessTreeTableModel treeTableModel;
    private ProfilerTreeTable treeTable;
    
    private Map<TreeNode, ClientUtils.SourceCodeSelection> nodesMap;
    private final Set<ClientUtils.SourceCodeSelection> selection;
    
    
    public LivenessTreeTableView(Set<ClientUtils.SourceCodeSelection> selection) {
        this.selection = selection;
        
        initUI();
    }
    
    
    protected RowFilter getExcludesFilter() {
        return new RowFilter() { // Do not filter first level nodes
            public boolean include(RowFilter.Entry entry) {
                PresoObjAllocCCTNode node = (PresoObjAllocCCTNode)entry.getIdentifier();
                return node.getParent() != null && node.getParent().getParent() == null;
            }
        };
    }
    
    protected ProfilerTable getResultsComponent() { return treeTable; }
    
    
    public void setData(MemoryResultsSnapshot snapshot, Collection filter, int aggregation) {
        final boolean includeEmpty = filter != null;
//        final boolean includeEmpty = false;
        final boolean diff = snapshot instanceof LivenessMemoryResultsDiff;
        final LivenessMemoryResultsSnapshot _snapshot = (LivenessMemoryResultsSnapshot)snapshot;
        
        String[] _classNames = _snapshot.getClassNames();
        long[] _nTrackedAllocObjects = _snapshot.getNTrackedAllocObjects();
        long[] _objectsSizePerClass = _snapshot.getObjectsSizePerClass();
        int[] _nTrackedLiveObjects = _snapshot.getNTrackedLiveObjects();
        int[] _nTotalAllocObjects = _snapshot.getnTotalAllocObjects();
        float[] _avgObjectAge = _snapshot.getAvgObjectAge();
        int[] _maxSurvGen = _snapshot.getMaxSurvGen();
        
        int _nTrackedItems = Math.min(_snapshot.getNProfiledClasses(), _classNames.length);
        _nTrackedItems = Math.min(_nTrackedItems, _nTotalAllocObjects.length);
        
        List<PresoObjLivenessCCTNode> nodes = new ArrayList();
        final Map<TreeNode, ClientUtils.SourceCodeSelection> _nodesMap = new HashMap();
        
        long totalLiveBytes = 0;
        long _totalLiveBytes = 0;
        long totalLiveObjects = 0;
        long _totalLiveObjects = 0;
        long totalTrackedAlloc = 0;
        long _totalTrackedAlloc = 0;
//        long totalTotalAlloc = 0;
//        long _totalTotalAlloc = 0;
        
        for (int i = 0; i < _nTrackedItems; i++) {
            if (diff) {
                totalLiveBytes = Math.max(totalLiveBytes, _objectsSizePerClass[i]);
                _totalLiveBytes = Math.min(_totalLiveBytes, _objectsSizePerClass[i]);
                totalLiveObjects = Math.max(totalLiveObjects, _nTrackedLiveObjects[i]);
                _totalLiveObjects = Math.min(_totalLiveObjects, _nTrackedLiveObjects[i]);
                totalTrackedAlloc = Math.max(totalTrackedAlloc, _nTrackedAllocObjects[i]);
                _totalTrackedAlloc = Math.min(_totalTrackedAlloc, _nTrackedAllocObjects[i]);
//                totalTotalAlloc = Math.max(totalTotalAlloc, _nTotalAllocObjects[i]);
//                _totalTotalAlloc = Math.min(_totalTotalAlloc, _nTotalAllocObjects[i]);
            } else {
                totalLiveBytes += _objectsSizePerClass[i];
                totalLiveObjects += _nTrackedLiveObjects[i];
                totalTrackedAlloc += _nTrackedAllocObjects[i];
//                totalTotalAlloc += _nTotalAllocObjects[i];
            }
            
//            String className = StringUtils.userFormClassName(_classNames[i]);
            String className = _classNames[i];
            
            if ((!includeEmpty && _nTrackedLiveObjects[i] > 0) || (includeEmpty && filter.contains(className))) {
                final int _i = i;
                PresoObjLivenessCCTNode node = new PresoObjLivenessCCTNode(className, _nTrackedAllocObjects[i], _objectsSizePerClass[i], _nTrackedLiveObjects[i], _nTotalAllocObjects[i], _avgObjectAge[i], _maxSurvGen[i]) {
                    public CCTNode[] getChildren() {
                        if (children == null) {
                            MemoryCCTManager callGraphManager = new MemoryCCTManager(_snapshot, _i, true);
                            PresoObjAllocCCTNode root = callGraphManager.getRootNode();
                            setChildren(root == null ? new PresoObjAllocCCTNode[0] :
                                        (PresoObjAllocCCTNode[])root.getChildren());
                        }
                        return children;
                    }
                    public boolean isLeaf() {
                        if (children == null) return includeEmpty ? nCalls == 0 : false;
                        else return super.isLeaf();
                    }   
                    public int getChildCount() {
                        if (children == null) getChildren();
                        return super.getChildCount();
                    }
                };
                nodes.add(node);
                _nodesMap.put(node, new ClientUtils.SourceCodeSelection(className, Wildcards.ALLWILDCARD, null));
            }
        }
        
        final long __totalLiveBytes = !diff ? totalLiveBytes :
                Math.max(Math.abs(totalLiveBytes), Math.abs(_totalLiveBytes));
        final long __totalLiveObjects = !diff ? totalLiveObjects :
                Math.max(Math.abs(totalLiveObjects), Math.abs(_totalLiveObjects));
        final long __totalTrackedAlloc = !diff ? totalTrackedAlloc :
                Math.max(Math.abs(totalTrackedAlloc), Math.abs(_totalTrackedAlloc));
//        final long __totalTotalAlloc = !diff ? totalTotalAlloc :
//                Math.max(Math.abs(totalTotalAlloc), Math.abs(_totalTotalAlloc));
        final PresoObjLivenessCCTNode root = PresoObjLivenessCCTNode.rootNode(nodes.toArray(new PresoObjLivenessCCTNode[nodes.size()]));
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                nodesMap = _nodesMap;
                renderers[0].setMaxValue(__totalLiveBytes);
                renderers[1].setMaxValue(__totalLiveObjects);
                renderers[2].setMaxValue(__totalTrackedAlloc);
//                renderers[3].setMaxValue(__totalTotalAlloc);
                
                renderers[0].setDiffMode(diff);
                renderers[1].setDiffMode(diff);
                renderers[2].setDiffMode(diff);
//                renderers[3].setDiffMode(diff);
                
                renderersEx[0].setDiffMode(diff);
                renderersEx[1].setDiffMode(diff);
                    
                treeTableModel.setRoot(root);
            }
        });
    }
    
    
    public void resetData() {
        final PresoObjLivenessCCTNode root = PresoObjLivenessCCTNode.rootNode(new PresoObjLivenessCCTNode[0]);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                nodesMap = null;
                
                renderers[0].setMaxValue(0);
                renderers[1].setMaxValue(0);
                renderers[2].setMaxValue(0);
//                renderers[3].setMaxValue(0);

                renderers[0].setDiffMode(false);
                renderers[1].setDiffMode(false);
                renderers[2].setDiffMode(false);
//                renderers[3].setDiffMode(false);

                renderersEx[0].setDiffMode(false);
                renderersEx[1].setDiffMode(false);
                
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
    
    
    public ExportUtils.ExportProvider[] getExportProviders() {
        return treeTable.getRowCount() == 0 ? null : new ExportUtils.ExportProvider[] {
            new ExportUtils.CSVExportProvider(treeTable),
            new ExportUtils.HTMLExportProvider(treeTable, EXPORT_ALLOCATED_LIVE),
            new ExportUtils.XMLExportProvider(treeTable, EXPORT_ALLOCATED_LIVE),
            new ExportUtils.PNGExportProvider(treeTable)
        };
    }
    
    
    protected abstract void performDefaultAction(ClientUtils.SourceCodeSelection userValue);
    
    protected abstract void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue);
    
    
    private HideableBarRenderer[] renderers;
    private NumberRenderer[] renderersEx;
    
    private void initUI() {
        final int offset = selection == null ? -1 : 0;
        
        treeTableModel = new LivenessTreeTableModel(PrestimeCPUCCTNode.EMPTY);
        
        treeTable = new ProfilerTreeTable(treeTableModel, true, true, new int[] { 1 + offset }) {
            public ClientUtils.SourceCodeSelection getUserValueForRow(int row) {
                return LivenessTreeTableView.this.getUserValueForRow(row);
            }
            protected void populatePopup(JPopupMenu popup, Object value, Object userValue) {
                LivenessTreeTableView.this.populatePopup(popup, value, (ClientUtils.SourceCodeSelection)userValue);
            }
        };
        
        treeTable.setColumnToolTips(selection == null ? new String[] {
                                  NAME_COLUMN_TOOLTIP,
                                  LIVE_SIZE_COLUMN_TOOLTIP,
                                  LIVE_COUNT_COLUMN_TOOLTIP,
                                  ALLOC_COUNT_COLUMN_TOOLTIP,
//                                  TOTAL_ALLOC_COUNT_COLUMN_TOOLTIP,
                                  AVG_AGE_COLUMN_TOOLTIP,
                                  GENERATIONS_COLUMN_TOOLTIP
                                } : new String[] {
                                  SELECTED_COLUMN_TOOLTIP,
                                  NAME_COLUMN_TOOLTIP,
                                  LIVE_SIZE_COLUMN_TOOLTIP,
                                  LIVE_COUNT_COLUMN_TOOLTIP,
                                  ALLOC_COUNT_COLUMN_TOOLTIP,
//                                  TOTAL_ALLOC_COUNT_COLUMN_TOOLTIP,
                                  AVG_AGE_COLUMN_TOOLTIP,
                                  GENERATIONS_COLUMN_TOOLTIP
                                });
        
        treeTable.providePopupMenu(true);
        treeTable.setDefaultAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int row = treeTable.getSelectedRow();
                ClientUtils.SourceCodeSelection userValue = getUserValueForRow(row);
                if (userValue != null) performDefaultAction(userValue);
            }
        });
        
        treeTable.setRootVisible(false);
        treeTable.setShowsRootHandles(true);
        treeTable.makeTreeAutoExpandable(2);
        
        treeTable.setMainColumn(1 + offset);
        treeTable.setFitWidthColumn(1 + offset);
        
        treeTable.setSortColumn(2 + offset);
        treeTable.setDefaultSortOrder(1 + offset, SortOrder.ASCENDING);
        
        if (selection != null) treeTable.setColumnVisibility(0, false);
        treeTable.setColumnVisibility(5 + offset, false);
//        treeTable.setColumnVisibility(6 + offset, false);
        
        renderers = new HideableBarRenderer[3];
        renderers[0] = new HideableBarRenderer(new NumberPercentRenderer(Formatters.bytesFormat()));
        renderers[1] = new HideableBarRenderer(new NumberPercentRenderer());
        renderers[2] = new HideableBarRenderer(new NumberPercentRenderer());
//        renderers[3] = new HideableBarRenderer(new NumberPercentRenderer() {
//            public void setValue(Object value, int row) {
//                if (((Number)value).longValue() == -1) {
//                    super.setValue(null, row);
//                } else {
//                    super.setValue(value, row);
//                }
//            }
//        });
        
        renderers[0].setMaxValue(123456789);
        renderers[1].setMaxValue(12345678);
        renderers[2].setMaxValue(12345678);
//        renderers[3].setMaxValue(12345678);
        
        renderersEx = new NumberRenderer[2];
        renderersEx[0] = new NumberRenderer() {
            protected String getValueString(Object value, int row, Format format) {
                if (value == null) return "-"; // NOI18N
                float _value = ((Float)value).floatValue();
                String s = StringUtils.floatPerCentToString(_value);
                if (renderingDiff && _value >= 0) s = '+' + s; // NOI18N
                return s;
            }
        };
        renderersEx[1] = new NumberRenderer();
        
        if (selection != null) treeTable.setColumnRenderer(0, new CheckBoxRenderer() {
            private boolean visible;
            public void setValue(Object value, int row) {
                TreePath path = treeTable.getPathForRow(row);
                visible = nodesMap.containsKey((TreeNode)path.getLastPathComponent());
                if (visible) super.setValue(value, row);
            }
            public void paint(Graphics g) {
                if (visible) {
                    super.paint(g);
                } else {
                    g.setColor(getBackground());
                    g.fillRect(0, 0, size.width, size.height);
                }
            }
        });
        treeTable.setTreeCellRenderer(new MemoryJavaNameRenderer());
        treeTable.setColumnRenderer(2 + offset, renderers[0]);
        treeTable.setColumnRenderer(3 + offset, renderers[1]);
        treeTable.setColumnRenderer(4 + offset, renderers[2]);
//        treeTable.setColumnRenderer(5 + offset, renderers[3]);
//        treeTable.setColumnRenderer(6 + offset, renderersEx[0]);
//        treeTable.setColumnRenderer(7 + offset, renderersEx[1]);
        treeTable.setColumnRenderer(5 + offset, renderersEx[0]);
        treeTable.setColumnRenderer(6 + offset, renderersEx[1]);

        if (selection != null) {
            int w = new JLabel(treeTable.getColumnName(0)).getPreferredSize().width;
            treeTable.setDefaultColumnWidth(0, w + 15);
        }
        treeTable.setDefaultColumnWidth(2 + offset, renderers[0].getOptimalWidth());
        treeTable.setDefaultColumnWidth(3 + offset, renderers[1].getMaxNoBarWidth());
        treeTable.setDefaultColumnWidth(4 + offset, renderers[2].getMaxNoBarWidth());
//        treeTable.setDefaultColumnWidth(5 + offset, renderers[3].getMaxNoBarWidth());
//        treeTable.setDefaultColumnWidth(6 + offset, renderers[3].getNoBarWidth() - 25);
        treeTable.setDefaultColumnWidth(5 + offset, renderers[2].getNoBarWidth() - 25);
        
        ProfilerTableContainer tableContainer = new ProfilerTableContainer(treeTable, false, null);
        
        setLayout(new BorderLayout());
        add(tableContainer, BorderLayout.CENTER);
    }
    
    protected ClientUtils.SourceCodeSelection getUserValueForRow(int row) {
        PresoObjLivenessCCTNode node = (PresoObjLivenessCCTNode)treeTable.getValueForRow(row);
        String[] name = node.getMethodClassNameAndSig();
        return new ClientUtils.SourceCodeSelection(name[0], name[1], name[2]);
    }
    
    
    private class LivenessTreeTableModel extends ProfilerTreeTableModel.Abstract {
        
        LivenessTreeTableModel(TreeNode root) {
            super(root);
        }
        
        public String getColumnName(int columnIndex) {
            if (selection == null) columnIndex++;
            
            if (columnIndex == 1) {
                return COLUMN_NAME;
            } else if (columnIndex == 2) {
                return COLUMN_LIVE_BYTES;
            } else if (columnIndex == 3) {
                return COLUMN_LIVE_OBJECTS;
            } else if (columnIndex == 4) {
                return COLUMN_ALLOCATED_OBJECTS;
//            } else if (columnIndex == 5) {
//                return COLUMN_TOTAL_ALLOCATED_OBJECTS;
//            } else if (columnIndex == 6) {
//                return COLUMN_AVG_AGE;
//            } else if (columnIndex == 7) {
//                return COLUMN_GENERATIONS;
            } else if (columnIndex == 5) {
                return COLUMN_AVG_AGE;
            } else if (columnIndex == 6) {
                return COLUMN_GENERATIONS;
            } else if (columnIndex == 0) {
                return COLUMN_SELECTED;
            }
            return null;
        }

        public Class<?> getColumnClass(int columnIndex) {
            if (selection == null) columnIndex++;
            
            if (columnIndex == 1) {
                return JTree.class;
            } else if (columnIndex == 2) {
                return Long.class;
            } else if (columnIndex == 3) {
                return Integer.class;
            } else if (columnIndex == 4) {
                return Long.class;
//            } else if (columnIndex == 5) {
//                return Integer.class;
//            } else if (columnIndex == 6) {
//                return Float.class;
//            } else if (columnIndex == 7) {
//                return Integer.class;
            } else if (columnIndex == 5) {
                return Float.class;
            } else if (columnIndex == 6) {
                return Integer.class;
            } else if (columnIndex == 0) {
                return Boolean.class;
            }
            return null;
        }

        public int getColumnCount() {
//            return selection == null ? 7 : 8;
            return selection == null ? 6 : 7;
        }

        public Object getValueAt(TreeNode node, int columnIndex) {
            PresoObjLivenessCCTNode livenessNode = (PresoObjLivenessCCTNode)node;
            
            if (selection == null) columnIndex++;
            
            if (columnIndex == 1) {
                return livenessNode.getNodeName();
            } else if (columnIndex == 2) {
                return livenessNode.totalObjSize;
            } else if (columnIndex == 3) {
                return livenessNode.nLiveObjects;
            } else if (columnIndex == 4) {
                return livenessNode.nCalls;
//            } else if (columnIndex == 5) {
//                return livenessNode.nTotalAllocObjects;
//            } else if (columnIndex == 6) {
//                return livenessNode.avgObjectAge;
//            } else if (columnIndex == 7) {
//                return livenessNode.survGen;
            } else if (columnIndex == 5) {
                return livenessNode.avgObjectAge;
            } else if (columnIndex == 6) {
                return livenessNode.survGen;
            } else if (columnIndex == 0) {
                if (selection.isEmpty()) return Boolean.FALSE;
                return selection.contains(nodesMap.get(node));
            }

            return null;
        }
        
        public void setValueAt(Object aValue, TreeNode node, int columnIndex) {
            if (selection == null) columnIndex++;
            
            if (columnIndex == 0) {
                if (Boolean.TRUE.equals(aValue)) selection.add(nodesMap.get(node));
                else selection.remove(nodesMap.get(node));
            }
        }

        public boolean isCellEditable(TreeNode node, int columnIndex) {
            if (selection == null) columnIndex++;
            if (columnIndex != 0) return false;
            return (nodesMap.containsKey(node));
        }
        
    }
    
}
