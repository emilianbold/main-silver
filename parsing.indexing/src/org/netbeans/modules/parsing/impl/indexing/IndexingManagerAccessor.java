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
package org.netbeans.modules.parsing.impl.indexing;

import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public abstract class IndexingManagerAccessor {

    private static volatile IndexingManagerAccessor instance;

    /*test*/ public static volatile Boolean requiresReleaseOfCompletionLock = null;

    public static void setInstance(final @NonNull IndexingManagerAccessor _instance) {
        Parameters.notNull("_instance", _instance);
        instance = _instance;
    }

    public static synchronized IndexingManagerAccessor getInstance() {
        if (instance == null) {
            try {
                Class.forName(IndexingManager.class.getName(),true,IndexingManagerAccessor.class.getClassLoader());
                assert instance != null;
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return instance;
    }


    protected abstract boolean isCalledFromRefreshIndexAndWait(final @NonNull IndexingManager manager);

    public final boolean isCalledFromRefreshIndexAndWait() {
        return isCalledFromRefreshIndexAndWait(IndexingManager.getDefault());
    }

    public final boolean requiresReleaseOfCompletionLock() {
        final Boolean result = requiresReleaseOfCompletionLock;
        return result != null ? result : SwingUtilities.isEventDispatchThread();
    }
    
    public boolean canReleaseCompletionLock() {
        return requiresReleaseOfCompletionLock()  &&
               isCalledFromRefreshIndexAndWait();
    }

    public final void releaseCompletionCondition() {
        releaseCompletionCondition.run();
    }
    
    private static volatile Runnable releaseCompletionCondition = new Runnable() { public void run() {}};
    
    public static void setReleaseCompletion(Runnable r) {
        assert r != null && releaseCompletionCondition.getClass().getName().startsWith(IndexingManagerAccessor.class.getName());
        
        releaseCompletionCondition = r;
    }
}
