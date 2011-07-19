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
package org.netbeans.modules.maven.jaxws;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.javaee.specs.support.api.JaxRsStackSupport;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.websvc.api.jaxws.project.LogUtils;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.spi.WebRestSupport;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.modules.websvc.wsstack.jaxrs.JaxRs;
import org.netbeans.modules.websvc.wsstack.jaxrs.JaxRsStackProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Nam Nguyen
 */
@ProjectServiceProvider(service=RestSupport.class, projectType="org-netbeans-modules-maven")
public class MavenProjectRestSupport extends WebRestSupport {

    private static final String DEPLOYMENT_GOAL = "package";             //NOI18N   

    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance";   //NOI18N
    
    public static final String DIRECTORY_DEPLOYMENT_SUPPORTED = "directory.deployment.supported"; // NOI18N
    
    public static final String ACTION_PROPERTY_DEPLOY_OPEN = "netbeans.deploy.open.in.browser"; //NOI18N
    
    private static final String TEST_SERVICES_HTML = "test-services.html"; //NOI18N

    String[] classPathTypes = new String[]{
                ClassPath.COMPILE
            };

    /** Creates a new instance of WebProjectRestSupport */
    public MavenProjectRestSupport(Project project) {
        super(project);
    }

    @Override
    public void upgrade() {
        if (!isRestSupportOn()) {
            return;
        }
        try {
            //Fix issue#141595, 154378
//            addSwdpLibrary();

            FileObject ddFO = getDeploymentDescriptor();
            if (ddFO == null) {
                return;
            }

            WebApp webApp = findWebApp();
            if (webApp == null) {
                return;
            }

            Servlet adaptorServlet = getRestServletAdaptorByName(webApp, REST_SERVLET_ADAPTOR);
            if (adaptorServlet != null) {
                // Starting with jersey 0.8, the adaptor class is under 
                // com.sun.jersey package instead of com.sun.we.rest package.
                if (REST_SERVLET_ADAPTOR_CLASS_OLD.equals(adaptorServlet.getServletClass())) {
                    boolean isSpring = hasSpringSupport();
                    if (isSpring) {
                        adaptorServlet.setServletClass(REST_SPRING_SERVLET_ADAPTOR_CLASS);
                        InitParam initParam =
                                (InitParam) adaptorServlet.findBeanByName("InitParam", //NOI18N
                                "ParamName", //NOI18N
                                JERSEY_PROP_PACKAGES);
                        if (initParam == null) {
                            try {
                                initParam = (InitParam) adaptorServlet.createBean("InitParam"); //NOI18N
                                initParam.setParamName(JERSEY_PROP_PACKAGES);
                                initParam.setParamValue("."); //NOI18N
                                initParam.setDescription(JERSEY_PROP_PACKAGES_DESC);
                                adaptorServlet.addInitParam(initParam);
                            } catch (ClassNotFoundException ex) {}
                        }
                    } else {
                        adaptorServlet.setServletClass(REST_SERVLET_ADAPTOR_CLASS);
                    }
                    webApp.write(ddFO);
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    @Override
    public void extendBuildScripts() throws IOException {
    }

    @Override
    public void ensureRestDevelopmentReady() throws IOException {
        String configType = getProjectProperty(PROP_REST_CONFIG_TYPE);
        WebRestSupport.RestConfig restConfig = null;
        if (configType == null && getApplicationPathFromDD() == null) {
            restConfig = setApplicationConfigProperty(false);
            if (restConfig == WebRestSupport.RestConfig.DD) {
                addResourceConfigToWebApp(restConfig.getResourcePath());
            }
        }

        addSwdpLibrary( restConfig );
    }

    @Override
    public void removeRestDevelopmentReadiness() throws IOException {
        removeResourceConfigFromWebApp();
        removeSwdpLibrary(new String[]{ClassPath.COMPILE} );
    }

    @Override
    public boolean isReady() {
        return isRestSupportOn() && hasSwdpLibrary() && hasRestServletAdaptor();
    }
    
    public String getContextRootURL() {
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        String serverInstanceID = provider.getServerInstanceID();
        if (WSStackUtils.DEVNULL.equals(serverInstanceID)) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(RestSupport.class, "MSG_MissingServer"), 
                    NotifyDescriptor.ERROR_MESSAGE));
            return "";
        } 
        else {
            return super.getContextRootURL();
        }
    }

    private boolean platformHasRestLib(J2eePlatform j2eePlatform) {
        if (j2eePlatform != null) {
            WSStack<JaxRs> wsStack = JaxRsStackProvider.getJaxRsStack(j2eePlatform);
            if (wsStack != null) {
                return wsStack.isFeatureSupported(JaxRs.Feature.JAXRS);
            }
        }
        return false;
    }

    @Override
    public boolean hasSwdpLibrary() {
        SourceGroup[] srcGroups = ProjectUtils.getSources(project).getSourceGroups(
        JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (srcGroups.length > 0) {
            ClassPath classPath = ClassPath.getClassPath(srcGroups[0].getRootFolder(), ClassPath.COMPILE);
            FileObject contextFO = classPath.findResource("javax/ws/rs/core/Context.class"); // NOI18N
            return contextFO != null;
        }
        return false;
    }
    
    @Override
    public String getBaseURL() throws IOException {
        WebApp webApp = getWebApp();
        if (webApp != null) {
            String servletNames = "";
            String urlPatterns = "";
            int i=0;
            for (ServletMapping mapping : webApp.getServletMapping()) {
                servletNames+=(i>0 ? ",":"")+mapping.getServletName();
                urlPatterns+= (i>0 ? ",":"")+mapping.getUrlPattern();
                i++;
            }
            http://localhost:8084/mavenprojectWeb3/||ServletAdaptor||resources/*
            return getContextRootURL()+"||"+servletNames+"||"+urlPatterns;
        } else {
            throw new IOException("Cannot read web.xml");
        }
    }

    private void addSwdpLibrary( RestConfig config ) throws IOException {
        boolean addLibrary = false;
        if (!hasSwdpLibrary()) { //platform does not have rest-api library, so add defaults
            addLibrary = true;
            boolean jsr311Added = false;
            if (config != null && config.isServerJerseyLibSelected() ) {
                JaxRsStackSupport support = getJaxRsStackSupport();
                if ( support != null ){
                    jsr311Added  = support.addJsr311Api(project);
                }
            }
            if ( !jsr311Added ){
                JaxRsStackSupport.getDefault().addJsr311Api(project);
            }
        }
        
        if (config != null) {
            boolean added = false;
            if (config.isServerJerseyLibSelected()) {
                JaxRsStackSupport support = getJaxRsStackSupport();
                if ( support != null ){
                    added  = support.extendsJerseyProjectClasspath(project);
                }
            }
            if (!added && config.isJerseyLibSelected()) {
                JaxRsStackSupport.getDefault().extendsJerseyProjectClasspath(project);
            }
        }
        else if (addLibrary ){
            JaxRsStackSupport.getDefault().extendsJerseyProjectClasspath(project);
        }
    }

    @Override
    public Datasource getDatasource(String jndiName) {
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);

        try {
            return provider.getConfigSupport().findDatasource(jndiName);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    @Override
    public boolean isRestSupportOn() {
        return true;
    }
    
    @Override
    public FileObject generateTestClient(File testdir, String url ) throws IOException {
        return generateMavenTester(testdir, url );
    }
    
    @Override
    public FileObject generateTestClient(File testdir ) throws IOException {
        return generateMavenTester(testdir, getBaseURL() );
    }
    
    @Override
    public void deploy() {
        RunConfig config = RunUtils.createRunConfig(FileUtil.toFile(
                getProject().getProjectDirectory()), project, 
                NbBundle.getMessage(MavenProjectRestSupport.class, "MSG_Deploy",    // NOI18N
                        getProject().getLookup().lookup(
                                ProjectInformation.class).getDisplayName()), 
                Collections.singletonList(DEPLOYMENT_GOAL));
        config.setProperty(ACTION_PROPERTY_DEPLOY_OPEN, Boolean.FALSE.toString() );
        RunUtils.executeMaven(config);
    }
    
    @Override
    public File getLocalTargetTestRest(){
        try {
            FileObject mainFolder = project.getProjectDirectory()
                    .getFileObject("src/main"); // NOI18N
            if (mainFolder != null) {
                FileObject resourcesFolder = mainFolder
                        .getFileObject("resources"); // NOI18N
                if (resourcesFolder == null) {
                    resourcesFolder = mainFolder.createFolder("resources"); // NOI18N
                }
                if (resourcesFolder != null) {
                    FileObject restFolder = resourcesFolder
                            .getFileObject("rest"); // NOI18N
                    if (restFolder == null) {
                        restFolder = resourcesFolder.createFolder("rest"); // NOI18N
                    }
                    return FileUtil.toFile(restFolder);
                }
            }
        }
        catch (IOException e) {
            Logger.getLogger( MavenProjectRestSupport.class.getName() ).log(Level.WARNING, 
                    null, e);
        }
        return null;
    }

    private FileObject generateMavenTester(File testdir, String baseURL) throws IOException {
        String[] replaceKeys1 = {
            "TTL_TEST_RESBEANS", "MSG_TEST_RESBEANS_INFO"
        };
        String[] replaceKeys2 = {
            "MSG_TEST_RESBEANS_wadlErr", "MSG_TEST_RESBEANS_No_AJAX", "MSG_TEST_RESBEANS_Resource",
            "MSG_TEST_RESBEANS_See", "MSG_TEST_RESBEANS_No_Container", "MSG_TEST_RESBEANS_Content",
            "MSG_TEST_RESBEANS_TabularView", "MSG_TEST_RESBEANS_RawView", "MSG_TEST_RESBEANS_ResponseHeaders",
            "MSG_TEST_RESBEANS_Help", "MSG_TEST_RESBEANS_TestButton", "MSG_TEST_RESBEANS_Loading",
            "MSG_TEST_RESBEANS_Status", "MSG_TEST_RESBEANS_Headers", "MSG_TEST_RESBEANS_HeaderName",
            "MSG_TEST_RESBEANS_HeaderValue", "MSG_TEST_RESBEANS_Insert", "MSG_TEST_RESBEANS_NoContents",
            "MSG_TEST_RESBEANS_AddParamButton", "MSG_TEST_RESBEANS_Monitor", "MSG_TEST_RESBEANS_No_SubResources",
            "MSG_TEST_RESBEANS_SubResources", "MSG_TEST_RESBEANS_ChooseMethod", "MSG_TEST_RESBEANS_ChooseMime",
            "MSG_TEST_RESBEANS_Continue", "MSG_TEST_RESBEANS_AdditionalParams", "MSG_TEST_RESBEANS_INFO",
            "MSG_TEST_RESBEANS_Request", "MSG_TEST_RESBEANS_Sent", "MSG_TEST_RESBEANS_Received",
            "MSG_TEST_RESBEANS_TimeStamp", "MSG_TEST_RESBEANS_Response", "MSG_TEST_RESBEANS_CurrentSelection",
            "MSG_TEST_RESBEANS_DebugWindow", "MSG_TEST_RESBEANS_Wadl", "MSG_TEST_RESBEANS_RequestFailed"

        };
        FileObject testFO = copyFileAndReplaceBaseUrl(testdir, TEST_SERVICES_HTML, replaceKeys1, baseURL);
        copyFile(testdir, TEST_RESBEANS_JS, replaceKeys2, false);
        copyFile(testdir, TEST_RESBEANS_CSS);
        copyFile(testdir, TEST_RESBEANS_CSS2);
        copyFile(testdir, "expand.gif");
        copyFile(testdir, "collapse.gif");
        copyFile(testdir, "item.gif");
        copyFile(testdir, "cc.gif");
        copyFile(testdir, "og.gif");
        copyFile(testdir, "cg.gif");
        copyFile(testdir, "app.gif");

        File testdir2 = new File(testdir, "images");
        testdir2.mkdir();
        copyFile(testdir, "images/background_border_bottom.gif");
        copyFile(testdir, "images/pbsel.png");
        copyFile(testdir, "images/bg_gradient.gif");
        copyFile(testdir, "images/pname.png");
        copyFile(testdir, "images/level1_selected-1lvl.jpg");
        copyFile(testdir, "images/primary-enabled.gif");
        copyFile(testdir, "images/masthead.png");
        copyFile(testdir, "images/primary-roll.gif");
        copyFile(testdir, "images/pbdis.png");
        copyFile(testdir, "images/secondary-enabled.gif");
        copyFile(testdir, "images/pbena.png");
        copyFile(testdir, "images/tbsel.png");
        copyFile(testdir, "images/pbmou.png");
        copyFile(testdir, "images/tbuns.png");
        return testFO;
    }

    /*
     * Copy File, as well as replace tokens, overwrite if specified
     */
    private FileObject copyFileAndReplaceBaseUrl(File testdir, String name, String[] replaceKeys, String baseURL) throws IOException {
        FileObject dir = FileUtil.toFileObject(testdir);
        FileObject fo = dir.getFileObject(name);
        if (fo == null) {
            fo = dir.createData(name);
        }
        FileLock lock = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            lock = fo.lock();
            OutputStream os = fo.getOutputStream(lock);
            writer = new BufferedWriter(new OutputStreamWriter(os));
            InputStream is = RestSupport.class.getResourceAsStream("resources/"+name);
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            String lineSep = "\n";//Unix
            if(File.separatorChar == '\\')//Windows
                lineSep = "\r\n";
            String[] replaceValues = null;
            if(replaceKeys != null) {
                replaceValues = new String[replaceKeys.length];
                for(int i=0;i<replaceKeys.length;i++)
                    replaceValues[i] = NbBundle.getMessage(RestSupport.class, replaceKeys[i]);
            }
            while((line = reader.readLine()) != null) {
                for(int i=0;i<replaceKeys.length;i++) {
                    line = line.replaceAll(replaceKeys[i], replaceValues[i]);
                }
                line = line.replace("${BASE_URL}", baseURL);
                writer.write(line);
                writer.write(lineSep);
            }
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
            if (lock != null) lock.releaseLock();
            if (reader != null) {
                reader.close();
            }
        }
        return fo;
    }

    @Override
    protected void logResourceCreation(Project prj) {
        Object[] params = new Object[3];
        params[0] = LogUtils.WS_STACK_JAXRS;
        params[1] = project.getClass().getName();
        params[2] = "RESOURCE"; // NOI18N
        LogUtils.logWsDetect(params);
    }

    @Override
    public String getProjectProperty(String name) {
        Preferences prefs = ProjectUtils.getPreferences(project, MavenProjectRestSupport.class, true);
        if (prefs != null) {
            return prefs.get(name, null);
        }
        return null;
    }

    @Override
    public void setProjectProperty(String name, String value) {
        Preferences prefs = ProjectUtils.getPreferences(project, MavenProjectRestSupport.class, true);
        if (prefs != null) {
            prefs.put(name, value);
        }
    }

    @Override
    public void removeProjectProperties(String[] propertyNames) {
        Preferences prefs = ProjectUtils.getPreferences(project, MavenProjectRestSupport.class, true);
        if (prefs != null) {
            for (String p : propertyNames) {
                prefs.remove(p);
            }
        }
    }

    @Override
    public int getProjectType() {
        NbMavenProject nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
        if (nbMavenProject != null) {
            String packagingType = nbMavenProject.getPackagingType();
            if (packagingType != null)
            if (NbMavenProject.TYPE_JAR.equals(packagingType)) {
                return PROJECT_TYPE_DESKTOP;
            } else if (NbMavenProject.TYPE_WAR.equals(packagingType)) {
                return PROJECT_TYPE_WEB;
            } else if (NbMavenProject.TYPE_NBM.equals(packagingType) ||
                       NbMavenProject.TYPE_NBM_APPLICATION.equals(packagingType)) {
                return PROJECT_TYPE_NB_MODULE;
            }
        }
        return PROJECT_TYPE_DESKTOP;
    }
    

   
}
