package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.pat.P;

public class Lib implements ICode {
    public DataSec variables;
    public ProcSec procedures;
    public End end;

    public Lib(DataSec variables, ProcSec procedures, End end){
        this.variables = variables;
        this.procedures = procedures;
        this.end = end;
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
        return P.PAT(variables.asPattern(), procedures.asPattern());
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(variables.toString());
        sb.append(procedures.toString());
        sb.append(end.toString());
        sb.append("\r\n");
        return sb.toString();
    }
}
