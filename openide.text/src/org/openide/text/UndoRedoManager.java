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

package org.openide.text;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.StyledDocument;
import javax.swing.undo.*;
import org.openide.awt.UndoRedo;


/**
 * An improved version of UndoRedo manager that locks document before
 * doing any other operations.
 * <br/>
 * It supports grouping of undoable edits by extending UndoGroupManager.
 * <br/>
 * It supports save actions that produce a compound undoable edit.
 * <br/>
 * 
 * <p>
 * Following requirements should be met:
 * <ul>
 *   <li>When saving document extra save actions are performed producing compound saveActionsEdit.</li>
 *   <li>When undoing just performed save the saveActionsEdit should be undone at once with the last
 *     performed edit.</li>
 *   <li>When save of the document was just performed a next edit must not be merged with last edit
 *     so that the savepoint can be retained.</li>
 *   <li>When save occurred after last edit the save actions are coalesced together with last edit.</li>
 *   <li>When save occurred inside UM.edits the extra save actions are kept separately and undone
 *     when "coming out of the savepoint" and redone when "coming to a savepoint".</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <tt>Undo Grouping</tt> allows explicit control of what
 * <tt>UndoableEdit</tt>s are coalesced into compound edits, rather than using
 * the rules defined by the edits themselves. Groups are defined using
 * BEGIN_COMMIT_GROUP and END_COMMIT_GROUP. Send these to UndoableEditListener.
 * These must always be paired. <p> These use cases are supported. </p> <ol>
 * <li> Default behavior is defined by {@link UndoManager}.</li> <li>
 * <tt>UnddoableEdit</tt>s issued between {@link #BEGIN_COMMIT_GROUP} and {@link #END_COMMIT_GROUP}
 * are placed into a single
 * {@link CompoundEdit}. Thus <tt>undo()</tt> and <tt>redo()</tt> treat them as
 * a single undo/redo.</li> <li>BEGIN/END nest.</li> <li> Issue
 * MARK_COMMIT_GROUP to commit accumulated <tt>UndoableEdit</tt>s into a single
 * <tt>CompoundEdit</tt> and to continue accumulating; an application could do
 * this at strategic points, such as EndOfLine input or cursor movement.</li>
 * </ol>
 * </p>
 * 
 * @author Miloslav Metelka
 * @author Jaroslav Tulach
 * @author Ernie Rael
 */
final class UndoRedoManager extends UndoRedo.Manager {
    
    // -J-Dorg.openide.text.UndoRedoManager.level=FINE
    private static final Logger LOG = Logger.getLogger(UndoRedoManager.class.getName());

    /**
     * Marker edit for the state when undo manager is right at the savepoint.
     * <br/>
     * Next performed edit will set afterSaveEdit field.
     */
    static final UndoableEdit SAVEPOINT = new SpecialEdit();

    /**
     * Start a group of edits which will be committed as a single edit
     * for purpose of undo/redo.
     * Nesting semantics are that any BEGIN_COMMIT_GROUP and
     * END_COMMIT_GROUP delimits a commit-group, unless the group is
     * empty in which case the begin/end is ignored.
     * While coalescing edits, any undo/redo/save implicitly delimits
     * a commit-group.
     */
    static final UndoableEdit BEGIN_COMMIT_GROUP = new SpecialEdit();

    /** End a group of edits. */
    static final UndoableEdit END_COMMIT_GROUP = new SpecialEdit();
    
    /**
     * Any coalesced edits become a commit-group and a new commit-group
     * is started.
     */
    static final UndoableEdit MARK_COMMIT_GROUP = new SpecialEdit();


    CloneableEditorSupport support;
    
    /**
     * Undo edit which brings the undo manager into savepoint.
     * <br/>
     * The field may be set to SAVEPOINT special value in case the undo manager
     * is set right to the savepoint.
     * <br/>
     * Value of beforeSavepoint field defines whether savepointEdit will be undone
     * to enter the savepoint (beforeSavepoint==false) or redone to enter the savepoint
     * (beforeSavepoint==true).
     * <br/>
     * Subsequent addEdit(), undo() and redo() operations will modify
     * the field to point to neighbor edit of the save point.
     */
    UndoableEdit savepointEdit;
    
    /**
     * Whether undo manager's undo()/redo() operations currently operate before/after
     * the savepoint (when right at savepoint the value is undefined).
     */
    boolean beforeSavepoint;
    
    /**
     * Undoable edit created as result of running save actions by using
     * "beforeSaveRunnable" document property (See CloneableEditorSupport).
     * <br/>
     * If saving occurs right at UM.edits end (i.e. either UM.addEdit() was just performed
     * or lastAddedEdit.redo() was performed) then the save actions edit
     * is merged with the last added edit.
     * <br/>
     * If UM.edits is empty (it could possibly happen when document is not saved
     * and discardAllEdits() was called for some reason) then the save actions edit
     * is not added to UM.edits (just edit.die() is called and the effect
     * of the save actions cannot be undone/redone).
     * <br/>
     * When the save actions edit is done in any other situation then it stands beside
     * regular undo manager structures and is handled specially. It is:
     * <ul>
     *   <li>undone when the UM is at savepoint and UM.undo() or UM.redo() is done.</li>
     *   <li>redone when edit right before savepoint is redone.</li>
     *   <li>redone when edit right after savepoint is undone.</li>
     * </ul>
     */
    private CompoundEdit saveActionsEdit;
    
    /**
     * Set to true when the CES gave control to save actions that may produce
     * undoable edits which need to be coalesced into a special compound edit.
     */
    private boolean performingSaveActions;
    
    /**
     * Flag to check whether support.notifyUnmodified() should be called
     * - it's necessary to do it outside of atomicLock acquired by DocLockedRun.
     */
    private boolean callNotifyUnmodified;
    
    /**
     * Signals that edits are being accumulated. When a nested group is added
     * the existing group must be ended and sent to UM unless the inner group is empty
     * (or contains empty groups).
     */
    private int buildUndoGroup;

    /**
     * Accumulate edits here in undoGroup. It may be null if no "real" edits were
     * added.
     */
    private CompoundEdit undoGroup;

    /**
     * Signal that nested group started and that current undo group
     * must be committed if edit is added. Then can avoid doing the commit
     * if the nested group turns out to be empty.
     */
    private int needsNestingCommit;


    public UndoRedoManager(CloneableEditorSupport support) {
        this.support = support;
        super.setLimit(1000);
    }
    
    void setPerformingSaveActions(boolean performingSaveActions) {
        commitUndoGroup(); // setPerformingSaveActions() won't be called unless save actions produce Runnable
        if (performingSaveActions != this.performingSaveActions) {
            this.performingSaveActions = performingSaveActions;
            if (performingSaveActions) {
                clearSaveActionsEdit();
                saveActionsEdit = new CompoundEdit();
                checkLogOp("    NEW-saveActionsEdit", saveActionsEdit); // NOI18N
            } else { // Stop performing save actions
                saveActionsEdit.end();
                checkLogOp("    COMPLETED-saveActionsEdit", saveActionsEdit); // NOI18N
            }
        }
    }
    
    void markSavepoint() {
        commitUndoGroup(); // setPerformingSaveActions() won't be called unless save actions produce Runnable
        savepointEdit = SAVEPOINT;
    }
    
    boolean isAtSavepoint() {
        return (savepointEdit == SAVEPOINT);
    }
    
    private void markSavepointAndUnmodified() {
        markSavepoint();
        callNotifyUnmodified = true;
    }
    
    private void checkCallNotifyUnmodified() {
        if (callNotifyUnmodified) {
            callNotifyUnmodified = false;
            if (support.isAlreadyModified()) {
                support.callNotifyUnmodified();
            }
        }
    }

    void mergeSaveActionsToLastEdit(WrapUndoEdit lastWrapEdit) {
        if (saveActionsEdit != null) {
            checkLogOp("    mergeSaveActionsToLastEdit-lastWrapEdit", lastWrapEdit); // NOI18N
            StableCompoundEdit compoundEdit = new StableCompoundEdit();
            compoundEdit.addEdit(lastWrapEdit.delegate());
            compoundEdit.addEdit(saveActionsEdit);
            compoundEdit.end();
            lastWrapEdit.setDelegate(compoundEdit);
            saveActionsEdit = null;
            checkLogOp("    compoundEdit", compoundEdit); // NOI18N
            // Note that there may be no edits present in UM.edits (e.g.
            // when discardAllEdits() was called). If the savepoint
            // is at index==0 in UM.edits then the saveActionsEdit cannot be merged
            // to any previous edit (there is no such one) and this method will not be called.
            // This case is handled specially in addEditImpl().
        }
    }

    void beforeUndoAtSavepoint(WrapUndoEdit edit) {
        checkLogOp("beforeUndoAtSavepoint", edit); // NOI18N
        undoSaveActions();
    }
        
    private void undoSaveActions() {
        if (saveActionsEdit != null) {
            checkLogOp("    saveActionsEdit.undo()", saveActionsEdit); // NOI18N
            saveActionsEdit.undo();
        }
    }

    void delegateUndoFailedAtSavepoint(WrapUndoEdit edit) {
        checkLogOp("delegateUndoFailedAtSavepoint", edit); // NOI18N
        redoSaveActions();
    }
        
    private void redoSaveActions() {   
        if (saveActionsEdit != null) {
            checkLogOp("    saveActionsEdit.redo()", saveActionsEdit); // NOI18N
            saveActionsEdit.redo();
        }
    }

    void afterUndoCheck(WrapUndoEdit edit) {
        if (isAtSavepoint()) { // Undoing edit right before savepoint.
            checkLogOp("afterUndoCheck-atSavepoint", edit); // NOI18N
            // saveActionsEdit already processed by checkSavepointBeforeUndo()
            beforeSavepoint = true;
            savepointEdit = edit;

        } else if (savepointEdit == edit) { // Undone to savepoint
            if (saveActionsEdit != null) {
                checkLogOp("    saveActionsEdit.redo()", saveActionsEdit); // NOI18N
                saveActionsEdit.redo();
            }
            checkLogOp("afterUndoCheck-becomesSavepoint-markUnmodified", edit); // NOI18N
            assert (!beforeSavepoint) : "Expected to be behind savepoint"; // NOI18N
            markSavepointAndUnmodified();
        }
    }
    
    void beforeRedoAtSavepoint(WrapUndoEdit edit) {
        checkLogOp("beforeRedoAtSavepoint", edit); // NOI18N
        undoSaveActions();
    }

    void delegateRedoFailedAtSavepoint(WrapUndoEdit edit) {
        checkLogOp("delegateRedoFailedAtSavepoint", edit); // NOI18N
        redoSaveActions();
    }
        
    void afterRedoCheck(WrapUndoEdit edit) {
        if (isAtSavepoint()) { // Redoing edit right before savepoint.
            checkLogOp("afterRedoCheck-atSavepoint", edit); // NOI18N
            // saveActionsEdit already processed by checkSavepointBeforeUndo()
            beforeSavepoint = false;
            savepointEdit = edit;

        } else if (savepointEdit == edit) { // Redone to savepoint
            if (saveActionsEdit != null) {
                checkLogOp("    saveActionsEdit.redo()", saveActionsEdit); // NOI18N
                saveActionsEdit.redo();
            }
            checkLogOp("afterRedoCheck-becomesSavepoint", edit); // NOI18N
            assert (beforeSavepoint) : "Expected to be before savepoint"; // NOI18N
            markSavepointAndUnmodified();
        }
    }
    
    void checkReplaceSavepointEdit(WrapUndoEdit origEdit, WrapUndoEdit newEdit) {
        if (savepointEdit == origEdit) {
            checkLogOp("checkReplaceSavepointEdit-replacedSavepointEdit", origEdit); // NOI18N
            savepointEdit = newEdit;
        }
    }
    
    void notifyWrapEditDie(UndoableEdit edit) {
        if (edit == savepointEdit) { // Savepoint neighbour died => no longer a savepoint
            checkLogOp("notifyWrapEditDie-savepoint-die", edit); // NOI18N
            savepointEdit = null;
            clearSaveActionsEdit();
        }
    }
    
    @Override
    public synchronized boolean addEdit(UndoableEdit edit) {
        if (!isInProgress()) {
            return false;
        }
        if (edit == BEGIN_COMMIT_GROUP) {
            beginUndoGroup();
            return true;
        } else if (edit == END_COMMIT_GROUP) {
            endUndoGroup();
            return true;
        } else if (edit == MARK_COMMIT_GROUP) {
            commitUndoGroup();
            return true;
        }
        
        if (needsNestingCommit > 0) {
            commitUndoGroup();
        }

        if (buildUndoGroup > 0) {
            if (undoGroup == null) {
                undoGroup = new CompoundEdit();
            }
            return undoGroup.addEdit(edit);
        }

        return addEditImpl(edit);
    }
    
    private boolean addEditImpl(UndoableEdit edit) {
        // This should already be called under document's lock so DocLockedRun not necessary
        assert (edit != null) : "Cannot add null edit"; // NOI18N
        if (performingSaveActions) {
            checkLogOp("addEdit-performingSaveActions", edit); // NOI18N
            boolean added = saveActionsEdit.addEdit(edit);
            if (!added) {
                throw new IllegalStateException("Cannot add to saveActionsEdit"); // NOI18N
            }
            return added;
        }
        WrapUndoEdit wrapEdit = new WrapUndoEdit(this, edit); // Wrap the edit
        boolean added = super.addEdit(wrapEdit);
        if (isAtSavepoint()) {
            checkLogOp("addEdit-atSavepoint", wrapEdit); // NOI18N
            beforeSavepoint = false;
            savepointEdit = wrapEdit;
            // In case UM.edits was empty before this addition and there are valid
            // save actions then these have to be dropped since they were not merged
            // with previous edit but the logic here would attempt to redo them
            // upon undo of just added edit which would be wrong.
            if (added && edits.size() == 1) {
                clearSaveActionsEdit();
            }
        } else {
            checkLogOp("addEdit", wrapEdit); // NOI18N
        }
        return added;
    }
    
    // replaceEdit() not overriden - it should return false

    @Override
    public void redo() throws javax.swing.undo.CannotRedoException {
        final StyledDocument doc = support.getDocument();
        if (doc == null) {
            throw new javax.swing.undo.CannotRedoException(); // NOI18N
        }
        new DocLockedRun(0, doc);
        checkCallNotifyUnmodified();
    }

    @Override
    public void undo() throws javax.swing.undo.CannotUndoException {
        final StyledDocument doc = support.getDocument();
        if (doc == null) {
            throw new javax.swing.undo.CannotUndoException(); // NOI18N
        }
        new DocLockedRun(1, doc);
        checkCallNotifyUnmodified();
    }

    @Override
    public boolean canRedo() {
        return new DocLockedRun(2, support.getDocument(), 0, true).booleanResult;
    }

    @Override
    public boolean canUndo() {
        return new DocLockedRun(3, support.getDocument(), 0, true).booleanResult;
    }

    @Override
    public int getLimit() {
        return new DocLockedRun(4, support.getDocument()).intResult;
    }

    @Override
    public void discardAllEdits() {
        new DocLockedRun(5, support.getDocument(), 0, true);
    }
    
    private void clearSaveActionsEdit() {
        if (saveActionsEdit != null) {
            checkLogOp("    saveActionsEdit-die", saveActionsEdit); // NOI18N
            saveActionsEdit.die();
            saveActionsEdit = null;
        }
    }

    @Override
    public void setLimit(int l) {
        new DocLockedRun(6, support.getDocument(), l);
    }

    @Override
    public boolean canUndoOrRedo() {
        return new DocLockedRun(7, support.getDocument(), 0, true).booleanResult;
    }

    @Override
    public java.lang.String getUndoOrRedoPresentationName() {
        if (support.isDocumentReady()) {
            return new DocLockedRun(8, support.getDocument(), 0, true).stringResult;
        } else {
            return "";
        }
    }

    @Override
    public java.lang.String getRedoPresentationName() {
        if (support.isDocumentReady()) {
            return new DocLockedRun(9, support.getDocument(), 0, true).stringResult;
        } else {
            return "";
        }
    }

    @Override
    public java.lang.String getUndoPresentationName() {
        if (support.isDocumentReady()) {
            return new DocLockedRun(10, support.getDocument(), 0, true).stringResult;
        } else {
            return "";
        }
    }

    @Override
    public void undoOrRedo() throws javax.swing.undo.CannotUndoException, javax.swing.undo.CannotRedoException {
        super.undoOrRedo();
    }
    
    /**
     * Begin coalescing <tt>UndoableEdit</tt>s that are added into a <tt>CompoundEdit</tt>.
     * <p>If edits are already being coalesced and some have been 
     * accumulated, they are flagged for commitment as an atomic group and
     * a new group will be started.
     * @see #addEdit
     * @see #endUndoGroup
     */
    private void beginUndoGroup() {
        if(undoGroup != null)
            needsNestingCommit++;
        LOG.log(Level.FINE, "beginUndoGroup: nesting {0}", buildUndoGroup); // NOI18N
        buildUndoGroup++;
    }

    /**
     * Stop coalescing edits. Until <tt>beginUndoGroupManager</tt> is invoked,
     * any received <tt>UndoableEdit</tt>s are added singly.
     * <p>
     * This has no effect if edits are not being coalesced, for example
     * if <tt>beginUndoGroup</tt> has not been called.
     */
    private void endUndoGroup() {
        buildUndoGroup--;
        LOG.log(Level.FINE, "endUndoGroup: nesting {0}", buildUndoGroup); // NOI18N
        if(buildUndoGroup < 0) {
            LOG.log(Level.INFO, null, new Exception("endUndoGroup without beginUndoGroup")); // NOI18N
            // slam buildUndoGroup to 0 to disable nesting
            buildUndoGroup = 0;
        }
        if(needsNestingCommit <= 0)
            commitUndoGroup();
        if(--needsNestingCommit < 0)
            needsNestingCommit = 0;
    }

    /**
     * Commit any accumulated <tt>UndoableEdit</tt>s as an atomic
     * <tt>undo</tt>/<tt>redo</tt> group. {@link CompoundEdit#end}
     * is invoked on the <tt>CompoundEdit</tt> and it is added as a single
     * <tt>UndoableEdit</tt> to this <tt>UndoManager</tt>.
     * <p>
     * If edits are currently being coalesced, a new undo group is started.
     * This has no effect if edits are not being coalesced, for example
     * <tt>beginUndoGroup</tt> has not been called.
     */
    private void commitUndoGroup() {
        if(undoGroup == null) {
            return;
        }

        // undoGroup is being set to null,
        // needsNestingCommit has no meaning now
        needsNestingCommit = 0;
        undoGroup.end();
        addEditImpl(undoGroup);
        undoGroup = null;
    }

    UndoableEdit editToBeUndoneRedone(boolean redone) { // Access for NbDocument
        WrapUndoEdit wrapEdit = (WrapUndoEdit) (redone ? editToBeRedone() : editToBeUndone());
        return (wrapEdit != null) ? wrapEdit.delegate() : null;
    }

    static String editToString(UndoableEdit edit) {
        if (edit instanceof WrapUndoEdit) {
            return toStringTerse(edit) + "->" + toStringTerse(((WrapUndoEdit)edit).delegate()); // NOI18N
        } else {
            return toStringTerse(edit);
        }
    }
    
    static String toStringTerse(Object o) {
        if (o != null) {
            String clsName = o.getClass().getName();
            return clsName.substring(clsName.lastIndexOf('.') + 1) + "@" + System.identityHashCode(o); // NOI18N
        } else {
            return "null"; // NOI18N
        }
    }
    
    void checkLogOp(String op, UndoableEdit edit) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(thisToString() + "->" + op + ": " + editToString(edit) + '\n'); // NOI18N
        }
    }
    
    String thisToString() {
        String name = support.messageName();
        return String.valueOf(name) + ":URM@" + System.identityHashCode(this); // NOI18N
    }

    private final class DocLockedRun implements Runnable {
        
        private final int type;
        
        boolean booleanResult;
        
        int intResult;
        
        String stringResult;

        public DocLockedRun(int type, StyledDocument doc) {
            this(type, doc, 0);
        }

        public DocLockedRun(int type, StyledDocument doc, int intValue) {
            this(type, doc, intValue, false);
        }

        public DocLockedRun(int type, StyledDocument doc, int intValue, boolean readLock) {
            this.type = type;
            this.intResult = intValue;

            if (!readLock && (doc instanceof NbDocument.WriteLockable)) {
                ((NbDocument.WriteLockable) doc).runAtomic(this);
            } else {
                if (readLock && doc != null) {
                    doc.render(this);
                } else {
                    // if the document is not one of "NetBeans ready"
                    // that supports locking we do not have many
                    // chances to do something. Maybe check for AbstractDocument
                    // and call writeLock using reflection, but better than
                    // that, let's leave this simple for now and wait for
                    // bug reports (if any appear)
                    run();
                }
            }
        }

        public void run() {
            switch (type) {
            case 0:
                if (undoGroup != null) {
                    throw new CannotRedoException();
                }
                UndoRedoManager.super.redo();
                break;

            case 1:
                commitUndoGroup();
                UndoRedoManager.super.undo();
                break;

            case 2:
                booleanResult = (undoGroup != null) ? false : UndoRedoManager.super.canRedo();
                break;

            case 3:
                booleanResult = (undoGroup != null) ? true : UndoRedoManager.super.canUndo();
                break;

            case 4:
                intResult = UndoRedoManager.super.getLimit();
                break;

            case 5:
                if (LOG.isLoggable(Level.FINE)) {
                    int editsSize = (edits != null) ? edits.size() : 0;
                    LOG.fine("discardAllEdits(): savepoint=" + isAtSavepoint() + // NOI18N
                            ", editsSize=" + editsSize + "\n"); // NOI18N
                }
                commitUndoGroup();
                clearSaveActionsEdit();
                UndoRedoManager.super.discardAllEdits();
                break;

            case 6:
                UndoRedoManager.super.setLimit(intResult);
                break;

            case 7:
                UndoRedoManager.super.canUndoOrRedo();
                break;

            case 8:
                stringResult = UndoRedoManager.super.getUndoOrRedoPresentationName();
                break;

            case 9:
                stringResult = (undoGroup != null)
                        ? undoGroup.getRedoPresentationName()
                        : UndoRedoManager.super.getRedoPresentationName();
                break;

            case 10:
                stringResult = (undoGroup != null)
                        ? undoGroup.getUndoPresentationName()
                        : UndoRedoManager.super.getUndoPresentationName();
                break;

            case 11:
                UndoRedoManager.super.undoOrRedo();
                break;

            default:
                throw new IllegalArgumentException("Unknown type: " + type);
            }
        }
    }

    private static class SpecialEdit extends CompoundEdit {

        public SpecialEdit()
        {
            super();
            // Prevent the special edits to merge with any other edits.
            // This might happen in an errorneous situation when
            // e.g. two undo managers are attached to the same document at once.
            end();
        }

        @Override
        public boolean canRedo()
        {
            return true;
        }

        @Override
        public boolean canUndo()
        {
            return true;
        }
    }

}
