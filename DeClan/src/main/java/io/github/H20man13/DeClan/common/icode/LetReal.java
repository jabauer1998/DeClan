package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.common.icode.exp.RealExp;

public class LetReal implements ICode {
	public String place;
	public RealExp value;
	
	public LetReal(String place, RealExp value) {
		this.place = place;
		this.value = value;
	}

	public LetReal(String place, double value){
		this.place = place;
		this.value = new RealExp(value);
	}

	@Override
	public String toString() {
		return place + " := " + value.toString();
	}
}
