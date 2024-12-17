package io.github.H20man13.DeClan.common.icode.symbols;

import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.pat.P;

public class ParamSymEntry extends SymEntry {
    public int paramNumber;
    public String funcName;

    public ParamSymEntry(String icodePlace, int paramType, String funcName, int paramNumber){
        super(paramType | SymEntry.PARAM, icodePlace);
        this.paramNumber = paramNumber;
        this.funcName = funcName;
    }
    
    private ParamSymEntry(ParamSymEntry toCopy) {
    	super(toCopy);
    	this.paramNumber = toCopy.paramNumber;
    	this.funcName = toCopy.funcName;
    }

    @Override
    public P asPattern() {
        if(containsAllQualities(SymEntry.CONST | SymEntry.INTERNAL)) return P.PAT(P.ID(), P.CONST(), P.INTERNAL(), P.PARAM(), P.ID(), P.INT());
        else if(containsAllQualities(SymEntry.CONST | SymEntry.EXTERNAL)) return P.PAT(P.ID(), P.CONST(), P.EXTERNAL(), P.PARAM(), P.ID(), P.INT());
        else if(containsAllQualities(SymEntry.INTERNAL)) return P.PAT(P.ID(), P.INTERNAL(), P.PARAM(), P.ID(), P.INT());
        else if(containsAllQualities(SymEntry.EXTERNAL)) return P.PAT(P.ID(), P.EXTERNAL(), P.PARAM(), P.ID(), P.INT());
        else return null;
    }

    @Override
    public SymEntry copy() {
        return new ParamSymEntry(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("ENTRY ");
        sb.append(icodePlace.toString());
        sb.append(' ');
        if(containsAllQualities(CONST)){
            sb.append("CONST ");
        }
        if(containsAllQualities(INTERNAL)){
            sb.append("INTERNAL ");
        } else if(containsAllQualities(EXTERNAL)){
            sb.append("EXTERNAL ");
        }

        sb.append("PARAM ");
        sb.append(funcName);
        sb.append(' ');
        sb.append(paramNumber);

        return sb.toString();
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof ParamSymEntry){
            ParamSymEntry otherEntry = (ParamSymEntry)obj;
            if(otherEntry.funcName.equals(funcName))
                    if(otherEntry.paramNumber == paramNumber)
                            return super.equals(obj);
        }
        return false;
    }
}
