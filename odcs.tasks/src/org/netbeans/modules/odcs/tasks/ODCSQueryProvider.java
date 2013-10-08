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
package org.netbeans.modules.odcs.tasks;

import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.netbeans.modules.bugtracking.team.spi.TeamQueryProvider;
import org.netbeans.modules.bugtracking.team.spi.OwnerInfo;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.odcs.tasks.issue.ODCSIssue;
import org.netbeans.modules.odcs.tasks.query.ODCSQuery;
import org.netbeans.modules.odcs.tasks.repository.ODCSRepository;

/**
 *
 * @author Tomas Stupka
 */
public class ODCSQueryProvider implements TeamQueryProvider<ODCSQuery, ODCSIssue> {

    @Override
    public String getDisplayName(ODCSQuery q) {
        return q.getDisplayName();
    }

    @Override
    public String getTooltip(ODCSQuery q) {
        return q.getTooltip();
    }

    @Override
    public QueryController getController(ODCSQuery q) {
        return q.getController();
    }

    @Override
    public boolean isSaved(ODCSQuery q) {
        return q.isSaved();
    }

    @Override
    public boolean canRemove(ODCSQuery q) {
        return q.canRemove();
    }
    
    @Override
    public void remove(ODCSQuery q) {
        q.remove();
    }

    @Override
    public Collection<ODCSIssue> getIssues(ODCSQuery q) {
        return q.getIssues();
    }

    @Override
    public boolean contains(ODCSQuery q, String id) {
        return q.contains(id);
    }

    @Override
    public void refresh(ODCSQuery q) {
        q.getController().refresh(true);
    }

    @Override
    public void removePropertyChangeListener(ODCSQuery q, PropertyChangeListener listener) {
        q.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(ODCSQuery q, PropertyChangeListener listener) {
        q.addPropertyChangeListener(listener);
    }

    /************************************************************************************
     * Team
     ************************************************************************************/
    
    @Override
    public boolean needsLogin (ODCSQuery q) {
        ODCSRepository repository = q.getRepository();
        return q != (repository).getPredefinedQuery(PredefinedTaskQuery.ALL)
            && q != (repository).getPredefinedQuery(PredefinedTaskQuery.RECENT);
    }

    @Override
    public void setOwnerInfo (ODCSQuery q, OwnerInfo info) {
        q.setOwnerInfo(info);
    }

    @Override
    public boolean canRename(ODCSQuery q) {
        return true;
    }

    @Override
    public void rename(ODCSQuery q, String displayName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
