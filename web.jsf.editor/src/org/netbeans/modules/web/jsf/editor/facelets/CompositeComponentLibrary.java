/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.web.jsf.editor.JsfUtils;
import org.netbeans.modules.web.jsf.editor.index.CompositeComponentModel;
import org.netbeans.modules.web.jsf.editor.index.JsfIndex;
import org.netbeans.modules.web.jsf.editor.tld.LibraryDescriptor;
import org.netbeans.modules.web.jsf.editor.tld.LibraryDescriptorException;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class CompositeComponentLibrary extends FaceletsLibrary {

    private String libraryName;
    private LibraryDescriptor generatedDescribingLibrary;

    //for undeclared libraries w/o prefix
    public CompositeComponentLibrary(FaceletsLibrarySupport support, String libraryName) {
        this(support, libraryName, null);
    }

    public CompositeComponentLibrary(FaceletsLibrarySupport support, String libraryName, String namespace) {
        super(support, namespace);
        this.libraryName = libraryName;
    }

    @Override
    public String getNamespace() {
        return getDeclaredNamespace() != null ? getDeclaredNamespace() : getDefaultNamespace();
    }
    
    public String getDeclaredNamespace() {
        return super.getNamespace();
    }

    public String getDefaultNamespace() {
        return JsfUtils.getCompositeLibraryURL(getLibraryName());
    }

    @Override
    public String getDisplayName() {
        return getLibraryName();
    }

    public String getLibraryName() {
        return libraryName;
    }

    @Override
    public Collection<NamedComponent> getComponents() {
        Collection<String> componentNames = index().getCompositeLibraryComponents(getLibraryName());
        Collection<NamedComponent> components = new ArrayList<NamedComponent>();
        for (String compName : componentNames) {
            NamedComponent comp = new CompositeComponent(compName);
            components.add(comp);
        }
        return components;
    }

    @Override
    public LibraryDescriptor getLibraryDescriptor() {
        LibraryDescriptor libDescriptor = support.getJsfSupport().getLibraryDescriptor(getNamespace());
            if (libDescriptor != null) {
                //ohh, someone made a .taglib.xml or TLD for us, nice...
                return libDescriptor;
            }
        //most cases, no tld, generate something so the completion and other stuff works
        //todo - implement reasonable caching
//        if (generatedDescribingLibrary == null) {
        generatedDescribingLibrary = new CCTldLibrary();
//        }
        return generatedDescribingLibrary;
    }

    @Override
    public String toString() {
        return "CompositeComponent(" + (getNamespace() == null ? //NOI18N
            "created via indexing" :  //NOI18N
            "created by Mojarra") + " " + super.toString(); //NOI18N
    }

    private JsfIndex index() {
        return support.getJsfSupport().getIndex();
    }

    public class CompositeComponent extends NamedComponent {

        public CompositeComponent(String name) {
            super(name);
        }

        public CompositeComponentModel getComponentModel() {
            return index().getCompositeComponentModel(getLibraryName(), name);
        }

    }

    private class CCTldLibrary extends LibraryDescriptor {

        private Map<String, Tag> cctags = new HashMap<String, Tag>();

        public CCTldLibrary() { 
            init();
        }

        @Override
        protected void parseLibrary(InputStream content) throws LibraryDescriptorException {
            //this library type does not need to parse any content this way
            //sure the inheritance is a mess
        }

        private void init() {
            Collection<String> componentNames = index().getCompositeLibraryComponents(getLibraryName());
            for (String cname : componentNames) {
                CompositeComponentModel model = index().getCompositeComponentModel(getLibraryName(), cname);
                Map<String, Attribute> attrs = new HashMap<String, Attribute>();
                String msgNoTld = NbBundle.getBundle(CompositeComponentLibrary.class).getString("MSG_NO_DESCRIPTOR"); //NOI18N
                for (Map<String, String> attrsMap : model.getExistingInterfaceAttributes()) {
                    String attrname = attrsMap.get("name"); //NOI18N
                    boolean required = Boolean.parseBoolean(attrsMap.get("required")); //NOI18N
                    String description = getAttributesDescription(model);
                    attrs.put(attrname, new Attribute(attrname, description, required));
                }

                StringBuffer sb = new StringBuffer();
                sb.append("<p><b>"); //NOI18N
                sb.append(NbBundle.getMessage(CompositeComponentLibrary.class, "MSG_COMPOSITE_COMPONENT_SOURCE") );//NOI18N
                sb.append("</b>");//NOI18N
                sb.append("&nbsp;");//NOI18N
                sb.append(model.getRelativePath());
                sb.append("</p>");//NOI18N
                sb.append("<p>");//NOI18N
                sb.append(getAttributesDescription(model));
                sb.append("</p>");//NOI18N
                sb.append("<p style=\"color: red\">" + msgNoTld + "</p>"); //NOI18N

                Tag t = new TagImpl(cname, sb.toString(), attrs);
                cctags.put(cname, t);
            }
        }

        private String getAttributesDescription(CompositeComponentModel model) {
            if(model.getExistingInterfaceAttributes().isEmpty()) {
                return NbBundle.getMessage(CompositeComponentLibrary.class, "MSG_NO_TAG_ATTRS");//NOI18N
            }

            StringBuffer sb = new StringBuffer();
            sb.append("<b>");//NOI18N
            sb.append(NbBundle.getMessage(CompositeComponentLibrary.class, "MSG_TAG_ATTRS"));//NOI18N
            sb.append("</b>");//NOI18N
            sb.append("<table border=\"1\">"); //NOI18N

            for (Map<String, String> descr : model.getExistingInterfaceAttributes()) {
                sb.append("<tr>"); //NOI18N
                sb.append("<td>"); //NOI18N
                sb.append("<div style=\"font-weight: bold\">"); //NOI18N
                String attrname = descr.get("name"); //NOI18N);
                sb.append(attrname);
                sb.append("</div>"); //NOI18N
                sb.append("</td>"); //NOI18N
                sb.append("<td>"); //NOI18N

                sb.append("<table border=\"0\" padding=\"0\" margin=\"0\" spacing=\"2\">"); //NOI18N
                for (String key : descr.keySet()) {
                    if (key.equals("name")) {//NOI18N
                        continue; //skip name
                    }
                    String val = descr.get(key);
                    sb.append("<tr><td><b>");//NOI18N
                    sb.append(key);
                    sb.append("</b></td><td>");//NOI18N
                    sb.append(val);
                    sb.append("</td></tr>");//NOI18N
                }
                sb.append("</table>"); //NOI18N


                sb.append("</td>"); //NOI18N
                sb.append("</tr>"); //NOI18N
                }
            sb.append("</table>"); //NOI18N


            return sb.toString();
        }

        @Override
        public Map<String, Tag> getTags() {
            return cctags;
        }
    }
}
