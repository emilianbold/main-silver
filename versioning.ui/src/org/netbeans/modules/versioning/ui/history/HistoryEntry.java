/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.versioning.ui.history;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.swing.Action;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;

/**
 *
 * @author tomas
 */
public class HistoryEntry {
    private final VCSHistoryProvider.HistoryEntry entry;
    private final boolean local;

    HistoryEntry(VCSHistoryProvider.HistoryEntry entry, File[] files, boolean local) {
        this.entry = entry;
        this.local = local;
    }

    public String getUsernameShort() {
        return entry.getUsernameShort();
    }

    public String getUsername() {
        return entry.getUsername();
    }

    public String getRevisionShort() {
        return entry.getRevisionShort();
    }

    public void getRevisionFile(File originalFile, File revisionFile) {
        entry.getRevisionFile(originalFile, revisionFile);
    }

    public String getRevision() {
        return entry.getRevision();
    }

    public String getMessage() {
        return entry.getMessage();
    }

    public File[] getFiles() {
        return entry.getFiles();
    }

    public Date getDateTime() {
        return entry.getDateTime();
    }

    public Action[] getActions() {
        return entry.getActions();
    }

    public void setMessage(String message) throws IOException {
        entry.setMessage(message);
    }

    public boolean canEdit() {
        return entry.canEdit();
    }
    
    public boolean isLocalHistory() {
        return local;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append("files=[");
        File[] files = getFiles();
        for (int i = 0; i < files.length; i++) {
            sb.append(files[i]);
            if(i < files.length -1) sb.append(",");
        }
        sb.append("],");
        sb.append("timestamp=");
        sb.append(getDateTime().getTime());
        sb.append(",");
        sb.append("revision=");
        sb.append(getRevision());
        sb.append(",");
        sb.append("username=");
        sb.append(getUsername());
        sb.append(",");
        sb.append("message=");
        sb.append(getMessage());
        sb.append(",");
        sb.append("canEdit=");
        sb.append(canEdit());
        sb.append("]");
        return sb.toString();
    }

    
}
