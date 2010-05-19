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
package org.netbeans.modules.nativeexecution.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.ConcurrentTasksSupport.Counters;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.openide.util.Exceptions;

public class NativeProcessBuilderTest extends NativeExecutionBaseTestCase {

    public NativeProcessBuilderTest(String name) {
        super(name);
    }

    public NativeProcessBuilderTest(String name, ExecutionEnvironment execEnv) {
        super(name, execEnv);
    }

    @SuppressWarnings("unchecked")
    public static Test suite() {
        return new NativeExecutionBaseTestSuite(NativeProcessBuilderTest.class);
    }

    public void testNewLocalProcessBuilder() throws Exception {
        NativeProcessBuilder npb = NativeProcessBuilder.newLocalProcessBuilder();
        assertNotNull(npb);
    }

    public void testSetExecutableLocal() throws Exception {
        doTestSetExecutable(ExecutionEnvironmentFactory.getLocal());
    }

    @ForAllEnvironments(section = "remote.platforms")
    public void testSetExecutable() throws Exception {
        doTestSetExecutable(getTestExecutionEnvironment());
    }

    public void testListenersLocal() throws Exception {
        doTestListeners(ExecutionEnvironmentFactory.getLocal());
    }

    @ForAllEnvironments(section = "remote.platforms")
    public void testListeners() throws Exception {
        doTestListeners(getTestExecutionEnvironment());
    }

    private void doTestListeners(ExecutionEnvironment env) {
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
        npb.setExecutable("echo");
        final Counters counters = new Counters();
        final AtomicInteger result = new AtomicInteger();
        final AtomicReference<NativeProcess> processRef = new AtomicReference<NativeProcess>();

        npb.addNativeProcessListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                State state = ((NativeProcessChangeEvent) e).state;
                counters.getCounter(state.name()).incrementAndGet();

                if (state == State.FINISHED) {
                    result.set(processRef.get().exitValue());
                }

                switch (state) {
                    case STARTING:
                        assertEquals(0, counters.getCounter(State.RUNNING.name()).get());
                        assertEquals(0, counters.getCounter(State.FINISHED.name()).get());
                        assertEquals(0, counters.getCounter(State.CANCELLED.name()).get());
                        assertEquals(0, counters.getCounter(State.ERROR.name()).get());
                        break;
                    case RUNNING:
                        assertEquals(1, counters.getCounter(State.STARTING.name()).get());
                        assertEquals(0, counters.getCounter(State.FINISHED.name()).get());
                        assertEquals(0, counters.getCounter(State.CANCELLED.name()).get());
                        assertEquals(0, counters.getCounter(State.ERROR.name()).get());
                        break;
                    case FINISHED:
                        assertEquals(1, counters.getCounter(State.STARTING.name()).get());
                        assertEquals(1, counters.getCounter(State.RUNNING.name()).get());
                        assertEquals(0, counters.getCounter(State.CANCELLED.name()).get());
                        assertEquals(0, counters.getCounter(State.ERROR.name()).get());
                        break;
                }

            }
        });

        try {
            NativeProcess process = npb.call();
            processRef.set(process);
            int exitCode = process.waitFor();
            assertTrue(exitCode == result.get());
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void testSetCommandLineLocal() throws Exception {
        doTestSetCommandLine(ExecutionEnvironmentFactory.getLocal());
    }

    @ForAllEnvironments(section = "remote.platforms")
    public void testSetCommandLine() throws Exception {
        doTestSetCommandLine(getTestExecutionEnvironment());
    }

    private void doTestSetCommandLine(ExecutionEnvironment execEnv) throws Exception {
        HostInfo hostinfo = HostInfoUtils.getHostInfo(execEnv);
        String tmpDir = hostinfo.getTempDir();
        String testDir = tmpDir + "/path with a space";
        NativeProcessBuilder npb;

        // Don't use CommonTasksSupport.{rmDir,mkDir} in this test

        npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setCommandLine("rm -rf \"" + testDir + "\"");
        assertEquals("rm -rf \"" + testDir + "\"", 0, npb.call().waitFor());

        npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setCommandLine("mkdir \"" + testDir + "\""); // NOI18N
        assertEquals("mkdir \"" + testDir + "\"", 0, npb.call().waitFor());

        if (execEnv.isLocal() && hostinfo.getOS().getFamily() == HostInfo.OSFamily.WINDOWS) {
            String realDir = WindowsSupport.getInstance().convertToWindowsPath(testDir);
            assertTrue(new File(realDir).isDirectory());
        } else {
            assertTrue(testDir + " does not exist", HostInfoUtils.fileExists(execEnv, testDir));
        }

        String lsCmd;
        if (execEnv.isLocal() && hostinfo.getOS().getFamily() == HostInfo.OSFamily.WINDOWS) {
            lsCmd = findComand("ls.exe");
            assertNotNull("Cannot find ls.exe in paths", lsCmd);
            lsCmd = WindowsSupport.getInstance().convertToShellPath(lsCmd);
        } else {
            lsCmd = "/bin/ls";
        }

        npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setCommandLine("cp " + lsCmd + " \"" + testDir + "/copied ls\""); // NOI18N
        assertEquals("cp " + lsCmd + " \"" + testDir + "/copied ls\"", 0, npb.call().waitFor());

        npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        if (execEnv.isLocal() && hostinfo.getOS().getFamily() == HostInfo.OSFamily.WINDOWS) {
            npb.setCommandLine("\"" + WindowsSupport.getInstance().convertToWindowsPath(testDir + "/copied ls") + "\" \"" + testDir + "\"");
        } else {
            npb.setCommandLine("\"" + testDir + "/copied ls\" \"" + testDir + "\"");
        }

        List<String> lsOut = ProcessUtils.readProcessOutput(npb.call());
        boolean found = false;
        for (String line : lsOut) {
            if (line.equals("copied ls")) {
                found = true;
                break;
            }
        }
        assertTrue("\"copied ls\" not found in ls output", found);
    }

    private void doTestSetExecutable(ExecutionEnvironment execEnv) throws Exception {
        HostInfo hostinfo = HostInfoUtils.getHostInfo(execEnv);
        String tmpDir = hostinfo.getTempDir();
        String testDir = tmpDir + "/path with a space"; // NOI18N
        NativeProcessBuilder npb;

        // Don't use CommonTasksSupport.{rmDir,mkDir} in this test

        npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setExecutable("rm").setArguments("-rf", testDir);
        assertEquals("rm -rf \"" + testDir + "\"", 0, npb.call().waitFor());

        npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setExecutable("mkdir").setArguments(testDir);
        assertEquals("mkdir \"" + testDir + "\"", 0, npb.call().waitFor());

        if (execEnv.isLocal() && hostinfo.getOS().getFamily() == HostInfo.OSFamily.WINDOWS) {
            String realDir = WindowsSupport.getInstance().convertToWindowsPath(testDir);
            assertTrue(new File(realDir).isDirectory());
        } else {
            assertTrue(testDir + " does not exist", HostInfoUtils.fileExists(execEnv, testDir));
        }

        String lsCmd;
        if (execEnv.isLocal() && hostinfo.getOS().getFamily() == HostInfo.OSFamily.WINDOWS) {
            lsCmd = findComand("ls.exe");
            assertNotNull("Cannot find ls.exe in paths", lsCmd);
            lsCmd = WindowsSupport.getInstance().convertToShellPath(lsCmd);
        } else {
            lsCmd = "/bin/ls";
        }

        npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setCommandLine("cp " + lsCmd + " \"" + testDir + "/copied ls\""); // NOI18N
        assertEquals("cp " + lsCmd + " \"" + testDir + "/copied ls\"", 0, npb.call().waitFor());

        npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        if (execEnv.isLocal() && hostinfo.getOS().getFamily() == HostInfo.OSFamily.WINDOWS) {
            npb.setExecutable(WindowsSupport.getInstance().convertToWindowsPath(testDir + "/copied ls"));
        } else {
            npb.setExecutable(testDir + "/copied ls");
        }
        npb.setArguments(testDir);

        List<String> lsOut = ProcessUtils.readProcessOutput(npb.call());
        boolean found = false;
        for (String line : lsOut) {
            if (line.equals("copied ls")) {
                found = true;
                break;
            }
        }
        assertTrue("\"copied ls\" not found in ls output", found);
    }

    private String findComand(String command) {
        ArrayList<String> list = new ArrayList<String>();
        String path = System.getenv("PATH"); // NOI18N
        if (path != null) {
            StringTokenizer st = new StringTokenizer(path, File.pathSeparator); // NOI18N
            while (st.hasMoreTokens()) {
                String dir = st.nextToken();
                list.add(dir);
            }
        } else {
            list.add("C:/WINDOWS/System32"); // NOI18N
            list.add("C:/WINDOWS"); // NOI18N
            list.add("C:/cygwin/bin"); // NOI18N
        }
        for (String p : list) {
            String task = p + File.separatorChar + command;
            File tool = new File(task);
            if (tool.exists() && tool.isFile()) {
                return tool.getAbsolutePath();
            }
        }
        return null;
    }
}
