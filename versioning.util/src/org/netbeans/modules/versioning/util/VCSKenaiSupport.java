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

package org.netbeans.modules.versioning.util;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.util.Date;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JLabel;

/**
 *
 * @author Tomas Stupka
 */
public abstract class VCSKenaiSupport {

    /**
     * Some kenai vcs repository was changed
     */
    public final static String PROP_KENAI_VCS_NOTIFICATION = "kenai.vcs.notification"; // NOI18N

    /**
     * A Kenai service
     */
    public enum Service {
        VCS_SVN,
        VCS_HG,
        UNKNOWN;
    }

    /**
     * Returns an instance of PasswordAuthentication holding the actuall
     * Kenai credentials or null if user not logged in.
     *
     * @return
     */
    public abstract PasswordAuthentication getPasswordAuthentication();

    /**
     * Returns an instance of PasswordAuthentication holding the actuall
     * Kenai credentials or forces a login if forceLogin is true
     *
     * @param forceLogin opens a login dialog is user not logged in
     * @return
     */
    public abstract PasswordAuthentication getPasswordAuthentication(boolean forceLogin);

    /**
     * Returns true if the given url belongs to a Kenai project, otherwise false.
     * 
     * @param url
     * @return
     */
    public abstract boolean isKenai(String url);

    /**
     * Opens the kenai login dialog.
     * @return true if login successfull, otherwise false
     */
    public abstract boolean showLogin();

    /**
     * Creates a firm association between the roots and a kenai issuetracking repository
     * which has the given vcs url
     *
     * @param roots roots to be associated with a kenai repository
     * @param url vcs url to match a kenai repository
     */
    public abstract void setFirmAssociations(File[] roots, String url);

    /**
     * Determines if the user is logged into kenai
     * @return true if user is logged into kenai otherwise false
     */
    public abstract boolean isLogged ();

    /**
     * Returns a {@link KenaiUser} with the given name
     * @param userName user name
     * @return a KenaiUser instance
     */
    public abstract KenaiUser forName(final String userName);

    /**
     * Determines wheter the user with the given name is online or not
     *
     * @param userName user name
     * @return true if user is online, otherwise false
     */
    public abstract boolean isUserOnline(String userName);

    /**
     * Registers a listener to listen on changes in a kenai VCS repository
     * @param l listener
     */
    public abstract void addVCSNoficationListener(PropertyChangeListener l);

    /**
     * Unregisters a listener to listen on changes in a kenai VCS repository
     * @param l listener
     */
    public abstract void removeVCSNoficationListener(PropertyChangeListener l);

    /**
     * Returns a path to a web page showing information about a revision in the repository.
     * @param sourcesUrl repository url
     * @param revision required revision
     * @return
     */
    public abstract String getRevisionUrl (String sourcesUrl, String revision);

    /**
     * Repesents a Kenai user
     */
    public abstract class KenaiUser {

        /**
         * Determines wheter the {@link KenaiUser} is online or not
         * @return true if user is online, othewise false
         */
        public abstract boolean isOnline();

        /**
         * Register a listener
         * @param listener
         */
        public abstract void addPropertyChangeListener(PropertyChangeListener listener);

        /**
         * Unregister a listener
         * @param listener
         */
        public abstract void removePropertyChangeListener(PropertyChangeListener listener);

        /**
         * Returns an icon representing the users online status
         * @return
         */
        public abstract Icon getIcon();

        /**
         * Returns an widget representing the users
         * @return
         */
        public abstract JLabel createUserWidget();

        /**
         * Returns user name
         * @return
         */
        public abstract String getUser();

        /**
         * Start a chat session with this user
         */
        public abstract void startChat();
    }

    /**
     * Represents a change in a kenai VCS repository
     */
    public abstract class VCSKenaiNotification {

        /**
         * The repository uri
         * @return
         */
        public abstract URI getUri();

        /**
         * Timestamp of change
         * @return
         */
        public abstract Date getStamp();

        /**
         * Determines the repository service - e.g svn, hg
         * @return
         */
        public abstract Service getService();

        /**
         * Notified modifications
         * @return
         */
        public abstract List<VCSKenaiModification> getModifications();

        /**
         * Author who made the change
         * @return
         */
        public abstract String getAuthor();
    }

    /**
     * Represenst a modification in a Kenai VCS repository
     */
    public abstract static class VCSKenaiModification {

        /**
         * Type of modification
         */
        public static enum Type {
            NEW,
            CHANGE,
            DELETE
        }

        /**
         * Determines the type of this modification
         * @return
         */
        public abstract Type getType();

        /**
         * Determines the affeted resource
         * @return
         */
        public abstract String getResource();

        /**
         * Identifies this modification - e.g reviosion or changeset
         * @return
         */
        public abstract String getId();
    }
    
}
