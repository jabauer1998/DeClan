package io.github.H20man13.DeClan.common.icode;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.icode.section.SymSec;
import io.github.H20man13.DeClan.common.pat.P;

public class Lib implements ICode {
    public DataSec variables;
    public ProcSec procedures;
    public SymSec symbols;
    public End end;

    public Lib(SymSec symbols, DataSec variables, ProcSec procedures){
        this.variables = variables;
        this.procedures = procedures;
        this.symbols = symbols;
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
    public boolean equals(Object obj){
        if(obj instanceof Lib){
            Lib lib = (Lib)obj;
            
            boolean symbolsEquals = lib.symbols.equals(symbols);
            boolean dataEquals = lib.variables.equals(variables);
            boolean procEquals = lib.procedures.equals(procedures);
            
            return symbolsEquals && dataEquals && procEquals;
        } else {
            return false;
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(symbols.toString());
        sb.append(variables.toString());
        sb.append(procedures.toString());
        return sb.toString();
    }

    @Override
    public List<ICode> genFlatCode() {
        LinkedList<ICode> resultList = new LinkedList<ICode>();
        resultList.addAll(variables.genFlatCode());
        resultList.addAll(procedures.genFlatCode());
        return resultList;
    }
}
