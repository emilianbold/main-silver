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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.DependencyManagement;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.model.pom.Repository;
import org.netbeans.modules.maven.options.MavenVersionSettings;
import org.openide.filesystems.FileObject;

/**
 * Various maven model related utilities.
 * @author mkleint
 * @author Anuradha G
 */
public final class ModelUtils {

    private ModelUtils() {}

    /**
     * 
     * @param pom       FileObject that represents POM
     * @param group     
     * @param artifact
     * @param version
     * @param type
     * @param scope
     * @param classifier
     * @param acceptNull accept null values to scope,type and classifier.
     *                   If true null values will remove corresponding tag.
     */
    public static void addDependency(FileObject pom,
            final String group,
            final String artifact,
            final String version,
            final String type,
            final String scope,
            final String classifier, final boolean acceptNull)
    {
        ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            private static final String BUNDLE_TYPE = "bundle"; //NOI18N
            @Override
            public void performOperation(POMModel model) {
                Dependency dep = checkModelDependency(model, group, artifact, true);
                dep.setVersion(version);
                if (acceptNull || scope != null) {
                    dep.setScope(scope);
                }
                if (acceptNull || (type != null && !BUNDLE_TYPE.equals(type))) {
                    dep.setType(type);
                }
                if (acceptNull || classifier != null) {
                    dep.setClassifier(classifier);
                }
            }
        };
        Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
    }

    public static Dependency checkModelDependency(POMModel pom, String groupId, String artifactId, boolean add) {
        Project mdl = pom.getProject();
        Dependency ret = mdl.findDependencyById(groupId, artifactId, null);
        Dependency managed = null;
        if (ret == null || ret.getVersion() == null) {
            //check dependency management section as well..
            DependencyManagement mng = mdl.getDependencyManagement();
            if (mng != null) {
                managed = mng.findDependencyById(groupId, artifactId, null);
            }
        }
        if (add && ret == null) {
            ret = mdl.getModel().getFactory().createDependency();
            ret.setGroupId(groupId);
            ret.setArtifactId(artifactId);
            mdl.addDependency(ret);
        }
        // if managed dependency section is present, return that one for editing..
        return managed == null ? ret : managed;
    }


    public static boolean hasModelDependency(POMModel mdl, String groupid, String artifactid) {
        return checkModelDependency(mdl, groupid, artifactid, false) != null;
    }

    /**
     *
     * @param mdl
     * @param url of the repository
     * @param add true == add to model, will not add if the repo is in project but not in model (eg. central repo)
     * @return null if repository with given url exists, otherwise a returned newly created item.
     */
    public static Repository addModelRepository(MavenProject project, POMModel mdl, String url) {
        if (url.contains(RepositorySystem.DEFAULT_REMOTE_REPO_URL) || /* #212336 */url.contains("http://repo1.maven.org/maven2")) {
            return null;
        }
        List<Repository> repos = mdl.getProject().getRepositories();
        if (repos != null) {
            for (Repository r : repos) {
                if (url.equals(r.getUrl())) {
                    //already in model..either in pom.xml or added in this session.
                    return null;
                }
            }
        }
        
        List<org.apache.maven.model.Repository> reps = project.getRepositories();
        org.apache.maven.model.Repository prjret = null;
        Repository ret = null;
        if (reps != null) {
            for (org.apache.maven.model.Repository re : reps) {
                if (url.equals(re.getUrl())) {
                    prjret = re;
                    break;
                }
            }
        }
        if (prjret == null) {
            ret = mdl.getFactory().createRepository();
            ret.setUrl(url);
            ret.setId(url);
            mdl.getProject().addRepository(ret);
        }
        return ret;
    }

    /**
     * Sets the Java source level of a project.
     * Use {@link PluginPropertyUtils#getPluginProperty(Project,String,String,String,String)} first
     * ({@link Constants#GROUP_APACHE_PLUGINS}, {@link Constants#PLUGIN_COMPILER}, {@link Constants#SOURCE_PARAM}, {@code "compile"})
     * to make sure that the current level is actually not what you want.
     * @param mdl a POM model
     * @param sourceLevel the desired source level
     * @since 2.19
     */
    public static void setSourceLevel(POMModel mdl, String sourceLevel) {
        Plugin old = null;
        Plugin plugin;
        Build bld = mdl.getProject().getBuild();
        if (bld != null) {
            old = bld.findPluginById(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
        } else {
            mdl.getProject().setBuild(mdl.getFactory().createBuild());
        }
        if (old != null) {
            plugin = old;
        } else {
            plugin = mdl.getFactory().createPlugin();
            plugin.setGroupId(Constants.GROUP_APACHE_PLUGINS);
            plugin.setArtifactId(Constants.PLUGIN_COMPILER);
            plugin.setVersion(MavenVersionSettings.getDefault().getVersion(MavenVersionSettings.VERSION_COMPILER));
            mdl.getProject().getBuild().addPlugin(plugin);
        }
        Configuration conf = plugin.getConfiguration();
        if (conf == null) {
            conf = mdl.getFactory().createConfiguration();
            plugin.setConfiguration(conf);
        }
        conf.setSimpleParameter(Constants.SOURCE_PARAM, sourceLevel);
        conf.setSimpleParameter(Constants.TARGET_PARAM, sourceLevel);
    }

    /**
     * Returns child element of given parent, specified by its local name.
     * Creates such child in case it doesn't exist.
     *
     * @param parent parent element
     * @param localQName local name of the child
     * @param pomModel whole pom model
     * @return existing or newly created child
     */
    public static POMExtensibilityElement getOrCreateChild (POMComponent parent, String localQName, POMModel pomModel) {
        POMExtensibilityElement result = null;
        for (POMExtensibilityElement el : parent.getExtensibilityElements()) {
            if (localQName.equals(el.getQName().getLocalPart())) {
                result = el;
                break;
            }
        }

        if (result == null) {
            result = pomModel.getFactory().
                    createPOMExtensibilityElement(new QName(localQName));
            parent.addExtensibilityElement(result);
        }

        return result;
    }

    private static final String PROBABLE_ROOTS
            = "maven2|" // mainly for Central
            + "maven[.]repo|" // often used for Eclipse repos
            + "content/(?:groups|repositories|shadows)/[^/]+|" // Nexus
            + ".+(?=/(?:javax|org|net|com)/)"; // common groupId starters
    /**
     * 1 - root
     * 2 - groupId as slashes
     * 3 - artifactId
     * 4 - version
     */
    private static Pattern DEFAULT = Pattern.compile("(.+://[^/]+/(?:(?:.+/)?(?:" + PROBABLE_ROOTS + ")/)?)(.+)/([^/]+)/([^/]+)/\\3-\\4[.]pom");
    /**
     * 1 - root
     * 2 - groupId
     * 3 - artifactId
     * 4 - version
     */
    private static Pattern LEGACY = Pattern.compile("(.+/)([^/]+)/poms/([a-zA-Z0-9_]+[a-zA-Z_-]+)-([0-9].+)[.]pom");

    /** Returns a library descriptor corresponding to the given library,
     * or null if not recognized successfully.
     *
     * @param pom library to check
     * @return LibraryDescriptor corresponding to the library, or null if the pom URL format is not recognized.
     */
    @SuppressWarnings("SBSC_USE_STRINGBUFFER_CONCATENATION")
    public static LibraryDescriptor checkLibrary(URL pom) {
        String pomS;
        try {
            pomS = new URL(pom.getProtocol(), pom.getHost(), pom.getPort(), pom.getFile()).toString(); // strip ref
        } catch (MalformedURLException x) {
            pomS = pom.toString();
        }
        Matcher m1 = LEGACY.matcher(pomS);
        if (m1.matches()) {
            return new LibraryDescriptor("legacy", m1.group(1), m1.group(2), m1.group(3), m1.group(4), pom.getRef());
        }
        Matcher m2 = DEFAULT.matcher(pomS);
        if (m2.matches()) {
            return new LibraryDescriptor("default", m2.group(1), m2.group(2).replace('/', '.'), m2.group(3), m2.group(4), pom.getRef());
        }
        return null;
    }

    public static class LibraryDescriptor {
        private String repoType /* default/legacy */, repoRoot, groupId, artifactId,
                version, classifier /* optional, not part of path, but url's ref */;

        LibraryDescriptor(String repoType, String repoRoot, String groupId, String artifactId, String version, String classifier) {
            this.repoType = repoType;
            this.repoRoot = repoRoot;
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.classifier = classifier;
        }

        public String getArtifactId() {
            return artifactId;
        }

        /** May return null. */
        public String getClassifier() {
            return classifier;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getRepoRoot() {
            return repoRoot;
        }

        public String getRepoType() {
            return repoType;
        }

        public String getVersion() {
            return version;
        }

        @Override public String toString() {
            return "LibraryDescriptor{" + "repoType=" + repoType + ", repoRoot=" + repoRoot + ", groupId=" + groupId + ", artifactId=" + artifactId + ", version=" + version + ", classifier=" + classifier + '}';
        }

    }

}
