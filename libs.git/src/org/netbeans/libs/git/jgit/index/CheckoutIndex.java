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

package org.netbeans.libs.git.jgit.index;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.JGitText;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheCheckout;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.LargeObjectException;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.util.FS;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitException.MissingObjectException;
import org.netbeans.libs.git.GitObjectType;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class CheckoutIndex {

    private final Repository repository;
    private final DirCache cache;
    private final File[] roots;
    private final FileListener listener;
    private final ProgressMonitor monitor;
    private Boolean filemode;
    private final boolean checkContent;

    private static final Logger LOG = Logger.getLogger(CheckoutIndex.class.getName());

    public CheckoutIndex (Repository repository, DirCache cache, File[] roots, FileListener listener, ProgressMonitor monitor, boolean checkContent) {
        this.repository = repository;
        this.cache = cache;
        this.roots = roots;
        this.listener = listener;
        this.monitor = monitor;
        this.checkContent = checkContent;
    }

    public void checkout () throws IOException, GitException {
        TreeWalk treeWalk = new TreeWalk(repository);
        Collection<String> relativePaths = Utils.getRelativePaths(repository.getWorkTree(), roots);
        if (!relativePaths.isEmpty()) {
            treeWalk.setFilter(PathFilterGroup.createFromStrings(relativePaths));
        }
        treeWalk.setRecursive(true);
        treeWalk.reset();
        treeWalk.addTree(new DirCacheIterator(cache));
        treeWalk.addTree(new FileTreeIterator(repository));
        String lastAddedPath = null;
        while (treeWalk.next() && !monitor.isCanceled()) {
            File path = new File(repository.getWorkTree(), treeWalk.getPathString());
            if (treeWalk.getPathString().equals(lastAddedPath)) {
                // skip conflicts
                continue;
            } else {
                lastAddedPath = treeWalk.getPathString();
            }
            DirCacheIterator dit = treeWalk.getTree(0, DirCacheIterator.class);
            FileTreeIterator fit = treeWalk.getTree(1, FileTreeIterator.class);
            if (dit != null && (fit == null || fit.isModified(dit.getDirCacheEntry(), checkContent, true, FS.DETECTED))) {
                // update entry
                listener.notifyFile(path, treeWalk.getPathString());
                checkoutEntry(repository, path, dit.getDirCacheEntry(), fit);
            }
        }
    }

    private void checkoutEntry (Repository repository, File file, DirCacheEntry e, FileTreeIterator fit) throws IOException, GitException {
        // ... create/overwrite this file ...
        if (!ensureParentFolderExists(file.getParentFile())) {
            return;
        }

        boolean exists = file.exists();
        if (exists && e.getFileMode() == FileMode.SYMLINK) {
            monitor.notifyWarning(NbBundle.getMessage(CheckoutIndex.class, "MSG_Warning_SymLink", file.getAbsolutePath())); //NOI18N
            return;
        }

        if (exists && file.isDirectory()) {
            monitor.notifyWarning(NbBundle.getMessage(CheckoutIndex.class, "MSG_Warning_ReplacingDirectory", file.getAbsolutePath())); //NOI18N
            Utils.deleteRecursively(file);
        }
        file.createNewFile();
        if (file.isFile()) {
            try {
                DirCacheCheckout.checkoutEntry(repository, file, e, getFileMode(repository));
            } catch (LargeObjectException ex) {
                LOG.log(Level.FINE, "checking out a large file: " + file, ex); //NOI18N
                // Should be removed when jgit is able to checkout large files
                if (fit.isModified(e, true, true, FS.DETECTED)) {
                    // do not checkout large if not modified
                    checkoutLargeEntry(file, e);
                }
            }
        } else {
            monitor.notifyError(NbBundle.getMessage(CheckoutIndex.class, "MSG_Warning_CannotCreateFile", file.getAbsolutePath())); //NOI18N
        }
    }

    private boolean getFileMode (Repository repository) {
        if (filemode == null) {
            filemode = Utils.checkExecutable(repository);
        }
        return filemode.booleanValue();
    }

    private boolean ensureParentFolderExists (File parentFolder) {
        File predecessor = parentFolder;
        while (!predecessor.exists()) {
            predecessor = predecessor.getParentFile();
        }
        if (predecessor.isFile()) {
            if (!predecessor.delete()) {
                monitor.notifyError(NbBundle.getMessage(CheckoutIndex.class, "MSG_Warning_CannotCreateFile", predecessor.getAbsolutePath())); //NOI18N
                return false;
            }
            monitor.notifyWarning(NbBundle.getMessage(CheckoutIndex.class, "MSG_Warning_ReplacingFile", predecessor.getAbsolutePath())); //NOI18N
        }
        return parentFolder.mkdirs() || parentFolder.exists();
    }

    /**
     * JGit cannot checkout a large object by itself, it throws an exception
     */
    private void checkoutLargeEntry (File f, DirCacheEntry entry) throws IOException, MissingObjectException {
                ObjectLoader ol = repository.open(entry.getObjectId());
        if (ol == null) {
            throw new MissingObjectException(entry.getObjectId().getName(), GitObjectType.BLOB);
        }

        if (!ol.isLarge()) {
            throw new IllegalStateException("Must be a large object: " + entry.getObjectId().getName());
        }

        File parentDir = f.getParentFile();
        File tmpFile = File.createTempFile("._" + f.getName(), null, parentDir);

        FileOutputStream fos = new FileOutputStream(tmpFile);
        ol.copyTo(fos);

        FS fs = repository.getFS();
        boolean config_filemode = getFileMode(repository);
        if (config_filemode && fs.supportsExecute()) {
            if (FileMode.EXECUTABLE_FILE.equals(entry.getRawMode())) {
                if (!fs.canExecute(tmpFile))
                    fs.setExecute(tmpFile, true);
            } else {
                if (fs.canExecute(tmpFile))
                    fs.setExecute(tmpFile, false);
            }
        }
        if (!tmpFile.renameTo(f)) {
            f.delete();
            if (!tmpFile.renameTo(f)) {
                throw new IOException(MessageFormat.format(JGitText.get().couldNotWriteFile, tmpFile.getPath(), f.getPath()));
            }
        }
        entry.setLastModified(f.lastModified());
        entry.setLength((int) ol.getSize());
    }
}
