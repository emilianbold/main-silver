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
package org.netbeans.modules.dlight.annotationsupport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.api.support.FunctionMetricFormatter;
import org.netbeans.modules.dlight.core.stack.dataprovider.SourceFileInfoDataProvider;
import org.netbeans.modules.dlight.core.stack.spi.AnnotatedSourceSupport;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author thp
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.dlight.core.stack.spi.AnnotatedSourceSupport.class)
public class AnnotatedSourceSupportImpl implements AnnotatedSourceSupport {

    private final static Logger log = Logger.getLogger("dlight.annotationsupport"); // NOI18N
    private static boolean checkedLogging = checkLogging();
    private static boolean logginIsOn;
    private HashMap<String, FileAnnotationInfo> activeAnnotations = new HashMap<String, FileAnnotationInfo>();

    public AnnotatedSourceSupportImpl() {
//        WindowManager.getDefault().getRegistry().addPropertyChangeListener(new EditorFileChangeListener());
        AnnotationSupport.getInstance().addPropertyChangeListener(new ProfilerPropertyChangeListener());
        EditorRegistry.addPropertyChangeListener(new EditorFileChangeListener());
    }

    private void preProcessAnnotations(SourceFileInfoDataProvider sourceFileInfoProvider, List<Column> metrics, List<FunctionCallWithMetric> list, boolean lineAnnotations) {
        if (list == null || list.size() == 0) {
            return;
        }
        for (FunctionCallWithMetric functionCall : list) {
            SourceFileInfo sourceFileInfo = sourceFileInfoProvider.getSourceFileInfo(functionCall);
            if (sourceFileInfo != null) {
                if (sourceFileInfo.isSourceKnown()) {
                    String filePath = sourceFileInfo.getFileName();
                    FileAnnotationInfo fileAnnotationInfo = activeAnnotations.get(filePath);
                    if (fileAnnotationInfo == null) {
                        fileAnnotationInfo = new FileAnnotationInfo();
                        fileAnnotationInfo.setFilePath(filePath);
                        fileAnnotationInfo.setColumnNames(new String[metrics.size()]);
                        fileAnnotationInfo.setMaxColumnWidth(new int[metrics.size()]);
                        activeAnnotations.put(filePath, fileAnnotationInfo);
                    }
                    LineAnnotationInfo lineAnnotationInfo = new LineAnnotationInfo(fileAnnotationInfo);
                    lineAnnotationInfo.setLine(sourceFileInfo.getLine());
                    lineAnnotationInfo.setOffset(sourceFileInfo.getOffset());
                    lineAnnotationInfo.setColumns(new String[metrics.size()]);
                    boolean below = true;
                    int col = 0;
                    for (Column column : metrics) {
                        String metricId = column.getColumnName();
                        Object metricVal = functionCall.getMetricValue(metricId);
                        String metricValString = FunctionMetricFormatter.getFormattedValue(functionCall, metricId);
                        if (!metricValString.equals("0.0")) { // NOI18N
                            below = false;
                        }
                        lineAnnotationInfo.getColumns()[col] = metricValString;
                        int metricValLength = metricValString.length();
                        if (fileAnnotationInfo.getMaxColumnWidth()[col] < metricValLength) {
                            fileAnnotationInfo.getMaxColumnWidth()[col] = metricValLength;
                        }

                        String metricUName = column.getColumnUName();
                        fileAnnotationInfo.getColumnNames()[col] = metricUName;

                        col++;
                    }
                    if (lineAnnotations && !below) {
                        // line annotation (none zero)
                        fileAnnotationInfo.getLineAnnotationInfo().add(lineAnnotationInfo);
                    }
                    if (!lineAnnotations) {
                        // block annotation
                        fileAnnotationInfo.getBlockAnnotationInfo().add(lineAnnotationInfo);
                    }
                }
            }
        }
    }

    public synchronized void updateSource(SourceFileInfoDataProvider sourceFileInfoProvider, List<Column> metrics, List<FunctionCallWithMetric> list, List<FunctionCallWithMetric> functionCalls) {
        // log(sourceFileInfoProvider, metrics, list, functionCalls);
        activeAnnotations = new HashMap<String, FileAnnotationInfo>();
        preProcessAnnotations(sourceFileInfoProvider, metrics, list, true);
        preProcessAnnotations(sourceFileInfoProvider, metrics, functionCalls, false);
        // Check current focused file in editor whether it should be annotated
        annotateCurrentFocusedFile();
    }

    private File fileFromEditorPane(JTextComponent jEditorPane) {
        File ret = null;
        if (jEditorPane != null) {
            Object source = jEditorPane.getDocument().getProperty(Document.StreamDescriptionProperty);
            if (source instanceof DataObject) {
                FileObject fo = ((DataObject) source).getPrimaryFile();
                ret = FileUtil.toFile(fo);
            }
        }
        return ret;
    }

//    public void updateSource(SourceFileInfoDataProvider sourceFileInfoProvider, List<Column> metrics, List<FunctionCallWithMetric> functionCalls) {
//        updateSource(sourceFileInfoProvider, metrics, functionCalls, true);
//    }
//
//    public void updateSourceWithBlockAnnotations(SourceFileInfoDataProvider sourceFileInfoProvider, List<Column> metrics, List<FunctionCallWithMetric> functionCalls) {
//        updateSource(sourceFileInfoProvider, metrics, functionCalls, false);
//    }
//    private File activatedFile(Node node) {
//        DataObject dobj = node.getCookie(DataObject.class);
//        if (dobj != null) {
//            FileObject fo = dobj.getPrimaryFile();
//            return FileUtil.toFile(fo);
//        }
//        return null;
//    }
//
//    private JEditorPane activatedEditorPane(Node node) {
//        EditorCookie ec = node.getCookie(EditorCookie.class);
//        if (ec == null) {
//            return null;
//        }
//
//        JEditorPane[] panes = ec.getOpenedPanes();
//        if (panes == null) {
//            ec.open();
//        }
//
//        panes = ec.getOpenedPanes();
//        if (panes == null) {
//            return null;
//        }
//        JEditorPane currentPane = panes[0];
//        return currentPane;
//    }
    private synchronized void annotateCurrentFocusedFile() {
        if (activeAnnotations.size() == 0) {
            return;
        }
        JTextComponent jEditorPane = EditorRegistry.focusedComponent();
        if (jEditorPane == null) {
            jEditorPane = EditorRegistry.lastFocusedComponent();
        }
        if (jEditorPane != null) {
            File textFile = fileFromEditorPane(jEditorPane);
            if (textFile != null) {
                final String filePath = textFile.getAbsolutePath();
                final FileAnnotationInfo fileAnnotationInfo = activeAnnotations.get(filePath);
                if (fileAnnotationInfo != null) {
                    if (!fileAnnotationInfo.isAnnotated()) {
                        fileAnnotationInfo.setEditorPane((JEditorPane) jEditorPane);
                        fileAnnotationInfo.setAnnotated(true);
                    }
                    SwingUtilities.invokeLater(new Annotate(jEditorPane, fileAnnotationInfo));
                }
            }
        }
    }

    class Annotate implements Runnable {

        JTextComponent jEditorPane;
        FileAnnotationInfo fileAnnotationInfo;

        public Annotate(JTextComponent jEditorPane, FileAnnotationInfo fileAnnotationInfo) {
            this.jEditorPane = jEditorPane;
            this.fileAnnotationInfo = fileAnnotationInfo;
        }

        public void run() {
            AnnotationBarManager.showAnnotationBar(jEditorPane, fileAnnotationInfo);
        }
    }

    class EditorFileChangeListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(EditorRegistry.FOCUS_GAINED_PROPERTY)) {
                DLightExecutorService.submit(new Runnable() {

                    public void run() {
                        annotateCurrentFocusedFile();
                    }
                }, "Annotate current focused file");//NOI18N
            }
        }
    }

    class ProfilerPropertyChangeListener implements PropertyChangeListener {

        public synchronized void propertyChange(PropertyChangeEvent evt) {
            final HashMap<String, FileAnnotationInfo> activeAnnotationsClone = (HashMap<String, FileAnnotationInfo>) activeAnnotations.clone();
            String prop = evt.getPropertyName();
            if (prop.equals(AnnotationSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE)) {
                boolean annotate = AnnotationSupport.getInstance().getTextAnnotationVisible();
                if (annotate) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            for (FileAnnotationInfo fileAnnotationInfo : activeAnnotationsClone.values()) {
                                if (fileAnnotationInfo.isAnnotated()) {
                                    AnnotationBarManager.showAnnotationBar((JTextComponent) fileAnnotationInfo.getEditorPane(), fileAnnotationInfo);
                                }
                            }
                        }
                    });
                } else {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            for (FileAnnotationInfo fileAnnotationInfo : activeAnnotationsClone.values()) {
                                if (fileAnnotationInfo.isAnnotated()) {
                                    AnnotationBarManager.hideAnnotationBar((JTextComponent) fileAnnotationInfo.getEditorPane());
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    private void log(SourceFileInfoDataProvider sourceFileInfoProvider, List<Column> metrics, List<FunctionCallWithMetric> functionCalls, boolean lineAnnotations) {
        if (!logginIsOn) {
            return;
        }
        log.fine("AnnotatedSourceSupportImpl.updateSource");
        log.finest("metrics:");
        for (Column column : metrics) {
            log.finest("  getColumnLongUName " + column.getColumnLongUName());
            log.finest("  getColumnName " + column.getColumnName());
            log.finest("  getColumnUName " + column.getColumnUName());
            log.finest("  getExpression = " + column.getExpression());
            log.finest("");
        }
        log.finest("functionCalls:");
        for (FunctionCallWithMetric functionCall : functionCalls) {
            log.finest("  getDisplayedName " + functionCall.getDisplayedName());
            log.finest("  getFunction " + functionCall.getFunction());
            log.finest("  getFunction().getName() " + functionCall.getFunction().getName());
            log.finest("  getFunction().getQuilifiedName() " + functionCall.getFunction().getSignature());

            SourceFileInfo sourceFileInfo = sourceFileInfoProvider.getSourceFileInfo(functionCall);
            if (sourceFileInfo != null) {
                if (sourceFileInfo.isSourceKnown()) {
                    log.finer(sourceFileInfo.getFileName() + "\n"); // NOI18N
                }
                log.finer("  type=" + (lineAnnotations ? "Line" : "Block") + "\n"); // NOI18N);
                log.finer("  line=" + sourceFileInfo.getLine() + "\n"); // NOI18N);
                log.finer("  column=" + sourceFileInfo.getColumn() + "\n"); // NOI18N););
                log.finer("  offset=" + sourceFileInfo.getOffset() + "\n"); // NOI18N););
                for (Column column : metrics) {
                    String metricId = column.getColumnName();
                    Object metricVal = functionCall.getMetricValue(metricId);
                    String metricUName = column.getColumnUName();
                    log.finer("  " + metricUName + "=" + metricVal + "\n"); // NOI18N
                }
            }
            log.finest("  " + functionCall);
        }
    }

    private static boolean checkLogging() {
        if (checkedLogging) {
            return true;
        }
        logginIsOn = false;
        String logProp = System.getProperty("dlight.annotationsupport"); // NOI18N
        if (logProp != null) {
            logginIsOn = true;
            if (logProp.equals("FINE")) { // NOI18N
                log.setLevel(Level.FINE);
            } else if (logProp.equals("FINER")) { // NOI18N
                log.setLevel(Level.FINER);
            } else if (logProp.equals("FINEST")) { // NOI18N
                log.setLevel(Level.FINEST);
            }
        }
        return true;
    }
}
