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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.search;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.CharConversionException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Caret;
import org.netbeans.api.search.SearchHistory;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.modules.search.ui.ReplaceCheckableNode;
import org.netbeans.modules.search.ui.ResultsOutlineSupport;
import org.netbeans.modules.search.ui.UiUtils;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.xml.XMLUtil;

/**
 * Holds details about one search hit in the text document.
 *
 * @author Tomas Pavek
 * @author Marian Petras
 */
public final class TextDetail implements Selectable {

    private static final Logger LOG = Logger.getLogger(
            TextDetail.class.getName());
    /** Property name which indicates this detail to show. */
    public static final int DH_SHOW = 1;
    /** Property name which indicates this detail to go to. */
    public static final int DH_GOTO = 2;
    /** Property name which indicates this detail to hide. */
    public static final int DH_HIDE = 3;
    
    /** Data object. */
    private DataObject dobj;
    /** Line number where search result occures.*/
    private int line;
    /** Text of the line. */ 
    private String lineText;
    /** Column where search result starts. */
    private int column;
    /** Length of search result which to mark. */
    private int markLength;
    /** Line. */
    private Line lineObj;
    /** SearchPattern used to create the hit of this DetailNode */
    private SearchPattern searchPattern;
    /** Start offset of the matched text in the file */
    private int startOffset;
    /** End offset of the matched text in the file */
    private int endOffset;
    /** Whole matched text */
    private String matchedText;
    /** Selected flag */
    private boolean selected = true;
    /** Line number indent */
    private String lineNumberIndent = "";                               //NOI18N
    /** Show the text detail after the data object is updated */
    private boolean showAfterDataObjectUpdated = false;

    private ChangeSupport changeSupport = new ChangeSupport(this);
    /** Constructor using data object. 
     * @param pattern  SearchPattern used to create the hit of this DetailNode 
     */
    public TextDetail(DataObject dobj, SearchPattern pattern) {
        this.dobj = dobj;
        this.searchPattern = pattern;
    }

    /**
     * Shows the search detail on the DataObject.
     * The document is opened in the editor, the caret is positioned on the
     * right line and column and searched string is marked.
     *
     * @param how indicates how to show detail. 
     * @see #DH_GOTO 
     * @see #DH_SHOW 
     * @see #DH_HIDE */
    @NbBundle.Messages({
        "MSG_CannotShowTextDetai=The text match cannot be shown."
    })
    public void showDetail(int how) {
        prepareLine();
        if (lineObj == null) {
            Toolkit.getDefaultToolkit().beep();
            EditCookie ed = dobj.getLookup().lookup(EditCookie.class);
            if (ed != null) {
                ed.edit();
                showAfterDataObjectUpdated = true; // show correct line later
            }
            return;
        }
        if (how == DH_HIDE) {
            return;
        }
        EditorCookie edCookie = dobj.getLookup().lookup(EditorCookie.class);
        if (edCookie != null) {
            edCookie.open();
	}
        if (how == DH_SHOW) {
            lineObj.show(ShowOpenType.NONE, 
                         ShowVisibilityType.NONE,
                         column - 1);
        } else if (how == DH_GOTO) {
            lineObj.show(ShowOpenType.OPEN, 
                         ShowVisibilityType.FOCUS,
                         column - 1);
        }
        if ((markLength > 0) && (edCookie != null)) {
            final JEditorPane[] panes = edCookie.getOpenedPanes();
            if (panes != null && panes.length > 0) {
                // Necessary since above lineObj.show leads to invoke
                // later as well.
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Caret caret = panes[0].getCaret(); // #23626
                            caret.moveDot(caret.getDot() + markLength);
                        } catch (Exception e) { // #217038
                            StatusDisplayer.getDefault().setStatusText(
                                    Bundle.MSG_CannotShowTextDetai());
                            LOG.log(Level.FINE,
                                    Bundle.MSG_CannotShowTextDetai(), e);
                        }
                    }
                });
            }
        }
        SearchHistory.getDefault().add(
                SearchPattern.create(
                searchPattern.getSearchExpression(),
                searchPattern.isWholeWords(), searchPattern.isMatchCase(),
                searchPattern.isRegExp()));
    }

    /** Getter for <code>lineText</code> property. */
    public String getLineText() {
        return lineText;
    }
    
    /** Setter for <code>lineText</code> property. */
    public void setLineText(String text) {
        lineText = text;
    }

    String getLineTextPart(int beginIndex, int endIndex) {
        return lineText.substring(beginIndex, endIndex);
    }

    String getLineTextPart(int beginIndex) {
        return lineText.substring(beginIndex);
    }

    public int getLineTextLength() {
        return lineText == null ? 0 : lineText.length();
    }

    /**
     * Gets the <code>DataObject</code> where the searched text was found. 
     *
     * @return data object or <code>null</code> if no data object is available
     */
    public DataObject getDataObject() {
        return dobj;
    }

    /** Gets the line position of the text. */
    public int getLine() {
        return line;
    }

    /** Sets the line position of the text. */
    public void setLine(int line) {
        this.line = line;
    }

    /** Gets the column position of the text or 0 (1 based). */
    public int getColumn() {
        return column;
    }

    /** Sets the column position of the text. */
    public void setColumn(int col) {
        column = col;
    }

    /** Gets the column position of the text or -1 (0 based). */
    int getColumn0() {
        return column - 1;
    }

    /**
     * Sets the length of the text that should be marked when the detail is
     * shown.
     * @param len the length of the marked text
     */
    public void setMarkLength(int len) {
        markLength = len;
    }

    /** 
     * Gets the length of the text that should be marked when the detail is
     * shown.
     * @return length of the marked text or 0
     */
    public int getMarkLength() {
        return markLength;
    }

    /** Gets the end position of the matched text in the file. */
    public int getEndOffset() {
        return endOffset;
    }

    /** Sets the end position of the matched text in the file. */
    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }

    /** Gets the start position of the matched text in the file. */
    public int getStartOffset() {
        return startOffset;
    }

    /** Sets the start position of the matched text in the file. */
    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    /** Gets the matched text. */
    public String getMatchedText() {
        return matchedText;
    }

    /** Sets the matched text. */
    public void setMatchedText(String matchedText) {
        this.matchedText = matchedText;
    }

    /**
     * Associates a result of the find with underlying text details.
     * @param lineNumber the line position of the text.
     * @param column the column position of the text or 0 (1 based).
     * @param lineText text of the line.
     */
    public void associate(int lineNumber, int column, String lineText) {
         setLine(lineNumber);
         setColumn(column);
         setLineText(lineText);
    }

    private void prepareLine() {
        if (dobj == null || !dobj.isValid()) {
            lineObj = null;
        } else if (lineObj == null) { // try to get Line from DataObject
            LineCookie lineCookie = dobj.getLookup().lookup(LineCookie.class);
            if (lineCookie != null) {
                Line.Set lineSet = lineCookie.getLineSet();
                try {
                    lineObj = lineSet.getOriginal(line - 1);
                } catch (IndexOutOfBoundsException ioobex) {
                    // The line doesn't exist - go to the last line
                    lineObj = lineSet.getOriginal(findMaxLine(lineSet));
                    column = markLength = 0;
                }
            }
        }
    }

    /**
     * Returns the maximum line in the <code>set</code>.
     * Used to display the end of file when the corresponding
     * line no longer exists. (Copied from org.openide.text)
     *
     * @param set the set we want to search.
     * @return maximum line in the <code>set</code>.
     */
    private static int findMaxLine(Line.Set set) {
        int from = 0;
        int to = 32000;
        
        for (;;) {
            try {
                set.getOriginal(to);
                // if the line exists, double the max number, but keep
                // for reference that it exists
                from = to;
                to *= 2;
            } catch (IndexOutOfBoundsException ex) {
                break;
            }
        }
        
        while (from < to) {
            int middle = (from + to + 1) / 2;
            
            try {
                set.getOriginal(middle);
                // line exists
                from = middle;
            } catch (IndexOutOfBoundsException ex) {
                // line does not exists, we have to search lower
                to = middle - 1;
            }
        }
        
        return from;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            fireChange();
        }
    }

    @Override
    public void setSelectedRecursively(boolean selected) {
        setSelected(selected); // always leaf
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public void fireChange() {
        changeSupport.fireChange();
    }

    /**
     * Update data object. Can be called when a module is enabled and new data
     * loader produces new data object. The new data object can provide new
     * features, e.g. LineCookie.
     */
    public void updateDataObject(DataObject dataObject) {
        if (this.dobj.getPrimaryFile().equals(
                dataObject.getPrimaryFile())) {
            this.dobj = dataObject;
            this.lineObj = null;
            if (showAfterDataObjectUpdated) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        showDetail(TextDetail.DH_GOTO);
                    }
                });
                showAfterDataObjectUpdated = false;
            }
        } else {
            throw new IllegalArgumentException(
                    "Expected data object for the same file");          //NOI18N
        }
    }

    /**
     * Node that represents information about one occurence of a matching
     * string.
     *
     * @see  TextDetail
     */
    static final class DetailNode extends AbstractNode
                                          implements OutputListener {
        private static final String ICON =
                "org/netbeans/modules/search/res/textDetail.png";       //NOI18N
        /** Maximal lenght of displayed text detail. */
        static final int DETAIL_DISPLAY_LENGTH = 240;
        private static final String ELLIPSIS = "...";                   //NOI18N
        
        /** Detail to represent. */
        private TextDetail txtDetail;
        /** Cached toString value. */
        private String name;
        private String htmlDisplayName;
        
        /**
         * Constructs a node representing the specified information about
         * a matching string.
         *
         * @param txtDetail  information to be represented by this node
         */
        public DetailNode(TextDetail txtDetail, boolean replacing) {
            super(Children.LEAF, Lookups.fixed(txtDetail,
                    new ReplaceCheckableNode(txtDetail, replacing)));
            
            this.txtDetail = txtDetail;
            
            setShortDescription(DetailNode.getShortDesc(txtDetail));
            setValue(SearchDisplayer.ATTR_OUTPUT_LINE,
                     DetailNode.getFullDesc(txtDetail));
            // A workaround for #124559 - when the detail becomes visible,
            // get the Line object. Later - if the user jumps to the document,
            // changes it and saves - the Line objects are not created for the
            // original set of lines.
            txtDetail.prepareLine();
            txtDetail.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    fireIconChange();
                    ResultsOutlineSupport.toggleParentSelected(DetailNode.this);
                }
            });
            setIconBaseWithExtension(ICON);
        }
        
        /** {@inheritDoc} */
        @Override
        public Action[] getActions(boolean context) {
            if (!context) {
                return new Action[] { getPreferredAction() };
            } else {
                return new Action[0];
            }
        }
        
        /** {@inheritDoc}
         * @return {@link GotoDetailAction}
         */
        @Override
        public Action getPreferredAction() {
            return new GotoDetailAction(this);
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(Object anotherObj) {
            return (anotherObj != null)
                && (anotherObj.getClass() == DetailNode.class)
                && (((DetailNode) anotherObj).txtDetail.equals(this.txtDetail));
        }
        
        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return txtDetail.hashCode() + 1;
        }
        
        /** {@inheritDoc} */
        @Override
        public String getName() {
            if (name == null) {
                name = cutLongLine(txtDetail.getLineText()) +
                    "      [" + DetailNode.getName(txtDetail) + "]";  // NOI18N
            }
            return name;
        }

        /** {@inheritDoc} */
        @Override
        public String getHtmlDisplayName() {
            if (htmlDisplayName != null) {
                return htmlDisplayName;
            }
            try {
                StringBuffer text = new StringBuffer();
                text.append("<html><font color='!controlShadow'>");     //NOI18N
                text.append(txtDetail.lineNumberIndent);
                text.append(txtDetail.getLine());
                text.append(": ");                                      //NOI18N
                text.append("</font>");                                 //NOI18N
                if(canBeMarked()) {
                    appendMarkedText(text);
                }
                else {
                    text.append(escape(cutLongLine(txtDetail.getLineText())));
                }
                text.append("      ");  // NOI18N
                text.append("<font color='!controlShadow'>[");  // NOI18N
                text.append(escape(DetailNode.getLinePos(txtDetail)));
                text.append("]</font></html>");                         //NOI18N
                htmlDisplayName = text.toString();
                return htmlDisplayName;
            } catch (CharConversionException e) {
                return null; // exception in escape(String s)
            }
        }

        private String cutLongLine(String s) {
            if (s == null) {
                return "";                                              //NOI18N
            } else if (s.length() < DETAIL_DISPLAY_LENGTH) {
                return s;
            } else {
                return s.substring(0,
                        DETAIL_DISPLAY_LENGTH - ELLIPSIS.length()) + ELLIPSIS;
            }
        }

        /**
         * Checks whether text can be marked.
         * @return {@code true} if text can be marked, otherwise {@code false}.
         */
        private boolean canBeMarked() {
            int col0 = txtDetail.getColumn0();
            return txtDetail.getMarkLength() > 0 && 
                   col0 > -1 &&
                   col0 < txtDetail.getLineTextLength(); // #177891
        }

        private void appendMarkedText(StringBuffer sb)
                throws CharConversionException {
            final int lineLen = txtDetail.getLineTextLength();
            int matchStart = txtDetail.getColumn0();  // base 0
            int matchEnd = matchStart + Math.min(txtDetail.getMarkLength(),
                    lineLen - matchStart);
            int detailLen = matchEnd - matchStart;
            int prefixStart, suffixEnd;

            if (detailLen > DETAIL_DISPLAY_LENGTH) {
                prefixStart = matchStart;
                suffixEnd = matchEnd;
            } else if (lineLen > DETAIL_DISPLAY_LENGTH) {
                int remaining = DETAIL_DISPLAY_LENGTH - detailLen;
                int quarter = remaining / 4;
                int shownPrefix = Math.min(quarter, matchStart);
                int shownSuffix = Math.min(3 * quarter, lineLen - matchEnd);
                int extraForSuffix = quarter - shownPrefix;
                int extraForPrefix = 3 * quarter - shownSuffix;
                prefixStart = Math.max(0,
                        matchStart - shownPrefix - extraForPrefix);
                suffixEnd = Math.min(lineLen,
                        matchEnd + shownSuffix + extraForSuffix);
            } else { // whole line can be displayed
                prefixStart = 0;
                suffixEnd = lineLen;
            }
            appendMarkedTextPrefix(sb, prefixStart, matchStart);
            appendMarkedTextMatch(sb, matchStart, matchEnd, lineLen, detailLen);
            appendMarkedTextSuffix(sb, matchEnd, suffixEnd, lineLen);
        }

        /**
         * Append part of line that is before matched text.
         *
         * @param text Buffer to append to.
         * @param prefixStart Line index of the first character to be displayed.
         * @param matchStart Line index of the matched text.
         */
        private void appendMarkedTextPrefix(StringBuffer text, int prefixStart,
                int matchStart) throws CharConversionException {
            if (prefixStart > 0) {
                text.append(ELLIPSIS);
            }
            text.append(escape(txtDetail.getLineTextPart(prefixStart,
                    matchStart)));
        }

        /**
         * Append part of line that contains the matched text.
         *
         * @param text Buffer to append to.
         * @param matchStart Line index of the first character of the matched
         * text.
         * @param matchEnd Line index after the last character of the matched
         * text.
         * @param lineLength Lenght of the line.
         * @param detailLength Lengt of matched part.
         */
        private void appendMarkedTextMatch(StringBuffer text, int matchStart,
                int matchEnd, int lineLength, int matchedLength)
                throws CharConversionException {

            text.append("<b>");  // NOI18N
            if (matchedLength > DETAIL_DISPLAY_LENGTH) {
                int off = (DETAIL_DISPLAY_LENGTH - ELLIPSIS.length()) / 2;
                text.append(escape(txtDetail.getLineTextPart(
                        matchStart, matchStart + off)));
                text.append("</b>");
                text.append(ELLIPSIS);
                text.append("<b>");
                text.append(escape(txtDetail.getLineTextPart(
                        matchEnd - off, matchEnd)));
            } else {
                text.append(escape(
                        txtDetail.getLineTextPart(matchStart, matchEnd)));
            }
            int markEnd = matchStart + txtDetail.getMarkLength();
            text.append("</b>"); // NOI18N
            if (markEnd > lineLength) { // mark up to the text end?
                text.append(ELLIPSIS);
            }
        }

        /**
         * Append a part of line that is after the matched text.
         *
         * @param text Buffer to append to.
         * @param matchEnd Line index after the last character of the matched
         * text.
         * @param suffixEnd Line index after the last character of displayed
         * text.
         */
        private void appendMarkedTextSuffix(StringBuffer text, int matchEnd,
                int suffixEnd, int lineLength) throws CharConversionException {

            if (lineLength > matchEnd) {
                text.append(escape(txtDetail.getLineTextPart(matchEnd,
                        suffixEnd)));
                if (suffixEnd < lineLength) {
                    text.append(ELLIPSIS);
                }
            }
        }

        private static String escape(String s) throws CharConversionException {
            return XMLUtil.toElementContent(s);
        }

        /** Displays the matching string in a text editor. */
        void gotoDetail() {
            txtDetail.showDetail(TextDetail.DH_GOTO);
        }

        /** Show the text occurence. */
        private void showDetail() {
            txtDetail.showDetail(TextDetail.DH_SHOW);
        }

        /** {@inheritDoc } 
         * Implements <code>OutputListener</code> interface method.
         */
        @Override
        public void outputLineSelected (OutputEvent evt) {
            txtDetail.showDetail(TextDetail.DH_SHOW);
        }

        /** {@inheritDoc}
         * Implements <code>OutputListener</code> interface method.
         */
        @Override
        public void outputLineAction (OutputEvent evt) {
            txtDetail.showDetail(TextDetail.DH_GOTO);
        }

        /** {@inheritDoc}
         * Implements <code>OutputListener</code> interface method.
         */
        @Override
        public void outputLineCleared (OutputEvent evt) {
            txtDetail.showDetail(TextDetail.DH_HIDE);
        }

        /**
         * Returns name of a node representing a <code>TextDetail</code>.
         *
         * @param  det  detailed information about location of a matching string
         * @return  name for the node
         */
        private static String getName(TextDetail det) {
            int line = det.getLine();
            int col = det.getColumn();
            if (col > 0) {
                /* position <line>:<col> */
                return NbBundle.getMessage(DetailNode.class, 
                                           "TEXT_DETAIL_FMT_NAME1",     //NOI18N
                                           Integer.toString(line),
                                           Integer.toString(col));
            }
            else {
                /* position <line> */
                return NbBundle.getMessage(DetailNode.class,
                                           "TEXT_DETAIL_FMT_NAME2", //NOI18N
                                           Integer.toString(line));
            }
        }

        private static String getLinePos(TextDetail det) {
            int col = det.getColumn();
            if (col > 0) {
                /* column <col> */
                return NbBundle.getMessage(DetailNode.class,
                                           "TEXT_DETAIL_FMT_NAME3",     //NOI18N
                                           col);
            } else {
                return "";                                              //NOI18N
            }
        }

        /**
         * Returns short description of a visual representation of
         * a <code>TextDetail</code>. The description may be used e.g.
         * for a tooltip text of a node.
         *
         * @param  det  detailed information about location of a matching string
         * @return  short description of a visual representation
         */
        private static String getShortDesc(TextDetail det) {
            int line = det.getLine();
            int col = det.getColumn();
            
            if (col > 0) {
                
                /* line <line>, column <col> */
                return NbBundle.getMessage(DetailNode.class,
                                           "TEXT_DETAIL_FMT_SHORT1",   //NOI18N
                                           new Object[] {Integer.toString(line),
                                                         Integer.toString(col)
                                                        });
            } else {
                
                /* line <line> */
                return NbBundle.getMessage(DetailNode.class,
                                           "TEXT_DETAIL_FMT_SHORT2",   //NOI18N
                                           Integer.toString(line));
            }
        }

        /**
         * Returns full description of a visual representation of
         * a <code>TextDetail</code>. The description may be printed e.g. to
         * an OutputWindow.
         *
         * @param  det  detailed information about location of a matching string
         * @return  full description of a visual representation
         */
        private static String getFullDesc(TextDetail det) {
            String filename = det.getDataObject().getPrimaryFile().getNameExt();
            String lineText = det.getLineText();
            int line = det.getLine();
            int col = det.getColumn();

            if (col > 0) {

                /* [<filename> at line <line>, column <col>] <text> */
                return NbBundle.getMessage(DetailNode.class,
                                           "TEXT_DETAIL_FMT_FULL1",    //NOI18N
                                           new Object[] {lineText,
                                                         filename,
                                                         Integer.toString(line),
                                                         Integer.toString(col)
                                                        });
            } else {

                /* [<filename> line <line>] <text> */
                return NbBundle.getMessage(DetailNode.class,
                                           "TEXT_DETAIL_FMT_FULL2",    //NOI18N
                                           new Object[] {lineText,
                                                         filename,
                                                         Integer.toString(line)
                                                        });
            }
        }

        @Override
        protected void createPasteTypes(Transferable t, List<PasteType> s) {
        }
        
    } // End of DetailNode class.

    /**
     * This action displays the matching string in a text editor.
     * This action is to be used in the window/dialog displaying a list of
     * found occurences of strings matching a search pattern.
     */
    private static class GotoDetailAction extends AbstractAction {

        private DetailNode detailNode;

        public GotoDetailAction(DetailNode detailNode) {
            super(UiUtils.getText("LBL_GotoDetailAction"));             //NOI18N
            this.detailNode = detailNode;
        }

        /**  {@inheritDoc}
         * Displays the matching string in a text editor.
         * Works only if condition specified in method {@link #enable} is met,
         * otherwise does nothing.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            detailNode.gotoDetail();
        }
    } // End of GotoDetailAction class.

    void setLineNumberIndent(String lineNumberIndent) {
        this.lineNumberIndent = lineNumberIndent;
    }
}
