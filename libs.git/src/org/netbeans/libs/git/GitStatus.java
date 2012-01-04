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

package org.netbeans.libs.git;

import java.io.File;
import org.eclipse.jgit.diff.DiffEntry;

/**
 *
 * @author ondra
 */
public final class GitStatus {

    private final File file;
    private final String relativePath;
    private final boolean tracked;
    private GitConflictDescriptor conflictDescriptor;
    private Status statusHeadIndex;
    private Status statusIndexWC;
    private Status statusHeadWC;
    private boolean isFolder;
    private DiffEntry diffEntry;
    private final String workTreePath;

    public enum Status {
        STATUS_ADDED, STATUS_REMOVED, STATUS_NORMAL, STATUS_MODIFIED, STATUS_IGNORED
    }

    public GitStatus (String workTreePath, File file, String relativePath, boolean tracked) {
        this.workTreePath = workTreePath;
        this.file = file;
        this.relativePath = relativePath;
        this.tracked = tracked;
    }
    
    public File getFile () {
        return file;
    }

    public String getRelativePath () {
        return relativePath;
    }

    public Status getStatusHeadIndex () {
        return statusHeadIndex;
    }

    public Status getStatusIndexWC () {
        return statusIndexWC;
    }

    public Status getStatusHeadWC () {
        return statusHeadWC;
    }

    public boolean isTracked () {
        return tracked;
    }

    public boolean isConflict () {
        return conflictDescriptor != null;
    }

    public boolean isFolder () {
        return isFolder;
    }

    public boolean isCopied () {
        return diffEntry != null && diffEntry.getChangeType().equals(DiffEntry.ChangeType.COPY);
    }

    public boolean isRenamed () {
        return diffEntry != null && diffEntry.getChangeType().equals(DiffEntry.ChangeType.RENAME);
    }

    public File getOldPath () {
        if (isRenamed() || isCopied()) {
            return new File(workTreePath + File.separator + diffEntry.getOldPath());
        } else {
            return null;
        }
    }

    public GitConflictDescriptor getConflictDescriptor () {
        return conflictDescriptor;
    }

    void setDiffEntry (DiffEntry diffEntry) {
        this.diffEntry = diffEntry;
    }

    void setConflictDescriptor (GitConflictDescriptor conflictDescriptor) {
        this.conflictDescriptor = conflictDescriptor;
    }

    void setFolder (boolean isFolder) {
        this.isFolder = isFolder;
    }

    void setStatusHeadIndex (Status statusHeadIndex) {
        this.statusHeadIndex = statusHeadIndex;
    }

    void setStatusHeadWC (Status statusHeadWC) {
        this.statusHeadWC = statusHeadWC;
    }

    void setStatusIndexWC (Status statusIndexWC) {
        this.statusIndexWC = statusIndexWC;
    }
}
