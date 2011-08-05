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
package org.netbeans.api.java.queries;

import java.io.IOException;
import java.net.URL;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 * A support for attaching source roots and javadoc roots to binary roots.
 * @since 1.35
 */
public final class SourceJavadocAttacher {

    private SourceJavadocAttacher() {}

    /**
     * Attaches a source root provided by the SPI {@link SourceJavadocAttacherImplementation}
     * to given binary root.
     * Has to be called by event dispatch thread as the SPI implementation may need to show
     * an UI to select source root(s).
     * @param root the binary root to attach sources to
     * @return true if the source root was successfully attached
     */
    public static boolean attachSources(@NonNull final URL root) {
        return attach(root,0);
    }

    /**
     * Attaches a javadoc root provided by the SPI {@link SourceJavadocAttacherImplementation}
     * to given binary root.
     * Has to be called by event dispatch thread as the SPI implementation may need to show
     * an UI to select javadoc root(s).
     * @param root the binary root to attach javadoc to
     * @return true if the javadoc root was successfully attached
     */
    public static boolean attachJavadoc(@NonNull final URL root) {
        return attach(root,1);
    }

    private static boolean attach(final URL root, final int mode) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Has to be called by EDT.");    //NOI18N
        }
        try {
            for (SourceJavadocAttacherImplementation attacher : Lookup.getDefault().lookupAll(SourceJavadocAttacherImplementation.class)) {
                final SourceJavadocAttacherImplementation.Result res =
                        mode == 0 ?
                            attacher.attachSources(root) :
                            attacher.attachJavadoc(root);
                if (res == SourceJavadocAttacherImplementation.Result.ATTACHED) {
                    return true;
                } else if (res == SourceJavadocAttacherImplementation.Result.CANCELED) {
                    return false;
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return false;
    }
}
