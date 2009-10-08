/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.php.editor.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.model.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.php.editor.PredefinedSymbols;
import org.netbeans.modules.php.editor.model.nodes.FunctionDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.LambdaFunctionDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.MagicMethodDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.MethodDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;

/**
 *
 * @author Radek Matous
 */
class FunctionScopeImpl extends ScopeImpl implements FunctionScope, VariableNameFactory {
    private List<? extends Parameter> paremeters;
    String returnType;

    //new contructors
    FunctionScopeImpl(Scope inScope, FunctionDeclarationInfo info, String returnType) {
        super(inScope, info, new PhpModifiers(PhpModifiers.PUBLIC), info.getOriginalNode().getBody());
        this.paremeters = info.getParameters();
        this.returnType = returnType;
    }
    FunctionScopeImpl(Scope inScope, LambdaFunctionDeclarationInfo info) {
        super(inScope, info, new PhpModifiers(PhpModifiers.PUBLIC), info.getOriginalNode().getBody());
        this.paremeters = info.getParameters();
    }
    protected FunctionScopeImpl(Scope inScope, MethodDeclarationInfo info, String returnType) {
        super(inScope, info, info.getAccessModifiers(), info.getOriginalNode().getFunction().getBody());
        this.paremeters = info.getParameters();
        this.returnType = returnType;
    }

    protected FunctionScopeImpl(Scope inScope, MagicMethodDeclarationInfo info) {
        super(inScope, info, info.getAccessModifiers(), null);
        this.paremeters = info.getParameters();
        this.returnType = info.getReturnType();
    }

    FunctionScopeImpl(Scope inScope, IndexedFunction indexedFunction) {
        this(inScope, indexedFunction, PhpKind.FUNCTION);
    }

    protected FunctionScopeImpl(Scope inScope, final IndexedFunction element, PhpKind kind) {
        super(inScope, element, kind);
        this.paremeters = element.getParameters();
        this.returnType =  element.getReturnType();
    }

    public static FunctionScopeImpl createElement(Scope scope, LambdaFunctionDeclaration node) {
        return new FunctionScopeImpl(scope, LambdaFunctionDeclarationInfo.create(node)) {
            @Override
            public boolean isAnonymous() {
                return true;
            }
        };
    }

    //old contructors
    

    public final Collection<? extends TypeScope> getReturnTypes() {
        return getReturnTypes(false);
    }

    public Collection<? extends String> getReturnTypeNames() {
        Collection<String> retval = Collections.<String>emptyList();
        if (returnType != null && returnType.length() > 0) {
            retval = new ArrayList<String>();
            for (String typeName : returnType.split("\\|")) {//NOI18N
                if (!typeName.contains("@")) {//NOI18N
                    retval.add(typeName);
                }
            }
        }
        return retval;
    }

    private static Set<String> recursionDetection = new HashSet<String>();//#168868

    public Collection<? extends TypeScope> getReturnTypes(boolean resolve) {
        Collection<TypeScope> retval = Collections.<TypeScope>emptyList();
        if (returnType != null && returnType.length() > 0) {
            retval = new ArrayList<TypeScope>();
            for (String typeName : returnType.split("\\|")) {//NOI18N
                if (typeName.trim().length() > 0) {
                    if (resolve && typeName.contains("@")) {//NOI18N
                        try {
                            if (recursionDetection.add(typeName) && recursionDetection.size() < 30) {
                            retval.addAll(VariousUtils.getType(this, typeName, getOffset(), false));
                            }
                        } finally {
                            recursionDetection.remove(typeName);
                        }
                    } else {
                        retval.addAll(CachingSupport.getTypes(typeName, this));
                    }
                }
            }
            returnType = null;//NOI18N
            for (TypeScope typeScope : retval) {
                if (returnType == null) {
                    returnType = typeScope.getNamespaceName().append(typeScope.getName()).toString();
                } else {
                    returnType += "|"+typeScope.getNamespaceName().append(typeScope.getName()).toString();//NOI18N
                }
            }
        }
        return retval;
    }

    @NonNull
    public List<? extends String> getParameterNames() {
        assert paremeters != null;
        List<String> parameterNames = new ArrayList<String>();
        for (Parameter parameter : paremeters) {
            parameterNames.add(parameter.getName());
        }
        return parameterNames;
    }

    @NonNull
    public List<? extends Parameter> getParameters() {
        return paremeters;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Collection<? extends TypeScope> returnTypes = getReturnTypes();
        sb.append('[');
        for (TypeScope typeScope : returnTypes) {
            if (sb.length() == 1) {
                sb.append("|");
            }
            sb.append(typeScope.getName());
        }
        sb.append("] ");
        sb.append(super.toString()).append("(");
        List<? extends String> parameters = getParameterNames();
        for (int i = 0; i < parameters.size(); i++) {
            String param = parameters.get(i);
            if (i > 0) sb.append(",");
            sb.append(param);
        }
        sb.append(")");

        return sb.toString();
    }

    public Collection<? extends VariableName> getDeclaredVariables() {
        return filter(getElements(), new ElementFilter() {
            public boolean isAccepted(ModelElement element) {
                return element.getPhpKind().equals(PhpKind.VARIABLE);
            }
        });
    }

    public VariableNameImpl createElement(Variable node) {
        VariableNameImpl retval = new VariableNameImpl(this, node, false);
        addElement(retval);
        return retval;
    }
        
    @Override
    public String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(";");//NOI18N
        sb.append(getName()).append(";");//NOI18N
        sb.append(getOffset()).append(";");//NOI18N
        List<? extends Parameter> parameters = getParameters();
        for (int idx = 0; idx < parameters.size(); idx++) {
            Parameter parameter = parameters.get(idx);
            if (idx > 0) {
                sb.append(',');//NOI18N
            }
            sb.append(parameter.getIndexSignature());
            
        }
        sb.append(";");//NOI18N
        if (returnType != null && !PredefinedSymbols.MIXED_TYPE.equalsIgnoreCase(returnType)) {
            sb.append(returnType);
        }
        sb.append(";");//NOI18N
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(this);
        QualifiedName qualifiedName = namespaceScope.getQualifiedName();
        sb.append(qualifiedName.toString()).append(";");//NOI18N
        return sb.toString();
    }

    @Override
    public QualifiedName getNamespaceName() {
        if (indexedElement instanceof IndexedFunction) {
            IndexedFunction indexedFunction = (IndexedFunction)indexedElement;
            return QualifiedName.create(indexedFunction.getNamespaceName());
        }
        return super.getNamespaceName();
    }

    public boolean isAnonymous() {
        return false;
    }
}
