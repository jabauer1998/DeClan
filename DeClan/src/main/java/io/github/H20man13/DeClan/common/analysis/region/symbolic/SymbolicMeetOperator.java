package io.github.H20man13.DeClan.common.analysis.region.symbolic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.analysis.region.expr.Expr;
import io.github.H20man13.DeClan.common.analysis.region.expr.NaaExpr;
import io.github.H20man13.DeClan.common.analysis.region.function.SetExpression;

public class SymbolicMeetOperator implements SetExpression<Tuple<String, Expr>> {
	private SetExpression<Tuple<String, Expr>> exp1;
	private SetExpression<Tuple<String, Expr>> exp2;
	public SymbolicMeetOperator(SetExpression<Tuple<String, Expr>> exp1, SetExpression<Tuple<String, Expr>> exp2) {
		this.exp1 = exp1;
		this.exp2 = exp2;
	}

	@Override
	public Set<Tuple<String, Expr>> compute(Set<Tuple<String, Expr>> input) {
		Map<String, Set<Tuple<String, Expr>>> storage = new HashMap<String, Set<Tuple<String, Expr>>>();
		Set<Tuple<String, Expr>> resultLeft = exp1.compute(input);
		Set<Tuple<String, Expr>> resultRight = exp2.compute(input);
		
		for(Tuple<String, Expr> result: resultLeft) {
			if(!storage.containsKey(result.source))
				storage.put(result.source, new HashSet<Tuple<String, Expr>>());
			
			storage.get(result.source).add(result);
		}
		
		for(Tuple<String, Expr> result: resultRight) {
			if(!storage.containsKey(result.source))
				storage.put(result.source, new HashSet<Tuple<String, Expr>>());
			
			storage.get(result.source).add(result);
		}
		
		Set<Tuple<String, Expr>> tempResult = new HashSet<Tuple<String, Expr>>();
		for(String key: storage.keySet()) {
			Set<Tuple<String, Expr>> result = storage.get(key);
			int size = result.size();
			if(size > 1) {
				tempResult.add(new Tuple<String, Expr>(key, new NaaExpr()));
			} else if(size == 1) {
				for(Tuple<String, Expr> one: result) {
					tempResult.add(one);
					break;
				}
			}
		}
		
		return tempResult;
	}
}
