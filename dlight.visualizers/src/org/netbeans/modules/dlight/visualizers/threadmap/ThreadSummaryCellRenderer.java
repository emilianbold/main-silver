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

import org.netbeans.modules.dlight.visualizers.api.ThreadStateResources;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.dlight.api.datafilter.support.TimeIntervalDataFilter;
import org.netbeans.modules.dlight.core.stack.api.ThreadState;
import org.netbeans.modules.dlight.core.stack.api.ThreadState.MSAState;
import org.netbeans.modules.dlight.core.stack.api.support.ThreadStateMapper;
import org.netbeans.modules.dlight.threadmap.api.ThreadSummaryData.StateDuration;

/**
 * @author Alexander Simon
 */
public class ThreadSummaryCellRenderer extends JPanel implements TableCellRenderer, Serializable {
    private Color unselectedBackground;
    private Color unselectedForeground;
    private ThreadStateColumnImpl threadData;
    private ThreadSummaryColumnImpl threadSummary;
    private ThreadsPanel viewManager; // view manager for this cell
    private long threadTime;
    private long threadRunningTime;
    private long threadRunningRatio;
    private Collection<TimeIntervalDataFilter> timeFilters;
    private long dataStart;
    private EnumMap<MSAState, AtomicInteger> map = new EnumMap<MSAState, AtomicInteger>(MSAState.class);

    /** Creates a new instance of ThreadStateCellRenderer */
    public ThreadSummaryCellRenderer(ThreadsPanel viewManager) {
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
        } else if (value instanceof ThreadSummaryColumnImpl) {
            threadSummary = (ThreadSummaryColumnImpl) value;
        }
        timeFilters = viewManager.getTimeIntervalSelection();
        dataStart = viewManager.getDataStart();

        return this;
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        for (AtomicInteger i : map.values()) {
            i.set(0);
        }
        int count = countSum(map);
        threadTime = count;
        if (count > 0) {
            ThreadStateColumnImpl.normilizeMap(map, count, 1);
            ThreadStateColumnImpl.roundMap(map);
        }
        threadRunningTime = sumStates(MSAState.Running, MSAState.RunningUser, MSAState.RunningSystemCall, MSAState.RunningOther);
        int height = getHeight() - ThreadsPanel.THREAD_LINE_TOP_BOTTOM_MARGIN * 2;
        if (count > 0) {
            int rest = ThreadState.POINTS/2;
            int oldRest = 0;
            oldRest = 0;
            int y = 6;
            int ThreadWidth = ThreadsPanel.MIN_SUMMARY_COLUMN_WIDTH - 12;
            for(OrderedEnumStateIterator it = new OrderedEnumStateIterator(map); it.hasNext();){
                Map.Entry<MSAState, AtomicInteger> entry = it.next();
                AtomicInteger value = entry.getValue();
                oldRest = rest;
                rest = (value.get()*ThreadWidth+oldRest)%ThreadState.POINTS;
                int d = (value.get()*ThreadWidth+oldRest)/ThreadState.POINTS;
                if (d > 0) {
                    g.setColor(ThreadStateColumnImpl.getThreadStateColor(entry.getKey()));
                    g.fillRect(y, ThreadsPanel.THREAD_LINE_TOP_BOTTOM_MARGIN, d, height);
                }
                y += d;
            }
        }
        threadRunningRatio = sumStates(MSAState.Running, MSAState.RunningUser, MSAState.RunningSystemCall, MSAState.RunningOther);
        int percent = (int)(100*threadRunningRatio)/ThreadState.POINTS;
        String s = ""+percent+"%"; // NOI18N
        Font summary = new Font(null, Font.BOLD, height-2);
        g.setFont(summary);
        int y = getHeight() - ThreadsPanel.THREAD_LINE_TOP_BOTTOM_MARGIN - 2;
        g.setColor(UIUtils.getDarker(getBackground(),0.4f));
        for(int dx = -1; dx < 2; dx++) {
            for(int dy = -1; dy < 2; dy++) {
                g.drawString(s, 6 + 3 + dx, y + dy);
            }
        }
        g.setColor(getBackground());
        g.drawString(s, 6 + 3, y);
        if (threadData != null) {
            threadData.setRunning(percent);
        }
    }

    @Override
    public String getToolTipText() {
        EnumMap<MSAState, AtomicInteger> aMap = new EnumMap<MSAState, AtomicInteger>(MSAState.class);
        int count = countSum(aMap);
        if (count > 0) {
            ThreadStateColumnImpl.normilizeMap(aMap, count, 1);
            ThreadStateColumnImpl.roundMap(aMap);
            StringBuilder buf = new StringBuilder();
            buf.append("<html>");// NOI18N
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
                    buf.append(TimeLineUtils.getMillisValue(value*count*10));
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
        return super.getToolTipText();
    }

    private boolean isSelected(long ts){
        Collection<TimeIntervalDataFilter> aTimeFilters = timeFilters;
        if (aTimeFilters == null || aTimeFilters.isEmpty()) {
            return true;
        }
        for(TimeIntervalDataFilter filter : aTimeFilters) {
            if (filter.getInterval().cotains(ts - dataStart*1000*1000)){
                return true;
            }
        }
        return false;
    }

    private int countSum(EnumMap<MSAState, AtomicInteger> aMap) {
        int count = 0;
        if (threadData != null) {
            for (int i = 0; i < threadData.size(); i++) {
                ThreadState state = threadData.getThreadStateAt(i);
                if (!isSelected(state.getTimeStamp())) {
                    continue;
                }
                if (threadData.isAlive(i)) {
                    int delta = 0; // interval in 10 ms
                    if (i + 1 < threadData.size()) {
                        ThreadState next = threadData.getThreadStateAt(i+1);
                        delta = (int) ((next.getTimeStamp() - state.getTimeStamp())/(1000*1000*10));
                    } else {
                        delta = (int) (viewManager.getInterval()/(1000*1000*10));
                    }
                    count += delta;
                    for (int j = 0; j < state.size(); j++) {
                        MSAState msa = state.getMSAState(j, viewManager.isFullMode());
                        if (msa != null) {
                            int value = state.getState(j) * delta;
                            AtomicInteger v = aMap.get(msa);
                            if (v != null) {
                                v.addAndGet(value);
                            } else {
                                v = new AtomicInteger(value);
                                aMap.put(msa, v);
                            }
                        } else {
                            System.err.println("Wrong MSA at index " + i + " MSA=" + state); // NOI18N
                        }
                    }
                }
            }
            count = (count+50)/100; // in seconds
            ThreadStateColumnImpl.normilizeMap(aMap, 100, count);
        } else {
            for(StateDuration duration : threadSummary.getSummary()){
                MSAState msa = duration.getState();
                if (!viewManager.isFullMode()) {
                    msa = ThreadStateMapper.toSimpleState(msa);
                }
                if (msa != null) {
                    int value = (int) (duration.getDuration() / 1000 / 1000 /10);
                    count += value;
                    AtomicInteger v = aMap.get(msa);
                    if (v != null) {
                        v.addAndGet(value);
                    } else {
                        v = new AtomicInteger(value);
                        aMap.put(msa, v);
                    }
                }
            }
            count = (count+50)/100; // in seconds
            ThreadStateColumnImpl.normilizeMap(aMap, 100, count);
        }
        return count;
    }

    private static String colorToHexString(Color c) {
        // Result must be exactly 6 digits long.
        // Color values 0x0..0xf need special care.
        return String.format("%06x", c.getRGB() & 0xFFFFFF); // NOI18N
    }

    private int sumStates(MSAState ... states){
        int i = 0;
        for(MSAState state : states){
            AtomicInteger r = map.get(state);
            if (r != null) {
                i += r.get();
            }
        }
        return i;
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
}
