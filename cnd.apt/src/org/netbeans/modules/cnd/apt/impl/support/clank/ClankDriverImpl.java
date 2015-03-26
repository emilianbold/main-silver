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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.apt.impl.support.clank;

import java.io.IOException;
import java.util.Collections;
import org.clang.tools.services.ClankCompilationDataBase;
import org.clang.tools.services.ClankPreprocessorServices;
import org.clang.tools.services.ClankRunPreprocessorSettings;
import org.clang.tools.services.support.Interrupter;
import org.clank.support.NativePointer;
import org.llvm.support.llvm;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenStream;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Voskresensky
 */
public class ClankDriverImpl {

    static final boolean TRACE = false;

    public static boolean preprocessImpl(APTFileBuffer buffer,
            PreprocHandler ppHandler,
            final ClankDriver.ClankPreprocessorCallback callback,
            final org.netbeans.modules.cnd.support.Interrupter interrupter) {
        try {
            ClankIncludeHandlerImpl includeHandler = (ClankIncludeHandlerImpl) ppHandler.getIncludeHandler();
            ClankDriver.APTTokenStreamCache cached = includeHandler.getCachedTokens();
            assert cached != null;
            CndUtils.assertTrueInConsole(!cached.hasTokenStream(), "Have to use ClankDriver.extractTokenStream(ppHandler) if TS is required");
            // TODO: prepare buffers mapping
            CharSequence path = buffer.getAbsolutePath();
            if (CndUtils.isDebugMode()) {
              byte[] bytes = toBytes(buffer.getCharBuffer());
              assert bytes != null;
            }
            // prepare params to run preprocessor
            ClankRunPreprocessorSettings settings = new ClankRunPreprocessorSettings();
            settings.WorkName = path;
            settings.KeepCommentsTokens = false;
            settings.GenerateDiagnostics = true;
            settings.PrettyPrintDiagnostics = false;
            settings.PrintDiagnosticsOS = llvm.nulls();
            settings.TraceClankStatistics = false;
            ClankPPCallback.CancellableInterrupter canceller = new ClankPPCallback.CancellableInterrupter(interrupter);
            settings.cancelled = canceller;
            ClankPPCallback fileTokensCallback = new ClankPPCallback(ppHandler, llvm.errs(), callback, canceller);
            settings.IncludeInfoCallbacks = fileTokensCallback;
            ClankCompilationDataBase db = APTToClankCompilationDB.convertPPHandler(ppHandler, path);
            ClankPreprocessorServices.preprocess(Collections.singleton(db), settings);
            return true;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    private static byte[] toBytes(char[] chars) {
        byte[] asciis = new byte[chars.length];
        for (int i = 0; i < asciis.length; i++) {
            asciis[i] = NativePointer.$(chars[i]);
        }
        return asciis;
    }

    public static ClankDriver.APTTokenStreamCache extractTokenStream(PreprocHandler ppHandler) {
        ClankIncludeHandlerImpl includeHandler = (ClankIncludeHandlerImpl) ppHandler.getIncludeHandler();
        ClankDriver.APTTokenStreamCache cached = includeHandler.getCachedTokens();
        return cached;
    }

    public static final class ArrayBasedAPTTokenStream implements APTTokenStream, TokenStream {

        private int index;
        private final int lastIndex;
        private final APTToken[] tokens;

        public ArrayBasedAPTTokenStream(APTToken[] tokens) {
            this.tokens = tokens;
            this.lastIndex = tokens.length;
            this.index = 0;
        }

        @Override
        public APTToken nextToken() {
            if (index < lastIndex) {
                return tokens[index++];
            } else {
                return APTUtils.EOF_TOKEN;
            }
        }

        @Override
        public String toString() {
            return APTUtils.debugString(new ArrayBasedAPTTokenStream(tokens)).toString();
        }
    }
}
