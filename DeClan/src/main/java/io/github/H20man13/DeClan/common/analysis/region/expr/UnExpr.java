package io.github.H20man13.DeClan.common.analysis.region.expr;

import java.util.Objects;

public class UnExpr implements Expr {
	public Operator op;
	public Expr right;
	public enum Operator{
		INEG
	}
	
	public UnExpr(Operator op, Expr right) {
		this.op = op;
		this.right = right;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(op == Operator.INEG) {
			sb.append('-');
		}
		sb.append(right);
		return sb.toString();
	}
	
	public int hashCode() {
		return Objects.hash(op, right);
	}
}
