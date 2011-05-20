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

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.jgit.JGitRevisionInfo;
import org.netbeans.libs.git.jgit.JGitUserInfo;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class ExportCommitCommand extends GitCommand {
    private final ProgressMonitor monitor;
    private final OutputStream out;
    private final FileListener listener;
    private final String revisionStr;
    
    private static final char NL = '\n';

    public ExportCommitCommand (Repository repository, String revisionStr, OutputStream out, ProgressMonitor monitor, FileListener listener) {
        super(repository, monitor);
        this.monitor = monitor;
        this.listener = listener;
        this.out = out;
        this.revisionStr = revisionStr;
    }

    @Override
    protected void run() throws GitException {
        Repository repository = getRepository();
        String workTreePath = repository.getWorkTree().getAbsolutePath();
        RevCommit commit = Utils.findCommit(repository, revisionStr);
        if (commit.getParentCount() != 1) {
            throw new GitException("Unable to export a merge commit");
        }
        RevCommit parentCommit = commit.getParent(0);
        DiffFormatter formatter = null;
        try {
            out.write(Constants.encode(formatCommitInfo(commit)));
            formatter = new DiffFormatter(out);
            formatter.setRepository(repository);
            formatter.setDetectRenames(true);
            List<DiffEntry> diffEntries = formatter.scan(parentCommit, commit);
            for (DiffEntry ent : diffEntries) {
                if (monitor.isCanceled()) {
                    break;
                }
                listener.notifyFile(new File(workTreePath + File.separator + ent.getNewPath()), ent.getNewPath());
                formatter.format(ent);
            }
            formatter.flush();
        } catch (IOException ex) {
            throw new GitException(ex);
        } finally {
            if (formatter != null) {
                formatter.release();
            }
        }
    }

    @Override
    protected String getCommandDescription () {
        return "git format-patch --no-stat -1 " + revisionStr;
    }

    private String formatCommitInfo (RevCommit commit) {
        GitRevisionInfo info = new JGitRevisionInfo(commit, getRepository());
        StringBuilder sb = new StringBuilder();
        sb.append("From ").append(info.getRevision()).append(" ").append("Mon Sep 17 00:00:00 2001").append(NL);
        if (info.getAuthor() != null) {
            sb.append("From: ").append(info.getAuthor().toString()).append(NL);
        } else if (info.getCommitter() != null) {
            sb.append("From: ").append(info.getAuthor().toString()).append(NL);
        }
        sb.append("Date: ").append(DateFormat.getDateTimeInstance().format(new Date(info.getCommitTime()))).append(NL);
        sb.append(NL).append(info.getFullMessage()).append(NL).append(NL);
        return sb.toString();
    }
}
