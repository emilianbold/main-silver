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
package org.netbeans.modules.javascript2.editor.doc.jsdoc;

import org.netbeans.modules.javascript2.editor.doc.jsdoc.JsDocTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocLexerTest extends NbTestCase {

    public JsDocLexerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    @SuppressWarnings("unchecked")
    public void testCommonDocComment01() {
        String text = "/** comment */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, " comment ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testCommonDocComment02() {
        String text = "/**comment*/";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, "comment");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testCommonDocComment03() {
        String text = "/** \n\n */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, " \n\n ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testCommonDocComment04() {
        String text = "/**   @  */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, "   ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, "@");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testCommonDocComment05() {
        String text = "/** @p */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.KEYWORD, "@p");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testCommonDocComment06() {
        String text = "/** \n * @param */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, " \n ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.KEYWORD, "@param");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testCommonDocComment07() {
        String text = "/** \n *@param */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, " \n ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.KEYWORD, "@param");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testCommonDocComment08() {
        String text = "/** \n *@ */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, " \n ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, "@");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_END, "*/");
    }

    // Not necessary since it's called just on documentation comments like /** */
//    @SuppressWarnings("unchecked")
//    public void testComment01() {
//        String text = "/***/";
//        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
//        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
//        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_CODE, "/***/");
//    }
//
//    @SuppressWarnings("unchecked")
//    public void testComment02() {
//        String text = "/**/";
//        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
//        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
//        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_CODE, "/**/");
//    }
//
//    @SuppressWarnings("unchecked")
//    public void testComment03() {
//        String text = "/* muj comment */";
//        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
//        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
//        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_CODE, "/* muj comment */");
//    }

    @SuppressWarnings("unchecked")
    public void testSharedTagComment01() {
        String text = "/**#@+\n* anyTags\n*/";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_SHARED_BEGIN, "/**#@+");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, " anyTags\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testSharedTagComment02() {
        String text = "/**#@+@class cokoliv*/";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_SHARED_BEGIN, "/**#@+");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.KEYWORD, "@class");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, " cokoliv");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testSharedTagComment03() {
        String text = "/**#@+ \n @private\n @final\n*/";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_SHARED_BEGIN, "/**#@+");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, " \n ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.KEYWORD, "@private");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, "\n ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.KEYWORD, "@final");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testSharedTagComment04() {
        String text = "/**#@-*/";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_SHARED_END, "/**#@-*/");
    }

    @SuppressWarnings("unchecked")
    public void testSharedTagComment05() {
        String text = "/**#@- */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, "@");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, "- ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testNoCodeComment01() {
        String text = "/**#nocode+*/";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_NOCODE_BEGIN, "/**#nocode+*/");
    }

    @SuppressWarnings("unchecked")
    public void testNoCodeComment02() {
        String text = "/**#nocode-*/";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_NOCODE_END, "/**#nocode-*/");
    }

    @SuppressWarnings("unchecked")
    public void testUnfinishedComment01() {
        String text = "/* \n var Carrot = {";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.UNKNOWN, "/* \n var Carrot = {");
    }

    @SuppressWarnings("unchecked")
    public void testUnfinishedComment02() {
        String text = "/** getColor: function () {}, ";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, " getColor: function () {}, ");
    }

    @SuppressWarnings("unchecked")
    public void testHtmlComment01() {
        String text = "/** <b>text</b> */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.HTML, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.HTML, "b");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.HTML, ">");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, "text");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.HTML, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.HTML, "/b");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.HTML, ">");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testHtmlComment02() {
        String text = "/** <a href=\"mailto:marfous@netbeans.org\">href</a> */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.HTML, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.HTML, "a href=\"mailto:marfous@netbeans.org\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.HTML, ">");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, "href");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.HTML, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.HTML, "/a");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.HTML, ">");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testHtmlComment03() {
        String text = "/** <a */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocTokenId.language());
        TokenSequence<?extends JsDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_BLOCK, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.HTML, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.HTML, "a ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocTokenId.COMMENT_END, "*/");
    }
}
