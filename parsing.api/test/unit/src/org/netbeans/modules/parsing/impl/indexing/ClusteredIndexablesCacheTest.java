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
package org.netbeans.modules.parsing.impl.indexing;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.impl.indexing.lucene.DocumentBasedIndexManager;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.DocumentIndexCache;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public class ClusteredIndexablesCacheTest extends NbTestCase {

    private static final String FOO_EXT = "foo";    //NOI18N
    private static final String FOO_MIME = "text/x-foo";    //NOI18N
    private static final String FOO_SOURCES = "foo-src";    //NOI18N

    private final Map<String, Map<ClassPath,Void>> registeredClasspaths = new HashMap<String, Map<ClassPath,Void>>();

    private FileObject src1;
    private FileObject file1;
    private FileObject file2;
    private FileObject file3;
    private FileObject file4;
    private ClassPath cp1;

    public ClusteredIndexablesCacheTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final File wd = getWorkDir();
        final FileObject wdo = FileUtil.toFileObject(wd);
        assertNotNull("No masterfs",wdo);   //NOI18N
        final FileObject cache = wdo.createFolder("cache"); //NOI18N
        CacheFolder.setCacheFolder(cache);
        src1 = wdo.createFolder("src1");        //NOI18N
        assertNotNull(src1);        
        file1 = src1.createData("test", FOO_EXT);   //NOI18N
        assertNotNull(file1);
        file2 = src1.createData("test2", FOO_EXT);  //NOI18N
        assertNotNull(file2);
        file3 = src1.createData("test3", FOO_EXT);  //NOI18N
        assertNotNull(file3);
        file4 = src1.createData("test4", FOO_EXT);  //NOI18N
        assertNotNull(file4);
        FileUtil.setMIMEType(FOO_EXT, FOO_MIME);
        cp1 = ClassPathSupport.createClassPath(src1);        
        MockServices.setServices(FooPathRecognizer.class);
        MockMimeLookup.setInstances(MimePath.get(FOO_MIME), new FooIndexerFactory());
        RepositoryUpdaterTest.setMimeTypes(FOO_MIME);
        RepositoryUpdaterTest.waitForRepositoryUpdaterInit();
    }

    @Override
    protected void tearDown() throws Exception {
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        try {
            logger.setLevel (Level.FINEST);
            logger.addHandler(handler);
            for(String id : registeredClasspaths.keySet()) {
                final Map<ClassPath,Void> classpaths = registeredClasspaths.get(id);
                GlobalPathRegistry.getDefault().unregister(id, classpaths.keySet().toArray(new ClassPath[classpaths.size()]));
            }
            handler.await();
        } finally {
            logger.removeHandler(handler);
        }
        super.tearDown();
    }


    public void testNoOutOfOrderFiles() throws InterruptedException, IOException {

        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES).isEmpty());
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        handler.beforeScanFinishedAction = new NoOutOfOrderPredicate();
        globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.src1.toURL(), handler.getSources().get(0));
        assertEquals(Boolean.TRUE, handler.res);
        QuerySupport qs = QuerySupport.forRoots(FooIndexerFactory.NAME, FooIndexerFactory.VERSION, src1);
        Collection<? extends IndexResult> res = qs.query("_sn", "", QuerySupport.Kind.PREFIX, (String[]) null);   //NOI18N
        assertEquals(4, res.size());

        handler.reset();
        globalPathRegistry_unregister(FOO_SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());

        file3.delete();
        file4.delete();
        handler.reset();
        handler.beforeScanFinishedAction = new NoOutOfOrderPredicate();
        globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.src1.toURL(), handler.getSources().get(0));
        assertEquals(Boolean.TRUE, handler.res);
        qs = QuerySupport.forRoots(FooIndexerFactory.NAME, FooIndexerFactory.VERSION, src1);
        res = qs.query("_sn", "", QuerySupport.Kind.PREFIX, (String[]) null);   //NOI18N
        assertEquals(2, res.size());

    }

    private void globalPathRegistry_register(String id, ClassPath [] classpaths) {
        Map<ClassPath,Void> map = registeredClasspaths.get(id);
        if (map == null) {
            map = new IdentityHashMap<ClassPath, Void>();
            registeredClasspaths.put(id, map);
        }
        for (ClassPath cp :  classpaths) {
            map.put(cp,null);
        }
        GlobalPathRegistry.getDefault().register(id, classpaths);
    }

    private final void globalPathRegistry_unregister(String id, ClassPath [] classpaths) {
        GlobalPathRegistry.getDefault().unregister(id, classpaths);
        final Map<ClassPath,Void> map = registeredClasspaths.get(id);
        if (map != null) {
            map.keySet().removeAll(Arrays.asList(classpaths));
        }
    }


    public static final class FooPathRecognizer extends PathRecognizer {

        @Override
        public Set<String> getSourcePathIds() {
            return Collections.<String>singleton(FOO_SOURCES);
        }

        @Override
        public Set<String> getLibraryPathIds() {
            return Collections.<String>emptySet();
        }

        @Override
        public Set<String> getBinaryLibraryPathIds() {
            return Collections.<String>emptySet();
        }

        @Override
        public Set<String> getMimeTypes() {
            return Collections.<String>singleton(FOO_MIME);
        }
    }

    private static class FooIndexerFactory extends CustomIndexerFactory {

        private static final String NAME = "FooIndexer";    //NOI18N
        private static final int VERSION = 1;
        

        @Override
        public CustomIndexer createIndexer() {
            return new CustomIndexer() {
                @Override
                protected void index(Iterable<? extends Indexable> files, Context context) {
                    try {
                        final IndexingSupport is = IndexingSupport.getInstance(context);
                        for (Indexable i : files) {
                            final IndexDocument doc = is.createDocument(i);
                            is.addDocument(doc);
                        }
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }                    
                }
            };
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return false;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            try {
                final IndexingSupport is = IndexingSupport.getInstance(context);
                for (Indexable i : deleted) {
                    is.removeDocuments(i);
                }
            } catch (IOException  ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        }

        @Override
        public String getIndexerName() {
            return NAME;
        }

        @Override
        public int getIndexVersion() {
            return VERSION;
        }        
    }

    private static class NoOutOfOrderPredicate implements Convertor<Pair<URL,String>, Boolean> {

        @Override
        public Boolean convert(@NonNull final Pair<URL,String> p) {
            try {
                final FileObject cacheFolder = CacheFolder.getDataFolder(p.first);
                final FileObject indexer = cacheFolder.getFileObject(p.second);
                final FileObject indexFolder = indexer.getFileObject("1/1");
                final DocumentIndexCache cache = DocumentBasedIndexManager.getDefault().getCache(indexFolder.toURL());
                final Class<?> c = Class.forName("org.netbeans.modules.parsing.impl.indexing.ClusteredIndexables$DocumentIndexCacheImpl");  //NOI18N
                final Field f = c.getDeclaredField("toDeleteOutOfOrder");   //NOI18N
                f.setAccessible(true);
                final Collection<?> data = (Collection<?>) f.get(cache);
                return data.isEmpty();
            } catch (Exception e) {
                return Boolean.FALSE;
            }
        }

    }

    private class TestHandler extends RepositoryUpdaterTest.TestHandler {

        volatile Convertor<Pair<URL,String>,Boolean> beforeScanFinishedAction;
        volatile Boolean res;

        @Override
        public void reset () {
            beforeScanFinishedAction = null;
            res = null;
            super.reset();
        }

        @Override
        public void reset(final Type t) {
            beforeScanFinishedAction = null;
            res = null;
            super.reset(t);
        }

        @Override
        public void reset(final Type t, int initialCount) {
            beforeScanFinishedAction = null;
            res = null;
            super.reset(t, initialCount);
        }

        @Override
        public void publish(LogRecord record) {
            final Convertor<Pair<URL,String>,Boolean> action = beforeScanFinishedAction;
            final String message = record.getMessage();
            if (action != null && "scanFinishing:{0}:{1}".equals(message)) {    //NOI18N
                try {
                    res = action.convert(
                        Pair.<URL,String>of(
                            new URL ((String)record.getParameters()[1]),
                            (String)record.getParameters()[0]));
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            super.publish(record);
        }

    }
}
