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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.nodes;

import org.netbeans.modules.maven.spi.nodes.NodeUtils;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * display the modules for pom packaged project
 * @author Milos Kleint 
 */
public class ModulesNode extends AbstractNode {

    /** Creates a new instance of ModulesNode */
    public ModulesNode(NbMavenProjectImpl proj) {
        super(Children.create(new ModulesChildFactory(proj), true));
        setName("Modules"); //NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(ModulesNode.class, "LBL_Modules"));
    }

    @Override
    public Action[] getActions(boolean bool) {
        return new Action[]{};
    }

    private Image getIcon(boolean opened) {
        Image badge = ImageUtilities.loadImage("org/netbeans/modules/maven/modules-badge.png", true); //NOI18N
        return ImageUtilities.mergeImages(NodeUtils.getTreeFolderIcon(opened), badge, 8, 8);
    }

    @Override
    public Image getIcon(int type) {
        return getIcon(false);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(true);
    }

    static class Wrapper {
        boolean isPOM;
        LogicalViewProvider provider;
        NbMavenProjectImpl proj;
    }
    
   
    
    static class ModulesChildFactory extends ChildFactory<Wrapper>{
        private final NbMavenProjectImpl project;
        private final PropertyChangeListener listener;
        
         ModulesChildFactory(NbMavenProjectImpl proj) {
            project = proj;
            NbMavenProject watcher = project.getProjectWatcher();
            listener = new PropertyChangeListener() {
                                       @Override
                                       public void propertyChange(PropertyChangeEvent evt) {
                                           if (NbMavenProjectImpl.PROP_PROJECT.equals(evt.getPropertyName())) {
                                               refresh(false);
                                           }
                                       }
                                   };
             
            watcher.addPropertyChangeListener(WeakListeners.propertyChange(listener, watcher));                       
            
 
        }
         
        @Override
        protected boolean createKeys(final List<Wrapper> modules) {
            File base = project.getOriginalMavenProject().getBasedir();
            for (Iterator it = project.getOriginalMavenProject().getModules().iterator(); it.hasNext();) {
                String elem = (String) it.next();
                File projDir = FileUtil.normalizeFile(new File(base, elem));
                FileObject fo = FileUtil.toFileObject(projDir);
                if (fo != null) {
                    try {
                        Project prj = ProjectManager.getDefault().findProject(fo);
                        if (prj != null && prj.getLookup().lookup(NbMavenProjectImpl.class) != null) {
                            Wrapper wr = new Wrapper();
                            wr.proj = (NbMavenProjectImpl) prj;
                            wr.isPOM = "pom".equals(wr.proj.getOriginalMavenProject().getPackaging()); //NOI18N
                            wr.provider = prj.getLookup().lookup(LogicalViewProvider.class);
                            modules.add(wr);
                        }
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace();//TODO log ?
                    } catch (IOException ex) {
                        ex.printStackTrace();//TODO log ?
                    }
                } else {
                    //TODO broken module reference.. show as such..
                }

            }
            
            return true;

        }

        @Override
        protected Node createNodeForKey(Wrapper wr) {
             return new ProjectFilterNode(project, wr.proj, wr.provider.createLogicalView(), wr.isPOM);
        }
        
        
    }
  
    private static class ProjectFilterNode extends FilterNode {

        private NbMavenProjectImpl project;
        private NbMavenProjectImpl parent;

        ProjectFilterNode(NbMavenProjectImpl parent, NbMavenProjectImpl proj, Node original, boolean isPom) {
            super(original, isPom ? Children.create(new ModulesChildFactory(proj), true) : Children.LEAF);
//            disableDelegation(DELEGATE_GET_ACTIONS);
            project = proj;
            this.parent = parent;
        }

        @Override
        public Action[] getActions(boolean b) {
            ArrayList<Action> lst = new ArrayList<Action>();
            lst.add(OpenProjectAction.SINGLETON);
            lst.add(new RemoveModuleAction(parent, project));
//            lst.addAll(Arrays.asList(super.getActions(b)));
            return lst.toArray(new Action[lst.size()]);
        }

        @Override
        public Action getPreferredAction() {
            return OpenProjectAction.SINGLETON;
        }
    }

    private static class RemoveModuleAction extends AbstractAction {

        private NbMavenProjectImpl project;
        private NbMavenProjectImpl parent;

        public RemoveModuleAction(NbMavenProjectImpl parent, NbMavenProjectImpl proj) {
            putValue(Action.NAME, org.openide.util.NbBundle.getMessage(ModulesNode.class, "BTN_Remove_Module"));
            project = proj;
            this.parent = parent;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(org.openide.util.NbBundle.getMessage(ModulesNode.class, "MSG_Remove_Module"), NotifyDescriptor.YES_NO_OPTION);
            Object ret = DialogDisplayer.getDefault().notify(nd);
            if (ret == NotifyDescriptor.YES_OPTION) {
                FileObject fo = FileUtil.toFileObject(parent.getPOMFile());
                ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                    @Override
                    public void performOperation(POMModel model) {
                        List<String> modules = model.getProject().getModules();
                        if (modules != null) {
                            for (String path : modules) {
                                File rel = new File(parent.getPOMFile().getParent(), path);
                                File norm = FileUtil.normalizeFile(rel);
                                FileObject folder = FileUtil.toFileObject(norm);
                                if (folder != null && folder.equals(project.getProjectDirectory())) {
                                    model.getProject().removeModule(path);
                                    break;
                                }
                            }
                        }
                    }
                };
                org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(fo, Collections.singletonList(operation));
                //TODO is the manual reload necessary if pom.xml file is being saved?
                NbMavenProject.fireMavenProjectReload(project);
            }
        }
    }

    private static class OpenProjectAction extends AbstractAction implements ContextAwareAction {

        static final OpenProjectAction SINGLETON = new OpenProjectAction();

        private OpenProjectAction() {}

        public @Override void actionPerformed(ActionEvent e) {
            assert false;
        }

        public @Override Action createContextAwareInstance(final Lookup context) {
            return new AbstractAction(NbBundle.getMessage(ModulesNode.class, "BTN_Open_Project")) {
                public @Override void actionPerformed(ActionEvent e) {
                    Collection<? extends NbMavenProjectImpl> projects = context.lookupAll(NbMavenProjectImpl.class);
                    OpenProjects.getDefault().open(projects.toArray(new NbMavenProjectImpl[projects.size()]), false, true);
                }
            };
        }
    }
}
