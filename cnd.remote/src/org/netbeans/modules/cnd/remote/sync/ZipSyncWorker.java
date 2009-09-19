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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;

/**
 *
 * @author Vladimir Kvashin
 */
/*package-local*/ class ZipSyncWorker extends BaseSyncWorker implements RemoteSyncWorker {

    private TimestampAndSharabilityFilter filter;

    private int totalCount;
    private int uploadCount;
    private long totalSize;
    private long uploadSize;

    public ZipSyncWorker(ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err, File privProjectStorageDir, File... localDirs) {
        super(executionEnvironment, out, err, privProjectStorageDir, localDirs);
    }

    private static File getTemp() {
        String tmpPath = System.getProperty("java.io.tmpdir");
        File tmpFile = new File(tmpPath);
        return tmpFile.exists() ? tmpFile : null;
    }

    protected TimestampAndSharabilityFilter createFilter() {
        return new TimestampAndSharabilityFilter(privProjectStorageDir, executionEnvironment);
    }

    @Override
    protected void synchronizeImpl(String remoteDir) throws InterruptedException, ExecutionException, IOException {

        totalCount = uploadCount = 0;
        totalSize = uploadSize = 0;
        long time = 0;
        
        if (RemoteUtil.LOGGER.isLoggable(Level.FINE)) {
            System.out.printf("Uploading %s to %s ...\n", topLocalDir.getAbsolutePath(), executionEnvironment); // NOI18N
            time = System.currentTimeMillis();
        }
        filter = createFilter();
        filter.setStatisticsCallback(new SharabilityFilter.StatisticsCallback() {
            public void onAccept(File file, boolean accepted) {
                totalCount++;
                totalSize += file.length();
                if (accepted) {
                    uploadCount++;
                    uploadSize += file.length();
                }
            }
        });
        // success flag is for tracing only. TODO: should we drop it?
        boolean success = false;
        File zipFile = null;
        upload: // the label allows us exiting this block on condition
        try  {
            // nb: here we only launch the task! we'll wait for the completion after local zip is created
            Future<Integer> mkDir = CommonTasksSupport.mkDir(executionEnvironment, remoteDir, err);

            String localDirName = topLocalDir.getName();
            if (localDirName.length() < 3) {
                localDirName = localDirName + ((localDirName.length() == 1) ? "_" : "__"); //NOI18N
            }
            zipFile = File.createTempFile(localDirName, ".zip", getTemp()); // NOI18N
            Zipper zipper = createZipper(zipFile);
            {
                if (RemoteUtil.LOGGER.isLoggable(Level.FINE)) {System.out.printf("\tZipping %s to %s...\n", topLocalDir.getAbsolutePath(), zipFile); } // NOI18N
                long zipStart = System.currentTimeMillis();
                int topDirLen = topLocalDir.getAbsolutePath().length();
                for (File dir : localDirs) {
                    String base;
                    if (dir.equals(topLocalDir)) {
                        base = null;
                    } else {
                        base = dir.getAbsolutePath().substring(topDirLen+1);
                    }
                    zipper.add(dir, filter, base);
                }
                zipper.close();
                float zipTime = ((float) (System.currentTimeMillis() - zipStart))/1000f;
                if (RemoteUtil.LOGGER.isLoggable(Level.FINE)) {System.out.printf("\t%d files zipped; file size is %d\n", zipper.getFileCount(), zipFile.length()); } // NOI18N
                if (RemoteUtil.LOGGER.isLoggable(Level.FINE)) {System.out.printf("\tZipping %s to %s took %f s\n", topLocalDir.getAbsolutePath(), zipFile, zipTime); } // NOI18N
            }

            if (zipper.getFileCount() == 0) {
                success = true; // just no changed files
                break upload;
            }

            // wait/check whether the remote dir was created sucessfully
            if (mkDir.get() != 0) {
                throw new IOException("Can not create directory " + remoteDir); //NOI18N
            }

            String remoteFile = remoteDir + '/' + zipFile.getName(); //NOI18N
            {
                long uploaStart = System.currentTimeMillis();
                if (RemoteUtil.LOGGER.isLoggable(Level.FINEST)) { System.out.printf("\tZSCP: uploading %s to %s:%s ...\n", zipFile, executionEnvironment, remoteFile); } // NOI18N
                Future<Integer> upload = CommonTasksSupport.uploadFile(zipFile.getAbsolutePath(), executionEnvironment, remoteFile, 0777, err);
                int rc = upload.get();
                float uploadTime = ((float) (System.currentTimeMillis() - uploaStart))/1000f;
                if (RemoteUtil.LOGGER.isLoggable(Level.FINEST)) { System.out.printf("\tZSCP: uploading %s to %s:%s finished in %f s with rc=%d\n", zipFile, executionEnvironment, remoteFile, uploadTime, rc); } // NOI18N
                if (rc != 0) {
                    throw new IOException("uploading " + zipFile + " to " + executionEnvironment + ':' + remoteFile + // NOI18N
                            " finished with error code " + rc); // NOI18N
                }
            }

            {
                if (RemoteUtil.LOGGER.isLoggable(Level.FINEST)) { System.out.printf("\tZSCP: unzipping %s:%s ...\n", executionEnvironment, remoteFile); } // NOI18N
                long unzipStart = System.currentTimeMillis();

                NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(executionEnvironment);
                pb.setCommandLine("unzip -o " + remoteFile + " > /dev/null"); // NOI18N
                //pb.setExecutable("unzip");
                //pb.setArguments("-o", remoteFile);
                pb.setWorkingDirectory(remoteDir);
                Process proc = pb.call();

                BufferedReader in;
                String line;

                in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                while ((line = in.readLine()) != null) {
                    //if (logger.isLoggable(Level.FINE)) { System.err.printf("\t%s\n", line); } //NOI18N
                }
                in = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                while ((line = in.readLine()) != null) {
                    if (RemoteUtil.LOGGER.isLoggable(Level.FINE)) { System.err.printf("\t%s\n", line); } //NOI18N
                }

                int rc = proc.waitFor();
                //String cmd = "sh -c \"unzip -o -q " + remoteFile + " > /dev/null";
                //RemoteCommandSupport rcs = new RemoteCommandSupport(executionEnvironment, cmd);
                //int rc = rcs.run();
                float unzipTime = ((float) (System.currentTimeMillis() - unzipStart))/1000f;
                if (RemoteUtil.LOGGER.isLoggable(Level.FINEST)) { System.out.printf("\tZSCP: unzipping %s:%s finished in %f s; rc=%d\n", executionEnvironment , remoteFile, unzipTime, rc); } // NOI18N
                if (rc != 0) {
                    throw new IOException("unzipping " + remoteFile + " at " + executionEnvironment + " finished with error code " + rc); // NOI18N
                }
            }
            success = true;
            CommonTasksSupport.rmFile(executionEnvironment, remoteFile, err);
            // NB: we aren't waining for completion,
            // since the name of the file made my File.createTempFile is new each time
            filter.flush();
        } finally {
            if (zipFile != null & zipFile.exists()) {
                if (!zipFile.delete()) {
                    RemoteUtil.LOGGER.info("Can not delete temporary file " + zipFile.getAbsolutePath()); //NOI18N
                }
            }
        }

        if (RemoteUtil.LOGGER.isLoggable(Level.FINE)) {
            time = System.currentTimeMillis() - time;
            long bps = uploadSize * 1000L / time;
            String speed = (bps < 1024*8) ? (bps + " b/s") : ((bps/1024) + " Kb/s"); // NOI18N

            String strTotalSize = (totalSize < 1024 ? (totalSize + " bytes") : ((totalSize/1024) + " K")); // NOI18N
            String strUploadSize = (uploadSize < 1024 ? (uploadSize + " bytes") : ((uploadSize/1024) + " K")); // NOI18N
            System.out.printf("Total: %s in %d files. Copied to %s:%s: %s in %d files. Time: %d ms. %s. Avg. speed: %s\n", // NOI18N
                    strTotalSize, totalCount, executionEnvironment, remoteDir,
                    strUploadSize, uploadCount, time, success ? "OK" : "FAILURE", speed); // NOI18N
        }
    }

    @Override
    public boolean cancel() {
        return false;
    }

    protected Zipper createZipper(File zipFile) throws FileNotFoundException {
        return new Zipper(zipFile);
    }
}
