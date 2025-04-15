package io.github.H20man13.DeClan.common.icode.section;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.pat.P;

public class SymSec implements ICode {
    public SymSec(){
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
    public String toString(){
        return "SYMBOL SECTION";
    }

    @Override
    public P asPattern() {
        return P.PAT(P.SYMBOL(), P.SECTION());
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof SymSec;
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
        //Do nothing
    }

    @Override
    public void replaceLabel(String from, String to) {
        //Do nothing
    }

	@Override
	public ICode copy() {
		return new SymSec();
	}
	
	@Override
	public int hashCode() {
		return SymSec.class.hashCode();
	}
}
