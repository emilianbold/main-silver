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

import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.util.CharSequences;

/**
 * Implements 
 * @author Vladimir Kvashin
 */
public class ClassImplSpecialization extends ClassImpl implements CsmTemplate {

    private CharSequence qualifiedNameSuffix = CharSequences.empty();

    private SpecializationDescriptor specializationDesctiptor;

    protected ClassImplSpecialization(AST ast, NameHolder name, CsmFile file) {
        super(name, ast, file);
    }

    protected ClassImplSpecialization(AST ast, NameHolder name, CsmFile file, int start, int end) {
        super(name, ast, file, start, end);
    }

    @Override
    public final void init(CsmScope scope, AST ast, CsmFile file, FileContent fileContent, boolean register) {
        // does not call super.init(), but copies super.init() with some changes:
        // it needs to initialize qualifiedNameSuffix
        // after rendering, but before calling initQualifiedName() and register()

        initScope(scope);
        temporaryRepositoryRegistration(register, this);
        render(ast, file, fileContent, !register);

        initQualifiedName(ast, scope, register);

        if (register) {
            register(getScope(), false);
        }
    }

    protected final void initQualifiedName(AST ast, CsmScope scope, boolean register) {
        AST qIdToken = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
        assert qIdToken != null;
        qualifiedNameSuffix = NameCache.getManager().getString(TemplateUtils.getSpecializationSuffix(qIdToken, getTemplateParameters()));
        initQualifiedName(scope);
        specializationDesctiptor = SpecializationDescriptor.createIfNeeded(ast, getContainingFile(), scope, register);
    }

    public static ClassImplSpecialization create(AST ast, CsmScope scope, CsmFile file, FileContent fileContent, boolean register, DeclarationsContainer container) {
        ClassImpl clsImpl = findExistingClassImplInContainer(container, ast);
        ClassImplSpecialization impl = null;
        if (clsImpl instanceof ClassImplSpecialization) {
            // not our instance
            impl = (ClassImplSpecialization) clsImpl;
        }
        NameHolder nameHolder = null;
        if (impl == null) {
            nameHolder = NameHolder.createClassName(ast);
            impl = new ClassImplSpecialization(ast, nameHolder, file);
        }
        impl.init(scope, ast, file, fileContent, register);
        if (nameHolder != null) {
            nameHolder.addReference(fileContent, impl);
        }
        return impl;
    }

    @Override
    public boolean isTemplate() {
        return true;
    }

    @Override
    public boolean isSpecialization() {
        return true;
    }

    @Override
    public boolean isExplicitSpecialization() {
        return false;
    }

//    public String getTemplateSignature() {
//	return qualifiedNameSuffix;
//    }
// This does not work since the method is called from base class' constructor    
//    protected String getQualifiedNamePostfix() {
//	String qName = super.getQualifiedNamePostfix();
//	if( isSpecialization() ) {
//	    qName += qualifiedNameSuffix;
//	}
//	return qName;
//    }
    @Override
    public String getQualifiedNamePostfix() {
        return super.getQualifiedNamePostfix() + qualifiedNameSuffix.toString();
    }

    protected String getQualifiedNameWithoutSuffix() {
        CsmScope scope = getScope();
        String name = getName().toString();
        if (CsmKindUtilities.isNamespace(scope)) {
            return Utils.getQualifiedName(name, (CsmNamespace) scope);
        } else if (CsmKindUtilities.isClass(scope)) {            
            String n = name;
            if (name.contains("::")) { // NOI18N
                name = name.substring(name.lastIndexOf("::") + 2); // NOI18N
            }
            return ((CsmClass) scope).getQualifiedName() + "::" + name; // NOI18N
        } else {
             return name;
        }
    }

    public List<CsmSpecializationParameter> getSpecializationParameters() {
        return (specializationDesctiptor != null) ? specializationDesctiptor.getSpecializationParameters() : Collections.<CsmSpecializationParameter>emptyList();
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeUTF(qualifiedNameSuffix, output);
        PersistentUtils.writeSpecializationDescriptor(specializationDesctiptor, output);
    }

    public ClassImplSpecialization(RepositoryDataInput input) throws IOException {
        super(input);
        qualifiedNameSuffix = PersistentUtils.readUTF(input, NameCache.getManager());
        specializationDesctiptor = PersistentUtils.readSpecializationDescriptor(input);
    }

    @Override
    public String getDisplayName() {
        return getName() + qualifiedNameSuffix.toString();
    }
}
