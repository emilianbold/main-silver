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

package org.netbeans.modules.cnd.remote.fs.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CancellationException;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.remote.server.RemoteServerListUI;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Vladimir Kvashin
 */
public class RemoteFileSystemNotifier {

    public interface Callback {
        /**
         * Is called as soon as the host has been connected.
         * It is always called in a specially created thread
         */
        void connected();

        List<String> getPendingFiles();
    }

    private final ExecutionEnvironment env;
    private final Callback callback;
    private boolean shown;
    private Notification notification;

    public RemoteFileSystemNotifier(ExecutionEnvironment execEnv, Callback callback) {
        this.env = execEnv;
        this.callback = callback;
        shown = false;
    }

    public void showIfNeed() {
        synchronized(this) {
            if (shown) {
                return;
            } else {
                shown = true;
                show();
            }
        }
    }

    public static String getDisplayName(ExecutionEnvironment execEnv) {
        ServerRecord rec = ServerList.get(execEnv);
        if (rec == null) {
            return execEnv.getDisplayName();
        } else {
            return rec.getDisplayName();
        }
    }

    private void show() {
        ActionListener onClickAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showConnectDialog();
            }
        };
        String envString = getDisplayName(env);
        notification = NotificationDisplayer.getDefault().notify(
                NbBundle.getMessage(RemoteFileSystemNotifier.class, "RemoteFileSystemNotifier.TITLE", envString),
                ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/remote/fs/ui/error.png", false), // NOI18N
                NbBundle.getMessage(RemoteFileSystemNotifier.class, "RemoteFileSystemNotifier.DETAILS", envString),
                onClickAction,
                NotificationDisplayer.Priority.HIGH);

//        RequestProcessor.getDefault().post(new Runnable() {
//            public void run() {
//                notification.clear();
//            }
//        }, 45*1000);
    }

    private void showConnectDialog() {
        final NotifierPanel panel = new NotifierPanel(env);
        panel.setPendingFiles(callback.getPendingFiles());
        String caption = NbBundle.getMessage(RemoteFileSystemNotifier.class, "RemoteFileSystemNotifier.TITLE");
        DialogDescriptor dd = new DialogDescriptor(panel, caption, true,
                new Object[]{DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION},
                DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {            
            RequestProcessor.getDefault().post(new NamedRunnable("Pending files synchronizer for " + env.getDisplayName()) { //NOI18N
                protected void runImpl() {
                    connect(panel.getPassword(), panel.isRememberPassword());
                }
            });
        } else {
            reShow();
        }
    }

    private void connect(char[] password, boolean rememberPassword) {
        boolean connected = false;
        try {
            if (password == null || password.length == 0) {
                ConnectionManager.getInstance().connectTo(env);
            } else {
                ConnectionManager.getInstance().connectTo(env, password, rememberPassword);
            }
            connected = true;
            RemoteServerListUI.revalidate(env, password, rememberPassword);
            callback.connected();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (CancellationException ex) {
            // don't log cancellation exception
        }
        if (!connected) {
            reShow();
        }
    }
    
    private void reShow() {
        shown = false;
        show();
        //notification.clear();
    }
}
