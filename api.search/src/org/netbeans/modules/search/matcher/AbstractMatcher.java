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
package org.netbeans.modules.search.matcher;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.search.MatchingObject.Def;
import org.openide.filesystems.FileObject;

/**
 * Base for all matchers used by the basic search provider.
 *
 * @author jhavlin
 */
public abstract class AbstractMatcher  {

    private long totalTime = 0;
    private int matchingFiles = 0;
    private int matchingItems = 0;
    private boolean strict = true;

    public AbstractMatcher() {
    }
    
    public final Def check(FileObject file, SearchListener listener) {
        long start = System.currentTimeMillis();
        Def def = checkMeasuredInternal(file, listener);
        long end = System.currentTimeMillis();
        if (def != null) {
            matchingFiles++;
            if (def.getTextDetails() != null
                    && !def.getTextDetails().isEmpty()) {
                matchingItems++;
            }
        }
        totalTime += end - start;
        return def;
    }

    protected abstract Def checkMeasuredInternal(FileObject file,
            SearchListener listener);

    public long getTotalTime() {
        return totalTime;
    }

    public int getMatchingFiles() {
        return matchingFiles;
    }

    public int getMatchingItems() {
        return matchingItems;
    }

    public abstract void terminate();

    public boolean isStrict() {
        return strict;
    }

    /**
     * @param strict True if an error should be raised for decoding errors
     * (unmappable character etc.), false if such error should be ignored.
     * Strict mode should be used when replacing.
     */
    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public CharsetDecoder prepareDecoder(Charset charset) {
        CharsetDecoder decoder = charset.newDecoder();
        if (strict) {
            decoder.onMalformedInput(CodingErrorAction.REPORT);
            decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        } else {
            decoder.onMalformedInput(CodingErrorAction.IGNORE);
            decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        }
        return decoder;
    }
}
