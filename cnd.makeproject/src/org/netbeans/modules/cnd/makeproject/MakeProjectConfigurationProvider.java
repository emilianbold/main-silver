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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.MakeCustomizerProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor.State;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import static org.netbeans.spi.project.ProjectConfigurationProvider.PROP_CONFIGURATIONS;
import org.openide.util.RequestProcessor;

public class MakeProjectConfigurationProvider implements ProjectConfigurationProvider<Configuration>, PropertyChangeListener {

    /**
     * Property name of the set of configurations.
     * Use it when firing a change in the set of configurations.
     */
    String PROP_CONFIGURATIONS_BROKEN = "brokenConfigurations"; // NOI18N

    private final Project project;
    private ConfigurationDescriptorProvider projectDescriptorProvider;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final RequestProcessor RP = new RequestProcessor("Make configuration provider RP", 1); // NOI18N

    public MakeProjectConfigurationProvider(Project project, ConfigurationDescriptorProvider projectDescriptorProvider, PropertyChangeListener info) {
        this.project = project;
        this.projectDescriptorProvider = projectDescriptorProvider;
        this.pcs.addPropertyChangeListener(info);
    }

    @Override
    public Collection<Configuration> getConfigurations() {
        if (!projectDescriptorProvider.gotDescriptor()) {
            return Collections.<Configuration>emptySet();
        }
        MakeConfigurationDescriptor configurationDescriptor = projectDescriptorProvider.getConfigurationDescriptor();
        if (configurationDescriptor == null) {
            return Collections.<Configuration>emptySet();
        }
        return configurationDescriptor.getConfs().getConfigurations();
    }

    @Override
    public Configuration getActiveConfiguration() {
        if (!projectDescriptorProvider.gotDescriptor()) {
            return null;
        }
        MakeConfigurationDescriptor configurationDescriptor = projectDescriptorProvider.getConfigurationDescriptor();
        if (configurationDescriptor == null) {
            return null;
        }
        return configurationDescriptor.getConfs().getActive();
    }

    @Override
    public void setActiveConfiguration(Configuration configuration) throws IllegalArgumentException, IOException {
        if (configuration != null && projectDescriptorProvider != null && projectDescriptorProvider.gotDescriptor()) {
            MakeConfigurationDescriptor configurationDescriptor = projectDescriptorProvider.getConfigurationDescriptor();
            if (configurationDescriptor != null) {
                configurationDescriptor.getConfs().setActive(configuration);
            }
        }
    }

    public void registerPropertyChangeListener(PropertyChangeListener lst) {
        pcs.addPropertyChangeListener(lst);
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener lst) {
        pcs.addPropertyChangeListener(lst);
        if (projectDescriptorProvider != null) {
            RP.post(new Runnable() {

                @Override
                public void run() {
                    if (projectDescriptorProvider != null) {
                        MakeConfigurationDescriptor makeConfigurationDescriptor = projectDescriptorProvider.getConfigurationDescriptor();
                        if (makeConfigurationDescriptor != null && makeConfigurationDescriptor.getState() != State.BROKEN) {  // IZ 122372 // IZ 182321
                            makeConfigurationDescriptor.getConfs().addPropertyChangeListener(MakeProjectConfigurationProvider.this);
                            pcs.firePropertyChange(PROP_CONFIGURATIONS, null, getConfigurations());
                            pcs.firePropertyChange(PROP_CONFIGURATION_ACTIVE, null, getActiveConfiguration());
                        } else {
                            if (makeConfigurationDescriptor != null && makeConfigurationDescriptor.getState() == State.BROKEN) {
                                // notify problem
                                pcs.firePropertyChange(PROP_CONFIGURATIONS_BROKEN, null, State.BROKEN);
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener lst) {
        pcs.removePropertyChangeListener(lst);
        if (projectDescriptorProvider != null && projectDescriptorProvider.gotDescriptor()) {
            if (projectDescriptorProvider != null) {
                MakeConfigurationDescriptor makeConfigurationDescriptor = projectDescriptorProvider.getConfigurationDescriptor();
                if (makeConfigurationDescriptor != null && makeConfigurationDescriptor.getState() != State.BROKEN) {  // IZ 122372 // IZ 182321
                    makeConfigurationDescriptor.getConfs().addPropertyChangeListener(MakeProjectConfigurationProvider.this);
                }
            }
        }
    }

    @Override
    public boolean hasCustomizer() {
        if (projectDescriptorProvider != null && projectDescriptorProvider.gotDescriptor() && projectDescriptorProvider.getConfigurationDescriptor() != null) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void customize() {
        MakeCustomizerProvider makeCustomizer = project.getLookup().lookup(MakeCustomizerProvider.class);
        makeCustomizer.showCustomizer("Build"); // NOI18N
    }

    @Override
    public boolean configurationsAffectAction(String command) {
        return false;
    /*
    return command.equals(ActionProvider.COMMAND_RUN) ||
    command.equals(ActionProvider.COMMAND_BUILD) ||
    command.equals(ActionProvider.COMMAND_CLEAN) ||
    command.equals(ActionProvider.COMMAND_DEBUG);
     */
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        assert pcs != null;

        pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE, null, null);
        pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATIONS, null, null);
    }
}
