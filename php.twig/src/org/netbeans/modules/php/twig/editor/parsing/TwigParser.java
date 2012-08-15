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
package org.netbeans.modules.php.twig.editor.parsing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import javax.swing.event.ChangeListener;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.php.twig.editor.lexer.TwigTokenId;

public class TwigParser extends Parser {

    Snapshot snapshot;
    TwigParserResult result;
    final static List<String> PARSE_ELEMENTS = new ArrayList<String>();

    static {
        PARSE_ELEMENTS.add("for"); //NOI18N
        PARSE_ELEMENTS.add("endfor"); //NOI18N

        PARSE_ELEMENTS.add("if"); //NOI18N
        //parseElements.add( "else" ); // TODO: Check for enclosing if block!
        //parseElements.add( "elseif" ); // TODO: Same as above!
        PARSE_ELEMENTS.add("endif"); //NOI18N

        PARSE_ELEMENTS.add("block"); //NOI18N
        PARSE_ELEMENTS.add("endblock"); //NOI18N

        PARSE_ELEMENTS.add("set"); //NOI18N
        PARSE_ELEMENTS.add("endset"); //NOI18N

        PARSE_ELEMENTS.add("macro"); //NOI18N
        PARSE_ELEMENTS.add("endmacro"); //NOI18N

        PARSE_ELEMENTS.add("filter"); //NOI18N
        PARSE_ELEMENTS.add("endfilter"); //NOI18N

        PARSE_ELEMENTS.add("autoescape"); //NOI18N
        PARSE_ELEMENTS.add("endautoescape"); //NOI18N

        PARSE_ELEMENTS.add("spaceless"); //NOI18N
        PARSE_ELEMENTS.add("endspaceless"); //NOI18N

    }

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent sme) throws ParseException {
        this.snapshot = snapshot;
        result = new TwigParserResult(snapshot);
        TokenHierarchy<?> tokenHierarchy = snapshot.getTokenHierarchy();
        LanguagePath twigPath = null;
        for (LanguagePath path : tokenHierarchy.languagePaths()) {
            if (path.mimePath().endsWith("twig-markup")) { //NOI18N
                twigPath = path;
                break;
            }
        }

        if (twigPath != null) {

            List<TokenSequence<?>> tokenSequenceList = tokenHierarchy.tokenSequenceList(twigPath, 0, Integer.MAX_VALUE);
            List<Instruction> instructionList = new ArrayList<Instruction>();

            for (TokenSequence<?> sequence : tokenSequenceList) {

                while (sequence.moveNext()) {

                    Token<TwigTokenId> token = (Token<TwigTokenId>) sequence.token();

                    /* Parse instruction */

                    if (token.id() == TwigTokenId.T_TWIG_INSTRUCTION) {

                        Instruction instruction = new Instruction();
                        instruction.function = "";
                        instruction.startTokenIndex = sequence.index();
                        instruction.endTokenIndex = sequence.index();
                        instruction.from = token.offset(tokenHierarchy);

                        while (sequence.moveNext()) {

                            token = (Token<TwigTokenId>) sequence.token();
                            if (token.id() == TwigTokenId.T_TWIG_NAME) {
                                instruction.extra = token.text();
                            }
                            if (token.id() == TwigTokenId.T_TWIG_INSTRUCTION) {
                                instruction.endTokenIndex = sequence.index();
                                instruction.length = token.offset(tokenHierarchy) - instruction.from + token.length();
                                break;
                            }

                        }

                        if (instruction.startTokenIndex != instruction.endTokenIndex) { // Closed instruction found

                            sequence.moveIndex(instruction.startTokenIndex);

                            while (sequence.moveNext()) {

                                token = (Token<TwigTokenId>) sequence.token();
                                if (token.id() == TwigTokenId.T_TWIG_FUNCTION) {

                                    instruction.function = token.text();
                                    instruction.functionTokenIndex = sequence.index();
                                    instruction.functionFrom = token.offset(tokenHierarchy);
                                    instruction.functionLength = token.length();
                                    break;

                                }

                            }

                            if (PARSE_ELEMENTS.contains(instruction.function.toString())) {
                                /* Have we captured a standalone instruction? */
                                if (CharSequenceUtilities.equals(instruction.function, "block")) { //NOI18N

                                    boolean standalone = false;
                                    int names = 0;

                                    do {

                                        sequence.moveNext();
                                        token = (Token<TwigTokenId>) sequence.token();

                                        if (token.id() == TwigTokenId.T_TWIG_NAME || token.id() == TwigTokenId.T_TWIG_STRING) {
                                            names++;
                                        }

                                        if (names > 1) {
                                            standalone = true;
                                            break;
                                        }

                                    } while (sequence.index() < instruction.endTokenIndex);

                                    if (!standalone) {
                                        instructionList.add(instruction);
                                    } else { // add a inline "block" immediately to the result set
                                        result.addBlock("*inline-block", instruction.from, instruction.length, instruction.extra); //NOI18N
                                    }

                                } else if (CharSequenceUtilities.equals(instruction.function, "set")) { //NOI18N

                                    boolean standalone = false;

                                    do {

                                        sequence.moveNext();
                                        token = (Token<TwigTokenId>) sequence.token();

                                        if (token.id() == TwigTokenId.T_TWIG_OPERATOR) {
                                            standalone = true;
                                            break;
                                        }

                                    } while (sequence.index() < instruction.endTokenIndex);

                                    if (!standalone) {
                                        instructionList.add(instruction);
                                    }

                                } else {
                                    instructionList.add(instruction);
                                }

                            }

                            sequence.moveIndex(instruction.endTokenIndex);

                        }

                    }

                }

            } // endfor: All instructions are now saved in instructionList

            /* Analyse instruction structure */

            Stack<Instruction> instructionStack = new Stack<Instruction>();

            for (Instruction instruction : instructionList) {

                if (CharSequenceUtilities.startsWith(instruction.function, "end")) { //NOI18N

                    if (instructionStack.empty()) { // End tag, but no more tokens on stack!

                        result.addError(
                                "Unopened '" + instruction.function + "' block",
                                instruction.functionFrom,
                                instruction.functionLength);

                    } else if (CharSequenceUtilities.endsWith(instruction.function, instructionStack.peek().function)) {
                        // end[sth] found a [sth] on the stack!

                        Instruction start = instructionStack.pop();
                        result.addBlock(start.function, start.from, instruction.from - start.from + instruction.length, start.extra);

                    } else {
                        // something wrong lies on the stack!
                        // assume that current token is invalid and let it stay on the stack

                        result.addError(
                                "Unexpected '" + instruction.function + "', expected 'end" + instructionStack.peek().function + "'",
                                instruction.functionFrom,
                                instruction.functionLength);

                    }

                } else {
                    instructionStack.push(instruction);
                }

            }

            // All instructions were parsed. Are there any left on the stack?
            if (!instructionStack.empty()) {
                // Yep, they were never closed!

                while (!instructionStack.empty()) {

                    Instruction instruction = instructionStack.pop();

                    result.addError(
                            "Unclosed '" + instruction.function + "'",
                            instruction.functionFrom,
                            instruction.functionLength);

                }

            }

            // Parsing done!

        }

    }

    @Override
    public Result getResult(Task task) throws ParseException {
        return result;
    }

    @Override
    public void addChangeListener(ChangeListener cl) {
    }

    @Override
    public void removeChangeListener(ChangeListener cl) {
    }

    static public class Factory extends ParserFactory {

        @Override
        public Parser createParser(Collection<Snapshot> clctn) {
            return new TwigParser();
        }
    }

    class Instruction {

        CharSequence function = null;
        CharSequence extra = null;
        int startTokenIndex = 0;
        int endTokenIndex = 0;
        int functionTokenIndex = 0;
        int from = 0;
        int length = 0;
        int functionFrom = 0;
        int functionLength = 0;
    }
}
