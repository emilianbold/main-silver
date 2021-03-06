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

package org.netbeans.modules.maven.classpath;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.test.MockPropertyChangeListener;

public class ClassPathProviderImplTest extends NbTestCase {

    public ClassPathProviderImplTest(String n) {
        super(n);
    }

    private FileObject d;
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
        System.setProperty("test.reload.sync", "true");
    }

    public void testClassPath() throws Exception {
        TestFileUtils.writeFile(d,
                "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>jar</packaging>" +
                "<version>1.0-SNAPSHOT</version>" +
                "<name>Test</name>" +
                "</project>");
        FileObject src = FileUtil.createFolder(d, "src/main/java");
        assertRoots(ClassPath.getClassPath(src, ClassPath.COMPILE));
    }

    public void testSourcePathWithResources() throws Exception {
        TestFileUtils.writeFile(d,
                "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>jar</packaging>" +
                "<version>1.0-SNAPSHOT</version>" +
                "<name>Test</name>" +
                "</project>");
        FileObject src = FileUtil.createFolder(d, "src/main/java");
        FileObject rsrc = FileUtil.createFolder(d, "src/main/resources");
        FileObject tsrc = FileUtil.createFolder(d, "src/test/java");
        FileObject trsrc = FileUtil.createFolder(d, "src/test/resources");
        assertRoots(ClassPath.getClassPath(src, ClassPath.SOURCE), src, rsrc);
        assertRoots(ClassPath.getClassPath(rsrc, ClassPath.SOURCE), src, rsrc);
        assertRoots(ClassPath.getClassPath(tsrc, ClassPath.SOURCE), tsrc, trsrc);
        assertRoots(ClassPath.getClassPath(trsrc, ClassPath.SOURCE), tsrc, trsrc);
    }

    public void testITSourcePath() throws Exception {
        TestFileUtils.writeFile(d,
                "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>jar</packaging>" +
                "<version>1.0-SNAPSHOT</version>" +
                "<name>Test</name>" +
                "<build>" +
                "<testSourceDirectory>src/it/java</testSourceDirectory>" +
                "</build>" +
                "</project>");
        FileObject itsrc = FileUtil.createFolder(d, "src/it/java");
        assertRoots(ClassPath.getClassPath(itsrc, ClassPath.SOURCE), itsrc);
    }

    public void testCompileClassPath() throws Exception {
        TestFileUtils.writeFile(d,
                "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>jar</packaging>" +
                "<version>1.0-SNAPSHOT</version>" +
                "<name>Test</name>" +
                "</project>");
        FileObject src = FileUtil.createFolder(d, "src/main/java");
        ClassPath cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
        assertNotNull(cp);
        List<ClassPath.Entry> entries = cp.entries();
        assertFalse(entries.isEmpty());
    }
    
    public void testCompileClassPathWithModuleInfo() throws Exception {
        TestFileUtils.writeFile(d,
                "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>jar</packaging>" +
                "<version>1.0-SNAPSHOT</version>" +
                "<name>Test</name>" +
                "</project>");
        FileObject src = FileUtil.createFolder(d, "src/main/java");
        FileObject mi = FileUtil.createData(src, "module-info.java");
        ClassPath cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
        assertNotNull(cp);
        List<ClassPath.Entry> entries = cp.entries();
        assertFalse(entries.isEmpty());
    }
    
    public void testCompileClassPathWithModuleInfoAddedLater() throws Exception {
        TestFileUtils.writeFile(d,
                "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>jar</packaging>" +
                "<version>1.0-SNAPSHOT</version>" +
                "<name>Test</name>" +
                "</project>");
        FileObject src = FileUtil.createFolder(d, "src/main/java");
        Project prj = FileOwnerQuery.getOwner(src);
        // trigger FSL on source groups
        prj.getLookup().lookup(Sources.class).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        ClassPath cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
        assertNotNull(cp);
        List<ClassPath.Entry> entries = cp.entries();
        assertFalse(entries.isEmpty());
        
        FileObject mi = FileUtil.createData(src, "module-info.java");
        cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
        assertNotNull(cp);
        entries = cp.entries();
        assertFalse(entries.isEmpty());
    }        

    private static void assertRoots(ClassPath cp, FileObject... files) {
        assertNotNull(cp);
        Set<FileObject> roots = new LinkedHashSet<FileObject>();
        for (FileObject file : files) {
            roots.add(FileUtil.isArchiveFile(file) ? FileUtil.getArchiveRoot(file) : file);
        }
        assertEquals(roots, new LinkedHashSet<FileObject>(Arrays.asList(cp.getRoots())));
    }

    public void testGeneratedSources() throws Exception { // #187595
        TestFileUtils.writeFile(d,
                "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>jar</packaging>" +
                "<version>0</version>" +
                "</project>");
        FileObject src = FileUtil.createFolder(d, "src/main/java");
        FileObject gsrc = FileUtil.createFolder(d, "target/generated-sources/xjc");
        gsrc.createData("Whatever.class");
        FileObject tsrc = FileUtil.createFolder(d, "src/test/java");
        FileObject gtsrc = FileUtil.createFolder(d, "target/generated-test-sources/jaxb");
        gtsrc.createData("Whatever.class");
        FileObject gtsrc2 = FileUtil.createFolder(d, "target/generated-sources/test-annotations"); // MCOMPILER-167
        gtsrc2.createData("Whatever.class");
        assertRoots(ClassPath.getClassPath(src, ClassPath.SOURCE), src, gsrc);
        assertRoots(ClassPath.getClassPath(gsrc, ClassPath.SOURCE), src, gsrc);
        assertRoots(ClassPath.getClassPath(tsrc, ClassPath.SOURCE), tsrc, gtsrc, gtsrc2);
        assertRoots(ClassPath.getClassPath(gtsrc, ClassPath.SOURCE), tsrc, gtsrc, gtsrc2);
        assertRoots(ClassPath.getClassPath(gtsrc2, ClassPath.SOURCE), tsrc, gtsrc, gtsrc2);
    }

    public void testNewlyCreatedSourceGroup() throws Exception { // #190852
        TestFileUtils.writeFile(d,
                "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>jar</packaging>" +
                "<version>0</version>" +
                "</project>");
        FileObject src = FileUtil.createFolder(d, "src/main/java");
        FileObject tsrc = FileUtil.createFolder(d, "src/test/java");
        ClassPath sourcepath = ClassPath.getClassPath(src, ClassPath.SOURCE);
        ClassPath tsourcepath = ClassPath.getClassPath(tsrc, ClassPath.SOURCE);
        assertRoots(sourcepath, src);
        assertRoots(tsourcepath, tsrc);
        MockPropertyChangeListener l = new MockPropertyChangeListener();
        sourcepath.addPropertyChangeListener(l);
        FileObject gsrc = FileUtil.createFolder(d, "target/generated-sources/xjc");
        gsrc.createData("Whatever.class");
        l.assertEvents(ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS);
        assertRoots(sourcepath, src, gsrc);
        assertSame(sourcepath, ClassPath.getClassPath(gsrc, ClassPath.SOURCE));
        tsourcepath.addPropertyChangeListener(l);
        FileObject gtsrc = FileUtil.createFolder(d, "target/generated-test-sources/jaxb");
        gtsrc.createData("Whatever.class");
        l.assertEvents(ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS);
        assertRoots(tsourcepath, tsrc, gtsrc);
        assertSame(tsourcepath, ClassPath.getClassPath(gtsrc, ClassPath.SOURCE));
    }

    public void testArchetypeResources() throws Exception { // #189037
        TestFileUtils.writeFile(d,
                "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>g</groupId>" +
                "<artifactId>a</artifactId>" +
                // unloadable during a test: "<packaging>maven-archetype</packaging>" +
                "<version>0</version>" +
                "</project>");
        TestFileUtils.writeFile(d, "src/main/resources/META-INF/maven/archetype-metadata.xml", "<archetype-descriptor/>");
        TestFileUtils.writeFile(d, "src/main/resources/archetype-resources/pom.xml", "<project/>");
        TestFileUtils.writeFile(d, "src/main/resources/archetype-resources/src/main/java/X.java", "package $package; public class X {}");
        Project p = ProjectManager.getDefault().findProject(d);
        ProjectSourcesClassPathProvider pscpp = p.getLookup().lookup(ProjectSourcesClassPathProvider.class);
        assertNotNull(pscpp);
        ClassPath[] sourceCPs = pscpp.getProjectClassPaths(ClassPath.SOURCE);
        assertEquals(Arrays.toString(sourceCPs), 2, sourceCPs.length); // src/main/* + src/test/*
        List<ClassPath.Entry> entries = sourceCPs[0].entries();
        assertEquals(entries.toString(), 2, entries.size());
        assertEquals(entries.toString(), new URL(d.toURL(), "src/main/java/"), entries.get(0).getURL());
        assertEquals(entries.toString(), new URL(d.toURL(), "src/main/resources/"), entries.get(1).getURL());
        assertTrue(entries.get(1).includes("META-INF/"));
        assertTrue(entries.get(1).includes("META-INF/maven/"));
        assertTrue(entries.get(1).includes("META-INF/maven/archetype-metadata.xml"));
        assertFalse(entries.get(1).includes("archetype-resources/"));
        assertFalse(entries.get(1).includes("archetype-resources/src/"));
        assertFalse(entries.get(1).includes("archetype-resources/src/main/"));
        assertFalse(entries.get(1).includes("archetype-resources/src/main/java/"));
        assertFalse(entries.get(1).includes("archetype-resources/src/main/java/X.java"));
    }

    private void assertRtJar(ClassPath cp, boolean shouldBeThere) {
        for (FileObject fo : cp.getRoots()) {
            if (fo.getNameExt().equals("rt.jar")) {
                if (shouldBeThere) {
                    return;
                }
                fail("We don't want rt.jar on boot classpath: " + fo);
            }
            FileObject archive = FileUtil.getArchiveFile(fo);
            if (archive != null && archive.getNameExt().equals("rt.jar")) {
                if (shouldBeThere) {
                    return;
                }
                fail("We don't want rt.jar on boot classpath: " + archive);
            }
        }
        assertFalse("We don't want rt.jar on cp, right?", shouldBeThere);
    }

    private void selectProfile(FileObject d, String cfgName) throws Exception {
        M2ConfigProvider cp = ProjectManager.getDefault().findProject(d).getLookup().lookup(M2ConfigProvider.class);
        boolean found = false;
        for (M2Configuration cfg : cp.getConfigurations()) {
            if (cfg.getActivatedProfiles().equals(Collections.singletonList(cfgName))) {
                cp.setActiveConfiguration(cfg);
                return;
            }
        }
        fail("Configuration " + cfgName + " not found!");
    }

}
