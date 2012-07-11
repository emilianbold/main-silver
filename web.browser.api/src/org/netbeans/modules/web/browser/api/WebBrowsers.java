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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.core.IDESettings;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * Access to browsers available in the IDE.
 */
public final class WebBrowsers {

    /**
     * Property fired when list of browsers has changed.
     */
    public static final String PROP_BROWSERS = "browsers"; // NOI18N
    
    /**
     * Property fired when default browser has changed.
     */
    public static final String PROP_DEFAULT_BROWSER = "browser"; // NOI18N
    
    private static final WebBrowsers INST = new WebBrowsers();
    private static final String BROWSERS_FOLDER = "Services/Browsers"; // NOI18N
    
    //private WebBrowserFactories fact;
    private PropertyChangeSupport sup = new PropertyChangeSupport(this);
    private PropertyChangeListener l;
    private FileChangeListener lis;
    private PreferenceChangeListener lis2;
    
    private WebBrowsers() {
        sup = new PropertyChangeSupport(this);
        FileObject servicesBrowsers = getConfigFolder();
        if (servicesBrowsers != null) {
            lis = new FileChangeListener() {

                @Override
                public void fileFolderCreated(FileEvent fe) {
                }

                @Override
                public void fileDataCreated(FileEvent fe) {
                    fireBrowsersChange();
                }

                @Override
                public void fileChanged(FileEvent fe) {
                    fireBrowsersChange();
                }

                @Override
                public void fileDeleted(FileEvent fe) {
                    fireBrowsersChange();
                }

                @Override
                public void fileRenamed(FileRenameEvent fe) {
                    fireBrowsersChange();
                }

                @Override
                public void fileAttributeChanged(FileAttributeEvent fe) {
                    fireBrowsersChange();
                }
            };
            servicesBrowsers.addRecursiveListener(lis);
        }
        lis2 = new PreferenceChangeListener() {

            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                if (IDESettings.PROP_WWWBROWSER.equals(evt.getKey())) {
                    fireDefaultBrowserChange();
                }
            }
        };
        NbPreferences.forModule(IDESettings.class).addPreferenceChangeListener(lis2);
    }

    /**
     * Singleton instance of WebBrowsers class
     */
    public static WebBrowsers getInstance() {
        return INST;
    }
    
    /**
     * Returns browser corresponding to user's choice in IDE options.
     */
    public WebBrowser getPreferred() {
        for (WebBrowserFactoryDescriptor desc : getFactories()) {
            if (!desc.isDefault()) {
                continue;
            }
            return new WebBrowser(desc);
        }
        assert false : "no default browser instance found: " + getFactories();
        return null;
    }

    /**
     * Returns all browsers registered in the IDE.
     */
    public List<WebBrowser> getAll() {
        List<WebBrowser> browsers = new ArrayList<WebBrowser>();
        for (WebBrowserFactoryDescriptor desc : getFactories()) {
            browsers.add(new WebBrowser(desc));
        }
        return browsers;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        sup.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        sup.removePropertyChangeListener(l);
    }
    
    private void fireBrowsersChange() {
        sup.firePropertyChange(PROP_BROWSERS, null, null);
    }
    
    private void fireDefaultBrowserChange() {
        sup.firePropertyChange(PROP_DEFAULT_BROWSER, null, null);
    }

    
    private FileObject getConfigFolder() {
        return FileUtil.getConfigFile(BROWSERS_FOLDER);
    }
    
    private List<WebBrowserFactoryDescriptor> getFactories() {
        List<WebBrowserFactoryDescriptor> browsers = new ArrayList<WebBrowserFactoryDescriptor>();
        FileObject servicesBrowsers = getConfigFolder();
        if (servicesBrowsers == null) {
            return browsers;
        }

        DataFolder folder = DataFolder.findFolder(servicesBrowsers);
        // force object creation:
        Lookup.getDefault ().lookupAll(HtmlBrowser.Factory.class).toArray();
        for (DataObject browserSetting : folder.getChildren()) {
            InstanceCookie cookie = browserSetting.getCookie(InstanceCookie.class);
            HtmlBrowser.Factory fact;
            try {
                fact = (HtmlBrowser.Factory) cookie.instanceCreate();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                continue;
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
                continue;
            }
            
            Lookup.Item<HtmlBrowser.Factory> item =
                    Lookup.getDefault ().lookupItem (
                            new Lookup.Template<HtmlBrowser.Factory> (
                                    HtmlBrowser.Factory.class, null, fact));
            if (item == null) {
                continue;
            }
            browsers.add(
                new WebBrowserFactoryDescriptor(
                    item.getId(), 
                    browserSetting, 
                    IDESettings.getWWWBrowser().equals(fact),
                    fact));
        }
        return browsers;
    }
    
}
