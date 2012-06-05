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

package org.netbeans.modules.profiler.drilldown;

import java.awt.Dimension;
import org.netbeans.lib.profiler.ui.cpu.statistics.StatisticalModule;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import java.awt.Image;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import org.netbeans.lib.profiler.common.CommonUtils;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;


/**
 * An IDE TopComponent to display drilldown for profiling results.
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "DrillDownWindow_DrillDownCaption=DrillDown",
    "DrillDownWindow_DrillDownAccessDescr=High-level category view of where application spends time"
})
// TODO: should extend ProfilerTopComponent
public final class DrillDownWindow extends TopComponent {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------
    private static final String HELP_CTX_KEY = "DrillDownWindow.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);
    private static final String PREFERRED_ID = "DrillDownWindow"; // NOI18N // for winsys persistence
    private static final Image pieIcon = Icons.getImage(GeneralIcons.PIE);
    private static DrillDownWindow defaultInstance;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private AbstractButton presenter;
    private DrillDownPanel ddPanel;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    // --- Private implementation ------------------------------------------------
    public DrillDownWindow() {
        setName(Bundle.DrillDownWindow_DrillDownCaption());
        getAccessibleContext().setAccessibleDescription(Bundle.DrillDownWindow_DrillDownAccessDescr());
        setIcon(pieIcon);
        setFocusable(true);
        setRequestFocusEnabled(true);

        presenter = createPresenter();
        updatePresenter();

        setLayout(new java.awt.BorderLayout());
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    // --- Public interface ------------------------------------------------------
    public static synchronized DrillDownWindow getDefault() {
        if (defaultInstance == null) {
            CommonUtils.runInEventDispatchThreadAndWait(new Runnable() {
                    public void run() {
                        final TopComponent tc = WindowManager.getDefault().findTopComponent(PREFERRED_ID);

                        if ((tc != null) && tc instanceof DrillDownWindow) {
                            defaultInstance = (DrillDownWindow) tc;
                        } else {
                            defaultInstance = new DrillDownWindow();
                        }
                    }
                });
        }

        return defaultInstance;
    }

    public HelpCtx getHelpCtx() {
        return HELP_CTX;
    }

    public AbstractButton getPresenter() {
        return presenter;
    }

    public static void closeIfOpened() {
        if (defaultInstance != null) {
            CommonUtils.runInEventDispatchThread(new Runnable() {
                    public void run() {
                        if (defaultInstance.isOpened()) {
                            defaultInstance.close();
                        }
                    }
                });
        }
    }

    public void setDrillDown(DrillDown drillDown, List statModules) {
        if (ddPanel != null) {
            remove(ddPanel);
        }

        if (!drillDown.isValid()) {
            revalidate();
            repaint();

            return;
        }

        //    drillDown.addListener(new DrillDownListener() {
        //      public void dataChanged() {}
        //
        //      public void drillDownPathChanged(java.util.List newDrillDownPath) {
        //        updater.run();
        //      }
        //    });
        ddPanel = new DrillDownPanel(drillDown);
        ddPanel.pause();
        add(ddPanel, java.awt.BorderLayout.CENTER);

        if (statModules != null) {
            for (Iterator it = statModules.iterator(); it.hasNext();) {
                StatisticalModule module = (StatisticalModule) it.next();
                ddPanel.addSnippet(module);
            }
        }

        ddPanel.resume();
        revalidate();
        repaint();
    }

    // --- TopComponent behavior -------------------------------------------------
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    public boolean needsDocking() {
        return WindowManager.getDefault().findMode(this) == null;
    }

    public void open() {
        if (needsDocking()) {
            Mode mode = WindowManager.getDefault().findMode("commonpalette"); //NOI18N

            if (mode != null) {
                mode.dockInto(this);
            }
        }

        super.open();
    }

    protected void componentClosed() {
        super.componentClosed();
        updatePresenter();

        //    if (ddPanel != null) ddPanel.pause();
    }

    protected void componentOpened() {
        super.componentOpened();
        updatePresenter();

        //    if (ddPanel != null) ddPanel.resume();
    }

    protected String preferredID() {
        return PREFERRED_ID;
    } //NOI18N

    // --- Presenter stuff -------------------------------------------------------
    private AbstractButton createPresenter() {
        JToggleButton button = new JToggleButton() {
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width += 20;
                return d;
            }

            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };

        if (getIcon() == null) {
            button.setText(getName());
            button.setToolTipText(getName());
        } else {
            button.setIcon(new ImageIcon(getIcon()));
            button.setToolTipText(getName());
        }

        return button;
    }

    private void updatePresenter() {
        presenter.setSelected(isOpened());
    }
}
