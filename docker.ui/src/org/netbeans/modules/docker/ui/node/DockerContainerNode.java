/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.node;

import java.awt.Image;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.docker.api.ContainerStatus;
import static org.netbeans.modules.docker.api.ContainerStatus.PAUSED;
import static org.netbeans.modules.docker.api.ContainerStatus.RUNNING;
import org.netbeans.modules.docker.api.DockerContainer;
import org.netbeans.modules.docker.ui.commit.CommitContainerAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Petr Hejl
 */
public class DockerContainerNode extends AbstractNode {

    private static final String DOCKER_INSTANCE_ICON =
            "org/netbeans/modules/docker/ui/resources/docker_image_small.png"; // NOI18N

    private static final String PAUSED_ICON =
            "org/netbeans/modules/docker/ui/resources/badge_paused.png"; // NOI18N

    private static final String RUNNING_ICON
            = "org/netbeans/modules/docker/ui/resources/badge_running.png"; // NOI18N

    private final DockerContainer container;

    public DockerContainerNode(DockerContainer container) {
        super(Children.LEAF, Lookups.fixed(container));
        this.container = container;
        setDisplayName(container.getImage() + container.getName() + " [" + container.getShortId() + "]");
        setShortDescription(container.getShortId());
        setIconBaseWithExtension(DOCKER_INSTANCE_ICON);
        this.container.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                fireIconChange();
            }
        });
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(StartContainerAction.class),
            SystemAction.get(StopContainerAction.class),
            SystemAction.get(PauseContainerAction.class),
            SystemAction.get(UnpauseContainerAction.class),
            null,
            SystemAction.get(CommitContainerAction.class),
            null,
            SystemAction.get(AttachContainerAction.class),
            SystemAction.get(ShowLogAction.class),
            null,
            SystemAction.get(CopyIdAction.class),
            null,
            SystemAction.get(RemoveContainerAction.class)
        };
    }

    @Override
    public Image getIcon(int type) {
        Image original = super.getIcon(type);
        return badgeIcon(original, container.getStatus());
    }

    private static Image badgeIcon(Image image, ContainerStatus status) {
        Image badge = null;
        switch (status) {
            case PAUSED:
                badge = ImageUtilities.loadImage(PAUSED_ICON);
                break;
            case RUNNING:
                badge = ImageUtilities.loadImage(RUNNING_ICON);
                break;
            default:
                break;
        }
        return badge != null ? ImageUtilities.mergeImages(image, badge, 13, 8) : image;
    }
}
