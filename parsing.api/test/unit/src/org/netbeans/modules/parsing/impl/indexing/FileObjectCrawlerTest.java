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

package org.netbeans.modules.parsing.impl.indexing;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class FileObjectCrawlerTest extends NbTestCase {

    private static final CancelRequest CR = new CancelRequest() {
        @Override
        public boolean isRaised() {
            return false;
        }
    };

    public FileObjectCrawlerTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws IOException {
        clearWorkDir();
        File wd = getWorkDir();
        final FileObject wdFO = FileUtil.toFileObject(wd);
        final FileObject cache = FileUtil.createFolder(wdFO, "cache");

        CacheFolder.setCacheFolder(cache);
    }

    public void testIncludesExcludes() throws IOException {
        final FileObject src = FileUtil.createFolder(FileUtil.toFileObject(getWorkDir()), "src");
        assertNotNull(src);

        populateFolderStructure(new File(getWorkDir(), "src"),
            "p1/Included1.java",
            "p1/Included2.java",
            "p1/a/Included3.java",
            "p1/a/Included4.java",
            "p2/Excluded1.java",
            "p2/Excluded2.java",
            "p2/a/Excluded3.java",
            "p2/a/Excluded4.java"
        );

        ClassPath cp = ClassPathSupport.createClassPath(Arrays.asList(new FilteringPathResourceImplementation() {
            private final Pattern p = Pattern.compile("p1/.*");

            public boolean includes(URL root, String resource) {
                return p.matcher(resource).matches();
            }

            public URL[] getRoots() {
                try {
                    return new URL[]{src.getURL()};
                } catch (FileStateInvalidException ex) {
                    throw new IllegalStateException(ex);
                }
            }

            public ClassPathImplementation getContent() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void addPropertyChangeListener(PropertyChangeListener listener) {}
            public void removePropertyChangeListener(PropertyChangeListener listener) {}
        }));

        FileObjectCrawler crawler = new FileObjectCrawler(src, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), cp.entries().get(0), CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong files collected", crawler.getAllResources(),
                "p1/Included1.java",
                "p1/Included2.java",
                "p1/a/Included3.java",
                "p1/a/Included4.java"
        );
        assertCollectedFiles("Wrong files collected", crawler.getResources(),
                "p1/Included1.java",
                "p1/Included2.java",
                "p1/a/Included3.java",
                "p1/a/Included4.java"
        );
    }

    public void testRelativePaths() throws IOException {
        File root = new File(getWorkDir(), "src");
        String [] paths = new String [] {
                "org/pckg1/file1.txt",
                "org/pckg1/pckg2/file1.txt",
                "org/pckg1/pckg2/file2.txt",
                "org/pckg2/"
        };
        populateFolderStructure(root, paths);

        FileObjectCrawler crawler1 = new FileObjectCrawler(FileUtil.toFileObject(root), EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong files collected", crawler1.getResources(), paths);
        assertCollectedFiles("Wrong files collected", crawler1.getAllResources(), paths);
        
        FileObject folder = FileUtil.toFileObject(new File(root, "org/pckg1/pckg2"));
        FileObjectCrawler crawler2 = new FileObjectCrawler(FileUtil.toFileObject(root), new FileObject [] { folder }, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong files collected from " + folder, crawler2.getResources(),
            "org/pckg1/pckg2/file1.txt",
            "org/pckg1/pckg2/file2.txt"
        );
        assertNull("All resources should not be computed for subtree", crawler2.getAllResources());
    }

    public void testDeletedFiles() throws IOException {
        File root = new File(getWorkDir(), "src");
        String [] paths = new String [] {
                "org/pckg1/file1.txt",
                "org/pckg1/pckg2/file1.txt",
                "org/pckg1/pckg2/file2.txt",
                "org/pckg2/"
        };
        populateFolderStructure(root, paths);

        FileObjectCrawler crawler1 = new FileObjectCrawler(FileUtil.toFileObject(root), EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong files collected", crawler1.getResources(), paths);
        assertCollectedFiles("Wrong files collected", crawler1.getAllResources(), paths);

        FileObject pckg2 = FileUtil.toFileObject(new File(root, "org/pckg1/pckg2"));
        FileObject org = FileUtil.toFileObject(new File(root, "org"));
        org.delete();

        FileObjectCrawler crawler2 = new FileObjectCrawler(FileUtil.toFileObject(root), new FileObject [] { pckg2 }, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("There should be no files in " + root, crawler2.getResources());
        assertNull("All resources should not be computed for subtree", crawler2.getAllResources());

        FileObjectCrawler crawler3 = new FileObjectCrawler(FileUtil.toFileObject(root), EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("There should be no files in " + root, crawler3.getResources());
        assertCollectedFiles("There should be no files in " + root, crawler3.getAllResources());
        assertCollectedFiles("All files in " + root + " should be deleted", crawler3.getDeletedResources());
    }

    public void testAllFilesIndexing() throws Exception {
        File root = new File(getWorkDir(), "src");
        String [] paths = new String [] {
                "org/pckg1/file1.txt",
                "org/pckg1/pckg2/file1.txt",
                "org/pckg1/pckg2/file2.txt",
                "org/pckg2/"
        };
        populateFolderStructure(root, paths);

        //First scan with timestamps enabled (project open)
        FileObjectCrawler crawler1 = new FileObjectCrawler(FileUtil.toFileObject(root), EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE, Crawler.TimeStampAction.CHECK), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong all files collected", crawler1.getAllResources(), paths);
        assertTrue(crawler1.getAllResources() != crawler1.getResources());
        assertEquals(crawler1.getAllResources().size(), crawler1.getResources().size());
        assertCollectedFiles("Wrong files collected", crawler1.getResources(), paths);
        crawler1.storeTimestamps();

        //Second scan with timestamps enabled (project reopen)
        FileObjectCrawler crawler2 = new FileObjectCrawler(FileUtil.toFileObject(root), EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE, Crawler.TimeStampAction.CHECK), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong all files collected", crawler2.getAllResources(), paths);
        assertTrue(crawler2.getAllResources() != crawler2.getResources());
        assertEquals(0, crawler2.getResources().size());
        crawler2.storeTimestamps();

        //Rescan of root with force == false
        FileObjectCrawler crawler3 = new FileObjectCrawler(FileUtil.toFileObject(root), EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE, Crawler.TimeStampAction.CHECK), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong all files collected", crawler3.getAllResources(), paths);
        assertTrue(crawler3.getAllResources() != crawler3.getResources());
        assertEquals(0, crawler3.getResources().size());
        crawler3.storeTimestamps();

        //Rescan of root with force == true
        FileObjectCrawler crawler4 = new FileObjectCrawler(FileUtil.toFileObject(root), EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong all files collected", crawler4.getAllResources(), paths);
        assertTrue(crawler4.getAllResources() == crawler4.getResources());
        crawler4.storeTimestamps();

        //Rescan of specified files (no timestamps)
        final FileObject rootFo = FileUtil.toFileObject(root);
        final FileObject expFile1 = rootFo.getFileObject("org/pckg1/pckg2/file1.txt");
        final FileObject expFile2 = rootFo.getFileObject("org/pckg1/pckg2/file2.txt");
        FileObjectCrawler crawler5 = new FileObjectCrawler(rootFo, new FileObject[] {expFile1, expFile2}, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertNull(crawler5.getAllResources());
        assertCollectedFiles("Wrong files collected", crawler5.getResources(), new String[] {"org/pckg1/pckg2/file1.txt","org/pckg1/pckg2/file2.txt"});
        crawler5.storeTimestamps();

        //Rescan of specified files with timestamps
        FileObjectCrawler crawler6 = new FileObjectCrawler(rootFo, new FileObject[] {expFile1, expFile2}, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE, Crawler.TimeStampAction.CHECK), null, CR, SuspendSupport.NOP);
        assertNull(crawler6.getAllResources());
        assertEquals(0, crawler6.getResources().size());
        crawler6.storeTimestamps();
    }

    public void testSymLinksInRoot() throws Exception {
        final File workDir = getWorkDir();
        final FileObject wd = FileUtil.toFileObject(workDir);
        final FileObject rootWithCycle = wd.createFolder("rootWithCycle");
        final FileObject folder1 = rootWithCycle.createFolder("folder1");
        final FileObject folder2 = rootWithCycle.createFolder("folder2");
        final FileObject inFolder1 = folder1.createFolder("infolder1");
        final FileObject inFolder2 = folder2.createFolder("folder2");
        folder1.createData("data1.txt");
        inFolder1.createData("data2.txt");
        folder2.createData("data3.txt");
        inFolder2.createData("data4.txt");
        final Map<Pair<File,File>,Boolean> linkMap = new HashMap<Pair<File,File>, Boolean>();
        linkMap.put(
            Pair.<File,File>of(
                FileUtil.toFile(folder2),
                FileUtil.toFile(inFolder2)),
            Boolean.TRUE);
        FileObjectCrawler.mockLinkTypes = linkMap;
        final FileObjectCrawler c = new FileObjectCrawler(rootWithCycle, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        final Collection<Indexable> indexables = c.getAllResources();
        assertCollectedFiles("Wring collected files", indexables,
                "folder1/data1.txt",
                "folder1/infolder1/data2.txt",
                "folder2/data3.txt");
    }

    public void testSymLinksFromRoot() throws Exception {
        final File workDir = getWorkDir();
        final FileObject wd = FileUtil.toFileObject(workDir);
        final FileObject cycleTarget= wd.createFolder("cycleTarget");
        final FileObject rootWithCycle = cycleTarget.createFolder("rootWithExtLink");
        final FileObject folder1 = rootWithCycle.createFolder("folder1");
        final FileObject folder2 = rootWithCycle.createFolder("folder2");
        final FileObject inFolder1 = folder1.createFolder("infolder1");
        final FileObject inFolder2 = folder2.createFolder("cycleTarget");
        folder1.createData("data1.txt");
        inFolder1.createData("data2.txt");
        folder2.createData("data3.txt");
        inFolder2.createData("data4.txt");
        final Map<Pair<File,File>,Boolean> linkMap = new HashMap<Pair<File,File>, Boolean>();
        linkMap.put(
            Pair.<File,File>of(
                FileUtil.toFile(cycleTarget),
                FileUtil.toFile(inFolder2)),
            Boolean.TRUE);
        FileObjectCrawler.mockLinkTypes = linkMap;
        final FileObjectCrawler c = new FileObjectCrawler(rootWithCycle, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        final Collection<Indexable> indexables = c.getAllResources();
        assertCollectedFiles("Wring collected files", indexables,
                "folder1/data1.txt",
                "folder1/infolder1/data2.txt",
                "folder2/data3.txt");
    }

    protected void assertCollectedFiles(String message, Collection<Indexable> resources, String... expectedPaths) throws IOException {
        Set<String> collectedPaths = new HashSet<String>();
        for(Indexable ii : resources) {
            collectedPaths.add(ii.getRelativePath());
        }
        Set<String> expectedPathsFiltered = new HashSet<String>();
        for(String path : expectedPaths) {
            if (!path.endsWith("/")) { // crawler only collects files
                expectedPathsFiltered.add(path);
            }
        }
        assertEquals(message, expectedPathsFiltered, collectedPaths);
    }

    private static void populateFolderStructure(File root, String... filesOrFolders) throws IOException {
        root.mkdirs();
        for(String fileOrFolder : filesOrFolders) {
            if (fileOrFolder.endsWith("/")) {
                // folder
                File folder = new File(root, fileOrFolder.substring(0, fileOrFolder.length() - 1));
                folder.mkdirs();
            } else {
                // file
                File file = new File(root, fileOrFolder);
                File folder = file.getParentFile();
                folder.mkdirs();
                FileUtil.createData(FileUtil.toFileObject(folder), file.getName());
            }
        }
    }
}