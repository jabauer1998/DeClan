package io.github.H20man13.DeClan.common.analysis.region.expr;

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
}
