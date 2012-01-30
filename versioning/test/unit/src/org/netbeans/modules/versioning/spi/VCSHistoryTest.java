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
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.versioning.spi;


import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;

import org.openide.util.Lookup;
import org.netbeans.modules.versioning.spi.testvcs.TestVCSInterceptor;

/**
 * Versioning SPI unit tests of VCSInterceptor.
 * 
 * @author Maros Sandor
 */
public class VCSHistoryTest extends NbTestCase {
    
    private File dataRootDir;

    public VCSHistoryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dataRootDir = getDataDir(); 
        File userdir = new File(dataRootDir + "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        if(!dataRootDir.exists()) dataRootDir.mkdirs();
        Lookup.getDefault().lookupAll(VersioningSystem.class);
        File f = new File(dataRootDir, "workdir");
        deleteRecursively(f);
        f.mkdirs();
        f = new File(dataRootDir, "workdir/root-test-versioned");
        f.mkdirs();
    }

    public void testHistoryProvider() throws IOException {
        File f = new File(dataRootDir, "workdir/root-test-versioned");
        VersioningSystem vs = VersioningSupport.getOwner(f);
        assertNotNull(vs);
        VCSHistoryProvider hp = vs.getVCSHistoryProvider();
        assertNotNull(hp);
    }
    
    public void testHistoryEntryProvidesRevision() throws IOException {
        RevisionProviderImpl provider = new RevisionProviderImpl();
        provider.revisionprovided = false;
        VCSHistoryProvider.HistoryEntry h = 
                new VCSHistoryProvider.HistoryEntry(
                    new File[] {new File("")}, 
                    new Date(System.currentTimeMillis()), 
                    "msg", 
                    "user", 
                    "username", 
                    "12345", 
                    "1234567890", 
                    new Action[0], 
                    provider);
        h.getRevisionFile(new File(""), new File(""));
        assertTrue(provider.revisionprovided);
    }
    
    public void testHistoryEntryDoesntProvideRevision() throws IOException {
        RevisionProviderImpl provider = new RevisionProviderImpl();
        provider.revisionprovided = false;
        VCSHistoryProvider.HistoryEntry h = 
                new VCSHistoryProvider.HistoryEntry(
                    new File[] {new File("")}, 
                    new Date(System.currentTimeMillis()), 
                    "msg", 
                    "user", 
                    "username", 
                    "12345", 
                    "1234567890", 
                    new Action[0], 
                    null);
        h.getRevisionFile(new File(""), new File(""));
        // nothing happend
    }
    
    public void testHistoryEntryEditable() throws IOException {
        MessageEditProviderImpl provider = new MessageEditProviderImpl();
        provider.message = null;
        VCSHistoryProvider.HistoryEntry h = 
                new VCSHistoryProvider.HistoryEntry(
                    new File[] {new File("")}, 
                    new Date(System.currentTimeMillis()), 
                    "msg", 
                    "user", 
                    "username", 
                    "12345", 
                    "1234567890", 
                    new Action[0], 
                    null,
                    provider);
        assertTrue(h.canEdit());
        h.setMessage("msg2");
        assertEquals("msg2", provider.message);
    }
    
    public void testHistoryEntryMsgReadOnly() throws IOException {
        VCSHistoryProvider.HistoryEntry h = 
                new VCSHistoryProvider.HistoryEntry(
                    new File[] {new File("")}, 
                    new Date(System.currentTimeMillis()), 
                    "msg", 
                    "user", 
                    "username", 
                    "12345", 
                    "1234567890", 
                    new Action[0], 
                    null,
                    null);
        try {
            h.setMessage("msg2");
        } catch(Throwable t) {
            return;
        }
        fail("exception should be raised on readonly entry");
    }

    private void deleteRecursively(File f) {
        if(f.isFile()) {
            f.delete();
        } else {
            File[] files = f.listFiles();
            if(files != null) {
                for (File file : files) {
                    deleteRecursively(file);
                    file.delete();
                }
            }
        }
    }
    
    private class RevisionProviderImpl implements VCSHistoryProvider.RevisionProvider {
        boolean revisionprovided = false;
        @Override
        public void getRevisionFile(File originalFile, File revisionFile) {
            revisionprovided = true;
        }
    }
    
    private class MessageEditProviderImpl implements VCSHistoryProvider.MessageEditProvider {
        String message;
        @Override
        public void setMessage(String message) throws IOException {
            this.message = message;
        }
    }       
}
