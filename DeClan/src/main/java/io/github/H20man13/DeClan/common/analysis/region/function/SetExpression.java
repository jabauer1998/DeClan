package io.github.H20man13.DeClan.common.analysis.region.function;

import java.util.Set;

import io.github.H20man13.DeClan.common.Computable;

public interface SetExpression<SetType> extends Computable<Set<SetType>> {
	public Set<SetType> compute(Set<SetType> input);
}
