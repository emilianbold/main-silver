/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.options;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.php.api.util.FileUtils;
import org.openide.util.WeakListeners;

/**
 * @author Tomas Mysik
 */
public final class OptionsUtils {
    private static final AtomicBoolean INITED = new AtomicBoolean(false);

    private static final PreferenceChangeListener PREFERENCES_TRACKER = new PreferenceChangeListener() {
        public void preferenceChange(PreferenceChangeEvent evt) {
            String settingName = evt == null ? null : evt.getKey();
            if (settingName == null || CodeCompletionPanel.PHP_CODE_COMPLETION_TYPE.equals(settingName)) {
                codeCompletionType = CodeCompletionPanel.CodeCompletionType.resolve(preferences.get(CodeCompletionPanel.PHP_CODE_COMPLETION_TYPE, null));
            }
            if (settingName == null || CodeCompletionPanel.PHP_CODE_COMPLETION_PROVIDE_STATIC_METHODS.equals(settingName)) {
                provideStaticMethods = preferences.getBoolean(
                        CodeCompletionPanel.PHP_CODE_COMPLETION_PROVIDE_STATIC_METHODS,
                        CodeCompletionPanel.PHP_CODE_COMPLETION_PROVIDE_STATIC_METHODS_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.PHP_CODE_COMPLETION_PROVIDE_NON_STATIC_METHODS.equals(settingName)) {
                provideNonStaticMethods = preferences.getBoolean(
                        CodeCompletionPanel.PHP_CODE_COMPLETION_PROVIDE_NON_STATIC_METHODS,
                        CodeCompletionPanel.PHP_CODE_COMPLETION_PROVIDE_NON_STATIC_METHODS_DEFAULT);
            }
        }
    };

    private static Preferences preferences;
    private static CodeCompletionPanel.CodeCompletionType codeCompletionType = null;;
    private static Boolean provideStaticMethods = null;
    private static Boolean provideNonStaticMethods = null;

    private OptionsUtils() {
    }

    /**
     * Code completion type for PHP 5.3.
     */
    public static CodeCompletionPanel.CodeCompletionType getCodeCompletionType() {
        lazyInit();
        assert codeCompletionType != null;
        return codeCompletionType;
    }

    /**
     * Provide also static methods after "->"
     */
    public static boolean provideCodeCompletionWithStaticMethods() {
        lazyInit();
        assert provideStaticMethods != null;
        return provideStaticMethods;
    }

    /**
     * Provide also non-static methods after "::"
     */
    public static boolean provideCodeCompletionWithNonStaticMethods() {
        lazyInit();
        assert provideNonStaticMethods != null;
        return provideNonStaticMethods;
    }

    private static void lazyInit() {
        if (INITED.compareAndSet(false, true)) {
            preferences = MimeLookup.getLookup(FileUtils.PHP_MIME_TYPE).lookup(Preferences.class);
            preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, PREFERENCES_TRACKER, preferences));
            PREFERENCES_TRACKER.preferenceChange(null);
        }
    }
}
