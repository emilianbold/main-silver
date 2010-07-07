/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.gotodeclaration.matcher;

import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilterBuilder;
import org.netbeans.spi.jumpto.support.NameMatcher;
import org.netbeans.spi.jumpto.type.SearchType;

/**
 * A factory that provides comparators
 * depending on SearchType
 *
 * @author Vladimir Kvashin
 */
public class NameMatcherFactory {
    
    public static CsmSelect.CsmFilter createNameFilter(String text, SearchType type) {
        final NameMatcher matcher = org.netbeans.spi.jumpto.support.NameMatcherFactory.createNameMatcher(text, type);
        if( matcher != null ) {
            CsmSelect.NameAcceptor acceptor = new CsmSelect.NameAcceptor() {
                @Override
                public boolean accept(CharSequence name) {
                    return matcher.accept(name.toString());
                }
            };
            CsmFilterBuilder filterBuilder = CsmSelect.getFilterBuilder();
            if (filterBuilder != null) {
                return filterBuilder.createNameFilter(acceptor);
            }
        }
        return null;
    }
}
