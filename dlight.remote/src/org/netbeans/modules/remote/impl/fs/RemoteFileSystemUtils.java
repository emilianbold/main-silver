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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.EnvUtils;
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.util.Utilities;

/**
 *
 * @author Vladimir Kvashin
 */
public class RemoteFileSystemUtils {

    private static final boolean TRUE_CASE_SENSITIVE_SYSTEM;

    private static boolean isWindows = Utilities.isWindows();

    private static final char[][] windowsReservedChars = new char[][] {
        { '<',  'L' },
        { '>',  'M' },
        { ':',  'C' },
        { '"',  'D' },
        { '/',  'F' },
        { '\\', 'B' },
        { '|',  'P' },
        { '?',  'Q' },
        { '*',  'A' }
    };

    private static final char windowsReservedNameChar = 'R'; // for CON, AUX, etc

    static {
        boolean caseSenstive;
        try {
            File tmpFile = File.createTempFile("CaseSensitiveFile", ".check"); // NOI18N
            String absPath = tmpFile.getAbsolutePath();
            absPath = absPath.toUpperCase();
            caseSenstive = !new File(absPath).exists();
            tmpFile.delete();
        } catch (IOException ex) {
            caseSenstive = Utilities.isUnix() && !Utilities.isMac();
        }
        TRUE_CASE_SENSITIVE_SYSTEM = caseSenstive;
    }

    public static boolean isSystemCaseSensitive() {
        return TRUE_CASE_SENSITIVE_SYSTEM;
    }

    private RemoteFileSystemUtils() {
    }
    
    public static ExecutionEnvironment getExecutionEnvironment(String hostName, int port) {
        ExecutionEnvironment result = null;
        for(ExecutionEnvironment env : ConnectionManager.getInstance().getRecentConnections()) {
            if (hostName.equals(EnvUtils.toHostID(env))) {
                if (port == 0 || port == env.getSSHPort()) {
                    result = env;
                    if (ConnectionManager.getInstance().isConnectedTo(env)) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    public static String escapeFileName(String name) {
        if (!isWindows) {
            return name;
        }
        StringBuilder sb = new StringBuilder();

        // Escape reserved names -
        // CON, PRN, AUX, NUL, COM1-COM9, LPT1-LPT9 with or without extensions

        if (name.startsWith("CON") || name.startsWith("PRN") || name.startsWith("AUX") || name.startsWith("NUL")) { // NOI18N
            if (name.length() == 3 || name.charAt(3) == '.') {
                sb.append('_').append(windowsReservedNameChar).append(name);
                return sb.toString();
            }
        }
        if (name.startsWith("COM") || name.startsWith("LPT")) { // NOI18N
            if (name.length() > 3) {
                char c = name.charAt(3);
                if ('1' <= c && c <= '9') {
                    if (name.length() == 4 || name.charAt(4) == '.') {
                        sb.append('_').append(windowsReservedNameChar).append(name);
                        return sb.toString();
                    }
                }

            }
        }

        // First, check whether we need to escape
        if (!containsReservedCharacters(name) && name.indexOf('_') < 0) {
            return name;
        }

        // Escape reserved characters
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_') {
                sb.append("__"); //NOI18N
            } else {
                boolean added = false;
                for (int j = 0; j < windowsReservedChars.length; j++) {
                    if (c == windowsReservedChars[j][0]) {
                        sb.append('_').append(windowsReservedChars[j][1]);
                        added = true;
                    }
                }
                if (!added) {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    private char unescapeChar(char c) {
        for (int j = 0; j < windowsReservedChars.length; j++) {
            if (c == windowsReservedChars[j][1]) {
                return windowsReservedChars[j][0];
            }
        }
        return 0;
    }

    public static String unescapeFileName(String name) {
        if (!isWindows) {
            return name;
        }
        if (name.length() < 2 || name.indexOf('_') < 0) {
            return name;
        }
        if (name.charAt(0) == '_' && name.charAt(1) == windowsReservedNameChar) {
            return name.substring(2);
        }
        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (escape) {
                escape = false;
                if (c == '_') {
                    sb.append('_');
                } else {
                    boolean added = false;
                    for (int j = 0; j < windowsReservedChars.length; j++) {
                        if (c == windowsReservedChars[j][1]) {
                            sb.append(windowsReservedChars[j][0]);
                            added = true;
                            break;
                        }
                    }
                    if (!added) {
                        RemoteLogger.getInstance().log(Level.SEVERE, "Incorrect name to unescape: ''{0}''", name);
                    }
                }
            } else {
                if (c == '_') {
                    escape = true;
                    if ((i+1) == name.length()) { // shouldn't be last one
                        RemoteLogger.getInstance().log(Level.SEVERE, "Incorrect name to unescape: ''{0}''", name);
                    }
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    private static boolean containsReservedCharacters(String name) {
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            for (int j = 0; j < windowsReservedChars.length; j++) {
                if (c == windowsReservedChars[j][0]) {
                    return true;
                }
            }
        }
        return false;
    }


    /*pakage*/ static void testSetWindows(boolean isWin) {
        isWindows = isWin;
    }
}
