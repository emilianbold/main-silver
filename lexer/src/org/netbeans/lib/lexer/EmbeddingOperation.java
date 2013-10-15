/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.lib.lexer;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.inc.TokenChangeInfo;
import org.netbeans.lib.lexer.inc.TokenHierarchyEventInfo;
import org.netbeans.lib.lexer.inc.TokenHierarchyUpdate;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.JoinToken;
import org.netbeans.spi.lexer.EmbeddingPresence;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;

/**
 * Various static methods for embedding maintenance.
 *
 * @author Miloslav Metelka
 */
public class EmbeddingOperation {
    
    // -J-Dorg.netbeans.lib.lexer.EmbeddingOperation.level=FINER
    private static final Logger LOG = Logger.getLogger(EmbeddingOperation.class.getName());

    /**
     * Get embedded token list.
     *
     * @param tokenList non-null token list in which the token for which the
     * embedding should be obtained resides.
     * @param index &gt;=0 index of the token in the token list where the
     * embedding should be obtained.
     * @param language whether only language embeddding of the particular
     * language was requested. It may be null if any embedding should be
     * returned.
     * @param initTokensInNew true if tokens should be created when a new ETL
     * gets created. False in case this is called from TokenListList to grab
     * ETLs for sections joining.
     */
    public static <T extends TokenId, ET extends TokenId> EmbeddedTokenList<T,ET> embeddedTokenList(
            TokenList<T> tokenList, int index, Set<Language<?>> embeddedLanguages, boolean initTokensInNew)
    {
        TokenList<?> rootTokenList = tokenList.rootTokenList();
        TokenOrEmbedding<T> tokenOrEmbedding = tokenList.tokenOrEmbedding(index);
//        @SuppressWarnings("unchecked")
        EmbeddedTokenList<T,?> existingEtl = tokenOrEmbedding.embedding();
        AbstractToken<T> token = tokenOrEmbedding.token();
        if (token.isFlyweight()) {
            return null;
        }
        if (token.getClass() == JoinToken.class) {
            // Currently do not allow to create embedding over token that is physically joined
            return null;
        }
        // Now either existingEtl == null for no embedding yet or linked list of embedded token lists
        // needs to be processed to find the embedded token list for requested language.
        if (existingEtl != null) { // No embedding yet
            while (true) {
                if (embeddedLanguages == null || embeddedLanguages.contains(existingEtl.languagePath().innerLanguage())) {
                    @SuppressWarnings("unchecked")
                    EmbeddedTokenList<T,ET> etlUC = (EmbeddedTokenList<T,ET>) existingEtl;
                    return etlUC;
                }
                EmbeddedTokenList<T,?> next = existingEtl.nextEmbeddedTokenList();
                if (next == null) {
                    break;
                }
                existingEtl = next;
            }
        }
        // Requested etl not found
        if (token.isNoDefaultEmbedding()) {
            return null;
        }
        // If the tokenList is removed then do not create the embedding
        if (tokenList.isRemoved()) {
            return null;
        }

        // Attempt to create the default embedding
        EmbeddingPresence ep;
        if (existingEtl != null) {
            ep = null;
        } else { // No embedding was created yet
            // Check embedding presence
            ep = LexerUtilsConstants.innerLanguageOperation(tokenList.languagePath()).embeddingPresence(token.id());
            if (ep == EmbeddingPresence.NONE) {
                return null;
            }
        }

        LanguagePath languagePath = tokenList.languagePath();
        LanguageHierarchy<T> languageHierarchy = LexerUtilsConstants.innerLanguageHierarchy(languagePath);
        @SuppressWarnings("unchecked")
        LanguageEmbedding<ET> embedding = (LanguageEmbedding<ET>) LexerUtilsConstants.findEmbedding(
                languageHierarchy, token, languagePath, tokenList.inputAttributes());
        if (embedding != null) {
            // Update the embedding presence ALWAYS_QUERY
            if (ep == EmbeddingPresence.CACHED_FIRST_QUERY) {
                LexerUtilsConstants.innerLanguageOperation(tokenList.languagePath()).
                        setEmbeddingPresence(token.id(), EmbeddingPresence.ALWAYS_QUERY);
            }

            // Check whether the token contains enough text to satisfy embedding's start and end skip lengths
            if ((embeddedLanguages != null && !embeddedLanguages.contains(embedding.language()))
                    || embedding.startSkipLength() + embedding.endSkipLength() > token.length()) {
                return null;
            }

            LanguagePath embeddedLanguagePath = LanguagePath.get(languagePath,
                    embedding.language());
            EmbeddedTokenList<T,ET> etl = new EmbeddedTokenList<T, ET>(rootTokenList, embeddedLanguagePath, embedding);
            
            etl.reinit(token, token.offset(null), rootTokenList.modCount());

            if (existingEtl == null) {
                tokenList.setTokenOrEmbedding(index, etl);
            } else {
                existingEtl.setNextEmbeddedTokenList(etl);
            }

            if (initTokensInNew) {
                TokenHierarchyOperation operation = rootTokenList.tokenHierarchyOperation();
                if (embedding.joinSections()) {
                    // Init corresponding TokenListList
                    operation.tokenListList(embeddedLanguagePath);
                } else { // sections not joined
                    // Check that there is no TLL in this case.
                    // If there would be one it would already have to run through its constructor
                    // which should have collected all the ETLs already (with initTokensInNew==false)
                    // and init tokens explicitly.
                    // Thus the following assert should always pass.
                    assert (operation.existingTokenListList(embeddedLanguagePath) == null);
                    etl.initAllTokens();
                }
            }

            if (LOG.isLoggable(Level.FINE)) {
                StringBuilder sb = new StringBuilder(200);
                sb.append("@@@@@@@@@@ NATURAL-EMBEDDING-CREATED EC-"); // NOI18N
                LexerUtilsConstants.appendIdentityHashCode(sb, etl);
                sb.append(" ROOT-"); // NOI18N
                LexerUtilsConstants.appendIdentityHashCode(sb, rootTokenList);
                sb.append(" for ").append(embeddedLanguagePath.mimePath()). // NOI18N
                        append(", ").append(embedding).append(": "). // NOI18N
                        append(existingEtl.dumpInfo(null)).append(", initTokensInNew="). // NOI18N
                        append(initTokensInNew).append('\n');
                LOG.fine(sb.toString());
                if (LOG.isLoggable(Level.FINER)) { // Include stack trace of the creation
                    LOG.log(Level.INFO, "Natural embedding created by:", new Exception()); // NOI18N
                }
            }

            return etl;

        } else { // No default embedding
            AbstractToken<T> replacementToken = token.markNoDefaultEmbedding();
            if (replacementToken != null) {
                tokenList.setTokenOrEmbedding(index, replacementToken);
            }

            // Update embedding presence to NONE
            if (ep == EmbeddingPresence.CACHED_FIRST_QUERY) {
                LexerUtilsConstants.innerLanguageOperation(tokenList.languagePath()).
                        setEmbeddingPresence(token.id(), EmbeddingPresence.NONE);
            }
        }

        return null;
    }

    /**
     * Create custom embedding.
     *
     * @param tokenList non-null token list in which the token for which the
     * embedding should be created resides.
     * @param index &gt;=0 index of the token in the token list where the
     * embedding should be created.
     * @param embeddedLanguage non-null embedded language.
     * @param startSkipLength &gt;=0 number of characters in an initial part of
     * the token for which the language embedding is being create that should be
     * excluded from the embedded section. The excluded characters will not be
     * lexed and there will be no tokens created for them.
     * @param endSkipLength &gt;=0 number of characters at the end of the token
     * for which the language embedding is defined that should be excluded from
     * the embedded section. The excluded characters will not be lexed and there
     * will be no tokens created for them.
     * @param joinSections whether the embedding joins sections. Currently if
     * this value is false but there is a joining token list list for this
     * embedding then this parameter will be updated to true automatically.
     */
    public static <T extends TokenId, ET extends TokenId> boolean createEmbedding(
            TokenList<T> tokenList, int index, Language<ET> embeddedLanguage,
            int startSkipLength, int endSkipLength, boolean joinSections)
    {
        if (embeddedLanguage == null) {
            throw new IllegalArgumentException("embeddedLanguage parameter cannot be null"); // NOI18N
        }
        TokenList<?> rootTokenList = tokenList.rootTokenList();
        // Only create embedddings for valid operations so not e.g. for removed token list
        
        TokenHierarchyEventInfo eventInfo;
        // Check TL.isRemoved() under syncing of rootTokenList
        if (tokenList.isRemoved()) { // Do not create embedding for removed TLs
            return false;
        }
        // If not removed then THO should be non-null
        TokenHierarchyOperation<?,?> tokenHierarchyOperation = tokenList.tokenHierarchyOperation();
        tokenHierarchyOperation.ensureWriteLocked();

        // Check presence of token list list for the embedded language path
        // If it exists and it's joining sections then the requested embedding must also join sections
        // even if the joinSections parameter would be false.
        LanguagePath languagePath = tokenList.languagePath();
        LanguagePath embeddedLanguagePath = LanguagePath.get(languagePath, embeddedLanguage);
        TokenListList<ET> tll = tokenHierarchyOperation.existingTokenListList(embeddedLanguagePath);
        if (tll != null && tll.joinSections()) {
            joinSections = true;
        }
        // Add the new embedding as the first one in the single-linked list
        LanguageEmbedding<ET> embedding = LanguageEmbedding.create(embeddedLanguage,
                startSkipLength, endSkipLength, joinSections);
        tokenHierarchyOperation.addLanguagePath(embeddedLanguagePath);
        // Make the embedded token list to be the first in the list

        TokenOrEmbedding<T> tokenOrEmbedding = tokenList.tokenOrEmbedding(index);
        EmbeddedTokenList<T,?> existingEtl = tokenOrEmbedding.embedding();
        if (existingEtl != null) {
            while (true) {
                if (embeddedLanguagePath == existingEtl.languagePath()) {
                    return true;
                }
                EmbeddedTokenList<T,?> next = existingEtl.nextEmbeddedTokenList();
                if (next == null) {
                    break;
                }
                existingEtl = next;
            }
        }
        AbstractToken<T> token = tokenOrEmbedding.token();
        if (startSkipLength + endSkipLength > token.length()) { // Check for appropriate size
            return false;
        }
        if (token.isFlyweight()) { // embedding cannot exist for this flyweight token
            return false;
        }

        int tokenStartOffset = token.offset(null);
        EmbeddedTokenList<T,ET> etl = new EmbeddedTokenList<T, ET>(rootTokenList, embeddedLanguagePath, embedding);
        etl.reinit(token, tokenStartOffset, rootTokenList.modCount());

        if (existingEtl == null) {
            tokenList.setTokenOrEmbedding(index, etl);
        } else {
            existingEtl.setNextEmbeddedTokenList(etl);
        }

        // Fire the embedding creation to the clients
        // Threading model may need to be changed if necessary
        eventInfo = new TokenHierarchyEventInfo(
                tokenHierarchyOperation,
                TokenHierarchyEventType.EMBEDDING_CREATED,
                tokenStartOffset, 0, "", 0);
        eventInfo.setMaxAffectedEndOffset(tokenStartOffset + token.length());

        if (tll != null) {
            // Update tll by embedding creation
            // etl.initAllTokens() will be called by TokenListListUpdate.collectAddedEmbeddings()
            new TokenHierarchyUpdate(eventInfo).updateCreateOrRemoveEmbedding(etl, true);
        } else { // tll == null
            if (embedding.joinSections()) {
                // Force token list list creation only when joining sections
                tll = tokenHierarchyOperation.tokenListList(etl.languagePath());
            } else {
                // Force initialization of the tokens of the ETL
                etl.initAllTokens();
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("@@@@@@@@@@ EXPLICIT-EMBEDDING-CREATED for " + embeddedLanguagePath.mimePath()
                    + ", " + embedding + ": " + etl.dumpInfo(null) + '\n');
            if (LOG.isLoggable(Level.FINER)) { // Include stack trace of the creation
                LOG.log(Level.INFO, "Explicit embedding created by:", new Exception());
            }
//                tokenHierarchyOperation.ensureConsistency();
        }

        // Construct outer token change info
        TokenChangeInfo<T> info = new TokenChangeInfo<T>(tokenList);
        info.setIndex(index);
        info.setOffset(tokenStartOffset);
        //info.setAddedTokenCount(0);
        eventInfo.setTokenChangeInfo(info);

        TokenChangeInfo<ET> embeddedInfo = new TokenChangeInfo<ET>(etl);
        embeddedInfo.setIndex(0);
        embeddedInfo.setOffset(tokenStartOffset + embedding.startSkipLength());
        // Should set number of added tokens directly?
        //  - would prevent further lazy embedded lexing so leave to zero for now
        //info.setAddedTokenCount(0);
        info.addEmbeddedChange(embeddedInfo);

        // Fire the change
        tokenHierarchyOperation.fireTokenHierarchyChanged(eventInfo);
        return true;
    }

    public static <T extends TokenId, ET extends TokenId> boolean removeEmbedding(
            TokenList<T> tokenList, int index, Language<ET> embeddedLanguage) {
        TokenList<?> rootTokenList = tokenList.rootTokenList();
        // Only create embedddings for valid operations so not e.g. for removed token list
        // Check TL.isRemoved() under syncing of rootTokenList
        if (tokenList.isRemoved()) { // Do not remove embedding for removed TLs
            return false;
        }
        // If TL is not removed then THO should be non-null
        TokenHierarchyOperation<?, ?> tokenHierarchyOperation = tokenList.tokenHierarchyOperation();
        tokenHierarchyOperation.ensureWriteLocked();

        TokenOrEmbedding<T> tokenOrEmbedding = tokenList.tokenOrEmbedding(index);
        @SuppressWarnings("unchecked")
        EmbeddedTokenList<T,?> etl = tokenOrEmbedding.embedding();
        if (etl != null) {
            int rootModCount = rootTokenList.modCount();
            EmbeddedTokenList<T,?> lastEtl = null;
            while (etl != null) {
                etl.updateModCount(rootModCount);
                if (embeddedLanguage == etl.languagePath().innerLanguage()) {
                    // The embedding with the given language exists
                    // Remove it from the chain
                    EmbeddedTokenList<T, ?> next = etl.nextEmbeddedTokenList();
                    if (lastEtl == null) { // etl is first in list
                        if (next == null) {
                            tokenList.setTokenOrEmbedding(index, etl.token());
                        } else {
                            tokenList.setTokenOrEmbedding(index, next);
                        }
                    } else { // etl in middle of chain
                        lastEtl.setNextEmbeddedTokenList(next);
                    }
                    etl.setNextEmbeddedTokenList(null);
                    // Do not increase the version of the hierarchy since
                    // all the existing token sequences would be invalidated.
                    // Instead invalidate only TSes for the etl only and all its children.
                    // State that the removed embedding was not default - should not matter anyway
                    etl.markRemoved();
                    etl.markChildrenRemovedDeep();

                    // Fire the embedding creation to the clients
                    int startOffset = etl.branchTokenStartOffset();
                    TokenHierarchyEventInfo eventInfo = new TokenHierarchyEventInfo(
                            tokenHierarchyOperation,
                            TokenHierarchyEventType.EMBEDDING_REMOVED,
                            startOffset, 0, "", 0);
                    eventInfo.setMaxAffectedEndOffset(startOffset + etl.token().length());
                    // Construct outer token change info
                    TokenChangeInfo<T> info = new TokenChangeInfo<T>(tokenList);
                    info.setIndex(index);
                    info.setOffset(startOffset);
                    //info.setAddedTokenCount(0);
                    eventInfo.setTokenChangeInfo(info);

                    @SuppressWarnings("unchecked")
                    EmbeddedTokenList<T,ET> etlET = (EmbeddedTokenList<T,ET>) etl;
                    TokenChangeInfo<ET> embeddedInfo = new TokenChangeInfo<ET>(etlET);
                    embeddedInfo.setIndex(0);
                    embeddedInfo.setOffset(startOffset + etl.languageEmbedding().startSkipLength());
                    // For now do not set the removed contents (requires RemovedTokenList)
                    info.addEmbeddedChange(embeddedInfo);

                    // Check for presence of etl in a token list list
                    TokenListList tll = tokenHierarchyOperation.existingTokenListList(etl.languagePath());
                    if (tll != null) {
                        // update-status already called
                        new TokenHierarchyUpdate(eventInfo).updateCreateOrRemoveEmbedding(etl, false);
                    }

                    // Fire the change
                    tokenHierarchyOperation.fireTokenHierarchyChanged(eventInfo);
                    return true;
                }
                etl = etl.nextEmbeddedTokenList();
            }
        }
        return false;
    }

}