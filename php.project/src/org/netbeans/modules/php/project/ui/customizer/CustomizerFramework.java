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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.customizer;

import java.awt.BorderLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.HelpCtx;

public final class CustomizerFramework extends JPanel implements ChangeListener, HelpCtx.Provider {
    private static final long serialVersionUID = 19349854609829890L;

    private static final Logger LOGGER = Logger.getLogger(CustomizerFramework.class.getName());

    private final Category category;
    private final PhpModuleCustomizerExtender extender;

    public CustomizerFramework(Category category, PhpModuleCustomizerExtender extender, PhpProjectProperties uiProps) {
        assert category != null;
        assert extender != null;
        assert uiProps != null;

        this.category = category;
        this.extender = extender;

        uiProps.addCustomizerExtender(extender);

        initComponents();

        add(extender.getComponent(), BorderLayout.CENTER);

        validateData();
    }

    @Override
    public void addNotify() {
        extender.addChangeListener(this);
        super.addNotify();
    }

    @Override
    public void removeNotify() {
        extender.removeChangeListener(this);
        super.removeNotify();
    }

    void validateData() {
        String error = extender.getErrorMessage();
        if (!extender.isValid()) {
            assert error != null : "Customizer extender " + category.getDisplayName() + " returns no error even if it is not valid";
            category.setErrorMessage(error);
            category.setValid(false);
            return;
        }

        // is valid
        if (error != null) {
            LOGGER.log(Level.INFO, "Customizer extender {0} returns error even if it is valid.", category.getDisplayName());
        }

        category.setErrorMessage(extender.getWarningMessage());
        category.setValid(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new BorderLayout());
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    @Override
    public void stateChanged(ChangeEvent e) {
        validateData();
    }

    @Override
    public HelpCtx getHelpCtx() {
        HelpCtx help = extender.getHelp();
        return help != null ? help : HelpCtx.DEFAULT_HELP;
    }
}
