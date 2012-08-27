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
package org.netbeans.modules.jumpto.common;

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
 * Contributor(s): markiewb@netbeans.org
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */


import java.awt.Color;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/**
 * Highlight the match of text patterns. The matching parts will be placed
 * within HTML-tags, so it can be used easily within the Swing UI.
 * @author markiewb
 */
public class HighlightingNameFormatter {

    private static final String COLOR_FORMAT_PATTERN = "<span style=\"background-color:%s; font-weight:bold; color:%s\">%s</span>"; //NOI18N
    private static final String BOLD_FORMAT_PATTERN = "<span style=\"font-weight:bold;\">%s</span>";    //NOI18N
    private final boolean caseSensitive;
    private String formatPattern;

    private HighlightingNameFormatter(
            @NonNull final String pattern,
            final boolean caseSensitive) {
        Parameters.notNull("pattern", pattern); //NOI18N
        this.formatPattern = pattern;
        this.caseSensitive = caseSensitive;
    }

    @NonNull
    public String formatName(
            @NonNull final String name,
            @NonNull final String textToFind) {
        if (null == textToFind || "".equals(textToFind)) {
            return name;
        }
        BitSet bitSet = new BitSet(name.length());
        List<String> parts = splitByCamelCaseAndWildcards(textToFind);

        String convertedTypeName = caseSensitive ? name : name.toLowerCase();
        //mark the chars to be highlighted
        int startIndex = 0;
        for (String camelCasePart : parts) {

            int indexOf = convertedTypeName.indexOf(caseSensitive ? camelCasePart : camelCasePart.toLowerCase(), startIndex);
            if (indexOf != -1) {

                //mark the chars 
                bitSet.set(indexOf, indexOf + camelCasePart.length(), true);
            } else {
                break;
            }
            startIndex = indexOf + camelCasePart.length();
        }

        //highlight the marked chars via  tags
        StringBuilder formattedTypeName = new StringBuilder();
        int i = 0;
        while (i < name.toCharArray().length) {

            boolean isMarked = bitSet.get(i);

            if (isMarked) {
                int numberOfContinuousHighlights = bitSet.nextClearBit(i) - i;
                String part = name.substring(i, i + numberOfContinuousHighlights);
                formattedTypeName.append(String.format(formatPattern, part));
                i += numberOfContinuousHighlights;
            } else {
                formattedTypeName.append(name.charAt(i));
                i++;
            }
        }
        return formattedTypeName.toString();
    }

    private List<String> splitByCamelCaseAndWildcards(String searchText) {
        //AbcDeFGhiJo -> [Abc, De, F, Ghi, Jo]
        StringBuilder sb = new StringBuilder(searchText.length());
        for (char c : searchText.toCharArray()) {
            if (Character.isUpperCase(c)) {
                //add magic split marker into text before the uppercase char
                //example: AbcDeFGhiJo -> &Abc&De&F&Ghi&Jo
                sb.append("&");
                sb.append(c);
            } else {
                sb.append(c);
            }
        }
        //split by camelcase (using the split marker) or the wildcards *,?
        String[] split = sb.toString().split("[&|\\*|\\?]");
        return Arrays.asList(split);
    }

    @NonNull
    public static HighlightingNameFormatter createColorFormatter(
            @NonNull final Color bgColor,
            @NonNull final Color fgColor,
            final boolean caseSensitive) {
        final String bgColorHighlight = Integer.toHexString(bgColor.getRGB()).substring(2);
        final String fgColorHighlight = Integer.toHexString(fgColor.getRGB()).substring(2);
        return new HighlightingNameFormatter(
            String.format(COLOR_FORMAT_PATTERN, bgColorHighlight, fgColorHighlight, "%s"),  //NOI18N
            caseSensitive);
    }

    @NonNull
    public static HighlightingNameFormatter createBoldFormatter(
            final boolean caseSensitive) {
        return new HighlightingNameFormatter(
            String.format(BOLD_FORMAT_PATTERN, "%s"),  //NOI18N
            caseSensitive);
    }

    @NonNull
    /*test*/ static HighlightingNameFormatter createCustomFormatter(
            @NonNull final String format,
            final boolean caseSensitive) {
        return new HighlightingNameFormatter(format, caseSensitive);
    }
}