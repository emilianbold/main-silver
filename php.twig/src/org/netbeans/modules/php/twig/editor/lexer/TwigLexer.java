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
 * Contributor(s): Sebastian Hörl
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.twig.editor.lexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.lexer.Token;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

public class TwigLexer implements Lexer<TwigTokenId> {

    protected TwigLexerState state;
    protected final TokenFactory<TwigTokenId> tokenFactory;
    protected final LexerInput input;

    private TwigLexer(LexerRestartInfo<TwigTokenId> info) {
        tokenFactory = info.tokenFactory();
        input = info.input();
        state = info.state() == null ? new TwigLexerState() : new TwigLexerState((TwigLexerState) info.state());
        initialize();
    }

    public static synchronized TwigLexer create(LexerRestartInfo<TwigTokenId> info) {
        return new TwigLexer(info);
    }

    @Override
    public Token<TwigTokenId> nextToken() {

        TwigTokenId tokenId = findNextToken();
        return tokenId == null ? null : tokenFactory.createToken(tokenId);

    }

    @Override
    public Object state() {
        return new TwigLexerState(state);
    }

    @Override
    public void release() {
    }
    static protected String BLOCK_START = "{%"; //NOI18N
    static protected String COMMENT_START = "{#"; //NOI18N
    static protected String VAR_START = "{{"; //NOI18N
    static protected String BLOCK_END = "{%"; //NOI18N
    static protected String COMMENT_END = "{#"; //NOI18N
    static protected String VAR_END = "{{"; //NOI18N
    static protected String PUNCTUATION = "|()[]{}?:.,"; //NOI18N
    static protected Pattern REGEX_ALPHANUM_END = Pattern.compile("[A-Za-z0-9]$"); //NOI18N
    static protected Pattern REGEX_WHITESPACE_END = Pattern.compile("[\\s]+$"); //NOI18N
    protected Pattern REGEX_OPERATOR = null;
    int OPERATOR_LENGTH = 0;
    final static List<String> OPERATORS = new ArrayList<String>();
    static {
        OPERATORS.add("as"); //NOI18N
        OPERATORS.add("="); //NOI18N
        OPERATORS.add("not"); //NOI18N
        OPERATORS.add("+"); //NOI18N
        OPERATORS.add("-"); //NOI18N
        OPERATORS.add("or"); //NOI18N
        OPERATORS.add("b-or"); //NOI18N
        OPERATORS.add("b-xor"); //NOI18N
        OPERATORS.add("and"); //NOI18N
        OPERATORS.add("b-and"); //NOI18N
        OPERATORS.add("=="); //NOI18N
        OPERATORS.add("!="); //NOI18N
        OPERATORS.add(">"); //NOI18N
        OPERATORS.add("<"); //NOI18N
        OPERATORS.add(">="); //NOI18N
        OPERATORS.add("<="); //NOI18N
        OPERATORS.add("in"); //NOI18N
        OPERATORS.add("~"); //NOI18N
        OPERATORS.add("*"); //NOI18N
        OPERATORS.add("/"); //NOI18N
        OPERATORS.add("//"); //NOI18N
        OPERATORS.add("%"); //NOI18N
        OPERATORS.add("is"); //NOI18N
        OPERATORS.add(".."); //NOI18N
        OPERATORS.add("**"); //NOI18N
    }

    protected class SortOperators implements Comparator<String> {

        @Override
        public int compare(String a, String b) {
            return a.length() - b.length();
        }
    }

    protected String implode(List<String> list, String delimeter) {
        String s = "";
        boolean first = true;
        for (String item : list) {
            if (!first) {
                s += delimeter;
            }
            s += item;
            first = false;
        }
        return s;
    }

    private void initialize() {
        Collections.sort(OPERATORS, new SortOperators());
        Collections.reverse(OPERATORS);
        ArrayList<String> regex = new ArrayList<String>();
        for (String operator : OPERATORS) {
            if (REGEX_ALPHANUM_END.matcher(operator).find()) {
                regex.add(Pattern.quote(operator) + "[ ()]"); //NOI18N
                if (operator.length() + 1 > OPERATOR_LENGTH) {
                    OPERATOR_LENGTH = operator.length() + 1;
                }
            } else {
                regex.add(Pattern.quote(operator));
                if (operator.length() > OPERATOR_LENGTH) {
                    OPERATOR_LENGTH = operator.length();
                }
            }
        }
        REGEX_OPERATOR = Pattern.compile("^" + implode(regex, "|^")); //NOI18N
    }

    public TwigTokenId findNextToken() {

        int c = input.read();
        int d = c;
        if (c == LexerInput.EOF) {
            return null;
        }

        Matcher matcher;

        while (c != LexerInput.EOF) {

            CharSequence text = input.readText();
            d = c;

            switch (state.main) {

                case INIT:
                    if (CharSequenceUtilities.startsWith(text, COMMENT_START)) {
                        state.main = TwigLexerState.Main.COMMENT;
                    } else if (CharSequenceUtilities.startsWith(text, BLOCK_START)) {
                        state.main = TwigLexerState.Main.BLOCK;
                        state.sub = TwigLexerState.Sub.INIT;
                        return TwigTokenId.T_TWIG_BLOCK_START;
                    } else if (CharSequenceUtilities.startsWith(text, VAR_START)) {
                        state.main = TwigLexerState.Main.VAR;
                        state.sub = TwigLexerState.Sub.INIT;
                        return TwigTokenId.T_TWIG_VAR_START;
                    }
                    break;

                case VAR:
                case BLOCK:

                    /* Whitespaces */

                    if (Character.isWhitespace(c)) {

                        do {
                            c = input.read();
                        } while (c != LexerInput.EOF && Character.isWhitespace(c));

                        if (c != LexerInput.EOF) {
                            input.backup(1);
                        }
                        return TwigTokenId.T_TWIG_WHITESPACE;

                    }

                    /* End markups */

                    if (c == '%' || c == '}') {

                        d = input.read();

                        if (d == LexerInput.EOF) {
                            return TwigTokenId.T_TWIG_OTHER;
                        }

                        int e = input.read();

                        if (d == '}' && e == LexerInput.EOF) {

                            if (state.main == TwigLexerState.Main.BLOCK && c == '%') {
                                return TwigTokenId.T_TWIG_BLOCK_END;
                            }

                            if (state.main == TwigLexerState.Main.VAR && c == '}') {
                                return TwigTokenId.T_TWIG_VAR_END;
                            }

                        }

                        input.backup(2);

                    }

                    /* Operators */

                    if (!(state.main == TwigLexerState.Main.BLOCK && state.sub == TwigLexerState.Sub.INIT)) {

                        d = c;

                        int characters = 0;
                        while (c != LexerInput.EOF && input.readLength() < OPERATOR_LENGTH) {
                            c = input.read();
                            characters++;
                        }

                        matcher = REGEX_OPERATOR.matcher(input.readText());
                        if (matcher.find()) {

                            String operator = matcher.group();
                            matcher = REGEX_WHITESPACE_END.matcher(operator);

                            if (matcher.find()) {

                                input.backup(characters - matcher.start());
                                return TwigTokenId.T_TWIG_OPERATOR;

                            } else {

                                input.backup(characters - operator.length() + 1);
                                return TwigTokenId.T_TWIG_OPERATOR;

                            }

                        }

                        input.backup(characters);
                        c = d;

                    } else if (c == '-') { /* Trim operator */
                        return TwigTokenId.T_TWIG_OPERATOR;
                    }

                    /* Names */

                    if (Character.isLetter(c) || c == '_') {

                        do {
                            c = input.read();
                        } while (c != LexerInput.EOF && (Character.isLetter(c) || Character.isDigit(c) || c == '_'));

                        if (c != LexerInput.EOF) {
                            input.backup(1);
                        }

                        if (state.main == TwigLexerState.Main.BLOCK && state.sub == TwigLexerState.Sub.INIT) {
                            state.sub = TwigLexerState.Sub.NONE;
                            return TwigTokenId.T_TWIG_FUNCTION;
                        } else {
                            return TwigTokenId.T_TWIG_NAME;
                        }

                    }

                    /* Numbers */

                    if (Character.isDigit(c)) {

                        boolean dotFound = false;

                        do {
                            if (c == '.') {
                                dotFound = true;
                            }
                            c = input.read();
                        } while (c != LexerInput.EOF && (Character.isDigit(c) || (!dotFound && c == '.')));

                        if (c != LexerInput.EOF) {
                            input.backup(1);
                        }
                        return TwigTokenId.T_TWIG_NUMBER;

                    }

                    /* Double quoted strings */

                    if (c == '"') {

                        boolean escaped = false;

                        do {
                            if (c == '\\' && !escaped) {
                                escaped = true;
                            } else {
                                escaped = false;
                            }
                            c = input.read();
                        } while (c != LexerInput.EOF && (escaped || c != '"'));

                        return TwigTokenId.T_TWIG_STRING;

                    }

                    /* Single quoted strings */

                    if (c == '\'') {

                        boolean escaped = false;

                        do {
                            if (c == '\\' && !escaped) {
                                escaped = true;
                            } else {
                                escaped = false;
                            }
                            c = input.read();
                        } while (c != LexerInput.EOF && (escaped || c != '\''));

                        return TwigTokenId.T_TWIG_STRING;

                    }

                    /* PUNCTUATION */

                    if (PUNCTUATION.indexOf(c) >= 0) {
                        return TwigTokenId.T_TWIG_PUNCTUATION;
                    }

                    return TwigTokenId.T_TWIG_OTHER;

            }

            c = input.read();

        }

        if (state.main == TwigLexerState.Main.COMMENT) {
            return TwigTokenId.T_TWIG_COMMENT;
        }
        return TwigTokenId.T_TWIG_OTHER;

    }
}