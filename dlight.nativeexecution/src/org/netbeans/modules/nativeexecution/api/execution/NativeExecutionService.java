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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.api.execution;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.modules.terminal.api.IOEmulation;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.NativeProcessChangeEvent;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
import org.netbeans.modules.terminal.api.IOTerm;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

/**
 * This is a wrapper over an <tt>Executionservice</tt> that handles running
 * NativeProcesses in a terminal output window.
 *
 * It also can be used for running in an output windows - in this case it just
 * delegates execution to the <tt>ExecutionService</tt>
 *
 * @see ExecutionService
 * @see NativeExecutionDescriptor
 *
 * @author ak119685
 */
public final class NativeExecutionService {

    private final NativeProcessBuilder processBuilder;
    private final String displayName;
    private final NativeExecutionDescriptor descriptor;
    private static final Charset execCharset;
    private Runnable postExecutable;
    private final AtomicReference<NativeProcess> processRef = new AtomicReference<NativeProcess>();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private ProcessChangeListener listener;
    private long startTimeMillis;

    static {
        String charsetName = System.getProperty("org.netbeans.modules.nativeexecution.execcharset", "UTF-8"); // NOI18N
        Charset cs = null;
        try {
            cs = Charset.forName(charsetName);
        } catch (Exception ex) {
            cs = Charset.defaultCharset();
        } finally {
            execCharset = cs;
        }
    }

    private NativeExecutionService(NativeProcessBuilder processBuilder, String displayName, NativeExecutionDescriptor descriptor) {
        this.processBuilder = processBuilder;
        this.displayName = displayName;
        this.descriptor = descriptor;
    }

    public static NativeExecutionService newService(NativeProcessBuilder processBuilder,
            NativeExecutionDescriptor descriptor, String displayName) {
        return new NativeExecutionService(processBuilder, displayName, descriptor);
    }

    public Future<Integer> run() {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("NativeExecutionService state error - cannot be called more than once!"); // NOI18N
        }

        // FIXME: processBuilder has no removeProcessListener method...
        // So have to add a weak listener...
        listener = new ProcessChangeListener();
        processBuilder.addNativeProcessListener(WeakListeners.create(ChangeListener.class, listener, processBuilder));

        postExecutable = descriptor.postExecution;
        descriptor.postExecution(new PostRunnable());

        if (IOTerm.isSupported(descriptor.inputOutput)) {
            return runTerm();
        } else {
            return runRegular();
        }
    }

    private Future<Integer> runTerm() {
        processBuilder.setUsePty(true);

        if (IOEmulation.isSupported(descriptor.inputOutput)) {
            processBuilder.getEnvironment().put("TERM", IOEmulation.getEmulation(descriptor.inputOutput)); // NOI18N
        } else {
            processBuilder.getEnvironment().put("TERM", "dumb"); // NOI18N
        }

        Callable<Integer> callable = new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                try {
                    final NativeProcess process = processBuilder.call();

                    if (descriptor.frontWindow) {
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                descriptor.inputOutput.select();
                            }
                        });

                    }

                    PtySupport.connect(descriptor.inputOutput, process);
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            // connected
                        }
                    });

                    if (process.getState() == State.ERROR) {
                        descriptor.inputOutput.getErr().print(ProcessUtils.readProcessErrorLine(process));
                        descriptor.inputOutput.getErr().println('\r');
                        return 1;
                    }

                    return process.waitFor();
                } finally {
                    // draining...
                    // before starting post execution routine
                    // need to be sure that all process'es output was read
                    final NativeProcess p = processRef.get();
                    if (p != null) {
                        final InputStream is = p.getInputStream();
                        if (is != null) {
                            while (true) {
                                try {
                                    if (is.available() == 0) {
                                        is.close();
                                        break;
                                    }
                                } catch (IOException ex) {
                                    // already closed ... that's OK
                                    break;
                                }
                            }
                        }
                    }

                    // After we are sure that our output was read, and queued in
                    // terminal, our runnable will be started only after
                    // all streams will be drained (by the terminal)...
                    // There is one thing.. If, for some reason, terminal is not
                    // connected at this point, continuation passed to the
                    // disconnect() method will not be executed.
                    // So do it directly...

                    IOTerm.disconnect(descriptor.inputOutput, null);
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            // disconnected
                        }
                    });

                    try {
                        if (descriptor.postExecution != null) {
                            descriptor.postExecution.run();
                        }
                    } finally {
                        IOTerm.term(descriptor.inputOutput).setReadOnly(true);
                        descriptor.inputOutput.getOut().close();
                    }
                }
            }
        };

        FutureTask<Integer> runTask = new FutureTask<Integer>(callable) {

            @Override
            /**
             * @return <tt>true</tt>
             */
            public boolean cancel(boolean mayInterruptIfRunning) {
                synchronized (processRef) {
                    /* *** Bug 186172 ***
                     *
                     * Do NOT call super.cancel() here as it will interrupt
                     * waiting thread (see callable's that this task is created
                     * from) and will initiate a postExecutionTask BEFORE the
                     * process is terminated.
                     * Just need to terminate the process. The process.waitFor()
                     * will naturally return then.
                     *
                     */
//                    boolean ret = super.cancel(mayInterruptIfRunning);

                    NativeProcess process = processRef.get();

                    if (process != null) {
                        process.destroy();
                    }

                    return true;
                }
            }
        };

        NativeTaskExecutorService.submit(runTask, "start process in term"); // NOI18N

        return runTask;
    }

    private Future<Integer> runRegular() {
        Charset charset = descriptor.charset;

        if (charset == null) {
            charset = execCharset;
        }

        Logger.getInstance().log(Level.FINE, "Input stream charset: {0}", charset);

        ExecutionDescriptor descr =
                new ExecutionDescriptor().controllable(descriptor.controllable).
                frontWindow(descriptor.frontWindow).
                inputVisible(descriptor.inputVisible).
                inputOutput(descriptor.inputOutput).
                outLineBased(descriptor.outLineBased).
                showProgress(descriptor.showProgress).
                postExecution(descriptor.postExecution).
                noReset(!descriptor.resetInputOutputOnFinish).
                errConvertorFactory(descriptor.errConvertorFactory).
                outConvertorFactory(descriptor.outConvertorFactory).
                charset(charset);

        ExecutionService es = ExecutionService.newService(processBuilder, descr, displayName);
        return es.run();
    }

    private void closeIO() {
        descriptor.inputOutput.getErr().close();
        descriptor.inputOutput.getOut().close();
        try {
            descriptor.inputOutput.getIn().close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private final class PostRunnable implements Runnable {

        @Override
        public void run() {
            NativeProcess process = processRef.get();

            if (process == null) {
                return;
            }

            int rc = -1;

            try {
                rc = process.waitFor();
            } catch (InterruptedException ex) {
//                Exceptions.printStackTrace(ex);
            } finally {
                if (descriptor.postMessageDisplayer != null) {
                    final NativeProcess.State state = process.getState();
                    final long time = System.currentTimeMillis() - startTimeMillis;
                    String postMsg = descriptor.postMessageDisplayer.getPostMessage(state, rc, time);

                    PrintWriter pw = (rc == 0)
                            ? descriptor.inputOutput.getOut()
                            : descriptor.inputOutput.getErr();

                    // use \n\r to correctly move cursor in terminals as well
                    pw.printf("\n\r%s\n\r", postMsg); // NOI18N

                    StatusDisplayer.getDefault().setStatusText(descriptor.postMessageDisplayer.getPostStatusString(state, rc));
                }

                if (descriptor.closeInputOutputOnFinish) {
                    closeIO();
                }

                // Finally, if there was some post executable set before - call it
                if (postExecutable != null) {
                    postExecutable.run();
                }
            }
        }
    }

    private final class ProcessChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            if (!(e instanceof NativeProcessChangeEvent)) {
                return;
            }

            final NativeProcessChangeEvent event = (NativeProcessChangeEvent) e;
            processRef.compareAndSet(null, (NativeProcess) event.getSource());

            if (event.state == NativeProcess.State.RUNNING) {
                startTimeMillis = System.currentTimeMillis();
            }
        }
    }
}
