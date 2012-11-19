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
package org.netbeans.modules.glassfish.javaee;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.glassfish.tools.ide.data.GlassFishLibrary;
import org.glassfish.tools.ide.data.GlassFishVersion;
import org.glassfish.tools.ide.server.config.LibraryBuilder;
import org.glassfish.tools.ide.server.config.LibraryConfig;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.glassfish.spi.GlassfishModule;

/**
 * GlassFish bundled Jersey library provider.
 * <p/>
 * Builds <code>LibraryImplementation3</code> instance containing Jersey
 * library from GlassFish modules.
 * Actually only GlassFish v3 and v4 are supported.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class JerseyLibrary /*implements JaxRsStackSupportImplementation*/ {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Jersey library provider type. */
    private static final String JERSEY_PROVIDER_TYPE = "j2se";

    /** Jersey library name suffix to be added after server instance name.
     *  Jersey library name must be unique so combination of instance name
     *  and some common suffix is used. */
    private static final String JERSEY_NAME_SUFFIX = " Jersey";

    /** Library builder default configuration file. */
    private static final URL LIBRARY_BUILDER_CONFIG_DEFAULT
            = JerseyLibrary.class.getResource("JerseyLibsDefault.xml");

    /** Library builder configuration since GlassFish 4. */
    private static final LibraryConfig.Next LIBRARY_BUILDER_CONFIG_V2
            = new LibraryConfig.Next(GlassFishVersion.GF_4,
            JerseyLibrary.class.getResource("JerseyLibs2.xml"));

    /** Library builder configuration for GlassFish cloud. */
    private static final LibraryConfig libraryConfig = new LibraryConfig(
            LIBRARY_BUILDER_CONFIG_DEFAULT, LIBRARY_BUILDER_CONFIG_V2);

    /** Jersey library name pattern to search for it in
     *  <code>GlassFishLibrary</code> list. */
    private Pattern JERSEY_PATTERN = Pattern.compile(".*[jJ]ersey.*");

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Library builder associated with current platform.
      * This attribute should be accessed only using {@see #getBuilder()} even
      * internally. */
    private volatile LibraryBuilder builder;

    /** GlassFish server home directory. */
    private final String serverHome;

    /** GlassFish server name. */
    private final String serverName;

    /** Jersey library name associated with current GlassFish server context.
     *  This is lazy initialized internal cache. Do not access this attribute
     *  outside {@see #getName()} method! */
    private volatile String jerseyName = null;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Jersey library provider.
     * <p/>
     * @param url GlassFish server URL.
     */
    JerseyLibrary(Hk2DeploymentManager dm) {
        if (dm == null) {
            throw new IllegalArgumentException(
                    "GlassFish server deployment manager shall not be null.");
        }
        serverHome = dm.getCommonServerSupport().getInstanceProperties()
                .get(GlassfishModule.GLASSFISH_FOLDER_ATTR);
        serverName = dm.getCommonServerSupport().getInstanceProperties()
                .get(GlassfishModule.DISPLAY_NAME_ATTR);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get Jersey library name for this server context.
     * <p/>
     * This library name shall be registered in default {@see LibraryManager}
     * and is unique for Jersey modules of given GlassFish server instance.
     * Library name is cached after first usage.
     * <p/>
     * @return Jersey library name for this server context.
     */
    public String getName() {
        if (jerseyName != null) {
            return jerseyName;
        }
        synchronized (this) {
            StringBuilder sb = new StringBuilder(
                    serverName.length() + JERSEY_NAME_SUFFIX.length());
            sb.append(serverName);
            sb.append(JERSEY_NAME_SUFFIX);
            jerseyName = sb.toString();
        }
        return jerseyName;
    }

    /**
     * Return Jersey libraries available in GlassFish v3.
     */
    public Library getLibrary() {
        Library lib = LibraryManager.getDefault().getLibrary(getName());
        if (lib != null) {
            return lib;
        }
        LibraryBuilder lb = getBuilder();
        List<GlassFishLibrary> gfLibs = lb.getLibraries(GlassFishVersion.GF_3);
        for (GlassFishLibrary gfLib : gfLibs) {
            if (JERSEY_PATTERN.matcher(gfLib.getLibraryID()).matches()) {
                Map<String,List<URL>> contents
                        = new HashMap<String, List<URL>>(1);
                Map<String, String> properties = new HashMap<String, String>(2);
                contents.put("classpath", gfLib.getClasspath());
                contents.put("javadoc", gfLib.getJavadocs());
                properties.put("maven-dependencies", gfLib.getMavenDeps());
                properties.put("maven-repositories", "default");
                try {
                    return LibraryManager.getDefault().createLibrary(
                            JERSEY_PROVIDER_TYPE,
                            getName(),
                            null,
                            null,
                            contents,
                            properties);
                } catch (IOException ioe) {
                    Logger.getLogger("glassfish-javaee").log(Level.WARNING,
                            "Could not create Jersey library for "
                            + serverName + ": ", ioe);
                }
            }
        }
        return null;
    }

    /**
     * Get library builder.
     * <p/>
     * Library builder instance is initialized in first request and cached for
     * subsequent usage. Library builder is thread safe so it should be used
     * without additional locking.
     * <p/>
     * @return Library builder.
     */
    private LibraryBuilder getBuilder() {
        if (builder != null) {
            return builder;
        }
        synchronized(this) {
            if (builder == null) {
                builder = new LibraryBuilder(libraryConfig,
                        serverHome, serverHome, serverHome);
            }
        }
        return builder;
    }

}
