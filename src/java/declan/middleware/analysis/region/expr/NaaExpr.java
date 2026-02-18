package declan.middleware.analysis.region.expr;

import java.util.Objects;

import declan.utils.Tuple;
import declan.middleware.icode.exp.Exp;

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
