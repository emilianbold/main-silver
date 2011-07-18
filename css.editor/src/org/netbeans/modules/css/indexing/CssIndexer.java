/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.indexing;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.css.editor.csl.CssLanguage;
import org.netbeans.modules.css.editor.csl.CssParserResultCslWrapper;
import org.netbeans.modules.css.refactoring.api.Entry;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Css content indexer
 *
 * @author mfukala@netbeans.org
 */
public class CssIndexer extends EmbeddingIndexer {

    private static final Logger LOGGER = Logger.getLogger(CssIndexer.class.getSimpleName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);

    public static final String CSS_CONTENT_KEY = "cssContent"; //NOI18N
    public static final String IMPORTS_KEY = "imports"; //NOI18N
    public static final String IDS_KEY = "ids"; //NOI18N
    public static final String CLASSES_KEY = "classes"; //NOI18N
    public static final String HTML_ELEMENTS_KEY = "htmlElements"; //NOI18N
    public static final String COLORS_KEY = "colors"; //NOI18N

    //used during the indexing (content is mutable)
    private static final Map<FileObject, AtomicLong> importsHashCodes = new HashMap<FileObject, AtomicLong>();
    
    //final version used after the indexing finishes (immutable)
    private static Map<FileObject, AtomicLong> computedImportsHashCodes = new HashMap<FileObject, AtomicLong>();
    
//    static {
//	LOG.setLevel(Level.ALL);
//    }
    @Override
    protected void index(Indexable indexable, Result parserResult, Context context) {
        try {
            if(LOG) {
                FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
                LOGGER.log(Level.FINE, "indexing {0}", fo.getPath()); //NOI18N
            }

            CssParserResultCslWrapper wrapper = (CssParserResultCslWrapper) parserResult;
            CssFileModel model = CssFileModel.create(wrapper.getWrappedCssParserResult());
            IndexingSupport support = IndexingSupport.getInstance(context);
            IndexDocument document = support.createDocument(indexable);

            storeEntries(model.getIds(), document, IDS_KEY);
            storeEntries(model.getClasses(), document, CLASSES_KEY);
            storeEntries(model.getHtmlElements(), document, HTML_ELEMENTS_KEY);
            storeEntries(model.getColors(), document, COLORS_KEY);
            
            //support for caching the file dependencies
            int entriesHashCode = storeEntries(model.getImports(), document, IMPORTS_KEY);
            FileObject root = context.getRoot();
            AtomicLong aggregatedHash = importsHashCodes.get(root);
            if(aggregatedHash == null) {
                aggregatedHash = new AtomicLong(0);
                importsHashCodes.put(root, aggregatedHash);
            } 
            aggregatedHash.set(aggregatedHash.get() * 79 + entriesHashCode);
            
            //this is a marker key so it's possible to find
            //all stylesheets easily
            document.addPair(CSS_CONTENT_KEY, Boolean.TRUE.toString(), true, true);

            support.addDocument(document);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    //1. no synchronization on the computedImportsHashCodes!
    //2. the callers of this method will get old results if an indexing is in progress and 
    //   if the cached hashcode changes - possibly add some kind of synchronization 
    //   to that call (but it seems too much error prone to me)
    public static long getImportsHashCodeForRoots(Collection<FileObject> roots) {
        long hash = 5;
        for(FileObject root : roots) {
            AtomicLong rootHash = computedImportsHashCodes.get(root);
            if(rootHash != null) {
                hash = hash * 51 + rootHash.longValue();
            }
        }
        return hash;
    }

    private int storeEntries(Collection<Entry> entries, IndexDocument doc, String key) {
        if (!entries.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            Iterator<Entry> i = entries.iterator();
            while (i.hasNext()) {
                sb.append(i.next().getName());
                if (i.hasNext()) {
                    sb.append(','); //NOI18N
                }
            }
            sb.append(';'); //end of string
            doc.addPair(key, sb.toString(), true, true);
            return sb.toString().hashCode();
        }
        return 0;
    }
    

    public static class Factory extends EmbeddingIndexerFactory {

        static final String NAME = "css"; //NOI18N
        static final int VERSION = 1;

        @Override
        public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
            if (isIndexable(snapshot)) {
                return new CssIndexer();
            } else {
                return null;
            }
        }

        @Override
        public boolean scanStarted(Context context) {
            importsHashCodes.remove(context.getRoot()); //remove the computed hashcode for the given indexing root
            return super.scanStarted(context);
        }

        
        @Override
        public void scanFinished(Context context) {
            computedImportsHashCodes = new HashMap<FileObject, AtomicLong>(importsHashCodes); //shallow copy
            super.scanFinished(context);
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            try {
                IndexingSupport is = IndexingSupport.getInstance(context);
                for(Indexable i : deleted) {
                    is.removeDocuments(i);
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, ioe);
            }
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            try {
                IndexingSupport is = IndexingSupport.getInstance(context);
                for(Indexable i : dirty) {
                    is.markDirtyDocuments(i);
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, ioe);
            }
        }

        @Override
        public String getIndexerName() {
            return NAME;
        }

        @Override
        public int getIndexVersion() {
            return VERSION;
        }

        private boolean isIndexable(Snapshot snapshot) {
            //index all files possibly containing css
            return CssLanguage.CSS_MIME_TYPE.equals(snapshot.getMimeType());
        }
    }
}
