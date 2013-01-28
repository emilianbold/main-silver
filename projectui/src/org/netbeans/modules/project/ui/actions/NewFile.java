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

package org.netbeans.modules.project.ui.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu.Separator;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.NewFileWizard;
import org.netbeans.modules.project.ui.NoProjectNew;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.OpenProjectList.TemplateItem;
import org.netbeans.modules.project.ui.ProjectUtilities;
import static org.netbeans.modules.project.ui.actions.Bundle.*;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter.Popup;

/** Action for invoking the project sensitive NewFile Wizard
 */
@Messages({
    "LBL_NewFileAction_Name=&New File...",
    "LBL_NewFileAction_PopupName=New",
    "# {0} - Name of the template", "LBL_NewFileAction_Template_PopupName={0}..."
})
public class NewFile extends ProjectAction implements PropertyChangeListener, Popup {

    private static final RequestProcessor RP = new RequestProcessor(NewFile.class);
    private static final RequestProcessor INSTANTIATE_RP = new RequestProcessor(NewFile.class.getName() + ".INSTANTIATE_RP", 5);
    private static final Logger LOG = Logger.getLogger(NewFile.class.getName());

    private JMenu subMenu;

    public NewFile() {
        this( null );
    }

    @Messages("LBL_NewFileAction_Tooltip=New File...")
    public NewFile( Lookup context ) {
        super((String) null, LBL_NewFileAction_Name(), null, context);
        putValue("iconBase","org/netbeans/modules/project/ui/resources/newFile.png"); //NOI18N
        putValue(SHORT_DESCRIPTION, LBL_NewFileAction_Tooltip());
        OpenProjectList.getDefault().addPropertyChangeListener( WeakListeners.propertyChange( this, OpenProjectList.getDefault() ) );
        refresh(getLookup(), true);
    }

    @Override
    protected void refresh(Lookup context, boolean immediate) {
        // #59615: update synch if possible; only replan if not already in EQ.
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
//issue 192354 - getOpenProjects() is expensive at times, uses read mutex on projects.
//                setEnabled(OpenProjectList.getDefault().getOpenProjects().length > 0);
                setDisplayName(LBL_NewFileAction_Name());
            }
        });
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem menu = new JMenuItem(this);
        menu.setIcon(null);
        Mnemonics.setLocalizedText(menu, (String) getValue(Action.NAME));
        // XXX accelerator not displayed here for some reason...why???
        return menu;
    }

    //private NewFileWizard wizardIterator;

    @Override
    protected void actionPerformed( Lookup context ) {
        doPerform( context, null, true );
    }

    private void doPerform( Lookup context, final DataObject template, boolean inProject ) {

        if ( context == null ) {
            context = getLookup();
        }

        if ( !inProject ) {
            // Context outside of projects
            NoProjectNew.showDialog( template, preselectedFolder( context ) );
            return;
        }
        
        if (OpenProjectList.getDefault().getOpenProjects().length == 0) {
            // Can sometimes happen when pressing Ctrl-N, it seems.
            return;
        }

        final NewFileWizard wd = new NewFileWizard( preselectedProject( context ) /* , null */ );
        
        DataFolder preselectedFolder = preselectedFolder( context );
        if ( preselectedFolder != null ) {
            wd.setTargetFolder( preselectedFolder );
        }

        INSTANTIATE_RP.post(new Runnable() {
            @Override public void run() {
        try {
            Set resultSet = template == null ? wd.instantiate () : wd.instantiate( template );

            if (resultSet == null || resultSet.isEmpty ()) {
                // no new object, no work
                return ;
            }

            Iterator it = resultSet.iterator ();

            while (it.hasNext ()) {
                Object obj = it.next ();
                DataObject newDO = null;
                if (obj instanceof DataObject) {
                    newDO = (DataObject) obj;
                } else if (obj instanceof FileObject) {
                    try {
                        newDO = DataObject.find ((FileObject) obj);
                    } catch (DataObjectNotFoundException x) {
                        // XXX
                        assert false : obj;
                    }
                } else {
                    assert false : obj;
                }
                if (newDO != null) {
                    ProjectUtilities.openAndSelectNewObject (newDO);
                }
            }
        }
        catch ( IOException e ) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, e);
        }

        // Update the Templates LRU for given project
        //Project project = Templates.getProject( wd );
        FileObject foTemplate = Templates.getTemplate( wd );
        OpenProjectList.getDefault().updateTemplatesLRU( foTemplate );
            }
        });
    }

    // Context Aware action implementation -------------------------------------

    @Override
    public Action createContextAwareInstance( Lookup actionContext ) {
        return new NewFile( actionContext );
    }

    // Presenter.Popup implementation ------------------------------------------

    public JMenuItem getSubmenuPopupPresenter() {
        if (subMenu == null) {
            subMenu = new JMenu(LBL_NewFileAction_PopupName());
        }
        return subMenu;
    }

    protected void fillSubMenu() {
        Project projects[] = ActionsUtil.getProjectsFromLookup( getLookup(), null );
        if ( projects != null && projects.length > 0 ) {
            fillSubMenu(subMenu, projects[0]);
        }
        else {
            // When no project is seleceted only file and folder can be created
            fillNonProjectSubMenu(subMenu);
        }
    }

    // Private methods ---------------------------------------------------------

    @NonNull
    private Project preselectedProject( Lookup context ) {
        Project preselectedProject = null;

        // if ( activatedNodes != null && activatedNodes.length != 0 ) {

        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );
        if ( projects.length > 0 ) {
            preselectedProject = projects[0];
        }


        if ( preselectedProject == null ) {
            // No project context => use main project
            preselectedProject = OpenProjectList.getDefault().getMainProject();
            if (preselectedProject == null && OpenProjectList.getDefault().getOpenProjects().length > 0) {
                // No main project => use the first one
                preselectedProject = OpenProjectList.getDefault().getOpenProjects()[0];
            }
        }

        if ( preselectedProject == null ) {
            assert false : "Action should be disabled"; // NOI18N
        }

        return preselectedProject;
    }

    private DataFolder preselectedFolder( Lookup context ) {

        DataFolder preselectedFolder = null;

        // Try to find selected folder
        preselectedFolder = context.lookup(DataFolder.class);
        if ( preselectedFolder == null ) {
            // No folder selectd try with DataObject
            DataObject dobj = context.lookup(DataObject.class);
            if ( dobj != null) {
                // DataObject found => we'll use the parent folder
                preselectedFolder = dobj.getFolder();
            }
        }

        LOG.log(Level.FINE, "#210919: found preselected folder {0} for {1}", new Object[] {preselectedFolder, context});
        return preselectedFolder;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        refresh(Lookup.EMPTY, false);
    }

    private static final String TEMPLATE_PROPERTY = "org.netbeans.modules.project.ui.actions.NewFile.Template"; // NOI18N
    private static final String IN_PROJECT_PROPERTY = "org.netbeans.modules.project.ui.actions.NewFile.InProject"; // NOI18N


    @Messages({
        "LBL_NewFileAction_File_PopupName=Other...",
        "NewFile.please_wait=Please wait..."
    })
    private void fillSubMenu(final JMenu menuItem, @NonNull final Project project) {
        menuItem.removeAll();
        JMenuItem wait = new JMenuItem(NewFile_please_wait());
        wait.setEnabled(false);
        menuItem.add(wait);
        RP.post(new Runnable() {
            @Override public void run() {
                final List<TemplateItem> items = OpenProjectList.prepareTemplates(project, getLookup());
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        menuItem.removeAll();
                        ActionListener menuListener = new PopupListener();
                        for (TemplateItem i : items) {
                            JMenuItem item = new JMenuItem(
                                    LBL_NewFileAction_Template_PopupName(i.displayName),
                                    i.icon);
                            item.addActionListener(menuListener);
                            item.putClientProperty(TEMPLATE_PROPERTY, i.template);
                            menuItem.add(item);
                        }
                        if (!items.isEmpty()) {
                            menuItem.add(new Separator());
                        }
                        JMenuItem fileItem = new JMenuItem(LBL_NewFileAction_File_PopupName(), (Icon) getValue(Action.SMALL_ICON));
                        fileItem.addActionListener(menuListener);
                        fileItem.putClientProperty(TEMPLATE_PROPERTY, null);
                        menuItem.add(fileItem);
                        // #205616 - need to refresh please wait node
                        menuItem.getPopupMenu().pack();
                    }
                });
            }
        });
    }

    private void fillNonProjectSubMenu(JMenu menuItem) {
        menuItem.removeAll();

        ActionListener menuListener = new PopupListener();

        DataFolder preselectedFolder = preselectedFolder( getLookup() );

        boolean canWrite;
        if ( preselectedFolder == null ) {
            canWrite = false;
        }
        else {
            FileObject pf = preselectedFolder.getPrimaryFile();
            canWrite = pf != null && pf.canWrite();
        }

        DataObject templates[] = NoProjectNew.getTemplates();
        for( int i = 0; i < templates.length; i++ ) {
            Node n = templates[i].getNodeDelegate();
            JMenuItem item = new JMenuItem(
                    LBL_NewFileAction_Template_PopupName(n.getDisplayName()),
                                      new ImageIcon( n.getIcon( BeanInfo.ICON_COLOR_16x16 ) ) );
            item.addActionListener( menuListener );
            item.putClientProperty( TEMPLATE_PROPERTY, templates[i] );
            item.putClientProperty( IN_PROJECT_PROPERTY, Boolean.FALSE );
            item.setEnabled( canWrite );
            menuItem.add( item );
        }
    }

    private class PopupListener implements ActionListener {

        @Override
        public void actionPerformed( ActionEvent e ) {
            JMenuItem source = (JMenuItem)e.getSource();

            Boolean inProject = (Boolean)source.getClientProperty( IN_PROJECT_PROPERTY );
            DataObject template = (DataObject)source.getClientProperty( TEMPLATE_PROPERTY );

            if ( inProject != null && inProject == Boolean.FALSE ) {
                doPerform( null, template, false );
            }
            else {
                doPerform( null, template, true );
            }
        }

    }

    /**
     * Variant for folder context menus that makes a submenu.
     */
    public static final class WithSubMenu extends NewFile {

        public WithSubMenu() {}

        private WithSubMenu(Lookup actionContext) {
            super(actionContext);
        }

        @Override
        public JMenuItem getPopupPresenter() {
            return new DynaMenu(LBL_NewFileAction_PopupName());
        }

        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            return new WithSubMenu(actionContext);
        }

        private final class DynaMenu extends JMenu implements DynamicMenuContent {

            public DynaMenu(String a) {
                super(a);
            }

            @Override
            public JComponent[] getMenuPresenters() {
                JComponent jc = getSubmenuPopupPresenter();
                fillSubMenu();
                return new JComponent[]{ jc };
            }

            @Override
            public JComponent[] synchMenuPresenters(JComponent[] items) {
                JComponent jc = getSubmenuPopupPresenter();
                fillSubMenu();
                return new JComponent[]{ jc };
            }
        }
    }


}
