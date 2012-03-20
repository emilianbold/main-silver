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
package org.netbeans.modules.php.project.connections.transfer;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.netbeans.modules.php.api.util.FileUtils;

/**
 * {@link TransferFile Transfer file} implementation for {@link File local file}.
 */
final class LocalTransferFile extends TransferFile {

    // considered to be thread-safe, see Javadoc and sources
    private final File file;
    private final boolean forceDirectory;


    LocalTransferFile(File file, TransferFile parent, String baseLocalDirectoryPath, String baseRemoteDirectoryPath, boolean forceDirectory) {
        super(parent, baseLocalDirectoryPath, baseRemoteDirectoryPath);
        this.file = file;
        this.forceDirectory = forceDirectory;

        if (file == null) {
            throw new NullPointerException("Local file cannot be null");
        }
        if (!file.getAbsolutePath().startsWith(baseLocalDirectoryPath)) {
            throw new IllegalArgumentException("File '" + file.getAbsolutePath() + "' must be underneath base directory '" + baseLocalDirectoryPath + "'");
        }
        if (forceDirectory && file.isFile()) {
            throw new IllegalArgumentException("File '" + file.getAbsolutePath() + "' can't be forced as a directory since it is a file");
        }
        // get attributes so we know them even if the file is later deleted
        getSize();
        getTimestamp();
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getRemotePath() {
        String absolutePath = file.getAbsolutePath();
        if (absolutePath.equals(baseLocalDirectoryPath)) {
            return REMOTE_PROJECT_ROOT;
        }
        // remove file-separator from the beginning of the relative path
        String remotePath = absolutePath.substring(baseLocalDirectoryPath.length() + File.separator.length());
        if (File.separator.equals(REMOTE_PATH_SEPARATOR)) {
            return remotePath;
        }
        return remotePath.replace(File.separator, REMOTE_PATH_SEPARATOR);
    }

    @Override
    protected Collection<TransferFile> fetchChildren() {
        // not supported for local files, these are always fetched (can be improved)
        return Collections.emptyList();
    }

    @Override
    protected long getSizeImpl() {
        if (isFile()) {
            return file.length();
        }
        return 0L;
    }

    @Override
    public boolean isDirectory() {
        if (file.exists()) {
            boolean directory = file.isDirectory();
            if (forceDirectory && !directory && file.isFile()) {
                assert false : "File forced as directory but is regular existing file";
            }
            return directory;
        }
        return forceDirectory;
    }

    @Override
    public boolean isFile() {
        if (file.exists()) {
            boolean isFile = file.isFile();
            if (isFile && forceDirectory) {
                assert false : "File forced as directory but is regular existing file";
            }
            return isFile;
        }
        return !forceDirectory;
    }

    @Override
    public boolean isLink() {
        return FileUtils.isDirectoryLink(file);
    }

    @Override
    protected long getTimestampImpl() {
        return TimeUnit.SECONDS.convert(file.lastModified(), TimeUnit.MILLISECONDS);
    }

}
