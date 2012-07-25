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
package org.netbeans.modules.web.inspect.webkit.actions;

import java.awt.EventQueue;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.html.editor.lib.api.HtmlParsingResult;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.inspect.CSSUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Go to source action for DOM nodes.
 *
 * @author Jan Stola
 */
public class GoToNodeSourceAction extends NodeAction  {

    @Override
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        org.netbeans.modules.web.webkit.debugging.api.dom.Node node = activatedNodes[0]
                .getLookup().lookup(org.netbeans.modules.web.webkit.debugging.api.dom.Node.class);
        String documentURL = getDocumentURL(node);
        try {
            URI uri = new URI(documentURL);
            // 208252: Workaround for file://localhost/<path> URIs that appear on Mac
            if ((uri.getAuthority() != null) || (uri.getFragment() != null) || (uri.getQuery() != null)) {
                uri = new URI(uri.getScheme(), null, uri.getPath(), null, null);
            }
            File file = new File(uri);
            file = FileUtil.normalizeFile(file);
            FileObject fob = FileUtil.toFileObject(file);
            Source source = Source.create(fob);
            ParserManager.parse(Collections.singleton(source), new GoToNodeTask(node, fob));
        } catch (URISyntaxException ex) {
            Logger.getLogger(GoToNodeSourceAction.class.getName()).log(Level.INFO, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(GoToNodeSourceAction.class.getName()).log(Level.INFO, null, ex);
        }
    }

    @Override
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            org.netbeans.modules.web.webkit.debugging.api.dom.Node node = activatedNodes[0]
                    .getLookup().lookup(org.netbeans.modules.web.webkit.debugging.api.dom.Node.class);
            if (node == null) {
                return false;
            }
            String documentURL = getDocumentURL(node);
            if (documentURL == null || !documentURL.startsWith("file://")) { // NOI18N
                return false;
            }
        }
        return true;
    }

    /**
     * Returns URL of a document the specified node belongs to.
     *
     * @param node node form the document in question.
     * @return URL of a document the specified node belongs to.
     */
    private String getDocumentURL(org.netbeans.modules.web.webkit.debugging.api.dom.Node node) {
        String documentURL = node.getDocumentURL();
        org.netbeans.modules.web.webkit.debugging.api.dom.Node parent = node.getParent();
        while ((documentURL == null) && (parent != null) && (parent.getContentDocument() == null)) {
            node = parent;
            documentURL = node.getDocumentURL();
            parent = node.getParent();
        }
        return documentURL;
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(GoToNodeSourceAction.class, "GoToNodeSourceAction.name"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Task that jumps to the source of the specified node in the document
     * in the given file.
     */
    static class GoToNodeTask extends UserTask {
        /** Node to jump to. */
        private org.netbeans.modules.web.webkit.debugging.api.dom.Node node;
        /** File to jump into. */
        private FileObject fob;

        /**
         * Creates a new {@code GoToNodeTask} for the specified file and node.
         *
         * @param node node to jump to.
         * @param fob file to jump into.
         */
        GoToNodeTask(org.netbeans.modules.web.webkit.debugging.api.dom.Node node, FileObject fob) {
            this.node = node;
            this.fob = fob;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            HtmlParsingResult result = (HtmlParsingResult)resultIterator.getParserResult();
            Node root = result.root();
            // PENDING
            final Node nodeToShow = root;
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    CSSUtils.open(fob, nodeToShow.from());
                }
            });
        }
        
    }

}
