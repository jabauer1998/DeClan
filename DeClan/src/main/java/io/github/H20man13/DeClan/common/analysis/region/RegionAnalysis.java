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
import io.github.H20man13.DeClan.common.region.RegionBase;
import io.github.H20man13.DeClan.common.region.RegionGraph;
import io.github.H20man13.DeClan.common.region.BaseRegion;
import io.github.H20man13.DeClan.common.region.BlockRegion;
import io.github.H20man13.DeClan.common.analysis.region.function.Closure;
import io.github.H20man13.DeClan.common.analysis.region.function.FunctionApplication;
import io.github.H20man13.DeClan.common.analysis.region.function.RegionTransferFunction;
import io.github.H20man13.DeClan.common.analysis.region.function.RegionTransferFunctionFactory;
import io.github.H20man13.DeClan.common.analysis.region.function.SetExpression;

public abstract class RegionAnalysis<SetType> implements AnalysisBase {
	private RegionGraph regions;
	private Direction direction;
	private Map<RegionFunctionHeader, RegionTransferFunction<SetType>> transferFunctions;
	private Map<RegionBase, Set<SetType>> mappedInputs;
	private Map<RegionBase, Set<SetType>> mappedOutputs;
	private Set<SetType> semilattice;
	
	
	public RegionAnalysis(RegionGraph regionGraph, Direction direction) {
		this.regions = regionGraph;
		this.direction = direction;
		this.transferFunctions = new HashMap<RegionFunctionHeader, RegionTransferFunction<SetType>>();
		this.mappedInputs = new HashMap<RegionBase, Set<SetType>>();
		this.mappedOutputs = new HashMap<RegionBase, Set<SetType>>();
		this.semilattice = new HashSet<SetType>();
	}
	
	public RegionAnalysis(RegionGraph regionGraph, Direction direction, Set<SetType> semilattice) {
		this.regions = regionGraph;
		this.direction = direction;
		this.transferFunctions = new HashMap<RegionFunctionHeader, RegionTransferFunction<SetType>>();
		this.mappedInputs = new HashMap<RegionBase, Set<SetType>>();
		this.mappedOutputs = new HashMap<RegionBase, Set<SetType>>();
		this.semilattice = semilattice;
	}
	
	public void run() {
		buildTransferFunctions();
		runAnalysis();
	}
	
	private void runAnalysis() {
		for(RegionBase region:this.regions) {
			if(region instanceof Region) {
				Region reg = (Region)region;
				RegionFunctionHeader header = new RegionFunctionHeader(reg.getParent(), RegionFunctionHeader.Direction.IN, region);
				RegionTransferFunction<SetType> rtf = transferFunctions.get(header);
				Set<SetType> result = rtf.compute(mappedInputs.get(reg.getParent()));
				mappedInputs.put(region, result);
			}
		}
	}
	
	private void buildTransferFunctions() {
		HashSet<RegionBase> discovered = new HashSet<RegionBase>();
		if(direction == Direction.FORWARDS) {
			for(RegionBase region: regions){
				if(!discovered.contains(region)) {
					if(region instanceof LoopBodyRegion) {
						LoopBodyRegion reg = (LoopBodyRegion)region;
						for(RegionBase subRegion: reg) {
							if(subRegion.equals(reg.getHeader())){
								RegionTransferFunction<SetType> ident = identityFunction();
								RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
								transferFunctions.put(header, ident);
							} else if(subRegion instanceof BaseRegion) {
								BaseRegion base = (BaseRegion)subRegion;
								boolean first = true;
								RegionTransferFunction<SetType> iterator = null;
								RegionBase header = base.getHeader();
								if(header instanceof BaseRegion) {
									BaseRegion baseHeader = (BaseRegion)header;
									for(RegionBase myReg: baseHeader.getInputsOutsideRegion(subRegion)) {
										RegionFunctionHeader myHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, reg);
										if(first) {
											first = false;
											if(!transferFunctions.containsKey(myHeader))
												analyzeRegion(myReg, direction, transferFunctions, discovered);
											iterator = transferFunctions.get(myHeader);
										} else {
											if(!transferFunctions.containsKey(myHeader))
												analyzeRegion(myReg, direction, transferFunctions, discovered);
											iterator = meetOfFunctions(iterator, transferFunctions.get(myHeader));
										}
									}
								}
								RegionFunctionHeader finalHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
								transferFunctions.put(finalHeader, iterator);
							
							
							
								RegionFunctionHeader subRegionInput = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
								if(!transferFunctions.containsKey(subRegionInput))
									analyzeRegion(subRegion, direction, transferFunctions, discovered);
								RegionTransferFunction<SetType> func2 = transferFunctions.get(subRegionInput);
								for(RegionBase exitRegion: base.getExitRegions()){
									RegionFunctionHeader exitHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, exitRegion);
									
									
									RegionFunctionHeader subRegionOutput = new RegionFunctionHeader(subRegion, RegionFunctionHeader.Direction.OUT, exitRegion);
									if(!transferFunctions.containsKey(subRegionOutput))
										analyzeRegion(exitRegion, direction, transferFunctions, discovered);
									RegionTransferFunction<SetType> func1 = transferFunctions.get(subRegionOutput);
									
									RegionTransferFunction<SetType> resultApplication = compositionOfFunctions(func1, func2);
									transferFunctions.put(exitHeader, resultApplication);
								}
							}
						}
					} else if(region instanceof LoopRegion) {
						LoopRegion reg = (LoopRegion)region;
						for(RegionBase subRegion: reg) {
							if(subRegion.equals(reg.getHeader())){
								RegionTransferFunction<SetType> ident = identityFunction();
								RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
								transferFunctions.put(header, ident);
							} else if(subRegion instanceof BaseRegion) {
								boolean first = true;
								RegionTransferFunction<SetType> iterator = null;
								BaseRegion base = (BaseRegion)subRegion;
								RegionBase myHeader = base.getHeader();
								if(myHeader instanceof BaseRegion) {
									BaseRegion myBase = (BaseRegion)myHeader;
									for(RegionBase myReg: myBase.getInputsOutsideRegion(subRegion)) {
										RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, reg);
										if(first) {
											first = false;
											if(!transferFunctions.containsKey(header))
												analyzeRegion(myReg, direction, transferFunctions, discovered);
											iterator = transferFunctions.get(header);
										} else {
											if(!transferFunctions.containsKey(header))
												analyzeRegion(myReg, direction, transferFunctions, discovered);
											iterator = meetOfFunctions(iterator, transferFunctions.get(header));
										}
									}
								}
								
								RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
								Closure<SetType> setType = closureOfFunction(subRegion, iterator);
								transferFunctions.put(header, setType);
							
								RegionFunctionHeader subRegionInput = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
								if(!transferFunctions.containsKey(subRegionInput))
									analyzeRegion(subRegion, direction, transferFunctions, discovered);
								RegionTransferFunction<SetType> func2 = transferFunctions.get(subRegionInput);
								for(RegionBase exitRegion: base.getExitRegions()){
									RegionFunctionHeader exitHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, exitRegion);
									
									
									RegionFunctionHeader subRegionOutput = new RegionFunctionHeader(subRegion, RegionFunctionHeader.Direction.OUT, exitRegion);
									if(!transferFunctions.containsKey(subRegionOutput))
										analyzeRegion(exitRegion, direction, transferFunctions, discovered);
									RegionTransferFunction<SetType> func1 = transferFunctions.get(subRegionOutput);
									
									RegionTransferFunction<SetType> resultApplication = compositionOfFunctions(func1, func2);
									transferFunctions.put(exitHeader, resultApplication);
								}
							}
						}
					} else if(region instanceof BlockRegion) {
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
			for(RegionBase region: regions){
				if(!discovered.contains(region)) {
					if(region instanceof LoopBodyRegion) {
						LoopBodyRegion loop = (LoopBodyRegion)region;
						for(RegionBase subRegion: loop) {
							if(subRegion.equals(loop.getHeader())){
								RegionTransferFunction<SetType> ident = identityFunction();
								RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
								transferFunctions.put(header, ident);
							} else if(subRegion instanceof BaseRegion) {
								boolean first = true;
								RegionTransferFunction<SetType> iterator = null;
								BaseRegion base = (BaseRegion)subRegion;
								RegionBase head = base.getHeader();
								if(head instanceof BaseRegion) {
									BaseRegion baseHead = (BaseRegion)head;
									for(RegionBase reg: baseHead.getTargetsOutsideRegion(subRegion)) {
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
								}
								
								RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
								transferFunctions.put(header, iterator);
							
							
							
								RegionFunctionHeader subRegionInput = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
								if(!transferFunctions.containsKey(subRegionInput))
									analyzeRegion(subRegion, direction, transferFunctions, discovered);
								RegionTransferFunction<SetType> func2 = transferFunctions.get(subRegionInput);
								for(RegionBase entryRegion: base.getEntryRegions()){
									RegionFunctionHeader exitHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, entryRegion);
									
									
									RegionFunctionHeader subRegionOutput = new RegionFunctionHeader(subRegion, RegionFunctionHeader.Direction.IN, entryRegion);
									if(!transferFunctions.containsKey(subRegionOutput))
										analyzeRegion(entryRegion, direction, transferFunctions, discovered);
									RegionTransferFunction<SetType> func1 = transferFunctions.get(subRegionOutput);
									
									RegionTransferFunction<SetType> resultApplication = compositionOfFunctions(func1, func2);
									transferFunctions.put(exitHeader, resultApplication);
								}
							}
						}
					} else if(region instanceof LoopRegion) {
						LoopRegion loop = (LoopRegion)region;
						for(RegionBase subRegion: loop) {
							if(subRegion.equals(loop.getHeader())){
								RegionTransferFunction<SetType> ident = identityFunction();
								RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
								transferFunctions.put(header, ident);
							} else if(subRegion instanceof BaseRegion) {
								boolean first = true;
								RegionTransferFunction<SetType> iterator = null;
								BaseRegion myBaseReg = (BaseRegion)subRegion;
								RegionBase myHeader = myBaseReg.getHeader();
								if(myHeader instanceof BaseRegion) {
									BaseRegion myBaseHeader = (BaseRegion)myHeader;
									for(RegionBase reg: myBaseHeader.getInputsOutsideRegion(subRegion)) {
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
								}
								RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
								Closure<SetType> setType = closureOfFunction(subRegion, iterator);
								transferFunctions.put(header, setType);
							
								RegionFunctionHeader subRegionInput = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
								if(!transferFunctions.containsKey(subRegionInput))
									analyzeRegion(subRegion, direction, transferFunctions, discovered);
								RegionTransferFunction<SetType> func2 = transferFunctions.get(subRegionInput);
								for(RegionBase exitRegion: myBaseReg.getEntryRegions()){
									RegionFunctionHeader exitHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, exitRegion);
									
									
									RegionFunctionHeader subRegionOutput = new RegionFunctionHeader(subRegion, RegionFunctionHeader.Direction.OUT, exitRegion);
									if(!transferFunctions.containsKey(subRegionOutput))
										analyzeRegion(exitRegion, direction, transferFunctions, discovered);
									RegionTransferFunction<SetType> func1 = transferFunctions.get(subRegionOutput);
									
									RegionTransferFunction<SetType> resultApplication = compositionOfFunctions(func1, func2);
									transferFunctions.put(exitHeader, resultApplication);
								}
							}
						}
					} else if(region instanceof BlockRegion) {
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
	
	protected void analyzeRegion(RegionBase region, Direction dir, Map<RegionFunctionHeader, RegionTransferFunction<SetType>> transferFunctions, Set<RegionBase> discovered){
		if(dir == Direction.FORWARDS) {
			if(region instanceof LoopBodyRegion) {
				LoopBodyRegion loopReg = (LoopBodyRegion)region;
				for(RegionBase subRegion: loopReg) {
					if(subRegion.equals(loopReg.getHeader())){
						RegionTransferFunction<SetType> ident = identityFunction();
						RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
						transferFunctions.put(header, ident);
					} else if(subRegion instanceof BaseRegion) {
						boolean first = true;
						RegionTransferFunction<SetType> iterator = null;
						BaseRegion base = (BaseRegion)subRegion;
						RegionBase baseHeader = base.getHeader();
						if(baseHeader instanceof BaseRegion) {
							BaseRegion finalBaseHeader = (BaseRegion)baseHeader;
							for(RegionBase reg: finalBaseHeader.getInputsOutsideRegion(subRegion)) {
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
						}
						
						RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
						transferFunctions.put(header, iterator);
					}
					
					
					
					RegionFunctionHeader subRegionInput = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
					if(!transferFunctions.containsKey(subRegionInput))
						analyzeRegion(subRegion, direction, transferFunctions, discovered);
					RegionTransferFunction<SetType> func2 = transferFunctions.get(subRegionInput);
					if(subRegion instanceof BaseRegion) {
						BaseRegion subExit = (BaseRegion)subRegion;
						for(RegionBase exitRegion: subExit.getExitRegions()){
							RegionFunctionHeader exitHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, exitRegion);
							
							
							RegionFunctionHeader subRegionOutput = new RegionFunctionHeader(subRegion, RegionFunctionHeader.Direction.OUT, exitRegion);
							if(!transferFunctions.containsKey(subRegionOutput))
								analyzeRegion(exitRegion, direction, transferFunctions, discovered);
							RegionTransferFunction<SetType> func1 = transferFunctions.get(subRegionOutput);
							
							RegionTransferFunction<SetType> resultApplication = compositionOfFunctions(func1, func2);
							transferFunctions.put(exitHeader, resultApplication);
						}
					}
				}
			} else if(region instanceof LoopRegion) {
				LoopRegion loopRegion = (LoopRegion)region;
				for(RegionBase subRegion: loopRegion) {
					if(subRegion.equals(loopRegion.getHeader())){
						RegionTransferFunction<SetType> ident = identityFunction();
						RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
						transferFunctions.put(header, ident);
					} else if(subRegion instanceof BaseRegion){
						boolean first = true;
						RegionTransferFunction<SetType> iterator = null;
						BaseRegion base = (BaseRegion)subRegion;
						RegionBase header = base.getHeader();
						if(header instanceof BaseRegion) {
							BaseRegion baseHeader = (BaseRegion)header;
							for(RegionBase reg: baseHeader.getInputsOutsideRegion(subRegion)) {
								RegionFunctionHeader myHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, reg);
								if(first) {
									first = false;
									if(!transferFunctions.containsKey(myHeader))
										analyzeRegion(reg, direction, transferFunctions, discovered);
									iterator = transferFunctions.get(myHeader);
								} else {
									if(!transferFunctions.containsKey(myHeader))
										analyzeRegion(reg, direction, transferFunctions, discovered);
									iterator = meetOfFunctions(iterator, transferFunctions.get(myHeader));
								}
							}
						}
						
						RegionFunctionHeader secondHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
						Closure<SetType> setType = closureOfFunction(subRegion, iterator);
						transferFunctions.put(secondHeader, setType);
					
						RegionFunctionHeader subRegionInput = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, subRegion);
						if(!transferFunctions.containsKey(subRegionInput))
							analyzeRegion(subRegion, direction, transferFunctions, discovered);
						RegionTransferFunction<SetType> func2 = transferFunctions.get(subRegionInput);
						for(RegionBase exitRegion: base.getExitRegions()){
							RegionFunctionHeader exitHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, exitRegion);
							
							
							RegionFunctionHeader subRegionOutput = new RegionFunctionHeader(subRegion, RegionFunctionHeader.Direction.OUT, exitRegion);
							if(!transferFunctions.containsKey(subRegionOutput))
								analyzeRegion(exitRegion, direction, transferFunctions, discovered);
							RegionTransferFunction<SetType> func1 = transferFunctions.get(subRegionOutput);
							
							RegionTransferFunction<SetType> resultApplication = compositionOfFunctions(func1, func2);
							transferFunctions.put(exitHeader, resultApplication);
						}
					}
				}
			} else if(region instanceof BlockRegion) {
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
				LoopBodyRegion body = (LoopBodyRegion)region;
				for(RegionBase subRegion: body) {
					if(subRegion.equals(body.getHeader())){
						RegionTransferFunction<SetType> ident = identityFunction();
						RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
						transferFunctions.put(header, ident);
					} else if(subRegion instanceof BaseRegion) {
						boolean first = true;
						RegionTransferFunction<SetType> iterator = null;
						BaseRegion sub = (BaseRegion)subRegion;
						RegionBase myHead = sub.getHeader();
						if(myHead instanceof BaseRegion) {
							BaseRegion myBase = (BaseRegion)myHead;
							for(RegionBase reg: myBase.getTargetsOutsideRegion(subRegion)) {
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
						}
						RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
						transferFunctions.put(header, iterator);
					
					
					
						RegionFunctionHeader subRegionInput = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
						if(!transferFunctions.containsKey(subRegionInput))
							analyzeRegion(subRegion, direction, transferFunctions, discovered);
						RegionTransferFunction<SetType> func2 = transferFunctions.get(subRegionInput);
						for(RegionBase entryRegion: sub.getEntryRegions()){
							RegionFunctionHeader exitHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, entryRegion);
							
							
							RegionFunctionHeader subRegionOutput = new RegionFunctionHeader(subRegion, RegionFunctionHeader.Direction.IN, entryRegion);
							if(!transferFunctions.containsKey(subRegionOutput))
								analyzeRegion(entryRegion, direction, transferFunctions, discovered);
							RegionTransferFunction<SetType> func1 = transferFunctions.get(subRegionOutput);
							
							RegionTransferFunction<SetType> resultApplication = compositionOfFunctions(func1, func2);
							transferFunctions.put(exitHeader, resultApplication);
						}
					}
				}
			} else if(region instanceof LoopRegion) {
				LoopRegion loop = (LoopRegion)region;
				for(RegionBase subRegion: loop) {
					if(subRegion.equals(loop.getHeader())){
						RegionTransferFunction<SetType> ident = identityFunction();
						RegionFunctionHeader header = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
						transferFunctions.put(header, ident);
					} else if(subRegion instanceof BaseRegion) {
						boolean first = true;
						RegionTransferFunction<SetType> iterator = null;
						BaseRegion subBase = (BaseRegion)subRegion;
						RegionBase header = subBase.getHeader();
						if(header instanceof BaseRegion) {
							BaseRegion base = (BaseRegion)header;
							for(RegionBase reg: base.getInputsOutsideRegion(subRegion)) {
								RegionFunctionHeader myHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, reg);
								if(first) {
									first = false;
									if(!transferFunctions.containsKey(myHeader))
										analyzeRegion(reg, direction, transferFunctions, discovered);
									iterator = transferFunctions.get(myHeader);
								} else {
									if(!transferFunctions.containsKey(myHeader))
										analyzeRegion(reg, direction, transferFunctions, discovered);
									iterator = meetOfFunctions(iterator, transferFunctions.get(myHeader));
								}
							}
						}
						RegionFunctionHeader finalHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
						Closure<SetType> setType = closureOfFunction(subRegion, iterator);
						transferFunctions.put(finalHeader, setType);
					}
					
					RegionFunctionHeader subRegionInput = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.OUT, subRegion);
					if(!transferFunctions.containsKey(subRegionInput))
						analyzeRegion(subRegion, direction, transferFunctions, discovered);
					RegionTransferFunction<SetType> func2 = transferFunctions.get(subRegionInput);
					for(RegionBase exitRegion: loop.getEntryRegions()){
						RegionFunctionHeader exitHeader = new RegionFunctionHeader(region, RegionFunctionHeader.Direction.IN, exitRegion);
						
						
						RegionFunctionHeader subRegionOutput = new RegionFunctionHeader(subRegion, RegionFunctionHeader.Direction.OUT, exitRegion);
						if(!transferFunctions.containsKey(subRegionOutput))
							analyzeRegion(exitRegion, direction, transferFunctions, discovered);
						RegionTransferFunction<SetType> func1 = transferFunctions.get(subRegionOutput);
						
						RegionTransferFunction<SetType> resultApplication = compositionOfFunctions(func1, func2);
						transferFunctions.put(exitHeader, resultApplication);
					}
				}
			} else if(region instanceof BlockRegion) {
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
	
	protected abstract Closure<SetType> closureOfFunction(RegionBase reg, RegionTransferFunction<SetType> input);
	protected abstract RegionTransferFunction<SetType> compositionOfFunctions(RegionTransferFunction<SetType> func1, RegionTransferFunction<SetType> func2);
	protected abstract RegionTransferFunction<SetType> meetOfFunctions(RegionTransferFunction<SetType> exp1, RegionTransferFunction<SetType> exp2);
	protected abstract RegionTransferFunction<SetType> transferFunction(RegionBase region);
	protected abstract RegionTransferFunction<SetType> identityFunction();
}
