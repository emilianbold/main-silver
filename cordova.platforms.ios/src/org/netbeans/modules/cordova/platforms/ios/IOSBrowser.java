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
package org.netbeans.modules.cordova.platforms.ios;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.platforms.BuildPerformer;
import org.netbeans.modules.web.browser.spi.EnhancedBrowser;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Becicka
 */
public class IOSBrowser extends HtmlBrowser.Impl implements EnhancedBrowser {
    private final Kind kind;
    private Lookup projectContext;

    @Override
    public void disablePageInspector() {
    }

    @Override
    public void enableLiveHTML() {
    }

    @Override
    public void close(boolean closeTab) {
    }

    @Override
    public void setProjectContext(Lookup projectContext) {
        this.projectContext = projectContext;
    }

    @Override
    public boolean canReloadPage() {
        return false;
    }
    
    public static enum Kind {
        IOS_DEVICE_DEFAULT,
        IOS_SIMULATOR_DEFAULT
    }
    

    public IOSBrowser(Kind kind) {
        this.kind = kind;
    }
    
    private URL url;

    @Override
    public Component getComponent() {
        return null;
    }

    @Override
    public void reloadDocument() {
    }

    @Override
    public void stopLoading() {
    }

    @Override
    public void setURL(URL url) {
        this.url = url;
        Project project = projectContext.lookup(Project.class);
        openBrowser(ActionProvider.COMMAND_RUN, Lookups.fixed(url), project);
    }

    public static void openBrowser(String command, final Lookup context, final Project project) throws IllegalArgumentException {
        if (!Utilities.isMac()) {
            NotifyDescriptor not = new NotifyDescriptor(
                    Bundle.LBL_NoMac(),
                    Bundle.ERR_Title(),
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    null,
                    null);
            DialogDisplayer.getDefault().notify(not);
            return;
        }
        final BuildPerformer build = Lookup.getDefault().lookup(BuildPerformer.class);
        assert build != null;
        ProgressUtils.runOffEventDispatchThread(new Runnable() {
            @Override
            public void run() {
                    IOSDevice.IPHONE.openUrl(build.getUrl(project, context));
            }
        }, Bundle.LBL_Opening(), new AtomicBoolean(), false);
    }
    

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public String getStatusMessage() {
        return "Status";
    }

    @Override
    public String getTitle() {
        return "Title";
    }

    @Override
    public boolean isForward() {
        return false;
    }

    @Override
    public void forward() {
    }

    @Override
    public boolean isBackward() {
        return false;
    }

    @Override
    public void backward() {
    }

    @Override
    public boolean isHistory() {
        return false;
    }

    @Override
    public void showHistory() {
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }
    
}
