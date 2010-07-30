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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.editor.ext.html.parser;

import org.netbeans.editor.ext.html.parser.api.AstNode;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.editor.ext.html.parser.api.HtmlSource;

/**
 *
 * @author marekfukala
 */
public class XmlSyntaxTreeBuilder extends SyntaxTreeBuilder {

    public static AstNode makeUncheckedTree(HtmlSource source, List<SyntaxElement> elements) {

        assert elements != null : "passed elements list cannot but null"; //NOI18N

        int lastEndOffset = source.getSourceCode().length();

        //create a root node, it can contain one or more child nodes
        //normally just <html> node should be its child
        AstNode rootNode = AstNode.createRootNode(0, lastEndOffset, null);
        LinkedList<AstNode> stack = new LinkedList<AstNode>();
        stack.add(rootNode);

        for (SyntaxElement element : elements) {

            if (element.type() == SyntaxElement.TYPE_TAG) { //open tag
                assert element instanceof SyntaxElement.Tag;

                SyntaxElement.Tag tagElement = (SyntaxElement.Tag) element;
                String tagName = tagElement.getName();

                AstNode lastNode = stack.getLast();

                //create an AST node for current element
                AstNode openTagNode = new AstNode(tagName, AstNode.NodeType.OPEN_TAG,
                        tagElement.offset(), tagElement.offset() + tagElement.length(), tagElement.isEmpty());

                //add existing tag attributes
                setTagAttributes(openTagNode, tagElement);

                //possible add the node to the nodes stack
                if (!(tagElement.isEmpty())) {
                    stack.addLast(openTagNode);
                }

                //add the node to its parent
                lastNode.addChild(openTagNode);

            } else if (element.type() == SyntaxElement.TYPE_ENDTAG) { //close tag

                String tagName = ((SyntaxElement.Named) element).getName();

                AstNode closeTagNode = new AstNode(tagName, AstNode.NodeType.ENDTAG,
                        element.offset(), element.offset() + element.length(), false);

                int matched_index = -1;
                for (int i = stack.size() - 1; i >= 0; i--) {
                    AstNode node = stack.get(i);
                    if (tagName.equals(node.name())) {
                        //ok, match
                        matched_index = i;
                        break;
                    }
                }

                assert matched_index != 0; //never match root node, either -1 or > 0

                if (matched_index > 0) {
                    //something matched
                    AstNode match = stack.get(matched_index);

                    //remove them ALL the left elements from the stack
                    for (int i = stack.size() - 1; i > matched_index; i--) {
                        AstNode node = stack.get(i);
                        node.setLogicalEndOffset(closeTagNode.startOffset());
                        stack.remove(i);
                    }

                    //add the node to the proper parent
                    AstNode match_parent = stack.get(matched_index - 1);
                    match_parent.addChild(closeTagNode);

                    //wont' help GS at all, but should be ok
                    match.setMatchingNode(closeTagNode);
                    match.setLogicalEndOffset(closeTagNode.endOffset());
                    closeTagNode.setMatchingNode(match);

                    //remove the matched tag from stack
                    stack.removeLast();

                } else {
                    //add it to the last node
                    stack.getLast().addChild(closeTagNode);
                }

            } else {
                //rest of the syntax element types
                //XXX do we need to have these in the AST???

                // add a new AST node to the last node on the stack
//                AstNode.NodeType nodeType = intToNodeType(element.type());
//
//                AstNode node = new AstNode(null, nodeType, element.offset(),
//                        element.offset() + element.length(), false);
//
//                stack.getLast().addChild(node);
            }
        }

        //check the stack content and resolve left nodes
        for (int i = stack.size() - 1; i > 0; i--) { // (i > 0) == do not process the very first (root) node
            AstNode node = stack.get(i);
            node.setLogicalEndOffset(lastEndOffset);

        }

        return rootNode;
    }

}
