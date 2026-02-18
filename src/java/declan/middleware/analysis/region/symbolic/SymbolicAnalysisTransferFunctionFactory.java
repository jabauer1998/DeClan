package io.github.h20man13.DeClan.common.analysis.region.symbolic;

import io.github.h20man13.DeClan.common.CopyStr;
import io.github.h20man13.DeClan.common.Tuple;
import io.github.h20man13.DeClan.common.analysis.region.expr.Expr;
import io.github.h20man13.DeClan.common.analysis.region.function.RegionTransferFunctionFactory;
import io.github.h20man13.DeClan.common.analysis.region.function.SetExpression;

public class SymbolicAnalysisTransferFunctionFactory extends RegionTransferFunctionFactory<Tuple<CopyStr, Expr>> {
	public SymbolicAnalysisTransferFunctionFactory() {
		super();
	}
	
	public SymbolicMeetOperator produceSymbolicAnalysisMeetOperator(SetExpression<Tuple<CopyStr, Expr>> exp1, SetExpression<Tuple<CopyStr, Expr>> exp2) {
		return new SymbolicMeetOperator(exp1, exp2);
	}
}
