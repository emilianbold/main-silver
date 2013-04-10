/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.openide.windows;

/**
 * An object that refers to a fold in output window. It can be used to finish
 * the fold, or to create a nested folds.
 *
 * @author jhavlin
 */
public final class FoldHandle {

    private final Definition definition;

    FoldHandle(Definition definition) {
        this.definition = definition;
    }

    /**
     * Finish the fold at the current last line in the output window.
     *
     * @throws IllegalStateException if parent fold has been already finished,
     * or if there is an unfinished child fold.
     */
    public void finish() {
        definition.finish();
    }

    /**
     * Start a nested fold at the current last line in output window.
     *
     * @param expanded True to expand the new fold, false to collapse it.
     * @return Handle for the newly created fold.
     * @throws IllegalStateException if the fold has been already finished.
     */
    public FoldHandle startFold(boolean expanded) {
        return new FoldHandle(definition.startFold(expanded));
    }

    /**
     * Set state of the fold.
     *
     * @param expanded True to expand the fold, false to collapse it.
     */
    public void setExpanded(boolean expanded) {
        definition.setExpanded(expanded);
    }

    /**
     * An SPI for creating custom FoldHandle implementations.
     */
    public static abstract class Definition {

        /**
         * Finish the fold at the current last line. Ensure that nested folds
         * are finished correctly.
         *
         * @throws IllegalStateException if parent fold has been already
         * finished, or if there is an unfinished nested fold.
         */
        public abstract void finish();

        /**
         * Start a new fold at the current last line. Ensure that the parent
         * fold hasn't been finished yet.
         *
         * @param expand If false, the fold will be collapsed by default,
         * otherwise it will be expanded.
         * @return Definition of handle for the newly created fold.
         * @throws IllegalStateException if the fold has been already finished,
         * or if the last nested fold hasn't been finished yet.
         */
        public abstract FoldHandle.Definition startFold(boolean expanded);

        /**
         * Set state of the fold.
         *
         * @param expanded True to expand the fold, false to collapse it.
         */
        public abstract void setExpanded(boolean expanded);
    }
}
