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

import java.io.IOException;
import javax.swing.text.BadLocationException;
import junit.framework.AssertionFailedError;
import org.netbeans.modules.css.lib.api.*;
import org.netbeans.modules.parsing.spi.ParseException;

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
                ".navb#{$navbar}ar {\n"
                + "  $navbar-width: 800px;"
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

    public void testInterpolationExpressionInSelectorWithWS() {
        String source =
                ".body.firefox #{$selector}:before {\n"
                + "    content: \"Hi, Firefox users!\";\n"
                + "  }";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testInterpolationExpressionInFunctionInTheExpression() {
        String source =
                ".body.firefox #{ie-hex-str($green)}:before {\n"
                + "    content: \"Hi, Firefox users!\";\n"
                + "  }";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testInterpolationExpressionInPropertyValue() {
        String source =
                "p {\n"
                + "  $font-size: 12px;\n"
                + "  $line-height: 30px;\n"
                + "  font: #{$font-size}/#{$line-height};\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    //fails as the 
    //
    //.body.firefox #{$selector}:before 
    //
    //selector is parsed as property declaration - due to the colon presence - FIXME!!!
    public void testInterpolationExpressionComplex_fails() {
        String source =
                "@mixin firefox-message($selector) {\n"
                + "  .body.firefox #{$selector}:before {\n"
                + "    content: \"Hi, Firefox users!\";\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testMixinsWithArgumentsComplex() {
        String source =
                "/* style.scss */\n"
                + "\n"
                + "@mixin rounded($vert, $horz, $radius: 10px) {\n"
                + "  border-#{$vert}-#{$horz}-radius: $radius;\n"
                + "  -moz-border-radius-#{$vert}#{$horz}: $radius;\n"
                + "  -webkit-border-#{$vert}-#{$horz}-radius: $radius;\n"
                + "}\n"
                + "\n"
                + "#navbar li { @include rounded(top, left); }\n"
                + "#footer { @include rounded(top, left, 5px); }\n"
                + "#sidebar { @include rounded(top, left, 8px); }";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    //like normal css import, but the ref. file doesn't need to have an extension,
    //there are also some rules regarding the naming convention, but these
    //are covered by semantic analysis, not parsing
    public void testImport() {
        String source =
                "@import \"rounded\";\n";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testNestedProperties() {
        String source =
                ".funky {\n"
                + "  font: {\n"
                + "    family: fantasy;\n"
                + "    size: 30em;\n"
                + "    weight: bold;\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testNestedPropertiesWithValue() {
        String source =
                ".funky {\n"
                + "  font: 2px/3px {\n"
                + "    family: fantasy;\n"
                + "    size: 30em;\n"
                + "    weight: bold;\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testLineComment() {
        String source =
                ".funky {\n"
                + " //line comment\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testMixinCallInStylesheet() {
        String source =
                "@include firefox-message(\".header\");\n";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testDefaultVariable() {
        String source =
                "$content: \"Second content?\" !default;\n";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testMultipleImport() {
        String source =
                "@import \"rounded-corners\", \"text-shadow\";\n";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testInterpolationExpressionInImport() {
        String source =
                "@import url(\"http://fonts.googleapis.com/css?family=#{$family}\");\n";
        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    //the grammar defines the imports needs to be at the very beginning of the file,
    //though this is not true in case of the preprocessor code
    public void testSASSCodeMayPrecedeImport() {
        String source = "$var: my;\n"
                + "@import url(\"#{$var}\");\n";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testNestedMediaQueries() {
        String source = ".sidebar {\n"
                + "  width: 300px;\n"
                + "  @media screen and (orientation: landscape) {\n"
                + "  .class {\n"
                + "    width: 500px;\n"
                + "  }\n"
                + "}\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testNestedMediaQueryInMediaQuery() {
        String source = "@media screen {\n"
                + "  .sidebar {\n"
                + "    @media (orientation: landscape) {\n"
                + "   //   width: 500px;\n"
                + "    }\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    //the media query can the property declarations directly.
    public void testPropertiesDirectlyInMediaQuery() {
        String source = "@media screen and (orientation: landscape) {\n"
                + "    width: 500px;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testInterpolationExpressionInMediaQuery() {
        String source = "@media #{$media} {\n"
                + "  .sidebar {\n"
                + "    width: 500px;\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    //the scss_mq_interpolation_expression doesn't want to be extended 
    //by LPAREN and RPAREN from some reason (endless loop).
    public void testInterpolationExpressionWithParenMediaQuery_fails() {
        String source = "$media: screen;\n"
                + "$feature: -webkit-min-device-pixel-ratio;\n"
                + "$value: 1.5;\n"
                + "\n"
                + "@media #{$media} and (#{$feature}: #{$value}) {\n"
                + "  .sidebar {\n"
                + "    width: 500px;\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testExtend() {
        String source = ".seriousError {\n"
                + "  @extend .error;\n"
                + "  border-width: 3px;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testExtendComplex() {
        String source = ".hoverlink {\n"
                + "  @extend a:hover;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testExtendOnlySelectors() {
        String source = "#context a%extreme {\n"
                + "  color: blue;\n"
                + "  font-weight: bold;\n"
                + "  font-size: 2em;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testExtendOnlySelectorCall() {
        String source = ".notice {\n"
                + "  @extend %extreme;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testDebug() {
        String source = "@debug 10em + 12em;\n"
                + ".class {\n"
                + "@debug \"hello\";\n"
                + "}\n"
                + "@mixin mymixin {\n"
                + "@debug 20;"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testWarn() {
        String source = "@warn 10em + 12em;\n"
                + ".class {\n"
                + "@warn \"hello\";\n"
                + "}\n"
                + "@mixin mymixin {\n"
                + "@warn 20;"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testIf() {
        String source = "p {\n"
                + "  @if 1 + 1 == 2 { border: 1px solid;  }\n"
                + "  @if 5 < 3      { border: 2px dotted; }\n"
                + "  @if null       { border: 3px double; }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testFor() {
        String source = "@for $i from 1 through 3 {\n"
                + "  .item-#{$i} { width: 2em * $i; }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testEach() {
        String source = "@each $animal in puma, sea-slug, egret, salamander {\n"
                + "  .#{$animal}-icon {\n"
                + "    background-image: url('/images/#{$animal}.png');\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testWhile() {
        String source = "$i: 6;\n"
                + "@while $i > 0 {\n"
                + "  .item-#{$i} { width: 2em * $i; }\n"
                + "  $i: $i - 2;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testDefineOwnFunction() {
        String source = "@function grid-width($n) {\n"
                + "  @return $n * $grid-width + ($n - 1) * $gutter-width;\n"
                + "}\n"
                + "\n"
                + "#sidebar { width: grid-width(5); }";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testMixinCallWithFunctionWithNoArgs() {
        String source = ".ease-out-expo-animation {\n"
                + "  @include transition-timing-function(ease-out-expo()); \n"
                + "  color: best-color();\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testVariableDeclarationWithCommaSeparatedValues() {
        String source = "$blueprint-font-family: Helvetica Neue, Arial, Helvetica, sans-serif;";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testAmpProblem_fails() {
        String source =
                ".clazz {\n"
                + "    &.position#{$i} {\n"
                + "    left: ($i * -910px); \n"
                + "}\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testMergedScssTests() throws ParseException, BadLocationException, IOException {
        CssParserResult result = TestUtil.parse(getTestFile("testfiles/scss/scss-tests-merged.scss"));
//        TestUtil.dumpResult(result);
        assertResult(result, 0);
    }

    public void testLocalVariableDeclaration() {
        String source =
                "p {\n"
                + "  $width: 1000px;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

        //the "$width: 1000px;" is supposed to be parsed as variable declaration, not property declaration!
        assertNull(NodeUtil.query(result.getParseTree(), "styleSheet/body/bodyItem/rule/declarations/declaration"));
        assertNotNull(NodeUtil.query(result.getParseTree(), "styleSheet/body/bodyItem/rule/declarations/cp_variable_declaration"));

    }

    public void testMixinCallWithWSBeforeFirstArgument() {
        String source =
                "@mixin a {\n"
                + "  @include b( linear-gradient(\n"
                + "      lighten($bg-color, 5%),\n"
                + "      darken($bg-color, 5%)\n"
                + "    )\n"
                + "  );\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testUnexpectedANDInMedia() {
        String source =
                "@media screen and ($width-name : $target-width) {\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testIf_Else() {
        String source =
                "$type: monster;\n"
                + "p {\n"
                + "  @if $type == ocean {\n"
                + "    color: blue;\n"
                + "  } @else if $type == matador {\n"
                + "    color: red;\n"
                + "  } @else if $type == monster {\n"
                + "    color: green;\n"
                + "  } @else {\n"
                + "    color: black;\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

        //only 'if' is allowed as the ident after @else keyword
        source =
                "p {\n"
                + "  @if $type == ocean {\n"
                + "    color: blue;\n"
                + "  } @else Yf $type == matador {\n"
                + "    color: red;\n"
                + "  }\n"
                + "}";

        result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertTrue(result.getDiagnostics().size() > 0);

    }

    public void testContentDirective() {
        String source =
                "@mixin apply-to-ie6-only {\n"
                + "  * html {\n"
                + "    @content;\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testContentDirectiveInMedia() {
        String source =
                "@mixin respond-to($media) {\n"
                + "  @if $media == handhelds {\n"
                + "    @media only screen and (max-width: $break-small) { @content; }\n"
                + "  } @else if $media == medium-screens {\n"
                + "    @media only screen and (min-width: $break-small + 1) and (max-width:\n"
                + "$break-large - 1) { @content; }\n"
                + "  }\n"
                + "  @else if $media == wide-screens {\n"
                + "    @media only screen and (min-width: $break-large) { @content; }\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testMixinCallArgWithPropertyName() {
        String source =
                "@mixin border-radius($radius: 5px, $moz: true, $webkit: true, $ms: true) {\n"
                + "}\n"
                + "div{\n"
                + "    @include border-radius($webkit:false);\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testMixinCallArgWithValueSeparatedByWS() {
        String source =
                "#id {\n"
                + "    @include border-radius(5px, -moz -webkit);\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testPropertyValueSyntacticPredicateBoundary() {
        //the scss_declaration_property_value_interpolation_expression synt. predicate
        //was terminated just by colon so it seen the interpolation expression
        //few lines below and caused bad parsing
        String source =
                "#test2 { \n"
                + "    background-color: cyan\n"
                + "}\n"
                + "#test#{$i} { }";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testMultiplicityOperatorInPropertyValueFunction() {
        String source =
                ".c {\n"
                + "    background-color: darken(orange, $i*5);\n"
                + "}\n"
                + "";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }
    
    public void testIfControlExpression() {
        assertParses(" @if $arg != null and $arg2 != transparent { }");
        assertParses(" @if not $arg != null and $arg2 != transparent { }");
        assertParses(" @if not $arg != null or not $arg2 != transparent { }");
        assertParses(" @if true or not $arg2 != transparent { }");
    }
    
    
}
