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

package org.netbeans.libs.git.jgit.commands;

import com.jcraft.jsch.JSchException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.JGitText;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.TransportProtocol;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.SystemReader;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
abstract class TransportCommand extends GitCommand {
    private static final String PROP_ENV_GIT_SSH = "GIT_SSH"; //NOI18N
    private CredentialsProvider credentialsProvider;
    private final String remote;
    private static final Logger LOG = Logger.getLogger(TransportCommand.class.getName());

    public TransportCommand (Repository repository, GitClassFactory gitFactory, String remote, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.remote = remote;
    }

    protected final URIish getUri (boolean pushUri) throws URISyntaxException {
        RemoteConfig config = getRemoteConfig();
        List<URIish> uris;
        if (config == null) {
            uris = Collections.emptyList();
        } else {
            if (pushUri) {
                uris = config.getPushURIs();
                if (uris.isEmpty()) {
                    uris = config.getURIs();
                }
            } else {
                uris = config.getURIs();
            }
        }
        if (uris.isEmpty()) {
            return new URIish(remote);
        } else {
            return uris.get(0);
        }
    }

    protected final URIish getUriWithUsername (boolean pushUri) throws URISyntaxException {
        URIish uri = getUri(pushUri);
        if (credentialsProvider != null) {
            CredentialItem.Username itm = new CredentialItem.Username();
            credentialsProvider.get(uri, itm);
            if (itm.getValue() != null) {
                if (itm.getValue().isEmpty()) {
                    uri = uri.setUser(null);
                } else {
                    uri = uri.setUser(itm.getValue());
                }
            }
        }
        return uri;
    }

    public final void setCredentialsProvider (CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }
    
    @Override
    public final void run () throws GitException {
        SystemReader original = SystemReader.getInstance();
        String externalTool = original.getenv(PROP_ENV_GIT_SSH);
        boolean replace = externalTool != null;
        try {
            if (replace) {
                LOG.log(Level.WARNING, "{0} set to {1}, ignoring and using the default implementation via JSch", new Object[] { PROP_ENV_GIT_SSH, externalTool }); //NOI18N
                SystemReader.setInstance(new DelegatingSystemReader(original));
            }
            runTransportCommand();
        } finally {
            if (replace) {
                SystemReader.setInstance(original);
            }
        }
    }
    
    protected final CredentialsProvider getCredentialsProvider () {
        return credentialsProvider;
    }
    
    protected final RemoteConfig getRemoteConfig () throws URISyntaxException {
        RemoteConfig config = new RemoteConfig(getRepository().getConfig(), remote);
        if (config.getURIs().isEmpty() && config.getPushURIs().isEmpty()) {
            return null;
        } else {
            return config;
        }
    }
    
    protected Transport openTransport (boolean openPush) throws URISyntaxException, NotSupportedException, TransportException {
        URIish uri = getUriWithUsername(openPush);
        // WA for #200693, jgit fails to initialize ftp protocol
        for (TransportProtocol proto : Transport.getTransportProtocols()) {
            if (proto.getSchemes().contains("ftp")) { //NOI18N
                Transport.unregister(proto);
            }
        }
        Transport transport = Transport.open(getRepository(), uri);
        RemoteConfig config = getRemoteConfig();
        if (config != null) {
            transport.applyConfig(config);
        }
        if (transport.getTimeout() <= 0) {
            transport.setTimeout(45);
        }
        transport.setCredentialsProvider(getCredentialsProvider());
        return transport;
    }
    
    protected final void handleException (TransportException e, URIish uri) throws GitException.AuthorizationException, GitException {
        String message = e.getMessage();
        int pos;
        if (message == null) {
            throw new GitException(e);
        } else if ((pos = message.indexOf(": " + JGitText.get().notAuthorized)) != -1) { //NOI18N
            String repositoryUrl = message.substring(0, pos);
            throw new GitException.AuthorizationException(repositoryUrl, message, e);
        } else if ((pos = message.indexOf(": " + HttpURLConnection.HTTP_UNAUTHORIZED + " ")) != -1) { //NOI18N
            String repositoryUrl = message.substring(0, pos);
            throw new GitException.AuthorizationException(repositoryUrl, message, e);
        } else if (message.contains(JGitText.get().notAuthorized)) { //NOI18N
            throw new GitException.AuthorizationException(uri.toString(), message, e);
        } else if ((pos = message.toLowerCase().indexOf(": auth cancel")) != -1) { //NOI18N
            String repositoryUrl = message.substring(0, pos);
            throw new GitException.AuthorizationException(repositoryUrl, message, e);
        } else if (e.getCause() instanceof JSchException) {
            if (message.contains("timeout:")) { //NOI18N
                throw new GitException(message, e);
            } else {
                throw new GitException.AuthorizationException(uri.toString(), message, e);
            }
        } else {
            throw new GitException(message, e);
        }
    }

    protected abstract void runTransportCommand () throws GitException;
    
    private static class DelegatingSystemReader extends SystemReader {
        private final SystemReader instance;

        public DelegatingSystemReader (SystemReader sr) {
            this.instance = sr;
        }

        @Override
        public String getHostname () {
            return instance.getHostname();
        }

        @Override
        public String getenv (String string) {
            if (PROP_ENV_GIT_SSH.equals(string)) {
                return null;
            }
            return instance.getenv(string);
        }

        @Override
        public String getProperty (String string) {
            return instance.getProperty(string);
        }

        @Override
        public FileBasedConfig openUserConfig (Config config, FS fs) {
            return instance.openUserConfig(config, fs);
        }

        @Override
        public FileBasedConfig openSystemConfig (Config config, FS fs) {
            return instance.openSystemConfig(config, fs);
        }

        @Override
        public long getCurrentTime () {
            return instance.getCurrentTime();
        }

        @Override
        public int getTimezone (long l) {
            return instance.getTimezone(l);
        }
    }
}
