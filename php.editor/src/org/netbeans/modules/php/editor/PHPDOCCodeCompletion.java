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
package org.netbeans.modules.php.editor;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.php.editor.PHPCompletionItem.CompletionRequest;
import org.netbeans.modules.php.editor.index.PHPDOCTagElement;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPDocCommentTokenId;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration;
import org.netbeans.modules.php.project.api.PhpAnnotations;
import org.netbeans.modules.php.spi.annotations.PhpAnnotationTag;
import org.netbeans.modules.php.spi.annotations.PhpAnnotationsProvider;

/**
 *
 * @author tomslot
 */
public class PHPDOCCodeCompletion {

    private static final String TAG_PREFIX = "@"; //NOI18N

    static boolean isTypeCtx(PHPCompletionItem.CompletionRequest request){
        //TODO: get rid of the document
        TokenHierarchy<?> th = request.info.getSnapshot().getTokenHierarchy();
        TokenSequence<PHPTokenId> phpTS = (th != null) ? LexUtilities.getPHPTokenSequence(th, request.anchor) : null;
        if (phpTS != null) {
            phpTS.move(request.anchor);
            TokenSequence<PHPDocCommentTokenId> tokenSequence = phpTS.moveNext() ? phpTS.embedded(PHPDocCommentTokenId.language()) : null;
            if (tokenSequence == null) {
                return false;
            }
            tokenSequence.move(request.anchor);
            if (tokenSequence.movePrevious()) {
                int offset = tokenSequence.offset() + tokenSequence.token().length();
                if (tokenSequence.moveNext()) {
                    CharSequence text = tokenSequence.token().text();
                    String txt = text.subSequence(0, request.anchor - offset).toString();
                    if (!txt.trim().isEmpty() && txt.charAt(txt.length() - 1) == '|') { //NOI18N
                        // expect that user wants to complete mixed type
                        txt = txt.trim();
                        for (int i = 0; i < txt.length(); i++) {
                            if (Character.isWhitespace(txt.charAt(i))) {
                                return false;
                            }
                        }
                        return true;
                    }
                    for (int i = 0; i < txt.length(); i++) {
                        if (!Character.isWhitespace(txt.charAt(i))) {
                            return false;
                        }
                    }
                }
                // text between PHPDoc directive and begining of the prefix, should only contain white spaces
                return true;
            }
        }
        return false;
    }

    public static void complete(final PHPCompletionResult completionResult, CompletionRequest request) {
        String prefix = null;
        if (request.prefix.startsWith(TAG_PREFIX)){
            prefix = request.prefix.substring(TAG_PREFIX.length());
        } else {
            return;
        }
        List<PhpAnnotationsProvider> providers = PhpAnnotations.getDefault().getProviders(request.info.getSnapshot().getSource().getFileObject());
        ASTNode nodeAfterOffset = Utils.getNodeAfterOffset(request.result, request.anchor);
        for (PhpAnnotationsProvider annotationProvider : providers) {
            List<PhpAnnotationTag> annotations = null;
            if (nodeAfterOffset instanceof TypeDeclaration) {
                annotations = annotationProvider.getTypeAnnotations();
            } else if (nodeAfterOffset instanceof MethodDeclaration) {
                annotations = annotationProvider.getMethodAnnotations();
            } else if (nodeAfterOffset instanceof FunctionDeclaration) {
                annotations = annotationProvider.getFunctionAnnotations();
            } else if (nodeAfterOffset instanceof FieldsDeclaration) {
                annotations = annotationProvider.getFieldAnnotations();
            } else {
                annotations = annotationProvider.getAnnotations();
            }
            for (PhpAnnotationTag tag : annotations) {
                if (tag.getName().startsWith(prefix)) {
                    completionResult.add(new PHPDOCCodeCompletionItem(request, tag, annotationProvider.getName()));
                }
            }
        }
    }

    public static class PHPDOCCodeCompletionItem implements CompletionProposal {
        private final PhpAnnotationTag tag;
        private final PHPCompletionItem.CompletionRequest request;
        private final PHPDOCTagElement elem;
        private final String providerName;

        public PHPDOCCodeCompletionItem(CompletionRequest request, PhpAnnotationTag tag, String providerName) {
            this.tag = tag;
            this.request = request;
            this.providerName= providerName;
            elem = new PHPDOCTagElement(tag.getName(), tag.getDocumentation());
        }

        public int getAnchorOffset() {
            return request.anchor;
        }

        public ElementHandle getElement() {
            return elem;
        }

        public String getName() {
            return TAG_PREFIX + tag.getName(); //NOI18N
        }

        public String getInsertPrefix() {
            return getName();
        }

        public String getSortText() {
            return getName();
        }

        public int getSortPrioOverride() {
            return 0;
        }

        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);
            tag.formatParameters(formatter);
            return formatter.getText();
        }

        public String getRhsHtml(HtmlFormatter formatter) {
            return providerName;
        }

        public ElementKind getKind() {
            return elem.getKind();
        }

        public ImageIcon getIcon() {
            return null;
        }

        public Set<Modifier> getModifiers() {
            return Collections.<Modifier>emptySet();
        }

        public boolean isSmart() {
            return false;
        }

        public String getCustomInsertTemplate() {
            return tag.getInsertTemplate();
        }
    }
}
