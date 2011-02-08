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

package org.netbeans.modules.cnd.debugger.gdb2;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionClient;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.openide.text.Line;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;

import org.netbeans.modules.cnd.debugger.common2.utils.Executor;
import org.netbeans.modules.cnd.debugger.common2.utils.ItemSelectorResult;
import org.netbeans.modules.cnd.debugger.common2.utils.StopWatch;
import org.netbeans.modules.cnd.debugger.gdb2.actions.GdbStartActionProvider;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.Handler;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.HandlerExpert;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.HandlerCommand;

import org.netbeans.modules.cnd.debugger.common2.debugger.io.IOPack;

import org.netbeans.modules.cnd.debugger.common2.debugger.options.DbgProfile;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DebuggerOption;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.Signals;

import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerImpl;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerInfo;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.Location;
import org.netbeans.modules.cnd.debugger.common2.debugger.Thread;
import org.netbeans.modules.cnd.debugger.common2.debugger.Frame;
import org.netbeans.modules.cnd.debugger.common2.debugger.Variable;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeWatch;
import org.netbeans.modules.cnd.debugger.common2.debugger.WatchVariable;
import org.netbeans.modules.cnd.debugger.common2.debugger.WatchModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.LocalModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.StackModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.ThreadModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.VarContinuation;
import org.netbeans.modules.cnd.debugger.common2.debugger.EvaluationWindow;
import org.netbeans.modules.cnd.debugger.common2.debugger.Error;
import org.netbeans.modules.cnd.debugger.common2.debugger.EvalAnnotation;
import org.netbeans.modules.cnd.debugger.common2.debugger.RoutingToken;
import org.netbeans.modules.cnd.debugger.common2.debugger.SignalDialog;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointManager.BreakpointMsg;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointManager.BreakpointOp;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointManager.BreakpointPlan;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointProvider;

import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.Controller;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.DisFragModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.DisassemblerWindow;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.RegistersWindow;

import org.netbeans.modules.cnd.debugger.gdb2.mi.MICommand;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIRecord;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIResult;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MITList;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIUserInteraction;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIValue;

import org.netbeans.modules.cnd.debugger.common2.capture.ExternalStartManager;
import org.netbeans.modules.cnd.debugger.common2.capture.ExternalStart;
import org.netbeans.modules.cnd.debugger.common2.debugger.Address;
import org.netbeans.modules.cnd.debugger.common2.debugger.MacroSupport;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Platform;
import org.netbeans.modules.cnd.debugger.common2.utils.FileMapper;
import org.netbeans.modules.cnd.debugger.common2.utils.InfoPanel;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIConst;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MITListItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;

    public final class GdbDebuggerImpl extends NativeDebuggerImpl 
    implements BreakpointProvider, Gdb.Factory.Listener {

    private GdbEngineProvider engineProvider;
    private Gdb gdb;				// gdb proxy
    private GdbVersionPeculiarity peculiarity;  // gdb version differences
    
    private static final Logger LOG = Logger.getLogger(GdbDebuggerImpl.class.toString());

    private final GdbHandlerExpert handlerExpert;
    private MILocation homeLoc;
    private boolean dynamicType;

    private DisModel disModel = new DisModel();
    private DisController disController = new DisController();
    private final Disassembly disassembly;
    private boolean update_dis = true;

    private final VariableBag variableBag = new VariableBag();

    /**
     * Utility class to help us deal with 'frame' or 'source file'
     */
    private static class MILocation extends Location {

        /*
         * frameTuple : frame information, could be null
         *    addr, func, args, file (short), fullname, line  (stopped)
         *
         * srcTuple : source file information
         *    line, file, fullname
         *
         * use 'frameTuple' information first, if null, then use 'srcTuple'
         */
        public static MILocation make(NativeDebugger debugger,
		                    MITList frameTuple,
				    MITList srcTuple,
				    boolean visited,
                                    int stackSize,
                                    NativeBreakpoint breakpoint) {

	    String src = null;
	    int line = 0;
	    String func = null;
	    long pc = 0;
            int level = 0;

            if (frameTuple != null) {
		MIValue addrValue = frameTuple.valueOf("addr");	// NOI18N
		if (addrValue != null) {
		    String addr = addrValue.asConst().value();
                    pc = Address.parseAddr(addr);
		}

                MIValue funcValue = frameTuple.valueOf("func"); // NOI18N
                func = funcValue.asConst().value();

                MIValue fullnameValue = frameTuple.valueOf("fullname"); // NOI18N
                if (fullnameValue != null) {
                    src = fullnameValue.asConst().value();
                } else if (srcTuple != null) {
		    // get fullname from srcTuple
		    fullnameValue = srcTuple.valueOf("fullname"); // NOI18N
		    if (fullnameValue != null) {
			src = fullnameValue.asConst().value();
		    }
		}

                MIValue levelValue = frameTuple.valueOf("level"); // NOI18N
                if (levelValue != null) {
                    String lineString = levelValue.asConst().value();
                    level = Integer.parseInt(lineString);
                }
                
                MIValue lineValue = frameTuple.valueOf("line"); // NOI18N
                if (lineValue != null) {
                    String lineString = lineValue.asConst().value();
                    line = Integer.parseInt(lineString);
                }
            } else {
                // use srcTuple
                MIValue fullnameValue = srcTuple.valueOf("fullname"); // NOI18N
                if (fullnameValue != null) {
                    src = fullnameValue.asConst().value();
                }

                MIValue lineValue = srcTuple.valueOf("line"); // NOI18N
                if (lineValue != null) {
                    String lineString = lineValue.asConst().value();
                    line = Integer.parseInt(lineString);
                }
            }

	    src = debugger.remoteToLocal("MILocation", src); // NOI18N

	    return new MILocation(src,
				  line,
				  func,
				  pc,
				  Location.UPDATE |
				  (visited ? Location.VISITED: 0) |
                                  (level == 0 ? Location.TOPFRAME : 0) |
                                  (level >= stackSize-1 ? Location.BOTTOMFRAME : 0),
                                  breakpoint);
        }

        public static MILocation make(MILocation h, boolean visited) {
	    return new MILocation(h.src(),
				  h.line(),
				  h.func(),
				  h.pc(),
				  Location.UPDATE |
				  (visited ? Location.VISITED: 0) |
                                  (h.topframe() ? Location.TOPFRAME: 0) |
                                  (h.bottomframe() ? Location.BOTTOMFRAME: 0),
                                  h.getBreakpoint());
	}

	private MILocation(String src, int line, String func, long pc,
			   int flags, NativeBreakpoint breakpoint) {
	    super(src, line, func, pc, flags, breakpoint);
	}
    }

    public GdbDebuggerImpl(ContextProvider ctxProvider) {
        super(ctxProvider);
        final List<? extends DebuggerEngineProvider> l = debuggerEngine.lookup(null, DebuggerEngineProvider.class);
        for (int lx = 0; lx < l.size(); lx++) {
            if (l.get(lx) instanceof GdbEngineProvider) {
                engineProvider = (GdbEngineProvider) l.get(lx);
            }
        }
        if (engineProvider == null) {
            throw new IllegalArgumentException("GdbDebuggerImpl not started via GdbEngineProvider"); // NOI18N
        }

        //
        // enhance State
        //

        // Actually SHOULD control this by prop sets
        state().capabAutoRun = false;

        profileBridge = new GdbDebuggerSettingsBridge(this);
        handlerExpert = new GdbHandlerExpert(this);
        disassembly = new Disassembly(this, breakpointModel());
        disStateModel().addListener(disassembly);
    }

    public String debuggerType() {
        return "gdb"; // NOI18N
    }

    public Gdb gdb() {
	return gdb;
    }

    /**
     * 
     * Return true if it's OK to send messages to gdb
     */
    private boolean isConnected() {
        // See "README.startup"
        if (gdb == null || !gdb.connected() || postedKillEngine) {
            return false;
        } else {
            return true;
        }
    }

    private GdbDebuggerInfo gdi;

    public void rememberDDI(GdbDebuggerInfo gdi) {
        this.gdi = gdi;
    }

    // interface NativeDebugger
    public NativeDebuggerInfo getNDI() {
        return gdi;
    }

    boolean isShortName() {
        DebuggerOption option = DebuggerOption.OUTPUT_SHORT_FILE_NAME;
        return option.isEnabled(optionLayers());
    }

    public void start(final GdbDebuggerInfo gdi) {
	// SHOULD factor with DbxDebuggerImpl

        //
        // The following is what used to be in startDebugger():
        //

        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
            int act = gdi.getAction();
            System.out.printf("START ==========\n\t"); // NOI18N
            if ((act & DebuggerManager.RUN) != 0) {
                System.out.printf("RUN "); // NOI18N
            }
            if ((act & DebuggerManager.STEP) != 0) {
                System.out.printf("STEP "); // NOI18N
            }
            if ((act & DebuggerManager.ATTACH) != 0) {
                System.out.printf("ATTACH "); // NOI18N
            }
            if ((act & DebuggerManager.CORE) != 0) {
                System.out.printf("CORE "); // NOI18N
            }
            if ((act & DebuggerManager.LOAD) != 0) {
                System.out.printf("LOAD "); // NOI18N
            }
            if ((act & DebuggerManager.CONNECT) != 0) {
                System.out.printf("CONNECT "); // NOI18N
            }
            System.out.printf("\n"); // NOI18N
        }

        rememberDDI(gdi);
	session().setSessionHost(gdi.getHostName());
	session().setSessionEngine(GdbEngineCapabilityProvider.getGdbEngineType());

	// This might make sense for gdbserver for example
        final boolean connectExisting;
        if ((gdi.getAction() & DebuggerManager.CONNECT) != 0) {
            connectExisting = true;
        } else {
            connectExisting = false;
        }

        profileBridge.setup(gdi);
	if (!connectExisting) {
	    int flags = 0;
	    if (Log.Startup.nopty)
		flags |= Executor.NOPTY;
	    executor = Executor.getDefault(Catalog.get("Gdb"), getHost(), flags); // NOI18N
	}

	final String additionalArgv[] = null; // gdi.getAdditionalArgv();

	if (gdi.isCaptured()) {
	    ExternalStart xstart = ExternalStartManager.getXstart(getHost());
	    if (xstart != null) {
		xstart.debuggerStarted();
	    }
	}

        // See "README.startup"
        if (DebuggerManager.isAsyncStart()) {

            // May not be neccessary in the future.
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    start2(executor, additionalArgv, GdbDebuggerImpl.this, connectExisting);
                }
            });

        } else {
            start2(executor, additionalArgv, this, connectExisting);
        }
    }


    private Gdb.Factory factory;

    private void start2(Executor executor,
			String additionalArgv[],
			Gdb.Factory.Listener listener,
			boolean connectExisting) {

	String gdbInitFile = DebuggerOption.GDB_INIT_FILE.getCurrValue(optionLayers());

	// SHOULD process OPTION_EXEC32?
        String runDir = gdi.getProfile().getRunDirectory();
        runDir = localToRemote("gdbRunDirectory", runDir); // NOI18N

	factory = new Gdb.Factory(executor, additionalArgv,
	    listener, false, isShortName(),
	    gdbInitFile,
	    getHost(),
	    connectExisting,
            runDir,
	    gdi);
	factory.start();
    }

    /* OLD

    Moved to Gdb.Factory et al

    private void start2(GdbDebuggerInfo gdi) {
        Host host = null;
        boolean remote = false;
        if (host == null) {
            remote = false;
        } else if (host.getHostName() == null) {
            remote = false;
        }

        String overrideInstallDir = null;
        if (remote) {
            overrideInstallDir = host.getRemoteStudioLocation();
        }


        //
        // Get gdb and pio consoles
        //
        IOPack ioPack = new GdbIOPack();
        ioPack.setup(remote);
        setIOPack(ioPack);


        // We need the slave name ahead of time
        boolean havePio = executor.startIO(getIOPack().pio);
        if (!havePio) {
            ;   // SHOULD do something
        }


        String gdbname = "gdb";

        // Startup arguments to gdb:
        Vector avec = new Vector();

        avec.add(gdbname);

        // flags to get gdb going as an MI service
        avec.add("--interpreter");
        avec.add("mi");

	// attach or debug corefile
        String program = gdi.getTarget();
        long attach_pid = gdi.getPid();
        String corefile = gdi.getCorefile();

        if (corefile != null) {
            // debug corefile
            if (program == null) {
                program = " ";
            }
            avec.add(program);
            avec.add(corefile);

        } else if (attach_pid != -1) {
            // attach
            String image = Long.toString(attach_pid);
            if (program == null) {
                program = "-";
            }
        }

        // Arrange for gdb victims to run under the Pio
        boolean ioInWindow =
                true;
        if (executor.slaveName() != null && ioInWindow) {
            avec.add("-tty");
            avec.add(executor.slaveName());
        }

        String[] gdb_argv = new String[avec.size()];
        for (int vx = 0; vx < avec.size(); vx++) {
            gdb_argv[vx] = (String) avec.elementAt(vx);
        }


        gdb = new Gdb();

        // setup back- and convenience links from Gdb
        gdb.setDebugger(this);


        getIOPack().console().getTerm().pushStream(gdb.tap());
        getIOPack().console().getTerm().setCustomColor(0,
                Color.yellow.darker().darker());
        getIOPack().console().getTerm().setCustomColor(1,
	    Color.green.darker());
        getIOPack().console().getTerm().setCustomColor(2,
	    Color.blue.brighter());



        int pid = 0;
        pid = executor.startEngine(gdbname, gdb_argv, null,
                getIOPack().console());
        if (pid == 0) {
            return;
        }

        String hostName = null;
        if (remote) {
            hostName = host.getHostName();
        }

    }
    */

    // interface Gdb.Factory.Listener
    public void assignGdb(Gdb tentativeGdb) {
        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
            System.out.printf("GdbDebuggerImpl.assignGdb()\n"); // NOI18N
        }
        gdb = tentativeGdb;
        gdb.setDebugger(this);
        GdbStartActionProvider.succeeded();
        DebuggerManager.get().setCurrentDebugger(this);
	// OLD initializeGdb(getGDI());
    }

    // interface Gdb.Factory.Listener
    public void assignIOPack(IOPack ioPack) {
        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
            System.out.printf("GdbDebuggerImpl.assignIOPack()\n"); // NOI18N
        }
        setIOPack(ioPack);
    }

    // interface gdb.Factory.Listener
    public void connectFailed(String toWhom, String why, IOPack ioPack) {
        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
            System.out.printf("GdbDebuggerImpl.connectFailed()\n"); // NOI18N
        }
        String msg = Catalog.format("ConnectionFailed", toWhom, why); // NOI18N
        Gdb.dyingWords(msg, ioPack);

        // kill() doesn't work unless ACTION_KILL is enabled.
        session.kill();
    }


    /* OLD
    void connectionEstablished() {

        GdbStartActionProvider.succeeded();

        // setup DebuggerManager currentDebugger
        DebuggerManager.get().setCurrentDebugger(this);

        initializeGdb(getGDI());
    }
    */

    private static boolean warnUnsupported = false;
    
    private void warnVersionUnsupported(double gdbVersion) {
        if (!warnUnsupported) {
            InfoPanel panel = new InfoPanel(
                    Catalog.format("ERR_UnsupportedVersion", gdbVersion), //NOI18N
                    Catalog.get("MSG_Do_Not_Show_Again_In_Session")); //NOI18N
            NotifyDescriptor descriptor = new NotifyDescriptor.Message(
                panel,
                NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(descriptor);
            warnUnsupported = panel.dontShowAgain();
        }
    }
    
    void setGdbVersion(String version) {
        double gdbVersion = 6.8;
        try {
             gdbVersion = GdbUtils.parseVersionString(version);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Unable to parse gdb version {0}", version); //NOI18N
        }
        peculiarity = GdbVersionPeculiarity.create(gdbVersion, getHost().getPlatform());
        if (!peculiarity.isSupported()) {
            warnVersionUnsupported(gdbVersion);
        }
    }

    /**
     * Only called by proxy when gdb goes away.
     * (Or on ACTION_KILL if there is no good gdb connection)
     *
     * was: sessionExited() and if(cleanup) portion of finishDebugger()
     */
    public final void kill() {
        Disassembly.close();
        super.preKill();

        optionLayers().save();

        // System.out.println("kill.resetDebugWindowLayout();");
        // resetDebugWindowLayout();

        /* LATER
        setDisassemblerWindow(false);
        if (currentDisassemblerWindow != null) {
        currentDisassemblerWindow.close();
        }
        setMemoryWindow(false);
        if (currentMemoryWindow != null) {
        currentMemoryWindow.close();
        }
        setRegistersWindow(false);
        if (currentRegistersWindow != null) {
        currentRegistersWindow.close();
        }
         */

        IOPack ioPack = getIOPack();
        if (ioPack != null) {
            ioPack.bringDown();
            ioPack.close();
        }

        postedKillEngine = true;
        session = null;
	state().isLoaded = false;
	stateChanged();

        // tell debuggercore that we're going away
        engineProvider.getDestructor().killEngine();

	// It all ends here
    }
    
    boolean postedKillEngine() {
        return postedKillEngine;
    }

    public void postKill() {
        // was: finishDebugger()
        // We get here when ...
        // - Finish action on session node
        // - When IDE is exiting

        // DEBUG System.out.println("GdbDebuggerImpl.postKill()");

        // The quit to dbx will come back to us as kill()
        // which will call killEngine()
        // debuggercore itself never calls killEngine()!

        postedKill = true;

        //termset.finish();
        if (gdb != null && gdb.connected()) {
            // see IZ 191508, need to pause before exit
            // or kill gdb if process pid is unavailable
            if (!pause(true)) {
                try {
                    executor.terminate();
                    kill();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return;
            }
            
            // Ask gdb to quit (shutdown)
            MICommand cmd = new MiCommandImpl("-gdb-exit") { // NOI18N

                @Override
                protected void onError(MIRecord record) {
                    finish();
                }

                @Override
                protected void onExit(MIRecord record) {
                    kill();
                    finish();
                }
            };
            gdb.sendCommand(cmd);
        } else {
            // since there's no gdb connection (e.g. failed to start)
            // call kill directly
            kill();
        }
    }

    public void shutDown() {
	postKill();
    }

    public final void stepInto() {
        sendResumptive("-exec-step"); // NOI18N
    }

    public final void stepIntoMain() {
        send("-break-insert -t main"); //NOI18N
        sendResumptive("-exec-run"); // NOI18N
	
	// IZ 189550
        sendPidCommand(false);
    }

    public final void stepOver() {
        sendResumptive("-exec-next"); // NOI18N
    }

    public final void stepOut() {
        send("-stack-select-frame 0"); // NOI18N
        execFinish();
    }

    private void execFinish() {
        sendResumptive("-exec-finish"); // NOI18N
    }

    public final void pathmap(String pathmap) {
        send(pathmap);
    }

    private void notImplemented(String method) {
        System.out.printf("NOT IMPLEMENTED: GdbDebuggerImpl.%s()\n",// NOI18N
                method);
    }

    public final void stepTo(String function) {
        notImplemented("stepTo");	// NOI18N
    }

    public final void go() {
        sendResumptive("-exec-continue"); // NOI18N
    }

    public void doMIAttach(GdbDebuggerInfo gdi) {
        final long pid = gdi.getPid();
        // MI command "-target-attach pid | file" does not available in
        // gdb 6.1, 6.6, use CLI command "attach" instead.
        String cmdString = "attach " + Long.toString(pid); // NOI18N
        MICommand cmd =
            new MiCommandImpl(cmdString) {

                    @Override
                    protected void onDone(MIRecord record) {
                        state().isProcess = true;
                        stateChanged();
			session().setSessionState(state());
			session().setPid(pid);
                        requestStack(null);
                        finish();
                    }
            };
        gdb.sendCommand(cmd);
    }

    public void doMICorefile(GdbDebuggerInfo gdi) {
        String corefile = gdi.getCorefile();
        String cmdString = "core " + corefile; // NOI18N
        /*
         * (gdb 6.2) core-file
         * ^done,frame={level="0",addr="0x080508ae",func="main",args=[],file="t.cc",line="4"},line="4",file="t.cc"
         */
        MICommand cmd =
            new MiCommandImpl(cmdString) {

                    @Override
                    protected void onDone(MIRecord record) {
                        state().isCore = true;
                        stateChanged();
			session().setSessionState(state());
                        requestStack(null);
                        finish();
                    }
            };
        gdb.sendCommand(cmd);
    }

    public void contAt(String src, int line) {
	/* (GDB) Version 7.2.50.20101202.
	src = localToRemote("runToCursor", src); // NOI18N
        String cmdString = "-exec-jump " + src + ":" + line; // NOI18N
        MICommand cmd = new MIResumptiveCommand(cmdString);
        gdb.sendCommand(cmd);
	 *
	 */
	notImplemented("-exec-jump");// NOI18N
    }

    public void runToCursor(String src, int line) {
	src = localToRemote("runToCursor", src); // NOI18N
        String cmdString = "-exec-until " + src + ":" + line; // NOI18N
        sendResumptive(cmdString);
    }

    public GdbVersionPeculiarity getGdbVersionPeculiarity() {
        return peculiarity;
    }

    public void pause() {
        pause(false);
    }

    public boolean pause(boolean silentStop) {
        /* LATER

        On unix, and probably in all non-embedded gdb scenarios,
        "-exec-interrupt" is not honored while running ...

        MICommand cmd =
        new MIResumpiveCommand("-exec-interrupt") {
	    protected void onRunning(MIRecord record) {
		unexpected("running", command());
	    }
        };
        gdb.sendCommand(cmd);
         */

        // ... so we interrupt
	int pid = (int) session().getPid();
	if (pid > 0) {
	    return gdb.pause(pid, silentStop);
        }
        return false;
    }

    public void interrupt() {
        gdb.interrupt();
    }

    // interface NativeDebugger
    public void terminate() {
        notImplemented("terminate");	// NOI18N
    }

    // interface NativeDebugger
    public void detach() {
        notImplemented("detach");	// NOI18N
    }

    private class MiCommandImpl extends MICommand {
	private MICommand successChain = null;
	private MICommand failureChain = null;

	private boolean emptyDoneIsError = false;
        private boolean reportError = true;
        
        protected MiCommandImpl(String cmd) {
	    super(0, cmd);
	}
        
	protected MiCommandImpl(int rt, String cmd) {
	    super(rt, cmd);
	}

	public void chain(MICommand successChain, MICommand failureChain) {
	    this.successChain = successChain;
	    this.failureChain = failureChain;
	}

	public void setEmptyDoneIsError() {
	    this.emptyDoneIsError = true;
	}

	public void dontReportError() {
	    this.reportError = false;
	}

	protected void onDone(MIRecord record) {
	    if (emptyDoneIsError && record.isEmpty()) {
		// See comment for isEmpty
		onError(record);
	    } else {
		finish();
		if (successChain != null) {
		    gdb.sendCommand(successChain);
                }
	    }
	}

	protected void onRunning(MIRecord record) {
	    unexpected("running", command()); // NOI18N
	}

	protected void onError(MIRecord record) {
	    if (failureChain == null && reportError) {
		genericFailure(record);
            }
	    finish();
	    if (failureChain != null) {
		gdb.sendCommand(failureChain);
            }
	}

	protected void onExit(MIRecord record) {
	    unexpected("exit", command()); // NOI18N
	    kill();
	    finish();
	}

	protected void onStopped(MIRecord record) {
	    unexpected("stopped", command()); // NOI18N
	}

	protected void onOther(MIRecord record) {
	    unexpected("other", command()); // NOI18N
	}

	protected void onUserInteraction(MIUserInteraction ui) {
	    unexpected("userinteraction", command()); // NOI18N
	}
    }

    /**
     * Handle the oputput of "info proc".
     */
    private static int extractPid1(MIRecord record) {
	StringTokenizer st =
	    new StringTokenizer(record.command().
				getConsoleStream());
	int pid = 0;
	if (st.hasMoreTokens()) {
	    st.nextToken();
	    if (st.hasMoreTokens()) {
		String pidStr = st.nextToken();
                int pidEnd = 0;
                while (pidEnd < pidStr.length() && Character.isDigit(pidStr.charAt(pidEnd))) {
                    pidEnd++;
                }
                try {
                    pid = Integer.parseInt(pidStr.substring(0, pidEnd));
                } catch (Exception e) {
                    Exceptions.printStackTrace(new Exception("Pid parsing error: " + record.command().getConsoleStream(), e)); //NOI18N
                }
	    }
	}
	return pid;
    }

    /**
     * handle output of the form
     *		~"[Switching to process 446 ...]\n"
     * which we get on Mac 10.4
     */
    private int extractPid2(String console) {
	int pid = 0;

	if (Log.Gdb.pid)
	    System.out.printf("//////// '%s'\n", console);

	if (console != null) {
	    StringTokenizer st =
		new StringTokenizer(console);
	    int ntokens = 0;
	    while (st.hasMoreTokens()) {
		String token = st.nextToken();
		if (Log.Gdb.pid)
		    System.out.printf("\t%d: '%s'\n", ntokens, token); // NOI18N
		if (ntokens == 3) {
		    String pidStr = token;
		    pid = Integer.parseInt(pidStr);
		    break;
		}
		ntokens++;
	    }
	}

	if (Log.Gdb.pid)
	    System.out.printf("\\\\\\\\ pid %d\n", pid); // NOI18N
	return pid;
    }

    private void sendPidCommand(boolean resume) {
        if (session().getPid() <= 0) {
            if (getHost().getPlatform() == Platform.Windows_x86) {
                MICommand findPidCmd = new InfoThreadsMICmd(resume);
                gdb.sendCommand(findPidCmd);
            } else if (getHost().getPlatform() != Platform.MacOSX_x86) {
                InfoProcMICmd findPidCmd = new InfoProcMICmd(resume);
                // if it fails - try "info threads"
                MICommand findPidCmd2 = new InfoThreadsMICmd(resume);
                findPidCmd.chain(null, findPidCmd2);
                gdb.sendCommand(findPidCmd);
            }
        } else if (resume) {
            go();
        }
    }

    private final class InfoThreadsMICmd extends MiCommandImpl {
        final boolean resume;

	public InfoThreadsMICmd(boolean resume) {
	    super("info threads");// NOI18N
	    this.resume = resume;
	}

        @Override
	protected void onDone(MIRecord record) {
            int pid = 0;
            String msg = record.command().getConsoleStream();
	    int pos1 = msg.toLowerCase().indexOf("* 1 thread "); // NOI18N
            if (pos1 >= 0) {
                int pos2 = msg.indexOf('.', pos1);
                if (pos2 > 0) {
                    try {
                        pid = Integer.valueOf(msg.substring(pos1 + 11, pos2));
                    } catch (NumberFormatException ex) {
                        //log.warning("Failed to get PID from \"info threads\""); // NOI18N
                    }
                }
            }

	    session().setSessionEngine(GdbEngineCapabilityProvider.getGdbEngineType());
	    if (pid != 0) {
		session().setPid(pid);
            }

	    if (resume) {
		go();	// resume
            }
	    finish();
	}
    }

    private final class InfoProcMICmd extends MiCommandImpl {
	final boolean resume;

	public InfoProcMICmd(boolean resume) {
	    super("info proc");// NOI18N
	    this.resume = resume;
	}

        @Override
	protected void onDone(MIRecord record) {
	    // We get something of the form
	    //		process <pid> flags:

	    if (Log.Gdb.pid) {
		System.out.printf("FindPidMICmd.onDone(): record: %s\n", // NOI18N
		    record);
		System.out.printf("                      command: %s\n", // NOI18N
		    record.command());
		System.out.printf("                      console: %s\n", // NOI18N
		    record.command().getConsoleStream());
	    }

	    int pid = extractPid1(record);

	    session().setSessionEngine(GdbEngineCapabilityProvider.getGdbEngineType());
	    if (pid != 0)
		session().setPid(pid);
	    
	    if (resume)
		go();	// resume
	    finish();
	}
    }
    
    private String firstBreakpointId = null;
    
    private void setFirstBreakpointId(MIRecord record) {
        MIValue bkptValue = record.results().valueOf("bkpt"); //NOI18N
        if (bkptValue != null) {
            MIValue numberValue = bkptValue.asTList().valueOf("number"); //NOI18N
            if (numberValue != null) {
                firstBreakpointId = numberValue.asConst().value();
            }
        }
    }

    public void rerun() {
        if (true /* LATER !state.isRunning */) {
	    //
	    // setup to discover the processes pid
	    // In effect we do this:
	    // 		tbreak _start || tbreak main
	    //		-exec-run
	    //		# we hit the bpt
	    //		info proc
	    //		# -> "process 977144 flags:"
	    // '_start' occurs much earlier than main, earlier than init
	    // sections(?), so we try it first and if we can't find it we
	    // try main. On linux stripped executables don't define
	    // either so we're SOL.
	    //
	    // I also tried to use gdb command (analog of dbx when)
	    // to issue "info proc" on the tbreak but the output of
	    // "info proc" goes into the bit bucket!
	    // 
	    // Note that the implicit resumption after a stoppage on main
	    // will interfere with a normal "break main"!

	    MiCommandImpl breakStartCmd =
		new MiCommandImpl("-break-insert -t _start") { // NOI18N
                    @Override
                    protected void onDone(MIRecord record) {
                        setFirstBreakpointId(record);
                        super.onDone(record);
                    }
                };
	    breakStartCmd.setEmptyDoneIsError();

	    MiCommandImpl breakMainCmd =
		new MiCommandImpl("-break-insert -t main") { // NOI18N
                    @Override
                    protected void onDone(MIRecord record) {
                        setFirstBreakpointId(record);
                        super.onDone(record);
                    }
                };
	    breakMainCmd.setEmptyDoneIsError();

	    //
	    // The actual run command
	    //
            MICommand runCmd =
                new MIResumptiveCommand("-exec-run") {		// NOI18N

                @Override
                    protected void onRunning(MIRecord record) {
                        state().isProcess = true;
                        super.onRunning(record);
                    }
                };

	    breakStartCmd.chain(runCmd, breakMainCmd);
	    breakMainCmd.chain(runCmd, null);

            // _start does not work on MacOS
            if (getHost().getPlatform() == Platform.MacOSX_x86) {
                gdb.sendCommand(breakMainCmd);
            } else {
                gdb.sendCommand(breakStartCmd);
            }
        }
    }

    public void makeCalleeCurrent() {
        Frame frame = getCurrentFrame();
        if (frame != null) {
            String number = frame.getNumber();
            makeFrameCurrent(getStack()[Integer.valueOf(number)-1]);
        }
    }

    public void makeCallerCurrent() {
        Frame frame = getCurrentFrame();
        if (frame != null) {
            String number = frame.getNumber();
            makeFrameCurrent(getStack()[Integer.valueOf(number)+1]);
        }
    }

    public void popToHere(Frame frame) {
        String number = frame.getNumber();
        makeFrameCurrent(getStack()[Integer.valueOf(number)-1]);
        execFinish();
    }

    public void popTopmostCall() {
        stepOut();
    }

    public void popLastDebuggerCall() {
    }

    public void popToCurrentFrame() {
        makeCalleeCurrent();
        execFinish();
    }

    private static final int PRINT_REPEAT = Integer.getInteger("gdb.print.repeat", 0); //NOI18N
    private static final int STACK_MAX_DEPTH = Integer.getInteger("gdb.stack.maxdepth", 1024); // NOI18N
    
    public void initializeGdb(FileMapper fmap) {
	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
	    System.out.printf("GdbDebuggerImpl.initializeGdb()\n"); // NOI18N
	}

	assert isConnected() : "initializeGdb() called when gdb wasn't ready";
        
        // for remote always use NULL mapper
        if (getHost().isRemote()) {
            this.fmap = FileMapper.getDefault(FileMapper.Type.NULL);
        } else if (fmap != null) {
            this.fmap = fmap;
        }

	// OLD overrideOptions();
	manager().initialUnsavedFiles(this);

	if (gdi.isCaptured()) {
	    setCaptureState(CaptureState.INITIAL);
	    setCaptureInfo(gdi.getCaptureInfo());
	} else {
	    assert getCaptureState() == CaptureState.NONE;
	}
        
        //init global parameters
        send("-gdb-set print repeat " + PRINT_REPEAT); // NOI18N
        send("-gdb-set backtrace limit " + STACK_MAX_DEPTH); // NOI18N
        
        // set terminal mode on windows, see IZ 193220
        if (getHost().getPlatform() == Platform.Windows_x86 && getIOPack().isExternal()) {
            send("set new-console"); //NOI18N
        }

        // Tell gdb what to debug
        debug(gdi);

	// Make us be the current session
	// We flip-flop to force the posting of another PROP_CURRENT_SESSION
	manager().setCurrentSession(null);
	manager().setCurrentSession(session.coreSession());
    }

    /**
     * Send any initial commands (like 'run' for Debug, or 'next' for
     * StepInto) after all initialization is done
     */
    private void initialAction() {
        if (DebuggerManager.isStartModel()) {
            // OLD GdbDebuggerInfo gdi = this.getGDI();
            if (gdi != null) {
                // For load and run
                if ((gdi.getAction() & DebuggerManager.RUN) != 0) {
                    rerun();
                    gdi.removeAction(DebuggerManager.RUN);
                }
                // For attach
                if ((gdi.getAction() & DebuggerManager.ATTACH) != 0) {

                    doMIAttach(gdi);
                    gdi.removeAction(DebuggerManager.ATTACH);
                }

                // For debugging core file
                if ((gdi.getAction() & DebuggerManager.CORE) != 0) {

                    doMICorefile(gdi);
                    gdi.removeAction(DebuggerManager.CORE);
                }

                // For load and step
                if ((gdi.getAction() & DebuggerManager.STEP) != 0) {
                    //stepOver(); // gdb 6.1
		    stepIntoMain(); // gdb 6.6
                    gdi.removeAction(DebuggerManager.STEP);
                }
            }
        }
    }
    void noteProgLoaded(String progname) {

        // OLD manager().cancelProgress();
        // LATER manager().startProgressManager().cancelStartProgress();

        profileBridge().noteProgLoaded(progname);

        // SHOULD add Handler cleanup code from DbxDebuggerImpl

        // OLD overrideOptions();

        manager().formatStatusText("ReadyToRun", null); // NOI18N

        DebuggerManager.get().addRecentDebugTarget(progname, false);

        if (Log.Bpt.fix6810534) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    initialAction();
                }
            });
        } else {
            initialAction();
        }


    }

    public OptionClient getOptionClient() {
        return null;
    }

    public String getDebuggingOption(String name) {
        notImplemented("getDebuggingOption");	// NOI18N
        return null;
    }

    public void setOption(String name, String value) {
        notImplemented("setOption() " + name + " " + value );	// NOI18N
    }

    /*
     * RTC stuff
     * Not supported.
     */
    public void setAccessChecking(boolean enable) {
    }

    public void setMemuseChecking(boolean enable) {
    }

    public void setLeaksChecking(boolean enable) {
    }

    /**
     * Set defaults which don't match gdb defaults
     */
    private void overrideOptions() {

        //
        // The following SHOULD really be set as a side-effect of option
        // application.
        //

        /* LATER
        This seems to be available only in gdb 6.4
        Meanwhile we use the gdb cmdline option -tty
        // Arrange for gdb victims to run under the Pio
        boolean ioInWindow =
        true;

        if (executor.slaveName() != null && ioInWindow) {
        MICommand cmd =
        new MICommand("-inferior-tty-set", executor.slaveName()) {
        protected void onDone(MIRecord record) {
        finish();
        }
        protected void onRunning(MIRecord record) {
        unexpected("running", command);
        }
        protected void onError(MIRecord record) {
        finish();
        }
        protected void onStopped(MIRecord record) {
        unexpected("stopped", command);
        }
        protected void onOther(MIRecord record) {
        unexpected("other", command());
        }
        };
        gdb.sendCommand(cmd);
        }
         */
    }

    private String getErrMsg(MIRecord record) {
        String errMsg = null;

        if (record.isError()) {
            errMsg = record.error();

        } else if (!record.isEmpty()) {
            MIValue value = record.results().valueOf("msg");	// NOI18N
            errMsg = value.asConst().value();

        } else {
	    // See comment to MIRecord.isEmpty().
	    if (record.command() != null) {
		errMsg = record.command().getLogStream();
	    } 
	    if (errMsg == null)
		errMsg = Catalog.get("MSG_UnknownFailure"); // NOI18N
	}
        return errMsg;
    }

    private void genericFailure(MIRecord record) {
        String errMsg = getErrMsg(record);
        manager().error(record.command().routingToken(), new GdbError(errMsg), this);
    }

    private void unexpected(String what, String command) {
        System.out.println("Unexpcted callback '" + what + "' on '" + command + "'"); // NOI18N
    }

    void genericRunning() {
        clearFiredEvents();
        deleteMarkLocations();
        deliverSignal = -1;

        stateSetRunning(true);
        stateChanged();
	session().setSessionState(state());
        setStatusText(Catalog.get("MSG_running"));//NOI18N
    }

    private boolean dontKillOnExit() {
        // SHOULD factor with inline code in DbxDebuggerImpl.noteProcGone

	if (!DebuggerOption.FINISH_SESSION.isEnabled(optionLayers()) ||
	    ((gdi.getAction() & DebuggerManager.LOAD) != 0)) {
	    return true;
	} else {
	    return false;
	}
    }

    private void noteProcGone(final String reason, final MITList results) {
        session().setPid(-1);
        session().setCorefile(null);
        session().update();
	session().setSessionEngine(null);

        state().isProcess = false;
        state().isCore = false;
        stateSetRunning(false);
        stateChanged();
	session().setSessionState(state());

        clearFiredEvents();

        String msg = "";	// NOI18N
        boolean skipkill = false;

        if ("exited-normally".equals(reason)) { // NOI18N
            final String exitcodeString = "0";		// NOI18N
            msg = Catalog.format("ProgCompletedExit", exitcodeString); // NOI18N
            skipkill = dontKillOnExit();

        } else if ("exited".equals(reason)) { // NOI18N
            final MIValue exitcodeValue = results.valueOf("exit-code"); // NOI18N
            final String exitcodeString = exitcodeValue.asConst().value();
            msg = Catalog.format("ProgCompletedExit", exitcodeString); // NOI18N
            skipkill = dontKillOnExit();

        } else if ("exited-signalled".equals(reason)) { // NOI18N
            final MIValue signalnameValue = results.valueOf("signal-name"); // NOI18N
            final String signalnameString = signalnameValue.asConst().value();
            msg = Catalog.format("ProgAborted", signalnameString); // NOI18N

        } else {
            msg = "Stopped for unrecognized reason: " + reason; // NOI18N
        }

        setStatusText(msg);

        if (!skipkill && DebuggerManager.isStartModel()) {
            postKill();
        }

        resetCurrentLine();
    }

    /*
     * thread stuff
     */
    private GdbThread[] threads = new GdbThread[0];
    private int current_thread_index;

    // SHOULD factor with DbxDebuggerImpl's threadsMasked
    private boolean get_threads = false; // indicate Thread View open/close

    // interface NativeDebugger
    public boolean isMultiThreading() {
        return false;
    }

    public void registerThreadModel(ThreadModel model) {
        if (Log.Variable.mi_threads) {
            System.out.println("registerThreadModel " + model); // NOI18N
        }
        threadUpdater.setListener(model);
        if (model != null) {
            get_threads = true;
            if (state().isProcess && !state().isRunning) {
                showThreads();
            }
        } else {
            get_threads = false;
        }
    }

    public Thread[] getThreads() {
        return threads;
    }

    public void makeThreadCurrent(Thread thread) {
        if (!thread.isCurrent()) {
            String tid = ((GdbThread) thread).getId();
            selectThread(-1, tid, true); // notify gdb to set current thread
        }
    }

    private void getAllThreads(MIRecord thread) {
        MITList threadresults = thread.results();
        MITList thread_ids = (MITList) threadresults.valueOf("thread-ids"); // NOI18N

        MIValue tid = threadresults.valueOf("number-of-threads"); // NOI18N

	// assume this thread is current
        String current_tid_no = tid.asConst().value(); 

        if (Log.Variable.mi_threads) {
            System.out.println("threads " + threadresults.toString()); // NOI18N
            System.out.println("thread_ids " + thread_ids.toString()); // NOI18N
        }
        int size = thread_ids.size();

        threads = new GdbThread[size];
        for (int vx = 0; vx < size; vx++) {
            MIResult thread_id = (MIResult) thread_ids.get(vx);
            String id_no = thread_id.value().asConst().value();
            if (Log.Variable.mi_threads) {
                System.out.println("threads_id " + thread_id.toString()); // NOI18N
                System.out.println("thread_ id " + id_no); // NOI18N
            }
	    if (id_no.equals(current_tid_no))
		current_thread_index = vx;
	    else
		// collect detail thread info from gdb engine
		selectThread(vx, id_no, false); 
        }

	// do current thread the last, becuase selectThread will also set
	// current thread
	selectThread(current_thread_index, current_tid_no, true); 
    }

//    private void interpAttach(MIRecord threadframe) {
//        MITList threadresults = threadframe.results();
//        if (Log.Variable.mi_threads) {
//            System.out.println("threadframe " + threadresults.toString()); // NOI18N
//	}
//
//        /* no used for now, may be used for attaching multi-thread prog
//        MIValue tid = threadresults.valueOf("thread-id");
//        String tid_no = tid.asConst().value();
//        System.out.println("tid_no " + tid);
//         */
//
////        if (get_frames || get_locals) { // get new Frames for current thread
////            // would call getMILocals.
////            showStackFrames();
////        }
//
//        GdbFrame f = null;
//
//        if (threadresults.isEmpty()) {
//            f = getCurrentFrame();
//        } else {
//            MIValue frame = threadresults.valueOf("frame"); // frame entry // NOI18N
//            f = new GdbFrame(this, frame, null); // args data are included in frame
//        }
//        
//        if (f != null && f.getLineNo() != null && !f.getLineNo().equals("")) {
//            // has source info,
//            // get full path for current frame from gdb,
//            // update source editor, and make frame as current
//            getFullPath(f);
//        }
//    }

    private void updateLocalsForSelectFrame() {
        if (get_locals) { // get local vars for current frame
            getMILocals(false);    // get local vars for current frame from gdb
        }
    }

//    private void interpCore(MIRecord threadframe) {
//        MITList threadresults = threadframe.results();
//        MIValue frame = threadresults.valueOf("frame"); // frame entry // NOI18N
//        GdbFrame f = new GdbFrame(this, frame, null); // args data are included in frame
//
////        if (get_frames || get_locals) {
////            // would call getMILocals.
////            showStackFrames();
////        }
//        if (!f.getLineNo().equals("")) {
//            // has source info,
//            // get full path for current frame from gdb,
//            // update source editor, and make frame as current
//            getFullPath(f);
//        }
//    }

    private void setCurrentThread(int index,
				  MIRecord threadframe,
				  boolean isCurrent) {

        MITList threadresults = threadframe.results();

        MIValue tid = threadresults.valueOf("new-thread-id"); // NOI18N
        String tid_no = tid.asConst().value();

        MIValue frame = threadresults.valueOf("frame");// frame entry // NOI18N
        if (Log.Variable.mi_threads) {
            System.out.println("threadframe " + threadresults.toString()); // NOI18N
            System.out.println("tid_no " + tid); // NOI18N
            System.out.println("frame " + frame.toString()); // NOI18N
        }
        GdbFrame f = new GdbFrame(this, frame, null);

	if (index != -1)
	    threads[index] = new GdbThread(this, threadUpdater, tid_no, f);
        if (isCurrent) {
            selectFrame(f.getNumber()); // notify gdb to change current frame
//            if (get_frames || get_locals) { // get Frames for current thread
//                // would call getMILocals.
//                showStackFrames();
//            }

            if (!f.getLineNo().equals("")) {
                // has source info,
                // get full path for current frame from gdb,
                // update source editor, and make frame as current
                getFullPath(f);
            }
            for (int tx = 0; tx < threads.length; tx++) {
                if (threads[tx].getId().equals(tid_no)) {
                    threads[tx].setCurrent(true);
                } else {
                    threads[tx].setCurrent(false);
                }
            }
        }
	if (isCurrent || index == threads.length-1)
	    threadUpdater.treeChanged();     // causes a pull
    }

    // get detail thread info, thread id, frame, args, file, fullpath...etc.
    // also set current thread, so order of calling this method is critical
    // the last call will be the current thread.
    private void selectThread(final int index, final String id_no, final boolean isCurrent) {
        MICommand cmd =
            new MiCommandImpl("-thread-select " + id_no) { // NOI18N
            @Override
                protected void onDone(MIRecord record) {
                    setCurrentThread(index, record, isCurrent);
                    finish();
                }
	    };
        gdb.sendCommand(cmd);
    }

    private void showThreads() {
	// -thread-list-all-threads and -thread-info
	// are not implemented in gdb 6.4,
	// can only use -thread-list-ids and -thread-select
	// to work around the problem, but we don't know
	// which thread is the current thread
        MICommand cmd =
            new MiCommandImpl("-thread-list-ids") { // NOI18N
            @Override
		protected void onDone(MIRecord record) {
		    getAllThreads(record);
		    finish();
		}
	    };
        gdb.sendCommand(cmd);
    }

    /*
     * stack stuff
     *
     */
    // SHOULD factor with DbxDebuggerImpl's stackMasked
    private boolean get_frames = false; // indicate Stack View open/close

    public void registerStackModel(StackModel model) {
        if (Log.Variable.mi_frame) {
            System.out.println("registerStackModel " + model); // NOI18N
        }
        stackUpdater.setListener(model);
        if (model != null) {
            get_frames = true;
//            if (state().isProcess && !state().isRunning) {
//                showStackFrames();
//            }
        } else {
            get_frames = false;
        }
    }

    public Frame[] getStack() {
        if (guiStackFrames == null) {
            return new GdbFrame[0];
        } else {
            return guiStackFrames;
        }
    }
    /*
    public boolean getVerboseStack() {
    return stack_verbose;
    }
     */

    public void postVerboseStack(boolean v) {
    }

    public GdbFrame getCurrentFrame() {
        if (guiStackFrames != null) {
            for (Frame frame : guiStackFrames) {
                if (frame.isCurrent()) {
                    return (GdbFrame) frame;
                }
            }
            return (GdbFrame) guiStackFrames[0];
        }
        return null;
    }

    public void moreFrame() {
	return;
    }
    
    public void makeFrameCurrent(Frame f) {
        String fno = f.getNumber();
        boolean changed = false;
        if (guiStackFrames != null) {
            for (Frame frame : guiStackFrames) {
                if (frame.getNumber().equals(fno)) {
                    /* can't break, need it for bring back source
                    if (guiStackFrames[fx].isCurrent())
                    break;              // no change in state
                     */
                    changed = true;
                    frame.setCurrent(true);
                } else {
                    frame.setCurrent(false);
                }
            }
        }
        if (changed) {
            // selectFrame would update local vars too
            selectFrame(fno); // notify gdb to change current frame

            // has source info,
            // get full path for current frame from gdb,
            // update source editor, and make frame as current
            getFullPath((GdbFrame) f);
        }
        stackUpdater.treeChanged();     // causes a pull
    }

    private void visitCurrentSrc(GdbFrame f, MIRecord srcRecord) {
        MITList  srcTuple = srcRecord.results();
        if (f == null)
            f = new GdbFrame(this, null, null);

	// create a non-visited location because it may be assigned to
	// homeLoc

        MILocation l = MILocation.make(this, f.getMIframe(), srcTuple, false, getStack().length, null);

	// We really SHOULD not be setting homeLoc in a method called
	// visitBlahBlah
	if (homeLoc == null)
	    homeLoc = l;		// attach scenario

        boolean visited;
        if (state().isProcess) {
            visited = ! l.equals(homeLoc);
        } else {
            visited = true;
        }
	visitedLocation = MILocation.make(l, visited);
        setVisitedLocation(visitedLocation);
        
        state().isUpAllowed = !l.bottomframe();
        state().isDownAllowed = !l.topframe();
        stateChanged();
    }

    private void getFullPath(final GdbFrame f) {
        MiCommandImpl cmd =
            new MiCommandImpl("-file-list-exec-source-file") { // NOI18N
            @Override
		protected void onDone(MIRecord record) {
		    visitCurrentSrc(f, record);
		    finish();
		}
	    };
        cmd.dontReportError();
        gdb.sendCommand(cmd);
    }

    /*
     * notify gdb to switch current frame to fno 
     * also get locals info for new current frame
     */
    private void selectFrame(final Object fno) {

        MICommand cmd =
            new MiCommandImpl("-stack-select-frame " + fno) { // NOI18N
            @Override
		protected void onDone(MIRecord record) {
		    updateLocalsForSelectFrame();
		    finish();
		}
	    };
        gdb.sendCommand(cmd);
    }

    /*
     * framerecords: what we got from -stack-list-frames = stack
     * args: what we got from -stack-list-arguments 1 = stack-args
     */
    private void setStackWithArgs(MIRecord framerecords, MIRecord args) {
        MITList argsresults;
        MITList args_list = null;
        String stringframes;

        if (args != null) {
            argsresults = args.results();
            args_list = (MITList) argsresults.valueOf("stack-args"); // NOI18N
            stringframes = args_list.toString();
            if (Log.Variable.mi_frame) {
                System.out.println("args_list " + stringframes); // NOI18N
            }
        }


        MITList results = framerecords.results();
        MITList stack_list = (MITList) results.valueOf("stack"); // NOI18N
        int size = stack_list.size();

        // iterate through frame list
        guiStackFrames = new GdbFrame[size];
        for (int vx = 0; vx < size; vx++) {
            MIResult frame = (MIResult) stack_list.get(vx);
            
            // try to find frame arguments
            MIResult frameArgs = null;
            if (args_list != null && vx <= args_list.size()) {
                frameArgs = (MIResult) args_list.get(vx);
            }
            
            guiStackFrames[vx] = new GdbFrame(this, frame.value(), frameArgs);
            
            if (vx == 0) {
                guiStackFrames[vx].setCurrent(true); // make top frame current
            }
        }
        if (get_locals) {
            getMILocals(true); // "true" for gdb "var-update *" to get value update
        }

        stackUpdater.treeChanged();     // causes a pull
    }

    /*
     * get frame info: level, args
     * for whole stack
     * framerecords: what we got from -stack-list-frames
     */
    private void setStack(final MIRecord framerecords) {
        // "1" means get both arg's name and value
        String args_command = "-stack-list-arguments 1"; // NOI18N

        MICommand cmd =
            new MiCommandImpl(args_command) {

            @Override
                    protected void onDone(MIRecord record) {
			try {
			    try {
				setStackWithArgs(framerecords, record);
			    } catch (RuntimeException x) {
				// This can happenif we issue steps too quickly
				// such that -stack-list-arguments comes
				// after a -stack-list-frames but also after
				// a resumption.
				/*
				System.out.printf("framerecords; %s\n", framerecords);
				System.out.printf("record; %s\n", record);
				*/
				throw x;
			    }
			} finally {
			    finish();
			}
                    }

		    @Override
                    protected void onError(MIRecord record) {
                        String errMsg = getErrMsg(record);
                        if (errMsg.equals(corrupt_stack)) {
                            setStack(framerecords);
                        } else {
                            setStackWithArgs(framerecords, null);
                        }
			finish();
                    }
                };
        gdb.sendCommand(cmd);
    }
    static String corrupt_stack = "Previous frame identical to this frame (corrupt stack?)"; // NOI18N
    boolean try_one_more = false;

    /*
     * get frame info : level, addr, func, file
     */
//    private void showStackFrames() {
//        MICommand cmd =
//            new MiCommandImpl("-stack-list-frames") { // NOI18N
//
//            @Override
//                    protected void onDone(MIRecord record) {
//                        try_one_more = true; // success
//                        setStack(record);
//                        finish();
//                    }
//
//            @Override
//                    protected void onError(MIRecord record) {
//                        String errMsg = getErrMsg(record);
//                        // to work around gdb thread frame problem
//                        // get the real and correct output of "-stack-list-frames"
//                        // just try_one_more is not enough, so I try it for as long
//                        // as it fail, but watch out for infinite loop
//                        //if (try_one_more && errMsg.equals(corrupt_stack)) {
//                        if (errMsg.equals(corrupt_stack)) {
//                            // sometime we have timing issue that we need
//                            // to try several time, one more time is not
//                            // enough.
//                            // try_one_more = false;
//                            showStackFrames(); // try again, watch out for infinite loop
//                        } else {
//                            genericFailure(record);
//                        }
//			finish();
//                    }
//                };
//        gdb.sendCommand(cmd);
//    }

    /* 
     * balloonEval stuff 
     */
    public void balloonEvaluate(int pos, String text) {
        // balloonEvaluate() requests come from the editor completely
        // independently of debugger startup and shutdown.
        if (gdb == null || !gdb.connected()) {
            return;
        }
        if (state().isProcess && state().isRunning) {
            return;
        }
        String expr;
        if (pos == -1) {
            expr = text;
        } else {
            expr = EvalAnnotation.extractExpr(pos, text);
        }

        if (expr != null) {
            dataMIEval(expr);
        }
    }

    public void postExprQualify(String expr, QualifiedExprListener qeListener) {
    }

    private void dataMIEval(final String expr) {
        String expandedExpr = MacroSupport.expandMacro(this, expr);
        String cmdString = "-data-evaluate-expression " + "\"" + expandedExpr + "\""; // NOI18N
        MICommand cmd =
            new MiCommandImpl(cmdString) {

            @Override
                    protected void onDone(MIRecord record) {
                        interpEvalResult(expr, record);
                        finish();
                    }

            @Override
		    protected void onError(MIRecord record) {
			// Be silent on balloon eval failures
			// genericFailure(record);
			finish();
		    }
                };
        gdb.sendCommand(cmd);
    }

    private void interpEvalResult(String expr, MIRecord record) {
        MITList value = record.results();
        if (Log.Variable.mi_vars) {
            System.out.println("value " + value.toString()); // NOI18N
        }
        String value_string = value.valueOf("value").asConst().value(); // NOI18N
        EvalAnnotation.postResult(0, 0, 0, expr, value_string, null, null);
    }
    /* 
     * watch stuff 
     */
    private boolean get_watches = false; // indicate Watch View open/close

    public void postCreateWatch(int routingToken, NativeWatch newWatch) {
    }

    public boolean watchError(int rt, Error error) {
        return false;
    }

    // interface NativeDebugger
    @Override
    public WatchVariable[] getWatches() {
        return watches.toArray(new WatchVariable[watches.size()]);
    }

    // interface NativeDebugger
    @Override
    public void replaceWatch(NativeWatch original, String replacewith) {
	// remove the original
	original.postDelete(false);
	// create a new one base on replacewith
	manager().createWatch(replacewith.trim());
	return;
    }

    // interface NativeDebuggerImpl
    protected void restoreWatch(NativeWatch template) {
	// We don't create watches on the gdb side so there's
	// nothing to post. Instead we use MI-based "var"s and scan
	// through them on our own.

	// See DbxDebuggerImpl.newWatch() and dupWatch() for comparison

	NativeWatch nativeWatch = template;

        GdbWatch gdbWatch;
	Object key = nativeWatch.getExpression();
	if (watches.byKey(key) != null) {
	    // duplicate watch
	    gdbWatch =
		new GdbWatch(this, watchUpdater(), nativeWatch.getExpression());
	    // set "value"
	    final String msg = String.format("Duplicate of %d", 999); // NOI18N
	    gdbWatch.setAsText(msg);

	} else {
	    gdbWatch =
		new GdbWatch(this, watchUpdater(), nativeWatch.getExpression());
	    createMIVar(gdbWatch);
	}

	updateMIVar();
	nativeWatch.setSubWatchFor(gdbWatch, this);
        watches.add(gdbWatch);
        manager().bringDownDialog();
        watchUpdater().treeChanged();     // causes a pull
    }

    public void postDeleteAllWatches() {
        // no-op
    }

    public void postDeleteWatch(final WatchVariable variable,
				final boolean spreading) {

        if (!(variable instanceof GdbWatch)) {
            return;
        }
	GdbWatch watch = (GdbWatch) variable;

	if (watch.getMIName() == null) {
	    deleteWatch(variable, spreading);
	} else {
	    MICommand cmd = new DeleteMIVarCommand(watch) {
                @Override
		protected void onDone(MIRecord record) {
		    super.onDone(record);
		    deleteWatch(variable, spreading);
		}

                @Override
		protected void onError(MIRecord record) {
		    super.onDone(record);
		    deleteWatch(variable, spreading);
		}
	    };
	    gdb.sendCommand(cmd);
	}
    }

    public void postDynamicWatch(Variable variable) {
        // TODO does gdb/MI support this ?
    }

    public void postInheritedWatch(Variable watch) {
        // TODO does gdb/MI support this ?
    }

    public void deleteVar(Variable var, MIRecord record) {
        variableBag.remove_count = 0;
        variableBag.remove(var);
        if (Log.Variable.mi_vars) {
            System.out.println("variableBag.remove_count " + // NOI18N
                    variableBag.remove_count);
        }
        variableBag.remove_count = 0;
    }

    public void registerWatchModel(WatchModel model) {
        if (Log.Variable.mi_vars) {
            System.out.println("registerWatchModel " + model); // NOI18N
        }
        watchUpdater().setListener(model);
        if (model != null) {
            get_watches = true;
            if (state().isProcess && !state().isRunning) {
                updateWatches();
            }
        } else {
            get_watches = false;
        }
    }

    /**
     * Try and re-create vars for watches which don't have var's (mi_name's)
     * yet.
     */
    private void retryWatches() {

	for (WatchVariable wv : watches) {
	    GdbWatch w = (GdbWatch) wv;
/* ambiguous in scope, might as well create new one everytime
	    if (w.getMIName() != null)
		continue;		// we already have a var for this one
*/
	    createMIVar(w);
	}
    }

    private void updateWatches() {
	retryWatches();
        updateMIVar();
    }

    private void updateVarAttr(GdbVariable v, MIRecord attr, boolean evalValue) {
        MITList attr_results = attr.results();
        String value = attr_results.valueOf("attr").asConst().value(); // NOI18N
        v.setEditable(value);
        if (v.isEditable() && evalValue) {
            evalMIVar(v);
        }
    }
    
    public static final String STRUCT_VALUE = "{...}"; // NOI18N

    private void updateValue(GdbVariable v, MIRecord varvalue) {
        MITList value_results = varvalue.results();
        MIValue miValue = value_results.valueOf("value"); //NOI18N
        String value = null;
        if (miValue != null) {
            value = miValue.asConst().value();
        }
        value = processValue(value);
        v.setAsText(value);
        if (v.isWatch()) {
            watchUpdater().treeNodeChanged(v); // just update this node
        } else {
            localUpdater.treeNodeChanged(v); // just update this node
        }
    }
    
    private static String processValue(String value) {
        if (value == null) {
	    return STRUCT_VALUE;
        } else if (value.startsWith("[") && value.endsWith("]")) { //NOI18N
            // detect arrays, see IZ 192927
            return STRUCT_VALUE;
        }
        return value;
    }

    private void interpMIChildren(GdbVariable parent,
				  MIRecord miRecord,
				  int level) {
        MITList results = miRecord.results();
        String count = results.valueOf("numchild").asConst().value(); // NOI18N
        MITList children_list = (MITList) results.valueOf("children"); // NOI18N

        int size = Integer.parseInt(count);

        // iterate through children list
	List<GdbVariable> children = new ArrayList<GdbVariable>();
        for (int vx = 0; vx < size; vx++) {
            MIResult childresult = (MIResult) children_list.get(vx);

            // full qualified name,
            // e.g. "var31.public.proc.private.p_proc_heap"
            String qname = childresult.value().asTuple().valueOf("name").asConst().value(); // NOI18N
            // display name,
            // e.g. "p_proc_heap"
            String exp = childresult.value().asTuple().valueOf("exp").asConst().value(); // NOI18N
            String numchild = childresult.value().asTuple().valueOf("numchild").asConst().value(); // NOI18N
            String value = childresult.value().asTuple().valueOf("value").asConst().value(); // NOI18N
            MIValue mitype = childresult.value().asTuple().valueOf("type"); // NOI18N

            String type = "";
            if (mitype != null) {
                type = mitype.asConst().value();
            }

            if (exp.equals("private") || exp.equals("public") || // NOI18N
					exp.equals("protected")) { // NOI18N
                getMIChildren(parent, qname, level+1);
            } else {
                // Show array name and index instead of only index, IZ 192123
                try {
                    Integer.parseInt(exp);
                    exp = parent.getVariableName() + '[' + exp + ']';
                } catch (Exception e) {
                    // do nothing
                }
                GdbVariable childvar = new GdbVariable(this, parent.getUpdater(),
                        parent, exp, null, null, parent.isWatch());
                value = processValue(value);
                childvar.setAsText(value);
                childvar.setType(type);
                childvar.setMIName(qname);
                childvar.setNumChild(numchild);
                variableBag.add(childvar);
		children.add(childvar);
                attrMIVar(childvar, false);
            }
	}

	// make a pull to update children's value
	GdbVariable[] vars = new GdbVariable[children.size()];
	if (level == 0)
	    parent.setChildren(children.toArray(vars), true);
	else
	    parent.addChildren(children.toArray(vars), true);

	// make a pull to update children's value
	// parent.setChildren(childrenvar, true); 
    }

    /** 
     * process a -var-update command
     */
    private void interpUpdate(MIRecord var) {
        MITList varsresults = var.results();
        MITList update_list = (MITList) varsresults.valueOf("changelist"); // NOI18N
        if (Log.Variable.mi_vars) {
            System.out.println("update_list " + update_list.toString()); // NOI18N
        }

        // iterate through update list
        for (MITListItem item : update_list) {
            MIValue updatevar;

	    // On the Mac a 'changelist' is a list of results not values
	    if (update_list.isResultList()) {
		MIResult result = (MIResult)item;
		assert result.variable().equals("varobj");
		updatevar = result.value();
	    } else {
		updatevar = (MIValue)item;
	    }

            String mi_name = updatevar.asTuple().valueOf("name").asConst().value(); // NOI18N
            String in_scope = updatevar.asTuple().valueOf("in_scope").asConst().value(); // NOI18N
            if (Log.Variable.mi_vars) {
                System.out.println("update name " + mi_name + " in_scope " + in_scope); // NOI18N
            }
            /* not used
            MIValue type_changed_entry = updatevar.asTuple().valueOf("type_changed");
            String type_changed;
            if (type_changed_entry != null)
            type_changed = type_changed_entry.asConst().value();
             */
//            if (in_scope != null && in_scope.equals("true")) { // NOI18N
//                Variable wv = variableBag.get(mi_name, true, VariableBag.FROM_BOTH);
//                if (wv != null) {
//                    evalMIVar(wv);
//                }
//            }
            GdbVariable wv = variableBag.get(mi_name, true, VariableBag.FROM_BOTH);
            if (wv != null) {
		if (wv instanceof GdbWatch && in_scope != null) {
		    GdbWatch w = (GdbWatch) wv;
		    w.setInScope(Boolean.parseBoolean(in_scope));
		}
                evalMIVar(wv);
            }
        }
    }


    /*
     * dynamic type is not supported in MI (base on gdb6.6)
     * this Gdb command won't take effected for MI output
     * e.g. if issues "-var-list-children --all-values var34.protected"
     * after turn on dynamic type, the children of var34.protected
     * won't get members of dynamic type.
     * Can be used for future when MI support dynamic type.
     */
    // interface NativeDebugger
    public void setDynamicType(boolean b) {
        String cmdString;
	if (b) {
	    cmdString = "-gdb-set print object on";			// NOI18N
        } else {
	    cmdString = "-gdb-set print object off";			// NOI18N
        }
	send(cmdString);
	dynamicType = b;
    }

    // interface NativeDebugger
    public boolean isDynamicType() {
	return dynamicType;
    }

    // interface NativeDebugger
    public boolean isStaticMembers() {
	return true; // always show static members
    }

    // interface NativeDebugger
    public void setStaticMembers(boolean b) {
	// no-op
	// GDB TODO
    }

    // interface NativeDebugger
    public boolean isInheritedMembers() {
	return true; // always show inherited members
    }

    // interface NativeDebugger
    public void setInheritedMembers(boolean b) {
	// no-op
	// GDB TODO
    }

    // interface NativeDebugger
    public String[] formatChoices() {
	return new String[] {
	    "binary", "octal", "decimal", "hexadecimal", "natural" // NOI18N
	};
    }

    private void interpVarFormat(GdbVariable v, MIRecord record) {
        MITList format_results = record.results();
        String format = format_results.valueOf("format").asConst().value(); // NOI18N
        v.setFormat(format);
	evalMIVar(v);
    }

    void postVarFormat(final GdbVariable v, String format) {
        String expr = v.getMIName();
	// update variable output format
        String cmdString = "-var-set-format " + expr + " " + format; // NOI18N
        MICommand cmd =
                new MiCommandImpl(cmdString) {

		    @Override
                    protected void onDone(MIRecord record) {
                        interpVarFormat(v, record);
                        finish();
                    }
                };
        gdb.sendCommand(cmd);
    }

    private void interpVar(GdbVariable v, MIRecord var) {
        MITList results = var.results();
        String mi_name = results.valueOf("name").asConst().value(); // NOI18N
        String type = results.valueOf("type").asConst().value(); // NOI18N
        String numchild = results.valueOf("numchild").asConst().value(); // NOI18N

        v.setMIName(mi_name);
        v.setType(type);
        v.setNumChild(numchild); // also set children if there is any
	Variable wv = variableBag.get(mi_name, true, VariableBag.FROM_BOTH);
        if (wv == null) {
            variableBag.add(v);
        }
        attrMIVar(v, true);
    }

    private void attrMIVar(final GdbVariable v, final boolean evalValue) {
        String expr = v.getMIName();
	// editable ?
        String cmdString = "-var-show-attributes " + expr; // NOI18N
        MICommand cmd =
            new MiCommandImpl(cmdString) {
            @Override
                    protected void onDone(MIRecord record) {
                        updateVarAttr(v, record, evalValue);
                        finish();
                    }
                };
        gdb.sendCommand(cmd);
    }


    private void updateMIVar() {
        String cmdString = "-var-update * "; // NOI18N
        MICommand cmd =
            new MiCommandImpl(cmdString) {

            @Override
                    protected void onDone(MIRecord record) {
                        interpUpdate(record);
                        finish();
                    }

            @Override
                    protected void onError(MIRecord record) {
                        String errMsg = getErrMsg(record);

                        // to work around gdb "corrupt stack" problem
                        if (try_one_more && errMsg.equals(corrupt_stack)) {
                            try_one_more = true;
                        //updateMIVar();
                        }
                        // to work around gdb "out of scope" problem
                        String out_of_scope = "mi_cmd_var_assign: Could not assign expression to varible object"; // NOI18N
                        if (!errMsg.equals(out_of_scope)) {
                            genericFailure(record);
                            finish();
                        }
                    }
                };

        gdb.sendCommand(cmd);
    }

    private void evalMIVar(final GdbVariable v) {
        String mi_name = v.getMIName();
	// value of mi_name
        String cmdString = "-var-evaluate-expression " + mi_name; // NOI18N
        final MICommand cmd =
            new MiCommandImpl(cmdString) {

            @Override
                    protected void onDone(MIRecord record) {
                        updateValue(v, record);
                        finish();
                    }

            @Override
                    protected void onError(MIRecord record) {
                        String errMsg = getErrMsg(record);

                        // to work around gdb "out of scope" problem
                        String out_of_scope = "mi_cmd_var_assign: Could not assign expression to varible object"; // NOI18N
                        if (!errMsg.equals(out_of_scope)) {
                            genericFailure(record);
                            finish();
                        }
                    }
                };
        gdb.sendCommand(cmd);
    }

    private class DeleteMIVarCommand extends MiCommandImpl {

	private final GdbVariable v;

	public DeleteMIVarCommand(GdbVariable v) {
	    super("-var-delete " + v.getMIName()); // NOI18N
	    this.v = v;
	}

        @Override
	protected void onDone(MIRecord record) {
	    deleteVar(v, record);
	    finish();
	}
    }

    /*
     * this MI call would create MI Vars for each child automatically by gdb
     */
    void getMIChildren(final GdbVariable parent,
			      String expr,
			      final int level) {

        String cmdString = "-var-list-children --all-values " + expr; // NOI18N
        MICommand cmd =
            new MiCommandImpl(cmdString) {
		    @Override
                    protected void onDone(MIRecord record) {
                        interpMIChildren(parent, record, level);
                        finish();
                    }
                };
        gdb.sendCommand(cmd);
    }

    private void createMIVar(final GdbVariable v) {
        String expr = MacroSupport.expandMacro(this, v.getVariableName());
        String cmdString = "-var-create - * " + expr; // NOI18N
        MICommand cmd =
            new MiCommandImpl(cmdString) {

            @Override
                protected void onDone(MIRecord record) {
		    v.setAsText("{...}");// clear any error messages // NOI18N
		    v.setInScope(true);
                    updateValue(v, record);
                    interpVar(v, record);
                    finish();
                }

            @Override
                protected void onError(MIRecord record) {
		    // If var's being created for watches cannot be parsed
		    // we get an error.
		    String errMsg = getErrMsg(record);
		    v.setAsText(errMsg);
		    v.setInScope(false);
                    finish();
		    watchUpdater().treeChanged();     // causes a pull
                }
	    };
        gdb.sendCommand(cmd);
    }

    /* 
     * local stuff 
     */
    // SHOULD factor with DbxDebuggerImpl's localsMasked
    private boolean get_locals = false; // indicate Locals View open/close
    private int local_count;
    private GdbVariable[] local_vars = new GdbVariable[0];

    public void registerLocalModel(LocalModel model) {
        if (Log.Variable.mi_vars) {
            System.out.println("registerLocalModel " + model); // NOI18N
        }
        localUpdater.setListener(model);
        if (model != null) {
            get_locals = true;
            if ((state().isProcess || state().isCore) && !state().isRunning) {
                // have frame args already
                getMILocals(false); // from current frame
            }
        } else {
            get_locals = false;
        }
    }

    public Variable[] getLocals() {
        return local_vars;
    }

    public int getLocalsCount() {
        return local_count;
    }

    @Override
    public Set<String> requestAutos() {
        Set<String> autoNames = super.requestAutos();
        LinkedList<Variable> res = new LinkedList<Variable>();
        for (String auto : autoNames) {
            GdbVariable var = variableBag.get(auto, false, VariableBag.FROM_BOTH);
            if (var == null) {
                var = new GdbWatch(this, watchUpdater(), auto);
                createMIVar(var);
            }
            res.add(var);
        }
        synchronized (autos) {
            autos.clear();
            autos.addAll(res);
        }
        return autoNames;
    }

    @Override
    public void setShowAutos(boolean showAutos) {
	super.setShowAutos(showAutos);
	if (gdb != null && gdb.connected()) {
	    if (showAutos) {
		requestAutos();
            }
	}
    }

    /*
     * update local vars, include paramaters
     *
     */
    private void setLocals(boolean update_var, MIRecord locals) {
        MITList localsresults = locals.results();
        MITList locals_list = (MITList) localsresults.valueOf("locals"); // NOI18N
        int size = locals_list.size();
        local_count = size;

        MITList param_list = null;
        int params_count = 0;

        // paramaters
        GdbFrame cf = getCurrentFrame();
        if (cf != null) {
            param_list = cf.getMIArgs();
            if (param_list != null) {
                params_count = param_list.size();
            }
        }

        local_count += params_count;
        if (Log.Variable.mi_vars) {
            System.out.println("locals " + locals_list.toString()); // NOI18N
            System.out.println("args " + param_list.toString()); // NOI18N
            System.out.println("local_count " + local_count); // NOI18N
            System.out.println("update_var " + update_var); // NOI18N
        }

        // iterate through local list
        local_vars = new GdbVariable[local_count];
        for (int vx = 0; vx < size; vx++) {
            MIValue localvar = (MIValue) locals_list.get(vx);
            GdbLocal loc = new GdbLocal(localvar);
	    String var_name = loc.getName();
            GdbVariable gv = variableBag.get(var_name, 
                  false, VariableBag.FROM_LOCALS);
            if (gv == null) {
                local_vars[vx] = new GdbVariable(this, localUpdater, null, 
                        var_name, loc.getType(), loc.getValue(), false);
                createMIVar(local_vars[vx]);
            } else {
		gv.setValue(loc.getValue()); // update value
                local_vars[vx] = gv;
            }
        }

        // iterate through frame arguments list
        for (int vx = 0; vx < params_count; vx++) {
            MIValue param = (MIValue) param_list.get(vx);
            GdbLocal loc = new GdbLocal(param);
            GdbVariable gv = null;
	    String var_name = loc.getName();
	    String var_value = loc.getValue();
            if (var_name.equals("this")) {// NOI18N
		int index = var_value.indexOf("0x"); // NOI18N
		if (var_value != null) {
		    String value_only = var_value.substring(index);
		    gv = variableBag.byAddr(var_name, value_only, VariableBag.FROM_LOCALS);
		}
	    }  else {
                gv = variableBag.get(var_name, false, 
                  VariableBag.FROM_LOCALS);
                if (gv != null)
                    gv.setValue(var_value); // update value
            }
            if (gv == null) {
                local_vars[size + vx] = new GdbVariable(this, localUpdater, 
                        null, var_name, loc.getType(), loc.getValue(), false);
                createMIVar(local_vars[size + vx]);
            } else {
                local_vars[size + vx] = gv;
            }
        }
        if (update_var) {
            updateMIVar(); // call var-update * , but results are not reliable
        }
        localUpdater.treeChanged();     // causes a pull
    }

    private void getMILocals(final boolean update_var) {
        MICommand cmd =
            new MiCommandImpl("-stack-list-locals --simple-values") { // NOI18N
            @Override
                    protected void onDone(MIRecord record) {
                        setLocals(update_var, record);
                        finish();
                    }
                };
        gdb.sendCommand(cmd);
    }

    private void getMIDis(String command) {
        MICommand cmd =
            new MiCommandImpl(command) {
            @Override
		protected void onDone(MIRecord record) {
		    setDis(record);
		    finish();
		}
	    };
	gdb.sendCommand(cmd);
    }

    @Override
    protected void openDis() {
        Disassembly.open();
    }
    
    /**
     * Continuation from genericStopped().
     *
     * We get here on a generic stop and after a success or failure of
     * This is not compitable with  "-file-list-exec-source-file" anymore
     * On failure srcRecord is null.
     * "-stack-info-frame". On failure srcRecord is null.
     * 
     */
    private void genericStoppedWithSrc(MIRecord record, MIRecord srcRecord) {
        final MITList srcResults = (srcRecord == null) ? null : srcRecord.results();
	MITList results = (record == null) ? null : record.results();
        // make results null if empty to avoid later checks, IZ194272
        if (results != null && results.isEmpty()) {
            results = null;
        }
        final MIValue reasonValue = (results == null) ? null : results.valueOf("reason"); // NOI18N
        final String reason;
        if (reasonValue == null) {
            reason = "breakpoint-hit"; // temp bpt hit // NOI18N
        } else {
            reason = reasonValue.asConst().value();
        }

        if (reason.equals("exited-normally")) { // NOI18N
            noteProcGone(reason, results);
            return;

        } else if (reason.equals("exited")) { // NOI18N
            noteProcGone(reason, results);
            return;

        } else if (reason.equals("exited-signalled")) { // NOI18N
            noteProcGone(reason, results);
            return;

        } else if (reason.equals("breakpoint-hit") || // NOI18N
            reason.equals("end-stepping-range") || // NOI18N
            reason.equals("location-reached") || // NOI18N
            reason.equals("signal-received") || // NOI18N
            reason.equals("function-finished")) { // NOI18N

	    // update our views

            NativeBreakpoint breakpoint = null;
            MIValue bkptnoValue = (results != null) ? results.valueOf("bkptno") : null; // NOI18N
            if (bkptnoValue != null) {
		// It's a breakpoint event
                String bkptnoString = bkptnoValue.asConst().value();
                int bkptno = Integer.parseInt(bkptnoString);
                Handler handler = bm().findHandler(bkptno);
                if (handler != null) {
                    handler.setFired(true);
                    breakpoint = handler.breakpoint();
                }
                // updateFiredEvent will set status
            }

            MIValue frameValue = (results != null) ? results.valueOf("frame") : null; // NOI18N

	    // Mac 10.4 gdb provides no "frame" attribute

            // For the scenario that stack view is closed and local view
            // is open, we need frame params info from here.
            if (get_locals && frameValue != null) {
                // needs to get args info
                guiStackFrames = new GdbFrame[1];
                // frameValue include args  info
                guiStackFrames[0] = new GdbFrame(this, frameValue, null);
            }

	    if (srcResults != null) {
                MITList stack = srcResults.valueOf("stack").asList(); // NOI18N
		if (false) {
		    // We have information about what src location we're
		    // stopped in.
		    MITList frameTuple = null;
		    if (frameValue != null)
			frameTuple = frameValue.asTuple();
		    homeLoc = MILocation.make(this, frameTuple, srcResults, false, stack.size(), breakpoint);

		} else {
                    frameValue = ((MIResult)stack.asList().get(0)).value();
		    MITList frameTuple = frameValue.asTuple();
		    homeLoc = MILocation.make(this, frameTuple, null, false, stack.size(), breakpoint);
		}

		visitedLocation = MILocation.make(homeLoc, false);
		setVisitedLocation(visitedLocation);
                
                state().isUpAllowed = !homeLoc.bottomframe();
                state().isDownAllowed = !homeLoc.topframe();
                setStack(srcRecord);
	    }

//            if (get_frames || get_locals) {
//                showStackFrames();
//            }

            if (get_threads) {
                showThreads();
            }

            if (get_watches) {
                updateWatches();
            }
            
            if (get_registers) {
                requestRegisters();
            }
            
            state().isProcess = true;
        }

        if (record != null) {
            explainStop(reason, record);
        }

        stateSetRunning(false);
        stateChanged();
	session().setSessionState(state());
    }

    private boolean haveCountingBreakpoints() {
	for (Handler h : bm().getHandlers()) {
	    NativeBreakpoint b = h.breakpoint();
	    if (b != null && b.hasCountLimit())
		return true;
	}
	return false;
    }

    /**
     * Re-reset the ignore count.
     *
     * Once a bpts ignore count reaches 0 it stops getting ignored.
     * That would mean that if we re-run we'll hit the bpt on the first time.
     * This is different from the dbx-style semantics that we've adopted,
     * so if the bpts ignore count has been reset we re-reset it back 
     * based on count limit.
     *
     * If instead of re-running we resume then the bpt will be hit
     * after countlimit more tries. For example, if count-limit is set to
     * 2, the bpt will be hit on the 2nd, 4th, 6th etc counts.
     *
     * Not that with gdb the actual count of the bpt will keep growing 
     * whereas in dbx it gets reset and never exceeds the limit.
     */
    private void adjustIgnore(NativeBreakpoint b, MITList props) {
	assert b.hasCountLimit() :
	       "adjustIgnore() called on a bpt w/o a count limit"; // NOI18N
	MIValue ignore = props.valueOf("ignore"); // NOI18N
	if (ignore != null) 
	    return;

	/* 
	System.out.printf("Handler %d has count limit of %d but gdb reset it\n", hid, h.breakpoint().getCountLimit());
	*/

	long limit = b.getCountLimit();
	int newIgnore;
	if (limit == -1)
	    newIgnore = GdbHandlerExpert.infinity;
	else
	    newIgnore = (int) limit - 1;
	send("-break-after " + b.getId() + ' ' + newIgnore); // NOI18N
    }

    /**
     * Process the reply from -break-list and update active counts.
     *
     * Also gdb (6.4 at least) has a bug where ignore counts get eliminated,
     * reset to 0, under MI. I can't even tell when they seem to get reset
     * since the get reset if they get hit or if they don't.
     * So we go through and re-reset the ignore counts for any counting
     * breakpoints.
     */
    private void updateCounts(MIRecord record) {
	MITList bptresults = record.results();
	MITList table =
	    bptresults.valueOf("BreakpointTable").asTuple();	// NOI18N
	MITList bpts = table.valueOf("body").asList();		// NOI18N
	System.out.printf("updateCounts: %d bpts\n", bpts.size()); // NOI18N

	for (int bx = 0; bx < bpts.size(); bx++) {
	    MIResult b = (MIResult) bpts.get(bx);
	    // System.out.printf("b %s\n", b.toString());

	    MITList props = b.value().asTuple();
	    // System.out.printf("props %s\n", props.toString());

	    MIValue number = props.valueOf("number");	// NOI18N

	    int hid = Integer.parseInt(number.asConst().value());
	    Handler h = bm().findHandler(hid);

	    if (h != null && h.breakpoint().hasCountLimit()) {
		MIValue times = props.valueOf("times");	// NOI18N
		int count = Integer.parseInt(times.asConst().value());
		h.setCount(count);

		adjustIgnore(h.breakpoint(), props);
	    }
	}
	System.out.printf("............................................\n"); // NOI18N
    }
    
    void genericStopped(final MIRecord stopRecord) {
        // Get as much info about the stopped src code location
        /* OLD
         * for gdb 6.3/6.4 ( can debug gcc-build dbx on linux )
         * 6.3/6.4 does not support "-stack-info-frame"
         * On the Mac -file-list-exec-source-file seems to return some
         * unrelated constant value

        MICommand cmd =
        new AbstractMICommand(0, "-file-list-exec-source-file") {
        protected void onDone(MIRecord record) {
        genericStoppedWithSrc(stopRecord, record);
        finish();
        }
        };
        gdb.sendCommand(cmd);
         */

        final MITList results = stopRecord.results();
        
        // detect first stop (in _start or main)
        if (firstBreakpointId != null) {
            MIValue bkptnoValue = results.valueOf("bkptno"); // NOI18N
            if (bkptnoValue == null ||
               (bkptnoValue != null && (firstBreakpointId.equals(bkptnoValue.asConst().value())))) {
                    firstBreakpointId = null;
                    sendPidCommand(true);
                    return;
            }
        }
        
        //detect silent stop
        if (gdb.isSignalled()) {
            final MIValue reasonValue = results.valueOf("reason"); //NOI18N
            if (reasonValue != null && "signal-received".equals(reasonValue.asConst().value())) { //NOI18N
                MIValue signalValue = results.valueOf("signal-name"); //NOI18N
                if (signalValue != null) {
                    String signal = signalValue.asConst().value();
                    if ("SIGCONT".equals(signal)) { // NOI18N
                        // continue after silent stop
                        gdb.resetSignalled();
                        go();
                        return;
                    } else if ("SIGINT".equals(signal)) { // NOI18N
                        // silent stop
                        if (gdb.isSilentStop()) {
                            gdb.resetSilentStop();
                            state().isRunning = false;
                            return;
                        }
                    } else if ("SIGTRAP".equals(signal) && // NOI18N
                            (getHost().getPlatform() == Platform.Windows_x86 ||
                            getHost().getPlatform() == Platform.MacOSX_x86)) {
                        // see IZ 172855 (On windows we need to skip SIGTRAP)
                        if (gdb.isSilentStop()) {
                            gdb.resetSignalled();
                            // silent stop
                            state().isRunning = false;
                            return;
                        }
                    } else {
                        gdb.resetSignalled();
                    }
                }
            }
        }

	requestStack(stopRecord);

	// If we have any counting bpts poll the bpt list in order to
	// learn the current bpt counts.
	if (haveCountingBreakpoints()) {
	    MICommand cmd = new MiCommandImpl("-break-list") { // NOI18N
                @Override
		protected void onDone(MIRecord record) {
		    updateCounts(record);
		    finish();
		}
	    };
	    gdb.sendCommand(cmd);
	}
    }
    
    protected void requestStack(final MIRecord stopRecord) {
        MICommand cmd =
            new MiCommandImpl("-stack-list-frames") { // NOI18N
                @Override
                protected void onDone(MIRecord record) {
                    genericStoppedWithSrc(stopRecord, record);
                    finish();
                }
                @Override
                protected void onError(MIRecord record) {
                    genericStoppedWithSrc(stopRecord, null);
                    finish();
                }
            };
        gdb.sendCommand(cmd);
    }
    
    /**
     * The program has hit a signal; produce a popup to ask the user
     * how to handle it.
     */
    private void showSignalPopup(String description, String sigName) {
	SignalDialog sd = new SignalDialog();

	// LATER SHOULD factor this info into a class
	String signame = "?"; // NOI18N
	String signum = "?"; // NOI18N
	String usercodename = "?"; // NOI18N
	String usercodenum = "?"; // NOI18N
	String senderpid = "?"; // NOI18N

	signame = sigName;
	// gdb doesn't furnish any of the other info

	String signalInfo;
	signalInfo = Catalog.format("FMT_SignalInfo", // NOI18N
	    signame, signum, usercodename, usercodenum);
	sd.setSignalInfo(signalInfo);

	sd.setSenderInfo(senderpid);

	if (session != null) {
	    sd.setReceiverInfo(session.getShortName(), session.getPid());
	} else {
	    sd.setReceiverInfo("", 0);
	}

	// get disposition of signal
	// LATER: use "info signal" to initialize gdb's signal disposition
	// LATER: lookup disposition by name?

	Signals.InitialSignalInfo dsii = null;
	int signo = 0;
	int index = 0;
	DbgProfile debugProfile = getNDI().getDbgProfile();
	dsii = debugProfile.signals().getSignal(index);

	boolean wasIgnored = false;
	if (dsii != null) {
	    wasIgnored = ! dsii.isCaught();
	    sd.setIgnore(true, wasIgnored);
	} else {
	    sd.setIgnore(true, false); // default
	}

	sd.show();

	if (dsii != null && sd.isIgnore() != wasIgnored) {
	    String cmd;
	    if (sd.isIgnore()) {
		// gdb seems to not be able to ignore caught signals???
		cmd = "ignore signal " + sigName; // NOI18N
	    } else {
		cmd = "catch signal " + sigName; // NOI18N
	    }
            send(cmd);
	}

	boolean signalDiscarded = sd.discardSignal();
	if (signalDiscarded) {
	    deliverSignal = -1;
	} else {
	    deliverSignal = signo;
	}

	if (sd.shouldContinue()) {
	    go();
	}
    }

    private void explainStop(String reason, MIRecord record) {
        final MITList results = record.results();

        String stateMsg = reason;
	String signalName = "<UNKNOWN>"; // NOI18N

        if (reason.equals("end-stepping-range")) {		// NOI18N
            stateMsg = Catalog.get("Dbx_program_stopped");	// NOI18N
        } else if (reason.equals("signal-received")) {		// NOI18N
	    final MIValue signalNameValue =
		results.valueOf("signal-name");		// NOI18N
	    if (signalNameValue != null)
		signalName = signalNameValue.asConst().value();
            stateMsg = Catalog.get("Dbx_signal") + // NOI18N
		       " " + signalName;			// NOI18N

        } else if (reason.equals("function-finished")) { // NOI18N
            stateMsg = Catalog.get("Dbx_program_stopped");	// NOI18N
        } else if (reason.equals("breakpoint-hit")) {		// NOI18N
            stateMsg = Catalog.get("Dbx_program_stopped");	// NOI18N
        } else {
            stateMsg = "Stopped for unrecognized reason: " + reason; // NOI18N
        }

	if (stateMsg != null)
	    setStatusText(stateMsg);

        if (reason.equals("signal-received") && !gdb.isSignalled()) { //NOI18N
	    showSignalPopup(stateMsg, signalName);
	}
    }

    /**
     * Convert a string into a C (C++,Java) string.
     *
     * For example, the following ... well I was going to write an example
     * using the backslash but being that it's a unicode escape character
     * 'javac' ends up complaining about it.
     *     XXX
     * becomes
     *     "c:Documents and Settings\\user\\Projects"
     */
    private static String toCString(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (int sx = 0; sx < s.length(); sx++) {
            char c = s.charAt(sx);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        sb.append('"');
        return sb.toString();
    }

    /**
     * Resue this session with a new debug target
     */
    public void reuse(NativeDebuggerInfo di) {
        // Tell gdb what to debug
        debug((GdbDebuggerInfo) di);
    }

    private void debug(GdbDebuggerInfo gdi) {
        String program = gdi.getTarget();
        long pid = gdi.getPid();
        String corefile = gdi.getCorefile();
        final boolean isCore = (corefile != null);

        profileBridge.setup(gdi);

        if (corefile != null) {
            // debug corefile
            if (program == null) {
                program = "-"; // NOI18N
            }

        } else if (pid != -1) {
            // attach
            if (program == null) {
                program = "-"; // NOI18N
            }

        } else {
            // raw gdb session, no need to send 'file' cmd.
            if (program == null) {
                return;
            }

        // load program
        }
        
        String outputFile = ((MakeConfiguration)gdi.getConfiguration()).getAbsoluteOutputValue();
        outputFile = localToRemote("symbol-file", outputFile); //NOI18N
        if (!CndPathUtilitities.sameString(program, outputFile)) {
            // load symbol file separately, IZ 194531
            send("-file-symbol-file " + toCString(outputFile), false); // NOI18N
        }

        String tmp_cmd;
        if (isCore || pid != -1) {
            tmp_cmd = "-file-symbol-file "; // NOI18N
        } else {
            tmp_cmd = "-file-exec-and-symbols "; // NOI18N
        }

        final String mi_command = tmp_cmd;
        final String fprogram = program;

        // There is no way to determine correct file mapper here, see #191835
        //final String mprogram = toCString(fmap.worldToEngine(program));
        final String mprogram = toCString(program);

	// mainly load symbol table
	// -file-core-file is not implemented in gdb 6.1
	// use CLI command "core-file" instead
        MICommand cmd =
            new MiCommandImpl(mi_command + ' ' + mprogram) {

            @Override
            protected void onDone(MIRecord record) {
                if (isCore) {
                    state().isCore = true;
                } else {
                    getFullPath(null);
                }

		gdb.startProgressManager().finishProgress();
                session().setTarget(fprogram);
                session().update();
		session().setSessionEngine(GdbEngineCapabilityProvider.getGdbEngineType());

                state().isLoaded = true;
                stateChanged();
		session().setSessionState(state());

                noteProgLoaded(fprogram);

                finish();
            }

            @Override
            protected void onError(MIRecord record) {
		gdb.startProgressManager().finishProgress();
                /* LATER
                session().setTarget(fprogram);
                session().update();
                 */

                state().isLoaded = false;
                stateChanged();
		session().setSessionState(state());

                genericFailure(record);
                finish();
            }
        };
        gdb.sendCommand(cmd);
    }

    // interface BreakpointProvider
    public HandlerExpert handlerExpert() {
        return handlerExpert;
    }

    // interface BreakpointProvider
    public void postRestoreHandler(final int rt, HandlerCommand hc) {

        final MICommand cmd = new MIRestoreBreakCommand(rt, hc.toString());

	//
	// What's the inject("1\n") about?
	//
	// 1 is the "all" choice for overloaded function names so we
	// pre-issue it. If we don't do this then onUserInteraction() will
	// get called against the wrong command because it only works
	// with the current outstanding command.
	// If the break command has no overloaded methods and there is no menu
	// then there is no harm done either. gdb simply responds with
	// 	1^done
	// and we also ignore it.
	//
	// An alternative, more robust, solution is to chain all these commands
	// together and let the regular overload resolution handle everything.
	// Chaining a bunch of bpt commands in a loop is easy. It's the
	// trickiness of ensuring that the following commands are also
	// chained that makes me favor the pre-injection solution ... for now.

	if (Log.Bpt.fix6810534) {
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    gdb.sendCommand(cmd);
		    gdb.tap().inject("1\n");	// TMP // NOI18N
		}
	    });
	} else {
	    gdb.sendCommand(cmd);
	    gdb.tap().inject("1\n");	// TMP // NOI18N
	}
    }

    private final class DisModel extends DisModelSupport {

	/*
	 * Parse list of instructions
	 * See gdb/mi/README.examples for examples.
	 */
	private void parseDisasm(MITList inss) {
	    for (int ix = 0; ix < inss.size(); ix++) {
		MITList ins = ((MIValue) inss.get(ix)).asTuple();
		String address = ins.valueOf("address").asConst().value(); // NOI18N

		MIValue fnameValue = ins.valueOf("func-name"); // NOI18N
		String fname;
		String offset = null;
		if (fnameValue != null) {
		    fname = fnameValue.asConst().value();
		    offset = ins.valueOf("offset").asConst().value(); // NOI18N
		} else {
		    fname = Catalog.get("MSG_UnknownFunction");	// NOI18N
		}

		String inst = ins.valueOf("inst").asConst().value(); // NOI18N
		/*
		System.out.printf("\t%s: %s+%s: %s\n",
		    address, fname, offset, inst);
		*/

		if (offset != null)
		    add(address + ":", fname + "+" + offset + ":\t" + inst); // NOI18N
		else
		    add(address + ":", fname + ":\t" + inst); // NOI18N
	    }
	}

	/**
	 * Interpret disassembly in 'record', stuff it into this
	 * DisFragModel and update(), notifying the DisView.
	 */

        public void parseRecord(MIRecord record) {
            clear();

	    StopWatch sw = new StopWatch("Parse MI instructions"); // NOI18N
	    sw.start();

	    MITList asm_insnsR = record.results();
	    MITList lines = asm_insnsR.valueOf("asm_insns").asList(); // NOI18N

	    if (lines.isValueList()) {
		// disassembly only
		parseDisasm(lines);

	    }  else {
		// src lines and disassembly
		for (int lx = 0; lx < lines.size(); lx++) {
		    MIResult src_and_asm_lineR = (MIResult) lines.get(lx);
		    MITList src_and_asm_line =
			src_and_asm_lineR.value().asTuple();

		    String line =
			src_and_asm_line.valueOf("line").asConst().value(); // NOI18N
		    String file =
			src_and_asm_line.valueOf("file").asConst().value(); // NOI18N
		    MITList inss = 
			src_and_asm_line.valueOf("line_asm_insn").asList(); // NOI18N

		    /*
		    System.out.printf("%s:%s\n", file, line);
		    */
		    add(line, file);

		    parseDisasm(inss);
		}
	    }
	    sw.stop();
	    // sw.dump();

	    update();
        }
    }

    private class DisController extends ControllerSupport {

	protected void setBreakpointHelp(String address) {
	    // Similar to ToggleBreakpointActionProvider.doAction
	    NativeBreakpoint b = NativeBreakpoint.newInstructionBreakpoint(address);
	    if (b != null) {
		int routingToken =
		    RoutingToken.BREAKPOINTS.getUniqueRoutingTokenInt();
		Handler.postNewHandler(GdbDebuggerImpl.this, b, routingToken);
	    }
	    // We'll continue in newHandler() or ?error?
	}

	/*
	 * Get some disassembly around the current visiting location.
	 *
	 * gdb-mi syntax is one of
	 *	-data-disassemble -s <start> -e <end> -- <mode>
	 *	-data-disassemble -f <file> -l <line> [ -n <Nins> ]  -- <mode>
	 * 
	 * <mode> if 0 provides only assembly, if 1 provides line info
	 * and assembly.
	 *
	 * NOTES:
	 *
	 * - gdb will automatically subtract from -l. As the docs say, it's
	 *   "line number to disassemble _around_".
	 *   This also means that subtracting our own lines as we do for dbx 
	 *   is unneccessarty. Moreover if the number of lines we subtract
	 *   reaches back into the _previous_ function, gbd will only give us
	 *   disassembly for that one.
	 *
	 * - It seems gdb will return a reasonable "window" when -n isn't
	 *   specified.
	 *
	 * - Apparenty -n specifies the number of _instructions_ not lines as
	 *   implied by the gdb docs.
	 *
	 * - Unlike what the gdb docs for -n imply, you cannot mix and match
	 *   -e and -n
	 *
	 * - -n of -1 wil disassemble the whole function but we avoid that
	 *   since it will swamp us for very large functions.
	 *
	 * - We will always ask for src line info.
	 */

        // interface Controller
        public void requestDis() {
            if (visitedLocation == null)
                return;

	    String cmd = "-data-disassemble"; // NOI18N
	    if (visitedLocation.hasSource()) {
		// request by line #

		// 6742661
		if (visitedLocation.line() <= 0)
		    visitedLocation = visitedLocation.line(1);

		String file = visitedLocation.src();
		file = localToRemote("requestDis", file); // NOI18N
		cmd += " -f " + file; // NOI18N
		cmd += " -l " + visitedLocation.line(); // NOI18N
		cmd += " -- 1";		// provide src lines as well // NOI18N

	    } else {
                cmd += " -s $pc -e \"$pc+1000\" -- 1"; //NOI18N
	    }
	    requestDisFromGdb(cmd);
        }

        // interface Controller
        public void requestDis(String start, int count) {
	    /* 
	    System.out.printf("DisController.requestDis(%s, %d)\n",
		start, count);
	    System.out.printf("%s\n", getVisitedLocation);
	    */
	    if (start == null)
		return;

	    String cmd = "-data-disassemble"; // NOI18N
	    cmd += " -s " + start; // NOI18N
	    cmd += " -e " + start + "+" + count; // NOI18N
	    cmd += " -- 1";		// provide disassembly only // NOI18N
	    requestDisFromGdb(cmd);
        }
    }

    // interface NativeDebugger
    public void registerDisassemblerWindow(DisassemblerWindow w) {
	assert w == null || w == disassemblerWindow();

	boolean makeAsmVisible = (w != null);
	if (makeAsmVisible == isAsmVisible())
	    return;

        if (postedKillEngine)
            return;

        if (!isConnected())
            return;

	if (! viaShowLocation) {
	    // I.e. user clicked on Disassembly tab or some other tab
	    if (makeAsmVisible)
		requestDisassembly();
	    else
		requestSource(false);
	}

        if (makeAsmVisible) {
	    setAsmVisible(true);
        } else {
	    setAsmVisible(false);
        }
    }

    // implement NativeDebuggerImpl
    protected DisFragModel disModel() {
	return disModel;
    }

    // implement NativeDebuggerImpl
    protected Controller disController() {
	return disController;
    }
    
    Disassembly getDisassembly() {
        return disassembly;
    }

    private void requestDisFromGdb(String cmd) {
	// DEBUG System.out.printf("requestDisFromGdb(%s)\n", cmd);
	if (postedKill || postedKillEngine || gdb == null || cmd == null)
	    return;
	getMIDis(cmd);
    }

    private void setDis(MIRecord record) {
	disModel.parseRecord(record);
        disassembly.update(record.toString());

	// 6582172
	if (update_dis)
	    disStateModel().updateStateModel(visitedLocation, false);
    }

    @Override
    public void requestDisassembly() {
        Disassembly.open();
    }

    private static final int MEMORY_READ_WIDTH = 16;
    
    public void requestMems(String start, String length, String format, int index) {
        int lines;
        try {
            lines = (Integer.valueOf(length)-1)/MEMORY_READ_WIDTH+1;
        } catch (Exception e) {
            return;
        }
        MICommand cmd = new MiCommandImpl("-data-read-memory " + start + " x 1 " + lines + " " + MEMORY_READ_WIDTH + " .") { // NOI18N
            @Override
            protected void onDone(MIRecord record) {
                if (memoryWindow != null) {
                    LinkedList<String> res = new LinkedList<String>();
                    for (MITListItem elem : record.results().valueOf("memory").asList()) { //NOI18N
                        StringBuilder sb = new StringBuilder();
                        MITList line = ((MITList)elem);
                        String addr = line.valueOf("addr").asConst().value(); //NOI18N
                        sb.append(addr).append(':'); //NOI18N
                        MIValue dataValue = line.valueOf("data"); //NOI18N
                        for (MITListItem dataElem : dataValue.asList()) {
                            sb.append(' ').append(((MIConst)dataElem).value());
                        }
                        String ascii = line.valueOf("ascii").asConst().value(); //NOI18N
                        sb.append(" \"").append(ascii).append("\""); //NOI18N
                        res.add(sb.toString() + "\n"); //NOI18N
                    }
                    memoryWindow.updateData(res);
                }
                finish();
            }
        };
        // LATER: sometimes it is sent too early, need to investigate
        if (gdb != null) {
            gdb.sendCommand(cmd);
        }
    }
    
    private Map<Integer, String> regNames = null;

    public void requestRegisters() {
        //check that we have regNames
        if (regNames == null) {
            MICommand cmd = new MiCommandImpl("-data-list-register-names") { // NOI18N
                @Override
                protected void onDone(MIRecord record) {
                    Map<Integer, String> res = new HashMap<Integer, String>();
                    int idx = 0;
                    for (MITListItem elem : record.results().valueOf("register-names").asList()) { //NOI18N
                        res.put(idx++, ((MIConst)elem).value());
                    }
                    regNames = res;
                    finish();
                }
            };
            // LATER: sometimes it is sent too early, need to investigate
            if (gdb != null) {
                gdb.sendCommand(cmd);
            }
        }
        
        MICommand cmd = new MiCommandImpl("-data-list-register-values x") { // NOI18N
            @Override
            protected void onDone(MIRecord record) {
                if (registersWindow != null) {
                    LinkedList<String> res = new LinkedList<String>();
                    for (MITListItem elem : record.results().valueOf("register-values").asList()) { //NOI18N
                        StringBuilder sb = new StringBuilder();
                        MITList line = ((MITList)elem);
                        String number = line.valueOf("number").asConst().value(); //NOI18N
                        // try to get real name
                        try {
                            number = regNames.get(Integer.valueOf(number));
                        } catch (Exception e) {
                            Exceptions.printStackTrace(e);
                        }
                        sb.append(number).append(' ');
                        String value = line.valueOf("value").asConst().value(); //NOI18N
                        sb.append(value);
                        res.add(sb.toString());
                    }
                    registersWindow.updateData(res);
                }
                finish();
            }
        };
        // LATER: sometimes it is sent too early, need to investigate
        if (gdb != null) {
            gdb.sendCommand(cmd);
        }
    }

    public void registerEvaluationWindow(EvaluationWindow w) {
        notImplemented("registerEvaluationWindow()");	// NOI18N
    }

//    public void registerArrayBrowserWindow(ArrayBrowserWindow w) {
//        notImplemented("registerArrayBrowserWindow()");	// NOI18N
//    }

    private void newHandlers(int rt, MIBreakCommand cmd, MIRecord record) {
	MITList results = record.results();
	for (int tx = 0; tx < results.size(); tx++) {
	    MIResult result = (MIResult) results.get(tx);
            if (result.matches("bkpt")) { //NOI18N
                newHandler(rt, cmd, result);
            }
	}
    }

    private void newHandler(int rt, MIBreakCommand cmd, MIResult result) {
	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Bpt.pathway) {
	    System.out.printf("GdbDebuggerImpl.newHandler(%s)\n", result); // NOI18N
	}

        Handler handler = null;
	try {
	    BreakpointPlan bp = bm().getBreakpointPlan(rt, BreakpointMsg.NEW);

	    /* LATER
	     See LATER below
	    // remember enable state before we process incoming bpt data
	    boolean disable = !bj.midlevel().isEnabled();
	    */

	    NativeBreakpoint template = bp.template();

	    switch (bp.op()) {
		case NEW:
		    handler = handlerExpert.newHandler(template, result, null);
                    // fix for #193505
                    if (!template.isEnabled()) {
                        postEnableHandler(rt, handler.getId(), false);
                    }
		    break;
		case RESTORE:
		    handler = handlerExpert.newHandler(template, result, bp.restored());
                    // fix for #193505
                    if (!template.isEnabled()) {
                        postEnableHandler(rt, handler.getId(), false);
                    }
		    assert handler.breakpoint() == bp.restored();
		    break;
		case MODIFY:
		    handler = bp.originalHandler();
		    handler = handlerExpert.replaceHandler(template, handler, result);
		    break;
	    }

	    handlerExpert.addAnnotations(handler,
					    handler.breakpoint(),
					    template,
					result);

	    /* LATER
	     Not needed if we work with 6.8

	    boolean isLoadModel =
		! DebuggerOption.RUN_AUTOSTART.
		      isEnabled(manager().globalOptions());

	    if (DebuggerManager.isStandalone() || isLoadModel) {
		// Not until gdb 6.8 do we get the -d option which would
		// allow us to create the handler in initially disabled
		// form.
		// Until then we need to send gdb an explicit disable.
		//
		// However, in the start model, because of the
		// scheduling of outgoing commands this won't get
		// sent until -exec-run is sent and will
		// arrive too late :-(
		// So we only try this in the load model.

		if (disable)
		    setHandlerEnabled(0, handler.getId(), false);
	    }
	    */
	    bm().noteNewHandler(rt, bp, handler);
        } catch (Exception x) {
            Exceptions.printStackTrace(x);
	    /* LATER
            // something went wrong, create a "broken" breakpoint
            if (created != null) {
                newBrokenHandler(created, null, x.getMessage(), false);
            } else {
                ErrorManager.getDefault().notify(x);
            }
	    */
        }
    }

    private void deleteForReplace(int rt, Handler targetHandler){
        // Don't use
        //	postDeleteHandler(hid);
        // because it will come around and call handlerDeleted which
        // we don't want.

        final int hid = targetHandler.getId();
        MICommand deleteCmd = new MIBreakCommand(rt, "-break-delete " + hid) { // NOI18N

            protected void onDone(MIRecord record) {
		// Don't use deleteHandlerById ... it ties back to
		// owning NB's ...
		// deleteHandlerById(b.getRoutingToken(), hid);
		Handler h = bm().findHandler(hid);

		// Don't cleanup either since it sets the bpts back pointer
		// to null.
		// OLD h.cleanup();

		// OLD handlers.remove(h);
		bm().simpleRemove(h);
                finish();
            }
        };
        gdb.sendCommand(deleteCmd);
    }


    private void replaceHandler(MIChangeBreakCommand cmd,
				int rt,
				MIResult result) {
	BreakpointPlan bp = bm().getBreakpointPlan(rt, BreakpointMsg.REPLACE);
        assert bp.op() == BreakpointOp.MODIFY :
                "replaceHandler(): bpt plan not CHANGE for rt " + rt; // NOI18N


        NativeBreakpoint targetBreakpoint = bp.target();

        assert targetBreakpoint.isSubBreakpoint();
        assert !targetBreakpoint.isEditable() :
                "targetBreakpoint is editable"; // NOI18N
        Handler targetHandler = targetBreakpoint.getHandler();
        assert targetHandler == bp.originalHandler();

        Handler replacementHandler =
            handlerExpert.replaceHandler(targetBreakpoint, targetHandler, result);

        handlerExpert.addAnnotations(replacementHandler, null, targetBreakpoint, result);

	deleteForReplace(rt, targetHandler);

	bm().noteReplacedHandler(bp, replacementHandler);
    }

    /**
     * Common behaviour for resumptive commands.
     * While these commands are mostly in the -exec family not all
     * -exec comamnds are resumptive (e.g. -exec-arguments)
     */
    private class MIResumptiveCommand extends MiCommandImpl {

        protected MIResumptiveCommand(String cmdString) {
            super(cmdString);
        }

        @Override
        protected void onRunning(MIRecord record) {
            // Actually we might get an error that will undo running
            // Perhaps we SHOULD note the receipt of running and commit to
            // it on a done?
            genericRunning();
        }

        @Override
        protected void onError(MIRecord record) {
            // gdb will send a "^running" even if step fails
            // cancel running state
            stateSetRunning(false);
            stateChanged();
	    session().setSessionState(state());

            genericFailure(record);
            finish();
        }

        @Override
        protected void onStopped(MIRecord record) {
            genericStopped(record);
            finish();
        }
    };

    /**
     * Common behaviour for -break command.
     */
    abstract class MIBreakCommand extends MiCommandImpl {
        private final boolean wasRunning;

        protected MIBreakCommand(int rt, String cmdString) {
            super(rt, cmdString);
            this.wasRunning = state().isRunning;
        }

        @Override
        protected abstract void onDone(MIRecord record);

        @Override
        protected void finish() {
            if (wasRunning) {
                go();
            }
            super.finish();
        }
    };

    public void runFailed() {
        setStatusText(Catalog.get("RunFailed")); // NOI18N
        stateSetRunning(false);
        stateChanged();
	session().setSessionState(state());
    }

    private boolean userInteraction(int rt, MIUserInteraction ui, boolean isBreakpoint) {
	boolean overloadCancelled = false;

	ItemSelectorResult result;

	int nitems = ui.items().length;
	String item[] = ui.items();

        if (isBreakpoint) {
	    // Special handling for breakpoint overloading
            assert rt == 0 || // spontaneous from cmdline
                RoutingToken.BREAKPOINTS.isSameSubsystem(rt);
	    String title = "Overloaded breakpoint"; // NOI18N
            result = this.bm().noteMultipleBreakpoints(rt, title, nitems, item);

        } else {

	    //
	    // convert to a form that popup() likes
	    //
	    String cookie = null; // OLD "eventspec";
	    String title = "Ambiguous symbol"; // NOI18N
	    boolean cancelable = ui.hasCancel();
	    boolean multiple_selection = true;

	    //
	    // post the popup
	    //
	    result = manager().popup(rt, cookie,
		GdbDebuggerImpl.this, title,
		nitems, item, cancelable, multiple_selection);
	}

	// 
	// convert popup results back to something to pass back to gdb
	//

	String returnValue;

	if (result.isCancelled()) {
	    overloadCancelled = true;

	    // "cancel" scenario
	    returnValue = "" + ui.cancelChoice();

	} else if (result.nSelected() == nitems) {
	    // "all" scenario;
	    returnValue = "" + ui.allChoice();

	} else {
	    returnValue = "";
	    for (int sx = 0; sx < result.nSelected(); sx++)
		returnValue += " " + (result.selections()[sx] + // NOI18N
				     ui.firstChoice());
	}

	gdb.tap().inject(returnValue + "\n"); // NOI18N

	// LATER return result.newRT;
	return overloadCancelled;
    }

    /**
     * Command to restore breakpoints.
     */
    private class MIRestoreBreakCommand extends MIBreakCommand {

	private boolean overloadCancelled = false;

        MIRestoreBreakCommand(int rt, String cmdString) {
            super(rt, cmdString);
        }

        protected void onDone(MIRecord record) {
	    if (record.isEmpty()) {
		// See comment for isEmpty
		onError(record);
	    } else {
		newHandlers(routingToken(), this, record);
	    }
	    finish();
        }

	// override MIBreakCommand
        @Override
	protected void onError(MIRecord record) {
	    if (overloadCancelled) {
		// don't do anything
		finish();
	    } else {
		super.onError(record);
	    }
	}

        @Override
	protected void onUserInteraction(MIUserInteraction ui) {
	    if (ui == null || ui.isEmpty())
		return;
	    overloadCancelled = userInteraction(this.routingToken(), ui, true);
	}
    };

    /**
     * Command to create line breakpoint.
     * Creates closure to retain original breakpoint data so we can recover
     * full pathname.
     */
    private class MIBreakLineCommand extends MIBreakCommand {

	// If we get an overload menu we might end up working with a new
	// non-0 rt:
	private int newRT = 0;

	// If we cancel an overload menu we'll get something like this:
	// 
	// which will come to us as an onError(). 'overloadCancelled' helps us 
	// bypass creating a broken bpt.
	private boolean overloadCancelled = false;
        
        MIBreakLineCommand(int rt, String cmdString) {
            super(rt, cmdString);
        }

        protected void onDone(MIRecord record) {
	    if (record.isEmpty()) {
		// See comment for isEmpty
		onError(record);
	    } else {
		newHandlers(newRT == 0? routingToken(): newRT, this, record);
		manager().bringDownDialog();
	    }
            finish();
        }

	// override MIBreakCommand
        @Override
	protected void onError(MIRecord record) {
	    if (overloadCancelled) {
		// don't do anything
		finish();
	    } else {
		super.onError(record);
	    }
	}

        @Override
	protected void onUserInteraction(MIUserInteraction ui) {
	    if (ui == null || ui.isEmpty())
		return;
	    overloadCancelled = userInteraction(this.routingToken(), ui, true);
	}
    };

    private abstract class MIChangeBreakCommand extends MIBreakCommand {

        MIChangeBreakCommand(int rt, String cmdString) {
            super(rt, cmdString);
        }
    }

    /**
     * Command to modify line breakpoint.
     */
    private class MIReplaceBreakLineCommand extends MIChangeBreakCommand {

        MIReplaceBreakLineCommand(int rt, String cmdString) {
            super(rt, cmdString);
        }

        protected void onDone(MIRecord record) {
	    if (record.isEmpty()) {
		// See comment for isEmpty
		onError(record);
	    } else {
		MITList results = record.results();
		MIValue bkptValue = results.valueOf("bkpt"); // NOI18N
		MIResult result = (MIResult) results.get(0);
		replaceHandler(this, routingToken(), result);
		manager().bringDownDialog();
	    }
            finish();
        }
    };

    /**
     * Command to repair broken breakpoint.
     */
    private class MIRepairBreakLineCommand extends MIChangeBreakCommand {

        MIRepairBreakLineCommand(int rt, String cmdString) {
            super(rt, cmdString);
        }

        protected void onDone(MIRecord record) {
	    if (record.isEmpty()) {
		// See comment for isEmpty
		onError(record);
	    } else {
		newHandlers(routingToken(), this, record);
		manager().bringDownDialog();
	    }
	    finish();
        }
    }

    public void postEnableAllHandlersImpl(final boolean enable) {
        final Handler[] handlers = bm().getHandlers();
        
        // no need to enable/disable if there is no handlers
        if (handlers.length == 0) {
            return;
        }
        
        StringBuilder command = new StringBuilder();
        if (enable) {
            command.append("-break-enable"); // NOI18N
        } else {
            command.append("-break-disable"); // NOI18N
        }
        
        for (Handler h : handlers) {
            command.append(' ');
            command.append(h.getId());
        }

        MICommand cmd = new MIBreakCommand(0, command.toString()) {

            protected void onDone(MIRecord record) {
		for (Handler h : handlers) {
                    h.setEnabled(enable);
                }
                finish();
            }
        };
        gdb.sendCommand(cmd);
    }

    public void postDeleteAllHandlersImpl() {
        final Handler[] handlers = bm().getHandlers();
        
        // no need to enable/disable if there is no handlers
        if (handlers.length == 0) {
            return;
        }

        // To test error recovery:
        // gdb sent back a &"warning: ..." and a ^done and seemed to have
        // processed everything following junk.
        //
        // However I'm still wary that under some circumstances we'll get
        // a partial result, where only some of the breakpoint would have
        // truly been deleted.
        //
        // hids += " " + "junk";

        StringBuilder command = new StringBuilder("-break-delete"); //NOI18N
        
	for (Handler h : handlers) {
            command.append(' ');
            command.append(h.getId());
        }

        MICommand cmd = new MIBreakCommand(0, command.toString()) {

            protected void onDone(MIRecord record) {
		for (Handler h : handlers) {
                    bm().deleteHandlerById(0, h.getId());
		}
                finish();
            }
        };
        gdb.sendCommand(cmd);
    }

    public void postDeleteHandlerImpl(final int rt, final int hid) {
        pause(true);
	MICommand cmd = new MIBreakCommand(rt, "-break-delete " + hid) { // NOI18N

	    protected void onDone(MIRecord record) {
		bm().deleteHandlerById(rt, hid);
		finish();
	    }
	};
	gdb.sendCommand(cmd);
    }

    public void postCreateHandlerImpl(int routingToken, HandlerCommand hc) {
	final MICommand cmd = new MIBreakLineCommand(routingToken, hc.toString());
        pause(true);
	gdb.sendCommand(cmd);
	// We'll continue in newHandler() or ?error?
    }

    public void postChangeHandlerImpl(int rt, HandlerCommand hc) {
        final MICommand cmd = new MIReplaceBreakLineCommand(rt, hc.toString());
        pause(true);
        gdb.sendCommand(cmd);
    }

    public void postRepairHandlerImpl(int rt, HandlerCommand hc) {
        final MICommand cmd = new MIRepairBreakLineCommand(rt, hc.toString());
        pause(true);
        gdb.sendCommand(cmd);
    }

    public void setHandlerCountLimit(int hid, long countLimit) {
        notImplemented("setHandlerCountLimit()");	// NOI18N
    }

    public void postEnableHandler(int rt, final int hid, final boolean enable) {
        String cmdString;
        if (enable) {
            cmdString = "-break-enable "; // NOI18N
        } else {
            cmdString = "-break-disable "; // NOI18N
        }

        MICommand cmd = new MIBreakCommand(rt, cmdString + hid) {

            protected void onDone(MIRecord record) {
                // SHOULD factor with code in Dbx.java
                Handler handler = bm().findHandler(hid);
                if (handler != null) {
                    handler.setEnabled(enable);
                }
                finish();
            }
        };
        gdb.sendCommand(cmd);
    }

    // interface NativeDebugger
    @Override
    public void postVarContinuation(VarContinuation vc) {
        notImplemented("postVarContinuation");	// NOI18N
    }

    protected void postVarContinuation(int rt, VarContinuation vc) {
        notImplemented("postVarContinuation");	// NOI18N
    }

    // interface GdbDebugger
    public void runArgs(String args) {
        sendSilent("-exec-arguments " + args); // NOI18N
    }

    public void runDir(String dir) {
	dir = localToRemote("runDir", dir); // NOI18N
        String cmdString = "cd " + dir; // NOI18N
        sendSilent(cmdString);
    }

    void setEnv(String envVar) {
        sendSilent("-gdb-set environment " + envVar); // NOI18N
    }
    
    private static String quoteValue(String value) {
        int length = value.length();
	if (length > 1 
                && (value.charAt(0) == '"') &&
                (value.charAt(length-1) == '"')) {
            return value.replace("\"", "\\\""); //NOI18N
	}
        return value;
    }
    
    void assignVar(final GdbVariable var, final String value, final boolean miVar) {
        String cmdString;
        if (miVar) {
            cmdString = "-var-assign " + var.getMIName() + " " + value; // NOI18N
        } else {
            cmdString = "-data-evaluate-expression \"" +  //NOI18N
                    var.getFullName() + '=' + quoteValue(value) + '"'; // NOI18N
        }
        MICommand cmd =
            new MiCommandImpl(cmdString) {

            @Override
                    protected void onDone(MIRecord record) {
                        updateMIVar();
                        finish();
                    }

            @Override
                    protected void onError(MIRecord record) {
                        String errMsg = getErrMsg(record);

                        // to work around gdb "corrupt stack" problem
                        if (try_one_more && errMsg.equals(corrupt_stack)) {
                            try_one_more = true;
                        //updateMIVar();
                        }
                        // to work around gdb "out of scope" problem
                        String out_of_scope = "mi_cmd_var_assign: Could not assign expression to varible object"; // NOI18N
                        if (!errMsg.equals(out_of_scope)) {
                            genericFailure(record);
                            finish();
                        }
                    }
            };
        gdb.sendCommand(cmd);
    }

    /**
     * Called when this session has been switched to
     * 'redundant' is true when we double-click on the same session.
     */
    @Override
    public void activate(boolean redundant) {

	if (isConnected()) {

	    super.activate(redundant);

            if (memoryWindow != null) {
                memoryWindow.setDebugger(this);
            }

	} else {
	    // See big comment in dbx side
	    updateActions();
	}

        if (redundant) {
            return;
        }
    }

    /**
     * Called when this session has been switched away from
     * 'redundant' is true when we double-click on the same session.
     */
    @Override
    public void deactivate(boolean redundant) {
        super.deactivate(redundant);
        if (redundant) {
            return;
        }
    }

    // interface NativeDebugger
    public void setCurrentDisLine(Line l) {
        notImplemented("setCurrentDisLine");	// NOI18N
    }

    // interface NativeDebugger
    public Line getCurrentDisLine() {
        return null;
    }

    public void notifyUnsavedFiles(String file[]) {
    }

    // interface NativeDebugger
    public void stepOutInst() {
        execFinish();
    }

    // interface NativeDebugger
    public void stepOverInst() {
        sendResumptive("-exec-next-instruction"); // NOI18N
    }

    // interface NativeDebugger
    public void stepInst() {
        sendResumptive("-exec-step-instruction"); // NOI18N
    }

    // interface NativeDebugger
    public void runToCursorInst(String addr) {
        notImplemented("runToCursorInst");	// NOI18N
    }


    // interface NativeDebugger
    public void postRestoring(boolean restoring) {
    }

    // interface NativeDebugger
    public void forkThisWay(DebuggerManager.FollowForkInfo ffi) {
        notImplemented("forkThisWay");	// NOI18N
    }

    // interface NativeDebugger
    public void fix() {
        notImplemented("fix");	// NOI18N
    }

    // interface NativeDebugger
    public void exprEval(String format, String expr) {
        notImplemented("exprEval");	// NOI18N
    }

    // interface NativeDebugger
    public void execute(String cmd) {
        notImplemented("execute");	// NOI18N
    }


    // implement NativeDebuggerImpl
    protected void stopUpdates() {
	// no-op for now
    }

    // implement NativeDebuggerImpl
    protected void startUpdates() {
	// no-op for now
    }
    
    private void send(String commandStr, boolean reportError) {
        MiCommandImpl cmd = new MiCommandImpl(commandStr);
        if (!reportError) {
            cmd.dontReportError();
        }
        gdb.sendCommand(cmd);
    }
    
    private void sendSilent(String commandStr) {
        send(commandStr, false);
    }
    
    private void send(String commandStr) {
        send(commandStr, true);
    }
    
    private void sendResumptive(String commandStr) {
        MICommand cmd = new MIResumptiveCommand(commandStr);
        gdb.sendCommand(cmd);
    }

    private boolean get_registers = false;
    
    @Override
    public void registerRegistersWindow(RegistersWindow w) {
        super.registerRegistersWindow(w);
        if (get_registers == false && w != null) {
            requestRegisters();
        }
        get_registers = (w != null);
    }
}
