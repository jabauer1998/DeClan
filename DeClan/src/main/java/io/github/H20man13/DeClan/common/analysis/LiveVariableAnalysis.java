package io.github.H20man13.DeClan.common.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.LetBin;
import io.github.H20man13.DeClan.common.icode.LetBool;
import io.github.H20man13.DeClan.common.icode.LetInt;
import io.github.H20man13.DeClan.common.icode.LetReal;
import io.github.H20man13.DeClan.common.icode.LetString;
import io.github.H20man13.DeClan.common.icode.LetUn;
import io.github.H20man13.DeClan.common.icode.LetVar;

public class LiveVariableAnalysis extends Analysis<String> {

    private Map<FlowGraphNode, Set<String>> defSets;
    private Map<FlowGraphNode, Set<String>> useSets;

    public LiveVariableAnalysis(FlowGraph flowGraph) {
        super(flowGraph, Direction.BACKWARDS, Meet.UNION);

        this.defSets = new HashMap<FlowGraphNode, Set<String>>();
        this.useSets = new HashMap<FlowGraphNode, Set<String>>();
        
        for(BlockNode block : flowGraph.getBlocks()){
            Set<String> blockDef = new HashSet<String>();
            Set<String> blockUse = new HashSet<String>();
            for(ICode code : block.getICode()){
                if(code instanceof LetBool){
                    LetBool defPlace = (LetBool)code;
                    if(!blockUse.contains(defPlace.place)){
                        blockDef.add(defPlace.place);
                    }
                } else if(code instanceof LetString){
                    LetString defPlace = (LetString)code;
                    if(!blockUse.contains(defPlace.place)){
                        blockDef.add(defPlace.place);
                    }
                } else if(code instanceof LetInt){
                    LetInt defPlace = (LetInt)code;
                    if(!blockUse.contains(defPlace.place)){
                        blockDef.add(defPlace.place);
                    }
                } else if(code instanceof LetReal){
                    LetReal defPlace = (LetReal)code;
                    if(!blockUse.contains(defPlace.place)){
                        blockDef.add(defPlace.place);
                    }
                } else if(code instanceof LetVar){
                    LetVar defPlace = (LetVar)code;
                    if(!blockUse.contains(defPlace.place)){
                        blockDef.add(defPlace.place);
                    }

                    if(!blockDef.contains(defPlace.var)){
                        blockUse.add(defPlace.var);
                    }
                } else if(code instanceof LetBin){
                    LetBin defPlace = (LetBin)code;
                    if(!blockUse.contains(defPlace.place)){
                        blockDef.add(defPlace.place);
                    }

                    if(!blockDef.contains(defPlace.left)){
                        blockUse.add(defPlace.place);
                    }

                    if(!blockDef.contains(defPlace.right)){
                        blockUse.add(defPlace.right);
                    }
                } else if(code instanceof LetUn){
                    LetUn defPlace = (LetUn)code;

                    if(!blockUse.contains(defPlace.place)){
                        blockDef.add(defPlace.place);
                    }

                    if(!blockDef.contains(defPlace.value)){
                        blockUse.add(defPlace.value);
                    }
                }
            }
            defSets.put(block, blockDef);
            useSets.put(block, blockUse);
        }

    }

    @Override
    public Set<String> transferFunction(FlowGraphNode Node, Set<String> inputSet) {
        Set<String> resultSet = new HashSet<String>();

        resultSet.addAll(inputSet);
        resultSet.removeAll(defSets.get(Node));
        resultSet.addAll(useSets.get(Node));

        return resultSet;
    }
}
