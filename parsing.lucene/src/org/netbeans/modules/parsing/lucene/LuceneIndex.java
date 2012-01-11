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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.lucene;

import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.NoLockFactory;
import org.netbeans.modules.parsing.lucene.support.LowMemoryWatcher;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.LucenePackage;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.*;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.RAMDirectory;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.StoppableConvertor;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Note - there can be only a single IndexWriter at a time for the dir index. For consistency, the Writer is
 * kept in a thread-local variable until it is committed. Lucene will throw an exception if another Writer creation
 * attempt is done (by another thread, presumably). 
 * <p/>
 * It should be thread-safe (according to Lucene docs) to use an IndexWriter while Readers are opened.
 * <p/>
 * As Reader and Writers can be used in parallel, all query+store operations use readLock so they can run in parallel.
 * Operations which affect the whole index (close, clear) use write lock. RefreshReader called internally from writer's commit (close)
 * is incompatible with parallel reads, as it closes the old reader - uses writeLock.
 * <p/>
 * Locks must be acquired in the order [rwLock, LuceneIndex]. The do* method synchronize on the DirCache instance and must be called 
 * if the code already holds rwLock.
 *
 * @author Tomas Zezula
 */
//@NotTreadSafe
public class LuceneIndex implements Index.Transactional {

    private static final String PROP_INDEX_POLICY = "java.index.useMemCache";   //NOI18N
    private static final String PROP_CACHE_SIZE = "java.index.size";    //NOI18N
    private static final boolean debugIndexMerging = Boolean.getBoolean("java.index.debugMerge");     // NOI18N
    private static final CachePolicy DEFAULT_CACHE_POLICY = CachePolicy.DYNAMIC;
    private static final float DEFAULT_CACHE_SIZE = 0.05f;
    private static final CachePolicy cachePolicy = getCachePolicy();
    private static final Logger LOGGER = Logger.getLogger(LuceneIndex.class.getName());
    
    private static LockFactory lockFactory;
    
    private final DirCache dirCache;       
    
    /** unit tests */
    public static void setDisabledLocks(final boolean disabled) {
        lockFactory = disabled ? NoLockFactory.getNoLockFactory() : null;
    }

    public static LuceneIndex create (final File cacheRoot, final Analyzer analyzer) throws IOException {
        assert cacheRoot != null && cacheRoot.exists() && cacheRoot.canRead() && cacheRoot.canWrite();
        return new LuceneIndex (cacheRoot, analyzer);
    }

    /** Creates a new instance of LuceneIndex */
    private LuceneIndex (final File refCacheRoot, final Analyzer analyzer) throws IOException {
        assert refCacheRoot != null;
        assert analyzer != null;
        this.dirCache = new DirCache(refCacheRoot,cachePolicy, analyzer);
    }
    
    @Override
    public <T> void query (
            final @NonNull Collection<? super T> result,
            final @NonNull Convertor<? super Document, T> convertor,
            @NullAllowed FieldSelector selector,
            final @NullAllowed AtomicBoolean cancel,
            final @NonNull Query... queries
            ) throws IOException, InterruptedException {
        Parameters.notNull("queries", queries);   //NOI18N
        Parameters.notNull("convertor", convertor); //NOI18N
        Parameters.notNull("result", result);       //NOI18N   
        
        if (selector == null) {
            selector = AllFieldsSelector.INSTANCE;
        }
        IndexReader in = null;
        try {
            in = dirCache.acquireReader();
            if (in == null) {
                LOGGER.log(Level.FINE, "{0} is invalid!", this);
                return;
            }
            final BitSet bs = new BitSet(in.maxDoc());
            final Collector c = new BitSetCollector(bs);
            final Searcher searcher = new IndexSearcher(in);
            try {
                for (Query q : queries) {
                    if (cancel != null && cancel.get()) {
                        throw new InterruptedException ();
                    }
                    searcher.search(q, c);
                }
            } finally {
                searcher.close();
            }        
            for (int docNum = bs.nextSetBit(0); docNum >= 0; docNum = bs.nextSetBit(docNum+1)) {
                if (cancel != null && cancel.get()) {
                    throw new InterruptedException ();
                }
                final Document doc = in.document(docNum, selector);
                final T value = convertor.convert(doc);
                if (value != null) {
                    result.add (value);
                }
            }
        } finally {
            dirCache.releaseReader(in);
        }
    }
    
    @Override
    public <T> void queryTerms(
            final @NonNull Collection<? super T> result,
            final @NullAllowed Term seekTo,
            final @NonNull StoppableConvertor<Term,T> filter,
            final @NullAllowed AtomicBoolean cancel) throws IOException, InterruptedException {
        
        IndexReader in = null;
        try {
            in = dirCache.acquireReader();
            if (in == null) {
                return;
            }

            final TermEnum terms = seekTo == null ? in.terms () : in.terms (seekTo);        
            try {
                do {
                    if (cancel != null && cancel.get()) {
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
            } catch (StoppableConvertor.Stop stop) {
                //Stop iteration of TermEnum
            } finally {
                terms.close();
            }
        } finally {
            dirCache.releaseReader(in);
        }
    }
    
    @Override
    public <S, T> void queryDocTerms(
            final @NonNull Map<? super T, Set<S>> result,
            final @NonNull Convertor<? super Document, T> convertor,
            final @NonNull Convertor<? super Term, S> termConvertor,
            @NullAllowed FieldSelector selector,
            final @NullAllowed AtomicBoolean cancel,
            final @NonNull Query... queries) throws IOException, InterruptedException {
        Parameters.notNull("queries", queries);             //NOI18N
        Parameters.notNull("slector", selector);            //NOI18N
        Parameters.notNull("convertor", convertor);         //NOI18N
        Parameters.notNull("termConvertor", termConvertor); //NOI18N
        Parameters.notNull("result", result);               //NOI18N
        if (selector == null) {
            selector = AllFieldsSelector.INSTANCE;
        }
        IndexReader in = null;
        try {
            in = dirCache.acquireReader();
            if (in == null) {
                LOGGER.fine(String.format("LuceneIndex[%s] is invalid!\n", this.toString()));   //NOI18N
                return;
            }
            final BitSet bs = new BitSet(in.maxDoc());
            final Collector c = new BitSetCollector(bs);
            final Searcher searcher = new IndexSearcher(in);
            final TermCollector termCollector = new TermCollector();
            try {
                for (Query q : queries) {
                    if (cancel != null && cancel.get()) {
                        throw new InterruptedException ();
                    }
                    if (q instanceof TermCollector.TermCollecting) {
                        ((TermCollector.TermCollecting)q).attach(termCollector);
                    } else {
                        throw new IllegalArgumentException (
                                String.format("Query: %s does not implement TermCollecting",    //NOI18N
                                q.getClass().getName()));
                    }
                    searcher.search(q, c);
                }
            } finally {
                searcher.close();
            }
        
            boolean logged = false;
            for (int docNum = bs.nextSetBit(0); docNum >= 0; docNum = bs.nextSetBit(docNum+1)) {
                if (cancel != null && cancel.get()) {
                    throw new InterruptedException ();
                }
                final Document doc = in.document(docNum, selector);
                final T value = convertor.convert(doc);
                if (value != null) {
                    final Set<Term> terms = termCollector.get(docNum);
                    if (terms != null) {
                        result.put (value, convertTerms(termConvertor, terms));
                    } else {
                        if (!logged) {
                            LOGGER.log(Level.WARNING, "Index info [maxDoc: {0} numDoc: {1} docs: {2}]",
                                    new Object[] {
                                        in.maxDoc(),
                                        in.numDocs(),
                                        termCollector.docs()
                                    });
                            logged = true;
                        }
                        LOGGER.log(Level.WARNING, "No terms found for doc: {0}", docNum);
                    }
                }
            }
        } finally {
            dirCache.releaseReader(in);
        }
    }
    
    private static <T> Set<T> convertTerms(final Convertor<? super Term, T> convertor, final Set<? extends Term> terms) {
        final Set<T> result = new HashSet<T>(terms.size());
        for (Term term : terms) {
            result.add(convertor.convert(term));
        }
        return result;
    }

    @Override
    public void commit() throws IOException {
        dirCache.closeTxWriter();
    }

    @Override
    public void rollback() throws IOException {
        dirCache.rollbackTxWriter();
    }

    @Override
    public <S, T> void txStore(
            final Collection<T> toAdd, 
            final Collection<S> toDelete, final Convertor<? super T, ? extends Document> docConvertor, 
            final Convertor<? super S, ? extends Query> queryConvertor) throws IOException {
        
        final IndexWriter[] wr = new IndexWriter[1];
        try {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Storing in TX {0}: {1} added, {2} deleted", 
                        new Object[] { this, toAdd.size(), toDelete.size() }
                        );
            }
            _doStore(toAdd, toDelete, docConvertor, queryConvertor, wr, false);
        } finally {
            // nothing committed upon failure - readers not affected
            boolean ok = false;
            try {
                if (wr[0] != null) {
                    ((FlushIndexWriter)wr[0]).callFlush(false, true);
                }
                ok = true;
            } finally {
                if (!ok) {
                    dirCache.rollbackTxWriter();
                }
            }
        }
    }
    
    private <S, T> void _doStore(Collection<T> data, 
            Collection<S> toDelete, Convertor<? super T, ? extends Document> docConvertor, 
            Convertor<? super S, ? extends Query> queryConvertor, IndexWriter[] ret, boolean optimize) throws IOException {
        final boolean create = !dirCache.exists();
        final IndexWriter out = dirCache.acquireWriter(create);
        try {
            ret[0] = out;
            if (!create) {
                for (S td : toDelete) {
                    out.deleteDocuments(queryConvertor.convert(td));
                }
            }            
            if (data.isEmpty()) {
                return;
            }
            if (debugIndexMerging) {
                out.setInfoStream (System.err);
            }                
            final LowMemoryWatcher lmListener = LowMemoryWatcher.getInstance();
            Directory memDir = null;
            IndexWriter activeOut = null;
            if (lmListener.isLowMemory()) {
                activeOut = out;
            }
            else {
                memDir = new RAMDirectory ();
                activeOut = new IndexWriter (memDir, dirCache.getAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
            }
            for (Iterator<T> it = data.iterator(); it.hasNext();) {
                T entry = it.next();
                it.remove();
                final Document doc = docConvertor.convert(entry);
                activeOut.addDocument(doc);
                if (memDir != null && lmListener.isLowMemory()) {
                    activeOut.close();
                    out.addIndexesNoOptimize(new Directory[] {memDir});
                    memDir = new RAMDirectory ();
                    activeOut = new IndexWriter (memDir, dirCache.getAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
                }
            }
            if (memDir != null) {
                activeOut.close();
                out.addIndexesNoOptimize(new Directory[] {memDir});
                activeOut = null;
                memDir = null;
            }
        } catch (RuntimeException e) {
            throw Exceptions.attachMessage(e, "Lucene Index Folder: " + dirCache.folder.getAbsolutePath());
        } catch (IOException e) {
            throw Exceptions.attachMessage(e, "Lucene Index Folder: " + dirCache.folder.getAbsolutePath());
        } finally {
            dirCache.releaseWriter(out);
        }
    }

    @Override
    public <S, T> void store (
            final @NonNull Collection<T> data,
            final @NonNull Collection<S> toDelete,
            final @NonNull Convertor<? super T, ? extends Document> docConvertor,
            final @NonNull Convertor<? super S, ? extends Query> queryConvertor,
            final boolean optimize) throws IOException {
        
        IndexWriter[] wr = new IndexWriter[1];
        try {
            _doStore(data, toDelete, docConvertor, queryConvertor, wr, optimize);
        } finally {
            LOGGER.log(Level.FINE, "Committing {0}", this);
            dirCache.close(wr[0]);
        }
    }
        
    @Override
    public Status getStatus (boolean force) throws IOException {
        return dirCache.getStatus(force);
    }

    @Override
    public void clear () throws IOException {
        dirCache.clear();
    }
    
    @Override
    public void close () throws IOException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Closing index: {0} {1}",  //NOI18N
                    new Object[]{
                        this.dirCache.toString(),
                        Thread.currentThread().getStackTrace()});
        }
        dirCache.close(true);
    }


    @Override
    public String toString () {
        return getClass().getSimpleName()+"["+this.dirCache.toString()+"]";  //NOI18N
    }    
           
    private static CachePolicy getCachePolicy() {
        final String value = System.getProperty(PROP_INDEX_POLICY);   //NOI18N
        if (Boolean.TRUE.toString().equals(value) ||
            CachePolicy.ALL.getSystemName().equals(value)) {
            return CachePolicy.ALL;
        }
        if (Boolean.FALSE.toString().equals(value) ||
            CachePolicy.NONE.getSystemName().equals(value)) {
            return CachePolicy.NONE;
        }
        if (CachePolicy.DYNAMIC.getSystemName().equals(value)) {
            return CachePolicy.DYNAMIC;
        }
        return DEFAULT_CACHE_POLICY;
    }
    

    //<editor-fold defaultstate="collapsed" desc="Private classes (NoNormsReader, TermComparator, CachePolicy)">
    
    private static class NoNormsReader extends FilterIndexReader {

        //@GuardedBy (this)
        private byte[] norms;

        public NoNormsReader (final IndexReader reader) {
            super (reader);
        }

        @Override
        public byte[] norms(String field) throws IOException {
            byte[] _norms = fakeNorms ();
            return _norms;
        }

        @Override
        public void norms(String field, byte[] norm, int offset) throws IOException {
            byte[] _norms = fakeNorms ();
            System.arraycopy(_norms, 0, norm, offset, _norms.length);
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
        
    private enum CachePolicy {
        
        NONE("none", false),          //NOI18N
        DYNAMIC("dynamic", true),     //NOI18N
        ALL("all", true);             //NOI18N
        
        private final String sysName;
        private final boolean hasMemCache;
        
        CachePolicy(final String sysName, final boolean hasMemCache) {
            assert sysName != null;
            this.sysName = sysName;
            this.hasMemCache = hasMemCache;
        }
        
        String getSystemName() {
            return sysName;
        }
        
        boolean hasMemCache() {
            return hasMemCache;
        }
    }    
    
    private static final class DirCache implements Evictable {
        
        private static final String CACHE_LOCK_PREFIX = "nb-lock";  //NOI18N
        private static final RequestProcessor RP = new RequestProcessor(LuceneIndex.class.getName(), 1);
        private static final long maxCacheSize = getCacheSize();
        private static volatile long currentCacheSize;
        
        private final File folder;
        private final CachePolicy cachePolicy;
        private final Analyzer analyzer;
        private volatile FSDirectory fsDir;
        private RAMDirectory memDir;
        private CleanReference ref;
        private IndexReader reader;
        private volatile boolean closed;
        private volatile Status validCache;
        private long lastUsedWriter;
        private ReadWriteLock   rwLock = new java.util.concurrent.locks.ReentrantReadWriteLock();
        
        /**
         * IndexWriter with potentially uncommitted data; local to a thread.
         */
        private ThreadLocal<IndexWriter>   txWriter = new ThreadLocal<IndexWriter>();
        
        private DirCache(
                final @NonNull File folder,
                final @NonNull CachePolicy cachePolicy,
                final @NonNull Analyzer analyzer) throws IOException {
            assert folder != null;
            assert cachePolicy != null;
            assert analyzer != null;
            this.folder = folder;
            this.fsDir = createFSDirectory(folder);
            this.cachePolicy = cachePolicy;                        
            this.analyzer = analyzer;
        }
        
        Analyzer getAnalyzer() {
            return this.analyzer;
        }
        
        void clear() throws IOException {
            rwLock.writeLock().lock();
            try {
                doClear();
            } finally {
                rwLock.writeLock().unlock();
            }
        }
                                
        private synchronized void doClear() throws IOException {
            checkPreconditions();
            // already write locked
            doClose(false);
            try {
                final String[] content = fsDir.listAll();
                boolean dirty = false;
                if (content != null) {
                    for (String file : content) {
                        try {
                            fsDir.deleteFile(file);
                        } catch (IOException e) {
                            //Some temporary files
                            if (fsDir.fileExists(file)) {
                                dirty = true;
                            }
                        }
                    }
                }
                if (dirty) {
                    //Try to delete dirty files and log what's wrong
                    final File cacheDir = fsDir.getFile();
                    final File[] children = cacheDir.listFiles();
                    if (children != null) {
                        for (final File child : children) {                                                
                            if (!child.delete()) {
                                final Class c = fsDir.getClass();
                                int refCount = -1;
                                try {
                                    final Field field = c.getDeclaredField("refCount"); //NOI18N
                                    field.setAccessible(true);
                                    refCount = field.getInt(fsDir);
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
                this.doClose(true);
                this.fsDir = createFSDirectory(this.folder);
                closed = false;
            }
        }
        
        void close(IndexWriter writer) throws IOException {
            if (writer == null) {
                return;
            }
            try {
                writer.close();
            } finally {
                if (txWriter.get() == writer) {
                    LOGGER.log(Level.FINE, "TX writer cleared for {0}", this);
                    txWriter.remove();
                    refreshReader();
                }
            }
        }
        
        void close (final boolean closeFSDir) throws IOException {
            try {
                rwLock.writeLock().lock();
                doClose(closeFSDir);
            } finally {
                rwLock.writeLock().unlock();
            }                
        }
        
        synchronized void doClose (final boolean closeFSDir) throws IOException {
            try {
                rollbackTxWriter();
                if (this.reader != null) {
                    this.reader.close();
                    this.reader = null;
                }
            } finally {                        
                if (memDir != null) {
                    assert cachePolicy.hasMemCache();
                    if (this.ref != null) {
                        this.ref.clear();
                    }
                    final Directory tmpDir = this.memDir;
                    memDir = null;
                    tmpDir.close();
                }
                if (closeFSDir) {
                    this.closed = true;
                    this.fsDir.close();
                }
            }
        }
        
        boolean exists() {
            try {
                return IndexReader.indexExists(this.fsDir);
            } catch (IOException e) {
                return false;
            } catch (RuntimeException e) {
                LOGGER.log(Level.INFO, "Broken index: " + folder.getAbsolutePath(), e);
                return false;
            }
        }
        
        Status getStatus (boolean force) throws IOException {
            checkPreconditions();
            Status valid = validCache;
            if (force ||  valid == null) {
                final Collection<? extends String> locks = getOrphanLock();
                Status res = Status.INVALID;
                if (!locks.isEmpty()) {
                    if (txWriter.get() != null) {
                        res = Status.WRITING;
                    } else {
                        LOGGER.log(Level.WARNING, "Broken (locked) index folder: {0}", folder.getAbsolutePath());   //NOI18N
                        synchronized (this) {
                            for (String lockName : locks) {
                                fsDir.deleteFile(lockName);
                            }
                        }
                        if (force) {
                            clear();
                        }
                    }
                } else {
                    if (!exists()) {
                        res = Status.EMPTY;
                    } else if (force) {
                        try {
                            getReader();
                            res = Status.VALID;
                        } catch (java.io.IOException e) {
                            clear();
                        } catch (RuntimeException e) {
                            clear();
                        }
                    } else {
                        res = Status.VALID;
                    }
                }
                valid = res;
                validCache = valid;
            }
            return valid;
        }
        
        boolean closeTxWriter() throws IOException {
            IndexWriter writer = txWriter.get();
            if (writer != null) {
                LOGGER.log(Level.FINE, "Committing {0}", this);
                close(writer);
                return true;
            } else {
                return false;
            }
        }
        
        boolean rollbackTxWriter() throws IOException {
            IndexWriter writer = txWriter.get();
            if (writer != null) {
                try {
                    writer.rollback();
                    return true;
                } finally {
                    txWriter.remove();
                }
            } else {
                return false;
            }
        }
        
        private static final long STALE_WRITER_THRESHOLD = 120 /* sec */ * 1000;
        
        /**
         * The writer operates under readLock(!) since we do not want to lock out readers,
         * but just close, clear and commit operations. 
         * 
         * @param create
         * @return
         * @throws IOException 
         */
        IndexWriter acquireWriter (final boolean create) throws IOException {
            checkPreconditions();
            hit();

            boolean ok = false;
            
            rwLock.readLock().lock();
            IndexWriter writer = txWriter.get();
            try {
                if (writer != null) {
                    long current = System.currentTimeMillis();
                    if (current - lastUsedWriter > STALE_WRITER_THRESHOLD) {
                        LOGGER.log(Level.WARNING, "Using stale writer, possibly forgotten call to store ?", new Throwable());
                    }
                    lastUsedWriter = current;
                    ok = true;
                    return writer;
                }
                //Issue #149757 - logging
                try {
                    // not synchronized, volatile var is accessed
                    final IndexWriter iw = new FlushIndexWriter (this.fsDir, analyzer, create, IndexWriter.MaxFieldLength.LIMITED);
                    iw.setMergeScheduler(new SerialMergeScheduler());
                    lastUsedWriter = System.currentTimeMillis();
                    txWriter.set(iw);
                    ok = true;
                    return iw;
                } catch (IOException ioe) {
                    throw annotateException (ioe);
                }
            } finally {
                if (!ok) {
                    rwLock.readLock().unlock();
                }
            }
        }
        
        void releaseWriter(IndexWriter w) {
            assert w == txWriter.get();
            rwLock.readLock().unlock();
        }
        
        IndexReader acquireReader() throws IOException {
            rwLock.readLock().lock();
            IndexReader r = null;
            try {
                r = getReader();
                return r;
            } finally {
                if (r == null) {
                  rwLock.readLock().unlock();
                }
            }
        }
        
        void releaseReader(IndexReader r) {
            if (r == null) {
                return;
            }
            assert r == this.reader;
            rwLock.readLock().unlock();
        }
        
        private synchronized IndexReader getReader () throws IOException {
            checkPreconditions();
            hit();
            if (this.reader == null) {
                if (validCache != Status.VALID &&
                    validCache != Status.WRITING &&
                    validCache != null) {
                    return null;
                }
                //Issue #149757 - logging
                try {
                    Directory source;
                    if (cachePolicy.hasMemCache()) {                        
                        memDir = new RAMDirectory(fsDir);
                        if (cachePolicy == CachePolicy.DYNAMIC) {
                            ref = new CleanReference (new RAMDirectory[] {this.memDir});
                        }
                        source = memDir;
                    } else {
                        source = fsDir;
                    }
                    assert source != null;
                    this.reader = new NoNormsReader(IndexReader.open(source,true));
                } catch (final FileNotFoundException fnf) {
                    //pass - returns null
                } catch (IOException ioe) {
                    if (validCache == null) {
                        return null;
                    } else {
                        throw annotateException (ioe);
                    }
                }
            }
            return this.reader;
        }


        void refreshReader() throws IOException {
            if (IndexWriter.isLocked(fsDir)) {
                IndexWriter.unlock(fsDir);
            }
            try {
                if (cachePolicy.hasMemCache()) {
                    close(false);
                } else {
                    rwLock.writeLock().lock();
                    try {
                        synchronized (this) {
                            if (reader != null) {
                                final IndexReader newReader = reader.reopen();
                                if (newReader != reader) {
                                    reader.close();
                                    reader = newReader;
                                }
                            }
                        }
                    } finally {
                        rwLock.writeLock().unlock();
                    }
                }
            } finally {
                 validCache = Status.VALID;
            }
        }
        
        @Override
        public String toString() {
            return this.folder.getAbsolutePath();
        }
        
        @Override
        public void evicted() {
            //When running from memory cache no need to close the reader, it does not own file handler.
            if (!cachePolicy.hasMemCache()) {
                //Threading: The called may own the CIM.readAccess, perform by dedicated worker to prevent deadlock
                RP.post(new Runnable() {
                    @Override
                    public void run () {
                        try {
                            close(false);
                            LOGGER.log(Level.FINE, "Evicted index: {0}", folder.getAbsolutePath()); //NOI18N
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            } else if ((ref != null && currentCacheSize > maxCacheSize)) {
                ref.clearHRef();
            }
        }
        
        private synchronized void hit() {
            if (!cachePolicy.hasMemCache()) {
                try {
                    final URL url = folder.toURI().toURL();
                    IndexCacheFactory.getDefault().getCache().put(url, this);
                } catch (MalformedURLException e) {
                    Exceptions.printStackTrace(e);
                }
            } else if (ref != null) {
                ref.get();
            }
        }
        
        private Collection<? extends String> getOrphanLock () {
            final List<String> locks = new LinkedList<String>();
            final String[] content = folder.list();
            if (content != null) {
                for (String name : content) {
                    if (name.startsWith(CACHE_LOCK_PREFIX)) {
                        locks.add(name);
                    }
                }
            }
            return locks;
        }
        
        private void checkPreconditions () throws IndexClosedException {
            if (closed) {
                throw new IndexClosedException();
            }
        }
        
        private IOException annotateException (final IOException ioe) {
            final StringBuilder message = new StringBuilder();            
            File[] children = folder.listFiles();
            if (children == null) {
                message.append("Non existing index folder");    //NOI18N
            }
            else {
                message.append("Current Lucene version: ").     //NOI18N
                        append(LucenePackage.get().getSpecificationVersion()).
                        append('(').    //NOI18N
                        append(LucenePackage.get().getImplementationVersion()).
                        append(")\n");    //NOI18N
                for (File c : children) {
                    message.append(c.getName()).append(" f: ").append(c.isFile()).
                    append(" r: ").append(c.canRead()).
                    append(" w: ").append(c.canWrite()).append("\n");  //NOI18N
                }
            }
            return Exceptions.attachMessage(ioe, message.toString());
        }
        
        private static FSDirectory createFSDirectory (final File indexFolder) throws IOException {
            assert indexFolder != null;
            final FSDirectory directory  = FSDirectory.open(indexFolder, lockFactory);
            directory.getLockFactory().setLockPrefix(CACHE_LOCK_PREFIX);
            return directory;
        } 
        
        private static long getCacheSize() {
            float per = -1.0f;
            final String propVal = System.getProperty(PROP_CACHE_SIZE); 
            if (propVal != null) {
                try {
                    per = Float.parseFloat(propVal);
                } catch (NumberFormatException nfe) {
                    //Handled below
                }
            }
            if (per<0) {
                per = DEFAULT_CACHE_SIZE;
            }
            return (long) (per * Runtime.getRuntime().maxMemory());
        }
        
        private final class CleanReference extends SoftReference<RAMDirectory[]> implements Runnable {
            
            @SuppressWarnings("VolatileArrayField")
            private volatile Directory[] hardRef; //clearHRef may be called by more concurrently (read lock).
            private final AtomicLong size = new AtomicLong();  //clearHRef may be called by more concurrently (read lock).

            private CleanReference(final RAMDirectory[] dir) {
                super (dir, Utilities.activeReferenceQueue());
                boolean doHardRef = currentCacheSize < maxCacheSize;
                if (doHardRef) {
                    this.hardRef = dir;
                    long _size = dir[0].sizeInBytes();
                    size.set(_size);
                    currentCacheSize+=_size;
                }
                LOGGER.log(Level.FINEST, "Caching index: {0} cache policy: {1}",    //NOI18N
                new Object[]{
                    folder.getAbsolutePath(),
                    cachePolicy.getSystemName()
                });
            }
            
            @Override
            public void run() {
                try {
                    LOGGER.log(Level.FINEST, "Dropping cache index: {0} cache policy: {1}", //NOI18N
                    new Object[] {
                        folder.getAbsolutePath(),
                        cachePolicy.getSystemName()
                    });
                    close(false);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
            @Override
            public void clear() {
                clearHRef();
                super.clear();
            }
            
            void clearHRef() {
                this.hardRef = null;
                long mySize = size.getAndSet(0);
                currentCacheSize-=mySize;
            }
        }        
    }
    //</editor-fold>

    private static class FlushIndexWriter extends IndexWriter {
        public FlushIndexWriter(Directory d, Analyzer a, boolean create, MaxFieldLength mfl) throws CorruptIndexException, LockObtainFailedException, IOException {
            super(d, a, create, mfl);
        }
        
        /**
         * Accessor to index flush for this package
         * @param triggerMerges
         * @param flushDeletes
         * @throws IOException 
         */
        void callFlush(boolean triggerMerges, boolean flushDeletes) throws IOException {
            // flushStores ignored in Lucene 3.5
            super.flush(triggerMerges, true, flushDeletes);
        }
    }
}
