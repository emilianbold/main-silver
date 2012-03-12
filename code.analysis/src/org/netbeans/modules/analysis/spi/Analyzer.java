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
package org.netbeans.modules.analysis.spi;

import java.awt.Image;
import java.util.Collection;
import java.util.Collections;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.analysis.SPIAccessor;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.Cancellable;
import org.openide.util.lookup.ServiceProvider;

/**A static analyzer. Called by the infrastructure on a given {@link Scope} to perform
 * the analysis and return the found warnings as {@link ErrorDescription}s.
 *
 * It is intended to be installed in the global lookup, using e.g. {@link ServiceProvider}.
 *
 * @author lahvac
 */
public interface Analyzer extends Cancellable {

    /**Perform the analysis over the {@link Scope} defined in the {@link Context}
     * given while constructing the {@link Analyzer}.
     *
     * @return the found warnings
     */
    public Iterable<? extends ErrorDescription> analyze();

    public static abstract class AnalyzerFactory {
        private final String id;
        private final String displayName;
        private final String iconPath;

        /**
         *
         * @param id a unique id of the analyzer
         * @param displayName the display name of the analyzer
         * @param iconPath a path to icon associated with this analyzer
         */
        public AnalyzerFactory(String id, String displayName, String iconPath) {
            this.id = id;
            this.displayName = displayName;
            this.iconPath = iconPath;
        }

        /**If additional modules are required to run the analysis (for the given {@code context}),
         * return their description.
         *
         * @param context over which the analysis is going to be performed
         * @return descriptions of the missing plugins, if any
         */
        public Collection<? extends MissingPlugin> requiredPlugins(Context context) {
            return Collections.emptyList();
        }

        public abstract Iterable<? extends WarningDescription> getWarnings();

        public abstract <D, C extends JComponent> CustomizerProvider<D, C> getCustomizerProvider();

        /**
         *
         * @param context containing the required {@link Scope}
         * @return
         */
        public abstract Analyzer createAnalyzer(Context context);
    }

    public static final class Context {
        private final Scope scope;
        private final Preferences settings;
        private final String singleWarningId;
        private final ProgressHandle progress;
        private final int bucketStart;
        private final int bucketSize;
        private int totalWork;

        Context(Scope scope, Preferences settings, String singleWarningId, ProgressHandle progress, int bucketStart, int bucketSize) {
            this.scope = scope;
            this.settings = settings;
            this.singleWarningId = singleWarningId;
            this.progress = progress;
            this.bucketStart = bucketStart;
            this.bucketSize = bucketSize;
        }

        public Scope getScope() {
            return scope;
        }

        public Preferences getSettings() {
            return settings;
        }

        public String getSingleWarningId() {
            return singleWarningId;
        }

        public void start(int workunits) {
            totalWork = workunits;
        }

        public void progress(String message, int unit) {
            progress.progress(message, computeProgress(unit));
        }

        private int computeProgress(int unit) {
            return (int) (bucketStart + ((double) unit / totalWork) * bucketSize);
        }

        public void progress(String message) {
            progress.progress(message);
        }

        public void progress(int workunit) {
            progress.progress(computeProgress(workunit));
        }

        public void finish() {
            progress.progress(bucketStart + bucketSize);
        }

        static {
            SPIAccessor.ACCESSOR = new SPIAccessor() {
                @Override
                public Context createContext(Scope scope, Preferences settings, String singleWarningId, ProgressHandle progress, int bucketStart, int bucketSize) {
                    return new Context(scope, settings, singleWarningId, progress, bucketStart, bucketSize);
                }

                @Override
                public String getDisplayName(MissingPlugin missing) {
                    return missing.displayName;
                }

                @Override
                public String getCNB(MissingPlugin missing) {
                    return missing.cnb;
                }

                @Override
                public String getWarningDisplayName(WarningDescription description) {
                    return description.warningDisplayName;
                }

                @Override
                public String getWarningCategoryId(WarningDescription description) {
                    return description.categoryId;
                }

                @Override
                public String getWarningCategoryDisplayName(WarningDescription description) {
                    return description.categoryDisplayName;
                }

                @Override
                public String getWarningId(WarningDescription description) {
                    return description.warningId;
                }

                @Override
                public String getSelectedId(CustomizerContext<?, ?> cc) {
                    return cc.selectedId;
                }

                @Override
                public String getAnalyzerId(AnalyzerFactory selected) {
                    return selected.id;
                }

                @Override
                public String getAnalyzerDisplayName(AnalyzerFactory a) {
                    return a.displayName;
                }

                @Override
                public String getAnalyzerIconPath(AnalyzerFactory analyzer) {
                    return analyzer.iconPath;
                }
            };
        }
    }

    public static final class MissingPlugin {
        private final String cnb;
        private final String displayName;
        public MissingPlugin(String cnb, String displayName) {
            this.cnb = cnb;
            this.displayName = displayName;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MissingPlugin other = (MissingPlugin) obj;
            if ((this.cnb == null) ? (other.cnb != null) : !this.cnb.equals(other.cnb)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (this.cnb != null ? this.cnb.hashCode() : 0);
            return hash;
        }
        
    }

    public static final class WarningDescription {

        public static WarningDescription create(String warningId, String warningDisplayName, String categoryId, String categoryDisplayName) {
            return new WarningDescription(warningId, warningDisplayName, categoryId, categoryDisplayName);
        }
        
        private final String warningId;
        private final String warningDisplayName;
        private final String categoryId;
        private final String categoryDisplayName;

        private WarningDescription(String warningId, String warningDisplayName, String categoryId, String categoryDisplayName) {
            this.warningId = warningId;
            this.warningDisplayName = warningDisplayName;
            this.categoryId = categoryId;
            this.categoryDisplayName = categoryDisplayName;
        }

    }

    public interface CustomizerProvider<D, C extends JComponent> {
        public D initialize();
        public C createComponent(CustomizerContext<D, C> context);
    }

    public static final class CustomizerContext<D, C extends JComponent> {
        private final Preferences preferences;
        private final String preselectId;
        private final C      previousComponent;
        private final D      data;

        /*XXX*/ public CustomizerContext(Preferences preferences, String preselectId, C previousComponent, D data) {
            this.preferences = preferences;
            this.preselectId = preselectId;
            this.previousComponent = previousComponent;
            this.data = data;
        }

        public Preferences getSettings() {
            return preferences;
        }

        public String getPreselectId() {
            return preselectId;
        }

        public C getPreviousComponent() {
            return previousComponent;
        }

        public D getData() {
            return data;
        }

        private String selectedId;

        public void setSelectedId(String id) {
            this.selectedId = id;
        }

    }

}
