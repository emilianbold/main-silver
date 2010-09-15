package org.netbeans.modules.php.api.editor;

import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Class representing a PHP element ({@link PhpClass PHP class}, method, field etc.).
 * @since 1.13
 */
public abstract class PhpBaseElement {

    private final String name;
    private final String fullyQualifiedName;
    private FileObject file;
    private final int offset;
    private final String description;
    private final PhpClass type;

    /**
     * @since 1.32
     */
    protected PhpBaseElement(String name, PhpClass type) {
        this(name, null, type, null, -1, null);
    }

    protected PhpBaseElement(String name, String fullyQualifiedName) {
        this(name, fullyQualifiedName, -1, null);
    }

    /**
     * @since 1.25
     */
    protected PhpBaseElement(String name, String fullyQualifiedName, FileObject file) {
        this(name, fullyQualifiedName, file, -1, null);
    }

    protected PhpBaseElement(String name, String fullyQualifiedName, String description) {
        this(name, fullyQualifiedName, -1, description);
    }

    protected PhpBaseElement(String name, String fullyQualifiedName, int offset) {
        this(name, fullyQualifiedName, offset, null);
    }

    protected PhpBaseElement(String name, String fullyQualifiedName, int offset, String description) {
        this(name, fullyQualifiedName, null, offset, description);
    }

    /**
     * @since 1.25
     */
    protected PhpBaseElement(String name, String fullyQualifiedName, FileObject file, int offset, String description) {
        this(name, fullyQualifiedName, null, file, offset, description);
    }

    /**
     * @since 1.32
     */
    protected PhpBaseElement(String name, String fullyQualifiedName, PhpClass type, FileObject file, int offset, String description) {
        Parameters.notEmpty("name", name);

        this.name = name;
        this.fullyQualifiedName = fullyQualifiedName;
        this.type = type;
        this.file = file;
        this.offset = offset;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    /**
     * @since 1.32
     */
    public PhpClass getType() {
        return type;
    }

    /**
     * @since 1.25
     */
    public FileObject getFile() {
        return file;
    }

    public int getOffset() {
        return offset;
    }

    public String getDescription() {
        return description;
    }

    /**
     * @param file the file to set
     */
    public void setFile(FileObject file) {
        this.file = file;
    }
}
