/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cloud.oracle.serverplugin;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.Future;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.swing.event.ChangeListener;
import org.netbeans.libs.oracle.cloud.sdkwrapper.api.ApplicationManager;
import org.netbeans.modules.cloud.common.spi.support.serverplugin.DeploymentStatus;
import org.netbeans.modules.cloud.common.spi.support.serverplugin.ProgressObjectImpl;
import org.netbeans.modules.cloud.common.spi.support.serverplugin.TargetImpl;
import org.netbeans.modules.cloud.oracle.OracleInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentManager2;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 *
 */
public class OracleDeploymentManager implements DeploymentManager2 {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final InstanceProperties props;
    
    private ApplicationManager pm;
    private String identityDomain;
    private String javaServiceName;
    private String cloudInstanceName;

    // FIXME should we query IP for other props as well rather than fetching
    // them before construction
    public OracleDeploymentManager(ApplicationManager pm, String identityDomain, 
          String javaServiceName, String cloudInstanceName, InstanceProperties props) {
        this.pm = pm;
        this.identityDomain = identityDomain;
        this.javaServiceName = javaServiceName;
        this.cloudInstanceName = cloudInstanceName;
        
        this.props = props;
        props.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (OracleDeploymentFactory.IP_PREMISE_SERVICE_INSTANCE_ID.equals(evt.getPropertyName())) {
                    changeSupport.fireChange();
                }
            }
        });
    }

    public String getOnPremiseServiceInstanceId() {
        return props.getProperty(OracleDeploymentFactory.IP_PREMISE_SERVICE_INSTANCE_ID);
    }
    
    public void addOnPremiseServerInstanceIdListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeOnPremiseServerInstanceIdListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public ProgressObject redeploy(TargetModuleID[] tmids, DeploymentContext deployment) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    @NbBundle.Messages({
        "MSG_NoEjbDeployment=Deployment of standalone EJB module is not support. Deploy it in EAR instead.",
        "MSG_SDKProblem=There was an error calling Cloud SDK. Double check your cloud configuration and try again."})
    public ProgressObject distribute(Target[] targets, DeploymentContext deployment) {
        File f = deployment.getModuleFile();
        ProgressObjectImpl po = new ProgressObjectImpl(NbBundle.getMessage(OracleDeploymentManager.class, "OracleDeploymentManager.distributing"), false);
        if (deployment.getModule().getType() == J2eeModule.Type.EJB) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.MSG_NoEjbDeployment()));
            po.updateDepoymentResult(DeploymentStatus.FAILED, null);
            return po;
        }
        if (pm == null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.MSG_SDKProblem()));
            po.updateDepoymentResult(DeploymentStatus.FAILED, null);
            return po;
        }
        OracleInstance.deployAsync(pm, f, identityDomain, javaServiceName, po, cloudInstanceName, getOnPremiseServiceInstanceId());
        return po;
    }

    @Override
    public Target[] getTargets() throws IllegalStateException {
        return new Target[]{TargetImpl.SOME};
    }

    @Override
    public TargetModuleID[] getRunningModules(ModuleType mt, Target[] targets) throws TargetException, IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public TargetModuleID[] getNonRunningModules(ModuleType mt, Target[] targets) throws TargetException, IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public TargetModuleID[] getAvailableModules(ModuleType mt, Target[] targets) throws TargetException, IllegalStateException {
        return new TargetModuleID[0];
    }

    @Override
    public DeploymentConfiguration createConfiguration(DeployableObject d) throws InvalidModuleException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public ProgressObject distribute(Target[] targets, File file, File file1) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    @Deprecated
    public ProgressObject distribute(Target[] targets, InputStream in, InputStream in1) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public ProgressObject distribute(Target[] targets, ModuleType mt, InputStream in, InputStream in1) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public ProgressObject start(TargetModuleID[] tmids) throws IllegalStateException {
        return new ProgressObjectImpl("", true); // NOI18N
    }

    @Override
    public ProgressObject stop(TargetModuleID[] tmids) throws IllegalStateException {
        return new ProgressObjectImpl("", true); // NOI18N
    }

    @Override
    public ProgressObject undeploy(TargetModuleID[] tmids) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public boolean isRedeploySupported() {
        return true;
    }

    @Override
    public ProgressObject redeploy(TargetModuleID[] tmids, File file, File file1) throws UnsupportedOperationException, IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public ProgressObject redeploy(TargetModuleID[] tmids, InputStream in, InputStream in1) throws UnsupportedOperationException, IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void release() {
    }

    @Override
    public Locale getDefaultLocale() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public Locale getCurrentLocale() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void setLocale(Locale locale) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public Locale[] getSupportedLocales() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public boolean isLocaleSupported(Locale locale) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public DConfigBeanVersionType getDConfigBeanVersion() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType dcbvt) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void setDConfigBeanVersion(DConfigBeanVersionType dcbvt) throws DConfigBeanVersionUnsupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

}
