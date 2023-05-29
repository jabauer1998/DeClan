package io.github.H20man13.DeClan.common.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;

public class AvailableExpressionsAnalysis extends Analysis<Exp> {

    private Map<ICode, Set<Exp>> genSets;
    private Map<ICode, Set<Exp>> killSets;

    public AvailableExpressionsAnalysis(FlowGraph flowGraph, Set<Exp> globalFlowSet) {
        super(flowGraph, Direction.FORWARDS, Meet.INTERSECTION, globalFlowSet);
        genSets = new HashMap<ICode, Set<Exp>>();
        killSets = new HashMap<ICode, Set<Exp>>();

        for(BlockNode block : flowGraph.getBlocks()){
            List<ICode> codeList = block.getICode();
            for(int i = 0; i < codeList.size(); i++){
                Set<Exp> instructionKill = new HashSet<Exp>();
                Set<Exp> instructionGen = new HashSet<Exp>();
                ICode icode = codeList.get(i);
                if(icode instanceof Assign){
                    Assign assICode = (Assign)icode;
                    
                    if(assICode.value instanceof IdentExp){
                        IdentExp value = (IdentExp)assICode.value;
                        int defIndex = searchForDefinition(codeList, i, value.ident);
                        if(defIndex != -1){
                            if(!searchForSubsequentExpression(codeList, defIndex, value)){
                                instructionKill.add(value);
                            }
                        } else {
                            instructionGen.add(value);
                        } 
                    } else if(assICode.value instanceof UnExp){
                        UnExp value = (UnExp)assICode.value;

                        if(!value.right.isConstant()){
                            int defIndex = searchForDefinition(codeList, i, value.right.toString());

                            if(defIndex != -1){
                                if(!searchForSubsequentExpression(codeList, defIndex, value)){
                                    instructionKill.add(value);
                                }
                            } else {
                                instructionGen.add(value);
                            }
                        }
                    } else if(assICode.value instanceof BinExp){
                        BinExp value = (BinExp)assICode.value;
                    
                        int defIndex1 = searchForDefinition(codeList, i, value.left.toString());
                        int defIndex2 = searchForDefinition(codeList, i, value.right.toString());

                        if(defIndex1 != -1 || defIndex2 != -1){
                            boolean shouldKill = false;
                            if(defIndex1 != -1 && !searchForSubsequentExpression(codeList, defIndex1, value.left)){
                                shouldKill = true;
                            }

                            if(defIndex2 != -1 && !searchForSubsequentExpression(codeList, defIndex2, value.right)){
                                shouldKill = true;
                            }

                            if(shouldKill){
                                instructionKill.add(value);
                            }
                        } else {
                            instructionGen.add(value);
                        }
                    }
                }
                killSets.put(icode, instructionKill);
                genSets.put(icode, instructionGen);
            }
        }
    }

    private boolean searchForSubsequentExpression(List<ICode> codeList, int defIndex, Exp defIdent){
        for(int i = defIndex + 1; defIndex < codeList.size(); i++){
            ICode icode = codeList.get(i);
            if(icode instanceof Assign){
                Assign icodeDef = (Assign)icode;
                if(defIdent.equals(icodeDef.value)){
                    return true;
                }
            }
        }

        return false;
    }

    private int searchForDefinition(List<ICode> codeList, int i, String var) {
        for(int x = i + 1; x < codeList.size(); x++){
            ICode icode = codeList.get(x);
            if(icode instanceof Assign){
                Assign icodeBool = (Assign)icode;
                String defVal = icodeBool.place;
                if(var.equals(defVal)){
                    return x;
                }
            }
        }
        return -1;
    }

    @Override
    public Set<Exp> transferFunction(FlowGraphNode block, ICode instruction, Set<Exp> inputSet) {
        Set<Exp> result = new HashSet<Exp>();

        result.addAll(inputSet);
        result.removeAll(killSets.get(instruction));
        result.addAll(genSets.get(instruction));

        return result;
    }
    
}
