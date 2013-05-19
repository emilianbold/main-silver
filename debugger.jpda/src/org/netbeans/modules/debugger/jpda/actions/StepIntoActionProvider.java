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
package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ui.MethodChooser;


/**
 * Implements non visual part of stepping through code in JPDA debugger.
 * It supports standard debugging actions StepInto, Over, Out, RunToCursor, 
 * and Go. And advanced "smart tracing" action.
 *
 * @author  Jan Jancura
 */
@ActionsProvider.Registration(path="netbeans-JPDASession", actions={"stepInto"})
public class StepIntoActionProvider extends JPDADebuggerActionProvider {
    
    public static final String SS_STEP_OUT = "SS_ACTION_STEPOUT";
    public static final String ACTION_SMART_STEP_INTO = "smartStepInto";

    private StepIntoNextMethod stepInto;
    private MethodChooser currentMethodChooser;

    static final Map<ContextProvider, Reference<StepIntoActionProvider>> instanceByContext
            = new WeakHashMap<ContextProvider, Reference<StepIntoActionProvider>>();

    public StepIntoActionProvider (ContextProvider contextProvider) {
        super (
            (JPDADebuggerImpl) contextProvider.lookupFirst 
                (null, JPDADebugger.class)
        );
        stepInto = new StepIntoNextMethod(contextProvider);
        setProviderToDisableOnLazyAction(this);
        instanceByContext.put(contextProvider, new WeakReference(this));
    }


    // ActionProviderSupport ...................................................
    
    @Override
    public Set getActions () {
        return new HashSet<Object>(Arrays.asList (new Object[] {
            ActionsManager.ACTION_STEP_INTO,
        }));
    }
    
    @Override
    public void doAction (Object action) {
        runAction(action, true, null);
    }
    
    @Override
    public void postAction(final Object action, final Runnable actionPerformedNotifier) {
        doLazyAction(action, new Runnable() {
            @Override
            public void run() {
                try {
                    runAction(action, true, null);
                } finally {
                    actionPerformedNotifier.run();
                }
            }
        });
    }
    
    public void runAction(Object action, boolean doResume, Lock lock) {
        if (ActionsManager.ACTION_STEP_INTO.equals(action) && doMethodSelection()) {
            return; // action performed
        }
        stepInto.runAction(action, doResume, lock);
    }
    
    @Override
    protected void checkEnabled (int debuggerState) {
        Iterator i = getActions ().iterator ();
        while (i.hasNext ())
            setEnabled (
                i.next (),
                (debuggerState == JPDADebugger.STATE_STOPPED) &&
                (getDebuggerImpl ().getCurrentThread () != null)
            );
    }
    
    // other methods ...........................................................
    
    public boolean doMethodSelection () {
        synchronized (this) {
            if (currentMethodChooser != null) {
                // perform action
                currentMethodChooser.releaseUI(true);
                return true;
            }
        }
        final String[] methodPtr = new String[1];
        final String[] urlPtr = new String[1];
        final int[] linePtr = new int[1];
        boolean retrieved = retievePosition(methodPtr, urlPtr, linePtr);
        if (!retrieved) {
            return false;
        }
        final int methodLine = linePtr[0];
        final String url = urlPtr[0];
        if (methodLine < 0 || url == null || !url.endsWith (".java")) {
            return false;
        }
        JPDAThreadImpl ct = (JPDAThreadImpl) debugger.getCurrentThread();
        ThreadReference threadReference = ct.getThreadReference();
        // Find the class the thread is stopped at
        ReferenceType clazz = null;
        try {
            if (ThreadReferenceWrapper.frameCount(threadReference) < 1) return false;
            clazz = LocationWrapper.declaringType(
                    StackFrameWrapper.location(ThreadReferenceWrapper.frame(threadReference, 0)));
        } catch (InternalExceptionWrapper ex) {
        } catch (ObjectCollectedExceptionWrapper ex) {
        } catch (InvalidStackFrameExceptionWrapper ex) {
        } catch (IncompatibleThreadStateException ex) {
        } catch (IllegalThreadStateExceptionWrapper ex) {
            // Thrown when thread has exited
        } catch (VMDisconnectedExceptionWrapper ex) {
        }
        if (clazz != null) {
            if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED) {
                return false;
            }
            final MethodChooserSupport cSupport = new MethodChooserSupport(debugger, url, clazz, methodLine);
            boolean continuedDirectly = cSupport.init();
            if (cSupport.getSegmentsCount() == 0) {
                return false;
            }
            if (continuedDirectly) {
                return true;
            }
            MethodChooser.ReleaseListener releaseListener = new MethodChooser.ReleaseListener() {
                @Override
                public void released(boolean performAction) {
                    synchronized (StepIntoActionProvider.this) {
                        currentMethodChooser = null;
                        cSupport.tearDown();
                        if (performAction) {
                            cSupport.doStepInto();
                        }
                    }
                }
            };
            MethodChooser chooser = cSupport.createChooser();
            chooser.addReleaseListener(releaseListener);
            boolean success = chooser.showUI();
            if (success && chooser.isUIActive()) {
                synchronized (this) {
                    cSupport.tearUp(chooser);
                    currentMethodChooser = chooser;
                }
            } else {
                chooser.removeReleaseListener(releaseListener);
            }
            return success;
        } else {
            return false;
        }
    }

    private boolean retievePosition(String[] methodPtr, String[] urlPtr, int[] linePtr) {
        JPDAThread t = debugger.getCurrentThread();
        String stratum = debugger.getSession().getCurrentLanguage();
        int lineNumber = t.getLineNumber (stratum);
        if (lineNumber < 1) {
            return false;
        }
        linePtr[0] = lineNumber;
        String sourcePath;
        try {
            sourcePath = SourcePath.convertSlash (t.getSourcePath (stratum));
        } catch (AbsentInformationException e) {
            sourcePath = SourcePath.convertClassNameToRelativePath (t.getClassName ());
        }
        String url = debugger.getEngineContext().getURL (sourcePath, true);
        if (url == null) {
            return false;
        }
        urlPtr[0] = url;
        methodPtr[0] = t.getMethodName();
        return true;
    }
    
}
