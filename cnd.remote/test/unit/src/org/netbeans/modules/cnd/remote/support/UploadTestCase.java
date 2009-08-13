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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import junit.framework.Test;
import org.junit.Ignore;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.remote.RemoteDevelopmentTest;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 * @author Sergey Grinev
 */
public class UploadTestCase extends RemoteTestBase {

    static {
//        System.setProperty("cnd.remote.testuserinfo", "rdtest:********@endif.russia");
//        System.setProperty("cnd.remote.logger.level", "0");
//        System.setProperty("nativeexecution.support.logger.level", "0");
    }
    public UploadTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testCopyTo() throws Exception {
        File localFile = File.createTempFile("cnd", ".cnd"); //NOI18N
        FileWriter fstream = new FileWriter(localFile);
        StringBuilder sb = new StringBuilder("File from "); //NOI18N
        try {
            InetAddress addr = InetAddress.getLocalHost();
            sb.append( addr.getHostName() );
        } catch (UnknownHostException e) {
        }
        sb.append("\ntime: " + System.currentTimeMillis()+ "\n"); //NOI18N
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(sb.toString());
        out.close();
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();        
        RemoteCopySupport rcs = new RemoteCopySupport(execEnv);
        String remoteFile = "/tmp/" + localFile.getName(); //NOI18N
        rcs.copyTo(localFile.getAbsolutePath(), remoteFile); //NOI18N
        assert HostInfoProvider.fileExists(execEnv, remoteFile) : "Error copying file " + remoteFile + " to " + execEnv + " : file does not exist";
        String catCommand = "cat " + remoteFile;
        RemoteCommandSupport rcs2 = new RemoteCommandSupport(execEnv, catCommand);
//            assert rcs2.run() == 0; // add more output
        int rc = rcs2.run();
        if (rc != 0) {
            assert false : "RemoteCommandSupport: " + catCommand + " returned " + rc + " on " + execEnv;
        }
        assert rcs2.getOutput().equals(sb.toString());
        assert RemoteCommandSupport.run(execEnv, "rm " + remoteFile) == 0;
    }
    
    public static Test suite() {
        return new RemoteDevelopmentTest(UploadTestCase.class);
    }
}
