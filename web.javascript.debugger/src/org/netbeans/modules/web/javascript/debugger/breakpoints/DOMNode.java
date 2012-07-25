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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.web.webkit.debugging.api.dom.DOM;
import org.netbeans.modules.web.webkit.debugging.api.dom.Node;

/**
 * Representation of a node in a DOM document.
 * It's storable as a String and it's able to adapt to changes in a DOM document.
 * 
 * @author Martin
 */
public final class DOMNode {
    
    public static final String PROP_NODE_CHANGED = "nodeChanged";               // NOI18N
    public static final String PROP_NODE_PATH_FAILED = "nodePathRequestFailed"; // NOI18N
    
    private static final Logger LOG = Logger.getLogger(DOMNode.class.getName());
    private static final char ETX = 0x0003;
    private final List<NodeId> path;
    
    private transient DOM dom;
    private transient Node node;
    private transient List<Node> nodePath;
    private transient DOMListener domListener;
    
    private final PropertyChangeSupport pchs = new PropertyChangeSupport(this);
    
    private DOMNode(List<NodeId> path) {
        this.path = path;
    }
    
    public static DOMNode create(Node node) {
        List<NodeId> path = new LinkedList<NodeId>();
        Node parent = node.getParent();
        Node origNode = node;
        while (parent != null) {
            List<Node> children = parent.getChildren();
            int childNumber = children.indexOf(node);
            String localName = node.getLocalName();
            path.add(0, new NodeId(localName, childNumber));
            node = parent;
            parent = node.getParent();
        }
        path.add(0, new NodeId(node.getLocalName(), -1));
        DOMNode dn = new DOMNode(path);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("new DOMNode("+dn.getNodePathNames()+") created from "+origNode);
        }
        return dn;
    }
    
    /**
     * @see #getStringDefinition()
     */
    public static DOMNode create(String stringDefinition) {
        if (stringDefinition.charAt(0) != '[') {
            throw new IllegalArgumentException("Missing opening bracket in '"+stringDefinition+"'");
        }
        if (stringDefinition.charAt(stringDefinition.length() - 1) != ']') {
            throw new IllegalArgumentException("Missing closing bracket in '"+stringDefinition+"'");
        }
        int n = stringDefinition.length() - 1;
        List<NodeId> path = new LinkedList<NodeId>();
        int i1 = 1;
        do {
            int i2 = stringDefinition.indexOf(ETX, i1);
            if (i2 < 0) {
                throw new IllegalArgumentException("Missing end text delimeter in '"+stringDefinition+"' after pos "+i1);
            }
            String name = stringDefinition.substring(i1, i2);
            i1 = i2 + 1;
            i2 = stringDefinition.indexOf(',', i1);
            if (i2 < 0) {
                throw new IllegalArgumentException("Missing comma delimeter in '"+stringDefinition+"' after pos "+i1);
            }
            int childNumber = Integer.parseInt(stringDefinition.substring(i1, i2));
            path.add(new NodeId(name, childNumber));
            i1 = i2 + 1;
        } while (i1 < n);
        DOMNode dn = new DOMNode(path);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("new DOMNode("+dn.getNodePathNames()+") created from "+stringDefinition);
        }
        return dn;
    }
    
    /**
     * Provide serializable string definition of this object.
     */
    public String getStringDefinition() {
        StringBuilder sb = new StringBuilder("[");
        synchronized (path) {
            for (NodeId n : path) {
                sb.append(n.name);
                sb.append(ETX);
                sb.append(n.childNumber);
                sb.append(',');
            }
        }
        sb.append(']');
        return sb.toString();
    }
    
    public String getNodeName() {
        return path.get(path.size() - 1).name;
    }
    
    public String getNodePathNames() {
        StringBuilder sb = new StringBuilder();
        for (NodeId ni : path) {
            sb.append(ni.name);
            sb.append("/");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
    
    /**
     * Bind to the DOM document and update the node as the document changes.
     * @param dom The DOM document
     * @throws PathNotFoundException When the DOM node can not be located.
     */
    public synchronized void bindTo(DOM dom) throws PathNotFoundException {
        if (this.dom != null) {
            throw new IllegalStateException("Still listening on "+this.dom);
        }
        this.dom = dom;
        this.domListener = new DOMListener();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("DOMNode("+getNodePathNames()+") is starting listening on "+dom);
        }
        dom.addListener(domListener);
        createNodePath();
    }
    
    private void createNodePath() throws PathNotFoundException {
        int n = path.size();
        nodePath = new ArrayList<Node>(n);
        this.node = null;
        Node node = dom.getDocument();
        if (node == null) {
            throw new PathNotFoundException(path.get(0).name, 0, getNodePathNames());
        }
        Node parent = null;
        for (int i = 0; i < n; i++) {
            NodeId ni = path.get(i);
            if (parent == null) {
                if (!ni.name.equals(node.getLocalName())) {
                    throw new PathNotFoundException(ni.name, i, getNodePathNames());
                }
                nodePath.add(node);
                parent = node;
            } else {
                List<Node> children = parent.getChildren();
                if (children == null) {
                    dom.requestChildNodes(parent.getNodeId());
                    // Have to wait until we're notified that the children were set.
                    throw new PathNotFoundException(ni.name, i, getNodePathNames(), true);
                }
                Node chn = null;
                int c = ni.childNumber;
                int nc = children.size();
                if (0 <= c && c < nc) {
                    chn = children.get(c);
                    if (!ni.name.equals(chn.getLocalName())) {
                        chn = null;
                    }
                }
                /* Uncomment this if we should check sibling elements
                if (chn == null) {
                    if (c >= nc) {
                        c = nc - 1;
                    }
                    if (c < 0) {
                        c = 0;
                    }
                    for (int ci = c; ci < nc; ci++) {
                        if (ni.name.equals(children.get(ci).getLocalName())) {
                            chn = children.get(ci);
                            break;
                        }
                    }
                    if (chn == null) {
                        for (int ci = 0; ci < c; ci++) {
                            if (ni.name.equals(children.get(ci).getLocalName())) {
                                chn = children.get(ci);
                                break;
                            }
                        }
                    }
                }
                */
                if (chn == null) {
                    throw new PathNotFoundException(ni.name, i, getNodePathNames());
                }
                node = chn;
                nodePath.add(node);
                parent = node;
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("createNodePath() succesfully set nodePath = "+nodePath+" and node = "+node);
        }
        this.node = node;
    }
    
    /**
     * Unbind from the DOM document.
     */
    public synchronized void unbind() {
        dom.removeListener(domListener);
        domListener = null;
        dom = null;
        node = null;
        nodePath = null;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("DOMNode("+getNodePathNames()+") has stopped listening on DOM changes.");
        }
    }
    
    /**
     * Get the node from the DOM document.
     * @return the node, if found in the document. <code>null</code> otherwise,
     * or when not bound to a DOM document.
     */
    public synchronized Node getNode() {
        return node;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pl) {
        pchs.addPropertyChangeListener(pl);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pl) {
        pchs.removePropertyChangeListener(pl);
    }
    
    private void fireNodeChanged(Node oldNode, Node newNode) {
        pchs.firePropertyChange(PROP_NODE_CHANGED, oldNode, newNode);
    }
    
    private void fireNodePathFailed(PathNotFoundException pex) {
        pchs.firePropertyChange(PROP_NODE_PATH_FAILED, null, pex);
    }
    
    
    private class DOMListener implements DOM.Listener {

        @Override
        public void documentUpdated() {
            LOG.fine("DOM documentUpdated()");
            PathNotFoundException pnfex = null;
            Node oldNode;
            Node newNode;
            synchronized(DOMNode.this) {
                oldNode = node;
                node = null;
                try {
                    createNodePath();
                } catch (PathNotFoundException pex) {
                    pnfex = pex;
                }
                newNode = node;
            }
            if (pnfex != null) {
                fireNodePathFailed(pnfex);
                return;
            }
            if (newNode != null) {
                fireNodeChanged(oldNode, newNode);
            }
        }

        @Override
        public void childNodesSet(Node parent) {
            LOG.fine("DOM childNodesSet("+parent+")");
            PathNotFoundException pnfex = null;
            Node oldNode;
            Node newNode;
            synchronized(DOMNode.this) {
                oldNode = newNode = node;
                if (node == null) {
                    // In process of searching for the node...
                    try {
                        createNodePath();
                    } catch (PathNotFoundException pex) {
                        pnfex = pex;
                    }
                    newNode = node;
                } else {
                    int ns = nodePath.size() - 1; // We can ignore the set of children to the last node
                    for (int i = 0; i < ns; i++) {
                        if (parent.equals(nodePath.get(i))) {
                            try {
                                createNodePath();
                            } catch (PathNotFoundException pex) {
                                pnfex = pex;
                            }
                            newNode = node;
                            break;
                        }
                    }
                }
            }
            if (pnfex != null) {
                fireNodePathFailed(pnfex);
            }
            if (oldNode != newNode) {
                fireNodeChanged(oldNode, newNode);
            }
        }

        @Override
        public void childNodeRemoved(Node parent, Node child) {
            LOG.fine("DOM childNodesRemoved("+parent+", "+child+")");
            PathNotFoundException pnfex = null;
            Node oldNode;
            Node newNode;
            synchronized(DOMNode.this) {
                oldNode = newNode = node;
                if (node != null) {
                    int ns = nodePath.size();
                    for (int i = 0; i < ns; i++) {
                        if (child.equals(nodePath.get(i))) {
                            try {
                                createNodePath();
                            } catch (PathNotFoundException pex) {
                                pnfex = pex;
                            }
                            newNode = node;
                            break;
                        }
                    }
                }
            }
            if (pnfex != null) {
                fireNodePathFailed(pnfex);
            }
            if (oldNode != newNode) {
                fireNodeChanged(oldNode, newNode);
            }
        }

        @Override
        public void childNodeInserted(Node parent, Node child) {
            LOG.fine("DOM childNodesInserted("+parent+", "+child+")");
            PathNotFoundException pnfex = null;
            Node oldNode;
            Node newNode;
            synchronized(DOMNode.this) {
                oldNode = newNode = node;
                if (node == null) {
                    // In process of searching for the node...
                    try {
                        createNodePath();
                    } catch (PathNotFoundException pex) {
                        pnfex = pex;
                    }
                    newNode = node;
                }
            }
            if (pnfex != null) {
                fireNodePathFailed(pnfex);
            }
            if (oldNode != newNode) {
                fireNodeChanged(oldNode, newNode);
            }
        }

        @Override
        public void attributeModified(Node node, String attrName) {
            // Ignored
        }

        @Override
        public void attributeRemoved(Node node, String attrName) {
            // Ignored
        }

        @Override
        public void characterDataModified(Node node) {
            // Ignored
        }
        
    }
    
    private static class NodeId {
        private String name;
        private int childNumber;
        
        public NodeId(String name, int childNumber) {
            this.name = name;
            this.childNumber = childNumber;
        }
    }
    
    public static class PathNotFoundException extends Exception {
        
        private String nodeName;
        private int pathPos;
        private String nodePathNames;
        private boolean childrenRequested;
        
        private PathNotFoundException(String nodeName, int pathPos, String nodePathNames) {
            this(nodeName, pathPos, nodePathNames, false);
        }
        
        private PathNotFoundException(String nodeName, int pathPos, String nodePathNames, boolean childrenRequested) {
            super(nodeName+" being "+pathPos+" in "+nodePathNames);
            this.nodeName = nodeName;
            this.pathPos = pathPos;
            this.nodePathNames = nodePathNames;
            this.childrenRequested = childrenRequested;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("new PathNotFoundException("+getMessage()+"), childrenRequested = "+childrenRequested);
            }
        }

        /**
         * Name of the node that was not found.
         */
        public String getNodeName() {
            return nodeName;
        }

        /**
         * The position of the node that was not found in the full path.
         */
        public int getPathPosition() {
            return pathPos;
        }

        /**
         * The full node path.
         */
        public String getNodePathNames() {
            return nodePathNames;
        }
        
        /**
         * Test if further children are requested.
         * @return <code>true</code> when a request was sent to retrieve children,
         * <code>false</code> otherwise.
         */
        public boolean isChildrenRequested() {
            return childrenRequested;
        }
    }
}
