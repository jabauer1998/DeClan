package io.github.h20man13.DeClan.common.analysis.region.symbolic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.h20man13.DeClan.common.CopyStr;
import io.github.h20man13.DeClan.common.Tuple;
import io.github.h20man13.DeClan.common.analysis.region.expr.Expr;
import io.github.h20man13.DeClan.common.analysis.region.expr.NaaExpr;
import io.github.h20man13.DeClan.common.analysis.region.function.SetExpression;
import io.github.h20man13.DeClan.common.util.ConversionUtils;

public class SymbolicMeetOperator implements SetExpression<Tuple<CopyStr, Expr>> {
	private SetExpression<Tuple<CopyStr, Expr>> exp1;
	private SetExpression<Tuple<CopyStr, Expr>> exp2;
	public SymbolicMeetOperator(SetExpression<Tuple<CopyStr, Expr>> exp1, SetExpression<Tuple<CopyStr, Expr>> exp2) {
		this.exp1 = exp1;
		this.exp2 = exp2;
	}

	@Override
	public Set<Tuple<CopyStr, Expr>> compute(Set<Tuple<CopyStr, Expr>> input) {
		Map<String, Set<Tuple<CopyStr, Expr>>> storage = new HashMap<String, Set<Tuple<CopyStr, Expr>>>();
		Set<Tuple<CopyStr, Expr>> resultLeft = exp1.compute(input);
		Set<Tuple<CopyStr, Expr>> resultRight = exp2.compute(input);
		
		for(Tuple<CopyStr, Expr> result: resultLeft) {
			if(!storage.containsKey(result.source.toString()))
				storage.put(result.source.toString(), new HashSet<Tuple<CopyStr, Expr>>());
			
			storage.get(result.source.toString()).add(result);
		}
		
		for(Tuple<CopyStr, Expr> result: resultRight) {
			if(!storage.containsKey(result.source.toString()))
				storage.put(result.source.toString(), new HashSet<Tuple<CopyStr, Expr>>());
			
			storage.get(result.source.toString()).add(result);
		}
		
		Set<Tuple<CopyStr, Expr>> tempResult = new HashSet<Tuple<CopyStr, Expr>>();
		for(String key: storage.keySet()) {
			Set<Tuple<CopyStr, Expr>> result = storage.get(key);
			int size = result.size();
			if(size > 1) {
				tempResult.add(new Tuple<CopyStr, Expr>(ConversionUtils.newS(key), new NaaExpr()));
			} else if(size == 1) {
				for(Tuple<CopyStr, Expr> one: result) {
					tempResult.add(one);
					break;
				}
			}
		}
		
		return tempResult;
	}
}
