package io.github.h20man13.DeClan.common.region;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.github.h20man13.DeClan.common.Tuple;
import io.github.h20man13.DeClan.common.exception.RegionAnalysisException;
import io.github.h20man13.DeClan.common.icode.ICode;

public class BaseRegion implements RegionBase, Iterable<RegionBase> {
	protected RegionBase header;
	protected List<RegionBase> subRegions;
	protected List<Tuple<RegionBase, RegionBase>> exitEdge;
	protected List<Tuple<RegionBase, RegionBase>> entryEdge;
	protected List<Tuple<RegionBase, RegionBase>> innerEdge;
	
	public BaseRegion(RegionBase header, List<RegionBase> subRegions) {
		this.header = header;
		this.subRegions = subRegions;
		this.exitEdge = new LinkedList<Tuple<RegionBase, RegionBase>>();
		this.entryEdge = new LinkedList<Tuple<RegionBase, RegionBase>>();
		this.innerEdge = new LinkedList<Tuple<RegionBase, RegionBase>>();
	}
	
	public RegionBase getHeader() {
		return header;
	}
	
	public void addExitEdge(RegionBase subRegion, RegionBase outsideRegion) {
		this.exitEdge.add(new Tuple<RegionBase, RegionBase>(subRegion, outsideRegion));
	}
	
	public void addEntryEdge(RegionBase outsideRegion, RegionBase subRegion) {
		this.entryEdge.add(new Tuple<RegionBase, RegionBase>(outsideRegion, subRegion));
	}
	
	public void addInnerEdge(RegionBase subRegion1, RegionBase subRegion2){
		this.innerEdge.add(new Tuple<RegionBase, RegionBase>(subRegion1, subRegion2));
	}
	
	public Iterable<RegionBase> getInputsOutsideRegion(RegionBase subRegion){
		LinkedList<RegionBase> srces = new LinkedList<RegionBase>();
		for(Tuple<RegionBase, RegionBase> entryEdge: this.entryEdge) {
			if(entryEdge.dest.equals(subRegion))
				srces.add(entryEdge.dest);
		}
		return srces;
	}
	
	public Iterable<RegionBase> getExitRegions(){
		LinkedList<RegionBase> exitRegions = new LinkedList<RegionBase>();
		for(Tuple<RegionBase, RegionBase> exitEdge: this.exitEdge) {
			if(exitEdge.source instanceof InstructionRegion) {
				if(!exitRegions.contains(exitEdge.source))
					exitRegions.add(exitEdge.source);
			} else if(exitEdge.source instanceof Region) {
				Region reg = (Region)exitEdge.source;
				Iterable<RegionBase> allExitRegions = reg.getExitRegions();
				for(RegionBase exitRegion: allExitRegions) {
					if(!exitRegions.contains(exitRegion))
						exitRegions.add(exitRegion);
				}
			}
		}
		return exitRegions;
	}
	
	public Iterable<RegionBase> getEntryRegions(){
		LinkedList<RegionBase> entryRegions = new LinkedList<RegionBase>();
		for(Tuple<RegionBase, RegionBase> entryEdge: this.entryEdge) {
			if(entryEdge.dest instanceof InstructionRegion) {
				if(!entryRegions.contains(entryEdge.dest))
					entryRegions.add(entryEdge.dest);
			} else if(entryEdge.dest instanceof Region) {
				Region reg = (Region)entryEdge.dest;
				Iterable<RegionBase> allExitRegions = reg.getEntryRegions();
				for(RegionBase exitRegion: allExitRegions) {
					if(!entryRegions.contains(exitRegion))
						entryRegions.add(exitRegion);
				}
			}
		}
		return entryRegions;
	}
	
	public Iterable<RegionBase> getTargetsOutsideRegion(RegionBase subRegion){
		LinkedList<RegionBase> targets = new LinkedList<RegionBase>();
		for(Tuple<RegionBase, RegionBase> exitEdge: this.exitEdge) {
			if(exitEdge.source.equals(subRegion))
				targets.add(exitEdge.dest);
		}
		return targets;
	}
	
	public Iterator<RegionBase> iterator(){
		return subRegions.iterator();
	}
	
	public ICode getFirstInstruction() {
		return header.getFirstInstruction();
	}
	
	public ICode getLastInstruction() {
		RegionBase last = null;
		for(RegionBase reg: this.getExitRegions()) {
			if(last == null) {
				last = reg;
			} else if(!last.equals(reg)) {
				throw new RuntimeException("Error there are multiple exit instructions so there is no last instruction");
			} else {
				last = reg;
			}
		}
		
		return last.getLastInstruction();
	}

	@Override
	public RegionBase copy() {
		return new BaseRegion(header, subRegions);
	}
}
