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

package org.netbeans.modules.maven;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.api.ProjectProfileHandler;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenSettingsSingleton;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.util.NbCollections;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Anuradha G
 */
public class ProjectProfileHandlerImpl implements ProjectProfileHandler {

    private static final String PROFILES = "profiles";//NOI18N
    private static final String ACTIVEPROFILES = "activeProfiles";//NOI18N
    private static final String SEPARATOR = " ";//NOI18N
    private static final String NAMESPACE = null;//FIXME add propper namespase
    private List<String> privateProfiles = new ArrayList<String>();
    private List<String> sharedProfiles = new ArrayList<String>();
    private AuxiliaryConfiguration ac;
    private final NbMavenProjectImpl nmp;
    private List<Model> lineage;

    ProjectProfileHandlerImpl(NbMavenProjectImpl nmp, AuxiliaryConfiguration ac) {
        this.nmp = nmp;
        this.ac = ac;
        privateProfiles.addAll(retrieveActiveProfiles(ac, false));
        sharedProfiles.addAll(retrieveActiveProfiles(ac, true));
    }

    /**
     * reset caching of the lineage, invoked from MavenProject reloads
     */
    synchronized void clearLineageCache() {
        lineage = null;
    }

    /**
     * cache the lineage for repeated use
     */
    private synchronized List<Model> getLineage() {
        if (lineage == null) {
            try {
                lineage = EmbedderFactory.createModelLineage(nmp.getPOMFile(), nmp.getEmbedder());
            } catch (ModelBuildingException ex) {
                Logger.getLogger(ProjectProfileHandlerImpl.class.getName()).log(Level.FINE, "Error reading model lineage", ex);//NOI18N
                lineage = Collections.emptyList();
            }
        }
        return lineage;
    }

    public @Override List<String> getAllProfiles() {
        Set<String> profileIds = new HashSet<String>();
        //pom+profiles.xml profiles come first
        extractProfiles(profileIds);
        //Add settings file Properties
        profileIds.addAll(NbCollections.checkedMapByFilter(MavenSettingsSingleton.getInstance().createUserSettingsModel().getProfilesAsMap(), String.class, org.apache.maven.settings.Profile.class, true).keySet());

        return new ArrayList<String>(profileIds);
    }

    public @Override List<String> getActiveProfiles(boolean shared) {
       return new ArrayList<String>(shared ? sharedProfiles : privateProfiles);
    }
    public @Override List<String> getMergedActiveProfiles(boolean shared) {
        Set<String> profileIds = new HashSet<String>();
        MavenProject mavenProject = nmp.getOriginalMavenProject();
        List<Profile> profiles = mavenProject.getActiveProfiles();
        for (Profile profile : profiles) {
            profileIds.add(profile.getId());
        }
        //read from Settings.xml
        List<String> profileStrings = MavenSettingsSingleton.getInstance().createUserSettingsModel().getActiveProfiles();
        for (String profile : profileStrings) {
            profileIds.add(profile);
        }
        
        /* MNG-4060: no longer supported
        File basedir = FileUtil.normalizeFile(mavenProject.getBasedir());
        FileObject fileObject = FileUtil.toFileObject(basedir);
        if (fileObject != null) {//144159
            //read from profiles.xml
            FileObject profilesFo = fileObject.getFileObject("profiles.xml");
            if (profilesFo != null) {
                ModelSource ms = Utilities.createModelSource(profilesFo);
                ProfilesModel pm = ProfilesModelFactory.getDefault().getModel(ms);
                if (State.VALID.equals(pm.getState())) {
                    List<String> actProfs = pm.getProfilesRoot().getActiveProfiles();
                    if (actProfs != null) {
                        profileIds.addAll(actProfs);
                    }
                }
            }
        }
         */
        profileIds.addAll(getActiveProfiles(shared));
        return new ArrayList<String>(profileIds);
    }

    public @Override void disableProfile(String id, boolean shared) {
        Element element = ac.getConfigurationFragment(PROFILES, NAMESPACE, shared);
        if (element == null) {

            String root = "project-private"; // NOI18N"

            Document doc = XMLUtil.createDocument(root, NAMESPACE, null, null);
            element = doc.createElementNS(NAMESPACE, PROFILES);
        }
        String activeProfiles = element.getAttributeNS(NAMESPACE, ACTIVEPROFILES);

        if (activeProfiles != null && activeProfiles.length() > 0) {
            StringTokenizer tokenizer = new StringTokenizer(activeProfiles, SEPARATOR);
            Set<String> set = new HashSet<String>(tokenizer.countTokens());
            while (tokenizer.hasMoreTokens()) {
                set.add(tokenizer.nextToken());
            }
            set.remove(id);
            StringBuilder buffer = new StringBuilder();
            for (String profle : set) {
                buffer.append(profle).append(SEPARATOR);
            }
            element.setAttributeNS(NAMESPACE, ACTIVEPROFILES, buffer.toString().trim());
        }

        ac.putConfigurationFragment(element, shared);
        if(shared){
            sharedProfiles.remove(id);
        }else{
            privateProfiles.remove(id);
        }
    }

    public @Override void enableProfile(String id, boolean shared) {
        Element element = ac.getConfigurationFragment(PROFILES, NAMESPACE, shared);
        if (element == null) {

            String root = "project-private"; // NOI18N"

            Document doc = XMLUtil.createDocument(root, NAMESPACE, null, null);
            element = doc.createElementNS(NAMESPACE, PROFILES);
        }


        String activeProfiles = element.getAttributeNS(NAMESPACE, ACTIVEPROFILES);
        element.setAttributeNS(NAMESPACE, ACTIVEPROFILES, activeProfiles + SEPARATOR + id);
        ac.putConfigurationFragment(element, shared);
        if (shared) {
            if (!sharedProfiles.contains(id)) {
                sharedProfiles.add(id);
            }
        } else {
            if (!privateProfiles.contains(id)) {
                privateProfiles.add(id);
            }
        }
    }

    private void extractProfiles(Set<String> profileIds) {
        extractProfilesFromModelLineage(profileIds);
        /* MNG-4060: no longer supported
        File basedir = FileUtil.normalizeFile(nmp.getPOMFile().getParentFile());
        FileObject fileObject = FileUtil.toFileObject(basedir);
        //read from profiles.xml
        if (fileObject != null) { //144159
            FileObject profiles = fileObject.getFileObject("profiles.xml");
            if (profiles != null) {
                // using xpp3 reader because xam based model needs project instance (for encoding)
                // and might deadlock
                ProfilesXpp3Reader reader = new ProfilesXpp3Reader();
                InputStream in = null;
                try {
                    in = profiles.getInputStream();
                    ProfilesRoot root = reader.read(in);
                    List<org.apache.maven.profiles.Profile> profs = root.getProfiles();
                    for (org.apache.maven.profiles.Profile prf : profs) {
                        profileIds.add(prf.getId());
                    }
                } catch (Exception e) {
                    Logger.getLogger(ProjectProfileHandlerImpl.class.getName()).log(Level.FINE, "Error while retrieving profiles from profiles.xml file. Ignore.", e); //NOI18N
                } finally {
                    IOUtil.close(in);
                }
            }
        }
         */
    }
    
    private void extractProfilesFromModelLineage(Set<String> profileIds) {
        /* Cannot use this as it would trigger a stack overflow when loading the project:
        for (Profile profile : nmp.getOriginalMavenProject().getModel().getProfiles()) {
            profileIds.add(profile.getId());
        }
         */
        for (Model model : getLineage()) {
            for (Profile profile : model.getProfiles()) {
                profileIds.add(profile.getId());
            }
        }
    }

    private List<String> retrieveActiveProfiles(AuxiliaryConfiguration ac, boolean shared) {

        Set<String> prifileides = new HashSet<String>();
        Element element = ac.getConfigurationFragment(PROFILES, NAMESPACE, shared);
        if (element != null) {

            String activeProfiles = element.getAttributeNS(NAMESPACE, ACTIVEPROFILES);

            if (activeProfiles != null && activeProfiles.length() > 0) {
                StringTokenizer tokenizer = new StringTokenizer(activeProfiles, SEPARATOR);

                while (tokenizer.hasMoreTokens()) {
                    prifileides.add(tokenizer.nextToken());
                }
            }
        }
        return new ArrayList<String>(prifileides);
    }

}
