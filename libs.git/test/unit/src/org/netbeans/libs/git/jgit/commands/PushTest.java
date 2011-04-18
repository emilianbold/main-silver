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
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitRefUpdateResult;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitTransportUpdate;
import org.netbeans.libs.git.GitTransportUpdate.Type;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class PushTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;
    private File otherWT;
    private File f;
    private GitRevisionInfo masterInfo;
    private GitBranch branch;

    public PushTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }

    public void testRemoteUpdateStatus () {
        for (RemoteRefUpdate.Status status : RemoteRefUpdate.Status.values()) {
            assertNotNull(GitRefUpdateResult.valueOf(status.name()));
        }
    }
    
    public void testPushNewBranch () throws Exception {
        String remoteUri = getRemoteRepository().getWorkTree().toURI().toString();
        assertEquals(0, getClient(workDir).listRemoteBranches(remoteUri, ProgressMonitor.NULL_PROGRESS_MONITOR).size());
        File f = new File(workDir, "f");
        add(f);
        String id = getClient(workDir).commit(new File[] { f }, "bbb", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR).getRevision();
        Map<String, GitTransportUpdate> updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "master", "master", id, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);

        // adding another branch
        write(f, "huhu");
        add(f);
        String newid = getClient(workDir).commit(new File[] { f }, "bbb", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR).getRevision();
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/anotherBranch" }), Collections.<String>emptyList(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertEquals(newid, remoteBranches.get("anotherBranch").getId());
        assertUpdate(updates.get("anotherBranch"), "master", "anotherBranch", newid, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);
    }
    
    public void testPushDeleteBranch () throws Exception {
        String remoteUri = getRemoteRepository().getWorkTree().toURI().toString();
        assertEquals(0, getClient(workDir).listRemoteBranches(remoteUri, ProgressMonitor.NULL_PROGRESS_MONITOR).size());
        File f = new File(workDir, "f");
        add(f);
        String id = getClient(workDir).commit(new File[] { f }, "bbb", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR).getRevision();
        Map<String, GitTransportUpdate> updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master", "refs/heads/master:refs/heads/newbranch" }), Collections.<String>emptyList(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertEquals(2, updates.size());
        assertUpdate(updates.get("master"), "master", "master", id, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);
        assertUpdate(updates.get("newbranch"), "master", "newbranch", id, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);

        // deleting branch
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { ":refs/heads/newbranch" }), Collections.<String>emptyList(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertUpdate(updates.get("newbranch"), null, "newbranch", null, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);
    }
    
    public void testPushChange () throws Exception {
        String remoteUri = getRemoteRepository().getWorkTree().toURI().toString();
        assertEquals(0, getClient(workDir).listRemoteBranches(remoteUri, ProgressMonitor.NULL_PROGRESS_MONITOR).size());
        File f = new File(workDir, "f");
        add(f);
        String id = getClient(workDir).commit(new File[] { f }, "bbb", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR).getRevision();
        Map<String, GitTransportUpdate> updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "master", "master", id, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);

        // modification
        write(f, "huhu");
        add(f);
        String newid = getClient(workDir).commit(new File[] { f }, "bbb", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR).getRevision();
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(newid, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "master", "master", newid, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);
    }
    
    public void testPushUpdateInRemotes () throws Exception {
        String remoteUri = getRemoteRepository().getWorkTree().toURI().toString();
        assertEquals(0, getClient(workDir).listRemoteBranches(remoteUri, ProgressMonitor.NULL_PROGRESS_MONITOR).size());
        File f = new File(workDir, "f");
        add(f);
        String id = getClient(workDir).commit(new File[] { f }, "bbb", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR).getRevision();
        Map<String, GitTransportUpdate> updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "master", "master", id, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);
        
        getClient(workDir).pull(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/remotes/origin/master" }), "master", ProgressMonitor.NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> branches = getClient(workDir).getBranches(true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertEquals(id, branches.get("origin/master").getId());

        // modification
        write(f, "huhu");
        add(f);
        String newid = getClient(workDir).commit(new File[] { f }, "bbb", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR).getRevision();
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, ProgressMonitor.NULL_PROGRESS_MONITOR);
        branches = getClient(workDir).getBranches(true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        // not yet updated, tracking branches has not been set
        assertEquals(id, branches.get("origin/master").getId());
        
        // another modification
        write(f, "huhu2");
        add(f);
        newid = getClient(workDir).commit(new File[] { f }, "bbb", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR).getRevision();
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Arrays.asList(new String[] { "refs/heads/master:refs/remotes/origin/master" }), ProgressMonitor.NULL_PROGRESS_MONITOR);
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, ProgressMonitor.NULL_PROGRESS_MONITOR);
        branches = getClient(workDir).getBranches(true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        // not yet updated, trcking branch has not been set
        assertEquals(newid, branches.get("origin/master").getId());
        
        //let's set tracking branch
        StoredConfig cfg = repository.getConfig();
        cfg.setString(ConfigConstants.CONFIG_REMOTE_SECTION, "origin", ConfigConstants.CONFIG_KEY_URL, new URIish(remoteUri).toString());
        cfg.setString(ConfigConstants.CONFIG_REMOTE_SECTION, "origin", "fetch", "+refs/heads/master:refs/remotes/origin/master");
        cfg.setString(ConfigConstants.CONFIG_BRANCH_SECTION, "master", ConfigConstants.CONFIG_KEY_REMOTE, "origin");
        cfg.setString(ConfigConstants.CONFIG_BRANCH_SECTION, "master", ConfigConstants.CONFIG_KEY_MERGE, "refs/heads/master");
        cfg.save();
        
        // what about now???
        updates = getClient(workDir).push("origin", Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertUpdate(updates.get("master"), "master", "master", newid, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.UP_TO_DATE);
        branches = getClient(workDir).getBranches(true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertEquals(newid, branches.get("origin/master").getId());
    }
    
    public void testPushRejectNonFastForward () throws Exception {
        String remoteUri = getRemoteRepository().getWorkTree().toURI().toString();
        assertEquals(0, getClient(workDir).listRemoteBranches(remoteUri, ProgressMonitor.NULL_PROGRESS_MONITOR).size());
        File f = new File(workDir, "f");
        add(f);
        String id = getClient(workDir).commit(new File[] { f }, "bbb", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR).getRevision();
        Map<String, GitTransportUpdate> updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "master", "master", id, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);

        // modification
        write(f, "huhu");
        add(f);
        String newid = getClient(workDir).commit(new File[] { f }, "bbb", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR).getRevision();
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(newid, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "master", "master", newid, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);
        
        getClient(workDir).createBranch("localbranch", id, ProgressMonitor.NULL_PROGRESS_MONITOR);
        getClient(workDir).checkoutRevision("localbranch", true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        write(f, "huhu2");
        add(f);
        id = getClient(workDir).commit(new File[] { f }, "some change before merge", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR).getRevision();
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/localbranch:refs/heads/master" }), Collections.<String>emptyList(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(newid, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "localbranch", "master", id, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.REJECTED_NONFASTFORWARD);
    }

    private void assertUpdate(GitTransportUpdate update, String localName, String remoteName, String newObjectId, String oldObjectId, String remoteUri, Type type, GitRefUpdateResult result) {
        assertEquals(localName, update.getLocalName());
        assertEquals(remoteName, update.getRemoteName());
        assertEquals(newObjectId, update.getNewObjectId());
        assertEquals(oldObjectId, update.getOldObjectId());
        assertEquals(remoteUri, update.getRemoteUri());
        assertEquals(type, update.getType());
        assertEquals(result, update.getResult());
    }
}
