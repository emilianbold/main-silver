/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.karma.coverage;

import java.io.File;
import org.netbeans.modules.web.clientproject.api.jstesting.Coverage;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

public final class CoverageWatcher {

    static final String COVERAGE_FILENAME = "clover.xml"; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(CoverageWatcher.class);

    private final Coverage coverage;
    private final File sourceDir;
    private final File coverageDir;
    private final RequestProcessor.Task processTask;
    private final FileChangeListener fileChangeListener = new FileChangeListenerImpl();

    private volatile File logFile = null;


    public CoverageWatcher(Coverage coverage, File sourceDir, File coverageDir) {
        assert coverage != null;
        assert sourceDir.isDirectory() : sourceDir;
        assert coverageDir != null;
        this.coverage = coverage;
        this.sourceDir = sourceDir;
        this.coverageDir = coverageDir;
        processTask = RP.create(new Runnable() {
            @Override
            public void run() {
                process();
            }
        });
    }

    public void start() {
        FileUtil.addRecursiveListener(fileChangeListener, coverageDir);
    }

    public void stop() {
        FileUtil.removeRecursiveListener(fileChangeListener, coverageDir);
    }

    void process() {
        assert logFile.isFile();
        if (coverage.isEnabled()) {
            new CoverageProcessor(coverage, sourceDir, logFile).process();
        }
    }

    void process(File logFile) {
        assert logFile.isFile();
        this.logFile = logFile;
        processTask.schedule(200);
    }

    //~ Inner classes

    private final class FileChangeListenerImpl extends FileChangeAdapter {

        @Override
        public void fileDataCreated(FileEvent fe) {
            processFileEvent(fe);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            processFileEvent(fe);
        }

        private void processFileEvent(FileEvent fileEvent) {
            FileObject file = fileEvent.getFile();
            if (COVERAGE_FILENAME.equals(file.getNameExt())) {
                process(FileUtil.toFile(file));
            }
        }

    }

}
