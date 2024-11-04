package io.github.H20man13.DeClan.common.analysis.region.expr;

import java.util.Objects;

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
}
