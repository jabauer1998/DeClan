package io.github.H20man13.DeClan.main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.RuntimeErrorException;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.analysis.AnticipatedExpressionsAnalysis;
import io.github.H20man13.DeClan.common.analysis.AvailableExpressionsAnalysis;
import io.github.H20man13.DeClan.common.analysis.ConstantPropogationAnalysis;
import io.github.H20man13.DeClan.common.analysis.LiveVariableAnalysis;
import io.github.H20man13.DeClan.common.analysis.PostponableExpressionsAnalysis;
import io.github.H20man13.DeClan.common.analysis.UsedExpressionAnalysis;
import io.github.H20man13.DeClan.common.dag.DagGraph;
import io.github.H20man13.DeClan.common.dag.DagIgnoredInstruction;
import io.github.H20man13.DeClan.common.dag.DagInlineAssemblyNode;
import io.github.H20man13.DeClan.common.dag.DagNode;
import io.github.H20man13.DeClan.common.dag.DagNodeFactory;
import io.github.H20man13.DeClan.common.dag.DagOperationNode;
import io.github.H20man13.DeClan.common.dag.DagValueNode;
import io.github.H20man13.DeClan.common.dag.DagVariableNode;
import io.github.H20man13.DeClan.common.dag.DagNode.ScopeType;
import io.github.H20man13.DeClan.common.dag.DagNode.ValueType;
import io.github.H20man13.DeClan.common.exception.OptimizerException;
import io.github.H20man13.DeClan.common.flow.BasicBlock;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.EntryNode;
import io.github.H20man13.DeClan.common.flow.ExitNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Call;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Inline;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.label.Label;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.icode.section.SymSec;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.symboltable.entry.LiveInfo;
import io.github.H20man13.DeClan.common.symboltable.entry.ProcedureEntry;
import io.github.H20man13.DeClan.common.util.ConversionUtils;
import io.github.H20man13.DeClan.common.util.OpUtil;
import io.github.H20man13.DeClan.common.util.Utils;

public class MyOptimizer {
    private Prog intermediateCode;
    private FlowGraph globalFlowGraph;
    private ConstantPropogationAnalysis propAnal;
    private LiveVariableAnalysis liveAnal;

    private enum OptName{
        COMMON_SUB_EXPRESSION_ELIMINATION,
        CONSTANT_PROPOGATION,
        DEAD_CODE_ELIMINATION
    }

    public MyOptimizer(Prog intermediateCode){
        this.intermediateCode = intermediateCode;
        this.globalFlowGraph = null;
        this.propAnal = null;
        this.liveAnal = null;
    }

    public LiveVariableAnalysis getLiveVariableAnalysis(){
        return this.liveAnal;
    }

    private static Map<String, BlockNode> findProcedureEntryPoints(List<BlockNode> nodes){
        Map<String, BlockNode> toRet = new HashMap<String, BlockNode>();
        for(BlockNode node : nodes){
            if(Utils.beginningOfBlockIsProcedureHeader(node.getBlock())){
                ProcLabel label = (ProcLabel)node.getICode().get(0);
                toRet.put(label.label, node);
            }
        }
        return toRet;
    }

    private static Map<String, BlockNode> findBranchEntryPoints(List<BlockNode> nodes){
        Map<String, BlockNode> toRet = new HashMap<String, BlockNode>();
        for(BlockNode node: nodes){
            BasicBlock block = node.getBlock();
            if(Utils.beginningOfBlockIsLabel(block)){
                Label lab = (Label)block.getIcode().get(0);
                toRet.put(lab.label, node);
            }
        }
        return toRet;
    }

    private static Map<String, BlockNode> findProcedureExitPoints(List<BlockNode> nodes){
        Map<String, BlockNode> toRet = new HashMap<String, BlockNode>();
        for(int i = 0; i < nodes.size(); i++){
            BlockNode node = nodes.get(i);
            if(Utils.beginningOfBlockIsProcedureHeader(node.getBlock())){
                ProcLabel label = (ProcLabel)node.getICode().get(0);
                for(; i < nodes.size(); i++){
                    BlockNode otherNode = nodes.get(i);
                    if(Utils.endOfBlockIsReturn(otherNode.getBlock())){
                        toRet.put(label.label, otherNode);
                        break;
                    }
                }
            }
        }
        return toRet;
    }

    private static void linkUpFunctionCalls(List<BlockNode> nodes, Map<String, BlockNode> functionBlocks){
        int nodeSize = nodes.size();
    	for(int i = 0; i < nodeSize; i++){
        	BlockNode node = nodes.get(i);
            BasicBlock block = node.getBlock();
            if(Utils.endOfBlockIsJump(block)){
                ICode lastCode = block.getIcode().get(block.getIcode().size() - 1);
                if(lastCode instanceof Call){
                    Call lastProc = (Call)lastCode;
                    BlockNode labeledNode = functionBlocks.get(lastProc.pname);
                    if(labeledNode != null){
                        node.addSuccessor(labeledNode);
                        labeledNode.addPredecessor(node);
                    } else if(i + 1 < nodeSize) {
                    	BlockNode retNode = nodes.get(i + 1);
                    	node.addSuccessor(retNode);
                    	retNode.addPredecessor(node);
                    }
                }
            }
        }
    }

    private static void linkUpJumps(List<BlockNode> nodes, Map<String, BlockNode> branchLabels){
        for(BlockNode node : nodes){
            BasicBlock block = node.getBlock();
            if(Utils.endOfBlockIsJump(block)){
                ICode lastICode = block.getIcode().get(block.getIcode().size() - 1);
                if(lastICode instanceof If){
                    If lastIf = (If)lastICode;
                    BlockNode trueNode = branchLabels.get(lastIf.ifTrue);
                    if(trueNode != null){
                        node.addSuccessor(trueNode);
                        trueNode.addPredecessor(node);
                    }
                    BlockNode falseNode = branchLabels.get(lastIf.ifFalse);
                    if(falseNode != null){
                        node.addSuccessor(falseNode);
                        falseNode.addPredecessor(node);
                    }
                } else if(lastICode instanceof Goto){
                    Goto lastGoto = (Goto)lastICode;
                    BlockNode labeledNode = branchLabels.get(lastGoto.label);
                    if(labeledNode != null){
                        node.addSuccessor(labeledNode);
                        labeledNode.addPredecessor(node);
                    }
                }
            }
        }
    }

    private static void linkUpReturns(List<BlockNode> nodes, Map<String, BlockNode> procedureEndings){
        for(int i = 0; i < nodes.size(); i++){
            BlockNode node = nodes.get(i);
            BasicBlock block = node.getBlock();
            if(Utils.endOfBlockIsJump(block)){
                ICode lastICode = block.getIcode().get(block.getIcode().size() - 1);
                if(lastICode instanceof Call && i + 1 < nodes.size()){
                    Call call = (Call)lastICode;
                    BlockNode nextNode = nodes.get(i + 1);
                    BlockNode returnNode = procedureEndings.get(call.pname);
                    if(returnNode != null){
                        returnNode.addSuccessor(nextNode);
                        nextNode.addPredecessor(returnNode);
                    }
                }
            }
        }
    }
    
    private static void linkUpFollowThrough(List<BlockNode> nodes) {
    	for(int i = 0; i < nodes.size() - 1; i++) {
    		BlockNode current = nodes.get(i);
    		BasicBlock currentBlock = current.getBlock();
    		if(!Utils.endOfBlockIsJump(currentBlock)) {
    			BlockNode nextNode = nodes.get(i + 1);
    			nextNode.addPredecessor(current);
    			current.addSuccessor(nextNode);
    		}
    	}
    }

    private List<BasicBlock> buildBlocks(){
        List<BasicBlock> blocks = new LinkedList<BasicBlock>();
        List<Integer> firsts = findFirstsInICode(this.intermediateCode.getICode());
        int firstSize = firsts.size();
        for(int leaderIndex = 0; leaderIndex < firstSize; leaderIndex++){
            int beginIndex = firsts.get(leaderIndex);
            int endIndex;
            if(leaderIndex + 1 < firstSize){
                endIndex = firsts.get(leaderIndex + 1) - 1;
            } else {
                endIndex = intermediateCode.getSize() - 1;
            }
            List<ICode> basicBlockList = new LinkedList<ICode>();
            for(int i = beginIndex; i <= endIndex; i++){
                basicBlockList.add(intermediateCode.getInstruction(i));
            }
            blocks.add(new BasicBlock(basicBlockList));
        }

        return blocks;
    }

    private List<BlockNode> buildBlockNodes(){
        if(this.intermediateCode == null){
            String message = "Intermediate Code variable not initialized to value";
            throw new OptimizerException(message.getClass().getEnclosingMethod().getName(), message);
        }
        List<BasicBlock> blocks = buildBlocks();
        List<BlockNode> nodes = new LinkedList<BlockNode>(); 
        for(BasicBlock block: blocks){
            nodes.add(new BlockNode(block));
        }
        return nodes;
    }

    private void buildFlowGraph() {
        List<BlockNode> nodeList = buildBlockNodes();
        
        Map<String, BlockNode> procedureEntryNodes = findProcedureEntryPoints(nodeList);
        linkUpFunctionCalls(nodeList, procedureEntryNodes);

        Map<String, BlockNode> codeLabeledNodes = findBranchEntryPoints(nodeList);
        linkUpJumps(nodeList, codeLabeledNodes);

        Map<String, BlockNode> procedureExitNodes = findProcedureExitPoints(nodeList);
        linkUpReturns(nodeList, procedureExitNodes);
        
        linkUpFollowThrough(nodeList);


        if(nodeList.size() == 0){
            throw new OptimizerException(nodeList.getClass().getEnclosingMethod().getName(), "Data section and the Code Section are empty!!!");
        }

        EntryNode entry;
        ExitNode exit;

        entry = new EntryNode(nodeList.get(0));
        
        
        exit = new ExitNode(findEndingBlock(nodeList));
        FlowGraph flowGraph = new FlowGraph(entry, nodeList, exit);
        this.globalFlowGraph = flowGraph;
    }
    
    private static BlockNode findEndingBlock(List<BlockNode> blocks) {
    	for(BlockNode block: blocks) {
    		if(Utils.endOfBlockIsEnd(block.getBlock())) {
    			return block;
    		}
    	}
    	
    	throw new OptimizerException("findEndingBlock", "Cant find block with End Exception");
    }

    public void rebuildFromFlowGraph(){
        if(this.globalFlowGraph != null){
            this.intermediateCode = new Prog(false);
            for(BlockNode dataBlock: this.globalFlowGraph){
                for(ICode icode: dataBlock){
                    this.intermediateCode.addInstruction(icode);
                }
            }
        }
    }

    public void performCommonSubExpressionElimination(){
            setUpOptimization(OptName.COMMON_SUB_EXPRESSION_ELIMINATION);
            for(BlockNode block: globalFlowGraph.getBlocks()){
                DagGraph dag = buildDagForBlock(block, true);
                regenerateICodeForBlock(block, dag);
            }
            cleanUpOptimization(OptName.COMMON_SUB_EXPRESSION_ELIMINATION);
    }
    
    private void resetLiveVariableAnalysis() {
    	this.liveAnal = null;
    }
    
    private void resetFlowGraph() {
    	this.globalFlowGraph = null;
    }
    
    private void resetConstantPropogationAnalysis() {
    	this.propAnal = null;
    }
    
    

    private void cleanUpOptimization(OptName name){
        switch(name){
            case COMMON_SUB_EXPRESSION_ELIMINATION:
            	rebuildFromFlowGraph();
                resetFlowGraph();
                resetLiveVariableAnalysis();
                break;
            case CONSTANT_PROPOGATION:
            	rebuildFromFlowGraph();
                resetConstantPropogationAnalysis();
                resetFlowGraph();
                break;
            case DEAD_CODE_ELIMINATION:
            	rebuildFromFlowGraph();
            	resetFlowGraph();
                resetLiveVariableAnalysis();
                break;
        }
    }

    private void setUpOptimization(OptName name){
        switch(name){
            case COMMON_SUB_EXPRESSION_ELIMINATION:
                buildFlowGraph();
                runLiveVariableAnalysis();
                break;
            case CONSTANT_PROPOGATION:
                buildFlowGraph();
                runConstantPropogationAnalysis();
                break;
            case DEAD_CODE_ELIMINATION:
                buildFlowGraph();
                runLiveVariableAnalysis();
                break;
        }
    }

    private void regenerateICodeForBlock(BlockNode block, DagGraph dag){
        List<ICode> result = new LinkedList<ICode>();
        List<ICode> initialList = block.getICode();

        Set<String> liveAtEndOfBlock = this.liveAnal.getInputSet(initialList.get(initialList.size() - 1));

        for(DagNode node : dag.getDagNodes()){
        	if(node instanceof DagIgnoredInstruction) {
        		DagIgnoredInstruction ignored = (DagIgnoredInstruction)node;
            	result.add(ignored.getIgnoredInstruction());
        	} else {
        		List<String> isAlive = new LinkedList<String>();
                for(String identifier: node.getIdentifiers()){
                    if(liveAtEndOfBlock.contains(identifier)){
                    	isAlive.add(identifier);
                    }
                }

                String identifier = null;
                if(isAlive.size() > 0){
                    identifier = isAlive.remove(0);
                } else {
                    identifier = node.getIdentifiers().get(0);
                }

                ScopeType scope = node.getScopeType();
                ValueType type = node.getValueType();

                if(node instanceof DagOperationNode){
                    DagOperationNode node2 = (DagOperationNode)node;

                    if(node2.getChildren().size() == 2){
                        //Its a Binary Operation
                        DagOperationNode.Op op = node2.getOperator();
                        BinExp.Operator op2 = ConversionUtils.getBinOp(op);

                        DagNode child1 = node2.getChildren().get(0);
                        DagNode child2 = node2.getChildren().get(1);

                        IdentExp identifier1 = getIdentifier(child1, liveAtEndOfBlock);
                        IdentExp identifier2 = getIdentifier(child2, liveAtEndOfBlock);

                        BinExp binExp = new BinExp(identifier1, op2, identifier2);
                        
                        if(node2.isDef()) {
                        	result.add(new Def(ConversionUtils.dagScopeTypeToAssignScope(scope), identifier, binExp, ConversionUtils.dagValueTypeToAssignType(type)));
                        } else {
                        	result.add(new Assign(ConversionUtils.dagScopeTypeToAssignScope(scope), identifier, binExp, ConversionUtils.dagValueTypeToAssignType(type)));
                        }
                    } else if(node2.getChildren().size() == 1) {
                        //Its a Unary Operation
                        DagOperationNode.Op op = node2.getOperator();
                        UnExp.Operator op2 = ConversionUtils.getUnOp(op);

                        DagNode child1 = node2.getChildren().get(0);

                        IdentExp identifier1 = getIdentifier(child1, liveAtEndOfBlock);

                        UnExp unExp = new UnExp(op2, identifier1);
                        
                        if(node2.isDef()) {
                        	result.add(new Def(ConversionUtils.dagScopeTypeToAssignScope(scope), identifier, unExp, ConversionUtils.dagValueTypeToAssignType(type)));
                        } else {
                        	result.add(new Assign(ConversionUtils.dagScopeTypeToAssignScope(scope), identifier, unExp, ConversionUtils.dagValueTypeToAssignType(type)));
                        }
                    }
                } else if(node instanceof DagValueNode){
                    DagValueNode valNode = (DagValueNode)node;
                    Object value = valNode.getValue();
                    Exp resultExp = ConversionUtils.valueToExp(value);
                    
                    if(valNode.isDef()) {
                    	result.add(new Def(ConversionUtils.dagScopeTypeToAssignScope(scope), identifier, resultExp, ConversionUtils.dagValueTypeToAssignType(type)));
                    } else {
                    	result.add(new Assign(ConversionUtils.dagScopeTypeToAssignScope(scope), identifier, resultExp, ConversionUtils.dagValueTypeToAssignType(type)));
                    }
                } else if(node instanceof DagVariableNode){
                    DagVariableNode varNode = (DagVariableNode)node;
                    DagNode child = varNode.getChild();
                    IdentExp identifier1 = getIdentifier(child, liveAtEndOfBlock);
                    
                    if(varNode.isDef()) {
                    	result.add(new Def(ConversionUtils.dagScopeTypeToAssignScope(scope), identifier, identifier1, ConversionUtils.dagValueTypeToAssignType(type)));
                    } else {
                    	result.add(new Assign(ConversionUtils.dagScopeTypeToAssignScope(scope), identifier, identifier1, ConversionUtils.dagValueTypeToAssignType(type)));
                    }
                } else if(node instanceof DagInlineAssemblyNode){
                    DagInlineAssemblyNode dagNode = (DagInlineAssemblyNode)node;
                    List<IdentExp> children = new LinkedList<IdentExp>();
                    for(DagNode child : dagNode.getChildren()){
                        IdentExp ident = getIdentifier(child, liveAtEndOfBlock);
                        children.add(ident);
                    }
                    List<String> operationList = dagNode.getIdentifiers();
                    result.add(new Inline(operationList.get(0), children));
                }
                
                for(String ident : isAlive){
                    IdentExp ident1 = new IdentExp(ConversionUtils.dagScopeTypeToAssignScope(scope), identifier);
                    result.add(new Def(ConversionUtils.dagScopeTypeToAssignScope(scope), ident, ident1, ConversionUtils.dagValueTypeToAssignType(type)));
                }
        	}
        }
        
        block.getBlock().setICode(result);
    }

    private static IdentExp getIdentifier(DagNode node, Set<String> table){
        List<String> identifiers = node.getIdentifiers();
        ScopeType scope = node.getScopeType();
        for(String identifier : identifiers){
            if(table.contains(identifier)){
            	return new IdentExp(ConversionUtils.dagScopeTypeToAssignScope(scope), identifier);
            }
        }

        if(identifiers.size() > 0){
            return new IdentExp(ConversionUtils.dagScopeTypeToAssignScope(scope), identifiers.get(0));
        } else {
            return null;
        }
    }

    private static DagGraph buildDagForBlock(BlockNode block, boolean optimized){
        DagGraph dag = new DagGraph();
        DagNodeFactory factory = new DagNodeFactory();
        List<ICode> icodes = block.getICode();
        for(ICode icode : icodes){
            if(icode instanceof Assign){
                Assign assignICode = (Assign)icode;

                if(assignICode.value instanceof BinExp){
                    BinExp exp = (BinExp)assignICode.value;
                    
                    DagNode left = dag.searchForLatestChild(exp.left);
                    DagNode right = dag.searchForLatestChild(exp.right);

                    if(left == null){
                        left = factory.createNullNode(exp.left.toString());
                        dag.addDagNode(left);
                    }
    
                    if(right == null){
                        right = factory.createNullNode(exp.right.toString());
                        dag.addDagNode(right);
                    }

                    DagNode newNode = Utils.createBinaryNode(false, assignICode.getScope(), exp.op, assignICode.place, left, right);

                    DagNode exists = dag.getDagNode(newNode);
                    if(exists == null || !optimized){
                        dag.addDagNode(newNode);
                    } else {
                        exists.addIdentifier(assignICode.place);
                    }
                } else if(assignICode.value instanceof UnExp){
                    UnExp exp = (UnExp)assignICode.value;

                    DagNode right = dag.searchForLatestChild(exp.right);

                    if(right == null){
                        right = factory.createNullNode(exp.right.toString());
                        dag.addDagNode(right);
                    }

                    DagNode newNode = Utils.createUnaryNode(false, assignICode.getScope(), exp.op, assignICode.place, right);

                    DagNode exists = dag.getDagNode(newNode);
                    if(exists == null || !optimized){
                        dag.addDagNode(newNode);
                    } else {
                        exists.addIdentifier(assignICode.place);
                    }
                } else if(assignICode.value instanceof IdentExp){
                    IdentExp exp = (IdentExp)assignICode.value;

                    DagNode right = dag.searchForLatestChild(exp);

                    if(right == null){
                        right = factory.createNullNode(exp.ident.toString());
                        dag.addDagNode(right);
                    }

                    DagNode newNode = factory.createDefaultVariableNode(false, assignICode.place, right, assignICode.getType());

                    DagNode exists = dag.getDagNode(newNode);
                    if(exists == null || !optimized){
                        dag.addDagNode(newNode);
                    } else {
                        exists.addIdentifier(assignICode.place);
                    }
                } else if(assignICode.value instanceof BoolExp){
                    BoolExp boolICode = (BoolExp)assignICode.value;
                    DagNode newNode = factory.createBooleanNode(false, assignICode.getScope(), assignICode.place, boolICode.trueFalse);
                    dag.addDagNode(newNode);
                } else if(assignICode.value instanceof IntExp){
                    IntExp intICode = (IntExp)assignICode.value;
                    DagNode newNode = factory.createIntNode(false, assignICode.getScope(), assignICode.place, intICode.value);
                    dag.addDagNode(newNode);
                } else if(assignICode.value instanceof RealExp){
                    RealExp realICode = (RealExp)assignICode.value;
                    DagNode newNode = factory.createRealNode(false, assignICode.getScope(), assignICode.place, realICode.realValue);
                    dag.addDagNode(newNode);
                } else if(assignICode.value instanceof StrExp){
                    StrExp strICode = (StrExp)assignICode.value;
                    DagNode newNode = factory.createStringNode(false, assignICode.getScope(), assignICode.place, strICode.value);
                    dag.addDagNode(newNode);
                }
            } else if(icode instanceof Def){
                Def assignICode = (Def)icode;

                if(assignICode.val instanceof BinExp){
                    BinExp exp = (BinExp)assignICode.val;
                    
                    DagNode left = dag.searchForLatestChild(exp.left);
                    DagNode right = dag.searchForLatestChild(exp.right);

                    if(left == null){
                        left = factory.createNullNode(exp.left.toString());
                        dag.addDagNode(left);
                    }
    
                    if(right == null){
                        right = factory.createNullNode(exp.right.toString());
                        dag.addDagNode(right);
                    }

                    DagNode newNode = Utils.createBinaryNode(true, assignICode.scope, exp.op, assignICode.label, left, right);

                    DagNode exists = dag.getDagNode(newNode);
                    if(exists == null || !optimized){
                        dag.addDagNode(newNode);
                    } else {
                        exists.addIdentifier(assignICode.label);
                    }
                } else if(assignICode.val instanceof UnExp){
                    UnExp exp = (UnExp)assignICode.val;

                    DagNode right = dag.searchForLatestChild(exp.right);

                    if(right == null){
                        right = factory.createNullNode(exp.right.toString());
                        dag.addDagNode(right);
                    }

                    DagNode newNode = Utils.createUnaryNode(true, assignICode.scope, exp.op, assignICode.label, right);

                    DagNode exists = dag.getDagNode(newNode);
                    if(exists == null || !optimized){
                        dag.addDagNode(newNode);
                    } else {
                        exists.addIdentifier(assignICode.label);
                    }
                } else if(assignICode.val instanceof IdentExp){
                    IdentExp exp = (IdentExp)assignICode.val;

                    DagNode right = dag.searchForLatestChild(exp);

                    if(right == null){
                        right = factory.createNullNode(exp.ident.toString());
                        dag.addDagNode(right);
                    }

                    DagNode newNode = factory.createDefaultVariableNode(true, assignICode.label, right, assignICode.type);

                    DagNode exists = dag.getDagNode(newNode);
                    if(exists == null || !optimized){
                        dag.addDagNode(newNode);
                    } else {
                        exists.addIdentifier(assignICode.label);
                    }
                } else if(assignICode.val instanceof BoolExp){
                    BoolExp boolICode = (BoolExp)assignICode.val;
                    DagNode newNode = factory.createBooleanNode(true, assignICode.scope, assignICode.label, boolICode.trueFalse);
                    dag.addDagNode(newNode);
                } else if(assignICode.val instanceof IntExp){
                    IntExp intICode = (IntExp)assignICode.val;
                    DagNode newNode = factory.createIntNode(true, assignICode.scope, assignICode.label, intICode.value);
                    dag.addDagNode(newNode);
                } else if(assignICode.val instanceof RealExp){
                    RealExp realICode = (RealExp)assignICode.val;
                    DagNode newNode = factory.createRealNode(true, assignICode.scope, assignICode.label, realICode.realValue);
                    dag.addDagNode(newNode);
                } else if(assignICode.val instanceof StrExp){
                    StrExp strICode = (StrExp)assignICode.val;
                    DagNode newNode = factory.createStringNode(true, assignICode.scope, assignICode.label, strICode.value);
                    dag.addDagNode(newNode);
                }
            } else if(icode instanceof Inline){
                Inline inline = (Inline)icode;
                LinkedList<DagNode> children = new LinkedList<DagNode>();
                for(IdentExp param : inline.params){
                    DagNode child = dag.searchForLatestChild(param);
                    if(child == null){
                        child = factory.createNullNode(param.ident);
                        dag.addDagNode(child);
                    }

                    children.add(child);
                }

                DagNode newNode = new DagInlineAssemblyNode(inline.inlineAssembly, children);
                
                DagNode exists = dag.getDagNode(newNode);
                if(exists == null){
                    dag.addDagNode(newNode);
                }
            } else {
            	dag.addDagNode(new DagIgnoredInstruction(icode));
            }
        }

        return dag;
    }

    public Prog getICode(){
        return this.intermediateCode;
    }

    private List<Integer> findFirstsInICode(List<ICode> intermediateCode){
        List<Integer> firsts = new LinkedList<Integer>();
        for(int i = 0; i < intermediateCode.size(); i++){
            ICode intermediateInstruction = intermediateCode.get(i);
            if(i == 0){
                //First Statement is allways a leader
                firsts.add(i);
            } else if(intermediateInstruction instanceof Label || intermediateInstruction instanceof ProcLabel){
                //Target of Jumps are allways leaders
                firsts.add(i);
            } else if(i + 1 < intermediateCode.size() && intermediateInstruction.isBranch()){
                //First instruction following an If/Goto/Proc/Call are leaders
                firsts.add(i + 1);
                //To prevent loading a Label as a first twice
                ICode nextInstruction = intermediateCode.get(i + 1);
                if(nextInstruction instanceof Label || nextInstruction instanceof ProcLabel){
                    i++;
                }
            }
        }

        return firsts;
    }

    private void runConstantPropogationAnalysis(){
        if(this.globalFlowGraph == null){
            buildFlowGraph();
        }
        this.propAnal = new ConstantPropogationAnalysis(this.globalFlowGraph);
        this.propAnal.run();
    }

    public void runLiveVariableAnalysis(){
        if(this.globalFlowGraph == null){
            buildFlowGraph();
        }
        this.liveAnal = new LiveVariableAnalysis(this.globalFlowGraph);
        this.liveAnal.run();
    }

    public void performDeadCodeElimination(){
        boolean changes = true;
        while(changes) {
        	changes = false;
        	setUpOptimization(OptName.DEAD_CODE_ELIMINATION);
        	for(BlockNode block : this.globalFlowGraph.getBlocks()){
                List<ICode> result = new LinkedList<ICode>();
                for(ICode icode : block.getICode()){
                    if(icode instanceof Assign){
                        Assign assICode = (Assign)icode;
                        Set<String> liveVariables = this.liveAnal.getOutputSet(icode);
                        if(liveVariables.contains(assICode.place)){
                            result.add(assICode);
                        } else {
                        	changes = true;
                        }
                    } else if(icode instanceof Def){
                    	Def assICode = (Def)icode;
                    	Set<String> liveVariables = this.liveAnal.getOutputSet(icode);
                        if(liveVariables.contains(assICode.label)){
                            result.add(assICode);
                        } else {
                        	changes = true;
                        }
                    } else {
                        result.add(icode);
                    }
                }
                block.getBlock().setICode(result);
            }
        	cleanUpOptimization(OptName.DEAD_CODE_ELIMINATION);
        }
    }

    public void performConstantPropogation(){
        boolean changes = true;
        while(changes) {
        	changes = false;
        	setUpOptimization(OptName.CONSTANT_PROPOGATION);
            for(BlockNode block : this.globalFlowGraph.getBlocks()){
                List<ICode> icodeList = block.getICode();
                for(int i = 0; i < icodeList.size(); i++){
                    ICode icode = icodeList.get(i);
                    Set<Tuple<String, Exp>> values = this.propAnal.getInputSet(icode);
                    if(icode instanceof Assign){
                        Assign varICode = (Assign)icode;
                        if(varICode.value instanceof IdentExp){
                            IdentExp identVal = (IdentExp)varICode.value;
                            String sourceVal = identVal.ident;
                            while(Utils.containsExpInSet(values, sourceVal)){
                                Exp rightHandSide = Utils.getExpFromSet(values, sourceVal);
                                if(rightHandSide instanceof IdentExp){
                                    identVal = (IdentExp)rightHandSide;
                                    sourceVal = identVal.ident;
                                } else if(rightHandSide.isConstant()){
                                    varICode.value = rightHandSide;
                                    changes = true;
                                    break;
                                } else {
                                    break;
                                }
                            }
                        } else if(varICode.value instanceof BinExp){
                            BinExp binExpVal = (BinExp)varICode.value;

                            IdentExp identLeft = binExpVal.left;
                            String sourceVal1 = identLeft.ident;
                            Exp leftExp = identLeft;
                            while(Utils.containsExpInSet(values, sourceVal1)){
                                leftExp = Utils.getExpFromSet(values, sourceVal1);
                                if(leftExp instanceof IdentExp){
                                    identLeft = (IdentExp)leftExp;
                                    sourceVal1 = identLeft.ident;
                                } else {
                                    break;
                                }
                            }
    
                            IdentExp identRight = (IdentExp)binExpVal.right;
                            String sourceVal2 = identRight.ident;
                            Exp rightExp = identRight;
                            while(Utils.containsExpInSet(values, sourceVal2)){
                                rightExp = Utils.getExpFromSet(values, sourceVal2);
                                if(rightExp instanceof IdentExp){
                                    identRight = (IdentExp)rightExp;
                                    sourceVal2 = identRight.ident;
                                } else {
                                    break;
                                }
                            }

                            if(leftExp.isConstant() && rightExp.isConstant()){
                                switch(binExpVal.op){
                                    case EQ:
                                        Object leftValEq = ConversionUtils.getValue(leftExp);
                                        Object rightValEq = ConversionUtils.getValue(rightExp);
                                        Object resultEq = OpUtil.equal(leftValEq, rightValEq);
                                        varICode.value = ConversionUtils.valueToExp(resultEq);
                                        changes = true;
                                        break;
                                    case NE:
                                        Object leftValNe = ConversionUtils.getValue(leftExp);
                                        Object rightValNe = ConversionUtils.getValue(rightExp);
                                        Object resultNe = OpUtil.notEqual(leftValNe, rightValNe);
                                        varICode.value = ConversionUtils.valueToExp(resultNe);
                                        changes = true;
                                        break;
                                    case IADD:
                                        Object leftValueIAdd = ConversionUtils.getValue(leftExp);
                                        Object rightValueIAdd = ConversionUtils.getValue(rightExp);
                                        Object resultIAdd = OpUtil.iAdd(leftValueIAdd, rightValueIAdd);
                                        varICode.value = ConversionUtils.valueToExp(resultIAdd);
                                        changes = true;
                                        break;
                                    case ISUB:
                                        Object leftValueISub = ConversionUtils.getValue(leftExp);
                                        Object rightValueISub = ConversionUtils.getValue(rightExp);
                                        Object resultISub = OpUtil.iSub(leftValueISub, rightValueISub);
                                        varICode.value = ConversionUtils.valueToExp(resultISub);
                                        changes = true;
                                        break;
                                    case IMUL:
                                        Object leftValueIMul = ConversionUtils.getValue(leftExp);
                                        Object rightValueIMul = ConversionUtils.getValue(rightExp);
                                        Object resultIMul = OpUtil.iMul(leftValueIMul, rightValueIMul);
                                        varICode.value = ConversionUtils.valueToExp(resultIMul);
                                        changes = true;
                                        break;
                                    case IDIV:
                                        Object leftValueIDiv = ConversionUtils.getValue(leftExp);
                                        Object rightValueIDiv = ConversionUtils.getValue(rightExp);
                                        Object resultIDiv = OpUtil.iDiv(leftValueIDiv, rightValueIDiv);
                                        varICode.value = ConversionUtils.valueToExp(resultIDiv);
                                        changes = true;
                                        break;
                                    case IMOD:
                                        Object leftValueIMod = ConversionUtils.getValue(leftExp);
                                        Object rightValueIMod = ConversionUtils.getValue(rightExp);
                                        Object resultIMod = OpUtil.iMod(leftValueIMod, rightValueIMod);
                                        varICode.value = ConversionUtils.valueToExp(resultIMod);
                                        changes = true;
                                        break;
                                    case ILSHIFT:
                                        Object leftValueILShift = ConversionUtils.getValue(leftExp);
                                        Object rightValueILShift = ConversionUtils.getValue(rightExp);
                                        Object resultILShift = OpUtil.leftShift(leftValueILShift, rightValueILShift);
                                        varICode.value = ConversionUtils.valueToExp(resultILShift);
                                        changes = true;
                                        break;
                                    case IRSHIFT:
                                        Object leftValueIRShift = ConversionUtils.getValue(leftExp);
                                        Object rightValueIRShift = ConversionUtils.getValue(rightExp);
                                        Object resultIRShift = OpUtil.rightShift(leftValueIRShift, rightValueIRShift);
                                        varICode.value = ConversionUtils.valueToExp(resultIRShift);
                                        changes = true;
                                        break;
                                    case IAND: 
                                        Object leftValueIAnd = ConversionUtils.getValue(leftExp);
                                        Object rightValueIAnd = ConversionUtils.getValue(rightExp);
                                        Object resultIAnd = OpUtil.bitwiseAnd(leftValueIAnd, rightValueIAnd);
                                        varICode.value = ConversionUtils.valueToExp(resultIAnd);
                                        changes = true;
                                        break;
                                    case IOR:
                                        Object leftValueIOr = ConversionUtils.getValue(leftExp);
                                        Object rightValueIOr = ConversionUtils.getValue(rightExp);
                                        Object resultIOr = OpUtil.bitwiseOr(leftValueIOr, rightValueIOr);
                                        varICode.value = ConversionUtils.valueToExp(resultIOr);
                                        changes = true;
                                        break;
                                    case IXOR:
                                        Object leftValueXOr = ConversionUtils.getValue(leftExp);
                                        Object rightValueXOr = ConversionUtils.getValue(rightExp);
                                        Object resultXor = OpUtil.bitwiseXor(leftValueXOr, rightValueXOr);
                                        varICode.value = ConversionUtils.valueToExp(resultXor);
                                        changes = true;
                                        break;
                                    case GE:
                                        Object leftValueGe = ConversionUtils.getValue(leftExp);
                                        Object rightValueGe = ConversionUtils.getValue(rightExp);
                                        Object resultValueGe = OpUtil.greaterThanOrEqualTo(leftValueGe, rightValueGe);
                                        varICode.value = ConversionUtils.valueToExp(resultValueGe);
                                        changes = true;
                                        break;
                                    case GT:
                                        Object leftValueGt = ConversionUtils.getValue(leftExp);
                                        Object rightValueGt = ConversionUtils.getValue(rightExp);
                                        Object resultValueGt = OpUtil.greaterThan(leftValueGt, rightValueGt);
                                        varICode.value = ConversionUtils.valueToExp(resultValueGt);
                                        changes = true;
                                        break;
                                    case LE:
                                        Object leftValueLe = ConversionUtils.getValue(leftExp);
                                        Object rightValueLe = ConversionUtils.getValue(rightExp);
                                        Object resultLe = OpUtil.lessThanOrEqualTo(leftValueLe, rightValueLe);
                                        varICode.value = ConversionUtils.valueToExp(resultLe);
                                        changes = true;
                                        break;
                                    case LT:
                                        Object leftValueLt = ConversionUtils.getValue(leftExp);
                                        Object rightValueLt = ConversionUtils.getValue(rightExp);
                                        Object resultLt = OpUtil.lessThan(leftValueLt, rightValueLt);
                                        varICode.value = ConversionUtils.valueToExp(resultLt);
                                        changes = true;
                                        break;
                                    case LAND:
                                        Object leftValueLand = ConversionUtils.getValue(leftExp);
                                        Object rightValueLand = ConversionUtils.getValue(rightExp);
                                        Object resultLand = OpUtil.and(leftValueLand, rightValueLand);
                                        varICode.value = ConversionUtils.valueToExp(resultLand);
                                        changes = true;
                                        break;
                                    case LOR:
                                        Object leftValueLor = ConversionUtils.getValue(leftExp);
                                        Object rightValueLor = ConversionUtils.getValue(rightExp);
                                        Object resultLor = OpUtil.or(leftValueLor, rightValueLor);
                                        varICode.value = ConversionUtils.valueToExp(resultLor);
                                        changes = true;
                                        break;
                                    default:
                                        throw new OptimizerException(icode.getClass().getEnclosingMethod().getName(), "UnExpected binary operation found when optomizing expression " + varICode.value);
                                }
                            }
                        } else if(varICode.value instanceof UnExp){
                            UnExp unExpVal = (UnExp)varICode.value;
    
                            IdentExp identRight = unExpVal.right;
                            String sourceVal = identRight.ident;
                            Exp rightExp = identRight;
                            while(Utils.containsExpInSet(values, sourceVal)){
                                rightExp = Utils.getExpFromSet(values, sourceVal);
                                if(rightExp instanceof IdentExp){
                                    identRight = (IdentExp)rightExp;
                                    sourceVal = identRight.ident;
                                    changes = true;
                                } else {
                                    break;
                                }
                            }

                            if(rightExp.isConstant()){
                                switch(unExpVal.op){
                                    case INEG:
                                        Object rightValINeg = ConversionUtils.getValue(rightExp);
                                        Object resultINeg = OpUtil.iNegate(rightValINeg);
                                        varICode.value = ConversionUtils.valueToExp(resultINeg);
                                        changes = true;
                                        break;
                                    case INOT:
                                        Object rightValueINot = ConversionUtils.getValue(rightExp);
                                        Object resultINot = OpUtil.bitwiseNot(rightValueINot);
                                        varICode.value = ConversionUtils.valueToExp(resultINot);
                                        changes = true;
                                        break;
                                    case BNOT:
                                        Object rightValueBNot = ConversionUtils.getValue(rightExp);
                                        Object resultBNot = OpUtil.not(rightValueBNot);
                                        varICode.value = ConversionUtils.valueToExp(resultBNot);
                                        changes = true;
                                        break;
                                    default:
                                        throw new OptimizerException(rightExp.getClass().getEnclosingMethod().getName(), "Error invalid operation in " + unExpVal);
                                }
                            }
                        }
                    } else if(icode instanceof Def){
                        Def varICode = (Def)icode;
                        if(varICode.val instanceof IdentExp){
                            IdentExp identVal = (IdentExp)varICode.val;
                            String sourceVal = identVal.ident;
                            while(Utils.containsExpInSet(values, sourceVal)){
                                Exp rightHandSide = Utils.getExpFromSet(values, sourceVal);
                                if(rightHandSide instanceof IdentExp){
                                    identVal = (IdentExp)rightHandSide;
                                    sourceVal = identVal.ident;
                                } else if(rightHandSide.isConstant()){
                                    varICode.val = rightHandSide;
                                    changes = true;
                                    break;
                                } else {
                                    break;
                                }
                            }
                        } else if(varICode.val instanceof BinExp){
                            BinExp binExpVal = (BinExp)varICode.val;

                            IdentExp identLeft = binExpVal.left;
                            String sourceVal1 = identLeft.ident;
                            Exp leftExp = identLeft;
                            while(Utils.containsExpInSet(values, sourceVal1)){
                                leftExp = Utils.getExpFromSet(values, sourceVal1);
                                if(leftExp instanceof IdentExp){
                                    identLeft = (IdentExp)leftExp;
                                    sourceVal1 = identLeft.ident;
                                } else {
                                    break;
                                }
                            }
    
                            IdentExp identRight = (IdentExp)binExpVal.right;
                            String sourceVal2 = identRight.ident;
                            Exp rightExp = identRight;
                            while(Utils.containsExpInSet(values, sourceVal2)){
                                rightExp = Utils.getExpFromSet(values, sourceVal2);
                                if(rightExp instanceof IdentExp){
                                    identRight = (IdentExp)rightExp;
                                    sourceVal2 = identRight.ident;
                                } else {
                                    break;
                                }
                            }

                            if(leftExp.isConstant() && rightExp.isConstant()){
                                switch(binExpVal.op){
                                    case EQ:
                                        Object leftValEq = ConversionUtils.getValue(leftExp);
                                        Object rightValEq = ConversionUtils.getValue(rightExp);
                                        Object resultEq = OpUtil.equal(leftValEq, rightValEq);
                                        varICode.val = ConversionUtils.valueToExp(resultEq);
                                        changes = true;
                                        break;
                                    case NE:
                                        Object leftValNe = ConversionUtils.getValue(leftExp);
                                        Object rightValNe = ConversionUtils.getValue(rightExp);
                                        Object resultNe = OpUtil.notEqual(leftValNe, rightValNe);
                                        varICode.val = ConversionUtils.valueToExp(resultNe);
                                        changes = true;
                                        break;
                                    case IADD:
                                        Object leftValueIAdd = ConversionUtils.getValue(leftExp);
                                        Object rightValueIAdd = ConversionUtils.getValue(rightExp);
                                        Object resultIAdd = OpUtil.iAdd(leftValueIAdd, rightValueIAdd);
                                        varICode.val = ConversionUtils.valueToExp(resultIAdd);
                                        changes = true;
                                        break;
                                    case ISUB:
                                        Object leftValueISub = ConversionUtils.getValue(leftExp);
                                        Object rightValueISub = ConversionUtils.getValue(rightExp);
                                        Object resultISub = OpUtil.iSub(leftValueISub, rightValueISub);
                                        varICode.val = ConversionUtils.valueToExp(resultISub);
                                        changes = true;
                                        break;
                                    case IMUL:
                                        Object leftValueIMul = ConversionUtils.getValue(leftExp);
                                        Object rightValueIMul = ConversionUtils.getValue(rightExp);
                                        Object resultIMul = OpUtil.iMul(leftValueIMul, rightValueIMul);
                                        varICode.val = ConversionUtils.valueToExp(resultIMul);
                                        changes = true;
                                        break;
                                    case IDIV:
                                        Object leftValueIDiv = ConversionUtils.getValue(leftExp);
                                        Object rightValueIDiv = ConversionUtils.getValue(rightExp);
                                        Object resultIDiv = OpUtil.iDiv(leftValueIDiv, rightValueIDiv);
                                        varICode.val = ConversionUtils.valueToExp(resultIDiv);
                                        changes = true;
                                        break;
                                    case IMOD:
                                        Object leftValueIMod = ConversionUtils.getValue(leftExp);
                                        Object rightValueIMod = ConversionUtils.getValue(rightExp);
                                        Object resultIMod = OpUtil.iMod(leftValueIMod, rightValueIMod);
                                        varICode.val = ConversionUtils.valueToExp(resultIMod);
                                        changes = true;
                                        break;
                                    case ILSHIFT:
                                        Object leftValueILShift = ConversionUtils.getValue(leftExp);
                                        Object rightValueILShift = ConversionUtils.getValue(rightExp);
                                        Object resultILShift = OpUtil.leftShift(leftValueILShift, rightValueILShift);
                                        varICode.val = ConversionUtils.valueToExp(resultILShift);
                                        changes = true;
                                        break;
                                    case IRSHIFT:
                                        Object leftValueIRShift = ConversionUtils.getValue(leftExp);
                                        Object rightValueIRShift = ConversionUtils.getValue(rightExp);
                                        Object resultIRShift = OpUtil.rightShift(leftValueIRShift, rightValueIRShift);
                                        varICode.val = ConversionUtils.valueToExp(resultIRShift);
                                        changes = true;
                                        break;
                                    case IAND: 
                                        Object leftValueIAnd = ConversionUtils.getValue(leftExp);
                                        Object rightValueIAnd = ConversionUtils.getValue(rightExp);
                                        Object resultIAnd = OpUtil.bitwiseAnd(leftValueIAnd, rightValueIAnd);
                                        varICode.val = ConversionUtils.valueToExp(resultIAnd);
                                        changes = true;
                                        break;
                                    case IOR:
                                        Object leftValueIOr = ConversionUtils.getValue(leftExp);
                                        Object rightValueIOr = ConversionUtils.getValue(rightExp);
                                        Object resultIOr = OpUtil.bitwiseOr(leftValueIOr, rightValueIOr);
                                        varICode.val = ConversionUtils.valueToExp(resultIOr);
                                        changes = true;
                                        break;
                                    case IXOR:
                                        Object leftValueXOr = ConversionUtils.getValue(leftExp);
                                        Object rightValueXOr = ConversionUtils.getValue(rightExp);
                                        Object resultXor = OpUtil.bitwiseXor(leftValueXOr, rightValueXOr);
                                        varICode.val = ConversionUtils.valueToExp(resultXor);
                                        changes = true;
                                        break;
                                    case GE:
                                        Object leftValueGe = ConversionUtils.getValue(leftExp);
                                        Object rightValueGe = ConversionUtils.getValue(rightExp);
                                        Object resultValueGe = OpUtil.greaterThanOrEqualTo(leftValueGe, rightValueGe);
                                        varICode.val = ConversionUtils.valueToExp(resultValueGe);
                                        changes = true;
                                        break;
                                    case GT:
                                        Object leftValueGt = ConversionUtils.getValue(leftExp);
                                        Object rightValueGt = ConversionUtils.getValue(rightExp);
                                        Object resultValueGt = OpUtil.greaterThan(leftValueGt, rightValueGt);
                                        varICode.val = ConversionUtils.valueToExp(resultValueGt);
                                        changes = true;
                                        break;
                                    case LE:
                                        Object leftValueLe = ConversionUtils.getValue(leftExp);
                                        Object rightValueLe = ConversionUtils.getValue(rightExp);
                                        Object resultLe = OpUtil.lessThanOrEqualTo(leftValueLe, rightValueLe);
                                        varICode.val = ConversionUtils.valueToExp(resultLe);
                                        changes = true;
                                        break;
                                    case LT:
                                        Object leftValueLt = ConversionUtils.getValue(leftExp);
                                        Object rightValueLt = ConversionUtils.getValue(rightExp);
                                        Object resultLt = OpUtil.lessThan(leftValueLt, rightValueLt);
                                        varICode.val = ConversionUtils.valueToExp(resultLt);
                                        changes = true;
                                        break;
                                    case LAND:
                                        Object leftValueLand = ConversionUtils.getValue(leftExp);
                                        Object rightValueLand = ConversionUtils.getValue(rightExp);
                                        Object resultLand = OpUtil.and(leftValueLand, rightValueLand);
                                        varICode.val = ConversionUtils.valueToExp(resultLand);
                                        changes = true;
                                        break;
                                    case LOR:
                                        Object leftValueLor = ConversionUtils.getValue(leftExp);
                                        Object rightValueLor = ConversionUtils.getValue(rightExp);
                                        Object resultLor = OpUtil.or(leftValueLor, rightValueLor);
                                        varICode.val = ConversionUtils.valueToExp(resultLor);
                                        changes = true;
                                        break;
                                    default:
                                        throw new OptimizerException(icode.getClass().getEnclosingMethod().getName(), "UnExpected binary operation found when optomizing expression " + varICode.val);
                                }
                            }
                        } else if(varICode.val instanceof UnExp){
                            UnExp unExpVal = (UnExp)varICode.val;
    
                            IdentExp identRight = unExpVal.right;
                            String sourceVal = identRight.ident;
                            Exp rightExp = identRight;
                            while(Utils.containsExpInSet(values, sourceVal)){
                                rightExp = Utils.getExpFromSet(values, sourceVal);
                                if(rightExp instanceof IdentExp){
                                    identRight = (IdentExp)rightExp;
                                    sourceVal = identRight.ident;
                                } else {
                                    break;
                                }
                            }

                            if(rightExp.isConstant()){
                                switch(unExpVal.op){
                                    case INEG:
                                        Object rightValINeg = ConversionUtils.getValue(rightExp);
                                        Object resultINeg = OpUtil.iNegate(rightValINeg);
                                        varICode.val = ConversionUtils.valueToExp(resultINeg);
                                        changes = true;
                                        break;
                                    case INOT:
                                        Object rightValueINot = ConversionUtils.getValue(rightExp);
                                        Object resultINot = OpUtil.bitwiseNot(rightValueINot);
                                        varICode.val = ConversionUtils.valueToExp(resultINot);
                                        changes = true;
                                        break;
                                    case BNOT:
                                        Object rightValueBNot = ConversionUtils.getValue(rightExp);
                                        Object resultBNot = OpUtil.not(rightValueBNot);
                                        varICode.val = ConversionUtils.valueToExp(resultBNot);
                                        changes = true;
                                        break;
                                    default:
                                        throw new OptimizerException(rightExp.getClass().getEnclosingMethod().getName(), "Error invalid operation in " + unExpVal);
                                }
                            }
                        }
                    }
                }
            }
            cleanUpOptimization(OptName.CONSTANT_PROPOGATION);
        }
    }
}
