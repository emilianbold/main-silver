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
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class ScanStartedTest extends NbTestCase {
    
    public static final String FOO_EXT = "foo";          //NOI18N
    public static final String FOO_MIME = "text/x-foo";  //NOI18N
    public static final String FOO_SOURCE = "foo-sources";  //NOI18N

    private final Map<String, Map<ClassPath,Void>> registeredClasspaths = new HashMap<String, Map<ClassPath,Void>>();
    
    private IndexerFactory factory1;
    private IndexerFactory factory2;
    private IndexerFactory factory3;

    private FileObject src1;
    private FileObject file1;
    private ClassPath cp1;

    public ScanStartedTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();


        this.clearWorkDir();
        final File _wd = this.getWorkDir();
        final FileObject wd = FileUtil.toFileObject(_wd);
        assertNotNull("No masterfs",wd);                    //NOI18N
        final FileObject cache = wd.createFolder("cache");  //NOI18N
        CacheFolder.setCacheFolder(cache);
        src1 = wd.createFolder("src1");     //NOI18N
        assertNotNull(src1);
        file1 = src1.createData("test", FOO_EXT);   //NOI18N
        assertNotNull(file1);
        FileUtil.setMIMEType(FOO_EXT, FOO_MIME);

        factory1 = new IndexerFactory("factory1", 1);   //NOI18N
        factory2 = new IndexerFactory("factory2", 1);   //NOI18N
        factory3 = new IndexerFactory("factory3", 1);   //NOI18N

        cp1 = ClassPathSupport.createClassPath(src1);
        FooCPP.roots2cps = Collections.unmodifiableMap(Collections.singletonMap(src1, cp1));
        MockServices.setServices(
                FooCPP.class,
                FooPathRecognizer.class);
        MockMimeLookup.setInstances(MimePath.get(FOO_MIME), factory1, factory2, factory3);
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


    public void testScanFinishedAfterScanStarted() throws Exception {
        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCE).isEmpty());
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);

        //Testing classpath registration
        globalPathRegistry_register(FOO_SOURCE,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.src1.toURL(), handler.getSources().get(0));
        assertEquals(1, factory1.scanStartedCount.get());
        assertEquals(1, factory2.scanStartedCount.get());
        assertEquals(1, factory3.scanStartedCount.get());
        assertEquals(1, factory1.scanFinishedCount.get());
        assertEquals(1, factory2.scanFinishedCount.get());
        assertEquals(1, factory3.scanFinishedCount.get());
    }


    public void testScanFinishedAfterScanStartedWithException() throws Exception {
        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCE).isEmpty());
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);

        factory2.throwFromScanStarted = new RuntimeException();

        //Testing classpath registration
        globalPathRegistry_register(FOO_SOURCE,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.src1.toURL(), handler.getSources().get(0));
        assertEquals(1, factory1.scanStartedCount.get());
        assertEquals(1, factory2.scanStartedCount.get());
        assertEquals(1, factory3.scanStartedCount.get());
        assertEquals(1, factory1.scanFinishedCount.get());
        assertEquals(1, factory2.scanFinishedCount.get());
        assertEquals(1, factory3.scanFinishedCount.get());
    }


    public void testScanFinishedAfterScanStartedWithInternalException() throws Exception {
        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCE).isEmpty());
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);

        handler.internalException = Pair.<String,RuntimeException>of(
                factory2.getIndexerName(),
                new RuntimeException());    //Symulate internal exception

        //Testing classpath registration
        globalPathRegistry_register(FOO_SOURCE,new ClassPath[]{cp1});
        assertFalse(handler.await(RepositoryUpdaterTest.NEGATIVE_TIME));
        assertEquals(0, handler.getBinaries().size());
        assertNull(handler.getSources());
        assertEquals(1, factory1.scanStartedCount.get());
        assertEquals(0, factory2.scanStartedCount.get());
        assertEquals(0, factory3.scanStartedCount.get());
        assertEquals(1, factory1.scanFinishedCount.get());
        assertEquals(0, factory2.scanFinishedCount.get());
        assertEquals(0, factory3.scanFinishedCount.get());
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


    public static class FooCPP implements ClassPathProvider {

        static volatile Map<FileObject,ClassPath> roots2cps;

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            if (FOO_SOURCE.equals(type)) {
                Map<FileObject,ClassPath> m = roots2cps;
                if (m != null) {
                    for (Map.Entry<FileObject,ClassPath> p : m.entrySet()) {
                        if (p.getKey().equals(file) || FileUtil.isParentOf(p.getKey(), file)) {
                            return p.getValue();
                        }
                    }
                }
            }
            return null;
        }

    }


    public static class FooPathRecognizer extends PathRecognizer {

        @Override
        public Set<String> getSourcePathIds() {
            return Collections.<String>singleton(FOO_SOURCE);
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


    private static class Indexer extends CustomIndexer {
        @Override
        protected void index(Iterable<? extends Indexable> files, Context context) {
        }
    }

    private static class IndexerFactory extends CustomIndexerFactory {

        private final String name;
        private final int version;

        private final AtomicInteger scanStartedCount = new AtomicInteger();
        private final AtomicInteger scanFinishedCount = new AtomicInteger();

        volatile RuntimeException throwFromScanStarted;

        IndexerFactory(
            @NonNull final String name,
            final int version) {
            this.name = name;
            this.version = version;
        }

        @Override
        public CustomIndexer createIndexer() {
            return new Indexer();
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return false;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        }

        @Override
        public String getIndexerName() {
            return name;
        }

        @Override
        public int getIndexVersion() {
            return version;
        }

        @Override
        public boolean scanStarted(Context context) {
            scanStartedCount.incrementAndGet();
            RuntimeException re = throwFromScanStarted;
            if (re != null) {
                throw re;
            }
            return true;
        }

        @Override
        public void scanFinished(Context context) {
            scanFinishedCount.incrementAndGet();
        }



    }

    private static class TestHandler extends RepositoryUpdaterTest.TestHandler {
        volatile Pair<String,RuntimeException> internalException;

        @Override
        public void publish(LogRecord record) {
            final String msg = record.getMessage();
            final Pair<String,RuntimeException> ie  = internalException;
            if (ie != null &&
                msg != null &&
                msg.startsWith("scanStarting:") &&  //NOI18N
                ie.first.equals(record.getParameters()[0])) {
                throw ie.second;
            } else {
                super.publish(record);
            }
        }


    }

}
