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

public class AnticipatedExpressionsAnalysis extends Analysis<Exp>{

    private Map<ICode, Set<Exp>> genSets;
    private Map<ICode, Set<Exp>> killSets;

    public AnticipatedExpressionsAnalysis(FlowGraph flowGraph, Set<Exp> globalFlowSet) {
        super(flowGraph, Direction.BACKWARDS, Meet.INTERSECTION, globalFlowSet);
        genSets = new HashMap<ICode, Set<Exp>>();
        killSets =  new HashMap<ICode, Set<Exp>>();

        for(BlockNode block : flowGraph.getBlocks()){
            List<ICode> codeList = block.getICode();
            for(int i = codeList.size(); i >= 0; i--){
                Set<Exp> instructionKill = new HashSet<Exp>();
                Set<Exp> instructionGen = new HashSet<Exp>();
                ICode icode = codeList.get(i);
                if(icode instanceof Assign){
                    Assign assIcode = (Assign)icode;
                    if(assIcode.value instanceof IdentExp){
                        IdentExp exp = (IdentExp)assIcode.value;
                        int defIndex = searchForDefinition(codeList, i, exp.ident);
                        if(defIndex != -1){
                            if(!searchForPreviousExpression(codeList, defIndex, exp)){
                                instructionKill.add(exp);
                            }
                        } else {
                                instructionGen.add(exp);
                        }
                    } else if(assIcode.value instanceof UnExp){
                        UnExp exp = (UnExp)assIcode.value;

                        if(!exp.right.isConstant()){
                            int defIndex = searchForDefinition(codeList, i, exp.right.toString());

                            if(defIndex != -1){
                                if(!searchForPreviousExpression(codeList, defIndex, exp)){
                                    instructionKill.add(exp);
                                }
                            } else {
                                instructionGen.add(exp);
                            }
                        }
                    } else if(assIcode.value instanceof BinExp){
                        BinExp exp = (BinExp)assIcode.value;

                        int defIndex1 = searchForDefinition(codeList, i, exp.left.toString());
                        int defIndex2 = searchForDefinition(codeList, i, exp.right.toString());

                        if(defIndex1 != -1 || defIndex2 != -1){
                            boolean shouldKill = false;
                            if(defIndex1 != -1 && !searchForPreviousExpression(codeList, defIndex1, exp)){
                                shouldKill = true;
                            }

                            if(defIndex2 != -1 && !searchForPreviousExpression(codeList, defIndex2, exp)){
                                shouldKill = true;
                            }

                            if(shouldKill){
                                instructionKill.add(exp);
                            }
                        } else {
                            instructionGen.add(exp);
                        }
                    }
                }
                killSets.put(icode, instructionKill);
                genSets.put(icode, instructionGen);
            }
        }
    }

    private boolean searchForPreviousExpression(List<ICode> codeList, int defIndex, Exp defIdent){
        for(int i = defIndex - 1; defIndex >= 0; i--){
            ICode icode = codeList.get(i);
            if(icode instanceof Assign){
                Assign assICode = (Assign)icode;
                if(defIdent.equals(assICode.value)){
                    return true;
                }
            }
        }

        return false;
    }

    private int searchForDefinition(List<ICode> codeList, int i, String var) {
        for(int x = i - 1; x >= 0; x--){
            ICode icode = codeList.get(x);
            if(icode instanceof Assign){
                Assign icodeAss = (Assign)icode;
                String defVal = icodeAss.place;
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
