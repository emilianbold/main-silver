/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.spi.webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Simple Web Server supporting only GET command on project's source files.
 */
public final class WebServer {

    private static int PORT = 8383;
    
    private WeakHashMap<Project, String> deployedApps = new WeakHashMap<Project, String>();
    private boolean init = false;
    private Server server;
    private static WebServer webServer;
    
    private WebServer() {
    }

    public static synchronized WebServer getWebserver() {
        if (webServer == null) {
            webServer = new WebServer();
        }
        return webServer;
    }

    private synchronized void checkStartedServer() {
        if (!init) {
            init = true;
            startServer();
        }
    }
    
    /**
     * Start serving project's sources under given web context root.
     */
    public void start(Project p, String webContextRoot) {
        checkStartedServer();
        deployedApps.remove(p);
        deployedApps.put(p, webContextRoot);
    }

    /**
     * Stop serving project's sources.
     */
    public void stop(Project p) {
        deployedApps.remove(p);
        // TODO: if deployedApps is empty we can stop the server
    }

    /**
     * Port server is running on.
     */
    public int getPort() {
        checkStartedServer();
        return server.getPort();
    }

    /**
     * Converts project's file into server URL.
     * @return returns null if project is not currently served
     */
    public URL toServer(FileObject projectFile) {
        Project p = FileOwnerQuery.getOwner(projectFile);
        if (p == null) {
            return null;
        }
        String webContext = deployedApps.get(p);
        if (webContext != null) {
            String path = webContext + (webContext.equals("/") ? "" : "/") + 
                    FileUtil.getRelativePath(p.getProjectDirectory(), projectFile);
            try {
                return new URL("http://localhost:"+PORT+path);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    /**
     * Converts server URL back into project's source file.
     */
    public FileObject fromServer(URL serverURL) {
        return fromServer(serverURL.getPath());
    }

    private FileObject fromServer(String serverURL) {
        for (Map.Entry<Project, String> entry : deployedApps.entrySet()) {
            if (serverURL.startsWith(entry.getValue())) {
                Project p = entry.getKey();
                int index = entry.getValue().length()+1;
                if (entry.getValue().equals("/")) {
                    index = 1;
                }
                String file = serverURL.substring(index);
                return p.getProjectDirectory().getFileObject(file);
            }
        }
        return null;
    }

    private void startServer() {
        server = new Server();
        new Thread( server ).start();
        Thread shutdown = new Thread(){
            @Override
            public void run() {
                server.stop();
            }
        };
        Runtime.getRuntime().addShutdownHook( shutdown);
    }

    private static class Server implements Runnable {

        private boolean stop = false;
        private ServerSocket sock;
        private int port;

        public Server() {
            port = PORT;
            while (true) {
                try {
                    sock = new ServerSocket(port);
                } catch (IOException ex) {
                    // port used:
                    port++;
                    continue;
                }
                break;
            }
        }
        
        @Override
        public void run() {
            try {
                while (!stop) {
                    Socket s = sock.accept();
                    if (stop) {
                        break;
                    }
                    read(s.getInputStream(), s.getOutputStream());
                }
            } catch (SocketException ex) {
                if (!stop) {
                    Exceptions.printStackTrace(ex);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        private void stop() {
            stop = true;
            try {
                sock.close();
            } catch (IOException ex) {
            }
        }

        public int getPort() {
            return port;
        }
        
        private void read(InputStream inputStream, OutputStream outputStream) throws IOException {
            BufferedReader r = null;
            DataOutputStream out = null;
            InputStream fis = null;
            try {
                r = new BufferedReader(new InputStreamReader(inputStream));
                String line = r.readLine();
                if (line == null || line.length() == 0) {
                    return;
                }
                if (line.startsWith("GET ")) {
                    StringTokenizer st = new StringTokenizer(line, " ");
                    st.nextToken();
                    String file = st.nextToken();
                    FileObject fo = getWebserver().fromServer(file);
                    if (fo != null) {
                        fis = fo.getInputStream();
                        out = new DataOutputStream(outputStream);
                        out.writeBytes("HTTP/1.0 200 OK\nContent-Length: "+fo.getSize()+"\n\n");
                        FileUtil.copy(fis, out);
                    }
                }
            } finally {
                if (fis != null) {
                    fis.close();
                }
                if (r != null) {
                    r.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        }
    
    }
    
}
