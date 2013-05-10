/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.embedding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.editor.Utils;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 *
 * @author marekfukala
 */
@EmbeddingProvider.Registration(
        mimeType = "text/html",
        targetMimeType = "text/javascript")
public class JsEmbeddingProvider extends EmbeddingProvider {
//@EmbeddingProvider.Registration(
//        mimeType = "text/html",
//        targetMimeType = "text/javascript")
//public class JsEmbeddingProvider extends ParserBasedEmbeddingProvider<HtmlParserResult> {

    private static final Logger LOGGER = Logger.getLogger(JsEmbeddingProvider.class.getSimpleName());
    private static final String JS_MIMETYPE = "text/javascript"; //NOI18N
    private static final String NETBEANS_IMPORT_FILE = "__netbeans_import__"; // NOI18N
    private boolean cancelled = true;
    private final Language JS_LANGUAGE;
    private final JsEPPluginQuery PLUGINS;

    public JsEmbeddingProvider() {
        JS_LANGUAGE = Language.find(JS_MIMETYPE); //NOI18N
        PLUGINS = JsEPPluginQuery.getDefault();
    }
    
//    @Override
//    public Class<? extends Scheduler> getSchedulerClass() {
//        return null;
//    }
//
//    @Override
//    public List<Embedding> getEmbeddings(HtmlParserResult result) {
    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
//        Snapshot snapshot = result.getSnapshot();
        if (snapshot.getMimePath().size() > 1) {
            //do not create any js embeddings in already embedded html code
            //another js embedding provider for such cases exists in 
            //javascript2.editor module.
            return Collections.emptyList();
        }

        cancelled = false; //resume
        List<Embedding> embeddings = new ArrayList<>();
        TokenSequence<HTMLTokenId> tokenSequence = snapshot.getTokenHierarchy().tokenSequence(HTMLTokenId.language());
        JsAnalyzerState state = new JsAnalyzerState();
        
//        process(result, snapshot, tokenSequence, state, embeddings);
        process(snapshot, tokenSequence, state, embeddings);
        if (embeddings.isEmpty()) {
            LOGGER.log(Level.FINE, "No javascript embedding created for source {0}", //NOI18N
                    snapshot.getSource().toString());
            return Collections.<Embedding>emptyList();
        } else {
            Embedding embedding = Embedding.create(embeddings);
            LOGGER.log(Level.FINE, "Javascript embedding for source {0}:\n{1}",
                    new Object[]{snapshot.getSource().toString(), embedding.getSnapshot().getText().toString()});
            return Collections.singletonList(embedding);

        }
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

//    private void process(HtmlParserResult parserResult, Snapshot snapshot, TokenSequence<HTMLTokenId> ts, JsAnalyzerState state, List<Embedding> embeddings) {
    private void process(Snapshot snapshot, TokenSequence<HTMLTokenId> ts, JsAnalyzerState state, List<Embedding> embeddings) {
        JsEPPluginQuery.Session session = PLUGINS.createSession();
//        session.startProcessing(parserResult, snapshot, ts, embeddings);
        session.startProcessing(snapshot, ts, embeddings);
        try {
            ts.moveStart();

            while (ts.moveNext()) {
                if (cancelled) {
                    embeddings.clear();
                    return;
                }

                //plugins
                if (session.processToken()) {
                    //the plugin already processed the token so we should? not? process it anymore ... that's a question
                    continue;
                }

                Token<HTMLTokenId> token = ts.token();
                switch (token.id()) {
                    case SCRIPT:
                        handleScript(snapshot, ts, state, embeddings);
                        break;
                    case TAG_OPEN:
                        handleOpenTag(snapshot, ts, embeddings);
                        break;
                    case TEXT:
                        if (state.in_javascript) {
                            embeddings.add(snapshot.create(ts.offset(), token.length(), JS_MIMETYPE));
                        }
                        break;
                    case VALUE_JAVASCRIPT:
                    case VALUE:
                        handleValue(snapshot, ts, embeddings);
                        break;
                    case TAG_CLOSE:
                        if (LexerUtils.equals("script", token.text(), true, true)) {
                            embeddings.add(snapshot.create("\n", JS_MIMETYPE)); //NOI18N
                        }
                        break;
                    case EL_OPEN_DELIMITER:
                        handleELOpenDelimiter(snapshot, ts, embeddings);
                        break;
                    default:
                        state.in_javascript = false;
                        break;
                }
            }
        } finally {
            session.endProcessing();
        }
    }

    //VALUE_JAVASCRIPT token always has text/javascript embedding
    //VALUE token MAY have text/javascript embedding (provided by HtmlLexerPlugin) or by dynamic embedding creation
    private void handleValue(Snapshot snapshot, TokenSequence<HTMLTokenId> ts, List<Embedding> embeddings) {
        if (ts.embedded(JS_LANGUAGE) != null) {
            //has javascript embedding
            embeddings.add(snapshot.create("(function(){\n", JS_MIMETYPE)); //NOI18N
            int diff = Utils.isAttributeValueQuoted(ts.token().text()) ? 1 : 0;
            embeddings.add(snapshot.create(ts.offset() + diff, ts.token().length() - diff * 2, JS_MIMETYPE));
            embeddings.add(snapshot.create("\n});\n", JS_MIMETYPE)); //NOI18N
        }
    }

    private void handleELOpenDelimiter(Snapshot snapshot, TokenSequence<HTMLTokenId> ts, List<Embedding> embeddings) {
        //1.check if the next token represents javascript content
        String mimetype = (String) ts.token().getProperty("contentMimeType"); //NOT IN AN API, TBD
        if (mimetype != null && "text/javascript".equals(mimetype)) {
            embeddings.add(snapshot.create("(function(){\n", JS_MIMETYPE)); //NOI18N

            //2. check content
            if (ts.moveNext()) {
                if (ts.token().id() == HTMLTokenId.EL_CONTENT) {
                    //not empty expression: {{sg}}
                    embeddings.add(snapshot.create(ts.offset(), ts.token().length(), JS_MIMETYPE));
                    embeddings.add(snapshot.create(";\n});\n", JS_MIMETYPE)); //NOI18N
                } else if (ts.token().id() == HTMLTokenId.EL_CLOSE_DELIMITER) {
                    //empty expression: {{}}
                    embeddings.add(snapshot.create(ts.offset(), 0, JS_MIMETYPE));
                    embeddings.add(snapshot.create(";\n});\n", JS_MIMETYPE)); //NOI18N
                }
            }
        }
    }

    private void handleScript(Snapshot snapshot, TokenSequence<HTMLTokenId> ts, JsAnalyzerState state, List<Embedding> embeddings) {
        String scriptType = (String) ts.token().getProperty(HTMLTokenId.SCRIPT_TYPE_TOKEN_PROPERTY);
        if (scriptType == null || "text/javascript".equals(scriptType)) {
            state.in_javascript = true;
            // Emit the block verbatim
            int sourceStart = ts.offset();
            String text = ts.token().text().toString();
            List<EmbeddingPosition> jsEmbeddings = extractJsEmbeddings(text, sourceStart);
            for (EmbeddingPosition embedding : jsEmbeddings) {
                embeddings.add(snapshot.create(embedding.getOffset(), embedding.getLength(), JS_MIMETYPE));
            }
        }
    }

    private void handleOpenTag(Snapshot snapshot, TokenSequence<HTMLTokenId> ts, List<Embedding> embeddings) {
        // TODO - if we see a <script src="someurl"> block that also
        // has a nonempty body, warn - the body will be ignored!!
        // (This should be a quickfix)
        if (LexerUtils.equals("script", ts.token().text(), false, false)) {
            // Look for "<script src=" and if found, locate any includes.
            // Quit when I find TAG_CLOSE or run out of tokens
            // (for files with errors)
            TokenSequence<? extends HTMLTokenId> ets = ts.subSequence(ts.offset());
            ets.moveStart();
            boolean foundSrc = false;
            boolean foundType = false;
            String type = null;
            String src = null;
            while (ets.moveNext()) {
                Token<? extends HTMLTokenId> t = ets.token();
                HTMLTokenId id = t.id();
                // TODO - if we see a DEFER attribute here record that somehow
                // such that I can have a quickfix look to make sure you don't try
                // to mess with the document!
                if (id == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                    break;
                } else if (foundSrc || foundType) {
                    if (id == HTMLTokenId.ARGUMENT) {
                        break;
                    } else if (id == HTMLTokenId.VALUE) {
                        // Found a script src
                        if (foundSrc) {
                            src = t.text().toString();
                        } else {
                            assert foundType;
                            type = t.text().toString();
                        }
                        foundSrc = false;
                        foundType = false;
                    }
                } else if (id == HTMLTokenId.ARGUMENT) {
                    String val = t.text().toString();
                    switch (val) {
                        case "src":
                            foundSrc = true;
                            break;
                        case "type":
                            foundType = true;
                            break;
                    }
                }
            }
            if (src != null) {
                if (type == null || type.toLowerCase().indexOf("javascript") != -1) {
                    if (src.length() > 2 && src.startsWith("\"") && src.endsWith("\"")) {
                        src = src.substring(1, src.length() - 1);
                    }
                    if (src.length() > 2 && src.startsWith("'") && src.endsWith("'")) {
                        src = src.substring(1, src.length() - 1);
                    }

                    // Insert a file link
                    String insertText = NETBEANS_IMPORT_FILE + "('" + src + "');\n"; // NOI18N
                    embeddings.add(snapshot.create(insertText, JS_MIMETYPE));
                }
            }
        }

    }

    private List<EmbeddingPosition> extractJsEmbeddings(String text, int sourceStart) {
        List<EmbeddingPosition> embeddings = new LinkedList<>();
        // beginning comment around the script
        int start = 0;
        for (; start < text.length(); start++) {
            char c = text.charAt(start);
            if (!Character.isWhitespace(c)) {
                break;
            }
        }
        if (start < text.length() && text.startsWith("<!--", start)) { //NOI18N
            int lineEnd = text.indexOf('\n', start); //NOI18N
            if (isHtmlCommentStartToSkip(text, start, lineEnd)) {
                if (start > 0) {
                    embeddings.add(new EmbeddingPosition(sourceStart, start));
                }
                lineEnd++; //skip the \n
                sourceStart += lineEnd;
                text = text.substring(lineEnd);
            }
        }

        // inline comments inside script
        Scanner scanner = new Scanner(text).useDelimiter("(<!--).*(-->)"); //NOI18N
        while (scanner.hasNext()) {
            scanner.next();
            MatchResult match = scanner.match();
            embeddings.add(new EmbeddingPosition(sourceStart + match.start(), match.group().length()));
        }
        return embeddings;
    }

    private boolean isHtmlCommentStartToSkip(String text, int start, int lineEnd) {
        if (lineEnd != -1) {
            // issue #223883 - one of suggested constructs: http://lachy.id.au/log/2005/05/script-comments (Example 4)
            if (text.startsWith("<!--//-->", start)) { //NOI18N
                return true;
            } else {
                //    embedded delimiter - issue #217081 || one line comment - issue #223883
                return (text.indexOf("-->", start) == -1 || lineEnd < text.indexOf("-->", start)); //NOI18N
            }
        } else {
            return false;
        }
    }

    private static final class JsAnalyzerState {

        boolean in_javascript = false;
    }

    protected static final class EmbeddingPosition {

        private final int offset;
        private final int length;

        public EmbeddingPosition(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }

        public int getLength() {
            return length;
        }

        public int getOffset() {
            return offset;
        }
    }
}
