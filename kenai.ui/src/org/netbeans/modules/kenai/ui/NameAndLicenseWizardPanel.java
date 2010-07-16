/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.ui;

import java.awt.Component;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.ui.NewKenaiProjectWizardIterator.SharedItem;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 *
 * @author Milan Kubec
 */
public class NameAndLicenseWizardPanel implements WizardDescriptor.Panel,
        WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {

    private NameAndLicenseWizardPanelGUI component;
    private WizardDescriptor settings;

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final List<SharedItem> initialItems;
    private NewKenaiProjectWizardIterator iter;

    public NameAndLicenseWizardPanel(NewKenaiProjectWizardIterator iter, List<SharedItem> items) {
        this.initialItems = items;
        this.iter=iter;
    }

    List<SharedItem> getInitialItems() {
        return initialItems;
    }

    public Component getComponent() {
        if (component == null) {
            component = new NameAndLicenseWizardPanelGUI(this);
        }
        return component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(NameAndLicenseWizardPanel.class.getName());
    }

    public void readSettings(Object settings) {
        this.settings = (WizardDescriptor) settings;
        component.read((WizardDescriptor) settings);
    }

    public void storeSettings(Object settings) {
        component.store((WizardDescriptor) settings);
    }

    public boolean isValid() {
        return component.valid();
    }

    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    protected final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    public void validate() throws WizardValidationException {
        component.validateWizard();
    }

    public boolean isFinishPanel() {
        return false;
    }

    Kenai getKenai() {
        return iter.getKenai();
    }

    void setKenai(Kenai kenai) {
        iter.setKenai(kenai);
    }
}
