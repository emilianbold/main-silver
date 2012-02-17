/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.indexing;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.parsing.spi.indexing.SuspendStatus;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
class SourcePrefetcher implements Iterator<CompileTuple> {
    
    private static final Logger LOG = Logger.getLogger(SourcePrefetcher.class.getName());
    private static final int DEFAULT_PROC_COUNT = 2;
    private static final int DEFAULT_BUFFER_SIZE = 1024*1024;
    private static final int MIN_PROC = 4;
    private static final int MIN_FILES = 10;    //Trivial problem size
    private static final boolean PREFETCH_DISABLED = Boolean.getBoolean("SourcePrefetcher.disabled");   //NOI18N
    private static final int PROC_COUNT = Integer.getInteger("SourcePrefetcher.proc.count", DEFAULT_PROC_COUNT);    //NOI18N
    private static final int BUFFER_SIZE = Integer.getInteger("SourcePrefetcher.beffer.size", DEFAULT_BUFFER_SIZE); //NOI18N
    
    /*test*/ static Boolean TEST_DO_PREFETCH;
    
    private final Iterator<? extends CompileTuple> iterator;
    //@NotThreadSafe
    private boolean active;

    private SourcePrefetcher(
            @NonNull final Iterator<? extends CompileTuple> iterator) {
        assert iterator != null;
        this.iterator = iterator;
    }
    
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }
    
    @Override
    @CheckForNull
    public CompileTuple next() {
        if (active) {
            throw new IllegalStateException("Call remove to free resources");   //NOI18N
        }
        final CompileTuple res = iterator.next();
        active = true;
        return res;
    }

    @Override
    public void remove() {
        if (!active) {
            throw new IllegalStateException("Call next before remove");   //NOI18N
        }
        try {
            iterator.remove();
        } finally {
            active = false;
        }
    }
    
    
    static SourcePrefetcher create(
            @NonNull final Collection<? extends CompileTuple> files,
            @NonNull final SuspendStatus suspendStatus) {
        return new SourcePrefetcher(getIterator(files, suspendStatus));
    }
    
    private static abstract class SuspendableIterator implements Iterator<CompileTuple> {
        
        private final SuspendStatus suspendStatus;
        
        protected SuspendableIterator(@NonNull final SuspendStatus suspendStatus) {
            assert suspendStatus != null;
            this.suspendStatus = suspendStatus;
        }
        
        protected final void safePark() {
            try {
                suspendStatus.parkWhileSuspended();
            } catch (InterruptedException ex) {
                //NOP - safe to ignore
            }
        }
    }
    
    private static final class NopRemoveItDecorator extends SuspendableIterator {
        
        private final Iterator<? extends CompileTuple> delegate;
        
        private NopRemoveItDecorator(
                @NonNull final Iterator<? extends CompileTuple> delegate,
                @NonNull final SuspendStatus suspendStatus) {
            super(suspendStatus);
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public CompileTuple next() {
            return delegate.next();
        }

        @Override
        public void remove() {
            //NOP
        }
        
    }
    
    private static final class ConcurrentIterator extends SuspendableIterator {
        
        private static final RequestProcessor RP = new RequestProcessor(
                SourcePrefetcher.class.getName(),
                PROC_COUNT,
                false,
                false);

        private final CompletionService<CompileTuple> cs;
        private final Semaphore sem;
        //@NotThreadSafe
        private int count;
        //@NotThreadSafe
        private CompileTuple active;
        

        private ConcurrentIterator(
                @NonNull final Iterable<? extends CompileTuple> files,
                @NonNull final SuspendStatus suspendStatus) {
            super(suspendStatus);
            this.cs = new ExecutorCompletionService<CompileTuple>(RP);
            this.sem = new Semaphore(BUFFER_SIZE);
            
            for (final CompileTuple ct : files) {
                cs.submit(new Callable<CompileTuple>() {
                    @Override
                    public CompileTuple call() throws Exception {
                        safePark();
                        final int len = ct.jfo.prefetch();
                        if (LOG.isLoggable(Level.FINEST) && 
                            (sem.availablePermits() - len) < 0) {
                            LOG.finest("Buffer full");  //NOI18N
                        }
                        sem.acquire(len);
                        return ct;
                    }
                });
                count++;
            }
        }
        
        @Override
        public boolean hasNext() {
            return count > 0;
        }

        @Override
        public CompileTuple next() {
            if (active != null) {
                throw new IllegalStateException("Call remove to free resources");   //NOI18N
            }
            if (!hasNext()) {
                throw new IllegalStateException("No more tuples."); //NOI18N
            }
            safePark();
            try {
                active = cs.take().get();
                return active;
            } catch (InterruptedException ex) {
                return null;
            } catch (ExecutionException ex) {
                return null;
            } finally {
                count--;
            }
        }
        
        @Override
        public void remove() {
            if (active == null) {
                throw new IllegalStateException("Call next before remove");   //NOI18N
            }
            try {
                final int len = active.jfo.dispose();
                sem.release(len);
            } finally {
                active = null;
            }
        }
        
    }
    
    private static Iterator<? extends CompileTuple> getIterator(
            @NonNull final Collection<? extends CompileTuple> files,
            @NonNull final SuspendStatus suspendStatus) {
        final int procCount = Runtime.getRuntime().availableProcessors();
        final int probSize = files.size();
        LOG.log(
            Level.FINER,
            "Proc Count: {0} File count: {1} Prefetch disabled: {2}",  //NOI18N
            new Object[]{
                procCount,
                probSize,
                PREFETCH_DISABLED}
        );
        final boolean supportsPar = procCount >= MIN_PROC && probSize > MIN_FILES;
        final boolean doPrefetch = TEST_DO_PREFETCH != null?
                TEST_DO_PREFETCH:
                supportsPar && !PREFETCH_DISABLED;
        if (doPrefetch) {
            LOG.log(
                Level.FINE,
                "Using concurrent iterator, {0} workers",    //NOI18N
                PROC_COUNT);
            return new ConcurrentIterator(files, suspendStatus);
        } else {
            LOG.fine("Using sequential iterator");    //NOI18N
            return new NopRemoveItDecorator(files.iterator(), suspendStatus);
        }
    }
    
}
