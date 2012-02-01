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
package org.netbeans.modules.php.spi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.openide.util.Parameters;

/**
 * Encapsulates a PHP annotations provider.
 *
 * <p>This class allows providing support for PHP annotations.</p>
 *
 * <p>Instances of this class are registered in the <code>{@value org.netbeans.modules.php.api.annotations.PhpAnnotations#ANNOTATIONS_PATH}</code>
 * in the module layer, see {@link Registration}.</p>
 * @since 1.63
 * @see org.netbeans.modules.php.spi.phpmodule.PhpFrameworkProvider#getAnnotationsProvider(PhpModule)
 */
public abstract class PhpAnnotationsProvider {

    private final String identifier;
    private final String name;
    private final String description;


    /**
     * Create a new PHP annotations provider with a name and description.
     *
     * @param  identifier the <b>non-localized (usually english)</b> identifier of this PHP annotations
     *         provider (e.g., "Symfony Annotations"); never {@code null}
     * @param  name the <b>localized</b> name of this PHP annotations provider (e.g., "Symfony
     *         PHP Web Framework Annotations"); never {@code null}
     * @param  description the description of this PHP annotations provider (e.g., "PHP annotations
     *        for an open source framework based on the MVC pattern."); can be {@code null}
     * @throws NullPointerException if the {@ code identifier} or {@code name} parameter is {@code null}
     */
    public PhpAnnotationsProvider(String identifier, String name, String description) {
        Parameters.notNull("identifier", identifier); // NOI18N
        Parameters.notNull("name", name); // NOI18N

        this.identifier = identifier;
        this.name = name;
        this.description = description;
    }

    /**
     * Get <b>non-localized (usually english)</b> identifier of this PHP annotations provider.
     *
     * @return <b>non-localized (usually english)</b> identifier; never {@code null}
     */
    public final String getIdentifier() {
        return identifier;
    }

    /**
     * Get the <b>localized</b> name of this PHP annotations provider.
     *
     * @return name; never {@code null}
     */
    public final String getName() {
        return name;
    }

    /**
     * Get the description of this PHP annotations provider. Defaults to the name
     * if a {@code null} {@code description} parameter was passed to the constructor.
     *
     * @return the description; never {@code null}
     */
    public final String getDescription() {
        if (description != null) {
            return description;
        }
        return getName();
    }

    /**
     * Find out if this annotations provider can provide annotations for the given PHP module. The PHP module can be {@code null};
     * in such case, it means "are these annotations available in all files?" (typical for files without any project).
     * <p>
     * <b>This method should be as fast as possible.</b>
     *
     * @param  phpModule PHP module; can be {@code null}
     * @return {@code true} if this annotations provider can provide annotations for the given PHP module, {@code false} otherwise
     */
    public abstract boolean isInPhpModule(PhpModule phpModule);

    /**
     * Get all possible annotations.
     * <p>
     * Default implementation simply return all the possible annotations.
     * @return all possible annotations
     */
    public List<PhpAnnotationTag> getAnnotations() {
        Set<PhpAnnotationTag> annotations = new LinkedHashSet<PhpAnnotationTag>();
        annotations.addAll(getFunctionAnnotations());
        annotations.addAll(getTypeAnnotations());
        annotations.addAll(getFieldAnnotations());
        annotations.addAll(getMethodAnnotations());
        return new ArrayList<PhpAnnotationTag>(annotations);
    }

    /**
     * Get annotations that are available for global functions.
     * @return annotations that are available for global functions
     */
    public abstract List<PhpAnnotationTag> getFunctionAnnotations();

    /**
     * Get annotations that are available for types (classes, interfaces).
     * @return annotations that are available for types (classes, interfaces)
     */
    public abstract List<PhpAnnotationTag> getTypeAnnotations();

    /**
     * Get annotations that are available for type fields.
     * @return annotations that are available for type fields
     */
    public abstract List<PhpAnnotationTag> getFieldAnnotations();

    /**
     * Get annotations that are available for type methods.
     * @return annotations that are available for type methods
     */
    public abstract List<PhpAnnotationTag> getMethodAnnotations();

    //~ Inner classes

    /**
     * Declarative registration of a singleton PHP annotations provider.
     * By marking an implementation class or a factory method with this annotation,
     * you automatically register that implementation, normally in {@link org.netbeans.modules.php.api.annotations.PhpAnnotations#ANNOTATIONS_PATH}.
     * The class must be public and have:
     * <ul>
     *  <li>a public no-argument constructor, or</li>
     *  <li>a public static factory method.</li>
     * </ul>
     *
     * <p>Example of usage:
     * <pre>
     * package my.module;
     * import org.netbeans.modules.php.spi.annotations.PhpAnnotationsProvider;
     * &#64;PhpAnnotationsProvider.Registration(position=100)
     * public class MyAnnotations extends PhpAnnotationsProvider {...}
     * </pre>
     * <pre>
     * package my.module;
     * import org.netbeans.modules.php.spi.phpmodule.PhpAnnotationsProvider;
     * public class MyAnnotations extends PhpAnnotationsProvider {
     *     &#64;PhpAnnotationsProvider.Registration(position=100)
     *     public static PhpAnnotationsProvider getInstance() {...}
     * }
     * </pre>
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface Registration {

        /**
         * An optional position in which to register this annotations provider relative to others.
         * Lower-numbered services are returned in the lookup result first.
         * Providers with no specified position are returned last.
         */
        int position() default Integer.MAX_VALUE;

    }

}
