/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.html.editor.gsf.embedding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.WebUtils;

/**
 * Creates CSS virtual source for html sources.
 * 
 * @author Marek Fukala
 */
public class CssHtmlTranslator implements CssEmbeddingProvider.Translator {

    private static final Logger LOGGER = Logger.getLogger(CssHtmlTranslator.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    public static final String CSS_MIME_TYPE = "text/x-css"; //NOI18N
    public static final String HTML_MIME_TYPE = "text/html"; //NOI18N

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        TokenHierarchy th = snapshot.getTokenHierarchy();
        TokenSequence<HTMLTokenId> ts = th.tokenSequence(HTMLTokenId.language());
        HashMap<String, Object> state = new HashMap<String, Object>(6);
        List<Embedding> embeddings = new ArrayList<Embedding>();
        extractCssFromHTML(snapshot, ts, state, embeddings);
        return embeddings;
    }
    //internal state names for the html code analyzer
    protected static final String END_OF_LAST_SEQUENCE = "end_of_last_sequence"; //NOI18N
    protected static final String IN_STYLE = "in_style"; //NOI18N
    protected static final String IN_INLINED_STYLE = "in_inlined_style"; //NOI18N
    private static final String QUTE_CUT = "quote_cut"; //NOI18N
    //TODO rewrite the whole embedding provider to the parser based version so
    //we do not have to parse what's already parsed - like the <link ... /> tag
    private static final String LINK_TAG_NAME = "link"; //NOI18N
    private static final String HREF_ATTR_NAME = "href"; //NOI18N
    private static final String HREF_ATTR_REL = "rel"; //NOI18N
    private static final String HREF_ATTR_TYPE = "type"; //NOI18N

    /** @param ts An HTML token sequence always positioned at the beginning. */
    protected void extractCssFromHTML(Snapshot snapshot, TokenSequence<HTMLTokenId> ts, HashMap<String, Object> state, List<Embedding> embeddings) {
        while (ts.moveNext()) {
            Token<HTMLTokenId> htmlToken = ts.token();
            TokenId htmlId = htmlToken.id();
            if (htmlId == HTMLTokenId.STYLE) {
                state.put(IN_STYLE, Boolean.TRUE);
                //jumped into style
                int sourceStart = ts.offset();
                int sourceEnd = sourceStart + htmlToken.length();
                embeddings.add(snapshot.create(sourceStart, sourceEnd - sourceStart, CSS_MIME_TYPE));
            } else {
                //jumped out of the style
                state.remove(IN_STYLE);

                if (state.get(IN_INLINED_STYLE) != null) {
                    if (htmlId == HTMLTokenId.VALUE_CSS) {
                        //continuation of the html style attribute value after templating
                        int sourceStart = ts.offset();
                        CharSequence text = htmlToken.text();
                        int tokenLength = htmlToken.length();
                        if (CharSequenceUtilities.endsWith(text, "\"") || CharSequenceUtilities.endsWith(text, "'")) {
                            tokenLength--;
                        }
                        embeddings.add(snapshot.create(sourceStart, tokenLength, CSS_MIME_TYPE));

                    } else {
                        state.remove(IN_INLINED_STYLE);


                        //xxx check if the following code has still meaning
                        //using the new embeddings

                        //generate the end of the virtual selector
                        int sourceStart = ts.offset();

                        if (state.get(QUTE_CUT) != null) {
                            sourceStart--;
                        }
                        // <<< eof xxx

                        state.remove(QUTE_CUT);
                        embeddings.add(snapshot.create(";\n}\n", CSS_MIME_TYPE));


                    }
                }

                if (htmlId == HTMLTokenId.TAG_OPEN) {
                    //look at the tag and try to find the style="xx" attribute
                    //TODO make it work at the border of embedded sections

                    //TokenSequence<? extends HTMLTokenId> ts = ts.subSequence(ts.offset());
                    //ts.moveStart();
                    boolean isLinkTag = LINK_TAG_NAME.equals(htmlToken.text().toString().toLowerCase(Locale.ENGLISH));
                    String currentAttributeName = null;
                    while (ts.moveNext()) {
                        Token<? extends HTMLTokenId> t = ts.token();
                        TokenId id = t.id();

                        if (id == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                            break;
                        } else if (id == HTMLTokenId.ARGUMENT) {
                            currentAttributeName = t.text().toString();
                        } else if (id == HTMLTokenId.VALUE_CSS) {
                            //found inlined css
                            int sourceStart = ts.offset();
                            String text = t.text().toString();

                            if (text.startsWith("\"") || text.startsWith("'")) {
                                sourceStart++;
                                text = text.substring(1);
                            }

                            int sourceEnd = sourceStart + text.length();
                            if (text.endsWith("\"") || text.endsWith("'")) {
                                sourceEnd--;
                                state.put(QUTE_CUT, Boolean.TRUE);
                                text = text.substring(0, text.length() - 1);
                            }

                            //determine the inlined css type
                            String valueCssType = (String) t.getProperty(HTMLTokenId.VALUE_CSS_TOKEN_TYPE_PROPERTY);
                            if (valueCssType != null) {
                                //XXX we do not support templating code in the value!
                                //class or id attribute value - generate fake selector with # or . prefix
                                StringBuilder buf = new StringBuilder();
                                //#180576 - filter out "illegal" characters from the selector name
                                if(text.indexOf(".") == -1 && text.indexOf(":") == -1) {
                                    buf.append("\n ");
                                    buf.append(HTMLTokenId.VALUE_CSS_TOKEN_TYPE_CLASS.equals(valueCssType) ? "." : "#");
                                    embeddings.add(snapshot.create(buf.toString(), CSS_MIME_TYPE));
                                    embeddings.add(snapshot.create(sourceStart, sourceEnd - sourceStart, CSS_MIME_TYPE));
                                    embeddings.add(snapshot.create("{}", CSS_MIME_TYPE));
                                }

                            } else {
                                //style attribute value - wrap with a fake selector
                                embeddings.add(snapshot.create("\n SELECTOR {\n\t", CSS_MIME_TYPE));
                                embeddings.add(snapshot.create(sourceStart, sourceEnd - sourceStart, CSS_MIME_TYPE));

                                state.put(IN_INLINED_STYLE, Boolean.TRUE);

                                break;
                            }
                        } else if (id == HTMLTokenId.VALUE) {
                            if (isLinkTag && HREF_ATTR_NAME.equals(currentAttributeName.toLowerCase(Locale.ENGLISH))) {
                                String unquotedValue = WebUtils.unquotedValue(t.text().toString().toString());
                                //found href value, generate virtual css import
                                StringBuilder buf = new StringBuilder();
                                buf.append("@import \""); //NOI18N
                                buf.append(unquotedValue);
                                buf.append("\";"); //NOI18N
                                //insert the import at the beginning of the virtual source
                                embeddings.add(0, snapshot.create(buf, CSS_MIME_TYPE));

                            }
                        }

                    }
                }
            }
        }

    }
}
