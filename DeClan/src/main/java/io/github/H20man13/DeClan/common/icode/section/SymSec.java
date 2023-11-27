package io.github.H20man13.DeClan.common.icode.section;

import java.util.List;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.SymEntry;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.symboltable.Environment;

public class SymSec implements ICode {
    public List<SymEntry> entries;

    public SymSec(List<SymEntry> entries){
        this.entries = entries;
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
        StringBuilder sb = new StringBuilder();
        sb.append("SYMBOL SECTION\r\n");
        for(SymEntry entry : entries){
            sb.append(' ');
            sb.append(entry.toString());
            sb.append("\r\n");
        }
        return sb.toString();
    }

    @Override
    public P asPattern() {
        int size = entries.size();
        P[] patterns = new P[size];
        for(int i = 0; i < size; i++){
            patterns[i] = entries.get(i).asPattern();
        }
        return P.PAT(patterns);
    }

}
