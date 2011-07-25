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
package org.netbeans.api.whitelist;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.whitelist.project.WhiteListCategoryPanel;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation.WhiteListImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Query for finding white lists used to emit errors for usages of non allowed
 * types or methods. The project types supporting a runtime with class usage
 * restriction implement the {@link WhiteListQueryImplementation} to provide a
 * white list of allowed types (methods).
 * @author Tomas Zezula
 */
public final class WhiteListQuery {

    private static final Lookup.Result<? extends WhiteListQueryImplementation> queries =
        Lookup.getDefault().lookupResult(WhiteListQueryImplementation.class);

    private WhiteListQuery(){}

    /**
     * Returns a white list for given file.
     * @param file the file for which the white list should be obtained.
     * @return the {@link WhiteList} for given file or null if no white list
     * is associated with given file.
     */
    @CheckForNull
    public static WhiteList getWhiteList(@NonNull final FileObject file) {
        Parameters.notNull("file", file);   //NOI18N
        for (WhiteListQueryImplementation query : queries.allInstances()) {
            final WhiteListImplementation whiteListImpl = query.getWhiteList(file);
            if (whiteListImpl != null) {
                return new WhiteList(whiteListImpl);
            }
        }
        return null;
    }

    /**
     * At the moment assumption is that if a project want to automatically enable 
     * some whitelist then they likely know whitelistId.
     */
    public static void enableWhiteListInProject(@NonNull Project p, @NonNull String whiteListId, boolean enable) {
        WhiteListCategoryPanel.enableWhiteListInProject(p, whiteListId, enable);
    }
    
    /**
     * The white list used to emit errors for usages of non allowed
     * types or methods.
     */
    public static final class WhiteList {

        private final WhiteListImplementation impl;

        private WhiteList(
            @NonNull final WhiteListImplementation impl) {
            Parameters.notNull("impl", impl);   //NOI18N
            this.impl = impl;
        }

        /**
         * Checks if given method (type) can be invoked (accessed).
         * @param element to check
         * @return true if the element is allowed
         */
        public final boolean canInvoke(
            @NonNull final ElementHandle<?> element) {
            Parameters.notNull("element", element); //NOI18N;
            return impl.check(element, Operation.USAGE).isAllowed();
        }

        /**
         * Checks if given method (type) can be overridden (subclassed).
         * @param element to check
         * @return true if the element is allowed
         */
        public final boolean canOverride(
             @NonNull final ElementHandle<?> element) {
//            Parameters.notNull("element", element); //NOI18N;
//            return impl.canOverride(element);
            return true;
        }
    }

    public enum Operation {
        USAGE
    }

    public static final class Result {
        private final boolean allowed;
        private final String violatedRuleName;
        private final String violatedRuleDescription;

        //Todo: Make private
        public Result(boolean allowed, String violatedRuleName, String violatedRuleDescription) {
            this.allowed = allowed;
            this.violatedRuleName = violatedRuleName;
            this.violatedRuleDescription = violatedRuleDescription;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getViolatedRuleDescription() {
            return violatedRuleDescription;
        }

        public String getViolatedRuleName() {
            return violatedRuleName;
        }
    }
}
