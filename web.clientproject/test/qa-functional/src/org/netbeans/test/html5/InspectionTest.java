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
package org.netbeans.test.html5;

import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Vladimir Riha
 */
public class InspectionTest extends GeneralHTMLProject {

    private static final Logger LOGGER = Logger.getLogger(InspectionTest.class.getName());

    public InspectionTest(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(InspectionTest.class).addTest(
                "testOpenProject",
                "testBasicInspection",
                "testMultipleSelect",
                "testEditNumberedProperty",
                "testMatchedHighlighted"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void testOpenProject() throws Exception {
        startTest();
        InspectionTest.current_project = "simpleProject";
        openProject("simpleProject");
        setRunConfiguration("Embedded WebKit Browser", true, true);
        endTest();
    }

    /**
     * Case: Runs file, turns inspection on, selects one element, adds another
     * element to selection, checks that 2 elements are selected
     */
    public void testMultipleSelect() {
        startTest();
        runFile("simpleProject", "index.html");
        EmbeddedBrowserOperator eb = new EmbeddedBrowserOperator("Web Browser");
        eb.checkInspectModeButton(true);
        EditorOperator eo = new EditorOperator("index.html");
        eo.setCaretPositionToLine(19);
        type(eo, "window.setTimeout(function() {document.getElementById(\"el1\").setAttribute(\":netbeans_selected\", \"set\")}, 3000);\n"
                + "window.setTimeout(function() {document.getElementById(\"el2\").setAttribute(\":netbeans_selected\", \"add\")}, 5000);");
        eo.save();
        waitElementsSelected(2, 0);
        HTMLElement[] el = getSelectedElements();
        assertEquals("Unexpected number of selected elements: was " + el.length + " should be 2", 2, el.length);
        eo.deleteLine(19);
        eo.deleteLine(19);
        eb.closeWindow();
        eo.save();
        endTest();
    }

    /**
     * Case: Runs file, turns inspection on, selects one element. Checks if
     * element is selected in Navigator, CSS Styles window contain proper data
     * and element is focused in editor
     */
    public void testBasicInspection() {
        startTest();
        runFile("simpleProject", "index.html");

        EmbeddedBrowserOperator eb = new EmbeddedBrowserOperator("Web Browser");
        eb.checkInspectModeButton(true);
        EditorOperator eo = new EditorOperator("index.html");
        eo.setCaretPositionToLine(19);
        type(eo, "window.setTimeout(function() {document.getElementById(\"el2\").setAttribute(\":netbeans_selected\", \"set\")}, 1000);");
        eo.save();
        waitElementsSelected(1, 2000);
        HTMLElement[] el = getSelectedElements();

        HTMLNavigatorOperator no = new HTMLNavigatorOperator("div - Navigator");
        CSSStylesOperator co = new CSSStylesOperator("index.html");
        int position = eo.txtEditorPane().getCaretPosition();
        AppliedRule[] rules = co.getAppliedRules();

        assertEquals("Unexpected number of applied rules", 3, rules.length);
        assertEquals("Unexpected At-rule", "(max-width: 2000px)", rules[0].atRule);
        assertEquals("Unexpected list of applied rules", "#el2.test.test", rules[0].selector + rules[1].selector + rules[2].selector);
        assertEquals("Unexpected source css file", "style.css:9", rules[0].source);
        assertEquals("Unexpected path", "div#el2.test", rules[1].path);
        assertEquals("Unexpected number of selected elements: was " + el.length + " should be 1", 1, el.length);
        assertEquals("Unexpected element in Navigator", "[html, body, div]div#el2.test", no.getFocusedElement());
        assertEquals("Unexpected element is selected", "[html, body, div]div#el2.test", el[0].getNavigatorString());
        assertEquals("Unexpected element in CSS Styles", "div #el2.test", co.getSelectedHTMLElementName());
        assertEquals("Unexpected element focused in editor", "<div id=\"el2\" class=\"test\">", eo.txtEditorPane().getText(position, 27));

        eo.deleteLine(19);
        eb.closeWindow();
        eo.save();
        endTest();
    }

    /**
     * Case: Runs file, puts cursor inside {@code div} element that has
     * font-size set in css rule. In CSS Styles window, font-size is changed
     * twice to different value using up/down buttons and after each change,
     * waits if focus is not lost and checks that font-size is updated
     */
    public void testEditNumberedProperty() {
        startTest();
        runFile("simpleProject", "index.html");

        EmbeddedBrowserOperator eb = new EmbeddedBrowserOperator("Web Browser");
        EditorOperator eo = new EditorOperator("index.html");
        eo.setCaretPosition("ipsum", true);
        evt.waitNoEvent(500);
        CSSStylesOperator co = new CSSStylesOperator("index.html");
        co.editNumberedProperty("font-size", 5, true, true, false);
        evt.waitNoEvent(2000); // wait to check if focus is not lost after a while
        String[] result = co.getFocusedProperty();
        assertEquals("Unexpect property selected after up/down modification", "font-size", result[0]);
        assertEquals("Unexpect property value after up/down modification", "15px", result[1]);

        co.editNumberedProperty("font-size", 5, false, false, true);
        result = co.getFocusedProperty();
        assertEquals("Unexpect property value after up/down modification", "10px", result[1]);
        evt.waitNoEvent(500); // wait to check if focus is not lost after 2nd modification
        result = co.getFocusedProperty();
        assertEquals("Unexpect property selected after up/down modification", "font-size", result[0]);

        eb.closeWindow();
        endTest();
    }

    /**
     * Case: Runs file, turns inspection on, selects one element. Then clicks on
     * one applied rule and checks number of matching elements that are outlined
     */
    public void testMatchedHighlighted() {
        startTest();
        runFile("simpleProject", "index.html");

        EmbeddedBrowserOperator eb = new EmbeddedBrowserOperator("Web Browser");
        eb.checkInspectModeButton(true);
        EditorOperator eo = new EditorOperator("index.html");
        eo.setCaretPositionToLine(19);
        type(eo, "window.setTimeout(function() {document.getElementById(\"el2\").setAttribute(\":netbeans_selected\", \"set\")}, 1000);");
        eo.save();
        waitElementsSelected(1, 2000);

        CSSStylesOperator co = new CSSStylesOperator("index.html");
        co.focusRule(".test");
        waitMatchedElements(2, 0);
        HTMLElement[] elements = getMatchingElements();

        assertEquals("Unexpected number of matched elements", 2, elements.length);
        assertEquals("Unexpected element is selected", "[html, body, div]div#el2.test", elements[0].getNavigatorString());
        assertEquals("Unexpected element is selected", "[html, body, div]div.test", elements[1].getNavigatorString());

        eo.deleteLine(19);
        eb.closeWindow();
        eo.save();
        endTest();
    }
}
