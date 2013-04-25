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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.bugtracking.bridge.nodes;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.RepositoryManager;
import org.netbeans.modules.bugtracking.util.RepositoryComparator;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.*;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Root node representing Bugtracking in the Servises window
 *
 * @author Tomas Stupka
 */
public class BugtrackingRootNode extends AbstractNode {
    
    private static final Logger LOG = Logger.getLogger(BugtrackingRootNode.class.getName());
    
    private static final String BUGTRACKING_NODE_NAME = "bugtracking";                                       // NOI18N
    private static final String ICON_BASE = "org/netbeans/modules/bugtracking/ui/resources/bugtracking.png"; // NOI18N
    
    /** Init lock */
    private static final Object LOCK_INIT = new Object();

    /** The only instance of the BugtrackingRootNode in the system */
    private static BugtrackingRootNode defaultInstance;
    
    /**
     * Creates a new instance of BugtrackingRootNode
     */
    private BugtrackingRootNode() {
        super(Children.create(new RootNodeChildren(), true));
        setName(BUGTRACKING_NODE_NAME); 
        setDisplayName(NbBundle.getMessage(BugtrackingRootNode.class, "LBL_BugtrackingNode")); // NOI18N
        setIconBaseWithExtension(ICON_BASE);
    }
    
    /**
     * Creates default instance of BugtrackingRootNode
     *
     * @return default instance of BugtrackingRootNode
     */
    @ServicesTabNodeRegistration(
        name="bugtracking",                                                                 // NOI18N
        displayName="org.netbeans.modules.bugtracking.bridge.nodes.Bundle#LBL_BugtrackingNode", // NOI18N
        iconResource="org/netbeans/modules/bugtracking/ui/resources/bugtracking.png",       // NOI18N
        position=588
    )
    public static BugtrackingRootNode getDefault() {
        synchronized(LOCK_INIT) {
            if (defaultInstance == null) {
                defaultInstance = new BugtrackingRootNode();
            }
            return defaultInstance;
        }
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            new AbstractAction(NbBundle.getMessage(BugtrackingRootNode.class, "LBL_CreateRepository")) { // NOI18N
            @Override
                public void actionPerformed(ActionEvent e) {
                    RepositoryManager.getInstance().createRepository();
                }
            }
        };
    }
    
    private static class RootNodeChildren extends ChildFactory<Repository> implements PropertyChangeListener  {

        /**
         * Creates a new instance of RootNodeChildren
         */
        public RootNodeChildren() {
            RepositoryManager.getInstance().addPropertChangeListener(this);
        }

        @Override
        protected Node createNodeForKey(Repository key) {
            return new RepositoryNode(key);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(RepositoryManager.EVENT_REPOSITORIES_CHANGED)) {
                refresh(false);
            }
        }

        @Override
        protected boolean createKeys(List<Repository> toPopulate) {
            Collection<Repository> repos = RepositoryManager.getInstance().getRepositories();
            
            // populate only mutable repositories -> those that the user can edit or delete
            Iterator<Repository> it = repos.iterator();
            while(it.hasNext()) {
                Repository repo = it.next();
                if(!repo.isMutable()) {
                    it.remove();
                }
            }
            
            toPopulate.addAll(repos);
            Collections.sort(toPopulate, new RepositoryComparator());
            return true;
        }
    }

    public static void selectNode(final String... path) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                TopComponent tab = WindowManager.getDefault().findTopComponent("services"); // NOI18N
                if (tab == null) {
                    // XXX have no way to open it, other than by calling ServicesTabAction
                    LOG.fine("No ServicesTab found"); // NOI18N
                    return;
                }
                tab.open();
                tab.requestActive();
                if (!(tab instanceof ExplorerManager.Provider)) {
                    LOG.fine("ServicesTab not an ExplorerManager.Provider"); // NOI18N
                    return;
                }
                final ExplorerManager mgr = ((ExplorerManager.Provider) tab).getExplorerManager();
                final Node root = mgr.getRootContext();
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        Node repository = NodeOp.findChild(root, BUGTRACKING_NODE_NAME);
                        if (repository == null) {
                            LOG.fine("ServicesTab does not contain node " + BUGTRACKING_NODE_NAME); // NOI18N
                            return;
                        }
                        Node _selected;
                        try {
                            _selected = NodeOp.findPath(repository, path);
                        } catch (NodeNotFoundException x) {
                            LOG.log(Level.FINE, "Could not find subnode", x); // NOI18N
                            _selected = x.getClosestNode();
                        }
                        final Node selected = _selected;
                        Mutex.EVENT.readAccess(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mgr.setSelectedNodes(new Node[] {selected});
                                } catch (PropertyVetoException x) {
                                    LOG.log(Level.FINE, "Could not select path", x); // NOI18N
                                }
                            }
                        });
                    }
                });
            }
        });
    }    
}
