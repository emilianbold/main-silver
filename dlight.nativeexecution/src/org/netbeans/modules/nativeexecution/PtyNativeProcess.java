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
package org.netbeans.modules.nativeexecution;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.pty.Pty;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.Signal;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.pty.PtyUtility;

/**
 *
 * @author ak119685
 */
public final class PtyNativeProcess extends AbstractNativeProcess {

    private String tty;
    private AbstractNativeProcess delegate = null;

    public PtyNativeProcess(NativeProcessInfo info) {
        super(info);
    }

    public String getTTY() {
        return tty;
    }

    @Override
    protected void create() throws Throwable {
        ExecutionEnvironment env = info.getExecutionEnvironment();
        Pty pty = info.getPty();

        String executable = PtyUtility.getInstance().getPath(env);
        List<String> newArgs = new ArrayList<String>();

        if (pty != null) {
            newArgs.add("-p"); // NOI18N
            newArgs.add(pty.getSlaveName());
        }

        String processExecutable = info.getExecutable();

        if (hostInfo.getOSFamily() == OSFamily.WINDOWS) {
            // pty requires Unix style executable path
            processExecutable = WindowsSupport.getInstance().convertToShellPath(processExecutable);
        }

        newArgs.add(processExecutable);
        newArgs.addAll(info.getArguments());

        // TODO: Clone Info!!!!
        info.setExecutable(executable);
        info.setArguments(newArgs.toArray(new String[0]));

        // Listeners...
        // listeners are copied already in super()
        // and never accessed via info anymore...
        // so when we change listeners here,
        // this change has effect on delegate only...

        if (info.getListeners() != null) {
            info.getListeners().clear();
        }

        if (env.isLocal()) {
            delegate = new LocalNativeProcess(info);
        } else {
            delegate = new RemoteNativeProcess(info);
        }

        delegate.createAndStart();

        if (pty != null) {
            setInputStream(pty.getInputStream());
            setOutputStream(pty.getOutputStream());
        } else {
            setInputStream(delegate.getInputStream());
            setOutputStream(delegate.getOutputStream());
        }

        setErrorStream(delegate.getErrorStream());

        String pidLine = null;
        String ttyLine = null;

        String line = readLine(delegate.getInputStream());

        if (line != null && line.startsWith("PID=")) { // NOI18
            pidLine = line.substring(4);
        }

        line = readLine(delegate.getInputStream());

        if (line != null && line.startsWith("TTY=")) { // NOI18
            ttyLine = line.substring(4);
        }

        if (pidLine == null || ttyLine == null) {
            String error = ProcessUtils.readProcessErrorLine(this);
            throw new IOException("Unable to start pty process: " + error); // NOI18N
        }

        tty = ttyLine;

        ByteArrayInputStream bis = new ByteArrayInputStream(pidLine.getBytes());
        readPID(bis);
    }

    @Override
    protected synchronized void cancel() {
        int pid = 0;

        try {
            pid = getPID();
        } catch (IOException ex) {
        }

        if (pid > 0) {
            try {
                CommonTasksSupport.sendSignal(info.getExecutionEnvironment(), pid, Signal.SIGKILL, null).get();
            } catch (InterruptedException ex) {
            } catch (ExecutionException ex) {
            }
        }
    }

    @Override
    protected int waitResult() throws InterruptedException {
        if (delegate == null) {
            return 1;
        }

        int result = delegate.waitResult();

        return result;
    }

    private String readLine(final InputStream is) throws IOException {
        int c = -1;
        StringBuilder sb = new StringBuilder(20);

        while (!isInterrupted()) {
            c = is.read();

            if (c < 0 || c == '\n') {
                break;
            }

            sb.append((char) c);
        }

        return sb.toString().trim();
    }
}
