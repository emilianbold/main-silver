/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.dlight.indicators.graph;

import java.awt.FontMetrics;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.util.ValueFormatter;
import org.netbeans.modules.dlight.indicators.TimeSeriesDescriptor;
import java.awt.Graphics;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.dlight.api.datafilter.DataFilterListener;
import org.netbeans.modules.dlight.api.datafilter.DataFilterManager;
import org.netbeans.modules.dlight.extras.api.AxisMark;
import org.netbeans.modules.dlight.extras.api.AxisMarksProvider;
import org.netbeans.modules.dlight.extras.api.ViewportAware;
import org.netbeans.modules.dlight.extras.api.ViewportModel;
import org.netbeans.modules.dlight.util.Range;
import org.netbeans.modules.dlight.extras.api.support.DefaultViewportModel;
import org.netbeans.modules.dlight.extras.api.support.TimeMarksProvider;
import org.netbeans.modules.dlight.extras.api.support.ValueMarksProvider;
import org.netbeans.modules.dlight.api.datafilter.support.TimeIntervalDataFilter;
import org.netbeans.modules.dlight.util.Util;
import org.netbeans.modules.dlight.util.ui.DLightUIPrefs;

/**
 * Displays a graph
 * @author Vladimir Kvashin
 * @author Alexey Vladykin
 */
public class TimeSeriesPlot extends JComponent implements ViewportAware, ChangeListener, DataFilterListener {

    private static final long EXTENT = 20000; // 20 seconds

    private final GraphPainter graph;
    private ViewportModel viewportModel;
    private int upperLimit;
    private Axis hAxis;
    private Axis vAxis;
    private AxisMarksProvider timeMarksProvider;
    private AxisMarksProvider valueMarksProvider;
    private final Object timeFilterLock = new Object();
    private volatile TimeIntervalDataFilter timeFilter;

    public TimeSeriesPlot(int scale, ValueFormatter formatter, List<TimeSeriesDescriptor> series) {
        upperLimit = scale;
        graph = new GraphPainter(series);
        graph.addData(new float[series.size()]); // 0th tick - all zeros
        timeMarksProvider = TimeMarksProvider.newInstance();
        valueMarksProvider = ValueMarksProvider.newInstance(formatter);
        DefaultViewportModel model = new DefaultViewportModel(new Range<Long>(0L, 0L), new Range<Long>(0L, EXTENT));
        model.setMinViewportSize(1000L); // 1 second
        setViewportModel(model);
        setOpaque(true);
//        ToolTipManager.sharedInstance().registerComponent(this);
//        addAncestorListener(new AncestorListener() {
//            public void ancestorAdded(AncestorEvent event) {
//                graph.setSize(getWidth(), getHeight());
//            }
//            public void ancestorRemoved(AncestorEvent event) {}
//            public void ancestorMoved(AncestorEvent event) {}
//        });
    }

    public JComponent getVerticalAxis() {
        if (vAxis == null) {
            vAxis = new Axis(AxisOrientation.VERTICAL);
        }
        return vAxis;
    }

    public JComponent getHorizontalAxis() {
        if (hAxis == null) {
            hAxis = new Axis(AxisOrientation.HORIZONTAL);
        }
        return hAxis;
    }

    public void setUpperLimit(int newScale) {
        if (newScale != upperLimit) {
            upperLimit = newScale;
            repaint();
            if (vAxis != null) {
                vAxis.repaint();
            }
        }
    }

    public int getUpperLimit() {
        return upperLimit;
    }

    public int calculateUpperLimit(float... data) {
        return graph.calculateUpperLimit(data);
    }

    @Override
    protected void paintComponent(Graphics g) {
        FontMetrics xfm = g.getFontMetrics(DLightUIPrefs.getFont(DLightUIPrefs.INDICATOR_X_AXIS_FONT));
        FontMetrics yfm = g.getFontMetrics(DLightUIPrefs.getFont(DLightUIPrefs.INDICATOR_Y_AXIS_FONT));
        Range<Long> viewport = viewportModel.getViewport();
        List<AxisMark> timeMarks = timeMarksProvider.getAxisMarks(viewport.getStart(), viewport.getEnd(), getWidth(), xfm);
        List<AxisMark> valueMarks = valueMarksProvider.getAxisMarks(0, upperLimit, getHeight() - yfm.getAscent() / 2, yfm);
        int filterStart, filterEnd;
        TimeIntervalDataFilter tmpTimeFilter = timeFilter;
        if (tmpTimeFilter != null) {
            Range<Long> filterInterval = tmpTimeFilter.getInterval();
            filterStart = (int)TimeUnit.NANOSECONDS.toSeconds(filterInterval.getStart());
            filterEnd = (int)TimeUnit.NANOSECONDS.toSeconds(filterInterval.getEnd());
        } else {
            filterStart = Integer.MIN_VALUE;
            filterEnd = Integer.MAX_VALUE;
        }
        graph.paint(g, upperLimit, valueMarks, viewport.getStart(), viewport.getEnd(), timeMarks, filterStart, filterEnd, 0, 0, getWidth(), getHeight(), isEnabled());
    }

    public void addData(float... newData) {
        graph.addData(newData);
        viewportModel.setLimits(new Range<Long>(0L, TimeUnit.SECONDS.toMillis(graph.getDataSize())));
        repaintAll();
    }

    public ViewportModel getViewportModel() {
        return viewportModel;
    }

    public void setViewportModel(ViewportModel viewportModel) {
        if (this.viewportModel != null) {
            this.viewportModel.removeChangeListener(this);
        }
        this.viewportModel = viewportModel;
        this.viewportModel.addChangeListener(this);
        repaintAll();
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == viewportModel) {
            repaintAll();
        }
    }

    public void setDataFilterManager(DataFilterManager filterManager) {
        if (filterManager != null) {
            filterManager.addDataFilterListener(this);
        } else {
            synchronized (timeFilterLock) {
                timeFilter = null;
            }
        }
    }

    public void dataFiltersChanged(List<DataFilter> newSet, boolean isAdjusting) {
        TimeIntervalDataFilter newTimeFilter = Util.firstInstanceOf(TimeIntervalDataFilter.class, newSet);
        synchronized (timeFilterLock) {
            if (newTimeFilter != timeFilter) {
                timeFilter = newTimeFilter;
                repaintAll();
            }
        }
    }

    private void repaintAll() {
        repaint();
        if (hAxis != null) {
            hAxis.repaint();
        }
        if (vAxis != null) {
            vAxis.repaint();
        }
    }

    private static enum AxisOrientation {
        HORIZONTAL,
        VERTICAL
    }

    private class Axis extends JComponent {

        private final AxisOrientation orientation;

        public Axis(AxisOrientation orientation) {
            this.orientation = orientation;
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (isEnabled()) {
                switch (orientation) {
                    case VERTICAL:
                        FontMetrics yfm = g.getFontMetrics(DLightUIPrefs.getFont(DLightUIPrefs.INDICATOR_Y_AXIS_FONT));
                        List<AxisMark> valueMarks = valueMarksProvider.getAxisMarks(0, upperLimit, getHeight() - yfm.getAscent() / 2, yfm);
                        graph.paintVerticalAxis(g, 0, 0, getWidth(), getHeight(), valueMarks, getBackground());
                        break;
                    case HORIZONTAL:
                        Range<Long> viewport = viewportModel.getViewport();
                        FontMetrics xfm = g.getFontMetrics(DLightUIPrefs.getFont(DLightUIPrefs.INDICATOR_X_AXIS_FONT));
                        List<AxisMark> timeMarks = timeMarksProvider.getAxisMarks(viewport.getStart(), viewport.getEnd(), getWidth(), xfm);
                        graph.paintHorizontalAxis(g, 0, 0, getWidth(), getHeight(), timeMarks, getBackground());
                        break;
                }
            }
        }
    }
}
