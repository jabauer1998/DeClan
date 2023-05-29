package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.common.icode.exp.Exp;

public class Assign implements ICode{
    public String place;
    public Exp value;

    public Assign(String place, Exp value){
        this.place = place;
        this.value = value;
    }

    @Override
	public String toString() {
		return place + " := " + value.toString();
	}

    @Override
    public boolean isConstant() {
        return value.isConstant();
    }

    @Override
    public boolean isBranch() {
        return value.isBranch();
    }
}
