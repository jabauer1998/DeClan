package io.github.H20man13.DeClan.common.icode.procedure;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.pat.P.PLACE;

public class ExternalPlace implements ICode {
    public String place;
    public String retPlace;

    public ExternalPlace(String place, String retPlace){
        this.place = place;
        this.retPlace = retPlace;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(place);
        sb.append(" <| ");
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
        return P.PAT(P.ID(), P.EXTERNAL(), P.PLACE(), P.ID());
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof ExternalPlace){
            ExternalPlace ePlace = (ExternalPlace)obj;

            boolean placeEquals = ePlace.place.equals(place);
            boolean returnPlaceEquals = ePlace.retPlace.equals(retPlace);

            return placeEquals && returnPlaceEquals;
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
