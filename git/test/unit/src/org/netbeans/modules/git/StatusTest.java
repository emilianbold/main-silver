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

package org.netbeans.modules.git;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.progress.StatusProgressMonitor;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ondra
 */
public class StatusTest extends AbstractGitTestCase {

    public StatusTest (String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Git.STATUS_LOG.setLevel(Level.ALL);
    }

    public void testStatusOnNoRepository () throws Exception {
        File folder = createFolder(repositoryLocation.getParentFile(), "folder");
        GitClient client = getClient(folder);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { folder }, StatusProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(statuses.isEmpty());
    }

    public void testStatusDifferentTree () throws IOException {
        try {
            File folder = createFolder("folder");
            getCache().refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(folder)));
            fail("different tree, exception should be thrown");
        } catch (IllegalArgumentException ex) {
            // OK
        }
    }

    public void testCacheRefresh () throws Exception {
        FileStatusCache cache = getCache();
        File unversionedFile = createFile("file");
        // create new files
        Set<File> newFiles = new HashSet<File>();
        File newFile;
        newFiles.add(newFile = createFile(repositoryLocation, "file"));
        assertTrue(cache.getStatus(unversionedFile).getStatus().equals(EnumSet.of(Status.STATUS_NOTVERSIONED_NOTMANAGED)));
        assertTrue(cache.getStatus(newFile).getStatus().equals(EnumSet.of(Status.STATUS_VERSIONED_UPTODATE)));

        cache.refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(repositoryLocation)));
        assertSameStatus(newFiles, Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE);
        assertEquals(1, cache.listFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES).length);
        assertTrue(cache.containsFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES, true));

        newFiles.add(newFile = createFile(repositoryLocation, "file2"));
        assertTrue(cache.getStatus(unversionedFile).getStatus().equals(EnumSet.of(Status.STATUS_NOTVERSIONED_NOTMANAGED)));
        assertTrue(cache.getStatus(newFile).getStatus().equals(EnumSet.of(Status.STATUS_VERSIONED_UPTODATE)));
        cache.refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(repositoryLocation)));
        assertSameStatus(newFiles, Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE);
        assertEquals(2, cache.listFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES).length);
        assertTrue(cache.containsFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES, true));

        // try refresh on a single file, other statuses should not change
        newFiles.add(newFile = createFile(repositoryLocation, "file3"));
        assertTrue(cache.getStatus(unversionedFile).getStatus().equals(EnumSet.of(Status.STATUS_NOTVERSIONED_NOTMANAGED)));
        assertTrue(cache.getStatus(newFile).getStatus().equals(EnumSet.of(Status.STATUS_VERSIONED_UPTODATE)));
        cache.refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(newFile)));
        assertSameStatus(newFiles, Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE);
        assertEquals(3, cache.listFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES).length);
        assertTrue(cache.containsFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES, true));

        // try refresh on a subfolder file, other statuses should not change
        File folder = createFolder(repositoryLocation, "folder");
        assertTrue(cache.getStatus(unversionedFile).getStatus().equals(EnumSet.of(Status.STATUS_NOTVERSIONED_NOTMANAGED)));
        cache.refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(folder)));
        assertSameStatus(newFiles, Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE);
        assertEquals(3, cache.listFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES).length);
        assertTrue(cache.containsFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES, true));

        // try refresh on a subfolder file, other statuses should not change
        newFiles.add(newFile = createFile(folder, "file4"));
        assertTrue(cache.getStatus(unversionedFile).getStatus().equals(EnumSet.of(Status.STATUS_NOTVERSIONED_NOTMANAGED)));
        assertTrue(cache.getStatus(newFile).getStatus().equals(EnumSet.of(Status.STATUS_VERSIONED_UPTODATE)));
        cache.refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(newFile)));
        assertSameStatus(newFiles, Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE);
        assertEquals(4, cache.listFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES).length);
        assertTrue(cache.containsFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES, true));
    }

    public void testPingRepository_Refresh () throws Exception {
        File folderA = new File(repositoryLocation, "folderA");
        File fileA1 = new File(folderA, "file1");
        File fileA2 = new File(folderA, "file2");
        folderA.mkdirs();
        fileA1.createNewFile();
        fileA2.createNewFile();
        File folderB = new File(repositoryLocation, "folderB");
        File fileB1 = new File(folderB, "file1");
        File fileB2 = new File(folderB, "file2");
        folderB.mkdirs();
        fileB1.createNewFile();
        fileB2.createNewFile();
        File folderC = new File(repositoryLocation, "folderC");
        File fileC1 = new File(folderC, "file1");
        File fileC2 = new File(folderC, "file2");
        folderC.mkdirs();
        fileC1.createNewFile();
        fileC2.createNewFile();

        LogHandler handler = new LogHandler(getWorkDir());
        Git.STATUS_LOG.addHandler(handler);
        handler.setFilesToRefresh(Collections.singleton(folderA));
        FileInformation status = getCache().getStatus(folderA);
        assertTrue(status.containsStatus(Status.STATUS_VERSIONED_UPTODATE));
        assertTrue(handler.waitForFilesToRefresh());
        status = getCache().getStatus(fileA1);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE));
        status = getCache().getStatus(fileA2);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE));

        handler.setFilesToRefresh(Collections.singleton(fileA2));
        status = getCache().getStatus(fileA2);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE));
        assertFalse(handler.waitForFilesToRefresh());
        assertFalse(handler.filesRefreshed);

        handler.setFilesToRefresh(Collections.singleton(fileB1));
        status = getCache().getStatus(fileB1);
        assertTrue(status.containsStatus(Status.STATUS_VERSIONED_UPTODATE));
        assertTrue(handler.waitForFilesToRefresh());
        status = getCache().getStatus(fileB1);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE));

        handler.setFilesToRefresh(Collections.singleton(fileB2));
        status = getCache().getStatus(fileB2);
        assertTrue(status.containsStatus(Status.STATUS_VERSIONED_UPTODATE));
        assertTrue(handler.waitForFilesToRefresh());
        status = getCache().getStatus(fileB1);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE));
        status = getCache().getStatus(fileB2);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE));

        handler.setFilesToRefresh(Collections.singleton(folderC));
        status = getCache().getStatus(folderC);
        assertTrue(status.containsStatus(Status.STATUS_VERSIONED_UPTODATE));
        assertTrue(handler.waitForFilesToRefresh());
        status = getCache().getStatus(folderC);
        assertTrue(status.containsStatus(Status.STATUS_VERSIONED_UPTODATE));
        status = getCache().getStatus(fileC1);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE));
        status = getCache().getStatus(fileC2);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE));

        handler.setFilesToRefresh(Collections.singleton(folderB));
        status = getCache().getStatus(folderB);
        assertTrue(status.containsStatus(Status.STATUS_VERSIONED_UPTODATE));
        assertTrue(handler.waitForFilesToRefresh());
        status = getCache().getStatus(fileB1);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE));
        status = getCache().getStatus(fileB2);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE));

        handler.setFilesToRefresh(Collections.singleton(folderB));
        status = getCache().getStatus(folderB);
        assertFalse(handler.waitForFilesToRefresh());

        handler.setFilesToRefresh(Collections.singleton(repositoryLocation));
        status = getCache().getStatus(repositoryLocation);
        assertTrue(handler.waitForFilesToRefresh());
    }

    public void testIgnoredFile () throws Exception {
        File folderA = new File(repositoryLocation, "folderA");
        File fileA1 = new File(folderA, "file1");
        File fileA2 = new File(folderA, "file2");
        folderA.mkdirs();
        fileA1.createNewFile();
        fileA2.createNewFile();

        File ignoreFile = new File(repositoryLocation, ".gitignore");
        write(ignoreFile, "file1");

        LogHandler handler = new LogHandler(getWorkDir());
        Git.STATUS_LOG.addHandler(handler);
        handler.setFilesToRefresh(Collections.singleton(fileA1));
        FileInformation status = getCache().getStatus(fileA1);
        assertTrue(status.containsStatus(Status.STATUS_VERSIONED_UPTODATE));
        assertTrue(handler.waitForFilesToRefresh());
        status = getCache().getStatus(fileA1);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_EXCLUDED));

        write(ignoreFile, "");
        handler.setFilesToRefresh(Collections.singleton(fileA1));
        getCache().refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(fileA1)));
        status = getCache().getStatus(fileA1);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE));

        write(ignoreFile, "folderA");
        handler.setFilesToRefresh(Collections.singleton(fileA2));
        status = getCache().getStatus(fileA2);
        assertTrue(status.containsStatus(Status.STATUS_VERSIONED_UPTODATE));
        assertTrue(handler.waitForFilesToRefresh());
        status = getCache().getStatus(fileA2);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_EXCLUDED));
        handler.setFilesToRefresh(Collections.singleton(folderA));
        status = getCache().getStatus(folderA);
        assertTrue(status.containsStatus(Status.STATUS_VERSIONED_UPTODATE)); // should be excluded actually
        assertTrue(handler.waitForFilesToRefresh());
        status = getCache().getStatus(fileA1);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_EXCLUDED));

        write(ignoreFile, "");
        getCache().refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(folderA)));
        status = getCache().getStatus(fileA1);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE));
        status = getCache().getStatus(fileA2);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE));

        ignoreFile = new File(folderA, ".gitignore");
        write(ignoreFile, "file1");
        getCache().refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(folderA)));
        status = getCache().getStatus(fileA1);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_EXCLUDED));
        status = getCache().getStatus(fileA2);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE));
    }

    public void testIgnoredBySharability () throws Exception {
        skeletonIgnoredBySharability();
    }

    public void testIgnoredBySharabilityAWT () throws Throwable {
        final Throwable[] th = new Throwable[1];
        Future<Project[]> projectOpenTask = OpenProjects.getDefault().openProjects();
        if (!projectOpenTask.isDone()) {
            try {
                projectOpenTask.get();
            } catch (Exception ex) {
                // not interested
            }
        }
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    skeletonIgnoredBySharability();
                } catch (Throwable t) {
                    th[0] = t;
                }
            }
        });
        if (th[0] != null) {
            throw th[0];
        }
    }

    private void skeletonIgnoredBySharability () throws Exception {
        File folder = new File(repositoryLocation, "folderA");
        File file1 = new File(folder, "notSharable");
        File file2 = new File(folder, "file2");
        folder.mkdirs();
        file1.createNewFile();
        file2.createNewFile();

        LogHandler handler = new LogHandler(getWorkDir());
        Git.STATUS_LOG.addHandler(handler);
        handler.setFilesToRefresh(Collections.singleton(file1));
        FileInformation status = getCache().getStatus(file1);
        if (EventQueue.isDispatchThread()) {
            assertTrue(status.containsStatus(Status.STATUS_VERSIONED_UPTODATE) || status.containsStatus(Status.STATUS_NOTVERSIONED_EXCLUDED));
        } else {
            assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_EXCLUDED));
        }
        assertTrue(handler.waitForFilesToRefresh());
        status = getCache().getStatus(file1);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_EXCLUDED));

        File newFolder = new File(repositoryLocation, "notSharable");
        folder.renameTo(newFolder);
        file1 = new File(newFolder, file1.getName());
        file2 = new File(newFolder, file2.getName());
        handler.setFilesToRefresh(Collections.singleton(file2));
        status = getCache().getStatus(file2);
        if (EventQueue.isDispatchThread()) {
            assertTrue(status.containsStatus(Status.STATUS_VERSIONED_UPTODATE) || status.containsStatus(Status.STATUS_NOTVERSIONED_EXCLUDED));
        } else {
            assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_EXCLUDED));
        }
        assertTrue(handler.waitForFilesToRefresh());
        status = getCache().getStatus(file2);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_EXCLUDED));

        folder = new File(repositoryLocation, "notSharableFolder");
        folder.mkdirs();
        file1 = new File(folder, "file1");
        file1.createNewFile();
        handler.setFilesToRefresh(Collections.singleton(folder));
        status = getCache().getStatus(folder);
        if (EventQueue.isDispatchThread()) {
            assertTrue(status.containsStatus(Status.STATUS_VERSIONED_UPTODATE) || status.containsStatus(Status.STATUS_NOTVERSIONED_EXCLUDED));
        } else {
            assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_EXCLUDED));
        }
        assertTrue(handler.waitForFilesToRefresh());
        status = getCache().getStatus(folder);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_EXCLUDED));
        status = getCache().getStatus(file1);
        assertTrue(status.containsStatus(Status.STATUS_NOTVERSIONED_EXCLUDED));
    }

    public void testPurgeRemovedIgnoredFiles () throws Exception {
        File folder = new File(repositoryLocation, "folder");
        final File file1 = new File(folder, "ignored");
        folder.mkdirs();
        file1.createNewFile();

        File ignoreFile = new File(repositoryLocation, ".gitignore");
        write(ignoreFile, "ignored");
        getCache().refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(file1)));
        assertTrue(getCache().getStatus(file1).containsStatus(Status.STATUS_NOTVERSIONED_EXCLUDED));
        file1.delete();
        assertFalse(file1.exists());
        final boolean[] cleaned = new boolean[1];
        Git.STATUS_LOG.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getMessage().contains("refreshAllRoots() uninteresting file: {0}") && file1.equals(record.getParameters()[0])) {
                    cleaned[0] = true;
                }
            }
            @Override
            public void flush() {
            }
            @Override
            public void close() throws SecurityException {
            }
        });
        getCache().refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(folder)));
        assertTrue(cleaned[0]);
    }

    public void testSkipIgnores () throws Exception {
        File folder = new File(repositoryLocation, "folder");
        File file = new File(repositoryLocation, "file");
        file.createNewFile();
        File file1 = new File(folder, "file1");
        File file2 = new File(folder, "file2");
        folder.mkdirs();
        file1.createNewFile();
        file2.createNewFile();

        File ignoreFile = new File(repositoryLocation, ".gitignore");
        write(ignoreFile, "folder");

        LogHandler handler = new LogHandler(repositoryLocation);
        Git.STATUS_LOG.addHandler(handler);
        handler.setFilesToRefresh(Collections.singleton(repositoryLocation));
        getCache().refreshAllRoots(Collections.singleton(repositoryLocation));
        handler.waitForFilesToRefresh();
        assertEquals(new HashSet(Arrays.asList(file.getAbsolutePath(), folder.getAbsolutePath(), ignoreFile.getAbsolutePath())), handler.interestingFiles);

        handler.setFilesToRefresh(Collections.singleton(repositoryLocation));
        getCache().refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(repositoryLocation)));
        handler.waitForFilesToRefresh();
        assertEquals(new HashSet(Arrays.asList(file.getAbsolutePath(), folder.getAbsolutePath(), ignoreFile.getAbsolutePath())), handler.interestingFiles);
    }

    public void testToggleIgnoreFolder () throws Exception {
        File file1 = new File(repositoryLocation, "file1");
        file1.createNewFile();
        File folder = new File(repositoryLocation, "folder");
        folder.mkdirs();
        File file2 = new File(folder, "file2");
        file2.createNewFile();
        File subFolder = new File(folder, "subfolder");
        subFolder.mkdirs();
        File file3 = new File(subFolder, "file3");
        file3.createNewFile();

        getCache().refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(repositoryLocation)));
        List<File> newFiles = Arrays.asList(getCache().listFiles(Collections.singleton(folder), EnumSet.of(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE)));
        List<File> ignoredFiles = Arrays.asList(getCache().listFiles(Collections.singleton(folder), EnumSet.of(Status.STATUS_NOTVERSIONED_EXCLUDED)));
        assertTrue(newFiles.contains(file2));
        assertTrue(newFiles.contains(file3));
        assertTrue(ignoredFiles.isEmpty());

        File ignoreFile = new File(repositoryLocation, ".gitignore");
        write(ignoreFile, "subfolder");
        getCache().getStatus(file3);
        getCache().refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(repositoryLocation)));
        getCache().getStatus(file3);
        Thread.sleep(500);
        newFiles = Arrays.asList(getCache().listFiles(Collections.singleton(folder), EnumSet.of(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE)));
        ignoredFiles = Arrays.asList(getCache().listFiles(Collections.singleton(folder), EnumSet.of(Status.STATUS_NOTVERSIONED_EXCLUDED)));
        assertTrue(newFiles.contains(file2));
        assertFalse(newFiles.contains(file3));
        assertTrue(ignoredFiles.contains(subFolder));
        assertTrue(ignoredFiles.contains(file3));

        write(ignoreFile, "subfolder2");
        getCache().refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(repositoryLocation)));
        newFiles = Arrays.asList(getCache().listFiles(Collections.singleton(folder), EnumSet.of(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE)));
        ignoredFiles = Arrays.asList(getCache().listFiles(Collections.singleton(folder), EnumSet.of(Status.STATUS_NOTVERSIONED_EXCLUDED)));
        assertTrue(newFiles.contains(file2));
        assertTrue(newFiles.contains(file3));
        assertTrue(ignoredFiles.isEmpty());

        write(ignoreFile, "folder");
        getCache().getStatus(file2);
        getCache().getStatus(file3);
        getCache().refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(repositoryLocation)));
        getCache().getStatus(file2);
        getCache().getStatus(file3);
        Thread.sleep(500);
        newFiles = Arrays.asList(getCache().listFiles(Collections.singleton(folder), EnumSet.of(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE)));
        ignoredFiles = Arrays.asList(getCache().listFiles(Collections.singleton(folder), EnumSet.of(Status.STATUS_NOTVERSIONED_EXCLUDED)));
        assertTrue(newFiles.isEmpty());
        assertTrue(ignoredFiles.contains(folder));
        assertTrue(ignoredFiles.contains(subFolder));
        assertTrue(ignoredFiles.contains(file2));
        assertTrue(ignoredFiles.contains(file3));

        write(ignoreFile, "subfolder");
        getCache().refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(repositoryLocation)));
        newFiles = Arrays.asList(getCache().listFiles(Collections.singleton(folder), EnumSet.of(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE)));
        ignoredFiles = Arrays.asList(getCache().listFiles(Collections.singleton(folder), EnumSet.of(Status.STATUS_NOTVERSIONED_EXCLUDED)));
        assertTrue(newFiles.contains(file2));
        assertFalse(newFiles.contains(file3));
        assertFalse(ignoredFiles.contains(folder));
        assertTrue(ignoredFiles.contains(subFolder));
        assertTrue(ignoredFiles.contains(file3));

        write(ignoreFile, "");
        getCache().refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(repositoryLocation)));
        newFiles = Arrays.asList(getCache().listFiles(Collections.singleton(repositoryLocation), EnumSet.of(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE)));
        ignoredFiles = Arrays.asList(getCache().listFiles(Collections.singleton(repositoryLocation), EnumSet.of(Status.STATUS_NOTVERSIONED_EXCLUDED)));
        assertTrue(newFiles.contains(file2));
        assertTrue(newFiles.contains(file3));
        assertTrue(ignoredFiles.isEmpty());
    }

    // TODO add more tests when add is implemented
    // TODO add more tests when remove is implemented
    // TODO add more tests when commit is implemented
    // TODO add more tests when exclusions are supported
    // TODO test statuses between HEAD-WC: when commit is implemented
    // TODO test conflicts

    private void assertSameStatus(Set<File> files, Status status) {
        for (File f : files) {
            assertTrue(getCache().getStatus(f).getStatus().equals(EnumSet.of(status)));
        }
    }

    private class LogHandler extends Handler {
        private Set<File> filesToRefresh;
        private boolean filesRefreshed;
        private final HashSet<File> refreshedFiles = new HashSet<File>();
        private final File topFolder;
        private final Set<String> interestingFiles = new HashSet<String>();

        private LogHandler (File topFolder) {
            this.topFolder = topFolder;
        }

        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().contains("refreshAllRoots() roots: finished")) {
                synchronized (this) {
                    if (refreshedFiles.equals(filesToRefresh)) {
                        filesRefreshed = true;
                        notifyAll();
                    }
                }
            } else if (record.getMessage().contains("refreshAllRoots() roots: ")) {
                synchronized (this) {
                    for (File f : (Set<File>) record.getParameters()[0]) {
                        if (f.getAbsolutePath().startsWith(topFolder.getAbsolutePath()))
                        refreshedFiles.add(f);
                    }
                    notifyAll();
                }
            } else if (record.getMessage().equals("refreshAllRoots() file status: {0} {1}")) {
                interestingFiles.add((String) record.getParameters()[0]);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        private void setFilesToRefresh (Set<File> files) {
            filesToRefresh = files;
            refreshedFiles.clear();
            filesRefreshed = false;
        }

        private boolean waitForFilesToRefresh () throws InterruptedException {
            for (int i = 0; i < 20; ++i) {
                synchronized (this) {
                    if (filesRefreshed) {
                        return true;
                    }
                    wait(500);
                }
            }
            return false;
        }

    }

    @ServiceProvider(service=SharabilityQueryImplementation.class)
    public static class DummySharabilityQuery implements SharabilityQueryImplementation {

        @Override
        public int getSharability (File file) {
            if (file.getAbsolutePath().contains("notSharable")) {
                return SharabilityQuery.NOT_SHARABLE;
            }
            return SharabilityQuery.UNKNOWN;
        }

    }
}
