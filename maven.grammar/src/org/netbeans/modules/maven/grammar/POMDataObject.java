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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.grammar;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.windows.CloneableOpenSupport;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class POMDataObject extends MultiDataObject {

    private static final Logger LOG = Logger.getLogger(POMDataObject.class.getName());

    public POMDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        cookies.add(new POMDataEditor());
        cookies.add(new ValidateXMLSupport(DataObjectAdapters.inputSource(this)));
    }

    protected @Override Node createNodeDelegate() {
        return new DataNode(this, Children.LEAF, getLookup());
    }

    public @Override Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    private class POMDataEditor extends DataEditorSupport implements EditorCookie.Observable, OpenCookie, EditCookie, PrintCookie, CloseCookie {

        private final SaveCookie save = new SaveCookie() {
            public @Override void save() throws IOException {
                saveDocument();
            }
        };

        private final FileChangeListener listener = new FileChangeAdapter() {
            public @Override void fileChanged(FileEvent fe) {
                updateTitles();
            }
        };

        POMDataEditor() {
            super(POMDataObject.this, new POMEnv(POMDataObject.this));
            getPrimaryFile().addFileChangeListener(FileUtil.weakFileChangeListener(listener, getPrimaryFile()));
        }

        protected @Override boolean notifyModified() {
            if (!super.notifyModified()) {
                return false;
            }
            if (getCookie(SaveCookie.class) == null) {
                getCookieSet().add(save);
                setModified(true);
            }
            return true;
        }

        protected @Override void notifyUnmodified() {
            super.notifyUnmodified();
            if (getCookie(SaveCookie.class) == save) {
                getCookieSet().remove(save);
                setModified(false);
            }
        }

        protected @Override String messageName() {
            return annotateWithProjectName(super.messageName());
        }

        protected @Override String messageHtmlName() {
            String name = super.messageHtmlName();
            return name != null ? annotateWithProjectName(name) : null;
        }

        private String annotateWithProjectName(String name) { // #154508
            if (getPrimaryFile().getNameExt().equals("pom.xml")) { // NOI18N
                try {
                    Element artifactId = XMLUtil.findElement(XMLUtil.parse(new InputSource(getPrimaryFile().getURL().toString()), false, false, XMLUtil.defaultErrorHandler(), null).getDocumentElement(), "artifactId", null); // NOI18N
                    if (artifactId != null) {
                        String text = XMLUtil.findText(artifactId);
                        if (text != null) {
                            return name + " [" + text + "]"; // NOI18N
                        }
                    }
                } catch (IOException x) {
                    LOG.log(Level.INFO, null, x);
                } catch (SAXException x) {
                    LOG.log(Level.FINE, null, x);
                }
            }
            return name;
        }

        // XXX override initializeCloneableEditor if needed; see AntProjectDataEditor

    }

    private static class POMEnv extends DataEditorSupport.Env {

        private static final long serialVersionUID = 1L;

        POMEnv(MultiDataObject d) {
            super(d);
        }

        protected @Override FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        protected @Override FileLock takeLock() throws IOException {
            return ((MultiDataObject) getDataObject()).getPrimaryEntry().takeLock();
        }

        public @Override CloneableOpenSupport findCloneableOpenSupport() {
            return getDataObject().getCookie(POMDataEditor.class);
        }

    }

}
