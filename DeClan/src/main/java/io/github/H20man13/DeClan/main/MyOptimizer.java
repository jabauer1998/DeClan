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
import io.github.H20man13.DeClan.common.dag.DagInlineAssemblyNode;
import io.github.H20man13.DeClan.common.dag.DagNode;
import io.github.H20man13.DeClan.common.dag.DagNodeFactory;
import io.github.H20man13.DeClan.common.dag.DagOperationNode;
import io.github.H20man13.DeClan.common.dag.DagValueNode;
import io.github.H20man13.DeClan.common.dag.DagVariableNode;
import io.github.H20man13.DeClan.common.dag.DagVariableNode.VariableType;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.EntryNode;
import io.github.H20man13.DeClan.common.flow.ExitNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.flow.ProcedureEntryNode;
import io.github.H20man13.DeClan.common.flow.ProcedureExitNode;
import io.github.H20man13.DeClan.common.flow.block.BasicBlock;
import io.github.H20man13.DeClan.common.flow.block.ProcedureBeginningBlock;
import io.github.H20man13.DeClan.common.flow.block.ProcedureEndingBlock;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.Assign;
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
import io.github.H20man13.DeClan.common.icode.procedure.Call;
import io.github.H20man13.DeClan.common.icode.procedure.ExternalPlace;
import io.github.H20man13.DeClan.common.icode.procedure.InternalPlace;
import io.github.H20man13.DeClan.common.icode.procedure.ParamAssign;
import io.github.H20man13.DeClan.common.icode.procedure.Proc;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.icode.section.SymSec;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.symboltable.entry.LiveInfo;
import io.github.H20man13.DeClan.common.symboltable.entry.ProcedureEntry;
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

    public MyOptimizer(Prog intermediateCode){
        this.intermediateCode = intermediateCode;
        this.latest = new HashMap<FlowGraphNode, Set<Exp>>();
        this.earliest = new HashMap<FlowGraphNode, Set<Exp>>();
        this.used = new HashMap<ICode, Set<Exp>>();
        this.globalFlowSet = new HashSet<Exp>();
        this.livelinessInformation = new HashMap<ICode, Environment<String, LiveInfo>>();
        this.updateLiveLinessInformation();
        this.buildFlowGraph();
    }

    public LiveVariableAnalysis getLiveVariableAnalysis(){
        return this.liveAnal;
    }

    public List<BasicBlock> buildDataBlocks(){
        DataSec dataSec = intermediateCode.variables;
        List<Integer> codeFirsts = findFirstsInICode(dataSec.intermediateCode, false);
        List<BasicBlock> dataBlocks = new LinkedList<BasicBlock>();

        for(int leaderIndex = 0; leaderIndex < codeFirsts.size(); leaderIndex++){
            int beginIndex = codeFirsts.get(leaderIndex);
            int endIndex;
            if(leaderIndex + 1 < codeFirsts.size()){
                endIndex = codeFirsts.get(leaderIndex + 1);
            } else {
                endIndex = dataSec.intermediateCode.size() - 1;
            }
            List<ICode> basicBlockList = new LinkedList<ICode>();
            for(int i = beginIndex; i <= endIndex; i++){
                basicBlockList.add(dataSec.intermediateCode.get(i));
            }
            dataBlocks.add(new BasicBlock(basicBlockList));
        }
        return dataBlocks;
    }

    public List<BasicBlock> buildCodeBlocks(){
         CodeSec code = intermediateCode.code;
         List<Integer> codeFirsts = findFirstsInICode(code.intermediateCode, false);
         List<BasicBlock> codeBlocks = new LinkedList<BasicBlock>();

        for(int leaderIndex = 0; leaderIndex < codeFirsts.size(); leaderIndex++){
            int beginIndex = codeFirsts.get(leaderIndex);
            int endIndex;
            if(leaderIndex + 1 < codeFirsts.size()){
                endIndex = codeFirsts.get(leaderIndex + 1);
            } else {
                endIndex = code.intermediateCode.size() - 1;
            }
            List<ICode> basicBlockList = new LinkedList<ICode>();
            for(int i = beginIndex; i <= endIndex; i++){
                basicBlockList.add(code.intermediateCode.get(i));
            }
            codeBlocks.add(new BasicBlock(basicBlockList));
        }

        return codeBlocks;
    }

    private List<BasicBlock> buildProcedureBlocks(){
        ProcSec proc = intermediateCode.procedures;
        List<BasicBlock> procedureBlocks = new LinkedList<BasicBlock>();
        for(Proc procedure : proc.procedures){
            List<Integer> procFirsts = findFirstsInICode(procedure.instructions, true);
            List<ParamAssign> assignments = procedure.paramAssign;
            List<ICode> instructionsInBlock = new LinkedList<ICode>();
            if(procFirsts.size() > 0){
                Integer firstIndex = procFirsts.get(0);
                Integer endIndex;
                if(procFirsts.size() > 1){
                    endIndex = procFirsts.get(1) - 1;
                } else {
                    endIndex = procedure.instructions.size() - 1;
                }
                for(int i = firstIndex; i <= endIndex; i++){
                    instructionsInBlock.add(procedure.instructions.get(i));
                }
            }

            procedureBlocks.add(new ProcedureBeginningBlock(procedure.label, assignments, instructionsInBlock));
                
            for(int leaderIndex = 1; leaderIndex < procFirsts.size() - 1; leaderIndex++){
                int beginIndex = procFirsts.get(leaderIndex);
                int endIndex;
                if(leaderIndex + 1 < procFirsts.size()){
                    endIndex = procFirsts.get(leaderIndex + 1);
                } else {
                    endIndex = procedure.instructions.size() - 1;
                }
                
                List<ICode> basicBlockList = new LinkedList<ICode>();
                for(int i = beginIndex; i <= endIndex; i++){
                    basicBlockList.add(procedure.instructions.get(i));
                }

                procedureBlocks.add(new BasicBlock(basicBlockList));
            }

            if(procFirsts.size() > 1){
                int beginIndex = procFirsts.get(procFirsts.size() - 1);
                int endIndex = procedure.instructions.size() - 1;

                List<ICode> basicBlockList = new LinkedList<ICode>();
                for(int i = beginIndex; i <= endIndex; i++){
                    basicBlockList.add(procedure.instructions.get(i));
                }
                procedureBlocks.add(new ProcedureEndingBlock(basicBlockList, procedure.placement, procedure.returnStatement));
            } else {
                procedureBlocks.add(new ProcedureEndingBlock(new LinkedList<ICode>(), null, procedure.returnStatement));
            }
        }

        return procedureBlocks;
    }

    private List<BlockNode> buildDataBlockNodes(){
        List<BasicBlock> blocks = buildDataBlocks();
        List<BlockNode> dataNodes = new LinkedList<BlockNode>();
        for(BasicBlock block: blocks){
            dataNodes.add(new BlockNode(block));
        }
        return dataNodes;
    }

    private List<BlockNode> buildCodeBlockNodes(){
        List<BasicBlock> blocks = buildCodeBlocks();
        List<BlockNode> codeNodes = new LinkedList<BlockNode>();
        for(BasicBlock block: blocks){
            codeNodes.add(new BlockNode(block));
        }
        return codeNodes;
    }

    private List<BlockNode> buildProcedureBlockNodes(){
        List<BasicBlock> blocks = buildProcedureBlocks();
        List<BlockNode> blockNodes = new LinkedList<BlockNode>();
        for(BasicBlock block : blocks){
            if(block instanceof ProcedureEndingBlock){
                ProcedureEndingBlock endBlock = (ProcedureEndingBlock)block;
                blockNodes.add(new ProcedureExitNode(endBlock));
            } else if(block instanceof ProcedureBeginningBlock){
                ProcedureBeginningBlock beginBlock = (ProcedureBeginningBlock)block;
                blockNodes.add(new ProcedureEntryNode(beginBlock));
            } else {
                blockNodes.add(new BlockNode(block));
            }
        }
        return blockNodes;
    }

    private static Map<String, BlockNode> findProcedureEntryPoints(List<BlockNode> nodes){
        Map<String, BlockNode> toRet = new HashMap<String, BlockNode>();
        for(BlockNode node : nodes){
            if(node instanceof ProcedureEntryNode){
                ProcedureBeginningBlock entryBlock = (ProcedureBeginningBlock)node.getBlock();
                ProcLabel label = entryBlock.getLabel();
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
            if(node instanceof ProcedureEntryNode){
                ProcedureBeginningBlock block = (ProcedureBeginningBlock)node.getBlock();
                ProcLabel label = block.getLabel();
                boolean endingNotFound = true;
                i++;
                while(endingNotFound){
                    BlockNode nodeEnd = nodes.get(i);
                    if(nodeEnd instanceof ProcedureExitNode){
                        toRet.put(label.label, nodeEnd);
                        endingNotFound = false;
                    } else {
                        i++;
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

    private void linkUpFollowThrough(List<BlockNode> dataBlockNodes, List<BlockNode> codeSectionNodes){
        for(int i = 0; i < dataBlockNodes.size(); i++){
            BlockNode node = dataBlockNodes.get(i);
            BasicBlock block = node.getBlock();
            if(!Utils.endOfBlockIsJump(block)){
                if(i + 1 < dataBlockNodes.size()){
                    BlockNode nextNode = dataBlockNodes.get(i + 1);
                    nextNode.addPredecessor(node);
                    node.addSuccessor(nextNode);
                }
            }
        }

        if(dataBlockNodes.size() > 0){
            BlockNode lastDataNode = dataBlockNodes.get(dataBlockNodes.size() - 1);
            BasicBlock lastDataNodeBlock = lastDataNode.getBlock();
            if(!Utils.endOfBlockIsJump(lastDataNodeBlock)){
                if(codeSectionNodes.size() > 0){
                    BlockNode firstCodeNode = codeSectionNodes.get(0);
                    lastDataNode.addSuccessor(firstCodeNode);
                    firstCodeNode.addPredecessor(lastDataNode);
                }
            }
        }

        for(int i = 0; i < codeSectionNodes.size() - 1; i++){
            BlockNode node = codeSectionNodes.get(i);
            BasicBlock block = node.getBlock();
            if(!Utils.endOfBlockIsJump(block)){
                if(i + 1 < codeSectionNodes.size()){
                    BlockNode nextNode = codeSectionNodes.get(i + 1);
                    nextNode.addPredecessor(node);
                    node.addSuccessor(nextNode);
                }
            }
        }
    }

    private void buildFlowGraph() {
        List<BlockNode> dataBlockNodes = buildDataBlockNodes();
        List<BlockNode> codeBlockNodes = buildCodeBlockNodes();
        List<BlockNode> procedureBlockNodes = buildProcedureBlockNodes();
        
        Map<String, BlockNode> procedureEntryNodes = findProcedureEntryPoints(procedureBlockNodes);
        linkUpFunctionCalls(dataBlockNodes, procedureEntryNodes);
        linkUpFunctionCalls(codeBlockNodes, procedureEntryNodes);
        linkUpFunctionCalls(procedureBlockNodes, procedureEntryNodes);

        Map<String, BlockNode> codeLabeledNodes = findBranchEntryPoints(codeBlockNodes);
        linkUpJumps(codeBlockNodes, codeLabeledNodes);
        linkUpJumps(procedureBlockNodes, codeLabeledNodes);

        Map<String, BlockNode> procedureExitNodes = findProcedureExitPoints(procedureBlockNodes);
        linkUpReturns(procedureBlockNodes, procedureExitNodes);

        linkUpFollowThrough(dataBlockNodes, codeBlockNodes);

        if(dataBlockNodes.size() == 0 && codeBlockNodes.size() == 0){
            throw new RuntimeException("Data section and the Code Section are empty!!!");
        }

        EntryNode entry;
        ExitNode exit;

        int dataSize = dataBlockNodes.size();
        int codeSize = codeBlockNodes.size();
        if(dataSize > 0 && codeSize == 0){
            entry = new EntryNode(dataBlockNodes.get(0));
            exit = new ExitNode(dataBlockNodes.get(dataSize - 1));
        } else if(dataSize == 0 && codeSize > 0){
            entry = new EntryNode(codeBlockNodes.get(0));
            exit = new ExitNode(codeBlockNodes.get(codeSize - 1));
        } else {
            entry = new EntryNode(dataBlockNodes.get(0));
            exit = new ExitNode(codeBlockNodes.get(codeSize - 1));
        }
        FlowGraph flowGraph = new FlowGraph(entry, dataBlockNodes, codeBlockNodes, procedureBlockNodes, exit);
        this.globalFlowGraph = flowGraph;
    }

    private void updateProcedureLivelinessInformation(Environment<String, LiveInfo> symbolTable){
        ProcSec data = intermediateCode.procedures;
        List<Proc> procedures = data.procedures;
        int size = procedures.size();
        for(int i = size - 1; i >= 0; i--){
            Proc procedure = procedures.get(i);
            if(procedure.placement != null){
                InternalPlace returnPlacement = procedure.placement;
                symbolTable.addEntry(returnPlacement.place, new LiveInfo(false, i));
                symbolTable.addEntry(returnPlacement.retPlace, new LiveInfo(true, i));
                livelinessInformation.put(returnPlacement, symbolTable.copy());
            }

            List<ICode> procedureCode = procedure.instructions;
            int codeSize = procedureCode.size();
            for(int x = codeSize - 1; x >= 0; x--){
                ICode icode = procedureCode.get(x);
                updateICodeLivelinessInformation(symbolTable, icode, x);
            }
                

            List<ParamAssign> paramaterAssignmants = procedure.paramAssign;
            int paramAssignSize = paramaterAssignmants.size();
            for(int z = paramAssignSize - 1; z >= 0; z--){
                ParamAssign assign = paramaterAssignmants.get(z);
                symbolTable.addEntry(assign.newPlace, new LiveInfo(false, z));
                symbolTable.addEntry(assign.paramPlace, new LiveInfo(true, z));
                livelinessInformation.put(assign, symbolTable.copy());
            }
        }
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
            for(Tuple<String, String> param : ((Call)icode).params){
                symbolTable.addEntry(param.source, new LiveInfo(true, x));
            }
        } else if(icode instanceof ExternalPlace){
            ExternalPlace place = (ExternalPlace)icode;
            symbolTable.addEntry(place.retPlace, new LiveInfo(true, x));
            symbolTable.addEntry(place.place, new LiveInfo(false, x));
        }
        livelinessInformation.put(icode, symbolTable.copy());
    }

    private void updateCodeLivelinessInformation(Environment<String, LiveInfo> symbolTable){
        CodeSec code = intermediateCode.code;
        List<ICode> icodeList = code.intermediateCode;
        int size = icodeList.size();
        for(int i = size - 1; i >= 0; i--){
            ICode icode = icodeList.get(i);
            updateICodeLivelinessInformation(symbolTable, icode, i);
        }
    }

    private void updateVariableLivelinessInformation(Environment<String, LiveInfo> symbolTable){
        DataSec data = intermediateCode.variables;
        List<ICode> assignmants = data.intermediateCode;
        int size = assignmants.size();
        for(int i = size - 1; i >= 0; i--){
            ICode icode = assignmants.get(i);
            updateICodeLivelinessInformation(symbolTable, icode, i);
        }
    }

    private void updateLiveLinessInformation() {
        Environment<String, LiveInfo> symbolTable = new Environment<String, LiveInfo>();
        symbolTable.addScope();
        updateProcedureLivelinessInformation(symbolTable);
        updateCodeLivelinessInformation(symbolTable);
        updateVariableLivelinessInformation(symbolTable);
    }

    public void rebuildFromFlowGraph(){
        if(this.globalFlowGraph != null){
            SymSec symbols = intermediateCode.symbols;
            this.intermediateCode = null;
            DataSec dataSec = new DataSec();
            List<BlockNode> dataBlocks = this.globalFlowGraph.getDataBlocks();
            for(BlockNode dataBlock: dataBlocks){
                List<ICode> icodes = dataBlock.getICode();
                for(ICode icode: icodes){
                    dataSec.addInstruction(icode);
                }
            }

            CodeSec codeSec = new CodeSec();
            List<BlockNode> codeBlocks = this.globalFlowGraph.getCodeBlocks();
            for(BlockNode codeBlock: codeBlocks){
                List<ICode> icodes = codeBlock.getICode();
                for(ICode icode: icodes){
                    codeSec.addInstruction(icode);
                }
            }

            ProcSec procSec = new ProcSec();
            List<BlockNode> procBlocks = this.globalFlowGraph.getProcedureBlocks();
            int i = 0;
            int procBlockSize = procBlocks.size();
            while(i < procBlockSize){
                BlockNode blockAtStart = procBlocks.get(i);
                if(blockAtStart instanceof ProcedureEntryNode){
                    ProcedureEntryNode procedureEntry = (ProcedureEntryNode)blockAtStart;
                    ProcedureBeginningBlock procedureEntryBlock = procedureEntry.getBlock();
                    Proc newProcedure = new Proc(procedureEntryBlock.getLabel());
                    List<ParamAssign> assigns = procedureEntryBlock.getParamaterAssignmants();
                    for(ParamAssign assign: assigns){
                        newProcedure.addParamater(assign);
                    }

                    List<ICode> instructions = procedureEntryBlock.getIcode();
                    for(ICode icode: instructions){
                        newProcedure.addInstruction(icode);
                    }

                    i++;
                    while(i < procBlockSize){
                        BlockNode nextBlock = procBlocks.get(i);
                        if(nextBlock instanceof ProcedureExitNode){
                            ProcedureExitNode procedureExitNode = (ProcedureExitNode)nextBlock;
                            ProcedureEndingBlock endBlock = procedureExitNode.getBlock();
                            List<ICode>icode = endBlock.getIcode();
                            for(ICode code: icode){
                                newProcedure.addInstruction(code);
                            }
                            InternalPlace place = endBlock.getPlacement();
                            if(place != null){
                                newProcedure.placement = place;
                            }
                            newProcedure.returnStatement = endBlock.getReturn();
                            i++;
                            break;
                        } else {
                            List<ICode> instrs = nextBlock.getICode();
                            for(ICode instr: instrs){
                                newProcedure.addInstruction(instr);
                            }
                            i++;
                        }
                    }

                    procSec.addProcedure(newProcedure);
                } 
            }

            this.intermediateCode = new Prog(symbols, dataSec, codeSec, procSec);
        }
    }

    public void eliminateCommonSubExpressions(){
        if(this.globalFlowGraph != null){
            for(BlockNode block: globalFlowGraph.getBlocks()){
                DagGraph dag = buildDagForBlock(block, true);
                regenerateICodeForBlock(block, dag);
            }
            rebuildFromFlowGraph();
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

            if(node instanceof DagOperationNode){
                DagOperationNode node2 = (DagOperationNode)node;

                if(node2.getChildren().size() == 2){
                    //Its a Binary Operation
                    DagOperationNode.Op op = node2.getOperator();
                    BinExp.Operator op2 = Utils.getBinOp(op);

                    DagNode child1 = node2.getChildren().get(0);
                    DagNode child2 = node2.getChildren().get(1);

                    String identifier1 = Utils.getIdentifier(child1, liveAtEndOfBlock);
                    String identifier2 = Utils.getIdentifier(child2, liveAtEndOfBlock);

                    IdentExp exp1 = new IdentExp(identifier1);
                    IdentExp exp2 = new IdentExp(identifier2);

                    BinExp binExp = new BinExp(exp1, op2, exp2);

                    result.add(new Assign(identifier, binExp));
                } else if(node2.getChildren().size() == 1) {
                    //Its a Unary Operation
                    DagOperationNode.Op op = node2.getOperator();
                    UnExp.Operator op2 = Utils.getUnOp(op);

                    DagNode child1 = node2.getChildren().get(0);

                    String identifier1 = Utils.getIdentifier(child1, liveAtEndOfBlock);

                    IdentExp exp1 = new IdentExp(identifier1);

                    UnExp unExp = new UnExp(op2, exp1);

                    result.add(new Assign(identifier, unExp));
                }
            } else if(node instanceof DagValueNode){
                DagValueNode valNode = (DagValueNode)node;
                Object value = valNode.getValue();
                Exp resultExp = Utils.valueToExp(value);
                result.add(new Assign(identifier, resultExp));
            } else if(node instanceof DagVariableNode){
                DagVariableNode varNode = (DagVariableNode)node;
                VariableType type = varNode.getType();
                if(type == VariableType.DEFAULT){
                    DagNode child = varNode.getChild();
                    String identifier1 = Utils.getIdentifier(child, liveAtEndOfBlock);
                    IdentExp ident1 = new IdentExp(identifier1);
                    result.add(new Assign(identifier, ident1));
                } else if(type == VariableType.PARAM){
                    DagNode child = varNode.getChild();
                    String identifier1 = Utils.getIdentifier(child, liveAtEndOfBlock);
                    result.add(new ParamAssign(identifier, identifier1));
                } else if(type == VariableType.EXTERNAL_RET){
                    DagNode child = varNode.getChild();
                    String identifier1 = Utils.getIdentifier(child, liveAtEndOfBlock);
                    result.add(new ExternalPlace(identifier, identifier1));
                } else if(type == VariableType.INTERNAL_RET){
                    DagNode child = varNode.getChild();
                    String identifier1 = Utils.getIdentifier(child, liveAtEndOfBlock);
                    result.add(new InternalPlace(identifier, identifier1));
                }
            } else if(node instanceof DagInlineAssemblyNode){
                DagInlineAssemblyNode dagNode = (DagInlineAssemblyNode)node;
                List<String> children = new LinkedList<String>();
                for(DagNode child : dagNode.getChildren()){
                    String ident = Utils.getIdentifier(child, liveAtEndOfBlock);
                    children.add(ident);
                }
                List<String> operationList = dagNode.getIdentifiers();
                result.add(new Inline(operationList.get(0), children));
            }

            for(String ident : isAlive){
                IdentExp ident1 = new IdentExp(identifier);
                result.add(new Assign(ident, ident1));
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

    private static DagGraph buildDagForBlock(BlockNode block, boolean optimized){
        DagGraph dag = new DagGraph();
        DagNodeFactory factory = new DagNodeFactory();
        List<ICode> icodes = block.getICode();
        for(ICode icode : icodes){
            if(icode instanceof Assign){
                Assign assignICode = (Assign)icode;

                if(assignICode.value instanceof BinExp){
                    BinExp exp = (BinExp)assignICode.value;
                    
                    DagNode left = dag.searchForLatestChild(exp.left.toString());
                    DagNode right = dag.searchForLatestChild(exp.right.toString());

                    if(left == null){
                        left = factory.createNullNode(exp.left.toString());
                        dag.addDagNode(left);
                    }
    
                    if(right == null){
                        right = factory.createNullNode(exp.right.toString());
                        dag.addDagNode(right);
                    }

                    DagNode newNode = Utils.createBinaryNode(exp.op, assignICode.place, left, right);

                    DagNode exists = dag.getDagNode(newNode);
                    if(exists == null || !optimized){
                        dag.addDagNode(newNode);
                    } else {
                        exists.addIdentifier(assignICode.place);
                    }
                } else if(assignICode.value instanceof UnExp){
                    UnExp exp = (UnExp)assignICode.value;

                    DagNode right = dag.searchForLatestChild(exp.right.toString());

                    if(right == null){
                        right = factory.createNullNode(exp.right.toString());
                        dag.addDagNode(right);
                    }

                    DagNode newNode = Utils.createUnaryNode(exp.op, assignICode.place, right);

                    DagNode exists = dag.getDagNode(newNode);
                    if(exists == null || !optimized){
                        dag.addDagNode(newNode);
                    } else {
                        exists.addIdentifier(assignICode.place);
                    }
                } else if(assignICode.value instanceof IdentExp){
                    IdentExp exp = (IdentExp)assignICode.value;

                    DagNode right = dag.searchForLatestChild(exp.ident.toString());

                    if(right == null){
                        right = factory.createNullNode(exp.ident.toString());
                        dag.addDagNode(right);
                    }

                    DagNode newNode = factory.createDefaultVariableNode(assignICode.place, right);

                    DagNode exists = dag.getDagNode(newNode);
                    if(exists == null || !optimized){
                        dag.addDagNode(newNode);
                    } else {
                        exists.addIdentifier(assignICode.place);
                    }
                } else if(assignICode.value instanceof BoolExp){
                    BoolExp boolICode = (BoolExp)assignICode.value;
                    DagNode newNode = factory.createBooleanNode(assignICode.place, boolICode.trueFalse);
                    dag.addDagNode(newNode);
                } else if(assignICode.value instanceof IntExp){
                    IntExp intICode = (IntExp)assignICode.value;
                    DagNode newNode = factory.createIntNode(assignICode.place, intICode.value);
                    dag.addDagNode(newNode);
                } else if(assignICode.value instanceof RealExp){
                    RealExp realICode = (RealExp)assignICode.value;
                    DagNode newNode = factory.createRealNode(assignICode.place, realICode.realValue);
                    dag.addDagNode(newNode);
                } else if(assignICode.value instanceof StrExp){
                    StrExp strICode = (StrExp)assignICode.value;
                    DagNode newNode = factory.createStringNode(assignICode.place, strICode.value);
                    dag.addDagNode(newNode);
                }
            } else if(icode instanceof Inline){
                Inline inline = (Inline)icode;
                LinkedList<DagNode> children = new LinkedList<DagNode>();
                for(String param : inline.param){
                    DagNode child = dag.searchForLatestChild(param);
                    if(child == null){
                        child = factory.createNullNode(param);
                        dag.addDagNode(child);
                    }

                    children.add(child);
                }

                DagNode newNode = new DagInlineAssemblyNode(inline.inlineAssembly, children);
                
                DagNode exists = dag.getDagNode(newNode);
                if(exists == null){
                    dag.addDagNode(newNode);
                }
            } else if(icode instanceof ExternalPlace){
                ExternalPlace place = (ExternalPlace)icode;
                DagNode right = dag.searchForLatestChild(place.retPlace);

                if(right == null){
                    right = factory.createNullNode(place.retPlace);
                    dag.addDagNode(right);
                }

                DagNode newNode = factory.createExternalReturnVariableNode(place.place, right);

                DagNode exists = dag.getDagNode(newNode);
                if(exists == null || !optimized){
                    dag.addDagNode(newNode);
                } else {
                    exists.addIdentifier(place.place);
                }
            } else if(icode instanceof InternalPlace){
                InternalPlace place = (InternalPlace)icode;
                DagNode right = dag.searchForLatestChild(place.retPlace);

                if(right == null){
                    right = factory.createNullNode(place.retPlace);
                    dag.addDagNode(right);
                }

                DagNode newNode = factory.createInternalReturnVariableNode(place.place, right);

                DagNode exists = dag.getDagNode(newNode);
                if(exists == null || !optimized){
                    dag.addDagNode(newNode);
                } else {
                    exists.addIdentifier(place.place);
                }
            }else if(icode instanceof ParamAssign){
                ParamAssign paramAssign = (ParamAssign)icode;
                DagNode right = dag.searchForLatestChild(paramAssign.paramPlace);

                if(right == null){
                    right = factory.createNullNode(paramAssign.paramPlace);
                    dag.addDagNode(right);
                }

                DagNode newNode = factory.createParamVariableNode(paramAssign.newPlace, right);

                DagNode exists = dag.getDagNode(newNode);
                if(exists == null || !optimized){
                    dag.addDagNode(newNode);
                } else {
                    exists.addIdentifier(paramAssign.newPlace);
                }
            }
        }

        return dag;
    }

    public Prog getICode(){
        return this.intermediateCode;
    }

    private List<Integer> findFirstsInICode(List<ICode> intermediateCode, boolean procedureFirst){
        List<Integer> firsts = new LinkedList<Integer>();
        for(int i = 0; i < intermediateCode.size(); i++){
            ICode intermediateInstruction = intermediateCode.get(i);
            if(i == 0 && !procedureFirst){
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
        this.propAnal = new ConstantPropogationAnalysis(this.globalFlowGraph);
        this.propAnal.run();
    }

    private void runLiveVariableAnalysis(){
        this.liveAnal = new LiveVariableAnalysis(this.globalFlowGraph);
        this.liveAnal.run();
    }

    public void runDataFlowAnalysis(){
        if(this.globalFlowGraph != null){
            //buildGlobalExpressionSemilattice();
            runLiveVariableAnalysis();
            //runAvailableExpressionAnalysis();
            //runAnticipatedExpressionAnalysis();
            //buildEarliest();
            //buildUsedExpressions();  
            //runPosponableExpressionAnalysis();
            //buildLatest();
            //runUsedExpressionAnalysis();
            runConstantPropogationAnalysis();
        }
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
        for(BlockNode block : this.globalFlowGraph.getBlocks()){
            List<ICode> result = new LinkedList<ICode>();
            for(ICode icode : block.getICode()){
                if(icode instanceof Assign){
                    Assign assICode = (Assign)icode;
                    Set<String> liveVariables = this.liveAnal.getInstructionOutputSet(icode);
                    if(liveVariables.contains(assICode.place)){
                        result.add(assICode);
                    }
                } else if(icode instanceof ExternalPlace){
                    ExternalPlace assICode = (ExternalPlace)icode;
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
    }

    public void performConstantPropogation(){
        for(BlockNode block : this.globalFlowGraph.getBlocks()){
            List<ICode> icodeList = block.getICode();
            for(int i = 0; i < icodeList.size(); i++){
                ICode icode = icodeList.get(i);
                Set<Tuple<String, Object>> values = this.propAnal.getInstructionInputSet(icode);
                if(icode instanceof Assign){
                    Assign varICode = (Assign)icode;
                    if(varICode.value instanceof IdentExp){
                        IdentExp identVal = (IdentExp)varICode.value;
                        Object result = Utils.getValueFromSet(values, identVal.ident);
                        Exp resultExp = Utils.valueToExp(result);
                        if(resultExp != null){
                            varICode.value = resultExp;
                        }
                    } else if(varICode.value instanceof BinExp){
                        BinExp binExpVal = (BinExp)varICode.value;

                        if(binExpVal.left instanceof IdentExp){
                            IdentExp identLeft = (IdentExp)binExpVal.left;
                            Object result = Utils.getValueFromSet(values, identLeft.ident);
                            Exp resultExp = Utils.valueToExp(result);
                            if(resultExp != null)
                                binExpVal.left = resultExp;
                        }

                        if(binExpVal.right instanceof IdentExp){
                            IdentExp identRight = (IdentExp)binExpVal.right;
                            Object result = Utils.getValueFromSet(values, identRight.ident);
                            Exp resultExp = Utils.valueToExp(result);
                            if(resultExp != null){
                                binExpVal.right = resultExp;
                            }
                        }
                    } else if(varICode.value instanceof UnExp){
                        UnExp unExpVal = (UnExp)varICode.value;

                        if(unExpVal.right instanceof IdentExp){
                            IdentExp identRight = (IdentExp)unExpVal.right;
                            Object result = Utils.getValueFromSet(values, identRight.ident);
                            Exp resultExp = Utils.valueToExp(result);
                            if(resultExp != null){
                                unExpVal.right = resultExp;
                            }
                        }
                    }
                }
            }
        }
    }
}
