package io.github.H20man13.DeClan.common.analysis.region.symbolic;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.analysis.region.expr.Expr;
import io.github.H20man13.DeClan.common.analysis.region.function.RegionTransferFunctionFactory;
import io.github.H20man13.DeClan.common.analysis.region.function.SetExpression;

public class SymbolicAnalysisTransferFunctionFactory extends RegionTransferFunctionFactory<Tuple<String, Expr>> {
	public SymbolicAnalysisTransferFunctionFactory() {
		super();
	}
	
	public SymbolicMeetOperator produceSymbolicAnalysisMeetOperator(SetExpression<Tuple<String, Expr>> exp1, SetExpression<Tuple<String, Expr>> exp2) {
		return new SymbolicMeetOperator(exp1, exp2);
	}
}
