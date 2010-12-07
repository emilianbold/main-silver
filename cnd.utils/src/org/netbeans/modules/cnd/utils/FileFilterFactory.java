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

package org.netbeans.modules.cnd.utils;

import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.utils.filters.AllBinaryFileFilter;
import org.netbeans.modules.cnd.utils.filters.AllFileFilter;
import org.netbeans.modules.cnd.utils.filters.AllSourceFileFilter;
import org.netbeans.modules.cnd.utils.filters.CCSourceFileFilter;
import org.netbeans.modules.cnd.utils.filters.CSourceFileFilter;
import org.netbeans.modules.cnd.utils.filters.ConfigureFileFilter;
import org.netbeans.modules.cnd.utils.filters.ElfDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.utils.filters.ElfExecutableFileFilter;
import org.netbeans.modules.cnd.utils.filters.ElfStaticLibraryFileFilter;
import org.netbeans.modules.cnd.utils.filters.FortranSourceFileFilter;
import org.netbeans.modules.cnd.utils.filters.HeaderSourceFileFilter;
import org.netbeans.modules.cnd.utils.filters.MacOSXDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.utils.filters.MacOSXExecutableFileFilter;
import org.netbeans.modules.cnd.utils.filters.MakefileFileFilter;
import org.netbeans.modules.cnd.utils.filters.PeDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.utils.filters.PeExecutableFileFilter;
import org.netbeans.modules.cnd.utils.filters.PeStaticLibraryFileFilter;
import org.netbeans.modules.cnd.utils.filters.QtFileFilter;
import org.netbeans.modules.cnd.utils.filters.ResourceFileFilter;
import org.netbeans.modules.cnd.utils.filters.ShellFileFilter;
import org.netbeans.modules.cnd.utils.filters.WorkshopProjectFilter;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public final class FileFilterFactory {

    /* A combination of FileFilter and FileObjectFilter */
    public static abstract class FileAndFileObjectFilter
            extends FileFilter implements FileObjectFilter {
    }

    private FileFilterFactory() {
    }

    public static FileFilter[] getBinaryFilters() {
        FileFilter[] filters = null;
        if (Utilities.isWindows()) {
            filters = new FileFilter[]{
                        FileFilterFactory.getAllBinaryFileFilter(),
                        FileFilterFactory.getPeExecutableFileFilter(),
                        FileFilterFactory.getElfStaticLibraryFileFilter(),
                        FileFilterFactory.getPeDynamicLibraryFileFilter()
                    };
        } else if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            filters = new FileFilter[]{
                        FileFilterFactory.getAllBinaryFileFilter(),
                        FileFilterFactory.getMacOSXExecutableFileFilter(),
                        FileFilterFactory.getElfStaticLibraryFileFilter(),
                        FileFilterFactory.getMacOSXDynamicLibraryFileFilter()
                    };
        } else {
            filters = new FileFilter[]{
                        FileFilterFactory.getAllBinaryFileFilter(),
                        FileFilterFactory.getElfExecutableFileFilter(),
                        FileFilterFactory.getElfStaticLibraryFileFilter(),
                        FileFilterFactory.getElfDynamicLibraryFileFilter()
                    };
        }
        return filters;
    }
    
    public static FileAndFileObjectFilter getAllFileFilter(){
        return AllFileFilter.getInstance();
    }
    public static FileAndFileObjectFilter getAllSourceFileFilter(){
        return AllSourceFileFilter.getInstance();
    }
    public static FileAndFileObjectFilter getAllBinaryFileFilter(){
        return AllBinaryFileFilter.getInstance();
    }
    public static FileAndFileObjectFilter getCCSourceFileFilter(){
        return CCSourceFileFilter.getInstance();
    }
    public static FileAndFileObjectFilter getCSourceFileFilter(){
        return CSourceFileFilter.getInstance();
    }
    public static FileAndFileObjectFilter getConfigureFileFilter(){
        return ConfigureFileFilter.getInstance();
    }
    public static FileAndFileObjectFilter getElfDynamicLibraryFileFilter(){
        return ElfDynamicLibraryFileFilter.getInstance();
    }
    public static FileAndFileObjectFilter getElfExecutableFileFilter(){
        return ElfExecutableFileFilter.getInstance();
    }
    public static FileAndFileObjectFilter getElfStaticLibraryFileFilter(){
        return ElfStaticLibraryFileFilter.getInstance();
    }
    public static FileAndFileObjectFilter getFortranSourceFileFilter(){
        return FortranSourceFileFilter.getInstance();
    }
    public static FileAndFileObjectFilter getHeaderSourceFileFilter(){
        return HeaderSourceFileFilter.getInstance();
    }
    public static FileAndFileObjectFilter getMacOSXDynamicLibraryFileFilter(){
        return MacOSXDynamicLibraryFileFilter.getInstance();
    }
    public static FileAndFileObjectFilter getMacOSXExecutableFileFilter(){
        return MacOSXExecutableFileFilter.getInstance();
    }
    public static FileAndFileObjectFilter getMakefileFileFilter(){
        return MakefileFileFilter.getInstance();
    }
    public static FileAndFileObjectFilter getPeDynamicLibraryFileFilter(){
        return PeDynamicLibraryFileFilter.getInstance();
    }
    public static FileAndFileObjectFilter getPeExecutableFileFilter(){
        return PeExecutableFileFilter.getInstance();
    }
    public static FileAndFileObjectFilter getPeStaticLibraryFileFilter(){
        return PeStaticLibraryFileFilter.getInstance();
    }
    public static FileAndFileObjectFilter getQtFileFilter(){
        return QtFileFilter.getInstance();
    }
    public static FileAndFileObjectFilter getResourceFileFilter(){
        return ResourceFileFilter.getInstance();
    }
    public static FileAndFileObjectFilter getShellFileFilter(){
        return ShellFileFilter.getInstance();
    }
    public static FileAndFileObjectFilter getWorkshopProjectFilter(){
        return WorkshopProjectFilter.getInstance();
    }
}
