package io.github.H20man13.DeClan.common.icode.symbols;

import io.github.H20man13.DeClan.common.pat.P;

public class RetSymEntry extends SymEntry{
    public String funcName;

    public RetSymEntry(String icodePlace, int symType, String funcName){
        super(symType | SymEntry.RETURN, icodePlace);
        this.funcName = funcName;
    }
    
    private RetSymEntry(RetSymEntry toCopy) {
    	super(toCopy);
    	this.funcName = toCopy.funcName;
    }

    @Override
    public P asPattern() {
        if(containsAllQualities(SymEntry.CONST | SymEntry.INTERNAL)) return P.PAT(P.ID(), P.CONST(), P.EXTERNAL(), P.RETURN(), P.ID());
        else if(containsAllQualities(SymEntry.CONST | SymEntry.EXTERNAL)) return P.PAT(P.ID(), P.CONST(), P.INTERNAL(), P.RETURN(), P.ID());
        else if(containsAllQualities(SymEntry.INTERNAL)) return P.PAT(P.ID(), P.INTERNAL(), P.RETURN(), P.ID());
        else if(containsAllQualities(SymEntry.EXTERNAL)) return P.PAT(P.ID(), P.EXTERNAL(), P.RETURN(), P.ID());
        else return null;
    }

    @Override
    public SymEntry copy() {
        return new RetSymEntry(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("ENTRY ");
        sb.append(icodePlace);
        sb.append(' ');
        
        if(this.containsAllQualities(CONST)){
            sb.append("CONST ");
        }

        if(this.containsAllQualities(INTERNAL)){
            sb.append("INTERNAL ");
        } else if(this.containsAllQualities(EXTERNAL)){
            sb.append("EXTERNAL ");
        }

        sb.append("RETURN ");
        sb.append(funcName);
        
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof RetSymEntry){
            RetSymEntry otherEntry = (RetSymEntry)obj;
            if(otherEntry.funcName.equals(funcName))
                return super.equals(obj);
        }
        return false;
    }
}
