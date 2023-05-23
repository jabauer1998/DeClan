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

public class AnticipatedExpressionsAnalysis extends Analysis<Exp>{

    private Map<FlowGraphNode, Set<Exp>> genSets;
    private Map<FlowGraphNode, Set<Exp>> killSets;

    public AnticipatedExpressionsAnalysis(FlowGraph flowGraph, Set<Exp> globalFlowSet) {
        super(flowGraph, Direction.BACKWARDS, Meet.INTERSECTION, globalFlowSet);
        genSets = new HashMap<FlowGraphNode, Set<Exp>>();
        killSets =  new HashMap<FlowGraphNode, Set<Exp>>();

        for(BlockNode block : flowGraph.getBlocks()){
            Set<Exp> blockKill = new HashSet<Exp>();
            Set<Exp> blockGen = new HashSet<Exp>();
            List<ICode> codeList = block.getICode();
            for(int i = codeList.size(); i >= 0; i--){
                ICode icode = codeList.get(i);
                if(icode instanceof LetVar){
                    LetVar value = (LetVar)icode;
                    
                    int defIndex = searchForDefinition(codeList, i, value.var);
                    IdentExp defIdent = new IdentExp(value.var);
                    if(defIndex != -1){
                        if(!searchForPreviousExpression(codeList, defIndex, defIdent)){
                            blockKill.add(defIdent);
                        }
                    } else {
                        blockGen.add(defIdent);
                    }
                } else if(icode instanceof LetUn){
                    LetUn value = (LetUn)icode;
                    int defIndex = searchForDefinition(codeList, i, value.value);
                    IdentExp iExp = new IdentExp(value.value);
                    UnExp uExp = new UnExp(Utils.getOp(value.op), iExp);

                    if(defIndex != -1){
                        if(!searchForPreviousExpression(codeList, defIndex, uExp)){
                            blockKill.add(uExp);
                        }
                    } else {
                        blockGen.add(uExp);
                    }
                } else if(icode instanceof LetBin){
                    LetBin value = (LetBin)icode;
                    
                    int defIndex1 = searchForDefinition(codeList, i, value.left);
                    int defIndex2 = searchForDefinition(codeList, i, value.right);

                    IdentExp lExp = new IdentExp(value.left);
                    IdentExp rExp = new IdentExp(value.right);
                    BinExp bExp = new BinExp(lExp, Utils.getOp(value.op), rExp);
                    if(defIndex1 != -1 || defIndex2 != -1){
                        boolean shouldKill = false;
                        if(defIndex1 != -1 && !searchForPreviousExpression(codeList, defIndex1, bExp)){
                            shouldKill = true;
                        }

                        if(defIndex2 != -1 && !searchForPreviousExpression(codeList, defIndex2, bExp)){
                            shouldKill = true;
                        }

                        if(shouldKill){
                            blockKill.add(bExp);
                        }
                    } else {
                        blockGen.add(bExp);
                    }
                }
            }

            killSets.put(block, blockKill);
            genSets.put(block, blockGen);
        }
    }

    private boolean searchForPreviousExpression(List<ICode> codeList, int defIndex, Exp defIdent){
        for(int i = defIndex - 1; defIndex >= 0; i--){
            ICode icode = codeList.get(i);
            if(icode instanceof LetBool){
                LetBool icodeDef = (LetBool)icode;
                BoolExp bExp = new BoolExp(icodeDef.value);
                if(defIdent.equals(bExp)){
                    return true;
                }
            } else if(icode instanceof LetInt){
                LetInt icodeDef = (LetInt)icode;
                IntExp iExp = new IntExp(icodeDef.value);
                if(defIdent.equals(iExp)){
                    return true;
                }
            } else if(icode instanceof LetString){
                LetString icodeDef = (LetString)icode;
                StrExp sExp = new StrExp(icodeDef.value);
                if(defIdent.equals(sExp)){
                    return true;
                }
            } else if(icode instanceof LetReal){
                LetReal icodeDef = (LetReal)icode;
                RealExp rExp = new RealExp(icodeDef.value);
                if(defIdent.equals(rExp)){
                    return true;
                }
            } else if(icode instanceof LetVar){
                LetVar icodeDef = (LetVar)icode;
                IdentExp iExp = new IdentExp(icodeDef.var);
                if(defIdent.equals(iExp)){
                    return true;
                }
            } else if(icode instanceof LetUn){
                LetUn icodeDef = (LetUn)icode;
                IdentExp iExp = new IdentExp(icodeDef.value);
                UnExp uExp = new UnExp(Utils.getOp(icodeDef.op), iExp);
                if(defIdent.equals(uExp)){
                    return true;
                }
            } else if(icode instanceof LetBin){
                LetBin icodeDef = (LetBin)icode;
                IdentExp iExp1 = new IdentExp(icodeDef.left);
                IdentExp iExp2 = new IdentExp(icodeDef.right);
                BinExp bExp = new BinExp(iExp1, Utils.getOp(icodeDef.op), iExp2);
                if(defIdent.equals(bExp)){
                    return true;
                }
            }
        }

        return false;
    }

    private int searchForDefinition(List<ICode> codeList, int i, String var) {
        for(int x = i - 1; x >= 0; x--){
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
