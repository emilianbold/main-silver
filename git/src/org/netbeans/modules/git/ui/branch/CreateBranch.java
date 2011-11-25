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

package org.netbeans.modules.git.ui.branch;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.ui.repository.RevisionDialogController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author ondra
 */
public class CreateBranch implements DocumentListener {
    private CreateBranchPanel panel;
    private RevisionDialogController revisionPicker;
    private JButton okButton;
    private DialogDescriptor dd;
    private boolean revisionValid = true;
    private Boolean nameValid = false;
    private final Task branchCheckTask;
    private String branchName;
    private final File repository;
    private final Icon ICON_ERROR = new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/git/resources/icons/info.png")); //NOI18N

    CreateBranch (File repository, String initialRevision) {
        this.repository = repository;
        this.branchCheckTask = Git.getInstance().getRequestProcessor(repository).create(new BranchNameCheckWorker());
        revisionPicker = new RevisionDialogController(repository, new File[] { repository }, initialRevision);
        panel = new CreateBranchPanel(revisionPicker.getPanel());
    }

    String getRevision () {
        return revisionPicker.getRevision();
    }
    
    String getBranchName () {
        return panel.branchNameField.getText();
    }

    boolean show() {
        okButton = new JButton(NbBundle.getMessage(CreateBranch.class, "LBL_CreateBranch.OKButton.text")); //NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(okButton, okButton.getText());
        dd = new DialogDescriptor(panel, NbBundle.getMessage(CreateBranch.class, "LBL_CreateBranch.title"), true,  //NOI18N
                new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(CreateBranch.class), null);
        validate();
        revisionPicker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange (PropertyChangeEvent evt) {
                if (evt.getPropertyName() == RevisionDialogController.PROP_VALID) {
                    setRevisionValid(Boolean.TRUE.equals(evt.getNewValue()));
                }
            }
        });
        panel.branchNameField.getDocument().addDocumentListener(this);
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.setVisible(true);
        return okButton == dd.getValue();
    }
    
    private void setRevisionValid (boolean flag) {
        this.revisionValid = flag;
        if (!flag) {
            setErrorMessage(NbBundle.getMessage(CreateBranch.class, "MSG_CreateBranch.errorRevision")); //NOI18N
        }
        validate();
    }

    private void validate () {
        boolean flag = revisionValid & Boolean.TRUE.equals(nameValid);
        if (revisionValid && Boolean.FALSE.equals(nameValid)) {
            if (panel.branchNameField.getText().isEmpty()) {
                setErrorMessage(NbBundle.getMessage(CreateBranch.class, "MSG_CreateBranch.errorBranchNameEmpty")); //NOI18N
            } else {
                setErrorMessage(NbBundle.getMessage(CreateBranch.class, "MSG_CreateBranch.errorBranchExists")); //NOI18N
            }
        }
        if (flag) {
            setErrorMessage(null);
        }
        okButton.setEnabled(flag);
        dd.setValid(flag);
    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        validateName();
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        validateName();
    }

    @Override
    public void changedUpdate (DocumentEvent e) { }

    private void validateName () {
        nameValid = false;
        branchName = panel.branchNameField.getText();
        if (!branchName.isEmpty()) {
            nameValid = null;
            branchCheckTask.cancel();
            branchCheckTask.schedule(500);
        }
        validate();
    }
    
    private class BranchNameCheckWorker implements Runnable {
        @Override
        public void run () {
            final String branchName = CreateBranch.this.branchName;
            try {
                GitClient client = Git.getInstance().getClient(repository);
                final Map<String, GitBranch> branches = client.getBranches(false, ProgressMonitor.NULL_PROGRESS_MONITOR);
                EventQueue.invokeLater(new Runnable () {
                    @Override
                    public void run () {
                        if (branchName.equals(panel.branchNameField.getText())) {
                            nameValid = !branches.containsKey(branchName);
                            validate();
                        }
                    }
                });
            } catch (GitException ex) {
                GitClientExceptionHandler.notifyException(ex, true);
            }
        }
    }

    private void setErrorMessage (String message) {
        panel.lblError.setText(message);
        if (message == null || message.isEmpty()) {
            panel.lblError.setIcon(null);
        } else {
            panel.lblError.setIcon(ICON_ERROR);
        }
    }
}
