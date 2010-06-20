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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.mercurial;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.ui.ignore.IgnoreAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author tomas
 */
public class IgnoresTest extends AbstractHgTest {

    public IgnoresTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();        
        
        // create
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        System.setProperty("netbeans.user", getWorkDir().getParentFile().getAbsolutePath());
    }

    // ignore patterns - issue 171378 - should pass
    public void testIgnores () throws IOException {
        File workDir = getWorkDir();
        File ignoreFile = new File(getDataDir().getAbsolutePath() + "/ignore/hgignore");
        File toFile = new File(workDir, ".hgignore");
        ignoreFile.renameTo(toFile);

        File ignoredFolder = new File(workDir, "ignoredFolderLevel1");
        ignoredFolder.mkdirs();
        FileInformation info = getCache().getCachedStatus(ignoredFolder);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        File ignoredFile = new File(ignoredFolder, "file");
        ignoredFile.createNewFile();
        info = getCache().getCachedStatus(ignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        File unignoredFile = new File(workDir, "file");
        unignoredFile.createNewFile();
        info = getCache().getCachedStatus(unignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0);


        File unignoredFolder = new File(workDir, "unignoredFolderLevel1");
        unignoredFolder.mkdirs();
        info = getCache().getCachedStatus(unignoredFolder);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0);

        ignoredFolder = new File(unignoredFolder, "ignoredFolderLevel2");
        ignoredFolder.mkdirs();
        info = getCache().getCachedStatus(ignoredFolder);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        ignoredFolder = new File(unignoredFolder, "ignoredFolderLevel2_2");
        ignoredFolder.mkdirs();
        info = getCache().getCachedStatus(ignoredFolder);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        ignoredFile = new File(ignoredFolder, "ignoredFile");
        ignoredFile.createNewFile();
        info = getCache().getCachedStatus(ignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        ignoredFile = new File(workDir, "file.ignore");
        ignoredFile.createNewFile();
        info = getCache().getCachedStatus(ignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        ignoredFile = new File(unignoredFolder, "file.ignore");
        ignoredFile.createNewFile();
        info = getCache().getCachedStatus(ignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);


        unignoredFolder = new File(unignoredFolder, "project");
        unignoredFolder.mkdirs();
        info = getCache().getCachedStatus(unignoredFolder);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0);

        unignoredFile = new File(unignoredFolder, "project");
        unignoredFile.createNewFile();
        info = getCache().getCachedStatus(unignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0);

        ignoredFile = new File(workDir, ".project");
        ignoredFile.createNewFile();
        info = getCache().getCachedStatus(ignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        ignoredFile = new File(unignoredFolder, ".project");
        ignoredFile.createNewFile();
        info = getCache().getCachedStatus(ignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        unignoredFile = new File(workDir, "file.ignore2");
        unignoredFile.createNewFile();
        info = getCache().getCachedStatus(unignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0);
    }
    
    public void testIgnoreAction () throws Exception {
        File workDir = getWorkDir();
        new File(workDir, ".hgignore").delete();
        File folderA = new File(workDir, "folderA");
        File folderAA = new File(folderA, "folderAA");
        File folderAB = new File(folderA, "folderAB");
        File fileAAA = new File(folderAA, "fileAAA");
        File fileAAB = new File(folderAA, "fileAAB");
        File fileABA = new File(folderAB, "fileABA");
        File fileABB = new File(folderAB, "fileABB");
        folderAA.mkdirs();
        folderAB.mkdirs();
        fileAAA.createNewFile();
        fileAAB.createNewFile();
        fileABA.createNewFile();
        fileABB.createNewFile();

        getCache().refreshAllRoots(Collections.singleton(workDir));

        Set<File> ignoredFiles = new HashSet<File>();
        File[] parentFiles = new File[] { workDir };
        // ignoredFiles is empty
        assertIgnoreStatus(parentFiles, ignoredFiles);
        // ignoring folderAA and all its descendants
        toggleIgnore(folderAA, ignoredFiles);
        ignoredFiles.addAll(getFiles(folderAA));
        assertIgnoreStatus(parentFiles, ignoredFiles);
        // ignoring folderA and all its descendants
        toggleIgnore(folderA, ignoredFiles);
        ignoredFiles.addAll(getFiles(folderA));
        assertIgnoreStatus(parentFiles, ignoredFiles);
        // unignoring folderAA and all its descendants - but has no effect since folderA is still ignored
        toggleIgnore(folderAA, ignoredFiles);
        assertIgnoreStatus(parentFiles, ignoredFiles);
        // unignoring folderA
        toggleIgnore(folderA, ignoredFiles);
        ignoredFiles.removeAll(getFiles(folderA));
        assertIgnoreStatus(parentFiles, ignoredFiles);
        // ignoring folderAB and all its descendants
        toggleIgnore(folderAB, ignoredFiles);
        ignoredFiles.addAll(getFiles(folderAB));
        assertIgnoreStatus(parentFiles, ignoredFiles);
        // ignoring folderA and all its descendants
        toggleIgnore(folderA, ignoredFiles);
        ignoredFiles.addAll(getFiles(folderA));
        assertIgnoreStatus(parentFiles, ignoredFiles);
        // unignoring folderA and all its descendants - folder AB remains ignored
        toggleIgnore(folderA, ignoredFiles);
        ignoredFiles.removeAll(getFiles(folderA));
        ignoredFiles.addAll(getFiles(folderAB));
        assertIgnoreStatus(parentFiles, ignoredFiles);
        // unignoring folderAB and all its descendants - no file is ignored
        toggleIgnore(folderAB, ignoredFiles);
        ignoredFiles.removeAll(getFiles(folderAB));
        assertIgnoreStatus(parentFiles, ignoredFiles);

        // bug #187304
        ignoredFiles.clear();
        File obscureFile = new File(folderA, "This + File + Might + Crash + Mercurial");
        obscureFile.createNewFile();
        // ignoring the file
        toggleIgnore(obscureFile, ignoredFiles);
        ignoredFiles.add(obscureFile);
        assertIgnoreStatus(parentFiles, ignoredFiles);
        // unignoring the file
        toggleIgnore(obscureFile, ignoredFiles);
        ignoredFiles.clear();
        assertIgnoreStatus(parentFiles, ignoredFiles);
    }

    private void assertIgnoreStatus (File[] parents, Set<File> ignoredFiles) {
        for (File parent : parents) {
            assertIgnoreStatus(parent, ignoredFiles);
            File[] files = parent.listFiles();
            if (files != null) {
                assertIgnoreStatus(files, ignoredFiles);
            }
        }
    }

    private void assertIgnoreStatus (File file, Set<File> ignoredFiles) {
        FileInformation info = getCache().getCachedStatus(file);
        int status = info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
        if (expectedIgnored(file, ignoredFiles)) {
            assertTrue("Supposed to be ignored: " + file.getAbsolutePath(), status != 0);
        } else {
            assertTrue("Supposed to be normal: " + file.getAbsolutePath(), status == 0);
        }
    }

    private boolean expectedIgnored (File file, Set<File> ignoredFiles) {
        File parent = file;
        while (parent != null && !ignoredFiles.contains(parent)) {
            parent = parent.getParentFile();
        }
        return ignoredFiles.contains(parent);
    }

    private void toggleIgnore (File folder, Set<File> ignoredFiles) throws InterruptedException {
        Logger logger = Logger.getLogger("org.netbeans.modules.mercurial.fileStatusCacheNewGeneration");
        logger.setLevel(Level.ALL);
        LogHandler handler = new LogHandler(folder);
        logger.addHandler(handler);
        TestIgnoreAction tia = SystemAction.get(TestIgnoreAction.class);
        tia.performContextAction(new Node[] { new AbstractNode(Children.LEAF, Lookups.singleton(folder)) });
        synchronized (handler) {
            if (!handler.flag) {
                handler.wait(20000);
            }
        }
        assert handler.flag : "Ignore action failed";
        logger.removeHandler(handler);
    }

    private Set<File> getFiles (File file) {
        Set<File> files = new HashSet<File>();
        files.add(file);
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                files.addAll(getFiles(child));
            }
        }
        return files;
    }

    public static class TestIgnoreAction extends IgnoreAction {

        @Override
        public void performContextAction(Node[] nodes) {
            super.performContextAction(nodes);
        }

    }

    private class LogHandler extends Handler {
        private final File expectedFile;
        private boolean flag;

        private LogHandler(File folderAA) {
            this.expectedFile = folderAA;
        }

        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().contains("refreshIgnores: File {0} refreshed") && record.getParameters().length > 0 && expectedFile.equals(record.getParameters()[0])) {
                synchronized (this) {
                    flag = true;
                    notify();
                }
            }
        }

        @Override
        public void flush() {
            //
        }

        @Override
        public void close() throws SecurityException {
            //
        }

    }
}
