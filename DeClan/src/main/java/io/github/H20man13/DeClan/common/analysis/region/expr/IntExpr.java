package io.github.H20man13.DeClan.common.analysis.region.expr;

import java.util.Objects;

import io.github.H20man13.DeClan.common.Tuple;

public class IntExpr implements Expr{
	public int value;
	
	public IntExpr(int val) {
		this.value = val;
	}
	
	public String toString() {
		return "" + value;
	}
	
	public int hashCode() {
		return Objects.hashCode(value);
	}

	@Override
	public Expr simplify() {
		return this;
	}

	@Override
	public Expr copy() {
		return new IntExpr(value);
	}
}
