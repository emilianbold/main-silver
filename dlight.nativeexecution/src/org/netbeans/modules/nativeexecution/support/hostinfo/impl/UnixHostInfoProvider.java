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
package org.netbeans.modules.nativeexecution.support.hostinfo.impl;

import com.jcraft.jsch.JSchException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.netbeans.modules.nativeexecution.JschSupport.ChannelStreams;
import org.netbeans.modules.nativeexecution.support.hostinfo.HostInfoProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.JschSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.support.EnvReader;
import org.netbeans.modules.nativeexecution.support.InstalledFileLocatorProvider;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = org.netbeans.modules.nativeexecution.support.hostinfo.HostInfoProvider.class, position = 100)
public class UnixHostInfoProvider implements HostInfoProvider {

    private static final String PATH_VAR = "PATH"; // NOI18N
    private static final String PATH_TO_PREPEND = "/bin:/usr/bin"; // NOI18N
    private static final java.util.logging.Logger log = Logger.getInstance();
    private static final File hostinfoScript;

    static {
        InstalledFileLocator fl = InstalledFileLocatorProvider.getDefault();
        hostinfoScript = fl.locate("bin/nativeexecution/hostinfo.sh", null, false); // NOI18N

        if (hostinfoScript == null) {
            log.severe("Unable to find hostinfo.sh script!"); // NOI18N
        }
    }

    @Override
    public HostInfo getHostInfo(ExecutionEnvironment execEnv) throws IOException {
        if (hostinfoScript == null) {
            return null;
        }

        boolean isLocal = execEnv.isLocal();

        if (isLocal && Utilities.isWindows()) {
            return null;
        }

        final Properties info;
        final Map<String, String> environment;

        if (execEnv.isLocal()) {
            environment = new ProcessBuilder("").environment(); // NOI18N
            info = getLocalHostInfo();
        } else {
            environment = new HashMap<String, String>();
            info = getRemoteHostInfo(execEnv, environment);
        }

        // Add /bin:/usr/bin
        String path = PATH_TO_PREPEND;

        if (environment.containsKey(PATH_VAR)) {
            path += ":" + environment.get(PATH_VAR); // NOI18N
        }

        environment.put(PATH_VAR, path); // NOI18N

        return HostInfoFactory.newHostInfo(info, environment);
    }

    private Properties getLocalHostInfo() throws IOException {
        Properties hostInfo = new Properties();

        try {
            ProcessBuilder pb = new ProcessBuilder("/bin/sh", // NOI18N
                    hostinfoScript.getAbsolutePath());

            File tmpDirFile = new File(System.getProperty("java.io.tmpdir")); // NOI18N
            String tmpDirBase = tmpDirFile.getCanonicalPath();

            pb.environment().put("TMPBASE", tmpDirBase); // NOI18N
            pb.environment().put("NB_KEY", HostInfoFactory.getNBKey()); // NOI18N

            Process hostinfoProcess = pb.start();

            // In case of some error goes to stderr, waitFor() will not exit
            // until error stream is read/closed.
            // So this case sould be handled.

            // We safely can do this in the same thread (in this exact case)

            List<String> errorLines = ProcessUtils.readProcessError(hostinfoProcess);
            int result = hostinfoProcess.waitFor();

            if (result != 0) {
                log.log(Level.INFO, "stderr:", errorLines.toArray(new String[0])); // NOI18N
                throw new IOException(hostinfoScript + " rc == " + result); // NOI18N
            }

            hostInfo.load(hostinfoProcess.getInputStream());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("HostInfo receiving for localhost interrupted " + ex); // NOI18N
        }

        return hostInfo;
    }

    // synchronized = attempt to workaround bug #184421 - random connection failures in jsch
    private synchronized Properties getRemoteHostInfo(ExecutionEnvironment execEnv, Map<String, String> environmentToFill) throws IOException {
        final ConnectionManager cm = ConnectionManager.getInstance();

        if (!cm.isConnectedTo(execEnv)) {
            ConnectionManagerAccessor access = ConnectionManagerAccessor.getDefault();
            access.doConnect(execEnv, false);
        }

        Properties hostInfo = new Properties();
        ChannelStreams sh_channels = null;

        try {
            sh_channels = JschSupport.startCommand(execEnv, "/bin/sh -s", null); // NOI18N

            long localStartTime = System.currentTimeMillis();

            OutputStream out = sh_channels.in;
            InputStream in = sh_channels.out;

            // echannel.setEnv() didn't work, so writing this directly
            out.write(("NB_KEY=" + HostInfoFactory.getNBKey() + '\n').getBytes()); // NOI18N
            out.flush();

            BufferedReader scriptReader = new BufferedReader(new FileReader(hostinfoScript));
            String scriptLine = scriptReader.readLine();

            while (scriptLine != null) {
                out.write((scriptLine + '\n').getBytes());
                out.flush();
                scriptLine = scriptReader.readLine();
            }

            scriptReader.close();
            hostInfo.load(in);

            long localEndTime = System.currentTimeMillis();

            hostInfo.put("LOCALTIME", Long.valueOf((localStartTime + localEndTime) / 2)); // NOI18N
        } catch (JSchException ex) {
            throw new IOException("Exception while receiving HostInfo for " + execEnv.toString() + ": " + ex); // NOI18N
        } finally {
            if (sh_channels != null) {
                if (sh_channels.channel != null) {
                    sh_channels.channel.disconnect();
                }
            }
        }

        ChannelStreams login_shell_channels = null;

        try {
            login_shell_channels = JschSupport.startLoginShellSession(execEnv);
            login_shell_channels.in.write(("/usr/bin/env 2>/dev/null || /bin/env 2>/dev/null\n").getBytes()); // NOI18N
            login_shell_channels.in.flush();
            login_shell_channels.in.close();

            EnvReader reader = new EnvReader(login_shell_channels.out, true);
            environmentToFill.putAll(reader.call());
        } catch (Exception ex) {
        } finally {
            if (login_shell_channels != null) {
                if (login_shell_channels.channel != null) {
                    login_shell_channels.channel.disconnect();
                }
            }
        }

        return hostInfo;
    }
}
