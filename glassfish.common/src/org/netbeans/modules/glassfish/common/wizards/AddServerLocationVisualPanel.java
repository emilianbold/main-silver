/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.glassfish.common.wizards;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.glassfish.common.ServerDetails;
import org.netbeans.modules.glassfish.spi.Utils;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * @author pblaha
 * @author Peter Williams
 */
public class AddServerLocationVisualPanel extends javax.swing.JPanel implements Retriever.Updater {

    public static enum DownloadState { AVAILABLE, DOWNLOADING, COMPLETED };

    // expose for qa-functional tests
    public static final String V3_DOWNLOAD_PREFIX = "http://download.java.net/"; // NOI18N
    
    private final List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();
    private Retriever retriever;
    private volatile DownloadState downloadState;
    private volatile String statusText;
    private ServerWizardIterator wizardIterator;

    public AddServerLocationVisualPanel(ServerWizardIterator swi) {
        this.wizardIterator = swi;
        initComponents();
        initUserComponents();
    }

    private void initUserComponents() {
        downloadButton.setEnabled(false);
        
        setName(NbBundle.getMessage(AddServerLocationVisualPanel.class, "TITLE_ServerLocation"));
        
        hk2HomeTextField.setText(getPreviousValue());            
        hk2HomeTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                homeFolderChanged();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                homeFolderChanged();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                homeFolderChanged();
            }                    
        });
        setDownloadState(DownloadState.AVAILABLE);
        updateMessageText("");
    }
    
    private String getPreviousValue() {
        Preferences prefs = NbPreferences.forModule(wizardIterator.getClass());
        String prevValue = null;
        if (null != prefs) {
            prevValue = prefs.get("INSTALL_ROOT_KEY", null); // NOI18N
        }
        if (null == prevValue) {
            String installDir = System.getProperty("INSTALL_ROOT_PROPERTY"); // NOI18N
            if (null != installDir && !(installDir.trim().length() == 0)) {
                 return installDir;
            } else {
                return System.getProperty("user.home") + File.separatorChar + "GlassFish_Server"; // NOI18N
            }
        } else {
            return prevValue;            
        }        
    }
    
    public DownloadState getDownloadState() {
        return downloadState;
    }

    /**
     * 
     * @return 
     */
    public String getHk2HomeLocation() {
        return hk2HomeTextField.getText();
    }
    
    /**
     * 
     * @return
     */
    public String getStatusText() {
        return statusText;
    }
    
    /**
     * 
     * @param l 
     */
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    /**
     * 
     * @param l 
     */
    public void removeChangeListener(ChangeListener l ) {
        listeners.remove(l);
    }

    private void fireChangeEvent() {
        ChangeEvent ev = new ChangeEvent(this);
        for(ChangeListener listener: listeners) {
            listener.stateChanged(ev);
        }
    }
    
    private String browseHomeLocation() {
        String hk2Location = null;
        JFileChooser chooser = getJFileChooser();
        int returnValue = chooser.showDialog(this, NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooseButton")); //NOI18N
        if(returnValue == JFileChooser.APPROVE_OPTION) {
            hk2Location = chooser.getSelectedFile().getAbsolutePath();
        }
        return hk2Location;
    }
    
    private JFileChooser getJFileChooser() {
        JFileChooser chooser = new JFileChooser();
        String t = NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooserName");
        chooser.setDialogTitle(t); //NOI18N
        chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setApproveButtonMnemonic("Choose_Button_Mnemonic".charAt(0)); //NOI18N
        chooser.setMultiSelectionEnabled(false);
        chooser.addChoosableFileFilter(new DirFilter());
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setApproveButtonToolTipText(t); //NOI18N
        chooser.getAccessibleContext().setAccessibleName(t); //NOI18N
        chooser.getAccessibleContext().setAccessibleDescription(t); //NOI18N

        // set the current directory
        File currentLocation = new File(hk2HomeTextField.getText());
        File currentLocationParent = currentLocation.getParentFile();
        if(currentLocationParent != null && currentLocationParent.exists()) {
            chooser.setCurrentDirectory(currentLocationParent);
        }
        if (currentLocation.exists() && currentLocation.isDirectory()) {
            chooser.setSelectedFile(currentLocation);
        } 
        
        return chooser;
    }   
    
    @Override
    public void removeNotify() {
        // !PW Is there a better place for this?  If the retriever is still running
        // the user must have hit cancel on the wizard, so tell the retriever thread
        // to shut down and clean up.
        if(retriever != null) {
            retriever.stopRetrieval();
        }
        super.removeNotify();
    }
    
    // ------------------------------------------------------------------------
    // Updater implementation
    // ------------------------------------------------------------------------
    @Override
    public void updateMessageText(final String msg) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                if (msg.trim().startsWith("<html>")) {
                    downloadStatusLabel.setText(msg);
                } else {
                    downloadStatusLabel.setText("<html>"+msg+"</html>");
                }
                fireChangeEvent();
            }
        });
    }
    
    @Override
    public void updateStatusText(final String status) {
        statusText = status;
        fireChangeEvent();
    }

    @Override
    public void clearCancelState() {
        setDownloadState(retriever.getDownloadState() == Retriever.STATUS_COMPLETE ? 
            DownloadState.COMPLETED : DownloadState.AVAILABLE);
        retriever = null;
    }
    
    // ------------------------------------------------------------------------
    private void updateButton() {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                DownloadState state = AddServerLocationVisualPanel.this.downloadState;
                boolean licenseAccepted = agreeCheckBox.isSelected();
                File val = new File(hk2HomeTextField.getText().trim());
                boolean writableLoc = AddServerLocationPanel.canCreate(val) || Utils.canWrite(val);
                String buttonTextKey = 
                        state == DownloadState.DOWNLOADING ? "LBL_CancelDownload" : 
                        state == DownloadState.COMPLETED ? "LBL_DownloadComplete" : "LBL_DownloadNow";
                String buttonText = NbBundle.getMessage(AddServerLocationVisualPanel.class, buttonTextKey);
                downloadButton.setText(buttonText);
                downloadButton.setEnabled(state != DownloadState.COMPLETED && licenseAccepted && writableLoc);
            }
        });
    }
    
    private synchronized void setDownloadState(DownloadState state) {
        downloadState = state;
        updateButton();
    }
    
    private void homeFolderChanged() {
        updateMessageText("");
        if(downloadState == DownloadState.COMPLETED) {
            setDownloadState(DownloadState.AVAILABLE);
        } else {
            updateButton();
        }
    }
       
    private static class DirFilter extends javax.swing.filechooser.FileFilter {
        DirFilter() {
        }
        
        @Override
        public boolean accept(File f) {
            if(!f.exists() || !f.canRead() || !f.isDirectory()) {
                return false;
            } else {
                return true;
            }
        }
        
        @Override
        public String getDescription() {
            return NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_DirType");
        }
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        hk2HomeLabel = new javax.swing.JLabel();
        hk2HomeTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        downloadButton = new javax.swing.JButton();
        agreeCheckBox = new javax.swing.JCheckBox();
        readlicenseButton = new javax.swing.JButton();
        downloadStatusLabel = new javax.swing.JLabel();

        hk2HomeLabel.setLabelFor(hk2HomeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(hk2HomeLabel, org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_InstallLocation")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_BrowseButton")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        downloadButton.setText("[download/cancel]"); // NOI18N
        downloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadButtonActionPerformed(evt);
            }
        });

        agreeCheckBox.setMargin(new java.awt.Insets(4, 4, 4, 4));
        agreeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                agreeCheckBoxActionPerformed(evt);
            }
        });

        readlicenseButton.setText(org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ReadLicenseText")); // NOI18N
        readlicenseButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        readlicenseButton.setBorderPainted(false);
        readlicenseButton.setContentAreaFilled(false);
        readlicenseButton.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        readlicenseButton.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        readlicenseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readlicenseButtonActionPerformed(evt);
            }
        });

        downloadStatusLabel.setText("[download status]"); // NOI18N
        downloadStatusLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        downloadStatusLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hk2HomeLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(downloadButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(agreeCheckBox)
                        .addGap(2, 2, 2)
                        .addComponent(readlicenseButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(hk2HomeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton))
                    .addComponent(downloadStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(hk2HomeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hk2HomeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(downloadButton)
                    .addComponent(agreeCheckBox)
                    .addComponent(readlicenseButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(downloadStatusLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        hk2HomeTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, "AddServerLocationVisualPanel.hk2HomeTextField.AccessibleContext.accessibleDescription")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, "AddServerLocationVisualPanel.browseButton.AccessibleContext.accessibleDescription")); // NOI18N
        agreeCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, "AddServerLocationVisualPanel.agreeCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        readlicenseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, "AddServerLocationVisualPanel.readlicenseButton.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void readlicenseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readlicenseButtonActionPerformed
        try {
            URLDisplayer.getDefault().showURL(
                    new URL("https://glassfish.dev.java.net/public/CDDL+GPL.html")); //NOI18N
        } catch (Exception ex){
            Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex);
        }
}//GEN-LAST:event_readlicenseButtonActionPerformed

private void downloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadButtonActionPerformed
        if(retriever == null) {
            ServerDetails selectedValue = wizardIterator.possibleValues[0];
            if (wizardIterator.possibleValues.length > 1) {
                selectedValue = (ServerDetails) JOptionPane.showInputDialog(null,
                    NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooseOne"), // NOI18N
                    NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_SELECT_BITS"), // NOI18N
                    JOptionPane.INFORMATION_MESSAGE, null,
                    wizardIterator.possibleValues, wizardIterator.possibleValues[0]);
            }
            if (null != selectedValue) {
            updateStatusText("");  // NOI18N
            retriever = new Retriever(new File(hk2HomeTextField.getText()), 
                    selectedValue.getIndirectUrl(), V3_DOWNLOAD_PREFIX,
                    selectedValue.getDirectUrl(),
                    this, "glassfish"); // NOI18N
            new Thread(retriever).start();
            setDownloadState(DownloadState.DOWNLOADING);
            }
        } else {
            retriever.stopRetrieval();
            setDownloadState(DownloadState.AVAILABLE);
        }
}//GEN-LAST:event_downloadButtonActionPerformed

private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        String newLoc = browseHomeLocation();
        if(newLoc != null && newLoc.length() > 0) {
            hk2HomeTextField.setText(newLoc);
        }
}//GEN-LAST:event_browseButtonActionPerformed

private void agreeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agreeCheckBoxActionPerformed
        DownloadState state = downloadState;
        if(state == DownloadState.COMPLETED) {
            setDownloadState(DownloadState.AVAILABLE);
        } else {
            File val = new File(hk2HomeTextField.getText().trim());
            boolean writableLoc = AddServerLocationPanel.canCreate(val) || Utils.canWrite(val);
            downloadButton.setEnabled(agreeCheckBox.isSelected() && writableLoc);
        }
}//GEN-LAST:event_agreeCheckBoxActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox agreeCheckBox;
    private javax.swing.JButton browseButton;
    private javax.swing.JButton downloadButton;
    private javax.swing.JLabel downloadStatusLabel;
    private javax.swing.JLabel hk2HomeLabel;
    private javax.swing.JTextField hk2HomeTextField;
    private javax.swing.JButton readlicenseButton;
    // End of variables declaration//GEN-END:variables
    
}
