/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.util;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.CustomizerProviderImpl;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Radek Matous
 */
abstract class FileOperationFactory {
    private static final Logger LOGGER = Logger.getLogger(FileOperationFactory.class.getName());

    protected final PhpProject project;

    private final FileObject nbprojectDir;
    private final PhpVisibilityQuery phpVisibilityQuery;

    volatile boolean factoryError = false;

    public FileOperationFactory(PhpProject project) {
        assert project != null;
        this.project = project;
        phpVisibilityQuery = PhpVisibilityQuery.forProject(project);
        nbprojectDir = project.getProjectDirectory().getFileObject("nbproject"); // NOI18N
        assert nbprojectDir != null : "No nbproject directory found for " + project;
        assert nbprojectDir.isFolder() && nbprojectDir.isValid() : "Not valid nbproject directory found for " + project;
    }

    final Callable<Boolean> createCopyHandler(FileObject source) {
        if (isInvalid()) {
            LOGGER.log(Level.FINE, "No CREATE handler, File Operation Factory invalid for project {0}", project.getName());
            return null;
        }
        return createCopyHandlerInternal(source);
    }

    final Callable<Boolean> createDeleteHandler(FileObject source) {
        if (isInvalid()) {
            LOGGER.log(Level.FINE, "No DELETE handler, File Operation Factory invalid for project {0}", project.getName());
            return null;
        }
        return createDeleteHandlerInternal(source);
    }

    final Callable<Boolean> createInitHandler(FileObject source) {
        if (isInvalid()) {
            LOGGER.log(Level.FINE, "No INIT handler, File Operation Factory invalid for project {0}", project.getName());
            return null;
        }
        return createInitHandlerInternal(source);
    }

    final Callable<Boolean> createRenameHandler(FileObject source, String oldName) {
        if (isInvalid()) {
            LOGGER.log(Level.FINE, "No RENAME handler, File Operation Factory invalid for project {0}", project.getName());
            return null;
        }
        return createRenameHandlerInternal(source, oldName);
    }

    abstract Callable<Boolean> createCopyHandlerInternal(FileObject source);
    abstract Callable<Boolean> createDeleteHandlerInternal(FileObject source);
    abstract Callable<Boolean> createInitHandlerInternal(FileObject source);
    abstract Callable<Boolean> createRenameHandlerInternal(FileObject source, String oldName);

    void reset() {
        factoryError = false;
    }

    void invalidate() {
        factoryError = true;
    }

    boolean isInvalid() {
        return factoryError;
    }

    protected final boolean isSourceFileValid(FileObject sourceRoot, FileObject source) {
        return (FileUtil.isParentOf(sourceRoot, source) || source.equals(sourceRoot))
                && !isNbProjectMetadata(source)
                && phpVisibilityQuery.isVisible(source);
    }

    boolean isNbProjectMetadata(FileObject fo) {
        return FileUtil.isParentOf(nbprojectDir, fo) || nbprojectDir.equals(fo);
    }

    protected FileObject getSources() {
        return ProjectPropertiesSupport.getSourcesDirectory(project);
    }

    protected static String getPath(FileObject fo) {
        return FileUtil.getFileDisplayName(fo);
    }

    protected boolean askUser(String message) {
        Object openProperties = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(message, project.getName(), JOptionPane.YES_NO_OPTION));
        return openProperties.equals(JOptionPane.YES_OPTION);
    }

    protected void showCustomizer() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                project.getLookup().lookup(CustomizerProviderImpl.class).showCustomizer(CompositePanelProviderImpl.SOURCES);
            }
        });
    }
}
