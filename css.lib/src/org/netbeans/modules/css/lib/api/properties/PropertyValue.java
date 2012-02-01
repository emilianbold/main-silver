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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib.api.properties;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.css.lib.properties.*;

/**
 *
 * @author mfukala@netbeans.org
 */
public class PropertyValue {

    private String value;
    private GroupGrammarElement groupGrammarElement;
    private GrammarResolver grammarResolver;
    private static final Pattern FILTER_COMMENTS_PATTERN = Pattern.compile("/\\*.*?\\*/");//NOI18N
    private Node simpleParseTree, fullParseTree;

    public PropertyValue(PropertyModel property, String value) {
        this(property.getGrammar(), property.getPropertyName(), filterComments(value));
    }
   
    public PropertyValue(String grammar, String propertyName, String value) {
        this(GrammarParser.parse(grammar, propertyName), value);
    }

    public PropertyValue(GroupGrammarElement groupGrammarElement, String value) {
        this.groupGrammarElement = groupGrammarElement;
        this.value = value;
        this.grammarResolver = GrammarResolver.resolve(groupGrammarElement, value);
    }
    
     //tests only>>>
    public PropertyValue(String grammar, String value) {
        this(GrammarParser.parse(grammar), value);
    }//<<<

    public GroupGrammarElement getGroupGrammarElement() {
        return groupGrammarElement;
    }
    
    public String getValue() {
        return value;
    }

    public List<Token> getTokens() {
        return grammarResolver.tokens();
    }
    
    public List<Token> getUnresolvedTokens() {
        return grammarResolver.left();
    }

    public List<ResolvedToken> getResolvedTokens() {
        return grammarResolver.resolved();
    }
    
    public synchronized Node getFullParseTree() {
        if(fullParseTree == null) {
            fullParseTree = generateParseTree(true);
        }
        return fullParseTree;
    }
    
    public Node getSimpleParseTree() {
        if(simpleParseTree == null) {
            simpleParseTree = generateParseTree(false);
        }
        return simpleParseTree;
    }

    public boolean isResolved() {
        return grammarResolver.success();
    }
    
    public Set<ValueGrammarElement> getAlternatives() {
        return grammarResolver.getAlternatives();
    }
    
    //tests
    GrammarResolver getGrammarResolver() {
        return grammarResolver;
    }

    private static String filterComments(String text) {
        Matcher m = FILTER_COMMENTS_PATTERN.matcher(text);
        StringBuilder b = new StringBuilder(text);
        while (m.find()) {
            int from = m.start();
            int to = m.end();
            if (from != to) {
                char[] spaces = new char[to - from];
                Arrays.fill(spaces, ' ');
                String replacement = new String(spaces);
                b.replace(from, to, replacement);
            }
        }
        return b.toString();
    }

    /**
     * 
     * @param fullParseTree - if true then the parse tree contains also the anonymous
     * group nodes. If false the parse tree contains only named nodes (references)
     * 
     */
    private Node generateParseTree(final boolean fullParseTree) {
        Node.GroupNode root = new Node.GroupNode(getGroupGrammarElement()) {

            @Override
            public String toString() {
                return fullParseTree ? super.toString() : group.getName();
            }
            
        };
        
        for(ResolvedToken token : getResolvedTokens()) {
            Node.GroupNode current = root; //each path starts with the root element
            List<GrammarElement> path = token.getGrammarElement().elementsPath();
            //create group nodes for the elements excluding the root node and the value node itself
            for(GrammarElement element : path.subList(1, path.size() - 1)) {
                GroupGrammarElement groupElement = (GroupGrammarElement)element;
                if(!fullParseTree) {
                    if(groupElement.getName() == null) {
                        //referred element == null so skip the anonymous element
                        continue; 
                    }
                }
                
                Node.GroupNode newGroupNode = new Node.GroupNode(groupElement) {

                    @Override
                    public String toString() {
                        return fullParseTree ? super.toString() : group.getName();
                    }
                    
                };
                Node.GroupNode child = current.addChild(newGroupNode); //either returns the given node or an existing one equal to it.
                current = child;
            }
            current.addChild(new Node.ResolvedTokenNode(token)); //add the leaf node for the resolved token itself
        }
        
        return root;
    }

}
