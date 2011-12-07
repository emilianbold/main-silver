/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remotefs.versioning.spi;

import org.netbeans.api.extexecution.ProcessBuilder;
import org.netbeans.modules.remote.impl.fileoperations.FileOperationsProvider;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.filesystems.VCSFileProxyOperations;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=FileOperationsProvider.class, position = 1000)
public class FileProxyProviderImpl extends FileOperationsProvider {
    
    @Override
    public FileOperations getFileOperations(FileSystem fs) {
        return new FileOperationsImpl(fs);
    }
    
    private static final class FileOperationsImpl extends FileOperations implements VCSFileProxyOperations {
        protected FileOperationsImpl(FileSystem fs) {
            super(fs);
        }

        @Override
        public String getName(VCSFileProxy file) {
            return getName(toFileProxy(file));
        }

        @Override
        public boolean isDirectory(VCSFileProxy file) {
            return isDirectory(toFileProxy(file));
        }

        @Override
        public boolean isFile(VCSFileProxy file) {
            return isFile(toFileProxy(file));
        }

        @Override
        public boolean canWrite(VCSFileProxy file) {
            return canWrite(toFileProxy(file));
        }

        @Override
        public VCSFileProxy getParentFile(VCSFileProxy file) {
            String parent = getDir(toFileProxy(file));
            if (parent == null) {
                return null;
            }
            FileObject root = getRoot();
            VCSFileProxy res = VCSFileProxy.createFileProxy(root);
            String[] split = parent.split("/"); // NOI18N
            for (int i = 0; i < split.length; i++) {
                res = VCSFileProxy.createFileProxy(res, split[i]);
            }
            return res;
        }


        @Override
        public String getAbsolutePath(VCSFileProxy file) {
            return file.getPath();
        }

        @Override
        public boolean exists(VCSFileProxy file) {
            return exists(toFileProxy(file));
        }

        @Override
        public VCSFileProxy normalize(VCSFileProxy file) {
            String path = normalizeUnixPath(toFileProxy(file));
            if (file.getPath().equals(path)) {
                return file;
            }
            FileObject root = getRoot();
            VCSFileProxy res = VCSFileProxy.createFileProxy(root);
            String[] split = path.split("/"); // NOI18N
            for (int i = 0; i < split.length; i++) {
                res = VCSFileProxy.createFileProxy(res, split[i]);
            }
            return res;
        }

        @Override
        public FileObject toFileObject(VCSFileProxy path) {
            return toFileObject(toFileProxy(path));
        }

        @Override
        public VCSFileProxy[] list(VCSFileProxy path) {
            String[] list = list(toFileProxy(path));
            if (list == null) {
                return null;
            }
            VCSFileProxy[] res = new VCSFileProxy[list.length];
            for(int i = 0; i < list.length; i++) {
                res[i] = VCSFileProxy.createFileProxy(path, list[i]);
            }
            return res;
        }

        @Override
        public boolean isFlat(VCSFileProxy path) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ProcessBuilder createProcessBuilder(VCSFileProxy file) {
            return createProcessBuilder(toFileProxy(file));
        }
    }

    private static FileProxyO toFileProxy(final VCSFileProxy file) {
        return new FileProxyO() {

            @Override
            public String getPath() {
                return file.getPath();
            }

            @Override
            public String toString() {
                return file.getPath();
            }
        };
    }
}
