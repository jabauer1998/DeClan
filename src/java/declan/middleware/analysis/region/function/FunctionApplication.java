package declan.middleware.analysis.region.function;

import java.util.Set;

public class FunctionApplication<SetType> implements SetExpression<SetType> {
	private RegionTransferFunction<SetType> func1;
	private RegionTransferFunction<SetType> func2;
	
	public FunctionApplication(RegionTransferFunction<SetType> func1, RegionTransferFunction<SetType> func2) {
		this.func1 = func1;
		this.func2 = func2;
	}

	@Override
	public Set<SetType> compute(Set<SetType> input){
		return func1.compute(func2.compute(input));
	}
}
