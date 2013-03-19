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
package org.netbeans.modules.css.prep.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import static org.junit.Assert.*;
import org.netbeans.modules.css.lib.CssTestBase;
import org.netbeans.modules.css.lib.TestUtil;
import org.netbeans.modules.css.lib.api.CssParserResult;

/**
 *
 * @author marekfukala
 */
public class CPModelTest extends CssTestBase {

    public CPModelTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        setScssSource();
    }

    public void testVariables() {
        String source = "#navbar {\n"
                + "  $navbar-width: 800px;\n"
                + "  width: $navbar-width;\n"
                + "  border-bottom: 2px solid $navbar-color;\n"
                + "\n"
                + "  li {\n"
                + "    float: left;\n"
                + "    width: $navbar-width/$items - 10px;\n"
                + "\n"
                + "    background-color:\n"
                + "      lighten($navbar-color, 20%);\n"
                + "    &:hover {\n"
                + "      background-color:\n"
                + "        lighten($navbar-color, 10%);\n"
                + "    }\n"
                + "  }\n"
                + ".class {\n"
                + "  @include mixin($switch, #888);\n"
                + "}"
                + "}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);

        CPModel model = CPModel.getModel(result);
        assertNotNull(model);

        Collection<String> vars = model.getVarNames();
        assertNotNull(vars);

        String[] expected = new String[]{"$navbar-color", "$items", "$switch", "$navbar-width"};

        Collection<String> expSet = Arrays.asList(expected);
        assertTrue(vars.containsAll(expSet));
        assertFalse(vars.retainAll(expSet));

    }

    public void testVariablesInMixin() {
        String source = "$var: 1;  @mixin my { $foo: $var + 1; }";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);

        CPModel model = CPModel.getModel(result);
        assertNotNull(model);

        Collection<String> vars = model.getVarNames();
        assertNotNull(vars);

        String[] expected = new String[]{"$var", "$foo"};

        Collection<String> expSet = Arrays.asList(expected);
        assertTrue(vars.containsAll(expSet));
        assertFalse(vars.retainAll(expSet));

    }

    public void testVariablesInMixinWithError_fails() {
        String source = "$var: 1;  @mixin my { $foo: $ }";
        CssParserResult result = TestUtil.parse(source);

        CPModel model = CPModel.getModel(result);
        assertNotNull(model);

        Collection<String> vars = model.getVarNames();
        assertNotNull(vars);

        //the $foo var won't get to the model's list as it is not parsed
        //xxx possible solutions: improve err. recovery or make some lexer based heuristic for the error area
//        String[] expected = new String[]{"$var","$foo"};
        String[] expected = new String[]{"$var"};

        Collection<String> expSet = Arrays.asList(expected);
        assertTrue(vars.containsAll(expSet));
        assertFalse(vars.retainAll(expSet));

    }

    public void testMixins() {
        String source = ""
                + "@mixin mymixin() {\n"
                + "    .clz {}\n"
                + "}\n"
                + "@include mymixin;\n";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);

        CPModel model = CPModel.getModel(result);
        assertNotNull(model);

        Collection<CPElement> mixins = model.getMixins();
        assertNotNull(mixins);

        assertEquals(2, mixins.size());
        Iterator<CPElement> iterator = mixins.iterator();

        CPElement m1 = iterator.next();
        assertEquals("mymixin", m1.getName());
        assertEquals(CPElementType.MIXIN_DECLARATION, m1.getType());
        
        CPElement m2 = iterator.next();
        assertEquals("mymixin", m2.getName());
        assertEquals(CPElementType.MIXIN_USAGE, m2.getType());
        

    }
    
    public void testVariablesType() {
        String source =
                "$global: 1;\n"
                + "@mixin my($arg) {\n"
                + "    $local: 2;\n"
                + "    color: $usage;\n"
                + "}\n";

        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);

//        TestUtil.dumpResult(result);
        
        CPModel model = CPModel.getModel(result);
        assertNotNull(model);

        Collection<CPElement> variables = model.getVariables();
        assertNotNull(variables);

        Iterator<CPElement> vars = variables.iterator();

//        for(Element v : variables) {
//            System.out.println(v.getName() + "; " + v.getRange() + "; " + v.getType());
//        }
        
        assertTrue(vars.hasNext());
        CPElement v = vars.next();
        assertNotNull(v);
        assertEquals("$global", v.getName().toString());
        assertEquals(CPElementType.VARIABLE_GLOBAL_DECLARATION, v.getType());

        assertTrue(vars.hasNext());
        v = vars.next();
        assertNotNull(v);
        assertEquals("$arg", v.getName().toString());
        assertEquals(CPElementType.VARIABLE_DECLARATION_MIXIN_PARAMS, v.getType());

        assertTrue(vars.hasNext());
        v = vars.next();
        assertNotNull(v);
        assertEquals("$local", v.getName().toString());
        assertEquals(CPElementType.VARIABLE_LOCAL_DECLARATION, v.getType());

        assertTrue(vars.hasNext());
        v = vars.next();
        assertNotNull(v);
        assertEquals("$usage", v.getName().toString());
        assertEquals(CPElementType.VARIABLE_USAGE, v.getType());

        assertFalse(vars.hasNext());
        
    }
}