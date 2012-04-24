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

import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCacheUtils;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import org.openide.nodes.*;
import org.openide.util.lookup.Lookups;
import javax.swing.*;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.openide.util.NbBundle;

/**
 * The node that is rendered in the IssuesTable. It gets values to display from an
 * Issue which serves as the 'data' 'visual' node.
 * 
 * @author Tomas Stupka
 */
public abstract class IssueNode<I> extends AbstractNode {

    /**
     * Seen property id
     */
    public static final String LABEL_NAME_SEEN = "issue.seen";                        // NOI18N
    /**
     * Recetn Changes property id
     */
    public static final String LABEL_RECENT_CHANGES = "issue.recent_changes";         // NOI18N

    public static final String LABEL_NAME_SUMMARY          = "issue.summary";     // NOI18N

    private IssueImpl issue;
    private I issueData;

    private String htmlDisplayName;
    private Action preferedAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            issue.open(true);
        }
    };

    /**
     * Creates a {@link IssueNode}
     * @param issue - the {@link Issue} to be represented by this IssueNode
     */
    public IssueNode(Repository repository, I issueData) {
        this(Children.LEAF, APIAccessor.IMPL.getImpl(repository).getIssue(issueData), issueData);
    }

    private IssueNode(Children children, IssueImpl issue, I issueData) {
        super(children, Lookups.fixed(issue));
        this.issue = issue;
        this.issueData = issueData;
        initProperties();
        refreshHtmlDisplayName();
        IssueCacheUtils.addCacheListener(issue, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(IssueNode.this.issue.getIssue() != evt.getSource()) {
                    return;
                }
                if(evt.getPropertyName().equals(IssueCache.EVENT_ISSUE_SEEN_CHANGED)) {
                    fireSeenValueChanged((Boolean)evt.getOldValue(), (Boolean)evt.getNewValue());
                }
            }
        });
    }
    
    public IssueImpl getIssue() {
        return issue;
    }
    
    public I getIssueData() {
        return issueData;
    }
    
    /**
     * Returns the properties to be shown in the Issue Table according to the ColumnDescriptors returned by
     * {@link Query#getColumnDescriptors() }
     *
     * @return properites
     */
    protected abstract Node.Property<?>[] getProperties();

    @Override
    public Action getPreferredAction() {
        return preferedAction;
    }

    public boolean wasSeen() {
        return IssueCacheUtils.wasSeen(issue);
    }

    private void initProperties() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();

        Node.Property<?>[] properties = getProperties();
        for (Property<?> property : properties) {
            ps.put(property);
        }
        ps.put(new RecentChangesProperty());
        ps.put(new SeenProperty());
        sheet.put(ps);
        setSheet(sheet);    
    }

    private void refreshHtmlDisplayName() {
        htmlDisplayName = issue.getDisplayName();
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    void fireSeenValueChanged(final boolean oldValue, final boolean newValue) {
        if(oldValue != newValue) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    firePropertyChange(LABEL_NAME_SEEN, oldValue, newValue);
                    Property[] properties = getProperties();
                    for (Property p : properties) {
                        if(p instanceof IssueNode.IssueProperty) {
                            String pName = ((IssueProperty)p).getName();
                            if(!pName.equals(LABEL_NAME_SEEN)) {
                                firePropertyChange(pName, null, null);
                            }
                        }
                    }
                }
            });
        }
    }

    protected void fireDataChanged() {
        // table sortes isn't thread safe
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Property[] properties = getProperties();
                for (Property p : properties) {
                    if(p instanceof IssueNode.IssueProperty) {
                        String pName = ((IssueProperty)p).getName();
                        firePropertyChange(pName, null, null);
                    }
                }
            }
        });
    }

    /**
     * An IssueNode Property
     */
    public abstract class IssueProperty<T> extends org.openide.nodes.PropertySupport.ReadOnly implements Comparable<IssueNode<I>.IssueProperty<T>> {
        protected IssueProperty(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }
        @Override
        public String toString() {
            try {
                return getValue().toString();
            } catch (Exception e) {
                BugtrackingManager.LOG.log(Level.INFO, null, e);
                return e.getLocalizedMessage();
            }
        }
        public I getIssueData() {
            return IssueNode.this.issueData;
        }
        public Issue getIssue() {
            return IssueNode.this.issue.getIssue();
        }
        @Override
        public int compareTo(IssueNode<I>.IssueProperty<T> o) {
            return toString().compareTo(o.toString());
        }
    }

    // XXX the same for id
    // XXX CTL_Issue_Summary_Title also defined in bugzilla nad jira!!!
    public class SummaryProperty extends IssueProperty<String> {
        public SummaryProperty() {
            super(LABEL_NAME_SUMMARY,
                  String.class,
                  NbBundle.getMessage(IssueNode.class, "CTL_Issue_Summary_Title"), // NOI18N
                  NbBundle.getMessage(IssueNode.class, "CTL_Issue_Summary_Desc")); // NOI18N
        }
        @Override
        public String getValue() {
            return getIssue().getSummary();
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            String s1 = getIssue().getSummary();
            String s2 = p.getIssue().getSummary();
            return s1.compareTo(s2);
        }
    }

    /**
     * Represens the Seen value in a IssueNode
     */
    public class SeenProperty extends IssueProperty<Boolean> {
        public SeenProperty() {
            super(LABEL_NAME_SEEN,
                  Boolean.class,
                  "", // NOI18N
                  NbBundle.getMessage(IssueNode.class, "CTL_Issue_Seen_Desc")); // NOI18N
        }
        @Override
        public Boolean getValue() {
            return IssueCacheUtils.wasSeen(issue);
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            Boolean b1 = IssueNode.this.wasSeen();
            Boolean b2 = IssueCacheUtils.wasSeen(APIAccessor.IMPL.getImpl(p.getIssue()));
            return b1.compareTo(b2);
        }

    }

    /**
     * Represens the Seen value in a IssueNode
     */
    public class RecentChangesProperty extends IssueNode<I>.IssueProperty<String> {
        public RecentChangesProperty() {
            super(LABEL_RECENT_CHANGES,
                  String.class,
                  NbBundle.getMessage(IssueNode.class, "CTL_Issue_Recent"), // NOI18N
                  NbBundle.getMessage(IssueNode.class, "CTL_Issue_Recent_Desc")); // NOI18N
        }
        @Override
        public String getValue() {
            return IssueCacheUtils.getRecentChanges(APIAccessor.IMPL.getImpl(getIssue()));
        }
        @Override
        public int compareTo(IssueNode<I>.IssueProperty<String> p) {
            if(p == null) return 1;
            if(p.getClass().isAssignableFrom(RecentChangesProperty.class)) {
                IssueImpl issueImpl = APIAccessor.IMPL.getImpl(getIssue());
                String recentChanges1 = IssueCacheUtils.getRecentChanges(issueImpl);
                issueImpl = APIAccessor.IMPL.getImpl(((RecentChangesProperty)p).getIssue());
                String recentChanges2 = IssueCacheUtils.getRecentChanges(issueImpl);
                return recentChanges1.compareToIgnoreCase(recentChanges2);
            }
            return 1;
        }
    }

}
