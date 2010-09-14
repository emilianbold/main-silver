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
package org.netbeans.modules.nativeexecution.api.util;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.netbeans.modules.nativeexecution.support.InstalledFileLocatorProvider;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/*
 * Used to unbuffer application's output in case OutputWindow is used.
 * 
 */
public class UnbufferSupport {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private static final boolean UNBUFFER_DISABLED = Boolean.getBoolean("execution.no_unbuffer"); // NOI18N
    private static final HashMap<ExecutionEnvironment, String> cache =
            new HashMap<ExecutionEnvironment, String>();

    private UnbufferSupport() {
    }

    public static void initUnbuffer(final ExecutionEnvironment execEnv, final MacroMap env) throws IOException {
        if (UNBUFFER_DISABLED) {
            return;
        }

        final HostInfo hinfo = HostInfoUtils.getHostInfo(execEnv);

        boolean isMacOS = hinfo.getOSFamily() == HostInfo.OSFamily.MACOSX;

        // Bug 179172 - unbuffer.dylib not found
        // Will disable unbuffer on Mac as seems it is not required...
        if (isMacOS) {
            return;
        }

        boolean isWindows = hinfo.getOSFamily() == HostInfo.OSFamily.WINDOWS;

        final MacroExpander macroExpander = MacroExpanderFactory.getExpander(execEnv);
        // Setup LD_PRELOAD to load unbuffer library...

        String unbufferPath = null; // NOI18N
        String unbufferLib = null; // NOI18N

        try {
            unbufferPath = macroExpander.expandPredefinedMacros(
                    "bin/nativeexecution/$osname-$platform"); // NOI18N
            unbufferLib = macroExpander.expandPredefinedMacros(
                    "unbuffer.$soext"); // NOI18N
        } catch (ParseException ex) {
        }

        if (unbufferLib != null && unbufferPath != null) {
            InstalledFileLocator fl = InstalledFileLocatorProvider.getDefault();
            File file = fl.locate(unbufferPath + "/" + unbufferLib, "org.netbeans.modules.dlight.nativeexecution", false); // NOI18N

            log.fine("Look for unbuffer library here: " + unbufferPath + "/" + unbufferLib); // NOI18N

            if (file != null && file.exists()) {
                if (execEnv.isRemote()) {
                    String remotePath = null;

                    synchronized (cache) {
                        remotePath = cache.get(execEnv);

                        if (remotePath == null) {
                            remotePath = hinfo.getTempDir() + "/" + unbufferPath; // NOI18N
                            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
                            npb.setExecutable("/bin/mkdir").setArguments("-p", remotePath, remotePath + "_64"); // NOI18N

                            try {
                                npb.call().waitFor();
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }

                            try {
                                String remoteLib_32 = remotePath + "/" + unbufferLib; // NOI18N
                                String remoteLib_64 = remotePath + "_64/" + unbufferLib; // NOI18N

                                String fullLocalPath = file.getParentFile().getAbsolutePath(); // NOI18N
                                Future<Integer> copyTask;
                                copyTask = CommonTasksSupport.uploadFile(fullLocalPath + "/" + unbufferLib, execEnv, remoteLib_32, 0755, null, true); // NOI18N
                                copyTask.get();
                                copyTask = CommonTasksSupport.uploadFile(fullLocalPath + "_64/" + unbufferLib, execEnv, remoteLib_64, 0755, null, true); // NOI18N
                                copyTask.get();
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (ExecutionException ex) {
                                Exceptions.printStackTrace(ex);
                            }

                            cache.put(execEnv, remotePath);
                        }
                    }

                    unbufferPath = remotePath;
                } else {
                    unbufferPath = new File(file.getParent()).getAbsolutePath();
                }

                String ldPreloadEnv;
                String ldLibraryPathEnv;

                if (isWindows) {
                    ldLibraryPathEnv = "PATH"; // NOI18N
                    ldPreloadEnv = "LD_PRELOAD"; // NOI18N
                } else if (isMacOS) {
                    ldLibraryPathEnv = "DYLD_LIBRARY_PATH"; // NOI18N
                    ldPreloadEnv = "DYLD_INSERT_LIBRARIES"; // NOI18N
                } else {
                    ldLibraryPathEnv = "LD_LIBRARY_PATH"; // NOI18N
                    ldPreloadEnv = "LD_PRELOAD"; // NOI18N
                }

                String ldPreload = env.get(ldPreloadEnv);

                if (isWindows) {
                    // TODO: FIXME (?) For Mac and Windows just put unbuffer
                    // with path to it to LD_PRELOAD/DYLD_INSERT_LIBRARIES
                    // Reason: no luck to make it work using PATH ;(
                    ldPreload = ((ldPreload == null) ? "" : (ldPreload + ";")) + // NOI18N
                            new File(unbufferPath, unbufferLib).getAbsolutePath(); // NOI18N

                    ldPreload = WindowsSupport.getInstance().convertToAllShellPaths(ldPreload);

                    if (ldPreload == null) {
                        // i.e. cannot convert [cygpath not found, for example]
                        // will not set LD_PRELOAD
                        return;
                    }

                } else if (isMacOS) {
                    // TODO: FIXME (?) For Mac and Windows just put unbuffer
                    // with path to it to LD_PRELOAD/DYLD_INSERT_LIBRARIES
                    // Reason: no luck to make it work using PATH ;(
                    ldPreload = ((ldPreload == null) ? "" : (ldPreload + ":")) + // NOI18N
                            unbufferPath + "/" + unbufferLib; // NOI18N
                } else {
                    ldPreload = ((ldPreload == null) ? "" : (ldPreload + ":")) + // NOI18N
                            unbufferLib;
                }

                env.put(ldPreloadEnv, ldPreload);

                if (isMacOS) {
                    env.put("DYLD_FORCE_FLAT_NAMESPACE", "yes"); // NOI18N
                } else if (isWindows) {
//                    String ldLibPath = env.get(ldLibraryPathEnv);
//                    ldLibPath = ((ldLibPath == null) ? "" : (ldLibPath + ";")) + // NOI18N
//                            unbufferPath + ";" + unbufferPath + "_64"; // NOI18N
//                    ldLibPath = CommandLineHelper.getInstance(execEnv).toShellPaths(ldLibPath);
//                    env.put(ldLibraryPathEnv, ldLibPath); // NOI18N
                } else {
                    String ldLibPath = env.get(ldLibraryPathEnv);
                    ldLibPath = ((ldLibPath == null) ? "" : (ldLibPath + ":")) + // NOI18N
                            unbufferPath + ":" + unbufferPath + "_64"; // NOI18N
                    env.put(ldLibraryPathEnv, ldLibPath); // NOI18N
                }
            }
        }
    }
}
