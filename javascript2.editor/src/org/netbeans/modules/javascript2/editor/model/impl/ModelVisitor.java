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
import com.oracle.nashorn.parser.Token;
import com.oracle.nashorn.parser.TokenType;
import java.util.*;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.doc.jsdoc.JsDocDocumentationProvider;
import org.netbeans.modules.javascript2.editor.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.model.*;
import org.netbeans.modules.javascript2.editor.model.DocumentationProvider;
import org.netbeans.modules.javascript2.editor.model.TypeUsage;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class ModelVisitor extends PathNodeVisitor {

    private final ModelBuilder modelBuilder;
    /**
     * Keeps the name of the visited properties
     */
    private final List<List<FunctionNode>> functionStack;
    private final JsParserResult parserResult;
    private final DocumentationProvider docSupport;

    public ModelVisitor(JsParserResult parserResult, DocumentationProvider docSupport) {
        FileObject fileObject = parserResult.getSnapshot().getSource().getFileObject();
        this.modelBuilder = new ModelBuilder(JsFunctionImpl.createGlobal(fileObject));
        this.functionStack = new ArrayList<List<FunctionNode>>();
        this.parserResult = parserResult;
        this.docSupport = docSupport;
    }

    public JsObject getGlobalObject() {
        return modelBuilder.getGlobal();
    }

    JsObjectImpl fromAN = null;
    @Override
    public Node visit(AccessNode accessNode, boolean onset) {
        if(onset) {
//            System.out.println("AccessNode: " + accessNode);
            BinaryNode node = getPath().get(getPath().size() - 1) instanceof BinaryNode
                    ? (BinaryNode)getPath().get(getPath().size() - 1) : null;
            if (!(node != null && node.tokenType() == TokenType.ASSIGN)) {
//                System.out.println("AccessNode: " + accessNode);
                if (accessNode.getBase() instanceof IdentNode && "this".equals(((IdentNode)accessNode.getBase()).getName())) { //NOI18N
                    if (accessNode.getProperty() instanceof IdentNode) {
                        IdentNode iNode = (IdentNode)accessNode.getProperty();
                        JsObject current = modelBuilder.getCurrentDeclarationScope();
                        JsObject property = current.getProperty(iNode.getName());
                        if (property == null && (current.getParent().getJSKind() == JsElement.Kind.CONSTRUCTOR
                                || current.getParent().getJSKind() == JsElement.Kind.OBJECT)) {
                            current = current.getParent();
                            property = current.getProperty(iNode.getName());
                        }
                        if (property != null) {
                            ((JsObjectImpl)property).addOccurrence(ModelUtils.documentOffsetRange(parserResult, iNode.getStart(), iNode.getFinish()));
                        }
                    }
                }
            }
        } else {
            if (accessNode.getBase() instanceof IdentNode) {
                IdentNode base = (IdentNode)accessNode.getBase();
                if (!"this".equals(base.getName())) {
                    Identifier name = ModelElementFactory.create(parserResult, (IdentNode)accessNode.getBase());
                    List<Identifier> fqname = new ArrayList<Identifier>();
                    fqname.add(name);
                    fromAN = ModelUtils.getJsObject(modelBuilder, fqname);
                    fromAN.addOccurrence(name.getOffsetRange());
                } else {
                    JsObject current = modelBuilder.getCurrentDeclarationScope();
                    JsObject property = current.getProperty(accessNode.getProperty().getName());
                    if (property == null && (current.getParent().getJSKind() == JsElement.Kind.CONSTRUCTOR
                            || current.getParent().getJSKind() == JsElement.Kind.OBJECT)) {
                        Node previous = getPreviousFromPath(2);
                        // check whether is not a part of method in constructor
                        if (!(previous instanceof BinaryNode && ((BinaryNode)previous).rhs() instanceof ReferenceNode)) {
                            current = current.getParent();
                        }
                    }
                    fromAN = (JsObjectImpl)current;
                    
                }
            }
            if (fromAN != null) {
                JsObjectImpl property = (JsObjectImpl)fromAN.getProperty(accessNode.getProperty().getName());
                if (property != null) {
                    property.addOccurrence(ModelUtils.documentOffsetRange(parserResult, accessNode.getProperty().getStart(), accessNode.getProperty().getFinish()));
                } else {
                    int pathSize = getPath().size();
                    Identifier name = ModelElementFactory.create(parserResult, (IdentNode)accessNode.getProperty());
                    if (pathSize > 1 && getPath().get(pathSize - 2) instanceof CallNode) {
                        CallNode cNode = (CallNode)getPath().get(pathSize - 2);
                        property = ModelElementFactory.createVirtualFunction(parserResult, fromAN, name, cNode.getArgs().size());                        
                    } else {
                        property = new JsObjectImpl(fromAN, name, name.getOffsetRange());
                    }
                    fromAN.addProperty(name.getName(), property);
                    
                }
                if(property != null) {
                    fromAN = property;
                }
            }
            if (!(getPath().get(getPath().size() - 1) instanceof AccessNode)) {
                fromAN = null;
            }
        }
        return super.visit(accessNode, onset);
    }


    @Override
    public Node visit(BinaryNode binaryNode, boolean onset) {
        if (onset) {
            if (binaryNode.tokenType() == TokenType.ASSIGN
                    && !(binaryNode.rhs() instanceof ReferenceNode || binaryNode.rhs() instanceof ObjectNode)
                    && (binaryNode.lhs() instanceof AccessNode || binaryNode.lhs() instanceof IdentNode)) {
                // TODO probably not only assign
                JsObjectImpl parent = modelBuilder.getCurrentDeclarationScope();
                if (binaryNode.lhs() instanceof AccessNode) {
                    List<Identifier> name = getName(binaryNode);
//                    System.out.println("in binarynode: " + binaryNode.lhs());
                    AccessNode aNode = (AccessNode)binaryNode.lhs();
                    if (aNode.getBase() instanceof IdentNode && "this".equals(((IdentNode)aNode.getBase()).getName())) { //NOI18N
                        // a usage of field
                        String fieldName = aNode.getProperty().getName();
                        if(!ModelUtils.isGlobal(parent.getParent()) &&
                            (parent.getParent() instanceof JsFunctionImpl
                                || isInPropertyNode())) {
                            parent = (JsObjectImpl)parent.getParent();
                        }
                        if(parent.getProperty(fieldName) == null) {
                            Identifier identifier = ModelElementFactory.create(parserResult, (IdentNode)aNode.getProperty());
                            parent.addProperty(fieldName, new JsObjectImpl(parent, identifier, identifier.getOffsetRange() ));
                        }
                    } else {
                        // probably a property of an object
                        List<Identifier> fqName = getName(aNode);
                        ModelUtils.getJsObject(modelBuilder, fqName);
                    }

                } else {
                    IdentNode ident = (IdentNode)binaryNode.lhs();
                    final Identifier name = ModelElementFactory.create(parserResult, ident);
                    final String newVarName = name.getName();
                    boolean hasParent = parent.getProperty(newVarName) != null ;
                    boolean hasGrandParent = parent.getJSKind() == JsElement.Kind.METHOD && parent.getParent().getProperty(newVarName) != null;
                    if (!hasParent && !hasGrandParent && modelBuilder.getGlobal().getProperty(newVarName) == null) {
                        // variable was not found -> it's not declared and it has to be
                        // added to the global scope (filescope) as implicit variable
                        JsObjectImpl variable = new JsObjectImpl(modelBuilder.getGlobal(), name, name.getOffsetRange());
                        variable.setDeclared(false);
                        modelBuilder.getGlobal().addProperty(newVarName, variable);
                    } else {
                        JsObject lhs = hasParent ? parent.getProperty(newVarName) : hasGrandParent ? parent.getParent().getProperty(newVarName) : null;
                        if (lhs != null) {
                            ((JsObjectImpl)lhs).addOccurrence(name.getOffsetRange());
                            if (binaryNode.rhs() instanceof UnaryNode && Token.descType(binaryNode.rhs().getToken()) == TokenType.NEW) {
                                // new XXXX() statement
                                modelBuilder.setCurrentObject((JsObjectImpl)lhs);
                                binaryNode.rhs().accept(this);
                                modelBuilder.reset();
                                return null;
                            }
                        }
                    }
                }
                if (binaryNode.rhs() instanceof IdentNode) {
                    addOccurence((IdentNode)binaryNode.rhs());
                }
            } else if(binaryNode.tokenType() != TokenType.ASSIGN) {
                if (binaryNode.lhs() instanceof IdentNode) {
                    addOccurence((IdentNode)binaryNode.lhs());
                }
                if (binaryNode.rhs() instanceof IdentNode) {
                    addOccurence((IdentNode)binaryNode.rhs());
                }
            }
        }
        return super.visit(binaryNode, onset);
    }

    @Override
    public Node visit(CallNode callNode, boolean onset) {
        if (onset) {
//            Node calledFunction = callNode.getFunction();
//            if (calledFunction instanceof AccessNode) {
//                AccessNode aNode = (AccessNode)callNode.getFunction();
//                if (aNode.getBase() instanceof IdentNode && "this".equals(((IdentNode)aNode.getBase()).getName())) {
//
//                } else if(aNode.getBase() instanceof IdentNode){
//                    List<Identifier> name = getName(aNode);
//                    JsObject parent = ModelUtils.getJsObject(modelBuilder, name.subList(0, 1));
//                    JsObject property;
//
//                    for (int i = 1; i < name.size(); i++) {
//                        Identifier iden = name.get(i);
//                        property = parent.getProperty(iden.getName());
//                        if (property == null) {
//                            if(!parent.isDeclared()) {
//                                property = new JsObjectImpl(parent, iden, iden.getOffsetRange());
//                                parent.addProperty(property.getName(), property);
//                            }
//                        }
//                    }
//
                    for(Node argument : callNode.getArgs()) {
                        if (argument instanceof IdentNode) {
                            addOccurence((IdentNode)argument);
                        }
                    }
//                    JsObject property = parent.getProperty(name.get(name.size() - 1).getName());
//                    if (property ==  null && (parent.getParent() != null && (parent.getParent().getJSKind() == JsElement.Kind.CONSTRUCTOR
//                                || parent.getParent().getJSKind() == JsElement.Kind.OBJECT))) {
//                        parent = parent.getParent();
//                        property = parent.getProperty(name.get(0).getName());
//                    }
//                    if (property == null) {
//                        parent = ModelUtils.getJsObject(modelBuilder, name.subList(0, name.size() - 1));
//                        property = parent.getProperty(name.get(name.size() - 1).getName());
//                    }
//                    if (property != null) {
//                        ((JsObjectImpl)property).addOccurrence(name.get(0).getOffsetRange());
//                    } else {
//                        property = new JsObjectImpl(parent, name.get(0), name.get(0).getOffsetRange());
//                        parent.addProperty(property.getName(), property);
//                    }
//                }
//            }
        } 
            
        return super.visit(callNode, onset);
    }

    @Override
    public Node visit(IndexNode indexNode, boolean onset) {
        if (!onset && indexNode.getIndex() instanceof LiteralNode) {
            Node base = indexNode.getBase();
            JsObjectImpl parent = null;
            if (base instanceof AccessNode) {
                parent = fromAN;
            } else if (base instanceof IdentNode) {
                Identifier parentName = ModelElementFactory.create(parserResult, (IdentNode)base);
                List<Identifier> fqName = new ArrayList<Identifier>();
                fqName.add(parentName);
                parent = ModelUtils.getJsObject(modelBuilder, fqName);
                parent.addOccurrence(parentName.getOffsetRange());
            }
            if (parent != null) {
                String index = ((LiteralNode)indexNode.getIndex()).getPropertyName();
                JsObjectImpl property = (JsObjectImpl)parent.getProperty(index);
                if (property != null) {
                    property.addOccurrence(ModelUtils.documentOffsetRange(parserResult, indexNode.getIndex().getStart(), indexNode.getIndex().getFinish()));
                } else {
                    Identifier name = ModelElementFactory.create(parserResult, (LiteralNode)indexNode.getIndex());
                    property = new JsObjectImpl(parent, name, name.getOffsetRange());
                    parent.addProperty(name.getName(), property);
                }
            }
        }
        return super.visit(indexNode, onset);
    }


    
    @Override
    public Node visit(FunctionNode functionNode, boolean onset) {
//        System.out.println("FunctionNode: " + functionNode.getName() + " , path: " + getPath().size() + " , onset: " + onset);

        if (onset) {
            JsObjectImpl inObject = modelBuilder.getGlobal();
            addToPath(functionNode);
            List<FunctionNode> functions = new ArrayList<FunctionNode>(functionNode.getFunctions().size());
            for (FunctionNode fn : functionNode.getFunctions()) {
                functions.add(fn);
            }

            List<Identifier> name = null;
            boolean isPrivate = false;
            boolean isStatic = false; 
            int pathSize = getPath().size();
            if (pathSize > 1 && getPath().get(pathSize - 2) instanceof ReferenceNode) {
                List<FunctionNode> siblings = functionStack.get(functionStack.size() - 1);
                siblings.remove(functionNode);

                if (pathSize > 3) {
                    Node node = getPath().get(pathSize - 3);
                    if (node instanceof PropertyNode) {
                        name = getName((PropertyNode)node);
                    } else if (node instanceof BinaryNode) {
                        name = getName((BinaryNode)node);
                    } else if (node instanceof VarNode) {
                       name = getName((VarNode)node);
                        // private method
                        // It can be only if it's in a function
                        isPrivate = functionStack.size() > 1;
                    }
                }
            }

            if (name == null || name.isEmpty()) {
                name = new ArrayList<Identifier>(1);
                int start = functionNode.getIdent().getStart();
                int end = functionNode.getIdent().getFinish();
                if(start != 0) {
                    start = LexUtilities.getLexerOffset(parserResult, start);
                }
                if(end != 0) {
                    end = LexUtilities.getLexerOffset(parserResult, end);
                } else {
                    end = parserResult.getSnapshot().getText().length();
                }
                name.add(new IdentifierImpl(functionNode.getIdent().getName(),
                        new OffsetRange(start, end)));
                if (pathSize > 2 && getPath().get(pathSize - 2) instanceof FunctionNode) {
                    isPrivate = true;
                    isStatic = true;
                }
            }
            functionStack.add(functions);

            // todo parameters;
            JsFunctionImpl fncScope = null;
            if (functionNode.getKind() != FunctionNode.Kind.SCRIPT) {
                DeclarationScopeImpl scope = modelBuilder.getCurrentDeclarationScope();
                fncScope = ModelElementFactory.create(parserResult, functionNode, name, modelBuilder);
                boolean isAnonymous = false;
                if (getPreviousFromPath(2) instanceof ReferenceNode) {
                    Node node = getPreviousFromPath(3);
                    if (node instanceof CallNode) {
                        isAnonymous = true;
                    } else if (node instanceof AccessNode && getPreviousFromPath(4) instanceof CallNode) {
                        String methodName = ((AccessNode)node).getProperty().getName();
                        if ("call".equals(methodName) || "apply".equals(methodName)) {
                            isAnonymous = true;
                        }
                    }
                }
                fncScope.setAnonymous(isAnonymous);
                Set<Modifier> modifiers = fncScope.getModifiers();
                if (isPrivate) {
                    modifiers.remove(Modifier.PUBLIC);
                    modifiers.add(Modifier.PRIVATE);
                }
                if (isStatic) {
                    modifiers.add(Modifier.STATIC);
                }
                scope.addDeclaredScope(fncScope);
                modelBuilder.setCurrentObject((JsObjectImpl)fncScope);
            }

            for (FunctionNode fn : functions) {
                if (fn.getIdent().getStart() < fn.getIdent().getFinish()) {
                    fn.accept(this);
                }
            }

            for (Node node : functionNode.getStatements()) {
                node.accept(this);
            }

            
            if (fncScope != null) {
                
                DocumentationProvider docProvider = DocumentationSupport.getDocumentationProvider(parserResult); 
                Types types = docProvider.getReturnType(functionNode);
                if (types != null) {
                    for(Type type : types.getTypes()) {
                        fncScope.addReturnType(new TypeUsageImpl(type.getType(), -1, true));
                    }
                }
                if (fncScope.areReturnTypesEmpty()) {
                    // the function doesn't have return statement -> returns undefined
                    fncScope.addReturnType(new TypeUsageImpl(Type.UNDEFINED, -1, false));
                }
            }
                
            for (FunctionNode fn : functions) {
                if (fn.getIdent().getStart() >= fn.getIdent().getFinish()) {
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

    @Override
    public Node visit(ObjectNode objectNode, boolean onset) {
        Node previousVisited = getPath().get(getPath().size() - (onset ? 1 : 2));
        if (onset) {
            if(previousVisited instanceof CallNode) {
                // TODO there should be handled anonymous object that are going as parameter to a funciton
                //create anonymous object
                JsObjectImpl object = ModelElementFactory.createAnonymousObject(parserResult, objectNode, (CallNode)previousVisited, modelBuilder);
                modelBuilder.setCurrentObject(object);
                return super.visit(objectNode, onset);
            }
            if (!(previousVisited instanceof ReturnNode
                    || previousVisited instanceof CallNode)) {
                List<Identifier> fqName = null;
                int pathSize = getPath().size();
                boolean isDeclaredInParent = false;
                Node lastVisited = getPath().get(pathSize - 1);
                if ( lastVisited instanceof VarNode) {
                    fqName = getName((VarNode)lastVisited);
                    isDeclaredInParent = true;
                } else if (lastVisited instanceof PropertyNode) {
                    fqName = getName((PropertyNode) lastVisited);
                    isDeclaredInParent = true;
                } else if (lastVisited instanceof BinaryNode) {
                    BinaryNode binNode = (BinaryNode) lastVisited;
                    fqName = getName(binNode);
                    if (binNode.lhs() instanceof AccessNode
                            && ((AccessNode) binNode.lhs()).getBase() instanceof IdentNode
                            && ((IdentNode) ((AccessNode) binNode.lhs()).getBase()).getName().equals("this")) {
                        isDeclaredInParent = true;
                    }
                }
                if (fqName == null || fqName.isEmpty()) {
                    fqName = new ArrayList<Identifier>(1);
                    fqName.add(new IdentifierImpl("UNKNOWN",   //NOI18N
                            ModelUtils.documentOffsetRange(parserResult, objectNode.getStart(), objectNode.getFinish())));
                }
                JsObjectImpl scope = modelBuilder.getCurrentObject();

                JsObjectImpl objectScope = ModelElementFactory.create(parserResult, objectNode, fqName, modelBuilder, isDeclaredInParent);
                modelBuilder.setCurrentObject(objectScope);
            }
        } else {
            if (!(previousVisited instanceof ReturnNode)) {
                modelBuilder.reset();
            }
        }

        return super.visit(objectNode, onset);
    }

    @Override
    public Node visit(PropertyNode propertyNode, boolean onset) {
        if (onset
                && (propertyNode.getKey() instanceof IdentNode || propertyNode.getKey() instanceof LiteralNode)
                && !(propertyNode.getValue() instanceof ObjectNode)) {
            JsObjectImpl scope = modelBuilder.getCurrentObject();
            Identifier name = null;
            Node key = propertyNode.getKey();
            if (key instanceof IdentNode) {
                name = ModelElementFactory.create(parserResult, (IdentNode)key);
            } else if (key instanceof LiteralNode) {
                name = ModelElementFactory.create(parserResult, (LiteralNode)key);
            }

            if (name != null) {
                JsObjectImpl property = (JsObjectImpl)scope.getProperty(name.getName());
                if (property == null) {
                    property = new JsObjectImpl(scope, name, name.getOffsetRange());
                } else {
                    // The property can be already defined, via a usage before declaration (see testfiles/model/simpleObject.js - called property)
                    JsObjectImpl newProperty = new JsObjectImpl(scope, name, name.getOffsetRange());
                    newProperty.addOccurrence(property.getDeclarationName().getOffsetRange());
                    for(Occurrence occurrence : property.getOccurrences()) {
                        newProperty.addOccurrence(occurrence.getOffsetRange());
                    }
                    property = newProperty;
                }
                scope.addProperty(name.getName(), property);
                property.setDeclared(true);
                Node value = propertyNode.getValue();
                if(value instanceof CallNode) {
                    // TODO for now, don't continue. There shoudl be handled cases liek
                    // in the testFiles/model/property02.js file
                    return null;
                } else {
                    Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(value);
                    if (!types.isEmpty()) {
                        property.addAssignment(types, name.getOffsetRange().getStart());
                    }
                }
                
            }
        }
        return super.visit(propertyNode, onset);
    }

    @Override
    public Node visit(ReferenceNode referenceNode, boolean onset) {
        if (onset && referenceNode.getReference() instanceof FunctionNode) {
            addToPath(referenceNode);
            ((FunctionNode) referenceNode.getReference()).accept(this);
            removeFromPathTheLast();
            return null;
        }
        return super.visit(referenceNode, onset);
    }

    @Override
    public Node visit(ReturnNode returnNode, boolean onset) {
        if (onset) {
            Node expression = returnNode.getExpression();
            if (expression instanceof IdentNode) {
                addOccurence((IdentNode)expression);
            }
            Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(expression);
            if(types.isEmpty()) {
               types.add(new TypeUsageImpl(Type.UNRESOLVED, returnNode.getStart(), true)); 
            }
            JsFunctionImpl function = (JsFunctionImpl)modelBuilder.getCurrentDeclarationScope();
            function.addReturnType(types);
        }
        return super.visit(returnNode, onset);
    }


    @Override
    public Node visit(UnaryNode unaryNode, boolean onset) {
        if (onset) {
            if (Token.descType(unaryNode.getToken()) == TokenType.NEW) {
                Node lastNode = getPath().get(getPath().size() -1);
                if (unaryNode.rhs() instanceof CallNode
                        && ((CallNode)unaryNode.rhs()).getFunction() instanceof IdentNode
                        && !(lastNode instanceof PropertyNode)) {
                    int start = unaryNode.getStart();
                    if (getPath().get(getPath().size() - 1) instanceof VarNode) {
                        start = ((VarNode)getPath().get(getPath().size() - 1)).getName().getFinish();
                    }
                    Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(unaryNode);
                    for (TypeUsage type : types) {
                        modelBuilder.getCurrentObject().addAssignment(type, start);
                    }
                    
                }
            }
        }
        return super.visit(unaryNode, onset);
    }

    @Override
    public Node visit(VarNode varNode, boolean onset) {
        if (onset && !(varNode.getInit() instanceof ObjectNode || varNode.getInit() instanceof ReferenceNode)) {
            JsObject parent = modelBuilder.getCurrentObject();
            Identifier name = new IdentifierImpl(varNode.getName().getName(),
                    ModelUtils.documentOffsetRange(parserResult, varNode.getName().getStart(), varNode.getName().getFinish()));
            JsObjectImpl variable = new JsObjectImpl(parent, name, name.getOffsetRange());
            variable.setDeclared(true);
            if (parent.getJSKind() != JsElement.Kind.FILE) {
                variable.getModifiers().remove(Modifier.PUBLIC);
                variable.getModifiers().add(Modifier.PRIVATE);
            }
            parent.addProperty(name.getName(), variable);
            modelBuilder.setCurrentObject(variable);
            if (varNode.getInit() instanceof IdentNode) {
                addOccurence((IdentNode)varNode.getInit());
            }
            if (!(varNode.getInit() instanceof UnaryNode)) {
                Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(varNode.getInit());
                for (TypeUsage type : types) {
                    variable.addAssignment(type, name.getOffsetRange().getEnd());
                }
            }
        }
        if (!onset && !(varNode.getInit() instanceof ObjectNode || varNode.getInit() instanceof ReferenceNode)) {
            modelBuilder.reset();
        }
        return super.visit(varNode, onset);
    }

//--------------------------------End of visit methods--------------------------------------

    private List<Identifier> getName(PropertyNode propertyNode) {
        List<Identifier> name = new ArrayList(1);
        if (propertyNode.getKey() instanceof IdentNode) {
            IdentNode ident = (IdentNode) propertyNode.getKey();
            name.add(new IdentifierImpl(ident.getName(),
                    ModelUtils.documentOffsetRange(parserResult, ident.getStart(), ident.getFinish())));
        } else if (propertyNode.getKey() instanceof LiteralNode){
            LiteralNode lNode = (LiteralNode)propertyNode.getKey();
            name.add(new IdentifierImpl(lNode.getString(),
                    ModelUtils.documentOffsetRange(parserResult, lNode.getStart(), lNode.getFinish())));
        }
        return name;
    }

    private List<Identifier> getName(VarNode varNode) {
        List<Identifier> name = new ArrayList();
        name.add(new IdentifierImpl(varNode.getName().getName(),
                ModelUtils.documentOffsetRange(parserResult, varNode.getName().getStart(), varNode.getName().getFinish())));
        return name;
    }

    private List<Identifier> getName(BinaryNode binaryNode) {
        List<Identifier> name = new ArrayList();
        Node lhs = binaryNode.lhs();
        if (lhs instanceof AccessNode) {
            name = getName((AccessNode)lhs);
        } else if (lhs instanceof IdentNode) {
            IdentNode ident = (IdentNode) lhs;
            name.add(new IdentifierImpl(ident.getName(),
                        ModelUtils.documentOffsetRange(parserResult, ident.getStart(), ident.getFinish())));
        } else if (lhs instanceof IndexNode) {
            IndexNode indexNode = (IndexNode)lhs;
            if (indexNode.getBase() instanceof AccessNode) {
                name.addAll(getName((AccessNode)indexNode.getBase()));
            }
            if (indexNode.getIndex() instanceof LiteralNode) {
                LiteralNode lNode = (LiteralNode)indexNode.getIndex();
                name.add(new IdentifierImpl(lNode.getPropertyName(), 
                        ModelUtils.documentOffsetRange(parserResult, lNode.getStart(), lNode.getFinish())));
            }
        }
        return name;
    }

    private List<Identifier> getName(AccessNode aNode) {
        List<Identifier> name = new ArrayList();
        name.add(new IdentifierImpl(aNode.getProperty().getName(),
                ModelUtils.documentOffsetRange(parserResult, aNode.getProperty().getStart(), aNode.getProperty().getFinish())));
        while (aNode.getBase() instanceof AccessNode) {
            aNode = (AccessNode) aNode.getBase();
            name.add(new IdentifierImpl(aNode.getProperty().getName(),
                    ModelUtils.documentOffsetRange(parserResult, aNode.getProperty().getStart(), aNode.getProperty().getFinish())));
        }
        if (name.size() > 0 && aNode.getBase() instanceof IdentNode) {
            IdentNode ident = (IdentNode) aNode.getBase();
            if (!"this".equals(ident.getName())) {
                name.add(new IdentifierImpl(ident.getName(),
                        ModelUtils.documentOffsetRange(parserResult, ident.getStart(), ident.getFinish())));
            }
        }
        Collections.reverse(name);
        return name;
    }

//    private Variable findVarWithName(final Scope scope, final String name) {
//        Variable result = null;
//        Collection<Variable> variables = ScopeImpl.filter(scope.getElements(), new ScopeImpl.ElementFilter() {
//
//            @Override
//            public boolean isAccepted(ModelElement element) {
//                return element.getJSKind().equals(JsElement.Kind.VARIABLE)
//                        && element.getName().equals(name);
//            }
//        });
//
//        if (!variables.isEmpty()) {
//            result = variables.iterator().next();
//        } else {
//            if (!(scope instanceof FileScope)) {
//                result = findVarWithName((Scope)scope.getInElement(), name);
//            }
//        }
//
//        return result;
//    }
//
//    private Field findFieldWithName(FunctionScope function, final String name) {
//        Field result = null;
//        Collection<? extends Field> fields = function.getFields();
//        result = ModelUtils.getFirst(ModelUtils.getFirst(fields, name));
//        if (result == null && function.getInElement() instanceof FunctionScope) {
//            FunctionScope parent = (FunctionScope)function.getInElement();
//            fields = parent.getFields();
//            result = ModelUtils.getFirst(ModelUtils.getFirst(fields, name));
//        }
//        return result;
//    }

    private boolean isInPropertyNode() {
        boolean inFunction = false;
        for (int i = getPath().size() - 1; i > 0 ; i--) {
            final Node node = getPath().get(i);
            if(node instanceof FunctionNode) {
                if (!inFunction) {
                    inFunction = true;
                } else {
                    return false;
                }
            } else if (node instanceof PropertyNode) {
                return true;
            }
        }
        return false;
    }

    private void addOccurence(IdentNode iNode) {
        DeclarationScope scope = modelBuilder.getCurrentDeclarationScope();
        JsObject property = null;
        while (scope != null && property == null) {
            JsFunction function = (JsFunction)scope;
            property = function.getParameter(iNode.getName());
            if (property == null) {
                property = function.getProperty(iNode.getName());
            }
            scope = scope.getInScope();
        }
        if (property != null) {
            ((JsObjectImpl)property).addOccurrence(ModelUtils.documentOffsetRange(parserResult, iNode.getStart(), iNode.getFinish()));
        }
    }
    
    private Node getPreviousFromPath(int back) {
        int size = getPath().size();
        if (size >= back) {
            return getPath().get(size - back);
        }
        return null;
    }    
}
