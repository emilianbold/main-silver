/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.completion;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import org.codehaus.groovy.ast.*;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.groovy.editor.api.completion.CaretLocation;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem.ConstructorItem;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem.ParameterDescriptor;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import org.netbeans.modules.groovy.editor.api.completion.util.ContextHelper;
import org.netbeans.modules.groovy.editor.completion.provider.CompleteElementHandler;
import org.netbeans.modules.groovy.editor.api.GroovyUtils;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;

/**
 * Complete the methods invokable on a class.
 *
 * @author Martin Janicek
 */
public class MethodCompletion extends BaseCompletion {

    // There attributes should be initiated after each complete() method call
    private List<CompletionProposal> proposals;
    private CompletionContext context;
    private int anchor;


    public MethodCompletion() {
    }


    @Override
    public boolean complete(final List<CompletionProposal> proposals, final CompletionContext context, final int anchor) {
        LOG.log(Level.FINEST, "-> completeMethods"); // NOI18N

        this.proposals = proposals;
        this.context = context;
        this.anchor = anchor;

        if (context == null || context.context == null || context.location == CaretLocation.INSIDE_PARAMETERS) {
            return false;
        }
        
        if (context.dotContext != null && context.dotContext.isFieldsOnly()) {
            return false;
        }

        // check whether we are either:
        //
        // 1.) This is a constructor-call like: String s = new String|
        // 2.) right behind a dot. Then we look for:
        //     2.1  method on collection type: List, Map or Range
        //     2.2  static/instance method on class or object
        //     2.3  Get apropriate groovy-methods from index.
        //     2.4  dynamic, mixin method on Groovy-object like getXbyY()


        // 1.) Test if this is a Constructor-call?
        if (ContextHelper.isConstructorCall(context)) {
            return completeConstructor();
        }

        // 2.2  static/instance method on class or object
        if (!context.isBehindDot() && context.context.before1 != null) {
            return false;
        }

        if (context.declaringClass == null) {
            LOG.log(Level.FINEST, "No declaring class found"); // NOI18N
            return false;
        }

        /*
            Here we need to figure out, whether we want to complete a variable:

            s.|

            where we want to complete fields and methodes *OR* a package prefix like:

            java.|

            To achive this we only complete methods if there is no basePackage, which is a valid
            package.
         */

        PackageCompletionRequest packageRequest = getPackageRequest(context);

        if (packageRequest.basePackage.length() > 0) {
            ClasspathInfo pathInfo = getClasspathInfoFromRequest(context);

            if (isValidPackage(pathInfo, packageRequest.basePackage)) {
                LOG.log(Level.FINEST, "The string before the dot seems to be a valid package"); // NOI18N
                return false;
            }
        }

        Map<MethodSignature, CompletionItem> result = new CompleteElementHandler(context).getMethods();
        proposals.addAll(result.values());

        return true;
    }

    /**
     * Constructor completion works for following types.
     *  1) Types in the same package
     *  2) Already imported types
     *  3) Groovy default imports
     *
     * @return true if we found some constructor proposal, false otherwise
     */
    private boolean completeConstructor() {
        LOG.log(Level.FINEST, "This looks like a constructor ...");

        // look for all imported types starting with prefix, which have public constructors
        final JavaSource javaSource = getJavaSourceFromRequest();
        if (javaSource != null) {
            try {
                javaSource.runUserActionTask(new Task<CompilationController>() {
                    @Override
                    public void run(CompilationController info) {

                        List<Element> typelist = new ArrayList<Element>();
                        for (String importName : getAllImports()) {
                            typelist.addAll(getElementListFor(info.getElements(), importName));
                        }
                        LOG.log(Level.FINEST, "Number of types found:  {0}", typelist.size());

                        if (exactConstructorExists(typelist, context.getPrefix())) {
                            // if we are in situation like "String s = new String|" we want to
                            // show only String constructors (not StringBuffer constructors etc.)
                            addExactProposals(typelist);
                        }
                        addConstructorProposalsForDeclaredClasses();
                    }
                }, true);
            } catch (IOException ex) {
                LOG.log(Level.FINEST, "IOException : {0}", ex.getMessage());
            }
        }

        return !proposals.isEmpty();
    }

    private List<String> getAllImports() {
        List<String> imports = new ArrayList<String>();
        imports.addAll(GroovyUtils.DEFAULT_IMPORT_PACKAGES);
        imports.addAll(getImportedTypes());
        imports.addAll(getTypesInSamePackage());
        imports.addAll(getTypesInSameFile());

        return imports;
    }

    private List<String> getImportedTypes() {
        List<String> importedTypes = new ArrayList<String>();

        ModuleNode moduleNode = ContextHelper.getSurroundingModuleNode(context);
        if (moduleNode != null) {
            // this gets the list of full-qualified names of imports.
            for (ImportNode importNode : moduleNode.getImports()) {
                importedTypes.add(importNode.getClassName());
            }

            // this returns a list of String's of wildcard-like included types.
            for (ImportNode wildcardImport : moduleNode.getStarImports()) {
                importedTypes.add(wildcardImport.getPackageName());
            }
        }
        return importedTypes;
    }

    private List<String> getTypesInSamePackage() {
        ModuleNode moduleNode = ContextHelper.getSurroundingModuleNode(context);
        if (moduleNode != null) {
            String packageName = ContextHelper.getSurroundingModuleNode(context).getPackageName();
            if (packageName != null) {
                packageName = packageName.substring(0, packageName.length() - 1); // Removing last '.' char

                return Collections.singletonList(packageName);
            }
        }
        return Collections.emptyList();
    }

    private List<String> getTypesInSameFile() {
        List<String> declaredClassNames = new ArrayList<String>();
        List<ClassNode> declaredClasses = ContextHelper.getDeclaredClasses(context);

        for (ClassNode declaredClass : declaredClasses) {
            declaredClassNames.add(declaredClass.getName());
        }
        return declaredClassNames;
    }

    /**
     * Finds out if the given prefix has an exact match to a one of given types.
     *
     * @param typelist list of types for comparison
     * @param prefix prefix we are looking for
     * @return true if there is an exact match, false otherwise
     */
    private boolean exactConstructorExists(List<? extends Element> typelist, String prefix) {
        for (Element element : typelist) {
            if (prefix.toUpperCase().equals(element.getSimpleName().toString().toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    private void addExactProposals(List<? extends Element> typelist) {
        for (Element element : typelist) {
            // only look for classes rather than enums or interfaces
            if (element.getKind() == ElementKind.CLASS) {
                for (Element encl : element.getEnclosedElements()) {
                    if (encl.getKind() == ElementKind.CONSTRUCTOR) {
                        // we gotta get the constructors name from the type itself, since
                        // all the constructors are named <init>.
                        String constructorName = element.getSimpleName().toString();

                        if (constructorName.toUpperCase().equals(context.getPrefix().toUpperCase())) {
                            addConstructorProposal(constructorName, (ExecutableElement) encl);
                        }
                    }
                }
            }
        }
    }

    private void addConstructorProposal(String constructorName, ExecutableElement encl) {
        String paramListString = getParameterListForMethod(encl);
        List<ParameterDescriptor> paramList = getParameterList(encl);
        
        ConstructorItem constructor = new ConstructorItem(constructorName, paramListString, paramList, anchor, false);
        if (!proposals.contains(constructor)) {
            proposals.add(constructor);
        }
    }

    private void addConstructorProposalsForDeclaredClasses() {
        for (ClassNode declaredClass : ContextHelper.getDeclaredClasses(context)) {
            addConstructorProposal(declaredClass);
        }
    }

    private void addConstructorProposal(ClassNode classNode) {
        String constructorName = classNode.getNameWithoutPackage();

        if (isPrefixed(context, constructorName)) {
            for (ConstructorNode constructor : classNode.getDeclaredConstructors()) {
                Parameter[] parameters = constructor.getParameters();
                String paramListString = getParameterListStringForMethod(parameters);
                List<ParameterDescriptor> paramList = getParameterListForMethod(parameters);

                proposals.add(new ConstructorItem(constructorName, paramListString, paramList, anchor, false));
            }
        }
    }

    private JavaSource getJavaSourceFromRequest() {
        ClasspathInfo pathInfo = getClasspathInfoFromRequest(context);
        assert pathInfo != null;

        JavaSource javaSource = JavaSource.create(pathInfo);

        if (javaSource == null) {
            LOG.log(Level.FINEST, "Problem retrieving JavaSource from ClassPathInfo, exiting.");
            return null;
        }

        return javaSource;
    }

    @NonNull
    private List<? extends Element> getElementListFor(Elements elements, final String importName) {
        if (elements != null && importName != null) {
            PackageElement packageElement = elements.getPackageElement(importName);

            if (packageElement != null) {
                return packageElement.getEnclosedElements();
            } else {
                TypeElement typeElement = elements.getTypeElement(importName);
                if (typeElement != null) {
                    return Collections.singletonList(typeElement);
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Get the list of parameters of this executable as a List of <code>ParameterDescriptor</code>'s
     * To be used in insert templates and pretty-printers.
     * 
     * @param exe executable element
     * @return list of <code>ParameterDescriptor</code>'s
     */
    private List<CompletionItem.ParameterDescriptor> getParameterList(ExecutableElement exe) {
        List<CompletionItem.ParameterDescriptor> paramList = new ArrayList<CompletionItem.ParameterDescriptor>();

        if (exe != null) {
            try {
                // generate a list of parameters
                // unfortunately, we have to work around # 139695 in an ugly fashion
                List<? extends VariableElement> params = exe.getParameters(); // this can cause NPE's
                int i = 1;

                for (VariableElement variableElement : params) {
                    TypeMirror tm = variableElement.asType();

                    String fullName = tm.toString();
                    String name = fullName;

                    if (tm.getKind() == javax.lang.model.type.TypeKind.DECLARED) {
                        name = GroovyUtils.stripPackage(fullName);
                    }

                    // todo: this needs to be replaced with values from the JavaDoc
                    String varName = "param" + String.valueOf(i);

                    paramList.add(new CompletionItem.ParameterDescriptor(fullName, name, varName));

                    i++;
                }
            } catch (NullPointerException e) {
                // simply do nothing.
            }
        }

        return paramList;
    }

    /**
     * Get the parameter-list of this executable as String.
     *
     * @param exe
     * @return
     */
    private String getParameterListForMethod(ExecutableElement exe) {
        StringBuilder sb = new StringBuilder();

        if (exe != null) {
            try {
                // generate a list of parameters
                // unfortunately, we have to work around # 139695 in an ugly fashion
                List<? extends VariableElement> params = exe.getParameters(); // this can cause NPE's

                for (VariableElement variableElement : params) {
                    TypeMirror tm = variableElement.asType();

                    if (sb.length() > 0) {
                        sb.append(", ");
                    }

                    if (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ARRAY) {
                        sb.append(GroovyUtils.stripPackage(tm.toString()));
                    } else {
                        sb.append(tm.toString());
                    }
                }
            } catch (NullPointerException e) {
                // simply do nothing.
            }
        }
        return sb.toString();
    }

    /**
     * Convert given parameter array into one String.
     * For example if we send two parameters (int and String), this method will
     * return "int, String"
     *
     * @param parameters array of parameters
     * @return parameter string
     */
    private String getParameterListStringForMethod(Parameter[] parameters) {
        if (parameters.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Parameter param : parameters) {
            sb.append(param.getType().getNameWithoutPackage()).append(", ");
        }

        // remove last comma and return
        return sb.substring(0, sb.lastIndexOf(","));
    }

    /**
     * Convert given parameter array into the list of <code>ParameterDescription</code>'s
     *
     * @param parameters array of parameters
     * @return list of <code>ParameterDescription</code>'s
     */
    private List<ParameterDescriptor> getParameterListForMethod(Parameter[] parameters) {
        if (parameters.length == 0) {
            return Collections.EMPTY_LIST;
        }

        List<ParameterDescriptor> paramDescriptors = new ArrayList<ParameterDescriptor>();
        for (Parameter param : parameters) {
            String fullTypeName = param.getType().getName();
            String typeName = param.getType().getNameWithoutPackage();
            String name = param.getName();

            paramDescriptors.add(new ParameterDescriptor(fullTypeName, typeName, name));
        }
        return paramDescriptors;
    }
}
