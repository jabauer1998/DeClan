package io.github.H20man13.DeClan.common.analysis.region.function;

import java.util.Set;

public class RegionTransferFunction<SetType> implements SetExpression<SetType> {
	private SetExpression<SetType> setExpression;
	
	public RegionTransferFunction(SetExpression<SetType> funcExpression) {
		this.setExpression = funcExpression;
	}

	@Override
	public Set<SetType> compute(Set<SetType> input) {
		return setExpression.compute(input);
	}
}
