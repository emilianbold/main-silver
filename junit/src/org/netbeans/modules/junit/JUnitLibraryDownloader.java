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

package org.netbeans.modules.junit;

import java.awt.event.ActionEvent;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.options.OptionsDisplayer;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Dialog;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.openide.DialogDisplayer;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.JScrollPane;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.autoupdate.ui.api.PluginManager;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport.LibraryDefiner;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import static org.netbeans.modules.junit.Bundle.*;

/**
 * Defines JUnit 3.x/4.x libraries by downloading their defining modules.
 */
@ServiceProvider(service=LibraryDefiner.class)
public class JUnitLibraryDownloader implements LibraryDefiner {
    private static RequestProcessor RP = new RequestProcessor(JUnitLibraryDownloader.class.getName(), 1);
    private static final Logger LOG = Logger.getLogger(JUnitLibraryDownloader.class.getName());

    @Messages({
        "searching_handle=Searching for \"junit\" library on NetBeans plugin portal...",
        "resolve_title=Resolve \"junit\" Reference Problem",
        "networkproblem_header=Unable to connect  to the NetBeans plugin portal",
        "networkproblem_message=Check your proxy settings or try again later. "
            + "The server may be unavailable at the moment. "
            + "You may also want to make sure that your firewall is not blocking network traffic. \n\n"
            + "If you have the missing \"junit\" library, you can resolve the reference "
            + "problem manually using Library Manager.",
        "proxy_button=&Proxy Settings...",
        "library_button=&Library Manager...",
        "tryagain_button=Try &Again",
        "nodownload_header=\"junit\" library has not been downloaded",
        "nodownload_message=You can try to download \"junit\" library again, or \n\n"
            + "if you have the missing \"junit\" library, you can resolve the reference "
            + "problem manually using Library Manager.",
        "active_handle=Activating JUnit"
    })
    
    public @Override Callable<Library> missingLibrary(final String name) {
        if (!name.matches("junit(_4)?")) {
            return null;
        }
        return new Callable<Library>() {
            public @Override Library call() throws Exception {
                return download(name);
            }
        };
    }
    
    private JButton libraryManager;
    private JButton tryAgain;
    private JButton proxySettings;

    @SuppressWarnings("SleepWhileInLoop")
    private Library download(String name) throws Exception {
        UpdateUnit unit = findJUnitLib();
        if (unit == null) {
            final ProgressHandle handle = ProgressHandleFactory.createHandle(searching_handle());
            initButtons();
            final DialogDescriptor searching = new DialogDescriptor(searchingPanel(new JLabel(searching_handle()),
                    ProgressHandleFactory.createProgressComponent(handle)), resolve_title(), true, null);
            handle.setInitialDelay (0);
            handle.start ();
            searching.setOptions(new Object[] {NotifyDescriptor.CANCEL_OPTION});
            searching.setMessageType(NotifyDescriptor.PLAIN_MESSAGE);
            final Dialog dlg = DialogDisplayer.getDefault().createDialog(searching);
            RP.post(new Runnable() {

                @Override
                public void run() {
                    // May be first start, when no update lists have yet been downloaded.
                    try {
                        for (UpdateUnitProvider p : UpdateUnitProviderFactory.getDefault().getUpdateUnitProviders(true)) {
                            p.refresh(handle, true);
                        }
                        // close searching
                        dlg.dispose();
                    } catch (IOException ex) {
                        Logger.getLogger(JUnitLibraryDownloader.class.getName()).log(Level.FINE, ex.getMessage(), ex);
                        if (! dlg.isVisible()) {
                            LOG.fine("dialog not visible => do nothing");
                            return ;
                        }
                        DialogDescriptor networkProblem = new DialogDescriptor(
                                problemPanel(resolve_title(), networkproblem_message()), // message
                                networkproblem_header(), // title
                                true, // modal
                                null);
                        networkProblem.setOptions(new Object[] {tryAgain, proxySettings, NotifyDescriptor.CANCEL_OPTION});
                        networkProblem.setAdditionalOptions(new Object[] {libraryManager});
                        networkProblem.setClosingOptions(new Object[] {libraryManager, tryAgain, NotifyDescriptor.CANCEL_OPTION});
                        networkProblem.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
                        Dialog networkProblemDialog = DialogDisplayer.getDefault().createDialog(networkProblem);
                        networkProblemDialog.setVisible(true);
                        Object answer = networkProblem.getValue();
                        if (NotifyDescriptor.CANCEL_OPTION.equals(answer) || answer.equals(-1) /* escape */ ) {
                            LOG.fine("cancel network problem dialog");
                            searching.setValue(answer);
                            dlg.dispose();
                        } else if (tryAgain.equals(answer)) {
                            LOG.fine("try again searching");
                            RP.post(this);
                        } else if (libraryManager.equals(answer)) {
                            LOG.fine("open library manager");
                            searching.setValue(answer);
                            dlg.dispose();
                        } else {
                            assert false : "Unknown " + answer;
                        }
                    }
                }
            });
            dlg.setVisible(true);
            handle.finish();
            if (NotifyDescriptor.CANCEL_OPTION.equals(searching.getValue()) || searching.getValue().equals(-1) /* escape */) {
                LOG.fine("user canceled searching JUnit");
                return showNoDownloadDialog(name);
            } else if (libraryManager.equals(searching.getValue())) {
                throw new Exception("user canceled searching");
            }
            unit = findJUnitLib();
            if (unit == null) {
                LOG.fine("could not find junitlib on any update site");
                return showNoDownloadDialog(name);
            }
        }
        // check if JUnit installed
        if (unit.getInstalled() != null) {
            LOG.fine(unit.getInstalled() + " already installed. Is active? " + unit.getInstalled().isEnabled());
            if (unit.getInstalled().isEnabled()) {
                throw new Exception(unit.getInstalled() + " already installed and active");
            } else {
                // activate it
                OperationContainer<OperationSupport> oc = OperationContainer.createForEnable();
                if (!oc.canBeAdded(unit, unit.getInstalled())) {
                    throw new Exception("could not add " + unit.getInstalled() + " for activation");
                }
                for (UpdateElement req : oc.add(unit.getInstalled()).getRequiredElements()) {
                    oc.add(req);
                }
                ProgressHandle activeHandle = ProgressHandleFactory.createHandle (active_handle());
                Restarter restarter = oc.getSupport().doOperation(activeHandle);
                assert restarter == null : "No Restater need to make " + unit.getInstalled() + " active";
                // XXX new library & build.properties apparently do not show up immediately... how to listen properly?
                for (int i = 0; i < 10; i++) {
                    Library lib = LibraryManager.getDefault().getLibrary(name);
                    if (lib != null) {
                        return lib;
                    }
                    Thread.sleep(1000);
                }
                LOG.info("junitlib failed to make active properly");
                return showNoDownloadDialog(name);
            }
        }
        List<UpdateElement> updates = unit.getAvailableUpdates();
        if (updates.isEmpty()) {
            throw new Exception("no updates for " + unit);
        }
        OperationContainer<InstallSupport> oc = OperationContainer.createForInstall();
        UpdateElement element = updates.get(0);
        if (!oc.canBeAdded(unit, element)) {
            throw new Exception("could not add " + element + " to updates");
        }
        for (UpdateElement req : oc.add(element).getRequiredElements()) {
            oc.add(req);
        }
        if (!PluginManager.openInstallWizard(oc)) {
            LOG.fine("user canceled PM");
            return showNoDownloadDialog(name);
        }
        // XXX new library & build.properties apparently do not show up immediately... how to listen properly?
        for (int i = 0; i < 10; i++) {
            Library lib = LibraryManager.getDefault().getLibrary(name);
            if (lib != null) {
                return lib;
            }
            Thread.sleep(1000);
        }
        LOG.info("junitlib failed to install properly");
        return showNoDownloadDialog(name);
    }

    private UpdateUnit findJUnitLib() throws IOException {
        for (UpdateUnit unit : UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE)) {
            if (unit.getCodeName().equals("org.netbeans.modules.junitlib")) {
                return unit;
            }
        }
        return null;
    }
    
    private void initButtons() {
        if (libraryManager != null) {
            return ;
        }
        libraryManager = new JButton();
        tryAgain = new JButton();
        proxySettings = new JButton();
        Mnemonics.setLocalizedText(tryAgain, tryagain_button());
        Mnemonics.setLocalizedText(libraryManager, library_button());
        Mnemonics.setLocalizedText(proxySettings, proxy_button());
        proxySettings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LOG.fine("show proxy options");
                OptionsDisplayer.getDefault().open("General"); // NOI18N
            }
        });
    }
    
    private Library showNoDownloadDialog(String name) throws Exception {
        DialogDescriptor networkProblem = new DialogDescriptor(
                problemPanel(nodownload_header(), nodownload_message()), // message
                resolve_title(), // title
                true, // modal
                null);
        initButtons();
        networkProblem.setOptions(new Object[] {tryAgain, NotifyDescriptor.CANCEL_OPTION});
        networkProblem.setAdditionalOptions(new Object[] {libraryManager});
        networkProblem.setClosingOptions(new Object[] {libraryManager, tryAgain, NotifyDescriptor.CANCEL_OPTION});
        networkProblem.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
        Dialog networkProblemDialog = DialogDisplayer.getDefault().createDialog(networkProblem);
        networkProblemDialog.setVisible(true);
        Object answer = networkProblem.getValue();
        if (NotifyDescriptor.CANCEL_OPTION.equals(answer) || answer.equals(-1) /* escape */ ) {
            LOG.fine("cancel no download dialog");
            //throw new InterruptedException("user canceled download & install JUnit");
            return null;
        } else if (tryAgain.equals(answer)) {
            LOG.fine("try again download()");
            return download(name);
        } else if (libraryManager.equals(answer)) {
            LOG.fine("open library manager");
            throw new Exception("junitlib failed/canceled to install properly, open library manager instaed");            
        } else {
            assert false : "Unknown " + answer;
        }
        assert false : "Unknown " + answer;
        return null;
    }
    
    private static JPanel searchingPanel(JLabel progressLabel, JComponent progressComponent) {
        JPanel panel = new JPanel();
        progressLabel.setLabelFor(progressComponent);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(progressLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(progressComponent, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(96, 96, 96)
                .addComponent(progressLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressComponent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(109, Short.MAX_VALUE))
        );
        return panel;
    }
    
    private static JPanel problemPanel(String header, String message) {
        JPanel panel = new JPanel();
        JLabel jLabel1 = new javax.swing.JLabel();
        JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        JTextArea jTextArea1 = new javax.swing.JTextArea();

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel1.setText(header);

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setRows(5);
        jTextArea1.setText(message);
        jTextArea1.setOpaque(false);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(107, 107, 107)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                .addGap(82, 82, 82))
        );
        return panel;
    }

}
