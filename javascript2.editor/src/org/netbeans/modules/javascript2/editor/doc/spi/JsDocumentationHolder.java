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
package org.netbeans.modules.javascript2.editor.doc.spi;

import com.oracle.nashorn.ir.Node;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.editor.doc.JsDocumentationPrinter;
import org.netbeans.modules.javascript2.editor.doc.api.JsModifier;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.model.Type;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * Holds processed JavaScript comments and serves all data to the JS model.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public abstract class JsDocumentationHolder {

    private final Snapshot snapshot;

    public JsDocumentationHolder(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    public abstract Map<Integer, ? extends JsComment> getCommentBlocks();

    /**
     * Gets possible return types get for the node.
     * @param node of the javaScript code
     * @return list of potential return types, never {@code null}
     */
    public List<Type> getReturnType(Node node) {
        JsComment comment = getCommentForOffset(node.getStart(), getCommentBlocks());
        if (comment != null && comment.getReturnType() != null) {
            return comment.getReturnType().getParamTypes();
        }
        return Collections.<Type>emptyList();
    }

    /**
     * Gets parameters of the method.
     * @param node of the javaScript code
     * @return list of parameters, never {@code null}
     */
    public List<DocParameter> getParameters(Node node) {
        JsComment comment = getCommentForOffset(node.getStart(), getCommentBlocks());
        if (comment != null) {
            return comment.getParameters();
        }
        return Collections.<DocParameter>emptyList();
    }

    /**
     * Gets documentation for given Node.
     * @param node of the javaScript code
     * @return documentation text if any {@code null} otherwise
     */
    public String getDocumentation(Node node) {
        JsComment comment = getCommentForOffset(node.getStart(), getCommentBlocks());
        if (comment != null) {
            return JsDocumentationPrinter.printDocumentation(comment);
        }
        return null;
    }

    /**
     * Says whether is code at given code depricated or not.
     * @param node examined node
     * @return {@code true} if the comment says "it's deprecated", {@code false} otherwise
     */
    public boolean isDeprecated(Node node) {
        JsComment comment = getCommentForOffset(node.getStart(), getCommentBlocks());
        if (comment != null) {
            return comment.isDeprecated();
        }
        return false;
    }

    /**
     * Gets the set of modifiers attached to given node.
     * @param node examinded node
     * @return {@code Set} of modifiers, never {@code null}
     */
    public Set<JsModifier> getModifiers(Node node) {
        JsComment comment = getCommentForOffset(node.getStart(), getCommentBlocks());
        if (comment != null) {
            return comment.getModifiers();
        }
        return Collections.<JsModifier>emptySet();
    }

    /**
     * Answers whether given token is whitespace or not.
     * @param token examined token
     * @return {@code true} if the token is whitespace, {@code false} otherwise
     */
    public boolean isWhitespaceToken(Token<? extends JsTokenId> token) {
        return token.id() == JsTokenId.EOL || token.id() == JsTokenId.WHITESPACE
                || token.id() == JsTokenId.BLOCK_COMMENT || token.id() == JsTokenId.DOC_COMMENT
                || token.id() == JsTokenId.LINE_COMMENT;
    }

    /**
     * Gets the closest documentation comment block to the given offset if any.
     * @param offset where to start searching for the doc comment block
     * @return documentation block
     */
    public JsComment getCommentForOffset(int offset, Map<Integer, ? extends JsComment> comments) {
        int endOffset = getEndOffsetOfAssociatedComment(offset);
        if (endOffset > 0) {
            return comments.get(endOffset);
        }
        return null;
    }

    @SuppressWarnings("empty-statement")
    private int getEndOffsetOfAssociatedComment(int offset) {
        TokenHierarchy<?> tokenHierarchy = snapshot.getTokenHierarchy();
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(tokenHierarchy, offset);
        if (ts != null) {
            ts.move(offset);

            // get to first EOL
            while (ts.movePrevious()
                    && ts.token().id() != JsTokenId.EOL
                    && ts.token().id() != JsTokenId.OPERATOR_SEMICOLON) {
                // do nothing - just search for interesting tokens
            }

            // search for DOC_COMMENT
            while (ts.movePrevious()) {
                if (ts.token().id() == JsTokenId.DOC_COMMENT) {
                    return ts.token().offset(tokenHierarchy) + ts.token().length();
                } else if (isWhitespaceToken(ts.token())) {
                    continue;
                } else {
                    return -1;
                }
            }
        }

        return -1;
    }
}
