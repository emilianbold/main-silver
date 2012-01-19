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
package org.netbeans.modules.clientside.project;

import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.clientside.project.ui.ClientSideProjectLogicalView;
import org.netbeans.modules.web.common.reload.BrowserReload;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.*;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.filesystems.*;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author david
 */

@AntBasedProjectRegistration(
    type=ClientSideProjectType.TYPE,
    iconResource="org/netbeans/modules/clientside/project/ui/resources/projecticon.png",
    sharedNamespace=ClientSideProjectType.PROJECT_CONFIGURATION_NAMESPACE,
    privateNamespace=ClientSideProjectType.PRIVATE_CONFIGURATION_NAMESPACE
)
public class ClientSideProject implements Project {

    final AntProjectHelper helper;
    private final ReferenceHelper refHelper;
    private final PropertyEvaluator eval;
    private final Lookup lookup;
    
    public ClientSideProject(AntProjectHelper helper) {
        this.helper = helper;
        AuxiliaryConfiguration configuration = helper.createAuxiliaryConfiguration();
        eval = createEvaluator();
        refHelper = new ReferenceHelper(helper, configuration, getEvaluator());
        lookup = createLookup(configuration);
    }

    
    public AntProjectHelper getHelper() {
        return helper;
    }
    
    @Override
    public FileObject getProjectDirectory() {
        return getHelper().getProjectDirectory();
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    PropertyEvaluator getEvaluator() {
        return eval;
    }
    
    private PropertyEvaluator createEvaluator() {
        PropertyEvaluator baseEval2 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        return PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
                PropertyUtils.userPropertiesProvider(baseEval2,
                    "user.properties.file", FileUtil.toFile(getProjectDirectory())), // NOI18N
                helper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH));
    }
    
    private Lookup createLookup(AuxiliaryConfiguration configuration) {
        return Lookups.fixed(new Object[] {
                this,
                configuration,
                helper.createCacheDirectoryProvider(),
                helper.createAuxiliaryProperties(),
                getEvaluator(),
                new ClientSideProjectLogicalView(this),
                new RecommendedAndPrivilegedTemplatesImpl(),
                new ClientSideProjectActionProvider(),
                new OpenHookImpl(this),
        });
    }

    private final class Info implements ProjectInformation {

        @Override
        public String getName() {
            return getProjectDirectory().getName();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public Icon getIcon() {
            return new ImageIcon(ImageUtilities.loadImage(
                    "org/netbeans/modules/clientside/project/ui/resources/projecticon.png"));
        }

        @Override
        public Project getProject() {
            return ClientSideProject.this;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    
    }
    
    private final class RecommendedAndPrivilegedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {

        @Override
        public String[] getRecommendedTypes() {
            return new String[] { 
                "clientside-types",     // NOI18N
                "XML",                  // NOI18N
                "simple-files"          // NOI18N
            };
        }

        @Override
        public String[] getPrivilegedTemplates() {
            return new String[] {
                "Templates/ClientSide/new.html",            // NOI18N
                "Templates/ClientSide/new.js",            // NOI18N
                "Templates/ClientSide/new.css",            // NOI18N
                "Templates/ClientSide/new.json",            // NOI18N
                "Templates/Other/Folder"                   // NOI18N
            };
        }
    
    }
    
    private static class OpenHookImpl extends ProjectOpenedHook {

        private final ClientSideProject p;
        private FileChangeListener projectFileChangesListener;

        public OpenHookImpl(ClientSideProject p) {
            this.p = p;
        }
        
        @Override
        protected void projectOpened() {
            projectFileChangesListener = new ProjectFilesListener();
            FileUtil.addRecursiveListener(projectFileChangesListener, FileUtil.toFile(p.getProjectDirectory()));
        }

        @Override
        protected void projectClosed() {
            FileUtil.removeRecursiveListener(projectFileChangesListener, FileUtil.toFile(p.getProjectDirectory()));
        }
        
    }
    
    private static class ProjectFilesListener implements FileChangeListener {

        private final BrowserReload br;

        ProjectFilesListener() {
            br = BrowserReload.getInstance();
        }
        
        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
        }

        @Override
        public void fileChanged(FileEvent fe) {
            refreshInBrowser(fe.getFile());
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            FileObject fo = fe.getFile();
            if (br.canReload(fo)) {
                // XXX: close browser's tab ???
            }
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            // XXX: notify BrowserReload about filename change
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        private void refreshInBrowser(FileObject fo) {
            if (br.canReload(fo)) {
                br.reload(fo);
            }
        }
    }
}
