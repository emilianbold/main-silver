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

package org.netbeans.modules.dlight.core.stack.ui;

import java.awt.Image;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.dataprovider.SourceFileInfoDataProvider;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;

/**
 *
 * @author mt154047
 */
final class StackRootNode extends AbstractNode{
    private final String stackName;
    private final Image icon;
    private final Action[] actions;

    StackRootNode(SourceFileInfoDataProvider sourceFileInfoDataProvider, Icon icon, String stackName, List<FunctionCall> stack, Action[] actions) {
        super(stack == null || stack.size() == 0 ? Children.LEAF : new PlainListFunctionCallChildren(sourceFileInfoDataProvider, stack));
        this.stackName = stackName;
        this.icon = icon != null ? ImageUtilities.icon2Image(icon) : null;
        this.actions = actions == null ? new Action[0] : actions;
    }

    StackRootNode(SourceFileInfoDataProvider sourceFileInfoDataProvider, Icon icon, String stackName, List<FunctionCall> stack) {
     //   super(stack == null || stack.size() == 0 ? Children.LEAF : new FunctionCallChildren( new CallStackTreeModel(sourceFileInfoDataProvider, stack), stack.get(stack.size() - 1)));
        this(sourceFileInfoDataProvider, icon, stackName, stack, null);

    }
    
    StackRootNode(SourceFileInfoDataProvider sourceFileInfoDataProvider, String stackName, List<FunctionCall> stack) {
        this(sourceFileInfoDataProvider, null, stackName, stack);
    }

    @Override
    public Action[] getActions(boolean context) {
        return actions;
    }

    @Override
    public String getDisplayName() {
        return stackName;
    }

    @Override
    public Image getIcon(int type) {
      return icon == null ? super.getIcon(type) : icon;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
}
