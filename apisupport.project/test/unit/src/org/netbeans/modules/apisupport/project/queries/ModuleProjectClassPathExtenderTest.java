/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.apisupport.project.queries;

import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.libraries.LibraryFactory;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.test.MockLookup;
import org.xml.sax.InputSource;

public class ModuleProjectClassPathExtenderTest extends NbTestCase {

    public ModuleProjectClassPathExtenderTest(String n) {
        super(n);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setLayersAndInstances();
        TestBase.initializeBuildProperties(getWorkDir(), getDataDir());
    }

    public void testAddLibraries() throws Exception {
        SuiteProject suite = TestBase.generateSuite(getWorkDir(), "suite");
        TestBase.generateSuiteComponent(suite, "lib");
        TestBase.generateSuiteComponent(suite, "testlib");
        NbModuleProject clientprj = TestBase.generateSuiteComponent(suite, "client");
        Library lib = LibraryFactory.createLibrary(new LibImpl("lib"));
        FileObject src = clientprj.getSourceDirectory();
        assertTrue(ProjectClassPathModifier.addLibraries(new Library[] {lib}, src, ClassPath.COMPILE));
        assertFalse(ProjectClassPathModifier.addLibraries(new Library[] {lib}, src, ClassPath.COMPILE));
        Library testlib = LibraryFactory.createLibrary(new LibImpl("testlib"));
        FileObject testsrc = clientprj.getTestSourceDirectory("unit");
        assertTrue(ProjectClassPathModifier.addLibraries(new Library[] {testlib}, testsrc, ClassPath.COMPILE));
        assertFalse(ProjectClassPathModifier.addLibraries(new Library[] {testlib}, testsrc, ClassPath.COMPILE));
        InputSource input = new InputSource(clientprj.getProjectDirectory().getFileObject("nbproject/project.xml").getURL().toString());
        XPath xpath = XPathFactory.newInstance().newXPath();
        assertEquals("org.example.client", xpath.evaluate("//*[local-name()='data']/*[local-name()='code-name-base']", input)); // control
        assertEquals("org.example.lib", xpath.evaluate("//*[local-name()='module-dependencies']/*/*[local-name()='code-name-base']", input));
        assertEquals("org.example.testlib", xpath.evaluate("//*[local-name()='test-dependencies']/*/*/*[local-name()='code-name-base']", input));
    }

    private static class LibImpl implements LibraryImplementation {
        private String name;
        LibImpl(String name) {
            this.name = name;
        }
        public String getType() {return "j2se";}
        public String getName() {return name;}
        public String getDescription() {return null;}
        public String getLocalizingBundle() {return null;}
        public List<URL> getContent(String volumeType) throws IllegalArgumentException {
            if (volumeType.equals("classpath")) {
                try {
                    return Collections.singletonList(new URL("jar:nbinst://org.example." + name + "/modules/ext/" + name + ".jar!/"));
                } catch (MalformedURLException x) {
                    throw new AssertionError(x);
                }
            } else {
                return Collections.emptyList();
            }
        }
        public void setName(String name) {}
        public void setDescription(String text) {}
        public void setLocalizingBundle(String resourceName) {}
        public void addPropertyChangeListener(PropertyChangeListener l) {}
        public void removePropertyChangeListener(PropertyChangeListener l) {}
        public void setContent(String volumeType, List<URL> path) throws IllegalArgumentException {}
    }

}
