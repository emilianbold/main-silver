/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.versioning.history;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.plaf.TextUI;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.versioning.history.AbstractSummaryView.LogEntry.Event;
import org.netbeans.modules.versioning.history.AbstractSummaryView.RevisionItem;
import org.netbeans.modules.versioning.history.AbstractSummaryView.SummaryViewMaster.SearchHighlight;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.VCSHyperlinkProvider;
import org.netbeans.modules.versioning.util.VCSHyperlinkSupport;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
class SummaryCellRenderer implements ListCellRenderer {
    private static final double DARKEN_FACTOR = 0.95;
    private static final double DARKEN_FACTOR_UNINTERESTING = 0.975;

    private final AbstractSummaryView summaryView;
    private final Map<String, VCSKenaiAccessor.KenaiUser> kenaiUsersMap;
    private final VCSHyperlinkSupport linkerSupport;

    private Color selectionBackgroundColor = new JList().getSelectionBackground();
    private Color selectionBackground = selectionBackgroundColor;
    private Color selectionForeground = new JList().getSelectionForeground();

    private ActionRenderer ar = new ActionRenderer();
    private MoreRevisionsRenderer mr = new MoreRevisionsRenderer();
    private DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
    private ListCellRenderer remainingFilesRenderer = new RemainingFilesRenderer();

    private AttributeSet searchHiliteAttrs;

    private static final Icon ICON_COLLAPSED = UIManager.getIcon("Tree.collapsedIcon"); //NOI18N
    private static final Icon ICON_EXPANDED = UIManager.getIcon("Tree.expandedIcon"); //NOI18N
    private static final int INDENT = ICON_EXPANDED.getIconWidth() + 3;
    private static final JLabel EMPTY_SPACE_LABEL = new JLabel();
    private static final String PREFIX_PATH_FROM = NbBundle.getMessage(SummaryCellRenderer.class, "MSG_SummaryCellRenderer.pathPrefixFrom"); //NOI18N
    private Collection<VCSHyperlinkProvider> hpInstances;
    
    Map<Object, ListCellRenderer> renderers = new WeakHashMap<Object, ListCellRenderer>();

    public SummaryCellRenderer(AbstractSummaryView summaryView, final VCSHyperlinkSupport linkerSupport, Map<String, VCSKenaiAccessor.KenaiUser> kenaiUsersMap) {
        this.summaryView = summaryView;
        this.kenaiUsersMap = kenaiUsersMap;
        this.linkerSupport = linkerSupport;
        searchHiliteAttrs = ((FontColorSettings) MimeLookup.getLookup(MimePath.get("text/x-java")).lookup(FontColorSettings.class)).getFontColors("highlight-search"); //NOI18N
    }

    private static Color darker (Color c) {
        return darker(c, DARKEN_FACTOR);
    }

    private static Color darkerUninteresting (Color c) {
        return darker(c, DARKEN_FACTOR_UNINTERESTING);
    }

    private static Color darker (Color c, double factor) {
        return new Color(Math.max((int)(c.getRed() * factor), 0),
             Math.max((int)(c.getGreen() * factor), 0),
             Math.max((int)(c.getBlue() * factor), 0));
    }

    private static Color lessInteresting (Color c, Color bg) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        r += (bg.getRed() - r) / 5;
        g += (bg.getGreen() - g) / 5;
        b += (bg.getBlue() - b) / 5;
        return new Color(r, g, b);
    }

    private static String getColorString(Color c) {
        return "#" + getHex(c.getRed()) + getHex(c.getGreen()) + getHex(c.getBlue()); //NOI18N
    }

    private static String getHex(int i) {
        String hex = Integer.toHexString(i & 0x000000FF);
        if (hex.length() == 1) {
            hex = "0" + hex; //NOI18N
        }
        return hex;
    }

    @Override
    public Component getListCellRendererComponent (JList list, Object value, int index, boolean selected, boolean hasFocus) {
        if (value instanceof AbstractSummaryView.RevisionItem) {
            ListCellRenderer ren = renderers.get(value);
            if (ren == null) {
                ren = new RevisionRenderer();
                renderers.put(value, ren);
            }
            return ren.getListCellRendererComponent(list, value, index, selected, hasFocus);
        } else if (value instanceof AbstractSummaryView.EventItem) {
            ListCellRenderer ren = renderers.get(value);
            if (ren == null) {
                ren = new EventRenderer();
                renderers.put(value, ren);
            }
            return ren.getListCellRendererComponent(list, value, index, selected, hasFocus);
        } else if (value instanceof AbstractSummaryView.LoadingEventsItem) {
            Component comp = dlcr.getListCellRendererComponent(list, NbBundle.getMessage(SummaryCellRenderer.class, "MSG_LoadingEvents"), index, selected, hasFocus); //NOI18N
            if (comp instanceof JComponent) {
                ((JComponent) comp).setBorder(BorderFactory.createEmptyBorder(0, INDENT, 0, 0));
            }
            return comp;
        } else if (value instanceof AbstractSummaryView.ShowAllEventsItem) {
            return remainingFilesRenderer.getListCellRendererComponent(list, value, index, selected, hasFocus);
        } else if (value instanceof AbstractSummaryView.ActionsItem) {
            return ar.getListCellRendererComponent(list, value, index, selected, hasFocus);
        } else if (value instanceof AbstractSummaryView.MoreRevisionsItem) {
            return mr.getListCellRendererComponent(list, value, index, selected, hasFocus);
        }
        return EMPTY_SPACE_LABEL;
    }

    private static final String FIELDS_SEPARATOR = "      "; //NOI18N

    private int getMaxPathWidth (JList list, RevisionItem revision, Graphics g) {
        assert revision.revisionExpanded;
        assert EventQueue.isDispatchThread();
        
        Collection<AbstractSummaryView.LogEntry.Event> events = revision.getUserData().getEvents();
        int maxWidth = -1;
        for (AbstractSummaryView.LogEntry.Event event : events) {
            int i = 0;
            for (String path : getInterestingPaths(event)) {
                if (++i == 2) {
                    if (path == null) {
                        break;
                    } else {
                        path = PREFIX_PATH_FROM + path;
                    }
                }
                StringBuilder sb = new StringBuilder(event.getAction()).append(" ").append(path);
                FontMetrics fm = list.getFontMetrics(list.getFont());
                Rectangle2D rect = fm.getStringBounds(sb.toString(), g);
                maxWidth = Math.max(maxWidth, (int) rect.getWidth() + 1);
            }
        }
        return maxWidth;
    }
    
    public Collection<VCSHyperlinkProvider> getHyperlinkProviders() {
        if (hpInstances == null) {
            Lookup.Result<VCSHyperlinkProvider> hpResult = Lookup.getDefault().lookupResult(VCSHyperlinkProvider.class);
            hpInstances = (Collection<VCSHyperlinkProvider>) hpResult.allInstances();
        }
        return hpInstances;
    }

    public VCSKenaiAccessor.KenaiUser getKenaiUser(String author) {
        VCSKenaiAccessor.KenaiUser kenaiUser = null;
        if (kenaiUsersMap != null && author != null && !author.isEmpty()) {
            kenaiUser = kenaiUsersMap.get(author);
        }
        return kenaiUser;
    }

    private class RevisionRenderer extends JPanel implements ListCellRenderer {

        private String id;
        private final Style selectedStyle;
        private final Style normalStyle;
        private final Style indentStyle;
        private final Style noindentStyle;
        private final Style issueHyperlinkStyle;
        private final Style linkStyle;
        private final Style authorStyle;
        private final Style hiliteStyle;
        private boolean lastSelection = false;
        private final JTextPane textPane;
        private final JButton expandButton;
        private String commitMessage = ""; //NOI18N
        private boolean lastMessageExpanded;
        private boolean lastRevisionExpanded;
        private int lastWidth;
        private Collection<SearchHighlight> lastHighlights;

        public RevisionRenderer() {
            selectionForeground = new JList().getSelectionForeground();
            textPane = new JTextPane();
            expandButton = new LinkButton(ICON_COLLAPSED);
            expandButton.setBorder(BorderFactory.createEmptyBorder());

            selectedStyle = textPane.addStyle("selected", null); //NOI18N
            StyleConstants.setForeground(selectedStyle, selectionForeground);
            StyleConstants.setBackground(selectedStyle, selectionBackground);
            normalStyle = textPane.addStyle("normal", null); //NOI18N
            StyleConstants.setForeground(normalStyle, UIManager.getColor("List.foreground")); //NOI18N
            indentStyle = textPane.addStyle("indent", null); //NOI18N
            StyleConstants.setLeftIndent(indentStyle, 50);
            noindentStyle = textPane.addStyle("noindent", null); //NOI18N
            StyleConstants.setLeftIndent(noindentStyle, 0);

            issueHyperlinkStyle = textPane.addStyle("issuehyperlink", normalStyle); //NOI18N
            StyleConstants.setForeground(issueHyperlinkStyle, Color.BLUE);
            StyleConstants.setUnderline(issueHyperlinkStyle, true);

            linkStyle = textPane.addStyle("link", normalStyle); //NOI18N
            StyleConstants.setForeground(linkStyle, Color.BLUE);
            StyleConstants.setBold(linkStyle, true);

            authorStyle = textPane.addStyle("author", normalStyle); //NOI18N
            StyleConstants.setForeground(authorStyle, Color.BLUE);

            hiliteStyle = textPane.addStyle("hilite", normalStyle); //NOI18N
            
            Color c = (Color) searchHiliteAttrs.getAttribute(StyleConstants.Background);
            if (c != null) StyleConstants.setBackground(hiliteStyle, c);
            c = (Color) searchHiliteAttrs.getAttribute(StyleConstants.Foreground);
            if (c != null) StyleConstants.setForeground(hiliteStyle, c);

            textPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, UIManager.getColor("List.background"))); //NOI18N
            
            add(expandButton);
            expandButton.setMaximumSize(expandButton.getPreferredSize());
            expandButton.setMinimumSize(expandButton.getPreferredSize());
            add(textPane);
        }

        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean selected, boolean hasFocus) {
            AbstractSummaryView.RevisionItem item = (AbstractSummaryView.RevisionItem) value;
            AbstractSummaryView.LogEntry entry = item.getUserData();

            StyledDocument sd = textPane.getStyledDocument();
            Collection<SearchHighlight> highlights = summaryView.getMaster().getSearchHighlights();
            if (sd.getLength() == 0 || selected != lastSelection || item.messageExpanded != lastMessageExpanded || item.revisionExpanded != lastRevisionExpanded
                    || !highlights.equals(lastHighlights)) {
                lastSelection = selected;
                lastMessageExpanded = item.messageExpanded;
                lastRevisionExpanded = item.revisionExpanded;
                lastHighlights = highlights;

                Style style;
                Color backgroundColor;
                Color foregroundColor;

                if (selected) {
                    foregroundColor = selectionForeground;
                    backgroundColor = selectionBackground;
                    style = selectedStyle;
                } else {
                    foregroundColor = UIManager.getColor("List.foreground"); //NOI18N
                    backgroundColor = UIManager.getColor("List.background"); //NOI18N
                    backgroundColor = entry.isLessInteresting() ? darkerUninteresting(backgroundColor) : darker(backgroundColor);
                    style = normalStyle;
                }
                textPane.setOpaque(false);
                textPane.setBackground(new Color(0, 0, 0, 0));
                setBackground(backgroundColor);
                if (item.revisionExpanded) {
                    expandButton.setIcon(ICON_EXPANDED);
                } else {
                    expandButton.setIcon(ICON_COLLAPSED);
                }

                id = item.getItemId();
                if (linkerSupport.getLinker(ExpandLink.class, id) == null) {
                    linkerSupport.add(new ExpandLink(item), id);
                }

                try {
                    // clear document
                    sd.remove(0, sd.getLength());
                    sd.setParagraphAttributes(0, sd.getLength(), noindentStyle, false);

                    // add revision
                    sd.insertString(0, item.getUserData().getRevision(), null);
                    sd.setCharacterAttributes(0, sd.getLength(), normalStyle, false);
                    if (!selected) {
                        for (AbstractSummaryView.LogEntry.RevisionHighlight highlight : item.getUserData().getRevisionHighlights()) {
                            Style s = textPane.addStyle(null, normalStyle);
                            StyleConstants.setForeground(s, highlight.getForeground());
                            StyleConstants.setBackground(s, highlight.getBackground());
                            sd.setCharacterAttributes(highlight.getStart(), highlight.getLength(), s, false);
                        }
                        for (SearchHighlight highlight : highlights) {
                            if (highlight.getKind() == SearchHighlight.Kind.REVISION) {
                                int doclen = sd.getLength();
                                String highlightMessage = highlight.getSearchText();
                                String revisionText = item.getUserData().getRevision().toLowerCase();
                                int idx = revisionText.indexOf(highlightMessage);
                                if (idx > -1) {
                                    sd.setCharacterAttributes(doclen - revisionText.length() + idx, highlightMessage.length(), hiliteStyle, false);
                                }
                            }
                        }
                    }

                    // add author
                    sd.insertString(sd.getLength(), FIELDS_SEPARATOR, style);
                    String author = entry.getAuthor();
                    VCSHyperlinkSupport.StyledDocumentHyperlink l = linkerSupport.getLinker(VCSHyperlinkSupport.AuthorLinker.class, id);
                    if(l == null) {
                        VCSKenaiAccessor.KenaiUser kenaiUser = getKenaiUser(author);
                        if (kenaiUser != null) {
                            l = new VCSHyperlinkSupport.AuthorLinker(kenaiUser, authorStyle, sd, author);
                            linkerSupport.add(l, id);
                        }
                    }
                    int pos = sd.getLength();
                    if(l != null) {
                        l.insertString(sd, selected ? style : null);
                    } else {
                        sd.insertString(sd.getLength(), author, style);
                    }
                    if (!selected) {
                        for (SearchHighlight highlight : highlights) {
                            if (highlight.getKind() == SearchHighlight.Kind.AUTHOR) {
                                int doclen = sd.getLength();
                                String highlightMessage = highlight.getSearchText();
                                String authorText = sd.getText(pos, doclen - pos).toLowerCase();
                                int idx = authorText.indexOf(highlightMessage);
                                if (idx > -1) {
                                    sd.setCharacterAttributes(doclen - authorText.length() + idx, highlightMessage.length(), hiliteStyle, false);
                                }
                            }
                        }
                    }

                    // add date
                    sd.insertString(sd.getLength(), FIELDS_SEPARATOR + entry.getDate(), null);

                    // add commit msg
                    boolean messageChanged = !entry.getMessage().equals(commitMessage);
                    commitMessage = entry.getMessage();
                    if (commitMessage.endsWith("\n")) commitMessage = commitMessage.substring(0, commitMessage.length() - 1); //NOI18N
                    sd.insertString(sd.getLength(), "\n", null); //NOI18N
                    int nlc, i;
                    for (i = 0, nlc = -1; i != -1 ; i = commitMessage.indexOf('\n', i + 1), nlc++);
                    if (nlc > 0 && !item.messageExpanded) {
                        commitMessage = commitMessage.substring(0, commitMessage.indexOf("\n")); //NOI18N
                    }

                    // compute issue hyperlinks
                    l = linkerSupport.getLinker(VCSHyperlinkSupport.IssueLinker.class, id);
                    if (messageChanged) {
                        lastWidth = -1;
                        if (l != null) {
                            // must reinitialize issue linker to paint the new message
                            linkerSupport.remove(l, id);
                            l = null;
                        }
                    }
                    if(l == null) {
                        for (VCSHyperlinkProvider hp : getHyperlinkProviders()) {
                            l = VCSHyperlinkSupport.IssueLinker.create(hp, issueHyperlinkStyle, summaryView.getRoot(), sd, commitMessage);
                            if(l != null) {
                                linkerSupport.add(l, id);
                                break; // get the first one
                            }
                        }
                    }
                    pos = sd.getLength();
                    if(l != null) {
                        l.insertString(sd, style);
                    } else {
                        sd.insertString(sd.getLength(), commitMessage, style);
                    }

                    // tooltip for message
                    MessageTooltip mtt = linkerSupport.getLinker(MessageTooltip.class, id);
                    if (messageChanged) {
                        linkerSupport.remove(mtt, id);
                        mtt = null;
                    }
                    if (mtt == null) {
                        linkerSupport.add(new MessageTooltip(entry.getMessage(), pos, sd.getLength()), id);
                    }
                    
                    // paint first line of commit message bold
                    int lineEnd = sd.getText(pos, sd.getLength() - pos).indexOf("\n");
                    if (lineEnd == -1) {
                        lineEnd = sd.getLength() - pos;
                    }
                    Style s = textPane.addStyle(null, style);
                    StyleConstants.setBold(s, true);
                    sd.setCharacterAttributes(pos, lineEnd, s, false);
                    int msglen = commitMessage.length();
                    int doclen = sd.getLength();

                    if (nlc > 0 && !item.messageExpanded) {
                        l = linkerSupport.getLinker(ExpandMsgHyperlink.class, id);
                        if (l == null) {
                            l = new ExpandMsgHyperlink(item, sd.getLength(), id);
                            linkerSupport.add(l, id);
                        }
                        l.insertString(sd, linkStyle);
                    }
                    

                    if (!selected) {
                        for (SearchHighlight highlight : highlights) {
                            if (highlight.getKind() == SearchHighlight.Kind.MESSAGE) {
                                String highlightMessage = highlight.getSearchText();
                                int idx = commitMessage.toLowerCase().indexOf(highlightMessage);
                                if (idx == -1) {
                                    if (nlc > 0 && !item.messageExpanded && entry.getMessage().toLowerCase().contains(highlightMessage)) {
                                        sd.setCharacterAttributes(doclen, sd.getLength(), hiliteStyle, false);
                                    }
                                } else {
                                    sd.setCharacterAttributes(doclen - msglen + idx, highlightMessage.length(), hiliteStyle, false);
                                }
                            }
                        }
                    }

                    if (selected) {
                        sd.setCharacterAttributes(0, Integer.MAX_VALUE, style, false);
                    }
                } catch (BadLocationException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
            lastWidth = resizePane(textPane.getText(), list, lastWidth);

            return this;
        }
        
        @SuppressWarnings("empty-statement")
        private int resizePane(String text, JList list, int lastWidth) {
            if(text == null) {
                text = ""; //NOI18N
            }
            int width = summaryView.getMaster().getComponent().getWidth();
            if (width > 0 && width != lastWidth) {
                String[] rows = text.split("\n"); //NOI18N
                FontMetrics fm = list.getFontMetrics(list.getFont());
                int lines = 0;
                for (String row : rows) {
                    Rectangle2D rect = fm.getStringBounds(row, textPane.getGraphics());
                    lines += (int) (rect.getWidth() / (width - 80) + 1);
                }
                int ph = fm.getHeight() * lines + 9;
                textPane.setPreferredSize(new Dimension(width - 50 - ICON_COLLAPSED.getIconWidth(), ph));
                setPreferredSize(textPane.getPreferredSize());
            }
            return width;
        }
        
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            linkerSupport.computeBounds(textPane, id);
            ExpandLink link = linkerSupport.getLinker(ExpandLink.class, id);
            if (link != null) {
                link.computeBounds(expandButton);
            }
        }
    }
    
    private static class MessageTooltip extends VCSHyperlinkSupport.Hyperlink {
        private Rectangle[] bounds;
        private final int start;
        private final int end;
        private final String text;

        private MessageTooltip (String text, int start, int end) {
            this.start = start;
            this.end = end;
            this.text = prepareText(text);
        }
        
        @Override
        public boolean mouseMoved (Point p, JComponent component) {
            if (bounds != null && component.getToolTipText() == null) {
                for (Rectangle b : bounds) {
                    if (b.contains(p)) {
                        component.setToolTipText(text);
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean mouseClicked (Point p) {
            return false;
        }

        @Override
        public void computeBounds (JTextPane textPane) {
            Rectangle tpBounds = textPane.getBounds();
            TextUI tui = textPane.getUI();
            try {
                int lastY = -1;
                Rectangle rec = null;
                List<Rectangle> rects = new LinkedList<Rectangle>();
                // get bounds for every line
                for (int pos = start; pos <= end; ++pos) {
                    Rectangle startr = tui.modelToView(textPane, pos, Position.Bias.Forward);
                    Rectangle endr = tui.modelToView(textPane, pos + 1, Position.Bias.Backward);
                    if (startr.y > lastY) {
                        rects.add(rec);
                        rec = new Rectangle(tpBounds.x + startr.x, startr.y, endr.x - startr.x, startr.height);
                        lastY = rec.y;
                    } else {
                        rec.setSize(rec.width + endr.x - startr.x, rec.height);
                    }
                }
                rects.add(rec);
                rects.remove(0);
                bounds = rects.toArray(new Rectangle[rects.size()]);
            } catch (BadLocationException ex) {
                bounds = null;
            }
        }

        private String prepareText (String text) {
            text = text.replaceAll("\n", "<br>"); //NOI18N
            return "<html><body>" + text + "</body></html>"; //NOI18N
        }
        
    }

    private class EventRenderer extends JPanel implements ListCellRenderer {
        
        private boolean lastSelection = false;
        private final JLabel pathLabel;
        private final JLabel actionLabel;
        private final JButton actionButton;
        private String id;
        private final String PATH_COLOR = getColorString(lessInteresting(UIManager.getColor("List.foreground"), UIManager.getColor("List.background"))); //NOI18N

        public EventRenderer () {
            pathLabel = new JLabel();
            actionLabel = new JLabel();
            actionButton = new LinkButton("..."); //NOI18N
            actionButton.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

            FlowLayout l = new FlowLayout(FlowLayout.LEFT, 0, 0);
            l.setAlignOnBaseline(true);
            setLayout(l);
            add(actionLabel);
            actionLabel.setBorder(BorderFactory.createEmptyBorder(0, INDENT, 0, 10));
            add(pathLabel);
            add(actionButton);
        }
        
        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean selected, boolean hasFocus) {
            AbstractSummaryView.EventItem item = (AbstractSummaryView.EventItem) value;
            if (pathLabel.getText().isEmpty() || lastSelection != selected) {
                lastSelection = selected;
                Color foregroundColor, backgroundColor;
                if (selected) {
                    foregroundColor = selectionForeground;
                    backgroundColor = selectionBackground;
                } else {
                    foregroundColor = UIManager.getColor("List.foreground"); //NOI18N
                    backgroundColor = UIManager.getColor("List.background"); //NOI18N
                }
                id = item.getItemId();
                if (linkerSupport.getLinker(ExpandLink.class, id) == null) {
                    linkerSupport.add(new EventActionsLink(item), id);
                }
                pathLabel.setFont(list.getFont());
                pathLabel.setForeground(foregroundColor);
                pathLabel.setBackground(backgroundColor);
                actionLabel.setBackground(backgroundColor);
                setBackground(backgroundColor);

                StringBuilder sb = new StringBuilder("<html><body>"); //NOI18N
                sb.append("<b>"); //NOI18N
                String action = item.getUserData().getAction();
                String color = summaryView.getActionColors().get(action);
                if (color != null && !selected) {
                    sb.append("<font color=\"").append(color).append("\">").append(action).append("</font>"); //NOI18N
                } else  {
                    actionLabel.setForeground(foregroundColor);
                    sb.append(action);
                }
                sb.append("</b></body></html>"); //NOI18N
                actionLabel.setText(sb.toString());

                sb = new StringBuilder("<html><body>"); //NOI18N
                int i = 0;
                for (String path : getInterestingPaths(item.getUserData())) {
                    if (++i == 2 && path == null) {
                        continue;
                    }
                    int idx = path.lastIndexOf("/"); //NOI18N
                    if (i == 2) {
                        // additional path information (like replace from, copied from, etc.)
                        sb.append("<br>").append(PREFIX_PATH_FROM); //NOI18N
                    }
                    if (idx < 0 || selected) {
                        sb.append(path);
                    } else {
                        ++idx;
                        sb.append("<font color=\"").append(PATH_COLOR).append("\">").append(path.substring(0, idx)).append("</font>"); //NOI18N
                        sb.append(path.substring(idx, path.length()));
                    }
                }
                pathLabel.setText(sb.append("</body></html>").toString()); //NOI18N
                int width = getMaxPathWidth(list, item.getParent(), pathLabel.getGraphics());
                width = width + 15 + INDENT - actionLabel.getPreferredSize().width;
                pathLabel.setPreferredSize(new Dimension(width, pathLabel.getPreferredSize().height));
            }
            return this;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            EventActionsLink link = linkerSupport.getLinker(EventActionsLink.class, id);
            if (link != null) {
                link.computeBounds(actionButton);
            }
        }

    }

    private static String[] getInterestingPaths (Event event) {
        List<String> paths = new ArrayList<String>(2);
        String path = event.getPath();
        String original = event.getOriginalPath();
        paths.add(path);
        if (original != null && !path.equals(original)) {
            paths.add(original);
        }
        return paths.toArray(new String[paths.size()]);
    }
    
    private class RemainingFilesRenderer extends JPanel implements ListCellRenderer{
        private String id;
        private Component comp;

        public RemainingFilesRenderer () {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setBorder(BorderFactory.createEmptyBorder(0, INDENT, 3, 0));
        }

        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            id = ((AbstractSummaryView.ShowAllEventsItem) value).getItemId();
            if (linkerSupport.getLinker(ShowRemainingFilesLink.class, id) == null) {
                linkerSupport.add(new ShowRemainingFilesLink(((AbstractSummaryView.ShowAllEventsItem) value).getParent()), id);
            }
            StringBuilder sb = new StringBuilder("<html><a href=\"expand\">"); //NOI18N
            if (isSelected) {
                Component c = dlcr.getListCellRendererComponent(list, "<html><a href=\"expand\">ACTION_NAME</a>", index, isSelected, cellHasFocus); //NOI18N
                sb.append("<font color=\"").append(getColorString(c.getForeground())).append("\">") //NOI18N
                        .append(NbBundle.getMessage(SummaryCellRenderer.class, "MSG_ShowAllFiles")).append("</font>"); //NOI18N
            } else {
                sb.append(NbBundle.getMessage(SummaryCellRenderer.class, "MSG_ShowAllFiles")); //NOI18N
            }
            sb.append("</a></html>"); //NOI18N
            comp = dlcr.getListCellRendererComponent(list, sb.toString(), index, isSelected, cellHasFocus);
            removeAll();
            add(comp);
            comp.setMaximumSize(comp.getPreferredSize());
            setBackground(comp.getBackground());
            return this;
        }

        @Override
        public void paint (Graphics g) {
            super.paint(g);
            ShowRemainingFilesLink link = linkerSupport.getLinker(ShowRemainingFilesLink.class, id);
            if (link != null) {
                link.computeBounds(comp);
            }
        }
        
    }

    private class ActionRenderer extends JPanel implements ListCellRenderer{
        private String id;
        private Map<Component, Action> labels;
        private final Map<String, JLabel> ACTION_LABELS = new HashMap<String, JLabel>();

        public ActionRenderer () {
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
            setBorder(BorderFactory.createEmptyBorder(3, INDENT - 5, 5, 0));
        }
        
        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Action[] actions = ((AbstractSummaryView.ActionsItem) value).getParent().getUserData().getActions();
            id = ((AbstractSummaryView.ActionsItem) value).getItemId();
            removeAll();
            labels = new HashMap<Component, Action>(actions.length);
            Component comp = dlcr.getListCellRendererComponent(list, "<html><a href=\"action\">ACTION_NAME</a>", index, isSelected, cellHasFocus); //NOI18N
            setBackground(comp.getBackground());
            for (Action a : actions) {
                JLabel label = getLabelFor((String) a.getValue(Action.NAME), isSelected ? comp.getForeground() : null);
                label.setForeground(comp.getForeground());
                label.setBackground(comp.getBackground());
                label.setBorder(BorderFactory.createEmptyBorder());
                labels.put(label, a);
                add(label);
            }
            if (linkerSupport.getLinker(ActionHyperlink.class, id) == null) {
                linkerSupport.add(new ActionHyperlink(), id);
            }
            return this;
        }

        @Override
        public void paint (Graphics g) {
            super.paint(g);
            ActionHyperlink link = linkerSupport.getLinker(ActionHyperlink.class, id);
            if (link != null) {
                link.computeBounds(labels);
            }
        }

        private JLabel getLabelFor (String actionName, Color fontColor) {
            JLabel lbl = ACTION_LABELS.get(actionName);
            if (lbl== null) {
                lbl = new JLabel();
                ACTION_LABELS.put(actionName, lbl);
            }
            StringBuilder sb = new StringBuilder("<html><a href=\"action\">"); //NOI18N
            if (fontColor == null) {
                sb.append(actionName);
            } else {
                sb.append("<font color=\"").append(getColorString(fontColor)).append("\">").append(actionName).append("</font>"); //NOI18N
            }
            sb.append("</a></html>"); //NOI18N
            lbl.setText(sb.toString());
            return lbl;
        }
        
    }

    private class MoreRevisionsRenderer extends JPanel implements ListCellRenderer{
        private String id;
        private final List<JLabel> labels;
        private final Color backgroundColor;
        private final JLabel more10Label;
        private final JLabel allLabel;
        private final JLabel more100Label;
        private final JLabel more50Label;
        private final Map<Component, String> tooltips;
        private final Map<Component, Integer> moreLabelValues;

        public MoreRevisionsRenderer () {
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 3));
            setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, UIManager.getColor("List.background"))); //NOI18N
            labels = new ArrayList<JLabel>();
            labels.add(new JLabel(NbBundle.getMessage(SummaryCellRenderer.class, "MSG_ShowMore"))); //NOI18N
            labels.add(more10Label = new JLabel());
            labels.add(new JLabel("/")); //NOI18N
            labels.add(more50Label = new JLabel());
            labels.add(new JLabel("/")); //NOI18N
            labels.add(more100Label = new JLabel());
            labels.add(new JLabel("/")); //NOI18N
            labels.add(allLabel = new JLabel());
            labels.add(new JLabel(NbBundle.getMessage(SummaryCellRenderer.class, "MSG_ShowMoreSuffix"))); //NOI18N
            for (JLabel lbl : labels) {
                lbl.setBorder(BorderFactory.createEmptyBorder());
                add(lbl);
            }
            labels.get(0).setBorder(BorderFactory.createEmptyBorder(0, INDENT, 0, 0));
            backgroundColor = darker(UIManager.getColor("List.background")); //NOI18N
            
            moreLabelValues = new HashMap<Component, Integer>(4);
            moreLabelValues.put(more10Label, 10);
            moreLabelValues.put(more50Label, 50);
            moreLabelValues.put(more100Label, 100);
            moreLabelValues.put(allLabel, -1);
            
            tooltips = new HashMap<Component, String>(4);
            tooltips.put(more10Label, NbBundle.getMessage(SummaryCellRenderer.class, "MSG_Show10MoreRevisions")); //NOI18N
            tooltips.put(more50Label, NbBundle.getMessage(SummaryCellRenderer.class, "MSG_Show50MoreRevisions")); //NOI18N
            tooltips.put(more100Label, NbBundle.getMessage(SummaryCellRenderer.class, "MSG_Show100MoreRevisions")); //NOI18N
            tooltips.put(allLabel, NbBundle.getMessage(SummaryCellRenderer.class, "MSG_ShowMoreRevisionsAll")); //NOI18N
        }
        
        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            id = ((AbstractSummaryView.MoreRevisionsItem) value).getItemId();
            if (linkerSupport.getLinker(MoreRevisionsHyperlink.class, id) == null) {
                linkerSupport.add(new MoreRevisionsHyperlink(), id);
            }
            Component comp = dlcr.getListCellRendererComponent(list, "<html><a href=\"more\">MORE</a>", index, isSelected, cellHasFocus); //NOI18N
            setLabelLinkText(more10Label, "10", isSelected ? comp.getForeground() : null); //NOI18N
            setLabelLinkText(more50Label, "50", isSelected ? comp.getForeground() : null); //NOI18N
            setLabelLinkText(more100Label, "100", isSelected ? comp.getForeground() : null); //NOI18N
            setLabelLinkText(allLabel, NbBundle.getMessage(SummaryCellRenderer.class, "MSG_AllRevisions"), isSelected ? comp.getForeground() : null); //NOI18N
            for (JLabel lbl : labels) {
                lbl.setForeground(comp.getForeground());
                lbl.setBackground(isSelected ? comp.getBackground() : backgroundColor);
            }
            setBackground(isSelected ? comp.getBackground() : backgroundColor);
            return this;
        }

        @Override
        public void paint (Graphics g) {
            super.paint(g);
            MoreRevisionsHyperlink link = linkerSupport.getLinker(MoreRevisionsHyperlink.class, id);
            if (link != null) {
                link.computeBounds();
            }
        }

        private JLabel setLabelLinkText (JLabel lbl, String text, Color fgColor) {
            StringBuilder sb = new StringBuilder("<html><a href=\"more\">"); //NOI18N
            if (fgColor == null) {
                sb.append(text);
            } else {
                sb.append("<font color=\"").append(getColorString(fgColor)).append("\">").append(text).append("</font>"); //NOI18N
            }
            sb.append("</a></html>"); //NOI18N
            lbl.setText(sb.toString());
            return lbl;
        }

        private class MoreRevisionsHyperlink extends VCSHyperlinkSupport.Hyperlink {
            private Map<Component, Rectangle> bounds;

            @Override
            public void computeBounds (JTextPane textPane) {

            }

            public void computeBounds () {
                bounds = new HashMap<Component, Rectangle>(labels.size());
                for (JLabel lbl : new JLabel[] { more10Label, more50Label, more100Label, allLabel }) {
                    bounds.put(lbl, lbl.getBounds());
                }
            }

            @Override
            public boolean mouseMoved (Point p, JComponent component) {
                for (Map.Entry<Component, Rectangle> e : bounds.entrySet()) {
                    if (e.getValue().contains(p)) {
                        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        component.setToolTipText(tooltips.get(e.getKey()));
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean mouseClicked (Point p) {
                for (Map.Entry<Component, Rectangle> e : bounds.entrySet()) {
                    if (e.getValue().contains(p)) {
                        summaryView.moreRevisions(moreLabelValues.get(e.getKey()));
                        return true;
                    }
                }
                return false;
            }
        }
    }

    private class ActionHyperlink extends VCSHyperlinkSupport.Hyperlink {
        private Map<Component, Rectangle> bounds;
        private Map<Component, Action> labels;


        public ActionHyperlink () {
        }

        @Override
        public void computeBounds (JTextPane textPane) {
            
        }
        
        public void computeBounds (Map<Component, Action> labels) {
            this.labels = labels;
            bounds = new HashMap<Component, Rectangle>(labels.size());
            for (Map.Entry<Component, Action> e : labels.entrySet()) {
                bounds.put(e.getKey(), e.getKey().getBounds());
            }
        }

        @Override
        public boolean mouseMoved (Point p, JComponent component) {
            for (Map.Entry<Component, Rectangle> e : bounds.entrySet()) {
                if (e.getValue().contains(p)) {
                    component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    component.setToolTipText((String) labels.get(e.getKey()).getValue(Action.NAME));
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean mouseClicked (Point p) {
            for (Map.Entry<Component, Rectangle> e : bounds.entrySet()) {
                if (e.getValue().contains(p)) {
                    Utils.setWaitCursor(true);
                    try {
                        labels.get(e.getKey()).actionPerformed(new ActionEvent(labels.get(e.getKey()), ActionEvent.ACTION_PERFORMED, null));
                    } finally {
                        Utils.setWaitCursor(false);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    private static final String LINK_STRING = "..."; //NOI18N
    private static final int LINK_STRING_LEN = LINK_STRING.length();
    private class ExpandMsgHyperlink extends VCSHyperlinkSupport.StyledDocumentHyperlink {
        private Rectangle bounds;
        private final int startoffset;
        private final AbstractSummaryView.RevisionItem item;
        private final String revision;

        public ExpandMsgHyperlink (AbstractSummaryView.RevisionItem item, int startoffset, String revision) {
            this.startoffset = startoffset;
            this.revision = revision;
            this.item = item;
        }

        @Override
        public boolean mouseMoved(Point p, JComponent component) {
            if (bounds != null && bounds.contains(p)) {
                component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                component.setToolTipText(NbBundle.getMessage(SummaryCellRenderer.class, "MSG_ExpandCommitMessage")); //NOI18N
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseClicked(Point p) {
            if (bounds != null && bounds.contains(p)) {
                item.messageExpanded = true;
                linkerSupport.remove(this, revision);
                summaryView.itemChanged(p);
                return true;
            }
            return false;
        }

        @Override
        public void computeBounds(JTextPane textPane) {
            Rectangle tpBounds = textPane.getBounds();
            TextUI tui = textPane.getUI();
            bounds = new Rectangle();
            try {
                Rectangle mtv = tui.modelToView(textPane, startoffset, Position.Bias.Forward);
                if(mtv == null) return;
                Rectangle startr = mtv.getBounds();
                mtv = tui.modelToView(textPane, startoffset + LINK_STRING_LEN, Position.Bias.Backward);
                if(mtv == null) return;
                Rectangle endr = mtv.getBounds();

                bounds = new Rectangle(tpBounds.x + startr.x, startr.y, endr.x - startr.x, startr.height);
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void insertString (StyledDocument sd, Style style) throws BadLocationException {
            sd.insertString(startoffset, LINK_STRING, style);
        }
    }

    public class ExpandLink extends VCSHyperlinkSupport.Hyperlink {

        private Rectangle bounds;
        private final AbstractSummaryView.RevisionItem item;

        private ExpandLink (AbstractSummaryView.RevisionItem item) {
            this.item = item;
        }

        @Override
        public void computeBounds (JTextPane textPane) {
            
        }

        public void computeBounds (JButton button) {
            bounds = button.getBounds();
        }

        @Override
        public boolean mouseMoved(Point p, JComponent component) {
            if (bounds != null && bounds.contains(p)) {
                component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                if (item.revisionExpanded) {
                    component.setToolTipText(NbBundle.getMessage(SummaryCellRenderer.class, "MSG_CollapseRevision")); //NOI18N
                } else {
                    component.setToolTipText(NbBundle.getMessage(SummaryCellRenderer.class, "MSG_ExpandRevision")); //NOI18N
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseClicked(Point p) {
            if (bounds != null && bounds.contains(p)) {
                item.getUserData().cancelExpand();
                item.setExpanded(!item.revisionExpanded);
                summaryView.itemChanged(p);
                return true;
            }
            return false;
        }
    }

    public class EventActionsLink extends VCSHyperlinkSupport.Hyperlink {

        private Rectangle bounds;
        private final AbstractSummaryView.EventItem item;

        private EventActionsLink (AbstractSummaryView.EventItem item) {
            this.item = item;
        }

        @Override
        public void computeBounds (JTextPane textPane) {
            
        }

        public void computeBounds (JButton button) {
            bounds = button.getBounds();
        }

        @Override
        public boolean mouseMoved(Point p, JComponent component) {
            if (bounds != null && bounds.contains(p)) {
                component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                component.setToolTipText(NbBundle.getMessage(SummaryCellRenderer.class, "MSG_ShowActions")); //NOI18N
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseClicked(Point p) {
            if (bounds != null && bounds.contains(p)) {
                item.actionsToPopup(p);
                return true;
            }
            return false;
        }
    }
    
    public class ShowRemainingFilesLink extends VCSHyperlinkSupport.Hyperlink {

        private Rectangle bounds;
        private final AbstractSummaryView.RevisionItem item;

        private ShowRemainingFilesLink (AbstractSummaryView.RevisionItem item) {
            this.item = item;
        }

        @Override
        public void computeBounds (JTextPane textPane) {
            
        }

        public void computeBounds (Component component) {
            bounds = component.getBounds();
        }

        @Override
        public boolean mouseMoved(Point p, JComponent component) {
            if (bounds != null && bounds.contains(p)) {
                component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                component.setToolTipText(NbBundle.getMessage(SummaryCellRenderer.class, "MSG_ShowAllFiles")); //NOI18N
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseClicked(Point p) {
            if (bounds != null && bounds.contains(p)) {
                summaryView.showRemainingFiles(item);
                return true;
            }
            return false;
        }
    }

}
