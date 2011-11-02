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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.repository.impl;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author sg155630
 */
public class RepositoryValidationInterruptedParse extends RepositoryValidationBase {
    private static final RequestProcessor RP = new RequestProcessor("Sleep");
    private boolean isShutdown = false;

    public RepositoryValidationInterruptedParse(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        System.setProperty("cnd.repository.hardrefs", Boolean.FALSE.toString()); //NOI18N
        System.setProperty("org.netbeans.modules.cnd.apt.level","OFF"); // NOI18N
        assertNotNull("This test can only be run from suite", RepositoryValidationGoldens.getGoldenDirectory()); //NOI18N
        System.setProperty(PROPERTY_GOLDEN_PATH, RepositoryValidationGoldens.getGoldenDirectory());
        super.setUp();
    }

    public void testInterrupt() throws Exception {
        List<String> args = find();
        assert args.size() > 0;
        //args.add("-fq"); //NOI18N
        RP.post(new Runnable() {

            @Override
            public void run() {
                try {
                    tearDown();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }, 500);
        final long currentTimeMillis = System.currentTimeMillis();
        performTest(args.toArray(new String[]{}), nimi + ".out", nimi + ".err");
        System.err.println("End "+(System.currentTimeMillis()-currentTimeMillis));
        assertNoExceptions();
    }

    @Override
    protected boolean returnOnShutdown() {
        if (CsmModelState.OFF == getTraceModel().getModel().getState()) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean dumpModel() {
        return false;
    }

    @Override
    protected synchronized void tearDown() throws Exception {
        if (!isShutdown) {
            getTestModelHelper().shutdown(false);
            isShutdown = true;
        }
    }
    
    @Override
    protected void performTest(String[] args, String goldenDataFileName, String goldenErrFileName, Object... params) throws Exception {
        File workDir = getWorkDir();

        File output = new File(workDir, goldenDataFileName);
        PrintStream streamOut = new PrintStream(output);
        File error = goldenErrFileName == null ? null : new File(workDir, goldenErrFileName);
        PrintStream streamErr = goldenErrFileName == null ? null : new FilteredPrintStream(error);
        try {
            doTest(args, streamOut, streamErr, params);
        } finally {
            // restore err and out
            streamOut.close();
            if (streamErr != null) {
                streamErr.close();
            }
        }
    }

}
