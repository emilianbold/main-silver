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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.xml.text.indent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.xml.text.folding.TokenElement;
import org.netbeans.modules.xml.text.folding.TokenElement.TokenType;
import org.netbeans.modules.xml.text.folding.XmlFoldManager;

/**
 * New XML formatter based on Lexer APIs.
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class XMLLexerFormatter {
    
    private static final Logger logger = Logger.getLogger(XMLLexerFormatter.class.getName());

    private static final String TAG_OPENING_PREFIX = "<"; //NOI18N
    private static final String TAG_CLOSING_PREFIX = "</"; //NOI18N

    private final LanguagePath languagePath;
    private int spacesPerTab = 4;

    public XMLLexerFormatter(LanguagePath languagePath) {
        this.languagePath = languagePath;
    }

    protected LanguagePath supportedLanguagePath() {
        return languagePath;
    }

// # 170343
    public void reformat(Context context, final int startOffset, final int endOffset)
            throws BadLocationException {
        final BaseDocument doc = (BaseDocument) context.document();
        doc.render(new Runnable() {

            public void run() {
                doReformat(doc, startOffset, endOffset);
            }
        });
    }

    public BaseDocument doReformat(BaseDocument doc, int startOffset, int endOffset) {
        spacesPerTab = IndentUtils.indentLevelSize(doc);
        try {
            List<TokenIndent> tags = getTags(doc, startOffset, endOffset);
            for (int i = tags.size() - 1; i >= 0; i--) {
                if (tags.get(i).isPreserveIndent()) {
                    continue;
                }
                TokenElement tag = tags.get(i).getToken();

                int so = tag.getStartOffset();
                int lineOffset = Utilities.getLineOffset(doc, so);
                String tagName = tag.getName();
                if (tagName.startsWith("</")) {
                    /* For Eg:-
                     *
                     * <abc><xyz>123</xyz> //skip format for 'xyz'
                     *
                     * <xyz><abc>123</abc></xyz> //do format for 'xyz' and skip for 'abc'
                     *
                     */
                    Element docElem = doc.getDefaultRootElement().getElement(lineOffset);
                    String lineStr = doc.getText(docElem.getStartOffset(),
                            docElem.getEndOffset() - docElem.getStartOffset());
                    int ndx = lineStr.lastIndexOf(tagName);
                    if (ndx != -1) {
                        lineStr = lineStr.substring(0, ndx);
                        int ndx2 = lineStr.lastIndexOf("<" + tagName.substring(2));
                        if (ndx2 == -1) {//no start found in this line, so indent this tag
                            changePrettyText(doc, tag, so);
                        } else {
                            lineStr = lineStr.substring(ndx2 + 1);
                            ndx2 = lineStr.indexOf("<");
                            if (ndx2 != -1) {//indent this tag if it contains another tag
                                changePrettyText(doc, tag, so);
                            }
                        }
                    }
                } else {
                    changePrettyText(doc, tag, so);
                }
            }
        } catch (BadLocationException ble) {
            //ignore exception
        } catch (IOException iox) {
            //ignore exception
        } finally {
            //((AbstractDocument)doc).readUnlock();
        }
        return doc;
    }

    private void changePrettyText(BaseDocument doc, TokenElement tag, int so) throws BadLocationException {
        //i expected the call IndentUtils.createIndentString() to return
        //the correct string for the indent level, but it doesn't.
        //so this is just a workaround.
        int spaces;
        boolean noNewline = false;
        
        switch (tag.getType()) {
            case TOKEN_ATTR_NAME:
                spaces = tag.getIndentLevel();
                break;
            case TOKEN_PI_START_TAG:
                noNewline = true;
                // fall through
            default:
                spaces = tag.getIndentLevel();
                break;
        }
        String newIndentText = IndentUtils.createIndentString(doc, spaces);
        //String newIndentText = formatter.getIndentString(doc, tag.getIndentLevel());
        int previousEndOffset = Utilities.getFirstNonWhiteBwd(doc, so) + 1;
        String temp = doc.getText(previousEndOffset, so - previousEndOffset);
        if(noNewline || so == 0 || temp.indexOf("\n") != -1){
            int i = Utilities.getRowFirstNonWhite(doc, so);
            int rowStart = Utilities.getRowStart(doc, so);
            
            String currentIndent = doc.getText(rowStart, i - rowStart);
            if (!currentIndent.equals(newIndentText)) {
                doc.insertString(so, newIndentText, null);
                doc.remove(rowStart, i - rowStart);
            }
        }
        else {
            doc.insertString(so, "\n" + newIndentText, null);
        }
    }

    /**
     * This is the core of the formatting algorithm.  It was originally derived
     * from {@link XmlFoldManager#createFolds(org.netbeans.spi.editor.fold.FoldHierarchyTransaction)}.
     * Like that method, this method parses the document using lexer.  Rather
     * than creating folds though, this method reformats by manipulating the
     * whitespace tokens.  To do this it keeps track of the nesting level of the
     * XML and the use of the xml:space attribute.  Together they are used to
     * calculate how much each token should be indented by.
     */
    private List<TokenIndent> getTags(BaseDocument basedoc, int startOffset, int endOffset)
            throws BadLocationException, IOException {
        List<TokenIndent> tags = new ArrayList<TokenIndent>();

        // List to keep track of whether whitespace is to be preserved at each
        // level of nesting.  By default whitesapce is not preserved.
        LinkedList<Boolean> preserveNesting_outdent = new LinkedList<Boolean>();
        preserveNesting_outdent.add(Boolean.FALSE);

        // flag that is true if whitespace is currently
        // to not be changed.  That is, xml:space
        // was last set to "preserve".
        boolean preserveWhitespace = false;

        // flag to indicate if the current
        // argument is xml:space
        boolean settingSpaceValue = false;

        // this is that 1st PI or tag will increment the level to 0
        int indentLevel = -spacesPerTab;
        basedoc.readLock();
        try {
            TokenHierarchy tokenHierarchy = TokenHierarchy.get(basedoc);
            TokenSequence<XMLTokenId> tokenSequence = tokenHierarchy.tokenSequence();
            org.netbeans.api.lexer.Token<XMLTokenId> token = tokenSequence.token();
            // Add the text token, if any, before xml declaration to document node
            if (token != null && token.id() == XMLTokenId.TEXT) {
                if (tokenSequence.moveNext()) {
                    token = tokenSequence.token();
                }
            }
            int currentTokensSize = 0;
            Stack<TokenElement> stack = new Stack<TokenElement>();

            // will be set to indent of 1st attribute of a tag. Will be reset to -1 by start tag
            int firstAttributeIndent = -1;
            int lineIndent = -1;
            
            while (tokenSequence.moveNext()) {
                int indentLineStart = 1;
                token = tokenSequence.token();
                XMLTokenId tokenId = token.id();
                String image = token.text().toString();
                if (tokenSequence.offset() > endOffset) {
                    break;
                }
                boolean tokenInSelectionRange = tokenSequence.offset() >= startOffset || tokenSequence.offset() + token.length() > endOffset;
                TokenType tokenType = TokenType.TOKEN_WHITESPACE;
                switch (tokenId) {
                    case TAG: { // Tag is encountered and the required level of indenting determined.
                                // The tokens are only assessed if they are in the selection
                                // range, which is the whole document if no text is selected.
                        int len = image.length();
                        firstAttributeIndent = -1;
                        if (image.charAt(len - 1) == '>') {// '/>'
                            if (len == 2) {
                                if (!preserveWhitespace) {
                                    indentLevel -=  spacesPerTab;
                                }
                                if (!stack.empty()) {
                                    stack.pop();
                                }
                            } else {
                                // end tag name marker
                                if (lineIndent > -1 && tokenInSelectionRange) {
                                    // 1st item on a new line, will indent according to the opening tag
                                    TokenElement tag = new TokenElement(TokenType.TOKEN_ELEMENT_END_TAG, image, 
                                            tokenSequence.offset(), tokenSequence.offset() + token.length(), indentLevel);
                                    tags.add(new TokenIndent(tag, false));
                                }
                            }
                        } else {
                            tokenType = TokenType.TOKEN_ELEMENT_START_TAG;
                            if (image.startsWith("</")) {
                                int begin = currentTokensSize;
                                int end = begin + image.length();

                                boolean preservingWhitespaceOnClose = !preserveNesting_outdent.isEmpty() && preserveNesting_outdent.removeLast();
                                if (indentLevel < 0) {
                                    indentLevel = 0;
                                }
                                // look into tag stack, try to find a proper indent level
                                String tagName = image.substring(2);
                                int newIndentLevel = -1;
                                for (int i = stack.size() - 1; newIndentLevel < 0 && i >= 0; i--) {
                                    TokenElement el = stack.get(i);
                                    if (el.getName().equals(tagName)) {
                                        newIndentLevel = el.getIndentLevel();
                                        stack.subList(i, stack.size() - 1).clear();
                                    }
                                }
                                boolean changeIndent = !preserveNesting_outdent.isEmpty() && !preserveNesting_outdent.getLast();
                                if (changeIndent) {
                                    indentLevel = (newIndentLevel >= 0 ? newIndentLevel : indentLevel);
                                }
                                if (tokenInSelectionRange) {
                                    TokenElement tag = new TokenElement(tokenType, image, begin, end, indentLevel);
                                    tags.add(new TokenIndent(tag, preservingWhitespaceOnClose));
                                }
                                if (changeIndent) {
                                    indentLevel -= spacesPerTab;
                                }
                                preserveWhitespace = !preserveNesting_outdent.isEmpty() && preserveNesting_outdent.getLast();
                            } else {
                                String tagName = image.substring(1);
                                int begin = currentTokensSize;
                                int end = begin + image.length();
                                preserveWhitespace = !preserveNesting_outdent.isEmpty() && preserveNesting_outdent.getLast();
                                if (!preserveWhitespace) {
                                    if (!tokenInSelectionRange) {
                                        indentLevel = Utilities.getVisualColumn(basedoc, tokenSequence.offset());
                                        // no increment here, indent level is taken from the current document's layout as absolute pos
                                    } else {
                                        indentLevel += spacesPerTab;
                                    }
                                }
                                TokenElement tag = new TokenElement(tokenType, tagName, begin, end, indentLevel);
                                preserveNesting_outdent.add(preserveWhitespace);
                                stack.push(tag);
                                if (tokenInSelectionRange) {
                                    tags.add(new TokenIndent(tag, preserveWhitespace));
                                }
                            }
                            settingSpaceValue = false;
                        }
                        break;
                    }
                    case PI_START: {
                        tokenType = TokenType.TOKEN_PI_START_TAG;
                        indentLevel += spacesPerTab;
                        if (tokenInSelectionRange && !preserveWhitespace) {
                            TokenElement tag = new TokenElement(tokenType, tokenId.name(), tokenSequence.offset(), tokenSequence.offset() + token.length(), indentLevel);
                            tags.add(new TokenIndent(tag, preserveWhitespace));
                        }
                        break;
                    }
                    case PI_END: {
                        indentLevel -= spacesPerTab;
                        if (lineIndent > -1 && tokenInSelectionRange) {
                            // 1st item on a new line, will indent according to the opening tag
                            TokenElement tag = new TokenElement(TokenType.TOKEN_PI_END_TAG, image, 
                                    tokenSequence.offset(), tokenSequence.offset() + token.length(), indentLevel);
                            tags.add(new TokenIndent(tag, false));
                        }
                        break;
                    }
                    case WS: {
                            // we assume there is nothing except whitespace
                            int lastNewline = image.lastIndexOf('\n');
                            if (lastNewline == -1) {
                                // nothing special here
                                break;
                            }
                            int tokenOffset = tokenSequence.offset();
                            int nextLine = tokenOffset + lastNewline;

                            // determine line start of the newline
                            lineIndent = Utilities.getRowIndent(basedoc, nextLine);
                            break;
                    }
                    case PI_CONTENT:
                        indentLineStart = 0;
                        // fall through
                    case TEXT: {
                        // must detect newlines. If inside a tag (between attributes), the 1st attribute on the line
                        // will emit indent token to the output stream.
                        // if outside tags (normal text), each newline in non-ws-preserving tag will emit an indent token
                        int lastNewline = image.lastIndexOf('\n');
                        if (lastNewline == -1) {
                            // nothing special here
                            break;
                        }
                        if (preserveWhitespace || !tokenInSelectionRange) {
                            break;
                        }
                        // emit tag record for each subsequent line
                        String[] lines = image.split("\n");
                        int lno = indentLineStart; // skip 1st line = up to the 1st newline
                        int currentOffset = tokenSequence.offset();
                        while (lno < lines.length) {
                            currentOffset += lno == 0 ? 0 : lines[lno - 1].length() + 1; // add 1 for newline
                            int lineEnd = currentOffset + lines[lno].length();
                            int nonWhiteStart = Utilities.getFirstNonWhiteFwd(basedoc, currentOffset, lineEnd);
                            // implies a check for nonWhitestart > -1
                            if (nonWhiteStart >= startOffset && nonWhiteStart <= endOffset) {
                                // emit a tag at this position
                                tags.add(new TokenIndent(
                                    new TokenElement(TokenType.TOKEN_CHARACTER_DATA, 
                                            tokenId.name(), 
                                            nonWhiteStart, lineEnd,
                                            indentLevel + spacesPerTab), 
                                    false
                                ));
                            }
                            lno++;
                        }
                        break;
                    }

                    /**
                     * Block comments are aligned as follows:
                     * - if there is some preceeding non-whitespace, do not format anything. E.g. comments after element. Skip entire comment from formatting
                     * - align 1st and last line at the appropriate indent level
                     * - compute "shift" from the last line & indent level
                     * - shift INTERIOR of the comment by the computed shift
                     * 
                     * This algorithm tries to preserve internal formatting of the comment
                     */
                    case BLOCK_COMMENT: {
                        int currentOffset = tokenSequence.offset();
                        
                        String[] lines = image.split("\n");

                        int lineStart = Utilities.getRowStart(basedoc, currentOffset);

                        if (lineStart < currentOffset && 
                             Utilities.getFirstNonWhiteBwd(basedoc, currentOffset, lineStart) > -1) {
                            // we cannot indent comment start, will not touch even the rest of the comment.
                            break;
                        }
                        
                        int lastLineStart = Utilities.getRowStart(basedoc, currentOffset + token.length() - 1);
                        int lastIndent = IndentUtils.lineIndent(basedoc, lastLineStart);

                        // align 1st and last row here:
                        int baseIndent = indentLevel + spacesPerTab;
                        // shift the rest of lines by this offset
                        int indentShift = baseIndent - lastIndent;

                        // how much to shift the interior of the comment
                        
                        for (int lno = 0; lno < lines.length; lno++) {
                            // indent 1st comment line, as if it was text:
                            int lineEnd = Utilities.getRowEnd(basedoc, currentOffset);
                            
                            int desiredIndent;
                            if (lno == 0 || lno == lines.length -1) {
                                desiredIndent = baseIndent;
                            } else {
                                desiredIndent = IndentUtils.lineIndent(basedoc, currentOffset) + indentShift;
                            }
                            
                            if ((currentOffset >= startOffset || currentOffset + lines[lno].length() > endOffset) && currentOffset < endOffset) {
                                tags.add(new TokenIndent(
                                    new TokenElement(TokenType.TOKEN_CHARACTER_DATA, 
                                            tokenId.name(), 
                                            currentOffset, lineEnd,
                                            Math.max(0, desiredIndent)), 
                                    false
                                ));
                            }
                            currentOffset += lines[lno].length() + 1;
                        }
                        break;
                    }
                        

                    case CDATA_SECTION:
                    case CHARACTER:
                    case OPERATOR:
                    case PI_TARGET:
                    case DECLARATION:
                        break; //Do nothing for above case's
                    case ARGUMENT: //attribute of an element
                        settingSpaceValue = token.text().equals("xml:space");
                        // fall through !
                        if (lineIndent != -1) {
                            int attrIndent;
                            if (firstAttributeIndent == -1) {
                                tokenType = TokenType.TOKEN_CHARACTER_DATA;
                                // this is the 1st attribute, on its own line - we respect the indentation of the XML tags
                                attrIndent = indentLevel + spacesPerTab;
                            } else {
                                tokenType = TokenType.TOKEN_ATTR_NAME;
                                attrIndent = firstAttributeIndent;
                            }
                            if (tokenInSelectionRange) {
                                tags.add(
                                    new TokenIndent(
                                        new TokenElement(tokenType, 
                                                token.text().toString(), 
                                                tokenSequence.offset(), tokenSequence.offset() + token.length(), 
                                                attrIndent), 
                                        false
                                    )
                                );
                            }
                        } else {
                            if (firstAttributeIndent == -1) {
                                firstAttributeIndent = Utilities.getVisualColumn(basedoc, tokenSequence.offset());
                            }
                        }
                        break;
                    case VALUE:
                        if (settingSpaceValue) {
                            if (token.text().equals("\"preserve\"")) {
                                preserveWhitespace = true;
                            } else if (token.text().equals("\"default\"")) {
                                preserveWhitespace = false;
                            }
                            preserveNesting_outdent.set(preserveNesting_outdent.size() - 1, preserveWhitespace);
                            settingSpaceValue = false;
                        }
                        break;

                    case ERROR:
                    case EOL:
                    default:
                        throw new IOException("Invalid token found in document: "
                                + "Please use the text editor to resolve the issues...");
                }
                currentTokensSize += image.length();
                if (tokenId != XMLTokenId.WS && tokenId != XMLTokenId.TEXT) {
                    // clear indicator of the newline
                    lineIndent = -1;
                }
            }
        } finally {
            basedoc.readUnlock();
        }
        return tags;
    }
    
    public boolean isOneLiner(int start, int end, BaseDocument doc) {
        try {
            return Utilities.getLineOffset(doc, start) ==
                    Utilities.getLineOffset(doc, end);
        } catch (BadLocationException ex) {
            //Exceptions.printStackTrace(ex);
            return false;
        }
    }

    /**
     * The formatter needs to keep track of when it can remove whitespace and
     * when it must preserve whitespace as defined by the xml:space attribute.
     * This class associates a flag that defines whether whitespace is to be
     * preserved with the other token data that is used in the code folding
     * algorithm.
     */
    private class TokenIndent {

        private TokenElement token;
        private boolean preserveIndent;

        public TokenIndent(TokenElement token, boolean preserveIndent) {
            this.token = token;
            this.preserveIndent = preserveIndent;
        }

        public TokenElement getToken() {
            return token;
        }

        public boolean isPreserveIndent() {
            return preserveIndent;
        }

        public void setPreserveIndent(boolean preserveIndent) {
            this.preserveIndent = preserveIndent;
        }

        @Override
        public String toString() {
            return "TokenIndent: name=" + token.getName() + " preserveIndent=" + preserveIndent;
        }
    }

    void reformat(Context context) throws BadLocationException{
        reformat(context, context.startOffset(), context.endOffset());
    }
}
