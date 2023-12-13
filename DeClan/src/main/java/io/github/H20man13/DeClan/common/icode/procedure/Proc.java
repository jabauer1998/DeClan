package io.github.H20man13.DeClan.common.icode.procedure;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
import io.github.H20man13.DeClan.common.pat.P;

public class Proc implements ICode {
    public ProcLabel label;
    public List<ParamAssign> paramAssign;
    public List<ICode> instructions;
    public InternalPlace placement;
    public Return returnStatement;

    public Proc(ProcLabel label, List<ParamAssign> paramAssign, List<ICode> instructions, InternalPlace place, Return returnStatement){
        this.label = label;
        this.paramAssign = paramAssign;
        this.instructions = instructions;
        this.placement = place;
        this.returnStatement = returnStatement;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public P asPattern() {
        int paramLength = paramAssign.size();
        int instructionsLength = instructions.size();
        int totalLength = paramLength + instructionsLength + 2;
        if(placement != null){
            totalLength++;
        }

        P[] globalPatterns = new P[instructionsLength];
        int globalIndex = 0;

        globalPatterns[globalIndex] = label.asPattern();
        globalIndex++;

        for(int i = 0; i < paramLength; i++){
            globalPatterns[globalIndex] = paramAssign.get(i).asPattern();
            globalIndex++;
        }
        
        
        for(int i = 0; i < instructionsLength; i++){
            globalPatterns[globalIndex] = instructions.get(i).asPattern();
            globalIndex++;
        }

        if(placement != null){
            globalPatterns[globalIndex] = placement.asPattern();
            globalIndex++;
        }

        globalPatterns[globalIndex] = returnStatement.asPattern();

        return P.PAT(globalPatterns);
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof Proc){
            Proc objProc = (Proc)obj;

            if(!objProc.label.equals(label))
                return false;

            if(objProc.paramAssign.size() != paramAssign.size())
                return false;

            for(int i = 0; i < paramAssign.size(); i++){
                ParamAssign assign1 = paramAssign.get(i);
                ParamAssign assign2 = objProc.paramAssign.get(i);

                if(!assign1.equals(assign2))
                    return false;
            }

            if(objProc.instructions.size() != instructions.size())
                return false;

            for(int i = 0; i < instructions.size(); i++){
                ICode instr1 = instructions.get(i);
                ICode instr2 = objProc.instructions.get(i);

                if(!instr1.equals(instr2))
                    return false;
            }

            if(!objProc.placement.equals(placement))
                return false;

            if(!objProc.returnStatement.equals(returnStatement))
                return false;

            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(label.toString());
        sb.append("\r\n  ");
        for(ParamAssign assign: paramAssign){
            sb.append(assign.toString());
            sb.append("\r\n  ");
        }
        for(ICode instruction: instructions){
            sb.append(instruction.toString());
            sb.append("\r\n  ");
        }
        if(placement != null){
            sb.append(placement.toString());
            sb.append("\r\n  ");
        }
        
        sb.append(returnStatement.toString());

        return sb.toString();
    }

    @Override
    public List<ICode> genFlatCode() {
        LinkedList<ICode> toRet = new LinkedList<ICode>();

        toRet.addAll(label.genFlatCode());

        for(ParamAssign assign : paramAssign){
            toRet.addAll(assign.genFlatCode());
        }

        for(ICode icode: instructions){
            toRet.addAll(icode.genFlatCode());
        }

        if(placement != null)
            toRet.addAll(placement.genFlatCode());

        toRet.addAll(returnStatement.genFlatCode());

        return toRet;
    }
}
