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
package org.netbeans.modules.dlight.core.stack.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.Action;
import javax.swing.Icon;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;

/**
 *
 * @author mt154047
 */
final class MultipleCallStackRootNode extends AbstractNode {

    private final Vector<StackRootNode> children = new Vector<StackRootNode>();
    private final Image icon = ImageUtilities.icon2Image(new MyIcon());
    private final Action prefferedAction;

    MultipleCallStackRootNode(Action action) {
        super(Children.LEAF);
        setDisplayName("Root");//NOI18N
        this.prefferedAction = action;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{prefferedAction};
    }

    synchronized void add(final StackRootNode node) {
        children.add(node);
        setChildren(Children.LEAF);
        setChildren(new MultipleCallStackRootChildren(children));

    }

    void removeAll() {

        children.clear();
        setChildren(Children.LEAF);
    }

//    @Override
//    public Image getIcon(int type) {
//        return icon;
//    }
//
//    @Override
//    public Image getOpenedIcon(int type) {
//        return getIcon(type);
//    }
//    @Override
//    public String getHtmlDisplayName() {
//        return "<h2>" + getDisplayName() + "</h2>"; // NOI18N
//    }
    class MyIcon implements Icon {

        public void paintIcon(Component c, Graphics g, int x, int y) {
        }

        public int getIconWidth() {
            return 10;
        }

        public int getIconHeight() {
            return 10;
        }
    }
}
