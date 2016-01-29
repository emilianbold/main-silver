/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.build2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.FutureTask;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.docker.api.DockerImage;
import org.netbeans.modules.docker.api.DockerIntegration;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public class BuildImageWizard {

    public static final String INSTANCE_PROPERTY = "instance";

    public static final String BUILD_CONTEXT_PROPERTY = "buildContext";

    public static final String REPOSITORY_PROPERTY = "repository";

    public static final String TAG_PROPERTY = "tag";

    public static final String DOCKERFILE_PROPERTY = "dockerfile";

    public static final String PULL_PROPERTY = "pull";

    public static final String NO_CACHE_PROPERTY = "noCache";

    public static final boolean PULL_DEFAULT = false;

    public static final boolean NO_CACHE_DEFAULT = false;

    private static final Map<InputOutput, Pair<RerunAction, StopAction>> IO_CACHE = new WeakHashMap<>();

    private final RequestProcessor requestProcessor = new RequestProcessor(BuildImageWizard.class);

    private DockerInstance instance;

    private File dockerfile;

    public DockerInstance getInstance() {
        return instance;
    }

    public void setInstance(DockerInstance instance) {
        this.instance = instance;
    }

    public File getDockerfile() {
        return dockerfile;
    }

    public void setDockerfile(File dockerfile) {
        this.dockerfile = dockerfile;
    }

    @NbBundle.Messages("LBL_BuildImage=Build Image")
    public void show() {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        if (instance == null) {
            panels.add(new BuildInstancePanel());
        }
        panels.add(new BuildContextPanel());
        panels.add(new BuildOptionsPanel());
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            JComponent c = (JComponent) panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            c.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
        }

        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(Bundle.LBL_BuildImage());

        if (instance != null) {
            wiz.putProperty(INSTANCE_PROPERTY, instance);
        }
        if (dockerfile != null && dockerfile.isFile()) {
            wiz.putProperty(BUILD_CONTEXT_PROPERTY, dockerfile.getParentFile().getAbsolutePath());
            wiz.putProperty(DOCKERFILE_PROPERTY, dockerfile.getName());
        }

        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            Boolean pull = (Boolean) wiz.getProperty(PULL_PROPERTY);
            Boolean noCache = (Boolean) wiz.getProperty(NO_CACHE_PROPERTY);
            build((DockerInstance) wiz.getProperty(INSTANCE_PROPERTY),
                    (String) wiz.getProperty(BUILD_CONTEXT_PROPERTY),
                    (String) wiz.getProperty(DOCKERFILE_PROPERTY),
                    (String) wiz.getProperty(REPOSITORY_PROPERTY),
                    (String) wiz.getProperty(TAG_PROPERTY),
                    pull != null ? pull : PULL_DEFAULT,
                    noCache != null ? noCache : NO_CACHE_DEFAULT);
        }
    }

    static boolean isFinishable(String buildContext, String dockerfile) {
        if (buildContext == null) {
            return false;
        }
        String realDockerfile = dockerfile;
        if (realDockerfile == null) {
            realDockerfile = DockerAction.DOCKER_FILE;
        }
        File file = new File(realDockerfile);
        if (!file.isAbsolute()) {
            file = new File(buildContext, realDockerfile);
        }

        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        // the last check avoids entires like Dockerfile/ to be considered valid files
        if (fo == null || !fo.isData() || !realDockerfile.endsWith(fo.getNameExt())) {
            return false;
        }
        FileObject build = FileUtil.toFileObject(FileUtil.normalizeFile(new File(buildContext)));
        if (build == null) {
            return false;
        }
        return FileUtil.isParentOf(build, fo);
    }

    private void build(final DockerInstance instance, final String buildContext,
            final String dockerfile, final String repository, final String tag,
            final boolean pull, final boolean noCache) {

        RerunAction r = new RerunAction(requestProcessor);
        StopAction s = new StopAction();

        final InputOutput io;
        synchronized (BuildImageWizard.class) {
            io = IOProvider.getDefault().getIO(Bundle.MSG_Building(buildContext),
                    false, new Action[]{r, s}, null);

            Pair<RerunAction, StopAction> actions = IO_CACHE.get(io);
            if (actions == null) {
                IO_CACHE.put(io, Pair.of(r, s));
            } else {
                r = actions.first();
                s = actions.second();
            }
        }

        final RerunAction rerun = r;
        final StopAction stop = s;

        stop.setEnabled(false);
        rerun.setEnabled(false);

        requestProcessor.post(new Runnable() {
            @Override
            public void run() {
                File file = null;
                if (dockerfile != null) {
                    file = new File(dockerfile);
                    if (!file.isAbsolute()) {
                        file = new File(buildContext, dockerfile);
                    }
                }

                BuildTask.Hook hook = new BuildTask.Hook() {
                    @Override
                    public void onStart(FutureTask<DockerImage> task) {
                        stop.configure(task);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                stop.setEnabled(true);
                            }
                        });
                    }

                    @Override
                    public void onFinish() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                stop.setEnabled(false);
                                if (rerun.isAvailable()) {
                                    rerun.setEnabled(true);
                                }
                            }
                        });
                    }
                };

                BuildTask task = new BuildTask(instance, io, hook, FileUtil.normalizeFile(new File(buildContext)),
                        FileUtil.normalizeFile(file), repository, tag, pull, noCache);
                rerun.configure(task);
                task.run();
            }
        });
    }

    private static class StopAction extends AbstractAction {

        private FutureTask<DockerImage> task;

        @NbBundle.Messages("LBL_Stop=Stop")
        public StopAction() {
            setEnabled(false); // initially, until ready
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/docker/ui/resources/action_stop.png", false)); // NOI18N
            putValue(Action.SHORT_DESCRIPTION, Bundle.LBL_Stop());
        }

        public synchronized void configure(FutureTask<DockerImage> task) {
            this.task = task;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false); // discourage repeated clicking

            FutureTask<DockerImage> actionTask;
            synchronized (this) {
                actionTask = task;
            }

            if (actionTask != null) {
                actionTask.cancel(true);
            }
        }
    }

    private static class RerunAction extends AbstractAction {

        private final RequestProcessor requestProcessor;

        private BuildTask buildTask;

        private ActionStateListener listener;

        private boolean available = true;

        @NbBundle.Messages("LBL_Rerun=Rerun")
        public RerunAction(RequestProcessor requestProcessor) {
            this.requestProcessor = requestProcessor;

            setEnabled(false); // initially, until ready
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/docker/ui/resources/action_rerun.png", false)); // NOI18N
            putValue(Action.SHORT_DESCRIPTION, Bundle.LBL_Rerun());
        }

        public synchronized boolean isAvailable() {
            return available;
        }

        public synchronized void configure(BuildTask buildTask) {
            detach();
            this.buildTask = buildTask;
            this.available = true;
            attach(buildTask);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false); // discourage repeated clicking

            requestProcessor.post(buildTask);
        }

        public synchronized void attach(BuildTask buildTask) {
            FileObject fo = FileUtil.toFileObject(buildTask.getDockerfile());

            this.buildTask = buildTask;
            this.listener = new ActionStateListener(this, buildTask.getInstance(), fo);

            fo.addFileChangeListener(listener);
            DockerIntegration.getDefault().addChangeListener(listener);

            listener.refresh();
        }

        public synchronized void detach() {
            if (buildTask == null) {
                return;
            }

            FileObject fo = listener.getFileObject();

            fo.removeFileChangeListener(listener);
            DockerIntegration.getDefault().removeChangeListener(listener);

            buildTask = null;
            listener = null;
            available = false;

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setEnabled(false);
                }
            });
        }
    }

    private static class ActionStateListener implements ChangeListener, FileChangeListener {

        private final RerunAction action;

        private final WeakReference<DockerInstance> instance;

        private final FileObject fo;

        public ActionStateListener(RerunAction action, WeakReference<DockerInstance> instance, FileObject fo) {
            this.action = action;
            this.instance = instance;
            this.fo = fo;
        }

        public FileObject getFileObject() {
            return fo;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refresh();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            refresh();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            refresh();
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            // noop
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            // noop
        }

        @Override
        public void fileChanged(FileEvent fe) {
            // noop
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // noop
        }

        private void refresh() {
            if (fo == null || !fo.isValid()) {
                action.detach();
            }
            DockerInstance inst = this.instance.get();
            DockerIntegration integration = DockerIntegration.getDefault();
            if (inst == null || !integration.getInstances().contains(inst)) {
                action.detach();
            }
        }

    }
}
