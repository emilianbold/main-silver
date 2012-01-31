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
package org.netbeans.modules.css.model.api;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.diff.Difference;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.model.ModelAccess;
import org.netbeans.modules.css.model.impl.ElementFactoryImpl;
import org.netbeans.modules.css.model.impl.ModelElementContext;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.util.Lookup;
import org.openide.util.Mutex;

/**
 * Model for CSS3 source
 *
 * TBD - specify the document - parser result - model flow && locking mechanism
 *
 * TODO possibly harden the model access conditions... just warning now in one
 * or two methods
 *
 * @author marekfukala
 */
public final class Model {

    private static final Logger LOGGER = Logger.getLogger(Model.class.getName());
    private final Mutex MODEL_MUTEX = new Mutex();
    private final CharSequence source;
    private final Node styleSheetNode;
    private final StyleSheet styleSheet;

    /**
     * Creates a new model with empty stylesheet
     */
    public Model() {
        source = "";
        styleSheetNode = null;

        styleSheet = getElementFactory().createStyleSheet();
    }

    public Model(CssParserResult parserResult) {
        this(parserResult.getSnapshot(), NodeUtil.query(parserResult.getParseTree(), NodeType.styleSheet.name())); //NOI18N
    }

    /**
     * Creates a model instance of given source and parser node
     *
     * @param snapshot
     * @param styleSheetNode
     */
    public Model(Snapshot snapshot, Node styleSheetNode) {
        this(snapshot.getText(), styleSheetNode);
    }

    /**
     * Creates a model instance of given source and parser node
     *
     * @param snapshot
     * @param styleSheetNode
     */
    public Model(CharSequence source, Node styleSheetNode) {
        this.source = source;
        this.styleSheetNode = styleSheetNode;

        ModelElementContext ctx = new ModelElementContext(source, styleSheetNode);
        this.styleSheet = (StyleSheet) getElementFactoryImpl().createElement(ctx);
    }

    /**
     * Any client wanting to access the model for reading should do it via
     * posting its ModelTask to this method. Any model access outside may cause
     * client may obtain corrupted data.
     *
     * @param runnable
     */
    public void runReadTask(final ModelTask runnable) {
        MODEL_MUTEX.readAccess(new Runnable() {

            @Override
            public void run() {
                runnable.run(Model.this);
            }
        });
    }

    /**
     * Any client wanting to access the model for writing should do it via
     * posting its ModelTask to this method. Any model access outside may cause
     * client may obtain corrupted data and cause data corruption to other
     * clients accessing the model.
     *
     * @param runnable
     */
    public void runWriteTask(final ModelTask runnable) {
        MODEL_MUTEX.writeAccess(new Runnable() {

            @Override
            public void run() {
                runnable.run(Model.this);
            }
        });
    }

    private void checkModelAccess() {
        if (ModelAccess.checkModelAccess) {
            if (!(MODEL_MUTEX.isReadAccess() || MODEL_MUTEX.isWriteAccess())) {
                LOGGER.log(Level.WARNING, "Model access outside of Model.runRead/WriteTask()!", new IllegalAccessException());
            }
        }
    }

    public StyleSheet getStyleSheet() {
        checkModelAccess();
        return styleSheet;
    }

    public CharSequence getOriginalSource() {
        return source;
    }

    /**
     * Returns the modified source upon changes done to the model.
     */
    public CharSequence getModelSource() {
        return getElementSource(getStyleSheet());
    }

    /**
     * Returns the modified piece of source upon changes done to the model
     * corresponding to the scope of the given element.
     */
    public CharSequence getElementSource(Element element) {
        checkModelAccess();
        StringBuilder b = new StringBuilder();
        getElementSource(element, b);
        return b;
    }

    private void getElementSource(Element e, StringBuilder b) {
        if (e instanceof PlainElement) {
            b.append(((PlainElement) e).getContent());
        }
        for (Iterator<Element> itr = e.childrenIterator(); itr.hasNext();) {
            Element element = itr.next();
            if (element != null) {
                getElementSource(element, b);
            }
        }
    }

    public Difference[] getModelSourceDiff() throws IOException {
        DiffProvider diffProvider = Lookup.getDefault().lookup(DiffProvider.class);

        Reader r1 = new StringReader(getOriginalSource().toString());
        Reader r2 = new StringReader(getModelSource().toString());

        Difference[] diffs = diffProvider.computeDiff(r1, r2);
        return diffs;
    }

    /**
     * Applies the changes done to the model to the original code source.
     *
     * Basically it applies all the changes obtained from {@link #getModelSourceDiff()}
     * to to given document.
     *
     * <b> It is up to the client to ensure: 1) it is the document upon which
     * source the model was build 2) the document has not changed since the
     * model creation. 3) the method is called under document atomic lock </b>
     *
     */
    public void applyChanges(Document document) throws IOException, BadLocationException {
        int sourceDelta = 0;
        for (Difference d : getModelSourceDiff()) {
            int from = LexerUtils.getLineBeginningOffset(getOriginalSource(), (d.getFirstStart() == 0 ? 0 : d.getFirstStart() - 1));
            switch (d.getType()) {

                case Difference.CHANGE:
                    int len = d.getFirstText().length();

                    document.remove(sourceDelta + from, len);
                    document.insertString(sourceDelta + from, d.getSecondText(), null);

                    int insertLen = d.getSecondText().length();
                    sourceDelta += insertLen - len;
                    break;

                case Difference.ADD:
                    from = LexerUtils.getLineBeginningOffset(getOriginalSource(), d.getFirstStart());
                    len = d.getSecondText().length();
                    document.insertString(sourceDelta + from, d.getSecondText(), null);
                    sourceDelta += len;
                    break;

                case Difference.DELETE:
                    len = d.getFirstText().length();
                    document.remove(sourceDelta + from, len);
                    sourceDelta -= len;
                    break;
            }

        }


    }

    /**
     * Returns an instance of {@link ElementFactory}.
     */
    public ElementFactory getElementFactory() {
        return getElementFactoryImpl();
    }

    private ElementFactoryImpl getElementFactoryImpl() {
        //possibly get instance from lookup, but ... who'd need that?
        return ElementFactoryImpl.getDefault();
    }

    public static interface ModelTask {

        public void run(Model model);
        
    }
    
}
