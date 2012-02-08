/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.JOptionPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.*;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * TODO: should be API
 * @author Jan Becicka
 */
public final class ContextAnalyzer {

    /**
     * create analyze task.
     * @param context context of refactoring
     * @param factory for creating of RefactoringUI
     * @return
     */
    public static Runnable createTask(Lookup context, final JavaRefactoringUIFactory factory) {

        Runnable task;
        EditorCookie ec = context.lookup(EditorCookie.class);
        if (isFromEditor(ec)) {
            task = new TextComponentTask(ec) {

                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement, int startOffset, int endOffset, final CompilationInfo info) {
                    return factory.create(info, new TreePathHandle[]{selectedElement}, new FileObject[]{selectedElement.getFileObject()}, new NonRecursiveFolder[0]);
                }
            };
        } else if (nodeHandle(context)) {
            task = new TreePathHandleTask(new HashSet<Node>(context.lookupAll(Node.class)), false) {

                RefactoringUI ui;
                private boolean created;

                @Override
                protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                    if (size()==1) {
                        ui = factory.create(javac, new TreePathHandle[]{handle}, null, new NonRecursiveFolder[0]);
                        created = true;
                    }
                }

                @Override
                protected RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles) {
                    if (!created) 
                        ui = factory.create(null, handles.toArray(new TreePathHandle[handles.size()]), null,  new NonRecursiveFolder[0]);
                    return ui;
                }
            };
        } else {
             task = new NodeToFileObjectTask(new HashSet<Node>(context.lookupAll(Node.class))) {

                RefactoringUI ui;
                private boolean created = false;

                @Override
                protected void nodeTranslated(Node node, Collection<TreePathHandle> handles, CompilationInfo javac) {
                    if (size()==1) {
                        ui = factory.create(javac, handles.toArray(new TreePathHandle[1]), new FileObject[]{handles.iterator().next().getFileObject()}, new NonRecursiveFolder[0]);
                        created = true;
                    }
                }

                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<TreePathHandle> handles) {
                    if (!created) 
                        ui = factory.create(null, handles.toArray(new TreePathHandle[handles.size()]), selectedElements, pkg);
                    return ui;
                }
            };
        }
        return task;
    }

    private static boolean isFromEditor(EditorCookie ec) {
        if (ec != null && ec.getOpenedPanes() != null) {
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            if (activetc instanceof CloneableEditorSupport.Pane) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * utility method to perform enable/disable logic for refactoring actions
     * @param lookup
     * @param onlyFromEditor action is enabled only in editor
     * @return 
     */
    public static boolean canRefactorSingle(Lookup lookup, boolean onlyFromEditor) {
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        if(nodes.size() != 1)
            return false;
        Node node = nodes.iterator().next();
        TreePathHandle tph = node.getLookup().lookup(TreePathHandle.class);
        if (tph != null) {
            return JavaRefactoringUtils.isRefactorable(tph.getFileObject());
        }
        DataObject dObj = node.getCookie(DataObject.class);
        if(null == dObj)
            return false;
        FileObject fileObj = dObj.getPrimaryFile();
        if(null == fileObj || !JavaRefactoringUtils.isRefactorable(fileObj))
            return false;

        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (isFromEditor(ec)) {
            return true;
        }
        return !onlyFromEditor;
    }
    
    private static abstract class TreePathHandleTask implements Runnable, CancellableTask<CompilationController> {
        private Collection<TreePathHandle> handles = new ArrayList<TreePathHandle>();
        private TreePathHandle current;
        boolean renameFile;
     
        public TreePathHandleTask(Collection<? extends Node> nodes) {
            this(nodes, false);
        }
        
        public int size() {
            return handles.size();
        }
        public TreePathHandleTask(Collection<? extends Node> nodes, boolean useFirstHandle) {
            for (Node n:nodes) {
                TreePathHandle temp = n.getLookup().lookup(TreePathHandle.class);
                if (temp!=null) {
                    handles.add(temp);
                    if (useFirstHandle) {
                        break;
                    }
                }
            }
        }
        
        public TreePathHandleTask(TreePathHandle tph) {
            handles.add(tph);
        }
        
        @Override
        public void cancel() {
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            Element el = current.resolveElement(info);
            if (el!=null && el instanceof TypeElement && !((TypeElement)el).getNestingKind().isNested()) {
                if (info.getFileObject().getName().equals(el.getSimpleName().toString())) {
                    renameFile = true;
                }
            }
            treePathHandleResolved(current, info);
        }
        
        @Override
        public void run() {
            for (TreePathHandle handle:handles) {
                FileObject f = handle.getFileObject();
                if (f==null) {
                    //ugly workaround for #205142
                    TopComponent top = (TopComponent) EditorRegistry.lastFocusedComponent().getParent().getParent().getParent().getParent();
                    f = top.getLookup().lookup(FileObject.class);
                }
                current = handle;
                JavaSource source = JavaSource.forFileObject(f);
                assert source != null;
                try {
                    source.runUserActionTask(this, true);
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            TopComponent activetc = TopComponent.getRegistry().getActivated();

            RefactoringUI ui = createRefactoringUI(handles);
            if (ui!=null) {
                UI.openRefactoringUI(ui, activetc);
            } else {
                JOptionPane.showMessageDialog(null,NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_CannotRenameKeyword"));
            }
        }

        /**
         * This is the place where subclasses may collect info about handles.
         * @param handle handle
         * @param javac context of running transaction
         */
        protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
        }

        protected abstract RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles);
    }    
    
    private static abstract class TextComponentTask implements Runnable, CancellableTask<CompilationController> {
        private JTextComponent textC;
        private int caret;
        private int start;
        private int end;
        private RefactoringUI ui;
        private boolean selection;
        
        public TextComponentTask(EditorCookie ec) {
            this(ec,false);
        }
        
        public TextComponentTask(EditorCookie ec, boolean selection) {
            this.selection = selection;
            this.textC = ec.getOpenedPanes()[0];
            this.caret = textC.getCaretPosition();
            this.start = textC.getSelectionStart();
            this.end = textC.getSelectionEnd();
            assert caret != -1;
            assert start != -1;
            assert end != -1;
        }
        
        @Override
        public void cancel() {
        }
        
        @Override
        public void run(final CompilationController cc) throws Exception {
            TreePath selectedElement = null;
            cc.toPhase(JavaSource.Phase.RESOLVED);
            
            final int c = selection?start:this.caret;

            final int[] adjustedCaret = new int[] {c};
//            final boolean[] insideJavadoc = {false};
            final Document doc = cc.getDocument();
            doc.render(new Runnable() {
                @Override
                public void run() {
                    TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(cc.getTokenHierarchy(), c);

                    ts.move(c);

                    if (ts.moveNext() && ts.token()!=null) {
                        if (ts.token().id() == JavaTokenId.IDENTIFIER) {
                            adjustedCaret[0] = ts.offset() + ts.token().length() / 2 + 1;
                        } /*else if (ts.token().id() == JavaTokenId.JAVADOC_COMMENT) {
                            TokenSequence<JavadocTokenId> jdts = ts.embedded(JavadocTokenId.language());
                            if (jdts != null && JavadocImports.isInsideReference(jdts, caret)) {
                                jdts.move(caret);
                                if (jdts.moveNext() && jdts.token().id() == JavadocTokenId.IDENT) {
                                    adjustedCaret[0] = jdts.offset();
                                    insideJavadoc[0] = true;
                                }
                            } else if (jdts != null && JavadocImports.isInsideParamName(jdts, caret)) {
                                jdts.move(caret);
                                if (jdts.moveNext()) {
                                    adjustedCaret[0] = jdts.offset();
                                    insideJavadoc[0] = true;
                                }
                            }
                        }*/
                    }
                }
            });
            selectedElement = cc.getTreeUtilities().pathFor(adjustedCaret[0]);
            //workaround for issue 89064
            if (selectedElement.getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
                List<? extends Tree> decls = cc.getCompilationUnit().getTypeDecls();
                if (!decls.isEmpty()) {
                    TreePath path = TreePath.getPath(cc.getCompilationUnit(), decls.get(0));
                    if (path!=null && cc.getTrees().getElement(path)!=null) {
                        selectedElement = path;
                    }
                }
            }
            ui = createRefactoringUI(TreePathHandle.create(selectedElement, cc), start, end, cc);
        }
        
        @Override
        public final void run() {
            try {
                JavaSource source = JavaSource.forDocument(textC.getDocument());
                source.runUserActionTask(this, true);
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
                return ;
            }
            TopComponent activetc = TopComponent.getRegistry().getActivated();

            SHOW.show(ui, activetc);
        }
        
        protected abstract RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info);
    }
    
    private static abstract class NodeToElementTask implements Runnable, CancellableTask<CompilationController>  {
        private Node node;
        private RefactoringUI ui;
        
        public NodeToElementTask(Collection<? extends Node> nodes) {
            assert nodes.size() == 1;
            this.node = nodes.iterator().next();
        }
        
        @Override
        public void cancel() {
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            CompilationUnitTree unit = info.getCompilationUnit();
            if (unit.getTypeDecls().isEmpty()) {
                ui = createRefactoringUI(null, info);
            } else {
                TreePathHandle representedObject = TreePathHandle.create(TreePath.getPath(unit, unit.getTypeDecls().get(0)),info);
                ui = createRefactoringUI(representedObject, info);
            }
        }
        
        @Override
        public final void run() {
            DataObject o = node.getCookie(DataObject.class);
            JavaSource source = JavaSource.forFileObject(o.getPrimaryFile());
            assert source != null;
            try {
                source.runUserActionTask(this, true);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (ui!=null) {
                UI.openRefactoringUI(ui);
            } else {
                JOptionPane.showMessageDialog(null,NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_NoTypeDecls"));
            }
        }
        protected abstract RefactoringUI createRefactoringUI(TreePathHandle selectedElement, CompilationInfo info);
    }
    
    private static abstract class NodeToFileObjectTask implements Runnable, CancellableTask<CompilationController> {
        private Collection<? extends Node> nodes;
        public NonRecursiveFolder pkg[];
        Collection<TreePathHandle> handles = new ArrayList<TreePathHandle>();
        private Node currentNode;
     
        public NodeToFileObjectTask(Collection<? extends Node> nodes) {
            this.nodes = nodes;
        }
        
        protected int size() {
            return nodes.size();
        }
        
        @Override
        public void cancel() {
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            Collection<TreePathHandle> handlesPerNode = new ArrayList<TreePathHandle>();
            CompilationUnitTree unit = info.getCompilationUnit();
            Collection<TreePathHandle> publicHandles = new ArrayList<TreePathHandle>();
            Collection<TreePathHandle> sameNameHandles = new ArrayList<TreePathHandle>();
            for (Tree t: unit.getTypeDecls()) {
                Element e = info.getTrees().getElement(TreePath.getPath(unit, t));
                if (e == null || !(e.getKind().isClass() || e.getKind().isInterface())) {
                    // syntax errors #111195
                    continue;
                }
                if (e.getSimpleName().toString().equals(info.getFileObject().getName())) {
                    TreePathHandle representedObject = TreePathHandle.create(TreePath.getPath(unit,t),info);
                    sameNameHandles.add(representedObject);
                }
                if (e.getModifiers().contains(Modifier.PUBLIC)) {
                    TreePathHandle representedObject = TreePathHandle.create(TreePath.getPath(unit,t),info);
                    publicHandles.add(representedObject);
                }
            }
            if (!publicHandles.isEmpty()) {
                handlesPerNode.addAll(publicHandles);
            } else {
                handlesPerNode.addAll(sameNameHandles);
            }

            if (!handlesPerNode.isEmpty()) {
                handles.addAll(handlesPerNode);
                nodeTranslated(currentNode, handlesPerNode, info);
            }
        }
        
        @Override
        public void run() {
            FileObject[] fobs = new FileObject[nodes.size()];
            pkg = new NonRecursiveFolder[fobs.length];
            int i = 0;
            for (Node node:nodes) {
                DataObject dob = node.getCookie(DataObject.class);
                if (dob!=null) {
                    fobs[i] = dob.getPrimaryFile();
                    if (RefactoringUtils.isJavaFile(fobs[i])) {
                        JavaSource source = JavaSource.forFileObject(fobs[i]);
                        assert source != null;
                        try {
                            currentNode = node;
                            // XXX this could be optimize by ClasspasthInfo in case of more than one file
                            source.runUserActionTask(this, true);
                        } catch (IllegalArgumentException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            currentNode = null;
                        }
                    }
                    
                    pkg[i++] = node.getLookup().lookup(NonRecursiveFolder.class);
                }
            }
            RefactoringUI ui = createRefactoringUI(fobs, handles);
            if (ui!=null) {
                UI.openRefactoringUI(ui);
            } else {
                JOptionPane.showMessageDialog(null,NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_NoTypeDecls"));
            }
        }

        /**
         * Notifies subclasses about the translation.
         * This is the place where subclasses may collect info about handles.
         * @param node node that is translated
         * @param handles handles translated from the node
         * @param javac context of running translation
         */
        protected void nodeTranslated(Node node, Collection<TreePathHandle> handles, CompilationInfo javac) {
        }

        protected abstract RefactoringUI createRefactoringUI(FileObject[] selectedElement, Collection<TreePathHandle> handles);
    }    

    
    private static boolean nodeHandle(Lookup lookup) {
        Node n = lookup.lookup(Node.class);
        if (n!=null) {
            if (n.getLookup().lookup(TreePathHandle.class)!=null)
                return true;
        }
        return false;
    }

    private static ShowUI SHOW = new ShowUI() {
        @Override
        public void show(RefactoringUI ui, TopComponent activetc) {
            if (ui!=null) {
                UI.openRefactoringUI(ui, activetc);
            } else {
                JOptionPane.showMessageDialog(null,NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_CannotRenameKeyword"));
            }
        }
    };
    private interface ShowUI {
        public void show(RefactoringUI ui, TopComponent activetc);
    }    
}
