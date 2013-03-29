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

import java.util.*;
import org.antlr.runtime.TokenStream;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.ClassForwardDeclarationImpl.ClassForwardDeclarationBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl.ClassBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl.ClassMemberForwardDeclaration.ClassMemberForwardDeclarationBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl.MemberBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl.MemberTypedef.MemberTypedefBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImplSpecialization.ClassSpecializationBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.CsmObjectBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.ConstructorImpl.ConstructorBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.DestructorImpl.DestructorBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.ConstructorDDImpl.ConstructorDDBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.DestructorDDImpl.DestructorDDBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.DestructorDefinitionImpl.DestructorDefinitionBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.ConstructorDefinitionImpl.ConstructorDefinitionBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.EnumImpl;
import org.netbeans.modules.cnd.modelimpl.csm.EnumImpl.EnumBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.EnumeratorImpl.EnumeratorBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.ExpressionBasedSpecializationParameterImpl.ExpressionBasedSpecializationParameterBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.FieldImpl.FieldBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.FriendClassImpl.FriendClassBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.FriendFunctionDDImpl.FriendFunctionDDBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.FriendFunctionDefinitionImpl.FriendFunctionDefinitionBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.FriendFunctionImpl.FriendFunctionBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionDDImpl.FunctionDDBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionDefinitionImpl.FunctionDefinitionBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImpl.FunctionBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionParameterListImpl.FunctionParameterListBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.InheritanceImpl.InheritanceBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.MethodDDImpl.MethodDDBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.MethodImpl.MethodBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceAliasImpl.NamespaceAliasBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceDefinitionImpl.NamespaceBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.ParameterEllipsisImpl.ParameterEllipsisBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.ParameterImpl.ParameterBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.SpecializationDescriptor.SpecializationDescriptorBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.SpecializationDescriptor.SpecializationParameterBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateDescriptor.TemplateDescriptorBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateParameterImpl.TemplateParameterBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.TypeBasedSpecializationParameterImpl.TypeBasedSpecializationParameterBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.TypeFactory.TypeBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.TypedefImpl.TypedefBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.UsingDeclarationImpl.UsingDeclarationBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.UsingDirectiveImpl.UsingDirectiveBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.VariableDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.VariableImpl.VariableBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase.DeclaratorBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase.SimpleDeclarationBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableIdentifiableBase.NameBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.CaseStatementImpl.CaseStatementBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.CompoundStatementImpl.CompoundStatementBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ConditionDeclarationImpl.ConditionDeclarationBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ConditionExpressionImpl.ConditionExpressionBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.DeclarationStatementImpl.DeclarationStatementBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase.ExpressionBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase.ExpressionBuilderContainer;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionStatementImpl.ExpressionStatementBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ForStatementImpl.ForStatementBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.GotoStatementImpl.GotoStatementBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.IfStatementImpl.IfStatementBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.LabelImpl.LabelBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.LoopStatementImpl.LoopStatementBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ReturnStatementImpl.ReturnStatementBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.StatementBase.StatementBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.StatementBase.StatementBuilderContainer;
import org.netbeans.modules.cnd.modelimpl.csm.deep.SwitchStatementImpl.SwitchStatementBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.UniversalStatement.UniversalStatementBuilder;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider.ParserError;
import org.netbeans.modules.cnd.modelimpl.parser.symtab.*;
import org.openide.util.CharSequences;

/**
 * @author Nikolay Krasilnikov (nnnnnk@netbeans.org)
 */
public class CppParserActionImpl implements CppParserActionEx {

    private enum CppAttributes implements SymTabEntryKey {
        SYM_TAB, DEFINITION, TYPE, TEMPLATE
    }
    
    private final CppParserBuilderContext builderContext;
    private final SymTabStack globalSymTab;
    private final FileContent mainFileContent;
    private Pair currentContext;
    private final Deque<Pair> contexts;
    private CsmParserProvider.CsmParserParameters params;
    private CXXParserActionEx wrapper;
    private CXXParserEx parser;
    
    private static final class Pair {
        final Map<Integer, CsmObject> objects = new HashMap<Integer, CsmObject>();
        final FileImpl file;

        public Pair(CsmFile file) {
            this.file = (FileImpl)file;
            
            if(this.file == null || this.file.getParsingFileContent() == null) {
                assert false;
            }
        }
        
    }

    public CppParserActionImpl(CsmParserProvider.CsmParserParameters params, CXXParserActionEx wrapper) {
        this.params = params;
        this.wrapper = wrapper;
        this.contexts = new ArrayDeque<Pair>();
        currentContext = new Pair(params.getMainFile());
        mainFileContent = currentContext.file.getParsingFileContent();
//        this.contexts.push(currentContext);
        this.globalSymTab = createGlobal();
        this.builderContext = new CppParserBuilderContext();
    }

    public void setParser(CXXParserEx parser) {
        this.parser = parser;
        parser.setErrorDelegate(new CsmParserProvider.ParserErrorDelegate() {

            @Override
            public void onError(ParserError e) {
                currentContext.file.getParsingFileContent().addParsingError(e);
            }
        });
    }
    
    @Override
    public boolean type_specifier_already_present(TokenStream input) {
        SimpleDeclarationBuilder simpleDeclarationBuilder = builderContext.getSimpleDeclarationBuilderIfExist();
        if(simpleDeclarationBuilder == null) {
            CsmObjectBuilder builder = builderContext.top();
            if(builder instanceof TypeBuilder) {
                CsmObjectBuilder builder2 = builderContext.top(1);
                if(builder2 instanceof SimpleDeclarationBuilder) {
                    simpleDeclarationBuilder = (SimpleDeclarationBuilder)builder2;
                }
            }
        }
        if(simpleDeclarationBuilder != null) {
            if(simpleDeclarationBuilder.hasTypeSpecifier()) {
                return false;
            }
            
            if(!simpleDeclarationBuilder.isInDeclSpecifiers() && simpleDeclarationBuilder.hasTypedefSpecifier()) {
                return false;
            }
            if(simpleDeclarationBuilder.isInDeclSpecifiers() && 
                    simpleDeclarationBuilder.hasTypedefSpecifier() &&
                    !simpleDeclarationBuilder.hasTypeSpecifier()) {
                return true;
            }
        }
        int index = input.index();
        int scopeLevel = 0;
        
        try {
            SymTabEntry entry = null;

            if(builderContext.top() instanceof NameBuilder) {
                NameBuilder nameBuilder = (NameBuilder) builderContext.top();
                if(!nameBuilder.getNameParts().isEmpty()) {
                    for (int i = 0; i < nameBuilder.getNameParts().size(); i++) {
                        CharSequence part = nameBuilder.getNameParts().get(i);

                        SymTabEntry classEntry = globalSymTab.lookup(part);
                        SymTab st = null;
                        if (classEntry != null) {
                            st = (SymTab)classEntry.getAttribute(CppAttributes.SYM_TAB);
                        }
                        if(st != null) {
                            globalSymTab.push(st);
                            scopeLevel++;
                        }
                    }            
                }
            }

            while (true) {
                int LA = input.LA(1);
                if (LA == APTTokenTypes.IDENT) {
                    final CharSequence name = ((APTToken) CXXParserActionImpl.convertToken(input.LT(1))).getTextID();
                    entry = globalSymTab.lookup(name);
                    if (entry == null || entry.getAttribute(CppAttributes.TYPE) == null) {
                        break;
                    }
                    input.consume();
                    LA = input.LA(1);
                    if (LA == APTTokenTypes.LESSTHAN) {
                        input.consume();
                        LA = input.LA(1);
                        int templateLevel = 0;
                        while (templateLevel != 0 || LA != APTTokenTypes.GREATERTHAN) {
                            if(LA == APTTokenTypes.GREATERTHAN) {
                                templateLevel--;
                            } else if(LA == APTTokenTypes.LESSTHAN) {
                                templateLevel++;
                            }
                            input.consume();
                            LA = input.LA(1);
                            if (LA == CXXParserEx.EOF) {
                                break;
                            }
                        }
                        input.consume();
                        LA = input.LA(1);
                    }
                    if (LA == APTTokenTypes.SCOPE) {
                        if (entry.getAttribute(CppAttributes.SYM_TAB) == null) {
                            entry = null;
                            break;
                        }
                        scopeLevel++;
                        globalSymTab.push((SymTab) entry.getAttribute(CppAttributes.SYM_TAB));
                    } else {
                        break;
                    }
                } else if (LA == APTTokenTypes.STAR) {
                    return true;
                } else { 
                    entry = null;
                    break;
                }
                input.consume();
            }
            
            if (entry != null && entry.getAttribute(CppAttributes.TYPE) != null) {
//            if(entry.toString().startsWith("Entry{name=value_type")) {
//                int i = 0;
//                globalSymTab.lookup(((APTToken) CXXParserActionImpl.convertToken(input.LT(1))).getTextID());
//            }
                return true;
            }
            return false;
        } finally {
            for (int i = 0; i < scopeLevel; i++) {
                globalSymTab.pop();
            }
            input.rewind(index);
        }
    }
    
    @Override
    public boolean identifier_is(int kind, Token token) {
        
        int scopeLevel = 0;
        if(builderContext.top() instanceof NameBuilder) {
            NameBuilder nameBuilder = (NameBuilder) builderContext.top();
            if(!nameBuilder.getNameParts().isEmpty()) {
                for (int i = 0; i < nameBuilder.getNameParts().size(); i++) {
                    CharSequence part = nameBuilder.getNameParts().get(i);

                    SymTabEntry classEntry = globalSymTab.lookup(part);
                    SymTab st = null;
                    if (classEntry != null) {
                        st = (SymTab)classEntry.getAttribute(CppAttributes.SYM_TAB);
                    }
                    if(st != null) {
                        globalSymTab.push(st);
                        scopeLevel++;
                    }
                }            
            }
        }
        
               
        APTToken aToken = (APTToken) token;
        final CharSequence name = aToken.getTextID();
        SymTabEntry entry = globalSymTab.lookup(name);

        for (int i = 0; i < scopeLevel; i++) {
            globalSymTab.pop();
        }
        
        if (entry != null) {
            if(entry.getAttribute(CppAttributes.TYPE) != null 
                    || entry.getAttribute(CppAttributes.TEMPLATE) != null) {
                return true;
            } else {
                return false;
            }            
        }
        return false;
//        return true;
    }
    
    @Override
    public boolean top_level_of_template_arguments() {
        if(builderContext.getSimpleDeclarationBuilderIfExist() != null && builderContext.getSimpleDeclarationBuilderIfExist().isInDeclSpecifiers()) {
            return true;
        }        
        if(builderContext.top(1) instanceof SimpleDeclarationBuilder && ((SimpleDeclarationBuilder)builderContext.top(1)).isInDeclSpecifiers()) {
            return true;
        }        
        return templateLevel != 0;
    }
    
    @Override
    public void parameter_declaration_list() {
    }

    @Override
    public void end_parameter_declaration_list() {
    }
    
    @Override
    public void decl_specifiers(Token token) {
        SimpleDeclarationBuilder declarationBuilder = builderContext.getSimpleDeclarationBuilderIfExist();
        if(declarationBuilder != null) {
            declarationBuilder.declSpecifiers();
        }
    }

    @Override
    public void end_decl_specifiers(Token token) {
        SimpleDeclarationBuilder declarationBuilder = builderContext.getSimpleDeclarationBuilderIfExist();
        if(declarationBuilder != null) {
            declarationBuilder.endDeclSpecifiers();
        }
    }
    
    
    @Override
    public void enum_declaration(Token token) {        
        //System.out.println("enum_declaration " + ((APTToken)token).getOffset());
        
        EnumBuilder enumBuilder = new EnumBuilder();
        CsmObjectBuilder parent = builderContext.top(2);
        if(parent instanceof ClassBuilder) {
            ((ClassBuilder)parent).addMemberBuilder(enumBuilder);
        }        
        enumBuilder.setParent(parent);
        enumBuilder.setFile(currentContext.file);
        if(token instanceof APTToken) {
            enumBuilder.setStartOffset(((APTToken)token).getOffset());
        }
        builderContext.push(enumBuilder);
    }

    @Override
    public void enum_strongly_typed(Token token) {
        //System.out.println("enum_strongly_typed " + ((APTToken)token).getOffset());

        CsmObjectBuilder top = builderContext.top();
        if(top instanceof EnumBuilder) {
            EnumBuilder enumBuilder = builderContext.getEnumBuilder();
            enumBuilder.setStronglyTyped();
        }
    }

    @Override
    public void enum_name(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof EnumBuilder) {
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
    }

    @Override
    public void enum_body(Token token) {
        globalSymTab.push();
    }
    
    @Override
    public void enumerator(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof EnumBuilder) {
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
                EnumeratorBuilder builder2 = new EnumeratorBuilder(currentContext.file.getParsingFileContent());
                builder2.setName(name);
                builder2.setFile(currentContext.file);
                builder2.setStartOffset(aToken.getOffset());
                builder2.setEndOffset(aToken.getEndOffset());
                enumBuilder.addEnumerator(builder2);
            }
        }
    }
    
    @Override
    public void end_enum_body(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof EnumBuilder) {
            EnumBuilder enumBuilder = builderContext.getEnumBuilder();

            if(token instanceof APTToken) {
                enumBuilder.setEndOffset(((APTToken)token).getEndOffset());
            }
        }
        SymTab enumerators = globalSymTab.pop();
        globalSymTab.importToLocal(enumerators);
    }

    @Override
    public void end_enum_declaration(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof EnumBuilder) {
            EnumBuilder enumBuilder = builderContext.getEnumBuilder();
            CsmObjectBuilder parent = builderContext.top(3);
            if(parent == null || parent instanceof NamespaceBuilder) {
                EnumImpl e = enumBuilder.create();
                if(e != null) {
                    currentContext.objects.put(e.getStartOffset(), e);
                    SymTabEntry enumEntry = globalSymTab.lookupLocal(e.getName());
                    if(enumEntry != null) {
                        enumEntry.setAttribute(CppAttributes.DEFINITION, e);
                        for (CsmEnumerator csmEnumerator : e.getEnumerators()) {
                            SymTabEntry enumeratorEntry = globalSymTab.lookupLocal(csmEnumerator.getName());
                            assert enumeratorEntry != null;
                            enumeratorEntry.setAttribute(CppAttributes.DEFINITION, csmEnumerator);
                        }
                    }
                }
            }
            builderContext.pop();
        }
    }
    
    @Override
    public void class_declaration(Token token) {
        ClassBuilder classBuilder = new ClassBuilder();
        CsmObjectBuilder parent = builderContext.top(2);
        if(parent instanceof ClassBuilder) {
            ((ClassBuilder)parent).addMemberBuilder(classBuilder);
        }
        classBuilder.setParent(parent);
        classBuilder.setFile(currentContext.file);
        classBuilder.setStartOffset(((APTToken)token).getOffset());
        
        SimpleDeclarationBuilder declBuilder = (SimpleDeclarationBuilder)builderContext.top(1);
        if(declBuilder.getTemplateDescriptorBuilder() != null) {
            classBuilder.setTemplateDescriptorBuilder(declBuilder.getTemplateDescriptorBuilder());        
            classBuilder.setStartOffset(declBuilder.getTemplateDescriptorBuilder().getStartOffset());
        }
        declBuilder.setTypeSpecifier();
        
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
//        builderContext.push(new NameBuilder());
    }
    
    @Override public void end_class_name(Token token) {
//        NameBuilder nameBuilder = (NameBuilder) builderContext.top();
//        builderContext.pop();
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
            classBuilder.setNameStartOffset(aToken.getOffset());
            classBuilder.setNameEndOffset(aToken.getEndOffset());
        }
    }
    
    @Override
    public void class_body(Token token) {
        CharSequence name = null;
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof ClassBuilder) {
            ClassBuilder classBuilder = (ClassBuilder) top;
            name = classBuilder.getName();
        }
        
        SymTab st;
        if(name != null) {
            st = globalSymTab.push(name);
        } else {
            st = globalSymTab.push();
        }
        
        if(name != null) {
            SymTabEntry classEntry = globalSymTab.lookup(name);
            if (classEntry != null) {
                classEntry.setAttribute(CppAttributes.SYM_TAB, st);
            }                 
        }

    }

    @Override
    public void end_class_body(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof ClassBuilder) {
            ClassBuilder classBuilder = (ClassBuilder) top;
            if(token instanceof APTToken) {
                classBuilder.setEndOffset(((APTToken)token).getEndOffset());
            }
            for (MemberBuilder memberBuilder : classBuilder.getMemberBuilders()) {
                if(memberBuilder instanceof MethodDDBuilder) {
                    org.netbeans.modules.cnd.antlr.TokenStream bodyTokenStream = ((MethodDDBuilder)memberBuilder).getBodyTokenStream();
                    if(bodyTokenStream != null) {
                        builderContext.push((MethodDDBuilder)memberBuilder);
                        ParserProviderImpl.Antlr3CXXParser parser = new ParserProviderImpl.Antlr3CXXParser(params);
                        parser.init(null, bodyTokenStream, wrapper);
                        parser.parse(CsmParserProvider.CsmParser.ConstructionKind.FUNCTION_DEFINITION_AFTER_DECLARATOR);
                        builderContext.pop();
                    }
                }
            }
        }
        globalSymTab.pop();
    }
    
    @Override
    public void end_class_declaration(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof ClassBuilder) {
            ClassBuilder classBuilder = (ClassBuilder) top;
            CsmObjectBuilder parent = builderContext.top(3);
            if(parent == null || parent instanceof NamespaceBuilder) {
                ClassImpl cls = classBuilder.create();
                if(cls != null) {
                    currentContext.objects.put(cls.getStartOffset(), cls);
                    SymTabEntry classEntry = globalSymTab.lookupLocal(cls.getName());
                    if(classEntry != null) {
                        classEntry.setAttribute(CppAttributes.DEFINITION, cls);
                    } else {
    //                    System.out.println("classEntry is empty " + cls);
                    }
                }
            }
            builderContext.pop();
        }
    }    

    @Override
    public void namespace_declaration(Token token) {
        NamespaceBuilder nsBuilder = new NamespaceBuilder();
        nsBuilder.setParentNamespace(builderContext.getNamespaceBuilderIfExist());
        nsBuilder.setFile(currentContext.file);
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
        NamespaceBuilder nsBuilder = builderContext.getNamespaceBuilder();
        if(token instanceof APTToken) {
            nsBuilder.setBodyStartOffset(((APTToken)token).getOffset());
        }
        SymTabEntry classEntry = globalSymTab.lookupLocal(nsBuilder.getName());
        SymTab st = null;
        if (classEntry != null) {
            st = (SymTab)classEntry.getAttribute(CppAttributes.SYM_TAB);
        }
        if(st != null) {
            globalSymTab.push(st);
        } else {
            if(nsBuilder.getName() != null) {
                st = globalSymTab.push(nsBuilder.getName());
            } else {
                st = globalSymTab.push();
            }
            if(classEntry != null) {
                classEntry.setAttribute(CppAttributes.SYM_TAB, st);
            }
        }
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
    public void simple_declaration(Token token) {
        SimpleDeclarationBuilder builder = new SimpleDeclarationBuilder();
        builder.setStartOffset(((APTToken)token).getOffset());
        if(builderContext.top() instanceof TemplateDescriptorBuilder) {
            TemplateDescriptorBuilder descriptorBuilder = (TemplateDescriptorBuilder)builderContext.top();
            builder.setStartOffset(descriptorBuilder.getStartOffset());
            builder.setTemplateDescriptorBuilder(descriptorBuilder);
            builderContext.pop();
        }
        if(builderContext.top() instanceof TemplateDescriptorBuilder) {
            TemplateDescriptorBuilder descriptorBuilder = (TemplateDescriptorBuilder)builderContext.top();
            builder.setStartOffset(descriptorBuilder.getStartOffset());
            descriptorBuilder.addTemplateDescriptorBuilder(builder.getTemplateDescriptorBuilder());
            builder.setTemplateDescriptorBuilder(descriptorBuilder);
            builderContext.pop();
        }        
        builderContext.push(builder);        
    }

    @Override
    public void simple_declaration(int kind, Token token) {
        if(kind == SIMPLE_DECLARATION__SEMICOLON) {
            if(builderContext.top(1) instanceof ClassBuilder) {
                simple_member_declaration(SIMPLE_MEMBER_DECLARATION__SEMICOLON, token);
            } else {
                SimpleDeclarationBuilder declBuilder = (SimpleDeclarationBuilder) builderContext.top();
                SimpleDeclarationBuilder builder;

                if(declBuilder.hasTypedefSpecifier()) {
                    builder = new TypedefBuilder(declBuilder);
                } else if(declBuilder.isFunction()) {
                    builder = new FunctionBuilder(declBuilder);
                } else {
                    CharSequence name = declBuilder.getDeclaratorBuilder().getName();
                    if(name != null && !name.toString().contains("::")) { //NOI18N
                        builder = new VariableBuilder(declBuilder);
                    } else {
                        builder = new VariableDefinitionImpl.VariableDefinitionBuilder(declBuilder);
                    }
                }
                CsmObjectBuilder parent = builderContext.top(1);
                builder.setParent(parent);
                builder.setFile(currentContext.file);
                // FIXME: We add 1 because previously we included ; inside
                builder.setEndOffset(((APTToken)token).getOffset()+1);
                builder.setName(declBuilder.getDeclaratorBuilder().getName());
                if(declBuilder.getTemplateDescriptorBuilder() != null) {
                    builder.setStartOffset(declBuilder.getTemplateDescriptorBuilder().getStartOffset());
                }        
                if(parent instanceof DeclarationStatementBuilder) {
                    ((DeclarationStatementBuilder)parent).addDeclarationBuilder(builder);
                } else if(parent instanceof ForStatementBuilder) {
                    DeclarationStatementBuilder dsBuilder = new DeclarationStatementBuilder();
                    dsBuilder.setFile(currentContext.file);
                    dsBuilder.setStartOffset(builder.getStartOffset());
                    dsBuilder.setEndOffset(builder.getEndOffset());
                    dsBuilder.addDeclarationBuilder(builder);
                    ((ForStatementBuilder)parent).addStatementBuilder(dsBuilder);
                } else {
                    builder.create();
                }
            }
        }
    }
    
    @Override
    public void end_simple_declaration(Token token) {
        if(builderContext.getSimpleDeclarationBuilderIfExist() != null) {
            SimpleDeclarationBuilder declBuilder = builderContext.getSimpleDeclarationBuilderIfExist();
            if(declBuilder != null && declBuilder.getDeclaratorBuilder() != null && declBuilder.getDeclaratorBuilder().getNameBuilder() != null) {
                NameBuilder nameBuilder = declBuilder.getDeclaratorBuilder().getNameBuilder();
                for (int i = nameBuilder.getNameParts().size() - 2; i >= 0 ; i--) {
                    CharSequence part = nameBuilder.getNameParts().get(i);
                    globalSymTab.pop(part);
                }
            }
            builderContext.pop();
        }
    }
    
    @Override
    public void compound_statement(Token token) {
        globalSymTab.push();
        
        CompoundStatementBuilder builder = new CompoundStatementBuilder();
        builder.setFile(currentContext.file);
        builder.setStartOffset(((APTToken)token).getOffset());
        builderContext.push(builder);
    }

    @Override
    public void end_compound_statement(Token token) {
        CompoundStatementBuilder builder = (CompoundStatementBuilder)builderContext.top();
        builderContext.pop();
        builder.setEndOffset(((APTToken)token).getEndOffset());
        
        if(builderContext.top() instanceof StatementBuilderContainer) {
            StatementBuilderContainer container = (StatementBuilderContainer)builderContext.top();
            container.addStatementBuilder(builder);
        }

        globalSymTab.pop();
    }

    @Override
    public void decl_specifier(int kind, Token token) {
        if(kind == DECL_SPECIFIER__LITERAL_TYPEDEF) {
            if(builderContext.getSimpleDeclarationBuilderIfExist() != null) {
                builderContext.getSimpleDeclarationBuilderIfExist().setTypedefSpecifier();
            }
        } else if(kind == DECL_SPECIFIER__LITERAL_FRIEND) {
            if(builderContext.getSimpleDeclarationBuilderIfExist() != null) {
                builderContext.getSimpleDeclarationBuilderIfExist().setFriend();
            }
        }
    }

    @Override
    public void simple_type_specifier(Token token) {
    }
    
    @Override
    public void simple_type_specifier(int kind, Token token) {        
        if(builderContext.top(1) instanceof SimpleDeclarationBuilder) {
            ((SimpleDeclarationBuilder)builderContext.top(1)).setTypeSpecifier();
        }
        
        if(kind == SIMPLE_TYPE_SPECIFIER__ID) {
            CsmObjectBuilder top = builderContext.top();
            if(top instanceof TypeBuilder) {
                builderContext.push(new NameBuilder());
            }
        }

        if (kind == SIMPLE_TYPE_SPECIFIER__CHAR
                || kind == SIMPLE_TYPE_SPECIFIER__WCHAR_T
                || kind == SIMPLE_TYPE_SPECIFIER__CHAR16_T
                || kind == SIMPLE_TYPE_SPECIFIER__CHAR32_T
                || kind == SIMPLE_TYPE_SPECIFIER__BOOL
                || kind == SIMPLE_TYPE_SPECIFIER__SHORT
                || kind == SIMPLE_TYPE_SPECIFIER__INT
                || kind == SIMPLE_TYPE_SPECIFIER__LONG
                || kind == SIMPLE_TYPE_SPECIFIER__SIGNED
                || kind == SIMPLE_TYPE_SPECIFIER__UNSIGNED
                || kind == SIMPLE_TYPE_SPECIFIER__FLOAT
                || kind == SIMPLE_TYPE_SPECIFIER__DOUBLE
                || kind == SIMPLE_TYPE_SPECIFIER__VOID) {
            CsmObjectBuilder top = builderContext.top();
            if(top instanceof TypeBuilder) {
                ((TypeBuilder)top).setSimpleTypeSpecifier(((APTToken)token).getTextID());
            }
        }
        
    }

    @Override
    public void end_simple_type_specifier(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof NameBuilder && builderContext.top(1) instanceof TypeBuilder) {
            builderContext.pop();
            if(builderContext.top() instanceof TypeBuilder) {
                TypeBuilder builder = (TypeBuilder)builderContext.top();
                builder.setNameBuilder((NameBuilder)top);
            }
        }
    }    

    @Override
    public void nested_name_specifier(Token token) {
        simple_template_id_or_ident(token);     
    }

    @Override
    public void simple_template_id_nocheck(Token token) {
    }

    @Override
    public void simple_template_id(Token token) {
    }    
    
    @Override
    public void id(Token token) {
//        APTToken aToken = (APTToken) token;
//        final CharSequence name = aToken.getTextID();
//        SymTabEntry entry = globalSymTab.lookup(name);
//        if (entry != null) {
//            addReference(token, (CsmObject) entry.getAttribute(CppAttributes.DEFINITION), CsmReferenceKind.DIRECT_USAGE);
//        }
    }
    
    @Override
    public void simple_type_id(Token token) {
//        APTToken aToken = (APTToken) token;
//        final CharSequence name = aToken.getTextID();
//        SymTabEntry entry = globalSymTab.lookup(name);
//        if (entry != null) {
//            CsmObject def = (CsmObject) entry.getAttribute(CppAttributes.DEFINITION);
//            addReference(token, def, CsmReferenceKind.DIRECT_USAGE);
//            
////            if(token instanceof APTToken && CsmKindUtilities.isClassifier(def)) {
////                CsmType type = TypeFactory.createSimpleType((CsmClassifier)def, currentContext.file, ((APTToken)token).getOffset(), ((APTToken)token).getEndOffset());
////                currentContext.objects.put(type.getStartOffset(), type);
////            }
//            
//        }
        
    }
    
    @Override
    public void simple_template_id_or_ident(Token token) {
        addNamePart(token);
        
        CsmObjectBuilder top2 = builderContext.top(2);
        if(top2 instanceof SimpleDeclarationBuilder && ((SimpleDeclarationBuilder)top2).hasTypedefSpecifier() && !((SimpleDeclarationBuilder)top2).isInDeclSpecifiers()) {
            APTToken aToken = (APTToken) token;
            final CharSequence name = aToken.getTextID();
            SymTabEntry classEntry = globalSymTab.lookup(name);
            if (classEntry == null) {
                classEntry = globalSymTab.enterLocal(name);
                classEntry.setAttribute(CppAttributes.TYPE, true);
            }
        }
    }

    int templateLevel = 0;
    
    @Override
    public void simple_template_id_or_ident(int kind, Token token) {
        if(kind == SIMPLE_TEMPLATE_ID_OR_IDENT__TEMPLATE_ARGUMENT_LIST) {
            templateLevel++;
        }        
        if(kind == SIMPLE_TEMPLATE_ID_OR_IDENT__END_TEMPLATE_ARGUMENT_LIST) {
            templateLevel--;
        }        
    }
    
    @Override
    public void simple_template_id(int kind, Token token) {
        if(kind == SIMPLE_TEMPLATE_ID__TEMPLATE_ARGUMENT_LIST) {
            templateLevel++;
        }        
        if(kind == SIMPLE_TEMPLATE_ID__END_TEMPLATE_ARGUMENT_LIST) {
            templateLevel--;
        }        
    }

    @Override
    public void simple_template_id_nocheck(int kind, Token token) {
        if(kind == SIMPLE_TEMPLATE_ID_NOCHECK__TEMPLATE_ARGUMENT_LIST) {
            templateLevel++;
        }        
        if(kind == SIMPLE_TEMPLATE_ID_NOCHECK__END_TEMPLATE_ARGUMENT_LIST) {
            templateLevel--;
        }        
    }
    
    @Override
    public void template_declaration(int kind, Token token) {
        if(kind == TEMPLATE_DECLARATION__TEMPLATE) {
            TemplateDescriptorBuilder builder = new TemplateDescriptorBuilder();
            builder.setStartOffset(((APTToken)token).getOffset());
            builderContext.push(builder);
        }
        if(kind == TEMPLATE_DECLARATION__TEMPLATE_ARGUMENT_LIST) {
            templateLevel++;
        }        
        if(kind == TEMPLATE_DECLARATION__END_TEMPLATE_ARGUMENT_LIST) {
            templateLevel--;
        }        
    }
    
//    @Override
//    public void using_declaration(Token usingToken) {
//        APTToken aToken = (APTToken) usingToken;
//        final CharSequence name = aToken.getTextID();
//        SymTabEntry classEntry = globalSymTab.lookupLocal(name);
//        if (classEntry == null) {
//            classEntry = globalSymTab.enterLocal(name);
//            classEntry.setAttribute(CppAttributes.TYPE, true);
//        }
//    }
    
    
    @Override
    public void using_declaration(Token usingToken) {
        UsingDeclarationBuilder usingBuilder = new UsingDeclarationBuilder(currentContext.file.getParsingFileContent());
        usingBuilder.setParent(builderContext.top());
        usingBuilder.setFile(currentContext.file);
        usingBuilder.setVisibility(CsmVisibility.PUBLIC);
        if(usingToken instanceof APTToken) {
            usingBuilder.setStartOffset(((APTToken)usingToken).getOffset());
        }
        builderContext.push(usingBuilder);
        
        builderContext.push(new NameBuilder());
    }

    @Override
    public void using_declaration(int kind, Token token) {
        if(kind == USING_DECLARATION__SCOPE) {
            CsmObjectBuilder top = builderContext.top();
            if(top instanceof NameBuilder) {
                NameBuilder nameBuilder = (NameBuilder) top;
                nameBuilder.setGlobal();
            }
        }
    }

    @Override
    public void end_using_declaration(Token semicolonToken) {        
        CsmObjectBuilder top = builderContext.top();        
        if(top instanceof NameBuilder) {
            NameBuilder nameBuilder = (NameBuilder) top;
            CharSequence name = nameBuilder.getName();

            builderContext.pop();
            top = builderContext.top();
            if(top instanceof UsingDeclarationBuilder) {
                UsingDeclarationBuilder usingBuilder = (UsingDeclarationBuilder) top;
                usingBuilder.setName(name, 0, 0);
                
                if(semicolonToken instanceof APTToken) {
                    usingBuilder.setEndOffset(((APTToken)semicolonToken).getEndOffset());
                }
                builderContext.pop();
                top = builderContext.top();
                if(top instanceof ClassBuilder) {
                    ((ClassBuilder)top).addMemberBuilder(usingBuilder);                    
                } else {
                    usingBuilder.create();
                }
            }
            
            CharSequence lastName = nameBuilder.getLastNamePart();
            SymTabEntry classEntry = globalSymTab.lookupLocal(lastName);
            if (classEntry == null) {
                classEntry = globalSymTab.enterLocal(lastName);
                classEntry.setAttribute(CppAttributes.TYPE, true);
            }
            
        }        
    }    
    

    @Override
    public void namespace_alias_definition(Token namespaceToken, Token identToken, Token assignequalToken) {
        NamespaceAliasBuilder builder = new NamespaceAliasBuilder(currentContext.file.getParsingFileContent());
        builder.setParent(builderContext.top());
        builder.setFile(currentContext.file);
        if(namespaceToken instanceof APTToken) {
            builder.setStartOffset(((APTToken)namespaceToken).getOffset());
        }
        if(identToken instanceof APTToken) {
            APTToken aToken = (APTToken) identToken;
            builder.setName(aToken.getTextID(), aToken.getOffset(), aToken.getEndOffset());
        }
        
        builderContext.push(builder);
        
        builderContext.push(new NameBuilder());
    }

    @Override
    public void end_namespace_alias_definition(Token semicolonToken) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof NamespaceAliasBuilder) {
            NamespaceAliasBuilder builder = (NamespaceAliasBuilder) top;
            if(semicolonToken instanceof APTToken) {
                builder.setEndOffset(((APTToken)semicolonToken).getEndOffset());
            }
            builder.create();
            builderContext.pop();
        }          
    }

    @Override
    public void qualified_namespace_specifier(int kind, Token token) {
        if(kind == QUALIFIED_NAMESPACE_SPECIFIER__IDENT) {
            CsmObjectBuilder top = builderContext.top();
            if(top instanceof NameBuilder && (templateLevel == 0 || builderContext.top(4) instanceof TemplateParameterBuilder)) {
                NameBuilder nameBuilder = (NameBuilder) top;
                APTToken aToken = (APTToken) token;
                CharSequence part = aToken.getTextID();
                nameBuilder.addNamePart(part);

                CharSequence name = nameBuilder.getName();

                builderContext.pop();
                top = builderContext.top();
                if(top instanceof NamespaceAliasBuilder) {
                    NamespaceAliasBuilder builder = (NamespaceAliasBuilder) top;
                    builder.setNamespaceName(name);
                }
            }
        } else if(kind == QUALIFIED_NAMESPACE_SPECIFIER__SCOPE) {
            CsmObjectBuilder top = builderContext.top();
            if(top instanceof NameBuilder) {
                NameBuilder nameBuilder = (NameBuilder) top;
                nameBuilder.setGlobal();
            }        
        }    
    }    
    
    
    @Override
    public void type_parameter(int kind, Token token, Token token2, Token token3) {
        if(kind == TYPE_PARAMETER__CLASS ||
                kind == TYPE_PARAMETER__TYPENAME) {
            TemplateParameterBuilder builder = (TemplateParameterBuilder) builderContext.top();            
            if(token3 != null) {
                builder.setName(((APTToken) token3).getText());
                
                APTToken aToken = (APTToken) token3;
                final CharSequence name = aToken.getTextID();
                SymTabEntry classEntry = globalSymTab.lookupLocal(name);
                if (classEntry == null) {
                    classEntry = globalSymTab.enterLocal(name);
                    classEntry.setAttribute(CppAttributes.TYPE, true);
                }
            }
        } else if(kind == TYPE_PARAMETER__CLASS_ASSIGNEQUAL ||
                kind == TYPE_PARAMETER__TYPENAME_ASSIGNEQUAL) {
            TemplateParameterBuilder builder = (TemplateParameterBuilder) builderContext.top();            
            if(token2 != null) {
                builder.setName(((APTToken) token2).getText());                
                
                APTToken aToken = (APTToken) token2;
                final CharSequence name = aToken.getTextID();
                SymTabEntry classEntry = globalSymTab.lookupLocal(name);
                if (classEntry == null) {
                    classEntry = globalSymTab.enterLocal(name);
                    classEntry.setAttribute(CppAttributes.TYPE, true);
                }
            }
        }

    }
    
    
    @Override
    public void using_directive(Token usingToken, Token namespaceToken) {
        UsingDirectiveBuilder usingBuilder = new UsingDirectiveBuilder(currentContext.file.getParsingFileContent());
        usingBuilder.setParent(builderContext.top());
        usingBuilder.setFile(currentContext.file);
        if(usingToken instanceof APTToken) {
            usingBuilder.setStartOffset(((APTToken)usingToken).getOffset());
        }
        builderContext.push(usingBuilder);
        
        builderContext.push(new NameBuilder());
    }

    @Override
    public void using_directive(int kind, Token token) {
        if(kind == USING_DIRECTIVE__IDENT) {
            addNamePart(token);
        } else if(kind == USING_DIRECTIVE__SCOPE) {
            CsmObjectBuilder top = builderContext.top();
            if(top instanceof NameBuilder) {
                NameBuilder nameBuilder = (NameBuilder) top;
                nameBuilder.setGlobal();
            }
        }
    }

    @Override
    public void end_using_directive(Token semicolonToken) {
        CsmObjectBuilder top = builderContext.top();        
        if(top instanceof NameBuilder) {
            NameBuilder nameBuilder = (NameBuilder) top;
            CharSequence name = nameBuilder.getName();

            builderContext.pop();
            top = builderContext.top();
            if(top instanceof UsingDirectiveBuilder) {
                UsingDirectiveBuilder usingBuilder = (UsingDirectiveBuilder) top;
                usingBuilder.setName(name, 0, 0);
                if(semicolonToken instanceof APTToken) {
                    usingBuilder.setEndOffset(((APTToken)semicolonToken).getEndOffset());
                }
                usingBuilder.create();
                builderContext.pop();


                SymTabEntry nsEntry = globalSymTab.lookup(usingBuilder.getName());
                SymTab st = null;
                if (nsEntry != null) {
                    st = (SymTab)nsEntry.getAttribute(CppAttributes.SYM_TAB);
                }
                if(st != null) {
                    globalSymTab.importToLocal(st);
                }            
            }               
        }        
        
    }   
    
    @Override
    public void greedy_declarator() {
        declarator(null);
    }

    @Override
    public void end_greedy_declarator() {
        end_declarator(null);
    }
    
    @Override
    public void declarator_id() {
        builderContext.push(new NameBuilder());
    }

    @Override
    public void end_declarator_id() {
        NameBuilder nameBuilder = (NameBuilder) builderContext.top();
        builderContext.pop();
        if(builderContext.top() instanceof DeclaratorBuilder) {
            CharSequence name = nameBuilder.getName();
            DeclaratorBuilder declaratorBuilder = (DeclaratorBuilder) builderContext.top();        
            declaratorBuilder.setName(name);
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
    public void pushFile(CsmFile file) {
        if (TRACE) System.err.println(contexts.size() + ":" + currentContext.file.getAbsolutePath() + " >>> " + file.getAbsolutePath());
        this.contexts.push(currentContext);
        currentContext = new Pair(file);
        mainFileContent.addIncludedFileContent(currentContext.file.getParsingFileContent());
    }

    @Override
    public CsmFile popFile() {
        assert !contexts.isEmpty();
        CsmFile out = currentContext.file;
        currentContext = contexts.pop();
        if (TRACE) System.err.println(contexts.size() + ":" + currentContext.file.getAbsolutePath() + " <<< " + out.getAbsolutePath());
        return out;
    }

    Map<Integer, CsmObject> getObjectsMap() {
        return currentContext.objects;
    }
    
    CsmFile getCurrentFile() {
        return currentContext.file;
    }
    
    int getBacktrackingLevel() {
        return parser.backtrackingLevel();
    }

    private SymTabStack createGlobal() {
        SymTabStack out = SymTabStack.create();
        // TODO: need to push symtab for predefined types
        
        // create global level 
        out.push();
        return out;
    }

    @Override public void translation_unit(Token token) {}
    @Override public void end_translation_unit(Token token) {}
    @Override public void statement(Token token) {}
    @Override public void end_statement(Token token) {}
    
    @Override public void labeled_statement(Token token) {}
    @Override public void labeled_statement(int kind, Token token) {
        if(kind == LABELED_STATEMENT__CASE) {
            CaseStatementBuilder builder = new CaseStatementBuilder();
            builder.setFile(currentContext.file);
            builder.setStartOffset(((APTToken)token).getOffset());
            builderContext.push(builder);            
        } else if(kind == LABELED_STATEMENT__CASE_COLON) {
            StatementBuilder builder = (StatementBuilder)builderContext.top();
            builderContext.pop();
            builder.setEndOffset(((APTToken)token).getEndOffset());

            StatementBuilderContainer container = (StatementBuilderContainer)builderContext.top();
            container.addStatementBuilder(builder);    
        }
    }
    
    @Override public void labeled_statement(int kind, Token token1, Token token2) {
        if(kind == LABELED_STATEMENT__LABEL) {
            LabelBuilder builder = new LabelBuilder();
            builder.setFile(currentContext.file);
            builder.setStartOffset(((APTToken)token1).getOffset());
            builder.setEndOffset(((APTToken)token2).getEndOffset());
            builder.setLabel(((APTToken)token1).getTextID());
            
            StatementBuilderContainer container = (StatementBuilderContainer)builderContext.top();
            container.addStatementBuilder(builder);    
        } else if(kind == LABELED_STATEMENT__DEFAULT) {
            UniversalStatementBuilder builder = new UniversalStatementBuilder();
            builder.setFile(currentContext.file);
            builder.setStartOffset(((APTToken)token1).getOffset());
            builder.setKind(CsmStatement.Kind.DEFAULT);
            builder.setEndOffset(((APTToken)token2).getEndOffset());
            
            StatementBuilderContainer container = (StatementBuilderContainer)builderContext.top();
            container.addStatementBuilder(builder);    
        }
    }
    
    @Override public void end_labeled_statement(Token token) {
    }
    
    @Override public void expression_statement(Token token) {
        ExpressionStatementBuilder builder = new ExpressionStatementBuilder();
        builder.setFile(currentContext.file);
        builder.setStartOffset(((APTToken)token).getOffset());
        builderContext.push(builder);            
    }
        
    @Override public void end_expression_statement(Token token) {
        ExpressionStatementBuilder builder = (ExpressionStatementBuilder)builderContext.top();
        builderContext.pop();
        builder.setEndOffset(((APTToken)token).getEndOffset());
        
        StatementBuilderContainer container = (StatementBuilderContainer)builderContext.top();
        container.addStatementBuilder(builder);    
    }
    
    @Override public void selection_statement(Token token) {}
    
    @Override public void selection_statement(int kind, Token token) {
        if (kind == SELECTION_STATEMENT__IF) {
            IfStatementBuilder builder = new IfStatementBuilder();
            builder.setFile(currentContext.file);
            builder.setStartOffset(((APTToken)token).getOffset());
            builderContext.push(builder);            
        } else if (kind == SELECTION_STATEMENT__SWITCH) {
            SwitchStatementBuilder builder = new SwitchStatementBuilder();
            builder.setFile(currentContext.file);
            builder.setStartOffset(((APTToken)token).getOffset());
            builderContext.push(builder);
        }
    }
    
    @Override public void end_selection_statement(Token token) {
        StatementBuilder builder = (StatementBuilder)builderContext.top();
        builderContext.pop();
        builder.setEndOffset(((APTToken)token).getEndOffset());
        
        StatementBuilderContainer container = (StatementBuilderContainer)builderContext.top();
        container.addStatementBuilder(builder);    
    }
    
    @Override public void condition(Token token) {}
    @Override public void condition(int kind, Token token) {}    
    @Override public void end_condition(Token token) {}
    @Override public void condition_declaration(Token token) {
        ConditionDeclarationBuilder builder = new ConditionDeclarationBuilder();
        builder.setFile(currentContext.file);
        builder.setStartOffset(((APTToken)token).getOffset());
        builderContext.push(builder);            
    }
    @Override public void end_condition_declaration(Token token) {
        ConditionDeclarationBuilder builder = (ConditionDeclarationBuilder)builderContext.top();
        builderContext.pop();
        builder.setEndOffset(((APTToken)token).getEndOffset());
        if(builderContext.top() instanceof LoopStatementBuilder) {
            LoopStatementBuilder container = (LoopStatementBuilder)builderContext.top();
            container.setConditionDeclaration(builder);
        } else if(builderContext.top() instanceof ForStatementBuilder) {
            ForStatementBuilder container = (ForStatementBuilder)builderContext.top();
            container.setConditionDeclaration(builder);
        } else if(builderContext.top() instanceof SwitchStatementBuilder) {
            SwitchStatementBuilder container = (SwitchStatementBuilder)builderContext.top();
            container.setConditionDeclaration(builder);
        } else if(builderContext.top() instanceof IfStatementBuilder) {
            IfStatementBuilder container = (IfStatementBuilder)builderContext.top();
            container.setConditionDeclaration(builder);
        }
    }
    @Override public void condition_expression(Token token) {
        ConditionExpressionBuilder builder = new ConditionExpressionBuilder();
        builder.setFile(currentContext.file);
        builder.setStartOffset(((APTToken)token).getOffset());
        builderContext.push(builder);            
    }
    @Override public void end_condition_expression(Token token) {
        ConditionExpressionBuilder builder = (ConditionExpressionBuilder)builderContext.top();
        builderContext.pop();
        builder.setEndOffset(((APTToken)token).getEndOffset());
        if(builderContext.top() instanceof LoopStatementBuilder) {
            LoopStatementBuilder container = (LoopStatementBuilder)builderContext.top();
            container.setConditionExpression(builder);
        } else if(builderContext.top() instanceof ForStatementBuilder) {
            ForStatementBuilder container = (ForStatementBuilder)builderContext.top();
            container.setConditionExpression(builder);
        } else if(builderContext.top() instanceof SwitchStatementBuilder) {
            SwitchStatementBuilder container = (SwitchStatementBuilder)builderContext.top();
            container.setConditionExpression(builder);
        } else if(builderContext.top() instanceof IfStatementBuilder) {
            IfStatementBuilder container = (IfStatementBuilder)builderContext.top();
            container.setConditionExpression(builder);
        }
    }
    
    @Override public void iteration_statement(Token token) {}
    @Override public void iteration_statement(int kind, Token token) {
        if (kind == ITERATION_STATEMENT__DO) {
            LoopStatementBuilder builder = new LoopStatementBuilder();
            builder.setFile(currentContext.file);
            builder.setStartOffset(((APTToken)token).getOffset());
            builder.setPostCheck();
            builderContext.push(builder);            
        } else if (kind == ITERATION_STATEMENT__WHILE) {
            LoopStatementBuilder builder = new LoopStatementBuilder();
            builder.setFile(currentContext.file);
            builder.setStartOffset(((APTToken)token).getOffset());
            builder.setPostCheck();
            builderContext.push(builder);            
        } else if (kind == ITERATION_STATEMENT__FOR) {
            ForStatementBuilder builder = new ForStatementBuilder();
            builder.setFile(currentContext.file);
            builder.setStartOffset(((APTToken)token).getOffset());
            builderContext.push(builder);            
        } else if (kind == ITERATION_STATEMENT__FOR_RPAREN) {
            ForStatementBuilder builder = (ForStatementBuilder)builderContext.top();
            builder.body();
        }  
    }
    
    @Override public void end_iteration_statement(Token token) {
        StatementBuilder builder = (StatementBuilder)builderContext.top();
        builderContext.pop();
        builder.setEndOffset(((APTToken)token).getEndOffset());
        
        StatementBuilderContainer container = (StatementBuilderContainer)builderContext.top();
        container.addStatementBuilder(builder);    
    }
    @Override public void for_init_statement(Token token) {}
    @Override public void end_for_init_statement(Token token) {}
    @Override public void for_range_declaration(Token token) {}
    @Override public void end_for_range_declaration(Token token) {}
    @Override public void for_range_initializer(Token token) {}
    @Override public void end_for_range_initializer(Token token) {}
    
    @Override public void jump_statement(Token token) {}
    @Override public void jump_statement(int kind, Token token) {
        if (kind == JUMP_STATEMENT__BREAK) {
            UniversalStatementBuilder builder = new UniversalStatementBuilder();
            builder.setFile(currentContext.file);
            builder.setStartOffset(((APTToken)token).getOffset());
            builder.setKind(CsmStatement.Kind.BREAK);
            builderContext.push(builder);            
        } else if (kind == JUMP_STATEMENT__CONTINUE) {
            UniversalStatementBuilder builder = new UniversalStatementBuilder();
            builder.setFile(currentContext.file);
            builder.setStartOffset(((APTToken)token).getOffset());
            builder.setKind(CsmStatement.Kind.CONTINUE);
            builderContext.push(builder);            
        } if (kind == JUMP_STATEMENT__RETURN) {
            ReturnStatementBuilder builder = new ReturnStatementBuilder();
            builder.setFile(currentContext.file);
            builder.setStartOffset(((APTToken)token).getOffset());
            builderContext.push(builder);            
        }
    }
    @Override public void jump_statement(int kind, Token token1, Token token2) {
        GotoStatementBuilder builder = new GotoStatementBuilder();
        builder.setFile(currentContext.file);
        builder.setStartOffset(((APTToken)token1).getOffset());
        builder.setLabel(((APTToken)token2).getTextID());
        builderContext.push(builder);            
    }
    @Override public void end_jump_statement(Token token) {
        StatementBuilder builder = (StatementBuilder)builderContext.top();
        builderContext.pop();
        builder.setEndOffset(((APTToken)token).getEndOffset());
        
        StatementBuilderContainer container = (StatementBuilderContainer)builderContext.top();
        container.addStatementBuilder(builder);    
    }
    
    @Override public void declaration_statement(Token token) {
        DeclarationStatementBuilder builder = new DeclarationStatementBuilder();
        builder.setFile(currentContext.file);
        builder.setStartOffset(((APTToken)token).getOffset());
        builderContext.push(builder);                
    }
    @Override public void end_declaration_statement(Token token) {
        DeclarationStatementBuilder builder = (DeclarationStatementBuilder)builderContext.top();
        builderContext.pop();
        builder.setEndOffset(((APTToken)token).getEndOffset());
        
        StatementBuilderContainer container = (StatementBuilderContainer)builderContext.top();
        container.addStatementBuilder(builder);    
    }
    
    @Override public void declaration(Token token) {}
    @Override public void end_declaration(Token token) {}
    @Override public void block_declaration(Token token) {}
    @Override public void end_block_declaration(Token token) {}
    @Override public void id_expression(Token token) {
        NameBuilder nameBuilder = new NameBuilder();
        nameBuilder.setFile(currentContext.file);
        nameBuilder.setStartOffset(((APTToken)token).getOffset());
        builderContext.push(nameBuilder);
    }
    @Override public void end_id_expression(Token token) {
        NameBuilder nameBuilder = (NameBuilder)builderContext.top();
        nameBuilder.setEndOffset(((APTToken)token).getEndOffset());
        builderContext.pop();
        if(builderContext.top() instanceof DeclaratorBuilder) {
            CharSequence name = nameBuilder.getName();
            DeclaratorBuilder declaratorBuilder = (DeclaratorBuilder) builderContext.top();        
            declaratorBuilder.setName(name);
            declaratorBuilder.setNameBuilder(nameBuilder);
        } else if (builderContext.top() instanceof NameBuilder) {
            TypeBuilder typeBuilder = new TypeBuilder();
            typeBuilder.setNameBuilder(nameBuilder);
            typeBuilder.setFile(nameBuilder.getFile());
            typeBuilder.setStartOffset(nameBuilder.getStartOffset());
            typeBuilder.setEndOffset(nameBuilder.getEndOffset());
            
            TypeBasedSpecializationParameterBuilder paramBuilder = new TypeBasedSpecializationParameterBuilder();
            paramBuilder.setTypeBuilder(typeBuilder);
            paramBuilder.setFile(typeBuilder.getFile());
            paramBuilder.setStartOffset(typeBuilder.getStartOffset());
            paramBuilder.setEndOffset(typeBuilder.getEndOffset());

            NameBuilder nameBuilder2 = (NameBuilder) builderContext.top();
            nameBuilder2.addParameterBuilder(paramBuilder);
        }
    }

    @Override public void tilde_class_name(Token token) {
        assert token.getType() == APTTokenTypes.TILDE;
        addNamePart(token);
    }
    
    @Override public void end_tilde_class_name(Token token) {
        if (builderContext.top(2) instanceof SimpleDeclarationBuilder) {
            SimpleDeclarationBuilder declBuilder = (SimpleDeclarationBuilder) builderContext.top(2);
            declBuilder.setDestructor();
        }
    }
    
    @Override public void alias_declaration(Token usingToken, Token identToken, Token assignequalToken) {}
    @Override public void end_alias_declaration(Token token) {}
    @Override public void function_specifier(int kind, Token token) {}
    
    @Override public void type_specifier(Token token) {
        if(builderContext.top() instanceof SimpleDeclarationBuilder) {
            SimpleDeclarationBuilder declarationBuilder = (SimpleDeclarationBuilder) builderContext.top();
            TypeBuilder typeBuilder = declarationBuilder.getTypeBuilder() != null ? declarationBuilder.getTypeBuilder() : new TypeBuilder();
            typeBuilder.setFile(currentContext.file);
            typeBuilder.setStartOffset(((APTToken)token).getOffset());
            builderContext.push(typeBuilder);    
        }
    }
    
    @Override public void end_type_specifier(Token token) {
        if(builderContext.top() instanceof TypeBuilder && builderContext.top(1) instanceof SimpleDeclarationBuilder 
                && !(builderContext.top(1) instanceof TemplateParameterBuilder)
                && !(builderContext.top(1) instanceof ClassBuilder)) {
            TypeBuilder typeBuilder = (TypeBuilder) builderContext.top();
            typeBuilder.setEndOffset(((APTToken)token).getEndOffset());
            builderContext.pop();
            SimpleDeclarationBuilder declarationBuilder = (SimpleDeclarationBuilder) builderContext.top();
            declarationBuilder.setTypeBuilder(typeBuilder);
        }
    }
    
    @Override public void trailing_type_specifier(Token token) {}
    @Override public void end_trailing_type_specifier(Token token) {}
    @Override public void decltype_specifier(Token token) {}
    @Override public void decltype_specifier(int kind, Token token) {}
    @Override public void end_decltype_specifier(Token token) {}
    @Override
    public void elaborated_type_specifier(Token token) {
        SimpleDeclarationBuilder builder;
        CsmObjectBuilder parent = builderContext.top(2);
        if(parent instanceof ClassBuilder) {
            CsmObjectBuilder decl = builderContext.top(1);
            if(decl instanceof SimpleDeclarationBuilder && ((SimpleDeclarationBuilder)decl).isFriend()) {
                builder = new FriendClassBuilder((SimpleDeclarationBuilder)decl);
                builder.setFile(currentContext.file);
                builder.setStartOffset(((APTToken)token).getOffset());
                builder.setParent(parent);        
            } else {
                builder = new ClassMemberForwardDeclarationBuilder();
                builder.setFile(currentContext.file);
                builder.setStartOffset(((APTToken)token).getOffset());
                builder.setParent(parent);        
            }
        } else {
            builder = new ClassForwardDeclarationBuilder();
            builder.setFile(currentContext.file);
            builder.setStartOffset(((APTToken)token).getOffset());
            builder.setParent(parent);        
        }
        SimpleDeclarationBuilder declBuilder = (SimpleDeclarationBuilder)builderContext.top(1);
        if(declBuilder.getTemplateDescriptorBuilder() != null) {
            builder.setTemplateDescriptorBuilder(declBuilder.getTemplateDescriptorBuilder());        
            builder.setStartOffset(declBuilder.getTemplateDescriptorBuilder().getStartOffset());
        }        
        declBuilder.setTypeSpecifier();
        builderContext.push(builder);
        builderContext.push(new NameBuilder());
    }
    @Override public void end_elaborated_type_specifier(Token token) {
        NameBuilder nameBuilder = (NameBuilder) builderContext.top();
        builderContext.pop();
        SimpleDeclarationBuilder builder = (SimpleDeclarationBuilder) builderContext.top();
        builderContext.pop();
        builder.setEndOffset(((APTToken)token).getEndOffset());
        builder.setName(nameBuilder.getName());
        CsmObjectBuilder parent = builderContext.top(2);
        if(parent == null || parent instanceof NamespaceBuilder) {
            builder.create();
        } else if(parent instanceof ClassBuilder) {
            if(builder instanceof ClassMemberForwardDeclarationBuilder) {
                ((ClassBuilder)parent).addMemberBuilder((ClassMemberForwardDeclarationBuilder)builder);
            } else if(builder instanceof FriendClassBuilder) {
                ((ClassBuilder)parent).addFriendBuilder((FriendClassBuilder)builder);
            }
        }
        
        CharSequence name = builder.getName();
        SymTabEntry classEntry = globalSymTab.lookup(name);
        if (classEntry == null) {
            classEntry = globalSymTab.enterLocal(name);
            classEntry.setAttribute(CppAttributes.TYPE, true);
        }    
    }
    @Override public void typename_specifier(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof TypeBuilder) {
            builderContext.push(new NameBuilder());
        }        
    }
    @Override public void end_typename_specifier(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof NameBuilder && builderContext.top(1) instanceof TypeBuilder) {
            builderContext.pop();
            if(builderContext.top() instanceof TypeBuilder) {
                TypeBuilder builder = (TypeBuilder)builderContext.top();
                builder.setNameBuilder((NameBuilder)top);
            }
        }
        if(builderContext.top(1) instanceof SimpleDeclarationBuilder) {
            ((SimpleDeclarationBuilder)builderContext.top(1)).setTypeSpecifier();
        }
    }    
    @Override public void asm_definition(Token asmToken, Token lparenToken, Token stringToken, Token rparenToken, Token semicolonToken) {}
    @Override public void linkage_specification(Token externToken, Token stringToken) {}
    @Override public void linkage_specification(int kind, Token token) {}
    @Override public void end_linkage_specification(Token token) {}
    @Override public void init_declarator_list(Token token) {}
    @Override public void init_declarator_list(int kind, Token token) {}
    @Override public void end_init_declarator_list(Token token) {}
    @Override public void init_declarator(Token token) {}
    @Override public void end_init_declarator(Token token) {}
    
    @Override public void declarator(Token token) {
        if(!(builderContext.top() instanceof DeclaratorBuilder)) {
            DeclaratorBuilder builder = new DeclaratorBuilder();
            builderContext.push(builder);        
        } else {
            DeclaratorBuilder declaratorBuilder = (DeclaratorBuilder) builderContext.top();
            declaratorBuilder.enterDeclarator();
        }
    }
    
    @Override public void end_declarator(Token token) {
        DeclaratorBuilder declaratorBuilder = (DeclaratorBuilder) builderContext.top();
        if(declaratorBuilder.isTopDeclarator()) {
            builderContext.pop();
            if(builderContext.top() instanceof SimpleDeclarationBuilder) {
                SimpleDeclarationBuilder declBuilder = (SimpleDeclarationBuilder) builderContext.top();
                declBuilder.setDeclaratorBuilder(declaratorBuilder);
                
                
                if(declBuilder.getTemplateDescriptorBuilder() != null &&
                        !declBuilder.isConstructor() && !declBuilder.isDestructor()) {
                    SymTabEntry classEntry = globalSymTab.lookupLocal(declaratorBuilder.getName());
                    if (classEntry == null) {
                        classEntry = globalSymTab.enterLocal(declaratorBuilder.getName());
                        classEntry.setAttribute(CppAttributes.TEMPLATE, true);
                    } else {
                        classEntry.setAttribute(CppAttributes.TEMPLATE, true);
                    }
                }
                
            }
        } else {
            declaratorBuilder.leaveDeclarator();
        }      
    }
    
    @Override public void noptr_declarator(Token token) {
        declarator(token);
    }
    @Override public void noptr_declarator(int kind, Token token) {}
    @Override public void end_noptr_declarator(Token token) {
        end_declarator(token);
    }
    @Override public void function_declarator(Token token) {
        declarator(token);
    }
    @Override public void function_declarator(int kind, Token token) {
    }
    @Override public void end_function_declarator(Token token) {
        end_declarator(token);
    }
    @Override public void constructor_declarator(Token token) {
        declarator(token);        
        DeclaratorBuilder declaratorBuilder = (DeclaratorBuilder) builderContext.top();
        SimpleDeclarationBuilder declBuilder = (SimpleDeclarationBuilder) builderContext.top(1);
        if(declBuilder.getTypeBuilder() != null) {
            NameBuilder nameBuilder = declBuilder.getTypeBuilder().getNameBuilder();
            CharSequence newName;
            if(nameBuilder != null && !nameBuilder.getNames().isEmpty()) {
                newName = nameBuilder.getLastNamePart();
            } else {
                newName = declBuilder.getTypeBuilder().getName();
            }        
            declaratorBuilder.setName(newName);
            declaratorBuilder.setNameBuilder(nameBuilder);
            declBuilder.setTypeBuilder(null);
            if(newName != null && newName.toString().contains("~")) { // NOI18N
                assert false : "Unexpected name for DeclBuilder " + declBuilder + " - " + newName;
                declBuilder.setDestructor();
            } else {
                declBuilder.setConstructor();
            }
        }
    }
    @Override public void end_constructor_declarator(Token token) {
        end_declarator(token);
    }
    @Override public void noptr_abstract_declarator(Token token) {
        declarator(token);
    }
    @Override public void noptr_abstract_declarator(int kind, Token token) {
    }
    @Override public void end_noptr_abstract_declarator(Token token) {
        end_declarator(token);
    }
    @Override public void universal_declarator(Token token) {
        declarator(token);
    }
    @Override public void end_universal_declarator(Token token) {
        end_declarator(token);
    }
    @Override public void greedy_declarator(Token token) {
        declarator(token);
    }
    @Override public void end_greedy_declarator(Token token) {
        end_declarator(token);
    }
    @Override public void greedy_nonptr_declarator(Token token) {
        declarator(token);
    }
    @Override public void greedy_nonptr_declarator(int kind, Token token) {}
    @Override public void end_greedy_nonptr_declarator(Token token) {
        end_declarator(token);
    }
    @Override public void ptr_operator(Token token) {}
    @Override public void ptr_operator(int kind, Token token) {
        if (kind == PTR_OPERATOR__STAR) {
            if (builderContext.top(1) instanceof SimpleDeclarationBuilder) {
                SimpleDeclarationBuilder sdb = (SimpleDeclarationBuilder)builderContext.top(1);
                sdb.getTypeBuilder().incPointerDepth();
            }
        } else if (kind == PTR_OPERATOR__AMPERSAND) {
            if (builderContext.top(1) instanceof SimpleDeclarationBuilder) {
                SimpleDeclarationBuilder sdb = (SimpleDeclarationBuilder)builderContext.top(1);
                sdb.getTypeBuilder().setReference();
            }
        }
    }
    @Override public void end_ptr_operator(Token token) {}
    
    @Override public void cv_qualifier(int kind, Token token) {
        if (kind == CV_QUALIFIER__CONST) {
            CsmObjectBuilder builder = builderContext.top();
            if (builder instanceof TypeBuilder) {
                TypeBuilder tb = (TypeBuilder)builder;
                tb.setConst();
            } else if (builderContext.top(1) instanceof SimpleDeclarationBuilder) {
                SimpleDeclarationBuilder sdb = (SimpleDeclarationBuilder)builderContext.top(1);
                sdb.setConst();
            }
        }
    }
    
    @Override public void ref_qualifier(int kind, Token token) {}
    @Override public void declarator_id(Token token) {
    }
    
    @Override public void declarator_id(int kind, Token token) {
    }
    
    @Override public void end_declarator_id(Token token) {
        if(builderContext.top() instanceof DeclaratorBuilder) {
            DeclaratorBuilder declaratorBuilder = (DeclaratorBuilder) builderContext.top();        
            CharSequence name = declaratorBuilder.getName();
            NameBuilder nameBuilder = declaratorBuilder.getNameBuilder();
            
            for (int i = 0; i < nameBuilder.getNameParts().size() - 1; i++) {
                CharSequence part = nameBuilder.getNameParts().get(i);
                
                SymTabEntry classEntry = globalSymTab.lookup(part);
                SymTab st = null;
                if (classEntry != null) {
                    st = (SymTab)classEntry.getAttribute(CppAttributes.SYM_TAB);
                }
                if(st != null) {
                    globalSymTab.push(st);
                }
            }
        }
    }
    
    @Override public void type_id(Token token) {
        TypeBuilder typeBuilder = new TypeBuilder();
        typeBuilder.setFile(currentContext.file);
        typeBuilder.setStartOffset(((APTToken)token).getOffset());
        builderContext.push(typeBuilder);    
    }
    
    @Override public void end_type_id(Token token) {
        TypeBuilder typeBuilder = (TypeBuilder) builderContext.top();
        typeBuilder.setEndOffset(((APTToken)token).getEndOffset());
        builderContext.pop();
        if (builderContext.top() instanceof NameBuilder) {
            TypeBasedSpecializationParameterBuilder paramBuilder = new TypeBasedSpecializationParameterBuilder();
            paramBuilder.setTypeBuilder(typeBuilder);
            paramBuilder.setFile(typeBuilder.getFile());
            paramBuilder.setStartOffset(typeBuilder.getStartOffset());
            paramBuilder.setEndOffset(typeBuilder.getEndOffset());

            NameBuilder nameBuilder2 = (NameBuilder) builderContext.top();
            nameBuilder2.addParameterBuilder(paramBuilder);
        }        
    }
    
    @Override public void parameters_and_qualifiers(Token token) {}
    
    @Override public void parameters_and_qualifiers(int kind, Token token) {
        if(kind == PARAMETERS_AND_QUALIFIERS__LPAREN) {
            FunctionParameterListBuilder builder = new FunctionParameterListBuilder();
            builder.setFile(currentContext.file);
            builder.setStartOffset(((APTToken)token).getOffset());
            builderContext.push(builder);
        } else if(kind == PARAMETERS_AND_QUALIFIERS__RPAREN) {
            FunctionParameterListBuilder builder = (FunctionParameterListBuilder) builderContext.top();
            builder.setEndOffset(((APTToken)token).getEndOffset());
            builderContext.pop();
            if(builderContext.top(1) instanceof SimpleDeclarationBuilder) {
                ((SimpleDeclarationBuilder)builderContext.top(1)).setParametersListBuilder(builder);
            }
        }
    }
    
    @Override public void end_parameters_and_qualifiers(Token token) {}
    
    @Override public void parameter_declaration_clause(Token token) {}
    @Override public void parameter_declaration_clause(int kind, Token token) {
        if(kind == PARAMETER_DECLARATION_CLAUSE__ELLIPSIS || kind == PARAMETER_DECLARATION_CLAUSE__ELLIPSIS2) {
            ParameterEllipsisBuilder builder = new ParameterEllipsisBuilder();
            builder.setFile(currentContext.file);
            builder.setStartOffset(((APTToken)token).getOffset());
            builder.setEndOffset(((APTToken)token).getEndOffset());
            if(builderContext.top() instanceof FunctionParameterListBuilder) {
                ((FunctionParameterListBuilder)builderContext.top()).addParameterBuilder(builder);
            }
        }
    }
    @Override public void end_parameter_declaration_clause(Token token) {}
    @Override public void parameter_declaration_list(Token token) {}
    @Override public void end_parameter_declaration_list(int kind, Token token) {}
    @Override public void end_parameter_declaration_list(Token token) {}
    
    @Override public void parameter_declaration(Token token) {
        ParameterBuilder builder = new ParameterBuilder();
        builder.setFile(currentContext.file);
        builder.setStartOffset(((APTToken)token).getOffset());
        builderContext.push(builder);
        SimpleDeclarationBuilder declBuilder = new SimpleDeclarationBuilder();
        declBuilder.setStartOffset(((APTToken)token).getOffset());
        builderContext.push(declBuilder);        
    }
    
    @Override public void parameter_declaration(int kind, Token token) {}
    
    @Override public void end_parameter_declaration(Token token) {
        SimpleDeclarationBuilder declBuilder = (SimpleDeclarationBuilder) builderContext.top();
        declBuilder.setEndOffset(((APTToken)token).getEndOffset());
        builderContext.pop();
        ParameterBuilder builder = (ParameterBuilder) builderContext.top();
        builder.setEndOffset(((APTToken)token).getEndOffset());
        builder.setTypeBuilder(declBuilder.getTypeBuilder());
        if(declBuilder.getDeclaratorBuilder() == null || declBuilder.getDeclaratorBuilder().getName() == null) {
            builder.setName(""); // NOI18N
        } else {
            builder.setName(declBuilder.getDeclaratorBuilder().getName());
        }
        builderContext.pop();
        if(builderContext.top() instanceof FunctionParameterListBuilder) {
            ((FunctionParameterListBuilder)builderContext.top()).addParameterBuilder(builder);
        } else if(builderContext.top() instanceof TemplateParameterBuilder) {
            TemplateParameterBuilder templateParameterBuilder = (TemplateParameterBuilder)builderContext.top();
            templateParameterBuilder.setName(builder.getName());
        }
    }    
    
    @Override public void function_definition_after_declarator(Token token) {
        SimpleDeclarationBuilder declBuilder = (SimpleDeclarationBuilder) builderContext.top();

        CsmObjectBuilder parent = builderContext.top(1);
        if(parent instanceof ClassBuilder) {
            FunctionBuilder builder;
            CharSequence name = declBuilder.getDeclaratorBuilder().getName();
                
            if(declBuilder.isFriend()) {
                if(name != null && !name.toString().contains("::")) { //NOI18N
                    builder = new FriendFunctionDDBuilder();
                } else {
                    builder = new FriendFunctionDefinitionBuilder();
                }
            } else if(declBuilder.isConstructor()) {
                builder = new ConstructorDDBuilder();
            } else if(declBuilder.isDestructor()) {
                builder = new DestructorDDBuilder();
            } else {
                builder = new MethodDDBuilder();
            }
                
            builder.setParent(parent);
            builder.setFile(currentContext.file);
            builder.setStartOffset(declBuilder.getStartOffset());

            builder.setName(name);
            builder.setTypeBuilder(declBuilder.getTypeBuilder());
            if(declBuilder.getTemplateDescriptorBuilder() != null) {
                builder.setTemplateDescriptorBuilder(declBuilder.getTemplateDescriptorBuilder());        
                builder.setStartOffset(declBuilder.getTemplateDescriptorBuilder().getStartOffset());
            }        
            builder.setParametersListBuilder(declBuilder.getParametersListBuilder());
            builderContext.push(builder);            
        } else {
            CharSequence name = declBuilder.getDeclaratorBuilder().getNameBuilder().getName();
            FunctionBuilder builder;
            if(name != null && !name.toString().contains("::")) { //NOI18N
                builder = new FunctionDDBuilder();
            } else {
                if(declBuilder.isConstructor()) {
                    builder = new ConstructorDefinitionBuilder();
                } else if(declBuilder.isDestructor()) {
                    builder = new DestructorDefinitionBuilder();
                } else {
                    builder = new FunctionDefinitionBuilder();
                }
            }
                
            builder.setParent(parent);
            builder.setFile(currentContext.file);
                builder.setStartOffset(declBuilder.getStartOffset());

            builder.setName(name);
                builder.setTypeBuilder(declBuilder.getTypeBuilder());
            if(declBuilder.getTemplateDescriptorBuilder() != null) {
                    builder.setTemplateDescriptorBuilder(declBuilder.getTemplateDescriptorBuilder());        
                builder.setStartOffset(declBuilder.getTemplateDescriptorBuilder().getStartOffset());
            }        
                builder.setParametersListBuilder(declBuilder.getParametersListBuilder());
            builderContext.push(builder);
        }
    }
    @Override public void function_definition_after_declarator(int kind, Token token) {}
    @Override public void end_function_definition_after_declarator(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof FunctionDDBuilder) {
            FunctionDDBuilder builder = (FunctionDDBuilder)top;
            builder.setEndOffset(((APTToken)token).getEndOffset());
            builderContext.pop();
            builder.create();                
        } else if(top instanceof FunctionDefinitionBuilder) {
            FunctionDefinitionBuilder builder = (FunctionDefinitionBuilder)top;
            builder.setEndOffset(((APTToken)token).getEndOffset());
            builderContext.pop();
            builder.create();                
        } else if(top instanceof MethodDDBuilder) {
            MethodDDBuilder builder = (MethodDDBuilder)top;
            builder.setEndOffset(((APTToken)token).getEndOffset());
            builderContext.pop();
            ((ClassBuilder)builderContext.top(1)).addMemberBuilder(builder);
        }
    }
    @Override public void function_declaration(Token token) {}
    @Override public void end_function_declaration(Token token) {}
    @Override public void function_definition(Token token) {}
    @Override public void end_function_definition(Token token) {}
    @Override public void function_body(Token token) {}
    @Override public void end_function_body(Token token) {}
    
    @Override public void initializer(Token token) {
        if(!(builderContext.top() instanceof ExpressionBuilder)) {
            ExpressionBuilder builder = new ExpressionBuilder();
            builder.setFile(currentContext.file);
            builder.setStartOffset(((APTToken)token).getOffset());
            builderContext.push(builder);        
        } else {
            ExpressionBuilder builder = (ExpressionBuilder) builderContext.top();
            builder.enterExpression();
        }    
    }
    @Override public void initializer(int kind, Token token) {}
    @Override public void end_initializer(Token token) {
        ExpressionBuilder builder = (ExpressionBuilder) builderContext.top();
        builder.setEndOffset(((APTToken)token).getEndOffset());
        if(builder.isTopExpression()) {
            builderContext.pop();
            if(builderContext.top() instanceof SimpleDeclarationBuilder) {
                SimpleDeclarationBuilder container = (SimpleDeclarationBuilder) builderContext.top();
                container.setInitializerBuilder(builder);
            }
        } else {
            builder.leaveExpression();
        }    
    }
    @Override public void brace_or_equal_initializer(Token token) {
        initializer(token);
    }
    @Override public void brace_or_equal_initializer(int kind, Token token) {}
    @Override public void end_brace_or_equal_initializer(Token token) {
        end_initializer(token);
    }
    @Override public void initializer_clause(Token token) {}
    @Override public void end_initializer_clause(Token token) {}
    @Override public void initializer_list(Token token) {}
    @Override public void initializer_list(int kind, Token token) {}
    @Override public void end_initializer_list(Token token) {}
    @Override public void braced_init_list(Token token) {}
    @Override public void braced_init_list(int kind, Token token) {}
    @Override public void end_braced_init_list(Token token) {}
    @Override public void optionally_qualified_name(Token token) {
        builderContext.push(new NameBuilder());
    }
    
    @Override public void end_optionally_qualified_name(Token token) {
        NameBuilder nameBuilder = (NameBuilder) builderContext.top();
        builderContext.pop();
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof ClassBuilder) {
            ClassBuilder classBuilder = (ClassBuilder) top;
            APTToken aToken = (APTToken) token;
            SymTabEntry classEntry = globalSymTab.lookupLocal(nameBuilder.getLastNamePart());
            if (classEntry == null) {
                classEntry = globalSymTab.enterLocal(nameBuilder.getLastNamePart());
                classEntry.setAttribute(CppAttributes.TYPE, true);
            } else {
                // error
            }

            classBuilder.setName(nameBuilder.getName());
            // FIXME: need to use nameBuilder offsets (now not initialized)
            classBuilder.setNameStartOffset(aToken.getOffset());
            classBuilder.setNameEndOffset(aToken.getEndOffset());
            List<SpecializationParameterBuilder> params = nameBuilder.getNames().get(nameBuilder.getNames().size() - 1).getParams();
            if(!params.isEmpty()) {
                SpecializationDescriptorBuilder sdb = new SpecializationDescriptorBuilder();
                for (SpecializationParameterBuilder specializationParameterBuilder : params) {
                    sdb.addParameterBuilder(specializationParameterBuilder);
                }
                ClassSpecializationBuilder classSpecializationBuilder = new ClassSpecializationBuilder(classBuilder);
                classSpecializationBuilder.setSpecializationDescriptorBuilder(sdb);
                classSpecializationBuilder.getTemplateDescriptorBuilder().setSpecialization();
                builderContext.pop();
                builderContext.push(classSpecializationBuilder);
            }
        }
    }
    @Override public void class_head(Token token) {}
    @Override public void end_class_head(Token token) {}
    @Override public void class_virtual_specifier(int kind, Token token) {}
    @Override public void member_specification(Token token) {}
    @Override public void member_specification(int kind, Token token) {}
    @Override public void end_member_specification(Token token) {}
    @Override public void member_declaration(Token token){}
    @Override public void member_declaration(int kind, Token token){}
    @Override public void end_member_declaration(Token token){}

    @Override public void simple_member_declaration(Token token){
        SimpleDeclarationBuilder builder = new SimpleDeclarationBuilder();
        builder.setStartOffset(((APTToken)token).getOffset());
        if(builderContext.top() instanceof TemplateDescriptorBuilder) {
            TemplateDescriptorBuilder descriptorBuilder = (TemplateDescriptorBuilder)builderContext.top();
            builder.setStartOffset(descriptorBuilder.getStartOffset());
            builder.setTemplateDescriptorBuilder(descriptorBuilder);
            builderContext.pop();
        }
        if(builderContext.top() instanceof TemplateDescriptorBuilder) {
            TemplateDescriptorBuilder descriptorBuilder = (TemplateDescriptorBuilder)builderContext.top();
            builder.setStartOffset(descriptorBuilder.getStartOffset());
            descriptorBuilder.addTemplateDescriptorBuilder(builder.getTemplateDescriptorBuilder());
            builder.setTemplateDescriptorBuilder(descriptorBuilder);
            builderContext.pop();
        }        
        builderContext.push(builder);            
    }
    
    @Override public void simple_member_declaration(int kind, Token token){
        if(kind == SIMPLE_MEMBER_DECLARATION__SEMICOLON) {
            SimpleDeclarationBuilder declBuilder = (SimpleDeclarationBuilder) builderContext.top();
            SimpleDeclarationBuilder builder;
            if(declBuilder.hasTypedefSpecifier()) {
                builder = new MemberTypedefBuilder(declBuilder);
            } else if(declBuilder.isFunction()) {
                if(declBuilder.isConstructor()) {
                    builder = new ConstructorBuilder(declBuilder);
                } else if(declBuilder.isDestructor()) {
                    builder = new DestructorBuilder(declBuilder);
                } else if (declBuilder.isFriend()){
                    builder = new FriendFunctionBuilder(declBuilder);
                } else {
                    builder = new MethodBuilder(declBuilder);
                }
            } else {
                builder = new FieldBuilder(declBuilder, currentContext.file.getParsingFileContent());
            }
            
            ClassBuilder parent = (ClassBuilder)builderContext.top(1);
            builder.setParent(parent);
            builder.setFile(currentContext.file);
            // FIXME: We add 1 because previously we included ; inside
            builder.setEndOffset(((APTToken)token).getOffset()+1);
            builder.setName(declBuilder.getDeclaratorBuilder().getName());
            if(declBuilder.getTemplateDescriptorBuilder() != null) {
                builder.setStartOffset(declBuilder.getTemplateDescriptorBuilder().getStartOffset());
            }
            if (builder instanceof FriendFunctionBuilder) {
                parent.addFriendBuilder((FriendFunctionBuilder)builder);
            } else {
                parent.addMemberBuilder((MemberBuilder)builder);
            }
        }    
    }
    
    @Override public void end_simple_member_declaration(Token token){
        if(builderContext.getSimpleDeclarationBuilderIfExist() != null) {
            SimpleDeclarationBuilder declBuilder = builderContext.getSimpleDeclarationBuilderIfExist();
            if(declBuilder != null && declBuilder.getDeclaratorBuilder() != null && declBuilder.getDeclaratorBuilder().getNameBuilder() != null) {
                NameBuilder nameBuilder = declBuilder.getDeclaratorBuilder().getNameBuilder();
                for (int i = nameBuilder.getNameParts().size() - 2; i >= 0 ; i--) {
                    CharSequence part = nameBuilder.getNameParts().get(i);
                    globalSymTab.pop(part);
                }
            }
            builderContext.pop();
        }    
    }    
    
    @Override public void member_declarator(Token token) {
        declarator(token);
    }
    
    @Override public void end_member_declarator(Token token) {
        end_declarator(token);
    }
    
    @Override public void pure_specifier(Token token) {}
    @Override public void end_pure_specifier(Token token) {}
    @Override public void constant_initializer(Token token) {
        initializer(token);
    }
    @Override public void end_constant_initializer(Token token) {
        end_initializer(token);
    }
    @Override public void virt_specifier(int kind, Token token) {}
    @Override public void base_clause(Token token) {}
    @Override public void end_base_clause(Token token) {}
    @Override public void base_specifier_list(Token token) {}
    @Override public void base_specifier_list(int kind, Token token) {}
    @Override public void end_base_specifier_list(Token token) {}
    @Override public void class_or_decltype(Token token) {}
    @Override public void class_or_decltype(int kind, Token token) {}
    @Override public void end_class_or_decltype(Token token) {}
    @Override public void base_type_specifier(Token token) {
        InheritanceBuilder builder = new InheritanceBuilder();
        builder.setStartOffset(((APTToken)token).getOffset());
        builder.setFile(currentContext.file);
        builderContext.push(builder);
        builderContext.push(new NameBuilder());
    }
    @Override public void end_base_type_specifier(Token token) {
        NameBuilder nameBuilder = (NameBuilder)builderContext.top();
        builderContext.pop();
        
        TypeBuilder typeBuilder = new TypeBuilder();
        typeBuilder.setFile(currentContext.file);
        typeBuilder.setStartOffset(((APTToken)token).getOffset());
        typeBuilder.setEndOffset(((APTToken)token).getEndOffset());
        typeBuilder.setNameBuilder(nameBuilder);
        
        InheritanceBuilder builder = (InheritanceBuilder)builderContext.top();
        builder.setEndOffset(((APTToken)token).getEndOffset());
        builder.setTypeBuilder(typeBuilder);
        builderContext.pop();
        ClassBuilder clsBuilder = (ClassBuilder)builderContext.top();
        clsBuilder.addInheritanceBuilder(builder);
        
        for (int i = 0; i < nameBuilder.getNameParts().size(); i++) {
            CharSequence part = nameBuilder.getNameParts().get(i);

            SymTabEntry classEntry = globalSymTab.lookup(part);
            SymTab st = null;
            if (classEntry != null) {
                st = (SymTab)classEntry.getAttribute(CppAttributes.SYM_TAB);
            }
            if(st != null) {
                globalSymTab.importToLocal(st);
            }
        }
                
    }
    @Override public void access_specifier(int kind, Token token) {}
    @Override public void conversion_function_id(Token token) {}
    @Override public void end_conversion_function_id(Token token) {}
    @Override public void conversion_type_id(Token token) {}
    @Override public void end_conversion_type_id(Token token) {}
    @Override public void ctor_initializer(Token token) {}
    @Override public void end_ctor_initializer(Token token) {}
    @Override public void mem_initializer_list(Token token) {}
    @Override public void mem_initializer_list(int kind, Token token) {}
    @Override public void end_mem_initializer_list(Token token) {}
    @Override public void mem_initializer(Token token) {}
    @Override public void mem_initializer(int kind, Token token) {}
    @Override public void end_mem_initializer(Token token) {}
    @Override public void mem_initializer_id(Token token) {}
    @Override public void end_mem_initializer_id(Token token) {}
    @Override public void mem_operator_function_id(Token token) {}
    @Override public void operator_function_id(int kind, Token token) {}
    @Override public void end_operator_function_id(Token token) {}
    @Override public void operator_id(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof NameBuilder /*&& (templateLevel == 0 || builderContext.top(4) instanceof TemplateParameterBuilder)*/) {
            NameBuilder nameBuilder = (NameBuilder) top;
            APTToken aToken = (APTToken) token;
            CharSequence part = aToken.getTextID();
            nameBuilder.addNamePart("operator " + part); // NOI18N
        }        
    }
    @Override public void end_operator_id(Token token) {}
    @Override public void literal_operator_id(Token operatorToken, Token stringToken, Token identToken) {}
    @Override public void template_declaration(Token token) {}
    @Override public void end_template_declaration(Token token) {}
    @Override public void template_parameter_list(Token token) {}
    @Override public void template_parameter_list(int kind, Token token) {}
    @Override public void end_template_parameter_list(Token token) {}
    @Override public void template_parameter(Token token) {
        TemplateParameterBuilder builder = new TemplateParameterBuilder();
        builder.setFile(currentContext.file);
        builder.setStartOffset(((APTToken)token).getOffset());
        builderContext.push(builder);            
    }
    @Override public void end_template_parameter(Token token) {
        TemplateParameterBuilder builder = (TemplateParameterBuilder) builderContext.top();
        builder.setEndOffset(((APTToken)token).getEndOffset());
        builderContext.pop();
        TemplateDescriptorBuilder descriptorBuilder = (TemplateDescriptorBuilder) builderContext.top();
        descriptorBuilder.addParameterBuilder(builder);
    }
    @Override public void type_parameter(int kind, Token token) {}
    @Override public void template_argument_list(Token token) {}
    @Override public void template_argument_list(int kind, Token token) {}
    @Override public void end_template_argument_list(Token token) {}
    @Override public void template_argument(Token token) {}
    @Override public void end_template_argument(Token token) {}
    @Override public void explicit_instantiation(Token token) {
        if(builderContext.top() instanceof TemplateDescriptorBuilder) {
            builderContext.pop();
        }    
    }
    @Override public void explicit_instantiation(int kind, Token token) {}
    @Override public void end_explicit_instantiation(Token token) {}
    @Override public void explicit_specialization(Token templateToken, Token lessthenToken, Token greaterthenToken) {
        TemplateDescriptorBuilder builder = new TemplateDescriptorBuilder();
        builder.setStartOffset(((APTToken)templateToken).getOffset());
        builderContext.push(builder);
    }
    @Override public void end_explicit_specialization(Token token) {}
    @Override public void try_block(Token token) {}
    @Override public void end_try_block(Token token) {}
    @Override public void function_try_block(Token token) {}
    @Override public void end_function_try_block(Token token) {}
    @Override public void handler(Token token) {}
    @Override public void handler(int kind, Token token) {}
    @Override public void end_handler(Token token) {}


    @Override
    public void assignment_expression(Token token) {
        expression(token);
    }

    @Override
    public void end_assignment_expression(Token token) {
        end_expression(token);
    }
    
    @Override
    public void expression(Token token) {
        if(!(builderContext.top() instanceof ExpressionBuilder)) {
            ExpressionBuilder builder = new ExpressionBuilder();
            builder.setFile(currentContext.file);
            builder.setStartOffset(((APTToken)token).getOffset());
            builderContext.push(builder);        
        } else {
            ExpressionBuilder builder = (ExpressionBuilder) builderContext.top();
            builder.enterExpression();
        }
    }
    @Override
    public void end_expression(Token token) {
        ExpressionBuilder builder = (ExpressionBuilder) builderContext.top();
        builder.setEndOffset(((APTToken)token).getEndOffset());
        if(builder.isTopExpression()) {
            builderContext.pop();
            if(builderContext.top() instanceof ExpressionBuilderContainer) {
                ExpressionBuilderContainer container = (ExpressionBuilderContainer) builderContext.top();
                container.addExpressionBuilder(builder);
            } else if(builderContext.top() instanceof NameBuilder) {
                ExpressionBasedSpecializationParameterBuilder paramBuilder = new ExpressionBasedSpecializationParameterBuilder();
                paramBuilder.setExpressionBuilder(builder);
                paramBuilder.setFile(builder.getFile());
                paramBuilder.setStartOffset(builder.getStartOffset());
                paramBuilder.setEndOffset(builder.getEndOffset());
                
                NameBuilder nameBuilder = (NameBuilder) builderContext.top();
                nameBuilder.addParameterBuilder(paramBuilder);
            }
        } else {
            builder.leaveExpression();
        }      
    }

    @Override
    public void constant_expression(Token token) {
        expression(token);
    }

    @Override
    public void end_constant_expression(Token token) {
        end_expression(token);
    }    
    
    @Override
    public void skip_balanced_curlies(Token token) {
        if (builderContext.top() instanceof MethodDDBuilder) {
            MethodDDBuilder builder = (MethodDDBuilder) builderContext.top();
            builder.addBodyToken(token);
        }
    }    
    
    private void addNamePart(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if (top instanceof NameBuilder) {
            NameBuilder nameBuilder = (NameBuilder) top;
            nameBuilder.addNamePart(((APTToken)token).getTextID());
        }
    }
    
    private static final boolean TRACE = false;
    private void addReference(Token token, final CsmObject definition, final CsmReferenceKind kind) {
        if (definition == null) {
//            assert false;
            if (TRACE) System.err.println("no definition for " + token + " in " + currentContext.file);
            return;
        }
        assert token instanceof APTToken : "token is incorrect " + token;
        if (APTUtils.isMacroExpandedToken(token)) {
            if (TRACE) System.err.println("skip registering macro expanded " + token + " in " + currentContext.file);
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
                return currentContext.file;
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
        currentContext.file.addReference(ref, definition);
    }   

}
