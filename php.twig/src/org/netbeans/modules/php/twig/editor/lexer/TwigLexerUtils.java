/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.twig.editor.lexer;

import java.util.List;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class TwigLexerUtils {

    private TwigLexerUtils() {
    }

    public static TokenSequence<? extends TwigTokenId> getTwigMarkupTokenSequence(final Snapshot snapshot, final int offset) {
        return getTokenSequence(snapshot.getTokenHierarchy(), offset, TwigTokenId.language());
    }

    public static <L> TokenSequence<? extends L> getTokenSequence(final TokenHierarchy<?> th, final int offset, final Language<? extends L> language) {
        TokenSequence<? extends L> ts = th.tokenSequence(language);
        if (ts == null) {
            List<TokenSequence<?>> list = th.embeddedTokenSequences(offset, true);
            for (TokenSequence t : list) {
                if (t.language() == language) {
                    ts = t;
                    break;
                }
            }
            if (ts == null) {
                list = th.embeddedTokenSequences(offset, false);
                for (TokenSequence t : list) {
                    if (t.language() == language) {
                        ts = t;
                        break;
                    }
                }
            }
        }
        return ts;
    }

    public static boolean isDelimiter(final TwigTokenId tokenId) {
        return TwigTokenId.T_TWIG_VAR_START.equals(tokenId) || TwigTokenId.T_TWIG_BLOCK_START.equals(tokenId) || isEndingDelimiter(tokenId);
    }

    public static boolean isEndingDelimiter(final TwigTokenId tokenId) {
        return TwigTokenId.T_TWIG_BLOCK_END.equals(tokenId) || TwigTokenId.T_TWIG_VAR_END.equals(tokenId);
    }

}
