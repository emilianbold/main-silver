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

package org.netbeans.modules.cnd.modelimpl.platform;

import java.util.MissingResourceException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.openide.util.NbBundle;


/**
 * provides progress bar in status bar
 */
final class ParsingProgress {
    
    private final ProgressHandle handle;
    private int curWorkedUnits = 0;
    private int maxWorkUnits = 0;
    private int addedAfterStartParsing = 0;
    private int allWork = 0; // in procent points.
    private static final double ALL_WORK_DOUBLE = 10000.0;
    private static final int ALL_WORK_INT = 10000;
    private boolean started = false;
    private boolean determinate = false;
    private final ParsingProblemDetector problemDetector;
    private long startTime;
    
    /**  
     * Delay amount of milliseconds
     * that shall pass before the progress appears in status bar
     */
    private static final int INITIAL_DELAY;
    static {
        int value = 1000;
        try {
            value = Integer.parseInt(NbBundle.getMessage(ParsingProgress.class, "CSM_PARSING_PROGRESS_INITIAL_DELAY")); // NOI18N
            if (value < 0) {
                value = Integer.MAX_VALUE;
            }
        } catch (NumberFormatException e) {
            
        }
        INITIAL_DELAY = value;
    }
    
    /**
     * Constructs progress information for project
     */
    public ParsingProgress(CsmProject project) {
        String msg=NbBundle.getMessage(ModelSupport.class, "MSG_ParsingProgress", project.getName());
        handle = ProgressHandleFactory.createHandle(msg);
        problemDetector = new ParsingProblemDetector(project);
    }
    
    /**
     * Start the progress indication for indeterminate task.
     * it will be visualized by a progress bar in indeterminate mode.
     */
    public void start() {
        synchronized (handle) {
            if(!started) {
                started = true;
                handle.setInitialDelay(INITIAL_DELAY);
                problemDetector.start();
                handle.start();
            }
        }
    }
    
    /**
     * finish the task, remove the task's component from the progress bar UI.
     */        
    public void finish() {
        synchronized (handle) {
            if( started ) {
                problemDetector.finish();
                handle.finish();
                started = false;
            }
        }
    }

    /**
     * inform about adding header to reparse
     */
    public void addedToParse(CsmFile file) {
        addedAfterStartParsing++;
    }

    /**
     * inform about starting handling next file item
     */
    public void nextCsmFile(CsmFile file) {
        synchronized (handle) {
            if( ! started || !determinate) {
                return;
            }
            if( curWorkedUnits < maxWorkUnits + addedAfterStartParsing) {
                curWorkedUnits++;
                double ratio = 1.0;
                if (maxWorkUnits + addedAfterStartParsing > 0) {
                    ratio = ALL_WORK_DOUBLE / (maxWorkUnits + addedAfterStartParsing);
                }
                int work = (int)(ratio * curWorkedUnits);
                if (allWork <= work && work < ALL_WORK_INT) {
                    allWork = work;
                }
            } 
            try {
                String elapsedTime = getElapsedTime();
                String problem = problemDetector.nextCsmFile(file, curWorkedUnits, maxWorkUnits + addedAfterStartParsing);
                String msg = NbBundle.getMessage(ModelSupport.class, "MSG_ParsingProgressFull", ""+curWorkedUnits, ""+(maxWorkUnits + addedAfterStartParsing), file.getName().toString(), elapsedTime, problem); // NOI18N
                handle.progress(msg, allWork);
                //assert(curWorkedUnits <= maxWorkUnits);
            } catch (NullPointerException ex) {
                // very strange... but do not interrupt process
                DiagnosticExceptoins.register(ex);
            }
        }
    }

    private String getElapsedTime() throws MissingResourceException {
        if (curWorkedUnits < 10) {
            return ""; // NOI18N
        }
        long delta = (System.currentTimeMillis() - startTime)*(maxWorkUnits + addedAfterStartParsing - curWorkedUnits)/(curWorkedUnits);
        String elapsedTime;
        if (delta < 1000) {
            elapsedTime = ""; // NOI18N
        } else if (delta < 1000*60) {
            int s = (int) (delta/1000);
            elapsedTime = NbBundle.getMessage(ModelSupport.class, "Elapsed_seconds", ""+s); // NOI18N
        } else if (delta < 1000*60*60) {
            int s = (int) (delta/1000/60);
            elapsedTime = NbBundle.getMessage(ModelSupport.class, "Elapsed_minutes", ""+s); // NOI18N
        } else {
            int s = (int) (delta/1000/60/60);
            elapsedTime = NbBundle.getMessage(ModelSupport.class, "Elapsed_hours", ""+s); // NOI18N
        }
        return elapsedTime;
    }

    /**
     * Currently indeterminate task can be switched to show percentage completed.
     * A common use case is to calculate the amount of work in the beginning showing
     * in indeterminate mode and later switch to the progress with known steps
     */
    public void switchToDeterminate(int maxWorkUnits) {
        synchronized (handle) {
            if( ! started ) {
                return;
            }
            if (!determinate) {
                this.startTime = System.currentTimeMillis();
                this.maxWorkUnits = maxWorkUnits;
                addedAfterStartParsing = 0;
                problemDetector.switchToDeterminate(maxWorkUnits);
                handle.switchToDeterminate(ALL_WORK_INT);
                determinate = true;
            }
        }
    }
}   
