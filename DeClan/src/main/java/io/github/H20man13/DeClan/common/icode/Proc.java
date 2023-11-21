package io.github.H20man13.DeClan.common.icode;

import java.util.List;

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
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(label.toString());
        sb.append("\r\n");
        for(ParamAssign assign: paramAssign){
            sb.append(assign.toString());
            sb.append("\r\n");
        }
        for(ICode instruction: instructions){
            sb.append(instruction.toString());
            sb.append("\r\n");
        }
        if(placement != null){
            sb.append(placement.toString());
            sb.append("\r\n");
        }
        
        sb.append(returnStatement.toString());
        sb.append("\r\n");

        return sb.toString();
    }
}
