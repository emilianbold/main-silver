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

package org.netbeans.modules.javascript.grunt.legacy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.Collator;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.grunt.legacy.TargetLister.Target;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 * Copy paste from ant project support Submenu which permits the user to run
 * various targets from the project. Distinction made between the main target,
 * other documented targets, and other undocumented targets.
 */
@MIMEResolver.Registration(displayName = "Gruntfile", resource = "grunt-resolver.xml", position = 123)
@ActionID(id = "org.netbeans.modules.web.clientproject.ui.action.RunTargetsAction", category = "Build")
@ActionRegistration(displayName = "#LBL_run_targets_action", lazy = false)
@ActionReferences(value = {
    @ActionReference(position = 900, path = "Editors/text/grunt+javascript/Popup"),
    @ActionReference(position = 150, path = "Loaders/text/grunt+javascript/Actions"),
    @ActionReference(path="Projects/org-netbeans-modules-web-clientproject/Actions", position = 180),
    @ActionReference(path="Projects/org-netbeans-modules-php-project/Actions", position = 670),
    @ActionReference(path="Projects/org-netbeans-modules-web-project/Actions", position = 670),
    @ActionReference(path="Projects/org-netbeans-modules-maven/Actions", position = 770)
})
public final class RunTargetsAction extends SystemAction implements ContextAwareAction {

    @Override
    public String getName () {
        return NbBundle.getMessage (RunTargetsAction.class, "LBL_run_targets_action");
    }

    @Override
    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //well, since someone can assign a shortcut ti the action, the invokation is unvaiodable, make it noop        
        //assert false : "Action should never be called without a context";
    }

    @Override
    public Action createContextAwareInstance(final Lookup actionContext) {
        if (actionContext.lookup(TargetLister.Target.class) != null) { //#220590
            final Target target = actionContext.lookup(TargetLister.Target.class);
            AbstractAction a = new AbstractAction(getName()) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        // XXX
                        //ClientSideProjectUtilities.logUsage(RunTargetsAction.class, "USG_GRUNT_BUILD", null);
                        new GruntfileExecutor(target.getOriginatingScript(), new String[]{target.getName()}).execute();
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            };

            a.putValue(ACCELERATOR_KEY, this.getValue(ACCELERATOR_KEY));
            return a;
        } else {
            return new ContextAction(actionContext);
        }
    }
    
    /**
     * The particular instance of this action for a given project.
     */
    private static final class ContextAction extends AbstractAction implements Presenter.Popup {
        
        private final FileObject file;
        
        public ContextAction(Lookup lkp) {
            super(SystemAction.get(RunTargetsAction.class).getName());
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            Collection<? extends FileObject> apcs = lkp.lookupAll(FileObject.class);
            FileObject _file = null;
            if (apcs.size() == 1) {
                _file = apcs.iterator().next();
                if (!_file.isValid()) {
                    _file = null;
                }
                Project project = lkp.lookup(Project.class);
                if (project!=null) {
                    _file = project.getProjectDirectory().getFileObject("Gruntfile.js");
                }
            }
            file = _file;
            super.setEnabled(file != null);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        //well, since someone can assign a shortcut ti the action, the invokation is unvaiodable, make it noop        
        //assert false : "Action should never be called without a context";
        }

        @Override
        public JMenuItem getPopupPresenter() {
            if (file != null) {
                return createMenu(file);
            } else {
                return new Actions.MenuItem(this, false);
            }
        }
    }

    /**
     * Create the submenu.
     */
    private static JMenu createMenu(FileObject gruntFile) {
        return new LazyMenu(gruntFile);
    }
    
    static final class LazyMenu extends JMenu {
        
        private final FileObject gruntFile;
        
        @NbBundle.Messages({
            "LBL_LoadingTasks=Loading Tasks...",
            "LBL_RefreshTasks=Reload Tasks"    
        })
        public LazyMenu(FileObject gruntFile) {
            //super(SystemAction.get(RunTargetsAction.class).getName());
            super(Bundle.LBL_LoadingTasks());
            this.gruntFile = gruntFile;
            try {
                boolean enabled = TargetLister.getTargets(LazyMenu.this, gruntFile) !=null; 
                if (enabled) {
                    setText(SystemAction.get(RunTargetsAction.class).getName());
                }
                setEnabled(enabled); 
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        @Override
        public JPopupMenu getPopupMenu() {
            rebuild();
            return super.getPopupMenu();
        }

        void rebuild() {
            Runnable rebuildTaks = new Runnable() {

                @Override
                public void run() {
                    LazyMenu.this.removeAll();
                    Collection<TargetLister.Target> allTargets = null;
                    try {
                        allTargets = TargetLister.getTargets(LazyMenu.this, gruntFile);
                    } catch (IOException e) {
                        // XXX how to notify properly?
                        Exceptions.printStackTrace(e);
                    }
                    if (allTargets==null) {
                        return;
                    }
                    String defaultTarget = null;
                    SortedSet<String> describedTargets = new TreeSet<>(Collator.getInstance());
                    SortedSet<String> otherTargets = new TreeSet<>(Collator.getInstance());
                    for (TargetLister.Target t : allTargets) {
                        if (t.isOverridden()) {
                            // Cannot be called.
                            continue;
                        }
                        if (t.isInternal()) {
                            // Don't present in GUI.
                            continue;
                        }
                        String name = t.getName();
                        if (t.isDefault()) {
                            defaultTarget = name;
                        } else if (t.isDescribed()) {
                            describedTargets.add(name);
                        } else {
                            otherTargets.add(name);
                        }
                    }
                    boolean needsep = false;
                    if (defaultTarget != null) {
                        needsep = true;
                        JMenuItem menuitem = new JMenuItem(defaultTarget);
                        menuitem.addActionListener(new TargetMenuItemHandler(gruntFile, defaultTarget));
                        add(menuitem);
                    }
                    if (needsep) {
                        needsep = false;
                        addSeparator();
                    }
                    if (!describedTargets.isEmpty()) {
                        needsep = true;
                        for (String target : describedTargets) {
                            JMenuItem menuitem = new JMenuItem(target);
                            menuitem.addActionListener(new TargetMenuItemHandler(gruntFile, target));
                            add(menuitem);
                        }
                    }
                    if (needsep) {
                        needsep = false;
                        addSeparator();
                    }
                    
                    add(new RefreshAction(gruntFile));
                    if (!otherTargets.isEmpty()) {
                        needsep = true;
                        JMenu submenu = new JMenu(NbBundle.getMessage(RunTargetsAction.class, "LBL_run_other_targets"));
                        for (String target : otherTargets) {
                            JMenuItem menuitem = new JMenuItem(target);
                            menuitem.addActionListener(new TargetMenuItemHandler(gruntFile, target));
                            submenu.add(menuitem);
                        }
                        add(submenu);
                    }
                    if (needsep) {
                        needsep = false;
                        addSeparator();
                    }
                    setEnabled(true);
                    setText(SystemAction.get(RunTargetsAction.class).getName());
                    validate();
                }
                
            };
            
            if (SwingUtilities.isEventDispatchThread()) {
                rebuildTaks.run();
            } else {
                SwingUtilities.invokeLater(rebuildTaks);
            }
        }
    }

    /**
     * Action handler for a menu item representing one target.
     */
    private static final class TargetMenuItemHandler implements ActionListener, Runnable {
        
        private final FileObject gruntFile;
        private final String target;
        
        public TargetMenuItemHandler(FileObject gruntFile, String target) {
            this.gruntFile = gruntFile;
            this.target = target;
        }
        
        @Override
        public void actionPerformed(ActionEvent ev) {
            // #16720 part 2: don't do this in the event thread...
            RequestProcessor.getDefault().post(this);
        }
        
        @Override
        public void run() {
            try {
                GruntfileExecutor te = new GruntfileExecutor(gruntFile, new String[] {target});
                te.execute();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }

    private static class RefreshAction extends AbstractAction {

        private FileObject gruntFile;
        public RefreshAction(FileObject gruntFile) {
            super(Bundle.LBL_RefreshTasks());
            this.gruntFile = gruntFile;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            TargetLister.invalidateCache(gruntFile);
        }
    }
}
    
