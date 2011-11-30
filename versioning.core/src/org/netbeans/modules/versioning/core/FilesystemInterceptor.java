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
package org.netbeans.modules.versioning.core;

import org.openide.filesystems.*;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSInterceptor;

/**
 * Plugs into IDE filesystem and delegates file operations to registered versioning systems.
 *
 * @author Maros Sandor
 */
class FilesystemInterceptor extends ProvidedExtensions implements FileChangeListener {

    public FilesystemInterceptor(boolean providesCanWrite) {
        super(providesCanWrite);
    }
    
    /**
     * A versioned files remote repository or origin.
     */
    private static final String ATTRIBUTE_REMOTE_LOCATION = "ProvidedExtensions.RemoteLocation";

    /**
     * A Runnable to refresh the file given in {@link #getAttribute()}.
     */
    private static final String ATTRIBUTE_REFRESH = "ProvidedExtensions.Refresh";

    /**
     * A o.n.m.versioning.util.SearchHistorySupport instance
     */
    private static final String ATTRIBUTE_SEARCH_HISTORY = "ProvidedExtensions.SearchHistorySupport";

    /**
     * Determines if a file is versioned or not
     */
    private static final String ATTRIBUTE_VCS_MANAGED = "ProvidedExtensions.VCSManaged";

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.versioning.FilesystemInterceptor");

    private VersioningManager master;

    // === LIFECYCLE =======================================================================================

    /**
     * Initializes the interceptor by registering it into master filesystem.
     * Registers listeners to all disk filesystems.
     * @param versioningManager
     */
    void init(VersioningManager versioningManager) {
        assert master == null;
        master = versioningManager;
        FileSystem fileSystem = Utils.getRootFilesystem();
        fileSystem.addFileChangeListener(this);
    }

    /**
     * Unregisters listeners from all disk filesystems.
     */
    void shutdown() {
        FileSystem fileSystem = Utils.getRootFilesystem();
        fileSystem.removeFileChangeListener(this);
    }    

    // ==================================================================================================
    // QUERIES
    // ==================================================================================================

    @Override
    public boolean canWrite(File file) {
        LOG.log(Level.FINE, "canWrite {0}", file);
        if (Utils.canWrite(VCSFileProxy.createFileProxy(file))) {
            return true;
        }
        if (!Utils.exists(VCSFileProxy.createFileProxy(file))) {
            return false;
        }
        // can be optimized by taking out local history from the search
        return getInterceptor(VCSFileProxy.createFileProxy(file), false, "isMutable").isMutable(VCSFileProxy.createFileProxy(file));        // NOI18N
    }

    @Override
    public Object getAttribute(File file, String attrName) {
        // LOG.log(Level.FINE, "getAttribute {0}, {1}", new Object[] {file, attrName});

        if(ATTRIBUTE_REMOTE_LOCATION.equals(attrName) ||           
           ATTRIBUTE_REFRESH.equals(attrName) ||
           ATTRIBUTE_SEARCH_HISTORY.equals(attrName))
        {
            return getInterceptor(VCSFileProxy.createFileProxy(file), file.isDirectory(), "getAttribute").getAttribute(attrName); // NOI18N
        } else if (ATTRIBUTE_VCS_MANAGED.equals(attrName)) {
            return master.getOwner(VCSFileProxy.createFileProxy(file)) != null;
        } else {
            return null;
        }
    }

    // ==================================================================================================
    // CHANGE
    // ==================================================================================================

    @Override
    public void fileChanged(FileEvent fe) {
        LOG.log(Level.FINE, "fileChanged {0}", fe.getFile());
        removeFromDeletedFiles(fe.getFile());
        getInterceptor(fe, "afterChange").afterChange();
    }

    @Override
    public void beforeChange(FileObject fo) {
        File file = FileUtil.toFile(fo);
        LOG.log(Level.FINE, "beforeChange {0}", file);
        getInterceptor(VCSFileProxy.createFileProxy(file), fo.isFolder(), "beforeChange").beforeChange(); // NOI18N
    }

    @Override
    public long refreshRecursively(File dir, long lastTimeStamp, List<? super File> children) {
        LOG.log(Level.FINE, "refreshRecursively {0}, {1}", new Object[]{dir, lastTimeStamp});
        if(LOG.isLoggable(Level.FINER)) {
            for (Object f : children) {
                LOG.log(Level.FINE, "  refreshRecursively child {1}", f);
            }
        }
        DelegatingInterceptor interceptor = getRefreshInterceptor(VCSFileProxy.createFileProxy(dir));
        return interceptor.refreshRecursively(dir, lastTimeStamp, children);
    }

    private boolean needsLH(String... methodNames) {
        for (String methodName : methodNames) {
            if(master.needsLocalHistory(methodName)) {
                return true;
            }
        }
        return false;
    }

    // ==================================================================================================
    // DELETE
    // ==================================================================================================

    private void removeFromDeletedFiles(File file) {
        synchronized(deletedFiles) {
            deletedFiles.remove(file);
        }
    }

    private void removeFromDeletedFiles(FileObject fo) {
        synchronized(deletedFiles) {
            if (deletedFiles.size() > 0) {
                deletedFiles.remove(FileUtil.toFile(fo));
            }
        }
    }

    @Override
    public DeleteHandler getDeleteHandler(File file) {
        LOG.log(Level.FINE, "getDeleteHandler {0}", file);
        removeFromDeletedFiles(file);
        DelegatingInterceptor dic = getInterceptor(VCSFileProxy.createFileProxy(file), (Boolean) null, "beforeDelete", "doDelete"); // NOI18N
        return dic.beforeDelete() ? dic : null;
    }

    @Override
    public void deleteSuccess(FileObject fo) {
        fileDeleted(fo);
    }

    @Override
    public void deletedExternally(FileObject fo) {
        fileDeleted(fo);
    }
    
    @Override
    public void fileDeleted(FileEvent fe) { }

    private void fileDeleted(FileObject fo) {
        FileEvent fe = new FileEvent(fo);
        LOG.log(Level.FINE, "fileDeleted {0}", fe.getFile());
        removeFromDeletedFiles(fe.getFile());
        getInterceptor(fe, "afterDelete").afterDelete(); // NOI18N
    }

    // ==================================================================================================
    // CREATE
    // ==================================================================================================

    /**
     * Stores files that are being created inside the IDE and the owner interceptor wants to handle the creation. Entries
     * are added in beforeCreate() and removed in fileDataCreated() or createFailure().
     */
    private final Map<FileEx, DelegatingInterceptor> filesBeingCreated = new HashMap<FileEx, DelegatingInterceptor>(10);

    @Override
    public void beforeCreate(FileObject parent, String name, boolean isFolder) {
        LOG.log(Level.FINE, "beforeCreate {0}, {1}, {2} ", new Object[] {parent, name, isFolder});
        File file = FileUtil.toFile(parent);
        if (file == null) return;
        file = new File(file, name);
        DelegatingInterceptor dic = getInterceptor(VCSFileProxy.createFileProxy(file), isFolder, "beforeCreate"); // NOI18N
        if (dic.beforeCreate()) {
            filesBeingCreated.put(new FileEx(parent, name, isFolder), dic);
        }
    }

    @Override
    public void createFailure(FileObject parent, String name, boolean isFolder) {
        LOG.log(Level.FINE, "createFailure {0}, {1}, {2} ", new Object[] {parent, name, isFolder});
        filesBeingCreated.remove(new FileEx(parent, name, isFolder));
    }

    @Override
    public void createSuccess(FileObject fo) {
        LOG.log(Level.FINE, "createSuccess {0}", fo);
        fileCreated(new FileEvent(fo));
    }

    @Override
    public void createdExternally(FileObject fo) {
        LOG.log(Level.FINE, "createdExternally {0}", fo);
        fileCreated(new FileEvent(fo));
    }
    
    @Override
    public void fileDataCreated(FileEvent fe) { }

    @Override
    public void fileFolderCreated(FileEvent fe) { }
    
    private void fileCreated(FileEvent fe) {
        FileObject fo = fe.getFile();
        FileEx fileEx = new FileEx(fo.getParent(), fo.getNameExt(), fo.isFolder());
        DelegatingInterceptor interceptor = filesBeingCreated.remove(fileEx);
        if (interceptor != null) {
            try {
                interceptor.doCreate();
            } catch (Exception e) {
                // ignore errors, the file is already created anyway
            }
        }
        removeFromDeletedFiles(fe.getFile());
        if(interceptor == null) {
            interceptor = getInterceptor(fe, "afterCreate");                    // NOI18N
        }
        interceptor.afterCreate();
    }

    // ==================================================================================================
    // MOVE
    // ==================================================================================================

    @Override
    public IOHandler getMoveHandler(File from, File to) {
        LOG.log(Level.FINE, "getMoveHandler {0}, {1}", new Object[] {from, to});
        return getMoveHandlerIntern(from, to);
    }

    @Override
    public IOHandler getRenameHandler(File from, String newName) {
        LOG.log(Level.FINE, "getRenameHandler {0}, {1}", new Object[] {from, newName});
        File to = new File(from.getParentFile(), newName);
        return getMoveHandlerIntern(from, to);
    }

    private IOHandler getMoveHandlerIntern(File from, File to) {
        DelegatingInterceptor dic = getInterceptor(VCSFileProxy.createFileProxy(from), VCSFileProxy.createFileProxy(to), "beforeMove", "doMove"); // NOI18N
        return dic.beforeMove() ? dic.getMoveHandler() : null;
    }

    @Override
    public void moveSuccess(FileObject from, File to) {
        getInterceptor(VCSFileProxy.createFileProxy(from), VCSFileProxy.createFileProxy(to), "afterMove").afterMove();
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        LOG.log(Level.FINE, "fileRenamed {0}", fe.getFile());
        removeFromDeletedFiles(fe.getFile());
        getInterceptor(fe, "afterMove").afterMove();                            // NOI18N
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
         LOG.log(Level.FINE, "fileAttributeChanged {0}", fe.getFile());
        // not interested
    }

    // ==================================================================================================
    // COPY
    // ==================================================================================================

    @Override
    public IOHandler getCopyHandler(File from, File to) {
        LOG.log(Level.FINE, "getCopyHandler {0}, {1}", new Object[] {from, to});
        return getCopyHandlerIntern(from, to);
    }

    private IOHandler getCopyHandlerIntern(File from, File to) {
        DelegatingInterceptor dic = getInterceptor(VCSFileProxy.createFileProxy(from), VCSFileProxy.createFileProxy(to), "beforeCopy", "doCopy"); // NOI18N
        return dic.beforeCopy() ? dic.getCopyHandler() : null;
    }

    @Override
    public void beforeCopy(FileObject from, File to) { }

    @Override
    public void copySuccess(FileObject from, File to) {
        getInterceptor(VCSFileProxy.createFileProxy(from), VCSFileProxy.createFileProxy(to), "afterCopy").afterCopy();
    }

    @Override
    public void copyFailure(FileObject from, File to) { }

    /**
     * There is a contract that says that when a file is locked, it is expected to be changed. This is what openide/text
     * does when it creates a Document. A versioning system is expected to make the file r/w.
     *
     * @param fo a FileObject
     */
    @Override
    public void fileLocked(FileObject fo) {
        LOG.log(Level.FINE, "fileLocked {0}", fo);
        getInterceptor(new FileEvent(fo), "beforeEdit").beforeEdit();           // NOI18N
    }
    
    private DelegatingInterceptor getInterceptor(FileEvent fe, String... forMethods) {
        if (master == null) return nullDelegatingInterceptor;
        FileObject fo = fe.getFile();
        if (fo == null) return nullDelegatingInterceptor;
        VCSFileProxy file = VCSFileProxy.createFileProxy(fo);
        if (file == null) return nullDelegatingInterceptor;
        
        VersioningSystem lh = needsLH(forMethods) ? master.getLocalHistory(file, !fo.isFolder()) : null;
        VersioningSystem vs = master.getOwner(file, !fo.isFolder());

        VCSInterceptor vsInterceptor = vs != null ? vs.getInterceptor() : null;
        VCSInterceptor lhInterceptor = lh != null ? lh.getInterceptor() : null;

        if (vsInterceptor == null && lhInterceptor == null) return nullDelegatingInterceptor;

        if (fe instanceof FileRenameEvent) {
            FileRenameEvent fre = (FileRenameEvent) fe;
            VCSFileProxy parent = file.getParentFile();
            if (parent != null) {
                String name = fre.getName();
                String ext = fre.getExt();
                if (ext != null && ext.length() > 0) {  // NOI18N
                    name += "." + ext;  // NOI18N
                }
                VCSFileProxy from = VCSFileProxy.createFileProxy(parent, name);
                return new DelegatingInterceptor(vsInterceptor, lhInterceptor, from, file, false);
            }
            return nullDelegatingInterceptor;
        } else {
            return new DelegatingInterceptor(vsInterceptor, lhInterceptor, file, null, false);
        }
    }

    private DelegatingInterceptor getInterceptor(VCSFileProxy file, Boolean isDirectory, String... forMethods) {
        if (file == null || master == null) return nullDelegatingInterceptor;

        Boolean isFile = isDirectory != null ? !isDirectory : null;
        isDirectory = isDirectory != null ? isDirectory : false;
        
        VersioningSystem vs = master.getOwner(file, isFile);
        VCSInterceptor vsInterceptor = vs != null ? vs.getInterceptor() : nullInterceptor;

        VersioningSystem lhvs = needsLH(forMethods) ? master.getLocalHistory(file, isFile) : null;
        VCSInterceptor localHistoryInterceptor = lhvs != null ? lhvs.getInterceptor() : nullInterceptor;

        return new DelegatingInterceptor(vsInterceptor, localHistoryInterceptor, file, null, isDirectory);
    }

    private DelegatingInterceptor getInterceptor(VCSFileProxy from, VCSFileProxy to, String... forMethods) {
        if (from == null || to == null) return nullDelegatingInterceptor;

        VersioningSystem vs = master.getOwner(from);
        VCSInterceptor vsInterceptor = vs != null ? vs.getInterceptor() : nullInterceptor;

        VersioningSystem lhvs = needsLH(forMethods) ? master.getLocalHistory(from) : null;
        VCSInterceptor localHistoryInterceptor = lhvs != null ? lhvs.getInterceptor() : nullInterceptor;

        return new DelegatingInterceptor(vsInterceptor, localHistoryInterceptor, from, to, false);
    }

    private DelegatingInterceptor getRefreshInterceptor (VCSFileProxy dir) {
        if (dir == null) return nullDelegatingInterceptor;
        VersioningSystem vs = master.getOwner(dir);
        VCSInterceptor Interceptor = vs != null ? vs.getInterceptor() : nullInterceptor;
        return new DelegatingInterceptor(Interceptor, nullInterceptor, dir, null, true);
    }

    private final DelegatingInterceptor nullDelegatingInterceptor = new DelegatingInterceptor() {
        public boolean beforeDelete() { return false; }
        public void doDelete() throws IOException {  }
        public void afterDelete() { }
        public boolean beforeMove() { return false; }
        public void doMove() throws IOException {  }
        public boolean beforeCreate() { return false; }
        public void doCreate() throws IOException {  }
        public void afterCreate() {  }
        public void beforeChange() {  }
        public void beforeEdit() { }
        public void afterChange() {  }
        public void afterMove() {  }
        public boolean delete(File file) {  throw new UnsupportedOperationException();  }
    };

    private final VCSInterceptor nullInterceptor = new VCSInterceptor() {

        @Override
        public boolean isMutable(VCSFileProxy file) {
            return true;
        }

        @Override
        public Object getAttribute(VCSFileProxy file, String attrName) {
            return null;
        }

        @Override
        public boolean beforeDelete(VCSFileProxy file) {
            return false;
        }

        @Override
        public void doDelete(VCSFileProxy file) throws IOException { }

        @Override
        public void afterDelete(VCSFileProxy file) {}

        @Override
        public boolean beforeMove(VCSFileProxy from, VCSFileProxy to) {
            return false;
        }

        @Override
        public void doMove(VCSFileProxy from, VCSFileProxy to) throws IOException {}

        @Override
        public void afterMove(VCSFileProxy from, VCSFileProxy to) {}

        @Override
        public boolean beforeCopy(VCSFileProxy from, VCSFileProxy to) {
            return false;
        }

        @Override
        public void doCopy(VCSFileProxy from, VCSFileProxy to) throws IOException {}

        @Override
        public void afterCopy(VCSFileProxy from, VCSFileProxy to) {}

        @Override
        public boolean beforeCreate(VCSFileProxy file, boolean isDirectory) {
            return false;
        }

        @Override
        public void doCreate(VCSFileProxy file, boolean isDirectory) throws IOException {}

        @Override
        public void afterCreate(VCSFileProxy file) {}

        @Override
        public void afterChange(VCSFileProxy file) {}

        @Override
        public void beforeChange(VCSFileProxy file) {}

        @Override
        public void beforeEdit(VCSFileProxy file) {}

        @Override
        public long refreshRecursively(VCSFileProxy dir, long lastTimeStamp, List<VCSFileProxy> children) {
            return -1;
        }
    };

    /**
     * Delete interceptor: holds files and folders that we do not want to delete but must pretend that they were deleted.
     */
    private final Set<VCSFileProxy> deletedFiles = new HashSet<VCSFileProxy>(5);

    private class DelegatingInterceptor implements DeleteHandler {

        final Collection<VCSInterceptor> interceptors;
        final VCSInterceptor  interceptor;
        final VCSInterceptor  lhInterceptor;
        final VCSFileProxy            file;
        final VCSFileProxy            to;
        private final boolean isDirectory;
        private IOHandler moveHandler;
        private IOHandler copyHandler;

        private DelegatingInterceptor() {
            this((VCSInterceptor) null, null, null, null, false);
        }

        public DelegatingInterceptor(VCSInterceptor interceptor, VCSInterceptor lhInterceptor, VCSFileProxy file, VCSFileProxy to, boolean isDirectory) {
            this.interceptor = interceptor != null ? interceptor : nullInterceptor;
            this.interceptors = Collections.singleton(this.interceptor);
            this.lhInterceptor = lhInterceptor != null ? lhInterceptor : nullInterceptor;
            this.file = file;
            this.to = to;
            this.isDirectory = isDirectory;
        }

        // TODO: special hotfix for #95243
        public DelegatingInterceptor(Collection<VCSInterceptor> interceptors, VCSInterceptor lhInterceptor, VCSFileProxy file, VCSFileProxy to, boolean isDirectory) {
            this.interceptors = interceptors != null && interceptors.size() > 0 ? interceptors : Collections.singleton(nullInterceptor);
            this.interceptor = this.interceptors.iterator().next();
            this.lhInterceptor = lhInterceptor != null ? lhInterceptor : nullInterceptor;
            this.file = file;
            this.to = to;
            this.isDirectory = isDirectory;
        }

        public boolean isMutable(VCSFileProxy file) {
            return interceptor.isMutable(file);
        }

        private Object getAttribute(String attrName) {
            return interceptor.getAttribute(file, attrName);
        }

        public boolean beforeDelete() {
            lhInterceptor.beforeDelete(file);
            return interceptor.beforeDelete(file);
        }

        public void doDelete() throws IOException {
            lhInterceptor.doDelete(file);
            interceptor.doDelete(file);
        }

        public void afterDelete() {
            lhInterceptor.afterDelete(file);
            interceptor.afterDelete(file);
        }

        public boolean beforeMove() {
            lhInterceptor.beforeMove(file, to);
            return interceptor.beforeMove(file, to);
        }

        public void doMove() throws IOException {
            lhInterceptor.doMove(file, to);
            interceptor.doMove(file, to);
        }

        public void afterMove() {
            lhInterceptor.afterMove(file, to);
            interceptor.afterMove(file, to);
        }

        public boolean beforeCopy() {
            lhInterceptor.beforeCopy(file, to);
            return interceptor.beforeCopy(file, to);
        }

        public void doCopy() throws IOException {
            lhInterceptor.doCopy(file, to);
            interceptor.doCopy(file, to);
        }

        public void afterCopy() {
            lhInterceptor.afterCopy(file, to);
            interceptor.afterCopy(file, to);
        }

        public boolean beforeCreate() {
            lhInterceptor.beforeCreate(file, isDirectory);
            return interceptor.beforeCreate(file, isDirectory);
        }

        public void doCreate() throws IOException {
            lhInterceptor.doCreate(file, isDirectory);
            interceptor.doCreate(file, isDirectory);
        }

        public void afterCreate() {
            lhInterceptor.afterCreate(file);
            interceptor.afterCreate(file);
        }

        public void afterChange() {
            lhInterceptor.afterChange(file);
            interceptor.afterChange(file);
        }

        public void beforeChange() {
            lhInterceptor.beforeChange(file);
            interceptor.beforeChange(file);
        }

        public void beforeEdit() {
            lhInterceptor.beforeEdit(file);
            interceptor.beforeEdit(file);
        }

        private IOHandler getMoveHandler() {
            if(moveHandler == null) {
                moveHandler = new IOHandler() {
                    @Override
        public void handle() throws IOException {
                        doMove();
        }
                };
            }
            return moveHandler;
        }

        private IOHandler getCopyHandler() {
            if(copyHandler == null) {
                copyHandler = new IOHandler() {
                    @Override
                    public void handle() throws IOException {
                        doCopy();
                    }
                };
            }
            return copyHandler;
        }

        /**
         * This must act EXACTLY like java.io.File.delete(). This means:

         * 1.1  if the file is a file and was deleted, return true
         * 1.2  if the file is a file and was NOT deleted because we want to keep it (is part of versioning metadata), also return true
         *      this is done this way to enable bottom-up recursive file deletion
         * 1.3  if the file is a file that should be deleted but the operation failed (the file is locked, for example), return false
         *
         * 2.1  if the file is an empty directory that was deleted, return true
         * 2.2  if the file is a NON-empty directory that was NOT deleted because it contains files that were NOT deleted in step 1.2, return true
         * 2.3  if the file is a NON-empty directory that was NOT deleted because it contains some files that were not previously deleted, return false
         *
         * @param file file or folder to delete
         * @return true if the file was successfully deleted (event virtually deleted), false otherwise
         */
        public boolean delete(File file) {
            File [] children = file.listFiles();
            if (children != null) {
                synchronized(deletedFiles) {
                    for (File child : children) {
                        if (!deletedFiles.contains(child)) return false;
                    }
                }
            }
            try {
                VCSFileProxy proxy = VCSFileProxy.createFileProxy(file);
                lhInterceptor.doDelete(proxy);
                interceptor.doDelete(proxy);
                synchronized(deletedFiles) {
                    if (file.isDirectory()) {
                        // the directory was virtually deleted, we can forget about its children
                        for (Iterator<VCSFileProxy> i = deletedFiles.iterator(); i.hasNext(); ) {
                            VCSFileProxy fakedFile = i.next();
                            if (file.equals(fakedFile.getParentFile())) {
                                i.remove();
                            }
                        }
                    }
                    if (Utils.exists(proxy)) {
                        deletedFiles.add(proxy);
                    } else {
                        deletedFiles.remove(proxy);
                    }
                }
                return true;
            } catch (IOException e) {
                // the interceptor failed to delete the file
                return false;
            }
        }

        public long refreshRecursively (File dir, long lastTimeStamp, List<? super File> children) {
            List<VCSFileProxy> files = new ArrayList<VCSFileProxy>(children.size());
            for (Object file : children) {
                files.add(VCSFileProxy.createFileProxy((File)file));
            }
            return interceptor.refreshRecursively(VCSFileProxy.createFileProxy(dir), lastTimeStamp, files);
        }
    }

    private class FileEx {
        final FileObject  parent;
        final String      name;
        final boolean     isFolder;

        public FileEx(FileObject parent, String name, boolean folder) {
            this.parent = parent;
            this.name = name;
            isFolder = folder;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof FileEx)) return false;
            FileEx fileEx = (FileEx) o;
            return isFolder == fileEx.isFolder && name.equals(fileEx.name) && parent.equals(fileEx.parent);
        }

        public int hashCode() {
            int result = parent.hashCode();
            result = 17 * result + name.hashCode();
            result = 17 * result + (isFolder ? 1 : 0);
            return result;
        }
    }
}
