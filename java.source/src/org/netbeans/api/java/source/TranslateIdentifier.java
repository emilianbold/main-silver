/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.api.java.source;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.SourcePositions;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.code.Symbol;

import com.sun.tools.javac.tree.JCTree.JCTypeAnnotation;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.builder.CommentSetImpl;
import static org.netbeans.modules.java.source.save.PositionEstimator.*;
import org.netbeans.modules.java.source.save.PositionEstimator;
import org.netbeans.modules.java.source.query.CommentHandler;
import org.netbeans.modules.java.source.query.CommentSet;

/**
 * Replaces identifiers representing all used types with the new ones - imports
 * for them will be solved throughout new commit phase.
 * 
 * This is provided because of refactoring, which wants to take tree from
 * one compilation unit and add it to another one and wants to have all
 * types resolved.
 *
 * @author Pavel Flaska
 */
class TranslateIdentifier implements TreeVisitor<Tree, Boolean> {
    
    private final CompilationInfo info;
    private final TreeMaker make;
    private final CompilationUnitTree unit;
    private final boolean copyComments;
    private final boolean resolveImports;
    private final TokenSequence<JavaTokenId> seq;
    private final CommentHandlerService commentService;
    private final SourcePositions positions;
    private int tokenIndexAlreadyAdded = -1;
    private Element rootElement;
    

    public TranslateIdentifier(final CompilationInfo info,
            final boolean copyComments,
            final boolean resolveImports,
            final TokenSequence<JavaTokenId> seq) {
        this(info, copyComments, resolveImports, seq, info.getCompilationUnit());
    }

    public TranslateIdentifier(final CompilationInfo info,
            final boolean copyComments,
            final boolean resolveImports,
            final TokenSequence<JavaTokenId> seq,
            final CompilationUnitTree cut) {
        this(info, copyComments, resolveImports, seq, cut, info.getTrees().getSourcePositions());
    }

    public TranslateIdentifier(final CompilationInfo info,
            final boolean copyComments,
            final boolean resolveImports,
            final TokenSequence<JavaTokenId> seq,
            final SourcePositions positions) {
        this(info, copyComments, resolveImports, seq, info.getCompilationUnit(), positions);
    }

    private TranslateIdentifier(final CompilationInfo info,
            final boolean copyComments, 
            final boolean resolveImports,
            final TokenSequence<JavaTokenId> seq,
            final CompilationUnitTree cut,
            final SourcePositions positions) {
        this.info = info;
        this.make = info instanceof WorkingCopy ? ((WorkingCopy) info).getTreeMaker() : null;
        this.unit = cut;
        this.seq = seq;
        this.copyComments = copyComments;
        this.resolveImports = resolveImports;
        this.commentService = CommentHandlerService.instance(info.impl.getJavacTask().getContext());
        this.positions = positions;
    }

    public Tree visitAnnotation(AnnotationTree node, Boolean p) {
        Tree annotationType = translateTree(node.getAnnotationType());
        List<? extends ExpressionTree> arguments = translateTree(node.getArguments());
        
        if (make == null) return node;
        
        if (annotationType != node.getAnnotationType() ||
            arguments != node.getArguments()) 
        {
            node = make.Annotation(annotationType, arguments);
        }
        return node;
    }

    public Tree visitMethodInvocation(MethodInvocationTree node, Boolean p) {
        List<? extends ExpressionTree> arguments = translateTree(node.getArguments());
        ExpressionTree methodSelect = (ExpressionTree) translateTree(node.getMethodSelect());
        List<? extends Tree> typeArguments = translateTree(node.getTypeArguments());
        
        if (make == null) return node;
        
        if (arguments != node.getArguments() ||
            methodSelect != node.getMethodSelect() ||
            typeArguments != node.getTypeArguments())
        {
            node = make.MethodInvocation((List<? extends ExpressionTree>) typeArguments, methodSelect, arguments);
        }
        return node;
    }

    public Tree visitAssert(AssertTree node, Boolean p) {
        ExpressionTree condition = (ExpressionTree) translateTree(node.getCondition());
        ExpressionTree detail = (ExpressionTree) translateTree(node.getDetail());
        
        if (make == null) return node;
        
        if (condition != node.getCondition() ||
            detail != node.getDetail())
        {
            node = make.Assert(condition, detail);
        }
        return node;
    }

    public Tree visitAssignment(AssignmentTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        ExpressionTree variable = (ExpressionTree) translateTree(node.getVariable());
        
        if (make == null) return node;
        
        if (expression != node.getExpression() ||
            variable != node.getVariable()) 
        {
            node = make.Assignment(variable, expression);
        }
        return node;
    }

    public Tree visitCompoundAssignment(CompoundAssignmentTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        ExpressionTree variable = (ExpressionTree) translateTree(node.getVariable());
        
        if (make == null) return node;
        
        if (expression != node.getExpression() ||
            variable != node.getVariable()) 
        {
            node = make.CompoundAssignment(node.getKind(), variable, expression);
        }
        return node;
    }

    public Tree visitBinary(BinaryTree node, Boolean p) {
        ExpressionTree leftOperand = (ExpressionTree) translateTree(node.getLeftOperand());
        ExpressionTree rightOperand = (ExpressionTree) translateTree(node.getRightOperand());
        
        if (make == null) return node;
        
        if (leftOperand != node.getLeftOperand() ||
            rightOperand != node.getRightOperand())
        {
            node = make.Binary(node.getKind(), leftOperand, rightOperand);
        }   
        return node;
    }

    public Tree visitBlock(BlockTree node, Boolean p) {
        List<? extends StatementTree> statements = translateTree(node.getStatements());
        
        if (make == null) return node;
        
        if (statements != node.getStatements()) {
            node = make.Block(statements, node.isStatic());
        }
        return node;
    }

    public Tree visitBreak(BreakTree node, Boolean p) {
        return node;
    }

    public Tree visitCase(CaseTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression(), true);
        List<? extends StatementTree> statements = translateTree(node.getStatements());
        
        if (make == null) return node;
        
        if (expression != node.getExpression() ||
            statements != node.getStatements())
        {
            node = make.Case(expression, statements);
        }
        return node;
    }

    public Tree visitCatch(CatchTree node, Boolean p) {
        BlockTree block = (BlockTree) translateTree(node.getBlock());
        VariableTree parameter = (VariableTree) translateTree(node.getParameter());
        
        if (make == null) return node;
        
        if (block != node.getBlock() ||
            parameter != node.getParameter()) 
        {
            node = make.Catch(parameter, block);
        }
        return node;
    }

    public Tree visitClass(ClassTree node, Boolean p) {
        Tree extendsClause = translateTree(node.getExtendsClause());
        List<? extends Tree> implementsClause = translateTree(node.getImplementsClause());
        List<? extends Tree> members = translateTree(node.getMembers());
        ModifiersTree modifiers = (ModifiersTree) translateTree(node.getModifiers());
        List<? extends TypeParameterTree> typeParameters = translateTree(node.getTypeParameters());
        
        if (make == null) return node;
        
        if (extendsClause != node.getExtendsClause() ||
            implementsClause != node.getImplementsClause() ||
            members != node.getMembers() ||
            modifiers != node.getModifiers() ||
            typeParameters != node.getTypeParameters())
        {
            node = make.Class(modifiers, node.getSimpleName(), typeParameters, extendsClause, implementsClause, members);
        }
        return node;
    }

    public Tree visitConditionalExpression(ConditionalExpressionTree node, Boolean p) {
        ExpressionTree condition = (ExpressionTree) translateTree(node.getCondition());
        ExpressionTree falseExpression = (ExpressionTree) translateTree(node.getFalseExpression());
        ExpressionTree trueExpression = (ExpressionTree) translateTree(node.getTrueExpression());
        
        if (make == null) return node;
        
        if (condition != node.getCondition() ||
            falseExpression != node.getFalseExpression() ||
            trueExpression != node.getTrueExpression())
        {
            node = make.ConditionalExpression(condition, trueExpression, falseExpression);
        }
        return node;
    }

    public Tree visitContinue(ContinueTree node, Boolean p) {
        return node;
    }

    public Tree visitDoWhileLoop(DoWhileLoopTree node, Boolean p) {
        StatementTree statement = (StatementTree) translateTree(node.getStatement());
        ExpressionTree condition = (ExpressionTree) translateTree(node.getCondition());
        
        if (make == null) return node;
        
        if (condition != node.getCondition() || statement != node.getStatement()) {
            node = make.DoWhileLoop(condition, statement);
        }
        return node;
    }

    public Tree visitErroneous(ErroneousTree node, Boolean p) {
        List<? extends Tree> errorTrees = translateTree(node.getErrorTrees());
        
        if (make == null) return node;
        
        if (errorTrees != node.getErrorTrees()) {
            node = make.Erroneous(errorTrees);
        }
        return node;
    }

    public Tree visitExpressionStatement(ExpressionStatementTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        
        if (make == null) return node;
        
        if (expression != node.getExpression()) {
            node = make.ExpressionStatement(expression);
        }
        return node;
    }

    public Tree visitEnhancedForLoop(EnhancedForLoopTree node, Boolean p) {
        StatementTree statement = (StatementTree) translateTree(node.getStatement());
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        VariableTree variable = (VariableTree) translateTree(node.getVariable());
        
        if (make == null) return node;
        
        if (statement != node.getStatement() ||
            expression != node.getExpression() ||
            variable != node.getVariable()) 
        {
            node = make.EnhancedForLoop(variable, expression, statement);
        }
        return node;
    }

    public Tree visitForLoop(ForLoopTree node, Boolean p) {
        StatementTree statement = (StatementTree) translateTree(node.getStatement());
        ExpressionTree condition = (ExpressionTree) translateTree(node.getCondition());
        List<? extends StatementTree> initializer = translateTree(node.getInitializer());
        List<? extends ExpressionStatementTree> update = translateTree(node.getUpdate());
        
        if (make == null) return node;
        
        if (statement != node.getStatement() ||
            condition != node.getCondition() ||
            initializer != node.getInitializer() ||
            update != node.getUpdate()) 
        {
            node = make.ForLoop(initializer, condition, update, statement);
        }
        return node;
    }

    public Tree visitIdentifier(IdentifierTree node, Boolean p) {
        if (!resolveImports) return node;
        if (make == null) return node;
                
        TreePath path = info.getTrees().getPath(unit, node);
        Element element;
        if (path == null) {
            element = ((JCIdent) node).sym;
        } else {
            element = info.getTrees().getElement(path);
        }
        if (element != null) {
            // solve the imports only when declared type!!!
            if (element.getKind().isClass() || element.getKind().isInterface()
                    || (element.getKind().isField() && ((Symbol) element).isStatic())) {
                TreePath elmPath = info.getTrees().getPath(element);
                boolean en = p == Boolean.TRUE && element.getKind() == ElementKind.ENUM_CONSTANT;
                if ((path == null && element == rootElement)
                        || (path != null && elmPath != null && path.getCompilationUnit().getSourceFile() == elmPath.getCompilationUnit().getSourceFile())
                        || en) {
                    return make.Identifier(element.getSimpleName());
                } else {
                    return make.QualIdent(element);
                }
            } 
        }
        return node;
    }
    
    public Tree visitIf(IfTree node, Boolean p) {
        ExpressionTree condition = (ExpressionTree) translateTree(node.getCondition());
        StatementTree elseStatement = (StatementTree) translateTree(node.getElseStatement());
        StatementTree thenStatement = (StatementTree) translateTree(node.getThenStatement());
        
        if (make == null) return node;
        
        if (condition != node.getCondition() ||
            elseStatement != node.getElseStatement() ||
            thenStatement != node.getThenStatement())
        {
            node = make.If(condition, thenStatement, elseStatement);
        }
        return node;
    }

    public Tree visitImport(ImportTree node, Boolean p) {
        return node;
    }

    public Tree visitArrayAccess(ArrayAccessTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        ExpressionTree index = (ExpressionTree) translateTree(node.getIndex());
        
        if (make == null) return node;
        
        if (expression != node.getExpression() ||
            index != node.getIndex())
        {
            node = make.ArrayAccess(expression, index);
        }
        return node;
    }

    public Tree visitLabeledStatement(LabeledStatementTree node, Boolean p) {
        StatementTree statement = (StatementTree) translateTree(node.getStatement());
        
        if (make == null) return node;
        
        if (statement != node.getStatement()) {
            node = make.LabeledStatement(node.getLabel(), statement);
        }
        return node;
    }

    public Tree visitLiteral(LiteralTree node, Boolean p) {
        return node;
    }

    public Tree visitMethod(MethodTree node, Boolean p) {
        BlockTree body = (BlockTree) translateTree(node.getBody());
        Tree defaultValue = translateTree(node.getDefaultValue());
        List<? extends VariableTree> parameters = translateTree(node.getParameters());
        ModifiersTree modifiers = (ModifiersTree) translateTree(node.getModifiers());
        Tree returnType = translateTree(node.getReturnType());
        List<? extends ExpressionTree> aThrows = translateTree(node.getThrows());
        List<? extends TypeParameterTree> typeParameters = translateTree(node.getTypeParameters());
        
        if (make == null) return node;
        
        if (body != node.getBody() ||
            defaultValue != node.getDefaultValue() ||
            parameters != node.getParameters() ||
            modifiers != node.getModifiers() ||
            returnType != node.getReturnType() ||
            aThrows != node.getThrows() ||
            typeParameters != node.getTypeParameters()) 
        {
            node = make.Method(modifiers,
                    node.getName(),
                    returnType,
                    typeParameters,
                    parameters,
                    aThrows,
                    body,
                    (ExpressionTree) defaultValue
            );
        }
        return node;
    }

    public Tree visitModifiers(ModifiersTree node, Boolean p) {
        List<? extends AnnotationTree> annotations = translateTree(node.getAnnotations());
        
        if (make == null) return node;
        
        if (annotations != node.getAnnotations()) {
            node = make.Modifiers(node.getFlags(), annotations);
        }
        return node;
    }

    public Tree visitNewArray(NewArrayTree node, Boolean p) {
        List<? extends ExpressionTree> initializers = translateTree(node.getInitializers());
        List<? extends ExpressionTree> dimensions = translateTree(node.getDimensions());
        Tree type = translateTree(node.getType());
        
        if (make == null) return node;
        
        if (initializers != node.getInitializers() ||
            dimensions != node.getDimensions() ||
            type != node.getType()) 
        {
            node = make.NewArray(type, dimensions, initializers);
        }
        return node;
    }

    public Tree visitNewClass(NewClassTree node, Boolean p) {
        List<? extends ExpressionTree> arguments = translateTree(node.getArguments());
        ClassTree classBody = (ClassTree) translateTree(node.getClassBody());
        ExpressionTree enclosingExpression = (ExpressionTree) translateTree(node.getEnclosingExpression());
        ExpressionTree identifier = (ExpressionTree) translateTree(node.getIdentifier());
        List<? extends Tree> typeArguments = translateTree(node.getTypeArguments());
        
        if (make == null) return node;
        
        if (arguments != node.getArguments() ||
            classBody != node.getClassBody() ||
            enclosingExpression != node.getEnclosingExpression() ||
            identifier != node.getIdentifier() ||
            typeArguments != node.getTypeArguments())
        {
            node = make.NewClass(enclosingExpression, (List<? extends ExpressionTree>) typeArguments, identifier, arguments, classBody);
        }
        return node;
    }

    public Tree visitParenthesized(ParenthesizedTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        
        if (make == null) return node;
        
        if (expression != node.getExpression()) {
            node = make.Parenthesized(expression);
        }
        return node;
    }

    public Tree visitReturn(ReturnTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        
        if (make == null) return node;
        
        if (expression != node.getExpression()) {
            node = make.Return(expression);
        }
        return node;
    }

    public Tree visitMemberSelect(MemberSelectTree node, Boolean p) {
        if (make == null) return node;
        
        TypeElement e = info.getElements().getTypeElement(node.toString());
        if (e != null) {
            return make.QualIdent(e);
        } else {
            ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());

            if (expression != node.getExpression()) {
                node = make.MemberSelect(expression, node.getIdentifier());
            }
            return node;
        }
    }

    public Tree visitEmptyStatement(EmptyStatementTree node, Boolean p) {
        return node;
    }

    public Tree visitSwitch(SwitchTree node, Boolean p) {
        List<? extends CaseTree> cases = translateTree(node.getCases());
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        
        if (make == null) return node;
        
        if (cases != node.getCases() ||
            expression != node.getExpression()) 
        {
            node = make.Switch(expression, cases);
        }
        return node;
    }

    public Tree visitSynchronized(SynchronizedTree node, Boolean p) {
        BlockTree block = (BlockTree) translateTree(node.getBlock());
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        
        if (make == null) return node;
        
        if (block != node.getBlock() ||
            expression != node.getExpression())
        {
            node = make.Synchronized(expression, block);
        }
        return node;
    }

    public Tree visitThrow(ThrowTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        
        if (make == null) return node;
        
        if (expression != node.getExpression()) {
            node = make.Throw(expression);
        }
        return node;
    }

    public Tree visitCompilationUnit(CompilationUnitTree node, Boolean p) {
        List<? extends Tree> typeDecls = translateTree(node.getTypeDecls());
        
        if (make == null) return node;
        
        if (typeDecls != node.getTypeDecls()) {
            node = make.CompilationUnit(
                    node.getPackageName(),
                    node.getImports(),
                    typeDecls,
                    node.getSourceFile()
            );                   
        }
        return node;
    }

    public Tree visitTry(TryTree node, Boolean p) {
        BlockTree block = (BlockTree) translateTree(node.getBlock());
        List<? extends CatchTree> catches = translateTree(node.getCatches());
        BlockTree finallyBlock = (BlockTree) translateTree(node.getFinallyBlock());
        
        if (make == null) return node;
        
        if (block != node.getBlock() ||
            catches != node.getCatches() ||
            finallyBlock != node.getFinallyBlock())
        {
            node = make.Try(block, catches, finallyBlock);
        }
        return node;
    }

    public Tree visitParameterizedType(ParameterizedTypeTree node, Boolean p) {
        Tree type = translateTree(node.getType());
        List<? extends Tree> typeArguments = translateTree(node.getTypeArguments());
        
        if (make == null) return node;
        
        if (type != node.getType() ||
            typeArguments != node.getTypeArguments())
        {
            node = make.ParameterizedType(type, typeArguments);
        }
        return node;
    }

    public Tree visitAnnotatedType(AnnotatedTypeTree node, Boolean p) {
        List<? extends AnnotationTree> annotations = translateTree(node.getAnnotations());
        Tree type = translateTree(node.getUnderlyingType());

        if (make == null) return node;

        if (type != node.getUnderlyingType() ||
            annotations != node.getAnnotations())
        {
            List<AnnotationTree> typeAnnotations = new LinkedList<AnnotationTree>();

            for (AnnotationTree at : annotations) {
                if (!(at instanceof JCTypeAnnotation)) {//XXX
                    at = JavaSourceAccessor.getINSTANCE().makeTypeAnnotation(make, at);
                }
                typeAnnotations.add(at);
            }

            node = JavaSourceAccessor.getINSTANCE().makeAnnotatedType(make, typeAnnotations, node);
        }
        return node;
    }

    public Tree visitArrayType(ArrayTypeTree node, Boolean p) {
        Tree type = translateTree(node.getType());
        
        if (make == null) return node;
        
        if (type != node.getType()) {
            node = make.ArrayType(type);
        }
        return node;
    }

    public Tree visitTypeCast(TypeCastTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        Tree type = translateTree(node.getType());
        
        if (make == null) return node;
        
        if (expression != node.getExpression() ||
            type != node.getType()) 
        {
            node = make.TypeCast(type, expression);
        }
        return node;
    }

    public Tree visitPrimitiveType(PrimitiveTypeTree node, Boolean p) {
        return node;
    }

    public Tree visitTypeParameter(TypeParameterTree node, Boolean p) {
        List<? extends Tree> bounds = translateTree(node.getBounds());
        
        if (make == null) return node;
        
        if (bounds != node.getBounds()) {
            node = make.TypeParameter(node.getName(), (List<? extends ExpressionTree>) bounds);
        }
        return node;
    }

    public Tree visitInstanceOf(InstanceOfTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        Tree type = translateTree(node.getType());
        
        if (make == null) return node;
        
        if (expression != node.getExpression() ||
            type != node.getType())
        {
            node = make.InstanceOf(expression, type);
        }
        return node;
    }

    public Tree visitUnary(UnaryTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        
        if (make == null) return node;
        
        if (expression != node.getExpression()) {
            node = make.Unary(node.getKind(), expression);
        }
        return node;
    }

    public Tree visitVariable(VariableTree node, Boolean p) {
        ModifiersTree modifiers = (ModifiersTree) translateTree(node.getModifiers());
        Tree type = translateTree(node.getType());
        ExpressionTree initializer = (ExpressionTree) translateTree(node.getInitializer());

        if (make == null) return node;
        
        if (modifiers != node.getModifiers() || type != node.getType() || initializer != node.getInitializer()) {
            node = make.Variable(modifiers, node.getName(), type, initializer);
        }
        return node;
    }

    public Tree visitWhileLoop(WhileLoopTree node, Boolean p) {
        StatementTree statement = (StatementTree) translateTree(node.getStatement());
        ExpressionTree condition = (ExpressionTree) translateTree(node.getCondition());
        
        if (make == null) return node;
        
        if (condition != node.getCondition() || statement != node.getStatement()) {
            node = make.WhileLoop(condition, statement);
        }
        return node;
    }

    public Tree visitWildcard(WildcardTree node, Boolean p) {
        Tree tree = translateTree(node.getBound());
        
        if (make == null) return node;
        
	if (tree != node.getBound()) {
	    node = make.Wildcard(node.getKind(), tree);
        }
        return node;
    }

    public Tree visitOther(Tree node, Boolean p) {
        return node;
    }

    ////////////////////////////////////////////////////////////////////////////
    public Tree translate(Tree tree) {
        if (tree == null) {
            return null;
        } else {
            if (copyComments) {
                mapComments2(tree, true);
            }
            TreePath path = info.getTrees().getPath(unit, tree);
            if (path == null) {
                if (tree instanceof JCClassDecl) {
                    rootElement = ((JCClassDecl) tree).sym;
                }
            } else {
                rootElement = info.getTrees().getElement(path);
            }
            Tree res = tree.accept(this, null);

            if (copyComments) {
                mapComments2(tree, false);
            }

            return res;
        }
    }
    
    private <T extends Tree> List<T> translateTree(List<T> trees) {
        if (trees == null || trees.isEmpty()) {
            return trees;
        }
        List<T> newTrees = new ArrayList<T>();
        boolean changed = false;
        for (T t : trees) {
            T newT = (T) translateTree(t);
            if (newT != t) {
                changed = true;
            }
            if (newT != null) {
                newTrees.add(newT);
            }
        }
        return changed ? newTrees : trees;
    }

    private Tree translateTree(Tree tree) {
        return translateTree(tree, null);
    }

    private Tree translateTree(Tree tree, Boolean p) {
        if (tree == null) {
            return null;
        } else {
            //XXX:
            if (copyComments && info.getTreeUtilities().isSynthetic(new TreePath(new TreePath(info.getCompilationUnit()), tree)))
                return tree;
            if (copyComments) {
                mapComments2(tree, true);
            }
            Tree newTree = tree.accept(this, p);
            if (copyComments) {
                mapComments2(tree, false);
            }
            // #144209
            commentService.copyComments(tree, newTree);
            return newTree;
        }
    }
        
    private void mapComments2(Tree tree, boolean preceding) {
        if (((JCTree) tree).pos <= 0) {
            return;
        }
        collect(tree, preceding);
    }
    
    /*
        Implementation of new gathering algorithm based on comment weighting by natural (my) aligning of comments to statements.
     */
    
    private static Logger log = Logger.getLogger(TranslateIdentifier.class.getName());
    
    private void collect(Tree tree, boolean preceding) {
        if (isEvil(tree)) {
            return;
        }
        if (preceding) {
            int pos = findInterestingStart((JCTree) tree);
            seq.move(pos);
            lookForPreceedings(seq, tree);
            if (tree instanceof BlockTree) {
                BlockTree blockTree = (BlockTree) tree;
                if (blockTree.getStatements().isEmpty()) {
                    lookWithinEmptyBlock(seq, blockTree);
                }
            }
        } else {
            lookForInline(seq, tree);
            lookForTrailing(seq, tree);
        }

        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "T: " + tree + "\nC: " + commentService.getComments(tree));
        }
    }

    private void lookForInline(TokenSequence<JavaTokenId> seq, Tree tree) {
        seq.move((int) positions.getEndPosition(unit, tree));
        CommentsCollection result = new CommentsCollection();
        while (seq.moveNext()) {
            if (seq.index() <= tokenIndexAlreadyAdded) continue;
            if (seq.token().id() == JavaTokenId.WHITESPACE) {
                if (numberOfNL(seq.token()) > 0) {
                    break;
                }
            } else if (isComment(seq.token().id())) {
                result.add(seq.token());
                tokenIndexAlreadyAdded = seq.index();
                if (seq.token().id() == JavaTokenId.LINE_COMMENT) {
                    break;
                }
            } else {
                break;
            }
        }
        if (!result.isEmpty()) {
            CommentSet.RelativePosition position = CommentSet.RelativePosition.INLINE;
            attachComments(tree, result, position);
        }
    }

    private void attachComments(Tree tree, CommentsCollection result, CommentSet.RelativePosition position) {
        CommentSetImpl cs = commentService.getComments(tree);
        for (Token<JavaTokenId> token : result) {
            attachComment(position, cs, token);
        }
    }

    private boolean isEvil(Tree tree) {
        Tree.Kind kind = tree.getKind();
        switch (kind) {
            case MODIFIERS:
            case COMPILATION_UNIT:
            case PRIMITIVE_TYPE:
                return true;
            default: return false;
        }
    }

    private void lookForTrailing(TokenSequence<JavaTokenId> seq, Tree tree) {
        //TODO: [RKo] This does not work correctly... need improvemetns.
        seq.move((int) positions.getEndPosition(unit, tree));
        List<TrailingCommentsDataHolder> comments = new LinkedList<TrailingCommentsDataHolder>();
        int maxLines = 0;
        int newlines = 0;
        while (seq.moveNext()) {
            if (seq.index() <= tokenIndexAlreadyAdded) continue;
            Token<JavaTokenId> t = seq.token();
            if (t.id() == JavaTokenId.WHITESPACE) {
                newlines += numberOfNL(t);
            } else if (isComment(t.id())) {
                comments.add(new TrailingCommentsDataHolder(newlines, t, seq.index()));
                maxLines = Math.max(maxLines, newlines);
                if (t.id() == JavaTokenId.LINE_COMMENT) {
                    newlines = 1;
                } else {
                    newlines = 0;
                }

            } else {
                if (t.id() == JavaTokenId.RBRACE) maxLines = Integer.MAX_VALUE;
                break;
            }

        }

        int index = seq.index() - 1;

        maxLines = Math.max(maxLines, newlines);

        for (TrailingCommentsDataHolder h : comments) {
            if (h.newlines < maxLines) {
                attachComments(Collections.singleton(h.comment), tree, commentService, CommentSet.RelativePosition.TRAILING);
            } else {
                index = h.index;
                break;
            }
        }
        
        tokenIndexAlreadyAdded = index;
    }

    private static final class TrailingCommentsDataHolder {
        private final int newlines;
        private final Token<JavaTokenId> comment;
        private final int index;
        public TrailingCommentsDataHolder(int newlines, Token<JavaTokenId> comment, int index) {
            this.newlines = newlines;
            this.comment = comment;
            this.index = index;
        }
    }

    private void lookWithinEmptyBlock(TokenSequence<JavaTokenId> seq, BlockTree tree) {
        // moving into opening brace.
        if (moveTo(seq, JavaTokenId.LBRACE, true)) {
            if (seq.moveNext()) {
                CommentsCollection cc = getCommentsCollection(seq, Integer.MAX_VALUE);
                attachComments(tree, cc, CommentSet.RelativePosition.INNER);
            }
        } else {
            int end = (int) positions.getEndPosition(unit, tree);
            seq.move(end); seq.moveNext();
        }
    }

    /**
     * Moves <code>seq</code> to first occurence of specified <code>toToken</code> if specified direction.
     * @param seq sequence of tokens
     * @param toToken token to stop on.
     * @param forward move forward if true, backward otherwise
     * @return true if token has been reached.
     */
    private boolean moveTo(TokenSequence<JavaTokenId> seq, JavaTokenId toToken, boolean forward) {
        do {
            if (toToken == seq.token().id()) {
                return true;
            } 
        } while (forward ? seq.moveNext() : seq.movePrevious());
        return false;
    }

    private void lookForPreceedings(TokenSequence<JavaTokenId> seq, Tree tree) {
        int reset = ((JCTree) tree).pos;
        CommentsCollection cc = null;
        while (seq.moveNext() && seq.offset() < reset) {
            JavaTokenId id = seq.token().id();
            if (isComment(id)) {
                if (cc == null) {
                    cc = getCommentsCollection(seq, Integer.MAX_VALUE);
                } else {
                    cc.merge(getCommentsCollection(seq, Integer.MAX_VALUE));
                }
            }
        }
        attachComments(cc, tree, commentService, CommentSet.RelativePosition.PRECEDING);
        seq.move(reset);
        seq.moveNext();
        tokenIndexAlreadyAdded = seq.index();
    }

    /**
     * Looking for position where to start looking up for preceeding commnets.
     * @param tree tree to examine.
     * @return position where to start 
     */
    private int findInterestingStart(JCTree tree) {
        int pos = (int) positions.getStartPosition(unit, tree);
        if (pos <= 0) return 0;
        seq.move(pos);
        while (seq.movePrevious() && tokenIndexAlreadyAdded < seq.index()) {
            switch (seq.token().id()) {
                case WHITESPACE:
                case LINE_COMMENT:
                case JAVADOC_COMMENT:
                case BLOCK_COMMENT:
                    continue;
                case LBRACE:
                    /*
                        we are reaching parent tree element. This tree has no siblings or is first child. We have no 
                        interest in number of NL before this kind of comments. This comments are always considered 
                        as preceeding to tree.
                    */
                    return seq.offset() + seq.token().length();
                default:
                    return seq.offset() + seq.token().length();
            }
        }
        return seq.offset(); 
    }

    private void consumeWS(TokenSequence<JavaTokenId> seq, boolean forward) {
        while (forward ? seq.moveNext() : seq.movePrevious()) {
            switch (seq.token().id()) {
                case WHITESPACE:
                    continue;
                default: return;
            }
        }
    }

    @SuppressWarnings({"MethodWithMultipleLoops"})
    private int adjustByComments(int pos, CommentSetImpl comments) {
        List<Comment> cl = comments.getComments(CommentSet.RelativePosition.INLINE);
        if (!cl.isEmpty()) {
            for (Comment comment : cl) {
                pos = Math.max(pos, comment.endPos());
            }
        }
        cl = comments.getComments(CommentSet.RelativePosition.TRAILING);
        if (!cl.isEmpty()) {
            for (Comment comment : cl) {
                pos = Math.max(pos, comment.endPos());
            }
        }
        return pos;
    }

    private void skipEvil(TokenSequence<JavaTokenId> ts) {
        do {
            JavaTokenId id = ts.token().id();
            switch (id) {
                case PUBLIC:
                case PRIVATE:
                case PROTECTED:
                case ABSTRACT:
                case FINAL:
                case STATIC:
                case VOID:
                case VOLATILE:
                case NATIVE:
                case STRICTFP:
                case WHITESPACE:
                case INT:
                case BOOLEAN:
                case DOUBLE:
                case FLOAT:
                case BYTE:
                case CHAR:
                case SHORT:
                case CONST:
                case LONG:
                    continue;
                default:
                    return;
            }
        } while (ts.moveNext());
    }

    private double belongsTo(int startPos, int endPos, TokenSequence<JavaTokenId> ts) {
        int index = ts.index();
        double result = getForwardWeight(endPos, ts) - getBackwardWeight(startPos, ts);
        ts.moveIndex(index);
        ts.moveNext();
        return result;
    }

    private double getForwardWeight(int endPos, TokenSequence<JavaTokenId> ts) {
        double result = 0;
        ts.move(endPos);
        while (ts.moveNext()) {
            if (ts.token().id() == JavaTokenId.WHITESPACE) {
                int nls = numberOfNL(ts.token());
                result = nls == 0 ? 1 : (1 / nls);
            } else if (isComment(ts.token().id())) {
                if (ts.token().id() == JavaTokenId.LINE_COMMENT) {
                    return 1;
                }
                result = 0;
                break;
            } else {
                break;
            }
        }
        return result;
    }

    private double getBackwardWeight(int startPos, TokenSequence<JavaTokenId> ts) {
        double result = 0;
        ts.move(startPos);
        while (ts.movePrevious()) {
            if (ts.token().id() == JavaTokenId.WHITESPACE) {
                int nls = numberOfNL(ts.token());
                result = nls == 0 ? 0 : (1 / nls);
            } else if (isComment(ts.token().id())) {
                result = 0;
                break;
            } else {
                break;
            }
        }
        return result;
    }

    private void attachComments(Iterable<? extends Token<JavaTokenId>> foundComments, Tree tree, CommentHandler ch, CommentSet.RelativePosition positioning) {
        if (foundComments == null || !foundComments.iterator().hasNext()) return;
        CommentSet set = createCommentSet(ch, tree);
        for (Token<JavaTokenId> comment : foundComments) {
            attachComment(positioning, set, comment);
        }
    }

    private void attachComment(CommentSet.RelativePosition positioning, CommentSet set, Token<JavaTokenId> comment) {
        Comment c = Comment.create(getStyle(comment.id()), comment.offset(null),
                getEndPos(comment), NOPOS, getText(comment));
        set.addComment(positioning, c);        
    }

    private String getText(Token<JavaTokenId> comment) {
        return String.valueOf(comment.text());
    }

    private int getEndPos(Token<JavaTokenId> comment) {
        return comment.offset(null) + comment.length();
    }

    private Comment.Style getStyle(JavaTokenId id) {
        switch (id) {
            case JAVADOC_COMMENT:
                return Comment.Style.JAVADOC;
            case LINE_COMMENT:
                return Comment.Style.LINE;
            case BLOCK_COMMENT:
                return Comment.Style.BLOCK;
            default:
                return Comment.Style.WHITESPACE;
        }
    }

    private int[] getBounds(JCTree tree) {
        return new int[]{(int) positions.getStartPosition(unit, tree), (int) positions.getEndPosition(unit, tree)};
    }


    private Tree getTree(TreeUtilities tu, TokenSequence<JavaTokenId> ts) {
        int start = ts.offset();
        if (ts.token().length() > 0) {
            start++; //going into token. This is required because token offset is not considered as start of tree :(
        }
        TreePath path = tu.pathFor(start);
        if (path != null) {
            return path.getLeaf();
        }
        return null;
    }

    private int numberOfNL(Token<JavaTokenId> t) {
        int count = 0;
        CharSequence charSequence = t.text();
        for (int i = 0; i < charSequence.length(); i++) {
            char a = charSequence.charAt(i);
            if ('\n' == a) {
                count++;
            }
        }
        return count;
    }

    private CommentsCollection getCommentsCollection(TokenSequence<JavaTokenId> ts, int maxTension) {
        CommentsCollection result = new CommentsCollection();
        Token<JavaTokenId> t = ts.token();
        result.add(t);
        boolean isLC = t.id() == JavaTokenId.LINE_COMMENT;
        int lastCommentIndex = ts.index();
        int start = ts.offset();
        int end = ts.offset() + ts.token().length();
        while (ts.moveNext()) {
            if (ts.index() < tokenIndexAlreadyAdded) continue;
            t = ts.token();
            if (isComment(t.id())) {
                result.add(t);
                start = Math.min(ts.offset(), start);
                end = Math.max(ts.offset() + t.length(), end);
                isLC = t.id() == JavaTokenId.LINE_COMMENT;
                lastCommentIndex = ts.index();                
            } else if (t.id() == JavaTokenId.WHITESPACE) {
                if ((numberOfNL(t) + (isLC ? 1 : 0)) > maxTension) {
                    break;
                }
            } else {
                break;                
            }
        }
        ts.moveIndex(lastCommentIndex);
        ts.moveNext();
        tokenIndexAlreadyAdded = ts.index();
        result.setBounds(new int[]{start, end});
//        System.out.println("tokenIndexAlreadyAdded = " + tokenIndexAlreadyAdded);
        return result;
    }

    private CommentSet createCommentSet(CommentHandler ch, Tree lastTree) {
        return ch.getComments(lastTree);
    }

    private boolean isComment(JavaTokenId tid) {
        switch (tid) {
            case LINE_COMMENT:
            case BLOCK_COMMENT:
            case JAVADOC_COMMENT:
                return true;
            default:
                return false;
        }
    }

    private static class CommentsCollection implements Iterable<Token<JavaTokenId>> {
        private final int[] bounds = {NOPOS, NOPOS};
        private final List<Token<JavaTokenId>> comments = new LinkedList<Token<JavaTokenId>>();

        void add(Token<JavaTokenId> comment) {
            comments.add(comment);
        }

        boolean isEmpty() {
            return comments.isEmpty();
        }

        public Iterator<Token<JavaTokenId>> iterator() {
            return comments.iterator();
        }

        void setBounds(int[] bounds) {
            this.bounds[0] = bounds[0];
            this.bounds[1] = bounds[1];
        }

        public int[] getBounds() {
            return bounds.clone();
        }

        public void merge(CommentsCollection cc) {
            comments.addAll(cc.comments);
            this.bounds[0] = Math.min(this.bounds[0], cc.bounds[0]);
            this.bounds[1] = Math.max(this.bounds[1], cc.bounds[1]);
        }
    }
}
