/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.profiler.categories.j2se;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.profiler.results.cpu.marking.MarkMapper;
import org.netbeans.lib.profiler.results.cpu.marking.MarkMapping;
import org.netbeans.lib.profiler.results.cpu.marking.MarkingEngine;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.profiler.categories.Categorization;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;


/**
 * @author ads
 *
 */
public class TestBase extends NbTestCase {
    
    private static final Repository REPOSITORY;

    static {
        REPOSITORY = new RepositoryImpl();
        // for setting the default lookup to TestUtil's one
        setLookup(new Object[0]);
        
    }

    public static void setLookup(Object[] instances) {
        Object[] newInstances = new Object[instances.length + 1];
        System.arraycopy(instances, 0, newInstances, 0, instances.length);
        newInstances[newInstances.length - 1] = REPOSITORY;

        TestUtilities.setLookup(newInstances);
    }
    
    public TestBase( String name ) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        if ( getProjectName() == null ){
            return;
        }
        
        TestUtilities.setCacheFolder(getWorkDir());
        System.setProperty("netbeans.user", new File(getWorkDir(), "ud").
                getAbsolutePath()); // NOI18N
        FileObject projectPath = FileUtil.toFileObject(FileUtil.normalizeFile(
                new File(getDataDir(), getProjectName())));
        
        myJavaApp = ProjectManager.getDefault().findProject(projectPath);
        
        OpenProjects.getDefault().open(new Project[]{myJavaApp}, false);
        
        IndexingManager.getDefault().refreshIndexAndWait( 
                projectPath.getFileObject("src").getURL(), null);
        
        myCategorization = new Categorization(myJavaApp);
        myCategorization.reset();
        MarkingEngine.getDefault().configure(myCategorization.getMappings(), Collections.emptyList());
        
    }
    
    protected String getProjectName(){
        return null;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        try {
            clearWorkDir();
        }
        catch( IOException e ){
            
        }
    }

    protected Project getProject(){
        return myJavaApp;
    }
    
    protected Categorization getCategorization(){
        return myCategorization;
    }
    
    private Project myJavaApp;
    private Categorization myCategorization;
    
}
