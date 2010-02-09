/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InternalException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.TypeComponent;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.InvalidRequestStateException;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.Properties;

import org.netbeans.api.debugger.jpda.DeadlockDetector;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.modules.debugger.jpda.actions.CompoundSmartSteppingListener;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.ObjectTranslation;
import org.netbeans.modules.debugger.jpda.util.JPDAUtils;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.AbstractDICookie;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.JPDAStep;
import org.netbeans.api.debugger.jpda.ListeningDICookie;

import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.breakpoints.BreakpointsEngineListener;
import org.netbeans.modules.debugger.jpda.expr.EvaluatorExpression;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.models.LocalsTreeModel;
import org.netbeans.modules.debugger.jpda.models.CallStackFrameImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAClassTypeImpl;
import org.netbeans.modules.debugger.jpda.models.ThreadsCache;
import org.netbeans.modules.debugger.jpda.util.Operator;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IntegerValueWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MirrorWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ValueWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestWrapper;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.DelegatingSessionProvider;

import org.netbeans.spi.debugger.jpda.Evaluator;
import org.netbeans.spi.viewmodel.TreeModel;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
* Representation of a debugging session.
*
* @author   Jan Jancura
*/
public class JPDADebuggerImpl extends JPDADebugger {

    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda");

    private static final boolean SINGLE_THREAD_STEPPING = !Boolean.getBoolean("netbeans.debugger.multiThreadStepping");


    // variables ...............................................................

    //private DebuggerEngine              debuggerEngine;
    private VirtualMachine              virtualMachine = null;
    private final Object                virtualMachineLock = new Object();
    private Exception                   exception;
    private int                         state = 0;
    private final Object                stateLock = new Object();
    private Operator                    operator;
    private PropertyChangeSupport       pcs;
    public  PropertyChangeSupport       varChangeSupport = new PropertyChangeSupport(this);
    private JPDAThreadImpl              currentThread;
    private CallStackFrame              currentCallStackFrame;
    private final Object                currentThreadAndFrameLock = new Object();
    private int                         suspend = (SINGLE_THREAD_STEPPING) ? SUSPEND_EVENT_THREAD : SUSPEND_ALL;
    public final ReentrantReadWriteLock accessLock = new DebuggerReentrantReadWriteLock(true);
    private final Object                LOCK2 = new Object ();
    private boolean                     starting;
    private AbstractDICookie            attachingCookie;
    private JavaEngineProvider          javaEngineProvider;
    private Set<String>                 languages;
    private String                      lastStratumn;
    private ContextProvider             lookupProvider;
    private ObjectTranslation           threadsTranslation;
    private ObjectTranslation           localsTranslation;
    private ExpressionPool              expressionPool;
    private ThreadsCache                threadsCache;
    private DeadlockDetector            deadlockDetector;
    private ThreadsCollectorImpl        threadsCollector;
    private final Object                threadsCollectorLock = new Object();
    private final Map<Long, String>     markedObjects = new LinkedHashMap<Long, String>();
    private final Map<String, ObjectVariable> markedObjectLabels = new LinkedHashMap<String, ObjectVariable>();

    private StackFrame      altCSF = null;  //PATCH 48174

    private boolean                     doContinue = true; // Whether resume() will actually resume
    private Boolean                     singleThreadStepResumeDecision = null;
    private Boolean                     stepInterruptByBptResumeDecision = null;

    // init ....................................................................

    public JPDADebuggerImpl (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        
        Properties p = Properties.getDefault().getProperties("debugger.options.JPDA");
        suspend = p.getInt("StepResume", suspend);

        pcs = new PropertyChangeSupport (this);
        List l = lookupProvider.lookup (null, DebuggerEngineProvider.class);
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            if (l.get (i) instanceof JavaEngineProvider)
                javaEngineProvider = (JavaEngineProvider) l.get (i);
        if (javaEngineProvider == null)
            throw new IllegalArgumentException
                ("JavaEngineProvider have to be used to start JPDADebugger!");
        languages = new HashSet<String>();
        languages.add ("Java");
        threadsTranslation = ObjectTranslation.createThreadTranslation(this);
        localsTranslation = ObjectTranslation.createLocalsTranslation(this);
        this.expressionPool = new ExpressionPool();
    }


    // JPDADebugger methods ....................................................

    /**
     * Returns current state of JPDA debugger.
     *
     * @return current state of JPDA debugger
     * @see #STATE_STARTING
     * @see #STATE_RUNNING
     * @see #STATE_STOPPED
     * @see #STATE_DISCONNECTED
     */
    public int getState () {
        synchronized (stateLock) {
            return state;
        }
    }

    /**
     * Gets value of suspend property.
     *
     * @return value of suspend property
     */
    public int getSuspend () {
        synchronized (stateLock) {
            return suspend;
        }
    }

    /**
     * Sets value of suspend property.
     *
     * @param s a new value of suspend property
     */
    public void setSuspend (int s) {
        int old;
        synchronized (stateLock) {
            if (s == suspend) return;
            old = suspend;
            suspend = s;
        }
        firePropertyChange (PROP_SUSPEND, Integer.valueOf(old), Integer.valueOf(s));
    }

    /**
     * Returns current thread or null.
     *
     * @return current thread or null
     */
    public JPDAThread getCurrentThread () {
        return currentThread;
    }

    /**
     * Returns current stack frame or null.
     *
     * @return current stack frame or null
     */
    public CallStackFrame getCurrentCallStackFrame () {
        synchronized (currentThreadAndFrameLock) {
            return currentCallStackFrame;
        }
    }

    /**
     * Evaluates given expression in the current context.
     *
     * @param expression a expression to be evaluated
     *
     * @return current value of given expression
     */
    public Variable evaluate (String expression) throws InvalidExpressionException {
        return evaluate(expression, null, null);
    }

    /**
     * Evaluates given expression in the context of the variable.
     *
     * @param expression a expression to be evaluated
     *
     * @return current value of given expression
     */
    public Variable evaluate (String expression, ObjectVariable var)
    throws InvalidExpressionException {
        return evaluate(expression, null, var);
    }

    /**
     * Evaluates given expression in the current context.
     *
     * @param expression a expression to be evaluated
     *
     * @return current value of given expression
     */
    public Variable evaluate (String expression, CallStackFrame csf)
    throws InvalidExpressionException {
        return evaluate(expression, csf, null);
    }

    /**
     * Evaluates given expression in the current context.
     *
     * @param expression a expression to be evaluated
     *
     * @return current value of given expression
     */
    public Variable evaluate (String expression, CallStackFrame csf, ObjectVariable var)
    throws InvalidExpressionException {
        return evaluateGeneric(new EvaluatorExpression(expression), csf, var);
    }

    /**
     * Waits till the Virtual Machine is started and returns
     * {@link DebuggerStartException} if any.
     *
     * @throws DebuggerStartException is some problems occurres during debugger
     *         start
     *
     * @see AbstractDICookie#getVirtualMachine()
     */
    public void waitRunning () throws DebuggerStartException {
        synchronized (LOCK2) {
            int state = getState();
            if (state == STATE_DISCONNECTED) {
                if (exception != null)
                    throw new DebuggerStartException (exception);
                else
                    return;
            }
            if (!starting && state != STATE_STARTING || exception != null) {
                return ; // We're already running
            }
            try {
                logger.fine("JPDADebuggerImpl.waitRunning(): starting = "+starting+", state = "+state+", exception = "+exception);
                LOCK2.wait ();
            } catch (InterruptedException e) {
                 throw new DebuggerStartException (e);
            }

            if (exception != null)
                throw new DebuggerStartException (exception);
            else
                return;
        }
    }

    /**
     * Returns <code>true</code> if this debugger supports Pop action.
     *
     * @return <code>true</code> if this debugger supports Pop action
     */
    public boolean canPopFrames () {
        VirtualMachine vm = getVirtualMachine ();
        if (vm == null) return false;
        return VirtualMachineWrapper.canPopFrames0(vm);
    }

    /**
     * Returns <code>true</code> if this debugger supports fix & continue
     * (HotSwap).
     *
     * @return <code>true</code> if this debugger supports fix & continue
     */
    public boolean canFixClasses () {
        VirtualMachine vm = getVirtualMachine ();
        if (vm == null) return false;
        return VirtualMachineWrapper.canRedefineClasses0(vm);
    }

    /**
     * Implements fix & continue (HotSwap). Map should contain class names
     * as a keys, and byte[] arrays as a values.
     *
     * @param classes a map from class names to be fixed to byte[]
     */
    public void fixClasses (Map<String, byte[]> classes) {
        accessLock.writeLock().lock();
        try {
            // 1) redefine classes
            Map<ReferenceType, byte[]> map = new HashMap<ReferenceType, byte[]>();
            Iterator<Map.Entry<String, byte[]>> e = classes.entrySet ().iterator ();
            VirtualMachine vm = getVirtualMachine();
            if (vm == null) {
                return ; // The session has finished
            }
            try {
                while (e.hasNext ()) {
                    Map.Entry<String, byte[]> classEntry = e.next();
                    String className = classEntry.getKey();
                    byte[] bytes = classEntry.getValue();
                    List<ReferenceType> classRefs = VirtualMachineWrapper.classesByName (vm, className);
                    int j, jj = classRefs.size ();
                    for (j = 0; j < jj; j++)
                        map.put (
                            classRefs.get (j),
                            bytes
                        );
                }
                VirtualMachineWrapper.redefineClasses (vm, map);
            } catch (InternalExceptionWrapper ex) {
            } catch (VMDisconnectedExceptionWrapper ex) {
            }

            // update breakpoints
            fixBreakpoints();

            firePropertyChange("classesFixed", null, null); // [TODO] add property to API

            // 2) pop obsoleted frames
            JPDAThread t = getCurrentThread ();
            if (t != null && t.isSuspended()) {
                try {
                    if (t.getStackDepth () < 2) return;
                    CallStackFrame frame;
                    try {
                        frame = t.getCallStack(0, 1)[0]; // Retrieve the new, possibly obsoleted frame and check it.
                    } catch (AbsentInformationException ex) {
                        return;
                    }

                    //PATCH #52209
                    if (frame.isObsolete () && ((CallStackFrameImpl) frame).canPop()) {
                        frame.popFrame ();
                        setState (STATE_RUNNING);
                        updateCurrentCallStackFrame (t);
                        setState (STATE_STOPPED);
                    }
                } catch (InvalidStackFrameException ex) {
                }
            }

        } finally {
            accessLock.writeLock().unlock();
        }
    }

    public void fixBreakpoints() {
        Session s = getSession();
        DebuggerEngine de = s.getEngineForLanguage ("Java");
        BreakpointsEngineListener bel = null;
        List lazyListeners = de.lookup(null, LazyActionsManagerListener.class);
        for (int li = 0; li < lazyListeners.size(); li++) {
            Object service = lazyListeners.get(li);
            if (service instanceof BreakpointsEngineListener) {
                bel = (BreakpointsEngineListener) service;
                break;
            }
        }
        // Just reset the time stamp so that new line numbers are taken.
        EditorContextBridge.getContext().disposeTimeStamp(this);
        EditorContextBridge.getContext().createTimeStamp(this);
        if (bel != null) bel.fixBreakpointImpls ();
    }

    public Session getSession() {
        return lookupProvider.lookupFirst(null, Session.class);
    }

    public RequestProcessor getRequestProcessor() {
        return javaEngineProvider.getRequestProcessor();
    }

    private Boolean canBeModified;
    private final Object canBeModifiedLock = new Object();

    public boolean canBeModified() {
        VirtualMachine vm = getVirtualMachine ();
        if (vm == null) return false;
        synchronized (canBeModifiedLock) {
            if (canBeModified == null) {
                canBeModified = vm.canBeModified();
            }
            return canBeModified.booleanValue();
        }
    }

    private SmartSteppingFilter smartSteppingFilter;

    /**
     * Returns instance of SmartSteppingFilter.
     *
     * @return instance of SmartSteppingFilter
     */
    public SmartSteppingFilter getSmartSteppingFilter () {
        if (smartSteppingFilter == null) {
            smartSteppingFilter = lookupProvider.lookupFirst(null, SmartSteppingFilter.class);
        }
        return smartSteppingFilter;
    }

    CompoundSmartSteppingListener compoundSmartSteppingListener;

    private CompoundSmartSteppingListener getCompoundSmartSteppingListener () {
        if (compoundSmartSteppingListener == null)
            compoundSmartSteppingListener = lookupProvider.lookupFirst(null, CompoundSmartSteppingListener.class);
        return compoundSmartSteppingListener;
    }

    /**
     * Test whether we should stop here according to the smart-stepping rules.
     */
    boolean stopHere(JPDAThread t) {
        return getCompoundSmartSteppingListener ().stopHere
                     (lookupProvider, t, getSmartSteppingFilter());
    }

    /**
     * Helper method that fires JPDABreakpointEvent on JPDABreakpoints.
     *
     * @param breakpoint a breakpoint to be changed
     * @param event a event to be fired
     */
    public void fireBreakpointEvent (
        JPDABreakpoint breakpoint,
        JPDABreakpointEvent event
    ) {
        super.fireBreakpointEvent (breakpoint, event);
    }

    /**
    * Adds property change listener.
    *
    * @param l new listener.
    */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
    * Removes property change listener.
    *
    * @param l removed listener.
    */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }

    /**
    * Adds property change listener.
    *
    * @param l new listener.
    */
    public void addPropertyChangeListener (String propertyName, PropertyChangeListener l) {
        pcs.addPropertyChangeListener (propertyName, l);
    }

    /**
    * Removes property change listener.
    *
    * @param l removed listener.
    */
    public void removePropertyChangeListener (String propertyName, PropertyChangeListener l) {
        pcs.removePropertyChangeListener (propertyName, l);
    }


    // internal interface ......................................................

    public void popFrames (ThreadReference thread, StackFrame frame) {
        accessLock.readLock().lock();
        try {
            JPDAThreadImpl threadImpl = getThread(thread);
            setState (STATE_RUNNING);
            try {
                threadImpl.popFrames(frame);
                updateCurrentCallStackFrame (threadImpl);
            } catch (IncompatibleThreadStateException ex) {
                ErrorManager.getDefault().notify(ex);
            } finally {
                setState (STATE_STOPPED);
            }
        } finally {
            accessLock.readLock().unlock();
        }
    }

    public void setException (Exception e) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("JPDADebuggerImpl.setException("+e+")");
        }
        synchronized (LOCK2) {
            exception = e;
            starting = false;
            LOCK2.notifyAll ();
        }
    }

    public void setCurrentThread (JPDAThread thread) {
        Object oldT;
        PropertyChangeEvent event;
        CallStackFrame topFrame = getTopFrame(thread);
        synchronized (currentThreadAndFrameLock) {
            oldT = currentThread;
            currentThread = (JPDAThreadImpl) thread;
            event = updateCurrentCallStackFrameNoFire(topFrame);
        }
        if (thread != oldT) {
            firePropertyChange (PROP_CURRENT_THREAD, oldT, thread);
        }
        if (event != null) {
            firePropertyChange(event);
        }
        setState(thread.isSuspended() ? STATE_STOPPED : STATE_RUNNING);
    }

    /**
     * Set the current thread and call stack, but do not fire changes.
     * @return The PropertyChangeEvent associated with this change, it can have
     *         attached other PropertyChangeEvents as a propagation ID.
     */
    private PropertyChangeEvent setCurrentThreadNoFire(JPDAThread thread) {
        Object oldT;
        PropertyChangeEvent evt = null;
        PropertyChangeEvent evt2;
        CallStackFrame topFrame = getTopFrame(thread);
        synchronized (currentThreadAndFrameLock) {
            oldT = currentThread;
            currentThread = (JPDAThreadImpl) thread;
            evt2 = updateCurrentCallStackFrameNoFire(topFrame);
        }
        if (thread != oldT) {
            evt = new PropertyChangeEvent(this, PROP_CURRENT_THREAD, oldT, thread);
        }
        if (evt == null) evt = evt2;
        else if (evt2 != null) evt.setPropagationId(evt2);
        return evt;
    }

    public void setCurrentCallStackFrame (CallStackFrame callStackFrame) {
        CallStackFrame old = setCurrentCallStackFrameNoFire(callStackFrame);
        if (old == callStackFrame) return ;
        firePropertyChange (
            PROP_CURRENT_CALL_STACK_FRAME,
            old,
            callStackFrame
        );
    }

    private CallStackFrame setCurrentCallStackFrameNoFire (CallStackFrame callStackFrame) {
        CallStackFrame old;
        synchronized (currentThreadAndFrameLock) {
            if (callStackFrame == currentCallStackFrame) return callStackFrame;
            old = currentCallStackFrame;
            currentCallStackFrame = callStackFrame;
        }
        return old;
    }

    /**
     * Used by AbstractVariable and watches.
     */
    // * Might be changed to return a variable with disabled collection. When not used any more,
    // * it's collection must be enabled again.
    public Value evaluateIn (String expression) throws InvalidExpressionException {
        return evaluateIn(new EvaluatorExpression(expression), null);
    }

    /**
     * Used by BreakpointImpl.
     */
    // * Might be changed to return a variable with disabled collection. When not used any more,
    // * it's collection must be enabled again.
    public Value evaluateIn (EvaluatorExpression expression, CallStackFrame csf) throws InvalidExpressionException {
        return evaluateIn(expression, csf, null);
    }

    // * Might be changed to return a variable with disabled collection. When not used any more,
    // * it's collection must be enabled again.
    private Value evaluateIn (EvaluatorExpression expression, CallStackFrame csf, ObjectVariable var) throws InvalidExpressionException {
        Variable variable = evaluateGeneric(expression, csf, var);
        if (variable instanceof JDIVariable) {
            return ((JDIVariable) variable).getJDIValue();
        } else {
            return null;
        }
    }

    //PATCH 48174
    public void setAltCSF(StackFrame sf) {
        altCSF = sf;
    }

    public StackFrame getAltCSF() {
        return altCSF;
    }

    /**
     * Used by WatchesModel & BreakpointImpl.
     */
    // * Might be changed to return a variable with disabled collection. When not used any more,
    // * it's collection must be enabled again.
    public Value evaluateIn (EvaluatorExpression expression) throws InvalidExpressionException {
        return evaluateIn(expression, null, null);
    }


    InvalidExpressionException methodCallsUnsupportedExc;

    /**
     * Evaluates given expression in the current context.
     *
     * @param expression a expression to be evaluated
     *
     * @return current value of given expression
     */
    private Variable evaluateGeneric(EvaluatorExpression expression, CallStackFrame c, ObjectVariable var)
            throws InvalidExpressionException {

        Session s = getSession();
        Evaluator e = s.lookupFirst(s.getCurrentLanguage(), Evaluator.class);
        logger.fine("Have evaluator "+e+" for language "+s.getCurrentLanguage());   // NOI18N

        if (e == null) {
            e = new JavaEvaluator(s);
        }

        CallStackFrameImpl csf;
        if (c instanceof CallStackFrameImpl) {
            csf = (CallStackFrameImpl) c;
        } else {
            csf = (CallStackFrameImpl) getCurrentCallStackFrame ();
        }
        if (csf == null) {
            StackFrame sf = getAltCSF();
            if (sf != null) {
                try {
                    ThreadReference t = StackFrameWrapper.thread(sf);
                    JPDAThread tr = getThread(t);
                    c = tr.getCallStack(0, 1)[0];
                    csf = (CallStackFrameImpl) c;
                } catch (InternalExceptionWrapper ex) {
                } catch (InvalidStackFrameExceptionWrapper ex) {
                } catch (VMDisconnectedExceptionWrapper ex) {
                } catch (AbsentInformationException aiex) {
                }
            }
        }
        if (csf == null) {
            throw new InvalidExpressionException
                (NbBundle.getMessage(JPDADebuggerImpl.class, "MSG_NoCurrentContextStackFrame"));
        }

        JPDAThread frameThread = csf.getThread();
        Lock lock = ((JPDAThreadImpl) frameThread).accessLock.writeLock();
        lock.lock();
        Variable vr;
        try {
            ObjectReference v = null;
            if (var instanceof JDIVariable) {
                v = (ObjectReference) ((JDIVariable) var).getJDIValue();
            }
            Evaluator.Result result;
            final List<EventRequest>[] disabledBreakpoints = new List[] { null };
            final JPDAThreadImpl[] resumedThread = new JPDAThreadImpl[] { null };
            try {
                StackFrame sf = csf.getStackFrame();
                int stackDepth = csf.getFrameDepth();
                final ThreadReference tr = StackFrameWrapper.thread(sf);
                Runnable methodToBeInvokedNotifier = new Runnable() {
                        public void run() {
                            if (disabledBreakpoints[0] == null) {
                                JPDAThreadImpl theResumedThread = getThread(tr);
                                try {
                                    theResumedThread.notifyMethodInvoking();
                                } catch (PropertyVetoException pvex) {
                                    throw new RuntimeException(
                                        new InvalidExpressionException (pvex.getMessage()));
                                }
                                try {
                                    disabledBreakpoints[0] = disableAllBreakpoints();
                                    resumedThread[0] = theResumedThread;
                                } catch (InternalExceptionWrapper ex) {
                                } catch (VMDisconnectedExceptionWrapper ex) {
                                }
                            }
                        }
                    };
                Lookup context;
                if (var != null) {
                    if (v != null) {
                        context = Lookups.fixed(csf, var, sf, stackDepth, v, methodToBeInvokedNotifier);
                    } else {
                        context = Lookups.fixed(csf, var, sf, stackDepth, methodToBeInvokedNotifier);
                    }
                } else {
                    context = Lookups.fixed(csf, sf, stackDepth, methodToBeInvokedNotifier);
                }
                result = expression.evaluate(e, new Evaluator.Context(context));
            } catch (InternalExceptionWrapper ex) {
                return null;
            } catch (InvalidStackFrameExceptionWrapper ex) {
                throw new InvalidExpressionException
                    (NbBundle.getMessage(JPDADebuggerImpl.class, "MSG_NoCurrentContextStackFrame"));
            } catch (VMDisconnectedExceptionWrapper ex) {
                // Causes kill action when something is being evaluated.
                return null;
            } catch (InternalException ex) {
                InvalidExpressionException isex = new InvalidExpressionException(ex.getLocalizedMessage());
                isex.initCause(ex);
                Exceptions.attachMessage(isex, "Expression = '"+expression+"'");
                throw isex;
            } catch (RuntimeException rex) {
                Throwable cause = rex.getCause();
                if (cause instanceof InvalidExpressionException) {
                    Exceptions.attachMessage(cause, "Expression = '"+expression+"'");
                    throw (InvalidExpressionException) cause;
                } else {
                    throw rex;
                }
            } finally {
                if (disabledBreakpoints[0] != null) {
                    enableAllBreakpoints (disabledBreakpoints[0]);
                }
                if (resumedThread[0] != null) {
                    resumedThread[0].notifyMethodInvokeDone();
                }
            }
            vr = result.getVariable();
            if (vr == null) {
                vr = getLocalsTreeModel ().getVariable (result.getValue());
            }
        } finally {
            lock.unlock();
        }
        return vr;
    }

    /**
     * Used by AbstractVariable.
     */
    public Value invokeMethod (
        ObjectReference reference,
        Method method,
        Value[] arguments
    ) throws InvalidExpressionException {
        return invokeMethod(null, reference, method, arguments, 0);
    }

    public Value invokeMethod (
        ObjectReference reference,
        Method method,
        Value[] arguments,
        int maxLength
    ) throws InvalidExpressionException {
        return invokeMethod(null, reference, method, arguments, maxLength);
    }

    /**
     * Used by AbstractVariable.
     */
    public Value invokeMethod (
        JPDAThreadImpl thread,
        ObjectReference reference,
        Method method,
        Value[] arguments
    ) throws InvalidExpressionException {
        return invokeMethod(thread, reference, method, arguments, 0);
    }

    private Value invokeMethod (
        JPDAThreadImpl thread,
        ObjectReference reference,
        Method method,
        Value[] arguments,
        int maxLength
    ) throws InvalidExpressionException {
        synchronized (currentThreadAndFrameLock) {
            if (thread == null && currentThread == null)
                throw new InvalidExpressionException
                        (NbBundle.getMessage(JPDADebuggerImpl.class, "MSG_NoCurrentContext"));
            if (thread == null) {
                thread = currentThread;
            }
        }
        thread.accessLock.writeLock().lock();
        try {
            if (methodCallsUnsupportedExc != null) {
                throw methodCallsUnsupportedExc;
            }
            boolean threadSuspended = false;
            JPDAThread frameThread = null;
            CallStackFrameImpl csf = null;
            List<EventRequest> l = null;
            try {
                // Remember the current stack frame, it might be necessary to re-set.
                csf = (CallStackFrameImpl) getCurrentCallStackFrame ();
                if (csf != null) {
                    try {
                        frameThread = csf.getThread();
                    } catch (InvalidStackFrameException isfex) {}
                }
                ThreadReference tr = thread.getThreadReference();
                try {
                    thread.notifyMethodInvoking();
                    threadSuspended = true;
                } catch (PropertyVetoException pvex) {
                    throw new InvalidExpressionException (pvex.getMessage());
                }
                try {
                    l = disableAllBreakpoints();
                } catch (InternalExceptionWrapper ex) {
                } catch (VMDisconnectedExceptionWrapper ex) {
                    return null;
                }
                try {
                    Value v = org.netbeans.modules.debugger.jpda.expr.TreeEvaluator.
                        invokeVirtual (
                            reference,
                            method,
                            tr,
                            Arrays.asList (arguments),
                            this
                        );
                    if (maxLength > 0 && maxLength < Integer.MAX_VALUE && (v instanceof StringReference)) {
                        v = cutLength((StringReference) v, maxLength, tr);
                    }
                    return v;
                } catch (InternalException e) {
                    InvalidExpressionException ieex = new InvalidExpressionException (e.getLocalizedMessage());
                    ieex.initCause(e);
                    throw ieex;
                }
            } catch (InvalidExpressionException ieex) {
                if (ieex.getTargetException() instanceof UnsupportedOperationException) {
                    methodCallsUnsupportedExc = ieex;
                }
                throw ieex;
            } finally {
                if (l != null) {
                    enableAllBreakpoints (l);
                }
                if (threadSuspended) {
                    thread.notifyMethodInvokeDone();
                }
                if (frameThread != null) {
                    try {
                        csf.getThread();
                    } catch (InvalidStackFrameException isfex) {
                        // The current frame is invalidated, set the new current...
                        int depth = csf.getFrameDepth();
                        try {
                            CallStackFrame csf2 = frameThread.getCallStack(depth, depth + 1)[0];
                            setCurrentCallStackFrameNoFire(csf2);
                        } catch (AbsentInformationException aiex) {
                            setCurrentCallStackFrame(null);
                        }
                    }
                }
            }
        } finally {
            thread.accessLock.writeLock().unlock();
        }
    }

    private Value cutLength(StringReference sr, int maxLength, ThreadReference tr) throws InvalidExpressionException {
        try {
            Method stringLengthMethod;
                stringLengthMethod = ClassTypeWrapper.concreteMethodByName(
                        (ClassType) ValueWrapper.type (sr), "length", "()I"); // NOI18N
            List<Value> emptyArgs = Collections.emptyList();
            IntegerValue lengthValue = (IntegerValue) org.netbeans.modules.debugger.jpda.expr.TreeEvaluator.
                invokeVirtual (
                    sr,
                    stringLengthMethod,
                    tr,
                    emptyArgs,
                    this
                );
                if (IntegerValueWrapper.value(lengthValue) > maxLength) {
                    Method subStringMethod = ClassTypeWrapper.concreteMethodByName(
                            (ClassType) ValueWrapper.type (sr), "substring", "(II)Ljava/lang/String;");  // NOI18N
                    if (subStringMethod != null) {
                        sr = (StringReference) org.netbeans.modules.debugger.jpda.expr.TreeEvaluator.
                            invokeVirtual (
                                sr,
                                subStringMethod,
                                tr,
                                Arrays.asList(new Value [] { VirtualMachineWrapper.mirrorOf(MirrorWrapper.virtualMachine(sr), 0),
                                               VirtualMachineWrapper.mirrorOf(MirrorWrapper.virtualMachine(sr), maxLength) }),
                                this
                            );
                    }
                }
        } catch (InternalExceptionWrapper ex) {
        } catch (ObjectCollectedExceptionWrapper ex) {
        } catch (VMDisconnectedExceptionWrapper ex) {
        } catch (ClassNotPreparedExceptionWrapper ex) {
        }
        return sr;
    }

    public static String getGenericSignature (TypeComponent component) {
        if (tcGenericSignatureMethod == null) return null;
        try {
            return (String) tcGenericSignatureMethod.invoke
                (component, new Object[0]);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;    // should not happen
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;    // should not happen
        }
    }

    public static String getGenericSignature (LocalVariable component) {
        if (lvGenericSignatureMethod == null) return null;
        try {
            return (String) lvGenericSignatureMethod.invoke(component, new Object[0]);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;    // should not happen
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;    // should not happen
        }
    }

    public VirtualMachine getVirtualMachine () {
        synchronized (virtualMachineLock) {
            return virtualMachine;
        }
    }

    public Operator getOperator () {
        synchronized (virtualMachineLock) {
            return operator;
        }
    }

    public void setStarting () {
        setState (STATE_STARTING);
    }

    public synchronized void setAttaching(AbstractDICookie cookie) {
        this.attachingCookie = cookie;
    }

    public void setRunning (VirtualMachine vm, Operator o) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Start - JPDADebuggerImpl.setRunning ()");
            JPDAUtils.printFeatures (logger, vm);
        }
        synchronized (LOCK2) {
            starting = true;
        }
        synchronized (virtualMachineLock) {
            virtualMachine = vm;
            operator = o;
        }
        synchronized (canBeModifiedLock) {
            canBeModified = null; // Reset the can be modified flag
        }

        initGenericsSupport ();
        EditorContextBridge.getContext().createTimeStamp(this);


//        Iterator i = getVirtualMachine ().allThreads ().iterator ();
//        while (i.hasNext ()) {
//            ThreadReference tr = (ThreadReference) i.next ();
//            if (tr.isSuspended ()) {
//                if (startVerbose)
//                    System.out.println("\nS JPDADebuggerImpl.setRunning () - " +
//                        "thread supended"
//                    );
//                setState (STATE_RUNNING);
//                synchronized (LOCK) {
//                    virtualMachine.resume ();
//                }
//                if (startVerbose)
//                    System.out.println("\nS JPDADebuggerImpl.setRunning () - " +
//                        "thread supended - VM resumed - end"
//                    );
//                synchronized (LOCK2) {
//                    LOCK2.notify ();
//                }
//                return;
//            }
//        }

        synchronized (threadsCollectorLock) {
            if (threadsCache != null) {
                threadsCache.setVirtualMachine(vm);
            }
        }

        setState (STATE_RUNNING);
        synchronized (virtualMachineLock) {
            vm = virtualMachine; // re-take the VM, it can be nulled by finish()
        }
        if (vm != null) {
            notifyToBeResumedAll();
            accessLock.writeLock().lock();
            try {
                VirtualMachineWrapper.resume(vm);
            } catch (VMDisconnectedExceptionWrapper e) {
            } catch (InternalExceptionWrapper e) {
            } finally {
                accessLock.writeLock().unlock();
            }
        }

        logger.fine("   JPDADebuggerImpl.setRunning () finished, VM resumed.");
        synchronized (LOCK2) {
            starting = false;
            LOCK2.notifyAll ();
        }
    }

    /**
    * Performs stop action.
    */
    public void setStoppedState (ThreadReference thread, boolean stoppedAll) {
        PropertyChangeEvent evt;
        accessLock.readLock().lock();
        try {
            // this method can be called in stopped state to switch
            // the current thread only
            JPDAThread c = getCurrentThread();
            JPDAThread t = getThread (thread);
            if (!stoppedAll && c != null && c != t && c.isSuspended()) {
                // We already have a suspended current thread, do not switch in that case.
                return ;
            }
            checkJSR45Languages (t);
            evt = setCurrentThreadNoFire(t);
            PropertyChangeEvent evt2 = setStateNoFire(STATE_STOPPED);

            if (evt == null) evt = evt2;
            else if (evt2 != null) {
                PropertyChangeEvent evt3 = evt;
                while(evt3.getPropagationId() != null) evt3 = (PropertyChangeEvent) evt3.getPropagationId();
                evt3.setPropagationId(evt2);
            }
        } finally {
            accessLock.readLock().unlock();
        }
        if (evt != null) {
            do {
                firePropertyChange(evt);
                evt = (PropertyChangeEvent) evt.getPropagationId();
            } while (evt != null);
        }
    }

    /**
     * Can be called if the current thread is resumed after stop.
     */
    public void setRunningState() {
        setState(STATE_RUNNING);
    }

    /**
    * Performs stop action and disable a next call to resume()
    */
    public void setStoppedStateNoContinue (ThreadReference thread) {
        PropertyChangeEvent evt;
        accessLock.readLock().lock();
        try {
            // this method can be called in stopped state to switch
            // the current thread only
            evt = setStateNoFire(STATE_RUNNING);
            JPDAThread t = getThread (thread);
            checkJSR45Languages (t);
            PropertyChangeEvent evt2 = setCurrentThreadNoFire(t);

            if (evt == null) evt = evt2;
            else if (evt2 != null) evt.setPropagationId(evt2);

            evt2 = setStateNoFire(STATE_STOPPED);

            if (evt == null) evt = evt2;
            else if (evt2 != null) {
                PropertyChangeEvent evt3 = evt;
                while(evt3.getPropagationId() != null) evt3 = (PropertyChangeEvent) evt3.getPropagationId();
                evt3.setPropagationId(evt2);
            }

            doContinue = false;
        } finally {
            accessLock.readLock().unlock();
        }
        if (evt != null) {
            do {
                firePropertyChange(evt);
                evt = (PropertyChangeEvent) evt.getPropagationId();
            } while (evt != null);
        }
    }


    private boolean finishing;

    /**
     * Used by KillActionProvider.
     */
    public void finish () {
        try {
            synchronized (this) {
                if (finishing) {
                    // Can easily be called twice - from the operator termination
                    return ;
                }
                finishing = true;
            }
            logger.fine("StartActionProvider.finish ()");
            if (getState () == STATE_DISCONNECTED) return;
            AbstractDICookie di = lookupProvider.lookupFirst(null, AbstractDICookie.class);
            Operator o = getOperator();
            if (o != null) o.stop();
            synchronized (this) {
                if (attachingCookie != null) {
                    if (attachingCookie instanceof ListeningDICookie) {
                        ListeningDICookie listeningCookie = (ListeningDICookie) attachingCookie;
                        try {
                            listeningCookie.getListeningConnector().stopListening(listeningCookie.getArgs());
                        } catch (java.io.IOException ioex) {
                        } catch (com.sun.jdi.connect.IllegalConnectorArgumentsException icaex) {
                        } catch (IllegalArgumentException iaex) {
                        }
                    }
                }
            }
            try {
                waitRunning(); // First wait till the debugger comes up
            } catch (DebuggerStartException dsex) {
                // We do not want to start it anyway when we're finishing - do not bother
            }
            VirtualMachine vm;
            synchronized (virtualMachineLock) {
                vm = virtualMachine;
                virtualMachine = null;
            }
            setState (STATE_DISCONNECTED);
            if (jsr45EngineProviders != null) {
                for (Iterator<JSR45DebuggerEngineProvider> i = jsr45EngineProviders.iterator(); i.hasNext();) {
                    JSR45DebuggerEngineProvider provider = i.next();
                    DebuggerEngine.Destructor d = provider.getDesctuctor();
                    if (d != null) {
                        d.killEngine();
                    }
                }
                jsr45EngineProviders = null;
            }
            DebuggerEngine.Destructor d = javaEngineProvider.getDestructor();
            if (d != null) {
                d.killEngine ();
            }
            if (vm != null) {
                try {
                    if (di instanceof AttachingDICookie) {
                        JPDAThreadImpl t;
                        synchronized (currentThreadAndFrameLock) {
                            t = currentThread;
                        }
                        if (t != null && t.isMethodInvoking()) {
                            try {
                                t.waitUntilMethodInvokeDone(5000); // Wait 5 seconds at most
                            } catch (InterruptedException ex) {}
                        }
                        logger.fine(" StartActionProvider.finish() VM dispose");
                        VirtualMachineWrapper.dispose (vm);
                    } else {
                        logger.fine(" StartActionProvider.finish() VM exit");
                        VirtualMachineWrapper.exit (vm, 0);
                    }
                } catch (InternalExceptionWrapper e) {
                    logger.fine(" StartActionProvider.finish() VM exception " + e);
                } catch (VMDisconnectedExceptionWrapper e) {
                    logger.fine(" StartActionProvider.finish() VM exception " + e);
                    // debugee VM is already disconnected (it finished normally)
                }
            }
            logger.fine (" StartActionProvider.finish() end.");

            //Notify LOCK2 so that no one is waiting forever
            synchronized (LOCK2) {
                starting = false;
                LOCK2.notifyAll ();
            }
            EditorContextBridge.getContext().disposeTimeStamp(this);
        } finally {
            finishing = false; // for safety reasons
        }
    }

    /**
     * Suspends the target virtual machine (if any).
     * Used by PauseActionProvider.
     *
     * @see  com.sun.jdi.ThreadReference#suspend
     */
    public void suspend () {
        VirtualMachine vm;
        synchronized (virtualMachineLock) {
            vm = virtualMachine;
        }
        accessLock.writeLock().lock();
        try {
            if (vm != null) {
                logger.fine("VM suspend");
                try {
                    VirtualMachineWrapper.suspend (vm);
                    // Check the suspended count
                    List<ThreadReference> threads = VirtualMachineWrapper.allThreads(vm);
                    for (ThreadReference t : threads) {
                        try {
                            while (ThreadReferenceWrapper.suspendCount(t) > 1) {
                                ThreadReferenceWrapper.resume(t);
                            }
                        } catch (IllegalThreadStateExceptionWrapper e) {
                        } catch (ObjectCollectedExceptionWrapper e) {
                        } catch (InternalExceptionWrapper e) {
                        }
                    }
                } catch (VMDisconnectedExceptionWrapper e) {
                    return ;
                } catch (InternalExceptionWrapper e) {
                    return ;
                }
            }
            setState (STATE_STOPPED);
        } finally {
            accessLock.writeLock().unlock();
        }
        notifySuspendAll(true, true);
    }

    public List<PropertyChangeEvent> notifySuspendAll(boolean doFire, boolean explicitelyPaused) {
        Collection threads = threadsTranslation.getTranslated();
        List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>(threads.size());
        for (Iterator it = threads.iterator(); it.hasNext(); ) {
            Object threadOrGroup = it.next();
            if (threadOrGroup instanceof JPDAThreadImpl) {
                int status = ((JPDAThreadImpl) threadOrGroup).getState();
                boolean invalid = (status == JPDAThread.STATE_NOT_STARTED ||
                                   status == JPDAThread.STATE_UNKNOWN ||
                                   status == JPDAThread.STATE_ZOMBIE);
                if (!invalid) {
                    try {
                        PropertyChangeEvent event = ((JPDAThreadImpl) threadOrGroup).notifySuspended(doFire, explicitelyPaused);
                        if (event != null) {
                            events.add(event);
                        }
                    } catch (ObjectCollectedException ocex) {
                        invalid = true;
                    }
                } else if (status == JPDAThread.STATE_UNKNOWN || status == JPDAThread.STATE_ZOMBIE) {
                    threadsTranslation.remove(((JPDAThreadImpl) threadOrGroup).getThreadReference());
                }
            }
        }
        return events;
    }

    public void notifySuspendAllNoFire() {
        Collection threads = threadsTranslation.getTranslated();
        for (Iterator it = threads.iterator(); it.hasNext(); ) {
            Object threadOrGroup = it.next();
            if (threadOrGroup instanceof JPDAThreadImpl) {
                int status = ((JPDAThreadImpl) threadOrGroup).getState();
                boolean invalid = (status == JPDAThread.STATE_NOT_STARTED ||
                                   status == JPDAThread.STATE_UNKNOWN ||
                                   status == JPDAThread.STATE_ZOMBIE);
                if (!invalid) {
                    try {
                        ((JPDAThreadImpl) threadOrGroup).notifySuspendedNoFire();
                    } catch (ObjectCollectedException ocex) {
                        invalid = true;
                    }
                } else if (status == JPDAThread.STATE_UNKNOWN || status == JPDAThread.STATE_ZOMBIE) {
                    threadsTranslation.remove(((JPDAThreadImpl) threadOrGroup).getThreadReference());
                }
            }
        }
    }

    /**
     * Used by ContinueActionProvider & StepActionProvider.
     */
    public void resume () {
        accessLock.readLock().lock();
        try {
            if (!doContinue) {
                doContinue = true;
                // Continue the next time and do nothing now.
                return ;
            }
        } finally {
            accessLock.readLock().unlock();
        }
        if (operator.flushStaledEvents()) {
            return ;
        }
        PropertyChangeEvent stateChangeEvent;
        //notifyToBeResumedAll();
        VirtualMachine vm;
        synchronized (virtualMachineLock) {
            vm = virtualMachine;
        }
        if (vm != null) {
            logger.fine("VM resume");
            List<JPDAThread> allThreads = getAllThreads();
            accessLock.writeLock().lock();
            logger.finer("Debugger WRITE lock taken.");
            stateChangeEvent = setStateNoFire(STATE_RUNNING);
            // We must resume only threads which are regularly suspended.
            // Otherwise we may unexpectedly resume threads which just hit an event!
            
            // However, this can not be done right without an atomic resume of a set of threads.
            // Since this functionality is not available in the backend, we will
            // call VirtualMachine.resume() if all threads are suspended
            // (so that the model of of suspend all/resume all works correctly)
            // and call ThreadReference.resume() on individual suspended threads
            // (so that suspend of event thread together with continue works correctly).

            // Other cases (some threads suspended and some running with events suspending all threads)
            // will NOT WORK CORRECTLY. Because while resuming threads one at a time,
            // if the first one hits an event which suspends all, other resumes will resume the
            // suspended threads.
            // But this looks like a reasonable trade-off considering the available functionality.

            List<JPDAThreadImpl> threadsToResume = new ArrayList<JPDAThreadImpl>();
            for (JPDAThread t : allThreads) {
                if (t.isSuspended()) {
                    threadsToResume.add((JPDAThreadImpl) t);
                }
            }
            try {
                for (int i = 0; i < threadsToResume.size(); i++) {
                    JPDAThreadImpl t = threadsToResume.get(i);
                    boolean can = t.cleanBeforeResume();
                    if (!can) {
                        threadsToResume.remove(i);
                        i--;
                    }
                }
                if (allThreads.size() == threadsToResume.size()) {
                    // Resuming all
                    VirtualMachineWrapper.resume(vm);
                    for (JPDAThreadImpl t : threadsToResume) {
                        t.setAsResumed();
                    }
                    logger.finer("All threads resumed.");
                } else {
                    for (JPDAThreadImpl t : threadsToResume) {
                        t.resumeAfterClean();
                        t.setAsResumed();
                    }
                }
            } catch (VMDisconnectedExceptionWrapper e) {
            } catch (InternalExceptionWrapper e) {
            } finally {
                accessLock.writeLock().unlock();
                logger.finer("Debugger WRITE lock released.");
                if (stateChangeEvent != null) {
                    firePropertyChange(stateChangeEvent);
                }
                for (JPDAThreadImpl t : threadsToResume) {
                    try {
                        t.fireAfterResume();
                    } catch (ThreadDeath td) {
                        throw td;
                    } catch (Throwable th) {
                        Exceptions.printStackTrace(th);
                    }
                }
            }
        }
    }

    /** DO NOT CALL FROM ANYWHERE BUT JPDAThreadImpl.resume(). */
    public boolean currentThreadToBeResumed() {
        accessLock.readLock().lock();
        try {
            if (!doContinue) {
                doContinue = true;
                // Continue the next time and do nothing now.
                return false;
            }
        } finally {
            accessLock.readLock().unlock();
        }
        if (operator.flushStaledEvents()) {
            return false;
        }
        setState (STATE_RUNNING);
        return true;
    }

    public void resumeCurrentThread() {
        accessLock.readLock().lock();
        try {
            if (!doContinue) {
                doContinue = true;
                // Continue the next time and do nothing now.
                return ;
            }
        } finally {
            accessLock.readLock().unlock();
        }
        if (operator.flushStaledEvents()) {
            return ;
        }
        setState (STATE_RUNNING);
        currentThread.resume();
    }

    public void notifyToBeResumedAll() {
        Collection threads = threadsTranslation.getTranslated();
        for (Iterator it = threads.iterator(); it.hasNext(); ) {
            Object threadOrGroup = it.next();
            if (threadOrGroup instanceof JPDAThreadImpl) {
                int status = ((JPDAThreadImpl) threadOrGroup).getState();
                boolean invalid = (status == JPDAThread.STATE_NOT_STARTED ||
                                   status == JPDAThread.STATE_UNKNOWN ||
                                   status == JPDAThread.STATE_ZOMBIE);
                if (!invalid) {
                    ((JPDAThreadImpl) threadOrGroup).notifyToBeResumed();
                } else if (status == JPDAThread.STATE_UNKNOWN || status == JPDAThread.STATE_ZOMBIE) {
                    threadsTranslation.remove(((JPDAThreadImpl) threadOrGroup).getThreadReference());
                }
            }
        }
    }

    public void notifyToBeResumedAllNoFire() {
        Collection threads = threadsTranslation.getTranslated();
        for (Iterator it = threads.iterator(); it.hasNext(); ) {
            Object threadOrGroup = it.next();
            if (threadOrGroup instanceof JPDAThreadImpl) {
                int status = ((JPDAThreadImpl) threadOrGroup).getState();
                boolean invalid = (status == JPDAThread.STATE_NOT_STARTED ||
                                   status == JPDAThread.STATE_UNKNOWN ||
                                   status == JPDAThread.STATE_ZOMBIE);
                if (!invalid) {
                    ((JPDAThreadImpl) threadOrGroup).notifyToBeResumedNoFire();
                } else if (status == JPDAThread.STATE_UNKNOWN || status == JPDAThread.STATE_ZOMBIE) {
                    threadsTranslation.remove(((JPDAThreadImpl) threadOrGroup).getThreadReference());
                }
            }
        }
    }

    public ThreadsCache getThreadsCache() {
        synchronized (threadsCollectorLock) {
            if (threadsCache == null) {
                threadsCache = new ThreadsCache(this);
                threadsCache.addPropertyChangeListener(new PropertyChangeListener() {
                    //  Re-fire the changes
                    public void propertyChange(PropertyChangeEvent evt) {
                        String propertyName = evt.getPropertyName();
                        if (ThreadsCache.PROP_THREAD_STARTED.equals(propertyName)) {
                            firePropertyChange(PROP_THREAD_STARTED, null, getThread((ThreadReference) evt.getNewValue()));
                        }
                        if (ThreadsCache.PROP_THREAD_DIED.equals(propertyName)) {
                            firePropertyChange(PROP_THREAD_DIED, getThread((ThreadReference) evt.getOldValue()), null);
                        }
                        if (ThreadsCache.PROP_GROUP_ADDED.equals(propertyName)) {
                            firePropertyChange(PROP_THREAD_GROUP_ADDED, null, getThreadGroup((ThreadGroupReference) evt.getNewValue()));
                        }
                    }
                });
            }
            return threadsCache;
        }
    }

    List<JPDAThread> getAllThreads() {
        ThreadsCache tc = getThreadsCache();
        if (tc == null) {
            return Collections.emptyList();
        }
        List<ThreadReference> threadList = tc.getAllThreads();
        int n = threadList.size();
        List<JPDAThread> threads = new ArrayList<JPDAThread>(n);
        for (int i = 0; i < n; i++) {
            threads.add(getThread(threadList.get(i)));
        }
        return Collections.unmodifiableList(threads);
    }

    public JPDAThreadGroup[] getTopLevelThreadGroups() {
        ThreadsCache tc = getThreadsCache();
        if (tc == null) {
            return new JPDAThreadGroup[0];
        }
        List<ThreadGroupReference> groupList = tc.getTopLevelThreadGroups();
        JPDAThreadGroup[] groups = new JPDAThreadGroup[groupList.size()];
        for (int i = 0; i < groups.length; i++) {
            groups[i] = getThreadGroup((ThreadGroupReference) groupList.get(i));
        }
        return groups;
    }

    public JPDAThreadImpl getThread (ThreadReference tr) {
        return (JPDAThreadImpl) threadsTranslation.translate (tr);
    }

    public JPDAThreadImpl getExistingThread (ThreadReference tr) {
        return (JPDAThreadImpl) threadsTranslation.translateExisting(tr);
    }

    public JPDAThreadGroup getThreadGroup (ThreadGroupReference tgr) {
        return (JPDAThreadGroup) threadsTranslation.translate (tgr);
    }

    public Variable getLocalVariable(LocalVariable lv, Value v) {
        return (Variable) localsTranslation.translate(lv, v);
    }

    public JPDAClassType getClassType(ReferenceType cr) {
        return (JPDAClassType) localsTranslation.translate (cr);
    }

    public Variable getVariable (Value value) {
        return getLocalsTreeModel ().getVariable (value);
    }

    public void markObject(ObjectVariable var, String label) {
        synchronized (markedObjects) {
            long uid = var.getUniqueID();
            String oldLabel = markedObjects.remove(uid);
            if (oldLabel != null) {
                markedObjectLabels.remove(oldLabel);
            }
            if (label != null) {
                markedObjects.put(uid, label);
                ObjectVariable markedVar = markedObjectLabels.get(label);
                if (markedVar != null) {
                    markedObjects.remove(markedVar.getUniqueID());
                }
                markedObjectLabels.put(label, var);
            }
        }
    }

    /*public Map<ObjectVariable, String> getMarkedObjects() {
        Map<ObjectVariable, String> map;
        synchronized (markedObjects) {
            map = new LinkedHashMap<ObjectVariable, String>(markedObjects);
        }
        return map;
    }*/

    public String getLabel(ObjectVariable var) {
        synchronized (markedObjects) {
            return markedObjects.get(var.getUniqueID());
        }
    }

    public ObjectVariable getLabeledVariable(String label) {
        synchronized (markedObjects) {
            return markedObjectLabels.get(label);
        }
    }

    public ExpressionPool getExpressionPool() {
        return expressionPool;
    }

    synchronized void setSingleThreadStepResumeDecision(Boolean decision) {
        singleThreadStepResumeDecision = decision;
    }

    synchronized Boolean getSingleThreadStepResumeDecision() {
        return singleThreadStepResumeDecision;
    }

    public synchronized void setStepInterruptByBptResumeDecision(Boolean decision) {
        stepInterruptByBptResumeDecision = decision;
    }

    public synchronized Boolean getStepInterruptByBptResumeDecision() {
        return stepInterruptByBptResumeDecision;
    }


    // private helper methods ..................................................

    private static final java.util.regex.Pattern jvmVersionPattern =
            java.util.regex.Pattern.compile ("(\\d+)\\.(\\d+)\\.(\\d+)(_\\d+)?(-\\w+)?");
    private static java.lang.reflect.Method  tcGenericSignatureMethod;
    private static java.lang.reflect.Method  lvGenericSignatureMethod;


    private void initGenericsSupport () {
        tcGenericSignatureMethod = null;
        if (Bootstrap.virtualMachineManager ().minorInterfaceVersion () >= 5) {
            VirtualMachine vm;
            synchronized (virtualMachineLock) {
                vm = virtualMachine;
            }
            if (vm == null) return ;
            try {
                java.util.regex.Matcher m = jvmVersionPattern.matcher(VirtualMachineWrapper.version(vm));
                if (m.matches ()) {
                    int minor = Integer.parseInt (m.group (2));
                    if (minor >= 5) {
                        try {
                            tcGenericSignatureMethod = TypeComponent.class.
                                getMethod ("genericSignature", new Class [0]);
                            lvGenericSignatureMethod = LocalVariable.class.
                                getMethod ("genericSignature", new Class [0]);
                        } catch (NoSuchMethodException e) {
                            // the method is not available, ignore generics
                        }
                    }
                }
            } catch (InternalExceptionWrapper e) {
            } catch (VMDisconnectedExceptionWrapper e) {
            }
        }
    }

    private PropertyChangeEvent setStateNoFire (int state) {
        int o;
        synchronized (stateLock) {
            if (state == this.state) return null;
            o = this.state;
            this.state = state;
        }
        //PENDING HACK see issue 46287
        System.setProperty(
            "org.openide.awt.SwingBrowserImpl.do-not-block-awt",
            String.valueOf (state != STATE_DISCONNECTED)
        );
        return new PropertyChangeEvent(this, PROP_STATE, new Integer (o), new Integer (state));
    }

    private void setState (int state) {
        if (state == STATE_RUNNING) {
            synchronized (currentThreadAndFrameLock) {
                currentCallStackFrame = null;
            }
        }
        PropertyChangeEvent evt = setStateNoFire(state);
        if (evt != null) {
            firePropertyChange(evt);
        }
    }

    /**
     * Fires property change.
     */
    private void firePropertyChange (String name, Object o, Object n) {
        pcs.firePropertyChange (name, o, n);
        //System.err.println("ALL Change listeners count = "+pcs.getPropertyChangeListeners().length);
    }

    /**
     * Fires property change.
     */
    private void firePropertyChange (PropertyChangeEvent evt) {
        pcs.firePropertyChange (evt);
        //System.err.println("ALL Change listeners count = "+pcs.getPropertyChangeListeners().length);
    }

    private SourcePath engineContext;
    public synchronized SourcePath getEngineContext () {
        if (engineContext == null)
            engineContext = lookupProvider.lookupFirst(null, SourcePath.class);
        return engineContext;
    }

    private LocalsTreeModel localsTreeModel;
    private LocalsTreeModel getLocalsTreeModel () {
        if (localsTreeModel == null)
            localsTreeModel = (LocalsTreeModel) lookupProvider.
                lookupFirst ("LocalsView", TreeModel.class);
        return localsTreeModel;
    }

    private ThreadReference getEvaluationThread () {
        synchronized (currentThreadAndFrameLock) {
            if (currentThread != null) return currentThread.getThreadReference ();
        }
        VirtualMachine vm;
        synchronized (virtualMachineLock) {
            vm = virtualMachine;
        }
        if (vm == null) return null;
        List l;
        try {
            l = VirtualMachineWrapper.allThreads(vm);
        } catch (InternalExceptionWrapper ex) {
            return null;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return null;
        }
        if (l.size () < 1) return null;
        int i, k = l.size ();
        ThreadReference thread = null;
        for (i = 0; i < k; i++) {
            ThreadReference t = (ThreadReference) l.get (i);
            try {
                if (ThreadReferenceWrapper.isSuspended (t)) {
                    thread = t;
                    if (ThreadReferenceWrapper.name (t).equals ("Finalizer"))
                        return t;
                }
            } catch (InternalExceptionWrapper ex) {
            } catch (IllegalThreadStateExceptionWrapper ex) {
            } catch (ObjectCollectedExceptionWrapper ex) {
            } catch (VMDisconnectedExceptionWrapper ex) {
            }
        }
        return thread;
    }

    private void updateCurrentCallStackFrame (JPDAThread thread) {
        if ((thread == null) || (thread.getStackDepth () < 1)) {
            setCurrentCallStackFrame (null);
        } else {
            try {
                setCurrentCallStackFrame (thread.getCallStack (0, 1) [0]);
            } catch (AbsentInformationException e) {
                setCurrentCallStackFrame (null);
            }
        }
    }

    private CallStackFrame getTopFrame(JPDAThread thread) {
        CallStackFrame callStackFrame;
        if ((thread == null) || (thread.getStackDepth () < 1)) {
            callStackFrame = null;
        } else {
            try {
                callStackFrame = thread.getCallStack(0, 1) [0];
            } catch (AbsentInformationException e) {
                callStackFrame = null;
            }
        }
        return callStackFrame;
    }

    /**
     * @param thread The thread to take the top frame from
     * @return A PropertyChangeEvent or <code>null</code>.
     */
    private PropertyChangeEvent updateCurrentCallStackFrameNoFire(CallStackFrame callStackFrame) {
        CallStackFrame old;
        old = setCurrentCallStackFrameNoFire(callStackFrame);
        if (old == callStackFrame) return null;
        else return new PropertyChangeEvent(this, PROP_CURRENT_CALL_STACK_FRAME,
                                            old, callStackFrame);
    }

    private List<EventRequest> disableAllBreakpoints () throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        logger.fine ("disableAllBreakpoints() start.");
        List<EventRequest> l = new ArrayList<EventRequest>();
        VirtualMachine vm = getVirtualMachine ();
        if (vm == null) return l;
        EventRequestManager erm = VirtualMachineWrapper.eventRequestManager (vm);
        l.addAll (EventRequestManagerWrapper.accessWatchpointRequests (erm));
        l.addAll (EventRequestManagerWrapper.breakpointRequests (erm));
        l.addAll (EventRequestManagerWrapper.classPrepareRequests (erm));
        l.addAll (EventRequestManagerWrapper.classUnloadRequests (erm));
        l.addAll (EventRequestManagerWrapper.exceptionRequests (erm));
        l.addAll (EventRequestManagerWrapper.methodEntryRequests (erm));
        l.addAll (EventRequestManagerWrapper.methodExitRequests (erm));
        l.addAll (EventRequestManagerWrapper.modificationWatchpointRequests (erm));
//        l.addAll (erm.stepRequests ());
        l.addAll (EventRequestManagerWrapper.threadDeathRequests (erm));
        l.addAll (EventRequestManagerWrapper.threadStartRequests (erm));
        int i = l.size () - 1;
        for (; i >= 0; i--)
            if (!EventRequestWrapper.isEnabled (l.get (i)))
                l.remove (i);
            else
                EventRequestWrapper.disable (l.get (i));
        operator.breakpointsDisabled();
        logger.fine ("disableAllBreakpoints() end.");
        return l;
    }

    private void enableAllBreakpoints (List<EventRequest> l) {
        logger.fine ("enableAllBreakpoints() start.");
        operator.breakpointsEnabled();
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            try {
                EventRequestWrapper.enable (l.get (i));
            } catch (IllegalThreadStateException ex) {
                // see #53163
                // this can occurr if there is some "old" StepRequest and
                // thread named in the request has died
            } catch (ObjectCollectedExceptionWrapper ocex) {
                // Something in the request was collected.
            } catch (InvalidRequestStateException ex) {
                // workaround for #51176
            } catch (InternalExceptionWrapper ex) {
            } catch (VMDisconnectedExceptionWrapper ex) {
                return ;
            }
        logger.fine ("enableAllBreakpoints() end.");
    }

    private void checkJSR45Languages (JPDAThread t) {
        if (t.getStackDepth () > 0)
            try {
                CallStackFrame[] frames = t.getCallStack (0, 1);
                if (frames.length < 1) return ; // Internal error or disconnected
                CallStackFrame f = frames [0];
                List<String> l = f.getAvailableStrata ();
                String stratum = f.getDefaultStratum ();
                //String sourceDebugExtension;
                    //sourceDebugExtension = (String) f.getClass().getMethod("getSourceDebugExtension").invoke(f);
                if (l.size() == 1 && "Java".equals(l.get(0))) {     // NOI18N
                    // Hack for non-Java languages that do not define stratum:
                    String sourceName = f.getSourceName(null);
                    int ext = sourceName.lastIndexOf('.');
                    if (ext > 0) {
                        String extension = sourceName.substring(++ext);
                        extension = extension.toUpperCase();
                        if (!"JAVA".equals(extension)) {    // NOI18N
                            l = Collections.singletonList(extension);
                            stratum = extension;
                        }
                    }
                }
                int i, k = l.size ();
                for (i = 0; i < k; i++) {
                    if (!languages.contains (l.get (i))) {
                        String language = l.get (i);
                        DebuggerManager.getDebuggerManager ().startDebugging (
                            createJSR45DI (language)
                        );
                        languages.add (language);
                    }
                } // for
                if ( (stratum != null) &&
                     (!stratum.equals (lastStratumn))
                )
                    javaEngineProvider.getSession ().setCurrentLanguage (stratum);
                lastStratumn = stratum;
            } catch (AbsentInformationException e) {
            }
    }

    private Set<JSR45DebuggerEngineProvider> jsr45EngineProviders;

    private DebuggerInfo createJSR45DI (final String language) {
        if (jsr45EngineProviders == null) {
            jsr45EngineProviders = new HashSet<JSR45DebuggerEngineProvider>(1);
        }
        JSR45DebuggerEngineProvider provider = new JSR45DebuggerEngineProvider(language);
        jsr45EngineProviders.add(provider);
        return DebuggerInfo.create (
            "netbeans-jpda-JSR45DICookie-" + language,
            new Object[] {
                new DelegatingSessionProvider () {
                    public Session getSession (
                        DebuggerInfo debuggerInfo
                    ) {
                        return javaEngineProvider.getSession ();
                    }
                },
                provider
            }
        );
    }

    public JPDAStep createJPDAStep(int size, int depth) {
        Session session = lookupProvider.lookupFirst(null, Session.class);
        return new JPDAStepImpl(this, session, size, depth);
    }

    /*public synchronized Heap getHeap() {
        if (virtualMachine != null && canGetInstanceInfo(virtualMachine)) {
            return new HeapImpl(virtualMachine);
        } else {
            return null;
        }
    }*/

    public List<JPDAClassType> getAllClasses() {
        List<ReferenceType> classes;
        synchronized (virtualMachineLock) {
            if (virtualMachine == null) {
                classes = Collections.emptyList();
            } else {
                classes = VirtualMachineWrapper.allClasses0(virtualMachine);
            }
        }
        return new ClassTypeList(this, classes);
    }

    public List<JPDAClassType> getClassesByName(String name) {
        List<ReferenceType> classes;
        synchronized (virtualMachineLock) {
            if (virtualMachine == null) {
                classes = Collections.emptyList();
            } else {
                classes = VirtualMachineWrapper.classesByName0(virtualMachine, name);
            }
        }
        return new ClassTypeList(this, classes);
    }

    @Override
    public long[] getInstanceCounts(List<JPDAClassType> classTypes) throws UnsupportedOperationException {
            VirtualMachine vm;
            synchronized (virtualMachineLock) {
                vm = virtualMachine;
            }
            if (vm == null) {
                return new long[classTypes.size()];
            }
            List<ReferenceType> types;
            if (classTypes instanceof ClassTypeList) {
                ClassTypeList cl = (ClassTypeList) classTypes;
                types = cl.getTypes();
            } else {
                types = new ArrayList<ReferenceType>(classTypes.size());
                for (JPDAClassType clazz : classTypes) {
                    types.add(((JPDAClassTypeImpl) clazz).getType());
                }
            }
            try {
                return VirtualMachineWrapper.instanceCounts(vm, types);
            } catch (InternalExceptionWrapper e) {
                return new long[classTypes.size()];
            } catch (VMDisconnectedExceptionWrapper e) {
                return new long[classTypes.size()];
            }
    }

    public boolean canGetInstanceInfo() {
        synchronized (virtualMachineLock) {
            return virtualMachine != null && virtualMachine.canGetInstanceInfo();
        }
    }

    @Override
    public ThreadsCollectorImpl getThreadsCollector() {
        synchronized (threadsCollectorLock) {
            if (threadsCollector == null) {
                threadsCollector = new ThreadsCollectorImpl(this);
            }
            return threadsCollector;
        }
    }

    DeadlockDetector getDeadlockDetector() {
        synchronized (threadsCollectorLock) {
            if (deadlockDetector == null) {
                deadlockDetector = new DeadlockDetectorImpl(this);
            }
            return deadlockDetector;
        }
    }

    private static class DebuggerReentrantReadWriteLock extends ReentrantReadWriteLock {

        private ReadLock readerLock;
        private WriteLock writerLock;

        public DebuggerReentrantReadWriteLock(boolean fair) {
            super(fair);
            readerLock = new DebuggerReadLock(this);
            writerLock = new DebuggerWriteLock(this);
        }

        @Override
        public ReadLock readLock() {
            return readerLock;
        }

        @Override
        public WriteLock writeLock() {
            return writerLock;
        }

        private static class DebuggerReadLock extends ReentrantReadWriteLock.ReadLock {

            protected DebuggerReadLock(ReentrantReadWriteLock lock) {
                super(lock);
            }

            @Override
            public void lock() {
                assert !EventQueue.isDispatchThread() : "Debugger lock taken in AWT Event Queue!";
                super.lock();
            }

        }

        private static class DebuggerWriteLock extends ReentrantReadWriteLock.WriteLock {

            protected DebuggerWriteLock(ReentrantReadWriteLock lock) {
                super(lock);
            }

            @Override
            public void lock() {
                assert !EventQueue.isDispatchThread() : "Debugger lock taken in AWT Event Queue!";
                super.lock();
            }

        }

    }

}
