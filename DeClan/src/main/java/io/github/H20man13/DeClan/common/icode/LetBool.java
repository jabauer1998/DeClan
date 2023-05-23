package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.common.icode.exp.BoolExp;

public class LetBool implements ICode {
	public String place;
	public BoolExp value;
	
	public LetBool(String place, BoolExp value) {
		this.place = place;
		this.value = value;
	}

	public LetBool(String place, boolean value){
		this.place = place;
		this.value = new BoolExp(value);
	}

	@Override
	public String toString() {
	    return  (value.trueFalse) ? place + " := TRUE" : place + " := FALSE";
	}
}
