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

package org.netbeans.modules.groovy.editor.api.completion.impl;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.GroovyIndex;
import org.netbeans.modules.groovy.editor.api.GroovyUtils;
import org.netbeans.modules.groovy.editor.api.NbUtilities;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.util.CamelCaseUtil;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionRequest;
import org.netbeans.modules.groovy.editor.api.completion.util.RequestHelper;
import org.netbeans.modules.groovy.editor.api.elements.IndexedClass;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;

/**
 * Complete the Groovy and Java types available at this position.
 * 
 * This could be either:
 * 1.) Completing all available Types in a given package. This is used for:
 * 1.1) import statements completion
 * 1.2) If you simply want to give the fq-name for something.
 *
 * 2.) Complete the types which are available without having to give a fqn:
 * 2.1.) Types defined in the Groovy File where the completion is invoked. (INDEX)
 * 2.2.) Types located in the same package (source or binary). (INDEX)
 * 2.3.) Types manually imported via the "import" statement. (AST)
 * 2.4.) The Default imports for Groovy, which are a super-set of Java. (NB JavaSource)
 *
 * These are the Groovy default imports:
 *
 * java.io.*
 * java.lang.*
 * java.math.BigDecimal
 * java.math.BigInteger
 * java.net.*
 * java.util.*
 * groovy.lang.*
 * groovy.util.*
 *
 * @author Martin Janicek
 */
public class TypesCompletion extends BaseCompletion {

    // There attributes should be initiated for each complete() method call
    private List<CompletionProposal> proposals;
    private CompletionRequest request;
    private int anchor;
    private boolean constructorCompletion;

    
    @Override
    public boolean complete(List<CompletionProposal> proposals, CompletionRequest request, int anchor) {
        LOG.log(Level.FINEST, "-> completeTypes"); // NOI18N

        this.proposals = proposals;
        this.request = request;
        this.anchor = anchor;

        final PackageCompletionRequest packageRequest = getPackageRequest(request);

        // todo: we don't handle single dots in the source. In that case we should
        // find the class we are living in. Disable it for now.

        if (packageRequest.basePackage.length() == 0
                && packageRequest.prefix.length() == 0
                && packageRequest.fullString.equals(".")) {
            return false;
        }

        // check for a constructor call
        if (RequestHelper.isConstructorCall(request)) {
            constructorCompletion = true;
        } else {
            constructorCompletion = false;
        }

        // are we dealing with a class xyz implements | {
        // kind of completion?

        boolean onlyInterfaces = false;

        Token<? extends GroovyTokenId> literal = request.ctx.beforeLiteral;
        if (literal != null) {
            
            // We don't need to complete Types after class definition
            if (literal.id() == GroovyTokenId.LITERAL_class) {
                return false;
            }

            if (literal.id() == GroovyTokenId.LITERAL_implements) {
                LOG.log(Level.FINEST, "Completing only interfaces after implements keyword.");
                onlyInterfaces = true;
            }
        }


        Set<TypeHolder> addedTypes = new HashSet<TypeHolder>();

        // This ModuleNode is used to retrieve the types defined here and the package name.
        ModuleNode moduleNode = retrieveModuleNode();
        String currentPackage = getCurrentPackageName(moduleNode);
        JavaSource javaSource = getJavaSourceFromRequest();

        // if we are dealing with a basepackage we simply complete all the packages given in the basePackage
        if (packageRequest.basePackage.length() > 0 || request.behindImport) {
            if (!(request.behindImport && packageRequest.basePackage.length() == 0)) {

                List<TypeHolder> typeList = getTypeHoldersForPackage(javaSource, packageRequest.basePackage, currentPackage);

                LOG.log(Level.FINEST, "Number of types found:  {0}", typeList.size());

                for (TypeHolder singleType : typeList) {
                    addToProposalUsingFilter(addedTypes, singleType, onlyInterfaces);
                }
            }
            return true;
        }

        // dont want types for objectExpression.something
        if (request.isBehindDot()) {
            return false;
        }

        // Retrieve the package we are living in from AST and then
        // all classes from that package using the Groovy Index.

        if (moduleNode != null) {
            LOG.log(Level.FINEST, "We are living in package : {0} ", currentPackage);

            // FIXME parsing API
            GroovyIndex index = null;
            FileObject fo = request.info.getSnapshot().getSource().getFileObject();
            if (fo != null) {
                index = GroovyIndex.get(QuerySupport.findRoots(fo,
                        Collections.singleton(ClassPath.SOURCE),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList()));
            }

            if (index != null) {
                Set<IndexedClass> classes = index.getClasses("", QuerySupport.Kind.PREFIX, true, false, false);

                if (classes.isEmpty()) {
                    LOG.log(Level.FINEST, "Nothing found in GroovyIndex");
                } else {
                    LOG.log(Level.FINEST, "Found this number of classes : {0} ", classes.size());

                    Set<TypeHolder> typelist = new HashSet<TypeHolder>();

                    for (IndexedClass indexedClass : classes) {
                        LOG.log(Level.FINEST, "FQN classname from index : {0} ", indexedClass.getFqn());

                        ElementKind ek;
                        if (indexedClass.getKind() == org.netbeans.modules.csl.api.ElementKind.CLASS) {
                            ek = ElementKind.CLASS;
                        } else {
                            ek = ElementKind.INTERFACE;
                        }

                        typelist.add(new TypeHolder(indexedClass.getFqn(), ek));
                    }

                    for (TypeHolder type : typelist) {
                        addToProposalUsingFilter(addedTypes, type, onlyInterfaces);
                    }
                }
            }
        }

        List<String> localDefaultImports = new ArrayList<String>();

        // Are there any manually imported types?

        if (moduleNode != null) {

            // this gets the list of full-qualified names of imports.
            List<ImportNode> imports = moduleNode.getImports();

            if (imports != null) {
                for (ImportNode importNode : imports) {
                    ElementKind ek;
                    if (importNode.getType().isInterface()) {
                        ek = ElementKind.INTERFACE;
                    } else {
                        ek = ElementKind.CLASS;
                    }

                    addToProposalUsingFilter(addedTypes, new TypeHolder(importNode.getClassName(), ek), onlyInterfaces);
                }
            }

            // this returns a list of String's of wildcard-like included types.
            List<ImportNode> importNodes = moduleNode.getStarImports();

            for (ImportNode wildcardImport : importNodes) {
                localDefaultImports.add(wildcardImport.getPackageName());
            }
        }


        // Now we compute the type-proposals for the default imports.
        // First, create a list of default JDK packages. These are reused,
        // so they are defined elsewhere.

        localDefaultImports.addAll(GroovyUtils.DEFAULT_IMPORT_PACKAGES);

        // adding types from default import, optionally filtered by
        // prefix

        for (String singlePackage : localDefaultImports) {
            List<TypeHolder> typeList = getTypeHoldersForPackage(javaSource, singlePackage, currentPackage);

            LOG.log(Level.FINEST, "Number of types found:  {0}", typeList.size());

            for (TypeHolder element : typeList) {
                addToProposalUsingFilter(addedTypes, element, onlyInterfaces);
            }
        }

        // Adding single classes
        for (String className : GroovyUtils.DEFAULT_IMPORT_CLASSES) {
            addToProposalUsingFilter(addedTypes, new TypeHolder(className, ElementKind.CLASS), onlyInterfaces);
        }

        // Adding declared classes
        for (ClassNode declaredClass : RequestHelper.getDeclaredClasses(request)) {
            addToProposalUsingFilter(addedTypes, new TypeHolder(declaredClass.getName(), ElementKind.CLASS), onlyInterfaces);
        }

        return true;
    }

    private ModuleNode retrieveModuleNode() {
        AstPath path = request.path;
        if (path != null) {
            for (Iterator<ASTNode> it = path.iterator(); it.hasNext();) {
                ASTNode current = it.next();
                if (current instanceof ModuleNode) {
                    LOG.log(Level.FINEST, "Found ModuleNode");
                    return (ModuleNode) current;
                }
            }
        }
        return null;
    }

    private String getCurrentPackageName(ModuleNode moduleNode) {
        if (moduleNode != null) {
            return moduleNode.getPackageName();
        } else {
            ClassNode node = RequestHelper.getSurroundingClassNode(request);
            if (node != null) {
                return node.getPackageName();
            }
        }
        return "";
    }

    private JavaSource getJavaSourceFromRequest() {
        ClasspathInfo pathInfo = getClasspathInfoFromRequest(request);
        assert pathInfo != null;

        JavaSource javaSource = JavaSource.create(pathInfo);
        if (javaSource == null) {
            LOG.log(Level.FINEST, "Problem retrieving JavaSource from ClassPathInfo, exiting.");
            return null;
        }
        return javaSource;
    }

    /**
     * Adds the type given in fqn with its simple name to the proposals, filtered by
     * the prefix and the package name.
     * 
     * @param alreadyPresent already presented proposals
     * @param type type we want to add into proposals
     * @param onlyInterfaces true, if we are dealing with only interfaces completion
     */
    private void addToProposalUsingFilter(Set<TypeHolder> alreadyPresent, TypeHolder type, boolean onlyInterfaces) {
        if ((onlyInterfaces && (type.getKind() != ElementKind.INTERFACE)) || alreadyPresent.contains(type)) {
            return;
        }

        String typeName = NbUtilities.stripPackage(type.getName());

        // If we are in situation: "String s = new String|" we don't want to show
        // String type as a option - we want to show String constructors + types
        // prefixed with String (e.g. StringBuffer)
        if (constructorCompletion && typeName.toUpperCase().equals(request.prefix.toUpperCase())) {
            return;
        }

        // We are dealing with prefix for some class type
        if (isPrefixed(request, typeName)) {
            alreadyPresent.add(type);
            proposals.add(new CompletionItem.TypeItem(typeName, anchor, type.getKind()));
        }

        // We are dealing with CamelCase completion for some class type
        if (CamelCaseUtil.compareCamelCase(typeName, request.prefix)) {
            CompletionItem.TypeItem camelCaseProposal = new CompletionItem.TypeItem(typeName, anchor, ElementKind.CLASS);
            
            if (!proposals.contains(camelCaseProposal)) {
                proposals.add(camelCaseProposal);
            }
        }
    }

    @NonNull
    private List<TypeHolder> getTypeHoldersForPackage(final JavaSource javaSource, final String pkg, final String currentPackage) {
        LOG.log(Level.FINEST, "getElementListForPackageAsString(), Package :  {0}", pkg);

        final List<TypeHolder> result = new ArrayList<TypeHolder>();

        if (javaSource != null) {

            try {
                javaSource.runUserActionTask(new Task<CompilationController>() {
                    @Override
                    public void run(CompilationController info) {
                        Elements elements = info.getElements();

                        if (elements != null && pkg != null) {
                            LOG.log(Level.FINEST, "TypeSearcherHelper.run(), elements retrieved");
                            PackageElement packageElement = elements.getPackageElement(pkg);

                            if (packageElement == null) {
                                LOG.log(Level.FINEST, "packageElement is null");
                            } else {
                                List<? extends Element> typelist = packageElement.getEnclosedElements();
                                boolean samePackage = pkg.equals(currentPackage);

                                for (Element element : typelist) {
                                    Set<Modifier> modifiers = element.getModifiers();
                                    if (modifiers.contains(Modifier.PUBLIC)
                                        || samePackage && (modifiers.contains(Modifier.PROTECTED)
                                        || (!modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.PRIVATE)))) {
                                        
                                        result.add(new TypeHolder(element.toString(), element.getKind()));
                                    }
                                }
                            }
                        }
                    }
                }, true);
            } catch (IOException ex) {
                LOG.log(Level.FINEST, "IOException : {0}", ex.getMessage());
            }
        }
        return result;
    }

    private static class TypeHolder {

        private final String name;
        private final ElementKind kind;

        
        public TypeHolder(String name, ElementKind kind) {
            this.name = name;
            this.kind = kind;
        }

        public ElementKind getKind() {
            return kind;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TypeHolder other = (TypeHolder) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            if (this.kind != other.kind) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 59 * hash + (this.kind != null ? this.kind.hashCode() : 0);
            return hash;
        }
    }
}
