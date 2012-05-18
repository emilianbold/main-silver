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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.hints.pom;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.apache.maven.DefaultMaven;
import org.apache.maven.Maven;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixProvider;
import org.netbeans.modules.maven.hints.pom.spi.SelectionPOMFixProvider;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProviderFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mkleint
 */
@MimeRegistration(mimeType=Constants.POM_MIME_TYPE, service=UpToDateStatusProviderFactory.class)
public final class StatusProvider implements UpToDateStatusProviderFactory {

    private static final String LAYER_POM = "pom"; //NOI18N
    private static final String LAYER_POM_SELECTION = "pom-selection"; //NOI18N
    private static final RequestProcessor RP = new RequestProcessor("StatusProvider"); //NOI18N
    private static final Logger LOG = Logger.getLogger(StatusProvider.class.getName());

    @Override
    public UpToDateStatusProvider createUpToDateStatusProvider(Document document) {
        return new StatusProviderImpl(document);
    }

    static class StatusProviderImpl extends UpToDateStatusProvider {
        private Document document;
        private @NullAllowed POMModel model;
        private Project project;
        private FileChangeListener listener;

        StatusProviderImpl(Document doc) {
            this.document = doc;
            listener = new FileChangeAdapter() {
                @Override
                public void fileChanged(FileEvent fe) {
                    // XXX fire PROP_UP_TO_DATE
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            checkHints();
                        }
                    });
                }
            };
            RP.post(new Runnable() {
                @Override
                public void run() {
                    initializeModel(); //#204067 moved to RP 
                    checkHints();
                }
            });
        }


        private void checkHints() {
            if (model == null) {
                return;
            }
            HintsController.setErrors(document, LAYER_POM, findHints(model, project, -1, -1));
        }

        static List<ErrorDescription> findHints(final @NonNull POMModel model, final Project project, final int selectionStart, final int selectionEnd) {
            assert model != null;
            if (!model.getModelSource().isEditable()) {
                return new ArrayList<ErrorDescription>();
            }
            try {
                model.getBaseDocument(); // #187615
                model.sync();
                // model.refresh();
            } catch (IOException ex) {
                LOG.log(Level.FINE, "Error while syncing pom model.", ex);
            }

            final List<ErrorDescription> err = new ArrayList<ErrorDescription>();

            if (!model.getState().equals(Model.State.VALID)) {
                LOG.log(Level.FINE, "Pom model document is not valid, is {0}", model.getState());
                return err;
            }
            if (model.getProject() == null) {
                LOG.log(Level.FINE, "Pom model root element missing");
                return err;
            }

            runMavenValidation(model, err);

            return ProjectManager.mutex().readAccess(new Mutex.Action<List<ErrorDescription>>() {
                public @Override List<ErrorDescription> run() {
                    boolean isInTransaction = model.isIntransaction();
                    if (! isInTransaction) {
                        if (! model.startTransaction()) {
                            return err;
                        }
                    }
                    try {
                        Lookup lkp = Lookups.forPath("org-netbeans-modules-maven-hints"); //NOI18N
                        if (selectionStart == -1 && selectionEnd == -1) {
                            Lookup.Result<POMErrorFixProvider> res = lkp.lookupResult(POMErrorFixProvider.class);
                            for (POMErrorFixProvider prov : res.allInstances()) {
                                if (!prov.getConfiguration().isEnabled(prov.getConfiguration().getPreferences())) {
                                    continue;
                                }
                                List<ErrorDescription> lst = prov.getErrorsForDocument(model, project);
                                if (lst != null) {
                                    err.addAll(lst);
                                }
                            }
                        } else {
                            Lookup.Result<SelectionPOMFixProvider> res = lkp.lookupResult(SelectionPOMFixProvider.class);
                            for (SelectionPOMFixProvider prov : res.allInstances()) {
                                if (!prov.getConfiguration().isEnabled(prov.getConfiguration().getPreferences())) {
                                    continue;
                                }
                                List<ErrorDescription> lst = prov.getErrorsForDocument(model, project, selectionStart, selectionEnd);
                                if (lst != null) {
                                    err.addAll(lst);
                                }
                            }
                        }
                        return err;
                    } finally {
                        if ((! isInTransaction) && model.isIntransaction()) {
                            try {
                                model.endTransaction();
                            } catch (IllegalStateException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            });
        }

        private void initializeModel() {
            FileObject fo = NbEditorUtilities.getFileObject(document);
            if (fo != null) {
                ModelSource ms = Utilities.createModelSource(fo);
                model = POMModelFactory.getDefault().getModel(ms);
                model.setAutoSyncActive(false);
                project = FileOwnerQuery.getOwner(fo);
                fo.addFileChangeListener(FileUtil.weakFileChangeListener(listener, fo));
            }
        }

        @Override
        public UpToDateStatus getUpToDate() {
            if (model == null) {
                return UpToDateStatus.UP_TO_DATE_OK;
            }
            FileObject fo = NbEditorUtilities.getFileObject(document);
            boolean ok = false;
            try {
                if (fo.isValid()) {
                    DataObject dobj = DataObject.find(fo);
                    EditorCookie ed = dobj.getLookup().lookup(EditorCookie.class);
                    if (ed != null) {
                        JEditorPane[] panes = ed.getOpenedPanes();
                        if (panes != null && panes.length > 0) {
                            final int selectionStart = panes[0].getSelectionStart();
                            final int selectionEnd = panes[0].getSelectionEnd();
                            if (selectionStart != selectionEnd) {
                                RP.post(new Runnable() {
                                    @Override public void run() {
                                        //this condition is important in order not to break any running hints
                                        //the model sync+refresh renders any existing POMComponents people
                                        // might be holding useless
                                        if (!model.isIntransaction()) {
                                            HintsController.setErrors(document, LAYER_POM_SELECTION, findHints(model, project, selectionStart, selectionEnd));
                                        } else {
                                            HintsController.setErrors(document, LAYER_POM_SELECTION, Collections.<ErrorDescription>emptyList());
                                        }
                                    }
                                });
                                ok = true;
                                return UpToDateStatus.UP_TO_DATE_PROCESSING;
                            }
                        }
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                //#166011 just a minor issue, just log, but don't show to user directly
                LOG.log(Level.INFO, "Touched somehow invalidated FileObject", ex);
            } finally {
                if (!ok) {
                    HintsController.setErrors(document, LAYER_POM_SELECTION, Collections.<ErrorDescription>emptyList());
                }
            }
            return UpToDateStatus.UP_TO_DATE_OK; // XXX should use UP_TO_DATE_PROCESSING if checkHints task is currently running
        }

    }

    private static void runMavenValidation(final POMModel model, final List<ErrorDescription> err) {
        File pom = model.getModelSource().getLookup().lookup(File.class);
        if (pom == null) {
            return;
        }
        
        List<ModelProblem> problems = runMavenValidationImpl(pom);
        for (ModelProblem problem : problems) {
            if (!problem.getSource().equals(pom.getAbsolutePath())) {
                LOG.log(Level.FINE, "found problem not in {0}: {1}", new Object[] {pom, problem.getSource()});
                continue;
            }
            int line = problem.getLineNumber();
            if (line <= 0) { // probably from a parent POM
                /* probably more irritating than helpful:
                line = 1; // fallback
                Parent parent = model.getProject().getPomParent();
                if (parent != null) {
                    Line l = NbEditorUtilities.getLine(model.getBaseDocument(), parent.findPosition(), false);
                    if (l != null) {
                        line = l.getLineNumber() + 1;
                    }
                }
                */
                continue;
            }
            if (problem.getException() instanceof UnresolvableModelException) {
                // If a <parent> reference cannot be followed because e.g. no projects are opened (so no repos registered), just ignore it.
                continue;
            }
            try {
                err.add(ErrorDescriptionFactory.createErrorDescription(problem.getSeverity() == ModelProblem.Severity.WARNING ? Severity.WARNING : Severity.ERROR, problem.getMessage(), model.getBaseDocument(), line));
            } catch (IndexOutOfBoundsException x) {
                LOG.log(Level.WARNING, "improper line number: {0}", problem);
            }
        }
        
    }
    
    //non-private because of tests..   
    static List<ModelProblem> runMavenValidationImpl(final File pom) {
        MavenEmbedder embedder = EmbedderFactory.getProjectEmbedder();
        MavenExecutionRequest meReq = embedder.createMavenExecutionRequest();
        ProjectBuildingRequest req = meReq.getProjectBuildingRequest();
        req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_3_1); // currently enables just <reporting> warning
        req.setLocalRepository(embedder.getLocalRepository());
        List<ArtifactRepository> remoteRepos = RepositoryPreferences.getInstance().remoteRepositories(embedder);
        req.setRemoteRepositories(remoteRepos);
        req.setRepositorySession(((DefaultMaven) embedder.lookupComponent(Maven.class)).newRepositorySession(meReq));
        List<ModelProblem> problems;
        try {
            problems = embedder.lookupComponent(ProjectBuilder.class).build(pom, req).getProblems();
        } catch (ProjectBuildingException x) {
            problems = new ArrayList<ModelProblem>();
            List<ProjectBuildingResult> results = x.getResults();
            if (results != null) { //one code point throwing ProjectBuildingException contains results,
                for (ProjectBuildingResult result : results) {
                    problems.addAll(result.getProblems());
                }
            } else {
                // another code point throwing ProjectBuildingException doesn't contain results..
                Throwable cause = x.getCause();
                if (cause instanceof ModelBuildingException) {
                    problems.addAll(((ModelBuildingException) cause).getProblems());
                }
            }
        }
        return problems;
    }

}
