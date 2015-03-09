/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2015 Oracle and/or its affiliates. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.lib.profiler.ui.cpu;

import java.util.ResourceBundle;
import org.netbeans.lib.profiler.ui.results.DataView;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class CPUView extends DataView {
    
    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.cpu.Bundle"); // NOI18N
    protected static final String EXPORT_METHODS = messages.getString("CPUView_ExportMethods"); // NOI18N
    protected static final String EXPORT_FORWARD_CALLS = messages.getString("CPUView_ExportForwardCalls"); // NOI18N
    protected static final String EXPORT_HOTSPOTS = messages.getString("CPUView_ExportHotSpots"); // NOI18N
    protected static final String EXPORT_REVERSE_CALLS = messages.getString("CPUView_ExportReverseCalls"); // NOI18N
    protected static final String COLUMN_NAME = messages.getString("CPUView_ColumnName"); // NOI18N
    protected static final String COLUMN_SELFTIME = messages.getString("CPUView_ColumnSelfTime"); // NOI18N
    protected static final String COLUMN_SELFTIME_CPU = messages.getString("CPUView_ColumnSelfTimeCpu"); // NOI18N
    protected static final String COLUMN_TOTALTIME = messages.getString("CPUView_ColumnTotalTime"); // NOI18N
    protected static final String COLUMN_TOTALTIME_CPU = messages.getString("CPUView_ColumnTotalTimeCpu"); // NOI18N
    protected static final String COLUMN_HITS = messages.getString("CPUView_ColumnHits"); // NOI18N
    protected static final String COLUMN_INVOCATIONS = messages.getString("CPUView_ColumnInvocations"); // NOI18N
    protected static final String COLUMN_SELECTED = messages.getString("CPUView_ColumnSelected"); // NOI18N
    protected static final String ACTION_GOTOSOURCE = messages.getString("CPUView_ActionGoToSource"); // NOI18N
    protected static final String ACTION_PROFILE_METHOD = messages.getString("CPUView_ActionProfileMethod"); // NOI18N
    protected static final String ACTION_PROFILE_CLASS = messages.getString("CPUView_ActionProfileClass"); // NOI18N
    // -----
    
}
