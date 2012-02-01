/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.module.main;

import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyModel;
import org.netbeans.modules.css.lib.api.properties.PropertyValue;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author mfukala@netbeans.org
 */
public class ColorsModuleTest extends CslTestBase {

    public ColorsModuleTest(String name) {
        super(name);
    }

    public void testPropertyDescriptors() throws ParseException {
        PropertyModel color = Properties.getPropertyModel("color");
        assertNotNull(color);

        assertNotNull(Properties.getPropertyModel("@rgb"));
        assertNotNull(Properties.getPropertyModel("@colors-list"));
        assertNotNull(Properties.getPropertyModel("@system-color"));
    }

    public void testTextValues() {
        PropertyModel p = Properties.getPropertyModel("color");
        assertTrue(new PropertyValue(p, "red").isResolved());
        assertTrue(new PropertyValue(p, "buttonface").isResolved());
    }

    public void testRGBValues() {
        PropertyModel p = Properties.getPropertyModel("color");
        assertTrue(new PropertyValue(p, "rgb(10,20,30)").isResolved());
        assertTrue(new PropertyValue(p, "rgb(10%,20,30)").isResolved());
        assertFalse(new PropertyValue(p, "rgb(,20,30)").isResolved());
        assertFalse(new PropertyValue(p, "rgb(10,x,30)").isResolved());

    }

    public void testHashValues() {
        PropertyModel p = Properties.getPropertyModel("color");
        assertTrue(new PropertyValue(p, "#ffaa00").isResolved());
        assertTrue(new PropertyValue(p, "#fb0").isResolved());
        assertFalse(new PropertyValue(p, "#fa001").isResolved());
    }

    public void testRGBaValues() {
        PropertyModel p = Properties.getPropertyModel("color");
        assertTrue(new PropertyValue(p, "rgba(255,0,0,1)").isResolved());
        assertTrue(new PropertyValue(p, "rgba(100%,0%,0%,1)").isResolved());
        assertTrue(new PropertyValue(p, "rgba(0,0,255,0.5)").isResolved());
    }
    

    public void testHSLValues() {
        PropertyModel p = Properties.getPropertyModel("color");
        assertTrue(new PropertyValue(p, "hsl(0, 100%, 50%)").isResolved());
        assertTrue(new PropertyValue(p, "hsl(120, 100%, 50%)").isResolved());
        assertTrue(new PropertyValue(p, "hsl(120, 100%, 25%)").isResolved());
        assertTrue(new PropertyValue(p, "hsl(120, 100%, 75%)").isResolved());
        assertTrue(new PropertyValue(p, "hsl(120, 75%, 75%)").isResolved());
        assertTrue(new PropertyValue(p, "hsl(120, 100%, 50%)").isResolved());
    }
    
    public void testHSLaValues() {
        PropertyModel p = Properties.getPropertyModel("color");
        assertTrue(new PropertyValue(p, "hsla(120, 100%, 50%, 1)").isResolved());
        assertTrue(new PropertyValue(p, "hsla(240, 100%, 50%, 0.5)").isResolved());
        assertTrue(new PropertyValue(p, "hsla(30, 100%, 50%, 0.1)").isResolved());
    }
    
    public void testSpecialValues() {
        PropertyModel p = Properties.getPropertyModel("color");
//        assertTrue(new PropertyValue(p, "inherit").success());
        assertTrue(new PropertyValue(p, "currentColor").isResolved());
        assertTrue(new PropertyValue(p, "transparent").isResolved());
    }
    
    
}
