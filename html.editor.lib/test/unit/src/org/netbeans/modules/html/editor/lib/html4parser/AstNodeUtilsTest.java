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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.lib.html4parser;

import java.util.*;
import javax.swing.text.BadLocationException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.html.editor.lib.api.*;
import org.netbeans.modules.html.editor.lib.api.elements.CloseTag;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.Named;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.ElementFilter;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.html.editor.lib.api.elements.ElementVisitor;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.editor.lib.dtd.DTD;
import org.netbeans.modules.html.editor.lib.test.TestBase;

/**
 *
 * @author mfukala@netbeans.org
 */
public class AstNodeUtilsTest extends TestBase {

    public static enum Match {

        EXACT, CONTAINS, DOES_NOT_CONTAIN, EMPTY, NOT_EMPTY;
    }

    public AstNodeUtilsTest(String testName) {
        super(testName);
    }

    
    @Override
    protected void setUp() throws Exception {
        HtmlVersionTest.setDefaultHtmlVersion(HtmlVersion.HTML41_TRANSATIONAL);
        super.setUp();
    }

    public static Test Xsuite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new AstNodeUtilsTest("testFindClosestNodeBackward"));
        return suite;
    }
    
    private OpenTag query(Node root, String path) {
        return ElementUtils.query(root, path);
    }

//    public void testFindClosestNodeBackward() throws Exception {
//        String code = "<p><a>text</a><b>xxx</b></p>";
//        //             0123456789012345678901234567
//
//        Node root = parse(code, null);
//        assertNotNull(root);
//
//        AstNode a = query(root, "p/a");
//        assertNotNull(a);
//        AstNode b = query(root, "p/b");
//        assertNotNull(b);
//        AstNode p = query(root, "p");
//        assertNotNull(p);
//
//        ElementFilter filter = new ElementFilter() {
//            @Override
//            public boolean accepts(Element node) {
//                return node.type() == ElementType.OPEN_TAG;
//            }
//        };
//
//        assertEquals(a, AstNodeUtils.getClosestNodeBackward(root, 7, filter));
//        assertEquals(a, AstNodeUtils.getClosestNodeBackward(root, 5, filter));
//        assertEquals(a, AstNodeUtils.getClosestNodeBackward(root, 4, filter));
//        assertEquals(a, AstNodeUtils.getClosestNodeBackward(root, 14, filter));
//
//        assertEquals(b, AstNodeUtils.getClosestNodeBackward(root, 15, filter));
//        assertEquals(b, AstNodeUtils.getClosestNodeBackward(root, 17, filter));
//        assertEquals(b, AstNodeUtils.getClosestNodeBackward(root, 19, filter));
//        assertEquals(b, AstNodeUtils.getClosestNodeBackward(root, 20, filter));
//        assertEquals(b, AstNodeUtils.getClosestNodeBackward(root, 26, filter));
//
//        assertEquals(p, AstNodeUtils.getClosestNodeBackward(root, 3, filter));
//        assertEquals(p, AstNodeUtils.getClosestNodeBackward(root, 1, filter));
//        assertEquals(root, AstNodeUtils.getClosestNodeBackward(root, 0, filter));
//
//
//    }

    public void testFindDescendant() throws Exception {
        String code = "<p><a>text</a></p>";
        //             0123456789012345678

        Node root = parse(code, null);
        assertNotNull(root);

        assertDescendant(root, 0, "p", ElementType.OPEN_TAG, 0, 18);
        assertDescendant(root, 4, "a", ElementType.OPEN_TAG, 3, 14);

        Element node = assertDescendant(root, 12, "a", ElementType.OPEN_TAG, 3, 14);
        
//        AstNode adjusted = AstNodeUtils.getTagNode(node, 12);
//
//        assertNotNull(adjusted);
//        assertEquals(10, adjusted.startOffset());
//        assertEquals(14, adjusted.endOffset());
//        assertEquals(ElementType.CLOSE_TAG, adjusted.type());
//
//        assertDescendant(root, 17, "p", ElementType.OPEN_TAG, 0, 18);

    }

    public void testFindDescendantInASTWithVirtualNodes() throws Exception {
        String code = "<!doctype html><p><a>text</a></p>";
        //             0123456789012345678901234567890123
        //             0         1         2         3


        Node root = parse(code, null);
        assertNotNull(root);

//        AstAstNodeUtils.dumpTree(root);

        assertDescendant(root, 15, "p", ElementType.OPEN_TAG, 15, 33);
        assertDescendant(root, 18, "a", ElementType.OPEN_TAG, 18, 29);
    }

    public void testQuery() throws Exception {
        String code = "<html><body><table><tr></tr><tr><td></tr></body></html>";
        //             0123456789012345678

        Node root = parse(code, null);
        assertNotNull(root);

        OpenTag node = query(root, "html");
        assertNotNull(node);
        assertEquals("html", node.name());

        node = query(root, "html/body");
        assertNotNull(node);
        assertEquals("body", node.name());

        node = query(root, "html/body/table");
        assertNotNull(node);
        assertEquals("table", node.name());

        node = query(root, "html/body/table/tr");
        assertNotNull(node);
        assertEquals("tr", node.name());

        node = query(root, "html/body/table/tr|1");
        assertNotNull(node);
        assertEquals("tr", node.name());

        node = query(root, "html/body/table/tr|1/td");
        assertNotNull(node);
        assertEquals("td", node.name());
    }

     public void testNodeVisitors() throws Exception {
        String code = "<html><body><table><tr></tr><tr><td></tr></body></html>";
        //             0123456789012345678

        Node root = parse(code, null);
        assertNotNull(root);

//        AstAstNodeUtils.dumpTree(root);

        final int nodes[] = new int[1];
        ElementUtils.visitChildren(root, new ElementVisitor() {
            @Override
            public void visit(Element node) {
                nodes[0]++;
            }
        });

        assertEquals(10, nodes[0]);

    }

    public void testGetPossibleOpenTagElements() throws BadLocationException, ParseException {
        String code = "<html><head><title></title></head><body>...<p>...</body></html>";
        //             0123456789012345678901234567890123456789012345678901234
        //             0         1         2         3         4         5

        AstNode root = (AstNode)parse(code, null);
        assertNotNull(root);

//        AstAstNodeUtils.dumpTree(root);

        //root node allows all dtd elements
        assertPossibleElements(root, 0, arr("a", "abbr", "html"), Match.CONTAINS);

        //inside html tag - nothing is offered inside the tag itself
        assertPossibleElements(root, 1, arr(), Match.EMPTY);

        //just after html tag
        assertPossibleElements(root, 6, arr("head"), Match.CONTAINS);

        //at the beginning of head tag
        assertPossibleElements(root, 12, arr("title", "meta"), Match.CONTAINS);
        //after title in head tag
        assertPossibleElements(root, 27, arr("meta"), Match.CONTAINS);
//        assertPossibleElements(root, 27, arr("title"), Match.DOES_NOT_CONTAIN);

        //just before body
        assertPossibleElements(root, 34, arr("body"), Match.CONTAINS);
        //inside body
        assertPossibleElements(root, 41, arr("p", "div"), Match.CONTAINS);

        //p can contain another p - will close the previous one with opt. end
        assertPossibleElements(root, 47, arr("p"), Match.CONTAINS);


    }

    public void testIssue169206() throws BadLocationException, ParseException {
        String code = "<html><head><title></title></head><body><table> </table></body></html>";
        //             0123456789012345678901234567890123456789012345678901234
        //             0         1         2         3         4         5

        AstNode root = (AstNode)parse(code, null);
        assertNotNull(root);

        assertPossibleElements(root, 47, arr("thead","tbody","tr"), Match.CONTAINS);


    }

    public void testIssue185837() throws BadLocationException, ParseException {
        String code = "<html><head><title></title></head><body><b><del>xxx</del></b></body></html>";
        //             0123456789012345678901234567890123456789012345678901234
        //             0         1         2         3         4         5

        AstNode root = (AstNode)parse(code, null);
        assertNotNull(root);

//        AstAstNodeUtils.dumpTree(root);

        //root node allows all dtd elements
        assertPossibleElements(root, 40, arr("del", "ins"), Match.CONTAINS);
        assertPossibleElements(root, 43, arr("del", "ins"), Match.CONTAINS);

    }

    public void testFindNodeVirtualNodes() throws BadLocationException, ParseException {
        String code = "<!doctype html><title>hi</title><div>buddy</div>";
        //             0123456789012345678901234567890123456789012345678901234
        //             0         1         2         3         4         5

        Node root = parse(code, null);
        assertNotNull(root);

//        AstAstNodeUtils.dumpTree(root);

        OpenTag title = query(root, "html/head/title");
        assertNotNull(title);

        //non logical range
        assertSame(title, ElementUtils.findNode(root, 17, true, false)); //middle
        assertSame(title, ElementUtils.findNode(root, 15, true, false)); //fw
        assertSame(title, ElementUtils.findNode(root, 22, false, false)); //bw

        //logical range
        assertSame(title, ElementUtils.findNode(root, 23, false, false));

        OpenTag div = query(root, "html/body/div");
        assertNotNull(div);
        //non logical range
        assertSame(div, ElementUtils.findNode(root, 35, true, false)); //middle
        assertSame(div, ElementUtils.findNode(root, 32, true, false)); //fw
        assertSame(div, ElementUtils.findNode(root, 37, false, false)); //bw

        //logical range
        assertSame(div, ElementUtils.findNode(root, 40, false, false));

    }

    public void testFindNodeByPhysicalRange() throws BadLocationException, ParseException {
        String code = "<html>  <body> nazdar </body> <div></html>";
        //             0123456789012345678901234567890123456789012345678901234
        //             0         1         2         3         4         5
        Node root = parse(code, null);
        assertNotNull(root);

//        AstAstNodeUtils.dumpTree(root);

        OpenTag html = query(root, "html");
        assertNotNull(html);
        CloseTag htmlEnd = html.matchingCloseTag();
        assertNotNull(htmlEnd);
        OpenTag body = query(root, "html/body");
        assertNotNull(body);
        CloseTag bodyEnd = body.matchingCloseTag();
        assertNotNull(bodyEnd);
        OpenTag div = query(root, "html/div");
        assertNotNull(div);

        assertNull(ElementUtils.findNode(root,7, true, true));

        //html open tag
        assertNull(ElementUtils.findNode(root,6, true, true)); //behind, look forward
        assertEquals(html, ElementUtils.findNode(root,6, false, true)); //behind, bw

        assertEquals(html, ElementUtils.findNode(root,0, true, true)); //before, fw
        assertNull(ElementUtils.findNode(root,0, false, true)); //before, look backward

        assertEquals(html, ElementUtils.findNode(root,3, true, true)); //middle, fw
        assertEquals(html, ElementUtils.findNode(root,3, false, true)); //middle, bw

        //body open tag
        assertNull(ElementUtils.findNode(root,14, true, true)); //behind, look forward
        assertEquals(body, ElementUtils.findNode(root,14, false, true)); //behind, bw

        assertEquals(body, ElementUtils.findNode(root,8, true, true)); //before, fw
        assertNull(ElementUtils.findNode(root,8, false, true)); //before, look backward

        assertEquals(body, ElementUtils.findNode(root,10, true, true)); //middle, fw
        assertEquals(body, ElementUtils.findNode(root,10, false, true)); //middle, bw

        //body end tag
        assertNull(ElementUtils.findNode(root,29, true, true)); //behind, look forward
        assertEquals(bodyEnd, ElementUtils.findNode(root,29, false, true)); //behind, bw

        assertEquals(bodyEnd, ElementUtils.findNode(root,22, true, true)); //before, fw
        assertNull(ElementUtils.findNode(root,22, false, true)); //before, look backward

        assertEquals(bodyEnd, ElementUtils.findNode(root,25, true, true)); //middle, fw
        assertEquals(bodyEnd, ElementUtils.findNode(root,25, false, true)); //middle, bw

        //div open tag
        assertNotNull(ElementUtils.findNode(root,35, true, true)); //behind, look forward //</html>
        assertEquals(div, ElementUtils.findNode(root,35, false, true)); //behind, bw

        assertEquals(div, ElementUtils.findNode(root,30, true, true)); //before, fw
        assertNull(ElementUtils.findNode(root,30, false, true)); //before, look backward

        assertEquals(div, ElementUtils.findNode(root,32, true, true)); //middle, fw
        assertEquals(div, ElementUtils.findNode(root,32, false, true)); //middle, bw

        //html end tag
        assertNull(ElementUtils.findNode(root,42, true, true)); //behind, look forward
        assertEquals(htmlEnd, ElementUtils.findNode(root,42, false, true)); //behind, bw

        assertEquals(htmlEnd, ElementUtils.findNode(root,35, true, true)); //before, fw
        assertNotNull(ElementUtils.findNode(root,35, false, true)); //before, look backward //<div>

        assertEquals(htmlEnd, ElementUtils.findNode(root,40, true, true)); //middle, fw
        assertEquals(htmlEnd, ElementUtils.findNode(root,40, false, true)); //middle, bw

        //out of content
        assertNull(ElementUtils.findNode(root,100, true, true));
        assertNull(ElementUtils.findNode(root,100, false, true));


    }

    private Element assertDescendant(Node searchedNode, int searchOffset, String name, ElementType type, int from, int to) {
        return assertDescendant(searchedNode, searchOffset, true, name, type, from, to);
    }
    private Element assertDescendant(Node searchedNode, int searchOffset, boolean forward, String name, ElementType type, int from, int to) {
        Element node = ElementUtils.findNode(searchedNode, searchOffset, forward, false);
        assertNotNull(node);
        assertEquals(type, node.type());
        
        
        if(node.type() == ElementType.OPEN_TAG || node.type() == ElementType.CLOSE_TAG) {
            Named named = (Named)node;
            assertEquals(name, named.name().toString());
        }
        
        if(node.type() == ElementType.OPEN_TAG) {
            OpenTag ot = (OpenTag)node;
            assertEquals(from, ot.from());
            assertEquals(to, ot.semanticEnd());
        }
        

        return node;
    }

    private Node parse(String code, String publicId) throws BadLocationException, ParseException {
        HtmlSource source = new HtmlSource(code);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();
        return result.parseHtml().root();
    }

    private void assertPossibleElements(AstNode rootNode, int offset, String[] expected, Match type) {
        Collection<DTD.Element> possible = AstNodeUtils.getPossibleOpenTagElements(rootNode, offset);
        assertNotNull(possible); 
        assertDTDElements(expected, possible, type);
    }

    private void assertDTDElements(String[] expected, Collection<DTD.Element> elements, Match type) {
        List<String> real = new ArrayList<String>();
        for (DTD.Element ccp : elements) {
            real.add(ccp.getName().toLowerCase());
        }
        List<String> exp = new ArrayList<String>(Arrays.asList(expected));

        if (type == Match.EXACT) {
            assertEquals(exp, real);
        } else if (type == Match.CONTAINS) {
            exp.removeAll(real);
            assertEquals(exp, Collections.EMPTY_LIST);
        } else if (type == Match.EMPTY) {
            assertEquals(0, real.size());
        } else if (type == Match.NOT_EMPTY) {
            assertTrue(real.size() > 0);
        } else if (type == Match.DOES_NOT_CONTAIN) {
            int originalRealSize = real.size();
            real.removeAll(exp);
            assertEquals(originalRealSize, real.size());
        }
    }

    private String[] arr(String... args) {
        return args;
    }
}
