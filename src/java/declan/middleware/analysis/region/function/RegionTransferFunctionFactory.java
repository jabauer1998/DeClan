package io.github.h20man13.DeClan.common.analysis.region.function;

import java.util.Set;

public class RegionTransferFunctionFactory<SetType> {
	public ComplimentOperation<SetType> produceCompliment(SetExpression<SetType> set, SetExpression<SetType> global) {
		return new ComplimentOperation<SetType>(set, global);
	}
	
	public Constant<SetType> produceConstant(Set<SetType> constant) {
		return new Constant<SetType>(constant);
	}
	
	public FunctionApplication<SetType> producefunctionApplication(RegionTransferFunction<SetType> func1, RegionTransferFunction<SetType> func2) {
		return new FunctionApplication<SetType>(func1, func2);
	}
	
	public InputParamater<SetType> produceParamater(){
		return new InputParamater<SetType>();
	}
	
	public IntersectionOperation<SetType> produceIntersectionOperation(SetExpression<SetType> set1, SetExpression<SetType> set2){
		return new IntersectionOperation<SetType>(set1, set2);
	}
	
	public UnionOperation<SetType> produceUnionOperation(SetExpression<SetType> set1, SetExpression<SetType> set2){
		return new UnionOperation<SetType>(set1, set2);
	}
	
	public RegionTransferFunction<SetType> produceRegionTransferFunction(SetExpression<SetType> type){
		return new RegionTransferFunction<SetType>(type);
	}
	
	public Closure<SetType> produceClosureFunction(SetExpression<SetType> func){
		return new Closure<SetType>(func);
	}
	
	public SubtractionOperation<SetType> produceSubtractionOperation(SetExpression<SetType> set1, SetExpression<SetType> set2){
		return new SubtractionOperation<SetType>(set1, set2);
	}
}
