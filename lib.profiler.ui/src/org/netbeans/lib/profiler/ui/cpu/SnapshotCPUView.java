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
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.netbeans.lib.profiler.results.cpu.FlatProfileContainer;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.JExtendedSplitPane;
import org.netbeans.lib.profiler.ui.components.ProfilerToolbar;
import org.netbeans.lib.profiler.ui.results.DataView;
import org.netbeans.lib.profiler.ui.swing.ActionPopupButton;
import org.netbeans.lib.profiler.ui.swing.ExportUtils;
import org.netbeans.lib.profiler.ui.swing.ExportUtils.ExportProvider;
import org.netbeans.lib.profiler.ui.swing.FilterUtils;
import org.netbeans.lib.profiler.ui.swing.GrayLabel;
import org.netbeans.lib.profiler.ui.swing.MultiButtonGroup;
import org.netbeans.lib.profiler.ui.swing.SearchUtils;
import org.netbeans.lib.profiler.utils.Wildcards;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class SnapshotCPUView extends JPanel {
    
    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.cpu.Bundle"); // NOI18N
    private static final String TOOLBAR_VIEW = messages.getString("SnapshotCPUView_ToolbarView"); // NOI18N
    private static final String VIEW_FORWARD = messages.getString("SnapshotCPUView_ViewForward"); // NOI18N
    private static final String VIEW_HOTSPOTS = messages.getString("SnapshotCPUView_ViewHotSpots"); // NOI18N
    private static final String VIEW_REVERSE = messages.getString("SnapshotCPUView_ViewReverse"); // NOI18N
    private static final String TOOLBAR_AGGREGATION = messages.getString("SnapshotCPUView_ToolbarAggregation"); // NOI18N
    private static final String AGGREGATION_METHODS = messages.getString("SnapshotCPUView_AggregationMethods"); // NOI18N
    private static final String AGGREGATION_CLASSES = messages.getString("SnapshotCPUView_AggregationClasses"); // NOI18N
    private static final String AGGREGATION_PACKAGES = messages.getString("SnapshotCPUView_AggregationPackages"); // NOI18N
    // -----
    
    private boolean sampled;
    private CPUResultsSnapshot snapshot;
    
    private int aggregation;
    private boolean mergedThreads;
    private Collection<Integer> selectedThreads;
    
    private DataView lastFocused;
    private CPUTableView hotSpotsView;
    private CPUTreeTableView forwardCallsView;
    private CPUTreeTableView reverseCallsView;
    
    private Component viewContainer;
    
    public SnapshotCPUView(CPUResultsSnapshot snapshot, boolean sampled, Action... actions) {
        initUI(actions);
        registerActions();
        
        aggregation = CPUResultsSnapshot.METHOD_LEVEL_VIEW;
        setSnapshot(snapshot, sampled);
    }
    
    
    public ExportUtils.Exportable getExportable(final File sourceFile) {
        return new ExportUtils.Exportable() {
            public String getName() {
                return CPUView.EXPORT_METHODS;
            }
            public ExportUtils.ExportProvider[] getProviders() {
                ExportUtils.ExportProvider[] providers = null;
                ExportProvider npsProvider = sourceFile == null ? null :
                    new ExportUtils.NPSExportProvider(sourceFile);
                
                if (hotSpotsView.isVisible() && !forwardCallsView.isVisible()) {
                    providers = hotSpotsView.getExportProviders();
                } else if (!hotSpotsView.isVisible() && forwardCallsView.isVisible()) {
                    providers = forwardCallsView.getExportProviders();
                }
                
                List<ExportUtils.ExportProvider> _providers = new ArrayList();
                if (npsProvider != null) _providers.add(npsProvider);
                if (providers != null) _providers.addAll(Arrays.asList(providers));
                _providers.add(new ExportUtils.PNGExportProvider(viewContainer));
                return _providers.toArray(new ExportUtils.ExportProvider[_providers.size()]);
            }
        };
    }
    
    
    public abstract boolean showSourceSupported();
    
    public abstract void showSource(ClientUtils.SourceCodeSelection value);
    
    public abstract void selectForProfiling(ClientUtils.SourceCodeSelection value);
    
    
    private void profileMethod(ClientUtils.SourceCodeSelection value) {
        selectForProfiling(value);
    }
    
    private void profileClass(ClientUtils.SourceCodeSelection value) {
        selectForProfiling(new ClientUtils.SourceCodeSelection(
                           value.getClassName(), Wildcards.ALLWILDCARD, null));
    }
    
    
    private void initUI(Action... actions) {
        setLayout(new BorderLayout(0, 0));
        
        forwardCallsView = new CPUTreeTableView(null, false) {
            protected void performDefaultAction(ClientUtils.SourceCodeSelection value) {
                if (showSourceSupported()) showSource(value);
            }
            protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                SnapshotCPUView.this.populatePopup(forwardCallsView, popup, value, userValue);
            }
        };
        forwardCallsView.notifyOnFocus(new Runnable() {
            public void run() { lastFocused = forwardCallsView; }
        });
        
        hotSpotsView = new CPUTableView(null) {
            protected void performDefaultAction(ClientUtils.SourceCodeSelection userValue) {
                if (showSourceSupported()) showSource(userValue);
            }
            protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                SnapshotCPUView.this.populatePopup(hotSpotsView, popup, value, userValue);
            }
        };
        hotSpotsView.notifyOnFocus(new Runnable() {
            public void run() { lastFocused = hotSpotsView; }
        });
        
        reverseCallsView = new CPUTreeTableView(null, true) {
            protected void performDefaultAction(ClientUtils.SourceCodeSelection value) {
                if (showSourceSupported()) showSource(value);
            }
            protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                SnapshotCPUView.this.populatePopup(reverseCallsView, popup, value, userValue);
            }
        };
        reverseCallsView.notifyOnFocus(new Runnable() {
            public void run() { lastFocused = reverseCallsView; }
        });
        
        JSplitPane upperSplit = new JExtendedSplitPane(JSplitPane.VERTICAL_SPLIT) {
            {
                setBorder(null);
                setDividerSize(5);

                if (getUI() instanceof BasicSplitPaneUI) {
                    BasicSplitPaneDivider divider = ((BasicSplitPaneUI)getUI()).getDivider();
                    if (divider != null) {
                        Color c = UIUtils.isNimbus() ? UIUtils.getDisabledLineColor() :
                                new JSeparator().getForeground();
                        divider.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, c));
                    }
                }
            }
        };
        upperSplit.setBorder(BorderFactory.createEmptyBorder());
        upperSplit.setTopComponent(forwardCallsView);
        upperSplit.setBottomComponent(hotSpotsView);
        upperSplit.setDividerLocation(0.5d);
        upperSplit.setResizeWeight(0.5d);
        
        JSplitPane lowerSplit = new JExtendedSplitPane(JSplitPane.VERTICAL_SPLIT) {
            {
                setBorder(null);
                setDividerSize(5);

                if (getUI() instanceof BasicSplitPaneUI) {
                    BasicSplitPaneDivider divider = ((BasicSplitPaneUI)getUI()).getDivider();
                    if (divider != null) {
                        Color c = UIUtils.isNimbus() ? UIUtils.getDisabledLineColor() :
                                new JSeparator().getForeground();
                        divider.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, c));
                    }
                }
            }
        };
        lowerSplit.setBorder(BorderFactory.createEmptyBorder());
        lowerSplit.setTopComponent(upperSplit);
        lowerSplit.setBottomComponent(reverseCallsView);
        lowerSplit.setDividerLocation(0.66d);
        lowerSplit.setResizeWeight(0.66d);
        
        add(lowerSplit, BorderLayout.CENTER);
        viewContainer = upperSplit;
        
        ProfilerToolbar toolbar = ProfilerToolbar.create(true);
        
        for (int i = 0; i < actions.length - 1; i++) {
            Action action = actions[i];
            if (action != null) {
                toolbar.add(action);
            } else {
                toolbar.addSpace(2);
                toolbar.addSeparator();
                toolbar.addSpace(2);
            }
        }
        
        if (actions.length > 0) {
            toolbar.addSpace(2);
            toolbar.addSeparator();
            toolbar.addSpace(5);
        }
        
        GrayLabel viewL = new GrayLabel(TOOLBAR_VIEW);
        toolbar.add(viewL);
        
        toolbar.addSpace(5);
        
        MultiButtonGroup group = new MultiButtonGroup();
        
        JToggleButton forwardCalls = new JToggleButton(Icons.getIcon(ProfilerIcons.NODE_FORWARD)) {
            protected void fireActionPerformed(ActionEvent e) {
                super.fireActionPerformed(e);
                setView(isSelected(), hotSpotsView.isVisible(), reverseCallsView.isVisible());
            }
        };
        forwardCalls.setToolTipText(VIEW_FORWARD);
        group.add(forwardCalls);
        toolbar.add(forwardCalls);
        forwardCallsView.setVisible(true);
        forwardCalls.setSelected(true);
        
        JToggleButton hotSpots = new JToggleButton(Icons.getIcon(ProfilerIcons.TAB_HOTSPOTS)) {
            protected void fireActionPerformed(ActionEvent e) {
                super.fireActionPerformed(e);
                setView(forwardCallsView.isVisible(), isSelected(), reverseCallsView.isVisible());
            }
        };
        hotSpots.setToolTipText(VIEW_HOTSPOTS);
        group.add(hotSpots);
        toolbar.add(hotSpots);
        hotSpotsView.setVisible(false);
        hotSpots.setSelected(false);
        
        JToggleButton reverseCalls = new JToggleButton(Icons.getIcon(ProfilerIcons.NODE_REVERSE)) {
            protected void fireActionPerformed(ActionEvent e) {
                super.fireActionPerformed(e);
                setView(forwardCallsView.isVisible(), hotSpotsView.isVisible(), isSelected());
            }
        };
        reverseCalls.setToolTipText(VIEW_REVERSE);
        group.add(reverseCalls);
        toolbar.add(reverseCalls);
        reverseCallsView.setVisible(false);
        reverseCalls.setSelected(false);
        
//        Action aCallTree = new AbstractAction() {
//            { putValue(NAME, VIEW_CALLTREE); }
//            public void actionPerformed(ActionEvent e) { setView(true, false); }
//            
//        };
//        Action aHotSpots = new AbstractAction() {
//            { putValue(NAME, VIEW_HOTSPOTS); }
//            public void actionPerformed(ActionEvent e) { setView(false, true); }
//            
//        };
//        Action aCombined = new AbstractAction() {
//            { putValue(NAME, VIEW_COMBINED); }
//            public void actionPerformed(ActionEvent e) { setView(true, true); }
//            
//        };
//        toolbar.add(new ActionPopupButton(2, aCallTree, aHotSpots, aCombined));
        
        toolbar.addSpace(5);
        ThreadsSelector threadsPopup = new ThreadsSelector() {
            public CPUResultsSnapshot getSnapshot() { return snapshot; }
            public void selectionChanged(Collection<Integer> selected, boolean mergeThreads) {
                mergedThreads = mergeThreads;
                selectedThreads = selected;
                setAggregation(aggregation);
            }
            
        };
        toolbar.add(threadsPopup);
        
        toolbar.addSpace(2);
        toolbar.addSeparator();
        toolbar.addSpace(5);
        
//        GrayLabel threadsL = new GrayLabel("Threads:");
//        toolbar.add(threadsL);
//        
//        toolbar.addSpace(2);
//        
//        PopupButton threads = new PopupButton("All threads") {
//            protected void populatePopup(JPopupMenu popup) {
//                popup.add(new JRadioButtonMenuItem("All threads"));
//                popup.add(new JRadioButtonMenuItem("main"));
//                popup.add(new JRadioButtonMenuItem("AWT-EventQueue-0"));
//            }
//        };
//        toolbar.add(threads);
//        
//        toolbar.addSpace(2);
//        toolbar.addSeparator();
//        toolbar.addSpace(5);
        
        GrayLabel aggregationL = new GrayLabel(TOOLBAR_AGGREGATION);
        toolbar.add(aggregationL);
        
        toolbar.addSpace(2);
        
        Action aMethods = new AbstractAction() {
            { putValue(NAME, AGGREGATION_METHODS); }
            public void actionPerformed(ActionEvent e) { setAggregation(CPUResultsSnapshot.METHOD_LEVEL_VIEW); }
            
        };
        Action aClasses = new AbstractAction() {
            { putValue(NAME, AGGREGATION_CLASSES); }
            public void actionPerformed(ActionEvent e) { setAggregation(CPUResultsSnapshot.CLASS_LEVEL_VIEW); }
            
        };
        Action aPackages = new AbstractAction() {
            { putValue(NAME, AGGREGATION_PACKAGES); }
            public void actionPerformed(ActionEvent e) { setAggregation(CPUResultsSnapshot.PACKAGE_LEVEL_VIEW); }
            
        };
        
        ActionPopupButton aggregation = new ActionPopupButton(aMethods, aClasses, aPackages);
        toolbar.add(aggregation);
        
        Action aInfo = actions.length > 0 ? actions[actions.length - 1] : null;
        if (aInfo != null) {
            toolbar.addFiller();
            toolbar.add(aInfo);
        }
        
        add(toolbar.getComponent(), BorderLayout.NORTH);
        
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
            if (forwardCallsView.isShowing()) lastFocused = forwardCallsView;
            else if (hotSpotsView.isShowing()) lastFocused = hotSpotsView;
            else if (reverseCallsView.isShowing()) lastFocused = reverseCallsView;
        }
        
        return lastFocused;
    }
    
    private void populatePopup(final DataView invoker, JPopupMenu popup, Object value, final ClientUtils.SourceCodeSelection userValue) {
        if (showSourceSupported()) {
            popup.add(new JMenuItem(CPUView.ACTION_GOTOSOURCE) {
                { setEnabled(userValue != null && aggregation != CPUResultsSnapshot.PACKAGE_LEVEL_VIEW); setFont(getFont().deriveFont(Font.BOLD)); }
                protected void fireActionPerformed(ActionEvent e) { showSource(userValue); }
            });
            popup.addSeparator();
        }
        
        popup.add(new JMenuItem(CPUView.ACTION_PROFILE_METHOD) {
            { setEnabled(userValue != null && aggregation == CPUResultsSnapshot.METHOD_LEVEL_VIEW && CPUTableView.isSelectable(userValue)); }
            protected void fireActionPerformed(ActionEvent e) { profileMethod(userValue); }
        });
        
        popup.add(new JMenuItem(CPUView.ACTION_PROFILE_CLASS) {
            { setEnabled(userValue != null && aggregation != CPUResultsSnapshot.PACKAGE_LEVEL_VIEW); }
            protected void fireActionPerformed(ActionEvent e) { profileClass(userValue); }
        });
        
        customizeNodePopup(invoker, popup, value, userValue);
        
        popup.addSeparator();
        popup.add(new JMenuItem(FilterUtils.ACTION_FILTER) {
            protected void fireActionPerformed(ActionEvent e) { invoker.activateFilter(); }
        });
        popup.add(new JMenuItem(SearchUtils.ACTION_FIND) {
            protected void fireActionPerformed(ActionEvent e) { invoker.activateSearch(); }
        });
        
    }
    
    protected void customizeNodePopup(DataView invoker, JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {}
    
    private void setView(boolean forwardCalls, boolean hotSpots, boolean reverseCalls) {
        forwardCallsView.setVisible(forwardCalls);
        hotSpotsView.setVisible(hotSpots);
        reverseCallsView.setVisible(reverseCalls);
    }
    
    private void setAggregation(int _aggregation) {
        aggregation = _aggregation;
        
        final FlatProfileContainer flatData = snapshot.getFlatProfile(selectedThreads, aggregation);

        final Map<Integer, ClientUtils.SourceCodeSelection> idMap = new HashMap();
        for (int i = 0; i < flatData.getNRows(); i++) // TODO: getNRows is filtered, may not work for tree data!
            idMap.put(flatData.getMethodIdAtRow(i), flatData.getSourceCodeSelectionAtRow(i));
//        SwingUtilities.invokeLater(new Runnable() {
//
//            @Override
//            public void run() {
//        treeTableView.setData(snapshot, idMap, aggregation, sampled);
//        tableView.setData(flatData, idMap, sampled);
//            }
//        });
        
        forwardCallsView.setData(snapshot, idMap, aggregation, selectedThreads, mergedThreads, sampled);
        hotSpotsView.setData(flatData, idMap, sampled);
        reverseCallsView.setData(snapshot, idMap, aggregation, selectedThreads, mergedThreads, sampled);
    }
    
    protected final void setSnapshot(CPUResultsSnapshot snapshot, boolean sampled) {
        this.snapshot = snapshot;
        this.sampled = sampled;
        
        setAggregation(aggregation);
    }
    
}
