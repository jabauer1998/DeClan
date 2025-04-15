package io.github.H20man13.DeClan.common.icode.section;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.pat.P;

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
        return "CODE SECTION";
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof CodeSec;
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

	@Override
	public ICode copy() {
		return new CodeSec();
	}
	
	@Override
	public int hashCode() {
		return CodeSec.class.hashCode();
	}
}
