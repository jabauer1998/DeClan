package io.github.H20man13.DeClan.common.icode;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.icode.procedure.Proc;
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

    public Prog(SymSec symbols, DataSec variables, CodeSec code, ProcSec procedures){
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
    public boolean equals(Object obj){
        if(obj instanceof Prog){
            Prog program = (Prog)obj;

            boolean symEquals = program.symbols.equals(symbols);
            boolean dataEquals = program.variables.equals(variables);
            boolean codeEquals = program.code.equals(code);
            boolean procEquals = program.procedures.equals(procedures);

            return symEquals && dataEquals && codeEquals && procEquals;
        } else {
            return false;
        }
    }

    public List<ICode> genFlatCode(){
        LinkedList<ICode> resultList = new LinkedList<ICode>();
        resultList.addAll(variables.genFlatCode());
        resultList.addAll(code.genFlatCode());
        resultList.addAll(procedures.genFlatCode());
        return resultList;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(symbols.toString());
        sb.append(variables.toString());
        sb.append(code.toString());
        sb.append(procedures.toString());
        return sb.toString();
    }
}
