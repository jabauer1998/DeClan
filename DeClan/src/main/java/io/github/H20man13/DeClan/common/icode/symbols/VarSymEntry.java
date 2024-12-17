package io.github.H20man13.DeClan.common.icode.symbols;

import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.pat.P;

public class VarSymEntry extends SymEntry {
    public String declanIdent;
    
    public VarSymEntry(String icodePlace, int type, String declanIdent){
        super(type, icodePlace);
        this.declanIdent = declanIdent;
    }
    
    private VarSymEntry(VarSymEntry toCopy) {
    	super(toCopy);
    	this.declanIdent = toCopy.declanIdent;
    }

    @Override
    public P asPattern() {
        if(containsAllQualities(SymEntry.CONST | SymEntry.EXTERNAL)) return P.PAT(P.ID(), P.CONST(), P.EXTERNAL(), P.ID());
        else if(containsAllQualities(SymEntry.CONST | SymEntry.INTERNAL)) return P.PAT(P.ID(), P.CONST(), P.INTERNAL(), P.ID());
        else if(containsAllQualities(SymEntry.INTERNAL)) return P.PAT(P.ID(), P.INTERNAL(), P.ID());
        else if(containsAllQualities(SymEntry.EXTERNAL)) return P.PAT(P.ID(), P.EXTERNAL(), P.ID());
        else return null;
    }

    @Override
    public SymEntry copy() {
        return new VarSymEntry(this);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("ENTRY ");
        sb.append(icodePlace);
        sb.append(' ');
        
        if(containsAllQualities(CONST)){
            sb.append("CONST ");
        }

        if(containsAllQualities(INTERNAL)){
            sb.append("INTERNAL ");
        } else if(containsAllQualities(EXTERNAL)){
            sb.append("EXTERNAL ");
        }

        sb.append(declanIdent);

        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof VarSymEntry){
            VarSymEntry otherEntry = (VarSymEntry)obj;
            if(otherEntry.declanIdent.equals(declanIdent))
                return super.equals(obj);
        }
        return false;
    }
}
