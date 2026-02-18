package io.github.h20man13.DeClan.common.analysis.region.function;

import java.util.Set;

import io.github.h20man13.DeClan.common.Computable;

public class RegionTransferFunction<SetType> implements Computable<Set<SetType>>{
	private SetExpression<SetType> setExpression;
	
	public RegionTransferFunction(SetExpression<SetType> funcExpression) {
		this.setExpression = funcExpression;
	}

	@Override
	public Set<SetType> compute(Set<SetType> input) {
		return setExpression.compute(input);
	}
	
	public SetExpression<SetType> getExpression(){
		return setExpression;
	}
}
