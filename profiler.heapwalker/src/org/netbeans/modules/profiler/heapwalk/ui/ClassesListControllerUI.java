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

package org.netbeans.modules.profiler.heapwalk.ui;

import java.awt.*;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.ui.UIConstants;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.FilterComponent;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.netbeans.lib.profiler.ui.components.JExtendedTable;
import org.netbeans.lib.profiler.ui.components.JTitledPanel;
import org.netbeans.lib.profiler.ui.components.table.ClassNameTableCellRenderer;
import org.netbeans.lib.profiler.ui.components.table.CustomBarCellRenderer;
import org.netbeans.lib.profiler.ui.components.table.ExtendedTableModel;
import org.netbeans.lib.profiler.ui.components.table.JExtendedTablePanel;
import org.netbeans.lib.profiler.ui.components.table.LabelBracketTableCellRenderer;
import org.netbeans.lib.profiler.ui.components.table.SortableTableModel;
import org.netbeans.modules.profiler.heapwalk.ClassesListController;
import org.openide.util.NbBundle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.netbeans.lib.profiler.ui.components.HTMLLabel;
import org.netbeans.lib.profiler.ui.components.table.DiffBarCellRenderer;
import org.netbeans.lib.profiler.ui.components.table.LabelTableCellRenderer;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.GoToSource;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.LanguageIcons;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.heapwalk.ui.icons.HeapWalkerIcons;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jiri Sedlacek
 */
public class ClassesListControllerUI extends JTitledPanel {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private class ClassesListTableKeyListener extends KeyAdapter {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void keyPressed(KeyEvent e) {
            if ((e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU)
                    || ((e.getKeyCode() == KeyEvent.VK_F10) && (e.getModifiers() == InputEvent.SHIFT_MASK))) {
                int selectedRow = classesListTable.getSelectedRow();

                if (selectedRow != -1) {
                    Rectangle rowBounds = classesListTable.getCellRect(selectedRow, 0, true);
                    tablePopup.show(classesListTable, rowBounds.x + (rowBounds.width / 2), rowBounds.y + (rowBounds.height / 2));
                }
            }
        }
    }

    // --- Table model -----------------------------------------------------------
    private class ClassesListTableModel extends SortableTableModel {
        final static String SELECTED_ROW_PROPERTY = "selectedRow";
        
        final private Object displayCacheLock = new Object();
        final private Object sortingLock = new Object();
        
        // @GuardedBy displayCacheLock
        private Object[][] displayCache = null;
        // @GuardedBy sortingLock
        private int sortingColumn = 1;
        // @GuardedBy sortingLock
        private boolean sortingOrder = false;
        
        private int selectedRow = -1;
        
        private static final int columnCount = 4;
        
        final private String[] columnNames;
        final private String[] columnToolTips;
        
        private JavaClass preselectedClass = null;
        
        final PropertyChangeSupport pcs = new PropertyChangeSupport(ClassesListTableModel.this);
        
        public ClassesListTableModel() {
            columnNames = new String[columnCount];
            columnToolTips = new String[columnCount];
            
            columnNames[0] = CLASSNAME_COLUMN_TEXT;
            columnToolTips[0] = CLASSNAME_COLUMN_DESCR;

            columnNames[1] = INSTANCES_REL_COLUMN_TEXT;
            columnToolTips[1] = INSTANCES_REL_COLUMN_DESCR;

            columnNames[2] = INSTANCES_COLUMN_TEXT;
            columnToolTips[2] = INSTANCES_COLUMN_DESCR;

            columnNames[3] = SIZE_COLUMN_TEXT;
            columnToolTips[3] = SIZE_COLUMN_DESCR;
        }
        
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(propertyName, listener);
        }

        public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(propertyName, listener);
        }

        public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        public Class getColumnClass(int columnIndex) {
            return Object.class;
        }

        public int getColumnCount() {
            return columnCount;
        }

        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        public String getColumnToolTipText(int col) {
            return columnToolTips[col];
        }

        public int getSortingColumn() {
            synchronized(sortingLock) {
                return sortingColumn;
            }
        }
        
        public boolean getSortingOrder() {
            synchronized(sortingLock) {
                return sortingOrder;
            }
        }
        
        public boolean getInitialSorting(int column) {
            switch (column) {
                case 0:
                    return true;
                default:
                    return false;
            }
        }

        public int getRowCount() {
            return getDisplayCache().length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return getDisplayCache()[rowIndex][columnIndex];
        }

        public void sortByColumn(int column, boolean order) {
            synchronized(sortingLock) {
                sortingColumn = column;
                sortingOrder = order;
            }
            resetDisplayCache();
            SwingUtilities.invokeLater(new Runnable() {            
                @Override
                public void run() {
                    repaint();
                }
            });
        }
        
        public JavaClass getClassForRow(int selectedRow) {
            return selectedRow == -1 ? null : (JavaClass) getDisplayCache()[selectedRow][4];
        }
        
        public JavaClass getSelectedClass() {
            return getClassForRow(getSelectedRow());
        }
        
        public String getSelectedClassName() {
            return selectedRow == -1 ? null : (String)getDisplayCache()[selectedRow][0];
        }
        
        public void setSelectedClass(JavaClass jc) {
            if (jc != null) {
                Object[][] cache = getDisplayCache();
                int index = 0;
                for(Object[] row : cache) {
                    if (row[4].equals(jc)) {
                        setSelectedRow(index);
                        break;
                    }
                    index++;
                }
            } else {
                setSelectedRow(-1);
            }
        }
        
        public void preselect(JavaClass preselected) {
            this.preselectedClass = preselected;
        }
        
        private void setSelectedRow(int row) {
            int oldSelectedRow = selectedRow;
            selectedRow = row;
            pcs.firePropertyChange(SELECTED_ROW_PROPERTY, oldSelectedRow, selectedRow);
        }
        
        private int getSelectedRow() {
            return selectedRow;
        }
        
        private Object[][] getDisplayCache() {
            synchronized(displayCacheLock) {
                if (displayCache == null) {
                    final AtomicBoolean initInProgress = new AtomicBoolean(false);

                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    if (contents != null && initInProgress.get())
                                        contents.show(contentsPanel, NO_DATA);
                                }
                            });
                        }
                    }, 100);
                    
                    displayCache = classesListController.getData(
                         FilterComponent.getFilterStrings(filterValue), filterType,
                         showZeroInstances, showZeroSize, sortingColumn, sortingOrder, columnCount);
                    
                    initInProgress.set(false);
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (preselectedClass != null) {
                                setSelectedClass(preselectedClass);
                                preselectedClass = null;
                            }
                        }
                    });
                }
                return displayCache;
            }
        }
        
        private void resetDisplayCache() {
            synchronized(displayCacheLock) {
                displayCache = null;
                setSelectedRow(-1);
            }
        }
    }

    // --- Listeners -------------------------------------------------------------
    private class ClassesListTableMouseListener extends MouseAdapter {
        final private AtomicBoolean handled  = new AtomicBoolean();
        //~ Methods --------------------------------------------------------------------------------------------------------------

        private void updateSelection(int row, boolean toggle) {
            if (toggle) {
                int oldRow = realClassesListTableModel.getSelectedRow();
                if (oldRow == row) {
                    realClassesListTableModel.setSelectedRow(-1);
                    return;
                }
            }
            realClassesListTableModel.setSelectedRow(row);
        }

        private boolean isToggle(MouseEvent e) {
            return (e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) > 0;
        }
        
        public void mousePressed(final MouseEvent e) {
//            final int row = classesListTable.rowAtPoint(e.getPoint());
//            updateSelection(row, isToggle(e));
            if (e.isPopupTrigger()) {
                handled.set(true);
                int row = classesListTable.rowAtPoint(e.getPoint());
                updateSelection(row, isToggle(e));
                tablePopup.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (handled.compareAndSet(false, true)) {
                int row = classesListTable.rowAtPoint(e.getPoint());
                updateSelection(row, isToggle(e));
                if (e.isPopupTrigger()) tablePopup.show(e.getComponent(), e.getX(), e.getY());
                handled.set(false);
            }
        }

        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                int row = classesListTable.rowAtPoint(e.getPoint());
                if (row != -1) showInstancesForClass(realClassesListTableModel.getClassForRow(row));
            }
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String VIEW_TITLE = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                 "ClassesListControllerUI_ViewTitle"); // NOI18N
    private static final String NO_INSTANCES_MSG = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                       "ClassesListControllerUI_NoInstancesMsg"); // NOI18N
    private static final String NO_CLASS_IN_BASE_MSG = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                       "ClassesListControllerUI_NoClassInBaseMsg"); // NOI18N
    private static final String FILTER_STARTS_WITH = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                         "ClassesListControllerUI_FilterStartsWith"); // NOI18N
    private static final String FILTER_CONTAINS = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                      "ClassesListControllerUI_FilterContains"); // NOI18N
    private static final String FILTER_ENDS_WITH = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                       "ClassesListControllerUI_FilterEndsWith"); // NOI18N
    private static final String FILTER_REGEXP = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                    "ClassesListControllerUI_FilterRegexp"); // NOI18N
    private static final String FILTER_IMPLEMENTATION = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                            "ClassesListControllerUI_FilterImplementation"); // NOI18N
    private static final String FILTER_SUBCLASS = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                      "ClassesListControllerUI_FilterSubclass"); // NOI18N
    private static final String DEFAULT_FILTER_TEXT = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                          "ClassesListControllerUI_DefaultFilterText"); // NOI18N
    private static final String SHOW_IN_INSTANCES_STRING = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                               "ClassesListControllerUI_ShowInInstancesString"); // NOI18N
    private static final String SHOW_IMPLEMENTATIONS_STRING = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                                  "ClassesListControllerUI_ShowImplementationsString"); // NOI18N
    private static final String SHOW_SUBCLASSES_STRING = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                             "ClassesListControllerUI_ShowSubclassesString"); // NOI18N
    private static final String GO_TO_SOURCE_STRING = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                          "ClassesListControllerUI_GoToSourceString"); // NOI18N
    private static final String SHOW_HIDE_COLUMNS_STRING = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                               "ClassesListControllerUI_ShowHideColumnsString"); // NOI18N
    private static final String FILTER_CHECKBOX_TEXT = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                           "ClassesListControllerUI_FilterCheckboxText"); // NOI18N
    private static final String CLASSNAME_COLUMN_TEXT = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                            "ClassesListControllerUI_ClassNameColumnText"); // NOI18N
    private static final String CLASSNAME_COLUMN_DESCR = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                             "ClassesListControllerUI_ClassNameColumnDescr"); // NOI18N
    private static final String INSTANCES_REL_COLUMN_TEXT = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                                "ClassesListControllerUI_InstancesRelColumnText"); // NOI18N
    private static final String INSTANCES_REL_COLUMN_DESCR = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                                 "ClassesListControllerUI_InstancesRelColumnDescr"); // NOI18N
    private static final String INSTANCES_COLUMN_TEXT = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                            "ClassesListControllerUI_InstancesColumnText"); // NOI18N
    private static final String INSTANCES_COLUMN_DESCR = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                             "ClassesListControllerUI_InstancesColumnDescr"); // NOI18N
    private static final String SIZE_COLUMN_TEXT = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                       "ClassesListControllerUI_SizeColumnText"); // NOI18N
    private static final String SIZE_COLUMN_DESCR = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                        "ClassesListControllerUI_SizeColumnDescr"); // NOI18N
    private static final String FITERING_PROGRESS_TEXT = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                             "ClassesListControllerUI_FilteringProgressText"); // NOI18N
    private static final String CLASSES_TABLE_ACCESS_NAME = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                             "ClassesListControllerUI_ClassesTableAccessName"); // NOI18N
    private static final String CLASSES_TABLE_ACCESS_DESCR = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                                  "ClassesListControllerUI_ClassesTableAccessDescr"); // NOI18N
    private static final String COMPARE_WITH_ANOTHER_TEXT = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                                  "ClassesListControllerUI_CompareWithAnotherText"); // NOI18N
    private static final String COMPARING_MSG = NbBundle.getMessage(ClassesListControllerUI.class,
                                                                                  "ClassesListControllerUI_ComparingMsg"); // NOI18N
                                                                                                                                       // -----
    private static Icon ICON_CLASSES = Icons.getIcon(HeapWalkerIcons.CLASSES);
    private static String filterValue = ""; // NOI18N
    private static int filterType = CommonConstants.FILTER_CONTAINS;

    // --- UI definition ---------------------------------------------------------
    private static final String DATA = "Data"; // NOI18N
    private static final String NO_DATA = "No data"; // NOI18N

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private CardLayout contents;
    private ClassesListController classesListController;
    private ClassesListTableModel realClassesListTableModel = new ClassesListTableModel();
    private ExtendedTableModel classesListTableModel = new ExtendedTableModel(realClassesListTableModel);
    private FilterComponent filterComponent;
    private JExtendedTable classesListTable;
    private JPanel contentsPanel;
    private JPopupMenu cornerPopup;
    private JPopupMenu tablePopup;
    private String selectedRowContents;
    private javax.swing.table.TableCellRenderer[] columnRenderers;
    private int[] columnWidths;
    private boolean hasProjectContext;
    private boolean internalCornerButtonClick = false; // flag for closing columns popup by pressing cornerButton

    // --- Selection utils -------------------------------------------------------
    private boolean selectionSaved = false;
    private boolean showZeroInstances = true;
    private boolean showZeroSize = true;
    
    // --- Private implementation ------------------------------------------------
    private boolean isDiff = false;
    
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    // --- Constructors ----------------------------------------------------------
    public ClassesListControllerUI(final ClassesListController classesListController) {
        super(VIEW_TITLE, ICON_CLASSES, true);

        this.classesListController = classesListController;
        hasProjectContext = classesListController.getClassesController().getHeapFragmentWalker().getHeapDumpProject() != null;

        classesListTableModel.setInitialSorting(realClassesListTableModel.getSortingColumn(), realClassesListTableModel.getSortingOrder());
        
        initColumnsData();
        initComponents();
        
        realClassesListTableModel.addPropertyChangeListener(ClassesListTableModel.SELECTED_ROW_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                int row = (Integer)evt.getNewValue();
                if (row != -1) {
                    classesListTable.setRowSelectionInterval(row, row);
                    classesListTable.ensureRowVisible(row);
                    classesListController.classSelected(realClassesListTableModel.getSelectedClass());
                } else {
                    classesListTable.clearSelection();
                    classesListController.classSelected(null);
                }
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                adjustRenderers();
                restoreSelection();
                if (contents != null) contents.show(contentsPanel, DATA);
            }
        });
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setColumnVisibility(int column, boolean columnVisible) {
        boolean isColumnVisible = classesListTableModel.isRealColumnVisible(column);

        if (isColumnVisible == columnVisible) {
            return;
        }

        saveSelection();

        boolean sortResults = false;
        int currentSortingColumn = classesListTableModel.getSortingColumn();
        int realSortingColumn = classesListTableModel.getRealColumn(currentSortingColumn);

        // Current sorting column is going to be hidden
        if (isColumnVisible && (column == realSortingColumn)) {
            // Try to set next column as a currentSortingColumn. If currentSortingColumn is the last column,
            // set previous column as a sorting Column (one column is always visible).
            currentSortingColumn = ((currentSortingColumn + 1) == classesListTableModel.getColumnCount())
                                   ? (currentSortingColumn - 1) : (currentSortingColumn + 1);
            realSortingColumn = classesListTableModel.getRealColumn(currentSortingColumn);
            sortResults = true;
        }

        classesListTableModel.setRealColumnVisibility(column, columnVisible);
        classesListTable.createDefaultColumnsFromModel();
        classesListTableModel.setTable(classesListTable); // required to restore table header renderer
        currentSortingColumn = classesListTableModel.getVirtualColumn(realSortingColumn);

        if (sortResults) {
            realClassesListTableModel.resetDisplayCache();
        }
        classesListTableModel.setInitialSorting(currentSortingColumn, classesListTableModel.getSortingOrder());
        classesListTable.getTableHeader().repaint();
        setColumnsData(true);
        restoreSelection();

        // TODO [ui-persistence]
    }

    public void ensureWillBeVisible(JavaClass javaClass) {
        // TODO: add showZeroSize and showZeroInstances checking
        if (ClassesListController.matchesFilter(javaClass, FilterComponent.getFilterStrings(filterValue), filterType,
                                                    showZeroInstances, showZeroSize)) {
            return;
        }

        //    if (ClassesListController.matchesFilter(javaClass, FilterComponent.getFilterStrings(filterValue + " " + javaClass.getName()), filterType, showZeroInstances, showZeroSize)) { // NOI18N
        //      filterComponent.setFilterString(filterValue + " " + javaClass.getName()); // NOI18N
        //      return;
        //    }
        filterComponent.setFilterString(""); // NOI18N
    }

    // --- Public interface ------------------------------------------------------
    public void selectClass(final JavaClass javaClass) {
        realClassesListTableModel.setSelectedClass(javaClass);
    }

    public void updateData() {
        updateTableRenderers();
        realClassesListTableModel.resetDisplayCache();
    }
    
    protected void initColumnSelectorItems() {
        cornerPopup.removeAll();

        JCheckBoxMenuItem menuItem;

        for (int i = 0; i < realClassesListTableModel.getColumnCount(); i++) {
            menuItem = new JCheckBoxMenuItem(realClassesListTableModel.getColumnName(i));
            menuItem.setActionCommand(Integer.valueOf(i).toString());
            addMenuItemListener(menuItem);

            if (classesListTable != null) {
                menuItem.setState(classesListTableModel.isRealColumnVisible(i));

                if (i == 0) {
                    menuItem.setEnabled(false);
                }
            } else {
                menuItem.setState(true);
            }

            cornerPopup.add(menuItem);
        }

        cornerPopup.addSeparator();

        JCheckBoxMenuItem filterMenuItem = new JCheckBoxMenuItem(FILTER_CHECKBOX_TEXT);
        filterMenuItem.setActionCommand("Filter"); // NOI18N
        addMenuItemListener(filterMenuItem);

        if (filterComponent == null) {
            filterMenuItem.setState(true);
        } else {
            filterMenuItem.setState(filterComponent.isVisible());
        }

        cornerPopup.add(filterMenuItem);

        cornerPopup.pack();
    }

    protected void saveColumnsData() {
        TableColumnModel colModel = classesListTable.getColumnModel();

        for (int i = 0; i < classesListTableModel.getColumnCount(); i++) {
            int index = classesListTableModel.getRealColumn(i);

            if (index != 0) {
                columnWidths[index - 1] = colModel.getColumn(i).getPreferredWidth();
            }
        }
    }

    private void setColumnsData(boolean widths) {
        TableColumnModel colModel = classesListTable.getColumnModel();

        for (int i = 0; i < classesListTableModel.getColumnCount(); i++) {
            int index = classesListTableModel.getRealColumn(i);

            if (widths && index != 0) {
                colModel.getColumn(i).setPreferredWidth(columnWidths[index - 1]);
            }

            colModel.getColumn(i).setCellRenderer(columnRenderers[index]);
        }
    }

    private void addMenuItemListener(JCheckBoxMenuItem menuItem) {
        menuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (e.getActionCommand().equals("Filter")) { // NOI18N
                        filterComponent.setVisible(!filterComponent.isVisible());

                        return;
                    }

                    int column = Integer.parseInt(e.getActionCommand());
                    setColumnVisibility(column, !classesListTableModel.isRealColumnVisible(column));
                }
            });
    }

    private JButton createHeaderPopupCornerButton(final JPopupMenu headerPopup) {
        final JButton cornerButton = new JButton(Icons.getIcon(GeneralIcons.HIDE_COLUMN));
        cornerButton.setToolTipText(SHOW_HIDE_COLUMNS_STRING);
        cornerButton.setDefaultCapable(false);

        if (UIUtils.isWindowsClassicLookAndFeel()) {
            cornerButton.setMargin(new Insets(0, 0, 2, 2));
        } else if (UIUtils.isWindowsXPLookAndFeel()) {
            cornerButton.setMargin(new Insets(0, 0, 0, 1));
        } else if (UIUtils.isMetalLookAndFeel()) {
            cornerButton.setMargin(new Insets(0, 0, 2, 1));
        }

        cornerButton.addKeyListener(new KeyAdapter() {
                public void keyPressed(final KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                        showColumnSelectionPopup(headerPopup, cornerButton);
                    }
                }
            });

        cornerButton.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent mouseEvent) {
                    if (headerPopup.isVisible()) {
                        internalCornerButtonClick = true;
                        cornerButton.getModel().setArmed(false);
                    } else {
                        internalCornerButtonClick = false;

                        if (mouseEvent.getModifiers() == InputEvent.BUTTON3_MASK) {
                            showColumnSelectionPopup(headerPopup, cornerButton);
                        }
                    }
                }

                public void mouseClicked(MouseEvent mouseEvent) {
                    if ((mouseEvent.getModifiers() == InputEvent.BUTTON1_MASK) && (!internalCornerButtonClick)) {
                        showColumnSelectionPopup(headerPopup, cornerButton);
                    }
                }
            });

        return cornerButton;
    }

    private JPopupMenu createTablePopup() {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem showInstancesItem = new JMenuItem(SHOW_IN_INSTANCES_STRING);
        showInstancesItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    performDefaultAction();
                }
            });
        showInstancesItem.setFont(popup.getFont().deriveFont(Font.BOLD));

        JMenuItem showInstancesOfItem = new JMenuItem(hasProjectContext ? SHOW_IMPLEMENTATIONS_STRING : SHOW_SUBCLASSES_STRING);
        showInstancesOfItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int row = classesListTable.getSelectedRow();

                    if (row != -1) {
                        showSubclassesForClass(realClassesListTableModel.getSelectedClass());
                    }
                }
            });

        JMenuItem showSourceItem = null;
        if (GoToSource.isAvailable()) {
            showSourceItem = new JMenuItem(GO_TO_SOURCE_STRING);
            showSourceItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        int row = classesListTable.getSelectedRow();

                        if (row != -1) {
                            String className = realClassesListTableModel.getSelectedClassName();

                            while (className.endsWith("[]")) { // NOI18N
                                className = className.substring(0, className.length() - 2);
                            }
                            Lookup.Provider p = classesListController.getClassesController().getHeapFragmentWalker().getHeapDumpProject();
                            GoToSource.openSource(p, className, null, null);
                        }
                    }
                });
        }

        popup.add(showInstancesItem);
        popup.add(showInstancesOfItem);
        if (showSourceItem != null) {
            popup.addSeparator();
            popup.add(showSourceItem);
        }

        return popup;
    }

    private void initColumnsData() {
        int columnCount = classesListTableModel.getColumnCount();
        columnWidths = new int[columnCount - 1]; // Width of the first column fits to width
        columnRenderers = new javax.swing.table.TableCellRenderer[columnCount];

        int maxWidth = getFontMetrics(getFont()).charWidth('W') * 12; // NOI18N // initial width of data columns

        ClassNameTableCellRenderer classNameCellRenderer = new ClassNameTableCellRenderer();
        CustomBarCellRenderer customBarCellRenderer = new CustomBarCellRenderer(0, 100);
        LabelBracketTableCellRenderer dataCellRenderer = new LabelBracketTableCellRenderer(JLabel.TRAILING);

        // method / class / package name
        columnRenderers[0] = classNameCellRenderer;

        columnWidths[1 - 1] = maxWidth;
        columnRenderers[1] = customBarCellRenderer;

        columnWidths[2 - 1] = maxWidth;
        columnRenderers[2] = dataCellRenderer;

        columnWidths[3 - 1] = maxWidth;
        columnRenderers[3] = dataCellRenderer;
    }
    
    private HTMLLabel l;
    private JLabel w;
    private JProgressBar p;
    
    protected Component[] getAdditionalControls() {
        if (l == null) {
            l = new HTMLLabel() {
                protected void showURL(URL url) {
                    if (classesListController.isDiff()) {
                        classesListController.resetDiffAction();
                    } else {
                        classesListController.compareAction();
                    }
                }
            };
            l.setBorder(BorderFactory.createEmptyBorder());
            l.setFont(UIManager.getFont("ToolTip.font")); // NOI18N
            l.setText("<nobr><a href='#'>" + COMPARE_WITH_ANOTHER_TEXT + "</a></nobr>"); // NOI18N
        }
        
        if (w == null) {
            w = new JLabel(COMPARING_MSG);
            w.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            w.setFont(UIManager.getFont("ToolTip.font")); // NOI18N
        }
        
        if (p == null) {
            p = new JProgressBar() {
                public Dimension getPreferredSize() {
                    Dimension d = l.getPreferredSize();
                    d.width = 130;
                    return d;
                }
                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
            p.setIndeterminate(true);
        }
        
        JPanel indent = new JPanel(null);
        indent.setOpaque(false);
        indent.setPreferredSize(new Dimension(5, 5));
        indent.setMinimumSize(indent.getPreferredSize());
        
        w.setVisible(false);
        p.setVisible(false);
        l.setVisible(true);
        
        return new Component[] { w, p, l, indent };
    }
    
    public void showDiffProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                w.setVisible(true);
                p.setVisible(true);
                l.setVisible(false);
            }
        });
    }
    
    public void hideDiffProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                w.setVisible(false);
                p.setVisible(false);
                
                if (classesListController.isDiff()) {
                    l.setText("<nobr>" + NbBundle.getMessage(ClassesListControllerUI.class, // NOI18N
                              "ClassesListControllerUI_ShowingDiffText", "<a href='#'>", "</a>") + "</nobr>"); // NOI18N
                } else {
                    l.setText("<nobr><a href='#'>" + COMPARE_WITH_ANOTHER_TEXT + "</a></nobr>"); // NOI18N
                }
                l.setVisible(true);
            }
        });
    }

    private void initComponents() {
        classesListTable = new JExtendedTable(classesListTableModel) {
                public void doLayout() {
                    int columnsWidthsSum = 0;
                    int realFirstColumn = -1;

                    TableColumnModel colModel = getColumnModel();

                    for (int i = 0; i < classesListTableModel.getColumnCount(); i++) {
                        if (classesListTableModel.getRealColumn(i) == 0) {
                            realFirstColumn = i;
                        } else {
                            columnsWidthsSum += colModel.getColumn(i).getPreferredWidth();
                        }
                    }

                    if (realFirstColumn != -1) {
                        colModel.getColumn(realFirstColumn).setPreferredWidth(getWidth() - columnsWidthsSum);
                    }

                    super.doLayout();
                }
                ;
            };
        classesListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        classesListTable.addMouseListener(new ClassesListTableMouseListener());
        classesListTable.addKeyListener(new ClassesListTableKeyListener());
        classesListTable.setGridColor(UIConstants.TABLE_VERTICAL_GRID_COLOR);
        classesListTable.setSelectionBackground(UIConstants.TABLE_SELECTION_BACKGROUND_COLOR);
        classesListTable.setSelectionForeground(UIConstants.TABLE_SELECTION_FOREGROUND_COLOR);
        classesListTable.setShowHorizontalLines(UIConstants.SHOW_TABLE_HORIZONTAL_GRID);
        classesListTable.setShowVerticalLines(UIConstants.SHOW_TABLE_VERTICAL_GRID);
        classesListTable.setRowMargin(UIConstants.TABLE_ROW_MARGIN);
        classesListTable.setRowHeight(UIUtils.getDefaultRowHeight() + 2);
        classesListTableModel.setTable(classesListTable);
        classesListTable.getColumnModel().getColumn(0).setMinWidth(150);
        classesListTable.getAccessibleContext().setAccessibleName(CLASSES_TABLE_ACCESS_NAME);
        classesListTable.getAccessibleContext().setAccessibleDescription(CLASSES_TABLE_ACCESS_DESCR);
        classesListTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "DEFAULT_ACTION"); // NOI18N
        classesListTable.getActionMap().put("DEFAULT_ACTION", // NOI18N
                                            new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    performDefaultAction();
                }
            });

        // Disable traversing table cells using TAB and Shift+TAB
        Set keys = new HashSet(classesListTable.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        keys.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
        classesListTable.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, keys);

        keys = new HashSet(classesListTable.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        keys.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK));
        classesListTable.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, keys);

        setColumnsData(true);

        filterComponent = new FilterComponent();
        filterComponent.addFilterItem(Icons.getImageIcon(GeneralIcons.FILTER_STARTS_WITH),
                FILTER_STARTS_WITH, CommonConstants.FILTER_STARTS_WITH);
        filterComponent.addFilterItem(Icons.getImageIcon(GeneralIcons.FILTER_CONTAINS),
                FILTER_CONTAINS, CommonConstants.FILTER_CONTAINS);
        filterComponent.addFilterItem(Icons.getImageIcon(GeneralIcons.FILTER_ENDS_WITH),
                FILTER_ENDS_WITH, CommonConstants.FILTER_ENDS_WITH);
        filterComponent.addFilterItem(Icons.getImageIcon(GeneralIcons.FILTER_REG_EXP),
                FILTER_REGEXP, CommonConstants.FILTER_REGEXP);
        filterComponent.addFilterItem(Icons.getImageIcon(LanguageIcons.CLASS),
                hasProjectContext ? FILTER_IMPLEMENTATION : FILTER_SUBCLASS,
                ClassesListController.FILTER_SUBCLASS);
        filterComponent.setEmptyFilterText(DEFAULT_FILTER_TEXT);
        filterComponent.setFilterValues(filterValue, filterType);
        filterComponent.addFilterListener(new FilterComponent.FilterListener() {
                public void filterChanged() {
                    JavaClass selected = realClassesListTableModel.getSelectedClass();
                    filterValue = filterComponent.getFilterString();
                    filterType = filterComponent.getFilterType();
                    realClassesListTableModel.resetDisplayCache();
                    realClassesListTableModel.preselect(selected);
                    classesListTableModel.fireTableDataChanged();
                }
            });

        tablePopup = createTablePopup();

        cornerPopup = new JPopupMenu();

        JExtendedTablePanel tablePanel = new JExtendedTablePanel(classesListTable);
        tablePanel.setCorner(JScrollPane.UPPER_RIGHT_CORNER, createHeaderPopupCornerButton(cornerPopup));

        setLayout(new BorderLayout());

        JPanel noDataPanel = new JPanel(new BorderLayout());
        noDataPanel.setBorder(BorderFactory.createLoweredBevelBorder());

        HTMLTextArea hintArea = new HTMLTextArea();
        hintArea.setBorder(BorderFactory.createEmptyBorder(10, 8, 8, 8));

        String progressRes = Icons.getResource(HeapWalkerIcons.PROGRESS);
        String hintText = "<img border='0' align='bottom' src='nbresloc:/" + progressRes + "'>&nbsp;&nbsp;" // NOI18N
                          + FITERING_PROGRESS_TEXT;
        hintArea.setText(hintText);
        noDataPanel.add(hintArea, BorderLayout.CENTER);

        contents = new CardLayout();
        contentsPanel = new JPanel(contents);
        contentsPanel.add(tablePanel, DATA);
        contentsPanel.add(noDataPanel, NO_DATA);
        contents.show(contentsPanel, NO_DATA);

        add(contentsPanel, BorderLayout.CENTER);
        add(filterComponent, BorderLayout.SOUTH);

        classesListTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                restoreSelection();
            }
        });
    }

//    private void asyncInitData() {
//        RequestProcessor.getDefault().post(new Runnable() {
//            @Override
//            public void run() {
//                initData();
//            }
//        });
//    }
    
//    /**
//     * #192918
//     * This semaphore guards access to the class list model.
//     * 
//     * "initData" method takes the only one permission from this semaphore and 
//     * returns it after "classListTableModel" has been refreshed.
//     * All other methods wishing to directly or indirectly modify the "classListTableModel" (eg. setClass())
//     * must try to obtain a permission from a thread *DIFFERENT FROM EDT* as "initData()" method defers
//     * parts of its execution via SwingUtilities.invokeLater() and obtaining the permission form EDT
//     * will lead to inevitable deadlock.
//     */
//    final Semaphore classListInitToken = new Semaphore(1);
//    private void initData() {
//        classListInitToken.acquireUninterruptibly();
//        if (displayCache == null) displayCache = new Object[0][columnCount + 1];
//
//        CommonUtils.runInEventDispatchThread(new Runnable() {
//            public void run() {
//                try {
//                    final AtomicBoolean initInProgress = new AtomicBoolean(false);
//
//                    RequestProcessor.getDefault().post(new Runnable() {
//                        public void run() {
//                            SwingUtilities.invokeLater(new Runnable() {
//                                public void run() {
//                                    if (contents != null && initInProgress.get())
//                                        contents.show(contentsPanel, NO_DATA);
//                                }
//                            });
//                        }
//                    }, 100);
//
//                    saveSelection();
//
//                    BrowserUtils.performTask(new Runnable() {
//                        public void run() {
//                            try {
//                                initInProgress.set(true);
//
//                                final Object[][] displayCache2 = classesListController.getData(
//                                         FilterComponent.getFilterStrings(filterValue), filterType,
//                                         showZeroInstances, showZeroSize, sortingColumn, sortingOrder, columnCount);
//
//                                initInProgress.set(false);
//
//                                SwingUtilities.invokeLater(new Runnable() {
//                                    public void run() {
//                                        try {
//                                            if (isDiff != classesListController.isDiff()) {
//                                                isDiff = !isDiff;
//                                                CustomBarCellRenderer customBarCellRenderer = isDiff ?
//                                                        new DiffBarCellRenderer(classesListController.minDiff, classesListController.maxDiff) :
//                                                        new CustomBarCellRenderer(0, 100);
//                                                columnRenderers[1] = customBarCellRenderer;
//
//                                                TableCellRenderer dataCellRenderer = isDiff ?
//                                                        new LabelTableCellRenderer(JLabel.TRAILING) :
//                                                        new LabelBracketTableCellRenderer(JLabel.TRAILING);
//                                                columnRenderers[2] = dataCellRenderer;
//                                                columnRenderers[3] = dataCellRenderer;
//                                                setColumnsData(false);
//                                            }
//
//                                            displayCache = displayCache2;
//                                            classesListTableModel.fireTableDataChanged();
//                                            restoreSelection();
//                                            if (contents != null) contents.show(contentsPanel, DATA);
//                                        } finally {
//                                            classListInitToken.release();
//                                        }
//                                    }
//                                });
//                            } catch (Throwable t) {
//                                classListInitToken.release();
//                                t.printStackTrace();
//                            }
//                        }
//                    });
//                } catch (Throwable t) {
//                    classListInitToken.release();
//                    t.printStackTrace();
//                } 
//            }
//        });
//    }

    private void performDefaultAction() {
        int row = classesListTable.getSelectedRow();

        if (row != -1) {
            showInstancesForClass(realClassesListTableModel.getSelectedClass());
        }
    }

    private void restoreSelection() {
        if (selectedRowContents != null) {
            classesListTable.selectRowByContents(selectedRowContents, 0, true);
            selectedRowContents = null;
        }

        selectionSaved = false;
    }

    private void saveSelection() {
        if (selectionSaved) {
            return;
        }

        selectedRowContents = realClassesListTableModel.getSelectedClassName();

        selectionSaved = true;
    }

    private void showColumnSelectionPopup(final JPopupMenu headerPopup, final JButton cornerButton) {
        initColumnSelectorItems();
        headerPopup.show(cornerButton, cornerButton.getWidth() - headerPopup.getPreferredSize().width, cornerButton.getHeight());
    }

    private void showInstancesForClass(JavaClass jClass) {
        if (classesListController.isDiff() && jClass == null) {
            ProfilerDialogs.displayInfo(NO_CLASS_IN_BASE_MSG);
        } else if (jClass.getInstancesCount() == 0) {
            ProfilerDialogs.displayInfo(MessageFormat.format(NO_INSTANCES_MSG, new Object[] { jClass.getName() }));
        } else {
            classesListController.getClassesController().getHeapFragmentWalker().showInstancesForClass(jClass);
        }
    }

    private void showSubclassesForClass(JavaClass jClass) {
        saveSelection();
        filterComponent.setFilterValues(jClass.getName(),ClassesListController.FILTER_SUBCLASS);
    }
    
    private void adjustRenderers() {
        if (isDiff != classesListController.isDiff()) {
            isDiff = !isDiff;
            CustomBarCellRenderer customBarCellRenderer = isDiff ?
                    new DiffBarCellRenderer(classesListController.minDiff, classesListController.maxDiff) :
                    new CustomBarCellRenderer(0, 100);
            columnRenderers[1] = customBarCellRenderer;

            TableCellRenderer dataCellRenderer = isDiff ?
                    new LabelTableCellRenderer(JLabel.TRAILING) :
                    new LabelBracketTableCellRenderer(JLabel.TRAILING);
            columnRenderers[2] = dataCellRenderer;
            columnRenderers[3] = dataCellRenderer;
            setColumnsData(false);
        }

        classesListTableModel.fireTableDataChanged();
    }
    
    private void updateTableRenderers() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                adjustRenderers();
            }
        });
    }
}
