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
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.module.spi;

import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * A CSS specific extension of CSL's CodeCompletionContext
 *
 * @author marekfukala
 */
public final class CssCodeCompletionContext {

    private Node activeNode;
    private int caretOffset, anchorOffset, embeddedCaretOffset, embeddedAnchorOffset;
    private String prefix;
    private CssParserResult result;
    private TokenSequence<CssTokenId> tokenSequence;
    private QueryType queryType;

    CssCodeCompletionContext(Node activeNode, int caretOffset, int anchorOffset, int embeddedCaretOffset, int embeddedAnchorOffset, String prefix, CssParserResult result, TokenSequence<CssTokenId> tokenSequence, QueryType queryType) {
        this.activeNode = activeNode;
        this.caretOffset = caretOffset;
        this.anchorOffset = anchorOffset;
        this.embeddedCaretOffset = embeddedCaretOffset;
        this.embeddedAnchorOffset = embeddedAnchorOffset;
        this.prefix = prefix;
        this.result = result;
        this.tokenSequence = tokenSequence;
        this.queryType = queryType;
    }
    
    public Node getActiveNode() {
        return activeNode;
    }
    
    /**
     * The editor's caret offset relative to the edited document.
     * 
     */
    public int getCaretOffset() {
        return caretOffset;
    }
    
    /**
     * anchor offset = caret offset - prefix length.
     * Relative to the edited document.
     * 
     */
    public int getAnchorOffset() {
        return anchorOffset;
    }
    
    /**
     * Same as getCaretOffset() but relative to the embedded css code.
     */
    public int getEmbeddedCaretOffset() {
        return embeddedCaretOffset;
    }
    
    /**
     * Same as getAnchorOffset() but relative to the embedded css code.
     */
    public int getEmbeddedAnchorOffset() {
        return embeddedAnchorOffset;
    }
        
    public String getPrefix() {
        return prefix;
    }
    
    public CssParserResult getParserResult() {
        return result;
    }
    
    public Snapshot getSnapshot() {
        return result.getSnapshot();
    }
        
    /**
     * 
     * @return a TokenSequence of Css tokens created on top of the *virtual* css source.
     * The TokenSequence is positioned on a token laying at the getAnchorOffset() offset.
     */
    public TokenSequence<CssTokenId> getTokenSequence() {
        return tokenSequence;
    }
    
    public QueryType getQueryType() {
        return queryType;
    }
    
    
}
