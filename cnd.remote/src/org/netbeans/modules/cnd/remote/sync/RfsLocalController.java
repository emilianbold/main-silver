package org.netbeans.modules.cnd.remote.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.openide.util.Exceptions;

class RfsLocalController implements Runnable {

    private final BufferedReader requestReader;
    private final PrintStream responseStream;
    private final String remoteDir;
    private final File localDir;
    private final ExecutionEnvironment execEnv;
    private final PrintWriter err;
    private final FileData fileData;
    private final Set<String> processedFiles = new HashSet<String>();

    public RfsLocalController(ExecutionEnvironment executionEnvironment, File localDir, String remoteDir,
            InputStream requestStream, OutputStream responseStream, PrintWriter err,
            FileData fileData) {
        super();
        this.execEnv = executionEnvironment;
        this.localDir = localDir;
        this.remoteDir = remoteDir;
        this.requestReader = new BufferedReader(new InputStreamReader(requestStream));
        this.responseStream = new PrintStream(responseStream);
        this.err = err;
        this.fileData = fileData;
    }

    private void respond_ok() {
        responseStream.printf("1\n"); // NOI18N
        // NOI18N
        responseStream.flush();
    }

    private void respond_err(String tail) {
        responseStream.printf("0 %s\n", tail); // NOI18N
        // NOI18N
        responseStream.flush();
    }

    public void run() {
        long totalCopyingTime = 0;
        while (true) {
            try {
                String request = requestReader.readLine();
                String remoteFile = request;
                RemoteUtil.LOGGER.finest("LC: REQ " + request);
                if (request == null) {
                    break;
                }
                if (processedFiles.contains(remoteFile)) {
                    RemoteUtil.LOGGER.info("RC asks for file " + remoteFile + " again?!");
                    respond_ok();
                    continue;
                } else {
                    processedFiles.add(remoteFile);
                }
                if (remoteFile.startsWith(remoteDir)) { // FIXUP: change startsWith(remoteDir) with more smart code
                    File localFile = new File(localDir, remoteFile.substring(remoteDir.length()));
                    if (localFile.exists() && !localFile.isDirectory()) {
                        FileState state = fileData.getState(localFile);
                        if (needsCopying(localFile)) {
                            RemoteUtil.LOGGER.finest("LC: uploading " + localFile + " to " + remoteFile + " started");
                            long fileTime = System.currentTimeMillis();
                            Future<Integer> task = CommonTasksSupport.uploadFile(localFile.getAbsolutePath(), execEnv, remoteFile, 511, err);
                            try {
                                int rc = task.get();
                                fileTime = System.currentTimeMillis() - fileTime;
                                totalCopyingTime += fileTime;
                                System.err.printf("LC: uploading %s to %s finished; rc=%d time =%d total time = %d ms \n", localFile, remoteFile, rc, fileTime, totalCopyingTime);
                                if (rc == 0) {
                                    fileData.setState(localFile, FileState.COPIED);
                                    respond_ok();
                                } else {
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
                            RemoteUtil.LOGGER.finest("LC: file " + localFile + " not accepted");
                            respond_ok();
                        }
                    } else {
                        respond_ok();
                    }
                } else {
                    respond_ok();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        fileData.store();
    }

    public boolean needsCopying(File file) {
        FileData.FileInfo info = fileData.getFileInfo(file);
        if (info == null) {
            return false;
        } else {
            switch (info.state) {
                case COPIED:
                    return file.lastModified() != info.timestamp;
                case TOUCHED:
                    return true;
                case INITIAL:
                    return true;
                case UNCONTROLLED:
                    return false;
                case ERROR:
                    return true; // TODO: ???
                default:
                    CndUtils.assertTrue(false, "Unexpecetd state: " + info.state); //NOI18N
                    return false;
            }
        }
    }

    void shutdown() {
        fileData.store();
    }

    private static class FileGatheringInfo {
        public final File file;
        public final String relPath;
        public FileGatheringInfo(File file, String relPath) {
            this.file = file;
            this.relPath = relPath;
        }
        @Override
        public String toString() {
            return relPath;
        }
    }

    /**
     * Feeds remote controller with the list of files and their lengths
     * @param rcOutputStream
     */
    void feedFiles(OutputStream rcOutputStream, SharabilityFilter filter) {
        PrintWriter writer = new PrintWriter(rcOutputStream);
        List<FileGatheringInfo> files = new ArrayList<FileGatheringInfo>(512);
        File[] children = localDir.listFiles(filter);
        for (File child : children) {
            gatherFiles(child, null, filter, files);
        }
        Collections.sort(files, new Comparator<FileGatheringInfo>() {
            public int compare(FileGatheringInfo f1, FileGatheringInfo f2) {
                if (f1.file.isDirectory() || f2.file.isDirectory()) {
                    if (f1.file.isDirectory() && f2.file.isDirectory()) {
                        return f1.relPath.compareTo(f2.relPath);
                    } else {
                        return f1.file.isDirectory() ? -1 : +1;
                    }
                } else {
                    long delta = f1.file.lastModified() - f2.file.lastModified();
                    return (delta == 0) ? 0 : ((delta < 0) ? -1 : +1); // signum(delta)
                }
            }
        });
        for (FileGatheringInfo info : files) {
            if (info.file.isDirectory()) {
                String text = String.format("D %s", info.relPath); // NOI18N
                writer.printf("%s\n", text); // adds LF
                writer.flush(); //TODO: remove?
            } else {
                String text = String.format("%d %s", info.file.length(), info.relPath); // NOI18N
                writer.printf("%s\n", text); // adds LF
                writer.flush(); //TODO: remove?
                fileData.setState(info.file, FileState.TOUCHED);
            }
        }
        writer.printf("\n"); // NOI18N
        writer.flush();
        fileData.store();
    }

    private static void gatherFiles(File file, String base, FileFilter filter, List<FileGatheringInfo> files) {
        // it is assumed that the file itself was already filtered
        String fileName = isEmpty(base) ? file.getName() : base + '/' + file.getName();
        files.add(new FileGatheringInfo(file, fileName));
        if (file.isDirectory()) {
            File[] children = file.listFiles(filter);
            for (File child : children) {
                String newBase = isEmpty(base) ? file.getName() : (base + "/" + file.getName()); // NOI18N
                gatherFiles(child, newBase, filter, files);
            }
        }
    }

    private static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

}
