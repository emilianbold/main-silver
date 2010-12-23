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
package org.netbeans.modules.remote.impl.fs.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import javax.swing.ImageIcon;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.remote.api.ui.ConnectionNotifier;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Vladimir Kvashin
 */
public class ConnectionNotifierDelegate implements ConnectionListener {


    private static final Map<ExecutionEnvironment, ConnectionNotifierDelegate> instances = new HashMap<ExecutionEnvironment, ConnectionNotifierDelegate>();
    private static RequestProcessor RP = new RequestProcessor("Connection notifier"); //NOI18N

    static public ConnectionNotifierDelegate getInstance(ExecutionEnvironment env) {
        synchronized (instances) {
            ConnectionNotifierDelegate instance = instances.get(env);
            if (instance == null) {
                instance = new ConnectionNotifierDelegate(env);
                instances.put(env, instance);
            }
            return instance;
        }
    }

    private final ExecutionEnvironment env;
    private boolean shown;
    private Notification notification;
    private final Set<ConnectionNotifier.NamedRunnable> tasks = new HashSet<ConnectionNotifier.NamedRunnable>();

    public ConnectionNotifierDelegate(ExecutionEnvironment execEnv) {
        this.env = execEnv;
        shown = false;
    }

    public void addTask(ConnectionNotifier.NamedRunnable task) {
        synchronized (tasks) {
            tasks.add(task);
            showIfNeed();
        }
    }

    public void removeTask(ConnectionNotifier.NamedRunnable task) {
        synchronized (tasks) {
            tasks.remove(task);
        }
    }


    @Override
    public void connected(ExecutionEnvironment env) {
        if (this.env.equals(env)) {
            ConnectionManager.getInstance().removeConnectionListener(this);
            RequestProcessor.getDefault().post(new ConnectionNotifier.NamedRunnable("Connection notifier for " + env.getDisplayName()) { //NOI18N
                @Override
                protected void runImpl() {
                    onConnect();
                }
            });
        }
    }

    @Override
    public void disconnected(ExecutionEnvironment env) {
    }

    private void onConnect() {
        Notification n;
        synchronized (this) {
            n = notification;
            shown = false;
        }
        if (n != null) {
            n.clear();
        }
        List<ConnectionNotifier.NamedRunnable> toLaunch;
        synchronized (tasks) {
            toLaunch = new ArrayList<ConnectionNotifier.NamedRunnable>(tasks);
            tasks.clear();
        }
        for (ConnectionNotifier.NamedRunnable task : toLaunch) {
            RP.post(task);
        }
    }

    public void showIfNeed() {
        synchronized (this) {
            if (shown) {
                return;
            } else {
                shown = true;
                ConnectionManager cm = ConnectionManager.getInstance();
                cm.addConnectionListener(this);
                show(null);
            }
        }
    }

    private void show(Exception error) {
        ActionListener onClickAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RequestProcessor.getDefault().post(new ConnectionNotifier.NamedRunnable("Requesting connection for " + env.getDisplayName()) { //NOI18N
                    @Override
                    protected void runImpl() {
                        connect();
                    }
                });
            }
        };
        String envString = env.getDisplayName(); // RemoteUtil.getDisplayName(env);

        String title, details;
        ImageIcon icon;

        if (error == null) {
            StringBuilder reasons = new StringBuilder();
            for (ConnectionNotifier.NamedRunnable task : tasks) {
                reasons.append(' ');
                reasons.append(task.getName());
            }
            title = NbBundle.getMessage(ConnectionNotifierDelegate.class, "ConnectionNotifier.TITLE", envString, reasons);
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/remote/impl/fs/ui/exclamation.gif", false); // NOI18N
            details = NbBundle.getMessage(ConnectionNotifierDelegate.class, "ConnectionNotifier.DETAILS", envString);
        } else {
            title = NbBundle.getMessage(getClass(), "ConnectionNotifier.error.TITLE", envString);
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/remote/impl/fs/ui/error.png", false); // NOI18N
            String errMsg = (error.getMessage() == null) ? "" : error.getMessage();
            details = NbBundle.getMessage(getClass(), "ConnectionNotifier.error.DETAILS", errMsg, envString);
        }
        Notification n = NotificationDisplayer.getDefault().notify(title, icon, details, onClickAction, NotificationDisplayer.Priority.HIGH);
        synchronized (this) {
            notification = n;
        }
    }

    private void connect() {
        try {
            ConnectionManager.getInstance().connectTo(env);
            //RemoteUtil.checkSetupAfterConnection(env);
        } catch (IOException ex) {
            reShow(ex);
        } catch (CancellationException ex) {
            // don't log cancellation exception
            reShow(null);
        }
    }

    private void reShow(Exception error) {
        synchronized (this) {
            shown = false;
        }
        show(error);
        //notification.clear();
    }
}
