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
package org.netbeans.modules.php.twig.editor.braces;

import java.util.Arrays;
import java.util.Collections;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.bracesmatching.api.BracesMatchingTestUtils;
import org.netbeans.modules.php.twig.editor.TwigTestBase;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class TwigBracesMatcherTest extends TwigTestBase {

    public TwigBracesMatcherTest(String testName) {
        super(testName);
    }

    public void testAutoescape() throws Exception {
        testMatches("<html>\n"
                + "{% auto^escape %}\n"
                + "text\n"
                + "{% endautoescape %}\n"
                + "</html>");
    }

    public void testEndAutoescape() throws Exception {
        testMatches("<html>\n"
                + "{% autoescape %}\n"
                + "text\n"
                + "{% end^autoescape %}\n"
                + "</html>");
    }

    public void testBlock() throws Exception {
        testMatches("<html>\n"
                + "{% bl^ock %}\n"
                + "text\n"
                + "{% endblock %}\n"
                + "</html>");
    }

    public void testEndBlock() throws Exception {
        testMatches("<html>\n"
                + "{% block %}\n"
                + "text\n"
                + "{% end^block %}\n"
                + "</html>");
    }

    public void testEmbed() throws Exception {
        testMatches("<html>\n"
                + "{% em^bed %}\n"
                + "text\n"
                + "{% endembed %}\n"
                + "</html>");
    }

    public void testEndEmbed() throws Exception {
        testMatches("<html>\n"
                + "{% embed %}\n"
                + "text\n"
                + "{% end^embed %}\n"
                + "</html>");
    }

    public void testFilter() throws Exception {
        testMatches("<html>\n"
                + "{% fil^ter %}\n"
                + "text\n"
                + "{% endfilter %}\n"
                + "</html>");
    }

    public void testEndFilter() throws Exception {
        testMatches("<html>\n"
                + "{% filter %}\n"
                + "text\n"
                + "{% end^filter %}\n"
                + "</html>");
    }

    public void testFor() throws Exception {
        testMatches("<html>\n"
                + "{% f^or %}\n"
                + "text\n"
                + "{% endfor %}\n"
                + "</html>");
    }

    public void testEndFor() throws Exception {
        testMatches("<html>\n"
                + "{% for %}\n"
                + "text\n"
                + "{% end^for %}\n"
                + "</html>");
    }

    public void testMacro() throws Exception {
        testMatches("<html>\n"
                + "{% mac^ro %}\n"
                + "text\n"
                + "{% endmacro %}\n"
                + "</html>");
    }

    public void testEndMacro() throws Exception {
        testMatches("<html>\n"
                + "{% macro %}\n"
                + "text\n"
                + "{% end^macro %}\n"
                + "</html>");
    }

    public void testRaw() throws Exception {
        testMatches("<html>\n"
                + "{% r^aw %}\n"
                + "text\n"
                + "{% endraw %}\n"
                + "</html>");
    }

    public void testEndRaw() throws Exception {
        testMatches("<html>\n"
                + "{% raw %}\n"
                + "text\n"
                + "{% end^raw %}\n"
                + "</html>");
    }

    public void testSandbox() throws Exception {
        testMatches("<html>\n"
                + "{% san^dbox %}\n"
                + "text\n"
                + "{% endsandbox %}\n"
                + "</html>");
    }

    public void testEndSandbox() throws Exception {
        testMatches("<html>\n"
                + "{% sandbox %}\n"
                + "text\n"
                + "{% end^sandbox %}\n"
                + "</html>");
    }

    public void testSet() throws Exception {
        testMatches("<html>\n"
                + "{% s^et %}\n"
                + "text\n"
                + "{% endset %}\n"
                + "</html>");
    }

    public void testEndSet() throws Exception {
        testMatches("<html>\n"
                + "{% set %}\n"
                + "text\n"
                + "{% end^set %}\n"
                + "</html>");
    }

    public void testSpaceless() throws Exception {
        testMatches("<html>\n"
                + "{% space^less %}\n"
                + "text\n"
                + "{% endspaceless %}\n"
                + "</html>");
    }

    public void testEndSpaceless() throws Exception {
        testMatches("<html>\n"
                + "{% spaceless %}\n"
                + "text\n"
                + "{% end^spaceless %}\n"
                + "</html>");
    }

    public void testIf_01() throws Exception {
        testMatches("<html>\n"
                + "{% i^f true %}\n"
                + "text\n"
                + "{% endif %}\n"
                + "</html>");
    }

    public void testIf_02() throws Exception {
        testMatches("<html>\n"
                + "{% i^f true %}\n"
                + "text\n"
                + "{% elseif true %}\n"
                + "text\n"
                + "{% endif %}\n"
                + "</html>");
    }

    public void testIf_03() throws Exception {
        testMatches("<html>\n"
                + "{% i^f true %}\n"
                + "text\n"
                + "{% else %}\n"
                + "text\n"
                + "{% endif %}\n"
                + "</html>");
    }

    public void testIf_04() throws Exception {
        testMatches("<html>\n"
                + "{% if true %}\n"
                + "text\n"
                + "{% end^if %}\n"
                + "</html>");
    }

    public void testIf_05() throws Exception {
        testMatches("<html>\n"
                + "{% if true %}\n"
                + "text\n"
                + "{% else^if true %}\n"
                + "text\n"
                + "{% endif %}\n"
                + "</html>");
    }

    public void testIf_06() throws Exception {
        testMatches("<html>\n"
                + "{% if true %}\n"
                + "text\n"
                + "{% elseif true %}\n"
                + "text\n"
                + "{% end^if %}\n"
                + "</html>");
    }

    public void testIf_07() throws Exception {
        testMatches("<html>\n"
                + "{% if true %}\n"
                + "text\n"
                + "{% el^se %}\n"
                + "text\n"
                + "{% endif %}\n"
                + "</html>");
    }

    public void testIf_08() throws Exception {
        testMatches("<html>\n"
                + "{% if true %}\n"
                + "text\n"
                + "{% else %}\n"
                + "text\n"
                + "{% end^if %}\n"
                + "</html>");
    }

    public void testEmptyElse() throws Exception {
        testMatches("<html>\n"
                + "{% el^se %}\n"
                + "</html>");
    }

    private void testMatches(String original) throws Exception {
        BracesMatcherFactory factory = MimeLookup.getLookup(getPreferredMimeType()).lookup(BracesMatcherFactory.class);
        int caretPosition = original.indexOf('^');
        assert caretPosition != -1;
        original = original.substring(0, caretPosition) + original.substring(caretPosition + 1);

        BaseDocument doc = getDocument(original);

        MatcherContext context = BracesMatchingTestUtils.createMatcherContext(doc, caretPosition, false, 1);
        BracesMatcher matcher = factory.createMatcher(context);
        int[] origin = null, matches = null;
        try {
            origin = matcher.findOrigin();
            matches = matcher.findMatches();
        } catch (InterruptedException ex) {
        }

        assertNotNull("Did not find origin", origin);
        assertEquals("Wrong origin length", 2, origin.length);

        int matchesLength = 0;
        if (matches != null) {
            matchesLength = matches.length;
        }
        int[] boundaries = new int[origin.length + matchesLength];
        System.arraycopy(origin, 0, boundaries, 0, origin.length);
        if (matchesLength != 0) {
            System.arraycopy(matches, 0, boundaries, origin.length, matches.length);
        }

        Integer[] boundariesIntegers = new Integer[boundaries.length];
        for (int i = 0; i < boundaries.length; i++) {
            boundariesIntegers[i] = boundaries[i];
        }
        Arrays.sort(boundariesIntegers, Collections.reverseOrder());
        String expected = original;
        boolean caretInserted = false;
        for (int i : boundariesIntegers) {
            if (i <= caretPosition && !caretInserted) {
                expected = expected.substring(0, caretPosition) + "^" + expected.substring(caretPosition);
                caretInserted = true;
            }
            expected = expected.substring(0, i) + "*" + expected.substring(i);
        }
        assertDescriptionMatches("testfiles/braces/" + getName(), expected, true, ".braces", false);
    }

}
