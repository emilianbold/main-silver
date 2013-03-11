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
package org.netbeans.modules.css.prep.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import static org.netbeans.modules.css.lib.api.NodeType.cp_variable_declaration;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.css.prep.CPType;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;

/**
 * naive temporary impl.
 *
 * @author marekfukala
 */
public class CPModel {

    public static String topLevelSnapshotMimetype; //set by unit tests
    private static final Collection<String> SASS_DIRECTIVES = Arrays.asList(new String[]{
        "@debug",
        "@each",
        "@extend",
        "@if",
        "@include",
        "@for",
        "@function",
        "@mixin",
        "@return",
        "@warn",
        "@while"
    });
    private CssParserResult result;
//    private Set<Element> vars;
    private CPType cpType;

    public static CPModel getModel(CssParserResult result) {
        CPModel curr = result.getProperty(CPModel.class);
        if (curr == null) {
            curr = new CPModel(result);
            result.setProperty(CPModel.class, curr);
        }
        return curr;
    }

    /**
     * Will run parsing task!.
     * 
     * XXX CPModel shouldn't be exposed outside of the parsing task :-(
     * 
     * @param file
     * @return 
     */
    public static CPModel getModel(FileObject file) throws ParseException {
        final AtomicReference<CPModel> model_ref = new AtomicReference<CPModel>();
        Source source = Source.create(file);
        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                ResultIterator cssRI = WebUtils.getResultIterator(resultIterator, "text/css");
                if(cssRI != null) {
                    CssParserResult result = (CssParserResult)cssRI.getParserResult();
                    if(result != null) {
                        model_ref.set(CPModel.getModel(result));
                    }
                }
            }
        });
        
        return model_ref.get();
    }
    
    private CPModel(CssParserResult result) {
        this.result = result;
    }

    public FileObject getFile() {
        return result.getSnapshot().getSource().getFileObject();
    }
    
    public CPType getPreprocessorType() {
        if (cpType == null) {
            String fileMimetype = topLevelSnapshotMimetype != null
                    ? topLevelSnapshotMimetype //unit tests - fileless snapshots
                    : result.getSnapshot().getSource().getFileObject().getMIMEType();
            if (fileMimetype == null) {
                cpType = CPType.NONE;
            } else {
                if ("text/less".equals(fileMimetype)) { //NOI18N
                    cpType = CPType.LESS;
                } else if ("text/scss".equals(fileMimetype)) { //NOI18N
                    cpType = CPType.SCSS;
                } else {
                    cpType = CPType.NONE;
                }
            }
        }
        return cpType;
    }
    
    /**
     * Gets both var and mixin elements.
     * @return 
     */
    public Collection<CPElement> getElements() {
        Collection<CPElement> all = new ArrayList<CPElement>();
        all.addAll(getVariables());
        all.addAll(getMixins());
        return all;
    }
    
    /**
     * Gets a collection of variables accessible(visible) at the given location.
     * 
     * @param offset
     * @return 
     */
    public Collection<CPElement> getVariables(int offset) {
        Collection<CPElement> visible = new ArrayList<CPElement>();
        for(CPElement var : getVariables()) {
            OffsetRange context = var.getScope();
            if(context == null || context.containsInclusive(offset)) {
                visible.add(var);
            }
        }
        return visible;
    }
        
    /**
     * Gets all variables from this file's model.
     * 
     * @return 
     */
    public Collection<CPElement> getVariables() {
        final Collection<CPElement> vars = new ArrayList<CPElement>();
        NodeVisitor visitor = new NodeVisitor() {
            private boolean in_cp_variable_declaration, in_cp_args_list, in_declarations;
            private Stack<OffsetRange> contexts = new Stack<OffsetRange>();
            
            @Override
            public boolean visit(Node node) {
                
                switch (node.type()) {
                    case declarations:
                        OffsetRange range = new OffsetRange(node.from(), node.to());
                        contexts.push(range);
                        //the declarations node represents a content of a code block
                        in_declarations = true;
                        
                        _visitChildren(this, node);
                        
                        contexts.pop();
                        in_declarations = false;
                        break;

                    case cp_variable_declaration:
                        in_cp_variable_declaration = true;
                        
                        _visitChildren(this, node);
                        
                        in_cp_variable_declaration = false;
                        break;

                    case cp_args_list:
                        in_cp_args_list = true;

                        _visitChildren(this, node);
                        
                        in_cp_args_list = false;
                        break;

                    case cp_variable:
                        //determine the variable type
                        CPElementType type;
                        if (in_cp_args_list) {
                            type = CPElementType.VARIABLE_DECLARATION_MIXIN_PARAMS;
                        } else {
                            if (in_cp_variable_declaration) {
                                if (in_declarations) {
                                    type = CPElementType.VARIABLE_LOCAL_DECLARATION;
                                } else {
                                    type = CPElementType.VARIABLE_GLOBAL_DECLARATION;
                                }
                            } else {
                                type = CPElementType.VARIABLE_USAGE;
                            }
                        }
                        OffsetRange context = contexts.isEmpty() ? null : contexts.peek();
                        
                        CPElementHandle handle = new CPElementHandle(getFile(), node.image().toString().trim(), type);
                        OffsetRange variableRange = new OffsetRange(node.from(), node.to());
                        CPElement element = new CPElement(handle, variableRange, context);
                        
                        vars.add(element);
                        break;

                    case token:
                        //ignore toke nodes
                        break;
                        
                    default:
                        _visitChildren(this, node);

                }
                return false;
            }
        };
        visitor.visit(result.getParseTree());
        return vars;
    }

    private static void _visitChildren(NodeVisitor visitor, Node node) {
        List<Node> children = node.children();
        if (children != null) {
            for (Node child : children) {
                visitor.visit(child);
            }
        }
    }

    //XXX mixin usages!
    public Collection<CPElement> getMixins() {
        final Collection<CPElement> mixins = new HashSet<CPElement>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public boolean visit(Node node) {
                switch (node.type()) {
                    case cp_mixin_declaration:
                        Node mixin_name = NodeUtil.getChildByType(node, NodeType.cp_mixin_name);
                        if (mixin_name != null) {
                            
                           CPElementHandle handle = new CPElementHandle(getFile(), mixin_name.image().toString().trim(), CPElementType.MIXIN_DECLARATION);
                           OffsetRange variableRange = new OffsetRange(mixin_name.from(), mixin_name.to());
                           OffsetRange scope = null; //TODO implement!
                           CPElement element = new CPElement(handle, variableRange, scope);
                           mixins.add(element);
                        }
                        break;
                    default:
                        //visit children
                        List<Node> children = node.children();
                        if (children != null) {
                            for (Node child : children) {
                                visit(child);
                            }
                        }
                        break;
                }
                return false;
            }
        };
        visitor.visit(result.getParseTree());
        return mixins;
    }

    public Collection<String> getMixinNames() {
        return getElementNames(getMixins());
    }

    public Collection<String> getVarNames() {
        return getElementNames(getVariables());
    }

    private static Collection<String> getElementNames(Collection<? extends CPElement> elements) {
        Collection<String> names = new HashSet<String>();
        for (CPElement e : elements) {
            names.add(e.getName().toString());
        }
        return names;
    }

    public Collection<String> getDirectives() {
        switch (getPreprocessorType()) {
            case SCSS:
                return SASS_DIRECTIVES;
            default:
                //nothing
                return Collections.emptyList();
        }
    }
}
