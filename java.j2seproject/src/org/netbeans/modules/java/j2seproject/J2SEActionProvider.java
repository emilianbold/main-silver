/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.java.j2seproject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.BaseActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.util.Lookup;

/** Action provider of the J2SE project. This is the place where to do
 * strange things to J2SE actions. E.g. compile-single.
 */
class J2SEActionProvider extends BaseActionProvider {

    private static final Logger LOG = Logger.getLogger(J2SEActionProvider.class.getName());

    // Commands available from J2SE project
    private static final String[] supportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE,
        JavaProjectConstants.COMMAND_JAVADOC,
        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        SingleMethod.COMMAND_RUN_SINGLE_METHOD,
        SingleMethod.COMMAND_DEBUG_SINGLE_METHOD,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
        COMMAND_DEBUG_STEP_INTO,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
    };


    private static final String[] platformSensitiveActions = {
        COMMAND_BUILD,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE,
        JavaProjectConstants.COMMAND_JAVADOC,
        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        SingleMethod.COMMAND_RUN_SINGLE_METHOD,
        SingleMethod.COMMAND_DEBUG_SINGLE_METHOD,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
        COMMAND_DEBUG_STEP_INTO,
    };

    private static final String[] actionsDisabledForQuickRun = {
        COMMAND_COMPILE_SINGLE,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
    };

    /** Map from commands to ant targets */
    private Map<String,String[]> commands;

    /**Set of commands which are affected by background scanning*/
    private Set<String> bkgScanSensitiveActions;

    /**Set of commands which need java model up to date*/
    private Set<String> needJavaModelActions;

    public J2SEActionProvider(J2SEProject project, UpdateHelper updateHelper) {
        super(project, updateHelper, project.evaluator(), project.getSourceRoots(),
                project.getTestSourceRoots(), project.getAntProjectHelper(),
                new BaseActionProvider.CallbackImpl(project.getClassPathProvider()));
        commands = new HashMap<String,String[]>();
        // treated specially: COMMAND_{,RE}BUILD
        commands.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        commands.put(COMMAND_COMPILE_SINGLE, new String[] {"compile-single"}); // NOI18N
        commands.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
        commands.put(COMMAND_RUN_SINGLE, new String[] {"run-single"}); // NOI18N
        commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        commands.put(COMMAND_DEBUG_SINGLE, new String[] {"debug-single"}); // NOI18N
        commands.put(JavaProjectConstants.COMMAND_JAVADOC, new String[] {"javadoc"}); // NOI18N
        commands.put(COMMAND_TEST, new String[] {"test"}); // NOI18N
        commands.put(COMMAND_TEST_SINGLE, new String[] {"test-single"}); // NOI18N
        commands.put(COMMAND_DEBUG_TEST_SINGLE, new String[] {"debug-test"}); // NOI18N
        commands.put(JavaProjectConstants.COMMAND_DEBUG_FIX, new String[] {"debug-fix"}); // NOI18N
        commands.put(COMMAND_DEBUG_STEP_INTO, new String[] {"debug-stepinto"}); // NOI18N

        this.bkgScanSensitiveActions = new HashSet<String>(Arrays.asList(
            COMMAND_RUN,
            COMMAND_RUN_SINGLE,
            COMMAND_DEBUG,
            COMMAND_DEBUG_SINGLE,
            COMMAND_DEBUG_STEP_INTO
        ));

        this.needJavaModelActions = new HashSet<String>(Arrays.asList(
            JavaProjectConstants.COMMAND_DEBUG_FIX
        ));
    }

    @Override
    protected String[] getPlatformSensitiveActions() {
        return platformSensitiveActions;
    }

    @Override
    protected String[] getActionsDisabledForQuickRun() {
        return actionsDisabledForQuickRun;
    }

    @Override
    public Map<String, String[]> getCommands() {
        return commands;
    }

    @Override
    protected Set<String> getScanSensitiveActions() {
        return bkgScanSensitiveActions;
    }

    @Override
    protected Set<String> getJavaModelActions() {
        return needJavaModelActions;
    }

    @Override
    protected boolean isCompileOnSaveEnabled() {
        return J2SEProjectUtil.isCompileOnSaveEnabled((J2SEProject)getProject());
    }

    @Override
    public String[] getSupportedActions() {
        return supportedActions;
    }

    @Override
    public String[] getTargetNames(String command, Lookup context, Properties p, boolean doJavaChecks) throws IllegalArgumentException {
        String names[] = super.getTargetNames(command, context, p, doJavaChecks);
        J2SEConfigurationProvider.Config c = context.lookup(J2SEConfigurationProvider.Config.class);
        if (c != null) {
            String config;
            if (c.name != null) {
                config = c.name;
            } else {
                // Invalid but overrides any valid setting in config.properties.
                config = "";
            }
            p.setProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG, config);
        }
        return names;
    }

}
