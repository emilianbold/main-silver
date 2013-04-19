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
package org.netbeans.modules.tasks.ui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.beans.PropertyChangeEvent;
import java.io.CharConversionException;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Issue.Status;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.Util;
import org.netbeans.modules.tasks.ui.DashboardTopComponent;
import org.netbeans.modules.tasks.ui.QuickSearchPanel;
import org.netbeans.modules.tasks.ui.dashboard.CategoryNode;
import org.netbeans.modules.tasks.ui.dashboard.DashboardViewer;
import org.netbeans.modules.tasks.ui.dashboard.RepositoryNode;
import org.netbeans.modules.tasks.ui.dashboard.TaskNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.FindAction;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;
import org.openide.xml.XMLUtil;

/**
 *
 * @author jpeska
 */
public class Utils {

    private final static int VISIBLE_START_CHARS = 5;
    private final static String BOLD_START_SUBSTITUTE = "$$$BOLD_START$$$"; //NOI18
    private final static String BOLD_END_SUBSTITUTE = "$$$BOLD_END$$$"; //NOI18

    public static String getCategoryDisplayText(CategoryNode categoryNode) {
        String categoryName = categoryNode.getCategory().getName();
        boolean containsActiveTask = DashboardViewer.getInstance().containsActiveTask(categoryNode);
        return getTopLvlDisplayText(containsActiveTask, categoryName, categoryNode.isOpened());
    }

    public static String getRepositoryDisplayText(RepositoryNode repositoryNode) {
        String repositoryName = repositoryNode.getRepository().getDisplayName();
        boolean containsActiveTask = DashboardViewer.getInstance().containsActiveTask(repositoryNode);
        return getTopLvlDisplayText(containsActiveTask, repositoryName, repositoryNode.isOpened());
    }

    private static String getTopLvlDisplayText(boolean containsActiveTask, String name, boolean isOpened) {
        String displayName;
        try {
            name = XMLUtil.toElementContent(name);
        } catch (CharConversionException ex) {
        }
        String activeText = containsActiveTask ? "<b>" + name + "</b>" : name; //NOI18N
        if (!isOpened) {
            displayName = "<html><strike>" + activeText + "</strike><html>"; //NOI18N
        } else {
            displayName = "<html>" + activeText + "<html>";
        }
        return displayName;
    }

    public static String getTaskPlainDisplayText(Issue task, JComponent component, int maxWidth) {
        return computeFitText(component, maxWidth, task.getID() + " - " + task.getSummary(), false);
    }

    public static String getTaskDisplayString(Issue task, JComponent component, int maxWidth, boolean active, boolean hasFocus) {
        String displayName;
        String fitText = computeFitText(component, maxWidth, task.getID() + " - " + task.getSummary(), active); //NOI18N

        String activeText = active ? BOLD_START_SUBSTITUTE + fitText + BOLD_END_SUBSTITUTE : getFilterBoldText(fitText); //NOI18N

        try {
            activeText = XMLUtil.toElementContent(activeText);
        } catch (CharConversionException ex) {
        }
        activeText = replaceSubstitutes(activeText);
        if (task.isFinished()) {
            activeText = "<strike>" + activeText + "</strike>"; //NOI18N
        }
        Status status = task.getStatus();
        if (status == Status.NEW && !hasFocus) {
            displayName = "<html><font color=\"green\">" + activeText + "</font></html>"; //NOI18N
        } else if (status == Status.MODIFIED && !hasFocus) {
            displayName = "<html><font color=\"blue\">" + activeText + "</font></html>"; //NOI18N
        } else {
            displayName = "<html>" + activeText + "</html>"; //NOI18N
        }
        return displayName;
    }

    public static String computeFitText(JComponent component, int maxWidth, String text, boolean bold) {
        if (text == null) {
            text = ""; // NOI18N
        }
        if (text.length() <= VISIBLE_START_CHARS + 3) {
            return text;
        }
        FontMetrics fm;
        if (bold) {
            fm = component.getFontMetrics(component.getFont().deriveFont(Font.BOLD));
        } else {
            fm = component.getFontMetrics(component.getFont());
        }
        int width = maxWidth;

        String sufix = "..."; // NOI18N
        int sufixLength = fm.stringWidth(sufix + " "); //NOI18N
        int desired = width - sufixLength;
        if (desired <= 0) {
            return text;
        }

        for (int i = 0; i <= text.length() - 1; i++) {
            String prefix = text.substring(0, i);
            int swidth = fm.stringWidth(prefix);
            if (swidth >= desired) {
                if (fm.stringWidth(text.substring(i + 1)) <= fm.stringWidth(sufix)) {
                    return text;
                }
                return prefix.length() > 0 ? prefix + sufix : text;
            }
        }
        return text;
    }

    private static String getFilterBoldText(String fitText) {
        String filterText = DashboardTopComponent.findInstance().getFilterText();
        if (!filterText.equals("")) { //NOI18N
            int searchIndex = 0;
            StringBuilder sb = new StringBuilder(fitText);

            int index = sb.toString().toLowerCase().indexOf(filterText.toLowerCase(), searchIndex);
            while (index != -1) {
                sb.insert(index, BOLD_START_SUBSTITUTE);
                index = index + BOLD_START_SUBSTITUTE.length() + filterText.length();
                sb.insert(index, BOLD_END_SUBSTITUTE);
                searchIndex = index + BOLD_END_SUBSTITUTE.length();
                index = sb.toString().toLowerCase().indexOf(filterText.toLowerCase(), searchIndex);
            }
            return sb.toString();
        } else {
            return fitText;
        }
    }

    public static String getFindActionMapKey() {
        return SharedClassObject.findObject(FindAction.class, true).getActionMapKey().toString();
    }

    private static String replaceSubstitutes(String text) {
        text = text.replace(BOLD_START_SUBSTITUTE, "<b>"); //NOI18N
        return text.replace(BOLD_END_SUBSTITUTE, "</b>"); //NOI18N
    }

    public static void quickSearchTask(Repository repository) {
        JButton open = new JButton(NbBundle.getMessage(DashboardTopComponent.class, "OPTION_Open"));
        open.setEnabled(false);
        JButton cancel = new JButton(NbBundle.getMessage(DashboardTopComponent.class, "OPTION_Cancel"));

        QuickSearchPanel quickSearchPanel = new QuickSearchPanel(repository);
        NotifyDescriptor quickSearchDialog = new NotifyDescriptor(
                quickSearchPanel,
                NbBundle.getMessage(DashboardTopComponent.class, "LBL_QuickTitle", repository.getDisplayName()), //NOI18N
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE,
                new Object[]{open, cancel},
                open);
        quickSearchDialog.setValid(false);
        QuickSearchListener quickSearchListener = new QuickSearchListener(quickSearchPanel, open);
        quickSearchPanel.addQuickSearchListener(quickSearchListener);
        Object result = DialogDisplayer.getDefault().notify(quickSearchDialog);
        if (result == open) {
            Issue task = quickSearchPanel.getSelectedTask();
            Util.openIssue(task.getRepository(), task.getID());
            Category selectedCategory = quickSearchPanel.getSelectedCategory();
            if (selectedCategory != null) {
                DashboardViewer.getInstance().addTaskToCategory(selectedCategory, new TaskNode(task, null));
            }
        }
        quickSearchPanel.removeQuickSearchListener(quickSearchListener);
    }

    private static class QuickSearchListener implements ChangeListener {

        private QuickSearchPanel quickSearchPanel;
        private JButton open;

        public QuickSearchListener(QuickSearchPanel quickSearchPanel, JButton open) {
            this.quickSearchPanel = quickSearchPanel;
            this.open = open;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            Issue selectedTask = quickSearchPanel.getSelectedTask();
            open.setEnabled(selectedTask != null);
        }
    }
}
