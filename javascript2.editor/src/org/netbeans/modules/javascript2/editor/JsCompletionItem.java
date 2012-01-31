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
package org.netbeans.modules.javascript2.editor;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.CompletionContextFinder.CompletionContext;
import org.netbeans.modules.javascript2.editor.index.IndexedElement;
import org.netbeans.modules.javascript2.editor.model.Identifier;
import org.netbeans.modules.javascript2.editor.model.JsFunction;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;

/**
 *
 * @author Petr Pisl
 */
public class JsCompletionItem implements CompletionProposal {
    
    private final CompletionRequest request;
    private final ElementHandle element;
    
    JsCompletionItem(ElementHandle element, CompletionRequest request) {
        this.element = element;
        this.request = request;
    }
    
    @Override
    public int getAnchorOffset() {
        return request.anchor;
    }

    @Override
    public ElementHandle getElement() {
        return element;
    }

    @Override
    public String getName() {
        return element.getName();
    }

    @Override
    public String getInsertPrefix() {
        return element.getName();
    }

    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        formatter.appendText(getName());
        return formatter.getText();
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        formatter.reset();
        formatter.appendText(getFileNameURL());
        return formatter.getText();
    }

    @Override
    public ElementKind getKind() {
        return element.getKind();
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public Set<Modifier> getModifiers() {
        Set<Modifier> emptyModifiers = Collections.emptySet();
        ElementHandle handle = getElement();
        return (handle != null) ? handle.getModifiers() : emptyModifiers;
    }

    @Override
    public boolean isSmart() {
        // TODO implemented properly
        return false;
    }

    @Override
    public int getSortPrioOverride() {
        return 0;
    }

    @Override
    public String getCustomInsertTemplate() {
        return null;
    }
    
    public String getFileNameURL() {
        ElementHandle elem = getElement(); 
        return elem.getFileObject().getName();
     }
    
    public static class CompletionRequest {
        public  int anchor;
        public  JsParserResult result;
        public  ParserResult info;
        public  String prefix;
        public  String currentlyEditedFileURL;
        public CompletionContext context;
    }
    
    public static class JsFunctionCompletionItem extends JsCompletionItem {
        private JsFunction function;
        
        JsFunctionCompletionItem(ElementHandle element, CompletionRequest request) {
            super(element, request);
            this.function = (JsFunction)element;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.emphasis(true);
            formatter.appendText(getName());
            formatter.emphasis(false);
            formatter.appendText("(");
            appendParamsStr(formatter);
            formatter.appendText(")");
            return formatter.getText();
        }
        
        private void appendParamsStr(HtmlFormatter formatter){
            Collection<? extends Identifier> allParameters = function.getParameters();
            for (Iterator<? extends Identifier> it = allParameters.iterator(); it.hasNext();) {
                Identifier ident = it.next();
                formatter.parameters(true);
                formatter.appendText(ident.getName());
                formatter.parameters(false);
                if (it.hasNext()) {
                    formatter.appendText(", ");  //NOI18N
                }    
            }
        }
    }

    public static class Factory {
        
        public static JsCompletionItem create(JsObject object, CompletionRequest request) {
            JsCompletionItem result;
            switch (object.getJSKind()) {
                case FUNCTION:
                case METHOD:
                    result = new JsFunctionCompletionItem(object, request);
                    break;
                default:
                    result = new JsCompletionItem(object, request);
            }
            return result;
        }
        
        public static JsCompletionItem create(IndexedElement element, CompletionRequest request) {
            JsCompletionItem result = new JsCompletionItem(element, request);
            return result;
        }
    }
}
