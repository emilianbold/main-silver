/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.model.impl;

import com.oracle.truffle.js.parser.nashorn.internal.ir.AccessNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.BinaryNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.Block;
import com.oracle.truffle.js.parser.nashorn.internal.ir.BlockStatement;
import com.oracle.truffle.js.parser.nashorn.internal.ir.BreakNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.CallNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.CaseNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.CatchNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.ClassNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.ContinueNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.DebuggerNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.EmptyNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.ErrorNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.ExpressionStatement;
import com.oracle.truffle.js.parser.nashorn.internal.ir.ForNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.FunctionNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.IdentNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.IfNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.IndexNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.JoinPredecessorExpression;
import com.oracle.truffle.js.parser.nashorn.internal.ir.LabelNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.LexicalContext;
import com.oracle.truffle.js.parser.nashorn.internal.ir.LiteralNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.Node;
import com.oracle.truffle.js.parser.nashorn.internal.ir.ObjectNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.PropertyNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.ReturnNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.RuntimeNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.SwitchNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.TernaryNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.ThrowNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.TryNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.UnaryNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.VarNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.WhileNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.WithNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.visitor.NodeVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Petr Pisl
 */
public class PathNodeVisitor extends NodeVisitor {

    private final List<Node> treePath = new ArrayList<Node>();

    public PathNodeVisitor() {
        this(new LexicalContext());
    }

    public PathNodeVisitor(LexicalContext lc) {
        super(lc);
    }

    public List<? extends Node> getPath() {
        return Collections.unmodifiableList(treePath);
    }

    public void addToPath(Node node) {
        treePath.add(node);
    }

    public void removeFromPathTheLast() {
        treePath.remove(treePath.size() - 1);
    }

    @Override
    public Node leaveClassNode(ClassNode classNode) {
        removeFromPathTheLast();
        return super.leaveClassNode(classNode);
    }

    @Override
    public boolean enterClassNode(ClassNode classNode) {
        addToPath(classNode);
        return super.enterClassNode(classNode);
    }

    @Override
    public Node leaveWithNode(WithNode withNode) {
        removeFromPathTheLast();
        return super.leaveWithNode(withNode);
    }

    @Override
    public boolean enterWithNode(WithNode withNode) {
        addToPath(withNode);
        return super.enterWithNode(withNode);
    }

    @Override
    public Node leaveWhileNode(WhileNode whileNode) {
        removeFromPathTheLast();
        return super.leaveWhileNode(whileNode);
    }

    @Override
    public boolean enterWhileNode(WhileNode whileNode) {
        addToPath(whileNode);
        return super.enterWhileNode(whileNode);
    }

    @Override
    public Node leaveVarNode(VarNode varNode) {
        removeFromPathTheLast();
        return super.leaveVarNode(varNode);
    }

    @Override
    public boolean enterVarNode(VarNode varNode) {
        addToPath(varNode);
        return super.enterVarNode(varNode);
    }

    @Override
    public Node leaveJoinPredecessorExpression(JoinPredecessorExpression expr) {
        removeFromPathTheLast();
        return super.leaveJoinPredecessorExpression(expr);
    }

    @Override
    public boolean enterJoinPredecessorExpression(JoinPredecessorExpression expr) {
        addToPath(expr);
        return super.enterJoinPredecessorExpression(expr);
    }

    @Override
    public Node leaveUnaryNode(UnaryNode unaryNode) {
        removeFromPathTheLast();
        return super.leaveUnaryNode(unaryNode);
    }

    @Override
    public boolean enterUnaryNode(UnaryNode unaryNode) {
        addToPath(unaryNode);
        return super.enterUnaryNode(unaryNode);
    }

    @Override
    public Node leaveTryNode(TryNode tryNode) {
        removeFromPathTheLast();
        return super.leaveTryNode(tryNode);
    }

    @Override
    public boolean enterTryNode(TryNode tryNode) {
        addToPath(tryNode);
        return super.enterTryNode(tryNode);
    }

    @Override
    public Node leaveThrowNode(ThrowNode throwNode) {
        removeFromPathTheLast();
        return super.leaveThrowNode(throwNode);
    }

    @Override
    public boolean enterThrowNode(ThrowNode throwNode) {
        addToPath(throwNode);
        return super.enterThrowNode(throwNode);
    }

    @Override
    public Node leaveTernaryNode(TernaryNode ternaryNode) {
        removeFromPathTheLast();
        return super.leaveTernaryNode(ternaryNode);
    }

    @Override
    public boolean enterTernaryNode(TernaryNode ternaryNode) {
        addToPath(ternaryNode);
        return super.enterTernaryNode(ternaryNode);
    }

    @Override
    public Node leaveSwitchNode(SwitchNode switchNode) {
        removeFromPathTheLast();
        return super.leaveSwitchNode(switchNode);
    }

    @Override
    public boolean enterSwitchNode(SwitchNode switchNode) {
        addToPath(switchNode);
        return super.enterSwitchNode(switchNode);
    }

    @Override
    public Node leaveRuntimeNode(RuntimeNode runtimeNode) {
        removeFromPathTheLast();
        return super.leaveRuntimeNode(runtimeNode);
    }

    @Override
    public boolean enterRuntimeNode(RuntimeNode runtimeNode) {
        addToPath(runtimeNode);
        return super.enterRuntimeNode(runtimeNode);
    }

    @Override
    public Node leaveReturnNode(ReturnNode returnNode) {
        removeFromPathTheLast();
        return super.leaveReturnNode(returnNode);
    }

    @Override
    public boolean enterReturnNode(ReturnNode returnNode) {
        addToPath(returnNode);
        return super.enterReturnNode(returnNode);
    }

    @Override
    public Node leavePropertyNode(PropertyNode propertyNode) {
        removeFromPathTheLast();
        return super.leavePropertyNode(propertyNode);
    }

    @Override
    public boolean enterPropertyNode(PropertyNode propertyNode) {
        addToPath(propertyNode);
        return super.enterPropertyNode(propertyNode);
    }

    @Override
    public Node leaveObjectNode(ObjectNode objectNode) {
        removeFromPathTheLast();
        return super.leaveObjectNode(objectNode);
    }

    @Override
    public boolean enterObjectNode(ObjectNode objectNode) {
        addToPath(objectNode);
        return super.enterObjectNode(objectNode);
    }

    @Override
    public Node leaveLiteralNode(LiteralNode literalNode) {
        removeFromPathTheLast();
        return super.leaveLiteralNode(literalNode);
    }

    @Override
    public boolean enterLiteralNode(LiteralNode literalNode) {
        addToPath(literalNode);
        return super.enterLiteralNode(literalNode);
    }

    @Override
    public Node leaveLabelNode(LabelNode labelNode) {
        removeFromPathTheLast();
        return super.leaveLabelNode(labelNode);
    }

    @Override
    public boolean enterLabelNode(LabelNode labelNode) {
        addToPath(labelNode);
        return super.enterLabelNode(labelNode);
    }

    @Override
    public Node leaveIndexNode(IndexNode indexNode) {
        removeFromPathTheLast();
        return super.leaveIndexNode(indexNode);
    }

    @Override
    public boolean enterIndexNode(IndexNode indexNode) {
        addToPath(indexNode);
        return super.enterIndexNode(indexNode);
    }

    @Override
    public Node leaveIfNode(IfNode ifNode) {
        removeFromPathTheLast();
        return super.leaveIfNode(ifNode);
    }

    @Override
    public boolean enterIfNode(IfNode ifNode) {
        addToPath(ifNode);
        return super.enterIfNode(ifNode);
    }

    @Override
    public Node leaveIdentNode(IdentNode identNode) {
        removeFromPathTheLast();
        return super.leaveIdentNode(identNode);
    }

    @Override
    public boolean enterIdentNode(IdentNode identNode) {
        addToPath(identNode);
        return super.enterIdentNode(identNode);
    }

    @Override
    public Node leaveFunctionNode(FunctionNode functionNode) {
        removeFromPathTheLast();
        return super.leaveFunctionNode(functionNode);
    }

    @Override
    public boolean enterFunctionNode(FunctionNode functionNode) {
        addToPath(functionNode);
        return super.enterFunctionNode(functionNode);
    }

    @Override
    public Node leaveForNode(ForNode forNode) {
        removeFromPathTheLast();
        return super.leaveForNode(forNode);
    }

    @Override
    public boolean enterForNode(ForNode forNode) {
        addToPath(forNode);
        return super.enterForNode(forNode);
    }

    @Override
    public Node leaveBlockStatement(BlockStatement blockStatement) {
        removeFromPathTheLast();
        return super.leaveBlockStatement(blockStatement);
    }

    @Override
    public boolean enterBlockStatement(BlockStatement blockStatement) {
        addToPath(blockStatement);
        return super.enterBlockStatement(blockStatement);
    }

    @Override
    public Node leaveExpressionStatement(ExpressionStatement expressionStatement) {
        removeFromPathTheLast();
        return super.leaveExpressionStatement(expressionStatement);
    }

    @Override
    public boolean enterExpressionStatement(ExpressionStatement expressionStatement) {
        addToPath(expressionStatement);
        return super.enterExpressionStatement(expressionStatement);
    }

    @Override
    public Node leaveErrorNode(ErrorNode errorNode) {
        removeFromPathTheLast();
        return super.leaveErrorNode(errorNode);
    }

    @Override
    public boolean enterErrorNode(ErrorNode errorNode) {
        addToPath(errorNode);
        return super.enterErrorNode(errorNode);
    }

    @Override
    public Node leaveEmptyNode(EmptyNode emptyNode) {
        removeFromPathTheLast();
        return super.leaveEmptyNode(emptyNode);
    }

    @Override
    public boolean enterEmptyNode(EmptyNode emptyNode) {
        addToPath(emptyNode);
        return super.enterEmptyNode(emptyNode);
    }

    @Override
    public Node leaveDebuggerNode(DebuggerNode debuggerNode) {
        removeFromPathTheLast();
        return super.leaveDebuggerNode(debuggerNode);
    }

    @Override
    public boolean enterDebuggerNode(DebuggerNode debuggerNode) {
        addToPath(debuggerNode);
        return super.enterDebuggerNode(debuggerNode);
    }

    @Override
    public Node leaveContinueNode(ContinueNode continueNode) {
        removeFromPathTheLast();
        return super.leaveContinueNode(continueNode);
    }

    @Override
    public boolean enterContinueNode(ContinueNode continueNode) {
        addToPath(continueNode);
        return super.enterContinueNode(continueNode);
    }

    @Override
    public Node leaveCatchNode(CatchNode catchNode) {
        removeFromPathTheLast();
        return super.leaveCatchNode(catchNode);
    }

    @Override
    public boolean enterCatchNode(CatchNode catchNode) {
        addToPath(catchNode);
        return super.enterCatchNode(catchNode);
    }

    @Override
    public Node leaveCaseNode(CaseNode caseNode) {
        removeFromPathTheLast();
        return super.leaveCaseNode(caseNode);
    }

    @Override
    public boolean enterCaseNode(CaseNode caseNode) {
        addToPath(caseNode);
        return super.enterCaseNode(caseNode);
    }

    @Override
    public Node leaveCallNode(CallNode callNode) {
        removeFromPathTheLast();
        return super.leaveCallNode(callNode);
    }

    @Override
    public boolean enterCallNode(CallNode callNode) {
        addToPath(callNode);
        return super.enterCallNode(callNode);
    }

    @Override
    public Node leaveBreakNode(BreakNode breakNode) {
        removeFromPathTheLast();
        return super.leaveBreakNode(breakNode);
    }

    @Override
    public boolean enterBreakNode(BreakNode breakNode) {
        addToPath(breakNode);
        return super.enterBreakNode(breakNode);
    }

    @Override
    public Node leaveBinaryNode(BinaryNode binaryNode) {
        removeFromPathTheLast();
        return super.leaveBinaryNode(binaryNode);
    }

    @Override
    public boolean enterBinaryNode(BinaryNode binaryNode) {
        addToPath(binaryNode);
        return super.enterBinaryNode(binaryNode);
    }

    @Override
    public Node leaveBlock(Block block) {
        removeFromPathTheLast();
        return super.leaveBlock(block);
    }

    @Override
    public boolean enterBlock(Block block) {
        addToPath(block);
        return super.enterBlock(block);
    }

    @Override
    public Node leaveAccessNode(AccessNode accessNode) {
        removeFromPathTheLast();
        return super.leaveAccessNode(accessNode);
    }

    @Override
    public boolean enterAccessNode(AccessNode accessNode) {
        addToPath(accessNode);
        return super.enterAccessNode(accessNode);
    }

}
