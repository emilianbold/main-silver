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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.common.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author marekfukala
 */
public class LexerUtils {

    /**
     * Note: The input text must contain only \n as line terminators.
     * This is compatible with the netbeans document which never contains \r\n
     * line separators.
     *
     * @param text
     * @param offset
     * @return line offset, starting with zero.
     */
    public static int getLineOffset(CharSequence text, int offset) throws BadLocationException {
        if(text == null) {
            throw new NullPointerException();
        }

        if(offset < 0 || offset > text.length()) {
            throw new BadLocationException("The given offset is out of bounds <0, " + text.length() + ">" , offset); //NOI18N
        }
        int line = 0;
        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if(c == '\r') {
                throw new IllegalArgumentException("The input text cannot contain carriage return char \\r"); //NOI18N
            }
            if(i == offset) {
                return line;
            }
            if(c == '\n') {
                line++;
            }
        }

        //for position just at the length of the text
        return line;
    }
    
    /**
     * Note: The input text must contain only \n as line terminators.
     * This is compatible with the netbeans document which never contains \r\n
     * line separators.
     *
     * @param text
     * @param line line number
     * @return offset of the beginning of the line
     */
    public static int getLineBeginningOffset(CharSequence text, int line) throws BadLocationException {
        if(text == null) {
            throw new NullPointerException();
        }

        if(line < 0) {
            throw new IllegalArgumentException("Line number must be >= 0!");
        }
        int linecount = 0; 
        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if(c == '\r') {
                throw new IllegalArgumentException("The input text cannot contain carriage return char \\r"); //NOI18N
            }
            if(linecount == line) {
                return i;
            }
            if(c == '\n') {
                linecount++;
            }
        }

        //for position just at the length of the text
        return linecount;
    }

    public static Token followsToken(TokenSequence ts, TokenId searchedId, boolean backwards, boolean repositionBack, TokenId... skipIds) {
        return followsToken(ts, Collections.singletonList(searchedId), backwards, repositionBack, skipIds);
    }

    public static Token followsToken(TokenSequence ts, Collection<? extends TokenId> searchedIds, boolean backwards, boolean repositionBack, TokenId... skipIds) {
        Collection<TokenId> skip = Arrays.asList(skipIds);
        int index = ts.index();
        while(backwards ? ts.movePrevious() : ts.moveNext()) {
            Token token = ts.token();
            TokenId id = token.id();
            if(searchedIds.contains(id)) {
                if(repositionBack) {
                    int idx = ts.moveIndex(index);
                    boolean moved = ts.moveNext();

                    assert idx == 0 && moved;

                }
                return token;
            }
            if(!skip.contains(id)) {
                break;
            }
        }
        
        return null;
    }

      /** returns top most joined html token seuence for the document at the specified offset. */
    public static TokenSequence getJoinedTokenSequence(Document doc, int offset, Language language) {
        return getTokenSequence(doc, offset, language, true);
    }
    
    public static TokenSequence getTokenSequence(Document doc, int offset, Language language, boolean joined) {
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence ts = th.tokenSequence();
        if(ts == null) {
            return null;
        }
        ts.move(offset);

        while(ts.moveNext() || ts.movePrevious()) {
            if(ts.language() == language) {
                return ts;
            }

            ts = ts.embeddedJoined();

            if(ts == null) {
                break;
            }

            //position the embedded ts so we can search deeper
            ts.move(offset);
        }

        return null;

    }

    /** @param optimized - first sequence is lowercase, one call to Character.toLowerCase() only*/
    public static boolean equals(CharSequence text1, CharSequence text2, boolean ignoreCase, boolean optimized) {
        if (text1.length() != text2.length()) {
            return false;
        } else {
            //compare content
            for (int i = 0; i < text1.length(); i++) {
                char ch1 = ignoreCase && !optimized ? Character.toLowerCase(text1.charAt(i)) : text1.charAt(i);
                char ch2 = ignoreCase ? Character.toLowerCase(text2.charAt(i)) : text2.charAt(i);
                if (ch1 != ch2) {
                    return false;
                }
            }
            return true;
        }
    }
    
    /** @param optimized - first sequence is lowercase, one call to Character.toLowerCase() only*/
    public static boolean startsWith(CharSequence text1, CharSequence prefix, boolean ignoreCase, boolean optimized) {
        if (text1.length() < prefix.length()) {
            return false;
        } else {
            return equals(text1.subSequence(0, prefix.length()), prefix, ignoreCase, optimized);
        }
    }


}
