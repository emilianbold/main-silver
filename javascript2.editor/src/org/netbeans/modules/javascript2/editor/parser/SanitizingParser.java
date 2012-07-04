/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.parser;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.javascript2.editor.jsdoc.JsDocParser;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.doc.spi.JsComment;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;

/**
 *
 * @author Petr Hejl
 */
public abstract class SanitizingParser extends Parser {

    private static final Logger LOGGER = Logger.getLogger(JsParser.class.getName());

    private JsParserResult lastResult = null;

    public SanitizingParser() {
        super();
    }

    public abstract String getDefaultScriptName();
    
    protected abstract com.oracle.nashorn.ir.FunctionNode parseSource(Snapshot snapshot, String name, String text, JsErrorManager errorManager) throws Exception;

    @Override
    public final void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        long startTime = System.currentTimeMillis();
        try {
            JsErrorManager errorManager = new JsErrorManager(snapshot.getSource().getFileObject());
            lastResult = parseSource(snapshot, event, Sanitize.NONE, errorManager);
            lastResult.setErrors(errorManager.getErrors());
        } catch (Exception ex) {
            LOGGER.log (Level.INFO, "Exception during parsing: {0}", ex);
            // TODO create empty result
            lastResult = new JsParserResult(snapshot, null);
        }
        long endTime = System.currentTimeMillis();
        LOGGER.log(Level.FINE, "Parsing took: {0} ms source: {1}", new Object[]{endTime - startTime, snapshot.getSource().getFileObject()}); //NOI18N
    }

    private JsParserResult parseSource(Snapshot snapshot, SourceModificationEvent event,
            Sanitize sanitizing, JsErrorManager errorManager) throws Exception {
        
        long startTime = System.nanoTime();
        String scriptName;
        if (snapshot.getSource().getFileObject() != null) {
            scriptName = snapshot.getSource().getFileObject().getNameExt();
        } else {
            scriptName = getDefaultScriptName();
        }
        
        int caretOffset = GsfUtilities.getLastKnownCaretOffset(snapshot, event);
        
        JsParserResult result = parseContext(new Context(scriptName, snapshot, caretOffset),
                sanitizing, errorManager);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Parsing took: {0} ms; source: {1}",
                    new Object[]{(System.nanoTime() - startTime) / 1000000, scriptName});
        }
        return result;
    }
    
    JsParserResult parseContext(Context context, Sanitize sanitizing,
            JsErrorManager errorManager) throws Exception {
        return parseContext(context, sanitizing, errorManager, true);
    }
    
    private JsParserResult parseContext(Context context, Sanitize sanitizing,
            JsErrorManager errorManager, boolean copyErrors) throws Exception {
        
        boolean sanitized = false;
        if ((sanitizing != Sanitize.NONE) && (sanitizing != Sanitize.NEVER)) {
            boolean ok = sanitizeSource(context, sanitizing, errorManager);

            if (ok) {
                sanitized = true;
                assert context.getSanitizedSource() != null;
            } else {
                // Try next trick
                return parseContext(context, sanitizing.next(), errorManager, false);
            }
        }
        
        JsErrorManager current = new JsErrorManager(context.getSnapshot().getSource().getFileObject());
        com.oracle.nashorn.ir.FunctionNode node = parseSource(context.getSnapshot(), context.getName(),
                context.getSource(), current);

        if (copyErrors) {
            errorManager.fill(current);
        }
        
        if (sanitizing != Sanitize.NEVER) {
            if (!sanitized && current.getMissingCurlyError() != null) {
                return parseContext(context, Sanitize.MISSING_CURLY, errorManager, false);
            // TODO not very clever check
            } if (node == null || !current.isEmpty()) {
                return parseContext(context, sanitizing.next(), errorManager, false);
            }
        }
        
//        // process comment elements
//        Map<Integer, ? extends JsComment> comments = Collections.<Integer, JsComment>emptyMap();
//        if (context.getSnapshot() != null) {
//            try {
//                long startTime = System.nanoTime();
//                comments = JsDocumentationParser.parse(context.getSnapshot());
//                if (LOGGER.isLoggable(Level.FINE)) {
//                    LOGGER.log(Level.FINE, "Parsing of comments took: {0} ms source: {1}",
//                            new Object[]{(System.nanoTime() - startTime) / 1000000, context.getName()});
//                }
//            } catch (Exception ex) {
//                // if anything wrong happen during parsing comments
//                LOGGER.log(Level.WARNING, null, ex);
//            }
//        }
        return new JsParserResult(context.getSnapshot(), node);
    }
    
    private boolean sanitizeSource(Context context, Sanitize sanitizing, JsErrorManager errorManager) {
        if (sanitizing == Sanitize.MISSING_CURLY) {
            org.netbeans.modules.csl.api.Error error = errorManager.getMissingCurlyError();
            if (error != null) {
                String source = context.getOriginalSource();
                int balance = 0;
                for (int i = 0; i < source.length(); i++) {
                    char current = source.charAt(i);
                    if (current == '{') { // NOI18N
                        balance++;
                    } else if (current == '}') { // NOI18N
                        balance--;
                    }
                }
                if (balance != 0) {
                    StringBuilder builder = new StringBuilder(source);
                    if (balance < 0) {
                        while (balance < 0) {
                            int index = builder.lastIndexOf("}"); // NOI18N
                            if (index < 0) {
                                break;
                            }
                            erase(builder, index, index + 1);
                            balance++;
                        }
                    } else if (balance > 0 && error.getStartPosition() >= source.length()) {
                        while (balance > 0) {
                            builder.append('}'); // NOI18N
                            balance--;
                        }
                    }
                    context.setSanitizedSource(builder.toString());
                    context.setSanitization(sanitizing);
                    return true;
                }
            }
        } else if (sanitizing == Sanitize.SYNTAX_ERROR_CURRENT) {
            List<? extends org.netbeans.modules.csl.api.Error> errors = errorManager.getErrors();
            if (!errors.isEmpty()) {
                org.netbeans.modules.csl.api.Error error = errors.get(0);
                int offset = error.getStartPosition();
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(
                        context.getSnapshot(), 0);
                if (ts != null) {
                    ts.move(offset);
                    if (ts.moveNext()) {
                        int start = ts.offset();
                        if (start >= 0 && ts.moveNext()) {
                            int end = ts.offset();
                            StringBuilder builder = new StringBuilder(context.getOriginalSource());
                            erase(builder, start, end);
                            context.setSanitizedSource(builder.toString());
                            context.setSanitization(sanitizing);
                            return true;
                        }
                    }
                }
            }
        } else if (sanitizing == Sanitize.SYNTAX_ERROR_PREVIOUS) {
            List<? extends org.netbeans.modules.csl.api.Error> errors = errorManager.getErrors();
            if (!errors.isEmpty()) {
                org.netbeans.modules.csl.api.Error error = errors.get(0);
                int offset = error.getStartPosition();
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(
                        context.getSnapshot(), 0);
                if (ts != null) {
                    ts.move(offset);
                    if (ts.moveNext()) {
                        int start = -1;
                        while (ts.movePrevious()) {
                            if (ts.token().id() != JsTokenId.WHITESPACE
                                    && ts.token().id() != JsTokenId.EOL
                                    && ts.token().id() != JsTokenId.DOC_COMMENT
                                    && ts.token().id() != JsTokenId.LINE_COMMENT
                                    && ts.token().id() != JsTokenId.BLOCK_COMMENT) {

                                start = ts.offset();
                                break;
                            }
                        }
                        if (start >= 0 && ts.moveNext()) {
                            int end = ts.offset();
                            StringBuilder builder = new StringBuilder(context.getOriginalSource());
                            erase(builder, start, end);
                            context.setSanitizedSource(builder.toString());
                            context.setSanitization(sanitizing);
                            return true;
                        }
                    }
                }
            }
        } else if (sanitizing == Sanitize.ERROR_LINE) {
            List<? extends org.netbeans.modules.csl.api.Error> errors = errorManager.getErrors();
            if (!errors.isEmpty()) {
                org.netbeans.modules.csl.api.Error error = errors.get(0);
                int offset = error.getStartPosition();
                sanitizeLine(sanitizing, context, offset);
            }
        } else if (sanitizing == Sanitize.EDITED_LINE) {
            int offset = context.getCaretOffset();
            sanitizeLine(sanitizing, context, offset);
        }
        return false;
    }

    private boolean sanitizeLine(Sanitize sanitizing, Context context, int offset) {
        if (offset > -1) {
            String source = context.getOriginalSource();
            int start = offset > 0 ? offset - 1 : offset;
            int end = start + 1;
            // fix until new line or }
            char c = source.charAt(start);
            while (start > 0 && c != '\n' && c != '\r' && c != '{' && c != '}') { // NOI18N
                c = source.charAt(--start);
            }
            start++;
            if (end < source.length()) {
                c = source.charAt(end);
                while (end < source.length() && c != '\n' && c != '\r' && c != '{' && c != '}') { // NOI18N
                    c = source.charAt(end++);
                }
            }

            StringBuilder builder = new StringBuilder(context.getOriginalSource());
            erase(builder, start, end);
            context.setSanitizedSource(builder.toString());
            context.setSanitization(sanitizing);
            return true;
        }
        return false;
    }
    
    @Override
    public final Result getResult(Task task) throws ParseException {
        return lastResult;
    }

    @Override
    public final void addChangeListener(ChangeListener changeListener) {
        LOGGER.log(Level.FINE, "Adding changeListener: {0}", changeListener); //NOI18N)
    }

    @Override
    public final void removeChangeListener(ChangeListener changeListener) {
        LOGGER.log(Level.FINE, "Removing changeListener: {0}", changeListener); //NOI18N)
    }

    private static void erase(StringBuilder builder, int start, int end) {
        builder.delete(start, end);
        for (int i = start; i < end; i++) {
            builder.insert(i, ' ');
        }
    }
    
    /**
     * Parsing context
     */
    static class Context {

        private final String name;
        
        private final Snapshot snapshot;

        private final int caretOffset;
        
        private String source;

        private String sanitizedSource;

        private Sanitize sanitization;

        public Context(String name, Snapshot snapshot, int caretOffset) {
            this.name = name;
            this.snapshot = snapshot;
            this.caretOffset = caretOffset;
        }

        public String getName() {
            return name;
        }

        public Snapshot getSnapshot() {
            return snapshot;
        }

        public int getCaretOffset() {
            return caretOffset;
        }

        public String getSource() {
            if (sanitizedSource != null) {
                return sanitizedSource;
            }
            return getOriginalSource();
        }

        public String getOriginalSource() {
            if (source == null) {
                source = snapshot.getText().toString();
            }
            return source;
        }
        
        public String getSanitizedSource() {
            return sanitizedSource;
        }

        public void setSanitizedSource(String sanitizedSource) {
            this.sanitizedSource = sanitizedSource;
        }

        public Sanitize getSanitization() {
            return sanitization;
        }

        public void setSanitization(Sanitize sanitization) {
            this.sanitization = sanitization;
        }

    }

    /** Attempts to sanitize the input buffer */
    public static enum Sanitize {
        /** Only parse the current file accurately, don't try heuristics */
        NEVER {

            @Override
            public Sanitize next() {
                return NEVER;
            }
        },

        /** Perform no sanitization */
        NONE {

            @Override
            public Sanitize next() {
                return MISSING_CURLY;
            }
        },
        
        /** Attempt to fix missing } */
        MISSING_CURLY {

            @Override
            public Sanitize next() {
                return SYNTAX_ERROR_CURRENT;
            }
        },
        
        /** Remove current error token */
        SYNTAX_ERROR_CURRENT {

            @Override
            public Sanitize next() {
                return SYNTAX_ERROR_PREVIOUS;
            }
        },
        
        /** Remove token before error */
        SYNTAX_ERROR_PREVIOUS {

            @Override
            public Sanitize next() {
                return SYNTAX_ERROR_PREVIOUS_LINE;
            }
        },
        
        /** remove line with error */
        SYNTAX_ERROR_PREVIOUS_LINE {

            @Override
            public Sanitize next() {
                return SYNTAX_ERROR_BLOCK;
            }
        },
        
        /** try to delete the whole block, where is the error*/
        SYNTAX_ERROR_BLOCK {

            @Override
            public Sanitize next() {
                return EDITED_DOT;
            }
        },
        
        /** Try to remove the trailing . or :: at the caret line */
        EDITED_DOT {

            @Override
            public Sanitize next() {
                return ERROR_DOT;
            }
        },
        
        /** 
         * Try to remove the trailing . or :: at the error position, or the prior
         * line, or the caret line
         */
        ERROR_DOT {

            @Override
            public Sanitize next() {
                return BLOCK_START;
            }
        },
        /** 
         * Try to remove the initial "if" or "unless" on the block
         * in case it's not terminated
         */
        BLOCK_START {

            @Override
            public Sanitize next() {
                return ERROR_LINE;
            }
        },
        
        /** Try to cut out the error line */
        ERROR_LINE {

            @Override
            public Sanitize next() {
                return EDITED_LINE;
            }
        },
        
        /** Try to cut out the current edited line, if known */
        EDITED_LINE {

            @Override
            public Sanitize next() {
                return NEVER;
            }
        };

        
        public abstract Sanitize next();
    }

}
