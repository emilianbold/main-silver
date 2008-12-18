package org.netbeans.core.netigso;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.jar.Manifest;
import org.netbeans.Events;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.openide.modules.SpecificationVersion;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

final class NetigsoModule extends Module {

    final Bundle bundle;
    private NetigsoLoader loader;
    private Manifest manifest;

    public NetigsoModule(File jar, ModuleManager mgr, Events ev, Object history, boolean reloadable, boolean autoload, boolean eager) throws IOException {
        super(mgr, ev, history, reloadable, autoload, eager);
        try {
            BundleContext bc = NetigsoModuleFactory.getContainer().getBundleContext();
            bundle = bc.installBundle(jar.toURI().toURL().toExternalForm());
            manifest = new Manifest();
        } catch (BundleException ex) {
            throw (IOException) new IOException(ex.getMessage()).initCause(ex);
        }
    }

    @Override
    public String[] getProvides() {
        return new String[0];
    }

    @Override
    public String getCodeName() {
        return bundle.getSymbolicName();
    }

    @Override
    public String getCodeNameBase() {
        return bundle.getSymbolicName();
    }

    @Override
    public int getCodeNameRelease() {
        String version = (String) bundle.getHeaders().get("Bundle-SymbolicName");
        // NOI18N
        int slash = version.lastIndexOf('/');
        if (slash != -1) {
            return Integer.parseInt(version.substring(slash + 1));
        }
        return -1;
    }

    @Override
    public SpecificationVersion getSpecificationVersion() {
        String version = (String) bundle.getHeaders().get("Bundle-Version");
        // NOI18N
        return new SpecificationVersion(version);
    }

    @Override
    protected void parseManifest() throws InvalidException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<File> getAllJars() {
        return Collections.emptyList();
    }

    @Override
    public void setReloadable(boolean r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reload() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void classLoaderUp(Set<Module> parents) throws IOException {
        if (bundle.getState() != Bundle.INSTALLED) {
            return;
        }
        try {
            bundle.start();
        } catch (BundleException ex) {
            throw (IOException) new IOException(ex.getMessage()).initCause(ex);
        }
        loader = new NetigsoLoader(bundle);
        assert bundle.getState() == Bundle.ACTIVE;
    }

    @Override
    protected void classLoaderDown() {
        assert bundle.getState() == Bundle.ACTIVE;
        try {
            bundle.stop();
        } catch (BundleException ex) {
            throw new IllegalStateException(ex);
        }
        loader = null;
    }

    @Override
    public ClassLoader getClassLoader() throws IllegalArgumentException {
        return loader;
    }

    @Override
    protected void cleanup() {
    }

    @Override
    protected void destroy() {
    }

    @Override
    public boolean isFixed() {
        return false;
    }

    @Override
    public Manifest getManifest() {
        return manifest;
    }

    @Override
    public Object getLocalizedAttribute(String attr) {
        // TBD;
        return null;
    }

    @Override
    public String toString() {
        return "Netigso: " + getCodeName();
    }
}
