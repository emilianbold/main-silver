/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.twig.editor.braces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.twig.editor.lexer.TwigLexerUtils;
import org.netbeans.modules.php.twig.editor.lexer.TwigTokenId;
import org.netbeans.modules.php.twig.editor.lexer.TwigTopTokenId;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class TwigBracesMatcher implements BracesMatcher {
    private static final List<Matcher> MATCHERS = new ArrayList<>();
    static {
        MATCHERS.add(new StartEndBlockMatcher("autoescape")); //NOI18N
        MATCHERS.add(new StartEndBlockMatcher("block")); //NOI18N
        MATCHERS.add(new StartEndBlockMatcher("embed")); //NOI18N
        MATCHERS.add(new StartEndBlockMatcher("filter")); //NOI18N
        MATCHERS.add(new StartEndBlockMatcher("for")); //NOI18N
        MATCHERS.add(new StartEndBlockMatcher("macro")); //NOI18N
        MATCHERS.add(new StartEndBlockMatcher("raw")); //NOI18N
        MATCHERS.add(new StartEndBlockMatcher("sandbox")); //NOI18N
        MATCHERS.add(new StartEndBlockMatcher("set")); //NOI18N
        MATCHERS.add(new StartEndBlockMatcher("spaceless")); //NOI18N
        MATCHERS.add(new IfMatcher());
        MATCHERS.add(new EndIfMatcher());
        MATCHERS.add(new ElseMatcher());
        MATCHERS.add(new ElseIfMatcher());
    }
    private final MatcherContext context;

    private TwigBracesMatcher(MatcherContext context) {
        this.context = context;
    }

    @Override
    public int[] findOrigin() throws InterruptedException, BadLocationException {
        int[] result = null;
        BaseDocument document = (BaseDocument) context.getDocument();
        document.readLock();
        try {
            result = findOriginUnderLock();
        } finally {
            document.readUnlock();
        }
        return result;
    }

    private int[] findOriginUnderLock() {
        int[] result = null;
        TokenSequence<? extends TwigTokenId> ts = TwigLexerUtils.getTwigMarkupTokenSequence(context.getDocument(), context.getSearchOffset());
        if (ts != null) {
            result = findOriginInSequence(ts);
        }
        return result;
    }

    private int[] findOriginInSequence(TokenSequence<? extends TwigTokenId> ts) {
        int[] result = null;
        ts.move(context.getSearchOffset());
        if (ts.moveNext()) {
            Token<? extends TwigTokenId> currentToken = ts.token();
            if (currentToken != null) {
                TwigTokenId currentTokenId = currentToken.id();
                if (currentTokenId == TwigTokenId.T_TWIG_TAG) {
                    result = new int[] {ts.offset(), ts.offset() + currentToken.length()};
                }
            }
        }
        return result;
    }

    @Override
    public int[] findMatches() throws InterruptedException, BadLocationException {
        int[] result = null;
        BaseDocument document = (BaseDocument) context.getDocument();
        document.readLock();
        try {
            result = findMatchesUnderLock();
        } finally {
            document.readUnlock();
        }
        return result;
    }

    private int[] findMatchesUnderLock() {
        int[] result = null;
        TokenSequence<? extends TwigTopTokenId> topTs = TwigLexerUtils.getTwigTokenSequence(context.getDocument(), context.getSearchOffset());
        if (topTs != null) {
            result = findMatchesInTopSequence(topTs);
        }
        return result;
    }

    private int[] findMatchesInTopSequence(TokenSequence<? extends TwigTopTokenId> topTs) {
        assert topTs != null;
        int[] result = null;
        topTs.move(context.getSearchOffset());
        topTs.moveNext();
        TokenSequence<TwigTokenId> ts = topTs.embeddedJoined(TwigTokenId.language());
        if (ts != null) {
            result = findMatchesInEmbeddedSequence(topTs, ts);
        }
        return result;
    }

    private int[] findMatchesInEmbeddedSequence(TokenSequence<? extends TwigTopTokenId> topTs, TokenSequence<TwigTokenId> embeddedTs) {
        int[] result = null;
        embeddedTs.move(context.getSearchOffset());
        if (embeddedTs.moveNext()) {
            Token<? extends TwigTokenId> currentToken = embeddedTs.token();
            if (currentToken != null) {
                result = processMatchers(currentToken, topTs);
            }
        }
        return result;
    }

    private int[] processMatchers(Token<? extends TwigTokenId> currentToken, TokenSequence<? extends TwigTopTokenId> topTs) {
        int[] result = null;
        for (Matcher matcher : MATCHERS) {
            if (matcher.matches(currentToken)) {
                result = matcher.findMatches(currentToken, topTs);
                break;
            }
        }
        return result;
    }

    private interface Matcher {

        boolean matches(Token<? extends TwigTokenId> token);

        int[] findMatches(Token<? extends TwigTokenId> token, TokenSequence<? extends TwigTopTokenId> topTs);

    }

    private abstract static class IfConditionMatcher implements Matcher {
        protected static final String IF = "if"; //NOI18N
        protected static final String ELSE_IF = "elseif"; //NOI18N
        protected static final String ELSE = "else"; //NOI18N
        protected static final String END_IF = "endif"; //NOI18N

        @Override
        public boolean matches(Token<? extends TwigTokenId> token) {
            return matchingToken().matches(token);
        }

        protected abstract TwigLexerUtils.TwigTokenText matchingToken();

        protected void findElseifForwards(List<OffsetRange> offsetRanges, TokenSequence<? extends TwigTopTokenId> topTs) {
            assert offsetRanges != null;
            assert topTs != null;
            OffsetRange elseifRange = OffsetRange.NONE;
            do {
                if (elseifRange != OffsetRange.NONE) {
                    offsetRanges.add(elseifRange);
                    topTs.move(elseifRange.getEnd());
                    topTs.moveNext();
                }
                elseifRange = TwigLexerUtils.findForwardInTwig(topTs, TwigLexerUtils.TwigTokenTextImpl.create(TwigTokenId.T_TWIG_TAG, ELSE_IF));
            } while (elseifRange != OffsetRange.NONE);
        }

        protected void findElseifBackwards(List<OffsetRange> offsetRanges, TokenSequence<? extends TwigTopTokenId> topTs) {
            assert offsetRanges != null;
            assert topTs != null;
            OffsetRange elseifRange = OffsetRange.NONE;
            do {
                if (elseifRange != OffsetRange.NONE) {
                    offsetRanges.add(elseifRange);
                    topTs.move(elseifRange.getStart());
                    topTs.movePrevious();
                }
                elseifRange = TwigLexerUtils.findBackwardInTwig(topTs, TwigLexerUtils.TwigTokenTextImpl.create(TwigTokenId.T_TWIG_TAG, ELSE_IF));
            } while (elseifRange != OffsetRange.NONE);
        }

        protected void findElseForwards(List<OffsetRange> offsetRanges, TokenSequence<? extends TwigTopTokenId> topTs) {
            assert offsetRanges != null;
            assert topTs != null;
            addRange(TwigLexerUtils.findForwardInTwig(topTs, TwigLexerUtils.TwigTokenTextImpl.create(TwigTokenId.T_TWIG_TAG, ELSE)), offsetRanges);
        }

        protected void findEndifForwards(List<OffsetRange> offsetRanges, TokenSequence<? extends TwigTopTokenId> topTs) {
            assert offsetRanges != null;
            assert topTs != null;
            addRange(TwigLexerUtils.findForwardInTwig(topTs, TwigLexerUtils.TwigTokenTextImpl.create(TwigTokenId.T_TWIG_TAG, END_IF)), offsetRanges);
        }

        protected void findIfBackwards(List<OffsetRange> offsetRanges, TokenSequence<? extends TwigTopTokenId> topTs) {
            assert offsetRanges != null;
            assert topTs != null;
            addRange(TwigLexerUtils.findBackwardInTwig(topTs, TwigLexerUtils.TwigTokenTextImpl.create(TwigTokenId.T_TWIG_TAG, IF)), offsetRanges);
        }

        private void addRange(OffsetRange what, List<OffsetRange> where) {
            if (what != null && what != OffsetRange.NONE) {
                where.add(what);
            }
        }

        protected static int[] createMatch(List<OffsetRange> offsetRanges) {
            int resultSize = offsetRanges.size() * 2;
            int[] result = new int[resultSize];
            for (int i = 0, j = 0; i < offsetRanges.size(); i++, j += 2) {
                result[j] = offsetRanges.get(i).getStart();
                result[j + 1] = offsetRanges.get(i).getEnd();
            }
            return result;
        }

    }

    private static final class IfMatcher extends IfConditionMatcher {
        private static final TwigLexerUtils.TwigTokenText START_TOKEN = TwigLexerUtils.TwigTokenTextImpl.create(TwigTokenId.T_TWIG_TAG, IF);

        @Override
        protected TwigLexerUtils.TwigTokenText matchingToken() {
            return START_TOKEN;
        }

        @Override
        public int[] findMatches(Token<? extends TwigTokenId> token, TokenSequence<? extends TwigTopTokenId> topTs) {
            assert token != null;
            assert topTs != null;
            List<OffsetRange> offsetRanges = TwigLexerUtils.findForwardMatching(
                    topTs,
                    START_TOKEN,
                    TwigLexerUtils.TwigTokenTextImpl.create(TwigTokenId.T_TWIG_TAG, END_IF),
                    Arrays.asList(
                            TwigLexerUtils.TwigTokenTextImpl.create(TwigTokenId.T_TWIG_TAG, ELSE_IF),
                            TwigLexerUtils.TwigTokenTextImpl.create(TwigTokenId.T_TWIG_TAG, ELSE))
                    );
            return createMatch(offsetRanges);
        }

    }

    private static final class EndIfMatcher extends IfConditionMatcher {
        private static final TwigLexerUtils.TwigTokenText START_TOKEN = TwigLexerUtils.TwigTokenTextImpl.create(TwigTokenId.T_TWIG_TAG, END_IF);

        @Override
        protected TwigLexerUtils.TwigTokenText matchingToken() {
            return START_TOKEN;
        }

        @Override
        public int[] findMatches(Token<? extends TwigTokenId> token, TokenSequence<? extends TwigTopTokenId> topTs) {
            assert token != null;
            assert topTs != null;
            List<OffsetRange> offsetRanges = TwigLexerUtils.findBackwardMatching(
                    topTs,
                    START_TOKEN,
                    TwigLexerUtils.TwigTokenTextImpl.create(TwigTokenId.T_TWIG_TAG, IF),
                    Arrays.asList(
                            TwigLexerUtils.TwigTokenTextImpl.create(TwigTokenId.T_TWIG_TAG, ELSE_IF),
                            TwigLexerUtils.TwigTokenTextImpl.create(TwigTokenId.T_TWIG_TAG, ELSE))
                    );
            return createMatch(offsetRanges);
        }

    }

    private static final class ElseMatcher extends IfConditionMatcher {
        private static final TwigLexerUtils.TwigTokenText START_TOKEN = TwigLexerUtils.TwigTokenTextImpl.create(TwigTokenId.T_TWIG_TAG, ELSE);

        @Override
        protected TwigLexerUtils.TwigTokenText matchingToken() {
            return START_TOKEN;
        }

        @Override
        public int[] findMatches(Token<? extends TwigTokenId> token, TokenSequence<? extends TwigTopTokenId> topTs) {
            assert token != null;
            assert topTs != null;
            List<OffsetRange> offsetRanges = new ArrayList<>();
            findEndifForwards(offsetRanges, topTs);
            findElseifBackwards(offsetRanges, topTs);
            findIfBackwards(offsetRanges, topTs);
            return createMatch(offsetRanges);
        }

    }

    private static final class ElseIfMatcher extends IfConditionMatcher {
        private static final TwigLexerUtils.TwigTokenText START_TOKEN = TwigLexerUtils.TwigTokenTextImpl.create(TwigTokenId.T_TWIG_TAG, ELSE_IF);

        @Override
        protected TwigLexerUtils.TwigTokenText matchingToken() {
            return START_TOKEN;
        }

        @Override
        public int[] findMatches(Token<? extends TwigTokenId> token, TokenSequence<? extends TwigTopTokenId> topTs) {
            assert token != null;
            assert topTs != null;
            List<OffsetRange> offsetRanges = new ArrayList<>();
            findIfBackwards(offsetRanges, topTs);
            findElseifBackwards(offsetRanges, topTs);
            findElseifForwards(offsetRanges, topTs);
            findElseForwards(offsetRanges, topTs);
            findEndifForwards(offsetRanges, topTs);
            return createMatch(offsetRanges);
        }

    }

    private static final class StartEndBlockMatcher implements Matcher {
        private static final String END = "end"; //NOI18N
        private final String blockName;

        public StartEndBlockMatcher(String blockName) {
            assert blockName != null;
            this.blockName = blockName;
        }

        @Override
        public boolean matches(Token<? extends TwigTokenId> token) {
            assert token != null;
            return token.id() == TwigTokenId.T_TWIG_TAG
                    && (blockName.equals(token.text().toString()) || (END + blockName).equals(token.text().toString()));
        }

        @Override
        public int[] findMatches(Token<? extends TwigTokenId> token, TokenSequence<? extends TwigTopTokenId> topTs) {
            assert token != null;
            assert topTs != null;
            int[] result = null;
            String tagText = token.text().toString();
            if (tagText.equals(blockName)) {
                OffsetRange offsetRange = TwigLexerUtils.findForwardMatching(
                        topTs,
                        TwigLexerUtils.TwigTokenTextImpl.create(TwigTokenId.T_TWIG_TAG, blockName),
                        TwigLexerUtils.TwigTokenTextImpl.create(TwigTokenId.T_TWIG_TAG, END + blockName));
                result = createMatch(offsetRange);
            } else if (tagText.equals(END + blockName)) {
                OffsetRange offsetRange = TwigLexerUtils.findBackwardMatching(
                        topTs,
                        TwigLexerUtils.TwigTokenTextImpl.create(TwigTokenId.T_TWIG_TAG, END + blockName),
                        TwigLexerUtils.TwigTokenTextImpl.create(TwigTokenId.T_TWIG_TAG, blockName));
                result = createMatch(offsetRange);
            }
            return result;
        }

        private static int[] createMatch(OffsetRange offsetRange) {
            assert offsetRange != null;
            return offsetRange == OffsetRange.NONE ? null : new int[] {offsetRange.getStart(), offsetRange.getEnd()};
        }

    }

    public static final class Factory implements BracesMatcherFactory {

        @Override
        public BracesMatcher createMatcher(MatcherContext context) {
            return new TwigBracesMatcher(context);
        }

    }

}
