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
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Vladimir Kvashin
 */
public class RemoteFileObjectFactory {

    private final ExecutionEnvironment env;
    private final RemoteFileSystem fileSystem;
    private final WeakCache<String, RemoteFileObjectBase> fileObjectsCache = new WeakCache<String, RemoteFileObjectBase>();
    private final Object lock = new Object();

    private final RequestProcessor.Task cleaningTask;
    private static RequestProcessor RP = new RequestProcessor("File objects cache dead entries cleanup", 1); //NOI18N
    private static final int CLEAN_INTERVAL = Integer.getInteger("rfs.cache.cleanup.interval", 10000); //NOI18N

    private int cacheRequests = 0;
    private int cacheHits = 0;

    public RemoteFileObjectFactory(RemoteFileSystem fileSystem) {
        this.fileSystem = fileSystem;
        this.env = fileSystem.getExecutionEnvironment();
        cleaningTask = RP.create(new Runnable() {
            public void run() {
                cleanDeadEntries();
            }
        });
        scheduleCleanDeadEntries();
    }

    private void scheduleCleanDeadEntries() {
        cleaningTask.schedule(CLEAN_INTERVAL);
    }

    private void cleanDeadEntries() {        
        boolean trace = RemoteLogger.getInstance().isLoggable(Level.FINEST);
        if (trace)         {
            int size;
            synchronized (lock) {
                size = fileObjectsCache.size();
            }
            RemoteLogger.getInstance().log(Level.FINEST, "Cleaning file objects dead entries for {0} ... {1} entries and {2}% ({3} of {4}) hits so far",
                    new Object[] {env, size, (cacheHits*100)/cacheRequests, cacheHits, cacheRequests});
        }

        fileObjectsCache.cleanDeadEntries();


        if (trace)         {
            int size;
            synchronized (lock) {
                size = fileObjectsCache.size();
            }
            RemoteLogger.getInstance().log(Level.FINEST, "Cleaning file objects dead entries for {0} ... {1} entries left", new Object[] {env, size});
        }

        synchronized (lock) {
            if (fileObjectsCache.size() > 0) {
                scheduleCleanDeadEntries();
            }
        }
    }

    public RemoteDirectory createRemoteDirectory(FileObject parent, String remotePath, File cacheFile) {
        cacheRequests++;
        synchronized (lock) {
            if (fileObjectsCache.size() == 0) {
                scheduleCleanDeadEntries(); // schedule on 1-st request
            }
            RemoteFileObjectBase fo = fileObjectsCache.get(remotePath);
            if (fo instanceof RemoteDirectory && fo.isValid() && fo.parent == parent && fo.cache.equals(cacheFile)) {
                cacheHits++;
                return (RemoteDirectory) fo;
            }
            fo = new RemoteDirectory(fileSystem, env, parent, remotePath, cacheFile);
            fileObjectsCache.put(remotePath, fo);
            return (RemoteDirectory) fo;
        }        
    }

    public RemotePlainFile createRemotePlainFile(RemoteDirectory parent, String remotePath, File cacheFile) {
        cacheRequests++;
        synchronized (lock) {
            if (fileObjectsCache.size() == 0) {
                scheduleCleanDeadEntries(); // schedule on 1-st request
            }
            RemoteFileObjectBase fo = fileObjectsCache.get(remotePath);
            if (fo instanceof RemotePlainFile && fo.isValid() && fo.parent == parent && fo.cache.equals(cacheFile)) {
                cacheHits++;
                return (RemotePlainFile) fo;
            }
            fo = new RemotePlainFile(fileSystem, env, parent, remotePath, cacheFile);
            fileObjectsCache.put(remotePath, fo);
            return (RemotePlainFile) fo;
        }
    }

//    /*package*/ int testGetCacheSize() {
//        return fileObjectsCache.size();
//    }
}
