/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * under the [CDDL or GPL Version 2] license." If you do not indicate a * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.csm;

import java.io.IOException;
import java.util.List;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.services.CsmTypeResolver;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionsFactory;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.util.CharSequences;

/**
 *
 * @author petrk
 */
public class DeclTypeImpl extends TypeImpl {
    
    public static final String DECLTYPE = "decltype"; // NOI18N
        
    public static boolean isDeclType(AST node) {
        return node != null 
               && node.getFirstChild() != null
               && CharSequences.comparator().compare(AstUtil.getText(node.getFirstChild()), DECLTYPE) == 0
               && AstUtil.findChildOfType(node, CPPTokenTypes.CSM_EXPRESSION) != null;
    }    
    
    
    private final CsmExpression typeExpression;
    
    private transient List<CsmInstantiation> instantiations;
    

    DeclTypeImpl(AST ast, CsmFile file, CsmScope scope, int pointerDepth, int reference, int arrayDepth, int constQualifiers, int startOffset, int endOffset) {
        super(file, pointerDepth, reference, arrayDepth, constQualifiers, startOffset, endOffset);
        AST expressionAst = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_EXPRESSION);
        this.typeExpression = ExpressionsFactory.create(expressionAst, file, scope);
    }

    public CsmExpression getTypeExpression() {
        return typeExpression;
    }   

    @Override
    protected CsmClassifier _getClassifier() {
        CsmClassifier classifier = null;
        
        if (canUseCache()) {
            classifier = super._getClassifier();
        }
        
        if (classifier == null) {
            synchronized (this) {
                if (!isClassifierInitialized() || !canUseCache()) {
                    CsmType type = CsmTypeResolver.resolveType(typeExpression, instantiations);
                    classifier = type != null ? type.getClassifier() : null;
                    if (classifier == null) {
                        classifier = BuiltinTypes.getBuiltIn(DECLTYPE); // Unresolved?
                    }
                    if (canUseCache()) {
                        initClassifier(classifier);
                    }
                } else {
                    classifier = super._getClassifier();
                    assert (classifier != null);
                }
            }
        }
        return classifier;
    }
    
    @Override
    public CsmClassifier getClassifier(List<CsmInstantiation> instantiations, boolean specialize) {    
        CsmClassifier classifier;
        try {
            this.instantiations = instantiations;
            classifier = super.getClassifier(instantiations, specialize);
        } finally {
            this.instantiations = null;
        }
        return classifier;
    }    
    
    private boolean canUseCache() {
        // We are allowed to use cache only if context is null
        return instantiations == null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeExpression(typeExpression, output);
    }

    public DeclTypeImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.typeExpression = (ExpressionBase) PersistentUtils.readExpression(input);
        this.instantiations = null;
    }    
}
