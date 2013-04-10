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
package org.netbeans.modules.web.common.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.cssprep.CssPreprocessorAccessor;
import org.netbeans.modules.web.common.spi.CssPreprocessorImplementation;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.util.Parameters;

/**
 * The API representation of a single CSS preprocessor.
 * @since 1.40
 */
public final class CssPreprocessor {

    /**
     * Property for changes in project customizer. <b>New value
     * contains the relevant project!</b>
     * @since 1.43
     */
    public static final String CUSTOMIZER_PROPERTY = CssPreprocessorImplementation.CUSTOMIZER_PROPERTY;
    /**
     * Property for changes in general configuration.
     * @since 1.43
     */
    public static final String OPTIONS_PROPERTY = CssPreprocessorImplementation.OPTIONS_PROPERTY;


    private final CssPreprocessorImplementation delegate;
    final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            Object oldValue = evt.getOldValue();
            Object newValue = evt.getNewValue();
            if (CUSTOMIZER_PROPERTY.equals(propertyName)) {
                if (!(newValue instanceof Project)) {
                    throw new IllegalArgumentException("Project for newValue expected for property " + CUSTOMIZER_PROPERTY);
                }
            }
            propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
        }
    };


    static {
        CssPreprocessorAccessor.setDefault(new CssPreprocessorAccessor() {
            @Override
            public CssPreprocessor create(CssPreprocessorImplementation cssPreprocessorImplementation) {
                CssPreprocessor cssPreprocessor = new CssPreprocessor(cssPreprocessorImplementation);
                // no need to remove listener since CssPreprocessor should not ever be root
                cssPreprocessorImplementation.addPropertyChangeListener(cssPreprocessor.propertyChangeListener);
                return cssPreprocessor;
            }
            @Override
            public CssPreprocessorImplementation.Customizer createCustomizer(CssPreprocessor cssPreprocessor, Project project) {
                return cssPreprocessor.createCustomizer(project);
            }

            @Override
            public CssPreprocessorImplementation.Options createOptions(CssPreprocessor cssPreprocessor) {
                return cssPreprocessor.createOptions();
            }
            @Override
            public ProjectProblemsProvider createProjectProblemsProvider(CssPreprocessor cssPreprocessor, ProjectProblemsProviderSupport support) {
                return cssPreprocessor.createProjectProblemsProvider(support);
            }
        });
    }

    private CssPreprocessor(CssPreprocessorImplementation delegate) {
        this.delegate = delegate;
    }

    // package private!
    CssPreprocessorImplementation getDelegate() {
        return delegate;
    }

    CssPreprocessorImplementation.Customizer createCustomizer(@NonNull Project project) {
        return delegate.createCustomizer(project);
    }

    CssPreprocessorImplementation.Options createOptions() {
        return delegate.createOptions();
    }

    ProjectProblemsProvider createProjectProblemsProvider(@NonNull CssPreprocessor.ProjectProblemsProviderSupport support) {
        Parameters.notNull("support", support); // NOI18N
        return delegate.createProjectProblemsProvider(support);
    }

    /**
     * Return the <b>non-localized (usually english)</b> identifier of this CSS preprocessor.
     * @return the <b>non-localized (usually english)</b> identifier; never {@code null}
     */
    @NonNull
    public String getIdentifier() {
        String identifier = delegate.getIdentifier();
        Parameters.notNull("identifier", identifier); // NOI18N
        return identifier;
    }

    /**
     * Return the display name of this CSS preprocessor. The display name is used
     * in the UI.
     * @return the display name; never {@code null}
     */
    @NonNull
    public String getDisplayName() {
        String displayName = delegate.getDisplayName();
        Parameters.notNull("displayName", displayName); // NOI18N
        return displayName;
    }

    /**
     * Attach a property change listener that is to be notified of changes
     * in this CSS peprocessor. Possible properties are:
     * <ul>
     *   <li>{@link #CUSTOMIZER_PROPERTY} - property for changes in project customizer. <b>New value
     *       contains the relevant project!</b></li>
     *   <li>{@link #OPTIONS_PROPERTY} - property for changes in general configuration</li>
     * </ul>
     * @param listener a listener, can be {@code null}
     * @since 1.43
     */
    public void addPropertyChangeListener(@NullAllowed PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener.
     * @param listener a listener, can be {@code null}
     * @since 1.43
     */
    public void removePropertyChangeListener(@NullAllowed PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    //~ Inner classes

    /**
     * Support class for creating and solving {@link CssPreprocessors#createProjectProblemsProvider(ProjectProblemsProviderSupport) project problems resolver}.
     * @since 1.41
     */
    public interface ProjectProblemsProviderSupport {

        /**
         * Get actual project for checking problems.
         * @return actual project, never {@code null}
         */
        Project getProject();

        /**
         * Open project customizer with CSS preprocessors.
         * @see CssPreprocessors#CUSTOMIZER_IDENT
         */
        void openCustomizer();

    }

}
