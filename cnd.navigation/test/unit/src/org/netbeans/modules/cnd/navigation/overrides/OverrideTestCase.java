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

package org.netbeans.modules.cnd.navigation.overrides;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;

/**
 *
 * @author Vladimir Kvashin
 */
public class OverrideTestCase extends ProjectBasedTestCase {

    public OverrideTestCase(String testName) {
        super(testName);
    }

    public void testPrimitive() throws Exception {
        performTest("primitive.cc");
    }

    public void testProperParentSingle() throws Exception {
        performTest("proper_parent_single.cc"); // , "\\d*:OVERRIDES .*");
    }

    public void testProperParentMulty() throws Exception {
        performTest("proper_parent_multy.cc"); // , "\\d*:OVERRIDES .*");
    }

    private void performTest(String sourceFileName) throws Exception {
        performTest(sourceFileName, null);
    }

    private void performTest(String sourceFileName, String patternString) throws Exception {
        File testSourceFile = getDataFile(sourceFileName);
        assertNotNull(testSourceFile);
        StyledDocument doc = (StyledDocument) getBaseDocument(testSourceFile);
        assertNotNull(doc);
        FileObject fo = FileUtil.toFileObject(testSourceFile);
        assertNotNull(fo);
        DataObject dao = DataObject.find(fo);
        assertNotNull(dao);
        CsmFile csmFile = getCsmFile(testSourceFile);
        assertNotNull(csmFile);
        List<OverriddeAnnotation> annotations = new ArrayList<OverriddeAnnotation>();
        ComputeAnnotations.getInstance(csmFile).computeAnnotations(annotations, doc, dao);
        Collections.sort(annotations, new Comparator<OverriddeAnnotation>() {
            @Override
            public int compare(OverriddeAnnotation o1, OverriddeAnnotation o2) {
                return o1.getPosition().getOffset() - o2.getPosition().getOffset();
            }
        });

        String goldenFileName = sourceFileName + ".ref";
        String dataFileName = sourceFileName + ".dat";
        File workDir = getWorkDir();
        File output = new File(workDir, dataFileName); //NOI18N
        PrintStream streamOut = new PrintStream(output);
        dumpAnnotations(annotations, doc, streamOut, patternString);
        streamOut.close();

        File goldenDataFile = getGoldenFile(goldenFileName);
        if (!goldenDataFile.exists()) {
            fail("No golden file " + goldenDataFile.getAbsolutePath() + "\n to check with output file " + output.getAbsolutePath());
        }
        if (CndCoreTestUtils.diff(output, goldenDataFile, null)) {
            System.err.printf("---------- Annotations dump for failed %s.%s ----------\n", getClass().getSimpleName(), getName());
            dumpAnnotations(annotations, doc, System.err, patternString);
            System.err.println();
            // copy golden
            File goldenCopyFile = new File(workDir, goldenFileName + ".golden");
            CndCoreTestUtils.copyToWorkDir(goldenDataFile, goldenCopyFile); // NOI18N
            fail("OUTPUT Difference between diff " + output + " " + goldenCopyFile); // NOI18N
        }
    }

    private void dumpAnnotations(Collection<OverriddeAnnotation> annotations, StyledDocument doc, PrintStream ps, String patternString) {
        Pattern pattern = (patternString == null) ? null : Pattern.compile(patternString);
        for (OverriddeAnnotation anno : annotations) {
            StringBuilder sb = new StringBuilder();
            int line = NbDocument.findLineNumber(doc, anno.getPosition().getOffset()) + 1; // convert to 1-based
            sb.append(line);
            sb.append(':');
            sb.append(anno.getType());
            sb.append(' ');
            boolean first = true;
            for (OverriddeAnnotation.Element element : anno.getElements()) {
                int gotoLine = element.declaration.getStartPosition().getLine();
                String gotoFile = element.declaration.getContainingFile().getName().toString();
                if (first) {
                    first = false;
                } else {
                    sb.append(',');
                }
                sb.append(element.declaration.getQualifiedName());
                sb.append(' ');
                sb.append(gotoFile);
                sb.append(':');
                sb.append(gotoLine);
            }
            if (pattern == null || pattern.matcher(sb).matches()) {
                ps.printf("%s\n", sb);
            }
        }
    }
}
