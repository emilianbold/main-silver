/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.glassfish.common;

import java.io.*;
import java.net.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map.Entry;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.net.ssl.*;
import org.glassfish.tools.ide.admin.*;
import org.glassfish.tools.ide.utils.ServerUtils;
import org.netbeans.modules.glassfish.spi.*;


/**
 * Implementation of management task that provides info about progress
 *
 * @author Peter Williams
 */
public class CommandRunner extends BasicTask<TaskState> {

    public final int HTTP_RETRY_DELAY = 3000;

    /** Executor that serializes management tasks.
     */
    private static ExecutorService executor;

    private static Authenticator AUTH = new AdminAuthenticator();

    /** Returns shared executor.
     */
    private static synchronized ExecutorService executor() {
        if(executor == null) {
            executor = Executors.newFixedThreadPool(1);
        }
        return executor;
    }

    /** Command type used for events. */
    private ServerCommand serverCmd;

    /** Has been the last access to  manager web app authorized? */
    private boolean authorized;
    private final CommandFactory cf;
    private final boolean isReallyRunning;


    public CommandRunner(boolean isReallyRunning, CommandFactory cf,
            GlassfishInstance instance, TaskStateListener... stateListener) {
        super(instance, stateListener);
        this.cf =cf;
        this.isReallyRunning = isReallyRunning;
    }

    /**
     * Sends stop-domain command to server (asynchronous)
     *
     */
    public Future<TaskState> stopServer() {
        return execute(Commands.STOP, "MSG_STOP_SERVER_IN_PROGRESS"); // NOI18N

    }

    /**
     * Sends restart-domain command to server (asynchronous)
     *
     */
    public Future<TaskState> restartServer(int debugPort, String query) {
        final String restartQuery = query; // cf.getRestartQuery(debugPort);
        if (-1 == debugPort || "".equals(restartQuery) ) {
            return execute(new ServerCommand("restart-domain") {

                @Override
                public String getQuery() {
                    return restartQuery;
                }
            }, "MSG_RESTART_SERVER_IN_PROGRESS"); // NOI18N
        }
        // force the options to be correct for remote debugging, then restart...
        CommandRunner inner = new CommandRunner(isReallyRunning, cf, instance,
                new TaskStateListener() {

            @Override
            public void operationStateChanged(TaskState newState,
                    TaskEvent event, String... args) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

        });

        // I wish that the server folks had let me add a port number to the
        // restart-domain --debug command... but this will have to do until then

        ServerCommand.GetPropertyCommand getCmd = new ServerCommand.GetPropertyCommand("configs.config.server-config.java-config.debug-options");

        TaskState state = null;
        try {
            state = inner.execute(getCmd).get();
        } catch (InterruptedException ie) {
            Logger.getLogger("glassfish").log(Level.INFO,debugPort+"",ie);
        } catch (ExecutionException ee) {
            Logger.getLogger("glassfish").log(Level.INFO,debugPort+"",ee);
        }
        String qs = null;
        if (state == TaskState.COMPLETED) {
            Map<String, String> data = getCmd.getData();
            if (!data.isEmpty()) {
                // now I can reset the debug data
                String oldValue = data.get("configs.config.server-config.java-config.debug-options");
                ServerCommand.SetPropertyCommand setCmd =
                        cf.getSetPropertyCommand("configs.config.server-config.java-config.debug-options",
                        oldValue.replace("transport=dt_shmem", "transport=dt_socket").
                        replace("address=[^,]+", "address=" + debugPort));
                //serverCmd = setCmd;
                //task = executor.submit(this);
                try {
                    inner.execute(setCmd).get();
                    qs = "debug=true";
                } catch (InterruptedException ie) {
                     Logger.getLogger("glassfish").log(Level.INFO,debugPort+"",ie);
                } catch (ExecutionException ee) {
                     Logger.getLogger("glassfish").log(Level.INFO,debugPort+"",ee);
                }
            }
        }
        if (null == qs) {
            qs = "debug=false";
        }
        final String fqs = qs;
        return execute(new ServerCommand("restart-domain") {

            @Override
            public String getQuery() {
                return fqs;
            }
        }, "MSG_RESTART_SERVER_IN_PROGRESS");
    }

    /**
     * Sends list-applications command to server (synchronous)
     *
     * @return String array of names of deployed applications.
     */
    public Map<String, List<AppDesc>> getApplications(String container) {
        CommandRunner inner = new CommandRunner(isReallyRunning, cf, instance,
                new TaskStateListener() {

            @Override
            public void operationStateChanged(TaskState newState,
                    TaskEvent event, String... args) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

        });
        Map<String, List<AppDesc>> result = Collections.emptyMap();
        try {
            Map<String, List<String>> apps = Collections.emptyMap();
            Command command = new CommandListComponents(
                    Util.computeTarget(instance.getProperties()));
            Future<ResultMap<String, List<String>>> future = 
                    ServerAdmin.<ResultMap<String,
                    List<String>>>exec(instance, command, null);
            ResultMap<String, List<String>> resultMap = future.get();
            TaskState state = resultMap.getState();
            if (state == TaskState.COMPLETED) {
                apps = resultMap.getValue();
            }
            if (null == apps || apps.isEmpty()) {
                return result;
            }
            ServerCommand.GetPropertyCommand getCmd = new ServerCommand.GetPropertyCommand("applications.application.*"); // NOI18N
            TaskState taskState = inner.execute(getCmd).get();
            if (taskState == TaskState.COMPLETED) {
                ServerCommand.GetPropertyCommand getRefs = new ServerCommand.GetPropertyCommand("servers.server.*.application-ref.*"); // NOI18N
                taskState = inner.execute(getRefs).get();
                if (TaskState.COMPLETED == taskState) {
                    result = processApplications(apps, getCmd.getData(),getRefs.getData());
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, ex.getMessage(), ex);  // NOI18N
        } catch (ExecutionException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, ex.getMessage(), ex);  // NOI18N
        }
        return result;
    }

    private Map<String, List<AppDesc>> processApplications(Map<String, List<String>> appsList, Map<String, String> properties, Map<String, String> refProperties){
        Map<String, List<AppDesc>> result = new HashMap<String, List<AppDesc>>();
        Iterator<String> appsItr = appsList.keySet().iterator();
        while (appsItr.hasNext()) {
            String engine = appsItr.next();
            List<String> apps = appsList.get(engine);
            for (int i = 0; i < apps.size(); i++) {
                String name = apps.get(i).trim();
                String appname = "applications.application." + name; // NOI18N
                String contextKey = appname + ".context-root"; // NOI18N
                String pathKey = appname + ".location"; // NOI18N

                String contextRoot = properties.get(contextKey);
                if (contextRoot == null) {
                    contextRoot = name;
                }
                if (contextRoot.startsWith("/")) {  // NOI18N
                    contextRoot = contextRoot.substring(1);
                }

                String path = properties.get(pathKey);
                if (path == null) {
                    path = "unknown"; //NOI18N
                }
                if (path.startsWith("file:")) {  // NOI18N
                    path = path.substring(5);
                    path = (new File(path)).getAbsolutePath();
                }

                String enabledKey = "servers.server.server.application-ref."+name+".enabled";  //NOI18N
                // XXX - this needs to be more focused... does it need to list of
                //  servers that are associated with the target?
                for (String possibleKey : refProperties.keySet()) {
                    if (possibleKey.endsWith(".application-ref."+name+".enabled")) { // NOI18N
                        enabledKey = possibleKey;
                    }
                }
                String enabledValue = refProperties.get(enabledKey);
                if (null != enabledValue) {
                    boolean enabled = Boolean.parseBoolean(enabledValue);

                    List<AppDesc> appList = result.get(engine);
                    if(appList == null) {
                        appList = new ArrayList<AppDesc>();
                        result.put(engine, appList);
                    }
                    appList.add(new AppDesc(name, path, contextRoot, enabled));
                }
            }
        }
        return result;
    }

    /**
     * Execute an arbitrary server command.
     */
    public Future<TaskState> execute(ServerCommand command) {
        return execute(command, null);
    }

    private Future<TaskState> execute(ServerCommand command, String msgResId) {
        serverCmd = command;
        if(msgResId != null) {
            fireOperationStateChanged(TaskState.RUNNING, TaskEvent.CMD_RUNNING,
                    msgResId, instanceName);
        }
        return executor().submit(this);
    }

    /** Executes one management task.
     */
    @SuppressWarnings("SleepWhileInLoop")
    @Override
    public TaskState call() {
        fireOperationStateChanged(TaskState.RUNNING, TaskEvent.CMD_RUNNING,
                "MSG_ServerCmdRunning", serverCmd.toString(), instanceName);

        if (!isReallyRunning) {
            return fireOperationStateChanged(TaskState.FAILED,
                    TaskEvent.CMD_FAILED,
                    "MSG_ServerCmdFailedIncorrectInstance",
                    serverCmd.toString(), instanceName);
        }

        boolean httpSucceeded = false;
        boolean commandSucceeded = false;
        HttpURLConnection hconn = null;
        String commandUrl;

        try {
            commandUrl = constructCommandUrl(serverCmd.getSrc(),
                    serverCmd.getCommand(), serverCmd.getQuery());
        } catch (URISyntaxException ex) {
            return fireOperationStateChanged(TaskState.FAILED,
                    TaskEvent.CMD_FAILED, "MSG_ServerCmdException",
                    serverCmd.toString(), instanceName, ex.getLocalizedMessage());
        }

        int retries = 1; // disable ("version".equals(cmd) || "__locations".equals(cmd)) ? 1 : 3;
        Logger.getLogger("glassfish").log(Level.FINEST, "CommandRunner.call({0}) called on thread \"{1}\"", new Object[]{commandUrl, Thread.currentThread().getName()}); // NOI18N

        // Create a connection for this command
        try {
            URL urlToConnectTo = new URL(commandUrl);

            while(!httpSucceeded && retries-- > 0) {
                try {
                    Logger.getLogger("glassfish").log(Level.FINE, "HTTP Command: {0}", commandUrl); // NOI18N

                    URLConnection conn = urlToConnectTo.openConnection();
                    if (conn instanceof HttpURLConnection) {
                        int respCode = 0;
                        URL oldUrlToConnectTo;
                        do { // deal with possible redirects from 3.1
                            oldUrlToConnectTo = urlToConnectTo;
                            hconn = (HttpURLConnection) conn;

                            if (conn instanceof HttpsURLConnection) {
                                // let's just trust any server that we connect to...
                                // we aren't send them money or secrets...
                                TrustManager[] tm = new TrustManager[]{
                                    new X509TrustManager() {

                                        @Override
                                        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                                        }

                                        @Override
                                        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                                        }

                                        @Override
                                        public X509Certificate[] getAcceptedIssuers() {
                                            return null;
                                        }
                                    }
                                };

                                try {
                                    SSLContext context = SSLContext.getInstance("SSL");
                                    context.init(null, tm, null);
                                    ((HttpsURLConnection) hconn).setSSLSocketFactory(context.getSocketFactory());
                                    ((HttpsURLConnection) hconn).setHostnameVerifier(new HostnameVerifier() {

                                        @Override
                                        public boolean verify(String string, SSLSession ssls) {
                                            return true;
                                        }
                                    });
                                } catch (Exception ex) {
                                    // if there is an issue here... there will be another exception later
                                    // which will take care of the user interaction...
                                    Logger.getLogger("glassfish").log(Level.INFO, "trust manager problem: " + urlToConnectTo, ex); // NOI18N
                                }

                            }

                            // Set up standard connection characteristics
                            hconn.setAllowUserInteraction(false);
                            hconn.setDoInput(true);
                            hconn.setUseCaches(false);
                            hconn.setRequestMethod(serverCmd.getRequestMethod());
                            hconn.setDoOutput(serverCmd.getDoOutput());
                            String contentType = serverCmd.getContentType();
                            if (contentType != null && contentType.length() > 0) {
                                hconn.setRequestProperty("Content-Type", contentType); // NOI18N
                                hconn.setChunkedStreamingMode(0);
                            } else {
                                // work around that helps prevent tickling the
                                // GF issue that is the root cause of 195384.
                                //
                                // GF doesn't expect to get image content, so it doesn't
                                // try to handle the content... which prevents the
                                // exception, according to Tim Quinn.
                                hconn.setRequestProperty("Content-Type", "image/png");
                            }
                            hconn.setRequestProperty("User-Agent", "hk2-agent"); // NOI18N
                            if (serverCmd.acceptsGzip()) {
                                hconn.setRequestProperty("Accept-Encoding", "gzip");
                            }

                            // Authorization shall be the same as in Tooling SDK Runner.
                            String adminUser = instance.getAdminUser();
                            String adminPassword = instance.getAdminPassword();
                            if (adminPassword != null && adminPassword.length() > 0) {
                                String auth = ServerUtils.basicAuthCredentials(
                                        adminUser, adminPassword);
                                conn.setRequestProperty("Authorization", "Basic " + auth);
                            }

                            // Establish the connection with the server
                            //Authenticator.setDefault(AUTH);
                            hconn.connect();
                            // Send data to server if necessary
                            handleSend(hconn);

                            respCode = hconn.getResponseCode();
                            if (respCode == HttpURLConnection.HTTP_UNAUTHORIZED
                                    || respCode == HttpURLConnection.HTTP_FORBIDDEN) {
                                // connection to manager has not been allowed
                                authorized = false;
                                String messageId = "MSG_AuthorizationFailed";  // NOI18N
                                if (instance.getProperty(GlassfishModule.DOMAINS_FOLDER_ATTR) == null) {
                                    messageId = "MSG_AuthorizationFailedRemote"; // NOI18N
                                }
                                return fireOperationStateChanged(
                                        TaskState.FAILED, TaskEvent.CMD_FAILED,
                                        messageId, serverCmd.toString(), instanceName);
                            } else if (respCode == HttpURLConnection.HTTP_MOVED_TEMP
                                    || respCode == HttpURLConnection.HTTP_MOVED_PERM) {
                                String newUrl = hconn.getHeaderField("Location");
                                if (null == newUrl || "".equals(newUrl.trim())) {
                                    Logger.getLogger("glassfish").log(Level.SEVERE,
                                            "invalid redirect for {0}", urlToConnectTo.toString());  //NOI18N
                                } else {
                                    Logger.getLogger("glassfish").log(Level.FINE, "  moved to {0}", newUrl); // NOI18N
                                    urlToConnectTo = new URL(newUrl);
                                    conn = urlToConnectTo.openConnection();
                                    hconn.disconnect();
                                }
                            }
                        } while (urlToConnectTo != oldUrlToConnectTo);

                        // !PW FIXME log status for debugging purposes
                        if(Boolean.getBoolean("org.netbeans.modules.hk2.LogManagerCommands")) { // NOI18N
                            Logger.getLogger("glassfish").log(Level.FINE, "  receiving response, code: {0}", respCode); // NOI18N
                        }

                        // Process the response message
                        if(handleReceive(hconn)) {
                            commandSucceeded = serverCmd.processResponse();
                        } else {
                            if (!serverCmd.isSilentFailureAllowed()) {
                                Logger.getLogger("glassfish").log(Level.WARNING, hconn.toString());
                                Logger.getLogger("glassfish").log(Level.WARNING, hconn.getContentType());
                                Logger.getLogger("glassfish").log(Level.WARNING, hconn.getContentEncoding());
                                Map<String,List<String>> ms2ls = hconn.getHeaderFields();
                                Logger.getLogger("glassfish").log(Level.WARNING, "Header Fields");
                                for (Entry<String,List<String>> e : ms2ls.entrySet()) {
                                    Logger.getLogger("glassfish").log(
                                            Level.WARNING, "{0} = ", e.getKey());
                                    for (String v : e.getValue()) {
                                        Logger.getLogger("glassfish").log(
                                                Level.WARNING, "     {0}", v);
                                    }
                                }
                            }
                        }

                        httpSucceeded = true;
                    } else {
                        Logger.getLogger("glassfish").log(Level.INFO,
                                "Unexpected connection type: {0}",
                                urlToConnectTo);
                    }
                } catch(ProtocolException ex) {
                    fireOperationStateChanged(TaskState.FAILED,
                            TaskEvent.CMD_FAILED, "MSG_Exception",
                            ex.getLocalizedMessage());
                    retries = 0;
                } catch (ConnectException ce) {
                    return fireOperationStateChanged(TaskState.FAILED,
                            TaskEvent.CMD_FAILED, "MSG_EmptyMessage");
                } catch(IOException ex) {
                    if(retries <= 0) {
                        fireOperationStateChanged(TaskState.FAILED,
                                TaskEvent.CMD_FAILED, "MSG_Exception",
                                ex.getLocalizedMessage());
                    }
                } finally {
                    if (null != hconn) hconn.disconnect();
                }

                if(!httpSucceeded && retries > 0) {
                    try {
                        Thread.sleep(HTTP_RETRY_DELAY);
                    } catch (InterruptedException e) {}
                }
            } // while
        } catch(MalformedURLException ex) {
            Logger.getLogger("glassfish").log(Level.WARNING, ex.getLocalizedMessage(), ex); // NOI18N
        }

        if(commandSucceeded) {
            return fireOperationStateChanged(TaskState.FAILED,
                    TaskEvent.CMD_FAILED, "MSG_ServerCmdCompleted",
                    serverCmd.toString(), instanceName);
        } else {
            return fireOperationStateChanged(TaskState.FAILED,
                    TaskEvent.CMD_FAILED, "MSG_ServerCmdFailed",
                    serverCmd.toString(), instanceName, serverCmd.getServerMessage());
        }
    }

    private String constructCommandUrl(final String cmdSrc, final String cmd,
            final String query) throws URISyntaxException {
        String host = instance.getProperty(GlassfishModule.HOSTNAME_ATTR);
        boolean useAdminPort = !"false".equals(System.getProperty(
                "glassfish.useadminport")); // NOI18N
        int port = Integer.parseInt(instance.getProperty(useAdminPort
                ? GlassfishModule.ADMINPORT_ATTR
                : GlassfishModule.HTTPPORT_ATTR));
        String protocol = "http";
        String url = instance.getProperty(GlassfishModule.URL_ATTR);
        String domainsDir = instance.getProperty(
                GlassfishModule.DOMAINS_FOLDER_ATTR);
        if (null == url) {
            protocol = getHttpListenerProtocol(host,port, ":::"+cmd+"?"+query);
        } else if (!(url.contains("ee6wc"))) {
            protocol = getHttpListenerProtocol(host,port,url+":::"+cmd+"?"+query);
        } else if (url.contains("ee6wc")
                && (null == domainsDir || "".equals(domainsDir))) {
            protocol = "https";
        }
        URI uri = new URI(protocol, null, host, port, cmdSrc + cmd, query,
                null);
        // These characters don't get handled by GF correctly...
        // Best I can tell.
        return uri.toASCIIString().replace("+", "%2b");
    }


    private static String getHttpListenerProtocol(String hostname, int port, String url) {
        String retVal = "http";  // NOI18N
        try {
            if (Utils.isSecurePort(hostname, port)) {
                retVal = "https"; // NOI18N
            }
        } catch (ConnectException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, hostname + ":" + port + "::" + url, ex); // NOI18N
        } catch (SocketException ex) {
            Logger.getLogger("glassfish").log(Level.FINE, hostname + ":" + port + "::" + url, ex); // NOI18N
        } catch (SocketTimeoutException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, hostname + ":" + port + "::" + url, ex); // NOI18N
        } catch (IOException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, hostname + ":" + port + "::" + url, ex); // NOI18N
        }
        return retVal;
    }

    /*
     * Note: this is based on reading the code of CLIRemoteCommand.java
     * from the server's code repository... Since some asadmin commands
     * need to send multiple files, the server assumes the input is a ZIP
     * stream.
     */
    private void handleSend(HttpURLConnection hconn) throws IOException {
        InputStream istream = serverCmd.getInputStream();
        if(istream != null) {
            ZipOutputStream ostream = null;
            try {
                ostream = new ZipOutputStream(new BufferedOutputStream(hconn.getOutputStream(), 1024*1024));
                ZipEntry e = new ZipEntry(serverCmd.getInputName());
                e.setExtra(getExtraProperties());
                ostream.putNextEntry(e);
                byte buffer[] = new byte[1024*1024];
                while (true) {
                    int n = istream.read(buffer);
                    if (n < 0) {
                        break;
                    }
                    ostream.write(buffer, 0, n);
                }
                ostream.closeEntry();
                ostream.flush();
            } finally {
                try {
                    istream.close();
                } catch(IOException ex) {
                    Logger.getLogger("glassfish").log(Level.INFO,
                            ex.getLocalizedMessage(), ex);
                }
                if(ostream != null) {
                    try {
                        ostream.close();
                    } catch(IOException ex) {
                        Logger.getLogger("glassfish").log(Level.INFO,
                                ex.getLocalizedMessage(), ex);
                    }
                }
            }
        } else if("POST".equalsIgnoreCase(serverCmd.getRequestMethod())) {
            Logger.getLogger("glassfish").log(Level.INFO,
                    "HTTP POST request but no data stream provided");
        }
    }

    private byte[] getExtraProperties() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Properties props = new Properties();
        props.setProperty("data-request-type", "file-xfer"); // NOI18N
        props.setProperty("last-modified", serverCmd.getLastModified()); // NOI18N
        props.put("data-request-name", "DEFAULT");
        props.put("data-request-is-recursive", "true");
        props.put("Content-Type", "application/octet-stream");
        props.list(new java.io.PrintStream(baos));
        return baos.toByteArray();
    }

    private boolean handleReceive(HttpURLConnection hconn) throws IOException {
        boolean result = false;
        InputStream httpInputStream = hconn.getInputStream();
        try {
            result = serverCmd.readResponse(httpInputStream, hconn);
        } finally {
            try {
                httpInputStream.close();
            } catch (IOException ex) {
                Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex);  // NOI18N
            }
        }
        return result;
    }

}
