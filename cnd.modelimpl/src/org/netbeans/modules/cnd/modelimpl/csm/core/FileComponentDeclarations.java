/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.modelimpl.repository.FileDeclarationsKey;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.openide.util.CharSequences;

/**
 *
 * @author Alecander Simon
 */
public class FileComponentDeclarations extends FileComponent implements Persistent, SelfPersistent {

    private final TreeMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> declarations;
    private WeakReference<Map<CsmDeclaration.Kind,SortedMap<NameKey, CsmUID<CsmOffsetableDeclaration>>>> sortedDeclarations;
    private final ReadWriteLock declarationsLock = new ReentrantReadWriteLock();

    // empty stub
    private static final FileComponentDeclarations EMPTY = new FileComponentDeclarations() {

        @Override
        public void put() {
        }
    };

    public static FileComponentDeclarations empty() {
        return EMPTY;
    }

    /** Creates a new instance of ClassifierContainer */
    public FileComponentDeclarations(FileImpl file) {
        super(new FileDeclarationsKey(file));
        declarations = new TreeMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>>();
        put();
    }

    public FileComponentDeclarations(DataInput input) throws IOException {
        super(input);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        this.declarations = factory.readOffsetSortedToUIDMap(input, null);
    }

    // only for EMPTY static field
    private FileComponentDeclarations() {
        super((org.netbeans.modules.cnd.repository.spi.Key) null);
        declarations = new TreeMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>>();
    }

    Collection<CsmUID<CsmOffsetableDeclaration>> clean() {
        Collection<CsmUID<CsmOffsetableDeclaration>> uids;
        try {
            declarationsLock.writeLock().lock();
            uids = new ArrayList<CsmUID<CsmOffsetableDeclaration>>(declarations.values());
            sortedDeclarations = null;
            declarations.clear();
        } finally {
            declarationsLock.writeLock().unlock();
        }
        put();
        return uids;
    }

    boolean hasDeclarations() {
        return declarations.size() != 0;
    }

    Collection<CsmOffsetableDeclaration> getDeclarations() {
        Collection<CsmOffsetableDeclaration> decls;
        try {
            declarationsLock.readLock().lock();
            Collection<CsmUID<CsmOffsetableDeclaration>> uids = declarations.values();
            decls = UIDCsmConverter.UIDsToDeclarations(uids);
        } finally {
            declarationsLock.readLock().unlock();
        }
        return decls;
    }

    Iterator<CsmOffsetableDeclaration> getDeclarations(CsmFilter filter) {
        Iterator<CsmOffsetableDeclaration> out;
        try {
            declarationsLock.readLock().lock();
            out = UIDCsmConverter.UIDsToDeclarationsFiltered(declarations.values(), filter);
        } finally {
            declarationsLock.readLock().unlock();
        }
        return out;
    }

    int getDeclarationsSize(){
        return declarations.size();
    }

    Collection<CsmUID<CsmOffsetableDeclaration>> findDeclarations(CsmDeclaration.Kind[] kinds, CharSequence prefix) {
        Collection<CsmUID<CsmOffsetableDeclaration>> out = null;
        try {
            declarationsLock.readLock().lock();
            Map<CsmDeclaration.Kind, SortedMap<NameKey, CsmUID<CsmOffsetableDeclaration>>> map = null;
            if (sortedDeclarations != null) {
                map = sortedDeclarations.get();
            }
            if (map == null) {
                map = new EnumMap<CsmDeclaration.Kind, SortedMap<NameKey, CsmUID<CsmOffsetableDeclaration>>>(CsmDeclaration.Kind.class);
                for(CsmUID<CsmOffsetableDeclaration> anUid : declarations.values()){
                    CsmDeclaration.Kind kind = UIDUtilities.getKind(anUid);
                    SortedMap<NameKey, CsmUID<CsmOffsetableDeclaration>> val = map.get(kind);
                    if (val == null){
                        val = new TreeMap<NameKey, CsmUID<CsmOffsetableDeclaration>>();
                        map.put(kind, val);
                    }
                    val.put(new NameKey(anUid), anUid);
                }
                sortedDeclarations = new WeakReference<Map<CsmDeclaration.Kind, SortedMap<NameKey, CsmUID<CsmOffsetableDeclaration>>>>(map);
            }
            out = new ArrayList<CsmUID<CsmOffsetableDeclaration>>();
            for(CsmDeclaration.Kind kind : kinds) {
                 SortedMap<NameKey, CsmUID<CsmOffsetableDeclaration>> val = map.get(kind);
                 if (val != null) {
                     if (prefix == null) {
                         out.addAll(val.values());
                     } else {
                         NameKey fromKey = new NameKey(prefix, 0);
                         NameKey toKey = new NameKey(prefix, Integer.MAX_VALUE);
                         out.addAll(val.subMap(fromKey, toKey).values());
                     }
                 }
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        return out;
    }

    CsmOffsetableDeclaration findExistingDeclaration(int startOffset, int endOffset, CharSequence name) {
        OffsetSortedKey key = new OffsetSortedKey(startOffset, Math.abs(CharSequences.create(name).hashCode()));
        CsmUID<CsmOffsetableDeclaration> anUid = null;
        try {
            declarationsLock.readLock().lock();
            anUid = declarations.get(key);
            // It seems next line wrong, so commented when method was moved from FileImpl
            //sortedDeclarations = null;
        } finally {
            declarationsLock.readLock().unlock();
        }
        if (anUid != null && UIDUtilities.getEndOffset(anUid) != endOffset) {
            anUid = null;
        }
        return UIDCsmConverter.UIDtoDeclaration(anUid);
    }

    Collection<CsmUID<CsmOffsetableDeclaration>> getDeclarations(int startOffset, int endOffset) {
        List<CsmUID<CsmOffsetableDeclaration>> res;
        try {
            declarationsLock.readLock().lock();
            res = getDeclarationsByOffset(startOffset-1);
            OffsetSortedKey fromKey = new OffsetSortedKey(startOffset,0);
            OffsetSortedKey toKey = new OffsetSortedKey(endOffset,0);
            SortedMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> map = declarations.subMap(fromKey, toKey);
            for(Map.Entry<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> entry : map.entrySet()){
                CsmUID<CsmOffsetableDeclaration> anUid = entry.getValue();
                int start = UIDUtilities.getStartOffset(anUid);
                int end = UIDUtilities.getEndOffset(anUid);
                if (start >= endOffset) {
                    break;
                }
                if(end >= startOffset && start < endOffset) {
                    res.add(anUid);
                }
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        return res;
    }

    Iterator<CsmOffsetableDeclaration> getDeclarations(int offset) {
        List<CsmUID<CsmOffsetableDeclaration>> res;
        try {
            declarationsLock.readLock().lock();
            res = getDeclarationsByOffset(offset);
        } finally {
            declarationsLock.readLock().unlock();
        }
        return UIDCsmConverter.UIDsToDeclarations(res).iterator();
    }

    // call under read lock
    private List<CsmUID<CsmOffsetableDeclaration>> getDeclarationsByOffset(int offset){
        List<CsmUID<CsmOffsetableDeclaration>> res = new ArrayList<CsmUID<CsmOffsetableDeclaration>>();
        OffsetSortedKey key = new OffsetSortedKey(offset+1,0); // NOI18N
        while(true) {
            SortedMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> head = declarations.headMap(key);
            if (head.isEmpty()) {
                break;
            }
            OffsetSortedKey last = head.lastKey();
            if (last == null) {
                break;
            }
            CsmUID<CsmOffsetableDeclaration> aUid = declarations.get(last);
            int from = UIDUtilities.getStartOffset(aUid);
            int to = UIDUtilities.getEndOffset(aUid);
            if (from <= offset && offset <= to) {
                res.add(0, aUid);
                key = last;
            } else {
                break;
            }
        }
        return res;
    }

    private OffsetSortedKey getOffsetSortKey(CsmOffsetableDeclaration declaration) {
        return new OffsetSortedKey(declaration);
    }

    CsmUID<CsmOffsetableDeclaration> addDeclaration(CsmOffsetableDeclaration decl) {
        CsmUID<CsmOffsetableDeclaration> uidDecl = RepositoryUtils.put(decl);
        try {
            declarationsLock.writeLock().lock();
            declarations.put(getOffsetSortKey(decl), uidDecl);
            sortedDeclarations = null;
        } finally {
            declarationsLock.writeLock().unlock();
        }
        put();
        return uidDecl;
    }

    void removeDeclaration(CsmOffsetableDeclaration declaration) {
        CsmUID<CsmOffsetableDeclaration> uidDecl;
        try {
            declarationsLock.writeLock().lock();
            uidDecl = declarations.remove(getOffsetSortKey(declaration));
            sortedDeclarations = null;
        } finally {
            declarationsLock.writeLock().unlock();
        }
        RepositoryUtils.remove(uidDecl, declaration);
        // update repository
        put();
    }

    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        try {
            declarationsLock.readLock().lock();
            factory.writeOffsetSortedToUIDMap(this.declarations, output, false);
        } finally {
            declarationsLock.readLock().unlock();
        }
    }

    public static final class OffsetSortedKey implements Comparable<OffsetSortedKey>, Persistent, SelfPersistent {

        private final int start;
        private final int name;

        public OffsetSortedKey(CsmOffsetableDeclaration declaration) {
            start = ((CsmOffsetable) declaration).getStartOffset();
            name = Math.abs(declaration.getName().hashCode());
        }

        public OffsetSortedKey(int offset, int name) {
            start = offset;
            this.name = name;
        }

        @Override
        public int compareTo(OffsetSortedKey o) {
            int res = start - o.start;
            if (res == 0) {
                res = name - o.name;
            }
            return res;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof OffsetSortedKey) {
                OffsetSortedKey key = (OffsetSortedKey) obj;
                return compareTo(key)==0;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + this.start;
            hash = 37 * hash + this.name;
            return hash;
        }

        @Override
        public String toString() {
            return "OffsetSortedKey: " + this.name + "[" + this.start; // NOI18N
        }

        @Override
        public void write(DataOutput output) throws IOException {
            output.writeInt(start);
            output.writeInt(name);
        }

        public OffsetSortedKey(DataInput input) throws IOException {
            start = input.readInt();
            name = input.readInt();
        }
    }

    public static class NameKey implements Comparable<NameKey> {
        private int start = 0;
        private CharSequence name;
        public NameKey(CsmUID<CsmOffsetableDeclaration> anUid) {
            name = UIDUtilities.getName(anUid);
            start = UIDUtilities.getStartOffset(anUid);
        }

        public NameKey(CharSequence name, int offset) {
            this.name = name;
            start = offset;
        }

        @Override
        public int compareTo(NameKey o) {
            int res = CharSequences.comparator().compare(name, o.name);
            if (res == 0) {
                res = start - o.start;
            }
            return res;
        }
    }
}
