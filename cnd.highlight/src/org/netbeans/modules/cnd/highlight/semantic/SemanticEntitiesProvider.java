/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.highlight.semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelutil.FontColorProvider;
import org.netbeans.modules.cnd.modelutil.FontColorProvider.Entity;
import org.openide.util.NbBundle;

/**
 *
 * @author Sergey Grinev
 */
public final class SemanticEntitiesProvider {

    private final List<SemanticEntity> list;

    public List<SemanticEntity> get() {
        return list;
    }
    
    private SemanticEntity getInactiveCode(){
        return new AbstractSemanticEntity(FontColorProvider.Entity.INACTIVE_CODE) {
            @Override
             public String getName() {
                return "inactive"; // NOI18N
            }
            @Override
            public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
                return ModelUtils.getInactiveCodeBlocks(csmFile);
            }
        };
    }

    private SemanticEntity getFastFields(){
        return new AbstractSemanticEntity(FontColorProvider.Entity.CLASS_FIELD) {
            @Override
            public String getName() {
                return "fast-class-fields"; // NOI18N
            }
            @Override
            public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
                Collection<CsmReference> references = CsmReferenceResolver.getDefault().getReferences(csmFile);
                List<CsmOffsetable> res = new ArrayList<CsmOffsetable>();
                for(CsmReference ref : references) {
                    if (CsmKindUtilities.isField(ref.getReferencedObject())){
                        res.add(ref);
                    }
                }
                return res;
            }
            @Override
            public ReferenceCollector getCollector() {
                return null;
            }
        };
    }

    private SemanticEntity getFields(){
        return new AbstractSemanticEntity(FontColorProvider.Entity.CLASS_FIELD) {
            @Override
            public String getName() {
                return "class-fields"; // NOI18N
            }
            @Override
            public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
                return ModelUtils.collect(csmFile, getCollector());
            }
            @Override
            public ReferenceCollector getCollector() {
                return new ModelUtils.FieldReferenceCollector();
            }
        };
    }

    private static final Set<CsmReferenceKind> FUN_DECLARATION_KINDS = EnumSet.of(CsmReferenceKind.DECLARATION, CsmReferenceKind.DEFINITION);
    private SemanticEntity getFastFunctions(){
        return new AbstractSemanticEntity(FontColorProvider.Entity.FUNCTION) {
            @Override
            public String getName() {
                return "fast-functions-names"; // NOI18N
            }
            @Override
            public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
                Collection<CsmReference> references = CsmReferenceResolver.getDefault().getReferences(csmFile);
                List<CsmOffsetable> res = new ArrayList<CsmOffsetable>();
                for(CsmReference ref : references) {
                    if (CsmKindUtilities.isFunction(ref.getReferencedObject())){
                        res.add(ref);
                    }
                }
                return res;
            }

            @Override
            public ReferenceCollector getCollector() {
                return null;
            }

            @Override
            public AttributeSet getAttributes(CsmOffsetable obj) {
                CsmReference ref = (CsmReference) obj;
                CsmFunction fun = (CsmFunction) ref.getReferencedObject();
                if (fun == null) {
                    return color;
                }
                // check if we are in the function declaration
                if (CsmReferenceResolver.getDefault().isKindOf(ref, FUN_DECLARATION_KINDS)) {
                    return color;
                } else {
                    return funUsageColors;
                }
            }

            @Override
            public void updateFontColors(FontColorProvider provider) {
                super.updateFontColors(provider);
                funUsageColors = getFontColor(provider, FontColorProvider.Entity.FUNCTION_USAGE);
            }
            private AttributeSet funUsageColors;
        };
    }
    private SemanticEntity getFunctions(){
        return new AbstractSemanticEntity(FontColorProvider.Entity.FUNCTION) {
            @Override
            public String getName() {
                return "functions-names"; // NOI18N
            }
            @Override
            public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
                return ModelUtils.collect(csmFile, getCollector());
            }
            @Override
            public ReferenceCollector getCollector() {
                return new ModelUtils.FunctionReferenceCollector();
            }

            @Override
            public AttributeSet getAttributes(CsmOffsetable obj) {
                CsmReference ref = (CsmReference) obj;
                CsmFunction fun = (CsmFunction) ref.getReferencedObject();
                if (fun == null) {
                    return color;
                }
                // check if we are in the function declaration
                if (CsmReferenceResolver.getDefault().isKindOf(ref, FUN_DECLARATION_KINDS)) {
                    return color;
                } else {
                    return funUsageColors;
                }
            }

            @Override
            public void updateFontColors(FontColorProvider provider) {
                super.updateFontColors(provider);
                funUsageColors = getFontColor(provider, FontColorProvider.Entity.FUNCTION_USAGE);
            }
            private AttributeSet funUsageColors;
        };
    }

    private SemanticEntity getMacros(){
        return new AbstractSemanticEntity(FontColorProvider.Entity.DEFINED_MACRO) {
            @Override
            public String getName() {
                return "macros"; // NOI18N
            }
            @Override
            public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
                return ModelUtils.getMacroBlocks(csmFile);
            }
            @Override
            public AttributeSet getAttributes(CsmOffsetable obj) {
                CsmMacro macro = (CsmMacro) ((CsmReference) obj).getReferencedObject();
                if (macro == null){
                    return color;
                }
                switch(macro.getKind()){
                    case USER_SPECIFIED:
                        return userMacroColors;
                    case COMPILER_PREDEFINED:
                    case POSITION_PREDEFINED:
                        return sysMacroColors;
                    case DEFINED:
                        return color;
                    default:
                        throw new IllegalArgumentException("unexpected macro kind:" + macro.getKind() + " in macro:" + macro); // NOI18N
                }
            }
            private AttributeSet sysMacroColors;
            private AttributeSet userMacroColors;
            @Override
            public void updateFontColors(FontColorProvider provider) {
                super.updateFontColors(provider);
                sysMacroColors = getFontColor(provider, FontColorProvider.Entity.SYSTEM_MACRO);
                userMacroColors = getFontColor(provider, FontColorProvider.Entity.USER_MACRO);
            }
        };
    }

    private SemanticEntity getTypedefs(){
        return new AbstractSemanticEntity(FontColorProvider.Entity.TYPEDEF) {
            @Override
            public String getName() {
                return "typedefs"; // NOI18N
            }
            @Override
            public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
                return ModelUtils.collect(csmFile, getCollector());
            }
            @Override
            public ReferenceCollector getCollector() {
                return new ModelUtils.TypedefReferenceCollector();
            }
        };
    }

    private SemanticEntity getUnusedVariables(){
        return new AbstractSemanticEntity(FontColorProvider.Entity.UNUSED_VARIABLES) {
            private final AttributeSet UNUSED_TOOLTIP = AttributesUtilities.createImmutable(
                        EditorStyleConstants.Tooltip,
                        NbBundle.getMessage(SemanticEntitiesProvider.class, "UNUSED_VARIABLE_TOOLTIP")); // NOI18N
            @Override
            public String getName() {
                return "unused-variables"; // NOI18N
            }
            @Override
            public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
                return ModelUtils.collect(csmFile, getCollector());
            }
            @Override
            public ReferenceCollector getCollector() {
                return new ModelUtils.UnusedVariableCollector();
            }
            @Override
            public void updateFontColors(FontColorProvider provider) {
                super.updateFontColors(provider);
                color = AttributesUtilities.createComposite(UNUSED_TOOLTIP, color);
            }
        };
    }
    
    private SemanticEntitiesProvider() {
        list = new ArrayList<SemanticEntity>();
        // Inactive Code
        list.add(getInactiveCode());
        if (!HighlighterBase.MINIMAL) { // for QEs who want to save performance on UI tests
            // Macro
            list.add(getMacros());
            // Class Fields declarations
            list.add(getFastFields());
            // Class Fields
            list.add(getFields());
            // Function declaration/definition Names
            list.add(getFastFunctions());
            // Function Names
            list.add(getFunctions());
            // typedefs
            list.add(getTypedefs());
            // unused variables
            list.add(getUnusedVariables());
        } 
    }
    
    private static abstract class AbstractSemanticEntity implements SemanticEntity {

        protected AttributeSet color;
        private final FontColorProvider.Entity entity;
        private static final AttributeSet cleanUp = AttributesUtilities.createImmutable(
                StyleConstants.Underline, null,
                StyleConstants.StrikeThrough, null,
                StyleConstants.Background, null,
                EditorStyleConstants.WaveUnderlineColor, null,
                EditorStyleConstants.Tooltip, null);

        public AbstractSemanticEntity() {
            this.entity = null;
        }

        public AbstractSemanticEntity(Entity entity) {
            this.entity = entity;
        }

        @Override
        public void updateFontColors(FontColorProvider provider) {
            assert entity != null;
            color = getFontColor(provider, entity);
        }

        protected static AttributeSet getFontColor(FontColorProvider provider, FontColorProvider.Entity entity) {
            AttributeSet attributes = AttributesUtilities.createComposite(provider.getColor(entity), cleanUp);
            return attributes;
        }

        @Override
        public AttributeSet getAttributes(CsmOffsetable obj) {
            return color;
        }

        @Override
        public ReferenceCollector getCollector() {
            return null;
        }

        @Override
        public boolean isEnabledByDefault() {
            return true;
        }
        
    }

    // Singleton
    private static class Instantiator {
        static SemanticEntitiesProvider instance = new SemanticEntitiesProvider();
        private Instantiator() {
        }
    }

    public static SemanticEntitiesProvider instance() {
        return Instantiator.instance;
    }
}

