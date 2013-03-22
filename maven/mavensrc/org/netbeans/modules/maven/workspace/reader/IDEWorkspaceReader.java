/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.workspace.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.repository.WorkspaceRepository;

/**
 *
 * @author mkleint
 */
public class IDEWorkspaceReader implements WorkspaceReader {

    private final WorkspaceRepository repo = new WorkspaceRepository("ide");
    private final Map<String, File> mappings;

    public IDEWorkspaceReader() {
        mappings = new HashMap<String, File>();
        String mapp = System.getenv("netbeansProjectMappings");
        if (mapp != null) {
            StringTokenizer st = new StringTokenizer(mapp, ",");
            while (st.hasMoreTokens()) {
                String tok = st.nextToken();
                StringTokenizer st2 = new StringTokenizer(tok, "=");
                if (st2.hasMoreTokens()) {
                    String gav = st2.nextToken();
                    if (st2.hasMoreElements()) {
                        String file = st2.nextToken();
                        File f = new File(file);
                        if (f.exists()) {
                            mappings.put(gav, new File(file));
                        }
                    }
                }
            }
        }

    }

    public WorkspaceRepository getRepository() {
        return repo;
    }

    public File findArtifact(Artifact artifact) {
        File f = mappings.get(artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getBaseVersion());
        if (f != null) {
            if ("pom".equals(artifact.getExtension())) {
                System.out.println("artifact pom=" + artifact + " " + new File(f, "pom.xml"));
                return new File(f, "pom.xml");
            }
            if ("jar".equals(artifact.getExtension()) && "".equals(artifact.getClassifier())) {
                System.out.println("artifact jar=" + artifact + " " + new File(f, "target/classes"));
                return new File(new File(f, "target"), "classes");
            }
            if ("jar".equals(artifact.getExtension()) && "tests".equals(artifact.getClassifier())) {
                System.out.println("artifact test jar=" + artifact + " " + new File(f, "target/test-classes"));
                return new File(new File(f, "target"), "test-classes");
            }
        }
        return null;
    }

    public List<String> findVersions(Artifact artifact) {
        String id = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":";
        List<String> toRet = new ArrayList<String>();
        for (String s : mappings.keySet()) {
            if (s.startsWith(id)) {
                toRet.add(s.substring(id.length()));
                System.out.println("ver=" + s.substring(id.length()));
            }
        }
        return toRet;
    }
}
