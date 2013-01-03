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
package org.netbeans.modules.php.editor.codegen;

import java.awt.Dialog;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement.PrintAs;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TypeNameResolver;
import org.netbeans.modules.php.editor.codegen.ui.ConstructorPanel;
import org.netbeans.modules.php.editor.codegen.ui.MethodPanel;
import org.netbeans.modules.php.editor.elements.TypeNameResolverImpl;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public final class CGSGenerator implements CodeGenerator {

    protected static final String START_OF_GETTER = "get";    //NOI18N
    protected static final String START_OF_SETTER = "set";    //NOI18N

    private static final String NEW_LINE = System.getProperty("line.separator");    //NOI18N
    private static final String FUNCTION_MODIFIER = "${FUNCTION_MODIFIER}"; //NOI18N
    private static final String UNDERSCORED_METHOD_NAME = "${UNDERSCORED_METHOD_NAME}"; //NOI18N
    private static final String ACCESSOR = "${ACCESSOR}"; //NOI18N
    private static final String PROPERTY = "${PROPERTY}";                           //NOI18N
    private static final String CURSOR = "${cursor}";                           //NOI18N
    private static final String PARAM_NAME = "${PARAM_NAME}"; //NOI18N
    private static final String PARAM_TYPE = "${PARAM_TYPE}"; //NOI18N
    private static final String UP_FIRST_LETTER_PROPERTY = "${UpFirstLetterProperty}";  //NOI18N
    private static final String UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE = "${UpFirstLetterPropertyWithoutUnderscore}";  //NOI18N
    private static final String PROPERTY_WITHOUT_UNDERSCORE = "${PropertyWithoutUnderscore}";  //NOI18N
    private static final String FLUENT_SETTER = "${FluentSetter}"; //NOI18N

    public enum GenType {
        CONSTRUCTOR(PanelStrategy.CONSTRUCTOR, FluentSetterStrategy.INVISIBLE) {

            @Override
            public String getPanelTitle() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_PANEL_CONSTRUCTOR"); //NOI18N
            }

            @Override
            public ComboBoxModel getModel(final String propertyName) {
                final DefaultComboBoxModel result = new DefaultComboBoxModel();
                for (CGSGenerator.GenWay way : CGSGenerator.GenWay.values()) {
                    if (!way.equals(CGSGenerator.GenWay.WITH_UNDERSCORE)) {
                        result.addElement(new ComboBoxModelElement(way.getSimpleDescription() + ": " + way.getConstructorExample(propertyName), way));
                    }
                }
                return result;
            }

            @Override
            public String getDisplayName() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_CONSTRUCTOR"); //NOI18N
            }

            @Override
            public String getDialogTitle() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_TITLE_CONSTRUCTOR"); //NOI18N
            }

            @Override
            public String getTemplateText(final CGSInfo cgsInfo, final JTextComponent textComponent) {
                final StringBuilder params = new StringBuilder();
                final StringBuilder assignments = new StringBuilder();
                for (Property property : cgsInfo.getProperties()) {
                    final String name = property.getName();
                    final String paramName = cgsInfo.getHowToGenerate() == GenWay.WITHOUT_UNDERSCORE
                                ? withoutUnderscore(name) : name;
                    if (property.isSelected()) {
                        params.append(", "); //NOI18N
                        String type = property.getType();
                        if (type != null && !type.isEmpty()) {
                            params.append(property.getTypeForTemplate());
                        }
                        params.append("$").append(paramName);        //NOI18N
                        assignments.append(ASSIGNMENT_TEMPLATE.replace(PROPERTY, name).replace(PARAM_NAME, paramName));
                    }
                }
                if (params.length() == 0) {
                    params.append(", ");                                        //NOI18N
                }
                return CONSTRUCTOR_TEMPLATE.replace(PARAMS, params.toString().substring(2)).replace(ASSIGNMENTS, assignments);
            }

        },
        GETTER(PanelStrategy.CONSTRUCTOR, FluentSetterStrategy.INVISIBLE) {

            @Override
            public String getPanelTitle() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_PANEL_GETTERS"); //NOI18N
            }

            @Override
            public ComboBoxModel getModel(final String propertyName) {
                final DefaultComboBoxModel result = new DefaultComboBoxModel();
                for (CGSGenerator.GenWay way : CGSGenerator.GenWay.values()) {
                    result.addElement(new ComboBoxModelElement(way.getSimpleDescription() + ": " + way.getGetterExample(propertyName), way));
                }
                return result;
            }

            @Override
            public String getDisplayName() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_GETTER"); //NOI18N
            }

            @Override
            public String getDialogTitle() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_TITLE_GETTERS"); //NOI18N
            }

            @Override
            public String getTemplateText(final CGSInfo cgsInfo, final JTextComponent textComponent) {
                final StringBuilder getters = new StringBuilder();
                for (Property property : cgsInfo.getPossibleGetters()) {
                    if (property.isSelected()) {
                        final String name = property.getName();
                        final String changedName = cgsInfo.getHowToGenerate() == GenWay.WITHOUT_UNDERSCORE
                                ? upFirstLetterWithoutUnderscore(name) : upFirstLetter(name);
                        final String methodName = getUnusedMethodName(new ArrayList<String>(), changedName);
                        getters.append(
                                getGetterTemplate(cgsInfo)
                                .replace(FUNCTION_MODIFIER, property.getFunctionModifier())
                                .replace(UNDERSCORED_METHOD_NAME, name)
                                .replace(ACCESSOR, property.getAccessor())
                                .replace(PROPERTY, property.getAccessedName())
                                .replace(UP_FIRST_LETTER_PROPERTY, methodName)
                                .replace(UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE, methodName));
                        getters.append(NEW_LINE);
                    }
                }
                return getters.toString();
            }

        },
        SETTER(PanelStrategy.CONSTRUCTOR, FluentSetterStrategy.VISIBLE) {

            @Override
            public String getPanelTitle() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_PANEL_SETTERS"); //NOI18N
            }

            @Override
            public ComboBoxModel getModel(final String propertyName) {
                final DefaultComboBoxModel result = new DefaultComboBoxModel();
                for (CGSGenerator.GenWay way : CGSGenerator.GenWay.values()) {
                    result.addElement(new ComboBoxModelElement(way.getSimpleDescription() + ": " + way.getSetterExample(propertyName), way));
                }
                return result;
            }

            @Override
            public String getDisplayName() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_SETTER"); //NOI18N
            }

            @Override
            public String getDialogTitle() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_TITLE_SETTERS"); //NOI18N
            }

            @Override
            public String getTemplateText(final CGSInfo cgsInfo, final JTextComponent textComponent) {
                StringBuilder setters = new StringBuilder();
                FluentSetterCreator fluentSetterCreator = new FluentSetterCreator(cgsInfo.isFluentSetter());
                for (Property property : cgsInfo.getPossibleSetters()) {
                    if (property.isSelected()) {
                        final String name = property.getName();
                        final String paramName = cgsInfo.getHowToGenerate() == GenWay.WITHOUT_UNDERSCORE
                                ? withoutUnderscore(name) : name;
                        final String type = property.getType();
                        String changedName = cgsInfo.getHowToGenerate() == GenWay.WITHOUT_UNDERSCORE
                                ? upFirstLetterWithoutUnderscore(name) : upFirstLetter(name);
                        final String methodName = getUnusedMethodName(new ArrayList<String>(), changedName);
                        setters.append(
                                getSetterTemplate(cgsInfo)
                                .replace(FUNCTION_MODIFIER, property.getFunctionModifier())
                                .replace(UNDERSCORED_METHOD_NAME, name)
                                .replace(ACCESSOR, property.getAccessor())
                                .replace(PROPERTY, property.getAccessedName())
                                .replace(FLUENT_SETTER, fluentSetterCreator.create(property))
                                .replace(PARAM_NAME, paramName)
                                .replace(UP_FIRST_LETTER_PROPERTY, methodName)
                                .replace(UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE, methodName)
                                .replace(PARAM_TYPE, type.isEmpty() ? type : property.getTypeForTemplate()));
                        setters.append(NEW_LINE);
                    }
                }
                return setters.toString();
            }

        },
        GETTER_AND_SETTER(PanelStrategy.CONSTRUCTOR, FluentSetterStrategy.VISIBLE) {

            @Override
            public String getPanelTitle() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_PANEL_GETTERS_AND_SETTERS"); //NOI18N
            }

            @Override
            public ComboBoxModel getModel(final String propertyName) {
                final DefaultComboBoxModel result = new DefaultComboBoxModel();
                for (CGSGenerator.GenWay way : CGSGenerator.GenWay.values()) {
                    result.addElement(
                            new ComboBoxModelElement(way.getSimpleDescription() + ": " + way.getGetterExample(propertyName) + ", " + way.getSetterExample(propertyName), way));
                }
                return result;
            }

            @Override
            public String getDisplayName() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_GETTER_AND_SETTER");  //NOI18N
            }

            @Override
            public String getDialogTitle() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_TITLE_GETTERS_AND_SETTERS"); //NOI18N
            }

            @Override
            public String getTemplateText(final CGSInfo cgsInfo, final JTextComponent textComponent) {
                final StringBuilder gettersAndSetters = new StringBuilder();
                FluentSetterCreator fluentSetterCreator = new FluentSetterCreator(cgsInfo.isFluentSetter());
                for (Property property : cgsInfo.getPossibleSetters()) {
                    if (property.isSelected()) {
                        final String name = property.getName();
                        String changedName = cgsInfo.getHowToGenerate() == GenWay.WITHOUT_UNDERSCORE
                                ? upFirstLetterWithoutUnderscore(name) : upFirstLetter(name);
                        final String methodName = getUnusedMethodName(new ArrayList<String>(), changedName);
                        final String paramName = cgsInfo.getHowToGenerate() == GenWay.WITHOUT_UNDERSCORE
                                ? withoutUnderscore(name) : name;
                        gettersAndSetters.append(
                                getGetterTemplate(cgsInfo)
                                .replace(FUNCTION_MODIFIER, property.getFunctionModifier())
                                .replace(UNDERSCORED_METHOD_NAME, name)
                                .replace(ACCESSOR, property.getAccessor())
                                .replace(PROPERTY, property.getAccessedName())
                                .replace(UP_FIRST_LETTER_PROPERTY, upFirstLetter(methodName))
                                .replace(UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE, methodName));
                        gettersAndSetters.append(NEW_LINE);
                        final String type = property.getType();
                        gettersAndSetters.append(
                                getSetterTemplate(cgsInfo)
                                .replace(FUNCTION_MODIFIER, property.getFunctionModifier())
                                .replace(UNDERSCORED_METHOD_NAME, name)
                                .replace(ACCESSOR, property.getAccessor())
                                .replace(PROPERTY, property.getAccessedName())
                                .replace(FLUENT_SETTER, fluentSetterCreator.create(property))
                                .replace(PARAM_NAME, paramName)
                                .replace(UP_FIRST_LETTER_PROPERTY, upFirstLetter(methodName))
                                .replace(UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE, methodName)
                                .replace(PARAM_TYPE, type.isEmpty() ? type : property.getTypeForTemplate()));
                        gettersAndSetters.append(NEW_LINE);
                    }
                }
                return gettersAndSetters.toString();
            }

        },
        METHODS(PanelStrategy.METHOD, FluentSetterStrategy.INVISIBLE) {

            @Override
            public String getPanelTitle() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_PANEL_METHODS"); //NOI18N
            }

            @Override
            public ComboBoxModel getModel(final String propertyName) {
                return new DefaultComboBoxModel();
            }

            @Override
            public String getDisplayName() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_METHOD"); //NOI18N
            }

            @Override
            public String getDialogTitle() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_TITLE_METHODS"); //NOI18N
            }

            @Override
            public String getTemplateText(final CGSInfo cgsInfo, final JTextComponent textComponent) {
                final StringBuilder inheritedMethods = new StringBuilder();
                for (MethodProperty methodProperty : cgsInfo.getPossibleMethods()) {
                    if (methodProperty.isSelected()) {
                        final MethodElement method = methodProperty.getMethod();
                        final TypeNameResolver typeNameResolver = method.getParameters().isEmpty()
                                ? TypeNameResolverImpl.forNull()
                                : CodegenUtils.createSmarterTypeNameResolver(
                                        method,
                                        ModelUtils.getModel(Source.create(textComponent.getDocument()), 300),
                                        textComponent.getCaretPosition());
                        if (method.isAbstract() || method.isMagic() || method.getType().isInterface()) {
                            inheritedMethods.append(method.asString(PrintAs.DeclarationWithEmptyBody, typeNameResolver).replace("abstract ", "")); //NOI18N;
                        } else {
                            inheritedMethods.append(method.asString(PrintAs.DeclarationWithParentCallInBody, typeNameResolver).replace("abstract ", "")); //NOI18N;
                        }
                        inheritedMethods.append(NEW_LINE);
                    }
                }
                return inheritedMethods.toString();
            }

        };

        private final PanelStrategy panelStrategy;
        private final FluentSetterStrategy fluentSetterStrategy;

        public abstract String getPanelTitle();
        public abstract ComboBoxModel getModel(final String propertyName);
        public abstract String getDisplayName();
        public abstract String getDialogTitle();
        public abstract String getTemplateText(final CGSInfo cgsInfo, final JTextComponent textComponent);

        private GenType(final PanelStrategy panelStrategy, final FluentSetterStrategy fluentSetterStrategy) {
            this.panelStrategy = panelStrategy;
            this.fluentSetterStrategy = fluentSetterStrategy;
        }

        public JPanel createPanel(final CGSInfo cgsInfo) {
            return panelStrategy.createPanel(this, cgsInfo);
        }

        public boolean isFluentSetterVisible() {
            return fluentSetterStrategy.isFluentSetterVisible();
        }

        String getGetterTemplate(final CGSInfo cgsInfo) {
            return GETTER_TEMPLATE.replace(TEMPLATE_NAME, cgsInfo.getHowToGenerate().getGetterTemplate());
        }

        String getSetterTemplate(final CGSInfo cgsInfo) {
            return SETTER_TEMPLATE.replace(TEMPLATE_NAME, cgsInfo.getHowToGenerate().getSetterTemplate());
        }

        private enum PanelStrategy {
            CONSTRUCTOR {
                @Override
                JPanel createPanel(final GenType genType, final CGSInfo cgsInfo) {
                    return new ConstructorPanel(genType, cgsInfo);
                }
            },
            METHOD {
                @Override
                JPanel createPanel(final GenType genType, final CGSInfo cgsInfo) {
                    return new MethodPanel(cgsInfo);
                }
            };

            abstract JPanel createPanel(final GenType genType, final CGSInfo cgsInfo);
        }

        private enum FluentSetterStrategy {
            VISIBLE {
                @Override
                boolean isFluentSetterVisible() {
                    return true;
                }
            },
            INVISIBLE {
                @Override
                boolean isFluentSetterVisible() {
                    return false;
                }
            };

            abstract boolean isFluentSetterVisible();
        }

        private static final class FluentSetterCreator {
            private final boolean isStatic;

            public FluentSetterCreator(boolean isStatic) {
                this.isStatic = isStatic;
            }

            public String create(Property property) {
                assert property != null;
                return isStatic ? "return " + property.getFluentReturnAccessor() + ";" + NEW_LINE : ""; //NOI18N
            }

        }

    }

    public enum GenWay {
        AS_JAVA(
                NbBundle.getMessage(CGSGenerator.class, "JAVA_STYLE"),
                "__construct($" + PROPERTY + ")",
                START_OF_GETTER + UP_FIRST_LETTER_PROPERTY,
                START_OF_SETTER + UP_FIRST_LETTER_PROPERTY), //NOI18N
        WITH_UNDERSCORE(
                NbBundle.getMessage(CGSGenerator.class, "ADD_UNDERSCORE"),
                "__construct($" + PROPERTY + ")",
                START_OF_GETTER + "_" + UNDERSCORED_METHOD_NAME,
                START_OF_SETTER + "_" + UNDERSCORED_METHOD_NAME), //NOI18N
        WITHOUT_UNDERSCORE(
                NbBundle.getMessage(CGSGenerator.class, "REMOVE_UNDERSCORE"),
                "__construct($" + PROPERTY_WITHOUT_UNDERSCORE + ")",
                START_OF_GETTER + UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE,
                START_OF_SETTER + UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE); //NOI18N

        private String constructorTemplate;
        private String getterTemplate;
        private String setterTemplate;
        /**
         * The description will appear in the ui combobox.
         */
        private String simpleDescription;

        private GenWay(String simpleDescription, String constructorTemplate, String getterTemplate, String setterTemplate) {
            this.constructorTemplate = constructorTemplate;
            this.getterTemplate = getterTemplate;
            this.setterTemplate = setterTemplate;
            this.simpleDescription = simpleDescription;
        }

        public String getConstructorTemplate() {
            return constructorTemplate;
        }

        public String getGetterTemplate() {
            return getterTemplate;
        }

        public String getSetterTemplate() {
            return setterTemplate;
        }

        public String getSimpleDescription() {
            return simpleDescription;
        }

        public String getConstructorExample(String property) {
            return createExample(getConstructorTemplate(), property);
        }

        public String getGetterExample(String property) {
             return createExample(getGetterTemplate(), property);
        }

        public String getSetterExample(String property) {
             return createExample(getSetterTemplate(), property);
        }

        private String createExample(String template, String property) {
            String example = template;
            if (template.contains(PROPERTY)) {
                example = example.replace(PROPERTY, property);
            }
            if (template.contains(UNDERSCORED_METHOD_NAME)) {
                example = example.replace(UNDERSCORED_METHOD_NAME, property);
            }
            if (template.contains(UP_FIRST_LETTER_PROPERTY)) {
                example = example.replace(UP_FIRST_LETTER_PROPERTY, upFirstLetter(property));
            }
            if (template.contains(UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE)) {
                example = example.replace(UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE, upFirstLetterWithoutUnderscore(property));
            }
            if (template.contains(PROPERTY_WITHOUT_UNDERSCORE)) {
                example = example.replace(PROPERTY_WITHOUT_UNDERSCORE, withoutUnderscore(property));
            }
            return example;
        }
    }

    //constructor
    private static final String PARAMS = "${PARAMS}";                               //NOI18N
    private static final String ASSIGNMENTS = "${ASSIGNMENT}";                       //NOI18N
    private static final String TEMPLATE_NAME = "${TEMPLATE_NAME}";                 //NOI18N
    private static final String CONSTRUCTOR_TEMPLATE = "function __construct(" + PARAMS + ") {" + ASSIGNMENTS  + CURSOR + NEW_LINE + "}" + NEW_LINE;    //NOI18N
    private static final String ASSIGNMENT_TEMPLATE = NEW_LINE + ACCESSOR + PROPERTY + " = $" + PARAM_NAME + ";";          //NOI18N
    private static final String GETTER_TEMPLATE
            = "public " + FUNCTION_MODIFIER + " function " + TEMPLATE_NAME + "() {"
            + NEW_LINE + "return " + ACCESSOR + PROPERTY + ";" + NEW_LINE + "}" + NEW_LINE;    //NOI18N
    private static final String SETTER_TEMPLATE
            = "public " + FUNCTION_MODIFIER + " function " + TEMPLATE_NAME + "(" + PARAM_TYPE + "$$" + PARAM_NAME + ") {"
            + ASSIGNMENT_TEMPLATE + NEW_LINE + FLUENT_SETTER + "}" + NEW_LINE; //NOI18N
    private final GenType genType;
    private final CGSInfo cgsInfo;
    private final JTextComponent component;

    private static final String GETTER_SETTER_PROJECT_PROPERTY = "getter.setter.method.name.generation";
    private static final String FLUENT_SETTER_PROJECT_PROPERTY = "fluent.setter.project.property"; //NOI18N

    private CGSGenerator(JTextComponent component, CGSInfo cgsInfo, GenType type) {
        this.genType = type;
        this.cgsInfo = cgsInfo;
        this.component = component;
    }

    @Override
    public void invoke() {
        // obtain the generation from project properties
        FileObject fo = NbEditorUtilities.getFileObject(component.getDocument());
        Project project = FileOwnerQuery.getOwner(fo);
        if (project != null) {
            Preferences preferences = ProjectUtils.getPreferences(project, CGSGenerator.class, false);
            try {
                cgsInfo.setHowToGenerate(GenWay.valueOf(preferences.get(GETTER_SETTER_PROJECT_PROPERTY, GenWay.AS_JAVA.name())));
            } catch (IllegalArgumentException ex) {
                cgsInfo.setHowToGenerate(GenWay.AS_JAVA);
            }
            cgsInfo.setFluentSetter(preferences.getBoolean(FLUENT_SETTER_PROJECT_PROPERTY, false));
            DialogDescriptor desc = new DialogDescriptor(genType.createPanel(cgsInfo), genType.getDialogTitle());
            Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
            dialog.setVisible(true);
            dialog.dispose();
            if (desc.getValue() == DialogDescriptor.OK_OPTION) {
                CodeTemplateManager manager = CodeTemplateManager.get(component.getDocument());
                CodeTemplate template = manager.createTemporary(genType.getTemplateText(cgsInfo, component));
                template.insert(component);
                //save the gen type value to the project properties
                preferences.put(GETTER_SETTER_PROJECT_PROPERTY, cgsInfo.getHowToGenerate().name());
                preferences.putBoolean(FLUENT_SETTER_PROJECT_PROPERTY, cgsInfo.isFluentSetter());
            }
        }
    }

    @Override
    public String getDisplayName() {
        return genType.getDisplayName();
    }

    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            JTextComponent textComp = context.lookup(JTextComponent.class);
            ArrayList<CodeGenerator> ret = new ArrayList<CodeGenerator>();
            CGSInfo info = CGSInfo.getCGSInfo(textComp);

            if (info.getClassName() != null) { // is the cursor in a class?
                if (!info.hasConstructor()) {
                    ret.add(new CGSGenerator(textComp, info, GenType.CONSTRUCTOR));
                }
                if (info.getPossibleGetters().size() > 0) {
                    ret.add(new CGSGenerator(textComp, info, GenType.GETTER));
                }
                if (info.getPossibleSetters().size() > 0) {
                    ret.add(new CGSGenerator(textComp, info, GenType.SETTER));
                }
                if (info.getPossibleGettersSetters().size() > 0) {
                    ret.add(new CGSGenerator(textComp, info, GenType.GETTER_AND_SETTER));
                }
                if (info.getPossibleMethods().size() > 0) {
                    ret.add(new CGSGenerator(textComp, info, GenType.METHODS));
                }
            }
            return ret;
        }
    }

    private static String getUnusedMethodName(List<String> usedMethods, String methodName) {
        if (usedMethods.contains(methodName)) {
            int counter = 1;
            while (usedMethods.contains(methodName + "_" + counter)) {  //NOI18N
                counter++;
            }
            methodName = methodName + "_" + counter;        //NOI18N
        }
        usedMethods.add(methodName);
        return methodName;
    }

    private static String upFirstLetter(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private static String upFirstLetterWithoutUnderscore(String name) {
        return upFirstLetter(withoutUnderscore(name));
    }

    private static String withoutUnderscore(String name) {
        return (name.length() > 0 && name.charAt(0) == '_') ? name.substring(1) : name;
    }
}
