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

package org.netbeans.modules.cnd.remote.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;

/**
 *
 * @author Vladimir Kvashin
 */
/* package-local */
class RemoteBuildProjectActionHandler implements ProjectActionHandler {

    private ProjectActionHandler delegate;
    private ProjectActionEvent pae;
    private ProjectActionEvent[] paes;
    private ExecutionEnvironment execEnv;

    private File localDir;
    private PrintWriter out;
    private PrintWriter err;
    private File privProjectStorageDir;
    private String remoteDir;

    private NativeProcess remoteControllerProcess = null;
    
    /* package-local */
    RemoteBuildProjectActionHandler() {
    }
    
    @Override
    public void init(ProjectActionEvent pae, ProjectActionEvent[] paes) {
        this.pae = pae;
        this.paes = paes;
        this.delegate = RemoteBuildProjectActionHandlerFactory.createDelegateHandler(pae);
        this.delegate.init(pae, paes);
        this.execEnv = pae.getConfiguration().getDevelopmentHost().getExecutionEnvironment();
    }

    private void initRfsIfNeed() throws IOException, InterruptedException, ExecutionException {
        if (RfsSyncFactory.ENABLE_RFS) {            
            if (execEnv.isRemote()) {
                if (ServerList.get(execEnv).getSyncFactory().getID().equals(RfsSyncFactory.ID)) {
                    // FIXUP: this should be done via ActionHandler.
                    RfsSyncWorker.Parameters params = RfsSyncWorker.getLastParameters();
                    assert params != null; // FIXUP: it's impossible because of the check above
                    this.localDir = params.localDir;
                    this.remoteDir = params.remoteDir;
                    this.out = params.out;
                    this.err = params.err;
                    this.privProjectStorageDir = params.privProjectStorageDir;
                    initRfs();
                }
            }
        }
    }

    private synchronized void remoteControllerCleanup() {
        // nobody calls this concurrently => no synchronization
        if (remoteControllerProcess != null) {
            remoteControllerProcess.destroy();
            remoteControllerProcess = null;
        }
    }

    private void initRfs() throws IOException, InterruptedException, ExecutionException {

        final Env env = pae.getProfile().getEnvironment();

        String remoteControllerPath;
        String ldLibraryPath;
        try {
            remoteControllerPath = RfsSetupProvider.getControllerPath(execEnv);
            CndUtils.assertTrue(remoteControllerPath != null);
            ldLibraryPath = RfsSetupProvider.getLdLibraryPath(execEnv);
            CndUtils.assertTrue(ldLibraryPath != null);
        } catch (ParseException ex) {
            throw new ExecutionException(ex);
        }

        NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(execEnv);
        // nobody calls this concurrently => no synchronization
        remoteControllerCleanup(); // just in case
        pb.setExecutable(remoteControllerPath); //I18N
        pb.setWorkingDirectory(remoteDir);
        remoteControllerProcess = pb.call();

        RequestProcessor.getDefault().post(new ErrorReader(remoteControllerProcess.getErrorStream(), err));

        final InputStream rcInputStream = remoteControllerProcess.getInputStream();
        final OutputStream rcOutputStream = remoteControllerProcess.getOutputStream();
        RfsLocalController localController = new RfsLocalController(
                execEnv, localDir,  remoteDir, rcInputStream,
                rcOutputStream, err, privProjectStorageDir);

        feedFiles(rcOutputStream);
        //try { rcOutputStream.flush(); Thread.sleep(10000); } catch (InterruptedException e) {}

        // read port
        String line = new BufferedReader(new InputStreamReader(rcInputStream)).readLine();
        String port;
        if (line != null && line.startsWith("PORT ")) { // NOI18N
            port = line.substring(5);
        } else if (line == null) {
            int rc = remoteControllerProcess.waitFor();
            throw new ExecutionException(String.format("Remote controller failed; rc=%d\n", rc), null); // NOI18N
        } else {
            String message = String.format("Protocol error: read \"%s\" expected \"%s\"\n", line,  "PORT <port-number>"); //NOI18N
            System.err.printf(message); // NOI18N
            remoteControllerProcess.destroy();
            throw new ExecutionException(message, null); //NOI18N
        }
        RemoteUtil.LOGGER.fine("Remote Controller listens port " + port); // NOI18N
        RequestProcessor.getDefault().post(localController);

        String preload = RfsSetupProvider.getPreloadName(execEnv);
        CndUtils.assertTrue(preload != null);
        // to be able to trace what we're doing, first put it all to a map
        Map<String, String> env2add = new HashMap<String, String>();

        env2add.put("LD_PRELOAD", preload); // NOI18N
        env2add.put("LD_LIBRARY_PATH", ldLibraryPath); // NOI18N
        env2add.put("RFS_CONTROLLER_DIR", remoteDir); // NOI18N
        env2add.put("RFS_CONTROLLER_PORT", port); // NOI18N

        addRemoteEnv(env2add, "cnd.rfs.preload.sleep", "RFS_PRELOAD_SLEEP");
        addRemoteEnv(env2add, "cnd.rfs.preload.log", "RFS_PRELOAD_LOG");
        addRemoteEnv(env2add, "cnd.rfs.controller.log", "RFS_CONTROLLER_LOG");
        addRemoteEnv(env2add, "cnd.rfs.controller.port", "RFS_CONTROLLER_PORT");
        addRemoteEnv(env2add, "cnd.rfs.controller.host", "RFS_CONTROLLER_HOST");

        RemoteUtil.LOGGER.fine("Setting environment:");
        for (Map.Entry<String, String> entry : env2add.entrySet()) {
            if (RemoteUtil.LOGGER.isLoggable(Level.FINE)) {
                RemoteUtil.LOGGER.fine(String.format("\t%s=%s", entry.getKey(), entry.getValue()));
            }
            env.putenv(entry.getKey(), entry.getValue());
        }
        
        delegate.addExecutionListener(new ExecutionListener() {
            public void executionStarted(int pid) {
                RemoteUtil.LOGGER.fine("RemoteBuildProjectActionHandler: build started; PID=" + pid);
            }
            public void executionFinished(int rc) {
                RemoteUtil.LOGGER.fine("RemoteBuildProjectActionHandler: build finished; RC=" + rc);
                shutdownRfs();
            }
        });
    }

    private void addRemoteEnv(Map<String, String> env2add, String localJavaPropertyName, String remoteEnvVarName) {
        String value = System.getProperty(localJavaPropertyName, null);
        if (value != null) {
            env2add.put(remoteEnvVarName, value);
        }
    }

    private void shutdownRfs() {
        remoteControllerCleanup();
    }

    @Override
    public void addExecutionListener(ExecutionListener l) {
        delegate.addExecutionListener(l);
    }

    @Override
    public void removeExecutionListener(ExecutionListener l) {
        delegate.removeExecutionListener(l);
    }

    @Override
    public boolean canCancel() {
        return delegate.canCancel();
    }

    @Override
    public void cancel() {
        delegate.cancel();
    }

    @Override
    public void execute(InputOutput io) {
        try {
            initRfsIfNeed();
            delegate.execute(io);
        } catch (InterruptedException ex) {
            // reporting does not make sense, just return false
            RemoteUtil.LOGGER.finest(ex.getMessage());
        } catch (InterruptedIOException ex) {
            // reporting does not make sense, just return false
            RemoteUtil.LOGGER.finest(ex.getMessage());
        } catch (IOException ex) {
            RemoteUtil.LOGGER.log(Level.FINE, null, ex);
            if (err != null) {
                err.printf("%s\n", NbBundle.getMessage(getClass(), "MSG_Error_Copying",
                        remoteDir, ServerList.get(execEnv).toString(), ex.getLocalizedMessage()));
            }
        } catch (ExecutionException ex) {
            RemoteUtil.LOGGER.log(Level.FINE, null, ex);
            if (err != null) {
                String message = NbBundle.getMessage(getClass(), "MSG_Error_Copying",
                        remoteDir, ServerList.get(execEnv).toString(), ex.getLocalizedMessage());
                io.getErr().printf("%s\n", message); // NOI18N
                io.getErr().printf("%s\n", NbBundle.getMessage(getClass(), "MSG_Build_Failed"));
                err.printf("%s\n", message); // NOI18N
            }
        }
    }

    /**
     * Feeds remote controller with the list of files and their lengths
     * @param rcOutputStream
     */
    private void feedFiles(OutputStream rcOutputStream) {
        PrintWriter writer = new PrintWriter(rcOutputStream);
        NonFlashingFilter filter = new NonFlashingFilter(privProjectStorageDir, execEnv);
        File[] children = localDir.listFiles(filter);
        for (File child : children) {
            feedFilesImpl(writer, child, null, filter);
        }
        writer.printf("\n"); // NOI18N
        writer.flush();
    }

    private static void feedFilesImpl(PrintWriter writer, File file, String base, FileFilter filter) {
        // it is assumed that the file itself was already filtered
        String fileName = isEmpty(base) ? file.getName() : base + '/' + file.getName();
        if (file.isDirectory()) {
            String text = String.format("D %s", fileName); // NOI18N
            writer.println(text); // adds LF
            writer.flush(); //TODO: remove?
            File[] children = file.listFiles(filter);
            for (File child : children) {
                String newBase = isEmpty(base) ? file.getName() : (base + "/" + file.getName()); // NOI18N
                feedFilesImpl(writer, child, newBase, filter);
            }
        } else {
            String text = String.format("%d %s", file.length(), fileName); // NOI18N
            writer.println(text); // adds LF
            writer.flush(); //TODO: remove?
        }
    }

    private static class NonFlashingFilter extends TimestampAndSharabilityFilter {

        public NonFlashingFilter(File privProjectStorageDir, ExecutionEnvironment executionEnvironment) {
            super(privProjectStorageDir, executionEnvironment);
        }

        @Override
        public boolean acceptImpl(File file) {
            return super.acceptImpl(file);
        }

        @Override
        public void flush() {
            // do nothing, since fake (empty) fies were sent!
        }
    }


    private static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    private static class ErrorReader implements Runnable {

        private final BufferedReader errorReader;
        private final PrintWriter errorWriter;

        public ErrorReader(InputStream errorStream, PrintWriter errorWriter) {
            this.errorReader = new BufferedReader(new InputStreamReader(errorStream));
            this.errorWriter = errorWriter;
        }
        public void run() {
            try {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    if (errorWriter != null) {
                         errorWriter.println(line);
                    }
                    RemoteUtil.LOGGER.fine(line);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

}
