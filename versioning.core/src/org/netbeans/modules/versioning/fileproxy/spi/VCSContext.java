/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.versioning.fileproxy.spi;

import org.netbeans.modules.versioning.core.SPIAccessor;
import java.io.File;
import org.netbeans.modules.versioning.core.Utils;
import org.netbeans.modules.versioning.core.VersioningManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.queries.SharabilityQuery;
import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;

import java.util.*;
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.versioning.core.spi.VCSSystemProvider;
import org.netbeans.modules.versioning.core.spi.VCSSystemProvider.VersioningSystem;
import org.netbeans.modules.versioning.fileproxy.api.VCSFileProxy;

/**
 * This encapsulates a context, typically set of selected files or nodes. Context is passed to VCSAnnotators when
 * asked for actions available on a given context or to annotate a name (label) representing a context.
 * 
 * @author Maros Sandor
 */
public final class VCSContext {
    
    /**
     * VCSContext that contains no files.
     */
    public static final VCSContext EMPTY = new VCSContext((Node[]) null, emptySet(), emptySet() );
    private static final Logger LOG = Logger.getLogger(VCSContext.class.getName());

    /**
     * Caching of current context for performance reasons, also see #72006.
     */
    private static Reference<VCSContext>  contextCached = new WeakReference<VCSContext>(null);    
    private static Reference<Node[]> contextNodesCached = new WeakReference<Node []>(null); 

    private final Lookup    elements;
    
    private final Set<VCSFileProxy> unfilteredRootFiles;
    private final Set<VCSFileProxy> rootFiles;
    private final Set<VCSFileProxy> exclusions;

    private Set<VCSFileProxy>       computedFilesCached;
    private FileFilter      fileFilterCached;

    public interface FileFilter {
        boolean accept(VCSFileProxy file);
    }
    
    static {
        SPIAccessor.IMPL = new SPIAccessorImpl();
    }
    
    /**
     * Constructs a VCSContext out of a set of files. These files are later available via getRootFiles().
     * 
     * @param rootFiles set of Files
     * @param originalFiles set of original files for which the context shall be created
     * @return VCSContext a context representing supplied set of Files
     */ 
    static VCSContext forFiles(Set<VCSFileProxy> rootFiles, Set<? extends FileObject> originalFiles) {
        return new VCSContext(originalFiles, rootFiles, emptySet());
    }

    /**
     * Initializes the context from array of nodes (typically currently activated nodes).
     * Nodes are converted to Files based on their nature. 
     * For example Project Nodes are queried for their SourceRoots and those roots become root files of this context and
     * exclusions list is constructed using sourceRoot.contains() queries.
     * 
     * Nodes' lookups are examined in the following way (the first applied rule wins):
     * - if there's a File, the File is added to set of root files
     * - if there's a Project, project's source roots of type Sources.TYPE_GENERIC are added to set of root files and
     *   all direct children that do not belong to the project (sg.contains() == false) are added to set of exclusions
     * - if there's a FileObject, it is added to set of root files
     * - if there's a DataObject, all dao.files() are added to set of root files 
     * 
     * @param nodes array of Nodes
     * @return VCSContext containing nodes and corresponding files they represent
     */
    // XXX replace with lookup
    public synchronized static VCSContext forNodes(Node[] nodes) {
        if (Arrays.equals(contextNodesCached.get(), nodes)) {
            VCSContext ctx = contextCached.get();
            if (ctx != null) return ctx;
        }
        Set<VCSFileProxy> rootFiles = new HashSet<VCSFileProxy>(nodes.length);
        Set<VCSFileProxy> rootFileExclusions = new HashSet<VCSFileProxy>(5);
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            File aFile = node.getLookup().lookup(File.class);
            if (aFile != null) {
                rootFiles.add(VCSFileProxy.createFileProxy(aFile));
                continue;
            }
            Project project =  node.getLookup().lookup(Project.class);
            if (project != null) {
                addProjectFiles(rootFiles, rootFileExclusions, project);
                continue;
            }
            addFileObjects(node, rootFiles);
        }

        if (rootFiles.isEmpty()) {
            LOG.fine("forNodes: context contains no root files");    //NOI18N
        }
        List<VCSFileProxy> unversionedFiles = new ArrayList<VCSFileProxy>(rootFiles.size());
        Set<VersioningSystem> projectOwners = new HashSet<VersioningSystem>(2);
        for (VCSFileProxy root : rootFiles) {
            VCSSystemProvider.VersioningSystem owner = VersioningManager.getInstance().getOwner(root);
            if (owner == null) {
                unversionedFiles.add(root);
            } else {
                projectOwners.add(owner);
            }
        }
        if(projectOwners.isEmpty()) {
            // all roots are unversioned -> keep them
        } else if(projectOwners.size() == 1) {
            // context contais one owner -> remove unversioned files
            for (VCSFileProxy unversionedFile : unversionedFiles) {
                for (Iterator<VCSFileProxy> i = rootFileExclusions.iterator(); i.hasNext(); ) {
                    VCSFileProxy exclusion = i.next();
                    if (Utils.isAncestorOrEqual(unversionedFile, exclusion)) {
                        i.remove();
                    }
                }
            }
            rootFiles.removeAll(unversionedFiles);
        } else {
            // more than one owner -> return empty context
            rootFileExclusions.clear();
            rootFiles.clear();
        }

        VCSContext ctx = new VCSContext(nodes, rootFiles, rootFileExclusions);
        contextCached = new WeakReference<VCSContext>(ctx);
        contextNodesCached = new WeakReference<Node []>(nodes);
        return ctx;
    }
        
    /**
     * Returns the smallest possible set of all files that lie under Root files and are NOT 
     * under some Excluded file. 
     * Technically, for every file in the returned set all of the following is true:
     * 
     * - the file itself or at least one of its ancestors is a root file/folder
     * - neither the file itself nor any of its ancestors is an exluded file/folder
     * - the file passed through the supplied FileFilter
     *  
     * @param filter custom file filter
     * @return filtered set of files that must pass through the filter
     */
    public synchronized Set<VCSFileProxy> computeFiles(FileFilter filter) {
        if (computedFilesCached == null || filter != fileFilterCached) {
            computedFilesCached = substract(rootFiles, exclusions, filter);
            fileFilterCached = filter;
        }
        return computedFilesCached;
    }
    
    /**
     * Retrieves elements that make up this VCS context. The returned lookup may be empty
     * or may contain any number of the following elements:
     * - instances of Node that were originally used to construct this context object
     *
     * @return Lookup lookup of this VCSContext
     */ 
    public Lookup getElements() {
        return elements;
    }

    /**
     * Retrieves set of files/folders that represent this context.
     * This set contains all files the user selected, unfiltered.
     * For example, if the user selects two elements: folder /var and file /var/Foo.java then getFiles() 
     * returns both of them and getRootFiles returns only the folder /var. 
     * This method is suitable for versioning systems that DO manage folders, such as Clearcase. 
     * 
     * @return Set<FileProxy> set of Files this context represents
     * @see #getRootFiles() 
     * @since 1.6
     */ 
    public Set<VCSFileProxy> getFiles() {
        return unfilteredRootFiles;
    }

    /**
     * Retrieves set of root files/folders that represent this context.
     * This set only contains context roots, not files/folders that are contained within these roots.
     * For example, if the user selects two elements: folder /var and file /var/Foo.java then getFiles() 
     * returns both of them and getRootFiles returns only the folder /var. 
     * This method is suitable for versioning systems that do not manage folders, such as CVS. 
     * 
     * @return Set<FileProxy> set of Files this context represents
     * @see #getFiles() 
     */ 
    public Set<VCSFileProxy> getRootFiles() {
        return rootFiles;
    }

    /**
     * Retrieves set of files/folders that are excluded from this context. Exclusions are files or folders that
     * are descendants of a root folder and should NOT be a part of a versioning operation. For example, an CVS/Update command
     * run on a project that contains a subproject should not touch any files in the subproject. Therefore the VCSContext for
     * the action would contain one root file (the project's root) and one exclusion (subproject root).
     * 
     * @return Set<FileProxy> set of files and folders that are not part of (are excluded from) this context. 
     * All their descendands are excluded too.
     */ 
    public Set<VCSFileProxy> getExclusions() {
        return exclusions;
    }

    /**
     * Determines whether the supplied File is contained in this context. In other words, the file must be either a root file/folder
     * or be a descendant of a root folder and also must NOT be an excluded file/folder or be a descendant of an excluded folder. 
     * 
     * @param file a File to test
     * @return true if this context contains the supplied file, false otherwise 
     */ 
    public boolean contains(VCSFileProxy file) {
        outter : for (VCSFileProxy root : rootFiles) {
            if (Utils.isAncestorOrEqual(root, file)) {
                for (VCSFileProxy excluded : exclusions) {
                    if (Utils.isAncestorOrEqual(excluded, file)) {
                        continue outter;
                    }
                }
                return true;
            }
        }
        return false;
    }
        
    private static void addProjectFiles(Collection<VCSFileProxy> rootFiles, Collection<VCSFileProxy> rootFilesExclusions, Project project) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        for (int j = 0; j < sourceGroups.length; j++) {
            SourceGroup sourceGroup = sourceGroups[j];
            FileObject srcRootFo = sourceGroup.getRootFolder();
            VCSFileProxy rootFile = VCSFileProxy.createFileProxy(srcRootFo);
            if (rootFile == null) continue;
            if (!srcRootFo.isValid()) {
                LOG.log(Level.WARNING, "addProjectFiles: invalid source root {0}", srcRootFo); //NOI18N
                continue;
            }
            rootFiles.add(rootFile);
            FileObject [] rootChildren = srcRootFo.getChildren();
            for (int i = 0; i < rootChildren.length; i++) {
                FileObject rootChildFo = rootChildren[i];
                VCSFileProxy child = VCSFileProxy.createFileProxy(rootChildFo);
                // TODO: #60516 deep scan is required here but not performed due to performace reasons
                try {
                    if (!srcRootFo.isValid()) {
                        LOG.log(Level.WARNING, "addProjectFiles: source root {0} changed from valid to invalid", srcRootFo); //NOI18N
                        break;
                    }
                    // XXX needed io.File for sharability
                    if (child != null && rootChildFo.isValid() && !sourceGroup.contains(rootChildFo) && SharabilityQuery.getSharability(child.toFile()) != SharabilityQuery.NOT_SHARABLE) {
                        rootFilesExclusions.add(child);
                    }
                } catch (IllegalArgumentException ex) {
                    // #161904
                    Logger logger = LOG;
                    logger.log(Level.WARNING, "addProjectFiles: IAE");
                    logger.log(Level.WARNING, "rootFO: " + srcRootFo);
                    if (srcRootFo != sourceGroup.getRootFolder()) {
                        logger.log(Level.WARNING, "root FO has changed");
                    }
                    String children = "[";
                    for (FileObject fo : rootChildren) {
                        children += "\"" + fo.getPath() + "\", ";
                    }
                    children += "]";
                    logger.log(Level.WARNING, "srcRootFo.getChildren(): " + children);
                    if (!rootChildFo.isValid()) {
                        logger.log(Level.WARNING, rootChildFo + " does not exist ");
                    }
                    if (!FileUtil.isParentOf(srcRootFo, rootChildFo)) {
                        logger.log(Level.WARNING, rootChildFo + " is not under " + srcRootFo);
                    }
                    logger.log(Level.WARNING, null, ex);
                }
            }
        }
    }
    
    private static void addFileObjects(Node node, Set<VCSFileProxy> rootFiles) {
        Collection<? extends NonRecursiveFolder> folders = node.getLookup().lookup(new Lookup.Template<NonRecursiveFolder>(NonRecursiveFolder.class)).allInstances();
        List<VCSFileProxy> nodeFiles = new ArrayList<VCSFileProxy>();
        if (folders.size() > 0) {
            for (Iterator j = folders.iterator(); j.hasNext();) {
                NonRecursiveFolder nonRecursiveFolder = (NonRecursiveFolder) j.next();
                VCSFileProxy file = Utils.createFlatFileProxy(nonRecursiveFolder.getFolder());
                nodeFiles.add(file);
            }
        } else {
            Collection<? extends FileObject> fileObjects = node.getLookup().lookup(new Lookup.Template<FileObject>(FileObject.class)).allInstances();
            if (fileObjects.size() > 0) {
                nodeFiles.addAll(toFileCollection(fileObjects));
            } else {
                DataObject dataObject = node.getCookie(DataObject.class);
                if (dataObject instanceof DataShadow) {
                    dataObject = ((DataShadow) dataObject).getOriginal();
                }
                if (dataObject != null) {
                    Collection<VCSFileProxy> doFiles = toFileCollection(dataObject.files());
                    nodeFiles.addAll(doFiles);
                }
            }
        }
        rootFiles.addAll(nodeFiles);
    }
    
    private static Collection<VCSFileProxy> toFileCollection(Collection<? extends FileObject> fileObjects) {
        Set<VCSFileProxy> files = new HashSet<VCSFileProxy>(fileObjects.size()*4/3+1);
        for (FileObject fo : fileObjects) {
            files.add(VCSFileProxy.createFileProxy(fo));
        }
        files.remove(null);
        return files;
    }    

    private VCSContext(Set<VCSFileProxy> rootFiles, Set<VCSFileProxy> exclusions, Object... elements) {
        Set<VCSFileProxy> tempRootFiles = new HashSet<VCSFileProxy>(rootFiles);
        Set<VCSFileProxy> tempExclusions = new HashSet<VCSFileProxy>(exclusions);
        this.unfilteredRootFiles = Collections.unmodifiableSet(new HashSet<VCSFileProxy>(tempRootFiles));
        // exclusions that are also root files should be removed
        tempExclusions.removeAll(tempRootFiles);
        while (normalize(tempRootFiles, tempExclusions));
        this.rootFiles = Collections.unmodifiableSet(tempRootFiles);
        this.exclusions = Collections.unmodifiableSet(tempExclusions);
        this.elements = Lookups.fixed(elements);
    }

    private VCSContext(Node [] nodes, Set<VCSFileProxy> rootFiles, Set<VCSFileProxy> exclusions) {
        this(rootFiles, exclusions, nodes != null ? (Object[]) nodes : new Node[0]);
    }

    private VCSContext(Set<? extends FileObject> elements, Set<VCSFileProxy> rootFiles, Set<VCSFileProxy> exclusions) {
        this(rootFiles, exclusions, elements != null ? elements : Collections.EMPTY_SET);
    }

    private boolean normalize(Set<VCSFileProxy> rootFiles, Set<VCSFileProxy> exclusions) {
        for (Iterator<VCSFileProxy> i = rootFiles.iterator(); i.hasNext();) {
            VCSFileProxy root = i.next();
            for (Iterator<VCSFileProxy> j = exclusions.iterator(); j.hasNext();) {
                VCSFileProxy exclusion = j.next();
                if (Utils.isAncestorOrEqual(exclusion, root)) {
                    j.remove();
                    exclusionRemoved(exclusions, exclusion, root);
                    return true;
                }
            }
        }
        removeDuplicates(rootFiles);
        removeDuplicates(exclusions);
        return false;
    }
    
    private void exclusionRemoved(Set<VCSFileProxy> exclusions, VCSFileProxy exclusion, VCSFileProxy root) {
        VCSFileProxy [] exclusionChildren = exclusion.listFiles();
        if (exclusionChildren == null) return;
        for (int i = 0; i < exclusionChildren.length; i++) {
            VCSFileProxy child = exclusionChildren[i];
            if (!Utils.isAncestorOrEqual(root, child)) {
                exclusions.add(child);
            }
        }
    }

    private static Set<VCSFileProxy> substract(Set<VCSFileProxy> roots, Set<VCSFileProxy> exclusions, FileFilter filter) {
        Set<VCSFileProxy> files = new HashSet<VCSFileProxy>(roots);
        Set<VCSFileProxy> checkedFiles = new HashSet<VCSFileProxy>();
        for (VCSFileProxy exclusion : exclusions) {
            assert exclusion != null;
            for (;;) {
                VCSFileProxy parent = exclusion.getParentFile();
                /**
                 * only if the parent has not been checked yet - #158221
                 * otherwise skip adding of the siblings - they have been already added
                 */
                if (!checkedFiles.contains(exclusion.getParentFile())) {
                    addSiblings(files, exclusion, filter);
                    checkedFiles.add(parent);
                }
                exclusion = parent;
                files.remove(exclusion);
                if (roots.contains(exclusion)) break;
            }
        }
        files.removeAll(exclusions);
        return files;
    }

    private static void addSiblings(Set<VCSFileProxy> files, VCSFileProxy exclusion, FileFilter filter) {
        if (exclusion.getParentFile() == null) return;  // roots have no siblings
        VCSFileProxy [] siblings = exclusion.getParentFile().listFiles();
        for (VCSFileProxy sibling : siblings) {
            if (filter.accept(sibling)) files.add(sibling);
        }
        files.remove(exclusion);
    }

    private static Set<VCSFileProxy> emptySet() {
        return Collections.emptySet();
    }

    private void removeDuplicates(Set<VCSFileProxy> files) {
        List<VCSFileProxy> newFiles = new ArrayList<VCSFileProxy>();
        outter: for (Iterator<VCSFileProxy> i = files.iterator(); i.hasNext();) {
            VCSFileProxy file = i.next();
            for (Iterator<VCSFileProxy> j = newFiles.iterator(); j.hasNext();) {
                VCSFileProxy includedFile = j.next();
                if (Utils.isAncestorOrEqual(includedFile, file) && (file.isFile() || !includedFile.isFlat())) continue outter;
                if (Utils.isAncestorOrEqual(file, includedFile) && (includedFile.isFile() || !file.isFlat())) {
                    j.remove();
                }
            }
            newFiles.add(file);
        }
        files.clear();
        files.addAll(newFiles);
    }
}
