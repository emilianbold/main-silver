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
package org.netbeans.modules.maven.j2ee.web;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.api.web.model.ServletInfo;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.j2ee.ui.customizer.impl.CustomizerRunWeb;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.WebBrowserSupport;
import org.netbeans.modules.web.browser.spi.PageInspectorCustomizer;
import org.netbeans.modules.web.browser.spi.URLDisplayerImplementation;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.common.spi.ServerURLMappingImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Janicek
 */
@ProjectServiceProvider(
    service = {
        ServerURLMappingImplementation.class,
        URLDisplayerImplementation.class,
        PageInspectorCustomizer.class
    },
    projectType = {
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR
    }
)
public final class ClientSideDevelopmentSupport implements
        ServerURLMappingImplementation,
        URLDisplayerImplementation,
        PageInspectorCustomizer,
        PropertyChangeListener {

    private final Project project;
    private volatile String projectRootURL;
    private volatile FileObject webDocumentRoot;
    // @GuardedBy("this")
    private BrowserSupport browserSupport = null;
    // @GuardedBy("this")
    private boolean browserSupportInitialized = false;

    public ClientSideDevelopmentSupport(Project project) {
        assert project != null;
        this.project = project;
    }

    @Override
    public void showURL(URL applicationRootURL, URL urlToOpenInBrowser, FileObject context) {
        projectRootURL = WebUtils.urlToString(applicationRootURL);
        if (projectRootURL != null && !projectRootURL.endsWith("/")) {
            projectRootURL += "/";
        }
        BrowserSupport bs = getBrowserSupport();
        if (bs != null) {
            bs.load(urlToOpenInBrowser, context);
        } else {
            HtmlBrowser.URLDisplayer.getDefault().showURL(urlToOpenInBrowser);
        }
    }

    @Override
    public URL toServer(int projectContext, FileObject projectFile) {
        init();
        if (projectRootURL == null || webDocumentRoot == null) {
            return null;
        }
        String relPath = FileUtil.getRelativePath(webDocumentRoot, projectFile);
        relPath = applyServletPattern(relPath);
        try {
            return new URL(projectRootURL + relPath);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public FileObject fromServer(int projectContext, URL serverURL) {
        init();
        if (projectRootURL == null || webDocumentRoot == null) {
            return null;
        }
        String u = WebUtils.urlToString(serverURL);
        if (u.startsWith(projectRootURL)) {
            String name = u.substring(projectRootURL.length());
            if (name.isEmpty()) {
                // name is empty - try to map server URL to one of the welcome files:
                return getExistingWelcomeFile();
            } else {
                // use servlet mappings to map server URL to a project file:
                return convertServerURLToProjectFile(name);
            }
        }
        return null;
    }

    @Override
    public boolean isHighlightSelectionEnabled() {
        return true;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        // noop
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        // noop
    }

    private void init() {
        if (webDocumentRoot == null) {
            webDocumentRoot = getWebRoot();
        }
        readWebAppMetamodelData();
    }

    private FileObject getWebRoot() {
        WebModule webModule = getWebModule();
        return webModule != null ? webModule.getDocumentBase() : null;
    }
    
    private WebModule getWebModule() {
        return WebModule.getWebModule(project.getProjectDirectory());
    }

    public boolean canReload() {
        return WebBrowserSupport.isIntegratedBrowser(getSelectedBrowser());
    }

    public void reload(FileObject fo) {
        BrowserSupport bs = getBrowserSupport();
        if (bs == null) {
            return;
        }
        URL u = bs.getBrowserURL(fo, true);
        if (u == null) {
            // check if given file is one of the welcome files and therefore
            // project folder should be used for reload instead of welcome file:
            if (isWelcomeFile(fo)) {
                u = bs.getBrowserURL(project.getProjectDirectory(), true);
            }
        }
        if (u != null) {
            assert bs.canReload(u) : u;
            bs.reload(u);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (CustomizerRunWeb.PROP_SELECTED_BROWSER.equals(evt.getPropertyName())) {
            resetBrowserSupport();
        }
    }

    private synchronized void resetBrowserSupport() {
        if (browserSupport != null) {
            browserSupport.close(false);
        }
        browserSupport = null;
        browserSupportInitialized = false;
    }

    private synchronized BrowserSupport getBrowserSupport() {
        if (browserSupportInitialized) {
            return browserSupport;
        }
        browserSupportInitialized = true;
        String selectedBrowser = getSelectedBrowser();
        WebBrowser browser = WebBrowserSupport.getBrowser(selectedBrowser);
        if (browser == null) {
            browserSupport = null;
            return null;
        }
        boolean integrated = WebBrowserSupport.isIntegratedBrowser(selectedBrowser);
        browserSupport = BrowserSupport.create(browser, !integrated);
        return browserSupport;
    }

    private String getSelectedBrowser() {
        return (String) project.getProjectDirectory().getAttribute(CustomizerRunWeb.PROP_SELECTED_BROWSER);
    }
    
    /*
     * EASEL support --> Move to web.common together with almost identical Web Project implementation
     */
    
    private List<String> servletURLPatterns = new CopyOnWriteArrayList<String>();
    private List<String> welcomeFiles = new CopyOnWriteArrayList<String>();

    private void readWebAppMetamodelData() {
        final WebModule webModule = getWebModule();
        try {
            webModule.getMetadataModel().runReadAction(new MetadataModelAction<WebAppMetadata, Void>() {
                
                @Override
                public Void run(WebAppMetadata metadata) throws Exception {
                    List<String> l = new ArrayList<String>();
                    for (ServletInfo si : metadata.getServlets()) {
                        for (String pattern : si.getUrlPatterns()) {
                            // only some patterns are currently handled;
                            // see comments in convertServerURLToLocalFile method
                            if (!pattern.endsWith("*")) { // NOI18N
                                continue;
                            } else {
                                pattern = pattern.substring(0, pattern.length()-1);
                            }
                            if (pattern.startsWith("/")) { // NOI18N
                                pattern = pattern.substring(1);
                            }
                            l.add(pattern);
                        }
                    }
                    // WelcomeList file is not available in merged WebAppMetadata;
                    // below code will also ignore WelcomeList from web-fragment.xml which
                    // on the other hand should be OK most of the time - a framework/web library
                    // should not define what welcome files an application is going to have
                    FileObject fo = webModule.getDeploymentDescriptor();
                    if (fo != null) {
                        WebApp ddRoot = DDProvider.getDefault().getDDRoot(fo);
                        if (ddRoot != null && ddRoot.getSingleWelcomeFileList() != null) {
                            welcomeFiles.addAll(Arrays.asList(ddRoot.getSingleWelcomeFileList().getWelcomeFile()));
                        }
                    }
                    welcomeFiles.add("index.html"); // NOI18N
                    welcomeFiles.add("index.htm"); // NOI18N
                    welcomeFiles.add("index.jsp"); // NOI18N
                    servletURLPatterns.addAll(l);
                    return null;
                }
            });
        } catch (MetadataModelException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private FileObject getExistingWelcomeFile() {
        // try to map it to welcome-file-list:
        for (String welcomeFile : welcomeFiles) {
            for (String pattern : servletURLPatterns) {
                if (welcomeFile.startsWith(pattern)) {
                    FileObject fo = webDocumentRoot.getFileObject(welcomeFile.substring(pattern.length()));
                    if (fo != null) {
                        return fo;
                    }
                }
            }
            FileObject fo = webDocumentRoot.getFileObject(welcomeFile);
            if (fo != null) {
                return fo;
            }
        }
        return null;
    }

    private FileObject convertServerURLToProjectFile(String name) {
        // bellow code is limited to understand following simple usecase:
        // pattern "/faces/*" means that URL /faces/index.anything maps to
        // file web-root/index.anything and vice versa:
        for (String pattern : servletURLPatterns) {
            if (name.startsWith(pattern)) {
                FileObject fo = webDocumentRoot.getFileObject(name.substring(pattern.length()));
                if (fo != null) {
                    return fo;
                }
            }
        }
        return webDocumentRoot.getFileObject(name);
    }

    private boolean isWelcomeFile(FileObject context) {
        for (String welcomeFile : welcomeFiles) {
            for (String pattern : servletURLPatterns) {
                if (welcomeFile.startsWith(pattern)) {
                    FileObject fo = webDocumentRoot.getFileObject(welcomeFile.substring(pattern.length()));
                    if (fo != null && fo.equals(context)) {
                        return true;
                    }
                }
            }
            FileObject fo = webDocumentRoot.getFileObject(welcomeFile);
            if (fo != null && fo.equals(context)) {
                return true;
            }
        }
        return false;
    }

    // TODO: below code works well for JSF framework but could broke impl
    // of ServerURLMappingImplementation.toServer for a custom servlet; if
    // this turns to be a problem then readWebAppMetamodelData() should be
    // changed to read servlet URL patterns only from a well-known servlets
    // like JSF.
    private String applyServletPattern(String relPath) {
        for (String pattern : servletURLPatterns) {
            return pattern + relPath;
        }
        return relPath;
    }
}