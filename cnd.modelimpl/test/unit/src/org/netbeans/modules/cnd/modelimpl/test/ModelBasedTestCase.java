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
package org.netbeans.modules.cnd.modelimpl.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.swing.undo.UndoManager;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.source.CndSourceTestUtilities;
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Vladimir Kvashin
 */
public class ModelBasedTestCase extends CndBaseTestCase {

    private final Collection<Throwable> exceptions = Collections.synchronizedList(new ArrayList<Throwable>());

    public ModelBasedTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DiagnosticExceptoins.Hook hook = new DiagnosticExceptoins.Hook() {

            @Override
            public void exception(Throwable thr) {
                thr.printStackTrace(System.err);
                exceptions.add(thr);
                CndUtils.threadsDump();
            }
        };
        DiagnosticExceptoins.setHook(hook);
    }

    /**
     * Registers an exception.
     * The idea is to process an exception that occurs in main thread
     * in the same way as exceptions that occur in code model threads
     */
    protected void registerException(Throwable thr) {
        exceptions.add(thr);
    }

    /** Asserts that no exceptions occur in code model threads */
    protected void assertNoExceptions() throws Exception {
        assertEmpty(exceptions);
    }

    private void assertEmpty(Collection<Throwable> errors) throws Exception {
        // the idea here was to somehow make JUnit infrastructure
        // display all caught exceptions;
        // but I don't yet know how to;
        // so for the time being we just throw 1-st one
        if (!errors.isEmpty()) {
            for (Throwable thr : errors) {
                if (thr instanceof Exception) {
                    throw (Exception) thr;
                } else if (thr instanceof Error) {
                    throw (Error) thr;
                } else {
                    throw new Exception(thr);
                }
            }
        }
    }

    protected BaseDocument getBaseDocument(File testSourceFile) throws Exception {
        FileObject testFileObject = CndFileUtils.toFileObject(testSourceFile);
        assertNotNull("Unresolved test file " + testSourceFile, testFileObject);//NOI18N
        DataObject testDataObject = DataObject.find(testFileObject);
        assertNotNull("Unresolved data object for file " + testFileObject, testDataObject);//NOI18N
        BaseDocument doc = CndCoreTestUtils.getBaseDocument(testDataObject);
        assertNotNull("Unresolved document for data object " + testDataObject, doc);//NOI18N
        return doc;
    }
    
    protected UndoManager getUndoRedoManager(File testSourceFile) throws Exception {
        FileObject testFileObject = CndFileUtils.toFileObject(testSourceFile);
        assertNotNull("Unresolved test file " + testSourceFile, testFileObject);//NOI18N
        DataObject testDataObject = DataObject.find(testFileObject);
        return CndSourceTestUtilities.getUndoRedo(testDataObject);
    }
    
    protected CsmFile getCsmFile(File testSourceFile) throws Exception {
        FileObject fo = FileUtil.toFileObject(testSourceFile);
        CsmFile csmFile = CsmModelAccessor.getModel().findFile(FSPath.toFSPath(fo), true, false);
        assertNotNull("Unresolved CsmFile for test file " + testSourceFile, csmFile);//NOI18N
        return csmFile;
    }    
}
