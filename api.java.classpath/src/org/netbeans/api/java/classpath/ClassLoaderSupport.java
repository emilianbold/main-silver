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

package org.netbeans.api.java.classpath;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.spi.java.classpath.ClassLoaderFactory;

import org.openide.filesystems.*;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/** Classloader for the filesystem pool. Attaches itself as a listener to
 * each file a class has been loaded from. If such a file is deleted, modified
 * or renamed clears the global variable that holds "current" classloader, so
 * on next request for current one new is created.
 *
 * @author Jaroslav Tulach
 * @author Tomas Zezula
 */
class ClassLoaderSupport extends ClassLoader implements FileChangeListener, PropertyChangeListener {
    
    static ClassLoader create(ClassPath cp) {
        return new ClassLoaderSupport(cp, ClassLoader.getSystemClassLoader());
    }

    private static final ClassLoader NULL = new NullClassLoader();
    private static final ClassLoaderFactory DEFAULT = new DefaultClassLoaderFactory();

    /** change listener */
    private org.openide.filesystems.FileChangeListener listener;

    /** PropertyChangeListener */
    private java.beans.PropertyChangeListener propListener;

    private final Object lock = new Object();
    private final Map<FileObject,Boolean> emittedFileObjects = new HashMap<FileObject,Boolean>();
    private boolean detachedFromCp;

    /** contains AllPermission */
    private static java.security.PermissionCollection allPermission;

    /**
     * The ClassPath to load classes from.
     */
    private final ClassPath  classPath;
    private final ClassLoader delegate;

    /** Constructor that attaches itself to the filesystem pool.
    */
    @SuppressWarnings("LeakingThisInConstructor")
    private ClassLoaderSupport (final ClassPath cp, final ClassLoader parentClassLoader) {
        super(parentClassLoader);
        this.classPath = cp;
        ClassLoaderFactory factory = Lookup.getDefault().lookup(ClassLoaderFactory.class);
        if (factory == null) {
            factory = DEFAULT;
        }
        this.delegate = factory.createClassLoader(NULL, this.classPath.getRoots());
        if (delegate == null) {
            throw new IllegalStateException(String.format(
                "ClassLoaderFactory %s : %s returned a null ClassLoader.",  //NOI18N
                factory,
                factory.getClass()));
        }
        listener = FileUtil.weakFileChangeListener(this, null);
        propListener = WeakListeners.propertyChange (this, null);
        cp.addPropertyChangeListener(propListener);
    }

    /**
     * Tries to locate the .class file on the ClassPath
     * @param name
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    protected Class findClass (String name) throws ClassNotFoundException {
        Class c = delegate.loadClass (name);
        if (c != null) {
            org.openide.filesystems.FileObject fo;
            String resName = name.replace('.', '/') + ".class"; // NOI18N
            fo = classPath.findResource(resName);
            if (fo != null) {
                // if the file is from the file system pool,
                // register to catch its changes
                addFileChangeListener(fo);
            }
        }
        return c;
    }
    
    /**
     * Tries to locate the resource on the ClassPath
     * @param name
     * @return URL of the resource
     */
    @Override
    public URL findResource (String name) {
        URL url = delegate.getResource (name);
        if (url != null) {
            FileObject fo = classPath.findResource(name);
            if (fo != null) {
                // if the file is from the file system pool,
                // register to catch its changes
                addFileChangeListener(fo);
            }
        }
        return url;
    }

    /** Tests whether this object is current loader and if so,
    * clears the loader.
    * @param fo file object that initiated the action
    */
    private void test (org.openide.filesystems.FileObject fo) {
        classPath.resetClassLoader(this);
        removeAllListeners();   //Detached from CP no need to listen
    }

    /** Resets the loader, removes it from listneing on all known objects.
    */
    private void reset () {
        classPath.resetClassLoader(this);
        removeAllListeners();   ////Detached from CP no need to listen
    }

    /** If this object is not current classloader, removes it from
    * listening on given file object.
    */
    private void testRemove (org.openide.filesystems.FileObject fo) {
        removeFileChangeListener(fo);
    }

    /** Fired when a new folder has been created. This action can only be
    * listened in folders containing the created file up to the root of
    * file system.
    *
    * @param fe the event describing context where action has taken place
    */
    @Override
    public void fileFolderCreated (org.openide.filesystems.FileEvent fe) {
        testRemove (fe.getFile ());
    }

    /** Fired when a new file has been created. This action can only be
    * listened in folders containing the created file up to the root of
    * file system.
    *
    * @param fe the event describing context where action has taken place
    */
    @Override
    public void fileDataCreated (org.openide.filesystems.FileEvent fe) {
        testRemove (fe.getFile ());
    }

    /** Fired when a file has been changed.
    * @param fe the event describing context where action has taken place
    */
    @Override
    public void fileChanged (org.openide.filesystems.FileEvent fe) {
        test (fe.getFile ());
    }

    /** Fired when a file has been deleted.
    * @param fe the event describing context where action has taken place
    */
    @Override
    public void fileDeleted (org.openide.filesystems.FileEvent fe) {
        test (fe.getFile ());
    }

    /** Fired when a file has been renamed.
    * @param fe the event describing context where action has taken place
    *           and the original name and extension.
    */
    @Override
    public void fileRenamed (org.openide.filesystems.FileRenameEvent fe) {
        test (fe.getFile ());
    }

    /** Fired when a file attribute has been changed.
    * @param fe the event describing context where action has taken place,
    *           the name of attribute and old and new value.
    */
    @Override
    public void fileAttributeChanged (org.openide.filesystems.FileAttributeEvent fe) {
        testRemove (fe.getFile ());
    }
    
    /** Getter for allPermissions */
    static synchronized java.security.PermissionCollection getAllPermissions() {
        if (allPermission == null) {
            allPermission = new java.security.Permissions();
            allPermission.add(new java.security.AllPermission());
        }
        return allPermission;
    }

    /**
     * This method gets called when a bound property is changed.
     * @param evt A PropertyChangeEvent object describing the event source 
     *  	and the property that has changed.
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName()))
            reset();
    }

    private void addFileChangeListener(final FileObject fo) {
        boolean add;
        synchronized(lock) {
            if (detachedFromCp) {
                return;
            }
            add = emittedFileObjects.put(fo,Boolean.FALSE) == null;
        }
        if (add) {
            fo.addFileChangeListener (listener);
            synchronized(lock) {
                if (!detachedFromCp) {
                    assert emittedFileObjects.get(fo) == Boolean.FALSE;
                    emittedFileObjects.put(fo,Boolean.TRUE);
                } else {
                    emittedFileObjects.remove(fo);
                    add = false;
                }
            }
            if (!add) {
                fo.removeFileChangeListener(listener);
            }
        }
    }

    private void removeFileChangeListener(final FileObject fo) {
        boolean remove;
        synchronized(lock) {
            remove = emittedFileObjects.remove(fo) == Boolean.TRUE;
        }
        if (remove) {
            fo.removeFileChangeListener(listener);
        }
    }

    private void removeAllListeners() {
        Map.Entry[] removeListenerFrom;
        synchronized(lock){
            detachedFromCp = true;  //No need to add more listeners
            if (emittedFileObjects.isEmpty()) {
                return;
            }
            removeListenerFrom = emittedFileObjects.entrySet().toArray(new Map.Entry[emittedFileObjects.size()]);
            emittedFileObjects.clear();
        }
        for (Map.Entry e : removeListenerFrom) {
            if (e.getValue() == Boolean.TRUE) {
                ((FileObject)e.getKey()).removeFileChangeListener(listener);
            }
        }
    }

    private static final class NullClassLoader extends ClassLoader {
        NullClassLoader() {
            super(null);
        }

        @Override
        protected Package getPackage(String name) {
            return null;
        }

        @Override
        protected Package[] getPackages() {
            return new Package[0];
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            return null;
        }

        @Override
        public URL getResource(String name) {
            return null;
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            return Collections.enumeration(Collections.<URL>emptySet());
        }
    }

    private static final class DefaultClassLoaderFactory implements ClassLoaderFactory {
        @Override
        @NonNull
        public ClassLoader createClassLoader(
            @NullAllowed final ClassLoader parentLoader,
            @NonNull FileObject... roots) {
            final URL[] urls = new URL[roots.length];
            for (int i=0; i< roots.length; i++) {
                urls[i] = roots[i].toURL();
            }
            return new URLClassLoader(urls, parentLoader);
        }
    }
}
