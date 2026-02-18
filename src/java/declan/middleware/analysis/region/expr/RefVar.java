package io.github.h20man13.DeClan.common.analysis.region.expr;

import io.github.h20man13.DeClan.common.Tuple;

public class RefVar implements Expr {
	public String varName;
	
	public RefVar(String varName) {
		this.varName = varName;
	}
	
	@Override
	public String toString() {
		return varName;
	}
	
	@Override
	public int hashCode() {
		return varName.hashCode();
	}

	@Override
	public Expr simplify() {
		return this;
	}

	@Override
	public Expr copy() {
		return new RefVar(varName);
	}
}
