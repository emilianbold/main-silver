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

package org.netbeans.modules.maven;

import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import static org.netbeans.modules.maven.Bundle.*;
import org.netbeans.modules.maven.api.problem.ProblemReport;
import org.netbeans.modules.maven.problems.ProblemReporterImpl;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * implementation of AuxiliaryConfiguration that relies on FileObject's attributes
 * for the non shared elements and on ${basedir}/nb-configuration file for share ones.
 * @author mkleint
 */
public class M2AuxilaryConfigImpl implements AuxiliaryConfiguration {
    public static final String BROKEN_NBCONFIG = "BROKENNBCONFIG"; //NOI18N

    private static final String AUX_CONFIG = "AuxilaryConfiguration"; //NOI18N
    public static final String CONFIG_FILE_NAME = "nb-configuration.xml"; //NOI18N

    private static final Logger LOG = Logger.getLogger(M2AuxilaryConfigImpl.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(M2AuxilaryConfigImpl.class);
    private static final int SAVING_DELAY = 100;
    private final NbMavenProjectImpl project;
    private RequestProcessor.Task savingTask;
    private Document scheduledDocument;
    private Date timeStamp = new Date(0);
    private Document cachedDoc;
    private final Object configIOLock = new Object();

    public M2AuxilaryConfigImpl(NbMavenProjectImpl proj) {
        this.project = proj;
        savingTask = RP.create(new Runnable() {
            public @Override void run() {
                try {
                    project.getProjectDirectory().getFileSystem().runAtomicAction(new AtomicAction() {
                        public @Override void run() throws IOException {
                            Document doc;
                            synchronized (M2AuxilaryConfigImpl.this) {
                                doc = scheduledDocument;
                                if (doc == null) {
                                    return;
                                }
                                scheduledDocument = null;
                            }
                            synchronized (configIOLock) {
                                FileObject config = project.getProjectDirectory().getFileObject(CONFIG_FILE_NAME);
                                if (doc.getDocumentElement().getElementsByTagName("*").getLength() > 0) {
                                    OutputStream out = config == null ? project.getProjectDirectory().createAndOpen(CONFIG_FILE_NAME) : config.getOutputStream();
                                    LOG.log(Level.FINEST, "Write configuration file for {0}", project.getProjectDirectory());
                                    try {
                                        XMLUtil.write(doc, out, "UTF-8"); //NOI18N
                                    } finally {
                                        out.close();
                                    }
                                } else if (config != null) {
                                    LOG.log(Level.FINEST, "Delete empty configuration file for {0}", project.getProjectDirectory());
                                    config.delete();
                                }
                            }
                        }
                    });
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    private Document loadConfig(FileObject config) throws IOException, SAXException {
        synchronized (configIOLock) {
            //TODO shall be have some kind of caching here to prevent frequent IO?
            return XMLUtil.parse(new InputSource(config.toURL().toString()), false, true, null, null);
        }
    }

    public @Override Element getConfigurationFragment(String elementName, String namespace, boolean shared) {
        Element e = doGetConfigurationFragment(elementName, namespace, shared);
        return e != null ? cloneSafely(e) : null;
    }
    // Copied from AntProjectHelper.
    private static final DocumentBuilder db;
    static {
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }
    private static Element cloneSafely(Element el) { // #190845
        // #50198: for thread safety, use a separate document.
        // Using XMLUtil.createDocument is much too slow.
        synchronized (db) {
            Document dummy = db.newDocument();
            return (Element) dummy.importNode(el, true);
        }
    }
    @Messages({
        "TXT_Problem_Broken_Config=Broken nb-configuration.xml file.",
        "# {0} - parser error message", "DESC_Problem_Broken_Config=The $project_basedir/nb-configuration.xml file cannot be parsed. "
            + "The information contained in the file will be ignored until fixed. "
            + "This affects several features in the IDE that will not work properly as a result.\n\n "
            + "The parsing exception follows:\n{0}"
    })
    private synchronized Element doGetConfigurationFragment(final String elementName, final String namespace, boolean shared) {
        if (shared) {
            //first check the document schedule for persistence
            if (scheduledDocument != null) {
                Element el = XMLUtil.findElement(scheduledDocument.getDocumentElement(), elementName, namespace);
                if (el != null) {
                    el = (Element) el.cloneNode(true);
                }
                return el;
            }
            final FileObject config = project.getProjectDirectory().getFileObject(CONFIG_FILE_NAME);
            if (config != null) {
                if (config.lastModified().after(timeStamp)) {
                    // we need to re-read the config file..
                    try {
                        Document doc = loadConfig(config);
                        cachedDoc = doc;
                        return XMLUtil.findElement(doc.getDocumentElement(), elementName, namespace);
                    } catch (SAXException ex) {
                        ProblemReporterImpl impl = project.getProblemReporter();
                        if (!impl.hasReportWithId(BROKEN_NBCONFIG)) {
                            ProblemReport rep = new ProblemReport(ProblemReport.SEVERITY_MEDIUM,
                                    TXT_Problem_Broken_Config(),
                                    DESC_Problem_Broken_Config(ex.getMessage()),
                                    new OpenConfigAction(config));
                            rep.setId(BROKEN_NBCONFIG);
                            impl.addReport(rep);
                        }
                        LOG.log(Level.INFO, ex.getMessage(), ex);
                        cachedDoc = null;
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                        cachedDoc = null;
                    } finally {
                        timeStamp = config.lastModified();
                    }
                    return null;
                } else {
                    //reuse cached value if available;
                    if (cachedDoc != null) {
                        return XMLUtil.findElement(cachedDoc.getDocumentElement(), elementName, namespace);
                    }
                }
            } else {
                // no file.. remove possible cache
                cachedDoc = null;
            }
            return null;
        } else {
            String str = (String) project.getProjectDirectory().getAttribute(AUX_CONFIG);
            if (str != null) {
                Document doc;
                try {
                    doc = XMLUtil.parse(new InputSource(new StringReader(str)), false, true, null, null);
                    return XMLUtil.findElement(doc.getDocumentElement(), elementName, namespace);
                } catch (SAXException ex) {
                    LOG.log(Level.FINE, "cannot parse", ex);
                } catch (IOException ex) {
                    LOG.log(Level.FINE, "error reading private auxiliary configuration", ex);
                }
            }
            return null;
        }
    }

    public @Override synchronized void putConfigurationFragment(final Element fragment, final boolean shared) throws IllegalArgumentException {
        Document doc = null;
        if (shared) {
            if (scheduledDocument != null) {
                doc = scheduledDocument;
            } else {
                FileObject config = project.getProjectDirectory().getFileObject(CONFIG_FILE_NAME);
                if (config != null) {
                    try {
                        doc = loadConfig(config);
                    } catch (SAXException ex) {
                        LOG.log(Level.INFO, "Cannot parse file " + config.getPath(), ex);
                        if (config.getSize() == 0) {
                            //something got wrong in the past..
                            doc = createNewSharedDocument();
                        }
                    } catch (IOException ex) {
                        LOG.log(Level.INFO, "IO Error with " + config.getPath(), ex);
                    }
                } else {
                    doc = createNewSharedDocument();
                }
            }
        } else {
            String str = (String) project.getProjectDirectory().getAttribute(AUX_CONFIG);
            if (str != null) {
                try {
                    doc = XMLUtil.parse(new InputSource(new StringReader(str)), false, true, null, null);
                } catch (SAXException ex) {
                    LOG.log(Level.FINE, "cannot parse", ex);
                } catch (IOException ex) {
                    LOG.log(Level.FINE, "error reading private auxiliary configuration", ex);
                }
            }
            if (doc == null) {
                String element = "project-private"; // NOI18N
                doc = XMLUtil.createDocument(element, null, null, null);
            }
        }
        if (doc != null) {
            Element el = XMLUtil.findElement(doc.getDocumentElement(), fragment.getNodeName(), fragment.getNamespaceURI());
            if (el != null) {
                doc.getDocumentElement().removeChild(el);
            }
            doc.getDocumentElement().appendChild(doc.importNode(fragment, true));

            if (shared) {
                if (scheduledDocument == null) {
                    scheduledDocument = doc;
                }
                LOG.log(Level.FINEST, "Schedule saving of configuration fragment for " + project.getProjectDirectory(), new Exception());
                savingTask.schedule(SAVING_DELAY);
            } else {
                try {
                    ByteArrayOutputStream wr = new ByteArrayOutputStream();
                    XMLUtil.write(doc, wr, "UTF-8"); //NOI18N
                    project.getProjectDirectory().setAttribute(AUX_CONFIG, wr.toString("UTF-8"));
                } catch (IOException ex) {
                    LOG.log(Level.FINE, "error writing private auxiliary configuration", ex);
                }
            }
        }

    }

    public @Override synchronized boolean removeConfigurationFragment(final String elementName, final String namespace, final boolean shared) throws IllegalArgumentException {
        Document doc = null;
        FileObject config = project.getProjectDirectory().getFileObject(CONFIG_FILE_NAME);
        if (shared) {
            if (scheduledDocument != null) {
                doc = scheduledDocument;
            } else {
                if (config != null) {
                    try {
                        try {
                            doc = loadConfig(config);
                        } catch (SAXException ex) {
                            LOG.log(Level.INFO, "Cannot parse file " + config.getPath(), ex);
                            if (config.getSize() == 0) {
                                //just delete the empty file, something got wrong a while back..
                                config.delete();
                            }
                            return true;
                        }
                    } catch (IOException ex) {
                        LOG.log(Level.INFO, "IO Error with " + config.getPath(), ex);
                    }
                } else {
                    return false;
                }
            }
        } else {
            String str = (String) project.getProjectDirectory().getAttribute(AUX_CONFIG);
            if (str != null) {
                try {
                    doc = XMLUtil.parse(new InputSource(new StringReader(str)), false, true, null, null);
                } catch (SAXException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                return false;
            }
        }
        if (doc != null) {
            Element el = XMLUtil.findElement(doc.getDocumentElement(), elementName, namespace);
            if (el != null) {
                doc.getDocumentElement().removeChild(el);
            }
            if (shared) {
                if (scheduledDocument == null) {
                    scheduledDocument = doc;
                }
                LOG.log(Level.FINEST, "Schedule saving of configuration fragment for " + project.getProjectDirectory(), new Exception());
                savingTask.schedule(SAVING_DELAY);
            } else {
                try {
                    ByteArrayOutputStream wr = new ByteArrayOutputStream();
                    XMLUtil.write(doc, wr, "UTF-8"); //NOI18N
                    project.getProjectDirectory().setAttribute(AUX_CONFIG, wr.toString("UTF-8"));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return true;
    }

    static class OpenConfigAction extends AbstractAction {

        private FileObject fo;

        @Messages("TXT_OPEN_FILE=Open File")
        OpenConfigAction(FileObject file) {
            putValue(Action.NAME, TXT_OPEN_FILE());
            fo = file;
        }


        public @Override void actionPerformed(ActionEvent e) {
            if (fo != null) {
                try {
                    DataObject dobj = DataObject.find(fo);
                    EditCookie edit = dobj.getLookup().lookup(EditCookie.class);
                    edit.edit();
                } catch (DataObjectNotFoundException ex) {
                    LOG.log(Level.FINEST, "no dataobject for " + fo, ex);
                }
            }
        }
    }

    private Document createNewSharedDocument() throws DOMException {
        String element = "project-shared-configuration";
        Document doc = XMLUtil.createDocument(element, null, null, null);
        doc.getDocumentElement().appendChild(doc.createComment(
                "\nThis file contains additional configuration written by modules in the NetBeans IDE.\n" +
                "The configuration is intended to be shared among all the users of project and\n" +
                "therefore it is assumed to be part of version control checkout.\n" +
                "Without this configuration present, some functionality in the IDE may be limited or fail altogether.\n"));
        return doc;
    }
}
