/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.*;
import com.sun.source.util.*;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.Pair;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Ralph Ruijs
 */
public class InlineMethodTransformer extends RefactoringVisitor {

    private Trees trees;
    private MethodTree methodTree;
    private boolean hasParameters;
    private boolean initialized = false;
    private Problem problem;
    private HashMap<Tree, Tree> original2Translated;
    private Deque<Map<Tree, List<StatementTree>>> queue;
    private final TreePathHandle tph;

    public InlineMethodTransformer(TreePathHandle tph) {
        this.tph = tph;
        queue = new LinkedList<Map<Tree, List<StatementTree>>>();
    }

    public Problem getProblem() {
        return problem;
    }

    @Override
    public Tree scan(Tree tree, Element p) {
        if (!initialized) {
            initialized = true;
            trees = workingCopy.getTrees();
            methodTree = (MethodTree) trees.getTree(p);
            hasParameters = methodTree.getParameters().size() > 0;
        }
        return super.scan(tree, p);
    }

    @Override
    public Tree visitClass(ClassTree node, Element p) {
        final TreePath classPath = getCurrentPath();
        ClassTree classTree = node;
        for (Tree member : classTree.getMembers()) {
            TreePath memberPath = new TreePath(classPath, member);
            Element element = workingCopy.getTrees().getElement(memberPath);
            if (p.equals(element)) {
                classTree = make.removeClassMember(classTree, member);
                break;
            }
        }
        if (classPath.getParentPath().getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT) {
            original2Translated.put(node, classTree);
            return super.visitClass(node, p);
        }

        original2Translated = new HashMap<Tree, Tree>();
        Tree value = super.visitClass(node, p);

        classTree = (ClassTree) workingCopy.getTreeUtilities().translate(classTree, original2Translated);
        rewrite(node, classTree);
        return value;
    }

    @Override
    public Tree visitBlock(BlockTree node, Element p) {
        queue.add(new HashMap<Tree, List<StatementTree>>());
        Tree value = super.visitBlock(node, p);
        Map<Tree, List<StatementTree>> original2TranslatedForBlock = queue.pollLast();
        List<StatementTree> newStatementList = new LinkedList<StatementTree>();

        if (!original2TranslatedForBlock.isEmpty()) {
            for (StatementTree statementTree : node.getStatements()) {
                List<StatementTree> stats = original2TranslatedForBlock.get(statementTree);
                if (stats != null) {
                    newStatementList.addAll(stats);
                } else {
                    newStatementList.add(statementTree);
                }
            }
            BlockTree newBlock = make.Block(newStatementList, node.isStatic());
            original2Translated.put(node, newBlock);
        }
        return value;
    }

    @Override
    public Tree visitMethod(MethodTree node, Element p) {
        if (workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            return node;
        }
        return super.visitMethod(node, p);
    }

    @Override
    public Tree visitMethodInvocation(MethodInvocationTree node, Element methodElement) {
        final TreePath methodInvocationPath = getCurrentPath();
        Element el = trees.getElement(methodInvocationPath);
        if (el.getKind() == ElementKind.METHOD && methodElement.equals(el)) {
            ExecutableElement method = (ExecutableElement) el;
            List<StatementTree> newStatementList = new LinkedList<StatementTree>();
            final HashMap<Tree, Tree> original2TranslatedBody = new HashMap<Tree, Tree>();

            final TypeElement bodyEnclosingTypeElement = workingCopy.getElementUtilities().enclosingTypeElement(methodElement);
            TreePath findEnclosingClass = JavaRefactoringUtils.findEnclosingClass(workingCopy, methodInvocationPath, true, true, true, true, true);
            final Element invocationEnclosingTypeElement = workingCopy.getTrees().getElement(findEnclosingClass);

            boolean inSameClass = bodyEnclosingTypeElement.equals(invocationEnclosingTypeElement);
            boolean inStatic = !methodElement.getModifiers().contains(Modifier.STATIC) && isInStaticContext(methodInvocationPath);

            TreePath statementPath = findCorrespondingStatement(methodInvocationPath);
            StatementTree statementTree = (StatementTree) statementPath.getLeaf();

            BlockTree body = methodTree.getBody();

            scanForNameClash(methodInvocationPath, body, methodElement);
            if (problem != null && problem.isFatal()) {
                return node;
            }

            if (hasParameters) {
                replaceParametersWithArguments(original2TranslatedBody, method, node, body);
            }

            body = (BlockTree) workingCopy.getTreeUtilities().translate(body, original2TranslatedBody);

            TreePath methodPath = trees.getPath(methodElement);
            TreePath bodyPath = new TreePath(methodPath, methodTree.getBody());
            Scope scope = workingCopy.getTrees().getScope(methodInvocationPath);

            ExpressionTree methodSelect = node.getMethodSelect();
            if (methodSelect.getKind() == Tree.Kind.MEMBER_SELECT) {
                methodSelect = ((MemberSelectTree) methodSelect).getExpression();
            } else {
                methodSelect = null;
            }

            // Add statements up to the last statement (return)
            for (int i = 0; i < body.getStatements().size() - 1; i++) {
                StatementTree statement = body.getStatements().get(i);
                if (!inSameClass || inStatic) {
                    statement = (StatementTree) fixReferences(statement, new TreePath(bodyPath, statement), method, scope, methodSelect);
                    if (!inSameClass) {
                        statement = GeneratorUtilities.get(workingCopy).importFQNs(statement);
                    }
                }
                newStatementList.add(statement);
            }

            Tree parent = methodInvocationPath.getParentPath().getLeaf();
            Tree grandparent = null;
            final Tree methodInvocation;

            if (parent.getKind() != Tree.Kind.EXPRESSION_STATEMENT) {
                methodInvocation = node;
            } else {
                grandparent = methodInvocationPath.getParentPath().getParentPath().getLeaf();
                switch (grandparent.getKind()) {
                    case FOR_LOOP:
                    case ENHANCED_FOR_LOOP:
                    case WHILE_LOOP:
                    case DO_WHILE_LOOP:
                    case IF:
                        methodInvocation = grandparent;
                        break;
                    default:
                        methodInvocation = parent;
                }
            }

            Tree lastStatement;
            if (body.getStatements().size() > 0) {
                lastStatement = body.getStatements().get(body.getStatements().size() - 1);
            } else {
                lastStatement = null;
            }
            if (lastStatement != null && (!inSameClass || inStatic)) {
                lastStatement = fixReferences(lastStatement, new TreePath(bodyPath, lastStatement), method, scope, methodSelect);
                if (!inSameClass) {
                    lastStatement = GeneratorUtilities.get(workingCopy).importFQNs(lastStatement);
                }
            }
            lastStatement = translateLastStatement(body, parent, grandparent, newStatementList, lastStatement);
            if(lastStatement != null) {
                StatementTree translate = (StatementTree) workingCopy.getTreeUtilities().translate(statementTree, Collections.singletonMap(methodInvocation, lastStatement));
                newStatementList.add(translate);
            }
            
            Element element = workingCopy.getTrees().getElement(statementPath);
            if (element != null && element.getKind() == ElementKind.FIELD) {
                if (newStatementList.size() != 1) {
                    SourcePositions positions = workingCopy.getTrees().getSourcePositions();
                    long startPosition = positions.getStartPosition(workingCopy.getCompilationUnit(), node);
                    long lineNumber = workingCopy.getCompilationUnit().getLineMap().getLineNumber(startPosition);
                    String source = FileUtil.getFileDisplayName(workingCopy.getFileObject()) + ':' + lineNumber;
                    problem = JavaPluginUtils.chainProblems(problem,
                            new Problem(false, NbBundle.getMessage(InlineMethodTransformer.class, "WRN_InlineMethodMultipleLines", source)));
                } else {
                    rewrite(statementTree, newStatementList.get(0));
                }
            } else {
                Map<Tree, List<StatementTree>> original2TranslatedForBlock = queue.getLast();
                original2TranslatedForBlock.put(statementTree, newStatementList);
            }
        }
        return super.visitMethodInvocation(node, methodElement);
    }

    private boolean isInStaticContext(TreePath methodInvocationPath) {
        TreePath parent = methodInvocationPath.getParentPath();
        while (parent != null) {
            if (parent.getLeaf().getKind() == Tree.Kind.METHOD) {
                break;
            }
            parent = parent.getParentPath();
        }
        return parent != null && ((MethodTree) parent.getLeaf()).getModifiers().getFlags().contains(Modifier.STATIC);
    }

    private Tree fixReferences(Tree tree, TreePath treePath, final ExecutableElement method, final Scope scope, final ExpressionTree methodSelect) {
        final HashMap<Tree, Tree> orig2trans = new HashMap<Tree, Tree>();

        final ElementUtilities elementUtilities = workingCopy.getElementUtilities();
        final TypeElement bodyEnclosingTypeElement = elementUtilities.enclosingTypeElement(method);

        TreePathScanner<Void, ExecutableElement> idScan = new TreePathScanner<Void, ExecutableElement>() {
            @Override
            public Void visitIdentifier(IdentifierTree node, ExecutableElement p) {
                TreePath currentPath = getCurrentPath();
                if (currentPath.getParentPath().getLeaf().getKind() == Tree.Kind.MEMBER_SELECT) {
                    return super.visitIdentifier(node, p); // Already checked by visitMemberSelect
                }
                Element el = trees.getElement(currentPath);
                if (el != null) {
                    DeclaredType declaredType = workingCopy.getTypes().getDeclaredType(scope.getEnclosingClass());
                    if (methodSelect != null
                            && el.getEnclosingElement() != method
                            && !workingCopy.getTrees().isAccessible(scope, el, declaredType)) {
                        problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveMembersTransformer.class, "WRN_InlineNotAccessible", el, declaredType)));
                    }
                    TypeElement invocationEnclosingTypeElement = elementUtilities.enclosingTypeElement(el);
                    if (el.getKind() != ElementKind.LOCAL_VARIABLE && bodyEnclosingTypeElement.equals(invocationEnclosingTypeElement)) {
                        if (el.getModifiers().contains(Modifier.STATIC)) {
                            Tree newTree = make.QualIdent(el);
                            orig2trans.put(node, newTree);
                        } else {
                            Tree newTree = make.MemberSelect(methodSelect, el);
                            orig2trans.put(node, newTree);
                        }
                    }
                }
                return super.visitIdentifier(node, p);
            }

            @Override
            public Void visitMethodInvocation(MethodInvocationTree node, ExecutableElement p) {
                TreePath currentPath = getCurrentPath();
                if (currentPath.getParentPath().getLeaf().getKind() == Tree.Kind.MEMBER_SELECT) {
                    return super.visitMethodInvocation(node, p); // Already checked by visitMemberSelect
                }

                Element el = trees.getElement(currentPath);
                if (el != null) {
                    DeclaredType declaredType = workingCopy.getTypes().getDeclaredType(scope.getEnclosingClass());
                    if (methodSelect != null
                            && el.getEnclosingElement() != method
                            && !workingCopy.getTrees().isAccessible(scope, el, declaredType)) {
                        problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveMembersTransformer.class, "WRN_InlineNotAccessible", el, declaredType)));
                    }
                    TypeElement invocationEnclosingTypeElement = elementUtilities.enclosingTypeElement(el);
                    if (bodyEnclosingTypeElement.equals(invocationEnclosingTypeElement)) {
                        if (el.getModifiers().contains(Modifier.STATIC)) {
                            Tree newTree = make.QualIdent(el);
                            orig2trans.put(node.getMethodSelect(), newTree);
                        } else {
                            ExpressionTree methodInvocationSelect = node.getMethodSelect();
                            if (methodInvocationSelect.getKind() == Tree.Kind.MEMBER_SELECT) {
                                ExpressionTree expression = ((MemberSelectTree) methodInvocationSelect).getExpression();
                                String isThis = expression.toString();
                                if (isThis.equals("this") || isThis.endsWith(".this")) { //NOI18N
                                    orig2trans.put(expression, methodSelect);
                                } else {
                                    Tree newTree = make.MemberSelect(methodSelect, el);
                                    orig2trans.put(node, newTree);
                                }
                            } else {
                                Tree newTree = make.MemberSelect(methodSelect, el);
                                orig2trans.put(methodInvocationSelect, newTree);
                            }
                        }
                    }
                }
                return super.visitMethodInvocation(node, p);
            }

            @Override
            public Void visitMemberSelect(MemberSelectTree node, ExecutableElement p) {
                TreePath currentPath = getCurrentPath();
                Element el = trees.getElement(currentPath);
                if (el != null && el.getKind() != ElementKind.PACKAGE) {
                    DeclaredType declaredType = workingCopy.getTypes().getDeclaredType(scope.getEnclosingClass());
                    if (methodSelect != null
                            && el.getEnclosingElement() != method
                            && !workingCopy.getTrees().isAccessible(scope, el, declaredType)) {
                        problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveMembersTransformer.class, "WRN_InlineNotAccessible", el, declaredType)));
                    }
                    TypeElement invocationEnclosingTypeElement = elementUtilities.enclosingTypeElement(el);
                    if (bodyEnclosingTypeElement.equals(invocationEnclosingTypeElement)) {
                        if (el.getModifiers().contains(Modifier.STATIC)) {
                            Tree newTree = make.QualIdent(el);
                            orig2trans.put(node, newTree);
                        } else {
                            ExpressionTree expression = node.getExpression();
                            String isThis = expression.toString();
                            if (isThis.equals("this") || isThis.endsWith(".this")) { //NOI18N
                                if (methodSelect == null) { // We must be in Inner class.
                                    MemberSelectTree memberSelect = make.MemberSelect(workingCopy.getTreeUtilities().parseExpression(bodyEnclosingTypeElement.getSimpleName() + ".this", new SourcePositions[1]), el);
                                    orig2trans.put(node, memberSelect);
                                } else {
                                    orig2trans.put(expression, methodSelect);
                                }
                            } else {
                                if (methodSelect != null) {
                                    Tree newTree = make.MemberSelect(methodSelect, el);
                                    orig2trans.put(node, newTree);
                                }
                            }
                        }
                    }
                }
                return super.visitMemberSelect(node, p);
            }
        };
        idScan.scan(treePath, method);

        Tree result = workingCopy.getTreeUtilities().translate(tree, orig2trans);
        return result;
    }

    private void scanForNameClash(final TreePath methodInvocationPath, BlockTree body, Element p) {
        // Scan the body and look for name clashes
        TreeScanner<Void, ExecutableElement> nameClashScanner = new TreeScanner<Void, ExecutableElement>() {
            @Override
            public Void visitVariable(VariableTree node, ExecutableElement p) {
                TreePath path = trees.getPath(workingCopy.getCompilationUnit(), node);
                if (path != null) {
                    Element variable = trees.getElement(path);
                    if (!(variable.getKind() == ElementKind.PARAMETER && p.getParameters().contains((VariableElement) variable))) {
                        String msg = RefactoringUtils.variableClashes(node.getName().toString(), methodInvocationPath, workingCopy);
                        if (msg != null) {
                            problem = MoveTransformer.createProblem(problem, true, NbBundle.getMessage(InlineRefactoringPlugin.class,
                                    "ERR_InlineMethodNameClash", msg)); // NOI18N
                        }
                    }
                }
                return super.visitVariable(node, p);
            }
        };

        nameClashScanner.scan(body, (ExecutableElement) p);
    }

    private TreePath findCorrespondingStatement(TreePath methodInvocationPath) {
        TreePath statementPath = methodInvocationPath;
        WHILE: while (statementPath != null) {
            if (statementPath.getParentPath() != null) {
                switch (statementPath.getParentPath().getLeaf().getKind()) {
                    case BLOCK:
                    case CLASS:
                        break WHILE;
                }
            }
            statementPath = statementPath.getParentPath();
        }
        return statementPath;
    }

    private void replaceParametersWithArguments(final HashMap<Tree, Tree> original2TranslatedBody, ExecutableElement el, MethodInvocationTree node, BlockTree body) {
        Element resolved = tph.getElementHandle().resolve(workingCopy);
        final CompilationUnitTree compilationUnitTree = workingCopy.getTrees().getPath(resolved).getCompilationUnit();
        TreeScanner<Void, Pair<Element, ExpressionTree>> idScan = new TreeScanner<Void, Pair<Element, ExpressionTree>>() {
            @Override
            public Void visitIdentifier(IdentifierTree node, Pair<Element, ExpressionTree> p) {
                TreePath currentPath = trees.getPath(compilationUnitTree, node);
                Element el = null;
                if(currentPath != null) {
                    el = trees.getElement(currentPath);
                }
                if (p.first.equals(el)) {
                    original2TranslatedBody.put(node, p.second);
                }
                return super.visitIdentifier(node, p);
            }
        };
        for (int i = 0; i < el.getParameters().size(); i++) {
            ExpressionTree argument = node.getArguments().get(i);
            Element element = el.getParameters().get(i);
            final Pair<Element, ExpressionTree> pair = Pair.of(element, argument);
            idScan.scan(body, pair);
        }
    }

    private Tree translateLastStatement(BlockTree body, Tree parent, Tree grandparent, List<StatementTree> newStatementList, Tree lastStatement) {
        Tree result = lastStatement;
        if (parent.getKind() != Tree.Kind.EXPRESSION_STATEMENT) {
            if (result != null) {
                switch (result.getKind()) {
                    case EXPRESSION_STATEMENT:
                        result = ((ExpressionStatementTree) result).getExpression();
                        break;
                    case RETURN:
                        result = ((ReturnTree) result).getExpression();
                        break;
                    default:
                    // TODO: Problem, need an expression, but last statement is not an expression.
                }
            }
        } else {
            if (result != null) {
                if (result.getKind() == Tree.Kind.RETURN) {
                    result = make.ExpressionStatement(((ReturnTree) result).getExpression());
                }
            }
            switch (grandparent.getKind()) {
                case FOR_LOOP: {
                    ForLoopTree forLoopTree = (ForLoopTree) grandparent;
                    StatementTree statement = forLoopTree.getStatement();
                    if (statement == parent) {
                        addResultToStatementList(result, newStatementList);
                        statement = make.Block(newStatementList, false);
                        newStatementList.clear();
                    }
                    List<? extends ExpressionStatementTree> updates = forLoopTree.getUpdate();
                    List<ExpressionStatementTree> newUpdates = new LinkedList<ExpressionStatementTree>();
                    for (ExpressionStatementTree update : updates) {
                        if (update == parent) {
                            addResultToStatementList(result, newStatementList);
                            for (StatementTree statementTree1 : newStatementList) {
                                if (statementTree1.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                                    newUpdates.add((ExpressionStatementTree) statementTree1);
                                } else {
                                    // TODO: Problem forloop list only accepts expression statements.
                                    break;
                                }
                            }
                            newStatementList.clear();
                        } else {
                            newUpdates.add(update);
                        }
                    }
                    List<? extends StatementTree> initializers = forLoopTree.getInitializer();
                    List<StatementTree> newInitializers = new LinkedList<StatementTree>();
                    for (StatementTree initiazer : initializers) {
                        if (initiazer == parent) {
                            addResultToStatementList(result, newStatementList);
                            for (StatementTree statementTree1 : newStatementList) {
                                if (statementTree1.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                                    newInitializers.add((ExpressionStatementTree) statementTree1);
                                } else if (statementTree1.getKind() == Tree.Kind.VARIABLE) {
                                    newInitializers.add((VariableTree) statementTree1);
                                } else {
                                    // TODO: Problem forloop list only accepts expression statements.
                                    break;
                                }
                            }
                            newStatementList.clear();
                        } else {
                            newInitializers.add(initiazer);
                        }
                    }
                    result = make.ForLoop(newInitializers, forLoopTree.getCondition(), newUpdates, statement);
                }
                break;
                case ENHANCED_FOR_LOOP: {
                    EnhancedForLoopTree enhancedForLoopTree = (EnhancedForLoopTree) grandparent;
                    StatementTree statement = enhancedForLoopTree.getStatement();
                    if (statement == parent) {
                        addResultToStatementList(result, newStatementList);
                        statement = make.Block(newStatementList, false);
                        newStatementList.clear();
                    }
                    result = make.EnhancedForLoop(enhancedForLoopTree.getVariable(), enhancedForLoopTree.getExpression(), statement);
                }
                break;
                case WHILE_LOOP: {
                    WhileLoopTree whileLoopTree = (WhileLoopTree) grandparent;
                    StatementTree statement = whileLoopTree.getStatement();
                    if (statement == parent) {
                        addResultToStatementList(result, newStatementList);
                        statement = make.Block(newStatementList, false);
                        newStatementList.clear();
                    }
                    result = make.WhileLoop(whileLoopTree.getCondition(), statement);
                }
                break;
                case DO_WHILE_LOOP: {
                    DoWhileLoopTree doWhileLoopTree = (DoWhileLoopTree) grandparent;
                    StatementTree statement = doWhileLoopTree.getStatement();
                    if (statement == parent) {
                        addResultToStatementList(result, newStatementList);
                        statement = make.Block(newStatementList, false);
                        newStatementList.clear();
                    }
                    result = make.DoWhileLoop(doWhileLoopTree.getCondition(), statement);
                }
                break;
                case IF: {
                    IfTree ifTree = (IfTree) grandparent;
                    StatementTree thenStatement = ifTree.getThenStatement();
                    if (thenStatement == parent) {
                        addResultToStatementList(result, newStatementList);
                        thenStatement = make.Block(newStatementList, false);
                        newStatementList.clear();
                    }
                    StatementTree elseStatement = ifTree.getElseStatement();
                    if (elseStatement == parent) {
                        addResultToStatementList(result, newStatementList);
                        elseStatement = make.Block(newStatementList, false);
                        newStatementList.clear();
                    }
                    result = make.If(ifTree.getCondition(), thenStatement, elseStatement);
                }
                break;
            }
        }
        return result;
    }

    private void addResultToStatementList(Tree result, List<StatementTree> newStatementList) {
        if (result != null) {
            newStatementList.add((StatementTree) result);
        }
    }
}
