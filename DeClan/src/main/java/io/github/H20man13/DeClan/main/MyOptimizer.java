package io.github.H20man13.DeClan.main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.EntryNode;
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

public class MyOptimizer {
    private List<ICode> intermediateCode;
    private Environment<String, Object> environment;
    private MyICodeMachine machine;
    private EntryNode globalFlowGraph;


    public MyOptimizer(List<ICode> intermediateCode){
        this.intermediateCode = intermediateCode;
        this.environment = new Environment<>();
        this.machine = new MyICodeMachine(this.environment);
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

    public void solveConstExpressions(){
        for(int i = 0; i < this.intermediateCode.size(); i++){
            ICode intermediateCode = this.intermediateCode.get(i);
            if(intermediateCode instanceof BasicBlock){
                solveConstExpressions((BasicBlock)intermediateCode);
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

    private void solveConstExpressions(BasicBlock block){
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
            ICode icode = intermediateCode.get(i);
            if(!indexes.contains(i)){
                finalList.add(icode);
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

        //To replace a Block Node with its root Entry Node all we need to do
        //is call the copy constructor for the LoopEntryNode
        //It will handle all the necessary removal and additions of Successors and predecessors
        //Garbage Collection will automatically clean up the one that should be cleaned
        for(FlowGraphNode nodeToMakeLoopEntry : nodesToMakeLoopEntries){
            if(nodeToMakeLoopEntry instanceof BlockNode){
                new LoopEntryNode((BlockNode)nodeToMakeLoopEntry);
            }
        }
    }
}
