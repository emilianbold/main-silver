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

package org.netbeans.modules.cnd.debugger.common2.debugger.io;

import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.utils.Executor;
import org.netbeans.modules.cnd.debugger.common2.utils.FileMapper;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

public abstract class IOPack {
    private final TermComponent console;
    protected String slaveName = null;
    protected final ExecutionEnvironment exEnv;

    protected IOPack(TermComponent console, ExecutionEnvironment exEnv) {
        this.console = console;
        this.exEnv = exEnv;
    }

    public TermComponent console() {
	return console;
    }

    public abstract boolean start();

    public void open() {
	console.open();
    }

    public void bringDown() {
	console.bringDown();
    }

    public void bringUp() {
	console.bringUp();
    }

    public void switchTo() {
	console.switchTo();
    }

    public static TermComponent makeConsole(int flags) {
	return TermComponentFactory.createNewTermComponent(ConsoleTopComponent.getDefault(), flags);
    }

    public String getExtraRunArgs(FileMapper fMapper) {
        return "";
    }

    public String getSlaveName() {
        return slaveName;
    }

    public abstract void close();

    public static IOPack create(boolean remote,
                                InputOutput io,
                                RunProfile runProfile,
                                Executor executor) {
        int consoleType = runProfile.getConsoleType().getValue();
        if (consoleType == RunProfile.CONSOLE_TYPE_DEFAULT) {
            consoleType = RunProfile.getDefaultConsoleType();
        }

        TermComponent console;
        if (remote || Utilities.isWindows()) {
            console = IOPack.makeConsole(0);
        } else {
            console = IOPack.makeConsole(TermComponentFactory.PTY | TermComponentFactory.RAW_PTY);
        }

        IOPack res;

        boolean createPio = DebuggerManager.isStandalone();
        if (createPio) {
            TermComponent pio;
            if (remote || Utilities.isWindows()) {
                pio = PioPack.makePio(0);
            } else {
                pio = PioPack.makePio(TermComponentFactory.PTY | TermComponentFactory.PACKET_MODE);
            }
            res = new PioPack(console, pio, executor.getExecutionEnvironment());
        } else if (consoleType == RunProfile.CONSOLE_TYPE_EXTERNAL) {
            res = new ExternalTerminalPack(console, runProfile.getTerminalPath(), executor.getExecutionEnvironment());
        } else if (consoleType == RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW) {
            res = new OutputPack(console, io, executor.getExecutionEnvironment());
        } else {
            res = new InternalTerminalPack(console, io, executor.getExecutionEnvironment());
        }

	res.bringUp();
	// OLD bug #181165 let "debugger" group open it
	// OLD open();

	// PioWindow multiplexes consoles so need to explicitly
	// bring the new ones to front.
	res.switchTo();

        return res;
    }
}
