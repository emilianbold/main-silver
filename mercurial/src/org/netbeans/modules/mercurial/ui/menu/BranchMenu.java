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
 *
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

package org.netbeans.modules.mercurial.ui.menu;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.mercurial.ui.branch.CloseBranchAction;
import org.netbeans.modules.mercurial.ui.branch.CreateBranchAction;
import org.netbeans.modules.mercurial.ui.branch.SwitchToBranchAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;

/**
 * Container menu for branch actions.
 *
 * @author Maros Sandor
 */
public class BranchMenu extends DynamicMenu implements Presenter.Menu {
    private final Lookup lkp;

    public BranchMenu (Lookup lkp) {
        super(NbBundle.getMessage(BranchMenu.class, "CTL_MenuItem_BranchMenu"));
        this.lkp = lkp;
    }

    @Override
    protected JMenu createMenu() {
        JMenu menu = new JMenu(this);
        JMenuItem item;
        if (lkp == null) {
            org.openide.awt.Mnemonics.setLocalizedText(menu, NbBundle.getMessage(BranchMenu.class, "CTL_MenuItem_BranchMenu")); // NOI18N
            item = new JMenuItem();
            Actions.connect(item, (Action) SystemAction.get(SwitchToBranchAction.class), false);
            menu.add(item);
            item = new JMenuItem();
            Actions.connect(item, (Action) SystemAction.get(CreateBranchAction.class), false);
            menu.add(item);
            item = new JMenuItem();
            Actions.connect(item, (Action) SystemAction.get(CloseBranchAction.class), false);
            menu.add(item);
        } else {
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(SwitchToBranchAction.class), NbBundle.getMessage(SwitchToBranchAction.class, "CTL_PopupMenuItem_SwitchToBranch"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(CreateBranchAction.class), NbBundle.getMessage(CreateBranchAction.class, "CTL_PopupMenuItem_CreateBranch"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(CloseBranchAction.class), NbBundle.getMessage(CloseBranchAction.class, "CTL_PopupMenuItem_CloseBranch"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
        }        
        return menu;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        JMenu menu = createMenu();
        menu.setText(NbBundle.getMessage(BranchMenu.class, "CTL_MenuItem_BranchMenu.popupName"));
        enableMenu(menu);
        return menu;
    }
}
