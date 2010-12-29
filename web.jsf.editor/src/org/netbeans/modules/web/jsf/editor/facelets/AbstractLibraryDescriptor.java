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
package org.netbeans.modules.web.jsf.editor.facelets;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.web.jsfapi.api.Tag;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author marekfukala
 */
public abstract class AbstractLibraryDescriptor implements LibraryDescriptor {

    private FileObject definitionFile;
    private InputStream content;
    protected String prefix;
    protected String uri;
    protected String displayName;
    protected Map<String, Tag> tags = new HashMap<String, Tag>();

    protected AbstractLibraryDescriptor() {
    }

    protected AbstractLibraryDescriptor(FileObject definitionFile) throws LibraryDescriptorException {
        this.definitionFile = definitionFile;
    }

    protected AbstractLibraryDescriptor(InputStream content) throws LibraryDescriptorException {
        this.content = content;
    }

    public FileObject getDefinitionFile() {
        return definitionFile;
    }

    @Override
    public String getNamespace() {
        return uri;
    }

    @Override
    public String getDefaultPrefix() {
        return prefix;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public Map<String, Tag> getTags() {
        return tags;
    }

    protected void parseLibrary() throws LibraryDescriptorException {
        try {
            parseLibrary(getDefinitionFile().getInputStream());
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected abstract void parseLibrary(InputStream content) throws LibraryDescriptorException;

    private static final String STOP_PARSING_MGS = "regularly_stopped"; //NOI18N

    protected static String parseNamespace(InputStream content, final String tagTagName, final String namespaceTagName ) {
        final String[] ns = new String[1];
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            SAXParser parser = factory.newSAXParser();

            class Handler extends DefaultHandler {

                private boolean inTaglib = false;
                private boolean inURI = false;

                @Override
                public void startElement(String uri, String localname, String qname, Attributes attr) throws SAXException {
                    String tagName = qname.toLowerCase();
                    if (tagTagName.equals(tagName)) { //NOI18N
                        inTaglib = true;
                    }
                    if (inTaglib) {
                        if (namespaceTagName.equals(tagName)) { //NOI18N
                            inURI = true;
                        }

                    }
                }

                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {
                    if(inURI) {
                        ns[0] = new String(ch, start, length).trim();
                        //stop parsing
                        throw new SAXException(STOP_PARSING_MGS);
                    }
                }

                @Override
                public InputSource resolveEntity(String publicId, String systemId) {
                    return new InputSource(new StringReader("")); //prevent the parser to use catalog entity resolver // NOI18N
                }
            }


            parser.parse(content, new Handler());

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SAXException ex) {
            if(!STOP_PARSING_MGS.equals(ex.getMessage())) {
                Exceptions.printStackTrace(ex);
            }
        }

        return ns[0];
    }

    @Override
    public String toString() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getDefinitionFile() != null ? getDefinitionFile().getFileSystem().getRoot().getURL().toString() + ";" + getDefinitionFile().getPath() : ""); //NOI18N
            sb.append("; defaultPrefix = ").append(getDefaultPrefix()).append("; uri = ").append(getNamespace()).append("; tags={"); //NOI18N
            for (Tag t : getTags().values()) {
                sb.append(t.toString());
            }
            sb.append("}]"); //NOI18N
            return sb.toString();
        } catch (FileStateInvalidException ex) {
            return null;
        }
    }

    protected static String getTextContent(Node parent, String childName) {
        Node found = getNodeByName(parent, childName);
        return found == null ? null : found.getTextContent().trim();
    }

    protected static Node getNodeByName(Node parent, String childName) {
        Collection<Node> found = getNodesByName(parent, childName);
        if (!found.isEmpty()) {
            return found.iterator().next();
        } else {
            return null;
        }
    }

    protected static Collection<Node> getNodesByName(Node parent, String childName) {
        Collection<Node> nodes = new ArrayList<Node>();
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(childName)) {
                nodes.add(n);
            }
        }
        return nodes;
    }
}
