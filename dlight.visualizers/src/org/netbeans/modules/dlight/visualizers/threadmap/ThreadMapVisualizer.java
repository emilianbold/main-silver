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

import java.awt.event.ActionEvent;
import java.util.List;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.core.stack.api.ThreadDump;
import org.netbeans.modules.dlight.core.stack.api.ThreadSnapshot;
import org.netbeans.modules.dlight.management.api.DLightSession;
import org.netbeans.modules.dlight.management.api.DLightSession.SessionState;
import org.netbeans.modules.dlight.visualizers.*;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.dlight.api.datafilter.DataFilterListener;
import org.netbeans.modules.dlight.api.datafilter.support.TimeIntervalDataFilter;
import org.netbeans.modules.dlight.api.storage.types.TimeDuration;
import org.netbeans.modules.dlight.core.stack.api.ThreadDumpQuery;
import org.netbeans.modules.dlight.core.stack.api.ThreadState.MSAState;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.management.api.SessionStateListener;
import org.netbeans.modules.dlight.spi.support.TimerBasedVisualizerSupport;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.threadmap.api.ThreadMapData;
import org.netbeans.modules.dlight.threadmap.api.ThreadMapSummaryData;
import org.netbeans.modules.dlight.threadmap.spi.dataprovider.ThreadMapDataProvider;
import org.netbeans.modules.dlight.threadmap.spi.dataprovider.ThreadMapDataQuery;
import org.netbeans.modules.dlight.threadmap.spi.dataprovider.ThreadMapSummaryDataQuery;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.visualizers.api.ThreadMapVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.ThreadStateResources;
import org.netbeans.modules.dlight.visualizers.threadmap.ThreadStackVisualizerConfiguration.StackNameProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class ThreadMapVisualizer extends JPanel implements
        Visualizer<ThreadMapVisualizerConfiguration>, ActionListener, SessionStateListener, DataFilterListener {

    private boolean isEmptyContent;

    private static final class QueryLock {
    }
    private Future<Boolean> task;
    private final Object queryLock = new QueryLock();
    private final ThreadMapDataProvider provider;
    private final ThreadMapVisualizerConfiguration configuration;

    private static final class UiLock {
    }
    private final Object uiLock = new UiLock();
    //private final List<String> columnNames = new ArrayList<String>();
    //private final List<Class> columnClasses = new ArrayList<Class>();
    private final JPanel threadsTimelinePanelContainer;
    private final ThreadsPanel threadsPanel;
    private final ThreadsDataManager dataManager;
    private long startTimeStamp;
    private TimerBasedVisualizerSupport timerSupport;
    private DLightSession session;
    private Collection<TimeIntervalDataFilter> lastTimeFilters;
    private String toolID;

    public ThreadMapVisualizer(ThreadMapDataProvider provider, ThreadMapVisualizerConfiguration configuration) {

        this.provider = provider;
        this.configuration = configuration;

        //for (Column col : configuration.getMetadata().getColumns()) {
        //    columnNames.add(col.getColumnUName());
        //    columnClasses.add(col.getColumnClass());
        //}
        dataManager = new ThreadsDataManager();

        threadsPanel = new ThreadsPanel(dataManager, new ThreadsPanel.ThreadsDetailsCallback() {

            public void showStack(long startTime, final ThreadDumpQuery query) {
                DLightExecutorService.submit(new Runnable() {

                    public void run() {
                        final ThreadDump threadDump = ThreadMapVisualizer.this.provider.getThreadDump(query);
                        UIThread.invoke(new Runnable() {

                            public void run() {
                                StackNameProvider stackNameProvider = new StackNameProvider() {

                                    public String getStackName(ThreadSnapshot snapshot) {
                                        String name = "";
                                        MSAState msa = snapshot.getState();
                                        ThreadStateResources res = ThreadStateResources.forState(msa);
                                        if (res != null) {
                                            name = res.name;
                                        }
                                        long time = ThreadStateColumnImpl.timeInervalToMilliSeconds(snapshot.getTimestamp());
                                        String at = TimeLineUtils.getMillisValue(time);
                                        return NbBundle.getMessage(ThreadMapVisualizer.class, "ThreadStackVisualizerStackAt1", //NOI18N
                                                name, dataManager.findThreadName(snapshot.getThreadInfo().getThreadId()), at);
                                    }
                                };
                                DLightManager.getDefault().openVisualizer(session, toolID, new ThreadStackVisualizerConfiguration(query.getStartTime(), threadDump, stackNameProvider));
                            }
                        });
                        session.cleanAllDataFilter(ThreadDumpFilter.class);
                        session.addDataFilter(new ThreadDumpFilter(query.getStartTime(), threadDump), false);

                    }
                }, "Thread Dump  request from Thread Map Visualizer");//NOI18N

//                DataProvider d = session == null ? null : session.createDataProvider(DataModelSchemeProvider.getInstance().getScheme("model:stack"), CpuSamplingSupport.CPU_SAMPLE_TABLE); //NOI18N
                //              final StackDataProvider stackDataProvider = d == null || !(d instanceof StackDataProvider) ? null : (StackDataProvider) d;
//                ThreadStackVisualizer visualizer = new ThreadStackVisualizer(stackDataProvider, threadDump, query.getStartTime());
//                CallStackTopComponent tc = CallStackTopComponent.findInstance();
//                tc.addVisualizer(toolID, visualizer.getDisplayName(), visualizer);
//                tc.open();
//                tc.requestVisible();
//                tc.requestFocus(true);
                //return null;

            }
        });

        threadsTimelinePanelContainer = new JPanel() {

            @Override
            public void requestFocus() {
                threadsPanel.requestFocus();
            }
        };

        threadsTimelinePanelContainer.setLayout(new BorderLayout());
        threadsTimelinePanelContainer.add(threadsPanel, BorderLayout.CENTER);
        threadsTimelinePanelContainer.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        //threadsPanel.addThreadsMonitoringActionListener(this);

        setLayout(new BorderLayout());
        add(threadsTimelinePanelContainer, BorderLayout.CENTER);
    }

    public final void setToolID(String toolID) {
        this.toolID = toolID;
    }

    public void dataFiltersChanged(List<DataFilter> newSet, boolean isAdjusting) {
        //filter out with the time
        if (session != null) {
            if (EventQueue.isDispatchThread()) {
                DLightExecutorService.submit(new Runnable() {

                    public void run() {
                        update();
                    }
                }, "ThreadMapVisualizer. Request Data when filters are changed");//NOI18N

            }else{
                update();
            }
        }

    }

    private final void update() {
        final Collection<TimeIntervalDataFilter> timeFilters = session.getDataFilter(TimeIntervalDataFilter.class);
        lastTimeFilters = timeFilters;
        setTimeIntervalSelection(timeFilters);
        final ThreadMapSummaryData summaryData = ThreadMapVisualizer.this.provider.queryData(new ThreadMapSummaryDataQuery(timeFilters, true));
        UIThread.invoke(new Runnable() {

            public void run() {
                updateList(null, summaryData);
            }
        });
    }

    private final void setTimeIntervalSelection(Collection<TimeIntervalDataFilter> timeFilters) {
        threadsPanel.setTimeIntervalSelection(timeFilters);
    }

    public void init() {
        timerSupport = new TimerBasedVisualizerSupport(this, new TimeDuration(TimeUnit.SECONDS, 1));
        startTimeStamp = 0;
        dataManager.reset();
        startup();
    }

    public void startup() {
        if (session != null) {
            switch (session.getState()) {
                case RUNNING:
                case STARTING:
                    startTimeStamp = 0;
                    dataManager.reset();
                    dataManager.startup();
                    timerSupport.start();
                    break;
                default:
                    timerSupport.stop();
                    dataManager.shutdown();
            }
        }
    }

    public void shutdown() {
        timerSupport.stop();
        dataManager.shutdown();
        dataManager.reset();
        startTimeStamp = 0;
    }

    public void actionPerformed(ActionEvent e) {
        refresh();
    }

    //implements Visualizer
    public ThreadMapVisualizerConfiguration getVisualizerConfiguration() {
        return configuration;
    }

    public JComponent getComponent() {
        return this;
    }

    public VisualizerContainer getDefaultContainer() {
        return ThreadMapTopComponent.findInstance();
    }

    private void asyncFillModel(boolean cancelIfNotDone) {
        synchronized (queryLock) {
            if (task != null && !task.isDone()) {
                if (cancelIfNotDone) {
                    task.cancel(true);
                } else {
                    return;
                }
            }

            task = DLightExecutorService.submit(new Callable<Boolean>() {

                public Boolean call() {
                    syncFillModel();
                    return Boolean.TRUE;
                }
            }, "ThreadMapVisualizer request for the ThreadMapData");//NOI18N
        }
    }

    private void syncFillModel() {
        final ThreadMapData mapData = ThreadMapVisualizer.this.provider.queryData(new ThreadMapDataQuery(startTimeStamp, true, false));
        final ThreadMapSummaryData summaryData = ThreadMapVisualizer.this.provider.queryData(new ThreadMapSummaryDataQuery(lastTimeFilters, true));
        final boolean isEmptyConent = mapData == null || mapData.getThreadsData().isEmpty();
        UIThread.invoke(new Runnable() {

            public void run() {
                setContent(isEmptyConent);
                if (isEmptyConent) {
                    return;
                }
                updateList(mapData, summaryData);
            }
        });

    }

    public void refresh() {
        if (EventQueue.isDispatchThread()) {
            asyncFillModel(false);
        } else {
            syncFillModel();
        }

    }

    private void setContent(boolean isEmpty) {
        if (isEmptyContent && isEmpty) {
            return;
        }
        if (isEmptyContent && !isEmpty) {
            setNonEmptyContent();
            return;
        }
        if (!isEmptyContent && isEmpty) {
            setEmptyContent();
            return;
        }
    }

    private void setEmptyContent() {
        isEmptyContent = true;
        //removeAll();
        //setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //JLabel label = new JLabel("Empty"); //NOI18N
        //label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        //this.add(label);
        //repaint();
        //revalidate();
    }

    private void setNonEmptyContent() {
        isEmptyContent = false;
        //this.removeAll();
        //this.setLayout(new BorderLayout());
        //refresh();
        //repaint();
        //validate();

    }

    protected void updateList(ThreadMapData mapData, ThreadMapSummaryData summaryData) {
        synchronized (uiLock) {
            if (mapData != null) {
                threadsPanel.threadsMonitoringEnabled();
                dataManager.processData(MonitoredData.getMonitoredData(mapData), session, provider);
                startTimeStamp = dataManager.getEndTimeStump();
            }
            dataManager.processData(summaryData);
            setNonEmptyContent();
        }
    }

    public void sessionStateChanged(DLightSession session, SessionState oldState, SessionState newState) {
        this.session = session;
        if (session != null && newState != SessionState.CLOSED) {
            session.addDataFilterListener(this);
        }
        switch (newState) {
            case CLOSED:
            case PAUSED:
            case ANALYZE:
                startTimeStamp = 0;
                timerSupport.stop();
                dataManager.shutdown();
                refresh();
                break;
            case RUNNING:
            case STARTING:
                timerSupport.start();
                break;
        }
    }
}
