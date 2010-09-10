/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.editor.ext.html.parser.api;

import org.netbeans.editor.ext.html.parser.spi.AstNodeVisitor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.editor.ext.html.dtd.DTD;

/**
 *
 * @author marek
 */
public class AstNodeUtils {

    private static final String INDENT = "   ";


    public static String dumpTree(AstNode node) {
        return dumpTree(node, (CharSequence)null);
    }

    public static String dumpTree(AstNode node, CharSequence source) {
        StringBuffer buf = new StringBuffer();
        dumpTree(node, buf, source);
        System.out.println(buf.toString());
        return buf.toString();
    }

    public static void dumpTree(AstNode node, StringBuffer buf) {
        dumpTree(node, buf, null);
    }

    public static void dumpTree(AstNode node, StringBuffer buf, CharSequence source) {
        dump(node, "", buf, source);
    }

    private static void dump(AstNode node, String prefix, StringBuffer buf, CharSequence source) {
        buf.append(prefix);
        buf.append(node.toString());
        if(source != null && node.startOffset() != -1 && node.endOffset() != -1) {
            buf.append(" (");
            buf.append(source.subSequence(node.startOffset(), node.endOffset()));
            buf.append(")");
        }
        buf.append('\n');
        for (AstNode child : node.children()) {
            dump(child, prefix + INDENT, buf, source);
        }
    }

    public static AstNode getRoot(AstNode node) {
        for (;;) {
            if (node.parent() == null) {
                return node;
            } else {
                node = node.parent();
            }
        }
    }

    /** Returns a list of all ancestors of the given node matching the filter.
     * Closest ancestors are at the beginning of the list.
     */
    public static List<AstNode> getAncestors(AstNode node, AstNode.NodeFilter filter) {
        List<AstNode> matching = new ArrayList<AstNode>();
        AstNode n = node;
        do {
            if (filter.accepts(n)) {
                matching.add(n);
            }

            n = n.parent();
        } while (n != null);

        return matching;
    }

    public static List<AstNode> getChildrenRecursivelly(AstNode node, AstNode.NodeFilter filter, boolean recurseOnlyMatching) {
        List<AstNode> matching = new ArrayList<AstNode>();
        getChildrenRecursivelly(matching, node, filter, recurseOnlyMatching);
        return matching;
    }

    private static void getChildrenRecursivelly(List<AstNode> found, AstNode node, AstNode.NodeFilter filter, boolean recurseOnlyMatching) {
        for (AstNode child : node.children()) {
            if (filter.accepts(child)) {
                found.add(child);
                getChildrenRecursivelly(found, child, filter, recurseOnlyMatching);
            } else {
                if (!recurseOnlyMatching) {
                    getChildrenRecursivelly(found, child, filter, recurseOnlyMatching);
                }
            }
        }
    }

    /**
     * searches the logical node ranges!
     */
    public static AstNode findDescendant(AstNode node, int astOffset) {
        return findDescendant(node, astOffset, false);
    }

    /**
     * searches the logical node ranges!
     */
    public static AstNode findDescendant(AstNode node, int astOffset, boolean exclusiveStartOffset) {
        int[] nodeRange = node.getLogicalRange();

        int so = nodeRange[0];
        int eo = nodeRange[1];

        if(!node.isVirtual()) {

            if (astOffset < so || astOffset > eo) {
                //we are out of the scope - may happen just with the first client call
                return node;
            }

            if (exclusiveStartOffset) {
                so++;
            }

            if (astOffset >= so && astOffset < eo && node.children().isEmpty()) {
                //if the node matches and has no children we found it
                return node;
            }

        }

        for (AstNode child : node.children()) {
            if(child.isVirtual()) {
//                return findDescendant(child, astOffset, exclusiveStartOffset);
                AstNode candidate = findDescendant(child, astOffset, exclusiveStartOffset);
                if(candidate != null) {
                    return candidate;
                }
            } else {

                int[] childNodeRange = child.getLogicalRange();
                int ch_so = childNodeRange[0];

                if (exclusiveStartOffset) {
                    ch_so++;
                }

                int ch_eo = childNodeRange[1];
                if (astOffset >= ch_so && astOffset < ch_eo) {
                    //the child is or contains the searched node
                    return findDescendant(child, astOffset, exclusiveStartOffset);
                }
            }

        }

//        return node;
         return node.isVirtual() ? null : node;
    }

    /**
     *
     * <div>|</div> ... forward == true will return <div> false </div>
     * 
     * @param node
     * @param astOffset
     * @param forward
     * @return
     */
    public static AstNode findDescendantTag(AstNode node, int astOffset, boolean useLogicalRanges, boolean forward) {
        int so = useLogicalRanges ? node.logicalStartOffset() : node.startOffset();
        int eo = useLogicalRanges ? node.logicalEndOffset() : node.endOffset();


        //if the node matches and has no children we found it
        if (forward) {
            if (astOffset >= so && astOffset < eo && node.children().isEmpty()) {
                return node;
            }
        } else {
            if (astOffset > so && astOffset <= eo && node.children().isEmpty()) {
                return node;
            }
        }

        for (AstNode child : node.children()) {
            int ch_so = child.logicalStartOffset();
            int ch_eo = child.logicalEndOffset();
            if (forward) {
                if (astOffset >= ch_so && astOffset < ch_eo) {
                    if(astOffset < child.endOffset()) {
                        return child;
                    }
                    //the child is or contains the searched node
                    AstNode n =  findDescendantTag(child, astOffset, useLogicalRanges, forward);
                    if(n != null) {
                        return n;
                    }
                }
            } else {
                if (astOffset > ch_so && astOffset <= ch_eo) {
                    if(astOffset <= child.endOffset()) {
                        return child;
                    }
                    //the child is or contains the searched node
                    AstNode n = findDescendantTag(child, astOffset, useLogicalRanges, forward);
                    if(n != null) {
                        return n;
                    }
                }
            }

        }

        return null;
    }

    public static AstNode getTagNode(AstNode node, int astOffset) {
        if (node.type() == AstNode.NodeType.OPEN_TAG) {
            if (astOffset >= node.startOffset() && astOffset < node.endOffset()) {
                //the offset falls directly to the tag
                return node;
            }

            AstNode match = node.getMatchingTag();
            if (match != null && match.type() == AstNode.NodeType.ENDTAG) {
                //end tag is possibly the searched node
                if (astOffset >= match.startOffset() && astOffset < match.endOffset()) {
                    return match;
                }
            }

            //offset falls somewhere inside the logical range but outside of
            //the open or end tag ranges.
            return null;
        }

        return node;
    }

    public static AstNode query(AstNode base, String path) {
        return query(base, path, false);
    }

    /** find an AstNode according to the path
     * example of path: html/body/table|2/tr -- find a second table tag in body tag
     *
     * note: queries OPEN TAGS ONLY!
     */
    public static AstNode query(AstNode base, String path, boolean caseInsensitive) {
        StringTokenizer st = new StringTokenizer(path, "/");
        AstNode found = base;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            int indexDelim = token.indexOf('|');

            String nodeName = indexDelim >= 0 ? token.substring(0, indexDelim) : token;
            if (caseInsensitive) {
                nodeName = nodeName.toLowerCase(Locale.ENGLISH);
            }
            String sindex = indexDelim >= 0 ? token.substring(indexDelim + 1, token.length()) : "0";
            int index = Integer.parseInt(sindex);

            int count = 0;
            AstNode foundLocal = null;
            for (AstNode child : found.children()) {
                String childName = child.name();
                if (child.type() == AstNode.NodeType.OPEN_TAG && (caseInsensitive ? childName = childName.toLowerCase(Locale.ENGLISH) : childName).equals(nodeName) && count++ == index) {
                    foundLocal = child;
                    break;
                }
            }
            if (foundLocal != null) {
                found = foundLocal;

                if (!st.hasMoreTokens()) {
                    //last token, we may return
                    assert found.name().equals(nodeName);
                    return found;
                }

            } else {
                return null; //no found
            }
        }

        return null;
    }

    public static Collection<AstNode> getPossibleEndTagElements(AstNode leaf) {
        Collection<AstNode> possible = new LinkedList<AstNode>();

        for (;;) {
            if (leaf.type() == AstNode.NodeType.ROOT) {
                break;
            }

            //if dtd element and doesn't have forbidden end tag
            if ((leaf.getDTDElement() == null || !AstNodeUtils.hasForbiddenEndTag(leaf))
                    && leaf.type() == AstNode.NodeType.OPEN_TAG) {

                possible.add(leaf);

                //check if the tag needs to have a matching tag and if is matched already
                if (leaf.needsToHaveMatchingTag() && leaf.getMatchingTag() == null) {
                    //if not, any of its parent cannot be closed here
                    break;
                }
            }
            leaf = leaf.parent();
            assert leaf != null;
        }
        return possible;
    }


    public static Collection<DTD.Element> getPossibleOpenTagElements(AstNode node) {
        return getPossibleOpenTagElements(node.getRootNode(), node.endOffset());

    }

    public static Collection<DTD.Element> getPossibleOpenTagElements(AstNode root, int astPosition) {
        HashSet<DTD.Element> elements = new HashSet<DTD.Element>();

        assert root.type() == AstNode.NodeType.ROOT;

        //exlusive to start offset so |<div> won't return the div tag but <|div> will
        AstNode leafNodeForPosition = AstNodeUtils.findDescendant(root, astPosition, true);

        //search first dtd element node in the tree path
        while (leafNodeForPosition.getDTDElement() == null &&
                leafNodeForPosition.type() != AstNode.NodeType.ROOT) {
            leafNodeForPosition = leafNodeForPosition.parent();
        }

        assert leafNodeForPosition != null;

        //root allows all dtd elements
        if (leafNodeForPosition == root) {
            return root.getAllPossibleElements();
        }

        //check if the ast offset falls into the node range (not logical range!!!)
        if (leafNodeForPosition.startOffset() <= astPosition && leafNodeForPosition.endOffset() > astPosition) {
            //if so return empty list - nothing is allowed inside tag content
            return Collections.EMPTY_LIST;
        }

        assert leafNodeForPosition.type() == AstNode.NodeType.OPEN_TAG; //nothing else than open tag can contain non-tag content

        DTD.ContentModel contentModel = leafNodeForPosition.getDTDElement().getContentModel();
        DTD.Content content = contentModel.getContent();
        //resolve all preceding siblings before the astPosition
        Collection<DTD.Element> childrenBefore = new ArrayList<DTD.Element>();
        for (AstNode sibling : leafNodeForPosition.children()) {
            if (sibling.startOffset() >= astPosition) {
                //process only siblings before the offset!
                break;
            }
            if (sibling.type() == AstNode.NodeType.OPEN_TAG) {
                DTD.Content subcontent = content.reduce(sibling.getDTDElement().getName());
                if (subcontent != null) {
                    //sibling reduced - update the content to the resolved one
                    if(content == subcontent) {
                        //the content is reduced to itself
                    } else {
                        content = subcontent;
                        childrenBefore.add(sibling.getDTDElement());
                    }
                } else {
                    //the siblibg doesn't reduce the content - it is unallowed there - ignore it
                }
            }
        }

        if (!leafNodeForPosition.needsToHaveMatchingTag()) {
            //optional end, we need to also add results for the situation
            //the node is automatically closed - which is before the node start

            //but do not do that on the root level
            if (leafNodeForPosition.parent().type() != AstNode.NodeType.ROOT) {
                Collection<DTD.Element> elementsBeforeLeaf = getPossibleOpenTagElements(root, leafNodeForPosition.startOffset());
                //remove all elements which has already been reduced before
                elementsBeforeLeaf.removeAll(childrenBefore);
                elements.addAll(elementsBeforeLeaf);
            }
        }

//      elements.addAll(content.getPossibleElements());
        addAllPossibleElements(elements, content.getPossibleElements());

        //process includes/excludes from the root node to the leaf
        List<AstNode> path = new ArrayList<AstNode>();
        for(AstNode node = leafNodeForPosition; node.type() != AstNode.NodeType.ROOT; node = node.parent()) {
            path.add(0, node);
        }
        for(AstNode node : path) {
            DTD.ContentModel cModel = node.getDTDElement().getContentModel();
            elements.addAll(cModel.getIncludes());
            elements.removeAll(cModel.getExcludes());
        }

        return elements;
    }

//    public static Collection<DTD.Element> getPossibleOpenTagElements(AstNode leafNodeForPosition) {
//        HashSet<DTD.Element> elements = new HashSet<DTD.Element>();
//
//        int astPosition = leafNodeForPosition.startOffset();
//        AstNode root = leafNodeForPosition.getRootNode();
//
//        //search first dtd element node in the tree path
//        while (leafNodeForPosition.getDTDElement() == null &&
//                leafNodeForPosition.type() != AstNode.NodeType.ROOT) {
//            leafNodeForPosition = leafNodeForPosition.parent();
//        }
//
//        assert leafNodeForPosition != null;
//
//        //root allows all dtd elements
//        if (leafNodeForPosition == root) {
//            return root.getAllPossibleElements();
//        }
//
//
//        assert leafNodeForPosition.type() == AstNode.NodeType.OPEN_TAG; //nothing else than open tag can contain non-tag content
//
//        DTD.ContentModel contentModel = leafNodeForPosition.getDTDElement().getContentModel();
//        DTD.Content content = contentModel.getContent();
//        //resolve all preceding siblings before the astPosition
//        Collection<DTD.Element> childrenBefore = new ArrayList<DTD.Element>();
//        for (AstNode sibling : leafNodeForPosition.children()) {
//            if (sibling.startOffset() >= astPosition) {
//                //process only siblings before the offset!
//                break;
//            }
//            if (sibling.type() == AstNode.NodeType.OPEN_TAG) {
//                DTD.Content subcontent = content.reduce(sibling.getDTDElement().getName());
//                if (subcontent != null) {
//                    //sibling reduced - update the content to the resolved one
//                    if(content == subcontent) {
//                        //the content is reduced to itself
//                    } else {
//                        content = subcontent;
//                        childrenBefore.add(sibling.getDTDElement());
//                    }
//                } else {
//                    //the siblibg doesn't reduce the content - it is unallowed there - ignore it
//                }
//            }
//        }
//
//        if (!leafNodeForPosition.needsToHaveMatchingTag()) {
//            //optional end, we need to also add results for the situation
//            //the node is automatically closed - which is before the node start
//
//            //but do not do that on the root level
//            if (leafNodeForPosition.parent().type() != AstNode.NodeType.ROOT) {
//                Collection<DTD.Element> elementsBeforeLeaf = getPossibleOpenTagElements(root, leafNodeForPosition.startOffset());
//                //remove all elements which has already been reduced before
//                elementsBeforeLeaf.removeAll(childrenBefore);
//                elements.addAll(elementsBeforeLeaf);
//            }
//        }
//
////      elements.addAll(content.getPossibleElements());
//        addAllPossibleElements(elements, content.getPossibleElements());
//
//        //process includes/excludes from the root node to the leaf
//        List<AstNode> path = new ArrayList<AstNode>();
//        for(AstNode node = leafNodeForPosition; node.type() != AstNode.NodeType.ROOT; node = node.parent()) {
//            path.add(0, node);
//        }
//        for(AstNode node : path) {
//            DTD.ContentModel cModel = node.getDTDElement().getContentModel();
//            elements.addAll(cModel.getIncludes());
//            elements.removeAll(cModel.getExcludes());
//        }
//
//        return elements;
//    }

    private static void addAllPossibleElements(Set<DTD.Element> result, Collection<DTD.Element> elements) {
        for(DTD.Element element : elements) {
            result.add(element);
            if(element.hasOptionalStart()) {
                addAllPossibleElements(result, element.getContentModel().getContent().getPossibleElements());
            }
        }
    }

    public static boolean hasForbiddenEndTag(AstNode node) {
        return node.getDTDElement() != null ? node.getDTDElement().isEmpty() : false;
    }

    public static void visitChildren(AstNode node, AstNodeVisitor visitor, AstNode.NodeType nodeType) {
        for (AstNode n : node.children()) {
            if (nodeType == null || n.type() == nodeType) {
                visitor.visit(n);
            }
            visitChildren(n, visitor, nodeType);
        }
    }

    public static void visitChildren(AstNode node, AstNodeVisitor visitor) {
        visitChildren(node, visitor, null);
    }

    public static void visitAncestors(AstNode node, AstNodeVisitor visitor) {
        AstNode parent = (AstNode) node.parent();
        if (parent != null) {
            visitor.visit(parent);
            visitAncestors(parent, visitor);
        }
    }

    /** finds closest physical preceeding node to the offset */
    public static AstNode getClosestNodeBackward(AstNode context, int offset, AstNode.NodeFilter filter) {
        for (AstNode child : context.children(filter)) {
            if (child.startOffset >= offset) {
                return context;
            }
            context = getClosestNodeBackward(child, offset, filter);
        }
        return context;
    }

}
    
