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
package org.netbeans.modules.html.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nu.validator.htmlparser.impl.ElementName;
import nu.validator.htmlparser.impl.ErrorReportingTokenizer;
import nu.validator.htmlparser.impl.StackNode;
import nu.validator.htmlparser.impl.StateSnapshot;
import nu.validator.htmlparser.impl.Tokenizer;
import nu.validator.htmlparser.io.Driver;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import org.netbeans.editor.ext.html.parser.api.AstNodeFactory;
import org.netbeans.editor.ext.html.parser.api.HtmlVersion;
import org.netbeans.editor.ext.html.parser.api.ParseException;
import org.netbeans.editor.ext.html.parser.spi.DefaultHtmlParseResult;
import org.netbeans.editor.ext.html.parser.spi.HtmlModel;
import org.netbeans.editor.ext.html.parser.spi.HtmlParseResult;
import org.netbeans.editor.ext.html.parser.spi.HtmlParser;
import org.netbeans.editor.ext.html.parser.api.HtmlSource;
import org.netbeans.editor.ext.html.parser.api.ProblemDescription;
import org.netbeans.editor.ext.html.parser.spi.HtmlTag;
import org.netbeans.html.api.validation.ValidationContext;
import org.netbeans.html.api.validation.ValidationException;
import org.netbeans.html.api.validation.Validator;
import org.netbeans.html.api.validation.ValidatorService;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author marekfukala
 */
@ServiceProvider(service = HtmlParser.class, position = 100)
public class Html5Parser implements HtmlParser {

    private static final String PARSER_NAME = String.format("validator.nu html5 parser (%s).", Html5Parser.class); //NOI18N

    private static final HtmlModel HTML5MODEL = new Html5Model();
    
    public HtmlParseResult parse(HtmlSource source, HtmlVersion preferedVersion, Lookup lookup) throws ParseException {
        try {
            String code = source.getSourceCode().toString();
            InputSource is = new InputSource(new StringReader(code));
            final AstNodeTreeBuilder treeBuilder = new AstNodeTreeBuilder(AstNodeFactory.shared().createRootNode(0, code.length()));
            final Tokenizer tokenizer = new ErrorReportingTokenizer(treeBuilder);
            final Collection<ProblemDescription> problems = new ArrayList<ProblemDescription>();

            treeBuilder.setErrorHandler(new ErrorHandler() {

                public void warning(SAXParseException exception) throws SAXException {
                    handleProblem(exception, ProblemDescription.WARNING);
                }

                public void error(SAXParseException exception) throws SAXException {
                    handleProblem(exception, ProblemDescription.ERROR);
                }

                public void fatalError(SAXParseException exception) throws SAXException {
                    handleProblem(exception, ProblemDescription.FATAL);
                }

                private void handleProblem(SAXException exception, int problemType) {
//                     AstNode current = treeBuilder.getCurrentNode();

                    ProblemDescription problem = ProblemDescription.create("nokey", //NOI18N
                            exception.getLocalizedMessage(),
                            problemType,
                            //                            current.startOffset(), current.endOffset());
                            treeBuilder.tagBeginningOffset(), treeBuilder.tagEndOffset());

                    problems.add(problem);
                }
            });
            Driver driver = new Driver(tokenizer);
            driver.tokenize(is);
            AstNode root = treeBuilder.getRoot();

            //html 5 source are validated by the validator.nu,
            //if there's no such validator available, use the errors from the parser itself
            if (preferedVersion == HtmlVersion.HTML5) {
                Validator html5validator = ValidatorService.getValidator(preferedVersion);
                if (html5validator != null) {
                    ValidationContext context = new ValidationContext(code, preferedVersion, source.getSourceFileObject());
                    try {
                        Collection<ProblemDescription> validatorProblems = html5validator.validate(context).getProblems();
                        return new Html5ParserResult(source, root, validatorProblems, preferedVersion);
                    } catch (ValidationException ex) {
                        Logger.getLogger(Html5Parser.class.getName()).log(Level.WARNING,
                                "Error during validating file " + source.getSourceFileObject(),
                                ex);
                    }
                }
            }

            return new Html5ParserResult(source, root, problems, preferedVersion);

        } catch (SAXException ex) {
            throw new ParseException(ex);
        } catch (IOException ex) {
            throw new ParseException(ex);
        }
    }

    public boolean canParse(HtmlVersion version) {
        return version == HtmlVersion.HTML5
                || version == HtmlVersion.HTML32
                || version == HtmlVersion.HTML41_STRICT
                || version == HtmlVersion.HTML41_TRANSATIONAL
                || version == HtmlVersion.HTML41_FRAMESET
                || version == HtmlVersion.HTML40_STRICT
                || version == HtmlVersion.HTML40_TRANSATIONAL
                || version == HtmlVersion.HTML40_FRAMESET
                || version == HtmlVersion.XHTML10_STICT
                || version == HtmlVersion.XHTML10_TRANSATIONAL
                || version == HtmlVersion.XHTML10_FRAMESET
                || version == HtmlVersion.XHTML11;
    }

    public HtmlModel getModel(HtmlVersion version) {
        assert version == HtmlVersion.HTML5;
        return HTML5MODEL;
    }

    public String getName() {
        return PARSER_NAME;
    }

    public static StateSnapshot makeTreeBuilderSnapshot(AstNode node) {
        int treeBuilderState = node.treeBuilderState;
        List<StackNode> stack = new ArrayList<StackNode>();
        while (node != null && !node.isRootNode()) {
            stack.add(0, new StackNode("http://www.w3.org/1999/xhtml", (ElementName) node.elementName, node));
            node = node.parent();
        }

        StateSnapshot snapshot = new StateSnapshot(stack.toArray(new StackNode[]{}),
                new StackNode[]{}, null, treeBuilderState);
        return snapshot;
    }

    private static class Html5ParserResult extends DefaultHtmlParseResult {

        public Html5ParserResult(HtmlSource source, AstNode root, Collection<ProblemDescription> problems, HtmlVersion version) {
            super(source, root, problems, version);
        }

        public HtmlModel model() {
            return HTML5MODEL;
        }

        public Collection<HtmlTag> getPossibleTagsInContext(AstNode node, boolean type) {
            Collection<HtmlTag> possible = new LinkedHashSet<HtmlTag>();
            if (type) {
                //open tags
                StateSnapshot snapshot = makeTreeBuilderSnapshot(node);
                ReinstatingTreeBuilder builder = ReinstatingTreeBuilder.create(snapshot);

                HashMap<Integer, Boolean> enabledGroups = new HashMap<Integer, Boolean>();
                for (ElementName element : ElementName.ELEMENT_NAMES) {
                    int group = element.group;
                    Boolean enabled = enabledGroups.get(group);

                    if (enabled == null) {
                        //not checked yet

                        //XXX is it even correct to assume that the result
                        //will be the same for all members of one group????

//                        System.out.print("element " + element + "...");
                        enabled = builder.canFollow(node, element);
//                        System.out.println(enabled ? "+" : "-");
                        enabledGroups.put(group, enabled);

                        if (enabled.booleanValue()) {
                            //add all element from the group as possible
                            for (ElementName member : ElementNames.getElementForTreeBuilderGroup(group)) {
                                possible.add(HtmlTagProvider.getTagForElement(member.name));
                            }

                        }

                    }

                }


            } else {
                //end tags
                do {
                    ElementName element = (ElementName) node.elementName;
                    if (element != null) {
                        //TODO if(element is not empty tag (end tag forbidden) {
                        possible.add(HtmlTagProvider.getTagForElement(element.name));
                        //}
                    }
                } while ((node = node.parent()) != null && !node.isRootNode());

            }

            return possible;
        }
        
    }

    private static final class Html5Model implements HtmlModel {

        private static Collection<HtmlTag> ALL_TAGS;

        public synchronized Collection<HtmlTag> getAllTags() {
            if (ALL_TAGS == null) {
                ALL_TAGS = new ArrayList<HtmlTag>();
                for (ElementName element : ElementName.ELEMENT_NAMES) {
                    ALL_TAGS.add(HtmlTagProvider.getTagForElement(element.name));
                }
            }
            return ALL_TAGS;
        }

        public HtmlTag getTag(String tagName) {
            return HtmlTagProvider.getTagForElement(tagName);
        }
    }
    
}
