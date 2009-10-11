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
package org.netbeans.modules.nativeexecution;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.support.EnvWriter;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.UnbufferSupport;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.openide.util.NbBundle;

public final class LocalNativeProcess extends AbstractNativeProcess {

    private Process process = null;
    private InputStream processOutput = null;
    private OutputStream processInput = null;
    private InputStream processError = null;

    public LocalNativeProcess(NativeProcessInfo info) {
        super(info);
    }

    protected void create() throws Throwable {
        boolean isWindows = hostInfo.getOSFamily() == OSFamily.WINDOWS;

        try {
            if (isWindows) {
                createWin();
            } else {
                createNonWin();
            }
        } catch (Throwable ex) {
            String msg = ex.getMessage() == null ? ex.toString() : ex.getMessage();
            processOutput = new ByteArrayInputStream(new byte[0]);
            processError = new ByteArrayInputStream(msg.getBytes());
            processInput = new ByteArrayOutputStream();
            throw ex;
        }
    }

    private void createNonWin() throws IOException, InterruptedException {
        // Get working directory ....
        String workingDirectory = info.getWorkingDirectory(true);

        if (workingDirectory != null) {
            workingDirectory = new File(workingDirectory).getAbsolutePath();
        }

        final MacroMap env = info.getEnvironment().clone();

        if (info.isUnbuffer()) {
            UnbufferSupport.initUnbuffer(info.getExecutionEnvironment(), env);
        }

        env.appendPathVariable("PATH", "/bin:/usr/bin:" + hostInfo.getPath()); // NOI18N

        final ProcessBuilder pb = new ProcessBuilder(hostInfo.getShell(), "-s"); // NOI18N

        if (isInterrupted()) {
            throw new InterruptedException();
        }

        process = pb.start();

        processInput = process.getOutputStream();
        processError = process.getErrorStream();
        processOutput = process.getInputStream();

        processInput.write("echo $$\n".getBytes()); // NOI18N
        processInput.flush();

        EnvWriter ew = new EnvWriter(processInput);
        ew.write(env);

        if (workingDirectory != null) {
            processInput.write(("cd \"" + workingDirectory + "\"\n").getBytes()); // NOI18N
        }

        if (info.getInitialSuspend()) {
            processInput.write("ITS_TIME_TO_START=\n".getBytes()); // NOI18N
            processInput.write("trap 'ITS_TIME_TO_START=1' CONT\n".getBytes()); // NOI18N
            processInput.write("while [ -z \"$ITS_TIME_TO_START\" ]; do sleep 1; done\n".getBytes()); // NOI18N
        }

        processInput.write(("exec " + info.getCommandLineForShell() + "\n").getBytes()); // NOI18N
        processInput.flush();

        creation_ts = System.nanoTime();

        readPID(processOutput);
    }

    private void createWinUsingShell() throws IOException, InterruptedException {
        // Get working directory ....
        String workingDirectory = info.getWorkingDirectory(true);

        if (workingDirectory != null) {
            workingDirectory = new File(workingDirectory).getAbsolutePath();
        }

        final MacroMap env = info.getEnvironment().clone();

        if (info.isUnbuffer()) {
            UnbufferSupport.initUnbuffer(info.getExecutionEnvironment(), env);
        }

        env.put("PATH", "/bin:" + WindowsSupport.getInstance().convertToAllShellPaths(env.get("PATH"))); // NOI18N

        final ProcessBuilder pb = new ProcessBuilder(hostInfo.getShell(), "-s"); // NOI18N

        if (isInterrupted()) {
            throw new InterruptedException();
        }

        process = pb.start();

        processInput = process.getOutputStream();
        processError = process.getErrorStream();
        processOutput = process.getInputStream();

        processInput.write("echo $$\n".getBytes()); // NOI18N
        processInput.flush();

        EnvWriter ew = new EnvWriter(processInput);
        ew.write(env);

        if (workingDirectory != null) {
            processInput.write(("cd \"" + WindowsSupport.getInstance().convertToShellPath(workingDirectory) + "\"\n").getBytes()); // NOI18N
        }

        processInput.write(("exec " + info.getCommandLineForShell() + "\n").getBytes()); // NOI18N
        processInput.flush();

        creation_ts = System.nanoTime();

        readPID(processOutput);
    }

    private void createWin() throws IOException, InterruptedException {
        // Don't use shell wrapping on Windows...
        // Mostly this is because exec works not as expected and we cannot
        // control processes started with exec method....

        // Suspend is not supported on Windows.

        final ProcessBuilder pb = new ProcessBuilder(); // NOI18N

        final MacroMap jointEnv = MacroMap.forExecEnv(ExecutionEnvironmentFactory.getLocal());
        jointEnv.putAll(pb.environment());
        jointEnv.putAll(info.getEnvironment());

        if (isInterrupted()) {
            throw new InterruptedException();
        }

        // In case we want to run application that was compiled with cygwin
        // and require cygwin1.dll to run - we need the path to the dll in the
        // PATH variable..

        if (hostInfo.getShell() != null) {
            jointEnv.appendPathVariable("PATH", new File(hostInfo.getShell()).getParent()); // NOI18N
        }

        if (info.isUnbuffer()) {
            UnbufferSupport.initUnbuffer(info.getExecutionEnvironment(), jointEnv);
        }

        pb.environment().clear();

        for (Entry<String, String> envEntry : jointEnv.entrySet()) {
            pb.environment().put(envEntry.getKey(), envEntry.getValue());
        }

        pb.command(info.getCommand());

        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest(String.format("Command: %s", info.getCommand())); // NOI18N
        }

        String wdir = info.getWorkingDirectory(true);
        if (wdir != null) {
            File wd = new File(wdir);
            if (wd.exists()) {
                pb.directory(wd);
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.finest(String.format("Working directory: %s", wdir)); // NOI18N
                }
            }
        }

        process = pb.start();

        creation_ts = System.nanoTime();

        processInput = process.getOutputStream();
        processError = process.getErrorStream();
        processOutput = process.getInputStream();

        // Fake PID...
        ByteArrayInputStream bis = new ByteArrayInputStream("12345".getBytes()); // NOI18N

        readPID(bis);
    }

    @Override
    public OutputStream getOutputStream() {
        return processInput;
    }

    @Override
    public InputStream getInputStream() {
        return processOutput;
    }

    @Override
    public InputStream getErrorStream() {
        return processError;
    }

    @Override
    public final int waitResult() throws InterruptedException {
        if (process == null) {
            throw new InterruptedException();
        }

        /*
         * Why not just process.waitResult()...
         * This is to avoid a problem with short-running tasks, when
         * this Thread (that waits for process' termination) doesn't see
         * that it has been interrupted....
         * TODO: describe situation in details...
         */

        int result = -1;

//        // Get lock on process not to take it on every itteration
//        // (in process.exitValue())
//
//        synchronized (process) {
        // Why this synchronized is commented-out..
        // This is because ProcessReaper is also synchronized on this...
        // And it should be able to react on process' termination....

        while (true) {
            // This sleep is to avoid lost interrupted exception...
            try {
                Thread.sleep(200);
                // 200 - to make this check not so often...
                // actually, to avoid the problem, 1 is OK.
            } catch (InterruptedException ex) {
                throw ex;
            }

            try {
                result = process.exitValue();
            } catch (IllegalThreadStateException ex) {
                continue;
            }

            break;
        }
//        }

        return result;
    }

    @Override
    protected final synchronized void cancel() {
        if (process != null) {
            process.destroy();
        }
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(LocalNativeProcess.class, key, params);
    }
}
