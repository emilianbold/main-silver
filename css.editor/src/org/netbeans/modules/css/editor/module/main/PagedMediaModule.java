/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.module.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.editor.module.CssModuleSupport;
import org.netbeans.modules.css.editor.module.spi.CompletionContext;
import org.netbeans.modules.css.editor.module.spi.CssModule;
import org.netbeans.modules.css.editor.module.spi.FeatureContext;
import org.netbeans.modules.css.editor.module.spi.PropertyDescriptor;
import org.netbeans.modules.css.editor.module.spi.Utilities;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mfukala@netbeans.org
 */
@ServiceProvider(service = CssModule.class)
public class PagedMediaModule extends CssModule {

    private static final String PROPERTIES_DEFINITION_PATH = "org/netbeans/modules/css/editor/module/main/paged_media"; //NOI18N
    private static Collection<PropertyDescriptor> propertyDescriptors;
    private static final Collection<String> PAGE_PSEUDO_CLASSES = Arrays.asList(new String[]{"first", "left", "right"}); //NOI18N
    private static final Collection<String> PAGE_MARGIN_SYMBOLS =
            Arrays.asList(new String[]{
                "top-left-corner",
                "top-left",
                "top-center",
                "top-right",
                "top-right-corner",
                "bottom-left-corner",
                "bottom-left",
                "bottom-center",
                "bottom-right",
                "bottom-right-corner",
                "left-top",
                "left-middle",
                "left-bottom",
                "right-top",
                "right-middle",
                "right-bottom"
            });

    @Override
    public synchronized Collection<PropertyDescriptor> getPropertyDescriptors() {
        if (propertyDescriptors == null) {
            propertyDescriptors = Utilities.parsePropertyDefinitionFile(PROPERTIES_DEFINITION_PATH);
        }
        return propertyDescriptors;
    }

    @Override
    public List<CompletionProposal> getCompletionProposals(CompletionContext context) {
        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
        Node activeNode = context.getActiveNode();
        boolean isError = activeNode.type() == NodeType.error;
        if (isError) {
            activeNode = activeNode.parent();
        }
        String prefix = context.getPrefix(); //default
        Token<CssTokenId> token = context.getTokenSequence().token();
        if(token == null) {
            return Collections.emptyList(); //empty file - no tokens
        }
        
        CssTokenId tokenId = token.id();
        switch (activeNode.type()) {
            case page:
                switch (tokenId) {
                    case IDENT:
                        if (context.getActiveTokenDiff() == 0 && LexerUtils.followsToken(context.getTokenSequence(),
                                CssTokenId.PAGE_SYM, true, true, CssTokenId.WS) != null) {
                            //just after page name: @page mypag|
                            prefix = "";
                            proposals.addAll(getPagePseudoClassCompletionProposals(context, true));
                        }
                        break;
                    case WS:
                        if (LexerUtils.followsToken(context.getTokenSequence(),
                                CssTokenId.LBRACE, true, true, CssTokenId.WS) != null) {
                            //inside the page rule body, no prefix: @page { | }
                            proposals.addAll(getPageMarginSymbolsCompletionProposals(context, true));
                            proposals.addAll(getPropertiesCompletionProposals(context));
                        }
                        break;
                    case PAGE_SYM: //just after @page keyword: @page|
                        proposals.addAll(getPagePseudoClassCompletionProposals(context, true));
                        break;
                    case ERROR:
                        //@page { @|  }
                        if(token.text().charAt(0) == '@') {
                            proposals.addAll(getPageMarginSymbolsCompletionProposals(context, true));
                        }
                        break;
                }
                break;
            case pseudoPage:
                switch (tokenId) {
                    case COLON: //just after colon: @page :|
                    case IDENT: //in the page pseudo class: @page:fir|
                        proposals.addAll(getPagePseudoClassCompletionProposals(context, false));
                        break;
                }
                break;
            case margin:
                switch(tokenId) {
                    case WS:
                        //no prefix in margin
                        proposals.addAll(getPropertiesCompletionProposals(context));
                        break;
                }
                break;
        }

        return Css3Utils.filterCompletionProposals(proposals, prefix, true);
    }

    private static List<CompletionProposal> getPageMarginSymbolsCompletionProposals(CompletionContext context, boolean addAtPrefix) {
        String prefix = addAtPrefix ? "@" : null;
        return Utilities.createRAWCompletionProposals(PAGE_MARGIN_SYMBOLS, ElementKind.FIELD, context.getAnchorOffset(), prefix);
    }

    private static List<CompletionProposal> getPagePseudoClassCompletionProposals(CompletionContext context, boolean addColonPrefix) {
        String prefix = addColonPrefix ? ":" : null;
        return Utilities.createRAWCompletionProposals(PAGE_PSEUDO_CLASSES, ElementKind.FIELD, context.getAnchorOffset(), prefix);
    }
    
    private static List<CompletionProposal> getPropertiesCompletionProposals(CompletionContext context) {
        return Utilities.wrapProperties(CssModuleSupport.getPropertyDescriptors().values(), context.getAnchorOffset());
    }
    
    

    @Override
    public <T extends Map<OffsetRange, Set<ColoringAttributes>>> NodeVisitor<T> getSemanticHighlightingNodeVisitor(FeatureContext context, T result) {
        return new NodeVisitor<T>(result) {

            @Override
            public boolean visit(Node node) {
                switch (node.type()) {
                    case pseudoPage:
                        getResult().put(Css3Utils.getOffsetRange(node), ColoringAttributes.CLASS_SET);
                        break;
                }
                return false;
            }
        };
    }
}
