/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.db.sql.editor;

import java.util.Arrays;
import java.util.Iterator;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Andrei Badea, Jiri Skrivanek
 */
public class SQLSyntaxTest extends NbTestCase {

    public SQLSyntaxTest(String testName) {
        super(testName);
    }

    public void testNumberLiteralsEndWithFirstNonDigitCharIssue67379() {
        assertTokens("10-20.3-3", new TokenID[] {
            SQLTokenContext.INT_LITERAL,
            SQLTokenContext.OPERATOR,
            SQLTokenContext.DOUBLE_LITERAL,
            SQLTokenContext.OPERATOR,
            SQLTokenContext.INT_LITERAL,
        });

        assertTokens("10foo", new TokenID[] {
            SQLTokenContext.INT_LITERAL,
            SQLTokenContext.IDENTIFIER,
        });
    }

    public void testLast() {
        SQLSyntax s = new SQLSyntax();
        String sql = "`ident";
        s.load(null, sql.toCharArray(), 0, sql.length(), true, sql.length());
        assertEquals(SQLTokenContext.IDENTIFIER, s.nextToken());
        sql = "select foo bar baz";
        s.load(null, sql.toCharArray(), 0, sql.length(), true, sql.length());
        assertEquals(SQLTokenContext.KEYWORD, s.nextToken());
    }

    public void testEscapeSingleQuote() {
        assertTokens("'Frank\\'s Book'", SQLTokenContext.STRING);
        assertTokens("'Frank''s Book'", SQLTokenContext.STRING, SQLTokenContext.STRING);
        assertTokens("'Frank\\s Book'", SQLTokenContext.STRING);
        assertTokens("'Frank\\", SQLTokenContext.INCOMPLETE_STRING);
        assertTokens("'Frank\\'", SQLTokenContext.INCOMPLETE_STRING);
    }

    public void testSlash() {
        assertTokens("((EndTime-StartTime)/2)*5", new TokenID[] {
            SQLTokenContext.OPERATOR,
            SQLTokenContext.OPERATOR,
            SQLTokenContext.IDENTIFIER,
            SQLTokenContext.OPERATOR,
            SQLTokenContext.IDENTIFIER,
            SQLTokenContext.OPERATOR,
            SQLTokenContext.OPERATOR,
            SQLTokenContext.INT_LITERAL,
            SQLTokenContext.OPERATOR,
            SQLTokenContext.OPERATOR,
            SQLTokenContext.INT_LITERAL,
        });
    }

    public void testComments() {
        assertTokens("select /* block comment */ * from #notLineComment -- line comment",
            SQLTokenContext.KEYWORD,
            SQLTokenContext.WHITESPACE,
            SQLTokenContext.BLOCK_COMMENT,
            SQLTokenContext.WHITESPACE,
            SQLTokenContext.OPERATOR,
            SQLTokenContext.WHITESPACE,
            SQLTokenContext.KEYWORD,
            SQLTokenContext.WHITESPACE,
            SQLTokenContext.IDENTIFIER,
            SQLTokenContext.WHITESPACE,
            SQLTokenContext.LINE_COMMENT);
        // https://netbeans.org/bugzilla/show_bug.cgi?id=172904
        assertTokens("# MySQL Line Comment", SQLTokenContext.LINE_COMMENT);
        // https://netbeans.org/bugzilla/show_bug.cgi?id=181020
        assertTokens("# my line comment \nselect * from mytable",
                SQLTokenContext.LINE_COMMENT,
                SQLTokenContext.WHITESPACE,
                SQLTokenContext.KEYWORD,
                SQLTokenContext.WHITESPACE,
                SQLTokenContext.OPERATOR,
                SQLTokenContext.WHITESPACE,
                SQLTokenContext.KEYWORD,
                SQLTokenContext.WHITESPACE,
                SQLTokenContext.IDENTIFIER
                );
        // https://netbeans.org/bugzilla/show_bug.cgi?id=191188
        assertTokens("select * from mytable where id# = 1",
            SQLTokenContext.KEYWORD,
            SQLTokenContext.WHITESPACE,
            SQLTokenContext.OPERATOR,
            SQLTokenContext.WHITESPACE,
            SQLTokenContext.KEYWORD,
            SQLTokenContext.WHITESPACE,
            SQLTokenContext.IDENTIFIER,
            SQLTokenContext.WHITESPACE,
            SQLTokenContext.KEYWORD,
            SQLTokenContext.WHITESPACE,
            SQLTokenContext.IDENTIFIER,
            SQLTokenContext.WHITESPACE,
            SQLTokenContext.OPERATOR,
            SQLTokenContext.WHITESPACE,
            SQLTokenContext.INT_LITERAL);
    }

    
    public void testHashInIdentifier() {
        assertTokens("id# = 1", SQLTokenContext.IDENTIFIER, SQLTokenContext.WHITESPACE, SQLTokenContext.OPERATOR, SQLTokenContext.WHITESPACE, SQLTokenContext.INT_LITERAL);
    }

    private void assertTokens(String m, TokenID... tokens) {
        Syntax s = new SQLSyntax();
        s.load(null, m.toCharArray(), 0, m.length(), true, m.length());
        
        TokenID token = null;
        Iterator<TokenID> i = Arrays.asList(tokens).iterator();
        do {
            token = s.nextToken();
            if (token != null) {
                if (!i.hasNext()) {
                    fail("More tokens returned than expected.");
                } else {
                    assertSame("Tokens differ", i.next(), token);
                }
            } else {
                assertFalse("More tokens expected than returned.", i.hasNext());
            }
            if (token != null) {
                log(token.getName());
            }
        } while (token != null);
    }
}
