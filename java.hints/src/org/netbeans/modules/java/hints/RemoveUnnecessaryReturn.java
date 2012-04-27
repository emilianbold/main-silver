/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.RemoveUnnecessaryReturn", description = "#DESC_org.netbeans.modules.java.hints.RemoveUnnecessaryReturn", category="general", suppressWarnings="UnnecessaryReturnStatement")
public class RemoveUnnecessaryReturn {

    @TriggerPattern("return $val$;")
    public static ErrorDescription hint(HintContext ctx) {
        TreePath tp = ctx.getPath();

        OUTER: while (tp != null && !TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
            Tree current = tp.getLeaf();
            List<? extends StatementTree> statements;

            tp = tp.getParentPath();

            switch (tp.getLeaf().getKind()) {
                case METHOD:
                    MethodTree mt = (MethodTree) tp.getLeaf();

                    if (mt.getReturnType() == null) {
                        if (mt.getName().contentEquals("<init>"))
                            break OUTER;//constructor
                        else
                            return null; //a method without a return type - better ignore
                    }
                    
                    TypeMirror tm = ctx.getInfo().getTrees().getTypeMirror(new TreePath(tp, mt.getReturnType()));

                    if (tm == null || tm.getKind() != TypeKind.VOID) return null;
                    break OUTER;
                case BLOCK: statements = ((BlockTree) tp.getLeaf()).getStatements(); break;
                case CASE: {
                    if (tp.getParentPath().getLeaf().getKind() == Kind.SWITCH) {
                        List<? extends CaseTree> cases = ((SwitchTree) tp.getParentPath().getLeaf()).getCases();
                        List<StatementTree> locStatements = new ArrayList<StatementTree>();

                        for (int i = cases.indexOf(tp.getLeaf()); i < cases.size(); i++) {
                            locStatements.addAll(cases.get(i).getStatements());
                        }

                        statements = locStatements;
                    } else {
                        //???
                        statements = ((CaseTree) tp.getLeaf()).getStatements();
                    }
                    break;
                }
                case DO_WHILE_LOOP:
                case ENHANCED_FOR_LOOP:
                case FOR_LOOP:
                case WHILE_LOOP:
                    return null;
                case TRY:
                    if (((TryTree) tp.getLeaf()).getFinallyBlock() == current) return null;
                default: continue OUTER;
            }

            assert !statements.isEmpty();

            int i = statements.indexOf(current);

            if (i == (-1)) {
                //XXX: should not happen?
                return null;
            }

            while (i + 1 < statements.size()) {
                StatementTree next = statements.get(i + 1);

                if (next.getKind() == Kind.EMPTY_STATEMENT) {
                    i++;
                    continue;
                }

                if (next.getKind() == Kind.BLOCK) {
                    statements = ((BlockTree) next).getStatements();
                    i = -1;
                    continue;
                }

                if (next.getKind() == Kind.BREAK) {
                    tp = TreePath.getPath(ctx.getInfo().getCompilationUnit(), ctx.getInfo().getTreeUtilities().getBreakContinueTarget(new TreePath(tp, next)));
                    continue OUTER;
                }

                return null;
            }
        }

        String displayName = NbBundle.getMessage(RemoveUnnecessaryReturn.class, "ERR_UnnecessaryReturnStatement");
        String fixDisplayName = NbBundle.getMessage(RemoveUnnecessaryReturn.class, "FIX_UnnecessaryReturnStatement");
        
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), displayName, JavaFixUtilities.removeFromParent(ctx, fixDisplayName, ctx.getPath()));
    }
}
