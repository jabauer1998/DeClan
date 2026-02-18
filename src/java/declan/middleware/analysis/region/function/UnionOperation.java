package io.github.h20man13.DeClan.common.analysis.region.function;

import java.util.HashSet;
import java.util.Set;

public class UnionOperation<SetType> implements SetExpression<SetType> {
	private SetExpression<SetType> set1;
	private SetExpression<SetType> set2;
	
	public UnionOperation(SetExpression<SetType> set1, SetExpression<SetType> set2) {
		this.set1 = set1;
		this.set2 = set2;
	}
	
	@Override
	public Set<SetType> compute(Set<SetType> input) {
		HashSet<SetType> toRet = new HashSet<SetType>();
		toRet.addAll(set1.compute(input));
		toRet.addAll(set2.compute(input));
		return toRet;
	}
}
