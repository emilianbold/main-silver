/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.indent;

import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPNewLineIndenterTest extends PHPTestBase {
    public PHPNewLineIndenterTest(String testName) {
        super(testName);
    }

    public void testSmartEnter() throws Exception{
        testIndentInFile("testfiles/indent/smart_enter.php");
    }

    public void testSmartEnter02() throws Exception{
        testIndentInFile("testfiles/indent/smart_enter_02.php");
    }

    public void testSmartEnter03() throws Exception{
        testIndentInFile("testfiles/indent/smart_enter_03.php");
    }

    public void testHtmlIndentInPHP() throws Exception{
        testIndentInFile("testfiles/indent/html_indent_in_php.php");
    }

    public void testFirstLine01() throws Exception{
        testIndentInFile("testfiles/indent/firstline_01.php");
    }

    public void testFirstLine02() throws Exception{
        testIndentInFile("testfiles/indent/firstline_02.php");
    }

    public void testFirstLine03() throws Exception{
        testIndentInFile("testfiles/indent/firstline_03.php");
    }

    public void testFirstLine04() throws Exception{
        testIndentInFile("testfiles/indent/firstline_04.php");
    }

    public void testTrivialRepeatedIndent() throws Exception{
        testIndentInFile("testfiles/indent/trivial_repeated_indent.php");
    }

    public void testAfterSwitchCase() throws Exception{
        testIndentInFile("testfiles/indent/after_switch_case.php");
    }

    public void testAfterSwitchBreak() throws Exception{
        testIndentInFile("testfiles/indent/after_switch_break.php");
    }

    public void testAfterSwitchBreak1() throws Exception{
        testIndentInFile("testfiles/indent/after_switch_break_1.php");
    }

    public void testBreakInWhile() throws Exception{
        testIndentInFile("testfiles/indent/break_in_while.php");
    }

    public void testMultilineFunctionCall() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call.php");
    }

    public void testMultilineFunctionCall01() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_01.php");
    }

    public void testMultilineFunctionCall02() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_02.php");
    }

    public void testMultilineFunctionCall03() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_03.php");
    }

    public void testMultilineFunctionCall04() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_04.php");
    }

    public void testMultilineFunctionCall05() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_05.php");
    }

    public void testMultilineFunctionCall06() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_06.php");
    }

    public void testMultilineFunctionCall07() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_07.php");
    }

    public void testMultilineFunctionCall08() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_08.php");
    }

    public void testMultilineFunctionCall09() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_09.php");
    }

    public void testMultilineFunctionCall10() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_10.php");
    }

    public void testMultilineFunctionCall11() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_11.php");
    }

    public void testMultilineFunctionCall12() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_12.php");
    }

    public void testMultilineFunctionCall13() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_13.php");
    }

    public void testMultilineFunctionCall14() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_14.php");
    }

    public void testMultilineFunctionCall15() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_15.php");
    }

    public void testMultilineFunctionCall16() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_16.php");
    }

    public void testMultilineFunctionCall17() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_17.php");
    }

    public void testMultilineFunctionCall18() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_18.php");
    }

    public void testMultilineFunctionCall19() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_19.php");
    }

    public void testMultilineFunctionCall20() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_20.php");
    }

    public void testMultilineFunctionCall21() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_21.php");
    }

    public void testMultilineFunctionCall22() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_22.php");
    }

    public void testMultilineFunctionCall23() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_23.php");
    }

    public void testMultilineFunctionCall24() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_24.php");
    }

    public void testMultilineFunctionCall25() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_25.php");
    }

    public void testMultilineFunctionCall26() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_26.php");
    }

    public void testMultilineFunctionCall27() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_27.php");
    }

    public void testMultilineFunctionCall28() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_28.php");
    }

    public void testMultilineFunctionCall29() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_29.php");
    }

    public void testMultilineFunctionCall30() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_30.php");
    }

    public void testMultilineFunctionCall31() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_31.php");
    }

    public void testIndentAfterClosingBracket() throws Exception{
        testIndentInFile("testfiles/indent/indent_after_closing_bracket.php");
    }

    public void testControlStmtWithoutBracket1() throws Exception{
        testIndentInFile("testfiles/indent/control_stmt_without_bracket1.php");
    }

    public void testControlStmtWithoutBracket2() throws Exception{
        testIndentInFile("testfiles/indent/control_stmt_without_bracket2.php");
    }

    public void testControlStmtWithoutBracket3() throws Exception{
        testIndentInFile("testfiles/indent/control_stmt_without_bracket3.php");
    }

    public void testIndentAfterMultilineStmt1() throws Exception{
        testIndentInFile("testfiles/indent/indent_after_multiline_stmt1.php");
    }

    public void testMultilineString1() throws Exception{
        testIndentInFile("testfiles/indent/multiline_string1.php");
    }

    public void testMultilineString2() throws Exception{
        testIndentInFile("testfiles/indent/multiline_string2.php");
    }

    public void testMultilineString3() throws Exception{
        testIndentInFile("testfiles/indent/multiline_string3.php");
    }

    public void testMultilineString4() throws Exception{
        testIndentInFile("testfiles/indent/multiline_string4.php");
    }

    public void testArrays1() throws Exception{
        testIndentInFile("testfiles/indent/arrays1.php");
    }

    public void testArrays2() throws Exception{
        testIndentInFile("testfiles/indent/arrays2.php");
    }

    public void testArrays3() throws Exception{
        testIndentInFile("testfiles/indent/arrays3.php");
    }

    public void testArrays4() throws Exception{
        testIndentInFile("testfiles/indent/arrays4.php");
    }
    
    public void testArrays5() throws Exception{
        testIndentInFile("testfiles/indent/arrays5.php");
    }

    public void testArrays6() throws Exception{
        testIndentInFile("testfiles/indent/arrays6.php");
    }

    public void testArrays7() throws Exception{
        testIndentInFile("testfiles/indent/arrays7.php");
    }

    public void testArrays8() throws Exception{
        testIndentInFile("testfiles/indent/arrays8.php");
    }

    public void testArrays9() throws Exception{
        testIndentInFile("testfiles/indent/arrays9.php");
    }

    public void testArrays10() throws Exception{
        testIndentInFile("testfiles/indent/arrays10.php");
    }

    public void testArrays11() throws Exception{
        testIndentInFile("testfiles/indent/arrays11.php");
    }

    public void testArrays12() throws Exception{
        testIndentInFile("testfiles/indent/arrays12.php");
    }

    public void testArrays13() throws Exception{
        testIndentInFile("testfiles/indent/arrays13.php");
    }

    public void testArrays14() throws Exception{
        testIndentInFile("testfiles/indent/arrays14.php");
    }

    public void testArrays15() throws Exception{
        testIndentInFile("testfiles/indent/arrays15.php");
    }

    public void testArrays16() throws Exception{
        testIndentInFile("testfiles/indent/arrays16.php");
    }

    public void testArrays17() throws Exception{
        testIndentInFile("testfiles/indent/arrays17.php");
    }

    public void testArrays18() throws Exception{
        testIndentInFile("testfiles/indent/arrays18.php");
    }

    public void testArrays19() throws Exception{
        testIndentInFile("testfiles/indent/arrays19.php");
    }

    public void testArrays20() throws Exception{
        testIndentInFile("testfiles/indent/arrays20.php");
    }

    public void testArrays21() throws Exception{
        testIndentInFile("testfiles/indent/arrays21.php");
    }

    public void testArrays22() throws Exception{
        testIndentInFile("testfiles/indent/arrays22.php");
    }

    public void testArrays23() throws Exception{
        testIndentInFile("testfiles/indent/arrays23.php");
    }

    public void testArrays24() throws Exception{
        testIndentInFile("testfiles/indent/arrays24.php");
    }

    public void testArrays25() throws Exception{
        testIndentInFile("testfiles/indent/arrays25.php");
    }

    public void testArrays26() throws Exception{
        testIndentInFile("testfiles/indent/arrays26.php");
    }

    public void testArrays27() throws Exception{
        testIndentInFile("testfiles/indent/arrays27.php");
    }

    public void testArrays28() throws Exception{
        testIndentInFile("testfiles/indent/arrays28.php");
    }

    public void testArrays29() throws Exception{
        testIndentInFile("testfiles/indent/arrays29.php");
    }
    
    public void test157137() throws Exception{
        testIndentInFile("testfiles/indent/issue157137.php");
    }

    public void test162586() throws Exception{
        testIndentInFile("testfiles/indent/issue162586.php");
    }

    public void test166552() throws Exception{
        testIndentInFile("testfiles/indent/issue166552.php");
    }
    
    public void test168908() throws Exception{
        testIndentInFile("testfiles/indent/issue168908.php");
    }

   public void test173979_1_stableFixed() throws Exception {
        testIndentInFile("testfiles/indent/issue173979_1.php");
    }

    public void test173979_2() throws Exception {
        testIndentInFile("testfiles/indent/issue173979_2.php");
    }

    public void test175118_01() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_01.php");
    }

    public void test175118_02() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_02.php");
    }

    public void test175118_03() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_03.php");
    }

    public void test175118_04() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_04.php");
    }

    public void test175118_05() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_05.php");
    }

    public void test175118_06() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_06.php");
    }

    public void test175118_07() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_07.php");
    }

    public void test175118_08() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_08.php");
    }
    
    public void test175118_09() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_09.php");
    }

    public void test175118_10() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_10.php");
    }

    public void test175118_11() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_11.php");
    }

    public void test175118_12() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_12.php");
    }

    public void test175118_13() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_13.php");
    }

    public void test175118_14() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_14.php");
    }

    public void test175118_15() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_15.php");
    }

    public void test175118_16() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_16.php");
    }

    public void test175118_17() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_17.php");
    }

    public void test175437_1() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_1.php");
    }

    public void test175437_2() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_2.php");
    }

    public void test175437_3() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_3.php");
    }

    public void test175437_4() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_4.php");
    }

    public void test175437_5() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_5.php");
    }

    public void test175437_6() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_6.php");
    }

    public void test175437_7() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_7.php");
    }

    public void test175437_8() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_8.php");
    }
    
//  need to be fiexed the multi line expressions
//    public void test175437_9() throws Exception {
//        testIndentInFile("testfiles/indent/issue175437_9.php");
//    }
//
//    public void test175437_10() throws Exception {
//        testIndentInFile("testfiles/indent/issue175437_10.php");
//    }

    public void test175437_11() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_11.php");
    }

    public void test175437_12() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_12.php");
    }

    public void test175437_13() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_13.php");
    }

    public void testLineComment_1() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_1.php");
    }

    public void testLineComment_2() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_2.php");
    }

    public void testLineComment_3() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_3.php");
    }

    public void testLineComment_4() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_4.php");
    }

    public void testLineComment_175685_1() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_1.php");
    }

    public void testLineComment_175685_2() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_2.php");
    }

    public void testLineComment_175685_3() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_3.php");
    }

    public void testLineComment_175685_4() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_4.php");
    }

    public void testLineComment_175685_5() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_5.php");
    }

    public void testLineComment_175685_6() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_6.php");
    }

    public void testLineComment_175685_7() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_7.php");
    }

    public void testLineComment_175685_8() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_8.php");
   }

    public void testLineComment_175685_9() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_9.php");
    }

    public void testLineComment_175685_10() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_10.php");
    }

    public void testLineComment_175685_11() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_11.php");
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        try {
            TestLanguageProvider.register(HTMLTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
        try {
            TestLanguageProvider.register(PHPTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
    }

    protected void testIndentInFile(String file) throws Exception {
        testIndentInFile(file, null);
    }

    protected void testIndentInFile(String file, IndentPrefs preferences) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        String source = readFile(fo);

        int sourcePos = source.indexOf('^');     
        assertNotNull(sourcePos);
        String sourceWithoutMarker = source.substring(0, sourcePos) + source.substring(sourcePos+1);
        Formatter formatter = getFormatter(null);
        
        JEditorPane ta = getPane(sourceWithoutMarker);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        BaseDocument doc = (BaseDocument) ta.getDocument();
        if (formatter != null) {
            configureIndenters(doc, formatter, true);
        }

        setupDocumentIndentation(doc, preferences);

        runKitAction(ta, DefaultEditorKit.insertBreakAction, "\n");

        doc.getText(0, doc.getLength());
        doc.insertString(caret.getDot(), "^", null);

        String target = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, target, false, ".indented");
    }
}
