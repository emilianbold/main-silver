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
package org.netbeans.modules.web.webkit.debugging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;
import org.netbeans.modules.web.webkit.debugging.spi.TransportImplementation;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

@NbBundle.Messages({"# {0} - url being debugged", "WebKitDebuggingProtocolPane=WebKit Protocol {0}"})
public class TransportHelper {
    
    private TransportImplementation impl;
    private Callback callback;
    private Map<Integer, Handle> map = new HashMap<Integer, Handle>();
    private List<ResponseCallback> listeners = new CopyOnWriteArrayList<ResponseCallback>();

    public static final String OBJECT_GROUP_NAME = "netbeans-debugger-objects";
    
    public TransportHelper(TransportImplementation impl) {
        this.impl = impl;
        this.callback = new Callback();
        impl.registerResponseCallback(callback);
    }
    
    public String getConnectionName() {
        return impl.getConnectionName();
    }
    
    public void sendCommand(Command command) {
        log("send "+command.toString()); // NOI18N
        impl.sendCommand(command);
    }
    
    public Response sendBlockingCommand(Command command) {
        log("blocking send "+command.toString()); // NOI18N
        Handle handle = createSynchronizationHandle(command);
        impl.sendCommand(command);
        boolean res = handle.waitForResponse();
        if (res) {
            return handle.getResponse();
        } else {
            return null;
        }
    }

    public void sendCallbackCommand(Command command, 
            ResponseCallback callback) {
        log("callback send "+command.toString()); // NOI18N
        createCallbackHandle(command, callback);
        impl.sendCommand(command);
    }
    
    public void addListener(ResponseCallback l) {
        listeners.add(l);
    }

    public void removeListener(ResponseCallback l) {
        listeners.remove(l);
    }
    
    private void notifyListeners(Response response) {
        for (ResponseCallback l : listeners ) {
            l.handleResponse(response);
        }
    }
    private synchronized Handle createSynchronizationHandle(Command command) {
        Handle handle = new Handle();
        map.put(command.getID(), handle);
        return handle;
    }

    private synchronized void createCallbackHandle(Command command, 
            ResponseCallback callback) {
        map.put(command.getID(), new Handle(callback));
    }

    private synchronized Handle removeHandle(int id) {
        return map.remove(id);
    }
    
    private static class Handle {
        private Response response;
        private Semaphore semaphore;
        private ResponseCallback callback;

        public Handle() {
            this.semaphore = new Semaphore(0);
        }

        public Handle(ResponseCallback callback) {
            this.callback = callback;
        }

        public void setResponse(Response response, TransportHelper transport) {
            this.response = response;
            if (semaphore != null) {
                semaphore.release();
            }
            if (callback != null) {
                transport.log("response "+response.getResponse().toJSONString()); // NOI18N
                callback.handleResponse(response);
            }
        }

        public Response getResponse() {
            return response;
        }
        
        public boolean waitForResponse() {
            assert semaphore != null;
            try {
                return semaphore.tryAcquire(5, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Logger.getLogger(TransportHelper.class.getName()).log(Level.INFO, null, ex);
                return false;
            }
        }
        
    }

    private InputOutput getOutputLogger() {
       return IOProvider.getDefault().getIO(Bundle.WebKitDebuggingProtocolPane(getConnectionName()), false); 
    }
    
    private void log(String s) {
       getOutputLogger().getOut().println(s); 
    }
    
    private void logError(String s) {
       getOutputLogger().getErr().println(s); 
    }
    
    
    private class Callback implements ResponseCallback {

        public Callback() {
        }
        
        @Override
        public void handleResponse(Response response) {
            int id = response.getID();
            if (id != -1) {
                // handle result of a command we issued earlier privately and
                // do not propagate that event further
                Handle handle = removeHandle(id);
                if (handle == null) {
                    log("ignoring response "+response.toString()); // NOI18N
                    return;
                }
                log("response "+response.toString()); // NOI18N
                handle.setResponse(response, TransportHelper.this);
            } else {
                // this is a unrequested notification from webkit - pass it
                // to API layer to handle it:
                log("event "+response.toString()); // NOI18N
                notifyListeners(response);
            }
        }
    
    }
}
