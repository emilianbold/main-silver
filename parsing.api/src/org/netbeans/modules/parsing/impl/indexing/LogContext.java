/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.impl.indexing;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
 public class LogContext {
    
    private static final RequestProcessor RP = new RequestProcessor("Thread dump shooter", 1); // NOI18N
    
    private static final int SECOND_DUMP_DELAY = 5 * 1000; 
    
    public enum EventType {
        PATH(1, 10),
        FILE(2, 20),
        // INDEXER has a special handling
        INDEXER(2, 5),
        MANAGER(1, 10),
        UI(1, 4);
        
        EventType(int minutes, int treshold) {
            String prefix = EventType.class.getName() + "." + name();
            Integer m = Integer.getInteger(prefix + ".minutes", minutes);
            Integer t = Integer.getInteger(prefix + ".treshold", treshold);
            
            this.minutes = m;
            this.treshold = t;
        }
        
        /**
         * Number of events per minute allowed
         */
        private int treshold;
        /**
         * Time in minutes
         */
        private int minutes;

        public int getTreshold() {
            return treshold;
        }

        public int getMinutes() {
            return minutes;
        }
    }

    public static LogContext create(
        @NonNull EventType eventType,
        @NullAllowed final String message) {
        return create(eventType, message, null);
    }

    public static LogContext create(
        @NonNull EventType eventType,
        @NullAllowed final String message,
        @NullAllowed final LogContext parent) {
        return new LogContext(
            eventType,
            Thread.currentThread().getStackTrace(),
            message,
            parent);
    }

    @Override
    public String toString() {
        final StringBuilder msg = new StringBuilder();
        createLogMessage(msg);
        return msg.toString();
    }
    
    private String createThreadDump() {
        StringBuilder sb = new StringBuilder();
        Map<Thread, StackTraceElement[]> allTraces = Thread.getAllStackTraces();
        for (Thread t : allTraces.keySet()) {
            sb.append(String.format("Thread id %d, \"%s\" (%s):\n", t.getId(), t.getName(), t.getState()));
            StackTraceElement[] elems = allTraces.get(t);
            for (StackTraceElement l : elems) {
                sb.append("\t").append(l).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    private synchronized void freeze() {
        this.frozen = true;
        this.timeCutOff = System.currentTimeMillis();
    }
    
    void log() {
        log(true);
    }

    void log(boolean cancel) {
        freeze();
        final LogRecord r = new LogRecord(Level.INFO, 
                cancel ? LOG_MESSAGE : LOG_EXCEEDS_RATE); //NOI18N
        r.setParameters(new Object[]{this});
        r.setResourceBundle(NbBundle.getBundle(LogContext.class));
        r.setResourceBundleName(LogContext.class.getPackage().getName() + ".Bundle"); //NOI18N
        r.setLoggerName(LOG.getName());
        final Exception e = new Exception(
                cancel ? 
                    "Scan canceled." : 
                    "Scan exceeded rate");    //NOI18N
        if (cancel) {
            e.setStackTrace(stackTrace);
            r.setThrown(e);
        }

        if (cancel) {
            threadDump = createThreadDump();
            RP.post(new Runnable() {

                @Override
                public void run() {
                    secondDump = createThreadDump();
                    LOG.log(r);
                }

            }, SECOND_DUMP_DELAY);
        } else {
            LOG.log(r);
        }
    }

    synchronized void absorb(@NonNull final LogContext other) {
        Parameters.notNull("other", other); //NOI18N
        if (absorbed == null) {
            absorbed = new ArrayDeque<LogContext>();
        }
        absorbed.add(other);
    }
    
    /**
     * Records this LogContext as 'executed'. Absorbed LogContexts are
     * not counted, as they are absorbed to an existing indexing work.
     */
    void recordExecuted() {
        executed = System.currentTimeMillis();
        STATS.record(this);
    }

    private final long timestamp;
    private long executed;
    private final EventType eventType;
    private final String message;
    private final StackTraceElement[] stackTrace;
    private final LogContext parent;
    //@GuardedBy("this")
    private Queue<LogContext> absorbed;
    private String threadDump;
    private String secondDump;
    
    // various path/root informaation, which was the reason for indexing.
    private Set<String>  filePathsChanged = Collections.emptySet();
    private Set<String>  classPathsChanged = Collections.emptySet();
    private Set<URL>        rootsChanged = Collections.emptySet();
    private Set<URL> filesChanged = Collections.emptySet();
    private Set<URI> fileObjsChanged = Collections.emptySet();
    
    /**
     * Source roots, which have been scanned so far in this LogContext
     */
    private Map<URL, Long>   scannedSourceRoots = new LinkedHashMap<URL, Long>();
    
    /**
     * Time spent in scanning source roots listed in {@link #scannedSourceRoots}
     */
    private long        totalScanningTime;
    
    private long        timeCutOff;
    
    /**
     * The current source root being scanned
     */
    private Map<Thread, RootInfo>    allCurrentRoots = new HashMap<Thread, RootInfo>();
    
    /**
     * The scanned root, possibly null.
     */
    private URL root;
    
    /**
     * If frozen becomes true, LogContext stops updating data.
     */
    private boolean frozen;
    
    private Map<String, Long>   totalIndexerTime = new HashMap<String, Long>();
    
    private class RootInfo {
        private URL     url;
        private long    startTime;

        public RootInfo(URL url, long startTime) {
            this.url = url;
            this.startTime = startTime;
        }
        
        public String toString() {
            return "< root = " + url.toString() + ", spent = " + (timeCutOff - startTime) + " >";
        }
    }
    
    public synchronized void noteRootScanning(URL currentRoot) {
        if (frozen) {
            return;
        }
        RootInfo ri = allCurrentRoots.get(Thread.currentThread());
        assert ri == null;
        allCurrentRoots.put(Thread.currentThread(), new RootInfo(
                    currentRoot,
                    System.currentTimeMillis()
        ));
    }
    
    public synchronized void finishScannedRoot(URL scannedRoot) {
        if (frozen) {
            return;
        }
        RootInfo ri = allCurrentRoots.get(Thread.currentThread());
        if (ri == null || !scannedRoot.equals(ri.url)) {
            return;
        }
        long time = System.currentTimeMillis();
        long diff = time - ri.startTime;
        totalScanningTime += diff;
        scannedSourceRoots.put(scannedRoot, diff);
        allCurrentRoots.remove(Thread.currentThread());
    }
    
    public synchronized void addIndexerTime(String fName, long addTime) {
        if (frozen) {
            return;
        }
        Long t = totalIndexerTime.get(fName);
        if (t == null) {
            t = Long.valueOf(0);
        }
        totalIndexerTime.put(fName, t + addTime);
    }
    
    public synchronized LogContext withRoot(URL root) {
        this.root = root;
        return this;
    }
    
    public synchronized LogContext addPaths(Collection<? extends ClassPath> paths) {
        if (paths == null || paths.isEmpty()) {
            return this;
        }
        if (classPathsChanged.isEmpty()) {
            classPathsChanged = new HashSet<String>(paths.size());
        }
        for (ClassPath cp : paths) {
            classPathsChanged.add(cp.toString());
        }
        return this;
    }
    
    public synchronized LogContext addFilePaths(Collection<String> paths) {
        if (paths == null || paths.isEmpty()) {
            return this;
        }
        if (filePathsChanged.isEmpty()) {
            filePathsChanged = new HashSet<String>(paths.size());
        }
        filePathsChanged.addAll(paths);
        return this;
    }
    
    public synchronized LogContext addRoots(Iterable<? extends URL> roots) {
        if (roots == null) {
            return this;
        }
        Iterator<? extends URL> it = roots.iterator();
        if (!it.hasNext()) {
            return this;
        }
        if (rootsChanged.isEmpty()) {
            rootsChanged = new HashSet<URL>(11);
        }
        while (it.hasNext()) {
            rootsChanged.add(it.next());
        }
        return this;
    }

    public synchronized LogContext addFileObjects(Collection<FileObject> files) {
        if (files == null || files.isEmpty()) {
            return this;
        }
        if (fileObjsChanged.isEmpty()) {
            fileObjsChanged = new HashSet<URI>(files.size());
        }
        for (FileObject file : files) {
            fileObjsChanged.add(file.toURI());
        }
        return this;
    }

    public synchronized LogContext addFiles(Collection<? extends URL> files) {
        if (files == null || files.isEmpty()) {
            return this;
        }
        if (filesChanged.isEmpty()) {
            filesChanged = new HashSet<URL>(files.size());
        }
        filesChanged.addAll(files);
        return this;
    }

    private LogContext(
        @NonNull final EventType eventType,
        @NonNull final StackTraceElement[] stackTrace,
        @NullAllowed final String message,
        @NullAllowed final LogContext parent) {
        Parameters.notNull("eventType", eventType);     //NOI18N
        Parameters.notNull("stackTrace", stackTrace);   //NOI18N
        this.eventType = eventType;
        this.stackTrace = stackTrace;
        this.message = message;
        this.parent = parent;
        this.timestamp = System.currentTimeMillis();
    }

    private synchronized void createLogMessage(@NonNull final StringBuilder sb) {
        sb.append("Type:").append(eventType);   //NOI18N
        if (message != null) {
            sb.append(" Description:").append(message); //NOI18N
        }
        sb.append("\nTime scheduled: ").append(new Date(timestamp));
        if (executed > 0) {
            sb.append("\nTime executed: ").append(new Date(executed));
        } else {
            sb.append("\nNOT executed");
        }
        sb.append("\nScanned roots: ").append(scannedSourceRoots).
                append("\n, total time: ").append(totalScanningTime);
        
        sb.append("\nCurrent root(s): ").append(allCurrentRoots.values());
        
        sb.append("\nTime spent in indexers:");
        List<String> iNames = new ArrayList<String>(totalIndexerTime.keySet());
        Collections.sort(iNames);
        for (Map.Entry<String, Long> indexTime : totalIndexerTime.entrySet()) {
            sb.append("\n\t").append(indexTime.getKey()).
                    append(": ").append(indexTime.getValue());
        }
        
        sb.append("\nStacktrace:\n");    //NOI18N
        for (StackTraceElement se : stackTrace) {
            sb.append('\t').append(se).append('\n'); //NOI18N
        }
        if (root != null) {
            sb.append("On root: ").append(root).append("\n");
        }
        if (!this.rootsChanged.isEmpty()) {
            sb.append("Changed CP roots: ").append(rootsChanged).append("\n");
        }
        if (!this.classPathsChanged.isEmpty()) {
            sb.append("Changed ClassPaths:").append(classPathsChanged).append("\n");
        }
        if (!this.filesChanged.isEmpty()) {
            sb.append("Changed files(URL): ").append(filesChanged.toString().replace(",", "\n\t")).append("\n");
        }
        
        if (!this.fileObjsChanged.isEmpty()) {            
            sb.append("Changed files(FO): ");
            for (URI uri : this.fileObjsChanged) {
                String name;
                try {
                    final File f = Utilities.toFile(uri);
                    name = f.getAbsolutePath();
                } catch (IllegalArgumentException iae) {
                    name = uri.toString();
                }
                sb.append(name).append("\n\t");
            }
            sb.append("\n");
        }
        if (!this.filePathsChanged.isEmpty()) {
            sb.append("Changed files(Str): ").append(filePathsChanged.toString().replace(",", "\n\t")).append("\n");
        }
        if (parent != null) {
            sb.append("Parent {");  //NOI18N
            parent.createLogMessage(sb);
            sb.append("}\n"); //NOI18N
        }
        
        if (threadDump != null) {
            sb.append("Thread dump:\n").append(threadDump).append("\n");
        }
        if (secondDump != null) {
            sb.append("Thread dump #2 (after ").
                    append(SECOND_DUMP_DELAY / 1000).
                    append(" seconds):\n").
                    append(secondDump).append("\n");
        }

        if (absorbed != null) {
            sb.append("Absorbed {");    //NOI18N
            for (LogContext a : absorbed) {
                a.createLogMessage(sb);
            }
            sb.append("}\n");             //NOI18N
        }
        
    }

    private static final Logger LOG = Logger.getLogger(LogContext.class.getName());
    private static final String LOG_MESSAGE = "SCAN_CANCELLED"; //NOI18N
    private static final String LOG_EXCEEDS_RATE = "SCAN_EXCEEDS_RATE {0}"; //NOI18N
    
    /**
     * Ring buffer that saves times and LogContexts for some past minutes.
     */
    private static class RingTimeBuffer {
        private static final int INITIAL_RINGBUFFER_SIZE = 20;
        /**
         * time limit to keep history, in minutes
         */
        private int historyLimit;
        
        /**
         * Ring buffer of timestamps
         */
        private long[]          times = new long[INITIAL_RINGBUFFER_SIZE];
        
        /**
         * LogContexts, at the same indexes as their timestamps
         */
        private LogContext[]    contexts = new LogContext[INITIAL_RINGBUFFER_SIZE];
        
        /**
         * Start = start of the data. Limit = just beyond of the data.
         * limit == start => empty buffer. Data is stored from start to the limit
         * modulo buffer sie.
         */
        private int start, limit;
        
        /**
         * index just beyond the last reported LogContext, -1 if nothing was
         * reported - will be printed from start/found position
         */
        private int reportedEnd = -1;
        
        /**
         * Timestamp of the last mark in the ringbuffer; for LRU expiration.
         */
        private long lastTime;

        public RingTimeBuffer(int historyLimit) {
            this.historyLimit = historyLimit;
        }
        
        /**
         * Advances start, dicards entries older that historyLimit.
         * @param now 
         */
        private void updateStart(long now) {
            long from = now - fromMinutes(historyLimit);
            
            while (!isEmpty() && times[start] < from) {
                // free for GC
                contexts[start] = null;
                
                if (reportedEnd == start) {
                    reportedEnd = -1;
                }
                start = inc(start);
            };
        }
        
        /**
         * ensures some minimum space is available; if gap reaches zero,
         * doubles the buffer size.
         */
        private void ensureSpaceAvailable() {
            if (!isEmpty() && gapSize() == 0) {
                long[] times2 = new long[times.length * 2];
                LogContext[] contexts2 = new LogContext[times.length * 2];
                
                int l;
                if (limit >= start) {
                    System.arraycopy(times, start, times2, 0, limit - start);
                    System.arraycopy(contexts, start, contexts2, 0, limit - start);
                    l = limit - start;
                } else {
                    // limit < start, end-of-array in the middle:
                    System.arraycopy(times, start, times2, 0, times.length - start);
                    System.arraycopy(times, 0, times2, times.length - start, limit);

                    System.arraycopy(contexts, start, contexts2, 0, times.length - start);
                    System.arraycopy(contexts, 0, contexts2, times.length - start, limit);

                    l = limit + (times.length - start);
                }
                limit = l;
                start = 0;

                this.times = times2;
                this.contexts = contexts2;
            }
        }
        
        /**
         * Adds LogContext to the ring buffer. Reports excess mark rate.
         * @param ctx 
         */
        public void mark(LogContext ctx) {
            long l = System.currentTimeMillis();
            updateStart(l);
            ensureSpaceAvailable();
            times[limit] = l;
            contexts[limit] = ctx;
            limit = inc(limit);
            
            EventType type = ctx.eventType;
            checkAndReport(l, type.getMinutes(), type.getTreshold());
            
            lastTime = l;
        }
        
        private int inc(int i) {
            return (i + 1) % ringSize();
        }
        
        private int ringSize() {
            return times.length;
        }
        
        private boolean isEmpty() {
            return start == limit;
        }
        
        private int gapSize() {
            if (start > limit) {
                return start - limit;
            } else {
                return start + ringSize() - limit;
            }
        }
        
        private int dataSize(int start, int end) {
            if (start < end) {
                return end - start;
            } else {
                return end + ringSize() - start;
            }
        }
        
        private Pair<Integer, Integer> findHigherRate(long minTime, int minutes, int treshold) {
            int s = start;
            int l = -1;
            
            // skip events earlier than history limit; should be already cleared.
            while (s != limit && times[s] < minTime) {
                s = inc(s);
                if (s == l) {
                    l = -1;
                }
            }
            
            long minDiff = fromMinutes(minutes);
            do {
                if (s == limit) {
                    // end of data reached
                    return null;
                }
                // end of previous range reached, or even passed, reset range.
                if (l == -1) {
                    l = s;
                }

                long t = times[s];
                while (l != limit && (times[l] - t) < minDiff) {
                    l = inc(l);
                }
                if (dataSize(s, l) > treshold) {
                    return Pair.<Integer, Integer>of(s, l);
                }
                // move start, since nothing interesting has been found from the previous
                // start up to 's' including.
                start = s = inc(s);
            } while (l != limit);
            return null;
        }
        
        void checkAndReport(long now, int minutes, int treshold) {
            long minTime = now - fromMinutes(historyLimit);
            Pair<Integer, Integer> found = findHigherRate(minTime, minutes, treshold);
            if (found == null) {
                return;
            }
            
            LOG.log(Level.WARNING, "Excessive indexing rate detected. Dumping suspicious contexts");
            int index;
            
            for (index = found.first; index != found.second; index = (index + 1) % times.length) {
                contexts[index].log(false);
            }
            this.reportedEnd = index;
        }
    }
    
    static class Stats {
        /**
         * For each possible event, one ring-buffer of LogContexts.
         */
        private Map<EventType, RingTimeBuffer>  history = new HashMap<EventType, RingTimeBuffer>(7);
        
        /**
         * For each root, one ring-buffer. Items are removed using least recently accessed strategy. Once an
         * item is touched, it is removed and re-added so it is at the tail of the entry iterator.
         */
        private LinkedHashMap<URL, RingTimeBuffer> rootHistory = new LinkedHashMap<URL, RingTimeBuffer>(9, 0.7f, true);
        
        public synchronized void record(LogContext ctx) {
            EventType type = ctx.eventType;
            
            if (type == EventType.INDEXER && ctx.root != null) {
                recordIndexer(ctx.root, ctx);
            } else {
                recordRegular(type, ctx);
            }
        }
        
        private void expireRoots() {
            long l = System.currentTimeMillis();
            l -= fromMinutes(EventType.INDEXER.getMinutes());
            
            for (Iterator<RingTimeBuffer> it = rootHistory.values().iterator(); it.hasNext(); ) {
                RingTimeBuffer rb = it.next();
                if (rb.lastTime < l) {
                    it.remove();
                } else {
                    break;
                }
            }
        }
        
        private void recordIndexer(URL root, LogContext ctx) {
            expireRoots();
            RingTimeBuffer existing = rootHistory.get(root);
            if (existing == null) {
                existing = new RingTimeBuffer(EventType.INDEXER.getMinutes() * 2);
                rootHistory.put(root, existing);
            }
            existing.mark(ctx);
        }
        
        private void recordRegular(EventType type, LogContext ctx) {
            
            RingTimeBuffer buf = history.get(type);
            if (buf == null) {
                buf = new RingTimeBuffer(type.getMinutes() * 2);
                history.put(type, buf);
            }
            
            buf.mark(ctx);
        }
    }

    private static long fromMinutes(int mins) {
        return mins * 60 * 1000;
    }
        
    private static final Stats STATS = new Stats();

}
