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

package org.netbeans.modules.debugger.ui.views;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.SessionProvider;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter;
import org.netbeans.spi.viewmodel.CheckNodeModel;
import org.netbeans.spi.viewmodel.CheckNodeModelFilter;
import org.netbeans.spi.viewmodel.Model;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.DnDNodeModel;
import org.netbeans.spi.viewmodel.DnDNodeModelFilter;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModelFilter;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.NodeModelFilter;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.Models.CompoundModel;
import org.netbeans.spi.viewmodel.ReorderableTreeModel;
import org.netbeans.spi.viewmodel.ReorderableTreeModelFilter;
import org.netbeans.spi.viewmodel.TableRendererModel;
import org.netbeans.spi.viewmodel.TableRendererModelFilter;
import org.netbeans.spi.viewmodel.TreeExpansionModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;


/**
 * This delegating CompoundModelImpl loads all models from DebuggerManager.
 * getDefault ().getCurrentEngine ().lookup (viewType, ..) lookup.
 *
 * <p>
 * This class is identical to org.netbeans.modules.debugger.jpda.ui.views.ViewModelListener.
 *
 * @author   Jan Jancura
 */
public class ViewModelListener extends DebuggerManagerAdapter {

    private static final Class[] TREE_MODELS = { TreeModel.class, ReorderableTreeModel.class };
    private static final Class[] TREE_MODEL_FILTERS = { TreeModelFilter.class, ReorderableTreeModelFilter.class };

    private static final Class[] NODE_MODELS = { NodeModel.class, CheckNodeModel.class, DnDNodeModel.class, ExtendedNodeModel.class };
    private static final Class[] NODE_MODEL_FILTERS = { NodeModelFilter.class, CheckNodeModelFilter.class, DnDNodeModelFilter.class, ExtendedNodeModelFilter.class };

    private String          viewType;
    private JComponent      view;
    private JComponent      buttonsPane;
    private List models = new ArrayList(11);
    private List hyperModels;

    private List<? extends SessionProvider> sessionProviders;
    private Session currentSession;
    private List[] treeModels = new List[TREE_MODELS.length];
    private List[] treeModelFilters = new List[TREE_MODEL_FILTERS.length];
    private List treeExpansionModels;
    private List treeExpansionModelFilters;
    private List[] nodeModels = new List[NODE_MODELS.length];
    private List[] nodeModelFilters = new List[NODE_MODEL_FILTERS.length];
    private List tableModels;
    private List tableModelFilters;
    private List nodeActionsProviders;
    private List nodeActionsProviderFilters;
    private List columnModels;
    private List mm;
    private List asynchModelFilters;
    private List tableRenderers;
    private List tableRendererFilters;
    //private RequestProcessor rp;

    private List<AbstractButton> buttons;
    private javax.swing.JTabbedPane tabbedPane;
    private Image viewIcon;
    private SessionProvider providerToDisplay;
    private List<ViewModelListener> subListeners = new ArrayList<ViewModelListener>();
    private final Object destroyLock = new Object();

    private boolean isUp;

    private Preferences preferences = NbPreferences.forModule(ContextProvider.class).node(VariablesViewButtons.PREFERENCES_NAME);
    private ViewPreferenceChangeListener prefListener = new ViewPreferenceChangeListener();

    private static final RequestProcessor RP = new RequestProcessor(ViewModelListener.class.getName(), 1);
    
    // <RAVE>
    // Store the propertiesHelpID to pass to the Model object that is
    // used in generating the nodes for the view
    private String propertiesHelpID = null;
    
    ViewModelListener(
        String viewType,
        JComponent view,
        JComponent buttonsPane,
        String propertiesHelpID,
        Image viewIcon
    ) {
        this.viewType = viewType;
        this.view = view;
        this.buttonsPane = buttonsPane;
        buttonsPane.setLayout(new GridBagLayout());
        this.propertiesHelpID = propertiesHelpID;
        this.viewIcon = viewIcon;
        setUp();
    }
    // </RAVE>
    
    void setUp() {
        if (SwingUtilities.isEventDispatchThread()) {
            RP.post(new Runnable() {
                public void run() {
                    setUp();
                }
            });
            return ;
        }
        DebuggerManager.getDebuggerManager ().addDebuggerListener (
            DebuggerManager.PROP_CURRENT_ENGINE,
            this
        );
        preferences.addPreferenceChangeListener(prefListener);
        synchronized (this) {
            isUp = true;
        }
        updateModelLazily ();
    }

    void destroy () {
        if (SwingUtilities.isEventDispatchThread()) {
            RP.post(new Runnable() {
                public void run() {
                    destroy();
                }
            });
            return ;
        }
        synchronized(this) {
            DebuggerManager.getDebuggerManager ().removeDebuggerListener (
                DebuggerManager.PROP_CURRENT_ENGINE,
                this
            );
            preferences.removePreferenceChangeListener(prefListener);
            boolean haveTreeModels = false;
            for (List tms : treeModels) {
                if (tms != null && tms.size() > 0) {
                    haveTreeModels = true;
                    break;
                }
            }
            boolean haveNodeModels = false;
            for (List nms : nodeModels) {
                if (nms != null && nms.size() > 0) {
                    haveNodeModels = true;
                    break;
                }
            }
            final boolean haveModels = haveTreeModels || haveNodeModels || tableModels != null && tableModels.size() > 0;
            if (haveModels && view.getComponentCount() > 0) {
                JComponent tree = (JComponent) view.getComponent(0);
                if (!(tree instanceof javax.swing.JTabbedPane)) {
                    Models.setModelsToView(tree, null);
                }
            }
            synchronized (destroyLock) {
                models.clear();
                treeModels = new List[TREE_MODELS.length];
                treeModelFilters = new List[TREE_MODEL_FILTERS.length];
                treeExpansionModels = null;
                treeExpansionModelFilters = null;
                nodeModels = new List[NODE_MODELS.length];
                nodeModelFilters = new List[NODE_MODEL_FILTERS.length];
                tableModels = null;
                tableModelFilters = null;
                nodeActionsProviders = null;
                nodeActionsProviderFilters = null;
                columnModels = null;
                mm = null;
                asynchModelFilters = null;
                //rp = null;
                sessionProviders = null;
                currentSession = null;
                providerToDisplay = null;
                buttonsPane.removeAll();
                buttons = null;
                view.removeAll();
                for (ViewModelListener l : subListeners) {
                    l.destroy();
                }
                subListeners.clear();
                isUp = false;
            }
        }
    }

    @Override
    public void propertyChange (PropertyChangeEvent e) {
        if (e.getNewValue() != null) {
            synchronized (this) {
                // Reset the provider to display the current one.
                providerToDisplay = null;
            }
        }
        updateModel ();
    }

    private static void getMultiModels(ContextProvider cp, String viewPath,
                                       List[] models, Class[] classTypes) {
        for (int i = 0; i < classTypes.length; i++) {
            models[i] = cp.lookup (viewPath, classTypes[i]);
        }
        //System.err.println("\ngetMultiModels("+viewPath+") = "+Arrays.asList(models)+"\n");
    }

    private synchronized void updateModel() {
        isUp = true;
        RP.post(new Runnable() {
            public void run() {
                updateModelLazily();
            }
        });
    }

    private synchronized void updateModelLazily() {
        if (!isUp) return ;    // Destroyed in between
        DebuggerManager dm = DebuggerManager.getDebuggerManager ();
        DebuggerEngine e = dm.getCurrentEngine ();
        if (e == null) {
            sessionProviders = dm.lookup (viewType, SessionProvider.class);
        } else {
            sessionProviders = DebuggerManager.join(e, dm).lookup (viewType, SessionProvider.class);
        }
        if (!sessionProviders.contains(providerToDisplay)) {
            providerToDisplay = null;
        }
        if (e == null && providerToDisplay == null && sessionProviders.size() > 0) {
            providerToDisplay = sessionProviders.get(0);
        }
        ContextProvider cp;
        String viewPath;
        if (providerToDisplay != null) {
            e = null;
            cp = dm;
            viewPath = viewType + "/" + providerToDisplay.getTypeID();
        } else {
            cp = e != null ? DebuggerManager.join(e, dm) : dm;
            viewPath = viewType;
        }

        currentSession =        dm.getCurrentSession();

        getMultiModels(cp, viewPath, treeModels, TREE_MODELS);
        getMultiModels(cp, viewPath, treeModelFilters, TREE_MODEL_FILTERS);
        treeExpansionModels =   cp.lookup (viewPath, TreeExpansionModel.class);
        treeExpansionModelFilters = cp.lookup (viewType, TreeExpansionModelFilter.class);
        getMultiModels(cp, viewPath, nodeModels, NODE_MODELS);
        getMultiModels(cp, viewPath, nodeModelFilters, NODE_MODEL_FILTERS);
        tableModels =           cp.lookup (viewPath, TableModel.class);
        tableModelFilters =     cp.lookup (viewPath, TableModelFilter.class);
        nodeActionsProviders =  cp.lookup (viewPath, NodeActionsProvider.class);
        nodeActionsProviderFilters = cp.lookup (viewPath, NodeActionsProviderFilter.class);
        columnModels =          cp.lookup (viewPath, ColumnModel.class);
        mm =                    cp.lookup (viewPath, Model.class);
        asynchModelFilters =    cp.lookup (viewPath, AsynchronousModelFilter.class);
        tableRenderers =        cp.lookup (viewPath, TableRendererModel.class);
        tableRendererFilters =  cp.lookup (viewPath, TableRendererModelFilter.class);
        String searchPath = viewPath; // Try to find the AsynchronousModelFilter in upper folders...
        while (asynchModelFilters.isEmpty() && searchPath != null) {
            int i = searchPath.lastIndexOf('/');
            if (i > 0) {
                searchPath = searchPath.substring(0, i);
            } else {
                searchPath = null;
            }
            asynchModelFilters = cp.lookup (searchPath, AsynchronousModelFilter.class);
        }
        //rp = (e != null) ? e.lookupFirst(null, RequestProcessor.class) : null;

        if (View.LOCALS_VIEW_NAME.equals(viewType) && (VariablesViewButtons.isResultsViewNested() ||
                VariablesViewButtons.isWatchesViewNested())) {

            hyperModels = new ArrayList();
            if (VariablesViewButtons.isResultsViewNested()) {
                hyperModels.add(createCompound(View.RESULTS_VIEW_NAME));
            }
            if (VariablesViewButtons.isWatchesViewNested()) {
                hyperModels.add(createCompound(View.WATCHES_VIEW_NAME));
            }
            Models.CompoundModel main;
            hyperModels.add(main = createCompound(View.LOCALS_VIEW_NAME));
            hyperModels.add(main);
            hyperModels.add(new TreeModelFilter() {

                public Object getRoot(TreeModel original) {
                    return original.getRoot();
                }

                public Object[] getChildren(TreeModel original, Object parent, int from, int to) throws UnknownTypeException {
                    Object[] ch = original.getChildren(parent, from, to);
                    if (ch != null) {
                        for (int x = 0; x < ch.length; x++) {
                            // exclude HistoryNode
                            if (ch[x] == null || "HistoryNode".equals(ch[x].getClass().getSimpleName())) { // NOI18N [TODO]
                                if (ch[x] == null) {
                                    Exceptions.printStackTrace(new NullPointerException("Null child at index "+x+", parent: "+parent+", model: "+original));
                                }
                                Object[] nch = new Object[ch.length - 1];
                                System.arraycopy(ch, 0, nch, 0, x);
                                if ((x+1) < ch.length) {
                                    System.arraycopy(ch, x+1, nch, x, ch.length - x - 1);
                                }
                                ch = nch;
                            }
                        }
                    }
                    return ch;
                }

                public int getChildrenCount(TreeModel original, Object node) throws UnknownTypeException {
                    return original.getChildrenCount(node);
                }

                public boolean isLeaf(TreeModel original, Object node) throws UnknownTypeException {
                    return false;
                }

                public void addModelListener(ModelListener l) {}

                public void removeModelListener(ModelListener l) {}
            });
        } else {
            hyperModels = null;
        }

        List<? extends AbstractButton> bList = cp.lookup(viewPath, AbstractButton.class);
        List<AbstractButton> theButtons = new ArrayList<AbstractButton>();
        List tempList = new ArrayList<AbstractButton>();
        for (AbstractButton b : bList) {
            if (b instanceof JToggleButton) { // [TODO]
                theButtons.add(b);
            } else {
                tempList.add(b);
            }
        }
        theButtons.addAll(tempList);
        buttons = theButtons;
        tabbedPane = cp.lookupFirst(viewPath, javax.swing.JTabbedPane.class);

        ModelsChangeRefresher mcr = new ModelsChangeRefresher();
        Customizer[] modelListCustomizers = new Customizer[] {
            //(Customizer) treeModels,
            //(Customizer) treeModelFilters,
            (Customizer) treeExpansionModels,
            (Customizer) treeExpansionModelFilters,
            //(Customizer) nodeModels,
            //(Customizer) nodeModelFilters,
            (Customizer) tableModels,
            (Customizer) tableModelFilters,
            (Customizer) nodeActionsProviders,
            (Customizer) nodeActionsProviderFilters,
            (Customizer) columnModels,
            (Customizer) mm,
            (Customizer) asynchModelFilters,
            (Customizer) tableRenderers,
            (Customizer) tableRendererFilters,
        };
        List<Customizer> modelListCustomizerLists = new ArrayList<Customizer>(50);
        modelListCustomizerLists.addAll(Arrays.asList(modelListCustomizers));
        addAsCustomizers(modelListCustomizerLists, treeModels);
        addAsCustomizers(modelListCustomizerLists, treeModelFilters);
        addAsCustomizers(modelListCustomizerLists, nodeModels);
        addAsCustomizers(modelListCustomizerLists, nodeModelFilters);
        for (int i = 0; i < modelListCustomizers.length; i++) {
            Customizer c = modelListCustomizers[i];
            if (c != null) { // Can be null when debugger is finishing
                c.addPropertyChangeListener(mcr);
                c.setObject("load first"); // NOI18N
                c.setObject("unload last"); // NOI18N
            }
        }

        refreshModel();
    }

    private static void addAsCustomizers(List<Customizer> modelListCustomizerLists, Object[] modelLists) {
        for (int i = 0; i < modelLists.length; i++) {
            modelListCustomizerLists.add((Customizer) modelLists[i]);
        }
    }

    private static List joinLists(List[] modelLists) {
        List models = new ArrayList();
        for (List l : modelLists) {
            synchronized (l) {
                for (Object o : l) {
                    if (!models.contains(o)) {
                        models.add(o);
                    }
                }
            }
        }
        return models;
    }

    private synchronized void refreshModel() {
        models.clear();
        if (mm == null) {
            // Destroyed
            return ;
        }
        synchronized (treeModels) {
            models.add(joinLists(treeModels));
        }
        synchronized (treeModelFilters) {
            models.add(joinLists(treeModelFilters));
        }
        synchronized (treeExpansionModels) {
            models.add(new ArrayList(treeExpansionModels));
        }
        synchronized (nodeModels) {
            models.add(joinLists(nodeModels));
        }
        synchronized (nodeModelFilters) {
            models.add(joinLists(nodeModelFilters));
        }
        synchronized (tableModels) {
            models.add(new ArrayList(tableModels));
        }
        synchronized (tableModelFilters) {
            models.add(new ArrayList(tableModelFilters));
        }
        synchronized (nodeActionsProviders) {
            models.add(new ArrayList(nodeActionsProviders));
        }
        synchronized (nodeActionsProviderFilters) {
            models.add(new ArrayList(nodeActionsProviderFilters));
        }
        synchronized (columnModels) {
            models.add(new ArrayList(columnModels));
        }
        synchronized (mm) {
            models.add(new ArrayList(mm));
        }
        synchronized (treeExpansionModelFilters) {
            models.add(new ArrayList(treeExpansionModelFilters));
        }
        synchronized (asynchModelFilters) {
            models.add(new ArrayList(asynchModelFilters));
        }
        synchronized (tableRenderers) {
            models.add(new ArrayList(tableRenderers));
        }
        synchronized (tableRendererFilters) {
            models.add(new ArrayList(tableRendererFilters));
        }
        /*if (rp != null) {
            models.add(rp);
        }*/

        // <RAVE>
        // Store the propertiesHelpID in the tree model to be retrieved later
        // by the TreeModelNode objects
        // Models.setModelsToView (
        //    view,
        //    Models.createCompoundModel (models)
        // );
        // ====

        boolean haveTreeModels = false;
        for (List tms : treeModels) {
            if (tms.size() > 0) {
                haveTreeModels = true;
                break;
            }
        }
        boolean haveNodeModels = false;
        for (List nms : nodeModels) {
            if (nms.size() > 0) {
                haveNodeModels = true;
                break;
            }
        }
        final boolean haveModels = haveTreeModels || haveNodeModels || tableModels.size() > 0 || hyperModels != null;
        final Models.CompoundModel newModel;
        if (hyperModels != null) {
            newModel = Models.createCompoundModel (hyperModels, propertiesHelpID);
        } else if (haveModels) {
            newModel = Models.createCompoundModel (models, propertiesHelpID);
        } else {
            newModel = null;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final JComponent buttonsSubPane;
                synchronized (destroyLock) {
                    List<AbstractButton> theButtons = buttons;
                    if (theButtons == null) return ; // Destroyed in between
                    buttonsPane.removeAll();
                    if (theButtons.size() == 0 && sessionProviders.size() == 0) {
                        buttonsPane.setVisible(false);
                        buttonsSubPane = null;
                    } else {
                        int i = 0;
                        if (sessionProviders.size() > 0) {
                            javax.swing.AbstractButton b = createSessionsSwitchButton();
                            GridBagConstraints c = new GridBagConstraints(0, i, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, 0, new Insets(3, 3, 0, 3), 0, 0);
                            buttonsPane.add(b, c);
                            i++;
                            javax.swing.JSeparator s = new javax.swing.JSeparator(SwingConstants.HORIZONTAL);
                            c = new GridBagConstraints(0, i, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, 0, new Insets(3, 0, 3, 0), 0, 0);
                            buttonsPane.add(s, c);
                            i++;
                        }
                        if (tabbedPane != null) {
                            buttonsSubPane = new javax.swing.JPanel();
                            buttonsSubPane.setLayout(new java.awt.GridBagLayout());
                            GridBagConstraints c = new GridBagConstraints(0, i, 1, 1, 0.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL, new Insets(3, 0, 3, 0), 0, 0);
                            buttonsPane.add(buttonsSubPane, c);
                            i++;
                        } else {
                            buttonsSubPane = null;
                            for (javax.swing.AbstractButton b : buttons) {
                                GridBagConstraints c = new GridBagConstraints(0, i, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, 0, new Insets(3, 3, 0, 3), 0, 0);
                                buttonsPane.add(b, c);
                                i++;
                            }
                        }
                        GridBagConstraints c = new GridBagConstraints(0, i, 1, 1, 0.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL, new Insets(3, 3, 3, 3), 0, 0);
                        javax.swing.JPanel pushUpPanel = new javax.swing.JPanel();
                        pushUpPanel.setBackground(buttonsPane.getBackground());
                        buttonsPane.add(pushUpPanel, c); // Push-panel
                        //Exceptions.printStackTrace(new IllegalArgumentException("L&F = '"+UIManager.getLookAndFeel().getID()+"'"));

                        // [TODO]
                        //GridBagConstraints c = new GridBagConstraints(1, 0, 1, i + 1, 0.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0);
                        //buttonsPane.add(new javax.swing.JSeparator(SwingConstants.VERTICAL), c); // Components separator, border-like
                        buttonsPane.revalidate();
                        buttonsPane.setVisible(true);
                        buttonsPane.repaint();
                        view.revalidate();
                    }
                }
                
                if (view.getComponentCount() > 0) {
                    if (tabbedPane == null && view.getComponent(0) instanceof javax.swing.JTabbedPane) {
                        view.removeAll();
                    } else if (tabbedPane != null) {
                        view.removeAll();
                    }
                }
                if (view.getComponentCount() == 0) {
                    if (haveModels) {
                        view.add(Models.createView(newModel));
                        view.revalidate();
                        view.repaint();
                    } else if (tabbedPane != null) {
                        int n = tabbedPane.getTabCount();
                        for (int i = 0; i < n; i++) {
                            java.awt.Component c = tabbedPane.getComponentAt(i);
                            if (c instanceof javax.swing.JPanel) {
                                c = (java.awt.Component) ((javax.swing.JPanel) c).getClientProperty(javax.swing.JLabel.class.getName());
                            }
                            if (c instanceof javax.swing.JLabel) {
                                String id = ((javax.swing.JLabel) c).getText();
                                if (providerToDisplay != null) {
                                    id = providerToDisplay.getTypeID() + "/" + id;
                                }
                                javax.swing.JPanel contentComponent = new javax.swing.JPanel(new java.awt.BorderLayout ());
                                subListeners.add(new ViewModelListener (
                                    viewType + "/" + id,
                                    contentComponent,
                                    buttonsSubPane,
                                    propertiesHelpID,
                                    viewIcon
                                ));
                                tabbedPane.setComponentAt(i, contentComponent);
                                contentComponent.putClientProperty(javax.swing.JLabel.class.getName(), c);
                            }
                        }
                        view.add(tabbedPane);
                        view.revalidate();
                        view.repaint();
                    }
                } else if (tabbedPane == null) {
                    if (!haveModels) {
                        view.removeAll();
                        view.revalidate();
                        view.repaint();
                    } else {
                        JComponent tree = (JComponent) view.getComponent(0);
                        Models.setModelsToView (
                            tree,
                            newModel
                        );
                    }
                }
            }
        });
        // </RAVE>
    }

    private javax.swing.JButton createSessionsSwitchButton() {
        final javax.swing.JButton b = VariablesViewButtons.createButton(
                new ImageIcon(viewIcon),
                NbBundle.getMessage(ViewModelListener.class, "Tooltip_SelectSrc"));
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == b) {
                    javax.swing.JPopupMenu m = new javax.swing.JPopupMenu();
                    if (currentSession != null) {
                        JMenuItem mi = new JMenuItem(currentSession.getName());
                        mi.putClientProperty("SESSION", currentSession);
                        mi.addActionListener(this);
                        m.add(mi);
                    }
                    for (SessionProvider sp : sessionProviders) {
                        JMenuItem mi = new JMenuItem(sp.getSessionName());
                        mi.putClientProperty("SESSION", sp);
                        mi.addActionListener(this);
                        m.add(mi);
                    }
                    java.awt.Point pos = b.getMousePosition();
                    if (pos == null) {
                        pos = new java.awt.Point(b.getWidth(), b.getHeight());
                    }
                    m.show(b, pos.x, pos.y);
                } else {
                    JMenuItem mi = (JMenuItem) e.getSource();
                    Object s = mi.getClientProperty("SESSION");
                    synchronized (ViewModelListener.this) {
                        if (s instanceof Session) {
                            providerToDisplay = null;
                        } else {
                            providerToDisplay = (SessionProvider) s;
                        }
                    }
                    updateModel();
                }
            }
        });
        return b;
    }
    
    private CompoundModel createCompound(String viewName) {
        DebuggerManager dm = DebuggerManager.getDebuggerManager ();
        DebuggerEngine e = dm.getCurrentEngine ();
        List<? extends SessionProvider> localSessionProviders;
        if (e == null) {
            localSessionProviders = dm.lookup (viewName, SessionProvider.class);
        } else {
            localSessionProviders = DebuggerManager.join(e, dm).lookup (viewName, SessionProvider.class);
        }
        if (!localSessionProviders.contains(providerToDisplay)) {
            providerToDisplay = null;
        }
        if (e == null && providerToDisplay == null && localSessionProviders.size() > 0) {
            providerToDisplay = localSessionProviders.get(0);
        }
        ContextProvider cp;
        String viewPath;
        if (providerToDisplay != null) {
            e = null;
            cp = dm;
            viewPath = viewName + "/" + providerToDisplay.getTypeID();
        } else {
            cp = e != null ? DebuggerManager.join(e, dm) : dm;
            viewPath = viewName;
        }

        List[] treeModels = new List[TREE_MODELS.length];
        List[] treeModelFilters = new List[TREE_MODEL_FILTERS.length];
        List treeExpansionModels;
        List treeExpansionModelFilters;
        List[] nodeModels = new List[NODE_MODELS.length];
        List[] nodeModelFilters = new List[NODE_MODEL_FILTERS.length];
        List tableModels;
        List tableModelFilters;
        List nodeActionsProviders;
        List nodeActionsProviderFilters;
        List columnModels;
        List mm;
        List asynchModelFilters;
        List tableRenderers;
        List tableRendererFilters;

        getMultiModels(cp, viewPath, treeModels, TREE_MODELS);
        getMultiModels(cp, viewPath, treeModelFilters, TREE_MODEL_FILTERS);
        treeExpansionModels =   cp.lookup (viewPath, TreeExpansionModel.class);
        treeExpansionModelFilters = cp.lookup (viewPath, TreeExpansionModelFilter.class);
        getMultiModels(cp, viewPath, nodeModels, NODE_MODELS);
        getMultiModels(cp, viewPath, nodeModelFilters, NODE_MODEL_FILTERS);
        tableModels =           cp.lookup (viewPath, TableModel.class);
        tableModelFilters =     cp.lookup (viewPath, TableModelFilter.class);
        nodeActionsProviders =  cp.lookup (viewPath, NodeActionsProvider.class);
        nodeActionsProviderFilters = cp.lookup (viewPath, NodeActionsProviderFilter.class);
        columnModels =          cp.lookup (viewPath, ColumnModel.class);
        mm =                    cp.lookup (viewPath, Model.class);
        asynchModelFilters =    cp.lookup (viewPath, AsynchronousModelFilter.class);
        tableRenderers =        cp.lookup (viewPath, TableRendererModel.class);
        tableRendererFilters =  cp.lookup (viewPath, TableRendererModelFilter.class);
        String searchPath = viewPath; // Try to find the AsynchronousModelFilter in upper folders...
        while (asynchModelFilters.isEmpty() && searchPath != null) {
            int i = searchPath.lastIndexOf('/');
            if (i > 0) {
                searchPath = searchPath.substring(0, i);
            } else {
                searchPath = null;
            }
            asynchModelFilters = cp.lookup (searchPath, AsynchronousModelFilter.class);
        }

        List treeNodeModelsCompound = new ArrayList(13);
        treeNodeModelsCompound.add(joinLists(treeModels));
        treeNodeModelsCompound.add(joinLists(treeModelFilters));
        treeNodeModelsCompound.add(treeExpansionModels); // TreeExpansionModel
        treeNodeModelsCompound.add(joinLists(nodeModels));
        treeNodeModelsCompound.add(joinLists(nodeModelFilters));
        treeNodeModelsCompound.add(tableModels); // TableModel
        treeNodeModelsCompound.add(tableModelFilters); // TableModelFilter
        treeNodeModelsCompound.add(nodeActionsProviders);
        treeNodeModelsCompound.add(nodeActionsProviderFilters);
        treeNodeModelsCompound.add(columnModels); // ColumnModel
        treeNodeModelsCompound.add(mm); // Model
        treeNodeModelsCompound.add(treeExpansionModelFilters); // TreeExpansionModelFilter
        treeNodeModelsCompound.add(asynchModelFilters); // AsynchronousModelFilter
        treeNodeModelsCompound.add(tableRenderers);
        treeNodeModelsCompound.add(tableRendererFilters);
        /*if (rp != null) {
            treeNodeModelsCompound.add(rp);
        }*/

        CompoundModel treeNodeModel = Models.createCompoundModel(treeNodeModelsCompound);

        return treeNodeModel;
    }

    // innerclasses .............................................................

    private class ModelsChangeRefresher implements PropertyChangeListener, Runnable {
        
        private RequestProcessor.Task task;

        public synchronized void propertyChange(PropertyChangeEvent evt) {
            if (task == null) {
                task = new RequestProcessor(ModelsChangeRefresher.class.getName(), 1).create(this);
            }
            task.schedule(1);
        }

        public void run() {
            refreshModel();
        }
        
    }

    private class ViewPreferenceChangeListener implements PreferenceChangeListener {

        public void preferenceChange(PreferenceChangeEvent evt) {
            String key = evt.getKey();
            if (VariablesViewButtons.SHOW_WATCHES.equals(key) ||
                    VariablesViewButtons.SHOW_EVALUTOR_RESULT.equals(key)) {
                updateModel();
            }
        }

    }

}
