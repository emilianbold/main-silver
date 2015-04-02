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

package org.netbeans.modules.projectapi;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.BaseUtilities;
import org.openide.util.NbPreferences;
import org.openide.util.WeakSet;

/**
 * Finds a project by searching the directory tree.
 * @author Jesse Glick
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.project.FileOwnerQueryImplementation.class, position=100)
public class SimpleFileOwnerQueryImplementation implements FileOwnerQueryImplementation {
    private static final Logger LOG = Logger.getLogger(SimpleFileOwnerQueryImplementation.class.getName());
    private static final URI UNOWNED_URI = URI.create("http:unowned");
    private static final Set<String> forbiddenFolders;
    static {
        Set<String> files = new HashSet<String>();
        try {
            String forbidden = System.getProperty("project.forbiddenFolders", System.getProperty("versioning.forbiddenFolders", "")); //NOI18N
            files.addAll(Arrays.asList(forbidden.split("\\;"))); //NOI18N
            files.remove(""); //NOI18N
        } catch (Exception e) {
            LOG.log(Level.INFO, e.getMessage(), e);
        }
        forbiddenFolders = files;
    }
    
    /** Do nothing */
    public SimpleFileOwnerQueryImplementation() {}
    
    public Project getOwner(URI fileURI) {
        // Try to find a FileObject for it.
        URI test = fileURI;
        FileObject file;
        do {
            file = uri2FileObject(test);
            test = goUp(test);
        } while (file == null && test != null);
        if (file == null) {
            return null;
        }
        return getOwner(file);
    }
    
    private final Set<FileObject> warnedAboutBrokenProjects = new WeakSet<FileObject>();
        
    private Reference<FileObject> lastFoundKey = null;
    private Reference<Project> lastFoundValue = null;
    
    /**
     * 
     * #111892
     */
    public void resetLastFoundReferences() {
        synchronized (this) {
            lastFoundValue = null;
            lastFoundKey = null;
        }
    }
    
    
    public Project getOwner(FileObject f) {
        deserialize();
        while (f != null) {
            synchronized (this) {
                if (lastFoundKey != null && lastFoundKey.get() == f) {
                    Project p = lastFoundValue.get();
                    if (p != null) {
                        return p;
                    }
                }
            }
            boolean folder = f.isFolder();
            if (folder) {
                if (!forbiddenFolders.contains(f.getPath())) {
                    Project p;
                    try {
                        p = ProjectManager.getDefault().findProject(f);
                    } catch (IOException e) {
                        // There is a project here, but we cannot load it...
                        if (warnedAboutBrokenProjects.add(f)) { // #60416
                            LOG.log(Level.FINE, "Cannot load project.", e); //NOI18N
                        }
                        return null;
                    }
                    if (p != null) {
                        synchronized (this) {
                            lastFoundKey = new WeakReference<FileObject>(f);
                            lastFoundValue = new WeakReference<Project>(p);
                        }
                        return p;
                    }
                }
            }
            
            if (!externalOwners.isEmpty() && (folder || externalRootsIncludeNonFolders)) {
                URI externalOwnersURI = externalOwners.get(f.toURI());

                if (externalOwnersURI != null) {
                    if (externalOwnersURI == UNOWNED_URI) {
                        return FileOwnerQuery.UNOWNED;
                    }
                    FileObject externalOwner = uri2FileObject(externalOwnersURI);

                    if (externalOwner != null && externalOwner.isValid()) {
                        try {
                            // Note: will be null if there is no such project.
                            Project p = ProjectManager.getDefault().findProject(externalOwner);
                            synchronized (this) {
                                lastFoundKey = new WeakReference<FileObject>(f);
                                lastFoundValue = new WeakReference<Project>(p);
                            }
                            return p;
                        } catch (IOException e) {
                            // There is a project there, but we cannot load it...
                            LOG.log(Level.FINE, "Cannot load project.", e); //NOI18N
                            return null;
                        }
                    }
                }
            }
            if (!deserializedExternalOwners.isEmpty() && (folder || externalRootsIncludeNonFolders)) {
                FileObject externalOwner = deserializedExternalOwners.get(f.toURI());
                if (externalOwner != null && externalOwner.isValid()) {
                    try {
                        // Note: will be null if there is no such project.
                        Project p = ProjectManager.getDefault().findProject(externalOwner);
                        synchronized (this) {
                            lastFoundKey = new WeakReference<FileObject>(f);
                            lastFoundValue = new WeakReference<Project>(p);
                        }
                        return p;
                    } catch (IOException e) {
                        // There is a project there, but we cannot load it...
                        LOG.log(Level.FINE, "Cannot load project.", e); //NOI18N
                        return null;
                    }
                }
            }
            
            f = f.getParent();
        }
        return null;
    }
    
    /**
     * Map from external source roots to the owning project directories.
     */
    private static final Map<URI,URI> externalOwners =
        Collections.synchronizedMap(new HashMap<URI,URI>());
    
    private static final Map<URI,FileObject> deserializedExternalOwners =
        Collections.synchronizedMap(new HashMap<URI,FileObject>());

    private static boolean externalRootsIncludeNonFolders = false;

    private static enum ExternalRootsState {
        NEW,
        LOADING,
        LOADED
    }

    //@GuardedBy("SimpleFileOwnerQueryImplementation.class")
    private static ExternalRootsState externalRootsState = ExternalRootsState.NEW;
    /**
     * Deserializes stored cross-reference of external files to their projects.
     * It is called from @OnStart, which runs asynchronously/in parallel, but 
     * getOwner() queries require that the cross-ref is laoded so the answers are
     * consistent in time.
     */
    static void deserialize() {
        boolean needsToLoad = false;
        synchronized (SimpleFileOwnerQueryImplementation.class) {
            LOG.log(Level.FINEST, "External Roots State: {0}", externalRootsState); //NOI18N    - unit tests
            switch (externalRootsState) {
                case NEW:
                    externalRootsState = ExternalRootsState.LOADING;
                    needsToLoad = true;
                    break;
                case LOADING:
                    while (externalRootsState == ExternalRootsState.LOADING) {
                        try {
                            SimpleFileOwnerQueryImplementation.class.wait();
                        } catch (InterruptedException ie) {
                            LOG.log(Level.INFO, null, ie);
                            break;
                        }
                    }
                    break;
                case LOADED:
                    break;
                default:
                    throw new IllegalStateException(String.format(
                        "Unknown external roots state: %s",    //NOI18N
                        externalRootsState));
            }
        }
        if (needsToLoad) {
            try {
                deserializeImpl();
                LOG.log(Level.FINEST, "External Roots Deserialized"); //NOI18N    - unit tests
            } finally {
                synchronized (SimpleFileOwnerQueryImplementation.class) {
                    assert externalRootsState == ExternalRootsState.LOADING;
                    externalRootsState = ExternalRootsState.LOADED;
                    SimpleFileOwnerQueryImplementation.class.notifyAll();
                }
            }
        }
    }

    private static void deserializeImpl() {
        try {
            Preferences p = NbPreferences.forModule(SimpleFileOwnerQueryImplementation.class).node("externalOwners");
            for (String name : p.keys()) {
                URL u = new URL(p.get(name, null));
                URI i = new URI(name);
                deserializedExternalOwners.put(i, URLMapper.findFileObject(u));
            }
        } catch (Exception ex) {
            LOG.log(Level.INFO, null, ex);
        }
        try {
            NbPreferences.forModule(SimpleFileOwnerQueryImplementation.class).node("externalOwners").removeNode();
        } catch (BackingStoreException ex) {
            LOG.log(Level.INFO, null, ex);
        }
    }

    static void serialize() {
        try {
            Preferences p = NbPreferences.forModule(SimpleFileOwnerQueryImplementation.class).node("externalOwners");
            for (URI uri : externalOwners.keySet()) {
                URI ownerURI = externalOwners.get(uri);
                if (ownerURI != UNOWNED_URI) {
                p.put(uri.toString(), ownerURI.toString());
            }
            }
            p.sync(); // #184310
        } catch (Exception ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        
    }
    
    /** @see FileOwnerQuery#reset */
    public static void reset() {
        externalOwners.clear();
    }
    
    /** @see FileOwnerQuery#markExternalOwner */
    public static void markExternalOwnerTransient(FileObject root, Project owner) {
        markExternalOwnerTransient(root.toURI(), owner);
    }
    
    /** @see FileOwnerQuery#markExternalOwner */
    public static void markExternalOwnerTransient(URI root, Project owner) {
        externalRootsIncludeNonFolders |= !root.getPath().endsWith("/");
        if (owner != null) {
            FileObject fo = owner.getProjectDirectory();
            externalOwners.put(root, owner == FileOwnerQuery.UNOWNED ? UNOWNED_URI : fo.toURI());
            deserializedExternalOwners.remove(root);
        } else {
            externalOwners.remove(root);
        }
    }
    
    private static FileObject uri2FileObject(URI u) {
        URL url;
        try {
            url = u.toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            assert false : u;
            return null;
        }
        return URLMapper.findFileObject(url);
    }
    
    private static URI goUp(URI u) {
        assert u.isAbsolute() : u;
        assert u.getFragment() == null : u;
        assert u.getQuery() == null : u;
        // XXX isn't there any easier way to do this?
        // Using getPath in the new path does not work; nbfs: URLs break. (#39613)
        // On the other hand, nbfs: URLs are not really used any more, so do we care?
        String path = u.getPath();
        if (path == null || path.equals("/")) { // NOI18N
            return null;
        }
        String us = u.toString();
        if (us.endsWith("/")) { // NOI18N
            us = us.substring(0, us.length() - 1);
            assert path.endsWith("/"); // NOI18N
            path = path.substring(0, path.length() - 1);
        }
        int idx = us.lastIndexOf('/');
        assert idx != -1 : path;
        if (path.lastIndexOf('/') == 0) {
            us = us.substring(0, idx + 1);
        } else {
            us = us.substring(0, idx);
        }
        URI nue;
        try {
            nue = new URI(us);
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
        if (WINDOWS) {
            String pth = nue.getPath();
            // check that path is not "/C:" or "/"
            if ((pth.length() == 3 && pth.endsWith(":")) ||
                (pth.length() == 1 && pth.endsWith("/"))) {
                return null;
            }
        }
        assert nue.isAbsolute() : nue;
        assert u.toString().startsWith(nue.toString()) : "not a parent: " + nue + " of " + u;
        return nue;
    }
    private static final boolean WINDOWS = BaseUtilities.isWindows();
    
}
