/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.parsing.impl.indexing.lucene;

import java.net.URL;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.impl.indexing.lucene.util.Evictable;
import org.netbeans.modules.parsing.impl.indexing.lucene.util.EvictionPolicy;
import org.netbeans.modules.parsing.impl.indexing.lucene.util.LRUCache;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public class IndexCacheFactory {
    
    private static final Logger LOG = Logger.getLogger(IndexCacheFactory.class.getName());
    private static final IndexCacheFactory instance = new IndexCacheFactory();
    private final LRUCache<URL, Evictable> cache;

    private IndexCacheFactory() {
        this.cache = new LRUCache<URL, Evictable>(new DefaultPolicy());
    }

    public LRUCache<URL,Evictable> getCache() {
        return cache;
    }

    public static IndexCacheFactory getDefault() {
        return instance;
    }

    private static class DefaultPolicy implements EvictionPolicy<URL,Evictable> {
        private static final int DEFAULT_SIZE = 400;
        private static final boolean NEEDS_REMOVE =  Boolean.getBoolean("IndexCache.force") || (Utilities.isUnix() && !Utilities.isMac());  //NOI18N
        private static final int MAX_SIZE;
        static {
            int value = DEFAULT_SIZE;
            final String sizeStr = System.getProperty("IndexCache.size");   //NOI18N
            if (sizeStr != null) {
                try {
                    value = Integer.parseInt(sizeStr);
                } catch (NumberFormatException nfe) {
                    LOG.warning("Wrong (non integer) cache size: " + sizeStr);  //NOI18N
                }
            }            
            MAX_SIZE = value;
            LOG.fine("NEEDS_REMOVE: " + NEEDS_REMOVE +" MAX_SIZE: " + MAX_SIZE);    //NOI18N
        }

        public boolean shouldEvict(int size, URL key, Evictable value) {
            return NEEDS_REMOVE && size>MAX_SIZE;
        }
    }

}
