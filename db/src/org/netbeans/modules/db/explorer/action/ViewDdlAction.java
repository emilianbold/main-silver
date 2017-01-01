/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2012 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.db.explorer.action;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;
import org.netbeans.lib.ddl.impl.CreateTable;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.impl.TableColumn;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseConnector;
import org.netbeans.modules.db.explorer.node.TableNode;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Christian Lenz
 */
@ActionID(id = "org.netbeans.modules.db.explorer.action.ViewDdlAction", category = "Database")
@ActionRegistration(displayName = "#ViewDDL", lazy = false)
@ActionReferences(value = {
    @ActionReference(path = "Databases/Explorer/Table/Actions", position = 450)
    ,
    @ActionReference(path = "Databases/Explorer/TableList/Actions", position = 450)
})
public class ViewDdlAction extends BaseAction {

    @Override
    public String getName() {
        return NbBundle.getMessage(ViewDdlAction.class, "ViewDDL"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.db.explorer.action.ViewDdlAction");
    }

    @Override
    public void performAction(final Node[] activatedNodes) {
        for (Node activatedNode : activatedNodes) {
            final TableNode node = activatedNode.getLookup().lookup(TableNode.class);

            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        getTableStructure(node);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void getTableStructure(final TableNode node) throws MetadataModelException {
        final DatabaseConnection dbConn = node.getLookup().lookup(DatabaseConnection.class);
        final DatabaseConnector connector = dbConn.getConnector();
        final MetadataModel model = dbConn.getMetadataModel();

        model.runReadAction(new Action<Metadata>() {
            @Override
            public void run(Metadata metaData) {
                Specification spec = connector.getDatabaseSpecification();
                String tablename = node.getName();

                Table table = node.getTableHandle().resolve(metaData);

                try {
                    CreateTable createCommandCreateTable = spec.createCommandCreateTable(tablename);
                    Collection<Column> columns = table.getColumns();
                    List<TableColumn> pks = new LinkedList<>();

                    for (Column column : columns) {
                        TableColumn col = connector.getColumnSpecification(table, column);
                        createCommandCreateTable.getColumns().add(col);

                        if (col.getObjectType().equals(TableColumn.PRIMARY_KEY)) {
                            pks.add(col);
                        }
                    }
//                            if (pks.size() > 1) {
//                                setPrimaryKeyColumns(pks, connector, createCommandCreateTable, table);
//                            }

                    JOptionPane.showMessageDialog(null, createCommandCreateTable.getCommand());

//                            JDialog dialog = new JDialog();
//                            dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
//                dialog.setText("CREATE " + tablename + ";\n"); // NOI18N
//                            dialog.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }
}