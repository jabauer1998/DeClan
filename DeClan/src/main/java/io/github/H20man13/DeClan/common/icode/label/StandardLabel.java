package io.github.H20man13.DeClan.common.icode.label;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedTransferQueue;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.pat.P.LABEL;

public class StandardLabel extends Label {

    public StandardLabel(String label) {
        super(label);
    }

    @Override
    public P asPattern() {
        return P.PAT(P.LABEL(), P.ID());
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("LABEL ");
        sb.append(label);
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof StandardLabel){
            StandardLabel label = (StandardLabel)obj;
            return label.label.equals(this.label);
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

    @Override
    public boolean containsParamater(String place) {
        return false;
    }

    @Override
    public Set<String> paramaterForFunctions(String place) {
        return new HashSet<String>();
    }

    @Override
    public Set<String> argumentInFunctions(String place) {
        return new HashSet<String>();
    }

    @Override
    public boolean containsArgument(String place) {
        return false;
    }

    @Override
    public Set<String> internalReturnForFunctions(String place) {
        return new HashSet<String>();
    }

    @Override
    public Set<String> externalReturnForFunctions(String place) {
        return new HashSet<String>();
    }

    @Override
    public boolean containsReturn(String place) {
        return false;
    }
}
