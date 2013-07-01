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
package org.netbeans.modules.web.browser.api;

import java.awt.Image;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.browser.spi.BrowserURLMapperImplementation;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;

/**
 * Single browser registered in the IDE.
 */
public final class WebBrowser {

    private WebBrowserFactoryDescriptor factoryDesc;

    WebBrowser(WebBrowserFactoryDescriptor factoryDesc) {
        this.factoryDesc = factoryDesc;
    }
    
    /**
     * Unique ID of browser. Useful for example to store per project reference to
     * user's browser choice.
     */
    public String getId() {
        return factoryDesc.getId();
    }

    public boolean hasNetBeansIntegration() {
        return factoryDesc.hasNetBeansIntegration();
    }

    /**
     * Name eg. FireFox, WebView, ...
     *
     * @return
     */
    public String getName() {
        return factoryDesc.getName();
    }

    public Image getIconImage() {
        Image im = factoryDesc.getIconImage();
        if (im == null) {
            ImageIcon icon = ImageUtilities.loadImageIcon( getIconFile(getBrowserFamily()), true );
            im = ImageUtilities.icon2Image( icon );
        }
        if (hasNetBeansIntegration() && factoryDesc.getBrowserFamily() != BrowserFamilyId.JAVAFX_WEBVIEW) {
            im = ImageUtilities.mergeImages(
                im,
                ImageUtilities.loadImage("org/netbeans/modules/web/browser/ui/resources/nb-badge.png"),
            12, 12);
        }
        return im;
    }
    
    public BrowserFamilyId getBrowserFamily() {
        return factoryDesc.getBrowserFamily();
    }

    /**
     * Is IDE embedded browser or external browser.
     */
    public boolean isEmbedded() {
        return getBrowserFamily() == BrowserFamilyId.JAVAFX_WEBVIEW;
    }

    private static Map<Project, BrowserURLMapperImplementation.BrowserURLMapper> browserMappings =
            new WeakHashMap<>();

    /**
     * Let browser implementation convert given URL into a browser specific URL
     * before opening it in browser. For example Android device Chrome browser
     * converts localhost URL into IP address so that Android device can access
     * the locahost.
     * @return converted or original URL
     */
    public URL toBrowserURL(Project p, FileObject projectFile, URL serverURL) {
        BrowserURLMapperImplementation impl = factoryDesc.getBrowserURLMapper();
        if (impl != null) {
            BrowserURLMapperImplementation.BrowserURLMapper m = impl.toBrowser(p, projectFile, serverURL);
            if (m != null) {
                browserMappings.put(p, m);
                String url = WebUtils.urlToString(serverURL);
                if (url.startsWith(m.getServerURLRoot())) {
                    url = m.getBrowserURLRoot() + url.substring(m.getServerURLRoot().length());
                    return WebUtils.stringToUrl(url);
                }
            } else {
                browserMappings.remove(p);
            }
        }
        return serverURL;
    }

    /**
     * Let browser implementation convert given browser URL into an URL which
     * can be translated back to project's source file. This is counter part for
     * {@link #toBrowserURL} method which translates browser specific URL back
     * to a URL which can be translated into project's source file.
     * @return converted or original URL
     */
    public URL fromBrowserURL(Project p, URL serverURL) {
        BrowserURLMapperImplementation.BrowserURLMapper m = browserMappings.get(p);
        if (m != null) {
            String url = WebUtils.urlToString(serverURL);
            if (url.startsWith(m.getBrowserURLRoot())) {
                url = m.getServerURLRoot()+ url.substring(m.getBrowserURLRoot().length());
            }
            return WebUtils.stringToUrl(url);
        }
        return serverURL;
    }

    /**
     * This methods creates new browser "pane", that is tab in external browser
     * or TopComponent for embedded browser. Through this method clients have control 
     * how many browser panes are opened. In case of embedded browser it is 
     * straightforward - each call of this method will result into a new TopComponent. 
     * In case of external browser situation depends on availability of NetBeans 
     * browser plugins. If browser plugins are available then the same behaviour as 
     * in the case of embedded browser is possible and user can via this method 
     * create multiple tabs in external browser or keep single tab and open all 
     * URLs in the single tab.
     */
    public WebBrowserPane createNewBrowserPane() {
        return createNewBrowserPane(true);
    }
    
    public WebBrowserPane createNewBrowserPane(WebBrowserFeatures features) {
        return createNewBrowserPane(features, true);
    }
    /**
     * The only difference from createNewBrowserPane() is that automatic TopComponent
     * creation in case of embedded browser can be prevented by setting 
     * wrapEmbeddedBrowserInTopComponent to false. Doing that means that client
     * of WebBrowserPane must call WebBrowserPane.getComponent method and 
     * take care about showing browser component in IDE. This is useful for example
     * in case when HTML file editor has multiview and one of its tabs is "Preview"
     * showing rendered view of the HTML document.
     */
    public WebBrowserPane createNewBrowserPane(boolean wrapEmbeddedBrowserInTopComponent) {
        return createNewBrowserPane(new WebBrowserFeatures(), wrapEmbeddedBrowserInTopComponent);
    }

    public WebBrowserPane createNewBrowserPane(WebBrowserFeatures features, boolean wrapEmbeddedBrowserInTopComponent) {
        return new WebBrowserPane(features, factoryDesc, wrapEmbeddedBrowserInTopComponent);
    }

    /**
     * Retrieve HTMLBrowser factory wrapped in this instance.
     * @return HtmlBrowser factory.
     */
    public HtmlBrowser.Factory getHtmlBrowserFactory() {
        return factoryDesc.getFactory();
    }

    WebBrowserFactoryDescriptor getFactoryDesc() {
        return factoryDesc;
    }

    private static String getIconFile(BrowserFamilyId browserFamily) {
        switch (browserFamily) {
            case CHROME:
                return "org/netbeans/modules/web/browser/ui/resources/browser-chrome.png";
            case FIREFOX:
                return "org/netbeans/modules/web/browser/ui/resources/browser-firefox.png";
            case CHROMIUM:
                return "org/netbeans/modules/web/browser/ui/resources/browser-chromium.png";
            case IE:
                return "org/netbeans/modules/web/browser/ui/resources/browser-ie.png";
            case SAFARI:
                return "org/netbeans/modules/web/browser/ui/resources/browser-safari.png";
            default:
                return "org/netbeans/modules/web/browser/ui/resources/browser-generic.png";
        }
            
       
    }
}
