package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.pat.P;

public class If implements ICode {
	public BinExp exp;
	public String ifTrue, ifFalse;

	public If(BinExp exp, String ifTrue, String ifFalse) {
		this.exp = exp;
		this.ifTrue = ifTrue;
		this.ifFalse = ifFalse;
	}
	
	@Override
	public String toString() {
		return "IF " + exp.toString() + " THEN " + ifTrue + " ELSE " + ifFalse;
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public boolean equals(Object obj){
		if(obj instanceof If){
			If objIf = (If)obj;

			boolean falseEqual = objIf.ifFalse.equals(ifFalse);
			boolean trueEqual = objIf.ifTrue.equals(ifTrue);
			boolean expEqual = objIf.exp.equals(exp);
			return falseEqual && trueEqual && expEqual;
		} else {
			return false;
		}
	}

	@Override
	public boolean isBranch() {
		return true;
	}

	@Override
	public P asPattern() {
		return P.PAT(P.IF(), exp.asPattern(true), P.THEN(), P.ID(), P.ELSE(), P.ID());
	}
}
