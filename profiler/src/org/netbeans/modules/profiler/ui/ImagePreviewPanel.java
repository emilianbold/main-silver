/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.profiler.ui;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import javax.swing.*;
import org.netbeans.lib.profiler.ui.UIUtils;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ImagePreviewPanel_NotAvailableMsg=Preview not available",
    "ImagePreviewPanel_GeneratingPreviewMsg=Generating preview...",
    "ImagePreviewPanel_NoImageMsg=No image"
})
public class ImagePreviewPanel extends JPanel {
    //~ Inner Interfaces ---------------------------------------------------------------------------------------------------------

    public static interface ImageProvider {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        BufferedImage getImage();
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final int PREVIEW_THRESHOLD = 2000000;
    private static int instanceCounter = 0;
    private static final Dimension ZERO_SIZE = new Dimension();

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private RequestProcessor.Task currentTask;
    private JLabel displayer;
    private RequestProcessor processor;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ImagePreviewPanel() {
        processor = new RequestProcessor("ImagePreviewPanel-Processor-" + instanceCounter++, 1, true); // NOI18N
        initComponents();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setImage(final ImageProvider imageProvider) {
        clearImage();

        currentTask = processor.post(new Runnable() {
                public void run() {
                    try {
                        if (Thread.interrupted()) {
                            setImageImpl(null, Bundle.ImagePreviewPanel_NotAvailableMsg());
                            return;
                        }
                        
                        // get image from the provider (long-running)
                        BufferedImage image = imageProvider.getImage();
                        
                        if (Thread.interrupted()) image = null;
                        
                        if (image == null) {
                            setImageImpl(null, Bundle.ImagePreviewPanel_NotAvailableMsg());
                            return;
                        }

                        // compute fit size (short-running)
                        Dimension fitSize = getImageFitSize(image.getWidth(), image.getHeight());
                        int width = fitSize.width;
                        int height = fitSize.height;

                        if (Thread.interrupted()) {
                            image = null;
                            setImageImpl(null, Bundle.ImagePreviewPanel_NotAvailableMsg());
                            return;
                        }

                        ImageIcon scaledPreview;

                        // Use Image.SCALE_FAST algorithm for large images to show something in short time
                        if ((image.getWidth() * image.getHeight()) > PREVIEW_THRESHOLD) {
                            // generate raw-scaled preview (mid-running)
                            scaledPreview = new ImageIcon(image.getScaledInstance(Math.max(1, width), Math.max(1, height),
                                                                                  Image.SCALE_FAST));

                            if (Thread.interrupted()) {
                                image = null;
                                setImageImpl(null, Bundle.ImagePreviewPanel_NotAvailableMsg());
                                return;
                            }

                            // update preview area (short-running)
                            setImageImpl(scaledPreview, null);
                        }

                        if (Thread.interrupted()) {
                            image = null;
                            setImageImpl(null, Bundle.ImagePreviewPanel_NotAvailableMsg());
                            return;
                        }

                        // generate fine-scaled preview (long-running)
                        scaledPreview = new ImageIcon(image.getScaledInstance(Math.max(1, width), Math.max(1, height),
                                                                              Image.SCALE_SMOOTH));

                        image = null;

                        if (Thread.interrupted()) {
                            setImageImpl(null, Bundle.ImagePreviewPanel_NotAvailableMsg());
                            return;
                        }

                        // update preview area (short-running)
                        setImageImpl(scaledPreview, null);
                    } catch (OutOfMemoryError e) {
                        setImageImpl(null, Bundle.ImagePreviewPanel_NotAvailableMsg());
                    }
                }
            });
    }
    
    private void setImageImpl(final Icon icon, final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                displayer.setText(text);
                if (icon != null) repaint();
                displayer.setIcon(icon);
            }
        });
    }

    public void clearImage() {
        displayer.setIcon(null);
        displayer.setText(Bundle.ImagePreviewPanel_GeneratingPreviewMsg());

        if (currentTask != null) {
            currentTask.cancel();
        }
    }

    public void reset() {
        displayer.setIcon(null);
        displayer.setText(Bundle.ImagePreviewPanel_NoImageMsg());

        if (currentTask != null) {
            currentTask.cancel();
        }
    }

    private Dimension getClientSize() {
        Insets insets = displayer.getInsets();
        Dimension size = displayer.getSize().equals(ZERO_SIZE) ? displayer.getPreferredSize() : displayer.getSize();

        return new Dimension(size.width - insets.left - insets.right, size.height - insets.top - insets.bottom);
    }

    private Dimension getImageFitSize(int imageWidth, int imageHeight) {
        Dimension displayerSize = getClientSize();
        int displayerWidth = displayerSize.width;
        int displayerHeight = displayerSize.height;

        float imageRatio = (float) imageWidth / (float) imageHeight;
        float displayerRatio = (float) displayerWidth / (float) displayerHeight;

        if (imageRatio > displayerRatio) {
            // will resize according to width
            int newWidth = displayerWidth;
            int newHeight = (int) ((float) newWidth / imageRatio);

            return new Dimension(newWidth, newHeight);
        } else if (imageRatio < displayerRatio) {
            // will resize according to height
            int newHeight = displayerHeight;
            int newWidth = (int) ((float) newHeight * imageRatio);

            return new Dimension(newWidth, newHeight);
        } else {
            // Most optimistic, both displayer and image have the same ratios
            return displayerSize;
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        displayer = new JLabel();
        displayer.setHorizontalAlignment(JLabel.CENTER);
        displayer.setBackground(UIUtils.getProfilerResultsBackground());
        displayer.setOpaque(true);
        displayer.setPreferredSize(new Dimension(200, 200));
        displayer.setBorder(BorderFactory.createCompoundBorder(UIManager.getBorder("TextField.border"),
                                                               BorderFactory.createEmptyBorder(3, 3, 3, 3))); // NOI18N
        add(displayer, BorderLayout.CENTER);
    }
}
