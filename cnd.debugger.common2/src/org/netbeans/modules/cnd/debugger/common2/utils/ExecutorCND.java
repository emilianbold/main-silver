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

package org.netbeans.modules.cnd.debugger.common2.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingUtilities;

import org.openide.util.Exceptions;

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.openide.ErrorManager;

import org.netbeans.modules.cnd.debugger.common2.debugger.io.TermComponent;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.PathUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.api.util.Signal;

/* package */ class ExecutorCND extends Executor {
    private NativeProcess engineProc;
    private int pid = -1;
    private final ExecutionEnvironment exEnv;

    public ExecutorCND(String name, Host host) {
	super(name, host);
        exEnv = host.executionEnvironment();
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return exEnv;
    }

    public boolean isAlive() {
        try {
            engineProc.exitValue();
        } catch (IllegalThreadStateException x) {
            return true;
        }
        return false;
    }

    public void terminate() throws IOException {
        engineProc.destroy();	// On unix this sends a SIGTERM
    }

    /*
     * Interrupt an arbitrary process with SIGINT
     */
    public void interrupt(int pid) throws IOException {
        try {
            CommonTasksSupport.sendSignal(exEnv, pid, Signal.SIGINT, null).get();
        } catch (InterruptedException ex) {
        } catch (ExecutionException ex) {
        }
    }

    public void interruptGroup() throws IOException {
	try {
            CommonTasksSupport.sendSignalGrp(exEnv, this.pid, Signal.SIGINT, null).get();
        } catch (InterruptedException ex) {
        } catch (ExecutionException ex) {
        }
    }

    public void sigqueue(int sig, int data) throws IOException {
	try {
            CommonTasksSupport.sigqueue(exEnv, this.pid, sig, data, null).get();
        } catch (InterruptedException ex) {
        } catch (ExecutionException ex) {
        }
    }

    public synchronized int startShellCmd(String cmd_argv[]) {
	NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(exEnv);

        String argv[] = new String[cmd_argv.length-1];
	//argv[0] = enginePath;
	for (int cx = 1; cx < cmd_argv.length; cx++) {
	    argv[cx-1] = cmd_argv[cx];
	}

        npb.setExecutable(cmd_argv[0]);
        npb.setArguments(argv);
        npb.setUsePty(true);

        // Set env variable, otherwise xstart does not find some tools
        // OLD (see 6986489):
	// npb.getEnvironment().put("_ST_GLUE_SM_PATH", host().getRemoteStudioLocation() + "/prod/lib"); //NOI18N

        try {
            engineProc = npb.call();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        
        try {
            pid = engineProc.getPID();
            return pid;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        return 1;
        }
    }

    public synchronized int startEngine(String enginePath,
					String engine_argv[], Map<String, String> additionalEnv,
			                TermComponent console,
                                        boolean usePty,
                                        boolean disableEcho) {

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(exEnv);

	String argv[] = new String[engine_argv.length-1];
	//argv[0] = enginePath;
	for (int cx = 1; cx < engine_argv.length; cx++) {
	    argv[cx-1] = engine_argv[cx];
	}

        npb.setExecutable(enginePath);
        npb.setArguments(argv);
        
        if (usePty) {
            npb.setUsePty(true);
            npb.getEnvironment().put("TERM", console.getTerm().getEmulation()); // NOI18N
        }

        try {
            engineProc = npb.call();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
            return 0;
        }

        // npb.call() may fail because of some Exception
        // in this case proc's state is ERROR and cause can be read from it's
        // error stream...
        if (engineProc.getState() == NativeProcess.State.ERROR) {
            try {
                String cause = ProcessUtils.readProcessErrorLine(engineProc);
                ErrorManager.getDefault().notify(new Exception(cause));
            } catch (IOException ex) {
            }

            // TODO: the only place this return value is analyzed is start2() of
            // org.netbeans.modules.cnd.debugger.gdb2.Gdb and it is compated 
            // with 0 to identify an error...
            return 0;
        }

        PtySupport.connect(console.getIO(), engineProc);

        if (disableEcho) {
            PtySupport.disableEcho(exEnv, PtySupport.getTTY(engineProc));
        }

	//startMonitor();
        
        try {
            pid = engineProc.getPID();
            return pid;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return 0;
        }
    }

    public String getStartError() {
	return null;
    }

    protected int waitForEngine() throws InterruptedException {
	if (engineProc == null) {
	    return -1;
        }
	return engineProc.waitFor();
    }

    @Override
    protected void destroyEngine() {
	if (engineProc == null) {
	    return;
        }
	super.destroyEngine();
	engineProc.destroy();
    }

    @Override
    public void reap() {
	Thread reaper = new Thread() {
	    @Override
	    public void run() {
		setName("ExecutorCND Reaper"); // NOI18N
		try {
		    engineProc.waitFor();
		} catch (InterruptedException ex) {
		    Exceptions.printStackTrace(ex);
		}
		int exitValue = engineProc.exitValue();
		if (Log.Executor.debug) {
		    // Normal termination is done using SIGTERM which gives us
		    // an exitValue of 1 instead of 0.
		    if (exitValue != 0) {
			List<String> processError = null;
			List<String> processOutput = null;
			try {
			    processError = ProcessUtils.readProcessError(engineProc);
			    processOutput = ProcessUtils.readProcessOutput(engineProc);
			} catch (IOException ex) {
			    Exceptions.printStackTrace(ex);
			}

			String output = "";
			output += String.format("Process exited with %d.\n", exitValue); // NOI18N
			output += String.format("Output:\n"); // NOI18N
			if (processOutput == null) {
			    output += "\t<empty>\n"; // NOI18N
			} else {
			    for (String l : processOutput)
				output += l + "\n"; // NOI18N
			}
			output += String.format("Error:\n"); // NOI18N
			if (processError == null) {
			    output += "\t<empty>\n"; // NOI18N
			}  else {
			    for (String l : processError)
				output += l + "\n"; // NOI18N
			}

			final String foutput = output;
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
				IpeUtils.postError(foutput);
			    }
			});
		    }
		}
	    }
	};
	reaper.start();
    }

    public String readlink(long pid) {
        return PathUtils.getExePath(pid, exEnv);
    }

    public boolean is_64(String filep) {
	ExitStatus status = ProcessUtils.execute(exEnv, "/usr/bin/file", filep); //NOI18N
        return status.output.contains(" 64"); //NOI18N
    }
	      
    public InputStream getInputStream() {
	return engineProc.getInputStream();
    }

    public OutputStream getOutputStream() {
	return engineProc.getOutputStream();
    }

    @Override
    public boolean svc_is64() {
        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(exEnv);
            return hostInfo.getOS().getBitness() == HostInfo.Bitness._64;
        } catch (CancellationException ex) {
        } catch (IOException ex) {
        }
        return false;
    }
}
