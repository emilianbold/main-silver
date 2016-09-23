/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.preprocessorbridge.spi;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * The Compile On Save performer.
 * @since 1.41
 * @author Tomas Zezula
 */
public interface CompileOnSaveAction {

    /**
     * Performs the Compile On Save operation.
     * @param ctx the context for Compile On Save operation
     * @return true in case of success, false in case of failure, null in case of no changes.
     * @throws IOException 
     */
    Boolean performAction (@NonNull final Context ctx) throws IOException;
    /**
     * Returns true when this action is enabled.
     * The first enabled {@link CompileOnSaveAction} is used for performing the Compile On Save operation.
     * @return true when enabled
     */
    boolean isEnabled();
    /**
     * Returns true when resources should be synchronized.
     * @return true when resources should be copied
     */
    boolean isUpdateResources();
    /**
     * Returns true when classes should be synchronized.
     * @return true when classes should be copied
     */
    boolean isUpdateClasses();
    /**
     * Adds {@link ChangeListener}.
     * @param l the listener to be added
     */
    void addChangeListener(@NonNull ChangeListener l);
    /**
     * Removes {@link ChangeListener}.
     * @param l the listener to be removed
     */
    void removeChangeListener(@NonNull ChangeListener l);

    /**
     * Compile On Save operation.
     */
    enum Operation {
        /**
         * Clean.
         */
        CLEAN,
        /**
         * Partial update.
         */
        UPDATE,
        /**
         * Full synchronization.
         */
        SYNC
    }

    /**
     * Context of the Compile On Save Operation.
     */
    final class Context {
        private final Operation operation;
        private final URL srcRoot;
        private final boolean isCopyResources;
        private final boolean isKeepResourcesUpToDate;
        private final File cacheRoot;
        private final Iterable<? extends File> updated;
        private final Iterable<? extends File> deleted;
        private final Object owner;
        private final Consumer<Iterable<File>> firer;
        
        private Context(
                @NonNull final Operation operation,
                @NonNull final URL srcRoot,
                final boolean isCopyResources,
                final boolean isKeepResourcesUpToDate,
                @NullAllowed final File cacheRoot,
                @NullAllowed final Iterable<? extends File> updated,
                @NullAllowed final Iterable<? extends File> deleted,
                @NullAllowed final Object owner,
                @NullAllowed final Consumer<Iterable<File>> firer) {
            this.operation = operation;
            this.srcRoot = srcRoot;
            this.isCopyResources = isCopyResources;
            this.isKeepResourcesUpToDate = isKeepResourcesUpToDate;
            this.cacheRoot = cacheRoot;
            this.updated = updated;
            this.deleted = deleted;
            this.owner = owner;
            this.firer = firer;
        }
        
        /**
         * Returns the kind of the Compile On Save operation.
         * @return the {@link Operation}
         */
        @NonNull
        public Operation getOperation() {
            return operation;
        }
        
        /**
         * Returns the changed files.
         * The operation is valid only for {@link Operation#UPDATE}.
         * @return the changed files
         */
        @NonNull
        public Iterable<? extends File> getUpdated() {
            if (operation != Operation.UPDATE) {
                throw new IllegalStateException();
            }
            return updated;
        }
        
        /**
         * Returns the deleted files.
         * The operation is valid only for {@link Operation#UPDATE}.
         * @return the deleted files
         */
        @NonNull
        public Iterable<? extends File> getDeleted() {
            if (operation != Operation.UPDATE) {
                throw new IllegalStateException();
            }
            return deleted;
        }
        
        /**
         * Returns true for resources.
         * The operation is valid only for {@link Operation#UPDATE} and {@link Operation#SYNC}.
         * @return true for resources
         */
        public boolean isCopyResources() {
            if (operation == Operation.CLEAN) {
                throw new IllegalStateException();
            }
            return isCopyResources;
        }
        
        /**
         * Returns true if resources should be updated on change.
         * The operation is valid only for {@link Operation#SYNC}.
         * @return true for update on change
         */
        public boolean isKeepResourcesUpToDate() {
            if (operation != Operation.SYNC) {
                throw new IllegalStateException();
            }
            return isKeepResourcesUpToDate;
        }
        
        /**
         * Returns the source root.
         * @return the source root
         */
        @NonNull
        public URL getSourceRoot() {
            return srcRoot;
        }
        
        /**
         * Returns the cache root.
         * The operation is valid only for {@link Operation#UPDATE}.
         * @return the cache root.
         */
        @NonNull
        public File getCacheRoot() {
            if (operation != Operation.UPDATE) {
                throw new IllegalStateException();
            }
            return cacheRoot;
        }
        
        /**
         * Returns the target folder.
         * @return the target folder.
         */
        @CheckForNull
        public File getTarget() {
            return getTarget(srcRoot);
        }
        
        /**
         * Returns the root owner.
         * The operation is valid only for {@link Operation#SYNC}.
         * @return the owner
         */
        @NonNull
        public Object getOwner() {
            if (operation != Operation.SYNC) {
                throw new IllegalStateException();
            }
            return owner;
        }
        
        /**
         * Fires updated files.
         * @param updatedFiles the updated files
         */
        public void filesUpdated(@NonNull final Iterable<File> updatedFiles) {
            if (firer != null) {
                firer.accept(updatedFiles);
            }
        }
        
        /**
         * Creates context for clean operation.
         * @param srcRoot the root
         * @return the {@link Context} for clean operation
         */
        @NonNull
        public static Context clean(@NonNull final URL srcRoot) {
            Parameters.notNull("srcRoot", srcRoot); //NOI18N
            return new Context(Operation.CLEAN, srcRoot, false, false, null, null, null, null, null);
        }
        
        /**
         * Creates context for update operation.
         * @param srcRoot the root
         * @param isCopyResources true for resource update
         * @param cacheRoot the cache root
         * @param updated the changed files
         * @param deleted the deleted files
         * @param firer the fire callback
         * @return the {@link Context} for update operation
         */
        @NonNull
        public static Context update(
                @NonNull final URL srcRoot,
                final boolean isCopyResources,
                @NonNull final File cacheRoot,
                @NonNull final Iterable<? extends File> updated,
                @NonNull final Iterable<? extends File> deleted,
                @NullAllowed final Consumer<Iterable<File>> firer) {
            Parameters.notNull("srcRoot", srcRoot); //NOI18N
            Parameters.notNull("cacheRoot", cacheRoot); //NOI18N
            Parameters.notNull("updated", updated); //NOI18N
            Parameters.notNull("deleted", deleted); //NOI18N            
            return new Context(
                    Operation.UPDATE, srcRoot, isCopyResources, false, cacheRoot, updated, deleted, null, firer);
        }
        
        /**
         * Creates context for sync operation.
         * @param srcRoot the root
         * @param isCopyResources should copy resources
         * @param isKeepResourcesUpToDate should synchronize the resources on change
         * @param owner the source root owner
         * @return the {@link Context} for sync operation
         */
        @NonNull
        public static Context sync(
                @NonNull final URL srcRoot,
                final boolean isCopyResources,
                final boolean isKeepResourcesUpToDate,
                @NonNull final Object owner) {
            Parameters.notNull("srcRoot", srcRoot); //NOI18N
            Parameters.notNull("owner", owner); //NOI18N
            return new Context(
                    Operation.SYNC, srcRoot, isCopyResources, isKeepResourcesUpToDate, null, null, null, owner, null);
        }
        
        /**
         * Returns the target folder for source root.
         * @param srcRoot the source root to return target folder for
         * @return the target folder
         */
        @CheckForNull
        public static File getTarget(@NonNull URL srcRoot) {
            BinaryForSourceQuery.Result binaryRoots = BinaryForSourceQuery.findBinaryRoots(srcRoot);
        
            File result = null;

            for (URL u : binaryRoots.getRoots()) {
                assert u != null : "Null in BinaryForSourceQuery.Result.roots: " + binaryRoots; //NOI18N
                if (u == null) {
                    continue;
                }
                File f = FileUtil.archiveOrDirForURL(u);

                try {
                    if (FileUtil.isArchiveFile(BaseUtilities.toURI(f).toURL())) {
                        continue;
                    }

                    if (f != null && result != null) {
                        Logger.getLogger(CompileOnSaveAction.class.getName()).log(
                                Level.WARNING,
                                "More than one binary directory for root: {0}",
                                srcRoot.toExternalForm());
                        return null;
                    }

                    result = f;
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            return result;
        }
    }

    /**
     * The provider of the {@link CompileOnSaveAction}.
     * The instances of the {@link Provider} should be registered in the
     * global {@link Lookup}.
     */
    interface Provider {
        /**
         * Finds the Compile On Save performer for given source root.
         * @param root the root to find the Compile On Save performer for.
         * @return the {@link CompileOnSaveAction} or null when the root is not recognized.
         */
        CompileOnSaveAction forRoot(@NonNull final URL root);
    }
}
