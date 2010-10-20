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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.glassfish.javaee.ide;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.glassfish.eecommon.api.HttpMonitorHelper;
import org.netbeans.modules.glassfish.eecommon.api.Utils;
import org.netbeans.modules.glassfish.javaee.Hk2DeploymentManager;
import org.netbeans.modules.glassfish.javaee.ResourceRegistrationHelper;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule2;
import org.netbeans.modules.j2ee.deployment.plugins.api.DeploymentChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;

/**
 *
 * @author Ludovic Champenois
 * @author Peter Williams
 */
public class FastDeploy extends IncrementalDeployment implements IncrementalDeployment2 {
    
    private Hk2DeploymentManager dm;
    
    /** 
     * Creates a new instance of FastDeploy 
     * 
     * @param dm The deployment manager for the server instance this object
     *   deploys to.
     */
    public FastDeploy(Hk2DeploymentManager dm) {
        this.dm = dm;
    }
    
    /**
     * 
     * @param target 
     * @param app 
     * @param configuration 
     * @param file 
     * @return 
     */
    @Override
    public ProgressObject initialDeploy(Target target, final J2eeModule module, ModuleConfiguration configuration, final File dir) {
        return initialDeploy(target, module, dir, new File[0]);
    }

    @Override
    public ProgressObject initialDeploy(Target target, DeploymentContext context) {
        return initialDeploy(target, context.getModule(), context.getModuleFile(), context.getRequiredLibraries());
    }


    private ProgressObject initialDeploy(Target target, J2eeModule module, final File dir, final File[] requiredLibraries) {
        final String moduleName = org.netbeans.modules.glassfish.spi.Utils.sanitizeName(Utils.computeModuleID(module, dir, Integer.toString(hashCode())));
        String contextRoot = null;
        // XXX fix cast -- need error instance for ProgressObject to return errors
        Hk2TargetModuleID moduleId = Hk2TargetModuleID.get((Hk2Target) target, moduleName,
                contextRoot, dir.getAbsolutePath());
        final MonitorProgressObject deployProgress = new MonitorProgressObject(dm, moduleId);
        final MonitorProgressObject updateCRProgress = new MonitorProgressObject(dm, moduleId);
        deployProgress.addProgressListener(new UpdateContextRoot(updateCRProgress,moduleId, dm.getServerInstance(), J2eeModule.Type.WAR.equals(module.getType())));
        MonitorProgressObject restartProgress = new MonitorProgressObject(dm, moduleId);

        final GlassfishModule commonSupport = dm.getCommonServerSupport();
        final GlassfishModule2 commonSupport2 = (commonSupport instanceof GlassfishModule2 ?
            (GlassfishModule2)commonSupport : null);
        boolean restart = false;
        try {
            restart = HttpMonitorHelper.synchronizeMonitor(commonSupport.getInstanceProperties().get(GlassfishModule.DOMAINS_FOLDER_ATTR),
                    commonSupport.getInstanceProperties().get(GlassfishModule.DOMAIN_NAME_ATTR),
                    Boolean.parseBoolean(commonSupport.getInstanceProperties().get(GlassfishModule.HTTP_MONITOR_FLAG)),
                    "modules/org-netbeans-modules-schema2beans.jar");
        } catch (IOException ex) {
            Logger.getLogger("glassfish-javaee").log(Level.WARNING, "http monitor state", ex);
        } catch (SAXException ex) {
            Logger.getLogger("glassfish-javaee").log(Level.WARNING, "http monitor state", ex);
        }
        ResourceRegistrationHelper.deployResources(dir,dm);
        if (restart) {
            restartProgress.addProgressListener(new ProgressListener() {
                @Override
                public void handleProgressEvent(ProgressEvent event) {
                    if (event.getDeploymentStatus().isCompleted()) {
                        if (commonSupport2 != null && requiredLibraries.length > 0) {
                            commonSupport2.deploy(deployProgress, dir, moduleName, null, Collections.<String, String>emptyMap(), requiredLibraries);
                        } else {
                            commonSupport.deploy(deployProgress, dir, moduleName);
                        }
                    } else {
                        deployProgress.fireHandleProgressEvent(event.getDeploymentStatus());
                    }
                }
            });
            commonSupport.restartServer(restartProgress);
            return updateCRProgress;
        } else {
            if (commonSupport2 != null && requiredLibraries.length > 0) {
                commonSupport2.deploy(deployProgress, dir, moduleName, null, Collections.<String, String>emptyMap(), requiredLibraries);
            } else {
                commonSupport.deploy(deployProgress, dir, moduleName);
            }
            return updateCRProgress;
        }
    }
    
    /**
     * 
     * @param targetModuleID 
     * @param appChangeDescriptor 
     * @return 
     */
    @Override
    public ProgressObject incrementalDeploy(final TargetModuleID targetModuleID, AppChangeDescriptor appChangeDescriptor) {
        return incrementalDeploy(targetModuleID, appChangeDescriptor, new File[0]);
    }

    @Override
    public ProgressObject incrementalDeploy(final TargetModuleID targetModuleID, DeploymentContext context) {
        return incrementalDeploy(targetModuleID, context.getChanges(), context.getRequiredLibraries());
    }

    private ProgressObject incrementalDeploy(final TargetModuleID targetModuleID, AppChangeDescriptor appChangeDescriptor, final File[] requiredLibraries) {
        final MonitorProgressObject progressObject = new MonitorProgressObject(dm,
                (Hk2TargetModuleID) targetModuleID, CommandType.REDEPLOY);
        MonitorProgressObject restartObject = new MonitorProgressObject(dm, (Hk2TargetModuleID) targetModuleID,
                CommandType.REDEPLOY);
        final MonitorProgressObject updateCRObject = new MonitorProgressObject(dm,
                (Hk2TargetModuleID) targetModuleID, CommandType.REDEPLOY);
        progressObject.addProgressListener(new UpdateContextRoot(updateCRObject,(Hk2TargetModuleID) targetModuleID, dm.getServerInstance(), ! (null == targetModuleID.getWebURL())));
        final GlassfishModule commonSupport = dm.getCommonServerSupport();
        final GlassfishModule2 commonSupport2 = (commonSupport instanceof GlassfishModule2 ?
            (GlassfishModule2)commonSupport : null);
        boolean restart = false;
        try {
            restart = HttpMonitorHelper.synchronizeMonitor(
                    commonSupport.getInstanceProperties().get(GlassfishModule.DOMAINS_FOLDER_ATTR),
                    commonSupport.getInstanceProperties().get(GlassfishModule.DOMAIN_NAME_ATTR),
                    Boolean.parseBoolean(commonSupport.getInstanceProperties().get(GlassfishModule.HTTP_MONITOR_FLAG)),
                    "modules/org-netbeans-modules-schema2beans.jar");
        } catch (IOException ex) {
            Logger.getLogger("glassfish-javaee").log(Level.WARNING,"http monitor state",
                    ex);
        } catch (SAXException ex) {
            Logger.getLogger("glassfish-javaee").log(Level.WARNING,"http monitor state",
                    ex);
        }
        final boolean resourcesChanged = containsFileWithName("glassfish-resources.xml",appChangeDescriptor.getChangedFiles()); // NOI18N
        final boolean hasChanges = appChangeDescriptor.classesChanged() ||
                appChangeDescriptor.descriptorChanged() ||
                appChangeDescriptor.ejbsChanged() ||
                appChangeDescriptor.manifestChanged() ||
                appChangeDescriptor.serverDescriptorChanged() ||
                resourcesChanged;

        if(appChangeDescriptor instanceof DeploymentChangeDescriptor) {
            DeploymentChangeDescriptor dcd = (DeploymentChangeDescriptor)appChangeDescriptor;
            if (dcd.serverResourcesChanged()) {
                File dir = getDirectoryForModule(targetModuleID);
                if (null != dir) {
                    ResourceRegistrationHelper.deployResources(dir, dm);
                }
            }
        }
                
        if (restart) {
            restartObject.addProgressListener(new ProgressListener() {

                @Override
                public void handleProgressEvent(ProgressEvent event) {
                    if (event.getDeploymentStatus().isCompleted()) {
                        if (hasChanges) {
                            if (commonSupport2 != null && requiredLibraries.length > 0) {
                                commonSupport2.redeploy(progressObject, targetModuleID.getModuleID(), null, requiredLibraries,resourcesChanged);
                            } else {
                                commonSupport.redeploy(progressObject, targetModuleID.getModuleID(),resourcesChanged);
                            }
                        } else {
                            progressObject.fireHandleProgressEvent(event.getDeploymentStatus());
                        }
                    } else {
                        progressObject.fireHandleProgressEvent(event.getDeploymentStatus());
                    }
                }
            });
            commonSupport.restartServer(restartObject);
            return updateCRObject;
        } else {
            if (hasChanges) {
                if (commonSupport2 != null && requiredLibraries.length > 0) {
                    commonSupport2.redeploy(progressObject, targetModuleID.getModuleID(), null, requiredLibraries, resourcesChanged);
                } else {
                    commonSupport.redeploy(progressObject, targetModuleID.getModuleID(), resourcesChanged);
                }
            } else {
                progressObject.operationStateChanged(GlassfishModule.OperationState.COMPLETED,
                        NbBundle.getMessage(FastDeploy.class, "MSG_RedeployUnneeded"));
            }
            return updateCRObject;
        }
    }
    
    /**
     * 
     * @param target 
     * @param deployable 
     * @return 
     */
    @Override
    public boolean canFileDeploy(Target target, J2eeModule deployable) {
        if (null == deployable){
            return false;
        }
        
        if (deployable.getType() == J2eeModule.Type.CAR) {
            return false;
        }

        return true;
    }
    
    /**
     * @return Absolute path root directory for the specified app or null if
     *   server can accept the deployment from an arbitrary directory.
     */
    @Override
    public File getDirectoryForNewApplication(Target target, J2eeModule app, ModuleConfiguration configuration) {
        File dest = null;
        if (app.getType() == J2eeModule.Type.EAR) {
            File tmp = getProjectDir(app);
            if (null == tmp) {
               return dest;
            }
            dest = new File(tmp, "target");  // NOI18N
            if (!dest.exists()) {
                // the app wasn't a maven project
                dest = new File(tmp, "dist");  // NOI18N
            }
            if (dest.isFile() || (dest.isDirectory() && !dest.canWrite())) {
               throw new IllegalStateException();
            }
            String moduleName = org.netbeans.modules.glassfish.spi.Utils.sanitizeName(Utils.computeModuleID(app, null, null));
            String dirName = "gfdeploy"; // NOI18N
            if (null != moduleName) {
                dirName += "/"+moduleName; // NOI18N
            }
            dest = new File(dest, dirName);
            boolean retval = true;
            if (!dest.exists()) {
                retval = dest.mkdirs();
            }
            if (!retval || !dest.isDirectory()) {
               dest = null;
            }
        }
        return dest;
    }
    
    /**
     * 
     * @param file 
     * @param string 
     * @param app 
     * @param configuration 
     * @return 
     */
    @Override
    public File getDirectoryForNewModule(File file, String string, J2eeModule app, ModuleConfiguration configuration) {
        return new File(file, transform(removeLeadSlash(string)));
    }

    private String removeLeadSlash(String s) {
        if (null == s) {
            return s;
        }
        if (s.length() < 1) {
            return s;
        }
        if (!s.startsWith("/")) {
            return s;
        }
        return s.substring(1);
    }

    static String transform(String s) {
        int len = s.length();
        if (len > 4) {
            StringBuilder sb = new StringBuilder(s);
            char tmp = sb.charAt(len - 4);
            if (tmp == '.') {
                sb.setCharAt(len-4, '_');
                return sb.toString();
            }
        }
        return s;
    }
    
    /**
     * 
     * @param targetModuleID 
     * @return 
     */
    @Override
    public File getDirectoryForModule(TargetModuleID targetModuleID) {
        return new File(((Hk2TargetModuleID) targetModuleID).getLocation());
    }

    @Override
    public ProgressObject deployOnSave(TargetModuleID module, DeploymentChangeDescriptor desc) {
        return incrementalDeploy(module, desc);
    }

    @Override
    public boolean isDeployOnSaveSupported() {
        return !"false".equals(System.getProperty("glassfish.javaee.deployonsave"));
    }

    // try to get the Project Directory as a File
    // use a couple different stratgies, since the resource.dir is in a user-
    // editable file -- but it is quicker to access, if it is there....
    //
    private File getProjectDir(J2eeModule app) {
        try {
            FileObject fo = app.getContentDirectory();
            Project p = FileOwnerQuery.getOwner(fo);
            if (null != p) {
                fo = p.getProjectDirectory();
                return FileUtil.toFile(fo);
            }
        } catch (IOException ex) {
            Logger.getLogger("glassfish-javaee").log(Level.FINER,    // NOI18N
                    null,ex);
        }
        java.io.File tmp = app.getResourceDirectory();

        if (tmp != null) {
            return tmp.getParentFile();
        }
        return null;
    }

    @Override
    public String getModuleUrl(TargetModuleID module) {
        assert null != module;
        if (null == module) {
            return "/bogusModule";
        }
        Hk2TargetModuleID self = (Hk2TargetModuleID) module;
        String retVal = self.getModuleID();
        return retVal.startsWith("/") ? retVal : "/"+retVal;
    }

    private boolean containsFileWithName(String name, File[] changedFiles) {
        if (null == changedFiles || null == name)
            return false;
        for (File f : changedFiles) {
            String fp = null != f ? f.getAbsolutePath() : null;
            if (null != fp && fp.contains(name))
                return true;
        }
        return false;
    }
}
