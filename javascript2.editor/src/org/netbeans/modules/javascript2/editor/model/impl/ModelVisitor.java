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
package org.netbeans.modules.javascript2.editor.model.impl;

import com.oracle.nashorn.ir.*;
import com.oracle.nashorn.parser.TokenType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.model.*;
import org.netbeans.modules.javascript2.editor.model.impl.ScopeImpl.ElementFilter;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;

/**
 *
 * @author Petr Pisl
 */
public class ModelVisitor extends PathNodeVisitor {

    private JsParserResult parserResult;
    private final FileScopeImpl fileScope;
    private final ModelBuilder modelBuilder;
    private final List<FunctionNode> unvisitedFn;
    /**
     * Keeps the name of the visited properties
     */
    private final List<String> visitedProperties;
    private final List<List<FunctionNode>> functionStack;

    public ModelVisitor(JsParserResult parserResult) {
        this.parserResult = parserResult;
        this.fileScope = new FileScopeImpl(parserResult);
        this.modelBuilder = new ModelBuilder(this.fileScope);
        this.unvisitedFn = new ArrayList<FunctionNode>();
        this.visitedProperties = new ArrayList<String>();
        this.functionStack = new ArrayList<List<FunctionNode>>();
    }

    @Override
    public Node visit(BinaryNode binaryNode, boolean onset) {
        if (onset) {            
            if (binaryNode.tokenType() == TokenType.ASSIGN 
                    && !(binaryNode.rhs() instanceof ReferenceNode || binaryNode.rhs() instanceof ObjectNode)
                    && (binaryNode.lhs() instanceof AccessNode || binaryNode.lhs() instanceof IdentNode)) {
                // TODO probably not only assign                
                ScopeImpl scope = modelBuilder.getCurrentScope();
                if (binaryNode.lhs() instanceof AccessNode) {
                    List<Identifier> name = getName(binaryNode);
                    AccessNode aNode = (AccessNode)binaryNode.lhs();
                    if (aNode.getBase() instanceof IdentNode && "this".equals(((IdentNode)aNode.getBase()).getName())) { //NOI18N
                        // a usage of field
                        String fieldName = aNode.getProperty().getName();
                        Field field = findFieldWithName((FunctionScope)scope, fieldName);
                        if (field == null) {
                            // needs to decide, whether it belongs to this function or the parent one
                            Scope whereToAdd = scope;
                            if (scope.getInElement() instanceof FunctionScope) {
                                whereToAdd = (Scope)scope.getInElement();
                            }
                            ((FunctionScopeImpl)whereToAdd).addElement(new FieldImpl(whereToAdd, 
                                    new IdentifierImpl(fieldName, new OffsetRange(aNode.getProperty().getStart(), aNode.getProperty().getFinish()))));
                        }
                    } else {
                        // probably a property of an object
                        List<Identifier> fqName = getName(aNode);
                        ModelElementFactory.createField(fqName, modelBuilder);
                    }
                    
                } else {
                    IdentNode ident = (IdentNode)binaryNode.lhs();
                    final Identifier name = new IdentifierImpl(ident.getName(), new OffsetRange(ident.getStart(), ident.getFinish()));
                    final String newVarName = name.getName();
                    Variable variable = findVarWithName(scope, newVarName);
                    if (variable == null) {
                        // variable was not found -> it's not declared and it has to be
                        // added to the global scope (filescope) as implicit variable
                        FileScopeImpl fScope = ModelUtils.getFileScope(scope);
                        fScope.addElement(new VariableImpl(scope, name, true, true));
                    }
                }
            }
        }
        return super.visit(binaryNode, onset);
    }

    @Override
    public Node visit(FunctionNode functionNode, boolean onset) {
        IdentNode ident = functionNode.getIdent();
        System.out.println("FunctionNode: " + functionNode.getName() + " , path: " + getPath().size() + " , onset: " + onset);

        if (onset) {
            addToPath(functionNode);
            List<FunctionNode> functions = new ArrayList<FunctionNode>(functionNode.getFunctions().size());
            for (FunctionNode fn : functionNode.getFunctions()) {
                functions.add(fn);
            }

            for (FunctionNode fn : functions) {
                if (fn.getIdent().getStart() < fn.getIdent().getFinish()) {
                    fn.accept(this);
                }
            }

            List<Identifier> name = null;

            int pathSize = getPath().size();
            if (pathSize > 1 && getPath().get(pathSize - 2) instanceof ReferenceNode) {
                List<FunctionNode> siblings = functionStack.get(functionStack.size() - 1);
                if (siblings.remove(functionNode)) {
                    System.out.println("   funkce smazana ze seznam ve stacku");
                } else {
                    System.out.println("    !! funkce nenalezena v seznamu ve stacku");
                }

                if (pathSize > 3) {
                    Node node = getPath().get(pathSize - 3);
                    if (node instanceof PropertyNode) {
                        name = getName((PropertyNode)node);
                    } else if (node instanceof BinaryNode) {
                        name = getName((BinaryNode)node);
                    }
                }
            }

            if (name == null) {
                name = new ArrayList<Identifier>(1);
                name.add(new IdentifierImpl(functionNode.getIdent().getName(), 
                        new OffsetRange(functionNode.getIdent().getStart(), functionNode.getIdent().getFinish())));
            }
            functionStack.add(functions);

            // todo parameters;
            if (functionNode.getKind() != FunctionNode.Kind.SCRIPT) {
                ScopeImpl scope = modelBuilder.getCurrentScope();
                FunctionScopeImpl fncScope = ModelElementFactory.create(functionNode, name, modelBuilder);
                modelBuilder.setCurrentScope(scope = fncScope);
            }

            for (Node node : functionNode.getStatements()) {
                node.accept(this);
            }


            for (FunctionNode fn : functions) {
                if (fn.getIdent().getStart() >= fn.getIdent().getFinish()) {
                    System.out.println("   jeste nutno navstivit dalsi funkci:");
                    fn.accept(this);
                }
            }
            if (functionNode.getKind() != FunctionNode.Kind.SCRIPT) {
                modelBuilder.reset();
            }
            functionStack.remove(functionStack.size() - 1);
            removeFromPathTheLast();
            return null;

        }
        return super.visit(functionNode, onset);
    }

    public FileScopeImpl getFileScope() {
        return fileScope;
    }
    
    @Override
    public Node visit(ObjectNode objectNode, boolean onset) {
        if (onset) {
            List<Identifier> name = null;
            int pathSize = getPath().size();
            Node lastVisited = getPath().get(pathSize - 1);
            if ( lastVisited instanceof VarNode) {
                name = getName((VarNode)lastVisited);
            } else if (lastVisited instanceof PropertyNode) {
                        name = getName((PropertyNode)lastVisited);
                    } else if (lastVisited instanceof BinaryNode) {
                        name = getName((BinaryNode)lastVisited);
                    }
            if (name == null) {
                name = new ArrayList<Identifier>(1);
                name.add(new IdentifierImpl("UNKNOWN", 
                        new OffsetRange(objectNode.getStart(), objectNode.getFinish())));
            }
            ScopeImpl scope = modelBuilder.getCurrentScope();
            
            ObjectScopeImpl objectScope = ModelElementFactory.create(objectNode, name, modelBuilder);

            modelBuilder.setCurrentScope(scope = objectScope);
        } else {
            modelBuilder.reset();
        }

        return super.visit(functionNode, onset);
    }

    @Override
    public Node visit(PropertyNode propertyNode, boolean onset) {
        return super.visit(propertyNode, onset);
    }

    @Override
    public Node visit(ReferenceNode referenceNode, boolean onset) {
        if (referenceNode.getReference() instanceof FunctionNode) {
            if (onset) {
                addToPath(referenceNode);
                ((FunctionNode) referenceNode.getReference()).accept(this);
                removeFromPathTheLast();
                return null;
            }
        }
        return super.visit(referenceNode, onset);
    }
    
    @Override
    public Node visit(VarNode varNode, boolean onset) {
        if (onset) {
            ScopeImpl scope = modelBuilder.getCurrentScope();
            boolean isGlobal = false;
            if (scope instanceof FileScope) {
                isGlobal = true;
            }
            scope.addElement(new VariableImpl(scope, 
                    new IdentifierImpl(varNode.getName().getName(), 
                    new OffsetRange(varNode.getName().getStart(), varNode.getName().getFinish())), 
                    isGlobal, false));
        }
        return super.visit(varNode, onset);
    }
    
    private List<Identifier> getName(PropertyNode propertyNode) {
        List<Identifier> name = new ArrayList(1);
        if (propertyNode.getKey() instanceof IdentNode) {
            IdentNode ident = (IdentNode) propertyNode.getKey();
            name.add(new IdentifierImpl(ident.getName(),
                    new OffsetRange(ident.getStart(), ident.getFinish())));
        }
        return name;
    }
    
    private List<Identifier> getName(VarNode varNode) {
        List<Identifier> name = new ArrayList();
        name.add(new IdentifierImpl(varNode.getName().getName(), 
                new OffsetRange(varNode.getName().getStart(), varNode.getName().getFinish())));
        return name;
    }
    
    private List<Identifier> getName(BinaryNode binaryNode) {
        List<Identifier> name = new ArrayList();
        Node lhs = binaryNode.lhs();
        if (lhs instanceof AccessNode) {
            name = getName((AccessNode)lhs);
        }
        return name;
    }
    
    private List<Identifier> getName(AccessNode aNode) {
        List<Identifier> name = new ArrayList();
        name.add(new IdentifierImpl(aNode.getProperty().getName(),
                new OffsetRange(aNode.getProperty().getStart(), aNode.getProperty().getFinish())));
        while (aNode.getBase() instanceof AccessNode) {
            aNode = (AccessNode) aNode.getBase();
            name.add(new IdentifierImpl(aNode.getProperty().getName(),
                    new OffsetRange(aNode.getProperty().getStart(), aNode.getProperty().getFinish())));
        }
        if (name.size() > 0 && aNode.getBase() instanceof IdentNode) {
            IdentNode ident = (IdentNode) aNode.getBase();
            if (!"this".equals(ident.getName())) {
                name.add(new IdentifierImpl(ident.getName(),
                        new OffsetRange(ident.getStart(), ident.getFinish())));
            }
        }
        Collections.reverse(name);
        return name;
    }
    
    private Variable findVarWithName(final Scope scope, final String name) {
        Variable result = null;
        Collection<Variable> variables = ScopeImpl.filter(scope.getElements(), new ScopeImpl.ElementFilter() {

            @Override
            public boolean isAccepted(ModelElement element) {
                return element.getJSKind().equals(JsElement.Kind.VARIABLE)
                        && element.getName().equals(name);
            }
        });
        
        if (!variables.isEmpty()) {
            result = variables.iterator().next();
        } else {
            if (!(scope instanceof FileScope)) {
                result = findVarWithName((Scope)scope.getInElement(), name);
            }
        }
        
        return result;
    }
    
    private Field findFieldWithName(FunctionScope function, final String name) {
        Field result = null;
        Collection<? extends Field> fields = function.getFields();
        result = ModelUtils.getFirst(ModelUtils.getFirst(fields, name));
        if (result == null && function.getInElement() instanceof FunctionScope) {
            FunctionScope parent = (FunctionScope)function.getInElement();
            fields = parent.getFields();
            result = ModelUtils.getFirst(ModelUtils.getFirst(fields, name));
        }
        return result;
    }
    
}
