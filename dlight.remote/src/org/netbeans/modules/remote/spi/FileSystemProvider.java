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

package org.netbeans.modules.remote.spi;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * A temporary solution until we have an official file system provider in thus module
 * @author Andrew Krasny
 * @author Vladimir Kvashin
 */
public final class FileSystemProvider {

    public interface DownloadListener {
        void postConnectDownloadFinished(ExecutionEnvironment env);
    }

    private static final  Collection<? extends FileSystemProviderImplementation> ALL_PROVIDERS =
            Lookup.getDefault().lookupAll(FileSystemProviderImplementation.class);

    private FileSystemProvider() {
    }

    public static FileSystem getFileSystem(ExecutionEnvironment env) {
        return getFileSystem(env, "/"); //NOI18N
    }

    public static ExecutionEnvironment getExecutionEnvironment(FileSystem fileSystem) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                return provider.getExecutionEnvironment(fileSystem);
            }
        }
        return ExecutionEnvironmentFactory.getLocal();
    }

    public static ExecutionEnvironment getExecutionEnvironment(FileObject fileObject) {
        try {
            return getExecutionEnvironment(fileObject.getFileSystem());
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);            
        }
        return ExecutionEnvironmentFactory.getLocal();
    }

    public static FileSystem getFileSystem(ExecutionEnvironment env, String root) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                return provider.getFileSystem(env, root);
            }
        }
        noProvidersWarning(env);
        return null;
    }

    public static boolean waitWrites(ExecutionEnvironment env, List<String> failedFiles) throws InterruptedException {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                return provider.waitWrites(env, failedFiles);
            }
        }
        noProvidersWarning(env);
        return true;
    }

    public static String normalizeAbsolutePath(String absPath, ExecutionEnvironment env) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                return provider.normalizeAbsolutePath(absPath, env);
            }
        }
        noProvidersWarning(env);
        return FileUtil.normalizePath(absPath); // or should it return just absPath?
    }

    /**
     * In many places, standard sequence is as follows:
     *  - convert path to absolute if need
     *  - normalize it
     *  - find file object
     * In the case of non-local file systems we should delegate it to correspondent file systems.
     */
    public static FileObject getFileObject(FileObject baseFileObject, String relativeOrAbsolutePath) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(baseFileObject)) {
                return provider.getFileObject(baseFileObject, relativeOrAbsolutePath);
            }
        }
        noProvidersWarning(baseFileObject);
        if (isAbsolute(relativeOrAbsolutePath)) {
            try {
                return baseFileObject.getFileSystem().findResource(relativeOrAbsolutePath);
            } catch (FileStateInvalidException ex) {
                return null;
            }
        } else {
            return baseFileObject.getFileObject(relativeOrAbsolutePath);
        }
    }

    public static FileObject getCanonicalFileObject(FileObject fileObject) throws IOException {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileObject)) {
                return provider.getCanonicalFileObject(fileObject);
            }
        }
        noProvidersWarning(fileObject);
        return fileObject;
    }
    
    public static String getCanonicalPath(FileObject fileObject) throws IOException {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileObject)) {
                return provider.getCanonicalPath(fileObject);
            }
        }
        noProvidersWarning(fileObject);
        return fileObject.getPath();
    }

    public static boolean isAbsolute(String path) {
        if (path == null || path.length() == 0) {
            return false;
        } else if (path.charAt(0) == '/') {
            return true;
        } else if (path.charAt(0) == '\\') {
            return true;
        } else if (path.indexOf(':') == 1 && Utilities.isWindows()) {
            return true;
        } else {
            return false;
        }
    }

    public static FileObject urlToFileObject(String url) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(url)) {
                return provider.urlToFileObject(url);
            }
        }
        noProvidersWarning(url);
        return null;
    }

    public static String fileObjectToUrl(FileObject fileObject) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileObject)) {
                return provider.toURL(fileObject);
            }
        }
        noProvidersWarning(fileObject);
        try {
            return fileObject.getURL().toExternalForm();
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public static void addDownloadListener(DownloadListener listener) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            provider.addDownloadListener(listener);
        }
    }

    public static void removeDownloadListener(DownloadListener listener) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            provider.addDownloadListener(listener);
        }
    }

    private static void noProvidersWarning(Object object) {
        if (RemoteLogger.getInstance().isLoggable(Level.FINE)) {        
            if (RemoteLogger.getInstance().isLoggable(Level.FINEST)) {
                String message = "No file system providers for " + object;
                RemoteLogger.getInstance().log( Level.FINE, message, new Exception(message)); //NOI18N
            } else {
                RemoteLogger.getInstance().log(Level.FINEST, "No file system providers for {0}", object); //NOI18N
            }
        }
    }
}
