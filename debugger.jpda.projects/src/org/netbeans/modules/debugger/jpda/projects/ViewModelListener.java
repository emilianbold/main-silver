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

package org.netbeans.modules.debugger.jpda.projects;

import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JComponent;

import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.editor.ext.ToolTipSupport;
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
import org.netbeans.spi.viewmodel.ReorderableTreeModel;
import org.netbeans.spi.viewmodel.ReorderableTreeModelFilter;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.TreeExpansionModelFilter;


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
    //private RequestProcessor rp;

    private List<AbstractButton> buttons;
    private javax.swing.JTabbedPane tabbedPane;
    private Image viewIcon;
    private ObjectVariable variable;
    private SessionProvider providerToDisplay;
    private List<ViewModelListener> subListeners = new ArrayList<ViewModelListener>();
    private ToolTipSupport toolTipSupport;

    // <RAVE>
    // Store the propertiesHelpID to pass to the Model object that is
    // used in generating the nodes for the view
    private String propertiesHelpID = null;
    
    ViewModelListener(
        String viewType,
        JComponent view,
        JComponent buttonsPane,
        String propertiesHelpID,
        Image viewIcon,
        ObjectVariable variable
    ) {
        this.viewType = viewType;
        this.view = view;
        this.buttonsPane = buttonsPane;
        this.propertiesHelpID = propertiesHelpID;
        this.viewIcon = viewIcon;
        this.variable = variable;
        setUp();
    }
    // </RAVE>
    
    void setUp() {
        DebuggerManager.getDebuggerManager ().addDebuggerListener (
            DebuggerManager.PROP_CURRENT_ENGINE,
            this
        );
        updateModel ();
    }
    
    void setToolTipSupport(ToolTipSupport toolTipSupport) {
        this.toolTipSupport = toolTipSupport;
    }

    synchronized void destroy () {
        DebuggerManager.getDebuggerManager ().removeDebuggerListener (
            DebuggerManager.PROP_CURRENT_ENGINE,
            this
        );
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
        final boolean haveModels = haveTreeModels || haveNodeModels ||
                                   (tableModels != null && tableModels.size() > 0);
        if (haveModels && view.getComponentCount() > 0) {
            JComponent tree = (JComponent) view.getComponent(0);
            if (!(tree instanceof javax.swing.JTabbedPane)) {
                Models.setModelsToView(tree, null);
            }
        }
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

        hyperModels = null;

        List<? extends AbstractButton> bList = cp.lookup(viewPath, AbstractButton.class);
        buttons = new ArrayList<AbstractButton>();
        List tempList = new ArrayList<AbstractButton>();
        for (AbstractButton b : bList) {
            if (b instanceof JToggleButton) { // [TODO]
                buttons.add(b);
            } else {
                tempList.add(b);
            }
        }
        buttons.addAll(tempList);
        tabbedPane = cp.lookupFirst(viewPath, javax.swing.JTabbedPane.class);

        refreshModel();
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
        if (treeModels == null) {
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
        /*if (rp != null) {
            models.add(rp);
        }*/

        final JComponent buttonsSubPane;
        synchronized (buttons) {
            buttonsPane.removeAll();
            if (buttons.size() == 0) {
                buttonsPane.setVisible(false);
                buttonsSubPane = null;
            } else {
                buttonsPane.setVisible(true);
                int i = 0;
                if (tabbedPane != null) {
                    buttonsSubPane = new javax.swing.JPanel();
                    buttonsSubPane.setLayout(new java.awt.GridBagLayout());
                    GridBagConstraints c = new GridBagConstraints(0, i, 1, 1, 0.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL, new Insets(5, 0, 5, 0), 0, 0);
                    buttonsPane.add(buttonsSubPane, c);
                    i++;
                } else {
                    buttonsSubPane = null;
                    for (javax.swing.AbstractButton b : buttons) {
                        GridBagConstraints c = new GridBagConstraints(0, i, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, 0, new Insets(5, 5, 5, 5), 0, 0);
                        buttonsPane.add(b, c);
                        i++;
                    }
                }
                //GridBagConstraints c = new GridBagConstraints(0, i, 1, 1, 0.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL, new Insets(5, 5, 5, 5), 0, 0);
                //buttonsPane.add(new javax.swing.JPanel(), c); // Push-panel

                // [TODO]
                //GridBagConstraints c = new GridBagConstraints(1, 0, 1, i + 1, 0.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0);
                //buttonsPane.add(new javax.swing.JSeparator(SwingConstants.VERTICAL), c); // Components separator, border-like
            }
        }
        
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
        if (newModel != null) {
            newModel.nodeExpanded(variable);
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
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
                                    viewIcon,
                                    variable
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
                        if (toolTipSupport != null) {
                            toolTipSupport.setToolTipVisible(false);
                        }
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

}
