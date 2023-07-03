package io.github.H20man13.DeClan.common.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Place;
import io.github.H20man13.DeClan.common.icode.Proc;
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
                            BinExp defPlace = (BinExp)assCode.value;

                            if(defPlace.left instanceof IdentExp){
                                instructionUse.add(defPlace.left.toString());
                            }

                            if(defPlace.right instanceof IdentExp){
                                instructionUse.add(defPlace.right.toString());
                            }
                        } else if(assCode.value instanceof UnExp){
                            UnExp defPlace = (UnExp)assCode.value;

                            if(defPlace.right instanceof IdentExp){
                                instructionUse.add(defPlace.right.toString());
                            }
                        } else if(assCode.value instanceof IdentExp){
                            IdentExp defPlace = (IdentExp)assCode.value;
                            instructionUse.add(defPlace.ident);
                        }
                    }
                } else if(code instanceof If){
                    BinExp exp = ((If)code).exp;

                    if(exp.left instanceof IdentExp){
                        instructionUse.add(exp.left.toString());
                    }

                    if(exp.right instanceof IdentExp){
                        instructionUse.add(exp.right.toString());
                    }
                } else if(code instanceof Place){
                    Place placement = (Place)code;
                    instructionUse.add(placement.retPlace);

                    instructionDef.add(placement.place);
                } else if(code instanceof Proc){
                    Proc placement = (Proc)code;
                    for(Tuple<String, String> arg : placement.params){
                        instructionUse.add(arg.source);
                    }
                }
                defSets.put(code, instructionDef);
                useSets.put(code, instructionUse);
            }
        }

    }

    @Override
    public Set<String> transferFunction(FlowGraphNode block, ICode instruction, Set<String> inputSet) {
        Set<String> resultSet = new HashSet<String>();

        resultSet.addAll(inputSet);
        resultSet.removeAll(defSets.get(instruction));
        resultSet.addAll(useSets.get(instruction));

        return resultSet;
    }
}
