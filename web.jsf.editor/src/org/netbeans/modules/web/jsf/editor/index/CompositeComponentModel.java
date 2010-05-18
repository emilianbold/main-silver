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
package org.netbeans.modules.web.jsf.editor.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.AstNodeVisitor;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.editor.JsfUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author marekfukala
 */
public class CompositeComponentModel extends JsfPageModel {

    //index keys
    static final String LIBRARY_NAME_KEY = "library"; //NOI18N
    static final String INTERFACE_ATTRIBUTES_KEY = "interface_attributes"; //NOI18N
    static final String HAS_IMPLEMENTATION_KEY = "has_implementation"; //NOI18N
    //other
    private static final String RESOURCES_FOLDER_NAME = "resources"; //NOI18N
    private static final char VALUES_SEPARATOR = ','; //NOI18N
    private static final char ATTRIBUTES_SEPARATOR = ';'; //NOI18N
    private static final char KEY_VALUE_SEPARATOR = '='; //NOI18N
    private static final Collection<String> COMPOSITE_ATTRIBUTE_TAG_ATTRIBUTES =
            Arrays.asList(new String[]{"name", "targets", "default", "displayName", "expert",
                "method-signature", "preferred", "required", "shortDescription", "type"}); //NOI18N
    protected Collection<Map<String, String>> attributes;
    protected boolean hasImplementation;
    protected FileObject sourceFile;
    protected String relativePath;

    public CompositeComponentModel(FileObject file, Collection<Map<String, String>> attributes, boolean hasImplementation) {
        this(file, null, attributes, hasImplementation);
    }

    public CompositeComponentModel(FileObject file, String relativePath, Collection<Map<String, String>> attributes, boolean hasImplementation) {
        super();
        this.attributes = attributes;
        this.hasImplementation = hasImplementation;
        this.sourceFile = file;
        this.relativePath = relativePath;
    }

    public String getComponentName() {
        return sourceFile.getName();
    }

    public FileObject getSourceFile() {
        return sourceFile;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public boolean isHasImplementation() {
        return hasImplementation;
    }

    public Collection<Map<String, String>> getExistingInterfaceAttributes() {
        return attributes;
    }

    public Collection<String> getPossibleAttributeTagAttributes() {
        return COMPOSITE_ATTRIBUTE_TAG_ATTRIBUTES;
    }

    @Override
    public String storeToIndex(IndexDocument document) {
        //store library name
        String libraryName = getLibraryPath();
        document.addPair(LIBRARY_NAME_KEY, libraryName, true, true);

        //store attributes
        StringBuffer buf = new StringBuffer();
        Iterator<Map<String, String>> itr = attributes.iterator();
        while (itr.hasNext()) {
            Map<String, String> attrs = itr.next();
            Iterator<String> attrsKeysItr = attrs.keySet().iterator();
            while (attrsKeysItr.hasNext()) {
                String key = attrsKeysItr.next();
                String value = attrs.get(key);
                buf.append(key);
                buf.append(KEY_VALUE_SEPARATOR);
                buf.append(encode(value));
                if (attrsKeysItr.hasNext()) {
                    buf.append(VALUES_SEPARATOR);
                }
            }
            if (itr.hasNext()) {
                buf.append(ATTRIBUTES_SEPARATOR);
            }
        }
        document.addPair(INTERFACE_ATTRIBUTES_KEY, buf.toString(), false, true);

        //store implementation mark
        document.addPair(HAS_IMPLEMENTATION_KEY, Boolean.toString(hasImplementation), false, true);

	return JsfUtils.getCompositeLibraryURL(libraryName);

    }

    private String getLibraryPath() {
        FileObject sourceFileFolder = sourceFile.getParent();
        FileObject resources = getResourcesDirectory(sourceFile);

        assert resources != null;
        assert !sourceFileFolder.equals(resources);

        return FileUtil.getRelativePath(resources, sourceFileFolder);
    }

    private static boolean isCompositeLibraryMember(FileObject file) {
        FileObject resourcesFolder = getResourcesDirectory(file);
        if (resourcesFolder != null) {
                //test if the file is an indirect ancestor of the resources folder.
                //the file cannot be in the resources folder itself
                if (FileUtil.isParentOf(resourcesFolder, file) && !file.getParent().equals(resourcesFolder)) {
                    return true;
                }
            }
        return false;
    }

    private static FileObject getResourcesDirectory(FileObject file) {
        WebModule wm = WebModule.getWebModule(file);
        if (wm != null) {
            //check webmodule's resources folder
            FileObject docRoot = wm.getDocumentBase();
            if(docRoot != null) { //document root may be null if the folder is deleted
                FileObject resourcesFolder = getChild(docRoot, RESOURCES_FOLDER_NAME);
                //check if the file is a descendant of the resources folder
                if(resourcesFolder != null && FileUtil.isParentOf(resourcesFolder, file)) {
                    return resourcesFolder;
                }
            }
        }
        
        //check project's sources - META-INF.*/resources
        //just check if the parent's parent directory is resources and then META-INF
        FileObject folder = file;
        do {
            if (folder.getName().equalsIgnoreCase("resources")) {
                //check if its parent is META-INF
                FileObject parent = folder.getParent();
                if (parent != null && parent.getNameExt().startsWith("META-INF")) {
                    //the folder seems to be the right resources folder
                    return folder;
                }
            }
            folder = folder.getParent();
        } while (folder != null);


        return null;
    }

    private static FileObject getChild(FileObject parent, String name) {
        for (FileObject child : parent.getChildren()) {
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    public static class Factory extends JsfPageModelFactory {

        private static final String COMPOSITE_ATTRIBUTE_TAG_NAME = "attribute"; //NOI18N
        private static final String INTERFACE_TAG_NAME = "interface"; //NOI18N
        private static final String IMPLEMENTATION_TAG_NAME = "implementation"; //NOI18N

        @Override
        public JsfPageModel getModel(HtmlParserResult result) {
            AstNode node = result.root(JsfUtils.COMPOSITE_LIBRARY_NS); //NOI18N
            if(node == null) {
                return null; //no composite library declaration
            }
            FileObject file = result.getSnapshot().getSource().getFileObject();

            //check whether the file lies in appropriate library folder
            if (!isCompositeLibraryMember(file)) {
                return null;
            }

            //composite:attribute tag -> map of its attributes
            final Collection<Map<String, String>> interfaceAttrs = new ArrayList<Map<String, String>>();
            final boolean[] hasInterface = new boolean[1];
            final boolean[] hasImplementation = new boolean[1];

            AstNodeUtils.visitChildren(node, new AstNodeVisitor() {

                public void visit(AstNode node) {
                    if (node.getNameWithoutPrefix().equals(INTERFACE_TAG_NAME)) {
                        hasInterface[0] = true;
                        for (AstNode child : node.children()) {
                            if (child.getNameWithoutPrefix().equals(COMPOSITE_ATTRIBUTE_TAG_NAME)) {
                                //found composite:attribute tag
                                Map<String, String> attrs = new HashMap<String, String>();
                                for (String attrKey : child.getAttributeKeys()) {
                                    attrs.put(attrKey, child.getAttribute(attrKey).unquotedValue());
                                }
                                interfaceAttrs.add(attrs);
                            }
                        }
                    } else if (node.getNameWithoutPrefix().equals(IMPLEMENTATION_TAG_NAME)) {
                        hasImplementation[0] = true;
                    }
                }
            });

            //#176807 - The component file itself doesn't have to declare the interface or
            //implementation, it can be done in another referred page
//            if (hasInterface[0]) {
            return new CompositeComponentModel(file, interfaceAttrs, hasImplementation[0]);
//            }

//            return null;

        }

        @Override
        public JsfPageModel loadFromIndex(IndexResult result) {
            String attrs = result.getValue(INTERFACE_ATTRIBUTES_KEY);
            boolean hasImplementation = Boolean.parseBoolean(result.getValue(HAS_IMPLEMENTATION_KEY));
            Collection<Map<String, String>> parsedAttrs = new ArrayList<Map<String, String>>();
            //parse attributes
            StringTokenizer st = new StringTokenizer(attrs, Character.valueOf(ATTRIBUTES_SEPARATOR).toString());
            while (st.hasMoreTokens()) {
                String attrText = st.nextToken();
                Map<String, String> pairs = new HashMap<String, String>();
                StringTokenizer st2 = new StringTokenizer(attrText, Character.valueOf(VALUES_SEPARATOR).toString());
                while (st2.hasMoreTokens()) {
                    String pair = st2.nextToken();
                    String key = pair.substring(0, pair.indexOf(KEY_VALUE_SEPARATOR));
                    String value = decode(pair.substring(pair.indexOf(KEY_VALUE_SEPARATOR) + 1));
                    pairs.put(key, value);
                }
                parsedAttrs.add(pairs);
            }
            return new CompositeComponentModel(result.getFile(), result.getRelativePath(), parsedAttrs, hasImplementation);

        }
    }


    static final String encode(String attributeValue) {
        //comma and equal sign needs to be encoded
        StringBuilder out = new StringBuilder();
        for(int i = 0; i < attributeValue.length(); i++) {
            char c = attributeValue.charAt(i);
            switch(c) {
                case ',':
                    out.append("\\c");
                    break;
                case '=':
                    out.append("\\e");
                    break;
                case '\\':
                    out.append("\\s");
                    break;
                default:
                    out.append(c);

            }
        }
        return out.toString();
    }

    static final String decode(String attributeValue) {
        //comma and equal sign needs to be encoded
        StringBuilder out = new StringBuilder();
        boolean encodeChar = false;
        for(int i = 0; i < attributeValue.length(); i++) {
            char c = attributeValue.charAt(i);
            if(encodeChar) {
                switch(c) {
                    case 'c':
                        out.append(',');
                        break;
                    case 'e':
                        out.append('=');
                        break;
                    case 's':
                        out.append('\\');
                        break;
                    default:
                        assert false;
                }
                encodeChar = false;
            } else {
                if(c == '\\') {
                    encodeChar = true;
                } else {
                    out.append(c);
                }
            }
        }
        return out.toString();
    }
}
