/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.mercurial;

import java.io.File;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author tomas
 */
public class IgnoresTest extends AbstractHgTest {

    public IgnoresTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();        
        
        // create
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        System.setProperty("netbeans.user", getWorkDir().getParentFile().getAbsolutePath());
        File ignoreFile = new File(getDataDir().getAbsolutePath() + "/ignore/hgignore");
        File toFile = new File(getWorkDir(), ".hgignore");
        ignoreFile.renameTo(toFile);
    }

    // ignore patterns - issue 171378 - should pass
    public void testIgnores () throws IOException {
        File workDir = getWorkDir();
        File ignoredFolder = new File(workDir, "ignoredFolderLevel1");
        ignoredFolder.mkdirs();
        FileInformation info = getCache().getCachedStatus(ignoredFolder);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        File ignoredFile = new File(ignoredFolder, "file");
        ignoredFile.createNewFile();
        info = getCache().getCachedStatus(ignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        File unignoredFile = new File(workDir, "file");
        unignoredFile.createNewFile();
        info = getCache().getCachedStatus(unignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0);


        File unignoredFolder = new File(workDir, "unignoredFolderLevel1");
        unignoredFolder.mkdirs();
        info = getCache().getCachedStatus(unignoredFolder);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0);

        ignoredFolder = new File(unignoredFolder, "ignoredFolderLevel2");
        ignoredFolder.mkdirs();
        info = getCache().getCachedStatus(ignoredFolder);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        ignoredFolder = new File(unignoredFolder, "ignoredFolderLevel2_2");
        ignoredFolder.mkdirs();
        info = getCache().getCachedStatus(ignoredFolder);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        ignoredFile = new File(ignoredFolder, "ignoredFile");
        ignoredFile.createNewFile();
        info = getCache().getCachedStatus(ignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        ignoredFile = new File(workDir, "file.ignore");
        ignoredFile.createNewFile();
        info = getCache().getCachedStatus(ignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        ignoredFile = new File(unignoredFolder, "file.ignore");
        ignoredFile.createNewFile();
        info = getCache().getCachedStatus(ignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);


        unignoredFolder = new File(unignoredFolder, "project");
        unignoredFolder.mkdirs();
        info = getCache().getCachedStatus(unignoredFolder);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0);

        unignoredFile = new File(unignoredFolder, "project");
        unignoredFile.createNewFile();
        info = getCache().getCachedStatus(unignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0);

        ignoredFile = new File(workDir, ".project");
        ignoredFile.createNewFile();
        info = getCache().getCachedStatus(ignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        ignoredFile = new File(unignoredFolder, ".project");
        ignoredFile.createNewFile();
        info = getCache().getCachedStatus(ignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        unignoredFile = new File(workDir, "file.ignore2");
        unignoredFile.createNewFile();
        info = getCache().getCachedStatus(unignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0);
    }
    
}
