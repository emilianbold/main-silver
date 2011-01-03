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

package org.netbeans.modules.debugger.jpda.ui.views;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComponent;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.modules.debugger.jpda.ui.debugging.DebuggingView;
import org.netbeans.spi.debugger.ContextProvider;
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
import org.netbeans.spi.viewmodel.ReorderableTreeModel;
import org.netbeans.spi.viewmodel.ReorderableTreeModelFilter;
import org.netbeans.spi.viewmodel.TreeExpansionModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.RequestProcessor;


/**
 * This delegating CompoundModelImpl loads all models from DebuggerManager.
 * getDefault ().getCurrentEngine ().lookup (viewType, ..) lookup.
 *
 * <p>
 * This class is identical to org.netbeans.modules.debugger.ui.views.ViewModelListener.
 *
 * @author   Jan Jancura
 */
public class ViewModelListener extends DebuggerManagerAdapter {
    
    private static final Class[] TREE_MODELS = { TreeModel.class, ReorderableTreeModel.class };
    private static final Class[] TREE_MODEL_FILTERS = { TreeModelFilter.class, ReorderableTreeModelFilter.class };

    private static final Class[] NODE_MODELS = { NodeModel.class, CheckNodeModel.class, DnDNodeModel.class, ExtendedNodeModel.class };
    private static final Class[] NODE_MODEL_FILTERS = { NodeModelFilter.class, CheckNodeModelFilter.class, DnDNodeModelFilter.class, ExtendedNodeModelFilter.class };

    private static final RequestProcessor RP = new RequestProcessor(ViewModelListener.class.getName(), 1);

    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.models") != null;

    private String          viewType;
    private JComponent      view;
    private boolean isUp;
    
    
    public ViewModelListener (
        String viewType,
        JComponent view
    ) {
        this.viewType = viewType;
        this.view = view;
        setUp();
    }
    
    public void setUp() {
        DebuggerManager.getDebuggerManager ().addDebuggerListener (
            DebuggerManager.PROP_CURRENT_ENGINE,
            this
        );
        updateModel ();
    }

    public void destroy () {
        DebuggerManager.getDebuggerManager ().removeDebuggerListener (
            DebuggerManager.PROP_CURRENT_ENGINE,
            this
        );
        if (view instanceof DebuggingView) {
            ((DebuggingView) view).setRootContext(null, null);
        } else {
            Models.setModelsToView (
                view, 
                Models.EMPTY_MODEL
            );
        }
        isUp = false;
    }

    public void propertyChange (PropertyChangeEvent e) {
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
        ContextProvider cp = e != null ? DebuggerManager.join(e, dm) : dm;
        getMultiModels(cp, viewType, treeModels, TREE_MODELS);
        getMultiModels(cp, viewType, treeModelFilters, TREE_MODEL_FILTERS);
        treeExpansionModels =   cp.lookup (viewType, TreeExpansionModel.class);
        treeExpansionModelFilters = cp.lookup (viewType, TreeExpansionModelFilter.class);
        getMultiModels(cp, viewType, nodeModels, NODE_MODELS);
        getMultiModels(cp, viewType, nodeModelFilters, NODE_MODEL_FILTERS);
        tableModels =           cp.lookup (viewType, TableModel.class);
        tableModelFilters =     cp.lookup (viewType, TableModelFilter.class);
        nodeActionsProviders =  cp.lookup (viewType, NodeActionsProvider.class);
        nodeActionsProviderFilters = cp.lookup (viewType, NodeActionsProviderFilter.class);
        columnModels =          cp.lookup (viewType, ColumnModel.class);
        mm =                    cp.lookup (viewType, Model.class);
        asynchModelFilters =    cp.lookup (viewType, AsynchronousModelFilter.class);
        //RequestProcessor rp = (e != null) ? e.lookupFirst(null, RequestProcessor.class) : null;
        
        List models = new ArrayList(11);
        models.add(joinLists(treeModels));
        models.add(joinLists(treeModelFilters));
        models.add(treeExpansionModels);
        models.add(joinLists(nodeModels));
        models.add(joinLists(nodeModelFilters));
        models.add(tableModels);
        models.add(tableModelFilters);
        models.add(nodeActionsProviders);
        models.add(nodeActionsProviderFilters);
        models.add(columnModels);
        models.add(mm);
        models.add(treeExpansionModelFilters);
        models.add(asynchModelFilters);
        /*if (rp != null) {
            models.add(rp);
        }*/
        
        if (view instanceof DebuggingView) {
            ((DebuggingView) view).setRootContext(
                    Models.createCompoundModel(models),
                    e);
        } else {
            Models.setModelsToView (
                view, 
                Models.createCompoundModel (models)
            );
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

    
    // innerclasses ............................................................

    private static class EmptyModel implements NodeModel {
        
        public String getDisplayName (Object node) throws UnknownTypeException {
            if (node == TreeModel.ROOT) {
                return "Name"; // TODO: Localized ???
            }
            throw new UnknownTypeException (node);
        }
        
        public String getIconBase (Object node) throws UnknownTypeException {
            if (node == TreeModel.ROOT) {
                return "org/netbeans/modules/debugger/resources/DebuggerTab";
            }
            throw new UnknownTypeException (node);
        }
        
        public String getShortDescription (Object node) 
        throws UnknownTypeException {
            throw new UnknownTypeException (node);
        }
        
        public void addModelListener (ModelListener l) {}
        public void removeModelListener (ModelListener l) {}
    }
}
