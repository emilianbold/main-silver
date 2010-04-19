/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */


package org.netbeans.modules.php.editor.indent;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.spi.lexer.MutableTextInput;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */
public class TokenFormatter {

    protected static String TEMPLATE_HANDLER_PROPERTY = "code-template-insert-handler";

    private static final Logger LOGGER = Logger.getLogger(TokenFormatter.class.getName());

//    private final Context context;

    public TokenFormatter() {
	
    }

    protected static class DocumentOptions {

	public int continualIndentSize;
	public int initialIndent;
	public int indentSize;
	public int indentArrayItems;
	public int margin;
	public CodeStyle.BracePlacement classDeclBracePlacement;
	public CodeStyle.BracePlacement methodDeclBracePlacement;
	public CodeStyle.BracePlacement ifBracePlacement;
	public CodeStyle.BracePlacement forBracePlacement;
	public CodeStyle.BracePlacement whileBracePlacement;
	public CodeStyle.BracePlacement switchBracePlacement;
	public CodeStyle.BracePlacement catchBracePlacement;
	public CodeStyle.BracePlacement otherBracePlacement;

	public boolean spaceBeforeClassDeclLeftBrace;
	public boolean spaceBeforeMethodDeclLeftBrace;
	public boolean spaceBeforeIfLeftBrace;
	public boolean spaceBeforeElseLeftBrace;
	public boolean spaceBeforeWhileLeftBrace;
	public boolean spaceBeforeForLeftBrace;
	public boolean spaceBeforeDoLeftBrace;
	public boolean spaceBeforeSwitchLeftBrace;
	public boolean spaceBeforeTryLeftBrace;
	public boolean spaceBeforeCatchLeftBrace;

	public boolean spaceBeforeMethodDeclParen;
	public boolean spaceBeforeMethodCallParen;
	public boolean spaceBeforeIfParen;
	public boolean spaceBeforeForParen;
	public boolean spaceBeforeWhileParen;
	public boolean spaceBeforeCatchParen;
	public boolean spaceBeforeSwitchParen;
	public boolean spaceBeforeArrayDeclParen;

	public boolean spaceBeforeWhile;
	public boolean spaceBeforeElse;
	public boolean spaceBeforeCatch;

	public boolean spaceAroundObjectOp;
	public boolean spaceAroundStringConcatOp;
	public boolean spaceAroundUnaryOps;
	public boolean spaceAroundBinaryOps;
	public boolean spaceAroundTernaryOps;
	public boolean spaceAroundAssignOps;
	public boolean spaceAroundKeyValueOps;

	public boolean spaceWithinArrayDeclParens;
	public boolean spaceWithinMethodDeclParens;
	public boolean spaceWithinMethodCallParens;
	public boolean spaceWithinIfParens;
	public boolean spaceWithinForParens;
	public boolean spaceWithinWhileParens;
	public boolean spaceWithinSwitchParens;
	public boolean spaceWithinCatchParens;
	public boolean spaceWithinArrayBrackets;
	public boolean spaceWithinTypeCastParens;

	public boolean spaceBeforeComma;
	public boolean spaceAfterComma;
	public boolean spaceBeforeSemi;
	public boolean spaceAfterSemi;
	public boolean spaceAfterTypeCast;
	public boolean spaceBeforeClosePHPTag;

	public boolean placeElseOnNewLine;
	public boolean placeWhileOnNewLine;
	public boolean placeCatchOnNewLine;
	public boolean placeNewLineAfterModifiers;

	public int blankLinesBeforeNamespace;
	public int blankLinesAfterNamespace;
	public int blankLinesBeforeUse;
	public int blankLinesAfterUse;
	public int blankLinesBeforeClass;
	public int blankLinesBeforeClassEnd;
	public int blankLinesAfterClass;
	public int blankLinesAfterClassHeader;
	public int blankLinesBeforeField;
	public int blankLinesAfterField;
	public int blankLinesBeforeFunction;
	public int blankLinesAfterFunction;
	public int blankLinesBeforeFunctionEnd;
	public int blankLinesAfterOpenPHPTag;
	public int blankLinesAfterOpenPHPTagInHTML;
	public int blankLinesBeforeClosePHPTag;

	public CodeStyle.WrapStyle wrapExtendsImplementsKeyword;
	public CodeStyle.WrapStyle wrapExtendsImplementsList;
	public CodeStyle.WrapStyle wrapMethodParams;
	public CodeStyle.WrapStyle wrapMethodCallArgs;
	public CodeStyle.WrapStyle wrapChainedMethodCalls;
	public CodeStyle.WrapStyle wrapArrayInit;
	public CodeStyle.WrapStyle wrapFor;
	public CodeStyle.WrapStyle wrapForStatement;
	public CodeStyle.WrapStyle wrapIfStatement;
	public CodeStyle.WrapStyle wrapWhileStatement;
	public CodeStyle.WrapStyle wrapDoWhileStatement;
	public CodeStyle.WrapStyle wrapBinaryOps;
	public CodeStyle.WrapStyle wrapTernaryOps;
	public CodeStyle.WrapStyle wrapAssignOps;
        public boolean wrapBlockBrace;
        public boolean wrapStatementsOnTheSameLine;

	public boolean alignMultilineMethodParams;
	public boolean alignMultilineCallArgs;
	public boolean alignMultilineImplements;
	public boolean alignMultilineParenthesized;
	public boolean alignMultilineBinaryOp;
	public boolean alignMultilineTernaryOp;
	public boolean alignMultilineAssignment;
	public boolean alignMultilineFor;
	public boolean alignMultilineArrayInit;

	public DocumentOptions(BaseDocument doc) {
	    CodeStyle codeStyle = CodeStyle.get(doc);
	    continualIndentSize = codeStyle.getContinuationIndentSize();
	    initialIndent = codeStyle.getInitialIndent();
	    indentSize = codeStyle.getIndentSize();
	    indentArrayItems = codeStyle.getItemsInArrayDeclarationIndentSize();
	    margin = codeStyle.getRightMargin();

	    classDeclBracePlacement = codeStyle.getClassDeclBracePlacement();
	    methodDeclBracePlacement = codeStyle.getMethodDeclBracePlacement();
	    ifBracePlacement = codeStyle.getIfBracePlacement();
	    forBracePlacement = codeStyle.getForBracePlacement();
	    whileBracePlacement = codeStyle.getWhileBracePlacement();
	    switchBracePlacement = codeStyle.getSwitchBracePlacement();
	    catchBracePlacement = codeStyle.getCatchBracePlacement();
	    otherBracePlacement = codeStyle.getOtherBracePlacement();

	    spaceBeforeClassDeclLeftBrace = codeStyle.spaceBeforeClassDeclLeftBrace();
	    spaceBeforeMethodDeclLeftBrace = codeStyle.spaceBeforeMethodDeclLeftBrace();
	    spaceBeforeIfLeftBrace = codeStyle.spaceBeforeIfLeftBrace();
	    spaceBeforeElseLeftBrace = codeStyle.spaceBeforeElseLeftBrace();
	    spaceBeforeWhileLeftBrace = codeStyle.spaceBeforeWhileLeftBrace();
	    spaceBeforeForLeftBrace = codeStyle.spaceBeforeForLeftBrace();
	    spaceBeforeDoLeftBrace = codeStyle.spaceBeforeDoLeftBrace();
	    spaceBeforeSwitchLeftBrace = codeStyle.spaceBeforeSwitchLeftBrace();
	    spaceBeforeTryLeftBrace = codeStyle.spaceBeforeTryLeftBrace();
	    spaceBeforeCatchLeftBrace = codeStyle.spaceBeforeCatchLeftBrace();

	    spaceBeforeMethodDeclParen = codeStyle.spaceBeforeMethodDeclParen();
	    spaceBeforeMethodCallParen = codeStyle.spaceBeforeMethodCallParen();
	    spaceBeforeIfParen = codeStyle.spaceBeforeIfParen();
	    spaceBeforeForParen = codeStyle.spaceBeforeForParen();
	    spaceBeforeWhileParen = codeStyle.spaceBeforeWhileParen();
	    spaceBeforeCatchParen = codeStyle.spaceBeforeCatchParen();
	    spaceBeforeSwitchParen = codeStyle.spaceBeforeSwitchParen();
	    spaceBeforeArrayDeclParen = codeStyle.spaceBeforeArrayDeclParen();

	    spaceBeforeWhile = codeStyle.spaceBeforeWhile();
	    spaceBeforeElse = codeStyle.spaceBeforeElse();
	    spaceBeforeCatch = codeStyle.spaceBeforeCatch();

	    spaceAroundObjectOp = codeStyle.spaceAroundObjectOps();
	    spaceAroundStringConcatOp = codeStyle.spaceAroundStringConcatOps();
	    spaceAroundUnaryOps = codeStyle.spaceAroundUnaryOps();
	    spaceAroundBinaryOps = codeStyle.spaceAroundBinaryOps();
	    spaceAroundTernaryOps = codeStyle.spaceAroundTernaryOps();
	    spaceAroundAssignOps = codeStyle.spaceAroundAssignOps();
	    spaceAroundKeyValueOps = codeStyle.spaceAroundKeyValueOps();

	    spaceWithinArrayDeclParens = codeStyle.spaceWithinArrayDeclParens();
	    spaceWithinMethodDeclParens = codeStyle.spaceWithinMethodDeclParens();
	    spaceWithinMethodCallParens = codeStyle.spaceWithinMethodCallParens();
	    spaceWithinIfParens = codeStyle.spaceWithinIfParens();
	    spaceWithinForParens = codeStyle.spaceWithinForParens();
	    spaceWithinWhileParens = codeStyle.spaceWithinWhileParens();
	    spaceWithinSwitchParens = codeStyle.spaceWithinSwitchParens();
	    spaceWithinCatchParens = codeStyle.spaceWithinCatchParens();
	    spaceWithinArrayBrackets = codeStyle.spaceWithinArrayBrackets();
	    spaceWithinTypeCastParens = codeStyle.spaceWithinTypeCastParens();

	    spaceBeforeComma = codeStyle.spaceBeforeComma();
	    spaceAfterComma = codeStyle.spaceAfterComma();
	    spaceBeforeSemi = codeStyle.spaceBeforeSemi();
	    spaceAfterSemi = codeStyle.spaceAfterSemi();
	    spaceAfterTypeCast = codeStyle.spaceAfterTypeCast();
	    spaceBeforeClosePHPTag = codeStyle.spaceBeforeClosePHPTag();

	    placeElseOnNewLine = codeStyle.placeElseOnNewLine();
	    placeWhileOnNewLine = codeStyle.placeWhileOnNewLine();
	    placeCatchOnNewLine = codeStyle.placeCatchOnNewLine();
	    placeNewLineAfterModifiers = codeStyle.placeNewLineAfterModifiers();

	    blankLinesBeforeNamespace = codeStyle.getBlankLinesBeforeNamespace();
	    blankLinesAfterNamespace = codeStyle.getBlankLinesAfterNamespace();
	    blankLinesBeforeUse = codeStyle.getBlankLinesBeforeUse();
	    blankLinesAfterUse = codeStyle.getBlankLinesAfterUse();
	    blankLinesBeforeClass = codeStyle.getBlankLinesBeforeClass();
	    blankLinesBeforeClassEnd = codeStyle.getBlankLinesBeforeClassEnd();
	    blankLinesAfterClass = codeStyle.getBlankLinesAfterClass();
	    blankLinesAfterClassHeader = codeStyle.getBlankLinesAfterClassHeader();
	    blankLinesBeforeField = codeStyle.getBlankLinesBeforeField();
	    blankLinesAfterField = codeStyle.getBlankLinesAfterField();
	    blankLinesBeforeFunction = codeStyle.getBlankLinesBeforeFunction();
	    blankLinesAfterFunction = codeStyle.getBlankLinesAfterFunction();
	    blankLinesBeforeFunctionEnd = codeStyle.getBlankLinesBeforeFunctionEnd();
	    blankLinesAfterOpenPHPTag = codeStyle.getBlankLinesAfterOpenPHPTag();
	    blankLinesAfterOpenPHPTagInHTML = codeStyle.getBlankLinesAfterOpenPHPTagInHTML();
	    blankLinesBeforeClosePHPTag = codeStyle.getBlankLinesBeforeClosePHPTag();

	    wrapExtendsImplementsKeyword = codeStyle.wrapExtendsImplementsKeyword();
	    wrapExtendsImplementsList = codeStyle.wrapExtendsImplementsList();
	    wrapMethodParams = codeStyle.wrapMethodParams();
	    wrapMethodCallArgs = codeStyle.wrapMethodCallArgs();
	    wrapChainedMethodCalls = codeStyle.wrapChainedMethodCalls();
	    wrapArrayInit = codeStyle.wrapArrayInit();
	    wrapFor = codeStyle.wrapFor();
	    wrapForStatement = codeStyle.wrapForStatement();
	    wrapIfStatement = codeStyle.wrapIfStatement();
	    wrapWhileStatement = codeStyle.wrapWhileStatement();
	    wrapDoWhileStatement = codeStyle.wrapDoWhileStatement();
	    wrapBinaryOps = codeStyle.wrapBinaryOps();
	    wrapTernaryOps = codeStyle.wrapTernaryOps();
	    wrapAssignOps = codeStyle.wrapAssignOps();
            wrapBlockBrace = codeStyle.wrapBlockBrace();
            wrapStatementsOnTheSameLine = codeStyle.wrapStatementsOnTheSameLine();

	    alignMultilineMethodParams = codeStyle.alignMultilineMethodParams();
	    alignMultilineCallArgs = codeStyle.alignMultilineCallArgs();
	    alignMultilineImplements = codeStyle.alignMultilineImplements();
	    alignMultilineParenthesized = codeStyle.alignMultilineParenthesized();
	    alignMultilineBinaryOp = codeStyle.alignMultilineBinaryOp();
	    alignMultilineTernaryOp = codeStyle.alignMultilineTernaryOp();
	    alignMultilineAssignment = codeStyle.alignMultilineAssignment();
	    alignMultilineFor = codeStyle.alignMultilineFor();
	    alignMultilineArrayInit = codeStyle.alignMultilineArrayInit();
	}
    }

    /**
     *
     * @param chs
     * @return number of new lines in the input
     */
    private int countOfNewLines(CharSequence chs) {
	int count = 0;
	for (int i = 0; i < chs.length(); i++) {
	    if (chs.charAt(i) == '\n') { // NOI18N
		count++;
	    }
	}
	return count;
    }


    public void reformat(Context context, ParserResult info) {
	final AtomicLong start = new AtomicLong(System.currentTimeMillis());

	final Context formatContext = context;
	PHPParseResult phpParseResult = ((PHPParseResult) info);

	TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(context.document(), 0);

	final BaseDocument doc = (BaseDocument) context.document();
	FormatVisitor fv = new FormatVisitor(doc);
	phpParseResult.getProgram().accept(fv);
	final List<FormatToken> formatTokens = fv.getFormatTokens();

	if (LOGGER.isLoggable(Level.FINE)) {
	    long end = System.currentTimeMillis();
	    LOGGER.fine("Creating formating stream took: " + (end - start.get()));
	}

	final DocumentOptions docOptions = new DocumentOptions(doc);

	doc.runAtomic(new Runnable() {

	    @Override
	    public void run() {
		TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, 0);
		if (LOGGER.isLoggable(Level.FINE)) {
		    LOGGER.fine("Tokens in TS: " + ts.tokenCount());
		    LOGGER.fine("Format tokens: " + formatTokens.size());
		}
		MutableTextInput mti = (MutableTextInput) doc.getProperty(MutableTextInput.class);
                    try {
                    mti.tokenHierarchyControl().setActive(false);

                    start.set(System.currentTimeMillis());

                    int delta = 0;
                    int indent = docOptions.initialIndent;
                    final boolean templateEdit = doc.getProperty(TEMPLATE_HANDLER_PROPERTY) != null; //NOI18N
                    final int caretOffset = EditorRegistry.lastFocusedComponent() != null
                            ? EditorRegistry.lastFocusedComponent().getCaretPosition()
                            : 0;
                    boolean caretInTemplateSolved = false;
                    int lastPHPIndent = 0;
                    int htmlIndent = -1;
                    int index = 0;
                    int newLines = 0;
                    int countSpaces = 0;
                    int column = 0;
                    int indentOfOpenTag = 0;


                    FormatToken formatToken;
                    String newText = null;
                    String oldText = null;
                    int changeOffset = -1;
                    FormatToken.AnchorToken lastAnchor = null;

                    while (index < formatTokens.size()) {
                        formatToken = formatTokens.get(index);
                        oldText = null;						//NOI18N
                        if (formatToken.isWhitespace()) {
                            newLines = -1;
                            countSpaces = 0;

                            boolean wasARule = false;
                            boolean indentLine = false;
                            boolean indentRule = false;
                            boolean afterSemi = false;
                            boolean wsBetweenBraces = false;

                            changeOffset = formatToken.getOffset();

                            while (index < formatTokens.size() && (formatToken.isWhitespace()
                                    || formatToken.getId() == FormatToken.Kind.INDENT
                                    || formatToken.getId() == FormatToken.Kind.ANCHOR)) {
                                if (oldText == null && formatToken.getOldText() != null) {
                                    oldText = formatToken.getOldText();
                                }
                                if (formatToken.getId() != FormatToken.Kind.INDENT
                                        && formatToken.getId() != FormatToken.Kind.WHITESPACE_INDENT
                                        && formatToken.getId() != FormatToken.Kind.ANCHOR
                                        && formatToken.getId() != FormatToken.Kind.WHITESPACE) {
                                    wasARule = true;
                                }
                                switch (formatToken.getId()) {
                                    case WHITESPACE:
                                        break;
                                    case WHITESPACE_BEFORE_CLASS_LEFT_BRACE:
                                        indentRule = true;
                                        Whitespace ws = countWhiteSpaceBeforeLeftBrace(docOptions.classDeclBracePlacement, docOptions.spaceBeforeClassDeclLeftBrace, oldText, indent);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_FUNCTION_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(docOptions.methodDeclBracePlacement, docOptions.spaceBeforeMethodDeclLeftBrace, oldText, indent);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_IF_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(docOptions.ifBracePlacement, docOptions.spaceBeforeIfLeftBrace, oldText, indent);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_FOR_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(docOptions.forBracePlacement, docOptions.spaceBeforeForLeftBrace, oldText, indent);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_WHILE_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(docOptions.whileBracePlacement, docOptions.spaceBeforeWhileLeftBrace, oldText, indent);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_SWITCH_LEFT_BACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(docOptions.switchBracePlacement, docOptions.spaceBeforeSwitchLeftBrace, oldText, indent);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_CATCH_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(docOptions.catchBracePlacement, docOptions.spaceBeforeCatchLeftBrace, oldText, indent);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_OTHER_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(docOptions.otherBracePlacement, docOptions.spaceBeforeTryLeftBrace, oldText, indent);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_AFTER_OTHER_LEFT_BRACE:
                                        indentRule = docOptions.wrapBlockBrace;
                                        int wrap = docOptions.wrapBlockBrace ? 1 : 0;
                                        newLines = countLinesAfter(index);
                                        newLines = wrap > newLines ? wrap : newLines;
                                        countSpaces = newLines > 0 ? indent :  1 ;
                                        break;
                                    case WHITESPACE_BEFORE_IF_RIGHT_BRACE:
                                        indentLine = indentRule =  oldText != null && countOfNewLines(oldText) > 0 ? true : docOptions.wrapBlockBrace;
                                        ws = countWhiteSpaceBeforeRightBrace(docOptions.ifBracePlacement, newLines, 0, indent, index - 1);
                                        newLines = ws.lines;
                                        countSpaces = indentRule ? ws.spaces : 1;
                                        break;
                                    case WHITESPACE_BEFORE_FOR_RIGHT_BRACE:
                                        indentLine = indentRule = oldText != null && countOfNewLines(oldText) > 0 ? true : docOptions.wrapBlockBrace;
                                        ws = countWhiteSpaceBeforeRightBrace(docOptions.forBracePlacement, newLines, 0, indent, index - 1);
                                        newLines = ws.lines;
                                        countSpaces = indentRule ? ws.spaces : 1;
                                        break;
                                    case WHITESPACE_BEFORE_WHILE_RIGHT_BRACE:
                                        indentLine = indentRule = oldText != null && countOfNewLines(oldText) > 0 ? true : docOptions.wrapBlockBrace;
                                        ws = countWhiteSpaceBeforeRightBrace(docOptions.whileBracePlacement, newLines, 0, indent, index - 1);
                                        newLines = ws.lines;
                                        countSpaces = indentRule ? ws.spaces : 1;
                                        break;
                                    case WHITESPACE_BEFORE_SWITCH_RIGHT_BACE:
                                        indentLine = indentRule = oldText != null && countOfNewLines(oldText) > 0 ? true : docOptions.wrapBlockBrace;
                                        ws = countWhiteSpaceBeforeRightBrace(docOptions.switchBracePlacement, newLines, 0, indent, index - 1);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_CATCH_RIGHT_BRACE:
                                        indentLine = indentRule = oldText != null && countOfNewLines(oldText) > 0 ? true : docOptions.wrapBlockBrace;
                                        ws = countWhiteSpaceBeforeRightBrace(docOptions.catchBracePlacement, newLines, 0, indent, index - 1);
                                        newLines = ws.lines;
                                        countSpaces = indentRule ? ws.spaces : 1;
                                        break;
                                    case WHITESPACE_BEFORE_OTHER_RIGHT_BRACE:
                                        indentLine = indentRule = oldText != null && countOfNewLines(oldText) > 0 ? true : docOptions.wrapBlockBrace;
                                        ws = countWhiteSpaceBeforeRightBrace(docOptions.otherBracePlacement, newLines, 0, indent, index - 1);
                                        newLines = ws.lines;
                                        countSpaces = indentRule ? ws.spaces : 1;
                                        break;
                                    case WHITESPACE_BETWEEN_OPEN_CLOSE_BRACES:
                                        wsBetweenBraces = true;
                                        break;
                                    case WHITESPACE_BEFORE_CLASS:
                                        indentRule = true;
                                        newLines = docOptions.blankLinesBeforeClass + 1 > newLines ? docOptions.blankLinesBeforeClass + 1 : newLines;
                                        countSpaces = indent;
                                        break;
                                    case WHITESPACE_AFTER_CLASS_LEFT_BRACE:
                                        indentRule = true;
                                        newLines = docOptions.blankLinesAfterClassHeader + 1 > newLines ? docOptions.blankLinesAfterClassHeader + 1 : newLines;
                                        countSpaces = indent;
                                        break;
                                    case WHITESPACE_AFTER_CLASS:
                                        indentRule = true;
                                        newLines = docOptions.blankLinesAfterClass + 1 > newLines ? docOptions.blankLinesAfterClass + 1 : newLines;
                                        break;
                                    case WHITESPACE_BEFORE_CLASS_RIGHT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeRightBrace(docOptions.classDeclBracePlacement, newLines, docOptions.blankLinesBeforeClassEnd, indent, index - 1);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_FUNCTION:
                                        indentRule = true;
                                        newLines = docOptions.blankLinesBeforeFunction + 1 > newLines ? docOptions.blankLinesBeforeFunction + 1 : newLines;
                                        countSpaces = indent;
                                        break;
                                    case WHITESPACE_AFTER_FUNCTION:
                                        newLines = docOptions.blankLinesAfterFunction + 1 > newLines ? docOptions.blankLinesAfterFunction + 1 : newLines;
                                        break;
                                    case WHITESPACE_BEFORE_FUNCTION_RIGHT_BRACE:
                                        indentLine = indentRule = oldText != null && countOfNewLines(oldText) > 0 ? true : docOptions.wrapBlockBrace;
                                        ws = countWhiteSpaceBeforeRightBrace(docOptions.methodDeclBracePlacement, newLines, docOptions.blankLinesBeforeFunctionEnd, indent, index - 1);
                                        newLines = ws.lines;
                                        countSpaces = indentRule ? ws.spaces : 1;
                                        break;
                                    case WHITESPACE_BEFORE_FIELD:
                                        newLines = docOptions.blankLinesBeforeField + 1 > newLines ? docOptions.blankLinesBeforeField + 1 : newLines;
                                        break;
                                    case WHITESPACE_BETWEEN_FIELDS:
                                        indentRule = true;
                                        newLines = 1;
                                        countSpaces = indent;
                                        break;
                                    case WHITESPACE_BEFORE_NAMESPACE:
                                        indentRule = true;
                                        newLines = docOptions.blankLinesBeforeNamespace + 1;
                                        break;
                                    case WHITESPACE_AFTER_NAMESPACE:
                                        indentRule = true;
                                        newLines = docOptions.blankLinesAfterNamespace + 1;
                                        break;
                                    case WHITESPACE_BEFORE_USE:
                                        indentRule = true;
                                        newLines = docOptions.blankLinesBeforeUse + 1;
                                        break;
                                    case WHITESPACE_BETWEEN_USE:
                                        indentRule = true;
                                        newLines = 1;
                                        break;
                                    case WHITESPACE_AFTER_USE:
                                        indentRule = true;
                                        newLines = docOptions.blankLinesAfterUse + 1;
                                        break;
                                    case WHITESPACE_BEFORE_EXTENDS_IMPLEMENTS:
                                        indentRule = true;
                                        switch (docOptions.wrapExtendsImplementsKeyword) {
                                            case WRAP_ALWAYS:
                                                newLines = 1;
                                                countSpaces = docOptions.continualIndentSize;
                                                break;
                                            case WRAP_NEVER:
                                                newLines = 0;
                                                countSpaces = 1;
                                                break;
                                            case WRAP_IF_LONG:
                                                if (column + 1 + countLengthOfNextSequence(index + 1) > docOptions.margin) {
                                                    newLines = 1;
                                                    countSpaces = docOptions.continualIndentSize;
                                                }
                                                else {
                                                    newLines = 0;
                                                    countSpaces = 1;
                                                }
                                                break;
                                        }
                                        break;
                                    case WHITESPACE_IN_INTERFACE_LIST:
                                        indentRule = true;
                                        switch (docOptions.wrapExtendsImplementsList) {
                                            case WRAP_ALWAYS:
                                                newLines = 1;
                                                countSpaces = docOptions.alignMultilineImplements ? lastAnchor.getAnchorColumn() : docOptions.continualIndentSize;
                                                break;
                                            case WRAP_NEVER:
                                                newLines = 0;
                                                countSpaces = 1;
                                                break;
                                            case WRAP_IF_LONG:
                                                if (column + 1 + countUnbreakableTextAfter(index + 1) > docOptions.margin) {
                                                    newLines = 1;
                                                    countSpaces = docOptions.continualIndentSize;
                                                    }
                                                else {
                                                    newLines = 0;
                                                    countSpaces = 1;
                                                }
                                                break;
                                        }
                                        break;
                                    case WHITESPACE_IN_PARAMETER_LIST:
                                        indentRule = true;
                                        switch (docOptions.wrapMethodParams) {
                                            case WRAP_ALWAYS:
                                                newLines = 1;
                                                countSpaces = docOptions.alignMultilineMethodParams ? lastAnchor.getAnchorColumn() : indent + docOptions.continualIndentSize;
                                                break;
                                            case WRAP_NEVER:
                                                if (isAfterLineComment(formatTokens, index)) {
                                                    newLines = 1;
                                                    countSpaces = docOptions.alignMultilineMethodParams ? lastAnchor.getAnchorColumn() : indent + docOptions.continualIndentSize;
                                                } else {
                                                    newLines = 0;
                                                    countSpaces = docOptions.spaceAfterComma ? 1 : 0;
                                                }
                                                break;
                                            case WRAP_IF_LONG:
                                                if (column + 1 + countLengthOfNextSequence(index + 1) > docOptions.margin) {
                                                    newLines = 1;
                                                    countSpaces = docOptions.alignMultilineMethodParams ? lastAnchor.getAnchorColumn() : indent + docOptions.continualIndentSize;
                                                }
                                                else {
                                                    newLines = 0;
                                                    countSpaces = 1;
                                                }
                                                break;
                                        }
                                        break;
                                    case WHITESPACE_AROUND_OBJECT_OP:
                                        countSpaces = docOptions.spaceAroundObjectOp ? 1 : 0;
                                        break;
                                    case WHITESPACE_AROUND_CONCAT_OP:
                                        countSpaces = docOptions.spaceAroundStringConcatOp ? 1 : 0;
                                        break;
                                    case WHITESPACE_AROUND_UNARY_OP:
                                        countSpaces = docOptions.spaceAroundUnaryOps ? 1 : countSpaces;
                                        break;
                                    case WHITESPACE_AROUND_BINARY_OP:
                                        countSpaces = docOptions.spaceAroundBinaryOps ? 1 : 0;
                                        break;
                                    case WHITESPACE_AROUND_TERNARY_OP:
                                        countSpaces = docOptions.spaceAroundTernaryOps ? 1 : 0;
                                        break;
                                    case WHITESPACE_AROUND_ASSIGN_OP:
                                        countSpaces = docOptions.spaceAroundAssignOps ? 1 : 0;
                                        break;
                                    case WHITESPACE_AROUND_KEY_VALUE_OP:
                                        countSpaces = docOptions.spaceAroundKeyValueOps ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_METHOD_DEC_PAREN:
                                        countSpaces = docOptions.spaceBeforeMethodDeclParen ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_METHOD_CALL_PAREN:
                                        countSpaces = docOptions.spaceBeforeMethodCallParen ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_IF_PAREN:
                                        countSpaces = docOptions.spaceBeforeIfParen ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_FOR_PAREN:
                                        countSpaces = docOptions.spaceBeforeForParen ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_WHILE_PAREN:
                                        countSpaces = docOptions.spaceBeforeWhileParen ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_CATCH_PAREN:
                                        countSpaces = docOptions.spaceBeforeCatchParen ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_SWITCH_PAREN:
                                        countSpaces = docOptions.spaceBeforeSwitchParen ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_ARRAY_DECL_PAREN:
                                        countSpaces = docOptions.spaceBeforeArrayDeclParen ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_COMMA:
                                        countSpaces = docOptions.spaceBeforeComma ? 1 : 0;
                                        break;
                                    case WHITESPACE_AFTER_COMMA:
                                        countSpaces = docOptions.spaceAfterComma ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_SEMI:
                                        countSpaces = docOptions.spaceBeforeSemi ? 1 : 0;
                                        break;
                                    case WHITESPACE_AFTER_SEMI:
//                                        countSpaces = docOptions.spaceAfterSemi ? 1 : 0;
                                        afterSemi = true;
                                        break;
                                    case WHITESPACE_WITHIN_ARRAY_DECL_PARENS:
                                        int hIndex = index - 1;
                                        FormatToken token ;
                                        do {
                                            token = formatTokens.get(hIndex);
                                            hIndex--;

                                        } while (token.getId() != FormatToken.Kind.WHITESPACE_INDENT
                                                && token.getId() != FormatToken.Kind.TEXT
                                                && hIndex > 0);
                                        if (token.getId() == FormatToken.Kind.WHITESPACE_INDENT) {
                                            countSpaces = indent;
                                        }
                                        else {
                                            countSpaces = docOptions.spaceWithinArrayDeclParens ? 1 : 0;
                                        }
                                        break;
                                    case WHITESPACE_WITHIN_METHOD_DECL_PARENS:
                                        countSpaces = docOptions.spaceWithinMethodDeclParens ? 1 : 0;
                                        break;
                                    case WHITESPACE_WITHIN_METHOD_CALL_PARENS:
                                        countSpaces = docOptions.spaceWithinMethodCallParens ? 1 : 0;
                                        break;
                                    case WHITESPACE_WITHIN_IF_PARENS:
                                        countSpaces = docOptions.spaceWithinIfParens ? 1 : 0;
                                        break;
                                    case WHITESPACE_WITHIN_FOR_PARENS:
                                        countSpaces = docOptions.spaceWithinForParens ? 1 : 0;
                                        break;
                                    case WHITESPACE_WITHIN_WHILE_PARENS:
                                        countSpaces = docOptions.spaceWithinWhileParens ? 1 : 0;
                                        break;
                                    case WHITESPACE_WITHIN_SWITCH_PARENS:
                                        countSpaces = docOptions.spaceWithinSwitchParens ? 1 : 0;
                                        break;
                                    case WHITESPACE_WITHIN_CATCH_PARENS:
                                        countSpaces = docOptions.spaceWithinCatchParens ? 1 : 0;
                                        break;
                                    case WHITESPACE_WITHIN_ARRAY_BRACKETS_PARENS:
                                        countSpaces = docOptions.spaceWithinArrayBrackets ? 1 : 0;
                                        break;
                                    case WHITESPACE_WITHIN_TYPE_CAST_PARENS:
                                        countSpaces = docOptions.spaceWithinTypeCastParens ? 1 : 0;
                                        break;
                                    case WHITESPACE_AFTER_TYPE_CAST:
                                        countSpaces = docOptions.spaceAfterTypeCast ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_FOR_STATEMENT:
                                        indentRule = true;
                                        ws = countWSBeforeAStatement(docOptions.wrapForStatement, true, column, countLengthOfNextSequence(index + 1), indent);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_WHILE_STATEMENT:
                                        indentRule = true;
                                        ws = countWSBeforeAStatement(docOptions.wrapWhileStatement, true, column, countLengthOfNextSequence(index + 1), indent);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_DO_STATEMENT:
                                        indentRule = true;
                                        ws = countWSBeforeAStatement(docOptions.wrapDoWhileStatement, true, column, countLengthOfNextSequence(index + 1), indent);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_IF_ELSE_STATEMENT:
                                        indentRule = true;
                                        ws = countWSBeforeAStatement(docOptions.wrapIfStatement, true,  column, countLengthOfNextSequence(index + 1), indent);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_IN_FOR:
                                        indentRule = true;
                                        ws = countWSBeforeAStatement(docOptions.wrapFor, true, column, countLengthOfNextSequence(index + 1), indent);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_IN_TERNARY_OP:
                                        indentRule = true;
                                        ws = countWSBeforeAStatement(docOptions.wrapTernaryOps, docOptions.spaceAroundTernaryOps, column, countLengthOfNextSequence(index + 1), indent);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_IN_CHAINED_METHOD_CALLS:
                                        indentRule = true;
                                        switch (docOptions.wrapChainedMethodCalls) {
                                            case WRAP_ALWAYS:
                                                newLines = 1;
                                                countSpaces = indent + docOptions.continualIndentSize;
                                                break;
                                            case WRAP_NEVER:
                                                newLines = 0;
                                                countSpaces = 0;
                                                break;
                                            case WRAP_IF_LONG:
                                                if (column + 1 + countLengthOfNextSequence(index + 1) > docOptions.margin) {
                                                    newLines = 1;
                                                    countSpaces = indent + docOptions.continualIndentSize;
                                                    }
                                                else {
                                                    newLines = 0;
                                                    countSpaces = 1;
                                                }
                                                break;
                                        }
                                        break;
                                    case WHITESPACE_BETWEEN_LINE_COMMENTS:
                                        newLines = 1;
                                        break;
                                    case WHITESPACE_BEFORE_CATCH:
                                        indentRule = true;
                                        ws = countWSBeforeKeyword(docOptions.placeCatchOnNewLine, docOptions.spaceBeforeCatch, indent, index);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_WHILE:
                                        indentRule = true;
                                        ws = countWSBeforeKeyword(docOptions.placeWhileOnNewLine, docOptions.spaceBeforeWhile, indent, index);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_ELSE:
                                        indentRule = true;
                                        ws = countWSBeforeKeyword(docOptions.placeElseOnNewLine, docOptions.spaceBeforeElse, indent, index);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_INDENT:
                                        indentLine = true;
                                        break;
                                    case INDENT:
                                        indent += ((FormatToken.IndentToken) formatToken).getDelta();
                                        break;
                                    case ANCHOR:
                                        lastAnchor = (FormatToken.AnchorToken) formatToken;
                                        lastAnchor.setAnchorColumn(column + 1);
                                        break;
                                    case WHITESPACE_BEFORE_OPEN_PHP_TAG:
                                        Map<Integer, Integer> suggestedLineIndents = (Map<Integer, Integer>)doc.getProperty("AbstractIndenter.lineIndents");

                                        if (oldText == null) {
                                            try {
                                                int offset = formatToken.getOffset() + delta;
                                                int lineNumber = Utilities.getLineOffset(doc, offset);
                                                Integer suggestedIndent = suggestedLineIndents != null
                                                    ? suggestedLineIndents.get(lineNumber)
                                                    : new Integer(0);
                                                if (suggestedIndent == null) {
                                                    suggestedIndent = new Integer(0);
                                                }
                                                int lineOffset = Utilities.getRowStart(doc, offset);
                                                int firstNW = Utilities.getFirstNonWhiteFwd(doc, lineOffset);
                                                if (firstNW == offset) {
                                                    indentRule = true;
                                                    changeOffset = lineOffset - delta;
                                                    oldText = doc.getText(lineOffset, firstNW - lineOffset);
                                                    htmlIndent = suggestedIndent.intValue();
    //						if (indent < suggestedIndent.intValue()) {
                                                        indent = suggestedIndent.intValue() + docOptions.initialIndent;
    //						}
                                                    countSpaces = htmlIndent;
                                                    indentOfOpenTag = countSpaces;
                                                }
                                            } catch (BadLocationException ex) {
                                                Exceptions.printStackTrace(ex);
                                            }
                                        }
                                        break;
                                    case WHITESPACE_AFTER_OPEN_PHP_TAG:
                                        indentRule = true;
                                        if (!isOpenAndCloseTagOnOneLine(index)) {
                                            newLines = ((FormatToken.InitToken)formatTokens.get(0)).hasHTML()
                                                    ? docOptions.blankLinesAfterOpenPHPTagInHTML + 1
                                                    : docOptions.blankLinesAfterOpenPHPTag + 1;
                                            countSpaces = indent;
                                        }
                                        else {
                                            newLines = 0;
                                            countSpaces = 1;
                                        }
                                        break;
                                    case WHITESPACE_BEFORE_CLOSE_PHP_TAG:
                                        suggestedLineIndents = (Map<Integer, Integer>)doc.getProperty("AbstractIndenter.lineIndents");
                                        indentRule = true;
                                        if (suggestedLineIndents != null) {
                                            try {
                                                int offset = formatToken.getOffset() + delta;
                                                int lineNumber = Utilities.getLineOffset(doc, offset);
                                                Integer suggestedIndent = suggestedLineIndents.get(lineNumber);
                                                if (suggestedIndent != null) {
                                                    int lineOffset = Utilities.getRowStart(doc, offset);
                                                    int firstNW = Utilities.getFirstNonWhiteFwd(doc, lineOffset);
                                                    if (firstNW == offset) {
                                                        countSpaces = indentOfOpenTag;
                                                        newLines = docOptions.blankLinesBeforeClosePHPTag + 1;
                                                    }
                                                    else {
    //						    newLines = docOptions.blankLinesBeforeClosePHPTag + 1;
                                                        if (isAfterLineComment(formatTokens, index)) {
                                                            // there should be logic, which will remove whitespaces at the end of line comment in the case // comment ?>
                                                            countSpaces = 0;
                                                        } else {
                                                        countSpaces = docOptions.spaceBeforeClosePHPTag ? 1 : 0;
                                                        }
                                                    }
                                                    indent = suggestedIndent;
                                                } else {
                                                    if (!isCloseAndOpenTagOnOneLine(index)) {
                                                        newLines = docOptions.blankLinesBeforeClosePHPTag + 1;
                                                        countSpaces = indentOfOpenTag;
                                                    } else {
                                                        newLines = 0;
                                                        countSpaces = docOptions.spaceBeforeClosePHPTag ? 1 : 0;
                                                    }
                                                }
                                            } catch (BadLocationException ex) {
                                                Exceptions.printStackTrace(ex);
                                            }
                                        }
                                        else {
                                            if (!isCloseAndOpenTagOnOneLine(index)) {
                                                newLines = docOptions.blankLinesBeforeClosePHPTag + 1;
                                                countSpaces = indentOfOpenTag;
                                            } else {
                                                newLines = 0;
                                                countSpaces = docOptions.spaceBeforeClosePHPTag ? 1 : 0;
                                            }
                                        }
                                        lastPHPIndent = indent;
                                        break;
                                    case WHITESPACE_AFTER_CLOSE_PHP_TAG:
    //				    if (index < formatTokens.size() -1
    //					    && formatTokens.get(index + 1).getId() == FormatToken.Kind.TEXT) {
    //					String text = formatTokens.get(index + 1).getOldText();
    //					int removeLength = 0;
    //					int lastLine = 0;
    //					while (removeLength < text.length()
    //						&& (text.charAt(removeLength) == ' '
    //						|| text.charAt(removeLength) == '\n')
    //						|| text.charAt(removeLength) == '\t') {
    //					    if (text.charAt(removeLength) == '\n') {
    //						lastLine = removeLength;
    //					    }
    //					    removeLength++;
    //					}
    //					if (lastLine > 0) {
    //					    oldText = text.substring(0, lastLine + 1);
    //					    newLines = 1;
    //					    countSpaces = 0;
    //					    indentRule = true;
    //					}
    //				    }
                                        break;
                                }
                                index++;//index += moveIndex;
                                if (index < formatTokens.size()) {
                                    formatToken = formatTokens.get(index);
                                }
                            }



                            if (changeOffset > -1) {
                                boolean isBeforeLineComment = isBeforeLineComment(formatTokens, index-1);
                                if (wasARule) {
                                    if ((!indentRule || newLines == -1) && indentLine) {
                                        boolean handlingSpecialCases = false;
                                        if (FormatToken.Kind.TEXT == formatToken.getId()
                                                && ")".equals(formatToken.getOldText())) {
                                            // tryin find out and handling cases when )) folows.
                                            int hIndex = index + 1;
                                            int hindent = indent;
                                            if (hIndex < formatTokens.size()) {
                                                FormatToken token;
                                                do {
                                                    token = formatTokens.get(hIndex);
                                                    if (token.getId() == FormatToken.Kind.INDENT) {
                                                        hindent += ((FormatToken.IndentToken) token).getDelta();
                                                    }

                                                    hIndex++;
                                                } while (hIndex < formatTokens.size()
                                                        && token.getId() != FormatToken.Kind.WHITESPACE_INDENT
                                                        && token.getId() != FormatToken.Kind.WHITESPACE
                                                        && (token.isWhitespace() || token.getId() == FormatToken.Kind.INDENT));
                                                if (FormatToken.Kind.TEXT == token.getId()
                                                        && (")".equals(token.getOldText()) || ";".equals(token.getOldText()))) {
                                                    countSpaces = hindent;
                                                    handlingSpecialCases = true;
                                                }
                                            }

                                        }
                                        if (!handlingSpecialCases)
                                            countSpaces = Math.max(countSpaces, indent);
                                        newLines = Math.max(1, newLines);
                                    }
                                }
                                else if (indentLine) {
                                    countSpaces = indent;
                                    newLines = oldText == null ? 1 : countOfNewLines(oldText);
                                    if (index > 1 && index < formatTokens.size()
                                            && formatTokens.get(index - 2).getId() == FormatToken.Kind.TEXT
                                            && formatTokens.get(index).getId() == FormatToken.Kind.TEXT
                                            && "(".equals(formatTokens.get(index - 2).getOldText())
                                            && ")".equals(formatTokens.get(index).getOldText())) {
                                        newLines = 0;
                                    } else if (index - 2 > -1){
                                        newLines = getPreviousNonWhite(formatTokens, index-2).getId() == FormatToken.Kind.DOC_COMMENT_END ? 1 : newLines;
                                    }
                                } else {
                                    boolean isBeginLine = isBeginLine(formatTokens, index-1);

                                    if (isBeforeLineComment) {
                                        countSpaces = isBeginLine ? indent : oldText.length();
                                    } else {
                                        countSpaces = isBeginLine
                                                ? isBeforeLineComment ? 0 : Math.max(countSpaces, indent)
                                                : Math.max(countSpaces, 1);
                                    }
                                }
                                if (isBeforeLineComment && oldText != null && oldText.endsWith("\n")) {
                                    countSpaces = 0;
                                }
                                if (wsBetweenBraces && newLines > 1) {
                                    newLines = 1;
                                }
                                if (afterSemi) {
                                    if (oldText == null || (oldText != null && countOfNewLines(oldText) == 0)) {
                                        if (formatToken.getId() == FormatToken.Kind.TEXT) {
                                            if (docOptions.wrapStatementsOnTheSameLine) {
                                                if (docOptions.wrapBlockBrace || !"}".equals(formatToken.getOldText())) {
                                                    newLines = Math.max(1, newLines);
                                                    countSpaces = indent;
                                                }
                                            } else {
                                                if (!indentRule) {
                                                    countSpaces = docOptions.spaceAfterSemi ? 1 : 0;
                                                }
                                            }
                                        } else if (formatToken.getId() == FormatToken.Kind.LINE_COMMENT
                                                || formatToken.getId() == FormatToken.Kind.COMMENT_START) {
                                            if (oldText == null || oldText.length() == 0) {
                                                countSpaces = docOptions.spaceAfterSemi ? 1 : 0;
                                            } else {
                                                countSpaces = oldText.length();
                                            }
                                        }
                                    } else {
                                        if (!indentRule) {
                                            newLines = countOfNewLines(oldText);
                                        }
                                    }
                                    afterSemi = false;
                                }

                                newText = createWhitespace(doc, newLines, countSpaces);
                                if (wsBetweenBraces) {
                                    newText = createWhitespace(doc, 1, indent + docOptions.indentSize) + createWhitespace(doc, 1, indent);
                                }
                                int realOffset = changeOffset + delta;
                                if (templateEdit && !caretInTemplateSolved && oldText != null
                                        && formatContext.startOffset() - 1 <= realOffset
                                    && realOffset <= formatContext.endOffset() + 1) {

                                    int caretPosition = caretOffset + delta;
                                    if (realOffset <= caretPosition && caretPosition <= realOffset + oldText.length() + 1) {
                                        int positionOldText = caretPosition - realOffset - 1;
                                        if (positionOldText > -1  && positionOldText < oldText.length()
                                                && oldText.charAt(positionOldText) ==  ' ') {
                                            newText = ' ' + newText;   // templates like public, return ...
                                        }
                                        caretInTemplateSolved = true;
                                    }
                                }
                            }
                            index--;
                        } else {

                            switch (formatToken.getId()) {
                                case INDENT:
                                    indent += ((FormatToken.IndentToken) formatToken).getDelta();
                                    break;
                                case COMMENT:
                                case DOC_COMMENT:
                                    oldText = formatToken.getOldText() != null ? formatToken.getOldText() : "";
                                    changeOffset = formatToken.getOffset();
                                    newText = formatComment(index, indent, oldText);
                                    if (newText.equals(oldText)) {
                                        newText = null;
                                    }
                                    break;
                                case ANCHOR:
                                    lastAnchor = (FormatToken.AnchorToken) formatToken;
                                    lastAnchor.setAnchorColumn(column);
                                    break;
                                case HTML:
                                    if (htmlIndent > -1) {
                                        oldText = formatToken.getOldText();
                                        int lineOffset = formatToken.getOffset() + delta;
                                        try {
                                            int firstLine = Utilities.getLineOffset(doc, lineOffset);
                                            int lastLine = Utilities.getLineOffset(doc, lineOffset + oldText.length());
                                            int indexInST = 0;
                                            for (StringTokenizer st = new StringTokenizer(oldText, "\n", true); st.hasMoreTokens();) { //NOI18N
                                                String token = st.nextToken();
                                                int currentOffset = formatToken.getOffset() + delta + indexInST;
                                                indexInST = indexInST + token.length();
                                                int currentLine = Utilities.getLineOffset(doc, currentOffset);
                                                if (firstLine < currentLine  /*&& (currentLine < lastLine || token.trim().length() > 0)*/) {
                                                    int lineIndent = Utilities.getRowIndent(doc, currentOffset + 1);
                                                    int phpIndent = indent - docOptions.initialIndent;
                                                    int finalIndent = lineIndent + phpIndent;
                                                    if (lineIndent < finalIndent) {
                                                        delta = replaceString(doc, currentOffset - delta, "", createWhitespace(doc, 0, finalIndent - lineIndent), delta, false);
                                                    }
    //						else if (lineIndent > indent) {
    //						    delta = replaceString(doc, currentOffset - delta, createWhitespace(0, lineIndent - finalIndent), "", delta);
    //						}
                                                }

                                            }

                                        } catch (BadLocationException ex) {
                                            Exceptions.printStackTrace(ex);
                                        }
                                        oldText = null;
                                        newText = null;
                                    }
                                    break;
                            }
                        }

                        delta = replaceString(doc, changeOffset, oldText, newText, delta, templateEdit);
                        if (newText == null) {
                            column += (formatToken.getOldText() == null) ? 0 : formatToken.getOldText().length();
                        } else {
                            int lines = countOfNewLines(newText);
                            if (lines > 0) {
                                column = newText.length() - lines;
                            } else {
                                column += newText.length();
                            }
                        }
                        newText = null;
                        index++;
                    }
                } finally {
                    mti.tokenHierarchyControl().setActive(true);
                }
		if (LOGGER.isLoggable(Level.FINE)) {
		    long end = System.currentTimeMillis();
		    LOGGER.fine("Applaying format stream took: " + (end - start.get())); // NOI18N
		}
	    }

	    private Whitespace countWhiteSpaceBeforeLeftBrace(CodeStyle.BracePlacement placement, boolean spaceBefore, CharSequence text, int indent) {
		int lines = 0;
		int spaces = 0;
		lines = (placement == CodeStyle.BracePlacement.SAME_LINE) ? 0 : 1;
		if (placement == CodeStyle.BracePlacement.PRESERVE_EXISTING) {
		    lines = (countOfNewLines(text) > 0) ? 1 : 0;
		}
		spaces = lines > 0
			? placement == CodeStyle.BracePlacement.NEW_LINE_INDENTED
			? indent + docOptions.indentSize :indent
			: spaceBefore ? 1 : 0;
		return new Whitespace(lines, spaces);
	    }

	    private Whitespace countWSBeforeAStatement(CodeStyle.WrapStyle style, boolean addSpaceIfNoLine, int column, int lengthOfNexSequence, int currentIndent) {
		int lines = 0;
		int spaces = 0;
		switch (style) {
		    case WRAP_ALWAYS:
			lines = 1;
			spaces = currentIndent;
			break;
		    case WRAP_NEVER:
			lines = 0;
			spaces = addSpaceIfNoLine ? 1 : 0;
			break;
		    case WRAP_IF_LONG:
			if (column + 1 + lengthOfNexSequence > docOptions.margin) {
			    lines = 1;
			    spaces = currentIndent + docOptions.indentSize;
			} else {
			    lines = 0;
			    spaces = addSpaceIfNoLine ? 1 : 0;
			}
			break;
		}
		return new Whitespace(lines, spaces);
	    }

	    private Whitespace countWhiteSpaceBeforeRightBrace(CodeStyle.BracePlacement placement, int currentLine, int addLine, int indent, int currentIndex) {
		int lines = 0;
		int spaces = 0;
		lines = addLines(currentLine, addLine);
		// check whether the } is not before open php tag in html
		int index = currentIndex;
		while (index > 0 && (formatTokens.get(index).isWhitespace()
			|| formatTokens.get(index).getId() == FormatToken.Kind.INDENT)) {
		    index--;
		}
		if (lines == 0 && formatTokens.get(index).getId() == FormatToken.Kind.OPEN_TAG) {
		    spaces = 1;
		} else {
		    spaces = placement == CodeStyle.BracePlacement.NEW_LINE_INDENTED ? indent + docOptions.indentSize :indent;
		}
		return new Whitespace(lines, spaces);
	    }

	    private Whitespace countWSBeforeKeyword(boolean placeOnNewLine, boolean placeSpaceBefore, int  currentIndent, int currentIndex) {
		int lines = 0;
		int spaces = 0;
		if (placeOnNewLine) {
		    lines = 1;
		    spaces = currentIndent;
		} else if (isAfterLineComment(formatTokens, currentIndex)) {
		    lines = 1;
		    spaces = currentIndent;
		} else {
		    lines = 0;
		    spaces = placeSpaceBefore ? 1 : 0;
		}
		return new Whitespace(lines, spaces);
	    }

	    private int countLinesAfter(int currentIndex) {
		int lines = -1;

		while (currentIndex < formatTokens.size()
			&& formatTokens.get(currentIndex).isWhitespace()
                        && formatTokens.get(currentIndex).getId() != FormatToken.Kind.WHITESPACE_INDENT) {
		    currentIndex++;
		}
                
                if (formatTokens.get(currentIndex).getId() == FormatToken.Kind.WHITESPACE_INDENT) {
                    lines = countOfNewLines(formatTokens.get(currentIndex).getOldText());
                }

		return lines;
	    }

	    private boolean isOpenAndCloseTagOnOneLine(int currentIndex) {
		boolean result = false;
		FormatToken token = formatTokens.get(currentIndex);
		do {
		    token = formatTokens.get(currentIndex);
		    currentIndex++;
		} while (currentIndex < formatTokens.size()
			&& token.getId() != FormatToken.Kind.WHITESPACE_INDENT
			&& token.getId() != FormatToken.Kind.CLOSE_TAG);
		if (currentIndex < formatTokens.size() && token.getId() == FormatToken.Kind.WHITESPACE_INDENT) {
		    do {
			token = formatTokens.get(currentIndex);
			currentIndex++;
		    } while (currentIndex < formatTokens.size()
			    && token.getId() != FormatToken.Kind.WHITESPACE_INDENT
			    && token.getId() != FormatToken.Kind.CLOSE_TAG);
		}
		result = token.getId() == FormatToken.Kind.CLOSE_TAG;
		return result;
	    }

	    private boolean isCloseAndOpenTagOnOneLine(int currentIndex) {
		boolean result = false;
		FormatToken token = formatTokens.get(currentIndex);
		do {
		    token = formatTokens.get(currentIndex);
		    currentIndex--;
		} while (currentIndex > 0
			&& token.getId() != FormatToken.Kind.WHITESPACE_INDENT
			&& token.getId() != FormatToken.Kind.OPEN_TAG);
		if (currentIndex > 0 && token.getId() == FormatToken.Kind.WHITESPACE_INDENT) {
		    do {
			token = formatTokens.get(currentIndex);
			currentIndex--;
		    } while (currentIndex > 0
			    && token.getId() != FormatToken.Kind.WHITESPACE_INDENT
			    && token.getId() != FormatToken.Kind.OPEN_TAG);
		}
		result = token.getId() == FormatToken.Kind.OPEN_TAG;
		return result;
	    }

	    private int addLines(int currentCount, int addLines) {
		addLines = addLines + 1;
		if (addLines > 1) {
		    currentCount = addLines > currentCount ? addLines : currentCount;
		}
		return currentCount;
	    }


	    private int countUnbreakableTextAfter(int index) {
		int result = 0;
		FormatToken token;

		do {
		    token = formatTokens.get(index);
		    index++;
		} while (index < formatTokens.size()
			&& (token.isWhitespace() || token.getId() == FormatToken.Kind.INDENT));
		index--;
		do {
		    token = formatTokens.get(index);
		    if (token.isWhitespace()) {
			result += token.getOldText() == null ? 0 : 1;
		    } else {
			result += token.getOldText() == null ? 0 : token.getOldText().length();
		    }
		    result++;  // space after the token
		    index++;
		} while (index < formatTokens.size() && !token.isBreakable());
		result--;
		return result;
	    }

	    private String formatComment(int index, int indent, String comment) {
		if (comment == null || comment.length() == 0) {
		    return "";
		}
		StringBuilder sb = new StringBuilder();
		boolean indentLine = false;
		String indentString = createWhitespace(doc, 0, indent + 1);
		if (comment.charAt(0) == '\n') {
		    sb.append('\n');
		    indentLine = true;
		}
		boolean lastAdded = false;
		for (StringTokenizer st = new StringTokenizer(comment, "\n"); st.hasMoreTokens();) { //NOI18N
		    String part = st.nextToken();
		    if (!(part.length() > (indent + 1) && part.charAt(indent + 1) == '*')) {
			part = part.trim();
			if (part.length() > 0) {
			    if (indentLine) {
				sb.append(indentString);
				if (part.length() > 1 && part.charAt(1) != ' ') {
				    sb.append("* "); //NOI18N
				    part = part.substring(1);
				}
			    } else {
				indentLine = true;
				sb.append(' ');
			    }
			}
		    }
		    if (part.length() > 0) {
			sb.append(part);
			sb.append('\n');
			lastAdded = true;
		    }
		    else {
			lastAdded = false;
		    }
		}


		if (comment.charAt(comment.length() - 1) == '\n') {
		    sb.append(indentString);
		}
		else {
		    if(sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n' && !lastAdded) {
			sb.append(indentString);
		    } else {
			if (sb.length() > 0) {
			    sb.setLength(sb.length() - 1); // remove the last new line
			}
			if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
			    sb.append(' ');
			}
			if (sb.length() == 0) {
			    sb.append(' ');
			}
		    }
		}
		return sb.toString();
	    }

	    private boolean isAfterOpenBrace (int offset) {
		boolean value = false;
		if (offset < doc.getLength()) {
		    String ch = "";
		    int index = offset;
		    try {
			do {
			    ch = doc.getText(index, 1);
			    index--;
			} while (index > 0 && "\n\t ".contains(ch));
		    } catch (BadLocationException ex) {
			Exceptions.printStackTrace(ex);
		    }
		    value = "{".equals(ch); // NOI18N
		}
		return value;
	    }

//	    private int countOfChanges = 0;

            private int startOffset = -1;
            private int endOffset = -1;

	    private int replaceString(BaseDocument document, int offset, String oldText, String newText, int delta, boolean templateEdit) {
		if (oldText == null) {
		    oldText = "";
		}
                if(startOffset == -1) {
                    startOffset = formatContext.startOffset() == 0
                            ? formatContext.startOffset()
                            : formatContext.startOffset() -1;
                    System.out.println("char: " + document.getText().charAt(startOffset));
                    while (startOffset > 0 && Character.isWhitespace(document.getText().charAt(startOffset))) {
                        startOffset --;
                    }
                    if (startOffset > 0) {
                        startOffset ++;
                    }
                    endOffset = formatContext.endOffset();
                    while (endOffset < document.getLength() && Character.isWhitespace(document.getText().charAt(endOffset))) {
                        endOffset ++;
                    }
                    if (endOffset < document.getLength()) {
                        endOffset --;
                    }

                }
                if (newText != null && !oldText.equals(newText)) {
                    int realOffset = offset + delta;
                    if (startOffset <= realOffset
                            && realOffset <= endOffset + delta) {

                        if (!templateEdit) { // if is not in template, then replace simply
                            delta = replaceSimpleString(document, realOffset, oldText, newText, delta);
                        } else {
                            // the replacing has to be done line by line.
                            int indexOldTextLine = oldText.indexOf('\n');
                            int indexNewTextLine = newText.indexOf('\n');
                            int indexOldText = 0;
                            int indexNewText = 0;
                            String replaceOld;
                            String replaceNew;

                            if ((indexOldTextLine == -1 && indexNewTextLine == -1) // no new line in both
                                    || oldText.length() == 0) {
                                delta = replaceSimpleString(document, realOffset, oldText, newText, delta);
                            } else {

                                do {
                                    indexOldTextLine = oldText.indexOf('\n', indexOldText); // NOI18N
                                    indexNewTextLine = newText.indexOf('\n', indexNewText); // NOI18N

                                    if (indexOldTextLine == -1)
                                        indexOldTextLine = oldText.length();
                                    if (indexNewTextLine == -1)
                                        indexNewTextLine = newText.length();
                                    replaceOld = indexOldText == indexOldTextLine ? "\n" : oldText.substring(indexOldText, indexOldTextLine); // NOI18N
                                    replaceNew = indexNewText == indexNewTextLine ? "\n" : newText.substring(indexNewText, indexNewTextLine); // NOI18N
                                    if (!replaceOld.equals(replaceNew)) {
                                        delta = replaceSimpleString(document, realOffset + indexOldText,
                                                replaceOld, replaceNew, delta);
                                    }
                                    indexOldText = indexOldTextLine + 1;//(indexOldText == indexOldTextLine ? 2 : 1);
                                    indexNewText = indexNewTextLine + 1;//(indexNewText == indexNewTextLine ? 2 : 1);
                                    realOffset = offset + delta;

                                } while (indexOldText < oldText.length()
                                        && indexNewText < newText.length());

                                if (indexOldText >= oldText.length()
                                        && indexNewText < newText.length()) {
                                    delta = replaceSimpleString(document, realOffset + oldText.length(),
                                            "", newText.substring(indexNewText), delta);
                                }
                            }

                        }
                    }
                }
		return delta;
	    }

            private int replaceSimpleString (BaseDocument document, int realOffset, String oldText, String newText, int delta) {
                try {
                    if (oldText.length() > 0) {

                        int removeLength = realOffset + oldText.length() < document.getLength()
                                ? oldText.length()
                                : document.getLength() - realOffset;
                        document.remove(realOffset, removeLength);

                    }
                    document.insertString(realOffset, newText, null);
                    delta = delta - oldText.length() + newText.length();
                } catch (BadLocationException ex) {
                    LOGGER.throwing(this.getClass().getName(), "replaceSimpleSring", ex); //NOI18N
                }
                return delta;
            }

	    private int countLengthOfNextSequence(int index) {
		FormatToken token = formatTokens.get(index);
		int length = 0;
		if (token.getId() == FormatToken.Kind.UNBREAKABLE_SEQUENCE_START) {
		    index++;
		    token = formatTokens.get(index);

		    while (index < formatTokens.size()
			    && token.getId() != FormatToken.Kind.UNBREAKABLE_SEQUENCE_END) {
			if (token.getId() == FormatToken.Kind.WHITESPACE) {
			    length += 1;
			}
			else {
			    if (token.getOldText() != null) {
				length += token.getOldText().length();
			    }
			}
			index++;
			if (index < formatTokens.size()) {
			    token = formatTokens.get(index);
			}
		    }
		}
		return length;
	    }
	});
    }

    static private class Whitespace {
	int lines;
	int spaces;

	public Whitespace(int lines, int spaces) {
	    this.lines = lines;
	    this.spaces = spaces;
	}

    }
    
    /**
     *
     * @param tokens
     * @param index of the whitespace token
     * @return
     */
    private boolean isBeforeLineComment(List<FormatToken> tokens, int index) {
	FormatToken token = tokens.get(index);
	while (index < tokens.size() - 1 && (token.isWhitespace() || token.getId() == FormatToken.Kind.INDENT)) {
	    token = tokens.get(++index);
	}
	return token.getId() == FormatToken.Kind.LINE_COMMENT;
    }

    private FormatToken getPreviousNonWhite(List<FormatToken> tokens, int index) {
	if(index < 0)
	    return null;
	FormatToken token = tokens.get(index);
	while (index < tokens.size() - 1 && (token.isWhitespace() || token.getId() == FormatToken.Kind.INDENT)) {
	    token = tokens.get(++index);
	}
	return token;
    }

    private boolean isAfterLineComment(List<FormatToken> tokens, int index) {
	FormatToken token = tokens.get(index);
	while (index > 0 && (token.isWhitespace() || token.getId() == FormatToken.Kind.INDENT
		|| token.getId() == FormatToken.Kind.UNBREAKABLE_SEQUENCE_END)) {
	    token = tokens.get(--index);
	}
	return token.getId() == FormatToken.Kind.LINE_COMMENT;
    }

    private boolean isBeginLine(List<FormatToken> tokens, int index) {
	FormatToken token = tokens.get(index);
	while (index > 0 && (token.isWhitespace() || token.getId() == FormatToken.Kind.INDENT)
		&& token.getId() != FormatToken.Kind.WHITESPACE_INDENT) {
	    token = tokens.get(--index);
	}

	return token.getId() == FormatToken.Kind.WHITESPACE_INDENT || token.getId() == FormatToken.Kind.LINE_COMMENT;
    }

    private String createWhitespace(Document document, int lines, int spaces) {
	StringBuilder sb = new StringBuilder();
        if (spaces > 0) {
            for (int i = 0; i < lines; i++) {
                sb.append('\n');
            }
            sb.append(IndentUtils.createIndentString(document, spaces));
        }
	return sb.toString();
    }
}
