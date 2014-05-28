/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.weblogic.common.api;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class WebLogicLayout {

    private static final String WEBLOGIC_JAR = "server/lib/weblogic.jar"; // NOI18N

    private static final int DEFAULT_PORT = 7001;

    private static final Collection<String> EXPECTED_FILES = new ArrayList<>();

    static {
        EXPECTED_FILES.add("common"); // NOI18N
        EXPECTED_FILES.add("server/bin"); // NOI18N
        EXPECTED_FILES.add(WEBLOGIC_JAR);
    }

    private static final Logger LOGGER = Logger.getLogger(WebLogicLayout.class.getName());

    public static final String DOMAIN_HOST = "host"; // NOI18N
    public static final String DOMAIN_PORT = "port"; // NOI18N
    public static final String ADMIN_SERVER_NAME= "adminName"; // NOI18N
    public static final String DOMAIN_NAME = "domainName"; // NOI18N
    public static final String PRODUCTION_MODE = "productionMode"; // NOI18N
    public static final String DOMAIN_VERSION = "domainVersion"; // NOI18N

    private static final Pattern LISTEN_ADDRESS_PATTERN =
        Pattern.compile("(?:[a-z]+\\:)?listen-address");            // NOI18N

    private static final Pattern LISTEN_PORT_PATTERN =
        Pattern.compile("(?:[a-z]+\\:)?listen-port");               // NOI18N

    private static final Pattern NAME_PATTERN =
        Pattern.compile("(?:[a-z]+\\:)?name");                      // NOI18N

    private static final  Pattern SERVER_PATTERN =
        Pattern.compile("(?:[a-z]+\\:)?server");                    // NOI18N

    private static final  Pattern ADMIN_SERVER_PATTERN =
        Pattern.compile("(?:[a-z]+\\:)?admin-server-name");         // NOI18N

    private static final String DOMAIN_LIST = "common/nodemanager/nodemanager.domains"; // NOI18N

    private static final String DOMAIN_REGISTRY = "domain-registry.xml"; // NOI18N

    private final WebLogicConfiguration config;

    WebLogicLayout(WebLogicConfiguration config) {
        this.config = config;
    }

    @CheckForNull
    public File getDomainConfigFile() {
        if (config.isRemote()) {
            return null;
        }
        return FileUtil.normalizeFile(new File(config.getDomainHome(),
                "config" + File.separator + "config.xml")); // NOI18N
    }

    @CheckForNull
    public File getServerLibDirectory() {
        File serverLib = new File(config.getServerHome(), "server" + File.separator + "lib"); // NOI18N
        if (serverLib.isDirectory()) {
            return serverLib;
        }
        return null;
    }

    @CheckForNull
    public File getDomainLibDirectory() {
        if (config.isRemote()) {
            return null;
        }
        File domainLib = new File(config.getDomainHome(), "lib"); // NOI18N
        if (domainLib.exists() && domainLib.isDirectory()) {
            return domainLib;
        }
        return null;
    }

    @NonNull
    public File getWeblogicJar() {
        return getWeblogicJar(config.getServerHome());
    }

    @NonNull
    public static File getWeblogicJar(@NonNull File serverHome) {
        File weblogicJar = FileUtil.normalizeFile(new File(serverHome, WEBLOGIC_JAR));
        return weblogicJar;
    }

    /**
     * Gets the list of registered domains according to the given server
     * installation root
     *
     * @param serverRoot the server's installation location
     *
     * @return an array if strings with the paths
     */
    public static String[] getRegisteredDomainPaths(String serverRoot) {
        // init the resulting vector
        List<String> result = new ArrayList<String>(getDomainsFromRegistry(serverRoot));
        if (result.isEmpty()) {
            result.addAll(getDomainsFromNodeManager(serverRoot));
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns map of server domain configuration properties red from config.xml file.
     * Only properties required for the moment are returned.
     * Method implementation should be extended for additional properties.
     * return server configuration properties
     */
    public static Properties getDomainProperties(String domainPath) {
        Properties properties = new Properties();
        String configPath = domainPath + "/config/config.xml"; // NOI18N

        // init the input stream for the file and the w3c document object
        InputStream inputStream = null;
        Document document = null;

        try {
            // open the stream from the instances config file
            File config = new File(configPath);
            if (!config.exists()){
                LOGGER.log(Level.FINE, "Domain config file "
                        + "is not found. Probably server configuration was "
                        + "changed externally"); // NOI18N
                return properties;
            }
            inputStream = new FileInputStream(config);

            // parse the document
            document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(inputStream);

            // get the root element
            Element root = document.getDocumentElement();

            // get the child nodes
            NodeList children = root.getChildNodes();

            String adminServer = null;
            LinkedHashMap<String, ServerDescriptor> servers = new LinkedHashMap<String, ServerDescriptor>();
            // for each child
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if ("name".equals(child.getNodeName())) {
                    String domainName = child.getFirstChild().getNodeValue();
                    properties.put(DOMAIN_NAME, domainName);
                } else if ("domain-version".equals(child.getNodeName())) {
                    String domainVersion = child.getFirstChild().getNodeValue();
                    properties.put(DOMAIN_VERSION, domainVersion);
                } else if ("production-mode-enabled".equals(child.getNodeName())) {
                    String isEnabled = child.getFirstChild().getNodeValue();
                    properties.put(PRODUCTION_MODE, "true".equals(isEnabled));
                } else if (ADMIN_SERVER_PATTERN.matcher(child.getNodeName()).matches()) {
                    adminServer = child.getFirstChild().getNodeValue();
                // if the child's name equals 'server' get its children
                // and iterate over them
                } else if (SERVER_PATTERN.matcher(child.getNodeName()).matches()) {
                    NodeList nl = child.getChildNodes();

                    // declare the server's name/host/port
                    String name = ""; // NOI18N
                    String port = ""; // NOI18N
                    String host = ""; // NOI18N

                    // iterate over the children
                    for (int k = 0; k < nl.getLength(); k++) {
                        Node ch = nl.item(k);

                        // if the child's name equals 'name' fetch the
                        // instance's name
                        if (NAME_PATTERN.matcher(ch.getNodeName()).matches()) {
                            name = ch.getFirstChild().getNodeValue();
                        }

                        // if the child's name equals 'listen-port' fetch the
                        // instance's port
                        if (LISTEN_PORT_PATTERN.matcher(ch.getNodeName())
                                .matches()) {
                            port = ch.getFirstChild().getNodeValue();
                        }

                        // if the child's name equals 'listen-address' fetch the
                        // instance's host
                        if (LISTEN_ADDRESS_PATTERN.matcher(ch.getNodeName())
                                .matches()) {
                            if (ch.hasChildNodes()) {
                                host = ch.getFirstChild().getNodeValue();
                            }
                        }
                    }

                    if (port != null) {
                        port = port.trim();
                    }

                    // if all the parameters were fetched successfully add
                    // them to the result
                    if ((name != null) && (!name.equals(""))) { // NOI18N
                        // address and port have minOccurs=0 and are missing in
                        // 90 examples server
                        port = (port == null || port.equals("")) // NOI18N
                                ? Integer.toString(DEFAULT_PORT)
                                : port;
                        host = (host == null || host.equals(""))
                                ? "localhost" // NOI18N
                                : host;

                        servers.put(name, new ServerDescriptor(host, port, name));
                    }
                }
            }
            ServerDescriptor admin = null;
            if (adminServer != null) {
                admin = servers.get(adminServer);
            }
            if (admin == null && !servers.isEmpty()) {
                admin = servers.entrySet().iterator().next().getValue();
            }
            if (admin != null) {
                properties.put(DOMAIN_PORT, admin.getPort());
                properties.put(DOMAIN_HOST, admin.getHost());
                properties.put(ADMIN_SERVER_NAME, admin.getName());
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, null, e);
        } catch (ParserConfigurationException | SAXException e) {
            LOGGER.log(Level.INFO, null, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }
        return properties;
    }

    public static boolean isSupportedLayout(File candidate){
        return null != candidate
                && candidate.canRead()
                && candidate.isDirectory()
                && hasRequiredChildren(candidate, EXPECTED_FILES);
    }

    /**
     * Checks whether the server root contains weblogic.jar of version 9, 10 or 11.
     */
    public static boolean isSupportedVersion(Version version) {
        return version != null && (Integer.valueOf(9).equals(version.getMajor())
                    || Integer.valueOf(10).equals(version.getMajor())
                    || Integer.valueOf(11).equals(version.getMajor())
                    || Integer.valueOf(12).equals(version.getMajor()));
    }

    @NonNull
    public File[] getClassPath() {
        File weblogicJar = getWeblogicJar();
        if (!weblogicJar.exists()) {
            LOGGER.log(Level.INFO, "File {0} does not exist for {1}",
                    new Object[] {weblogicJar.getAbsolutePath(), config.getServerHome()});
            return new File[] {weblogicJar};
        }

        // we will add weblogic.server.modules jar manually as the path is hardcoded
        // and may not be valid see #189537 and #206259
        String serverModulesJar = null;
        try {
            // JarInputStream cannot be used due to problem in weblogic.jar in Oracle Weblogic Server 10.3
            JarFile jar = new JarFile(weblogicJar);
            try {
                Manifest manifest = jar.getManifest();
                if (manifest != null) {
                    String classpath = manifest.getMainAttributes()
                            .getValue("Class-Path"); // NOI18N
                    String[] elements = classpath.split("\\s+"); // NOI18N
                    for (String element : elements) {
                        if (element.contains("weblogic.server.modules")) { // NOI18N
                            File ref = new File(weblogicJar.getParentFile(), element);
                            if (!ref.exists()) {
                                LOGGER.log(Level.INFO, "Broken {0} classpath file {1} for {2}",
                                        new Object[] {weblogicJar.getAbsolutePath(), ref.getAbsolutePath(), config.getServerHome()});
                            }
                            serverModulesJar = element;
                            // last element of ../../../modules/something
                            int index = serverModulesJar.lastIndexOf("./"); // NOI18N
                            if (index >= 0) {
                                serverModulesJar = serverModulesJar.substring(index + 1);
                            }
                        }
                    }
                }
            } finally {
                try {
                    jar.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.FINEST, null, ex);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.FINE, null, e);
        }

        if (serverModulesJar != null) {
            // XXX cache values
            File mwHome = getMiddlewareHome(config.getServerHome());
            if (mwHome != null) {
                File serverModuleFile = FileUtil.normalizeFile(new File(mwHome,
                        serverModulesJar.replaceAll("/", Matcher.quoteReplacement(File.separator)))); // NOI18N
                return new File[] {weblogicJar, serverModuleFile};
            }
        }

        return new File[] {weblogicJar};
    }

    @CheckForNull
    public Version getServerVersion() {
        return getServerVersion(config.getServerHome());
    }

    @CheckForNull
    public static Version getServerVersion(@NonNull File serverHome) {
        File weblogicJar = getWeblogicJar(serverHome);
        if (!weblogicJar.exists()) {
            return null;
        }
        try {
            // JarInputStream cannot be used due to problem in weblogic.jar in Oracle Weblogic Server 10.3
            JarFile jar = new JarFile(weblogicJar);
            try {
                Manifest manifest = jar.getManifest();
                String implementationVersion = null;
                if (manifest != null) {
                    implementationVersion = manifest.getMainAttributes()
                            .getValue("Implementation-Version"); // NOI18N
                }
                if (implementationVersion != null) { // NOI18N
                    implementationVersion = implementationVersion.trim();
                    return Version.fromJsr277OrDottedNotationWithFallback(implementationVersion);
                }
            } finally {
                try {
                    jar.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.FINEST, null, ex);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.FINE, null, e);
        }
        return null;
    }

    @CheckForNull
    public Version getDomainVersion() {
        // Domain config file
        File configFile = getDomainConfigFile();

        // Check if the file exists
        if (configFile == null || !configFile.exists()) {
            return null;
        }

        try {
            InputSource source = new InputSource(new FileInputStream(configFile));
            Document d = XMLUtil.parse(source, false, false, null, null);

            // Retrieve domain version
            if (d.getElementsByTagName("domain-version").getLength() > 0) {
                String strVersion = d.getElementsByTagName("domain-version").item(0).getTextContent();
                return  strVersion != null ? Version.fromJsr277OrDottedNotationWithFallback(strVersion) : null;
            }
        } catch(IOException | SAXException e) {
            LOGGER.log(Level.INFO, null, e);
        }

        return null;
    }

    @CheckForNull
    public File getMiddlewareHome() {
        return getMiddlewareHome(config.getServerHome());
    }

    @CheckForNull
    public static File getMiddlewareHome(@NonNull File serverHome) {
        Properties ret = new Properties();
        File productProps = new File(serverHome, ".product.properties"); // NOI18N

        if (!productProps.exists() || !productProps.canRead()) {
            return getMiddlewareHome(serverHome, null);
        }
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(productProps));
            try {
                ret.load(is);
            } finally {
                is.close();
            }
            return getMiddlewareHome(serverHome, ret.getProperty("MW_HOME"));
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return getMiddlewareHome(serverHome, null);
        }
    }

    @CheckForNull
    private static File getMiddlewareHome(@NonNull File serverHome, @NullAllowed String mwHome) {
        File middleware = null;
        if (mwHome != null) {
            middleware = new File(mwHome);
        }
        if (middleware == null || !middleware.exists() || !middleware.isDirectory()) {
            middleware = serverHome.getParentFile();
        }

        if (middleware != null && middleware.exists() && middleware.isDirectory()) {
            return middleware;
        }
        return null;
    }

    private static List<String> getDomainsFromNodeManager(String serverRoot) {
        // is the server root was not defined, return an empty array of domains
        if (serverRoot == null) {
            return Collections.emptyList();
        }

        // init the input stream for the file and the w3c document object
        File file = new File(serverRoot + File.separator
                + DOMAIN_LIST.replaceAll("/", Matcher.quoteReplacement(File.separator)));
        if (!file.exists() || !file.canRead()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>();
        BufferedReader lnr = null;

        // read the list file line by line fetching out the domain paths
        try {
            // create a new reader for the FileInputStream
            lnr = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            // read the lines
            String line;
            while ((line = lnr.readLine()) != null) {
                // skip the comments
                if (line.startsWith("#")) {  // NOI18N
                    continue;
                }

                // fetch the domain path
                String path = line.split("=")[1].replaceAll("\\\\\\\\", "/").replaceAll("\\\\:", ":"); // NOI18N

                // add the path to the resulting set
                result.add(path);
            }
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.INFO, null, e);   // NOI18N
        } catch (IOException e) {
            LOGGER.log(Level.INFO, null, e);   // NOI18N
        } finally {
            try {
                // close the stream
                if (lnr != null) {
                    lnr.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);  // NOI18N
            }
        }
        return result;
    }

    private static List<String> getDomainsFromRegistry(String serverRoot) {
        // is the server root was not defined, return an empty array of domains
        if (serverRoot == null) {
            return Collections.emptyList();
        }

        File mwHome = getMiddlewareHome(new File(serverRoot));
        if (mwHome == null) {
            return Collections.emptyList();
        }
        // init the input stream for the file and the w3c document object
        File file = new File(mwHome, DOMAIN_REGISTRY);
        if (!file.exists() || !file.canRead()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>();
        // init the input stream for the file and the w3c document object
        InputStream inputStream = null;
        Document document = null;

        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));

            // parse the document
            document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(inputStream);

            // get the root element
            Element root = document.getDocumentElement();

            // get the child nodes
            NodeList children = root.getChildNodes();

            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if ("domain".equals(child.getNodeName())) { // NOI18N
                    Node attr = child.getAttributes().getNamedItem("location"); // NOI18N
                    if (attr != null) {
                        String location = attr.getNodeValue();
                        if (location != null) {
                            result.add(location);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, null, e);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.INFO, null, e);
        } catch (SAXException e) {
            LOGGER.log(Level.INFO, null, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }
        return result;
    }

    private static boolean hasRequiredChildren(File candidate, Collection<String> requiredChildren) {
        if (null == candidate) {
            return false;
        }
        String[] children = candidate.list();
        if (null == children) {
            return false;
        }
        if (null == requiredChildren) {
            return true;
        }

        for (Iterator<String> it = requiredChildren.iterator(); it.hasNext();) {
            String next = it.next();
            File test = new File(candidate.getPath() + File.separator + next);
            if (!test.exists()) {
                return false;
            }
        }
        return true;
    }

    private static class ServerDescriptor {

        private final String host;

        private final String port;

        private final String name;

        public ServerDescriptor(String host, String port, String name) {
            this.host = host;
            this.port = port;
            this.name = name;
        }

        public String getHost() {
            return host;
        }

        public String getName() {
            return name;
        }

        public String getPort() {
            return port;
        }
    }
}
