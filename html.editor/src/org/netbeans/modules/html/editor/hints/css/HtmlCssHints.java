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
package org.netbeans.modules.html.editor.hints.css;

import java.io.IOException;
import java.util.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider.HintsManager;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.css.refactoring.api.RefactoringElementType;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.hints.EmbeddingUtil;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.AttributeFilter;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.NodeUtils;
import org.netbeans.modules.html.editor.lib.api.elements.NodeVisitor;
import org.netbeans.modules.html.editor.lib.api.elements.Tag;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mfukala@netbeans.org
 */
public class HtmlCssHints {

    private static final String ID_ATTR_NAME = "id"; //NOI18N
    private static final String CLASS_ATTR_NAME = "class"; //NOI18N

    public static void computeHints(HintsManager manager, RuleContext context, List<Hint> hints) {
        HtmlParserResult result = (HtmlParserResult) context.parserResult;
        FileObject file = result.getSnapshot().getSource().getFileObject();
        if (file == null) {
            return;
        }
        Project project = FileOwnerQuery.getOwner(file);
        if (project == null) {
            return;
        }
        try {
            NodeUtils.visitChildren(result.root(), new HintsNodeVisitor(context, file, project, hints), ElementType.OPEN_TAG);
        } catch (IOException ex) {
            //no-op
        }
    }

    private static class HintsNodeVisitor implements NodeVisitor {

        private final RuleContext context;
        private final List<Hint> hints;
        private final FileObject file;
        private final Collection<FileObject> referredFiles;
        private final Map<FileObject, Collection<String>> ids, classes;
        private final Map<String, Collection<FileObject>> ids2files;
        private final Map<String, Collection<FileObject>> classes2files;

        public HintsNodeVisitor(RuleContext context, FileObject file, Project project, List<Hint> hints) throws IOException {
            this.context = context;
            this.file = file;
            this.hints = hints;
            CssIndex index = CssIndex.create(project);
            DependenciesGraph deps = index.getDependencies(file);
            referredFiles = deps.getAllReferedFiles();

            ids = index.findAll(RefactoringElementType.ID);
            classes = index.findAll(RefactoringElementType.CLASS);

            ids2files = createReversedMap(ids);
            classes2files = createReversedMap(classes);
        }

        private static Map<String, Collection<FileObject>> createReversedMap(Map<FileObject, Collection<String>> file2elements) {
            Map<String, Collection<FileObject>> map = new HashMap<String, Collection<FileObject>>();
            for (FileObject file : file2elements.keySet()) {
                for (String element : file2elements.get(file)) {
                    Collection<FileObject> files = map.get(element);
                    if (files == null) {
                        files = new HashSet<FileObject>();
                    }
                    files.add(file);
                    map.put(element, files);
                }
            }
            return map;
        }

        @Override
        public void visit(Node node) {
            Tag tag = (Tag)node;
            for (Attribute id : tag.attributes(new AttributeFilter() {

                @Override
                public boolean accepts(Attribute attribute) {
                    return LexerUtils.equals(ID_ATTR_NAME, attribute.name(), true, true);
                }
            })) {
                processElements(id, CssElementType.ID, ids2files);
            }
            for (Attribute id : tag.attributes(new AttributeFilter() {

                @Override
                public boolean accepts(Attribute attribute) {
                    return LexerUtils.equals(CLASS_ATTR_NAME, attribute.name(), true, true);
                }
            })) {
                processElements(id, CssElementType.CLASS, classes2files);
            }

        }

        private void processElements(Attribute attribute, CssElementType elementType, Map<String, Collection<FileObject>> elements2files) {
            //all files containing the id declaration
            Collection<FileObject> filesWithTheId = elements2files.get(NodeUtils.unquotedValue(attribute).toString());

            //all referred files with the id declaration
            Collection<FileObject> referredFilesWithTheId = new LinkedList<FileObject>();
            if (filesWithTheId != null) {
                filesWithTheId.remove(file);
                referredFilesWithTheId.addAll(filesWithTheId);
                referredFilesWithTheId.retainAll(referredFiles);
            }

            if (referredFilesWithTheId.isEmpty()) {
                //unknown id
                hints.add(new MissingCssElement(elementType,
                        context,
                        getAttributeValueOffsetRange(attribute, context),
                        filesWithTheId));
            }
        }
    }

    private static OffsetRange getAttributeValueOffsetRange(Attribute attr, RuleContext context) {
        boolean quoted = NodeUtils.isValueQuoted(attr);
        int from = attr.valueOffset() + (quoted ? 1 : 0);
        int to = from + NodeUtils.unquotedValue(attr).length();
        return EmbeddingUtil.convertToDocumentOffsets(from, to, context.parserResult.getSnapshot());
    }
}
