package io.github.h20man13.DeClan.common.analysis.region.function;

import java.util.Set;

public class Constant<SetType> implements SetExpression<SetType> {
	Set<SetType> val;
	
	public Constant(Set<SetType> val) {
		this.val = val;
	}
	
	@Override
	public Set<SetType> compute(Set<SetType> input) {
		return val;
	}

}
