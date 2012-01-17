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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.text.StyledDocument;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.editor.plain.PlainKit;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.event.EventSupport;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdaterTestSupport;
import org.netbeans.modules.parsing.impl.indexing.Util;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public class TaskProcessorTest extends NbTestCase {
    
    public TaskProcessorTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {        
        super.setUp();
        clearWorkDir();
    }
    
    public void testWarningWhenRunUserTaskCalledFromAWT() throws Exception {
        this.clearWorkDir();
        final File _wd = this.getWorkDir();
        final FileObject wd = FileUtil.toFileObject(_wd);

        FileUtil.setMIMEType("foo", "text/foo");
        final FileObject foo = wd.createData("file.foo");
        final LogRecord[] warning = new LogRecord[1];
        final String msgTemplate = "ParserManager.parse called in AWT event thread by: {0}";  //NOI18N

        MockMimeLookup.setInstances(MimePath.parse("text/foo"), new FooParserFactory());
        Logger.getLogger(TaskProcessor.class.getName()).addHandler(new Handler() {
            public @Override void publish(LogRecord record) {
                if (record.getMessage().startsWith(msgTemplate)) {
                    warning[0] = record;
                }
            }

            public @Override void flush() {
            }

            public @Override void close() throws SecurityException {
            }
        });

        final StackTraceUserTask stackTraceUserTask = new StackTraceUserTask();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    ParserManager.parse(Collections.singleton(Source.create(foo)), stackTraceUserTask);
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        assertNotNull("No warning when calling ParserManager.parse from AWT", warning[0]);
        assertEquals("Wrong message", msgTemplate, warning[0].getMessage());
        assertEquals("Suspiciosly wrong warning message (is the caller identified correctly?)", stackTraceUserTask.caller, warning[0].getParameters()[0]);
    }
    
    public void testDeadlock() throws Exception {
        FileUtil.setMIMEType("foo", "text/foo");
        MockMimeLookup.setInstances(MimePath.parse("text/foo"), new FooParserFactory(), new PlainKit());
        MockMimeLookup.setInstances(MimePath.parse("text/plain"), new FooParserFactory(), new PlainKit());
        final File workingDir = getWorkDir();        
        final FileObject file = FileUtil.createData(new File(workingDir,"test.foo"));
        final Source src = Source.create(file);
        final DataObject dobj = DataObject.find(file);
        final EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
        final StyledDocument doc = ec.openDocument();
        final CountDownLatch start_a = new CountDownLatch(1);
        final CountDownLatch start_b = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(1);
        final CountDownLatch taskEnded = new CountDownLatch(1);
        final Collection<SchedulerTask> tasks = Collections.<SchedulerTask>singleton(
            new ParserResultTask<Parser.Result>() {
                @Override
                public void run(Result result, SchedulerEvent event) {
                    taskEnded.countDown();
                }

                @Override
                public int getPriority() {
                    return 1000;
                }

                @Override
                public Class<? extends Scheduler> getSchedulerClass() {
                    return null;
                }

                @Override
                public void cancel() {
                }                    
            });
        TaskProcessor.addPhaseCompletionTasks(
                tasks,
                SourceAccessor.getINSTANCE().getCache(src),
                true,
                null);
        taskEnded.await();
        final Thread t = new Thread () {
            @Override
            public void run() {
                NbDocument.runAtomic(doc, new Runnable() {
                    @Override
                    public void run() {
                        start_a.countDown();
                        try {
                            start_b.await();
                            synchronized(TaskProcessor.INTERNAL_LOCK) {
                                end.await();
                            }
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });                    
            }
        };
        t.start();        
        synchronized(TaskProcessor.INTERNAL_LOCK) {
            start_b.countDown();
            start_a.await();
            SourceAccessor.getINSTANCE().getCache(src).invalidate();
            TaskProcessor.removePhaseCompletionTasks(tasks, src);
        }
        end.countDown();
    }

    public void testCancelCall () {                
        final FooTask task = new FooTask();
        final FooParser parser = new FooParser();
        boolean success = false;
        try {
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                TaskProcessor.cancelTask(task, Parser.CancelReason.USER_TASK);
            }
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling cancelTask under INTERNAL_LOCK", success);       //NOI18N

        success = false;
        try {
            TaskProcessor.cancelTask(task, Parser.CancelReason.USER_TASK);
            success = true;
        } catch (AssertionError ae) {}
        assertTrue("AssertionError not expected when calling cancelTask without INTERNAL_LOCK", success); //NOI18N
        assertEquals(1, task.cancelCount);

        success = false;
        try {
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                TaskProcessor.cancelParser(parser, true, Parser.CancelReason.USER_TASK, null);
            }
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling cancelParser under INTERNAL_LOCK", success);       //NOI18N

        success = false;
        try {
            TaskProcessor.cancelParser(parser, true, Parser.CancelReason.USER_TASK, null);
            success = true;
        } catch (AssertionError ae) {}
        assertTrue("AssertionError not expected when calling cancelParser without INTERNAL_LOCK", success); //NOI18N
        assertEquals(1, parser.cancelCount);
    }

    public void testTaskCall () throws Exception {
        FileUtil.setMIMEType("foo", "text/foo");
        MockMimeLookup.setInstances(MimePath.parse("text/foo"), new FooParserFactory(), new PlainKit());
        final File workingDir = getWorkDir();
        final FileObject file = FileUtil.createData(new File(workingDir,"test.foo"));
        final Source src = Source.create(file);

        final FooTask task = new FooTask();
        final FooUserTask userTask = new FooUserTask();
        final FooEmbeddingProvider embProv = new FooEmbeddingProvider();
        final Parser.Result result = new FooParserResult(src.createSnapshot());
        final ResultIterator[] it = new ResultIterator[1];
        ParserManager.parse(Collections.singleton(src), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                it[0] = resultIterator;
            }
        });

        boolean success = false;
        try {
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                TaskProcessor.callParserResultTask(task, result, null);
            }
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling callParserResultTask under INTERNAL_LOCK", success);       //NOI18N

        success = false;
        try {
            TaskProcessor.callParserResultTask(task, result, null);
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling callParserResultTask without parser lock", success); //NOI18N

        success = false;
        try {
            Utilities.acquireParserLock();
            try {
                TaskProcessor.callParserResultTask(task, result, null);
                success = true;
            } finally {
                Utilities.releaseParserLock();
            }
        } catch (AssertionError ae) {}
        assertTrue("AssertionError not expected when calling callParserResultTask with parser lock", success); //NOI18N
        assertEquals(1, task.runCount);


        success = false;
        try {
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                TaskProcessor.callEmbeddingProvider(embProv, src.createSnapshot());
            }
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling callEmbeddingProvider under INTERNAL_LOCK", success);       //NOI18N

        success = false;
        try {
            TaskProcessor.callEmbeddingProvider(embProv, src.createSnapshot());
            success = true;
        } catch (AssertionError ae) {}
        assertTrue("AssertionError not expected when calling callEmbeddingProvider without INTERNAL_LOCK", success); //NOI18N
        assertEquals(1, embProv.runCount);


        success = false;
        try {
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                TaskProcessor.callUserTask(userTask, it[0]);
            }
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling callUserTask under INTERNAL_LOCK", success);       //NOI18N

        success = false;
        try {
            TaskProcessor.callUserTask(userTask, it[0]);
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling callUserTask without parser lock", success); //NOI18N

        success = false;
        try {
            Utilities.acquireParserLock();
            try {
                TaskProcessor.callUserTask(userTask, it[0]);
                success = true;
            } finally {
                Utilities.releaseParserLock();
            }
        } catch (AssertionError ae) {}
        assertTrue("AssertionError not expected when calling callUserTask with parser lock", success); //NOI18N
        assertEquals(1, userTask.runCount);
        
    }

    public void testParserCall () throws Exception {
        FileUtil.setMIMEType("foo", "text/foo");
        MockMimeLookup.setInstances(MimePath.parse("text/foo"), new FooParserFactory(), new PlainKit());
        final File workingDir = getWorkDir();
        final FileObject file = FileUtil.createData(new File(workingDir,"test.foo"));
        final Source src = Source.create(file);
        final FooParser parser = new FooParser();
        final FooTask task = new FooTask();

        boolean success = false;
        try {
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                TaskProcessor.callParse(parser, src.createSnapshot(), task, null);
            }
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling callParse under INTERNAL_LOCK", success);       //NOI18N

        success = false;
        try {
            TaskProcessor.callParse(parser, src.createSnapshot(), task, null);
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling callParse without parser lock", success); //NOI18N

        success = false;
        try {
            Utilities.acquireParserLock();
            try {
                TaskProcessor.callParse(parser, src.createSnapshot(), task, null);
                success = true;
            } finally {
                Utilities.releaseParserLock();
            }
        } catch (AssertionError ae) {}
        assertTrue("AssertionError not expected when calling callParse with parser lock", success); //NOI18N
        assertEquals(1, parser.parseCount);

        success = false;
        try {
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                TaskProcessor.callGetResult(parser, task);
            }
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling callGetResult under INTERNAL_LOCK", success);       //NOI18N

        success = false;
        try {
            TaskProcessor.callGetResult(parser, task);
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling callGetResult without parser lock", success); //NOI18N

        success = false;
        try {
            Utilities.acquireParserLock();
            try {
                TaskProcessor.callGetResult(parser, task);
                success = true;
            } finally {
                Utilities.releaseParserLock();
            }
        } catch (AssertionError ae) {}
        assertTrue("AssertionError not expected when calling callGetResult with parser lock", success); //NOI18N
        assertEquals(1, parser.resultCount);
        
    }

    public void testRunWhenScanFinishGetCalledUnderCCLock() throws Exception {
        final File wd = getWorkDir();
        final File srcDir = new File (wd,"src");
        srcDir.mkdirs();
        final File file = new File (srcDir,"test.foo");
        file.createNewFile();
        FileUtil.setMIMEType("foo", "text/foo");
        MockMimeLookup.setInstances(MimePath.parse("text/foo"), new FooParserFactory(), new PlainKit());
        final FileObject fo = FileUtil.toFileObject(file);
        final DataObject dobj = DataObject.find(fo);
        final EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
        final StyledDocument doc = ec.openDocument();
        final Source src = Source.create(doc);
        final CountDownLatch ruRunning = new CountDownLatch(1);
        final CountDownLatch rwsfCalled = new CountDownLatch(1);
        final AtomicReference<Set<RepositoryUpdater.IndexingState>> indexing = new AtomicReference<Set<RepositoryUpdater.IndexingState>>();
        final Utilities.IndexingStatus is = new Utilities.IndexingStatus() {
            @Override
            public Set<? extends RepositoryUpdater.IndexingState> getIndexingState() {
                return indexing.get();
            }
        };
        Utilities.setIndexingStatus(is);
        RepositoryUpdaterTestSupport.runAsWork(
                new Runnable(){
                    @Override
                    public void run() {
                        indexing.set(EnumSet.of(RepositoryUpdater.IndexingState.WORKING));
                        try {
                            ruRunning.countDown();
                            rwsfCalled.await();
                        } catch (InterruptedException ie) {
                        } finally {
                            indexing.set(EnumSet.noneOf(RepositoryUpdater.IndexingState.class));
                        }
                    }
                });
        ruRunning.await();
        doc.putProperty("completion-active", Boolean.TRUE);
        try {
            final Future<Void> done = ParserManager.parseWhenScanFinished(Collections.<Source>singleton(src),new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {                    
                }
        });
        assertFalse(done.isDone());
        assertFalse(done.isCancelled());
        rwsfCalled.countDown();
        try {
            done.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            assertTrue("Deadlock",false);
        }
        } finally {
            doc.putProperty("completion-active", null);
        }
    }

    public void testRunLoopSuspend() throws Exception {
        FileUtil.setMIMEType("foo", "text/foo");    //NOI18N
        MockMimeLookup.setInstances(MimePath.parse("text/foo"), new FooParserFactory(), new PlainKit());    //NOI18N
        final File wd = getWorkDir();
        final File srcFolder = new File (wd,"src");
        final FileObject srcRoot = FileUtil.createFolder(srcFolder);
        final FileObject srcFile = srcRoot.createData("test.foo");  //NOI18N        
        final Source source = Source.create(srcFile);
        final SourceCache cache = SourceAccessor.getINSTANCE().getCache(source);
        final CountDownLatch taskStarted = new CountDownLatch(1);
        final CountDownLatch cancelCalled = new CountDownLatch(1);
        final CountDownLatch taskDone = new CountDownLatch(1);
        final CountDownLatch secondTaskCalled = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean();        
        final SchedulerTask task1 = new ParserResultTask() {

            @Override
            public void run(Result pr, SchedulerEvent event) {
                taskStarted.countDown();
                try {
                    result.set(cancelCalled.await(5000, TimeUnit.MILLISECONDS));
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    taskDone.countDown();
                }
            }

            @Override
            public int getPriority() {
                return 0;
            }

            @Override
            public Class<? extends Scheduler> getSchedulerClass() {
                return Scheduler.SELECTED_NODES_SENSITIVE_TASK_SCHEDULER;
            }

            @Override
            public void cancel() {
                cancelCalled.countDown();
            }
        };
        final SchedulerTask task2 = new ParserResultTask() {
            @Override
            public void run(Result result, SchedulerEvent event) {
                secondTaskCalled.countDown();
            }

            @Override
            public int getPriority() {
                return 10;
            }

            @Override
            public Class<? extends Scheduler> getSchedulerClass() {
                return Scheduler.SELECTED_NODES_SENSITIVE_TASK_SCHEDULER;
            }

            @Override
            public void cancel() {
            }
        };
        TaskProcessor.addPhaseCompletionTasks(
                Arrays.asList(task1, task2),
                cache,
                false,
                task1.getSchedulerClass());
        assertTrue(taskStarted.await(5000, TimeUnit.MILLISECONDS));
        runLoop(source, true);
        try {
            assertTrue(taskDone.await(5000, TimeUnit.MILLISECONDS));
            assertTrue(result.get());
            assertFalse(secondTaskCalled.await(2000, TimeUnit.MILLISECONDS));
        } finally {
            runLoop(source, false);
        }
        assertTrue(secondTaskCalled.await(5000, TimeUnit.MILLISECONDS));
    }

    public void testRunLoopSuspend2() throws Exception {
        FileUtil.setMIMEType("foo", "text/foo");    //NOI18N
        MockMimeLookup.setInstances(MimePath.parse("text/foo"), new FooParserFactory(), new PlainKit());    //NOI18N
        final File wd = getWorkDir();
        final File srcFolder = new File (wd,"src");
        final FileObject srcRoot = FileUtil.createFolder(srcFolder);
        final FileObject srcFile = srcRoot.createData("test.foo");  //NOI18N
        final Source source = Source.create(srcFile);
        final SourceCache cache = SourceAccessor.getINSTANCE().getCache(source);
        final CountDownLatch taskCalled = new CountDownLatch(1);
        final SchedulerTask task = new ParserResultTask() {
            @Override
            public void run(Result result, SchedulerEvent event) {
                taskCalled.countDown();
            }

            @Override
            public int getPriority() {
                return 10;
            }

            @Override
            public Class<? extends Scheduler> getSchedulerClass() {
                return Scheduler.SELECTED_NODES_SENSITIVE_TASK_SCHEDULER;
            }

            @Override
            public void cancel() {
            }
        };

        runLoop(source, true);
        try {
            TaskProcessor.addPhaseCompletionTasks(
                Arrays.asList(task),
                cache,
                false,
                task.getSchedulerClass());
            assertFalse(taskCalled.await(2000, TimeUnit.MILLISECONDS));
        } finally {
            runLoop(source, false);
        }
        assertTrue(taskCalled.await(5000, TimeUnit.MILLISECONDS));
    }

    @RandomlyFails // in fact always for jglick
    public void testSlowCancelSampler() throws Exception {
        //Enable sampling
        TaskProcessor.SAMPLING_ENABLED = true;
        //Set bigger profiling start report time outs to make the test deterministic
        //on loaded build machenes
        System.setProperty("org.netbeans.modules.parsing.api.taskcancel.slowness.start","3000");    //NOI18N
        System.setProperty("org.netbeans.modules.parsing.api.taskcancel.slowness.report","3000");  //NOI18N
        final File wd = getWorkDir();
        FileUtil.setMIMEType("foo", "text/foo");
        MockMimeLookup.setInstances(MimePath.parse("text/foo"), new FooParserFactory(), new PlainKit());
        final File srcFolder = new File (wd,"src");
        final FileObject srcRoot = FileUtil.createFolder(srcFolder);
        final FileObject srcFile = srcRoot.createData("test.foo");  //NOI18N
        final Source source = Source.create(srcFile);
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger timeToWait = new AtomicInteger(0);
        final MockProfiler mockProfiler = new MockProfiler();
        final Logger log = Logger.getLogger("org.netbeans.modules.parsing.impl.SelfProfile");   //NOI18N
        log.addHandler(mockProfiler);
        log.setLevel(Level.FINEST);
        try {
            final Callable<Void> runCB = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    latch.countDown();
                    return null;
                }
            };
            final Callable<Void> cancelCB = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    try {
                        Thread.sleep(timeToWait.get());
                    } catch (InterruptedException ie) {}
                    return null;
                }
            };

            //No profiling should be started when cancel is immediate
            SlowCancelTask sct = new SlowCancelTask(runCB, cancelCB);
            mockProfiler.expect(EnumSet.noneOf(MockProfiler.Event.class));
            TaskProcessor.addPhaseCompletionTasks(
                    Arrays.<SchedulerTask>asList(sct),
                    SourceAccessor.getINSTANCE().getCache(source),
                    false,
                    sct.getSchedulerClass());
            assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
            ParserManager.parse(Arrays.asList(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                }
            });
            assertTrue(mockProfiler.verify(5000));

            //Profiling should be started when cancel is delayed when cancel is slower
            timeToWait.set(3500);
            sct = new SlowCancelTask(runCB, cancelCB);
            mockProfiler.expect(EnumSet.of(MockProfiler.Event.STARTED, MockProfiler.Event.CANCELED));
            TaskProcessor.addPhaseCompletionTasks(
                    Arrays.<SchedulerTask>asList(sct),
                    SourceAccessor.getINSTANCE().getCache(source),
                    false,
                    sct.getSchedulerClass());
            assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
            ParserManager.parse(Arrays.asList(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                }
            });
            assertTrue(mockProfiler.verify(5000));

            //Profiling should be started and report sent when cancel is very slow
            timeToWait.set(6500);
            sct = new SlowCancelTask(runCB, cancelCB);
            mockProfiler.expect(EnumSet.of(MockProfiler.Event.STARTED, MockProfiler.Event.LOGGED));
            TaskProcessor.addPhaseCompletionTasks(
                    Arrays.<SchedulerTask>asList(sct),
                    SourceAccessor.getINSTANCE().getCache(source),
                    false,
                    sct.getSchedulerClass());
            assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
            ParserManager.parse(Arrays.asList(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                }
            });
            assertTrue(mockProfiler.verify(8000));
        } finally {
            log.removeHandler(mockProfiler);
        }
    }

    private void runLoop(
            final @NonNull Source source,
            final boolean suspend) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final Field editorRegistryListenerField = EventSupport.class.getDeclaredField("editorRegistryListener");    //NOI18N
        assertNotNull(editorRegistryListenerField);
        editorRegistryListenerField.setAccessible(true);
        final Object erl = editorRegistryListenerField.get(null);
        assertNotNull(erl);
        final Method handleCompletionActive = erl.getClass().getDeclaredMethod("handleCompletionActive", Source.class, Object.class); //NOI18N
        assertNotNull(handleCompletionActive);
        handleCompletionActive.setAccessible(true);
        handleCompletionActive.invoke(erl, source, suspend);
    }

    private static final class FooParserFactory extends ParserFactory {
        @Override
        public Parser createParser(Collection<Snapshot> snapshots) {
            return new FooParser();
        }
    }

    private static final class FooParser extends Parser {
        private FooParserResult result;
        private int cancelCount;
        private int parseCount;
        private int resultCount;

        public @Override void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
            parseCount++;
            result = new FooParserResult((snapshot));
        }

        public @Override Result getResult(Task task) throws ParseException {
            resultCount++;
            return result;
        }

        public @Override void cancel() {
            cancelCount++;
        }

        public @Override void addChangeListener(ChangeListener changeListener) {
        }

        public @Override void removeChangeListener(ChangeListener changeListener) {
        }
    }

    private static final class FooParserResult extends Parser.Result {
        public FooParserResult(Snapshot snapshot) {
            super(snapshot);
        }

        protected @Override void invalidate() {
        }
    }

    private static final class StackTraceUserTask extends UserTask {
        public StackTraceElement caller;
        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            ArrayList<StackTraceElement> filteredStackTrace = new ArrayList<StackTraceElement>();
            StackTraceElement [] stackTrace = Thread.currentThread().getStackTrace();
            boolean active = false;
            for(StackTraceElement e : stackTrace) {
                if (!active) {
                    if (e.getClassName().equals(TaskProcessor.class.getName()) && e.getMethodName().equals("runUserTask")) {
                        active = true;
                    } else {
                        continue;
                    }
                }
                filteredStackTrace.add(e);
            }
            caller = Util.findCaller(filteredStackTrace.toArray(new StackTraceElement[filteredStackTrace.size()]));
        }
    }

    private static class FooTask extends  ParserResultTask  {
        
        private int cancelCount;
        private int runCount;

        @Override
        public void run(Result result, SchedulerEvent event) {
            runCount++;
        }

        @Override
        public int getPriority() {
            return 10;
        }

        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return Scheduler.SELECTED_NODES_SENSITIVE_TASK_SCHEDULER;
        }

        @Override
        public void cancel() {
            cancelCount++;
        }
    }

    private static class FooEmbeddingProvider extends EmbeddingProvider {

        private int runCount;

        @Override
        public List<Embedding> getEmbeddings(Snapshot snapshot) {
            runCount++;
            return Collections.<Embedding>emptyList();
        }

        @Override
        public int getPriority() {
            return 10;
        }

        @Override
        public void cancel() {
        }

    }

    private static class FooUserTask extends UserTask {

        int runCount;

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            runCount++;
        }
    }

    private static class SlowCancelTask extends ParserResultTask {

        private final Semaphore sem = new Semaphore(0);
        private final Callable<Void> cancelStartCallBack;
        private final Callable<Void> runStartCallBack;

        public SlowCancelTask (
                final @NonNull Callable<Void> runStartCallBack,
                final @NonNull Callable<Void> cancelStartCallBack) {
            assert runStartCallBack != null;
            assert cancelStartCallBack != null;
            this.runStartCallBack = runStartCallBack;
            this.cancelStartCallBack = cancelStartCallBack;
        }

        public void inc() {
            sem.release();
        }

        public void dec() throws InterruptedException {
            sem.acquire();
        }

        @Override
        public void run(Result result, SchedulerEvent event) {
            try {
                runStartCallBack.call();
                dec();
            } catch (Exception ie) {}
        }

        @Override
        public void cancel() {
            try {
                cancelStartCallBack.call();
                inc();
            } catch (Exception e) {}
        }

        @Override
        public int getPriority() {
            return 1;
        }

        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return Scheduler.SELECTED_NODES_SENSITIVE_TASK_SCHEDULER;
        }
    }

    private static class MockProfiler extends Handler {

        private enum Event {
            STARTED,
            CANCELED,
            LOGGED
        };

        private final Lock lck = new ReentrantLock();
        private final Condition cnd = lck.newCondition();

        private volatile Set<Event> expectedEvents;

        public void expect (final @NonNull Set<Event> expectedEvents) {
            lck.lock();
            try {
                this.expectedEvents = EnumSet.copyOf(expectedEvents);
            } finally {
                lck.unlock();
            }
        }

        public boolean verify(int ms) throws InterruptedException {
            final long st = System.currentTimeMillis();
            lck.lock();
            try {
                while (!expectedEvents.isEmpty()) {
                    if (System.currentTimeMillis()-st >= ms) {
                        return false;
                    }
                    cnd.await(ms, TimeUnit.MILLISECONDS);
                }
            } finally {
                lck.unlock();
            }
            return true;
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        @Override
        public void publish(LogRecord record) {
            final String msg = record.getMessage();
            if ("STARTED".equals(msg)) {        //NOI18N
                handleEvent(Event.STARTED);
            } else if ("LOGGED".equals(msg)) {  //NOI18N
                handleEvent(Event.LOGGED);
            } else if ("CANCEL".equals(msg)) {    //NOI18N
                handleEvent(Event.CANCELED);
            }
        }

        private void handleEvent(final @NonNull Event event) {
            lck.lock();
            try {
                expectedEvents.remove(event);                
                if (expectedEvents.isEmpty()) {
                    cnd.signalAll();
                }
            } finally {
                lck.unlock();
            }
        }

    }
}
