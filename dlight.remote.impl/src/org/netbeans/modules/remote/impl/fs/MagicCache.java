/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.impl.fs;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander Simon
 */
public class MagicCache {
    private static final int BUF_LENGTH = 80;
    private static final String cacheName = ".rfs_magic"; // NOI18N
    private final RemoteDirectory dir;
    private Map<String, byte[]> cache;
    
    public MagicCache(RemoteDirectory dir) {
        this.dir = dir;
    }
    
    public synchronized byte[] get(String fileName) {
        if (cache == null) {
            cache = new HashMap<String, byte[]>();
            readCache();
        }
        
        return cache.get(fileName);
    }
    
    public synchronized void clean(String fileName) {
        File od = new File(dir.getCache(),cacheName);
        if (od.exists()) {
            od.delete();
            if (cache != null) {
                if (fileName == null) {
                    cache.clear();
                } else {
                    cache.remove(fileName);
                }
            }
        }
    }
     
    private void readCache() {
        File od = new File(dir.getCache(),cacheName);
        if (!od.exists()) {
            try {
                updateCache();
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (UnsupportedEncodingException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (od.exists()) {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(od));
                String line = null;
                String file = null;
                byte[] res = null;
                int pos = 0;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("./")) { // NOI18N
                        createEntry(file, res, pos);
                        file = line.substring(2);
                        res = new byte[BUF_LENGTH];
                        pos = 0;
                    } else if (line.startsWith("00000")){ // NOI18N
                        String[] split = line.split(" "); // NOI18N
                        if (split.length > 1) {
                            for(int i = 1; i< split.length; i++) {
                                String s = split[i];
                                long L = Long.parseLong(s, 16);
                                res[pos++] = (byte) (L & 0xFF);
                                res[pos++] = (byte) (L>>8 & 0xFF);
                                res[pos++] = (byte) (L>>16 & 0xFF);
                                res[pos++] = (byte) (L>>24 & 0xFF);
                            }
                        }
                    }
                }
                createEntry(file, res, pos);
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
    }
    
    private void createEntry(String file, byte[] res, int pos) {
        if (file != null) {
            if (pos < res.length) {
                byte[] ares = new byte[pos];
                System.arraycopy(res, 0, ares, 0, pos);
                res = ares;
            }
            cache.put(file, res);
        }
    }
    
    private void updateCache() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        // TODO check connection
        String command = "/usr/bin/find . ! -name . -prune -type f -print -exec od -X -N "+BUF_LENGTH+" {} \\;"; // NOI18N
        String path = dir.getPath();
        if (path.isEmpty()) {
            path = "/"; // NOI18N
        }
        ExitStatus result = ProcessUtils.executeInDir(path, dir.getExecutionEnvironment(), "/bin/sh", "-c", command); // NOI18N
        File od = new File(dir.getCache(),cacheName);
        OutputStreamWriter os = null;
        try {
            os = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(od)));
            os.write(result.output);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                }
            }
        }
    }
}
