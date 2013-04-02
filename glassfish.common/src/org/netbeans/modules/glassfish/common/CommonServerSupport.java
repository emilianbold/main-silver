/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.common;

import org.netbeans.modules.glassfish.common.utils.Util;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.glassfish.tools.ide.admin.TaskEvent;
import org.glassfish.tools.ide.admin.TaskState;
import org.glassfish.tools.ide.admin.TaskStateListener;
import org.netbeans.modules.glassfish.common.nodes.actions.RefreshModulesCookie;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.GlassfishModule.ServerState;
import org.netbeans.modules.glassfish.spi.ServerCommand.GetPropertyCommand;
import org.netbeans.modules.glassfish.spi.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author Peter Williams
 */
public class CommonServerSupport
        implements GlassfishModule3, RefreshModulesCookie {


    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Task state listener watching __locations command execution.
     */
    private static class LocationsTaskStateListener
            implements TaskStateListener {

        /** GlassFish server support object instance. */
        final CommonServerSupport css;

        /**
         * Creates an instance of task state listener watching __locations
         * command execution.
         * <p/>
         * @param css GlassFish server support object instance.
         */
        LocationsTaskStateListener(CommonServerSupport css) {
            this.css = css;
        }

        private String adminCommandFailedMsg(String resName, String[] args) {
            String serverName = args[0];
            String command = args[1];
            String exMessage = args.length > 2 ? args[2] : null;
            return args.length > 2
                    ? NbBundle.getMessage(CommonServerSupport.class, resName,
                    args[0], args[1], args[2])
                    : NbBundle.getMessage(CommonServerSupport.class, resName,
                    args[0], args[1]);
        }
        /**
         * Callback to notify about GlassFish __locations command execution
         * state change.
         * <p/>
         * <code>String</codce> arguments passed to state listener
         * from runner:<ul>
         *   <li><code>args[0]</code> server name</li>
         *   <li><code>args[1]</code> administration command</li>
         *   <li><code>args[2]</code> exception message</li>
         *   <li><code>args[3]</code> display message in GUI</li></ul>
         * <p/>
         * @param newState New command execution state.
         * @param event    Event related to execution state change.
         * @param args     <code>String</codce> arguments passed to state
         *                 listener.
         */
        @Override
        public void operationStateChanged(
                TaskState newState, TaskEvent event,
                String[] args) {
            // Server name and command are mandatory.
            if (args.length > 1) {
                String exMessage = args.length > 2 ? args[2] : null;
                boolean display = args.length > 3
                        ? Boolean.parseBoolean(args[3]) : false;
                if (display) {
                    long lastDisplayed = css.getLatestWarningDisplayTime();
                    long currentTime = System.currentTimeMillis();
                    if (TaskState.FAILED == newState
                            && currentTime - lastDisplayed > 5000) {
                        String message;

                        switch (event) {
                            case EXCEPTION:
                                if (exMessage != null
                                        && exMessage.length() > 0) {
                                    message = adminCommandFailedMsg(
                                            "MSG_ADMIN_EXCEPTION", args);
                                } else {
                                    message = adminCommandFailedMsg(
                                            "MSG_ADMIN_FAILED", args);
                                }
                                break;
                            case LOCAL_AUTH_FAILED:
                                message = adminCommandFailedMsg(
                                        "MSG_ADMIN_LOCAL_AUTH_FAILED", args);
                                break;
                            case REMOTE_AUTH_FAILED:
                                message = adminCommandFailedMsg(
                                        "MSG_ADMIN_LOCAL_AUTH_FAILED", args);
                                break;
                            default:
                                message = adminCommandFailedMsg(
                                        "MSG_ADMIN_FAILED", args);
                        }
                        displayPopUpMessage(css, message);
                    }
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Keep trying for up to 10 minutes while server is initializing [ms]. */
    private static final int STARTUP_TIMEOUT = 600000;

    /** Delay before next try while server is initializing [ms]. */
    private static final int STARTUP_RETRY_DELAY = 2000;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Display pop up window with given message.
     * <p/>
     * Method is thread safe.
     * <p/>
     * @param css     GlassFish server support object.
     * @param message Message to be displayed.
     */
    public static void displayPopUpMessage(final CommonServerSupport css,
            final String message) {
        synchronized (css) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(message);
            DialogDisplayer.getDefault().notifyLater(nd);
            css.setLatestWarningDisplayTime(System.currentTimeMillis());
            Logger.getLogger("glassfish").log(Level.INFO, message);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Managed GlassFish instance. */
    private final GlassfishInstance instance;

    private volatile ServerState serverState = ServerState.UNKNOWN;
    private final Object stateMonitor = new Object();

    private ChangeSupport changeSupport = new ChangeSupport(this);

    private FileObject instanceFO;

    private volatile boolean startedByIde = false;

    /** Cache local/remote test for instance. */
    private transient boolean isRemote = false;

    // prevent j2eeserver from stopping an authenticated domain that
    // the IDE did not start.
    private boolean stopDisabled = false;

    private Process localStartProcess;

    CommonServerSupport(GlassfishInstance instance) {
        this.instance = instance;
        this.isRemote = instance.isRemote();
        // !PW FIXME temporary patch for JavaONE 2008 to make it easier
        // to persist per-instance property changes made by the user.
        instanceFO = getInstanceFileObject();
    }

    /**
     * Get <code>GlassfishInstance</code> object associated with this object.
     * <p/>
     * @return <code>GlassfishInstance</code> object associated with this object.
     */
    @Override
    public GlassfishInstance getInstance() {
        return this.instance;
    }

    private FileObject getInstanceFileObject() {
        FileObject dir = FileUtil.getConfigFile(
                instance.getInstanceProvider().getInstancesDirName());
        if(dir != null) {
            String instanceFN = instance
                    .getProperty(GlassfishInstanceProvider.INSTANCE_FO_ATTR);
            if(instanceFN != null) {
                return dir.getFileObject(instanceFN);
            }
        }
        return null;
    }

    @Override
    public String getPassword() {
        return instance.getPassword();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public String getInstallRoot() {
        return instance.getInstallRoot();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public String getGlassfishRoot() {
        return instance.getGlassfishRoot();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public String getDisplayName() {
        return instance.getDisplayName();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public String getDeployerUri() {
        return instance.getDeployerUri();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public String getUserName() {
        return instance.getUserName();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public String getAdminPort() {
        return instance.getHttpAdminPort();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public String getHttpPort() {
        return instance.getHttpPort();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public int getHttpPortNumber() {
        return instance.getPort();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public int getAdminPortNumber() {
        return instance.getAdminPort();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public String getHostName() {
        return instance.getProperty(HOSTNAME_ATTR);
    }

   /** @deprecated Use in <code>GlassfishInstance</code> context. */
   @Deprecated
   public String getDomainsRoot() {
        return instance.getDomainsRoot();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public String getDomainName() {
        return instance.getDomainName();
    }

    public void setServerState(final ServerState newState) {
        // Synchronized on private monitor to serialize changes in state.
        // Storage of serverState is volatile to facilitate readability of
        // current state regardless of lock status.
        boolean fireChange = false;

        synchronized (stateMonitor) {
            if(serverState != newState) {
                serverState = newState;
                fireChange = true;
            }
        }

        if(fireChange) {
            changeSupport.fireChange();
        }
    }

    boolean isStartedByIde() {
        return startedByIde;
    }

    // ------------------------------------------------------------------------
    // GlassfishModule interface implementation
    // ------------------------------------------------------------------------
    @Override
    public Map<String, String> getInstanceProperties() {
        // force the domains conversion
        getDomainsRoot();
        return Collections.unmodifiableMap(instance.getProperties());
    }

    @Override
    public GlassfishInstanceProvider getInstanceProvider() {
        return instance.getInstanceProvider();
    }

    @Override
    public boolean isRemote() {
        return isRemote;
    }

    private static final RequestProcessor RP = new RequestProcessor("CommonServerSupport - start/stop/refresh",5); // NOI18N

    @Override
    public Future<OperationState> startServer(final OperationStateListener stateListener, ServerState endState) {
        Logger.getLogger("glassfish").log(Level.FINEST, "CSS.startServer called on thread \"{0}\"", Thread.currentThread().getName()); // NOI18N
        OperationStateListener startServerListener = new StartOperationStateListener(endState);
        VMIntrospector vmi = Lookups.forPath(Util.GF_LOOKUP_PATH).lookup(VMIntrospector.class);
        FutureTask<OperationState> task = new FutureTask<OperationState>(
                new StartTask(this, getRecognizers(), vmi,
                              (String[])(endState == ServerState.STOPPED_JVM_PROFILER ? new String[]{""} : null),
                              startServerListener, stateListener));
        RP.post(task);
        return task;
    }

    private List<Recognizer> getRecognizers() {
        List<Recognizer> recognizers;
        Collection<? extends RecognizerCookie> cookies = 
                instance.localLookup().lookupAll(RecognizerCookie.class);
        if(!cookies.isEmpty()) {
            recognizers = new LinkedList<Recognizer>();
            for(RecognizerCookie cookie: cookies) {
                recognizers.addAll(cookie.getRecognizers());
            }
            recognizers = Collections.unmodifiableList(recognizers);
        } else {
            recognizers = Collections.emptyList();
        }
        return recognizers;
    }


    @Override
    public Future<OperationState> stopServer(final OperationStateListener stateListener) {
        Logger.getLogger("glassfish").log(Level.FINEST, "CSS.stopServer called on thread \"{0}\"", Thread.currentThread().getName()); // NOI18N
        OperationStateListener stopServerListener = new OperationStateListener() {
            @Override
            public void operationStateChanged(OperationState newState, String message) {
                if(newState == OperationState.RUNNING) {
                    setServerState(ServerState.STOPPING);
                } else if(newState == OperationState.COMPLETED) {
                    setServerState(ServerState.STOPPED);
                } else if(newState == OperationState.FAILED) {
                    // possible bug - what if server was started in other mode than RUNNING
                    setServerState(ServerState.RUNNING);
                }
            }
        };
        FutureTask<OperationState> task;
        if (!isRemote() || !Util.isDefaultOrServerTarget(instance.getProperties())) {
            if (getServerState() == ServerState.STOPPED_JVM_PROFILER) {
                task = new FutureTask<OperationState>(
                        new StopProfilingTask(this, stateListener));
            } else {
                task = new FutureTask<OperationState>(
                        new StopTask(this, stopServerListener, stateListener));
            }
        // prevent j2eeserver from stopping a server it did not start.
        } else {
            task = new FutureTask<OperationState>(new NoopTask(this,stopServerListener,stateListener));
        }
        if (stopDisabled) {
            stopServerListener.operationStateChanged(OperationState.COMPLETED, "");
            if (null != stateListener) {
                stateListener.operationStateChanged(OperationState.COMPLETED, "");
            }
            return task;
        }
        RP.post(task);
        return task;
    }

    
    @Override
    public Future<OperationState> restartServer(OperationStateListener stateListener) {
        Logger.getLogger("glassfish").log(Level.FINEST, "CSS.restartServer called on thread \"{0}\"", Thread.currentThread().getName()); // NOI18N
        FutureTask<OperationState> task = new FutureTask<OperationState>(
                new RestartTask(this, stateListener));
        RP.post(task);
        return task;
    }

    @Override
    public Future<OperationState> deploy(final OperationStateListener stateListener,
            final File application, final String name) {
        return deploy(stateListener, application, name, null);
    }

    @Override
    public Future<OperationState> deploy(final OperationStateListener stateListener,
            final File application, final String name, final String contextRoot) {
        return deploy(stateListener, application, name, contextRoot, null);
    }

    @Override
    public Future<OperationState> deploy(final OperationStateListener stateListener,
            final File application, final String name, final String contextRoot, Map<String,String> properties) {
        return deploy(stateListener, application, name, contextRoot, null, new File[0]);
    }

    @Override
    public Future<OperationState> deploy(OperationStateListener stateListener,
            File application, String name, String contextRoot,
            Map<String, String> properties, File[] libraries) {
        CommandRunner mgr = new CommandRunner(
                GlassFishStatus.isReady(instance, false),
                getCommandFactory(), instance, stateListener);
        return mgr.deploy(application, name, contextRoot, properties,
                libraries);
    }

    @Override
    public Future<OperationState> redeploy(
            final OperationStateListener stateListener,
            final String name, boolean resourcesChanged) {
        return redeploy(stateListener, name, null, resourcesChanged);
    }

    @Override
    public Future<OperationState> redeploy(
            final OperationStateListener stateListener,
            final String name, final String contextRoot,
            boolean resourcesChanged) {
        return redeploy(stateListener, name, contextRoot, new File[0],
                resourcesChanged);
    }

    @Override
    public Future<OperationState> redeploy(OperationStateListener stateListener,
    String name, String contextRoot, File[] libraries,
    boolean resourcesChanged) {
        CommandRunner mgr = new CommandRunner(
                GlassFishStatus.isReady(instance, false),
                getCommandFactory(), instance, stateListener);
        return mgr.redeploy(name, contextRoot, libraries, resourcesChanged);
    }

    @Override
    public Future<OperationState> undeploy(
            final OperationStateListener stateListener, final String name) {
        CommandRunner mgr = new CommandRunner(
                GlassFishStatus.isReady(instance, false),
                getCommandFactory(), instance, stateListener);
        return mgr.undeploy(name);
    }

    @Override
    public Future<OperationState> enable(
            final OperationStateListener stateListener, final String name) {
        CommandRunner mgr = new CommandRunner(
                GlassFishStatus.isReady(instance, false),
                getCommandFactory(), instance, stateListener);
        return mgr.enable(name);
    }
    @Override
    public Future<OperationState> disable(
            final OperationStateListener stateListener, final String name) {
        CommandRunner mgr = new CommandRunner(
                GlassFishStatus.isReady(instance, false),
                getCommandFactory(), instance, stateListener);
        return mgr.disable(name);
    }

    @Override
    public Future<OperationState> execute(ServerCommand command) {
        CommandRunner mgr = new CommandRunner(
                GlassFishStatus.isReady(instance, false),
                getCommandFactory(), instance);
        return mgr.execute(command);
    }

    private Future<OperationState> execute(boolean irr, ServerCommand command) {
        CommandRunner mgr = new CommandRunner(irr, getCommandFactory(),
                instance);
        return mgr.execute(command);
    }
    private Future<OperationState> execute(boolean irr, ServerCommand command,
            OperationStateListener... osl) {
        CommandRunner mgr = new CommandRunner(irr, getCommandFactory(),
                instance, osl);
        return mgr.execute(command);
    }

    @Override
    public AppDesc [] getModuleList(String container) {
        CommandRunner mgr = new CommandRunner(
                GlassFishStatus.isReady(instance, false),
                getCommandFactory(), instance);
        int total = 0;
        Map<String, List<AppDesc>> appMap = mgr.getApplications(container);
        Collection<List<AppDesc>> appLists = appMap.values();
        for(List<AppDesc> appList: appLists) {
            total += appList.size();
        }
        AppDesc [] result = new AppDesc[total];
        int index = 0;
        for(List<AppDesc> appList: appLists) {
            for(AppDesc app: appList) {
                result[index++] = app;
            }
        }
        return result;
    }

    @Override
    public Map<String, ResourceDesc> getResourcesMap(String type) {
        CommandRunner mgr = new CommandRunner(
                GlassFishStatus.isReady(instance, false),
                getCommandFactory(), instance);
        Map<String, ResourceDesc> resourcesMap
                = new HashMap<String, ResourceDesc>();
        List<ResourceDesc> resourcesList = mgr.getResources(type);
        for (ResourceDesc resource : resourcesList) {
            resourcesMap.put(resource.getName(), resource);
        }
        return resourcesMap;
    }

    @Override
    public ServerState getServerState() {
        if (serverState == ServerState.UNKNOWN) {
            RequestProcessor.Task task = refresh();
            task.waitFinished();
        }
        return serverState;
    }

    @Override
    public void addChangeListener(final ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(final ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public String setEnvironmentProperty(final String name, final String value,
            final boolean overwrite) {
        String result;

        synchronized (instance.getProperties()) {
            result = instance.getProperty(name);
            if(result == null || overwrite == true) {
                instance.putProperty(name, value);
                setInstanceAttr(name, value);
                result = value;
            }
        }

        return result;
    }

    // ------------------------------------------------------------------------
    // bookkeeping & impl managment, not exposed via interface.
    // ------------------------------------------------------------------------
    void setProperty(final String key, final String value) {
        instance.putProperty(key, value);
    }

    void getProperty(String key) {
        instance.getProperty(key);
    }

    boolean setInstanceAttr(String name, String value) {
        boolean retVal = false;
        if(instanceFO == null || !instanceFO.isValid()) {
            instanceFO = getInstanceFileObject();
        }
        if(instanceFO != null && instanceFO.canWrite()) {
            try {
                Object currentValue = instanceFO.getAttribute(name);
                if (null != currentValue && currentValue.equals(value)) {
                    // do nothing
                } else {
                    instanceFO.setAttribute(name, value);
                }
                retVal = true;
            } catch(IOException ex) {
                Logger.getLogger("glassfish").log(Level.WARNING,
                        "Unable to save attribute " + name + " in " + instanceFO.getPath() + " for " + getDeployerUri(), ex); // NOI18N
            }
        } else {
            if (null == instanceFO)
                Logger.getLogger("glassfish").log(Level.WARNING,
                        "Unable to save attribute {0} for {1} in {3}. Instance file is writable? {2}",
                        new Object[]{name, getDeployerUri(), false, "null"}); // NOI18N
            else
                Logger.getLogger("glassfish").log(Level.WARNING,
                        "Unable to save attribute {0} for {1} in {3}. Instance file is writable? {2}",
                        new Object[]{name, getDeployerUri(), instanceFO.canWrite(), instanceFO.getPath()}); // NOI18N
        }
        return retVal;
    }

    void setFileObject(FileObject fo) {
        instanceFO = fo;
    }

   
    public static boolean isRunning(final String host, final int port,
            final String name, final int timeout) {
        if(null == host)
            return false;

        try {
            InetSocketAddress isa = new InetSocketAddress(host, port);
            Socket socket = new Socket();
            Logger.getLogger("glassfish-socket-connect-diagnostic").log(
                    Level.FINE, "Using socket.connect", new Exception());
            socket.connect(isa, timeout);
            socket.setSoTimeout(timeout);
            try {
                socket.close();
            } catch (IOException ioe) {
                Logger.getLogger("glassfish").log(
                        Level.INFO, "Socket closing failed: {0}",
                        ioe.getMessage());
            }
            return true;
        } catch (java.net.ConnectException ex) {
            return false;
        } catch (java.net.SocketTimeoutException ste) {
            return false;
        } catch (IOException ioe) {
            String message = NbBundle.getMessage(CommonServerSupport.class,
                    name == null || "".equals(name.trim())
                    ? "MSG_FLAKEY_NETWORK" : "MSG_FLAKEY_NETWORK2",
                    host, Integer.toString(port), ioe.getLocalizedMessage());
            NotifyDescriptor nd = new NotifyDescriptor.Message(message);
            DialogDisplayer.getDefault().notifyLater(nd);
            Logger.getLogger("glassfish").log(Level.INFO,
                    "Evidence of network flakiness: {0}", ioe.getMessage());
            return false;
        }
    }

    public static boolean isRunning(final String host, final int port,
            final String name) {
        return isRunning(host, port, name, 2000);
    }

////////////////////////////////////////////////////////////////////////////////
// MOVED TO GlassFishStatus                                             START //
////////////////////////////////////////////////////////////////////////////////

//    public boolean isReallyRunning() {
//        return isReady(false,30,TimeUnit.SECONDS);
//    }
//
//    /**
//     * Suspend thread execution for {@link #STARTUP_RETRY_DELAY} ms.
//     * <p/>
//     * Internal {@link #isReady(boolean, int, TimeUnit)} helper.
//     * <p/>
//     * @param begTm {@link #isReady(boolean, int, TimeUnit)} execution
//     *              start time.
//     * @param actTm Actual time.
//     * @param tries Number of retries.
//     */
//    private void retrySleep(long begTm, long actTm, int tries) {
//        Logger.getLogger("glassfish").log(Level.FINEST,
//                "Keep trying while server is not yet ready. Time until giving it up: {0}, retry {1}",
//                new Object[]{
//                    Long.toString(STARTUP_TIMEOUT - actTm + begTm),
//                    Integer.toString(tries)});
//        try {
//            Thread.sleep(STARTUP_RETRY_DELAY);
//        } catch (InterruptedException ie) {
//            Logger.getLogger("glassfish").log(Level.INFO,
//                    "Thread sleep interrupted: {0}", ie.getLocalizedMessage());
//        }
//    }
//
//    /**
//     * Execute Location command on GlassFish instance.
//     * <p/>
//     * Internal {@link #isReady(boolean, int, TimeUnit)} helper.
//     * <p/>
//     * @param command Location command entity.
//     * @param timeout Execution timeout value.
//     * @param units   Execution timeout units.
//     * @return Location command asynchronous execution future result object.
//     */
//    private Future<ResultMap<String, String>> execLocation(
//            CommandLocation command, int timeout, TimeUnit units) {
//        Logger.getLogger("glassfish").log(Level.FINEST,
//                "Running admin interface Location command on {0} with timeout {1} [{2}]",
//                new Object[]{instance.getName(), Integer.toString(timeout),
//                    units.toString()});
//        if (isRemote) {
//            TaskStateListener[] listenersLocation = new TaskStateListener[]{
//                new LocationsTaskStateListener(this)};
//            return ServerAdmin.<ResultMap<String, String>>exec(
//                    instance, new CommandLocation(), new IdeContext(),
//                    listenersLocation);
//        } else {
//            return ServerAdmin.<ResultMap<String, String>>exec(
//                    instance, new CommandLocation(), new IdeContext());
//        }
//    }
//
//    /**
//     * Execute Version command on GlassFish instance.
//     * <p/>
//     * Internal {@link #isReady(boolean, int, TimeUnit)} helper.
//     * <p/>
//     * @param command Version command entity.
//     * @param timeout Execution timeout value.
//     * @param units   Execution timeout units.
//     * @return Version command asynchronous execution future result object.
//     */
//    private Future<ResultString> execVersion( CommandVersion command,
//            int timeout, TimeUnit units) {
//        Logger.getLogger("glassfish").log(Level.FINEST,
//                "Running admin interface Version command on {0} with timeout {1} [{2}]",
//                new Object[]{instance.getName(), Integer.toString(timeout),
//                    units.toString()});
//        return ServerAdmin.<ResultString>exec(
//                instance, command, new IdeContext());
//    }
//
//    /**
//     * Wait for Location command execution result and return it.
//     * <p/>
//     * Command execution timeout should be specified.
//     * Internal {@link #isReady(boolean, int, TimeUnit)} helper.
//     * <p/>
//     * @param futureLocation Location command asynchronous execution
//     *                       future result object
//     * @param command        Location command entity.
//     * @param timeout        Execution timeout value (only for logging).
//     * @param units          Execution timeout units (only for logging).
//     * @param maxtries       Maximum retries allowed (only for logging).
//     * @param tries          Current retries count (only for logging).
//     * @return Location command asynchronous execution final result.
//     * @throws InterruptedException If the current thread was interrupted
//     *                              while waiting
//     * @throws ExecutionException   If the Location command asynchronous
//     *                              execution threw an exception.
//     * @throws TimeoutException     If the wait timed out.
//     */
//    private ResultMap<String, String> resultLocation(
//            Future<ResultMap<String, String>> futureLocation,
//            CommandLocation command, int timeout, TimeUnit units,
//            int maxtries, int tries)
//            throws InterruptedException, ExecutionException, TimeoutException {
//        try {
//            return futureLocation.get(timeout, units);
//        } catch (TimeoutException ex) {
//            Logger.getLogger("glassfish").log(Level.INFO,
//                    "Server {0} {1}:{2} user {3}: {4} timed out. Try {5} of {6}.",
//                    new Object[]{instance.getName(), instance.getHost(),
//                        instance.getHttpAdminPort(), instance.getAdminUser(),
//                        command.getCommand(), Integer.toString(tries),
//                        Integer.toString(maxtries)});
//            throw ex;
//        } catch (InterruptedException ex) {
//            Logger.getLogger("glassfish").log(Level.INFO,
//                    "Server {0} {1}:{2} user {3}: {4} interrupted. Try {5} of {6}.",
//                    new Object[]{instance.getName(), instance.getHost(),
//                        instance.getHttpAdminPort(), instance.getAdminUser(),
//                        command.getCommand(), Integer.toString(tries),
//                        Integer.toString(maxtries)});
//            throw ex;
//        } catch (ExecutionException ex) {
//            Logger.getLogger("glassfish").log(Level.INFO,
//                    "Server {0} {1}:{2} user {3}: {4} threw an exception. Try {5} of {6}.",
//                    new Object[]{instance.getName(), instance.getHost(),
//                        instance.getHttpAdminPort(), instance.getAdminUser(),
//                        command.getCommand(), Integer.toString(tries),
//                        Integer.toString(maxtries)});
//            throw ex;
//        }
//    }
//
//    /**
//     * Wait for Version command execution result and return it.
//     * <p/>
//     * Command execution timeout should be specified.
//     * Internal {@link #isReady(boolean, int, TimeUnit)} helper.
//     * <p/>
//     * @param futureVersion Version command asynchronous execution
//     *                      future result object
//     * @param command       Version command entity.
//     * @param timeout       Execution timeout value (only for logging).
//     * @param units         Execution timeout units (only for logging).
//     * @param maxtries      Maximum retries allowed (only for logging).
//     * @param tries         Current retries count (only for logging).
//     * @return Version command asynchronous execution final result.
//     * @throws InterruptedException If the current thread was interrupted
//     *                              while waiting
//     * @throws ExecutionException   If the Version command asynchronous
//     *                              execution threw an exception.
//     * @throws TimeoutException     If the wait timed out.
//     */
//    private ResultString resultVersion(
//            Future<ResultString> futureVersion,
//            CommandVersion command, int timeout, TimeUnit units,
//            int maxtries, int tries)
//            throws InterruptedException, ExecutionException, TimeoutException {
//        try {
//            return futureVersion.get(timeout, units);
//        } catch (TimeoutException ex) {
//            Logger.getLogger("glassfish").log(Level.INFO,
//                    "Server {0} {1}:{2} user {3}: {4} timed out. Try {5} of {6}.",
//                    new Object[]{instance.getName(), instance.getHost(),
//                        instance.getHttpAdminPort(), instance.getAdminUser(),
//                        command.getCommand(), Integer.toString(tries),
//                        Integer.toString(maxtries)});
//            throw ex;
//        } catch (InterruptedException ex) {
//            Logger.getLogger("glassfish").log(Level.INFO,
//                    "Server {0} {1}:{2} user {3}: {4} interrupted. Try {5} of {6}.",
//                    new Object[]{instance.getName(), instance.getHost(),
//                        instance.getHttpAdminPort(), instance.getAdminUser(),
//                        command.getCommand(), Integer.toString(tries),
//                        Integer.toString(maxtries)});
//            throw ex;
//        } catch (ExecutionException ex) {
//            Logger.getLogger("glassfish").log(Level.INFO,
//                    "Server {0} {1}:{2} user {3}: {4} threw an exception. Try {5} of {6}.",
//                    new Object[]{instance.getName(), instance.getHost(),
//                        instance.getHttpAdminPort(), instance.getAdminUser(),
//                        command.getCommand(), Integer.toString(tries),
//                        Integer.toString(maxtries)});
//            throw ex;
//        }
//    }
//
//    /**
//     * Log Location command execution result.
//     * <p/>
//     * Internal {@link #isReady(boolean, int, TimeUnit)} helper.
//     * <p/>
//     * @param result Location command asynchronous execution final result.
//     * @param start  Location command asynchronous execution start time.
//     */
//    private void logLocationResult(
//            ResultMap<String, String> result, long start) {
//        if (Logger.getLogger("glassfish").isLoggable(Level.FINEST)) {
//            String message;
//            if (result != null && result.getValue() != null) {
//                if (result.getValue().get("message") != null) {
//                    message = result.getValue().get("message");
//                } else {
//                    String baseRootValue = result
//                            .getValue().get("Base-Root_value");
//                    String domainRootValue = result
//                            .getValue().get("Domain-Root_value");
//                    if (baseRootValue == null) {
//                        baseRootValue = "null";
//                    }
//                    if (domainRootValue == null) {
//                        domainRootValue = "null";
//                    }
//                    StringBuilder sb = new StringBuilder(baseRootValue.length()
//                            + 1 + domainRootValue.length());
//                    sb.append(baseRootValue);
//                    sb.append(' ');
//                    sb.append(domainRootValue);
//                    message = sb.toString();
//                }
//            } else {
//                message = null;
//            }
//            Logger.getLogger("glassfish").log(Level.FINEST,
//                    "Location command responded in {0} ms with result {1} and response {2}",
//                    new Object[]{
//                        Long.toString((System.nanoTime() - start) / 1000000),
//                        result.getState().toString(), message});
//        }
//    }
//
//    /**
//    * Log Version command execution result.
//     * <p/>
//     * Internal {@link #isReady(boolean, int, TimeUnit)} helper.
//     * <p/>
//     * @param result Version command asynchronous execution final result.
//     * @param start  Version command asynchronous execution start time.
//     */
//    private void logVersionResult(ResultString result, long start) {
//        if (Logger.getLogger("glassfish").isLoggable(Level.FINEST)) {
//            String resultState = result != null && result.getState() != null
//                        ? result.getState().toString() : "null";
//            String resultValue = result != null ? result.getValue() : "null";
//            Logger.getLogger("glassfish").log(Level.FINEST,
//                    "Version command responded in {0} ms with result {1} and response {2}",
//                    new Object[]{
//                        Long.toString((System.nanoTime() - start) / 1000000),
//                        resultState, resultValue});
//        }
//    }
//
//    /**
//     * Verify GlassFish server installation and domain directories and update
//     * HTTP port.
//     * <p/>
//     * Internal {@link #isReady(boolean, int, TimeUnit)} helper.
//     * <p/>
//     * @param result Location command asynchronous execution final result. 
//     * @return Returns <code>true</code> when server is ready or
//     *         <code>false</code> otherwise.
//     */
//    private boolean processReadyLocationresult(
//            ResultMap<String, String> result) {
//        boolean isReady;
//        String domainRoot = instance.getDomainsRoot()
//                + File.separator + instance.getDomainName();
//        String targetDomainRoot = result.getValue().get("Domain-Root_value");
//        if (instance.getDomainsRoot() != null
//                && targetDomainRoot != null) {
//            File installDir = FileUtil.normalizeFile(new File(domainRoot));
//            File targetInstallDir = FileUtil.normalizeFile(
//                    new File(targetDomainRoot));
//            isReady = installDir.equals(targetInstallDir);
//        } else {
//            // if we got a response from the server... we are going
//            // to trust that it is the 'right one'
//            // TODO -- better edge case detection/protection
//            isReady = null != targetDomainRoot;
//        }
//        if (isReady) {
//            // Make sure the http port info is corrected
//            updateHttpPort();
//        }
//        return isReady;
//    }
//
//    /**
//     * When command asynchronous execution failed with TimeoutException, check
//     * if server administration port is alive and if so, display pop up
//     * window with error message.
//     * <p/>
//     * Port connect timeout is set to 15 seconds.
//     * Internal {@link #isReady(boolean, int, TimeUnit)} helper.
//     * <p/>
//     * @param command Command entity.
//     */
//    private void checkPortAndDisplayWarning(Command command) {
//        if (isRunning(instance.getHost(), instance.getAdminPort(),
//                instance.getName(), 15000)) {
//            String message = NbBundle.getMessage(
//                    CommonServerSupport.class, "MSG_COMMAND_SSL_ERROR",
//                    command.getCommand(), instance.getName(),
//                    Integer.toString(instance.getAdminPort()));
//            displayPopUpMessage(this, message);
//        }
//    }
//
//
//    /**
//     * Check if GlassFish server is ready using Location command and
//     * Version command as fallback option.
//     * <p/>
//     * @param retry   Maximum number of administration command retries.
//     * @param timeout Administration command execution timeout value.
//     * @param units   Administration command execution timeout units .
//     * @return Returns <code>true</code> when GlassFish server is ready
//     *         or <code>false</code> otherwise.
//     */
//    @SuppressWarnings("SleepWhileInLoop")
//    private boolean isReady(boolean retry, int timeout, TimeUnit units) {
//        boolean isReady = false;
//        int maxtries = retry ? 3 : 1;
//        int tries = 0;
//        boolean notYetReadyResponse = false;
//        long begTm = System.currentTimeMillis();
//        long actTm = begTm;
//        Logger.getLogger("glassfish").log(Level.FINEST,
//                "GlassFish status check: retries = {0} timeout = {1} [{2}]",
//                new Object[]{Integer.toString(maxtries),
//                    Integer.toString(timeout), units.toString()});
//        while (!isReady && (
//                tries++ < maxtries || (
//                notYetReadyResponse && (actTm - begTm) < STARTUP_TIMEOUT))) {
//            if (tries > 1 || notYetReadyResponse) {
//                retrySleep(begTm, actTm, tries);
//            }
//            // Location command check
//            long start = System.nanoTime();
//            CommandLocation commandLocation = new CommandLocation();
//            Future<ResultMap<String, String>> futureLocation
//                    = execLocation(commandLocation, timeout, units);
//            ResultMap<String, String> resultLocation;
//            try {
//                resultLocation = resultLocation(futureLocation,
//                        commandLocation, timeout, units, maxtries, tries);
//            // Retry next cycle on TimeoutException.
//            } catch (TimeoutException ex) {
//                isReady = false;
//                checkPortAndDisplayWarning(commandLocation);
//                continue;
//            // Give it up on other exceptions.
//            } catch (InterruptedException ex) {
//                isReady = false;
//                break;
//            } catch (ExecutionException ex) {
//                isReady = false;
//                break;
//            }
//            String message = resultLocation != null
//                    && resultLocation.getValue() != null
//                    ? resultLocation.getValue().get("message") : null;
//            // Not ready response and timer update belongs to each other.
//            notYetReadyResponse = ServerUtils.notYetReadyMsg(message);
//            actTm = System.currentTimeMillis();
//            logLocationResult(resultLocation, start);
//
//            if (resultLocation.getState() == TaskState.COMPLETED) {
//                isReady = processReadyLocationresult(resultLocation);
//                break;
//            // Version command check
//            } else if (!commandLocation.retry()) {
//                // !PW temporary while some server versions support
//                // __locationsband some do not but are still V3 and might
//                // the ones the user is using.
//                start = System.nanoTime();
//                CommandVersion commandVersion = new CommandVersion();
//                Future<ResultString> futureVersion
//                        = execVersion(commandVersion, timeout, units);
//                ResultString resultVersion;
//                try {
//                    resultVersion = resultVersion(futureVersion,
//                            commandVersion, timeout, units, maxtries, tries);
//                // Retry next cycle on TimeoutException.
//                } catch (TimeoutException ex) {
//                    isReady = false;
//                    checkPortAndDisplayWarning(commandVersion);
//                    continue;
//                    // Give it up on other exceptions.
//                } catch (InterruptedException ex) {
//                    isReady = false;
//                    break;
//                } catch (ExecutionException ex) {
//                    isReady = false;
//                    break;
//                }
//                message = resultVersion.getValue();
//                // Not ready response and timer update belongs to each other.
//                notYetReadyResponse = ServerUtils.notYetReadyMsg(message);
//                actTm = System.currentTimeMillis();
//                logVersionResult(resultVersion, start);
//
//                isReady = resultVersion.getState() == TaskState.COMPLETED;
//                if (notYetReadyResponse) {
//                    continue;
//                }
//                break;
//            } else {
//                // keep trying for 10 minutes if the server is stuck between
//                // httpLive and server ready state. We have to give up
//                // sometime, though.
//                VMIntrospector vmi = Lookups.forPath(Util.GF_LOOKUP_PATH)
//                        .lookup(VMIntrospector.class);
//                boolean suspended = null == vmi
//                        ? false
//                        : vmi.isSuspended(getHostName(),
//                        (String) instance.getProperty(
//                        GlassfishModule.DEBUG_PORT));
//                if (suspended) {
//                    tries--;
//                } else if (maxtries < 20) {
//                    maxtries++;
//                }
//            }
//        } // while
//
//        return isReady;
//    }

////////////////////////////////////////////////////////////////////////////////
// MOVED TO GlassFishStatus                                               END //
////////////////////////////////////////////////////////////////////////////////

    // ------------------------------------------------------------------------
    //  RefreshModulesCookie implementation (for refreshing server state)
    // ------------------------------------------------------------------------
    private final AtomicBoolean refreshRunning = new AtomicBoolean(false);

    @Override
    public final RequestProcessor.Task refresh() {
        return refresh(null,null);
    }

    @Override
    public RequestProcessor.Task refresh(String expected, String unexpected) {
        // !PW FIXME we can do better here, but for now, make sure we only change
        // server state from stopped or running states -- leave stopping or starting
        // states alone.
        if(refreshRunning.compareAndSet(false, true)) {
            return RP.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Can block for up to a few seconds...
                        boolean isRunning = GlassFishStatus.isReady(
                                instance, false, GlassFishStatus.Mode.REFRESH);
                        ServerState currentState = serverState;

                        if ((currentState == ServerState.STOPPED || currentState == ServerState.UNKNOWN) && isRunning) {
                            setServerState(ServerState.RUNNING);
                        } else if ((currentState == ServerState.RUNNING || currentState == ServerState.UNKNOWN) && !isRunning) {
                            setServerState(ServerState.STOPPED);
                        } else if (currentState == ServerState.STOPPED_JVM_PROFILER && isRunning) {
                            setServerState(ServerState.RUNNING);
                        }
                    } catch (Exception ex) {
                         Logger.getLogger("glassfish").log(Level.WARNING,
                                 ex.getMessage());
                    } finally {
                        refreshRunning.set(false);
                    }
                }
            });
        } else {
            return null;
        }
    }

    void disableStop() {
        stopDisabled = true;
    }
    
    void setLocalStartProcess(Process process) {
        this.localStartProcess = process;
    }
    
    Process getLocalStartProcess() {
        return localStartProcess;
    }
    
    void stopLocalStartProcess() {
        localStartProcess.destroy();
        localStartProcess = null;
    }

    @Override
    public CommandFactory getCommandFactory() {
        return instance.getInstanceProvider().getCommandFactory();
    }

    @Override
    public String getResourcesXmlName() {
        return Utils.useGlassfishPrefix(getDeployerUri()) ?
                "glassfish-resources" : "sun-resources"; // NOI18N
    }

    @Override
    public boolean supportsRestartInDebug() {
        return getDeployerUri().contains(GlassfishInstanceProvider.EE6WC_DEPLOYER_FRAGMENT);
    }

    @Override
    public boolean isRestfulLogAccessSupported() {
        return getDeployerUri().contains(GlassfishInstanceProvider.EE6WC_DEPLOYER_FRAGMENT);
    }

    @Override
    public boolean isWritable() {
        return (null == instanceFO) ? false : instanceFO.canWrite();
    }

    private long latestWarningDisplayTime = System.currentTimeMillis();
    
    private long getLatestWarningDisplayTime() {
        return latestWarningDisplayTime;
    }

    private void setLatestWarningDisplayTime(long currentTime) {
        latestWarningDisplayTime = currentTime;
    }

    class StartOperationStateListener implements OperationStateListener {
        private ServerState endState;

        StartOperationStateListener(ServerState endState) {
            this.endState = endState;
        }

        @Override
        public void operationStateChanged(OperationState newState, String message) {
            if(newState == OperationState.RUNNING) {
                setServerState(ServerState.STARTING);
            } else if(newState == OperationState.COMPLETED) {
                startedByIde = isRemote
                        ? false : GlassFishStatus.isReady(instance, false);
                setServerState(endState);
            } else if(newState == OperationState.FAILED) {
                setServerState(ServerState.STOPPED);
                // Open a warning dialog here...
                NotifyDescriptor nd = new NotifyDescriptor.Message(message);
                DialogDisplayer.getDefault().notifyLater(nd);
            }
        }
    }

    void updateHttpPort() {
        String target = Util.computeTarget(instance.getProperties());
        GetPropertyCommand gpc;
        if (Util.isDefaultOrServerTarget(instance.getProperties())) {
            gpc = new GetPropertyCommand("*.server-config.*.http-listener-1.port"); // NOI18N
            setEnvironmentProperty(GlassfishModule.HTTPHOST_ATTR, 
                    instance.getProperty(GlassfishModule.HOSTNAME_ATTR), true); // NOI18N
        } else {
            String server = getServerFromTarget(target);
            String adminHost = instance.getProperty(GlassfishModule.HOSTNAME_ATTR);
            setEnvironmentProperty(GlassfishModule.HTTPHOST_ATTR,
                    getHttpHostFromServer(server,adminHost), true);
            gpc = new GetPropertyCommand("servers.server."+server+".system-property.HTTP_LISTENER_PORT.value", true); // NOI18N
        }
        Future<OperationState> result2 = execute(true, gpc);
        try {
            boolean didSet = false;
            if (result2.get(10, TimeUnit.SECONDS) == OperationState.COMPLETED) {
                Map<String, String> retVal = gpc.getData();
                for (Entry<String, String> entry : retVal.entrySet()) {
                    String val = entry.getValue();
                    try {
                        if (null != val && val.trim().length() > 0) {
                            Integer.parseInt(val);
                            setEnvironmentProperty(GlassfishModule.HTTPPORT_ATTR, val, true);
                            didSet = true;
                        }
                    } catch (NumberFormatException nfe) {
                        // skip it quietly..
                    }
                }
            }
            if (!didSet && !Util.isDefaultOrServerTarget(instance.getProperties())) {
                setEnvironmentProperty(GlassfishModule.HTTPPORT_ATTR, "28080", true); // NOI18N
            }
        } catch (InterruptedException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
        } catch (ExecutionException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
        } catch (TimeoutException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, "could not get http port value in 10 seconds from the server", ex); // NOI18N
        }
    }

    private String getServerFromTarget(String target) {
        String retVal = target; // NOI18N
        GetPropertyCommand  gpc = new GetPropertyCommand("clusters.cluster."+target+".server-ref.*.ref", true); // NOI18N

        Future<OperationState> result2 = execute(true, gpc);
        try {
            if (result2.get(10, TimeUnit.SECONDS) == OperationState.COMPLETED) {
                Map<String, String> data = gpc.getData();
                for (Entry<String, String> entry : data.entrySet()) {
                    String val = entry.getValue();
                        if (null != val && val.trim().length() > 0) {
                            retVal = val;
                            break;
                        }
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
        } catch (ExecutionException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
        } catch (TimeoutException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, "could not get http port value in 10 seconds from the server", ex); // NOI18N
        }

        return retVal;
    }
    private String getHttpHostFromServer(String server, String nameOfLocalhost) {
        String retVal = "localhostFAIL"; // NOI18N
        GetPropertyCommand  gpc = new GetPropertyCommand("servers.server."+server+".node-ref"); // NOI18N
        String refVal = null;
        Future<OperationState> result2 = execute(true, gpc);
        try {
            if (result2.get(10, TimeUnit.SECONDS) == OperationState.COMPLETED) {
                Map<String, String> data = gpc.getData();
                for (Entry<String, String> entry : data.entrySet()) {
                    String val = entry.getValue();
                        if (null != val && val.trim().length() > 0) {
                            refVal = val;
                            break;
                        }
                }
            }
            gpc = new GetPropertyCommand("nodes.node."+refVal+".node-host"); // NOI18N
            result2 = execute(true,gpc);
            if (result2.get(10, TimeUnit.SECONDS) == OperationState.COMPLETED) {
                Map<String, String> data = gpc.getData();
                for (Entry<String, String> entry : data.entrySet()) {
                    String val = entry.getValue();
                        if (null != val && val.trim().length() > 0) {
                            retVal = val;
                            break;
                        }
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
        } catch (ExecutionException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
        } catch (TimeoutException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, "could not get http port value in 10 seconds from the server", ex); // NOI18N
        }

        return "localhost".equals(retVal) ? nameOfLocalhost : retVal; // NOI18N
    }

}
