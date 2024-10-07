package io.github.H20man13.DeClan.common.analysis.region.function;

import java.util.HashSet;
import java.util.Set;

public class IntersectionOperation<SetType> implements SetExpression<SetType>{
	private SetExpression<SetType> set1;
	private SetExpression<SetType> set2;
	
	public IntersectionOperation(SetExpression<SetType> set1, SetExpression<SetType> set2) {
		this.set1 = set1;
		this.set2 = set2;
	}
	
	@Override
	public Set<SetType> compute(Set<SetType> input) {
		HashSet<SetType> toRet = new HashSet<SetType>();
		toRet.addAll(set1.compute(input));
		toRet.retainAll(set2.compute(input));
		return toRet;
	}

}
