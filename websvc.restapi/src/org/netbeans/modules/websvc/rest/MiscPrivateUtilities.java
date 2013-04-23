/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.rest;

import com.sun.source.tree.MethodTree;
import com.sun.source.util.SourcePositions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.rest.model.api.RestApplication;
import org.netbeans.modules.websvc.rest.spi.MiscUtilities;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 *
 * @author david
 */
public class MiscPrivateUtilities {
    // copy pasted from Maven project:
    public static final String DEVNULL = "DEV-NULL"; //NOI18N

    public static void setProjectProperty(final Project project, final AntProjectHelper helper, final String name, final String value, final String propertyPath) {
        if (helper == null) {
            return;
        }
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                @Override
                public Object run() throws IOException {
                    try {
                        EditableProperties ep = helper.getProperties(propertyPath);
                        ep.setProperty(name, value);
                        helper.putProperties(propertyPath, ep);
                        ProjectManager.getDefault().saveProject(project);
                    } catch (IOException ioe) {
                        Logger.getLogger(MiscPrivateUtilities.class.getName()).log(Level.INFO, ioe.getLocalizedMessage(), ioe);
                    }
                    return null;
                }
            });
        } catch (MutexException e) {
            Logger.getLogger(MiscUtilities.class.getName()).log(Level.INFO, null, e);
        }
    }

    public static ClassPath getClassPath(Project project, String type) {
        ClassPathProvider provider = project.getLookup().lookup(ClassPathProvider.class);
        if (provider == null) {
            return null;
        }
        Sources sources = project.getLookup().lookup(Sources.class);
        if (sources == null) {
            return null;
        }
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        List<ClassPath> classPaths = new ArrayList<ClassPath>(sourceGroups.length);
        for (SourceGroup sourceGroup : sourceGroups) {
            String sourceGroupId = sourceGroup.getName();
            if (sourceGroupId != null && sourceGroupId.contains("test")) {
                // NOI18N
                continue;
            }
            FileObject rootFolder = sourceGroup.getRootFolder();
            ClassPath path = provider.findClassPath(rootFolder, type);
            classPaths.add(path);
        }
        return ClassPathSupport.createProxyClassPath(classPaths.toArray(new ClassPath[classPaths.size()]));
    }

    public static boolean hasResource(Project project, String resource) {
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sgs.length < 1) {
            return false;
        }
        FileObject sourceRoot = sgs[0].getRootFolder();
        ClassPath classPath = ClassPath.getClassPath(sourceRoot, ClassPath.COMPILE);
        if (classPath == null) {
            return false;
        }
        FileObject resourceFile = classPath.findResource(resource);
        if (resourceFile != null) {
            return true;
        }
        return false;
    }

    public static void removeProperty(final AntProjectHelper helper, String[] propertyNames, String propertiesPath) {
        EditableProperties ep = helper.getProperties(propertiesPath);
        for (String name : propertyNames) {
            ep.remove(name);
        }
        helper.putProperties(propertiesPath, ep);
    }

    public static boolean hasApplicationResourceClass(RestSupport restSupport, final String fqn){
        List<RestApplication> applications = restSupport.getRestApplications();
        if ( applications.isEmpty() ){
            return false;
        }
        final String clazz = applications.get(0).getApplicationClass();
        final boolean[] has = new boolean[1];
        try {
            JavaSource javaSource = getJavaSourceFromClassName(restSupport.getProject(), clazz);

            if (javaSource == null ){
                return false;
            }
            javaSource.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run( final CompilationController controller )
                        throws Exception
                {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

                    TypeElement typeElement = controller.getElements()
                            .getTypeElement(clazz);
                    if (typeElement == null) {
                        return;
                    }
                    TypeElement restResource = controller.getElements()
                            .getTypeElement(fqn);
                    if (restResource == null) {
                        return;
                    }
                    List<ExecutableElement> methods = ElementFilter
                            .methodsIn(typeElement.getEnclosedElements());
                    ExecutableElement getClasses = null;
                    for (ExecutableElement method : methods) {
                        if ((method.getSimpleName().contentEquals(ApplicationSubclassGenerator.GET_REST_RESOURCE_CLASSES) ||
                            method.getSimpleName().contentEquals(ApplicationSubclassGenerator.GET_REST_RESOURCE_CLASSES2))
                                && method.getParameters().isEmpty())
                        {
                            getClasses = method;
                            break;
                        }
                    }
                    if (getClasses == null) {
                        return;
                    }

                    final String className = restResource.getQualifiedName()
                            .toString() + ".class"; // NOI18N
                    final MethodTree tree = controller.getTrees().getTree(
                            getClasses);
                    final Document doc = controller.getDocument();
                    if ( doc ==null){
                        return;
                    }
                    doc.render(new Runnable() {

                        @Override
                        public void run() {
                            SourcePositions srcPos = controller.getTrees()
                                    .getSourcePositions();
                            int start = (int) srcPos.getStartPosition(
                                    controller.getCompilationUnit(), tree);
                            int end = (int) srcPos.getEndPosition(
                                    controller.getCompilationUnit(), tree);

                            try {
                                String text = doc.getText(start, end - start + 1);
                                if (text.contains(className)) {
                                    has[0] = true;
                                }
                            }
                            catch(BadLocationException e ){
                                // should not happen inside document lock
                                assert false;
                            }
                        }
                    });

                    /*
                     * List<? extends ImportTree> imports =
                     * controller.getCompilationUnit().getImports(); for
                     * (ImportTree importTree : imports) { importTree. }
                     */
                }

            }, true);
        }
        catch(IOException e ){
            Logger.getLogger(RestSupport.class.getName()).log(
                    Level.INFO, e.getLocalizedMessage(), e);
        }

        return has[0];
    }

    private static FileObject getFileObjectFromClassName(Project p, String qualifiedClassName)
            throws IOException
    {
        FileObject root = MiscUtilities.findSourceRoot(p);
        ClasspathInfo cpInfo = ClasspathInfo.create(root);
        ClassIndex ci = cpInfo.getClassIndex();
        int beginIndex = qualifiedClassName.lastIndexOf('.')+1;
        String simple = qualifiedClassName.substring(beginIndex);
        Set<ElementHandle<TypeElement>> handles = ci.getDeclaredTypes(
                simple, ClassIndex.NameKind.SIMPLE_NAME,
                Collections.singleton(ClassIndex.SearchScope.SOURCE));
        if ( handles == null ){
            return null;
        }
        for (ElementHandle<TypeElement> handle : handles) {
            if (qualifiedClassName.equals(handle.getQualifiedName())) {
                return SourceUtils.getFile(handle, cpInfo);
            }
        }
        return null;
    }

    static JavaSource getJavaSourceFromClassName(Project p, String qualifiedClassName)
            throws IOException
    {
        FileObject fo = getFileObjectFromClassName(p, qualifiedClassName);
        if (fo != null) {
            return JavaSource.forFileObject(fo);
        } else {
            return null;
        }
    }


}
