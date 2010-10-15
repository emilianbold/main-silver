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
import java.util.Collection;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheCheckout;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.util.FS;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class CheckoutIndexCommand extends GitCommand {

    private final File[] roots;
    private final FileListener listener;
    private final ProgressMonitor monitor;

    public CheckoutIndexCommand (Repository repository, File[] roots, ProgressMonitor monitor, FileListener listener) {
        super(repository, monitor);
        this.roots = roots;
        this.listener = listener;
        this.monitor = monitor;
    }

    @Override
    protected void run() throws GitException {
        Repository repository = getRepository();
        try {
            DirCache cache = repository.readDirCache();
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
                if (dit != null && (fit == null || fit.isModified(dit.getDirCacheEntry(), true, true, FS.DETECTED))) {
                    // update entry
                    listener.notifyFile(path);
                    checkoutEntry(repository, path, dit.getDirCacheEntry());
                }
            }
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

    private void checkoutEntry (Repository repository, File file, DirCacheEntry e) throws IOException {
        // ... create/overwrite this file ...
        if (!ensureParentFolderExists(file.getParentFile())) {
            return;
        }
        if (file.isDirectory()) {
            monitor.notifyWarning("Replacing directory " + file.getAbsolutePath());
            Utils.deleteRecursively(file);
        }
        file.createNewFile();
        if (file.isFile()) {
            DirCacheCheckout.checkoutEntry(repository, file, e, getFileMode(repository));
        } else {
            monitor.notifyError("Cannot create file " + file.getAbsolutePath());
        }
    }

    private Boolean filemode;
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
                monitor.notifyError("Cannot replace file " + predecessor.getAbsolutePath());
                return false;
            }
            monitor.notifyWarning("Replacing file " + predecessor.getAbsolutePath());
        }
        return parentFolder.mkdirs() || parentFolder.exists();
    }
}
