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
package org.netbeans.modules.css.lib.nblexer;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.css.lib.api.CssTokenId;

/**
 * @author  marek.fukala@sun.com
 */
public class NbCss3LexerTest extends NbTestCase {

    public NbCss3LexerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
    }

    //http://www.netbeans.org/issues/show_bug.cgi?id=161642
    public void testIssue161642() throws Exception {
        String input = "/* c */;";
        TokenHierarchy th = TokenHierarchy.create(input, CssTokenId.language());
        TokenSequence ts = th.tokenSequence();
        ts.moveStart();

        assertTrue(ts.moveNext());
        assertEquals("/* c */", ts.token().text().toString());
        assertEquals(CssTokenId.COMMENT, ts.token().id());
        assertEquals("comment", ts.token().id().primaryCategory());

        assertTrue(ts.moveNext());
        assertEquals(";", ts.token().text().toString());
        assertEquals(CssTokenId.SEMI, ts.token().id());
    }

    public void testBasicLexing() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/testBasic.css.txt",
                CssTokenId.language());
    }

    public void testLexingOfMissingTokens() throws Exception {
        String code = "a {\n"
                + " @ color: red; \n"
                + " background: red; \n"
                + "}";

        TokenHierarchy th = TokenHierarchy.create(code, CssTokenId.language());
        TokenSequence ts = th.tokenSequence();
        ts.moveStart();

        while (ts.moveNext()) {
            System.out.println(ts.offset() + "-" + (ts.token().length() + ts.offset()) + ": " + ts.token().text() + "(" + ts.token().id() + ")");
        }

    }
    
    //currently fails! FIX!!!
    public void testLexing_Netbeans_org() throws Exception {
      LexerTestUtilities.checkTokenDump(this, "testfiles/netbeans.css",
                CssTokenId.language());
    };
    
    public void testInput() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/testInputGeneratedCode.css.txt",
                CssTokenId.language());
    }

    public void testImportsLexing() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/testImportsLexing.css.txt",
                CssTokenId.language());
    }

    
}
