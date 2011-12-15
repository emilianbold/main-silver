/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.profiler.j2ee.weblogic;

import org.netbeans.lib.profiler.common.AttachSettings;
import org.netbeans.lib.profiler.common.integration.IntegrationUtils;
import org.netbeans.modules.profiler.attach.spi.IntegrationProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Bachorik
 */
@NbBundle.Messages({
    "WebLogicIntegrationProvider_WebLogic9String=WebLogic 9+"
})
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.attach.spi.IntegrationProvider.class)
public class WebLogic9IntegrationProvider extends WebLogicIntegrationProvider {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public String getTitle() {
        return Bundle.WebLogicIntegrationProvider_WebLogic9String();
    }

    public boolean supportsDynamic() {
        return true;
    }

    protected int getAttachWizardPriority() {
        return 40;
    }

    protected IntegrationProvider.IntegrationHints getManualLocalDirectIntegrationStepsInstructions(String targetOS,
                                                                                                    AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints instructions = new IntegrationProvider.IntegrationHints();

        // Step 1
        instructions.addStep(Bundle.WebLogicIntegrationProvider_ManualDirectDynamicStep1Msg(
                                IntegrationUtils.getEnvVariableReference("BEA_HOME", targetOS), // NOI18N
                                IntegrationUtils.getDirectorySeparator(targetOS), "bin", // NOI18N
                                "base_domain", // NOI18N
                                Bundle.WebLogicIntegrationProvider_CopyFiles9Msg(
                                    getStartScriptExtension(targetOS))));

        // Step 2
        String wlSettings = IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_VENDOR", "Sun") // NOI18N
                            + "<br>" // NOI18N
                            + IntegrationUtils.getAssignEnvVariableValueString(
                                targetOS, 
                                "JAVA_HOME",  // NOI18N
                                Bundle.WebLogicIntegrationProvider_PathToJvmDirText(
                                    IntegrationUtils.getJavaPlatformName(getTargetJava())));

        final String cleanupText = Bundle.WebLogicIntegrationProvider_Wl9CleanupText(
                                        IntegrationUtils.getBatchExtensionString(targetOS));
        instructions.addStep(Bundle.WebLogicIntegrationProvider_ManualDirectStep2Msg(
                                getStartScriptExtension(targetOS),
                                IntegrationUtils.isWindowsPlatform(targetOS) ? 
                                    Bundle.WebLogicIntegrationProvider_WindowsAnchorText()
                                    : Bundle.WebLogicIntegrationProvider_Wl9AnchorText(), 
                                wlSettings,
                                IntegrationUtils.getAssignEnvVariableValueString(
                                    targetOS, 
                                    "JAVA_OPTIONS",
                                    IntegrationUtils.getProfilerAgentCommandLineArgs(
                                        targetOS,
                                        getTargetJava(),
                                        attachSettings.isRemote(),
                                        attachSettings.getPort())
                                        + " "
                                        + IntegrationUtils.getEnvVariableReference(
                                            "JAVA_OPTIONS",
                                            targetOS)),
                                cleanupText)); // NOI18N

        // Step 3
        instructions.addStep(Bundle.WebLogicIntegrationProvider_ManualDirectDynamicStep3Wl9Msg(
                                getStartScriptExtension(targetOS),
                                Bundle.WebLogicIntegrationProvider_PathToJvmDirText(
                                    IntegrationUtils.getJavaPlatformName(getTargetJava()))));

        // Step 4
        instructions.addStep(Bundle.WebLogicIntegrationProvider_ManualDirectDynamicStep4Wl9Msg(getStartScriptExtension(targetOS)));

        // Step 5
        instructions.addStep(Bundle.WebLogicIntegrationProvider_ManualDirectStep5Wl9Msg());

        // Note about decreasing CPU profiling overhead
        instructions.addHint(REDUCE_OVERHEAD_MSG);

        return instructions;
    }

    protected IntegrationProvider.IntegrationHints getManualLocalDynamicIntegrationStepsInstructions(String targetOS,
                                                                                                     AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints instructions = new IntegrationProvider.IntegrationHints();

        // Step 1
        instructions.addStep(Bundle.WebLogicIntegrationProvider_ManualDirectDynamicStep1Msg(
                                IntegrationUtils.getEnvVariableReference("BEA_HOME", targetOS), // NOI18N
                                IntegrationUtils.getDirectorySeparator(targetOS), "bin", // NOI18N
                                "base_domain", // NOI18N
                                Bundle.WebLogicIntegrationProvider_CopyFiles9Msg(getStartScriptExtension(targetOS))));

        // Step 2
        String wlSettings = IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_VENDOR", "Sun") // NOI18N
                            + "<br>" // NOI18N
                            + IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_HOME",  // NOI18N
                                                                               Bundle.WebLogicIntegrationProvider_PathToJvmDirText(
                                                                                IntegrationUtils.getJavaPlatformName(getTargetJava())));

        final String cleanupText = Bundle.WebLogicIntegrationProvider_Wl9CleanupText(IntegrationUtils.getBatchExtensionString(targetOS));
        instructions.addStep(Bundle.WebLogicIntegrationProvider_ManualDynamicStep2Msg(
                                getStartScriptExtension(targetOS),
                                IntegrationUtils.isWindowsPlatform(targetOS) ? 
                                    Bundle.WebLogicIntegrationProvider_WindowsAnchorText()
                                    : Bundle.WebLogicIntegrationProvider_Wl9AnchorText(), 
                                wlSettings,
                                cleanupText));

        // Step 3
        instructions.addStep(Bundle.WebLogicIntegrationProvider_ManualDirectDynamicStep3Wl9Msg(
                                getStartScriptExtension(targetOS),
                                Bundle.WebLogicIntegrationProvider_PathToJvmDirText(
                                    IntegrationUtils.getJavaPlatformName(getTargetJava()))));

        // Step 4
        instructions.addStep(Bundle.WebLogicIntegrationProvider_ManualDirectDynamicStep4Wl9Msg(getStartScriptExtension(targetOS)));

        // Step 5
        instructions.addStep(Bundle.WebLogicIntegrationProvider_ManualDynamicStep5Wl9Msg());

        // Put here a warning that the IDE must be run under JDK6/7
        instructions.addWarning(Bundle.WebLogicIntegrationProvider_DynamicWarningMessage(
                                    IntegrationUtils.getJavaPlatformName(getTargetJava())));

        return instructions;
    }

    protected IntegrationProvider.IntegrationHints getManualRemoteIntegrationStepsInstructions(String targetOS,
                                                                                               AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints instructions = new IntegrationProvider.IntegrationHints();

        // Step 1
        instructions.addStep(getManualRemoteStep1(targetOS));

        // Step 2
        instructions.addStep(getManualRemoteStep2(targetOS));

        // Step 3
        instructions.addStep(Bundle.WebLogicIntegrationProvider_ManualRemoteStep3Msg(
                                IntegrationUtils.getEnvVariableReference("BEA_HOME", targetOS), // NOI18N
                                IntegrationUtils.getDirectorySeparator(targetOS), "bin", // NOI18N
                                "base_domain", // NOI18N
                                Bundle.WebLogicIntegrationProvider_CopyFiles9Msg(getStartScriptExtension(targetOS))));

        // Step 4
        String wlSettings = IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_VENDOR", "Sun") // NOI18N
                            + "<br>" // NOI18N
                            + IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_HOME",  // NOI18N
                                                                               Bundle.WebLogicIntegrationProvider_PathToJvmDirText(
                                                                                    IntegrationUtils.getJavaPlatformName(getTargetJava())));

        final String cleanupText = Bundle.WebLogicIntegrationProvider_Wl9CleanupText(IntegrationUtils.getBatchExtensionString(targetOS));
        instructions.addStep(Bundle.WebLogicIntegrationProvider_ManualRemoteStep4Msg(
                                getStartScriptExtension(targetOS),
                                IntegrationUtils.isWindowsPlatform(targetOS) ? 
                                    Bundle.WebLogicIntegrationProvider_WindowsAnchorText()
                                    : Bundle.WebLogicIntegrationProvider_Wl9AnchorText(), 
                                wlSettings,
                                IntegrationUtils.getAssignEnvVariableValueString(
                                    targetOS, 
                                    "JAVA_OPTIONS", // NOI18N
                                    IntegrationUtils.getProfilerAgentCommandLineArgs(
                                        targetOS,
                                        getTargetJava(),
                                        attachSettings.isRemote(),
                                        attachSettings.getPort())
                                        + " "
                                        + IntegrationUtils.getEnvVariableReference(
                                            "JAVA_OPTIONS", // NOI18N
                                            targetOS)),
                                IntegrationUtils.getRemoteAbsolutePathHint(), 
                                cleanupText));

        // Step 5
        instructions.addStep(Bundle.WebLogicIntegrationProvider_ManualRemoteStep5Wl9Msg(
                                getStartScriptExtension(targetOS),
                                Bundle.WebLogicIntegrationProvider_PathToJvmDirText(
                                    IntegrationUtils.getJavaPlatformName(getTargetJava()))));

        // Step 6
        instructions.addStep(Bundle.WebLogicIntegrationProvider_ManualRemoteStep6Wl9Msg(getStartScriptExtension(targetOS)));

        // Step 7
        instructions.addStep(Bundle.WebLogicIntegrationProvider_ManualRemoteStep7Wl9Msg());

        // Note about decreasing CPU profiling overhead
        instructions.addHint(REDUCE_OVERHEAD_MSG);

        return instructions;
    }

    private String getStartScriptExtension(String targetOS) {
        return (IntegrationUtils.isWindowsPlatform(targetOS) ? "cmd" : "sh"); // NOI18N
    }
}
