/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.apisupport.installer.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import org.openide.ErrorManager;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.windows.WindowManager;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.apisupport.installer.ui.SuiteInstallerProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.RequestProcessor;

public final class BuildInstallersAction extends AbstractAction implements ContextAwareAction {

    private static BuildInstallersAction inst = null;

    private BuildInstallersAction() {
    }

    public static synchronized BuildInstallersAction getDefault() {
        if (inst == null) {
            inst = new BuildInstallersAction();
        }
        return inst;
    }

    public static void actionPerformed(Node[] e) {
        ContextBuildInstaller.actionPerformed(e);
    }

    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new ContextBuildInstaller();
    }

    static class ContextBuildInstaller extends AbstractAction implements Presenter.Popup {

        public ContextBuildInstaller() {
            putValue(NAME, NbBundle.getMessage(BuildInstallersAction.class, "CTL_BuildInstallers"));
        }

        public void actionPerformed(ActionEvent e) {
            Node[] n = WindowManager.getDefault().getRegistry().getActivatedNodes();
            if (n.length > 0) {
                ContextBuildInstaller.actionPerformed(n);
            } else {
                ContextBuildInstaller.actionPerformed((Node[]) null);
            }
        }

        public static void actionPerformed(Node[] e) {
            if (e != null) {
                for (Node node : e) {
                    final Project prj = node.getLookup().lookup(Project.class);
                    if (prj != null) {
                        File suiteLocation = FileUtil.toFile(prj.getProjectDirectory());
                        FileObject propertiesFile = prj.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        Properties ps = new Properties();
                        String appName = "";
                        String appIcon = null;
                        String licenseType = null;
                        boolean usePack200 = false;
                        try {
                            InputStream is = propertiesFile.getInputStream();
                            ps.load(is);
                            appName = ps.getProperty("app.name");
                            licenseType = ps.getProperty("installer.license.type");
                            usePack200 = Boolean.parseBoolean(ps.getProperty(SuiteInstallerProjectProperties.USE_PACK200_COMPRESSION));
                            appIcon = ps.getProperty("app.icon");
                            if (appName == null) {
                                //suite, not standalone app
                                RequestProcessor.getDefault().post(new Runnable() {

                                    public void run() {
                                        DialogDescriptor d = new DialogDescriptor(
                                                NbBundle.getMessage(BuildInstallersAction.class, "BuildInstallersAction.NotApp.Warning.Message"),
                                                NbBundle.getMessage(BuildInstallersAction.class, "BuildInstallersAction.NotApp.Warning.Title"));
                                        d.setModal(true);
                                        JButton accept = new JButton(NbBundle.getMessage(BuildInstallersAction.class, "BuildInstallersAction.NotApp.Warning.OK"));
                                        accept.setDefaultCapable(true);
                                        d.setOptions(new Object[]{
                                                    accept});
                                        d.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
                                        if (DialogDisplayer.getDefault().notify(d).equals(accept)) {
                                            //SuiteCustomizer cpi = prj.getLookup().lookup(org.netbeans.modules.apisupport.project.ui.customizer.SuiteCustomizer.class);
                                            //cpi.showCustomizer(SuiteCustomizer.APPLICATION, SuiteCustomizer.APPLICATION_CREATE_STANDALONE_APPLICATION);
                                        }
                                    }
                                });

                                return;
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(BuildInstallersAction.class.getName()).log(Level.WARNING, "Can`t store properties", ex);
                        }

                        File licenseFile = null;
                        if (licenseType != null && !licenseType.equals("no")) {
                            Logger.getLogger(BuildInstallersAction.class.getName()).log(Level.WARNING, "License type defined to " + licenseType);
                            String licenseResource = null;
                            try {
                                licenseResource = NbBundle.getMessage(SuiteInstallerProjectProperties.class,
                                        "SuiteInstallerProjectProperties.license.file." + licenseType);
                            } catch (MissingResourceException ex) {
                                Logger.getLogger(BuildInstallersAction.class.getName()).log(Level.WARNING, "License resource not found");
                            }

                            if (licenseResource != null) {
                                InputStream is = null;
                                try {
                                    URL url = new URL(licenseResource);
                                    is = url.openStream();
                                    if (is != null) {
                                        licenseFile = File.createTempFile("license", ".txt");
                                        licenseFile.getParentFile().mkdirs();
                                        licenseFile.deleteOnExit();

                                        OutputStream os = new FileOutputStream(licenseFile);
                                        byte[] bytes = new byte[4096];
                                        int read = 0;
                                        while ((read = is.read(bytes)) > 0) {
                                            os.write(bytes, 0, read);
                                        }
                                        os.flush();
                                        os.close();
                                    } else {
                                        Logger.getLogger(BuildInstallersAction.class.getName()).log(
                                                Level.WARNING,
                                                "License resource " + licenseResource
                                                + " not found");
                                    }
                                } catch (MalformedURLException ex) {
                                    Logger.getLogger(BuildInstallersAction.class.getName()).log(Level.WARNING,
                                            "Can`t parse URL", ex);
                                } catch (IOException ex) {
                                    Logger.getLogger(BuildInstallersAction.class.getName()).log(Level.WARNING,
                                            "Input/Output error", ex);
                                } finally {
                                    if (is != null) {
                                        try {
                                            is.close();
                                        } catch (IOException ex) {
                                        }
                                    }
                                }
                            }
                        }

                        //Logger.getLogger(BuildInstallersAction.class.getName()).warning("actionPerformed for " + suiteLocation);
                        Properties props = new Properties();
                        props.put("suite.location", suiteLocation.getAbsolutePath().replace("\\", "/"));
                        props.put("suite.nbi.product.uid",
                                appName.replaceAll("[0-9]+", "").replace("_", "-").toLowerCase(Locale.ENGLISH));


                        props.put("nbi.stub.location", InstalledFileLocator.getDefault().locate(
                                "nbi/stub",
                                "org.netbeans.modules.apisupport.installer", false).getAbsolutePath().replace("\\", "/"));
                        props.put(
                                "nbi.stub.common.location", InstalledFileLocator.getDefault().locate(
                                "nbi/.common",
                                "org.netbeans.modules.apisupport.installer", false).getAbsolutePath().replace("\\", "/"));

                        props.put(
                                "nbi.ant.tasks.jar", InstalledFileLocator.getDefault().locate(
                                "modules/ext/nbi-ant-tasks.jar",
                                "org.netbeans.modules.apisupport.installer", false).getAbsolutePath().replace("\\", "/"));
                        props.put(
                                "nbi.registries.management.jar", InstalledFileLocator.getDefault().locate(
                                "modules/ext/nbi-registries-management.jar",
                                "org.netbeans.modules.apisupport.installer", false).getAbsolutePath().replace("\\", "/"));
                        props.put(
                                "nbi.engine.jar", InstalledFileLocator.getDefault().locate(
                                "modules/ext/nbi-engine.jar",
                                "org.netbeans.modules.apisupport.installer", false).getAbsolutePath().replace("\\", "/"));
                        if (licenseFile != null) {
                            Logger.getLogger(BuildInstallersAction.class.getName()).log(Level.INFO,
                                    "License file is at " + licenseFile + ", exist = " + licenseFile.exists());
                            props.put(
                                    "nbi.license.file", licenseFile.getAbsolutePath());
                        }

                        List<String> platforms = new ArrayList<String>();

                        boolean installerConfDefined = false;
                        for (Object s : ps.keySet()) {
                            String key = (String) s;
                            String prefix = "installer.os.";
                            if (key.startsWith(prefix)) {
                                installerConfDefined = true;
                                if (ps.getProperty(key).equals("true")) {
                                    platforms.add(key.substring(prefix.length()));
                                }
                            }
                        }
                        if (!installerConfDefined) {
                            String osName = System.getProperty("os.name");
                            if (osName.contains("Windows")) {
                                platforms.add("windows");
                            } else if (osName.contains("Linux")) {
                                platforms.add("linux");
                            } else if (osName.contains("SunOS") || osName.contains("Solaris")) {
                                platforms.add("solaris");
                            } else if (osName.contains("Mac OS X") || osName.contains("Darwin")) {
                                platforms.add("macosx");                                
                            }
                        }
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < platforms.size(); i++) {
                            if (i != 0) {
                                sb.append(" ");
                            }
                            sb.append(platforms.get(i));
                        }
                        if (sb.length() == 0) {
                            //nothing to build
                            RequestProcessor.getDefault().post(new Runnable() {

                                public void run() {
                                    DialogDescriptor d = new DialogDescriptor(
                                            NbBundle.getMessage(BuildInstallersAction.class, "BuildInstallersAction.NotConfigured.Warning.Message"),
                                            NbBundle.getMessage(BuildInstallersAction.class, "BuildInstallersAction.NotConfigured.Warning.Title"));
                                    d.setModal(true);
                                    JButton accept = new JButton(NbBundle.getMessage(BuildInstallersAction.class, "BuildInstallersAction.NotConfigured.Warning.OK"));
                                    accept.setDefaultCapable(true);
                                    d.setOptions(new Object[]{
                                                accept});
                                    d.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
                                    if (DialogDisplayer.getDefault().notify(d).equals(accept)) {
                                        //SuiteCustomizer cpi = prj.getLookup().lookup(org.netbeans.modules.apisupport.project.ui.customizer.SuiteCustomizer.class);
                                        //cpi.showCustomizer(SuiteCustomizer.APPLICATION, SuiteCustomizer.APPLICATION_CREATE_STANDALONE_APPLICATION);
                                    }
                                }
                            });
                            return;
                        }

                        props.put("generate.installer.for.platforms",
                                sb.toString());



                        File javaHome = new File(System.getProperty("java.home"));
                        if (new File(javaHome,
                                "lib/rt.jar").exists() && javaHome.getName().equals("jre")) {
                            javaHome = javaHome.getParentFile();
                        }
                        props.put(
                                "generator-jdk-location-forward-slashes", javaHome.getAbsolutePath().replace("\\", "/"));
                        /*
                        props.put(
                        "generated-installers-location-forward-slashes",
                        new File(suiteLocation, "dist").getAbsolutePath().replace("\\", "/"));
                         */
                        props.put(
                                "pack200.enabled", "" + usePack200);

                        if(appIcon!=null) {
                            File appIconFile = new File(appIcon);
                            if(!appIconFile.equals(appIconFile.getAbsoluteFile())) {
                                //path is relative to suite directory
                                appIconFile = new File(suiteLocation, appIcon);
                            }
                            props.put(
                                "nbi.icon.file", appIconFile.getAbsolutePath());

                        }

                        /*
                        for (Object s : props.keySet()) {
                        Logger.getLogger(BuildInstallersAction.class.getName()).log(Level.INFO,
                        "[" + s + "] = " + props.get(s));
                        }
                         */
                        /*
                        File tmpProps = null;
                        try {
                        tmpProps = File.createTempFile("nbi-properties-", ".properties");
                        FileOutputStream fos = new FileOutputStream(tmpProps);
                        props.store(fos, null);
                        fos.close();
                        } catch (IOException ex) {
                        Logger.getLogger(BuildInstallersAction.class.getName()).log(Level.WARNING, "Can`t store properties", ex);
                        }*/
                        try {
                            final ExecutorTask executorTask = ActionUtils.runTarget(findGenXml(prj), new String[]{"build"}, props);
                            /*
                            executorTask.addTaskListener(new TaskListener() {

                            public void taskFinished(Task task) {
                            if (executorTask.result() == 0) {
                            try {
                            ActionUtils.runTarget(findInstXml(prj), new String[]{"build"}, new Properties());
                            } catch (FileStateInvalidException ex) {
                            ErrorManager.getDefault().getInstance("org.netbeans.modules.apisupport.project").notify(ex); // NOI18N
                            } catch (IOException ex) {
                            ErrorManager.getDefault().getInstance("org.netbeans.modules.apisupport.project").notify(ex); // NOI18N

                            }
                            }
                            }
                            });*/
                        } catch (FileStateInvalidException ex) {
                            ErrorManager.getDefault().getInstance("org.netbeans.modules.apisupport.project").notify(ex); // NOI18N
                        } catch (IOException ex) {
                            ErrorManager.getDefault().getInstance("org.netbeans.modules.apisupport.project").notify(ex); // NOI18N
                        }

                        /*
                        if (tmpProps != null && !tmpProps.delete() && tmpProps.exists()) {
                        tmpProps.deleteOnExit();
                        }*/
                    }
                }

            }



        }

        private static FileObject findBuildXml(Project project) {
            return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        }

        private static FileObject findGenXml(Project project) {
            return FileUtil.toFileObject(InstalledFileLocator.getDefault().locate(
                    "nbi/stub/template.xml",
                    "org.netbeans.modules.apisupport.installer", false));
        }

        private static FileObject findInstXml(Project project) throws FileStateInvalidException {
            return project.getProjectDirectory().getFileObject("build/installer/build.xml");
        }

        public JMenuItem getPopupPresenter() {
            Node[] n = WindowManager.getDefault().getRegistry().getActivatedNodes();
            if (n.length == 1) {
                Project prj = n[0].getLookup().lookup(Project.class);
                if (prj != null && prj.getClass().getSimpleName().equals("SuiteProject")) {
                    return new JMenuItem(this);
                }
            }

            JMenuItem dummy = new JMenuItem();
            dummy.setVisible(false);
            return dummy;

        }
    }
}


