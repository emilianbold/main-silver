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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.io.NullInputStream;

/**
 *
 * @author Vladimir Kvashin
 */
class RfsSyncWorker extends ZipSyncWorker {

    private static final boolean allAtOnce = false;

    public RfsSyncWorker(File localDir, ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err, File privProjectStorageDir) {
        super(localDir, executionEnvironment, out, err, privProjectStorageDir);
    }

    @Override
    protected Zipper createZipper(File zipFile) {
        return new Zipper(zipFile) {
            @Override
            protected InputStream getFileInputStream(File file) throws FileNotFoundException {
                if (allAtOnce) {
                    return super.getFileInputStream(file);
                } else {
                    return new NullInputStream();
                }
            }

        };
    }

    @Override
    protected void synchronizeImpl(String remoteDir) throws InterruptedException, ExecutionException, IOException {
        super.synchronizeImpl(remoteDir);
        NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(executionEnvironment);
        pb.setExecutable("/tmp/rfs_controller"); // NOI18N
        NativeProcess remoteControllerProcess = pb.call();

        RequestProcessor.getDefault().post(new ErrorReader(remoteControllerProcess.getErrorStream()));

        final InputStream rcStream = remoteControllerProcess.getInputStream();
        LocalController localController = new LocalController(remoteDir, rcStream,
                remoteControllerProcess.getOutputStream());
        // read port
        String line = new BufferedReader(new InputStreamReader(rcStream)).readLine();
        String port;
        if (line.startsWith("PORT ")) { // NOI18N
            port = line.substring(5);
        } else {
            String message = String.format("Protocol error: read \"%s\" expected \"%s\"\n", line,  "PORT <port-number>"); //NOI18N
            System.err.printf(message); // NOI18N
            remoteControllerProcess.destroy();
            throw new ExecutionException(message, null); //NOI18N
        }
        logger.fine("Remote Controller listens port " + port); // NOI18N
        RequestProcessor.getDefault().post(localController);
    }

    @Override
    protected TimestampAndSharabilityFilter createFilter() {
        return new Filter(privProjectStorageDir, executionEnvironment) {
        };
    }

    private class Filter extends TimestampAndSharabilityFilter {

        public Filter(File privProjectStorageDir, ExecutionEnvironment executionEnvironment) {
            super(privProjectStorageDir, executionEnvironment);
        }

        @Override
        public boolean acceptImpl(File file) {
            return true; // super.acceptImpl(file);
        }
    }

    private class ErrorReader implements Runnable {
        private final BufferedReader err;
        public ErrorReader(InputStream err) {
            this.err = new BufferedReader(new InputStreamReader(err));
        }
        public void run() {
            try {
                String line;
                while ((line = err.readLine()) != null) {
                    logger.fine(line);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private class LocalController implements Runnable {

        private final BufferedReader requestReader;
        private final PrintStream responseStream;
        private final String remoteDir;

        private final Set<String> processedFiles = new HashSet<String>();
        
        public LocalController(String remoteDir, InputStream requestStream, OutputStream responseStream) {
            this.remoteDir = remoteDir;
            this.requestReader = new BufferedReader(new InputStreamReader(requestStream));
            this.responseStream = new PrintStream(responseStream);
        }

        private void respond_ok() {
            responseStream.printf("1\n"); // NOI18N
            responseStream.flush();
        }
        private void respond_err(String tail) {
            responseStream.printf("0 %s\n", tail); // NOI18N
            responseStream.flush();
        }

        public void run() {
            while (true) {
                try {
                    String request = requestReader.readLine();
                    String remoteFile = request;
                    logger.finest("LC: REQ " + request);
                    if (request == null) {
                        break;
                    }
                    if (processedFiles.contains(remoteFile)) {
                        logger.info("RC asks for file " + remoteFile + " again?!");
                        respond_ok();
                        continue;
                    } else {
                        processedFiles.add(remoteFile);
                    }
                    if (remoteFile.startsWith(remoteDir)) {
                        File localFile =  new File(localDir, remoteFile.substring(remoteDir.length()));
                        if (localFile.exists() && !allAtOnce) {
                            logger.finest("LC: uploading " + localFile + " to " + remoteFile + " started");
                            Future<Integer> task = CommonTasksSupport.uploadFile(localFile.getAbsolutePath(),
                                    executionEnvironment, remoteFile, 0777, err);
                            try {
                                int rc = task.get();
                                logger.finest("LC: uploading " + localFile + " to " + remoteFile + " finished; rc=" + rc);
                                if (rc == 0) {
                                    respond_ok();
                                } else {
                                    respond_err("1");
                                }
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                                break;
                            } catch (ExecutionException ex) {
                                Exceptions.printStackTrace(ex);
                                respond_err("2 execution exception\n"); // NOI18N
                            } finally {
                                responseStream.flush();
                            }
                        } else {
                            respond_ok();
                        }
                    } else {
                        respond_ok();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex); //TODO: error processing
                }
            }
        }

    }


}
