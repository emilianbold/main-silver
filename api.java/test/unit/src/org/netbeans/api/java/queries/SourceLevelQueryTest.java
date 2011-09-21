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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.api.java.queries;

import java.util.HashMap;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Jesse Glick
 */
public class SourceLevelQueryTest extends NbTestCase {

    public SourceLevelQueryTest(String n) {
        super(n);
    }

    private static Map<FileObject, String> slq2Files  = new HashMap<FileObject, String>();
    private static String LEVEL;
    private FileObject f;

    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(SLQ.class,SLQ2.class);
        LEVEL = null;
        f = FileUtil.createMemoryFileSystem().getRoot();
    }

    public void testBasicUsage() throws Exception {
        assertNull(SourceLevelQuery.getSourceLevel(f));
        LEVEL = "1.3";
        assertEquals("1.3", SourceLevelQuery.getSourceLevel(f));
        LEVEL = "1.5";
        assertEquals("1.5", SourceLevelQuery.getSourceLevel(f));
        MockServices.setServices();
        assertNull(SourceLevelQuery.getSourceLevel(f));
    }

    public void testRobustness() throws Exception {
        // #83994: should only return well-formed source levels.
        LEVEL = "${default.javac.source}";
        assertNull(SourceLevelQuery.getSourceLevel(f));
    }

    public void testSLQ2() throws Exception {
        LEVEL = "1.3";
        FileObject f1 = f.createFolder("f1");   //NOI18N
        FileObject f2 = f.createFolder("f2");   //NOI18N
        assertEquals("1.3", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("1.3", SourceLevelQuery.getSourceLevel(f2));   //NOI18N
        slq2Files.put(f1, "1.5");   //NOI18N
        assertEquals("1.5", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("1.3", SourceLevelQuery.getSourceLevel(f2));   //NOI18N
        assertEquals("1.5", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel().toString());   //NOI18N
        assertTrue(SourceLevelQuery.getSourceLevel2(f1).supportsChanges());
        SourceLevelQuery.Result sourceLevel2 = SourceLevelQuery.getSourceLevel2(f2);
        assertEquals("1.3",sourceLevel2.getSourceLevel().toString());   //NOI18N
        assertFalse(sourceLevel2.supportsChanges());
        ChangeListener dummy = new ChangeListener() {@Override public void stateChanged(ChangeEvent e) {}};
        sourceLevel2.addChangeListener(dummy);
        sourceLevel2.removeChangeListener(dummy);
        LEVEL = "1.5";
        assertEquals("1.5",sourceLevel2.getSourceLevel().toString());   //NOI18N
    }

    public static final class SLQ implements SourceLevelQueryImplementation {

        public SLQ() {}

        public String getSourceLevel(FileObject javaFile) {
            return LEVEL;
        }
    }

    public static final class SLQ2 implements SourceLevelQueryImplementation2 {

        @Override
        public Result getSourceLevel(FileObject javaFile) {
            final String sl = slq2Files.get(javaFile);
            if (sl != null) {
                return new SourceLevelQueryImplementation2.Result() {
                    @Override
                    public String getSourceLevel() {
                        return sl;
                    }
                    @Override
                    public void addChangeListener(ChangeListener listener) {
                    }
                    @Override
                    public void removeChangeListener(ChangeListener listener) {
                    }
                };
            }
            return null;
        }

    }

}
