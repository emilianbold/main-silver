/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.lang.model.SourceVersion;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.jackpot.code.spi.Constraint;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPatterns;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.JavaFix;
import org.netbeans.modules.java.hints.jackpot.spi.MatcherUtilities;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
@Hint(category="rules15", suppressWarnings="ConvertToStringSwitch")
public class ConvertToStringSwitch {

    private static final String[] PATTERNS = {
        "$var == $constant",
        "$constant == $var",
        "$var.equals($constant)",
        "$constant.equals($var)",
        "$var.contentEquals($constant)",
        "$constant.contentEquals($var)"
    };

    @TriggerPatterns({
        @TriggerPattern(value="if ($c1 == $c2) $body; else $else;",
                        constraints={
                            @Constraint(variable="$c1", type="java.lang.String"),
                            @Constraint(variable="$c2", type="java.lang.String")
                        }),
        @TriggerPattern(value="if ($c1.equals($c2) $body; else $else;",
                        constraints={
                            @Constraint(variable="$c1", type="java.lang.String"),
                            @Constraint(variable="$c2", type="java.lang.String")
                        }),
        @TriggerPattern(value="if ($c1.contentEquals($c2) $body; else $else;",
                        constraints={
                            @Constraint(variable="$c1", type="java.lang.String"),
                            @Constraint(variable="$c2", type="java.lang.String")
                        })
    })
    public static List<ErrorDescription> hint(HintContext ctx) {
        if (   ctx.getPath().getParentPath().getLeaf().getKind() == Kind.IF
            || ctx.getInfo().getSourceVersion().compareTo(SourceVersion.RELEASE_7) < 0) {
            return null;
        }

        Map<TreePathHandle, TreePathHandle> literal2Statement = new LinkedHashMap<TreePathHandle, TreePathHandle>();
        TreePathHandle defaultStatement = null;

        TreePath c1 = ctx.getVariables().get("$c1");
        TreePath c2 = ctx.getVariables().get("$c2");
        TreePath body = ctx.getVariables().get("$body");
        TreePath variable;

        if (Utilities.isConstantString(ctx.getInfo(), c1)) {
            literal2Statement.put(TreePathHandle.create(c1, ctx.getInfo()), TreePathHandle.create(body, ctx.getInfo()));
            variable = c2;
        } else if (Utilities.isConstantString(ctx.getInfo(), c2)) {
            literal2Statement.put(TreePathHandle.create(c2, ctx.getInfo()), TreePathHandle.create(body, ctx.getInfo()));
            variable = c1;
        } else {
            return null;
        }

        ctx.getVariables().put("$var", variable); //XXX: hack

        TreePath tp = ctx.getVariables().get("$else");

        while (true) {
            if (tp.getLeaf().getKind() == Kind.IF) {
                IfTree it = (IfTree) tp.getLeaf();
                TreePath lt = isStringComparison(ctx, new TreePath(tp, it.getCondition()));

                if (lt == null) {
                    return null;
                }

                literal2Statement.put(TreePathHandle.create(lt, ctx.getInfo()), TreePathHandle.create(new TreePath(tp, it.getThenStatement()), ctx.getInfo()));

                if (it.getElseStatement() == null) {
                    break;
                }

                tp = new TreePath(tp, it.getElseStatement());
            } else {
                defaultStatement = TreePathHandle.create(tp, ctx.getInfo());
                break;
            }
        }

        if (literal2Statement.size() <= 1) {
            return null;
        }

        Fix convert = JavaFix.toEditorFix(new ConvertToSwitch(ctx.getInfo(),
                                                              ctx.getPath(),
                                                              TreePathHandle.create(variable, ctx.getInfo()),
                                                              literal2Statement,
                                                              defaultStatement));
        ErrorDescription ed = ErrorDescriptionFactory.forName(ctx,
                                                              ctx.getPath(),
                                                              "Convert to switch",
                                                              convert);

        return Collections.singletonList(ed);
    }

    private static TreePath isStringComparison(HintContext ctx, TreePath tp) {
        Tree leaf = tp.getLeaf();

        while (leaf.getKind() == Kind.PARENTHESIZED) {
            tp = new TreePath(tp, ((ParenthesizedTree) leaf).getExpression());
            leaf = tp.getLeaf();
        }

        for (String patt : PATTERNS) {
            ctx.getVariables().remove("$constant");

            if (!MatcherUtilities.matches(ctx, tp, patt, true))
                continue;

            return ctx.getVariables().get("$constant");
        }

        return null;
    }

    private static final class ConvertToSwitch extends JavaFix {

        private final TreePathHandle value;
        private final Map<TreePathHandle, TreePathHandle> literal2Statement;
        private final TreePathHandle defaultStatement;

        public ConvertToSwitch(CompilationInfo info, TreePath create, TreePathHandle value, Map<TreePathHandle, TreePathHandle> literal2Statement, TreePathHandle defaultStatement) {
            super(info, create);
            this.value = value;
            this.literal2Statement = literal2Statement;
            this.defaultStatement = defaultStatement;
        }

        public String getText() {
            return NbBundle.getMessage(ConvertToStringSwitch.class, "FIX_ConvertToStringSwitch");
        }

        @Override
        protected void performRewrite(WorkingCopy copy, TreePath it, UpgradeUICallback callback) {
            TreeMaker make = copy.getTreeMaker();
            List<CaseTree> cases = new LinkedList<CaseTree>();

            for (Entry<TreePathHandle, TreePathHandle> e : ConvertToSwitch.this.literal2Statement.entrySet()) {
                TreePath l = e.getKey().resolve(copy);
                TreePath s = e.getValue().resolve(copy);

                if (l == null || s == null) {
                    return ;
                }

                addCase(copy, s, cases, (ExpressionTree) l.getLeaf());
            }

            if (defaultStatement != null) {
                TreePath s = defaultStatement.resolve(copy);

                if (s == null) {
                    return ;
                }

                addCase(copy, s, cases, null);
            }

            TreePath value = ConvertToSwitch.this.value.resolve(copy);

            SwitchTree s = make.Switch((ExpressionTree) value.getLeaf(), cases);

            copy.rewrite(it.getLeaf(), s); //XXX
        }

        private void addCase(WorkingCopy copy, TreePath s, List<CaseTree> cases, ExpressionTree value) {
            TreeMaker make = copy.getTreeMaker();
            List<StatementTree> statements = new LinkedList<StatementTree>();
            Tree then = s.getLeaf();
            boolean exitsFromAllBranches = false;

            if (then.getKind() == Kind.BLOCK) {
                //XXX: should verify declarations inside the blocks
                statements.addAll(((BlockTree) then).getStatements());

                for (Tree st : ((BlockTree) then).getStatements()) {
                    exitsFromAllBranches |= Utilities.exitsFromAllBranchers(copy, new TreePath(s, st));
                }
            } else {
                statements.add((StatementTree) then);
                exitsFromAllBranches = Utilities.exitsFromAllBranchers(copy, s);
            }

            if (!exitsFromAllBranches) {
                statements.add(make.Break(null));
            }

            cases.add(make.Case(value, statements));
        }

    }

}
