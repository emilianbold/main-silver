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

import org.netbeans.modules.web.browser.spi.EnhancedBrowser;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;
import org.netbeans.core.HtmlBrowserComponent;
import org.netbeans.modules.web.browser.ui.DeveloperHtmlBrowserComponent;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Lookup;

/**
 * Single opened browser tab. Methods on this class can be called from any thread
 * and opening browser URL or reloading a document will be re-posted into AWT
 * thread as necessary.
 */
public final class WebBrowserPane {

    private HtmlBrowser.Impl impl;
    private WebBrowserFactoryDescriptor descriptor;
    private final List<WebBrowserPaneListener> listeners = 
        new CopyOnWriteArrayList<WebBrowserPaneListener>();
    private PropertyChangeListener listener;
    private HtmlBrowserComponent topComponent;
    private boolean wrapEmbeddedBrowserInTopComponent;
    private boolean createTopComponent = false;
    
//    WebBrowserPane(HtmlBrowserComponent comp) {
//        this(comp.getBrowserImpl(), null, false, comp);
//    }
    
    WebBrowserPane(WebBrowserFactoryDescriptor desc, 
            boolean wrapEmbeddedBrowserInTopComponent, boolean disableNetBeansIntegration) 
    {
        this(desc, wrapEmbeddedBrowserInTopComponent, null, disableNetBeansIntegration);
    }
    
    private WebBrowserPane(WebBrowserFactoryDescriptor descriptor, 
            boolean wrapEmbeddedBrowserInTopComponent, HtmlBrowserComponent comp, 
            boolean disableNetBeansIntegration) 
    {
        this.descriptor = descriptor;
        this.wrapEmbeddedBrowserInTopComponent = wrapEmbeddedBrowserInTopComponent;
        listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (HtmlBrowser.Impl.PROP_BROWSER_WAS_CLOSED.equals(
                        evt.getPropertyName())) 
                {
                    firePaneClosed();
                }
                if (HtmlBrowser.Impl.PROP_URL.equals(evt.getPropertyName())) {
                    fireUrlChange();
                }
                if (HtmlBrowser.Impl.PROP_RUNNING.equals(evt.getPropertyName())) {
                    fireRunningStateChange();
                }
            }
        };
        /*
         * That's bad practice: HtmlBrowserComponent will create other impl 
         * to work with it internally. So there will be two different
         * not related impls with different Swing Components.
         * embedded = impl.getComponent() != null;
         */
        
        if (comp != null) {
            topComponent = comp;
        } 
        else {
            if (isEmbedded() && wrapEmbeddedBrowserInTopComponent) {
                // this needs to happen in AWT thread so let's do it later
                createTopComponent = true;
            }
            else {
                impl = descriptor.getFactory().createHtmlBrowserImpl();
                if ( impl instanceof EnhancedBrowser && !disableNetBeansIntegration){
                    ((EnhancedBrowser) impl).setEnhancedMode( true );
                }
                impl.addPropertyChangeListener(listener);
            }
        }
    }
    
    public void disablePageInspector() {
        assert impl instanceof EnhancedBrowser;
        if ( impl instanceof EnhancedBrowser ){
            ((EnhancedBrowser) impl).disablePageInspector();
        }
    }
    
    public void enableLiveHTML() {
        assert impl instanceof EnhancedBrowser;
        if ( impl instanceof EnhancedBrowser ){
            ((EnhancedBrowser) impl).enableLiveHTML();
        }
    }
    
    public void close(boolean closeTab) {
        if (impl != null) {
            assert impl instanceof EnhancedBrowser;
            if ( impl instanceof EnhancedBrowser ){
                ((EnhancedBrowser) impl).close(closeTab);
            }
        }
    }
    
    private synchronized HtmlBrowserComponent getTopComponent() {
        if (topComponent == null && createTopComponent) {

            // below constructor sets some TopComponent properties and needs
            // to be therefore called in AWT thread:
            topComponent = new DeveloperHtmlBrowserComponent(descriptor.getFactory());
        }
        return topComponent;
    }
    
    /**
     * Is this embedded or external browser.
     */
    public boolean isEmbedded() {
        return descriptor.getBrowserFamily() == BrowserFamilyId.JAVAFX_WEBVIEW;  // NOI18N
    }

    /**
     * Returns bare browser component. This method returns valid value only
     * when browser is embedded one and only if this pane was created via
     * WebBrowser.createNewBrowserPane(false). In all other cases it will 
     * always return null.
     */
    public Component getBrowserComponent() {
        if (isEmbedded() && !wrapEmbeddedBrowserInTopComponent) {
            return impl.getComponent();
        }
        return null;
    }
    
    /*
     * Access to the same browser pane as used by HtmlBrowser.UrlDisplayer class.
     */
//    public synchronized static WebBrowserPane getDefault() {
//        URLDisplayer u = URLDisplayer.getDefault();
//        if (!(u instanceof NbURLDisplayer)) {
//            return null;
//        }
//        NbURLDisplayer uu = (NbURLDisplayer)u;
//        HtmlBrowserComponent comp = uu.getInternalBrowserTopComponent();
//        if (comp == null) {
//            return null;
//        }
//        return new WebBrowserPane(comp);
//    }
    
    
    /**
     * This method shows given URL in *this* pane and should never result into
     * opening an additional browser window. Can be guaranteed only when NetBeans
     * plugins for external browsers are used or in case of embedded browser.
     */
    public void showURL(final URL u) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                HtmlBrowserComponent comp = getTopComponent();
                if (comp != null) {
                    comp.setURLAndOpen(u);
                    impl = topComponent.getBrowserImpl();
                    if ( impl!= null){
                        impl.addPropertyChangeListener(listener);
                    }
                } else {
                    impl.setURL(u);
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }
    
    void setProjectContext(Lookup projectContext) {
        if ( impl instanceof EnhancedBrowser ){
            ((EnhancedBrowser) impl).setProjectContext(projectContext);
        }
    }

    /**
     * Reload whatever is in this browser pane. Again can work reliable only
     * when NetBeans plugins for external browsers are used or in case of embedded browser.
     */
    public void reload() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (impl != null ) {
                    impl.reloadDocument();
                }
                else {
                    HtmlBrowserComponent comp = getTopComponent();
                    if (comp != null) {
                        impl = comp.getBrowserImpl();
                        if ( impl == null ){
                            comp.setURLAndOpen( comp.getDocumentURL());
                        }
                        else {
                            impl.reloadDocument();
                        }
                    }
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    /**
     * Lookup associated with this browser pane.
     */
    public Lookup getLookup() {
        if ( impl == null ){
            return Lookup.EMPTY;
        }
        return impl.getLookup();
    }

    public void addListener(WebBrowserPaneListener l) {
        listeners.add(l);
    }

    public void removeListener(WebBrowserPaneListener l) {
        listeners.remove(l);
    }

    /**
     * Is browser pane running or not? See Javadoc for HtmlBrowser.Impl.isRunning.
     */
    public Boolean isRunning() {
        if (impl != null) {
            return impl.isRunning();
        } else {
            return null;
        }
    }

    private void firePaneClosed() {
        for (WebBrowserPaneListener listener : listeners) {
            listener.browserEvent(new WebBrowserPaneWasClosedEvent(this));
        }
    }
    
    private void fireUrlChange() {
        for (WebBrowserPaneListener listener : listeners) {
            listener.browserEvent(new WebBrowserPaneURLChangedEvent(this));
        }
    }
    
    private void fireRunningStateChange() {
        for (WebBrowserPaneListener listener : listeners) {
            listener.browserEvent(new WebBrowserRunningStateChangedEvent(this));
        }
    }
    
    /**
     * Listener to browser pane events, eg. pane was closed.
     */
    public static interface WebBrowserPaneListener {

        void browserEvent(WebBrowserPaneEvent event);
    }

    /**
     * Marker interface for all browser events.
     */
    public static abstract class WebBrowserPaneEvent {

        private WebBrowserPane pane;

        private WebBrowserPaneEvent(WebBrowserPane pane) {
            this.pane = pane;
        }
        
        /**
         * Which pane was closed.
         */
        public WebBrowserPane getWebBrowserPane() {
            return pane;
        }
    }

    /**
     * Event notifying listeners that the pane was closed.
     */
    public static final class WebBrowserPaneWasClosedEvent extends WebBrowserPaneEvent {

        private WebBrowserPaneWasClosedEvent(WebBrowserPane pane) {
            super(pane);
        }

    }
    
    /**
     * Event notifying listeners that the pane URL has changed.
     */
    public static final class WebBrowserPaneURLChangedEvent extends WebBrowserPaneEvent {

        private WebBrowserPaneURLChangedEvent(WebBrowserPane pane) {
            super(pane);
        }

    }
    
    public static final class WebBrowserRunningStateChangedEvent extends WebBrowserPaneEvent {

        private WebBrowserRunningStateChangedEvent(WebBrowserPane pane) {
            super(pane);
        }

    }
}
