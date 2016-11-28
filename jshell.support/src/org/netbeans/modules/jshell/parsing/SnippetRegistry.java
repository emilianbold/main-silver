/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.parsing;

import org.netbeans.modules.jshell.model.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.jshell.JShell;
import org.netbeans.lib.nbjshell.JShellAccessor;
import jdk.jshell.Snippet;
import org.netbeans.lib.nbjshell.SnippetWrapping;
import org.netbeans.modules.jshell.support.ShellSession;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;

/**
 * Registry of all snippets defined by JShell session. Each snippet will recreive
 * its {@link SnippetHandle} wrapper. Snippets processed by JShell are accumulated
 * and assigned to ConsoleSections. 
 * <p/>
 * Exactly one instance of SnippetRegistry lives for each {@link ShellSession}.
 * <p/>
 * Until the input is actually <b>executed</b> by JShell, no Snippet exists; for
 * inputs which have not been yet installed (registered after they are executed) a
 * transient {@link SnippetWrapping} is created. Such wrapping will get a generated
 * classname and will automatically import all preceding transient wrappings - this is
 * so that the user can enter multiple snippets and they still can refer to each other.
 * <p/>
 * Lifecycle of all handles/wrappers are tied to their respective ConsoleSection. If the
 * section is GCed, its handles should be GCed as well. For transient handles, the 
 * SnippetRegistry will erase their file (if it exists) upon Section/Handle GC.
 * 
 * @author sdedic
 */
public final class SnippetRegistry {
    private static final Logger LOG = Logger.getLogger(SnippetRegistry.class.getName());
    private static final String JSHELL_TRANSIENT_SNIPPET_CLASS = "$JShell$DOESNOTMATTER"; // NOI18N
    
    /**
     * The JShell state
     */
    private final JShell  state;
    
    /**
     * Registered snippets, and their handles. For each snippet produced by the JShell
     * will return the relevant handle which is used throughout NB support.
     */
    private final Map<Snippet, SnippetHandle> snippets = new HashMap<>();
    
    /**
     * For each ConsoleSection registers processed snippets. Does not contain snippets for active input
     * section.
     */
    private final Map<ConsoleSection, List<SnippetHandle>> sectionHandles = new WeakHashMap<>();
    
    /**
     * Transient handles
     */
    private final Map<ConsoleSection, List<SnippetHandle>> transientHandles = new WeakHashMap<>();
    
    /**
     * Monotonic counter used to generate classnames.
     */
    private final AtomicInteger counter = new AtomicInteger(0);
    
    private final Map<Reference<SnippetHandle>, FileObject> cleanupTransientFiles = new HashMap<>();
    
    private final ShellAccessBridge    shellExecutor;
    
    public SnippetRegistry(JShell state, ShellAccessBridge shellExecutor, FileObject persistentRoot, FileObject transientRoot) {
        this.state = state;
        this.persistentSnippetsRoot = persistentRoot;
        this.transientSnippetsRoot = transientRoot;
        this.shellExecutor = shellExecutor;
    }
    
    /**
     * Transient snippets are created here, on a memory fs. Transient snippets
     * are deleted after their handles are no longer used.
     */
    private FileObject  transientSnippetsRoot;
    
    /**
     * Root where sources from executed snippets are generated.
     */
    private FileObject  persistentSnippetsRoot;
    
    /**
     * Mapps snippets to the timestamps of their snippet files. Only valid snippets will
     */
    private final Map<Snippet, Long>    snippetTimeStamps = new WeakHashMap<>();

    /**
     * Installs a new snippet and binds it with a ConsoleSection.
     * 
     * @param s the new snippet processed by JShell
     * @param section ConsoleSection that produced the snippet
     * @param sectionOffset snippet's offset within section
     * @return offset <b>after</b> snippet's text.
     */
    public SnippetHandle installSnippet(Snippet s, ConsoleSection section, int sectionOffset, boolean nontransient) {
        SnippetWrapping wrap = wrap(s); //JShellAccessor.snippetWrap(state, s);
        SnippetHandle handle;
        synchronized (this) {
            if (section != null) {
                List<SnippetHandle> sectionSnippets = sectionHandles.get(section);
                if (sectionSnippets == null) {
                    sectionSnippets = new ArrayList<>(1);
                } else {
                    sectionSnippets = new ArrayList<>(sectionSnippets);
                }
                Rng[] fragments = null;
                int l = sectionSnippets.size();

                // in section which has been parsed, as is now being executed, replace the snippets
                // according to the real 
                int start = sectionOffset;
                int end = sectionOffset + wrap.getSource().length();

                int so = section.offsetFromContents(start);
                int eo = section.offsetFromContents(end);
                fragments = section.computeFragments(new Rng(so, eo));
                handle = ModelAccessor.INSTANCE.createHandle(
                        this,
                        section,
                        fragments, wrap, false);
                LOG.log(Level.FINER, "Section: {0}, installed snippet {1}, classname {2}", new Object[] { section, s, handle.getClassName() });
                sectionSnippets.add(handle);
                sectionHandles.put(section, sectionSnippets);
            } else {
                handle = ModelAccessor.INSTANCE.createHandle(
                        this, null, null, wrap, !nontransient);
            }
            if (wrap.getSnippet() != null) {
                snippets.put(wrap.getSnippet(), handle);
            }
            clearTransientSnippets();
            return handle;
        }
    }
    
    private SnippetWrapping wrap(Snippet s) {
        try {
            return shellExecutor.execute(new Callable<SnippetWrapping>() {
                @Override
                public SnippetWrapping call() throws Exception {
                    return JShellAccessor.snippetWrap(state, s);
                }
            });
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    private SnippetWrapping wrap(String input) {
        try {
            return shellExecutor.execute(new Callable<SnippetWrapping>() {
                @Override
                public SnippetWrapping call() throws Exception {
                    return JShellAccessor.wrapInput(state, input);
                }
            });
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    private synchronized void clearTransientSnippets() {
        transientHandles.clear();
    }
    
    public synchronized SnippetHandle getHandle(Snippet snip) {
        return snippets.get(snip);
    }
    
    public synchronized List<SnippetHandle> getSectionSnippets(ConsoleSection s) {
        List<SnippetHandle> snips;
        snips = transientHandles.get(s);
        if (snips != null) {
            return snips;
        }
        snips = sectionHandles.get(s);
        if (snips == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(snips);
        }
    }
    
    /**
     * Creates a snippet file. If the `editedSnippetIndex' is not >= 0, it generates
     * a file NOT seen by repository updater; if the edited section contains multiple
     * snippets, then it is possible to give the snippet explicit name.
     * 
     * @param snippet
     * @param editedSnippetIndex
     * @return 
     */
    public FileObject   snippetFile(SnippetHandle snippet, int editedSnippetIndex) {
//        String resName = snippetFileName(snippet, editedSnippetIndex);
//        FileObject fob = workRoot.getFileObject(resName);
//        if (fob != null && fob.isValid()) {
//            return fob;
//        }
        return createSnippetFile(snippet, null);
    }
    
    private String snippetFileName(SnippetHandle snippet) {
        return snippet.getClassName() + ".java"; 
    }
    
    class SWR extends WeakReference<SnippetHandle> implements Runnable {
        public SWR(SnippetHandle referent) {
            super(referent, BaseUtilities.activeReferenceQueue());
        }

        @Override
        public void run() {
            delete(this);
        }
    }
    
    private void delete(Reference<SnippetHandle> ref) {
        FileObject f;
        synchronized (this) {
            f = cleanupTransientFiles.remove(ref);
            if (f == null) {
                return;
            }
        }
        try {
            f.delete();
        } catch (IOException ex) {
            Exceptions.printStackTrace(Exceptions.attachSeverity(ex, Level.INFO));
        }
    }
    
    private FileObject finalize(SnippetHandle h, FileObject f) {
       synchronized (this) {
           cleanupTransientFiles.put(new SWR(h), f);
       }
       return f;
    }
    
    private FileObject createSnippetFile(SnippetHandle info, String resName) {
        boolean transientFile = info.isTransient();
        FileObject pkg;
        try {
            pkg = FileUtil.createFolder(transientFile ? 
                    transientSnippetsRoot  : persistentSnippetsRoot, "REPL");
        } catch (IOException ex) {
            // this is quite unexpected
            Exceptions.printStackTrace(ex);
            return null;
        }
        String fn = resName != null ? resName : snippetFileName(info); 
        String contents = info.getWrappedCode();
        
        if (contents == null) {
            return null;
        }
        Snippet snip = info.getSnippet();
        Long l = null;
        if (snip != null) {
            synchronized (this) {
                l = snippetTimeStamps.get(snip);
            }
        }
        
        int retries = 0;
        IOException lastException = null;
        while (retries++ < 10) {
            FileObject fob = pkg.getFileObject(fn);
            if (fob != null) {
                if (l != null && l == fob.lastModified().getTime()) {
                    return fob;
                }
                try {
                    fob.delete();
                } catch (IOException ex1) {
                    lastException = ex1;
                }
            }
            try (OutputStream ostm = pkg.createAndOpen(fn)) {
                try (OutputStreamWriter ows = new OutputStreamWriter(ostm, "UTF-8")) {
                    ows.append(contents);
                    ows.flush();
                }
                FileObject ret = pkg.getFileObject(fn);
                synchronized (this) {
                    snippetTimeStamps.put(snip, ret.lastModified().getTime());
                }
                ModelAccessor.INSTANCE.setFile(info, ret);
                return finalize(info, ret);
            } catch (IOException ex) {
                // perhaps the file is being created in another thread ?
                lastException = ex;
            }
        }
        if (lastException != null) {
            Exceptions.printStackTrace(lastException);
        }
        return null;
    }
    
    public List<SnippetHandle> createSnippets(ConsoleSection section, CharSequence snapshot, boolean extendToEnd) {
        List<SnippetHandle> snips; 
        synchronized (this) {
            snips = sectionHandles.get(section);
            if (snips == null) {
                snips = transientHandles.get(section);
                if (snips != null) {
                    return snips;
                }
                snips = Collections.emptyList();
            }
        }
        Rng[] ranges = section.getAllSnippetBounds();
        if (ranges.length == snips.size()) {
            return snips;
        }
        List<SnippetHandle> result = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < ranges.length; i++) {
            Rng curRange = ranges[i];
            int end = curRange.end;
            if (extendToEnd && i == ranges.length - 1) {
                end = snapshot.length();
                curRange = new Rng(curRange.start, end);
            }
            if (curRange.len() == 0) {
                continue;
            }
            Rng[] fragments;
            
            if (index < snips.size()) {
                SnippetHandle h = snips.get(index);
                fragments = h.getFragments();
                if (fragments[0].start == curRange.start) {
                    // it's that snippet, accept the handle:
                    result.add(h);
                    index++;
                    continue;
                }
            }
            // create a transient snippet wrapping
            String text;
            
            try {
                text = section.getRangeContents(snapshot, curRange);
            } catch (StringIndexOutOfBoundsException ex) {
                ex.printStackTrace();
                return Collections.emptyList();
            }
            if (text == null || text.isEmpty()) {
                continue;
            }
            SnippetWrapping shellWrapping = wrap(text);//JShellAccessor.wrapInput(state, text);
            if (shellWrapping == null) {
                continue;
            }
            SnippetWrapping transWrapping = new TransientWrapping(shellWrapping, result);

            int so = section.offsetFromContents(curRange.start - section.getPartBegin());
            int eo = section.offsetFromContents(end - section.getPartBegin());
            fragments = section.computeFragments(new Rng(so, eo));
            result.add(
                ModelAccessor.INSTANCE.createHandle(
                    this, section, fragments, transWrapping, true
                )
            );
        }
        synchronized (this) {
            Object check = sectionHandles.get(section);
            if (check == null || check == snips) {
                transientHandles.put(section, result);
            }
        }
        return result;
    }
    
    private String generateClassName() {
        int id = counter.incrementAndGet();
        StringBuilder sb = new StringBuilder();
        sb.append(JSHELL_TRANSIENT_SNIPPET_CLASS.substring(0,8));
        String stringId = Integer.toString(id, Character.MAX_RADIX);
        int pad = JSHELL_TRANSIENT_SNIPPET_CLASS.length() - (sb.length() + stringId.length());
        for (int i = pad; i > 0; i--) {
            sb.append("_");
        }
        sb.append(stringId);
        return sb.toString();
    }
    
    public class TransientWrapping implements SnippetWrapping {
        private final SnippetWrapping original;
        private final List<SnippetHandle>   precedings;
        private String cachedCode;
        private int importsLen;
        private String className;

        public TransientWrapping(SnippetWrapping original, List<SnippetHandle> precedings) {
            this.original = original;
            this.precedings = new ArrayList<>(precedings);
            //              "$JShell$DOESNOTMATTER
            this.className = generateClassName();
        }

        @Override
        public Snippet.Kind getSnippetKind() {
            return original.getSnippetKind();
        }

        @Override
        public Snippet.Status getStatus() {
            return original.getStatus();
        }

        @Override
        public Snippet getSnippet() {
            return original.getSnippet();
        }

        @Override
        public String getCode() {
            synchronized (this) {
                if (cachedCode != null) {
                    return cachedCode;
                }
            }
            StringBuilder addImports = new StringBuilder();
            
            for (SnippetHandle h : precedings) {
                switch (h.getKind()) {
                    case IMPORT:
                        addImports.append(h.getSource().trim());
                        break;
                    default:
                        break;
                }
            }
            synchronized (this) {
                String c = original.getCode();
                int indexOfClass = c.indexOf("class " + JSHELL_TRANSIENT_SNIPPET_CLASS); // NOI18N
                if (indexOfClass != -1) {
                    this.cachedCode = (c.substring(0, indexOfClass) + addImports.toString() + c.substring(indexOfClass)).replace(JSHELL_TRANSIENT_SNIPPET_CLASS, className);
                    this.importsLen = addImports.length();
                } else {
                    this.cachedCode = c;
                }
            }
            return cachedCode;
        }

        @Override
        public String getSource() {
            return original.getSource();
        }

        @Override
        public int getWrappedPosition(int pos) {
            getCode();
            int orig = original.getWrappedPosition(pos); 
            return orig + importsLen;
        }

        @Override
        public String getClassName() {
            return className;
        }
        
        public String toString() {
            return "Transient(" + original + ")";
        }
    }
}
