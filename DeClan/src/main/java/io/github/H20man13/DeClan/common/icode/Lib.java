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

    public Lib(){
        this.variables = new DataSec();
        this.procedures = new ProcSec();
        this.symbols = new SymSec();
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

    public boolean dataSectionContainsInstruction(ICode icode){
        int variablesLength = variables.getLength();
        for(int i = 0; i < variablesLength; i++){
            ICode instructionAtIndex = variables.getInstruction(i);
            if(instructionAtIndex.equals(icode))
                return true;
        }

        return false;
    }

    public boolean containsPlace(String place){
        if(symbols.containsPlace(place))
            return true;
        if(variables.containsPlace(place))
            return true;
        if(procedures.containsPlace(place))
            return true;

        return false;
    }

    public boolean containsLabel(String label){
        if(variables.containsLabel(label))
            return true;

        if(procedures.containsLabel(label))
            return true;

        return false;
    }

    @Override
    public List<ICode> genFlatCode() {
        LinkedList<ICode> resultList = new LinkedList<ICode>();
        resultList.addAll(variables.genFlatCode());
        resultList.addAll(procedures.genFlatCode());
        return resultList;
    }

    @Override
    public void replacePlace(String from, String to) {
        symbols.replacePlace(from, to);
        variables.replacePlace(from, to);
        procedures.replacePlace(from, to);
    }

    @Override
    public void replaceLabel(String from, String to) {
        variables.replaceLabel(from, to);
        procedures.replaceLabel(from, to);
    }
}
