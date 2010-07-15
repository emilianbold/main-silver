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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.core.startup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.Util;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.lookup.ServiceProvider;

/**
 * Ability to locate NBM-installed files.
 * Looks in ${netbeans.user} then each component of ${netbeans.dirs}
 * and finally ${netbeans.home}.
 * @author Jesse Glick
 */
@ServiceProvider(service=InstalledFileLocator.class)
public final class InstalledFileLocatorImpl extends InstalledFileLocator {
    
    private static final Logger LOG = Logger.getLogger(InstalledFileLocatorImpl.class.getName());
    
    private final File[] dirs;
    public InstalledFileLocatorImpl() {
        List<File> _dirs = new ArrayList<File>();
        addDir(_dirs, System.getProperty("netbeans.user"));
        String nbdirs = System.getProperty("netbeans.dirs"); // #27151
        if (nbdirs != null) {
            StringTokenizer tok = new StringTokenizer(nbdirs, File.pathSeparator);
            while (tok.hasMoreTokens()) {
                addDir(_dirs, tok.nextToken());
            }
        }
        addDir(_dirs, System.getProperty("netbeans.home"));
        dirs = _dirs.toArray(new File[_dirs.size()]);
    }
    
    private static void addDir(List<File> _dirs, String d) {
        if (d != null) {
            File f = new File(d).getAbsoluteFile();
            if (f.isDirectory()) {
                _dirs.add(FileUtil.normalizeFile(f));
            }
        }
    }
    
    /**
     * Cache of installed files (if present).
     * Keys are directory prefixes, e.g. "" or "x/" or "x/y/" ('/' is sep).
     * The values are nested maps; keys are entries in {@link #dirs}
     * (not all entries need have keys, only those for which the dir exists),
     * and values are unqualified file names which exist in that dir.
     */
    private static Map<String,Map<File,Set<String>>> fileCache = null;
    
    /**
     * Called from <code>Main.run</code> early in the startup sequence to indicate
     * that available files should be cached from now on. Should be matched by a call to
     * {@link #discardCache} since the cache will be invalid if the user
     * e.g. installs a new NBM without restarting.
     */
    public static synchronized void prepareCache() {
        assert fileCache == null;
        fileCache = new HashMap<String,Map<File,Set<String>>>();
    }
    
    /**
     * Called after startup is essentially complete.
     * After this point, the list of files in the installation are not
     * cached, since they might change due to dynamic NBM installation.
     * Anyway the heaviest uses of {@link InstalledFileLocator} are
     * during startup so that is when the cache has the most effect.
     */
    public static synchronized void discardCache() {
        assert fileCache != null;
        fileCache = null;
    }
    
    /**
     * Searches user dir and install dir(s).
     */
    public @Override File locate(String relativePath, String codeNameBase, boolean localized) {
        Set<File> files = doLocate(relativePath, localized, true, codeNameBase);
        return files.isEmpty() ? null : files.iterator().next();
    }
    
    public @Override Set<File> locateAll(String relativePath, String codeNameBase, boolean localized) {
        return doLocate(relativePath, localized, false, codeNameBase);
    }

    private Set<File> doLocate(String relativePath, boolean localized, boolean single, String codeNameBase) {
        String[] prefixAndName = prefixAndName(relativePath);
        String prefix = prefixAndName[0];
        String name = prefixAndName[1];
        synchronized (InstalledFileLocatorImpl.class) {
            if (localized) {
                int i = name.lastIndexOf('.');
                String baseName, ext;
                if (i == -1) {
                    baseName = name;
                    ext = "";
                } else {
                    baseName = name.substring(0, i);
                    ext = name.substring(i);
                }
                Set<File> files = null;
                for (String suffix : org.netbeans.Util.getLocalizingSuffixesFast()) {
                    String locName = baseName + suffix + ext;
                    Set<File> f = locateExactPath(prefix, locName, single, codeNameBase);
                    if (!f.isEmpty()) {
                        if (single) {
                            return f;
                        } else if (files == null) {
                            files = f;
                        } else {
                            files = new LinkedHashSet<File>(files);
                            files.addAll(f);
                        }
                    }
                }
                return files != null ? files : Collections.<File>emptySet();
            } else {
                return locateExactPath(prefix, name, single, codeNameBase);
            }
        }
    }

    /** Search all top dirs for a file. */
    private Set<File> locateExactPath(String prefix, String name, boolean single, String codeNameBase) {
        assert Thread.holdsLock(InstalledFileLocatorImpl.class);
        Set<File> files = null;
        if (fileCache != null) {
            Map<File,Set<String>> fileCachePerPrefix = fileCachePerPrefix(prefix);
            for (int i = 0; i < dirs.length; i++) {
                Set<String> names = fileCachePerPrefix.get(dirs[i]);
                if (names != null && names.contains(name)) {
                    assert owned(codeNameBase, dirs[i], prefix, name);
                    File f = makeFile(dirs[i], prefix, name);
                    if (single) {
                        return Collections.singleton(f);
                    } else if (files == null) {
                        files = Collections.singleton(f);
                    } else {
                        files = new LinkedHashSet<File>(files);
                        files.add(f);
                    }
                }
            }
        } else {
            for (int i = 0; i < dirs.length; i++) {
                File f = makeFile(dirs[i], prefix, name);
                if (f.exists()) {
                    assert owned(codeNameBase, dirs[i], prefix, name);
                    if (single) {
                        return Collections.singleton(f);
                    } else if (files == null) {
                        files = Collections.singleton(f);
                    } else {
                        files = new LinkedHashSet<File>(files);
                        files.add(f);
                    }
                }
            }
        }
        return files != null ? files : Collections.<File>emptySet();
    }

    private static String[] prefixAndName(String relativePath) {
        if (relativePath.length() == 0) {
            throw new IllegalArgumentException("Cannot look up \"\" in InstalledFileLocator.locate"); // NOI18N
        }
        if (relativePath.charAt(0) == '/') {
            throw new IllegalArgumentException("Paths passed to InstalledFileLocator.locate should not start with '/': " + relativePath); // NOI18N
        }
        int slashIdx = relativePath.lastIndexOf('/');
        if (slashIdx == relativePath.length() - 1) {
            throw new IllegalArgumentException("Paths passed to InstalledFileLocator.locate should not end in '/': " + relativePath); // NOI18N
        }

        String prefix, name;
        if (slashIdx != -1) {
            prefix = relativePath.substring(0, slashIdx + 1);
            name = relativePath.substring(slashIdx + 1);
            assert name.length() > 0;
        } else {
            prefix = "";
            name = relativePath;
        }
        return new String[] {prefix, name};
    }

    private Map<File,Set<String>> fileCachePerPrefix(String prefix) {
        assert Thread.holdsLock(InstalledFileLocatorImpl.class);
        Map<File,Set<String>> fileCachePerPrefix = fileCache.get(prefix);
        if (fileCachePerPrefix == null) {
            fileCachePerPrefix = new HashMap<File,Set<String>>(dirs.length * 2);
            for (int i = 0; i < dirs.length; i++) {
                File root = dirs[i];
                File d;
                if (prefix.length() > 0) {
                    assert prefix.charAt(prefix.length() - 1) == '/';
                    d = new File(root, prefix.substring(0, prefix.length() - 1).replace('/', File.separatorChar));
                } else {
                    d = root;
                }
                if (d.isDirectory()) {
                    String[] kids = d.list();
                    if (kids != null) {
                        fileCachePerPrefix.put(root, new HashSet<String>(Arrays.asList(kids)));
                    } else {
                        Util.err.log(Level.WARNING, "could not read files in {0}", d);
                    }
                }
            }
            fileCache.put(prefix, fileCachePerPrefix);
        }
        return fileCachePerPrefix;
    }
    
    private static File makeFile(File dir, String prefix, String name) {
        return new File(dir, prefix.replace('/', File.separatorChar) + name);
    }
    
    private static synchronized boolean owned(String codeNameBase, File dir, String prefix, String name) {
        String path = prefix + name;
        if (codeNameBase == null) {
            LOG.log(Level.WARNING, "no code name base passed when looking up {0}", path);
            return true;
        }
        if (name.indexOf('_') != -1) {
            // Probably a locale variant. Permit these to be owned by any module -
            // otherwise it would be difficult to contribute branding.
            return true;
        }
        String codeNameBaseDashes = codeNameBase.replace('.', '-');
        if (path.equals("modules/" + codeNameBaseDashes + ".jar")) { // NOI18N
            // Very common case, no need to waste time checking this.
            return true;
        }
        if (path.equals("update_tracking/" + codeNameBaseDashes + ".xml")) { // NOI18N
            // Technically illegitimate - no one owns this metadata - but used by
            // org.netbeans.modules.autoupdate.services.Utilities.locateUpdateTracking
            // and probably harmless since this module would not be used with other impls.
            return true;
        }
        Map<String,Set<String>> ownershipByModule = ownershipByModuleByCluster.get(dir);
        File updateDir = new File(dir, "update_tracking");
        if (ownershipByModule == null) {
            if (!updateDir.isDirectory()) {
                LOG.log(Level.FINE, "No update tracking found in {0}", dir);
                return true;
            }
            ownershipByModule = new HashMap<String,Set<String>>();
            ownershipByModuleByCluster.put(dir, ownershipByModule);
        }
        Set<String> ownership = ownershipByModule.get(codeNameBase);
        if (ownership == null) {
            File list = new File(updateDir, codeNameBaseDashes + ".xml"); // NOI18N
            if (!list.isFile()) {
                LOG.log(Level.WARNING, "no such module {0}", list);
                return true;
            }
            ownership = new HashSet<String>();
            try {
                // Could do a proper XML parse but likely too slow.
                Reader r = new FileReader(list);
                try {
                    BufferedReader br = new BufferedReader(r);
                    String line;
                    while ((line = br.readLine()) != null) {
                        Matcher m = FILE_PATTERN.matcher(line);
                        if (m.matches()) {
                            ownership.add(m.group(1));
                        }
                    }
                    br.close();
                } finally {
                    r.close();
                }
            } catch (IOException x) {
                LOG.log(Level.INFO, "could not parse " + list, x);
                return true;
            }
            LOG.log(Level.FINE, "parsed {0} -> {1}", new Object[] {list, ownership});
            ownershipByModule.put(codeNameBase, ownership);
        }
        if (!ownership.contains(path)) {
            boolean found = false;
            if (makeFile(dir, prefix, name).isDirectory()) {
                String pathSlash = path + "/"; // NOI18N
                for (String owned : ownership) {
                    if (owned.startsWith(pathSlash)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                LOG.log(Level.WARNING, "module {0} in {1} does not own {2}", new Object[] {codeNameBase, dir, path});
            }
        }
        return true;
    }
    private static final Pattern FILE_PATTERN = Pattern.compile("\\s*<file.+name=\"([^\"]+)\".*/>");
    private static final Map<File,Map<String,Set<String>>> ownershipByModuleByCluster = new HashMap<File,Map<String,Set<String>>>();
    
}
