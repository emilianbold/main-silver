/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.php.editor.codegen;

import java.awt.Dialog;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement.PrintAs;
import org.netbeans.modules.php.editor.codegen.ui.ConstructorPanel;
import org.netbeans.modules.php.editor.codegen.ui.MethodPanel;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class CGSGenerator implements CodeGenerator {

    protected static final String START_OF_GETTER = "get";    //NOI18N
    protected static final String START_OF_SETTER = "set";    //NOI18N

    private static final String NEW_LINE = System.getProperty("line.separator");    //NOI18N
    private static final String PROPERTY = "${PROPERTY}";                           //NOI18N
    private static final String CURSOR = "${cursor}";                           //NOI18N
    private static final String UP_FIRST_LETTER_PROPERTY = "${UpFirstLetterProperty}";  //NOI18N
    private static final String UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE = "${UpFirstLetterPropertyWithoutUnderscore}";  //NOI18N

    public enum GenType {
        CONSTRUCTOR,
        GETTER,
        SETTER,
        GETTER_AND_SETTER,
        METHODS;
    }

    public enum GenWay {
        AS_JAVA(NbBundle.getMessage(CGSGenerator.class, "JAVA_STYLE"), START_OF_GETTER + UP_FIRST_LETTER_PROPERTY, START_OF_SETTER + UP_FIRST_LETTER_PROPERTY), //NOI18N
        WITH_UNDERSCORE(NbBundle.getMessage(CGSGenerator.class, "ADD_UNDERSCORE"), START_OF_GETTER + "_" + PROPERTY, START_OF_SETTER + "_" + PROPERTY), //NOI18N
        WITHOUT_UNDERSCORE(NbBundle.getMessage(CGSGenerator.class, "REMOVE_UNDERSCORE"), START_OF_GETTER + UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE, START_OF_SETTER + UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE);    //NOI18N

        private String getterTemplate;
        private String setterTemplate;
        /**
         * The description will appear in the ui combobox.
         */
        private String simpleDescription;

        private GenWay(String simpleDescription, String getterTemplate, String setterTemplate) {
            this.getterTemplate = getterTemplate;
            this.setterTemplate = setterTemplate;
            this.simpleDescription = simpleDescription;
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
            if (template.contains(UP_FIRST_LETTER_PROPERTY)) {
                example = example.replace(UP_FIRST_LETTER_PROPERTY, upFirstLetter(property));
            }
            if (template.contains(UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE)) {
                example = example.replace(UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE, upFirstLetterWithoutUnderscore(property));
            }
            return example;
        }
    }

    //constructor
    private static final String PARAMS = "${PARAMS}";                               //NOI18N
    private static final String ASSIGNMENTS = "${ASSIGNMENT}";                       //NOI18N
    private static final String TEMPLATE_NAME = "${TEMPLATE_NAME}";                 //NOI18N
    private static final String CONSTRUCTOR_TEMPLATE = "function __construct(" + PARAMS + ") {" + ASSIGNMENTS  + CURSOR + NEW_LINE + "}" + NEW_LINE;    //NOI18N
    private static final String ASSIGNMENT_TEMPLATE = NEW_LINE + "$this->" + PROPERTY + " = $" + PROPERTY + ";";          //NOI18N
    private static final String GETTER_TEMPLATE = "public function " + TEMPLATE_NAME + "() {" + NEW_LINE + "return $$this->" + PROPERTY + ";" + NEW_LINE + "}" + NEW_LINE;    //NOI18N
    private static final String SETTER_TEMPLATE = "public function " + TEMPLATE_NAME + "($$" + PROPERTY + ") {" + ASSIGNMENT_TEMPLATE + NEW_LINE + "}" + NEW_LINE; //NOI18N
    private final GenType type;
    private final CGSInfo cgsInfo;
    private final JTextComponent component;

    private static final String GETTER_SETTER_PROJECT_PROPERTY = "getter.setter.method.name.generation";

    private CGSGenerator(JTextComponent component, CGSInfo cgsInfo, GenType type) {
        this.type = type;
        this.cgsInfo = cgsInfo;
        this.component = component;
    }

    @Override
    public void invoke() {
        String dialogTitle = null;
        JPanel panel = null;
        String methodGenerationWay = null;
        AntProjectHelper helper = null;
        EditableProperties properties = null;

        // obtain the generation from project properties
        FileObject fo = NbEditorUtilities.getFileObject(component.getDocument());
        Project project = FileOwnerQuery.getOwner(fo);
        if (project != null) {
            helper = project.getLookup().lookup(AntProjectHelper.class);
            properties = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            methodGenerationWay = properties.getProperty(GETTER_SETTER_PROJECT_PROPERTY);
        }
        if (methodGenerationWay != null) {
            try {
                cgsInfo.setHowToGenerate(GenWay.valueOf(methodGenerationWay));
            } catch (IllegalArgumentException exception) {
                cgsInfo.setHowToGenerate(GenWay.AS_JAVA);
            }
        } else {
            cgsInfo.setHowToGenerate(GenWay.AS_JAVA);
        }
       
        switch (type) {
            case CONSTRUCTOR:
                dialogTitle = NbBundle.getMessage(CGSGenerator.class, "LBL_TITLE_CONSTRUCTOR");    //NOI18N
                panel = new ConstructorPanel(type, cgsInfo);
                break;
            case GETTER:
                dialogTitle = NbBundle.getMessage(CGSGenerator.class, "LBL_TITLE_GETTERS");    //NOI18N
                panel = new ConstructorPanel(type, cgsInfo);
                break;
            case SETTER:
                dialogTitle = NbBundle.getMessage(CGSGenerator.class, "LBL_TITLE_SETTERS");    //NOI18N
                panel = new ConstructorPanel(type, cgsInfo);
                break;
            case GETTER_AND_SETTER:
                dialogTitle = NbBundle.getMessage(CGSGenerator.class, "LBL_TITLE_GETTERS_AND_SETTERS");    //NOI18N
                panel = new ConstructorPanel(type, cgsInfo);
                break;
            case METHODS:
                dialogTitle = NbBundle.getMessage(CGSGenerator.class, "LBL_TITLE_METHODS");    //NOI18N
                panel = new MethodPanel(cgsInfo);
                break;

        }

        DialogDescriptor desc = new DialogDescriptor(panel, dialogTitle);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
        dialog.setVisible(true);
        dialog.dispose();
        if (desc.getValue() == DialogDescriptor.OK_OPTION) {
            CodeTemplateManager manager = CodeTemplateManager.get(component.getDocument());
            CodeTemplate template = manager.createTemporary(getTemplateText());
            template.insert(component);
            //save the gen type value to the project properties
            if (project != null) {
                properties.put(GETTER_SETTER_PROJECT_PROPERTY, cgsInfo.getHowToGenerate().name());
                helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, properties);
            }
        }
    }

    @Override
    public String getDisplayName() {
        String name = null;
        switch (type) {
            case CONSTRUCTOR:
                name = NbBundle.getMessage(CGSGenerator.class, "LBL_CONSTRUCTOR");    //NOI18N
                break;
            case GETTER:
                name = NbBundle.getMessage(CGSGenerator.class, "LBL_GETTER");    //NOI18N
                break;
            case SETTER:
                name = NbBundle.getMessage(CGSGenerator.class, "LBL_SETTER");    //NOI18N
                break;
            case GETTER_AND_SETTER:
                name = NbBundle.getMessage(CGSGenerator.class, "LBL_GETTER_AND_SETTER");    //NOI18N
                break;
            case METHODS:
                name = NbBundle.getMessage(CGSGenerator.class, "LBL_METHOD");    //NOI18N
                break;
        }
        return name;
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

    private String getTemplateText() {
        String text = null;
        String name;
        String methodName;
        String getterTemplate = GETTER_TEMPLATE.replace(TEMPLATE_NAME, cgsInfo.getHowToGenerate().getGetterTemplate());
        String setterTemplate = SETTER_TEMPLATE.replace(TEMPLATE_NAME, cgsInfo.getHowToGenerate().getSetterTemplate());

        ArrayList<String> createdMethods = new ArrayList<String>();
        switch (type) {
            case CONSTRUCTOR:
                StringBuilder params = new StringBuilder();
                StringBuilder assignments = new StringBuilder();
                for (Property property : cgsInfo.getProperties()) {
                    name = property.getName();
                    if (property.isSelected()) {
                        params.append(", $").append(name);        //NOI18N
                        assignments.append(ASSIGNMENT_TEMPLATE.replace(PROPERTY, name));
                    }
                }
                if (params.length() == 0) {
                    params.append(", ");                                        //NOI18N
                }
                text = CONSTRUCTOR_TEMPLATE.replace(PARAMS, params.toString().substring(2)).replace(ASSIGNMENTS, assignments);
                break;
            case GETTER:
                StringBuilder getters = new StringBuilder();
                for (Property property : cgsInfo.getPossibleGetters()) {
                    if (property.isSelected()) {
                        name = property.getName();
                        String changedName = cgsInfo.getHowToGenerate() == GenWay.WITHOUT_UNDERSCORE 
                                ? upFirstLetterWithoutUnderscore(name) : upFirstLetter(name);
                        methodName = getUnusedMethodName(createdMethods, changedName);
                        getters.append(getterTemplate.replace(PROPERTY, name).replace(UP_FIRST_LETTER_PROPERTY, methodName).replace(UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE, methodName));
                        getters.append(NEW_LINE);
                    }
                }
                text = getters.toString();
                break;
            case SETTER:
                StringBuilder setters = new StringBuilder();
                for (Property property : cgsInfo.getPossibleSetters()) {
                    if (property.isSelected()) {
                        name = property.getName();
                        String changedName = cgsInfo.getHowToGenerate() == GenWay.WITHOUT_UNDERSCORE
                                ? upFirstLetterWithoutUnderscore(name) : upFirstLetter(name);
                        methodName = getUnusedMethodName(createdMethods, changedName);
                        setters.append(setterTemplate.replace(PROPERTY, name).replace(UP_FIRST_LETTER_PROPERTY, methodName).replace(UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE, methodName));
                        setters.append(NEW_LINE);
                    }
                }
                text = setters.toString();
                break;
            case GETTER_AND_SETTER:
                StringBuilder gettersAndSetters = new StringBuilder();
                for (Property property : cgsInfo.getPossibleSetters()) {
                    if (property.isSelected()) {
                        name = property.getName();
                        String changedName = cgsInfo.getHowToGenerate() == GenWay.WITHOUT_UNDERSCORE
                                ? upFirstLetterWithoutUnderscore(name) : upFirstLetter(name);
                        methodName = getUnusedMethodName(createdMethods, changedName);
                        gettersAndSetters.append(getterTemplate.replace(PROPERTY, name).replace(UP_FIRST_LETTER_PROPERTY, upFirstLetter(methodName)).replace(UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE, methodName));
                        gettersAndSetters.append(NEW_LINE);
                        gettersAndSetters.append(setterTemplate.replace(PROPERTY, name).replace(UP_FIRST_LETTER_PROPERTY, upFirstLetter(methodName)).replace(UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE, methodName));
                        gettersAndSetters.append(NEW_LINE);
                    }
                }
                text = gettersAndSetters.toString();
                break;
            case METHODS:
                StringBuilder inheritedMethods = new StringBuilder();
                for (MethodProperty methodProperty : cgsInfo.getPossibleMethods()) {
                    if (methodProperty.isSelected()) {
                        MethodElement method = methodProperty.getMethod();
                        if (method.isAbstract() || method.isMagic() || method.getType().isInterface()) {
                            inheritedMethods.append(method.asString(PrintAs.DeclarationWithEmptyBody).replace("abstract ", "")); //NOI18N;
                        } else {
                            inheritedMethods.append(method.asString(PrintAs.DeclarationWithParentCallInBody).replace("abstract ", "")); //NOI18N;
                        }
                        inheritedMethods.append(NEW_LINE);
                    }
                }
                text = inheritedMethods.toString();
                break;
        }
        return text;
    }

    private String getUnusedMethodName(List<String> usedMethods, String methodName) {
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
        String result = (name.length() > 0 && name.charAt(0) == '_') ? name.substring(1) : name;
        return upFirstLetter(result);
    }
}
