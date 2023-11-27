package io.github.H20man13.DeClan.common.icode.procedure;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.pat.P;

public class InternalPlace implements ICode {
    public String place;
    public String retPlace;

    public InternalPlace(String place, String retPlace){
        this.place = place;
        this.retPlace = retPlace;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(place);
        sb.append(" &< ");
        sb.append(retPlace);
        return sb.toString();
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
        return P.PAT(P.ID(), P.INTERNAL(), P.PLACE(), P.ID());
    }
}