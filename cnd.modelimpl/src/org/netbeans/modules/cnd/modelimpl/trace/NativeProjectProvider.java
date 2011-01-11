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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.api.project.NativeFileSearch;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeExitStatus;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * 
 * @author vv159170
 */
public final class NativeProjectProvider {
    
    /** Creates a new instance of NativeProjectProvider */
    private NativeProjectProvider() {
    }
    
    public static NativeProject createProject(String projectRoot, List<File> files,
	    List<String> sysIncludes, List<String> usrIncludes,
	    List<String> sysMacros, List<String> usrMacros, boolean pathsRelCurFile) {
	
        NativeProjectImpl project = new NativeProjectImpl(projectRoot, 
		sysIncludes, usrIncludes, sysMacros, usrMacros, pathsRelCurFile);
	
	project.addFiles(files);
	
        return project;
    }
    
    public static void fireAllFilesChanged(NativeProject nativeProject) {
	if( nativeProject instanceof NativeProjectImpl) {
	    ((NativeProjectImpl) nativeProject).fireAllFilesChanged();
	}
    }
    
    public static void setUserMacros(NativeProject nativeProject, List<String> usrMacros) {
	if( nativeProject instanceof NativeProjectImpl) {
	    ((NativeProjectImpl) nativeProject).usrMacros.clear();
            ((NativeProjectImpl) nativeProject).usrMacros.addAll(usrMacros);
	}
    }

    // XXX:FileObject conversion: remove
    public static NativeFileItem.Language getLanguage(File file, DataObject dobj) {
        CndUtils.assertNotNull(file, "null file"); //NOI18N
        FileObject fo = null;
        if (dobj != null) {
            fo = dobj.getPrimaryFile();
        }
        String mimeType = "";
        if (fo != null) {
            mimeType = MIMESupport.getFileMIMEType(fo);
        } else {
            mimeType = MIMESupport.getFileMIMEType(file);
        }
        return getLanguage(mimeType);
    }

    public static NativeFileItem.Language getLanguage(FileObject fo, DataObject dobj) {
        CndUtils.assertNotNull(fo, "null file object"); //NOI18N
        String mimeType = MIMESupport.getFileMIMEType(fo);
        return getLanguage(mimeType);
    }

    private static NativeFileItem.Language getLanguage(String mimeType) {
        if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType)) {
            return NativeFileItem.Language.CPP;
        } else if (MIMENames.C_MIME_TYPE.equals(mimeType)) {
            return NativeFileItem.Language.C;
        } else if (MIMENames.FORTRAN_MIME_TYPE.equals(mimeType)) {
            return NativeFileItem.Language.FORTRAN;
        } else if (MIMENames.HEADER_MIME_TYPE.equals(mimeType)) {
            return NativeFileItem.Language.C_HEADER;
        }
        return NativeFileItem.Language.OTHER;
    }

    public static DataObject getDataObject(FileObject fo) {
        DataObject dobj = null;
        if (fo != null) {
            try {
                dobj = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                // skip;
            }
        }
        return dobj;
    }

    // XXX:FileObject conversion: remove
    public static DataObject getDataObject(File file) {
        CndUtils.assertNormalized(file);
        return getDataObject(CndFileUtils.toFileObject(file));
    }
    
    public static final class NativeProjectImpl implements NativeProject {
	
	private final List<String> sysIncludes;
	private final List<String> usrIncludes;
	private final List<String> sysMacros;
	private final List<String> usrMacros;
	    
        private final List<NativeFileItem> files  = new ArrayList<NativeFileItem>();
	
        private final String projectRoot;
	private boolean pathsRelCurFile;
	
	private List<NativeProjectItemsListener> listeners = new ArrayList<NativeProjectItemsListener>();

        private static final class Lock {}
        private final Object listenersLock = new Lock();
	
	public NativeProjectImpl(String projectRoot,
		List<String> sysIncludes, List<String> usrIncludes, 
		List<String> sysMacros, List<String> usrMacros,
		boolean pathsRelCurFile) {

	    this.projectRoot = projectRoot;
	    this.pathsRelCurFile = pathsRelCurFile;
	    
	    this.sysIncludes = createIncludes(sysIncludes);
	    this.usrIncludes = createIncludes(usrIncludes);
	    this.sysMacros = new ArrayList<String>(sysMacros);
	    this.usrMacros = new ArrayList<String>(usrMacros);
	}
	
	private List<String> createIncludes(List<String> src) {
	    if( pathsRelCurFile ) {
		return new ArrayList<String>(src);
	    }
	    else {
		List<String> result = new ArrayList<String>(src.size());
		for( String path : src ) {
		    File file = new File(path);
		    result.add(file.getAbsolutePath());
		}
		return result;
	    }
	}
	
	private void addFiles(List<File> files) {
	    for( File file : files ) {
		addFile(file.getAbsoluteFile());
	    }
	}
	
        @Override
        public Object getProject() {
            return null;
        }

        @Override
        public FileSystem getFileSystem() {
            return CndFileUtils.getLocalFileSystem();
        }
        
        @Override
        public List<String> getSourceRoots() {
            return Collections.<String>emptyList();
        }
                
        @Override
        public String getProjectRoot() {
            return this.projectRoot;
        }

        @Override
        public String getProjectDisplayName() {
            return "DummyProject"; // NOI18N
        }

        @Override
        public List<NativeFileItem> getAllFiles() {
            return Collections.unmodifiableList(files);
        }

        @Override
        public NativeFileSearch getNativeFileSearch() {
            return new NativeFileSearch() {
                @Override
                public Collection<CharSequence> searchFile(NativeProject project, String fileName) {
                    return Collections.<CharSequence>emptyList();
                }
            };
        }

        @Override
        public void addProjectItemsListener(NativeProjectItemsListener listener) {
            synchronized( listenersLock ) {
		listeners.add(listener);
	    }
        }

        @Override
        public void removeProjectItemsListener(NativeProjectItemsListener listener) {
            synchronized( listenersLock ) {
		listeners.remove(listener);
	    }
        }

	public void fireFileChanged(File file) {
            NativeFileItem item = findFileItem(file);
	    List<NativeProjectItemsListener> listenersCopy;
	    synchronized( listenersLock ) {
		listenersCopy = new ArrayList<NativeProjectItemsListener>(listeners);
	    }
	    for( NativeProjectItemsListener listener : listenersCopy ) {
		listener.filePropertiesChanged(item);
	    }
        }

        public void fireFileAdded(File file) {
            NativeFileItem item = findFileItem(file);
            if (item == null) {
                item = addFile(file);
            }
	    List<NativeProjectItemsListener> listenersCopy;
	    synchronized( listenersLock ) {
		listenersCopy = new ArrayList<NativeProjectItemsListener>(listeners);
	    }
	    for( NativeProjectItemsListener listener : listenersCopy ) {
		listener.fileAdded(item);
	    }
        }

	private void fireAllFilesChanged() {
	    List<NativeProjectItemsListener> listenersCopy;
	    synchronized( listenersLock ) {
		listenersCopy = new ArrayList<NativeProjectItemsListener>(listeners);
	    }
	    List<NativeFileItem> items = Collections.unmodifiableList(files);
	    for( NativeProjectItemsListener listener : listenersCopy ) {
		listener.filesPropertiesChanged(items);
	    }
	}

        @Override
        public NativeFileItem findFileItem(FileObject fileObject) {
            return findFileItem(fileObject.getPath());
        }

        @Override
        public NativeFileItem findFileItem(File file) {
            return findFileItem(file.getAbsolutePath());
        }

        private NativeFileItem findFileItem(String path) {
            for (NativeFileItem item : files) {
                if (item.getAbsolutePath().equalsIgnoreCase(path)) {
                    return item;
                }
            }
            return null;
        }

        @Override
        public List<String> getSystemIncludePaths() {
            return this.sysIncludes;
        }

        @Override
        public List<String> getUserIncludePaths() {
            return this.usrIncludes;
        }

        @Override
        public List<String> getSystemMacroDefinitions() {
            return this.sysMacros;
        }

        @Override
        public List<String> getUserMacroDefinitions() {
            return this.usrMacros;
        }
        
	private NativeFileItem addFile(File file) {
            file = CndFileUtils.normalizeFile(file);
            DataObject dobj = getDataObject(file);
	    NativeFileItem.Language lang = getLanguage(file, dobj);
	    NativeFileItem item = new NativeFileItemImpl(file, this, lang);
	    //TODO: put item in loockup of DataObject
            // registerItemInDataObject(dobj, item);
	    this.files.add(item);
            return item;
	}
	
        @Override
        public List<NativeProject> getDependences() {
            return Collections.<NativeProject>emptyList();
        }

        @Override
        public void runOnProjectReadiness(NamedRunnable task) {
            task.run();
        }

        @Override
	public NativeExitStatus execute(String executable, String[] env, String... args) {
	    return null;
        }

        @Override
        public String getPlatformName() {
            return null;
        }
    }    
        
    /*package*/ static void registerItemInDataObject(DataObject obj, NativeFileItem item) {
        if (obj != null) {
            NativeFileItemSet set = obj.getLookup().lookup(NativeFileItemSet.class);
            if (set != null) {
                set.add(item);
            }
        }
    }
    
    private static final class NativeFileItemImpl implements NativeFileItem {
	
        private final File file;
        private final NativeProjectImpl project;
        private final NativeFileItem.Language lang;

        public NativeFileItemImpl(File file, NativeProjectImpl project, NativeFileItem.Language language) {
	    
            this.project = project;
            this.file = CndFileUtils.normalizeFile(file);
            this.lang = language;
        }
        
        @Override
        public NativeProject getNativeProject() {
            return project;
        }

        @Override
        public File getFile() {
            return file;
        }

        @Override
        public FileObject getFileObject() {
            return CndFileUtils.toFileObject(file); // XXX:FileObject conversion
        }
        
        @Override
        public String getAbsolutePath() {
            return file.getAbsolutePath();
        }

        @Override
        public String getName() {
            return file.getName();
        }

        @Override
        public List<String> getSystemIncludePaths() {
	    List<String> result = project.getSystemIncludePaths();
	    return project.pathsRelCurFile ? toAbsolute(result) : result;
        }

        @Override
        public List<String> getUserIncludePaths() {
	    List<String> result = project.getUserIncludePaths();
            return project.pathsRelCurFile ? toAbsolute(result) : result;
        }
	
	private List<String> toAbsolute(List<String> orig) {
	    File base = file.getParentFile();
	    List<String> result = new ArrayList<String>(orig.size());
	    for( String path : orig ) {
		File pathFile = new File(path);
		if( pathFile.isAbsolute() ) {
		    result.add(path);
		}
		else {
		    pathFile = new File(base, path);
		    result.add(pathFile.getAbsolutePath());
		}
	    }
	    return result;
	}

        @Override
        public List<String> getSystemMacroDefinitions() {
            return project.getSystemMacroDefinitions();
        }

        @Override
        public List<String> getUserMacroDefinitions() {
            return project.getUserMacroDefinitions();
        }

        @Override
        public NativeFileItem.Language getLanguage() {
            return lang;
        }

        @Override
        public NativeFileItem.LanguageFlavor getLanguageFlavor() {
            return NativeFileItem.LanguageFlavor.UNKNOWN;
        }

        @Override
        public boolean isExcluded() {
            return false;
        }

        @Override
        public String toString() {
            return file.getAbsolutePath();
        }

    }
}
