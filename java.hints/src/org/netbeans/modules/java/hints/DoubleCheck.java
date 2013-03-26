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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav tulach
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.DoubleCheck", description = "#DESC_org.netbeans.modules.java.hints.DoubleCheck", id="org.netbeans.modules.java.hints.DoubleCheck", category="thread", suppressWarnings="DoubleCheckedLocking")
public class DoubleCheck {

    @TriggerTreeKind(Kind.SYNCHRONIZED)
    public static ErrorDescription run(HintContext ctx) {
        CompilationInfo compilationInfo = ctx.getInfo();
        TreePath treePath = ctx.getPath();
        Tree e = treePath.getLeaf();

        SynchronizedTree synch = (SynchronizedTree)e;
        TreePath outer = findOuterIf(ctx, treePath);
        if (outer == null) {
            return null;
        }

        IfTree same = null;
        TreePath block = new TreePath(treePath, synch.getBlock());
        for (StatementTree statement : synch.getBlock().getStatements()) {
            if (sameIfAndValidate(compilationInfo, new TreePath(block, statement), outer)) {
                same = (IfTree)statement;
                break;
            }
            if (ctx.isCanceled()) {
                return null;
            }
        }
        if (same == null) {
            return null;
        }

        Fix fix = new FixImpl(
TreePathHandle.create(treePath, compilationInfo),
TreePathHandle.create(outer, compilationInfo),
compilationInfo.getFileObject()
).toEditorFix();

        int span = (int)compilationInfo.getTrees().getSourcePositions().getStartPosition(
            compilationInfo.getCompilationUnit(),
            synch
        );

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), NbBundle.getMessage(DoubleCheck.class, "ERR_DoubleCheck"), fix);// NOI18N
    }

    private static TreePath findOuterIf(HintContext ctx, TreePath treePath) {
        while (!ctx.isCanceled()) {
            treePath = treePath.getParentPath();
            if (treePath == null) {
                break;
            }
            Tree leaf = treePath.getLeaf();
            
            if (leaf.getKind() == Kind.IF) {
                return treePath;
            }
            
            if (leaf.getKind() == Kind.BLOCK) {
                BlockTree b = (BlockTree)leaf;
                if (b.getStatements().size() == 1) {
                    // ok, empty blocks can be around synchronized(this) 
                    // statements
                    continue;
                }
            }
            
            return null;
        }
        return null;
    }

    private static boolean sameIfAndValidate(CompilationInfo info, TreePath statementTP, TreePath secondTP) {
        StatementTree statement = (StatementTree) statementTP.getLeaf();
        
        if (statement.getKind() != Kind.IF) {
            return false;
        }
        
        IfTree first = (IfTree)statement;
        IfTree second = (IfTree) secondTP.getLeaf();
        
        if (first.getElseStatement() != null) {
            return false;
        }
        if (second.getElseStatement() != null) {
            return false;
        }
        
        TreePath varFirst = equalToNull(new TreePath(statementTP, first.getCondition()));
        TreePath varSecond = equalToNull(new TreePath(secondTP, second.getCondition()));
        
        if (varFirst == null || varSecond == null) {
            return false;
        }

        Element firstVariable = info.getTrees().getElement(varFirst);
        Element secondVariable = info.getTrees().getElement(varSecond);
        
        if (firstVariable != null && firstVariable.equals(secondVariable)) {
            return    info.getSourceVersion().compareTo(SourceVersion.RELEASE_5) < 0
                   || !firstVariable.getModifiers().contains(Modifier.VOLATILE);
        }
        
        return false;
    }
    
    private static TreePath equalToNull(TreePath tp) {
        ExpressionTree t = (ExpressionTree) tp.getLeaf();
        if (t.getKind() == Kind.PARENTHESIZED) {
            ParenthesizedTree p = (ParenthesizedTree)t;
            t = p.getExpression();
            tp = new TreePath(tp, t);
        }
        
        if (t.getKind() != Kind.EQUAL_TO) {
            return null;
        }
        BinaryTree bt = (BinaryTree)t;
        if (bt.getLeftOperand().getKind() == Kind.NULL_LITERAL && bt.getRightOperand().getKind() != Kind.NULL_LITERAL) {
            return new TreePath(tp, bt.getRightOperand());
        }
        if (bt.getLeftOperand().getKind() != Kind.NULL_LITERAL && bt.getRightOperand().getKind() == Kind.NULL_LITERAL) {
            return new TreePath(tp, bt.getLeftOperand());
        }
        return null;
    }

    private static final class FixImpl extends JavaFix {
        private TreePathHandle synchHandle;
        private FileObject file;

        public FixImpl(TreePathHandle synchHandle, TreePathHandle ifHandle, FileObject file) {
            super(ifHandle);
            this.synchHandle = synchHandle;
            this.file = file;
        }
        
        
        public String getText() {
            return NbBundle.getMessage(DoubleCheck.class, "FIX_DoubleCheck"); // NOI18N
        }
        
        @Override public String toString() {
            return "FixDoubleCheck"; // NOI18N
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath ifTreePath = ctx.getPath();
            Tree syncTree = synchHandle.resolve(wc).getLeaf();
            wc.rewrite(ifTreePath.getLeaf(), syncTree);
        }
    }
    
}
