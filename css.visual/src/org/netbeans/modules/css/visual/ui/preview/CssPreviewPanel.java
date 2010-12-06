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
package org.netbeans.modules.css.visual.ui.preview;

import java.awt.Graphics;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.FutureTask;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.swing.NaiveUserAgent;

/**
 * JPanel wrapping XHTMLPanel, the Flying Saucer's rendering area.
 * The class also suppresses exceptions falling from the renderer
 * so they are just logged, not displayed to user as execeptions.
 *
 * @author  Marek Fukala
 */
public class CssPreviewPanel extends javax.swing.JPanel implements CssPreviewComponent {

    private static final Logger LOGGER = Logger.getLogger(CssPreviewPanel.class.getName());
    private static final boolean LOG_FINE = LOGGER.isLoggable(Level.FINE);
    private Handler FS_HANDLER = new FlyingSaucerLoggersHandler();
    private XHTMLPanel xhtmlPanel;
    private Runnable panelCreatedTask;

    /** Creates new form CssPreviewPanel2 */
    public CssPreviewPanel() {
        initComponents();

        //run the xhtml panel creation in a non-AWT thread
        RequestProcessor.getDefault().execute(new FutureTask<XHTMLPanel>(new Runnable() {

            @Override
            public void run() {
                //create outside of AWT
                final XHTMLPanel panel = new PatchedXHTMLPanel(new PreviewUserAgent());
                try {
                    //call the setDocument(...) on the freshly created panel so all the resources
                    //loading is done now and not later in EDT
                    panel.setDocument(new ByteArrayInputStream(CssPreviewGenerator.getEmptyDocumentContent().getBytes()), null);
                } catch (Exception ex) {
                    //no-op
                }
                //and set the panel to this component in AWT
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        setXhtmlPanel(panel);
                    }
                });
            }
        }, null));

        configureFlyingSaucerLoggers();
    }

    private synchronized void setXhtmlPanel(XHTMLPanel panel) {
        assert SwingUtilities.isEventDispatchThread();
        this.xhtmlPanel = panel;
        jScrollPane1.setViewportView(panel);
        //set the content if some was already requested
        if (panelCreatedTask != null) {
            panelCreatedTask.run();
            //release the only once used runnable
            panelCreatedTask = null;
        }
    }

    @Override
    public synchronized void setDocument(final InputStream is, final String url) throws Exception {
        if (xhtmlPanel == null) {
            //early attempt to set a document content, the xhtml panel initialization
            //is still running
            panelCreatedTask = new Runnable() {

                @Override
                public void run() {
                    //call it in non-AWT thread, it may take some time
                    RequestProcessor.getDefault().post(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                xhtmlPanel.setDocument(is, url);
                                //repaing the panel in AWT then
                                SwingUtilities.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        xhtmlPanel.revalidate();
                                        xhtmlPanel.repaint();
                                    }
                                    
                                });
                            } catch (Exception ex) {
                                LOGGER.log(Level.INFO, "Error setting the document to be previewed!", ex); //NOI18N
                            }
                        }
                    });
                }

            };
        } else {
            //and set the document to he renderer
            xhtmlPanel.setDocument(is, url);
        }
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void dispose() {
        // nothing to dispose here
    }

    private void configureFlyingSaucerLoggers() {
        //remove potential flying saucer handlers
        Logger logger = Logger.getLogger("plumbing.exception");
        for (Handler h : logger.getHandlers()) {
            logger.removeHandler(h);
        }
        //do not report event to the parent handler ...
        logger.setUseParentHandlers(false);
        //...just to me
        logger.addHandler(FS_HANDLER);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout());
        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    //delegating flying saucer handler
    private class FlyingSaucerLoggersHandler extends Handler {

        @Override
        public void publish(LogRecord record) {
            Level level = record.getLevel();
            if (LOG_FINE) {
                //set log level to FILE to prevent the exceptions
                //popping up in a netbeans exceptions dialog
                record.setLevel(Level.FINE);
                LOGGER.log(record);
            } else {
                //just swallow the log record if FINE logging disabled
            }

            //log the important messages with INFO level and show them in the status bar
            if(level.intValue() >= Level.WARNING.intValue()) {
                //log the exception message to output
                LOGGER.log(Level.INFO, record.getMessage());
                //...and to the status bar
                StatusDisplayer.getDefault().setStatusText(record.getMessage());
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }

    //workaround for FlyingSaucer bug (reported as netbeans issue #117499 (NullPointerException for unreachable url))
    private static class PatchedXHTMLPanel extends XHTMLPanel {

        /**
         * Instantiates a panel with a custom {@link org.xhtmlrenderer.extend.UserAgentCallback}
         * implementation.
         *
         * @param uac The custom UserAgentCallback implementation.
         */
        public PatchedXHTMLPanel(UserAgentCallback uac) {
            super(uac);
        }

        @Override
        public void paintComponent(Graphics g) {
            try {
                super.paintComponent(g);
            } catch (Throwable e) {
                if (LOG_FINE) {
                    LOGGER.log(Level.FINE, "It seems there is a bug in FlyinSaucer XHTML renderer.", e);
                }
                CssPreviewTopComponent.getDefault().setError();
            }
        }
    }

    //workaround for specifying Base URL
    private static class PreviewUserAgent extends NaiveUserAgent {

        @Override
        public String resolveURI(String uri) {
            if (uri == null) {
                return null;
            }

            try {
                //the FS tries to resolve the absolute link by simply adding it to the
                //current relative base so the uri given here looks like:
                //
                //file:/Users/marekfukala/NetBeansProjects/CssTesting/web/resources//duke2.png
                //
                //We can try to identify the doubleslash here and resolve the rest of the URI
                //by relativizing it to the web root (base of the absolute links).
                //This solution however seems to be little hacky to me and can stop
                //working if FS changes the (undefined) semantic.
                int doubleSlashIndex = uri.lastIndexOf("//"); //NOI18N
                if(doubleSlashIndex != -1) {
                    //get the link path w/o the base, KEEP one slash as the link prefix
                    //so we can simply use WebUtils.resolve()
                    String absoluteLink = uri.substring(doubleSlashIndex + 1); //+1 => just one slash goes away
                    // Absolute path now we will look to the webRoot of the project
                    URL url = new URL(getBaseURL());

                    //fix for Bug 188259 - java.net.URISyntaxException: Illegal character in path at index 20: file:/C:/Users/Bryan Field/Projects/SurfNSecure.com-Applied-Thermal-DocumentNotes/web/,.css (edit)
                    //the FileBasedURLMapper.getFileObjects() logs URISyntaxException instead of passing it out,
                    //if happens during its call to url.getURI();
                    url.toURI(); //lets catch the exception here

                    FileObject edited = URLMapper.findFileObject(url);
                    if (edited != null) {
                        FileObject target = WebUtils.resolve(edited, absoluteLink);
                        if (target != null) {
                            URL targetURL = URLMapper.findURL(target, URLMapper.INTERNAL);
                            if (targetURL != null) {
                                return targetURL.toExternalForm();
                            }
                        }
                    }
                }

            } catch (URISyntaxException ex) {
                LOGGER.log(Level.INFO, null, ex); //NOI18N
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.INFO, null, ex); //NOI18N
            }
            // falling back to FlyingSaucer implementation
            return super.resolveURI(uri);
        }
    }
}
