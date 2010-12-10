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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsfapi.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import org.netbeans.editor.ext.html.parser.api.HtmlParsingResult;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.LibraryType;

/**
 *
 * @author marekfukala
 */
public class LibraryUtils {

    public static final String COMPOSITE_LIBRARY_NS = "http://java.sun.com/jsf/composite"; //NOI18N
    public static final String XHTML_NS = "http://www.w3.org/1999/xhtml"; //NOI18N

    public static String getCompositeLibraryURL(String libraryFolderPath) {
        return COMPOSITE_LIBRARY_NS + "/" + libraryFolderPath;
    }

    public static boolean isCompositeComponentLibrary(Library library) {
        return library.getType() == LibraryType.COMPOSITE;
    }

    public static boolean importLibrary(Document document, Library library, String prefix) {
        return !importLibrary(document, Collections.singletonMap(library, prefix)).isEmpty();
    }

    /**
     * Imports a facelets libraries
     *
     * @param document
     * @param libraries2prefixes a map of Library to prefix to declare. The prefix may be null, in
     * such case the default library prefix is used.
     *
     * @return a map of library2declared prefixes which contains just the imported pairs
     */
    public static Map<Library, String> importLibrary(Document document, Map<Library, String> libraries2prefixes) {
        assert document instanceof BaseDocument;

        final Map<Library, String> imports = new LinkedHashMap<Library, String>(libraries2prefixes);

        //verify and update the imports map
        Iterator<Library> libsIterator = imports.keySet().iterator();
        while (libsIterator.hasNext()) {
            Library l = libsIterator.next();
            String prefix = imports.get(l);
            if (prefix == null) {
                //not explicitly specified prefix, we may take the library's default one
                String defaultPrefix = l.getDefaultPrefix();
                if (defaultPrefix != null) {
                    imports.put(l, defaultPrefix); //update the map - add the default prefix, I recon no ConcurrentModificationException is thrown since the keyset remains the same
                } else {
                    //remove the library from the imports, we have no enough information
                    libsIterator.remove();
                }
            }
        }

        final BaseDocument bdoc = (BaseDocument) document;
        try {
            Source source = Source.create(bdoc);
            final HtmlParsingResult[] _result = new HtmlParsingResult[1];
            ParserManager.parse(Collections.singleton(source), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ResultIterator ri = WebUtils.getResultIterator(resultIterator, "text/html"); //NOI18N
                    if (ri != null) {
                        _result[0] = (HtmlParsingResult) ri.getParserResult();
                    }
                }
            });

            if (_result[0] == null) {
                //no html code
                return Collections.emptyMap();
            }

            //try find the html root node first
            final HtmlParsingResult result = _result[0];
            AstNode root = null;
            //no html root node, we need to find a root node of some other ast tree
            //belonging to some namespace
            Collection<AstNode> roots = new ArrayList<AstNode>();
            roots.addAll(result.roots().values());
            roots.add(result.rootOfUndeclaredTagsParseTree());

            for (AstNode r : roots) {
                //find first open tag node

                List<AstNode> chs = r.children(new AstNode.NodeFilter() {

                    @Override
                    public boolean accepts(AstNode node) {
                        return (node.type() == AstNode.NodeType.OPEN_TAG
                                || node.type() == AstNode.NodeType.UNKNOWN_TAG) && !node.isEmpty();
                    }
                });

                if (!chs.isEmpty()) {
                    AstNode top = chs.get(0);
                    if (root == null) {
                        root = top;
                    } else {
                        if (top.startOffset() < root.startOffset()) {
                            root = top;
                        }
                    }
                }
            }


            final AstNode rootNode = root;
            if (rootNode == null) {
                //TODO we may want to add a root node in such case
                return Collections.emptyMap();
            }

            //TODO decide whether to add a new line before or not based on other attrs - could be handled by the formatter!?!?!
            //first check if the library is already declared

            //XXX please note that the htmlresult.getNamespaces() returns a context free namespaces
            //declarations which is wrong. If any of the nested elements declares the namespace
            //the namespaces map will contain it and we will not add a new declaration which will
            //result into an invalid page

            //eliminate already declared libraries
            Iterator<Library> librariesIterator = imports.keySet().iterator();
            while (librariesIterator.hasNext()) {
                Library library = librariesIterator.next();
                Map<String, String> declaredNamespaces = result.getNamespaces();
                String alreadyDeclaredPrefix = declaredNamespaces.get(library.getNamespace());
                if (alreadyDeclaredPrefix == null) {
                    //try composite component library default prefix
                    if (library.getType() == LibraryType.COMPOSITE) {
                        String defaultNS = library.getDefaultNamespace();
                        alreadyDeclaredPrefix = declaredNamespaces.get(defaultNS);
                    }
                }
                if (alreadyDeclaredPrefix != null) {
                    //already declared, remove the library from the imports
                    librariesIterator.remove();

                }
            }

            //else add the declaration
            final Indent indent = Indent.get(bdoc);
            indent.lock();
            try {
                bdoc.runAtomic(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            boolean noAttributes = rootNode.getAttributeKeys().isEmpty();
                            //if there are no attributes, just add the new one at the end of the tag,
                            //if there are some, add the new one on a new line and reformat the tag

                            int offset_shift = 0;
                            Iterator<Library> libsItr = imports.keySet().iterator();
                            Parser.Result parsingApiResult = (Parser.Result)result;
                            int originalInsertPosition = parsingApiResult.getSnapshot().getOriginalOffset(rootNode.endOffset() - 1); //just before the closing symbol
                            if (originalInsertPosition == -1) {
                                //error, cannot recover
                                imports.clear();
                                return;
                            }

                            while (libsItr.hasNext()) {
                                Library library = libsItr.next();
                                String prefixToDeclare = imports.get(library);
                                int insertPosition = originalInsertPosition + offset_shift;

                                String text = (!noAttributes ? "\n" : "") + " xmlns:" + prefixToDeclare + //NOI18N
                                        "=\"" + library.getNamespace() + "\""; //NOI18N

                                bdoc.insertString(insertPosition, text, null);

                                offset_shift += text.length();
                            }


                            //reformat the tag so the new attribute gets aligned with the previous one/s
                            indent.reindent(originalInsertPosition, originalInsertPosition + offset_shift);

                        } catch (BadLocationException ex) {
                            Logger.global.log(Level.INFO, null, ex);
                        }
                    }
                });
            } finally {
                indent.unlock();
            }

            return imports; //return the remained libraries which should be those really imported

        } catch (ParseException ex) {
            Logger.global.log(Level.INFO, null, ex);
        }

        return Collections.emptyMap();
    }


}
