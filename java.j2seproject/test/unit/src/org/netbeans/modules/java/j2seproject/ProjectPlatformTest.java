/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2seproject;

import java.io.File;
import java.io.IOException;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seproject.api.J2SEProjectPlatform;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tomas Zezula
 */
public final class ProjectPlatformTest extends NbTestCase {
    
    private File wd;

    public ProjectPlatformTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.setLayersAndInstances();
        clearWorkDir();
        wd = FileUtil.normalizeFile(getWorkDir());
    }
    
    public void testProjectPlatform() throws Exception {
        final Project p = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Project>() {
            @Override
            public Project run() throws Exception {
                final Project[] prjHolder = new Project[1];
                FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
                    @Override
                    public void run() throws IOException {
                        final AntProjectHelper helper = J2SEProjectGenerator.createProject(
                                getWorkDir(),
                                "test",
                                null,
                                null,
                                null,
                                false);
                        final EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        props.setProperty(ProjectProperties.PLATFORM_ACTIVE, "MyTestPlatform");
                        props.setProperty("platforms.MyTestPlatform.home", getJdkHome().getAbsolutePath());
                        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                        final Project p = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
                        ProjectManager.getDefault().saveProject(p);
                        prjHolder[0] = p;
                    }
                });
                return prjHolder[0];
            }
        });
        assertNotNull(p);
        
        final J2SEProjectPlatform projectPlatform = p.getLookup().lookup(J2SEProjectPlatform.class);
        assertNotNull(projectPlatform);
        final JavaPlatform activePlatform = projectPlatform.getProjectPlatform();
        assertNotNull(activePlatform);
        assertNotSame(activePlatform, JavaPlatformManager.getDefault().getDefaultPlatform());
        assertEquals("MyTestPlatform", activePlatform.getDisplayName());
        
        final J2SEProject j2se = p.getLookup().lookup(J2SEProject.class);
        assertNotNull(j2se);
        final ClassPath[] cps = j2se.getClassPathProvider().getProjectClassPaths(ClassPath.BOOT);
        assertEquals(1, cps.length);
        assertNotNull(cps[0]);
        assertFalse(cps[0].entries().isEmpty());    
    }
    
    private static File getJdkHome() {
        File javaHome = FileUtil.normalizeFile(new File(System.getProperty("java.home")));
        return javaHome;
    }
    
//    private static boolean hasJavac(@NonNull final File home) {
//        final File bin = new File (home, "bin");                //NOI18N
//        final File[] children = bin.listFiles();
//        if (children != null) {
//            for (File cld : children) {
//                if ("javac".equals(name(cld.getName()))) {      //NOI18N
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//    
//    private static String name(String nameWithExt) {
//        final int index = nameWithExt.lastIndexOf('.'); //NOI18N
//        return index > 0 ?
//                nameWithExt.substring(0, index) :
//                nameWithExt;
//    }
    
}
