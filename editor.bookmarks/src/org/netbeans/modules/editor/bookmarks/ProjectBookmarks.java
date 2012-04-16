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
package org.netbeans.modules.editor.bookmarks;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;

/**
 * Bookmarks for a project consist of bookmarks for all URLs (where the bookmarks exist)
 * within the project.
 * 
 * @author Miloslav Metelka
 */
public final class ProjectBookmarks {
    
    private final Project project;

    private volatile int lastBookmarkId;

    private final Map<URL,FileBookmarks> url2FileBookmarks;
    
    private boolean removed;
    
    public ProjectBookmarks(Project project) {
        this(project, 0);
    }
    
    public ProjectBookmarks(Project project, int lastBookmarkId) {
        this.project = project;
        this.lastBookmarkId = lastBookmarkId;
        url2FileBookmarks = new HashMap<URL, FileBookmarks>();
    }

    public Project getProject() {
        return project;
    }
    
    public int getLastBookmarkId() {
        return lastBookmarkId;
    }

    public int generateBookmarkId() {
        return ++lastBookmarkId;
    }
    
    public void ensureBookmarkIdSkip(int bookmarkId) {
        lastBookmarkId = Math.max(lastBookmarkId, bookmarkId);
    }

    public FileBookmarks get(URL url) {
        return url2FileBookmarks.get(url);
    }
    
    public void remove(URL url) {
        url2FileBookmarks.remove(url);
    }
    
    public void add(FileBookmarks fileBookmarks) {
        url2FileBookmarks.put(fileBookmarks.getUrl(), fileBookmarks);
    }
    
    public Collection<URL> allURLs() {
        return url2FileBookmarks.keySet();
    }
    
    public boolean containsAnyBookmarks() {
        for (FileBookmarks fileBookmarks : url2FileBookmarks.values()) {
            if (fileBookmarks.containsAnyBookmarks()) {
                return true;
            }
        }
        return false;
    }

    public Collection<FileBookmarks> allFileBookmarks() {
        return url2FileBookmarks.values();
    }
    
    public List<BookmarkInfo> allBookmarks() {
       List<BookmarkInfo> allBookmarks = new ArrayList<BookmarkInfo>();
       for (FileBookmarks fileBookmarks : url2FileBookmarks.values()) {
           allBookmarks.addAll(fileBookmarks.getBookmarks());
       }
       return allBookmarks;
    }
    
    public void markRemoved() {
        removed = true;
    }

    public boolean isRemoved() {
        return removed;
    }

    @Override
    public String toString() {
        return "project=" + project + ", lastBId=" + lastBookmarkId + ", removed=" + removed; // NOI18N
    }

}
