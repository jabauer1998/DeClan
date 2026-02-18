package io.github.h20man13.DeClan.common.analysis.region.function;

import java.util.HashSet;
import java.util.Set;

public class SubtractionOperation<SetType> implements SetExpression<SetType> {
	private SetExpression<SetType> side1;
	private SetExpression<SetType> side2;
	
	public SubtractionOperation(SetExpression<SetType> side1, SetExpression<SetType> side2){
		this.side1 = side1;
		this.side2 = side2;
	}
	
	@Override
	public Set<SetType> compute(Set<SetType> input) {
		Set<SetType> result = new HashSet<SetType>();
		result.addAll(side1.compute(input));
		result.removeAll(side2.compute(input));
		return result;
	}
	
}
