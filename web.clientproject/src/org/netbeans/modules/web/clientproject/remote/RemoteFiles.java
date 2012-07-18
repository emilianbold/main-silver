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

package org.netbeans.modules.web.clientproject.remote;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.html.editor.api.index.HtmlIndex;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.openide.filesystems.FileObject;
import org.openide.modules.Places;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class RemoteFiles {

    private ClientSideProject project;
    private RequestProcessor RP = new RequestProcessor();
    private List<URL> urls;
    private ChangeSupport changeSupport = new ChangeSupport(this);
    private HtmlIndex index;
    
    public RemoteFiles(ClientSideProject project) {
        this.project = project;
    }
    
    private synchronized HtmlIndex getHtmlIndex() {
        if (index == null) {
            try {
                index = HtmlIndex.get(project);
                index.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        update();
                    }
                });
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return index;
    }
    
    private void update() {
        RP.post(new Runnable() {
            @Override
            public void run() {
// 215101 prevents me from using ParserManager.parseWhenScanFinished;
// adding 500 dalay for task to run to give indexing enough time to finish
                
//                try {
//                    ParserManager.parseWhenScanFinished("text/html", new UserTask() {
//                        @Override
//                        public void run(ResultIterator resultIterator) throws Exception {
                            updateRemoteFiles();
//                        }
//                    });
//                } catch (ParseException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
            }
        }, 500);
    }
    
    private void updateRemoteFiles() {
        try {
            setUrls(filter(getHtmlIndex().getAllRemoteDependencies()));
            fireChange();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public synchronized List<URL> getRemoteFiles() {
        if (urls == null) {
            urls = new ArrayList<URL>();
            // first time asked for remote files: return empty array and
            // initialize index etc and fire even when real data are available:
            update();
         }
        return urls;
    }

    private synchronized void setUrls(List<URL> urls) {
        this.urls = urls;
        prefetchRemoteFiles(urls);
    }
    
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    private void fireChange() {
        changeSupport.fireChange();
    }
    
    // TODO:
    
    // content of remote URL is downloaded and cached - cache is shared by all projects
    
    // ability to force cache refresh

    private void prefetchRemoteFiles(final List<URL> urls) {
        for (final URL u : urls) {
            try {
                RemoteFilesCache.getDefault().getRemoteFile(u);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    // for now filter out all remote files which are not JavaScript files:
    private List<URL> filter(List<URL> allRemoteDependencies) {
        List<URL> res = new ArrayList<URL>();
        for (URL u : allRemoteDependencies) {
            String uu = u.toExternalForm();
            if (uu.toLowerCase().endsWith(".js")) {
                res.add(u);
            }
        }
        return res;
    }
    
}
