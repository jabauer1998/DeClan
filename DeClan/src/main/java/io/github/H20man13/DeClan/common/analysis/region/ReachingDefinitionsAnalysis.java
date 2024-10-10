package io.github.H20man13.DeClan.common.analysis.region;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.analysis.region.function.Closure;
import io.github.H20man13.DeClan.common.analysis.region.function.Constant;
import io.github.H20man13.DeClan.common.analysis.region.function.FunctionApplication;
import io.github.H20man13.DeClan.common.analysis.region.function.InputParamater;
import io.github.H20man13.DeClan.common.analysis.region.function.RegionTransferFunction;
import io.github.H20man13.DeClan.common.analysis.region.function.RegionTransferFunctionFactory;
import io.github.H20man13.DeClan.common.analysis.region.function.SubtractionOperation;
import io.github.H20man13.DeClan.common.analysis.region.function.UnionOperation;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.region.Region;
import io.github.H20man13.DeClan.common.region.RegionGraph;
import io.github.H20man13.DeClan.common.region.RootRegion;

public class ReachingDefinitionsAnalysis extends RegionAnalysis<ICode> {
	private RegionTransferFunctionFactory<ICode> factory;
	private Map<Region, Set<ICode>> genDefs;
	private Map<Region, Set<ICode>> killDefs;
	
	public ReachingDefinitionsAnalysis(RegionGraph regionGraph, Direction direction) {
		super(regionGraph, direction);
		this.factory = new RegionTransferFunctionFactory<ICode>();
		this.genDefs = new HashMap<Region, Set<ICode>>();
		this.killDefs = new HashMap<Region, Set<ICode>>();
		
		for(Region reg: regionGraph) {
			if(reg instanceof RootRegion){
				RootRegion root = (RootRegion)reg;
				
			}
		}
	}

	@Override
	protected Closure<ICode> closureOfFunction(Region reg, RegionTransferFunction<ICode> input) {
		InputParamater<ICode> param = factory.produceParamater();
		Constant<ICode> gen = factory.produceConstant(genDefs.get(reg));
		UnionOperation<ICode> union = factory.produceUnionOperation(param, gen);
		RegionTransferFunction<ICode> func = factory.produceRegionTransferFunction(union);
		return factory.produceClosureFunction(func);
	}

	@Override
	protected RegionTransferFunction<ICode> compositionOfFunctions(RegionTransferFunction<ICode> func1,
			RegionTransferFunction<ICode> func2) {
		FunctionApplication<ICode> application = factory.producefunctionApplication(func1, func2);
		return factory.produceRegionTransferFunction(application);
	}

	@Override
	protected RegionTransferFunction<ICode> meetOfFunctions(RegionTransferFunction<ICode> exp1,
			RegionTransferFunction<ICode> exp2) {
		UnionOperation<ICode> union = factory.produceUnionOperation(exp1, exp2);
		return factory.produceRegionTransferFunction(union);
	}

	@Override
	protected RegionTransferFunction<ICode> transferFunction(Region region) {
		Constant<ICode> kill = factory.produceConstant(killDefs.get(region));
		InputParamater<ICode> param = factory.produceParamater();
		SubtractionOperation<ICode> sub = factory.produceSubtractionOperation(param, kill);
		Constant<ICode> gen = factory.produceConstant(genDefs.get(region));
		UnionOperation<ICode> union = factory.produceUnionOperation(gen, sub);
		return factory.produceRegionTransferFunction(union);
	}

	@Override
	protected RegionTransferFunction<ICode> identityFunction() {
		InputParamater<ICode> param = factory.produceParamater();
		return factory.produceRegionTransferFunction(param);
	}
}
