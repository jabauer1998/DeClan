package io.github.H20man13.DeClan.common.icode.procedure;

import java.util.LinkedList;
import java.util.List;

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
        sb.append(" |< ");
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

    @Override
    public boolean equals(Object obj){
        if(obj instanceof InternalPlace){
            InternalPlace iPlace = (InternalPlace)obj;
             
            boolean placeEquals = iPlace.place.equals(place);
            boolean returnEquals = iPlace.retPlace.equals(retPlace);

            return placeEquals && returnEquals;
        } else {
            return false;
        }
    }

    @Override
    public List<ICode> genFlatCode() {
        LinkedList<ICode> resultList = new LinkedList<ICode>();
        resultList.add(this);
        return resultList;    
    }
}