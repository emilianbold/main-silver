/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007-2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.spellchecker.bindings.htmlxml;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * Tokenize RHTML for spell checking: Spell check Ruby comments AND HTML text content!
 *
 * @author Tor Norbye
 */
public class HtmlTokenList extends AbstractTokenList {


    private boolean hidden = false;

    public HtmlTokenList(BaseDocument doc) {
        super(doc);
    }

    @Override
    public void setStartOffset(int offset) {
        super.setStartOffset (offset);
        FileObject fileObject = FileUtil.getConfigFile ("Spellcheckers/HTML");
        Boolean b = (Boolean) fileObject.getAttribute ("Hidden");
        hidden = Boolean.TRUE.equals (b);
    }

    //fast hack for making the spellchecking embedding aware, should be fixed properly
    //performance: the current approach is wrong since a new token sequence is obtained
    //and positioned for each search offset!
    @Override
    protected int[] findNextSpellSpan() throws BadLocationException {
        TokenSequence<HTMLTokenId> ts = getHtmlTokenSequence(doc, nextSearchOffset);
        return findNextSpellSpan(ts, nextSearchOffset); 
    }

    /** Given a sequence of HTML tokens, return the next span of eligible comments */
    @Override
    protected int[] findNextSpellSpan(TokenSequence<? extends TokenId> ts, int offset) throws BadLocationException {
        if (ts == null || hidden) {
            return new int[]{-1, -1};
        }

        ts.move(offset);

        while (ts.moveNext()) {
            TokenId id = ts.token().id();

            if (id == HTMLTokenId.SGML_COMMENT || id == HTMLTokenId.BLOCK_COMMENT || id == HTMLTokenId.TEXT) {
                return new int[]{ts.offset(), ts.offset() + ts.token().length()};
            }
        }

        return new int[]{-1, -1};
    }


    private TokenSequence<HTMLTokenId> getHtmlTokenSequence(Document doc, int offset) {
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence ts = th.tokenSequence();
        if(ts == null) {
            return null;
        }
        ts.move(offset);

        while(ts.moveNext() || ts.movePrevious()) {
            if(ts.language() == HTMLTokenId.language()) {
                return ts;
            }

            ts = ts.embedded();

            if(ts == null) {
                break;
            }

            //position the embedded ts so we can search deeper
            ts.move(offset);
        }

        return null;

    }

}
