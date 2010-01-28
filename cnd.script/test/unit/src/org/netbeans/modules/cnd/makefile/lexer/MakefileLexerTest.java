/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.makefile.lexer;

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import static org.netbeans.lib.lexer.test.LexerTestUtilities.assertNextTokenEquals;

/**
 * @author Alexey Vladykin
 */
public class MakefileLexerTest extends NbTestCase {

    public MakefileLexerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        LexerTestUtilities.setTesting(true);
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testSimple() {
        String text = "# Environment\n" +
                      "MKDIR=mkdir\n" +
                      "BUILDDIR=build/${CONF}\n" +
                      "OS := $(shell uname | grep -i Darwin)\n\n" +
                      "build:\n" +
                      "\t$(COMPILE.cc) source.cpp -o source.o\n\n" +
                      ".PHONY: build\n" +
                      "include foo.mk\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, new MakefileLanguageHierarchy().language());
        TokenSequence<?> ts = hi.tokenSequence();

        assertNextTokenEquals(ts, MakefileTokenId.COMMENT, "# Environment");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "MKDIR");
        assertNextTokenEquals(ts, MakefileTokenId.EQUALS, "=");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "mkdir");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "BUILDDIR");
        assertNextTokenEquals(ts, MakefileTokenId.EQUALS, "=");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "build/");
        assertNextTokenEquals(ts, MakefileTokenId.MACRO, "${CONF}");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "OS");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.COLON_EQUALS, ":=");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.MACRO, "$(shell uname | grep -i Darwin)");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "build");
        assertNextTokenEquals(ts, MakefileTokenId.COLON, ":");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.TAB, "\t");
        assertNextTokenEquals(ts, MakefileTokenId.SHELL, "$(COMPILE.cc) source.cpp -o source.o");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.SPECIAL_TARGET, ".PHONY");
        assertNextTokenEquals(ts, MakefileTokenId.COLON, ":");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "build");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.KEYWORD, "include");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "foo.mk");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");

        assertFalse("Unexpected tokens remaining", ts.moveNext());
    }

    public void testBare() {
        String text = "a\\ b := a\\:b\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, new MakefileLanguageHierarchy().language());
        TokenSequence<?> ts = hi.tokenSequence();

        assertNextTokenEquals(ts, MakefileTokenId.BARE, "a\\ b");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.COLON_EQUALS, ":=");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "a\\:b");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");

        assertFalse("Unexpected tokens remaining", ts.moveNext());
    }

    public void testTabs() {
        String text = "\tfoo\n\t\tbar\n \tbaz";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, new MakefileLanguageHierarchy().language());
        TokenSequence<?> ts = hi.tokenSequence();

        assertNextTokenEquals(ts, MakefileTokenId.TAB, "\t");
        assertNextTokenEquals(ts, MakefileTokenId.SHELL, "foo");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.TAB, "\t");
        assertNextTokenEquals(ts, MakefileTokenId.SHELL, "\tbar");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " \t");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "baz");

        assertFalse("Unexpected tokens remaining", ts.moveNext());
    }

    public void testNewline() {
        String text = "var = foo\\\n\\\r\n\tbar\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, new MakefileLanguageHierarchy().language());
        TokenSequence<?> ts = hi.tokenSequence();

        assertNextTokenEquals(ts, MakefileTokenId.BARE, "var");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.EQUALS, "=");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "foo");
        assertNextTokenEquals(ts, MakefileTokenId.ESCAPED_NEW_LINE, "\\\n");
        assertNextTokenEquals(ts, MakefileTokenId.ESCAPED_NEW_LINE, "\\\r\n");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, "\t");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "bar");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");

        assertFalse("Unexpected tokens remaining", ts.moveNext());
    }

    public void testShell() {
        String text = "\techo foo\\\nbar\\\n\tbaz";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, new MakefileLanguageHierarchy().language());
        TokenSequence<?> ts = hi.tokenSequence();

        assertNextTokenEquals(ts, MakefileTokenId.TAB, "\t");
        assertNextTokenEquals(ts, MakefileTokenId.SHELL, "echo foo\\\nbar\\\n\tbaz");

        assertFalse("Unexpected tokens remaining", ts.moveNext());
    }

    public void testMacro() {
        String text = "ab=$(a\nb)\ncd=$(c\\\nd)";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, new MakefileLanguageHierarchy().language());
        TokenSequence<?> ts = hi.tokenSequence();

        assertNextTokenEquals(ts, MakefileTokenId.BARE, "ab");
        assertNextTokenEquals(ts, MakefileTokenId.EQUALS, "=");
        assertNextTokenEquals(ts, MakefileTokenId.MACRO, "$(a");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "b)");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "cd");
        assertNextTokenEquals(ts, MakefileTokenId.EQUALS, "=");
        assertNextTokenEquals(ts, MakefileTokenId.MACRO, "$(c\\\nd)");

        assertFalse("Unexpected tokens remaining", ts.moveNext());
    }
}
