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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.groovy.editor.occurrences;

import java.util.HashSet;
import java.util.Set;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.netbeans.api.lexer.Token;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.ASTUtils.FakeASTNode;
import org.netbeans.modules.groovy.editor.api.Methods;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.FindTypeUtils;

/**
 * Visitor for finding occurrences of the class types, variables and methods.
 *
 * @author Martin Adamek
 * @author Martin Janicek
 */
public final class VariableScopeVisitor extends TypeVisitor {

    private final Set<ASTNode> occurrences;
    private final ASTNode leafParent;


    public VariableScopeVisitor(SourceUnit sourceUnit, AstPath path, BaseDocument doc, int cursorOffset) {
        super(sourceUnit, path, doc, cursorOffset, true);
        this.occurrences = new HashSet<ASTNode>();
        this.leafParent = path.leafParent();
    }

    public Set<ASTNode> getOccurrences() {
        return occurrences;
    }

    @Override
    public void visitArrayExpression(ArrayExpression visitedArray) {
        final ClassNode visitedType = visitedArray.getElementType();
        final String visitedName = removeParentheses(visitedType.getName());

        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            ASTNode currentNode = FindTypeUtils.findCurrentNode(path, doc, cursorOffset);
            addOccurrences(visitedType, (ClassNode) currentNode);
        } else if (leaf instanceof Variable) {
            String varName = removeParentheses(((Variable) leaf).getName());
            if (varName.equals(visitedName)) {
                occurrences.add(new FakeASTNode(visitedType, visitedName));
            }
        } else if (leaf instanceof ConstantExpression && leafParent instanceof PropertyExpression) {
            if (visitedName.equals(((PropertyExpression) leafParent).getPropertyAsString())) {
                occurrences.add(new FakeASTNode(visitedType, visitedName));
            }
        }
        super.visitArrayExpression(visitedArray);
    }

    @Override
    protected void visitParameters(Parameter[] parameters, Variable variable) {
        // method is declaring given variable, let's visit only the method,
        // but we need to check also parameters as those are not part of method visit
        for (Parameter parameter : parameters) {
            ClassNode paramType = parameter.getType();
            if (helper.isCaretOnParamType(parameter)) {
                occurrences.add(new FakeASTNode(paramType, paramType.getNameWithoutPackage()));
            } else if (helper.isCaretOnGenericType(paramType)) {
                ClassNode genericType = helper.getGenericType(paramType);
                occurrences.add(new FakeASTNode(genericType, genericType.getNameWithoutPackage()));
            } else {
                if (parameter.getName().equals(variable.getName())) {
                    occurrences.add(parameter);
                    break;
                }
            }
        }
        super.visitParameters(parameters, variable);
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        if (expression.isParameterSpecified() && (leaf instanceof Variable)) {
            visitParameters(expression.getParameters(), (Variable) leaf);
        }
        super.visitClosureExpression(expression);
    }


    @Override
    protected boolean isValidToken(Token<? extends GroovyTokenId> currentToken, Token<? extends GroovyTokenId> previousToken) {
        // cursor must be positioned on identifier, otherwise occurences doesn't make sense
        // second check is here because we want to have occurences also at the end of the identifier (see issue #155574)
        return currentToken.id() == GroovyTokenId.IDENTIFIER || previousToken.id() == GroovyTokenId.IDENTIFIER;
    }

    @Override
    public void visitVariableExpression(VariableExpression variableExpression) {
        if (leaf instanceof FieldNode) {
            addVariableExpressionOccurrences(variableExpression, (FieldNode) leaf);
        } else if (leaf instanceof PropertyNode) {
            addVariableExpressionOccurrences(variableExpression, ((PropertyNode) leaf).getField());
        } else if (leaf instanceof Parameter) {
            if (!helper.isCaretOnParamType(((Parameter) leaf)) && ((Parameter) leaf).getName().equals(variableExpression.getName())) {
                occurrences.add(variableExpression);
            }
        } else if (leaf instanceof Variable) {
            if (((Variable) leaf).getName().equals(variableExpression.getName())) {
                occurrences.add(variableExpression);
            }
        } else if (leaf instanceof ConstantExpression && leafParent instanceof PropertyExpression) {
            PropertyExpression property = (PropertyExpression) leafParent;
            if (variableExpression.getName().equals(property.getPropertyAsString())) {
                occurrences.add(variableExpression);
                return;
            }
        }
        super.visitVariableExpression(variableExpression);
    }

    private void addVariableExpressionOccurrences(VariableExpression visited, FieldNode findingNode) {
        if (helper.isCaretOnFieldType(findingNode)) {
            addOccurrences(visited.getType(), findingNode.getType());
        } else {
            final String visitedVariableName = visited.getName();
            final String fieldName = removeParentheses(findingNode.getName());
            if (visitedVariableName.equals(fieldName)) {
                occurrences.add(visited);
            }
        }
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        ClassNode visitedType;
        if (!expression.isMultipleAssignmentDeclaration()) {
            visitedType = expression.getVariableExpression().getType();
        } else {
            visitedType = expression.getTupleExpression().getType();
        }

        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            addOccurrences(visitedType, (ClassNode) FindTypeUtils.findCurrentNode(path, doc, cursorOffset));
        }
        super.visitDeclarationExpression(expression);
    }

    @Override
    public void visitField(FieldNode visitedField) {
        final ClassNode visitedType = visitedField.getType();
        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            ASTNode currentNode = FindTypeUtils.findCurrentNode(path, doc, cursorOffset);
            addOccurrences(visitedType, (ClassNode) currentNode);
        } else {
            if (leaf instanceof FieldNode) {
                if (visitedField.getName().equals(((FieldNode) leaf).getName())) {
                    occurrences.add(visitedField);
                }
            } else if (leaf instanceof PropertyNode) {
                if (visitedField.getName().equals(((PropertyNode) leaf).getField().getName())) {
                    occurrences.add(visitedField);
                }
            } else if (leaf instanceof Variable && ((Variable) leaf).getName().equals(visitedField.getName())) {
                occurrences.add(visitedField);
            } else if (leaf instanceof ConstantExpression && leafParent instanceof PropertyExpression) {
                PropertyExpression property = (PropertyExpression) leafParent;
                if (visitedField.getName().equals(property.getPropertyAsString())) {
                    occurrences.add(visitedField);
                }
            }
        }
        super.visitField(visitedField);
    }

    @Override
    public void visitMethod(MethodNode methodNode) {
        VariableScope variableScope = methodNode.getVariableScope();

        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            addMethodOccurrences(methodNode, (ClassNode) FindTypeUtils.findCurrentNode(path, doc, cursorOffset));
        } else {
            if (leaf instanceof Variable) {
                String name = ((Variable) leaf).getName();
                // This check is here because we can have method parameter with the same
                // name hidding property/field and we don't want to show occurences of these
                if (variableScope != null && variableScope.getDeclaredVariable(name) != null) {
                    return;
                }
            } else if (leaf instanceof MethodNode) {
                if (Methods.isSameMethod(methodNode, (MethodNode) leaf)) {
                    occurrences.add(methodNode);
                }
            } else if (leaf instanceof DeclarationExpression) {
                VariableExpression variable = ((DeclarationExpression) leaf).getVariableExpression();
                if (!variable.isDynamicTyped() && !methodNode.isDynamicReturnType()) {
                    addMethodOccurrences(methodNode, variable.getType());
                }
            } else if (leaf instanceof ConstantExpression && leafParent instanceof MethodCallExpression) {
                MethodCallExpression methodCallExpression = (MethodCallExpression) leafParent;
                if (Methods.isSameMethod(methodNode, methodCallExpression)) {
                    occurrences.add(methodNode);
                }
            }
        }
        super.visitMethod(methodNode);
    }

    private void addMethodOccurrences(MethodNode visitedMethod, ClassNode findingNode) {
        // Check return type
        addOccurrences(visitedMethod.getReturnType(), findingNode);

        // Check method parameters
        for (Parameter parameter : visitedMethod.getParameters()) {
            addOccurrences(parameter.getType(), findingNode);
        }
    }

    @Override
    public void visitConstructor(ConstructorNode constructor) {
        VariableScope variableScope = constructor.getVariableScope();
        if (leaf instanceof Variable) {
            String name = ((Variable) leaf).getName();
            if (variableScope != null && variableScope.getDeclaredVariable(name) != null) {
                return;
            }
        } else if (leaf instanceof ConstantExpression && leafParent instanceof PropertyExpression) {
            String name = ((ConstantExpression) leaf).getText();
            if (variableScope != null && variableScope.getDeclaredVariable(name) != null) {
                return;
            }
        }

        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            addConstructorOccurrences(constructor, (ClassNode) FindTypeUtils.findCurrentNode(path, doc, cursorOffset));
        } else {
            if (leaf instanceof ConstructorNode) {
                if (Methods.isSameConstructor(constructor, (ConstructorNode) leaf)) {
                    occurrences.add(constructor);
                }
            } else if (leaf instanceof ConstructorCallExpression) {
                if (Methods.isSameConstructor(constructor, (ConstructorCallExpression) leaf)) {
                    occurrences.add(constructor);
                }
            }
        }
        super.visitConstructor(constructor);
    }

    private void addConstructorOccurrences(ConstructorNode constructor, ClassNode findingNode) {
        for (Parameter parameter : constructor.getParameters()) {
            addOccurrences(parameter.getType(), findingNode);
        }
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression methodCall) {
        if (leaf instanceof MethodNode) {
            MethodNode method = (MethodNode) leaf;
            if (Methods.isSameMethod(method, methodCall) && !helper.isCaretOnReturnType(method)) {
                occurrences.add(methodCall);
            }
        } else if (leaf instanceof ConstantExpression && leafParent instanceof MethodCallExpression) {
            if (Methods.isSameMethod(methodCall, (MethodCallExpression) leafParent)) {
                occurrences.add(methodCall);
            }
        }
        super.visitMethodCallExpression(methodCall);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            addOccurrences(call.getType(), (ClassNode) FindTypeUtils.findCurrentNode(path, doc, cursorOffset));
        } else {
            if (leaf instanceof ConstructorNode) {
                ConstructorNode constructor = (ConstructorNode) leaf;
                if (Methods.isSameConstructor(constructor, call)) {
                    occurrences.add(call);
                }
            } else if (leaf instanceof ConstructorCallExpression) {
                if (Methods.isSameConstuctor(call, (ConstructorCallExpression) leaf)) {
                    occurrences.add(call);
                }
            }
        }
        super.visitConstructorCallExpression(call);
    }

    @Override
    public void visitClassExpression(ClassExpression clazz) {
        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            addClassExpressionOccurrences(clazz, (ClassNode) FindTypeUtils.findCurrentNode(path, doc, cursorOffset));
        }
        super.visitClassExpression(clazz);
    }

    private void addClassExpressionOccurrences(ClassExpression clazz, ClassNode findingNode) {
        final String visitedName = removeParentheses(clazz.getType().getName());
        final String findingName = removeParentheses(findingNode.getName());
        if (visitedName.equals(findingName)) {
            occurrences.add(clazz);
        }
    }

    @Override
    public void visitClass(ClassNode classNode) {
        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            addClassNodeOccurrences(classNode, (ClassNode) FindTypeUtils.findCurrentNode(path, doc, cursorOffset));
        }
        super.visitClass(classNode);
    }

    private void addClassNodeOccurrences(ClassNode visitedNode, ClassNode findingNode) {
        final String findingName = findingNode.getName();
        final ClassNode superClass = visitedNode.getUnresolvedSuperClass(false);
        final ClassNode[] interfaces = visitedNode.getInterfaces();

        // Check if the caret is on the ClassNode itself
        if (findingName.equals(visitedNode.getName())) {
            occurrences.add(new FakeASTNode(visitedNode, visitedNode.getNameWithoutPackage()));
        }

        // Check if the caret is on the parent type
        if (superClass.getLineNumber() > 0 && superClass.getColumnNumber() > 0) {
            if (findingName.equals(superClass.getName())) {
                occurrences.add(new FakeASTNode(superClass, superClass.getNameWithoutPackage()));
            }
        }

        // Check all implemented interfaces
        for (ClassNode interfaceNode : interfaces) {
            if (interfaceNode.getLineNumber() > 0 && interfaceNode.getColumnNumber() > 0) {
                if (findingName.equals(interfaceNode.getName())) {
                    occurrences.add(new FakeASTNode(interfaceNode, interfaceNode.getNameWithoutPackage()));
                }
            }
        }
    }

    @Override
    public void visitPropertyExpression(PropertyExpression node) {
        Expression property = node.getProperty();
        if (leaf instanceof Variable && ((Variable) leaf).getName().equals(node.getPropertyAsString())) {
            occurrences.add(property);
        } else if (leaf instanceof ConstantExpression && leafParent instanceof PropertyExpression) {
            PropertyExpression propertyUnderCursor = (PropertyExpression) leafParent;
            String nodeAsString = node.getPropertyAsString();
            if (nodeAsString != null && nodeAsString.equals(propertyUnderCursor.getPropertyAsString())) {
                occurrences.add(property);
            }
        }
        super.visitPropertyExpression(node);
    }

    @Override
    public void visitForLoop(ForStatement forLoop) {
        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            addOccurrences(forLoop.getVariableType(), (ClassNode) FindTypeUtils.findCurrentNode(path, doc, cursorOffset));
        }
        super.visitForLoop(forLoop);
    }

    private void addOccurrences(ClassNode visitedType, ClassNode findingType) {
        final String visitedTypeName = removeParentheses(visitedType.getName());
        final String findingName = removeParentheses(findingType.getName());
        final String findingNameWithoutPkg = removeParentheses(findingType.getNameWithoutPackage());

        if (visitedTypeName.equals(findingName)) {
            occurrences.add(new FakeASTNode(visitedType, findingNameWithoutPkg));
        }
        addGenericsOccurrences(visitedType, findingType);
    }

    private void addGenericsOccurrences(ClassNode visitedType, ClassNode findingNode) {
        final String findingTypeName = removeParentheses(findingNode.getName());
        final GenericsType[] genericsTypes = visitedType.getGenericsTypes();

        if (genericsTypes != null && genericsTypes.length > 0) {
            for (GenericsType genericsType : genericsTypes) {
                final String genericTypeName = genericsType.getType().getName();
                final String genericTypeNameWithoutPkg = genericsType.getName();

                if (genericTypeName.equals(findingTypeName)) {
                    occurrences.add(new FakeASTNode(genericsType, genericTypeNameWithoutPkg));
                }
            }
        }
    }

    /**
     * Removes [] parentheses.
     *
     * @param name where we want to strip parentheses off
     * @return name without [] parentheses
     */
    private String removeParentheses(String name) {
        if (name.endsWith("[]")) { // NOI18N
            name = name.substring(0, name.length() - 2);
        }
        return name;
    }
}
