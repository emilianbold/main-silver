/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.clientproject.grunt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

public class TargetLister {
     
    private static final Map<String, Pair<Long, Collection<Target>>> cache = new HashMap<>();
    private static final RequestProcessor RP = new RequestProcessor(TargetLister.class);
    
    public static Collection<Target> getTargets(RunTargetsAction.LazyMenu menu, FileObject pr) throws IOException { 
        Pair<Long, Collection<Target>> targetPair = cache.get(pr.getPath());
        if (targetPair != null) {
            if (targetPair.first().equals(pr.lastModified().getTime())) {
                return targetPair.second();
            }
        }
        cache.put(pr.getPath(), Pair.of(-1L, (Collection<Target>) null));
        read(pr, menu);
        return null; 
    }
    
    public static void read(final FileObject gruntFile, final RunTargetsAction.LazyMenu m) {
        RequestProcessor.Task post = RP.post(new Runnable() {
            
            @Override
            public void run() {
                ProgressHandle handle = ProgressHandleFactory.createSystemHandle(Bundle.LBL_LoadingTasks());
                handle.start();
                try {
                    String data = null;

                    String work = gruntFile.getParent().getPath();
                    if (Utilities.isWindows()) {
                        data = ProcessUtilities.callProcess("cmd", work, true, 3 * 60 * 1000 , "/C grunt -h --no-color");//NOI18N
                    } else if (Utilities.isMac()) {
                        data = ProcessUtilities.callProcess("/bin/bash", work, true, 3 * 60 * 1000 , "-lc", "grunt -h --no-color");//NOI18N
                    } else {
                        data = ProcessUtilities.callProcess("grunt", work, true, 3 * 60 * 1000 , "-h", "--no-color");//NOI18N
                    }

                    parse(data, gruntFile);
                } catch (IOException ex) {
                    Collection<Target> col = new ArrayList<>();
                    col.add(new Target("default", gruntFile)); //NOI18n
                    cache.put(gruntFile.getPath(), Pair.of(gruntFile.lastModified().getTime(), col));
                } finally {
                    handle.finish();
                    m.rebuild();
                }
            }

        });
    }
    
    public static void invalidateCache(final FileObject gruntFile) {
        if (gruntFile == null) {
            return;
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                cache.remove(gruntFile.getPath());
            }
        });
    }
    
    private static void parse(String output, FileObject gruntFile) throws IOException {
        BufferedReader r = new BufferedReader(new StringReader(output));

        Collection<Target> col = new ArrayList<>();
        for (;;) {
            String line = r.readLine();
            if (line == null) {
                col.add(new Target("default", gruntFile));//NI18N
                break;
            }
            if (line.trim().toLowerCase().startsWith("available tasks")) {//NOI18N
                break;
            }
        }
        
        String line = r.readLine();
        int white_space_column=-1;
        
        while (line != null) {
            if (line.trim().isEmpty()) {
                break;
            }
            if (line.trim().equals("(no tasks found)")) {//NOI18N
                col.add(new Target("default", gruntFile));//NI18N
                break;
            }

            String l = line.trim();
            int right = l.indexOf(" ");
            int left = line.indexOf(l);
            if (white_space_column == -1) {
                white_space_column = left + right + 2;
            }
            
            if (right > 0 && left < white_space_column) {
                l = l.substring(0, right);
                col.add(new Target(l, gruntFile));
            }
            line = r.readLine();
        }
        cache.put(gruntFile.getPath(), Pair.of(gruntFile.lastModified().getTime(), col));
    }


    public static class Target {
        private final String name;
        private final FileObject script;

        public Target(String name, FileObject script) {
            this.name = name;
            this.script = script;
                  
        }

        String getName() {
            return name;
        }
        
        FileObject getOriginatingScript() {
            return script;
        }

        boolean isOverridden() {
            return false;
        }

        boolean isInternal() {
            return false;
        }

        boolean isDefault() {
            return name.equals("default");//NOI18N
        }

        boolean isDescribed() {
            return true;
        }
    }
    
}
