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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.remote.mapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.remote.support.RemoteCommandSupport;
import org.netbeans.modules.cnd.remote.ui.EditPathMapDialog;
import org.netbeans.modules.cnd.spi.remote.setup.MirrorPathProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 * An implementation of PathMap which returns remote path information.
 * 
 * @author gordonp
 */
public abstract class RemotePathMap extends PathMap {

    private final static Map<ExecutionEnvironment, Map<String, RemotePathMap>> pmtable =
            new HashMap<ExecutionEnvironment, Map<String, RemotePathMap>>();

    public static RemotePathMap getPathMap(ExecutionEnvironment env) {
        String syncID = getEnvSyncID(env);
        Map<String, RemotePathMap> pathmaps = pmtable.get(env);
        RemotePathMap pathmap = null;
        if (pathmaps == null) {
            synchronized (pmtable) {
                pathmaps = new HashMap<String, RemotePathMap>();
                pmtable.put(env, pathmaps);
            }
        }
        pathmap = pathmaps.get(syncID);
        if (pathmap == null) {
            boolean customizable = ServerList.get(env).getSyncFactory().isPathMappingCustomizable();
            synchronized (pmtable) {
                pathmap = customizable ? new CustomizableRemotePathMap(env) : new FixedRemotePathMap(env);
                pathmap.init();
                pathmaps.put(syncID, pathmap);
            }
        }
        return pathmap;
    }

    public static boolean isReady(ExecutionEnvironment execEnv) {
        return pmtable.get(execEnv) != null;
    }

    //

    protected final HashMap<String, String> map = new HashMap<String, String>();
    protected final ExecutionEnvironment execEnv;
    protected volatile String localBase;

    protected RemotePathMap(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
    }

    protected final boolean loadFromPrefs() {
        synchronized (map) {
            String list = getPreferences(execEnv);

            if (list == null) {
                // 1. Developers entry point
                String pmap = System.getProperty("cnd.remote.pmap");
                if (pmap != null) {
                    String line;
                    File file = new File(pmap);

                    if (file.exists() && file.canRead()) {
                        try {
                            BufferedReader in = new BufferedReader(new FileReader(file));
                            try {
                                while ((line = in.readLine()) != null) {
                                    int pos = line.indexOf(' ');
                                    if (pos > 0) {
                                        map.put(line.substring(0, pos), line.substring(pos + 1).trim());
                                    }
                                }
                            } finally {
                                in.close();
                            }
                        } catch (IOException ioe) {
                        }
                    }
                } else {
                    return false;
                }
            } else {
                // 3. Deserialization
                String[] paths = list.split(DELIMITER);
                for (int i = 0; i < paths.length; i += 2) {
                    if (i + 1 < paths.length) { //TODO: only during development
                        map.put(paths[i], paths[i + 1]);
                    } else {
                        System.err.println("mapping serialization flaw. Was found: " + list);
                    }
                }
            }
            return true;
        }
    }
    /** 
     *
     * Initialization the path map here:
     */
    public abstract void init();
    
    // PathMap
    public String getRemotePath(String lpath, boolean useDefault) {
        CndUtils.assertNotNull(lpath, "local path should not be null"); // nOI18N
        if (lpath == null) {
            return null;
        }
        String ulpath = unifySeparators(lpath);
        String rpath = null;
        int max = 0;
        // search for the *longest* key that starts with lpath
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = unifySeparators(entry.getKey());
            if (ulpath.startsWith(key)) {
                if (rpath == null || key.length() > max) {
                    max = key.length();
                    String mpoint = entry.getValue();
                    rpath = mpoint + lpath.substring(key.length()).replace('\\', '/');
                }
            }
        }
        if (rpath != null) {
            return rpath;
        } else {
            return useDefault ? lpath : null;
        }
    }

    public String getLocalPath(String rpath, boolean useDefault) {
        CndUtils.assertNotNull(rpath, "remote path should not be null"); // nOI18N
        if (rpath == null) {
            return null;
        }
        String urpath = unifySeparators(rpath);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String value = unifySeparators(entry.getValue());
            if (urpath.startsWith(value)) {
                String mpoint = entry.getKey();
                return mpoint + rpath.substring(value.length());
            }
        }        
        if (useDefault) {
            initLocalBase();
            return localBase  + '/' + rpath;
        }
        return null;
    }

    /**
     * See if a path is local or remote. The main use of this call is to verify a project's
     * Development Host setting. If the project's sources are local then you should not be
     * able to set a remote development host.
     * 
     * @param lpath The local path to check
     * @return true if path is remote, false otherwise
     */
    public boolean checkRemotePath(String lpath, boolean fixMissingPaths) {
        CndUtils.assertNotNull(lpath, "local path should not be null"); // nOI18N
        if (lpath == null) {
            return false;
        }
        String ulpath = unifySeparators(lpath);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String mpoint = unifySeparators(entry.getKey());
            if (ulpath.startsWith(mpoint)) {
                return true;
            }
        }

        for (String mpoint : map.keySet()) {
            if (ulpath.startsWith(unifySeparators(mpoint))) {
                return true;
            }
        }

        try {
            // check if local path is mirrored by remote path
            if (validateMapping(execEnv, lpath, new File(lpath))) {
                synchronized (map) {
                    map.put(lpath, lpath);
                }
                return true;
            }
        } catch (InterruptedException ex) {
            return false;
        }

        if (fixMissingPaths) {
            return EditPathMapDialog.showMe(execEnv, lpath) && checkRemotePath(lpath, false);
        } else {
            return false;
        }

    }

    public void addMapping(String localParent, String remoteParent) {
        addMappingImpl(localParent, remoteParent);
    }

    protected void addMappingImpl(String localParent, String remoteParent) {
        CndUtils.assertNotNull(localParent, "local path shouldn't be null"); //NOI18N
        CndUtils.assertNotNull(remoteParent, "remote path shouldn't be null"); //NOI18N
        if (localParent == null || remoteParent == null) {
            return;
        }
        synchronized( map ) {
            Map<String, String> clone = new LinkedHashMap<String, String>(map);
            clone.put(localParent,remoteParent);
            updatePathMapImpl(clone);
        }
    }


    // Utility
    public void updatePathMap(Map<String, String> newPathMap) {
        updatePathMapImpl(newPathMap);
    }

    protected void updatePathMapImpl(Map<String, String> newPathMap) {
        synchronized( map ) {
            map.clear();
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : newPathMap.entrySet()) {
                String remotePath = fixEnding(entry.getValue());
                String path = fixEnding(entry.getKey());
                map.put(path, remotePath);
                sb.append( fixEnding(path) );
                sb.append(DELIMITER);
                sb.append( remotePath );
                sb.append(DELIMITER);
            }
            setPreferences(sb.toString());
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getMap() {
        return (Map<String, String>)map.clone();
    }

    private static String fixEnding(String path) {
        //TODO: system dependent separator?
        if (path.charAt(path.length()-1)!='/' && path.charAt(path.length()-1)!='\\') {
            return path + "/"; //NOI18N
        } else {
            return path;
        }
    }
    // inside path mapper we use only / and lowercase 
    // TODO: lowercase should be only windows issue -- possible flaw
    private static String unifySeparators(String path) {
        String result = path.replace('\\', '/');
        if (!CndFileUtils.isSystemCaseSensitive()) {
            result = result.toLowerCase();
        }
        return result;
    }

    public static boolean isSubPath(String path, String pathToValidate) {
        CndUtils.assertNotNull(path, "path should not be null"); // nOI18N
        CndUtils.assertNotNull(pathToValidate, "pathToValidate should not be null"); // nOI18N
        if (path == null || pathToValidate == null) {
            return false;
        }
        return unifySeparators(pathToValidate).startsWith(unifySeparators(path));
    }

    private static final String REMOTE_PATH_MAP = "remote-path-map"; // NOI18N
    private static final String DELIMITER = "\n"; // NOI18N

    private static String getEnvSyncID(ExecutionEnvironment env) {
        return ServerList.get(env).getSyncFactory().getID();
    }

    private static String getPreferences(ExecutionEnvironment execEnv) {
        return NbPreferences.forModule(RemotePathMap.class).get(
                REMOTE_PATH_MAP + ExecutionEnvironmentFactory.toUniqueID(execEnv) + getEnvSyncID(execEnv), null);
    }

    private void setPreferences(String newValue) {
        NbPreferences.forModule(RemotePathMap.class).put(
                REMOTE_PATH_MAP + ExecutionEnvironmentFactory.toUniqueID(execEnv) + getEnvSyncID(execEnv), newValue);
    }

    private static boolean validateMapping(ExecutionEnvironment execEnv,
            String rpath, File lpath) throws InterruptedException {
        if (!PlatformInfo.getDefault(execEnv).isWindows() && !PlatformInfo.getDefault(ExecutionEnvironmentFactory.getLocal()).isWindows()) {
            return isTheSame(execEnv, rpath, lpath);
        }
        return false;
    }
    
    /**
     * Determines whether local and remote directories coincide,
     * i.e. map to the same physical directory
     * @param execEnv remote environment
     * @param localDir local path
     * @param remoteDir remote path
     * @return
     */
    // TODO: move to a more appropriate place
    @org.netbeans.api.annotations.common.SuppressWarnings("RV") // FindBugs warns that validationFile.delete() ret. value is ignored
    public static boolean isTheSame(ExecutionEnvironment execEnv, String rpath, File path) throws InterruptedException {
        if (path.exists() && path.isDirectory()) {
            File validationFile = null;
            try {
                // create file
                validationFile = File.createTempFile("cnd", "tmp", path); // NOI18N
                if (validationFile.exists()) {
                    BufferedWriter out = new BufferedWriter(new FileWriter(validationFile));
                    String validationLine = Double.toString(Math.random());
                    out.write(validationLine);
                    out.close();

                    RemoteCommandSupport rcs = new RemoteCommandSupport(
                            execEnv, "grep", null, // NOI18N
                            validationLine,
                            rpath + "/" + validationFile.getName()); // NOI18N

                    if (rcs.run() == 0) {
                        return true;
                    }
                    if (rcs.isCancelled() || rcs.isInterrupted()) {
                        throw new InterruptedException();
                    }
                }
            } catch (IOException ex) {
                // directory is write protected
            } finally {
                if (validationFile != null && validationFile.exists()) {
                    validationFile.delete(); // it isn\t worth removing RV FindBugs violation here
                }
            }
        }
        return false;
    }

    protected void initLocalBase() {
        if (localBase == null) {
            String tmpLocalBase = getLocalSyncRoot(execEnv);
            if (tmpLocalBase.endsWith("/")) { //NOI18N
                tmpLocalBase = tmpLocalBase.substring(0, tmpLocalBase.length() - 1);
            }
            localBase = tmpLocalBase;
        }
    }


    private final static class CustomizableRemotePathMap extends RemotePathMap {

        private CustomizableRemotePathMap(ExecutionEnvironment exc) {
            super(exc);
        }

        @Override
        public void init() {
            if (!loadFromPrefs()) {
                // 2. Automated mappings gathering entry point
                HostMappingsAnalyzer ham = new HostMappingsAnalyzer(execEnv);
                synchronized( map ) {
                    map.putAll(ham.getMappings());
                }
                // TODO: what about consequent runs. User may share something, we need to check it
            }
        }
    }

    private static final String NO_MAPPING_PREFIX = "///"; // NOI18N
    private final static class FixedRemotePathMap extends RemotePathMap {

        private volatile String remoteBase;
        
        private FixedRemotePathMap(ExecutionEnvironment exc) {
            super(exc);
            initRemoteBase(false);
        }

        @Override
        public void init() {
            if (!loadFromPrefs()) {
                if (remoteBase != null) {
                    super.addMappingImpl("/", remoteBase); // NOI18N
                }
            }
        }

        @Override
        public String getRemotePath(String lpath, boolean useDefault) {
            CndUtils.assertNotNull(lpath, "local path should not be null"); // nOI18N
            if (lpath == null) {
                return null;
            }
            initRemoteBase(true);
            if (remoteBase == null) {
                return useDefault ? lpath : null;
            }
            String remotePath = lpath;
            // for IZ#175198
            if (remotePath.startsWith(NO_MAPPING_PREFIX)) {
                return remotePath;
            }
            if (!isSubPath(remoteBase, lpath)) {
                if (lpath != null && Utilities.isWindows() && !"/".equals(lpath)) { // NOI18N
                    lpath = WindowsSupport.getInstance().convertToMSysPath(lpath);
                }
                remotePath = super.getRemotePath(lpath, useDefault);
            }
            return remotePath;
        }

        @Override
        public String getLocalPath(String rpath, boolean useDefault) {
            initRemoteBase(true);
            // for IZ#175198
            if (rpath.startsWith(NO_MAPPING_PREFIX)) {
                return rpath;
            }
            String res;
            if (isSubPath(remoteBase, rpath)) {
                res = super.getLocalPath(rpath, useDefault);
                if (res != null && Utilities.isWindows() && !"/".equals(res)) { // NOI18N
                    res = WindowsSupport.getInstance().convertFromMSysPath(res);
                }
            } else {
                initLocalBase();
                res = localBase  + '/' + rpath;
            }
            return res;
        }

        private void initRemoteBase(boolean addMapping) {
            if (remoteBase == null) {
                remoteBase = getRemoteSyncRoot(super.execEnv);
                if (addMapping && remoteBase != null) {
                    addMappingImpl("/", remoteBase); // NOI18N
                }
            }
        }

        @Override
        public void addMapping(String localParent, String remoteParent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void updatePathMap(Map<String, String> newPathMap) {
            CndUtils.assertTrue(false, "Should never be called for " + getClass().getSimpleName()); //NOI18N
        }
    }

    public static String getRemoteSyncRoot(ExecutionEnvironment executionEnvironment) {
        for (MirrorPathProvider mpp : Lookup.getDefault().lookupAll(MirrorPathProvider.class)) {
            String result = mpp.getRemoteMirror(executionEnvironment);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static String getLocalSyncRoot(ExecutionEnvironment executionEnvironment) {
        for (MirrorPathProvider mpp : Lookup.getDefault().lookupAll(MirrorPathProvider.class)) {
            String result = mpp.getLocalMirror(executionEnvironment);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}

