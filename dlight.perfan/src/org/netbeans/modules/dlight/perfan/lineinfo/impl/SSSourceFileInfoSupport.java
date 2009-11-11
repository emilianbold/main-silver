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
package org.netbeans.modules.dlight.perfan.lineinfo.impl;

import org.netbeans.modules.dlight.perfan.stack.impl.FunctionCallImpl;
import org.netbeans.modules.dlight.perfan.storage.impl.FunctionStatistic;
import org.netbeans.modules.dlight.perfan.storage.impl.PerfanDataStorage;
import org.netbeans.modules.dlight.perfan.util.Computable;
import org.netbeans.modules.dlight.perfan.util.TasksCachedProcessor;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;

/**
 *
 * @author ak119685
 */
public final class SSSourceFileInfoSupport {

    private final PerfanDataStorage storage;
    private final TasksCachedProcessor<SourceFileInfoFetchTaskParams, SourceFileInfo> sourceLineInfoCachedProvider =
            new TasksCachedProcessor<SourceFileInfoFetchTaskParams, SourceFileInfo>(new SourceFileInfoFetchTask(), false);

    private SSSourceFileInfoSupport(final PerfanDataStorage storage) {
        this.storage = storage;
    }

    public static SSSourceFileInfoSupport getSourceFileInfoSupportFor(PerfanDataStorage storage) {
        return new SSSourceFileInfoSupport(storage);
    }

    public SourceFileInfo getSourceFileInfo(final FunctionCallImpl functionCall) {
        SourceFileInfo result = null;

        try {
            SourceFileInfoFetchTaskParams params = new SourceFileInfoFetchTaskParams(
                    functionCall, storage);
            result = sourceLineInfoCachedProvider.compute(params);
        } catch (Throwable ex) {
        }

        return result;
    }

    public final static class SourceFileInfoFetchTaskParams {

        public final FunctionCallImpl functionCall;
        public final PerfanDataStorage storage;

        SourceFileInfoFetchTaskParams(FunctionCallImpl functionCall, PerfanDataStorage storage) {
            this.functionCall = functionCall;
            this.storage = storage;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SourceFileInfoFetchTaskParams)) {
                return false;
            }

            SourceFileInfoFetchTaskParams that = (SourceFileInfoFetchTaskParams) obj;

            return this.functionCall.getFunctionRefID() == that.functionCall.getFunctionRefID() &&
                    this.storage.equals(that.storage);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = (int) (17 * hash + this.functionCall.getFunctionRefID());
            hash = 17 * hash + (this.storage != null ? this.storage.hashCode() : 0);
            return hash;
        }
    }

    public final static class SourceFileInfoFetchTask implements Computable<SourceFileInfoFetchTaskParams, SourceFileInfo> {

        public SourceFileInfo compute(final SourceFileInfoFetchTaskParams params) throws InterruptedException {
            SourceFileInfo result = null;
            String sourceFile = null;
            int line = -1;

            sourceFile = params.functionCall.getSourceFile();
            line = (int) params.functionCall.getOffset();

            if (!"(unknown)".equals(sourceFile)) { // NOI18N
                if (sourceFile == null || line < 0 || !(sourceFile.startsWith("/"))) { // NOI18N
                    // i.e. if no source info or file that we have is without full path
                    final FunctionStatistic fStatistics = params.storage.getFunctionStatistic(params.functionCall);

                    if (fStatistics != null) {
                        sourceFile = fStatistics.getSourceFile();
                        line = fStatistics.getSrcFileLine();
                        params.functionCall.setSourceFile(sourceFile);
                    }
                }
                result = new SourceFileInfo(sourceFile, line, 0);
            }

            return result;
        }
    }
}
