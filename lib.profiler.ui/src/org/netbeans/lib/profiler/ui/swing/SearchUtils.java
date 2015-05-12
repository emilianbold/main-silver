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
package org.netbeans.lib.profiler.ui.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.CloseButton;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.spi.ActionsSupportProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
public final class SearchUtils {
    
    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.swing.Bundle"); // NOI18N
    public static final String ACTION_FIND = messages.getString("SearchUtils_ActionFind"); // NOI18N
    private static final String MSG_NODATA = messages.getString("SearchUtils_MsgNoData"); // NOI18N
    private static final String MSG_NOTFOUND = messages.getString("SearchUtils_MsgNotFound"); // NOI18N
    private static final String SIDEBAR_CAPTION = messages.getString("SearchUtils_SidebarCaption"); // NOI18N
    private static final String BTN_PREVIOUS = messages.getString("SearchUtils_BtnPrevious"); // NOI18N
    private static final String BTN_PREVIOUS_TOOLTIP = messages.getString("SearchUtils_BtnPreviousTooltip"); // NOI18N
    private static final String BTN_NEXT = messages.getString("SearchUtils_BtnNext"); // NOI18N
    private static final String BTN_NEXT_TOOLTIP = messages.getString("SearchUtils_BtnNextTooltip"); // NOI18N
    private static final String BTN_CLOSE_TOOLTIP = messages.getString("SearchUtils_BtnCloseTooltip"); // NOI18N
    // -----
    
    public static final String FIND_ACTION_KEY = "find-action-key"; // NOI18N
    
    public static boolean findString(ProfilerTable table, String text, boolean next) {
        int rowCount = table.getRowCount();
        
        ProfilerTreeTable treeTable = null;
        
        if (rowCount == 0) {
            ProfilerDialogs.displayWarning(MSG_NODATA, ACTION_FIND, null);
            return false;
        } else if (rowCount == 1) {
            if (!(table instanceof ProfilerTreeTable)) return false;
            
            treeTable = (ProfilerTreeTable)table;
            TreeNode node = treeTable.getValueForRow(0);
            if (node == null || node.isLeaf()) return false;
        }
        
        text = text.toLowerCase();
        
        int mainColumn = table.convertColumnIndexToView(table.getMainColumn());
        
        if (treeTable != null || table instanceof ProfilerTreeTable) {
            if (treeTable == null) treeTable = (ProfilerTreeTable)table;
            TreePath selectedPath = treeTable.getSelectionPath();
            if (selectedPath == null) selectedPath = treeTable.getRootPath();
            boolean firstPath = true;
            TreePath startPath = null;
            do {
                selectedPath = next ? treeTable.getNextPath(selectedPath) :
                                      treeTable.getPreviousPath(selectedPath);
                TreeNode node = (TreeNode)selectedPath.getLastPathComponent();
                if (treeTable.getStringValue(node, mainColumn).toLowerCase().contains(text)) {
                    treeTable.selectPath(selectedPath, true);
                    return true;
                }
                if (startPath == null) startPath = selectedPath;
                else if (firstPath) firstPath = false;
            } while (firstPath || !selectedPath.equals(startPath));
        } else {
            int selectedRow = table.getSelectedRow();
            boolean fromSelection = selectedRow != -1;
        
            if (!fromSelection) selectedRow = next ? 0 : rowCount - 1;
            else selectedRow = next ? table.getNextRow(selectedRow) :
                                      table.getPreviousRow(selectedRow);
        
            int searchSteps = fromSelection ? rowCount - 1 : rowCount;
            for (int i = 0; i < searchSteps; i++) {
                if (table.getStringValue(selectedRow, mainColumn).toLowerCase().contains(text)) {
                    table.selectRow(selectedRow, true);
                    return true;
                }
                selectedRow = next ? table.getNextRow(selectedRow) :
                                     table.getPreviousRow(selectedRow);
            }
        }
        
        ProfilerDialogs.displayInfo(MSG_NOTFOUND, ACTION_FIND, null);
        return false;
    }
    
    
    public static JComponent createSearchPanel(final ProfilerTable table) {
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
        if (UIUtils.isGTKLookAndFeel() || UIUtils.isNimbusLookAndFeel())
                toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.LINE_AXIS));
        toolbar.setBorder(BorderFactory.createEmptyBorder(getTopMargin(), 2, getBottomMargin(), 2));
        toolbar.setBorderPainted(false);
        toolbar.setRollover(true);
        toolbar.setFloatable(false);
        toolbar.setOpaque(false);
        
        toolbar.add(Box.createHorizontalStrut(6));
        toolbar.add(new JLabel(SIDEBAR_CAPTION));
        toolbar.add(Box.createHorizontalStrut(3));
        
        final EditableHistoryCombo combo = new EditableHistoryCombo();        
        final JTextComponent textC = combo.getTextComponent();
        
        JPanel comboContainer = new JPanel(new BorderLayout());
        comboContainer.add(combo, BorderLayout.CENTER);
        comboContainer.setMinimumSize(combo.getMinimumSize());
        comboContainer.setPreferredSize(combo.getPreferredSize());
        comboContainer.setMaximumSize(combo.getMaximumSize());
        
        toolbar.add(comboContainer);
        
        toolbar.add(Box.createHorizontalStrut(5));
        
        KeyStroke escKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        KeyStroke prevKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK);
        KeyStroke nextKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        
        final JButton prev = new JButton(BTN_PREVIOUS, Icons.getIcon(GeneralIcons.FIND_PREVIOUS)) {
            protected void fireActionPerformed(ActionEvent e) {
                super.fireActionPerformed(e);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        String search = getSearchString(combo);
                        if (search == null || search.isEmpty()) return;
                        if (findString(table, search, false)) combo.addItem(search);
                    }
                });
            }
        };
        String prevAccelerator = UIUtils.keyAcceleratorString(prevKey);
        prev.setToolTipText(MessageFormat.format(BTN_PREVIOUS_TOOLTIP, prevAccelerator));
        prev.setEnabled(false);
        toolbar.add(prev);
        
        toolbar.add(Box.createHorizontalStrut(2));
        
        final JButton next = new JButton(BTN_NEXT, Icons.getIcon(GeneralIcons.FIND_NEXT)) {
            protected void fireActionPerformed(ActionEvent e) {
                super.fireActionPerformed(e);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        String search = getSearchString(combo);
                        if (search == null || search.isEmpty()) return;
                        if (findString(table, search, true)) combo.addItem(search);
                    }
                });
            }
        };
        String nextAccelerator = UIUtils.keyAcceleratorString(nextKey);
        next.setToolTipText(MessageFormat.format(BTN_NEXT_TOOLTIP, nextAccelerator));
        next.setEnabled(false);
        toolbar.add(next);
        
        toolbar.add(Box.createHorizontalStrut(2));
        
        combo.setOnTextChangeHandler(new Runnable() {
            public void run() {
                boolean enable = !combo.getText().trim().isEmpty();
                prev.setEnabled(enable);
                next.setEnabled(enable);
            }
        });
        
        final JPanel panel = new JPanel(new BorderLayout()) {
            public void setVisible(boolean visible) {
                super.setVisible(visible);
                if (!visible) table.requestFocusInWindow();
            }
            public boolean requestFocusInWindow() {
                if (textC != null) textC.selectAll();
                return combo.requestFocusInWindow();
            }
        };
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("controlShadow"))); // NOI18N
        panel.add(toolbar, BorderLayout.CENTER);
        
        final Runnable hider = new Runnable() { public void run() { panel.setVisible(false); } };
        JButton closeButton = CloseButton.create(hider);
        String escAccelerator = UIUtils.keyAcceleratorString(escKey);
        closeButton.setToolTipText(MessageFormat.format(BTN_CLOSE_TOOLTIP, escAccelerator));
        panel.add(closeButton, BorderLayout.EAST);
        
        String HIDE = "hide-action"; // NOI18N
        InputMap map = panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        Action hiderAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) { hider.run(); }
        };
        panel.getActionMap().put(HIDE, hiderAction);
        map.put(escKey, HIDE);
        
        if (textC != null) {
            map = textC.getInputMap();
            String NEXT = "search-next-action"; // NOI18N
            Action nextAction = new AbstractAction() {
                public void actionPerformed(final ActionEvent e) {
                    if (combo.isPopupVisible()) combo.hidePopup();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() { if (next.isEnabled()) next.doClick(); }
                    });
                }
            };
            textC.getActionMap().put(NEXT, nextAction);
            map.put(nextKey, NEXT);

            String PREV = "search-prev-action"; // NOI18N
            Action prevAction = new AbstractAction() {
                public void actionPerformed(final ActionEvent e) {
                    if (combo.isPopupVisible()) combo.hidePopup();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() { if (next.isEnabled()) prev.doClick(); }
                    });
                }
            };
            textC.getActionMap().put(PREV, prevAction);
            map.put(prevKey, PREV);
        }
        
        return panel;
    }
    
    private static String getSearchString(EditableHistoryCombo combo) {
        String search = combo.getText();
        return search == null ? null : search.trim();
    }
    
    private static int getTopMargin() {
        if (UIUtils.isWindowsLookAndFeel() || UIUtils.isMetalLookAndFeel()) return 1;
        if (UIUtils.isAquaLookAndFeel() || UIUtils.isNimbusLookAndFeel() || UIUtils.isOracleLookAndFeel()) return 0;
        return 2;
    }
    
    private static int getBottomMargin() {
        if (UIUtils.isOracleLookAndFeel()) return 1;
        return 0;
    }
    
    
    // Do not create instances of this class
    private SearchUtils() {}
    
    
    // Default keybinding Ctrl+F for Find action
    private static interface Support { @ServiceProvider(service=ActionsSupportProvider.class, position=100)
        public static final class SearchActionProvider extends ActionsSupportProvider {
            public boolean registerAction(String actionKey, Action action, ActionMap actionMap, InputMap inputMap) {
                if (!FIND_ACTION_KEY.equals(actionKey)) return false;

                actionMap.put(actionKey, action);
                inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK), actionKey);

                return true;
            }
        }
    }
    
}
