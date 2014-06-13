/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.ExecutionContext;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.instrument.PhylumTag;
import com.oracle.truffle.api.instrument.Probe;
import com.oracle.truffle.api.instrument.ProbeListener;
import com.oracle.truffle.api.instrument.Visualizer;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.debug.DebugClient;
import com.oracle.truffle.debug.DebugManager;
import com.oracle.truffle.debug.impl.AbstractDebugManager;
import com.oracle.truffle.debug.instrument.DebugInstrumentCallback;
import com.oracle.truffle.debug.instrument.DebugLineInstrument;
import com.oracle.truffle.js.engine.TruffleJSEngine;
import com.oracle.truffle.js.engine.TruffleJSEngineFactory;
import com.oracle.truffle.js.nodes.JavaScriptNode;
import com.oracle.truffle.js.nodes.ScriptNode;
import com.oracle.truffle.js.nodes.instrument.JSNodeProber;
import com.oracle.truffle.js.parser.JSEngine;
import com.oracle.truffle.js.parser.env.Environment;
import com.oracle.truffle.js.runtime.JSContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.netbeans.modules.debugger.jpda.backend.truffle.js.JPDAJSNodeProber;

/**
 *
 * @author martin
 */
class JPDATruffleDebugManager extends AbstractDebugManager {
    
    private static final JSNodeProberDelegate nodeProberDelegate = new JSNodeProberDelegate();
    
    private final ScriptEngine engine;
    private final ExecutionContext context;

    public JPDATruffleDebugManager(ScriptEngine engine, ExecutionContext context, DebugClient dbgClient) {
        super(context, dbgClient);
        this.engine = engine;
        this.context = context;
        nodeProberDelegate.addNodeProber(
                new JPDAJSNodeProber((JSContext) context, this, new JPDAInstrumentProxy(instrumentCallback)));
        System.err.println("new JPDATruffleDebugManager("+engine+")");
    }
    
    static JPDATruffleDebugManager setUp() {
        System.err.println("JPDATruffleDebugManager.setUp()");
        TruffleJSEngineFactory.addNodeProber(nodeProberDelegate);
        //TruffleJSEngine engine = (TruffleJSEngine) new ScriptEngineManager().getEngineByName(TruffleJSEngineFactory.ENGINE_NAME);
        //JSContext jsContext = engine.getJSContext();
        return null; // Initialize TruffleJSEngine class only.
        /*
        JPDATruffleDebugManager debugManager = new JPDATruffleDebugManager(engine, jsContext, new JPDADebugClient());
        //jsContext.setDebugContext(new JPDADebugContext(jsContext, debugManager));
        jsContext.addProbeListener(new ProbeListener() {
            @Override
            public void newProbeInserted(SourceSection ss, Probe probe) {
                System.err.println("ProbeListener.newProbeInserted("+ss+", "+probe+")");
            }
            @Override
            public void probeTaggedAs(Probe probe, PhylumTag pt) {
                System.err.println("ProbeListener.probeTaggedAs("+probe+", "+pt+")");
            }
        });
        //jsContext.addNodeProber(new JPDAJSNodeProber(jsContext, debugManager, ));
        System.err.println("SET UP of JPDATruffleDebugManager = "+debugManager+" and prober to "+jsContext);
        return debugManager;
        */
    }

    static JPDATruffleDebugManager setUp(ScriptEngine engine) {
        //System.err.println("JPDATruffleDebugManager.setUp()");
        JSContext jsContext = ((TruffleJSEngine) engine).getJSContext();
        //ScriptContext context = engine.getContext();
        JPDATruffleDebugManager debugManager = new JPDATruffleDebugManager(engine, jsContext, new JPDADebugClient(jsContext));
        //jsContext.setDebugContext(new JPDADebugContext(jsContext, debugManager));
        //jsContext.addNodeProber(new JPDAJSNodeProber(jsContext, debugManager, ));
        System.err.println("SET UP of JPDATruffleDebugManager = "+debugManager+" for "+engine+" and prober to "+jsContext);
        return debugManager;
    }
    
    ExecutionContext getContext() {
        return context;
    }
    
    @Override
    public void run(Source source) {
        System.err.println("JPDATruffleDebugManager.run("+source+")");
        startExecution(source);
        try {
            final ScriptNode scriptNode = JSEngine.getInstance().getParser().parseScriptNode((JSContext) context, source);
            scriptNode.run();
        } finally {
            endExecution(source);
        }
    }

    @Override
    public Object eval(Source source, Node node, MaterializedFrame frame) {
        System.err.println("JPDATruffleDebugManager.eval("+source+", "+node+", "+frame+")");
        startExecution(source);
        try {
            if (frame == null) {
                return engine.eval(source.getCode());
            }
            final JavaScriptNode jsNode = (JavaScriptNode) node;
            final Environment environment = (Environment) jsNode.getEnvironment();
            return ((JSContext) context).getEvaluator().evaluate((JSContext) context, source, environment, frame);
        } catch (ScriptException e) {
            //throw JSException.create(JSErrorType.EvalError, e.getMessage());
            return null;
        } finally {
            endExecution(source);
        }
    }
    
    private static SourcePosition getPosition(Node node) {
        SourceSection sourceSection = node.getSourceSection();
        int line = sourceSection.getStartLine();
        Source source = sourceSection.getSource();
        System.err.println("source of "+node+" = "+source);
        System.err.println("  name = "+source.getName());
        System.err.println("  short name = "+source.getShortName());
        System.err.println("  path = "+source.getPath());
        System.err.println("  code at line = "+source.getCode(line));
        String name = source.getShortName();
        String path = source.getPath();
        String code = source.getCode();
        return new SourcePosition(source, name, path, line, code);
    }
    
    private static class JPDADebugClient implements DebugClient {
        
        private final ExecutionContext context;
        
        public JPDADebugClient(ExecutionContext context) {
            this.context = context;
        }

        @Override
        public void haltedAt(Node astNode, MaterializedFrame frame) {
            SourcePosition position = getPosition(astNode);
            Visualizer visualizer = context.getVisualizer();
            
            Object[] arguments = frame.getArguments();
            FrameDescriptor frameDescriptor = frame.getFrameDescriptor();
            Set<Object> identifiers = frameDescriptor.getIdentifiers();
            
            List<? extends FrameSlot> slotsList = frameDescriptor.getSlots();
            ArrayList<FrameSlot> slotsArr = new ArrayList<>();
            for (FrameSlot fs : slotsList) {
                FrameSlotKind kind = fs.getKind();
                if (FrameSlotKind.Illegal.equals(kind)) {
                    continue;
                }
                slotsArr.add(fs);
            }
            FrameSlot[] slots = slotsArr.toArray(new FrameSlot[]{});
            String[] slotNames = new String[slots.length];
            String[] slotTypes = new String[slots.length];
            for (int i = 0; i < slots.length; i++) {
                slotNames[i] = visualizer.displayIdentifier(slots[i]);// slots[i].getIdentifier().toString();
                slotTypes[i] = slots[i].getKind().toString();
            }
            
            System.err.println("JPDADebugClient: HALTED AT "+astNode+", "+frame+
                               "\n                 src. pos. = "+
                               position.path+":"+position.line);
            System.err.println("  frame arguments = "+Arrays.toString(arguments));
            System.err.println("  identifiers = "+Arrays.toString(identifiers.toArray()));
            System.err.println("  slots = "+Arrays.toString(slotsList.toArray()));
            
            for (int i = 0; i < slots.length; i++) {
                System.err.println("    "+slotNames[i]+" = "+JPDATruffleAccessor.getSlotValue(frame, slots[i]));
            }
            
            Iterable<FrameInstance> stackTraceIt = Truffle.getRuntime().getStackTrace();
            ArrayList<FrameInstance> stackTraceArr = new ArrayList<>();
            for (FrameInstance fi : stackTraceIt) {
                stackTraceArr.add(fi);
            }
            FrameInstance[] stackTrace = stackTraceArr.toArray(new FrameInstance[]{});
            String[] stackNames = new String[stackTrace.length];
            for (int i = 0; i < stackTrace.length; i++) {
                //stackNames[i] = stackTrace[i].getCallNode().getDescription();
                stackNames[i] = visualizer.displaySourceLocation(stackTrace[i].getCallNode());
            }
            System.err.println("  stack trace = "+Arrays.toString(stackTrace));
            System.err.println("  stack names = "+Arrays.toString(stackNames));
            
            JPDATruffleAccessor.executionHalted(astNode, frame,
                    position.id, position.name, position.path,
                    position.line, position.code,
                    slots, slotNames, slotTypes,
                    stackTrace, stackNames);
        }
        
    }
    
    private static class JPDAInstrumentProxy implements DebugInstrumentCallback {
        
        private final DebugInstrumentCallback delegateCallback;
        private boolean isStepping = false;
        
        public JPDAInstrumentProxy(DebugInstrumentCallback delegateCallback) {
            this.delegateCallback = delegateCallback;
        }

        @Override
        public boolean isStepping() {
            if (!isStepping) {
                return delegateCallback.isStepping();
            } else {
                return true;
            }
        }

        @Override
        public void haltedAt(Node astNode, MaterializedFrame frame) {
            System.err.println("JPDAInstrumentProxy.haltedAt("+astNode+", "+frame+")");
            delegateCallback.haltedAt(astNode, frame);
        }

        @Override
        public void callEntering(Node astNode, String name) {
            System.err.println("JPDAInstrumentProxy.callEntering("+astNode+", "+name+")");
            astNode.getSourceSection();
            if (JPDATruffleAccessor.isSteppingInto) {
                SourcePosition position = getPosition(astNode);
                JPDATruffleAccessor.executionStepInto(astNode, name,
                        position.id, position.name, position.path,
                        position.line, position.code);                        
            }
            delegateCallback.callEntering(astNode, name);
        }

        @Override
        public void callReturned(Node astNode, String name) {
            System.err.println("JPDAInstrumentProxy.callReturned("+astNode+", "+name+")");
            delegateCallback.callReturned(astNode, name);
        }
        
    }
    
    private static final class SourcePosition {
        
        private static final Map<Source, Long> sourceId = new WeakHashMap<Source, Long>();
        private static long nextId = 0;
        
        long id;
        String name;
        String path;
        int line;
        String code;
        
        public SourcePosition(Source source, String name, String path, int line, String code) {
            this.id = getId(source);
            this.name = name;
            this.path = path;
            this.line = line;
            this.code = code;
        }
        
        private static synchronized long getId(Source s) {
            Long id = sourceId.get(s);
            if (id == null) {
                id = new Long(nextId++);
                sourceId.put(s, id);
            }
            return id;
        }
    }
    
    private static final class JSNodeProberDelegate implements JSNodeProber {
        
        private final List<JSNodeProber> probers = new ArrayList<>();

        @Override
        public JavaScriptNode probeAsStatement(JavaScriptNode jsn) {
            for (JSNodeProber p : probers) {
                jsn = p.probeAsStatement(jsn);
            }
            return jsn;
        }

        @Override
        public JavaScriptNode probeAsCall(JavaScriptNode jsn, String name) {
            for (JSNodeProber p : probers) {
                jsn = p.probeAsCall(jsn, name);
            }
            return jsn;
        }

        @Override
        public JavaScriptNode probeAsLocalAssignment(JavaScriptNode jsn, String name) {
            for (JSNodeProber p : probers) {
                jsn = p.probeAsLocalAssignment(jsn, name);
            }
            return jsn;
        }

        @Override
        public JavaScriptNode probeAsThrow(JavaScriptNode jsn) {
            for (JSNodeProber p : probers) {
                jsn = p.probeAsThrow(jsn);
            }
            return jsn;
        }

        @Override
        public Node probeAs(Node node, PhylumTag pt, Object... os) {
            for (JSNodeProber p : probers) {
                node = p.probeAs(node, pt, os);
            }
            return node;
        }

        private void addNodeProber(JSNodeProber nodeProber) {
            probers.add(nodeProber);
        }
        
    }
    
    /*
    private static class JPDADebugContext implements DebugContext {
        
        private final ExecutionContext execContext;
        private final DebugManager debugManager;
        
        public JPDADebugContext(ExecutionContext execContext, DebugManager debugManager) {
            this.execContext = execContext;
            this.debugManager = debugManager;
        }

        @Override
        public ExecutionContext getContext() {
            return execContext;
        }

        @Override
        public NodeInstrumenter getNodeInstrumenter() {
            return null;
        }

        @Override
        public DebugManager getDebugManager() {
            return debugManager;
        }

        @Override
        public ASTPrinter getASTPrinter() {
            return null;
        }

        @Override
        public String displayValue(Object o) {
            return String.valueOf(o);
        }

        @Override
        public String displayIdentifier(FrameSlot fs) {
            return fs.getIdentifier().toString();
        }

        @Override
        public void executionHalted(Node node, MaterializedFrame mf) {
            JPDATruffleAccessor.executionHalted(node, mf);
        }
        
    }
    */
}
