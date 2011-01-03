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

import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.ResolverFactory;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;

/**
 * Implements CsmUsingDirective
 * @author Vladimir Kvasihn
 */
public final class UsingDirectiveImpl extends OffsetableDeclarationBase<CsmUsingDirective> implements CsmUsingDirective, RawNamable {

    private final CharSequence name;
    private final CharSequence[] rawName;
    // TODO: don't store declaration here since the instance might change
    private CsmUID<CsmNamespace> referencedNamespaceUID = null;
    
    private UsingDirectiveImpl(AST ast, CsmFile file) {
        super(file, ((CsmAST)ast.getFirstChild()).getOffset(), getEndOffset(ast));
        rawName = AstUtil.getRawNameInChildren(ast);
        name = NameCache.getManager().getString(AstUtil.getText(ast));
    }

    public static UsingDirectiveImpl create(AST ast, CsmFile file, boolean global) {
        UsingDirectiveImpl usingDirectiveImpl = new UsingDirectiveImpl(ast, file);
        if (!global) {
            Utils.setSelfUID(usingDirectiveImpl);
        }
        return usingDirectiveImpl;
    }
    
    @Override
    public CsmNamespace getReferencedNamespace() {
        // TODO: process preceding aliases
        CsmNamespace referencedNamespace = _getReferencedNamespace();
        if (referencedNamespace == null) {
            _setReferencedNamespace(null);
            CsmObject result = null;
            Resolver aResolver = ResolverFactory.createResolver(this);
            try {
                result = aResolver.resolve(Utils.splitQualifiedName(name.toString()), Resolver.NAMESPACE);
            } finally {
                ResolverFactory.releaseResolver(aResolver);
            }
            if (result != null && result instanceof CsmNamespaceDefinition) {
                result = ((CsmNamespaceDefinition)result).getNamespace();
            }
            if (CsmKindUtilities.isNamespace(result)) {
                referencedNamespace = (CsmNamespace)result;
                _setReferencedNamespace(referencedNamespace);
            }
        }
        return referencedNamespace;
    }
    
    private CsmNamespace _getReferencedNamespace() {
        // can be null if namespace was removed 
        return UIDCsmConverter.UIDtoNamespace(referencedNamespaceUID);
    }    

    private void _setReferencedNamespace(CsmNamespace referencedNamespace) {
        this.referencedNamespaceUID = UIDCsmConverter.namespaceToUID(referencedNamespace);
        assert this.referencedNamespaceUID != null || referencedNamespace == null;
    }
 
    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.USING_DIRECTIVE;
    }
    
    @Override
    public CharSequence getName() {
        return name;
    }
    
    @Override
    public CharSequence getQualifiedName() {
        return getName();
    }
    
    @Override
    public CharSequence[] getRawName() {
        return rawName;
    }
    
    @Override
    public CsmScope getScope() {
        //TODO: implement!
        return null;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        PersistentUtils.writeUTF(name, output);
        PersistentUtils.writeStrings(this.rawName, output);
        
        // save cached namespace
        UIDObjectFactory.getDefaultFactory().writeUID(this.referencedNamespaceUID, output);
    }
    
    public UsingDirectiveImpl(DataInput input) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, NameCache.getManager());
        assert this.name != null;
        this.rawName = PersistentUtils.readStrings(input, NameCache.getManager());
        
        // read cached namespace
        this.referencedNamespaceUID = UIDObjectFactory.getDefaultFactory().readUID(input);        
    }  
    
}
