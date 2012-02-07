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
package org.netbeans.modules.cnd.modelimpl.parser;

import java.util.Map;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl.ClassBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.CsmObjectBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.EnumImpl;
import org.netbeans.modules.cnd.modelimpl.csm.EnumImpl.EnumBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.EnumeratorImpl.EnumeratorBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.TypeFactory;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceDefinitionImpl.NamespaceBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.parser.symtab.*;
import org.openide.util.CharSequences;

/**
 * @author Nikolay Krasilnikov (nnnnnk@netbeans.org)
 */
public class CppParserActionImpl implements CppParserAction {

    private enum CppAttributes implements SymTabEntryKey {
        SYM_TAB, DEFINITION, TYPE
    }
    
    private final SymTabStack globalSymTab;
    
    Map<Integer, CsmObject> objects;
    FileImpl file;

    CppParserBuilderContext builderContext;
    
    public CppParserActionImpl(CsmFile file, Map<Integer, CsmObject> objects) {
        assert objects != null;
        assert file != null;
        
        this.globalSymTab = createGlobal();
        this.objects = objects;
        this.file = (FileImpl) file;
        this.builderContext = new CppParserBuilderContext();
    }
    
    @Override
    public void enum_declaration(Token token) {        
        //System.out.println("enum_declaration " + ((APTToken)token).getOffset());
        
        EnumBuilder enumBuilder = new EnumBuilder();
        enumBuilder.setFile(file);
        if(token instanceof APTToken) {
            enumBuilder.setStartOffset(((APTToken)token).getOffset());
        }
        builderContext.push(enumBuilder);
    }

    @Override
    public void enum_name(Token token) {
        EnumBuilder enumBuilder = builderContext.getEnumBuilder();
        
        APTToken aToken = (APTToken) token;
        final CharSequence name = aToken.getTextID();
        SymTabEntry enumEntry = globalSymTab.lookupLocal(name);
        if (enumEntry == null) {
            enumEntry = globalSymTab.enterLocal(name);
            enumEntry.setAttribute(CppAttributes.TYPE, true);
        } else {
            // error
        }
        enumBuilder.setName(name);
    }

    @Override
    public void enum_body(Token token) {
        globalSymTab.push();
    }
    
    @Override
    public void enumerator(Token token) {
        EnumBuilder enumBuilder = builderContext.getEnumBuilder();
        
        APTToken aToken = (APTToken) token;
        final CharSequence name = aToken.getTextID();
        SymTabEntry enumeratorEntry = globalSymTab.lookupLocal(name);
        if (enumeratorEntry == null) {
            enumeratorEntry = globalSymTab.enterLocal(name);
//            enumeratorEntry.setAttribute(CppAttributes.SYM_TAB, globalSymTab.getLocal());
        } else {
            // ERROR redifinition
        }
        if(enumBuilder != null) {
            EnumeratorBuilder builder2 = new EnumeratorBuilder();
            builder2.setName(name);
            builder2.setFile(file);
            builder2.setStartOffset(aToken.getOffset());
            builder2.setEndOffset(aToken.getEndOffset());
            enumBuilder.addEnumerator(builder2);
        }
    }
    
    @Override
    public void end_enum_body(Token token) {
        EnumBuilder enumBuilder = builderContext.getEnumBuilder();
        
        if(token instanceof APTToken) {
            enumBuilder.setEndOffset(((APTToken)token).getEndOffset());
        }
        SymTab enumerators = globalSymTab.pop();
        globalSymTab.importToLocal(enumerators);
    }

    @Override
    public void end_enum_declaration(Token token) {
        EnumBuilder enumBuilder = builderContext.getEnumBuilder();
        
        EnumImpl e = enumBuilder.create(true);
        if(e != null) {
            objects.put(e.getStartOffset(), e);
            SymTabEntry enumEntry = globalSymTab.lookupLocal(e.getName());
            enumEntry.setAttribute(CppAttributes.DEFINITION, e);
            for (CsmEnumerator csmEnumerator : e.getEnumerators()) {
                SymTabEntry enumeratorEntry = globalSymTab.lookupLocal(csmEnumerator.getName());
                assert enumeratorEntry != null;
                enumeratorEntry.setAttribute(CppAttributes.DEFINITION, csmEnumerator);
            }
        }

        builderContext.pop();
    }
    
    @Override
    public void class_declaration(Token token) {
        ClassBuilder classBuilder = new ClassBuilder();
        classBuilder.setParent(builderContext.top());
        classBuilder.setFile(file);
        if(token instanceof APTToken) {
            classBuilder.setStartOffset(((APTToken)token).getOffset());
        }
        builderContext.push(classBuilder);
    }

    @Override
    public void class_kind(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof ClassBuilder) {
            ClassBuilder classBuilder = (ClassBuilder) top;
            Kind kind = Kind.CLASS;
            switch (token.getType()) {
                case CPPTokenTypes.LITERAL_class:
                    kind = Kind.CLASS;
                    break;
                case CPPTokenTypes.LITERAL_union:
                    kind = Kind.UNION;
                    break;
                case CPPTokenTypes.LITERAL_struct:
                    kind = Kind.STRUCT;
                    break;
            }
            classBuilder.setKind(kind);
        }
    }    
    
    @Override
    public void class_name(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof ClassBuilder) {
            ClassBuilder classBuilder = (ClassBuilder) top;
            APTToken aToken = (APTToken) token;
            final CharSequence name = aToken.getTextID();
            SymTabEntry classEntry = globalSymTab.lookupLocal(name);
            if (classEntry == null) {
                classEntry = globalSymTab.enterLocal(name);
                classEntry.setAttribute(CppAttributes.TYPE, true);
            } else {
                // error
            }

            classBuilder.setName(name);
        }
    }
    
    @Override
    public void class_body(Token token) {
        globalSymTab.push();
    }

    @Override
    public void end_class_body(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof ClassBuilder) {
            ClassBuilder classBuilder = (ClassBuilder) top;
            if(token instanceof APTToken) {
                classBuilder.setEndOffset(((APTToken)token).getEndOffset());
            }
        }
        globalSymTab.pop();
    }
    
    @Override
    public void end_class_declaration(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof ClassBuilder) {
            ClassBuilder classBuilder = (ClassBuilder) top;

            ClassImpl cls = classBuilder.create();
            if(cls != null) {
                objects.put(cls.getStartOffset(), cls);
                SymTabEntry classEntry = globalSymTab.lookupLocal(cls.getName());
                if(classEntry != null) {
                    classEntry.setAttribute(CppAttributes.DEFINITION, cls);
                } else {
//                    System.out.println("classEntry is empty " + cls);
                }
            }
            builderContext.pop();
        }
    }    

    @Override
    public void namespace_declaration(Token token) {
        NamespaceBuilder nsBuilder = new NamespaceBuilder();
        nsBuilder.setParentNamespace(builderContext.getNamespaceBuilderIfExist());
        nsBuilder.setFile(file);
        if(token instanceof APTToken) {
            nsBuilder.setStartOffset(((APTToken)token).getOffset());
        }
        builderContext.push(nsBuilder);
    }

    @Override
    public void namespace_name(Token token) {
        NamespaceBuilder nsBuilder = builderContext.getNamespaceBuilder();
        
        APTToken aToken = (APTToken) token;
        final CharSequence name = aToken.getTextID();
        SymTabEntry enumEntry = globalSymTab.lookupLocal(name);
        if (enumEntry == null) {
            enumEntry = globalSymTab.enterLocal(name);
            enumEntry.setAttribute(CppAttributes.TYPE, true);
        } else {
            // error
        }
        nsBuilder.setName(name);
    }
    
    @Override
    public void namespace_body(Token token) {
        globalSymTab.push();
    }

    @Override
    public void end_namespace_body(Token token) {
        globalSymTab.pop();
    }

    @Override
    public void end_namespace_declaration(Token token) {
        NamespaceBuilder nsBuilder = builderContext.getNamespaceBuilder();
        if(token instanceof APTToken) {
            nsBuilder.setEndOffset(((APTToken)token).getEndOffset());
        }
        builderContext.pop();
        nsBuilder.create();
    }
    
    @Override
    public void compound_statement(Token token) {
        globalSymTab.push();
    }

    @Override
    public void end_compound_statement(Token token) {
        globalSymTab.pop();
    }

    @Override
    public void id(Token token) {
        APTToken aToken = (APTToken) token;
        final CharSequence name = aToken.getTextID();
        SymTabEntry entry = globalSymTab.lookup(name);
        if (entry != null) {
            addReference(token, (CsmObject) entry.getAttribute(CppAttributes.DEFINITION), CsmReferenceKind.DIRECT_USAGE);
        }
    }
    
    @Override
    public void simple_type_id(Token token) {
        APTToken aToken = (APTToken) token;
        final CharSequence name = aToken.getTextID();
        SymTabEntry entry = globalSymTab.lookup(name);
        if (entry != null) {
            CsmObject def = (CsmObject) entry.getAttribute(CppAttributes.DEFINITION);
            addReference(token, def, CsmReferenceKind.DIRECT_USAGE);
            
            if(token instanceof APTToken && CsmKindUtilities.isClassifier(def)) {
                CsmType type = TypeFactory.createSimpleType((CsmClassifier)def, file, ((APTToken)token).getOffset(), ((APTToken)token).getEndOffset());
                objects.put(type.getStartOffset(), type);
            }
            
        }
        
    }

    @Override
    public boolean isType(String name) {
        SymTabEntry entry = globalSymTab.lookup(CharSequences.create(name));
        if (entry != null) {
            return entry.getAttribute(CppAttributes.TYPE) != null;
        }
        return false;
    }

    @Override
    public void onInclude(CsmFile inclFile, APTPreprocHandler.State stateBefore) {
        assert inclFile instanceof FileImpl;
        ((FileImpl)inclFile).parseOnInclude(stateBefore, this);
    }
        
    private SymTabStack createGlobal() {
        SymTabStack out = SymTabStack.create();
        // TODO: need to push symtab for predefined types
        
        // create global level 
        out.push();
        return out;
    }

    private static final boolean TRACE = false;
    private void addReference(Token token, final CsmObject definition, final CsmReferenceKind kind) {
        if (definition == null) {
//            assert false;
            if (TRACE) System.err.println("no definition for " + token + " in " + file);
            return;
        }
        assert token instanceof APTToken : "token is incorrect " + token;
        if (APTUtils.isMacroExpandedToken(token)) {
            if (TRACE) System.err.println("skip registering macro expanded " + token + " in " + file);
            return;
        }
        APTToken aToken = (APTToken) token;
        final CharSequence name = aToken.getTextID();
        final int startOffset = aToken.getOffset();
        final int endOffset = aToken.getEndOffset();
        CsmReference ref = new CsmReference() {

            @Override
            public CsmReferenceKind getKind() {
                return kind;
            }

            @Override
            public CsmObject getReferencedObject() {
                return definition;
            }

            @Override
            public CsmObject getOwner() {
                return null;
            }

            @Override
            public CsmFile getContainingFile() {
                return file;
            }

            @Override
            public int getStartOffset() {
                return startOffset;
            }

            @Override
            public int getEndOffset() {
                return endOffset;
            }

            @Override
            public CsmOffsetable.Position getStartPosition() {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public CsmOffsetable.Position getEndPosition() {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public CharSequence getText() {
                return name;
            }

            @Override
            public CsmObject getClosestTopLevelObject() {
                return null;
            }
        };
        file.addReference(ref, definition);
    }   
}
