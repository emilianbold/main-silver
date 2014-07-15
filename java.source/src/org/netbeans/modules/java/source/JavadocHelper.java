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
package org.netbeans.modules.java.source;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import java.awt.EventQueue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.ChangedCharSetException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.parsing.CachingArchiveProvider;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.Convertors;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Union2;

/**
 * Utilities to assist with retrieval of Javadoc text.
 */
public class JavadocHelper {

    private static final Logger LOG = Logger.getLogger(JavadocHelper.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(JavadocHelper.class.getName(),1);

    /**
     * Remote Javadoc handling policy.
     * @since 0.138
     */
    public enum RemoteJavadocPolicy {
        /**
         * The connection to remote Javadoc is verified and when valid it's returned.
         */
        USE,
        /**
         * The connection to remote Javadoc is ignored.
         */
        IGNORE,
        /**
         * The {@link RemoteJavadocException} is thrown in case of remote Javadoc.
         */
        EXCEPTION,
        /**
         * All possible connections to remote Javadoc are returned without verification.
         */
        SPECULATIVE
    }

    /**
     * A RemoteJavadocException is thrown in case of remote Javadoc with {@link RemoteJavadocPolicy#EXCEPTION} policy.
     * @since 0.138
     */
    public static final class RemoteJavadocException extends Exception {
        private final URL root;

        /**
         * Creates a new RemoteJavadocException.
         * @param root the remote Javadoc root
         */
        public RemoteJavadocException(@NullAllowed URL root) {
            this.root = root;
        }

        /**
         * Returns the remote Javadoc root.
         * @return the root
         */
        @CheckForNull
        public URL getRoot() {
            return root;
        }
    }

    private JavadocHelper() {}
    
    /**
     * A reopenable stream of text from a particular location.
     * You <em>must</em> either call {@link #close}, or call {@link #openStream}
     * (and {@linkplain InputStream#close close} it) at least once.
     */
    public static final class TextStream {
        private final List<? extends URL> urls;
        private final AtomicReference<InputStream> stream = new AtomicReference<InputStream>();
        private byte[] cache;
        /**
         * Creates a text stream from a given URL with no preopened stream.
         * @param url a URL
         */
        public TextStream(@NonNull final URL url) {
            Parameters.notNull("url", url); //NOI18N
            this.urls = Collections.singletonList(url);
        }

        TextStream(@NonNull final Collection<? extends URL> urls) {
            Parameters.notNull("urls", urls);   //NOI18N            
            final List<URL> tmpUrls = new ArrayList<>(urls.size());
            for (URL u : urls) {
                Parameters.notNull("urls[]", u);  //NOI18N
                tmpUrls.add(u);
            }
            if (tmpUrls.isEmpty()) {
                throw new IllegalArgumentException("At least one URL has to be given.");    //NOI18N
            }
            this.urls = Collections.unmodifiableList(tmpUrls);
        }

        TextStream(@NonNull final Collection<? extends URL> urls, InputStream stream) {
            this(urls);
            this.stream.set(stream);
        }
        /**
         * Location of the text.
         * @return its (possibly network) location
         */
        @CheckForNull
        public URL getLocation() {
            return urls.iterator().next();
        }

        @NonNull
        public List<? extends URL> getLocations() {
            return urls;
        }
        /**
         * Close any preopened stream without reading it.
         */
        public void close() {
            final InputStream is = stream.getAndSet(null);
            if (is != null) {
                try {
                    is.close();
                } catch (IOException x) {
                    LOG.log(Level.INFO, null, x);
                }
            }
        }
        /**
         * Open a stream.
         * (Might have already been opened but not read, in which case the preexisting stream is used.)
         * @return a stream, which you are obliged to close
         * @throws IOException if there is a problem reopening the stream
         */
        public synchronized InputStream openStream() throws IOException {
            if (cache != null) {
                LOG.log(Level.FINE, "loaded cached content for {0}", getLocation());
                return new ByteArrayInputStream(cache);
            }
            assert !isRemote() || !EventQueue.isDispatchThread();
            InputStream uncached = stream.getAndSet(null);
            if (uncached == null) {
                uncached = JavadocHelper.openStream(getLocation());
            }
            if (isRemote()) {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(20 * 1024); // typical size for Javadoc page?
                    FileUtil.copy(uncached, baos);
                    cache = baos.toByteArray();
                } finally {
                    uncached.close();
                }
                LOG.log(Level.FINE, "cached content for {0} ({1}k)", new Object[] {getLocation(), cache.length / 1024});
                return new ByteArrayInputStream(cache);
            } else {
                return uncached;
            }
        }
        /**
         * @return true if this looks to be a web location
         */
        public boolean isRemote() {
            return JavadocHelper.isRemote(getLocation());
        }
    }

    private static boolean isRemote(URL url) {
        return url.getProtocol().startsWith("http") || url.getProtocol().startsWith("ftp"); // NOI18N
    }
    
    /**
     * Like {@link URL#openStream} but uses the platform's user JAR cache ({@code ArchiveURLMapper}) when available.
     * @param url a url to open
     * @return its input stream
     * @throws IOException for the usual reasons
     */
    public static InputStream openStream(URL url) throws IOException {
        if (url.getProtocol().equals("jar")) { // NOI18N
            FileObject f = URLMapper.findFileObject(url);
            if (f != null) {
                return f.getInputStream();
            }
        }
        if (isRemote(url)) {
            LOG.log(Level.FINE, "opening network stream: {0}", url);
        }
        return url.openStream();
    }
    
    private static final Map<Element,TextStream> cachedJavadoc = new WeakHashMap<Element,TextStream>();
    
    /**
     * Richer version of {@link SourceUtils#getJavadoc}.
     * Finds {@link URL} of a javadoc page for given element when available. This method
     * uses {@link JavadocForBinaryQuery} to find the javadoc page for the give element.
     * For {@link PackageElement} it returns the package-summary.html for given package.
     * @param element to find the Javadoc for
     * @param cancel a Callable to signal cancel request
     * @return the javadoc page or null when the javadoc is not available.
     */
    public static TextStream getJavadoc(Element element, final @NullAllowed Callable<Boolean> cancel) {
        return getJavadoc(element, true, cancel);
    }

    /**
     * Richer version of {@link SourceUtils#getJavadoc}.
     * Finds {@link URL} of a javadoc page for given element when available. This method
     * uses {@link JavadocForBinaryQuery} to find the javadoc page for the give element.
     * For {@link PackageElement} it returns the package-summary.html for given package.
     * @param element to find the Javadoc for
     * @param allowRemoteJavadoc true if non-local javadoc sources should be enabled
     * @param cancel a Callable to signal cancel request
     * @return the javadoc page or null when the javadoc is not available.
     */
    public static TextStream getJavadoc(Element element, boolean allowRemoteJavadoc, final @NullAllowed Callable<Boolean> cancel) {
        try {
            final List<TextStream> res = getJavadoc(
                element,
                allowRemoteJavadoc ? RemoteJavadocPolicy.USE : RemoteJavadocPolicy.IGNORE,
                cancel);
            return res.isEmpty() ?
                null :
                res.get(0);
        } catch (RemoteJavadocException rje) {
            throw new IllegalStateException(
                "Never thrown", //NOI18N
                rje);
        }
    }

    /**
     * Returns Javadoc for given {@link Element}.
     * Finds {@link URL} of a javadoc page for given element when available. This method
     * uses {@link JavadocForBinaryQuery} to find the javadoc page for the give element.
     * For {@link PackageElement} it returns the package-summary.html for given package.
     * @param element to find the Javadoc for
     * @param remoteJavadocPolicy the remote javadoc hanlding policy
     * @param cancel a Callable to signal cancel request
     * @return the javadoc pages
     * @throws JavadocHelper.RemoteJavadocException in case of remote Javadoc and {@link RemoteJavadocPolicy#EXCEPTION} policy
     * @since 0.138
     */
    @NonNull
    public static List<TextStream> getJavadoc(
        @NonNull final Element element,
        @NonNull final RemoteJavadocPolicy remoteJavadocPolicy,
        @NullAllowed final Callable<Boolean> cancel) throws RemoteJavadocException {
        Parameters.notNull("element", element); //NOI18N
        Parameters.notNull("remoteJavadocPolicy", remoteJavadocPolicy); //NOI18N
        synchronized (cachedJavadoc) {
            final TextStream cached = cachedJavadoc.get(element);
            if (cached != null) {
                LOG.log(Level.FINE, "cache hit on {0}", cached.getLocation());
                return Collections.singletonList(cached);
            }
        }
        final List<TextStream> result = doGetJavadoc(element, remoteJavadocPolicy, cancel);
        if (result.size() == 1) {
            synchronized (cachedJavadoc) {
                cachedJavadoc.put(element, result.get(0));
            }
        }
        return result;
    }

    /**
     * Richer version of {@link SourceUtils#getJavadoc}.
     * Finds {@link URL} of a javadoc page for given element when available. This method
     * uses {@link JavadocForBinaryQuery} to find the javadoc page for the give element.
     * For {@link PackageElement} it returns the package-summary.html for given package.
     * @param element to find the Javadoc for
     * @return the javadoc page or null when the javadoc is not available.
     */
    public static TextStream getJavadoc(Element element) {
        return getJavadoc(element, null);
    }

    /**
     * Returns the charset from given {@link ChangedCharSetException}
     * @param e the {@link ChangedCharSetException}
     * @return the charset or null
     */
    @CheckForNull
    public static String getCharSet(ChangedCharSetException e) {
        String spec = e.getCharSetSpec();
        if (e.keyEqualsCharSet()) {
            //charsetspec contains only charset
            return spec;
        }

        //charsetspec is in form "text/html; charset=UTF-8"

        int index = spec.indexOf(";"); // NOI18N
        if (index != -1) {
            spec = spec.substring(index + 1);
        }

        spec = spec.toLowerCase();

        StringTokenizer st = new StringTokenizer(spec, " \t=", true); //NOI18N
        boolean foundCharSet = false;
        boolean foundEquals = false;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals(" ") || token.equals("\t")) { //NOI18N
                continue;
            }
            if (foundCharSet == false && foundEquals == false
                    && token.equals("charset")) { //NOI18N
                foundCharSet = true;
                continue;
            } else if (foundEquals == false && token.equals("=")) {//NOI18N
                foundEquals = true;
                continue;
            } else if (foundEquals == true && foundCharSet == true) {
                return token;
            }

            foundCharSet = false;
            foundEquals = false;
        }

        return null;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(value="DMI_COLLECTION_OF_URLS", justification="URLs have never host part")
    private static List<TextStream> doGetJavadoc(final Element element, final RemoteJavadocPolicy remoteJavadocPolicy, final Callable<Boolean> cancel) throws RemoteJavadocException {
        if (element == null) {
            throw new IllegalArgumentException("Cannot pass null as an argument of the SourceUtils.getJavadoc"); // NOI18N
        }
        ClassSymbol clsSym = null;
        String pkgName;
        String pageName;
        boolean buildFragment = false;
        if (element.getKind() == ElementKind.PACKAGE) {
            List<? extends Element> els = element.getEnclosedElements();
            for (Element e : els) {
                if (e.getKind().isClass() || e.getKind().isInterface()) {
                    clsSym = (ClassSymbol) e;
                    break;
                }
            }
            if (clsSym == null) {
                return Collections.emptyList();
            }
            pkgName = FileObjects.convertPackage2Folder(((PackageElement) element).getQualifiedName().toString());
            pageName = PACKAGE_SUMMARY;
        } else {
            Element e = element;
            StringBuilder sb = new StringBuilder();
            while (e.getKind() != ElementKind.PACKAGE) {
                if (e.getKind().isClass() || e.getKind().isInterface()) {
                    if (sb.length() > 0) {
                        sb.insert(0, '.');
                    }
                    sb.insert(0, e.getSimpleName());
                    if (clsSym == null) {
                        clsSym = (ClassSymbol) e;
                    }
                }
                e = e.getEnclosingElement();
            }
            if (clsSym == null) {
                return Collections.emptyList();
            }
            pkgName = FileObjects.convertPackage2Folder(((PackageElement) e).getQualifiedName().toString());
            pageName = sb.toString();
            buildFragment = element != clsSym;
        }

        if (clsSym.completer != null) {
            clsSym.complete();
        }
        if (clsSym.classfile != null) {
            try {
                final URL classFile = clsSym.classfile.toUri().toURL();
                final String pkgNameF = pkgName;
                final String pageNameF = pageName;
                final Collection<? extends CharSequence> fragment = buildFragment ? getFragment(element) : Collections.<CharSequence>emptySet();
                if (cancel == null) {
                    return findJavadoc(classFile, pkgName, pageNameF, fragment, remoteJavadocPolicy);
                }
                final Future<Union2<List<TextStream>,RemoteJavadocException>> future = RP.submit(new Callable<Union2<List<TextStream>,RemoteJavadocException>>() {
                    @Override
                    public Union2<List<TextStream>,RemoteJavadocException> call() throws Exception {
                        try {
                            return Union2.<List<TextStream>,RemoteJavadocException>createFirst(
                                findJavadoc(classFile, pkgNameF, pageNameF, fragment, remoteJavadocPolicy));
                        } catch (RemoteJavadocException rje) {
                            return Union2.<List<TextStream>,RemoteJavadocException>createSecond(rje);
                        }
                    }
                });
                do {
                    if (cancel != null && cancel.call()) {
                        future.cancel(false);
                        break;
                    }
                    try {
                        final Union2<List<TextStream>,RemoteJavadocException> res = future.get(100, TimeUnit.MILLISECONDS);
                        if (res == null) {
                            break;
                        }
                        if (res.hasFirst()) {
                            return res.first();
                        }
                        assert res.hasSecond();
                        throw res.second();
                    } catch (TimeoutException timeOut) {
                        //Retry
                    }
                } while (true);
            } catch (RemoteJavadocException rje) {
                throw rje;
            }catch (Exception e) {
                LOG.log(Level.INFO, null, e);
            }
        }
        return Collections.emptyList();
    }

    private static final String PACKAGE_SUMMARY = "package-summary"; // NOI18N

    private static List<TextStream> findJavadoc(
            @NonNull final URL classFile,
            @NonNull final String pkgName,
            @NonNull final String pageName,
            @NonNull final Collection<? extends CharSequence> fragment,
            @NonNull final RemoteJavadocPolicy remoteJavadocPolicy) throws RemoteJavadocException {
        final List<TextStream> resList = new ArrayList<>();
        URL sourceRoot = null;
        Set<URL> binaries = new HashSet<URL>();
        try {
            FileObject fo = URLMapper.findFileObject(classFile);
            StringTokenizer tk = new StringTokenizer(pkgName, "/"); // NOI18N
            for (int i = 0; fo != null && i <= tk.countTokens(); i++) {
                fo = fo.getParent();
            }
            if (fo != null) {
                final URL url = CachingArchiveProvider.getDefault().mapCtSymToJar(fo.toURL());
                sourceRoot = JavaIndex.getSourceRootForClassFolder(url);
                if (sourceRoot == null) {
                    binaries.add(url);
                } else {
                    // sourceRoot may be a class root in reality
                    binaries.add(sourceRoot);
                }
            }
            if (sourceRoot != null) {
                FileObject sourceFo = URLMapper.findFileObject(sourceRoot);
                if (sourceFo != null) {
                    ClassPath exec = ClassPath.getClassPath(sourceFo, ClassPath.EXECUTE);
                    ClassPath compile = ClassPath.getClassPath(sourceFo, ClassPath.COMPILE);
                    ClassPath source = ClassPath.getClassPath(sourceFo, ClassPath.SOURCE);
                    if (exec == null) {
                        exec = compile;
                        compile = null;
                    }
                    if (exec != null && source != null) {
                        Set<URL> roots = new HashSet<URL>();
                        for (ClassPath.Entry e : exec.entries()) {
                            roots.add(e.getURL());
                        }
                        if (compile != null) {
                            for (ClassPath.Entry e : compile.entries()) {
                                roots.remove(e.getURL());
                            }
                        }
                        List<FileObject> sourceRoots = Arrays.asList(source.getRoots());
                        out:
                        for (URL e : roots) {
                            FileObject[] res = SourceForBinaryQuery.findSourceRoots(e).getRoots();
                            for (FileObject r : res) {
                                if (sourceRoots.contains(r)) {
                                    binaries.add(e);
                                    continue out;
                                }
                            }
                        }
                    }
                }
            }
binRoots:   for (URL binary : binaries) {
                JavadocForBinaryQuery.Result javadocResult = JavadocForBinaryQuery.findJavadoc(binary);
                URL[] result = javadocResult.getRoots();
                for (URL root : result) {
                    if (!root.toExternalForm().endsWith("/")) { // NOI18N
                        LOG.log(Level.WARNING, "JavadocForBinaryQuery.Result: {0} returned non-folder URL: {1}, ignoring",
                                new Object[] {javadocResult.getClass(), root.toExternalForm()});
                        continue;
                    }
                    boolean isRemote = isRemote(root);
                    boolean speculative = false;
                    if (isRemote) {
                        switch (remoteJavadocPolicy) {
                            case EXCEPTION:
                                throw new RemoteJavadocException(root);
                            case IGNORE:
                                continue;
                            case USE:
                                break;
                            case SPECULATIVE:
                                speculative = true;
                                break;
                            default:
                                throw new IllegalArgumentException(remoteJavadocPolicy.name());
                        }
                    }
                    URL url = new URL(root, pkgName + "/" + pageName + ".html");
                    InputStream is = null;
                    String rootS = root.toString();
                    boolean useKnownGoodRoots = result.length == 1 && isRemote;
                    if (useKnownGoodRoots && knownGoodRoots.contains(rootS)) {
                        LOG.log(Level.FINE, "assumed valid Javadoc stream at {0}", url);
                    } else if (!speculative || !isRemote) {
                        try {
                            is = openStream(url);
                            if (useKnownGoodRoots) {
                                knownGoodRoots.add(rootS);
                                LOG.log(Level.FINE, "found valid Javadoc stream at {0}", url);
                            }
                        } catch (IOException x) {
                            LOG.log(Level.FINE, "invalid Javadoc stream at {0}: {1}", new Object[] {url, x});
                            continue;
                        }
                    }
                    if (!fragment.isEmpty()) {
                        try {
                            // Javadoc fragments may contain chars that must be escaped to comply with RFC 2396.
                            // Unfortunately URLEncoder escapes almost everything but
                            // spaces replaces with '+' char which is wrong so it is
                            // replaced with "%20"escape sequence here.                            
                            final Collection<URL> urls = new ArrayList<>(fragment.size());
                            for (CharSequence f : fragment) {
                                final String encodedfragment = URLEncoder.encode(f.toString(), "UTF-8").  // NOI18N
                                    replace("+", "%20"); // NOI18N
                                urls.add(new URI(url.toExternalForm() + '#' + encodedfragment).toURL());
                            }
                            resList.add(new TextStream(urls, is));
                            if (!speculative) {
                                break binRoots;
                            }
                        } catch (URISyntaxException x) {
                            LOG.log(Level.INFO, null, x);
                        } catch (UnsupportedEncodingException x) {
                            LOG.log(Level.INFO, null, x);
                        } catch (MalformedURLException x) {
                            LOG.log(Level.INFO, null, x);
                        }
                    }
                    resList.add(new TextStream(Collections.<URL>singleton(url), is));
                    if (!speculative) {
                        break binRoots;
                    }
                }
            }

        } catch (MalformedURLException x) {
            LOG.log(Level.INFO, null, x);
        }
        return resList;
    }
    
    /**
     * {@code ElementJavadoc} currently will check every class in an API set if you keep on using code completion.
     * We do not want to make a new network connection each time, especially if src.zip supplies the Javadoc anyway.
     * Assume that if one class can be found, they all can.
     */
    private static final Set<String> knownGoodRoots = Collections.synchronizedSet(new HashSet<String>());

    @NonNull
    private static Collection<? extends CharSequence> getFragment(Element e) {
        final FragmentBuilder fb = new FragmentBuilder();
        if (!e.getKind().isClass() && !e.getKind().isInterface()) {
            if (e.getKind() == ElementKind.CONSTRUCTOR) {
                fb.append(e.getEnclosingElement().getSimpleName());
            } else {
                fb.append(e.getSimpleName());
            }
            if (e.getKind() == ElementKind.METHOD || e.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement ee = (ExecutableElement) e;
                fb.append("("); //NOI18N
                for (Iterator<? extends VariableElement> it = ee.getParameters().iterator(); it.hasNext();) {
                    VariableElement param = it.next();
                    appendType(fb, param.asType(), ee.isVarArgs() && !it.hasNext());
                    if (it.hasNext()) {
                        fb.append(", ");    //NOI18N
                    }
                }
                fb.append(")"); //NOI18N
            }
        }
        return fb.getFragments();
    }
    
    private static void appendType(FragmentBuilder fb, TypeMirror type, boolean varArg) {
        switch (type.getKind()) {
        case ARRAY:
            appendType(fb, ((ArrayType) type).getComponentType(), false);
            fb.append(varArg ? "..." : "[]"); // NOI18N
            break;
        case DECLARED:
            fb.append(((TypeElement) ((DeclaredType) type).asElement()).getQualifiedName());
            break;
        default:
            fb.append(type.toString());
        }
    }

    private static final class FragmentBuilder {
        private static final List<Convertor<CharSequence,CharSequence>> FILTERS;
        static  {
            final List<Convertor<CharSequence,CharSequence>> tmp = new ArrayList<>();
            tmp.add(Convertors.<CharSequence>identity());
            tmp.add(new JDoc8025633());
            FILTERS = Collections.unmodifiableList(tmp);
        };
        private final StringBuilder[] sbs;

        FragmentBuilder() {
            this.sbs = new StringBuilder[FILTERS.size()];
            for (int i = 0; i < sbs.length; i++) {
                sbs[i] = new StringBuilder();
            }
        }

        @NonNull
        FragmentBuilder append(@NonNull final CharSequence text) {
            for (int i = 0; i < sbs.length; i++) {
                sbs[i].append(FILTERS.get(i).convert(text));
            }
            return this;
        }

        @NonNull
        Collection<? extends CharSequence> getFragments() {
            final Collection<CharSequence> res = new ArrayList<>(sbs.length);
            for (StringBuilder sb : sbs) {
                res.add(sb.toString());
            }
            return Collections.unmodifiableCollection(res);
        }

        private static final class JDoc8025633 implements Convertor<CharSequence,CharSequence> {
            @Override
            @NonNull
            @SuppressWarnings("fallthrough")
            public CharSequence convert(@NonNull final CharSequence text) {
                final StringBuilder sb = new StringBuilder();
                for (int i = 0; i < text.length(); i++) {
                    final char c = text.charAt(i);
                    switch (c) {
                        case '(':    //NOI18N
                        case ')':    //NOI18N
                        case '<':    //NOI18N
                        case '>':    //NOI18N
                        case ',':    //NOI18N
                            sb.append('-');    //NOI18N
                            break;
                        case ' ':    //NOI18N
                        case '[':    //NOI18N
                            //NOP
                            break;
                        case ']':    //NOI18N
                            sb.append(":A");    //NOI18N
                            break;
                        case '$':   //NOI18N
                            if (i == 0) {
                                sb.append("Z:Z");   //NOI18N
                            }
                            sb.append(":D");        //NOI18N
                            break;
                        case '_':   //NOI18N
                            if (i == 0) {
                                sb.append("Z:Z");   //NOI18N
                            }
                        default:
                            sb.append(c);
                    }
                }
                return sb.toString();
            }
        }
    }

}
