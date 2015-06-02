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
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.clang.tools.services.ClankCompilationDataBase;
import org.clang.tools.services.ClankPreprocessorServices;
import org.clang.tools.services.ClankRunPreprocessorSettings;
import org.clang.tools.services.support.ClangFileSystemProvider;
import org.clang.tools.services.support.PrintWriter_ostream;
import org.clank.support.NativePointer;
import org.clank.support.aliases.char$ptr;
import org.llvm.adt.StringRef;
import org.llvm.support.MemoryBuffer;
import org.llvm.support.llvm;
import org.llvm.support.raw_ostream;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenStream;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.spi.APTUnsavedBuffersProvider;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Vladimir Voskresensky
 */
public class ClankDriverImpl {

    public interface APTTokenStreamCacheImplementation extends ClankDriver.APTTokenStreamCache {

        APTTokenStreamCacheImplementation prepareCachesIfPossible();
    }

    static final boolean TRACE = false;
    
    static {
        ClangFileSystemProvider.setImplementation(new ClankFileSystemProviderImpl());
    }

    public static void invalidateImpl(CharSequence absPath) {
        if (APTTraceFlags.USE_CLANK) {
            ClankPreprocessorServices.invalidate(absPath);
        }
    }

    public static void invalidateImpl(APTFileBuffer buffer) {
        // TODO: split by file system?
        invalidateImpl(buffer.getAbsolutePath());
    }

    public static void invalidateAllImpl() {
        // TODO: split by file system?
        ClankPreprocessorServices.invalidateAll();
    }

    public static boolean preprocessImpl(APTFileBuffer buffer,
            PreprocHandler ppHandler,
            final ClankDriver.ClankPreprocessorCallback callback,
            final org.netbeans.modules.cnd.support.Interrupter interrupter) {
        try {
            // TODO: prepare buffers mapping
            CharSequence path = buffer.getAbsolutePath();
            if (CndUtils.isDebugMode()) {
                byte[] bytes = toBytes(path, buffer.getCharBuffer());
                assert bytes != null;
            }
            // prepare params to run preprocessor
            ClankRunPreprocessorSettings settings = new ClankRunPreprocessorSettings();
            settings.WorkName = path;
            boolean fortranFlavor = APTToClankCompilationDB.isFortran(ppHandler);
            settings.KeepCommentsTokens = callback.needComments() || fortranFlavor;
            settings.GenerateDiagnostics = true;
            PrintWriter printWriter = null;
            if (CndUtils.isUnitTestMode() && !fortranFlavor) {
                settings.PrettyPrintDiagnostics = true;
                printWriter = new PrintWriter(System.err);
                settings.PrintDiagnosticsOS = new PrintWriter_ostream(printWriter);
            } else {
                settings.PrettyPrintDiagnostics = false;
                settings.PrintDiagnosticsOS = llvm.nulls();
            }
            settings.TraceClankStatistics = false;
            ClankPPCallback.CancellableInterrupter canceller = new ClankPPCallback.CancellableInterrupter(interrupter);
            settings.cancelled = canceller;
            raw_ostream traceOS = CndUtils.isUnitTestMode() ? llvm.nulls() : llvm.errs();
            ClankPPCallback fileTokensCallback = new ClankPPCallback(ppHandler, traceOS, callback, canceller);
            settings.IncludeInfoCallbacks = fileTokensCallback;
            ClankCompilationDataBase db = APTToClankCompilationDB.convertPPHandler(ppHandler, path);
            Map<StringRef, MemoryBuffer> remappedBuffers = getRemappedBuffers();
            MemoryBuffer fileContent;
            StringRef file = new StringRef(path);
            if (fortranFlavor) {
                char[] chars = fixFortranTokens(buffer);
                fileContent = MemoryBufferImpl.create(chars);
            } else {
                char[] chars = buffer.getCharBuffer();
                fileContent = MemoryBufferImpl.create(chars);
            }
            remappedBuffers = new HashMap<StringRef, MemoryBuffer>(remappedBuffers);
            remappedBuffers.put(file, fileContent);
            ClankPreprocessorServices.preprocess(Collections.singleton(db), settings, remappedBuffers);
            return true;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    private static char[] fixFortranTokens(APTFileBuffer buffer) throws IOException {
        // Fortran has special string concatenation (//)
        // and a logical operators (.and.) which produces bad token stream.
        // The method replaces:
        // //     -> ~~
        // .not.  -> ^
        // .ne.   -> /=
        // .neqv. -> <>
        // .eq.   -> ==
        // .eqv.  -> ==
        // .gt.   -> >
        // .ge.   -> >=
        // .lt.   -> <
        // .le.   -> <=
        // .and.  -> &&
        // .or.   -> ||
        // The class APTFortranFilterEx converts tokens ~~, ^ and <> back to right Fortran tokens.
        char[] chars = buffer.getCharBuffer();
        int i = 0;
        while (true) {
            if (i >= chars.length - 1) {
                break;
            }
            if (i < chars.length - 1) {
                if (chars[i] == '/' && chars[i+1] == '/') {
                    chars[i] = '~';
                    chars[i+1] = '~';
                }
            }
            if (chars[i] == '.') {
                if (i < chars.length - 3) {
                    if ((chars[i+1] == 'n' || chars[i+1] == 'N') &&
                        (chars[i+2] == 'e' || chars[i+2] == 'E') &&
                         chars[i+3] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '/';
                        chars[i+2] = '=';
                        chars[i+3] = ' ';
                    } else if ((chars[i+1] == 'e' || chars[i+1] == 'E') &&
                               (chars[i+2] == 'q' || chars[i+2] == 'Q') &&
                                chars[i+3] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '=';
                        chars[i+2] = '=';
                        chars[i+3] = ' ';
                    } else if ((chars[i+1] == 'g' || chars[i+1] == 'G') &&
                               (chars[i+2] == 't' || chars[i+2] == 'T') &&
                                chars[i+3] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '>';
                        chars[i+2] = ' ';
                        chars[i+3] = ' ';
                    } else if ((chars[i+1] == 'g' || chars[i+1] == 'G') &&
                               (chars[i+2] == 'e' || chars[i+2] == 'E') &&
                                chars[i+3] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '>';
                        chars[i+2] = '=';
                        chars[i+3] = ' ';
                    } else if ((chars[i+1] == 'l' || chars[i+1] == 'L') &&
                               (chars[i+2] == 't' || chars[i+2] == 'T') &&
                                chars[i+3] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '<';
                        chars[i+2] = ' ';
                        chars[i+3] = ' ';
                    } else if ((chars[i+1] == 'l' || chars[i+1] == 'L') &&
                               (chars[i+2] == 'e' || chars[i+2] == 'E') &&
                                chars[i+3] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '<';
                        chars[i+2] = '=';
                        chars[i+3] = ' ';
                    } else if ((chars[i+1] == 'o' || chars[i+1] == 'O') &&
                               (chars[i+2] == 'r' || chars[i+2] == 'R') &&
                                chars[i+3] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '|';
                        chars[i+2] = '|';
                        chars[i+3] = ' ';
                    }
                }
                if (i < chars.length - 4) {
                    if ((chars[i+1] == 'a' || chars[i+1] == 'A') &&
                        (chars[i+2] == 'n' || chars[i+2] == 'N') &&
                        (chars[i+3] == 'd' || chars[i+3] == 'D') &&
                         chars[i+4] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '&';
                        chars[i+2] = '&';
                        chars[i+3] = ' ';
                        chars[i+4] = ' ';
                    } else if ((chars[i+1] == 'e' || chars[i+1] == 'E') &&
                        (chars[i+2] == 'q' || chars[i+2] == 'Q') &&
                        (chars[i+3] == 'v' || chars[i+3] == 'V') &&
                         chars[i+4] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '=';
                        chars[i+2] = '=';
                        chars[i+3] = ' ';
                        chars[i+4] = ' ';
                    } else if ((chars[i+1] == 'n' || chars[i+1] == 'N') &&
                               (chars[i+2] == 'o' || chars[i+2] == 'O') &&
                               (chars[i+3] == 't' || chars[i+3] == 'T') &&
                                chars[i+4] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '^';
                        chars[i+2] = ' ';
                        chars[i+3] = ' ';
                        chars[i+4] = ' ';
                    }
                }
                if (i < chars.length - 5) {
                    if ((chars[i+1] == 'n' || chars[i+1] == 'N') &&
                        (chars[i+2] == 'e' || chars[i+2] == 'E') &&
                        (chars[i+3] == 'q' || chars[i+3] == 'Q') &&
                        (chars[i+4] == 'v' || chars[i+3] == 'V') &&
                         chars[i+5] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '<';
                        chars[i+2] = '>';
                        chars[i+3] = ' ';
                        chars[i+4] = ' ';
                        chars[i+5] = ' ';
                    }
                }
            }
            i++;
        }
        return chars;
    }

    private static class MemoryBufferImpl extends MemoryBuffer {
    
        
        public static MemoryBufferImpl create(APTFileBuffer aptBuf) throws IOException {
            char[] chars = aptBuf.getCharBuffer();
            return create(chars);
        }

        public static MemoryBufferImpl create(char[] chars) throws IOException {
//            String s = new String(chars); // TODO: should it be optimized?
//            char$ptr start = NativePointer.create_char$ptr(s);
//            char$ptr end = start.$add(s.length());

            CharBuffer cb = CharBuffer.wrap(chars);
            ByteBuffer bb = getUTF8Charset().encode(cb);
            // we need to add a trailing zero
            byte[] array;
            int nullTermIndex = bb.limit();
            if (bb.limit() < bb.capacity()) {
                // expand existing to keep \0
                bb.limit(bb.limit() + 1);
                bb.position(nullTermIndex);
                bb.put((byte) '\0');
                array = bb.array();
            } else {
                // have to create new to keep \0
                array = new byte[nullTermIndex + 1];
                System.arraycopy(bb.array(), 0, array, 0, nullTermIndex);
                array[nullTermIndex] = (byte) '\0';
            }
            // NB: the above adds a treailing zero. If you don't want this, use Arrays.copyOfRange instead
            //byte[] res = copyOfRange(bb.array(), bb.position(), bb.limit());
            //return res;    
            char$ptr start = NativePointer.create_char$ptr(array);
            char$ptr end = start.$add(nullTermIndex);
            return new MemoryBufferImpl(start, end, true);
        }

        private MemoryBufferImpl(char$ptr start, char$ptr end, boolean RequiresNullTerminator) {
            super();
            init(start, end, RequiresNullTerminator);
        }

        @Override
        public BufferKind getBufferKind() {        
            return BufferKind.MemoryBuffer_Malloc;
        }        
        
        private static volatile Charset UTF8Charset = null;

        private static Charset getUTF8Charset() {
            if (UTF8Charset == null) {
                UTF8Charset = Charset.forName("UTF-8"); //NOI18N
            }
            return UTF8Charset;
        }

//        /**
//         * The same as Arrays.copyOfRange, but adds a trailing zero
//         */
//        public static byte[] copyOfRange(byte[] original, int from, int to) {
//            int newLength = to - from;
//            if (newLength < 0) {
//                throw new IllegalArgumentException(from + " > " + to);
//            }
//            newLength++; // reserve space for '\0'
//            byte[] copy = new byte[newLength];
//            System.arraycopy(original, from, copy, 0,
//                    Math.min(original.length - from, newLength));
//            copy[newLength - 1] = 0;
//            return copy;
//        }        
    }
    
    private static Map<StringRef, MemoryBuffer> getRemappedBuffers() {
        Map<StringRef, MemoryBuffer> result = Collections.<StringRef, MemoryBuffer>emptyMap();
        APTUnsavedBuffersProvider provider = Lookup.getDefault().lookup(APTUnsavedBuffersProvider.class);
        if (provider != null) {
            Collection<APTFileBuffer> buffers = provider.getUnsavedBuffers();
            if (buffers != null && !buffers.isEmpty()) {
                result = new HashMap<StringRef, MemoryBuffer>();
                for (APTFileBuffer buf : buffers) {
                    StringRef path = new StringRef(buf.getAbsolutePath());
                    ClankMemoryBufferImpl mb;
                    try {
                        mb = ClankMemoryBufferImpl.create(buf);
                        result.put(path, mb);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex); //TODO: error processing!!!!
                    }
                }
            }
        }
        return result;
    }

    private static byte[] toBytes(CharSequence path, char[] chars) {
        byte[] asciis = new byte[chars.length];
        for (int i = 0; i < asciis.length; i++) {
            char c = chars[i];
            if (c >= 256) {
                CndUtils.assertTrueInConsole(CndUtils.isUnitTestMode(), path.toString(), ":could be problematic char[" + i + "] [" + c + "] " + (int)c);
            } else if (c >= 128) {
                CndUtils.assertTrueInConsole(CndUtils.isUnitTestMode(), path.toString(), ":could be problematic non-ANSII " + c);
            }
            asciis[i] = (byte)(c);
        }
        return asciis;
    }

    public static ClankDriverImpl.APTTokenStreamCacheImplementation extractTokenStream(PreprocHandler ppHandler) {
        ClankIncludeHandlerImpl includeHandler = (ClankIncludeHandlerImpl)ppHandler.getIncludeHandler();
        ClankDriverImpl.APTTokenStreamCacheImplementation cached = includeHandler.getCachedTokens();
        return cached;
    }

    public static int extractFileIndex(PreprocHandler ppHandler) {
        ClankIncludeHandlerImpl includeHandler = (ClankIncludeHandlerImpl)ppHandler.getIncludeHandler();
        return includeHandler.getInclStackIndex();
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
