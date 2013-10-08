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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.model.settings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.maven.model.settings.SettingsQName.Version;

/**
 *
 * @author mkleint
 */
public final class SettingsQNames {
    
    public final SettingsQName SETTINGS; // NOI18N
    public final SettingsQName REPOSITORY; // NOI18N
    public final SettingsQName PLUGINREPOSITORY; // NOI18N
    public final SettingsQName RELEASES; // NOI18N
    public final SettingsQName SNAPSHOTS; // NOI18N
    public final SettingsQName PROFILE; // NOI18N
    public final SettingsQName ACTIVATION; // NOI18N
    public final SettingsQName ACTIVATIONPROPERTY; // NOI18N
    public final SettingsQName ACTIVATIONOS; // NOI18N
    public final SettingsQName ACTIVATIONFILE; // NOI18N
    public final SettingsQName ACTIVATIONCUSTOM; // NOI18N

    public final SettingsQName PROFILES; // NOI18N
    public final SettingsQName REPOSITORIES; // NOI18N
    public final SettingsQName PLUGINREPOSITORIES; // NOI18N

    public final SettingsQName ID; //NOI18N
    public final SettingsQName CONFIGURATION; //NOI18N
    public final SettingsQName PROPERTIES; //NOI18N

    public final SettingsQName URL; //NOI18N
    public final SettingsQName NAME; //NOI18N
    public final SettingsQName VALUE; //NOI18N

    public final SettingsQName LAYOUT; //NOI18N

    public final SettingsQName ACTIVEPROFILES; //NOI18N
    public final SettingsQName ACTIVEPROFILE; //NOI18N
    public final SettingsQName PLUGINGROUPS; //NOI18N
    public final SettingsQName PLUGINGROUP; //NOI18N

    public final SettingsQName MIRROROF; //NOI18N
    public final SettingsQName MIRROR; //NOI18N
    public final SettingsQName MIRRORS; //NOI18N

    public final SettingsQName PROXIES; //NOI18N
    public final SettingsQName PROXY; //NOI18N
    public final SettingsQName ACTIVE; //NOI18N
    public final SettingsQName HOST; //NOI18N
    public final SettingsQName PORT; //NOI18N
    public final SettingsQName USERNAME; //NOI18N
    public final SettingsQName PASSWORD; //NOI18N
    public final SettingsQName PROTOCOL; //NOI18N
    public final SettingsQName NONPROXYHOSTS; //NOI18N

    public final SettingsQName SERVER; //NOI18N
    public final SettingsQName SERVERS; //NOI18N

    public final SettingsQName PRIVATEKEY; //NOI18N
    public final SettingsQName PASSPHRASE; //NOI18N

    public final SettingsQName OFFLINE; //NOI18N
    public final SettingsQName INTERACTIVEMODE; //NOI18N
    public final SettingsQName USEPLUGINREGISTRY; //NOI18N
    public final SettingsQName LOCALREPOSITORY; //NOI18N

  
    private final Version version;
    
    @Deprecated
    public SettingsQNames(boolean ns, boolean old) {
        this(SettingsQName.resolveVersion(ns, old));
    }
/**
 * 
 * @param version 
 * @since 1.34
 */
    public SettingsQNames(@NonNull Version version) {

        this.version = version;
        SETTINGS = new SettingsQName(SettingsQName.createQName("settings",version)); // NOI18N
        REPOSITORY = new SettingsQName(SettingsQName.createQName("repository", version)); // NOI18N
        PLUGINREPOSITORY = new SettingsQName(SettingsQName.createQName("pluginRepository", version)); // NOI18N
        RELEASES = new SettingsQName(SettingsQName.createQName("releases",version)); // NOI18N
        SNAPSHOTS = new SettingsQName(SettingsQName.createQName("snapshots",version)); // NOI18N
        PROFILE = new SettingsQName(SettingsQName.createQName("profile",version)); // NOI18N
        ACTIVATION = new SettingsQName(SettingsQName.createQName("activation",version)); // NOI18N
        ACTIVATIONPROPERTY = new SettingsQName(SettingsQName.createQName("property",version)); // NOI18N
        ACTIVATIONOS = new SettingsQName(SettingsQName.createQName("os",version)); // NOI18N
        ACTIVATIONFILE = new SettingsQName(SettingsQName.createQName("file",version)); // NOI18N
        ACTIVATIONCUSTOM = new SettingsQName(SettingsQName.createQName("custom",version)); // NOI18N
        PROFILES = new SettingsQName(SettingsQName.createQName("profiles",version)); // NOI18N
        REPOSITORIES = new SettingsQName(SettingsQName.createQName("repositories",version)); // NOI18N
        PLUGINREPOSITORIES = new SettingsQName(SettingsQName.createQName("pluginRepositories",version)); // NOI18N

        ID = new SettingsQName(SettingsQName.createQName("id",version)); //NOI18N
        CONFIGURATION = new SettingsQName(SettingsQName.createQName("configuration",version)); //NOI18N
        PROPERTIES = new SettingsQName(SettingsQName.createQName("properties",version)); //NOI18N
        URL = new SettingsQName(SettingsQName.createQName("url",version)); //NOI18N
        NAME = new SettingsQName(SettingsQName.createQName("name",version)); //NOI18N

        VALUE = new SettingsQName(SettingsQName.createQName("value",version)); //NOI18N

        LAYOUT = new SettingsQName(SettingsQName.createQName("layout",version)); //NOI18N

        ACTIVEPROFILE = new SettingsQName(SettingsQName.createQName("activeProfile",version)); //NOI18N
        ACTIVEPROFILES = new SettingsQName(SettingsQName.createQName("activeProfiles",version)); //NOI18N

        PLUGINGROUP = new SettingsQName(SettingsQName.createQName("pluginGroup",version)); //NOI18N
        PLUGINGROUPS = new SettingsQName(SettingsQName.createQName("pluginGroups",version)); //NOI18N

        MIRROROF = new SettingsQName(SettingsQName.createQName("mirrorOf",version)); //NOI18N
        MIRROR = new SettingsQName(SettingsQName.createQName("mirror",version)); //NOI18N
        MIRRORS = new SettingsQName(SettingsQName.createQName("mirrors",version)); //NOI18N

        PROXIES = new SettingsQName(SettingsQName.createQName("proxies",version)); //NOI18N
        PROXY = new SettingsQName(SettingsQName.createQName("proxy",version)); //NOI18N
        ACTIVE = new SettingsQName(SettingsQName.createQName("active",version)); //NOI18N
        HOST = new SettingsQName(SettingsQName.createQName("host",version)); //NOI18N
        PORT = new SettingsQName(SettingsQName.createQName("port",version)); //NOI18N
        USERNAME = new SettingsQName(SettingsQName.createQName("username",version)); //NOI18N
        PASSWORD = new SettingsQName(SettingsQName.createQName("password",version)); //NOI18N
        PROTOCOL = new SettingsQName(SettingsQName.createQName("protocol",version)); //NOI18N
        NONPROXYHOSTS = new SettingsQName(SettingsQName.createQName("nonProxyHosts",version)); //NOI18N
        //when adding items here, need to add them to the set below as well.

        SERVER = new SettingsQName(SettingsQName.createQName("server",version)); //NOI18N
        SERVERS = new SettingsQName(SettingsQName.createQName("servers",version)); //NOI18N

        PASSPHRASE = new SettingsQName(SettingsQName.createQName("passphrase",version)); //NOI18N
        PRIVATEKEY = new SettingsQName(SettingsQName.createQName("privateKey",version)); //NOI18N

        OFFLINE = new SettingsQName(SettingsQName.createQName("offline",version)); //NOI18N
        USEPLUGINREGISTRY = new SettingsQName(SettingsQName.createQName("usePluginRegistry",version)); //NOI18N
        LOCALREPOSITORY = new SettingsQName(SettingsQName.createQName("localRepository",version)); //NOI18N
        INTERACTIVEMODE = new SettingsQName(SettingsQName.createQName("interactiveMode",version)); //NOI18N

    }

    public boolean isNSAware() {
        return version.getNamespace() != null;
    }
    @Deprecated
    public boolean isOldNS() {
        return version.equals(Version.OLD);
    }
    /**
     * 
     * @return 
     * @since 1.34
     */
    public Version getNamespaceVersion() {
        return version;
    }


    public Set<QName> getElementQNames() {
        QName[] names = new QName[] {
            SETTINGS.getQName(),
            REPOSITORY.getQName(),
            PLUGINREPOSITORY.getQName(),
            RELEASES.getQName(),
            SNAPSHOTS.getQName(),
            PROFILE.getQName(),
            ACTIVATION.getQName(),
            ACTIVATIONPROPERTY.getQName(),
            ACTIVATIONOS.getQName(),
            ACTIVATIONFILE.getQName(),
            ACTIVATIONCUSTOM.getQName(),
            PROFILES.getQName(),
            REPOSITORIES.getQName(),
            PLUGINREPOSITORIES.getQName(),
            ID.getQName(),
            CONFIGURATION.getQName(),
            PROPERTIES.getQName(),
            URL.getQName(),
            NAME.getQName(),
            VALUE.getQName(),
            LAYOUT.getQName(),
            ACTIVEPROFILE.getQName(),
            ACTIVEPROFILES.getQName(),
            PLUGINGROUPS.getQName(),
            PLUGINGROUP.getQName(),
            MIRROROF.getQName(),
            MIRRORS.getQName(),
            MIRROR.getQName(),
            PROXIES.getQName(),
            PROXY.getQName(),
            ACTIVE.getQName(),
            HOST.getQName(),
            PORT.getQName(),
            USERNAME.getQName(),
            PASSWORD.getQName(),
            PROTOCOL.getQName(),
            NONPROXYHOSTS.getQName(),
            SERVER.getQName(),
            SERVERS.getQName(),
            PRIVATEKEY.getQName(),
            PASSPHRASE.getQName(),
            OFFLINE.getQName(),
            INTERACTIVEMODE.getQName(),
            USEPLUGINREGISTRY.getQName(),
            LOCALREPOSITORY.getQName(),
        };
        List<QName> list = Arrays.asList(names);
        return new HashSet<QName>(list);
    }
    
}
