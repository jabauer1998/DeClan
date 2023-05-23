package io.github.H20man13.DeClan.common.icode;

import edu.depauw.declan.common.ast.BinaryOperation;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.main.MyTypeChecker;

public class LetBin implements ICode {
	public String place;
	public BinExp exp;

	public LetBin(String place, BinExp exp) {
		this.place = place;
		this.exp = exp;
	}
	
	@Override
	public String toString() {
		return place + " := " + exp.toString();
	}
}
