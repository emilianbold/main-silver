/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common.status;

import org.glassfish.tools.ide.GlassFishStatus;
import static org.glassfish.tools.ide.GlassFishStatus.OFFLINE;
import static org.glassfish.tools.ide.GlassFishStatus.UNKNOWN;
import org.glassfish.tools.ide.GlassFishStatusListener;
import org.glassfish.tools.ide.data.GlassFishServer;
import org.glassfish.tools.ide.data.GlassFishStatusCheck;
import static org.glassfish.tools.ide.data.GlassFishStatusCheck.LOCATIONS;
import static org.glassfish.tools.ide.data.GlassFishStatusCheck.VERSION;
import org.glassfish.tools.ide.data.GlassFishStatusCheckResult;
import org.glassfish.tools.ide.data.GlassFishStatusTask;

/**
 * Notification about server state check results.
 * <p/>
 * Handles initial period of time after adding new server into status
 * monitoring.
 * At least port checks are being executed periodically so this class will
 * be called back in any situation.
 * <p/>
 * Should receive GlassFishStatus.ONLINE, GlassFishStatus.SHUTDOWN
 * and GlassFishStatus.STARTUP state change events.
 * <p/>
 * @author Tomas Kraus
 */
public class MonitoringInitStateListener implements GlassFishStatusListener {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Requested wake up of checking thread. */
    private volatile boolean wakeUp;

    /** Number of verification checks passed. */
    private short count;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of state check results notification.
     */
    public MonitoringInitStateListener() {
        wakeUp = false;
        count = 0;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Wake up checking thread.
     */
    private void wakeUp() {
        if (!wakeUp) synchronized(this) {
            wakeUp = true;
            this.notify();
        }
    }

    /**
     * Get status of wake up request of checking thread.
     * <p/>
     * @return Status of wake up request of checking thread.
     */
    public boolean isWakeUp() {
        return wakeUp;
    }

    /**
     * Callback to notify about current server status after every check
     * when enabled.
     * <p/>
     * Wait for more checking cycles to make sure server status monitoring
     * has settled down.
     * <p/>
     * @param server GlassFish server instance being monitored.
     * @param status Current server status.
     * @param task   Last GlassFish server status check task details.
     */
    @Override
    public void currentState(final GlassFishServer server,
            final GlassFishStatus status, final GlassFishStatusTask task) {
        count++;
        switch(status) {
            case UNKNOWN:
                // Something should be wrong when state is UNKNOWN after
                // port check.
                if (task != null
                        && task.getType() == GlassFishStatusCheck.PORT) {
                    wakeUp();
                // Otherwise wait for 2 checks.
                } else if (count > 1) {
                    wakeUp();
                }
                break;
            // Wait for 4 internal checks in OFFLINE state.
            case OFFLINE:
                // Command check failure means server is really not online.
                if (task != null) {
                   switch(task.getType()) {
                       case LOCATIONS: case VERSION:
                           if (task.getStatus()
                                   == GlassFishStatusCheckResult.FAILED) {
                               wakeUp();
                               // Skip 2nd wake up.
                               count = 0;
                           }
                   }
                }
                // Otherwise wait for 3 internal checks in OFFLINE state.
                if (count > 2) {
                    wakeUp();
                }
                break;
            // Wake up after 1st check in any other state.
            default:
                wakeUp();
        }
    }

    /**
     * Callback to notify about server status change when enabled.
     * <p/>
     * Listens on <code>ONLINE</code>, <code>SHUTDOWN</code>
     * and <code>STARTUP</code> state changes where we can wake up checking
     * thread immediately.
     * <p/>
     * @param server GlassFish server instance being monitored.
     * @param status Current server status.
     * @param task   Last GlassFish server status check task details.
     */    
    @Override
    public void newState(final GlassFishServer server,
            final GlassFishStatus status, final GlassFishStatusTask task) {
        wakeUp();
    }

    /**
     * Callback to notify about server status check failures.
     * <p/>
     * @param server GlassFish server instance being monitored.
     * @param task   GlassFish server status check task details.
     */
    @Override
    public void error(final GlassFishServer server,
            final GlassFishStatusTask task) {
        // Not used yet.
    }

}
