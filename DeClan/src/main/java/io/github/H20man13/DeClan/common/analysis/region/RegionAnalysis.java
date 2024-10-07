package io.github.H20man13.DeClan.common.analysis.region;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase.Direction;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase.Meet;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.region.LoopBodyRegion;
import io.github.H20man13.DeClan.common.region.LoopRegion;
import io.github.H20man13.DeClan.common.region.Region;
import io.github.H20man13.DeClan.common.region.RegionGraph;
import io.github.H20man13.DeClan.common.region.RootRegion;
import io.github.H20man13.DeClan.common.analysis.region.function.Closure;
import io.github.H20man13.DeClan.common.analysis.region.function.FunctionApplication;
import io.github.H20man13.DeClan.common.analysis.region.function.RegionTransferFunction;
import io.github.H20man13.DeClan.common.analysis.region.function.RegionTransferFunctionFactory;
import io.github.H20man13.DeClan.common.analysis.region.function.SetExpression;

public abstract class RegionAnalysis<SetType> implements AnalysisBase {
	private RegionGraph regions;
	private Direction direction;
	
	public RegionAnalysis(RegionGraph regionGraph, Direction direction) {
		this.regions = regionGraph;
		this.direction = direction;
	}
	
	public void run() {
		runAnalysis(regions, direction);
	}
	
	protected void runAnalysis(RegionGraph regionGraph, Direction direction) {
		HashSet<Region> discovered = new HashSet<Region>();
		Map<RegionFunctionHeader, RegionTransferFunction<SetType>> transferFunctions = new HashMap<RegionFunctionHeader, RegionTransferFunction<SetType>>();
		if(direction == Direction.FORWARDS) {
			for(Region region: regions){
				if(!discovered.contains(region)) {
					if(region instanceof LoopBodyRegion) {
						for(Region subRegion: region) {
							if(subRegion.equals(region.getHeader())){
								RegionTransferFunction<SetType> ident = identityFunction();
								RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
								transferFunctions.put(header, ident);
							} else {
								boolean first = true;
								RegionTransferFunction<SetType> iterator = null;
								for(Region reg: subRegion.getHeader().getInputsOutsideRegion(subRegion)) {
									RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, reg);
									if(first) {
										first = false;
										if(!transferFunctions.containsKey(header))
											analyzeRegion(reg, direction, transferFunctions, discovered);
										iterator = transferFunctions.get(header);
									} else {
										if(!transferFunctions.containsKey(header))
											analyzeRegion(reg, direction, transferFunctions, discovered);
										iterator = meetOfFunctions(iterator, transferFunctions.get(header));
									}
								}
								RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
								transferFunctions.put(header, iterator);
							}
							
							
							
							RegionFunctionHeader subRegionInput = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
							if(!transferFunctions.containsKey(subRegionInput))
								analyzeRegion(subRegion, direction, transferFunctions, discovered);
							RegionTransferFunction<SetType> func2 = transferFunctions.get(subRegionInput);
							for(Region exitRegion: subRegion.getExitRegions()){
								RegionFunctionHeader exitHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, exitRegion);
								
								
								RegionFunctionHeader subRegionOutput = new RegionFunctionHeader(subRegion, RegionFunctionHeader.Direction.OUT, exitRegion);
								if(!transferFunctions.containsKey(subRegionOutput))
									analyzeRegion(exitRegion, direction, transferFunctions, discovered);
								RegionTransferFunction<SetType> func1 = transferFunctions.get(subRegionOutput);
								
								RegionTransferFunction<SetType> resultApplication = compositionOfFunctions(func1, func2);
								transferFunctions.put(exitHeader, resultApplication);
							}
						}
					} else if(region instanceof LoopRegion) {
						for(Region subRegion: region) {
							if(subRegion.equals(region.getHeader())){
								RegionTransferFunction<SetType> ident = identityFunction();
								RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
								transferFunctions.put(header, ident);
							} else {
								boolean first = true;
								RegionTransferFunction<SetType> iterator = null;
								for(Region reg: subRegion.getHeader().getInputsOutsideRegion(subRegion)) {
									RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, reg);
									if(first) {
										first = false;
										if(!transferFunctions.containsKey(header))
											analyzeRegion(reg, direction, transferFunctions, discovered);
										iterator = transferFunctions.get(header);
									} else {
										if(!transferFunctions.containsKey(header))
											analyzeRegion(reg, direction, transferFunctions, discovered);
										iterator = meetOfFunctions(iterator, transferFunctions.get(header));
									}
								}
								RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
								Closure<SetType> setType = closureOfFunction(iterator);
								transferFunctions.put(header, setType);
								
							}
							
							RegionFunctionHeader subRegionInput = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
							if(!transferFunctions.containsKey(subRegionInput))
								analyzeRegion(subRegion, direction, transferFunctions, discovered);
							RegionTransferFunction<SetType> func2 = transferFunctions.get(subRegionInput);
							for(Region exitRegion: subRegion.getExitRegions()){
								RegionFunctionHeader exitHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, exitRegion);
								
								
								RegionFunctionHeader subRegionOutput = new RegionFunctionHeader(subRegion, RegionFunctionHeader.Direction.OUT, exitRegion);
								if(!transferFunctions.containsKey(subRegionOutput))
									analyzeRegion(exitRegion, direction, transferFunctions, discovered);
								RegionTransferFunction<SetType> func1 = transferFunctions.get(subRegionOutput);
								
								RegionTransferFunction<SetType> resultApplication = compositionOfFunctions(func1, func2);
								transferFunctions.put(exitHeader, resultApplication);
							}
						}
					} else if(region instanceof RootRegion) {
						RegionTransferFunction<SetType> ident = identityFunction();
						RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, region);
						transferFunctions.put(header, ident);
						RegionTransferFunction<SetType> func = transferFunction(region);
						RegionTransferFunction<SetType> result = compositionOfFunctions(func, ident);
						RegionFunctionHeader resultHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, region);
						transferFunctions.put(resultHeader, result);
					} else {
						throw new RuntimeException("Error unknown type of region encountered");
					}
					discovered.add(region);
				}
			}
		} else {
			for(Region region: regions){
				if(!discovered.contains(region)) {
					if(region instanceof LoopBodyRegion) {
						for(Region subRegion: region) {
							if(subRegion.equals(region.getHeader())){
								RegionTransferFunction<SetType> ident = identityFunction();
								RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
								transferFunctions.put(header, ident);
							} else {
								boolean first = true;
								RegionTransferFunction<SetType> iterator = null;
								for(Region reg: subRegion.getHeader().getTargetsOutsideRegion(subRegion)) {
									RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, reg);
									if(first) {
										first = false;
										if(!transferFunctions.containsKey(header))
											analyzeRegion(reg, direction, transferFunctions, discovered);
										iterator = transferFunctions.get(header);
									} else {
										if(!transferFunctions.containsKey(header))
											analyzeRegion(reg, direction, transferFunctions, discovered);
										iterator = meetOfFunctions(iterator, transferFunctions.get(header));
									}
								}
								RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
								transferFunctions.put(header, iterator);
							}
							
							
							
							RegionFunctionHeader subRegionInput = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
							if(!transferFunctions.containsKey(subRegionInput))
								analyzeRegion(subRegion, direction, transferFunctions, discovered);
							RegionTransferFunction<SetType> func2 = transferFunctions.get(subRegionInput);
							for(Region entryRegion: subRegion.getEntryRegions()){
								RegionFunctionHeader exitHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, entryRegion);
								
								
								RegionFunctionHeader subRegionOutput = new RegionFunctionHeader(subRegion, RegionFunctionHeader.Direction.IN, entryRegion);
								if(!transferFunctions.containsKey(subRegionOutput))
									analyzeRegion(entryRegion, direction, transferFunctions, discovered);
								RegionTransferFunction<SetType> func1 = transferFunctions.get(subRegionOutput);
								
								RegionTransferFunction<SetType> resultApplication = compositionOfFunctions(func1, func2);
								transferFunctions.put(exitHeader, resultApplication);
							}
						}
					} else if(region instanceof LoopRegion) {
						for(Region subRegion: region) {
							if(subRegion.equals(region.getHeader())){
								RegionTransferFunction<SetType> ident = identityFunction();
								RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
								transferFunctions.put(header, ident);
							} else {
								boolean first = true;
								RegionTransferFunction<SetType> iterator = null;
								for(Region reg: subRegion.getHeader().getInputsOutsideRegion(subRegion)) {
									RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, reg);
									if(first) {
										first = false;
										if(!transferFunctions.containsKey(header))
											analyzeRegion(reg, direction, transferFunctions, discovered);
										iterator = transferFunctions.get(header);
									} else {
										if(!transferFunctions.containsKey(header))
											analyzeRegion(reg, direction, transferFunctions, discovered);
										iterator = meetOfFunctions(iterator, transferFunctions.get(header));
									}
								}
								RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
								Closure<SetType> setType = closureOfFunction(iterator);
								transferFunctions.put(header, setType);
								
							}
							
							RegionFunctionHeader subRegionInput = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
							if(!transferFunctions.containsKey(subRegionInput))
								analyzeRegion(subRegion, direction, transferFunctions, discovered);
							RegionTransferFunction<SetType> func2 = transferFunctions.get(subRegionInput);
							for(Region exitRegion: subRegion.getEntryRegions()){
								RegionFunctionHeader exitHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, exitRegion);
								
								
								RegionFunctionHeader subRegionOutput = new RegionFunctionHeader(subRegion, RegionFunctionHeader.Direction.OUT, exitRegion);
								if(!transferFunctions.containsKey(subRegionOutput))
									analyzeRegion(exitRegion, direction, transferFunctions, discovered);
								RegionTransferFunction<SetType> func1 = transferFunctions.get(subRegionOutput);
								
								RegionTransferFunction<SetType> resultApplication = compositionOfFunctions(func1, func2);
								transferFunctions.put(exitHeader, resultApplication);
							}
						}
					} else if(region instanceof RootRegion) {
						RegionTransferFunction<SetType> ident = identityFunction();
						RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, region);
						transferFunctions.put(header, ident);
						RegionTransferFunction<SetType> func = transferFunction(region);
						RegionTransferFunction<SetType> result = compositionOfFunctions(func, ident);
						RegionFunctionHeader resultHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, region);
						transferFunctions.put(resultHeader, result);
					} else {
						throw new RuntimeException("Error unknown type of region encountered");
					}
					discovered.add(region);
				}
			}
		}
	}
	
	protected void analyzeRegion(Region region, Direction dir, Map<RegionFunctionHeader, RegionTransferFunction<SetType>> transferFunctions, Set<Region> discovered){
		if(dir == Direction.FORWARDS) {
			if(region instanceof LoopBodyRegion) {
				for(Region subRegion: region) {
					if(subRegion.equals(region.getHeader())){
						RegionTransferFunction<SetType> ident = identityFunction();
						RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
						transferFunctions.put(header, ident);
					} else {
						boolean first = true;
						RegionTransferFunction<SetType> iterator = null;
						for(Region reg: subRegion.getHeader().getInputsOutsideRegion(subRegion)) {
							RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, reg);
							if(first) {
								first = false;
								if(!transferFunctions.containsKey(header))
									analyzeRegion(reg, direction, transferFunctions, discovered);
								iterator = transferFunctions.get(header);
							} else {
								if(!transferFunctions.containsKey(header))
									analyzeRegion(reg, direction, transferFunctions, discovered);
								iterator = meetOfFunctions(iterator, transferFunctions.get(header));
							}
						}
						RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
						transferFunctions.put(header, iterator);
					}
					
					
					
					RegionFunctionHeader subRegionInput = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
					if(!transferFunctions.containsKey(subRegionInput))
						analyzeRegion(subRegion, direction, transferFunctions, discovered);
					RegionTransferFunction<SetType> func2 = transferFunctions.get(subRegionInput);
					for(Region exitRegion: subRegion.getExitRegions()){
						RegionFunctionHeader exitHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, exitRegion);
						
						
						RegionFunctionHeader subRegionOutput = new RegionFunctionHeader(subRegion, RegionFunctionHeader.Direction.OUT, exitRegion);
						if(!transferFunctions.containsKey(subRegionOutput))
							analyzeRegion(exitRegion, direction, transferFunctions, discovered);
						RegionTransferFunction<SetType> func1 = transferFunctions.get(subRegionOutput);
						
						RegionTransferFunction<SetType> resultApplication = compositionOfFunctions(func1, func2);
						transferFunctions.put(exitHeader, resultApplication);
					}
				}
			} else if(region instanceof LoopRegion) {
				for(Region subRegion: region) {
					if(subRegion.equals(region.getHeader())){
						RegionTransferFunction<SetType> ident = identityFunction();
						RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
						transferFunctions.put(header, ident);
					} else {
						boolean first = true;
						RegionTransferFunction<SetType> iterator = null;
						for(Region reg: subRegion.getHeader().getInputsOutsideRegion(subRegion)) {
							RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, reg);
							if(first) {
								first = false;
								if(!transferFunctions.containsKey(header))
									analyzeRegion(reg, direction, transferFunctions, discovered);
								iterator = transferFunctions.get(header);
							} else {
								if(!transferFunctions.containsKey(header))
									analyzeRegion(reg, direction, transferFunctions, discovered);
								iterator = meetOfFunctions(iterator, transferFunctions.get(header));
							}
						}
						RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
						Closure<SetType> setType = closureOfFunction(iterator);
						transferFunctions.put(header, setType);
						
					}
					
					RegionFunctionHeader subRegionInput = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
					if(!transferFunctions.containsKey(subRegionInput))
						analyzeRegion(subRegion, direction, transferFunctions, discovered);
					RegionTransferFunction<SetType> func2 = transferFunctions.get(subRegionInput);
					for(Region exitRegion: subRegion.getExitRegions()){
						RegionFunctionHeader exitHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, exitRegion);
						
						
						RegionFunctionHeader subRegionOutput = new RegionFunctionHeader(subRegion, RegionFunctionHeader.Direction.OUT, exitRegion);
						if(!transferFunctions.containsKey(subRegionOutput))
							analyzeRegion(exitRegion, direction, transferFunctions, discovered);
						RegionTransferFunction<SetType> func1 = transferFunctions.get(subRegionOutput);
						
						RegionTransferFunction<SetType> resultApplication = compositionOfFunctions(func1, func2);
						transferFunctions.put(exitHeader, resultApplication);
					}
				}
			} else if(region instanceof RootRegion) {
				RegionTransferFunction<SetType> ident = identityFunction();
				RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, region);
				transferFunctions.put(header, ident);
				RegionTransferFunction<SetType> func = transferFunction(region);
				RegionTransferFunction<SetType> result = compositionOfFunctions(func, ident);
				RegionFunctionHeader resultHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, region);
				transferFunctions.put(resultHeader, result);
			} else {
				throw new RuntimeException("Error unknown type of region encountered");
			}
			discovered.add(region);
		} else {
			if(region instanceof LoopBodyRegion) {
				for(Region subRegion: region) {
					if(subRegion.equals(region.getHeader())){
						RegionTransferFunction<SetType> ident = identityFunction();
						RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
						transferFunctions.put(header, ident);
					} else {
						boolean first = true;
						RegionTransferFunction<SetType> iterator = null;
						for(Region reg: subRegion.getHeader().getTargetsOutsideRegion(subRegion)) {
							RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, reg);
							if(first) {
								first = false;
								if(!transferFunctions.containsKey(header))
									analyzeRegion(reg, direction, transferFunctions, discovered);
								iterator = transferFunctions.get(header);
							} else {
								if(!transferFunctions.containsKey(header))
									analyzeRegion(reg, direction, transferFunctions, discovered);
								iterator = meetOfFunctions(iterator, transferFunctions.get(header));
							}
						}
						RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
						transferFunctions.put(header, iterator);
					}
					
					
					
					RegionFunctionHeader subRegionInput = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
					if(!transferFunctions.containsKey(subRegionInput))
						analyzeRegion(subRegion, direction, transferFunctions, discovered);
					RegionTransferFunction<SetType> func2 = transferFunctions.get(subRegionInput);
					for(Region entryRegion: subRegion.getEntryRegions()){
						RegionFunctionHeader exitHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, entryRegion);
						
						
						RegionFunctionHeader subRegionOutput = new RegionFunctionHeader(subRegion, RegionFunctionHeader.Direction.IN, entryRegion);
						if(!transferFunctions.containsKey(subRegionOutput))
							analyzeRegion(entryRegion, direction, transferFunctions, discovered);
						RegionTransferFunction<SetType> func1 = transferFunctions.get(subRegionOutput);
						
						RegionTransferFunction<SetType> resultApplication = compositionOfFunctions(func1, func2);
						transferFunctions.put(exitHeader, resultApplication);
					}
				}
			} else if(region instanceof LoopRegion) {
				for(Region subRegion: region) {
					if(subRegion.equals(region.getHeader())){
						RegionTransferFunction<SetType> ident = identityFunction();
						RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
						transferFunctions.put(header, ident);
					} else {
						boolean first = true;
						RegionTransferFunction<SetType> iterator = null;
						for(Region reg: subRegion.getHeader().getInputsOutsideRegion(subRegion)) {
							RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, reg);
							if(first) {
								first = false;
								if(!transferFunctions.containsKey(header))
									analyzeRegion(reg, direction, transferFunctions, discovered);
								iterator = transferFunctions.get(header);
							} else {
								if(!transferFunctions.containsKey(header))
									analyzeRegion(reg, direction, transferFunctions, discovered);
								iterator = meetOfFunctions(iterator, transferFunctions.get(header));
							}
						}
						RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
						Closure<SetType> setType = closureOfFunction(iterator);
						transferFunctions.put(header, setType);
						
					}
					
					RegionFunctionHeader subRegionInput = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
					if(!transferFunctions.containsKey(subRegionInput))
						analyzeRegion(subRegion, direction, transferFunctions, discovered);
					RegionTransferFunction<SetType> func2 = transferFunctions.get(subRegionInput);
					for(Region exitRegion: subRegion.getEntryRegions()){
						RegionFunctionHeader exitHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, exitRegion);
						
						
						RegionFunctionHeader subRegionOutput = new RegionFunctionHeader(subRegion, RegionFunctionHeader.Direction.OUT, exitRegion);
						if(!transferFunctions.containsKey(subRegionOutput))
							analyzeRegion(exitRegion, direction, transferFunctions, discovered);
						RegionTransferFunction<SetType> func1 = transferFunctions.get(subRegionOutput);
						
						RegionTransferFunction<SetType> resultApplication = compositionOfFunctions(func1, func2);
						transferFunctions.put(exitHeader, resultApplication);
					}
				}
			} else if(region instanceof RootRegion) {
				RegionTransferFunction<SetType> ident = identityFunction();
				RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, region);
				transferFunctions.put(header, ident);
				RegionTransferFunction<SetType> func = transferFunction(region);
				RegionTransferFunction<SetType> result = compositionOfFunctions(func, ident);
				RegionFunctionHeader resultHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, region);
				transferFunctions.put(resultHeader, result);
			} else {
				throw new RuntimeException("Error unknown type of region encountered");
			}
			discovered.add(region);
		}
	}
	
	protected abstract Closure<SetType> closureOfFunction(RegionTransferFunction<SetType> input);
	protected abstract RegionTransferFunction<SetType> compositionOfFunctions(RegionTransferFunction<SetType> func1, RegionTransferFunction<SetType> func2);
	protected abstract RegionTransferFunction<SetType> meetOfFunctions(RegionTransferFunction<SetType> exp1, RegionTransferFunction<SetType> exp2);
	protected abstract RegionTransferFunction<SetType> transferFunction(Region region);
	protected abstract RegionTransferFunction<SetType> identityFunction();
}
