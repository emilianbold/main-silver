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
package org.netbeans.modules.php.project.connections.common;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Utility methods for remote connections.
 */
public final class RemoteUtils {

    private static final Logger LOGGER = Logger.getLogger(RemoteUtils.class.getName());


    private RemoteUtils() {
    }

    /**
     * Display remote exception in a dialog window to inform user about error
     * on a remote server.
     * @param remoteException remote exception to be displayed
     */
    @NbBundle.Messages({
        "LBL_RemoteError=Remote Error",
        "MSG_RemoteErrorReason=\n\nReason: {0}"
    })
    public static void processRemoteException(RemoteException remoteException) {
        String title = Bundle.LBL_RemoteError();
        StringBuilder message = new StringBuilder(remoteException.getMessage());
        String remoteServerAnswer = remoteException.getRemoteServerAnswer();
        Throwable cause = remoteException.getCause();
        if (remoteServerAnswer != null && remoteServerAnswer.length() > 0) {
            message.append(Bundle.MSG_RemoteErrorReason(remoteServerAnswer));
        } else if (cause != null) {
            message.append(Bundle.MSG_RemoteErrorReason(cause.getMessage()));
        }
        NotifyDescriptor notifyDescriptor = new NotifyDescriptor(
                message.toString(),
                title,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.ERROR_MESSAGE,
                new Object[] {NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notifyLater(notifyDescriptor);
    }

    /**
     * Remote trailing {@value TransferFile#REMOTE_PATH_SEPARATOR} from the given
     * directory path.
     * <p>
     * If the path is <i>root</i> (it equals just {@value TransferFile#REMOTE_PATH_SEPARATOR}),
     * no sanitation is done.
     * @param directoryPath directory to be sanitized
     * @return sanitized directory path
     */
    public static String sanitizeDirectoryPath(String directoryPath) {
        while (!directoryPath.equals(TransferFile.REMOTE_PATH_SEPARATOR)
                && directoryPath.endsWith(TransferFile.REMOTE_PATH_SEPARATOR)) {
            LOGGER.log(Level.FINE, "Removing ending slash from directory {0}", directoryPath);
            directoryPath = directoryPath.substring(0, directoryPath.length() - TransferFile.REMOTE_PATH_SEPARATOR.length());
        }
        return directoryPath;
    }

    /**
     * Sanitize upload directory, see issue #169793 for more information.
     * @param uploadDirectory upload directory to sanitize
     * @param allowEmpty <code>true</code> if the string can be empty
     * @return sanitized upload directory
     */
    public static String sanitizeUploadDirectory(String uploadDirectory, boolean allowEmpty) {
        if (StringUtils.hasText(uploadDirectory)) {
            while (uploadDirectory.length() > 1
                    && uploadDirectory.endsWith(TransferFile.REMOTE_PATH_SEPARATOR)) {
                uploadDirectory = uploadDirectory.substring(0, uploadDirectory.length() - 1);
            }
        } else if (!allowEmpty) {
            uploadDirectory = TransferFile.REMOTE_PATH_SEPARATOR;
        }
        if (allowEmpty
                && (uploadDirectory == null || TransferFile.REMOTE_PATH_SEPARATOR.equals(uploadDirectory))) {
            uploadDirectory = ""; // NOI18N
        }
        return uploadDirectory;
    }

}
