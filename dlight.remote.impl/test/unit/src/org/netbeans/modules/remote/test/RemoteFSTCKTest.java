/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.test;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.remote.impl.fs.RemoteFSTCKTestCase;
import org.openide.filesystems.FileObjectTestHid;
import org.openide.filesystems.FileSystemTestHid;
import org.openide.filesystems.FileUtilTestHidden;
import org.openide.filesystems.URLMapperTestHidden;

/**
 *
 * @author vv159170
 */
public class RemoteFSTCKTest extends RemoteFSTCKTestCase {
   
    public RemoteFSTCKTest(Test test) {
        super(test);
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(FileSystemTestHid_.class);
        suite.addTestSuite(FileObjectTestHid_.class);
        // it seems AttributesTestHidden does not belong to FS TCK
        //suite.addTestSuite(AttributesTestHidden.class);
        suite.addTestSuite(URLMapperTestHidden_.class);
        suite.addTestSuite(FileUtilTestHidden_.class);
        return new RemoteFSTCKTest(suite);
    }
    
    public static class FileSystemTestHid_ extends FileSystemTestHid {

        public FileSystemTestHid_(String testName) {
            super(testName);
        }
    }

    public static class FileObjectTestHid_ extends FileObjectTestHid {

        public FileObjectTestHid_(String testName) {
            super(testName);
        }

        @RandomlyFails
        @Override
        public void testWriteReadExclusion() throws Exception {
            super.testWriteReadExclusion();
        }

        @RandomlyFails
        @Override
        public void testWriteReadExclusionDeadlock() throws Exception {
            super.testWriteReadExclusionDeadlock();
        }

        @RandomlyFails
        @Override
        public void testFireFileDeletedEvent() throws IOException {
            super.testFireFileDeletedEvent();
        }
        
        @RandomlyFails
        @Override
        public void testFireFileDeletedEvent2() throws IOException {
            super.testFireFileDeletedEvent2();
        }

        @RandomlyFails
        @Override
        public void testFireFileAttributeChangedEvent() {
            super.testFireFileAttributeChangedEvent();
        }

        @RandomlyFails
        @Override
        public void testFireFileAttributeChangedEvent_FS() {
            super.testFireFileAttributeChangedEvent_FS();
        }

        @RandomlyFails
        @Override
        public void testDefaultMimeTypeForBinaryFiles() throws Exception {
            super.testDefaultMimeTypeForBinaryFiles();
        }

        @RandomlyFails
        @Override
        public void testRecursiveListener() throws IOException {
            super.testRecursiveListener();
        }

        @RandomlyFails
        @Override
        public void testBigFileAndAsString() throws Exception {
            super.testBigFileAndAsString();
        }

        @RandomlyFails
        @Override
        public void testAddFileChangeListener() {
            super.testAddFileChangeListener();
        }

        @RandomlyFails
        @Override
        public void testAddFileChangeListener_FS() {
            super.testAddFileChangeListener_FS();
        }

        @RandomlyFails
        @Override
        public void testRemoveFileChangeListener() throws IOException {
            super.testRemoveFileChangeListener();
        }

        @RandomlyFails
        @Override
        public void testRemoveFileChangeListener_FS() throws IOException {
            super.testRemoveFileChangeListener_FS();
        }

        @RandomlyFails
        @Override
        public void testToURL() throws Exception {
            super.testToURL();
        }
        
    }

    public static class URLMapperTestHidden_ extends URLMapperTestHidden {

        public URLMapperTestHidden_(String testName) {
            super(testName);
        }
    }

    public static class FileUtilTestHidden_ extends FileUtilTestHidden {

        public FileUtilTestHidden_(String testName) {
            super(testName);
        }

        @RandomlyFails
        @Override
        public void testRunAtomicAction() throws Exception {
            super.testRunAtomicAction();
        }

        @RandomlyFails
        @Override
        public void testToFile() throws Exception {
            super.testToFile();
        }

        @RandomlyFails
        @Override
        public void testToFileObject() throws Exception {
            super.testToFileObject();
        }

        @RandomlyFails
        @Override
        public void testIsParentOf() throws Exception {
            super.testIsParentOf();
        }
    }
}
