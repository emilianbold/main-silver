/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla.repository;

import org.eclipse.mylyn.internal.bugzilla.core.BugzillaAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.openide.util.NbBundle;

/**
 * Information about an issue field.
 *
 * @author Jan Stola, Tomas Stupka
 */
public class IssueField {
    public static final IssueField SUMMARY = new IssueField(BugzillaAttribute.SHORT_DESC.getKey(), "LBL_SUMMARY");
    public static final IssueField WHITEBOARD = new IssueField(BugzillaAttribute.STATUS_WHITEBOARD.getKey(), "LBL_WHITEBOARD");
    public static final IssueField STATUS = new IssueField(TaskAttribute.STATUS, "LBL_STATUS");
    public static final IssueField PRIORITY = new IssueField(BugzillaAttribute.PRIORITY.getKey(), "LBL_PRIORITY");
    public static final IssueField RESOLUTION = new IssueField(TaskAttribute.RESOLUTION, "LBL_RESOLUTION");
    public static final IssueField PRODUCT = new IssueField(BugzillaAttribute.PRODUCT.getKey(), "LBL_PRODUCT");
    public static final IssueField COMPONENT = new IssueField(BugzillaAttribute.COMPONENT.getKey(), "LBL_COMPONENT");
    public static final IssueField VERSION = new IssueField(BugzillaAttribute.VERSION.getKey(), "LBL_VERSION");
    public static final IssueField PLATFORM = new IssueField(BugzillaAttribute.REP_PLATFORM.getKey(), "LBL_PLATFORM");
    public static final IssueField OS = new IssueField(BugzillaAttribute.OP_SYS.getKey(), "LBL_OS");
    public static final IssueField MILESTONE = new IssueField(BugzillaAttribute.TARGET_MILESTONE.getKey(), "LBL_MILESTONE");
    public static final IssueField REPORTER = new IssueField(BugzillaAttribute.REPORTER.getKey(), "LBL_REPORTER");
    public static final IssueField REPORTER_NAME = new IssueField(BugzillaAttribute.REPORTER_NAME.getKey(), "LBL_REPORTER_NAME");
    public static final IssueField ASSIGNED_TO = new IssueField(BugzillaAttribute.ASSIGNED_TO.getKey(), "LBL_ASSIGNED_TO");
    public static final IssueField ASSIGNED_TO_NAME = new IssueField(BugzillaAttribute.ASSIGNED_TO_NAME.getKey(), "LBL_ASSIGNED_TO_NAME");
    public static final IssueField QA_CONTACT = new IssueField(BugzillaAttribute.QA_CONTACT.getKey(), "LBL_QA_CONTACT");
    public static final IssueField QA_CONTACT_NAME = new IssueField(BugzillaAttribute.QA_CONTACT_NAME.getKey(), "LBL_QA_CONTACT_NAME");
    public static final IssueField DEPENDS_ON = new IssueField(BugzillaAttribute.DEPENDSON.getKey(), "LBL_DEPENDS_ON");
    public static final IssueField BLOCKS = new IssueField(BugzillaAttribute.BLOCKED.getKey(), "LBL_BLOCKS");
    public static final IssueField URL = new IssueField(BugzillaAttribute.BUG_FILE_LOC.getKey(), "LBL_URL");
    public static final IssueField KEYWORDS = new IssueField(BugzillaAttribute.KEYWORDS.getKey(), "LBL_KEYWORDS");
    public static final IssueField SEVERITY = new IssueField(BugzillaAttribute.BUG_SEVERITY.getKey(), "LBL_SEVERITY");
    public static final IssueField ISSUE_TYPE = new IssueField("cf_bug_type", "LBL_ISSUE_TYPE");
    public static final IssueField DESCRIPTION = new IssueField(BugzillaAttribute.LONG_DESC.getKey(), "LBL_DESCRIPTION");
    public static final IssueField CREATION = new IssueField(TaskAttribute.DATE_CREATION, "LBL_CREATION");
    public static final IssueField CC = new IssueField(BugzillaAttribute.CC.getKey(), "LBL_CC");
    public static final IssueField MODIFICATION = new IssueField(TaskAttribute.DATE_MODIFICATION, null);
    public static final IssueField NEWCC = new IssueField(BugzillaAttribute.NEWCC.getKey(), null);
    public static final IssueField REMOVECC = new IssueField(BugzillaAttribute.REMOVECC.getKey(), null);
    public static final IssueField COMMENT_COUNT = new IssueField(TaskAttribute.TYPE_COMMENT, null, false);
    public static final IssueField ATTACHEMENT_COUNT = new IssueField(TaskAttribute.TYPE_ATTACHMENT, null, false);

    private final String key;
    private final String displayNameKey;
    private boolean singleAttribute;

    IssueField(String key, String displayNameKey) {
        this(key, displayNameKey, true);
    }

    IssueField(String key, String displayNameKey, boolean singleAttribute) {
        this.key = key;
        this.singleAttribute = singleAttribute;
        this.displayNameKey = displayNameKey;
    }

    public String getKey() {
        return key;
    }

    public boolean isSingleAttribute() {
        return singleAttribute;
    }

    public boolean isReadOnly() {
        return !singleAttribute;
    }

    public String getDisplayName() {
        assert displayNameKey != null; // shouldn't be called for a field with a null display name
        return NbBundle.getMessage(IssueField.class, displayNameKey);
    }

}
