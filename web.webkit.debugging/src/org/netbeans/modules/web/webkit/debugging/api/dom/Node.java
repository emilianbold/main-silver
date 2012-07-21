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
package org.netbeans.modules.web.webkit.debugging.api.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * {@code DOM.Node} in WebKit Remote Debugging Protocol.
 *
 * @author Jan Stola
 */
public class Node {
    /** Properties of the node. */
    private JSONObject properties;
    /** Children of the node ({@code nul} when the children are not known yet). */
    private List<Node> children;
    /** Attributes of the node. */
    private List<Attribute> attributes;
    /** Content document (for {@code Frame} nodes). */
    private Node contentDocument;

    /**
     * Creates a new {@code Node} that corresponds to the given JSONObject.
     * 
     * @param node JSONObject describing the node.
     */
    Node(JSONObject node) {
        this.properties = node;

        // Children
        Object childrenValue = node.get("children"); // NOI18N
        if (childrenValue != null) {
            initChildren();
            JSONArray childrenArray = (JSONArray)childrenValue;
            for (Object child : childrenArray) {
                addChild(new Node((JSONObject)child));
            }
        }

        // Attributes
        JSONArray array = (JSONArray)getProperties().get("attributes"); // NOI18N
        if (array != null) {
            for (int i=0; i<array.size()/2; i++) {
                String name = (String)array.get(2*i);
                String value = (String)array.get(2*i+1);
                setAttribute(name, value);
            }
        }

        // Content document
        JSONObject document = (JSONObject)getProperties().get("contentDocument"); // NOI18N
        if (document != null) {
            contentDocument = new Node(document);
        }

        // Cleanup
        node.remove("childNodeCount"); // NOI18N
        node.remove("children"); // NOI18N
        node.remove("attributes"); // NOI18N
        node.remove("contentDocument"); // NOI18N
    }

    /**
     * Returns properties of the node.
     * 
     * @return properties of the node.
     */
    private JSONObject getProperties() {
        return properties;
    }

    /**
     * Returns ID of this node.
     * 
     * @return ID of this node.
     */
    public int getNodeId() {
        return ((Number)getProperties().get("nodeId")).intValue(); // NOI18N
    }

    /**
     * Returns type of this node.
     * 
     * @return type of this node.
     */
    public int getNodeType() {
        return ((Number)getProperties().get("nodeType")).intValue(); // NOI18N
    }

    /**
     * Returns node name.
     * 
     * @return node name.
     */
    public String getNodeName() {
        return (String)getProperties().get("nodeName"); // NOI18N
    }

    /**
     * Returns local name.
     * 
     * @return local name.
     */
    public String getLocalName() {
        return (String)getProperties().get("localName"); // NOI18N
    }

    /**
     * Returns node value.
     * 
     * @return node value.
     */
    public synchronized String getNodeValue() {
        return (String)getProperties().get("nodeValue"); // NOI18N
    }

    /**
     * Sets node value.
     * 
     * @param value new node value.
     */
    synchronized void setNodeValue(String value) {
        getProperties().put("nodeValue", value); // NOI18N
    }

    /**
     * Initializes {@code children} field.
     */
    final synchronized void initChildren() {
        children = new ArrayList<Node>();
    }

    /**
     * Returns sub-nodes of this node. This method returns an empty list
     * if there are no sub-nodes but it can return {@code null} when
     * the sub-nodes are not known yet. If that happens then you may
     * request the sub-nodes using {@code DOM.requestChildNodes()} method.
     * 
     * @return sub-nodes of this node or {@code null} when the sub-nodes
     * are not known yet.
     */
    public synchronized List<Node> getChildren() {
        return children;
    }

    /**
     * Adds child to this node.
     * 
     * @param child new child to add.
     */
    synchronized final void addChild(Node child) {
        if (children == null) {
            initChildren();
        }
        children.add(child);
    }

    /**
     * Removes child from this node.
     * 
     * @param child child to remove.
     */
    synchronized final void removeChild(Node child) {
        children.remove(child);
    }

    /**
     * Inserts child into this node.
     * 
     * @param child child to insert.
     * @param previousChild previous child ({@code null} when the new child
     * should be the first one).
     */
    synchronized final void insertChild(Node child, Node previousChild) {
        if (children == null) {
            initChildren();
        }
        int index;
        if (previousChild == null) {
            index = 0;
        } else {
            index = children.indexOf(previousChild)+1;
        }
        children.add(index, child);
    }

    /**
     * Sets an attribute.
     * 
     * @param name name of the attribute to set.
     * @param value new value of the attribute.
     */
    final synchronized void setAttribute(String name, String value) {
        Attribute attribute = getAttribute(name);
        if (attribute == null) {
            attribute = new Attribute(name, value);
            if (attributes == null) {
                attributes = new ArrayList<Attribute>();
            }
            attributes.add(attribute);
        } else {
            attribute.setValue(value);
        }
    }

    /**
     * Removes an attribute.
     * 
     * @param name name of the attribute to remove.
     */
    final synchronized void removeAttribute(String name) {
        Attribute attribute = getAttribute(name);
        if (attribute != null) {
            attributes.remove(attribute);
        }
    }

    /**
     * Returns the attribute with the specified name.
     * 
     * @param name name of the attribute.
     * @return attribute with the specified name or {@code null} when there
     * is no such attribute.
     */
    public synchronized Attribute getAttribute(String name) {
        Attribute result = null;
        for (Attribute attr : getAttributes()) {
            if (name.equals(attr.getName())) {
                result = attr;
                break;
            }
        }
        return result;
    }

    /**
     * Returns all attributes of this node.
     * 
     * @return attributes of this node ({@code null} is never returned,
     * an empty list is returned when there are no attributes).
     */
    public synchronized List<Attribute> getAttributes() {
        return (attributes == null) ? Collections.EMPTY_LIST : attributes;
    }

    /**
     * Returns URL of the document.
     * 
     * @return URL of the document the node points to (for {@code Document}
     * and {@code FrameOwner} nodes) or {@code null} (otherwise).
     */
    public String getDocumentURL() {
        return (String)getProperties().get("documentURL"); // NOI18N
    }

    /**
     * Returns Public ID.
     * 
     * @return public ID (for {@code DocumentType} nodes)
     * or {@code null} (otherwise).
     */
    public String getPublicId() {
        return (String)getProperties().get("publicId"); // NOI18N
    }

    /**
     * Returns System ID.
     * 
     * @return system ID (for {@code DocumentType} nodes)
     * or {@code null} (otherwise).
     */
    public String getSystemId() {
        return (String)getProperties().get("systemId"); // NOI18N
    }

    /**
     * Returns internal subset.
     * 
     * @return internal subset (for {@code DocumentType} nodes)
     * or {@code null} (otherwise).
     */
    public String getInternalSubset() {
        return (String)getProperties().get("internalSubset"); // NOI18N
    }

    /**
     * Returns XML version.
     * 
     * @return XML version (for {@code Document} nodes of XML documents)
     * or {@code null} (otherwise).
     */
    public String getXmlVersion() {
        return (String)getProperties().get("xmlVersion"); // NOI18N
    }

    public String getName() {
        return (String)getProperties().get("name"); // NOI18N
    }

    public String getValue() {
        return (String)getProperties().get("value"); // NOI18N
    }

    /**
     * Determines whether this node was injected by NetBeans or whether
     * it is part of the original document.
     * 
     * @return {@code true} when the node was injected by NetBeans,
     * returns {@code false} when the node is part of the original document.
     */
    public boolean isInjectedByNetBeans() {
        return getAttribute(":netbeans_generated") != null; // NOI18N
    }

    /**
     * Returns a content document.
     * 
     * @return content document (for {@code FrameOwner} nodes)
     * or {@code null} (otherwise).
     */
    public Node getContentDocument() {
        return contentDocument;
    }
    
    // PENDING
    //getShadowRoots()

    @Override
    public boolean equals(Object obj) {
        boolean equal = false;
        if (obj instanceof Node) {
            int nodeId = ((Node)obj).getNodeId();
            equal = (nodeId == getNodeId());
        }
        return equal;
    }

    @Override
    public int hashCode() {
        return getNodeId();
    }

    /**
     * Element's attribute.
     */
    public static class Attribute {
        /** Name of this attribute. */
        private String name;
        /** Value of this attribute. */
        private String value;

        /**
         * Creates a new {@code Attribute}.
         * 
         * @param name name of the attribute.
         * @param value value of the attribute.
         */
        Attribute(String name, String value) {
            this.name = name;
            this.value = value;
        }

        /**
         * Returns name of this attribute.
         * 
         * @return name of this attribute.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns value of this attribute.
         * 
         * @return value of this attribute.
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets value of this attribute.
         * 
         * @param value new value of this attribute.
         */
        void setValue(String value) {
            this.value = value;
        }
    }

}
