/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.custom.conf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * TODO: project's config should be merged with the default one in $userdir/conf
 *
 * @author marek
 */
public class Configuration {

    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

    private static final String CONF_FILE_NAME = "customs.json"; //NOI18N

    private static final Map<Project, Configuration> MAP
            = new WeakHashMap<>();

    //json file keys
    public static final String DESCRIPTION = "description";
    public static final String CONTEXT = "context";
    public static final String DOC = "doc";
    public static final String DOC_URL = "doc_url";
    public static final String ELEMENTS = "elements";
    public static final String ATTRIBUTES = "attributes";
    public static final String TYPE = "type";

    private final Map<String, Tag> TAGS = new HashMap<>();
    private final Map<String, Attribute> ATTRS = new HashMap<>();

    public static Configuration get(@NonNull FileObject file) {
        Project owner = FileOwnerQuery.getOwner(file);
        return get(owner);
    }

    @NonNull
    public static Configuration get(@NonNull Project project) {
        synchronized (MAP) {
            Configuration conf = MAP.get(project);
            if (conf == null) {
                conf = new Configuration(project);
                MAP.put(project, conf);
            }
            return conf;
        }
    }

    private FileObject configFile;
    private JSONObject root;

    public Configuration(Project project) {
        //TODO fix the conf location in maven and other project types
        FileObject nbproject = project.getProjectDirectory().getFileObject("config"); //NOI18N
        if (nbproject != null) {
            try {
                configFile = nbproject.getFileObject(CONF_FILE_NAME);

//            if (configFile == null) {
//                configFile = nbproject.createData(CONF_FILE_NAME); //create one if doesn't exist
//                LOGGER.log(Level.INFO, "Created configuration file {0} ", configFile.getPath()); //NOI18N
//            }
                if (configFile != null) {
                    configFile.addFileChangeListener(new FileChangeAdapter() {

                        @Override
                        public void fileChanged(FileEvent fe) {
                            LOGGER.log(Level.INFO, "Config file {0} changed - reloading configuration.", configFile.getPath()); //NOI18N
                            try {
                                reload();
                            } catch (IOException ex) {
                                handleIOEFromReload(ex);
                            }
                        }

                    });

                    reload();
                }

            } catch (IOException ex) {
                handleIOEFromReload(ex);
            }
        }

    }

    public FileObject getProjectsConfigurationFile() {
        return configFile;
    }

    private void handleIOEFromReload(IOException e) {
        Project project = FileOwnerQuery.getOwner(configFile);
        String projectDisplayName = project != null ? ProjectUtils.getInformation(project).getDisplayName() : "???"; //NOI18N
        String msg = String.format("An error found in the configuration file %s in the project %s: %s", configFile.getNameExt(), projectDisplayName, e.getMessage());

        NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notifyLater(d);
    }

    /**
     * Gets a collection of the tags registered to the root context.
     *
     * @return
     */
    public Collection<String> getTagsNames() {
        return TAGS.keySet();
    }

    public Collection<Tag> getTags() {
        return TAGS.values();
    }

    /**
     * Gets a collection of the attributes registered to the root context.
     *
     * @return
     */
    public Collection<String> getAttributesNames() {
        return ATTRS.keySet();
    }

    public Collection<Attribute> getAttributes() {
        return ATTRS.values();
    }

    public Tag getTag(String tagName) {
        return TAGS.get(tagName);
    }

    public Attribute getAttribute(String name) {
        return ATTRS.get(name);
    }

    public void add(Tag t) {
        TAGS.put(t.getName(), t);
    }

    public void remove(Tag t) {
        TAGS.remove(t.getName());
    }

    public void add(Attribute a) {
        ATTRS.put(a.getName(), a);
    }

    public void remove(Attribute a) {
        ATTRS.remove(a.getName());
    }

    private void reload() throws IOException {
        //if something goes wrong, the data will be empty until the problem is corrected
        TAGS.clear();
        ATTRS.clear();

        final Document document = getDocument(configFile);
        final AtomicReference<String> docContentRef = new AtomicReference<>();
        final AtomicReference<BadLocationException> bleRef = new AtomicReference<>();
        document.render(new Runnable() {

            @Override
            public void run() {
                try {
                    docContentRef.set(document.getText(0, document.getLength()));
                } catch (BadLocationException ex) {
                    bleRef.set(ex);
                }
            }

        });
        if (bleRef.get() != null) {
            throw new IOException(bleRef.get());
        }

        String content = docContentRef.get();
        root = (JSONObject) JSONValue.parse(content);
        if (root == null) {
            //parsing error
            throw new IOException("Can't parse the JSON source"); //NOI18N
        }

        JSONObject elements = (JSONObject) root.get(ELEMENTS);
        if (elements != null) {
            Collection<Tag> rootTags = loadTags(elements, null);
            for (Tag rootTag : rootTags) {
                TAGS.put(rootTag.getName(), rootTag);
            }
        }
        JSONObject attributes = (JSONObject) root.get(ATTRIBUTES);
        if (attributes != null) {
            Collection<Attribute> rootAttrs = loadAttributes(attributes, null);
            for (Attribute a : rootAttrs) {
                ATTRS.put(a.getName(), a);
            }
        }

    }

    private Document getDocument(FileObject file) throws DataObjectNotFoundException, IOException {
        DataObject dobj = DataObject.find(file);
        EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
        if (ec != null) {
            return ec.openDocument();
        }
        return null;
    }

    public JSONObject store() throws IOException {
        JSONObject node = new JSONObject();
        storeTags(node, TAGS.values());
        storeAttributes(node, ATTRS.values());

        //serialize the current model
        final String newContent = node.toJSONString();

        //and save it to the underlying document/file
        DataObject dobj = DataObject.find(configFile);
        EditorCookie editorCookie = dobj.getLookup().lookup(EditorCookie.class);
        final BaseDocument document = (BaseDocument) editorCookie.openDocument();
        final AtomicReference<BadLocationException> bleRef = new AtomicReference<>();

        final Reformat reformat = Reformat.get(document);
        Preferences preferences = CodeStylePreferences.get(document).getPreferences();

        //depends on javascript2.editor
        preferences.put("wrapArrayInit", "WRAP_ALWAYS"); //NOI18N
        preferences.put("wrapArrayInitItems", "WRAP_ALWAYS"); //NOI18N
        preferences.put("wrapObjects", "WRAP_ALWAYS"); //NOI18N
        preferences.put("wrapProperties", "WRAP_ALWAYS"); //NOI18N

        //modify
        document.runAtomic(new Runnable() {

            @Override
            public void run() {
                try {
                    //TODO apply just changes via diff!
                    document.remove(0, document.getLength());
                    document.insertString(0, newContent, null);
                } catch (BadLocationException ex) {
                    bleRef.set(ex);
                }
            }

        });
        if (bleRef.get() != null) {
            throw new IOException(bleRef.get());
        }

        //reformat
        try {
            reformat.lock();
            document.runAtomic(new Runnable() {

                @Override
                public void run() {
                    try {
                        reformat.reformat(0, document.getLength());
                    } catch (BadLocationException ex) {
                        bleRef.set(ex);
                    }
                }

            });
            if (bleRef.get() != null) {
                throw new IOException(bleRef.get());
            }
        } finally {
            reformat.unlock();
        }

        //save changes
        editorCookie.saveDocument();

        //TODO reindex all affected indexers!
        return node;
    }

    private void storeTags(JSONObject node, Collection<Tag> tags) {
        JSONObject el = new JSONObject();
        node.put(ELEMENTS, el);

        for (Tag t : tags) {
            storeTag(el, t);
        }
    }

    private void storeTag(JSONObject node, Tag t) {
        JSONObject ctn = new JSONObject();
        node.put(t.getName(), ctn);

        storeElement(ctn, t);

        Collection<Tag> children = t.getTags();
        if (!children.isEmpty()) {
            storeTags(ctn, children);
        }
        Collection<Attribute> attributes = t.getAttributes();
        if (!attributes.isEmpty()) {
            storeAttributes(ctn, attributes);
        }
    }

    private void storeAttributes(JSONObject node, Collection<Attribute> attributes) {
        JSONObject el = new JSONObject();
        node.put(ATTRIBUTES, el);

        for (Attribute t : attributes) {
            storeAttribute(el, t);
        }
    }

    private void storeAttribute(JSONObject node, Attribute t) {
        JSONObject ctn = new JSONObject();
        node.put(t.getName(), ctn);
        String type = t.getType();
        if (type != null) {
            ctn.put(TYPE, type);
        }
        storeElement(ctn, t);
    }

    private void storeElement(JSONObject ctn, Element t) {
        if (t.getDescription() != null) {
            ctn.put(DESCRIPTION, t.getDescription());
        }
        if (t.getDocumentation() != null) {
            ctn.put(DOC, t.getDocumentation());
        }
        if (t.getDocumentationURL() != null) {
            ctn.put(DOC_URL, t.getDocumentationURL());
        }
        Collection<String> contexts = t.getContexts();
        if (!contexts.isEmpty()) {
            if (contexts.size() == 1) {
                //as string
                ctn.put(CONTEXT, contexts.iterator().next());
            } else {
                //as array
                ctn.put(CONTEXT, contexts);
            }
        }
    }

    private List<Tag> loadTags(JSONObject node, Tag parent) {
        List<Tag> tags = new ArrayList<>();
        for (Object key : node.keySet()) {
            String name = (String) key;
            JSONObject val = (JSONObject) node.get(name);

            LOGGER.log(Level.FINE, "element {0}: {1}", new Object[]{name, val});

            ArrayList<String> contexts = new ArrayList<>();
            Object ctx = val.get(CONTEXT);
            if (ctx instanceof JSONObject) {
                //????
            } else if (ctx instanceof String) {
                contexts.add((String) ctx);
            } else if (ctx instanceof JSONArray) {
                //list of string values
                contexts = (JSONArray) ctx;
            }

            String description = null;
            Object _description = val.get(DESCRIPTION);
            if (_description != null) {
                if (_description instanceof String) {
                    description = (String) _description;
                } else {
                    LOGGER.log(Level.WARNING, "The '{0}' key needs to have a string value!", DESCRIPTION);
                }
            }

            String doc = null;
            Object _doc = val.get(DOC);
            if (_doc != null) {
                if (_doc instanceof String) {
                    doc = (String) _doc;
                } else {
                    LOGGER.log(Level.WARNING, "The '{0}' key needs to have a string value!", DOC);
                }
            }

            String docURL = null;
            Object _docURL = val.get(DOC_URL);
            if (_docURL != null) {
                if (_docURL instanceof String) {
                    docURL = (String) _docURL;
                } else {
                    LOGGER.log(Level.WARNING, "The '{0}' key needs to have a string value!", DOC_URL);
                }
            }

            Tag tag = new Tag(name, description, doc, docURL, parent, contexts.toArray(new String[0]));

            //process nested elements
            Object _elements = val.get(ELEMENTS);
            if (_elements != null) {
                if (_elements instanceof JSONObject) {
                    JSONObject els = (JSONObject) _elements;
                    Collection<Tag> elements = loadTags(els, tag);
                    tag.setChildren(elements);
                } else {
                    LOGGER.log(Level.WARNING, "The '{0}' key needs to have a map value!", ELEMENTS);
                }
            }

            //process nested attributes
            JSONObject attributes = (JSONObject) val.get(ATTRIBUTES);

            Collection<Attribute> attrs = attributes != null
                    ? loadAttributes(attributes, tag)
                    : Collections.<Attribute>emptyList();

            tag.setAttributes(attrs);

            tags.add(tag);

        }
        return tags;
    }

    private List<Attribute> loadAttributes(JSONObject node, Tag tag) {
        List<Attribute> attrs = new ArrayList<>();
        for (Object key : node.keySet()) {
            String name = (String) key;
            Object value = node.get(key);
            LOGGER.log(Level.FINE, "attribute {0}: {1}", new Object[]{key, value});

            if (value instanceof String) {
                //the string value specifies just the type - boolean, string etc.
                String type = (String) value;
                Attribute a = new Attribute(name, type, null, null, null, tag);
                attrs.add(a);

            } else if (value instanceof JSONObject) {
                //map
                JSONObject val = (JSONObject) value;

                ArrayList<String> contexts = new ArrayList<>();
                Object ctx = val.get(CONTEXT);
                if (ctx instanceof JSONObject) {
                    //????
                } else if (ctx instanceof String) {
                    contexts.add((String) ctx);
                } else if (ctx instanceof JSONArray) {
                    //list of string values
                    contexts = (JSONArray) ctx;
                }

                String type = null;
                Object _type = val.get(TYPE);
                if (_type != null) {
                    if (_type instanceof String) {
                        type = (String) _type;
                    } else {
                        LOGGER.log(Level.WARNING, "The '{0}' key needs to have string value!", TYPE);
                    }
                }

                String description = null;
                Object _description = val.get(DESCRIPTION);
                if (_description != null) {
                    if (_description instanceof String) {
                        description = (String) _description;
                    } else {
                        LOGGER.log(Level.WARNING, "The '{0}' key needs to have string value!", DESCRIPTION);
                    }
                }

                String doc = null;
                Object _doc = val.get(DOC);
                if (_doc != null) {
                    if (_doc instanceof String) {
                        doc = (String) _doc;
                    } else {
                        LOGGER.log(Level.WARNING, "The '{0}' key needs to have string value!", DOC);
                    }
                }

                String docURL = null;
                Object _docURL = val.get(DOC_URL);
                if (_docURL != null) {
                    if (_docURL instanceof String) {
                        docURL = (String) _docURL;
                    } else {
                        LOGGER.log(Level.WARNING, "The '{0}' key needs to have string value!", DOC_URL);
                    }
                }

                Attribute a = new Attribute(name, type, description, doc, docURL, tag, contexts.toArray(new String[0]));
                attrs.add(a);
            }

        }
        return attrs;
    }

}
