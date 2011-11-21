/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.Icon;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.maven.api.NbMavenProject;
import static org.netbeans.modules.maven.queries.Bundle.*;
import org.netbeans.modules.maven.spi.nodes.SpecialIcon;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;

@ProjectServiceProvider(service=ProjectInformation.class, projectType="org-netbeans-modules-maven")
public final class Info implements ProjectInformation, PropertyChangeListener {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final Project project;
    private final NbMavenProject nbmp;

    public Info(final Project project) {
        this.project = project;
        nbmp = project.getLookup().lookup(NbMavenProject.class);
        nbmp.addPropertyChangeListener(this);
    }

    @Override public String getName() {
        return nbmp.getMavenProject().getId().replace(':', '_');
    }

    @Messages({
        "# {0} - dir basename", "LBL_misconfigured_project={0} [unloadable]",
        "TXT_Maven_project_at=Maven project at {0}"
    })
    @Override public @NonNull String getDisplayName() {
        MavenProject pr = nbmp.getMavenProject();
        if (NbMavenProject.isErrorPlaceholder(pr)) {
            return LBL_misconfigured_project(project.getProjectDirectory().getNameExt());
        }
        String toReturn = pr.getName();
        if (toReturn == null) {
            String grId = pr.getGroupId();
            String artId = pr.getArtifactId();
            if (grId != null && artId != null) {
                toReturn = grId + ":" + artId; //NOI18N
            } else {
                toReturn = TXT_Maven_project_at(project.getProjectDirectory().getPath());
            }
        }
        return toReturn;
    }
    
    @Override public Icon getIcon() {
        SpecialIcon special = project.getLookup().lookup(SpecialIcon.class);
        return special != null ? special.getIcon() : ImageUtilities.loadImageIcon("org/netbeans/modules/maven/resources/Maven2Icon.gif", true);
    }
    
    @Override public Project getProject() {
        return project;
    }
    
    @Override public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    
    @Override public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override public void propertyChange(PropertyChangeEvent evt) {
        if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
            pcs.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME, null, null);
            pcs.firePropertyChange(ProjectInformation.PROP_ICON, null, null);
        }
    }

}
