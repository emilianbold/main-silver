
package org.netbeans.modules.bugtracking.issuetable;

import java.awt.Color;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import javax.swing.JTable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.bugtracking.issuetable.QueryTableCellRenderer.TableCellStyle;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.issuetable.IssueNode.IssueProperty;
import org.netbeans.modules.bugtracking.spi.*;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author tomas
 */
public class QueryTableCellRendererTest {

    public QueryTableCellRendererTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getCellStyle method, of class QueryTableCellRenderer.
     */
    @Test
    public void testGetCellStyle() {
        JTable table = new JTable();
        RendererQuery query = new RendererQuery();
        RendererIssue issue = new RendererIssue();
        IssueProperty property = new RendererNode(issue, "some value").createProperty();

        MessageFormat issueNewFormat       = getFormat("issueNewFormat");      // NOI18N
        MessageFormat issueObsoleteFormat  = getFormat("issueObsoleteFormat"); // NOI18N
        MessageFormat issueModifiedFormat  = getFormat("issueModifiedFormat"); // NOI18N

        Color newHighlightColor            = new Color(0x00b400);
        Color modifiedHighlightColor       = new Color(0x0000ff);
        Color obsoleteHighlightColor       = new Color(0x999999);

        IssueTable issueTable = new IssueTable(query, new ColumnDescriptor[] {new ColumnDescriptor("dummy", String.class, "dummy", "dummy")});

        // issue seen, not selected
        query.containsIssue = true;
        issue.wasSeen = true;
        issue.recentChanges = "";
        boolean selected = true;
        TableCellStyle defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        TableCellStyle result = QueryTableCellRenderer.getCellStyle(table, query, issueTable, property, selected, 0);
        assertEquals(defaultStyle.getBackground(), result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(null, result.getFormat());
        assertEquals("<html>some value</html>", result.getTooltip());

        // issue seen, selected
        query.containsIssue = true;
        issue.wasSeen = true;
        issue.recentChanges = "";
        selected = true;
        result = QueryTableCellRenderer.getCellStyle(table, query, issueTable, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        assertEquals(defaultStyle.getBackground(), result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(null, result.getFormat());
        assertEquals("<html>some value</html>", result.getTooltip());

        // obsolete issue, not selected
        query.containsIssue = false;
        issue.wasSeen = false;
        issue.recentChanges = "";
        selected = false;
        result = QueryTableCellRenderer.getCellStyle(table, query, issueTable, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        assertEquals(defaultStyle.getBackground(), result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(issueObsoleteFormat, result.getFormat());
        assertEquals("<html>some value<br><font color=\"#999999\"><s>Archived</s></font>- this issue doesn't belong to the query anymore</html>", result.getTooltip());

        // obsolete issue, selected
        query.containsIssue = false;
        selected = true;
        issue.wasSeen = false;
        issue.recentChanges = "";
        result = QueryTableCellRenderer.getCellStyle(table, query, issueTable, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        assertEquals(obsoleteHighlightColor, result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(defaultStyle.getFormat(), result.getFormat());
        assertEquals("<html>some value<br><font color=\"#999999\"><s>Archived</s></font>- this issue doesn't belong to the query anymore</html>", result.getTooltip());

        // modified issue, not selected
        query.containsIssue = true;
        selected = false;
        issue.wasSeen = false;
        issue.recentChanges = "changed";
        query.status = IssueCache.ISSUE_STATUS_MODIFIED;
        result = QueryTableCellRenderer.getCellStyle(table, query, issueTable, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        assertEquals(defaultStyle.getBackground(), result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(issueModifiedFormat, result.getFormat());
        assertEquals("<html>some value<br><font color=\"#0000FF\">Modified</font>- this issue is modified - changed</html>", result.getTooltip());


        // modified issue, selected
        query.containsIssue = true;
        selected = true;
        issue.wasSeen = false;
        issue.recentChanges = "changed";
        query.status = IssueCache.ISSUE_STATUS_MODIFIED;
        result = QueryTableCellRenderer.getCellStyle(table, query, issueTable, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        assertEquals(modifiedHighlightColor, result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(null, result.getFormat());
        assertEquals("<html>some value<br><font color=\"#0000FF\">Modified</font>- this issue is modified - changed</html>", result.getTooltip());

        // new issue, not selected
        query.containsIssue = true;
        selected = false;
        issue.wasSeen = false;
        issue.recentChanges = "";
        query.status = IssueCache.ISSUE_STATUS_NEW;
        result = QueryTableCellRenderer.getCellStyle(table, query, issueTable, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        assertEquals(defaultStyle.getBackground(), result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(issueNewFormat, result.getFormat());
        assertEquals("<html>some value<br><font color=\"#00b400\">New</font>- this issue is new</html>", result.getTooltip());


        // new issue, selected
        query.containsIssue = true;
        selected = true;
        issue.wasSeen = false;
        issue.recentChanges = "";
        query.status = IssueCache.ISSUE_STATUS_NEW;
        result = QueryTableCellRenderer.getCellStyle(table, query, issueTable, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        assertEquals(newHighlightColor, result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(null, result.getFormat());
        assertEquals("<html>some value<br><font color=\"#00b400\">New</font>- this issue is new</html>", result.getTooltip());

    }


    /**
     * Test of getDefaultCellStyle method, of class QueryTableCellRenderer.
     */
    @Test
    public void testGetDefaultCellStyle() {
        JTable table = new JTable();
        RendererQuery query = new RendererQuery();
        RendererIssue issue = new RendererIssue();
        IssueProperty property = new RendererNode(issue, "some value").createProperty();

        IssueTable issueTable = new IssueTable(query, new ColumnDescriptor[] {new ColumnDescriptor("dummy", String.class, "dummy", "dummy")});
        
        TableCellStyle result = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, true, 0);
        assertEquals(table.getSelectionBackground(), result.getBackground()); // keep table selection colors
        assertEquals(Color.WHITE, result.getForeground());
        assertNull(result.getFormat());
        assertNull(result.getTooltip());

        result = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, false, 0);
        assertEquals(table.getForeground(), result.getForeground()); // keep table selection colors
        assertNull(result.getFormat());
        assertNull(result.getTooltip());
        Color unevenBackground = result.getBackground();

        result = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, false, 1);
        assertEquals(table.getForeground(), result.getForeground()); // keep table selection colors
        assertNull(result.getFormat());
        assertNull(result.getTooltip());
        Color evenBackground = result.getBackground();

        assertNotSame(evenBackground, unevenBackground);
        assertTrue(evenBackground.equals(Color.WHITE) || unevenBackground.equals(Color.WHITE));
        assertTrue(evenBackground.equals(new Color(0xf3f6fd)) || unevenBackground.equals(new Color(0xf3f6fd)));
    }

    private static MessageFormat getFormat(String key) {
        String format = NbBundle.getMessage(IssueTable.class, key);
        return new MessageFormat(format);
    }

    private class RendererQuery extends QueryProvider {
        private boolean containsIssue;
        private int status;
        private RendererRepository repository;

        public RendererQuery() {
        }

        @Override
        public boolean isSaved() {            
            return false;
        }

        @Override
        public String getDisplayName() {
            return "Renderer Query";
        }

        @Override
        public String getTooltip() {
            return "Renderer Query";
        }

        @Override
        public BugtrackingController getController() {
            fail("implement me!!!");
            return null;
        }

        @Override
        public RepositoryProvider getRepository() {
            if(repository == null) {
                repository = new RendererRepository();
            }
            return repository;
        }

        @Override
        public IssueProvider[] getIssues(int includeStatus) {
            fail("implement me!!!");
            return null;
        }

        @Override
        public boolean contains(IssueProvider issue) {
            return containsIssue;
        }

        @Override
        public int getIssueStatus(IssueProvider issue) {
            return status;
        }

        @Override
        public void setContext(Node[] nodes) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {}

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {}
    }

    private class RendererNode extends IssueNode {

        Object propertyValue;
        public RendererNode(IssueProvider issue, String value) {
            super(issue);
            propertyValue = value;
        }
        RendererIssueProperty createProperty() {
            return new RendererIssueProperty(null, null, null, null, this);
        }
        @Override
        protected Property<?>[] getProperties() {
            return new Property[0];
        }
        class RendererIssueProperty extends IssueProperty {
            public RendererIssueProperty(String arg0, Class name, String type, String displayName, Object value) {
                super(arg0, name, type, displayName);
            }
            @Override
            public Object getValue() throws IllegalAccessException, InvocationTargetException {
                return propertyValue;
            }
        }
    }

    private class RendererIssue extends IssueProvider {
        boolean wasSeen = false;
        private String recentChanges;
        public RendererIssue() {
            super(new RendererRepository());
            ((RendererRepository)getRepository()).setIssue(this);
        }

        @Override
        public String getDisplayName() {
            return "Renderer Issue";
        }

        @Override
        public String getTooltip() {
            return "Renderer Issue";
        }

        @Override
        public boolean isNew() {
            fail("implement me!!!");
            return false;
        }

        @Override
        public boolean refresh() {
            fail("implement me!!!");
            return false;
        }

        @Override
        public void addComment(String comment, boolean closeAsFixed) {
            fail("implement me!!!");
        }

        @Override
        public void attachPatch(File file, String description) {
            fail("implement me!!!");
        }

        @Override
        public BugtrackingController getController() {
            fail("implement me!!!");
            return null;
        }

        public IssueNode getNode() {
            fail("implement me!!!");
            return null;
        }

        @Override
        public String getID() {
            return "id";
        }

        @Override
        public String getSummary() {
            fail("implement me!!!");
            return null;
        }

        public String getRecentChanges() {
            return recentChanges;
        }

        public Map<String, String> getAttributes() {
            fail("implement me!!!");
            return null;
        }

        @Override
        public void setContext(Node[] nodes) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private class RendererRepository extends RepositoryProvider {
        private RendererIssue issue;
        private RepositoryInfo info;
        public RendererRepository() {
            info = new RepositoryInfo("testrepo", "testconnector", null, null, null, null, null, null, null);
        }

        @Override
        public RepositoryInfo getInfo() {
            return info;
        }
        
        public void setIssue(RendererIssue issue) {
            this.issue = issue;
        }
        @Override
        public Image getIcon() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        @Override
        public IssueProvider getIssue(String id) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public RepositoryController getController() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public QueryProvider createQuery() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public IssueProvider createIssue() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public QueryProvider[] getQueries() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public IssueProvider[] simpleSearch(String criteria) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        public Lookup getLookup() {
            return Lookups.singleton(new IssueCache("renderer", new IssueCache.IssueAccessor() {
                public IssueProvider createIssue(Object issueData) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                public void setIssueData(IssueProvider issue, Object issueData) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                public boolean wasSeen(String id) {
                    return issue.wasSeen;
                }
                public String getRecentChanges(IssueProvider issue) {
                    return ((RendererIssue) issue).getRecentChanges();
                }
                public long getLastModified(IssueProvider issue) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                public long getCreated(IssueProvider issue) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                public String getID(Object issueData) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                public Map getAttributes(IssueProvider issue) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            }) {});
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };

}
