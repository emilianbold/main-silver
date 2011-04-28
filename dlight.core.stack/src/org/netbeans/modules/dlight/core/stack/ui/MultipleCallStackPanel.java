/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.core.stack.ui;

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.dataprovider.SourceFileInfoDataProvider;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mt154047
 */
public final class MultipleCallStackPanel extends JPanel implements ExplorerManager.Provider, Lookup.Provider {

    private final ExplorerManager manager = new ExplorerManager();
    private final MultipleCallStackRootNode rootNode;
    private final BeanTreeView treeView;
    private Lookup lookup;
    private final SourceFileInfoDataProvider sourceFileInfoDataProvider;
    private final boolean useHtmlFormat;
    private final GoToSourceCallbackAction callbackAction;

    private MultipleCallStackPanel(SourceFileInfoDataProvider sourceFileInfoDataProvider, boolean useHTML,
            GoToSourceCallbackAction callbackAction) {        
        this.sourceFileInfoDataProvider = sourceFileInfoDataProvider;
        this.callbackAction = callbackAction;
        this.useHtmlFormat = useHTML;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        lookup = ExplorerUtils.createLookup(manager, new ActionMap());
        treeView = new MyOwnBeanTreeView();
        treeView.setRootVisible(false);
        add(treeView);
        Action expandAll = new AbstractAction(NbBundle.getMessage(MultipleCallStackPanel.class, "ExpandAll")) {//NOI18N

            @Override
            public void actionPerformed(ActionEvent e) {
                MultipleCallStackPanel.this.treeView.expandAll();
            }
        };
        Action copyAction = new AbstractAction(NbBundle.getMessage(MultipleCallStackPanel.class, "CopyStack")) {//NOI18N

            @Override
            public void actionPerformed(ActionEvent e) {
                //rootNode-> StackRootNode->(childen - PlainFunctionCallChildren)PlainFunctionCallNode
                StringBuilder content = new StringBuilder();
                Node[] stackRootNodes = MultipleCallStackPanel.this.rootNode.getChildren().getNodes();
                for (Node n : stackRootNodes){
                    if (n instanceof StackRootNode){
                        Children c = ((StackRootNode)n).getChildren();
                        Node[] fcNodes = c.getNodes();
                        content.append(n.getDisplayName()).append("\n");
                        for (Node fcNode : fcNodes){
                           content.append("    ").append(fcNode.getName()).append("\n");
                        }                        
                    }
                }
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(content.toString()), new ClipboardOwner() {

                    @Override
                    public void lostOwnership(Clipboard clipboard, Transferable contents) {
                        //do nothing
                    }
                });
            }
        };
        rootNode = new MultipleCallStackRootNode(expandAll, new Action[]{copyAction});
        manager.setRootContext(rootNode);//NOI18N
        final JPopupMenu popup = new JPopupMenu();
        popup.add(expandAll);
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    popup.show(MultipleCallStackPanel.this, e.getX(), e.getY());
                }
            }
        });
        ActionMap map = new ActionMap();
        map.put("org.openide.actions.PopupAction", expandAll);//NOI18N
        setActionMap(map);
        treeView.setPopupAllowed(true);
        treeView.setActionMap(map);
    }

    public static MultipleCallStackPanel createInstance() {
        return new MultipleCallStackPanel(null, false, null);
    }

    public static MultipleCallStackPanel createInstance(SourceFileInfoDataProvider sourceFileInfoDataProvider) {
        return new MultipleCallStackPanel(sourceFileInfoDataProvider, false, null);
    }

    public static MultipleCallStackPanel createInstance(SourceFileInfoDataProvider sourceFileInfoDataProvider,
            boolean useHTML) {
        return new MultipleCallStackPanel(sourceFileInfoDataProvider, useHTML, null);
    }

    public static MultipleCallStackPanel createInstance(SourceFileInfoDataProvider sourceFileInfoDataProvider,
            boolean useHTML, GoToSourceCallbackAction callbackAction) {
        return new MultipleCallStackPanel(sourceFileInfoDataProvider, useHTML, callbackAction);
    }

    @Override
    public boolean requestFocus(boolean temporary) {
        if (treeView != null) {
            return treeView.requestFocus(temporary);
        }
         return super.requestFocus(temporary);
    }

    public void clean() {
        rootNode.removeAll();
        treeView.setRootVisible(false);
    }

    @Override
    public void addNotify() {
        //  rootNode.update();
        super.addNotify();
        treeView.expandAll();

    }

    @Override
    public void requestFocus() {
        treeView.requestFocus();
    }

    @Override
    public boolean requestFocusInWindow() {
        return treeView.requestFocusInWindow();
    }

    public void expandAll() {
        if (manager.getRootContext() == null) {
            return;
        }
        treeView.expandAll();
    }

    public void scrollToRoot() {
        //     Component c  = treeView.getComponents();
        treeView.getViewport().setViewPosition(new Point(0, 0));
    }

    public void expandNode(Node node){
        treeView.expandNode(node);
    }

    public void setRootVisible(String rootName) {
        treeView.setRootVisible(true);
        rootNode.setDisplayName(rootName);
    }

    public final void add(String rootName, Icon icon, List<FunctionCall> stack, Action[] actions) {
        rootNode.add(new StackRootNode(sourceFileInfoDataProvider, icon, rootName, stack, actions, useHtmlFormat, callbackAction));
    }


    public final void add(String rootName, Icon icon, List<FunctionCall> stack) {
        rootNode.add(new StackRootNode(sourceFileInfoDataProvider, icon, rootName, stack, useHtmlFormat, callbackAction));
    }

    public final void add(String rootName, boolean isRootVisible, List<FunctionCall> stack) {
        treeView.setRootVisible(false);
        rootNode.add(new StackRootNode(sourceFileInfoDataProvider, rootName, stack, useHtmlFormat, callbackAction));
    }

    public void update() {
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    public Lookup getLookup() {
        return lookup;
    }

    private static final class MyOwnBeanTreeView extends BeanTreeView {

        MyOwnBeanTreeView() {
            super();
        }

        @Override
        public void expandAll() {
            if (!EventQueue.isDispatchThread()){
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        MyOwnBeanTreeView.super.expandAll();
                    }
                });
           }else{
                super.expandAll();
           }
            

        }
    }
}
