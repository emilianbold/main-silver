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
package org.netbeans.modules.db.sql.history;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author John Baker, Jiri Skrivanek
 */
public class SQLHistoryPersistenceManagerTest extends NbTestCase {

    /** Default constructor.
     * @param testName name of particular test case
     */
    public SQLHistoryPersistenceManagerTest(String testName) {
        super(testName);
    }

    /** Called before every test case. */
    @Override
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }

    /** Called after every test case. */
    @Override
    public void tearDown() throws IOException {
        clearWorkDir();
    }

    /** Test testExecuteStatements passes if no exceptions occur. */
    public void testExecuteStatements() throws Exception {
        List<SQLHistory> sqlHistoryList = new ArrayList<SQLHistory>();
        sqlHistoryList.add(new SQLHistory("jdbc:// mysql", "select * from TRAVEL.PERSON", Calendar.getInstance().getTime()));
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        sqlHistoryList.add(new SQLHistory("jdbc:// oracle", "select * from PERSON", Calendar.getInstance().getTime()));
        SQLHistoryPersistenceManager.getInstance().create(fo, sqlHistoryList);
    }

    /** Test testMultipleExecutions passes if no exceptions occur. */
    public void testMultipleExecutions() throws Exception {
        List<SQLHistory> sqlHistoryList = new ArrayList<SQLHistory>();
        sqlHistoryList.add(new SQLHistory("jdbc:// derby", "select * from TRAVEL.TRIP", Calendar.getInstance().getTime()));
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        SQLHistoryPersistenceManager.getInstance().create(fo, sqlHistoryList);
        sqlHistoryList.add(new SQLHistory("jdbc:// postgres", "select * from TRAVEL.TRIP", Calendar.getInstance().getTime()));
        fo = FileUtil.toFileObject(getWorkDir());
        SQLHistoryPersistenceManager.getInstance().create(fo, sqlHistoryList);
    }

    /** Tests parsing of date format. */
    public void testDateParsing() throws Exception {
        URL u = this.getClass().getResource("sql_history.xml");
        FileObject fo = FileUtil.toFileObject(new File(u.toURI()));
        List<SQLHistory> sqlHistoryList = SQLHistoryPersistenceManager.getInstance().retrieve(FileUtil.toFile(fo).getAbsolutePath(), fo.getParent());
        for (SQLHistory sqlHistory : sqlHistoryList) {
            assertNotNull(sqlHistory.getDate());
        }
    }
}
