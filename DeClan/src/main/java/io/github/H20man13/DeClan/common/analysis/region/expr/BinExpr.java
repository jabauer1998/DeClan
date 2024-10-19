package io.github.H20man13.DeClan.common.analysis.region.expr;

import java.util.Objects;

public class BinExpr implements Expr{
	public Expr left;
	public Expr right;
	public Operator op;
	public enum Operator{
		IPLUS,
		IMINUS,
		BAND,
		BOR,
		LAND,
		LOR
	}
	
	public BinExpr(Expr left, Operator op, Expr right) {
		this.left = left;
		this.right = right;
		this.op = op;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		sb.append(this.left.toString());
		sb.append(' ');
		switch(op) {
		case IPLUS: sb.append('+');
		break;
		case IMINUS: sb.append('-');
		break;
		case BAND: sb.append('&');
		break;
		case BOR: sb.append("|");
		break;
		case LAND: sb.append("&&");
		break;
		case LOR: sb.append("||");
		break;
		}
		sb.append(' ');
		sb.append(this.right.toString());
		sb.append(')');
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(left, op, right);
	}
}
