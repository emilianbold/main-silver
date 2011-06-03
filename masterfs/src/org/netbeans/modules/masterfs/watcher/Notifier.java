/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.masterfs.watcher;

import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * This internal SPI represents the interface between masterfs and
 * different implementations of filesystem watches on various systems.
 *
 * The SPI is kept very minimal, as the only necessary information is a queue
 * of modified folders, the filesystems code will evaluate the nature
 * of the change itself.
 * The SPI also doesn't distinguish between systems able of hierarchical
 * listening and systems without such a capability.
 * The implementation can report more events than registered, the infrastructure
 * should take care of filtering them.
 *
 * @author nenik
 */
public abstract class Notifier<KEY> {

    /**
     * Register a path for notifications. Optionally provide a key useful
     * for unregistering the path. The implementations that need to have every
     * path registered individually shall return a valid key, and shall
     * implement the {@link #removeWatch(java.lang.Object)} properly.
     *
     * @param path the path to register for notifications
     * @return a key useful for unregistering the path.
     * @throws IOException if the path can't be registered. For example if the
     * OS limit on the number of watched folders is reached. The exception
     * should be annotated with localized explanation.
     */
    public abstract KEY addWatch(String path) throws IOException;
    
    /**
     * Unregister a path. Implementations that listen recursively on the whole
     * filesystem may ignore this request. They shall also return
     * <code>null</code> from the {@link #addWatch(java.lang.String)} call.
     * 
     * @param key the key obtained during registration.
     * @throws IOException
     */
    public abstract void removeWatch(KEY key) throws IOException;

    /**
     *
     * @return absolute path of the changed folder or null in case
     * of overflow or any other reason to cause a full rescan
     * @throws IOException
     * @throws InterruptedException
     */
    public abstract String nextEvent() throws IOException, InterruptedException;
    
    /** Get ready for stop. Clean all resources, the system is about to
     * shutdown the VM. By default this is no-op operation.
     */
    protected void stop() throws IOException {
    }
    
    class KeyRef extends WeakReference<FileObject> {
        private final KEY key;
        private final int hash;

        public KeyRef(FileObject fo, KEY key, ReferenceQueue<FileObject> queue) {
            super(fo, queue);
            this.key = key;
            this.hash = fo.hashCode();
            if (key != null) {
                Watcher.LOG.log(Level.FINE, "Adding watch for {0}", key);
            }
        }

        @Override
        public FileObject get() {
            return super.get();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            try {
                KeyRef kr = (KeyRef)obj;
                FileObject mine = get();
                FileObject theirs = kr.get();
                if (mine == null) {
                    return theirs == null;
                } else {
                    return mine.equals(theirs);
                }
            } catch (ClassCastException ex) {
                return false;
            }
        }

        final void removeWatch() throws IOException {
            Watcher.LOG.log(Level.FINE, "Removing watch for {0}", key);
            Notifier.this.removeWatch(key);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    } // KeyRef
    
}
