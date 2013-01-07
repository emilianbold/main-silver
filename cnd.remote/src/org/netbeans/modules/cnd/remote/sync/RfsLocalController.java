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
 * Portions Copyrighted 2009, 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.sync;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.remote.support.RemoteUtil.PrefixedLogger;
import org.netbeans.modules.cnd.remote.sync.download.HostUpdates;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport.UploadStatus;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.api.util.ShellScriptRunner;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

class RfsLocalController extends NamedRunnable {

    public static final int SKEW_THRESHOLD = Integer.getInteger("cnd.remote.skew.threshold", 1); // NOI18N

    private final RfsSyncWorker.RemoteProcessController remoteController;
    private final BufferedReader requestReader;
    private final PrintWriter responseStream;
    private final File[] files;
    private final ExecutionEnvironment execEnv;
    private final PrintWriter err;
    private final FileData fileData;
    private final RemotePathMap mapper;
    private final Set<File> remoteUpdates;
    private final FileObject privProjectStorageDir;
    private final PrefixedLogger logger;
    private final String prefix;
    private final SharabilityFilter filter;
    private String timeStampFile;

    private static final boolean USE_TIMESTAMPS = DebugUtils.getBoolean("cnd.rfs.timestamps", true);
    private static  final char VERSION = USE_TIMESTAMPS ? '5' : '3';

    private static final boolean CHECK_ALIVE = DebugUtils.getBoolean("cnd.rfs.check.alive", true);

    private static final RequestProcessor RP = new RequestProcessor("RfsLocalController", 1); // NOI18N
    /**
     * Maps remote canonical remote path remote controller operates with
     * to the absolute remote path local controller uses
     */
    private final Map<String, String> canonicalToAbsolute = new HashMap<String, String>();

    private static enum RequestKind {
        REQUEST,
        WRITTEN,
        PING,
        UNKNOWN,
        KILLED
    }

    public RfsLocalController(ExecutionEnvironment executionEnvironment, File[] files,
            RfsSyncWorker.RemoteProcessController remoteController, BufferedReader requestStreamReader, PrintWriter responseStreamWriter, PrintWriter err,
            FileObject privProjectStorageDir) throws IOException {
        super("RFS local controller thread " + executionEnvironment); //NOI18N
        this.execEnv = executionEnvironment;
        this.files = files;
        this.remoteController = remoteController;
        this.requestReader = requestStreamReader;
        this.responseStream = responseStreamWriter;
        this.err = err;
        this.mapper = RemotePathMap.getPathMap(execEnv);
        this.remoteUpdates = new HashSet<File>();
        this.privProjectStorageDir = privProjectStorageDir;
        this.fileData = FileData.get(privProjectStorageDir, executionEnvironment);
        this.prefix = "LC[" + executionEnvironment + "]"; //NOI18N
        this.logger = new RemoteUtil.PrefixedLogger(prefix);
        this.filter = new SharabilityFilter();
    }

    private void respond_ok() {
        responseStream.printf("1\n"); // NOI18N
        responseStream.flush();
    }

    private void respond_err(String tail) {
        responseStream.printf("0 %s\n", tail); // NOI18N
        responseStream.flush();
    }

//    private String toRemoteFilePathName(String localAbsFilePath) {
//        String out = localAbsFilePath;
//        if (Utilities.isWindows()) {
//            out = WindowsSupport.getInstance().convertToMSysPath(localAbsFilePath);
//        }
//        if (out.charAt(0) == '/') {
//            out = out.substring(1);
//        } else {
//            RemoteUtil.LOGGER.warning("Path must start with /: " + out + "\n");
//        }
//        return out;
//    }

    private RequestKind getRequestKind(String request) {
        switch (request.charAt(0)) {
            case 'r':   return RequestKind.REQUEST;
            case 'w':   return RequestKind.WRITTEN;
            case 'p':   return RequestKind.PING;
            default:
                if ("Killed".equals(request)){//NOI18N
                    //BZ #193114 - IllegalArgumentException: Protocol error: Killed
                    //let's check the process state
                    if (remoteController.isStopped()){
                        return RequestKind.KILLED;
                    }
                }
                return RequestKind.UNKNOWN;
        }
    }

    @Override
    protected void runImpl() {
        long totalCopyingTime = 0;
        while (true) {
            try {
                String request = requestReader.readLine();
                logger.log(Level.FINEST, "REQ %s", request);
                if (request == null) {
                    break;
                }
                RequestKind kind = getRequestKind(request);
                if (kind == RequestKind.KILLED){
                    //there is something wrong with the process
                    //print to error that remote process is killed
                    err.append("\nRemote process is killed");//NOI18N
                    break;
                }else if (kind == RequestKind.UNKNOWN){
                    err.append("\nProtocol error: " + request);//NOI18N
                }else   if (kind == RequestKind.PING) {
                    logger.log(Level.FINEST, "PING from remote controller");
                    // no response needed
                    // respond_ok();
                } else {
                    if (request.charAt(1) != ' ') {
                        throw new IllegalArgumentException("Protocol error: " + request); // NOI18N
                    }
                    String remoteFile = request.substring(2);
                    String realPath = canonicalToAbsolute.get(remoteFile);
                    if (realPath != null) {
                        remoteFile = realPath;
                    }
                    String localFilePath = mapper.getLocalPath(remoteFile);
                    if (localFilePath != null) {
                        File localFile = CndFileUtils.createLocalFile(localFilePath);
                        if (kind == RequestKind.WRITTEN) {
                            fileData.setState(localFile, FileState.UNCONTROLLED);
                            remoteUpdates.add(localFile);
                            RfsListenerSupportImpl.getInstanmce(execEnv).fireFileChanged(localFile, remoteFile);
                            logger.log(Level.FINEST, "uncontrolled %s", localFile);
                        } else {
                            CndUtils.assertTrue(kind == RequestKind.REQUEST, "kind should be RequestKind.REQUEST, but is ", kind);
                            if (localFile.exists() && !localFile.isDirectory()) {
                                //FileState state = fileData.getState(localFile);
                                logger.log(Level.FINEST, "uploading %s to %s started", localFile, remoteFile);
                                long fileTime = System.currentTimeMillis();
                                Future<UploadStatus> task = CommonTasksSupport.uploadFile(localFile.getAbsolutePath(), execEnv, remoteFile, 0777);
                                try {
                                    UploadStatus uploadStatus = task.get();
                                    fileTime = System.currentTimeMillis() - fileTime;
                                    totalCopyingTime += fileTime;
                                    logger.log(Level.FINEST, "uploading %s to %s finished; rc=%d time = %d total time = %d ms",
                                            localFile, remoteFile, uploadStatus.getExitCode(), fileTime, totalCopyingTime);
                                    if (uploadStatus.isOK()) {
                                        fileData.setState(localFile, FileState.COPIED);
                                        respond_ok();
                                    } else {
                                        if (err != null) {
                                            err.println(uploadStatus.getError());
                                        }
                                        respond_err("1"); // NOI18N
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
                        }
                    } else {
                        respond_ok();
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        //fileData.store();
        shutdown();
    }

    private void shutdown() {
        // this try-catch is only for investigation of the instable test failures
        try {
            logger.log(Level.FINEST, "shutdown");
            try {
                runNewFilesDiscovery(true);
                shutDownNewFilesDiscovery();
            } catch (CancellationException ex) {
                // nothing
            } catch (InterruptedIOException ex) {
                // nothing
            } catch (InterruptedException ex) {
                // nothing
            } catch (IOException ex) {
                logger.log(Level.INFO, "Error discovering newer files at remote host", ex); //NOI18N
            } catch (ExecutionException ex) {
                logger.log(Level.INFO, "Error discovering newer files at remote host", ex); //NOI18N
            }
            fileData.store();
            logger.log(Level.FINE, "registering %d updated files", remoteUpdates.size());
            if (!remoteUpdates.isEmpty()) {
                HostUpdates.register(remoteUpdates, execEnv, privProjectStorageDir);
                logger.log(Level.FINE, "registered  %d updated files", remoteUpdates.size());
            }
        } catch (Throwable thr) {
            thr.printStackTrace();
        }
    }

    private boolean initNewFilesDiscovery() {
        String remoteSyncRoot = RemotePathMap.getRemoteSyncRoot(execEnv);
        ExitStatus res = ProcessUtils.execute(execEnv, "mktemp", "-p", remoteSyncRoot); // NOI18N
        if (res.isOK()) {
           timeStampFile = res.output.trim();
           return true;
        } else {
            timeStampFile = null;
            String errMsg = NbBundle.getMessage(getClass(), "MSG_Error_Running_Command", "mktemp -p " + remoteSyncRoot, execEnv, res.error, res.exitCode);
            logger.log(Level.INFO, errMsg);
            if (err != null) {
                err.printf("%s\n", errMsg); // NOI18N
            }
            return false;
        }
    }

    private void shutDownNewFilesDiscovery() throws InterruptedException, ExecutionException {
        if (timeStampFile != null) {
            CommonTasksSupport.rmFile(execEnv, timeStampFile, err).get();
        }
    }

    private void runNewFilesDiscovery(boolean srcOnly) throws IOException, InterruptedException, CancellationException {
        if (timeStampFile == null) {
            return;
        }
        long time = System.currentTimeMillis();
        int oldSize = remoteUpdates.size();

        StringBuilder remoteDirs = new StringBuilder();
        for (File file : files) {
            if (file.isDirectory()) {
                String rPath = mapper.getRemotePath(file.getAbsolutePath(), false);
                if (rPath == null) {
                    logger.log(Level.INFO, "Can't get remote path for %s at %s", file.getAbsolutePath(), execEnv);
                } else {
                    if (remoteDirs.length() > 0) {
                        remoteDirs.append(' ');
                    }
                    remoteDirs.append('"');
                    remoteDirs.append(rPath);
                    remoteDirs.append('"');
                }
            }
        }

        StringBuilder extOptions = new StringBuilder();
        if (srcOnly) {
            Collection<Collection<String>> values = new ArrayList<Collection<String>>();
            values.add(MIMEExtensions.get(MIMENames.C_MIME_TYPE).getValues());
            values.add(MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE).getValues());
            values.add(MIMEExtensions.get(MIMENames.HEADER_MIME_TYPE).getValues());
            for (Collection<String> v : values) {
                for (String ext : v) {
                    if (extOptions.length() > 0) {
                        extOptions.append(" -o "); // NOI18N
                    }
                    extOptions.append("-name \"*."); // NOI18N
                    extOptions.append(ext);
                    extOptions.append("\""); // NOI18N
                }
            }
            if (extOptions.length() > 0) {
                extOptions.append(" -o "); // NOI18N
            }
            extOptions.append(" -name Makefile"); // NOI18N
        }

        String script = String.format(
            "for F in `find %s %s -newer %s`; do test -f $F &&  echo $F;  done;", // NOI18N
            remoteDirs, extOptions.toString(), timeStampFile);

        final AtomicInteger lineCnt = new AtomicInteger();

        LineProcessor lp = new LineProcessor() {
            @Override
            public void processLine(String remoteFile) {
                lineCnt.incrementAndGet();
                logger.log(Level.FINEST, " Updates check: %s", remoteFile);
                String realPath = canonicalToAbsolute.get(remoteFile);
                if (realPath != null) {
                    remoteFile = realPath;
                }
                String localPath = mapper.getLocalPath(remoteFile);
                if (localPath == null) {
                    logger.log(Level.FINE, "Can't find local path for %s", remoteFile);
                } else {
                    File localFile = CndFileUtils.createLocalFile(localPath);
                    if (fileData.getFileInfo(localFile) == null) { // this is only for files we don't control
                        if (filter.accept(localFile)) {
                            remoteUpdates.add(localFile);
                            RfsListenerSupportImpl.getInstanmce(execEnv).fireFileChanged(localFile, remoteFile);
                        }
                    }
                }
            }

            @Override
            public void reset() {}

            @Override
            public void close() {}
        };

        ShellScriptRunner ssr = new ShellScriptRunner(execEnv, script, lp);
        ssr.setErrorProcessor(new ShellScriptRunner.LoggerLineProcessor(prefix));
        int rc = ssr.execute();
        if (rc != 0 ) {
            logger.log(Level.FINE, "Error %d running script \"%s\" at %s", rc, script, execEnv);
        }
        logger.log(Level.FINE, "New files discovery at %s took %d ms; %d lines processed; %d additional new files were discovered",
                execEnv, System.currentTimeMillis() - time, lineCnt.get(), remoteUpdates.size() - oldSize);
    }

    private static class FileGatheringInfo {

        public final File file;
        public final String remotePath;
        private String linkTarget;
        private FileGatheringInfo linkTargetInfo;

        public FileGatheringInfo(File file, String remotePath) {
            this.file = file;
            this.remotePath = remotePath;
            CndUtils.assertTrue(remotePath.startsWith("/"), "Non-absolute remote path: ", remotePath);
            this.linkTarget = null;
        }

        @Override
        public String toString() {
            return (isLink() ? "L " : file.isDirectory() ? "D " : "F ") + file.getPath() + " -> " + remotePath; // NOI18N
        }

        public boolean isLink() {
            return linkTarget != null;
        }

        public String getLinkTarget() {
            return linkTarget;
        }

        public void setLinkTarget(String link) {
            this.linkTarget = link;
        }

        public FileGatheringInfo getLinkTargetInfo() {
            return linkTargetInfo;
        }

        public void setLinkTargetInfo(FileGatheringInfo linkTargetInfo) {
            this.linkTargetInfo = linkTargetInfo;
        }
    }

    private boolean checkVersion() throws IOException {
        String versionsString = requestReader.readLine();
        if (versionsString == null) {
            return false;
        }
        String versionsPattern = "VERSIONS "; // NOI18N
        if (!versionsString.startsWith(versionsPattern)) {
            if (err != null) {
                err.printf("Protocol error, expected %s, got %s\n", versionsPattern, versionsString); //NOI18N
            }
            return false;
        }
        String[] versionsArray = versionsString.substring(versionsPattern.length()).split(" "); // NOI18N
        for (String v : versionsArray) {
            if (v.length() != 1) {
                if (err != null) {
                    err.printf("Protocol error: incorrect version format: %s\n", versionsString); //NOI18N
                }
                return false;
            }
            if (v.charAt(0) == VERSION) {
                return true;
            }
        }
        return true;
    }

    /*package*/ static char testGetVersion() {
        return VERSION;
    }

    private static class FormatException extends Exception {
        public FormatException(String message) {
            super(message);
        }        
        public FormatException(String message, Throwable cause) {
            super(message, cause);
        }        
    }

    private long getTimeSkew() throws IOException, CancellationException, FormatException {        
        final int cnt = 10;
        responseStream.printf("SKEW_COUNT=%d\n", cnt); //NOI18N
        responseStream.flush();
        long[] deltas = new long[cnt];
        for (int i = 0; i < cnt; i++) {
            long localTime1 = System.currentTimeMillis();
            responseStream.printf("SKEW %d\n", i); //NOI18N
            responseStream.flush();
            String line = requestReader.readLine();
            long localTime2 = System.currentTimeMillis();
            try {
                long remoteTime = Long.parseLong(line);
                deltas[i] = remoteTime - (localTime1 + localTime2)/ 2;
            } catch (NumberFormatException nfe) {
                throw new FormatException("Wrong skew format: " + line, nfe); //NOI18N
            }
        }
        responseStream.printf("SKEW_END\n"); //NOI18N
        responseStream.flush();
        
        long skew = 0;
        for (int i = 0; i < cnt; i++) {
            skew += deltas[i];
        }
        skew /= cnt;
        
        String line = requestReader.readLine();
        if (!line.startsWith("FS_SKEW ")) { //NOI18N
            throw new FormatException("Wrong file system skew response: " + line); //NOI18N
        }   
        try {
            long fsSkew = Long.parseLong(line.substring(8));
            fsSkew /=  1000;
            if (Math.abs(fsSkew) > SKEW_THRESHOLD) {
                FsSkewNotifier.getInstance().notify(execEnv, fsSkew);
            }
            return skew + fsSkew;
        } catch (NumberFormatException nfe) {
            throw new FormatException("Wrong file system skew format: " + line, nfe); //NOI18N
        }
    }

    /**
     * Feeds remote controller with the list of files and their lengths
     * @return true in the case of success, otherwise false
     * NB: in the case false is returned, no shutdown will be called
     */
    boolean init() throws IOException, CancellationException {        
        if (!checkVersion()) {
            return false;
        }
        logger.log(Level.FINE, "Initialization. Version=%c", VERSION);
        if (CHECK_ALIVE && !remoteController.isAlive()) { // fixup for remote tests unstable failure (caused by jsch issue)
            if (err != null) {
                err.printf("Process exited unexpectedly when initializing\n"); //NOI18N
            }
            return false;
        }
        responseStream.printf("VERSION=%c\n", VERSION); //NOI18N
        responseStream.flush();

        long clockSkew;
        try {
            clockSkew = getTimeSkew();
            if (logger .isLoggable(Level.FINE)) {
                logger .log(Level.FINE, "HostInfo skew={0} calculated skew={1}", //NOI18N
                        new Object[]{HostInfoUtils.getHostInfo(execEnv).getClockSkew(), clockSkew}); 
            }
        } catch (FormatException ex) {
            if (err != null) {
                err.printf("protocol errpr: %s\n", ex.getMessage()); // NOI18N
            }
            return false;
        }

        long time = System.currentTimeMillis();
        long timeTotal = System.currentTimeMillis();
        List<FileGatheringInfo> filesToFeed = new ArrayList<FileGatheringInfo>(512);

        // the set of top-level dirs
        Set<File> topDirs = new HashSet<File>();

        for (File file : files) {
            file = CndFileUtils.normalizeFile(file);
            if (file.isDirectory()) {
                String toRemoteFilePathName = mapper.getRemotePath(file.getAbsolutePath());
                addFileGatheringInfo(filesToFeed, file, toRemoteFilePathName);
                File[] children = file.listFiles(filter);
                if (children != null) {
                    for (File child : children) {
                        gatherFiles(child, toRemoteFilePathName, filter, filesToFeed);
                    }
                }
                topDirs.add(file);
            } else {
                final File parentFile = file.getAbsoluteFile().getParentFile();
                String toRemoteFilePathName = mapper.getRemotePath(parentFile.getAbsolutePath());
                if (!topDirs.contains(parentFile)) {
                    // add parent folder for external file
                    topDirs.add(parentFile);
                    addFileGatheringInfo(filesToFeed, parentFile, toRemoteFilePathName);
                }
                gatherFiles(file, toRemoteFilePathName, filter, filesToFeed);
            }
        }

        Collection<File> parents = gatherParents(topDirs);
        for (File file : parents) {
            file = CndFileUtils.normalizeFile(file);
            String toRemoteFilePathName = mapper.getRemotePath(file.getAbsolutePath());
            addFileGatheringInfo(filesToFeed, file, toRemoteFilePathName);
        }
        logger.log(Level.FINE, "gathered %d files in %d ms", filesToFeed.size(), System.currentTimeMillis() - time);

        time = System.currentTimeMillis();
        checkLinks(filesToFeed);
        logger.log(Level.FINE, "checking links took %d ms", System.currentTimeMillis() - time);

        time = System.currentTimeMillis();
        Collections.sort(filesToFeed, new Comparator<FileGatheringInfo>() {
            @Override
            public int compare(FileGatheringInfo f1, FileGatheringInfo f2) {
                if (f1.file.isDirectory() || f2.file.isDirectory()) {
                    if (f1.file.isDirectory() && f2.file.isDirectory()) {
                        return f1.remotePath.compareTo(f2.remotePath);
                    } else {
                        return f1.file.isDirectory() ? -1 : +1;
                    }
                } else {
                    long delta = f1.file.lastModified() - f2.file.lastModified();
                    return (delta == 0) ? 0 : ((delta < 0) ? -1 : +1); // signum(delta)
                }
            }
        });
        logger.log(Level.FINE, "sorting file list took %d ms", System.currentTimeMillis() - time);

        time = System.currentTimeMillis();
        for (FileGatheringInfo info : filesToFeed) {
            try {
                sendFileInitRequest(info, clockSkew);
            } catch (IOException ex) {
                if (err != null) {
                    err.printf("Process exited unexpectedly while file info was being sent\n"); //NOI18N
                }
                return false;
            }
        }
        if (CHECK_ALIVE && !remoteController.isAlive()) { // fixup for remote tests unstable failure (caused by jsch issue)
            if (err != null) {
                err.printf("Process exited unexpectedly\n"); //NOI18N
            }
            return false;
        }
        responseStream.printf("\n"); // NOI18N
        responseStream.flush();
        logger.log(Level.FINE, "sending file list took %d ms", System.currentTimeMillis() - time);

        try {
            time = System.currentTimeMillis();
            readFileInitResponse();
            logger.log(Level.FINE, "reading initial response took %d ms", System.currentTimeMillis() - time);
        } catch (IOException ex) {
            if (err != null) {
                err.printf("%s\n", ex.getMessage());
                return false;
            }
        }
        fileData.store();
        if (!initNewFilesDiscovery()) {
            return false;
        }
        logger.log(Level.FINE, "the entire initialization took %d ms", System.currentTimeMillis() - timeTotal);
        return true;
    }

    private Collection<File> gatherParents(Collection<File> files) {
        Set<File> parents = new HashSet<File>();
        for (File file : files) {
            gatherParents(file, parents);
        }
        return parents;
    }

    private void gatherParents(File file, Set<File> parents) {
        //file = CndFileUtils.normalizeFile(file);
        File parent = file.getAbsoluteFile().getParentFile();
        if (parent != null && parent.getParentFile() != null) { // don't add top-level parents
            parents.add(parent);
            gatherParents(parent, parents);
        }
    }

    private void checkLinks(final List<FileGatheringInfo> filesToFeed) {
        if (Utilities.isWindows()) {
            return; // this is for Unixes only
        }
        // the counter is just in case here;
        // the real cycling check is inside checkLinks(List,List) logic
        int cnt = 0;
        final int max = 16;
        Collection<FileGatheringInfo> filesToCheck = new ArrayList<FileGatheringInfo>(filesToFeed);
        do {
            filesToCheck = checkLinks(filesToCheck, filesToFeed);
        } while (!filesToCheck.isEmpty() && cnt++ < max);
        logger.log(Level.FINE, "checkLinks done in %d passes", cnt);
        if (!filesToCheck.isEmpty()) {
            logger.log(Level.INFO, "checkLinks exited by count. Cyclic symlinks?");
        }
    }

    private Collection<FileGatheringInfo> checkLinks(final Collection<FileGatheringInfo> filesToCheck, final List<FileGatheringInfo> filesToAdd) {
        Set<FileGatheringInfo> addedInfos = new HashSet<FileGatheringInfo>();
        NativeProcessBuilder pb = NativeProcessBuilder.newLocalProcessBuilder();
        pb.setExecutable("sh"); //NOI18N
        pb.setArguments("-c", "xargs ls -ld | grep '^l'"); //NOI18N
        final NativeProcess process;
        try {
            process = pb.call();
        } catch (IOException ex) {
            logger.log(Level.INFO, "Error when checking links: %s", ex.getMessage());
            return addedInfos;
        }

        RP.post(new Runnable() {
            @Override
            public void run() {
                BufferedWriter requestWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                try {
                    for (FileGatheringInfo info : filesToCheck) {
                        String path = "\"" + info.file.getAbsolutePath() + "\""; // NOI18N
                        requestWriter.append(path);
                        requestWriter.newLine();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        requestWriter.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        RP.post(new Runnable() {
            private final BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            @Override
            public void run() {
                try {
                    for (String errLine = errorReader.readLine(); errLine != null; errLine = errorReader.readLine()) {
                        logger.log(Level.INFO, errLine);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        errorReader.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        Map<String, FileGatheringInfo> map = new HashMap<String, FileGatheringInfo>(filesToCheck.size());
        for (FileGatheringInfo info : filesToCheck) {
            map.put(info.file.getAbsolutePath(), info);
        }

        BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        try {
            boolean errorReported = false;
            for (String line = outputReader.readLine(); line != null; line = outputReader.readLine()) {
                // line format is:
                // lrwxrwxrwx   1 root     root           5 Mar 24 13:33 /export/link-home -> home/
                String[] parts = line.split(" +"); // NOI18N
                if (parts.length <= 4) {
                    if (!errorReported) {
                        errorReported = true;
                        logger.log(Level.WARNING, "Unexpected ls output: %s", line);
                    }
                }
                String localLinkTarget = parts[parts.length - 1];
                if (localLinkTarget.endsWith("/")) { // NOI18N
                    localLinkTarget = localLinkTarget.substring(0, localLinkTarget.length() - 1);
                }
                String linkPath = parts[parts.length - 3];
                FileGatheringInfo info = map.get(linkPath);
                CndUtils.assertNotNull(info, "Null FileGatheringInfo for " + linkPath); //NOI18N
                if (info != null) {
                    logger.log(Level.FINEST, "\tcheckLinks: %s -> %s", linkPath, localLinkTarget);
                    //info.setLinkTarget(localLinkTarget);
                    File linkParentFile = CndFileUtils.createLocalFile(linkPath).getParentFile();
                    //File localLinkTargetFile = CndFileUtils.createLocalFile(linkParentFile, localLinkTarget);
                    File localLinkTargetFile;
                    if (CndPathUtilitities.isPathAbsolute(localLinkTarget)) {
                        String remoteLinkTarget = mapper.getRemotePath(localLinkTarget, false);
                        info.setLinkTarget(remoteLinkTarget);
                        localLinkTargetFile = CndFileUtils.createLocalFile(localLinkTarget);
                    } else {
                        info.setLinkTarget(localLinkTarget); // it's relative, so it's the same for remote
                        localLinkTargetFile = CndFileUtils.createLocalFile(linkParentFile, localLinkTarget);
                    }
                    localLinkTargetFile = CndFileUtils.normalizeFile(localLinkTargetFile);
                    FileGatheringInfo targetInfo;
                    targetInfo = map.get(localLinkTargetFile.getAbsolutePath());
                    // TODO: try finding in newly added infos. Probably replace List to Map in filesToAdd
                    if (targetInfo == null) {
                        String remotePath = mapper.getRemotePath(localLinkTargetFile.getAbsolutePath(), false);
                        targetInfo = addFileGatheringInfo(filesToAdd, localLinkTargetFile, remotePath);
                        addedInfos.add(targetInfo);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            process.waitFor();
        } catch (InterruptedException ex) {
            // don't report InterruptedException
        }
        return addedInfos;
    }

    private void readFileInitResponse() throws IOException {
        String request;
        while ((request = requestReader.readLine()) != null) {
            if (request.length() == 0) {
                break;
            }
            if (request.length() < 3) {
                throw new IllegalArgumentException("Protocol error: " + request); // NOI18N
            }
            // temporaraily we support both old and new protocols here
            if (request.startsWith("*")) { // "*" denotes new protocol
                char charState = request.charAt(1);
                FileState state = FileState.fromId(charState);
                if (state == null) {
                    throw new IllegalArgumentException("Protocol error: unexpected state: '" + charState + "'"); // NOI18N
                }
                String remotePath = request.substring(2);
                String remoteCanonicalPath = requestReader.readLine();
                if (remoteCanonicalPath == null) {
                    throw new IllegalArgumentException("Protocol error: no canoical path for " + remotePath); //NOI18N
                }
                String localFilePath = mapper.getLocalPath(remotePath);
                if (localFilePath != null) {
                    //RemoteUtil.LOGGER.log(Level.FINEST, "canonicalToAbsolute: {0} -> {0}", new Object[] {remoteCanonicalPath, remotePath});
                    canonicalToAbsolute.put(remoteCanonicalPath, remotePath);
                    File localFile = CndFileUtils.createLocalFile(localFilePath);
                    fileData.setState(localFile, state);
                } else {
                    logger.log(Level.FINEST, "ERROR no local file for %s", remotePath);
                }
            } else {
                // OLD protocol (temporarily)
                //update info about file where we thought file is copied, but it doesn't
                // exist remotely (i.e. project directory was removed)
                if (request.length() < 3 || !request.startsWith("t ")) { // NOI18N
                    throw new IllegalArgumentException("Protocol error: " + request); // NOI18N
                }
                String remoteFile = request.substring(2);
                String localFilePath = mapper.getLocalPath(remoteFile);
                if (localFilePath != null) {
                    File localFile = CndFileUtils.createLocalFile(localFilePath);
                    fileData.setState(localFile, FileState.TOUCHED);
                } else {
                    logger.log(Level.FINEST, "ERROR no local file for %s", remoteFile);
                }
            }
        }
    }

    private void sendFileInitRequest(FileGatheringInfo fgi, long timeSkew) throws IOException {
        if (CHECK_ALIVE && !remoteController.isAlive()) { // fixup for remote tests unstable failure (caused by jsch issue)
            throw new IOException("process already exited"); //NOI18N
        }
        if(fgi.isLink()) {
            responseStream.printf("L %s\n%s\n", fgi.remotePath, fgi.getLinkTarget()); //NOI18N
        } else if (fgi.file.isDirectory()) {
            responseStream.printf("D %s\n", fgi.remotePath); //NOI18N
            responseStream.flush(); //TODO: remove?
        } else {
            File file = fgi.file;
            String remotePath = fgi.remotePath;
            FileData.FileInfo info = fileData.getFileInfo(file);
            FileState newState;
            if (file.exists()) {
                switch(info  == null ? FileState.INITIAL : info.state) {
                    case COPIED:
                    case TOUCHED:
                        if (info.timestamp == file.lastModified()) {
                            newState = info.state;
                        } else {
                            newState = FileState.INITIAL;
                        }
                        break;
                    case ERROR: // fall through
                    case INITIAL:
                        newState = FileState.INITIAL;
                        break;
                    case UNCONTROLLED:
                    case INEXISTENT:
                        newState = info.state;
                        break;
                    default:
                        CndUtils.assertTrue(false, "Unexpected state: " + info.state); //NOI18N
                        return;
                }
            } else {
                if (info != null && info.state == FileState.UNCONTROLLED) {
                    newState = FileState.UNCONTROLLED;
                } else {
                    newState = FileState.INEXISTENT;
                }            
            }
            CndUtils.assertTrue(newState == FileState.INITIAL || newState == FileState.COPIED
                    || newState == FileState.TOUCHED || newState == FileState.UNCONTROLLED
                    || newState == FileState.INEXISTENT,
                    "State shouldn't be ", newState); //NOI18N
            if (USE_TIMESTAMPS) {
                long fileTime = file.exists() ? Math.max(0, file.lastModified() + timeSkew) : 0;
                long seconds = fileTime / 1000;
                long microseconds = (fileTime % 1000) * 1000;
                responseStream.printf("%c %d %d %d %s\n", newState.id, file.length(), seconds, microseconds, remotePath); // NOI18N
            } else {
                responseStream.printf("%c %d %s\n", newState.id, file.length(), remotePath); // NOI18N
            }
            responseStream.flush(); //TODO: remove?
            if (newState == FileState.INITIAL ) {
                newState = FileState.TOUCHED;
            }
            fileData.setState(file, newState);
        }
    }

    private static void gatherFiles(File file, String base, FileFilter filter, List<FileGatheringInfo> files) {
        // it is assumed that the file itself was already filtered
        String remotePath = isEmpty(base) ? file.getName() : base + '/' + file.getName();
        files.add(new FileGatheringInfo(file, remotePath));
        if (file.isDirectory()) {
            File[] children = file.listFiles(filter);
            for (File child : children) {
                String newBase = isEmpty(base) ? file.getName() : (base + "/" + file.getName()); // NOI18N
                gatherFiles(child, newBase, filter, files);
            }
        }
    }

    private static FileGatheringInfo addFileGatheringInfo(List<FileGatheringInfo> filesToFeed, final File file, String remoteFilePathName) {
        FileGatheringInfo info = new FileGatheringInfo(file, remoteFilePathName);
        filesToFeed.add(info);
        return info;
    }


    private static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

}
