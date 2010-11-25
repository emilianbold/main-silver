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

package org.netbeans.modules.git.utils;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.FileStatusCache;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.ui.status.GitStatusNode;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.FileSelector;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *s
 * @author ondra
 */
public final class GitUtils {

    public static final String DOT_GIT = ".git"; //NOI18N
    private static final Pattern METADATA_PATTERN = Pattern.compile(".*\\" + File.separatorChar + "(\\.)git(\\" + File.separatorChar + ".*|$)"); // NOI18N
    private static final String FILENAME_GITIGNORE = ".gitignore"; // NOI18N
    public static final String HEAD = "HEAD"; //NOI18N
    public static final String INDEX = "INDEX"; //NOI18N
    public static final String CURRENT = "CURRENT"; //NOI18N

    /**
     * Checks file location to see if it is part of git metadata
     *
     * @param file file to check
     * @return true if the file or folder is a part of git metadata, false otherwise
     */
    public static boolean isPartOfGitMetadata (File file) {
        return METADATA_PATTERN.matcher(file.getAbsolutePath()).matches();
    }

    /**
     * Tests <tt>.hg</tt> directory itself.
     */
    public static boolean isAdministrative (File file) {
        String name = file.getName();
        return isAdministrative(name) && file.isDirectory();
    }

    public static boolean isAdministrative (String fileName) {
        return fileName.equals(DOT_GIT); // NOI18N
    }

    public static boolean repositoryExistsFor (File file) {
        return new File(file, DOT_GIT).exists();
    }

    /**
     * Returns the administrative git folder for the given repository and normalizes the file
     * @param repositoryRoot root of the repository
     * @return administrative git folder
     */
    public static File getGitFolderForRoot (File repositoryRoot) {
        return FileUtil.normalizeFile(new File(repositoryRoot, DOT_GIT));
    }

    /**
     * Adds the given file into filesUnderRoot:
     * <ul>
     * <li>if the file was already in the set, does nothing and returns true</li>
     * <li>if the file lies under a folder already present in the set, does nothing and returns true</li>
     * <li>if the file and none of it's ancestors is not in the set yet, this adds the file into the set,
     * removes all it's children and returns false</li>
     * @param repository repository root
     * @param filesUnderRoot set of repository roots
     * @param file file to add
     * @return false if the file was added or true if it was already contained
     */
    public static boolean prepareRootFiles (File repository, Collection<File> filesUnderRoot, File file) {
        boolean added = false;
        Set<File> filesToRemove = new HashSet<File>();
        for (File fileUnderRoot : filesUnderRoot) {
            if (file.equals(fileUnderRoot) || fileUnderRoot.equals(repository)) {
                // file has already been inserted or scan is planned for the whole repository root
                added = true;
                break;
            }
            if (file.equals(repository)) {
                // plan the scan for the whole repository root
                // adding the repository, there's no need to leave all other files
                filesUnderRoot.clear();
                break;
            } else {
                if (file.getAbsolutePath().length() < fileUnderRoot.getAbsolutePath().length()) {
                    if (Utils.isAncestorOrEqual(file, fileUnderRoot)) {
                        filesToRemove.add(fileUnderRoot);
                    }
                } else {
                    if (Utils.isAncestorOrEqual(fileUnderRoot, file)) {
                        added = true;
                        break;
                    }
                }
            }
        }
        filesUnderRoot.removeAll(filesToRemove);
        if (!added) {
            // not added yet
            filesUnderRoot.add(file);
        }
        return added;
    }
    
    public static boolean isIgnored(File file, boolean checkSharability){
        if (file == null) return false;
        String path = file.getPath();
        File topFile = Git.getInstance().getRepositoryRoot(file);
        
        // We assume that the toplevel directory should not be ignored.
        if (topFile == null || topFile.equals(file)) {
            return false;
        }
        
//        Set<Pattern> patterns = getIgnorePatterns(topFile);
//        try {
//        path = path.substring(topFile.getAbsolutePath().length() + 1);
//        } catch(StringIndexOutOfBoundsException e) {
//            throw e;
//        }
//        if (File.separatorChar != '/') {
//            path = path.replace(File.separatorChar, '/');
//        }
//
//        for (Iterator i = patterns.iterator(); i.hasNext();) {
//            Pattern pattern = (Pattern) i.next();
//            if (pattern.matcher(path).find()) {
//                return true;
//            }
//        }

        // check cached not sharable folders and files
        if (isNotSharable(path, topFile)) {
            return true;
        }

        // If a parent of the file matches a pattern ignore the file
        File parentFile = file.getParentFile();
        if (!parentFile.equals(topFile)) {
            if (isIgnored(parentFile, false)) return true;
        }

        if (FILENAME_GITIGNORE.equals(file.getName())) return false;
        if (checkSharability) {
            int sharability = SharabilityQuery.getSharability(FileUtil.normalizeFile(file));
            if (sharability == SharabilityQuery.NOT_SHARABLE) {
                addNotSharable(topFile, path);
                return true;
            }
        }
        return false;
    }

    // cached not sharable files and folders
    private static final Map<File, Set<String>> notSharable = Collections.synchronizedMap(new HashMap<File, Set<String>>(5));
    private static void addNotSharable (File topFile, String ignoredPath) {
        synchronized (notSharable) {
            // get cached patterns
            Set<String> ignores = notSharable.get(topFile);
            if (ignores == null) {
                ignores = new HashSet<String>();
            }
            String patternCandidate = ignoredPath;
            // test for duplicate patterns
            for (Iterator<String> it = ignores.iterator(); it.hasNext();) {
                String storedPattern = it.next();
                if (storedPattern.equals(ignoredPath) // already present
                        || ignoredPath.startsWith(storedPattern + '/')) { // path already ignored by its ancestor
                    patternCandidate = null;
                    break;
                } else if (storedPattern.startsWith(ignoredPath + '/')) { // stored pattern matches a subset of ignored path
                    // remove the stored pattern and add the ignored path
                    it.remove();
                }
            }
            if (patternCandidate != null) {
                ignores.add(patternCandidate);
            }
            notSharable.put(topFile, ignores);
        }
    }

    private static boolean isNotSharable (String path, File topFile) {
        boolean retval = false;
        Set<String> notSharablePaths = notSharable.get(topFile);
        if (notSharablePaths == null) {
            notSharablePaths = Collections.emptySet();
        }
        retval = notSharablePaths.contains(path);
        return retval;
    }

    /**
     * Determines if the given context contains at least one root file from a git repository
     *
     * @param VCSContext
     * @return true if the given context contains a root file from a git repository
     */
    public static boolean isFromGitRepository (VCSContext context){
        return getRootFile(context) != null;
    }

    /**
     * Returns path to repository root or null if not managed
     *
     * @param VCSContext
     * @return String of repository root path
     */
    public static File getRootFile (VCSContext context){
        if (context == null) return null;
        Git git = Git.getInstance();
        File [] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        if (files == null || files.length == 0) return null;

        File root = git.getRepositoryRoot(files[0]);
        return root;
    }

    /**
     * Returns repository roots for all root files from context
     *
     * @param VCSContext
     * @return repository roots
     */
    public static Set<File> getRepositoryRoots(VCSContext context) {
        Set<File> rootsSet = context.getRootFiles();
        return getRepositoryRoots(rootsSet);
    }

    /**
     * Returns repository roots for all root files from context
     *
     * @param roots root files
     * @return repository roots
     */
    public static Set<File> getRepositoryRoots (Set<File> roots) {
        Set<File> ret = new HashSet<File>();

        // filter managed roots
        for (File file : roots) {
            if(Git.getInstance().isManaged(file)) {
                File repoRoot = Git.getInstance().getRepositoryRoot(file);
                if(repoRoot != null) {
                    ret.add(repoRoot);
                }
            }
        }
        return ret;
    }

    /**
     *
     * @param ctx
     * @return
     */
    public static HashMap.SimpleImmutableEntry<File, File[]> getActionRoots(VCSContext ctx) {
        Set<File> rootsSet = ctx.getRootFiles();
        Map<File, List<File>> map = new HashMap<File, List<File>>();

        // filter managed roots
        for (File file : rootsSet) {
            if(Git.getInstance().isManaged(file)) {
                File repoRoot = Git.getInstance().getRepositoryRoot(file);
                if(repoRoot != null) {
                    List<File> l = map.get(repoRoot);
                    if(l == null) {
                        l = new LinkedList<File>();
                        map.put(repoRoot, l);
                    }
                    l.add(file);
                }
            }
        }

        Set<File> repoRoots = map.keySet();
        if(map.size() > 1) {
            // more than one managed root => need a dlg
            FileSelector fs = new FileSelector(
                    NbBundle.getMessage(GitUtils.class, "LBL_FileSelector_Title"), //NOI18N
                    NbBundle.getMessage(GitUtils.class, "FileSelector.jLabel1.text"), //NOI18N
                    new HelpCtx("org.netbeans.modules.git.FileSelector"), //NOI18N
                    GitModuleConfig.getDefault().getPreferences());
            if(fs.show(repoRoots.toArray(new File[repoRoots.size()]))) {
                File selection = fs.getSelectedFile();
                List<File> l = map.get(selection);
                return new HashMap.SimpleImmutableEntry<File, File[]>(selection, l.toArray(new File[l.size()]));
            } else {
                return null;
            }
        } else if (map.isEmpty()) {
            return null;
        } else {
            File root = map.keySet().iterator().next();
            List<File> l = map.get(root);
            return new HashMap.SimpleImmutableEntry<File, File[]>(root, l.toArray(new File[l.size()]));
        }
    }

    /**
     * Returns only those root files from the given context which belong to repository
     * @param ctx
     * @param repository
     * @return
     */
    public static File[] filterForRepository(final VCSContext ctx, final File repository) {
        File[] files = null;
        if(ctx != null) {
            Set<File> s = ctx.getRootFiles();
            files = s.toArray(new File[s.size()]);
        }
        if (files != null) {
            List<File> l = new LinkedList<File>();
            for (File file : files) {
                File r = Git.getInstance().getRepositoryRoot(file);
                if (r != null && r.equals(repository)) {
                    l.add(file);
                }
            }
            files = l.toArray(new File[l.size()]);
        }
        return files;
    }

    /**
     * Normalize flat files, Git treats folder as normal file
     * so it's necessary explicitly list direct descendants to
     * get classical flat behaviour.
     * <strong>Does not return up-to-date files</strong>
     *
     * <p> E.g. revert on package node means:
     * <ul>
     *   <li>revert package folder properties AND
     *   <li>revert all modified (including deleted) files in the folder
     * </ul>
     *
     * @return files with given status and direct descendants with given status.
     */

    public static File[] flatten(File[] files, Set<Status> statuses) {
        LinkedList<File> ret = new LinkedList<File>();

        FileStatusCache cache = Git.getInstance().getFileStatusCache();
        for (int i = 0; i<files.length; i++) {
            File dir = files[i];
            FileInformation info = cache.getStatus(dir);
            if (info.containsStatus(statuses)) {
                ret.add(dir);
            }
            File[] entries = cache.listFiles(dir);  // comparing to dir.listFiles() lists already deleted too
            for (int e = 0; e<entries.length; e++) {
                File entry = entries[e];
                info = cache.getStatus(entry);
                if (info.containsStatus(statuses)) {
                    ret.add(entry);
                }
            }
        }

        return ret.toArray(new File[ret.size()]);
    }

    /**
     * Semantics is similar to {@link org.openide.windows.TopComponent#getActivatedNodes()} except that this
     * method returns File objects instead of Nodes. Every node is examined for Files it represents. File and Folder
     * nodes represent their underlying files or folders. Project nodes are represented by their source groups. Other
     * logical nodes must provide FileObjects in their Lookup.
     *
     * @return File [] array of activated files
     * @param nodes or null (then taken from windowsystem, it may be wrong on editor tabs #66700).
     */
    public static VCSContext getCurrentContext(Node[] nodes) {
        if (nodes == null) {
            nodes = TopComponent.getRegistry().getActivatedNodes();
        }
        return VCSContext.forNodes(nodes);
    }

    /**
     * Uses content analysis to return the mime type for files.
     *
     * @param file file to examine
     * @return String mime type of the file (or best guess)
     */
    public static String getMimeType(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        String foMime;
        boolean hasMime = false;
        if (fo == null) {
            foMime = "content/unknown"; // NOI18N
        } else {
            foMime = fo.getMIMEType();
            if ("content/unknown".equals(foMime)) { // NOI18N
                foMime = "text/plain"; // NOI18N
            } else {
                hasMime = true;
            }
        }
        if (!hasMime) {
            return isFileContentBinary(file) ? "application/octet-stream" : foMime; // NOI18N
        } else {
            return foMime;
        }
    }

    /**
     * Checks if the file is binary.
     *
     * @param file file to check
     * @return true if the file cannot be edited in NetBeans text editor, false otherwise
     */
    public static boolean isFileContentBinary(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) return false;
        try {
            DataObject dao = DataObject.find(fo);
            return dao.getCookie(EditorCookie.class) == null;
        } catch (DataObjectNotFoundException e) {
            // not found, continue
        }
        return false;
    }

    /**
     * Determines if the context has been created in a git view, i.e. it consists of instances of {@link GitStatusNode}
     * @param context
     * @return true if the context contains instances of {@link GitStatusNode}
     */
    public static boolean isFromInternalView (VCSContext context) {
        return context.getElements().lookup(GitStatusNode.class) != null;
    }
    
    private GitUtils() {
    }
}
