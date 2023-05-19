package io.github.H20man13.DeClan.main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.RegisterGenerator;
import io.github.H20man13.DeClan.common.analysis.AnticipatedThis.globalFlowSetAnalysis;
import io.github.H20man13.DeClan.common.analysis.AvailableThis.globalFlowSetAnalysis;
import io.github.H20man13.DeClan.common.analysis.PostponableThis.globalFlowSetAnalysis;
import io.github.H20man13.DeClan.common.analysis.AnticipatedExpressionsAnalysis;
import io.github.H20man13.DeClan.common.analysis.AvailableExpressionsAnalysis;
import io.github.H20man13.DeClan.common.analysis.PostponableExpressionsAnalysis;
import io.github.H20man13.DeClan.common.analysis.UsedExpressionAnalysis;
import io.github.H20man13.DeClan.common.analysis.exp.BinExp;
import io.github.H20man13.DeClan.common.analysis.exp.BoolExp;
import io.github.H20man13.DeClan.common.analysis.exp.Exp;
import io.github.H20man13.DeClan.common.analysis.exp.IdentExp;
import io.github.H20man13.DeClan.common.analysis.exp.IntExp;
import io.github.H20man13.DeClan.common.analysis.exp.RealExp;
import io.github.H20man13.DeClan.common.analysis.exp.StrExp;
import io.github.H20man13.DeClan.common.analysis.exp.UnExp;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.EntryNode;
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

    private void moveConstStatementsToFront(BasicBlock block){
        List<ICode> blockCode = block.getIcode();
        List<ICode> constStatements = new LinkedList<ICode>();
        List<ICode> compoundStatements = new LinkedList<ICode>();
        
        for(int i = 0; i < blockCode.size(); i++){
            ICode intCode = blockCode.get(i);
            if(intCode instanceof LetBool){
                constStatements.add(intCode);
            } else if(intCode instanceof LetInt){
                constStatements.add(intCode);
            } else if(intCode instanceof LetReal){
                constStatements.add(intCode);
            } else if(intCode instanceof LetString){
                constStatements.add(intCode);
            } else {
                compoundStatements.add(intCode);
            }
        }

        constStatements.addAll(compoundStatements);
        block.setICode(constStatements);
    }

    public void moveConstStatementsToFront(){
        List<ICode> constStatements = new LinkedList<ICode>();
        List<ICode> compoundStatements = new LinkedList<ICode>();
        for(int i = 0; i < this.intermediateCode.size(); i++){
            ICode intermediateCode = this.intermediateCode.get(i);
            if(intermediateCode instanceof BasicBlock){
                moveConstStatementsToFront((BasicBlock)intermediateCode);
                compoundStatements.add(intermediateCode);
            } else if(intermediateCode instanceof LetBool){
                constStatements.add(intermediateCode);
            } else if(intermediateCode instanceof LetInt){
                constStatements.add(intermediateCode);
            } else if(intermediateCode instanceof LetReal){
                constStatements.add(intermediateCode);
            } else if(intermediateCode instanceof LetString){
                constStatements.add(intermediateCode);
            } else {
                compoundStatements.add(intermediateCode);
            }
        }

        constStatements.addAll(compoundStatements);
        this.intermediateCode = constStatements;
    }

    public void solveConstExpression(){
        for(int i = 0; i < this.intermediateCode.size(); i++){
            ICode intermediateCode = this.intermediateCode.get(i);
            if(intermediateCode instanceof BasicBlock){
                solveConstExpression((BasicBlock)intermediateCode);
            } else if(intermediateCode instanceof LetBool){
                machine.interpretLetBool((LetBool)intermediateCode);
            } else if(intermediateCode instanceof LetInt){
                machine.interpretLetInt((LetInt)intermediateCode);
            } else if(intermediateCode instanceof LetReal){
                machine.interpretLetReal((LetReal)intermediateCode);
            } else if(intermediateCode instanceof LetString){
                machine.interpretLetString((LetString)intermediateCode);
            } else if(intermediateCode instanceof LetVar){
                LetVar varDecl = (LetVar)intermediateCode;
                if(environment.entryExists(varDecl.var)){
                    //Then It is Constant and we need to Make this Var a Constant
                    machine.interpretLetVar(varDecl);
                    Object entryValue = environment.getEntry(varDecl.place);
                    this.intermediateCode.set(i, generateConstant(varDecl.place, entryValue));
                }
            } else if(intermediateCode instanceof LetBin){
                LetBin binOp = (LetBin)intermediateCode;
                if(environment.entryExists(binOp.left) && environment.entryExists(binOp.right)){
                    machine.interpretLetBin(binOp);
                    Object entryValue = environment.getEntry(binOp.place);
                    this.intermediateCode.set(i, generateConstant(binOp.place, entryValue));
                }
            } else if(intermediateCode instanceof LetUn){
                LetUn unOp = (LetUn)intermediateCode;
                if(environment.entryExists(unOp.value)){
                    machine.interpretLetUn(unOp);
                    Object entryValue = environment.getEntry(unOp.place);
                    this.intermediateCode.set(i, generateConstant(unOp.place, entryValue));
                }
            }
        }
    }

    private ICode generateConstant(String place, Object value){
        if(value instanceof Double){
            return new LetReal(place, (double)value);
        } else if(value instanceof Integer){
            return new LetInt(place, (int)value);
        } else if(value instanceof String){
            return new LetString(place, (String)value);
        } else if(value instanceof Boolean){
            return new LetBool(place, (boolean)value);
        } else {
            return null;
        }
    }

    private void solveConstExpression(BasicBlock block){
        List<ICode> statements = block.getIcode();
        //First we need to move constant statements to the front of the Block
        for(int i = 0; i < statements.size(); i++){
            ICode intermediateCode = statements.get(i);
            if(intermediateCode instanceof LetBool){
                machine.interpretLetBool((LetBool)intermediateCode);
            } else if(intermediateCode instanceof LetInt){
                machine.interpretLetInt((LetInt)intermediateCode);
            } else if(intermediateCode instanceof LetReal){
                machine.interpretLetReal((LetReal)intermediateCode);
            } else if(intermediateCode instanceof LetString){
                machine.interpretLetString((LetString)intermediateCode);
            } else if(intermediateCode instanceof LetVar){
                LetVar varDecl = (LetVar)intermediateCode;
                if(environment.entryExists(varDecl.var)){
                    //Then It is Constant and we need to Make this Var a Constant
                    machine.interpretLetVar(varDecl);
                    Object entryValue = environment.getEntry(varDecl.place);
                    statements.set(i, generateConstant(varDecl.place, entryValue));
                }
            } else if(intermediateCode instanceof LetBin){
                LetBin binOp = (LetBin)intermediateCode;
                if(environment.entryExists(binOp.left) && environment.entryExists(binOp.right)){
                    machine.interpretLetBin(binOp);
                    Object entryValue = environment.getEntry(binOp.place);
                    statements.set(i, generateConstant(binOp.place, entryValue));
                }
            } else if(intermediateCode instanceof LetUn){
                LetUn unOp = (LetUn)intermediateCode;
                if(environment.entryExists(unOp.value)){
                    machine.interpretLetUn(unOp);
                    Object entryValue = environment.getEntry(unOp.place);
                    statements.set(i, generateConstant(unOp.place, entryValue));
                }
            }
        }
    }

    public void removeUnusedCode(){
        HashMap<String, Integer> defined = new HashMap<>();
        HashSet<String> used = new HashSet<>();
        for(int i = 0; i < this.intermediateCode.size(); i++){
            ICode intermCode = this.intermediateCode.get(i);
            if(intermCode instanceof BasicBlock){
                removeUnusedCode((BasicBlock)intermCode);
            } else if(intermCode instanceof LetBool){
                LetBool bool = (LetBool)intermCode;
                defined.put(bool.place, i);
            } else if(intermCode instanceof LetInt){
                LetInt intVal = (LetInt)intermCode;
                defined.put(intVal.place, i);
            } else if(intermCode instanceof LetString){
                LetString strVal = (LetString)intermCode;
                defined.put(strVal.place, i);
            } else if(intermCode instanceof LetReal){
                LetReal realVal = (LetReal)intermCode;
                defined.put(realVal.place, i);
            } else if(intermCode instanceof LetVar){
                LetVar varVal = (LetVar)intermCode;
                defined.put(varVal.place, i);
                used.add(varVal.var);
            } else if(intermCode instanceof LetUn){
                LetUn varVal = (LetUn)intermCode;
                defined.put(varVal.place, i);
                used.add(varVal.value);
            } else if(intermCode instanceof LetBin){
                LetBin varVal = (LetBin)intermCode;
                defined.put(varVal.place, i);
                used.add(varVal.left);
                used.add(varVal.right);
            }
        }

        //Now we need to interate through all the Defined Keys
        //If any of them are Defined but never used then we need to add the Index to the List
        HashSet<Integer> indexes = new HashSet<>();
        for(String defVal : defined.keySet()){
            if(!used.contains(defVal)){
                indexes.add(defined.get(defVal));
            }
        }

        //After all the Indexes are collected that we need to ignore we can build the final list
        List<ICode> finalList = new LinkedList<ICode>();
        for(int i = 0; i < intermediateCode.size(); i++){
            ICode iCode = intermediateCode.get(i);
            if(!indexes.contains(i)){
                finalList.add(iCode);
            }
        }

        this.intermediateCode = finalList;
    }

    private void removeUnusedCode(BasicBlock block){
        HashMap<String, Integer> defined = new HashMap<>();
        HashSet<String> used = new HashSet<>();
        List<ICode> internalICode = block.getIcode();
        for(int i = 0; i < internalICode.size(); i++){
            ICode intermCode = internalICode.get(i);
            if(intermCode instanceof LetBool){
                LetBool bool = (LetBool)intermCode;
                defined.put(bool.place, i);
            } else if(intermCode instanceof LetInt){
                LetInt intVal = (LetInt)intermCode;
                defined.put(intVal.place, i);
            } else if(intermCode instanceof LetString){
                LetString strVal = (LetString)intermCode;
                defined.put(strVal.place, i);
            } else if(intermCode instanceof LetReal){
                LetReal realVal = (LetReal)intermCode;
                defined.put(realVal.place, i);
            } else if(intermCode instanceof LetVar){
                LetVar varVal = (LetVar)intermCode;
                defined.put(varVal.place, i);
                used.add(varVal.var);
            } else if(intermCode instanceof LetUn){
                LetUn varVal = (LetUn)intermCode;
                defined.put(varVal.place, i);
                used.add(varVal.value);
            } else if(intermCode instanceof LetBin){
                LetBin varVal = (LetBin)intermCode;
                defined.put(varVal.place, i);
                used.add(varVal.left);
                used.add(varVal.right);
            }
        }

        //Now we need to interate through all the Defined Keys
        //If any of them are Defined but never used then we need to add the Index to the List
        HashSet<Integer> indexes = new HashSet<>();
        for(String defVal : defined.keySet()){
            if(!used.contains(defVal)){
                indexes.add(defined.get(defVal));
            }
        }

        //After all the Indexes are collected that we need to ignore we can build the final list
        List<ICode> finalList = new LinkedList<ICode>();
        for(int i = 0; i < intermediateCode.size(); i++){
            ICode icode = intermediateCode.get(i);
            if(!indexes.contains(i)){
                finalList.add(icode);
            }
        }

        block.setICode(finalList);
    }

    public void buildGlobalFlowGraph(){
        MyFlowGraphBuilder builder = new MyFlowGraphBuilder(intermediateCode);
        this.globalFlowGraph = builder.buildFlowGraph();
    }

    public void removeDeadCode(){
        this.globalFlowGraph.removeDeadCode();
    }

    public void makeLoopEntries(){
        Set<Set<FlowGraphNode>> loops = this.globalFlowGraph.identifyLoops();

        Set<FlowGraphNode> nodesToMakeLoopEntries = new HashSet<FlowGraphNode>();
        for(Set<FlowGraphNode> loop: loops){
            for(FlowGraphNode nodeInLoop: loop){
                if(nodeInLoop.containsPredecessorOutsideLoop(loop)){
                    nodesToMakeLoopEntries.add(nodeInLoop);
                    break;
                }
            }
        }

        //To replace a Block Node with its LoopEntryNode all we need to do
        //is call the copy constructor for the LoopEntryNode
        //It will handle all the necessary removal and additions of Successors and predecessors
        //Garbage Collection will automatically clean up the one that should be cleaned...
        for(FlowGraphNode nodeToMakeLoopEntry : nodesToMakeLoopEntries){
            if(nodeToMakeLoopEntry instanceof BlockNode){
                globalFlowGraph.replaceBlockNode(nodeToMakeLoopEntry, new LoopEntryNode((BlockNode)nodeToMakeLoopEntry));
            }
        }
    }

    public void generateOptimizedIrFromDag(){
        if(this.globalFlowGraph != null){
            this.globalFlowGraph.generateOptimizedIr();
        }
    }

    public void runDataFlowAnalysis(){
        if(this.globalFlowGraph != null){

            for(BlockNode block : this.globalFlowGraph.getBlocks()){
                for(ICode icode : block.getICode()){
                    if(icode instanceof LetVar){
                        IdentExp ident = new IdentExp(((LetVar)icode).var);
                        if(!Utils.setContainsExp(this.globalFlowSet, ident)){
                            this.globalFlowSet.add(ident);
                        }
                    } else if(icode instanceof LetReal){
                        RealExp realExp = new RealExp(((LetReal)icode).value);
                        if(!Utils.setContainsExp(this.globalFlowSet, realExp)){
                            this.globalFlowSet.add(realExp);
                        }
                    } else if(icode instanceof LetInt){
                        IntExp intExp = new IntExp(((LetInt)icode).value);
                        if(!Utils.setContainsExp(this.globalFlowSet, intExp)){
                            this.globalFlowSet.add(intExp);
                        }
                    } else if(icode instanceof LetBool){
                        BoolExp boolExp = new BoolExp(((LetBool)icode).value);
                        if(!Utils.setContainsExp(this.globalFlowSet, boolExp)){
                            this.globalFlowSet.add(boolExp);
                        }
                    } else if(icode instanceof LetString){
                        StrExp strExp = new StrExp(((LetString)icode).value);
                        if(!Utils.setContainsExp(this.globalFlowSet, strExp)){
                            this.globalFlowSet.add(strExp);
                        }
                    } else if(icode instanceof LetBin){
                        LetBin binOp = (LetBin)icode;
                        IdentExp left = new IdentExp(binOp.left);
                        BinExp.Operator op = Utils.getOp(binOp.op);
                        IdentExp right = new IdentExp(binOp.right);
                        BinExp bin = new BinExp(left, op, right);
                        if(!Utils.setContainsExp(this.globalFlowSet, bin)){
                            this.globalFlowSet.add(bin);
                        }
                    } else if(icode instanceof LetUn){
                        LetUn unOp = (LetUn)icode;
                        IdentExp right = new IdentExp(unOp.value);
                        UnExp.Operator op = Utils.getOp(unOp.op);
                        UnExp unExp = new UnExp(op, right);
                        if(!Utils.setContainsExp(this.globalFlowSet, unExp)){
                            this.globalFlowSet.add(unExp);
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
                        BoolExp exp = new BoolExp(boolICode.value);
                        if(!Utils.setContainsExp(blockUsed, exp)){
                            blockUsed.add(exp);
                        }
                    } else if(icode instanceof LetInt){
                        LetInt intICode = (LetInt)icode;
                        IntExp exp = new IntExp(intICode.value);
                        if(!Utils.setContainsExp(blockUsed, exp)){
                            blockUsed.add(exp);
                        }
                    } else if(icode instanceof LetReal){
                        LetReal realICode = (LetReal)icode;
                        RealExp exp = new RealExp(realICode.value);
                        if(!Utils.setContainsExp(blockUsed, exp)){
                            blockUsed.add(exp);
                        }
                    } else if(icode instanceof LetString){
                        LetString strICode = (LetString)icode;
                        StrExp exp = new StrExp(strICode.value);
                        if(!Utils.setContainsExp(blockUsed, exp)){
                            blockUsed.add(exp);
                        }
                    } else if(icode instanceof LetVar){
                        LetVar varICode = (LetVar)icode;
                        IdentExp exp = new IdentExp(varICode.var);
                        if(!Utils.setContainsExp(blockUsed, exp)){
                            blockUsed.add(exp);
                        }
                    } else if(icode instanceof LetUn){
                        LetUn unICode = (LetUn)icode;
                        IdentExp iExp = new IdentExp(unICode.value);
                        UnExp unExp = new UnExp(Utils.getOp(unICode.op), iExp);
                        if(!Utils.setContainsExp(blockUsed, unExp)){
                            blockUsed.add(unExp);
                        }
                    } else if(icode instanceof LetBin){
                        LetBin binICode = (LetBin)icode;
                        IdentExp iExp1 = new IdentExp(binICode.left);
                        IdentExp iExp2 = new IdentExp(binICode.right);
                        BinExp bExp = new BinExp(iExp1, Utils.getOp(binICode.op), iExp2);
                        if(!Utils.setContainsExp(blockUsed, bExp)){
                            blockUsed.add(bExp);
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
        }
    }
}
