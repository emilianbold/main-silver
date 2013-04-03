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
package org.netbeans.modules.css.prep.util;

import org.netbeans.api.options.OptionsDisplayer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Parameters;

// XXX copied & adjusted from PHP
public final class UiUtils {

    public static final String OPTIONS_CATEGORY = "Advanced"; // NOI18N
    public static final String OPTIONS_SUBCATEGORY = "CssPreprocessors"; // NOI18N
    public static final String OPTIONS_PATH = OPTIONS_CATEGORY + "/" + OPTIONS_SUBCATEGORY; // NOI18N


    private UiUtils() {
    }

    /**
     * Display Options dialog with Miscellaneous &lt; CSS Preprocessors panel preselected.
     */
    public static void showOptions() {
        OptionsDisplayer.getDefault().open(OPTIONS_PATH);
    }

    /**
     * Display a dialog with the message and then open IDE options.
     * @param message message to display before IDE options are opened
     * @param optionsSubcategory IDE options subcategory to open (suitable e.g. for frameworks)
     * @see #invalidScriptProvided(String)
     */
    public static void invalidScriptProvided(String message) {
        Parameters.notNull("message", message);

        informAndOpenOptions(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
    }

    private static void informAndOpenOptions(NotifyDescriptor descriptor) {
        assert descriptor != null;

        DialogDisplayer.getDefault().notify(descriptor);
        showOptions();
    }

}
