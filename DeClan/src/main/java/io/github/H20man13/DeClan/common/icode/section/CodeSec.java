package io.github.H20man13.DeClan.common.icode.section;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.util.Utils;

public class CodeSec implements ICode {
    public CodeSec(){
        //Do nothing
    };

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
        return P.PAT(P.CODE(), P.SECTION());
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("CODE SECTION\r\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof CodeSec){
            return true;
        } else {
            return false;
        }
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
    public void replacePlace(String from, String to) {
        //Do noothing
    }

    @Override
    public void replaceLabel(String from, String to) {
        //Do nothing
    }
}
