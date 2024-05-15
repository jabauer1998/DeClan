package io.github.H20man13.DeClan.common.icode.symbols;

import io.github.H20man13.DeClan.common.pat.P;

public class ParamSymEntry extends SymEntry {
    public int paramNumber;
    public String funcName;

    private static int constInternalParam = SymEntry.CONST | SymEntry.INTERNAL;
    private static int constExternalParam = SymEntry.CONST | SymEntry.EXTERNAL;
    private static int internalParam = SymEntry.INTERNAL;
    private static int externalParam = SymEntry.EXTERNAL;

    public ParamSymEntry(String icodePlace, int paramType, String funcName, int paramNumber){
        super(paramType, icodePlace);
        this.paramNumber = paramNumber;
        this.funcName = funcName;
    }

    @Override
    public P asPattern() {
        if(this.symType == constInternalParam) return P.PAT(P.ID(), P.CONST(), P.INTERNAL(), P.PARAM(), P.ID(), P.INT());
        else if(this.symType == constExternalParam) return P.PAT(P.ID(), P.CONST(), P.EXTERNAL(), P.PARAM(), P.ID(), P.INT());
        else if(this.symType == internalParam) return P.PAT(P.ID(), P.INTERNAL(), P.PARAM(), P.ID(), P.INT());
        else if(this.symType == externalParam) return P.PAT(P.ID(), P.EXTERNAL(), P.PARAM(), P.ID(), P.INT());
        else return null;
    }

    @Override
    public SymEntry copy() {
        return new ParamSymEntry(icodePlace, symType, funcName, paramNumber);
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

        sb.append("PARAM ");
        sb.append(funcName);
        sb.append(' ');
        sb.append(paramNumber);

        return sb.toString();
    }
}
