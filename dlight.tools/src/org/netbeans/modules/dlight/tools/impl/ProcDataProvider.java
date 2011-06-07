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
package org.netbeans.modules.dlight.tools.impl;

import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.tools.ProcDataProviderConfiguration;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.dlight.api.execution.AttachableTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.NativeProcessExecutionService;
import org.netbeans.modules.nativeexecution.api.util.AsynchronousAction;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.NbBundle;

/**
 * Indicator data provider that reads CPU usage information from
 * <code>/proc</code> filesystem. This is supported on Linux and Solaris only.
 *
 * @author Alexey Vladykin
 */
public class ProcDataProvider extends IndicatorDataProvider<ProcDataProviderConfiguration> implements DataRowConsumer {

    private static final String NAME = "ProcReader"; // NOI18N
    private static final List<DataTableMetadata> metadata;
    private Future<Integer> procReaderTask;

    static {
        metadata = Collections.singletonList(new DataTableMetadata(
                NAME, Arrays.asList(
                ProcDataProviderConfiguration.SYS_TIME,
                ProcDataProviderConfiguration.USR_TIME,
                ProcDataProviderConfiguration.THREADS),
                null));
    }

    public ProcDataProvider(ProcDataProviderConfiguration configuration) {
        super(NAME);
    }

    @Override
    public List<DataTableMetadata> getDataTablesMetadata() {
        return metadata;
    }

    @Override
    protected ValidationStatus doValidation(DLightTarget target) {
        ExecutionEnvironment env = target.getExecEnv();
        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
            AsynchronousAction connectAction = ConnectionManager.getInstance().getConnectToAction(env, new Runnable() {

                @Override
                public void run() {
                    DLightManager.getDefault().revalidateSessions();
                }
            });
            return ValidationStatus.unknownStatus(
                    getMessage("ValidationStatus.HostNotConnected"), // NOI18N
                    connectAction);
        }

        OSFamily osFamily = OSFamily.UNKNOWN;

        try {
            osFamily = HostInfoUtils.getHostInfo(env).getOSFamily();
        } catch (IOException ex) {
        } catch (CancellationException ex) {
        }

        if (osFamily != OSFamily.LINUX && osFamily != OSFamily.SUNOS) {
            return ValidationStatus.invalidStatus(getMessage("ValidationStatus.ProcReader.OSNotSupported")); // NOI18N
        }

        try {
            if (!HostInfoUtils.fileExists(env, "/proc")) { // NOI18N
                return ValidationStatus.invalidStatus(getMessage("ValidationStatus.ProcNotFound")); // NOI18N
            }
        } catch (InterruptedException ex) {
            return ValidationStatus.invalidStatus(getMessage("ValidationStatus.Interrupted"));
        } catch (IOException ex) {
            return ValidationStatus.invalidStatus(ex.getMessage());
        }

        return ValidationStatus.validStatus();
    }
    /*
     * Synchronization protects procReaderTask.
     */

    @Override
    protected synchronized void targetStarted(DLightTarget target) {
        ExecutionEnvironment env = target.getExecEnv();
        HostInfo hostInfo = null;
        try {
            hostInfo = HostInfoUtils.getHostInfo(env);
        } catch (IOException ex) {
        } catch (CancellationException ex) {
        }

        if (hostInfo == null) {
            return;
        }

        int pid = ((AttachableTarget) target).getPID();

        Engine engine;

        switch (hostInfo.getOSFamily()) {
            case LINUX:
                engine = new ProcDataProviderLinux(this, getServiceInfoDataStorage());
                break;
            case SUNOS:
                engine = new ProcDataProviderSolaris(this, getServiceInfoDataStorage(), hostInfo.getCpuNum());
                break;
            default:
                DLightLogger.instance.severe("Called ProcDataProvider.targetStarted() on unsupported OS"); // NOI18N
                return;
        }

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
        npb = npb.setExecutable("sh").setArguments("-c", engine.getCommand(pid)); // NOI18N

        procReaderTask = NativeProcessExecutionService.newService(npb, engine, null, "procreader").start(); // NOI18N
    }

    /*
     * Synchronization protects procReaderTask.
     */
    @Override
    protected synchronized void targetFinished(DLightTarget target) {
        if (procReaderTask != null) {
            if (!procReaderTask.isDone()) {
                procReaderTask.cancel(true);
            }
            procReaderTask = null;
        }
    }

    @Override
    public void dataFiltersChanged(List<DataFilter> newSet, boolean isAdjusting) {
    }

    /**
     * ProcDataProvider backend.
     */
    /*package*/ static interface Engine extends LineProcessor {

        String getCommand(int pid);
    }

    /**
     * To be used by engines.
     *
     * @param row  new data row
     */
    @Override
    public void consume(DataRow row) {
        super.notifyIndicators(Collections.singletonList(row));
    }

    private static String getMessage(String name) {
        return NbBundle.getMessage(ProcDataProvider.class, name);
    }
}
