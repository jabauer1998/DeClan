package declan.middleware.analysis.region.expr;

import declan.utils.Copyable;
import declan.utils.Tuple;
import declan.frontend.ast.ExpressionVisitor;

public interface Expr extends Copyable<Expr>{
	@Override
	public String toString();
	@Override
	public int hashCode();
	public Expr simplify();
}
