/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor;

import com.oracle.js.parser.ir.Block;
import com.oracle.js.parser.ir.CallNode;
import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.LexicalContext;
import com.oracle.js.parser.ir.LiteralNode;
import com.oracle.js.parser.ir.Node;
import com.oracle.js.parser.ir.VarNode;
import com.oracle.js.parser.ir.visitor.NodeVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.api.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;

/**
 *
 * @author Petr Pisl
 */
class JsKeyStrokeHandler implements KeystrokeHandler {

    public JsKeyStrokeHandler() {
    }

    @Override
    public boolean beforeCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    public boolean afterCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    public boolean charBackspaced(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    public int beforeBreak(Document doc, int caretOffset, JTextComponent target) throws BadLocationException {
        return -1;
    }

    @Override
    public OffsetRange findMatching(Document doc, int caretOffset) {
        return OffsetRange.NONE;
    }

    @Override
    public List<OffsetRange> findLogicalRanges(final ParserResult info, final int caretOffset) {
        final Set<OffsetRange> ranges = new LinkedHashSet();
        if (info instanceof JsParserResult) {
            final JsParserResult jsParserResult = (JsParserResult) info;
            FunctionNode root = jsParserResult.getRoot();
            final TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(jsParserResult.getSnapshot(), caretOffset);
            if (root != null && ts != null) {

                root.accept(new NodeVisitor(new LexicalContext()) {

                    final HashSet<String> referencedFunction = new HashSet();

                    @Override
                    protected boolean enterDefault(Node node) {
                        if (node != null && node.getStart() <= caretOffset && caretOffset <= node.getFinish()) {
                            ranges.add(new OffsetRange(node.getStart(), node.getFinish()));
                            return super.enterDefault(node);
                        }
                        return false;
                    }

                    @Override
                    public boolean enterFunctionNode(FunctionNode node) {
                        if (node.isProgram()) {
                            ranges.add(new OffsetRange(0, jsParserResult.getSnapshot().getText().length()));
                            if (node.getStart() <= caretOffset && caretOffset <= node.getFinish()) {
                                ranges.add(new OffsetRange(node.getStart(), node.getFinish()));
                            }
                            processFunction(node);
                            return false;
                        }
                        ts.move(node.getStart());
                        Token<? extends JsTokenId> token = LexUtilities.findPreviousIncluding(ts, Arrays.asList(JsTokenId.KEYWORD_FUNCTION));
                        if (token != null && ts.offset() <= caretOffset && caretOffset <= node.getFinish()) {
                            ranges.add(new OffsetRange(ts.offset(), node.getFinish()));
                            int firstParamOffset = node.getFinish();
                            int lastParamOffset = -1;
                            for (Node param : node.getParameters()) {
                                if (param.getStart() < firstParamOffset) {
                                    firstParamOffset = param.getStart();
                                }
                                if (param.getFinish() > lastParamOffset) {
                                    lastParamOffset = param.getFinish();
                                }
                            }
                            if (node.getParameters().size() > 1 && firstParamOffset < lastParamOffset && firstParamOffset <= caretOffset && caretOffset <= lastParamOffset) {
                                ranges.add(new OffsetRange(firstParamOffset, lastParamOffset));
                                for (Node param : node.getParameters()) {
                                    param.accept(this);
                                }
                            }
                            if (node.getStart() <= caretOffset && caretOffset <= node.getFinish()) {
                                ranges.add(new OffsetRange(node.getStart(), node.getFinish()));
                                processFunction(node);
                            }
                        }
                        return false;
                    }

                    private void processFunction(FunctionNode node) {
                        Block body = node.getBody();
                        for (Node statement : body.getStatements()) {
                            statement.accept(this);
                        }
// TRUFFLE                        
//                        for (Node declaration : node.getDeclarations()) {
//                            declaration.accept(this);
//                        }
//                        for (FunctionNode function : node.getFunctions()) {
//                            if (!referencedFunction.contains(function.getName())) {
//                                function.accept(this);
//                            }
//                        }
                    }

                    // TRUFFLE
//                    @Override
//                    public Node enter(ReferenceNode node) {
//                        FunctionNode fun = node.getReference();
//                        referencedFunction.add(fun.getName());
//                        fun.accept(this);
//                        return null;
//                    }

                    @Override
                    public boolean enterVarNode(VarNode node) {
                        ts.move(node.getStart());
                        Token<? extends JsTokenId> token = LexUtilities.findPreviousIncluding(ts, Arrays.asList(JsTokenId.KEYWORD_VAR));
                        if (token != null && ts.offset() <= caretOffset && caretOffset <= node.getFinish()) {
                            ranges.add(new OffsetRange(ts.offset(), node.getFinish()));
                            return enterDefault(node);
                        }
                        return false;
                    }

                    @Override
                    public boolean enterLiteralNode(LiteralNode node) {
                        if (node.isString() && node.getStart() <= caretOffset && caretOffset <= node.getFinish()) {
                            // include the " or '
                            ranges.add(new OffsetRange(node.getStart() - 1, node.getFinish() + 1));
                        }
                        return super.enterLiteralNode(node);
                    }

                    @Override
                    public boolean enterCallNode(CallNode node) {
                        if (node.getArgs().size() > 1) {
                            if (node.getStart() <= caretOffset && caretOffset <= node.getFinish()) {
                                ranges.add(new OffsetRange(node.getStart(), node.getFinish()));
                                int firstArgOffset = node.getFinish();
                                int lastArgOffset = -1;
                                for (Node arg : node.getArgs()) {
                                    if (arg.getStart() < firstArgOffset) {
                                        firstArgOffset = arg.getStart();
                                        if (arg instanceof LiteralNode && ((LiteralNode)arg).isString()) {
                                            firstArgOffset--;
                                        }
                                    }
                                    if (arg.getFinish() > lastArgOffset) {
                                        lastArgOffset = arg.getFinish();
                                        if (arg instanceof LiteralNode && ((LiteralNode)arg).isString()) {
                                            lastArgOffset++;
                                        }
                                    }
                                }
                                if (firstArgOffset <= caretOffset && caretOffset <= lastArgOffset) {
                                    ranges.add(new OffsetRange(firstArgOffset, lastArgOffset));
                                }
                                for (Node arg : node.getArgs()) {
                                    arg.accept(this);
                                }
                            }
                            return false;
                        } else {
                            return super.enterCallNode(node);
                        }
                    }

                });
            }
        }

        final ArrayList<OffsetRange> retval = new ArrayList(ranges);
        Collections.reverse(retval);
        return retval;
    }

    @Override
    public int getNextWordOffset(Document doc, int caretOffset, boolean reverse) {
        return -1;
    }

}
