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
package org.netbeans.modules.web.jsf.editor.completion;

import java.net.URL;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.parser.spi.DefaultHelpItem;
import org.netbeans.editor.ext.html.parser.spi.HelpItem;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import org.netbeans.modules.web.jsf.editor.facelets.AbstractFaceletsLibrary;
import org.netbeans.modules.web.jsfapi.spi.LibraryUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class JsfCompletionItem {

    //html items priority varies from 10 to 20
    private static final int JSF_DEFAULT_SORT_PRIORITY = 5;

    //----------- Factory methods --------------
    public static JsfTag createTag(int substitutionOffset, AbstractFaceletsLibrary.NamedComponent component, String declaredPrefix, boolean autoimport) {
        return new JsfTag(substitutionOffset, component, declaredPrefix, autoimport);
    }

    public static JsfTagAttribute createAttribute(String name, int substitutionOffset, AbstractFaceletsLibrary library, org.netbeans.modules.web.jsfapi.api.Tag tag, org.netbeans.modules.web.jsfapi.api.Attribute attr) {
        return new JsfTagAttribute(name, substitutionOffset, library, tag, attr);
    }

    public static class JsfTag extends HtmlCompletionItem.Tag {

        private static final String BOLD_OPEN_TAG = "<b>"; //NOI18N
        private static final String BOLD_END_TAG = "</b>"; //NOI18N
        private AbstractFaceletsLibrary.NamedComponent component;
        private boolean autoimport; //autoimport (declare) the tag namespace if set to true

        public JsfTag(int substitutionOffset, AbstractFaceletsLibrary.NamedComponent component, String declaredPrefix, boolean autoimport) {
            super(generateItemText(component, declaredPrefix), substitutionOffset, null, true);
            this.component = component;
            this.autoimport = autoimport;
        }

        private static String generateItemText(AbstractFaceletsLibrary.NamedComponent component, String declaredPrefix) {
            String libraryPrefix = component.getLibrary().getDefaultPrefix();
            return (declaredPrefix != null ? declaredPrefix : libraryPrefix) + ":" + component.getName(); //NOI18N
        }

        @Override
        protected String getRightHtmlText() {
            return component.getLibrary().getDisplayName();
        }

        @Override
        public void defaultAction(JTextComponent component) {
            super.defaultAction(component);
            if (autoimport) {
                autoimportLibrary(component);
            }
        }

        private void autoimportLibrary(JTextComponent component) {
            final BaseDocument doc = (BaseDocument) component.getDocument();
            AbstractFaceletsLibrary lib = JsfTag.this.component.getLibrary();
            LibraryUtils.importLibrary(doc, lib, null);
        }

        //use bold font
        @Override
        protected String getLeftHtmlText() {
            StringBuilder buff = new StringBuilder();
            buff.append(BOLD_OPEN_TAG);
            buff.append(super.getLeftHtmlText());
            buff.append(BOLD_END_TAG);
            return buff.toString();
        }

        @Override
        public int getSortPriority() {
            return JSF_DEFAULT_SORT_PRIORITY; //jsf tags are more important than html content
        }

        private String getHelpContent() {
            StringBuilder sb = new StringBuilder();
            sb.append(getLibraryHelpHeader(component.getLibrary()));
            sb.append("<h1>"); //NOI18N
            sb.append(component.getName());
            sb.append("</h1>"); //NOI18N

            if(Boolean.getBoolean("show-facelets-libraries-locations")) {
                URL url = component.getLibrary().getLibraryDescriptorSource();
                if(url != null) {
                    FileObject fo = URLMapper.findFileObject(url);
                    if(fo != null) {
                        sb.append("<div style=\"font-size: smaller; color: gray;\">");
                        sb.append("Source: ");
                        sb.append(FileUtil.getFileDisplayName(fo));
                        sb.append("</div>");
                    }
                }
            }

            org.netbeans.modules.web.jsfapi.api.Tag tag = component.getTag();
            if (tag != null) {
                //there is TLD available
                String descr = tag.getDescription();
                if (descr == null) {
                    sb.append(NbBundle.getBundle(this.getClass()).getString("MSG_NO_TLD_ITEM_DESCR")); //NOI18N
                } else {
                    sb.append(descr);
                }
            } else {
                //extract some simple info from the component
                sb.append("<table border=\"1\">"); //NOI18N
                for (String[] descr : component.getDescription()) {
                    sb.append("<tr>"); //NOI18N
                    sb.append("<td>"); //NOI18N
                    sb.append("<div style=\"font-weight: bold\">"); //NOI18N
                    sb.append(descr[0]);
                    sb.append("</div>"); //NOI18N
                    sb.append("</td>"); //NOI18N
                    sb.append("<td>"); //NOI18N
                    sb.append(descr[1]);
                    sb.append("</td>"); //NOI18N
                    sb.append("</tr>"); //NOI18N
                }
                sb.append("</table>"); //NOI18N
            }
            return sb.toString();
        }

        @Override
        public boolean hasHelp() {
            return true;
        }

        @Override
        public HelpItem getHelpItem() {
            return new DefaultHelpItem(null, JsfDocumentation.getDefault(), null, getHelpContent());
        }


    }

    public static class JsfTagAttribute extends HtmlCompletionItem.Attribute {

        private AbstractFaceletsLibrary library;
        private org.netbeans.modules.web.jsfapi.api.Tag tag;
        private org.netbeans.modules.web.jsfapi.api.Attribute attr;

        public JsfTagAttribute(String value, int offset, AbstractFaceletsLibrary library, org.netbeans.modules.web.jsfapi.api.Tag tag, org.netbeans.modules.web.jsfapi.api.Attribute attr) {
            super(value, offset, attr.isRequired(), null);
            this.library = library;
            this.tag = tag;
            this.attr = attr;
        }

        private String getHelpContent() {
            StringBuilder sb = new StringBuilder();
            sb.append(getLibraryHelpHeader(library));
            sb.append("<div><b>Tag:</b> "); //NOI18N
            sb.append(tag.getName());
            sb.append("</div>"); //NOI18N
            sb.append("<h1>"); //NOI18N
            sb.append(attr.getName());
            sb.append("</h1>"); //NOI18N
            if(attr.isRequired()) {
                sb.append("<p>");
                sb.append(NbBundle.getMessage(JsfCompletionItem.class, "MSG_RequiredAttribute"));
                sb.append("</p>");
            }
            sb.append("<p>");
            if(attr.getDescription() != null) {
                sb.append(attr.getDescription());
            } else {
                sb.append(NbBundle.getMessage(JsfCompletionItem.class, "MSG_NoAttributeDescription"));
            }
            sb.append("</p>");
            
            return sb.toString();
        }

        @Override
        public boolean hasHelp() {
            return attr.getDescription() != null;
        }

         @Override
        public HelpItem getHelpItem() {
            return new DefaultHelpItem(null, JsfDocumentation.getDefault(), null, getHelpContent());
        }

    }

    private static String getLibraryHelpHeader(AbstractFaceletsLibrary library) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div><b>Library:</b> "); //NOI18N
        sb.append(library.getNamespace());
        if(library.getDisplayName() != null) {
            sb.append(" ("); //NOI18N
            sb.append(library.getDisplayName());
            sb.append(")</div>"); //NOI18N
        }
        return sb.toString();

    }
}
