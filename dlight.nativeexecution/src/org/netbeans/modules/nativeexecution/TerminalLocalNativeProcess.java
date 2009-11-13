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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminal;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.Signal;
import org.netbeans.modules.nativeexecution.support.EnvWriter;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.support.InstalledFileLocatorProvider;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 */
public final class TerminalLocalNativeProcess extends AbstractNativeProcess {

    private final static java.util.logging.Logger log = Logger.getInstance();
    private final static File dorunScript;
    private ExternalTerminal terminal;
    private InputStream processOutput;
    private InputStream processError;
    private File resultFile;
    private final boolean isWindows;
    private final boolean isMacOS;

    static {
        InstalledFileLocator fl = InstalledFileLocatorProvider.getDefault();
        File dorunScriptFile = fl.locate("bin/nativeexecution/dorun.sh", null, false); // NOI18N

        if (dorunScriptFile == null) {
            log.severe("Unable to locate bin/nativeexecution/dorun.sh file!"); // NOI18N
        } else if (!Utilities.isWindows()) {
            CommonTasksSupport.chmod(ExecutionEnvironmentFactory.getLocal(),
                    dorunScriptFile.getAbsolutePath(), 0755, null);
        }

        dorunScript = dorunScriptFile;
    }

    public TerminalLocalNativeProcess(
            final NativeProcessInfo info, final ExternalTerminal terminal) {
        super(info);
        this.terminal = terminal;
        this.processOutput = new ByteArrayInputStream(
                (loc("TerminalLocalNativeProcess.ProcessStarted.text") + '\n').getBytes()); // NOI18N

        isWindows = hostInfo != null && hostInfo.getOSFamily() == OSFamily.WINDOWS;
        isMacOS = hostInfo != null && hostInfo.getOSFamily() == OSFamily.MACOSX;
    }

    protected void create() throws Throwable {
        File pidFileFile = null;
        File envFileFile = null;
        File shFileFile = null;

        try {
            if (dorunScript == null) {
                throw new IOException(loc("TerminalLocalNativeProcess.dorunNotFound.text")); // NOI18N
            }

            if (isWindows && hostInfo.getShell() == null) {
                throw new IOException(loc("NativeProcess.shellNotFound.text")); // NOI18N
            }

            final String commandLine = info.getCommandLineForShell();
            final String wDir = info.getWorkingDirectory(true);

            final File workingDirectory = (wDir == null) ? new File(".") : new File(wDir); // NOI18N

            pidFileFile = File.createTempFile("dlight", "termexec", hostInfo.getTempDirFile()).getAbsoluteFile(); // NOI18N
            envFileFile = new File(pidFileFile.getPath() + ".env"); // NOI18N
            shFileFile = new File(pidFileFile.getPath() + ".sh"); // NOI18N
            resultFile = new File(shFileFile.getPath() + ".res"); // NOI18N

            resultFile.deleteOnExit();

            String pidFile = (isWindows) ? WindowsSupport.getInstance().convertToShellPath(pidFileFile.getPath()) : pidFileFile.getPath();
            String envFile = pidFile + ".env"; // NOI18N
            String shFile = pidFile + ".sh"; // NOI18N

            FileWriter shWriter = new FileWriter(shFileFile);
            shWriter.write("echo $$ > \"" + pidFile + "\" || exit $?\n"); // NOI18N
            shWriter.write(". \"" + envFile + "\" 2>/dev/null\n"); // NOI18N
            shWriter.write("exec " + commandLine + "\n"); // NOI18N
            shWriter.flush();
            shWriter.close();

            final ExternalTerminalAccessor terminalInfo =
                    ExternalTerminalAccessor.getDefault();

            if (terminalInfo.getTitle(terminal) == null) {
                terminal = terminal.setTitle(commandLine);
            }

            List<String> terminalArgs = new ArrayList<String>();

            terminalArgs.addAll(Arrays.asList(
                    dorunScript.getAbsolutePath(),
                    "-p", terminalInfo.getPrompt(terminal), // NOI18N
                    "-x", shFile)); // NOI18N

            List<String> command = terminalInfo.wrapCommand(
                    info.getExecutionEnvironment(),
                    terminal,
                    terminalArgs);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(workingDirectory);

            LOG.log(Level.FINEST, "Command: " + command); // NOI18N

            final MacroMap env = info.getEnvironment().clone();

            // setup DISPLAY variable for MacOS...
            if (isMacOS) {
                ProcessBuilder pb1 = new ProcessBuilder("/bin/sh", "-c", "/bin/echo $DISPLAY"); // NOI18N
                Process p1 = pb1.start();
                int status = p1.waitFor();
                String display = null;

                if (status == 0) {
                    display = ProcessUtils.readProcessOutputLine(p1);
                }

                if (display == null || "".equals(display)) { // NOI18N
                    display = ":0.0"; // NOI18N
                }

                pb.environment().put("DISPLAY", display); // NOI18N
            }

            env.appendPathVariable("PATH", hostInfo.getPath()); // NOI18N

            if (!env.isEmpty()) {
                // TODO: FIXME (?)
                // Do PATH normalization on Windows....
                // Problem here is that this is done for PATH env. variable only!

                if (isWindows) {
                    env.put("PATH", "/bin:/usr/bin:" + WindowsSupport.getInstance().convertToAllShellPaths(env.get("PATH"))); // NOI18N
                }

                OutputStream fos = new FileOutputStream(envFileFile);
                EnvWriter ew = new EnvWriter(fos);
                ew.write(env);
                fos.close();

                /**
                 * IZ#176361: Sometimes when external terminal is used,
                 * execution fails because env file is not found
                 *
                 * TODO: ???
                 * What is it? FS caches? How to deal with this?
                 */
                int attempts = 10;
                boolean exists = false;

                while (attempts > 0) {
                    exists = HostInfoUtils.fileExists(ExecutionEnvironmentFactory.getLocal(), shFileFile.getPath()) &
                            HostInfoUtils.fileExists(ExecutionEnvironmentFactory.getLocal(), envFileFile.getPath());

                    if (exists) {
                        break;
                    }

                    LOG.warning("env or sh file is not available yet... waiting [" + attempts + "]"); // NOI18N
                    Thread.sleep(50);
                    attempts--;
                }


                if (LOG.isLoggable(Level.FINEST)) {
                    env.dump(System.err);
                }
            }

            processError = new ByteArrayInputStream(new byte[0]);

            Process terminalProcess = pb.start();

            creation_ts = System.nanoTime();

            waitPID(terminalProcess, pidFileFile);
        } catch (Throwable ex) {
            String msg = (ex.getMessage() == null ? ex.toString() : ex.getMessage()) + "\n"; // NOI18N
            processError = new ByteArrayInputStream(msg.getBytes());
            resultFile = null;
            throw ex;
        } finally {
            if (pidFileFile != null) {
                pidFileFile.delete();
            }
            if (envFileFile != null) {
                envFileFile.delete();
            }
            if (shFileFile != null) {
                shFileFile.delete();
            }
        }
    }

    private int getPIDNoException() {
        int pid = -1;

        try {
            pid = getPID();
        } catch (Exception ex) {
        }

        return pid;
    }

    @Override
    public void cancel() {
        int pid = getPIDNoException();

        if (pid < 0) {
            // Process even was not started ...
            return;
        }

        CommonTasksSupport.sendSignal(info.getExecutionEnvironment(), pid, Signal.SIGTERM, null);
    }

    @Override
    public int waitResult() throws InterruptedException {
        int pid = getPIDNoException();

        if (pid < 0) {
            // Process was not even started
            return -1;
        }

        if (isWindows || isMacOS) {
            int rc = 0;
            while (rc == 0) {
                try {
                    rc = CommonTasksSupport.sendSignal(info.getExecutionEnvironment(), pid, Signal.NULL, null).get();
                } catch (ExecutionException ex) {
                    log.log(Level.FINEST, "", ex); // NOI18N
                    rc = -1;
                }

                Thread.sleep(300);
            }
        } else {
            File f = new File("/proc/" + pid); // NOI18N

            while (f.exists()) {
                Thread.sleep(300);
            }
        }

        if (resultFile == null) {
            return -1;
        }

        int exitCode = -1;

        BufferedReader statusReader = null;

        try {
            int attempts = 10;
            while (attempts-- > 0) {
                if (resultFile.exists() && resultFile.length() > 0) {
                    statusReader = new BufferedReader(new FileReader(resultFile));
                    String exitCodeString = statusReader.readLine();
                    if (exitCodeString != null) {
                        exitCode = Integer.parseInt(exitCodeString.trim());
                    }
                    break;
                }

                Thread.sleep(500);
            }
        } catch (InterruptedIOException ex) {
            throw new InterruptedException();
        } catch (IOException ex) {
        } catch (NumberFormatException ex) {
        } finally {
            if (statusReader != null) {
                try {
                    statusReader.close();
                } catch (IOException ex) {
                }
            }
        }

        return exitCode;
    }

    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        return processOutput;
    }

    @Override
    public InputStream getErrorStream() {
        return processError;
    }

    private void waitPID(Process termProcess, File pidFile) throws IOException {
        int count = 10;

        while (!isInterrupted()) {
            /**
             * The following sleep appears after an attempt to support konsole
             * KDE4. This was done to give some time for external process to
             * write information about process' PID to the pidfile and not to
             * get to termProcess.exitValue() too eraly...
             * Currently there are no means on KDE4 to start konsole in
             * 'not-background' mode.
             * An attempt to use --nofork fails when start konsole from jvm
             * (see http://www.nabble.com/Can%27t-use---nofork-for-KUniqueApplications-from-another-kde-process-td21047022.html)
             * So termProcess exits immediately...
             *
             * Also this sleep is justifable because this doesn't make any sense
             * to check for a pid file too often.
             *
             */
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                continue;
            }

            if (pidFile.exists() && pidFile.length() > 0) {
                InputStream pidIS = null;
                try {
                    pidIS = new FileInputStream(pidFile);
                    readPID(pidIS);
                } finally {
                    if (pidIS != null) {
                        pidIS.close();
                    }
                }
                break;
            }

            if (count-- == 0) {
                // PID is not available after limit attempts
                // Wrapping everything up...
                termProcess.destroy();
                throw new IOException(loc("TerminalLocalNativeProcess.terminalRunFailed.text")); // NOI18N
            }

            try {
                int result = termProcess.exitValue();

                if (result != 0) {
                    String err = ProcessUtils.readProcessErrorLine(termProcess);
                    log.info(loc("TerminalLocalNativeProcess.terminalFailed.text")); // NOI18N
                    log.info(err);
                    throw new IOException(err);
                }

                // No exception - means process is finished..
                break;
            } catch (IllegalThreadStateException ex) {
                // expected ... means that terminal process exists
            }
        }
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(TerminalLocalNativeProcess.class, key, params);
    }
}
