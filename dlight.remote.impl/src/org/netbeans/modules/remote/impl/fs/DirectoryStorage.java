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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Keeps information about all files that reside in the directory
 * @author Vladimir Kvashin
 */
public final class DirectoryStorage {

    private final Map<String, DirEntry> entries;
    /** known inexistent files in the case of r/o directory - see #196841 - remote FS doesn't work if any part of path to file does not have read permission */
    private final Set<String> dummies = new HashSet<String>();
    private final File cacheFile;
    private static final int VERSION = RemoteDirectory.getLsViaSftp() ? 4 : 3;
    /* Incompatible version to discard */
    private static final int ODD_VERSION = RemoteDirectory.getLsViaSftp() ? 3 : 2;

    public DirectoryStorage(File file) {
        this.cacheFile = file;
        entries = new HashMap<String, DirEntry>();
    }

    public DirectoryStorage(File file, Collection<DirEntry> newEntries) {
        this(file, newEntries, null);
    }

    public DirectoryStorage(File file, Collection<DirEntry> newEntries, Collection<String> newDummies) {
        this.cacheFile = file;
        this.entries = new HashMap<String, DirEntry>();
        for (DirEntry entry : newEntries) {
            entries.put(entry.getName(), entry);
        }
        if (newDummies != null) {
            dummies.addAll(newDummies);
        }
    }
    
    static DirectoryStorage load(File storageFile) throws IOException, FormatException {
        DirectoryStorage out = new DirectoryStorage(storageFile);
        out.load();
        return out;
    }

    /**
     * Format is:
     *      name cache access user group size "timestamp" link
     * Note that
     *      access contains file type as well (leftmost character)
     *      name is escaped (i.e. " " is replaced by "\\ ", "\\" by "\\\\")
     *      timestamp is quoted,
     *      access and timestamp is as in ls output on remote system
     * @throws IOException
     */
    private void load() throws IOException, FormatException {
        synchronized (DirectoryStorage.this) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(cacheFile));
                // check version
                String line = br.readLine();
                String prefix = "VERSION="; // NOI18N
                if (line == null || ! line.startsWith(prefix)) {
                    throw new FormatException("Wrong file format " + cacheFile.getAbsolutePath() + " line " + line, false); //NOI18N)
                }
                int version;
                try {
                    version = Integer.parseInt(line.substring(prefix.length()));
                } catch (NumberFormatException nfe) {
                    throw new FormatException("wrong version format " + cacheFile.getAbsolutePath(), nfe); // NOI18N
                }
                if (version > VERSION) {
                    throw new FormatException("directory cache file version " + version +  //NNOI18N
                            " not supported: " + cacheFile.getAbsolutePath(), true); //NOI18N
                }
                if (version < ODD_VERSION) {
                    throw new FormatException("Discarding old directory cache file version " + version +  //NNOI18N
                            ' ' + cacheFile.getAbsolutePath(), true); //NOI18N
                }
                line = br.readLine();
                prefix = "dummies="; // NOI18N
                if (line == null || ! line.startsWith(prefix)) {
                    throw new FormatException("Wrong file format " + cacheFile.getAbsolutePath() + " line " + line, false); //NOI18N)
                }
                int dummiesCount;
                try {
                    dummiesCount = Integer.parseInt(line.substring(prefix.length()));
                } catch (NumberFormatException nfe) {
                    throw new FormatException("wrong dummies count format " + cacheFile.getAbsolutePath(), nfe); // NOI18N
                }
                for (int i = 0; i < dummiesCount; i++) {
                    line = br.readLine();
                    if (line == null) {
                        throw new FormatException("premature end of file " + cacheFile.getAbsolutePath(), false); // NOI18N
                    } else {
                        dummies.add(line);
                    }
                }
                while ((line = br.readLine()) != null) {
                    if (line.length() == 0) {
                        continue; // just in case, ignore empty lines
                    }
                    DirEntry entry = RemoteDirectory.getLsViaSftp() ? 
                            DirEntrySftp.fromExternalForm(line) : DirEntryLs.fromExternalForm(line);
                    entries.put(entry.getName(), entry);
                }
             } finally {
                if (br != null) {
                    br.close();
                }
            }
        }
    }

    public void touch() throws IOException {
        if (cacheFile.exists()) {
            cacheFile.setLastModified(System.currentTimeMillis());
        } else {
            store();
        }
    }
    
    public boolean isKnown(String fileName) {
        synchronized (this) {
            return entries.containsKey(fileName) || dummies.contains(fileName);
        }
    }
    
    public void store() throws IOException {
        BufferedWriter wr = null;
        synchronized (this) {
            try {
                wr = new BufferedWriter(new FileWriter(cacheFile));
                wr.write("VERSION=" + VERSION + "\n"); //NOI18N
                wr.write("dummies=" + dummies.size() + '\n'); //NOI18N
                for (String dummy: dummies) {
                    wr.write(dummy);
                    wr.write('\n');
                }
                for (DirEntry entry : entries.values()) {
                    wr.write(entry.toExternalForm());
                    wr.write('\n');
                }
                wr.close();
                wr = null;
            } finally {
                if (wr != null) {
                    wr.close();
                }
            }
        }
    }

    public DirEntry getEntry(String fileName) {
        synchronized (this) {
            return entries.get(fileName);
        }
    }
    
    public  DirEntry removeEntry(String fileName) {
        synchronized (this) {
            return entries.remove(fileName);
        }
    }

    public List<DirEntry> list() {
        synchronized (this) {
            return new ArrayList<DirEntry>(entries.values());
        }
    }

    public int size() {
        synchronized (this) {
            return entries.size();
        }
    }

    /*package*/ void testAddEntry(DirEntry entry) {
        synchronized (this) {
            entries.put(entry.getName(), entry);
        }
    }
    
    /*package*/ void testAddDummy(String dummy) {
        synchronized (this) {
            dummies.add(dummy);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DirectoryStorage"); // NOI18N
        sb.append(" file=").append(cacheFile.getAbsolutePath()); // NOI18N
        sb.append(" entries.size()=").append(entries.size()).append("\n"); // NOI18N
        int cnt = 0;
        for (DirEntry entry : entries.values()) {
            if (cnt > 0) {
                sb.append('\n');
            }
            if (cnt++ <= 10) {
                sb.append(entry);
            } else {
                sb.append("..."); // NOI18N
                break;
            }
        }
        return sb.toString();
    }    
}
