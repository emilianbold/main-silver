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

package org.netbeans.modules.web.clientproject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import org.netbeans.modules.javascript.jstestdriver.api.JsTestDriver;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectConstants;
import org.netbeans.modules.web.clientproject.api.ServerURLMapping;
import org.netbeans.modules.web.clientproject.spi.webserver.ServerURLMappingImplementation;
import org.netbeans.modules.web.clientproject.spi.webserver.WebServer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 */
public class ServerURLMappingImpl implements ServerURLMappingImplementation {

    private ClientSideProject project;

    public ServerURLMappingImpl(ClientSideProject project) {
        this.project = project;
    }

    @Override
    public URL toServer(int projectContext, FileObject projectFile) {
        if (projectContext == ServerURLMapping.CONTEXT_PROJECT_SOURCES) {
            if (project.isUsingEmbeddedServer()) {
                return WebServer.getWebserver().toServer(projectFile);
            } else {
                String relPath = FileUtil.getRelativePath(project.getSiteRootFolder(), projectFile);
                String root = project.getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_PROJECT_URL);
                if (!root.endsWith("/")) { //NOI18N
                    root += "/"; //NOI18N
                }
                try {
                    return new URL(root + relPath);
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                }
            }
        } else {
            return toJsTestDriverServer(projectFile);
        }
    }

    @Override
    public FileObject fromServer(int projectContext, URL serverURL) {
        FileObject fo = null;
        if (project.isUsingEmbeddedServer()) {
            fo = WebServer.getWebserver().fromServer(serverURL);
        } else {
            String root = project.getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_PROJECT_URL);
            String u = serverURL.toExternalForm();
            if (u.startsWith(root)) {
                u = u.substring(root.length());
                if (u.startsWith("/")) { //NOI18N
                    u = u.substring(1);
                }
                fo = project.getSiteRootFolder().getFileObject(u);
            }
        }
        if (fo == null) {
            fo = fromJsTestDriverServer(serverURL);
        }
        return fo;
    }

    private URL toJsTestDriverServer(FileObject projectFile) {
        String prefix = JsTestDriver.getServerURL();
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }
        prefix += "test/";
        String relativePath = FileUtil.getRelativePath(project.getProjectDirectory(), projectFile);
        if (relativePath != null) {
            try {
                return new URL(prefix+relativePath);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }
    
    private FileObject fromJsTestDriverServer(URL serverURL) {
        String serverU = serverURL.toExternalForm();
        String prefix = JsTestDriver.getServerURL();
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }
        prefix += "test/";
        if (!serverU.startsWith(prefix)) {
            return null;
        }
        String projectRelativePath = serverU.substring(prefix.length());
        try {
            projectRelativePath = URLDecoder.decode(projectRelativePath, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (projectRelativePath.length() > 0) {
            return project.getProjectDirectory().getFileObject(projectRelativePath);
        }
        return null;
    }

}
