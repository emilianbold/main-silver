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
package org.netbeans.modules.javascript2.editor;

import java.util.*;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.model.*;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;

/**
 *
 * @author Petr Pisl
 */
public class JsStructureScanner implements StructureScanner {

    //private static final String LAST_CORRECT_FOLDING_PROPERTY = "LAST_CORRECT_FOLDING_PROPERY";
    
    private static final String FOLD_CODE_BLOCKS = "codeblocks"; //NOI18N
    
    private static final String FONT_GRAY_COLOR = "<font color=\"#999999\">"; //NOI18N
    private static final String CLOSE_FONT = "</font>";                   //NOI18N
    
    @Override
    public List<? extends StructureItem> scan(ParserResult info) {
        final List<StructureItem> items = new ArrayList<StructureItem>();        
        JsParserResult result = (JsParserResult) info;
        final Model model = result.getModel();
        JsObject globalObject = model.getGlobalObject();
        
        getEmbededItems(result, globalObject, items);
        return items;
    }
    
    private List<StructureItem> getEmbededItems(JsParserResult result, JsObject jsObject, List<StructureItem> collectedItems) {
        Collection<? extends JsObject> properties = jsObject.getProperties().values();
        for (JsObject child : properties) {
            List<StructureItem> children = new ArrayList<StructureItem>();
            if (child.getJSKind() != JsElement.Kind.FUNCTION && child.getJSKind() != JsElement.Kind.METHOD) {
                // don't count children for functions and methods
                children = getEmbededItems(result, child, children);
            }
            if ((child.hasExactName() || child.isAnonymous()) && child.getJSKind().isFunction()) {
                JsFunction function = (JsFunction)child;
                if (function.isAnonymous()) {
                    collectedItems.addAll(children);
                } else {
                    collectedItems.add(new JsFunctionStructureItem(function, children, result));
                }
            } else if ((child.getJSKind() == JsElement.Kind.OBJECT || child.getJSKind() == JsElement.Kind.ANONYMOUS_OBJECT) && child.isDeclared()) {
                collectedItems.add(new JsObjectStructureItem(child, children, result));
            } else if (child.getJSKind() == JsElement.Kind.PROPERTY) {
                if(child.getModifiers().contains(Modifier.PUBLIC)
                        || !(jsObject.getParent() instanceof JsFunction))
                collectedItems.add(new JsSimpleStructureItem(child, "prop-", result)); //NOI18N
            } else if (child.getJSKind() == JsElement.Kind.VARIABLE && child.isDeclared()
                    /*&& (jsObject.getJSKind() == JsElement.Kind.FILE || jsObject.getJSKind() == JsElement.Kind.CONSTRUCTOR)*/) {
                collectedItems.add(new JsSimpleStructureItem(child, "var-", result)); //NOI18N
            }
         }
        return collectedItems;
    }

//    private JsFunctionStructureItem createItem(FunctionScope scope, List<StructureItem> children) {
//        return new JsFunctionStructureItem(scope, children);
//    }
//    
//    private JsObjectStructureItem createItem(ObjectScope scope, List<StructureItem> children) {
//        return new JsObjectStructureItem(scope, children);
//    }
     
    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        final Map<String, List<OffsetRange>> folds = new HashMap<String, List<OffsetRange>>();
         
        JsParserResult result = (JsParserResult) info;
        final Model model = result.getModel();
//        DeclarationScope fileScope = model.getGlobalObject();
//        
//        
//            
//            List<Scope> scopes = getEmbededScopes(fileScope, null);
//            for (Scope scope : scopes) {
//                OffsetRange offsetRange = scope.getBlockRange();
//                if (offsetRange == null) continue;
//                    
//                getRanges(folds, FOLD_CODE_BLOCKS).add(offsetRange);
//                
//            }
       return folds;
    }
    
    private List<OffsetRange> getRanges(Map<String, List<OffsetRange>> folds, String kind) {
        List<OffsetRange> ranges = folds.get(kind);
        if (ranges == null) {
            ranges = new ArrayList<OffsetRange>();
            folds.put(kind, ranges);
        }
        return ranges;
    }
    
    private List<DeclarationScope>  getEmbededScopes(DeclarationScope scope, List<DeclarationScope> collectedScopes) {
        if (collectedScopes == null) {
            collectedScopes = new ArrayList<DeclarationScope>();
        }
//        List<? extends ModelElement> elements = scope.getElements();
//        for (ModelElement element : elements) {
//            if (element instanceof Scope) {
//                collectedScopes.add((Scope) element);
//                getEmbededScopes((Scope) element, collectedScopes);
//            }
//        }
        return collectedScopes;
    }

    @Override
    public Configuration getConfiguration() {
        // TODO return a configuration to alow filter items. 
        return null;
    }

    private boolean hasDeclaredProperty(JsObject jsObject) {
        boolean result =  false;
        if (jsObject.isDeclared()) {
            result = true;
        } else {
            Iterator <? extends JsObject> it = jsObject.getProperties().values().iterator();
            while (!result && it.hasNext()) {
                result = hasDeclaredProperty(it.next());
            }
        }
        return result;
    }
    
    
    private abstract class JsStructureItem implements StructureItem {

        private JsObject modelElement;
        
        final private List<? extends StructureItem> children;
        final private String sortPrefix;
        final private JsParserResult parserResult;
        
        public JsStructureItem(JsObject elementHandle, List<? extends StructureItem> children, String sortPrefix, JsParserResult parserResult) {
            this.modelElement = elementHandle;
            this.sortPrefix = sortPrefix;
            this.parserResult = parserResult;
            if (children != null) {
                this.children = children;
            } else {
                this.children = Collections.emptyList();
            }
        }
        
        @Override
        public boolean equals(Object obj) {
            boolean thesame = false;
            if (obj instanceof JsStructureItem) {
                JsStructureItem item = (JsStructureItem) obj;
                if (item.getName() != null && this.getName() != null) {
                    thesame = item.modelElement.getName().equals(modelElement.getName()) 
                            && item.modelElement.getOffsetRange(null) == modelElement.getOffsetRange(null);
                }
            }
            return thesame;
        }
        
        @Override
        public int hashCode() {
            int hashCode = 11;
            if (getName() != null) {
                hashCode = 31 * getName().hashCode() + hashCode;
            }
            hashCode = (int) (31 * getPosition() + hashCode);
            return hashCode;
        }
        
        @Override
        public String getName() {
            return modelElement.getName();
        }

        @Override
        public String getSortText() {
            return sortPrefix + modelElement.getName();
        }

        @Override
        public ElementHandle getElementHandle() {
            return modelElement;
        }

        @Override
        public ElementKind getKind() {
              return modelElement.getKind();
        }

        @Override
        public Set<Modifier> getModifiers() {
            return modelElement.getModifiers();
        }

        @Override
        public boolean isLeaf() {
            return children.isEmpty();
        }

        @Override
        public List<? extends StructureItem> getNestedItems() {
            return children;
        }

        @Override
        public long getPosition() {
            return modelElement.getOffset();
        }

        @Override
        public long getEndPosition() {
            return modelElement.getOffsetRange(null).getEnd();
        }

        @Override
        public ImageIcon getCustomIcon() {
            return null;
        }
     
        public JsObject getModelElement() {
            return modelElement;
        }
        
        protected void appendTypeInfo(HtmlFormatter formatter, Collection<? extends Type> types) {
            if (!types.isEmpty() && !types.contains(Type.UNRESOLVED)) {
                formatter.appendHtml(FONT_GRAY_COLOR);
                formatter.appendText(" : ");
                boolean addDelimiter = false;
                for (Type type : types) {
                    if (addDelimiter) {
                        formatter.appendText("|");
                    } else {
                        addDelimiter = true;
                    }
                    formatter.appendHtml(type.getType());
                }
                formatter.appendHtml(CLOSE_FONT);
            }
        }
        
    }
    
    private class JsFunctionStructureItem extends JsStructureItem {

        public JsFunctionStructureItem(JsFunction elementHandle, List<? extends StructureItem> children, JsParserResult parserResult) {
            super(elementHandle, children, "fn", parserResult); //NOI18N
        }

        public JsFunction getFunctionScope() {
            return (JsFunction) getModelElement();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
                formatter.reset();
                appendFunctionDescription(getFunctionScope(), formatter);
                return formatter.getText();
        }
        
        protected void appendFunctionDescription(JsFunction function, HtmlFormatter formatter) {
            formatter.reset();
            if (function == null) {
                return;
            }
            formatter.appendText(getFunctionScope().getDeclarationName().getName());
            formatter.appendText("(");   //NOI18N
            formatter.parameters(true);
            boolean addComma = false;
            for(JsObject jsObject : function.getParameters()) {
                if (addComma) {
                    formatter.appendText(", "); //NOI8N
                } else {
                    addComma = true;
                }
                formatter.appendText(jsObject.getName());
            }
            formatter.parameters(false);
            formatter.appendText(")");   //NOI18N
            
            appendTypeInfo(formatter, function.getReturnTypes());
        }

        @Override
        public String getName() {
            return getFunctionScope().getDeclarationName().getName();
        }
    }

    private class JsObjectStructureItem extends JsStructureItem {

        public JsObjectStructureItem(JsObject elementHandle, List<? extends StructureItem> children, JsParserResult parserResult) {
            super(elementHandle, children, "ob", parserResult); //NOI18N
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
                formatter.reset();
                appendObjectDescription(getModelElement(), formatter);
                return formatter.getText();
        }
        
        protected void appendObjectDescription(JsObject object, HtmlFormatter formatter) {
            formatter.reset();
            if (object == null) {
                return;
            }
            formatter.appendText(object.getName());
        }

    }
    
    private class JsSimpleStructureItem extends JsStructureItem {
        private final JsObject object;
        
        public JsSimpleStructureItem(JsObject elementHandle, String sortPrefix, JsParserResult parserResult) {
            super(elementHandle, null, sortPrefix, parserResult);
            this.object = elementHandle;
        }

        
        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            formatter.appendText(getElementHandle().getName());
            Collection<? extends Type> types = object.getAssignmentForOffset(object.getDeclarationName().getOffsetRange().getEnd());
            appendTypeInfo(formatter, types);
            return formatter.getText();
        }
        
    }
    
}
