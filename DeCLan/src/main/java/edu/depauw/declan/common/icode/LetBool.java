package edu.depauw.declan.common.icode;

public class LetBool implements ICode {
	private String place;
	private boolean value;
	
	public LetBool(String place, boolean value) {
		this.place = place;
		this.value = value;
	}

	@Override
	public String toString() {
	    return  (value) ? place + " := TRUE" : place + " := FALSE";
	}
}
