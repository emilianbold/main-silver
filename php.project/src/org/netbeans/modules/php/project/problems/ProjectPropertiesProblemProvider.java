/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.problems;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Problems in project properties.
 */
public final class ProjectPropertiesProblemProvider implements ProjectProblemsProvider, PropertyChangeListener {

    // set would be better but it is fine to use a list for small number of items
    private static final List<String> WATCHED_PROPERTIES = new CopyOnWriteArrayList<String>(Arrays.asList(
            PhpProjectProperties.SRC_DIR,
            PhpProjectProperties.TEST_SRC_DIR,
            PhpProjectProperties.SELENIUM_SRC_DIR,
            PhpProjectProperties.WEB_ROOT));

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final PhpProject project;
    private final Object problemsLock = new Object();

    // @GuardedBy("problemsLock")
    private Collection<ProjectProblem> problems;
    // @GuardedBy("problemsLock")
    private long eventId;


    private ProjectPropertiesProblemProvider(PhpProject project) {
        this.project = project;
    }

    private void addListeners() {
        ProjectPropertiesSupport.addWeakPropertyEvaluatorListener(project, this);
    }

    public static ProjectPropertiesProblemProvider createForProject(PhpProject project) {
        ProjectPropertiesProblemProvider projectProblems = new ProjectPropertiesProblemProvider(project);
        projectProblems.addListeners();
        return projectProblems;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    @Override
    public Collection<? extends ProjectProblem> getProblems() {
        Collection<ProjectProblem> currentProblems;
        long curEventId;
        synchronized (problemsLock) {
            currentProblems = problems;
            curEventId = eventId;
        }
        if (currentProblems != null) {
            return currentProblems;
        }
        // check all problems
        currentProblems = new LinkedHashSet<ProjectProblem>();
        checkSrcDir(currentProblems);
        if (currentProblems.isEmpty()) {
            // check other problems only if sources are correct (other problems are fixed in customizer but customizer needs correct sources)
            checkTestDir(currentProblems);
            checkSeleniumDir(currentProblems);
            checkWebRoot(currentProblems);
        }
        if (currentProblems.isEmpty()) {
            currentProblems = Collections.<ProjectProblem>emptySet();
        }
        synchronized (problemsLock) {
            if (curEventId == eventId) {
                problems = currentProblems;
            } else if (problems != null) {
                currentProblems = problems;
            }
        }
        assert currentProblems != null;
        return currentProblems;
    }

    @NbBundle.Messages({
        "ProjectPropertiesProblemProvider.invalidSrcDir.title=Invalid Source Files",
        "# {0} - src dir path",
        "ProjectPropertiesProblemProvider.invalidSrcDir.description=The directory \"{0}\" does not exist and cannot be used for Source Files.",
        "# {0} - project name",
        "ProjectPropertiesProblemProvider.invalidSrcDir.dialog.title=Select Source Files for {0}"
    })
    private void checkSrcDir(Collection<ProjectProblem> currentProblems) {
        File invalidDirectory = getInvalidDirectory(ProjectPropertiesSupport.getSourcesDirectory(project), PhpProjectProperties.SRC_DIR);
        if (invalidDirectory != null) {
            ProjectProblem problem = ProjectProblem.createError(
                    Bundle.ProjectPropertiesProblemProvider_invalidSrcDir_title(),
                    Bundle.ProjectPropertiesProblemProvider_invalidSrcDir_description(invalidDirectory.getAbsolutePath()),
                    new DirectoryProblemResolver(project, PhpProjectProperties.SRC_DIR, Bundle.ProjectPropertiesProblemProvider_invalidSrcDir_dialog_title(project.getName())));
            currentProblems.add(problem);
        }
    }

    @NbBundle.Messages({
        "ProjectPropertiesProblemProvider.invalidTestDir.title=Invalid Test Files",
        "# {0} - test dir path",
        "ProjectPropertiesProblemProvider.invalidTestDir.description=The directory \"{0}\" does not exist and cannot be used for Test Files."
    })
    private void checkTestDir(Collection<ProjectProblem> currentProblems) {
        File invalidDirectory = getInvalidDirectory(ProjectPropertiesSupport.getTestDirectory(project, false), PhpProjectProperties.TEST_SRC_DIR);
        if (invalidDirectory != null) {
            ProjectProblem problem = ProjectProblem.createError(
                    Bundle.ProjectPropertiesProblemProvider_invalidTestDir_title(),
                    Bundle.ProjectPropertiesProblemProvider_invalidTestDir_description(invalidDirectory.getAbsolutePath()),
                    new CustomizerProblemResolver(project, CompositePanelProviderImpl.SOURCES));
            currentProblems.add(problem);
        }
    }

    @NbBundle.Messages({
        "ProjectPropertiesProblemProvider.invalidSeleniumDir.title=Invalid Selenium Test Files",
        "# {0} - selenium dir path",
        "ProjectPropertiesProblemProvider.invalidSeleniumDir.description=The directory \"{0}\" does not exist and cannot be used for Selenium Test Files.",
        "# {0} - project name",
        "ProjectPropertiesProblemProvider.invalidSeleniumDir.dialog.title=Select Selenium Test Files for {0}"
    })
    private void checkSeleniumDir(Collection<ProjectProblem> currentProblems) {
        File invalidDirectory = getInvalidDirectory(ProjectPropertiesSupport.getSeleniumDirectory(project, false), PhpProjectProperties.SELENIUM_SRC_DIR);
        if (invalidDirectory != null) {
            ProjectProblem problem = ProjectProblem.createError(
                    Bundle.ProjectPropertiesProblemProvider_invalidSeleniumDir_title(),
                    Bundle.ProjectPropertiesProblemProvider_invalidSeleniumDir_description(invalidDirectory.getAbsolutePath()),
                    new DirectoryProblemResolver(project, PhpProjectProperties.SELENIUM_SRC_DIR, Bundle.ProjectPropertiesProblemProvider_invalidSeleniumDir_dialog_title(project.getName())));
            currentProblems.add(problem);
        }
    }

    @NbBundle.Messages({
        "ProjectPropertiesProblemProvider.invalidWebRoot.title=Invalid Web Root",
        "# {0} - web root path",
        "ProjectPropertiesProblemProvider.invalidWebRoot.description=The directory \"{0}\" does not exist and cannot be used for Web Root."
    })
    private void checkWebRoot(Collection<ProjectProblem> currentProblems) {
        File invalidDirectory = getInvalidDirectory(FileUtil.toFileObject(getWebRoot()), PhpProjectProperties.WEB_ROOT);
        if (invalidDirectory != null) {
            ProjectProblem problem = ProjectProblem.createError(
                    Bundle.ProjectPropertiesProblemProvider_invalidWebRoot_title(),
                    Bundle.ProjectPropertiesProblemProvider_invalidWebRoot_description(invalidDirectory.getAbsolutePath()),
                    new CustomizerProblemResolver(project, CompositePanelProviderImpl.SOURCES));
            currentProblems.add(problem);
        }
    }

    private File getInvalidDirectory(FileObject directory, String propertyName) {
        assert WATCHED_PROPERTIES.contains(propertyName) : "Property '" + propertyName + "' should be watched for changes";
        if (directory != null && directory.isValid()) {
            // ok
            return null;
        }
        String propValue = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(propertyName);
        if (propValue == null) {
            return null;
        }
        File dir = ProjectPropertiesSupport.getSubdirectory(project, project.getProjectDirectory(), propValue);
        assert !dir.exists() : "Directory should not exists since fileobject does not exist (or is invalid): " + dir;
        return dir;
    }

    // XXX put somewhere and use everywhere (copied to more places)
    private File getWebRoot() {
        // ProjectPropertiesSupport.getWebRootDirectory(project) cannot be used since it always returns a valid fileobject (even if webroot is invalid, then sources are returned)
        return ProjectPropertiesSupport.getSourceSubdirectory(project, ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(PhpProjectProperties.WEB_ROOT));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (WATCHED_PROPERTIES.contains(evt.getPropertyName())) {
            synchronized (problemsLock) {
                problems = null;
                eventId++;
            }
            propertyChangeSupport.firePropertyChange(PROP_PROBLEMS, null, null);
        }
    }

}
