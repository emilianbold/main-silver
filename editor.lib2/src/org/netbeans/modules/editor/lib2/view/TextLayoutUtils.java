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

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.Bidi;

/**
 * Utilities related to text layout and TextLayoutPart.
 * 
 * @author Miloslav Metelka
 */
final class TextLayoutUtils {
    
    private TextLayoutUtils() {
        // NO instances
    }

    public static float getHeight(TextLayout textLayout) {
        float height = textLayout.getAscent() + textLayout.getDescent() + textLayout.getLeading();
        // Ceil to whole points since when doing a compound TextLayout and then
        // using TextLayoutUtils.getRealAlloc() with its TL.getVisualHighlightShape() and doing
        // Graphics2D.fill(Shape) on the returned shape then for certain fonts such as
        // Lucida Sans Typewriter size=10 on Ubuntu 10.04 the background is rendered one pixel down for certain lines
        // so there appear white lines inside a selection.
        return (float) Math.ceil(height);
    }
    
    /**
     * Compute a most appropriate width of the given text layout.
     */
    public static float getWidth(TextLayout textLayout, String textLayoutText, Font font) {
        // For italic fonts the textLayout.getAdvance() includes some extra horizontal space.
        // On the other hand index2X() for TL.getCharacterCount() is width along baseline
        // so when TL ends with e.g. 'd' char the end of 'd' char is cut off.
        float width;
        int tlLen = textLayoutText.length();
        if (!font.isItalic() ||
            tlLen == 0 ||
            Character.isWhitespace(textLayoutText.charAt(tlLen - 1)) ||
            Bidi.requiresBidi(textLayoutText.toCharArray(), 0, textLayoutText.length()))
        {
            width = textLayout.getAdvance();
        } else { // Compute pixel bounds (with frc equal to textLayout's frc and default bounds)
            Rectangle pixelBounds = textLayout.getPixelBounds(null, 0, 0);
            width = (float) pixelBounds.getMaxX();
        }
//        width = textLayout.getPixelBounds(null, 0f, 0f).getMaxX();
//        width = Math.abs(textLayout.getAdvance());
        
        //
        // For RTL text the hit-info of the first char is above the hit-info of ending char.
        // However textLayout.isLeftToRight() returns true in case of mixture of LTR and RTL text
        // in a single textLayout.
        
        // Ceil the width to avoid rendering artifacts.
        width = (float) Math.ceil(width);
        return width;
    }
    
    /**
     * Get real allocation (possibly not rectangular) of a part of layout.
     * <br/>
     * It's used when rendering the text layout for filling background highlights of the view.
     *
     * @param length Total number of characters for which the allocation is computed.
     * @param alloc Allocation given by a parent view.
     * @return
     */
    public static Shape getRealAlloc(TextLayout textLayout, Rectangle2D textLayoutRect,
            TextHitInfo startHit, TextHitInfo endHit)
    {
        // Quick-fix to eliminate missing line in rendering italic "d" - more elaborate fix is needed
        textLayoutRect = new Rectangle2D.Double(textLayoutRect.getX(), textLayoutRect.getY(),
                textLayoutRect.getWidth() + 2, textLayoutRect.getHeight());
        Rectangle2D.Double zeroBasedRect = ViewUtils.shape2Bounds(textLayoutRect);
        zeroBasedRect.x = 0;
        zeroBasedRect.y = 0;
        Shape ret = textLayout.getVisualHighlightShape(startHit, endHit, zeroBasedRect);
        AffineTransform transform = AffineTransform.getTranslateInstance(
                textLayoutRect.getX(),
                textLayoutRect.getY()
        );
        ret = transform.createTransformedShape(ret);
        // The following gives bad result for some reason (works for layout but not for caret modelToView())
//        Shape ret2 = textLayout.getVisualHighlightShape(startHit.getCharIndex(), endHit.getCharIndex(), textLayoutRect);
        return ret;
    }
    
    public static String toStringShort(TextLayout textLayout) {
        return "[" + textLayout.getCharacterCount() + "]W=" + textLayout.getAdvance(); // NOI18N
    }

    public static String toString(TextLayout textLayout) {
        return toStringShort(textLayout) + "; " + // NOI18N
                textLayout.toString();
    }

}
