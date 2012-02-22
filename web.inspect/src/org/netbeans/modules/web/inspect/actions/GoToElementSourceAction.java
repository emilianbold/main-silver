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
package org.netbeans.modules.web.inspect.actions;

import java.awt.EventQueue;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import org.netbeans.editor.ext.html.parser.api.HtmlParsingResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.inspect.CSSUtils;
import org.netbeans.modules.web.inspect.ElementHandle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.w3c.dom.Element;

/**
 * Go to source action for DOM elements.
 *
 * @author Jan Stola
 */
public class GoToElementSourceAction extends NodeAction  {

    @Override
    protected void performAction(Node[] activatedNodes) {
        Element element = activatedNodes[0].getLookup().lookup(Element.class);
        String uriTxt = element.getOwnerDocument().getDocumentURI();
        try {
            URI uri = new URI(uriTxt);
            // 208252: Workaround for file://localhost/<path> URIs that appear on Mac
            if (uri.getAuthority() != null) {
                uri = new URI(uri.getScheme(), null, uri.getPath(), null, null);
            }
            File file = new File(uri);
            file = FileUtil.normalizeFile(file);
            FileObject fob = FileUtil.toFileObject(file);
            Source source = Source.create(fob);
            ElementHandle handle = ElementHandle.forElement(element);
            ParserManager.parse(Collections.singleton(source), new GoToElementTask(handle, fob));
        } catch (URISyntaxException ex) {
            Logger.getLogger(GoToElementSourceAction.class.getName()).log(Level.INFO, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(GoToElementSourceAction.class.getName()).log(Level.INFO, null, ex);
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            Element element = activatedNodes[0].getLookup().lookup(Element.class);
            if (element == null) {
                return false;
            }
            String uri = element.getOwnerDocument().getDocumentURI();
            if (uri == null || !uri.startsWith("file://")) { // NOI18N
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(GoToElementSourceAction.class, "GoToElementSourceAction.name"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Task that jumps on the source tag of the specified element
     * in the document in the given file.
     */
    static class GoToElementTask extends UserTask {
        /** Element to jump to. */
        private ElementHandle element;
        /** File to jump into. */
        private FileObject fob;

        /**
         * Creates a new {@code GoToElementTask} for the specified file and element.
         * 
         * @param element element to jump to.
         * @param fob file to jump into.
         */
        GoToElementTask(ElementHandle element, FileObject fob) {
            this.element = element;
            this.fob = fob;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            HtmlParsingResult result = (HtmlParsingResult)resultIterator.getParserResult();
            AstNode root = result.root();
            AstNode[] searchResult = element.locateInAst(root);
            AstNode node = searchResult[0];
            if (node == null) {
                // Exact match not found, use the nearest node
                node = searchResult[1];
            }
            while (node.isVirtual()) {
                node = node.parent();
            }
            final AstNode nodeToShow = node;
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    CSSUtils.open(fob, nodeToShow.startOffset());
                }
            });
        }
        
    }

}
