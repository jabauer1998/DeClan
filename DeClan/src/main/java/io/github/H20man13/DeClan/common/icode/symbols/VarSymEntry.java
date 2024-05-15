package io.github.H20man13.DeClan.common.icode.symbols;

import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.pat.P.ID;

public class VarSymEntry extends SymEntry {
    public String declanIdent;
    
    public VarSymEntry(String icodePlace, int type, String declanIdent){
        super(type, icodePlace);
        this.declanIdent = declanIdent;
    }

    @Override
    public P asPattern() {
        if(this.symType == (SymEntry.CONST | SymEntry.EXTERNAL)) return P.PAT(P.ID(), P.CONST(), P.EXTERNAL(), P.ID());
        else if(this.symType == (SymEntry.CONST | SymEntry.INTERNAL)) return P.PAT(P.ID(), P.CONST(), P.INTERNAL(), P.ID());
        else if(this.symType == (SymEntry.INTERNAL)) return P.PAT(P.ID(), P.INTERNAL(), P.ID());
        else if(this.symType == (SymEntry.EXTERNAL)) return P.PAT(P.ID(), P.EXTERNAL(), P.ID());
        else return null;
    }

    @Override
    public SymEntry copy() {
        return new VarSymEntry(icodePlace, symType, declanIdent);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append(icodePlace);
        sb.append(' ');
        
        if(containsQualities(CONST)){
            sb.append("CONST ");
        }

        if(containsQualities(INTERNAL)){
            sb.append("INTERNAL ");
        } else if(containsQualities(EXTERNAL)){
            sb.append("EXTERNAL ");
        }

        sb.append(declanIdent);

        return sb.toString();
    }
}
