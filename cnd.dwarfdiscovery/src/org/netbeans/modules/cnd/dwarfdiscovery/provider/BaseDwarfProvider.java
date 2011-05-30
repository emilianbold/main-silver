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
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
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
import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader.SharedLibraries;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
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

    private FileSystem getFileSystem(ProjectProxy project) {
        if (project != null) {
            Project p = project.getProject();
            if (p != null) {                
                return RemoteFileUtil.getProjectSourceFileSystem(p);
            }
        }
        return CndFileUtils.getLocalFileSystem();
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
        FileSystem fileSystem  = getFileSystem(project);
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
            boolean exist = false;
            FileObject fo = fileSystem.findResource(name);
            if (fo == null || !fo.isValid()) {
                  String fileFinder = Dwarf.fileFinder(file, name);
                  if (fileFinder != null) {
                      fo = fileSystem.findResource(fileFinder);
                      if (fo != null && fo.isValid()) {
                          if (f instanceof DwarfSource) {
                              ((DwarfSource)f).resetItemPath(fileFinder);
                              name = fileFinder;
                              exist = true;
                          }
                      }
                  }
            } else {
                exist = true;
            }

            if (exist) {
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
        Position position = null;
        List<String> errors = new ArrayList<String>();
        List<String> searchPaths = new ArrayList<String>();
        TreeMap<String,AtomicInteger> roots = new TreeMap<String,AtomicInteger>();
        int foundDebug = 0;
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
                    foundDebug++;
                    String path = cu.getSourceFileAbsolutePath();
                    path = DiscoveryUtils.normalizeAbsolutePath(path);
                    if (!CndFileUtils.isExistingFile(path)) {
                        String fileFinder = Dwarf.fileFinder(objFileName, path);
                        if (fileFinder != null) {
                            fileFinder = DiscoveryUtils.normalizeAbsolutePath(fileFinder);
                            if (!CndFileUtils.isExistingFile(fileFinder)) {
                                continue;
                            } else {
                                path = fileFinder;
                            }
                        } else {
                            continue;
                        }
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
                    path = path.replace('\\', '/');
                    {
                        int i = path.lastIndexOf('/');
                        if (i >= 0) {
                            String folder = path.substring(0, i);
                            AtomicInteger val = roots.get(folder);
                            if (val == null) {
                                val = new AtomicInteger();
                                roots.put(folder, val);
                            }
                            val.incrementAndGet();
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
                SharedLibraries pubNames = dump.readPubNames();
                synchronized (dlls) {
                    for (String dll : pubNames.getDlls()) {
                        dlls.add(dll);
                    }
                    searchPaths.addAll(pubNames.getPaths());
                }
            }
        } catch (FileNotFoundException ex) {
            errors.add(NbBundle.getMessage(BaseDwarfProvider.class, "FileNotFoundException", objFileName));  // NOI18N
            if (TRACE_READ_EXCEPTIONS) {
                System.out.println("File not found " + objFileName + ": " + ex.getMessage());  // NOI18N
            }
        } catch (WrongFileFormatException ex) {
            errors.add(NbBundle.getMessage(BaseDwarfProvider.class, "WrongFileFormatException", objFileName));  // NOI18N
            if (TRACE_READ_EXCEPTIONS) {
                System.out.println("Unsuported format of file " + objFileName + ": " + ex.getMessage());  // NOI18N
            }
        } catch (IOException ex) {
            errors.add(NbBundle.getMessage(BaseDwarfProvider.class, "IOException", objFileName, ex.toString()));  // NOI18N
            if (TRACE_READ_EXCEPTIONS) {
                System.err.println("Exception in file " + objFileName + ": " + ex.getMessage());  // NOI18N
                ex.printStackTrace(System.err);
            }
        } catch (Exception ex) {
            errors.add(NbBundle.getMessage(BaseDwarfProvider.class, "Exception", objFileName, ex.toString()));  // NOI18N
            //if (TRACE_READ_EXCEPTIONS) {
            System.err.println("Exception in file " + objFileName + ": " + ex.getMessage());  // NOI18N
            ex.printStackTrace(System.err);
            //}
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
        ArrayList<String> dllResult = null;
        if (dlls != null) {
            dllResult = new ArrayList<String>(dlls);
        }
        ArrayList<String> pathsResult = null;
        if (dlls != null) {
            pathsResult = new ArrayList<String>(searchPaths);
        }
        String commonRoot = getRoot(roots);
        if (res > 0) {
            return new ApplicableImpl(true, errors, top, res, sunStudio > res/2, dllResult, pathsResult, commonRoot, position);
        } else {
            if (errors.isEmpty()) {
                if (foundDebug > 0) {
                    errors.add(NbBundle.getMessage(BaseDwarfProvider.class, "BadDebugInformation", objFileName));  // NOI18N
                } else {
                    errors.add(NbBundle.getMessage(BaseDwarfProvider.class, "NotFoundDebugInformation", objFileName));  // NOI18N
                }
            }
            return new ApplicableImpl(false, errors, top, res, sunStudio > res/2, dllResult, pathsResult, commonRoot, position);
        }
    }

    private String getCommonPart(String path, String commonRoot) {
        String[] splitPath = path.split("/"); // NOI18N
        ArrayList<String> list1 = new ArrayList<String>();
        boolean isUnixPath = false;
        for (int i = 0; i < splitPath.length; i++) {
            if (!splitPath[i].isEmpty()) {
                list1.add(splitPath[i]);
            } else {
                if (i == 0) {
                    isUnixPath = true;
                }
            }
        }
        String[] splitRoot = commonRoot.split("/"); // NOI18N
        ArrayList<String> list2 = new ArrayList<String>();
        boolean isUnixRoot = false;
        for (int i = 0; i < splitRoot.length; i++) {
            if (!splitRoot[i].isEmpty()) {
                list2.add(splitRoot[i]);
            } else {
                if (i == 0) {
                    isUnixRoot = true;
                }
            }
        }
        if (isUnixPath != isUnixRoot) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        if (isUnixPath) {
            buf.append('/');
        }
        for (int i = 0; i < Math.min(list1.size(), list2.size()); i++) {
            if (list1.get(i).equals(list2.get(i))) {
                if (i > 0) {
                    buf.append('/');
                }
                buf.append(list1.get(i));
            } else {
                break;
            }
        }
        return buf.toString();
    }

    private String getRoot(TreeMap<String,AtomicInteger> roots) {
        ArrayList<String> res = new ArrayList<String>();
        ArrayList<AtomicInteger> resCount = new ArrayList<AtomicInteger>();
        String current = null;
        AtomicInteger currentCount = null;
        for(Map.Entry<String,AtomicInteger> entry : roots.entrySet()) {
            if (current == null) {
                current = entry.getKey();
                currentCount = new AtomicInteger(entry.getValue().get());
                continue;
            }
            String s = getCommonPart(entry.getKey(), current);
            String[] split = s.split("/"); // NOI18N
            int length = (split.length > 0 && split[0].isEmpty()) ? split.length - 1 : split.length;
            if (length >= 2) {
                current = s;
                currentCount.addAndGet(entry.getValue().get());
            } else {
                res.add(current);
                resCount.add(currentCount);
                current = entry.getKey();
                currentCount = new AtomicInteger(entry.getValue().get());
            }
        }
        if (current != null) {
            res.add(current);
            resCount.add(currentCount);
        }
        TreeMap<String,AtomicInteger> newRoots = new TreeMap<String, AtomicInteger>();
        String bestRoot = null;
        int bestCount = 0;
        for(int i = 0; i < res.size(); i++) {
            newRoots.put(res.get(i), resCount.get(i));
            if (bestRoot == null) {
                bestRoot = res.get(i);
                bestCount = resCount.get(i).get();
            } else {
                if (bestCount < resCount.get(i).get()) {
                    bestRoot = res.get(i);
                    bestCount = resCount.get(i).get();
                }
            }
        }
        return bestRoot;
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
                SharedLibraries pubNames = dump.readPubNames();
                synchronized(dlls) {
                    for(String dll : pubNames.getDlls()) {
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
                ex.printStackTrace(System.err);
            }
        } catch (Exception ex) {
            if (TRACE_READ_EXCEPTIONS) {
                System.err.println("Exception in file " + objFileName);  // NOI18N
                ex.printStackTrace(System.err);
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
        private final CompilerFlavor compileFlavor;
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
            path = DiscoveryUtils.normalizeAbsolutePath(path);
            if (Utilities.isWindows()) {
                path = path.replace('\\', '/');
            }
            return path;
        }

        public CompilerFlavor getCompileFlavor() {
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

        @Override
        public String toString() {
            return path+":"+line; //NOI18N
        }
    }
}
