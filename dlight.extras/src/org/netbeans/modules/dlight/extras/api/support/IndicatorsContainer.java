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
package org.netbeans.modules.dlight.extras.api.support;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.layout.GroupLayout;
import org.netbeans.modules.dlight.api.datafilter.DataFilterManager;
import org.netbeans.modules.dlight.extras.api.ViewportAware;
import org.netbeans.modules.dlight.util.Range;
import org.netbeans.modules.dlight.extras.api.ViewportModel;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.util.TimeFormatter;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.util.ui.DLightUIPrefs;
import org.openide.util.NbBundle;

/**
 * @author Alexey Vladykin
 */
public final class IndicatorsContainer extends JPanel
        implements AdjustmentListener, ChangeListener {

    private static final long EXTENT = 20000L; // 20 seconds
    private static final TimeFormatter TIME_FORMATTER = new TimeFormatter();

    private final JScrollPane indicatorsScrollPane;
    private final ViewportBar viewportBar;
    private final JScrollBar hScrollBar;
    private final JScrollBar vScrollBar;
    private final ViewportModel viewportModel;
    private final JLabel timeLabel;
    private boolean isAdjusting;

    public IndicatorsContainer(DataFilterManager filterManager, List<Indicator<?>> indicators) {
        viewportModel = new DefaultViewportModel();
        viewportModel.setLimits(new Range<Long>(0L, 0L));
        viewportModel.setViewport(new Range<Long>(0L, EXTENT));
        viewportModel.addChangeListener(this);

        indicatorsScrollPane = packIndicatorsIntoScrollPane(indicators, viewportModel);
        vScrollBar = indicatorsScrollPane.getVerticalScrollBar();
        vScrollBar.setVisible(false);
        indicatorsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        indicatorsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        indicatorsScrollPane.getViewport().addChangeListener(this);

        timeLabel = new JLabel(TIME_FORMATTER.format(0), JLabel.CENTER);
        timeLabel.setOpaque(false);
        timeLabel.setFont(DLightUIPrefs.getFont(DLightUIPrefs.INDICATOR_TITLE_FONT));
        timeLabel.setForeground(DLightUIPrefs.getColor(DLightUIPrefs.INDICATOR_TITLE_FONT_COLOR));

        int scrollBarButtonSize = UIManager.getInt("ScrollBar.width"); // NOI18N
        int padding = DLightUIPrefs.getInt(DLightUIPrefs.INDICATOR_PADDING);
        int leftMargin = padding + DLightUIPrefs.getInt(DLightUIPrefs.INDICATOR_Y_AXIS_WIDTH) - scrollBarButtonSize;
        int rightMargin = padding + DLightUIPrefs.getInt(DLightUIPrefs.INDICATOR_LEGEND_WIDTH) - scrollBarButtonSize;
        viewportBar = new ViewportBar(viewportModel, filterManager, scrollBarButtonSize, scrollBarButtonSize);

        hScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
        adjust();
        hScrollBar.addAdjustmentListener(this);

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.LEADING, true)
                    .add(indicatorsScrollPane, 200, 200, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                            .addContainerGap(leftMargin, leftMargin)
                            .add(layout.createParallelGroup(GroupLayout.LEADING, true)
                                    .add(GroupLayout.LEADING, hScrollBar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(GroupLayout.LEADING, viewportBar, 200 - leftMargin - rightMargin, 200 - leftMargin - rightMargin, Short.MAX_VALUE))
                            .add(timeLabel, GroupLayout.PREFERRED_SIZE, rightMargin, GroupLayout.PREFERRED_SIZE)))
                .add(vScrollBar)
        );

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.LEADING, true)
                .add(layout.createSequentialGroup()
                    .add(indicatorsScrollPane, 100, 100, Short.MAX_VALUE)
                    .add(layout.createParallelGroup(GroupLayout.CENTER, false)
                        .add(layout.createSequentialGroup()
                            .add(viewportBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .add(hScrollBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .add(timeLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .add(vScrollBar)
        );

    }

    private static JScrollPane packIndicatorsIntoScrollPane(List<Indicator<?>> indicators, ViewportModel viewportModel) {
        JScrollPane indicatorsComponent = null;
        if (indicators != null && !indicators.isEmpty()) {
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
            JSplitPane prevSplit = null;
            // We will resize only components without MaximumSize.
            // Implemented for Parallel Adviser indicator.
            int freeSizeComponentsNumber = 0;
            for (int i = 0; i < indicators.size(); ++i) {
                JComponent component = indicators.get(i).getComponent();
                if(!component.isMaximumSizeSet()) {
                    freeSizeComponentsNumber++;
                }
            }
            for (int i = 0; i < indicators.size(); ++i) {
                Indicator<?> indicator = indicators.get(i);
                JComponent component = indicators.get(i).getComponent();
                if (indicator instanceof ViewportAware) {
                    ((ViewportAware) indicator).setViewportModel(viewportModel);
                }
                if (i + 1 < indicators.size()) {
                    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                    splitPane.setBorder(BorderFactory.createEmptyBorder());
                    splitPane.setContinuousLayout(true);
                    splitPane.setDividerSize(5);
                    if(!component.isMaximumSizeSet()) {
                        splitPane.setResizeWeight(1.0 / (freeSizeComponentsNumber - i));
                    }
                    splitPane.setTopComponent(component);
                    component = splitPane;
                }
                if (prevSplit == null) {
                    scrollPane.setViewportView(component);
                } else {
                    prevSplit.setBottomComponent(component);
                }
                if (component instanceof JSplitPane) {
                    prevSplit = (JSplitPane) component;
                }
            }
            indicatorsComponent = scrollPane;
        } else {
            JLabel emptyLabel = new JLabel(getMessage("IndicatorsContainer.EmptyContent")); // NOI18N
            emptyLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            indicatorsComponent = new JScrollPane(emptyLabel);
        }
        indicatorsComponent.setBorder(new ThreeSidesBorder());
        return indicatorsComponent;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(DLightUIPrefs.getColor(DLightUIPrefs.INDICATOR_BORDER_COLOR));
        g.fillRect(0, viewportBar.getY(), indicatorsScrollPane.getWidth(), 2);
    }

    private void adjust() {
        Range<Long> limits = viewportModel.getLimits();
        timeLabel.setText(TIME_FORMATTER.format(Math.max(0, (int) TimeUnit.MILLISECONDS.toSeconds(limits.getEnd()) - 1)));

        Range<Long> viewport = viewportModel.getViewport();
        limits = limits.extend(viewport);
        isAdjusting = true;
        hScrollBar.setMinimum((int) TimeUnit.MILLISECONDS.toSeconds(limits.getStart()));
        hScrollBar.setMaximum((int) TimeUnit.MILLISECONDS.toSeconds(limits.getEnd()));
        hScrollBar.setValue((int) TimeUnit.MILLISECONDS.toSeconds(viewport.getStart()));
        hScrollBar.setVisibleAmount((int) TimeUnit.MILLISECONDS.toSeconds(viewport.getEnd() - viewport.getStart()));
        isAdjusting = false;
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == viewportModel) {
            UIThread.invoke(new Runnable() {
                public void run() {
                    adjust();
                }
            });
        } else if (e.getSource() == indicatorsScrollPane.getViewport()) {
            UIThread.invoke(new Runnable() {
                public void run() {
                    Dimension viewSize = indicatorsScrollPane.getViewport().getViewSize();
                    Dimension portSize = indicatorsScrollPane.getViewport().getExtentSize();
                    vScrollBar.setVisible(portSize.height < viewSize.height);
                }
            });
        }
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (!isAdjusting) {
            Range<Long> viewport = viewportModel.getViewport();
            long newViewportStart = TimeUnit.SECONDS.toMillis(e.getValue());
            viewportModel.setViewport(new Range<Long>(newViewportStart, newViewportStart + viewport.getEnd() - viewport.getStart()));
        }
    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(IndicatorsContainer.class, key);
    }

    private static class ThreeSidesBorder implements Border {

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(DLightUIPrefs.getColor(DLightUIPrefs.INDICATOR_BORDER_COLOR));
            g.fillRect(0, 0, width, 2); // top
            g.fillRect(0, 0, 2, height); // left
            g.fillRect(width - 2, 0, 2, height); // right
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 0, 2);
        }

        public boolean isBorderOpaque() {
            return true;
        }
    }
}
