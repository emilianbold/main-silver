/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2011 Sun
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

package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;

/**
 *
 * @author lahvac
 */
public class Flow {

    public static FlowResult assignmentsForUse(CompilationInfo info, AtomicBoolean cancel) {
        return assignmentsForUse(info, new TreePath(info.getCompilationUnit()), cancel);
    }

    public static FlowResult assignmentsForUse(CompilationInfo info, TreePath from, AtomicBoolean cancel) {
        Map<Tree, Iterable<? extends TreePath>> result = new HashMap<Tree, Iterable<? extends TreePath>>();
        VisitorImpl v = new VisitorImpl(info, cancel);

        v.scan(from, null);

        if (cancel.get()) return null;

        for (Entry<Tree, State> e : v.use2Values.entrySet()) {
            result.put(e.getKey(), e.getValue() != null ? e.getValue().assignments : Collections.<TreePath>emptyList());
        }

        v.deadBranches.remove(null);

        return new FlowResult(result, v.deadBranches);
    }

    public static final class FlowResult {
        private final Map<Tree, Iterable<? extends TreePath>> assignmentsForUse;
        private final Set<? extends Tree> deadBranches;
        private FlowResult(Map<Tree, Iterable<? extends TreePath>> assignmentsForUse, Set<Tree> deadBranches) {
            this.assignmentsForUse = assignmentsForUse;
            this.deadBranches = deadBranches;
        }
        public Map<Tree, Iterable<? extends TreePath>> getAssignmentsForUse() {
            return assignmentsForUse;
        }
        public Set<? extends Tree> getDeadBranches() {
            return deadBranches;
        }
    }

    public static boolean definitellyAssigned(CompilationInfo info, VariableElement var, Iterable<? extends TreePath> trees, AtomicBoolean cancel) {
        VisitorImpl v = new VisitorImpl(info, cancel);

        v.variable2State.put(var, State.create(null));

        for (TreePath tp : trees) {
            if (cancel.get()) return false;
            
            v.scan(tp, null);

            if (!v.variable2State.get(var).assignments.contains(null)) return true;
        }

        return false;
    }

    private static final class VisitorImpl extends CancellableTreePathScanner<Boolean, Void> {
        
        private final CompilationInfo info;
        
        private Map<VariableElement, State> variable2State = new HashMap<VariableElement, Flow.State>();
        private Map<Tree, State> use2Values = new IdentityHashMap<Tree, State>();
        private boolean inParameters;
        private final Set<Tree> deadBranches = new HashSet<Tree>();

        public VisitorImpl(CompilationInfo info, AtomicBoolean cancel) {
            super(cancel);
            this.info = info;
        }

        @Override
        public Boolean visitAssignment(AssignmentTree node, Void p) {
            switch (node.getVariable().getKind()) {
                case MEMBER_SELECT:
                    scan(((MemberSelectTree) node.getVariable()).getExpression(), null); //XXX: this will not create a correct TreePath
                    break;
                case ARRAY_ACCESS:
                    scan(node.getVariable(), null);
                    break;
                case IDENTIFIER:
                    break;
                default:
                    //#198233: ignore
            }

            scan(node.getExpression(), p);

            Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable()));
            
            if (e != null && LOCAL_VARIABLES.contains(e.getKind())) {
                variable2State.put((VariableElement) e, State.create(new TreePath(getCurrentPath(), node.getExpression())));
            }
            
            return null;
        }

        @Override
        public Boolean visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
            switch (node.getVariable().getKind()) {
                case MEMBER_SELECT:
                    scan(((MemberSelectTree) node.getVariable()).getExpression(), null); //XXX: this will not create a correct TreePath
                    break;
                case ARRAY_ACCESS:
                    scan(node.getVariable(), null);
                    break;
                case IDENTIFIER:
                    break;
                default:
                    throw new IllegalStateException(node.getVariable().getKind().name());
            }

            scan(node.getExpression(), p);

            Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable()));

            if (e != null && LOCAL_VARIABLES.contains(e.getKind())) {
                use2Values.put(node.getVariable(), variable2State.get((VariableElement) e)); //XXX
                variable2State.put((VariableElement) e, State.create(getCurrentPath()));
            }

            return null;
        }

        @Override
        public Boolean visitVariable(VariableTree node, Void p) {
            super.visitVariable(node, p);

            Element e = info.getTrees().getElement(getCurrentPath());
            
            if (e != null && LOCAL_VARIABLES.contains(e.getKind())) {
                variable2State.put((VariableElement) e, State.create(node.getInitializer() != null ? new TreePath(getCurrentPath(), node.getInitializer()) : inParameters ? getCurrentPath() : null));
            }
            
            return null;
        }

        @Override
        public Boolean visitMemberSelect(MemberSelectTree node, Void p) {
            super.visitMemberSelect(node, p);

            Element e = info.getTrees().getElement(getCurrentPath());

            if (e != null && LOCAL_VARIABLES.contains(e.getKind())) {
                use2Values.put(node, variable2State.get((VariableElement) e));
            }
            
            return null;
        }

        @Override
        public Boolean visitLiteral(LiteralTree node, Void p) {
            Object val = node.getValue();

            if (val instanceof Boolean) {
                return (Boolean) val;
            } else {
                return null;
            }
        }

        @Override
        public Boolean visitIf(IfTree node, Void p) {
            Boolean result = scan(node.getCondition(), p);

            if (result != null) {
                if (result) {
                    scan(node.getThenStatement(), null);
                    deadBranches.add(node.getElseStatement());
                } else {
                    scan(node.getElseStatement(), null);
                    deadBranches.add(node.getThenStatement());
                }

                return null;
            }

            Map<VariableElement, State> oldVariable2State = variable2State;
            
            variable2State = new HashMap<VariableElement, Flow.State>(oldVariable2State);

            scan(node.getThenStatement(), null);
            
            if (node.getElseStatement() != null) {
                Map<VariableElement, State> variableStatesAfterThen = new HashMap<VariableElement, Flow.State>(variable2State);

                variable2State = new HashMap<VariableElement, Flow.State>(oldVariable2State);

                scan(node.getElseStatement(), null);

                variable2State = mergeOr(variable2State, variableStatesAfterThen);
            } else {
                variable2State = mergeOr(variable2State, oldVariable2State);
            }
            
            return null;
        }

        @Override
        public Boolean visitBinary(BinaryTree node, Void p) {
            Boolean left = scan(node.getLeftOperand(), p);

            if (left != null && (node.getKind() == Kind.CONDITIONAL_AND || node.getKind() == Kind.CONDITIONAL_OR)) {
                if (left) {
                    if (node.getKind() == Kind.CONDITIONAL_AND) {
                        return scan(node.getRightOperand(), p);
                    } else {
                        return true;
                    }
                } else {
                    if (node.getKind() == Kind.CONDITIONAL_AND) {
                        return false;
                    } else {
                        return scan(node.getRightOperand(), p);
                    }
                }
            }

            Map<VariableElement, State> oldVariable2State = variable2State;

            variable2State = new HashMap<VariableElement, Flow.State>(oldVariable2State);
            
            Boolean right = scan(node.getRightOperand(), p);

            variable2State = mergeOr(variable2State, oldVariable2State);

            if (left == null || right == null) {
                return null;
            }

            switch (node.getKind()) {
                case AND: case CONDITIONAL_AND: return left && right;
                case OR: case CONDITIONAL_OR: return left || right;
                case EQUAL_TO: return left == right;
                case NOT_EQUAL_TO: return left != right;
            }
            
            return null;
        }

        @Override
        public Boolean visitConditionalExpression(ConditionalExpressionTree node, Void p) {
            Boolean result = scan(node.getCondition(), p);

            if (result != null) {
                if (result) {
                    scan(node.getTrueExpression(), null);
                } else {
                    scan(node.getFalseExpression(), null);
                }

                return null;
            }

            Map<VariableElement, State> oldVariable2State = variable2State;

            variable2State = new HashMap<VariableElement, Flow.State>(oldVariable2State);

            scan(node.getTrueExpression(), null);

            if (node.getFalseExpression() != null) {
                Map<VariableElement, State> variableStatesAfterThen = new HashMap<VariableElement, Flow.State>(variable2State);

                variable2State = new HashMap<VariableElement, Flow.State>(oldVariable2State);

                scan(node.getFalseExpression(), null);

                variable2State = mergeOr(variable2State, variableStatesAfterThen);
            } else {
                variable2State = mergeOr(variable2State, oldVariable2State);
            }

            return null;
        }

        @Override
        public Boolean visitIdentifier(IdentifierTree node, Void p) {
            super.visitIdentifier(node, p);

            Element e = info.getTrees().getElement(getCurrentPath());

            if (e != null && LOCAL_VARIABLES.contains(e.getKind())) {
                use2Values.put(node, variable2State.get((VariableElement) e));
            }
            
            return null;
        }

        @Override
        public Boolean visitUnary(UnaryTree node, Void p) {
            Boolean val = super.visitUnary(node, p);

            if (val != null && node.getKind() == Kind.LOGICAL_COMPLEMENT) {
                return !val;
            }

            if (    node.getKind() == Kind.PREFIX_DECREMENT
                 || node.getKind() == Kind.PREFIX_INCREMENT
                 || node.getKind() == Kind.POSTFIX_DECREMENT
                 || node.getKind() == Kind.POSTFIX_INCREMENT) {
                Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getExpression()));

                if (e != null && LOCAL_VARIABLES.contains(e.getKind())) {
                    State prev = variable2State.get((VariableElement) e);

                    use2Values.put(node.getExpression(), prev);
                    variable2State.put((VariableElement) e, State.create(getCurrentPath()));
                }
            }


            return null;
        }

        @Override
        public Boolean visitMethod(MethodTree node, Void p) {
            scan(node.getModifiers(), p);
            scan(node.getReturnType(), p);
            scan(node.getTypeParameters(), p);

            inParameters = true;

            try {
                scan(node.getParameters(), p);
            } finally {
                inParameters = false;
            }

            scan(node.getThrows(), p);
            scan(node.getBody(), p);
            scan(node.getDefaultValue(), p);
            
            return null;
        }

        @Override
        public Boolean visitWhileLoop(WhileLoopTree node, Void p) {
            Boolean condValue = scan(node.getCondition(), null);

            if (condValue != null) {
                if (condValue) {
                    //XXX: handle possibly infinite loop
                } else {
                    //will not run at all, skip:
                    return null;
                }
            }
            
            Map<VariableElement, State> beforeLoop = variable2State;

            variable2State = new HashMap<VariableElement, Flow.State>(beforeLoop);

            scan(node.getStatement(), null);

            beforeLoop = new HashMap<VariableElement, State>(variable2State = mergeOr(beforeLoop, variable2State));

            scan(node.getCondition(), null);
            scan(node.getStatement(), null);
            
            variable2State = beforeLoop;

            return null;
        }

        @Override
        public Boolean visitDoWhileLoop(DoWhileLoopTree node, Void p) {
            Map<VariableElement, State> beforeLoop = variable2State;

            variable2State = new HashMap<VariableElement, Flow.State>(beforeLoop);

            scan(node.getStatement(), null);
            Boolean condValue = scan(node.getCondition(), null);

            if (condValue != null) {
                if (condValue) {
                    //XXX: handle possibly infinite loop
                } else {
                    //will not run more than once, skip:
                    return null;
                }
            }

            beforeLoop = new HashMap<VariableElement, State>(variable2State = mergeOr(beforeLoop, variable2State));

            scan(node.getStatement(), null);
            scan(node.getCondition(), null);

            variable2State = beforeLoop;

            return null;
        }

        @Override
        public Boolean visitForLoop(ForLoopTree node, Void p) {
            scan(node.getInitializer(), null);
            Boolean condValue = scan(node.getCondition(), null);

            if (condValue != null) {
                if (condValue) {
                    //XXX: handle possibly infinite loop
                } else {
                    //will not run at all, skip:
                    return null;
                }
            }

            Map<VariableElement, State> beforeLoop = variable2State;

            variable2State = new HashMap<VariableElement, Flow.State>(beforeLoop);

            scan(node.getStatement(), null);
            scan(node.getUpdate(), null);

            beforeLoop = new HashMap<VariableElement, State>(variable2State = mergeOr(beforeLoop, variable2State));

            scan(node.getInitializer(), null);
            scan(node.getCondition(), null);
            scan(node.getStatement(), null);
            scan(node.getUpdate(), null);

            variable2State = beforeLoop;

            return null;
        }

        public Boolean visitWildcard(WildcardTree node, Void p) {
            super.visitWildcard(node, p);
            return null;
        }

        public Boolean visitUnionType(UnionTypeTree node, Void p) {
            super.visitUnionType(node, p);
            return null;
        }

        public Boolean visitTypeParameter(TypeParameterTree node, Void p) {
            super.visitTypeParameter(node, p);
            return null;
        }

        public Boolean visitTypeCast(TypeCastTree node, Void p) {
            super.visitTypeCast(node, p);
            return null;
        }

        public Boolean visitTry(TryTree node, Void p) {
            super.visitTry(node, p);
            return null;
        }

        public Boolean visitThrow(ThrowTree node, Void p) {
            super.visitThrow(node, p);
            return null;
        }

        public Boolean visitSynchronized(SynchronizedTree node, Void p) {
            super.visitSynchronized(node, p);
            return null;
        }

        public Boolean visitSwitch(SwitchTree node, Void p) {
            super.visitSwitch(node, p);
            return null;
        }

        public Boolean visitReturn(ReturnTree node, Void p) {
            super.visitReturn(node, p);
            return null;
        }

        public Boolean visitPrimitiveType(PrimitiveTypeTree node, Void p) {
            super.visitPrimitiveType(node, p);
            return null;
        }

        public Boolean visitParenthesized(ParenthesizedTree node, Void p) {
            super.visitParenthesized(node, p);
            return null;
        }

        public Boolean visitParameterizedType(ParameterizedTypeTree node, Void p) {
            super.visitParameterizedType(node, p);
            return null;
        }

        public Boolean visitOther(Tree node, Void p) {
            super.visitOther(node, p);
            return null;
        }

        public Boolean visitNewClass(NewClassTree node, Void p) {
            super.visitNewClass(node, p);
            return null;
        }

        public Boolean visitNewArray(NewArrayTree node, Void p) {
            super.visitNewArray(node, p);
            return null;
        }

        public Boolean visitModifiers(ModifiersTree node, Void p) {
            super.visitModifiers(node, p);
            return null;
        }

        public Boolean visitMethodInvocation(MethodInvocationTree node, Void p) {
            super.visitMethodInvocation(node, p);
            return null;
        }

        public Boolean visitLabeledStatement(LabeledStatementTree node, Void p) {
            super.visitLabeledStatement(node, p);
            return null;
        }

        public Boolean visitInstanceOf(InstanceOfTree node, Void p) {
            super.visitInstanceOf(node, p);
            return null;
        }

        public Boolean visitImport(ImportTree node, Void p) {
            super.visitImport(node, p);
            return null;
        }

        public Boolean visitExpressionStatement(ExpressionStatementTree node, Void p) {
            super.visitExpressionStatement(node, p);
            return null;
        }

        public Boolean visitErroneous(ErroneousTree node, Void p) {
            super.visitErroneous(node, p);
            return null;
        }

        public Boolean visitEnhancedForLoop(EnhancedForLoopTree node, Void p) {
            super.visitEnhancedForLoop(node, p);
            return null;
        }

        public Boolean visitEmptyStatement(EmptyStatementTree node, Void p) {
            super.visitEmptyStatement(node, p);
            return null;
        }

        public Boolean visitContinue(ContinueTree node, Void p) {
            super.visitContinue(node, p);
            return null;
        }

        public Boolean visitCompilationUnit(CompilationUnitTree node, Void p) {
            super.visitCompilationUnit(node, p);
            return null;
        }

        public Boolean visitClass(ClassTree node, Void p) {
            super.visitClass(node, p);
            return null;
        }

        public Boolean visitCatch(CatchTree node, Void p) {
            super.visitCatch(node, p);
            return null;
        }

        public Boolean visitCase(CaseTree node, Void p) {
            super.visitCase(node, p);
            return null;
        }

        public Boolean visitBreak(BreakTree node, Void p) {
            super.visitBreak(node, p);
            return null;
        }

        public Boolean visitBlock(BlockTree node, Void p) {
            super.visitBlock(node, p);
            return null;
        }

        public Boolean visitAssert(AssertTree node, Void p) {
            super.visitAssert(node, p);
            return null;
        }

        public Boolean visitArrayType(ArrayTypeTree node, Void p) {
            super.visitArrayType(node, p);
            return null;
        }

        public Boolean visitArrayAccess(ArrayAccessTree node, Void p) {
            super.visitArrayAccess(node, p);
            return null;
        }

        public Boolean visitAnnotation(AnnotationTree node, Void p) {
            super.visitAnnotation(node, p);
            return null;
        }

        private Map<VariableElement, State> mergeOr(Map<VariableElement, State> into, Map<VariableElement, State> what) {
            for (Entry<VariableElement, State> e : what.entrySet()) {
                State stt = into.get(e.getKey());

                if (stt != null) {
                    into.put(e.getKey(), stt.merge(e.getValue()));
                } else {
                    into.put(e.getKey(), e.getValue());
                }
            }

            return into;
        }
    }
    
    private static final Set<ElementKind> LOCAL_VARIABLES = EnumSet.of(ElementKind.EXCEPTION_PARAMETER, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);
    
    static class State {
        private final Set<TreePath> assignments;
        private State(Set<TreePath> assignments) {
            this.assignments = assignments;
        }
        public static State create(TreePath assignment) {
            return new State(Collections.singleton(assignment));
        }

        public State merge(State value) {
            @SuppressWarnings("LocalVariableHidesMemberVariable")
            Set<TreePath> assignments = new HashSet<TreePath>(this.assignments);

            assignments.addAll(value.assignments);

            return new State(assignments);
        }
    }
    
}
