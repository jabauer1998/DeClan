package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.icode.section.SymSec;
import io.github.H20man13.DeClan.common.pat.P;

public class Prog implements ICode {
    public SymSec symbols;
    public DataSec variables;
    public ProcSec procedures;
    public CodeSec code;

    public Prog(SymSec symbols, DataSec variables, ProcSec procedures, CodeSec code){
        this.symbols = symbols;
        this.variables = variables;
        this.procedures = procedures;
        this.code = code;
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
        return P.PAT(variables.asPattern(), procedures.asPattern(), code.asPattern());
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(variables.toString());
        sb.append(procedures.toString());
        sb.append(code.toString());
        return sb.toString();
    }
}
