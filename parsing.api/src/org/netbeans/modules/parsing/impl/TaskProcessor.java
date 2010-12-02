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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.modules.parsing.impl.indexing.Util;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public class TaskProcessor {
    
    private static final Logger LOGGER = Logger.getLogger(TaskProcessor.class.getName());
    
    /**Limit for task to be marked as a slow one, in ms*/
    private static final int SLOW_CANCEL_LIMIT = 50;
    
    /** Default reparse delay*/
    private static final int DEFAULT_REPARSE_DELAY = 500;
    
    /**May be changed by unit test*/
    public static int reparseDelay = DEFAULT_REPARSE_DELAY;
    
    //Scheduled requests waiting for execution
    private final static PriorityBlockingQueue<Request> requests = new PriorityBlockingQueue<Request> (10, new RequestPriorityComparator());
    //Finished requests waiting on reschedule by some scheduler or parser
    private final static Map<Source,Collection<Request>> finishedRequests = new WeakHashMap<Source,Collection<Request>>();    
    //Tasks which are scheduled (not yet executed) but blocked by expected event (waiting for event)
    private final static Map<Source,Collection<Request>> waitingRequests = new WeakHashMap<Source,Collection<Request>>();    
    //Tasked which should be cleared from requests or finieshedRequests
    private final static Collection<RemovedTask> toRemove = new LinkedList<RemovedTask> ();
    
    //Worker thread factory - single worker thread
    final static WorkerThreadFactory factory = new WorkerThreadFactory ();
    //Currently running SchedulerTask
    private final static CurrentRequestReference currentRequest = new CurrentRequestReference ();
    //Deferred task until scan is done
    private final static List<DeferredTask> todo = Collections.synchronizedList(new LinkedList<DeferredTask>());
                    
    //Internal lock used to synchronize parsing api iternal state (TaskProcessor, Source, SourceCache)
    private static class InternalLock {};    
    public static final Object INTERNAL_LOCK = new InternalLock ();
    
    
    //Parser lock used to prevent other tasks to run in case when there is an active task
    private final static ReentrantLock parserLock = new ReentrantLock (true);
    private static int lockCount = 0;
    
    //Regexp of class names of tasks which shouldn't be scheduled - used for debugging & performance testing
    private static final Pattern excludedTasks;
    //Regexp of class names of tasks which should be scheduled - used for debugging & performance testing
    private static final Pattern includedTasks;
    //Already logged warninig about running in AWT
    private static final Set<StackTraceElement> warnedAboutRunInEQ = new HashSet<StackTraceElement>();
    
    static {
        Executors.newSingleThreadExecutor(factory).submit (new CompilationJob());
        //Initialize the excludedTasks
        Pattern _excludedTasks = null;
        try {
            String excludedValue= System.getProperty("org.netbeans.modules.parsing.impl.Source.excludedTasks");      //NOI18N
            if (excludedValue != null) {
                _excludedTasks = Pattern.compile(excludedValue);
            }
        } catch (PatternSyntaxException e) {
            Exceptions.printStackTrace(e);
        }
        excludedTasks = _excludedTasks;
        Pattern _includedTasks = null;
        try {
            String includedValue= System.getProperty("org.netbeans.modules.parsing.impl.Source.includedTasks");      //NOI18N
            if (includedValue != null) {
                _includedTasks = Pattern.compile(includedValue);
            }
        } catch (PatternSyntaxException e) {
            Exceptions.printStackTrace(e);
        }
        includedTasks = _includedTasks;
    }
        
    public static void runUserTask (final Mutex.ExceptionAction<Void> task, final Collection<Source> sources) throws ParseException {
        Parameters.notNull("task", task);
        //tzezula: ugly, Hanzy isn't here a nicer solution to distinguish single source from multi source?
        if (sources.size() == 1) {
            SourceAccessor.getINSTANCE().assignListeners(sources.iterator().next());
        }
        boolean a = false;
        assert a = true;
        if (a && javax.swing.SwingUtilities.isEventDispatchThread()) {
            StackTraceElement stackTraceElement = Util.findCaller(Thread.currentThread().getStackTrace(), TaskProcessor.class, ParserManager.class, 
                    "org.netbeans.api.java.source.JavaSource", //NOI18N
                    "org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper"); //NOI18N
            if (stackTraceElement != null && warnedAboutRunInEQ.add(stackTraceElement)) {
                LOGGER.log(Level.WARNING, "ParserManager.parse called in AWT event thread by: {0}", stackTraceElement); // NOI18N
            }
        }
        final Request request = currentRequest.getTaskToCancel();
        try {
            if (request != null) {
                request.task.cancel();
            }            
            parserLock.lock();
            try {
                if (lockCount < 1) {
                    for (Source source : sources) {
                        SourceAccessor.getINSTANCE ().invalidate(source,false);
                    }
                }
                lockCount++;
                Utilities.runPriorityIO(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        task.run ();
                        return null;
                    }
                });
            } catch (final Exception e) {
                final ParseException ioe = new ParseException ();
                ioe.initCause(e);
                throw ioe;
            } finally {                    
                lockCount--;
                parserLock.unlock();
            }
        } finally {
            currentRequest.cancelCompleted (request);
        }        
    }

    public static Future<Void> runWhenScanFinished (final Mutex.ExceptionAction<Void> task, final Collection<Source> sources) throws ParseException {
        assert task != null;
        final ScanSync sync = new ScanSync (task);
        final DeferredTask r = new DeferredTask (sources,task,sync);
        //0) Add speculatively task to be performed at the end of background scan
        todo.add (r);
        if (Utilities.isScanInProgress()) {
            return sync;
        }
        //1) Try to aquire javac lock, if successfull no task is running
        //   perform the given taks synchronously if it wasn't already performed
        //   by background scan.
        final boolean locked = parserLock.tryLock();
        if (locked) {
            try {
                if (todo.remove(r)) {
                    try {
                        runUserTask(task, sources);
                    } finally {
                        sync.taskFinished();
                    }
                }
            } finally {
                parserLock.unlock();
            }
        }
        else {
            //Otherwise interrupt currently running task and try to aquire lock
            do {
                final Request[] request = new Request[1];
                boolean isScanner = currentRequest.getTaskToCancel(request);
                try {
                    if (isScanner) {
                        return sync;
                    }
                    if (request[0] != null) {
                        request[0].task.cancel();
                    }
                    if (parserLock.tryLock(100, TimeUnit.MILLISECONDS)) {
                        try {
                            if (todo.remove(r)) {
                                try {
                                    runUserTask(task,sources);
                                    return sync;
                                } finally {
                                    sync.taskFinished();
                                }
                            }
                            else {
                                return sync;
                            }
                        } finally {
                            parserLock.unlock();
                        }
                    }
                } catch (InterruptedException e) {
                    throw new ParseException ("Interupted.",e); //NOI18N
                }
                finally {
                    if (!isScanner) {
                        currentRequest.cancelCompleted(request[0]);
                    }
                }
            } while (true);
        }
        return sync;
    }
    
    /** Adds a task to scheduled requests. The tasks will run sequentially.
     * @see SchedulerTask for information about implementation requirements 
     * @task The task to run.
     * @source The source on which the task operates
     */ 
    public static void addPhaseCompletionTasks(final Collection<SchedulerTask> tasks, final SourceCache cache,
            boolean bridge, Class<? extends Scheduler> schedulerType) {
        addPhaseCompletionTasks(tasks, cache, cache.getSnapshot().getSource(), bridge, schedulerType);
    }

    /**
     * Adds the task, used by addPhaseCompletionTasks and updatePhaseCompletionTask, can be called under
     * INTERNAL_LOCK. The original addPhaseCompletionTasks cannot be called under INTERNAL_LOCK as it
     * calls {@link SourceCache#getSnapshot()} which takes private SourceCache lock.
     * @param tasks
     * @param cache
     * @param source
     * @param bridge
     * @param schedulerType
     */
    private static void addPhaseCompletionTasks(final Collection<SchedulerTask> tasks, final SourceCache cache,
            final Source source, boolean bridge, Class<? extends Scheduler> schedulerType) {
        Parameters.notNull("task", tasks);   //NOI18N
        Parameters.notNull("source", source);   //NOI18N
        Parameters.notNull("cache", cache);   //NOI18N
        List<Request> _requests = new ArrayList<Request> ();
        for (SchedulerTask task : tasks) {
            final String taskClassName = task.getClass().getName();
            if (excludedTasks != null && excludedTasks.matcher(taskClassName).matches()) {
                if (includedTasks == null || !includedTasks.matcher(taskClassName).matches())
                    continue;
            }
            _requests.add (new Request (task, cache, bridge ? ReschedulePolicy.ON_CHANGE : ReschedulePolicy.CANCELED, schedulerType));
        }
        if (!_requests.isEmpty ()) {
            handleAddRequests (source, _requests);
        }
    }
    
    
    /**
     * Removes a task from scheduled requests.
     * @param task The task to be removed.
     */
    public static void removePhaseCompletionTasks(final Collection<? extends SchedulerTask> tasks, final Source source) {
        Parameters.notNull("task", tasks);
        Parameters.notNull("source", source);
        synchronized (INTERNAL_LOCK) {
            Collection<Request> frqs = finishedRequests.get(source);
            Collection<Request> wrqs = waitingRequests.get(source);
            for (SchedulerTask task : tasks) {
                boolean found = false;
                //Ignore excluded tasks
                final String taskClassName = task.getClass().getName();
                if (excludedTasks != null && excludedTasks.matcher(taskClassName).matches()) {
                    if (includedTasks == null || !includedTasks.matcher(taskClassName).matches()) {
                        continue;
                    }
                }
                //First) Try to find it in finished tasks
                if (frqs != null) {
                    for (Iterator<Request> it = frqs.iterator(); it.hasNext(); ) {
                        final Request rq = it.next();
                        if (rq.task == task && rq.cache != null && rq.cache.getSource() == source) {
                            it.remove();
                            found = true;
//                      break; todo: Some tasks are duplicated (racecondition?), remove even them, Prevent duplication of tasks
                        }
                    }
                    if (frqs.isEmpty()) {
                        finishedRequests.remove(source);
                        frqs = null;
                    }
                }
                //Sencond) Try to find it among started task
                for (Iterator<Request> it = requests.iterator(); it.hasNext();) {
                    final Request rq = it.next();
                    if (rq.task == task && rq.cache != null && rq.cache.getSource() == source) {
                        it.remove();
                        found = true;
                    }
                }
                //Third) Try to find in among waiting tasks
                if (wrqs != null) {
                    for (Iterator<Request> it = wrqs.iterator(); it.hasNext(); ) {
                        final Request rq = it.next();
                        if (rq.task == task && rq.cache != null && rq.cache.getSource() == source) {
                            it.remove();
                            found = true;
                        }
                    }
                    if (wrqs.isEmpty()) {
                        waitingRequests.remove(source);
                        wrqs = null;
                    }
                }
                
                //Fourh) Not found either due to weak consistency (1 task) or removing task which is not added
                if (!found) {
                    toRemove.add (new RemovedTask(source,task));
                    // there was a modification in toRemove, wake up the thread
                    requests.add(Request.NONE);
                }
                SourceAccessor.getINSTANCE().taskRemoved(source);
            }
        }
    }
    
    /**
     * Reschedules the task in case it was already executed.
     * Does nothing if the task was not yet executed.
     * @param task to reschedule
     * @param source to which the task it bound
     */
    public static void rescheduleTasks(final Collection<SchedulerTask> tasks, final Source source, final Class<? extends Scheduler> schedulerType) {
        Parameters.notNull("task", tasks);
        Parameters.notNull("source", source);
        synchronized (INTERNAL_LOCK) {
            final Request request = currentRequest.getTaskToCancel (tasks);
            try {
                final Collection<Request> cr = finishedRequests.get(source);
                if (cr != null) {
                    for (SchedulerTask task : tasks) {
                        if (request == null || request.task != task) {
                            List<Request> aRequests = new ArrayList<Request> ();
                            for (Iterator<Request> it = cr.iterator(); it.hasNext();) {
                                Request fr = it.next();                                
                                if (task == fr.task) {
                                    it.remove();
                                    assert fr.reschedule == ReschedulePolicy.ON_CHANGE;
                                    fr.schedulerType = schedulerType;
                                    aRequests.add(fr);
                                    if (cr.isEmpty()) {
                                        finishedRequests.remove(source);
                                    }
                                    break;
                                }
                            }
                            requests.addAll (aRequests);
                        }
                    }
                }
            } finally {
                if (request != null) {
                    currentRequest.cancelCompleted(request);
                }
            }
        }
    }

    public static void updatePhaseCompletionTask (final Collection<SchedulerTask>add, final Collection<SchedulerTask>remove,
            final Source source, SourceCache cache, Class<? extends Scheduler> schedulerType) {
        Parameters.notNull("add", add);
        Parameters.notNull("remove", remove);
        Parameters.notNull("source", source);
        Parameters.notNull("cache", cache);
       // Parameters.notNull("schedulerType", schedulerType);
        synchronized (INTERNAL_LOCK) {
            removePhaseCompletionTasks(remove, source);
            addPhaseCompletionTasks(add, cache, source, false, schedulerType);
        }
    }
    
    //Changes handling
    
    private final static AtomicReference<Request> rst = new AtomicReference<Request>();
    
    //DO NOT CALL DIRECTLY - called by Source
    public static Request resetState (final Source source,
            final boolean mayInterruptParser,
            final boolean sync) {
        assert source != null;
        TaskProcessor.Request r = currentRequest.getTaskToCancel (mayInterruptParser);
        if (r != null) {
            try {
                r.task.cancel();
            } finally {
                if (sync) {
                    Request oldR = rst.getAndSet(r);
                    assert oldR == null;
                }
            }
        }
        return r;
    }
    
    //DO NOT CALL DIRECTLY - called by EventSupport
    public static void resetStateImpl (final Source source) {
        final Request r = rst.getAndSet(null);
        currentRequest.cancelCompleted(r);
        if (source != null) {
            synchronized (INTERNAL_LOCK) {
                final boolean reschedule = SourceAccessor.getINSTANCE().testAndCleanFlags(source,SourceFlags.RESCHEDULE_FINISHED_TASKS,
                            EnumSet.of(SourceFlags.RESCHEDULE_FINISHED_TASKS, SourceFlags.CHANGE_EXPECTED));

                Collection<Request> cr;
                if (reschedule) {
                    if ((cr=finishedRequests.remove(source)) != null && cr.size()>0)  {
                        for (Request toAdd : cr) {
                            assert toAdd.reschedule == ReschedulePolicy.ON_CHANGE;
                            requests.add(toAdd);
                        }
                    }
                }
                if ((cr=waitingRequests.remove(source)) != null && cr.size()>0)  {
                    for (Request toAdd : cr) {                        
                        requests.add(toAdd);
                    }
                }
            }
        }
    }
            
    //Package private methods needed by the Utilities accessor
    static void acquireParserLock () {
        parserLock.lock();
    }

    static void releaseParserLock () {
        parserLock.unlock();
    }

    static boolean holdsParserLock () {
        return parserLock.isHeldByCurrentThread();
    }
    
    static void scheduleSpecialTask (final SchedulerTask task) {
        assert task != null;
        final Request rq = new Request(task, null, ReschedulePolicy.NEVER, null);
        handleAddRequests (null,Collections.<Request>singletonList (rq));
    }
    
    
    //Private methods
    private static void handleAddRequests (final Source source, final List<Request> requests) {
        assert requests != null;
        if (requests.isEmpty()) {
            return;
        }
        if (source != null) {
            SourceAccessor.getINSTANCE().assignListeners(source);
        }
        //Issue #102073 - removed running task which is readded is not performed
        int priority = Integer.MAX_VALUE;
        synchronized (INTERNAL_LOCK) {
            for (Request request : requests) {
                if (source != null) {
                    toRemove.remove(new RemovedTask(source, request.task));
                }
                priority = Math.min(priority, request.task.getPriority());
            }
            TaskProcessor.requests.addAll (requests);
        }        
        Request request = currentRequest.getTaskToCancel(priority);
        try {
            if (request != null) {
                request.task.cancel();
            }
        } finally {
            currentRequest.cancelCompleted(request);
        }
    }
    
    /**
     * Checks if the current thread holds a document write lock on some of given files
     * Slow should be used only in assertions
     * @param files to be checked
     * @return true when the current thread holds a edeitor write lock on some of given files
     */
    private static boolean holdsDocumentWriteLock (final Iterable<Source> sources) {
        assert sources != null;
        final Class<AbstractDocument> docClass = AbstractDocument.class;
        try {
            final Method method = docClass.getDeclaredMethod("getCurrentWriter"); //NOI18N
            method.setAccessible(true);
            final Thread currentThread = Thread.currentThread();
            for (Source source : sources) {
                try {
                    Document doc = source.getDocument (true);
                    if (doc instanceof AbstractDocument) {
                        Object result = method.invoke(doc);
                        if (result == currentThread) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }            
            }
        } catch (NoSuchMethodException e) {
            Exceptions.printStackTrace(e);
        }
        return false;
    }

    //Private classes
    /**
     * SchedulerTask scheduler loop
     * Dispatches scheduled tasks from {@link TaskProcessor#requests} and performs
     * them.
     */
     private static class CompilationJob implements Runnable {
        
        @SuppressWarnings ("unchecked") //NOI18N
        @Override
        public void run () {
            try {
                while (true) {                   
                    try {                        
                        final Request r = requests.take();
                        if (r != null && r != Request.NONE) {
                            currentRequest.setCurrentTask(r);
                            try {                            
                                final SourceCache sourceCache = r.cache;
                                if (sourceCache == null) {
                                    assert r.task instanceof ParserResultTask : "Illegal request: EmbeddingProvider has to be bound to Source";     //NOI18N
                                    parserLock.lock ();
                                    try {
                                        try {
                                            if (LOGGER.isLoggable(Level.FINE)) {
                                                LOGGER.log(Level.FINE, "Running Special Task: {0}", r.toString());
                                            }
                                            // needs some description!!!! (tzezula)
                                            ((ParserResultTask) r.task).run (null, null);
                                        } finally {
                                            currentRequest.clearCurrentTask();
                                            boolean cancelled = requests.contains(r);
                                            if (!cancelled) {
                                            DeferredTask[] _todo;
                                                synchronized (todo) {
                                                    _todo = todo.toArray(new DeferredTask[todo.size()]);
                                                    todo.clear();
                                                }
                                                for (DeferredTask rq : _todo) {
                                                    try {
                                                        runUserTask(rq.task, rq.sources);
                                                    } finally {
                                                        rq.sync.taskFinished();
                                                    }
                                                }
                                            }
                                        }
                                    } catch (RuntimeException re) {
                                        Exceptions.printStackTrace(re);
                                    }
                                    finally {
                                        parserLock.unlock();
                                    }
                                } else {                                    
                                    final Source source = sourceCache.getSnapshot ().getSource ();
                                    assert source != null;

                                    boolean reschedule = false;
                                    byte validFlags = 0;
                                    synchronized (INTERNAL_LOCK) {
                                        if (toRemove.contains(new RemovedTask(source,r.task))) {
                                            //No need to perform the task as it's removed
                                            validFlags = 1;
                                        } else if (SourceAccessor.getINSTANCE().testFlag(source,SourceFlags.CHANGE_EXPECTED)) {
                                            validFlags = 2;
                                        }                                        
                                    }
                                    if (validFlags == 0) {
                                        Snapshot snapshot = null;
                                        long[] id = new long[] {-1};
                                        if (SourceAccessor.getINSTANCE().testFlag(source, SourceFlags.INVALID)) {
                                            snapshot = sourceCache.createSnapshot(id);
                                        }
                                        parserLock.lock();                                    
                                        try {
                                            if (SourceAccessor.getINSTANCE ().invalidate(source,id[0],snapshot)) {
                                                lockCount++;
                                                try {
                                                    if (r.task instanceof EmbeddingProvider) {
                                                        sourceCache.refresh ((EmbeddingProvider) r.task, r.schedulerType);
                                                    }
                                                    else {
                                                        currentRequest.setCurrentParser(sourceCache.getParser());
                                                        final Parser.Result currentResult = sourceCache.getResult (r.task);
                                                        if (currentResult != null) {
                                                            try {
                                                                boolean shouldCall = !SourceAccessor.getINSTANCE().testFlag(source, SourceFlags.INVALID);
                                                                if (shouldCall) {
                                                                    try {
                                                                        final long startTime = System.currentTimeMillis();
                                                                        if (r.task instanceof ParserResultTask) {
                                                                            if (LOGGER.isLoggable(Level.FINE)) {
                                                                                LOGGER.log(Level.FINE, "Running Task: {0}", r.toString());
                                                                            }
                                                                            ParserResultTask parserResultTask = (ParserResultTask) r.task;
                                                                            SchedulerEvent schedulerEvent = SourceAccessor.getINSTANCE ().getSchedulerEvent (source, parserResultTask.getSchedulerClass ());
                                                                            parserResultTask.run (currentResult, schedulerEvent);                                                                        
                                                                        }
                                                                        else {
                                                                            assert false : "Unknown task type: " + r.task.getClass();   //NOI18N
                                                                        }
                                                                        final long endTime = System.currentTimeMillis();
                                                                        if (LOGGER.isLoggable(Level.FINEST)) {
                                                                            LOGGER.finest(String.format("Executed task: %s in %d ms.",  //NOI18N
                                                                                r.task.getClass().toString(), (endTime-startTime)));
                                                                        }
                                                                        if (LOGGER.isLoggable(Level.FINER)) {
                                                                            final long cancelTime = currentRequest.getCancelTime();
                                                                            if (cancelTime >= startTime && (endTime - cancelTime) > SLOW_CANCEL_LIMIT) {
                                                                                LOGGER.finer(String.format("Task: %s ignored cancel for %d ms.",  //NOI18N
                                                                                    r.task.getClass().toString(), (endTime-cancelTime)));
                                                                            }
                                                                        }
                                                                    } catch (Exception re) {
                                                                        Exceptions.printStackTrace (re);
                                                                    }
                                                                }
                                                            } finally {
                                                                ParserAccessor.getINSTANCE().invalidate(currentResult);
                                                            }
                                                        }
                                                    }
                                                } finally {
                                                    lockCount--;
                                                }
                                            }
                                            else {
                                                reschedule = true;
                                            }
                                        } finally {                                        
                                            parserLock.unlock();
                                        }
                                    }
                                    //Maybe should be in finally to prevent task lost when parser crashes
                                    if (r.reschedule != ReschedulePolicy.NEVER) {
                                        reschedule |= currentRequest.setCurrentTask(null);
                                        synchronized (INTERNAL_LOCK) {
                                            if (!toRemove.contains(new RemovedTask(source,r.task))) {
                                                if (validFlags == 2) {
                                                    if (SourceAccessor.getINSTANCE().testFlag(source,SourceFlags.CHANGE_EXPECTED)) {
                                                        Collection<Request> rc = waitingRequests.get (source);
                                                        if (rc == null) {
                                                            rc = new LinkedList<Request> ();
                                                            waitingRequests.put (source, rc);
                                                        }
                                                        rc.add(r);
                                                        LOGGER.log(Level.FINE, "Waiting Task: {0}", r.toString());      //NOI18N
                                                    } else {
                                                        requests.add(r);
                                                        LOGGER.log(Level.FINE, "Rescheduling Waiting Task: {0}", r.toString()); //NOI18N
                                                    }
                                                }
                                                else if (reschedule || SourceAccessor.getINSTANCE().testFlag(source, SourceFlags.INVALID)) {
                                                    //The JavaSource was changed or canceled rechedule it now
                                                    requests.add(r);
                                                    LOGGER.log(Level.FINE, "Rescheduling Canceled Task: {0}", r.toString()); //NOI18N
                                                } else if (r.reschedule == ReschedulePolicy.ON_CHANGE) {
                                                    //Up to date JavaSource add it to the finishedRequests
                                                    Collection<Request> rc = finishedRequests.get (r.cache.getSnapshot ().getSource ());
                                                    if (rc == null) {
                                                        rc = new LinkedList<Request> ();
                                                        finishedRequests.put (r.cache.getSnapshot ().getSource (), rc);
                                                    }
                                                    rc.add(r);
                                                    LOGGER.log(Level.FINE, "Finished ON_CHANGE Task: {0}", r.toString()); //NOI18N
                                                }
                                            } else {
                                                LOGGER.log(Level.FINE, "Removing Task: {0}", r.toString()); //NOI18N
                                            }
                                            toRemove.clear();
                                        }
                                   } else {
                                       synchronized (INTERNAL_LOCK) {
                                           if (validFlags == 2 && !toRemove.contains(new RemovedTask(source,r.task))) {
                                               if (SourceAccessor.getINSTANCE().testFlag(source,SourceFlags.CHANGE_EXPECTED)) {
                                                   Collection<Request> rc = waitingRequests.get (source);
                                                    if (rc == null) {
                                                        rc = new LinkedList<Request> ();
                                                        waitingRequests.put (source, rc);
                                                    }
                                                    rc.add(r);
                                                    LOGGER.log(Level.FINE, "Waiting NEVER Task: {0}", r.toString()); //NOI18N
                                               } else {
                                                   requests.add(r);
                                                   LOGGER.log(Level.FINE, "Rescheduling Waiting NEVER Task: {0}", r.toString()); //NOI18N
                                               }
                                           } else {
                                               LOGGER.log(Level.FINE, "Finished NEVER task: {0}", r.toString()); //NOI18N
                                           }
                                           toRemove.clear();
                                       }
                                       SourceAccessor.getINSTANCE().taskRemoved(source);
                                   }
                                }
                            } finally {
                                currentRequest.setCurrentTask(null);                   
                            }
                        } 
                    } catch (Throwable e) {
                        if (e instanceof InterruptedException) {
                            throw (InterruptedException)e;
                        }
                        else if (e instanceof ThreadDeath) {
                            throw (ThreadDeath)e;
                        }
                        else {
                            Exceptions.printStackTrace(e);
                        }
                    }                    
                }
            } catch (InterruptedException ie) {
                Exceptions.printStackTrace(ie);
                // stop the service.
            }
        }                        
    }
     
     private enum ReschedulePolicy {
         NEVER,
         CANCELED,
         ON_CHANGE
     }
    
    /**
     * Request for performing a task on given Source
     */
    //@ThreadSafe
    public static class Request {
        
        static final Request DUMMY = new Request ();
        static final Request NONE = new Request();
        
        private final SchedulerTask task;
        private final SourceCache cache;
        private final ReschedulePolicy reschedule;
        private Class<? extends Scheduler> schedulerType;
        
        /**
         * Creates new Request
         * @param task to be performed
         * @param source on which the task should be performed
         * @param reschedule when true the task is periodic request otherwise one time request
         */
        private Request (final SchedulerTask task, final SourceCache cache, final ReschedulePolicy reschedule,
            Class<? extends Scheduler> schedulerType) {
            assert task != null;
            assert reschedule != null;
            this.task = task;
            this.cache = cache;
            this.reschedule = reschedule;
            this.schedulerType = schedulerType;
        }

        private Request () {
            this (new ParserResultTask(){
                @Override
                public int getPriority() {
                    return 0;
                }
                @Override
                public Class<? extends Scheduler> getSchedulerClass() {
                    return null;
                }
                @Override
                public void cancel() {
                }
                @Override
                public void run(Result result, SchedulerEvent event) {
                }
            },null,ReschedulePolicy.NEVER,null);
        }
        
        public @Override String toString () {            
            if (reschedule != ReschedulePolicy.NEVER) {
                return String.format("Periodic request to perform: %s on: %s",  //NOI18N
                        task == null ? null : task.toString(),
                        cache == null ? null : cache.toString());
            }
            else {
                return String.format("One time request to perform: %s on: %s",  //NOI18N
                        task == null ? null : task.toString(),
                        cache == null ? null : cache.toString());
            }
        }
        
        public @Override int hashCode () {
            return this.task == null ? 0 : this.task.getPriority();
        }
        
        public @Override boolean equals (Object other) {
            if (other instanceof Request) {
                Request otherRequest = (Request) other;
                return reschedule == otherRequest.reschedule
                    && (cache == null ? otherRequest.cache == null : cache.equals (otherRequest.cache))
                    && (task == null ? otherRequest.task == null : task.equals(otherRequest.task));
            }
            else {
                return false;
            }
        }        
    }
    
    /**
     * Comparator of {@link Request}s which oreders them using {@link SchedulerTask#getPriority()}
     */
    //@ThreadSafe
    private static class RequestPriorityComparator implements Comparator<Request> {
        @Override
        public int compare (Request r1, Request r2) {
            assert r1 != null && r2 != null;
            return r1.task.getPriority() - r2.task.getPriority();
        }
    }
    
    /**
     * Single thread factory creating worker thread.
     */
    //@NotThreadSafe
    static class WorkerThreadFactory implements ThreadFactory {
        
        private Thread t;
        
        @Override
        public Thread newThread(Runnable r) {
            assert this.t == null;
            this.t = new Thread(r, "Parsing & Indexing Loop (" + System.getProperty("netbeans.buildnumber") + ")"); //NOI18N
            return this.t;
        }
        /**
         * Checks if the given thread is a worker thread
         * @param t the thread to be checked
         * @return true when the given thread is a worker thread
         */        
        public boolean isDispatchThread (Thread t) {
            assert t != null;
            return this.t == t;
        }
    }
    
    
    /**
     *  Encapsulates current request. May be trasformed into 
     *  JavaSource private static methods, but it may be less readable.
     */
    //@ThreadSafe
    private static final class CurrentRequestReference {                        

        
        private Request reference;
        private Request canceledReference;
        private Parser activeParser;
        private long cancelTime;
        private boolean canceled;
        /**
         * Threading: The CurrentRequestReference has it's own private lock
         * rather than the INTERNAL_LOCK to prevent deadlocks caused by events
         * fired under Document locks. So, it's NOT allowed to call outside this
         * class (to the rest of the parsing api) under the private lock!
         */
        private static class CRRLock {};
        private static final Object CRR_LOCK = new CRRLock();
        
        boolean setCurrentTask (Request reference) throws InterruptedException {
            boolean result = false;
            assert !parserLock.isHeldByCurrentThread();
            assert reference == null || reference.cache == null || !Thread.holdsLock(INTERNAL_LOCK);
            synchronized (CRR_LOCK) {
                while (this.canceledReference!=null) {
                    CRR_LOCK.wait();
                }
                result = this.canceled;
                canceled = false;
                this.cancelTime = 0;
                this.activeParser = null;
                this.reference = reference;
            }
            return result;
        }

        void clearCurrentTask () {
            synchronized (CRR_LOCK) {
                this.reference = null;
            }
        }

        void setCurrentParser (final Parser parser) {
            synchronized (CRR_LOCK) {
                activeParser = parser;
            }
        }
        
        Request getTaskToCancel (final int priority) {
            Request request = null;
            Parser parser = null;
            if (!factory.isDispatchThread(Thread.currentThread())) {
                synchronized (CRR_LOCK) {
                    if (this.reference != null && priority<this.reference.task.getPriority()) {
                        assert this.canceledReference == null;
                        request = this.reference;
                        this.canceledReference = request;
                        this.reference = null;
                        this.canceled = true;
                        this.cancelTime = System.currentTimeMillis();
                        parser = activeParser;
                    }
                }
                if (parser != null) {
                    parser.cancel(Parser.CancelReason.PARSER_RESULT_TASK, null);
                }
            }
            return request;
        }
        
                
        Request getTaskToCancel (final Collection<? extends SchedulerTask> tasks) {
            assert tasks != null;
            Request request = null;
            Parser parser = null;
            if (!factory.isDispatchThread(Thread.currentThread())) {                
                synchronized (CRR_LOCK) {
                    if (this.reference != null && tasks.contains(this.reference.task)) {
                        assert this.canceledReference == null;
                        request = this.reference;
                        this.canceledReference = request;
                        this.reference = null;
                        this.canceled = true;
                        parser = activeParser;
                    }
                }
                if (parser != null) {
                    parser.cancel(Parser.CancelReason.PARSER_RESULT_TASK, null);
                }
            }
            return request;
        }
        
        Request getTaskToCancel () {
            Request request = null;
            Parser parser = null;
            if (!factory.isDispatchThread(Thread.currentThread())) {                
                synchronized (CRR_LOCK) {
                     request = this.reference;
                    if (request != null) {
                        assert this.canceledReference == null;
                        this.canceledReference = request;
                        this.reference = null;
                        this.canceled = true;
                        this.cancelTime = System.currentTimeMillis();
                        parser = activeParser;
                    }
                }
                if (parser != null) {
                    parser.cancel(Parser.CancelReason.USER_TASK, null);
                }
            }
            return request;
        }
        
        Request getTaskToCancel (final boolean mayCancelParser) {
            Request request = null;
            if (!factory.isDispatchThread(Thread.currentThread())) {    
                Parser parser = null;
                synchronized (CRR_LOCK) {
                    if (this.reference != null) {
                        assert this.canceledReference == null;
                        request = this.reference;
                        this.canceledReference = request;
                        this.reference = null;
                        this.canceled = true;
                        //todo: Cancel parser
                        this.cancelTime = System.currentTimeMillis();
                    }
                    else if (canceledReference == null)  {
                        request = Request.DUMMY;
                        this.canceledReference = request;
                        //todo: Cancel parser
                        this.cancelTime = System.currentTimeMillis();
                    }
                    parser = activeParser;
                }
                if (parser != null) {
                    Source src = null;
                    SourceCache sc;
                    if (request != null && (sc = request.cache)!= null) {
                        src = sc.getSnapshot().getSource();
                    }
                    if (src != null) {
                        if (mayCancelParser) {
                            parser.cancel();
                        }
                        parser.cancel(
                            Parser.CancelReason.SOURCE_MODIFICATION_EVENT,
                            SourceAccessor.getINSTANCE().getSourceModificationEvent(src));
                    }
                }
            }
            return request;
        }

        boolean getTaskToCancel (Request[] request) {
            assert request != null;
            assert request.length == 1;
            boolean result = false;
            Parser parser = null;
            if (!factory.isDispatchThread(Thread.currentThread())) {
                synchronized (CRR_LOCK) {
                     request[0] = this.reference;
                    if (request[0] != null) {
                        result = request[0].cache == null;
                        assert this.canceledReference == null;
                        if (!result) {
                            this.canceledReference = request[0];
                            this.reference = null;
                        }
                        this.canceled = result;
                        this.cancelTime = System.currentTimeMillis();
                        parser = activeParser;
                    }
                }
                if (parser != null) {
                    parser.cancel(Parser.CancelReason.USER_TASK, null);
                }
            }
            return result;
        }
                        
        boolean isCanceled () {
            synchronized (CRR_LOCK) {
                return this.canceled;
            }
        }                
        
        long getCancelTime () {
            synchronized (CRR_LOCK) {
                return this.cancelTime;
            }
        }
        
        void cancelCompleted (final Request request) {
            if (request != null) {
                synchronized (CRR_LOCK) {
                    assert request == this.canceledReference;
                    this.canceledReference = null;
                    CRR_LOCK.notify();
                }
            }
        }
    }

    final static class ScanSync implements Future<Void> {

        private Mutex.ExceptionAction<Void> task;
        private final CountDownLatch sync;
        private final AtomicBoolean canceled;

        public ScanSync (final Mutex.ExceptionAction<Void> task) {
            assert task != null;
            this.task = task;
            this.sync = new CountDownLatch (1);
            this.canceled = new AtomicBoolean (false);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (this.sync.getCount() == 0) {
                return false;
            }
            synchronized (todo) {
                boolean _canceled = canceled.getAndSet(true);
                if (!_canceled) {
                    for (Iterator<DeferredTask> it = todo.iterator(); it.hasNext();) {
                        DeferredTask t = it.next();
                        if (t.task == this.task) {
                            it.remove();
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public boolean isCancelled() {
            return this.canceled.get();
        }

        @Override
        public synchronized boolean isDone() {
            return this.sync.getCount() == 0;
        }

        @Override
        public Void get() throws InterruptedException, ExecutionException {
            checkCaller();
            this.sync.await();
            return null;
        }

        @Override
        public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            checkCaller();
            this.sync.await(timeout, unit);
            return null;
        }

        private void taskFinished () {
            this.sync.countDown();
        }

        private void checkCaller() {
            if (RepositoryUpdater.getDefault().isProtectedModeOwner(Thread.currentThread())) {
                throw new IllegalStateException("ScanSync.get called by protected mode owner.");    //NOI18N
            }
        }

    }

    static final class DeferredTask {
        final Collection<Source> sources;
        final Mutex.ExceptionAction<Void> task;
        final ScanSync sync;

        public DeferredTask (final Collection<Source> sources,
                final Mutex.ExceptionAction<Void> task,
                final ScanSync sync) {
            assert sources != null;
            assert task != null;
            assert sync != null;

            this.sources = sources;
            this.task = task;
            this.sync = sync;
        }
    }
    
    static final class RemovedTask extends WeakReference<Source> implements Runnable {
        
        private final SchedulerTask task;
        
        public RemovedTask(final @NonNull Source src, final @NonNull SchedulerTask task) {
            super (src, org.openide.util.Utilities.activeReferenceQueue());
            Parameters.notNull("src", src);     //NOI18N
            Parameters.notNull("task", task);   //NOI18N
            this.task = task;            
        }
        
        @Override
        public boolean equals(final Object other) {
            if (!(other instanceof RemovedTask)) {
                return false;
            }
            final RemovedTask otherRt = (RemovedTask) other;
            final Source thisSrc = get();
            final Source otherSrc = otherRt.get();
            return (thisSrc == null ? otherSrc == null : thisSrc.equals(otherSrc)) &&
                (this.task.equals(otherRt.task));
        }
        
        @Override
        public int hashCode() {
            return task.hashCode();
        }
        
        @Override
        public String toString() {
            return String.format("RemovedTask[%s, %s]", get(), task);   //NOI18N
        }

        @Override
        public void run() {
            synchronized (INTERNAL_LOCK) {
                for (Iterator<RemovedTask> it = toRemove.iterator(); it.hasNext(); ) {
                    final RemovedTask rt = it.next();
                    if (rt == this) {
                        it.remove();
                        break;
                    }
                }
            }
        }                
    }
}
