package org.netbeans.modules.php.editor.model.impl;

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.AliasedName;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.UseElement;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatementPart;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

class UseElementImpl extends ModelElementImpl implements UseElement {
    private AliasedName aliasName;
    UseElementImpl(NamespaceScopeImpl inScope, ASTNodeInfo<UseStatementPart> node) {
        this(inScope,node.getName(),inScope.getFile(),node.getRange());
        final Identifier alias = node.getOriginalNode().getAlias();
        this.aliasName = alias != null ? new AliasedName(alias.getName(),QualifiedName.create(getName())) : null;
    }

    private UseElementImpl(ScopeImpl inScope, String name,
            Union2<String, FileObject> file, OffsetRange offsetRange) {
        super(inScope, name, file, offsetRange, PhpElementKind.USE_STATEMENT);
    }

    @Override
    public AliasedName getAliasedName() {
        return aliasName;
    }
}
