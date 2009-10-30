/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.cnd.remote.support;

import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import junit.framework.Test;
import org.netbeans.modules.cnd.remote.RemoteDevelopmentTest;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 * @author Vladimir Kvashin
 */
public class UploadSmallFilesTestCase extends RemoteTestBase {

    public UploadSmallFilesTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testCopySmallFiles() throws Exception {
        File dir = new File("/tmp/smallfiles"); //NOI18N
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
        File[] files = dir.listFiles();
        long totalSize = 0;
        int totalCount = 0;
        long totalTime = System.currentTimeMillis();
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        ConnectionManager.getInstance().connectTo(execEnv);

        File tmpFile = File.createTempFile("copy_small_files", ".dat");
        String remoteDir = "/tmp/" + tmpFile.getName();
        tmpFile.delete();
        
        Future<Integer> mkDirTask = CommonTasksSupport.mkDir(execEnv, remoteDir, new PrintWriter(System.err));
        System.out.printf("Mkdir %s\n", remoteDir);
        int rc = mkDirTask.get(30, TimeUnit.SECONDS);
        System.out.printf("mkdir %s done, rc=%d\n", remoteDir, rc);
        assertEquals(0, rc);
        //Thread.sleep(2000);

        RemoteCopySupport rcs = new RemoteCopySupport(execEnv);
        for (File localFile : files) {
            totalCount++;
            totalSize += localFile.length();
            assertTrue(localFile.exists());                        
            String remoteFile = remoteDir + "/" + localFile.getName(); //NOI18N
            long time = System.currentTimeMillis();
            rcs.copyTo(localFile.getAbsolutePath(), remoteFile); //NOI18N
            time = System.currentTimeMillis() - time;
            System.out.printf("File %s copied to %s:%s in %d ms\n", localFile, execEnv, remoteFile, time);
        }
        totalTime = System.currentTimeMillis() - totalTime;
        System.out.printf("%d Kb in %d files to %s in %d ms\n", totalSize/1024, totalCount, execEnv, totalTime);
    }
    
    public static Test suite() {
        return new RemoteDevelopmentTest(UploadSmallFilesTestCase.class);
    }
}
