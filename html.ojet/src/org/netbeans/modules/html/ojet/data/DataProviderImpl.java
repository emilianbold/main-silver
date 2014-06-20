/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.ojet.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.html.ojet.OJETUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author Petr Pisl
 */
public class DataProviderImpl extends DataProvider {

    private static DataProviderImpl instance = null;
    private static final String zipURL = "docs/ojetdocs.zip";
    private static final HashMap<String, DataItemImpl.DataItemComponent> data = new HashMap<>();
    private static FileObject docRoot = null;

    synchronized public static DataProvider getInstance() {
        if (instance == null) {
            instance = new DataProviderImpl();
            File zipFile = InstalledFileLocator.getDefault().locate(zipURL, "org.netbeans.modules.html.ojet", false); //NOI18N
            if (zipFile.exists()) {
                docRoot = FileUtil.toFileObject(zipFile);
                docRoot = FileUtil.getArchiveRoot(docRoot);
                if (docRoot != null) {
                    FileObject folder = docRoot.getFileObject("docs"); // NOI18N
                    if (folder != null && folder.isValid()) {
                        for (FileObject child : folder.getChildren()) {
                            String name = child.getName();
                            if (name.startsWith("oj.oj")) {
                                name = name.substring(3);
                                
                                data.put(name, new DataItemImpl.DataItemComponent(name, child.toURL().toString()));
                            }
                        }
                    }
                }
            }
        }
        return instance;
    }

    @Override
    public Collection<DataItem> getBindingOptions() {
        List<DataItem> result = new ArrayList(1);
        result.add(new DataItemImpl(OJETUtils.OJ_COMPONENT, null));
        return result;
    }

    @Override
    public Collection<DataItem> getComponents() {
        List<DataItem> result = new ArrayList<>();
        for (DataItem component : data.values()) {
            result.add(component);
        }
        return result;
    }

    @Override
    public Collection<DataItem> getComponentOptions(String compName) {
        DataItemImpl.DataItemComponent component = data.get(compName);
        if (component != null) {
            return component.getOptions();
        }
        return Collections.emptyList();
    }
}
