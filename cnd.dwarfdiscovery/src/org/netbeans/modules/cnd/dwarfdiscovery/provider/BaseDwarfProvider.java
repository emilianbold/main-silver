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

package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.cnd.discovery.api.ApplicableImpl;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface.Position;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.LANG;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public abstract class BaseDwarfProvider implements DiscoveryProvider {
    
    private static final boolean TRACE_READ_EXCEPTIONS = Boolean.getBoolean("cnd.dwarfdiscovery.trace.read.errors"); // NOI18N
    private static final boolean FULL_TRACE = Boolean.getBoolean("cnd.dwarfdiscovery.trace.read.source"); // NOI18N
    public static final String RESTRICT_SOURCE_ROOT = "restrict_source_root"; // NOI18N
    public static final String RESTRICT_COMPILE_ROOT = "restrict_compile_root"; // NOI18N
    protected AtomicBoolean isStoped = new AtomicBoolean(false);
    
    public BaseDwarfProvider() {
    }
    
    @Override
    public boolean isApplicable(ProjectProxy project) {
        return true;
    }
    
    @Override
    public void stop() {
        isStoped.set(true);
    }

    protected List<SourceFileProperties> getSourceFileProperties(String[] objFileName, Progress progress, ProjectProxy project, Set<String> dlls){
        CountDownLatch countDownLatch = new CountDownLatch(objFileName.length);
        RequestProcessor rp = new RequestProcessor("Parallel analyzing", CndUtils.getNumberCndWorkerThreads()); // NOI18N
        try{
            Map<String,SourceFileProperties> map = new ConcurrentHashMap<String,SourceFileProperties>();
            for (String file : objFileName) {
                MyRunnable r = new MyRunnable(countDownLatch, file, map, progress, project, dlls);
                rp.post(r);
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            List<SourceFileProperties> list = new ArrayList<SourceFileProperties>();
            list.addAll(map.values());
            return list;
        } finally {
            PathCache.dispose();
            grepBase.clear();
            grepBase = new ConcurrentHashMap<String, GrepEntry>();
            getCommpilerSettings().dispose();
        }
    }

    private boolean processObjectFile(String file, Map<String, SourceFileProperties> map, Progress progress, ProjectProxy project, Set<String> dlls) {
        if (isStoped.get()) {
            return true;
        }
        String restrictSourceRoot = null;
        ProviderProperty p = getProperty(RESTRICT_SOURCE_ROOT);
        if (p != null) {
            String s = (String) p.getValue();
            if (s.length() > 0) {
                restrictSourceRoot = CndFileUtils.normalizeFile(new File(s)).getAbsolutePath();
            }
        }
        String restrictCompileRoot = null;
        p = getProperty(RESTRICT_COMPILE_ROOT);
        if (p != null) {
            String s = (String) p.getValue();
            if (s.length() > 0) {
                restrictCompileRoot = CndFileUtils.normalizeFile(new File(s)).getAbsolutePath();
            }
        }
        for (SourceFileProperties f : getSourceFileProperties(file, map, project, dlls)) {
            if (isStoped.get()) {
                break;
            }
            String name = f.getItemPath();
            if (name == null) {
                continue;
            }
            if (restrictSourceRoot != null) {
                if (!name.startsWith(restrictSourceRoot)) {
                    continue;
                }
            }
            if (restrictCompileRoot != null) {
                if (f.getCompilePath() != null && !f.getCompilePath().startsWith(restrictCompileRoot)) {
                    continue;
                }
            }
            if (new File(name).exists()) {
                SourceFileProperties existed = map.get(name);
                if (existed == null) {
                    map.put(name, f);
                } else {
                    // Duplicated
                    if (existed.getUserInludePaths().size() < f.getUserInludePaths().size()) {
                        map.put(name, f);
                    }
                }
            } else {
                if (FULL_TRACE) {
                    System.out.println("Not Exist " + name); // NOI18N
                } //NOI18N
            }
        }
        if (progress != null) {
            synchronized(progress) {
                progress.increment(file);
            }
        }
        return false;
    }
    
    protected ApplicableImpl sizeComilationUnit(String objFileName, Set<String> dlls){
        int res = 0;
        int sunStudio = 0;
        Dwarf dump = null;
        String commonRoot = null;
        Position position = null;
        Map<String, AtomicInteger> compilers = new HashMap<String, AtomicInteger>();
        try{
            dump = new Dwarf(objFileName);
            Iterator<CompilationUnit> iterator = dump.iteratorCompilationUnits();
            while (iterator.hasNext()) {
                CompilationUnit cu = iterator.next();
                if (cu != null) {
                    if (cu.getRoot() == null || cu.getSourceFileName() == null) {
                        continue;
                    }
                    String lang = cu.getSourceLanguage();
                    if (lang == null) {
                        continue;
                    }
                    String path = cu.getSourceFileAbsolutePath();
                    File normalizeFile = CndFileUtils.normalizeFile(new File(path));
                    if (!normalizeFile.exists()) {
                        continue;
                    }
                    ItemProperties.LanguageKind language = null;
                    if (LANG.DW_LANG_C.toString().equals(lang) ||
                            LANG.DW_LANG_C89.toString().equals(lang) ||
                            LANG.DW_LANG_C99.toString().equals(lang)) {
                        language = ItemProperties.LanguageKind.C;
                        res++;
                    } else if (LANG.DW_LANG_C_plus_plus.toString().equals(lang)) {
                        language = ItemProperties.LanguageKind.CPP;
                        res++;
                    } else if (LANG.DW_LANG_Fortran77.toString().equals(lang) ||
                           LANG.DW_LANG_Fortran90.toString().equals(lang) ||
                           LANG.DW_LANG_Fortran95.toString().equals(lang)) {
                        language = ItemProperties.LanguageKind.Fortran;
                        res++;
                    } else {
                        continue;
                    }
                    path = normalizeFile.getAbsolutePath().replace('\\', '/');
                    if (commonRoot == null) {
                        int i = path.lastIndexOf('/');
                        if (i >= 0) {
                            commonRoot = path.substring(0, i+1);
                        }
                    } else {
                        if (!path.startsWith(commonRoot)) {
                            while(true) {
                                int i = commonRoot.lastIndexOf('/');
                                if (i < 0) {
                                    break;
                                }
                                commonRoot = commonRoot.substring(0, i);
                                i = commonRoot.lastIndexOf('/');
                                if (i < 0) {
                                    break;
                                }
                                commonRoot = commonRoot.substring(0, i+1);
                                if (path.startsWith(commonRoot)) {
                                    break;
                                }
                            }
                        }
                    }
                    String compilerName = DwarfSource.extractCompilerName(cu, language);
                    if (compilerName != null) {
                        AtomicInteger count = compilers.get(compilerName);
                        if (count == null) {
                            count = new AtomicInteger();
                            compilers.put(compilerName, count);
                        }
                        count.incrementAndGet();
                    }
                    if (DwarfSource.isSunStudioCompiler(cu)) {
                        sunStudio++;
                    }
                    if (position == null) {
                        List<DwarfEntry> topLevelEntries = cu.getTopLevelEntries();
                        for(DwarfEntry entry : topLevelEntries) {
                            if (entry.getKind() == TAG.DW_TAG_subprogram) {
                                if ("main".equals(entry.getName())) { // NOI18N
                                    if (entry.isExternal()) {
                                        //VIS visibility = entry.getVisibility();
                                        //if (visibility == VIS.DW_VIS_exported) {
                                            position = new MyPosition(path, entry.getLine());
                                        //}
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (dlls != null) {
                List<String> pubNames = dump.readPubNames();
                synchronized (dlls) {
                    for (String dll : pubNames) {
                        dlls.add(dll);
                    }

                }
            }
        } catch (FileNotFoundException ex) {
            // Skip Exception
        } catch (WrongFileFormatException ex) {
            // Skip Exception
        } catch (IOException ex) {
            // Skip Exception
        } catch (Exception ex) {
            // Skip Exception
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
        int max = 0;
        String top = "";
        for(Map.Entry<String, AtomicInteger> entry : compilers.entrySet()){
            if (entry.getValue().get() > max) {
                max = entry.getValue().get();
                top = entry.getKey();
            }
        }
        if (dlls != null) {
            return new ApplicableImpl(res > 0, top, res, sunStudio > res/2, new ArrayList<String>(dlls), commonRoot, position);
        } else {
            return new ApplicableImpl(res > 0, top, res, sunStudio > res/2, null, commonRoot, position);
        }
    }
    
    protected List<SourceFileProperties> getSourceFileProperties(String objFileName, Map<String, SourceFileProperties> map, ProjectProxy project, Set<String> dlls) {
        List<SourceFileProperties> list = new ArrayList<SourceFileProperties>();
        Dwarf dump = null;
        try {
            if (FULL_TRACE) {
                System.out.println("Process file " + objFileName);  // NOI18N
            }
            dump = new Dwarf(objFileName);
            Iterator<CompilationUnit> iterator = dump.iteratorCompilationUnits();
            while (iterator.hasNext()) {
                CompilationUnit cu = iterator.next();
                if (cu != null) {
                    if (isStoped.get()) {
                        break;
                    }
                    if (cu.getRoot() == null || cu.getSourceFileName() == null) {
                        if (TRACE_READ_EXCEPTIONS) {
                            System.out.println("Compilation unit has broken name in file " + objFileName);  // NOI18N
                        }
                        continue;
                    }
                    String lang = cu.getSourceLanguage();
                    if (lang == null) {
                        if (TRACE_READ_EXCEPTIONS) {
                            System.out.println("Compilation unit has unresolved language in file " + objFileName + "for " + cu.getSourceFileName());  // NOI18N
                        }
                        continue;
                    }
                    DwarfSource source = null;
                    if (LANG.DW_LANG_C.toString().equals(lang)) {
                        source = new DwarfSource(cu, ItemProperties.LanguageKind.C, ItemProperties.LanguageStandard.C, getCommpilerSettings(), grepBase);
                    } else if (LANG.DW_LANG_C89.toString().equals(lang)) {
                        source = new DwarfSource(cu, ItemProperties.LanguageKind.C, ItemProperties.LanguageStandard.C89, getCommpilerSettings(), grepBase);
                    } else if (LANG.DW_LANG_C99.toString().equals(lang)) {
                        source = new DwarfSource(cu, ItemProperties.LanguageKind.C, ItemProperties.LanguageStandard.C99, getCommpilerSettings(), grepBase);
                    } else if (LANG.DW_LANG_C_plus_plus.toString().equals(lang)) {
                        source = new DwarfSource(cu, ItemProperties.LanguageKind.CPP, ItemProperties.LanguageStandard.CPP, getCommpilerSettings(), grepBase);
                    } else if (LANG.DW_LANG_Fortran77.toString().equals(lang)) {
                        source = new DwarfSource(cu, ItemProperties.LanguageKind.Fortran, ItemProperties.LanguageStandard.F77, getCommpilerSettings(), grepBase);
                    } else if (LANG.DW_LANG_Fortran90.toString().equals(lang)) {
                        source = new DwarfSource(cu, ItemProperties.LanguageKind.Fortran, ItemProperties.LanguageStandard.F90, getCommpilerSettings(), grepBase);
                    } else if (LANG.DW_LANG_Fortran95.toString().equals(lang)) {
                        source = new DwarfSource(cu, ItemProperties.LanguageKind.Fortran, ItemProperties.LanguageStandard.F95, getCommpilerSettings(), grepBase);
                    } else {
                        if (FULL_TRACE) {
                            System.out.println("Unknown language: " + lang);  // NOI18N
                        }
                        // Ignore other languages
                    }
                    if (source != null) {
                        if (source.getCompilePath() == null) {
                            if (TRACE_READ_EXCEPTIONS) {
                                System.out.println("Compilation unit has NULL compile path in file " + objFileName);  // NOI18N
                            }
                            continue;
                        }
                        String name = source.getItemPath();
                        SourceFileProperties old = map.get(name);
                        if (old != null && old.getUserInludePaths().size() > 0) {
                            if (FULL_TRACE) {
                                System.out.println("Compilation unit already exist. Skip " + name);  // NOI18N
                            }
                            // do not process processed item
                            continue;
                        }
                        source.process(cu);
                        list.add(source);
                    }
                }
            }
            if (dlls != null) {
                List<String> pubNames = dump.readPubNames();
                synchronized(dlls) {
                    for(String dll : pubNames) {
                        dlls.add(dll);
                    }

                }
            }
        } catch (FileNotFoundException ex) {
            // Skip Exception
            if (TRACE_READ_EXCEPTIONS) {
                System.out.println("File not found " + objFileName + ": " + ex.getMessage());  // NOI18N
            }
        } catch (WrongFileFormatException ex) {
            if (TRACE_READ_EXCEPTIONS) {
                System.out.println("Unsuported format of file " + objFileName + ": " + ex.getMessage());  // NOI18N
            }
        } catch (IOException ex) {
            if (TRACE_READ_EXCEPTIONS) {
                System.err.println("Exception in file " + objFileName);  // NOI18N
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            if (TRACE_READ_EXCEPTIONS) {
                System.err.println("Exception in file " + objFileName);  // NOI18N
                ex.printStackTrace();
            }
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
        return list;
    }

    private Map<String,GrepEntry> grepBase = new ConcurrentHashMap<String, GrepEntry>();
    
    public CompilerSettings getCommpilerSettings(){
        return myCommpilerSettings;
    }
    
    public void setCommpilerSettings(ProjectProxy project) {
        myCommpilerSettings = new CompilerSettings(project);
    }
    private CompilerSettings myCommpilerSettings;

    public static class GrepEntry {
        ArrayList<String> includes = new ArrayList<String>();
        String firstMacro = null;
        int firstMacroLine = -1;
    }

    public static class CompilerSettings{
        private final List<String> systemIncludePathsC;
        private final List<String> systemIncludePathsCpp;
        private final Map<String,String> systemMacroDefinitionsC;
        private final Map<String,String> systemMacroDefinitionsCpp;
        private Map<String,String> normalizedPaths = new ConcurrentHashMap<String, String>();
        private final String compileFlavor;
        private final String cygwinDriveDirectory;
        private final boolean isWindows;
        
        public CompilerSettings(ProjectProxy project){
            systemIncludePathsCpp = DiscoveryUtils.getSystemIncludePaths(project, true);
            systemIncludePathsC = DiscoveryUtils.getSystemIncludePaths(project,false);
            systemMacroDefinitionsCpp = DiscoveryUtils.getSystemMacroDefinitions(project, true);
            systemMacroDefinitionsC = DiscoveryUtils.getSystemMacroDefinitions(project,false);
            compileFlavor = DiscoveryUtils.getCompilerFlavor(project);
            isWindows = Utilities.isWindows();
            if (isWindows) {
                cygwinDriveDirectory = DiscoveryUtils.getCygwinDrive(project);
            } else {
                cygwinDriveDirectory = null;
            }
        }
        
        public List<String> getSystemIncludePaths(ItemProperties.LanguageKind lang) {
            if (lang == ItemProperties.LanguageKind.CPP) {
                return systemIncludePathsCpp;
            } else if (lang == ItemProperties.LanguageKind.C) {
                return systemIncludePathsC;
            }
            return Collections.<String>emptyList();
        }
        
        public Map<String,String> getSystemMacroDefinitions(ItemProperties.LanguageKind lang) {
            if (lang == ItemProperties.LanguageKind.CPP) {
                return systemMacroDefinitionsCpp;
            } else if (lang == ItemProperties.LanguageKind.C) {
                return systemMacroDefinitionsC;
            }
            return Collections.<String,String>emptyMap();
        }
        
        public String getNormalizedPath(String path){
            String res = normalizedPaths.get(path);
            if (res == null) {
                res = PathCache.getString(normalizePath(path));
                normalizedPaths.put(PathCache.getString(path),res);
            }
            return res;
        }

        protected String normalizePath(String path){
            path = CndFileUtils.normalizeFile(new File(path)).getAbsolutePath();
            if (Utilities.isWindows()) {
                path = path.replace('\\', '/');
            }
            return path;
        }

        public String getCompileFlavor() {
            return compileFlavor;
        }

        public String getCygwinDrive() {
            return cygwinDriveDirectory;
        }

        public boolean isWindows(){
            return isWindows;
        }

        private void dispose(){
            systemIncludePathsC.clear();
            systemIncludePathsCpp.clear();
            systemMacroDefinitionsC.clear();
            systemMacroDefinitionsCpp.clear();
            normalizedPaths.clear();
            normalizedPaths = new ConcurrentHashMap<String, String>();
        }
    }

    private class MyRunnable implements Runnable {
        private String file;
        private Map<String, SourceFileProperties> map;
        private Progress progress;
        private CountDownLatch countDownLatch;
        private ProjectProxy project;
        private Set<String> dlls;

        private MyRunnable(CountDownLatch countDownLatch, String file, Map<String, SourceFileProperties> map, Progress progress, ProjectProxy project, Set<String> dlls){
            this.file = file;
            this.map = map;
            this.progress = progress;
            this.countDownLatch = countDownLatch;
            this.project = project;
            this.dlls = dlls;
        }
        @Override
        public void run() {
            try {
                if (!isStoped.get()) {
                    Thread.currentThread().setName("Parallel analyzing "+file); // NOI18N
                    processObjectFile(file, map, progress, project, dlls);
                }
            } finally {
                countDownLatch.countDown();
            }
        }
    }
    private static class MyPosition implements Position {
        private final String path;
        private final int line;

        private MyPosition(String path, int line){
            this.path = path;
            this.line = line;
        }

        @Override
        public String getFilePath() {
            return path;
        }

        @Override
        public int getLine() {
            return line;
        }
    }
}
