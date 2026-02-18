package declan.middleware.analysis.region.expr;

import java.util.Objects;

import declan.utils.Tuple;

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
