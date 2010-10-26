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

package org.netbeans.modules.cnd.debugger.common2.debugger.remote;

import java.util.ArrayList;
import java.util.List;
import java.text.MessageFormat;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


import org.openide.windows.WindowManager;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;

import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;

import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;

import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.debugger.common2.utils.Executor;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.StringConfiguration;
// LATER import org.netbeans.modules.nativeexecution.support.Authentication;

/**
 * Bridge utility between our remote DB to the CND remote DB.
 */

public class CndRemote {
    /**
     * Convert a combo-box index into a HostName
     */
    public static String hostNameFromIndex(int index) {
	if (index < 0)
	    return null;
        if (DebuggerManager.isStandalone()) {
            // string in combo-box has more than just hostname in it
	    HostList hostList = DebuggerManager.get().getHostList();
            Host host = hostList.getRecordAt(index);
            if (host == null)
                return null;
            return host.getHostName();
        } else {
	/*
	 * Server list does NOT support getting/setting by index any more
	    ServerList serverList = Lookup.getDefault().lookup(ServerList.class);
	*/
	    //String serverKey = serverList.getRecords().get[index];
            // IZ 179270
	    String[] IdList = getServerListIDs();
	    if (index < IdList.length)
		return IdList[index];
	    else
		return "localhost"; // NOI18N
        }
    }

    /**
     * Convert a combo-box index into a Host
     */
    public static Host OLD_hostFromIndex(int index) {
	if (index < 0)
	    return null;
        if (DebuggerManager.isStandalone()) {
	    HostList hostList = DebuggerManager.get().getHostList();
            return hostList.getRecordAt(index);
        } else {
	/*
	 * Server list does NOT support getting/setting by index any more
	    ServerList serverList = Lookup.getDefault().lookup(ServerList.class);
	*/
	    //String serverKey = serverList.getServerNames()[index];

            // IZ 179270
            String serverKey = getServerListIDs()[index];
	    return hostFromName(null, serverKey);
        }
    }

    private final static RequestProcessor validatorRP = 
			new RequestProcessor("validator"); // throughput 1 // NOI18N
    
    /**
     * If the host 'name' is offline, bring it online and get it's
     * compiler-set up-to-date.
     * Once everything is ready continuation is called.
     * See IZ 147560.
     */
    public static void validate(String name, final Runnable continuation) {

	if (DebuggerManager.isStandalone() && !Executor.CND_EXEC) {
	    continuation.run();
	    return;
	}
	if (name != null && name.equals("localhost")) { // NOI18N
	    continuation.run();
	    return;
	}
        
        // extend name back to full form
        if (DebuggerManager.isStandalone()) {
            HostList hostList = DebuggerManager.get().getHostList();
            if (hostList != null) {
                Host host = hostList.getHostByName(name);
                if (host == null) {
                    host = hostList.getHostByDispName(name);
                }
                name = host.getHostKey();
            }
        }

	final ServerRecord serverRecord = ServerList.get(ExecutionEnvironmentFactory.fromUniqueID(name));

	Runnable validator = new Runnable() {
	    public void run() {
		serverRecord.validate(true);
                // No need to continue if connection is not available
                if (!serverRecord.isOnline()) {
                    return;
                }
                ExecutionEnvironment exEnv = serverRecord.getExecutionEnvironment();
		CompilerSetManager csm = CompilerSetManager.get(exEnv);
		csm.initialize(true, true, null);
                platformInfo(exEnv);
		try {
		    javax.swing.SwingUtilities.invokeAndWait(continuation);
		} catch (Exception x) {
		    ErrorManager.getDefault().notify(x);
		    final String message = MessageFormat.format(Catalog.get("ERR_Cant_Cnnect"), serverRecord.getDisplayName()); // NOI18N
		    final String title = Catalog.get("DLG_TITLE_Cant_Connect"); //NOI18N
		    SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    JOptionPane.showMessageDialog(
				WindowManager.getDefault().getMainWindow(),
				message, title, JOptionPane.ERROR_MESSAGE);
			}
		    });
		}
	    }
	};
	RequestProcessor.Task task = validatorRP.post(validator);
    }

    /**
     * Convert a host display name "localhost (Solaris_intel) " from either
     * - cnd's host and cset DB's
     * - dbxtools host DB
     * to our host.
     * @return host passed in or new one if the passed-in one is null.
     */
    public static Host hostFromDispName(Host host, String name) {
	if (host == null)
	    host = new Host();

	if (DebuggerManager.isStandalone()) {
	    host = DebuggerManager.get().getHostList().getHostByDispName(name);
	} else {
            // don't apply to IDE remote
	    assert false : "Cndremote.hostfromDispName() called under IDE";
	}
	return host;
    }

    public static String[] getServerListIDs() {
        List<String> l = new ArrayList<String>();
        for (ExecutionEnvironment env : ServerList.getEnvironments()) {
            l.add(ExecutionEnvironmentFactory.toUniqueID(env));
        }
        return l.toArray(new String[l.size()]);
    }

    private static SecuritySettings getSecuritySettings(ExecutionEnvironment ee) {
	SecuritySettings securitySettings;
	securitySettings = new SecuritySettings(ee.getSSHPort(), null);
	/* LATER
	Authentication a = Authentication.getFor(ee);
	switch (a.getType()) {
	    case PASSWORD:
		securitySettings = new SecuritySettings(ee.getSSHPort(), null);
		break;
	    case SSH_KEY:
		securitySettings = new SecuritySettings(ee.getSSHPort(), a.getKey());
		break;
	    default:
	    case UNDEFINED:
		securitySettings = new SecuritySettings(ee.getSSHPort(), null);
		break;
	}
	 */
	return securitySettings;
    }

    /**
     * Convert a hostname from either
     * - cnd's host and cset DB's
     * - dbxtools host DB
     * to our host.
     * @return host passed in or new one if the passed-in one is null.
     */
    public static Host hostFromName(Host host, String name) {
	if (Log.Remote.host)
	    System.out.printf("hostFromName(%s, %s)\n", host, name); // NOI18N

	if (host == null)
	    host = new Host();

	if (name == null)
	    name = "localhost";		// NOI18N

	if (DebuggerManager.isStandalone()) {
	    host = DebuggerManager.get().getHostList().getHostByName(name);
	} else {
	    /*
	     * not apply anymore
	     * ServerList is static
	     ServerList serverList = Lookup.getDefault().lookup(ServerList.class);
	     */

            final ServerRecord serverRecord = ServerList.get(ExecutionEnvironmentFactory.fromUniqueID(name));
	    String userName = serverRecord.getUserName();
	    String hostName = serverRecord.getServerName();

	    if ("127.0.0.1".equals(hostName))			// NOI18N
		hostName = "localhost";				// NOI18N


	    String userHostName;
	    if ("localhost".equals(hostName))			// NOI18N
		userHostName = hostName;
	    else
		userHostName = userName + "@" + hostName; // NOI18N

	    ExecutionEnvironment ee = serverRecord.getExecutionEnvironment();

	    host.setHostName(hostName);
	    host.setHostLogin(userName);
	    host.setSecuritySettings(getSecuritySettings(ee));



	    // convert Cnd platform info to Host platform		
	    // for remote debugging that launched from "Debug Executable"
            ExecutionEnvironment exEnv = serverRecord.getExecutionEnvironment();
	    String pName = platformInfo(exEnv); // generic platform name
	    if (pName != null)
		host.setPlatformName(pName);

	    CompilerSetManager csm =
		CompilerSetManager.get(exEnv);

	    CompilerSet cs = csm.getDefaultCompilerSet();
	    if (cs == null) {
		String csname = "SunStudio";		// NOI18N
		cs = csm.getCompilerSet(csname);
	    }

	    if (Log.Remote.host)
		System.out.printf("hostFromName() cs %s\n", cs); // NOI18N
	    if (cs != null) {
		String base = cs.getDirectory();
		if (Log.Remote.host)
		    System.out.printf("hostFromName() base %s\n", base); // NOI18N
		host.setRemoteStudioLocation(base + "/.."); // NOI18N
	    } else {
		// explicitly set to null so we don't end up with the
		// default value.
		host.setRemoteStudioLocation(null);
		// Executor will fall back an a non glue-based provider
	    }
	}
	return host;
    }

    /**
     * Return the usr@hostname stored in 'conf'.
     */
    public static String userhostFromConfiguration(Configuration conf) {
	if (! (conf instanceof MakeConfiguration))
	    return "localhost"; // NOI18N

	MakeConfiguration makeConfiguration = (MakeConfiguration) conf;

	DevelopmentHostConfiguration hostConfig =
	    makeConfiguration.getDevelopmentHost();

        return ExecutionEnvironmentFactory.toUniqueID(hostConfig.getExecutionEnvironment());
    }

    /**
     * Convert information pointed to by 'conf' in cnd's host and cset DB's
     * to our Host.
     * @return host passed in or new one if the passed-in one is null.
     */
    public static Host hostFromConfiguration(Host host, Configuration conf) {
	if (Log.Remote.host)
	    System.out.printf("hostFromConfiguration(%s, %s)\n", host, conf); // NOI18N
	if (host == null)
	    host = new Host();

	if (! (conf instanceof MakeConfiguration))
	    return host;

	MakeConfiguration makeConfiguration = (MakeConfiguration) conf;

	DevelopmentHostConfiguration hostConfig =
	    makeConfiguration.getDevelopmentHost();


	// userHostName is of the form <user>@<hostname>
	String userName;
	String hostName;
	String platformName;

        String userHostName = ExecutionEnvironmentFactory.toUniqueID(hostConfig.getExecutionEnvironment());
	if ("127.0.0.1".equals(userHostName))	    // NOI18N
	    userHostName = "localhost";		    // NOI18N

	if ("localhost".equals(userHostName)) {	    // NOI18N
	    userName = null;
	    hostName = userHostName;
	} else {
	    int atx = userHostName.indexOf('@');
	    userName = userHostName.substring(0, atx);
	    hostName = userHostName.substring(atx+1);
	}

	// covert Cnd platform info to Host platform
        ExecutionEnvironment exEnv = hostConfig.getExecutionEnvironment();
	String pName = platformInfo(exEnv); // generic platform name
	if (pName != null)
	    host.setPlatformName(pName);

	CompilerSetManager csm = CompilerSetManager.get(exEnv);

	CompilerSet2Configuration csconf = makeConfiguration.getCompilerSet();
	if (Log.Remote.host)
	    System.out.printf("hostFromConfiguration() csm %s  csconf %s\n", csm, csconf); // NOI18N

	if (csm != null && csconf.isValid()) {
	    String csname = csconf.getOption();
	    CompilerSet cs = csm.getCompilerSet(csname);
	    if (Log.Remote.host)
		System.out.printf("hostFromConfiguration() csname %s  cs %s\n", csname, cs); // NOI18N
	    if (cs != null) {
		host.setHostName(hostName);
		if (userName != null)
		    host.setHostLogin(userName);

		String base = cs.getDirectory();        // usually .../bin
		if (Log.Remote.host)
		    System.out.printf("hostFromConfiguration() base %s\n", base); // NOI18N
		host.setRemoteStudioLocation(base + "/.."); // NOI18N
		// platform determined automatically
		host.setSecuritySettings(getSecuritySettings(exEnv));
	    }
	}

	return host;
    }

    /**
     * Convert information described by host into CND-style and stuff it
     * into 'conf' and the relevant cset.
     * Sort of the reverse of hostFromConfiguration().
     */
    public static void fillConfiguratioFromHost(Configuration conf, Host host) {
	String userHostName = host.getHostKey();

	MakeConfiguration makeConfiguration = (MakeConfiguration) conf;
	if (! makeConfiguration.isMakefileConfiguration())
	    return;

	DevelopmentHostConfiguration hostConfig =
	    makeConfiguration.getDevelopmentHost();

        ExecutionEnvironment exEnv = ExecutionEnvironmentFactory.createNew(host.getHostLogin(), host.getHostName());
	CompilerSetManager csm = CompilerSetManager.get(exEnv);

	CompilerSet2Configuration csconf = makeConfiguration.getCompilerSet();
	

	if (csm == null || ! csconf.isValid())
	    return;

	// default compiler set of configuration is GNU, this work around the problem
	// IZ 150946 was filed to request API to set compiler set in MakeConfiguration
	String csname = "SunStudio"; // NOI18N
	StringConfiguration compilerSetName = new StringConfiguration(null, csname);
	csconf.setCompilerSetName(compilerSetName);
	
	// 6762221
	// String csname = csconf.getOption(); <== default GNU
	CompilerSet cs = csm.getCompilerSet(csname);
	if (cs == null) {
	    // See IZ 147560
	    return;
	}

	String base = host.getRemoteStudioLocation();
	base += "/bin"; // NOI18N
	hostConfig.setValue(userHostName);

	// point of no return

	/* LATER
	The relevant CND objects don't have setters ...


	cs.setDirectory(base);
	*/
    }

    // covert Cnd platform info to Platform name
    private static String platformInfo (ExecutionEnvironment exEnv) {

        // OLD NB69 int platformx = HostInfoProvider.getPlatform(exEnv);
	PlatformInfo platformInfo = PlatformInfo.getDefault(exEnv);
	int platformx = platformInfo.getPlatform();

	return platformByCNDId(platformx).name();

	/* OLD
	String platformname = null;
	switch (platformx) {
	    case PlatformTypes.PLATFORM_LINUX:
		platformname = "Linux_x86";
		break;
	    case PlatformTypes.PLATFORM_SOLARIS_INTEL:
		platformname = "Solaris_x86";
		break;
	    case PlatformTypes.PLATFORM_SOLARIS_SPARC:
		platformname = "Solaris_Sparc";
		break;
	    case PlatformTypes.PLATFORM_MACOSX:
		platformname = "MacOSX_x86";
		break;
	    case PlatformTypes.PLATFORM_WINDOWS:
		platformname = "Windows_x86";
		break;
	}

	return platformname;
	*/
    }

    static Platform platformByCNDId(int id) {
	switch (id) {
	    case PlatformTypes.PLATFORM_LINUX:
		return Platform.Linux_x86;

	    case PlatformTypes.PLATFORM_SOLARIS_INTEL:
		return Platform.Solaris_x86;

	    case PlatformTypes.PLATFORM_SOLARIS_SPARC:
		return Platform.Solaris_Sparc;

	    case PlatformTypes.PLATFORM_MACOSX:
		return Platform.MacOSX_x86;

	    case PlatformTypes.PLATFORM_WINDOWS:
		return Platform.Windows_x86;

	    default:
		return Platform.Unknown;
	}
    }

}
