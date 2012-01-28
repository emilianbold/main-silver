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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.mercurial.ui.log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.util.HgCommand;

/**
 *
 * @author jr140578
 */
public class HgLogMessage {
    public static char HgModStatus = 'M';
    public static char HgAddStatus = 'A';
    public static char HgDelStatus = 'D';
    public static char HgCopyStatus = 'C';
    public static char HgRenameStatus = 'R';

    private final List<HgLogMessageChangedPath> paths;
    private final List<HgLogMessageChangedPath> dummyPaths;
    private HgRevision rev;
    private String author;
    private String username;
    private String desc;
    private Date date;
    private String timeZoneOffset;

    private HgRevision parentOneRev;
    private HgRevision parentTwoRev;
    private boolean bMerged;
    private String rootURL;
    private OutputLogger logger;
    private HashMap<File, HgRevision> ancestors = new HashMap<File, HgRevision>();
    private final String[] branches;
    private final String[] tags;

    private void updatePaths(List<String> pathsStrings, String path, char status) {
        paths.add(new HgLogMessageChangedPath(path, null, status));
        if (pathsStrings != null){
            pathsStrings.add(path);
        }
    }

    public HgLogMessage(String rootURL, List<String> filesShortPaths, String rev, String auth, String username, String desc, String date, String id,
            String parents, String fm, String fa, String fd, String fc, String branches, String tags) {

        this.rootURL = rootURL;
        this.rev = new HgRevision(id, rev);
        this.author = auth;
        this.username = username;
        this.desc = desc;
        this.date = new Date(Long.parseLong(date.split(" ")[0]) * 1000); // UTC in miliseconds
        String[] parentSplits;
        parentSplits = parents != null ? parents.split(" ") : null;
        if ((parentSplits != null) && (parentSplits.length == 2)) {
            parentOneRev = createRevision(parentSplits[0]);
            parentTwoRev = createRevision(parentSplits[1]);
        }
        this.bMerged = this.parentOneRev != null && this.parentTwoRev != null && !this.parentOneRev.getRevisionNumber().equals("-1") && !this.parentTwoRev.getRevisionNumber().equals("-1");

        this.paths = new ArrayList<HgLogMessageChangedPath>();
        List<String> apathsStrings = new ArrayList<String>();
        List<String> dpathsStrings = new ArrayList<String>();
        List<String> cpathsStrings = new ArrayList<String>();

        // Mercurial Bug: Currently not seeing any file_copies coming back from Mercurial
        if (fd != null && !fd.equals("")) {
            for (String s : fd.split("\t")) {
                updatePaths(dpathsStrings, s, HgDelStatus);
            }
        }
        if (fc != null && !fc.equals("")) {
            String[] copyPaths = fc.split("\t");
            for (int i = 0; i < copyPaths.length / 2; ++i) {
                String path = copyPaths[i * 2];
                String original = copyPaths[i * 2 + 1];
                cpathsStrings.add(path);
                if (dpathsStrings.contains(original)) {
                    for (ListIterator<HgLogMessageChangedPath> it = paths.listIterator(); it.hasNext(); ) {
                        HgLogMessageChangedPath msg = it.next();
                        if (original.equals(msg.getPath())) {
                            it.remove();
                        }
                    }
                    paths.add(new HgLogMessageChangedPath(path, original, HgRenameStatus));
                } else {
                    paths.add(new HgLogMessageChangedPath(path, original, HgCopyStatus));
                }
            }
        }
        if (fa != null && !fa.equals("")) {
            for (String s : fa.split("\t")) {
                if(!cpathsStrings.contains(s)){
                    updatePaths(apathsStrings, s, HgAddStatus);
                }
            }
        }
        if (fm != null && !fm.equals("")) {
            for (String s : fm.split("\t")) {
                //#132743, incorrectly reporting files as added/modified, deleted/modified in same changeset
                if (!apathsStrings.contains(s) && !dpathsStrings.contains(s) && !cpathsStrings.contains(s)) {
                    updatePaths(null, s, HgModStatus);
                }
            }
        }
        this.dummyPaths = new ArrayList<HgLogMessageChangedPath>(filesShortPaths.size());
        for (String fileSP : filesShortPaths) {
            dummyPaths.add(new HgLogMessageChangedPath(fileSP, null, ' '));
        }
        if (branches.isEmpty()) {
            this.branches = new String[0];
        } else {
            this.branches = branches.split("\t");
        }
        if (tags.isEmpty()) {
            this.tags = new String[0];
        } else {
            this.tags = tags.split("\t");
        }
    }

    public HgLogMessageChangedPath [] getChangedPaths(){
        return paths.toArray(new HgLogMessageChangedPath[paths.size()]);
    }

    HgLogMessageChangedPath [] getDummyChangedPaths () {
        return dummyPaths.toArray(new HgLogMessageChangedPath[dummyPaths.size()]);
    }

    /**
     * Equal to getHgRevision().getRevisionNumber()
     * @return
     */
    public String getRevisionNumber () {
        return rev.getRevisionNumber();
    }

    public long getRevisionAsLong() {
        long revLong;
        try{
            revLong = Long.parseLong(rev.getRevisionNumber());
        }catch(NumberFormatException ex){
            // Ignore number format errors
            return 0;
        }
        return revLong;
    }
    
    public HgRevision getHgRevision () {
        return rev;
    }

    public Date getDate() {
        return date;
    }

    public String getAuthor() {
        return author;
    }

    public String getUsername () {
        return username;
    }

    /**
     * Equal to getHgRevision().getChangesetId()
     * @return
     */
    public String getCSetShortID() {
        return rev.getChangesetId();
    }
    
    public boolean isMerge () {
        return bMerged;
    }

    public HgRevision getAncestor (File file) {
        HgRevision ancestor = getAncestorFromMap(file);
        if (ancestor != null) {
            return ancestor;
        }
        if(bMerged){
            try{
                ancestor = HgCommand.getCommonAncestor(rootURL, parentOneRev.getRevisionNumber(), parentTwoRev.getRevisionNumber(), getLogger());
            } catch (HgException ex) {
                Mercurial.LOG.log(ex instanceof HgException.HgCommandCanceledException ? Level.FINE : Level.INFO, null, ex);
                return HgRevision.EMPTY;
            }
        } else {
            try{
                ancestor = HgCommand.getParent(new File(rootURL), file, rev.getRevisionNumber());
            } catch (HgException ex) {
                Mercurial.LOG.log(ex instanceof HgException.HgCommandCanceledException ? Level.FINE : Level.INFO, null, ex);
                return HgRevision.EMPTY;
            }
            if (ancestor == null) {
                // fallback to the old impl in case of any error
                try {
                    Integer.toString(Integer.parseInt(rev.getRevisionNumber()) - 1);
                } catch (NumberFormatException ex) {
                    ancestor = HgRevision.EMPTY;
                }
            }
        }
        addAncestorToMap(file, ancestor);
        return ancestor;
    }

    private OutputLogger getLogger() {
        if (logger == null) {
            logger = Mercurial.getInstance().getLogger(rootURL);
        }
        return logger;
    }

    public String getMessage() {
        return desc;
    }

    public String getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(String timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

    public String[] getBranches () {
        return branches;
    }

    public String[] getTags () {
        return tags;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("rev: ");
        sb.append(rev.getRevisionNumber());
        sb.append("\nauthor: ");
        sb.append(this.author);
        sb.append("\ndesc: ");
        sb.append(this.desc);
        sb.append("\ndate: ");
        sb.append(this.date);
        sb.append("\nid: ");
        sb.append(rev.getChangesetId());
        sb.append("\npaths: ");
        sb.append(this.paths);
        return sb.toString();
    }

    public File getOriginalFile (File root, File file) {
        for (HgLogMessageChangedPath path : paths) {
            if (file.equals(new File(root, path.getPath()))) {
                if (path.getCopySrcPath() == null) {
                    return file;
                } else {
                    return new File(root, path.getCopySrcPath());
                }
            }
        }
        return null;
    }

    private void addAncestorToMap (File file, HgRevision ancestor) {
        ancestors.put(file, ancestor);
    }

    private HgRevision getAncestorFromMap(File file) {
        return ancestors.get(file);
    }

    private HgRevision createRevision (String revisionString) {
        String[] ps1 = revisionString.split(":"); // NOI18N
        String revisionNumber = ps1 != null && ps1.length >= 1 ? ps1[0] : null;
        String changesetId = ps1 != null && ps1.length >= 2 ? ps1[1] : revisionNumber;
        return revisionNumber == null ? null : new HgRevision(changesetId, revisionNumber);
    }

    void refreshChangedPaths (HgProgressSupport supp, boolean incoming) {
        HgLogMessage[] messages;
        if (incoming) {
            messages = HgCommand.getIncomingMessages(new File(rootURL), getCSetShortID(), true, true, false, 1, supp.getLogger());
        } else {
            messages = HgCommand.getLogMessages(new File(rootURL), 
                    null, 
                    getCSetShortID(),
                    getCSetShortID(),
                    true,
                    true,
                    false,
                    1,
                    Collections.<String>emptyList(),
                    supp.getLogger(),
                    true);
        }
        paths.clear();
        dummyPaths.clear();
        paths.addAll(Arrays.asList(messages.length == 1 ? messages[0].getChangedPaths() : new HgLogMessageChangedPath[0]));
    }
    
    public static class HgRevision {
        private final String changesetId;
        private final String revisionNumber;
        
        public static final HgRevision EMPTY = new HgRevision("-1", "-1"); //NOI18N
        public static final HgRevision BASE = new HgRevision("BASE", "BASE"); //NOI18N
        public static final HgRevision CURRENT = new HgRevision("LOCAL", "LOCAL"); //NOI18N

        public HgRevision(String changesetId, String revisionNumber) {
            this.changesetId = changesetId;
            this.revisionNumber = revisionNumber;
        }

        public String getChangesetId() {
            return changesetId;
        }

        public String getRevisionNumber() {
            return revisionNumber;
        }
    }
}
