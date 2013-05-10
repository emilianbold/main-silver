/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.rest.spi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping25;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.rest.MiscPrivateUtilities;
import org.netbeans.modules.websvc.rest.WebXmlUpdater;
import static org.netbeans.modules.websvc.rest.WebXmlUpdater.getRestServletAdaptorByName;
import static org.netbeans.modules.websvc.rest.WebXmlUpdater.getRestServletMapping;
import static org.netbeans.modules.websvc.rest.spi.RestSupport.REST_SERVLET_ADAPTOR;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * The purpose of this class is to trim down RestSupport and WebRestSupport down and
 * as such it contains bunch of random static utility methods which previously
 * were polluting and bloating RestSupport. They should be further rafactored and
 * moved to right places.
 */
public class MiscUtilities {

    public static FileObject findSourceRoot(Project project) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups != null && sourceGroups.length > 0) {
            return sourceGroups[0].getRootFolder();
        }
        return null;
    }

    /*
     * Copy File only
     */
    public static void copyFile(File testdir, String name) throws IOException {
        String path = "resources/" + name;
        File df = new File(testdir, name);
        if (!df.exists()) {
            InputStream is = null;
            OutputStream os = null;
            try {
                is = RestSupport.class.getResourceAsStream(path);
                os = new FileOutputStream(df);
                int c;
                while ((c = is.read()) != -1) {
                    os.write(c);
                }
            } finally {
                if (os != null) {
                    os.flush();
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
            }
        }
    }

    /*
     * Copy File, as well as replace tokens, overwrite if specified
     */
    public static FileObject copyFile(File testdir, String name, String[] replaceKeys, boolean overwrite) throws IOException {
        FileObject dir = FileUtil.toFileObject(testdir);
        FileObject fo = dir.getFileObject(name);
        if (fo == null) {
            fo = dir.createData(name);
        } else {
            if (!overwrite) {
                return fo;
            }
        }
        FileLock lock = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            lock = fo.lock();
            OutputStream os = fo.getOutputStream(lock);
            writer = new BufferedWriter(new OutputStreamWriter(os, Charset.forName("UTF-8")));
            InputStream is = RestSupport.class.getResourceAsStream("resources/" + name);
            reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String line;
            String lineSep = "\n";
            if (File.separatorChar == '\\') {
                lineSep = "\r\n";
            }
            String[] replaceValues = null;
            if (replaceKeys != null) {
                replaceValues = new String[replaceKeys.length];
                for (int i = 0; i < replaceKeys.length; i++) {
                    replaceValues[i] = NbBundle.getMessage(RestSupport.class, replaceKeys[i]);
                }
            }
            while ((line = reader.readLine()) != null) {
                for (int i = 0; i < replaceKeys.length; i++) {
                    line = line.replaceAll(replaceKeys[i], replaceValues[i]);
                }
                writer.write(line);
                writer.write(lineSep);
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (lock != null) {
                lock.releaseLock();
            }
            if (reader != null) {
                reader.close();
            }
        }
        return fo;
    }

    public static FileObject modifyFile(FileObject fo, Map<String, String> replace) throws IOException {
        StringWriter content = new StringWriter();
        FileLock lock = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            writer = new BufferedWriter(content);
            InputStream is = fo.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String line;
            String lineSep = "\n";
            if (File.separatorChar == '\\') {
                lineSep = "\r\n";
            }
            while ((line = reader.readLine()) != null) {
                for (Map.Entry<String, String> entry : replace.entrySet()) {
                    line = line.replaceAll(entry.getKey(), entry.getValue());
                }
                writer.write(line);
                writer.write(lineSep);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            StringBuffer buffer = content.getBuffer();
            lock = fo.lock();
            try {
                OutputStream outputStream = fo.getOutputStream(lock);
                writer = new BufferedWriter(new OutputStreamWriter(outputStream, Charset.forName("UTF-8")));
                writer.write(buffer.toString());
            } finally {
                if (lock != null) {
                    lock.releaseLock();
                }
                if (writer != null) {
                    writer.close();
                }
            }
        }
        return fo;
    }

    public static FileObject getApplicationContextXml(Project project) {
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().
            lookup(J2eeModuleProvider.class);
        FileObject[] fobjs = provider.getSourceRoots();

        if (fobjs.length > 0) {
            FileObject configRoot = fobjs[0];
            FileObject webInf = configRoot.getFileObject("WEB-INF");        //NOI18N

            if (webInf != null) {
                return webInf.getFileObject("applicationContext", "xml");      //NOI18N
            }
        }

        return null;
    }

    public static String getContextRootURL(Project project) {
        String portNumber = "8080"; //NOI18N
        String host = "localhost"; //NOI18N
        String contextRoot = "";
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        Deployment.getDefault().getServerInstance(provider.getServerInstanceID());
        String serverInstanceID = provider.getServerInstanceID();
        if (serverInstanceID == null || MiscPrivateUtilities.DEVNULL.equals(serverInstanceID)) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(RestSupport.class, "MSG_MissingServer"),
                    NotifyDescriptor.ERROR_MESSAGE));
        } else {
            // getting port and host name
            ServerInstance serverInstance = Deployment.getDefault().
                getServerInstance(serverInstanceID);
            try {
                ServerInstance.Descriptor instanceDescriptor =
                    serverInstance.getDescriptor();
                if (instanceDescriptor != null) {
                    int port = instanceDescriptor.getHttpPort();
                    if (port>0) {
                        portNumber = String.valueOf(port);
                    }
                    String hostName = instanceDescriptor.getHostname();
                    if (hostName != null) {
                        host = hostName;
                    }
                }
            }
            catch (InstanceRemovedException ex) {

            }
        }
        J2eeModuleProvider.ConfigSupport configSupport = provider.getConfigSupport();
        try {
            contextRoot = configSupport.getWebContextRoot();
        } catch (ConfigurationException e) {
            // TODO the context root value could not be read, let the user know about it
        }
        if (contextRoot.length() > 0 && contextRoot.startsWith("/")) { //NOI18N
            contextRoot = contextRoot.substring(1);
        }
        return "http://"+host+":"+portNumber+"/"+ //NOI18N
                (contextRoot.length()>0 ? contextRoot+"/" : ""); //NOI18N
    }

    public static boolean hasApplicationResourceClass(RestSupport restSupport, final String fqn){
        return MiscPrivateUtilities.hasApplicationResourceClass(restSupport, fqn);
    }

    public static FileObject generateTestClient(File testdir) throws IOException {
        return MiscPrivateUtilities.generateTestClient(testdir);
    }

    public static void addInitParam( RestSupport restSupport, String paramName, String value ) {
        try {
            FileObject ddFO = restSupport.getWebXmlUpdater().getWebXml(false);
            WebApp webApp = restSupport.getWebXmlUpdater().findWebApp();
            if (ddFO == null || webApp == null) {
                return;
            }
            Servlet adaptorServlet = getRestServletAdaptorByName(webApp,
                    REST_SERVLET_ADAPTOR);
            if (adaptorServlet == null) {
                // servlet is missing in the web.xml and so parameter cannot be added
                return;
            }
            InitParam initParam = (InitParam) adaptorServlet.findBeanByName(
                    "InitParam", // NOI18N
                    "ParamName", // NOI18N
                    paramName);
            if (initParam == null) {
                try {
                    initParam = (InitParam) adaptorServlet
                            .createBean("InitParam"); // NOI18N
                    adaptorServlet.addInitParam(initParam);
                }
                catch (ClassNotFoundException ex) {
                }
            }
            initParam.setParamName(paramName);
            initParam.setParamValue(value);

            webApp.write(ddFO);
        }
        catch (IOException e) {
            Logger.getLogger(RestSupport.class.getName()).log(Level.WARNING,  null , e);
        }
    }


    public static ServletMapping getRestServletMapping(WebApp webApp) {
        return WebXmlUpdater.getRestServletMapping(webApp);
    }

    public static String getApplicationPathFromDD(WebApp webApp) {
        if (webApp != null) {
            ServletMapping sm = getRestServletMapping(webApp);
            if (sm != null) {
                String urlPattern = null;
                if (sm instanceof ServletMapping25) {
                    String[] urlPatterns = ((ServletMapping25)sm).getUrlPatterns();
                    if (urlPatterns.length > 0) {
                        urlPattern = urlPatterns[0];
                    }
                } else {
                    urlPattern = sm.getUrlPattern();
                }
                if (urlPattern != null) {
                    if (urlPattern.endsWith("*")) { //NOI18N
                        urlPattern = urlPattern.substring(0, urlPattern.length()-1);
                    }
                    if (urlPattern.endsWith("/")) { //NOI18N
                        urlPattern = urlPattern.substring(0, urlPattern.length()-1);
                    }
                    if (urlPattern.startsWith("/")) { //NOI18N
                        urlPattern = urlPattern.substring(1);
                    }
                    return urlPattern;
                }

            }
        }
        return null;
    }


    public static Datasource getDatasource(Project p, String jndiName) {
        J2eeModuleProvider provider = (J2eeModuleProvider) p.getLookup().lookup(J2eeModuleProvider.class);

        try {
            return provider.getConfigSupport().findDatasource(jndiName);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }


}
