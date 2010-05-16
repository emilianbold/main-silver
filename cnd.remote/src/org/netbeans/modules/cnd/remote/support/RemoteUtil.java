/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.remote.support;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsCacheManager;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;

/**
 * Misc. utiliy finctions
 * @author Vladimir Kvashin
 */
public class RemoteUtil {

    private static final Map<ExecutionEnvironment, String> homeDirs = new LinkedHashMap<ExecutionEnvironment, String>();
    public static final Logger LOGGER = Logger.getLogger("cnd.remote.logger"); //NOI18N

    public static class PrefixedLogger {

        private final String prefix;

        public PrefixedLogger(String prefix) {
            this.prefix = prefix;
        }

        public void log(Level level, String format, Object... args) {
            if (LOGGER.isLoggable(level)) {
                String text = String.format(format, args);
                text = prefix + ": " + text; // NOI18N
                LOGGER.log(level, text);
            }
        }
    }

    private RemoteUtil() {}

//    public static void log(String prefix, Level level, String format, Object... args) {
//        if (LOGGER.isLoggable(level)) {
//            String text = String.format(format, args);
//            LOGGER.log(level, String.format("%s: ", text));
//        }
//    }

    /** 
     * Returns home directory for the given host
     * NB: this is a LONG RUNNING method - never call from UI thread
     */
    public static String getHomeDirectory(ExecutionEnvironment execEnv) {
        CndUtils.assertNonUiThread();
        String dir = null;
        // it isn't worth doing smart synchronization here
        synchronized(homeDirs) {
            // we cache nulls as well
            if (homeDirs.containsKey(execEnv)) {
                return homeDirs.get(execEnv);
            }
        }
        try { // FIXUP: remove this try/catch as soon as in NPE in execution is fixed
            if (Boolean.getBoolean("cnd.emulate.null.home.dir")) { // to emulate returning null //NOI18N
                return null;
            }
            // NB: it's important that /bin/pwd is called since it always reports resolved path
            // while shell's pwd result depend on shell
            ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", "cd; /bin/pwd"); // NOI18N
            if (res.isOK()) {
                String s = res.output;
                if (HostInfoProvider.fileExists(execEnv, s)) {
                    dir = s;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        synchronized(homeDirs) {
            // we cache nulls as well
            homeDirs.put(execEnv, dir);            
        }
        return dir;
    }

    /**
     * FIXUP: * Need this hack for Cloud stuff:
     * we have to distinguish "normal", i.e. CND environments
     * from "foreign", i.e. "cloud" ones.
     */
    public static boolean isForeign(ExecutionEnvironment execEnv ) {
        if (execEnv == null) {
            return false;
        }
        String id = ExecutionEnvironmentFactory.toUniqueID(execEnv);
        String protocolSeparator = "://"; //NOI18N
        if (id.indexOf(protocolSeparator) < 0) {
            // old-style environment - no protocol
            return false; 
        } else {
            // there is a protocol and it equals to ssh
            return ! id.startsWith("ssh" + protocolSeparator); //NOI18N
        }
    }
    
    public static String getDisplayName(ExecutionEnvironment execEnv) {
        ServerRecord rec = ServerList.get(execEnv);
        if (rec == null) {
            return execEnv.getDisplayName();
        } else {
            return rec.getDisplayName();
        }
    }

    public static void checkSetupAfterConnection(ExecutionEnvironment env) {
        RemoteServerRecord record = (RemoteServerRecord) ServerList.get(env);
        if (!record.isOnline()) {
            record.resetOfflineState();
            record.init(null);
            if (record.isOnline()) {
                ToolsCacheManager cacheManager = ToolsCacheManager.createInstance(true);
                CompilerSetManager csm = cacheManager.getCompilerSetManagerCopy(record.getExecutionEnvironment(), false);
                csm.initialize(false, true, null);
                cacheManager.applyChanges();
            }
        }
    }
}
