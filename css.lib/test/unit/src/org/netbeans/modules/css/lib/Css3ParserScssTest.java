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

import org.netbeans.modules.css.lib.api.*;

/**
 *
 * @author marekfukala
 */
public class Css3ParserScssTest extends CssTestBase {

    public Css3ParserScssTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setScssSource();
    }

    @Override
    protected void tearDown() throws Exception {
        setPlainSource();
    }

    public void testAllANTLRRulesHaveNodeTypes() {
        for (String rule : Css3Parser.ruleNames) {
            if (!rule.startsWith("synpred") && !rule.toLowerCase().endsWith("predicate")) {
                assertNotNull(NodeType.valueOf(rule));
            }
        }
    }

    public void testDisabledScssSupport() {
        try {
            ExtCss3Parser.isScssSource_unit_tests = false;
            String source = "$color: #4D926F;\n"
                    + "\n"
                    + "#header {\n"
                    + "  color: $color;\n"
                    + "}\n"
                    + "h2 {\n"
                    + "  color: $color;\n"
                    + "}";

            CssParserResult result = TestUtil.parse(source);

            //there must be some css parsing errors as the less support is disabled
            assertTrue(result.getDiagnostics().size() > 0);
        } finally {
            ExtCss3Parser.isScssSource_unit_tests = true;
        }
    }

    public void testVariable() {
        String source = "$color: #4D926F;\n"
                + "\n"
                + "#header {\n"
                + "  color: $color;\n"
                + "}\n"
                + "h2 {\n"
                + "  color: $color;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testVariable2() {
        String source = "#header {\n"
                + "  border: 2px $color solid;\n"
                + "}\n";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testVariableAsPropertyName() {
        String source = ".class {\n"
                + "    $var: 2;\n"
                + "    three: $var;\n"
                + "    $var: 3;\n"
                + "  }";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testFunction() {
        String source =
                "#header {\n"
                + "  color: ($base-color * 3);\n"
                + "  border-left: $the-border;\n"
                + "  border-right: ($the-border * 2);\n"
                + "}\n";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testFunction2() {
        String source =
                "#footer {\n"
                + "  border-color: desaturate($red, 10%);\n"
                + "  color: ($base-color + #003300);\n"
                + "}";
        ;

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testMixinDeclaration() {
        String source =
                "@mixin rounded-corners ($radius: 5px) {\n"
                + "  -webkit-border-radius: $radius;\n"
                + "  -moz-border-radius: $radius;\n"
                + "  -ms-border-radius: $radius;\n"
                + "  -o-border-radius: $radius;\n"
                + "  border-radius: $radius;\n"
                + "}";
        ;

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testMixinDeclaration2() {
        String source =
                "@mixin box-shadow ($x: 0, $y: 0, $blur: 1px, $color: #000) {\n"
                + "  box-shadow: $arguments;\n"
                + "  -moz-box-shadow: $arguments;\n"
                + "  -webkit-box-shadow: $arguments;\n"
                + "}";
        ;

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testMixinDeclarationWithoutParams() {
        String source =
                "@mixin box-shadow {\n"
                + "  box-shadow: $arguments;\n"
                + "  -moz-box-shadow: $arguments;\n"
                + "  -webkit-box-shadow: $arguments;\n"
                + "}";
        ;

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }
//
//    public void testMixinDeclarationAdvancedArguments() {
//        String source =
//                ".mixin1 (...) {}"
//                + ".mixin2 () {}"
//                + ".mixin3 (@a: 1) {}"
//                + ".mixin4 (@a: 1, ...) {}"
//                + ".mixin5 (@a, ...) {}";
//        ;
//
//        CssParserResult result = TestUtil.parse(source);
//
////        NodeUtil.dumpTree(result.getParseTree());
//        assertResultOK(result);
//    }
//
//    public void testGuardedMixins() {
//        String source =
//                ".mixin (@a) when (@a > 10), (@a = -10) {\n"
//                + "  background-color: black;\n"
//                + "}";
//        ;
//
//        CssParserResult result = TestUtil.parse(source);
//
////        NodeUtil.dumpTree(result.getParseTree());
//        assertResultOK(result);
//    }
//
//    public void testGuardedMixins2() {
//        String source =
//                ".truth (@a) when (@a) { }\n"
//                + ".truth (@a) when (@a = true) { }\n"
//                + ".mixin (@a) when (@media = mobile) { } \n";
//        ;
//
//        CssParserResult result = TestUtil.parse(source);
//
////        NodeUtil.dumpTree(result.getParseTree());
//        assertResultOK(result);
//    }
//
//    public void testGuardedMixinIsFunction() {
//        String source =
//                ".mixin (@a, @b: 0) when (isnumber(@b)) { }\n";
//        ;
//
//        CssParserResult result = TestUtil.parse(source);
//
//        NodeUtil.dumpTree(result.getParseTree());
//        assertResultOK(result);
//    }
//
//    public void testGuardedMixinNotOperator() {
//        String source =
//                ".mixin (@b) when not (@b > 0) { }\n";
//        ;
//
//        CssParserResult result = TestUtil.parse(source);
//
////        NodeUtil.dumpTree(result.getParseTree());
//        assertResultOK(result);
//    }
//

    public void testMixinCall() {
        String source =
                ".class {\n"
                + "  @include mixin($switch, #888);\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testMixinCall2() {
        String source =
                "#menu a {\n"
                + "  color: #111;\n"
                + "  @include bordered;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testFunctions() {
        String source = ".class {\n"
                + "  width: percentage(0.5);\n"
                + "  color: saturate($base, 5%);\n"
                + "  background-color: spin(lighten($base, 25%), 8);\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testFunctions2() {
        String source = "#navbar {\n"
                + "  $navbar-width: 800px;\n"
                + "  $items: 5;\n"
                + "  $navbar-color: #ce4dd6;\n"
                + "\n"
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
                + "}";
        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testRulesNesting() {
        String source = "#header {\n"
                + "  color: black;\n"
                + "  @mixin navigation {\n"
                + "    font-size: 12px;\n"
                + "  }\n"
                + "  font-size: 10px;\n"
                + "  @mixin navigation($a) {\n"
                + "    font-size: 12px;\n"
                + "  }\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testAmpCombinatorInNestedRules() {
        String source = "#header        { color: black;\n"
                + "  .navigation  { font-size: 12px; }\n"
                + "  .logo        { width: 300px;\n"
                + "    &:hover    { text-decoration: none; }\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testAmpCombinatorInNestedRules2() {
        String source = ".shape{\n"
                + "    &:hover{ \n"
                + "        background:$lightRed;   \n"
                + "    }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testNestedRules() {
        String source = "#header{\n"
                + "    /* #header styles */\n"
                + "    h1{\n"
                + "        /* #header h1 styles */\n"
                + "    }\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testOperationsInVariableDeclaration() {
        String source = "$darkBlue: $lightBlue - #555;";
        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testLessExpressionNotInParens() {
        String source = "div {"
                + "width: $pageWidth * .75;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testMixinCallWithoutParams() {
        String source = "#shape1{ @include mymixin; }";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testPropertyValueWithParenthesis() {
        String source = "div {\n"
                + "width: ($u * $unit) - (($margin * 2) + $gpadding + $gborder);\n "
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testPropertyValue() {
        String source = "div {\n"
                + "border-top: 1px solid $color1 - #222; "
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testInterpolationInClassSelector() {
        String source =
                ".rounded-#{$vert}-#{$horz} {\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }
  
    public void testInterpolationInIdSelector() {
        String source =
                ".navb#{$navbar}ar {\n" +
                "  $navbar-width: 800px;"
                + "}\n";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }
    
    public void testInterpolationInPropertyName() {
        String source = 
                  ".rounded {\n"
                + "  border-#{$vert}-#{$horz}-radius: $radius;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }
}
