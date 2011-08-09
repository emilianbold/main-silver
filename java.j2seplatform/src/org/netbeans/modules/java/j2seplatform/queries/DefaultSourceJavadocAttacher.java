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
package org.netbeans.modules.java.j2seplatform.queries;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service=SourceJavadocAttacherImplementation.class) //position=last
public class DefaultSourceJavadocAttacher implements SourceJavadocAttacherImplementation {

    @Override
    public Future<Result> attachSources(@NonNull final URL root) throws IOException {
        return attach(root, 0);
    }

    @Override
    public Future<Result> attachJavadoc(@NonNull final URL root) throws IOException {
        return attach(root, 1);
    }

    private Future<Result> attach (@NonNull final URL root, final int mode) throws IOException {
        final Callable<Result> call = new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                final URL[] toAttach = selectRoots(root, mode);
                if (toAttach != null) {
                    switch (mode) {
                        case 0:
                            QueriesCache.getSources().updateRoot(root, toAttach);
                            break;
                        case 1:
                            QueriesCache.getJavadoc().updateRoot(root, toAttach);
                            break;
                        default:
                            throw new IllegalArgumentException(Integer.toString(mode));
                    }
                    return Result.ATTACHED;
                }
                return Result.CANCELED;
            }
        };
        return SourceJavadocAttacherUtil.scheduleInEDT(call);
    }

    @NbBundle.Messages({
        "TXT_Title=Browse ZIP/Folder",
        "TXT_Javadoc=Library Javadoc (folder, ZIP or JAR file)",
        "TXT_Sources=Library Sources (folder, ZIP or JAR file)"
    })
    private static URL[] selectRoots(final URL root, final int mode) throws MalformedURLException, FileStateInvalidException {
        final File[] cfh = new File[]{currentFolder};
        final List<? extends URI> selected;
        if (mode == 0) {
            selected = SourceJavadocAttacherUtil.selectSources(
                root,
                SourceJavadocAttacherUtil.createDefaultBrowseCall(
                    Bundle.TXT_Title(),
                    Bundle.TXT_Sources(),
                    cfh),
                SourceJavadocAttacherUtil.createDefaultURIConvertor(true));
        } else if (mode == 1) {
            selected = SourceJavadocAttacherUtil.selectJavadoc(
                root,
                SourceJavadocAttacherUtil.createDefaultBrowseCall(
                    Bundle.TXT_Title(),
                    Bundle.TXT_Javadoc(),
                    cfh),
                SourceJavadocAttacherUtil.createDefaultURIConvertor(false));
        } else {
            throw new IllegalStateException(Integer.toString(mode));
        }

        if (selected == null) {
            return null;
        }
        currentFolder = cfh[0];
        final URL[] result = new URL[selected.size()];
        for (int i=0; i< result.length; i++) {
            result[i] = selected.get(i).toURL();
        }
        return result;
    }

    private static File currentFolder;
}
