package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.common.icode.exp.IdentExp;

public class LetVar implements ICode {
	public String place;
	public IdentExp var;

	public LetVar(String place, IdentExp var) {
		this.place = place;
		this.var = var;
	}

	public LetVar(String place, String val){
		this.place = place;
		this.var = new IdentExp(val);
	}

	@Override
	public String toString() {
		return place + " := " + var.toString();
	}
}
