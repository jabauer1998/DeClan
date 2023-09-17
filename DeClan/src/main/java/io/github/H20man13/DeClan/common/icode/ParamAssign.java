package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.common.pat.P;

public class ParamAssign implements ICode {
    public String newPlace;
    public String paramPlace;

    public ParamAssign(String newPlace, String paramPlace){
        this.newPlace = newPlace;
        this.paramPlace = paramPlace;
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
        return P.PAT(P.ID(), P.ASSIGN(), P.PARAM());   
    }

    @Override
    public String toString(){
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(newPlace);
        resultBuilder.append(" << ");
        resultBuilder.append(paramPlace);
        return resultBuilder.toString();
    }
}
