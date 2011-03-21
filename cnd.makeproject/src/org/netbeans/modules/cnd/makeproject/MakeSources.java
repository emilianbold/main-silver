/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.cnd.makeproject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakefileConfiguration;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * Handles source dir list for a freeform project.
 * XXX will not correctly unregister released external source roots
 */
public final class MakeSources implements Sources, AntProjectListener {

    private static final String GENERIC = "generic"; // NOI18N
    private MakeProject project;
    private AntProjectHelper helper;

    public MakeSources(MakeProject project, AntProjectHelper helper) {
        this.project = project;
        this.helper = helper;
        helper.addAntProjectListener(MakeSources.this);
        changeSupport = new ChangeSupport(this);
    }
    private Sources delegate;
    private final ChangeSupport changeSupport;
    private long eventID = 0;

    @Override
    public SourceGroup[] getSourceGroups(String str) {
        if (!str.equals(GENERIC)) { // NOI18N
            return new SourceGroup[0];
        }
        Sources srcs;
        long curEvent;
        synchronized (this) {
            srcs = delegate;
            curEvent = eventID++;
        }

        AtomicBoolean completeSouces = new AtomicBoolean(true);
        if (srcs == null) {
            srcs = initSources(completeSouces);
        }

        synchronized (this) {
            if (curEvent == eventID && completeSouces.get()) {
                delegate = srcs;
            }
        }
        SourceGroup[] sg = srcs.getSourceGroups(str);
        return sg;
    }

    public List<String> getSourceRootsFromProjectXML() {
        Element data = helper.getPrimaryConfigurationData(true);
        if (data.getElementsByTagName(MakeProjectType.SOURCE_ROOT_LIST_ELEMENT).getLength() > 0) {
            List<String> list = new ArrayList<String>();
            NodeList nl4 = data.getElementsByTagName(MakeProjectType.SOURCE_ROOT_ELEMENT);
            if (nl4.getLength() > 0) {
                for (int i = 0; i < nl4.getLength(); i++) {
                    Node node = nl4.item(i);
                    NodeList nl2 = node.getChildNodes();
                    for (int j = 0; j < nl2.getLength(); j++) {
                        String typeTxt = nl2.item(j).getNodeValue();
                        String sRoot = typeTxt;
                        list.add(sRoot);
                    }
                }
            }
            return list;
        }
        return null;
    }

    public List<String> getAbsoluteSourceRootsFromProjectXML() {
        String baseDir = FileUtil.toFile(helper.getProjectDirectory()).getPath();
        List<String> sourceRoots = getSourceRootsFromProjectXML();
        List<String> absSourceRoots = null;
        if (sourceRoots != null) {
            absSourceRoots = new ArrayList<String>();
            for (String sRoot : sourceRoots) {
                String absSRoot = CndPathUtilitities.toAbsolutePath(baseDir, sRoot);
                absSourceRoots.add(absSRoot);
            }
        }
        return absSourceRoots;
    }

    private Sources initSources(AtomicBoolean completeSouces) {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        Set<String> sourceRootList = null;//new LinkedHashSet<String>();        

        // Try project.xml first if project not already read (this is cheap)
        if (!pdp.gotDescriptor()) {
            completeSouces.set(false);
            List<String> absSourceRoots = getAbsoluteSourceRootsFromProjectXML();
            if (absSourceRoots != null) {
                sourceRootList = new LinkedHashSet<String>();
                sourceRootList.addAll(absSourceRoots);
            }
        }
        if (sourceRootList == null) {
            sourceRootList = new LinkedHashSet<String>();
            MakeConfigurationDescriptor pd = pdp.getConfigurationDescriptor(true);
            if (pd != null) {
                // Add external folders to sources.
                if (pd.getVersion() < 41) {
                    Item[] projectItems = pd.getProjectItems();
                    if (projectItems != null) {
                        for (int i = 0; i < projectItems.length; i++) {
                            Item item = projectItems[i];
                            String name = item.getPath();
                            if (!CndPathUtilitities.isPathAbsolute(name)) {
                                continue;
                            }
                            File file = new File(name);
                            if (!file.exists()) {
                                continue;
                            }
                            if (!file.isDirectory()) {
                                file = file.getParentFile();
                            }
                            name = file.getPath();
                            sourceRootList.add(name);
                            pd.addSourceRootRaw(CndPathUtilitities.toRelativePath(pd.getBaseDir(), name));
                        }
                    }
                }
                // Add source roots to set (>= V41)
                List<String> list = pd.getAbsoluteSourceRoots();
                for (String sr : list) {
                    sourceRootList.add(sr);
                }

                // Add buildfolder from makefile projects to sources. See IZ 90190.
                if (pd.getVersion() < 41) {
                    Configuration[] confs = pd.getConfs().toArray();
                    for (int i = 0; i < confs.length; i++) {
                        MakeConfiguration makeConfiguration = (MakeConfiguration) confs[i];
                        if (makeConfiguration.isMakefileConfiguration()) {
                            MakefileConfiguration makefileConfiguration = makeConfiguration.getMakefileConfiguration();
                            String path = makefileConfiguration.getAbsBuildCommandWorkingDir();
                            sourceRootList.add(path);
                            pd.addSourceRootRaw(CndPathUtilitities.toRelativePath(pd.getBaseDir(), path));
                        }
                    }
                }

            } else {
                completeSouces.set(false);
            }
        }
        if (project.getRemoteMode() == RemoteProject.Mode.REMOTE_SOURCES) {
            FileObjectBasedSources sources = new FileObjectBasedSources();
            MakeConfigurationDescriptor pd = pdp.getConfigurationDescriptor(true);            
            if (pd != null) {
                String baseDir = pd.getBaseDir();
                Set<FileObject> added = new HashSet<FileObject>();
                ExecutionEnvironment fsEnv = project.getRemoteFileSystemHost();                
                sourceRootList.add(baseDir); // add remote project itself to the tail
                for (String name : sourceRootList) {
                    String path = CndPathUtilitities.toAbsolutePath(baseDir, name);
                    path = RemoteFileUtil.normalizeAbsolutePath(path, fsEnv);
                    String displayName = fsEnv.getDisplayName() + ":" + path; //NOI18N
                    FileObject fo = RemoteFileUtil.getFileObject(path, fsEnv);
                    if (!added.contains(fo)) {
                        if (fo == null) {
                            new NullPointerException("Null file object for " + fsEnv + ':' + path).printStackTrace(); //NOI18N
                        } else {
                            sources.addGroup(project, GENERIC, fo, displayName);
                            added.add(fo);
                        }
                    }
                }
            } else {
                completeSouces.set(false);
            }
            sources.addGroup(project, GENERIC, project.getProjectDirectory(), 
                        NbBundle.getMessage(MakeSources.class, "SHADOW_METADATA_DISPLAY_NAME"));
            return sources;
        } else {
            String baseDir = helper.getProjectDirectory().getPath();
            SourcesHelper h = new SourcesHelper(project, helper, project.evaluator());
            for (String name : sourceRootList) {
                String displayName = CndPathUtilitities.toRelativePath(baseDir, name);
                displayName = CndPathUtilitities.naturalizeSlashes(displayName);
                h.sourceRoot(name).displayName(displayName).add();
                h.sourceRoot(name).type(GENERIC).displayName(displayName).add(); // NOI18N
            }
            h.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
            return h.createSources();
        }
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        changeSupport.addChangeListener(changeListener);
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        changeSupport.removeChangeListener(changeListener);
    }

    private void fireChange() {
        delegate = null;
        changeSupport.fireChange();
    }

    @Override
    public void configurationXmlChanged(AntProjectEvent ev) {
        // fireChange(); // ignore - cnd projects don't keep source file info in project.xml
    }

    public void descriptorChanged() {
        // fireChange(); // ignore
    }

    @Override
    public void propertiesChanged(AntProjectEvent ev) {
        // ignore
    }

    public void sourceRootsChanged() {
        fireChange();
    }
}
