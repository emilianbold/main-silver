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
package org.netbeans.modules.css.model.api;

import java.io.IOException;
import java.util.Collection;
import javax.swing.text.BadLocationException;
import org.netbeans.api.diff.Difference;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.css.lib.TestUtil;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.model.ModelTestBase;
import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class ModelTest extends ModelTestBase {

    public ModelTest(String name) {
        super(name);
    }

    public void testAddElements() throws BadLocationException, ParseException, IOException, InterruptedException {
        String code = "div {\n"
                + "\tcolor: red; /* comment */\n"
                + "\tpadding: 10px;\n"
                + "}";

        CssParserResult result = TestUtil.parse(code);

        Snapshot snapshot = result.getSnapshot();
        Node root = result.getParseTree();

        Model model = new Model(snapshot, NodeUtil.query(root, "styleSheet"));
        ElementFactory factory = model.getElementFactory();
        StyleSheet styleSheet = model.getStyleSheet();

        Expression expression = factory.createExpression("20px");
        PropertyValue pv = factory.createPropertyValue(expression);
        Property property = factory.createProperty("margin");
        Declaration declaration = factory.createDeclaration(property, pv, false);
        Declarations declarations = factory.createDeclarations(declaration);
        Selector selector = factory.createSelector("h1");
        SelectorsGroup sgroup = factory.createSelectorsGroup(selector);
        Rule rule = factory.createRule(sgroup, declarations);

        styleSheet.getBody().addRule(rule);

        Difference[] diffs = model.getModelSourceDiff();
        assertEquals(1, diffs.length);

        Difference diff = diffs[0];
        assertEquals(Difference.ADD, diff.getType());

        assertEquals("h1 {\n"
                + "\n\tmargin: 20px;\n"
                + "\n"
                + "}\n", diff.getSecondText());

    }

    public void testModify() throws BadLocationException, ParseException, IOException, InterruptedException {
        String code = "/* comment */\n"
                + " div { \n"
                + "     color: red; /* my color */\n"
                + "     color: green /* c2 */;\n"
                + " } \n"
                + "a { padding: 2px }\n";

        CssParserResult result = TestUtil.parse(code);
        Model model = new Model(code, NodeUtil.query(result.getParseTree(), "styleSheet"));
        ElementFactory factory = model.getElementFactory();
        StyleSheet styleSheet = model.getStyleSheet();
        assertNotNull(styleSheet);

        Rule rule = styleSheet.getBody().getRules().get(0);
        assertNotNull(rule);

        Declaration declaration = rule.getDeclarations().getDeclarations().get(1);
        assertNotNull(declaration);

        Declaration newd = factory.createDeclaration(
                factory.createProperty("margin"), 
                factory.createPropertyValue(factory.createExpression("20px")), false);
        rule.getDeclarations().addDeclaration(newd);

        Difference[] diffs = model.getModelSourceDiff();
        assertEquals(1, diffs.length);

        Difference diff = diffs[0];
        assertEquals("Difference(ADD, 4, 0, 5, 6)", diff.toString());

    }

    public void testBuildModel() throws BadLocationException, ParseException {
        String code = "/* comment */\n"
                + " div { \n"
                + "     color: red; /* my color */\n"
                + "     color: green;\n"
                + " } \n"
                + "a { padding: 2px }\n";

        CssParserResult result = TestUtil.parse(code);
        Model model = new Model(code, NodeUtil.query(result.getParseTree(), "styleSheet"));
        StyleSheet styleSheet = model.getStyleSheet();
        assertNotNull(styleSheet);

        Collection<Rule> rules = styleSheet.getBody().getRules();
        assertNotNull(rules);
        assertEquals(2, rules.size());

        Rule rule = rules.iterator().next();
        Collection<Selector> selectors = rule.getSelectorsGroup().getSelectors();
        assertNotNull(selectors);
        assertEquals(1, selectors.size());

        Selector selector = selectors.iterator().next();
        assertEquals("div", selector.getContent().toString().trim());

        Collection<Declaration> declarations = rule.getDeclarations().getDeclarations();
        assertNotNull(declarations);
        assertEquals(2, declarations.size());

        Declaration declaration = declarations.iterator().next();
        assertEquals("color", declaration.getProperty().getContent());

        PropertyValue pv = declaration.getPropertyValue();
        assertNotNull(pv);
        
        Expression expression = pv.getExpression();
        assertNotNull(expression);
        assertEquals("red", expression.getContent());

    }

    public void testRunReadTask() throws BadLocationException, ParseException {
        CssParserResult result = TestUtil.parse("div { color: red }");
        Model model = new Model(result);
        model.runReadTask(new Model.ModelTask() {

            @Override
            public void run(StyleSheet styleSheet) {
                assertEquals(1, styleSheet.getBody().getRules().size());
            }
        });
        
    }
    
    public void testRunWriteTask() throws BadLocationException, ParseException {
        CssParserResult result = TestUtil.parse("div { color: red }");
        Model model = new Model(result);
        model.runWriteTask(new Model.ModelTask() {

            @Override
            public void run(StyleSheet styleSheet) {
                styleSheet.getBody().getRules().get(0).getDeclarations()
                        .getDeclarations().get(0).getProperty().setContent("background-color");
            }
        });
        
    }
    
    
}
