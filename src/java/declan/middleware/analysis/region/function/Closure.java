package declan.middleware.analysis.region.function;

public class Closure<SetType> extends RegionTransferFunction<SetType> {
	public Closure(SetExpression<SetType> funcExpression) {
		super(funcExpression);
	}
}
