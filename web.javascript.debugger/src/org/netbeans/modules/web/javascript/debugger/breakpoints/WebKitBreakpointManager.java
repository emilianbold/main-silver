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
package org.netbeans.modules.web.javascript.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.javascript.debugger.breakpoints.DOMNode.PathNotFoundException;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.modules.web.webkit.debugging.api.dom.Node;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.text.Line;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin
 */
abstract class WebKitBreakpointManager implements PropertyChangeListener {
    
    private static final Logger LOG = Logger.getLogger(WebKitBreakpointManager.class.getName());
    protected final Debugger d;
    private final AbstractBreakpoint ab;
    private static final RequestProcessor rp = new RequestProcessor(WebKitBreakpointManager.class);

    protected WebKitBreakpointManager(Debugger d, AbstractBreakpoint ab) {
        this.d = d;
        this.ab = ab;
        ab.addPropertyChangeListener(this);
    }
    
    public static WebKitBreakpointManager create(Debugger d, LineBreakpoint lb) {
        return new WebKitLineBreakpointManager(d, lb);
    }
    
    public static WebKitBreakpointManager create(WebKitDebugging wd, Project project, DOMBreakpoint db) {
        return new WebKitDOMBreakpointManager(wd, project, db);
    }

    public static WebKitBreakpointManager create(Debugger d, EventsBreakpoint eb) {
        return new WebKitEventsBreakpointManager(d, eb);
    }
    
    public static WebKitBreakpointManager create(Debugger d, XHRBreakpoint xb) {
        return new WebKitXHRBreakpointManager(d, xb);
    }
    
    public abstract void add();

    public abstract void remove();

    public void destroy() {
        remove();
        ab.removePropertyChangeListener(this);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (!Breakpoint.PROP_ENABLED.equals(event.getPropertyName())) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            rp.post(new Runnable() {
                @Override
                public void run() {
                    propertyChange(event);
                }
            });
            return ;
        }
        Breakpoint b = (Breakpoint) event.getSource();
        if (b.isEnabled()) {
            add();
        } else {
            remove();
        }
    }
    
    private static final class WebKitLineBreakpointManager extends WebKitBreakpointManager implements Debugger.Listener {
        
        private final LineBreakpoint lb;
        private org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint b;
        private final AtomicBoolean lineChanged = new AtomicBoolean(false);
        
        public WebKitLineBreakpointManager(Debugger d, LineBreakpoint lb) {
            super(d, lb);
            this.lb = lb;
        }

        @Override
        public void add() {
            if (b != null) {
                return ;
            }
            String url = lb.getURLString();
            url = reformatFileURL(url);
            b = d.addLineBreakpoint(url, lb.getLine().getLineNumber(), 0);
            d.addListener(this);
        }

        @Override
        public void remove() {
            if (b == null) {
                return ;
            }
            d.removeListener(this);
            if (d.isEnabled()) {
                d.removeLineBreakpoint(b);
            }
            b = null;
        }
        
        private void resubmit() {
            if (b != null) {
                d.removeLineBreakpoint(b);
                String url = lb.getURLString();
                url = reformatFileURL(url);
                b = d.addLineBreakpoint(url, lb.getLine().getLineNumber(), 0);
            }
        }
        
        // changes "file:/some" to "file:///some"
        private static String reformatFileURL(String tabToDebug) {
            if (!tabToDebug.startsWith("file:")) {
                return tabToDebug;
            }
            tabToDebug = tabToDebug.substring(5);
            while (tabToDebug.length() > 0 && tabToDebug.startsWith("/")) {
                tabToDebug = tabToDebug.substring(1);
            }
            return "file:///"+tabToDebug;
        }

        @Override
        public void paused(List<CallFrame> callStack, String reason) {}

        @Override
        public void resumed() {}

        @Override
        public void reset() {
            if (lineChanged.getAndSet(false)) {
                resubmit();
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            String propertyName = event.getPropertyName();
            if (LineBreakpoint.PROP_LINE.equals(propertyName)) {
                resubmit();
            } else if (LineBreakpoint.PROP_LINE_NUMBER.equals(propertyName)) {
                lineChanged.set(true);
            } else {
                super.propertyChange(event);
            }
        }
        
    }

    private static final class WebKitDOMBreakpointManager extends WebKitBreakpointManager {
        
        private final WebKitDebugging wd;
        private final Project project;
        private final DOMBreakpoint db;
        private Node node;
        private Set<org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint> bps;
        
        public WebKitDOMBreakpointManager(WebKitDebugging wd, Project project, DOMBreakpoint db) {
            super(wd.getDebugger(), db);
            this.wd = wd;
            this.project = project;
            this.db = db;
        }

        @Override
        public void add() {
            if (bps != null) {
                return ;
            }
            if (project != null) {
                URL urlBP = db.getURL();
                FileObject fo = URLMapper.findFileObject(urlBP);
                if (fo != null) {
                    FileObject projectDirectory = project.getProjectDirectory();
                    if (!FileUtil.isParentOf(projectDirectory, fo)) {
                        // Belongs somewhere else
                        return;
                    }
                }
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("WebKitDOMBreakpointManager.add(): breakpoint URL = '"+db.getURL()+"'");
            }
            DOMNode dn = db.getNode();
            dn.addPropertyChangeListener(this);
            try {
                dn.bindTo(wd.getDOM());
            } catch (PathNotFoundException pex) {
                db.setValidity(pex);
                return ;
            }
            Node n = dn.getNode();
            if (n != null) {
                addTo(n);
            }
        }
        
        private void addTo(Node node) {
            this.node = node;
            Set<DOMBreakpoint.Type> types = db.getTypes();
            if (types.isEmpty()) {
                return ;
            }
            bps = new HashSet<org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint>(types.size());
            for (DOMBreakpoint.Type type : types) {
                org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint b = 
                        d.addDOMBreakpoint(node, type.getTypeString());
                if (b != null) {
                    bps.add(b);
                }
            }
        }

        @Override
        public void remove() {
            DOMNode dn = db.getNode();
            dn.unbind();
            dn.removePropertyChangeListener(this);
            removeBreakpoints();
        }
        
        private void removeBreakpoints() {
            this.node = null;
            if (bps == null) {
                return ;
            }
            if (d.isEnabled()) {
                for (org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint b : bps) {
                    d.removeLineBreakpoint(b);
                }
            }
            bps = null;
        }

        @Override
        public void propertyChange(final PropertyChangeEvent event) {
            if (SwingUtilities.isEventDispatchThread()) {
                rp.post(new Runnable() {
                    @Override
                    public void run() {
                        propertyChange(event);
                    }
                });
                return ;
            }
            String propertyName = event.getPropertyName();
            if (DOMNode.PROP_NODE_CHANGED.equals(propertyName)) {
                Node oldNode = (Node) event.getOldValue();
                if (oldNode != null) {
                    removeBreakpoints();
                }
                Node newNode = (Node) event.getNewValue();
                if (newNode != null) {
                    addTo(newNode);
                }
            } else if (DOMNode.PROP_NODE_PATH_FAILED.equals(propertyName)) {
                removeBreakpoints();
                db.setValidity((DOMNode.PathNotFoundException) event.getNewValue());
            } else if (DOMBreakpoint.PROP_TYPES.equals(propertyName)) {
                Node theNode = node;
                if (theNode != null) {
                    removeBreakpoints();
                    addTo(theNode);
                }
            } else {
                super.propertyChange(event);
            }
        }
        
    }
    
    private static final class WebKitEventsBreakpointManager extends WebKitBreakpointManager {
        
        private EventsBreakpoint eb;
        private Map<String, org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint> bps;

        public WebKitEventsBreakpointManager(Debugger d, EventsBreakpoint eb) {
            super(d, eb);
            this.eb = eb;
        }

        @Override
        public void add() {
            Set<String> events = eb.getEvents();
            bps = new HashMap<String, org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint>(events.size());
            for (String event : events) {
                org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint b = 
                        d.addEventBreakpoint(event);
                if (b != null) {
                    bps.put(event, b);
                }
            }
            
        }

        @Override
        public void remove() {
            if (bps == null) {
                return ;
            }
            if (d.isEnabled()) {
                for (org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint b : bps.values()) {
                    d.removeLineBreakpoint(b);
                }
            }
            bps = null;
        }

        @Override
        public void propertyChange(final PropertyChangeEvent event) {
            if (SwingUtilities.isEventDispatchThread()) {
                rp.post(new Runnable() {
                    @Override
                    public void run() {
                        propertyChange(event);
                    }
                });
                return ;
            }
            String propertyName = event.getPropertyName();
            if (EventsBreakpoint.PROP_EVENTS.equals(propertyName) && bps != null) {
                Object newValue = event.getNewValue();
                Object oldValue = event.getOldValue();
                if (newValue != null) {
                    String newEvent = (String) newValue;
                    org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint b = 
                            d.addEventBreakpoint(newEvent);
                    if (b != null) {
                        bps.put(newEvent, b);
                    }
                } else if (oldValue != null) {
                    String oldEvent = (String) oldValue;
                    org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint b =
                            bps.remove(oldEvent);
                    if (b != null) {
                        d.removeEventBreakpoint(oldEvent);
                    }
                } else { // total refresh
                    remove();
                    add();
                }
            } else {
                super.propertyChange(event);
            }
        }
        
    }
    
    private static final class WebKitXHRBreakpointManager extends WebKitBreakpointManager {

        private final XHRBreakpoint xb;
        private org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint b;
        
        public WebKitXHRBreakpointManager(Debugger d, XHRBreakpoint xb) {
            super(d, xb);
            this.xb = xb;
        }

        @Override
        public void add() {
            if (b != null) {
                return ;
            }
            String urlSubstring = xb.getUrlSubstring();
            b = d.addXHRBreakpoint(urlSubstring);
        }

        @Override
        public void remove() {
            if (b == null) {
                return ;
            }
            if (d.isEnabled()) {
                d.removeLineBreakpoint(b);
            }
            b = null;
        }
        
    }

}
