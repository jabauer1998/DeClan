package io.github.H20man13.DeClan.common.icode;

import java.util.Objects;

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
		StringBuilder sb = new StringBuilder();
		sb.append("IF ");
		sb.append(exp.toString());
		sb.append("\r\nTHEN ");
		sb.append(ifTrue);
		sb.append("\r\nELSE ");
		sb.append(ifFalse);
		return sb.toString();
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

	@Override
	public boolean containsPlace(String place) {
		return exp.containsPlace(place);
	}

	@Override
	public boolean containsLabel(String label) {
		if(this.ifTrue.equals(label))
			return true;
		if(this.ifFalse.equals(label))
			return true;
		return false;
	}

	@Override
	public void replacePlace(String from, String to) {
		exp.replacePlace(from, to);
	}

	@Override
	public void replaceLabel(String from, String to) {
		if(this.ifTrue.equals(from))
			this.ifTrue = to;

		if(this.ifFalse.equals(from))
			this.ifFalse = to;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(exp, ifTrue, ifFalse);
	}

	@Override
	public ICode copy() {
		return new If((BinExp)exp.copy(), ifTrue, ifFalse);
	}
}
