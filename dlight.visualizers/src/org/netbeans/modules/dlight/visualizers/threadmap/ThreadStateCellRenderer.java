/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.visualizers.threadmap;

import java.awt.AlphaComposite;
import org.netbeans.modules.dlight.visualizers.api.ThreadStateResources;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.dlight.api.datafilter.support.TimeIntervalDataFilter;
import org.netbeans.modules.dlight.core.stack.api.ThreadState;
import org.netbeans.modules.dlight.core.stack.api.ThreadState.MSAState;
import org.netbeans.modules.dlight.util.Range;

/**
 * @author Jiri Sedlacek
 * @author Alexander Simon (adapted for CND)
 */
public class ThreadStateCellRenderer extends JPanel implements TableCellRenderer, Serializable {

    private Color unselectedBackground;
    private Color unselectedForeground;
    private ThreadStateColumnImpl threadData;
    private ThreadsPanel viewManager; // view manager for this cell
    private long dataEnd;
    private long dataStart;
    private long viewEnd;
    private long viewStart;
    private TimeLine timeLine;
    private Collection<TimeIntervalDataFilter> timeFilters;
    private EnumMap<MSAState, AtomicInteger> map = new EnumMap<MSAState, AtomicInteger>(MSAState.class);
    private JTable table;
    private boolean isShiftCounted;
    private float BorderDarkerFactor = 0.9f;
    private float AlphaCompositeConstant = 0.5f;
    private Color TIME_SELECTION_BORDER = new Color(255, 150, 150);


    /** Creates a new instance of ThreadStateCellRenderer */
    public ThreadStateCellRenderer(ThreadsPanel viewManager) {
        this.viewManager = viewManager;
    }

    /**
     * Overrides <code>JComponent.setBackground</code> to assign
     * the unselected-background color to the specified color.
     *
     * @param c set the background color to this value
     */
    @Override
    public void setBackground(Color c) {
        super.setBackground(c);
        unselectedBackground = UIUtils.getProfilerResultsBackground();
    }

    /**
     * Overrides <code>JComponent.setForeground</code> to assign
     * the unselected-foreground color to the specified color.
     *
     * @param c set the foreground color to this value
     */
    @Override
    public void setForeground(Color c) {
        super.setForeground(c);
        unselectedForeground = c;
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public boolean isOpaque() {
        Color back = getBackground();
        Component p = getParent();

        if (p != null) {
            p = p.getParent();
        }

        boolean colorMatch = (back != null) && (p != null) && back.equals(p.getBackground()) && p.isOpaque();

        return !colorMatch && super.isOpaque();
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        if (isSelected) {
            super.setForeground(table.isFocusOwner() ? table.getSelectionForeground() : UIUtils.getUnfocusedSelectionForeground());
            super.setBackground(table.isFocusOwner() ? table.getSelectionBackground() : UIUtils.getUnfocusedSelectionBackground());
        } else {
            if ((row & 0x1) == 0) { //even row
                super.setForeground((unselectedForeground != null) ? unselectedForeground : table.getForeground());
                super.setBackground(UIUtils.getDarker((unselectedBackground != null) ? unselectedBackground : table.getBackground()));
            } else {
                super.setForeground((unselectedForeground != null) ? unselectedForeground : table.getForeground());
                super.setBackground((unselectedBackground != null) ? unselectedBackground : table.getBackground());
            }
        }

        if (value instanceof ThreadStateColumnImpl) {
            threadData = (ThreadStateColumnImpl) value;
        }

        viewStart = viewManager.getViewStart();
        viewEnd = viewManager.getViewEnd();
        dataStart = viewManager.getDataStart();
        dataEnd = viewManager.getDataEnd();
        timeLine = viewManager.getTimeLine();
        timeFilters = viewManager.getTimeIntervalSelection();
        this.table = table;

        return this;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        String tip = getToolTipText(event.getPoint());
        if (tip != null) {
            return tip;
        }
        return super.getToolTipText(event);
    }

    public String getToolTipText(Point point) {
        int index = getStateIndex(point);
        if (index >= 0) {
            ThreadState state = threadData.getThreadStateAt(index);
            long ms = ThreadStateColumnImpl.timeStampToMilliSeconds(state.getTimeStamp()) - dataStart;
            StringBuilder buf = new StringBuilder();
            buf.append("<html>");// NOI18N
            buf.append("Time ");// NOI18N
            buf.append(TimeLineUtils.getMillisValue(ms));
            EnumMap<MSAState, AtomicInteger> aMap = new EnumMap<MSAState, AtomicInteger>(MSAState.class);
            ThreadStateColumnImpl.fillMap(viewManager, state, aMap);
            ThreadStateColumnImpl.roundMap(aMap);
            buf.append("<table>");// NOI18N
            for(OrderedEnumStateIterator it = new OrderedEnumStateIterator(aMap); it.hasNext();){
                Map.Entry<MSAState, AtomicInteger> entry = it.next();
                int value = entry.getValue().get();
                MSAState s = entry.getKey();
                ThreadStateResources res = ThreadStateResources.forState(s);
                if (res != null) {
                    buf.append("<tr>");// NOI18N
                    buf.append("<td>");// NOI18N
                    buf.append("<font bgcolor=\"#");// NOI18N
                    buf.append(colorToHexString(res.color));
                    buf.append("\">&nbsp;&nbsp;");// NOI18N
                    buf.append("</font></td>");// NOI18N
                    buf.append("<td>");// NOI18N
                    buf.append(res.name);
                    buf.append("</td>");// NOI18N
                    buf.append("<td>");// NOI18N
                    buf.append(""+value+"%");// NOI18N
                    buf.append("</td>");// NOI18N
                    buf.append("</tr>");// NOI18N
                }
            }
            buf.append("</table>");// NOI18N
            buf.append("</html>");// NOI18N
            return buf.toString();
        }
        return null;
    }

    private int getStateIndex(Point point){
        if (threadData != null) {
            int index = getFirstVisibleDataUnit();
            if (index != -1) {
                int width = Math.abs(getX());
                if ((viewEnd - viewStart) > 0) {
                    float factor = (float) width / (float) (viewEnd - viewStart);

                    while ((index < threadData.size()) &&
                           (ThreadStateColumnImpl.timeStampToMilliSeconds(threadData.getThreadStateAt(index).getTimeStamp()) <= viewEnd)) {
                        // Thread alive
                        if (threadData.isAlive(index)) {
                            int x; // Begin of rectangle
                            int xx; // End of rectangle

                            ThreadState threadStateAt = threadData.getThreadStateAt(index);
                            x = Math.max((int) ((float) (ThreadStateColumnImpl.timeStampToMilliSeconds(threadStateAt.getTimeStamp()) - viewStart) * factor), 0);

                            if (index < (threadData.size() - 1)) {
                                xx = Math.min((int) ((float) (ThreadStateColumnImpl.timeStampToMilliSeconds(threadData.getThreadStateAt(index + 1).getTimeStamp()) - viewStart) * factor), width);
                            } else {
                                //xx = Math.min((int) ((dataEnd - viewStart) * factor), width + 1);
                                int interval = ThreadStateColumnImpl.timeInervalToMilliSeconds(threadStateAt.getMSASamplePeriod());
                                xx = Math.min((int) ((float) (ThreadStateColumnImpl.timeStampToMilliSeconds(threadStateAt.getTimeStamp()) + interval - viewStart) * factor + 0.5f), width);
                            }
                            if (x <= point.x && point.x < xx) {
                                return index;
                            }
                        }
                        index++;
                    }
                }
            }
        }
        return -1;
    }

    private static String colorToHexString(Color c) {
        // Result must be exactly 6 digits long.
        // Color values 0x0..0xf need special care.
        return String.format("%06x", c.getRGB() & 0xFFFFFF); // NOI18N
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    }

    private void calculateShift(){
        if (!isShiftCounted) {
            Point header = table.getTableHeader().getLocationOnScreen();
            Point stateRow = new Point(0,0);
            SwingUtilities.convertPointToScreen(stateRow, this);
            int nameWidth = table.getTableHeader().getColumnModel().getColumn(0).getWidth();
            int shift = stateRow.x - header.x - nameWidth;
            isShiftCounted = true;
            ThreadStateHeaderRenderer.shift = shift;
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        calculateShift();
        paintTimeMarks(g);

        if (threadData != null) {
            int index = getFirstVisibleDataUnit();

            if (index != -1) {
                int width = getWidth();

                if ((viewEnd - viewStart) > 0) {
                    float factor = (float) width / (float) (viewEnd - viewStart);

                    while ((index < threadData.size()) &&
                           (ThreadStateColumnImpl.timeStampToMilliSeconds(threadData.getThreadStateAt(index).getTimeStamp()) <= viewEnd)) {
                        // Thread alive
                        if (threadData.isAlive(index)) {
                            paintThreadState(g, index, threadData.getThreadStateAt(index), factor, width);
                        }

                        index++;
                    }
                }
            }
            paintGray(g);
        }
        paintTimeLine(g);
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void repaint(Rectangle r) {
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void revalidate() {
    }

    /**
     * Notification from the <code>UIManager</code> that the look and feel
     * [L&F] has changed.
     */
    @Override
    public void updateUI() {
        super.updateUI();
        setForeground(null);
        setBackground(null);
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void validate() {
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    }

    private int getFirstVisibleDataUnit() {
        for (int i = 0; i < threadData.size(); i++) {
            long timestamp = ThreadStateColumnImpl.timeStampToMilliSeconds(threadData.getThreadStateAt(i).getTimeStamp());

            if ((timestamp <= viewEnd) && (i == (threadData.size() - 1))) {
                return i; // last data unit before viewEnd
            }

            if (timestamp <= viewStart) {
                if (ThreadStateColumnImpl.timeStampToMilliSeconds(threadData.getThreadStateAt(i + 1).getTimeStamp()) > viewStart) {
                    return i; // data unit ends between viewStart and viewEnd
                }
            } else {
                if (timestamp <= viewEnd) {
                    return i; // data unit begins between viewStart and viewEnd
                }
            }
        }

        return -1;
    }

    private List<Range<Long>> getGray(){
        Collection<TimeIntervalDataFilter> aTimeFilters = timeFilters;
        if (aTimeFilters == null || aTimeFilters.isEmpty()) {
            return Collections.<Range<Long>>emptyList();
        }
        Range<Long> range = new Range<Long>((viewStart - dataStart)*1000*1000, (viewEnd - dataStart)*1000*1000);
        //System.err.println("Interval ["+range.getStart()/1000/1000+","+range.getEnd()/1000/1000+"]");
        ArrayList<Range<Long>> ranges = new ArrayList<Range<Long>>(timeFilters.size());
        for(TimeIntervalDataFilter filter : aTimeFilters) {
            //System.err.println("Selection ["+filter.getInterval().getStart()/1000/1000+","+filter.getInterval().getEnd()/1000/1000+"]");
            ranges.add(filter.getInterval());
        }
        return range.subtract(ranges);
    }

    private void paintThreadState(Graphics g, int index, ThreadState threadStateColor, float factor, int width) {
        int x; // Begin of rectangle
        int xx; // End of rectangle

        x = Math.max((int) ((float) (ThreadStateColumnImpl.timeStampToMilliSeconds(threadStateColor.getTimeStamp()) - viewStart) * factor), 0);

        if (index < (threadData.size() - 1)) {
            xx = Math.min((int) ((float) (ThreadStateColumnImpl.timeStampToMilliSeconds(threadData.getThreadStateAt(index + 1).getTimeStamp()) - viewStart) * factor), width);
        } else {
            //xx = Math.min((int) ((dataEnd - viewStart) * factor), width + 1);
            int interval = ThreadStateColumnImpl.timeInervalToMilliSeconds(threadStateColor.getMSASamplePeriod());
            xx = Math.min((int) ((float) (ThreadStateColumnImpl.timeStampToMilliSeconds(threadStateColor.getTimeStamp()) + interval - viewStart) * factor + 0.5f), width);
        }

        int delta = getHeight() - ThreadsPanel.THREAD_LINE_TOP_BOTTOM_MARGIN * 2;
        if (viewManager.isMSAMode()){
            for (AtomicInteger i : map.values()) {
                i.set(0);
            }

            ThreadStateColumnImpl.fillMap(viewManager, threadStateColor, map);
            ThreadStateColumnImpl.roundMap(map);

            int y = 0;
            int rest = ThreadState.POINTS/2;
            int oldRest = 0;
            for(OrderedEnumStateIterator it = new OrderedEnumStateIterator(map); it.hasNext();){
                Map.Entry<MSAState, AtomicInteger> entry = it.next();
                int v = entry.getValue().get();
                Color c = ThreadStateColumnImpl.getThreadStateColor(entry.getKey());
                oldRest = rest;
                rest = (v*delta+oldRest)%ThreadState.POINTS;
                int d = (v*delta+oldRest)/ThreadState.POINTS;
                y += d;
                if (d > 0) {
                    g.setColor(c);
                    g.fillRect(x, ThreadsPanel.THREAD_LINE_TOP_BOTTOM_MARGIN + delta - y, xx - x, d);
                }
            }
        } else {
            Color c = ThreadStateColumnImpl.getThreadStateColor(threadStateColor.getMSAState(threadStateColor.getSamplingStateIndex(viewManager.isFullMode()), viewManager.isFullMode()));
            g.setColor(c);
            g.fillRect(x, ThreadsPanel.THREAD_LINE_TOP_BOTTOM_MARGIN, xx - x, delta);
        }
    }

    private void paintGray(Graphics g) {
        if ((viewEnd - viewStart) > 0) {
            int width = getWidth();
            float factor = (float) width / (float) (viewEnd - viewStart);
            long firstValue = viewStart - dataStart;
            int i = 0;
            for (Range<Long> range : getGray()) {
                g.setColor(UIUtils.getDarker(getBackground(), BorderDarkerFactor));
                int x1 = (int) ((float) (range.getStart() / 1000 / 1000 - firstValue) * factor);
                int x2 = (int) ((float) (range.getEnd() / 1000 / 1000 - firstValue) * factor);
                Composite composite = ((Graphics2D) g).getComposite();
                ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, AlphaCompositeConstant));
                g.fillRect(x1, 0, x2, getHeight() - 1);
                ((Graphics2D) g).setComposite(composite);
                g.setColor(TIME_SELECTION_BORDER);
                if (x1 > 0) {
                    g.drawLine(x1, 0, x1, getHeight() - 1);
                }
                if (x2 < width) {
                    g.drawLine(x2, 0, x2, getHeight() - 1);
                }
                i++;
            }
        }
    }

    private void paintTimeMarks(Graphics g) {
        if ((viewEnd - viewStart) > 0) {
            int firstValue = (int) (viewStart - dataStart);
            int lastValue = (int) (viewEnd - dataStart);
            float factor = (float) getWidth() / (float) (viewEnd - viewStart);
            int optimalUnits = TimeLineUtils.getOptimalUnits(factor);

            int firstMark = Math.max((int) (Math.ceil((double) firstValue / optimalUnits) * optimalUnits), 0);

            int currentMark = firstMark - optimalUnits;

            while (currentMark <= (lastValue + optimalUnits)) {
                if (currentMark >= 0) {
                    float currentMarkRel = currentMark - firstValue;
                    int markPosition = (int) (currentMarkRel * factor);
                    paintTimeTicks(g, (int) (currentMarkRel * factor), (int) ((currentMarkRel + optimalUnits) * factor),
                            TimeLineUtils.getTicksCount(optimalUnits));
                    g.setColor(TimeLineUtils.MAIN_TIMELINE_COLOR);
                    g.drawLine(markPosition, 0, markPosition, getHeight() - 1);
                }

                currentMark += optimalUnits;
            }
        }
    }

    private void paintTimeLine(Graphics g) {
        if ((viewEnd - viewStart) > 0) {
            if (timeLine != null){
                long time = timeLine.getTimeStamp() + timeLine.getInterval() / 2;
                if (viewStart < time && time < viewEnd) {
                    int x = (int) ((getWidth() * (time - viewStart)) / (viewEnd - viewStart));
                    g.setColor(TimeLineUtils.TIMELINE_CURSOR_COLOR);
                    g.drawLine(x, 0, x, getHeight() - 1);
                }
            }
        }
    }

    private void paintTimeTicks(Graphics g, int startPos, int endPos, int count) {
        float factor = (float) (endPos - startPos) / (float) count;

        g.setColor(TimeLineUtils.TICK_TIMELINE_COLOR);

        for (int i = 1; i < count; i++) {
            int x = startPos + (int) (i * factor);
            g.drawLine(x, 0, x, getHeight() - 1);
        }
    }

}
