package org.netbeans.modules.java.j2seproject.ui.customizer.vmo;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

/**
 * @author Rastislav Komara
 */
public class JavaVMOption<V extends OptionValue<?>> extends CommonTree implements Comparable<JavaVMOption<?>>{
    private String name;
    private V value;
    /**
     * Indicated that this option should not be specified by user alone (e.g. classpath, bootclasspath)
     */
    private boolean valid = true;
    protected static final String SPACE = " ";
    protected static final char HYPHEN = '-';

    protected JavaVMOption(Token t) {
        super(t);
    }

    protected JavaVMOption(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public V getValue() {
        return value;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setValue(V value) {
        this.value = value;
    }

    public StringBuilder print(StringBuilder builder) {
        return ensureBuilder(builder);
    }

    protected StringBuilder ensureBuilder(StringBuilder builder) {
        if (builder == null) {
            builder = new StringBuilder();
        }
        return builder;
    }

    @Override
    public String toString() {
        return "JavaVMOption{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", valid=" + valid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JavaVMOption)) return false;

        JavaVMOption that = (JavaVMOption) o;

        if (!name.equals(that.name)) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    protected void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }

    public int compareTo(JavaVMOption<?> o) {
        return getName().compareTo(o.getName());
    }
}
