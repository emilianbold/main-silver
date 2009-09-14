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
package org.netbeans.modules.dlight.msa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.modules.dlight.indicators.DataRowToTimeSeries;
import org.netbeans.modules.dlight.indicators.TimeSeriesDescriptor;
import org.netbeans.modules.dlight.indicators.TimeSeriesIndicatorConfiguration;
import org.netbeans.modules.dlight.msa.support.MSASQLTables;
import org.netbeans.modules.dlight.procfs.ProcFSDCConfiguration;
import org.netbeans.modules.dlight.spi.tool.DLightToolConfigurationProvider;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.visualizers.api.ThreadMapVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.ThreadStateResources;
import org.openide.util.NbBundle;

public class ThreadMapToolConfigurationProvider implements DLightToolConfigurationProvider {

    private final static Logger log = DLightLogger.getLogger(ThreadMapToolConfigurationProvider.class);
    public static final int INDICATOR_POSITION = 10;
    private static final String ID = "dlight.tool.threadmap"; // NOI18N
    private static final String TOOL_NAME = loc("ThreadMapTool.ToolName"); // NOI18N
    private static final String DETAILED_TOOL_NAME = loc("ThreadMapTool.DetailedToolName"); // NOI18N

    public DLightToolConfiguration create() {
        final DLightToolConfiguration toolConfiguration = new DLightToolConfiguration(ID, TOOL_NAME);
        toolConfiguration.setLongName(DETAILED_TOOL_NAME);

        final List<Column> indicatorDataColumns = Arrays.asList(
                MSASQLTables.prstat.P_SLEEP,
                MSASQLTables.prstat.P_WAIT,
                MSASQLTables.prstat.P_BLOCKED,
                MSASQLTables.prstat.P_RUNNING);

        final TimeSeriesIndicatorConfiguration indicatorConfig = new TimeSeriesIndicatorConfiguration(
                new IndicatorMetadata(indicatorDataColumns), INDICATOR_POSITION);

        indicatorConfig.setTitle(loc("ThreadMapTool.Indicator.Title")); // NOI18N
        indicatorConfig.setGraphScale(1);

        indicatorConfig.addTimeSeriesDescriptors(
                new TimeSeriesDescriptor(ThreadStateResources.THREAD_SLEEPING.color, ThreadStateResources.THREAD_SLEEPING.name, TimeSeriesDescriptor.Kind.REL_SURFACE), // NOI18N
                new TimeSeriesDescriptor(ThreadStateResources.THREAD_WAITING.color, ThreadStateResources.THREAD_WAITING.name, TimeSeriesDescriptor.Kind.REL_SURFACE), // NOI18N
                new TimeSeriesDescriptor(ThreadStateResources.THREAD_BLOCKED.color, ThreadStateResources.THREAD_BLOCKED.name, TimeSeriesDescriptor.Kind.REL_SURFACE), // NOI18N
                new TimeSeriesDescriptor(ThreadStateResources.THREAD_RUNNING.color, ThreadStateResources.THREAD_RUNNING.name, TimeSeriesDescriptor.Kind.REL_SURFACE)); // NOI18N
        indicatorConfig.setDataRowHandler(new IndicatorDataHandler(indicatorDataColumns));
        indicatorConfig.setActionDisplayName(loc("ThreadMapTool.Indicator.Action")); // NOI18N

        // Here we should configure visualizer...
        // TODO: Currently is dummy imlpementation.
        // We say which data it will visualize and which column is a "thread",
        // which colors to use and so on...
        // Moreover, later, on opening visualizer, a tables that are returned by 
        // getMetadata() if visualizerConfiguration (SEARCHED BY TABLE NAME(!))
        // will be searched in DB...
        VisualizerConfiguration visualizerConfig = new ThreadMapVisualizerConfiguration();

        indicatorConfig.addVisualizerConfiguration(visualizerConfig);
        toolConfiguration.addIndicatorConfiguration(indicatorConfig);

        ProcFSDCConfiguration procFSDCConfig = new ProcFSDCConfiguration();
        procFSDCConfig.collectProcInfo(1000);
        procFSDCConfig.collectMSA(1000);
        toolConfiguration.addIndicatorDataProviderConfiguration(procFSDCConfig);
        toolConfiguration.addDataCollectorConfiguration(procFSDCConfig);

//        CLIODCConfiguration prstatConfig = new CLIODCConfiguration(
//                "/bin/prstat", "-mL -p @PID -c 1", // NOI18N
//                new MSAPrstatParser1(msaTableMetadata), Arrays.asList(msaTableMetadata));
//        prstatConfig.setName("SunStudio"); // NOI18N

//        toolConfiguration.addIndicatorDataProviderConfiguration(prstatConfig);
//        toolConfiguration.addDataCollectorConfiguration(prstatConfig);

//        prstatConfig.setName("SunStudio");//NOI18N
//        dataCollectorConfigurations.add(prstatConfig);

        // Enable call stacks without dependency on CPU tool.
        toolConfiguration.addDataCollectorConfiguration(DTDCConfiguration.createCpuSamplingConfiguration());

        return toolConfiguration;
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(
                ThreadMapToolConfigurationProvider.class, key, params);
    }

//    private DataTableMetadata createMSATableMetadata() {
//        Column timestamp = new Column("ts", Long.class); // NOI18N
//        Column threadID = new Column("thrID", Integer.class); // NOI18N
//        Column usrTime = new Column(MSAState.RunningUser.toString(), Integer.class);
//        Column sysTime = new Column(MSAState.RunningSystemCall.toString(), Integer.class);
//        Column othTime = new Column(MSAState.RunningOther.toString(), Integer.class);
//        Column tpfTime = new Column(MSAState.SleepingUserTextPageFault.toString(), Integer.class);
//        Column dpfTime = new Column(MSAState.SleepingUserDataPageFault.toString(), Integer.class);
//        Column kpfTime = new Column(MSAState.SleepingKernelPageFault.toString(), Integer.class);
//        Column lckTime = new Column(MSAState.SleepingUserLock.toString(), Integer.class);
//        Column slpTime = new Column(MSAState.SleepingOther.toString(), Integer.class);
//        Column latTime = new Column(MSAState.WaitingCPU.toString(), Integer.class);
//        Column stpTime = new Column(MSAState.ThreadStopped.toString(), Integer.class);
//
//        return new DataTableMetadata("MSAData", // NOI18N
//                Arrays.asList(timestamp, threadID, usrTime, sysTime, othTime, tpfTime, dpfTime, kpfTime, lckTime, slpTime, latTime, stpTime), null);
//    }
//    private DataTableMetadata createVisualizerTableMetadata() {
//        Column threads = new Column("threads", Integer.class); // NOI18N
//        Column usrTime = new Column(MSAState.RunningUser.toString(), Integer.class);
//        Column sysTime = new Column(MSAState.RunningSystemCall.toString(), Integer.class);
//        Column othTime = new Column(MSAState.RunningOther.toString(), Integer.class);
//        Column tpfTime = new Column(MSAState.SleepingUserTextPageFault.toString(), Integer.class);
//        Column dpfTime = new Column(MSAState.SleepingUserDataPageFault.toString(), Integer.class);
//        Column kpfTime = new Column(MSAState.SleepingKernelPageFault.toString(), Integer.class);
//        Column lckTime = new Column(MSAState.SleepingUserLock.toString(), Integer.class);
//        Column slpTime = new Column(MSAState.SleepingOther.toString(), Integer.class);
//        Column latTime = new Column(MSAState.WaitingCPU.toString(), Integer.class);
//        Column stpTime = new Column(MSAState.ThreadStopped.toString(), Integer.class);
//
//        return new DataTableMetadata("MSA", // NOI18N
//                Arrays.asList(threads, usrTime, sysTime, othTime, tpfTime, dpfTime, kpfTime, lckTime, slpTime, latTime, stpTime), null);
//    }
    private static class IndicatorDataHandler implements DataRowToTimeSeries {

        private final static Object lock = new String(IndicatorDataHandler.class.getName());
        private final List<String> colNames;
        private final float[] data;

        private IndicatorDataHandler(List<Column> indicatorDataColumns) {
            colNames = new ArrayList<String>();

            for (Column c : indicatorDataColumns) {
                colNames.add(c.getColumnName());
            }

            data = new float[colNames.size()];
        }

        public void addDataRow(DataRow row) {
            int idx = 0;

            synchronized (lock) {
                for (String cn : colNames) {
                    float f = 0;
                    try {
                        f = row.getFloatValue(cn);
                    } catch (NumberFormatException ex) {
                        if (log.isLoggable(Level.FINE)) {
                            log.log(Level.FINE, "Will not add this entry", ex); // NOI18N
                        }

                    }
                    data[idx++] = f;
                }
            }
        }

        public void tick(float[] data, Map<String, String> details) {
            synchronized (lock) {
                System.arraycopy(this.data, 0, data, 0, data.length);
            }
        }
    }
//    private static class DataRowToMSA implements DataRowToTimeSeries {
//
//        private final List<Column> columns;
//        private float[] data;
//
//        public DataRowToMSA(List<Column> columns) {
//            this.columns = new ArrayList<Column>(columns);
//            this.data = new float[STATE_COUNT];
//        }
//
//        public void addDataRow(DataRow row) {
//            String threadsColumn = columns.get(0).getColumnName();
//            int threads = DataUtil.toInt(row.getData(threadsColumn), 1);
//            float[] newData = new float[STATE_COUNT];
//            int sum = 0;
//            for (int i = 1; i < columns.size(); ++i) {
//                String columnName = columns.get(i).getColumnName();
//                Object value = row.getData(columnName);
//                if (value != null) {
//                    int intValue = DataUtil.toInt(value);
//                    int state = mapMicrostateToIndex(i - 1);
//                    if (0 <= state && state < STATE_COUNT) {
//                        newData[state] += intValue;
//                        sum += intValue;
//                    }
//                }
//            }
//            if (0 < sum) {
//                for (int i = 0; i < newData.length; ++i) {
//                    newData[i] = threads * newData[i] / sum;
//                }
//                data = newData;
//            }
//        }
//
//        public void tick(float[] data, Map<String, String> details) {
//            System.arraycopy(this.data, 0, data, 0, data.length);
//        }
//    }
//
//    /*
//     * Maps microstate index (LMS_USER = 0, LMS_SYSTEM = 1, etc)
//     * to index in array returned to indicator.
//     */
//    private static int mapMicrostateToIndex(int microstate) {
//        MSAState state = ThreadStateMapper.toSimpleState(MSAState.values()[microstate + MSAState.START_LONG_LIST.ordinal() + 1]);
//        switch (state) {
//            case Running:
//                return 3;
//            case Blocked:
//                return 2;
//            case Waiting:
//                return 1;
//            case Sleeping:
//                return 0;
//            default:
//                return -1; // out of range
//        }
//    }
}
