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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.makeproject;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectRegistry;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup.Provider;

/**
 * FileOwnerQuery dealing with files that are not in the project directory.
 * A typical situation for CND project created from existing sources.
 *
 * @author Alexey Vladykin
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.project.FileOwnerQueryImplementation.class, position=98)
public class MakeProjectFileOwnerQuery implements FileOwnerQueryImplementation {

    @Override
    public Project getOwner(URI uri) {
        return getOwner(toFileObject(uri));
    }
    
    private FileObject toFileObject(URI uri) {
        try {
            URL url =  uri.toURL();
            return URLMapper.findFileObject(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public Project getOwner(FileObject fo) {
        if (fo == null) {
            return null;
        }
        FileSystem fs;
        try {
            fs = fo.getFileSystem();
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }        
        String path = CndPathUtilitities.normalizeSlashes(fo.getPath());
        for(NativeProject nativeProject : NativeProjectRegistry.getDefault().getOpenProjects()) {
            Provider project = nativeProject.getProject();
            if (project instanceof Project) {
                if (!fs.equals(RemoteFileUtil.getProjectSourceFileSystem((Project) project))) {
                    continue;
                }
                ConfigurationDescriptorProvider provider = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
                if (provider != null && provider.gotDescriptor()) {
                    MakeConfigurationDescriptor descriptor = provider.getConfigurationDescriptor();
                    if (descriptor != null) {
                        boolean mine = false;
                        if (fo.isData()) {
                            mine = descriptor.findProjectItemByPath(path) != null || descriptor.findExternalItemByPath(path) != null;
                        } else if (fo.isFolder()) {
                            mine = descriptor.findFolderByPath(path) != null;
                        }
                        if (!mine) {
                            List<String> absRoots = new ArrayList<String>();
                            absRoots.addAll(descriptor.getAbsoluteSourceRoots());
                            absRoots.addAll(descriptor.getAbsoluteTestRoots());
                            for (String srcPath : absRoots) {
                                srcPath = CndPathUtilitities.normalizeSlashes(srcPath);
                                if (path.startsWith(srcPath)) {
                                    mine = true;
                                    break;
                                }
                            }
                        }
                        if (mine) {
                            return (Project) project;
                        }
                    }
                }
            }
        }
        return null;
    }
}
