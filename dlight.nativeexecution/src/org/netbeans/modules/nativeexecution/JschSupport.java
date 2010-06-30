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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.support.Logger;

/**
 *
 * @author ak119685
 */
public final class JschSupport {

    private static final int JSCH_CONNECTION_TIMEOUT = Integer.getInteger("jsch.connection.timeout", 10000); // NOI18N
    private final static java.util.logging.Logger log = Logger.getInstance();

    private JschSupport() {
    }

    public static synchronized ChannelStreams startCommand(final ExecutionEnvironment env, final String command, final ChannelParams params)
            throws IOException, JSchException {

        JSchWorker<ChannelStreams> worker = new JSchWorker<ChannelStreams>() {

            @Override
            public ChannelStreams call() throws JSchException, IOException {
                Session session = ConnectionManagerAccessor.getDefault().getConnectionSession(env, true);
                ChannelExec echannel = (ChannelExec) session.openChannel("exec"); // NOI18N
                echannel.setCommand(command);
                echannel.setXForwarding(params == null ? false : params.x11forward);
                echannel.connect(JSCH_CONNECTION_TIMEOUT);

                return new ChannelStreams(echannel,
                        echannel.getInputStream(),
                        echannel.getErrStream(),
                        echannel.getOutputStream());
            }

            @Override
            public String toString() {
                return command;
            }
        };

        return start(worker, env, 2);
    }

    public static synchronized ChannelStreams startLoginShellSession(final ExecutionEnvironment env) throws IOException, JSchException {
        JSchWorker<ChannelStreams> worker = new JSchWorker<ChannelStreams>() {

            @Override
            public ChannelStreams call() throws JSchException, IOException {
                Session session = ConnectionManagerAccessor.getDefault().getConnectionSession(env, true);
                ChannelShell shell = (ChannelShell) session.openChannel("shell"); // NOI18N
                shell.setPty(false);
                shell.connect(JSCH_CONNECTION_TIMEOUT);

                return new ChannelStreams(shell,
                        shell.getInputStream(),
                        new ByteArrayInputStream(new byte[0]),
                        shell.getOutputStream());
            }

            @Override
            public String toString() {
                return "shell session for " + env.getDisplayName(); // NOI18N
            }
        };

        return start(worker, env, 2);
    }

    private static synchronized ChannelStreams start(final JSchWorker<ChannelStreams> worker, final ExecutionEnvironment env, final int attempts) throws IOException, JSchException {
        int retry = attempts;

        while (retry-- > 0) {
            try {
                return worker.call();
            } catch (JSchException ex) {
                String message = ex.getMessage();
                Throwable cause = ex.getCause();
                if (cause != null && cause instanceof NullPointerException) {
                    // Jsch bug... retry?
                    log.log(Level.INFO, "JSch exception opening channel to " + env + ". Retrying", ex); // NOI18N
                } else if ("java.io.InterruptedIOException".equals(message)) { // NOI18N
                    log.log(Level.INFO, "JSch exception opening channel to " + env + ". Retrying in 0.5 seconds", ex); // NOI18N
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex1) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else if ("channel is not opened.".equals(message)) { // NOI18N
                    log.log(Level.INFO, "JSch exception opening channel to " + env + ". Reconnecting and retrying", ex); // NOI18N
                    // Looks like in this case an attempt to
                    // just re-open a channel will fail - so create a new session
                    ConnectionManagerAccessor.getDefault().reconnect(env);
                } else {
                    throw ex;
                }
            } catch (NullPointerException npe) {
                // Jsch bug... retry? ;)
                log.log(Level.FINE, "Exception from JSch", npe); // NOI18N
            }
        }

        throw new IOException("Failed to execute " + worker.toString()); // NOI18N
    }

    public final static class ChannelStreams {

        public final InputStream out;
        public final InputStream err;
        public final OutputStream in;
        public final Channel channel;

        public ChannelStreams(Channel channel, InputStream out,
                InputStream err, OutputStream in) {
            this.channel = channel;
            this.out = out;
            this.err = err;
            this.in = in;
        }
    }

    public static final class ChannelParams {

        private boolean x11forward = false;

        public void setX11Forwarding(boolean forward) {
            this.x11forward = forward;
        }
    }

    private static interface JSchWorker<T> {

        T call() throws JSchException, IOException;
    }
}
