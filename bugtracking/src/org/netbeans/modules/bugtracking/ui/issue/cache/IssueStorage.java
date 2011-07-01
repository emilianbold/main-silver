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

package org.netbeans.modules.bugtracking.ui.issue.cache;

import org.netbeans.modules.bugtracking.util.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.BugtrackingConfig;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache.IssueEntry;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
class IssueStorage {

    private static IssueStorage instance;
    private File storage;
    private static final String STORAGE_FILE  = "storage";              // NOI18N
    private static final String STORAGE_VERSION_1_0 = "1.0";            // NOI18N
    private static final String STORAGE_VERSION_1_1 = "1.1";            // NOI18N
    private static final String STORAGE_VERSION = STORAGE_VERSION_1_1;  // NOI18N
    private String QUERY_ARCHIVED_SUFIX = ".qa";                        // NOI18N
    private String QUERY_SUFIX = ".q";                                  // NOI18N
    private String ISSUE_SUFIX = ".i";                                  // NOI18N

    private FileLocks fileLocks = new FileLocks();

    private IssueStorage() { }

    public static IssueStorage getInstance() {
        if(instance == null) {
            instance = new IssueStorage();
            instance.initStorage();
        }
        return instance;
    }

    private void initStorage() {
        storage = getStorageRootFile();
        if(!storage.exists()) {
            storage.mkdirs();
        }
        writeStorage();
        Task t = BugtrackingManager.getInstance().getRequestProcessor().create(new Runnable() {
            public void run() {
                cleanup();
            }
        });
        t.schedule(0);
    }

    long getReferenceTime(String nameSpace) throws IOException {
        File folder = getNameSpaceFolder(nameSpace);
        File data = new File(folder, "data");
        
        FileLock lock = fileLocks.getLock(data);
        try {
            synchronized(lock) {
                long ret = -1;
                if(data.exists()) {
                    DataInputStream is = null;
                    try {
                        is = getDataInputStream(data);
                        ret = is.readLong();
                        return ret;
                    } catch (EOFException ex) {
                        BugtrackingManager.LOG.log(Level.SEVERE, data.getAbsolutePath(), ex);
                        return -1;
                    } catch (InterruptedException ex) {
                        BugtrackingManager.LOG.log(Level.WARNING, null, ex);
                        IOException ioe = new IOException(ex.getMessage());
                        ioe.initCause(ex);
                        throw ioe;
                    } finally {
                        if(BugtrackingManager.LOG.isLoggable(Level.FINE)) {
                            String dateString = ret > -1 ? new SimpleDateFormat().format(new Date(ret)) : "null";   // NOI18N
                            BugtrackingManager.LOG.log(Level.FINE, "finished reading greference time {0} - {1}", new Object[] {nameSpace, dateString}); // NOI18N
                        }
                        try { if(is != null) is.close(); } catch (IOException e) {}
                    }
                } else {
                    data.createNewFile();
                    ret = System.currentTimeMillis();
                    DataOutputStream os = null;
                    try {
                        os = getDataOutputStream(data, false);
                        os.writeLong(ret);
                        return ret;
                    } catch (InterruptedException ex) {
                        BugtrackingManager.LOG.log(Level.WARNING, null, ex);
                        IOException ioe = new IOException(ex.getMessage());
                        ioe.initCause(ex);
                        throw ioe;
                    } finally {
                        if(BugtrackingManager.LOG.isLoggable(Level.FINE)) {
                            String dateString = ret > -1 ? new SimpleDateFormat().format(new Date(ret)) : "null";   // NOI18N
                            BugtrackingManager.LOG.log(Level.FINE, "finished writing greference time {0} - {1}", new Object[] {nameSpace, dateString}); // NOI18N
                        }
                        try { if(os != null) os.close(); } catch (IOException e) {}
                    }
                }
            }
        } finally {
            if(lock != null) { lock.release(); }
        }
    }

    void storeIssue(String nameSpace, IssueEntry entry) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start storing issue {0} - {1}", new Object[] {nameSpace, entry.getId()}); // NOI18N
        InputStream is = null;
        DataOutputStream dos = null;
        FileLock lock = null;
        try {
            File issueFile = getIssueFile(getNameSpaceFolder(nameSpace), entry.getId());
            lock = fileLocks.getLock(issueFile);
            synchronized(lock) {
                dos = getIssueOutputStream(issueFile);
                if(dos == null) {
                    return;
                }
                dos.writeBoolean(entry.wasSeen());
                dos.writeLong(entry.getLastSeenModified());
                dos.writeInt(entry.getLastUnseenStatus());
                if(entry.getSeenAttributes() != null) {
                    for(Entry<String, String> e : entry.getSeenAttributes().entrySet()) {
                        writeString(dos, e.getKey());
                        writeString(dos, e.getValue());
                    }
                }
            }
        } catch (InterruptedException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        } finally {
            try { if(dos != null) dos.close(); } catch (IOException e) {}
            try { if(is != null) is.close(); } catch (IOException e) {}
            if(lock != null) {
                lock.release();
            }
            BugtrackingManager.LOG.log(Level.FINE, "finished storing issue {0} - {1}", new Object[] {nameSpace, entry.getId()}); // NOI18N
        }
    }

    void readIssue(String nameSpace, IssueEntry entry) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start reading issue {0} - {1}", new Object[] {nameSpace, entry.getId()}); // NOI18N
        DataInputStream is = null;
        FileLock lock = null;
        try {
            File issueFile = getIssueFile(getNameSpaceFolder(nameSpace), entry.getId());
            lock = fileLocks.getLock(issueFile);
            synchronized(lock) {
                is = getIssueInputStream(issueFile);
                if(is == null) {
                    return;
                }
                Map<String, String> m = new HashMap<String, String>();
                boolean seen = is.readBoolean();
                long lastModified = -1;
                int lastStatus = IssueCache.ISSUE_STATUS_UNKNOWN;
                if(!STORAGE_VERSION.equals(STORAGE_VERSION_1_0)) {
                    lastModified = is.readLong();
                    lastStatus = is.readInt();
                }
                while(true) {
                    try {
                        String key = readString(is);
                        String value = readString(is);
                        m.put(key, value);
                    } catch (EOFException e) { // XXX
                        break;
                    }
                }
                entry.setSeenAttributes(m);
                entry.setSeen(seen);
                entry.setLastSeenModified(lastModified);
                entry.setLastUnseenStatus(lastStatus);
            }
        } catch (InterruptedException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        } finally {
            if(is != null) try { is.close(); } catch(IOException e) {}
            if(lock != null) {
                lock.release();
            }
            BugtrackingManager.LOG.log(Level.FINE, "finished reading issue {0} - {1}", new Object[] {nameSpace, entry.getId()}); // NOI18N
        }
    }

    List<String> readQuery(String nameSpace, String queryName) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start reading query {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N

        DataInputStream dis = null;
        FileLock lock = null;
        try {
            File folder = getNameSpaceFolder(nameSpace);
            if(!folder.exists()) return Collections.emptyList();

            File f = getQueryFile(folder, queryName, false);
            lock = fileLocks.getLock(f);
            synchronized(lock) {
                dis = getQueryInputStream(f);
                return readQuery(dis);
            }
        } catch (InterruptedException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finished reading query {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
            if(dis != null) try { dis.close(); } catch(IOException e) {}
            if(lock != null) {
                lock.release();
            }
        }
    }

    private List<String> readQuery(DataInputStream dis) throws IOException {
        if(dis == null) return Collections.emptyList();
        List<String> ids = new ArrayList<String>();
        while(true) {
            String id = null;
            try {
                id = readString(dis);
            } catch (EOFException e) {
                    break;
            }
            ids.add(id);
        }
        return ids;
    }

    long getQueryTimestamp(String nameSpace, String name) {
        File folder = getNameSpaceFolder(nameSpace);
        File file = new File(folder, TextUtils.encodeURL(name) + QUERY_SUFIX);
        return file.lastModified();
    }

    Map<String, Long> readArchivedQueryIssues(String nameSpace, String queryName) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start reading archived query issues {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        long now = System.currentTimeMillis();
        long ttl = BugtrackingConfig.getInstance().getArchivedIssuesTTL() * 1000 * 60 * 60 * 24;

        FileLock lock = null;
        DataInputStream dis = null;
        try {
            File folder = getNameSpaceFolder(nameSpace);
            if(!folder.exists()) return Collections.emptyMap();

            File f = getQueryFile(folder, queryName, true);
            lock = fileLocks.getLock(f);
            synchronized(lock) {
                dis = getQueryInputStream(f);
                if(dis == null) return Collections.emptyMap();
                Map<String, Long> ids = readArchivedQueryIssues(dis);
                Iterator<String> it = ids.keySet().iterator();
                while(it.hasNext()) {
                    String id = it.next();
                    long ts = ids.get(id);
                    if(ts < now - ttl) {
                        it.remove();
                    }
                }
                return ids;
            }
        } catch (InterruptedException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finished reading archived query issues {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
            if(dis != null) try { dis.close(); } catch(IOException e) {}
            if(lock != null) {
                lock.release();
            }
        }
    }

    private Map<String, Long> readArchivedQueryIssues(DataInputStream dis) throws IOException {
        if(dis == null) return Collections.emptyMap();
        Map<String, Long> ids = new HashMap<String, Long>();
        while(true) {
            String id = null;
            long ts = -1;
            try {
                id = readString(dis);
                ts = dis.readLong();
                ids.put(id, ts);
            } catch (EOFException e) {
                break;
            }
        }
        return ids;
    }

    void removeQuery(String nameSpace, String queryName) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start removing query {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        FileLock lock = null;
        try {
            File folder = getNameSpaceFolder(nameSpace);
            File query = getQueryFile(folder, queryName, false);
            if(query.exists()) {
                lock = fileLocks.getLock(query);
                try {
                    synchronized(lock) {
                        BugtrackingUtil.deleteRecursively(query);
                    }
                } finally {
                    if(lock != null) { lock.release(); }
                }
            }
            lock = null;
            File queryArchived = getQueryFile(folder, queryName, true);
            if(queryArchived.exists()) {
                lock = fileLocks.getLock(queryArchived);
                try {
                    synchronized(lock) {
                        BugtrackingUtil.deleteRecursively(queryArchived);
                    }
                } finally {
                    if(lock != null) {lock.release();}
                }
            }
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finished removing query {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        }
    }

    void storeQuery(String nameSpace, String queryName, String[] ids) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start storing query issues {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        FileLock lock = null;
        DataOutputStream dos = null;
        try {
            File folder = getNameSpaceFolder(nameSpace);
            File f = getQueryFile(folder, queryName, false);
            lock = fileLocks.getLock(f);
            synchronized(lock) {
                dos = getQueryOutputStream(f);
                for (String id : ids) {
                    writeString(dos, id);
                }
                dos.flush();
            }
        } catch (InterruptedException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finished storing query issues {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
            if(dos != null) try { dos.close(); } catch(IOException e) {}
            if(lock != null) {
                lock.release();
            }
        }
    }

    void storeArchivedQueryIssues(String nameSpace, String queryName, String[] ids) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start storing archived query issues {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        long now = System.currentTimeMillis();
        Map<String, Long> archived = readArchivedQueryIssues(nameSpace, queryName);
        DataOutputStream dos = null;
        FileLock lock = null;
        try {
            File folder = getNameSpaceFolder(nameSpace);
            File f = getQueryFile(folder, queryName, true);
            lock = fileLocks.getLock(f);
            synchronized(lock) {
                dos = getQueryOutputStream(f);
                for (String id : ids) {
                    writeString(dos, id);
                    Long ts = archived.get(id);
                    if(ts != null && ts.longValue() != -1) {
                        dos.writeLong(ts);
                    } else {
                        dos.writeLong(now);
                    }
                }
                dos.flush();
            }
        } catch (InterruptedException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        } finally {
            try { if(dos != null) dos.close(); } catch (IOException e) {}
            if(lock != null) {
                lock.release();
            }
            BugtrackingManager.LOG.log(Level.FINE, "finished storing archived query issues {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        }
    }

    void cleanup() {
        try {
            BugtrackingManager.LOG.log(Level.FINE, "starting bugtrackig storage cleanup"); // NOI18N

            File root = getStorageRootFile();
            File[] repos = root.listFiles();
            if(repos == null) {
                return;
            }
            for (File repo : repos) {
                cleanup(repo);
            }
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finnished bugtrackig storage cleanup"); // NOI18N
        }
    }

    void cleanup(String namespace) {
        try {
            BugtrackingManager.LOG.log(Level.FINE, "starting bugtrackig storage cleanup for {0}", new Object[] {namespace}); // NOI18N
            cleanup(getNameSpaceFolder(namespace));
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finnished bugtrackig storage cleanup for {0}", new Object[] {namespace}); // NOI18N
        }
    }


    private void cleanup(File repo) {
        try {
            BugtrackingManager.LOG.log(Level.FINE, "starting bugtrackig storage cleanup for {0}", new Object[] {repo.getAbsoluteFile()}); // NOI18N
            Set<String> livingIssues = new HashSet<String>();
            File[] queries = repo.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(QUERY_SUFIX);
                }
            });
            if(queries != null && queries.length > 0) {
                for (File lq : queries) {
                    FileLock lock = fileLocks.getLock(lq);
                    List<String> ids;
                    try {
                        synchronized(lock) {
                            ids = readQuery(getDataInputStream(lq));
                        }
                    } finally {
                        if(lock != null) lock.release();
                    }
                    if(ids == null || ids.size() == 0) {
                        continue;
                    }
                    livingIssues.addAll(ids);
                }
            }
            queries = repo.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(QUERY_ARCHIVED_SUFIX);
                }
            });
            if(queries != null) {
                for (File lq : queries) {
                    Map<String, Long> ids;
                    FileLock lock = fileLocks.getLock(lq);
                    try {
                        synchronized(lock) {
                            ids = readArchivedQueryIssues(getDataInputStream(lq));
                        }
                    } finally {
                        if(lock != null) lock.release();
                    }
                    if(ids == null || ids.size() == 0) {
                        continue;
                    }
                    livingIssues.addAll(ids.keySet());
                }
            }
            BugtrackingManager.LOG.log(Level.FINER, "living query issues {0}", new Object[] {livingIssues}); // NOI18N
            File[] issues = repo.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(ISSUE_SUFIX);
                }
            });
            if(issues != null) {
                for (File issue : issues) {
                    String id = issue.getName();
                    id = id.substring(0, id.length() - ISSUE_SUFIX.length());
                    if(!livingIssues.contains(id)) {
                        BugtrackingManager.LOG.log(Level.FINE, "removing issue {0}", new Object[] {id}); // NOI18N
                        FileLock lock = fileLocks.getLock(issue);
                        try {
                            synchronized(lock) {
                                issue.delete();
                            }
                        } finally {
                            if(lock != null) lock.release();
                        }
                    }
                }
            }
            
        } catch (IOException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex); // NOI18N
        } catch (InterruptedException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex); // NOI18N
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finished bugtrackig storage cleanup for {0}", new Object[] {repo.getAbsoluteFile()}); // NOI18N
        }
    }

    private File getStorageRootFile() {
        String userDir = System.getProperty("netbeans.user");                   // NOI18N
        return new File(new File(userDir, "var"), "bugtracking");               // NOI18N
    }

    private void writeStorage() {
        DataOutputStream dos = null;
        try {
            dos = getDataOutputStream(new File(storage, STORAGE_FILE), false);
            writeString(dos, STORAGE_VERSION);
            dos.flush();
        } catch (IOException e) {
            BugtrackingManager.LOG.log(Level.INFO, null, e);
        } catch (InterruptedException ie) {
            BugtrackingManager.LOG.log(Level.INFO, null, ie);
        } finally {
            if (dos != null) {
                try { dos.close(); } catch (IOException e) { }
            }
        }
    }

    private void writeString(DataOutputStream dos, String str) throws IOException {
        if(str != null) {
            dos.writeInt(str.length());
            dos.writeChars(str);
        } else {
            dos.writeInt(0);
        }
    }

    private static String readString(DataInputStream dis) throws IOException {
        int len = dis.readInt();
        if(len == 0) {
            return "";                                                          // NOI18N
        }
        StringBuffer sb = new StringBuffer();                
        while(len-- > 0) {
            char c = dis.readChar();
            sb.append(c);                       
        }        
        return sb.toString();
    }

    private DataOutputStream getIssueOutputStream(File issueFile) throws IOException, InterruptedException {
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(getFileOutputStream(issueFile, false)));
        ZipEntry entry = new ZipEntry(issueFile.getName());
        zos.putNextEntry(entry);
        return new DataOutputStream(zos);
    }

    private DataInputStream getIssueInputStream(File file) throws IOException, InterruptedException {
        if(!file.exists()) return null;
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(getFileInputStream(file)));
        zis.getNextEntry();
        return new DataInputStream(zis);
    }

    private File getIssueFile(File folder, String id) {
        return new File(folder, id + ISSUE_SUFIX);
    }

    private DataOutputStream getDataOutputStream(File file, boolean append) throws IOException, InterruptedException {
        return new DataOutputStream(getFileOutputStream(file, append));
    }

    private DataInputStream getDataInputStream(File file) throws IOException, InterruptedException {
        return new DataInputStream(getFileInputStream(file));
    }

    private FileOutputStream getFileOutputStream(File file, boolean append) throws IOException, InterruptedException {
        int retry = 0;
        while (true) {
            try {
                return new FileOutputStream(file, append);
            } catch (IOException ioex) {
                retry++;
                if (retry > 7) {
                    throw ioex;
                }
                Thread.sleep(retry * 30);
            }
        }
    }

    private FileInputStream getFileInputStream(File file) throws IOException, InterruptedException {
        int retry = 0;
        while (true) {
            try {
                return new FileInputStream(file);
            } catch (IOException ioex) {
                retry++;
                if (retry > 7) {
                    throw ioex;
                }
                Thread.sleep(retry * 30);
            }
        }
    }

    private static void copyStreams(OutputStream out, InputStream in) throws IOException {
        byte [] buffer = new byte[4096];
        for (;;) {
            int n = in.read(buffer);
            if (n < 0) break;
            out.write(buffer, 0, n);
        }
    }

    private File getNameSpaceFolder(String url) {
        File folder = new File(storage, TextUtils.encodeURL(url));
        if(!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    private DataOutputStream getQueryOutputStream(File queryFile) throws IOException, InterruptedException {
        return getDataOutputStream(queryFile, false);
    }

    private DataInputStream getQueryInputStream(File queryFile) throws IOException, InterruptedException {
        if(!queryFile.exists()) return null;
        return getDataInputStream(queryFile);
    }

    private File getQueryFile(File folder, String queryName, boolean archived){
        return new File(folder, TextUtils.encodeURL(queryName) + (archived ? QUERY_ARCHIVED_SUFIX : QUERY_SUFIX));
    }

    private class FileLocks {
        private final Map<String, FileLock> locks = new HashMap<String, FileLock>();
        FileLock getLock(File file) {
            synchronized(locks) {
                FileLock fl = locks.get(file.getAbsolutePath());
                if(fl == null) {
                    fl = new FileLock(file);
                }
                locks.put(file.getAbsolutePath(), fl);
                return fl;
            }
        }
    }

    class FileLock {
        private final File file;
        public FileLock(File file) {
            this.file = file;
        }
        void release() {
            synchronized(fileLocks.locks) {
                fileLocks.locks.remove(file.getAbsolutePath());
            }
        }
    }
    
}
