/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.annotationsupport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.accessibility.Accessible;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.StatusBar;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;

/**
 *
 * @author ak119685
 */
public class AnnotationBar extends JComponent implements Accessible, PropertyChangeListener, DocumentListener, ChangeListener, ActionListener, ComponentListener, MouseMotionListener {

    //TestAnnotationsPanel mainPanel = new TestAnnotationsPanel();
    /**
     * Target text component for which the annotation bar is aiming.
     */
    private FileAnnotationInfo fileAnnotationInfo;
    private final JTextComponent textComponent;
    /**
     * User interface related to the target text component.
     */
    private final EditorUI editorUI;
    /**
     * Fold hierarchy of the text component user interface.
     */
    private final FoldHierarchy foldHierarchy;
    /**
     * Document related to the target text component.
     */
    private final BaseDocument doc;
    /**
     * Caret of the target text component.
     */
    private final Caret caret;
    /**
     * Controls annotation bar visibility.
     */
    private boolean annotated;
    private Color backgroundColor = Color.WHITE;
    private Color foregroundColor = Color.BLACK;
//    private Color BOX_COLOR = new Color(200, 200, 200);
    private Color metricsFG = null;
    private Color metricsBG = null;
    private Color navigationBarFG = null;
    private Color navigationBarBG = null;
    /**
     * Most recent status message.
     */
    private String recentStatusMessage = "";

    public AnnotationBar(JTextComponent target) {
        this.textComponent = target;
        this.editorUI = Utilities.getEditorUI(target);
        this.foldHierarchy = FoldHierarchy.get(editorUI.getComponent());
        this.doc = editorUI.getDocument();
        this.caret = textComponent.getCaret();
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
//    this.setLayout(new BorderLayout());
//    this.add(mainPanel, BorderLayout.CENTER);
    }

    private void getColors() {
        EditorSettings editorSettings = EditorSettings.getDefault();
        Map<String, AttributeSet> map = editorSettings.getHighlightings(editorSettings.getCurrentFontColorProfile());
        AttributeSet aSet = map.get("dlight-metrics-annotations"); // NOI18N
        metricsFG = (Color) aSet.getAttribute(StyleConstants.Foreground);
        metricsBG = (Color) aSet.getAttribute(StyleConstants.Background);
        aSet = map.get("dlight-metrics-text-highlighting"); // NOI18N
        navigationBarFG = (Color) aSet.getAttribute(StyleConstants.Foreground);
        navigationBarBG = (Color) aSet.getAttribute(StyleConstants.Background);
    }

    private Color getMetricsFGColor() {
        if (metricsFG == null) {
            getColors();
        }
        return metricsFG;
    }

    private Color getMetricsBGColor() {
        if (metricsBG == null) {
            getColors();
        }
        return metricsBG;
    }

    private Color getNavigationBarFGColor() {
        if (navigationBarFG == null) {
            getColors();
        }
        return navigationBarFG;
    }

    private Color getTextHighlightBGColor() {
        if (navigationBarBG == null) {
            getColors();
        }
        return navigationBarBG;
    }

    private Color backgroundColor() {
        if (getMetricsBGColor() != null) {
            return getMetricsBGColor();
        }
        if (textComponent != null) {
            return textComponent.getBackground();
        }
        return backgroundColor;
    }

    private Color foregroundColor() {
        if (getMetricsFGColor() != null) {
            return getMetricsFGColor();
        }
        if (textComponent != null) {
            return textComponent.getForeground();
        }
        return foregroundColor;
    }

    private Color defaultBackground() {
        if (textComponent != null) {
            return textComponent.getBackground();
        }
        return backgroundColor;
    }

    /**
     * Components created by SibeBarFactory are positioned
     * using a Layout manager that determines componnet size
     * by retireving preferred size.
     *
     * <p>Once componnet needs resizing it simply calls
     * {@link #revalidate} that triggers new layouting
     * that consults prefered size.
     */
    @Override
    public Dimension getPreferredSize() {
        if (!annotated || fileAnnotationInfo == null) {
            return new Dimension(0, 0);
        }
        Dimension dim = textComponent.getSize();
        int width = getBarWidth();
        dim.width = width;
        dim.height *= 2;  // XXX
        return dim;
    }

    /**
     * Gets the preferred width of this component.
     *
     * @return the preferred width of this component
     */
    private int getBarWidth() {
        String sample = fileAnnotationInfo.getLineAnnotationInfo().get(0).getAnnotation();
        int cwidth = getGraphics().getFontMetrics().charWidth('X');
        return sample.length() * cwidth + 7; // thp: controls bar width
    }

    /**
     * Makes the bar visible and sensitive to
     * LogOutoutListener events that should deliver
     * actual content to be displayed.
     */
    public void annotate(FileAnnotationInfo fileAnnotationInfo) {
        if (fileAnnotationInfo == null) {
            return;
        }
        this.fileAnnotationInfo = fileAnnotationInfo;
        setToolTipText(fileAnnotationInfo.getTooltip());
        annotated = true;

        doc.addDocumentListener(this);
        textComponent.addComponentListener(this);
        editorUI.addPropertyChangeListener(this);
        EditorSettings.getDefault().addPropertyChangeListener(this);
        addMouseMotionListener(this);

        List<AnnotationMark> marks = new ArrayList<AnnotationMark>();
        int index = 0;
        for (LineAnnotationInfo lineAnnotationInfo : fileAnnotationInfo.getLineAnnotationInfo()) {
            //setHighlight((StyledDocument) doc, lineAnnotationInfo.getLine(), lineAnnotationInfo.getLine(), getTextHighlightBGColor(), getTextHighlightFGColor());
            marks.add(index++, new AnnotationMark(lineAnnotationInfo.getLine() - 1, lineAnnotationInfo.getTooltip(), getNavigationBarFGColor()));
        }

        AnnotationMarkProvider amp = AnnotationMarkInstaller.getMarkProvider(textComponent);
        if (amp != null) {
            amp.setMarks(marks);
        }
        revalidate();  // resize the component
    }

    public void unAnnotate() {
        annotated = false;

        for (LineAnnotationInfo lineAnnotationInfo : fileAnnotationInfo.getLineAnnotationInfo()) {
            setHighlight((StyledDocument) doc, lineAnnotationInfo.getLine(), lineAnnotationInfo.getLine(), new Color(255, 255, 255), null);
        }

        List<AnnotationMark> marks = new ArrayList<AnnotationMark>();
        AnnotationMarkProvider amp = AnnotationMarkInstaller.getMarkProvider(textComponent);
        if (amp != null) {
            amp.setMarks(marks);
        }

        doc.removeDocumentListener(this);
        textComponent.removeComponentListener(this);
        editorUI.removePropertyChangeListener(this);
        EditorSettings.getDefault().removePropertyChangeListener(this);
        removeMouseMotionListener(this);

        revalidate();  // resize the component

    }

    private void setHighlight(StyledDocument doc, int line1, int line2, Color bgColor, Color fgColor) {
        if (bgColor == null) {
            return;
        }
        for (int line = line1 - 1; line < line2; line++) {
            if (line < 0) {
                continue;
            }
            int offset = org.openide.text.NbDocument.findLineOffset(doc, line);
            if (offset >= 0) {
                Style s = doc.getLogicalStyle(offset);
                if (s == null) {
                    s = doc.addStyle("gizmo-style(" + bgColor + "):1000", null); // NOI18N
                }
                if (bgColor != null) {
                    s.addAttribute(StyleConstants.ColorConstants.Background, bgColor);
                }
//                if (fgColor != null) {
//                    s.addAttribute(StyleConstants.ColorConstants.Foreground, fgColor);
//                }
                doc.setLogicalStyle(offset, s);
            }
        }
    }

    /**
     * GlyphGutter copy pasted bolerplate method.
     * It invokes {@link #paintView} that contains
     * actual business logic.
     */
    @Override
    public void paintComponent(Graphics g) {
        if (!annotated || fileAnnotationInfo == null) {
            return;
        }
        super.paintComponent(g);

        Rectangle clip = g.getClipBounds();

        JTextComponent component = editorUI.getComponent();
        if (component == null) {
            return;
        }

        BaseTextUI textUI = (BaseTextUI) component.getUI();
        View rootView = Utilities.getDocumentView(component);
        if (rootView == null) {
            return;
        }

        g.setColor(defaultBackground());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        AbstractDocument adoc = (AbstractDocument) component.getDocument();
        adoc.readLock();
        try {
            foldHierarchy.lock();
            try {
                int startPos = textUI.getPosFromY(clip.y);
                int startViewIndex = rootView.getViewIndex(startPos, Position.Bias.Forward);
                int rootViewCount = rootView.getViewCount();

                if (startViewIndex >= 0 && startViewIndex < rootViewCount) {
                    // find the nearest visible line with an annotation
                    Rectangle rec = textUI.modelToView(component, rootView.getView(startViewIndex).getStartOffset());
                    int y = (rec == null) ? 0 : rec.y;

                    int clipEndY = clip.y + clip.height;
                    for (int i = startViewIndex; i < rootViewCount; i++) {
                        View view = rootView.getView(i);
                        paintView(view, g, y);
                        y += editorUI.getLineHeight();
                        if (y >= clipEndY) {
                            break;
                        }
                    }
                }

            } finally {
                foldHierarchy.unlock();
            }
        } catch (BadLocationException ble) {
            System.out.println("ble: " + ble.getMessage());
            ble.printStackTrace();
        } finally {
            adoc.readUnlock();
        }
    }

    /**
     * Paints one view that corresponds to a line (or
     * multiple lines if folding takes effect).
     */
    private void paintView(View view, Graphics g, int yBase) {
        JTextComponent component = editorUI.getComponent();
        if (component == null) {
            return;
        }
        BaseTextUI textUI = (BaseTextUI) component.getUI();
        Element rootElem = textUI.getRootView(component).getElement();
        int offset = view.getStartOffset();
        int line = rootElem.getElementIndex(offset);
//        LineAnnotationInfo lineAnnotationInfo = fileAnnotationInfo.getLineAnnotationInfoByLine(line + 1);
        LineAnnotationInfo lineAnnotationInfo = fileAnnotationInfo.getLineAnnotationInfoByLineOffset(offset);
        if (lineAnnotationInfo != null) {
            Rectangle clip = g.getClipBounds();
            int x1 = clip.x;
            int x2 = clip.width-1;
            int y1 = yBase;
            int y2 = editorUI.getLineHeight();
            // paint background
            g.setColor(backgroundColor());
            g.fillRect(x1, y1+1, x2, y2-2);
            // paint box
//            g.setColor(BOX_COLOR);
//            g.drawRect(x1, y1, x2, y2);
//            // make corners round
//            g.setColor(backgroundColor());
//            g.drawLine(x1, y1, x1, y1);
//            g.drawLine(x1+x2, y1, x1+x2, y1);
//            g.drawLine(x1, y1+y2, x1, y1+y2);
//            g.drawLine(x1+x2, y1+y2, x1+x2, y1+y2);
            // paint text
            String annotation = lineAnnotationInfo.getAnnotation();
            g.setFont(editorUI.getComponent().getFont());
            g.setColor(foregroundColor());
            g.drawString(annotation, 4, yBase + editorUI.getLineAscent());
            lineAnnotationInfo.setY(yBase, yBase + editorUI.getLineHeight());
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null) {
            return;
        }
        String id = evt.getPropertyName();
        if (EditorUI.COMPONENT_PROPERTY.equals(id)) {  // NOI18N
            if (evt.getNewValue() == null) {
                // component deinstalled, lets uninstall all isteners
                release();
            }
        }
        if ("editorFontColors".equals(id)) {  // NOI18N
            if (evt.getNewValue() != null && ((String) evt.getNewValue()).equals("NetBeans")) { // NOI18N
                metricsFG = null;
                metricsBG = null;
                navigationBarFG = null;
                navigationBarBG = null;

                if (annotated) {
                    unAnnotate();
                    annotate(fileAnnotationInfo);
                }
            }
        }
    }

    /**
     * Pair method to {@link #annotate}. It releases
     * all resources.
     */
    private void release() {
        editorUI.removePropertyChangeListener(this);
        textComponent.removeComponentListener(this);
        doc.removeDocumentListener(this);
        caret.removeChangeListener(this);
//    if (caretTimer != null) {
//      caretTimer.removeActionListener(this);
//    }
//    elementAnnotations = null;

        // cancel running annotation task if active
//    if (latestAnnotationTask != null) {
//      latestAnnotationTask.cancel();
//    }

        AnnotationMarkProvider amp = AnnotationMarkInstaller.getMarkProvider(textComponent);
        if (amp != null) {
            amp.setMarks(Collections.<AnnotationMark>emptyList());
        }

        clearRecentFeedback();
    }

    /**
     * Clears the status bar if it contains the latest status message
     * displayed by this annotation bar.
     */
    private void clearRecentFeedback() {
        StatusBar statusBar = editorUI.getStatusBar();
        if (statusBar.getText(StatusBar.CELL_MAIN) == recentStatusMessage) {
            statusBar.setText(StatusBar.CELL_MAIN, "");  // NOI18N
        }
    }

    public void insertUpdate(DocumentEvent e) {
    }

    public void removeUpdate(DocumentEvent e) {
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void stateChanged(ChangeEvent e) {
    }

    public void actionPerformed(ActionEvent e) {
    }

    public void componentResized(ComponentEvent e) {
        revalidate();
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
        String tooltip = fileAnnotationInfo.getTooltip();
        LineAnnotationInfo lineAnnotationInfo = fileAnnotationInfo.getLineAnnotationInfoByYCoordinate(e.getY());
        if (lineAnnotationInfo != null) {
            tooltip = lineAnnotationInfo.getTooltip();
        }
        setToolTipText(tooltip);
    }

    public void mouseDragged(MouseEvent e) {
    }


}
