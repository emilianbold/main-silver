/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.api.editor;

import org.openide.filesystems.FileObject;

/**
 * Class representing a PHP local variable.
 * @since 1.13
 * @author Tomas Mysik
 */
public final class PhpVariable extends PhpBaseElement {

    public PhpVariable(String name, String fullyQualifiedName, String description) {
        super(name, fullyQualifiedName, description);
    }

    /**
     * @since 1.25
     */
    public PhpVariable(String name, String fullyQualifiedName, FileObject file) {
        super(name, fullyQualifiedName, file);
    }

    /**
     * @since 1.28
     */
    public PhpVariable(String name, String fullyQualifiedName, FileObject file, int offset) {
        super(name, fullyQualifiedName, file, offset, null);
    }

    /**
     * @since 1.32
     */
    public PhpVariable(String name, PhpClass type, FileObject file, int offset) {
        super(name, null, type, file, offset, null);
    }

    public PhpVariable(String name, String fullyQualifiedName) {
        super(name, fullyQualifiedName);
    }

    /**
     * @since 1.32
     */
    public PhpVariable(String name, PhpClass type) {
        super(name, type);
    }

    public PhpVariable(String name, String fullyQualifiedName, int offset, String description) {
        super(name, fullyQualifiedName, offset, description);
    }

    public PhpVariable(String name, String fullyQualifiedName, int offset) {
        super(name, fullyQualifiedName, offset);
    }
}
