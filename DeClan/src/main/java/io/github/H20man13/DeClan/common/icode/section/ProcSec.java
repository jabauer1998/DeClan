package io.github.H20man13.DeClan.common.icode.section;

import java.util.List;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Proc;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.pat.Pattern;

public class ProcSec implements ICode {
    public List<Proc> procedures;

    public ProcSec(List<Proc> procedures){
        this.procedures = procedures;
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
        int size = procedures.size();
        P[] patList = new P[size];
        for(int i = 0; i < size; i++){
            patList[i] = procedures.get(i).asPattern();
        }
        return P.PAT(patList);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("PROC SECTION\r\n");
        for(Proc procedure: procedures){
            sb.append(' ');
            sb.append(procedure.toString());
            sb.append("\r\n");
        }
        return sb.toString();
    }
}
