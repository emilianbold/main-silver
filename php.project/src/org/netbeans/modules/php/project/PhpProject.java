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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.php.project;

import java.awt.Image;
import org.netbeans.modules.php.project.copysupport.CopySupport;
import org.netbeans.modules.php.project.ui.logicalview.PhpLogicalViewProvider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchInfoUtils;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.php.api.phpmodule.BadgeIcon;
import org.netbeans.modules.php.api.phpmodule.PhpFrameworks;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.project.api.PhpLanguageProperties;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.php.project.api.PhpSeleniumProvider;
import org.netbeans.modules.php.project.classpath.BasePathSupport;
import org.netbeans.modules.php.project.classpath.ClassPathProviderImpl;
import org.netbeans.modules.php.project.classpath.IncludePathClassPathProvider;
import org.netbeans.modules.php.project.internalserver.InternalWebServer;
import org.netbeans.modules.php.project.ui.actions.support.ConfigAction;
import org.netbeans.modules.php.project.ui.codecoverage.PhpCoverageProvider;
import org.netbeans.modules.php.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.IgnorePathSupport;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.modules.php.spi.phpmodule.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleIgnoredFilesExtender;
import org.netbeans.modules.web.common.spi.ProjectWebRootProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.FilterPropertyProvider;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.netbeans.spi.search.SubTreeSearchOptions;
import org.netbeans.spi.search.provider.TerminationFlag;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 * @author ads, Tomas Mysik
 */
@AntBasedProjectRegistration(
    type=PhpProjectType.TYPE,
    iconResource="org/netbeans/modules/php/project/ui/resources/phpProject.png",
    sharedNamespace=PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE,
    privateNamespace=PhpProjectType.PRIVATE_CONFIGURATION_NAMESPACE
)
public final class PhpProject implements Project {
    static final Logger LOGGER = Logger.getLogger(PhpProject.class.getName());

    final AntProjectHelper helper;
    final UpdateHelper updateHelper;
    private final ReferenceHelper refHelper;
    private final PropertyEvaluator eval;
    private final Lookup lookup;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;
    private final SourceRoots seleniumRoots;

    private final SearchFilterDefinition fileObjectFilter = new PhpSearchFilterDef();

    // all next properties are guarded by PhpProject.this lock as well so it could be possible to break this lock to individual locks
    // #165136
    // @GuardedBy(ProjectManager.mutex())
    volatile FileObject sourcesDirectory;
    // @GuardedBy(ProjectManager.mutex())
    volatile FileObject testsDirectory;
    // @GuardedBy(ProjectManager.mutex())
    volatile FileObject seleniumDirectory;
    // @GuardedBy(ProjectManager.mutex())
    volatile FileObject webRootDirectory;

    volatile String name;
    private final AntProjectListener phpAntProjectListener = new PhpAntProjectListener();
    private final PropertyChangeListener projectPropertiesListener = new ProjectPropertiesListener();

    // @GuardedBy(ProjectManager.mutex())
    volatile Set<BasePathSupport.Item> ignoredFolders;
    final Object ignoredFoldersLock = new Object();
    // changes in ignored files - special case because of PhpVisibilityQuery
    final ChangeSupport ignoredFoldersChangeSupport = new ChangeSupport(this);

    // frameworks
    private volatile boolean frameworksDirty = true;
    final List<PhpFrameworkProvider> frameworks = new CopyOnWriteArrayList<PhpFrameworkProvider>();
    private final FileChangeListener sourceDirectoryFileChangeListener = new SourceDirectoryFileChangeListener();
    private final LookupListener frameworksListener = new FrameworksListener();

    // project's property changes
    public static final String PROP_FRAMEWORKS = "frameworks"; // NOI18N
    public static final String PROP_WEB_ROOT = "webRoot"; // NOI18N
    final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final Set<PropertyChangeListener> propertyChangeListeners = new WeakSet<PropertyChangeListener>();

    public PhpProject(AntProjectHelper helper) {
        assert helper != null;

        this.helper = helper;
        updateHelper = new UpdateHelper(UpdateImplementation.NULL, helper);
        AuxiliaryConfiguration configuration = helper.createAuxiliaryConfiguration();
        eval = createEvaluator();
        refHelper = new ReferenceHelper(helper, configuration, getEvaluator());
        sourceRoots = SourceRoots.create(updateHelper, eval, refHelper, SourceRoots.Type.SOURCES);
        testRoots = SourceRoots.create(updateHelper, eval, refHelper, SourceRoots.Type.TESTS);
        seleniumRoots = SourceRoots.create(updateHelper, eval, refHelper, SourceRoots.Type.SELENIUM);
        lookup = createLookup(configuration);

        addWeakPropertyEvaluatorListener(projectPropertiesListener);
        helper.addAntProjectListener(WeakListeners.create(AntProjectListener.class, phpAntProjectListener, helper));
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    PropertyEvaluator getEvaluator() {
        return eval;
    }

    void addWeakPropertyEvaluatorListener(PropertyChangeListener listener) {
        eval.addPropertyChangeListener(WeakListeners.propertyChange(listener, eval));
    }

    void addWeakIgnoredFilesListener(ChangeListener listener) {
        ignoredFoldersChangeSupport.addChangeListener(WeakListeners.change(listener, ignoredFoldersChangeSupport));

        VisibilityQuery visibilityQuery = VisibilityQuery.getDefault();
        visibilityQuery.addChangeListener(WeakListeners.change(listener, visibilityQuery));
    }

    // add as a weak listener, only once
    boolean addWeakPropertyChangeListener(PropertyChangeListener listener) {
        if (!propertyChangeListeners.add(listener)) {
            // already added
            return false;
        }
        addPropertyChangeListener(WeakListeners.propertyChange(listener, propertyChangeSupport));
        return true;
    }

    void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public SearchFilterDefinition getFileObjectFilter() {
        return fileObjectFilter;
    }

    private PropertyEvaluator createEvaluator() {
        // It is currently safe to not use the UpdateHelper for PropertyEvaluator; UH.getProperties() delegates to APH
        // Adapted from APH.getStandardPropertyEvaluator (delegates to ProjectProperties):
        PropertyEvaluator baseEval1 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(PhpConfigurationProvider.CONFIG_PROPS_PATH));
        PropertyEvaluator baseEval2 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        return PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(PhpConfigurationProvider.CONFIG_PROPS_PATH),
                new ConfigPropertyProvider(baseEval1, "nbproject/private/configs", helper), // NOI18N
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
                helper.getProjectLibrariesPropertyProvider(),
                PropertyUtils.userPropertiesProvider(baseEval2,
                    "user.properties.file", FileUtil.toFile(getProjectDirectory())), // NOI18N
                new ConfigPropertyProvider(baseEval1, "nbproject/configs", helper), // NOI18N
                helper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH));
    }

    @Override
    public FileObject getProjectDirectory() {
        return getHelper().getProjectDirectory();
    }

    public SourceRoots getSourceRoots() {
        return sourceRoots;
    }

    public SourceRoots getTestRoots() {
        return testRoots;
    }

    public SourceRoots getSeleniumRoots() {
        return seleniumRoots;
    }

    FileObject getSourcesDirectory() {
        if (sourcesDirectory == null) {
            ProjectManager.mutex().readAccess(new Mutex.Action<Void>() {
                @Override
                public Void run() {
                    synchronized (PhpProject.this) {
                        if (sourcesDirectory == null) {
                            sourcesDirectory = resolveSourcesDirectory();
                            sourcesDirectory.addFileChangeListener(FileUtil.weakFileChangeListener(sourceDirectoryFileChangeListener, sourcesDirectory));
                        }
                    }
                    return null;
                }
            });
        }
        assert sourcesDirectory != null : "Sources directory cannot be null";
        return sourcesDirectory;
    }

    private FileObject resolveSourcesDirectory() {
        String srcDirProperty = eval.getProperty(PhpProjectProperties.SRC_DIR);
        // #168390, #165494
        if (srcDirProperty == null) {
            FileObject projectProps = helper.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            boolean projectPropsFound = projectProps != null;

            StringBuilder buffer = new StringBuilder(2000);
            buffer.append("Property 'src.dir' was not found in 'nbproject/project.properties' (NB metadata corrupted?)\n"); // NOI18N
            buffer.append("diagnostics:\n"); // NOI18N
            buffer.append("project.properties exists: "); // NOI18N
            buffer.append(projectPropsFound);
            if (projectPropsFound) {
                boolean canRead = projectProps.canRead();
                buffer.append("\nproject.properties valid: "); // NOI18N
                buffer.append(projectProps.isValid());
                buffer.append("\nproject.properties can read: "); // NOI18N
                buffer.append(canRead);
                if (canRead) {
                    buffer.append("\nproject.properties content: ["); // NOI18N
                    try {
                        buffer.append(projectProps.asText());
                    } catch (IOException exc) {
                        buffer.append(exc.getMessage());
                    }
                    buffer.append("]"); // NOI18N
                }
            } else {
                // project properties not found
                FileObject projectDirectory = getProjectDirectory();
                buffer.append("\nproject directory: "); // NOI18N
                buffer.append(projectDirectory);
                buffer.append("\nproject directory children: "); // NOI18N
                buffer.append(Arrays.asList(projectDirectory.getChildren()));

                FileObject nbproject = projectDirectory.getFileObject("nbproject"); // NOI18N
                boolean nbprojectFound = nbproject != null;
                buffer.append("\nnbproject exists: "); // NOI18N
                buffer.append(nbprojectFound);
                if (nbprojectFound) {
                    buffer.append("\nnbproject children: "); // NOI18N
                    buffer.append(Arrays.asList(nbproject.getChildren()));
                }
            }
            buffer.append("\nproperties (helper): "); // NOI18N
            buffer.append(helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH));
            buffer.append("\nproperties (evaluator): "); // NOI18N
            buffer.append(eval.getProperties());
            throw new IllegalStateException(buffer.toString());
        }
        FileObject srcDir = helper.resolveFileObject(srcDirProperty);
        if (srcDir != null) {
            return srcDir;
        }
        return restoreDirectory(PhpProjectProperties.SRC_DIR, "MSG_SourcesFolderRestored", "MSG_SourcesFolderTemporaryToProjectDirectory");
    }

    /**
     * @return tests directory or <code>null</code>
     */
    FileObject getTestsDirectory() {
        if (testsDirectory == null) {
            ProjectManager.mutex().readAccess(new Mutex.Action<Void>() {
                @Override
                public Void run() {
                    synchronized (PhpProject.this) {
                        if (testsDirectory == null) {
                            testsDirectory = resolveTestsDirectory();
                        }
                    }
                    return null;
                }
            });
        }
        return testsDirectory;
    }

    void setTestsDirectory(FileObject testsDirectory) {
        assert testsDirectory != null && testsDirectory.isValid();
        this.testsDirectory = testsDirectory;
    }

    private FileObject resolveTestsDirectory() {
        // similar to source directory
        String testsProperty = eval.getProperty(PhpProjectProperties.TEST_SRC_DIR);
        if (testsProperty == null) {
            // test directory not set yet
            return null;
        }
        FileObject testDir = helper.resolveFileObject(testsProperty);
        if (testDir != null) {
            return testDir;
        }
        return restoreDirectory(PhpProjectProperties.TEST_SRC_DIR, "MSG_TestsFolderRestored", "MSG_TestsFolderTemporaryToProjectDirectory");
    }

    /**
     * @return selenium tests directory or <code>null</code>
     */
    FileObject getSeleniumDirectory() {
        if (seleniumDirectory == null) {
            ProjectManager.mutex().readAccess(new Mutex.Action<Void>() {
                @Override
                public Void run() {
                    synchronized (PhpProject.this) {
                        if (seleniumDirectory == null) {
                            seleniumDirectory = resolveSeleniumDirectory();
                        }
                    }
                    return null;
                }
            });
        }
        return seleniumDirectory;
    }

    void setSeleniumDirectory(FileObject seleniumDirectory) {
        assert this.seleniumDirectory == null : "Project selenium directory already set to " + this.seleniumDirectory;
        assert seleniumDirectory != null && seleniumDirectory.isValid();
        this.seleniumDirectory = seleniumDirectory;
    }

    private FileObject resolveSeleniumDirectory() {
        // similar to source directory
        String testsProperty = eval.getProperty(PhpProjectProperties.SELENIUM_SRC_DIR);
        if (testsProperty == null) {
            // test directory not set yet
            return null;
        }
        FileObject testDir = helper.resolveFileObject(testsProperty);
        if (testDir != null) {
            return testDir;
        }
        return restoreDirectory(PhpProjectProperties.SELENIUM_SRC_DIR, "MSG_SeleniumFolderRestored", "MSG_SeleniumFolderTemporaryToProjectDirectory");
    }

    /**
     * @return web root directory or sources directory if not set
     */
    FileObject getWebRootDirectory() {
        if (webRootDirectory == null) {
            ProjectManager.mutex().readAccess(new Mutex.Action<Void>() {
                @Override
                public Void run() {
                    synchronized (PhpProject.this) {
                        if (webRootDirectory == null) {
                            webRootDirectory = resolveWebRootDirectory();
                        }
                    }
                    return null;
                }
            });
        }
        return webRootDirectory;
    }

    private FileObject resolveWebRootDirectory() {
        String webRootProperty = eval.getProperty(PhpProjectProperties.WEB_ROOT);
        if (webRootProperty == null) {
            // web root directory not set, return sources
            return getSourcesDirectory();
        }
        FileObject webRootDir = getSourcesDirectory().getFileObject(webRootProperty);
        if (webRootDir != null) {
            return webRootDir;
        }
        // web root directory not found, return sources
        return getSourcesDirectory();
    }

    private FileObject restoreDirectory(String propertyName, String infoMessageKey, String errorMessageKey) {
        // #144371 - source folder probably deleted => so:
        //  1. try to restore it - if it fails, then
        //  2. just return the project directory & warn user about impossibility of creating src dir
        String projectName = getName();
        File dir = FileUtil.normalizeFile(new File(helper.resolvePath(eval.getProperty(propertyName))));
        NotifyDescriptor notifyDescriptor = new NotifyDescriptor(
                NbBundle.getMessage(PhpProject.class, "MSG_CanFolderRestore", dir.getAbsolutePath()),   //NOI18N
                NbBundle.getMessage(PhpProject.class, "LBL_TitleCanFolderRestore", projectName),        //NOI18N
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                null, NotifyDescriptor.NO_OPTION);
        if (DialogDisplayer.getDefault().notify(notifyDescriptor) == NotifyDescriptor.YES_OPTION) {
            if (dir.mkdirs()) {
                // original sources restored
                informUser(projectName, NbBundle.getMessage(PhpProject.class, infoMessageKey, dir.getAbsolutePath()), NotifyDescriptor.INFORMATION_MESSAGE);
                return FileUtil.toFileObject(dir);
            }
            // temporary set sources to project directory, do not store it anywhere
            informUser(projectName, NbBundle.getMessage(PhpProject.class, errorMessageKey, dir.getAbsolutePath()), NotifyDescriptor.ERROR_MESSAGE);
        }
        return helper.getProjectDirectory();
    }

    private void informUser(String title, String message, int type) {
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor(
                message,
                title,
                NotifyDescriptor.DEFAULT_OPTION,
                type,
                new Object[] {NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION));
    }

    public PhpModule getPhpModule() {
        PhpModule phpModule = getLookup().lookup(PhpModule.class);
        assert phpModule != null;
        return phpModule;
    }

    boolean isVisible(File file) {
        if (getIgnoredFiles().contains(file)) {
            return false;
        }
        return VisibilityQuery.getDefault().isVisible(file);
    }

    boolean isVisible(FileObject fileObject) {
        File file = FileUtil.toFile(fileObject);
        if (file == null) {
            if (getIgnoredFileObjects().contains(fileObject)) {
                return false;
            }
            return VisibilityQuery.getDefault().isVisible(fileObject);
        }
        return isVisible(file);
    }

    public Set<File> getIgnoredFiles() {
        Set<File> ignored = new HashSet<File>();
        putIgnoredProjectFiles(ignored);
        putIgnoredFrameworkFiles(ignored);
        return ignored;
    }

    // #172139 caused NPE in GlobalVisibilityQueryImpl
    public Set<FileObject> getIgnoredFileObjects() {
        Set<FileObject> ignoredFileObjects = new HashSet<FileObject>();
        for (File file : getIgnoredFiles()) {
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                ignoredFileObjects.add(fo);
            }
        }
        return ignoredFileObjects;
    }

    private void putIgnoredProjectFiles(Set<File> ignored) {
        if (ignoredFolders == null) {
            ProjectManager.mutex().readAccess(new Mutex.Action<Void>() {
                @Override
                public Void run() {
                    synchronized (ignoredFoldersLock) {
                        if (ignoredFolders == null) {
                            ignoredFolders = resolveIgnoredFolders();
                        }
                    }
                    return null;
                }
            });
        }
        assert ignoredFolders != null : "Ignored folders cannot be null";

        File projectDir = FileUtil.toFile(getProjectDirectory());
        for (BasePathSupport.Item item : ignoredFolders) {
            if (item.isBroken()) {
                continue;
            }
            File file = new File(item.getFilePath());
            if (!file.isAbsolute()) {
                file = helper.resolveFile(item.getFilePath());
            }
            ignored.add(file);
        }
    }

    private void putIgnoredFrameworkFiles(Set<File> ignored) {
        PhpModule phpModule = getPhpModule();
        for (PhpFrameworkProvider provider : getFrameworks()) {
            PhpModuleIgnoredFilesExtender ignoredFilesExtender = provider.getIgnoredFilesExtender(phpModule);
            if (ignoredFilesExtender == null) {
                continue;
            }
            for (File file : ignoredFilesExtender.getIgnoredFiles()) {
                assert file != null : "Ignored file = null found in " + provider.getIdentifier();
                assert file.isAbsolute() : "Not absolute file found in " + provider.getIdentifier();

                ignored.add(file);
            }
        }
    }

    private Set<BasePathSupport.Item> resolveIgnoredFolders() {
        IgnorePathSupport ignorePathSupport = new IgnorePathSupport(eval, refHelper, helper);
        Set<BasePathSupport.Item> ignored = new HashSet<BasePathSupport.Item>();
        EditableProperties properties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        Iterator<BasePathSupport.Item> itemsIterator = ignorePathSupport.itemsIterator(properties.getProperty(PhpProjectProperties.IGNORE_PATH));
        while (itemsIterator.hasNext()) {
            ignored.add(itemsIterator.next());
        }
        return ignored;
    }

    public List<PhpFrameworkProvider> getFrameworks() {
        if (frameworksDirty) {
            synchronized (frameworks) {
                if (frameworksDirty) {
                    frameworksDirty = false;
                    List<PhpFrameworkProvider> newFrameworks = new LinkedList<PhpFrameworkProvider>();
                    PhpModule phpModule = getPhpModule();
                    for (PhpFrameworkProvider frameworkProvider : PhpFrameworks.getFrameworks()) {
                        if (frameworkProvider.isInPhpModule(phpModule)) {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.fine(String.format("Adding framework %s for project %s", frameworkProvider.getIdentifier(), getSourcesDirectory()));
                            }
                            newFrameworks.add(frameworkProvider);
                        }
                    }
                    frameworks.clear();
                    frameworks.addAll(newFrameworks);
                }
            }
        }
        return new ArrayList<PhpFrameworkProvider>(frameworks);
    }

    public boolean hasConfigFiles() {
        final PhpModule phpModule = getPhpModule();
        for (PhpFrameworkProvider frameworkProvider : getFrameworks()) {
            if (frameworkProvider.getConfigurationFiles(phpModule).length > 0) {
                return true;
            }
        }
        return false;
    }

    public void resetFrameworks() {
        List<PhpFrameworkProvider> oldFrameworkProviders = getFrameworks();
        frameworksDirty = true;
        List<PhpFrameworkProvider> newFrameworkProviders = getFrameworks();
        if (!oldFrameworkProviders.equals(newFrameworkProviders)) {
            propertyChangeSupport.firePropertyChange(PROP_FRAMEWORKS, null, null);
        }
    }

    public String getName() {
        if (name == null) {
            ProjectManager.mutex().readAccess(new Mutex.Action<Void>() {
                @Override
                public Void run() {
                    synchronized (PhpProject.this) {
                        if (name == null) {
                            Element data = getHelper().getPrimaryConfigurationData(true);
                            NodeList nl = data.getElementsByTagNameNS(PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                            if (nl.getLength() == 1) {
                                nl = nl.item(0).getChildNodes();
                                if (nl.getLength() == 1
                                        && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                                    name = ((Text) nl.item(0)).getNodeValue();
                                }
                            }
                        }
                        if (name == null) {
                            name = "???"; // NOI18N
                        }
                    }
                    return null;
                }
            });
        }
        assert name != null;
        return name;
    }

    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            @Override
            public void run() {
                Element data = getHelper().getPrimaryConfigurationData(true);
                NodeList nl = data.getElementsByTagNameNS(PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(
                            PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                getHelper().putPrimaryConfigurationData(data, true);
            }
        });
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(200);
        buffer.append(getClass().getName());
        buffer.append(" [ project directory: ");
        buffer.append(getProjectDirectory());
        buffer.append(", source directory: ");
        buffer.append(sourcesDirectory);
        buffer.append(" ]");
        return buffer.toString();
    }

    public AntProjectHelper getHelper() {
        return helper;
    }

    public CopySupport getCopySupport() {
        return getLookup().lookup(CopySupport.class);
    }

    private Lookup createLookup(AuxiliaryConfiguration configuration) {
        PhpProjectEncodingQueryImpl phpProjectEncodingQueryImpl = new PhpProjectEncodingQueryImpl(getEvaluator());
        return Lookups.fixed(new Object[] {
                this,
                CopySupport.getInstance(this),
                new SeleniumProvider(),
                new PhpCoverageProvider(this),
                new Info(),
                configuration,
                new PhpOpenedHook(),
                new PhpProjectXmlSavedHook(),
                new PhpActionProvider(this),
                new PhpConfigurationProvider(this),
                new PhpModuleImpl(this),
                PhpLanguagePropertiesAccessor.getDefault().createForProject(this),
                new PhpEditorExtender(this),
                helper.createCacheDirectoryProvider(),
                helper.createAuxiliaryProperties(),
                new ClassPathProviderImpl(this, getSourceRoots(), getTestRoots(), getSeleniumRoots()),
                new PhpLogicalViewProvider(this),
                new CustomizerProviderImpl(this),
                PhpSharabilityQuery.create(helper, getEvaluator(), getSourceRoots(), getTestRoots(), getSeleniumRoots()),
                new PhpProjectOperations(this) ,
                phpProjectEncodingQueryImpl,
                new TemplateAttributesProviderImpl(getHelper(), phpProjectEncodingQueryImpl),
                new PhpTemplates(),
                new PhpSources(this, getHelper(), getEvaluator(), getSourceRoots(), getTestRoots(), getSeleniumRoots()),
                getHelper(),
                getEvaluator(),
                PhpSearchInfo.create(this),
                new PhpSubTreeSearchOptions(),
                InternalWebServer.createForProject(this),
                new ProjectWebRootProviderImpl()
                // ?? getRefHelper()
        });
    }

    public ReferenceHelper getRefHelper() {
        return refHelper;
    }

    public void fireIgnoredFilesChange() {
        ignoredFolders = null;
        ignoredFoldersChangeSupport.fireChange();
    }

    private final class Info implements ProjectInformation {
        private static final String TOOLTIP = "<img src=\"%s\">&nbsp;%s"; // NOI18N

        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        public Info() {
            PhpProject.this.propertyChangeSupport.addPropertyChangeListener(PhpProject.PROP_FRAMEWORKS, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(ProjectInformation.PROP_ICON);
                }
            });
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener  listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        @Override
        public String getDisplayName() {
            return PhpProject.this.getName();
        }

        @Override
        public Icon getIcon() {
            return ImageUtilities.image2Icon(annotateImage(ImageUtilities.loadImage("org/netbeans/modules/php/project/ui/resources/phpProject.png"))); // NOI18N
        }

        @Override
        public String getName() {
            return PropertyUtils.getUsablePropertyName(getDisplayName());
        }

        @Override
        public Project getProject() {
            return PhpProject.this;
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }

        void firePropertyChange(String prop) {
            propertyChangeSupport.firePropertyChange(prop , null, null);
        }

        private Image annotateImage(Image image) {
            Image badged = image;
            boolean first = true;
            for (PhpFrameworkProvider frameworkProvider : getFrameworks()) {
                BadgeIcon badgeIcon = frameworkProvider.getBadgeIcon();
                if (badgeIcon != null) {
                    badged = ImageUtilities.addToolTipToImage(badged, String.format(TOOLTIP, badgeIcon.getUrl(), frameworkProvider.getName()));
                    if (first) {
                        badged = ImageUtilities.mergeImages(badged, badgeIcon.getImage(), 15, 0);
                        first = false;
                    }
                } else {
                    badged = ImageUtilities.addToolTipToImage(badged, String.format(TOOLTIP, Utils.PLACEHOLDER_BADGE, frameworkProvider.getName()));
                }
            }
            return badged;
        }
    }

    private final class PhpOpenedHook extends ProjectOpenedHook {
        @Override
        protected void projectOpened() {
            // #165494 - moved from projectClosed() to projectOpened()
            // clear references to ensure that all the dirs are read again
            sourcesDirectory = null;
            testsDirectory = null;
            seleniumDirectory = null;
            webRootDirectory = null;
            ignoredFolders = null;
            resetFrameworks();

            // #139159 - we need to hold sources FO to prevent gc
            getSourcesDirectory();
            getWebRootDirectory();
            LOGGER.log(Level.FINE, "Adding frameworks listener for {0}", sourcesDirectory);
            PhpFrameworks.addFrameworksListener(frameworksListener);
            // do it in a background thread
            getIgnoredFiles();
            List<PhpFrameworkProvider> frameworkProviders = getFrameworks();
            getName();

            ClassPathProviderImpl cpProvider = lookup.lookup(ClassPathProviderImpl.class);
            ClassPath[] bootClassPaths = cpProvider.getProjectClassPaths(PhpSourcePath.BOOT_CP);
            GlobalPathRegistry.getDefault().register(PhpSourcePath.BOOT_CP, bootClassPaths);
            GlobalPathRegistry.getDefault().register(PhpSourcePath.SOURCE_CP, cpProvider.getProjectClassPaths(PhpSourcePath.SOURCE_CP));
            for (ClassPath classPath : bootClassPaths) {
                IncludePathClassPathProvider.addProjectIncludePath(classPath);
            }

            // ensure that code coverage is initialized in case it's enabled...
            PhpCoverageProvider coverageProvider = getLookup().lookup(PhpCoverageProvider.class);
            if (coverageProvider.isEnabled()) {
                PhpCoverageProvider.notifyProjectOpened(PhpProject.this);
            }

            // frameworks
            PhpModule phpModule = getPhpModule();
            assert phpModule != null;
            for (PhpFrameworkProvider frameworkProvider : frameworkProviders) {
                frameworkProvider.phpModuleOpened(phpModule);
            }

            // #187060 - exception in projectOpened => project IS NOT opened (so move it at the end of the hook)
            getCopySupport().projectOpened();

            // log usage
            PhpProjectUtils.logUsage(PhpProject.class, "USG_PROJECT_OPEN_PHP", Arrays.asList(PhpProjectUtils.getFrameworksForUsage(frameworkProviders))); // NOI18N
            // #192386
            LOGGER.finest("PROJECT_OPENED_FINISHED");
        }

        @Override
        protected void projectClosed() {
            try {
                assert sourcesDirectory != null;
                sourcesDirectory.removeFileChangeListener(sourceDirectoryFileChangeListener);
                LOGGER.log(Level.FINE, "Removing frameworks listener for {0}", sourcesDirectory);
                PhpFrameworks.removeFrameworksListener(frameworksListener);

                ClassPathProviderImpl cpProvider = lookup.lookup(ClassPathProviderImpl.class);
                ClassPath[] bootClassPaths = cpProvider.getProjectClassPaths(PhpSourcePath.BOOT_CP);
                GlobalPathRegistry.getDefault().unregister(PhpSourcePath.BOOT_CP, bootClassPaths);
                GlobalPathRegistry.getDefault().unregister(PhpSourcePath.SOURCE_CP, cpProvider.getProjectClassPaths(PhpSourcePath.SOURCE_CP));
                for (ClassPath classPath : bootClassPaths) {
                    IncludePathClassPathProvider.removeProjectIncludePath(classPath);
                }

                // frameworks
                PhpModule phpModule = getPhpModule();
                assert phpModule != null;
                for (PhpFrameworkProvider frameworkProvider : getFrameworks()) {
                    frameworkProvider.phpModuleClosed(phpModule);
                }

                // internal web server
                lookup.lookup(InternalWebServer.class).stop();
            } finally {
                // #187060 - exception in projectClosed => project IS closed (so do it in finally block)
                getCopySupport().projectClosed();
                // #192386
                LOGGER.finest("PROJECT_CLOSED_FINISHED");
            }
        }
    }

    private static final class ConfigPropertyProvider extends FilterPropertyProvider implements PropertyChangeListener {
        private final PropertyEvaluator baseEval;
        private final String prefix;
        private final AntProjectHelper helper;
        public ConfigPropertyProvider(PropertyEvaluator baseEval, String prefix, AntProjectHelper helper) {
            super(computeDelegate(baseEval, prefix, helper));
            this.baseEval = baseEval;
            this.prefix = prefix;
            this.helper = helper;
            baseEval.addPropertyChangeListener(this);
        }
        @Override
        public void propertyChange(PropertyChangeEvent ev) {
            if (PhpConfigurationProvider.PROP_CONFIG.equals(ev.getPropertyName())) {
                setDelegate(computeDelegate(baseEval, prefix, helper));
            }
        }
        private static PropertyProvider computeDelegate(PropertyEvaluator baseEval, String prefix, AntProjectHelper helper) {
            String config = baseEval.getProperty(PhpConfigurationProvider.PROP_CONFIG);
            if (config != null) {
                return helper.getPropertyProvider(prefix + "/" + config + ".properties"); // NOI18N
            }
            return PropertyUtils.fixedPropertyProvider(Collections.<String, String>emptyMap());
        }
    }

    public final class PhpProjectXmlSavedHook extends ProjectXmlSavedHook {

        @Override
        protected void projectXmlSaved() throws IOException {
            Info info = getLookup().lookup(Info.class);
            assert info != null;
            info.firePropertyChange(ProjectInformation.PROP_NAME);
            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
        }
    }

    private final class SeleniumProvider implements PhpSeleniumProvider {
        @Override
        public FileObject getTestDirectory(boolean showCustomizer) {
            return ProjectPropertiesSupport.getSeleniumDirectory(PhpProject.this, showCustomizer);
        }

        @Override
        public void runAllTests() {
            ConfigAction.get(ConfigAction.Type.SELENIUM, PhpProject.this).runProject();
        }
    }

    private final class ProjectPropertiesListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (PhpProjectProperties.IGNORE_PATH.equals(propertyName)) {
                fireIgnoredFilesChange();
            } else if (PhpProjectProperties.TEST_SRC_DIR.equals(propertyName)) {
                testsDirectory = null;
            } else if (PhpProjectProperties.WEB_ROOT.equals(propertyName)) {
                FileObject oldWebRoot = webRootDirectory;
                webRootDirectory = null;
                // useful since it fires changes with fileobjects -> client can better use it than "htdocs/web/" values
                propertyChangeSupport.firePropertyChange(PROP_WEB_ROOT, oldWebRoot, getWebRootDirectory());
            }
        }
    }

    // if source folder changes, reset frameworks (new framework can be found in project)
    private final class SourceDirectoryFileChangeListener implements FileChangeListener {

        @Override
        public void fileFolderCreated(FileEvent fe) {
            processFileChange();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            processFileChange();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            // probably not interesting for us
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            processFileChange();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            processFileChange();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // probably not interesting for us
        }

        void processFileChange() {
            LOGGER.fine("file change, frameworks back to null");
            resetFrameworks();
        }
    }

    private final class FrameworksListener implements LookupListener {
        @Override
        public void resultChanged(LookupEvent ev) {
            LOGGER.fine("frameworks change, frameworks back to null");
            resetFrameworks();
        }
    }

    private final class PhpAntProjectListener implements AntProjectListener {

        @Override
        public void configurationXmlChanged(AntProjectEvent ev) {
            name = null;
        }

        @Override
        public void propertiesChanged(AntProjectEvent ev) {
        }
    }

    private final class ProjectWebRootProviderImpl implements ProjectWebRootProvider {

        @Override
        public FileObject getWebRoot(FileObject file) {
            return ProjectPropertiesSupport.getWebRootDirectory(PhpProject.this);
        }
    }

    private static final class PhpSearchInfo extends SearchInfoDefinition implements PropertyChangeListener {

        private static final Logger LOGGER = Logger.getLogger(PhpSearchInfo.class.getName());

        private final PhpProject project;
        // @GuardedBy(this)
        private SearchInfo delegate = null;

        private PhpSearchInfo(PhpProject project) {
            this.project = project;
        }

        public static SearchInfoDefinition create(PhpProject project) {
            PhpSearchInfo phpSearchInfo = new PhpSearchInfo(project);
            project.getSourceRoots().addPropertyChangeListener(phpSearchInfo);
            project.getTestRoots().addPropertyChangeListener(phpSearchInfo);
            project.getSeleniumRoots().addPropertyChangeListener(phpSearchInfo);
            return phpSearchInfo;
        }

        private SearchInfo createDelegate() {
            SearchInfo searchInfo = SearchInfoUtils.createSearchInfoForRoots(
                    getRoots(), false, project.getFileObjectFilter());
            return searchInfo;
        }

        @Override
        public boolean canSearch() {
            return true;
        }

        @Override
        public Iterator<FileObject> filesToSearch(
                SearchScopeOptions searchScopeOptions, 
                SearchListener listener, 
                TerminationFlag terminationFlag) {
            return getDelegate().getFilesToSearch(searchScopeOptions, 
                    listener, terminationFlag);
        }

        @Override
        public List<SearchRoot> getSearchRoots() {
            return getDelegate().getSearchRoots();
        }

        private FileObject[] getRoots() {
            List<FileObject> roots = new LinkedList<FileObject>();
            addRoots(roots, project.getSourceRoots());
            addRoots(roots, project.getTestRoots());
            addRoots(roots, project.getSeleniumRoots());
            addIncludePath(roots, PhpSourcePath.getIncludePath(project.getSourcesDirectory()));
            return roots.toArray(new FileObject[roots.size()]);
        }

        // #197968
        private void addRoots(List<FileObject> roots, SourceRoots sourceRoots) {
            for (FileObject root : sourceRoots.getRoots()) {
                if (!root.isFolder()) {
                    LOGGER.log(Level.WARNING, "Not folder {0} for source roots {1}", new Object[] {root, Arrays.toString(sourceRoots.getRootNames())});
                } else {
                    roots.add(root);
                }
            }
        }

        private void addIncludePath(List<FileObject> roots, List<FileObject> includePath) {
            for (FileObject folder : includePath) {
                if (!folder.isFolder()) {
                    LOGGER.log(Level.WARNING, "Not folder {0} for Include path {1}", new Object[] {folder, includePath});
                } else {
                    roots.add(folder);
                }
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (SourceRoots.PROP_ROOTS.equals(evt.getPropertyName())) {
                synchronized (this) {
                    delegate = createDelegate();
                }
            }
        }

        /**
         * @return the delegate
         */
        private synchronized SearchInfo getDelegate() {
            if (delegate == null) {
                delegate = createDelegate();
            }
            return delegate;
        }
    }

    private final class PhpSearchFilterDef extends SearchFilterDefinition {

        @Override
        public boolean searchFile(FileObject file) {
            if (!file.isData()) {
                throw new IllegalArgumentException("File expected");
            }
            return PhpVisibilityQuery.forProject(PhpProject.this).isVisible(file);
        }

        @Override
        public FolderResult traverseFolder(FileObject folder) {
            if (!folder.isFolder()) {
                throw new IllegalArgumentException("Folder expected");
            }
            if (PhpVisibilityQuery.forProject(PhpProject.this).isVisible(folder)) {
                return FolderResult.TRAVERSE;
            }
            return FolderResult.DO_NOT_TRAVERSE;
        }

    }

    private final class PhpSubTreeSearchOptions extends SubTreeSearchOptions {

        @Override
        public List<SearchFilterDefinition> getFilters() {
            return Collections.singletonList(getFileObjectFilter());
        }
    }
}
