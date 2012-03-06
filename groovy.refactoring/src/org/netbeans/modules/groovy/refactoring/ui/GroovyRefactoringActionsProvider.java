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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.refactoring.ui;

import java.util.Collection;
import javax.swing.JOptionPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.AstUtilities;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.groovy.editor.api.parser.SourceUtils;
import org.netbeans.modules.groovy.refactoring.GroovyRefactoringElement;
import org.netbeans.modules.groovy.refactoring.utils.GroovyProjectUtil;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author martin
 */
@ServiceProvider(service = ActionsImplementationProvider.class, position=100)
public class GroovyRefactoringActionsProvider extends ActionsImplementationProvider {

    private static boolean isFindUsages;

    @Override
    public boolean canFindUsages(Lookup lookup) {
        /*
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        if (nodes.size() != 1) {
            return false;
        }
        Node n = nodes.iterator().next();

        DataObject dob = n.getCookie(DataObject.class);
        if (dob == null) {
            return false;
        }

        FileObject fo = dob.getPrimaryFile();

        if ((dob!=null) && GroovyProjectUtil.isGroovyOrGspFile(fo)) {
            return true;
        }
        */
        return false;
    }

    @Override
    public void doFindUsages(Lookup lookup) {
        Runnable task;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        FileObject fileObject = lookup.lookup(FileObject.class);
        if (isFromEditor(ec)) {
            task = new TextComponentTask(ec, fileObject);
        } else {
            task = new NodeToElementTask(lookup.lookupAll(Node.class), fileObject);
        }
        try {
            isFindUsages = true;
            task.run();
        } finally {
            isFindUsages = false;
        }
    }

    /**
     * GroovyProjectUtil.isGspFile is not implemented yet, so it's useless to call
     * this method at the moment.
     *
     * @deprecated it will always return false at the moment
     */
    @Deprecated
    private static boolean isOutsideGroovy(Lookup lookup, FileObject fo) {
        if (GroovyProjectUtil.isGspFile(fo)) {
            // We're attempting to refactor in an RHTML file... If it's in
            // the editor, make sure we're trying to refactoring in a Ruby section;
            // if not, we shouldn't grab it. (JavaScript refactoring won't get
            // invoked if Ruby returns true for canRename even when the caret is
            // in the caret section
            EditorCookie ec = lookup.lookup(EditorCookie.class);
            if (isFromEditor(ec)) {
                JTextComponent textC = ec.getOpenedPanes()[0];
                Document d = textC.getDocument();
                if (!(d instanceof BaseDocument)) {
                    return true;
                }
                int caret = textC.getCaretPosition();
                if (LexUtilities.getToken((BaseDocument)d, caret) == null) {
                    // Not in Ruby code!
                    return true;
                }

            }
        }

        return false;
    }

    private static boolean isFromEditor(EditorCookie ec) {
        if (ec != null && ec.getOpenedPanes() != null) {
            // This doesn't seem to work well - a lot of the time, I'm right clicking
            // on the editor and it still has another activated view (this is on the mac)
            // and as a result does file-oriented refactoring rather than the specific
            // editor node...
            //            TopComponent activetc = TopComponent.getRegistry().getActivated();
            //            if (activetc instanceof CloneableEditorSupport.Pane) {
            //
            return true;
            //            }
        }

        return false;
    }

    private class TextComponentTask extends UserTask implements Runnable {
        private JTextComponent textC;
        private int caret;
        private int start;
        private int end;
        private RefactoringUI ui;
        private final FileObject fileObject;

        public TextComponentTask(EditorCookie ec, FileObject fileObject) {
            this.textC = ec.getOpenedPanes()[0];
            this.caret = textC.getCaretPosition();
            this.start = textC.getSelectionStart();
            this.end = textC.getSelectionEnd();
            this.fileObject = fileObject;
            assert caret != -1;
            assert start != -1;
            assert end != -1;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            GroovyParserResult cc = AstUtilities.getParseResult(resultIterator.getParserResult());
            ASTNode root = AstUtilities.getRoot(cc);
            if (root == null) {
                return;
            }

            BaseDocument doc = GroovyProjectUtil.getDocument(cc, fileObject);
            AstPath path = new AstPath(root, caret, doc);

            GroovyRefactoringElement ctx = new GroovyRefactoringElement(cc, (ModuleNode) root, path.leaf(), fileObject);
            if (ctx.getSimpleName() == null) {
                return;
            }
            ui = createRefactoringUI(ctx, start, end, cc);
        }

        @Override
        public final void run() {
            try {
                SourceUtils.runUserActionTask(fileObject, this);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            TopComponent activetc = TopComponent.getRegistry().getActivated();

            if (ui != null) {
                UI.openRefactoringUI(ui, activetc);
            } else {
                String key = "ERR_CannotRenameLoc"; // NOI18N
                if (isFindUsages) {
                    key = "ERR_CannotFindUsages"; // NOI18N
                }
                JOptionPane.showMessageDialog(null, NbBundle.getMessage(GroovyRefactoringActionsProvider.class, key));
            }
        }

        protected RefactoringUI createRefactoringUI(GroovyRefactoringElement selectedElement,int startOffset,int endOffset, GroovyParserResult info) {
            return new WhereUsedQueryUI(selectedElement);
        }
    }

    private class NodeToElementTask extends UserTask implements Runnable {
        private Node node;
        private RefactoringUI ui;
        private final FileObject fileObject;

        public NodeToElementTask(Collection<? extends Node> nodes, FileObject fileObject) {
            assert nodes.size() == 1;
            this.node = nodes.iterator().next();
            this.fileObject = fileObject;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            GroovyParserResult info = AstUtilities.getParseResult(resultIterator.getParserResult());
            ASTNode root = AstUtilities.getRoot(info);
            if (root != null) {
                // FIXME parsing API
                GroovyRefactoringElement fileCtx = new GroovyRefactoringElement(info, (ModuleNode) root, root,
                        info.getSnapshot().getSource().getFileObject());
                ui = createRefactoringUI(fileCtx, info);
            }
        }

        @Override
        public final void run() {
            DataObject o = node.getCookie(DataObject.class);
            try {
                SourceUtils.runUserActionTask(fileObject, this);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            UI.openRefactoringUI(ui);
        }

        protected RefactoringUI createRefactoringUI(GroovyRefactoringElement selectedElement, GroovyParserResult info) {
            return new WhereUsedQueryUI(selectedElement);
        }
    }
}
