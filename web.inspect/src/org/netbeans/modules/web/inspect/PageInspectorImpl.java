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
package org.netbeans.modules.web.inspect;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.netbeans.modules.web.browser.spi.MessageDispatcher;
import org.netbeans.modules.web.browser.spi.MessageDispatcher.MessageListener;
import org.netbeans.modules.web.browser.spi.ScriptExecutor;
import org.netbeans.modules.web.inspect.script.Script;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the {@code PageInspector}.
 *
 * @author Jan Stola
 */
@ServiceProvider(service=PageInspector.class)
public class PageInspectorImpl extends PageInspector {
    /** Name of the property that is fired when the page model changes. */
    public static final String PROP_MODEL = "model"; // NOI18N
    /** Property change support. */
    private PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);
    /** Current inspected page. */
    private PageModel pageModel;
    /** Message listener for the inspected page. */
    private MessageListener messageListener;
    /** Message dispatcher for the inspected page. */
    private MessageDispatcher messageDispatcher;
    /** Lock guarding access to modifiable fields. */
    private final Object LOCK = new Object();

    /**
     * Creates a new {@code PageInspectorImpl}.
     */
    public PageInspectorImpl() {
        propChangeSupport.addPropertyChangeListener(PageInspectionTCGroupManager.getInstance());
    }

    /**
     * Helper method that just casts {@code PageInspector.getDefault()}
     * to {@code PageInspectorImpl}.
     * 
     * @return value of {@code PageInspector.getDefault()} casted to {@code PageInspectorImpl}.
     */
    public static PageInspectorImpl getDefault() {
        return (PageInspectorImpl)PageInspector.getDefault();
    }

    @Override
    public void inspectPage(Lookup pageContext) {
        synchronized (LOCK) {
            if (pageModel != null) {
                if (pageModel instanceof PageModelImpl) {
                    ((PageModelImpl)pageModel).dispose();
                }
                if (messageDispatcher != null) {
                    messageDispatcher.removeMessageListener(messageListener);
                }
            }
            ScriptExecutor executor = pageContext.lookup(ScriptExecutor.class);
            if (executor != null) {
                pageModel = new PageModelImpl(executor);
                messageDispatcher = pageContext.lookup(MessageDispatcher.class);
                if (messageDispatcher != null) {
                    messageListener = new InspectionMessageListener(pageModel);
                    messageDispatcher.addMessageListener(messageListener);
                }
                String initScript = Script.getScript("initialization"); // NOI18N
                executor.execute(initScript);
            } else {
                pageModel = null;
                messageDispatcher = null;
                messageListener = null;
            }
        }
        firePropertyChange(PROP_MODEL, null, null);
    }

    /**
     * Returns the current inspected page.
     * 
     * @return current inspected page.
     */
    public PageModel getPage() {
        synchronized (LOCK) {
            return pageModel;
        }
    }

    /**
     * Adds a property change listener.
     * 
     * @param listener listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener.
     * 
     * @param listener listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Fires the specified property change.
     * 
     * @param propName name of the property.
     * @param oldValue old value of the property or {@code null}.
     * @param newValue new value of the property or {@code null}.
     */
    private void firePropertyChange(String propName, Object oldValue, Object newValue) {
        propChangeSupport.firePropertyChange(propName, oldValue, newValue);
    }

    /**
     * Message listener of the inspected page.
     */
    private class InspectionMessageListener implements MessageListener {
        /** Name of the message type attribute. */
        private static final String MESSAGE_TYPE = "message"; // NOI18N
        /** Value of the message type attribute for the selection message. */
        private static final String MESSAGE_SELECTION = "selection"; // NOI18N
        /** Request processor for this class. */
        private RequestProcessor RP = new RequestProcessor(InspectionMessageListener.class.getName(), 5);
        /** Page model this message listener is related to. */
        private PageModel pageModel;

        /**
         * Creates a new {@code InspectionMessageListener}.
         * 
         * @param pageModel page model the listener is related to.
         */
        InspectionMessageListener(PageModel pageModel) {
            this.pageModel = pageModel;
        }
        
        @Override
        public void messageReceived(String featureId, final String messageTxt) {
            if (!PageInspector.MESSAGE_DISPATCHER_FEATURE_ID.equals(featureId)) {
                return;
            }
            // When the message comes from the external browser then
            // this method is called in the thread that is processing all
            // messages from the WebSocket server. We have to avoid blocking
            // of this thread => we process the message in another thread.
            RP.post(new Runnable() {
                @Override
                public void run() {
                    processMessage(messageTxt);
                }
            });
        }

        /**
         * Processes incoming message for the inspected page.
         * 
         * @param messageTxt message to process.
         */
        private void processMessage(final String messageTxt) {
            if (messageTxt == null) {
                synchronized (LOCK) {
                    if (pageModel == PageInspectorImpl.this.pageModel) {
                        inspectPage(Lookup.EMPTY);
                    }
                }
            } else {
                try {
                    JSONObject message = new JSONObject(messageTxt);
                    Object type = message.opt(MESSAGE_TYPE);
                    if (MESSAGE_SELECTION.equals(type)) {
                        try {
                            JSONObject jsonHandle = message.getJSONObject(MESSAGE_SELECTION);
                            ElementHandle handle = ElementHandle.forJSONObject(jsonHandle);
                            pageModel.setSelectedElements(Collections.singleton(handle));
                        } catch (JSONException ex) {
                            Logger.getLogger(PageInspectorImpl.class.getName())
                                    .log(Level.INFO, "Ignoring message with malformed selection attribute: {0}", messageTxt); // NOI18N
                        }
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(PageInspectorImpl.class.getName())
                            .log(Level.INFO, "Ignoring message that is not in JSON format: {0}", messageTxt); // NOI18N
                }
            }
        }

    }

}
