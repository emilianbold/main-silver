/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.api.compilers;


import java.util.ArrayList;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.api.compilers.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.api.compilers.ToolchainManager.ToolchainDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
final class ToolchainScriptGenerator {
    private static final boolean TRACE = true;
    private static final String[] platforms = new String[]{
            "PLATFORM_SOLARIS_SPARC", // NOI18N
            "PLATFORM_SOLARIS_INTEL", // NOI18N
            "PLATFORM_LINUX", // NOI18N
            "PLATFORM_WINDOWS", // NOI18N
            "PLATFORM_MACOSX", // NOI18N
            "PLATFORM_NONE" // NOI18N
    };
    private static final int[] platformsID = new int[]{
            PlatformTypes.PLATFORM_SOLARIS_SPARC,
            PlatformTypes.PLATFORM_SOLARIS_INTEL,
            PlatformTypes.PLATFORM_LINUX,
            PlatformTypes.PLATFORM_WINDOWS,
            PlatformTypes.PLATFORM_MACOSX,
            PlatformTypes.PLATFORM_NONE
    };
    private final StringBuilder buf = new StringBuilder();
    
    private ToolchainScriptGenerator(){
    }

    static final String generateScript(String path){
        ToolchainScriptGenerator generator = new ToolchainScriptGenerator();
        generator.prefix(path);
        generator.scanPaths();
        generator.printSets();
        return generator.buf.toString();
    }

    private void prefix(String path){
        line("#!/bin/sh"); // NOI18N
        lines(NbBundle.getMessage(ToolchainScriptGenerator.class, "DetectHostInfo")); // NOI18N
        if (path != null) {
            line("PATHSLIST=\""+path+"\""); // NOI18N
        } else {
            line("echo $PLATFORM_NAME"); // NOI18N
            for(int i = 0; i < platforms.length; i++) {
                line("if [ \"$PLATFORM\" == \"$"+platforms[i]+"\" ]; then"); // NOI18N
                if (platformsID[i] == PlatformTypes.PLATFORM_WINDOWS) {
                    line("PATH=$PATH;C:/WINDOWS/System32;C:/WINDOWS;C:/WINDOWS/System32/WBem"); // NOI18N
                } else if (platformsID[i] != PlatformTypes.PLATFORM_NONE) {
                    line("PATH=$PATH:/bin:/usr/bin:/sbin:/usr/sbin"); // NOI18N
                } else {
                    line("PATH=$PATH"); // NOI18N
                }
                ArrayList<String> dirlist = new ArrayList<String>();
                CompilerSetManager.appendDefaultLocations(platformsID[i], dirlist);
                for(String s : dirlist) {
                    line("PATH=$PATH:"+s); // NOI18N
                }
                line("fi"); // NOI18N
            }
            line("PATHSLIST=$PATH"); // NOI18N
        }
        line("if [ \"$PLATFORM\" == \"$PLATFORM_WINDOWS\" ]; then"); // NOI18N
        line(" IFS=;"); // NOI18N
        line("else"); // NOI18N
        line(" IFS=:"); // NOI18N
        line("fi"); // NOI18N
        line("declare -a cset=('')"); // NOI18N
        line("declare i=0"); // NOI18N
    }

    private void scanPaths(){
        line("for f in $PATHSLIST; do"); // NOI18N
        line("  line="); // NOI18N
        line("  flavor="); // NOI18N
        line("  if [ \"${f:0:1}\" != \"/\" ]; then"); // NOI18N
        line("    continue  # skip relative directories"); // NOI18N
        line("  fi"); // NOI18N
        line("  if [ \"${f:0:8}\" = \"/usr/ucb\" ]; then"); // NOI18N
        line("    continue  # skip /usr/ucb (IZ #142780)"); // NOI18N
        line("  fi"); // NOI18N
        scanPath();
        line("done"); // NOI18N
    }
    private void scanPath(){
        for(int i = 0; i < platforms.length; i++) {
            line("if [ \"$PLATFORM\" == \"$"+platforms[i]+"\" ]; then"); // NOI18N
            platformPath(platformsID[i]);
            line("fi"); // NOI18N
        }
    }
    private void platformPath(int platform){
        for (ToolchainDescriptor d : ToolchainManager.getImpl().getToolchains(platform)) {
            CompilerDescriptor c = d.getC();
            if (c == null || c.getNames().length == 0) {
                continue;
            }
            line("status="); // NOI18N
            line("while [ ! -n \"$status\" ]; do"); // NOI18N
            if (c.getPathPattern() != null) {
                // todo windows use case insensitive regexp
                line("  echo $f | egrep -s -e \""+c.getPathPattern()+"\""); // NOI18N
                line("  status=$?"); // NOI18N
                line("  if [ ! \"$status\" == \"0\" ]; then"); // NOI18N
                if (c.getExistFolder() == null) {
                    line("    break"); // NOI18N
                } else {
                    line("    if [ ! -d \"$f/"+c.getExistFolder()+"\" ]; then"); // NOI18N
                    line("      break"); // NOI18N
                    line("    fi"); // NOI18N
                }
                line("  fi"); // NOI18N
            }
            line("  file=\"$f/"+c.getNames()[0]+"\""); // NOI18N
            line("  if [ ! -x \"$file\" ]; then"); // NOI18N
            if (platform == PlatformTypes.PLATFORM_WINDOWS) {
                line("    file=\"$f/"+c.getNames()[0]+".exe\""); // NOI18N
                line("    if [ ! -x \"$f/"+c.getNames()[0]+".exe\" ]; then"); // NOI18N
                line("      break"); // NOI18N
                line("    fi"); // NOI18N
            } else {
                line("    break"); // NOI18N
            }
            line("  fi"); // NOI18N
            if (c.getVersionFlags() != null && c.getVersionPattern() != null){
                line("  $file "+c.getVersionFlags()+" 2>&1 | egrep -s -e \""+c.getVersionPattern()+"\""); // NOI18N
                line("  status=$?"); // NOI18N
                line("  if [ ! \"$status\" == \"0\" ]; then"); // NOI18N
                line("    break"); // NOI18N
                line("  fi"); // NOI18N
            }
            //Found compiler set
            line("  line=\""+d.getName()+";$f\""); // NOI18N
            line("  flavor=\""+d.getName()+";\""); // NOI18N
            addTool("c", d.getC().getNames(), platform); // NOI18N
            if (d.getCpp() != null) {
                addTool("cpp", d.getCpp().getNames(), platform); // NOI18N
            }
            if (d.getFortran() != null) {
                addTool("fortran", d.getFortran().getNames(), platform); // NOI18N
            }
            if (d.getAssembler() != null) {
                addTool("assembler", d.getAssembler().getNames(), platform); // NOI18N
            }
            if (d.getMake() != null) {
                addTool("make", d.getMake().getNames(), platform); // NOI18N
            }
            if (d.getDebugger() != null) {
                addTool("debugger", d.getDebugger().getNames(), platform); // NOI18N
            }
            if (d.getCMake() != null) {
                addTool("cmake", d.getCMake().getNames(), platform); // NOI18N
            }
            if (d.getQMake() != null) {
                addTool("qmake", d.getQMake().getNames(), platform); // NOI18N
            }
            line("  break"); // NOI18N
            line("done"); // NOI18N
            line("addNewToolChain"); // NOI18N
        }
    }

    private void addTool(String kind, String[] names, int platform){
        if (names != null) {
            line("status="); // NOI18N
            StringBuilder list = new StringBuilder();
            for(String name : names) {
                if (list.length()>0) {
                    if (platform == PlatformTypes.PLATFORM_WINDOWS) {
                        list.append(';'); // NOI18N
                    } else {
                        list.append(':'); // NOI18N
                    }
                }
                list.append(name);
                line("if [ ! -n \"$status\" ]; then"); // NOI18N
                line("  if [ -x \"$f/"+name+"\" ]; then"); // NOI18N
                line("    line=\"$line;"+kind+"=$f/"+name+"\""); // NOI18N
                line("    status=1"); // NOI18N
                if (platform == PlatformTypes.PLATFORM_WINDOWS) {
                    line("  else"); // NOI18N
                    line("    if [ -x \"$f/"+name+".exe\" ]; then"); // NOI18N
                    line("    line=\"$line;"+kind+"=$f/"+name+".exe\""); // NOI18N
                    line("      status=1"); // NOI18N
                    line("    fi"); // NOI18N
                }
                line("  fi"); // NOI18N
                line("fi"); // NOI18N
            }
            if (list.length()>0) {
                line("if [ ! -n \"$status\" ]; then"); // NOI18N
                line("  status=`findInPath \""+list.toString()+"\"`"); // NOI18N
                line("  if [ -n \"$status\" ]; then"); // NOI18N
                line("    line=\"$line;"+kind+"(PATH)=$status\""); // NOI18N
                line("  fi"); // NOI18N
                line("fi"); // NOI18N
            }
        }
    }

    void printSets(){
        line("# Print the set of compiler collections, one per line"); // NOI18N
        line("if [ -n \"${cset[*]}\" ]; then"); // NOI18N
        line("  j=0"); // NOI18N
        line("  while [ $j -lt $i ]; do"); // NOI18N
        line("    echo \"${cset[$j]}\""); // NOI18N
        line("    j=$((j+1))"); // NOI18N
        line("  done"); // NOI18N
        line("fi"); // NOI18N
    }

    private void lines(String lines){
        StringTokenizer st = new StringTokenizer(lines,"\n"); // NOI18N
        while(st.hasMoreTokens()) {
            line(st.nextToken());
        }
    }

    int level = 0;
    private void line(String line){
        String l = line.trim();
        if (TRACE) {
            if (l.equals("fi") || l.equals("done") || l.equals("else") || l.endsWith("}")){ // NOI18N
                level--;
            }
            if (level >= 0) {
                for(int i = 0; i < level; i++){
                    buf.append(' ');
                    buf.append(' ');
                }
            }
            if (l.startsWith("while ") || l.startsWith("if ") || l.startsWith("for ") || l.equals("else") || l.endsWith("{")){ // NOI18N
                level++;
            }
        }
        buf.append(l).append('\n'); // NOI18N
    }
}
