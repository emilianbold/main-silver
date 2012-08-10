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
package org.netbeans.modules.javascript2.editor.formatter;

import com.oracle.nashorn.ir.*;
import com.oracle.nashorn.parser.TokenType;
import java.util.*;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;

/**
 *
 * @author Petr Hejl
 */
public class FormatVisitor extends NodeVisitor {

    private static final Set<TokenType> UNARY_TYPES = EnumSet.noneOf(TokenType.class);

    static {
        Collections.addAll(UNARY_TYPES, TokenType.ADD, TokenType.SUB,
                TokenType.BIT_NOT, TokenType.NOT,
                TokenType.INCPOSTFIX, TokenType.INCPREFIX,
                TokenType.DECPOSTFIX, TokenType.DECPREFIX);
    }

    private final TokenSequence<? extends JsTokenId> ts;

    private final FormatTokenStream tokenStream;

    private final int formatFinish;

    private final Set<Block> caseNodes = new HashSet<Block>();

    public FormatVisitor(FormatTokenStream tokenStream, TokenSequence<? extends JsTokenId> ts, int formatFinish) {
        this.ts = ts;
        this.tokenStream = tokenStream;
        this.formatFinish = formatFinish;
    }

    @Override
    public Node visit(Block block, boolean onset) {
        if (onset && (block instanceof FunctionNode || isScript(block)
                || block.getStart() < block.getFinish())) {

            if (caseNodes.contains(block)) {
                // if the block is real block it is reused down the ast tree
                // so we need to remove it to be handled normally later
                caseNodes.remove(block);
                handleCaseBlock(block);
            } else if (isScript(block)){
                handleBlockContent(block);
            } else {
                handleStandardBlock(block);
            }
        }

        if (block instanceof FunctionNode || isScript(block)
                || block.getStart() != block.getFinish()) {
            return null;
        } else {
            return super.visit(block, onset);
        }
    }

    @Override
    public Node visit(CaseNode caseNode, boolean onset) {
        // we need to mark if block is case body as block itself has
        // no reference to case node
        if (onset) {
            caseNodes.add(caseNode.getBody());
        } else {
            caseNodes.remove(caseNode.getBody());
        }
        return super.visit(caseNode, onset);
    }

    @Override
    public Node visit(WhileNode whileNode, boolean onset) {
        if (onset) {
            // within parens spaces
            markSpacesWithinParentheses(whileNode, getStart(whileNode), getStart(whileNode.getBody()),
                    FormatToken.Kind.AFTER_WHILE_PARENTHESIS, FormatToken.Kind.BEFORE_WHILE_PARENTHESIS);

            // mark space before left brace
            markSpacesBeforeBrace(whileNode.getBody(), FormatToken.Kind.BEFORE_WHILE_BRACE);

            if (handleWhile(whileNode, FormatToken.Kind.AFTER_WHILE_START)) {
                return null;
            }
        }

        return super.visit(whileNode, onset);
    }

    @Override
    public Node visit(DoWhileNode doWhileNode, boolean onset) {
        if (onset) {
            // within parens spaces
            markSpacesWithinParentheses(doWhileNode, getFinish(doWhileNode.getBody()), getFinish(doWhileNode),
                    FormatToken.Kind.AFTER_WHILE_PARENTHESIS, FormatToken.Kind.BEFORE_WHILE_PARENTHESIS);

            // mark space before left brace
            markSpacesBeforeBrace(doWhileNode.getBody(), FormatToken.Kind.BEFORE_DO_BRACE);

            FormatToken whileToken = getPreviousToken(doWhileNode.getFinish(), JsTokenId.KEYWORD_WHILE);
            FormatToken beforeWhile = whileToken.previous();
            if (beforeWhile != null) {
                appendToken(beforeWhile, FormatToken.forFormat(FormatToken.Kind.BEFORE_WHILE_KEYWORD));
            }
            if (handleWhile(doWhileNode, FormatToken.Kind.AFTER_DO_START)) {
                return null;
            }
        }

        return super.visit(doWhileNode, onset);
    }

    @Override
    public Node visit(ForNode forNode, boolean onset) {
        if (onset) {
            // within parens spaces
            markSpacesWithinParentheses(forNode, getStart(forNode), getStart(forNode.getBody()),
                    FormatToken.Kind.AFTER_FOR_PARENTHESIS, FormatToken.Kind.BEFORE_FOR_PARENTHESIS);

            // mark space before left brace
            markSpacesBeforeBrace(forNode.getBody(), FormatToken.Kind.BEFORE_FOR_BRACE);

            if (handleWhile(forNode, FormatToken.Kind.AFTER_FOR_START)) {
                return null;
            }
        }

        return super.visit(forNode, onset);
    }

    @Override
    public Node visit(IfNode ifNode, boolean onset) {
        if (onset) {
            ifNode.getTest().accept(this);

            // within parens spaces
            markSpacesWithinParentheses(ifNode, getStart(ifNode), getStart(ifNode.getPass()),
                    FormatToken.Kind.AFTER_IF_PARENTHESIS, FormatToken.Kind.BEFORE_IF_PARENTHESIS);

            // pass block
            Block body = ifNode.getPass();
            // mark space before left brace
            markSpacesBeforeBrace(body, FormatToken.Kind.BEFORE_IF_BRACE);

            if (body.getStart() == body.getFinish()) {
                handleVirtualBlock(body, FormatToken.Kind.AFTER_IF_START);
            } else {
                visit(body, onset);
            }

            // fail block
            body = ifNode.getFail();
            if (body != null) {
                // mark space before left brace
                markSpacesBeforeBrace(body, FormatToken.Kind.BEFORE_ELSE_BRACE);

                if (body.getStart() == body.getFinish()) {
                    // do the standard block related things
                    if (body.getStatements().get(0) instanceof IfNode) {
                        // we mark else if statement here
                        handleVirtualBlock(body, FormatToken.Kind.ELSE_IF_INDENTATION_INC,
                                FormatToken.Kind.ELSE_IF_INDENTATION_DEC, FormatToken.Kind.ELSE_IF_AFTER_BLOCK_START);
                    } else {
                        handleVirtualBlock(body, FormatToken.Kind.AFTER_ELSE_START);
                    }
                } else {
                    visit(body, onset);
                }
            }
        }

        return null;
    }

    @Override
    public Node visit(WithNode withNode, boolean onset) {
        if (onset) {
            // within parens spaces
            markSpacesWithinParentheses(withNode, getStart(withNode), getStart(withNode.getBody()),
                    FormatToken.Kind.AFTER_WITH_PARENTHESIS, FormatToken.Kind.BEFORE_WITH_PARENTHESIS);

            Block body = withNode.getBody();

            // mark space before left brace
            markSpacesBeforeBrace(body, FormatToken.Kind.BEFORE_WITH_BRACE);

            if (body.getStart() == body.getFinish()) {
                handleVirtualBlock(body, FormatToken.Kind.AFTER_WITH_START);
                return null;
            }
        }

        return super.visit(withNode, onset);
    }


    @Override
    public Node visit(FunctionNode functionNode, boolean onset) {
        visit((Block) functionNode, onset);

        if (onset && !isScript(functionNode)) {
            int start = getFunctionStart(functionNode);

            FormatToken leftParen = getNextToken(start, JsTokenId.BRACKET_LEFT_PAREN);
            if (leftParen != null) {
                FormatToken previous = leftParen.previous();
                if (previous != null) {
                    appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_FUNCTION_DECLARATION));
                }

                // mark the within parenthesis places

                // remove original paren marks
                FormatToken mark = leftParen.next();
                assert mark.getKind() == FormatToken.Kind.AFTER_LEFT_PARENTHESIS : mark.getKind();
                tokenStream.removeToken(mark);

                // this works if the offset starts with block as it is now
                FormatToken rightParen = getPreviousToken(getStart(functionNode),
                        JsTokenId.BRACKET_RIGHT_PAREN, leftParen.getOffset());
                if (rightParen != null) {
                    previous = rightParen.previous();
                    assert previous.getKind() == FormatToken.Kind.BEFORE_RIGHT_PARENTHESIS : previous.getKind();
                    tokenStream.removeToken(previous);
                }

                // mark left brace of block - this works if function node
                // start offset is offset of the left brace
                FormatToken leftBrace = getNextToken(getStart(functionNode),
                        JsTokenId.BRACKET_LEFT_CURLY, getFinish(functionNode));
                if (leftBrace != null) {
                    previous = leftBrace.previous();
                    if (previous != null) {
                        appendToken(previous, FormatToken.forFormat(
                                FormatToken.Kind.BEFORE_FUNCTION_DECLARATION_BRACE));
                    }
                }

                // place the new marks
                if (!functionNode.getParameters().isEmpty()) {
                    appendToken(leftParen, FormatToken.forFormat(
                            FormatToken.Kind.AFTER_FUNCTION_DECLARATION_PARENTHESIS));

                    if (rightParen != null) {
                        previous = rightParen.previous();
                        if (previous != null) {
                            appendToken(previous, FormatToken.forFormat(
                                    FormatToken.Kind.BEFORE_FUNCTION_DECLARATION_PARENTHESIS));
                        }
                    }
                }

                // place function parameters marks
                for (IdentNode param : functionNode.getParameters()) {
                    FormatToken ident = getNextToken(getStart(param), JsTokenId.IDENTIFIER);
                    if (ident != null) {
                        FormatToken beforeIdent = ident.previous();
                        if (beforeIdent != null) {
                            appendToken(beforeIdent,
                                    FormatToken.forFormat(FormatToken.Kind.BEFORE_FUNCTION_DECLARATION_PARAMETER));
                        }
                    }
                }

                if (functionNode.isStatement()) {
                    FormatToken rightBrace = getPreviousToken(getFinish(functionNode),
                            JsTokenId.BRACKET_RIGHT_CURLY, leftBrace.getOffset());
                    if (rightBrace != null) {
                        appendToken(rightBrace, FormatToken.forFormat(
                                FormatToken.Kind.AFTER_STATEMENT));
                    }
                }
            }

        }
        return null;
    }

    @Override
    public Node visit(CallNode callNode, boolean onset) {
        if (onset) {
            FormatToken leftBrace = getNextToken(getFinish(callNode.getFunction()),
                    JsTokenId.BRACKET_LEFT_PAREN, getFinish(callNode));
            if (leftBrace != null) {
                FormatToken previous = leftBrace.previous();
                appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_FUNCTION_CALL));

                // mark the within parenthesis places

                // remove original paren marks
                FormatToken mark = leftBrace.next();
                assert mark.getKind() == FormatToken.Kind.AFTER_LEFT_PARENTHESIS : mark.getKind();
                tokenStream.removeToken(mark);

                // there is -1 as on the finish position may be some outer paren
                // so we really need the position precisely
                FormatToken rightBrace = getPreviousToken(getFinish(callNode) - 1,
                        JsTokenId.BRACKET_RIGHT_PAREN, getStart(callNode));
                if (rightBrace != null) {
                    previous = rightBrace.previous();
                    while (previous != null && previous.isVirtual()
                            && previous.getKind() != FormatToken.Kind.BEFORE_RIGHT_PARENTHESIS) {
                        previous = previous.previous();
                    }
                    assert previous != null
                            && previous.getKind() == FormatToken.Kind.BEFORE_RIGHT_PARENTHESIS : previous.getKind();
                    tokenStream.removeToken(previous);
                }

                // place the new marks
                if (!callNode.getArgs().isEmpty()) {
                    appendToken(leftBrace, FormatToken.forFormat(
                            FormatToken.Kind.AFTER_FUNCTION_CALL_PARENTHESIS));

                    if (rightBrace != null) {
                        previous = rightBrace.previous();
                        if (previous != null) {
                            appendToken(previous, FormatToken.forFormat(
                                    FormatToken.Kind.BEFORE_FUNCTION_CALL_PARENTHESIS));
                        }
                    }
                }

                // place function arguments marks
                for (Node arg : callNode.getArgs()) {
                    FormatToken argToken = getNextToken(getStart(arg), null);
                    if (argToken != null) {
                        FormatToken beforeArg = argToken.previous();
                        if (beforeArg != null) {
                            appendToken(beforeArg,
                                    FormatToken.forFormat(FormatToken.Kind.BEFORE_FUNCTION_CALL_ARGUMENT));
                        }
                    }
                }
            }
        }
        return super.visit(callNode, onset);
    }


    @Override
    public Node visit(ObjectNode objectNode, boolean onset) {
        if (onset) {
            // indentation mark
            FormatToken formatToken = getPreviousToken(getStart(objectNode), JsTokenId.BRACKET_LEFT_CURLY, true);
            if (formatToken != null) {
                appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
                FormatToken previous = formatToken.previous();
                if (previous != null) {
                    appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_OBJECT));
                }
            }

            for (Node property : objectNode.getElements()) {
                int finish = getFinish(property);

                property.accept(this);

                formatToken = getPreviousToken(finish, null);
                if (formatToken != null) {
                    appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_PROPERTY));
                }
            }

            // put indentation mark after non white token preceeding curly bracket
            formatToken = getPreviousNonWhiteToken(getFinish(objectNode) - 1,
                    getStart(objectNode), JsTokenId.BRACKET_RIGHT_CURLY, true);
            if (formatToken != null) {
                appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
            }
        }

        return null;
    }

    @Override
    public Node visit(SwitchNode switchNode, boolean onset) {
        if (onset) {
            // within parens spaces
            markSpacesWithinParentheses(switchNode);

            // mark space before left brace
            markSpacesBeforeBrace(switchNode);

            FormatToken formatToken = getNextToken(getStart(switchNode), JsTokenId.BRACKET_LEFT_CURLY, true);
            if (formatToken != null) {
                appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
                appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_BLOCK_START));
            }

            List<CaseNode> nodes = new ArrayList<CaseNode>(switchNode.getCases());
            if (switchNode.getDefaultCase() != null) {
                nodes.add(switchNode.getDefaultCase());
            }

            for (CaseNode caseNode : nodes) {
                int start = getStart(caseNode.getBody());

                formatToken = getPreviousToken(start, JsTokenId.OPERATOR_COLON);
                if (formatToken != null) {
                    appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_CASE));
                }
            }

            // put indentation mark after non white token preceeding curly bracket
            formatToken = getPreviousNonWhiteToken(getFinish(switchNode),
                    getStart(switchNode), JsTokenId.BRACKET_RIGHT_CURLY, true);
            if (formatToken != null) {
                appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
            }
        }
        return super.visit(switchNode, onset);
    }

    @Override
    public Node visit(UnaryNode unaryNode, boolean onset) {
        if (onset) {
            TokenType type = unaryNode.tokenType();
            if (UNARY_TYPES.contains(type)) {
                if (TokenType.DECPOSTFIX.equals(type) || TokenType.INCPOSTFIX.equals(type)) {
                    FormatToken formatToken = getPreviousToken(getFinish(unaryNode),
                            TokenType.DECPOSTFIX.equals(type) ? JsTokenId.OPERATOR_DECREMENT : JsTokenId.OPERATOR_INCREMENT);

                    if (formatToken != null) {
                        formatToken = formatToken.previous();
                        if (formatToken != null) {
                            appendToken(formatToken,
                                    FormatToken.forFormat(FormatToken.Kind.BEFORE_UNARY_OPERATOR));
                        }
                    }
                } else {
                    FormatToken formatToken = getNextToken(getStart(unaryNode), null);

                    // remove around binary operator tokens added during token
                    // stream creation
                    if (TokenType.ADD.equals(type) || TokenType.SUB.equals(type)) {
                        assert formatToken.getText() != null
                                && (formatToken.getText().toString().equals(JsTokenId.OPERATOR_PLUS.fixedText())
                                    || formatToken.getText().toString().equals(JsTokenId.OPERATOR_MINUS.fixedText()));
                        FormatToken toRemove = formatToken.previous();
                        while (toRemove != null && toRemove.isVirtual()
                                && toRemove.getKind() != FormatToken.Kind.BEFORE_BINARY_OPERATOR) {
                            toRemove = toRemove.previous();
                        }
                        assert toRemove != null
                                && toRemove.getKind() == FormatToken.Kind.BEFORE_BINARY_OPERATOR : toRemove;
                        tokenStream.removeToken(toRemove);

                        toRemove = formatToken.next();
                        while (toRemove != null && toRemove.isVirtual()
                                && toRemove.getKind() != FormatToken.Kind.AFTER_BINARY_OPERATOR) {
                            toRemove = toRemove.next();
                        }
                        assert toRemove != null
                                && toRemove.getKind() == FormatToken.Kind.AFTER_BINARY_OPERATOR : toRemove;
                        tokenStream.removeToken(toRemove);
                    }

                    if (formatToken != null) {
                        appendToken(formatToken,
                                FormatToken.forFormat(FormatToken.Kind.AFTER_UNARY_OPERATOR));
                    }
                }
            }
        }
        return super.visit(unaryNode, onset);
    }

    @Override
    public Node visit(TernaryNode ternaryNode, boolean onset) {
        if (onset) {
            int start = getStart(ternaryNode.rhs());
            FormatToken question = getPreviousToken(start, JsTokenId.OPERATOR_TERNARY);
            if (question != null) {
                FormatToken previous = question.previous();
                if (previous != null) {
                    appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_TERNARY_OPERATOR));
                }
                appendToken(question, FormatToken.forFormat(FormatToken.Kind.AFTER_TERNARY_OPERATOR));
                FormatToken colon = getPreviousToken(getStart(ternaryNode.third()), JsTokenId.OPERATOR_COLON);
                if (colon != null) {
                    previous = colon.previous();
                    if (previous != null) {
                        appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_TERNARY_OPERATOR));
                    }
                    appendToken(colon, FormatToken.forFormat(FormatToken.Kind.AFTER_TERNARY_OPERATOR));
                }
            }
        }
        return super.visit(ternaryNode, onset);
    }

    @Override
    public Node visit(CatchNode catchNode, boolean onset) {
        if (onset) {
            // within parens spaces
            markSpacesWithinParentheses(catchNode, getStart(catchNode), getStart(catchNode.getBody()),
                    FormatToken.Kind.AFTER_CATCH_PARENTHESIS, FormatToken.Kind.BEFORE_CATCH_PARENTHESIS);

            // mark space before left brace
            markSpacesBeforeBrace(catchNode.getBody(), FormatToken.Kind.BEFORE_CATCH_BRACE);
        }

        return super.visit(catchNode, onset);
    }

    @Override
    public Node visit(TryNode tryNode, boolean onset) {
        if (onset) {
            // mark space before left brace
            markSpacesBeforeBrace(tryNode.getBody(), FormatToken.Kind.BEFORE_TRY_BRACE);

            Block finallyBody = tryNode.getFinallyBody();
            if (finallyBody != null) {
                // mark space before finally left brace
                markSpacesBeforeBrace(tryNode.getFinallyBody(), FormatToken.Kind.BEFORE_FINALLY_BRACE);
            }
        }

        return super.visit(tryNode, onset);
    }

    @Override
    public Node visit(LiteralNode literalNode, boolean onset) {
        if (onset) {
            Object value = literalNode.getValue();
            if (value != null && Collection.class.isAssignableFrom(value.getClass())) {
                int start = getStart(literalNode);
                int finish = getFinish(literalNode);
                FormatToken leftBracket = getNextToken(start, JsTokenId.BRACKET_LEFT_BRACKET, finish);
                if (leftBracket != null) {
                    appendToken(leftBracket, FormatToken.forFormat(FormatToken.Kind.AFTER_ARRAY_LITERAL_BRACKET));
                    FormatToken rightBracket = getPreviousToken(finish - 1, JsTokenId.BRACKET_RIGHT_BRACKET, start + 1);
                    if (rightBracket != null) {
                        FormatToken previous = rightBracket.previous();
                        if (previous != null) {
                            appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_ARRAY_LITERAL_BRACKET));
                        }
                    }
                }
            }
        }
        return super.visit(literalNode, onset);
    }

    @Override
    public Node visit(VarNode varNode, boolean onset) {
        if (onset) {
            int finish = getFinish(varNode) - 1;
            Token nextToken = getNextNonEmptyToken(finish);
            if (nextToken != null && nextToken.id() == JsTokenId.OPERATOR_COMMA) {
                FormatToken formatToken = tokenStream.getToken(ts.offset());
                if (formatToken != null) {
                    FormatToken next = formatToken.next();
                    assert next.getKind() == FormatToken.Kind.AFTER_COMMA : next.getKind();
                    appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_VAR_DECLARATION));
                }
            }
        }
        return super.visit(varNode, onset);
    }

    private boolean handleWhile(WhileNode whileNode, FormatToken.Kind afterStart) {
        Block body = whileNode.getBody();
        if (body.getStart() == body.getFinish()) {
            handleVirtualBlock(body, afterStart);
            return true;
        }
        return false;
    }

    private void handleStandardBlock(Block block) {
        handleBlockContent(block);

        // indentation mark & block start
        FormatToken formatToken = getPreviousToken(getStart(block), JsTokenId.BRACKET_LEFT_CURLY, true);
        if (formatToken != null && !isScript(block)) {
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_BLOCK_START));
        }

        // put indentation mark after non white token preceeding curly bracket
        formatToken = getPreviousNonWhiteToken(getFinish(block) - 1,
                getStart(block), JsTokenId.BRACKET_RIGHT_CURLY, true);
        if (formatToken != null && !isScript(block)) {
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
        }
    }

    private void handleCaseBlock(Block block) {
        handleBlockContent(block);

        // indentation mark & block start
        FormatToken formatToken = getPreviousToken(getStart(block), JsTokenId.OPERATOR_COLON, true);
        if (formatToken != null) {
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
        }

        // put indentation mark
        formatToken = getCaseBlockEndToken(block);
        if (formatToken != null) {
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
        }
    }

    private void handleVirtualBlock(Block block) {
        handleVirtualBlock(block, FormatToken.Kind.INDENTATION_INC, FormatToken.Kind.INDENTATION_DEC,
                FormatToken.Kind.AFTER_BLOCK_START);
    }

    private void handleVirtualBlock(Block block, FormatToken.Kind afterBlock) {
        handleVirtualBlock(block, FormatToken.Kind.INDENTATION_INC, FormatToken.Kind.INDENTATION_DEC,
                afterBlock);
    }

    private void handleVirtualBlock(Block block, FormatToken.Kind indentationInc,
            FormatToken.Kind indentationDec, FormatToken.Kind afterBlock) {

        assert block.getStart() == block.getFinish() && block.getStatements().size() <= 1;

        if (block.getStatements().isEmpty()) {
            return;
        }

        handleBlockContent(block);

        Node statement = block.getStatements().get(0);
        
        // indentation mark & block start
        Token token = getPreviousNonEmptyToken(getStart(statement));
        
        /*
         * If its VarNode it does not contain var keyword so we have to search
         * for it.
         */
        if (statement instanceof VarNode && token.id() == JsTokenId.KEYWORD_VAR) {
            token = getPreviousNonEmptyToken(ts.offset());
        }
        
        if (token != null) {
            FormatToken formatToken = tokenStream.getToken(ts.offset());
            if (!isScript(block)) {
                if (formatToken == null && ts.offset() <= formatFinish) {
                    formatToken = tokenStream.getTokens().get(0);
                }
                if (formatToken != null) {
                    appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(indentationInc));
                    if (afterBlock != null) {
                        appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(afterBlock));
                    }
                }
            }
        }

        // put indentation mark after non white token
        int finish = getFinish(statement);
        // empty statement has start == finish
        FormatToken formatToken = getPreviousToken(
                statement.getStart() < finish ? finish - 1 : finish, null, true);
        if (formatToken != null && !isScript(block)) {
            if (formatToken != null) {
                appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(indentationDec));
            }
        }
    }

    private void handleBlockContent(Block block) {
        // functions
        if (block instanceof FunctionNode) {
            for (FunctionNode function : ((FunctionNode) block).getFunctions()) {
                function.accept(this);
            }
        }

        // statements
        List<Node> statements = block.getStatements();
        for (int i = 0; i < statements.size(); i++) {
            Node statement = statements.get(i);
            statement.accept(this);

            int start = getStart(statement);
            int finish = getFinish(statement);

            /*
             * What do we solve here? Unfortunately nashorn parses single
             * var statement as (possibly) multiple VarNodes. For example:
             * var a=1,b=2; is parsed to two VarNodes. The first covering a=1,
             * the second b=2. So we iterate subsequent VarNodes searching the
             * last one and the proper finish token.
             */
            if (statement instanceof VarNode) {
                int index = i + 1;
                Node lastVarNode = statement;

                while (i + 1 < statements.size()) {
                    Node next = statements.get(++i);
                    if (!(next instanceof VarNode)) {
                        i--;
                        break;
                    } else {
                        Token token = getPreviousNonEmptyToken(getStart(next));
                        if (token != null && JsTokenId.KEYWORD_VAR == token.id()) {
                            i--;
                            break;
                        }
                    }
                    lastVarNode = next;
                }

                assert lastVarNode instanceof VarNode;
                for (int j = index; j < i; j++) {
                    Node skipped = statements.get(j);
                    skipped.accept(this);
                }

                Token token = getNextNonEmptyToken(getFinish(lastVarNode) - 1);
                if (token != null && JsTokenId.OPERATOR_SEMICOLON == token.id()) {
                    finish = ts.offset() + 1;
                } else {
                    finish = getFinish(lastVarNode);
                }
            }

            // empty statement has start == finish
            FormatToken formatToken = getPreviousToken(start < finish ? finish - 1 : finish, null);
            if (formatToken != null) {
                appendTokenAfterLastVirtual(formatToken,
                        FormatToken.forFormat(FormatToken.Kind.AFTER_STATEMENT), true);
            }
        }
    }

    private void markSpacesWithinParentheses(SwitchNode node) {
        int leftStart = getStart(node);

        // the { has to be there for switch
        FormatToken token = getNextToken(leftStart, JsTokenId.BRACKET_LEFT_CURLY, getFinish(node));
        if (token != null) {
            markSpacesWithinParentheses(node, leftStart, token.getOffset(),
                    FormatToken.Kind.AFTER_SWITCH_PARENTHESIS, FormatToken.Kind.BEFORE_SWITCH_PARENTHESIS);
        }
    }

    private void markSpacesBeforeBrace(SwitchNode node) {
        int leftStart = getStart(node);

        // the { has to be there for switch
        FormatToken token = getNextToken(leftStart, JsTokenId.BRACKET_LEFT_CURLY, getFinish(node));
        if (token != null) {
            FormatToken previous = token.previous();
            if (previous != null) {
                appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_SWITCH_BRACE));
            }
        }
    }

    /**
     * Method putting formatting tokens for within parenthesis rule. Note
     * that this method may be more secure as it can search for the left paren
     * from start of the node and for the right from the body of the node
     * avoiding possibly wrong offset of expressions/conditions.
     *
     * @param outerNode the node we are marking, such as if, while, with
     * @param leftStart from where to start search to the right for the left paren
     * @param rightStart from where to start search to the left for the right paren
     * @param leftMark where to stop searching for the left paren
     * @param rightMark where to stop searching for the right paren
     */
    private void markSpacesWithinParentheses(Node outerNode, int leftStart,
            int rightStart, FormatToken.Kind leftMark, FormatToken.Kind rightMark) {

        FormatToken leftParen = getNextToken(leftStart,
                JsTokenId.BRACKET_LEFT_PAREN, getFinish(outerNode));
        if (leftParen != null) {
            FormatToken mark = leftParen.next();
            assert mark.getKind() == FormatToken.Kind.AFTER_LEFT_PARENTHESIS;
            tokenStream.removeToken(mark);

            appendToken(leftParen, FormatToken.forFormat(leftMark));
            FormatToken rightParen = getPreviousToken(rightStart,
                    JsTokenId.BRACKET_RIGHT_PAREN, getStart(outerNode));
            if (rightParen != null) {
                FormatToken previous = rightParen.previous();
                assert previous.getKind() == FormatToken.Kind.BEFORE_RIGHT_PARENTHESIS;
                tokenStream.removeToken(previous);

                previous = rightParen.previous();
                if (previous != null) {
                    appendToken(previous, FormatToken.forFormat(rightMark));
                }
            }
        }
    }

    private void markSpacesBeforeBrace(Block block, FormatToken.Kind mark) {
        FormatToken brace = getPreviousToken(getStart(block), null,
                getStart(block) - 1);
        if (brace != null) {
            FormatToken previous = brace.previous();
            if (previous != null) {
                appendToken(previous, FormatToken.forFormat(mark));
            }
        }
    }

    private FormatToken getNextToken(int offset, JsTokenId expected) {
        return getToken(offset, expected, false, false, null);
    }

    private FormatToken getNextToken(int offset, JsTokenId expected, int stop) {
        return getToken(offset, expected, false, false, stop);
    }

    private FormatToken getNextToken(int offset, JsTokenId expected, boolean startFallback) {
        return getToken(offset, expected, false, startFallback, null);
    }

    private FormatToken getPreviousToken(int offset, JsTokenId expected) {
        return getPreviousToken(offset, expected, false);
    }

    private FormatToken getPreviousToken(int offset, JsTokenId expected, int stop) {
        return getToken(offset, expected, true, false, stop);
    }

    private FormatToken getPreviousToken(int offset, JsTokenId expected, boolean startFallback) {
        return getToken(offset, expected, true, startFallback, null);
    }

    private FormatToken getToken(int offset, JsTokenId expected, boolean backward,
            boolean startFallback, Integer stopMark) {

        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }

        Token<? extends JsTokenId> token = ts.token();
        if (expected != null) {
            while (expected != token.id()
                    && (stopMark == null || ((stopMark >= ts.offset() && !backward) || (stopMark <=ts.offset() && backward)))
                    && ((backward && ts.movePrevious()) || (!backward && ts.moveNext()))) {
                token = ts.token();
            }
            if (expected != token.id()) {
                return null;
            }
        }
        if (token != null) {
            return getFallback(ts.offset(), startFallback);
        }
        return null;
    }

    private Token getPreviousNonEmptyToken(int offset) {
        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }

        Token ret = null;
        while (ts.movePrevious()) {
            Token token = ts.token();
            if ((token.id() != JsTokenId.BLOCK_COMMENT && token.id() != JsTokenId.DOC_COMMENT
                && token.id() != JsTokenId.LINE_COMMENT && token.id() != JsTokenId.EOL
                && token.id() != JsTokenId.WHITESPACE)) {
                ret = token;
                break;
            }
        }
        return ret;
    }

    private Token getNextNonEmptyToken(int offset) {
        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }

        Token ret = null;
        while (ts.moveNext()) {
            Token token = ts.token();
            if ((token.id() != JsTokenId.BLOCK_COMMENT && token.id() != JsTokenId.DOC_COMMENT
                && token.id() != JsTokenId.LINE_COMMENT && token.id() != JsTokenId.EOL
                && token.id() != JsTokenId.WHITESPACE)) {
                ret = token;
                break;
            }
        }
        return ret;
    }

    private FormatToken getPreviousNonWhiteToken(int offset, int stop, JsTokenId expected, boolean startFallback) {
        assert stop <= offset;
        FormatToken ret = getPreviousToken(offset, expected, startFallback);
        if (startFallback && ret != null && ret.getKind() == FormatToken.Kind.SOURCE_START) {
            return ret;
        }

        if (ret != null) {
            if (expected == null) {
                return ret;
            }

            Token token = null;
            while (ts.movePrevious() && ts.offset() >= stop) {
                Token current = ts.token();
                if (current.id() != JsTokenId.WHITESPACE) {
                    token = current;
                    break;
                }
            }

            if (token != null) {
                return getFallback(ts.offset(), startFallback);
            }
        }
        return null;
    }

    /**
     * Finds the next non empty token first and then move back to non whitespace
     * token.
     *
     * @param block case block
     * @return format token
     */
    private FormatToken getCaseBlockEndToken(Block block) {
        int start = getStart(block);
        int finish = getFinish(block) - 1;
        ts.move(finish);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }

        Token ret = null;
        while (ts.moveNext()) {
            Token token = ts.token();
            if ((token.id() != JsTokenId.BLOCK_COMMENT && token.id() != JsTokenId.DOC_COMMENT
                && token.id() != JsTokenId.LINE_COMMENT && token.id() != JsTokenId.EOL
                && token.id() != JsTokenId.WHITESPACE)) {
                ret = token;
                break;
            }
        }

        if (ret != null) {
            while (ts.movePrevious() && ts.offset() >= start) {
                Token current = ts.token();
                if (current.id() != JsTokenId.WHITESPACE) {
                    ret = current;
                    break;
                }
            }

            if (ret != null) {
                return getFallback(ts.offset(), true);
            }
        }
        return null;
    }

    private FormatToken getFallback(int offset, boolean fallback) {
        FormatToken ret = tokenStream.getToken(offset);
        if (ret == null && fallback && offset < formatFinish) {
            ret = tokenStream.getTokens().get(0);
            assert ret != null && ret.getKind() == FormatToken.Kind.SOURCE_START;
        }
        return ret;
    }

    private static int getStart(Node node) {
        // unfortunately in binary node the token represents operator
        // so string fix would not work
        if (node instanceof BinaryNode) {
            return getStart((BinaryNode) node);
        }
        // All this magic is because nashorn nodes and tokens don't contain the
        // quotes for string. Due to this we call this method to add 1 to start
        // in case it is string literal.
        int start = node.getStart();
        long firstToken = node.getToken();
        TokenType type = com.oracle.nashorn.parser.Token.descType(firstToken);
        if (type.equals(TokenType.STRING) || type.equals(TokenType.ESCSTRING)) {
            start--;
        }

        return start;
    }

    private static int getStart(BinaryNode node) {
        return getStart(node.lhs());
    }

    private static int getFunctionStart(FunctionNode node) {
        return com.oracle.nashorn.parser.Token.descPosition(node.getFirstToken());
    }

    private int getFinish(Node node) {
        // we are fixing the wrong finish offset here
        // only function node has last token
        if (node instanceof FunctionNode) {
            FunctionNode function = (FunctionNode) node;
            if (node.getStart() == node.getFinish()) {
                long lastToken = function.getLastToken();
                int finish = node.getStart() + com.oracle.nashorn.parser.Token.descPosition(lastToken)
                        + com.oracle.nashorn.parser.Token.descLength(lastToken);
                // check if it is a string
                if (com.oracle.nashorn.parser.Token.descType(lastToken).equals(TokenType.STRING)) {
                    finish++;
                }
                return finish;
            } else {
                return node.getFinish();
            }
        }

        // All this magic is because nashorn nodes and tokens don't contain the
        // quotes for string. Due to this we call this method to add 1 to finish
        // in case it is string literal.
        int finish = node.getFinish();
        ts.move(finish);
        if(!ts.moveNext()) {
            return finish;
        }
        Token<? extends JsTokenId> token = ts.token();
        if (token.id() == JsTokenId.STRING_END) {
            return finish + 1;
        }

        return finish;
    }

    private boolean isScript(Node node) {
        return (node instanceof FunctionNode)
                && ((FunctionNode) node).getKind() == FunctionNode.Kind.SCRIPT;
    }

    private static void appendTokenAfterLastVirtual(FormatToken previous,
            FormatToken token) {
        appendTokenAfterLastVirtual(previous, token, false);
    }

    private static void appendTokenAfterLastVirtual(FormatToken previous,
            FormatToken token, boolean checkDuplicity) {

        FormatToken current = previous;
        while (current.next() != null && current.next().isVirtual()) {
            current = current.next();
        }
        if (!checkDuplicity || !current.isVirtual() || !token.isVirtual()
                || current.getKind() != token.getKind()) {
            appendToken(current, token);
        }
    }

    private static void appendToken(FormatToken previous, FormatToken token) {
        FormatToken original = previous.next();
        previous.setNext(token);
        token.setPrevious(previous);
        token.setNext(original);
        if (original != null) {
            original.setPrevious(token);
        }
    }

}
