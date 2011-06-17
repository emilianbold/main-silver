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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public final class PasswordManager {

    private static final boolean keepPasswordsInMemory;
    private static final String KEY_PREFIX = "remote.user.info.password."; // NOI18N
    private static final String STORE_PREFIX = "remote.user.info.store."; // NOI18N
    private final Map<String, String> cache = Collections.synchronizedMap(new HashMap<String, String>());
    private boolean keyringIsActivated = false;
    private static PasswordManager instance = new PasswordManager();
    
    static {
        keepPasswordsInMemory = !Boolean.getBoolean("remote.user.password.do_not_keep_in_memory"); // NOI18N
    }

    private PasswordManager() {
    }

    public static PasswordManager getInstance() {
        return instance;
    }

    /**
     *
     * @param execEnv
     * @return password from memory or from Keyring if user selected "remember password" in previous IDE invocation
     */
    public char[] getPassword(ExecutionEnvironment execEnv) {
        String key = ExecutionEnvironmentFactory.toUniqueID(execEnv);
        if (keepPasswordsInMemory) {
            String cachedPassword = cache.get(key);
            if (cachedPassword != null) {
                Logger.getInstance().log(Level.FINEST, "PasswordManager.get({0}) found password in memory", execEnv); // NOI18N
                return cachedPassword.toCharArray();
            }
        }
        boolean stored = NbPreferences.forModule(PasswordManager.class).getBoolean(STORE_PREFIX + key, false);
        if (stored) {
            keyringIsActivated = true;
            char[] keyringPassword = Keyring.read(KEY_PREFIX + key);
            if (keepPasswordsInMemory && keyringPassword != null) {
                cache.put(key, String.valueOf(keyringPassword));
            }
            Logger.getInstance().log(Level.FINEST, "PasswordManager.get({0}) found password in keyring", execEnv); // NOI18N
            return keyringPassword;
        }

        Logger.getInstance().log(Level.FINEST, "PasswordManager.get({0}) failed to find password", execEnv); // NOI18N
        return null;
    }

    /**
     * Store password in memory. If user select "remember password" option password is stored in Keyring.
     *
     * @param execEnv
     * @param password
     * @param rememberPassword
     */
    public void storePassword(ExecutionEnvironment execEnv, char[] password, boolean rememberPassword) {
        setRememberPassword(execEnv, rememberPassword);
        put(execEnv, password);
    }

    /**
     * Update password in memory. If user selected "remember password" option before password is updated in Keyring.
     *
     * @param execEnv
     * @param password
     */
    private void put(ExecutionEnvironment execEnv, char[] password) {
        String key = ExecutionEnvironmentFactory.toUniqueID(execEnv);
        if (keepPasswordsInMemory) {
            if (password != null) {
                cache.put(key, String.valueOf(password));
                Logger.getInstance().log(Level.FINEST, "PasswordManager.put({0}, non-null) stored password in memory", execEnv); // NOI18N
            } else {
                Logger.getInstance().log(Level.FINEST, "PasswordManager.put({0}, null) cleared password from memory", execEnv); // NOI18N
                cache.put(key, null);
            }
        }
        boolean store = NbPreferences.forModule(PasswordManager.class).getBoolean(STORE_PREFIX + key, false);
        if (store) {
            keyringIsActivated = true;
            Keyring.save(KEY_PREFIX + key, password,
            NbBundle.getMessage(PasswordManager.class, "PasswordManagerPasswordFor", execEnv.getDisplayName())); // NOI18N
            Logger.getInstance().log(Level.FINEST, "PasswordManager.put({0}, non-null) stored password in keyring", execEnv); // NOI18N
        }
    }

    /** Should be called on disconnect. */
    /*package*/ void onExplicitDisconnect(ExecutionEnvironment execEnv) {
        String key = ExecutionEnvironmentFactory.toUniqueID(execEnv);
        cache.remove(key);
    }

    /**
     * Remove password from memory and Keyring
     *
     * @param execEnv
     */
    public void clearPassword(ExecutionEnvironment execEnv) {
        String key = ExecutionEnvironmentFactory.toUniqueID(execEnv);
        if (keepPasswordsInMemory) {
            cache.remove(key);
        }
        NbPreferences.forModule(PasswordManager.class).remove(STORE_PREFIX + key);
        if (keyringIsActivated) {
            Keyring.delete(KEY_PREFIX + key);
        }
        Logger.getInstance().log(Level.FINEST, "PasswordManager.clearPassword({0})", execEnv); // NOI18N
    }

    /**
     * Remove password from memory and Keyring
     *
     * @param execEnv
     */
    public void forceClearPassword(ExecutionEnvironment execEnv) {
        String key = ExecutionEnvironmentFactory.toUniqueID(execEnv);
        if (keepPasswordsInMemory) {
            cache.remove(key);
        }
        NbPreferences.forModule(PasswordManager.class).remove(STORE_PREFIX + key);
        Keyring.delete(KEY_PREFIX + key);
        Logger.getInstance().log(Level.FINEST, "PasswordManager.forceClearPassword({0})", execEnv); // NOI18N
    }

    /**
     * Remove passwords for hosts that are not in list.
     *
     * @param envs
     */
    public void setServerList(List<ExecutionEnvironment> envs) {
        Set<String> keys = new HashSet<String>();
        for (ExecutionEnvironment env : envs) {
            String key = ExecutionEnvironmentFactory.toUniqueID(env);
            keys.add(KEY_PREFIX + key);
            keys.add(STORE_PREFIX + key);
        }
        try {
            String[] allKeys = NbPreferences.forModule(PasswordManager.class).keys();
            for (String aKey : allKeys) {
                if (!keys.contains(aKey)) {
                    if (aKey.startsWith(STORE_PREFIX)) {
                        if (keyringIsActivated) {
                            Keyring.delete(KEY_PREFIX + aKey.substring(STORE_PREFIX.length()));
                        }
                        if (keepPasswordsInMemory) {
                            cache.remove(aKey.substring(STORE_PREFIX.length()));
                        }
                    }
                }
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        } catch (Throwable ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * User intention of "remember password"
     * @param execEnv
     * @return true if user checked "remember password" option.
     */
    public boolean isRememberPassword(ExecutionEnvironment execEnv) {
        String key = ExecutionEnvironmentFactory.toUniqueID(execEnv);
        boolean stored = NbPreferences.forModule(PasswordManager.class).getBoolean(STORE_PREFIX + key, false);
        return stored;
    }

    /**
     * Store user intention of "remember password"
     * @param execEnv
     * @param rememberPassword
     */
    public void setRememberPassword(ExecutionEnvironment execEnv, boolean rememberPassword) {
        String key = ExecutionEnvironmentFactory.toUniqueID(execEnv);
        if (!rememberPassword) {
            if (keyringIsActivated) {
                Keyring.delete(KEY_PREFIX + key);
            }
        }
        NbPreferences.forModule(PasswordManager.class).putBoolean(STORE_PREFIX + key, rememberPassword);
    }
}
