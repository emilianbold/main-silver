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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wag.manager.zembly;

import com.zembly.gateway.client.Zembly;
import com.zembly.gateway.client.config.Configuration;
import com.zembly.oauth.api.Parameter;
import com.zembly.oauth.api.Response;
import com.zembly.oauth.core.UrlConnection;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.netbeans.modules.wag.manager.util.WagPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author peterliu
 */
public class ZemblySession implements PropertyChangeListener {

    private static final String LOGIN_URL = "https://zembly.com/ui/loginProcess";   //NOI18N
    private static final String USERID_ATTR = "userid";         //NOI18N
    private static final String USERNAME_ATTR = "username";     //NOI18N
    private static final String KEY_ATTR = "key";               //NOI18N
    private static final String SECRET_ATTR = "secret";         //NOI18N
    private static final String PROP_LOGIN = "login";           //NOI18N
    private static final String EMAIL_PARAM = "email";          //NOI18N
    private static final String PASSWORD_PARAM = "password";    //NOI18N
    private static final String USE_CWP_PARAM = "useCWP";       //NOI18N
    private static ZemblySession instance;
    private Zembly zembly;
    private SearchEngine searchEngine;
    private DomainRetriever domainRetriever;
    private ContentRetriever contentRetriever;
    private UserServiceRetriever userServiceRetriever;
    private RankingRetriever rankingRetriever;
    private ItemInfoRetriever itemInfoRetriever;
    private TestDriver testDriver;
    private ZemblyUserInfo userInfo;
    protected PropertyChangeSupport pps;


    static {
        ZemblySession session = ZemblySession.getInstance();
        Kenai kenai = KenaiManager.getDefault().getKenai("https://kenai.com");

        kenai.addPropertyChangeListener(WeakListeners.propertyChange(session, kenai));
    }

    private ZemblySession() {
        pps = new PropertyChangeSupport(this);
    }

    public synchronized static ZemblySession getInstance() {
        if (instance == null) {
            instance = new ZemblySession();
        }

        return instance;
    }

    public void login(String username, char[] password) throws IOException, JSONException {
        if (userInfo == null) {
            // For local testing
            userInfo = zemblyLogin(username, new String(password));

            fireChange(false, true, PROP_LOGIN);
        }
    }

    public void logout() {
        userInfo = null;

        fireChange(true, false, PROP_LOGIN);
    }

    public boolean isLoggedIn() {
        return userInfo != null;
    }

    public ZemblyUserInfo getUserInfo() {
        return userInfo;
    }

    public DomainRetriever getDomainRetriever() {
        assert isLoggedIn();

        if (domainRetriever == null) {
            domainRetriever = new DomainRetriever(getZembly());
        }

        return domainRetriever;
    }

    public SearchEngine getSearchEngine() {
        assert isLoggedIn();

        if (searchEngine == null) {
            searchEngine = new SearchEngine(getZembly());
        }

        return searchEngine;
    }

    public ContentRetriever getContentRetriever() {
        assert isLoggedIn();

        if (contentRetriever == null) {
            contentRetriever = new ContentRetriever(getZembly());
        }

        return contentRetriever;
    }

    public UserServiceRetriever getUserServiceRetriever() {
        assert isLoggedIn();

        if (userServiceRetriever == null) {
            userServiceRetriever = new UserServiceRetriever(getZembly());
        }

        return userServiceRetriever;
    }

    public RankingRetriever getRankingRetriever() {
        assert isLoggedIn();

        if (rankingRetriever == null) {
            rankingRetriever = new RankingRetriever(getZembly());
        }

        return rankingRetriever;
    }

    public ItemInfoRetriever getItemInfoRetriever() {
        assert isLoggedIn();

        if (itemInfoRetriever == null) {
            itemInfoRetriever = new ItemInfoRetriever(getZembly());
        }

        return itemInfoRetriever;
    }

    public TestDriver getTestDriver() {
        assert isLoggedIn();

        if (testDriver == null) {
            testDriver = new TestDriver(getZembly());
        }

        return testDriver;
    }

    private ZemblyUserInfo zemblyLogin(String username, String password) throws IOException, JSONException {
        List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter(EMAIL_PARAM, username));
        params.add(new Parameter(PASSWORD_PARAM, password));
        params.add(new Parameter(USE_CWP_PARAM, "true"));   //NOI18N

        //Utilities.trustZemblyCertificate();

        UrlConnection conn = new UrlConnection(LOGIN_URL, params);
        Response result = conn.post(null);

        return parseUserInfo(result.getString());
    }

    private Zembly getZembly() {
        if (zembly == null) {
            try {
                zembly = Zembly.getInstance("org/netbeans/modules/wag/manager/resources/zcl.properties");   //NOI18N

                Configuration config = zembly.getConfig();
                config.setConsumerKey(userInfo.getKey());
                config.setConsumerSecret(userInfo.getSecret());
            } catch (Exception ex) {
                //ignore
            }
        }

        return zembly;
    }

    private ZemblyUserInfo parseUserInfo(String data) throws JSONException {
        JSONTokener parser = new JSONTokener(data);
        JSONObject obj = (JSONObject) parser.nextValue();
        userInfo = new ZemblyUserInfo();

        userInfo.setUserid(obj.getString(USERID_ATTR));
        userInfo.setUsername(obj.getString(USERNAME_ATTR));

        // The OAuth keys are not yet available from zembly.
        try {
            userInfo.setKey(obj.getString(KEY_ATTR));
            userInfo.setSecret(obj.getString(SECRET_ATTR));
        } catch (JSONException ex) {
            // ignore
            }

        return userInfo;

    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pps.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pps.removePropertyChangeListener(l);
    }

    protected void fireChange(Object old, Object neu, String propName) {
        PropertyChangeEvent pce = new PropertyChangeEvent(this, propName, old, neu);
        pps.firePropertyChange(pce);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        if (propName.equals(Kenai.PROP_LOGIN_STARTED)) {
            String kenaiEmail = (String) evt.getNewValue();

            if (kenaiEmail.contains("@")) {
                WagPreferences.getInstance().setKenaiEmail(kenaiEmail);
                
                // Need to remove previous saved kenai username
                WagPreferences.getInstance().setKenaiUsername(null);
            }
        } else if (propName.equals(Kenai.PROP_LOGIN)) {
            final PasswordAuthentication newValue = (PasswordAuthentication) evt.getNewValue();
            // If new value is not null, that means login, otherwise, logout
            if (newValue != null) {
                final String email = lookupEmail(newValue.getUserName(), true);

                //System.out.println("username = " + newValue.getUserName() + " email = " + email);
                if (email != null) {
                    RequestProcessor.getDefault().post(new Runnable() {

                        public void run() {
                            try {
                                login(email, newValue.getPassword());
                            } catch (Exception ex) {
                                // Ignore - fail silently
                                //Utilities.handleException(ex);
                            }
                        }
                    });
                }
            } else {
                /*
                PasswordAuthentication oldValue = (PasswordAuthentication) evt.getOldValue();
                String email = lookupEmail(oldValue.getUserName(), false);

                if (email != null) {
                    if (userInfo != null && userInfo.getUsername().equals(email)) {
                        logout();
                    }
                }
                 */
            }

        }
    }

    private String lookupEmail(String kenaiUsername, boolean updateCache) {
        String savedKenaiUsername = WagPreferences.getInstance().getKenaiUsername();
        String savedKenaiEmail = WagPreferences.getInstance().getKenaiEmail();

        if (savedKenaiUsername == null) {
            if (updateCache) {
                WagPreferences.getInstance().setKenaiUsername(kenaiUsername);
            }

            return savedKenaiEmail;
        } else if (savedKenaiUsername.equals(kenaiUsername)) {
            return savedKenaiEmail;
        } else {
            return null;
        }
    }
}
