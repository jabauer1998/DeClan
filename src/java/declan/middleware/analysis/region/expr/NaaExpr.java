package io.github.h20man13.DeClan.common.analysis.region.expr;

import java.util.Objects;

import io.github.h20man13.DeClan.common.Tuple;
import io.github.h20man13.DeClan.common.icode.exp.Exp;

public class NaaExpr implements Expr {
	public NaaExpr() {}
	
	public int hashCode() {
		return 0;
	}
	
	public String toString() {
		return "NAA";
	}

	@Override
	public Expr simplify() {
		return this;
	}

	@Override
	public Expr copy() {
		return new NaaExpr();
	}
}
