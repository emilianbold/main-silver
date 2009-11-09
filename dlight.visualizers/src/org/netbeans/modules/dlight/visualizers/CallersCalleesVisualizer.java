/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.visualizers;

import org.netbeans.modules.dlight.spi.SourceSupportProvider;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.dataprovider.FunctionCallTreeTableNode;
import org.netbeans.modules.dlight.core.stack.dataprovider.StackDataProvider;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.spi.AnnotatedSourceSupport;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;
import org.netbeans.modules.dlight.spi.impl.TreeTableDataProvider;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.visualizers.api.CallersCalleesVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.TreeTableVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.impl.TreeTableVisualizerConfigurationAccessor;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

final class CallersCalleesVisualizer extends TreeTableVisualizer<FunctionCallTreeTableNode> {

    public static final String IS_CALLS = "TopTenFunctionsIsCalls"; // NOI18N
    private static final int TOP_FUNCTIONS_COUNT = 10;
    private final CallersCalleesVisualizerConfiguration configuration;
    private final StackDataProvider dataProvider;
    private final Object syncFillInLock = new Object();
    private JToggleButton callers;
    private JToggleButton calls;
    private boolean isCalls = true;
    private Future<List<FunctionCallWithMetric>> syncFillDataTask;
    private DefaultMutableTreeNode focusedTreeNode = null;
    private final List<Column> metricsList;

    CallersCalleesVisualizer(StackDataProvider dataProvider, TreeTableVisualizerConfiguration configuration) {
        super(configuration, (TreeTableDataProvider<FunctionCallTreeTableNode>) dataProvider);
        this.configuration = (CallersCalleesVisualizerConfiguration) configuration;
        this.configuration.setNodeActionProvider(new NodeActionsProviderImpl());
        this.dataProvider = dataProvider;
        isCalls = NbPreferences.forModule(CallersCalleesVisualizer.class).getBoolean(IS_CALLS, true);
        metricsList = Arrays.asList(TreeTableVisualizerConfigurationAccessor.getDefault().getTableColumns(configuration));

    }

    public TreeTableVisualizerConfiguration getConfiguration() {
        return super.getVisualizerConfiguration();
    }

    @Override
    protected void initComponents() {
        super.initComponents();

        if (TreeTableVisualizerConfigurationAccessor.getDefault().isTableView(getConfiguration())) {//we do not need focus on and other buttons here
            return;
        }

        JButton focusOn = new JButton();
        calls = new JToggleButton();
        callers = new JToggleButton();
        JToolBar buttonsToolbar = getButtonsTolbar();
        buttonsToolbar.setFloatable(false);
        buttonsToolbar.setOrientation(1);
        buttonsToolbar.setRollover(true);


        // focusOn button...
        focusOn.setIcon(ImageLoader.loadIcon("focus.png")); // NOI18N
//    focusOn.setToolTipText(org.openide.util.NbBundle.getMessage(PerformanceMonitorViewTopComponent.class, "FocusOnActionTooltip")); // NOI18N
        focusOn.setFocusable(false);
        focusOn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        focusOn.setMaximumSize(new java.awt.Dimension(28, 28));
        focusOn.setMinimumSize(new java.awt.Dimension(28, 28));
        focusOn.setPreferredSize(new java.awt.Dimension(28, 28));
        focusOn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        focusOn.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                focusOnActionPerformed(evt);
            }
        });

        buttonsToolbar.add(focusOn);

        buttonsToolbar.add(new JToolBar.Separator());

        calls.setIcon(ImageLoader.loadIcon("who_is_called.png")); // NOI18N
//    calls.setToolTipText(org.openide.util.NbBundle.getMessage(PerformanceMonitorViewTopComponent.class, "CallsActionTooltip")); // NOI18N
        calls.setFocusable(false);
        calls.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        calls.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        calls.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                callsActionPerformed(evt);
            }
        });

        buttonsToolbar.add(calls);

        callers.setIcon(ImageLoader.loadIcon("who_calls.png")); // NOI18N
//    callers.setToolTipText(org.openide.util.NbBundle.getMessage(PerformanceMonitorViewTopComponent.class, "CallersActionTooltip")); // NOI18N
        callers.setFocusable(false);
        callers.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        callers.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        callers.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                callersActionPerformed(evt);
            }
        });

        buttonsToolbar.add(callers);
        repaint();
        revalidate();
    }

    private void callsActionPerformed(ActionEvent evt) {
        if (isCalls == calls.isSelected()) {
            return;
        }
        setDirection(true);
    }

    private void callersActionPerformed(ActionEvent evt) {
        if (isCalls != callers.isSelected()) {
            return;
        }
        setDirection(false);
    }

    private void focusOnActionPerformed(ActionEvent evt) {
        //find selected
        //functionsCallTreeModel.
        //throw new UnsupportedOperationException("Not yet implemented");
        ExplorerManager manager = getExplorerManager();
        if (manager == null) {
            return;
        }
        //get selected
        Node[] selectedNodes = manager.getSelectedNodes();
        if (selectedNodes == null || selectedNodes.length == 0) {
            return;
        }
        Node selectedNode = selectedNodes[0];
        focusedTreeNode = selectedNode.getLookup().lookup(DefaultMutableTreeNode.class);
        FunctionCallWithMetric focusedFunction = focusedTreeNode == null ? null : ((FunctionCallTreeTableNode) focusedTreeNode.getUserObject()).getDeligator();
        setNodes(Arrays.asList(focusedTreeNode));

        loadTree(focusedTreeNode, Arrays.asList(new FunctionCallTreeTableNode(focusedFunction)));

        //
        //and now chage tree and invoke fireTreeModelChanged()

    }

    private ExplorerManager getExplorerManager() {
        if (treeTableView != null && treeTableView instanceof ExplorerManager.Provider) {
            return ((ExplorerManager.Provider) treeTableView).getExplorerManager();
        }
        return null;
    }

    private void setDirection(boolean direction) {
        isCalls = direction;
        NbPreferences.forModule(CallersCalleesVisualizer.class).putBoolean(IS_CALLS, isCalls);
        updateButtons();
        update();
    }

    private synchronized void update() {
        if (focusedTreeNode == null) {
            //just update tree
            asyncFillModel(getConfiguration().getMetadata().getColumns(), true);
            return;
        }
        //otherwise we should update
        loadTree(focusedTreeNode, Arrays.asList((FunctionCallTreeTableNode) focusedTreeNode.getUserObject()));
    }

    /**
     * This method will be invoked when
     */
    @Override
    protected void loadTree(final DefaultMutableTreeNode rootNode, final List<FunctionCallTreeTableNode> ppath) {
        //we should show Loading Node
        //this.functionsCallTreeModel.get
        Runnable r = new Runnable() {

            public void run() {
                final List<FunctionCallWithMetric> path = FunctionCallTreeTableNode.getFunctionCalls(ppath);
                //FunctionCall[] path = ppath.toArray(new FunctionCallTreeTableNode[0]);
                final List<FunctionCallWithMetric> result = dataProvider.getCallees(path, configuration.getMetadata().getColumns(), null, isCalls);

                UIThread.invoke(new Runnable() {

                    public void run() {
                        update(rootNode, result);
                    }
                });

            }
        };

        //go away from AWT Thread
        if (SwingUtilities.isEventDispatchThread()) {
            DLightExecutorService.submit(r, "Get callers/callees"); // NOI18N
        } else {
            r.run();
        }
    }

    @Override
    protected void updateTree(final DefaultMutableTreeNode rootNode, List<FunctionCallTreeTableNode> result) {
        rootNode.removeAllChildren();
        if (result != null) {
            for (FunctionCallTreeTableNode call : result) {
                rootNode.add(new DefaultMutableTreeNode(call));
            }
        }

        fireTreeModelChanged(rootNode);
    }

    private void update(final DefaultMutableTreeNode rootNode, List<FunctionCallWithMetric> result) {
        //add them all as a children to rootNode
        rootNode.removeAllChildren();
        if (result != null) {
            for (FunctionCallWithMetric call : result) {
                rootNode.add(new DefaultMutableTreeNode(new FunctionCallTreeTableNode(call)));
            }
        }        
        fireTreeModelChanged(rootNode);
        notifyAnnotedSourceProviders();

    }

    @Override
    protected void syncFillModel(final List<Column> columns) {
        List<FunctionCallWithMetric> flist =
                dataProvider.getHotSpotFunctions(columns, null, TOP_FUNCTIONS_COUNT);

        update(flist);
    }

    private final List<FunctionCallWithMetric> getAllChildren(TreeNode n){
        if (!(n instanceof DefaultMutableTreeNode)){
            return Collections.emptyList();
        }
        DefaultMutableTreeNode node =(DefaultMutableTreeNode)n;
        if (!(node.getUserObject() instanceof FunctionCallTreeTableNode)){
            return Collections.emptyList();
        }
        List<FunctionCallWithMetric> result = new ArrayList<FunctionCallWithMetric>();
        result.add(((FunctionCallTreeTableNode)node.getUserObject()).getDeligator());
        for (int i = 0, count = node.getChildCount(); i < count; i++){
            result.addAll(getAllChildren(node.getChildAt(i)));
        }
        return result;
        
    }

    private final List<FunctionCallWithMetric> getAllFunctions(){
        List<FunctionCallWithMetric> result = new ArrayList<FunctionCallWithMetric>();
        for (int i = 0, count = TREE_ROOT.getChildCount(); i < count; i++){
            result.addAll(getAllChildren(TREE_ROOT.getChildAt(i)));
        }
        return result;
    }

    private void notifyAnnotedSourceProviders() {
        
        final List<FunctionCallWithMetric> list =  getAllFunctions();
        Collection<? extends AnnotatedSourceSupport> supports = Lookup.getDefault().lookupAll(AnnotatedSourceSupport.class);
        for (final AnnotatedSourceSupport sourceSupport : supports) {
            DLightExecutorService.submit(new Runnable() {

                public void run() {
                    sourceSupport.updateSource(dataProvider, metricsList, list, null);
                }
            }, "Annoted Source from FunctionsListView Visualizer");//NOI18N
        }
    }

    private void notifyAnnotedSourceProviders(final List<FunctionCallWithMetric> list) {

        Collection<? extends AnnotatedSourceSupport> supports = Lookup.getDefault().lookupAll(AnnotatedSourceSupport.class);
        for (final AnnotatedSourceSupport sourceSupport : supports) {
            DLightExecutorService.submit(new Runnable() {

                public void run() {
                    sourceSupport.updateSource(dataProvider, metricsList, list, null);
                }
            }, "Annoted Source from FunctionsListView Visualizer");//NOI18N
        }
    }

    private void update(List<FunctionCallWithMetric> list) {
        final boolean isEmptyConent = list == null || list.isEmpty();
        setContent(isEmptyConent);

        if (!isEmptyConent) {
            List<FunctionCallTreeTableNode> res = new ArrayList<FunctionCallTreeTableNode>();

            for (FunctionCallWithMetric c : list) {
                res.add(new FunctionCallTreeTableNode(c));
            }

            updateList(res);
            notifyAnnotedSourceProviders(list);
        }
    }

    @Override
    protected String getIcon(FunctionCallTreeTableNode node) {
        return super.getIcon(node);
    }

    @Override
    protected void updateButtons() {
        if (TreeTableVisualizerConfigurationAccessor.getDefault().isTableView(getConfiguration())) {
            return;
        }
        if (calls != null) {
            calls.setSelected(isCalls);
            callers.setSelected(!isCalls);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
    }

    @Override
    public void refresh() {
        super.refresh();
        updateButtons();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        synchronized (syncFillInLock) {
            if (syncFillDataTask != null) {
                syncFillDataTask.cancel(true);
                syncFillDataTask = null;
            }
        }
    }

    @Override
    public int onTimer() {
        if (!isShown() || !isShowing()) {
            return 0;
        }
        syncFillModel(this.configuration.getMetadata().getColumns());
//    update(dataProvider.getHotSpotFunctions(, null, TOP_FUNCTIONS_COUNT));
        return 0;
    }

    class NodeActionsProviderImpl implements NodeActionsProvider {

        public void performDefaultAction(Object node) throws UnknownTypeException {
            if (!(node instanceof DefaultMutableTreeNode)) {
                throw new UnknownTypeException(node);
            }

            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
            Object nodeObject = treeNode.getUserObject();

            if (!(nodeObject instanceof FunctionCallTreeTableNode)) {
                return;
            }

            FunctionCallWithMetric functionCall = ((FunctionCallTreeTableNode) nodeObject).getDeligator();
            GoToSourceAction action = new GoToSourceAction(functionCall);
            action.actionPerformed(null);
        }

        public Action[] getActions(Object node) throws UnknownTypeException {
            if (!(node instanceof DefaultMutableTreeNode)) {
                throw new UnknownTypeException(node);
            }
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
            Object nodeObject = treeNode.getUserObject();
            if (!(nodeObject instanceof FunctionCallTreeTableNode)) {
                return null;
            }
            return new Action[]{new GoToSourceAction(((FunctionCallTreeTableNode) nodeObject).getDeligator())};
        }
    }

    private class GoToSourceAction extends AbstractAction {

        private final FunctionCallWithMetric functionCall;
        private final Future<SourceFileInfo> sourceFileInfoTask;
        private boolean isEnabled = true;
        private boolean gotTheInfo = false;

        public GoToSourceAction(FunctionCallWithMetric funcCall) {
            super(NbBundle.getMessage(CallersCalleesVisualizer.class, "GoToSourceActionName"));//NOI18N
            this.functionCall = funcCall;
            sourceFileInfoTask = DLightExecutorService.submit(new Callable<SourceFileInfo>() {

                public SourceFileInfo call() {
                    return dataProvider.getSourceFileInfo(functionCall);
                }
            }, "SourceFileInfo getting info from CallersCalees Visualizer"); // NOI18N
            waitForSourceFileInfo();
        }

        private void waitForSourceFileInfo() {
            DLightExecutorService.submit(new Runnable() {

                public void run() {
                    try {
                        SourceFileInfo sourceFileInfo = sourceFileInfoTask.get();
                        isEnabled = sourceFileInfo != null && sourceFileInfo.isSourceKnown();
                    } catch (InterruptedException ex) {
                        isEnabled = false;
                    } catch (ExecutionException ex) {
                        isEnabled = false;
                    } finally {
                        synchronized (GoToSourceAction.this) {
                            gotTheInfo = true;
                        }
                        setEnabled(isEnabled);

                    }

                }
            }, "Wait For the SourceFileInfo");//NOI18N
        }

        @Override
        public boolean isEnabled() {
            return isEnabled;
        }

        public void actionPerformed(ActionEvent e) {
            DLightExecutorService.submit(new Runnable() {

                public void run() {
                    SourceFileInfo sourceFileInfo = null;
                    try {
                        sourceFileInfo = sourceFileInfoTask.get();                        
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (ExecutionException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    if (sourceFileInfo == null) {// TODO: what should I do here if there is no source file info
                        return;
                    }

                    SourceSupportProvider sourceSupportProvider = Lookup.getDefault().lookup(SourceSupportProvider.class);
                    sourceSupportProvider.showSource(sourceFileInfo);
                    notifyAnnotedSourceProviders();
                }
            }, "GoToSource from Callers Calees Visualizer"); // NOI18N
        }
    }
}
