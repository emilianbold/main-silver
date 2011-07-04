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
package org.netbeans.modules.php.project.connections;

import java.io.File;
import java.util.Date;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.project.connections.spi.RemoteFile;

/**
 * @author Tomas Mysik
 */
public class TransferFileTest extends NbTestCase {

    public TransferFileTest(String name) {
        super(name);
    }

    public void testLocalTransferFilePaths() {
        TransferFile file = TransferFile.fromFile(null, new File("/a/b/c"), "/a");
        assertEquals("c", file.getName());
        assertEquals("b/c", file.getRelativePath());
        assertEquals("b", file.getParentRelativePath());

        TransferFile file2 = TransferFile.fromFile(null, new File("/a/b/c"), "/a/b");
        assertFalse(file.equals(file2));

        TransferFile file3 = TransferFile.fromFile(null, new File("/0/1/2/b/c"), "/0/1/2");
        assertTrue(file.equals(file3));

        file = TransferFile.fromFile(null, new File("/a/b"), "/a");
        assertEquals("b", file.getName());
        assertEquals("b", file.getRelativePath());
        assertEquals(TransferFile.CWD, file.getParentRelativePath());

        file = TransferFile.fromFile(null, new File("/a"), "/a");
        assertEquals("a", file.getName());
        assertSame(TransferFile.CWD, file.getRelativePath());
        assertEquals(null, file.getParentRelativePath());
    }

    public void testRemoteTransferFilePaths() {
        TransferFile file = TransferFile.fromRemoteFile(null,
                new RemoteFileImpl("readme.txt", "/pub/myproject/tests/info", true),
                "/pub/myproject",
                "/pub/myproject/tests/info");
        assertEquals("readme.txt", file.getName());
        assertEquals("tests/info/readme.txt", file.getRelativePath());
        assertEquals("tests/info", file.getParentRelativePath());
    }

    public void testTransferFileRelations() {
        TransferFile projectRoot = TransferFile.fromFile(null, new File("/a"), "/a");
        assertNull(projectRoot.getParent());
        assertTrue(projectRoot.isRoot());
        assertTrue(projectRoot.isProjectRoot());
        assertFalse(projectRoot.getChildren().toString(), projectRoot.hasChildren());

        TransferFile child1 = TransferFile.fromFile(projectRoot, new File("/a/1"), "/a");
        TransferFile child2 = TransferFile.fromFile(projectRoot, new File("/a/2"), "/a");
        for (TransferFile child : new TransferFile[] {child1, child2}) {
            assertNotNull(child.getParent());
            assertFalse(child.isRoot());
            assertFalse(child.isProjectRoot());
            assertSame(child.getParent().toString(), projectRoot, child.getParent());
        }
        assertTrue(projectRoot.getChildren().toString(), projectRoot.hasChildren());
        assertSame(projectRoot.getChildren().toString(), 2, projectRoot.getChildren().size());
        assertTrue(projectRoot.getChildren().toString(), projectRoot.getChildren().contains(child1));
        assertTrue(projectRoot.getChildren().toString(), projectRoot.getChildren().contains(child2));
    }


    private final class RemoteFileImpl implements RemoteFile {

        private final String name;
        private final String parentDirectory;
        private final boolean file;


        public RemoteFileImpl(String name, String parentDirectory, boolean file) {
            this.name = name;
            this.parentDirectory = parentDirectory;
            this.file = file;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getParentDirectory() {
            return parentDirectory;
        }

        @Override
        public boolean isDirectory() {
            return !file;
        }

        @Override
        public boolean isFile() {
            return file;
        }

        @Override
        public boolean isLink() {
            return false;
        }

        @Override
        public long getSize() {
            return 999;
        }

        @Override
        public long getTimestamp() {
            return new Date().getTime();
        }

    }

}
