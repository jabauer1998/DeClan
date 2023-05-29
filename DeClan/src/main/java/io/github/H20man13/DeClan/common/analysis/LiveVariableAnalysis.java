package io.github.H20man13.DeClan.common.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;

public class LiveVariableAnalysis extends Analysis<String> {

    private Map<ICode, Set<String>> defSets;
    private Map<ICode, Set<String>> useSets;

    public LiveVariableAnalysis(FlowGraph flowGraph) {
        super(flowGraph, Direction.BACKWARDS, Meet.UNION);

        this.defSets = new HashMap<ICode, Set<String>>();
        this.useSets = new HashMap<ICode, Set<String>>();
        
        for(BlockNode block : flowGraph.getBlocks()){
            for(ICode code : block.getICode()){
                Set<String> instructionDef = new HashSet<String>();
                Set<String> instructionUse = new HashSet<String>();
                if(code instanceof Assign){
                    Assign assCode = (Assign)code;
                    instructionDef.add(assCode.place);
                    if(!assCode.isConstant()){
                        if(assCode.value instanceof BinExp){
                            BinExp defPlace = (BinExp)code;

                            if(!instructionDef.contains(defPlace.left.toString())){
                                instructionUse.add(defPlace.left.toString());
                            }

                            if(!instructionDef.contains(defPlace.right.toString())){
                                instructionUse.add(defPlace.right.toString());
                            }
                        } else if(assCode.value instanceof UnExp){
                            UnExp defPlace = (UnExp)code;

                            if(!instructionDef.contains(defPlace.right.toString())){
                                instructionUse.add(defPlace.right.toString());
                            }
                        } else if(assCode.value instanceof IdentExp){
                            IdentExp defPlace = (IdentExp)assCode.value;
                            if(!instructionUse.contains(defPlace.ident)){
                                instructionDef.add(defPlace.ident);
                            }
                        }
                    }
                }
                defSets.put(code, instructionDef);
                useSets.put(code, instructionUse);
            }
        }

    }

    @Override
    public Set<String> transferFunction(FlowGraphNode block, ICode Node, Set<String> inputSet) {
        Set<String> resultSet = new HashSet<String>();

        resultSet.addAll(inputSet);
        resultSet.removeAll(defSets.get(Node));
        resultSet.addAll(useSets.get(Node));

        return resultSet;
    }
}
