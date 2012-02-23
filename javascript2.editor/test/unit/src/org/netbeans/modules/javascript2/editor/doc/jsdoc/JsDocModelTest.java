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
package org.netbeans.modules.javascript2.editor.doc.jsdoc;

import java.util.*;
import org.netbeans.modules.javascript2.editor.doc.jsdoc.model.*;
import org.netbeans.modules.javascript2.editor.model.JsComment;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocModelTest extends JsDocTestBase {

    public JsDocModelTest(String testName) {
        super(testName);
    }

    /**
     * The string should look like: <type>||<key1>=<value1>:<key2>=<value2>;<type>
     */
    private static List<JsDocElement> parseExpected(String expected) {
        List<JsDocElement> elements = new ArrayList<JsDocElement>();
        String[] tags = expected.split("[;]+");
        for (String tag : tags) {
            String[] tmp = tag.split("[|][|]");
            FakeJsDocElement element = new FakeJsDocElement(JsDocElement.Type.fromString(tmp[0]));
            if (tmp.length > 1) {
                String[] keyValues = tmp[1].split("[:]+");
                for (String keyValue : keyValues) {
                    String[] items = keyValue.split("[=]+");
                    if (items.length == 1) {
                        // in context sensitive cases
                        element.addProperty("desc", items[0]);
                    } else {
                        element.addProperty(items[0], items[1]);
                    }
                }
            }
            elements.add(element);
        }
        return elements;
    }

    private static void checkJsDocElements(String expected, List<JsDocElement> elements) {
        List<JsDocElement> expectedTags = parseExpected(expected);
        assertElementsEquality(expectedTags, elements);

    }

    private static void checkJsDocBlock(Source source, final int offset, final String expected) throws Exception {
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                assertTrue(result instanceof JsParserResult);
                JsParserResult parserResult = (JsParserResult) result;

                JsDocDocumentationProvider documentationProvider = getDocumentationProvider(parserResult);
                JsComment comment = documentationProvider.getCommentForOffset(offset);
                assertTrue(comment instanceof JsDocBlock);

                JsDocBlock jsDoc = (JsDocBlock) comment;
                checkJsDocElements(expected, jsDoc.getTags());
            }
        });
    }

    public void testContextSensitiveDescription() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line1;");
        checkJsDocBlock(source, caretOffset, "contextSensitive||This could be description");
    }

    public void testArgument() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line2;");
        checkJsDocBlock(source, caretOffset, "@argument||type=paramType:name=paramName:desc=paramDescription");
    }

    public void testAugments() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line3;");
        checkJsDocBlock(source, caretOffset, "@augments||type=otherClass");
    }

    public void testAuthor() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line4;");
        checkJsDocBlock(source, caretOffset, "@author||desc=Jackie Chan");
    }

    public void testBorrows() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line5;");
        checkJsDocBlock(source, caretOffset, "@borrows||param1=otherMemberName:param2=thisMemberName");
    }

    public void testClass() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line6;");
        checkJsDocBlock(source, caretOffset, "@class||desc=description");
    }

    public void testConstant() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line7;");
        checkJsDocBlock(source, caretOffset, "@constant");
    }

    public void testConstructor() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line8;");
        checkJsDocBlock(source, caretOffset, "@constructor");
    }

    public void testConstructs() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line9;");
        checkJsDocBlock(source, caretOffset, "@constructs");
    }

    public void testDefault() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line10;");
        checkJsDocBlock(source, caretOffset, "@default||desc=valueDescription");
    }

    public void testDeprecated() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line11;");
        checkJsDocBlock(source, caretOffset, "@deprecated||desc=deprecatedDescription");
    }

    public void testDescription() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line12;");
        checkJsDocBlock(source, caretOffset, "@description||desc=description");
    }

    public void testEvent() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line13;");
        checkJsDocBlock(source, caretOffset, "@event");
    }

    public void testExample() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line14;");
        checkJsDocBlock(source, caretOffset, "@example||desc=var bleeper");
    }

    public void testExtends() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line15;");
        checkJsDocBlock(source, caretOffset, "@extends||type=otherClass");
    }

    public void testField() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line16;");
        checkJsDocBlock(source, caretOffset, "@field");
    }

    public void testFileOverview() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line17;");
        checkJsDocBlock(source, caretOffset, "@fileOverview||desc=fileDescription");
    }

    public void testFunction() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line18;");
        checkJsDocBlock(source, caretOffset, "@function");
    }

    public void testIgnore() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line19;");
        checkJsDocBlock(source, caretOffset, "@ignore");
    }

    public void testInner() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line20;");
        checkJsDocBlock(source, caretOffset, "@inner");
    }

    public void testLends() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line21;");
        checkJsDocBlock(source, caretOffset, "@lends||namepath=symbolAlias");
    }

    public void testMemberOf() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line22;");
        checkJsDocBlock(source, caretOffset, "@memberOf||namepath=parentNamepath");
    }

    public void testName() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line23;");
        checkJsDocBlock(source, caretOffset, "@name||namepath=theNamepath");
    }

    public void testNamespace() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line24;");
        checkJsDocBlock(source, caretOffset, "@namespace||desc=description");
    }

    public void testParam() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line25;");
        checkJsDocBlock(source, caretOffset, "@param||type=paramType:name=paramName:desc=paramDescription");
    }

    public void testPrivate() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line26;");
        checkJsDocBlock(source, caretOffset, "@private");
    }

    public void testProperty() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line27;");
        checkJsDocBlock(source, caretOffset, "@property||type=propertyType:name=propertyName:desc=propertyDescription");
    }

    public void testPublic() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line28;");
        checkJsDocBlock(source, caretOffset, "@public");
    }

    public void testRequires() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line29;");
        checkJsDocBlock(source, caretOffset, "@requires||desc=requireDescription");
    }

    public void testReturn() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line30;");
        checkJsDocBlock(source, caretOffset, "@return||type=returnType:desc=returnDescription");
    }

    public void testReturns() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line31;");
        checkJsDocBlock(source, caretOffset, "@returns||type=returnType:desc=returnDescription");
    }

    public void testSee() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line32;");
        checkJsDocBlock(source, caretOffset, "@see||desc=seeDescription");
    }

    public void testSince() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line33;");
        checkJsDocBlock(source, caretOffset, "@since||desc=versionDescription");
    }

    public void testStatic() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line34;");
        checkJsDocBlock(source, caretOffset, "@static");
    }

    public void testThrows() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line35;");
        checkJsDocBlock(source, caretOffset, "@throws||type=exceptionType:desc=exceptionDescription");
    }

    public void testType() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line36;");
        checkJsDocBlock(source, caretOffset, "@type||type=typeName");
    }

    public void testVersion() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line37;");
        checkJsDocBlock(source, caretOffset, "@version||desc=versionDescription");
    }

     public void testParameterWithoutDesc() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/parameterTypes.js"));
        final int caretOffset = getCaretOffset(source, "function ^line1(userName){}");
        checkJsDocBlock(source, caretOffset, "@param||type=String:name=userName");
    }

     public void testParameterWithMoreTypes() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/parameterTypes.js"));
        final int caretOffset = getCaretOffset(source, "function ^line2(product){}");
        checkJsDocBlock(source, caretOffset, "@param||type=String|Number:name=product");
    }

     public void testParameterOptional() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/parameterTypes.js"));
        final int caretOffset = getCaretOffset(source, "function ^line3(accessLevel){}");
        checkJsDocBlock(source, caretOffset, "@param||type=String:name=accessLevel:desc=accessLevel is optional");
    }

     public void testParameterOptionalWithDefault() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/parameterTypes.js"));
        final int caretOffset = getCaretOffset(source, "function ^line4(accessLevel){}");
        checkJsDocBlock(source, caretOffset, "@param||type=String:name=accessLevel:desc=accessLevel is optional");
    }

    /**
     * Examples of expected values:
     * @borrows <param1> as <param2>
     * @type <type>
     * @author <desc>
     * @memberOf <namepath>
     * @param <type> <name> <desc>
     * @private
     * @throws <type> <desc>
     */
    private static void assertElementEquality(FakeJsDocElement expected, JsDocElement parsed) {
        switch (parsed.getType().getCategory()) {
            case ASSIGN:
                assertTrue(parsed instanceof AssignElement);
                AssignElement assignElement = (AssignElement) parsed;
                assertEquals(expected.getProperty("param1"), assignElement.getOtherMemberName().toString());
                assertEquals(expected.getProperty("param2"), assignElement.getThisMemberName().toString());
                break;
            case DECLARATION:
                assertTrue(parsed instanceof DeclarationElement);
                DeclarationElement declarationElement = (DeclarationElement) parsed;
                assertEquals(expected.getProperty("type"), declarationElement.getDeclaredType().toString());
                break;
            case DESCRIPTION:
                assertTrue(parsed instanceof DescriptionElement);
                DescriptionElement descElement = (DescriptionElement) parsed;
                assertEquals(expected.getProperty("desc"), descElement.getDescription().toString());
                break;
            case LINK:
                assertTrue(parsed instanceof LinkElement);
                LinkElement linkElement = (LinkElement) parsed;
                assertEquals(expected.getProperty("namepath"), linkElement.getLinkedPath().toString());
                break;
            case NAMED_PARAMETER:
                assertTrue(parsed instanceof NamedParameterElement);
                NamedParameterElement namedParameterElement = (NamedParameterElement) parsed;
                assertEquals(expected.getProperty("name"), namedParameterElement.getParamName().toString());
                assertEquals(expected.getProperty("desc"), namedParameterElement.getParamDescription().toString());
                assertEquals(expected.getProperty("type"), namedParameterElement.getParamTypes().toString());
                break;
            case SIMPLE:
                assertTrue(parsed instanceof SimpleElement);
                break;
            case UNNAMED_PARAMETER:
                assertTrue(parsed instanceof UnnamedParameterElement);
                UnnamedParameterElement unnamedParameterElement = (UnnamedParameterElement) parsed;
                assertEquals(expected.getProperty("desc"), unnamedParameterElement.getParamDescription().toString());
                assertEquals(expected.getProperty("type"), unnamedParameterElement.getParamTypes().toString());
                break;
            default:
                throw new AssertionError();
        }
    }

    private static void assertElementsEquality(List<JsDocElement> expectedTags, List<JsDocElement> elements) {
        Collections.sort(expectedTags, new JsDocElementComparator());
        Collections.sort(elements, new JsDocElementComparator());

        assertEquals(expectedTags.size(), elements.size());

        for (int i = 0; i < expectedTags.size(); i++) {
            JsDocElement expected = expectedTags.get(i);
            JsDocElement parsed = elements.get(i);
            assertElementEquality((FakeJsDocElement) expected, parsed);
        }
    }

    private static class JsDocElementComparator implements Comparator<JsDocElement> {

        @Override
        public int compare(JsDocElement o1, JsDocElement o2) {
            return o1.getType().toString().compareTo(o2.getType().toString());
        }

    }

    private static class FakeJsDocElement implements JsDocElement {

        private final Type type;
        private Map<String, String> properties = new HashMap<String, String>();

        public FakeJsDocElement(Type type) {
            assertNotNull(type);
            this.type = type;
        }

        @Override
        public Type getType() {
            return type;
        }

        public void addProperty(String key, String value) {
            assertNotNull(key);
            assertNotNull(value);
            properties.put(key, value);
        }

        public String getProperty(String key) {
            String property = properties.get(key);
            return property == null ? "" : property;
        }
    }
}
