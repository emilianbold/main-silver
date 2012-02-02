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

package org.netbeans.libs.git;

import java.io.File;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.JGitCredentialsProvider;
import org.netbeans.libs.git.jgit.JGitRepository;
import org.netbeans.libs.git.jgit.commands.AddCommand;
import org.netbeans.libs.git.jgit.commands.BlameCommand;
import org.netbeans.libs.git.jgit.commands.CatCommand;
import org.netbeans.libs.git.jgit.commands.CheckoutIndexCommand;
import org.netbeans.libs.git.jgit.commands.CheckoutRevisionCommand;
import org.netbeans.libs.git.jgit.commands.CleanCommand;
import org.netbeans.libs.git.jgit.commands.CommitCommand;
import org.netbeans.libs.git.jgit.commands.ConflictCommand;
import org.netbeans.libs.git.jgit.commands.CopyCommand;
import org.netbeans.libs.git.jgit.commands.CreateBranchCommand;
import org.netbeans.libs.git.jgit.commands.CreateTagCommand;
import org.netbeans.libs.git.jgit.commands.DeleteBranchCommand;
import org.netbeans.libs.git.jgit.commands.DeleteTagCommand;
import org.netbeans.libs.git.jgit.commands.ExportCommitCommand;
import org.netbeans.libs.git.jgit.commands.ExportDiffCommand;
import org.netbeans.libs.git.jgit.commands.FetchCommand;
import org.netbeans.libs.git.jgit.commands.GetCommonAncestorCommand;
import org.netbeans.libs.git.jgit.commands.GetPreviousCommitCommand;
import org.netbeans.libs.git.jgit.commands.GetRemotesCommand;
import org.netbeans.libs.git.jgit.commands.IgnoreCommand;
import org.netbeans.libs.git.jgit.commands.InitRepositoryCommand;
import org.netbeans.libs.git.jgit.commands.ListBranchCommand;
import org.netbeans.libs.git.jgit.commands.ListModifiedIndexEntriesCommand;
import org.netbeans.libs.git.jgit.commands.ListRemoteBranchesCommand;
import org.netbeans.libs.git.jgit.commands.ListRemoteTagsCommand;
import org.netbeans.libs.git.jgit.commands.ListTagCommand;
import org.netbeans.libs.git.jgit.commands.LogCommand;
import org.netbeans.libs.git.jgit.commands.MergeCommand;
import org.netbeans.libs.git.jgit.commands.PullCommand;
import org.netbeans.libs.git.jgit.commands.PushCommand;
import org.netbeans.libs.git.jgit.commands.RemoveCommand;
import org.netbeans.libs.git.jgit.commands.RemoveRemoteCommand;
import org.netbeans.libs.git.jgit.commands.RenameCommand;
import org.netbeans.libs.git.jgit.commands.ResetCommand;
import org.netbeans.libs.git.jgit.commands.RevertCommand;
import org.netbeans.libs.git.jgit.commands.SetRemoteCommand;
import org.netbeans.libs.git.jgit.commands.StatusCommand;
import org.netbeans.libs.git.jgit.commands.UnignoreCommand;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.NotificationListener;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.libs.git.progress.RevisionInfoListener;
import org.netbeans.libs.git.progress.StatusListener;

/**
 * This class provides access to all supported git commands, methods that 
 * allow you to get information about a git repository or affect the behavior 
 * of invoked commands.
 * <br/>
 * An instance of this class is <strong>always</strong> bound to a local git repository.
 * The repository (identified by a git repository root file) may not exist on disk however
 * because obviously when cloning or initializing a repository it may not yet physically exist.
 * 
 * <h5>Working with this class</h5>
 * A client of the API should follow these steps in order to run a certain git commands:
 * <ol>
 * <li><h6>Acquire an instance of a git client</h6>
 * <p>Instances of a git client are provided by {@link GitClientFactory}. To get one call
 * {@link GitClientFactory#getClient(java.io.File) }. The method takes as a parameter a repository
 * root folder. In case you want to initialize a not yet existing repository, you construct the client
 * with a folder that tells the client where the repository should be created.</p>
 * </li>
 * <li><h6>Configure the client</h6>
 * <p>Some git client commands may (or may not) require additional setup of the client to successfully finish their work.
 * One quite usual use case is setting an instance of {@link GitClientCallback} to the client so commands like <code>push</code>,
 * <code>fetch</code> or <code>pull</code> may connect to and access a remote repository. To set an instance of <code>GitClientCallback</code>
 * use {@link #setCallback(org.netbeans.libs.git.GitClientCallback) } method.</p>
 * </li>
 * <li><h6>Attaching listeners</h6>
 * <p>Certain git commands may take a long time to finish and they are capable of notifying the world about the progress in their work.<br/>
 * If you want to be notified about such changes while the command is in process, attach a listener to the client 
 * via {@link #addNotificationListener(org.netbeans.libs.git.progress.NotificationListener) }.<br/>
 * An example can be the log command. Digging through the history may take a lot of time so if you do not want to wait for the complete result only
 * and want to present the commit information incrementally as it is accepted one by one into the result, you can do so by adding an instance of 
 * {@link RevisionInfoListener} to the client.</p>
 * </li>
 * <li><h6>Running git commands</h6>
 * <p>When you have the client correctly set up, you may call any git command we support. The commands are mapped to appropriate methods in <code>GitClient</code>.
 * <br/>Every method representing a git command accepts as a parameter an instance of {@link ProgressMonitor}. With that class you may affect the flow of commands - it
 * has the ability to cancel running git commands - and listen for error or information messages the commands produce.</p>
 * </li>
 * @author Ondra Vrabec
 */
public final class GitClient {
    private final DelegateListener delegateListener;
    private GitClassFactory gitFactory;

    /**
     * Used as a parameter of {@link #reset(java.lang.String, org.netbeans.libs.git.GitClient.ResetType, org.netbeans.libs.git.progress.ProgressMonitor) }
     * to set the behavior of the command.
     */
    public enum ResetType {
        /**
         * The command will only set the current HEAD but will not affect the Index 
         * or the Working tree.
         */
        SOFT {
            @Override
            public String toString() {
                return "--soft"; //NOI18N
            }
        },
        /**
         * The reset command will move the current HEAD and update the Index with
         * the state in the new HEAD but will not affect files in the Working tree.
         */
        MIXED {
            @Override
            public String toString() {
                return "--mixed"; //NOI18N
            }
        },
        /**
         * The reset command will move the current HEAD and update both the Index 
         * and the Working tree with the state in the new HEAD.
         */
        HARD {
            @Override
            public String toString() {
                return "--hard"; //NOI18N
            }
        }
    }

    /**
     * Used as a parameter in commands comparing two trees in the repository.
     * Currently used as a parameter of e.g. 
     * {@link #exportDiff(java.io.File[], org.netbeans.libs.git.GitClient.DiffMode, java.io.OutputStream, org.netbeans.libs.git.progress.ProgressMonitor) }.
     * It tells the command what trees it is supposed to compare.
     */
    public enum DiffMode {
        /**
         * Compares the current HEAD vs. the Index
         */
        HEAD_VS_INDEX,
        /**
         * Compares the current HEAD vs. the Working tree
         */
        HEAD_VS_WORKINGTREE,
        /**
         * Compares the Index vs. the Working tree
         */
        INDEX_VS_WORKINGTREE
    }
    
    private final JGitRepository gitRepository;
    private final Set<NotificationListener> listeners;
    private JGitCredentialsProvider credentialsProvider;

    GitClient (JGitRepository gitRepository) {
        this.gitRepository = gitRepository;
        listeners = new HashSet<NotificationListener>();
        delegateListener = new DelegateListener();
    }

    /**
     * Adds all files under the given roots to the index
     * @param roots files or folders to add recursively to the index
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public void add (File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        AddCommand cmd = new AddCommand(repository, getClassFactory(), roots, monitor, delegateListener);
        cmd.execute();
    }

    /**
     * Adds a listener of any kind to the client. Git commands that support a listener will notify
     * the appropriate ones while working.
     * @param listener a listener to add
     */
    public void addNotificationListener (NotificationListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Annotates lines of a given file in a given revision and returns the result
     * with annotate information.
     * @param file file to be annotated
     * @param revision a revision the file should be annotated in or <code>null</code> for blaming a checked-out file against HEAD
     * @param monitor progress monitor
     * @return annotation information
     * @throws org.netbeans.libs.git.GitException.MissingObjectException when the revision <code>revision</code> cannot be resolved.
     * @throws GitException an unexpected error occurs
     */
    public GitBlameResult blame (File file, String revision, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        BlameCommand cmd = new BlameCommand(repository, getClassFactory(), file, revision, monitor);
        cmd.execute();
        return cmd.getResult();
    }

    /**
     * Prints file's content in the given revision to output stream
     * @param file file to cat
     * @param revision git revision, never <code>null</code>
     * @param out output stream to print the content to.
     * @return <code>true</code> if the file was found in the specified revision and printed to out, otherwise <code>false</code>
     * @throws GitException.MissingObjectException if the given revision does not exist
     * @throws GitException an unexpected error occurs
     */
    public boolean catFile (File file, String revision, java.io.OutputStream out, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        CatCommand cmd = new CatCommand(repository, getClassFactory(), file, revision, out, monitor);
        cmd.execute();
        return cmd.foundInRevision();
    }

    /**
     * Prints content of an index entry accordant to the given file to the given output stream
     * @param file file whose relevant index entry to cat
     * @param stage version of the file in the index. In case of a merge conflict there are usually more
     *              versions of the file. <code>0</code> for normal non-conflict version,
     *              <code>1</code> for the base version,
     *              <code>2</code> for the first merged version ("ours") and 
     *              <code>3</code> for the second merged version ("theirs").
     * @param out output stream
     * @return <code>true</code> if the file was found in the index and printed to out, otherwise <code>false</code>
     * @throws GitException an unexpected error occurs
     */
    public boolean catIndexEntry (File file, int stage, java.io.OutputStream out, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        CatCommand cmd = new CatCommand(repository, getClassFactory(), file, stage, out, monitor);
        cmd.execute();
        return cmd.foundInRevision();
    }

    /**
     * Checks out the index into the working copy root. Does not move current HEAD.
     * @param revision if not <code>null</code>, index is updated with the revision content before checking out to WC
     * @param roots files/folders to checkout
     * @param recursively if set to <code>true</code>, all files under given roots will be checked out, otherwise only roots and direct file children will be affected.
     * @throws GitException an unexpected error occurs
     */
    public void checkout(File[] roots, String revision, boolean recursively, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        if (revision != null) {
            ResetCommand cmd = new ResetCommand(repository, getClassFactory(), revision, roots, recursively, monitor, delegateListener);
            cmd.execute();
        }
        if (!monitor.isCanceled()) {
            CheckoutIndexCommand cmd = new CheckoutIndexCommand(repository, getClassFactory(), roots, recursively, monitor, delegateListener);
            cmd.execute();
        }
    }

    /**
     * Checks out a given revision, modifies the Index as well as the Working tree.
     * @param revision cannot be <code>null</code>. If the value equals to anything other than an existing branch name, the revision will be checked out
     * and the working tree will be in the detached HEAD state.
     * @param failOnConflict if set to <code>false</code>, the command tries to merge local changes into the new branch
     * @throws GitException an unexpected error occurs
     */
    public void checkoutRevision (String revision, boolean failOnConflict, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        if (!failOnConflict) {
            throw new IllegalArgumentException("Currently unsupported. failOnConflict must be set to true. JGit lib is buggy."); //NOI18N
        }
        Repository repository = gitRepository.getRepository();
        CheckoutRevisionCommand cmd = new CheckoutRevisionCommand(repository, getClassFactory(), revision, failOnConflict, monitor, delegateListener);
        cmd.execute();
    }

    /**
     * Cleans the working tree by recursively removing files that are not under 
     * version control starting from the given roots.
     * @param roots files or folders to recursively remove from disk, versioned files under these files will not be deleted.
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public void clean(File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        CleanCommand cmd = new CleanCommand(repository, getClassFactory(), roots, monitor, delegateListener);
        cmd.execute();        
    }
    
    /**
     * Commits all changes made in the index to all files under the given roots
     * @param roots files or folders to recursively commit.
     * @param commitMessage commit message
     * @param author person who is the author of the changes to be committed
     * @param commiter person who is committing the changes, may not be the same person as author.
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public GitRevisionInfo commit(File[] roots, String commitMessage, GitUser author, GitUser commiter, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        CommitCommand cmd = new CommitCommand(repository, getClassFactory(), roots, commitMessage, author, commiter, monitor);
        cmd.execute();
        return cmd.revision;
    }

    /**
     * The index entries representing files under the source are copied and the newly created entries represent the corresponding files under the target.
     * <strong>Modifies only the index</strong>.
     * @param source source tree to copy
     * @param target target file or folder the source should be copied onto.
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public void copyAfter (File source, File target, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        CopyCommand cmd = new CopyCommand(repository, getClassFactory(), source, target, monitor, delegateListener);
        cmd.execute();
    }

    /**
     * Creates a new branch with a given name, starting at the given revision
     * @param branchName name that should be assigned to the new branch
     * @param revision revision that should be referenced by the new branch
     * @param monitor progress monitor
     * @return created branch
     * @throws GitException an unexpected error occurs
     */
    public GitBranch createBranch (String branchName, String revision, ProgressMonitor monitor) throws GitException {
        CreateBranchCommand cmd = new CreateBranchCommand(gitRepository.getRepository(), getClassFactory(), branchName, revision, monitor);
        cmd.execute();
        return cmd.getBranch();
    }

    /**
     * Creates a tag for any object represented by a given taggedObjectId. 
     * If message is set to <code>null</code> or an empty value and signed set to <code>false</code> than this method creates a <em>lightweight tag</em>.
     * @param tagName name of the new tag
     * @param taggedObject object to tag
     * @param message tag message
     * @param signed if the tag should be signed. Currently unsupported.
     * @param forceUpdate if a tag with the same name already exists, the method fails and throws an exception unless this is set to <code>true</code>. In that case the
     *                    old tag is replaced with the new one.
     * @param monitor progress monitor
     * @return the created tag
     * @throws GitException an unexpected error occurs
     */
    public GitTag createTag (String tagName, String taggedObject, String message, boolean signed, boolean forceUpdate, ProgressMonitor monitor) throws GitException {
        CreateTagCommand cmd = new CreateTagCommand(gitRepository.getRepository(), getClassFactory(), tagName, taggedObject, message, signed, forceUpdate, monitor);
        cmd.execute();
        return cmd.getTag();
    }

    /**
     * Deletes a given branch from the repository
     * @param branchName name of a branch to delete.
     * @param forceDeleteUnmerged if set to <code>true</code> then trying to delete an unmerged branch will not fail but will forcibly delete the branch
     * @param monitor progress monitor
     * @throws GitException.NotMergedException branch has not been fully merged yet and forceDeleteUnmerged is set to <code>false</code>
     * @throws GitException an unexpected error occurs
     */
    public void deleteBranch (String branchName, boolean forceDeleteUnmerged, ProgressMonitor monitor) throws GitException.NotMergedException, GitException {
        DeleteBranchCommand cmd = new DeleteBranchCommand(gitRepository.getRepository(), getClassFactory(), branchName, forceDeleteUnmerged, monitor);
        cmd.execute();
    }

    /**
     * Deletes a given tag from the repository
     * @param tagName name of a tag to delete
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public void deleteTag (String tagName, ProgressMonitor monitor) throws GitException {
        DeleteTagCommand cmd = new DeleteTagCommand(gitRepository.getRepository(), getClassFactory(), tagName, monitor);
        cmd.execute();
    }

    /**
     * Exports a given commit in the format accepted by git am
     * @param commit id of a commit whose diff to export
     * @param out output stream the diff will be printed to
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public void exportCommit (String commit, OutputStream out, ProgressMonitor monitor) throws GitException {
        ExportCommitCommand cmd = new ExportCommitCommand(gitRepository.getRepository(), getClassFactory(), commit, out, monitor, delegateListener);
        cmd.execute();
    }
    
    /**
     * Exports uncommitted changes in files under given roots to the given output stream
     * @param roots the diff will be exported only for modified files under these roots, can be empty to export all modifications in the whole working tree
     * @param mode defines the compared trees 
     * @param out output stream the diff will be printed to
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public void exportDiff (File[] roots, DiffMode mode, OutputStream out, ProgressMonitor monitor) throws GitException {
        ExportDiffCommand cmd = new ExportDiffCommand(gitRepository.getRepository(), getClassFactory(), roots, mode, out, monitor, delegateListener);
        cmd.execute();
    }
    
    /**
     * Fetches remote changes for references specified in the config file under a given remote.
     * @param remote should be a name of a remote set up in the repository config file
     * @param monitor progress monitor
     * @return result of the command with listed local reference updates
     * @throws GitException.AuthorizationException when the authentication or authorization fails
     * @throws GitException an unexpected error occurs
     */
    public Map<String, GitTransportUpdate> fetch (String remote, ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        FetchCommand cmd = new FetchCommand(gitRepository.getRepository(), getClassFactory(), remote, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getUpdates();
    }
    
    /**
     * Fetches remote changes from a remote repository for given reference specifications.
     * @param remote preferably a name of a remote, but can also be directly a URL of a remote repository
     * @param fetchRefSpecifications list of reference specifications describing the objects to fetch from the remote repository
     * @param monitor progress monitor
     * @return result of the command with listed local reference updates
     * @throws GitException.AuthorizationException when the authentication or authorization fails
     * @throws GitException an unexpected error occurs
     */
    public Map<String, GitTransportUpdate> fetch (String remote, List<String> fetchRefSpecifications, ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        FetchCommand cmd = new FetchCommand(gitRepository.getRepository(), getClassFactory(), remote, fetchRefSpecifications, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getUpdates();
    }
    
    /**
     * Returns all known branches from the repository
     * @param all if <code>false</code> then only local (and no remote) branches will be returned
     * @return all known branches in the repository
     * @throws GitException an unexpected error occurs
     */
    public Map<String, GitBranch> getBranches (boolean all, ProgressMonitor monitor) throws GitException {
        ListBranchCommand cmd = new ListBranchCommand(gitRepository.getRepository(), getClassFactory(), all, monitor);
        cmd.execute();
        return cmd.getBranches();
    }

    /**
     * Returns all tags in the repository
     * @param monitor progress monitor
     * @param allTags if set to <code>false</code>, only commit tags, otherwise tags for all objects are returned
     * @return all known tags from the repository
     * @throws GitException an unexpected error occurs
     */
    public Map<String, GitTag> getTags (ProgressMonitor monitor, boolean allTags) throws GitException {
        ListTagCommand cmd = new ListTagCommand(gitRepository.getRepository(), getClassFactory(), allTags, monitor);
        cmd.execute();
        return cmd.getTags();
    }

    /**
     * Returns a common ancestor for given revisions or <code>null</code> if none found.
     * @param revisions revisions whose common ancestor to search
     * @param monitor progress monitor
     * @return common ancestor for given revisions or <code>null</code> if none found.
     * @throws GitException an unexpected error occurs
     */
    public GitRevisionInfo getCommonAncestor (String[] revisions, ProgressMonitor monitor) throws GitException {
        GetCommonAncestorCommand cmd = new GetCommonAncestorCommand(gitRepository.getRepository(), getClassFactory(), revisions, monitor);
        cmd.execute();
        return cmd.getRevision();
    }

    /**
     * Returns an ancestor revision that affected a given file
     * @param file limit the result only on revision that actually modified somehow the file
     * @param revision revision to start search from, only its ancestors will be investigated
     * @param monitor progress monitor
     * @return an ancestor of a given revision that affected the given file or <code>null</code> if none found.
     * @throws GitException an unexpected error occurs
     */
    public GitRevisionInfo getPreviousRevision (File file, String revision, ProgressMonitor monitor) throws GitException {
        GetPreviousCommitCommand cmd = new GetPreviousCommitCommand(gitRepository.getRepository(), getClassFactory(), file, revision, monitor);
        cmd.execute();
        return cmd.getRevision();
    }

    /**
     * Similar to {@link #getStatus(java.io.File[], org.netbeans.libs.git.progress.ProgressMonitor)}, but returns only conflicts.
     * @param roots files to search the conflicts under
     * @param monitor progress monitor
     * @return conflicted files and their accordant statuses
     * @throws GitException an unexpected error occurs
     */
    public Map<File, GitStatus> getConflicts (File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        ConflictCommand cmd = new ConflictCommand(repository, getClassFactory(), roots, monitor, delegateListener);
        cmd.execute();
        return cmd.getStatuses();
    }

    /**
     * Returns an array of statuses for files under given roots
     * @param roots root folders or files to search under
     * @return status array
     * @throws GitException an unexpected error occurs
     */
    public Map<File, GitStatus> getStatus (File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        StatusCommand cmd = new StatusCommand(repository, getClassFactory(), roots, monitor, delegateListener);
        cmd.execute();
        return cmd.getStatuses();
    }

    /**
     * Returns remote configuration set up for this repository identified by a given remoteName
     * @param remoteName name under which the remote is stored in repository's config file
     * @param monitor progress monitor
     * @return remote config or <code>null</code> if no remote with such name was found
     * @throws GitException an unexpected error occurs
     */
    public GitRemoteConfig getRemote (String remoteName, ProgressMonitor monitor) throws GitException {
        return getRemotes(monitor).get(remoteName);
    }

    /**
     * Returns all remote configurations set up for this repository
     * @param monitor progress monitor
     * @return all known remote configurations
     * @throws GitException an unexpected error occurs
     */
    public Map<String, GitRemoteConfig> getRemotes (ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        GetRemotesCommand cmd = new GetRemotesCommand(repository, getClassFactory(), monitor);
        cmd.execute();
        return cmd.getRemotes();
    }
    
    /**
     * Returns the current state of the repository this client is associated with.
     * The state indicates what commands may be run on the repository and if the repository
     * requires any additional commands to get into the normal state.
     * @param monitor progress monitor
     * @return current repository state
     * @throws GitException an unexpected error occurs
     */
    public GitRepositoryState getRepositoryState (ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        RepositoryState state = repository.getRepositoryState();
        return GitRepositoryState.getStateFor(state);
    }

    /**
     * Returns the user from this clients repository
     * @throws GitException an unexpected error occurs
     */
    public GitUser getUser() throws GitException {        
        return getClassFactory().createUser(new PersonIdent(gitRepository.getRepository()));
    }

    /**
     * Ignores given files and add their path into <em>gitignore</em> file.
     * @param files files to ignore
     * @param monitor progress monitor
     * @return array of <em>.gitignore</em> modified during the ignore process
     * @throws GitException an unexpected error occurs
     */
    public File[] ignore (File[] files, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        IgnoreCommand cmd = new IgnoreCommand(repository, getClassFactory(), files, monitor, delegateListener);
        cmd.execute();
        return cmd.getModifiedIgnoreFiles();
    }

    /**
     * Initializes an empty git repository in a folder specified in the constructor. The repository must not yet exist - meaning
     * there cannot not be a <em>.git</em> folder in the given folder - however the folder itself may exist and contain any other source files
     * (except for git repository metadata).
     * @param monitor progress monitor
     * @throws GitException if the repository could not be created either because it already exists inside <code>workDir</code> or cannot be created for other reasons.
     */
    public void init (ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        InitRepositoryCommand cmd = new InitRepositoryCommand(repository, getClassFactory(), monitor);
        cmd.execute();
    }

    /**
     * Returns files that are marked as modified between the HEAD and Index.
     * @param roots files or folders to search for modified files.
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public File[] listModifiedIndexEntries (File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        ListModifiedIndexEntriesCommand cmd = new ListModifiedIndexEntriesCommand(repository, getClassFactory(), roots, monitor, delegateListener);
        cmd.execute();
        return cmd.getFiles();
    }
    
    /**
     * Returns available branches in a given remote repository
     * @param remoteRepositoryUrl url of the remote repository
     * @param monitor progress monitor
     * @return collection of available branches in the remote repository
     * @throws GitException.AuthorizationException when the authentication or authorization fails
     * @throws GitException an unexpected error occurs
     */
    public Map<String, GitBranch> listRemoteBranches (String remoteRepositoryUrl, ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        Repository repository = gitRepository.getRepository();
        ListRemoteBranchesCommand cmd = new ListRemoteBranchesCommand(repository, getClassFactory(), remoteRepositoryUrl, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getBranches();
    }
    
    /**
     * Returns pairs tag name/id from a given remote repository
     * @param remoteRepositoryUrl url of the remote repository
     * @param monitor progress monitor
     * @return remote repository tags
     * @throws GitException.AuthorizationException when the authentication or authorization fails
     * @throws GitException an unexpected error occurs
     */
    public Map<String, String> listRemoteTags (String remoteRepositoryUrl, ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        Repository repository = gitRepository.getRepository();
        ListRemoteTagsCommand cmd = new ListRemoteTagsCommand(repository, getClassFactory(), remoteRepositoryUrl, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getTags();
    }

    /**
     * Digs through the repository's history and returns the revision information belonging to the given revision string.
     * @param revision revision to search in the history
     * @param monitor progress monitor
     * @return revision information
     * @throws GitException.MissingObjectException no such revision exists
     * @throws GitException an unexpected error occurs
     */
    public GitRevisionInfo log (String revision, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        LogCommand cmd = new LogCommand(repository, getClassFactory(), revision, monitor, delegateListener);
        cmd.execute();
        GitRevisionInfo[] revisions = cmd.getRevisions();
        return revisions.length == 0 ? null : revisions[0];
    }

    /**
     * Digs through the repository's history and returns revisions according to the given search criteria.
     * @param searchCriteria criteria filtering the returned revisions
     * @param monitor progress monitor
     * @return revisions that follow the given search criteria
     * @throws GitException.MissingObjectException revision specified in search criteria (or head if no such revision is specified) does not exist
     * @throws GitException an unexpected error occurs
     */
    public GitRevisionInfo[] log (SearchCriteria searchCriteria, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        LogCommand cmd = new LogCommand(repository, getClassFactory(), searchCriteria, monitor, delegateListener);
        cmd.execute();
        return cmd.getRevisions();
    }
    
    /**
     * Merges a given revision with the current head
     * @param revision id of a revision to merge.
     * @param monitor progress monitor
     * @return result of the merge
     * @throws GitException.CheckoutConflictException there are local modifications in Working Tree, merge fails in such a case
     * @throws GitException an unexpected error occurs
     */
    public GitMergeResult merge (String revision, ProgressMonitor monitor) throws GitException.CheckoutConflictException, GitException {
        Repository repository = gitRepository.getRepository();
        MergeCommand cmd = new MergeCommand(repository, getClassFactory(), revision, monitor);
        cmd.execute();
        return cmd.getResult();
    }
    
    /**
     * Pulls changes from a remote repository and merges a given remote branch to an active one.
     * @param remote preferably a name of a remote, but can also be directly a URL of a remote repository
     * @param fetchRefSpecifications list of reference specifications describing what objects to fetch from the remote repository
     * @param branchToMerge a remote branch that will be merged into an active branch
     * @param monitor progress monitor
     * @return result of the command containing the list of updated local references
     * @throws GitException.AuthorizationException when the authentication or authorization fails
     * @throws GitException.CheckoutConflictException there are local changes in the working tree that would result in a merge conflict
     * @throws GitException.MissingObjectException given branch to merge does not exist
     * @throws GitException an unexpected error occurs
     */
    public GitPullResult pull (String remote, List<String> fetchRefSpecifications, String branchToMerge, ProgressMonitor monitor) throws GitException.AuthorizationException, 
            GitException.CheckoutConflictException, GitException.MissingObjectException, GitException {
        PullCommand cmd = new PullCommand(gitRepository.getRepository(), getClassFactory(), remote, fetchRefSpecifications, branchToMerge, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getResult();
    }
    
    /**
     * Pushes changes to a remote repository specified by remote for given reference specifications.
     * @param remote preferably a name of a remote defined in the repository's config,
     *               but can also be directly a URL of a remote repository
     * @param pushRefSpecifications list of reference specifications describing the list of references to push
     * @param fetchRefSpecifications list of fetch reference specifications describing the list of local references to update
     *                               to correctly track remote repository branches.
     * @param monitor progress monitor
     * @return result of the push process with information about updated local and remote references
     * @throws GitException.AuthorizationException when the authentication or authorization fails
     * @throws GitException an unexpected error occurs
     */
    public GitPushResult push (String remote, List<String> pushRefSpecifications, List<String> fetchRefSpecifications, ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        PushCommand cmd = new PushCommand(gitRepository.getRepository(), getClassFactory(), remote, pushRefSpecifications, fetchRefSpecifications, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getResult();
    }

    /**
     * Removes given files/folders from the index and/or from the working tree
     * @param roots files/folders to remove, can not be empty
     * @param cached if <code>true</code> the working tree will not be affected
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public void remove (File[] roots, boolean cached, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        RemoveCommand cmd = new RemoveCommand(repository, getClassFactory(), roots, cached, monitor, delegateListener);
        cmd.execute();
    }

    /**
     * Removes an already added notification listener. Such a listener will not get notifications from the 
     * git subsystem.
     * @param listener listener to remove.
     */
    public void removeNotificationListener (NotificationListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    /**
     * Removes remote configuration from the repository's config file
     * @param remote name of the remote
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public void removeRemote (String remote, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        RemoveRemoteCommand cmd = new RemoveRemoteCommand(repository, getClassFactory(), remote, monitor);
        cmd.execute();
    }

    /**
     * Renames source file or folder to target
     * @param source file or folder to be renamed
     * @param target target file or folder. Must not yet exist.
     * @param after set to true if you don't only want to correct the index
     * @param monitor progress monitor
     * @throws GitException an unexpected error occurs
     */
    public void rename (File source, File target, boolean after, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        RenameCommand cmd = new RenameCommand(repository, getClassFactory(), source, target, after, monitor, delegateListener);
        cmd.execute();
    }
    
    /**
     * Updates entries for given files in the index with those from the given revision
     * @param revision revision to go back to
     * @param roots files or folders to update in the index
     * @param recursively if set to <code>true</code>, all files under given roots will be affected, otherwise only roots and direct file children will be modified in the index.
     * @param monitor progress monitor
     * @throws GitException.MissingObjectException if the given revision does not exist
     * @throws GitException an unexpected error occurs
     */
    public void reset (File[] roots, String revision, boolean recursively, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        ResetCommand cmd = new ResetCommand(repository, getClassFactory(), revision, roots, recursively, monitor, delegateListener);
        cmd.execute();
    }

    /**
     * Sets HEAD to the given revision and updates index and working copy accordingly to the given reset type
     * @param revisionStr revision HEAD will reference to
     * @param resetType type of reset, see git help reset
     * @param monitor progress monitor
     * @throws GitException.MissingObjectException if the given revision does not exist
     * @throws GitException an unexpected error occurs
     */
    public void reset (String revision, ResetType resetType, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        ResetCommand cmd = new ResetCommand(repository, getClassFactory(), revision, resetType, monitor, delegateListener);
        cmd.execute();
    }

    /**
     * Reverts already committed changes and creates an inverse commit.
     * @param revision the id of a commit to revert
     * @param commitMessage used as the commit message for the revert commit. If set to null or an empty value, a default value will be used for the commit message
     * @param commit if set to <code>false</code>, the revert modifications will not be committed but will stay in index
     * @param monitor progress monitor
     * @return result of the revert command
     * @throws GitException.MissingObjectException if the given revision does not exist
     * @throws GitException.CheckoutConflictException there are local modifications in Working Tree, merge fails in such a case
     * @throws GitException an unexpected error occurs
     */
    public GitRevertResult revert (String revision, String commitMessage, boolean commit, ProgressMonitor monitor)
            throws GitException.MissingObjectException, GitException.CheckoutConflictException, GitException {
        Repository repository = gitRepository.getRepository();
        RevertCommand cmd = new RevertCommand(repository, getClassFactory(), revision, commitMessage, commit, monitor);
        cmd.execute();
        return cmd.getResult();
    }

    /**
     * Sets credentials callback for this client.
     * Some actions (like inter-repository commands) may need it for its work to communicate with an external repository.
     * @param callback callback implementation providing credentials for an authentication process.
     */
    public void setCallback (GitClientCallback callback) {
        this.credentialsProvider = callback == null ? null : new JGitCredentialsProvider(callback);
    }
    
    /**
     * Sets the remote configuration in the configuration file.
     * @param remoteConfig new remote config to store as a <em>remote</em> section in the repository's <em>config</em> file.
     * @param monitor progress monitor
     */
    public void setRemote (GitRemoteConfig remoteConfig, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        SetRemoteCommand cmd = new SetRemoteCommand(repository, getClassFactory(), remoteConfig, monitor);
        cmd.execute();
    }

    /**
     * Unignores given files
     * @param files files to mark unignored again and remove their respective record from <em>gitignore</em> files.
     * @param monitor progress monitor
     * @return array of .gitignore files modified during the unignore process
     * @throws GitException an unexpected error occurs
     */
    public File[] unignore (File[] files, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        UnignoreCommand cmd = new UnignoreCommand(repository, getClassFactory(), files, monitor, delegateListener);
        cmd.execute();
        return cmd.getModifiedIgnoreFiles();
    }

    private GitClassFactory getClassFactory () {
        if (gitFactory == null) {
            gitFactory = GitClassFactoryImpl.getInstance();
        }
        return gitFactory;
    }
    
    private class DelegateListener implements StatusListener, FileListener, RevisionInfoListener {

        @Override
        public void notifyStatus (GitStatus status) {
            GitClient.this.notifyStatus(status);
        }

        @Override
        public void notifyFile (File file, String relativePathToRoot) {
            GitClient.this.notifyFile(file, relativePathToRoot);
        }

        @Override
        public void notifyRevisionInfo (GitRevisionInfo revisionInfo) {
            GitClient.this.notifyRevisionInfo(revisionInfo);
        }
        
    }
    
    // <editor-fold defaultstate="collapsed" desc="listener methods">
    private void notifyFile (File file, String relativePathToRoot) {
        List<NotificationListener> lists;
        synchronized (listeners) {
            lists = new LinkedList<NotificationListener>(listeners);
        }
        for (NotificationListener list : lists) {
            if (list instanceof FileListener) {
                ((FileListener) list).notifyFile(file, relativePathToRoot);
            }
        }
    }

    private void notifyStatus (GitStatus status) {
        List<NotificationListener> lists;
        synchronized (listeners) {
            lists = new LinkedList<NotificationListener>(listeners);
        }
        for (NotificationListener list : lists) {
            if (list instanceof StatusListener) {
                ((StatusListener) list).notifyStatus(status);
            }
        }
    }

    private void notifyRevisionInfo (GitRevisionInfo info) {
        List<NotificationListener> lists;
        synchronized (listeners) {
            lists = new LinkedList<NotificationListener>(listeners);
        }
        for (NotificationListener list : lists) {
            if (list instanceof RevisionInfoListener) {
                ((RevisionInfoListener) list).notifyRevisionInfo(info);
            }
        }
    }// </editor-fold>
}
