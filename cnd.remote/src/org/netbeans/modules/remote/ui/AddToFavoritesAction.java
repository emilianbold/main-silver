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

package org.netbeans.modules.remote.ui;

import java.awt.Frame;
import java.io.IOException;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.favorites.api.Favorites;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Vladimir Voskresensky
 */
@ActionID(id = "org.netbeans.modules.remote.ui.AddToFavoritesAction", category = "NativeRemote")
@ActionRegistration(displayName = "AddToFavoritesMenuItem")
@ActionReference(path = "Remote/Host/Actions", name = "AddToFavoritesAction", position = 600)
public class AddToFavoritesAction extends SingleHostAction {
    private JMenu popupMenu;
    
    @Override
    public String getName() {
        return NbBundle.getMessage(HostListRootNode.class, "AddToFavoritesMenuItem"); // NOI18N
    }

    @Override
    protected void performAction(final ExecutionEnvironment env, Node node) {
        FileSystem fs = FileSystemProvider.getFileSystem(env);
        FileObject fo = fs.getRoot();
        if (!Favorites.getDefault().isInFavorites(fo)) {
            try {
                Favorites.getDefault().add(fo);
            } catch (NullPointerException ex) {
            } catch (DataObjectNotFoundException ex) {
            }
        }        
    }

    @Override
    public boolean isVisible(Node node) {
        TopComponent favoritesComponent = getFavorites();
        return favoritesComponent != null && isRemote(node);
    }

    private static TopComponent getFavorites() {
        TopComponent favoritesComponent = WindowManager.getDefault().findTopComponent("favorites"); // NOI18N
        return favoritesComponent;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        createSubMenu();
        return popupMenu;
    }
        
    private void createSubMenu() {
        if (popupMenu == null) {
            popupMenu = new JMenu(getName());
            popupMenu.add(new AddHome().getPopupPresenter());
//            popupMenu.add(new AddProjects().getPopupPresenter());
            popupMenu.add(new AddMirror().getPopupPresenter());
            popupMenu.add(new AddRoot().getPopupPresenter());
            popupMenu.add(new AddOther().getPopupPresenter());
        }
    }    
    
    private enum PLACE {
        ROOT("AddRoot"),// NOI18N
        HOME("AddHome"),// NOI18N
        PROJECTS("AddProjects"),// NOI18N
        OTHER("AddOtherFolder"), // NOI18N
        MIRROR("AddMirror");// NOI18N
        
        private final String name;
        PLACE(String nameKey) {
            this.name = NbBundle.getMessage(AddToFavoritesAction.class, nameKey);
        }
        
        private String getName() {
            return name;
        }
    }
    
    private static abstract class AddPlace extends SingleHostAction {
        private final PLACE place;
        private AddPlace(PLACE place) {
            this.place = place;
            putProperty("noIconInMenu", Boolean.TRUE);// NOI18N
        }

        protected abstract FileObject getRoot(ExecutionEnvironment env, FileSystem fs);
        protected abstract String getPath(ExecutionEnvironment env);
        
        @Override
        protected void performAction(final ExecutionEnvironment env, Node node) {
            final TopComponent favorites = getFavorites();
            if (favorites != null) {
                Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        try {
                            ConnectionManager.getInstance().connectTo(env);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                            return;
                        } catch (CancellationException ex) {
                            // don't report CancellationException
                            return;
                        }
                        FileSystem fs = FileSystemProvider.getFileSystem(env);
                        final FileObject fo = getRoot(env, fs);
                        if (fo != null) {
                            Runnable openFavorites = new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        Favorites.getDefault().selectWithAddition(fo);
                                    } catch (DataObjectNotFoundException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            };
                            SwingUtilities.invokeLater(openFavorites);
                        } else {
                            String path = getPath(env);
                            if (path != null) {
                                String msg;
                                if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                                    msg = NbBundle.getMessage(AddToFavoritesAction.class, "NotConnected", path, env.getDisplayName());
                                } else {
                                    msg = NbBundle.getMessage(AddToFavoritesAction.class, "NoRemotePath", path);
                                }
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
                            }
                        }
                    }
                };
                RequestProcessor.getDefault().post(runnable);
            }
        }

        @Override
        public String getName() {
            return place.getName();
        }
    }
    
    private static final class AddRoot extends AddPlace {
        public AddRoot() {
            super(PLACE.ROOT);
        }

        @Override
        protected FileObject getRoot(ExecutionEnvironment env, FileSystem fs) {
            return fs.getRoot();
        }

        @Override
        protected String getPath(ExecutionEnvironment env) {
            return "/"; // NOI18N
        }
    }
    private static final class AddHome extends AddPlace {

        public AddHome() {
            super(PLACE.HOME);
        }

        @Override
        protected FileObject getRoot(ExecutionEnvironment env, FileSystem fs) {
            String path = getPath(env);
            return path == null ? null : fs.findResource(path);
        }
        
        @Override
        protected String getPath(ExecutionEnvironment env) {
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                if (hostInfo != null) {
                    String userDir = hostInfo.getUserDir();
                    return userDir;
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (CancellationException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
    }
    private static final class AddProjects extends AddPlace {

        public AddProjects() {
            super(PLACE.PROJECTS);
        }

        @Override
        protected FileObject getRoot(ExecutionEnvironment env, FileSystem fs) {
            return fs.getRoot();
        }
        
        @Override
        protected String getPath(ExecutionEnvironment env) {
            return "/"; // NOI18N
        }
    }
    private static final class AddMirror extends AddPlace {

        public AddMirror() {
            super(PLACE.MIRROR);
        }

        @Override
        protected FileObject getRoot(ExecutionEnvironment env, FileSystem fs) {
            String path = getPath(env);
            return path == null ? null : fs.findResource(path);
        }
        
        @Override
        protected String getPath(ExecutionEnvironment env) {
            String remoteSyncRoot = RemotePathMap.getRemoteSyncRoot(env);
            return remoteSyncRoot;
        }
    }
    
    private static final class AddOther extends AddPlace {

        private final Frame mainWindow;

        public AddOther() {
            super(PLACE.OTHER);
            mainWindow = WindowManager.getDefault().getMainWindow();
        }

        @Override
        protected FileObject getRoot(ExecutionEnvironment env, FileSystem fs) {
            String title = NbBundle.getMessage(AddToFavoritesAction.class, "SelectFolder");
            String btn = NbBundle.getMessage(AddToFavoritesAction.class, "AddText");
            FileObject fo = OpenTerminalAction.getRemoteFileObject(env, title, btn, mainWindow);
            return fo;
        }

        @Override
        protected String getPath(ExecutionEnvironment env) {
            return null;
        }
    }    
}
