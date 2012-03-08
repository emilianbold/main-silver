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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.util;

import org.netbeans.modules.bugtracking.kenai.spi.KenaiUtil;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.DelegatingConnector;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.kenai.spi.OwnerInfo;
import org.netbeans.modules.bugtracking.ui.selectors.RepositorySelectorBuilder;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Tomas Stupka
 * @author Marian Petras
 */
public abstract class BugtrackingOwnerSupport {

    private static BugtrackingOwnerSupport instance;

    protected BugtrackingOwnerSupport() { }

    public static BugtrackingOwnerSupport getInstance() {
        if(instance == null) {
            instance = Lookup.getDefault().lookup(BugtrackingOwnerSupport.class);
            if (instance == null) {
                instance = new DefaultImpl();
            }
        }
        return instance;
    }

    public enum ContextType {
        MAIN_PROJECT_ONLY,
        MAIN_OR_SINGLE_PROJECT,
        ALL_PROJECTS,
        SELECTED_FILE_AND_ALL_PROJECTS,
    }

    //--------------------------------------------------------------------------

    public static Project getMainOrSingleProject() {
        final OpenProjects projects = OpenProjects.getDefault();

        Project[] openProjects = projects.getOpenProjects();
        if (openProjects.length == 1) {
            return openProjects[0];
        } else {
            return projects.getMainProject();
        }
    }

    //--------------------------------------------------------------------------

    public RepositoryImpl getRepository(ContextType context) {
        switch (context) {
            case MAIN_PROJECT_ONLY:
                Project mainProject = OpenProjects.getDefault().getMainProject();
                if (mainProject != null) {
                    return getRepository(mainProject, false);
                }
                break;
            case MAIN_OR_SINGLE_PROJECT:
                Project mainOrSingleProject = getMainOrSingleProject();
                if (mainOrSingleProject != null) {
                    return getRepository(mainOrSingleProject, false);
                }
                break;
            case ALL_PROJECTS:
                return getRepository(OpenProjects.getDefault().getOpenProjects());
            case SELECTED_FILE_AND_ALL_PROJECTS:
                File contextFile = getLargerContext();
                if (contextFile != null) {
                    return getRepositoryForContext(contextFile, false);
                }
                break;
            default:
                assert false;
                break;
        }
        return null;
    }

    public RepositoryImpl getRepository(Node... nodes) {
        if (nodes == null) {
            return null;
        }
        if (nodes.length == 0) {
            return null;
        }
        if (nodes.length == 1) {
            return getRepository(nodes[0]);
        }

        RepositoryImpl chosenRepo = null;
        for (Node node : nodes) {
            RepositoryImpl repo = getRepository(node);
            if (repo == null) {
                continue;
            }
            if (chosenRepo == null) {
                chosenRepo = repo;
            } else if (repo != chosenRepo) {    //various repositories assigned
                return null;
            }
        }
        return chosenRepo;
    }

    protected RepositoryImpl getRepository(Node node) {
        final Lookup nodeLookup = node.getLookup();

        Project project = nodeLookup.lookup(Project.class);
        if (project != null) {
            return getRepository(project, false);
        }

        DataObject dataObj = nodeLookup.lookup(DataObject.class);
        if (dataObj != null) {
            return getRepository(dataObj);
        }

        return null;
    }

    protected abstract RepositoryImpl getRepository(DataObject dataObj);

    public RepositoryImpl getRepository(Project... projects) {
        if (projects.length == 0) {
            return null;
        }
        if (projects.length == 1) {
            return getRepository(projects[0], false);
        }

        RepositoryImpl chosenRepo = null;
        for (Project project : projects) {
            RepositoryImpl repo = getRepository(project, false);
            if (repo == null) {
                continue;
            }
            if (chosenRepo == null) {
                chosenRepo = repo;
            } else if (repo != chosenRepo) {
                return null;   //the projects have various repositories assigned
            }
        }
        return chosenRepo;
    }

    public abstract RepositoryImpl getRepository(Project project, boolean askIfUnknown);

    public RepositoryImpl getRepository(File file, boolean askIfUnknown) {
        return getRepository(file, null, askIfUnknown);
    }

    public abstract RepositoryImpl getRepository(File file, String issueId, boolean askIfUnknown);

    protected RepositoryImpl getRepositoryForContext(File context,
                                                 boolean askIfUnknown) {
        return getRepositoryForContext(context, null, askIfUnknown);
    }

    protected abstract RepositoryImpl getRepositoryForContext(File context,
                                                          String issueId,
                                                          boolean askIfUnknown);

    public void setFirmAssociations(File[] files, RepositoryImpl repository) {
        if (files == null) {
            throw new IllegalArgumentException("files is null");        //NOI18N
        }
        if (files.length == 0) {
            return;
        }

        FileToRepoMappingStorage.getInstance().setFirmAssociation(
                getLargerContext(files[0]),
                repository);
    }

    public void setFirmAssociation(File file, RepositoryImpl repository) {
        FileToRepoMappingStorage.getInstance().setFirmAssociation(
                getLargerContext(file),
                repository);
    }

    public void setLooseAssociation(ContextType contextType, RepositoryImpl repository) {
        final OpenProjects projects = OpenProjects.getDefault();

        File context = null;

        switch (contextType) {
            case MAIN_PROJECT_ONLY:
                Project mainProject = projects.getMainProject();
                if (mainProject != null) {
                    context = getLargerContext(mainProject);
                }
                break;
            case MAIN_OR_SINGLE_PROJECT:
                Project mainOrSingleProject = getMainOrSingleProject();
                if (mainOrSingleProject != null) {
                    context = getLargerContext(mainOrSingleProject);
                }
                break;
            case ALL_PROJECTS:
                context = getContextFromProjects();
                break;
            case SELECTED_FILE_AND_ALL_PROJECTS:
                context = getLargerContext();
                break;
            default:
                assert false;
                break;
        }

        if (context != null) {
            FileToRepoMappingStorage.getInstance().setLooseAssociation(
                    context,
                    repository);
        }
    }

    public void setLooseAssociation(File file, RepositoryImpl repository) {
        FileToRepoMappingStorage.getInstance().setLooseAssociation(
                getLargerContext(file),
                repository);
    }

    /**
     * Returns all repository urls that appear in a <strong>firm</strong> association.
     * @return
     */
    public final Collection<String> getAllAssociatedUrls() {
        return FileToRepoMappingStorage.getInstance().getAllFirmlyAssociatedUrls();
    }

    private static File getLargerContext() {
        FileObject openFile = getOpenFileObj();
        if (openFile != null) {
            File largerContext = getLargerContext(openFile);
            if (largerContext != null) {
                return largerContext;
            }
        }

        return getContextFromProjects();
    }

    private static File getContextFromProjects() {
        final OpenProjects projects = OpenProjects.getDefault();

        Project mainProject = projects.getMainProject();
        if (mainProject != null) {
            return getLargerContext(mainProject);       //null or non-null
        }

        Project[] openProjects = projects.getOpenProjects();
        if ((openProjects != null) && (openProjects.length == 1)) {
            return getLargerContext(openProjects[0]);
        }

        return null;
    }

    private static File getLargerContext(File file) {
        return getLargerContext(file, null);
    }

    private static File getLargerContext(FileObject fileObj) {
        return getLargerContext(null, fileObj);
    }

    private static File getLargerContext(File file, FileObject fileObj) {
        if ((file == null) && (fileObj == null)) {
            throw new IllegalArgumentException(
                    "both File and FileObject are null");               //NOI18N
        }

        assert (file == null)
               || (fileObj == null)
               || FileUtil.toFileObject(file).equals(fileObj);

        if (fileObj == null) {
            fileObj = getFileObjForFileOrParent(file);
        } else if (file == null) {
            file = FileUtil.toFile(fileObj);
        }

        if (fileObj == null) {
            return null;
        }
        if (!fileObj.isValid()) {
            return null;
        }

        Project parentProject = FileOwnerQuery.getOwner(fileObj);
        if (parentProject != null) {
            FileObject parentProjectFolder = parentProject.getProjectDirectory();
            if (parentProjectFolder.equals(fileObj) && (file != null)) {
                return file;
            }
            File folder = FileUtil.toFile(parentProjectFolder);
            if (folder != null) {
                return folder;
            }
        }

        if (fileObj.isFolder()) {
            return file;                        //whether it is null or non-null
        } else {
            fileObj = fileObj.getParent();
            assert fileObj != null;      //every non-folder should have a parent
            return FileUtil.toFile(fileObj);    //whether it is null or non-null
        }
    }


    private static FileObject getFileObjForFileOrParent(File file) {
        FileObject fileObj = FileUtil.toFileObject(file);
        if (fileObj != null) {
            return fileObj;
        }

        File closestParentFile = file.getParentFile();
        while (closestParentFile != null) {
            fileObj = FileUtil.toFileObject(closestParentFile);
            if (fileObj != null) {
                return fileObj;
            }
            closestParentFile = closestParentFile.getParentFile();
        }

        return null;
    }

    private static File getLargerContext(Project project) {
        FileObject projectFolder = project.getProjectDirectory();
        assert projectFolder != null;

        return FileUtil.toFile(projectFolder);
    }

    private static FileObject getOpenFileObj() {
        TopComponent activatedTopComponent = TopComponent.getRegistry()
                                             .getActivated();
        if (activatedTopComponent == null) {
            return null;
        }

        DataObject dataObj = activatedTopComponent.getLookup()
                             .lookup(DataObject.class);
        if ((dataObj == null) || !dataObj.isValid()) {
            return null;
        }

        return dataObj.getPrimaryFile();
    }

    //--------------------------------------------------------------------------

    private static class DefaultImpl extends BugtrackingOwnerSupport {

        private static final Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.bridge.BugtrackingOwnerSupport");   // NOI18N

        @Override
        protected RepositoryImpl getRepository(DataObject dataObj) {
            FileObject fileObj = dataObj.getPrimaryFile();
            if (fileObj == null) {
                return null;
            }

            Project project = FileOwnerQuery.getOwner(fileObj);
            if (project != null) {
                return getRepository(project, false);
            }

            RepositoryImpl repo;

            try {
                repo = getKenaiBugtrackingRepository(fileObj);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                repo = null;
            }

            return repo;
        }

        @Override
        public RepositoryImpl getRepository(Project project, boolean askIfUnknown) {
            RepositoryImpl repo;

            FileObject fileObject = project.getProjectDirectory();
            try {
                repo = getKenaiBugtrackingRepository(fileObject);
                if (repo != null) {
                    return repo;
                }
            } catch (IOException ex) {
                return null;
            }

            File context = getLargerContext(project);
            if (context != null) {
                return getRepositoryForContext(context, null, askIfUnknown);
            } else {
                return askUserToSpecifyRepository(null);
            }
        }

        @Override
        public RepositoryImpl getRepository(File file, String issueId, boolean askIfUnknown) {
            //TODO - synchronization/threading
            FileObject fileObject = FileUtil.toFileObject(file);
            if (fileObject == null) {
                LOG.log(Level.WARNING, " did not find a FileObject for file {0}", new Object[] {file}); //NOI18N
            } else {
                try {
                    RepositoryImpl repo = getKenaiBugtrackingRepository(fileObject);
                    if (repo != null) {
                        return repo;
                    }
                } catch (IOException ex) {
                    LOG.log(Level.INFO,
                          " communication with Kenai failed while loading " //NOI18N
                              + "information about bugtracking repository", //NOI18N
                          ex);
                    return null;
                }
            }

            File context = getLargerContext(file, fileObject);
            if (context == null) {
                context = file;
            }

            return getRepositoryForContext(context, issueId, askIfUnknown);
        }

        protected RepositoryImpl getRepositoryForContext(File context, String issueId,
                                                     boolean askIfUnknown) {
            RepositoryImpl repo = FileToRepoMappingStorage.getInstance()
                              .getFirmlyAssociatedRepository(context);
            if (repo != null) {
                LOG.log(Level.FINER, 
                        " found stored repository [{0}] for directory {1}",     //NOI18N
                        new Object[]{repo, context});
                return repo;
            }

            RepositoryImpl suggestedRepository = FileToRepoMappingStorage.getInstance()
                                             .getLooselyAssociatedRepository(context);
            if (!askIfUnknown) {
                return suggestedRepository;
            }

            repo = askUserToSpecifyRepository(suggestedRepository);
            if (repo != null) {
                return repo;
            }

            return null;
        }

        private static RepositoryImpl getKenaiBugtrackingRepository(FileObject fileObject) throws IOException {
            return getRepository(fileObject);
        }

        /**
         *
         * @param fileObject
         * @return
         * @throws IOException
         */
        private static RepositoryImpl getRepository(FileObject fileObject) throws IOException {
            Object attValue = fileObject.getAttribute(
                                           "ProvidedExtensions.RemoteLocation");//NOI18N
            if (attValue instanceof String) {
                RepositoryImpl repository = null;
                String url = (String) attValue;
                if(BugtrackingUtil.isNbRepository(url)) {
                    File file = FileUtil.toFile(fileObject);
                    if(file != null) {
                        OwnerInfo ownerInfo = KenaiUtil.getOwnerInfo(file);
                        if(ownerInfo != null) {
                            repository = APIAccessor.IMPL.getImpl(KenaiUtil.getRepository(url, ownerInfo.getOwner()));
                        }
                        if(repository == null) {
                            repository = APIAccessor.IMPL.getImpl(KenaiUtil.findNBRepository());
                        }
                    }
                }
                if(repository != null) {
                    return repository;
                }
                try {
                    repository = APIAccessor.IMPL.getImpl(KenaiUtil.getRepository(url));
                    if (repository != null) {
                        return repository;
                    }
                } catch (IOException ex) {
                    /* the remote location (URL) denotes a Kenai project */
                    if ("Not Found".equals(ex.getMessage())) {              // NOI18N
                        BugtrackingManager.LOG.log(
                                Level.INFO,
                                "Kenai project corresponding to URL {0} does not exist.",  // NOI18N
                                attValue);
                    } else {
                        BugtrackingManager.LOG.throwing(
                                BugtrackingOwnerSupport.class.getName(),    //class name
                                "getRepository(String)",    //method name       // NOI18N
                                ex);
                    }
                    throw ex;
                }
            }
            return null;
        }

        /**
         * Find a Kenai bug-tracking repository for the given URL.
         *
         * @param  string containing information about bug-tracking repository
         *         or versioning repository
         * @return  instance of an existing Kenai bug-tracking repository, if the
         *          given string denotes such a repository;
         *          {@code null} if the string format does not denote a Kenai
         *          bug-tracking repository (in other cases, an exception is thrown)
         * @throws  org.netbeans.modules.kenai.api.KenaiException
         *          if the URI denotes a Kenai project's repository but there was
         *          some problem getting the project's repository, e.g. because
         *          the given project does not exist on Kenai
         */
        private static RepositoryImpl getKenaiBugtrackingRepository(String remoteLocation) throws IOException {
            return APIAccessor.IMPL.getImpl(KenaiUtil.getRepository(remoteLocation));
        }

        private RepositoryImpl askUserToSpecifyRepository(RepositoryImpl suggestedRepo) {
            Collection<RepositoryImpl> repos = BugtrackingUtil.getKnownRepositories(true);
            DelegatingConnector[] connectors = BugtrackingManager.getInstance().getConnectors();

            final RepositorySelectorBuilder selectorBuilder = new RepositorySelectorBuilder();
            selectorBuilder.setDisplayFormForExistingRepositories(true);
            selectorBuilder.setExistingRepositories(repos.toArray(new RepositoryImpl[repos.size()]));
            selectorBuilder.setBugtrackingConnectors(connectors);
            selectorBuilder.setPreselectedRepository(suggestedRepo);
            selectorBuilder.setLabelAboveComboBox();

            final String dialogTitle = getMessage("LBL_BugtrackerSelectorTitle"); //NOI18N

            DialogDescriptor dialogDescriptor
                    = selectorBuilder.createDialogDescriptor(dialogTitle);

            Object selectedOption = DialogDisplayer.getDefault().notify(dialogDescriptor);
            if (selectedOption == NotifyDescriptor.OK_OPTION) {
                RepositoryImpl repository = selectorBuilder.getSelectedRepository();
                try {
                    repository.applyChanges();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                    repository = null;
                }
                return repository;
            } else {
                return null;
            }
        }

        private String getMessage(String msgKey) {
            return NbBundle.getMessage(BugtrackingOwnerSupport.class, msgKey);
        }

    }

}
