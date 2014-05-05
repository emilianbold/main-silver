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

package org.netbeans.lib.profiler.ui.memory;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.netbeans.lib.profiler.results.memory.HeapHistogram;
import org.netbeans.lib.profiler.ui.Formatters;
import org.netbeans.lib.profiler.ui.swing.ProfilerTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTableContainer;
import org.netbeans.lib.profiler.ui.swing.renderer.CheckBoxRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.HideableBarRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.JavaNameRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.NumberPercentRenderer;

/**
 *
 * @author Jiri Sedlacek
 */
abstract class SampledTableView extends JPanel {
    
    private MemoryTableModel tableModel;
    private ProfilerTable table;
    
    private HeapHistogram.ClassInfo[] data;
    
    private final Set<String> selection;
    
    
    public SampledTableView(Set<String> selection) {
        this.selection = selection;
        
        initUI();
    }
    
    
    void setData(final HeapHistogram histogram) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (tableModel != null) {
                    Set<HeapHistogram.ClassInfo> classes = histogram == null ? null :
                                                 histogram.getHeapHistogram();
                    data = classes == null ? null :
                           classes.toArray(new HeapHistogram.ClassInfo[classes.size()]);
                    
                    renderers[0].setMaxValue(histogram == null ? 0 : histogram.getTotalHeapBytes());
                    renderers[1].setMaxValue(histogram == null ? 0 : histogram.getTotalHeapInstances());
                    
                    tableModel.fireTableDataChanged();
                }
            }
        });
    }
    
    void resetData() {
        setData(null);
    }
    
    
    public void showSelectionColumn() {
        table.setColumnVisibility(0, true);
    }
    
    public void refreshSelection() {
        tableModel.fireTableDataChanged();
    }
    
    
    protected abstract void performDefaultAction(String value);
    
    protected abstract void populatePopup(JPopupMenu popup, String value);
    
    protected abstract void popupShowing();
    
    protected abstract void popupHidden();
    
    
    private HideableBarRenderer[] renderers;
    
    private void initUI() {
        tableModel = new MemoryTableModel();
        
        table = new ProfilerTable(tableModel, true, true, null) {
            protected String getValueForPopup(int row) {
                return valueForRow(row);
            }
            protected void populatePopup(JPopupMenu popup, Object value) {
                SampledTableView.this.populatePopup(popup, (String)value);
            }
            protected void popupShowing() {
                SampledTableView.this.popupShowing();
            }
            protected void popupHidden() {
                SampledTableView.this.popupHidden();
            }
        };
        
        table.providePopupMenu(true);
        table.setDefaultAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                String value = valueForRow(row);
                if (value != null) performDefaultAction(value);
            }
        });
        
        table.setMainColumn(1);
        table.setFitWidthColumn(1);
        
        table.setSortColumn(2);
        table.setDefaultSortOrder(1, SortOrder.ASCENDING);
        
        table.setColumnVisibility(0, false);
        
        renderers = new HideableBarRenderer[2];
        renderers[0] = new HideableBarRenderer(new NumberPercentRenderer(Formatters.bytesFormat()));
        renderers[1] = new HideableBarRenderer(new NumberPercentRenderer());
        
        renderers[0].setMaxValue(123456789);
        renderers[1].setMaxValue(12345678);
        
        table.setColumnRenderer(0, new CheckBoxRenderer());
        table.setColumnRenderer(1, new JavaNameRenderer());
        table.setColumnRenderer(2, renderers[0]);
        table.setColumnRenderer(3, renderers[1]);
        
        int w = new JLabel(table.getColumnName(0)).getPreferredSize().width;
        table.setDefaultColumnWidth(0, w + 15);
        table.setDefaultColumnWidth(2, renderers[0].getOptimalWidth());
        table.setDefaultColumnWidth(3, renderers[1].getMaxNoBarWidth());
        
        ProfilerTableContainer tableContainer = new ProfilerTableContainer(table, false, null);
        
        setLayout(new BorderLayout());
        add(tableContainer, BorderLayout.CENTER);
    }
    
    
    private String valueForRow(int row) {
        if (data == null || row == -1) return null;
        if (row >= tableModel.getRowCount()) return null; // #239936
        return data[table.convertRowIndexToModel(row)].getName();
    }
    
    
    private class MemoryTableModel extends AbstractTableModel {
        
        public String getColumnName(int columnIndex) {
            if (columnIndex == 1) {
                return "Name";
            } else if (columnIndex == 2) {
                return "Live Bytes";
            } else if (columnIndex == 3) {
                return "Live Objects";
            } else if (columnIndex == 0) {
                return "Selected";
            }
            return null;
        }

        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 1) {
                return String.class;
            } else if (columnIndex == 0) {
                return Boolean.class;
            } else {
                return Long.class;
            }
        }

        public int getRowCount() {
            return data == null ? 0 : data.length;
        }

        public int getColumnCount() {
            return 4;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (data == null) return null;
            
            if (columnIndex == 1) {
                return data[rowIndex].getName();
            } else if (columnIndex == 2) {
                return data[rowIndex].getBytes();
            } else if (columnIndex == 3) {
                return data[rowIndex].getInstancesCount();
            } else if (columnIndex == 0) {
                if (selection.isEmpty()) return Boolean.FALSE;
                return selection.contains(data[rowIndex].getName());
            }

            return null;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                if (Boolean.FALSE.equals(aValue)) selection.remove(data[rowIndex].getName());
                else selection.add(data[rowIndex].getName());
            }
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }
        
    }
    
}
