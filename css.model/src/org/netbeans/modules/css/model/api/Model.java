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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.css.live.LiveUpdater;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.model.ModelAccess;
import org.netbeans.modules.css.model.impl.ElementFactoryImpl;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.Lookups;

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

    //property names:
    public static final String CHANGES_APPLIED_TO_DOCUMENT = "changes.applied"; //NOI18N
    
    
    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    private static final Logger LOGGER = Logger.getLogger(Model.class.getName());

    private final Mutex MODEL_MUTEX = new Mutex();

    private Lookup MODEL_LOOKUP;
    private ElementFactory ELEMENT_FACTORY;
    
    private static final Map<CssParserResult, Reference<Model>> PR_MODEL_CACHE
            = new WeakHashMap<CssParserResult, Reference<Model>>();
    
    /**
     * Gets the Model instance for given CssParserResult.
     * 
     * This is the basic way how clients should obtain the Css Source Model.
     * 
     * The returned model instance can be cached to the given parsing result.
     * 
     * <b>This method should be called under the parsing lock as the parser 
     * result task should not escape the UserTask</b>
     * 
     * @param parserResult
     * 
     * @return an instance of the Css Source Model
     */
    public static synchronized Model getModel(CssParserResult parserResult) {
        //try cache first
        Reference<Model> cached_ref = PR_MODEL_CACHE.get(parserResult);
        if(cached_ref != null) {
            Model cached = cached_ref.get();
            if(cached != null) {
                return cached;
            }
        }
        
        //recreate
        Model model = new Model(parserResult);
        
        //cache
        PR_MODEL_CACHE.put(parserResult, new WeakReference<Model>(model));
        
        return model;
    }
    
    
    /**
     * Creates a new model with empty stylesheet
     */
    public Model() {
        MODEL_LOOKUP = Lookups.fixed(
                getElementFactory().createStyleSheet()
                );
    }

    public Model(CssParserResult parserResult) {
        Node styleSheetNode = NodeUtil.query(parserResult.getParseTree(), NodeType.styleSheet.name());
        
        Collection<Object> lookupContent = new ArrayList<Object>();
        if(styleSheetNode == null) {
            //empty file
            lookupContent.add(getElementFactory().createStyleSheet());
        } else {
            lookupContent.add(styleSheetNode);
            lookupContent.add((StyleSheet) getElementFactoryImpl(this).createElement(this, styleSheetNode));
        }
        
        Snapshot snapshot = parserResult.getSnapshot();
        Source source = snapshot.getSource();
        FileObject file = source.getFileObject();
        Document doc = source.getDocument(true);
        
        lookupContent.add(parserResult);
        lookupContent.add(snapshot);
        lookupContent.add(snapshot.getText());
        if(file != null) {
            lookupContent.add(file);
        }
        if(doc != null) {
            lookupContent.add(doc);
        }
        
        MODEL_LOOKUP = Lookups.fixed(lookupContent.toArray());
    }

    /**
     * Creates a model instance of given source and parser node
     *
     * @param snapshot
     * @param styleSheetNode
     */
    public Model(CharSequence source, Node styleSheetNode) {
        StyleSheet styleSheet = (StyleSheet) getElementFactoryImpl(this).createElement(this, styleSheetNode);
        
        MODEL_LOOKUP = Lookups.fixed(
                source,
                styleSheetNode,
                styleSheet);

    }

    public Lookup getLookup() {
        return MODEL_LOOKUP;
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
                runnable.run(Model.this.getStyleSheet());
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
                runnable.run(Model.this.getStyleSheet());
            }
        });
    }

   

    public CharSequence getOriginalSource() {
        return getLookup().lookup(CharSequence.class);
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
     * This method will throw an exception if the model instance is not created
     * from a CssParserResult based on a document.
     *
     * Basically it applies all the changes obtained from {@link #getModelSourceDiff()}
     * to to given document.
     *
     * <b> It is up to the client to ensure: 1) it is the document upon which
     * source the model was build 2) the document has not changed since the
     * model creation. 3) the method is called under document atomic lock </b>
     *
     */
    public void applyChanges() throws IOException, BadLocationException {
        Document doc = getLookup().lookup(Document.class);
        if(doc == null) {
            throw new IOException("Not document based model instance!"); //NOI18N
        }
        
        Snapshot snapshot = getLookup().lookup(Snapshot.class);
        applyChanges(doc, new SnapshotOffsetConvertor(snapshot));
        
        support.firePropertyChange(CHANGES_APPLIED_TO_DOCUMENT, null, null);
    }
    
    
    /**
     * Applies the changes done to the model to a document instance.
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
        applyChanges(document, DIRECT_OFFSET_CONVERTOR);
    }
    
    

    /**
     * Returns an instance of {@link ElementFactory}.
     */
    public synchronized ElementFactory getElementFactory() {
        if(ELEMENT_FACTORY == null) {
            ELEMENT_FACTORY = getElementFactoryImpl(this);
        }
        return ELEMENT_FACTORY;
    }

    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
    
    /**
     * Allows clients to read/write access the css source model
     */
    public static interface ModelTask {

        /**
         * This method is called within the model lock when one 
         * calls Model.runRead/WriteTask
         * 
         * @param styleSheet the stylesheet object representing
         * the model source. May be used for reading or modifying 
         * the source.
         */
        public void run(StyleSheet styleSheet);
        
    }
    
    // ---------------------- private -------------------------
    
    //for tests only
    StyleSheet getStyleSheet() {
        checkModelAccess();
        return getLookup().lookup(StyleSheet.class);
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
    
    private void checkModelAccess() {
        if (ModelAccess.checkModelAccess) {
            if (!(MODEL_MUTEX.isReadAccess() || MODEL_MUTEX.isWriteAccess())) {
                LOGGER.log(Level.WARNING, "Model access outside of Model.runRead/WriteTask()!", new IllegalAccessException());
            }
        }
    }

    
    private void applyChanges(Document document, OffsetConvertor convertor) throws IOException, BadLocationException {
        int sourceDelta = 0;
        for (Difference d : getModelSourceDiff()) {
            int firstStart = d.getFirstStart();
            int from = convertor.getOriginalOffset(LexerUtils.getLineBeginningOffset(getOriginalSource(), (firstStart == 0 ? 0 : firstStart - 1)));
            switch (d.getType()) {

                case Difference.CHANGE:
                    //Bug in internal diff workaround:
                    //
                    //if there's a change at the last line the returned 
                    //change diff contains first and second texts with endline
                    //at the text end which doesn't exist.
                    //
                    //caused by a bug at HuntDiff:284-298
                    
                    String first = d.getFirstText();
                    String second = d.getSecondText();
                    
                    if(first.endsWith("\n") && second.endsWith("\n")) {
                        first = first.substring(0, first.length() - 1);
                        second = second.substring(0, second.length() - 1);
                    }
                    
                    int len = first.length();

                    document.remove(sourceDelta + from, len);
                    document.insertString(sourceDelta + from, second, null);

                    int insertLen = second.length();
                    sourceDelta += insertLen - len;
                    break;

                case Difference.ADD:
                    from = convertor.getOriginalOffset(LexerUtils.getLineBeginningOffset(getOriginalSource(), d.getFirstStart()));
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
        saveIfNotOpenInEditor(document);
    }

    //>>> TEMPORARY HACK - TO BE REMOVED FROM THE MODULE !!! 
    private static void saveIfNotOpenInEditor(Document document) throws IOException {
        DataObject dataObject = getDataObject(document);
        if (dataObject == null) {
            LOGGER.log(Level.FINE, "Cannot find DataObject for document " + document );
            return;
        }
        final LiveUpdater liveUpdater = Lookup.getDefault().lookup(LiveUpdater.class);
        EditorCookie ec = dataObject.getLookup().lookup(EditorCookie.class);
        if (ec!=null && ec.getOpenedPanes() == null) {
            //file not open in any editor
            SaveCookie save = dataObject.getLookup().lookup(SaveCookie.class);
            if (save!=null) {
                save.save();
            }
            liveUpdater.update(document);
        } else {
            if (!ec.getOpenedPanes()[0].equals(EditorRegistry.lastFocusedComponent())) {
                liveUpdater.update(document);
            }
        }
    }
    
    private static DataObject getDataObject(Document doc) {
        Object sdp = doc == null ? null : doc.getProperty(Document.StreamDescriptionProperty);
        if (sdp instanceof DataObject) {
            return (DataObject)sdp;
        }
        return null;
    }
    //<<<

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString())
                .append("file=")
                .append(getLookup().lookup(FileObject.class))
                .toString();
    }
    
    private static ElementFactoryImpl getElementFactoryImpl(Model model) {
        return new ElementFactoryImpl(model);
    }

    private static interface OffsetConvertor {
        
        public int getOriginalOffset(int documentOffset);
        
    }
    
    private static final OffsetConvertor DIRECT_OFFSET_CONVERTOR = new OffsetConvertor() {

        @Override
        public int getOriginalOffset(int documentOffset) {
            return documentOffset;
        }
        
    };
    
    private static class SnapshotOffsetConvertor implements OffsetConvertor {

        private Snapshot snapshot;

        public SnapshotOffsetConvertor(Snapshot snapshot) {
            this.snapshot = snapshot;
        }
        
        @Override
        public int getOriginalOffset(int embeddedOffset) {
            return snapshot.getOriginalOffset(embeddedOffset);
        }
        
    }
    
}
