/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.truffle.access;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.debugger.jpda.models.CallStackFrameImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.spi.StrataProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;

/**
 *
 * @author martin
 */
@DebuggerServiceRegistration(path = "netbeans-JPDASession", types = { StrataProvider.class })
public class TruffleStrataProvider implements StrataProvider {
    
    public static final String TRUFFLE_STRATUM = "TruffleScript";
    
    private static final String TRUFFLE_ACCESS_CLASS = TruffleAccess.BASIC_CLASS_NAME;
    private static final String TRUFFLE_ACCESS_METHOD = "executionHalted";
    
    @Override
    public String getDefaultStratum(CallStackFrameImpl csf) {
        if (isInTruffleAccessPoint(csf)) {
            return TRUFFLE_STRATUM;
        }
        return null;
    }

    @Override
    public List<String> getAvailableStrata(CallStackFrameImpl csf) {
        if (isInTruffleAccessPoint(csf)) {
            return Collections.singletonList(TRUFFLE_STRATUM);
        }
        return null;
    }
    
    private boolean isInTruffleAccessPoint(CallStackFrameImpl csf) {
        return TRUFFLE_ACCESS_CLASS.equals(csf.getClassName()) &&
               TRUFFLE_ACCESS_METHOD.equals(csf.getMethodName());
    }

    @Override
    public int getStrataLineNumber(CallStackFrameImpl csf, String stratum) {
        if (TRUFFLE_STRATUM.equals(stratum)) {
            CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(((JPDAThreadImpl) csf.getThread()).getDebugger());
            if (currentPCInfo != null) {
                return currentPCInfo.getSourcePosition().getLine();
            }
        }
        return csf.getLineNumber(stratum);
    }
    
}
