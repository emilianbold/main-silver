/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ods.hudson;

import com.tasktop.c2c.server.profile.domain.build.BuildDetails;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails.BuildResult;
import com.tasktop.c2c.server.profile.domain.build.BuildSummary;
import com.tasktop.c2c.server.profile.domain.build.HudsonStatus;
import com.tasktop.c2c.server.profile.domain.build.JobDetails;
import com.tasktop.c2c.server.profile.domain.build.JobSummary;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonJobBuild.Result;
import org.netbeans.modules.hudson.api.HudsonVersion;
import org.netbeans.modules.hudson.spi.BuilderConnector;
import org.netbeans.modules.team.c2c.api.CloudServer;
import org.netbeans.modules.team.c2c.api.ODSProject;
import org.netbeans.modules.team.c2c.client.api.ClientFactory;
import org.netbeans.modules.team.c2c.client.api.CloudClient;
import org.netbeans.modules.team.c2c.client.api.CloudException;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author jhavlin
 */
public class ODSBuilderConnector extends BuilderConnector {

    private static final Logger LOG = Logger.getLogger(
            ODSBuilderConnector.class.getName());
    private ProjectHandle<ODSProject> projectHandle;
    private HudsonInstance hudsonInstance;

    public HudsonInstance getHudsonInstance() {
        return hudsonInstance;
    }

    public void setHudsonInstance(HudsonInstance hudsonInstance) {
        this.hudsonInstance = hudsonInstance;
    }

    public ODSBuilderConnector(ProjectHandle<ODSProject> projectHandle) {
        this.projectHandle = projectHandle;
    }

    @Override
    public InstanceData getInstanceData(boolean authentication) {
        try {
            HudsonStatus hudsonStatus = getClient().getHudsonStatus(projectHandle.getId());
            if(hudsonStatus == null) {
                return null;
            }
            List<JobData> jobs = new ArrayList<JobData>();
            String url = null;
            for (JobSummary job : hudsonStatus.getJobs()) {
                JobData jd = new JobData();
                jd.setJobName(job.getName());
                jd.setColor(Color.find(job.getColor()));
                jd.setJobUrl(job.getUrl());
                jd.setDisplayName(job.getName());
                jd.setBuildable(true); //TODO
                jd.setInQueue(false); //TODO
                jd.setSecured(false); //TODO
                jd.addView("All"); //NOI18N
                jobs.add(jd);
                if (url == null) {
                    String noLastSlash = job.getUrl().substring(
                            0, job.getUrl().length() - 1);
                    String parentUrl = noLastSlash.substring(
                            0, noLastSlash.lastIndexOf('/') + 1);
                    url = parentUrl;
                }
            }
            List<ViewData> viewsData = new ArrayList<ViewData>();
            viewsData.add(new ViewData("All", url, true));
            return new InstanceData(jobs, viewsData);
        } catch (CloudException ex) {
            LOG.log(Level.INFO, null, ex);
            return new InstanceData(Collections.<JobData>emptyList(),
                    Collections.<ViewData>emptyList());
        }
    }

    @Override
    public Collection<BuildData> getJobBuildsData(HudsonJob job) {
        try {
            CloudClient client = getClient();
            JobDetails jds = client.getJobDetails(projectHandle.getId(), job.getName());
            List<BuildSummary> builds = jds.getBuilds();
            ArrayList<BuildData> ret = new ArrayList<BuildData>(builds.size());
            for (BuildSummary b : builds) {
                BuildDetails bds = client.getBuildDetails(projectHandle.getId(), job.getName(), b.getNumber());
                if(bds == null) {
                    continue;
                }
                BuildResult result = bds.getResult();
                ret.add(new BuildData(bds.getNumber(), result == null ? null : Result.valueOf(result.name()), bds.getBuilding()));
            }
            return ret;
        } catch (CloudException ex) {
            LOG.log(Level.INFO, null, ex);
            return Collections.emptyList(); 
        }
    }

    @Override
    public void getJobBuildResult(HudsonJobBuild build, AtomicBoolean building, AtomicReference<Result> result) {
        try {
            BuildDetails bds = getClient().getBuildDetails(projectHandle.getId(), build.getJob().getName(), build.getNumber());
            building.set(bds.getBuilding());
            result.set(Result.valueOf(bds.getResult().name()));
        } catch (CloudException ex) {
            LOG.log(Level.INFO, null, ex);
        }
    }

    @Override
    public FileSystem getArtifacts(HudsonJobBuild build) {
        return FileUtil.createMemoryFileSystem();
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean isForbidden() {
        return false;
    }

    @Override
    public HudsonVersion getHudsonVersion(boolean authentication) {
        return new HudsonVersion("2.2"); //TODO
    }

    @Override
    public void startJob(HudsonJob job) {
        // TODO
    }

    @Override
    public ConsoleDisplayer getConsoleDisplayer() {
        return null;
    }

    @Override
    public FailureDisplayer getFailureDisplayer() {
        return null;
    }
    
    private CloudClient getClient() {
        CloudServer server = projectHandle.getTeamProject().getServer();
        return ClientFactory.getInstance().createClient(server.getUrl().toString(), server.getPasswordAuthentication());        
    }

}
