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
package org.netbeans.modules.versioning.masterfs;

import org.openide.filesystems.*;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor;

/**
 * Plugs into IDE filesystem and delegates file operations to registered versioning systems.
 *
 * @author Maros Sandor
 */
class FilesystemInterceptor extends ProvidedExtensions implements FileChangeListener, FileStatusListener {
    private final VersioningAnnotationProvider vap;
    
    public FilesystemInterceptor(VersioningAnnotationProvider vap) {
        super(true);
        this.vap = vap;
        VCSFilesystemInterceptor.registerFileStatusListener(this);
        getRootFilesystem().addFileChangeListener(this);
    }
    

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.versioning.FilesystemInterceptor");

    // === LIFECYCLE =======================================================================================

    static FileSystem getRootFilesystem() {
        try {
            String userDir = System.getProperty("netbeans.user"); // NOI18N
            FileObject fo = FileUtil.toFileObject(new File(userDir));
            return fo.getFileSystem();
        } catch (FileStateInvalidException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    /**
     * Unregisters listeners from all disk filesystems.
     */
    void shutdown() {
        getRootFilesystem().removeFileChangeListener(this);
    }    

    // ==================================================================================================
    // QUERIES
    // ==================================================================================================

    /**
     * Provide a way for VCS systems to eventually override the fact that a file 
     * is read-only - e.g. when a file is VCS locked and we still want be able to edit and 
     * do a silent unlock in the background.
     * The contract with masterfs is that the proper value is determined no matter 
     * if the file is managed by a VCS or not.
     * 
     * @param file
     * @return 
     */
    @Override
    public boolean canWrite(File file) {
        if(file.canWrite()) {
            // In case it isn't read-only we can directly return and avoid 
            // potential (and unnecessary) io caused in VCS by determining the files ownership.
            return true;
        }
        if (!file.exists()) {
            // In case it doesn't even exist we can directly return and avoid 
            // potential (and unnecessary) io caused in VCS by determining the files ownership.
            return false;
        }        
        return VCSFilesystemInterceptor.canWriteReadonlyFile(VCSFileProxy.createFileProxy(file));
    }

    @Override
    public Object getAttribute(File file, String attrName) {
        return VCSFilesystemInterceptor.getAttribute(VCSFileProxy.createFileProxy(file), attrName);
    }

    // ==================================================================================================
    // CHANGE
    // ==================================================================================================

    @Override
    public void fileChanged(FileEvent fe) {
        VCSFilesystemInterceptor.fileChanged(VCSFileProxy.createFileProxy(fe.getFile()));
    }

    @Override
    public void beforeChange(FileObject fo) {
        VCSFilesystemInterceptor.beforeChange(VCSFileProxy.createFileProxy(fo));
    }

    @Override
    public long refreshRecursively(File dir, long lastTimeStamp, List<? super File> children) {
        List<VCSFileProxy> arr = new ArrayList<VCSFileProxy>();
        long ret = VCSFilesystemInterceptor.listFiles(VCSFileProxy.createFileProxy(dir), lastTimeStamp, arr);
        for (VCSFileProxy p : arr) {
            children.add(p.toFile());
        }
        assert !children.contains(null);
        return ret;
    }

    // ==================================================================================================
    // DELETE
    // ==================================================================================================


    @Override
    public DeleteHandler getDeleteHandler(final File file) {
        final VCSFileProxy p = VCSFileProxy.createFileProxy(file);
        final VCSFilesystemInterceptor.IOHandler h = VCSFilesystemInterceptor.getDeleteHandler(p);
        if (h == null) {
            return null;
        }
        return new DeleteHandler() {

            @Override
            public boolean delete(File dummy) {
//                assert dummy.equals(file);
                try {
                    h.handle();
                    return true;
                } catch (IOException ex) {
                    LOG.log(Level.INFO, null, ex);
                    return false;
                }
            }
            
        };
    }

    @Override
    public void deleteSuccess(FileObject fo) {
        VCSFilesystemInterceptor.deleteSuccess(VCSFileProxy.createFileProxy(fo));
    }

    @Override
    public void deletedExternally(FileObject fo) {
        VCSFilesystemInterceptor.deletedExternally(VCSFileProxy.createFileProxy(fo));
    }
    
    @Override
    public void fileDeleted(FileEvent fe) { }

    // ==================================================================================================
    // CREATE
    // ==================================================================================================


    @Override
    public void beforeCreate(FileObject parent, String name, boolean isFolder) {
        VCSFilesystemInterceptor.beforeCreate(VCSFileProxy.createFileProxy(parent), name, isFolder);
    }

    @Override
    public void createFailure(FileObject parent, String name, boolean isFolder) {
        VCSFilesystemInterceptor.createFailure(VCSFileProxy.createFileProxy(parent), name, isFolder);
    }

    @Override
    public void createSuccess(FileObject fo) {
        VCSFilesystemInterceptor.createSuccess(VCSFileProxy.createFileProxy(fo));
    }

    @Override
    public void createdExternally(FileObject fo) {
        VCSFilesystemInterceptor.createdExternally(VCSFileProxy.createFileProxy(fo));
    }
    
    @Override
    public void fileDataCreated(FileEvent fe) { }

    @Override
    public void fileFolderCreated(FileEvent fe) { }
    
    // ==================================================================================================
    // MOVE
    // ==================================================================================================

    @Override
    public IOHandler getMoveHandler(File from, File to) {
        return wrap(VCSFilesystemInterceptor.getMoveHandler(
            VCSFileProxy.createFileProxy(from), VCSFileProxy.createFileProxy(to)
        ));
    }

    @Override
    public IOHandler getRenameHandler(File from, String newName) {
        return wrap(VCSFilesystemInterceptor.getRenameHandler(
            VCSFileProxy.createFileProxy(from), newName
        ));
    }

    @Override
    public void moveSuccess(FileObject from, File to) {
        VCSFilesystemInterceptor.afterMove(
            VCSFileProxy.createFileProxy(from), VCSFileProxy.createFileProxy(to)
        );
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        VCSFilesystemInterceptor.afterMove(
            VCSFileProxy.createFileProxy(
                VCSFileProxy.createFileProxy(fe.getFile()).getParentFile(), 
                fe.getName() + '.' + fe.getExt()
            ),
            VCSFileProxy.createFileProxy(fe.getFile())
        );
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
        return wrap(VCSFilesystemInterceptor.getCopyHandler(
            VCSFileProxy.createFileProxy(from), VCSFileProxy.createFileProxy(to)
        ));
    }

    @Override
    public void beforeCopy(FileObject from, File to) {
        VCSFilesystemInterceptor.beforeCopy(
            VCSFileProxy.createFileProxy(from), VCSFileProxy.createFileProxy(to)
        );
    }

    @Override
    public void copySuccess(FileObject from, File to) {
        VCSFilesystemInterceptor.copySuccess(
            VCSFileProxy.createFileProxy(from), VCSFileProxy.createFileProxy(to)
        );
    }

    @Override
    public void copyFailure(FileObject from, File to) {
    }

    /**
     * There is a contract that says that when a file is locked, it is expected to be changed. This is what openide/text
     * does when it creates a Document. A versioning system is expected to make the file r/w.
     *
     * @param fo a FileObject
     */
    @Override
    public void fileLocked(FileObject fo) {
        VCSFilesystemInterceptor.fileLocked(VCSFileProxy.createFileProxy(fo));
    }
    
    @Override
    public void annotationChanged(FileStatusEvent ev) {
        vap.deliverStatusEvent(ev);
    }

    private IOHandler wrap(final VCSFilesystemInterceptor.IOHandler io) {
        if (io == null) {
            return null;
        }
        return new IOHandler() {
            @Override
            public void handle() throws IOException {
                io.handle();
            }
        };
    }
}
