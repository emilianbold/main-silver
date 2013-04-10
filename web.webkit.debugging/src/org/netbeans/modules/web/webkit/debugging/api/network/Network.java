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
package org.netbeans.modules.web.webkit.debugging.api.network;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.LiveHTML;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.console.ConsoleMessage;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;

/**
 *
 */
public class Network {
    private TransportHelper transport;
    private boolean enabled;
    private Callback callback;
    private WebKitDebugging webKit;
    private int numberOfClients = 0;
    private boolean inLiveHTMLMode = false;
    private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    private Map<String, Request> activeRequests = new HashMap<String, Request>();
    private Map<String, WebSocketRequest> activeWebSocketRequests = new HashMap<String, WebSocketRequest>();
    
    public Network(TransportHelper transport, WebKitDebugging webKit) {
        this.transport = transport;
        this.callback = new Callback();
        this.transport.addListener(callback);
        this.webKit = webKit;
    }

    public void enable() {
        numberOfClients++;
        if (!enabled) {
            enabled = true;
            transport.sendBlockingCommand(new Command("Network.enable"));
        }
        inLiveHTMLMode = webKit.getDebugger().isInLiveHTMLMode();
    }

    public void disable() {
        assert numberOfClients > 0;
        numberOfClients--;
        if (numberOfClients == 0) {
            transport.sendCommand(new Command("Network.disable"));
            enabled = false;
        }
    }

    private String getResponseBody(String requestId) {
        JSONObject params = new JSONObject();
        params.put("requestId", requestId); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("Network.getResponseBody", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                // XXX: check here base64Encoded property and decode it if necessary
                return String.valueOf(result.get("body")); // NOI18N
            }
        }
        return null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private void recordDataEvent(long timeStamp, String id, String request, String mime) {
        assert inLiveHTMLMode;
        // TODO: fetch request here as well
        String response = getResponseBody(id);
        LiveHTML.getDefault().storeDataEvent(transport.getConnectionURL(), timeStamp, response, request, mime);
    }

    private void requestReceived(JSONObject params) {
        Request req = new Request(this, params);
        activeRequests.put(req.getRequestId(), req);
        fireNetworkRequest(req);
    }

    private void responseReceived(JSONObject params) {
        String requestId = String.valueOf(params.get("requestId"));
        assert requestId != null;
        Request req = activeRequests.get(requestId);
        if (req == null) {
            // ignore this. I noticed that WebKit protocol sometimes sends
            // duplicate messages. for example POST on a Java REST service
            // results into OPTIONS message first followed by POST itself.
            // webkit protocol send OPTIONS and gets response and data and
            // a message that loading is finished (ie. Network.loadingFinished) yet (sometimes?)
            // another Network.responseReceived followed by Network.loadingFinished
            // can be received for the same requestId
            return;
        }
        req.setResponse(params);
    }

    private void requestFailed(JSONObject params) {
        String requestId = String.valueOf(params.get("requestId"));
        assert requestId != null;
        Request req = activeRequests.remove(requestId);
        if (req == null) {
            // see comment in responseReceived()
            return;
        }
        req.setFailed(params);
    }

    private void dataReceived(JSONObject params) {
        String requestId = String.valueOf(params.get("requestId"));
        assert requestId != null;
        Request req = activeRequests.get(requestId);
        if (req == null) {
            // see comment in responseReceived()
            return;
        }
        req.dataLoadingStarted();
    }

    private void responseFinished(JSONObject params) {
        String requestId = String.valueOf(params.get("requestId"));
        assert requestId != null;
        Request req = activeRequests.remove(requestId);
        if (req == null) {
            // see comment in responseReceived()
            return;
        }
        req.requestCompleted();
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void fireNetworkRequest(Request req) {
        for (Listener l : listeners) {
            l.networkRequest(req);
        }
    }

    private void webSocketCreated(JSONObject params) {
        WebSocketRequest req = new WebSocketRequest(params);
        activeWebSocketRequests.put(req.getRequestId(), req);
        fireWebSocketNetworkRequest(req);
    }

    private void webSocketHandshakeRequest(JSONObject params) {
        String requestId = String.valueOf(params.get("requestId"));
        assert requestId != null;
        WebSocketRequest req = activeWebSocketRequests.get(requestId);
        if (req == null) {
            return;
        }
        req.setHandshakeRequest(params);
    }

    private void webSocketHandshakeResponse(JSONObject params) {
        String requestId = String.valueOf(params.get("requestId"));
        assert requestId != null;
        WebSocketRequest req = activeWebSocketRequests.get(requestId);
        if (req == null) {
            return;
        }
        req.setHandshakeResponse(params);
    }

    private void webSocketFrameSent(JSONObject params) {
        String requestId = String.valueOf(params.get("requestId"));
        assert requestId != null;
        WebSocketRequest req = activeWebSocketRequests.get(requestId);
        if (req == null) {
            return;
        }
        req.addFrame(Direction.SEND, params);
    }

    private void webSocketFrameReceived(JSONObject params) {
        String requestId = String.valueOf(params.get("requestId"));
        assert requestId != null;
        WebSocketRequest req = activeWebSocketRequests.get(requestId);
        if (req == null) {
            return;
        }
        req.addFrame(Direction.RECEIVED, params);
    }

    private void webSocketFrameError(JSONObject params) {
        String requestId = String.valueOf(params.get("requestId"));
        assert requestId != null;
        WebSocketRequest req = activeWebSocketRequests.get(requestId);
        if (req == null) {
            return;
        }
        req.setFrameError(params);
    }

    private void webSocketClosed(JSONObject params) {
        String requestId = String.valueOf(params.get("requestId"));
        assert requestId != null;
        WebSocketRequest req = activeWebSocketRequests.remove(requestId);
        if (req == null) {
            return;
        }
        req.close();
    }

    private void fireWebSocketNetworkRequest(WebSocketRequest req) {
        for (Listener l : listeners) {
            l.webSocketRequest(req);
        }
    }

    /**
     * Listener for events about network events.
     */
    public static interface Listener {

        /**
         * New network request was created.
         */
        void networkRequest(Request request);

        /**
         * New WebSocket request was created.
         */
        void webSocketRequest(WebSocketRequest request);

    }

    public final static class Request {

        public static final String PROP_RESPONSE = "Network.Request.Response";
        public static final String PROP_RESPONSE_DATA = "Network.Request.Response.Data";

        private final JSONObject request;
        private final JSONObject initiator;
        private final String requestId;
        private String responseType;
        private JSONObject response;
        private PropertyChangeSupport support = new PropertyChangeSupport(this);
        private boolean hasData = false;
        private boolean dataReady = false;
        private final Network network;
        private boolean failed = false;
        private final String documentUrl;

        private Request(Network network, JSONObject params) {
            this.request = (JSONObject)params.get("request");
            this.initiator = (JSONObject)params.get("initiator");
            this.requestId = String.valueOf(params.get("requestId"));
            this.network = network;
            this.documentUrl = (String)params.get("documentURL");
        }

        public String getInitiatorType() {
            return (String)getInitiator().get("type");
        }

        public String getDocumentUrl() {
            return documentUrl;
        }

        public String getResponseType() {
            return responseType;
        }

        public JSONObject getInitiator() {
            return initiator;
        }

        public JSONObject getRequest() {
            return request;
        }

        private String getRequestId() {
            return requestId;
        }

        public JSONObject getResponse() {
            return response;
        }

        public int getResponseCode() {
            if (response != null) {
                Number statusCode = (Number)response.get("status");
                if (statusCode != null) {
                    return statusCode.intValue();
                }
            }
            return -1;
        }

        private void setResponse(JSONObject response) {
            this.response = (JSONObject)response.get("response");
            this.responseType = String.valueOf(response.get("type"));
            support.firePropertyChange(PROP_RESPONSE, null, null);
        }

        public String getResponseData() {
            if (!dataReady) {
                return null;
            }
            return network.getResponseBody(getRequestId());
        }

        private void dataLoadingStarted() {
            hasData = true;
        }

        private void requestCompleted() {
            if (hasData) {
                dataReady = true;
                support.firePropertyChange(PROP_RESPONSE_DATA, null, null);
            }
        }
        
        public boolean hasData() {
            return hasData;
        }

        public List<ConsoleMessage.StackFrame> getInitiatorCallStack() {
            JSONArray stack = (JSONArray)getInitiator().get("stackTrace");
            if (stack != null && stack.size() > 0) {
                List<ConsoleMessage.StackFrame> stackTrace = new ArrayList<ConsoleMessage.StackFrame>();
                for (Object o : stack) {
                    JSONObject json = (JSONObject)o;
                    stackTrace.add(new ConsoleMessage.StackFrame(json));
                }
                return stackTrace;
            }
            return null;
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            support.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            support.addPropertyChangeListener(l);
        }

        private void setFailed(JSONObject params) {
            // there is "errorText" in params but it is always empty so I ignore it for now
            failed = true;
            support.firePropertyChange(PROP_RESPONSE, null, null);
        }

        public boolean isFailed() {
            return failed;
        }

    }

    public final static class WebSocketRequest {

        public static final String PROP_HANDSHAKE_REQUEST = "Network.WebSocketRequest.Handshake.Request";
        public static final String PROP_HANDSHAKE_RESPONSE = "Network.WebSocketRequest.Handshake.Response";
        public static final String PROP_FRAMES = "Network.WebSocketRequest.Frame";
        public static final String PROP_CLOSED = "Network.WebSocketRequest.Closed";

        private final String requestId;
        private final String url;
        private JSONObject handshakeRequest;
        private JSONObject handshakeResponse;
        private PropertyChangeSupport support = new PropertyChangeSupport(this);
        private List<WebSocketFrame> frames = new ArrayList<WebSocketFrame>();
        private boolean closed = false;
        private String errorMessage = null;

        private WebSocketRequest(JSONObject params) {
            this.requestId = String.valueOf(params.get("requestId"));
            this.url = String.valueOf(params.get("url"));
        }

        private String getRequestId() {
            return requestId;
        }

        public String getURL() {
            return url;
        }

        public JSONObject getHandshakeRequest() {
            return handshakeRequest;
        }

        private void setHandshakeRequest(JSONObject params) {
            this.handshakeRequest = (JSONObject)params.get("request");
            support.firePropertyChange(PROP_HANDSHAKE_REQUEST, null, null);
        }
        public JSONObject getHandshakeResponse() {
            return handshakeResponse;
        }

        private void setHandshakeResponse(JSONObject params) {
            this.handshakeResponse = (JSONObject)params.get("response");
            support.firePropertyChange(PROP_HANDSHAKE_RESPONSE, null, null);
        }

        private void setFrameError(JSONObject params) {
            this.errorMessage = (String)params.get("errorMessage");
            support.firePropertyChange(PROP_FRAMES, null, null);
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        private void addFrame(Direction direction, JSONObject params) {
            JSONObject response = (JSONObject)params.get("response");
            Number timestamp = (Number)params.get("timestamp");
            Number opcode = (Number)response.get("opcode");
            WebSocketFrame frame = new WebSocketFrame(new Date(
                    timestamp.longValue()), direction, response, opcode.intValue());
            frames.add(frame);
            support.firePropertyChange(PROP_FRAMES, null, null);
        }

        public List<WebSocketFrame> getFrames() {
            return new ArrayList<WebSocketFrame>(frames);
        }

        public boolean isClosed() {
            return closed;
        }

        private void close() {
            this.closed = true;
            support.firePropertyChange(PROP_CLOSED, null, null);
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            support.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            support.addPropertyChangeListener(l);
        }
    }

    public enum Direction {
        SEND,
        RECEIVED,
    }
    
    public final static class WebSocketFrame {

        private Direction direction;
        private JSONObject data;
        private Date timestamp;
        private int opcode;

        private WebSocketFrame(Date timestamp, Direction direction, JSONObject data, int opcode) {
            this.timestamp = timestamp;
            this.direction = direction;
            this.data = data;
            this.opcode = opcode;
        }

        public String getPayload() {
            return String.valueOf(data.get("payloadData"));
        }

        public Direction getDirection() {
            return direction;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public int getOpcode() {
            return opcode;
        }

    }

    private class Callback implements ResponseCallback {

        @Override
        public void handleResponse(Response response) {
            if ("Network.requestWillBeSent".equals(response.getMethod())) {
                requestReceived(response.getParams());
            } else if ("Network.responseReceived".equals(response.getMethod())) {
                responseReceived(response.getParams());

                // LiveHTML support:
                if (inLiveHTMLMode && 
                        "XHR".equals(response.getParams().get("type"))) {
                    final long timeStamp = System.currentTimeMillis();
                    final String id = (String)response.getParams().get("requestId");
                    final String request = (String)((JSONObject)response.getParams().get("response")).get("url");
                    final String mime = (String)((JSONObject)response.getParams().get("response")).get("mimeType");
                    transport.getRequestProcessor().post(new Runnable() {
                        @Override
                        public void run() {
                            recordDataEvent(timeStamp, id, request, mime);
                        }
                    });
                }
            } else if ("Network.loadingFailed".equals(response.getMethod())) {
                requestFailed(response.getParams());
                

// TODO: XXX: handle requestServedFromMemoryCache here as well


            } else if ("Network.dataReceived".equals(response.getMethod())) {
                dataReceived(response.getParams());
            } else if ("Network.loadingFinished".equals(response.getMethod())) {
                responseFinished(response.getParams());
            } else if ("Network.webSocketCreated".equals(response.getMethod())) {
                webSocketCreated(response.getParams());
            } else if ("Network.webSocketWillSendHandshakeRequest".equals(response.getMethod())) {
                webSocketHandshakeRequest(response.getParams());
            } else if ("Network.webSocketHandshakeResponseReceived".equals(response.getMethod())) {
                webSocketHandshakeResponse(response.getParams());
            } else if ("Network.webSocketFrameSent".equals(response.getMethod())) {
                webSocketFrameSent(response.getParams());
            } else if ("Network.webSocketFrameReceived".equals(response.getMethod())) {
                webSocketFrameReceived(response.getParams());
            } else if ("Network.webSocketFrameError".equals(response.getMethod())) {
                webSocketFrameError(response.getParams());
            } else if ("Network.webSocketClosed".equals(response.getMethod())) {
                webSocketClosed(response.getParams());
            }
        }

    }
    
    
}
