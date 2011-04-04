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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.impl.fs;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 * @author Vladimir Kvashin
 */
public class ListenersParityTestCase extends RemoteFileTestBase {

    public ListenersParityTestCase(String testName) {
        super(testName);
    }
    
    public ListenersParityTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    private void doTestListeners2(FileObject baseDirFO, File log, boolean recursive) throws Exception {
        PrintStream out = new PrintStream(log);
        try {
            String prefix = baseDirFO.getPath();
            FCL fcl = new FCL("baseDir", prefix, out);
            if (recursive) {
                FileSystemProvider.addRecursiveListener(fcl, baseDirFO.getFileSystem(), baseDirFO.getPath());
            } else {
                baseDirFO.addFileChangeListener(fcl);
            }
            FileObject childFO = baseDirFO.createData("child_file_1");
            FileObject subdirFO = baseDirFO.createFolder("child_folder");
            if (!recursive) {
                subdirFO.addFileChangeListener(new FCL(subdirFO.getNameExt(), prefix, out));
            }
            FileObject grandChildFO = subdirFO.createData("grand_child_file");
            FileObject grandChildDirFO = subdirFO.createFolder("grand_child_dir");
            FileObject grandGrandChildFO = grandChildDirFO.createData("grand_grand_child_file");
            // baseDirFO.refresh() will break the test. TODO: investigate.
            // baseDirFO.refresh();
            grandGrandChildFO.delete();
            grandChildDirFO.delete();
        } finally {
            out.close();
        }
    }
    
    private void doTestListeners1(boolean recursive) throws Throwable {
        String remoteBaseDir = mkTemp(true);
        File localTmpDir = createTempFile(getClass().getSimpleName(), ".tmp", true);
        try {            
            FileObject remoteBaseDirFO = getFileObject(remoteBaseDir);
            FileObject localBaseDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(localTmpDir));            
            File workDir = getWorkDir();
            File remoteLog = new File(workDir, "remote.dat");
            File localLog = new File(workDir, "local.dat");
            doTestListeners2(remoteBaseDirFO, remoteLog, recursive);
            doTestListeners2(localBaseDirFO, localLog, recursive);
            printFile(localLog, "LOCAL ", System.out);
            printFile(remoteLog, "REMOTE", System.out);
            File diff = new File(workDir, "diff.diff");
            try {
                assertFile("Remote and local events differ, see diff " + remoteLog.getAbsolutePath() + " " + localLog.getAbsolutePath(), remoteLog, localLog, diff);
            } catch (Throwable ex) {
                if (diff.exists()) {
                    printFile(diff, null, System.err);
                }
                throw ex;
            }
        } finally {
            if (remoteBaseDir != null) {
                CommonTasksSupport.rmDir(execEnv, remoteBaseDir, true, new OutputStreamWriter(System.err));
            }
            if (localTmpDir != null && localTmpDir.exists()) {
                removeDirectory(localTmpDir);
            }
        }    
    }
    
    @ForAllEnvironments
    public void testListeners() throws Throwable {                
        doTestListeners1(false);
    }

    @ForAllEnvironments
    public void testRecursiveListeners() throws Throwable {                
        doTestListeners1(true);
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(ListenersParityTestCase.class);
    }
}
