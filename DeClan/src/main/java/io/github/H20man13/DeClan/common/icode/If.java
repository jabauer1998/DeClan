package io.github.H20man13.DeClan.common.icode;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

	@Override
	public List<ICode> genFlatCode() {
		List<ICode> icode = new LinkedList<ICode>();
		icode.add(this);
		return icode;
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
	public boolean containsParamater(String place) {
		return false;
	}

	@Override
	public boolean containsArgument(String place) {
		return false;
	}

	@Override
	public Set<String> paramaterForFunctions(String place) {
		return new HashSet<String>();
	}

	@Override
	public Set<String> argumentInFunctions(String place) {
		return new HashSet<String>();
	}

	@Override
	public Set<String> internalReturnForFunctions(String place) {
		return new HashSet<String>();
	}

	@Override
	public Set<String> externalReturnForFunctions(String place) {
		return new HashSet<String>();
	}

	@Override
	public boolean containsInternalReturn(String place) {
		return false;
	}

	@Override
	public boolean containsExternalReturn(String place) {
		return false;
	}
}
