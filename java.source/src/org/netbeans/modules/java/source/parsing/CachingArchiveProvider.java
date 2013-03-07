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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.source.parsing;



import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.java.source.usages.Pair;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;


/** Global cache for Archives (zip files and folders).
 *
 * XXX-Perf Add swapping for lower memory usage
 *
 * @author Petr Hrebejk
 */
public class CachingArchiveProvider {

    private static final String NAME_RT_JAR = "rt.jar";         //NOI18N
    private static final String PATH_CT_SYM = "lib/ct.sym";     //NOI18N
    private static final String PATH_RT_JAR_IN_CT_SYM = "META-INF/sym/rt.jar/"; //NOI18N
    private static CachingArchiveProvider instance;

    // Names to caching zip files
    // XXX-PERF Consider swapping
    HashMap<URL,Archive> archives;

    public static synchronized CachingArchiveProvider getDefault () {
        if (instance == null) {
            instance = new CachingArchiveProvider ();
        }
        return instance;
    }
        
    /** Creates a new instance CachingArchiveProvider 
     *  Can be caleed only from UnitTests or {@link CachingArchiveProvider#getDefault} !!!!!
     */
    CachingArchiveProvider() {
        archives = new HashMap<URL,Archive>();
    }
    
    /** Gets archive for given file.
     */
    public synchronized Archive getArchive( URL root, boolean cacheFile)  {                
        Archive archive = archives.get(root);
        if (archive == null) {
            archive = create(root, cacheFile);
            if (archive != null) {
                archives.put(root, archive );
            }
        }
        return archive;
    }
                        
    public synchronized void removeArchive (final URL root) {
        final Archive archive = archives.remove(root);
        if (archive != null) {
            archive.clear();
        }
    }
    
    public synchronized void clearArchive (final URL root) {
        Archive archive = archives.get(root);
        if (archive != null) {
            archive.clear();
        }
    }
        
    // Private methods ---------------------------------------------------------
    
    /** Creates proper archive for given file.
     */
    private static Archive create( URL root, boolean cacheFile ) {
        String protocol = root.getProtocol();
        if ("file".equals(protocol)) {
            File f = Utilities.toFile(URI.create(root.toExternalForm()));
            if (f.isDirectory()) {
                return new FolderArchive (f);
            }
            else {
                return null;
            }
        }
        if ("jar".equals(protocol)) {
            URL inner = FileUtil.getArchiveFile(root);
            protocol = inner.getProtocol();
            if ("file".equals(protocol)) {
                File f = Utilities.toFile(URI.create(inner.toExternalForm()));
                if (f.isFile()) {
                    final Pair<File,String> resolved = replace(f);
                    return new CachingArchive(
                            resolved.first,
                            resolved.second,
                            cacheFile);
                }
                else {
                    return null;
                }
            }
        }                
        //Slow
        FileObject fo = URLMapper.findFileObject(root);
        if (fo != null) {
            return new FileObjectArchive (fo);
        }
        else {
            return null;
        }
    }
            
    void clear() {
        archives.clear();
    }

    @NonNull
    private static Pair<File,String> replace(
        @NonNull final File file) {
        if (NAME_RT_JAR.equals(file.getName())) {
            final FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                for (JavaPlatform jp : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
                    for (FileObject jdkFolder : jp.getInstallFolders()) {
                        if (FileUtil.isParentOf(jdkFolder, fo)) {
                            final FileObject ctSym = jdkFolder.getFileObject(PATH_CT_SYM);
                            File ctSymFile;
                            if (ctSym != null && (ctSymFile = FileUtil.toFile(ctSym)) != null) {
                                return Pair.<File,String>of(ctSymFile,PATH_RT_JAR_IN_CT_SYM);
                            }
                        }
                    }
                }
            }
        }
        return Pair.<File,String>of(file, null);
    }
}
