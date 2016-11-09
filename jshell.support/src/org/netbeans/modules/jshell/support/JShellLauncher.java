/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.jshell.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.lang.model.SourceVersion;
import org.netbeans.lib.nbjshell.LaunchJDIAgent;
import jdk.jshell.JShell;
import jdk.jshell.JShellAccessor;
import org.netbeans.lib.nbjshell.NbExecutionControl;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionEnv;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class JShellLauncher extends InternalJShell {
    private ClasspathInfo   cpInfo;
    private SpecificationVersion srcVersion;
    private SpecificationVersion targetVersion;
    private String prefix = "";
    private JShellGenerator execGen;

    /**
     * 
     * @param cmdout command output
     * @param cmderr command error
     * @param userin user input to the JShell to the JShell VM
     * @param userout user output from the JShell VM
     * @param usererr  user error from the JShell VM
     */
    public JShellLauncher(ClasspathInfo cpInfo, 
            PrintStream cmdout, PrintStream cmderr, InputStream userin, PrintStream userout, PrintStream usererr, JShellGenerator execEnv) {
        super(cmdout, cmderr, userin, userout, usererr);
        this.execGen = execEnv;
        this.cpInfo = cpInfo;
    }
    
    public void setSourceVersion(SpecificationVersion level) {
        this.srcVersion = level;
    }
    
    public void setTargetVersion(SpecificationVersion level) {
        this.targetVersion = level;
    }

    
    protected String prompt(boolean continuation) {
        int index = state == null ? 0 :  (int)state.snippets().count() + 1;
        if (continuation) {
            return ">> "; // NOI18N 
        } else if (feedback() == Feedback.Concise) {
            return "[" + index + "] -> "; // NOI18N 
        } else {
            return "\n[" + index + "] -> "; // NOI18N 
        }
    }

    public void start() {
        fluff("Welcome to the JShell NetBeans integration"); // NOI18N 
        fluff("Type /help for help"); // NOI18N 
        ensureLive();
        cmdout.append(prompt(false));
    }
    
    public void stop() {
        closeState();
    }
    
    public void evaluate(String command, boolean prompt) throws IOException {
        ensureLive();
        String trimmed = trimEnd(command);
        if (!trimmed.isEmpty()) {
            prefix = process(prefix, command);
        }
        if (prompt) {
            cmdout.append(prompt(!prefix.isEmpty()));
        }
    }
    
    public List<String> completion(String command) {
        return completions(prefix, command);
    }

    private void ensureLive() {
        if (!live) {
            resetState();
            live = true;
        }
    }

    public JShell getJShell() {
        ensureLive();
        return state;
    }

    @Override
    protected void setupState() {
        printSystemInfo();
    }
    
    @Override
    protected JShell createJShellInstance() {
        if (execGen == null) {
            execGen = new JShellGenerator() {
                @Override
                public String getTargetSpec() {
                    return null;
                }

                @Override
                public ExecutionControl generate(ExecutionEnv ee) throws Throwable {
                    return LaunchJDIAgent.launch().generate(ee);
                }
            };
        }
        ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            JShell.Builder b = createJShell().
                    executionEngine(execGen).
                    fileManager(
                        new StubJavaFileManager(
                            ClasspathInfoAccessor.getINSTANCE().createFileManager(cpInfo),
                            cpInfo
                        )
                    );
            String s = System.getProperty("jshell.logging.properties");
            if (s == null) {
                b = b.remoteVMOptions("-classpath", quote(classpath));
            } else {
                b = b.remoteVMOptions("-classpath", quote(classpath), quote("-Djava.util.logging.config.file=" + s));
            }
            if (srcVersion != null) {
                b.compilerOptions("-source", srcVersion.toString());
            }
            if (targetVersion != null) {
                b.compilerOptions("-target", targetVersion.toString());
            }
            JShell ret = b.build();
            return ret;
        } finally {
            Thread.currentThread().setContextClassLoader(ctxLoader);
        }
    }
    
    private static String quote(String s) {
        if (s.indexOf(' ') == -1) {
            return s;
        }
        return '"' + s + '"';
    }
    
    private String decorateLaunchArgs(String s) {
        return "-classpath " + classpath; // NOI18N
    }
    
    @NbBundle.Messages({
        "MSG_SystemInformation=System Information:",
        "# {0} - java vm version",
        "MSG_JavaVersion=    Java version:    {0}",
        "# {0} - virtual machine",
        "# {1} - virtual machine version",
        "MSG_VirtualMachine=    Virtual Machine: {0}  {1}",
        "MSG_Classpath=    Classpath:",
        "MSG_VMVersionUnknown=<unknown>",
        "MSG_MachineUnknown=<unknown>",
        "MSG_VersionUnknown=<unknown>",
    })
    private void printSystemInfo() {
        NbExecutionControl ctrl = JShellAccessor.getNbExecControl(state);
        Map<String, String> versionInfo = ctrl.commandVersionInfo();
        
        if (versionInfo.isEmpty()) {
            // some error ?
            return;
        }
        fluff(""); // newline
        fluff(Bundle.MSG_SystemInformation());
        String javaName = versionInfo.getOrDefault("java.vm.name", Bundle.MSG_MachineUnknown()); // NOI18N
        String vmVersion = versionInfo.getOrDefault("java.vm.version", Bundle.MSG_VMVersionUnknown()); // NOI18N
        String javaSpec = versionInfo.getOrDefault("java.runtime.version", Bundle.MSG_VersionUnknown() );
        fluff(Bundle.MSG_JavaVersion(javaSpec));
        fluff(Bundle.MSG_VirtualMachine(javaName, vmVersion));
        
        String cpString = versionInfo.get("nb.class.path"); // NOI18N
        String[] cpItems = cpString.split(":"); // NOI18N
        if (cpItems.length > 0) {
            fluff("Classpath:");
            for (String item : cpItems) {
                if (item.isEmpty()) {
                    continue;
                }
                fluff("\t%s", item);
            }
        }
        fluff(""); // newline
    }

    private String classpath;

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }
}
