package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.main.MyTypeChecker;

public class LetUn implements ICode {
	public String place;
	public UnExp unExp;

	public LetUn(String place, UnExp exp) {
		this.place = place;
		this.unExp = exp;
	}
	
	@Override
	public String toString() {
		return place + " := " + unExp.toString();
	}
}
