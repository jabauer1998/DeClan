package declan.middleware.analysis.region.symbolic;

import declan.utils.CopyStr;
import declan.utils.Tuple;
import declan.middleware.analysis.region.expr.Expr;
import declan.middleware.analysis.region.function.RegionTransferFunctionFactory;
import declan.middleware.analysis.region.function.SetExpression;

public class SymbolicAnalysisTransferFunctionFactory extends RegionTransferFunctionFactory<Tuple<CopyStr, Expr>> {
	public SymbolicAnalysisTransferFunctionFactory() {
		super();
	}
	
	public SymbolicMeetOperator produceSymbolicAnalysisMeetOperator(SetExpression<Tuple<CopyStr, Expr>> exp1, SetExpression<Tuple<CopyStr, Expr>> exp2) {
		return new SymbolicMeetOperator(exp1, exp2);
	}
}
