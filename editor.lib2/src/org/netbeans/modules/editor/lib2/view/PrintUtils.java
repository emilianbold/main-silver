/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing the
 * software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.view;

import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.*;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.highlighting.HighlightItem;
import org.netbeans.modules.editor.lib2.highlighting.HighlightingManager;
import org.netbeans.modules.editor.lib2.highlighting.HighlightsList;
import org.netbeans.modules.editor.lib2.highlighting.HighlightsReader;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.openide.util.Lookup;

/**
 * Printing services related to document or views.
 *
 * @author Miloslav Metelka
 */
public final class PrintUtils {

    private PrintUtils() {
        // no instances
    }

    /**
     * 
     * @param doc
     * @param printLineNumbers
     * @param startOffset
     * @param endOffset
     * @return 
     */
    public static List<AttributedCharacterIterator> printDocument(final Document doc, final boolean printLineNumbers,
            final int startOffset, final int endOffset)
    {
        assert (SwingUtilities.isEventDispatchThread()) : "EDT required";
        final List<AttributedCharacterIterator> attributedLines = new ArrayList<AttributedCharacterIterator>();
        doc.render(new Runnable() {
            @Override
            public void run() {
                Element lineRoot = doc.getDefaultRootElement();        
                int startLineIndex = (startOffset > 0) ? lineRoot.getElementIndex(startOffset) : 0;
                int endLineIndex;
                if (endOffset < doc.getLength()) {
                    endLineIndex = lineRoot.getElementIndex(endOffset);
                    if (endLineIndex >= 0 && endOffset > lineRoot.getElement(endLineIndex).getStartOffset()) {
                        endLineIndex++;
                    }
                } else {
                    endLineIndex = lineRoot.getElementCount();
                }
                // Create temporary text component so that highlighting layers can be initialized.
                JEditorPane pane = new JEditorPane();
                pane.setDocument(doc);
                CharSequence docText = DocumentUtilities.getText(doc);
                String mimeType = DocumentUtilities.getMimeType(pane);
                Lookup lookup = MimeLookup.getLookup(mimeType);
                Lookup.Result<FontColorSettings> result = lookup.lookupResult(FontColorSettings.class);
                Collection<? extends FontColorSettings> fcsInstances = result.allInstances();
                HighlightsContainer bottomHighlights = HighlightingManager.getInstance(pane).getBottomHighlights();
                HighlightsReader hiReader = new HighlightsReader(bottomHighlights, startOffset, endOffset);
                hiReader.readUntil(endOffset);
                HighlightsList hiList = hiReader.highlightsList();
                Integer tabSize = (Integer) doc.getProperty(PlainDocument.tabSizeAttribute);
                if (tabSize == null) {
                    tabSize = 8;
                }

                if (endOffset > startOffset && fcsInstances.size() > 0) { // It should always be non-null
                    assert (hiList.size() > 0); // for endOffset > startOffset there should be items
                    int hiItemIndex = 0;
                    HighlightItem hiItem = hiList.get(hiItemIndex);
                    FontColorSettings fcs = fcsInstances.iterator().next();
                    AttributeSet defaultAttrs = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
                    AttributeSet lineNumberAttrs = fcs.getFontColors(FontColorNames.LINE_NUMBER_COLORING);
                    lineNumberAttrs = AttributesUtilities.createComposite(defaultAttrs, lineNumberAttrs);
                    Map<AttributeSet,Map<Attribute,Object>> cache = new HashMap<AttributeSet, Map<Attribute, Object>>();
                    Map<Attribute,Object> defaultTextAttrs = translate(defaultAttrs, cache);
                    Map<Attribute,Object> lineNumberTextAttrs = translate(lineNumberAttrs, cache);
                    int maxDigitCount = String.valueOf(endLineIndex + 1).length();
                    StringBuilder sb = new StringBuilder(100);

                    while (startLineIndex < endLineIndex) {
                        sb.setLength(0);
                        int startColumn = 0;
                        Element lineElement = lineRoot.getElement(startLineIndex);
                        if (printLineNumbers) {
                            ArrayUtilities.appendIndex(sb, startLineIndex + 1, maxDigitCount);
                            sb.append(' '); // Space separates line numbering from text
                        }
                        int lineTextStartIndex = sb.length();
                        // Use String to ensure post-readlock stability
                        int offset0 = lineElement.getStartOffset();
                        while (offset0 < startOffset) { // Starting in middle of line -> adjust column
                            if (docText.charAt(offset0) == '\t') {
                                startColumn = (startColumn + tabSize) % tabSize * tabSize;
                            } else {
                                startColumn++;
                            }
                            offset0++;
                        }
                        int offset1 = Math.min(lineElement.getEndOffset() - 1, endOffset);
                        AttributedCharSequence acs = new AttributedCharSequence();
                        if (printLineNumbers) {
                            acs.addTextRun(maxDigitCount, lineNumberTextAttrs); // Color line numbers
                            acs.addTextRun(maxDigitCount + 1, defaultTextAttrs); // Space colored by default attrs
                        }
                        int offset = offset0;
                        int column = startColumn;
                        while (offset < offset1) {
                            while (hiItem.getEndOffset() <= offset) {
                                hiItemIndex++;
                                hiItem = hiList.get(hiItemIndex);
                            }
                            int hiEndOffset = Math.min(hiItem.getEndOffset(), offset1);
                            while (offset < hiEndOffset) { // Starting in middle of line -> adjust column
                                char ch = docText.charAt(offset);
                                if (ch == '\t') {
                                    int newColumn = (column + tabSize) / tabSize * tabSize;
                                    while (column < newColumn) {
                                        sb.append(' ');
                                        column++;
                                    }
                                } else {
                                    sb.append(ch);
                                    column++;
                                }
                                offset++;
                            }
                            Map<Attribute,Object> textAttrs = (hiItem.getAttributes() != null)
                                    ? translate(
                                        AttributesUtilities.createComposite(hiItem.getAttributes(), defaultAttrs),
                                        cache)
                                    : defaultTextAttrs;
                            acs.addTextRun(lineTextStartIndex + (column - startColumn), textAttrs);
                        }

                        acs.setText(sb.toString(), defaultTextAttrs);
                        attributedLines.add(acs);
                        startLineIndex++;
                    }
                }
            }
        });
        return attributedLines;
    }
    
    private static Map<Attribute,Object> translate(AttributeSet attrs, Map<AttributeSet,Map<Attribute,Object>> cache) {
        Map<Attribute,Object> textAttrs = cache.get(attrs);
        if (textAttrs == null) {
            textAttrs = AttributedCharSequence.translate(attrs);
            cache.put(attrs, textAttrs);
        }
        return textAttrs;
    }

}
