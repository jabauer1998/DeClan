package declan.middleware.analysis.region.function;

import java.util.Set;

import declan.utils.Computable;

public interface SetExpression<SetType> extends Computable<Set<SetType>> {
	public Set<SetType> compute(Set<SetType> input);
}
