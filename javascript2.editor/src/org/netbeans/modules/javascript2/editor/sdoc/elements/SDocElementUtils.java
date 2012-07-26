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
package org.netbeans.modules.javascript2.editor.sdoc.elements;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.javascript2.editor.jsdoc.model.AssignElement;
import org.netbeans.modules.javascript2.editor.jsdoc.model.DeclarationElement;
import org.netbeans.modules.javascript2.editor.jsdoc.model.DescriptionElement;
import org.netbeans.modules.javascript2.editor.jsdoc.model.LinkElement;
import org.netbeans.modules.javascript2.editor.jsdoc.model.NamePath;
import org.netbeans.modules.javascript2.editor.jsdoc.model.NamedParameterElement;
import org.netbeans.modules.javascript2.editor.jsdoc.model.ParameterElement;
import org.netbeans.modules.javascript2.editor.jsdoc.model.SimpleElement;
import org.netbeans.modules.javascript2.editor.jsdoc.model.UnnamedParameterElement;
import org.netbeans.modules.javascript2.editor.model.Type;
import org.netbeans.modules.javascript2.editor.model.impl.DocIdentifierImpl;
import org.netbeans.modules.javascript2.editor.model.impl.TypeImpl;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class SDocElementUtils {

    /**
     * Creates element of correct type for given type and remaining element text.
     * @param elementType element type
     * @param elementText tag description text - without the initial type and first spaces, can be empty but never {@code null}
     * @param elementTextStartOffset type description text start offset
     * @return created {@code SDocElement)
     */
    public static SDocElement createElementForType(SDocElement.Type elementType, String elementText, int elementTextStartOffset) {
//        switch (elementType.getCategory()) {
//            case ASSIGN:
//                String[] values = elementText.split("(\\s)*as(\\s)*"); //NOI18N
//                return AssignElement.create(
//                        elementType,
//                        (values.length > 0) ? new NamePath(values[0].trim()) : null,
//                        (values.length > 1) ? new NamePath(values[1].trim()) : null);
//            case DECLARATION:
//                return DeclarationElement.create(elementType, new TypeImpl(elementText, elementTextStartOffset));
//            case DESCRIPTION:
//                return DescriptionElement.create(elementType, elementText);
//            case LINK:
//                return LinkElement.create(elementType, new NamePath(elementText));
//            case NAMED_PARAMETER:
//                return createParameterElement(elementType, elementText, true, elementTextStartOffset);
//            case SIMPLE:
//                return SimpleElement.create(elementType);
//            case UNNAMED_PARAMETER:
//                return createParameterElement(elementType, elementText, false, elementTextStartOffset);
//            default:
//                // unknown jsDoc element type
//                return DescriptionElement.create(elementType, elementText);
//        }
        return null;
    }

//    /**
//     * Gets list of {@link Type}s parsed from given string.
//     * @param typesString string to be parsed for types
//     * @param offset offset of the typesString in the file
//     * @return list of {@code type}s
//     */
//    public static List<Type> parseTypes(String typesString, int offset) {
//        List<Type> types = new LinkedList<Type>();
//        String[] typesArray = typesString.split("[|]"); //NOI18N
//        for (String string : typesArray) {
//            types.add(new TypeImpl(string, offset + typesString.indexOf(string)));
//        }
//        return types;
//    }
//
//    private static ParameterElement createParameterElement(SDocElement.Type elementType,
//            String elementText, boolean named, int descStartOffset) {
//        int typeOffset = -1, nameOffset = -1;
//        String types = "", desc = ""; //NOI18N
//        StringBuilder name = new StringBuilder();
//        int process = 0;
//        String[] parts = elementText.split("[\\s]+"); //NOI18N
//
//        if (parts.length > process) {
//            // get type value if any
//            if (parts[0].startsWith("{")) { //NOI18N
//                typeOffset = descStartOffset + 1;
//                int rparIndex = parts[0].indexOf("}"); //NOI18N
//                if (rparIndex == -1) {
//                    types = parts[0].trim();
//                } else {
//                    types = parts[0].substring(1, rparIndex);
//                }
//                process++;
//            }
//
//            // get name value (mandatory part)
//            if (parts.length > process && named) {
//                nameOffset = descStartOffset + elementText.indexOf(parts[process]);
//                name.append(parts[process].trim());
//                process++;
//                if (name.toString().contains("\"") || name.toString().contains("'")) { //NOI18N
//                    process = buildNameForString(name, process, parts);
//                }
//            }
//
//            // get description
//            StringBuilder sb = new StringBuilder();
//            while (process < parts.length) {
//                sb.append(parts[process]).append(" "); //NOI18N
//                process++;
//            }
//            desc = sb.toString().trim();
//        }
//
//        if (named) {
//            return NamedParameterElement.createWithNameDiagnostics(elementType,
//                    new DocIdentifierImpl(name.toString(), nameOffset), parseTypes(types, typeOffset), desc);
//        } else {
//            return UnnamedParameterElement.create(elementType, parseTypes(types, typeOffset), desc);
//        }
//    }
//
//    private static int buildNameForString(StringBuilder name, int currentOffset, String[] parts) {
//        // TODO - better would be to solve that using lexer
//        String nameString = name.toString();
//        if ((nameString.indexOf("\"") != -1 && (nameString.indexOf("\"") == nameString.lastIndexOf("\""))) //NOI18N
//                || (nameString.indexOf("'") != -1 && nameString.indexOf("'") == nameString.lastIndexOf("'"))) { //NOI18N
//            // string with spaces
//            boolean endOfString = false;
//            while (currentOffset < parts.length && !endOfString) {
//                name.append(" ").append(parts[currentOffset]); //NOI18N
//                if (parts[currentOffset].contains("\"") || parts[currentOffset].contains("'")) { //NOI18H
//                    endOfString = true;
//                }
//                currentOffset++;
//            }
//        }
//        return currentOffset;
//    }

}
