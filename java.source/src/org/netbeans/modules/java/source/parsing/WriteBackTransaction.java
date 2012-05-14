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
package org.netbeans.modules.java.source.parsing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.util.Iterators;
import org.openide.util.Utilities;

/**
 * 
 * @author sdedic
 */
class WriteBackTransaction extends FileManagerTransaction {
    private static final Logger LOG = Logger.getLogger(WriteBackTransaction.class.getName());
    
    private final Set<File> deleted;
    
    /**
     * For testing purposes, disables caching, flushes every file
     */
    static boolean disableCache;
    
    private URL root;

    WriteBackTransaction(URL root) {
        super(true);
        this.root = root;
        
        deleted = new HashSet<File>();
        createCacheRef();
    }

    @Override
    public void delete(@NonNull final File file) {
        assert file != null;
        deleted.add(file);
    }
    /**
     * Guard reference, when it is freed, the storage cache will be flushed
     */
    private Reference<Map[]> cacheRef;
    /**
     * Index of generated files, files are kept here until committed, although
     * the files might be already flushed to shadow storage
     */
    private Map<String, Map<File, CachedFileObject>> contentCache = new HashMap<String, Map<File, CachedFileObject>>();

    /**
     * Flag to indicate that the memory storage should be flushed; the flag is set
     * from CacheRef, and reset by flush()
     */
    volatile boolean memExhausted;
    
    /**
     * Set of work dirs. One workdir is expected in practice, 
     * as the Transaction should work for one source root only.
     */
    private Collection<File> workDirs = new HashSet<File>();

    private void createCacheRef() {
        cacheRef = new CacheRef(this, new Map[]{contentCache});
        memExhausted = false;
    }

    void addFile(String packageName, CachedFileObject fo) {
        LOG.log(Level.FINE, "File added to cache:{0}:{1}", new Object[] { fo.getFile(), root });
        // check whether the softref has been freed:
        Map<File, CachedFileObject> dirContent = contentCache.get(packageName);
        if (dirContent == null) {
            dirContent = new HashMap<File, CachedFileObject>();
            contentCache.put(packageName, dirContent);
        }
        dirContent.put(toFile(fo), fo);
    }

    Collection<File> listDir(String dir) {
        Map<File, CachedFileObject> content = contentCache.get(dir);
        return content == null ? Collections.<File>emptyList() : content.keySet();
    }
    
    private URL getRootDir() {
        return root;
    }

    /**
     * This method makes a copy of the storage
     * @param dir
     * @return
     */
    Collection<JavaFileObject> getFileObjects(String dir) {
        Map<File, CachedFileObject> content = contentCache.get(dir);
        return new ArrayList<JavaFileObject>(content.values());
    }

    void maybeFlush() throws IOException {
        if (disableCache || memExhausted) {
            LOG.log(Level.FINE, "Memory exhausted:{0}", getRootDir());
            flushFiles(false);
            memExhausted = false;
            createCacheRef();
        }
    }

    @Override
    @NonNull
    Iterable<JavaFileObject> filter(String packageName, Iterable<JavaFileObject> files) {
        final Collection<File> added = listDir(packageName);
        Iterable<JavaFileObject> res = files;
        if (deleted.isEmpty() && added.isEmpty()) {
            return res;
        }
        if (added.isEmpty()) {
            // just filter out the deleted files
            return Iterators.filter(res, new Comparable<JavaFileObject>() {

                public int compareTo(@NonNull final JavaFileObject o) {
                    final File f = toFile(o);
                    return deleted.contains(f) ? 0 : -1;
                }
            });
        }
        Collection<JavaFileObject> toAdd = getFileObjects(packageName);
        Collection<Iterable<JavaFileObject>> chain = new ArrayList<Iterable<JavaFileObject>>(2);
        chain.add(toAdd);
        
        chain.add(deleted.isEmpty()?
            res:
            Iterators.filter (
                res,
                new Comparable<JavaFileObject>() {
                    public int compareTo(@NonNull final JavaFileObject o) {
                        final File f = toFile(o);
                        return deleted.contains(f) ? 0 : -1;
                    }
                }
        ));
        return Iterators.chained(chain);
    }

    @Override
    JavaFileObject createFileObject(@NonNull final File file, @NonNull final File root, @NullAllowed final JavaFileFilterImplementation filter, @NullAllowed final Charset encoding) {
        final String[] pkgNamePair = FileObjects.getFolderAndBaseName(FileObjects.getRelativePath(root, file), File.separatorChar);
        String pname = FileObjects.convertFolder2Package(pkgNamePair[0], File.separatorChar);
        CachedFileObject cfo = getFileObject(pname, pkgNamePair[1]);
        if (cfo != null) {
            return cfo;
        }
        String relPath = FileObjects.getRelativePath(root, file);
        File shadowRoot = new File(root.getParent(), root.getName() + WORK_SUFFIX);
        File shadowFile = new File(shadowRoot, relPath);
        workDirs.add(shadowRoot);
        cfo = new CachedFileObject(this, file, pname, pkgNamePair[1], filter, encoding);
        cfo.setShadowFile(shadowFile);
        if (!shadowRoot.mkdirs() && !shadowRoot.exists() && !shadowRoot.isDirectory()) {
            throw new IllegalStateException();
        }
        return cfo;
    }

    @CheckForNull
    CachedFileObject getFileObject(@NonNull final String dir, @NonNull final String file) {
        final Map<File, CachedFileObject> content = contentCache.get(dir);
        if (content != null) {
            for (Map.Entry<File, CachedFileObject> en : content.entrySet()) {
                if (file.equals(en.getKey().getName())) {
                    return en.getValue();
                }
            }
        }
        return null;
    }
    private static final String WORK_SUFFIX = ".work";

    @Override
    protected void commit() throws IOException {
        LOG.log(Level.FINE, "Committed:{0}", getRootDir());
        try {
            for (File f : deleted) {
                f.delete();
            }
            flushFiles(true);
        } finally {
            clean();
        }
    }

    void flushFiles(boolean inCommit) throws IOException {
        LOG.log(Level.FINE, "Flushing:{0}", getRootDir());
        for (Map<File, CachedFileObject> dirContent : contentCache.values()) {
            for (CachedFileObject cfo : dirContent.values()) {
                cfo.flush(inCommit);
                if (inCommit) {
                    cfo.commit();
                }
            }
        }
    }

    @Override
    protected void rollBack() throws IOException {
        clean();
    }

    private void clean() {
        // remove the workdir(s)
        for (File d : workDirs) {
            FileObjects.deleteRecursively(d);
        }
        deleted.clear();
        contentCache.clear();
    }

    @Override
    JavaFileObject readFileObject(String dirName, String relativeName) {
        return getFileObject(dirName, relativeName);
    }
    
    /**
     * Simple guard against memory overflow; when the reference is freed, it instructs
     * Storage to flush on next file addition.
     */
    private static class CacheRef extends SoftReference implements Runnable {
        private WriteBackTransaction storage;

        public CacheRef(WriteBackTransaction storage, Object referent) {
            super(referent, Utilities.activeReferenceQueue());
            this.storage = storage;
        }

        @Override
        public void run() {
            LOG.fine("Reference freed");
            storage.memExhausted = true;
        }
    }
    
    /**
     * Helper that extracts j.o.File from the JFO instance
     * @param o JFO instance
     * @return the File represented by the JFO
     */
    static File toFile(JavaFileObject o) {
        File f = ((FileObjects.FileBase)o).f;
        return f;
    }

    /**
     * File object, whose contents is cached in memory and eventually flushed to the disk.
     * When flushed, methods will delegate to a plain regular FileObject over j.o.File,
     * so dangling references still work.
     * 
     * Note that these FOs only work during transaction; the super.f File is a key for comparison
     * with regular FileObjects, so it points to the REAL location after commit. The tx storage is
     * stored in shadowFile.
     * 
     * The CacheFO may be "flushed" (written to shadowFile) or "committed" (moved to the final dest).
     */
    static class CachedFileObject extends FileObjects.FileBase {

        private static final byte[] NOTHING = new byte[0];
        
        byte[] content = NOTHING;
        WriteBackTransaction   writer;
        FileObjects.FileBase    delegate;

        /**
         * The final file, after the tx is committed
         */
        File shadowFile;
        
        public CachedFileObject(WriteBackTransaction wb, File file, String pkgName, String name, JavaFileFilterImplementation filter, Charset encoding) {
            super(file, pkgName, name, filter, encoding);
            this.writer = wb;
        }
        
        /**
         * Computes root of the file hierarchy given an output file and package name. Goes
         * up one directory for each package name segment (delimited by .)
         * 
         * @param startFrom the class/resource output file
         * @param pkgName package name of the class/resource
         * @return File representing the root of the package structure
         */
        private static File getRootFile(File startFrom, String pkgName) {
            int index = -1;
            
            while ((index = pkgName.indexOf('.', index + 1)) != -1) {
                startFrom = startFrom.getParentFile();
            }
            return startFrom;
        }
        
        private JavaFileObject createDelegate() {
            if (wasCommitted()) {
                delegate = (FileObjects.FileBase)FileObjects.fileFileObject(f, getRootFile(getFile(), getPackage()), filter, encoding);
            } else if (wasFlushed()) {
                delegate = (FileObjects.FileBase)FileObjects.fileFileObject(shadowFile, getRootFile(shadowFile, getPackage()), filter, encoding);
            }
            return delegate;
        }
        
        public File getCurrentFile() {
            return shadowFile;
        }

        /**
         * Flushes buffered content and releases this object. 
         * 
         * @throws IOException if file cannot be written, or the target directory created
         */
        void flush(boolean inCommit) throws IOException {
            if (wasFlushed()) {
                return;
            }

            // create directories up to the parent
            if (inCommit) {
                shadowFile = this.f;
            }
            
            File f = getCurrentFile();
            if (!f.getParentFile().mkdirs() && !f.getParentFile().exists()) {
                throw new IOException();
            }
            final FileOutputStream out = new FileOutputStream(f);
            try {
                out.write(content);
                release();
            } finally {
                out.close();
            }
        }
        
        void setShadowFile(File f) {
            this.shadowFile = f;
        }
        
        boolean wasCommitted() {
            return shadowFile == null || shadowFile == f;
        }
        
        boolean wasFlushed() {
            return writer == null;
        }
        
        void commit() throws IOException {
            if (wasCommitted()) {
                return;
            }
            flush(true);
            File cur = getCurrentFile();
            if (f.equals(cur)) {
                return;
            }
            if (f.exists() && !f.delete()) {
                throw new IOException("Cannot delete obsolete sigfile");
            }
            cur.renameTo(f);
            shadowFile = null;
        }
        
        /**
         * Releases data held by the cache, and redirects all calls to a regular
         * File-based FileObject
         */
        void release() {
            content = null;
            writer = null;
            createDelegate();
        }

        @Override
        public boolean delete() {
            if (delegate != null) {
                return delegate.delete();
            } else {
                if (writer != null) {
                    writer.delete(toFile(this));
                }
                return true;
            }
        }

        @Override
        public InputStream openInputStream() throws IOException {
            if (delegate != null) {
                return delegate.openInputStream();
            } else {
                return new ByteArrayInputStream(content);
            }
        } 

        @Override
        public OutputStream openOutputStream() throws IOException {
            if (writer != null) {
                writer.addFile(getPackage(), this);
            }
            if (delegate != null) {
                return delegate.openOutputStream();
            } else {
                return new ByteArrayOutputStream() {
                    boolean closed;
                    
                    @Override
                    public void close() throws IOException {
                        // prevent work when close() called multiple times
                        if (closed) {
                            return;
                        }
                        super.close();
                        closed = true;
                        content = toByteArray();
                        writer.maybeFlush();
                    }
                };
            }
        }
    }
}
