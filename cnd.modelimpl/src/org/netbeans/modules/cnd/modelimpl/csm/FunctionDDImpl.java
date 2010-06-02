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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.Disposable;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;

/**
 * Implements both CsmFunction and CsmFunctionDefinition -
 * for those cases, when they coinside (i.e. implivit inlines)
 * @author Vladimir Kvasihn
 */
public class FunctionDDImpl<T> extends FunctionImpl<T> implements CsmFunctionDefinition {

    private final CsmCompoundStatement body;

    public FunctionDDImpl(AST ast, CsmFile file, CsmScope scope, boolean global) throws AstRendererException {
        super(ast, file, scope, false, global);
        body = AstRenderer.findCompoundStatement(ast, getContainingFile(), this);
        if (body == null) {
            throw new AstRendererException((FileImpl)file, getStartOffset(),
                    "Null body in function definition."); // NOI18N
        }
        if (global) {
            registerInProject();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (body instanceof Disposable) {
            ((Disposable) body).dispose();
        }
    }

    @Override
    public CsmCompoundStatement getBody() {
        return body;
    }

    @Override
    public CsmFunction getDeclaration() {
        if( isCStyleStatic() ) {
            CharSequence name = getName();
            CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(
                               name, true, true, false);
            Iterator<CsmFunction> it = CsmSelect.getStaticFunctions(getContainingFile(), filter);
            while(it.hasNext()){
                CsmFunction fun = it.next();
                if( name.equals(fun.getName()) ) {
                    return fun;
                }
            }
            return this;
        }
        String uname = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION) + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix();
        CsmProject prj = getContainingFile().getProject();
        CsmDeclaration decl = findDeclaration(prj, uname);
        if (decl != null && decl.getKind() == CsmDeclaration.Kind.FUNCTION) {
            if (!isStatic() || CsmKindUtilities.isClassMember(this)
                    || !CsmKindUtilities.isOffsetableDeclaration(decl)
                    || getContainingFile().equals(((CsmOffsetableDeclaration)decl).getContainingFile())) {
                return (CsmFunction) decl;
            }
        }
        for (CsmProject lib : prj.getLibraries()) {
            CsmFunction def = findDeclaration(lib, uname);
            if (def != null) {
                return def;
            }
        }
        if(decl == null) {
            uname = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_FRIEND) + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix();
            decl = findDeclaration(prj, uname);
            if (decl != null && decl.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND) {
                if (!isStatic() || CsmKindUtilities.isClassMember(this)
                        || !CsmKindUtilities.isOffsetableDeclaration(decl)
                        || getContainingFile().equals(((CsmOffsetableDeclaration)decl).getContainingFile())) {
                    return (CsmFunction) decl;
                }
            }
            for (CsmProject lib : prj.getLibraries()) {
                CsmFunction def = findDeclaration(lib, uname);
                if (def != null) {
                    return def;
                }
            }
        }
        return this;
    }

    @Override
    public boolean isPureDefinition() {
        return false;
    }

    private CsmFunction findDeclaration(CsmProject prj, String uname){
        CsmDeclaration decl = null;
        for(CsmOffsetableDeclaration candidate : prj.findDeclarations(uname)) {
            if ((candidate.getKind() == CsmDeclaration.Kind.FUNCTION ||
                candidate.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND)) {
                if (FunctionImpl.isObjectVisibleInFile(getContainingFile(), candidate)) {
                    decl = candidate;
                    break;
                }
                if (decl == null) {
                    decl = candidate;
                }
            }
        }
        if( decl != null && (decl.getKind() == CsmDeclaration.Kind.FUNCTION ||
                decl.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND)) {
            return (CsmFunction) decl;
        }
        FunctionParameterListImpl parameterList = getParameterList();
        if (parameterList != null && !parameterList.isEmpty()) {
            CsmFile file = getContainingFile();
            if (!Utils.isCppFile(file)){
                uname = uname.substring(0,uname.indexOf('('))+"()"; // NOI18N
                decl = prj.findDeclaration(uname);
                if( (decl instanceof FunctionImpl<?>) &&
                        !((FunctionImpl<?>)decl).isVoidParameterList()) {
                    return (CsmFunction) decl;
                }
            }
        }
        return null;
    }

    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.FUNCTION_DEFINITION;
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        Collection<CsmScopeElement> l = super.getScopeElements();
        l.add(getBody());
        return l;
    }

    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent

    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.body != null: "null body in " + this.getQualifiedName();
        PersistentUtils.writeCompoundStatement(body, output);
    }

    public FunctionDDImpl(DataInput input) throws IOException {
        super(input);
        this.body = PersistentUtils.readCompoundStatement(input);
        assert this.body != null: "read null body for " + this.getName();
    }
}

