/*
 * SvnConfigFilesTest.java
 * JUnit based test
 *
 * Created on June 26, 2007, 3:50 PM
 */

package org.netbeans.modules.subversion.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.ui.repository.RepositoryConnection;
import org.openide.util.NbPreferences;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Peter Pis
 * 
 * Test of creation and merging "config" and "servers" subversion administrative files. 
 * 
 * - svn1..X - represents subversion configuration directory for user 
 * - golden1..x - represents expected results of generated content of subversion config directory by IDE according to appropriate svn1..x directory
 * - each loop generates subversion config directory into: "/tmp" + File.separator + "svn" + File.separator + "config" + System.currentTimeMillis();
 * 
 * svn1: 
 *   config: standart content with basic [tunnels] content
 *   servers: standart content with [groups] enabled. Nb Proxy has always right of way.
 * 
 * svn2:
 *   config: standart content with [helpers] content    
 *   servers: standart content with [groups] enabled. Copy non-related proxy settings from [global] section
 * 
 * svn3:
 *   config: standart content with [miscellany] and [auto-props] content    
 *   servers: standart content with [groups] enabled. Copy non-related proxy settings from [global] and specific GROUP section
 * 
 * svn4:
 *   config: empty file ... generate default content 
 *   servers: empty file ... generate default content
 * 
 * svn5:
 *   config: standard content with not-used section by subversion. This section should be merged into the generated file too.
 *   servers: standard content with not-used key/value that should be merged into the generated file.
 * 
 * svn6:
 *   config: without commented lines.
 *   servers: group with wildcard - *.czech.sun.com
 * 
 * svn7:
 *   config: without commented lines.
 *   servers: group with wildcard - peterp.*.sun.com
 * 
 * svn8:
 *   config: without commented lines.
 *   servers: group with wildcard - peterp.*
 
 * 
 */
public class SvnConfigFilesTest extends NbTestCase {

    private static String SYSTEM_PROXY_HOST = "system.cache.org";
    private static String SYSTEM_PROXY_PORT = "777";
    private static String USER_PROXY_HOST = "my.webcache";
    private static String USER_PROXY_PORT = "8080";

    private Preferences proxyPreferences;
    private ProxySelector selector;
    
    String svnUserPath = "";
    String svnNbPath = "";
    String svnGoldenPath = "";
    private ProxySelector defaultPS;

    public SvnConfigFilesTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        
        if (defaultPS == null) {
            defaultPS = ProxySelector.getDefault();
        }
        
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        ProxySelector.setDefault(defaultPS);
        System.setProperty("netbeans.t9y.svn.user.config.path", "");
        System.setProperty("netbeans.t9y.svn.nb.config.path", "");
    }

    public void testSSL() throws MalformedURLException, IOException {
        SVNUrl url = new SVNUrl("https://feher.lo.nem.lo.com/kuon");
        RepositoryConnection rc = new RepositoryConnection(
                url.toString(),
                "usr", "psswd".toCharArray(), null, false, "/cert/file", "pssphrs".toCharArray());

        SvnModuleConfig.getDefault().insertRecentUrl(rc);
        String path = "/tmp" + File.separator + "svn" + File.separator + "config" + System.currentTimeMillis();
        System.setProperty("netbeans.t9y.svn.nb.config.path", path);
        SvnConfigFiles scf = SvnConfigFiles.getInstance();
        scf.storeSvnServersSettings(url);

        File serversFile = new File(path + "/servers");
        long lastMod = serversFile.lastModified();
        Section s = getSection(serversFile);
        assertNotNull(s);

        // values were written
        assertEquals("/cert/file", s.get("ssl-client-cert-file"));
        assertEquals("pssphrs", s.get("ssl-client-cert-password"));

        // nothing was changed ...
        scf.storeSvnServersSettings(url);
        // ... the file so also the file musn't change
        assertEquals(lastMod, serversFile.lastModified());

        // lets change the credentials ...
        rc = new RepositoryConnection(
                url.toString(),
                "usr", "psswd".toCharArray(), null, false, "/cert/file2", "pssphrs2".toCharArray());
        SvnModuleConfig.getDefault().insertRecentUrl(rc);
        scf.storeSvnServersSettings(url);
        s = getSection(serversFile);
        // values were written
        assertNotNull(s);
        assertNotSame(lastMod, serversFile.lastModified());
        assertEquals("/cert/file2", s.get("ssl-client-cert-file"));
        assertEquals("pssphrs2", s.get("ssl-client-cert-password"));

        // lets test a new url
        url = url.appendPath("whatever");
        rc = new RepositoryConnection(
                url.toString(),
                "usr", "psswd".toCharArray(), null, false, "/cert/file3", "pssphrs3".toCharArray());
        SvnModuleConfig.getDefault().insertRecentUrl(rc);
        lastMod = serversFile.lastModified();
        scf.storeSvnServersSettings(url);
        s = getSection(serversFile);
        // values were written
        assertNotNull(s);
        assertNotSame(lastMod, serversFile.lastModified());
        assertEquals("/cert/file3", s.get("ssl-client-cert-file"));
        assertEquals("pssphrs3", s.get("ssl-client-cert-password"));
    }

    public void testSSH() throws MalformedURLException, IOException {
        // TODO
    }

    public void testProxy() throws IOException {
        String[] proxy = {"my.proxy", "my.proxy", "my.proxy", "", "", "my.proxy", "my.proxy", "my.proxy", null, null};       
        //for (int i = 1; i < proxy.length + 1; i++) {
        for (int i = 1; i < proxy.length + 1; i++) {           
            //changeSvnConfigLocation("svn" + i, "golden" + i, "my.proxy", "8080");
            changeSvnConfigLocation("svn" + i, "golden" + i, proxy[i-1], "8080");           
            //Compare and verify
            //config file
            isSubsetOf(svnNbPath + java.io.File.separator + "config", svnGoldenPath + java.io.File.separator + "config");                                   
            //servers file
            isSubsetOf(svnNbPath + java.io.File.separator + "servers", svnGoldenPath + java.io.File.separator + "servers");            
        } 
    } /* Test of getNBConfigPath method, of class SvnConfigFiles. */

    /*
     * Method changes svn config files location
     * @param source defines the directory that contain test subversion 'config' and 'servers' file of DATA folder
     * @param proxyHost defines a proxy host
     * @param proxyPort defines a proxy port
     *
     */
    private void changeSvnConfigLocation(String source, String golden, String proxyHost, String proxyPort) {
        //set svn user config path - DATA test folder + source
        try {
            svnUserPath = getDataDir().getCanonicalPath() + File.separator + "subversion" + File.separator + source;
            svnGoldenPath = getDataDir().getCanonicalPath() + File.separator + "subversion" + File.separator + golden;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.setProperty("netbeans.t9y.svn.user.config.path", svnUserPath);

        //create temporary location for svn config files generated by the IDE
        svnNbPath = "/tmp" + File.separator + "svn" + File.separator + "config" + System.currentTimeMillis();
        //System.out.println("work: " + svnNbPath);
        final File tmp = new File(svnNbPath);
        
        Thread t = new Thread(new Runnable() {
           public void run() {
            tmp.mkdirs();
                while(!tmp.isDirectory()) {
                    try {
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e) {}    
               }
           } 
        });
        t.start();
        try {
            t.join(3000);
        } catch (InterruptedException e) {
            
        }
        //tmp.deleteOnExit();
        System.setProperty("netbeans.t9y.svn.nb.config.path", svnNbPath);
        setupProxy(proxyHost, proxyPort);
                
        SvnConfigFiles scf = SvnConfigFiles.getInstance();
        try {
            scf.storeSvnServersSettings(new SVNUrl("http://peterp.czech.sun.com/svn"));
        } catch (MalformedURLException me) {
        }

    }

    private void setupProxy(final String proxyHost, final String proxyPort) {
        ProxySelector ps = new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                if (uri == null) {
                    return Collections.singletonList(Proxy.NO_PROXY);
                }
                if(proxyHost == null) {
                    return Collections.singletonList(Proxy.NO_PROXY);
                }
                return Collections.singletonList(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort))));
            }
            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        ProxySelector.setDefault(ps);                
    }

    private Section getSection(File serversFile) throws FileNotFoundException, IOException {
        FileInputStream is = new FileInputStream(serversFile);
        Ini ini = new Ini();
        try {
            ini.load(is);
        } finally {
            is.close();
        }
        return ini.get("global");
    }

    private String getContent(String fileName) {
        StringBuffer content = new StringBuffer("");
        BufferedReader br = null;
        
        try {
            br = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
                content.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }    
            }
        }
        
        return content.toString();
    }
    
    public void printArray(String[] array) {
        System.out.println("=== " + array.length + " ===");
        for (String string : array) {
            System.out.println(string);
        }
        System.out.println("===");
    }

    public void testWinUserAppdata() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        SvnConfigFiles config = SvnConfigFiles.getInstance();
        Method m = config.getClass().getDeclaredMethod("getWinUserAppdata", new Class[]{String.class, String.class});
        m.setAccessible(true);
        
        String ret = (String) m.invoke(config, new Object[] {"", ""});
        assertEquals("", ret);

        ret = (String) m.invoke(config, new Object[] {"c:\\dil\\dil", "c:\\foo\\bar"});
        assertEquals("c:\\foo\\bar/dil", ret);

        ret = (String) m.invoke(config, new Object[] {"c:\\dil\\dil", "c:\\foo\\bar\\"});
        assertEquals("c:\\foo\\bar/dil", ret);

        ret = (String) m.invoke(config, new Object[] {"c:\\dil\\dil", "c:\\foo\\bar"});
        assertEquals("c:\\foo\\bar/dil", ret);

        ret = (String) m.invoke(config, new Object[] {"c:\\dil\\dil\\", "c:\\foo\\bar\\"});
        assertEquals("c:\\foo\\bar/dil", ret);

    }

    private void isSubsetOf(String sourceIniPath, String expectedIniPath) throws IOException {
        Ini goldenIni = new Ini(new FileInputStream(expectedIniPath));
        Ini sourceIni = new Ini(new FileInputStream(sourceIniPath));
        for(String key : goldenIni.keySet()) {
            if(!sourceIni.containsKey(key) && goldenIni.get(key).size() > 0) {
                fail("missing section " + key + " in file " + sourceIniPath);
            }

            Section goldenSection = goldenIni.get(key);
            Section sourceSection = sourceIni.get(key);

            for(String name : goldenSection.childrenNames()) {
                if(!sourceSection.containsKey(name)) {
                    fail("missing name " + name + " in file " + sourceIniPath + " section [" + name + "]");
                }                
                assertEquals(goldenSection.get(name), sourceSection.get(name));
            }                
        }
    }
    
}
