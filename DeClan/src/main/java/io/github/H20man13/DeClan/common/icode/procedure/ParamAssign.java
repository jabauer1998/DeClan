package io.github.H20man13.DeClan.common.icode.procedure;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.icode.ICode;
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
        resultBuilder.append(" <- ");
        resultBuilder.append(paramPlace);
        return resultBuilder.toString();
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof ParamAssign){
            ParamAssign assign = (ParamAssign)obj;

            boolean paramEquals = assign.paramPlace.equals(paramPlace);
            boolean newEquals = assign.newPlace.equals(newPlace);

            return paramEquals && newEquals;
        } else {
            return false;
        }
    }

    @Override
    public List<ICode> genFlatCode() {
        List<ICode> resultList = new LinkedList<ICode>();
        resultList.add(this);
        return resultList;
    }
}
