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

package org.netbeans.modules.java.source.usages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.modules.java.source.util.LMListener;
import org.netbeans.modules.parsing.impl.indexing.lucene.IndexCacheFactory;
import org.netbeans.modules.parsing.impl.indexing.lucene.util.Evictable;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
//@NotTreadSafe
class LuceneIndex extends Index implements Evictable {

    private static final boolean debugIndexMerging = Boolean.getBoolean("java.index.debugMerge");     // NOI18N
    private static final boolean useMemoryCache = Boolean.getBoolean("java.index.useMemCache");       //NOI18N
    private static final String REFERENCES = "refs";    // NOI18N

    private static final Logger LOGGER = Logger.getLogger(LuceneIndex.class.getName());
    private static final String CACHE_LOCK_PREFIX = "nb-lock";  //NOI18N

    private static final RequestProcessor RP = new RequestProcessor(LuceneIndex.class.getName(), 1);

    private final File refCacheRoot;
    //@GuardedBy(this)
    private Directory directory;

    //@GuardedBy (this)
    private IndexReader reader; //Cache, do not use this dirrectly, use getReader
    private Analyzer analyzer;  //Analyzer used to store documents
    private volatile boolean closed;
    private volatile Boolean validCache;
    private Directory memCacheDir;

    static Index create (final File cacheRoot) throws IOException {
        assert cacheRoot != null && cacheRoot.exists() && cacheRoot.canRead() && cacheRoot.canWrite();
        return new LuceneIndex (getReferencesCacheFolder(cacheRoot));
    }

    /** Creates a new instance of LuceneIndex */
    private LuceneIndex (final File refCacheRoot) throws IOException {
        assert refCacheRoot != null;
        this.refCacheRoot = refCacheRoot;
        this.directory = createDirectory(this.refCacheRoot);
        PerFieldAnalyzerWrapper _analyzer = new PerFieldAnalyzerWrapper(new KeywordAnalyzer());
        _analyzer.addAnalyzer(DocumentUtil.identTerm("").field(), new WhitespaceAnalyzer());
        _analyzer.addAnalyzer(DocumentUtil.featureIdentTerm("").field(), new WhitespaceAnalyzer());
        _analyzer.addAnalyzer(DocumentUtil.caseInsensitiveFeatureIdentTerm("").field(), new DocumentUtil.LCWhitespaceAnalyzer());
        this.analyzer = _analyzer;
    }
    
    @Override
    public <T> void query (
            final @NonNull Query[] queries,
            final @NonNull FieldSelector selector,
            final @NonNull ResultConvertor<? super Document, T> convertor, 
            final @NonNull Collection<? super T> result) throws IOException, InterruptedException {
        Parameters.notNull("queries", queries);   //NOI18N
        Parameters.notNull("selector", selector);   //NOI18N
        Parameters.notNull("convertor", convertor); //NOI18N
        Parameters.notNull("result", result);       //NOI18N
        checkPreconditions();
        
        final IndexReader in = getReader();
        if (in == null) {
            LOGGER.fine(String.format("LuceneIndex[%s] is invalid!\n", this.toString()));
            return;
        }
        final AtomicBoolean _cancel = cancel.get();
        assert _cancel != null;        
        final BitSet bs = new BitSet(in.maxDoc());
        final Collector c = QueryUtil.createBitSetCollector(bs);
        final Searcher searcher = new IndexSearcher(in);
        try {
            for (Query q : queries) {
                if (_cancel.get()) {
                    throw new InterruptedException ();
                }
                searcher.search(q, c);
            }
        } finally {
            searcher.close();
        }        
        for (int docNum = bs.nextSetBit(0); docNum >= 0; docNum = bs.nextSetBit(docNum+1)) {
            if (_cancel.get()) {
                throw new InterruptedException ();
            }
            final Document doc = in.document(docNum, selector);
            try {
                final T value = convertor.convert(doc);
                if (value != null) {
                    result.add (value);
                }
            } catch (ResultConvertor.Stop stop) {
                //Stop not supported not needed
            }
        }
    }
    
    @Override
    public <T> void queryTerms(
            final @NullAllowed Term seekTo,
            final @NonNull ResultConvertor<Term,T> filter,
            final @NonNull Collection<? super T> result) throws IOException, InterruptedException {
        
        checkPreconditions();        
        final IndexReader in = getReader();
        if (in == null) {
            return;
        }
        final AtomicBoolean _cancel = cancel.get();
        assert _cancel != null;

        final TermEnum terms = seekTo == null ? in.terms () : in.terms (seekTo);        
        try {
            do {
                if (_cancel.get()) {
                    throw new InterruptedException ();
                }
                final Term currentTerm = terms.term();
                if (currentTerm != null) {                    
                    final T vote = filter.convert(currentTerm);
                    if (vote != null) {
                        result.add(vote);
                    }
                }
            } while (terms.next());
        } catch (ResultConvertor.Stop stop) {
            //Stop iteration of TermEnum
        } finally {
            terms.close();
        }
    }

    //Todo: replace by query(QueryUtil.createQueries) - needs to collect terms in queries
    public <T> void getDeclaredElements (String ident, ClassIndex.NameKind kind, ResultConvertor<? super Document, T> convertor, Map<T,Set<String>> result) throws IOException, InterruptedException {
        checkPreconditions();
        final IndexReader in = getReader();
        if (in == null) {
            LOGGER.fine(String.format("LuceneIndex[%s] is invalid!\n", this.toString()));   //NOI18N
            return;
        }
        final AtomicBoolean cancel = this.cancel.get();
        assert cancel != null;
        Parameters.notNull("ident", ident);             //NOI18N
        Parameters.notEmpty("ident", ident);            //NOI18N
        final Set<Term> toSearch = new TreeSet<Term> (new TermComparator());
        switch (kind) {
            case SIMPLE_NAME:
            {
                toSearch.add(DocumentUtil.featureIdentTerm(ident));
                break;
            }
            case PREFIX:
                {
                final Term nameTerm = DocumentUtil.featureIdentTerm(ident);
                prefixSearh(nameTerm, in, toSearch, cancel);
                break;
            }
            case CASE_INSENSITIVE_PREFIX:
            {
                final Term nameTerm = DocumentUtil.caseInsensitiveFeatureIdentTerm(ident.toLowerCase());    //I18N Locale?
                prefixSearh(nameTerm, in, toSearch, cancel);
                break;
            }
            case REGEXP:
            {
                final Pattern pattern = Pattern.compile(ident);
                if (Character.isJavaIdentifierStart(ident.charAt(0))) {
                    regExpSearch(pattern, DocumentUtil.featureIdentTerm(ident), in, toSearch, cancel, true);
                }
                else {
                    regExpSearch(pattern, DocumentUtil.featureIdentTerm(""), in, toSearch, cancel, true);             //NOI18N
                }
                break;
            }
            case CASE_INSENSITIVE_REGEXP:
            {
                final Pattern pattern = Pattern.compile(ident,Pattern.CASE_INSENSITIVE);
                if (Character.isJavaIdentifierStart(ident.charAt(0))) {
                    regExpSearch(pattern, DocumentUtil.caseInsensitiveFeatureIdentTerm(ident.toLowerCase()), in, toSearch,cancel, false);      //XXX: Locale
                }
                else {
                    regExpSearch(pattern, DocumentUtil.caseInsensitiveFeatureIdentTerm(""), in, toSearch,cancel, false);      //NOI18N
                }
                    break;
            }
            case CAMEL_CASE:
            {
                final StringBuilder sb = new StringBuilder();
                String prefix = null;
                int lastIndex = 0;
                int index;
                do {
                    index = findNextUpper(ident, lastIndex + 1);
                    String token = ident.substring(lastIndex, index == -1 ? ident.length(): index);
                    if ( lastIndex == 0 ) {
                        prefix = token;
                    }
                    sb.append(token);
                    sb.append( index != -1 ?  "[\\p{javaLowerCase}\\p{Digit}_\\$]*" : ".*"); // NOI18N
                    lastIndex = index;
                }
                while(index != -1);

                final Pattern pattern = Pattern.compile(sb.toString());
                regExpSearch(pattern,DocumentUtil.featureIdentTerm(prefix),in,toSearch,cancel, true);
                break;
            }
            case CAMEL_CASE_INSENSITIVE:
            {
                final Term nameTerm = DocumentUtil.caseInsensitiveFeatureIdentTerm(ident.toLowerCase());     //XXX: I18N, Locale
                prefixSearh(nameTerm, in, toSearch, cancel);
                StringBuilder sb = new StringBuilder();
                String prefix = null;
                int lastIndex = 0;
                int index;
                do {
                    index = findNextUpper(ident, lastIndex + 1);
                    String token = ident.substring(lastIndex, index == -1 ? ident.length(): index);
                    if ( lastIndex == 0 ) {
                        prefix = token;
                    }
                    sb.append(token);
                    sb.append( index != -1 ?  "[\\p{javaLowerCase}\\p{Digit}_\\$]*" : ".*"); // NOI18N
                    lastIndex = index;
                }
                while(index != -1);
                final Pattern pattern = Pattern.compile(sb.toString());
                regExpSearch(pattern,DocumentUtil.featureIdentTerm(prefix),in,toSearch,cancel, true);
                break;
            }
            default:
                throw new UnsupportedOperationException (kind.toString());
        }
        LOGGER.fine(String.format("LuceneIndex.getDeclaredElements[%s] returned %d elements\n",this.toString(), toSearch.size()));  //NOI18N
        final Map<Integer,Set<String>> docNums = new HashMap<Integer,Set<String>>();   //todo: TreeMap may perform better, ordered according to doc nums => linear IO
        final TermDocs tds = in.termDocs();
        try {
            int[] docs = new int[25];
            int[] freq = new int [25];
            int len;
            for (Term t : toSearch) {
                if (cancel.get()) {
                    throw new InterruptedException ();
                }
                tds.seek(t);
                while ((len = tds.read(docs, freq))>0) {
                    for (int i = 0; i < len; i++) {
                        Set<String> row = docNums.get(docs[i]);
                        if (row == null) {
                            row = new HashSet<String>();
                            docNums.put(docs[i], row);
                        }
                        row.add(t.text());
                    }
                }
            }
        } finally {
            tds.close();
        }
        for (Map.Entry<Integer,Set<String>> docNum : docNums.entrySet()) {
            if (cancel.get()) {
                throw new InterruptedException ();
            }
            final Document doc = in.document(docNum.getKey(), DocumentUtil.declaredTypesFieldSelector());
            try {
                final T key = convertor.convert(doc);
                if (key != null) {
                    result.put (key,docNum.getValue());
                }
            } catch (ResultConvertor.Stop stop) {
                //Stop not supported, not needed
            }
        }
    }
    //where
    private static int findNextUpper(String text, int offset ) {
        for( int i = offset; i < text.length(); i++ ) {
            if ( Character.isUpperCase(text.charAt(i)) ) {
                return i;
            }
        }
        return -1;
    }

    //<editor-fold desc="Implementation of Evictable interface">
    public void evicted() {
        //When running from memory cache no need to close the reader, it does not own file handler.
        if (!useMemoryCache) {
            //Threading: The called may own the CIM.readAccess, perform by dedicated worker to prevent deadlock
            RP.post(new Runnable() {
                public void run () {
                    try {
                        ClassIndexManager.getDefault().takeWriteLock(new ClassIndexManager.ExceptionAction<Void>() {
                            public Void run() throws IOException, InterruptedException {
                                close(false);
                                LOGGER.fine("Evicted index: " + refCacheRoot.getAbsolutePath()); //NOI18N
                                return null;
                            }
                        });
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (InterruptedException ie) {
                        Exceptions.printStackTrace(ie);
                    }
                }
            });
        }
    }
    ///</editor-fold>

    private void _hit() {
        if (!useMemoryCache) {
            try {
                final URL url = this.refCacheRoot.toURI().toURL();
                IndexCacheFactory.getDefault().getCache().put(url, this);
            } catch (MalformedURLException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }


    private void regExpSearch (final Pattern pattern, Term startTerm, final IndexReader in, final Set<Term> toSearch, final AtomicBoolean cancel, boolean caseSensitive) throws IOException, InterruptedException {        
        final String startText = startTerm.text();
        String startPrefix;
        if (startText.length() > 0) {
            final StringBuilder startBuilder = new StringBuilder ();
            startBuilder.append(startText.charAt(0));
            for (int i=1; i<startText.length(); i++) {
                char c = startText.charAt(i);
                if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
                startBuilder.append(c);
            }
            startPrefix = startBuilder.toString();
            startTerm = new Term (startTerm.field(),startPrefix);
        }
        else {
            startPrefix=startText;
        }
        final String camelField = startTerm.field();
        final TermEnum en = in.terms(startTerm);
        try {
            do {
                if (cancel.get()) {
                    throw new InterruptedException ();
                }
                Term term = en.term();
                if (term != null && camelField == term.field() && term.text().startsWith(startPrefix)) {
                    final Matcher m = pattern.matcher(term.text());
                    if (m.matches()) {
                        toSearch.add (term);
                    }
                }
                else {
                    break;
                }
            } while (en.next());
        } finally {
            en.close();
        }
    }
    
    private void prefixSearh (Term nameTerm, final IndexReader in, final Set<Term> toSearch, final AtomicBoolean cancel) throws IOException, InterruptedException {
        final String prefixField = nameTerm.field();
        final String name = nameTerm.text();
        final TermEnum en = in.terms(nameTerm);
        try {
            do {
                if (cancel.get()) {
                    throw new InterruptedException ();
                }
                Term term = en.term();
                if (term != null && prefixField == term.field() && term.text().startsWith(name)) {
                    toSearch.add (term);
                }
                else {
                    break;
                }
            } while (en.next());
        } finally {
            en.close();
        }
    }

    public void store (final Map<Pair<String,String>, Object[]> refs, final List<Pair<String,String>> topLevels) throws IOException {
        try {
            ClassIndexManager.getDefault().takeWriteLock(new ClassIndexManager.ExceptionAction<Void>() {
                public Void run() throws IOException, InterruptedException {
                    _store(refs, topLevels);
                    validCache = true;
                    return null;
                }
            });
        } catch (InterruptedException ie) {
            throw new IOException("Interrupted");   //NOI18N
        }
    }

    private void _store (final Map<Pair<String,String>, Object[]> refs, final List<Pair<String,String>> topLevels) throws IOException {
        checkPreconditions();
        assert ClassIndexManager.getDefault().holdsWriteLock();
        boolean create = !exists();
        final IndexWriter out = getWriter(create);
        try {
            if (!create) {
                for (Pair<String,String> topLevel : topLevels) {
                    out.deleteDocuments(DocumentUtil.binaryContentNameQuery(topLevel));
                }
            }
            storeData(out, refs, false);
        } finally {
            try {
                out.close();
            } finally {
                refreshReader();
            }
        }
    }

    //Todo: probably unsed, the java source should be refactored and cleaned up.
    public void store(final Map<Pair<String,String>, Object[]> refs, final Set<Pair<String,String>> toDelete) throws IOException {
        try {
            ClassIndexManager.getDefault().takeWriteLock(new ClassIndexManager.ExceptionAction<Void>() {
                public Void run() throws IOException, InterruptedException {
                    _store(refs, toDelete);
                    validCache = true;
                    return null;
                }
            });
        } catch (InterruptedException ie) {
            throw new IOException("Interrupted");   //NOI18N
        }
    }

    private void _store(final Map<Pair<String,String>, Object[]> refs, final Set<Pair<String,String>> toDelete) throws IOException {
        checkPreconditions();
        assert ClassIndexManager.getDefault().holdsWriteLock();
        boolean create = !exists();
        final IndexWriter out = getWriter(create);
        try {
            if (!create) {
                for (Pair<String,String> toDeleteItem : toDelete) {
                    out.deleteDocuments(DocumentUtil.binaryNameSourceNamePairQuery(toDeleteItem));
                }
            }
            storeData(out, refs, true);
        } finally {
            try {
                out.close();
            } finally {
                refreshReader();
            }
        }
    }

    private void storeData (final IndexWriter out,
            final Map<Pair<String,String>, Object[]> refs,
            final boolean optimize) throws IOException {
        if (debugIndexMerging) {
            out.setInfoStream (System.err);
        }
        final LuceneIndexMBean indexSettings = LuceneIndexMBeanImpl.getDefault();
        if (indexSettings != null) {
            out.setMergeFactor(indexSettings.getMergeFactor());
            out.setMaxMergeDocs(indexSettings.getMaxMergeDocs());
            out.setMaxBufferedDocs(indexSettings.getMaxBufferedDocs());
        }
        final LMListener lmListener = new LMListener ();
        Directory memDir = null;
        IndexWriter activeOut = null;
        if (lmListener.isLowMemory()) {
            activeOut = out;
        }
        else {
            memDir = new RAMDirectory ();
            activeOut = new IndexWriter (memDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        }
        for (Iterator<Map.Entry<Pair<String,String>,Object[]>> it = refs.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Pair<String,String>,Object[]> refsEntry = it.next();
            it.remove();
            final Pair<String,String> pair = refsEntry.getKey();
            final String cn = pair.first;
            final String srcName = pair.second;
            final Object[] data = refsEntry.getValue();
            final List<String> cr = (List<String>) data[0];
            final String fids = (String) data[1];
            final String ids = (String) data[2];
            final Document newDoc = DocumentUtil.createDocument(cn,cr,fids,ids,srcName);
            activeOut.addDocument(newDoc);
            if (memDir != null && lmListener.isLowMemory()) {
                activeOut.close();
                out.addIndexesNoOptimize(new Directory[] {memDir});
                memDir = new RAMDirectory ();
                activeOut = new IndexWriter (memDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
            }
        }
        if (memDir != null) {
            activeOut.close();
            out.addIndexesNoOptimize(new Directory[] {memDir});
            activeOut = null;
            memDir = null;
        }
        if (optimize) {
            out.optimize(false);
        }
    }

    public boolean isValid (boolean force) throws IOException {
        checkPreconditions();
        Boolean valid = validCache;
        if (force ||  valid == null) {
            final Collection<? extends String> locks = getOrphanLock();
            boolean res = false;
            if (!locks.isEmpty()) {
                LOGGER.warning("Broken (locked) index folder: " + refCacheRoot.getAbsolutePath());   //NOI18N
                for (String lockName : locks) {
                    directory.deleteFile(lockName);
                }
                if (force) {
                    clear();
                }
            } else {
                res = exists();
                if (res && force) {
                    try {
                        getReader();
                    } catch (java.io.IOException e) {
                        res = false;
                        clear();
                    } catch (RuntimeException e) {
                        res = false;
                        clear();
                    }
                }
            }
            valid = res;
            validCache = valid;
        }
        return valid;
    }

    public void clear () throws IOException {
        try {
            checkPreconditions();
            ClassIndexManager.getDefault().takeWriteLock(new ClassIndexManager.ExceptionAction<Void>() {

                public Void run() throws IOException, InterruptedException {
                    _clear();
                    return null;
                }
            });
        } catch (InterruptedException ex) {
            //Never happens
            IOException newException = new IOException();
            newException.initCause(ex);
            throw newException;
        }
    }

    private synchronized void _clear() throws IOException {
        this.close (false);
        try {
            final String[] content = this.directory.list();
            boolean dirty = false;
            if (content != null) {
                for (String file : content) {
                    try {
                        directory.deleteFile(file);
                    } catch (IOException e) {
                        //Some temporary files
                        if (directory.fileExists(file)) {
                            dirty = true;
                        }
                    }
                }
            }
            if (dirty) {
                //Try to delete dirty files and log what's wrong
                final File cacheDir = ((FSDirectory)this.directory).getFile();
                final File[] children = cacheDir.listFiles();
                if (children != null) {
                    for (final File child : children) {                                                
                        if (!child.delete()) {
                            final Class c = this.directory.getClass();
                            int refCount = -1;
                            try {
                                final Field field = c.getDeclaredField("refCount"); //NOI18N
                                field.setAccessible(true);
                                refCount = field.getInt(this.directory);
                            } catch (NoSuchFieldException e) {/*Not important*/}
                              catch (IllegalAccessException e) {/*Not important*/}
                            final Map<Thread,StackTraceElement[]> sts = Thread.getAllStackTraces();
                            throw new IOException("Cannot delete: " + child.getAbsolutePath() + "(" +   //NOI18N
                                    child.exists()  +","+                                               //NOI18N
                                    child.canRead() +","+                                               //NOI18N
                                    child.canWrite() +","+                                              //NOI18N
                                    cacheDir.canRead() +","+                                            //NOI18N
                                    cacheDir.canWrite() +","+                                           //NOI18N
                                    refCount +","+                                                      //NOI18N
                                    sts +")");                                                          //NOI18N
                        }
                    }
                }
            }
        } finally {
            //Need to recreate directory, see issue: #148374
            this.close(true);
            this.directory = createDirectory(refCacheRoot);
            closed = false;
        }
    }

    public boolean exists () {
        try {
            return IndexReader.indexExists(this.directory);
        } catch (IOException e) {
            return false;
        } catch (RuntimeException e) {
            LOGGER.log(Level.INFO, "Broken index: " + refCacheRoot.getAbsolutePath(), e);
            return false;
        }
    }

    public void close () throws IOException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Closing index: {0} {1}",  //NOI18N
                    new Object[]{
                        this.refCacheRoot.getAbsolutePath(),
                        Thread.currentThread().getStackTrace()});
        }
        close(true);
    }

    public synchronized void close (boolean closeDir) throws IOException {
        try {
            try {
                if (this.reader != null) {
                    this.reader.close();
                    this.reader = null;
                }
            } finally {
                if (memCacheDir != null) {
                    assert useMemoryCache;
                    final Directory tmpDir = this.memCacheDir;
                    this.memCacheDir = null;
                    tmpDir.close();
                }
            }
        } finally {
            if (closeDir) {
                this.closed = true;
                this.directory.close();
            }
        }
    }

    public @Override String toString () {
        return getClass().getSimpleName()+"["+this.refCacheRoot.getAbsolutePath()+"]";  //NOI18N
    }

    private synchronized IndexReader getReader () throws IOException {
        _hit();
        if (this.reader == null) {
            if (validCache == Boolean.FALSE) {
                return null;
            }
            //Issue #149757 - logging
            try {
                Directory source;
                if (useMemoryCache) {
                    source = memCacheDir = new RAMDirectory(this.directory);

                } else {
                    source = this.directory;
                }
                //It's important that no Query will get access to original IndexReader
                //any norms call to it will initialize the HashTable of norms: sizeof (byte) * maxDoc() * max(number of unique fields in document)
                this.reader = new NoNormsReader(IndexReader.open(source));
            } catch (final FileNotFoundException fnf) {
                //pass - returns null
            } catch (IOException ioe) {
                throw annotateException (ioe);
            }
        }
        return this.reader;
    }

    private synchronized IndexWriter getWriter (final boolean create) throws IOException {
        _hit();
        //Issue #149757 - logging
        try {
            IndexWriter writer = new IndexWriter (this.directory, analyzer, create, IndexWriter.MaxFieldLength.LIMITED);
            return writer;
        } catch (IOException ioe) {
            throw annotateException (ioe);
        }
    }

    private synchronized void refreshReader() throws IOException {
        if (useMemoryCache) {
            close(false);
        } else {
            if (reader != null) {
                final IndexReader newReader = reader.reopen();
                if (newReader != reader) {
                    reader.close();
                    reader = newReader;
                }
            }
        }
    }

    private IOException annotateException (final IOException ioe) {
        String message;
        File[] children = refCacheRoot.listFiles();
        if (children == null) {
            message = "Non existing index folder";
        }
        else {
            StringBuilder b = new StringBuilder();
            for (File c : children) {
                b.append(c.getName() +" f: " + c.isFile() + " r: " + c.canRead() + " w: " + c.canWrite()+"\n");  //NOI18N
            }
            message = b.toString();
        }
        return Exceptions.attachMessage(ioe, message);
    }

    private static File getReferencesCacheFolder (final File cacheRoot) throws IOException {
        File refRoot = new File (cacheRoot,REFERENCES);
        if (!refRoot.exists()) {
            refRoot.mkdir();
        }
        return refRoot;
    }

    private void checkPreconditions () throws ClassIndexImpl.IndexAlreadyClosedException{
        if (closed) {
            throw new ClassIndexImpl.IndexAlreadyClosedException();
        }
    }


    private static Directory createDirectory(final File indexFolder) throws IOException {
        assert indexFolder != null;
        FSDirectory directory  = FSDirectory.getDirectory(indexFolder);
        directory.getLockFactory().setLockPrefix(CACHE_LOCK_PREFIX);
        return directory;
    }

    private Collection<? extends String> getOrphanLock () {
        final List<String> locks = new LinkedList<String>();
        final String[] content = refCacheRoot.list();
        if (content != null) {
            for (String name : content) {
                if (name.startsWith(CACHE_LOCK_PREFIX)) {
                    locks.add(name);
                }
            }
        }
        return locks;
    }

    /**
     * Expert: Bypass read of norms
     */
    private static class NoNormsReader extends FilterIndexReader {

        //@GuardedBy (this)
        private byte[] norms;

        public NoNormsReader (final IndexReader reader) {
            super (reader);
        }

        @Override
        public byte[] norms(String field) throws IOException {
            byte[] norms = fakeNorms ();
            return norms;
        }

        @Override
        public void norms(String field, byte[] norm, int offset) throws IOException {
            byte[] norms = fakeNorms ();
            System.arraycopy(norms, 0, norm, offset, norms.length);
        }

        @Override
        public boolean hasNorms(String field) throws IOException {
            return false;
        }

        @Override
        protected void doSetNorm(int doc, String field, byte norm) throws CorruptIndexException, IOException {
            //Ignore
        }

        @Override
        protected void doClose() throws IOException {
            synchronized (this)  {
                this.norms = null;
            }
            super.doClose();
        }

        @Override
        public IndexReader reopen() throws IOException {
            final IndexReader newIn = in.reopen();
            if (newIn == in) {
                return this;
            }
            return new NoNormsReader(newIn);
        }

        /**
         * Expert: Fakes norms, norms are not needed for Netbeans index.
         */
        private synchronized byte[] fakeNorms() {
            if (this.norms == null) {
                this.norms = new byte[maxDoc()];
                Arrays.fill(this.norms, DefaultSimilarity.encodeNorm(1.0f));
            }
            return this.norms;
        }
    }

    private static class TermComparator implements Comparator<Term> {
        @Override
        public int compare (Term t1, Term t2) {
            int ret = t1.field().compareTo(t2.field());
            if (ret == 0) {
                ret = t1.text().compareTo(t2.text());
            }
            return ret;
        }
    }    

}
