package io.github.H20man13.DeClan.main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.RuntimeErrorException;

import edu.depauw.declan.model.SymbolTable;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.analysis.AnticipatedExpressionsAnalysis;
import io.github.H20man13.DeClan.common.analysis.AvailableExpressionsAnalysis;
import io.github.H20man13.DeClan.common.analysis.ConstantPropogationAnalysis;
import io.github.H20man13.DeClan.common.analysis.LiveVariableAnalysis;
import io.github.H20man13.DeClan.common.analysis.PostponableExpressionsAnalysis;
import io.github.H20man13.DeClan.common.analysis.UsedExpressionAnalysis;
import io.github.H20man13.DeClan.common.dag.DagGraph;
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
import io.github.H20man13.DeClan.common.util.Utils;

public class MyOptimizer {
    private Prog intermediateCode;
    private FlowGraph globalFlowGraph;
    private AnticipatedExpressionsAnalysis anticipatedAnal;
    private AvailableExpressionsAnalysis availableAnal;
    private PostponableExpressionsAnalysis postponableAnal;
    private UsedExpressionAnalysis usedAnal;
    private ConstantPropogationAnalysis propAnal;
    private LiveVariableAnalysis liveAnal;
    private Map<ICode, Environment<String, LiveInfo>> livelinessInformation;
    private Map<FlowGraphNode, Set<Exp>> latest;
    private Map<FlowGraphNode, Set<Exp>> earliest;
    private Map<ICode, Set<Exp>> used;
    private Set<Exp> globalFlowSet;

    private enum OptName{
        COMMON_SUB_EXPRESSION_ELIMINATION,
        CONSTANT_PROPOGATION,
        DEAD_CODE_ELIMINATION
    }

    public MyOptimizer(Prog intermediateCode){
        this.intermediateCode = intermediateCode;
        this.latest = new HashMap<FlowGraphNode, Set<Exp>>();
        this.earliest = new HashMap<FlowGraphNode, Set<Exp>>();
        this.used = new HashMap<ICode, Set<Exp>>();
        this.globalFlowSet = new HashSet<Exp>();
        this.livelinessInformation = new HashMap<ICode, Environment<String, LiveInfo>>();
        this.globalFlowGraph = null;
        this.anticipatedAnal = null;
        this.availableAnal = null;
        this.postponableAnal = null;
        this.usedAnal = null;
        this.propAnal = null;
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
        for(BlockNode node : nodes){
            BasicBlock block = node.getBlock();
            if(Utils.endOfBlockIsJump(block)){
                ICode lastCode = block.getIcode().get(block.getIcode().size() - 1);
                if(lastCode instanceof Call){
                    Call lastProc = (Call)lastCode;
                    BlockNode labeledNode = functionBlocks.get(lastProc.pname);
                    if(labeledNode != null){
                        node.addSuccessor(labeledNode);
                        labeledNode.addPredecessor(node);
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

    private List<BasicBlock> buildBlocks(){
        List<BasicBlock> blocks = new LinkedList<BasicBlock>();
        List<Integer> firsts = findFirstsInICode(this.intermediateCode.getICode());
        int firstSize = firsts.size();
        for(int leaderIndex = 0; leaderIndex < firstSize; leaderIndex++){
            int beginIndex = firsts.get(leaderIndex);
            int endIndex;
            if(leaderIndex + 1 < firstSize){
                endIndex = firsts.get(leaderIndex + 1);
            } else {
                endIndex = intermediateCode.getSize();
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


        if(nodeList.size() == 0){
            throw new OptimizerException(nodeList.getClass().getEnclosingMethod().getName(), "Data section and the Code Section are empty!!!");
        }

        EntryNode entry;
        ExitNode exit;

        entry = new EntryNode(nodeList.get(0));
        exit = new ExitNode(nodeList.get(nodeList.size() - 1));
        FlowGraph flowGraph = new FlowGraph(entry, nodeList, exit);
        this.globalFlowGraph = flowGraph;
    }

    private void updateICodeLivelinessInformation(Environment<String, LiveInfo> symbolTable, ICode icode, int x){
        if(icode instanceof Assign){
            Assign icodeAssign = (Assign)icode;
            symbolTable.addEntry(icodeAssign.place, new LiveInfo(false, x));
            if(icodeAssign.value instanceof IdentExp){
                IdentExp identExp = (IdentExp)icodeAssign.value;
                symbolTable.addEntry(identExp.ident, new LiveInfo(true, x));
            } else if(icodeAssign.value instanceof BinExp){
                BinExp binExp = (BinExp)icodeAssign.value;

                if(binExp.right instanceof IdentExp){
                    IdentExp rightExp = (IdentExp)binExp.right;
                    symbolTable.addEntry(rightExp.ident, new LiveInfo(true, x));
                }

                if(binExp.left instanceof IdentExp){
                    IdentExp leftExp = (IdentExp)binExp.left;
                    symbolTable.addEntry(leftExp.ident, new LiveInfo(true, x));
                }
            } else if(icodeAssign.value instanceof UnExp){
                UnExp unExp = (UnExp)icodeAssign.value;

                if(unExp.right instanceof IdentExp){
                    IdentExp rightExp = (IdentExp)unExp.right;
                    symbolTable.addEntry(rightExp.ident, new LiveInfo(true, x));
                }
            }
        } else if(icode instanceof If){
            If ifStat = (If)icode;

            BinExp binExp = ifStat.exp;

            if(binExp.left instanceof IdentExp){
                IdentExp leftIdent = (IdentExp)binExp.left;
                symbolTable.addEntry(leftIdent.ident, new LiveInfo(true, x));
            }

            if(binExp.right instanceof IdentExp){
                IdentExp rightIdent = (IdentExp)binExp.right;
                symbolTable.addEntry(rightIdent.ident, new LiveInfo(true, x));
            }
        } else if(icode instanceof Call){
            Call icodeCall = (Call)icode;
            for(Def param : icodeCall.params){
                if(param.val instanceof IdentExp){
                    IdentExp paramVal = (IdentExp)param.val;
                    symbolTable.addEntry(paramVal.ident, new LiveInfo(true, x));
                } else if(param.val instanceof UnExp){
                    UnExp exp = (UnExp)param.val;
                    symbolTable.addEntry(exp.right.ident, new LiveInfo(true, x));
                } else if(param.val instanceof BinExp){
                    BinExp exp = (BinExp)param.val;
                    symbolTable.addEntry(exp.left.ident, new LiveInfo(true, x));
                    symbolTable.addEntry(exp.right.ident, new LiveInfo(true, x));
                }
            }
        }
        livelinessInformation.put(icode, symbolTable.copy());
    }

    private void updateCodeLivelinessInformation(Environment<String, LiveInfo> symbolTable){
        int size = intermediateCode.getSize();
        for(int i = size - 1; i >= 0; i--){
            ICode icode = intermediateCode.getInstruction(i);
            updateICodeLivelinessInformation(symbolTable, icode, i);
        }
    }

    private void updateLiveLinessInformation() {
        Environment<String, LiveInfo> symbolTable = new Environment<String, LiveInfo>();
        symbolTable.addScope();
        updateCodeLivelinessInformation(symbolTable);
    }

    public void rebuildFromFlowGraph(){
        if(this.globalFlowGraph != null){
            this.intermediateCode = new Prog();
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
            rebuildFromFlowGraph();
            cleanUpOptimization(OptName.COMMON_SUB_EXPRESSION_ELIMINATION);
    }

    private void cleanUpOptimization(OptName name){
        switch(name){
            case COMMON_SUB_EXPRESSION_ELIMINATION:
                this.globalFlowGraph = null;
                this.livelinessInformation = new HashMap<ICode, Environment<String, LiveInfo>>();
                break;
            case CONSTANT_PROPOGATION:
                this.propAnal = null;
                this.globalFlowGraph = null;
                break;
            case DEAD_CODE_ELIMINATION:
                this.liveAnal = null;
                this.globalFlowGraph = null;
                break;
        }
    }

    private void setUpOptimization(OptName name){
        switch(name){
            case COMMON_SUB_EXPRESSION_ELIMINATION:
                updateLiveLinessInformation();
                buildFlowGraph();
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
        int initialListSize = initialList.size();

        if(initialListSize > 0){
            ICode firstElem = initialList.get(0);
            if(firstElem instanceof Label){
                result.add(firstElem);
            }
        }

        Environment<String, LiveInfo> liveAtEndOfBlock = this.livelinessInformation.get(initialList.get(initialListSize - 1));

        for(DagNode node : dag.getDagNodes()){
            
            List<String> isAlive = new LinkedList<String>();
            for(String identifier: node.getIdentifiers()){
                if(liveAtEndOfBlock.entryExists(identifier)){
                    LiveInfo info = liveAtEndOfBlock.getEntry(identifier);
                    if(info.isAlive){
                        isAlive.add(identifier);
                    }
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

                    result.add(new Assign(ConversionUtils.dagScopeTypeToAssignScope(scope), identifier, binExp, ConversionUtils.dagValueTypeToAssignType(type)));
                } else if(node2.getChildren().size() == 1) {
                    //Its a Unary Operation
                    DagOperationNode.Op op = node2.getOperator();
                    UnExp.Operator op2 = ConversionUtils.getUnOp(op);

                    DagNode child1 = node2.getChildren().get(0);

                    IdentExp identifier1 = getIdentifier(child1, liveAtEndOfBlock);

                    UnExp unExp = new UnExp(op2, identifier1);

                    result.add(new Assign(ConversionUtils.dagScopeTypeToAssignScope(scope), identifier, unExp, ConversionUtils.dagValueTypeToAssignType(type)));
                }
            } else if(node instanceof DagValueNode){
                DagValueNode valNode = (DagValueNode)node;
                Object value = valNode.getValue();
                Exp resultExp = ConversionUtils.valueToExp(value);
                result.add(new Assign(ConversionUtils.dagScopeTypeToAssignScope(scope), identifier, resultExp, ConversionUtils.dagValueTypeToAssignType(type)));
            } else if(node instanceof DagVariableNode){
                DagVariableNode varNode = (DagVariableNode)node;
                DagNode child = varNode.getChild();
                IdentExp identifier1 = getIdentifier(child, liveAtEndOfBlock);
                result.add(new Assign(ConversionUtils.dagScopeTypeToAssignScope(scope), identifier, identifier1, ConversionUtils.dagValueTypeToAssignType(type)));
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
                result.add(new Assign(ConversionUtils.dagScopeTypeToAssignScope(scope), ident, ident1, ConversionUtils.dagValueTypeToAssignType(type)));
            }
        }

        if(initialList.size() > 0){
            int size = initialList.size();
            ICode lastElem = initialList.get(size - 1);
            if(lastElem.isBranch()){
                result.add(lastElem);
            }
        }
        
        block.getBlock().setICode(result);
    }

    private static IdentExp getIdentifier(DagNode node, Environment<String, LiveInfo> table){
        List<String> identifiers = node.getIdentifiers();
        ScopeType scope = node.getScopeType();
        for(String identifier : identifiers){
            if(table.entryExists(identifier)){
                LiveInfo life = table.getEntry(identifier);
                if(life.isAlive){
                    return new IdentExp(ConversionUtils.dagScopeTypeToAssignScope(scope), identifier);
                }
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

                    DagNode newNode = Utils.createBinaryNode(assignICode.getScope(), exp.op, assignICode.place, left, right);

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

                    DagNode newNode = Utils.createUnaryNode(assignICode.getScope(), exp.op, assignICode.place, right);

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

                    DagNode newNode = factory.createDefaultVariableNode(assignICode.place, right, assignICode.getType());

                    DagNode exists = dag.getDagNode(newNode);
                    if(exists == null || !optimized){
                        dag.addDagNode(newNode);
                    } else {
                        exists.addIdentifier(assignICode.place);
                    }
                } else if(assignICode.value instanceof BoolExp){
                    BoolExp boolICode = (BoolExp)assignICode.value;
                    DagNode newNode = factory.createBooleanNode(assignICode.getScope(), assignICode.place, boolICode.trueFalse);
                    dag.addDagNode(newNode);
                } else if(assignICode.value instanceof IntExp){
                    IntExp intICode = (IntExp)assignICode.value;
                    DagNode newNode = factory.createIntNode(assignICode.getScope(), assignICode.place, intICode.value);
                    dag.addDagNode(newNode);
                } else if(assignICode.value instanceof RealExp){
                    RealExp realICode = (RealExp)assignICode.value;
                    DagNode newNode = factory.createRealNode(assignICode.getScope(), assignICode.place, realICode.realValue);
                    dag.addDagNode(newNode);
                } else if(assignICode.value instanceof StrExp){
                    StrExp strICode = (StrExp)assignICode.value;
                    DagNode newNode = factory.createStringNode(assignICode.getScope(), assignICode.place, strICode.value);
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

    private void buildGlobalExpressionSemilattice(){
        for(BlockNode block : this.globalFlowGraph.getBlocks()){
            for(ICode icode : block.getICode()){
                if(icode instanceof Assign){
                    Assign ident = (Assign)icode;
                    if(!Utils.setContainsExp(this.globalFlowSet, ident.value)){
                        this.globalFlowSet.add(ident.value);
                    }
                }
            }
        }
    }

    private void buildEarliest(){
        this.earliest.put(this.globalFlowGraph.getExit(), new HashSet<Exp>());
        this.earliest.put(this.globalFlowGraph.getEntry(), new HashSet<Exp>());
        for(BlockNode block : this.globalFlowGraph.getBlocks()){
            Set<Exp> earliest = new HashSet<Exp>();
            for(ICode icode : block.getICode()){
                earliest.addAll(this.anticipatedAnal.getInstructionInputSet(icode));
                earliest.removeAll(this.availableAnal.getInstructionInputSet(icode));
            }
            this.earliest.put(block, earliest);
        }
    }

    private void buildUsedExpressions(){
        for(BlockNode block : this.globalFlowGraph.getBlocks()){
            for(ICode icode : block.getICode()){
                Set<Exp> blockUsed = new HashSet<Exp>();
                if(icode instanceof Assign){
                    Assign utilICode = (Assign)icode;
                    blockUsed.add(utilICode.value);
                } else if(icode instanceof If){
                    If ifICode = (If)icode;
                    blockUsed.add(ifICode.exp);
                }
                this.used.put(icode, blockUsed);
            }
        }
    }

    private void runAvailableExpressionAnalysis(){
        this.availableAnal = new AvailableExpressionsAnalysis(this.globalFlowGraph, this.globalFlowSet);
        this.availableAnal.run();
    }

    private void runAnticipatedExpressionAnalysis(){
        this.anticipatedAnal = new AnticipatedExpressionsAnalysis(this.globalFlowGraph, this.globalFlowSet);    
        this.anticipatedAnal.run();
    }

    private void runPosponableExpressionAnalysis(){
        this.postponableAnal = new PostponableExpressionsAnalysis(this.globalFlowGraph, this.globalFlowSet, this.earliest, this.used);
        this.postponableAnal.run();
    }

    private void buildLatest(){
        for(BlockNode block : this.globalFlowGraph.getBlocks()){
            Set<Exp> latest = new HashSet<Exp>();
            
            Map<FlowGraphNode, Set<Exp>> earliestUnionPosponableSaved = new HashMap<FlowGraphNode, Set<Exp>>();
            for (FlowGraphNode sucessor : block.getSuccessors()){
                Set<Exp> earliestUnionPosponable = new HashSet<Exp>();

                Set<Exp> earliestOfSuccessor = earliest.get(sucessor);
                earliestUnionPosponable.addAll(earliestOfSuccessor);

                Set<Exp> postponableInput = postponableAnal.getBlockInputSet(sucessor);
                earliestUnionPosponable.addAll(postponableInput);

                earliestUnionPosponableSaved.put(sucessor, earliestUnionPosponable);
            }

            Set<Exp> intersectionOfAllSucessors = new HashSet<Exp>();
            intersectionOfAllSucessors.addAll(this.globalFlowSet);

            for(FlowGraphNode sucessor : block.getSuccessors()){
                Set<Exp> earliestUnionPosponable = earliestUnionPosponableSaved.get(sucessor);
                intersectionOfAllSucessors.retainAll(earliestUnionPosponable);
            }

            Set<Exp> usedUnionCompliment = new HashSet<Exp>();

            Set<Exp> compliment = new HashSet<Exp>();
            compliment.addAll(this.globalFlowSet);
            compliment.removeAll(intersectionOfAllSucessors);

            for(ICode icode : block.getICode()){
                usedUnionCompliment.addAll(this.used.get(icode));
            }
            usedUnionCompliment.addAll(compliment);

            Set<Exp> earliestUnionPosponable = new HashSet<Exp>();
            earliestUnionPosponable.addAll(this.earliest.get(block));
            earliestUnionPosponable.addAll(this.postponableAnal.getBlockInputSet(block));

            latest.addAll(earliestUnionPosponable);
            latest.retainAll(usedUnionCompliment);

            this.latest.put(block, latest);
        }
    }

    private void runUsedExpressionAnalysis(){
        this.usedAnal = new UsedExpressionAnalysis(this.globalFlowGraph, this.used, this.latest);
        this.usedAnal.run();
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

    /*
    public void performPartialRedundancyElimination(){
        for(BlockNode block : this.globalFlowGraph.getBlocks()){
            Set<Exp> latestInstructionUsedOutput = new HashSet<Exp>();
            latestInstructionUsedOutput.addAll(this.latest.get(block));
            latestInstructionUsedOutput.retainAll(this.usedAnal.getBlockOutputSet(block));

            Map<Exp, String> identifierMap = new HashMap<Exp, String>();
            List<ICode> toPreAppendToBeginning = new LinkedList<ICode>();
            for(Exp latestInstructionOutput : latestInstructionUsedOutput){
                String register = gen.genNext();
                toPreAppendToBeginning.add(new Assign(register, latestInstructionOutput));
                identifierMap.put(latestInstructionOutput, register);
            }

            Set<Exp> notLatest = new HashSet<Exp>();
            notLatest.addAll(this.globalFlowSet);
            notLatest.removeAll(this.latest.get(block));

            Set<Exp> notLatestUnionUsedOut = new HashSet<Exp>();
            notLatestUnionUsedOut.addAll(notLatest);
            notLatestUnionUsedOut.addAll(usedAnal.getBlockOutputSet(block));

            Set<Exp> useIntersectionNotLatestUnionUsedOut = new HashSet<Exp>();

            for(ICode icode : block.getICode()){
                useIntersectionNotLatestUnionUsedOut.addAll(this.used.get(icode));
            }

            useIntersectionNotLatestUnionUsedOut.retainAll(notLatestUnionUsedOut);
            

            List<ICode> icodeList = block.getICode();
            for(int i = 0; i < icodeList.size(); i++){
                ICode elem = icodeList.get(i);
                if(elem instanceof Assign){
                    Assign assignElem = (Assign)elem;
                    String place = assignElem.place;
                    Exp codeExp = assignElem.value;
                    if(codeExp != null && place != null){
                        if(Utils.setContainsExp(useIntersectionNotLatestUnionUsedOut, codeExp)){
                            String value = identifierMap.get(codeExp);
                            if(value != null){
                                Assign var = new Assign(place, new IdentExp(value));
                                icodeList.set(i, var);
                            }
                        }
                    }
                }
            }
            
            List<ICode> resultList = new LinkedList<ICode>();
            resultList.addAll(toPreAppendToBeginning);
            resultList.addAll(icodeList);

            block.getBlock().setICode(resultList);
        }
    }
    */

    public void performDeadCodeElimination(){
        setUpOptimization(OptName.DEAD_CODE_ELIMINATION);
        for(BlockNode block : this.globalFlowGraph.getBlocks()){
            List<ICode> result = new LinkedList<ICode>();
            for(ICode icode : block.getICode()){
                if(icode instanceof Assign){
                    Assign assICode = (Assign)icode;
                    Set<String> liveVariables = this.liveAnal.getInstructionOutputSet(icode);
                    if(liveVariables.contains(assICode.place)){
                        result.add(assICode);
                    }
                } else {
                    result.add(icode);
                }
            }
            block.getBlock().setICode(result);
        }
        rebuildFromFlowGraph();
        cleanUpOptimization(OptName.DEAD_CODE_ELIMINATION);
    }

    public void performConstantPropogation(){
            setUpOptimization(OptName.CONSTANT_PROPOGATION);
            for(BlockNode block : this.globalFlowGraph.getBlocks()){
                List<ICode> icodeList = block.getICode();
                for(int i = 0; i < icodeList.size(); i++){
                    ICode icode = icodeList.get(i);
                    Set<Tuple<String, Exp>> values = this.propAnal.getInstructionInputSet(icode);
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
                                    break;
                                } else {
                                    break;
                                }
                            }
                        } else if(varICode.value instanceof BinExp){
                            BinExp binExpVal = (BinExp)varICode.value;
    
                            if(binExpVal.left instanceof IdentExp){
                                IdentExp identLeft = (IdentExp)binExpVal.left;
                                String sourceVal = identLeft.ident;
                                while(Utils.containsExpInSet(values, sourceVal)){
                                    Exp leftExp = Utils.getExpFromSet(values, sourceVal);
                                    if(leftExp instanceof IdentExp){
                                        identLeft = (IdentExp)leftExp;
                                        sourceVal = identLeft.ident;
                                    } else if(leftExp.isConstant()){
                                        binExpVal.left = leftExp;
                                        break;
                                    } else {
                                        break;
                                    }
                                }
                            }
    
                            if(binExpVal.right instanceof IdentExp){
                                IdentExp identRight = (IdentExp)binExpVal.right;
                                String sourceVal = identRight.ident;
                                while(Utils.containsExpInSet(values, sourceVal)){
                                    Exp rightHandSide = Utils.getExpFromSet(values, sourceVal);
                                    if(rightHandSide instanceof IdentExp){
                                        identRight = (IdentExp)rightHandSide;
                                        sourceVal = identRight.ident;
                                    } else if(rightHandSide.isConstant()){
                                        binExpVal.right = rightHandSide;
                                        break;
                                    } else {
                                        break;
                                    }
                                }
                            }
                        } else if(varICode.value instanceof UnExp){
                            UnExp unExpVal = (UnExp)varICode.value;
    
                            IdentExp identRight = (IdentExp)unExpVal.right;
                            String sourceVal = identRight.ident;
                            while(Utils.containsExpInSet(values, sourceVal)){
                                Exp rightHandSide = Utils.getExpFromSet(values, sourceVal);
                                if(rightHandSide instanceof IdentExp){
                                    identRight = (IdentExp)rightHandSide;
                                    sourceVal = identRight.ident;
                                } else if(rightHandSide.isConstant()){
                                    unExpVal.right = rightHandSide;
                                    break;
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            rebuildFromFlowGraph();
            cleanUpOptimization(OptName.CONSTANT_PROPOGATION);
    }
}
