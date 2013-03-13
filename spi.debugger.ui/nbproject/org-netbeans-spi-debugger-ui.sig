#Signature file v4.1
#Version 2.37.1

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public abstract interface java.lang.annotation.Annotation
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.lang.Class<? extends java.lang.annotation.Annotation> annotationType()
meth public abstract java.lang.String toString()

CLSS public abstract interface !annotation java.lang.annotation.Documented
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation java.lang.annotation.Retention
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.RetentionPolicy value()

CLSS public abstract interface !annotation java.lang.annotation.Target
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.ElementType[] value()

CLSS public abstract org.netbeans.spi.debugger.ui.AttachType
cons public init()
innr public abstract interface static !annotation Registration
meth public abstract javax.swing.JComponent getCustomizer()
meth public java.lang.String getTypeDisplayName()
meth public org.netbeans.spi.debugger.ui.Controller getController()
supr java.lang.Object
hcls ContextAware

CLSS public abstract interface static !annotation org.netbeans.spi.debugger.ui.AttachType$Registration
 outer org.netbeans.spi.debugger.ui.AttachType
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract java.lang.String displayName()

CLSS public abstract org.netbeans.spi.debugger.ui.BreakpointAnnotation
cons public init()
meth public abstract org.netbeans.api.debugger.Breakpoint getBreakpoint()
supr org.openide.text.Annotation

CLSS public abstract org.netbeans.spi.debugger.ui.BreakpointType
cons public init()
innr public abstract interface static !annotation Registration
meth public abstract boolean isDefault()
meth public abstract java.lang.String getCategoryDisplayName()
meth public abstract javax.swing.JComponent getCustomizer()
meth public java.lang.String getTypeDisplayName()
meth public org.netbeans.spi.debugger.ui.Controller getController()
supr java.lang.Object
hcls ContextAware

CLSS public abstract interface static !annotation org.netbeans.spi.debugger.ui.BreakpointType$Registration
 outer org.netbeans.spi.debugger.ui.BreakpointType
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract !hasdefault java.lang.String path()
meth public abstract java.lang.String displayName()

CLSS public abstract interface !annotation org.netbeans.spi.debugger.ui.ColumnModelRegistration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract java.lang.String path()

CLSS public abstract interface !annotation org.netbeans.spi.debugger.ui.ColumnModelRegistrations
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract org.netbeans.spi.debugger.ui.ColumnModelRegistration[] value()

CLSS public abstract interface org.netbeans.spi.debugger.ui.Constants
fld public final static java.lang.String BREAKPOINT_ENABLED_COLUMN_ID = "BreakpointEnabled"
fld public final static java.lang.String CALL_STACK_FRAME_LOCATION_COLUMN_ID = "CallStackFrameLocation"
fld public final static java.lang.String LOCALS_TO_STRING_COLUMN_ID = "LocalsToString"
fld public final static java.lang.String LOCALS_TYPE_COLUMN_ID = "LocalsType"
fld public final static java.lang.String LOCALS_VALUE_COLUMN_ID = "LocalsValue"
fld public final static java.lang.String SESSION_HOST_NAME_COLUMN_ID = "SessionHostName"
fld public final static java.lang.String SESSION_LANGUAGE_COLUMN_ID = "SessionLanguage"
fld public final static java.lang.String SESSION_STATE_COLUMN_ID = "SessionState"
fld public final static java.lang.String THREAD_STATE_COLUMN_ID = "ThreadState"
fld public final static java.lang.String THREAD_SUSPENDED_COLUMN_ID = "ThreadSuspended"
fld public final static java.lang.String WATCH_TO_STRING_COLUMN_ID = "WatchToString"
fld public final static java.lang.String WATCH_TYPE_COLUMN_ID = "WatchType"
fld public final static java.lang.String WATCH_VALUE_COLUMN_ID = "WatchValue"

CLSS public abstract interface org.netbeans.spi.debugger.ui.Controller
fld public final static java.lang.String PROP_VALID = "valid"
meth public abstract boolean cancel()
meth public abstract boolean isValid()
meth public abstract boolean ok()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public final org.netbeans.spi.debugger.ui.EditorContextDispatcher
fld public final static java.lang.String PROP_EDITOR = "editor"
fld public final static java.lang.String PROP_FILE = "file"
meth public int getCurrentLineNumber()
meth public int getMostRecentLineNumber()
meth public java.lang.String getCurrentURLAsString()
meth public java.lang.String getMostRecentURLAsString()
meth public javax.swing.JEditorPane getCurrentEditor()
meth public javax.swing.JEditorPane getMostRecentEditor()
meth public org.openide.filesystems.FileObject getCurrentFile()
meth public org.openide.filesystems.FileObject getMostRecentFile()
meth public org.openide.text.Line getCurrentLine()
meth public org.openide.text.Line getMostRecentLine()
meth public static org.netbeans.spi.debugger.ui.EditorContextDispatcher getDefault()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds NO_FILE,NO_FILE_CHANGE,NO_TEXT_COMPONENT,ccrp,context,currentFile,currentFileChangeListener,currentFileChangeListenerWeak,currentTextComponent,currentURL,erListener,lastFiredMIMEType,lastMIMETypeEvents,logger,lookupCoalescedChange,mostRecentFileChangeListener,mostRecentFileChangeListenerWeak,mostRecentFileRef,pcs,pcsByMIMEType,refreshProcessor,resFileObject
hcls AddRemoveFileListenerInEQThread,CoalescedChange,EditorLookupListener,EditorRegistryListener,EventFirer,FileRenameListener

CLSS public abstract interface org.netbeans.spi.debugger.ui.EngineComponentsProvider
innr public abstract interface static ComponentProvider
innr public final static ComponentInfo
innr public static TopComponentProvider
meth public abstract java.util.List<org.netbeans.spi.debugger.ui.EngineComponentsProvider$ComponentInfo> getComponents()
meth public abstract void willCloseNotify(java.util.List<org.netbeans.spi.debugger.ui.EngineComponentsProvider$ComponentInfo>)

CLSS public final static org.netbeans.spi.debugger.ui.EngineComponentsProvider$ComponentInfo
 outer org.netbeans.spi.debugger.ui.EngineComponentsProvider
meth public boolean isMinimized()
meth public boolean isOpened()
meth public java.awt.Component getComponent()
meth public java.lang.String toString()
meth public static org.netbeans.spi.debugger.ui.EngineComponentsProvider$ComponentInfo create(java.lang.String)
meth public static org.netbeans.spi.debugger.ui.EngineComponentsProvider$ComponentInfo create(java.lang.String,boolean)
meth public static org.netbeans.spi.debugger.ui.EngineComponentsProvider$ComponentInfo create(java.lang.String,boolean,boolean)
supr java.lang.Object
hfds minimized,opened,provider

CLSS public abstract interface static org.netbeans.spi.debugger.ui.EngineComponentsProvider$ComponentProvider
 outer org.netbeans.spi.debugger.ui.EngineComponentsProvider
meth public abstract java.awt.Component getComponent()

CLSS public static org.netbeans.spi.debugger.ui.EngineComponentsProvider$TopComponentProvider
 outer org.netbeans.spi.debugger.ui.EngineComponentsProvider
intf org.netbeans.spi.debugger.ui.EngineComponentsProvider$ComponentProvider
meth public java.awt.Component getComponent()
meth public java.lang.String toString()
supr java.lang.Object
hfds tcId

CLSS public org.netbeans.spi.debugger.ui.MethodChooser
cons public init(java.lang.String,org.netbeans.spi.debugger.ui.MethodChooser$Segment[],int)
cons public init(java.lang.String,org.netbeans.spi.debugger.ui.MethodChooser$Segment[],int,java.lang.String,javax.swing.KeyStroke[],javax.swing.KeyStroke[])
innr public abstract interface static ReleaseListener
innr public static Segment
meth public boolean isUIActive()
meth public boolean showUI()
meth public int getSelectedIndex()
meth public static org.netbeans.spi.editor.highlighting.HighlightsLayerFactory createHighlihgtsLayerFactory()
meth public void addReleaseListener(org.netbeans.spi.debugger.ui.MethodChooser$ReleaseListener)
meth public void releaseUI(boolean)
meth public void removeReleaseListener(org.netbeans.spi.debugger.ui.MethodChooser$ReleaseListener)
supr java.lang.Object
hfds arrowCursor,attribsAll,attribsAllUnc,attribsArea,attribsHyperlink,attribsLeft,attribsLeftUnc,attribsMethod,attribsMethodUnc,attribsMiddle,attribsMiddleUnc,attribsRight,attribsRightUnc,confirmEvents,defaultHyperlinkHighlight,doc,editorPane,endLine,handCursor,hintText,isInSelectMode,mainListener,mousedIndex,originalCursor,releaseListeners,segments,selectedIndex,startLine,stopEvents,url
hcls CentralListener,MethodChooserHighlightsLayerFactory,TooltipResolver

CLSS public abstract interface static org.netbeans.spi.debugger.ui.MethodChooser$ReleaseListener
 outer org.netbeans.spi.debugger.ui.MethodChooser
meth public abstract void released(boolean)

CLSS public static org.netbeans.spi.debugger.ui.MethodChooser$Segment
 outer org.netbeans.spi.debugger.ui.MethodChooser
cons public init(int,int)
meth public int getEndOffset()
meth public int getStartOffset()
supr java.lang.Object
hfds endOffset,startOffset

CLSS public org.netbeans.spi.debugger.ui.ViewFactory
meth public javax.swing.JComponent createViewComponent(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public org.netbeans.spi.debugger.ui.ViewLifecycle createViewLifecycle(java.lang.String,java.lang.String)
meth public org.openide.windows.TopComponent createViewTC(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public static org.netbeans.spi.debugger.ui.ViewFactory getDefault()
supr java.lang.Object
hfds vf

CLSS public final org.netbeans.spi.debugger.ui.ViewLifecycle
innr public abstract interface static ModelUpdateListener
meth public org.netbeans.spi.viewmodel.Models$CompoundModel getModel()
meth public void addModelUpdateListener(org.netbeans.spi.debugger.ui.ViewLifecycle$ModelUpdateListener)
meth public void destroy()
meth public void removeModelUpdateListener(org.netbeans.spi.debugger.ui.ViewLifecycle$ModelUpdateListener)
supr java.lang.Object
hfds cmul,vml
hcls CompoundModelUpdateListener

CLSS public abstract interface static org.netbeans.spi.debugger.ui.ViewLifecycle$ModelUpdateListener
 outer org.netbeans.spi.debugger.ui.ViewLifecycle
meth public abstract void modelUpdated(org.netbeans.spi.viewmodel.Models$CompoundModel,org.netbeans.api.debugger.DebuggerEngine)

CLSS public abstract org.openide.text.Annotation
cons public init()
fld public final static java.lang.String PROP_ANNOTATION_TYPE = "annotationType"
fld public final static java.lang.String PROP_MOVE_TO_FRONT = "moveToFront"
fld public final static java.lang.String PROP_SHORT_DESCRIPTION = "shortDescription"
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void notifyAttached(org.openide.text.Annotatable)
meth protected void notifyDetached(org.openide.text.Annotatable)
meth public abstract java.lang.String getAnnotationType()
meth public abstract java.lang.String getShortDescription()
meth public final org.openide.text.Annotatable getAttachedAnnotatable()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void attach(org.openide.text.Annotatable)
meth public final void detach()
meth public final void moveToFront()
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds attached,inDocument,support

