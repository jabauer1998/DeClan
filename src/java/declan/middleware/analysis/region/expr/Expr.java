package io.github.h20man13.DeClan.common.analysis.region.expr;

import io.github.h20man13.DeClan.common.Copyable;
import io.github.h20man13.DeClan.common.Tuple;
import io.github.h20man13.DeClan.common.ast.ExpressionVisitor;

public interface Expr extends Copyable<Expr>{
	@Override
	public String toString();
	@Override
	public int hashCode();
	public Expr simplify();
}
