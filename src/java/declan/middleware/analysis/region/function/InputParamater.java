package declan.middleware.analysis.region.function;

import java.util.Set;

public class InputParamater<SetType> implements SetExpression<SetType> {
	public InputParamater() {}

	@Override
	public Set<SetType> compute(Set<SetType> input) {
		return input;
	}
}
