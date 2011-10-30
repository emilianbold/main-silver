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

package org.netbeans.modules.hudson.impl;

import java.io.IOException;
import java.util.ArrayList;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.impl.HudsonJobImpl.HudsonMavenModule;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.Utilities;
import org.netbeans.modules.hudson.spi.HudsonSCM;
import org.netbeans.modules.hudson.ui.interfaces.OpenableInBrowser;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.hudson.impl.Bundle.*;
import org.openide.windows.OutputListener;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class HudsonJobBuildImpl implements HudsonJobBuild, OpenableInBrowser {

    private static final Logger LOG = Logger.getLogger(HudsonJobBuildImpl.class.getName());

    private final HudsonJobImpl job;
    private final int build;
    private boolean building;
    private Result result;
    private final HudsonConnector connector;

    HudsonJobBuildImpl(HudsonConnector connector, HudsonJobImpl job, int build, boolean building, Result result) {
        this.connector = connector;
        this.job = job;
        this.build = build;
        this.building = building;
        this.result = result;
    }
    
    public HudsonJob getJob() {
        return job;
    }

    public int getNumber() {
        return build;
    }

    public String getUrl() {
        return job.getUrl() + build + "/"; // NOI18N
    }

    public @Override String toString() {
        return getUrl();
    }

    public @Override boolean isBuilding() {
        getResult();
        return building;
    }
    
    public @Override synchronized Result getResult() {
        if (result == null && !building) {
            AtomicBoolean _building = new AtomicBoolean();
            AtomicReference<Result> _result = new AtomicReference<Result>(Result.NOT_BUILT);
            connector.loadResult(this, _building, _result);
            building = _building.get();
            result = _result.get();
        }
        return result;
    }

    private Collection<? extends HudsonJobChangeItem> changes;
    public Collection<? extends HudsonJobChangeItem> getChanges() {
        if (changes == null || /* #171978 */changes.isEmpty()) {
            for (HudsonSCM scm : Lookup.getDefault().lookupAll(HudsonSCM.class)) {
                changes = scm.parseChangeSet(this);
                if (changes != null) {
                    break;
                }
            }
            if (changes == null) {
                changes = parseChangeSetGeneric();
            }
        }
        return changes;
    }

    public FileSystem getArtifacts() {
        return job.getInstance().getArtifacts(this);
    }

    public Collection<? extends HudsonMavenModuleBuild> getMavenModules() {
        List<HudsonMavenModuleBuildImpl> modules = new ArrayList<HudsonMavenModuleBuildImpl>();
        for (HudsonJobImpl.HudsonMavenModule module : job.mavenModules) {
            modules.add(new HudsonMavenModuleBuildImpl(module));
        }
        return modules;
    }

    @Messages({"# {0} - job/module display name", "# {1} - build number", "HudsonJobBuildImpl.display_name={0} #{1,number,#}"})
    public String getDisplayName() {
        return HudsonJobBuildImpl_display_name(job.getDisplayName(), getNumber());
    }

    private Collection<? extends HudsonJobChangeItem> parseChangeSetGeneric() {
        final Element changeSet;
        try {
            changeSet = XMLUtil.findElement(new ConnectionBuilder().job(job).url(getUrl() + "api/xml?tree=changeSet[items[author[fullName],msg,affectedFile[path,editType]]]").parseXML().getDocumentElement(), "changeSet", null);
        } catch (IOException x) {
            LOG.log(Level.WARNING, "could not parse changelog for {0}: {1}", new Object[] {this, x});
            return Collections.emptyList();
        }
        class Item implements HudsonJobChangeItem {
            final Element itemXML;
            Item(Element itemXML) {
                this.itemXML = itemXML;
            }
            @Override public String getUser() {
                return Utilities.xpath("author/fullName", itemXML);
            }
            @Override public String getMessage() {
                return Utilities.xpath("msg", itemXML);
            }
            @Override public Collection<? extends HudsonJobChangeItem.HudsonJobChangeFile> getFiles() {
                class AffectedFile implements HudsonJobChangeItem.HudsonJobChangeFile {
                    final Element fileXML;
                    AffectedFile(Element fileXML) {
                        this.fileXML = fileXML;
                    }
                    @Override public String getName() {
                        return Utilities.xpath("path", fileXML);
                    }
                    @Override public HudsonJobChangeItem.HudsonJobChangeFile.EditType getEditType() {
                        return HudsonJobChangeItem.HudsonJobChangeFile.EditType.valueOf(Utilities.xpath("editType", fileXML));
                    }
                    @Override public OutputListener hyperlink() {
                        return null;
                    }
                }
                List<AffectedFile> files = new ArrayList<AffectedFile>();
                // XXX this is not typically @Exported, in which case no file changes will be shown
                NodeList nl = itemXML.getElementsByTagName("affectedFile");
                for (int i = 0; i < nl.getLength(); i++) {
                    files.add(new AffectedFile((Element) nl.item(i)));
                }
                return files;
            }
        }
        List<Item> items = new ArrayList<Item>();
        NodeList nl = changeSet.getElementsByTagName("item");
        for (int i = 0; i < nl.getLength(); i++) {
            items.add(new Item(((Element) nl.item(i))));
        }
        return items;
    }

    private final class HudsonMavenModuleBuildImpl implements HudsonMavenModuleBuild, OpenableInBrowser {

        private final HudsonJobImpl.HudsonMavenModule module;

        HudsonMavenModuleBuildImpl(HudsonMavenModule module) {
            this.module = module;
        }

        public String getName() {
            return module.name;
        }

        public String getDisplayName() {
            return module.displayName;
        }

        public Color getColor() {
            return module.color;
        }

        public String getUrl() {
            return module.url + build + "/"; // NOI18N
        }

        public HudsonJobBuild getBuild() {
            return HudsonJobBuildImpl.this;
        }

        public FileSystem getArtifacts() {
            return job.getInstance().getArtifacts(this);
        }

        public @Override String toString() {
            return getUrl();
        }

        public String getBuildDisplayName() {
            return HudsonJobBuildImpl_display_name(getDisplayName(), getNumber());
        }

    }

}
