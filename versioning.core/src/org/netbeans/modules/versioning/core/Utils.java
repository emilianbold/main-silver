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
package org.netbeans.modules.versioning.core;

import org.netbeans.modules.versioning.core.spi.VCSSystemProvider;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.RequestProcessor;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;
import org.openide.awt.Actions;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.netbeans.modules.versioning.fileproxy.spi.VCSContext;
import org.netbeans.api.fileinfo.NonRecursiveFolder;

import javax.swing.*;
import javax.swing.text.Document;
import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.versioning.fileproxy.api.VCSFileProxy;
import org.netbeans.modules.versioning.fileproxy.spi.VersioningSupport;
import org.netbeans.modules.versioning.fileproxy.spi.VersioningSystem;
import org.openide.filesystems.FileSystem;
import org.openide.util.Utilities;

/**
 * Utilities for Versioning SPI classes. 
 * 
 * @author Maros Sandor
 */
public class Utils {
    
    /**
     * Request processor for long running tasks.
     */
    private static final RequestProcessor vcsBlockingRequestProcessor = new RequestProcessor("Versioning long tasks", 1, false, false);

    /**
     * Keeps the nb masterfilesystem
     */
    private static FileSystem filesystem;

    /**
     * Keeps excluded/unversioned folders
     */
    private static VCSFileProxy [] unversionedFolders;
    
    /**
     * Temporary top folder for vcs in a single session
     */
    private static File tempDir;

    /**
     * Constructs a VCSContext out of a Lookup, basically taking all Nodes inside. 
     * Nodes are converted to Files based on their nature. 
     * For example Project Nodes are queried for their SourceRoots and those roots become the root files of this context.
     * 
     * @param lookup a lookup
     * @return VCSContext containing nodes from Lookup
     */ 
    public static VCSContext contextForLookup(Lookup lookup) {
        Lookup.Result<Node> result = lookup.lookup(new Lookup.Template<Node>(Node.class));
        Collection<? extends Node> nodes = result.allInstances();
        return VCSContext.forNodes(nodes.toArray(new Node[nodes.size()]));
    }
        
    public static VCSContext contextForFileObjects(Set<? extends FileObject> files) {
        Set<VCSFileProxy> roots = new HashSet<VCSFileProxy>(files.size());
        if (files instanceof NonRecursiveFolder) {
            FileObject folder = ((NonRecursiveFolder) files).getFolder();
            VCSFileProxy file = createFlatFileProxy(folder);
            if(file != null) {
                roots.add(file);
            }
        } else {
            for (FileObject fo : files) {
                VCSFileProxy file = VCSFileProxy.createFileProxy(fo);
                if (file != null) {
                    roots.add(file);
                }
            }
        }
        return SPIAccessor.IMPL.createContextForFiles(roots, files);
    }
    
    public static VCSFileProxy createFlatFileProxy(FileObject fo) {
        return APIAccessor.IMPL.createFlatFileProxy(fo);
    }
    
    /**
     * Tests for ancestor/child file relationsip.
     * 
     * @param ancestor supposed ancestor of the file
     * @param file a file
     * @return true if ancestor is an ancestor folder of file OR both parameters are equal, false otherwise
     */
    public static boolean isAncestorOrEqual(VCSFileProxy ancestor, VCSFileProxy file) {
        if (ancestor.isFlat()) {
            return ancestor.equals(file) || ancestor.equals(file.getParentFile()) && !file.isDirectory();
        }
        
        String filePath = file.getAbsolutePath();
        String ancestorPath = ancestor.getAbsolutePath();
        if(Utilities.isWindows()) {
            if(filePath.indexOf("~") < 0 && ancestorPath.indexOf("~") < 0) {
                if(filePath.length() < ancestorPath.length()) {
                    return false;
                }
            }
        } else if (Utilities.isMac()) {
            // Mac is not case sensitive, cannot use the else statement
            if(filePath.length() < ancestorPath.length()) {
                return false;
            }
        } else {
            if(!filePath.startsWith(ancestorPath)) {
                return false;
            }
        }

        // get sure as it still could be something like:
        // ancestor: /home/dil
        // file:     /home/dil1/dil2
        for (; file != null; file = file.getParentFile()) {
            if (file.equals(ancestor)) return true;
        }
        return false;
    }

    /**
     * Creates a menu item from an action.
     * 
     * @param action an action
     * @return JMenuItem
     */
    public static JMenuItem toMenuItem(Action action) {
        JMenuItem item;
        if (action instanceof Presenter.Menu) {
            item = ((Presenter.Menu) action).getMenuPresenter();
        } else {
            item = new JMenuItem();
            Actions.connect(item, action, false);
        }
        return item;
    }

    public static File getTempFolder() {
        File tmpDir = getMainTempDir();
        for (;;) {
            File dir = new File(tmpDir, "vcs-" + Long.toString(System.currentTimeMillis())); // NOI18N
            if (!dir.exists() && dir.mkdirs()) {
                dir.deleteOnExit();
                return FileUtil.normalizeFile(dir);
            }
        }
    }

    /**
     * Recursively deletes the file or directory.
     *
     * @param file file/directory to delete
     */
    public static void deleteRecursively(File file) {
        deleteRecursively(file, Level.WARNING);
    }

    /**
     * Recursively deletes the file or directory.
     *
     * @param file file/directory to delete
     * @param level log level
     */
    public static void deleteRecursively(File file, Level level) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) return;
        try {
            fo.delete();
        } catch (IOException e) {
            Logger.getLogger(Utils.class.getName()).log(level, "", e);
        }
    }
    
    public static Reader getDocumentReader(final Document doc) {
        final String[] str = new String[1];
        Runnable run = new Runnable() {
            @Override
            public void run () {
                try {
                    str[0] = doc.getText(0, doc.getLength());
                } catch (javax.swing.text.BadLocationException e) {
                    // impossible
                    VersioningManager.LOG.log(Level.WARNING, null, e);
                }
            }
        };
        doc.render(run);
        return new StringReader(str[0]);
    }
    
    /**
     * Creates a task that will run in the Versioning RequestProcessor (with has throughput of 1). The runnable may take long
     * to execute (connet through network, etc).
     * 
     * @param runnable Runnable to run
     * @return RequestProcessor.Task created task
     */
    public static RequestProcessor.Task createTask(Runnable runnable) {
        return vcsBlockingRequestProcessor.create(runnable);
    }

    public static String getDisplayName(VCSSystemProvider.VersioningSystem system) {
        return (String) system.getProp(VersioningSystem.PROP_DISPLAY_NAME); // 
    }

    public static String getMenuLabel(VCSSystemProvider.VersioningSystem system) {
        return (String) system.getProp(VersioningSystem.PROP_MENU_LABEL);
    }
    
    public static JSeparator createJSeparator() {
        JMenu menu = new JMenu();
        menu.addSeparator();
        return (JSeparator)menu.getPopupMenu().getComponent(0);
    }

    /**
     * Calls {@link java.io.File#isFile()} and returns it's result.
     * But loggs a warning if {@link java.io.File#isFile()} blocks for a defined amount of time.
     * @param file file to test
     * @return result of {@link java.io.File#isFile()}
     */
    public static boolean isFile (VCSFileProxy file) {
        long startTime = System.currentTimeMillis();
        try {
            return file.isFile();
        } finally {
            logLasting(file, System.currentTimeMillis() - startTime,
                    "Utils.isFile: java.io.File.isFile takes too much time ({0} ms): {1}, stacktrace:"); //NOI18N
        }
    }

    /**
     * Calls {@link java.io.File#exists()} and returns it's result.
     * But loggs a warning if {@link java.io.File#exists()} blocks for a defined amount of time.
     * @param file file to test
     * @return result of {@link java.io.File#exists()}
     */
    public static boolean exists (VCSFileProxy file) {
        long startTime = System.currentTimeMillis();
        try {
            return file.exists();
        } finally {
            logLasting(file, System.currentTimeMillis() - startTime,
                    "Utils.exists: java.io.File.exists takes too much time ({0} ms): {1}, stacktrace:"); //NOI18N
        }
    }

    /**
     * Calls {@link java.io.File#canWrite()} and returns it's result.
     * But loggs a warning if {@link java.io.File#canWrite()} blocks for a defined amount of time.
     * @param file file to test
     * @return result of {@link java.io.File#canWrite()}
     */
    public static boolean canWrite (VCSFileProxy file) {
        long startTime = System.currentTimeMillis();
        try {
            return file.canWrite();
        } finally {
            logLasting(file, System.currentTimeMillis() - startTime,
                    "Utils.canWrite: java.io.File.canWrite takes too much time ({0} ms): {1}, stacktrace:"); //NOI18N
        }
    }

    public static VCSFileProxy[] getUnversionedFolders () {
        if (unversionedFolders == null) {
            VCSFileProxy[] files;
            try {
                String uf = VersioningSupport.getPreferences().get("unversionedFolders", ""); //NOI18N
                String ufProp = System.getProperty("versioning.unversionedFolders", ""); //NOI18N
                StringBuilder sb = new StringBuilder(uf);
                String nbUserdir = System.getProperty("netbeans.user", ""); //NOI18N
                if (!nbUserdir.isEmpty() && !isVersionUserdir()) { 
                    if (sb.length() > 0) {
                        sb.append(';');
                    }
                    sb.append(nbUserdir);
                }
                if (!ufProp.isEmpty()) {
                    if (sb.length() > 0) {
                        sb.append(';');
                    }
                    sb.append(ufProp);
                }
                if (sb.length() == 0) {
                    files = new VCSFileProxy[0];
                } else {
                    String [] paths = sb.toString().split("\\;"); //NOI18N
                    files = new VCSFileProxy[paths.length];
                    int idx = 0;
                    for (String path : paths) {
                        files[idx++] = VCSFileProxy.createFileProxy(new File(path));
                    }
                }
            } catch (Exception e) {
                files = new VCSFileProxy[0];
                Logger.getLogger(Utils.class.getName()).log(Level.INFO, e.getMessage(), e);
            }
            unversionedFolders = files;
        }
        return unversionedFolders;
    }

    static boolean isVersionUserdir() {
        return "true".equals(System.getProperty("versioning.netbeans.user.versioned", "false")); // NOI18N
    }

    static FileSystem getRootFilesystem() {
        if(filesystem == null) {
            try {
                String userDir = System.getProperty("netbeans.user"); // NOI18N
                FileObject fo = FileUtil.toFileObject(new File(userDir));
                filesystem = fo.getFileSystem();
            } catch (FileStateInvalidException ex) {
                VersioningManager.LOG.log(Level.WARNING, null, ex);
            }
        }
        return filesystem;
    }

    /**
     * Helper method to get an array of Strings from preferences.
     *
     * @param prefs storage
     * @param key key of the String array
     * @return List<String> stored List of String or an empty List if the key was not found (order is preserved)
     */
    public static List<String> getStringList (Preferences prefs, String key) {
        List<String> retval = new ArrayList<String>();
        try {
            String[] keys = prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                if (k != null && k.startsWith(key)) {
                    int idx = Integer.parseInt(k.substring(k.lastIndexOf('.') + 1));
                    retval.add(idx + "." + prefs.get(k, null));
                }
            }
            List<String> rv = new ArrayList<String>(retval.size());
            rv.addAll(retval);
            for (String s : retval) {
                int pos = s.indexOf('.');
                int index = Integer.parseInt(s.substring(0, pos));
                rv.set(index, s.substring(pos + 1));
            }
            return rv;
        } catch (Exception ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.INFO, null, ex);
            return new ArrayList<String>(0);
        }
    }

    /**
     * Stores a List of Strings into Preferences node under the given key.
     *
     * @param prefs storage
     * @param key key of the String array
     * @param value List of Strings to write (order will be preserved)
     */
    public static void put (Preferences prefs, String key, List<String> value) {
        try {
            String[] keys = prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                if (k != null && k.startsWith(key + ".")) {
                    prefs.remove(k);
                }
            }
            int idx = 0;
            for (String s : value) {
                prefs.put(key + "." + idx++, s);
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.INFO, null, ex);
        }
    }

    private static synchronized File getMainTempDir () {
        if (tempDir == null) {
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));   // NOI18N
            for (;;) {
                File dir = new File(tmpDir, "vcs-" + Long.toString(System.currentTimeMillis())); // NOI18N
                if (!dir.exists() && dir.mkdirs()) {
                    tempDir = FileUtil.normalizeFile(dir);
                    tempDir.deleteOnExit();
                    break;
                }
            }
        }
        return tempDir;
    }

    private static void logLasting (VCSFileProxy file, long last, String message) {
        boolean allowed = false;
        assert allowed = true;
        if (allowed && last > 1500) {
            StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (StackTraceElement e : stElements) {
                if (i++ > 1) {
                    if (i == 8) {
                        sb.append("...\n");                         // NOI18N
                        break;
                    } else {
                        sb.append(e.toString()).append("\n");       // NOI18N
                    }
                }
            }
            Logger.getLogger(Utils.class.getName()).log(Level.INFO, message, new String[]{Long.toString(last), file.getAbsolutePath()});
            Logger.getLogger(Utils.class.getName()).log(Level.INFO, sb.toString());
        }
    }
}
