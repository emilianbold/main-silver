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
package org.netbeans.modules.php.editor.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.php.editor.api.AliasedName;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.ConstantElement;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.UseElement;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.*;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Petr Pisl, Radek Matous
 */
public class PhpStructureScanner implements StructureScanner {

    private static ImageIcon INTERFACE_ICON = null;
    private static ImageIcon TRAIT_ICON = null;

    private static final String FOLD_CODE_BLOCKS = "codeblocks"; //NOI18N

    private static final String FOLD_CLASS = "tags"; //NOI18N

    private static final String FOLD_PHPDOC = "comments"; //NOI18N

    private static final String FOLD_COMMENT = "initial-comment"; //NOI18N

    private static final String FONT_GRAY_COLOR = "<font color=\"#999999\">"; //NOI18N

    private static final String CLOSE_FONT = "</font>";                   //NOI18N

    private static final String LAST_CORRECT_FOLDING_PROPERTY = "LAST_CORRECT_FOLDING_PROPERY";

    public List<? extends StructureItem> scan(final ParserResult info) {
        final List<StructureItem> items = new ArrayList<StructureItem>();
        PHPParseResult result = (PHPParseResult) info;
        final Model model = result.getModel();
        FileScope fileScope = model.getFileScope();
        Collection<? extends NamespaceScope> declaredNamespaces = fileScope.getDeclaredNamespaces();
        for (NamespaceScope nameScope : declaredNamespaces) {
            List<StructureItem> namespaceChildren = nameScope.isDefaultNamespace() ? items : new ArrayList<StructureItem>();
            if (!nameScope.isDefaultNamespace()) {
                items.add(new PHPNamespaceStructureItem(nameScope, namespaceChildren));
            }
            Collection<? extends UseElement> declaredUses = nameScope.getDeclaredUses();
            for (UseElement useElement : declaredUses) {
                namespaceChildren.add(new PHPUseStructureItem(useElement));
            }

            Collection<? extends FunctionScope> declaredFunctions = nameScope.getDeclaredFunctions();
            for (FunctionScope fnc : declaredFunctions) {
                if (fnc.isAnonymous()) continue;
                List<StructureItem> variables = new ArrayList<StructureItem>();
                namespaceChildren.add(new PHPFunctionStructureItem(fnc, variables));
                //TODO: #170281 - API for declaring item in Navigator to collapsed/expanded as default
                /*Collection<? extends VariableName> declaredVariables = fnc.getDeclaredVariables();
                for (VariableName variableName : declaredVariables) {
                    variables.add(new PHPSimpleStructureItem(variableName, "var"));
                }*/
            }
            Collection<? extends ConstantElement> declaredConstants = nameScope.getDeclaredConstants();
            for (ConstantElement constant : declaredConstants) {
                namespaceChildren.add(new PHPConstantStructureItem(constant, "const"));
            }
            Collection<? extends TypeScope> declaredTypes = nameScope.getDeclaredTypes();
            for (TypeScope type : declaredTypes) {
                List<StructureItem> children = new ArrayList<StructureItem>();
                if (type instanceof ClassScope) {
                    namespaceChildren.add(new PHPClassStructureItem((ClassScope) type, children));
                } else if (type instanceof InterfaceScope) {
                    namespaceChildren.add(new PHPInterfaceStructureItem((InterfaceScope) type, children));
                } else if (type instanceof TraitScope) {
                    namespaceChildren.add(new PHPTraitStructureItem((TraitScope) type, children));
                }
                Collection<? extends MethodScope> declaredMethods = type.getDeclaredMethods();
                for (MethodScope method : declaredMethods) {
                    // The method name doesn't have to be always defined during parsing.
                    // For example when user writes in  a php doc @method and parsing is
                    // started when there is no name yet.
                    if (method.getName() != null && !method.getName().isEmpty()) {
                        List<StructureItem> variables = new ArrayList<StructureItem>();
                        if (method.isConstructor()) {
                            children.add(new PHPConstructorStructureItem(method, variables));
                        } else {
                            children.add(new PHPMethodStructureItem(method, variables));
                        }
                        //TODO: #170281 - API for declaring item in Navigator to collapsed/expanded as default
                        /*Collection<? extends VariableName> declaredVariables = method.getDeclaredVariables();
                        for (VariableName variableName : declaredVariables) {
                            if (!variableName.representsThis()) {
                                variables.add(new PHPSimpleStructureItem(variableName, "var"));
                            }
                        }*/
                    }
                }
                Collection<? extends ClassConstantElement> declaredClsConstants = type.getDeclaredConstants();
                for (ClassConstantElement classConstant : declaredClsConstants) {
                    children.add(new PHPConstantStructureItem(classConstant, "con"));//NOI18N
                }
                if (type instanceof ClassScope) {
                    ClassScope cls = (ClassScope) type;
                    Collection<? extends FieldElement> declaredFields = cls.getDeclaredFields();
                    for (FieldElement field : declaredFields) {
                        children.add(new PHPFieldStructureItem(field));//NOI18N
                    }
                }
                if (type instanceof TraitScope) {
                    TraitScope trait = (TraitScope) type;
                    Collection<? extends FieldElement> declaredFields = trait.getDeclaredFields();
                    for (FieldElement field : declaredFields) {
                        children.add(new PHPFieldStructureItem(field));//NOI18N
                    }
                }
            }
        }



        return items;
    }

    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        final Map<String, List<OffsetRange>> folds = new HashMap<String, List<OffsetRange>>();
        Program program = Utils.getRoot(info);
        if (program != null) {
            if (program.getStatements().size() == 1) {
                // check whether the ast is broken.
                if (program.getStatements().get(0) instanceof ASTError) {
                    final Document document = info.getSnapshot().getSource().getDocument(false);
                    @SuppressWarnings("unchecked")
                    Map<String, List<OffsetRange>> lastCorrect = document != null ?
                        ((Map<String, List<OffsetRange>>) document.getProperty(LAST_CORRECT_FOLDING_PROPERTY)) : null;
                    if (lastCorrect != null){
                        return lastCorrect;
                    }
                    else {
                        return Collections.emptyMap();
                    }
                }
            }
            List<Comment> comments = program.getComments();
            if (comments != null) {
                for (Comment comment : comments) {
                    if (comment.getCommentType() == Comment.Type.TYPE_PHPDOC) {
                        getRanges(folds, FOLD_PHPDOC).add(createOffsetRange(comment));
                    } else {
                        if (comment.getCommentType() == Comment.Type.TYPE_MULTILINE) {
                            getRanges(folds, FOLD_COMMENT).add(createOffsetRange(comment));
                        }
                    }
                }
            }
            PHPParseResult result = (PHPParseResult) info;
            final Model model = result.getModel();
            FileScope fileScope = model.getFileScope();
            List<Scope> scopes = getEmbededScopes(fileScope, null);
            for (Scope scope : scopes) {
                OffsetRange offsetRange = scope.getBlockRange();
                if (offsetRange == null) continue;
                if (scope instanceof TypeScope) {
                    getRanges(folds, FOLD_CLASS).add(offsetRange);
                } else {
                    //NamespaceScope excluded until getBlockRange() return proper vaalues
                    if (scope instanceof FunctionScope || scope instanceof MethodScope /*|| scope instanceof NamespaceScope*/) {
                        getRanges(folds, FOLD_CODE_BLOCKS).add(offsetRange);
                    }
                }
            }
            program.accept(new FoldingVisitor(folds));
            Source source = info.getSnapshot().getSource();
            assert source != null : "source was null";
            Document doc = source.getDocument(false);

            if (doc != null){
                doc.putProperty(LAST_CORRECT_FOLDING_PROPERTY, folds);
            }
            return folds;
        }
        return Collections.emptyMap();
    }

    private OffsetRange createOffsetRange(ASTNode node) {
        return new OffsetRange(node.getStartOffset(), node.getEndOffset());
    }

    private List<OffsetRange> getRanges(Map<String, List<OffsetRange>> folds, String kind) {
        List<OffsetRange> ranges = folds.get(kind);
        if (ranges == null) {
            ranges = new ArrayList<OffsetRange>();
            folds.put(kind, ranges);
        }
        return ranges;
    }

    public Configuration getConfiguration() {
        return new Configuration(true, true);
    }

    private List<Scope>  getEmbededScopes(Scope scope, List<Scope> collectedScopes) {
        if (collectedScopes == null) {
            collectedScopes = new ArrayList<Scope>();
        }
        List<? extends ModelElement> elements = scope.getElements();
        for (ModelElement element : elements) {
            if (element instanceof Scope) {
                collectedScopes.add((Scope) element);
                getEmbededScopes((Scope) element, collectedScopes);
            }
        }
        return collectedScopes;
    }

    private class FoldingVisitor extends DefaultVisitor {
        private final Map<String, List<OffsetRange>> folds;

        public FoldingVisitor(final Map<String, List<OffsetRange>> folds) {
            this.folds = folds;
        }

        @Override
        public void visit(IfStatement node) {
            super.visit(node);
            int endOffset = node.getEndOffset();
            Statement falseStatement = node.getFalseStatement();
            if (falseStatement != null) {
                endOffset = falseStatement.getEndOffset();
                addFold(falseStatement);
            }
            addFold(new OffsetRange(node.getStartOffset(), endOffset));
        }

        @Override
        public void visit(ForEachStatement node) {
            super.visit(node);
            addFold(node);
        }

        @Override
        public void visit(ForStatement node) {
            super.visit(node);
            addFold(node);
        }

        @Override
        public void visit(WhileStatement node) {
            super.visit(node);
            addFold(node);
        }

        @Override
        public void visit(DoStatement node) {
            super.visit(node);
            addFold(node);
        }

        @Override
        public void visit(SwitchStatement node) {
            super.visit(node);
            addFold(node);
        }

        @Override
        public void visit(SwitchCase node) {
            super.visit(node);
            addFold(node);
        }

        @Override
        public void visit(TryStatement node) {
            super.visit(node);
            addFold(node);
        }

        @Override
        public void visit(CatchClause node) {
            super.visit(node);
            addFold(node);
        }

        private void addFold(final ASTNode node) {
            addFold(createOffsetRange(node));
        }

        private void addFold(final OffsetRange offsetRange) {
            getRanges(folds, FOLD_CODE_BLOCKS).add(offsetRange);
        }

    }

    private abstract class PHPStructureItem implements StructureItem {

        final private ModelElement modelElement;
        final private List<? extends StructureItem> children;
        final private String sortPrefix;

        public PHPStructureItem(ModelElement elementHandle, List<? extends StructureItem> children, String sortPrefix) {
            this.modelElement = elementHandle;
            this.sortPrefix = sortPrefix;
            if (children != null) {
                this.children = children;
            } else {
                this.children = Collections.emptyList();
            }
        }

        @Override
        public boolean equals(Object obj) {
            boolean thesame = false;
            if (obj instanceof PHPStructureItem) {
                PHPStructureItem item = (PHPStructureItem) obj;
                if (item.getName() != null && this.getName() != null) {
                    thesame = item.modelElement.getName().equals(modelElement.getName()) && item.modelElement.getOffset() == modelElement.getOffset();
                }
            }
            return thesame;
        }

        @Override
        public int hashCode() {
            //int hashCode = super.hashCode();
            int hashCode = 11;
            if (getName() != null) {
                hashCode = 31 * getName().hashCode() + hashCode;
            }
            hashCode = (int) (31 * getPosition() + hashCode);
            return hashCode;
        }

        public String getName() {
            return modelElement.getName();
        }

        public String getSortText() {
            return sortPrefix + modelElement.getName();
        }

        public ElementHandle getElementHandle() {
            return modelElement.getPHPElement();
        }

        public ModelElement getModelElement() {
            return modelElement;
        }

        public ElementKind getKind() {
            return modelElement.getPHPElement().getKind();
        }

        public Set<Modifier> getModifiers() {
            return modelElement.getPHPElement().getModifiers();
        }

        public boolean isLeaf() {
            return (children.size() == 0);
        }

        public List<? extends StructureItem> getNestedItems() {
            return children;
        }

        public long getPosition() {
            return modelElement.getOffset();
        }

        public long getEndPosition() {
            if (modelElement instanceof Scope) {
                final OffsetRange blockRange = ((Scope) modelElement).getBlockRange();
                if (blockRange != null) {
                    return blockRange.getEnd();
                }
            }
            return modelElement.getNameRange().getEnd();
        }

        public ImageIcon getCustomIcon() {
            return null;
        }

        protected void appendInterfeas(Collection<? extends String> interfaes, HtmlFormatter formatter) {
            boolean first = true;
            for (String identifier : interfaes) {
                if (identifier != null) {
                    if (!first) {
                        formatter.appendText(", ");  //NOI18N

                    } else {
                        first = false;
                    }
                    formatter.appendText(identifier);
                }

            }
        }

        protected void appendUsedTraits(Collection<QualifiedName> usedTraits, HtmlFormatter formatter) {
            boolean first = true;
            for (QualifiedName qualifiedName : usedTraits) {
                if (!first) {
                    formatter.appendText(", ");  //NOI18N
                } else {
                    first = false;
                }
                formatter.appendText(qualifiedName.toString());
            }
        }

        protected void appendFunctionDescription(FunctionScope function, HtmlFormatter formatter) {
            formatter.reset();
            if (function == null) {
                return;
            }
            formatter.appendText(function.getName());
            formatter.appendText("(");   //NOI18N

            //NOI18N
            //NOI18N
            List<? extends ParameterElement> parameters = function.getParameters();
            if (parameters != null && parameters.size() > 0) {
                boolean first = true;
                for (ParameterElement formalParameter : parameters) {
                    String name = formalParameter.getName();
                    Set<TypeResolver> types = formalParameter.getTypes();
                    if (name != null) {
                        if (!first) {
                            formatter.appendText(", "); //NOI18N

                        }

                        if (!types.isEmpty()) {
                            formatter.appendHtml(FONT_GRAY_COLOR);
                            StringBuilder typeSb = new StringBuilder();
                            for (TypeResolver typeResolver : types) {
                                if (typeResolver.isResolved()) {
                                    QualifiedName typeName = typeResolver.getTypeName(false);
                                    if (typeName != null) {
                                        if (typeSb.length() > 0) {
                                            typeSb.append("|");//NOI18N
                                        }
                                        typeSb.append(typeName.toString());
                                    }
                                }
                            }
                            if (typeSb.length() > 0) {
                                formatter.appendText(typeSb.toString());
                            }
                            formatter.appendText(" ");   //NOI18N

                            formatter.appendHtml(CLOSE_FONT);
                        }
                        formatter.appendText(name);
                        first = false;
                    }
                }
            }
            formatter.appendText(")");   //NOI18N
            Collection<? extends String> returnTypes = function.getReturnTypeNames();
            if (!returnTypes.isEmpty()) {
                formatter.appendHtml(FONT_GRAY_COLOR + ":"); //NOI18N
                StringBuilder sb = null;
                for (String type : returnTypes) {
                    if (sb == null) {
                        sb = new StringBuilder();
                    } else {
                        sb.append(", ");//NOI18N
                    }
                    sb.append(type);
                }
                formatter.appendText(sb.toString());
                formatter.appendHtml(CLOSE_FONT);
            }
        }
    }

    private class PHPFieldStructureItem extends PHPSimpleStructureItem {
        public PHPFieldStructureItem(ModelElement elementHandle) {
            super(elementHandle, "field");//NOI18N
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            ElementHandle elementHandle = getElementHandle();
            formatter.appendText(elementHandle.getName());
            if (elementHandle instanceof FieldElement) {
                final FieldElement fieldElement = (FieldElement) elementHandle;
                Collection<? extends String> types = fieldElement.getDefaultTypeNames();
                StringBuilder sb = null;
                if (!types.isEmpty()) {
                    formatter.appendHtml(FONT_GRAY_COLOR + ":"); //NOI18N
                    for (String type : types) {
                        if (sb == null) {
                            sb = new StringBuilder();
                        } else {
                            sb.append(", ");//NOI18N
                        }
                        sb.append(type);

                    }
                    formatter.appendText(sb.toString());
                    formatter.appendHtml(CLOSE_FONT);
                }
            }
            return formatter.getText();
        }

    }
    private class PHPSimpleStructureItem extends PHPStructureItem {

        private String simpleText;

        public PHPSimpleStructureItem(ModelElement elementHandle, String prefix) {
            super(elementHandle, null, prefix);
            this.simpleText = elementHandle.getName();
        }

        public String getHtml(HtmlFormatter formatter) {
            formatter.appendText(simpleText);
            return formatter.getText();
        }

    }

    private class PHPNamespaceStructureItem extends PHPStructureItem {
        public PHPNamespaceStructureItem(NamespaceScope elementHandle, List<? extends StructureItem> children) {
            super(elementHandle, children, "namespace"); //NOI18N
        }

        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            formatter.appendText(getName());
            return formatter.getText();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.MODULE;
        }
    }

    private class PHPUseStructureItem extends PHPStructureItem {
        public PHPUseStructureItem(UseElement elementHandle) {
            super(elementHandle, null, "aaaa_use"); //NOI18N
        }

        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            formatter.appendText(getName());
            UseElement useElement = (UseElement) getElementHandle();
            final AliasedName aliasedName = useElement.getAliasedName();
            if (aliasedName != null) {
                formatter.appendText(" as ");//NOI18N
                formatter.appendText(aliasedName.getAliasName());
            }
            return formatter.getText();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.RULE;
        }
    }

    private class PHPClassStructureItem extends PHPStructureItem {

        public PHPClassStructureItem(ClassScope elementHandle, List<? extends StructureItem> children) {
            super(elementHandle, children, "cl"); //NOI18N
        }

        public ClassScope getClassScope() {
            return (ClassScope) getModelElement();
        }

        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            formatter.appendText(getName());
            String superCalssName = ModelUtils.getFirst(getClassScope().getSuperClassNames());
            if (superCalssName != null) {
                formatter.appendHtml(FONT_GRAY_COLOR + "::"); //NOI18N
                formatter.appendText(superCalssName);
                formatter.appendHtml(CLOSE_FONT);
            }
            Collection<? extends String> interfaes = getClassScope().getSuperInterfaceNames();
            if (interfaes != null && interfaes.size() > 0) {
                formatter.appendHtml(FONT_GRAY_COLOR + ":"); //NOI18N
                appendInterfeas(interfaes, formatter);
                formatter.appendHtml(CLOSE_FONT);
            }
            Collection<QualifiedName> usedTraits = getClassScope().getUsedTraits();
            if (usedTraits != null && usedTraits.size() > 0) {
                formatter.appendHtml(FONT_GRAY_COLOR + "#"); //NOI18N
                appendUsedTraits(usedTraits, formatter);
                formatter.appendHtml(CLOSE_FONT);
            }
            return formatter.getText();
        }

    }

    private class PHPConstantStructureItem extends PHPStructureItem {
        public PHPConstantStructureItem(ConstantElement elementHandle, String prefix) {
            super(elementHandle, null, prefix);
        }

        public ConstantElement getConstant() {
            return (ConstantElement) getModelElement();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            formatter.appendText(getName());
            final ConstantElement constant = getConstant();
            String value = constant.getValue();
            if (value != null) {
                formatter.appendText(" ");//NOI18N
                formatter.appendHtml(FONT_GRAY_COLOR); //NOI18N
                formatter.appendText(value);
                formatter.appendHtml(CLOSE_FONT);
            }
            return formatter.getText();
        }

    }

    private class PHPFunctionStructureItem extends PHPStructureItem {

        public PHPFunctionStructureItem(FunctionScope elementHandle, List<? extends StructureItem> children) {
            super(elementHandle, children, "fn"); //NOI18N
        }

        public FunctionScope getFunctionScope() {
            return (FunctionScope) getModelElement();
        }

        public String getHtml(HtmlFormatter formatter) {
                formatter.reset();
                appendFunctionDescription(getFunctionScope(), formatter);
                return formatter.getText();
        }

    }

    private class PHPMethodStructureItem extends PHPStructureItem {

        public PHPMethodStructureItem(MethodScope elementHandle, List<? extends StructureItem> children) {
            super(elementHandle, children, "fn"); //NOI18N
        }

        public MethodScope getMethodScope() {
            return (MethodScope) getModelElement();
        }

        public String getHtml(HtmlFormatter formatter) {
                formatter.reset();
                appendFunctionDescription(getMethodScope(), formatter);
                return formatter.getText();
        }


    }

    private class PHPInterfaceStructureItem extends PHPStructureItem {

        private static final String PHP_INTERFACE_ICON = "org/netbeans/modules/php/editor/resources/interface.png"; //NOI18N

        public PHPInterfaceStructureItem(InterfaceScope elementHandle, List<? extends StructureItem> children) {
            super(elementHandle, children, "cl"); //NOI18N
        }

        @Override
        public ImageIcon getCustomIcon() {
            if (INTERFACE_ICON == null) {
                INTERFACE_ICON = new ImageIcon(ImageUtilities.loadImage(PHP_INTERFACE_ICON));
            }
            return INTERFACE_ICON;
        }

        public InterfaceScope getInterfaceScope() {
            return (InterfaceScope) getModelElement();
        }

        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            formatter.appendText(getElementHandle().getName());
            Collection<? extends String> interfaes = getInterfaceScope().getSuperInterfaceNames();
            if (interfaes != null && interfaes.size() > 0) {
                formatter.appendHtml(FONT_GRAY_COLOR + "::"); //NOI18N
                appendInterfeas(interfaes, formatter);
                formatter.appendHtml(CLOSE_FONT);
            }
            return formatter.getText();
        }
    }

    private class PHPTraitStructureItem extends PHPStructureItem {
        private static final String PHP_TRAIT_ICON = "org/netbeans/modules/php/editor/resources/trait.png"; //NOI18N

        public PHPTraitStructureItem(ModelElement elementHandle, List<? extends StructureItem> children) {
            super(elementHandle, children, "cl");
        }

        @Override
        public ImageIcon getCustomIcon() {
            if (TRAIT_ICON == null) {
                TRAIT_ICON = new ImageIcon(ImageUtilities.loadImage(PHP_TRAIT_ICON));
            }
            return TRAIT_ICON;
        }

        public TraitScope getTraitScope() {
            return (TraitScope) getModelElement();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            formatter.appendText(getElementHandle().getName());
            Collection<QualifiedName> usedTraits = getTraitScope().getUsedTraits();
            if (usedTraits != null && usedTraits.size() > 0) {
                formatter.appendHtml(FONT_GRAY_COLOR + "#"); //NOI18N
                appendUsedTraits(usedTraits, formatter);
                formatter.appendHtml(CLOSE_FONT);
            }
            return formatter.getText();
        }

    }

    private class PHPConstructorStructureItem extends PHPStructureItem {

        public PHPConstructorStructureItem(MethodScope elementHandle, List<? extends StructureItem> children) {
            super(elementHandle, children, "con");
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CONSTRUCTOR;
        }

        public MethodScope getMethodScope() {
            return (MethodScope) getModelElement();
        }

        public String getHtml(HtmlFormatter formatter) {
                formatter.reset();
                appendFunctionDescription(getMethodScope(), formatter);
                return formatter.getText();
        }

    }
}
