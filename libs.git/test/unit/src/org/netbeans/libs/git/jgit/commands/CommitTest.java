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

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.netbeans.libs.git.GitFileInfo;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.GitStatus.Status;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class CommitTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;

    public CommitTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }

    public void testCommitNoRoots () throws Exception {
        File toCommit = new File(workDir, "testnotadd.txt");
        write(toCommit, "blablabla");
        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { toCommit }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, toCommit, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        client.add(new File[] { toCommit }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new File[] { toCommit }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, toCommit, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        GitRevisionInfo info = client.commit(new File[0], "initial commit", ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new File[] { toCommit }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, toCommit, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        Git git = new Git(repository);
        LogCommand log = git.log();
        RevCommit com = log.call().iterator().next();
        assertEquals("initial commit", info.getFullMessage());
        assertEquals("initial commit", com.getFullMessage());
        assertEquals(ObjectId.toString(com.getId()), info.getRevision());
        Map<File, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertTrue(modifiedFiles.get(toCommit).getStatus().equals(Status.STATUS_ADDED));
    }

    public void testSingleFileCommit () throws Exception {
        repository.getConfig().setString("user", null, "name", "John");
        repository.getConfig().setString("user", null, "email", "john@git.com");
        repository.getConfig().save();

        File toCommit = new File(workDir, "testnotadd.txt");
        write(toCommit, "blablabla");
        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { toCommit }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, toCommit, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        client.add(new File[] { toCommit }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new File[] { toCommit }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, toCommit, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        long t1 = System.currentTimeMillis();
        Thread.sleep(1000);
        GitRevisionInfo info = client.commit(new File[] { toCommit }, "initial commit", ProgressMonitor.NULL_PROGRESS_MONITOR);
        Thread.sleep(1000);
        long t2 = System.currentTimeMillis();
        statuses = client.getStatus(new File[] { toCommit }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, toCommit, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertTrue(t1 <= info.getCommitTime() && t2 >= info.getCommitTime());

        Git git = new Git(repository);
        LogCommand log = git.log();
        RevCommit com = log.call().iterator().next();
        assertEquals("initial commit", info.getFullMessage());
        assertEquals("initial commit", com.getFullMessage());
        assertEquals( "john@git.com", info.getAuthor().getEmailAddress());
        assertEquals( "john@git.com", com.getAuthorIdent().getEmailAddress());
        assertEquals(ObjectId.toString(com.getId()), info.getRevision());
        Map<File, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertTrue(modifiedFiles.get(toCommit).getStatus().equals(Status.STATUS_ADDED));
    }

    public void testMultipleFileCommit () throws Exception {
        repository.getConfig().setString("user", null, "name", "John");
        repository.getConfig().setString("user", null, "email", "john@git.com");
        repository.getConfig().save();

        File dir = new File(workDir, "testdir");
        File newOne = new File(dir, "test.txt");
        File another = new File(dir, "test2.txt");
        dir.mkdirs();
        write(newOne, "this is test!");
        write(another, "this is another test!");

        GitClient client = getClient(workDir);
        client.add(new File[] { newOne, another }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { newOne, another }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, newOne, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, another, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        GitRevisionInfo info = client.commit(new File[] { newOne, another }, "initial commit", ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new File[] { newOne, another }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, newOne, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, another, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        Map<File, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertTrue(modifiedFiles.get(newOne).getStatus().equals(Status.STATUS_ADDED));
        assertTrue(modifiedFiles.get(another).getStatus().equals(Status.STATUS_ADDED));

        Git git = new Git(repository);
        LogCommand log = git.log();
        RevCommit com = log.call().iterator().next();
        assertEquals("initial commit", com.getFullMessage());
        assertEquals( "john@git.com", com.getAuthorIdent().getEmailAddress());

        write(newOne, "!modification!");
        write(another, "another modification!");

        client.add(new File[] { workDir }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new File[] { workDir }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, newOne, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, another, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        info = client.commit(new File[] { newOne, another }, "second commit", ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new File[] { workDir }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, newOne, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, another, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        modifiedFiles = info.getModifiedFiles();
        assertTrue(modifiedFiles.get(newOne).getStatus().equals(Status.STATUS_MODIFIED));
        assertTrue(modifiedFiles.get(another).getStatus().equals(Status.STATUS_MODIFIED));

        log = git.log();
        com = log.call().iterator().next();
        assertEquals("second commit", com.getFullMessage());
        assertEquals( "john@git.com", com.getAuthorIdent().getEmailAddress());
    }

    public void testCommitOnlySomeOfAllFiles () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "file1 content");
        File file2 = new File(workDir, "file2");
        write(file2, "file2 content");
        File[] files = new File[] { file1, file2 };
        GitClient client = getClient(workDir);
        client.add(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        GitRevisionInfo info = client.commit(new File[] { file1 }, "initial commit", ProgressMonitor.NULL_PROGRESS_MONITOR);
        Map<File, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file1).getStatus().equals(Status.STATUS_ADDED));
        Map<File, GitStatus> statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        // file1 should be up to date
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        // but file2 should still be staged for commit
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        info = client.commit(new File[] { file2 }, "initial commit", ProgressMonitor.NULL_PROGRESS_MONITOR);
        modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file2).getStatus().equals(Status.STATUS_ADDED));

        write(file1, "file1 content changed");
        write(file2, "file2 content changed");
        client.add(new File[] { file1 }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        info = client.commit(new File[] { file1 }, "change in content", ProgressMonitor.NULL_PROGRESS_MONITOR);
        modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file1).getStatus().equals(Status.STATUS_MODIFIED));
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        // file1 should be up to date
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        // but file2 was not staged
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);

        write(file1, "file1 content changed again");
        write(file2, "file2 content changed again");
        client.add(new File[] { file1, file2 }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        write(file2, "file2 content changed again and again");
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        info = client.commit(new File[] { file1 }, "another change in content", ProgressMonitor.NULL_PROGRESS_MONITOR);
        modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file1).getStatus().equals(Status.STATUS_MODIFIED));
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        // file1 should be up to date
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        // but file2 should still be staged for commit
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);

        write(file1, "file1 content changed again and again");
        client.add(new File[] { file1 }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        client.remove(new File[] { file2 }, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_MODIFIED, false);
        info = client.commit(new File[] { file1 }, "another change in content", ProgressMonitor.NULL_PROGRESS_MONITOR);
        modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file1).getStatus().equals(Status.STATUS_MODIFIED));
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        // file1 should be up to date
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        // but file2 should still be staged for commit
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_MODIFIED, false);
    }

    public void testCommitRemoval () throws Exception {
        File file = new File(workDir, "file");
        write(file, "file1 content");
        File[] files = new File[] { file };
        GitClient client = getClient(workDir);
        client.add(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        GitRevisionInfo info = client.commit(files, "initial commit", ProgressMonitor.NULL_PROGRESS_MONITOR);
        Map<File, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file).getStatus().equals(Status.STATUS_ADDED));

        Map<File, GitStatus> statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        file.delete();
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_REMOVED, false);

        // commit should remove file from the repository
        client.remove(files, false, ProgressMonitor.NULL_PROGRESS_MONITOR);
        info = client.commit(files, "deleting file", ProgressMonitor.NULL_PROGRESS_MONITOR);
        modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file).getStatus().equals(Status.STATUS_REMOVED));
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertNull(statuses.get(file));
    }

    public void testSingleTreeCommit () throws Exception {
        File folder = new File(workDir, "folder");
        File subfolder1 = new File(folder, "subfolder");
        File subfolder11 = new File(subfolder1, "subfolder1");
        File subfolder12 = new File(subfolder1, "subfolder2");
        subfolder11.mkdirs();
        subfolder12.mkdirs();
        File file1 = new File(subfolder11, "file1");
        File file2 = new File(subfolder12, "file2");
        write(file1, "file1 content");
        write(file2, "file2 content");
        File[] files = new File[] { folder };
        GitClient client = getClient(workDir);
        client.add(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        GitRevisionInfo info = client.commit(files, "initial commit", ProgressMonitor.NULL_PROGRESS_MONITOR);
        Map<File, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertEquals(2, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file1).getStatus().equals(Status.STATUS_ADDED));
        assertTrue(modifiedFiles.get(file2).getStatus().equals(Status.STATUS_ADDED));

        Map<File, GitStatus> statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        Git git = new Git(repository);
        LogCommand log = git.log();
        RevCommit com = log.call().iterator().next();
        assertEquals("initial commit", com.getFullMessage());
    }

    public void testMultipleTreesCommit () throws Exception {
        File folder1 = new File(workDir, "folder1");
        File subfolder11 = new File(folder1, "subfolder1");
        File subfolder12 = new File(folder1, "subfolder2");
        subfolder11.mkdirs();
        subfolder12.mkdirs();
        File file11 = new File(subfolder11, "file1");
        File file12 = new File(subfolder12, "file2");
        write(file11, "file1 content");
        write(file12, "file2 content");
        File folder2 = new File(workDir, "folder2");
        File subfolder21 = new File(folder2, "subfolder1");
        File subfolder22 = new File(folder2, "subfolder2");
        subfolder21.mkdirs();
        subfolder22.mkdirs();
        File file21 = new File(subfolder21, "file1");
        File file22 = new File(subfolder22, "file2");
        write(file21, "file1 content");
        write(file22, "file2 content");
        File[] files = new File[] { folder1, folder2 };
        GitClient client = getClient(workDir);
        client.add(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        GitRevisionInfo info = client.commit(files, "initial commit", ProgressMonitor.NULL_PROGRESS_MONITOR);
        Map<File, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertEquals(4, modifiedFiles.size());

        Map<File, GitStatus> statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file12, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file21, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file22, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        Git git = new Git(repository);
        LogCommand log = git.log();
        RevCommit com = log.call().iterator().next();
        assertEquals("initial commit", com.getFullMessage());

        write(file11, "!modification!");
        write(file12, "another modification!");
        write(file21, "!modification!");
        write(file22, "another modification!");

        client.add(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file12, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file21, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file22, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        client.commit(files, "second commit", ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file12, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file21, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file22, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        log = git.log();
        com = log.call().iterator().next();
        assertEquals("second commit", com.getFullMessage());
    }

    public void testCommitOnlySomeOfAllFilesFromMultipleTrees () throws Exception {
        File folder1 = new File(workDir, "folder1");
        File subfolder11 = new File(folder1, "subfolder1");
        File subfolder12 = new File(folder1, "subfolder2");
        subfolder11.mkdirs();
        subfolder12.mkdirs();
        File file11 = new File(subfolder11, "file1");
        File file12 = new File(subfolder12, "file2");
        write(file11, "file1 content");
        write(file12, "file2 content");
        File folder2 = new File(workDir, "folder2");
        File subfolder21 = new File(folder2, "subfolder1");
        File subfolder22 = new File(folder2, "subfolder2");
        subfolder21.mkdirs();
        subfolder22.mkdirs();
        File file21 = new File(subfolder21, "file1");
        File file22 = new File(subfolder22, "file2");
        write(file21, "file1 content");
        write(file22, "file2 content");
        File[] trees = new File[] { folder1, folder2 };
        File[] filesToCommit = new File[] { folder1, subfolder21 };
        File[] filesSingleFolder = new File[] { subfolder21 };
        GitClient client = getClient(workDir);
        client.add(trees, ProgressMonitor.NULL_PROGRESS_MONITOR);

        // COMMIT SOME
        GitRevisionInfo info = client.commit(filesSingleFolder, "initial commit", ProgressMonitor.NULL_PROGRESS_MONITOR);
        Map<File, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        Map<File, GitStatus> statuses = client.getStatus(trees, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, file12, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, file21, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file22, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        Git git = new Git(repository);
        LogCommand log = git.log();
        RevCommit com = log.call().iterator().next();
        assertEquals("initial commit", com.getFullMessage());

        // COMMIT ALL
        info = client.commit(trees, "commit all", ProgressMonitor.NULL_PROGRESS_MONITOR);
        modifiedFiles = info.getModifiedFiles();
        assertEquals(3, modifiedFiles.size());
        statuses = client.getStatus(trees, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file12, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file21, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file22, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        write(file11, "!modification!");
        write(file12, "another modification!");
        write(file21, "!modification!");
        write(file22, "another modification!");

        client.add(trees, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(trees, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file12, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file21, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file22, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        info = client.commit(filesToCommit, "second commit", ProgressMonitor.NULL_PROGRESS_MONITOR);
        modifiedFiles = info.getModifiedFiles();
        assertEquals(3, modifiedFiles.size());
        statuses = client.getStatus(trees, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file12, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file21, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file22, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);

        log = git.log();
        com = log.call().iterator().next();
        assertEquals("second commit", com.getFullMessage());

        // COMMIT ALL
        client.commit(trees, "commit all", ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(trees, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file12, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file21, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file22, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
    }

    public void testCommitRemovalTree () throws Exception {
        File folder = new File(workDir, "folder");
        File subfolder1 = new File(folder, "subfolder");
        File subfolder11 = new File(subfolder1, "subfolder1");
        File subfolder12 = new File(subfolder1, "subfolder2");
        subfolder11.mkdirs();
        subfolder12.mkdirs();
        File file1 = new File(subfolder11, "file1");
        File file2 = new File(subfolder12, "file2");
        write(file1, "file1 content");
        write(file2, "file2 content");
        File[] files = new File[] { folder };
        GitClient client = getClient(workDir);
        client.add(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        GitRevisionInfo info = client.commit(files, "initial commit", ProgressMonitor.NULL_PROGRESS_MONITOR);
        Map<File, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertEquals(2, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file1).getStatus().equals(Status.STATUS_ADDED));
        assertTrue(modifiedFiles.get(file2).getStatus().equals(Status.STATUS_ADDED));

        Map<File, GitStatus> statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        client.remove(files, false, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);

        // commit should remove file from the repository
        info = client.commit(files, "deleting files", ProgressMonitor.NULL_PROGRESS_MONITOR);
        modifiedFiles = info.getModifiedFiles();
        assertEquals(2, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file1).getStatus().equals(Status.STATUS_REMOVED));
        assertTrue(modifiedFiles.get(file2).getStatus().equals(Status.STATUS_REMOVED));
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertNull(statuses.get(file1));
        assertNull(statuses.get(file2));
    }
}
