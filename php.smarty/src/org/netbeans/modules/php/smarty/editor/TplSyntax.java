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
package org.netbeans.modules.php.smarty.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.openide.util.Parameters;

/**
 * Holds information about syntax and block of the Smarty templating engine. These information are used on various
 * places like indenter, parser etc.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class TplSyntax {

    /**
     * List of all tags which introduce block of code.
     */
    public static final List<String> BLOCK_TAGS = new ArrayList<String>(Arrays.asList(
            "block", //NOI18N
            "capture", //NOI18N
            "for", //NOI18N
            "foreach", //NOI18N
            "function", //NOI18N
            "if", //NOI18N
            "literal", //NOI18N
            "nocache", //NOI18N
            "php", //NOI18N
            "section", //NOI18N
            "setfilter", //NOI18N
            "strip", //NOI18N
            "while")); //NOI18N
    /**
     * List of all tags which are else-typed.
     */
    public static final List<String> ELSE_TAGS = new ArrayList<String>(Arrays.asList(
            "foreachelse", //NOI18N
            "elseif", //NOI18N
            "else", //NOI18N
            "sectionelse")); //NOI18N
    /**
     * Mapping of non-else tag to else-like tags. To every else tag must correspond at least one normal tag.
     */
    public static final HashMap<String, ArrayList<String>> RELATED_TAGS = new HashMap<String, ArrayList<String>>() {
        {
            put("if", new ArrayList<String>(Arrays.asList("else", "elseif"))); //NOI18N
            put("foreach", new ArrayList<String>(Arrays.asList("foreachelse"))); //NOI18N
            put("section", new ArrayList<String>(Arrays.asList("sectionelse"))); //NOI18N
        }
    };

    /**
     * Gets information whether the given command is block command.
     *
     * @param tag examined tag
     * @return {@code true} when the given tag is block tag, {@code false} otherwise
     */
    public static boolean isBlockCommand(String tag) {
        Parameters.notNull("tag", tag); //NOI18N
        String tokenText = tag.toLowerCase();
        return BLOCK_TAGS.contains(tokenText)
                || BLOCK_TAGS.contains(tokenText.substring(1))
                || ELSE_TAGS.contains(tokenText);
    }

    /**
     * Gets information whether the given command is "else-like" command.
     *
     * @param tag examined tag
     * @return {@code true} when the given tag is "else-like" tag, {@code false} otherwise
     */
    public static boolean isElseCommand(String tag) {
        Parameters.notNull("tag", tag); //NOI18N
        String tokenText = tag.toLowerCase();
        return ELSE_TAGS.contains(tokenText);
    }

    /**
     * Gets information whether the actual command is in relation to comparing command.
     *
     * @param actualTag examined tag
     * @param relatedToTag tag which is suspicious for relation
     * @return {@code true} when the tag is "else-like" or ending tag to the relatedToTag, {@code false} otherwise
     */
    public static boolean isInRelatedCommand(String actualTag, String relatedToTag) {
        Parameters.notNull("actualTag", actualTag); //NOI18N
        Parameters.notNull("relatedToTag", relatedToTag); //NOI18N
        return actualTag.substring(1).equals(relatedToTag)
                || (RELATED_TAGS.get(relatedToTag) != null && RELATED_TAGS.get(relatedToTag).contains(actualTag));
    }
}
