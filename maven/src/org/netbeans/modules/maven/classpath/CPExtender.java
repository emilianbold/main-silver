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

package org.netbeans.modules.maven.classpath;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Repository;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * an implementation of ProjectClassPathModifierImplementation that tried to match 
 * maven dependencies to the way classpath items are added through this api.
 * @author mkleint
 */
@SuppressWarnings("deprecation")
public class CPExtender extends ProjectClassPathModifierImplementation implements org.netbeans.spi.java.project.classpath.ProjectClassPathExtender {

    private NbMavenProjectImpl project;
    private static final String POM_XML = "pom.xml"; //NOI18N
    
    /**
     * ClassPath for compiling only, but not running. In practice this means that scope
     * for the artefacts with this classpath will be set to <code>provided</code>
     * if added to a source group, and <code>test</code> if added to test source group.
     * This constant is in practice a friend API, even if it is hardcoded in the
     * consuming module (see e.g. bug 186221).
     */
    public static final String CLASSPATH_COMPILE_ONLY = "classpath/compile_only";

    /** Creates a new instance of CPExtender */
    public CPExtender(NbMavenProjectImpl project) {
        this.project = project;
    }
    
    public boolean addLibrary(final Library library) throws IOException {
        final Boolean[] added = new Boolean[1];
        ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            public void performOperation(POMModel model) {
                try {
                    added[0] = addRemoveLibrary(library, model, null, true);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    added[0] = Boolean.FALSE;
                }
            }
        };
        FileObject pom = project.getProjectDirectory().getFileObject(POM_XML);//NOI18N
        org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
        //TODO is the manual reload necessary if pom.xml file is being saved?
//                NbMavenProject.fireMavenProjectReload(project);
        if (added[0]) {
            project.getLookup().lookup(NbMavenProject.class).triggerDependencyDownload();
        }

        return added[0];
    }
    
    public boolean addArchiveFile(FileObject arch) throws IOException {
        final File jar = FileUtil.archiveOrDirForURL(arch.getURL());
        if (jar == null || jar.isDirectory()) {
            throw new IOException("Cannot add folders to Maven projects as dependencies: " + arch); //NOI18N
        }
        final AtomicBoolean added = new AtomicBoolean();
        ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            public void performOperation(POMModel model) {
                try {
                    added.compareAndSet(false, addRemoveJAR(jar, model, null, true));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        FileObject pom = project.getProjectDirectory().getFileObject(POM_XML);//NOI18N
        org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
        if (added.get()) {
            project.getLookup().lookup(NbMavenProject.class).triggerDependencyDownload();
        }
        return added.get();
    }

    private boolean addRemoveLibrary(Library library, POMModel model, String scope, boolean add) throws IOException {
        Boolean modified = checkLibraryForPoms(library, model, scope, add);
        if (modified == null) {
            List<URL> urls = library.getContent("classpath"); //NOI18N
            modified = urls.size() > 0;
            assert model != null;
            for (URL url : urls) {
                File jar = FileUtil.archiveOrDirForURL(url);
                if (jar == null) {
                    throw new IOException("Could find no file corresponding to " + url);
                }
                if (jar.isDirectory()) {
                    throw new IOException("Cannot add folders to Maven projects as dependencies: " + url); //NOI18N
                }
                modified = modified && addRemoveJAR(jar, model, scope, add);
            }
        }
        return modified;
    }
    
    private boolean addRemoveJAR(File jar, POMModel mdl, String scope, boolean add) throws IOException {
        if (!add) {
            throw new UnsupportedOperationException("removing JARs not yet supported");
        }
        NBVersionInfo dep = null;
        for (NBVersionInfo _dep : RepositoryQueries.findBySHA1(jar, RepositoryPreferences.getInstance().getRepositoryInfos())) {
            if (!"unknown.binary".equals(_dep.getGroupId())) {
                dep = _dep;
                break;
            }
        }
        if (dep == null) {
            dep = new NBVersionInfo(null, "unknown.binary", jar.getName().replaceFirst("[.]jar$", ""), "SNAPSHOT", null, null, null, null, null);
            addJarToPrivateRepo(jar, mdl, dep);
        }
        //if not found anywhere, add to a custom file:// based repository structure within the project's directory.
        boolean added = false;
        Dependency dependency = ModelUtils.checkModelDependency(mdl, dep.getGroupId(), dep.getArtifactId(), false);
        if (dependency == null) {
            dependency = ModelUtils.checkModelDependency(mdl, dep.getGroupId(), dep.getArtifactId(), true);
            added = true;
        }
        if (!Utilities.compareObjects(dep.getVersion(), dependency.getVersion())) {
            dependency.setVersion(dep.getVersion());
            added = true;
        }
        if (!Utilities.compareObjects(scope, dependency.getScope())) {
            dependency.setScope(scope);
            added = true;
        }
        return added;
    }
    
    /**
     * @return true if something was added, false if everything was already there, null if could not do anything
     */
    @SuppressWarnings("NP_BOOLEAN_RETURN_NULL")
    private Boolean checkLibraryForPoms(Library library, POMModel model, String scope, boolean add) {
        if (!"j2se".equals(library.getType())) {//NOI18N
            //only j2se library supported for now..
            return null;
        }
        Boolean modified = null;
        for (URL pom : library.getContent("maven-pom")) {
            ModelUtils.LibraryDescriptor result = ModelUtils.checkLibrary(pom);
            if (result != null) {
                //set dependency
                modified = false;
                Dependency dep = ModelUtils.checkModelDependency(model, result.getGroupId(), result.getArtifactId(), false);
                if (!add) {
                    if (dep != null &&
                            Utilities.compareObjects(result.getVersion(), dep.getVersion()) &&
                            Utilities.compareObjects(scope, dep.getScope()) &&
                            Utilities.compareObjects(result.getClassifier(), dep.getClassifier())) {
                        model.removeChildComponent(dep);
                        modified = true;
                    }
                    break;
                }
                if (dep == null) {
                    dep = ModelUtils.checkModelDependency(model, result.getGroupId(), result.getArtifactId(), true);
                    modified = true;
                }
                if (!Utilities.compareObjects(result.getVersion(), dep.getVersion())) {
                    dep.setVersion(result.getVersion());
                    modified = true;
                }
                if (!Utilities.compareObjects(scope, dep.getScope())) {
                    dep.setScope(scope);
                    modified = true;
                }
                if (!Utilities.compareObjects(result.getClassifier(), dep.getClassifier())) {
                    dep.setClassifier(result.getClassifier());
                    modified = true;
                }
                //set repository
                org.netbeans.modules.maven.model.pom.Repository reposit = ModelUtils.addModelRepository(
                        project.getOriginalMavenProject(), model, result.getRepoRoot());
                if (reposit != null) {
                    reposit.setId(library.getName());
                    reposit.setLayout(result.getRepoType());
                    reposit.setName("Repository for library " + library); //NOI18N - content coming into the pom.xml file
                    modified = true;
                }
            }
        }
        return modified;
    }
        
    public boolean addAntArtifact(AntArtifact arg0, URI arg1) throws IOException {
        throw new IOException("Cannot add Ant based projects as subprojecs to Maven projects."); //NOI18N
    }
    
    public SourceGroup[] getExtensibleSourceGroups() {
        Sources s = this.project.getLookup().lookup(Sources.class);
        assert s != null;
        List<SourceGroup> grps = new ArrayList<SourceGroup>();
        SourceGroup[] java = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (java != null) {
            grps.addAll(Arrays.asList(java));
        }
        SourceGroup[] res = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
        if (res != null) {
            grps.addAll(Arrays.asList(res));
        }
        SourceGroup[] web = s.getSourceGroups("doc_root");
        if (web != null) {
            grps.addAll(Arrays.asList(web));
        }
        
        return grps.toArray(new SourceGroup[0]);
    }
    
    @Override
    public String[] getExtensibleClassPathTypes(SourceGroup arg0) {
        return new String[] {
            ClassPath.COMPILE,
            ClassPath.EXECUTE,
            CLASSPATH_COMPILE_ONLY,
            JavaClassPathConstants.PROCESSOR_PATH
        };
    }

    public @Override boolean addLibraries(Library[] libraries, SourceGroup grp, String type) throws IOException {
        return addRemoveLibraries(libraries, grp, type, true);
    }

    public @Override boolean removeLibraries(Library[] libraries, SourceGroup grp, String type) throws IOException {
        return addRemoveLibraries(libraries, grp, type, false);
    }

    private boolean addRemoveLibraries(final Library[] libraries, SourceGroup grp, String type, final boolean add) throws IOException {
        final AtomicBoolean modified = new AtomicBoolean();
        final String scope = findScope(grp, type);
        ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            public @Override void performOperation(POMModel model) {
                for (Library library : libraries) {
                    try {
                        modified.compareAndSet(false, addRemoveLibrary(library, model, scope, add));
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        };
        FileObject pom = project.getProjectDirectory().getFileObject(POM_XML);//NOI18N
        org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
        //TODO is the manual reload necessary if pom.xml file is being saved?
//                NbMavenProject.fireMavenProjectReload(project);
        if (modified.get()) {
            project.getLookup().lookup(NbMavenProject.class).triggerDependencyDownload();
        }
        return modified.get();
    }

    public @Override boolean addRoots(final URL[] urls, SourceGroup grp, String type) throws IOException {
        final AtomicBoolean added = new AtomicBoolean();
        final String scope = findScope(grp, type);
        ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            public @Override void performOperation(POMModel model) {
                for (URL url : urls) {
                    File jar = FileUtil.archiveOrDirForURL(url);
                    if (jar != null && jar.isFile()) {
                        try {
                            added.compareAndSet(false, addRemoveJAR(jar, model, scope, true));
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    } else {
                        Logger.getLogger(CPExtender.class.getName()).log(Level.INFO, "Adding non-jar root to Maven projects makes no sense. ({0})", url); //NOI18N
                    }
                }
            }
        };
        FileObject pom = project.getProjectDirectory().getFileObject(POM_XML);//NOI18N
        org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
        if (added.get()) {
            project.getLookup().lookup(NbMavenProject.class).triggerDependencyDownload();
        }
        return added.get();
    }

    @Override
    protected boolean addProjects(final Project[] projects, SourceGroup sg, String classPathType) throws IOException, UnsupportedOperationException {
        final AtomicBoolean added = new AtomicBoolean();
        final AtomicBoolean nonMavenError = new AtomicBoolean();
        final String scope = findScope(sg, classPathType);
        ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            public @Override void performOperation(POMModel model) {
                Set<Artifact> arts = project.getOriginalMavenProject().getArtifacts();
                for (Project prj: projects) {
                    NbMavenProject nbprj = prj.getLookup().lookup(NbMavenProject.class);
                    if (nbprj != null) {
                        MavenProject mp = nbprj.getMavenProject();
                        String id = mp.getArtifact().getId();
                        for (Artifact a : arts) {
                            if (a.getId().equals(id)) {
                                //already there..
                                continue;
                            }
                        }
                        Dependency dependency = ModelUtils.checkModelDependency(model, mp.getGroupId(), mp.getArtifactId(), true);
                        dependency.setVersion(mp.getVersion());
                        if (scope != null) {
                            dependency.setScope(scope);
                        } else {
                            if (NbMavenProject.TYPE_EJB.equals(nbprj.getPackagingType()) ||
                                NbMavenProject.TYPE_WAR.equals(nbprj.getPackagingType())) {
                                dependency.setScope(Artifact.SCOPE_PROVIDED);
                            }
                        }
                        added.set(true);
                    } else {
                        // unsupported usecase, not a maven project
                        nonMavenError.set(true);
                    }
                }
            }
        };
        FileObject pom = project.getProjectDirectory().getFileObject(POM_XML);//NOI18N
        org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
        if (nonMavenError.get()) {
            //throw late to prevent the pom model to go bust eventually
            throw new UnsupportedOperationException("Attempting to add a non-Maven project dependency to a Maven project, not supported."); //NOI18N
        }

        return added.get();

    }

    public boolean removeRoots(URL[] arg0, SourceGroup arg1, String arg2) throws IOException,
                                                                                    UnsupportedOperationException {
        throw new UnsupportedOperationException("Removing binary dependencies is not supported by Maven projects.");
    }
    
    public boolean addAntArtifacts(AntArtifact[] arg0, URI[] arg1,
                                      SourceGroup arg2, String arg3) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot add Ant based projects as subprojects to Maven projects.");//NOI18N
    }
    
    public boolean removeAntArtifacts(AntArtifact[] arg0, URI[] arg1,
                                         SourceGroup arg2, String arg3) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot remove Ant based projects as subprojects from Maven projects.");//NOI18N
    }
    
    private static String findScope(SourceGroup grp, String type) {
        String scope = ClassPath.EXECUTE.equals(type) ? Artifact.SCOPE_RUNTIME : null; //NOI18N
        //figure if we deal with test or regular sources.
        String name = grp.getName();
        if (MavenSourcesImpl.NAME_TESTSOURCE.equals(name)) {
            scope = "test"; //NOI18N
        }
        if (scope == null &&
            (CLASSPATH_COMPILE_ONLY.equals(type) || JavaClassPathConstants.PROCESSOR_PATH.equals(type))) {
            scope = Artifact.SCOPE_PROVIDED;
        }
        return scope;
    }

    // XXX this is a poor solution; http://jira.codehaus.org/secure/attachment/53864/MNG-1867.zip is better
    private void addJarToPrivateRepo(File jar, POMModel mdl, NBVersionInfo dep) throws IOException {
        //first add the local repo to
        List<Repository> repos = mdl.getProject().getRepositories();
        boolean found = false;
        String path = null;
        if (repos != null) {
            for (Repository repo : repos) {
                if ("unknown-jars-temp-repo".equals(repo.getId())) { //NOI18N
                    found = true;
                    String url = repo.getUrl();
                    if (url.startsWith("file:${project.basedir}/")) { //NOI18N
                        path = url.substring("file:${project.basedir}/".length()); //NOI18N
                    } else {
                        path = "lib"; //NOI18N
                    }
                    break;
                }
            }
        }
        if (!found) {
            Repository repo = mdl.getFactory().createRepository();
            repo.setId("unknown-jars-temp-repo"); //NOI18N
            repo.setName("A temporary repository created by NetBeans for libraries and jars it could not identify. Please replace the dependencies in this repository with correct ones and delete this repository."); //NOI18N
            repo.setUrl("file:${project.basedir}/lib"); //NOI18N
            mdl.getProject().addRepository(repo);
            path = "lib"; //NOI18N
        }
        assert path != null;
        FileObject root = FileUtil.createFolder(project.getProjectDirectory(), path);
        FileObject grp = FileUtil.createFolder(root, dep.getGroupId().replace('.', '/')); //NOI18N
        FileObject art = FileUtil.createFolder(grp, dep.getArtifactId());
        FileObject ver = FileUtil.createFolder(art, dep.getVersion());
        String name = dep.getArtifactId() + '-' + dep.getVersion();
        FileObject file = FileUtil.toFileObject(jar);
        if (ver.getFileObject(name, file.getExt()) == null) { //#160803
            FileUtil.copyFile(file, ver, name, file.getExt());
        }
    }
}
