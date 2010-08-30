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
import nu.validator.htmlparser.impl.ElementName;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author marekfukala
 */
public class ElementNameGroupsTest extends NbTestCase {

    public ElementNameGroupsTest(String name) {
        super(name);
    }


    public void testGetElementForTreeBuilderGroup() {
        Collection<ElementName> members = ElementNames.getElementForTreeBuilderGroup(ElementName.A.group);
        assertTrue(members.contains(ElementName.A));
        assertEquals(1, members.size()); //only one member group

        members = ElementNames.getElementForTreeBuilderGroup(ElementName.B.group);
        assertTrue(members.contains(ElementName.B));
        assertTrue(members.contains(ElementName.BIG));
        assertTrue(members.contains(ElementName.CODE));
        assertFalse(members.contains(ElementName.A));
        assertFalse(members.contains(ElementName.FONT));

        members = ElementNames.getElementForTreeBuilderGroup(4343); //nonexisting group
        assertTrue(members.isEmpty());

    }

    //for generation of the list of html elements in HtmlSpecTest in html.validation module
//    public void testGenerateAttributesList() {
//        StringBuilder sb = new StringBuilder();
//        int i = 0;
//        for(ElementName en : ElementName.ELEMENT_NAMES) {
//            sb.append('"');
//            sb.append(en.name);
//            sb.append('"');
//            sb.append(',');
//            sb.append(' ');
//            if(++i % 8 == 0) {
//                sb.append('\n');
//            }
//        }
//
//        System.out.println(sb);
//    }

}