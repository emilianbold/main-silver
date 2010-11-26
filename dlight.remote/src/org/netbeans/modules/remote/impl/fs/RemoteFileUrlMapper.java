/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs;

import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Kvashin
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.filesystems.URLMapper.class)
public class RemoteFileUrlMapper extends URLMapper {

    @Override
    public FileObject[] getFileObjects(URL url) {
        if (url.getProtocol().equals(RemoteFileURLStreamHandler.PROTOCOL)) {
            ExecutionEnvironment env;
            String user = url.getUserInfo();
            if (user != null) {
                env = ExecutionEnvironmentFactory.createNew(user, url.getHost(), url.getPort());
            } else {
                RemoteLogger.assertTrue(false, "Trying to access remote file system without user name");
                env = RemoteFileSystemUtils.getExecutionEnvironment(url.getHost(), url.getPort());
                if (env == null) {
                    user = System.getProperty("user.name");
                    if (user != null) {
                        env = ExecutionEnvironmentFactory.createNew(user, url.getHost(), url.getPort());
                    }
                }
            }
            if (env != null) {
                RemoteFileSystem fs = RemoteFileSystemManager.getInstance().getFileSystem(env);
                FileObject fo = fs.findResource(url.getFile());
                return new FileObject[] { fo };
            }
        }
        return null;
    }

    @Override
    public URL getURL(FileObject fo, int type) {
        if (fo instanceof RemoteFileObjectBase) {
            RemoteFileObjectBase rfo = (RemoteFileObjectBase) fo;
            try {
                ExecutionEnvironment env = rfo.getExecutionEnvironment();
                return getURL(env, rfo.getPath());
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    /*package*/ static URL getURL(ExecutionEnvironment env, String path) throws MalformedURLException {
        String host = env.getUser() + '@' + env.getHost();
        URL url = new URL(RemoteFileURLStreamHandler.PROTOCOL, host, env.getSSHPort(), path);
        String ext = url.toExternalForm(); // is there a way to set authority?
        return new URL(ext);
    }
}
