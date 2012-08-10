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

import java.util.Arrays;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.lexer.LexUtilities;

/**
 *
 * @author Petr Pisl
 */
public class CompletionContextFinder {
    
    public static enum CompletionContext {
        NONE,       // There shouldn't be any code completion
        EXPRESSION, // usually, we will offer everything what we know in the context
        OBJECT_PROPERTY, // object property that are visible outside the object
        OBJECT_MEMBERS, // usually after this.
        DOCUMENTATION, // inside documentation blocks
        GLOBAL
    } 
   
    private static final List<JsTokenId> WHITESPACES_TOKENS = Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL);
    
    private static final List<JsTokenId> CHANGE_CONTEXT_TOKENS = Arrays.asList(
            JsTokenId.OPERATOR_SEMICOLON, JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.BRACKET_RIGHT_CURLY);
    private static final List<Object[]> OBJECT_PROPERTY_TOKENCHAINS = Arrays.asList(
        new Object[]{JsTokenId.OPERATOR_DOT},
        new Object[]{JsTokenId.OPERATOR_DOT, JsTokenId.IDENTIFIER}
    );
    
    private static final List<Object[]> OBJECT_THIS_TOKENCHAINS = Arrays.asList(
        new Object[]{JsTokenId.KEYWORD_THIS, JsTokenId.OPERATOR_DOT},
        new Object[]{JsTokenId.KEYWORD_THIS, JsTokenId.OPERATOR_DOT, JsTokenId.IDENTIFIER}
    );
    
    @NonNull
    static CompletionContext findCompletionContext(ParserResult info, int caretOffset){
         TokenHierarchy<?> th = info.getSnapshot().getTokenHierarchy();
        if (th == null) {
            return CompletionContext.NONE;
        }
        TokenSequence<JsTokenId> ts = th.tokenSequence(JsTokenId.javascriptLanguage());
        if (ts == null) {
            return CompletionContext.NONE;
        }
        ts.move(caretOffset);
        
        if (!ts.moveNext() && !ts.movePrevious()){
            return CompletionContext.NONE;
        }
        
        Token<? extends JsTokenId> token = ts.token();
        JsTokenId tokenId =token.id();
        int tokenOffset = ts.offset();
        
        if (acceptTokenChains(ts, OBJECT_THIS_TOKENCHAINS, true)) {
            return CompletionContext.OBJECT_MEMBERS;
        }
        if (acceptTokenChains(ts, OBJECT_PROPERTY_TOKENCHAINS, tokenId != JsTokenId.OPERATOR_DOT)) {
            return CompletionContext.OBJECT_PROPERTY;
        }
        
        if (tokenId == JsTokenId.EOL && ts.movePrevious()) {
            token = ts.token();
            tokenId = token.id();
        }
        if (tokenId == JsTokenId.IDENTIFIER || WHITESPACES_TOKENS.contains(tokenId)) {
            ts.movePrevious();
            token = LexUtilities.findPrevious(ts, WHITESPACES_TOKENS);
        }
        if (CHANGE_CONTEXT_TOKENS.contains(token.id())) {
            return CompletionContext.GLOBAL;
        }
        if (tokenId == JsTokenId.DOC_COMMENT) {
            return CompletionContext.DOCUMENTATION;
        }
        return CompletionContext.EXPRESSION;
    }
    
    private static boolean acceptTokenChains(TokenSequence tokenSequence, List<Object[]> tokenIdChains, boolean movePrevious) {
        for (Object[] tokenIDChain : tokenIdChains){
            if (acceptTokenChain(tokenSequence, tokenIDChain, movePrevious)){
                return true;
            }
        }

        return false;
    }
    
    private static boolean acceptTokenChain(TokenSequence tokenSequence, Object[] tokenIdChain, boolean movePrevious) {
        int orgTokenSequencePos = tokenSequence.offset();
        boolean accept = true;
        boolean moreTokens = movePrevious ? tokenSequence.movePrevious() : true;

        for (int i = tokenIdChain.length - 1; i >= 0; i --){
            Object tokenID = tokenIdChain[i];

            if (!moreTokens){
                accept = false;
                break;
            }

           if (tokenID instanceof JsTokenId) {
                if (tokenSequence.token().id() == tokenID){
                    moreTokens = tokenSequence.movePrevious();
                } else {
                    // NO MATCH
                    accept = false;
                    break;
                }
            } else {
                assert false : "Unsupported token type: " + tokenID.getClass().getName();
            }
        }

        tokenSequence.move(orgTokenSequencePos);
        tokenSequence.moveNext();
       return accept;
    }
}
