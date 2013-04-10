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
 *
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

package org.netbeans.modules.bugtracking.issuetable;

import java.util.regex.Matcher;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.issuetable.IssueNode.IssueProperty;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.util.TextUtils;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class QueryTableCellRenderer extends DefaultTableCellRenderer {
    public static final String PROPERTY_FORMAT = "format";                      // NOI18N
    public static final String PROPERTY_HIGHLIGHT_PATTERN = "highlightPattern"; // NOI18N

    private final QueryImpl query;
    private final IssueTable issueTable;

    private static final int VISIBLE_START_CHARS = 0;
    private static final Icon seenValueIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/bugtracking/ui/resources/seen-value.png")); // NOI18N

    private static final MessageFormat issueNewFormat       = getFormat("issueNewFormat");      // NOI18N
    private static final MessageFormat issueObsoleteFormat  = getFormat("issueObsoleteFormat"); // NOI18N
    private static final MessageFormat issueModifiedFormat  = getFormat("issueModifiedFormat"); // NOI18N

    private static final String labelNew = NbBundle.getMessage(QueryTableCellRenderer.class, "LBL_IssueStatusNew");             // NOI18N
    private static final String labelModified = NbBundle.getMessage(QueryTableCellRenderer.class, "LBL_IssueStatusModified");   // NOI18N
    private static final String labelObsolete = NbBundle.getMessage(QueryTableCellRenderer.class, "LBL_IssueStatusObsolete");   // NOI18N

    private static final String msgNew = NbBundle.getMessage(QueryTableCellRenderer.class, "MSG_IssueStatusNew");             // NOI18N
    private static final String msgModified = NbBundle.getMessage(QueryTableCellRenderer.class, "MSG_IssueStatusModified");   // NOI18N
    private static final String msgObsolete = NbBundle.getMessage(QueryTableCellRenderer.class, "MSG_IssueStatusObsolete");   // NOI18N

    private static Color evenLineColor                      = null;
    private static Color unevenLineColor                    = null;
    private static final Color newHighlightColor            = new Color(0x00b400);
    private static final Color modifiedHighlightColor       = new Color(0x0000ff);
    private static final Color obsoleteHighlightColor       = new Color(0x999999);

    static {
        evenLineColor = UIManager.getColor( "nb.bugtracking.table.background" ); //NOI18N
        if( null == evenLineColor )
            evenLineColor = Color.white;

        unevenLineColor = UIManager.getColor( "nb.bugtracking.table.background.alternate" ); //NOI18N
        if( null == unevenLineColor )
            unevenLineColor = new Color(0xf3f6fd);
    }

    public QueryTableCellRenderer(Query query, IssueTable issueTable) {
        this.query = APIAccessor.IMPL.getImpl(query);
        this.issueTable = issueTable;
    }

    private static MessageFormat getFormat(String key) {
        String format = NbBundle.getMessage(IssueTable.class, key);
        return new MessageFormat(format);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        renderer.setIcon(null);
        if(!query.isSaved()) {
            TableCellStyle style = getDefaultCellStyle(table, issueTable, (IssueProperty) value, isSelected, row);
            setStyleProperties(renderer, style);
            return renderer;
        }
        
        TableCellStyle style = null;
        if(value instanceof IssueNode.SeenProperty) {
            IssueNode.SeenProperty ps = (IssueNode.SeenProperty) value;
            renderer.setIcon(!ps.getValue() ? seenValueIcon : null);
            renderer.setText("");                                               // NOI18N
        } 

        if(value instanceof IssueNode.IssueProperty) {
            style = getCellStyle(table, query.getQuery(), issueTable, (IssueProperty)value, isSelected, row);
        }
        setStyleProperties(renderer, style);
        return renderer;
    }

    public void setStyleProperties(JLabel renderer, TableCellStyle style) {
        if (style != null) {
            renderer.putClientProperty(PROPERTY_FORMAT, style.format); // NOI18N
            renderer.putClientProperty(PROPERTY_HIGHLIGHT_PATTERN, style.highlightPattern); // NOI18N
            ((JComponent) renderer).setToolTipText(style.tooltip);
            setRowColors(style, renderer);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {        
        processText(this);
        super.paintComponent(g);
    }

    public static void processText(JLabel label) {
        MessageFormat format = (MessageFormat) label.getClientProperty(PROPERTY_FORMAT);     // NOI18N
        Pattern pattern = (Pattern) label.getClientProperty(PROPERTY_HIGHLIGHT_PATTERN);     // NOI18N
        String s = computeFitText(label);
        if(format != null || pattern != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("<html>");                                                // NOI18N
            s = TextUtils.escapeForHTMLLabel(s);
            if(format != null) {
                format.format(new Object[] {s}, sb, null);
            }
            if(pattern != null) {
                sb.append(highLight(s, pattern));
            }
            sb.append("</html>");                                               // NOI18N
            s = sb.toString();
        } 
        label.setText(s);
    }

    private static String computeFitText(JLabel label) {
        String text = label.getText();
        if(text == null) text = "";
        if (text.length() <= VISIBLE_START_CHARS + 3) return text;
        
        Icon icon = label.getIcon();
        int iconWidth = icon != null ? icon.getIconWidth() : 0;
        
        FontMetrics fm = label.getFontMetrics(label.getFont());
        int width = label.getSize().width - iconWidth;

        String sufix = "...";                                                   // NOI18N
        int sufixLength = fm.stringWidth(sufix);
        int desired = width - sufixLength;
        if (desired <= 0) return text;

        for (int i = 0; i <= text.length() - 1; i++) {
            String prefix = text.substring(0, i);
            int swidth = fm.stringWidth(prefix);
            if (swidth >= desired) {
                return prefix.length() > 0 ? prefix + sufix: text;
            }
        }
        return text;
    }

    private static String highLight(String s, Pattern pattern) {
        Matcher matcher = pattern.matcher(s);
        int idx = 0;
        StringBuilder sb = new StringBuilder();
        while (matcher.find(idx)) {
            int start = matcher.start();
            int end = matcher.end();
            if (start == end) {
                break;
            }
            sb.append(s.substring(idx, start));
            sb.append("<font bgcolor=\"FFB442\" color=\"black\">");
            sb.append(s.substring(start, end));
            sb.append("</font>");
            idx = matcher.end();
        }
        if(sb.length() > 0) {
            sb.append(idx < s.length() ? s.substring(idx, s.length()) : "");
            s = sb.toString();
        }
        return s;
    }

    public static class TableCellStyle {
        private MessageFormat format;
        private Color background;
        private Color foreground;
        private String tooltip;
        private Pattern highlightPattern;

        private TableCellStyle(MessageFormat format, Color background, Color foreground, String tooltip, Pattern highlightPattern) {
            this.background = background;
            this.foreground = foreground;
            this.tooltip = tooltip;
            this.format = format;
            this.highlightPattern = highlightPattern;
        }
        public Color getBackground() {
            return background;
        }
        public Color getForeground() {
            return foreground;
        }
        public MessageFormat getFormat() {
            return format;
        }
        public Pattern getHighlightPattern() {
            return highlightPattern;
        }
        public String getTooltip() {
            return tooltip;
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");                                                     // NOI18N
            sb.append("background=");                                           // NOI18N
            sb.append(background);
            sb.append(", foreground=");                                         // NOI18N
            sb.append(foreground);
            sb.append(", format=");                                             // NOI18N
            sb.append(format != null ? format.toPattern() : null);
            sb.append(", tooltip=");                                            // NOI18N
            sb.append(tooltip);
            sb.append("]");                                                     // NOI18N
            return sb.toString();
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TableCellStyle other = (TableCellStyle) obj;
            if (this.format != other.format && (this.format == null || !this.format.equals(other.format))) {
                return false;
            }
            if (this.background != other.background && (this.background == null || !this.background.equals(other.background))) {
                return false;
            }
            if (this.foreground != other.foreground && (this.foreground == null || !this.foreground.equals(other.foreground))) {
                return false;
            }
            if ((this.tooltip == null) ? (other.tooltip != null) : !this.tooltip.equals(other.tooltip)) {
                return false;
            }
            return true;
        }
        @Override
        public int hashCode() {
            return toString().hashCode();
        }

    }
    
    public static TableCellStyle getCellStyle(JTable table, Query query, IssueTable issueTable, IssueProperty p, boolean isSelected, int row) {
        QueryImpl queryImpl = APIAccessor.IMPL.getImpl(query);
        TableCellStyle style = getDefaultCellStyle(table, issueTable, p, isSelected, row);
        try {
            // set text format and background depending on selection and issue status
            IssueStatusProvider.Status status = null;
            IssueImpl issue = APIAccessor.IMPL.getImpl(p.getIssue());
            if(!queryImpl.contains(issue.getID())) {
                // archived issues
                style.format     = isSelected ? style.format           : issueObsoleteFormat;
                style.background = isSelected ? obsoleteHighlightColor : style.background;
            } else {
                status = issue.getStatus();
                if(status != IssueStatusProvider.Status.SEEN) {
                    switch(status) {
                        case NEW :
                            style.format     = isSelected ? style.format      : issueNewFormat;
                            style.background = isSelected ? newHighlightColor : style.background;
                            break;
                        case MODIFIED :
                            style.format     = isSelected ? style.format           : issueModifiedFormat;
                            style.background = isSelected ? modifiedHighlightColor : style.background;
                            break;
                    }
                }
            }
            
            Object o = p.getValue();
            if(o instanceof String) {
                String s = (String) o;
                if(s == null) s = "";                                               // NOI18N
                s = TextUtils.escapeForHTMLLabel(s);
                StringBuilder sb = new StringBuilder();
                sb.append("<html>");                                                // NOI18N
                sb.append(s);
                if(!queryImpl.contains(issue.getID())) {
                    sb.append("<br>").append(issueObsoleteFormat.format(new Object[] { labelObsolete }, new StringBuffer(), null)); // NOI18N
                    sb.append(msgObsolete);
                } else {
                    if(status == null) {
                        status = issue.getStatus();
                    }
                    switch(status) {
                        case NEW :
                            sb.append("<br>").append(issueNewFormat.format(new Object[] { labelNew }, new StringBuffer(), null)); // NOI18N
                            sb.append(msgNew);
                            break;
                        case MODIFIED :
                            sb.append("<br>").append(issueModifiedFormat.format(new Object[] { labelModified }, new StringBuffer(), null)); // NOI18N
                            sb.append(msgModified);
                            sb.append(p.getRecentChanges());
                            break;
                    }
                }
                sb.append("</html>"); // NOI18N
                style.tooltip = sb.toString();
            }
        } catch (Exception ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
        }
        return style;
    }
    public static TableCellStyle getDefaultCellStyle(JTable table, IssueTable issueTable, IssueProperty p, boolean isSelected, int row) {
        // set default values
        return new TableCellStyle(
            null,                                                                       // format
            isSelected ? table.getSelectionBackground() : getUnselectedBackground(row), // background
            isSelected ? Color.WHITE : table.getForeground(),                           // foreground
            null,                                                                       // tooltip
            getHightlightPattern(issueTable, p)
        );
    }

    private static Pattern getHightlightPattern(IssueTable issueTable, IssueProperty p) {
        if(p instanceof IssueNode.SummaryProperty) {            
            SummaryTextFilter f = issueTable.getSummaryFilter();
            if(f != null && f.isHighLightingOn()) {
                return f.getPattern();
            }
        }
        return null;
    }

    private static Color getUnselectedBackground(int row) {
        return row % 2 != 0 ? unevenLineColor : evenLineColor;
    }

    public static void setRowColors(TableCellStyle style, JComponent l) {
        if(style == null) {
            assert false;
            return; // prefer to do nothing instead of breaking the rendering with an NPE
        }
        if (style.background != null) {
            l.setBackground(style.background);
        }
        if (style.foreground != null) {
            l.setForeground(style.foreground);
        }
    }
}


