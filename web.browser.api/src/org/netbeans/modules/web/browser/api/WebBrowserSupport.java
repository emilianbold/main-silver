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
package org.netbeans.modules.web.browser.api;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Support for web browsers.
 * @since 1.9
 */
public final class WebBrowserSupport {

    private static final Logger LOGGER = Logger.getLogger(WebBrowserSupport.class.getName());

    private WebBrowserSupport() {
    }

    /**
     * Create model for component with browsers, possibly with the
     * {@link BrowserComboBoxModel#getSelectedBrowserId() selected browser identifier}.
     * <p>
     * If the browser identifier is {@code null} (likely not set yet?), then the {@link #getDefaultBrowserId() default} browser is selected.
     * @param selectedBrowserId browser identifier, can be {@code null} if e.g. not set yet
     * @return model for component with browsers
     * @see #createBrowserRenderer()
     */
    public static BrowserComboBoxModel createBrowserModel(@NullAllowed String selectedBrowserId, boolean showIDEGlobalBrowserOption) {
        return createBrowserModel(selectedBrowserId, showIDEGlobalBrowserOption, false);
    }

    // XXX dkonecny: add javadoc!
    public static BrowserComboBoxModel createBrowserModel(@NullAllowed String selectedBrowserId,
            boolean showIDEGlobalBrowserOption, boolean includePhoneGap) {
        List<WebBrowser> browsers = WebBrowsers.getInstance().getAll(false, true, showIDEGlobalBrowserOption, includePhoneGap, true);
        if (selectedBrowserId == null) {
            selectedBrowserId = getDefaultBrowserId();
        }
        BrowserComboBoxModel model = new BrowserComboBoxModel(browsers);
        for (int i = 0; i < model.getSize(); i++) {
            WebBrowser browser = model.getElementAt(i);
            assert browser != null;
            if (browser.getId().equals(selectedBrowserId)) {
                model.setSelectedItem(browser);
                break;
            }
        }
        return model;
    }

    /**
     * Create renderer for component with browsers.
     * @return renderer for component with browsers
     * @see #createBrowserModel(String)
     */
    public static ListCellRenderer<WebBrowser> createBrowserRenderer() {
        return new BrowserRenderer();
    }

    /**
     * Returns an ID of default IDE's browser, that is not really a browser instance
     * but an artificial browser item representing whatever is IDE's default browser.
     * @since 1.11
     */
    public static String getDefaultBrowserId() {
        return WebBrowsers.DEFAULT;
    }

    /**
     * Check whether the given {@link BrowserComboBoxModel#getSelectedBrowserId() browser identifier} represents browser with NetBeans integration.
     * <p>
     * If the browser identifier is {@code null} (likely not set yet?), then the {@link #getDefaultBrowserId() default} browser is used.
     * @param browserId browser identifier, can be {@code null} if e.g. not set yet
     * @return {@code true} if the given browser identifier represents browser with NetBeans integration
     */
    public static boolean isIntegratedBrowser(@NullAllowed String browserId) {
        if (browserId != null
                && browserId.endsWith(WebBrowser.INTEGRATED)) {
            return true;
        }
        ComboBoxModel<WebBrowser> model = createBrowserModel(browserId, true);
        return ((WebBrowser) model.getSelectedItem()).hasNetBeansIntegration();
    }

    /**
     * Get browser for the given {@link BrowserComboBoxModel#getSelectedBrowserId() browser identifier}. Returns {@code null}
     * for the default IDE browser (set in IDE Options).
     * If the browser identifier is {@code null} (likely not set yet?), then the {@link #getDefaultBrowserId() default} browser is returned.
     * @param browserId browser identifier, can be {@code null} if e.g. not set yet
     * @return browser for the given browser identifier
     */
    @CheckForNull
    public static WebBrowser getBrowser(@NullAllowed String browserId) {
        if (browserId == null) {
            browserId = getDefaultBrowserId();
        }
        return findWebBrowserById(browserId);
    }

    private static WebBrowser findWebBrowserById(String id) {
        for (WebBrowser wb : WebBrowsers.getInstance().getAll(false, true, true, true, false)) {
            if (wb.getId().equals(id)) {
                return wb;
            }
        }
            return null;
        }

    //~ Inner classes

    /**
     * Model for component with browsers.
     */
    public static final class BrowserComboBoxModel extends AbstractListModel<WebBrowser> implements ComboBoxModel<WebBrowser> {

        private static final long serialVersionUID = -65798754321321L;

        private final List<WebBrowser> browsers = new CopyOnWriteArrayList<WebBrowser>();

        private volatile WebBrowser selectedBrowser = null;


        BrowserComboBoxModel(List<WebBrowser> browsers) {
            assert browsers != null;
            assert !browsers.isEmpty();
            this.browsers.addAll(browsers);
            selectedBrowser = browsers.get(0);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getSize() {
            return browsers.size();
        }

        /**
         * {@inheritDoc}
         */
        @CheckForNull
        @Override
        public WebBrowser getElementAt(int index) {
            try {
                return browsers.get(index);
            } catch (IndexOutOfBoundsException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setSelectedItem(Object browser) {
            selectedBrowser = (WebBrowser) browser;
            fireContentsChanged(this, -1, -1);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public WebBrowser getSelectedItem() {
            assert selectedBrowser != null;
            return selectedBrowser;
        }

        /**
         * Get selected browser or {@code null} if the IDE default browser selected.
         * @return selected browser or {@code null} if the IDE default browser selected
         */
        @CheckForNull
        public WebBrowser getSelectedBrowser() {
            assert selectedBrowser != null;
            return selectedBrowser;
        }

        /**
         * Get selected browser identifier.
         * @return selected browser identifier
         */
        public String getSelectedBrowserId() {
            assert selectedBrowser != null;
            return selectedBrowser.getId();
        }

    }

    /**
     * Renderer for component with browsers.
     */
    private static final class BrowserRenderer implements ListCellRenderer<WebBrowser> {

        // @GuardedBy("EDT")
        private final ListCellRenderer<Object> defaultRenderer = new DefaultListCellRenderer();

        @Override
        public Component getListCellRendererComponent(JList<? extends WebBrowser> list, WebBrowser value, int index, boolean isSelected, boolean cellHasFocus) {
            assert EventQueue.isDispatchThread();
            Component c = defaultRenderer.getListCellRendererComponent(list, value.getName(), index, isSelected, cellHasFocus);
            if (c instanceof JLabel) {
                JLabel l = (JLabel)c;
                l.setIcon(new ImageIcon(value.getIconImage()));
            }
            return c;
        }

    }

}
