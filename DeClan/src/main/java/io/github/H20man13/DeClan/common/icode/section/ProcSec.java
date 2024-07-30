package io.github.H20man13.DeClan.common.icode.section;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.pat.P;

public class ProcSec implements ICode {
    public ProcSec(){
        //Do nothing
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
    public boolean equals(Object obj){
        if(obj instanceof ProcSec){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public P asPattern() {
        return P.PAT(P.PROC(), P.SECTION());
    }

    @Override
    public String toString(){
        return "PROC SECTION";
    }

    @Override
    public boolean containsPlace(String place) {
        return false;
    }

    @Override
    public boolean containsLabel(String label) {
        return false;
    }

    @Override
    public void replacePlace(String from, String to){
        //Do nothing
    }

    @Override
    public void replaceLabel(String from, String to) {
        //Do nothing  
    }
}
