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

package org.netbeans.modules.profiler.j2ee.selector.nodes.ejb.entity;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.java.ProfilerTypeUtils;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.j2ee.impl.icons.JavaEEIcons;
import org.netbeans.modules.profiler.nbimpl.javac.ClasspathInfoFactory;
import org.netbeans.modules.profiler.projectsupport.utilities.ProjectUtilities;
import org.netbeans.modules.profiler.selector.api.nodes.ContainerNode;
import org.netbeans.modules.profiler.selector.api.nodes.GreedySelectorChildren;
import org.netbeans.modules.profiler.selector.api.nodes.SelectorChildren;
import org.netbeans.modules.profiler.selector.api.nodes.SelectorNode;


/**
 *
 * @author Jaroslav Bachorik
 */
public class JPAEntitiesNode extends ContainerNode {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class Children extends GreedySelectorChildren<JPAEntitiesNode> {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        protected List<SelectorNode> prepareChildren(final JPAEntitiesNode parent) {
            final List<SelectorNode> entityBeans = new ArrayList<SelectorNode>();
            
            final Project project = parent.getLookup().lookup(Project.class);
            for (MetadataModel<EntityMappingsMetadata> mdModel : listAllMetadata(project)) {
                try {
                    entityBeans.addAll(mdModel.runReadAction(new MetadataModelAction<EntityMappingsMetadata, List<SelectorNode>>() {
                            public List<SelectorNode> run(EntityMappingsMetadata metadata)
                                                   throws Exception {
                                final List<SelectorNode> beanList = new ArrayList<SelectorNode>();

                                Entity[] entities = metadata.getRoot().getEntity();

                                for (Entity entity : entities) {
                                    final Entity entityBean = entity;
                                    SourceClassInfo jpa = ProfilerTypeUtils.resolveClass(entityBean.getClass2(), project);
                                    beanList.add(new EntityBeanNode(jpa, entityBean.getName(), Icons.getIcon(JavaEEIcons.CLASS), parent));
                                }

                                return beanList;
                            }
                        }));
                } catch (MetadataModelException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            return entityBeans;
        }
    }

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of SessionBeansNode */
    @NbBundle.Messages("JPAEntitiesNode_JPAEntityString=JPA Entities")
    public JPAEntitiesNode(final ContainerNode parent) {
        super(Bundle.JPAEntitiesNode_JPAEntityString(), Icons.getIcon(JavaEEIcons.PACKAGE), parent);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    protected SelectorChildren getChildren() {
        return new Children();
    }

    private static Set<MetadataModel<EntityMappingsMetadata>> listAllMetadata(final Project project) {
        final Set<MetadataModel<EntityMappingsMetadata>> metadata = new HashSet<MetadataModel<EntityMappingsMetadata>>();

        Set<Project> projects = new HashSet<Project>();

        projects.add(project);
        ProjectUtilities.fetchSubprojects(project, projects);

        for (Project subProj : projects) {
            PersistenceScopes scopes = PersistenceScopes.getPersistenceScopes(subProj);

            if (scopes != null) {
                for (PersistenceScope scope : scopes.getPersistenceScopes()) {
                    try {
                        Persistence persistence = PersistenceMetadata.getDefault().getRoot(scope.getPersistenceXml());

                        for (PersistenceUnit pu : persistence.getPersistenceUnit()) {
                            metadata.add(scope.getEntityMappingsModel(pu.getName()));
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }

        return metadata;
    }
}
