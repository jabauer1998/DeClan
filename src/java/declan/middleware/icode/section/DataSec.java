package io.github.h20man13.DeClan.common.icode.section;

import io.github.h20man13.DeClan.common.icode.ICode;
import io.github.h20man13.DeClan.common.pat.P;

public class DataSec extends ICode {
    public DataSec(){}

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
        return obj instanceof DataSec;
    }
    
    @Override
    public P asPattern() {
        return P.PAT(P.DATA(), P.SECTION());
    }

    @Override
    public String toString(){
        return "DATA SECTION";
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
		return new DataSec();
	}
	
	@Override
	public int hashCode() {
		return DataSec.class.hashCode();
	}
}
