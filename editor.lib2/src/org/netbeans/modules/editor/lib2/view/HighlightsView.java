/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.editor.lib2.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
 * View with highlights. This is the most used view.
 *
 * @author Miloslav Metelka
 */

public class HighlightsView extends EditorView implements TextLayoutView {

    // -J-Dorg.netbeans.modules.editor.lib2.view.HighlightsView.level=FINE
    private static final Logger LOG = Logger.getLogger(HighlightsView.class.getName());

    /** Offset of start offset of this view. */
    private int rawOffset; // 24-super + 4 = 28 bytes

    /** Length of text occupied by this view. */
    private int length; // 28 + 4 = 32 bytes

    /** Attributes for rendering */
    private final AttributeSet attributes; // 36 + 4 = 40 bytes

    public HighlightsView(int offset, int length, AttributeSet attributes) {
        super(null);
        assert (length > 0) : "length=" + length + " <= 0"; // NOI18N
        this.rawOffset = offset;
        this.length = length;
        this.attributes = attributes;
    }

    @Override
    public float getPreferredSpan(int axis) {
        TextLayout textLayout = getTextLayout();
        if (textLayout == null) {
            return 0f;
        }
        float span = (axis == View.X_AXIS)
            ? textLayout.getAdvance()
            : textLayout.getAscent() + textLayout.getDescent() + textLayout.getLeading();
        return span;
    }

    @Override
    public int getRawOffset() {
        return rawOffset;
    }

    @Override
    public void setRawOffset(int rawOffset) {
        this.rawOffset = rawOffset;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public boolean setLength(int length) {
        this.length = length;
        releaseTextLayout(); // Ensure that text layout gets recreated
        return true;
    }

    @Override
    public int getStartOffset() {
        ParagraphView parent = (ParagraphView) getParent();
        return (parent != null) ? parent.getViewOffset(rawOffset) : rawOffset;
    }

    @Override
    public int getEndOffset() {
        return getStartOffset() + getLength();
    }

    @Override
    public Document getDocument() {
        View parent = getParent();
        return (parent != null) ? parent.getDocument() : null;
    }

    @Override
    public AttributeSet getAttributes() {
        return attributes;
    }

    @Override
    public TextLayout createTextLayout() {
        return createTextLayout(0, getLength());
    }

    TextLayout createTextLayout(int shift, int length) {
        DocumentView documentView = getDocumentView();
        if (documentView == null) {
            return null;
        }
        Document doc = documentView.getDocument();
        FontRenderContext frc = documentView.getFontRenderContext();
        if (doc == null || frc == null) {
            return null;
        }
        String text;
        try {
            text = doc.getText(getStartOffset() + shift, length);
            if (documentView.isShowNonprintingCharacters()) {
                text = text.replace(' ', DocumentView.PRINTING_SPACE);
            }
        } catch (BadLocationException e) {
            return null; // => Null text layout
        }
        Font font = ViewUtils.getFont(getAttributes(), documentView.getTextComponent().getFont());
        TextLayout textLayout = new TextLayout(text, font, frc);
        return textLayout;
    }

    void releaseTextLayout() {
        ParagraphView paragraphView = getParagraphView();
        if (paragraphView != null) {
            DocumentView documentView = paragraphView.getDocumentView();
            if (documentView != null) {
                getTextLayoutCache().put(paragraphView, this, null);
            }
        }
    }

    ParagraphView getParagraphView() {
        return (ParagraphView) getParent();
    }

    DocumentView getDocumentView() {
        ParagraphView paragraphView = getParagraphView();
        return (paragraphView != null) ? paragraphView.getDocumentView() : null;
    }

    TextLayoutCache getTextLayoutCache() {
        DocumentView documentView = getDocumentView();
        return (documentView != null) ? documentView.getTextLayoutCache() : null;
    }

    TextLayout getTextLayout() {
        ParagraphView paragraphView = getParagraphView();
        if (paragraphView != null) {
            DocumentView documentView = paragraphView.getDocumentView();
            if (documentView != null) {
                return getTextLayoutCache().get(paragraphView, this);
            }
        }
        return null;
    }

    float getSpan() {
        TextLayout textLayout = getTextLayout();
        if (textLayout == null) {
            return 0f;
        }
        return textLayout.getAdvance();
    }

    @Override
    public Shape modelToViewChecked(int offset, Shape alloc, Position.Bias bias) {
        return modelToViewChecked(offset, alloc, bias, getTextLayout(), getStartOffset(), getLength());
    }

    static Shape modelToViewChecked(int offset, Shape alloc, Position.Bias bias,
            TextLayout textLayout, int startOffset, int textLength)
    {
        if (textLayout == null) {
            return alloc; // Leave given bounds
        }
        assert (textLayout.getCharacterCount() == textLength) : "textLayout.getCharacterCount()=" + // NOI18N
                textLayout.getCharacterCount() + " != textLength=" + textLength; // NOI18N
        // If offset is >getEndOffset() use view-end-offset - otherwise it would throw exception from textLayout.getCaretInfo()
	int charIndex = Math.min(offset - startOffset, textLength);
	TextHitInfo hit = (bias == Position.Bias.Forward)
                ? TextHitInfo.afterOffset(charIndex)
                : TextHitInfo.beforeOffset(charIndex);
	float[] locs = textLayout.getCaretInfo(hit);
        Rectangle2D.Double bounds = ViewUtils.shape2Bounds(alloc);
	bounds.setRect(
                bounds.getX() + locs[0],
                bounds.getY(),
                1, // ?? glyphpainter2 uses 1 but shouldn't be a char width ??
                bounds.getHeight()
        );
        return bounds;
    }

    @Override
    public int viewToModelChecked(double x, double y, Shape alloc, Position.Bias[] biasReturn) {
        return viewToModelChecked(x, y, alloc, biasReturn, getTextLayout(), getStartOffset());

    }

    static int viewToModelChecked(double x, double y, Shape alloc, Position.Bias[] biasReturn,
            TextLayout textLayout, int startOffset)
    {
        if (textLayout == null) {
            return startOffset;
        }
        Rectangle2D.Double mutableBounds = ViewUtils.shape2Bounds(alloc);
        TextHitInfo hitInfo = x2RelOffset(textLayout, (float)(x - mutableBounds.getX()));
        if (biasReturn != null) {
            biasReturn[0] = hitInfo.isLeadingEdge() ? Position.Bias.Forward : Position.Bias.Backward;
        }
        return startOffset + hitInfo.getInsertionIndex();
    }

    static TextHitInfo x2RelOffset(TextLayout textLayout, float x) {
        TextHitInfo hit;
        if (x >= textLayout.getAdvance()) {
            hit = TextHitInfo.trailing(textLayout.getCharacterCount());
        } else {
            hit = textLayout.hitTestChar(x, 0); // What about backward bias -> with higher offsets it may go back visually
        }
        return hit;

    }

    @Override
    public int getNextVisualPositionFromChecked(int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet) {
        return getNextVisualPositionFromChecked(offset, bias, alloc, direction, biasRet,
                getTextLayout(), getStartOffset(), getLength(), getDocumentView());
    }

    static int getNextVisualPositionFromChecked(int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet,
            TextLayout textLayout, int startOffset, int textLength, DocumentView docView)
    {
        switch (direction) {
            case View.NORTH:
            case View.SOUTH:
                if (offset != -1) {
                    // Presumably pos is between startOffset and endOffset,
                    // since GlyphView is only one line, we won't contain
                    // the position to the north/south, therefore return -1.
                    return -1;
                }
                if (docView != null) {
                    JTextComponent textComponent = docView.getTextComponent();
                    Caret caret = textComponent.getCaret();
                    Point magicPoint;
                    magicPoint = (caret != null) ? caret.getMagicCaretPosition() : null;
                    if (magicPoint == null) {
                        biasRet[0] = Position.Bias.Forward;
                        return startOffset;
                    }
                    return viewToModelChecked((double)magicPoint.x, 0d, alloc, biasRet,
                            textLayout, startOffset);
                }
                break;

            case WEST:
                if (offset == -1) {
                    offset = Math.max(0, startOffset + textLength - 1);
                } else {
                    offset = Math.max(0, offset - 1);
                }
                break;
            case EAST:
                if (offset == -1) {
                    offset = startOffset;
                } else {
                    if (docView != null) {
                        offset = Math.min(offset + 1, docView.getDocument().getLength());
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("Bad direction: " + direction);
        }
        return offset;
    }

    @Override
    public void paint(Graphics2D g, Shape alloc, Rectangle clipBounds) {
        paint(g, alloc, clipBounds, this, getDocumentView(), getTextLayout());
    }

    static void paint(Graphics2D g, Shape alloc, Rectangle clipBounds,
            EditorView view, DocumentView docView, TextLayout textLayout)
    {
        Rectangle2D.Double mutableBounds = ViewUtils.shape2Bounds(alloc);
        if (mutableBounds.intersects(clipBounds)) {
            boolean loggable = LOG.isLoggable(Level.FINEST);
            if (docView != null) {
                Color origColor = g.getColor();
                Font origFont = g.getFont();
                try {
                    // Paint background
                    JTextComponent textComponent = docView.getTextComponent();
                    Color componentBackground = textComponent.getBackground();
                    AttributeSet attrs = view.getAttributes();
                    float baselineOffset = docView.getDefaultBaselineOffset();
                    ViewUtils.applyBackgroundAttributes(attrs, componentBackground, g);
                    if (!componentBackground.equals(g.getColor())) { // Not yet cleared by BasicTextUI.paintBackground()
                        if (loggable) {
                            LOG.finest(view.getDumpId() + ":paint-bkg: " + ViewUtils.toString(g.getColor()) + // NOI18N
                                    ", bounds=" + ViewUtils.toString(mutableBounds) + '\n'); // NOI18N
                        }
                        // clearRect() uses g.getBackground() color
                        g.fillRect(
                                (int)mutableBounds.getX(),
                                (int)mutableBounds.getY(),
                                (int)mutableBounds.getWidth(),
                                (int)mutableBounds.getHeight()
                        );
                    }

                    // Paint possible underlines
                    if (attrs != null) {
                        Color bottomBorderLineColor = null;
                        Color waveUnderlineColor = (Color) attrs.getAttribute(EditorStyleConstants.WaveUnderlineColor);
                        if (waveUnderlineColor != null && bottomBorderLineColor == null) { // draw wave underline
                            g.setColor(waveUnderlineColor);
                            float underlineOffset = docView.getDefaultUnderlineOffset() + baselineOffset;
                            int wavePixelCount = (int) mutableBounds.getWidth() + 1;
                            if (wavePixelCount > 0) {
                                int[] waveForm = {0, 0, -1, -1};
                                int[] xArray = new int[wavePixelCount];
                                int[] yArray = new int[wavePixelCount];

                                int intX = (int) mutableBounds.x;
                                int intY = (int) (mutableBounds.y + underlineOffset + 0.5);
                                int waveFormIndex = intX % 4;
                                for (int i = 0; i < wavePixelCount; i++) {
                                    xArray[i] = intX + i;
                                    yArray[i] = intY + waveForm[waveFormIndex];
                                    waveFormIndex = (++waveFormIndex) & 3;
                                }
                                g.drawPolyline(xArray, yArray, wavePixelCount - 1);
                            }
                        }
                    }

                    // Paint foreground
                    ViewUtils.applyForegroundAttributes(attrs, textComponent.getFont(), textComponent.getForeground(), g);
                    if (textLayout == null) {
                        return;
                    }
                    float x = (float) mutableBounds.getX();
                    float y = (float) mutableBounds.getY();
                    if (loggable) {
                        int startOffset = view.getStartOffset();
                        int endOffset = view.getEndOffset();
                        Document doc = docView.getDocument();
                        CharSequence text = DocumentUtilities.getText(doc).subSequence(startOffset, endOffset);
                        // Here it's assumed that 'text' var contains the same content as (possibly cached)
                        // textLayout but if textLayout caching would be broken then they could differ.
                        LOG.finest(view.getDumpId() + ":paint-txt: \"" + CharSequenceUtilities.debugText(text) + // NOI18N
                                "\", XY["+ x + ";" + y + "(B" + // NOI18N
                                ViewUtils.toStringPrec1(baselineOffset) + // NOI18N
                                ")], color=" + ViewUtils.toString(g.getColor()) + '\n'); // NOI18N
                    }
                    // TextLayout is unable to do a partial render
                    textLayout.draw(g, x, y + baselineOffset);
                } finally {
                    g.setFont(origFont);
                    g.setColor(origColor);
                }
            }
        }
    }

    @Override
    public View breakView(int axis, int offset, float x, float len) {
        View part = breakView(axis, offset, x, len, this, 0, getLength(), getTextLayout());
        return (part != null) ? part : this;
    }

    static View breakView(int axis, int offset, float x, float len, HighlightsView fullView,
            int partShift, int partLength, TextLayout textLayout)
    {
        if (axis == View.X_AXIS) {
            DocumentView docView = fullView.getDocumentView();
            // [TODO] Should check for RTL text
            if (docView != null && textLayout != null) {
                // The logic
                int partStartOffset = fullView.getStartOffset() + partShift;
                int shift = offset - partStartOffset;
                if (shift < 0 || shift > partLength) {
                    throw new IllegalArgumentException("offset=" + offset + // NOI18N
                            "partStartOffset=" + partStartOffset + // NOI18N
                            ", partLength=" + partLength + // NOI18N
                            ", shift=" + shift
                    );
                }
                int breakCharIndex = Math.max(offset - (partStartOffset + partShift), 0);
                float breakCharIndexX;
                if (breakCharIndex != 0) {
                    TextHitInfo hit = TextHitInfo.afterOffset(breakCharIndex);
                    float[] locs = textLayout.getCaretInfo(hit);
                    breakCharIndexX = locs[0];
                } else {
                    breakCharIndexX = 0f;
                }
                TextHitInfo hitInfo = x2RelOffset(textLayout, breakCharIndexX + len);
                int breakPartLength = hitInfo.getCharIndex() - shift;
                // Length must be > 0; BTW TextLayout can't be constructed with empty string.
                boolean breakFailed = (breakPartLength == 0 || breakPartLength == partLength);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("HV.breakView(): <"  + partStartOffset + "," + (partStartOffset+partLength) + // NOI18N
                        "> => <" + offset + "," + (partStartOffset+breakPartLength) + // NOI18N
                        ">, x=" + x + ", len=" + len + // NOI18N
                        ", charIndexX=" + breakCharIndexX + "\n"); // NOI18N
                }
                if (breakFailed) {
                    return null;
                }
                return new HighlightsViewPart(fullView, shift, breakPartLength);
            }
        }
        return null;
    }

    @Override
    public View createFragment(int p0, int p1) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("HV.createFragment(" + p0 + "," + p1+ "): <" + getStartOffset() + "," + // NOI18N
                    getEndOffset() + ">\n"); // NOI18N
        }
        int shift = p0 - getStartOffset();
        int len = p1 - p0;
        return new HighlightsViewPart(this, shift, len);
    }

    @Override
    protected String getDumpName() {
        return "HV";
    }

    @Override
    public String toString() {
        return appendViewInfo(new StringBuilder(200), 0, -1).toString();
    }

}
