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

package org.netbeans.modules.versioning;

import org.netbeans.modules.versioning.core.VersioningAnnotationProvider;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.providers.AnnotationProvider;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSAnnotator;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.core.spi.VersioningSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.test.MockLookup;

/**
 *
 * @author ondra
 */
public class VersioningAnnotationProviderTest extends NbTestCase {

    private static VCSAnnotator annotator;
    private StatusListener statusListener;
    private FileObject workDir;
    private static final BufferedImage IMAGE = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
    private static final BufferedImage IMAGE_ANNOTATION = new BufferedImage(6, 6, BufferedImage.TYPE_INT_ARGB);

    public VersioningAnnotationProviderTest(String arg) {
        super(arg);
    }

    protected void setUp () throws IOException {
        MockLookup.setLayersAndInstances();
        File userdir = new File(getWorkDir() + "/userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        System.setProperty("versioning.asyncAnnotator", "true");
        File wDir = new File(getWorkDir(), String.valueOf(System.currentTimeMillis()));
        wDir.mkdirs();
        workDir = FileUtil.toFileObject(wDir);
    }

    public void testProviderPresent () {
        Collection<? extends AnnotationProvider> providers = Lookup.getDefault().lookupAll(AnnotationProvider.class);
        for (AnnotationProvider provider : providers) {
            if (provider.getClass().getName().equals(VersioningAnnotationProvider.class.getName())) {
                return;
            }
        }
        assert false;
    }

    public void testAnnotationChanged () throws Exception {
        DummyVCS.topmostFile = VCSFileProxy.createFileProxy(FileUtil.toFile(workDir));
        HashMap<FileObject, String> expectedLabelAnnotations = new HashMap<FileObject, String>();
        HashMap<FileObject, String> expectedIconToolTips = new HashMap<FileObject, String>();
        expectedLabelAnnotations.put(workDir, workDir.getNameExt() + " - annotated");
        expectedIconToolTips.put(workDir, workDir.getNameExt() + "<br>Annotated");
        FileObject f = workDir.createFolder("folder1");
        expectedLabelAnnotations.put(f, f.getNameExt() + " - annotated");
        expectedIconToolTips.put(f, f.getNameExt() + "<br>Annotated");
        f = f.createFolder("folder1_1");
        expectedLabelAnnotations.put(f, f.getNameExt() + " - annotated");
        expectedIconToolTips.put(f, f.getNameExt() + "<br>Annotated");
        FileObject file = f.createData("file1", "txt");
        expectedLabelAnnotations.put(file, file.getNameExt() + " - annotated");
        expectedIconToolTips.put(file, file.getNameExt() + "<br>Annotated");
        file = f.createData("file2", "txt");
        expectedLabelAnnotations.put(file, file.getNameExt() + " - annotated");
        expectedIconToolTips.put(file, file.getNameExt() + "<br>Annotated");
        f = workDir.createFolder("folder2");
        expectedLabelAnnotations.put(f, f.getNameExt() + " - annotated");
        expectedIconToolTips.put(f, f.getNameExt() + "<br>Annotated");
        f = f.createFolder("folder2_1");
        expectedLabelAnnotations.put(f, f.getNameExt() + " - annotated");
        expectedIconToolTips.put(f, f.getNameExt() + "<br>Annotated");
        file = f.createData("file1", "txt");
        expectedLabelAnnotations.put(file, file.getNameExt() + " - annotated");
        expectedIconToolTips.put(file, file.getNameExt() + "<br>Annotated");
        file = f.createData("file2", "txt");
        expectedLabelAnnotations.put(file, file.getNameExt() + " - annotated");
        expectedIconToolTips.put(file, file.getNameExt() + "<br>Annotated");

        statusListener = new VersioningAnnotationProviderTest.StatusListener(expectedLabelAnnotations.keySet());
        annotator = new VersioningAnnotationProviderTest.DummyVCSAnnotator();
        FileSystem fileSystem = (FileSystem) workDir.getFileSystem();
        fileSystem.addFileStatusListener(statusListener);
        statusListener.startAnnotation(expectedLabelAnnotations.keySet());
        Thread.sleep(500);
        // annotations should not be ready yet, test that
        for (Map.Entry<FileObject, String> e : expectedLabelAnnotations.entrySet()) {
            assertEquals(e.getKey().getNameExt(), statusListener.annotationsLabels.get(e.getKey()));
            Image annotatedIcon = statusListener.annotationsIcons.get(e.getKey());
            assertTrue(10 == annotatedIcon.getWidth(null));
            assertTrue(10 == annotatedIcon.getHeight(null));
            assertEquals(e.getKey().getNameExt(), ImageUtilities.getImageToolTip(annotatedIcon));
        }
        
        statusListener.waitForSilence();
        // annotations should be ready
        for (Map.Entry<FileObject, String> e : expectedLabelAnnotations.entrySet()) {
            assertEquals(e.getValue(), statusListener.annotationsLabels.get(e.getKey()));
        }
        for (Map.Entry<FileObject, String> e : expectedIconToolTips.entrySet()) {
            Image annotatedIcon = statusListener.annotationsIcons.get(e.getKey());
            assertTrue(22 == annotatedIcon.getWidth(null));
            assertTrue(22 == annotatedIcon.getHeight(null));
            assertEquals(e.getValue(), ImageUtilities.getImageToolTip(annotatedIcon));
        }

        statusListener.clear();
        statusListener.startAnnotation(expectedLabelAnnotations.keySet());
        Thread.sleep(500);
        // annotations should be already cached in Versioning AP and should be immediately returned
        for (Map.Entry<FileObject, String> e : expectedLabelAnnotations.entrySet()) {
            assertEquals(e.getValue(), statusListener.annotationsLabels.get(e.getKey()));
        }
        for (Map.Entry<FileObject, String> e : expectedIconToolTips.entrySet()) {
            Image annotatedIcon = statusListener.annotationsIcons.get(e.getKey());
            assertTrue(22 == annotatedIcon.getWidth(null));
            assertTrue(22 == annotatedIcon.getHeight(null));
            assertEquals(e.getValue(), ImageUtilities.getImageToolTip(annotatedIcon));
        }
    }

    private class StatusListener implements FileStatusListener {

        private long lastEvent;
        private Exception ex;
        private HashMap<FileObject, String> annotationsLabels = new HashMap<FileObject, String>();
        private HashMap<FileObject, Image> annotationsIcons = new HashMap<FileObject, Image>();
        private final Set<FileObject> allFiles;

        private StatusListener(Set<FileObject> keySet) {
            this.allFiles = keySet;
        }

        public void annotationChanged(FileStatusEvent ev) {
            HashSet<FileObject> fos = new HashSet<FileObject>();
            for (FileObject fo : allFiles) {
                if (ev.hasChanged(fo)) {
                    fos.add(fo);
                }
            }
            startAnnotation(fos);
        }

        private void clear () {
            annotationsLabels.clear();
            annotationsIcons.clear();
            ex = null;
            lastEvent = 0;
        }

        private void startAnnotation(final Set<FileObject> files) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    lastEvent = System.currentTimeMillis();
                    Collection<? extends AnnotationProvider> providers = Lookup.getDefault().lookupAll(AnnotationProvider.class);
                    for (AnnotationProvider provider : providers) {
                        if (provider instanceof VersioningAnnotationProvider) {
                            long time = System.currentTimeMillis();
                            for (FileObject fo : files) {
                                String name = fo.getNameExt();
                                name = provider.annotateNameHtml(name, Collections.singleton(fo));
                                annotationsLabels.put(fo, name);
                                Image image = ImageUtilities.assignToolTipToImage(VersioningAnnotationProviderTest.IMAGE, fo.getNameExt());
                                ImageUtilities.getImageToolTip(image);
                                image = provider.annotateIcon(image, 0, Collections.singleton(fo));
                                annotationsIcons.put(fo, image);
                            }
                            time = System.currentTimeMillis() - time;
                            if (time > 500) {
                                ex = new Exception("Annotation takes more than 200ms");
                            }
                        }
                    }
                }
            });
        }

        private void waitForSilence() throws Exception {
            while (System.currentTimeMillis() - lastEvent < 10000) {
                if (ex != null) {
                    throw ex;
                }
                Thread.sleep(1000);
            }
        }

    }

    @VersioningSystem.Registration(actionsCategory="dummy", displayName="DummyVCS", menuLabel="DummyVCS", metadataFolderNames="")
    public static class DummyVCS extends VersioningSystem {
        private static VCSFileProxy topmostFile;
        public DummyVCS () {
            
        }

        @Override
        public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file) {
            return topmostFile;
        }

        @Override
        public VCSAnnotator getVCSAnnotator() {
            return annotator;
        }
    }

    private class DummyVCSAnnotator extends VCSAnnotator {

        @Override
        public Image annotateIcon(Image icon, VCSContext context) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            icon = ImageUtilities.mergeImages(icon, VersioningAnnotationProviderTest.IMAGE_ANNOTATION, 16, 16);
            icon = ImageUtilities.addToolTipToImage(icon, "Annotated");
            return icon;
        }

        @Override
        public String annotateName(String name, VCSContext context) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            return name + " - annotated";
        }
    }
}
