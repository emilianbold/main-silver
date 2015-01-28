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
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.results.RuntimeCCTNode;
import org.netbeans.lib.profiler.results.cpu.CPUCCTProvider;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.netbeans.lib.profiler.results.cpu.FlatProfileContainer;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.JExtendedSplitPane;
import org.netbeans.lib.profiler.ui.memory.LiveMemoryView;
import org.netbeans.lib.profiler.ui.results.DataView;
import org.netbeans.lib.profiler.ui.swing.FilterUtils;
import org.netbeans.lib.profiler.ui.swing.SearchUtils;
import org.netbeans.lib.profiler.utils.Wildcards;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class LiveCPUView extends JPanel {

    private static final int MIN_UPDATE_DIFF = 900;
    private static final int MAX_UPDATE_DIFF = 1400;
    
    private final ProfilerClient client;
    private final ResultsMonitor rm;
    
    private DataView lastFocused;
    private CPUTableView tableView;
    private CPUTreeTableView treeTableView;
    
    private long lastupdate;
    private volatile boolean paused;
    private volatile boolean forceRefresh;
    
    @ServiceProvider(service=CPUCCTProvider.Listener.class)
    public static final class ResultsMonitor implements CPUCCTProvider.Listener {

        private LiveCPUView view;
        
        @Override
        public void cctEstablished(RuntimeCCTNode appRootNode, boolean empty) {
            if (view != null && !empty) {
                try {
                    view.refreshData(appRootNode);
                } catch (ClientUtils.TargetAppOrVMTerminated ex) {
                    Logger.getLogger(LiveMemoryView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        @Override
        public void cctReset() {
            if (view != null) {
                view.resetData();
            }
        }
    }
    
    public LiveCPUView(ProfilerClient client, Set<ClientUtils.SourceCodeSelection> selection) {
        this.client = client;
        
        initUI(selection);
        registerActions();
        
        rm = Lookup.getDefault().lookup(ResultsMonitor.class);
        rm.view = this;
    }
    
    
    public void setView(boolean callTree, boolean hotSpots) {
        treeTableView.setVisible(callTree);
        tableView.setVisible(hotSpots);
    }
    
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void setForceRefresh(boolean forceRefresh) {
        this.forceRefresh = forceRefresh;
    }

    private void refreshData(RuntimeCCTNode appRootNode) throws ClientUtils.TargetAppOrVMTerminated {
        if ((lastupdate + MIN_UPDATE_DIFF > System.currentTimeMillis() || paused) && !forceRefresh) return;
        try {
            final CPUResultsSnapshot snapshotData =
                    client.getStatus().getInstrMethodClasses() == null ?
                    null : client.getCPUProfilingResultsSnapshot(false);

            if (snapshotData != null) {
                final FlatProfileContainer flatData = snapshotData.getFlatProfile(
                        -1, CPUResultsSnapshot.METHOD_LEVEL_VIEW);
                
                final Map<Integer, ClientUtils.SourceCodeSelection> idMap = new HashMap();
                for (int i = 0; i < flatData.getNRows(); i++) // TODO: getNRows is filtered, may not work for tree data!
                    idMap.put(flatData.getMethodIdAtRow(i), flatData.getSourceCodeSelectionAtRow(i));

                final boolean sampled = client.getCurrentInstrType() == ProfilerClient.INSTR_NONE_SAMPLING;
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        treeTableView.setData(snapshotData, idMap, CPUResultsSnapshot.METHOD_LEVEL_VIEW, sampled);
                        tableView.setData(flatData, idMap, sampled);
                    }
                });
                lastupdate = System.currentTimeMillis();

            }
            forceRefresh = false;
        } catch (CPUResultsSnapshot.NoDataAvailableException e) {
        } catch (Throwable t) {
            if (t instanceof ClientUtils.TargetAppOrVMTerminated) {
                throw ((ClientUtils.TargetAppOrVMTerminated)t);
            } else {
                Logger.getLogger(LiveCPUView.class.getName()).log(Level.SEVERE, null, t);
            }
        }
    }
    
    public void refreshData() throws ClientUtils.TargetAppOrVMTerminated {
        if ((lastupdate + MAX_UPDATE_DIFF < System.currentTimeMillis() && !paused) || forceRefresh) {
            client.forceObtainedResultsDump(true);
        }        
    }
    
    public void resetData() {
        treeTableView.resetData();
        tableView.resetData();
    }
    
    
    public void showSelectionColumn() {
        treeTableView.showSelectionColumn();
        tableView.showSelectionColumn();
    }
    
    public void refreshSelection() {
        treeTableView.refreshSelection();
        tableView.refreshSelection();
    }
    
    
    public abstract boolean showSourceSupported();
    
    public abstract void showSource(ClientUtils.SourceCodeSelection value);
    
    public abstract void selectForProfiling(ClientUtils.SourceCodeSelection value);
    
    public void popupShowing() {};
    
    public void popupHidden() {};
    
    
    private void profileMethod(ClientUtils.SourceCodeSelection value) {
        selectForProfiling(value);
    }
    
    private void profileClass(ClientUtils.SourceCodeSelection value) {
        selectForProfiling(new ClientUtils.SourceCodeSelection(
                           value.getClassName(), Wildcards.ALLWILDCARD, null));
    }
    
    
    private void initUI(Set<ClientUtils.SourceCodeSelection> selection) {
        setLayout(new BorderLayout(0, 0));
        
        treeTableView = new CPUTreeTableView(selection) {
            protected void performDefaultAction(ClientUtils.SourceCodeSelection userValue) {
                if (showSourceSupported()) showSource(userValue);
            }
            protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                LiveCPUView.this.populatePopup(treeTableView, popup, value, userValue);
            }
            protected void popupShowing() { LiveCPUView.this.popupShowing(); }
            protected void popupHidden()  { LiveCPUView.this.popupHidden(); }
            protected boolean hasBottomFilterFindMargin() { return true; }
        };
        treeTableView.notifyOnFocus(new Runnable() {
            public void run() { lastFocused = treeTableView; }
        });
        
        tableView = new CPUTableView(selection) {
            protected void performDefaultAction(ClientUtils.SourceCodeSelection userValue) {
                if (showSourceSupported()) showSource(userValue);
            }
            protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                LiveCPUView.this.populatePopup(tableView, popup, value, userValue);
            }
            protected void popupShowing() { LiveCPUView.this.popupShowing(); }
            protected void popupHidden()  { LiveCPUView.this.popupHidden(); }
            protected boolean hasBottomFilterFindMargin() { return true; }
        };
        tableView.notifyOnFocus(new Runnable() {
            public void run() { lastFocused = tableView; }
        });
        
        JSplitPane split = new JExtendedSplitPane(JSplitPane.VERTICAL_SPLIT) {
            {
                setBorder(null);
                setDividerSize(5);

                if (getUI() instanceof BasicSplitPaneUI) {
                    BasicSplitPaneDivider divider = ((BasicSplitPaneUI)getUI()).getDivider();
                    if (divider != null) {
                        Color c = UIUtils.isNimbus() ? UIUtils.getDisabledLineColor() :
                                new JSeparator().getForeground();
                        divider.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, c));
                    }
                }
            }
        };
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setTopComponent(treeTableView);
        split.setBottomComponent(tableView);
        split.setDividerLocation(0.5d);
        split.setResizeWeight(0.5d);
        
        add(split, BorderLayout.CENTER);
        
//        // TODO: read last state?
//        setView(true, false);
    }
    
    private void registerActions() {
        ActionMap map = getActionMap();
        
        map.put(FilterUtils.FILTER_ACTION_KEY, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                DataView active = getLastFocused();
                if (active != null) active.activateFilter();
            }
        });
        
        map.put(SearchUtils.FIND_ACTION_KEY, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                DataView active = getLastFocused();
                if (active != null) active.activateSearch();
            }
        });
    }
    
    private DataView getLastFocused() {
        if (lastFocused != null && !lastFocused.isShowing()) lastFocused = null;
        
        if (lastFocused == null) {
            if (treeTableView.isShowing()) lastFocused = treeTableView;
            else if (tableView.isShowing()) lastFocused = tableView;
        }
        
        return lastFocused;
    }
    
    private void populatePopup(final DataView invoker, JPopupMenu popup, Object value, final ClientUtils.SourceCodeSelection userValue) {
        if (showSourceSupported()) {
            popup.add(new JMenuItem("Go to Source") {
                { setEnabled(userValue != null); setFont(getFont().deriveFont(Font.BOLD)); }
                protected void fireActionPerformed(ActionEvent e) { showSource(userValue); }
            });
            popup.addSeparator();
        }
        
        popup.add(new JMenuItem("Profile Method") {
            { setEnabled(userValue != null && CPUTableView.isSelectable(userValue)); }
            protected void fireActionPerformed(ActionEvent e) { profileMethod(userValue); }
        });
        
        popup.add(new JMenuItem("Profile Class") {
            { setEnabled(userValue != null); }
            protected void fireActionPerformed(ActionEvent e) { profileClass(userValue); }
        });
        
        popup.addSeparator();
        popup.add(new JMenuItem("Filter") {
            protected void fireActionPerformed(ActionEvent e) { invoker.activateFilter(); }
        });
        popup.add(new JMenuItem("Find") {
            protected void fireActionPerformed(ActionEvent e) { invoker.activateSearch(); }
        });
    }
    
}
