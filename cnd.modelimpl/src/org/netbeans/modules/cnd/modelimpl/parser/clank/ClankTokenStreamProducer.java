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
package org.netbeans.modules.cnd.modelimpl.parser.clank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.event.ChangeListener;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.apt.support.ClankDriver.ClankPreprocessorCallback;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.api.StartEntry;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.accessors.CsmCorePackageAccessor;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.IncludeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ErrorDirectiveImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBuffer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBufferFile;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FilePreprocessorConditionState;
import org.netbeans.modules.cnd.modelimpl.csm.core.Line2Offset;
import org.netbeans.modules.cnd.modelimpl.csm.core.PreprocessorStatePair;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.spi.TokenStreamProducer;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class ClankTokenStreamProducer extends TokenStreamProducer {
    private int[] skipped;

    private ClankTokenStreamProducer(FileImpl file, FileContent newFileContent) {
        super(file, newFileContent);
    }
    
    public static TokenStreamProducer createImpl(FileImpl file, FileContent newFileContent, boolean index) {
        return new ClankTokenStreamProducer(file, newFileContent);
    }

    @Override
    public TokenStream getTokenStreamOfIncludedFile(PreprocHandler.State includeOwnerState, CsmInclude include, Interrupter interrupter) {
        FileImpl fileImpl = getMainFile();
        ProjectBase projectImpl = fileImpl.getProjectImpl(true);
        if (projectImpl == null) {
          return null;
        }
        PreprocHandler ppHandler = projectImpl.createPreprocHandlerFromState(fileImpl.getAbsolutePath(), includeOwnerState);
        int fileOwnerIndex = ClankDriver.extractTokenStream(ppHandler).getFileIndex();
        // do preprocessing
        IncludeDirectiveTokensStreamCallback callback =
                new IncludeDirectiveTokensStreamCallback(ppHandler, fileOwnerIndex, include.getStartOffset());
        boolean tsFromClank = ClankDriver.preprocess(fileImpl.getBuffer(), ppHandler, callback, interrupter);
        if (!tsFromClank) {
            return null;
        }
        ClankDriver.APTTokenStreamCache tokStreamCache = callback.getPPOut();
        if (tokStreamCache == null) {
          return null;
        }
        skipped = new int[0];
        TokenStream tokenStream = tokStreamCache.getTokenStream();
        if (tokenStream == null) {
          return null;
        }
        return tokenStream;
    }

    @Override
    public TokenStream getTokenStream(boolean triggerParsingActivity,
            boolean filterOutComments, boolean applyLanguageFilter, Interrupter interrupter) {
        PreprocHandler ppHandler = getCurrentPreprocHandler();
        ClankDriver.APTTokenStreamCache tokStreamCache = ClankDriver.extractTokenStream(ppHandler);
        assert tokStreamCache != null;
        FileImpl fileImpl = getMainFile();
        if (!tokStreamCache.hasTokenStream()) {
          // do preprocessing
          FileTokenStreamCallback callback = new FileTokenStreamCallback(
                  ppHandler,
                  triggerParsingActivity,
                  filterOutComments,
                  fileImpl, getFileContent(),
                  tokStreamCache.getFileIndex());
          FileBuffer buffer = fileImpl.getBuffer();
          if (getFixCode() != null) {
              buffer = new FixedFileBuffer(buffer, getFixCode());
          }
          boolean tsFromClank = ClankDriver.preprocess(buffer, ppHandler, callback, interrupter);
          if (!tsFromClank) {
              return null;
          }
          tokStreamCache = callback.getPPOut();
          if (tokStreamCache == null) {
            return null;
          }
        }
        TokenStream tokenStream = tokStreamCache.getTokenStream();
        if (tokenStream == null) {
          return null;
        }
        if (triggerParsingActivity) {
          addPreprocessorDirectives(fileImpl, getFileContent(), tokStreamCache);
        }
        skipped = tokStreamCache.getSkippedRanges();
        if (applyLanguageFilter) {
          APTLanguageFilter languageFilter = fileImpl.getLanguageFilter(ppHandler.getState());
          tokenStream = languageFilter.getFilteredStream(new APTCommentsFilter(tokenStream));
        }
        return tokenStream;
    }

    @Override
    public FilePreprocessorConditionState release() {
        return CsmCorePackageAccessor.get().createPCState(getMainFile().getAbsolutePath(), skipped);
    }
    
    private static final class FileTokenStreamCallback implements ClankPreprocessorCallback {
        private final ProjectBase startProject;
        private final FileImpl startFile;
        private final PreprocHandler ppHandler;

        private final FileImpl stopFileImpl;
        private final FileContent fileContent;
        private final int stopAtIndex;
        private ClankDriver.APTTokenStreamCache foundTokens;

        private enum State {
          INITIAL,
          SEEN,
          EXITED
        }
        private State alreadySeenInterestedFileEnter = State.INITIAL;
        private boolean insideInterestedFile = false;
        private final boolean triggerParsingActivity;
        private final boolean filterOutComments;

        private List<FileImpl> curFiles = new ArrayList<FileImpl>();

        private FileTokenStreamCallback(
                PreprocHandler ppHandler,
                boolean triggerParsingActivity,
                boolean filterOutComments,
                FileImpl stopFileImpl, 
                FileContent fileContent,
                int stopAtIndex) {
            this.ppHandler = ppHandler;
            StartEntry startEntry = ppHandler.getIncludeHandler().getStartEntry();
            this.startProject = Utils.getStartProject(startEntry);
            this.startFile = Utils.getStartFile(ppHandler.getState());
            this.triggerParsingActivity = triggerParsingActivity;
            this.filterOutComments = filterOutComments;
            this.stopFileImpl = stopFileImpl;
            this.fileContent = fileContent;
            this.stopAtIndex = stopAtIndex;
        }

        boolean isTrace() {
          if (false && stopFileImpl.getName().toString().endsWith(".h")) {// NOI18N
            return true;
          }
          return false;
        }

        @Override
        public void onErrorDirective(ClankDriver.ClankFileInfo directiveOwner, ClankDriver.ClankErrorDirective directive) {
        }

        @Override
        public boolean needTokens() {
          return this.insideInterestedFile || !CLEAN_STATE;
        }

        @Override
        public boolean needSkippedRanges() {
          return this.insideInterestedFile || !CLEAN_STATE;
        }

        @Override
        public boolean needMacroExpansion() {
          return this.insideInterestedFile || !CLEAN_STATE;
        }

        @Override
        public boolean needComments() {
          return !filterOutComments;
        }
        
        @Override
        public void onInclusionDirective(ClankDriver.ClankFileInfo directiveOwner, ClankDriver.ClankInclusionDirective directive) {
            if ((alreadySeenInterestedFileEnter == State.SEEN) && triggerParsingActivity) {
              // let's resolve include as FileImpl
              ResolvedPath resolvedPath = directive.getResolvedPath();
              if (resolvedPath == null) {
                // broken #include path
                return;
              }
              FileImpl curFile = getCurFile(false);
              FileImpl includedFile = null;
              CharSequence path = resolvedPath.getPath();
              ProjectBase aStartProject = startProject;
              if (aStartProject != null) {
                  if (aStartProject.isValid()) {
                      ProjectBase inclFileOwner = aStartProject.getLibraryManager().resolveFileProjectOnInclude(aStartProject, curFile, resolvedPath);
                      if (inclFileOwner == null) {
                          assert false : "something wrong when parsing " + stopFileImpl + " from " + this.startProject;
                          if (aStartProject.getFileSystem() == resolvedPath.getFileSystem()) {
                              inclFileOwner = aStartProject;
                          }
                      }
                      assert inclFileOwner != null;
                      if (CndUtils.isDebugMode()) {
                          CndUtils.assertTrue(inclFileOwner.getFileSystem() == resolvedPath.getFileSystem(), "Different FS for " + path + ": " + inclFileOwner.getFileSystem() + " vs " + resolvedPath.getFileSystem()); // NOI18N
                      }
                      includedFile = inclFileOwner.prepareIncludedFile(aStartProject, path, ppHandler);
                      if (includedFile == null) {
                        assert false : "something wrong when including " + path + " from " + curFile;
                      }
                  } else {
                    assert false : "invalid start project when including " + path + " from " + curFile;
                  }
              } else {
                  APTUtils.LOG.log(Level.SEVERE, "FileTokenStreamCallback: file {0} without project!!!", new Object[]{path});// NOI18N
              }
              directive.setAnnotation(includedFile);
            }            
        }

        @Override
        public void onEnter(ClankDriver.ClankFileInfo enteredFrom, ClankDriver.ClankFileInfo enteredTo) {
            assert enteredTo != null;
            if (enteredTo.getFileIndex() == stopAtIndex) {
              assert alreadySeenInterestedFileEnter == State.INITIAL;
              alreadySeenInterestedFileEnter = State.SEEN;
              CndUtils.assertTrueInConsole(CharSequenceUtilities.textEquals(enteredTo.getFilePath(), stopFileImpl.getAbsolutePath()),
                      enteredTo + "\n vs. \n", stopFileImpl);// NOI18N
              if (triggerParsingActivity) {
                // we entered target file and after that we can
                // handle inclusive #includes
                curFiles.add(stopFileImpl);
              }
            } else {
              if ((alreadySeenInterestedFileEnter == State.SEEN) && triggerParsingActivity) {
                // let's keep stack of inner includes
                // then onExit post process headers wthich should be parsed
                FileImpl curFile = getCurFile(false);
                FileImpl includedFile = (FileImpl) enteredTo.getInclusionDirective().getAnnotation();
                if (includedFile != null) {
                  curFiles.add(includedFile);
                } else {
                  assert false : "something wrong when including " + enteredTo.getFilePath() + " from " + curFile;
                  curFiles.add(curFile);
                }
              }
            }
            insideInterestedFile = (enteredTo.getFileIndex() == stopAtIndex);
        }

        private FileImpl getCurFile(boolean pop) {
          assert curFiles.size() > 0;
          FileImpl curFile;
          if (pop) {
            curFile = curFiles.remove(curFiles.size() - 1);
          } else {
            curFile = curFiles.get(curFiles.size() - 1);
          }
          assert curFile != null;
          return curFile;
        }

        private ProjectBase getStartProject() {
          return startProject;
        }

        @Override
        public boolean onExit(ClankDriver.ClankFileInfo exitedFrom, ClankDriver.ClankFileInfo exitedTo) {
            assert exitedFrom != null;
            if (alreadySeenInterestedFileEnter == State.EXITED) {
              return false;
            } else if (alreadySeenInterestedFileEnter == State.INITIAL) {
              return true;
            }
            insideInterestedFile = (exitedTo != null) && (exitedTo.getFileIndex() == stopAtIndex);
            if (stopAtIndex == exitedFrom.getFileIndex()) {
              CndUtils.assertTrueInConsole(CharSequenceUtilities.textEquals(exitedFrom.getFilePath(), stopFileImpl.getAbsolutePath()) ,
                      "expected " + stopFileImpl.getAbsolutePath(), exitedFrom);// NOI18N
              foundTokens = ClankDriver.extractPreparedCachedTokenStream(ppHandler);
              assert foundTokens.hasTokenStream();
              // stop all activity
              alreadySeenInterestedFileEnter = State.EXITED;
              return false;
            } else if (triggerParsingActivity) {
              assert alreadySeenInterestedFileEnter == State.SEEN;
              try {
                assert ClankDriver.extractTokenStream(ppHandler).hasTokenStream();
                PreprocHandler.State inclState = ppHandler.getState();
                assert !inclState.isCleaned();
                FileImpl currentInclusion = getCurFile(true);
                if (currentInclusion != null) {
                  CharSequence inclPath = currentInclusion.getAbsolutePath();
                  ProjectBase inclFileOwner = currentInclusion.getProjectImpl(true);
                  ProjectBase aStartProject = getStartProject();
                  if (inclFileOwner.isDisposing() || aStartProject.isDisposing()) {
                    if (TraceFlags.TRACE_VALIDATION || TraceFlags.TRACE_MODEL_STATE) {
                      System.err.printf("onFileIncluded: %s file [%s] is interrupted on disposing project%n", inclPath, inclFileOwner.getName());
                    }
                  } else {
                    if (CLEAN_STATE) {
                      // FIXME: to regenerate headers' TS clean state
                      inclState = APTHandlersSupport.createCleanPreprocState(inclState);
                    }

                    FilePreprocessorConditionState pcState = CsmCorePackageAccessor.get().createPCState(inclPath, exitedFrom.getSkippedRanges());
                    PreprocessorStatePair ppStatePair = new PreprocessorStatePair(inclState, pcState);
                    inclFileOwner.postIncludeFile(aStartProject, currentInclusion, inclPath, ppStatePair, null);
                  }
                }
              } catch (Exception ex) {
                APTUtils.LOG.log(Level.SEVERE, "MyClankPreprocessorCallback: error on including {0}:%n{1}", new Object[]{exitedFrom.getFilePath(), ex});
                DiagnosticExceptoins.register(ex);
              }
            }
            return true;
        }        

        private ClankDriver.APTTokenStreamCache getPPOut() {
            return foundTokens;
        }

        private static final boolean CLEAN_STATE = false;
    }
    
    private static final class IncludeDirectiveTokensStreamCallback implements ClankPreprocessorCallback {
        private final PreprocHandler ppHandler;

        private final int includeFileOnwerIndex;
        private final int interestedClankIncludeDirectiveOffset;
        private ClankDriver.APTTokenStreamCache foundTokens = null;
        private boolean insideInterestedFile = false;

        private ClankDriver.ClankFileInfo includedFileInfo = null;

        private IncludeDirectiveTokensStreamCallback(PreprocHandler ppHandler,
                int includeFileOnwerIndex, int interestedIncludeDirectiveOffset) {
            this.ppHandler = ppHandler;
            this.includeFileOnwerIndex = includeFileOnwerIndex;
            // adjust to Clank offset
            this.interestedClankIncludeDirectiveOffset = interestedIncludeDirectiveOffset;
        }

        @Override
        public void onErrorDirective(ClankDriver.ClankFileInfo directiveOwner, ClankDriver.ClankErrorDirective directive) {
        }

        @Override
        public boolean needTokens() {
          return this.insideInterestedFile;
        }

        @Override
        public boolean needMacroExpansion() {
          return false;
        }

        @Override
        public boolean needComments() {
          return false;
        }

        @Override
        public boolean needSkippedRanges() {
          return false;
        }

        @Override
        public void onInclusionDirective(ClankDriver.ClankFileInfo directiveOwner, ClankDriver.ClankInclusionDirective directive) {

        }

        @Override
        public void onEnter(ClankDriver.ClankFileInfo enteredFrom, ClankDriver.ClankFileInfo enteredTo) {
            assert enteredTo != null;
            insideInterestedFile = false;
            // TODO: we can collect all included recursively, but not now
            if (enteredFrom != null && enteredFrom.getFileIndex() == includeFileOnwerIndex) {
              // entering from file owner into include directive
              if (enteredTo.getInclusionDirective().getDirectiveStartOffset() == this.interestedClankIncludeDirectiveOffset) {
                assert includedFileInfo == null : "seen twice? " + includedFileInfo;
                includedFileInfo = enteredTo;
                insideInterestedFile = true;
              }
            }
        }

        @Override
        public boolean onExit(ClankDriver.ClankFileInfo exitedFrom, ClankDriver.ClankFileInfo exitedTo) {
            if (foundTokens != null) {
              return false;
            }
            assert exitedFrom != null;
            if (exitedFrom == includedFileInfo) {
              // stop all activity on exit from interested include directive
              foundTokens = ClankDriver.extractPreparedCachedTokenStream(ppHandler);
              assert foundTokens.hasTokenStream();
              return false;
            }
            // gather when come back to interested file
            insideInterestedFile = (exitedTo == includedFileInfo);
            return true;
        }

        private ClankDriver.APTTokenStreamCache getPPOut() {
            return foundTokens;
        }
    }

    private static void addPreprocessorDirectives(FileImpl curFile, FileContent parsingFileContent, ClankDriver.APTTokenStreamCache cache) {
        assert parsingFileContent != null;
        assert curFile != null;
        assert cache != null;
        for (ClankDriver.ClankPreprocessorDirective cur : cache.getPreprocessorDirectives()) {
            if (cur instanceof ClankDriver.ClankInclusionDirective) {
                addInclude(curFile, parsingFileContent, (ClankDriver.ClankInclusionDirective)cur);
            } else if (cur instanceof ClankDriver.ClankErrorDirective) {
                addError(curFile, parsingFileContent, (ClankDriver.ClankErrorDirective)cur);
            } else if (cur instanceof ClankDriver.ClankMacroDirective) {
                addMacro(curFile, parsingFileContent, (ClankDriver.ClankMacroDirective)cur);
            } else {
              CndUtils.assertTrueInConsole(false, "unknown directive " + cur.getClass().getSimpleName() + " " + cur);
            }
        }
    }

    private static void addMacro(FileImpl curFile, FileContent parsingFileContent, ClankDriver.ClankMacroDirective ppDirective) {
        if (!ppDirective.isDefined()) {
            // only #define are handled by old model, not #undef
            return;
        }
        CsmMacro.Kind kind = CsmMacro.Kind.DEFINED;
        List<CharSequence> params = ppDirective.getParameters();
        CharSequence name = ppDirective.getMacroName();
        String body = "";
        int startOffset = ppDirective.getDirectiveStartOffset();
        int endOffset = ppDirective.getDirectiveEndOffset();
        MacroImpl impl = MacroImpl.create(name, params, body/*sb.toString()*/, curFile, startOffset, endOffset, kind);
        parsingFileContent.addMacro(impl);
    }

    private static void addError(FileImpl curFile, FileContent parsingFileContent, ClankDriver.ClankErrorDirective ppDirective) {
        CharSequence msg = ppDirective.getMessage();
        PreprocHandler.State state = ppDirective.getStateWhenMetErrorDirective();
        int start = ppDirective.getDirectiveStartOffset();
        int end = ppDirective.getDirectiveEndOffset();
        ErrorDirectiveImpl impl = ErrorDirectiveImpl.create(curFile, msg, new CsmOffsetableImpl(curFile, start, end), state);
        parsingFileContent.addError(impl);
    }

    private static void addInclude(FileImpl curFile, FileContent parsingFileContent, ClankDriver.ClankInclusionDirective ppDirective) {
        ResolvedPath resolvedPath = ppDirective.getResolvedPath();
        CharSequence fileName = ppDirective.getSpellingName();
        boolean system = ppDirective.isAngled();
        boolean broken = (resolvedPath == null);
        FileImpl includedFile = (FileImpl) ppDirective.getAnnotation();
        assert (includedFile == null) == broken : "broken " + broken + " vs. " + includedFile;
        int startOffset = ppDirective.getDirectiveStartOffset();
        int endOffset = ppDirective.getDirectiveEndOffset();
        IncludeImpl incl = IncludeImpl.create(fileName.toString(), system, false, includedFile, curFile, startOffset, endOffset);
        parsingFileContent.addInclude(incl, broken);
    }

    private static final class CsmOffsetableImpl implements CsmOffsetable {

        private final CsmFile file;
        private final int selectionStart;
        private final int selectionEnd;

        public CsmOffsetableImpl(CsmFile file, int selectionStart, int selectionEnd) {
            this.file = file;
            this.selectionStart = selectionStart;
            this.selectionEnd = selectionEnd;
        }

        @Override
        public CsmFile getContainingFile() {
            return file;
        }

        @Override
        public int getStartOffset() {
            return selectionStart;
        }

        @Override
        public int getEndOffset() {
            return selectionEnd;
        }

        @Override
        public Position getStartPosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Position getEndPosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getText() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class FixedFileBuffer implements FileBuffer {
        private final FileBuffer delegate;
        private final FixCode fixCode;
        private char[] res;
        private Line2Offset lines;

        private FixedFileBuffer(FileBuffer delegate, FixCode fixCode) {
            this.delegate = delegate;
            this.fixCode = fixCode;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isFileBased() {
            return delegate.isFileBased();
        }

        @Override
        public FileObject getFileObject() {
            return delegate.getFileObject();
        }

        @Override
        public CharSequence getUrl() {
            return delegate.getUrl();
        }

        @Override
        public String getText(int start, int end) throws IOException {
            return new String(getCharBuffer(), start, end - start);
        }

        @Override
        public CharSequence getText() throws IOException {
            return new FileBufferFile.MyCharSequence(getCharBuffer());
        }

        @Override
        public long lastModified() {
            return delegate.lastModified()+1;
        }

        @Override
        public long getCRC() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int[] getLineColumnByOffset(int offset) throws IOException {
            if (lines == null) {
                lines = new Line2Offset(getCharBuffer());
            }

            return lines.getLineColumnByOffset(offset);
        }

        @Override
        public int getLineCount() throws IOException {
            if (lines == null) {
                lines = new Line2Offset(getCharBuffer());
            }
            return lines.getLineCount();
        }

        @Override
        public int getOffsetByLineColumn(int line, int column) throws IOException {
            if (lines == null) {
                lines = new Line2Offset(getCharBuffer());
            }
            return lines.getOffsetByLineColumn(line, column);
        }

        @Override
        public CharSequence getAbsolutePath() {
            return delegate.getAbsolutePath();
        }

        @Override
        public FileSystem getFileSystem() {
            return delegate.getFileSystem();
        }

        @Override
        public char[] getCharBuffer() throws IOException {
            if (res == null) {
                char[] charBuffer = delegate.getCharBuffer();
                char[] patch = fixCode.getPatch().toCharArray();
                res = new char[charBuffer.length-(fixCode.getEndOffset()-fixCode.getStartOffset())+patch.length];
                System.arraycopy(charBuffer, 0, res, 0, fixCode.getStartOffset());
                System.arraycopy(patch, 0, res, fixCode.getStartOffset(), patch.length);
                System.arraycopy(charBuffer, fixCode.getEndOffset(), res, fixCode.getStartOffset()+patch.length, charBuffer.length - fixCode.getEndOffset());
            }
            return res;
        }

        @Override
        public BufferType getType() {
            return delegate.getType();
        }

    }
}
