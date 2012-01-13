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
package org.netbeans.modules.profiler.categorization.api;

import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.lib.profiler.marker.Mark;
import org.netbeans.modules.profiler.categorization.api.definitions.CustomCategoryDefinition;
import org.netbeans.modules.profiler.categorization.api.definitions.PackageCategoryDefinition;
import org.netbeans.modules.profiler.categorization.api.definitions.SingleTypeCategoryDefinition;
import org.netbeans.modules.profiler.categorization.api.definitions.SubtypeCategoryDefinition;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Bachorik
 * @author ads changed by ads
 */
abstract public class CategoryBuilder {
    
    private static final Logger LOGGER = Logger.getLogger(CategoryBuilder.class.getName());
    private static final String CATEGORY_ATTRIB_CUSTOM = "custom"; // NOI18N
    private static final String CATEGORY_ATTRIB_EXCLUDES = "excludes"; // NOI18N
    private static final String CATEGORY_ATTRIB_INCLUDES = "includes"; // NOI18N
    private static final String CATEGORY_ATTRIB_INSTANCENAME = "instanceClass"; // NOI18N
    private static final String CATEGORY_ATTRIB_PREFIX = "."; // NOI18N
    private static final String CATEGORY_ATTRIB_SUBTYPES = "subtypes"; // NOI18N
    private static final String CATEGORY_ATTRIB_TYPE = "type"; // NOI18N
    private static final String CATEGORY_ATTRIB_PACKAGE = "package"; // NOI18N
    private static final String SHADOW_SUFFIX = "shadow";// NOI18N
    private CategoryContainer rootCategory = null;
    private Lookup.Provider project;
        
    public CategoryBuilder(@NonNull Lookup.Provider proj) {
        assert proj != null;
        project = proj;
    }

    final public synchronized Category getRootCategory() {
        if (rootCategory == null) {
            rootCategory = new CategoryContainer("ROOT", Bundle.ROOT_CATEGORY_NAME(), Mark.DEFAULT); // NOI18N

            FileObject aoi = FileUtil.getConfigFile("Projects/" + getProjectTypeId() + "/NBProfiler/Categories"); //NOI18N
            if (aoi != null) {
                Enumeration<? extends FileObject> folders = aoi.getChildren(false);
                while (folders.hasMoreElements()) {
                    FileObject folder = folders.nextElement();
                    processCategories(rootCategory, folder);
                }
            }
        }
        return rootCategory;
    }
    
    final protected Lookup.Provider getProject() {
        return project;
    }
    
    abstract protected String getProjectTypeId();

    private void processCategories(CategoryContainer container, FileObject node) {
        if (SHADOW_SUFFIX.equals(node.getExt())) {
            String reference = (String) node.getAttribute("originalFile");
            try {
                FileObject refNode = node.getFileSystem().findResource(reference);
                if (refNode != null) {
                    processCategories(container, refNode);
                }
            } catch (FileStateInvalidException e) {
                LOGGER.log(Level.SEVERE, "Can not process {0}", node.getPath()); // NOI18N
                LOGGER.throwing(CategoryBuilder.class.getName(), "processCategories", e); // NOI18N
            }
        } else if (node.isFolder()) {
            String label = (String)node.getAttribute("displayName"); // NOI18N
            label = label != null ? label : node.getName();

            CategoryContainer newCategory = new CategoryContainer(node.getPath(), label);
            container.add(newCategory);

            Enumeration<? extends FileObject> subNodes = node.getFolders(false);
            while (subNodes.hasMoreElements()) {
                FileObject subNode = subNodes.nextElement();
                String nodeName = subNode.getName();
                if (nodeName.startsWith(CATEGORY_ATTRIB_PREFIX)) {
                    if (nodeName.endsWith(CATEGORY_ATTRIB_SUBTYPES) 
                            && nodeName.length()==CATEGORY_ATTRIB_SUBTYPES.length()+1) {
                        Enumeration<? extends FileObject> definitions = subNode.getChildren(false);
                        while (definitions.hasMoreElements()) {
                            FileObject typeDef = definitions.nextElement();
                            String excludes = (String) typeDef.getAttribute(CATEGORY_ATTRIB_EXCLUDES);
                            String includes = (String) typeDef.getAttribute(CATEGORY_ATTRIB_INCLUDES);
                            String includesArr[] = includes != null ? includes.split(",") : null; // NOI18N
                            String excludesArr[] = excludes != null ? excludes.split(",") : null; // NOI18N
                            if (includesArr != null) {
                                for (int i = 0; i < includesArr.length; i++) {
                                    includesArr[i] = includesArr[i].trim();
                                }
                            }
                            if (excludesArr != null) {
                                for (int i = 0; i < excludesArr.length; i++) {
                                    excludesArr[i] = excludesArr[i].trim();
                                }
                            }

                            newCategory.getDefinitions().add(new SubtypeCategoryDefinition(newCategory, typeDef.getNameExt(), includesArr, excludesArr));
                        }
                    } else if (nodeName.endsWith(CATEGORY_ATTRIB_TYPE)&&
                            nodeName.length()==CATEGORY_ATTRIB_TYPE.length()+1) {
                        Enumeration<? extends FileObject> definitions = subNode.getChildren(false);
                        while (definitions.hasMoreElements()) {
                            FileObject typeDef = definitions.nextElement();

                            String excludes = (String) typeDef.getAttribute(CATEGORY_ATTRIB_EXCLUDES);
                            String includes = (String) typeDef.getAttribute(CATEGORY_ATTRIB_INCLUDES);
                            String includesArr[] = includes != null ? includes.split(",") : null; // NOI18N
                            String excludesArr[] = excludes != null ? excludes.split(",") : null; // NOI18N
                            if (includesArr != null) {
                                for (int i = 0; i < includesArr.length; i++) {
                                    includesArr[i] = includesArr[i].trim();
                                }
                            }
                            if (excludesArr != null) {
                                for (int i = 0; i < excludesArr.length; i++) {
                                    excludesArr[i] = excludesArr[i].trim();
                                }
                            }

                            newCategory.getDefinitions().add(new SingleTypeCategoryDefinition(newCategory, typeDef.getNameExt(), includesArr, excludesArr));
                        }
                    } else if (nodeName.endsWith(CATEGORY_ATTRIB_CUSTOM)
                            && nodeName.length()==CATEGORY_ATTRIB_CUSTOM.length()+1) 
                    {
                        String instanceClass = (String) subNode.getAttribute(CATEGORY_ATTRIB_INSTANCENAME);
                        if (instanceClass != null) {
                            Exception thrownException = null;
                            try {
                                ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
                                Class<CustomMarker> markerClz = (Class<CustomMarker>) cl.loadClass(instanceClass);
                                CustomMarker marker = markerClz.getConstructor(Lookup.Provider.class, Mark.class).newInstance(project, newCategory.getAssignedMark());
                                if (marker != null) {
                                    newCategory.getDefinitions().add(new CustomCategoryDefinition(newCategory, marker));
                                }
                            } catch (InstantiationException ex) {
                                thrownException = ex;
                            } catch (IllegalAccessException ex) {
                                thrownException = ex;
                            } catch (IllegalArgumentException ex) {
                                thrownException = ex;
                            } catch (InvocationTargetException ex) {
                                thrownException = ex;
                            } catch (NoSuchMethodException ex) {
                                thrownException = ex;
                            } catch (SecurityException ex) {
                                thrownException = ex;
                            } catch (ClassNotFoundException ex) {
                                thrownException = ex;
                            }
                            if (thrownException != null) {
                                LOGGER.logp(Level.WARNING, CategoryBuilder.class.getName(), "processCategories", "Error while building profiling results categories", thrownException); // NOI18N
                            }
                        }
                    } else if (nodeName.endsWith(CATEGORY_ATTRIB_PACKAGE)
                            && nodeName.length()==CATEGORY_ATTRIB_PACKAGE.length()+1) 
                    {
                        Enumeration<? extends FileObject> definitions = subNode.getChildren(false);
                        while (definitions.hasMoreElements()) {
                            FileObject packageDef = definitions.nextElement();
                            Boolean recursive = (Boolean) packageDef.getAttribute("recursive"); // NOI18N
                            newCategory.getDefinitions().add(new PackageCategoryDefinition(newCategory, packageDef.getNameExt(), recursive != null ? recursive.booleanValue() : true));
                        }
                    }
                    else {
                        LOGGER.logp(Level.SEVERE, CategoryBuilder.class.getName(), 
                                "processCategories", "Unkown category attribute name :"+
                                nodeName); // NOI18N
                    }
                } else {
                    processCategories(newCategory, subNode);
                }
            }
        }
    }
}
