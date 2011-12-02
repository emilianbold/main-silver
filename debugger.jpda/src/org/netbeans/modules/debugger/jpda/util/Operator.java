/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.debugger.jpda.util;

import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.ThreadReference;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocatableWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MirrorWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.ClassPrepareEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.ClassUnloadEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.EventQueueWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.EventSetWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.EventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.LocatableEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.ThreadDeathEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.ThreadStartEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.StepRequestWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;

/**
 * Listens for events coming from a remove VM and notifies registered objects.
 * <P>
 * Any object implementing interface {@link Executor} can bind itself
 * with an {@link EventRequest}. Each time an {@link Event} corresponding
 * to the request comes from the virtual machine the <TT>Operator</TT>
 * notifies the registered object by calling its <TT>exec()</TT> method.
 * <P>
 * The only exceptions to the above rule are <TT>VMStartEvent</TT>,
 * <TT>VMDeathEvent</TT> and <TT>VMDisconnectEvent</TT> that cannot be
 * bound to any request. To listen for these events, specify <EM>starter</EM>
 * and <EM>finalizer</EM> in the constructor.
 * <P>
 * The operator is not active until it is started - use method <TT>start()</TT>.
 * The operator stops itself when either <TT>VMDeathEvent</TT> or <TT>VMDisconnectEvent</TT>
 * is received; it can be started again.
 * <P>
 * Use method {@link #register} to bind a requst with an object.
 * The object can be unregistered - use method {@link #unregister}.
 * <P>
 * There should be only one <TT>Operator</TT> per remote VM.
*
* @author Jan Jancura
*/
public class Operator {

    private static Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.jdievents"); // NOI18N

    public static final String SILENT_EVENT_PROPERTY = "silent"; // NOI18N

    private Thread            thread;
    private final Set<ThreadReference> methodInvokingThreads = new HashSet<ThreadReference>();
    private boolean           stop;
    private boolean           canInterrupt;
    private JPDADebuggerImpl  debugger;
    private RequestProcessor  eventHandler;
    private Map<ThreadReference, HandlerTask> eventHandlers = new HashMap<ThreadReference, HandlerTask>();
    private final List<EventSet> parallelEvents = new LinkedList<EventSet>();
    private final LoopControl loopControl;

    /**
     * Creates an operator for a given virtual machine. The operator will listen
     * to the VM's event queue.
     *
     * @param  virtualMachine  remote VM this operator will listen to
     * @param  starter  thread to be started upon start of the remote VM
     *                  (may be <TT>null</TT>)
     * @param  finalizer  thread to be started upon death of the remote VM
     *                    or upon disconnection from the VM
     *                    (may be <TT>null</TT>)
     * @param resumeLock Debugger's access lock under which threads should be
     *                   resumed after events are processed.
    */
    public Operator (
        VirtualMachine virtualMachine,
        final JPDADebuggerImpl debugger,
        Executor starter,
        Runnable finalizer,
        final ReadWriteLock resumeLock
    ) {
        EventQueue eventQueue;
        try {
            eventQueue = VirtualMachineWrapper.eventQueue(virtualMachine);
        } catch (InternalExceptionWrapper ex) {
            eventQueue = null;
        } catch (VMDisconnectedExceptionWrapper ex) {
            eventQueue = null;
        }
        if (eventQueue == null)
            throw new NullPointerException ();
        this.debugger = debugger;
        final AWTGrabHandler awtGrabHandler = new AWTGrabHandler(debugger);
        final SuspendCount suspendCount = new SuspendCount();
        final Object[] params = new Object[] {eventQueue, starter, finalizer};
        thread = new Thread (new Runnable () {
        public void run () {
            EventQueue eventQueue = (EventQueue) params [0];
            Executor starter = (Executor) params [1];
            Runnable finalizer = (Runnable) params [2];
            params [0] = null;
            params [1] = null;
            params [2] = null;

       loop: for (;;) {
                 try {
                     EventSet eventSet = null;
                     if (!loopControl.isInMethodInvoke()) { // Do not process the parallel events while a method is being invoked.
                         boolean haveParallelEvents = false;
                         synchronized (parallelEvents) {
                             if (!parallelEvents.isEmpty()) {
                                 eventSet = parallelEvents.remove(0);
                                 haveParallelEvents = parallelEvents.isEmpty();
                             }
                         }
                         if (!haveParallelEvents) {
                             // No more parallel events
                             loopControl.setHaveParallelEventsInLoopThread(false);
                         }
                     }
                     if (eventSet == null) {
                        try {
                            synchronized (Operator.this) {
                                if (stop) break;
                                canInterrupt = true;
                            }
                            eventSet = EventQueueWrapper.remove (eventQueue);
                            if (logger.isLoggable(Level.FINE)) {
                                try {
                                    logger.fine("HAVE EVENT(s) in the Queue: "+eventSet);
                                } catch (ObjectCollectedException ocex) {
                                    logger.log(Level.FINE, "HAVE EVENT(s) in the Queue with something collected:", ocex);
                                }
                            }
                        } catch (InterruptedException iexc) {
                            if (loopControl.isInterrupedToProcessParallelEvents()) {
                                continue;
                            }
                            synchronized (Operator.this) {
                                if (stop) {
                                    break;
                                }
                            }
                            continue;
                        }
                        synchronized (Operator.this) {
                            canInterrupt = false;
                        }
                     } else {
                        if (logger.isLoggable(Level.FINE)) {
                            try {
                                logger.fine("HAVE PARALLEL EVENT(s) in the Queue: "+eventSet);
                            } catch (ObjectCollectedException ocex) {
                                logger.log(Level.FINE, "HAVE PARALLEL EVENT(s) in the Queue with something collected:", ocex);
                            }
                        }
                     }
                     boolean doContinue = processEvents(eventSet, starter, awtGrabHandler, suspendCount);
                     if (!doContinue) {
                         break;
                     }
                 } catch (VMDisconnectedException e) {
                     break;
                 } catch (VMDisconnectedExceptionWrapper e) {
                     break;
                 //} catch (InterruptedException e) {
                 } catch (Exception e) {
                     ErrorManager.getDefault().notify(e);
                 }
             }// for
             if (finalizer != null) finalizer.run ();
             //S ystem.out.println ("Operator end"); // NOI18N
             finalizer = null;
             eventQueue = null;
             starter = null;
         }
     }, "Debugger operator thread"); // NOI18N
        loopControl = new LoopControl(thread, starter, awtGrabHandler, suspendCount);
    }

    private boolean processEvents(EventSet eventSet, Executor starter,
                                  AWTGrabHandler awtGrabHandler, SuspendCount suspendCount) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper, IllegalThreadStateExceptionWrapper {                 
        boolean silent = eventSet.size() > 0;
        for (Event e: eventSet) {
            EventRequest r = EventWrapper.request(e);
            if (r == null || !Boolean.TRUE.equals(EventRequestWrapper.getProperty (r, SILENT_EVENT_PROPERTY))) {
                silent = false;
                break;
            }
        }

        // Notify threads about suspend state, but do not fire any events yet.
        boolean resume = true, startEventOnly = true;
        int suspendPolicy = EventSetWrapper.suspendPolicy(eventSet);
        boolean suspendedAll = suspendPolicy == EventRequest.SUSPEND_ALL;
        JPDAThreadImpl suspendedThread = null;
        boolean threadWasInitiallySuspended = false;
        Lock eventAccessLock = null;
        try {
        ThreadReference thref = null;
        boolean isThreadEvent = false;
        for (Event e: eventSet) {
            if (e instanceof ThreadStartEvent || e instanceof ThreadDeathEvent) {
                isThreadEvent = true;
            }
            thref = getEventThread(e);
            if (thref != null) {
                break;
            }
        }
        Set<ThreadReference> ignoredThreads = new HashSet<ThreadReference>();
        if (testIgnoreEvent(eventSet, ignoredThreads)) {
            // Only if it's a breakpoint event, resume and forget.
            eventSet.resume();
            return true;
        } else {
            // If not, add into parallel events, but assure that we'd
            // wait for more events comming from the EventQueue and
            // *after* method invocation is complete, process these
            // events before further reading from the EventQueue.
            if (thref != null) {
                boolean isInMethodInvokeThread;
                synchronized (methodInvokingThreads) {
                    isInMethodInvokeThread = methodInvokingThreads.contains(thref);
                }
                if (isInMethodInvokeThread) {
                    // The event comes while we're invoking a method.
                    // Remember it for a further processing
                    synchronized (parallelEvents) {
                        parallelEvents.add(eventSet);
                        if (logger.isLoggable(Level.FINE)) {
                            try {
                                logger.fine("  the event(s) in the Queue are stored as parallelEvents = "+parallelEvents);
                            } catch (ObjectCollectedException ocex) {
                                logger.log(Level.FINE, "  the event(s) in the Queue are stored as parallelEvents with something collected:", ocex);
                            }
                        }
                    }
                    loopControl.setHaveParallelEventsInLoopThread(true);
                    if (EventSetWrapper.suspendPolicy(eventSet) != EventRequest.SUSPEND_NONE) {
                        // Resume the thread suspended by the event so that the method invocation can be finished.
                        if (logger.isLoggable(Level.FINE)) {
                            try {
                                logger.fine("  resuming the method invocation thread so that it can complete: "+thref);
                            } catch (ObjectCollectedException ocex) {
                                logger.log(Level.FINE, "  resuming the method invocation thread so that it can complete.");
                            }
                        }
                        ThreadReferenceWrapper.resume(thref);
                    }
                    return true;
                }
            }
        }
        if (thref != null && !isThreadEvent) {
            debugger.getThreadsCache().assureThreadIsCached(thref);
        }
        if (!silent && suspendedAll) {
            eventAccessLock = debugger.accessLock.writeLock();
            eventAccessLock.lock();
            logger.finer("Write access lock TAKEN "+eventAccessLock+" on whole debugger.");
            debugger.notifySuspendAllNoFire();
        }
        if (suspendPolicy == EventRequest.SUSPEND_EVENT_THREAD) {
            if (thref != null && !silent) {
               suspendedThread = debugger.getThread(thref);
               eventAccessLock = suspendedThread.accessLock.writeLock();
               eventAccessLock.lock();
               try {
                   if (!ThreadReferenceWrapper.isSuspended(thref)) {
                       // Can not do anything, someone already resumed the thread in the mean time.
                       // The event is missed. We will therefore ignore it.
                       try {
                           logger.warning("!!\nMissed event "+eventSet+" thread "+thref+" is not suspended!\n");
                       } catch (ObjectCollectedException ocex) {
                           try {
                               logger.warning("!!\nMissed event "+eventSet+" due to collected thread");
                           } catch (ObjectCollectedException ocex2) {
                               logger.warning("!!\nMissed some event due to collected thread!");
                           }
                       }
                       return true;
                   }
               } catch (ObjectCollectedExceptionWrapper e) {
                   // O.K. the thread is gone...
                   return true;
               } catch (IllegalThreadStateExceptionWrapper e) {
                   // O.K. the thread is gone...
                   return true;
               } catch (InternalExceptionWrapper e) {
                   // ignore VM defects
               }
               // We check for multiple-suspension when event is received
               try {
                   int sc = ThreadReferenceWrapper.suspendCount(thref);
                   if (logger.isLoggable(Level.FINER)) {
                       logger.finer("Suspend count of "+thref+" is "+sc+"."+((sc > 1) ? "Reducing to one." : ""));
                   }
                   while (sc-- > 1) {
                       suspendCount.add(thref);
                       ThreadReferenceWrapper.resume(thref);
                   }
               } catch (ObjectCollectedExceptionWrapper e) {
               } catch (IllegalThreadStateExceptionWrapper e) {
                   // ignore mobility VM defects
               } catch (InternalExceptionWrapper e) {
                   // ignore mobility VM defects
               }
               if (logger.isLoggable(Level.FINE)) {
                   try {
                       logger.finer("Write access lock TAKEN "+eventAccessLock+" on thread "+thref);
                       logger.fine(" event thread "+thref.name()+" is suspended = "+thref.isSuspended());
                   } catch (Exception ex) {}
               }
               threadWasInitiallySuspended = suspendedThread.isSuspended();
               suspendedThread.notifySuspendedNoFire();
            }
        }

        Map<Event, Executor> eventsToProcess = new HashMap<Event, Executor>();
        for (Event e: eventSet) {
            EventRequest r = EventWrapper.request(e);
            Executor exec = (r != null) ? (Executor) EventRequestWrapper.getProperty (r, "executor") : null;
            if (exec instanceof ConditionedExecutor) {
                boolean success = ((ConditionedExecutor) exec).processCondition(e);
                if (success) {
                   eventsToProcess.put(e, exec);
                }
            } else {
                eventsToProcess.put(e, exec);
            }
        }
        if (eventsToProcess.size() == 0) {
            // Notify Resumed No Fire
            if (!silent && suspendedAll) {
                //TODO: Not really all might be suspended!
                debugger.notifyToBeResumedAllNoFire();
            }
            if (!silent && suspendedThread != null) {
                resume = resume && suspendedThread.notifyToBeResumedNoFire();
            }
            if (!resume) {
                if (!silent && suspendedAll) {
                    //TODO: Not really all might be suspended!
                    boolean grabSolved = awtGrabHandler.solveGrabbing(debugger.getVirtualMachine()); // TODO: check AWT thread!
                    if (!grabSolved) {
                       resume = true; // We must not stop here, nobody will ever be able to resume
                    } else {
                        List<PropertyChangeEvent> events = debugger.notifySuspendAll(false, false,
                                                                                     ignoredThreads.isEmpty() ? null : ignoredThreads);
                        if (eventAccessLock != null) {
                            logger.finer("Write access lock RELEASED:"+eventAccessLock);
                            eventAccessLock.unlock();
                            eventAccessLock = null;
                        }
                        for (PropertyChangeEvent event : events) {
                            ((JPDAThreadImpl) event.getSource()).fireEvent(event);
                        }
                    }
                }
                if (!silent && suspendedThread != null) {
                    boolean grabSolved = awtGrabHandler.solveGrabbing(thref);
                    if (!grabSolved) {
                       resume = true; // We must not stop here, nobody will ever be able to resume
                    } else {
                        PropertyChangeEvent event = suspendedThread.notifySuspended(false, false);
                        if (eventAccessLock != null) {
                            logger.finer("Write access lock RELEASED:"+eventAccessLock);
                            eventAccessLock.unlock();
                            eventAccessLock = null;
                        }
                        if (event != null) {
                            suspendedThread.fireEvent(event);
                        }
                    }
                }
            }
            logger.fine("Resuming the event set = "+resume);
            if (resume) {
                int sc = suspendCount.removeSuspendCountFor(thref);
                while (sc-- > 1) {
                    ThreadReferenceWrapper.suspend(thref);
                }
                try {
                   EventSetWrapper.resume(eventSet);
                } catch (IllegalThreadStateExceptionWrapper itex) {
                    logger.throwing(Operator.class.getName(), "loop", itex);
                } catch (ObjectCollectedExceptionWrapper ocex) {
                    logger.throwing(Operator.class.getName(), "loop", ocex);
                }
                if (eventAccessLock != null) {
                    logger.finer("Write access lock RELEASED:"+eventAccessLock);
                    eventAccessLock.unlock();
                    eventAccessLock = null;
                }
            }
            return true;
        }
        if (logger.isLoggable(Level.FINE)) {
            switch (suspendPolicy) {
                case EventRequest.SUSPEND_ALL:
                    logger.fine("JDI new events (suspend all)=============================================");
                    break;
                case EventRequest.SUSPEND_EVENT_THREAD:
                    logger.fine("JDI new events (suspend one)=============================================");
                    break;
                case EventRequest.SUSPEND_NONE:
                    logger.fine("JDI new events (suspend none)=============================================");
                    break;
                default:
                    logger.fine("JDI new events (?????)=============================================");
                    break;
            }
            logger.fine("  event is silent = "+silent);
        }
        for (Event e: eventSet) {
            if (!eventsToProcess.containsKey(e)) {
                // Ignore events whose executor conditions did not evaluate successfully.
                continue;
            }
            if ((e instanceof VMDeathEvent) ||
                (e instanceof VMDisconnectEvent)
               ) {
                
                if (logger.isLoggable(Level.FINE)) {
                    printEvent (e, null);
                }
                synchronized (Operator.this) {
                    stop = true;
                }
                return false;
            }

            if ((e instanceof VMStartEvent) && (starter != null)) {
                resume = resume & starter.exec (e);
                //S ystem.out.println ("Operator.start VM"); // NOI18N
                if (logger.isLoggable(Level.FINE)) {
                    printEvent (e, null);
                }
                continue;
            }
            Executor exec = null;
            if (EventWrapper.request(e) == null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("EVENT: " + e + " REQUEST: null"); // NOI18N
                }
            } else
                exec = eventsToProcess.get(e);

            if (logger.isLoggable(Level.FINE)) {
                printEvent (e, exec);
            }

            // safe invocation of user action
            if (exec != null) {
                try {
                    startEventOnly = false;
                    if (logger.isLoggable(Level.FINE)) {
                        ThreadReference tref = getEventThread(e);
                        if (tref != null) {
                            try {
                                logger.fine(" event thread "+tref.name()+" suspend before exec = "+tref.isSuspended());
                            } catch (Exception ex) {}
                            //System.err.println("\nOperator: event thread "+tref.name()+" suspend before exec = "+tref.isSuspended()+"\n");
                        }
                    }
                    resume = resume & exec.exec (e);
                } catch (VMDisconnectedException exc) {
//                                 disconnected = true;
                    synchronized (Operator.this) {
                        stop = true;
                    }
                    //S ystem.out.println ("EVENT: " + e); // NOI18N
                    //S ystem.out.println ("Operator end"); // NOI18N
                    return false;
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        } // for

        //            S ystem.out.println ("END (" + set.suspendPolicy () + ") ==========================================================================="); // NOI18N
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("JDI events dispatched (resume " + (resume && (!startEventOnly)) + ")");
            logger.fine("  resume = "+resume+", startEventOnly = "+startEventOnly);
        }

        int sc = suspendCount.removeSuspendCountFor(thref);
        while (sc-- > 1) {
            ThreadReferenceWrapper.suspend(thref);
        }
        // Notify the resume under eventAccessLock so that nobody can get in between,
        // which would result in resuming the thread twice.
        if (!resume) { // notify about the suspend if not resumed.
            if (!silent && suspendedAll) {
                //TODO: Not really all might be suspended!
                boolean grabSolved = awtGrabHandler.solveGrabbing(debugger.getVirtualMachine()); // TODO: check AWT thread!
                if (!grabSolved) {
                    resume = true; // We must not stop here, nobody will ever be able to resume
                } else {
                    List<PropertyChangeEvent> events = debugger.notifySuspendAll(false, false,
                                                                                 ignoredThreads.isEmpty() ? null : ignoredThreads);
                    if (eventAccessLock != null) {
                        logger.finer("Write access lock RELEASED:"+eventAccessLock);
                        eventAccessLock.unlock();
                        eventAccessLock = null;
                    }
                    for (PropertyChangeEvent event : events) {
                        ((JPDAThreadImpl) event.getSource()).fireEvent(event);
                    }
                }
            }
            if (!silent && suspendedThread != null) {
                boolean grabSolved = awtGrabHandler.solveGrabbing(thref);
                if (!grabSolved) {
                    resume = true; // We must not stop here, nobody will ever be able to resume
                } else {
                    PropertyChangeEvent event = suspendedThread.notifySuspended(false, false);
                    if (eventAccessLock != null) {
                        logger.finer("Write access lock RELEASED:"+eventAccessLock);
                        eventAccessLock.unlock();
                        eventAccessLock = null;
                    }
                    if (event != null) {
                        suspendedThread.fireEvent(event);
                    }
                }
            }
        }
        if (resume) {
            if (!silent && suspendedAll) {
                //TODO: Not really all might be suspended!
                debugger.notifyToBeResumedAllNoFire();
            }
            if (!silent && suspendedThread != null) {
                resume = resume && suspendedThread.notifyToBeResumedNoFire();
            }
        }
        if (!startEventOnly) {
            if (logger.isLoggable(Level.FINE)) {
               try {
                   logger.fine("Resuming the event set "+eventSet+" = "+resume);
               } catch (ObjectCollectedException ocex) {
                   logger.log(Level.FINE, "Resuming the event set that with something collected = "+resume);
               }
            }
            if (resume) {
                //resumeLock.writeLock().lock();
                try {
                    EventSetWrapper.resume(eventSet);
                } catch (IllegalThreadStateExceptionWrapper itex) {
                    logger.throwing(Operator.class.getName(), "loop", itex);
                } catch (ObjectCollectedExceptionWrapper ocex) {
                    logger.throwing(Operator.class.getName(), "loop", ocex);
                }
                //} finally {
                //    resumeLock.writeLock().unlock();
                //}
            } else if (!silent && (suspendedAll || suspendedThread != null)) {
               Session session = debugger.getSession();
               if (session != null) {
                   DebuggerManager.getDebuggerManager().setCurrentSession(session);
               }
               if (thref != null) debugger.setStoppedState (thref, suspendedAll);
            }
        }


        } finally {
            if (eventAccessLock != null) {
                logger.finer("Write access lock RELEASED:"+eventAccessLock);
                eventAccessLock.unlock();
            }
            if (resume && threadWasInitiallySuspended) {
                suspendedThread.fireAfterNotifyToBeResumedNoFire();
            }
        }
        /* We check for multiple-suspension when event is received
         * This check is already performed above
        if (!silent && !resume) { // Check for multiply-suspended threads
            resumeLock.writeLock().lock();
            try {
                List<ThreadReference> threads = VirtualMachineWrapper.allThreads(MirrorWrapper.virtualMachine(eventSet));
                for (ThreadReference t : threads) {
                    try {
                        JPDAThreadImpl jt = debugger.getExistingThread(t);
                        while (ThreadReferenceWrapper.suspendCount(t) > 1) {
                            if (jt != null) {
                                jt.notifyToBeResumed();
                                jt = null;
                            }
                            ThreadReferenceWrapper.resume(t);
                        } // while
                    } catch (ObjectCollectedExceptionWrapper e) {
                    } catch (IllegalThreadStateExceptionWrapper e) {
                        // ignore mobility VM defects
                    } catch (InternalExceptionWrapper e) {
                        // ignore mobility VM defects
                    }
                } // for
            } finally {
                resumeLock.writeLock().unlock();
            }
        }*/
        return true;
    }
    
    private static final ThreadReference getEventThread(Event e) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        ThreadReference tref = null;
        if (e instanceof LocatableEvent) {
            tref = LocatableEventWrapper.thread((LocatableEvent) e);
        } else
        if (e instanceof ClassPrepareEvent) {
            tref = ClassPrepareEventWrapper.thread((ClassPrepareEvent) e);
        } else
        if (e instanceof ThreadStartEvent) {
            tref = ThreadStartEventWrapper.thread((ThreadStartEvent) e);
        } else
        if (e instanceof ThreadDeathEvent) {
            tref = ThreadDeathEventWrapper.thread((ThreadDeathEvent) e);
        }
        return tref;
    }

    /**
    * Starts checking of JPDA messages.
    */
    public void start () {
        thread.start ();
    }

    /**
     * Binds the specified object with the event request.
     * If the request is already bound with another object,
     * the old binding is removed.
     *
     * @param  req  request
     * @param  e  object to be bound with the request
     *            (if <TT>null</TT>, the binding is removed - the same as <TT>unregister()</TT>)
     * @see  #unregister
     */
    public synchronized void register (EventRequest req, Executor e) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        EventRequestWrapper.putProperty(req, "executor", e); // NOI18N
    }

    /**
     * Removes binding between the specified event request and a registered object.
     *
     * @param  req  request
     * @see  #register
     */
    public synchronized void unregister (EventRequest req) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        Executor e = (Executor) EventRequestWrapper.getProperty(req, "executor");
        EventRequestWrapper.putProperty (req, "executor", null); // NOI18N
        if (e != null) {
            e.removed(req);
        }
        if (req instanceof StepRequest) {
            ThreadReference tr = StepRequestWrapper.thread((StepRequest) req);
            debugger.getThread(tr).setInStep(false, null);
        }
    }

    /**
     * Stop the operator thread.
     */
    public void stop() {
        synchronized (this) {
            if (stop) return ; // Do not interrupt the thread when we're stopped
            stop = true;
            if (canInterrupt) {
                thread.interrupt();
            }
        }
    }

    private void startEventHandlerThreadFor(final ThreadReference tr) {
        RequestProcessor rp;
        synchronized (this) {
            if (eventHandler == null) {
                eventHandler = new RequestProcessor("Debugger Event Handler", 10);  // NOI18N
            }
            rp = eventHandler;
        }
        final Thread[] threadPtr = new Thread[] { null };
        RequestProcessor.Task task = rp.post(new Runnable() {
            @Override
            public void run() {
                synchronized (threadPtr) {
                    threadPtr[0] = Thread.currentThread();
                    threadPtr.notifyAll();
                }
                EventQueue eventQueue;
                try {
                    eventQueue = VirtualMachineWrapper.eventQueue(MirrorWrapper.virtualMachine(tr));
                } catch (InternalExceptionWrapper ex) {
                    return ;
                } catch (VMDisconnectedExceptionWrapper ex) {
                    return ;
                }
                if (Thread.interrupted()) {
                    return ;
                }
                for (;;) {
                    EventSet eventSet;
                    try {
                        eventSet = EventQueueWrapper.remove(eventQueue);
                        
                        if (logger.isLoggable(Level.FINE)) {
                            try {
                                logger.fine("HAVE EVENT(s) in the Queue for "+tr+" : "+eventSet);
                            } catch (ObjectCollectedException ocex) {
                                logger.log(Level.FINE, "HAVE EVENT(s) in the Queue for a thread with something collected:", ocex);
                            }
                        }
                        
                        Set<ThreadReference> ignoredThreads = new HashSet<ThreadReference>();
                        if (testIgnoreEvent(eventSet, ignoredThreads)) {
                            eventSet.resume();
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("  the event(s) in the Queue for "+tr+" are ignored and event set is resumed.");
                            }
                        } else {
                            synchronized (parallelEvents) {
                                parallelEvents.add(eventSet);
                                if (logger.isLoggable(Level.FINE)) {
                                    try {
                                        logger.fine("  the event(s) in the Queue for "+tr+" are stored as parallelEvents = "+parallelEvents);
                                    } catch (ObjectCollectedException ocex) {
                                        logger.log(Level.FINE, "  the event(s) in the Queue for "+tr+" are stored as parallelEvents with something collected:", ocex);
                                    }
                                }
                            }
                        }

                    } catch (InterruptedException ex) {
                        return ;
                    } catch (InternalExceptionWrapper ex) {
                        //Exceptions.printStackTrace(ex);
                        // Ignore
                        continue;
                    } catch (VMDisconnectedExceptionWrapper ex) {
                        return ;
                    } catch (Exception e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    if (Thread.interrupted()) {
                        return ;
                    }
                }
            }
        }, 500);
        eventHandlers.put(tr, new HandlerTask(task, threadPtr));
    }

    public void notifyMethodInvoking(ThreadReference tr) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("  notifyMethodInvoking("+tr+")");
        }
        if (Thread.currentThread() == thread) {
            // start another event handler thread...
            startEventHandlerThreadFor(tr);
        }
        synchronized (methodInvokingThreads) {
            methodInvokingThreads.add(tr);
        }
        loopControl.setInMethodInvoke(true);
    }

    public void notifyMethodInvokeDone(ThreadReference tr) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("  notifyMethodInvokeDone("+tr+")");
        }
        if (Thread.currentThread() == thread) {
            HandlerTask task = eventHandlers.remove(tr);
            if (task != null) {
                task.cancel();
            }
        }
        boolean done;
        synchronized (methodInvokingThreads) {
            methodInvokingThreads.remove(tr);
            done = methodInvokingThreads.isEmpty();
        }
        if (done) {
            loopControl.setInMethodInvoke(false);
        }
    }

    private boolean testIgnoreEvent(EventSet eventSet, Set<ThreadReference> resumedThreads) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        ThreadReference tref = null;
        for (Event e : eventSet) {
            if (e instanceof LocatableEvent) {
                tref = LocatableEventWrapper.thread((LocatableEvent) e);
            }
        }
        if (tref != null) {
            synchronized (methodInvokingThreads) {
                if (methodInvokingThreads.contains(tref)) {
                    return true;
                }
            }
        }
        int suspendPolicy = EventSetWrapper.suspendPolicy(eventSet);
        if (suspendPolicy == EventRequest.SUSPEND_ALL) {
            // Event suspended all threads, including those in which a method is being invoked.
            synchronized (methodInvokingThreads) {
                for (ThreadReference tr : methodInvokingThreads) {
                    try {
                        ThreadReferenceWrapper.resume(tr);
                        resumedThreads.add(tr);
                    } catch (ObjectCollectedExceptionWrapper ex) {
                    } catch (IllegalThreadStateExceptionWrapper ex) {
                    }
                }
            }
            // We handle resumed threads later - exclude them from notifying, etc.
            // TODO: If we hit this just before a method invoke, the thread will be suspended double-times.
        }
        return false;
    }

    private void printEvent (Event e, Executor exec) {
        try {
            if (e instanceof ClassPrepareEvent) {
                logger.fine("JDI EVENT: ClassPrepareEvent " + ClassPrepareEventWrapper.referenceType((ClassPrepareEvent) e)); // NOI18N
            } else
            if (e instanceof ClassUnloadEvent) {
                logger.fine("JDI EVENT: ClassUnloadEvent " + ClassUnloadEventWrapper.className((ClassUnloadEvent) e)); // NOI18N
            } else
            if (e instanceof ThreadStartEvent) {
                try {
                    logger.fine("JDI EVENT: ThreadStartEvent " + ThreadStartEventWrapper.thread((ThreadStartEvent) e)); // NOI18N
                } catch (Exception ex) {
                    logger.fine("JDI EVENT: ThreadStartEvent1 " + e); // NOI18N
                }
            } else
            if (e instanceof ThreadDeathEvent) {
                try {
                    logger.fine("JDI EVENT: ThreadDeathEvent " + ThreadDeathEventWrapper.thread((ThreadDeathEvent) e)); // NOI18N
                } catch (Exception ex) {
                    logger.fine("JDI EVENT: ThreadDeathEvent1 " + e); // NOI18N
                }
            } else
            if (e instanceof MethodEntryEvent) {
                try {
                    logger.fine("JDI EVENT: MethodEntryEvent " + e);
                } catch (Exception ex) {
                    logger.fine("JDI EVENT: MethodEntryEvent " + e);
                }
            } else
            if (e instanceof BreakpointEvent) {
                logger.fine("JDI EVENT: BreakpointEvent " + LocatableEventWrapper.thread((BreakpointEvent) e) + " : " + LocatableWrapper.location((BreakpointEvent) e)); // NOI18N
            } else
            if (e instanceof StepEvent) {
                logger.fine("JDI EVENT: StepEvent " + LocatableEventWrapper.thread((StepEvent) e) + " : " + LocatableWrapper.location((StepEvent) e)); // NOI18N
            } else
                logger.fine("JDI EVENT: " + e + " : " + exec); // NOI18N
        } catch (Exception ex) {
            logger.fine(ex.getLocalizedMessage());
        }
    }
    
    private static final class HandlerTask {

        private RequestProcessor.Task task;
        private final Thread[] threadPtr;

        HandlerTask(RequestProcessor.Task task, Thread[] threadPtr) {
            this.task = task;
            this.threadPtr = threadPtr;
        }

        void cancel() {
            if (!task.cancel()) {
                synchronized (threadPtr) {
                    if (threadPtr[0] == null) {
                        try {
                            threadPtr.wait();
                        } catch (InterruptedException ex) {
                        }
                    }
                }
                Thread t = threadPtr[0];
                if (t != null) {
                    t.interrupt();
                    try {
                        while (!task.waitFinished(250)) {
                            t.interrupt();
                        }
                    } catch (InterruptedException ex) {
                        task.waitFinished();
                    }
                }
            }
        }
    }
    
    private static final class SuspendCount {
        
        private final Map<ThreadReference, MutableInteger> threads = new WeakHashMap<ThreadReference, MutableInteger>();
        
        public synchronized void add(ThreadReference t) {
            MutableInteger i = threads.get(t);
            if (i == null) {
                i = new MutableInteger(1);
                threads.put(t, i);
            }
            i.i++;
        }
        
        public synchronized int getSuspendCountFor(ThreadReference t) {
            MutableInteger i = threads.get(t);
            if (i == null) {
                return 0;
            } else {
                return i.i;
            }
        }
        
        public synchronized int removeSuspendCountFor(ThreadReference t) {
            MutableInteger i = threads.remove(t);
            if (i == null) {
                return 0;
            } else {
                return i.i;
            }
        }
        
        private static final class MutableInteger {
            
            public int i;
            
            public MutableInteger(int i) {
                this.i = i;
            }
            
        }
    }
    
    private final class LoopControl {
        
        private Thread t;
        private boolean isMethodInvoke;
        private boolean haveParallelEventsInLoopThread;
        private boolean interrupedToProcessParalelEvents;
        
        public LoopControl(Thread t, Executor starter,
                           AWTGrabHandler awtGrabHandler, SuspendCount suspendCount) {
            this.t = t;
        }
        
        public void setInMethodInvoke(boolean isMethodInvoke) {
            if (!isMethodInvoke) {
                synchronized (this) {
                    if (haveParallelEventsInLoopThread) {
                        interrupedToProcessParalelEvents = true;
                        t.interrupt();
                    }
                }
            }
            synchronized (this) {
                this.isMethodInvoke = isMethodInvoke;
            }
        }
        
        public boolean isInterrupedToProcessParallelEvents() {
            boolean is = this.interrupedToProcessParalelEvents;
            this.interrupedToProcessParalelEvents = false;
            return is;
        }
        
        public synchronized void setHaveParallelEventsInLoopThread(boolean haveParallelEventsInLoopThread) {
            this.haveParallelEventsInLoopThread = haveParallelEventsInLoopThread;
        }
        
        public synchronized boolean haveParallelEventsInLoopThread() {
            return haveParallelEventsInLoopThread;
        }
        
        public synchronized boolean isInMethodInvoke() {
            return isMethodInvoke;
        }
        
    }

}
