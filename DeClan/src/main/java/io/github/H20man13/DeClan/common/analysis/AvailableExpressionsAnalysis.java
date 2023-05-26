package io.github.H20man13.DeClan.common.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.util.Utils;

public class AvailableExpressionsAnalysis extends Analysis<Exp> {

    private Map<FlowGraphNode, Set<Exp>> genSets;
    private Map<FlowGraphNode, Set<Exp>> killSets;

    public AvailableExpressionsAnalysis(FlowGraph flowGraph, Set<Exp> globalFlowSet) {
        super(flowGraph, Direction.FORWARDS, Meet.INTERSECTION, globalFlowSet);
        genSets = new HashMap<FlowGraphNode, Set<Exp>>();
        killSets = new HashMap<FlowGraphNode, Set<Exp>>();

        for(BlockNode block : flowGraph.getBlocks()){
            Set<Exp> blockKill = new HashSet<Exp>();
            Set<Exp> blockGen = new HashSet<Exp>();
            List<ICode> codeList = block.getICode();
            for(int i = 0; i < codeList.size(); i++){
                ICode icode = codeList.get(i);
                if(icode instanceof LetVar){
                    LetVar value = (LetVar)icode;
                    
                    int defIndex = searchForDefinition(codeList, i, value.var.toString());
                    if(defIndex != -1){
                        if(!searchForSubsequentExpression(codeList, defIndex, value.var)){
                            blockKill.add(value.var);
                        }
                    } else {
                        blockGen.add(value.var);
                    }
                } else if(icode instanceof LetUn){
                    LetUn value = (LetUn)icode;
                    int defIndex = searchForDefinition(codeList, i, value.unExp.right.toString());

                    if(defIndex != -1){
                        if(!searchForSubsequentExpression(codeList, defIndex, value.unExp)){
                            blockKill.add(value.unExp);
                        }
                    } else {
                        blockGen.add(value.unExp);
                    }
                } else if(icode instanceof LetBin){
                    LetBin value = (LetBin)icode;
                    
                    int defIndex1 = searchForDefinition(codeList, i, value.exp.left.toString());
                    int defIndex2 = searchForDefinition(codeList, i, value.exp.right.toString());

                    if(defIndex1 != -1 || defIndex2 != -1){
                        boolean shouldKill = false;
                        if(defIndex1 != -1 && !searchForSubsequentExpression(codeList, defIndex1, value.exp.left)){
                            shouldKill = true;
                        }

                        if(defIndex2 != -1 && !searchForSubsequentExpression(codeList, defIndex2, value.exp.right)){
                            shouldKill = true;
                        }

                        if(shouldKill){
                            blockKill.add(value.exp);
                        }
                    } else {
                        blockGen.add(value.exp);
                    }
                }
            }

            killSets.put(block, blockKill);
            genSets.put(block, blockGen);
        }
    }

    private boolean searchForSubsequentExpression(List<ICode> codeList, int defIndex, Exp defIdent){
        for(int i = defIndex + 1; defIndex < codeList.size(); i++){
            ICode icode = codeList.get(i);
            if(icode instanceof LetBool){
                LetBool icodeDef = (LetBool)icode;
                if(defIdent.equals(icodeDef.value)){
                    return true;
                }
            } else if(icode instanceof LetInt){
                LetInt icodeDef = (LetInt)icode;
                if(defIdent.equals(icodeDef.value)){
                    return true;
                }
            } else if(icode instanceof LetString){
                LetString icodeDef = (LetString)icode;
                if(defIdent.equals(icodeDef.value)){
                    return true;
                }
            } else if(icode instanceof LetReal){
                LetReal icodeDef = (LetReal)icode;
                if(defIdent.equals(icodeDef.value)){
                    return true;
                }
            } else if(icode instanceof LetVar){
                LetVar icodeDef = (LetVar)icode;
                if(defIdent.equals(icodeDef.var)){
                    return true;
                }
            } else if(icode instanceof LetUn){
                LetUn icodeDef = (LetUn)icode;
                if(defIdent.equals(icodeDef.unExp)){
                    return true;
                }
            } else if(icode instanceof LetBin){
                LetBin icodeDef = (LetBin)icode;
                if(defIdent.equals(icodeDef.exp)){
                    return true;
                }
            }
        }

        return false;
    }

    private int searchForDefinition(List<ICode> codeList, int i, String var) {
        for(int x = i + 1; x < codeList.size(); x++){
            ICode icode = codeList.get(x);
            if(icode instanceof LetBool){
                LetBool icodeBool = (LetBool)icode;
                String defVal = icodeBool.place;
                if(var.equals(defVal)){
                    return x;
                }
            } else if (icode instanceof LetString){
                LetString icodeString = (LetString)icode;
                String defVal = icodeString.place;
                if(var.equals(defVal)){
                    return x;
                }
            } else if(icode instanceof LetInt){
                LetInt icodeInt = (LetInt)icode;
                String defVal = icodeInt.place;
                if(var.equals(defVal)){
                    return x;
                }
            } else if(icode instanceof LetReal){
                LetReal icodeReal = (LetReal)icode;
                String defVal = icodeReal.place;
                if(var.equals(defVal)){
                    return x;
                }
            } else if(icode instanceof LetVar){
                LetVar icodeVar = (LetVar)icode;
                String defVal = icodeVar.place;
                if(var.equals(defVal)){
                    return x;
                }
            } else if(icode instanceof LetUn){
                LetUn icodeVar = (LetUn)icode;
                String defVal = icodeVar.place;
                if(var.equals(defVal)){
                    return x;
                }
            } else if(icode instanceof LetBin){
                LetBin icodeVar = (LetBin)icode;
                String defVal = icodeVar.place;
                if(var.equals(defVal)){
                    return x;
                }
            }
        }
        return -1;
    }

    @Override
    public Set<Exp> transferFunction(FlowGraphNode Node, Set<Exp> inputSet) {
        Set<Exp> result = new HashSet<Exp>();

        result.addAll(inputSet);
        result.removeAll(killSets.get(Node));
        result.addAll(genSets.get(Node));

        return result;
    }
    
}
