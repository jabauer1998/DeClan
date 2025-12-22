package io.github.H20man13.DeClan.common.icode.exp;

import java.util.Objects;

import io.github.H20man13.DeClan.common.pat.P;

public class IntExp implements Exp {
    public int value;

    public IntExp(int value){
        this.value = value;
    }

    @Override
    public boolean equals(Object exp) {
        if(exp instanceof IntExp){
            IntExp intExp = (IntExp)exp;
            return this.value == intExp.value;
        } else {
            return false;
        }
    } 

    @Override
    public String toString(){
        return Integer.toUnsignedString(value);
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public P asPattern(boolean hasContainer) {
        if(hasContainer){
            return P.PAT(P.INT());
        } else {
            return P.INT();
        }
    }

    @Override
    public boolean containsPlace(String place) {
        return false;
    }

    @Override
    public void replacePlace(String from, String to) {
        //Do nothing
    }
    
    @Override
    public int hashCode() {
    	return Objects.hashCode(value);
    }

	@Override
	public Exp copy() {
		return new IntExp(value);
	}

	@Override
	public boolean isZero() {
		return value == 0;
	}
}
