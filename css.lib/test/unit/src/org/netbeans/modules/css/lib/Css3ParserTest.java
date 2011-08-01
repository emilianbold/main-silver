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
package org.netbeans.modules.css.lib;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.BadLocationException;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class Css3ParserTest extends CslTestBase {

    public Css3ParserTest(String testName) {
        super(testName);
    }
    
//     public static Test suite() throws IOException, BadLocationException {
//        System.err.println("Beware, only selected tests runs!!!");
//        TestSuite suite = new TestSuite();
//        suite.addTest(new Css3ParserTest("testErrors"));
//        return suite;
//    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        CssParserResult.IN_UNIT_TESTS = true;
    }
    
    public void testErrorRecoveryInRule() throws ParseException, BadLocationException {
        //resync the parser to the last right curly bracket
        String code = "myns|h1  color: red; } h2 { color: blue; }";
        
        CssParserResult res = TestUtil.parse(code);
//        dumpResult(res);
        
        //this case recovers badly so far - the myns|h1 and h2 are joined into a single ruleset
    }

    public void testErrorRecoveryInsideDeclaration() throws ParseException, BadLocationException {
        //recovery inside declaration rule, resyncing to next semicolon or right curly brace
        String code = "a {\n"
                        + " s  red; \n"
                        + " background: red; \n"
                      + "}";
        
        CssParserResult res = TestUtil.parse(code);
        
        assertResult(res, 1);
        
        //the background: red; declaration is properly parsed even if the previous declaration is broken
        assertNotNull(NodeUtil.query(res.getParseTree(), 
                TestUtil.bodysetPath + "ruleSet/declarations/declaration|1/property/background"));
        
//        dumpResult(res);
    }
    
    public void testErrorRecoveryGargabeBeforeDeclaration() throws ParseException, BadLocationException {
        //recovery before entering declaration rule, the Parser.syncToIdent() is used to skip until ident is found
        
        String code = "a {\n"
                        + " @ color: red; \n"
                        + " background: red; \n"
                      + "}";
        
        CssParserResult res = TestUtil.parse(code);
//        TestUtil.dumpResult(res);
        
        assertResult(res, 1); 
        
        //the garbage char @ is skipped by Parser.syncToIdent()
        assertNotNull(NodeUtil.query(res.getParseTree(), 
                TestUtil.bodysetPath + "ruleSet/declarations/declaration|0/property/color"));
        assertNotNull(NodeUtil.query(res.getParseTree(), 
                TestUtil.bodysetPath + "ruleSet/declarations/declaration|1/property/background"));
        
    }
    
    public void testSimpleValidCode() throws ParseException, BadLocationException {
        String code = "a {"
                        + "color : black;"
                      + "}";
        
        CssParserResult res = TestUtil.parse(code);
//        TestUtil.dumpResult(res);
        assertResult(res, 0);
        
        assertNotNull(NodeUtil.query(res.getParseTree(), 
                TestUtil.bodysetPath + "ruleSet/declarations/declaration|0/property/color"));
        
    }
    
    public void testValidCode() throws ParseException, BadLocationException {
        String code = "a {\n"
                        + "color : black; \n"
                        + "background: red; \n"
                      + "}\n\n"
                +      ".class { }\n"
                +      "#id { }";
        
        CssParserResult res = TestUtil.parse(code);
        assertResult(res, 0);
        
        assertNotNull(NodeUtil.query(res.getParseTree(), 
                TestUtil.bodysetPath + "ruleSet/declarations/declaration|0/property/color"));
        assertNotNull(NodeUtil.query(res.getParseTree(), 
                TestUtil.bodysetPath + "ruleSet/declarations/declaration|1/property/background"));
        
    }
    
    public void testParseTreeOffsets() throws ParseException, BadLocationException {
        String code = "/* comment */ body { color: red; }";
        //             01234567890123456789
        //             0         1
        
        CssParserResult res = TestUtil.parse(code);
//        TestUtil.dumpResult(res);
        assertResult(res, 0);
        
        Node aNode = NodeUtil.query(res.getParseTree(), 
                TestUtil.bodysetPath + "ruleSet/selectorsGroup/selector/simpleSelectorSequence/typeSelector/elementName/body");
        
        assertNotNull(aNode);
        assertTrue(aNode instanceof TokenNode);
        
        assertEquals("body", aNode.name());
        assertEquals(NodeType.token, aNode.type());
        
        assertEquals("body".length(), aNode.name().length());
        assertEquals(14, aNode.from());
        assertEquals(18, aNode.to());
    }
   
    public void testNamespacesInSelector() throws ParseException, BadLocationException {
        CssParserResult res = assertResultOK(TestUtil.parse("myns|h1 { color: red; }"));
        //dumpResult(res);
        
        String typeSelectorPath = "ruleSet/selectorsGroup/selector/simpleSelectorSequence/typeSelector/";
        
        assertNotNull(NodeUtil.query(res.getParseTree(), 
                TestUtil.bodysetPath + typeSelectorPath + "namespacePrefix/namespaceName/myns"));
        assertNotNull(NodeUtil.query(res.getParseTree(), 
                TestUtil.bodysetPath + typeSelectorPath + "elementName/h1"));        
        
        res = assertResultOK(TestUtil.parse("*|h1 { color: red; }"));
        //dumpResult(res);
        
        assertNotNull(NodeUtil.query(res.getParseTree(), 
                TestUtil.bodysetPath + typeSelectorPath + "namespacePrefix/namespaceName/*"));
        assertNotNull(NodeUtil.query(res.getParseTree(), 
                TestUtil.bodysetPath + typeSelectorPath + "elementName/h1"));
        
        res = assertResultOK(TestUtil.parse("*|* { color: red; }"));
        //dumpResult(res);
        
        assertNotNull(NodeUtil.query(res.getParseTree(), 
                TestUtil.bodysetPath + typeSelectorPath + "namespacePrefix/namespaceName/*"));
        assertNotNull(NodeUtil.query(res.getParseTree(), 
                TestUtil.bodysetPath + typeSelectorPath + "elementName/*"));
    }
    
    public void testNodeImages() throws ParseException, BadLocationException {
        String selectors = "#id .class body ";
        String code = selectors + "{ color: red}";
        CssParserResult res = TestUtil.parse(code);
//        dumpResult(res);
        
        String selectorsGroupPath = "ruleSet/selectorsGroup";
        
        //test rule node image
        Node selectorsGroup = NodeUtil.query(res.getParseTree(), TestUtil.bodysetPath + selectorsGroupPath); 
        assertNotNull(selectorsGroup);
        
        assertTrue(CharSequenceUtilities.equals(selectors, selectorsGroup.image()));
         
        //test root node image
        assertTrue(CharSequenceUtilities.equals(code, res.getParseTree().image()));
        
        //test token node image
        Node id = NodeUtil.query(selectorsGroup, "selector/simpleSelectorSequence/elementSubsequent/cssId/#id"); 
        assertNotNull(id);
        assertTrue(id instanceof TokenNode);
        assertTrue(CharSequenceUtilities.equals("#id", id.image()));
        
    }
    
    public void testCommon() throws ParseException, BadLocationException {
        String code = "#id .class body { color: red}     body {}";
        CssParserResult res = TestUtil.parse(code);
//        TestUtil.dumpResult(res);
        assertResult(res, 0);
    }
    
    public void testMedia() throws ParseException, BadLocationException {
        String code = "@media screen { h1 { color: red; } }";
        CssParserResult res = TestUtil.parse(code);
//        TestUtil.dumpResult(res);
        assertResult(res, 0);
    }
    
    public void testRootNodeSpan() throws ParseException, BadLocationException {
        String code = "   h1 { }    ";
        //             012345678901234
        CssParserResult res = TestUtil.parse(code);
        TestUtil.dumpResult(res);
        
        Node root = res.getParseTree();
        assertEquals(0, root.from());
        assertEquals(code.length(), root.to());
    }
        
    public void testImport() throws ParseException, BadLocationException {
        String code = "@import \"file.css\";";
        CssParserResult res = TestUtil.parse(code);
        
        TestUtil.dumpResult(res);
        Node imports = NodeUtil.query(res.getParseTree(), "styleSheet/imports"); 
        assertNotNull(imports);
        
        //url form
         code = "@import url(\"file.css\");";
        res = TestUtil.parse(code);
        
//        TestUtil.dumpResult(res);
        imports = NodeUtil.query(res.getParseTree(), "styleSheet/imports"); 
        assertNotNull(imports);
        
    }
    
    public void testErrorCase1() throws BadLocationException, ParseException, FileNotFoundException {
        String code = "h1 { color:  }";
        CssParserResult res = TestUtil.parse(code);
        
        //Test whether all the nodes are properly intialized - just dump the tree.
        //There used to be a bug that error token caused some rule
        //nodes not having first token set properly by the NbParseTreeBuilder
        NodeUtil.dumpTree(res.getParseTree(), new PrintWriter(new StringWriter()));
        
        
//        NodeUtil.dumpTree(res.getParseTree());
    }
    
    public void testErrorCase2() throws BadLocationException, ParseException, FileNotFoundException {
        String code = "a { color: red; } ";
        
        CssParserResult res = TestUtil.parse(code);
        
//        NodeUtil.dumpTree(res.getParseTree());
        
        assertResult(res, 0);
        
    }
    
    public void testErrorCase_emptyDeclarations() throws BadLocationException, ParseException, FileNotFoundException {
        String code = "h1 {}";
        
        CssParserResult res = TestUtil.parse(code);
        
        //syncToIdent bug - it cannot sync to ident since there isn't one - but the case is valid
        //=> reconsider putting syncToIdent back to the declarations rule, but then I need 
        //to resolve why it is not called even in proper cases!!!
        NodeUtil.dumpTree(res.getParseTree());
        AtomicBoolean recoveryNodeFound = new AtomicBoolean(false);
        NodeVisitor<AtomicBoolean> visitor = new NodeVisitor<AtomicBoolean>(recoveryNodeFound) {

            @Override
            public boolean visit(Node node) {
                if(node.type() == NodeType.recovery) {
                    getResult().set(true);
                    return true;
                } 
                return false;
            }
        };
        
        visitor.visitChildren(res.getParseTree());
        
        assertResult(res, 0);
        
        assertFalse(recoveryNodeFound.get());
        
        //this doesn't work actually, the resyncing to ident doesn't work naturally
        
    }
    
     //issue #160780
    public void testFatalParserError() throws ParseException, BadLocationException {
        //fatal parse error on such input
        String content = "@charset";
        
        CssParserResult result = TestUtil.parse(content);        
        assertNotNull(result.getParseTree());
        assertEquals(1, result.getDiagnostics().size());
    }
    
    public void testCharsetParsing() throws ParseException, BadLocationException {
        String content = "@charset \"iso-8859-1\";\n h1 { color: red; }";
        
        CssParserResult result = TestUtil.parse(content);        
        assertResult(result, 0);
    }
    
    
    public void testErrorCase4() throws ParseException, BadLocationException {
        String content = "h1 { color: ;}";
        
        CssParserResult result = TestUtil.parse(content);        
        assertResult(result, 1);
    }
    
    public void testIdParsing() throws ParseException, BadLocationException {
        String content = "h1 #myid { color: red }";
        
        CssParserResult result = TestUtil.parse(content);        
        assertResult(result, 0);
//        TestUtil.dumpResult(result);
        
        Node id = NodeUtil.query(result.getParseTree(), TestUtil.bodysetPath + "ruleSet/selectorsGroup/selector/simpleSelectorSequence/elementSubsequent/cssId"); 
        assertNotNull(id);
        
        assertEquals(NodeType.cssId, id.type());
        
    }
    
    public void testErrorRecoveryBetweenRuleSets() throws ParseException, BadLocationException {
        String content = "h1 { color: red} ; h2 { color: blue }";
        //                                 ^ -- semicolon not allowed here
        
        CssParserResult result = TestUtil.parse(content);        
//        TestUtil.dumpResult(result);
        
        //commented out since it currently fails
        //assertResult(result, 0); 
    }
    
    public void testErrorCase5() throws ParseException, BadLocationException {
        String content = "a { }   m { }";
        
        CssParserResult result = TestUtil.parse(content);        
//        TestUtil.dumpResult(result);
        
        assertResult(result, 0);
        
        //fails - recovery in declarations eats the closing right curly bracket
    }
    
    public void testNetbeans_Css() throws ParseException, BadLocationException, IOException {
        CssParserResult result = TestUtil.parse(getTestFile("testfiles/netbeans.css"));
//        TestUtil.dumpResult(result);
        assertResult(result, 4);
    }
    
    private CssParserResult assertResultOK(CssParserResult result) {
        return assertResult(result, 0);
    }
    
    private CssParserResult assertResult(CssParserResult result, int problems) {
        assertNotNull(result);
        assertNotNull(result.getParseTree());
        
        if(problems != result.getDiagnostics().size()) {
            TestUtil.dumpResult(result);
        }
        assertEquals(problems, result.getDiagnostics().size());
        
        return result;
    }
    
  
    
}
