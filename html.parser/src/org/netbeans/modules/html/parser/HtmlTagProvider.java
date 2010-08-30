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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.html.parser;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Logger;
import nu.validator.htmlparser.impl.ElementName;
import org.netbeans.editor.ext.html.parser.spi.HtmlTag;
import org.netbeans.editor.ext.html.parser.spi.HtmlTagAttribute;
import org.netbeans.editor.ext.html.parser.spi.HtmlTagAttributeType;

/**
 *
 * @author marekfukala
 */
public class HtmlTagProvider {

    private static Logger LOGGER = Logger.getLogger(HtmlTagProvider.class.getName());

    private static HashMap<ElementName, HtmlTag> MAP = new HashMap<ElementName, HtmlTag>();
    private static HashMap<String, HtmlTagAttribute> ATTRS_MAP = new HashMap<String, HtmlTagAttribute>();

    public static synchronized HtmlTag getTagForElement(ElementName elementName) {
        HtmlTag impl = MAP.get(elementName);
        if(impl == null) {
            impl = new ElementName2HtmlTagAdapter(elementName);
            MAP.put(elementName, impl);
        }
        return impl;
    }

    private static synchronized HtmlTagAttribute getHtmlTagAttribute(String attributeName) {
        HtmlTagAttribute attr = ATTRS_MAP.get(attributeName);
        if(attr == null) {
            attr = new SimpleHtmlTagAttribute(attributeName);
            ATTRS_MAP.put(attributeName, attr);
        }
        return attr;
    }

    private static class ElementName2HtmlTagAdapter implements HtmlTag {

         private ElementName element;
         private Collection<HtmlTagAttribute> attrs;

        private ElementName2HtmlTagAdapter(ElementName element) {
            this.element = element;
            this.attrs = wrap(ElementAttributes.getAttrNamesForElement(element.name));
        }

        private Collection<HtmlTagAttribute> wrap(Collection<String> attrNames) {
            if(attrNames == null) {
                return Collections.emptyList();
            }
            Collection<HtmlTagAttribute> attributes = new LinkedList<HtmlTagAttribute>();
            for(String an : attrNames) {
                HtmlTagAttribute hta = getHtmlTagAttribute(an);
                if(hta != null) {
                    attributes.add(hta);
                } else {
                    LOGGER.info("Unknown attribute " + an + " requested.");//NOI18N
                }
            }
            return attributes;
        }

        public String getName() {
            return element.name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if(!(obj instanceof HtmlTag)) {
                return false;
            }
            final HtmlTag other = (HtmlTag) obj;
            if ((this.getName() == null) ? (other.getName() != null) : !this.getName().equals(other.getName())) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 47 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "ElementName2HtmlTagAdapter{" + "name=" + getName() + '}';
        }

        public Collection<HtmlTagAttribute> getAttributes() {
            return attrs;
        }

        //TODO implement!
        public boolean isEmpty() {
            return false;
        }

        //TODO implement!
        public boolean hasOptionalOpenTag() {
            return false;
        }

        //TODO implement!
        public boolean hasOptionalEndTag() {
            return false;
        }

    }

    private static class SimpleHtmlTagAttribute implements HtmlTagAttribute {

        private String name;

        public SimpleHtmlTagAttribute(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public boolean isRequired() {
            return false;
        }

        public HtmlTagAttributeType getType() {
            return HtmlTagAttributeType.GENERIC;
        }

        public Collection<String> getPossibleValues() {
            return Collections.emptyList();
        }

    }

}
