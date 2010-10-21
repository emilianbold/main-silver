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

package org.netbeans.modules.cnd.remote.fs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.remote.ConnectionNotifier;
import org.netbeans.modules.cnd.remote.server.RemoteServerListUI;
import org.netbeans.modules.cnd.remote.support.RemoteCodeModelUtils;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.api.util.ShellScriptRunner;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Responsible for copying files from remote host.
 * Each instance of the RemoteFileSupport class corresponds to a remote server
 * 
 * @author Vladimir Kvashin
 */
public class RemoteFileSupport extends NamedRunnable {

    private final PendingFilesQueue pendingFilesQueue = new PendingFilesQueue();
    private final ExecutionEnvironment execEnv;

    /** File transfer statistics */
    private int fileCopyCount;

    /** Directory synchronization statistics */
    private int dirSyncCount;

    private final Object mainLock = new Object();
    private Map<File, Object> locks = new HashMap<File, Object>();

    private final Map<ExecutionEnvironment, Boolean> cancels = new HashMap<ExecutionEnvironment, Boolean>();

    public static final String FLAG_FILE_NAME = ".rfs"; // NOI18N

    public RemoteFileSupport(ExecutionEnvironment execEnv) {
        super(NbBundle.getMessage(RemoteFileSupport.class, "RemoteDownloadTask.TITLE", RemoteUtil.getDisplayName(execEnv)));
        this.execEnv = execEnv;
        resetStatistic();
    }

    private Object getLock(File file) {
        synchronized(mainLock) {
            Object lock = locks.get(file);
            if (lock == null) {
                lock = new Object();
                locks.put(file, lock);
            }
            return lock;
        }
    }

    private void removeLock(File file) {
        synchronized(mainLock) {
            locks.remove(file);
        }
    }

    private static final String CC_STR = "cc"; // NOI18N
    /*package*/static final String POSTFIX = ".cnd.rfs.small"; // NOI18N

    /*package*/static String fixCaseSensitivePathIfNeeded(String in) {
        return _fixCaseSensitivePathIfNeeded(in, false);
    }

    /** for TEST purposes ONLY */
    /*package*/static String testFixCaseSensitivePathIfNeeded(String in) {
        return _fixCaseSensitivePathIfNeeded(in, true);
    }

    private static String _fixCaseSensitivePathIfNeeded(String in, boolean force) {
        if (!force && CndFileUtils.isSystemCaseSensitive()) {
            return in;
        }
        StringBuilder out = new StringBuilder(in);
        // now we support only cc replacement into cc.cnd
        int left = out.indexOf(CC_STR); // NOI18N
        if (left >= 0 && out.length() >= CC_STR.length()) {
             // check what we have before "cc"
            if (left > 0 && out.charAt(left-1) != '/') { // NOI18N
                return out.toString();
            }
            int right = left + CC_STR.length();
            // check what we have after "cc"
            if (out.length() > right && out.charAt(right) != '/') { // NOI18N
                return out.toString();
            }
            if (right == out.length()) {
                out.append(POSTFIX);
            } else {
                out.insert(right, POSTFIX);
            }
        }
        return out.toString();
    }

    /*package*/static String fromFixedCaseSensitivePathIfNeeded(String in) {
        return in.replaceAll(POSTFIX, "");
    }
    
    public void ensureFileSync(File file, String remotePath) throws IOException, InterruptedException, ExecutionException, ConnectException {
        if (!file.exists() || file.length() == 0) {
            synchronized (getLock(file)) {
                // dbl check is ok here since it's file-based
                if (!file.exists() || file.length() == 0) {
                    syncFile(file, fromFixedCaseSensitivePathIfNeeded(remotePath));
                    removeLock(file);
                }
            }
        }
    }

    private void syncFile(File file, String remotePath) throws IOException, InterruptedException, ExecutionException, ConnectException {
        CndUtils.assertTrue(!file.exists() || file.isFile(), "not a file " + file.getAbsolutePath());
        checkConnection(file, remotePath, false);
        Future<Integer> task = CommonTasksSupport.downloadFile(remotePath, execEnv, file.getAbsolutePath(), null);
        try {
            int rc = task.get().intValue();
            if (rc == 0) {
                fileCopyCount++;
            } else {
                throw new IOException("Can't copy file " + file.getAbsolutePath() + // NOI18N
                        " from " + execEnv + ':' + remotePath + ": rc=" + rc); //NOI18N
            }
        } catch (InterruptedException ex) {
            truncate(file);
            throw ex;
        } catch (ExecutionException ex) {
            truncate(file);
            throw ex;
        }
    }

    private void truncate(File file) throws IOException {
        OutputStream os = new FileOutputStream(file);
        os.close();

    }

    /**
     * Ensured that the directory is synchronized
     */
    public final DirectoryAttributes ensureDirSync(File dir, String remoteDir) throws IOException, ConnectException {
        DirectoryAttributes attrs = null;
        if( ! dir.exists() || ! CndFileUtils.isValidLocalFile(dir, FLAG_FILE_NAME)) {
            synchronized (getLock(dir)) {
                // dbl check is ok here since it's file-based
                File flagFile = CndFileUtils.createLocalFile(dir, FLAG_FILE_NAME);
                if( ! dir.exists() || ! CndFileUtils.isValidLocalFile(dir, FLAG_FILE_NAME)) {
                    attrs = syncDirStruct(dir, fromFixedCaseSensitivePathIfNeeded(remoteDir), flagFile);
                    removeLock(dir);
                }
            }
        }
        return attrs;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("RV") // it's ok to ignore File.createNewFile() return value
    private DirectoryAttributes syncDirStruct(final File dir, final String remoteDir, final File flagFile) throws IOException, ConnectException {
        Parameters.notNull("null remote dir", remoteDir);
        if (dir.exists()) {
            CndUtils.assertTrue(dir.isDirectory(), dir.getAbsolutePath() + " is not a directory"); //NOI18N
        }

        final String rdir = remoteDir.length() == 0 ? "/" : remoteDir; // NOI18N
        checkConnection(dir, rdir, true);

        final String script = "test -d \"" + rdir + "\" && " + // NOI18N
                "cd \"" + rdir + "\" &&" + // NOI18N
                "for D in `/bin/ls`; do " + // NOI18N
                "if [ -d \"$D\" ]; then echo D \"$D\"; else if [ -w \"$D\" ]; then echo w \"$D\"; else echo r \"$D\"; fi; fi; done"; // NOI18N

        final AtomicReference<IOException> criticalException = new AtomicReference<IOException>();
        final AtomicReference<IOException> nonCriticalException = new AtomicReference<IOException>();
        final AtomicBoolean dirCreated = new AtomicBoolean(false);
        final DirectoryAttributes attrs = new DirectoryAttributes(flagFile);

        LineProcessor outputProcessor = new LineProcessor() {
            @Override
            public void processLine(String inputLine) {
                if (!dirCreated.get()) {
                    dirCreated.set(true);
                    if (!dir.mkdirs() && !dir.exists()) {
                        criticalException.set(new IOException("Can not create directory " + dir.getAbsolutePath())); //NOI18N
                        return;
                    }
                }
                CndUtils.assertTrueInConsole(inputLine.length() > 2, "unexpected file information " + inputLine); // NOI18N
                char mode = inputLine.charAt(0);
                boolean directory = (mode == 'D');
                String fileName = inputLine.substring(2);
                if (directory) {
                    fileName = fixCaseSensitivePathIfNeeded(fileName);
                } else {
                    attrs.setWritable(fileName, mode == 'w');
                }
                File file = CndFileUtils.createLocalFile(dir, fileName);
                try {
                    RemoteUtil.LOGGER.log(Level.FINEST, "\tcreating {0}", fileName); // NOI18N
                    if (directory) {
                        if (!file.mkdirs() && !file.exists()) {
                            throw new IOException("can't create directory " + file.getAbsolutePath()); // NOI18N
                        }
                    } else {
                        file.createNewFile();
                    }
                } catch (IOException ioex) {
                    RemoteUtil.LOGGER.log(Level.WARNING,
                            "Error creating {0}{1}{2}: {3}", // NOI18N
                            new Object[]{directory ? "directory" : "file", ' ', file.getAbsolutePath(), ioex.getMessage()}); // NOI18N
                    nonCriticalException.set(ioex);
                }
            }

            @Override
            public void reset() {
            }

            @Override
            public void close() {
            }
        };

        LineProcessor errorProcessor = new LineProcessor() {

            @Override
            public void processLine(String line) {
                RemoteUtil.LOGGER.log(Level.FINEST,
                        "Error [{0}]\n\ton Synchronizing dir {1} with {2}{3}{4}", // NOI18N
                        new Object[]{line, dir.getAbsolutePath(), execEnv, ':', rdir});
            }

            @Override
            public void reset() {
            }

            @Override
            public void close() {
            }
        };

        ShellScriptRunner scriptRunner = new ShellScriptRunner(execEnv, script, outputProcessor);
        scriptRunner.setErrorProcessor(errorProcessor);

        RemoteUtil.LOGGER.log(Level.FINEST, "Synchronizing dir {0} with {1}{2}{3}", // NOI18N
                new Object[]{dir.getAbsolutePath(), execEnv, ':', rdir});

        int rc = scriptRunner.execute();

        if (nonCriticalException.get() != null) {
            IOException e = nonCriticalException.get();
            IOException ioe = new IOException("Error synchronizing " + rdir + " at " + execEnv.getDisplayName() + ": " + e.getMessage(), e); //NOI18N;
            Exceptions.printStackTrace(ioe);
        }
        if (criticalException.get() != null) {
            IOException e = criticalException.get();
            IOException ioe = new IOException("Error synchronizing " + rdir + " at " + execEnv.getDisplayName() + ": " + e.getMessage(), e); //NOI18N;
            throw ioe;
        }

        if (dirCreated.get()) {
            File flag = CndFileUtils.createLocalFile(dir, FLAG_FILE_NAME);
            RemoteUtil.LOGGER.log(Level.FINEST, "Creating Flag file {0}", flag.getAbsolutePath());
            try {
                flag.createNewFile(); // TODO: error processing
            } catch (IOException ie) {
                RemoteUtil.LOGGER.log(Level.FINEST, "FAILED creating Flag file {0}", flag.getAbsolutePath());
                criticalException.set(ie);
            }
            dirSyncCount++;
        }
        if (rc == 0) {
            CndUtils.assertTrue(flagFile.getParentFile().exists(), "File " + flagFile.getParentFile().getAbsolutePath() + " should exist"); //NOI18N
            attrs.store();
            return attrs;
        } else {
            if (RemoteUtil.LOGGER.isLoggable(Level.FINE)) {
                ExitStatus ls = ProcessUtils.execute(execEnv, "/bin/ls", rdir); // NOI18N
                RemoteUtil.LOGGER.log(Level.FINE, "Content of directory {0}:\n{1}", new Object[]{rdir, ls.output});
            }
            return null;
        }
    }

    /*package-local test method*/ final void resetStatistic() {
        this.dirSyncCount = 0;
        this.fileCopyCount = 0;
    }

    /*package-local test method*/ int getDirSyncCount() {
        return dirSyncCount;
    }

    /*package-local test method*/ int getFileCopyCount() {
        return fileCopyCount;
    }

    private void checkConnection(File localFile, String remotePath, boolean isDirectory) throws ConnectException {
        if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            RemoteUtil.LOGGER.log(Level.FINEST, "Adding notification for {0}:{1}", new Object[]{execEnv, remotePath}); //NOI18N
            pendingFilesQueue.add(localFile, remotePath, isDirectory);
            ConnectionNotifier.addTask(execEnv, this);
            throw new ConnectException();
        }
    }

    @Override
    protected void runImpl() {
        try {
            onConnect();
        } catch (ConnectException ex) {
            RemoteUtil.LOGGER.log(Level.INFO, NbBundle.getMessage(getClass(), "RemoteFileSystemNotifier.ERROR", execEnv), ex);
            ConnectionNotifier.addTask(execEnv, this);
        } catch (InterruptedException ex) {
            // don't report interruption
        } catch (InterruptedIOException ex) {
            // don't report interruption
        } catch (IOException ex) {
            RemoteUtil.LOGGER.log(Level.INFO, NbBundle.getMessage(getClass(), "RemoteFileSystemNotifier.ERROR", execEnv), ex);
            ConnectionNotifier.addTask(execEnv, this);
        } catch (ExecutionException ex) {
            RemoteUtil.LOGGER.log(Level.INFO, NbBundle.getMessage(getClass(), "RemoteFileSystemNotifier.ERROR", execEnv), ex);
            ConnectionNotifier.addTask(execEnv, this);
        }
    }


    // NB: it is always called in a specially created thread
    private void onConnect() throws InterruptedException, ConnectException, InterruptedIOException, IOException, ExecutionException {
        ProgressHandle handle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(getClass(), "Progress_Title", RemoteUtil.getDisplayName(execEnv)));
        handle.start();
        RemoteServerListUI.revalidate(execEnv);
        handle.switchToDeterminate(pendingFilesQueue.size());
        int cnt = 0;
        try {
            PendingFile pendingFile;
            // die after half a minute inactivity period
            while ((pendingFile = pendingFilesQueue.poll(1, TimeUnit.SECONDS)) != null) {
                if (pendingFile.isDirectory) {
                    ensureDirSync(pendingFile.localFile, pendingFile.remotePath);
                } else {
                    ensureFileSync(pendingFile.localFile, pendingFile.remotePath);
                }
                handle.progress(NbBundle.getMessage(getClass(), "Progress_Message", pendingFile.remotePath), cnt++); // NOI18N
            }
        } finally {
            handle.finish();
            RemoteCodeModelUtils.scheduleReparse(execEnv);
        }
    }

    private static class PendingFile {
        public final File localFile;
        public final String remotePath;
        private final boolean isDirectory;
        public PendingFile(File localFile, String remotePath, boolean isDirectory) {
            this.localFile = localFile;
            this.remotePath = remotePath;
            this.isDirectory = isDirectory;
        }
    }

    /**
     * NB: the class is not optimized, for in fact it's used from CndFileUtils,
     * which perform all necessary caching.
     */
    private static class PendingFilesQueue {

        private final BlockingQueue<PendingFile> queue = new LinkedBlockingQueue<PendingFile>();
        private final Set<String> remoteAbsPaths = new TreeSet<String>();

        public synchronized void add(File localFile, String remotePath, boolean isDirectory) {
            if (remoteAbsPaths.add(remotePath)) {
                queue.add(new PendingFile(localFile, remotePath, isDirectory));
            }
        }

        public synchronized PendingFile take() throws InterruptedException {
            PendingFile pendingFile = queue.take();
            remoteAbsPaths.remove(pendingFile.remotePath);
            return pendingFile;
        }

        public synchronized PendingFile poll(long timeout, TimeUnit unit) throws InterruptedException {
            PendingFile pendingFile = queue.poll(timeout, unit);
            if (pendingFile != null) {
                remoteAbsPaths.remove(pendingFile.remotePath);
            }
            return pendingFile;
        }
        
        public synchronized List<String> getPendingFiles() {
            return Collections.unmodifiableList(new ArrayList<String>(remoteAbsPaths));
        }

        public int size() {
            return remoteAbsPaths.size();
        }
    }
}
