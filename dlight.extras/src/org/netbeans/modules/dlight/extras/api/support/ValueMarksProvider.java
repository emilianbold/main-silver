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
package org.netbeans.modules.dlight.extras.api.support;

import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.dlight.extras.api.AxisMark;
import org.netbeans.modules.dlight.util.DLightMath;
import org.netbeans.modules.dlight.util.ValueFormatter;

/**
 * @author Alexey Vladykin
 */
public final class ValueMarksProvider extends AbstractCachingAxisMarksProvider {

    public static ValueMarksProvider newInstance() {
        return new ValueMarksProvider(null);
    }

    public static ValueMarksProvider newInstance(ValueFormatter formatter) {
        return new ValueMarksProvider(formatter);
    }

    private final ValueFormatter formatter;

    private ValueMarksProvider(ValueFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    protected List<AxisMark> getAxisMarksImpl(long viewportStart, long viewportEnd, int axisSize, FontMetrics axisFontMetrics) {
        List<AxisMark> marks = new ArrayList<AxisMark>();
        createMarks(viewportStart, viewportEnd, 0, axisSize, axisFontMetrics, marks);
        marks.add(new AxisMark(axisSize, formatValue(viewportEnd)));
        return marks;
    }

    private void createMarks(long minVal, long maxVal, int minPos, int maxPos, FontMetrics axisFontMetrics, List<AxisMark> marks) {
        if (maxPos - minPos <= axisFontMetrics.getAscent()) {
            return;
        }
        int midPos = (minPos + maxPos) / 2;
        long midVal = (minVal + maxVal) / 2;
        if (axisFontMetrics.getAscent() <= midPos - minPos) {
            createMarks(minVal, midVal, minPos, midPos, axisFontMetrics, marks);
        }
        marks.add(new AxisMark(midPos, maxVal - minVal < 2 ? null : formatValue(midVal), DLightMath.map(maxPos - minPos, 3 * axisFontMetrics.getAscent() / 2, 2 * axisFontMetrics.getAscent(), 0, 255), DLightMath.map(maxPos - minPos, 2 * axisFontMetrics.getAscent(), 3 * axisFontMetrics.getAscent(), 0, 255)));
        if (axisFontMetrics.getAscent() <= maxPos - midPos) {
            createMarks(midVal, maxVal, midPos, maxPos, axisFontMetrics, marks);
        }
    }

    private String formatValue(long value) {
        if (formatter == null) {
            return String.valueOf(value);
        } else {
            return formatter.format(value);
        }
    }
}
