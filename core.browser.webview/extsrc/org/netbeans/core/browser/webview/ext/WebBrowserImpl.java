/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.core.browser.webview.ext;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.*;
import javafx.scene.web.WebHistory.Entry;
import javafx.util.Callback;
import javax.swing.*;
import org.netbeans.core.HtmlBrowserComponent;
import org.netbeans.core.browser.api.WebBrowser;
import org.netbeans.core.browser.api.WebBrowserEvent;
import org.netbeans.core.browser.api.WebBrowserListener;
import org.netbeans.core.browser.webview.BrowserCallback;
import org.netbeans.core.browser.webview.HtmlBrowserImpl;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.netbeans.modules.web.browser.spi.EnhancedBrowser;
import org.netbeans.modules.web.webkit.debugging.spi.Factory;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.HtmlBrowser.Impl;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Browser component implementation.
 *
 * @author S. Aubrecht, Jan Stola
 */
public class WebBrowserImpl extends WebBrowser implements BrowserCallback, EnhancedBrowser {
    
    private JFXPanel container;
    private String urlToLoad;
    private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
    private final List<WebBrowserListener> browserListners = new ArrayList<WebBrowserListener>();
    private final Object LOCK = new Object();
    private WebView browser;
    private String status;
    private boolean initialized;
    private Color defaultPanelColor = Color.LIGHT_GRAY;
    /** Lookup of this web-browser tab. */
    private Lookup lookup;
    private String currentLocation = null;
    private String currentTitle = null;
    private boolean isBackward = false;
    private boolean isForward = false;
    //toolbar for extra buttons (e.g. for developer tools)
    private final JToolBar toolbar = new JToolBar();

    private final Semaphore INIT_LOCK = new Semaphore( -1 );
    private boolean enhancedMode;
    private Lookup projectContext;

    /**
     * Creates a new {@code WebBrowserImpl}.
     */
    public WebBrowserImpl() {
        this( null );
    }

    private WebBrowserImpl( WebView webView ) {
        this.browser = webView;
        INIT_LOCK.release();
    }

    WebEngine getEngine() {
        WebEngine res = null;
        try {
            INIT_LOCK.acquire();
            assert browser != null;
            res = browser.getEngine();
        } catch( InterruptedException iE ) {
            Exceptions.printStackTrace( iE );
        } finally {
            INIT_LOCK.release();
        }
        return res;
    }
    
    
    @Override
    public Component getComponent() {
        synchronized( LOCK ) {
            if( null == container ) {
                container = new JFXPanel();
                defaultPanelColor = new JPanel().getBackground();
                javafx.application.Platform.runLater( new Runnable() {
                    @Override
                    public void run() {
                        createBrowser();
                    }
                });
                //initComponentPopupMenu(container);
                initialized = true;
            }
        }
        return container;
    }

    /**
     * Returns the lookup of this web-browser tab.
     * 
     * @return lookup of this web-browser tab.
     */
    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            lookup = createLookup();
        }
        return lookup;
    }

    private Lookup createLookup() {
        WebKitDebuggingTransport transport = new WebKitDebuggingTransport(this);
        Lookup l = Lookups.fixed(
                new MessageDispatcherImpl(),
                new ScriptExecutorImpl(this),
                transport,
                Factory.createWebKitDebugging(transport),
                new ZoomAndResizeImpl(this),
                toolbar
        );
        return l;
    }
    
    @Override
    public void reloadDocument() {
        if( !isInitialized() )
            return;
        javafx.application.Platform.runLater( new Runnable() {
            @Override
            public void run() {
                getEngine().reload();
            }
        });
    }

    @Override
    public void stopLoading() {
        if( !isInitialized() )
            return;
        javafx.application.Platform.runLater( new Runnable() {
            @Override
            public void run() {
                getEngine().getLoadWorker().cancel();
            }
        });
    }

    @Override
    public void setURL(final String url) {
        if( !isInitialized() ) {
            urlToLoad = url;
            return;
        }
        _setURL( url );
    }
    
    private void _setURL( final String url ) {
        javafx.application.Platform.runLater( new Runnable() {
            @Override
            public void run() {
                String fullUrl = url;
                if (!(url.startsWith( "http://") || url.startsWith( "https://") || url.startsWith("file:/"))) { // NOI18N
                    fullUrl = "http://" + url; // NOI18N
                }
                getEngine().load( fullUrl );
            }
        });
    }

    @Override
    public String getURL() {
        String url = currentLocation;
        if (url == null) {
            url = urlToLoad;
        }
        return url;
    }

    @Override
    public String getStatusMessage() {
        if( !isInitialized() )
            return null;
        return status;
    }

    @Override
    public String getTitle() {
        return currentTitle;
    }

    @Override
    public boolean isForward() {
        return isForward;
    }

    @Override
    public void forward() {
        if (isInitialized()) {
            javafx.application.Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    getEngine().getHistory().go( 1 );
                }
            });
        }
    }

    @Override
    public boolean isBackward() {
        return isBackward;
    }

    @Override
    public void backward() {
        if (isInitialized()) {
            javafx.application.Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    getEngine().getHistory().go( -1 );
                }
            });
        }
    }

    @Override
    public boolean isHistory() {
        return false;
    }

    @Override
    public void showHistory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propSupport.removePropertyChangeListener(l);
    }

    @Override
    public void setContent(final String content) {
        if( !isInitialized() ) {
            return;
        }
        javafx.application.Platform.runLater( new Runnable() {
            @Override
            public void run() {
                getEngine().loadContent( content );
            }
        });
    }

    @Override
    public Document getDocument() {
        Document document = null;
        if (isInitialized()) {
            document = runInFXThread(new Callable<Document>() {
                @Override
                public Document call() throws Exception {
                    return getEngine().getDocument();
                }
            });
        }
        return document;
    }

    @Override
    public void dispose() {
        synchronized( LOCK ) {
            if( isInitialized() ) {
                container.removeAll();
                // There can be pending tasks in FX thread that
                // will dereference the browser field => clear the field
                // once all these tasks are done
                javafx.application.Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        browser = null;
                    }
                });
                initialized = false;
            }
            browserListners.clear();
            container = null;
            paneClosed();
        }
    }

    @Override
    public void addWebBrowserListener(WebBrowserListener l) {
        synchronized( browserListners ) {
            browserListners.add(l);
        }
    }

    @Override
    public void removeWebBrowserListener(WebBrowserListener l) {
        synchronized( browserListners ) {
            browserListners.remove(l);
        }
    }

    @Override
    public Map<String, String> getCookie(String domain, String name, String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteCookie(String domain, String name, String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addCookie(Map<String, String> cookie) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object executeJavaScript(final String script) {
        Object result = null;
        if (isInitialized()) {
            result = runInFXThread(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    return getEngine().executeScript(script);
                }
            });
        }
        return result;
    }

    private boolean isInitialized() {
        synchronized( LOCK ) {
            return initialized;
        }
    }

    @Override
    public boolean fireBrowserEvent(int type, String url) {
        WebBrowserEventImpl event = new WebBrowserEventImpl(type, this, url);
        urlToLoad = url;
        synchronized( browserListners ) {
            for( WebBrowserListener l : browserListners ) {
                l.onDispatchEvent(event);
            }
        }
        return event.isCancelled();
    }

    @Override
    public void fireBrowserEvent(int type, AWTEvent e, Node n) {
        WebBrowserEvent event = new WebBrowserEventImpl(type, this, e, n);
        synchronized( browserListners ) {
            for( WebBrowserListener l : browserListners ) {
                l.onDispatchEvent(event);
            }
        }
    }

    private void createBrowser() {
        Platform.setImplicitExit(false);
        if( null == browser ) {
            WebView view = new WebView();
            initBrowser( view );

            browser = view;
            INIT_LOCK.release();
        }

        BorderPane pane = new BorderPane();
        pane.setCenter( browser );
        Scene scene = new Scene( pane );
        scene.setFill( getSceneFillColor() );
        container.setScene( scene );

        if( null != urlToLoad ) {
            _setURL( urlToLoad );
            urlToLoad = null;
        }
    }

    private void initBrowser( WebView view ) {
        view.setMinSize(100, 100);
        final WebEngine eng = view.getEngine();
        eng.setOnStatusChanged( new EventHandler<WebEvent<String>> () {
            @Override
            public void handle( WebEvent<String> e ) {
                final String oldStatus = status;
                status = e.getData();
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        propSupport.firePropertyChange( WebBrowser.PROP_STATUS_MESSAGE, oldStatus, status );
                    }
                } );
            }
        });

        eng.locationProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                currentLocation = eng.getLocation();
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        propSupport.firePropertyChange( WebBrowser.PROP_URL, oldValue, newValue );
                    }
                } );
            }
        });
        eng.titleProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                currentTitle = eng.getTitle();
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        propSupport.firePropertyChange( WebBrowser.PROP_TITLE, oldValue, newValue );
                    }
                } );
            }
        });
        eng.setOnAlert(new EventHandler<WebEvent<String>>() {
            @Override
            public void handle(WebEvent<String> event) {
                processAlert(event.getData());
            }
        });
        eng.getHistory().getEntries().addListener( new ListChangeListener<WebHistory.Entry> () {
            @Override
            public void onChanged( Change<? extends Entry> change ) {
                _updateBackAndForward();
            }
        });
        eng.getHistory().currentIndexProperty().addListener( new ChangeListener<Number> () {

            @Override
            public void changed( ObservableValue<? extends Number> ov, Number t, Number t1 ) {
                _updateBackAndForward();
            }
        });
        eng.getLoadWorker().runningProperty().addListener( new ChangeListener<Boolean> () {

            @Override
            public void changed( ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1 ) {
                final boolean isLoading = eng.getLoadWorker().isRunning();
                SwingUtilities.invokeLater( new Runnable() {

                    @Override
                    public void run() {
                        propSupport.firePropertyChange( WebBrowser.PROP_LOADING, !isLoading, isLoading );
                    }
                });
            }
        });
        eng.setCreatePopupHandler( new Callback<PopupFeatures, WebEngine>() {

            @Override
            public WebEngine call( PopupFeatures p ) {
                if( p.hasMenu() && p.hasStatus() && p.hasToolbar() && p.isResizable() ) {
                    //TODO this should be probably configurable
                    return _createNewBrowserWindow();
                }
                return eng;
            }
        });
    }

    /** Alert message with this prefix are used for page inspection-related communication. */
    static final String PAGE_INSPECTION_PREFIX = "NetBeans-Page-Inspection"; // NOI18N
    /**
     * Processing of alert messages from this web-browser pane.
     * 
     * @param message alert message.
     */
    private void processAlert(String message) {
        if (message.startsWith(PAGE_INSPECTION_PREFIX)) {
            message = message.substring(PAGE_INSPECTION_PREFIX.length());
            MessageDispatcherImpl dispatcher = getLookup().lookup(MessageDispatcherImpl.class);
            if (dispatcher != null) {
                dispatcher.dispatchMessage(PageInspector.MESSAGE_DISPATCHER_FEATURE_ID, message);
            }
        } // else whatever suitable processing of regular alerts
    }

    private <T> T runInFXThread(final Callable<T> task) {
        T result = null;
        try {
            if (javafx.application.Platform.isFxApplicationThread()) {
                result = task.call();
            } else {
                final Object[] resultWrapper = new Object[1];
                final CountDownLatch latch = new CountDownLatch(1);
                javafx.application.Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            resultWrapper[0] = task.call();
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            latch.countDown();
                        }
                    }
                });
                try {
                    latch.await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                result = (T)resultWrapper[0];
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }

    /**
     * Initializes popup-menu of the web-browser component.
     * 
     * @param browserComponent component whose popup-menu should be initialized.
     */
    private void initComponentPopupMenu(JComponent browserComponent) {
        if (PageInspector.getDefault() != null) {
            // Web-page inspection support is available in the IDE
            // => add a menu item that triggers page inspection.
            String inspectPage = NbBundle.getMessage(WebBrowserImpl.class, "WebBrowserImpl.inspectPage"); // NOI18N
            JPopupMenu menu = new JPopupMenu();
            menu.add(new AbstractAction(inspectPage) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    PageInspector inspector = PageInspector.getDefault();
                    if (inspector == null) {
                        Logger logger = Logger.getLogger(WebBrowserImpl.class.getName());
                        logger.log(Level.INFO, "No PageInspector found: ignoring the request for page inspection!"); // NOI18N
                    } else {
                        inspector.inspectPage(new ProxyLookup(getLookup(), projectContext));
                    }
                }
            });
            browserComponent.setComponentPopupMenu(menu);
        }
    }

    /**
     * Invoked when the web-browser pane is closed.
     */
    private void paneClosed() {
        MessageDispatcherImpl dispatcher = getLookup().lookup(MessageDispatcherImpl.class);
        if (dispatcher != null) {
            dispatcher.dispatchMessage(PageInspector.MESSAGE_DISPATCHER_FEATURE_ID, null);
        }
    }

    private void _updateBackAndForward() {
        final boolean oldForward = isForward;
        final boolean oldBackward = isBackward;
        isForward = _isForward();
        isBackward = _isBackward();
        SwingUtilities.invokeLater( new Runnable() {

            @Override
            public void run() {
                propSupport.firePropertyChange( WebBrowser.PROP_BACKWARD, oldBackward, isBackward );
                propSupport.firePropertyChange( WebBrowser.PROP_FORWARD, oldForward, isForward );
            }
        });
    }

    private boolean _isForward() {
        if( null == browser )
            return false;
        WebHistory history = getEngine().getHistory();
        return history.getCurrentIndex() < history.getEntries().size()-1;
    }

    private boolean _isBackward() {
        if( null == browser )
            return false;
        WebHistory history = getEngine().getHistory();
        return history.getCurrentIndex() > 0;
    }

    private WebEngine _createNewBrowserWindow() {
        final WebView newView = new WebView();
        final WebEngine newEngine = newView.getEngine();
        Platform.runLater( new Runnable() {
            @Override
            public void run() {
                final WebBrowserImpl browserImpl = new WebBrowserImpl( newView );
                browserImpl.initBrowser( newView );
                SwingUtilities.invokeLater( new Runnable() {

                    @Override
                    public void run() {
                        openNewBrowserWindow( browserImpl );
                    }
                });
            }
        });
        return newEngine;
    }

    private void openNewBrowserWindow( final WebBrowserImpl browserImpl ) {
        HtmlBrowser.Factory factory = new HtmlBrowser.Factory() {

            @Override
            public Impl createHtmlBrowserImpl() {
                return new HtmlBrowserImpl( browserImpl );
            }
        };
        HtmlBrowserComponent browserComponent = new HtmlBrowserComponent( factory, true, true );
        browserComponent.open();
        browserComponent.requestActive();
        browserComponent.makeBusy( true );
    }

    void zoom( final double zoomFactor ) {
        Platform.runLater( new Runnable() {
            @Override
            public void run() {
                browser.impl_setScale( zoomFactor );
            }
        });
    }

    void resize( final double width, final double height ) {
        Platform.runLater( new Runnable() {
            @Override
            public void run() {
                browser.setMaxWidth( width );
                browser.setMaxHeight( height );
            }
        });
    }

    void autofit() {
        Platform.runLater( new Runnable() {
            @Override
            public void run() {
                browser.setMaxWidth( Integer.MAX_VALUE );
                browser.setMaxHeight( Integer.MAX_VALUE );
                browser.autosize();
            }
        });
    }

    private javafx.scene.paint.Color getSceneFillColor() {
        javafx.scene.paint.Color res = javafx.scene.paint.Color.LIGHTGRAY;
        Color c = defaultPanelColor;
        if( null != c ) {
            res = javafx.scene.paint.Color.rgb( c.getRed(), c.getGreen(), c.getBlue() );
        }
        return res;
    }
    
    @Override
    public void setProjectContext(Lookup projectContext) {
        this.projectContext = projectContext;
    }

    @Override
    public boolean hasEnhancedMode() {
        return enhancedMode;
    }

    @Override
    public void setEnhancedMode(boolean mode) {
        enhancedMode = mode;
    }

    @Override
    public void disablePageInspector() {
    }

    @Override
    public void enableLiveHTML() {
    }

    @Override
    public void close(boolean closeTab) {
    }
}
