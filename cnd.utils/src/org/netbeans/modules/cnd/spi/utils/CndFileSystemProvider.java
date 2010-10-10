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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.spi.utils;

import java.io.File;
import java.util.Collection;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.support.InvalidFileObjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Vladimir Kvashin
 */
public abstract class CndFileSystemProvider {

    private static final DefaultProvider DEFAULT = new DefaultProvider();

    private static DefaultProvider getDefault() {
        return DEFAULT;
    }

    public static String getCaseInsensitivePath(CharSequence path) {
        return getDefault().getCaseInsensitivePathImpl(path);
    }

    public static CharSequence lowerPathCaseIfNeeded(CharSequence path) {
        return getDefault().lowerPathCaseIfNeededImpl(path);
    }

    public static File toFile(FileObject fileObject) {
        return getDefault().toFileImpl(fileObject);
    }

    public static FileObject toFileObject(CharSequence path) {
        FileObject result = getDefault().toFileObjectImpl(path);
        CndUtils.assertNotNull(result, "Null file object"); //NOI18N
        return result;
    }

    public static FileObject toFileObject(File file) {
        FileObject result = getDefault().toFileObjectImpl(file);
        CndUtils.assertNotNull(result, "Null file object"); //NOI18N
        return result;
    }

    public static CharSequence toPath(FileObject fileObject) {
        CharSequence result = getDefault().toPathImpl(fileObject);
        CndUtils.assertNotNull(result, "Null file object unique string"); //NOI18N
        return result;
    }

    protected abstract FileObject toFileObjectImpl(CharSequence path);
    protected abstract File toFileImpl(FileObject fileObject);
    protected abstract FileObject toFileObjectImpl(File file);
    protected abstract CharSequence toPathImpl(FileObject fileObject);

    protected abstract String getCaseInsensitivePathImpl(CharSequence path);
    protected abstract boolean isMine(CharSequence path);

    private static class DefaultProvider extends CndFileSystemProvider {

        private CndFileSystemProvider[] cache;
        private FileSystem fileFileSystem;

        DefaultProvider() {
            Collection<? extends CndFileSystemProvider> instances =
                    Lookup.getDefault().lookupAll(CndFileSystemProvider.class);
            cache = instances.toArray(new CndFileSystemProvider[instances.size()]);
        }

        private synchronized FileSystem getFileFileSystem() {
            if (fileFileSystem == null) {
                File tmpDirFile = new File(System.getProperty("java.io.tmpdir"));
                tmpDirFile = CndFileUtils.normalizeFile(tmpDirFile);
                FileObject tmpDirFo = FileUtil.toFileObject(tmpDirFile); // File SIC!  //NOI18N
                if (tmpDirFo != null) {
                    try {
                        fileFileSystem = tmpDirFo.getFileSystem();
                    } catch (FileStateInvalidException ex) {
                        // it's no use to log it here
                    }
                }
                if (fileFileSystem == null) {
                    fileFileSystem = InvalidFileObjectSupport.getDummyFileSystem();
                }
            }
            return fileFileSystem;
        }

        @Override
        protected File toFileImpl(FileObject fileObject) {
            File file;
            for (CndFileSystemProvider provider : cache) {
                file = provider.toFileImpl(fileObject);
                if (file != null) {
                    return file;
                }
            }
            file = FileUtil.toFile(fileObject);
            if (file == null && fileObject != null && !fileObject.isValid()) {
                file = new File(fileObject.getPath());
            }
            return file;
        }

        @Override
        public FileObject toFileObjectImpl(CharSequence path) {
            FileObject  fo;
            for (CndFileSystemProvider provider : cache) {
                fo = provider.toFileObjectImpl(path);
                if (fo != null) {
                    return fo;
                }
            }
            File file = new File(path.toString());
            fo = FileUtil.toFileObject(file);
            if (fo == null) {
                fo = InvalidFileObjectSupport.getInvalidFileObject(getFileFileSystem(), file.getAbsolutePath());
            }
            return fo;
        }

        @Override
        public FileObject toFileObjectImpl(File file) {
            FileObject fo;
            for (CndFileSystemProvider provider : cache) {
                fo = provider.toFileObjectImpl(file);
                if (fo != null) {
                    return fo;
                }
            }
            fo = FileUtil.toFileObject(file);
            if (fo == null) {
                fo = InvalidFileObjectSupport.getInvalidFileObject(getFileFileSystem(), file.getAbsolutePath());
            }
            return fo;
        }

        @Override
        protected CharSequence toPathImpl(FileObject fileObject) {
            for (CndFileSystemProvider provider : cache) {
                CharSequence path = provider.toPathImpl(fileObject);
                if (path != null) {
                    return path;
                }
            }
            return fileObject.getPath();
        }

        @Override
        public String getCaseInsensitivePathImpl(CharSequence path) {
            for (CndFileSystemProvider provider : cache) {
                String data = provider.getCaseInsensitivePathImpl(path);
                if (data != null) {
                    return data;
                }
            }
            return path.toString();
        }

        protected CharSequence lowerPathCaseIfNeededImpl(CharSequence path) {
            if (CndFileUtils.isSystemCaseSensitive()) {
                return path;
            } else {
                for (CndFileSystemProvider provider : cache) {
                    if (provider.isMine(path)) {
                        return path;
                    }
                }
                return path.toString().toLowerCase();
            }
        }

        @Override
        protected boolean isMine(CharSequence path) {
            for (CndFileSystemProvider provider : cache) {
                if (provider.isMine(path)) {
                    return true;
                }
            }
            return false;
        }
    }
}
