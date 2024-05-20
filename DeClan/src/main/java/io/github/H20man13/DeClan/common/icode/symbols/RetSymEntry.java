package io.github.H20man13.DeClan.common.icode.symbols;

import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.pat.P.ID;

public class RetSymEntry extends SymEntry{
    public String funcName;

    private static int constInternalRet = SymEntry.CONST | SymEntry.INTERNAL;
    private static int constExternalRet = SymEntry.CONST | SymEntry.EXTERNAL;
    private static int internalRet = SymEntry.INTERNAL;
    private static int externalRet = SymEntry.EXTERNAL;

    public RetSymEntry(String icodePlace, int symType, String funcName){
        super(symType, icodePlace);
        this.funcName = funcName;
    }

    @Override
    public P asPattern() {
        if(this.symType == constExternalRet) return P.PAT(P.ID(), P.CONST(), P.EXTERNAL(), P.RETURN(), P.ID());
        else if(this.symType == constInternalRet) return P.PAT(P.ID(), P.CONST(), P.INTERNAL(), P.RETURN(), P.ID());
        else if(this.symType == internalRet) return P.PAT(P.ID(), P.INTERNAL(), P.RETURN(), P.ID());
        else if(this.symType == externalRet) return P.PAT(P.ID(), P.EXTERNAL(), P.RETURN(), P.ID());
        else return null;
    }

    @Override
    public SymEntry copy() {
        return new RetSymEntry(icodePlace, symType, funcName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(icodePlace);
        sb.append(' ');
        
        if(this.containsQualities(CONST)){
            sb.append("CONST ");
        }

        if(this.containsQualities(INTERNAL)){
            sb.append("INTERNAL ");
        } else if(this.containsQualities(EXTERNAL)){
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
