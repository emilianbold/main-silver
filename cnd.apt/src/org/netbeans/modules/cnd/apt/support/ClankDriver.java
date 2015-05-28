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
package org.netbeans.modules.cnd.apt.support;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.impl.support.clank.ClankDriverImpl;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.support.Interrupter;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class ClankDriver {

    private ClankDriver() {
    }

    public interface APTTokenStreamCache {

      int getFileIndex();

      int[] getSkippedRanges();

      TokenStream getTokenStream();

      boolean hasTokenStream();

      Collection<ClankPreprocessorDirective> getPreprocessorDirectives();
    }

    public static int extractFileIndex(PreprocHandler ppHandler) {
      return ClankDriverImpl.extractFileIndex(ppHandler);
    }

    public static APTTokenStreamCache extractTokenStream(PreprocHandler ppHandler) {
      return ClankDriverImpl.extractTokenStream(ppHandler);
    }

    public static APTTokenStreamCache extractPreparedCachedTokenStream(PreprocHandler ppHandler) {
      ClankDriverImpl.APTTokenStreamCacheImplementation cache = ClankDriverImpl.extractTokenStream(ppHandler);
      return cache.prepareCachesIfPossible();
    }

    public static boolean preprocess(APTFileBuffer buffer,
            PreprocHandler ppHandler,
            ClankPreprocessorCallback callback, Interrupter interrupter) {
        return ClankDriverImpl.preprocessImpl(buffer, ppHandler, callback, interrupter);
    }

    public interface ClankPreprocessorDirective {
      void setAnnotation(Object attr);
      Object getAnnotation();
      int getDirectiveStartOffset();
      int getDirectiveEndOffset();
    }

    // // #define or #undef directive
    public interface ClankMacroDirective extends ClankPreprocessorDirective {
      // #define or #undef
      boolean isDefined();

      /**
       * @return name of #define'd or #undef'ed macro
       */
      CharSequence getMacroName();

      /**
       * @return null for object-like macros, collection of parameter names for
       *        function-like macros
       */
      public List<CharSequence> getParameters();
    }

    public interface ClankErrorDirective extends ClankPreprocessorDirective {
      CharSequence getMessage();
      PreprocHandler.State getStateWhenMetErrorDirective();
    }
    
    public interface ClankInclusionDirective extends ClankPreprocessorDirective {
      ResolvedPath getResolvedPath();
      CharSequence getSpellingName();
      boolean isAngled();
    }

    public interface ClankPreprocessorCallback {
      /**
       *
       * @param directiveOwner
       * @param directive
       */
      void onInclusionDirective(ClankFileInfo directiveOwner, ClankInclusionDirective directive);

      /**
       * 
       * @param enteredFrom
       * @param enteredTo
       */
      void onEnter(ClankFileInfo enteredFrom, ClankFileInfo enteredTo);
      
      /**
       * return true to continue or false to stop preprocessing and exit
       * @param exitedFrom
       * @param exitedTo
       * @return true to continue, false to cancel
       */
      boolean onExit(ClankFileInfo exitedFrom, ClankFileInfo exitedTo);

      boolean needTokens();
      boolean needSkippedRanges();
      boolean needMacroExpansion();
      boolean needComments();
      
      /**
       * 
       * @param directiveOwner
       * @param directive
       */
      void onErrorDirective(ClankFileInfo directiveOwner, ClankErrorDirective directive);
    }

    public interface ClankFileInfo {
      CharSequence getFilePath();
      int getFileIndex();
      ClankInclusionDirective getInclusionDirective();
      int[] getSkippedRanges();
    }

    ////////////////////////////////////////////////////////////////////////////
    // state/cache related methods
    public static void invalidate(APTFileBuffer buffer) {
        ClankDriverImpl.invalidateImpl(buffer);
    }

    public static void invalidateAll() {
        ClankDriverImpl.invalidateAllImpl();
    }

    public static void close() {
        invalidateAll();
    }
}
