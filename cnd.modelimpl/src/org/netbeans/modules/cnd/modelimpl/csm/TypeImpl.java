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

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameterType;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionStatementImpl;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver.SafeTemplateBasedProvider;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.ResolverFactory;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.impl.services.InstantiationProviderImpl;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.parser.FakeAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDProviderIml;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.TextCache;
import org.openide.util.CharSequences;

/**
 *
 * @author Vladimir Kvashin
 */
public class TypeImpl extends OffsetableBase implements CsmType, SafeTemplateBasedProvider {
    private static final byte FLAGS_TYPE_OF_TYPEDEF = 1;
    private static final byte FLAGS_REFERENCE = 1 << 1;
    private static final byte FLAGS_CONST = 1 << 2;
    private static final byte FLAGS_TYPE_WITH_CLASSIFIER = 1 << 3;
    private static final byte FLAGS_RVALREFERENCE = 1 << 4;
    protected static final int LAST_USED_FLAG_INDEX = 5;
    private static final CharSequence NON_INITIALIZED_CLASSIFIER_TEXT = CharSequences.empty();

    private final byte pointerDepth;
    private final byte arrayDepth;
    private byte flags;
    private CharSequence classifierText;
    private volatile CachePair lastCache = EMPTY_CACHE_PAIR;

    // lazy initialization here, add new params only with addInstantiationParam method
    private ArrayList<CsmSpecializationParameter> instantiationParams = null;

    // FIX for lazy resolver calls
    private CharSequence[] qname = null;
    private CsmUID<CsmClassifier> classifierUID;

    // package-local - for facory only
    TypeImpl(CsmClassifier classifier, int pointerDepth, int reference, int arrayDepth, AST ast, CsmFile file, int startOffset, int endOffset) {
        super(file, startOffset, endOffset);
        this.initClassifier(classifier);
        this.pointerDepth = (byte) pointerDepth;
        // nothing, & or && as reference specifier
        // too slow my typing is too slow
        assert reference >= 0 && reference <=2 : "unexpected " + reference;
        setFlags(FLAGS_REFERENCE, reference > 0);
        setFlags(FLAGS_RVALREFERENCE, reference == 2);
        this.arrayDepth = (byte) arrayDepth;
        boolean _const = isTypeDefAST(ast) ? initIsConst(ast.getFirstChild()) : initIsConst(ast);
        setFlags(FLAGS_CONST, _const);
        if (classifier == null) {
            CndUtils.assertTrueInConsole(false, "why null classifier?");
            this.initClassifier(initClassifier(ast));
            this.classifierText = initClassifierText(ast);
        } else {
            setFlags(FLAGS_TYPE_WITH_CLASSIFIER, true);
            CharSequence typeName = classifier.getName();
            if (typeName == null || typeName.length()==0){
                this.classifierText = initClassifierText(ast);
            } else {
                this.classifierText = typeName;
            }
        }
        if (this.classifierText == null) {
            CndUtils.assertTrueInConsole(false, "why null classifierText?"+classifier);
            this.classifierText = NON_INITIALIZED_CLASSIFIER_TEXT;
        }
        trimInstantiationParams();
    }
    
    // package-local - for facory only
    TypeImpl(CsmClassifier classifier, int pointerDepth, int reference, int arrayDepth, boolean _const, CsmFile file, int startOffset, int endOffset) {
        super(file, startOffset, endOffset);
        this.initClassifier(classifier);
        this.pointerDepth = (byte) pointerDepth;
        // nothing, & or && as reference specifier
        // too slow my typing is too slow
        assert reference >= 0 && reference <= 2 : "unexpected " + reference;
        setFlags(FLAGS_REFERENCE, reference > 0);
        setFlags(FLAGS_RVALREFERENCE, reference == 2);
        this.arrayDepth = (byte) arrayDepth;
        setFlags(FLAGS_CONST, _const);
        setFlags(FLAGS_TYPE_WITH_CLASSIFIER, true);
        this.classifierText = classifier.getName();
        trimInstantiationParams();
    }    

    // package-local - for facory only
    TypeImpl(CsmFile file, int pointerDepth, int reference, int arrayDepth, boolean _const, int startOffset, int endOffset) {
        super(file, startOffset, endOffset);
        this.classifierText = NON_INITIALIZED_CLASSIFIER_TEXT;
        this.pointerDepth = (byte) pointerDepth;
        // nothing, & or && as reference specifier
        // too slow my typing is too slow
        assert reference >= 0 && reference <= 2 : "unexpected " + reference;
        setFlags(FLAGS_REFERENCE, reference > 0);
        setFlags(FLAGS_RVALREFERENCE, reference == 2);
        this.arrayDepth = (byte) arrayDepth;
        setFlags(FLAGS_CONST, _const);
        trimInstantiationParams();
    }

    // package-local - for factory only
    TypeImpl(TypeImpl type, int pointerDepth, int reference, int arrayDepth, boolean _const) {
        super(type.getContainingFile(), type.getStartOffset(), type.getEndOffset());

        this.pointerDepth = (byte) pointerDepth;
        // nothing, & or && as reference specifier
        // too slow my typing is too slow
        assert reference >= 0 && reference <= 2 : "unexpected " + reference;
        setFlags(FLAGS_REFERENCE, reference > 0);
        setFlags(FLAGS_RVALREFERENCE, reference == 2);
        this.arrayDepth = (byte) arrayDepth;
        setFlags(FLAGS_CONST, _const);
        setFlags(FLAGS_TYPE_OF_TYPEDEF, type.isTypeOfTypedef());
        
        this.classifierUID = type.classifierUID;
        this.qname = type.qname;
        this.classifierText = type.classifierText;
        addAllInstantiationParams(type.instantiationParams);
        trimInstantiationParams();
    }

    public void setTypeOfTypedef() {
        setFlags(FLAGS_TYPE_OF_TYPEDEF, true);
    }

    protected boolean hasFlags(byte mask) {
        return (flags & mask) == mask;
    }

    private void setFlags(byte mask, boolean value) {
        if (value) {
            flags |= mask;
        } else {
            flags &= ~mask;
        }
    }

    private static boolean isTypeDefAST(AST ast){
        if (ast != null ) {
            if (ast.getType() == CPPTokenTypes.CSM_FIELD ||
                ast.getType() == CPPTokenTypes.CSM_GENERIC_DECLARATION) {
                return true;
            }
        }
        return false;
    }

    // package-local - for factory only
    TypeImpl(TypeImpl type, List<CsmSpecializationParameter> instantiationParams) {
        super(type.getContainingFile(), type.getStartOffset(), type.getEndOffset());

        this.pointerDepth = (byte) type.getPointerDepth();
        setFlags(FLAGS_REFERENCE, type.isReference());
        this.arrayDepth = (byte) type.getArrayDepth();
        setFlags(FLAGS_CONST, type.isConst());
        setFlags(FLAGS_TYPE_OF_TYPEDEF, type.isTypeOfTypedef());
        setFlags(FLAGS_TYPE_WITH_CLASSIFIER, type.isTypeWithClassifier());

        this.classifierUID = type.classifierUID;
        this.qname = type.qname;
        this.classifierText = type.classifierText;
        addAllInstantiationParams(instantiationParams);
        trimInstantiationParams();
    }


    // package-local
    TypeImpl(CsmType type) {
        super(type.getContainingFile(), type.getStartOffset(), type.getEndOffset());

        this.pointerDepth = (byte) type.getPointerDepth();
        setFlags(FLAGS_REFERENCE, type.isReference());
        this.arrayDepth = (byte) type.getArrayDepth();
        setFlags(FLAGS_CONST, type.isConst());

        if (type instanceof TypeImpl) {
            TypeImpl ti = (TypeImpl) type;
            setFlags(FLAGS_TYPE_OF_TYPEDEF, ti.isTypeOfTypedef());
            setFlags(FLAGS_TYPE_WITH_CLASSIFIER, ti.isTypeWithClassifier());
            this.classifierUID = ti.classifierUID;
            this.qname = ti.qname;
            this.classifierText = ti.classifierText;
            addAllInstantiationParams(ti.instantiationParams);
        }
        trimInstantiationParams();
    }

     /*TypeImpl(AST ast, CsmFile file, int pointerDepth, boolean reference, int arrayDepth) {
        this(null, pointerDepth, reference, arrayDepth, ast, file, null);
     }*/

    public static int getEndOffset(AST node) {
        AST ast = node;
        if( ast == null ) {
            return 0;
        }
        if (isTypeDefAST(ast)) {
            return OffsetableBase.getEndOffset(ast);
        }
        ast = getLastNode(ast);
        if( ast instanceof CsmAST ) {
            return ((CsmAST) ast).getEndOffset();
        }
        return OffsetableBase.getEndOffset(node);
    }

    private static AST getLastNode(AST first) {
        AST last = first;
        if(last != null) {
            for( AST token = last.getNextSibling(); token != null; token = token.getNextSibling() ) {
                switch( token.getType() ) {
                    case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                    case CPPTokenTypes.CSM_VARIABLE_LIKE_FUNCTION_DECLARATION:
                    case CPPTokenTypes.CSM_QUALIFIED_ID:
                    case CPPTokenTypes.CSM_ARRAY_DECLARATION:
                        return AstUtil.getLastChildRecursively(last);
                    default:
                        last = token;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isReference() {
        return hasFlags(FLAGS_REFERENCE);
    }

    @Override
    public boolean isRValueReference() {
        return hasFlags(FLAGS_RVALREFERENCE);
    }
    
    @Override
    public boolean isPointer() {
        return pointerDepth > 0;
    }

    private boolean isTypeOfTypedef() {
        return hasFlags(FLAGS_TYPE_OF_TYPEDEF);
    }

    private boolean isTypeWithClassifier() {
        return hasFlags(FLAGS_TYPE_WITH_CLASSIFIER);
    }

    @Override
    public List<CsmSpecializationParameter> getInstantiationParams() {
        return instantiationParams == null ? Collections.<CsmSpecializationParameter>emptyList() : instantiationParams;
    }

    @Override
    public boolean isInstantiation() {
        return instantiationParams != null;
    }

    /** Though it returns the same for now, it's better if its name differs */
    protected boolean isInstantiationOrSpecialization() {
        return instantiationParams != null;
    }
    
    final void trimInstantiationParams() {
        if (instantiationParams != null) {
            instantiationParams.trimToSize();
        }
    }
    
    final void addAllInstantiationParams(Collection<CsmSpecializationParameter> params) {
        if (params == null || params.isEmpty()) {
            return;
        }
        if (instantiationParams == null) {
            instantiationParams = new ArrayList<CsmSpecializationParameter>(params.size());
        }
        instantiationParams.addAll(params);
    }
    
    final void addInstantiationParam(CsmSpecializationParameter param) {
        if (instantiationParams == null) {
            instantiationParams = new ArrayList<CsmSpecializationParameter>();
        }
        instantiationParams.add(param);
    }

    @Override
    public boolean isTemplateBased() {
        return isTemplateBased(new HashSet<CsmType>());
    }

    @Override
    public boolean isTemplateBased(Set<CsmType> visited) {
        CsmClassifier classifier = getClassifier();
        if (CsmKindUtilities.isTypedef(classifier)) {
            if (visited.contains(this)) {
                return false;
            }
            visited.add(this);
            CsmType type = ((CsmTypedef)classifier).getType();
            if (type instanceof SafeTemplateBasedProvider) {
                return ((SafeTemplateBasedProvider)type).isTemplateBased(visited);
            } else {
                return type.isTemplateBased();
            }
        }
        return false;
    }

    public static boolean initIsConst(AST node) {
        if( node != null ) {
            for( AST token = node; token != null; token = token.getNextSibling() ) {
                int tokenType = token.getType();
                if (AstRenderer.isConstQualifier(tokenType)) {
                    return true;
                } else if (tokenType == CPPTokenTypes.CSM_VARIABLE_DECLARATION ||
                           tokenType == CPPTokenTypes.CSM_ARRAY_DECLARATION ||
                               tokenType == CPPTokenTypes.CSM_QUALIFIED_ID) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isConst() {
        return hasFlags(FLAGS_CONST);
    }

    @Override
    public CharSequence getCanonicalText() {
        CharSequence text = getClassifierText();
        if (isInstantiationOrSpecialization()) {
            text = text.toString() + Instantiation.getInstantiationCanonicalText(this.instantiationParams);
        }
	return decorateText(text, this, true, null);
    }

    /*package*/static CharSequence getCanonicalText(CsmType type) {
        CharSequence canonicalText = null;
        if (type instanceof CsmTemplateParameterType) {
            CsmTemplateParameterType parType = (CsmTemplateParameterType) type;
            CsmTemplateParameter par = parType.getParameter();
            if (CsmKindUtilities.isClassifierBasedTemplateParameter(par)) {
                canonicalText = TemplateUtils.TYPENAME_STRING;
            }
        }
        if (canonicalText == null) {
            canonicalText = type.getCanonicalText().toString();
        }
        return canonicalText;
    }


    // package
    public CharSequence getOwnText() {
        if (qname != null && qname.length>0) {
            return qname[qname.length-1];
        } else {
            return "";
        }
    }

    @Override
    public CharSequence getText() {
	// TODO: resolve typedefs
        CharSequence instantiationText = getInstantiationText(this);
        if (instantiationText.length() == 0) {
            return decorateText(getClassifierText(), this, false, null);
        } else {
            return decorateText(getClassifierText().toString() + instantiationText, this, false, null);
        }
    }

    protected CharSequence getText(boolean canonical, CharSequence variableNameToInsert) {
        CharSequence instantiationText = getInstantiationText(this);
        if (instantiationText.length() == 0) {
            return decorateText(getClassifierText(), this, canonical, variableNameToInsert);
        } else {
            return decorateText(getClassifierText().toString()  + instantiationText, this, canonical, variableNameToInsert);
        }
    }

    public CharSequence decorateText(CharSequence classifierText, CsmType decorator, boolean canonical, CharSequence variableNameToInsert) {
        if (decorator.isConst() || decorator.getPointerDepth() > 0 || 
            decorator.isReference() || decorator.getArrayDepth() > 0 ||
            variableNameToInsert != null) {
            StringBuilder sb = new StringBuilder();
            if( decorator.isConst() ) {
                sb.append("const "); // NOI18N
            }
            sb.append(classifierText);
            for( int i = 0; i < decorator.getPointerDepth(); i++ ) {
                sb.append('*');
            }
            if (decorator.isRValueReference()) {
                sb.append("&&"); // NOI18N
            } else if( decorator.isReference() ) {
                sb.append('&');
            }
            if(canonical) {
                for( int i = 0; i < decorator.getArrayDepth(); i++ ) {
                    sb.append("*"); // NOI18N
                }
                if (variableNameToInsert != null && !(variableNameToInsert.length() == 0)) {
                    sb.append(' ');
                    sb.append(variableNameToInsert);
                }
            } else {
                if (variableNameToInsert != null && !(variableNameToInsert.length() == 0)) {
                    sb.append(' ');
                    sb.append(variableNameToInsert);
                }              
                for( int i = 0; i < decorator.getArrayDepth(); i++ ) {
                    sb.append("[]"); // NOI18N
                }
            }
            return sb;
        }
        return classifierText;
    }

    final CharSequence initClassifierText(AST node) {
        if( node == null ) {
            CsmClassifier classifier = _getClassifier();
            return classifier == null ? "" : classifier.getName();
        }
        else {
            StringBuilder sb = new StringBuilder();
            addText(sb, AstRenderer.getFirstSiblingSkipQualifiers(node));
            return TextCache.getManager().getString(sb.toString());
//            return sb.toString();
        }
    }

    /*
     * Add text without instantiation params
     */
    private static void addText(StringBuilder sb, AST ast) {
        if( ! (ast instanceof FakeAST) ) {
            if( sb.length() > 0 ) {
                sb.append(' ');
            }
            sb.append(AstUtil.getText(ast));
        }
        int curDepth = 0;
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            if (token.getType() == CPPTokenTypes.LESSTHAN) {
                curDepth++;
                continue;
            } else if (token.getType() == CPPTokenTypes.GREATERTHAN) {
                curDepth--;
                continue;
            }
            if (curDepth == 0) {
                addText(sb,  token);
            }
        }
    }

    public static CharSequence getInstantiationText(CsmType type) {
        if (!type.getInstantiationParams().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append('<');
            boolean first = true;
            for (CsmSpecializationParameter param : type.getInstantiationParams()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(',');
                }
                sb.append(param.getText());
            }
            TemplateUtils.addGREATERTHAN(sb);
            return sb;
        }
	return CharSequences.empty();
    }

    @Override
    public CharSequence getClassifierText() {
        return classifierText;
    }

    boolean isInitedClassifierText() {
        return classifierText != NON_INITIALIZED_CLASSIFIER_TEXT;
    }

    void setClassifierText(CharSequence classifierText) {
        this.classifierText = classifierText;
    }

    void setQName(CharSequence[] qname) {
        this.qname = qname;
    }

    @Override
    public CsmClassifier getClassifier() {
        return getClassifier(null, false);
    }
    
    public CsmClassifier getClassifier(List<CsmInstantiation> instantiations, boolean specialize) {
        CsmClassifier classifier = _getClassifier();
        boolean needToRender = true;
        if (CsmBaseUtilities.isValid(classifier)) {
            // skip
            needToRender = false;
            Resolver parent = ResolverFactory.getCurrentResolver();
            if (!isTypeWithClassifier() && (qname != null) && (parent != null) && !CsmKindUtilities.isBuiltIn(classifier)) {
                // check visibility of classifier
                if (ForwardClass.isForwardClass(classifier) || !CsmIncludeResolver.getDefault().isObjectVisible(parent.getStartFile(), classifier)) {
                    needToRender = true;
                    classifier = null;
                }
            }
        }
        if (needToRender) {
            CachePair newCachePair = new CachePair(FileImpl.getParseCount(), ResolverFactory.getCurrentStartFile(this));
            if (classifier == null || !newCachePair.equals(lastCache)) {                
                if (qname != null) {
                    classifier = renderClassifier(qname);
                } else if (classifierText.length() > 0) {
                    classifier = renderClassifier(new CharSequence[] { classifierText });
                }
                synchronized (this) {
                    _setClassifier(classifier);
                    lastCache = newCachePair;
                }
                classifier = _getClassifier();
            }
        }
        if (isInstantiation() && CsmKindUtilities.isTemplate(classifier) && !((CsmTemplate)classifier).getTemplateParameters().isEmpty()) {
            CsmInstantiationProvider ip = CsmInstantiationProvider.getDefault();
            CsmObject obj;
            if (ip instanceof InstantiationProviderImpl) {
                Resolver resolver = ResolverFactory.createResolver(this);
                try {
                    if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                        obj = ((InstantiationProviderImpl) ip).instantiate((CsmTemplate) classifier, this, specialize);
                        if(CsmKindUtilities.isInstantiation(obj)) {
                            if (instantiations == null) {
                                instantiations = new ArrayList<CsmInstantiation>();
                            }
                            instantiations.add((CsmInstantiation)obj);
                        }
                    } else {
                        return null;
                    }
                } finally {
                    ResolverFactory.releaseResolver(resolver);
                }
            } else {
                obj = ip.instantiate((CsmTemplate) classifier, this);
            }
            if (CsmKindUtilities.isClassifier(obj)) {
                obj = specialize((CsmClassifier) obj, instantiations);
                classifier = (CsmClassifier) obj;
            }
        }
        return classifier;
    }

    public CsmObject specialize(CsmClassifier classifier, List<CsmInstantiation> instantiations) {
        CsmObject obj = classifier;
        if(instantiations != null && !instantiations.isEmpty()) {
            List<CsmInstantiation> originalInstantiations = new ArrayList<CsmInstantiation>();
            while (CsmKindUtilities.isInstantiation(obj)) {
                originalInstantiations.add((CsmInstantiation)obj);
                obj = ((CsmInstantiation)obj).getTemplateDeclaration();
            }

            CsmInstantiationProvider ip = CsmInstantiationProvider.getDefault();
            if (ip instanceof InstantiationProviderImpl) {
                Resolver resolver = ResolverFactory.createResolver(this);
                try {
                    if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                        for (int i = instantiations.size() - 1; i > 0; i--) {
                            obj = ((InstantiationProviderImpl) ip).instantiate((CsmTemplate) obj, instantiations.get(i), false);
                        }
                        obj = ((InstantiationProviderImpl) ip).instantiate((CsmTemplate) obj, instantiations.get(0), true);
                    }
                } finally {
                    ResolverFactory.releaseResolver(resolver);
                }
            }

            if(!CsmKindUtilities.isSpecialization(obj)) {
                while (CsmKindUtilities.isInstantiation(obj)) {
                    obj = ((CsmInstantiation)obj).getTemplateDeclaration();
                }
                if (ip instanceof InstantiationProviderImpl) {
                    Resolver resolver = ResolverFactory.createResolver(this);
                    try {
                        if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                            for (int i = originalInstantiations.size() - 1; i >= 0; i--) {
                                obj = ((InstantiationProviderImpl) ip).instantiate((CsmTemplate) obj, originalInstantiations.get(i), false);
                            }
                        }
                    } finally {
                        ResolverFactory.releaseResolver(resolver);
                    }
                }
            }
        }
        return obj;
    }     
    
    protected CsmClassifier renderClassifier(CharSequence[] qname) {
        CsmClassifier result = null;
        if (!isValid()) {
            return result;
        }
        Resolver resolver = ResolverFactory.createResolver(this);
        try {
            if (isInstantiationOrSpecialization()) {
                CharSequence[] specializationQname = new CharSequence[qname.length];
                final int last = qname.length - 1;
                StringBuilder sb = new StringBuilder(qname[last]);
                sb.append(Instantiation.getInstantiationCanonicalText(this.instantiationParams));
                specializationQname[last] = sb.toString();
                System.arraycopy(qname, 0, specializationQname, 0, last);
                CsmObject o = resolver.resolve(specializationQname, Resolver.CLASSIFIER);
                if( CsmKindUtilities.isClassifier(o) ) {
                    result = (CsmClassifier) o;
                }
                if (result == null) {
                    specializationQname[last] = qname[last].toString() + "<>"; //NOI18N
                    o = resolver.resolve(specializationQname, Resolver.CLASSIFIER);
                    if( CsmKindUtilities.isClassifier(o) ) {
                        result = (CsmClassifier) o;
                    }
                }
            }
            if (result == null) {
                CsmObject o = resolver.resolve(qname, Resolver.CLASSIFIER);
                if( CsmKindUtilities.isClassifier(o) ) {
                    result = (CsmClassifier) o;
                }
            }
        } finally {
            ResolverFactory.releaseResolver(resolver);
        }
        if( result == null ) {
            result = ProjectBase.getDummyForUnresolved(qname, this);
        }
        return result;
    }

    private CsmClassifier initClassifier(AST node) {
        AST tokType = AstRenderer.getFirstSiblingSkipQualifiers(node);
        if (tokType == null ||
                (tokType.getType() != CPPTokenTypes.CSM_TYPE_BUILTIN &&
                tokType.getType() != CPPTokenTypes.CSM_TYPE_COMPOUND) &&
                tokType.getType() != CPPTokenTypes.CSM_QUALIFIED_ID) {
            return null;
        }

        if (tokType.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN) {
            return BuiltinTypes.getBuiltIn(tokType);
        } else { // tokType.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND
            try {
                CsmAST tokFirstId = (CsmAST) tokType.getFirstChild();
                if (tokFirstId == null) {
                    // this is unnormal; but we should be able to work even on incorrect AST
                    return null;
                }

                //Resolver resolver = ResolverFactory.createResolver(getContainingFile(), firstOffset);
                // gather name components into string array
                // for example, for std::vector new CharSequence[] { "std", "vector" }

                //TODO: we have AstRenderer.getNameTokens, it is better to use it here
                List<CharSequence> l = new ArrayList<CharSequence>();
                int templateDepth = 0;
                for (AST namePart = tokFirstId; namePart != null; namePart = namePart.getNextSibling()) {
                    if (templateDepth == 0 && namePart.getType() == CPPTokenTypes.IDENT) {
                        l.add(NameCache.getManager().getString(AstUtil.getText(namePart)));
                    } else if (namePart.getType() == CPPTokenTypes.LESSTHAN) {
                        // the beginning of template parameters
                        templateDepth++;
                    } else if (namePart.getType() == CPPTokenTypes.GREATERTHAN) {
                        // the beginning of template parameters
                        templateDepth--;
                    } else {
                        //assert namePart.getType() == CPPTokenTypes.SCOPE;
                        if (templateDepth == 0) {
                            if (namePart.getType() != CPPTokenTypes.SCOPE) {
                                if (TraceFlags.DEBUG) {
                                    StringBuilder tokenText = new StringBuilder();
                                    tokenText.append('[').append(AstUtil.getText(namePart));
                                    if (namePart.getNumberOfChildren() == 0) {
                                        tokenText.append(", line=").append(namePart.getLine()); // NOI18N
                                        tokenText.append(", column=").append(namePart.getColumn()); // NOI18N
                                    }
                                    tokenText.append(']');
                                    System.err.println("Incorect token: expected '::', found " + tokenText.toString());
                                }
                            }
                        } else {
                            // TODO: maybe we need to filter out some more tokens
                            if (namePart.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN
                                    || namePart.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND
                                    || namePart.getType() == CPPTokenTypes.LITERAL_struct) {
                                CsmType type = AstRenderer.renderType(namePart, getContainingFile(), true);
                                addInstantiationParam(new TypeBasedSpecializationParameterImpl(type));
                            }
                            if (namePart.getType() == CPPTokenTypes.CSM_EXPRESSION) {
                                addInstantiationParam(ExpressionBasedSpecializationParameterImpl.create(ExpressionStatementImpl.create(namePart, getContainingFile(), null),
                                        getContainingFile(), OffsetableBase.getStartOffset(namePart), OffsetableBase.getEndOffset(namePart)));
                            }
                        }
                    }
                }
                qname = l.toArray(new CharSequence[l.size()]);
            /*CsmObject o = resolver.resolve(qname);
            if( CsmKindUtilities.isClassifier(o) ) {
            result = (CsmClassifier) o;
            }
            //		else if( CsmKindUtilities.isTypedef(o) ) {
            //		    CsmTypedef td = (CsmTypedef) o;
            //		    CsmType type = td.getType();
            //		    if( type != null ) {
            //			result = type.getClassifier();
            //		    }
            //		}
            if( result == null ) {
            result = ((ProjectBase) getContainingFile().getProject()).getDummyForUnresolved(qname, getContainingFile(), offset);
            }*/
            } catch (Exception e) {
                DiagnosticExceptoins.register(e);
            }
        }
        return null;
    }

    @Override
    public int getArrayDepth() {
        return arrayDepth;
    }

    @Override
    public int getPointerDepth() {
        return pointerDepth;
    }

    protected CsmClassifier _getClassifier() {
        CsmClassifier classifier = null;
        if (classifierUID != null) {
            classifier = UIDCsmConverter.UIDtoDeclaration(classifierUID);
        } else {
            FileImpl file = (FileImpl) getContainingFile();
            CsmReference typeReference = file.getResolvedReference(new CsmTypeReferenceImpl(this));
            if (typeReference != null) {
                CsmObject referencedObject = typeReference.getReferencedObject();
                if (CsmKindUtilities.isClassifier(referencedObject)) {
                    classifier = (CsmClassifier) referencedObject;
                    //System.out.println("Hit "+classifier);
                }
            }
        }
        // can be null if cached one was removed
        return classifier;
    }

    public final void initClassifier(CsmClassifier classifier) {
        this.classifierUID = UIDCsmConverter.declarationToUID(classifier);
        assert (classifierUID != null || classifier == null);
    }

    final void _setClassifier(final CsmClassifier classifier) {
        // remove old cached value
        CsmFile csmFile = getContainingFile();
        if(csmFile instanceof FileImpl) {
            FileImpl fileImpl = (FileImpl) csmFile;
            fileImpl.removeResolvedReference(new CsmTypeReferenceImpl(this));
            CsmUID<CsmClassifier> cUID = UIDCsmConverter.declarationToUID(classifier);
            this.classifierUID = cUID;
            // register new cached value
            if (cUID != null && classifier != null && !CsmKindUtilities.isBuiltIn(classifier) && CsmBaseUtilities.isValid(classifier) && !CsmKindUtilities.isTypedef(classifier)
                //&& !CsmKindUtilities.isTemplate(classifier) && !isInstantiation()
               ) {
               fileImpl.addResolvedReference(new CsmTypeReferenceImpl(this), classifier);
            }
            assert (cUID != null || classifier == null);
        }
    }

    @Override
    public boolean isBuiltInBased(boolean resolveTypeChain) {
        CsmClassifier classifier;
        if (resolveTypeChain) {
            classifier = getClassifier();
            if (CsmKindUtilities.isTypedef(classifier)) {
                return ((CsmTypedef)classifier).getType().isBuiltInBased(true);
            }
        } else {
            classifier = _getClassifier();
        }
        return CsmKindUtilities.isBuiltIn(classifier);
    }


    @Override
    public String toString() {
        return "TYPE " + getText()  + getOffsetString(); // NOI18N
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        output.writeByte(pointerDepth);
        output.writeByte(arrayDepth);
        output.writeByte(flags);
        assert this.classifierText != null;
        PersistentUtils.writeUTF(classifierText, output);

        PersistentUtils.writeStrings(qname, output);
        PersistentUtils.writeSpecializationParameters(instantiationParams, output);
        
        CsmUID<?> uid = this.classifierUID;
        if(!UIDProviderIml.isPersistable(uid)) {
            uid = null;
        }
        UIDObjectFactory.getDefaultFactory().writeUID(uid, output);
    }
    
    // Proxy list to be able to work with null instantiationParams collection
    private class ProxyParamsList extends AbstractList<CsmSpecializationParameter> {
        @Override
        public boolean add(CsmSpecializationParameter e) {
            if (instantiationParams == null) {
                instantiationParams = new ArrayList<CsmSpecializationParameter>();
            }
            return instantiationParams.add(e);
        }
        
        @Override
        public CsmSpecializationParameter get(int index) {
            if (instantiationParams != null) {
                return instantiationParams.get(index);
            }
            throw new IndexOutOfBoundsException("Index: "+index+", Size: 0"); //NOI18N
        }

        @Override
        public int size() {
            if (instantiationParams != null) {
                return instantiationParams.size();
            }
            return 0;
        }
    }

    public TypeImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.pointerDepth = input.readByte();
        this.arrayDepth= input.readByte();
        this.flags = input.readByte();
        this.classifierText = PersistentUtils.readUTF(input, NameCache.getManager());
        assert this.classifierText != null;

        this.qname = PersistentUtils.readStrings(input, NameCache.getManager());
        PersistentUtils.readSpecializationParameters(new ProxyParamsList(), input);
        trimInstantiationParams();
        this.classifierUID = UIDObjectFactory.getDefaultFactory().readUID(input);
    }
    
    private final static CachePair EMPTY_CACHE_PAIR = new CachePair(-1, null);
    
    private static final class CachePair {
        private final int parseCount;
        private final CsmUID<CsmFile> fileUID;

        public CachePair(int parseCount, CsmUID<CsmFile> fileUID) {
            this.parseCount = parseCount;
            this.fileUID = fileUID;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CachePair other = (CachePair) obj;
            if (this.parseCount != other.parseCount) {
                return false;
            }
            if (this.fileUID != other.fileUID && (this.fileUID == null || !this.fileUID.equals(other.fileUID))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + this.parseCount;
            hash = 37 * hash + (this.fileUID != null ? this.fileUID.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "CachePair{" + "parseCount=" + parseCount + ", fileUID=" + fileUID + '}'; // NOI18N
        }
    }

    private static class CsmTypeReferenceImpl implements CsmReference {
        private final TypeImpl type;

        public CsmTypeReferenceImpl(TypeImpl type) {
            this.type = type;
        }

        @Override
        public CsmReferenceKind getKind() {
            return CsmReferenceKind.DIRECT_USAGE;
        }

        @Override
        public CsmObject getReferencedObject() {
            return null;
        }

        @Override
        public CsmObject getOwner() {
            return null;
        }

        @Override
        public CsmObject getClosestTopLevelObject() {
            return null;
        }

        @Override
        public CsmFile getContainingFile() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getStartOffset() {
            return type.getStartOffset();
        }

        @Override
        public int getEndOffset() {
            return type.getEndOffset();
        }

        @Override
        public Position getStartPosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Position getEndPosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getText() {
            return type.classifierText;
        }

        @Override
        public String toString() {
            return type.classifierText+"["+type.getStartOffset()+","+type.getEndOffset()+"]"; //NOI18N
        }
    }
}
