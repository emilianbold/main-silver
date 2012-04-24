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

package org.netbeans.modules.cnd.discovery.projectimport;

import java.util.Collection;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.discovery.projectimport.ReconfigureProject.CompilerOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.SharedClassObject;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Alexander Simon
 */
public class ReconfigureAction extends NodeAction implements Presenter.Popup {
    private static final RequestProcessor RP = new RequestProcessor(ReconfigureAction.class.getName(), 1);
    private boolean actionPerformedFlag = false;
    private JMenuItem presenter;
    private boolean inited = false;


    public ReconfigureAction() {
        putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/discovery/wizard/resources/waitNode.gif", false)); // NOI18N
    }

    public static Action getReconfigureAction() {
        return SharedClassObject.findObject(ReconfigureAction.class, true);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "CTL_ReconfigureAction"); //NOI18N
    }
    
    @Override
    public JMenuItem getPopupPresenter() {
        return getPresenter();
    }

    private synchronized JMenuItem getPresenter() {
        if (!inited) {
            presenter = new JMenuItem(this);
            inited = true;
        }
        return presenter;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    private boolean enableImpl(Node[] activatedNodes) {
        Project p = getProject(activatedNodes);
        if (p == null) {
            return false;
        }
        ConfigurationDescriptorProvider pdp = p.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp == null || !pdp.gotDescriptor()){
            return false;
        }
        MakeConfigurationDescriptor mcd = pdp.getConfigurationDescriptor();
        if (mcd == null) {
            return false;
        }
        MakeConfiguration configuration = mcd.getActiveConfiguration();
        if (configuration == null || configuration.getConfigurationType().getValue() !=  MakeConfiguration.TYPE_MAKEFILE){
            return false;
        }
        CompilerSet2Configuration set = configuration.getCompilerSet();
        if (set == null) {
            return false;
        }
        CompilerSet cs = set.getCompilerSet();
        if (cs == null) {
            return false;
        }
        return new ReconfigureProject(p).isApplicable();
    }

    
    @Override
    final protected boolean enable(final Node[] nodes) {
        if (actionPerformedFlag) {
            return true;
        }

        putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/discovery/wizard/resources/waitNode.gif", false)); // NOI18N
        if (inited) {
            getPresenter().setEnabled(false);
            getPresenter().invalidate();
        }

        RP.post(new Runnable() {
            private boolean enabled;
            @Override
            public void run() {
                if (SwingUtilities.isEventDispatchThread()) {
                    setEnabled(enabled);
                    putValue(Action.SMALL_ICON, null);
                    if (inited) {
                        getPresenter().setEnabled(enabled);
                        getPresenter().invalidate();
                    }
                } else {
                    enabled = enableImpl(nodes);
                    SwingUtilities.invokeLater(this);
                }
            }
        });

        return false;
    }
    
    @Override
    public void performAction(final Node[] activatedNodes) {
        actionPerformedFlag = true;
        try {
            Project p = getProject(activatedNodes);
            final ReconfigureProject reconfigurator = new ReconfigureProject(p);
            String cFlags;
            String cxxFlags;
            String linkerFlags = "";
            if (reconfigurator.isSunCompiler()){
                cFlags = "-g"; // NOI18N
                cxxFlags = "-g"; // NOI18N
            } else {
                cFlags = "-g3 -gdwarf-2"; // NOI18N
                cxxFlags = "-g3 -gdwarf-2"; // NOI18N
            }

            ReconfigurePanel panel = new ReconfigurePanel(cFlags, cxxFlags, linkerFlags, reconfigurator.getRestOptions(), 
                    getLegend(reconfigurator), reconfigurator.getCompilerSet().getDisplayName());
            JButton runButton = new JButton(NbBundle.getMessage(getClass(), "ReconfigureButton")); // NOI18N
            runButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(getClass(), "ReconfigureButtonAD")); // NOI18N
            Object options[] =  new Object[]{runButton, DialogDescriptor.CANCEL_OPTION};
            DialogDescriptor dialogDescriptor = new DialogDescriptor(
                    panel,
                    NbBundle.getMessage(getClass(), "ReconfigureDialogTitle"), // NOI18N
                    true,
                    options,
                    runButton,
                    DialogDescriptor.BOTTOM_ALIGN,
                    null,
                    null);
            Object ret = DialogDisplayer.getDefault().notify(dialogDescriptor);
            if (ret == runButton) {
                reconfigurator.setConfigureCodeAssistance(true);
                final String c = panel.getCFlags();
                final String cpp = panel.getCppFlags();
                final String link = panel.getLinkerFlags();
                final String other = panel.getOtherOptions();
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        reconfigurator.reconfigure(c, cpp, link, other, false, null);
                    }
                });
            }
        } finally {
           actionPerformedFlag = false;
        }
    }

    private String getLegend(ReconfigureProject reconfigurator){
        CompilerOptions options = reconfigurator.getLastCompilerOptions();
        if (options != null && options.CFlags != null && options.CppFlags != null &&
            options.CCompiler != null && options.CppCompiler != null) {
            String linker = options.LinkerFlags == null ? "" : options.LinkerFlags;
            return NbBundle.getMessage(getClass(), "ReconfigureLegend", options.CCompiler, options.CppCompiler, options.CFlags, options.CppFlags, linker); // NOI18N
        }
        return ""; // NOI18N
    }


    @Override
    protected boolean asynchronous() {
        return false;
    }

    protected Project getProject(Node[] nodes) {
        if (nodes.length != 1) {
            return null;
        }
        for (int i = 0; i < nodes.length; i++) {
            Project p = nodes[i].getLookup().lookup(Project.class);
            if (p != null) {
                return p;
            }
        }
        return null;
    }
}
