/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.sync.download;

import java.io.File;
import java.lang.String;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.netbeans.modules.cnd.remote.pbuild.*;
import junit.framework.Test;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.remote.RemoteDevelopmentTestSuite;
import org.netbeans.modules.cnd.remote.sync.download.FileDownloadInfo.State;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileUtil;
/**
 *
 * @author Vladimir Kvashin
 */
public class RemoteBuildUpdatesDownloadTest extends RemoteBuildTestBase {

    public RemoteBuildUpdatesDownloadTest(String testName) {
        super(testName);
    }

    public RemoteBuildUpdatesDownloadTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    private static class NameStatePair {
        public final File file;
        public final FileDownloadInfo.State state;
        public NameStatePair(File file, State state) {
            this.file = file;
            this.state = state;
        }
    }

    @ForAllEnvironments
    public void test_LexYacc_BuildLocalAndRemote() throws Exception {
        MakeProject makeProject = prepareSampleProject(Sync.RFS, Toolchain.GNU, "LexYacc", "LexYacc_Build");
        int timeout = getSampleBuildTimeout();
        changeProjectHost(makeProject, ExecutionEnvironmentFactory.getLocal());
        buildProject(makeProject, ActionProvider.COMMAND_BUILD, timeout, TimeUnit.SECONDS);
        changeProjectHost(makeProject, getTestExecutionEnvironment());
        buildProject(makeProject, ActionProvider.COMMAND_BUILD, timeout, TimeUnit.SECONDS);
        buildProject(makeProject, ActionProvider.COMMAND_REBUILD, timeout, TimeUnit.SECONDS);
    }

    @ForAllEnvironments
    public void test_LexYacc_Updates() throws Exception {
        List<FileDownloadInfo> updates;
        //buildSample(Sync.RFS, Toolchain.GNU, "LexYacc", "LexYacc_Updates", 1);
        MakeProject makeProject = prepareSampleProject(Sync.RFS, Toolchain.GNU, "LexYacc", "LexYacc_Updates");
        int timeout = getSampleBuildTimeout();
        buildProject(makeProject, ActionProvider.COMMAND_REBUILD, timeout, TimeUnit.SECONDS);
        File projectDirFile = FileUtil.toFile(makeProject.getProjectDirectory());
        NameStatePair[] filesToCheck = new NameStatePair[] {
            new NameStatePair(new File(projectDirFile, "y.tab.c"), FileDownloadInfo.State.UNCONFIRMED),
            new NameStatePair(new File(projectDirFile, "y.tab.h"), FileDownloadInfo.State.UNCONFIRMED),
            new NameStatePair(new File(projectDirFile, "lex.yy.c"), FileDownloadInfo.State.UNCONFIRMED)
        };
        sleep(2000); // workaround for #182824 program run under pty in Internal Terminal writes incorrect status and writes it incorrectly
        updates = HostUpdates.testGetUpdates(getTestExecutionEnvironment());
        checkInfo(updates, filesToCheck);
    }

    private void checkInfo(List<FileDownloadInfo> updates, NameStatePair[] pairsToCheck) {
        boolean success = true;
        StringBuilder notFoundMessage = new StringBuilder();
        StringBuilder wrongStateFoundMessage = new StringBuilder();
        for (NameStatePair pair : pairsToCheck) {
            FileDownloadInfo info = find(updates, pair.file);
            if (info == null) {
                success = false;
                if (notFoundMessage.length() == 0) {
                    notFoundMessage.append("Can not find FileDownloadInfo for ");
                } else {
                    notFoundMessage.append(", ");
                }
                notFoundMessage.append(pair.file.getName());
            } else {
                FileDownloadInfo.State state = info.getState();
                if (state.equals(pair.state)) {
                    System.err.printf("\tOK state %s for %s at %s\n", info.getState(), info.getLocalFile(), getTestExecutionEnvironment());
                } else {
                    success = false;
                    if (wrongStateFoundMessage.length() == 0) {
                        wrongStateFoundMessage.append("Wrong state: ");
                    } else {
                        wrongStateFoundMessage.append(", ");
                    }
                }
                wrongStateFoundMessage.append(pair.file.getName()).append(": expected ").append(pair.state).append(" found ").append(state);
            }
        }
        if (!success) {
            StringBuilder message = new StringBuilder(notFoundMessage);
            if (message.length() > 0 && wrongStateFoundMessage.length() > 0) {
                message.append(";\n");
            }
            message.append(wrongStateFoundMessage);
            assertTrue(message.toString(), false);
        }
    }

    private FileDownloadInfo find(List<FileDownloadInfo> updates, File file) {
        for (FileDownloadInfo info : updates) {
            if (info.getLocalFile().equals(file)) {
                return info;
            }
        }
        return null;
    }

    public static Test suite() {
        return new RemoteDevelopmentTestSuite(RemoteBuildUpdatesDownloadTest.class);
    }
}
