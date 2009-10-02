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

package org.netbeans.modules.php.api.phpmodule;

import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Properties of {@link PhpModule}. This class is used by PHP frameworks
 * to provide default values or to get current properties of the PHP module
 * (please note that not all the properties are provided).
 * @author Tomas Mysik
 */
public final class PhpModuleProperties {
    private final FileObject tests;
    private final FileObject webRoot;
    private final FileObject indexFile;
    private final String url;

    public PhpModuleProperties() {
        this(new PhpModulePropertiesData());
    }

    private PhpModuleProperties(PhpModulePropertiesData data) {
        tests = data.tests;
        webRoot = data.webRoot;
        indexFile = data.indexFile;
        url = data.url;
    }

    /**
     * Get test directory.
     * @return test directory
     */
    public FileObject getTests() {
        return tests;
    }

    /**
     * Return properties with configured test directory.
     * <p>
     * All other properties of the returned properties are inherited from
     * <code>this</code>.
     *
     * @param tests test directory
     * @return new properties with configured test directory
     */
    public PhpModuleProperties setTests(FileObject tests) {
        Parameters.notNull("tests", tests);
        return new PhpModuleProperties(new PhpModulePropertiesData(this).setTests(tests));
    }

    /**
     * Get web root directory.
     * @return web root directory
     */
    public FileObject getWebRoot() {
        return webRoot;
    }

    /**
     * Return properties with configured web root directory.
     * <p>
     * All other properties of the returned properties are inherited from
     * <code>this</code>.
     *
     * @param webRoot web root directory
     * @return new properties with configured web root directory
     */
    public PhpModuleProperties setWebRoot(FileObject webRoot) {
        Parameters.notNull("webRoot", webRoot);
        return new PhpModuleProperties(new PhpModulePropertiesData(this).setWebRoot(webRoot));
    }

    /**
     * Get index file.
     * @return index file
     */
    public FileObject getIndexFile() {
        return indexFile;
    }

    /**
     * Return properties with configured index file.
     * <p>
     * All other properties of the returned properties are inherited from
     * <code>this</code>.
     *
     * @param indexFile index file
     * @return new properties with configured index file
     */
    public PhpModuleProperties setIndexFile(FileObject indexFile) {
        Parameters.notNull("indexFile", indexFile);
        return new PhpModuleProperties(new PhpModulePropertiesData(this).setIndexFile(indexFile));
    }

    /**
     * Get project URL.
     * @return project URL
     * @since 1.19
     */
    public String getUrl() {
        return url;
    }

    /**
     * Return properties with configured project URL.
     * <p>
     * All other properties of the returned properties are inherited from
     * <code>this</code>.
     *
     * @param url project URL
     * @return new properties with configured project URL
     * @since 1.19
     */
    public PhpModuleProperties setUrl(String url) {
        Parameters.notNull("url", url);
        return new PhpModuleProperties(new PhpModulePropertiesData(this).setUrl(url));
    }

    private static final class PhpModulePropertiesData {
        FileObject tests;
        FileObject webRoot;
        FileObject indexFile;
        String url;

        PhpModulePropertiesData() {
        }

        PhpModulePropertiesData(PhpModuleProperties properties) {
            tests = properties.getTests();
            webRoot = properties.getWebRoot();
            indexFile = properties.getIndexFile();
            url = properties.getUrl();
        }

        PhpModulePropertiesData setTests(FileObject tests) {
            this.tests = tests;
            return this;
        }

        PhpModulePropertiesData setWebRoot(FileObject webRoot) {
            this.webRoot = webRoot;
            return this;
        }

        PhpModulePropertiesData setIndexFile(FileObject indexFile) {
            this.indexFile = indexFile;
            return this;
        }

        PhpModulePropertiesData setUrl(String url) {
            this.url = url;
            return this;
        }
    }
}
