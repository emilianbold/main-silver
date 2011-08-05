/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.spi.java.queries;

import java.io.IOException;
import java.net.URL;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.SourceJavadocAttacher;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;


/**
 * A SPI for attaching source roots and javadoc roots to binary roots.
 * The implementations of this interface are registered in global {@link Lookup}
 * @see ServiceProvider
 * @author Tomas Zezula
 * @since 1.35
 */
public interface SourceJavadocAttacherImplementation {

    /**
     * Result of the attaching sources (javadoc) to binary root.
     */
    enum Result {
        /**
         * The source (javadoc) root was successfully attached to
         * the binary root.
         */
        ATTACHED,

        /**
         * User canceled the attaching, no other SPI provider
         * is called and {@link SourceJavadocAttacher} returns false.
         */
        CANCELED,

        /**
         * The SPI is not able to attach sources (javadoc) to given
         * binary root (it does not handle it), next SPI provider is
         * called.
         */
        UNSUPPORTED
    }


    /**
     * Attaches a source root provided by this SPI to given binary root.
     * Called by event dispatch thread, it's safe to show an UI to select source root(s).
     * @param root the binary root to attach sources to
     * @return {@link SourceJavadocAttacherImplementation.Result} the result of
     * attach operation.
     */
    @NonNull
    Result attachSources(@NonNull URL root) throws IOException;

    /**
     * Attaches a javadoc root provided by this SPI to given binary root.
     * Called by event dispatch thread, it's safe to show an UI to select javadoc root(s).
     * @param root the binary root to attach javadoc to
     * @return {@link SourceJavadocAttacherImplementation.Result} the result of
     * attach operation.
     */
    @NonNull
    Result attachJavadoc(@NonNull URL root) throws IOException;
}
