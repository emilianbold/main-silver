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

package org.netbeans.modules.profiler.attach.panels;

import org.netbeans.modules.profiler.attach.wizard.AttachWizardContext;
import org.netbeans.modules.profiler.attach.wizard.WizardContext;
import org.netbeans.modules.profiler.attach.wizard.screen.AbstractWizardScreen;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Bachorik
 */
@NbBundle.Messages({
    "AttachWizard_GroupApplication=Application",
    "AttachWizard_GroupApplet=Applet",
    "AttachWizard_GroupJ2EE=J2EE Web/App Server",
    "AttachWizard_AttachWizardCaption=Attach Wizard",
    "AttachWizard_AttachWizardIntegrationCaption=Attach Wizard ({0} Integration)",
    "AttachWizard_UnknownValueString=...",
    "AttachWizard_SelectTargetTypeString=Select Target Type",
    "AttachWizard_SelectTargetTypeForceString=<Select Target Type ...>",
    "AttachWizard_SelectTargetForceString=<Select Target ...>",
    "AttachWizard_ProvideTargetSettingsString=Provide Target Settings",
    "AttachWizard_ProvideDynamicAttachSettingsString=Provide Attach Settings",
    "AttachWizard_ReviewAttachSettingsString=Review Attach Settings",
    "AttachWizard_ChooseIntegrationTypeString=Choose Integration Type",
    "AttachWizard_PerformIntegrationString=Review Integration",
    "AttachWizard_ReviewAdditionalStepsString=Review Additional Steps",
    "AttachWizard_ChooseTargetJvmString=Choose Target JVM",
    "AttachWizard_ReviewIntegrationStepsString=Review Integration Steps",
    "AttachWizard_TargetTypeString=Target type",
    "AttachWizard_TargetNameTypeString=Target",
    "AttachWizard_TargetNameLocationString=Target location",
    "AttachWizard_RemoteSystemString=Remote system",
    "AttachWizard_RemoteSystemHostNameString=Remote system hostname",
    "AttachWizard_RemoteSystemOsString=Remote system OS",
    "AttachWizard_LocalMachineString=Local machine",
    "AttachWizard_AttachMethodString=Attach method",
    "AttachWizard_DirectAttachString=Direct attach",
    "AttachWizard_DynamicAttachString=Dynamic attach",
    "AttachWizard_AttachInvocationString=Attach invocation",
    "AttachWizard_TargetNamePidString=Target {0} PID",
    "AttachWizard_WorkingDirectoryString=Working directory",
    "AttachWizard_AutomaticIntegrationSuccMsg=Automatic integration performed successfuly.",
    "AttachWizard_ReviewAdditionalStepsMsg=Please review additional steps:",
    "AttachWizard_NoStepsRequiredMsg=No additional steps are required.",
    "AttachWizard_AutomaticIntegrationFailedMsg=Automatic integration failed.",
    "AttachWizard_TheTargetNameString=the {0}"
})
public abstract class AttachWizardPanel extends AbstractWizardScreen {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private AttachWizardContext temporaryContext;

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public final boolean isFinishPanel() {
        return this.canFinish(this.temporaryContext);
    }

    public final boolean canBack(WizardContext context) {
        if (this.temporaryContext == null) return false;

        return canBack(this.temporaryContext);
    }

    public final boolean canFinish(WizardContext context) {
        if (this.temporaryContext == null) return false;
        return canFinish(this.temporaryContext);
    }

    public final boolean canNext(WizardContext context) {
        if (this.temporaryContext == null) return false;
        return canNext(this.temporaryContext);
    }

    public final boolean onCancel(WizardContext context) {
        if (this.temporaryContext == null) return false;
        return onCancel(this.temporaryContext);
    }

    public final void onEnter(WizardContext context) {
        this.temporaryContext = (AttachWizardContext) context;
        onEnter(this.temporaryContext);
    }

    public final void onExit(WizardContext context) {
        if (this.temporaryContext == null) return;
        onExit(this.temporaryContext);
        this.temporaryContext = null;
    }

    public final void onFinish(WizardContext context) {
        if (this.temporaryContext == null) return;
        onFinish(this.temporaryContext);
    }

    public abstract boolean canBack(AttachWizardContext context);

    public abstract boolean canFinish(AttachWizardContext context);

    public abstract boolean canNext(AttachWizardContext context);

    public abstract boolean onCancel(AttachWizardContext context);

    public abstract void onEnter(AttachWizardContext context);

    public abstract void onExit(AttachWizardContext context);

    public abstract void onFinish(AttachWizardContext context);

    protected AttachWizardContext getContext() {
        return this.temporaryContext;
    }

    protected final void onPanelShow(WizardContext ctx) {
        this.temporaryContext = (AttachWizardContext) ctx;
        this.onPanelShow();
    }

    protected abstract void onPanelShow();

    protected void onStoreToContext(WizardContext ctx) {
        this.temporaryContext = (AttachWizardContext) ctx;
    }
}
