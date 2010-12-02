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

package org.netbeans.modules.remote.impl.fs;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Vladimir Kvashin
 */
public class RemoteFileTestBase extends NativeExecutionBaseTestCase {

    protected final RemoteFileSystem fs;
    protected final FileObject rootFO;
    protected final ExecutionEnvironment execEnv;

    public RemoteFileTestBase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
        this.execEnv = execEnv;
        fs = RemoteFileSystemManager.getInstance().getFileSystem(execEnv);
        assertNotNull("Null remote file system", fs);
        rootFO = fs.getRoot();
        assertNotNull("Null root file object", rootFO);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File cache = fs.getCache();
        removeDirectoryContent(cache);
        assertTrue("Can not create directory " + cache.getAbsolutePath(), cache.exists() || cache.mkdirs());
        ExecutionEnvironment env = getTestExecutionEnvironment();
        ConnectionManager.getInstance().connectTo(env);
    }

    protected String readRemoteFile(String absPath) throws Exception {
        FileObject fo = rootFO.getFileObject(absPath);
        assertNotNull("Null file object for " + getFileName(execEnv, absPath), fo);
        assertTrue("File " +  getFileName(execEnv, absPath) + " does not exist", fo.isValid());
        return readFile(fo);
    }

    protected void writeFile(FileObject fo, CharSequence content) throws Exception {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(fo.getOutputStream()));
            bw.append(content);
        } finally {
            if (bw != null) {
                bw.close();
            }
        }
    }

    protected String readFile(FileObject fo) throws Exception {
        assertTrue("File " +  fo.getPath() + " does not exist", fo.isValid());
        InputStream is = fo.getInputStream();
        BufferedReader rdr = new BufferedReader(new InputStreamReader(new BufferedInputStream(is)));
        try {
            assertNotNull("Null input stream", is);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rdr.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } finally {
            rdr.close();
        }
    }

    protected String getFileName(ExecutionEnvironment execEnv, String absPath) {
        return execEnv.toString() + ':' + absPath;
    }

//    protected void checkFileExistance(String absPath) throws Exception {
//        FileObject fo = rootFO.getFileObject(absPath);
//        assertNotNull("Null file object for " + getFileName(execEnv, absPath), fo);
//        assertFalse("File " +  getFileName(execEnv, absPath) + " does not exist", fo.isVirtual());
//    }



}
