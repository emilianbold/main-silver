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

import com.sun.tools.javac.api.ClientCodeWrapper.Trusted;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.source.util.Iterators;

/**
 *
 * @author Tomas Zezula
 */
@Trusted
public final class ProxyFileManager implements JavaFileManager {

    private static final Logger LOG = Logger.getLogger(ProxyFileManager.class.getName());

    private static final Location ALL = new Location () {
        @Override
        public String getName() { return "ALL";}   //NOI18N

        @Override
        public boolean isOutputLocation() { return false; }
    };

    /**
     * Workaround to allow Filer ask for getFileForOutput for StandardLocation.SOURCE_PATH
     * which is not allowed but Filer does not allow write anyway => safe to do it.
     */
    private static final Location SOURCE_PATH_WRITE = new Location () {
        @Override
        public String getName() { return "SOURCE_PATH_WRITE"; }  //NOI18N
        @Override
        public boolean isOutputLocation() { return false;}
    };

    private final Map<Location,JavaFileManager[]> fileManagers;
    
    private final ProcessorGenerated processorGeneratedFiles;
    private final SiblingSource siblings;
    private JavaFileObject lastInfered;
    private String lastInferedResult;
    

    /** Creates a new instance of ProxyFileManager */
    public ProxyFileManager(
            @NonNull final JavaFileManager bootPath,
            @NonNull final JavaFileManager classPath,
            @NullAllowed final JavaFileManager sourcePath,
            @NullAllowed final JavaFileManager aptSources,
            @NullAllowed final JavaFileManager outputhPath,
            @NullAllowed final MemoryFileManager memoryFileManager,
            @NonNull final ProcessorGenerated processorGeneratedFiles,
            @NonNull final SiblingSource siblings) {
        assert bootPath != null;
        assert classPath != null;
        assert memoryFileManager == null || sourcePath != null;
        assert processorGeneratedFiles != null;
        assert siblings != null;
        this.fileManagers = new HashMap<Location, JavaFileManager[]>();
        this.fileManagers.put(
            StandardLocation.CLASS_PATH,
            outputhPath == null ?
                new JavaFileManager[] {classPath}:
                new JavaFileManager[] {classPath, outputhPath});
        this.fileManagers.put(
            StandardLocation.PLATFORM_CLASS_PATH,
            new JavaFileManager[] {bootPath});
        this.fileManagers.put(
            StandardLocation.SOURCE_PATH,
            sourcePath == null ?
                new JavaFileManager[0]:
                memoryFileManager == null ?
                    new JavaFileManager[] {sourcePath}:
                    new JavaFileManager[] {
                        sourcePath,
                        memoryFileManager});
        this.fileManagers.put(
            StandardLocation.CLASS_OUTPUT,
            outputhPath == null ?
                new JavaFileManager[0]:
                new JavaFileManager[] {outputhPath});
        this.fileManagers.put(
            StandardLocation.SOURCE_OUTPUT,
            aptSources == null ?
                new JavaFileManager[0]:
                new JavaFileManager[] {aptSources});
        this.fileManagers.put(
             SOURCE_PATH_WRITE,
             sourcePath == null ?
                new JavaFileManager[0]:
                new JavaFileManager[] {sourcePath}
             );

        final Map<JavaFileManager,Void> all = new IdentityHashMap<JavaFileManager, Void>();
        for (JavaFileManager[] jfmsForLoc : fileManagers.values()) {
            for (JavaFileManager jfm : jfmsForLoc) {
                all.put(jfm, null);
            }
        }        
        this.fileManagers.put(
             ALL,
             all.keySet().toArray(new JavaFileManager[all.size()]));
        this.processorGeneratedFiles = processorGeneratedFiles;
        this.siblings = siblings;
    }
    

    @Override
    @NonNull
    public Iterable<JavaFileObject> list(
            @NonNull final Location l,
            @NonNull final String packageName,
            @NonNull final Set<JavaFileObject.Kind> kinds,
            final boolean recurse) throws IOException {
        List<Iterable<JavaFileObject>> iterables = new LinkedList<Iterable<JavaFileObject>>();
        JavaFileManager[] fms = getFileManagers (l);
        for (JavaFileManager fm : fms) {
            iterables.add( fm.list(l, packageName, kinds, recurse));
        }
        final Iterable<JavaFileObject> result = Iterators.chained(iterables);
        if (LOG.isLoggable(Level.FINER)) {
            final StringBuilder urls = new StringBuilder ();
            for (JavaFileObject jfo : result ) {
                urls.append(jfo.toUri().toString());
                urls.append(", ");  //NOI18N
            }
            LOG.log(
                Level.FINER,
                "List {0} Package: {1} Kinds: {2} -> {3}", //NOI18N
                new Object[] {
                    l,
                    packageName,
                    kinds,
                    urls
                });
        }
        return result;
    }

    @Override
    @CheckForNull
    public FileObject getFileForInput(
            @NonNull final Location l,
            @NonNull final String packageName,
            @NonNull final String relativeName) throws IOException {
        JavaFileManager[] fms = getFileManagers(l);
        for (JavaFileManager fm : fms) {
            FileObject result = fm.getFileForInput(l, packageName, relativeName);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    @CheckForNull
    public FileObject getFileForOutput(
            @NonNull final Location l,
            @NonNull final String packageName,
            @NonNull final String relativeName,
            @NullAllowed final FileObject sibling)
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        JavaFileManager[] fms = getFileManagers(
                l == StandardLocation.SOURCE_PATH ?
                    SOURCE_PATH_WRITE : l);
        assert fms.length <=1;
        if (fms.length == 0) {
            return null;
        }
        else {
            return mark(fms[0].getFileForOutput(l, packageName, relativeName, sibling), l);
        }
    }

    @Override
    @CheckForNull
    public ClassLoader getClassLoader (@NonNull final Location l) {
        return null;
    }

    @Override
    public void flush() throws IOException {
        for (JavaFileManager fm : getFileManagers(ALL)) {
            fm.flush();
        }
    }

    @Override
    public void close() throws IOException {
        for (JavaFileManager fm : getFileManagers(ALL)) {
            fm.close();
        }
    }

    @Override
    public int isSupportedOption(@NonNull final String string) {
        return -1;
    }

    @Override
    public boolean handleOption (
            @NonNull final String current,
            @NonNull final Iterator<String> remains) {
        boolean isSourceElement;
        final Collection<String> defensiveCopy = copy(remains);
        if (AptSourceFileManager.ORIGIN_FILE.equals(current)) {
            final Iterator<String> it = defensiveCopy.iterator();
            if (!it.hasNext()) {
                throw new IllegalArgumentException("The apt-source-root requires folder.");    //NOI18N
            }
            final String sib = it.next();
            if(sib.length() != 0) {                
                siblings.push(asURL(sib));                
            } else {
                siblings.pop();
            }
        } else if ((isSourceElement=AptSourceFileManager.ORIGIN_SOURCE_ELEMENT_URL.equals(current)) || 
                   AptSourceFileManager.ORIGIN_RESOURCE_ELEMENT_URL.equals(current)) {
            if (!defensiveCopy.isEmpty()) {
                URL bestSoFar = siblings.getProvider().getSibling();
                for  (String surl : defensiveCopy) {
                    if (FileObjects.JAVA.equals(FileObjects.getExtension(surl))) {
                        bestSoFar = asURL(surl);
                        break;
                    }
                }
                if (LOG.isLoggable(Level.INFO) && isSourceElement && defensiveCopy.size() > 1) {
                    final StringBuilder sb = new StringBuilder();
                    for (String surl : defensiveCopy) {
                        if (sb.length() > 0) {
                            sb.append(", ");    //NOI18N
                        }
                        sb.append(surl);
                    }
                    LOG.log(
                        Level.FINE,
                        "Multiple source files passed as ORIGIN_SOURCE_ELEMENT_URL: {0}; using: {1}",  //NOI18N
                        new Object[]{
                            sb,
                            bestSoFar
                        });
                }
                siblings.push(bestSoFar);
            } else {
                siblings.pop();
            }
        }
        for (JavaFileManager m : getFileManagers(ALL)) {
            if (m.handleOption(current, defensiveCopy.iterator())) {
                return true;
            }
        }
        return false;
    }    

    @Override
    public boolean hasLocation(@NonNull final JavaFileManager.Location location) {
        return fileManagers.containsKey(location);
    }

    @Override
    @CheckForNull
    public JavaFileObject getJavaFileForInput (
            @NonNull final Location l,
            @NonNull final String className,
            @NonNull final JavaFileObject.Kind kind) throws IOException {
        JavaFileManager[] fms = getFileManagers (l);
        for (JavaFileManager fm : fms) {
            JavaFileObject result = fm.getJavaFileForInput(l,className,kind);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    @CheckForNull
    public JavaFileObject getJavaFileForOutput(
            @NonNull final Location l,
            @NonNull final String className,
            @NonNull final JavaFileObject.Kind kind,
            @NonNull final FileObject sibling)
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        JavaFileManager[] fms = getFileManagers (l);
        assert fms.length <=1;
        if (fms.length == 0) {
            return null;
        } else {
            final JavaFileObject result = fms[0].getJavaFileForOutput (l, className, kind, sibling);
            return mark (result,l);
        }
    }


    @Override
    @CheckForNull
    public String inferBinaryName(
            @NonNull final JavaFileManager.Location location,
            @NonNull final JavaFileObject javaFileObject) {
        assert javaFileObject != null;
        //If cached return it dirrectly
        if (javaFileObject == lastInfered) {
            return lastInferedResult;
        }
        String result;
        //If instanceof FileObject.Base no need to delegate it
        if (javaFileObject instanceof InferableJavaFileObject) {
            final InferableJavaFileObject ifo = (InferableJavaFileObject) javaFileObject;
            result = ifo.inferBinaryName();
            if (result != null) {
                this.lastInfered = javaFileObject;
                this.lastInferedResult = result;
                return result;
            }
        }
        //Ask delegates to infer the binary name
        JavaFileManager[] fms = getFileManagers (location);
        for (JavaFileManager fm : fms) {
            result = fm.inferBinaryName (location, javaFileObject);
            if (result != null && result.length() > 0) {
                this.lastInfered = javaFileObject;
                this.lastInferedResult = result;
                return result;
            }
        }
        return null;
    }

    @Override
    public boolean isSameFile(FileObject fileObject, FileObject fileObject0) {
        final JavaFileManager[] fms = getFileManagers(ALL);
        for (JavaFileManager fm : fms) {
            if (fm.isSameFile(fileObject, fileObject0)) {
                return true;
            }
        }
        return fileObject.toUri().equals (fileObject0.toUri());
    }

    @SuppressWarnings("unchecked")
    @NonNull
    private <T extends javax.tools.FileObject> T mark(
            @NonNull final T result,
            @NonNull final JavaFileManager.Location l) throws MalformedURLException {
        ProcessorGenerated.Type type = null;
        if (l == StandardLocation.CLASS_OUTPUT) {
            type = ProcessorGenerated.Type.RESOURCE;
        } else if (l == StandardLocation.SOURCE_OUTPUT) {
            type = ProcessorGenerated.Type.SOURCE;
        }
        final boolean hasSibling = siblings.getProvider().hasSibling();
        final boolean write = processorGeneratedFiles.canWrite() || !hasSibling;
        if (result != null && hasSibling) {
            if (type == ProcessorGenerated.Type.SOURCE) {
                processorGeneratedFiles.register(
                    siblings.getProvider().getSibling(),
                    result,
                    type);
            } else if (type == ProcessorGenerated.Type.RESOURCE) {
                try {
                    result.openInputStream().close();
                } catch (IOException ioe) {
                    //Marking only created files
                    processorGeneratedFiles.register(
                        siblings.getProvider().getSibling(),
                        result,
                        type);
                }
            }
        }
        return write ? result : (T) FileManagerTransaction.nullFileObject((InferableJavaFileObject)result);    //safe - NullFileObject subclass of both JFO and FO.
    }

    private JavaFileManager[] getFileManagers (final Location location) {
        final JavaFileManager[] result = fileManagers.get(location);
        return result != null ? result : new JavaFileManager[0];
    }    

    private static URL asURL(final String url) throws IllegalArgumentException {
        try {
            return new URL(url);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Invalid path argument: " + url, ex);    //NOI18N
        }
    }

    private static Collection<String> copy(final Iterator<String> from) {
        if (!from.hasNext()) {
            return Collections.<String>emptyList();
        } else {
            final LinkedList<String> result = new LinkedList<String>();
            while (from.hasNext()) {
                result.add(from.next());
            }
            return result;
        }
    }
}
