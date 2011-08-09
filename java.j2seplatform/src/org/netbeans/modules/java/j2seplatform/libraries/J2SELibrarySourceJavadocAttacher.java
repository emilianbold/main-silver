/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2seplatform.libraries;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.java.j2seplatform.queries.SourceJavadocAttacherUtil;
import org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service=SourceJavadocAttacherImplementation.class, position=151)
public class J2SELibrarySourceJavadocAttacher implements SourceJavadocAttacherImplementation {

    @Override
    public Future<Result> attachSources(@NonNull final URL root) throws IOException {
        return attach(root, J2SELibraryTypeProvider.VOLUME_TYPE_SRC);
    }

    @Override
    public Future<Result> attachJavadoc(@NonNull final URL root) throws IOException {
        return attach(root, J2SELibraryTypeProvider.VOLUME_TYPE_JAVADOC);
    }

    private Future<Result> attach(
            @NonNull final URL root,
            final String volume) {
        final Pair<LibraryManager,Library> pair = findOwner(root);
        if (pair == null) {
            return SourceJavadocAttacherUtil.resultAsFuture(Result.UNSUPPORTED);
        }
        final Callable<Result> call = new Callable<Result>() {
            @Override
            public Result call() {
                final LibraryManager lm = pair.first;
                final Library lib = pair.second;
                assert lm != null;
                assert lib != null;
                try {
                    final URL areaLocation = lm.getLocation();
                    final File baseFolder = areaLocation == null ? null : new File(areaLocation.toURI()).getParentFile();
                    final List<? extends URI> selected;
                    if (volume == J2SELibraryTypeProvider.VOLUME_TYPE_SRC) {
                        selected = SourceJavadocAttacherUtil.selectSources(
                            root,
                            new SelectFolder(volume, lib.getName(), baseFolder),
                            new Convertor(volume, baseFolder));
                    } else if (volume == J2SELibraryTypeProvider.VOLUME_TYPE_JAVADOC) {
                        selected = SourceJavadocAttacherUtil.selectJavadoc(
                            root,
                            new SelectFolder(volume, lib.getName(), baseFolder),
                            new Convertor(volume, baseFolder));
                    } else {
                        throw new IllegalStateException();
                    }
                    if (selected != null) {
                        final String name = lib.getName();
                        final String displayName = lib.getDisplayName();
                        final String desc = lib.getDescription();
                        final Map<String,List<URI>> volumes = new HashMap<String, List<URI>>();
                        for (String currentVolume : J2SELibraryTypeProvider.VOLUME_TYPES) {
                            List<URI> content = lib.getURIContent(currentVolume);
                            if (volume == currentVolume) {
                                final List<URI> newContent = new ArrayList<URI>(content.size()+selected.size());
                                newContent.addAll(content);
                                newContent.addAll(selected);
                                content = newContent;
                            }
                            volumes.put(currentVolume,content);
                        }
                        lm.removeLibrary(lib);
                        lm.createURILibrary(
                            J2SELibraryTypeProvider.LIBRARY_TYPE,
                            name,
                            displayName,
                            desc,
                            volumes);
                        return Result.ATTACHED;
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                } catch (URISyntaxException use) {
                    Exceptions.printStackTrace(use);
                }
                return Result.CANCELED;
            }
        };
        return SourceJavadocAttacherUtil.scheduleInEDT(call);
    }

    private Pair<LibraryManager,Library> findOwner(final URL root) {
        for (LibraryManager lm : LibraryManager.getOpenManagers()) {
            for (Library l : lm.getLibraries()) {
                if (!J2SELibraryTypeProvider.LIBRARY_TYPE.equals(l.getType())) {
                    continue;
                }
                final List<URL> cp = l.getContent(J2SELibraryTypeProvider.VOLUME_TYPE_CLASSPATH);
                if (cp.contains(root)) {
                    return Pair.<LibraryManager,Library>of(lm, l);
                }
            }
        }
        return null;
    }

    private static class Pair<F,S> {
        public final F first;
        public final S second;

        private Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        public static <F,S> Pair<F,S> of(F first, S second) {
            return new Pair<F,S>(first,second);
        }
    }

    private static class SelectFolder implements Callable<List<? extends String>> {

        private final String volume;
        private final String libName;
        private final File baseFolder;

        private SelectFolder(
                @NonNull final String volume,
                @NonNull final String libName,
                @NullAllowed final File baseFolder) {
            this.volume = volume;
            this.libName = libName;
            this.baseFolder = baseFolder;
        }

        @Override
        public List<? extends String> call() throws Exception {
            final String[] paths = J2SEVolumeCustomizer.select(
                volume,
                libName,
                new File[1],
                null,
                baseFolder);
            return paths == null ? null : Arrays.<String>asList(paths);
        }
    }

    private static class Convertor implements SourceJavadocAttacherUtil.Function<String, URI> {

        private final String volume;
        private final File baseFolder;

        private Convertor(
                @NonNull final String volume,
                @NullAllowed final File baseFolder) {
            this.volume = volume;
            this.baseFolder = baseFolder;
        }

        @Override
        public URI call(String param) throws Exception {
            return J2SEVolumeCustomizer.pathToURI(baseFolder, param, volume);
        }

    }

}
