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

package org.netbeans.modules.db.sql.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.db.sql.analyzer.SQLStatement.Context;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;

/**
 *
 * @author Andrei Badea
 */
class SelectStatementAnalyzer extends SQLStatementAnalyzer {

    private final List<List<String>> selectValues = new ArrayList<List<String>>();
    private final List<FromTable> fromTables = new ArrayList<FromTable>();
    private final List<SelectStatement> subqueries = new ArrayList<SelectStatement>();

    public static SelectStatement analyze(TokenSequence<SQLTokenId> seq, Quoter quoter) {
        seq.moveStart();
        if (!seq.moveNext()) {
            return null;
        }
        SelectStatementAnalyzer sa = new SelectStatementAnalyzer(seq, quoter);
        sa.parse();
        // Return a non-null FromClause if there was a FROM clause in the statement.
        FromClause fromClause = sa.context.isAfter(Context.FROM) ? sa.createFromClause() : null;
        return new SelectStatement(sa.startOffset, seq.offset() + seq.token().length(), Collections.unmodifiableList(sa.selectValues), fromClause, Collections.unmodifiableList(sa.subqueries), sa.offset2Context);
    }

    private SelectStatementAnalyzer(TokenSequence<SQLTokenId> seq, Quoter quoter) {
        super(seq, quoter);
    }

    private FromClause createFromClause() {
        Set<QualIdent> unaliasedTableNames = new HashSet<QualIdent>();
        Map<String, QualIdent> aliasedTableNames = new HashMap<String, QualIdent>();
        for (FromTable table : fromTables) {
            if (table.alias == null) {
                unaliasedTableNames.add(table.tableName);
            } else {
                if (!aliasedTableNames.containsKey(table.alias)) {
                    aliasedTableNames.put(table.alias, table.tableName);
                }
            }
        }
        return new FromClause(Collections.unmodifiableSet(unaliasedTableNames), Collections.unmodifiableMap(aliasedTableNames));
    }

    private void parse() {
        startOffset = seq.offset();
        boolean afterFromTableKeyword = false;
        do {
            switch (context) {
                case START:
                    if (SQLStatementAnalyzer.isKeyword ("SELECT", seq)) { // NOI18N
                        moveToContext(Context.SELECT);
                    }
                    break;
                case SELECT:
                    switch (seq.token().id()) {
                        case IDENTIFIER:
                            List<String> selectValue = analyzeSelectValue();
                            if (!selectValue.isEmpty()) {
                                selectValues.add(selectValue);
                            }
                            break;
                        case KEYWORD:
                            if (SQLStatementAnalyzer.isKeyword ("FROM", seq)) { // NOI18N
                                moveToContext(Context.FROM);
                                afterFromTableKeyword = true;
                            }
                            break;
                    }
                    break;
                case FROM:
                    switch (seq.token().id()) {
                        case IDENTIFIER:
                            if (afterFromTableKeyword) {
                                FromTable fromTable = parseFromTable();
                                if (fromTable != null) {
                                    fromTables.add(fromTable);
                                }
                                afterFromTableKeyword = false;
                            }
                            break;
                        case COMMA:
                            afterFromTableKeyword = true;
                            break;
                        case KEYWORD:
                            if (SQLStatementAnalyzer.isKeyword ("JOIN", seq)) { // NOI18N
                                afterFromTableKeyword = true;
                            } else {
                                Context newContext = getContextForKeywordAfterFrom();
                                if (newContext != null) {
                                    moveToContext(newContext);
                                }
                            }
                            break;
                    }
                    break;
                case JOIN_CONDITION:
                    switch (seq.token().id()) {
                        case COMMA:
                            moveToContext(Context.FROM);
                            afterFromTableKeyword = true;
                            break;
                        case KEYWORD:
                            if (SQLStatementAnalyzer.isKeyword ("JOIN", seq)) { // NOI18N
                                moveToContext(Context.FROM);
                                afterFromTableKeyword = true;
                            }
                            break;
                    }
                    break;
                case GROUP:
                    if (SQLStatementAnalyzer.isKeyword("BY", seq)) { // NOI18N
                        moveToContext(Context.GROUP_BY);
                    }
                    break;
                case ORDER:
                    if (SQLStatementAnalyzer.isKeyword("BY", seq)) { // NOI18N
                        moveToContext(Context.ORDER_BY);
                    }
                    break;
                default:
                    Context newState = getContextForKeywordAfterFrom();
                    if (newState != null) {
                        moveToContext(newState);
                    }
                    break;
            }
        } while (nextToken());
    }

    private List<String> analyzeSelectValue() {
        List<String> parts = new ArrayList<String>();
        parts.add(getUnquotedIdentifier());
        boolean afterDot = false;
        main: for (;;) {
            if (!nextToken()) {
                return parts;
            }
            switch (seq.token().id()) {
                case DOT:
                    // Tentatively considering this a qualified identifier.
                    afterDot = true;
                    break;
                case IDENTIFIER:
                    if (afterDot) {
                        afterDot = false;
                        parts.add(getUnquotedIdentifier());
                    } else {
                        // Alias like "foo.bar baz".
                        parts.clear();
                        parts.add(getUnquotedIdentifier());
                    }
                    break;
                case LPAREN:
                    // Looks like function call.
                    parts.clear();
                    break;
                case COMMA:
                    break main;
                case KEYWORD:
                    if (SQLStatementAnalyzer.isKeyword ("AS", seq)) { // NOI18N
                        // Alias will follow.
                        afterDot = false;
                        parts.clear();
                    } else if (SQLStatementAnalyzer.isKeyword ("FROM", seq) || isKeywordAfterFrom()) { // NOI18N
                        break main;
                    }
                    break;
                default:
            }
        }
        // Need to process the current token,
        // which doesn't belong to the current SELECT value, once again.
        seq.movePrevious();
        return parts;
    }

    /** Returns table name and its alias. */
    private FromTable parseFromTable() {
        QualIdent table = parseIdentifier();
        if (table == null) {
            return null;
        }
        String alias = parseAlias();
        return new FromTable(table, alias);
    }

    /** Returns alias of table or null (e.g. returns c if statement is as
     * follows "select * from customer as c". */
    private String parseAlias() {
        String alias = null;
        main: while (nextToken()) {
            switch (seq.token().id()) {
                case IDENTIFIER:
                    alias = getUnquotedIdentifier();
                    break;
                case KEYWORD:
                    if (!SQLStatementAnalyzer.isKeyword("AS", seq)) { // NOI18N
                        seq.movePrevious();
                        break main;
                    }
                    break;
                default:
                    seq.movePrevious();
                    break main;
            }
        }
        if (alias != null && alias.length() == 0) {
            alias = null;
        }
        return alias;
    }

    @Override
    protected boolean nextToken() {
        boolean move = super.nextToken();
        if (context.isAfter(Context.SELECT) && SQLStatementAnalyzer.isKeyword("SELECT", seq)) { // NOI18N
            // Looks like a subquery.
            int subStartOffset = seq.offset();
            int parLevel = 1;
            main: while (move = seq.moveNext()) {
                switch (seq.token().id()) {
                    case LPAREN:
                        parLevel++;
                        break;
                    case RPAREN:
                        if (--parLevel == 0) {
                            TokenSequence<SQLTokenId> subSeq = seq.subSequence(subStartOffset, seq.offset());
                            SelectStatement subquery = SelectStatementAnalyzer.analyze(subSeq, quoter);
                            if (subquery != null) {
                                subqueries.add(subquery);
                            }
                            break main;
                        }
                        break;
                }
            }
        }
        return move;
    }

    private boolean isKeywordAfterFrom() {
        return getContextForKeywordAfterFrom() != null;
    }

    private Context getContextForKeywordAfterFrom() {
        if (SQLStatementAnalyzer.isKeyword("ON", seq)) { // NOI18N
            return Context.JOIN_CONDITION;
        } else if (SQLStatementAnalyzer.isKeyword("WHERE", seq)) { // NOI18N
            return Context.WHERE;
        } else if (SQLStatementAnalyzer.isKeyword("GROUP", seq)) { // NOI18N
            return Context.GROUP;
        } else if (SQLStatementAnalyzer.isKeyword("HAVING", seq)) { // NOI18N
            return Context.HAVING;
        } else if (SQLStatementAnalyzer.isKeyword("ORDER", seq)) { // NOI18N
            return Context.ORDER;
        }
        return null;
    }

    private static class FromTable {

        private final QualIdent tableName;
        private final String alias;

        public FromTable(QualIdent tableName, String alias) {
            this.tableName = tableName;
            this.alias = alias;
        }
    }
}
