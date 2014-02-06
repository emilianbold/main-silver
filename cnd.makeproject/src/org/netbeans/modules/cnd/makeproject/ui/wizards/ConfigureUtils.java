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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.io.File;
import org.netbeans.modules.cnd.actions.AbstractExecutorRunAction;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;

/**
 *
 * @author Alexander Simon
 */
public final class ConfigureUtils {
    private static final String PREDEFINED_FLAGS_GNU = "\"-g3 -gdwarf-2\""; // NOI18N
    private static final String PREDEFINED_FLAGS_SUN = "-g"; // NOI18N

    private ConfigureUtils() {
    }

    public static String findConfigureScript(String path){
        return findConfigureScript(FileUtil.toFileObject(new File(path)));
    }

    public static String findConfigureScript(FileObject folder){
        if (folder == null) {
            return null;
        }
        String pattern[] = new String[]{"configure"}; // NOI18N
        if (!(folder.isFolder() && (folder.canRead()))) {
            return null;
        }
        for (String name : pattern) {
            FileObject child = folder.getFileObject(name); // NOI18N
            if (isRunnable(child)){
                return child.getPath();
            }
        }
        String res = detectQTProject(folder);
        if (res != null) {
            return res;
        }
        res = detectCMake(folder);
        if (res != null) {
            return res;
        }
        return null;
    }

    private static String detectQTProject(FileObject folder){
        if (folder == null) {
            return null;
        }
        final FileObject[] listFiles = folder.getChildren();
        if (listFiles == null) {
            return null;
        }
        for(FileObject file : listFiles){
            if (file.getExt().equals("pro")){ // NOI18N
                if (AbstractExecutorRunAction.findTools("qmake") != null){ // NOI18N
                    return file.getPath();
                }
                break;
            }
        }
        return null;
    }

    private static String detectCMake(FileObject folder){
        if (folder == null) {
            return null;
        }
        FileObject configure = folder.getFileObject("CMakeLists.txt"); // NOI18N
        if (configure != null && configure.isValid()) {
            if (AbstractExecutorRunAction.findTools("cmake") != null) { // NOI18N
                return configure.getPath();
            }
        }
        return null;
    }

    public static boolean isRunnable(FileObject configureFileObject) {
        if (configureFileObject == null) {
            return false;
        }
        if (configureFileObject.isValid() && configureFileObject.isData() && (configureFileObject.canRead()||FileSystemProvider.canExecute(configureFileObject))) {
            DataObject dObj;
            try {
                dObj = DataObject.find(configureFileObject);
            } catch (DataObjectNotFoundException ex) {
                return false;
            }
            if (dObj == null) {
                return false;
            }
            Node node = dObj.getNodeDelegate();
            if (node == null) {
                return false;
            }
            ShellExecSupport ses = node.getLookup().lookup(ShellExecSupport.class);
            if (ses != null) {
                return true;
            }
            if (configureFileObject.getPath().endsWith("CMakeLists.txt")){ // NOI18N
                return AbstractExecutorRunAction.findTools("cmake") != null; // NOI18N
            }
            if (configureFileObject.getPath().endsWith(".pro")){ // NOI18N
                return AbstractExecutorRunAction.findTools("qmake") != null; // NOI18N
            }
        }
        return false;
    }

    public static FileObject findMakefile(FileObject projDirFo) {
        String pattern[] = new String[]{"GNUmakefile", "makefile", "Makefile",}; // NOI18N
        if (projDirFo == null || !(projDirFo.isFolder() && projDirFo.canRead())) {
            return null;
        }
        for (String name : pattern) {
            FileObject makeFO = projDirFo.getFileObject(name); // NOI18N
            if (makeFO != null && makeFO.isValid() && makeFO.isData() && makeFO.canRead()) {
                return makeFO;
            }
        }
        return null;
    }

    public static String getConfigureArguments(ExecutionEnvironment ee, CompilerSet def, String configure, String flags) {
        ee = (ee != null) ? ee : ServerList.getDefaultRecord().getExecutionEnvironment();
        def = (def != null) ? def : CompilerSetManager.get(ee).getDefaultCompilerSet();
        String cCompiler = ConfigureUtils.getDefaultC(def);
        String cppCompiler = ConfigureUtils.getDefaultCpp(def);
        StringBuilder buf = new StringBuilder(flags);
        String cCompilerFlags = getCompilerFlags(def);
        String cppCompilerFlags = getCompilerFlags(def);
        if (configure.endsWith("CMakeLists.txt")){ // NOI18N
            appendIfNeed("-G ", flags, buf, "\"Unix Makefiles\""); // NOI18N
            appendIfNeed("-DCMAKE_BUILD_TYPE=", flags, buf, "Debug"); // NOI18N
            appendIfNeed("-DCMAKE_C_COMPILER=", flags, buf, cCompiler); // NOI18N
            appendIfNeed("-DCMAKE_CXX_COMPILER=", flags, buf, cppCompiler); // NOI18N
            appendIfNeed("-DCMAKE_C_FLAGS_DEBUG=", flags, buf, cCompilerFlags); // NOI18N
            appendIfNeed("-DCMAKE_CXX_FLAGS_DEBUG=", flags, buf, cppCompilerFlags); // NOI18N
            appendIfNeed("-DCMAKE_EXPORT_COMPILE_COMMANDS=", flags, buf, "ON"); // NOI18N
        } else if (configure.endsWith(".pro")){ // NOI18N
            int platform = getPlatform(ee);
            if (isSunStudio(def) && (platform == PlatformTypes.PLATFORM_SOLARIS_INTEL || platform == PlatformTypes.PLATFORM_SOLARIS_SPARC)) { // NOI18N
                appendIfNeed("-spec ", flags, buf, "solaris-cc"); // NOI18N
            }
            if (platform == PlatformTypes.PLATFORM_MACOSX) {
                buf.append("-spec macx-g++"); // NOI18N
            }
            appendIfNeed("QMAKE_CC=", flags, buf, cCompiler); // NOI18N
            appendIfNeed("QMAKE_CXX=", flags, buf, cppCompiler); // NOI18N
            appendIfNeed("QMAKE_CFLAGS=", flags, buf, cCompilerFlags); // NOI18N
            appendIfNeed("QMAKE_CXXFLAGS=", flags, buf, cppCompilerFlags); // NOI18N
        } else {
            appendIfNeed("CC=", flags, buf, cCompiler); // NOI18N
            appendIfNeed("CXX=", flags, buf, cppCompiler); // NOI18N
            appendIfNeed("CFLAGS=", flags, buf, cCompilerFlags); // NOI18N
            appendIfNeed("CXXFLAGS=", flags, buf, cppCompilerFlags); // NOI18N
        }
        return buf.toString();
    }

    private static void appendIfNeed(String key, String flags, StringBuilder buf, String flag){
        if (!flags.contains(key) ){
            if (buf.length() > 0) {
                buf.append(' '); // NOI18N
            }
            buf.append(key).append(flag);
        }
    }

    private static int getPlatform(ExecutionEnvironment ee){
        return CompilerSetManager.get(ee).getPlatform();
    }

    private static boolean isSunStudio(CompilerSet def){
        if (def != null) {
            CompilerFlavor flavor = def.getCompilerFlavor();
            if (flavor.isSunStudioCompiler()) {
                return true;
            }
        }
        return false;
    }

    private static String getDefaultC(CompilerSet def){
        String cCompiler = getToolPath(def, PredefinedToolKind.CCompiler);
        if (cCompiler != null) {
            return cCompiler;
        }
        cCompiler = "gcc"; // NOI18N
        if (def != null) {
            CompilerFlavor flavor = def.getCompilerFlavor();
            if (flavor.isSunStudioCompiler()) {
                cCompiler = "cc"; // NOI18N
            }
        }
        return cCompiler;
    }

    private static String getDefaultCpp(CompilerSet def){
        String cppCompiler = getToolPath(def, PredefinedToolKind.CCCompiler);
        if (cppCompiler != null) {
            return cppCompiler;
        }
        cppCompiler = "g++"; // NOI18N
        if (def != null) {
            CompilerFlavor flavor = def.getCompilerFlavor();
            if (flavor.isSunStudioCompiler()) {
                cppCompiler = "CC"; // NOI18N
            }
        }
        return cppCompiler;
    }

    private static String getToolPath(CompilerSet compilerSet, PredefinedToolKind tool){
        if (compilerSet == null) {
            return null;
        }
        Tool compiler = compilerSet.findTool(tool);
        if (compiler == null) {
            return null;
        }
        return escapePath(compiler.getPath());
    }

    private static String escapePath(String path) {
        path = path.replace("\\", "/"); // NOI18N
        if ((path.indexOf(' ') > 0 || path.indexOf('=') > 0)&& !path.startsWith("\"")) { // NOI18N
            path = "\""+path+"\""; // NOI18N
        }
        return path;
    }

    private static String getCompilerFlags(CompilerSet def){
        if (def != null) {
            CompilerFlavor flavor = def.getCompilerFlavor();
            if (flavor.isSunStudioCompiler()) {
                return PREDEFINED_FLAGS_SUN;
            }
        }
        return PREDEFINED_FLAGS_GNU;
    }
}
