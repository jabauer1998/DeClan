package io.github.H20man13.DeClan.main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.RegisterGenerator;
import io.github.H20man13.DeClan.common.analysis.AnticipatedExpressionsAnalysis;
import io.github.H20man13.DeClan.common.analysis.AvailableExpressionsAnalysis;
import io.github.H20man13.DeClan.common.analysis.PostponableExpressionsAnalysis;
import io.github.H20man13.DeClan.common.analysis.UsedExpressionAnalysis;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.EntryNode;
import io.github.H20man13.DeClan.common.flow.ExitNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.flow.LoopEntryNode;
import io.github.H20man13.DeClan.common.icode.BasicBlock;
import io.github.H20man13.DeClan.common.icode.Call;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Label;
import io.github.H20man13.DeClan.common.icode.LetBin;
import io.github.H20man13.DeClan.common.icode.LetBool;
import io.github.H20man13.DeClan.common.icode.LetInt;
import io.github.H20man13.DeClan.common.icode.LetReal;
import io.github.H20man13.DeClan.common.icode.LetString;
import io.github.H20man13.DeClan.common.icode.LetUn;
import io.github.H20man13.DeClan.common.icode.LetVar;
import io.github.H20man13.DeClan.common.icode.Proc;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.util.Utils;

public class MyOptimizer {
    private List<ICode> intermediateCode;
    private Environment<String, Object> environment;
    private MyICodeMachine machine;
    private FlowGraph globalFlowGraph;
    private AnticipatedExpressionsAnalysis anticipatedAnal;
    private AvailableExpressionsAnalysis availableAnal;
    private PostponableExpressionsAnalysis postponableAnal;
    private UsedExpressionAnalysis usedAnal;
    private Map<FlowGraphNode, Set<Exp>> latest;
    private Map<FlowGraphNode, Set<Exp>> earliest;
    private Map<FlowGraphNode, Set<Exp>> used;
    private Set<Exp> globalFlowSet;
    
    private RegisterGenerator gen;

    public MyOptimizer(List<ICode> intermediateCode){
        this(intermediateCode, new RegisterGenerator());
    }

    public MyOptimizer(List<ICode> intermediateCode, RegisterGenerator gen){
        this.gen = gen;
        this.intermediateCode = intermediateCode;
        this.environment = new Environment<>();
        this.machine = new MyICodeMachine(this.environment);
        this.latest = new HashMap<FlowGraphNode, Set<Exp>>();
        this.earliest = new HashMap<FlowGraphNode, Set<Exp>>();
        this.used = new HashMap<FlowGraphNode, Set<Exp>>();
        this.globalFlowSet = new HashSet<Exp>();
    }

    private List<Integer> findFirsts(){
        List<Integer> firsts = new LinkedList<Integer>();
        for(int i = 0; i < intermediateCode.size(); i++){
            ICode intermediateInstruction = intermediateCode.get(i);
            if(i == 0){
                //First Statement is allways a leader
                firsts.add(i);
            } else if(intermediateInstruction instanceof Label){
                //Target of Jumps are allways leaders
                firsts.add(i);
            } else if(i + 1 < intermediateCode.size() && 
            (intermediateInstruction instanceof If || 
            intermediateInstruction instanceof Goto ||
            intermediateInstruction instanceof Proc ||
            intermediateInstruction instanceof Call)){
                //First instruction following an If/Goto/Proc/Call are leaders
                firsts.add(i + 1);
            }
        }

        return firsts;
    }

    public void breakIntoBasicBlocks(){
        List<Integer> firsts = findFirsts();
        List<ICode> basicBlocks = new LinkedList<ICode>();
        for(int leaderIndex = 0; leaderIndex < firsts.size(); leaderIndex++){
            int endIndex;
            if(leaderIndex + 1 < firsts.size()){
                endIndex = firsts.get(leaderIndex + 1) - 1;
            } else {
                endIndex = this.intermediateCode.size() - 1;
            }

            List<ICode> basicBlockList = new LinkedList<ICode>();
            for(int i = leaderIndex; i <= endIndex; i++){
                basicBlockList.add(intermediateCode.get(i));
            }

            basicBlocks.add(new BasicBlock(basicBlockList));
        }

        this.intermediateCode = basicBlocks;
    }

    public void buildGlobalFlowGraph(){
        if(this.intermediateCode.size() > 0){
            HashMap<String, BlockNode> labeledNodes = new HashMap<String, BlockNode>();
            List<BlockNode> dagNodes = new LinkedList<BlockNode>();

            for(int i = 0; i < intermediateCode.size(); i++){
                ICode icodeAtIndex = intermediateCode.get(i);
                if(icodeAtIndex instanceof BasicBlock){
                    BasicBlock blockAtIndex = (BasicBlock)icodeAtIndex;
                    BlockNode blockNode = new BlockNode(blockAtIndex);
                    
                    if(MyAnalysis.beginningOfBlockIsLabel(blockAtIndex)){
                        Label firstLabel = (Label)blockAtIndex.getIcode().get(0);
                        String labelName = firstLabel.label;
                        labeledNodes.put(labelName, blockNode);
                    }

                    dagNodes.add(blockNode);
                }
            }

            for(int i = 0; i < dagNodes.size(); i++){
                BlockNode node = dagNodes.get(i);
                BasicBlock block = node.getBlock();
                if(MyAnalysis.endOfBlockIsJump(block)){
                    ICode lastCode = block.getIcode().get(block.getIcode().size() - 1);
                    if(lastCode instanceof If){
                        If lastIf = (If)lastCode;
                        BlockNode trueNode = labeledNodes.get(lastIf.ifTrue);
                        node.addSuccessor(trueNode);
                        trueNode.addPredecessor(node);
                        BlockNode falseNode = labeledNodes.get(lastIf.ifFalse);
                        node.addSuccessor(falseNode);
                        falseNode.addPredecessor(node);
                    } else if(lastCode instanceof Goto){
                        Goto lastGoto = (Goto)lastCode;
                        BlockNode labeledNode = labeledNodes.get(lastGoto.label);
                        node.addSuccessor(labeledNode);
                        labeledNode.addPredecessor(node);
                    }
                } else if(i + 1 < dagNodes.size()){
                    BlockNode nextNode = dagNodes.get(i + 1);
                    node.addSuccessor(nextNode);
                    nextNode.addPredecessor(node);
                }
            }

            EntryNode entry = new EntryNode(dagNodes.get(0));
            ExitNode exit = new ExitNode(dagNodes.get(dagNodes.size() - 1));

            FlowGraph flowGraph = new FlowGraph(entry, dagNodes, exit);

            this.globalFlowGraph = flowGraph;
        }
    }

    public void removeDeadCode(){
        if(this.globalFlowGraph != null){
            for(BlockNode block : this.globalFlowGraph.getBlocks()){
                block.removeDeadCode();
            }
        }
    }

    public void runDataFlowAnalysis(){
        if(this.globalFlowGraph != null){
            for(BlockNode block : this.globalFlowGraph.getBlocks()){
                for(ICode icode : block.getICode()){
                    if(icode instanceof LetVar){
                        LetVar ident = (LetVar)icode;
                        if(!Utils.setContainsExp(this.globalFlowSet, ident.var)){
                            this.globalFlowSet.add(ident.var);
                        }
                    } else if(icode instanceof LetReal){
                        LetReal realExp = (LetReal)icode;
                        if(!Utils.setContainsExp(this.globalFlowSet, realExp.value)){
                            this.globalFlowSet.add(realExp.value);
                        }
                    } else if(icode instanceof LetInt){
                        LetInt intExp = (LetInt)icode;
                        if(!Utils.setContainsExp(this.globalFlowSet, intExp.value)){
                            this.globalFlowSet.add(intExp.value);
                        }
                    } else if(icode instanceof LetBool){
                        LetBool boolExp = (LetBool)icode;
                        if(!Utils.setContainsExp(this.globalFlowSet, boolExp.value)){
                            this.globalFlowSet.add(boolExp.value);
                        }
                    } else if(icode instanceof LetString){
                        LetString strExp = (LetString)icode;
                        if(!Utils.setContainsExp(this.globalFlowSet, strExp.value)){
                            this.globalFlowSet.add(strExp.value);
                        }
                    } else if(icode instanceof LetBin){
                        LetBin binOp = (LetBin)icode;
                        if(!Utils.setContainsExp(this.globalFlowSet, binOp.exp)){
                            this.globalFlowSet.add(binOp.exp);
                        }
                    } else if(icode instanceof LetUn){
                        LetUn unOp = (LetUn)icode;
                        if(!Utils.setContainsExp(this.globalFlowSet, unOp.unExp)){
                            this.globalFlowSet.add(unOp.unExp);
                        }
                    }
                }
            }

            this.availableAnal = new AvailableExpressionsAnalysis(this.globalFlowGraph, this.globalFlowSet);
            this.anticipatedAnal = new AnticipatedExpressionsAnalysis(this.globalFlowGraph, this.globalFlowSet);
            
            this.availableAnal.run();
            this.anticipatedAnal.run();

            for(BlockNode block : this.globalFlowGraph.getBlocks()){
                Set<Exp> earliest = new HashSet<Exp>();
                earliest.addAll(this.anticipatedAnal.getInputSet(block));
                earliest.removeAll(this.availableAnal.getInputSet(block));
                this.earliest.put(block, earliest);
            }
            

            for(BlockNode block : this.globalFlowGraph.getBlocks()){
                Set<Exp> blockUsed = new HashSet<Exp>();
                for(ICode icode : block.getICode()){
                    if(icode instanceof LetBool){
                        LetBool boolICode = (LetBool)icode;
                        if(!Utils.setContainsExp(blockUsed, boolICode.value)){
                            blockUsed.add(boolICode.value);
                        }
                    } else if(icode instanceof LetInt){
                        LetInt intICode = (LetInt)icode;
                        if(!Utils.setContainsExp(blockUsed, intICode.value)){
                            blockUsed.add(intICode.value);
                        }
                    } else if(icode instanceof LetReal){
                        LetReal realICode = (LetReal)icode;
                        if(!Utils.setContainsExp(blockUsed, realICode.value)){
                            blockUsed.add(realICode.value);
                        }
                    } else if(icode instanceof LetString){
                        LetString strICode = (LetString)icode;
                        if(!Utils.setContainsExp(blockUsed, strICode.value)){
                            blockUsed.add(strICode.value);
                        }
                    } else if(icode instanceof LetVar){
                        LetVar varICode = (LetVar)icode;
                        if(!Utils.setContainsExp(blockUsed, varICode.var)){
                            blockUsed.add(varICode.var);
                        }
                    } else if(icode instanceof LetUn){
                        LetUn unICode = (LetUn)icode;
                        if(!Utils.setContainsExp(blockUsed, unICode.unExp)){
                            blockUsed.add(unICode.unExp);
                        }
                    } else if(icode instanceof LetBin){
                        LetBin binICode = (LetBin)icode;
                        if(!Utils.setContainsExp(blockUsed, binICode.exp)){
                            blockUsed.add(binICode.exp);
                        }
                    }
                }
                this.used.put(block, blockUsed);
            }

            this.postponableAnal = new PostponableExpressionsAnalysis(this.globalFlowGraph, this.globalFlowSet, this.earliest, this.used);
            this.postponableAnal.run();

            for(BlockNode block : this.globalFlowGraph.getBlocks()){
                Set<Exp> latest = new HashSet<Exp>();
                
                Map<FlowGraphNode, Set<Exp>> earliestUnionPosponableSaved = new HashMap<FlowGraphNode, Set<Exp>>();
                for (FlowGraphNode sucessor : block.getSuccessors()){
                    Set<Exp> earliestUnionPosponable = new HashSet<Exp>();
                    earliestUnionPosponable.addAll(earliest.get(sucessor));
                    earliestUnionPosponable.addAll(postponableAnal.getInputSet(sucessor));

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

                usedUnionCompliment.addAll(this.used.get(block));
                usedUnionCompliment.addAll(compliment);

                Set<Exp> earliestUnionPosponable = new HashSet<Exp>();
                earliestUnionPosponable.addAll(this.earliest.get(block));
                earliestUnionPosponable.addAll(this.postponableAnal.getInputSet(block));

                latest.addAll(earliestUnionPosponable);
                latest.retainAll(usedUnionCompliment);

                this.latest.put(block, latest);
            }

            this.usedAnal = new UsedExpressionAnalysis(this.globalFlowGraph, this.used, this.latest);
            this.usedAnal.run();
        }
    }

    public void performPartialRedundancyElimination(){
        for(BlockNode block : this.globalFlowGraph.getBlocks()){
            Set<Exp> latestInstructionUsedOutput = new HashSet<Exp>();
            latestInstructionUsedOutput.addAll(this.latest.get(block));
            latestInstructionUsedOutput.retainAll(this.usedAnal.getOutputSet(block));

            Map<Exp, String> identifierMap = new HashMap<Exp, String>();
            List<ICode> toPreAppendToBeginning = new LinkedList<ICode>();
            for(Exp latestInstructionOutput : latestInstructionUsedOutput){
                String register = gen.genNextRegister();
                ICode instruction = Utils.getICodeFromExpression(register, latestInstructionOutput);
                if(instruction != null){
                    toPreAppendToBeginning.add(instruction);
                }
            }

            Set<Exp> notLatest = new HashSet<Exp>();
            notLatest.addAll(this.globalFlowSet);
            notLatest.removeAll(this.latest.get(block));

            Set<Exp> notLatestUnionUsedOut = new HashSet<Exp>();
            notLatestUnionUsedOut.addAll(notLatest);
            notLatestUnionUsedOut.addAll(usedAnal.getOutputSet(block));

            Set<Exp> useIntersectionNotLatestUnionUsedOut = new HashSet<Exp>();
            useIntersectionNotLatestUnionUsedOut.addAll(this.used.get(block));
            useIntersectionNotLatestUnionUsedOut.retainAll(notLatestUnionUsedOut);

            List<ICode> icodeList = block.getICode();
            for(int i = 0; i < icodeList.size(); i++){
                ICode elem = icodeList.get(i);
                String place = Utils.getPlace(elem);
                Exp codeExp = Utils.getExpressionFromICode(elem);
                if(codeExp != null && place != null){
                    if(Utils.setContainsExp(useIntersectionNotLatestUnionUsedOut, codeExp)){
                        String value = identifierMap.get(codeExp);
                        LetVar var = new LetVar(place, value);
                        icodeList.set(i, var);
                    }
                }
            }
            
            List<ICode> resultList = new LinkedList<ICode>();
            resultList.addAll(toPreAppendToBeginning);
            resultList.addAll(icodeList);

            block.getBlock().setICode(resultList);
        }
    }
}
