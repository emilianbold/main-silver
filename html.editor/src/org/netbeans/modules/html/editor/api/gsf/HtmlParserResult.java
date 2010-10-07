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
package org.netbeans.modules.html.editor.api.gsf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import org.netbeans.editor.ext.html.parser.api.ParseException;
import org.netbeans.editor.ext.html.parser.api.ProblemDescription;
import org.netbeans.editor.ext.html.parser.api.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.spi.AstNodeVisitor;
import org.netbeans.editor.ext.html.parser.api.SyntaxAnalyzerResult;
import org.netbeans.editor.ext.html.parser.spi.HtmlParseResult;
import org.netbeans.editor.ext.html.parser.spi.ParseResult;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.editor.ext.html.parser.api.HtmlVersion;
import org.netbeans.html.api.validation.ValidationContext;
import org.netbeans.html.api.validation.ValidationResult;
import org.netbeans.html.api.validation.ValidatorService;
import org.netbeans.modules.html.editor.gsf.HtmlParserResultAccessor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * HTML parser result
 *
 * @author mfukala@netbeans.org
 */
public class HtmlParserResult extends ParserResult {

    /**
     * Used as a key of a swing document to find a default fallback dtd.
     */
    public static final String FALLBACK_DTD_PROPERTY_NAME = "fallbackDTD";
    private static final String UNEXPECTED_TOKEN = "unexpected_token"; //NOI18N
    private SyntaxAnalyzerResult result;
    private List<Error> errors;
    private boolean isValid = true;

    private HtmlParserResult(SyntaxAnalyzerResult result) {
        super(result.getSource().getSnapshot());
        this.result = result;
    }

    public SyntaxAnalyzerResult getSyntaxAnalyzerResult() {
        return result;
    }

    /** The parser result may be invalidated by the parsing infrastructure.
     * In such case the method returns false.
     * @return true for valid result, false otherwise.
     */
    public boolean isValid() {
        return this.isValid;
    }

    /**
     * Returns an html version for the specified parser result input.
     * The return value depends on:
     * 1) doctype declaration content
     * 2) if not present, xhtml file extension
     * 3) if not xhtml extension, present of default XHTML namespace declaration
     *
     */
    public HtmlVersion getHtmlVersion() {
        return result.getHtmlVersion();
    }

    public HtmlVersion getDetectedHtmlVersion() {
        return result.getDetectedHtmlVersion();
    }

//    //kinda hacky method
//    public ParseResult getParseResultForRootTag(AstNode root) {
//        try {
//            if (!root.isRootNode()) {
//                throw new IllegalArgumentException("AstNode " + root + " is not a root node!"); //NOI18N
//            }
//            if (result.parseHtml().root() == root) {
//                return result.parseHtml();
//            }
//            if(result.parseUndeclaredEmbeddedCode().root() == root) {
//                return result.parseUndeclaredEmbeddedCode();
//            }
//
//            for(String ns : result.getAllDeclaredNamespaces().keySet()) {
//                ParseResult pr = result.parseEmbeddedCode(ns);
//                if(pr.root() == root) {
//                    return pr;
//                }
//            }
//
//        } catch (ParseException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//
//        throw new IllegalArgumentException("The AstNode " + root
//                + " doesn't belong to " + this + " HtmlParserResult!");//NOI18N
//    }
    /** @return a root node of the hierarchical parse tree of the document.
     * basically the tree structure is done by postprocessing the flat parse tree
     * you can get by calling elementsList() method.
     * Use the flat parse tree results if you do not need the tree structure since
     * the postprocessing takes some time and is done lazily.
     */
    public AstNode root() {
        try {
            return result.parseHtml().root();
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public AstNode rootOfUndeclaredTagsParseTree() {
        try {
            return result.parseUndeclaredEmbeddedCode().root();
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /** returns a parse tree for non-html content */
    public AstNode root(String namespace) {
        try {
            ParseResult pr = result.parseEmbeddedCode(namespace);
            assert pr != null : "Cannot get ParseResult for " + namespace; //NOI18N
            return pr.root();
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /** returns a map of all namespaces to astnode roots.*/
    public Map<String, AstNode> roots() {
        Map<String, AstNode> roots = new HashMap<String, AstNode>();
        for (String uri : getNamespaces().keySet()) {
            roots.put(uri, root(uri));
        }

        //non xhtml workaround, add the default namespaces if missing
        if (!roots.containsValue(root())) {
            roots.put(null, root());
        }

        return roots;

    }

    /**declared uri to prefix map */
    public Map<String, String> getNamespaces() {
        return result.getDeclaredNamespaces();
    }

    /** Returns a leaf most AstNode from the parse tree to which range the given
     * offset belongs. Uses logical node ranges.
     *
     * @param offset of the searched node
     */
    public AstNode findLeaf(int offset) {
        return findLeaf(offset, false);
    }

    public AstNode findLeaf(int offset, boolean exclusiveStartOffset) {
        //first try to find the leaf in html content
        AstNode mostLeaf = AstNodeUtils.findDescendant(root(), offset, exclusiveStartOffset);
        //now search the non html trees
        for (String uri : getNamespaces().keySet()) {
            AstNode root = root(uri);
            AstNode leaf = AstNodeUtils.findDescendant(root, offset, exclusiveStartOffset);
            if (mostLeaf == null) {
                mostLeaf = leaf;
            } else {
                //they cannot overlap, just be nested, at least I think
                if (leaf.logicalStartOffset() > mostLeaf.logicalStartOffset()) {
                    mostLeaf = leaf;
                }
            }
        }
        return mostLeaf;
    }

    /** Returns a leaf most AstNode from the parse tree to which range the given
     * offset belongs.
     *
     * @param offset of the searched node
     */
    public AstNode findLeafTag(int offset, boolean useLogicalRanges, boolean forward) {
        //first try to find the leaf in html content
        AstNode mostLeaf = AstNodeUtils.findDescendantTag(root(), offset, useLogicalRanges, forward);
        //now search the non html trees
        for (String uri : getNamespaces().keySet()) {
            AstNode root = root(uri);
            AstNode leaf = AstNodeUtils.findDescendantTag(root, offset, useLogicalRanges, forward);
            if (leaf == null) {
                continue;
            }
            if (mostLeaf == null) {
                mostLeaf = leaf;
            } else {
                //they cannot overlap, just be nested, at least I think
                if (leaf.logicalStartOffset() > mostLeaf.logicalStartOffset()) {
                    mostLeaf = leaf;
                }
            }
        }
        return mostLeaf;
    }

    @Override
    public synchronized List<? extends Error> getDiagnostics() {
        if (errors == null) {
            errors = new ArrayList<Error>();
            errors.addAll(getParseResultErrors());
            errors.addAll(extractErrorsFromAST());
            errors.addAll(findLexicalErrors());
//            errors.addAll(getValidationResults());
        }
        return errors;
    }

    @Override
    protected void invalidate() {
        this.isValid = false;
    }

//    private Collection<Error> getValidationResults() {
//
//        //use the filtered snapshot or use the namespaces filtering facility in the nu.validator
//        Collection<ValidationResult> results =
//                ValidatorService.getDefault().getValidators(new ValidationContext(getSnapshot().getText().toString(), getSnapshot().getSource().getFileObject()));
//
//
//        if (!results.isEmpty()) {
//
//            //XXX just use first for now
//            ValidationResult validatorResult = results.iterator().next();
//
//            if (!validatorResult.isSuccess()) {
//
//                Collection<Error> errs = new ArrayList<Error>();
//                for (ProblemDescription pd : validatorResult.getProblems()) {
//
//                    DefaultError error =
//                            new DefaultError(pd.getKey(),
//                            "nu.validator issue",
//                            pd.getText(),
//                            validatorResult.getContext().getFile(),
//                            pd.getFrom(),
//                            pd.getTo(),
//                            false /* not line error */,
//                            forProblemType(pd.getType()));
//                    errs.add(error);
//                }
//
//                return errs;
//
//
//
//            }
//        }
//
//        return Collections.emptyList();
//
//
//
//    }

    private Collection<Error> getParseResultErrors() {
        Collection<Error> diagnostics = new ArrayList<Error>();
        try {
            //collect problem descriptions from all embedded parse results
            for (ParseResult parseResult : result.getAllParseResults()) {
                for (ProblemDescription problem : parseResult.getProblems()) {
                    DefaultError error =
                            new DefaultError(problem.getKey(),
                            problem.getText(),
                            problem.getText(),
                            result.getSource().getSourceFileObject(),
                            problem.getFrom(),
                            problem.getTo(),
                            false /* not line error */,
                            forProblemType(problem.getType()));

                    diagnostics.add(error);
                }
            }
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyList();
        }

        return diagnostics;
    }

    private static Severity forProblemType(int problemtype) {
        switch (problemtype) {
            case ProblemDescription.INFORMATION:
            case ProblemDescription.WARNING:
                return Severity.WARNING;
            case ProblemDescription.ERROR:
            case ProblemDescription.FATAL:
                return Severity.ERROR;
            default:
                throw new IllegalArgumentException("Invalid ProblemDescription type: " + problemtype); //NOI18N
        }

    }

    private List<Error> findLexicalErrors() {
        TokenHierarchy th = getSnapshot().getTokenHierarchy();
        TokenSequence<HTMLTokenId> ts = th.tokenSequence(HTMLTokenId.language());
        if (ts == null) {
            return Collections.emptyList();
        }

        final List<Error> lexicalErrors = new ArrayList<Error>();
        ts.moveStart();
        while (ts.moveNext()) {
            if (ts.token().id() == HTMLTokenId.ERROR) {
                //some error in the node, report
                String msg = NbBundle.getMessage(HtmlParserResult.class, "MSG_UnexpectedToken", ts.token().text()); //NOI18N
                DefaultError error =
                        new DefaultError(UNEXPECTED_TOKEN,
                        msg,
                        msg,
                        getSnapshot().getSource().getFileObject(),
                        ts.offset(),
                        ts.offset() + ts.token().length(),
                        false /* not line error */,
                        Severity.ERROR);

                lexicalErrors.add(error);
            }
        }
        return lexicalErrors;

    }

    private List<Error> extractErrorsFromAST() {
        final List<Error> _errors = new ArrayList<Error>();

        AstNodeVisitor errorsCollector = new AstNodeVisitor() {

            @Override
            public void visit(AstNode node) {
                if (node.type() == AstNode.NodeType.OPEN_TAG
                        || node.type() == AstNode.NodeType.ENDTAG
                        || node.type() == AstNode.NodeType.UNKNOWN_TAG) {

                    for (ProblemDescription desc : node.getDescriptions()) {
                        if (desc.getType() < ProblemDescription.WARNING) {
                            continue;
                        }
                        //some error in the node, report
                        DefaultError error =
                                new DefaultError(desc.getKey(), //NOI18N
                                desc.getText(),
                                desc.getText(),
                                getSnapshot().getSource().getFileObject(),
                                desc.getFrom(),
                                desc.getTo(),
                                false /* not line error */,
                                desc.getType() == ProblemDescription.WARNING ? Severity.WARNING : Severity.ERROR); //NOI18N

                        error.setParameters(new Object[]{node});

                        _errors.add(error);

                    }
                }
            }
        };

        Collection<AstNode> roots = new ArrayList<AstNode>();
        roots.addAll(roots().values());
        roots.add(rootOfUndeclaredTagsParseTree());
        for (AstNode root : roots) {
            AstNodeUtils.visitChildren(root, errorsCollector);
        }

        return _errors;

    }

    public static AstNode getBoundAstNode(Error e) {
        if (e instanceof DefaultError) {
            if (e.getParameters() != null && e.getParameters().length > 0 && e.getParameters()[0] instanceof AstNode) {
                return (AstNode) e.getParameters()[0];
            }
        }

        return null;
    }

    static {
        HtmlParserResultAccessor.set(new Accessor());
    }

    private static class Accessor extends HtmlParserResultAccessor {

        @Override
        public HtmlParserResult createInstance(SyntaxAnalyzerResult result) {
            return new HtmlParserResult(result);
        }
    }
}
