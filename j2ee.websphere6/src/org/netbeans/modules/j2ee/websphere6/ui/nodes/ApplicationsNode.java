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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.websphere6.ui.nodes;

import java.awt.Image;
import javax.swing.Action;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class ApplicationsNode extends AbstractNode {

    public ApplicationsNode(Lookup lookup) {
        super(new ApplicationsChildren(lookup));
        setDisplayName(NbBundle.getMessage(ApplicationsNode.class, "LBL_Apps"));
    }

    @Override
    public Image getIcon(int type) {
        return getIconDelegate().getIcon(type);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIconDelegate().getOpenedIcon(type);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{};
    }

    private Node getIconDelegate() {
        return DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
    }

    private static class ApplicationsChildren extends Children.Keys<Node> {

        public ApplicationsChildren(Lookup lookup) {
            setKeys(new Node[]{
                EnterpriseModulesNode.newInstance(lookup),
                EjbModulesNode.newInstance(lookup),
                WebModulesNode.newInstance(lookup)
            });
        }

        @Override
        protected Node[] createNodes(Node key) {
            return new Node[] {key};
        }

    }
}
