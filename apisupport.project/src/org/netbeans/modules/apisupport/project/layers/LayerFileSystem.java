package org.netbeans.modules.apisupport.project.layers;

import java.io.IOException;
import java.util.Locale;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.MultiFileSystem;

public class LayerFileSystem extends MultiFileSystem {

    protected final BadgingSupport status;

    public LayerFileSystem(final FileSystem[] layers) {
        super(layers);
        status = new BadgingSupport(this);
        status.setSuffix("_" + Locale.getDefault());
        setPropagateMasks(true);
    }

    @Override
    public FileSystem.Status getStatus() {
        return status;
    }


    public FileSystem[] getLayerFileSystems() {
        return getDelegates();
    }

    @Override
    protected FileSystem createWritableOn(String name) throws IOException {
        if( name.endsWith(LayerUtils.HIDDEN) ) {
            FileObject fo = findResource(name);
            if( null != fo ) {
                try {
                    FileSystem fs = findSystem(fo);
                    if( fs.isReadOnly() )
                        throw new IOException();
                } catch( IllegalArgumentException e ) {
                    //ignore
                }
            }
        }
        return super.createWritableOn(name);
    }
}
