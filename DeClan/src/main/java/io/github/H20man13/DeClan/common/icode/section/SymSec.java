package io.github.H20man13.DeClan.common.icode.section;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.symbols.ParamSymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.RetSymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.VarSymEntry;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.symboltable.entry.VariableEntry;

public class SymSec implements ICode {
    public SymSec(){
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
    public String toString(){
        return "SYMBOL SECTION\r\n";
    }

    @Override
    public P asPattern() {
        return P.PAT(P.SYMBOL(), P.SECTION());
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof SymSec){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean containsPlace(String place) {
        return false;
    }

    @Override
    public boolean containsLabel(String label) {
        return false;
    }

    @Override
    public void replacePlace(String from, String to) {
        //Do nothing
    }

    @Override
    public void replaceLabel(String from, String to) {
        //Do nothing
    }
}
