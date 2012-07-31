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

package org.netbeans.modules.web.clientproject.api;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectConfigurationImplementation;
import org.netbeans.modules.web.clientproject.spi.webserver.ServerURLMappingImplementation;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 * Provides mapping between project's source file and its location on server
 * and vice versa. If project has not deployed files to a server the file:///
 * URL will be returned instead.
 */
public final class ServerURLMapping {

    private ServerURLMapping() {
    }
    
    /**
     * Convert given project's file into server URL.
     * @return could return null if file is not deployed to server and therefore
     *   not accessible
     */
    public static URL toServer(Project p, FileObject projectFile) {
        ClientProjectConfigurationImplementation cfg = (ClientProjectConfigurationImplementation)
                p.getLookup().lookup(ProjectConfigurationProvider.class).getActiveConfiguration();
        if (cfg == null) {
            return null;
        }
        ServerURLMappingImplementation impl = cfg.getServerURLMapping();
        if (impl != null) {
            URL u = impl.toServer(projectFile);
            if (u != null) {
                return u;
            }
        }
        try {
            URL url = projectFile.toURL();
            String urlString = url.toURI().toString();
            String urlString2 = urlString.replaceAll("file:/", "file:///");
            if (!urlString.equals(urlString2)) {
                url = new URL(urlString2);
            }
            return url;
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    /**
     * Convert given server URL into project's file.
     * @return returns null if nothing is known about this server URL
     */
    public static FileObject fromServer(Project p, URL serverURL) {
        ClientProjectConfigurationImplementation cfg = (ClientProjectConfigurationImplementation)
                p.getLookup().lookup(ProjectConfigurationProvider.class).getActiveConfiguration();
        if (cfg == null) {
            return null;
        }
        ServerURLMappingImplementation impl = cfg.getServerURLMapping();
        if (impl != null) {
            FileObject fo = impl.fromServer(serverURL);
            if (fo != null) {
                return fo;
            }
        }
        if ("file".equals(serverURL.getProtocol())) {
            return URLMapper.findFileObject(serverURL);
        }
        return null;
    }
}
