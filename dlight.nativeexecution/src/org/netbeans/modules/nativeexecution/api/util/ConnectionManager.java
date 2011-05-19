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
package org.netbeans.modules.nativeexecution.api.util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.jsch.JSchChannelsSupport;
import org.netbeans.modules.nativeexecution.jsch.JSchConnectionTask;
import org.netbeans.modules.nativeexecution.support.Authentication;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
import org.netbeans.modules.nativeexecution.support.ui.AuthenticationSettingsPanel;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author ak119685
 */
public final class ConnectionManager {

    private static final java.util.logging.Logger log = Logger.getInstance();
    // Actual sessions pools. One per host
    private static final ConcurrentHashMap<ExecutionEnvironment, JSchChannelsSupport> channelsSupport = new ConcurrentHashMap<ExecutionEnvironment, JSchChannelsSupport>();
    private static List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<ConnectionListener>();
    private static final Object channelsSupportLock = new Object();
    private static HashMap<ExecutionEnvironment, ConnectToAction> connectionActions = new HashMap<ExecutionEnvironment, ConnectToAction>();
    // Instance of the ConnectionManager
    private static final ConnectionManager instance = new ConnectionManager();
    private static final ConcurrentHashMap<ExecutionEnvironment, JSch> jschPool =
            new ConcurrentHashMap<ExecutionEnvironment, JSch>();
    private static final ConcurrentHashMap<ExecutionEnvironment, JSchConnectionTask> connectionTasks =
            new ConcurrentHashMap<ExecutionEnvironment, JSchConnectionTask>();
    private static final boolean UNIT_TEST_MODE = Boolean.getBoolean("nativeexecution.mode.unittest"); // NOI18N

    private final AbstractList<ExecutionEnvironment> recentConnections = new ArrayList<ExecutionEnvironment>();

    static {
        ConnectionManagerAccessor.setDefault(new ConnectionManagerAccessorImpl());

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                shutdown();
            }
        }));
    }

    private ConnectionManager() {
        // init jsch logging

        if (log.isLoggable(Level.FINEST)) {
            JSch.setLogger(new com.jcraft.jsch.Logger() {

                @Override
                public boolean isEnabled(int level) {
                    return true;
                }

                @Override
                public void log(int level, String message) {
                    log.log(Level.FINEST, "JSCH: {0}", message); // NOI18N
                }
            });
        }
        restoreRecentConnectionsList();
    }

    public void addConnectionListener(ConnectionListener listener) {
        // No need to lock - use thread-safe collection
        connectionListeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        // No need to lock - use thread-safe collection
        connectionListeners.remove(listener);
    }

    public List<ExecutionEnvironment> getRecentConnections() {
        synchronized (recentConnections) {
            return Collections.unmodifiableList(new ArrayList<ExecutionEnvironment>(recentConnections));
        }
    }

    /*package-local for test purposes*/ void updateRecentConnectionsList(ExecutionEnvironment execEnv) {
        synchronized (recentConnections) {
            recentConnections.remove(execEnv);
            recentConnections.add(0, execEnv);
            storeRecentConnectionsList();
        }
    }

    /*package-local for test purposes*/ void storeRecentConnectionsList() {
        Preferences prefs = NbPreferences.forModule(ConnectionManager.class);
        synchronized (recentConnections) {
            for (int i = 0; i < recentConnections.size(); i++) {
                prefs.put(getConnectoinsHistoryKey(i), ExecutionEnvironmentFactory.toUniqueID(recentConnections.get(i)));
            }
        }
    }

    /*package-local for test purposes*/ void restoreRecentConnectionsList() {
        Preferences prefs = NbPreferences.forModule(ConnectionManager.class);
        synchronized (recentConnections) {
            recentConnections.clear();
            int idx = 0;
            while (true) {
                String id = prefs.get(getConnectoinsHistoryKey(idx), null);
                if (id == null) {
                    break;
                }
                recentConnections.add(ExecutionEnvironmentFactory.fromUniqueID(id));
                idx++;
            }
        }
    }

    private static String getConnectoinsHistoryKey(int idx) {
        return ConnectionManager.class.getName() + "_connection.history_" + idx; //NOI18N
    }

    /** for test purposes only; package-local */ void clearRecentConnectionsList() {
        synchronized (recentConnections) {
            recentConnections.clear();
        }
    }

    private void fireConnected(ExecutionEnvironment execEnv) {
        // No need to lock - use thread-safe collection
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.connected(execEnv);
        }
        updateRecentConnectionsList(execEnv);
    }

    private void fireDisconnected(ExecutionEnvironment execEnv) {
        // No need to lock - use thread-safe collection
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.disconnected(execEnv);
        }
    }

    /**
     * Tests whether the connection with the <tt>execEnv</tt> is established or
     * not.
     * @param execEnv execution environment to test connection with.
     * @return true if connection is established or if execEnv refers to the
     * localhost environment. false otherwise.
     */
    public boolean isConnectedTo(final ExecutionEnvironment execEnv) {
        if (execEnv.isLocal()) {
            return true;
        }
        JSchChannelsSupport support = channelsSupport.get(execEnv); // it's a ConcurrentHashMap => no lock is needed
        return (support != null) && support.isConnected();
    }

    private static final int RETRY_MAX = 10;

    /**
     *
     * @param env <tt>ExecutionEnvironment</tt> to connect to.
     * @throws IOException
     * @throws CancellationException
     */
    public void connectTo(final ExecutionEnvironment env) throws IOException, CancellationException {
        if (SwingUtilities.isEventDispatchThread()) {
            // otherwise UI can hang forever
            throw new IllegalThreadStateException("Should never be called from AWT thread"); // NOI18N
        }

        if (isConnectedTo(env)) {
            return;
        }

        JSch jsch = jschPool.get(env);

        if (jsch == null) {
            jsch = new JSch();
            JSch old = jschPool.putIfAbsent(env, jsch);
            if (old != null) {
                jsch = old;
            }
        }

        if (!UNIT_TEST_MODE) {
            initiateConnection(env, jsch);
        } else {
            // Attempt to workaround "Auth fail" in tests, see IZ 190458
            // We try to reconnect up to 10 times if "Auth fail" exception happens
            int retry = RETRY_MAX;
            IOException ex = null;
            while (retry > 0) {
                try {
                    initiateConnection(env, jsch);
                    return;
                } catch (IOException e) {
                    if (!(e.getCause() instanceof JSchException)) {
                        throw e;
                    }
                    if (!"Auth fail".equals(e.getCause().getMessage())) { //NOI18N
                        throw e;
                    }
                    ex = e;
                }
                System.out.println("AUTH_FAIL: Connection failed, re-runing test " + retry); // NOI18N
                retry--;
            }
            System.out.println("AUTH_FAIL: Retry limit reached"); // NOI18N
            throw ex;
        }
    }

    private void initiateConnection(final ExecutionEnvironment env, final JSch jsch) throws IOException, CancellationException {
        JSchConnectionTask connectionTask;

        synchronized (connectionTasks) {
            connectionTask = connectionTasks.get(env);

            if (connectionTask == null) {
                connectionTask = new JSchConnectionTask(jsch, env);
                connectionTask.start();
                connectionTasks.put(env, connectionTask);
            }
        }

        final ProgressHandle ph = ProgressHandleFactory.createHandle(
                loc("ConnectionManager.Connecting", // NOI18N
                env.toString()), connectionTask);

        ph.start();

        try {
            JSchChannelsSupport cs = connectionTask.getResult();

            if (cs != null) {
                synchronized (channelsSupportLock) {
                    channelsSupport.put(env, cs);
                }
            } else {
                JSchConnectionTask.Problem problem = connectionTask.getProblem();
                switch (problem.type) {
                    case CONNECTION_CANCELLED:
                        throw new CancellationException("Connection cancelled for " + env); // NOI18N
                    default:
                        // Note that AUTH_FAIL is generated not only on bad password,
                        // but on socket timeout as well. These cases are
                        // indistinguishable based on information from JSch.
                        throw new IOException(env.getDisplayName() + ": " + problem.type.name(), problem.cause); // NOI18N
                    }
            }

            HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
            log.log(Level.FINE, "New connection established: {0} - {1}", new String[]{env.toString(), hostInfo.getOS().getName()}); // NOI18N

            fireConnected(env);
        } catch (InterruptedException ex) {
            // don't report interrupted exception
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            ph.finish();
            connectionTasks.remove(env);
        }
    }

    public static ConnectionManager getInstance() {
        return instance;
    }

    /**
     * Returns {@link Action javax.swing.Action} that can be used
     * to get connected to the {@link ExecutionEnvironment}.
     * It is guaranteed that the same Action is returned for equal execution
     * environments.
     *
     * @param execEnv - {@link ExecutionEnvironment} to connect to.
     * @param onConnect - Runnable that is executed when connection is
     *        established.
     * @return action to be used to connect to the <tt>execEnv</tt>.
     * @see Action
     */
    public synchronized AsynchronousAction getConnectToAction(
            final ExecutionEnvironment execEnv, final Runnable onConnect) {

        if (connectionActions.containsKey(execEnv)) {
            return connectionActions.get(execEnv);
        }

        ConnectToAction action = new ConnectToAction(execEnv, onConnect);

        connectionActions.put(execEnv, action);

        return action;
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(ConnectionManager.class, key, params);
    }

    private void reconnect(ExecutionEnvironment env) throws IOException {
        synchronized (channelsSupportLock) {
            if (channelsSupport.containsKey(env)) {
                try {
                    channelsSupport.get(env).reconnect(env);
                } catch (JSchException ex) {
                    throw new IOException(ex);
                }
            }
        }
    }

    public void disconnect(ExecutionEnvironment env) {
        disconnectImpl(env);
        PasswordManager.getInstance().onExplicitDisconnect(env);
    }

    private void disconnectImpl(final ExecutionEnvironment env) {
        synchronized (channelsSupportLock) {
            if (channelsSupport.containsKey(env)) {
                JSchChannelsSupport cs = channelsSupport.remove(env);
                cs.disconnect();
                fireDisconnected(env);
            }
        }
    }

    private static void shutdown() {
        log.fine("Shutting down Connection Manager");
        synchronized (channelsSupportLock) {
            for (JSchChannelsSupport cs : channelsSupport.values()) {
                cs.disconnect();
            }
        }
    }

    public ValidateablePanel getConfigurationPanel(ExecutionEnvironment env) {
        Authentication auth = Authentication.getFor(env);
        AuthenticationSettingsPanel panel = new AuthenticationSettingsPanel(auth, env != null);
        return panel;
    }

    /**
     * Do clean up for the env.
     * Any stored settings will be removed
     * @param env
     */
    public void forget(ExecutionEnvironment env) {
        if (env == null) {
            return;
        }

        Authentication.getFor(env).remove();
        jschPool.remove(env);
    }

    /**
     * onConnect will be invoked ONLY if this action has initiated a new
     * connection.
     */
    private static class ConnectToAction
            extends AbstractAction implements AsynchronousAction {

        private final static ConnectionManager cm = ConnectionManager.getInstance();
        private final ExecutionEnvironment env;
        private final Runnable onConnect;

        private ConnectToAction(ExecutionEnvironment execEnv, Runnable onConnect) {
            this.env = execEnv;
            this.onConnect = onConnect;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            NativeTaskExecutorService.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        invoke();
                    } catch (Throwable ex) {
                        log.warning(ex.getMessage());
                    }
                }
            }, "Connecting to " + env.toString()); // NOI18N
        }

        @Override
        public synchronized void invoke() throws IOException, CancellationException {
            if (cm.isConnectedTo(env)) {
                return;
            }

            cm.connectTo(env);
            onConnect.run();
        }
    }

    private static final class ConnectionManagerAccessorImpl
            extends ConnectionManagerAccessor {

        @Override
        public Channel openAndAcquireChannel(ExecutionEnvironment env, String type, boolean waitIfNoAvailable) throws InterruptedException, JSchException, IOException {
            synchronized (channelsSupportLock) {
                if (channelsSupport.containsKey(env)) {
                    JSchChannelsSupport cs = channelsSupport.get(env);
                    return cs.acquireChannel(type, waitIfNoAvailable);
                }
            }

            return null;
        }

        @Override
        public void closeAndReleaseChannel(final ExecutionEnvironment env, final Channel channel) throws JSchException {
            JSchChannelsSupport cs = null;

            synchronized (channelsSupportLock) {
                if (channelsSupport.containsKey(env)) {
                    cs = channelsSupport.get(env);
                }
            }

            if (cs != null && channel != null) {
                cs.releaseChannel(channel);
            }
        }

        @Override
        public void reconnect(final ExecutionEnvironment env) throws IOException {
            instance.reconnect(env);
        }

        @Override
        public void changeAuth(ExecutionEnvironment env, Authentication auth) {
            JSch jsch = jschPool.get(env);

            if (jsch != null) {
                try {
                    jsch.removeAllIdentity();
                } catch (JSchException ex) {
                    Exceptions.printStackTrace(ex);
                }

                try {
                    String knownHosts = auth.getKnownHostsFile();
                    if (knownHosts != null) {
                        jsch.setKnownHosts(knownHosts);
                    }
                } catch (JSchException ex) {
                    Exceptions.printStackTrace(ex);
                }

                switch (auth.getType()) {
                    case SSH_KEY:
                        try {
                            jsch.addIdentity(auth.getSSHKeyFile());
                        } catch (JSchException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                }
            }
        }
    }
}
