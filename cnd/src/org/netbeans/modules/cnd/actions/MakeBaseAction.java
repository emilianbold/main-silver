/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.actions;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.LineConvertorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.execution.CompilerLineConvertor;
import org.netbeans.modules.cnd.loaders.MakefileDataObject;
import org.netbeans.modules.cnd.settings.MakeSettings;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.NativeProcessChangeEvent;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * Base class for Make Actions ...
 */
public abstract class MakeBaseAction extends AbstractExecutorRunAction {

    @Override
    protected boolean accept(DataObject object) {
        return object instanceof MakefileDataObject;
    }

    protected void performAction(Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++){
            performAction(activatedNodes[i], "");
        }
    }

    protected void performAction(Node node, String target) {
        performAction(node, target, null, null, null, null);
    }

    protected void performAction(Node node, String target, final ExecutionListener listener, final Writer outputListener, Project project, List<String> additionalEnvironment) {
        if (MakeSettings.getDefault().getSaveAll()) {
            LifecycleManager.getDefault().saveAll();
        }
        DataObject dataObject = node.getCookie(DataObject.class);
        final FileObject fileObject = dataObject.getPrimaryFile();
        File makefile = FileUtil.toFile(fileObject);
        // Build directory
        File buildDir = getBuildDirectory(node,Tool.MakeTool);
        // Executable
        String executable = getCommand(node, project, Tool.MakeTool, "make"); // NOI18N
        // Arguments
        String[] args;
        if (target.length() == 0) {
            args = new String[]{"-f", makefile.getName()}; // NOI18N
        } else {
            args = new String[]{"-f", makefile.getName(), target}; // NOI18N
        }
        // Tab Name
        String tabName = getString("MAKE_LABEL", node.getName()); // NOI18N
        if (target != null && target.length() > 0) {
            tabName += " " + target; // NOI18N
        }

        final ExecutionEnvironment execEnv = getExecutionEnvironment(fileObject, project);
        String[] env = prepareEnv(execEnv);
        if (additionalEnvironment != null && additionalEnvironment.size()>0){
            String[] tmp = new String[env.length + additionalEnvironment.size()];
            for(int i=0; i < env.length; i++){
                tmp[i] = env[i];
            }
            for(int i=0; i < additionalEnvironment.size(); i++){
                tmp[env.length + i] = additionalEnvironment.get(i);
            }
            env = tmp;
        }
        Map<String, String> envMap = new HashMap<String, String>();
        for(String s: env) {
            int i = s.indexOf('='); // NOI18N
            if (i>0) {
                String key = s.substring(0, i);
                String value = s.substring(i+1);
                envMap.put(key, value);
            }
        }
        if ("cc".equals(envMap.get("CC"))){ // NOI18N
            envMap.put("SPRO_EXPAND_ERRORS", ""); // NOI18N
        }

        InputOutput _tab = IOProvider.getDefault().getIO(tabName, false); // This will (sometimes!) find an existing one.
        _tab.closeInputOutput(); // Close it...
        final InputOutput tab = IOProvider.getDefault().getIO(tabName, true); // Create a new ...
        try {
            tab.getOut().reset();
        } catch (IOException ioe) {
        }
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv)
        .setExecutable(executable)
        .addEnvironmentVariables(envMap)
        .setWorkingDirectory(buildDir.getPath())
        .setArguments(args)
        .unbufferOutput(false)
        .addNativeProcessListener(new ChangeListener() {
           private long startTimeMillis;
           public void stateChanged(ChangeEvent e) {
                if (!(e instanceof NativeProcessChangeEvent)) {
                    return;
                }
                NativeProcessChangeEvent event = (NativeProcessChangeEvent) e;
                NativeProcess process = (NativeProcess) event.getSource();
                switch (event.state) {
                    case INITIAL:
                        break;
                    case STARTING:
                        startTimeMillis = System.currentTimeMillis();
                        if (listener != null) {
                            listener.executionStarted(event.pid);
                        }
                        break;
                    case RUNNING:
                        break;
                    case CANCELLED:
                    {
                        if (listener != null) {
                            listener.executionFinished(process.exitValue());
                        }
                        String message = getString("Output.MakeTerminated", formatTime(System.currentTimeMillis() - startTimeMillis)); // NOI18N
                        tab.getOut().println();
                        tab.getOut().println(message);
                        tab.getOut().flush();
                        break;
                    }
                    case ERROR:
                    {
                        if (listener != null) {
                            listener.executionFinished(-1);
                        }
                        String message = getString("Output.MakeFailedToStart"); // NOI18N
                        tab.getOut().println();
                        tab.getOut().println(message);
                        tab.getOut().flush();
                        break;
                    }
                    case FINISHED:
                    {
                        if (listener != null) {
                            listener.executionFinished(process.exitValue());
                        }
                        String message;
                        if (process.exitValue() != 0) {
                            message = getString("Output.MakeFailed", ""+process.exitValue(), formatTime(System.currentTimeMillis() - startTimeMillis)); // NOI18N
                        } else {
                            message = getString("Output.MakeSuccessful", formatTime(System.currentTimeMillis() - startTimeMillis)); // NOI18N
                        }
                        tab.getOut().println();
                        tab.getOut().println(message);
                        tab.getOut().flush();
                        break;
                    }
                }
            }
        });
        npb.redirectError();
        
        final LineConvertor lineConvertor = new CompilerLineConvertor(execEnv, fileObject.getParent());
        LineConvertorFactory factory = new ExecutionDescriptor.LineConvertorFactory() {
            public LineConvertor newLineConvertor() {
                return new LineConvertor() {
                    @Override
                    public List<ConvertedLine> convert(String line) {
                        if (outputListener != null) {
                            try {
                                outputListener.write(line);
                                outputListener.write("\n"); // NOI18N
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        return lineConvertor.convert(line);
                    }
                };
            }
        };
        ExecutionDescriptor descr = new ExecutionDescriptor()
        .controllable(true)
        .frontWindow(true)
        .inputVisible(true)
        .showProgress(true)
        .inputOutput(tab)
        .outLineBased(true)
        .errConvertorFactory(factory)
        .outConvertorFactory(factory);
        final ExecutionService es = ExecutionService.newService(npb, descr, "make"); // NOI18N
        Future<Integer> result = es.run();
    }
}
