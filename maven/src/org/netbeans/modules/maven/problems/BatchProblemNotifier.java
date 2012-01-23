/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.problems;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.modules.maven.execute.ReactorChecker;
import org.netbeans.modules.maven.execute.ui.RunGoalsPanel;
import static org.netbeans.modules.maven.problems.Bundle.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;

/**
 * Keeps track of problems in all open Maven projects and offers to do reactor sanity builds.
 */
public class BatchProblemNotifier {

    private static final Logger LOG = Logger.getLogger(BatchProblemNotifier.class.getName());

    /** reactors by root directory */
    private static final Map<File,Reactor> reactors = new HashMap<File,Reactor>();
    private static final Map<NbMavenProjectImpl,File> roots = new WeakHashMap<NbMavenProjectImpl,File>();

    public static void opened(NbMavenProjectImpl p) {
        ProblemReporterImpl pr = p.getProblemReporter();
        pr.doBaseProblemChecks(p.getOriginalMavenProject());
        Set<Artifact> missingArtifacts = pr.getMissingArtifacts();
        if (!missingArtifacts.isEmpty()) {
            Set<File> files = new HashSet<File>();
            for (Artifact art : missingArtifacts) {
                File file = art.getFile();
                if (file != null) {
                    files.add(file);
                } else {
                    LOG.log(Level.WARNING, "no file found for {0}", art);
                    // XXX could also store IDs and use MavenFileOwnerQueryImpl.findCoordinates for reverse mapping if necessary
                }
            }
            final File root = ReactorChecker.findReactor(p.getProjectWatcher()).getMavenProject().getBasedir();
            synchronized (roots) {
                roots.put(p, root);
            }
            File basedir = p.getPOMFile().getParentFile();
            String path = FileUtilities.relativizeFile(root, basedir);
            if (path == null) {
                path = basedir.getAbsolutePath();
            }
            synchronized (reactors) {
                Reactor reactor = reactors.get(root);
                if (reactor == null) {
                    reactor = new Reactor(root);
                    reactors.put(root, reactor);
                }
                reactor.register(path, files);
            }
        }
    }

    public static void closed(NbMavenProjectImpl p) {
        File root;
        synchronized (roots) {
            root = roots.remove(p);
        }
        if (root == null) {
            return;
        }
        File basedir = p.getPOMFile().getParentFile();
        String path = FileUtilities.relativizeFile(root, basedir);
        if (path == null) {
            path = basedir.getAbsolutePath();
        }
        synchronized (reactors) {
            Reactor reactor = reactors.get(root);
            if (reactor != null) {
                reactor.unregister(path);
            }
        }
    }

    static void resolved(File f) {
        synchronized (reactors) {
            for (Reactor reactor : new ArrayList<Reactor>(reactors.values())) {
                reactor.resolved(f);
            }
        }
    }

    @Messages({
        "dialog_title=Run Priming Build",
        "# {0} - directory name of reactor", "build_label=Priming {0}"
    })
    private static void showUI(File reactor, Set<String> projects) {
        RunGoalsPanel pnl = new RunGoalsPanel();
        BeanRunConfig cfg = new BeanRunConfig();
        String label = build_label(reactor.getName());
        cfg.setExecutionName(label);
        cfg.setTaskDisplayName(label);
        cfg.setExecutionDirectory(reactor);
        try {
            FileObject reactorFO = FileUtil.toFileObject(reactor);
            if (reactorFO != null && reactorFO.isFolder()) {
                Project reactorP = ProjectManager.getDefault().findProject(reactorFO);
                if (reactorP != null) {
                    cfg.setProject(reactorP);
                    // Similar to ReactorChecker, except there can be multiple submodules to build.
                    M2Configuration m2c = reactorP.getLookup().lookup(M2ConfigProvider.class).getActiveConfiguration();
                    if (m2c != null) {
                        cfg.setActivatedProfiles(m2c.getActivatedProfiles());
                    }
                }
            }
        } catch (IOException x) {
            LOG.log(Level.FINE, null, x);
        }
        StringBuilder pl = new StringBuilder();
        for (String project : projects) {
            if (pl.length() > 0) {
                pl.append(',');
            }
            pl.append(project);
        }
        // validate, test-compile, dependency:go-offline also possible
        cfg.setGoals(Arrays.asList("--fail-at-end", "--also-make", "--projects", pl.toString(), "install"));
        pnl.readConfig(cfg);
        DialogDescriptor dd = new DialogDescriptor(pnl, dialog_title());
        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            LOG.log(Level.FINE, "running build for {0}", reactor);
            pnl.applyValues(cfg);
            RunUtils.run(cfg);
        }
    }

    private static class Reactor implements ActionListener {

        final File root;

        /** affected open projects, as (usually) relative paths from reactor root to sets of artifacts still missing */
        final Map<String,Set<File>> projects = new TreeMap<String,Set<File>>();

        final Notification n;

        @Messages({
            "# {0} - directory basename", "build_title=Build {0}",
            "# {0} - full directory path", "build_details=Run priming build in {0}"
        })
        Reactor(File root) {
            assert Thread.holdsLock(reactors);
            this.root = root;
            n = NotificationDisplayer.getDefault().notify(
                build_title(root.getName()),
                ImageUtilities.image2Icon(ImageUtilities.mergeImages(
                    ImageUtilities.loadImage("org/netbeans/modules/maven/resources/Maven2Icon.gif", true),
                    ImageUtilities.loadImage("org/netbeans/modules/maven/brokenProjectBadge.png", true), 8, 0)),
                build_details(root), this);
            LOG.log(Level.FINE, "created for {0}", root);
        }

        void register(String path, Set<File> files) {
            assert Thread.holdsLock(reactors);
            projects.put(path, files);
            LOG.log(Level.FINE, "registered {0} for {1} in {2}", new Object[] {files, path, root});
        }

        void unregister(String path) {
            assert Thread.holdsLock(reactors);
            projects.remove(path);
            LOG.log(Level.FINE, "unregistered {0} in {1}", new Object[] {path, root});
            checkComplete();
        }

        void resolved(File f) {
            assert Thread.holdsLock(reactors);
            Iterator<Map.Entry<String,Set<File>>> it = projects.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String,Set<File>> entry = it.next();
                String path = entry.getKey();
                Set<File> files = entry.getValue();
                if (files.remove(f)) {
                    LOG.log(Level.FINE, "resolved {0} for {1} in {2}", new Object[] {f, path, root});
                }
                if (files.isEmpty()) {
                    it.remove();
                    LOG.log(Level.FINE, "completed {0} in {1}", new Object[] {path, root});
                }
            }
            checkComplete();
        }

        private void checkComplete() {
            if (projects.isEmpty()) {
                n.clear();
                reactors.remove(root);
                LOG.log(Level.FINE, "completed {0}", root);
            }
        }

        @Override public void actionPerformed(ActionEvent e) {
            LOG.log(Level.FINE, "showing UI for {0} with {1}", new Object[] {Reactor.this.root, projects.keySet()});
            synchronized (reactors) {
                reactors.remove(Reactor.this.root);
            }
            showUI(Reactor.this.root, projects.keySet());
        }
        
    }

    private BatchProblemNotifier() {}

}
