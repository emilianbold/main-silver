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
package org.netbeans.modules.php.editor.nav;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.Occurence;
import org.netbeans.modules.php.editor.model.OccurencesSupport;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.model.nodes.MagicMethodDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.PhpDocTypeTagInfo;
import org.netbeans.modules.php.editor.parser.PHPDocCommentParser;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocMethodTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Radek Matous
 */
public class DeclarationFinderImpl implements DeclarationFinder {

    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        return findDeclarationImpl(info, caretOffset);
    }

    @Override
    public OffsetRange getReferenceSpan(final Document doc, final int caretOffset) {
        TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, caretOffset);
        return getReferenceSpan(ts, caretOffset);
    }

    public static OffsetRange getReferenceSpan(TokenSequence<PHPTokenId> ts, final int caretOffset) {
        return new ReferenceSpanFinder().getReferenceSpan(ts, caretOffset);
    }

    public static DeclarationLocation findDeclarationImpl(ParserResult info, int caretOffset) {
        if (!(info instanceof PHPParseResult)) return DeclarationLocation.NONE;
        PHPParseResult result = (PHPParseResult) info;
        final Model model = result.getModel();
        OccurencesSupport occurencesSupport = model.getOccurencesSupport(caretOffset);
        Occurence underCaret = occurencesSupport.getOccurence();
        return findDeclarationImpl(underCaret, info);
    }

    private static DeclarationLocation findDeclarationImpl(Occurence underCaret, ParserResult info) {
        DeclarationLocation location = DeclarationLocation.NONE;
        if (underCaret != null) {
            Collection<? extends PhpElement> gotoDeclarations = underCaret.gotoDeclarations();
            if (gotoDeclarations == null || gotoDeclarations.isEmpty()) {
                return DeclarationLocation.NONE;
            }
            PhpElement declaration = gotoDeclarations.iterator().next();
            FileObject declarationFo = declaration.getFileObject();
            if (declarationFo == null) {
                return DeclarationLocation.NONE;
            }
            location = new DeclarationLocation(declarationFo, declaration.getOffset(), declaration);
            Collection<? extends PhpElement> alternativeDeclarations = gotoDeclarations;
            if (alternativeDeclarations.size() > 1) {
                final FileObject currentFile = info.getSnapshot().getSource().getFileObject();
                int numberOfCurrentDeclaration = 0;
                DeclarationLocation alternatives = DeclarationLocation.NONE;
                for (PhpElement elem : alternativeDeclarations) {
                    FileObject elemFo = elem.getFileObject();
                    if (elemFo == null) {
                        continue;
                    }
                    DeclarationLocation declLocation = new DeclarationLocation(elemFo, elem.getOffset(), elem);
                    if (currentFile == elemFo) {
                        location = declLocation;
                        numberOfCurrentDeclaration++;
                    }
                    AlternativeLocation al = new AlternativeLocationImpl(elem, declLocation);
                    if (alternatives == DeclarationLocation.NONE) {
                        alternatives = al.getLocation();
                    }
                    alternatives.addAlternative(al);
                }
                return (numberOfCurrentDeclaration == 1 &&
                        !EnumSet.<Occurence.Accuracy>of(Occurence.Accuracy.MORE_TYPES, Occurence.Accuracy.MORE).contains(underCaret.degreeOfAccuracy()))
                        ? location : alternatives;
            }
        }
        return location;
    }

    private static class ReferenceSpanFinder {

        private static final int RECURSION_LIMIT = 100;
        private int recursionCounter = 0;
        private static final Logger LOGGER = Logger.getLogger(DeclarationFinderImpl.class.getName());

        public OffsetRange getReferenceSpan(TokenSequence<PHPTokenId> ts, final int caretOffset) {
            if (ts == null) {
                return OffsetRange.NONE;
            }
            ts.move(caretOffset);
            int startTSOffset = 0;
            if (ts.moveNext()) {
                startTSOffset = ts.offset();
                Token<PHPTokenId> token = ts.token();
                PHPTokenId id = token.id();
                if (id.equals(PHPTokenId.PHP_STRING) || id.equals(PHPTokenId.PHP_VARIABLE)) {
                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
                }
                if (id.equals(PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING)) {
                    OffsetRange retval = new OffsetRange(ts.offset(), ts.offset() + token.length());
                    for (int i = 0; i < 2 && ts.movePrevious(); i++) {
                        token = ts.token();
                        id = token.id();
                        if (id.equals(PHPTokenId.PHP_INCLUDE) || id.equals(PHPTokenId.PHP_INCLUDE_ONCE) || id.equals(PHPTokenId.PHP_REQUIRE) || id.equals(PHPTokenId.PHP_REQUIRE_ONCE)) {
                            return retval;
                        } if (id.equals(PHPTokenId.PHP_STRING) && token.text().toString().equalsIgnoreCase("define")) {//NOI18N
                            return retval;
                        }
                    }
                } else if (id.equals(PHPTokenId.PHPDOC_COMMENT)) {
                    PHPDocCommentParser docParser = new PHPDocCommentParser();
                    PHPDocBlock docBlock = docParser.parse(ts.offset()-3, ts.offset() + token.length()-3, token.toString());
                    ASTNode[] hierarchy = Utils.getNodeHierarchyAtOffset(docBlock, caretOffset);
                    PhpDocTypeTagInfo node = null;
                    PHPDocTypeTag typeTag = null;
                    if (hierarchy != null && hierarchy.length > 0) {
                        if (hierarchy[0] instanceof PHPDocTypeTag) {
                            typeTag = (PHPDocTypeTag) hierarchy[0];
                            if (typeTag.getStartOffset() < caretOffset && caretOffset < typeTag.getEndOffset()) {
                                List<? extends PhpDocTypeTagInfo> tagInfos = PhpDocTypeTagInfo.create(typeTag, Kind.CLASS);
                                for (PhpDocTypeTagInfo typeTagInfo : tagInfos) {
                                    if (typeTagInfo.getKind().equals(Kind.CLASS)
                                            && typeTagInfo.getRange().containsInclusive(caretOffset)) {
                                        node = typeTagInfo;
                                        break;
                                    }
                                }
                                if (node == null || !node.getRange().containsInclusive(caretOffset)) {
                                    tagInfos = PhpDocTypeTagInfo.create(typeTag, Kind.VARIABLE);
                                    for (PhpDocTypeTagInfo typeTagInfo : tagInfos) {
                                        if (typeTagInfo.getKind().equals(Kind.VARIABLE)) {
                                            node = typeTagInfo;
                                            break;
                                        }
                                    }
                                }
                                if (node != null) {
                                    return node.getRange().containsInclusive(caretOffset) ? node.getRange() : OffsetRange.NONE;
                                }
                            }
                        } else {
                            List<PHPDocTag> tags = docBlock.getTags();
                            for (PHPDocTag phpDocTag : tags) {
                                if (phpDocTag instanceof PHPDocMethodTag) {
                                    PHPDocMethodTag methodTag = (PHPDocMethodTag) phpDocTag;
                                    MagicMethodDeclarationInfo methodInfo = MagicMethodDeclarationInfo.create(methodTag);
                                    if (methodInfo != null) {
                                        if (methodInfo.getRange().containsInclusive(caretOffset)) {
                                            return methodInfo.getRange();
                                        } else if (methodInfo.getTypeRange().containsInclusive(caretOffset)) {
                                            return methodInfo.getTypeRange();
                                        }
                                    }
                                }
                            }

                        }
                    }
                } else if (id.equals(PHPTokenId.PHP_COMMENT) && token.text() != null) {
                    String text = token.text().toString();
                    final String dollaredVar = "@var";
                    if (text.contains(dollaredVar)) {
                        String[] segments = text.split("\\s");
                        for (int i = 0; i < segments.length; i++) {
                            String seg = segments[i];
                            if (seg.equals(dollaredVar) && segments.length > i+2) {
                                for (int j = 1; j <= 2 ; j++) {
                                    seg = segments[i + j];
                                    if (seg != null && seg.trim().length() > 0) {
                                        int indexOf = text.indexOf(seg);
                                        assert indexOf != -1;
                                        indexOf += ts.offset();
                                        OffsetRange range = new OffsetRange(indexOf, indexOf + seg.length());
                                        if (range.containsInclusive(caretOffset)) {
                                            return range;
                                        }
                                    }
                                }
                                return OffsetRange.NONE;
                            }
                        }
                    }

                }
            }
            if (caretOffset == startTSOffset) {
                if (recursionCounter < RECURSION_LIMIT) {
                    recursionCounter++;
                    // if there is not a refence, and the curet is just beetween two tokens,
                    // try the previous token. See issue #199329
                    return getReferenceSpan(ts, caretOffset - 1);
                } else {
                    logRecursion(ts);
                }
            }
            return OffsetRange.NONE;
        }
        
        private void logRecursion(TokenSequence<PHPTokenId> ts) {
            CharSequence tokenText = null;
            if (ts != null) {
                Token<PHPTokenId> token = ts.token();
                if (token != null) {
                    tokenText = token.text();
                } else {
                    tokenText = "Possibly between tokens"; //NOI18N
                }
            }
            LOGGER.log(Level.WARNING, "Stack overflow detection - limit: {0}, token: {1}", new Object[]{RECURSION_LIMIT, tokenText});
        }

    }

    public static class AlternativeLocationImpl implements AlternativeLocation {

        private PhpElement modelElement;
        private DeclarationLocation declaration;

        public AlternativeLocationImpl(PhpElement modelElement, DeclarationLocation declaration) {
            this.modelElement = modelElement;
            this.declaration = declaration;
        }
        public AlternativeLocationImpl(PhpElement modelElement) {
            this(modelElement, new DeclarationLocation(modelElement.getFileObject(), modelElement.getOffset(), modelElement));
        }

        @Override
        public ElementHandle getElement() {
            return modelElement;
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            formatter.reset();
            ElementKind ek = modelElement.getKind();

            if (ek != null) {
                formatter.name(ek, true);
                if ((modelElement instanceof FullyQualifiedElement) && !((FullyQualifiedElement)modelElement).getNamespaceName().isDefaultNamespace()) {
                    QualifiedName namespaceName = ((FullyQualifiedElement) modelElement).getNamespaceName();
                    formatter.appendText(namespaceName.append(modelElement.getName()).toString());
                } else {
                    formatter.appendText(modelElement.getName());
                }
                formatter.name(ek, false);
            } else {
                formatter.appendText(modelElement.getName());
            }

            if (declaration.getFileObject() != null) {
                formatter.appendText(" in ");
                formatter.appendText(FileUtil.getFileDisplayName(declaration.getFileObject()));
            }

            return formatter.getText();
        }

        @Override
        public DeclarationLocation getLocation() {
            return declaration;
        }

        @Override
        public int compareTo(AlternativeLocation o) {
            AlternativeLocationImpl i = (AlternativeLocationImpl) o;
            return this.modelElement.getName().compareTo(i.modelElement.getName());
        }
    }
}
