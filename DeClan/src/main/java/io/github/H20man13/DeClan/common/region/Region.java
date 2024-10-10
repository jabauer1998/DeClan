package io.github.H20man13.DeClan.common.region;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.Tuple;

public class Region implements Iterable<Region>{
	private Region header;
	private List<Region> subRegions;
	private Region parent;
	private List<Tuple<Region, Region>> exitEdge;
	private List<Tuple<Region, Region>> entryEdge;
	private List<Tuple<Region, Region>> innerEdge;
	
	public Region(Region header, List<Region> subRegions) {
		this.subRegions = subRegions;
		this.exitEdge = new LinkedList<Tuple<Region, Region>>();
		this.entryEdge = new LinkedList<Tuple<Region, Region>>();
		this.innerEdge = new LinkedList<Tuple<Region, Region>>();
		for(Region subReg: subRegions) {
			subReg.setParent(this);
		}
	}
	
	public void setParent(Region parent) {
		this.parent = parent;
	}
	
	public Region getParent() {
		return parent;
	}
	
	public Region getHeader() {
		return header;
	}
	
	public void addExitEdge(Region subRegion, Region outsideRegion) {
		this.exitEdge.add(new Tuple<Region, Region>(subRegion, outsideRegion));
	}
	
	public void addEntryEdge(Region outsideRegion, Region subRegion) {
		this.entryEdge.add(new Tuple<Region, Region>(outsideRegion, subRegion));
	}
	
	public void addInnerEdge(Region subRegion1, Region subRegion2){
		this.innerEdge.add(new Tuple<Region, Region>(subRegion1, subRegion2));
	}
	
	public Iterable<Region> getInputsOutsideRegion(Region subRegion){
		LinkedList<Region> srces = new LinkedList<Region>();
		for(Tuple<Region, Region> entryEdge: this.entryEdge) {
			if(entryEdge.dest.equals(subRegion))
				srces.add(entryEdge.dest);
		}
		return srces;
	}
	
	public Iterable<Region> getExitRegions(){
		LinkedList<Region> exitRegions = new LinkedList<Region>();
		for(Tuple<Region, Region> exitEdge: this.exitEdge) {
			if(exitEdge.source instanceof RootRegion) {
				if(!exitRegions.contains(exitEdge.source))
					exitRegions.add(exitEdge.source);
			} else {
				Iterable<Region> allExitRegions = exitEdge.source.getExitRegions();
				for(Region exitRegion: allExitRegions) {
					if(!exitRegions.contains(exitRegion))
						exitRegions.add(exitRegion);
				}
			}
		}
		return exitRegions;
	}
	
	public Iterable<Region> getEntryRegions(){
		LinkedList<Region> entryRegions = new LinkedList<Region>();
		for(Tuple<Region, Region> entryEdge: this.entryEdge) {
			if(entryEdge.dest instanceof RootRegion) {
				if(!entryRegions.contains(entryEdge.dest))
					entryRegions.add(entryEdge.dest);
			} else {
				Iterable<Region> allExitRegions = entryEdge.dest.getEntryRegions();
				for(Region exitRegion: allExitRegions) {
					if(!entryRegions.contains(exitRegion))
						entryRegions.add(exitRegion);
				}
			}
		}
		return entryRegions;
	}
	
	public Iterable<Region> getTargetsOutsideRegion(Region subRegion){
		LinkedList<Region> targets = new LinkedList<Region>();
		for(Tuple<Region, Region> exitEdge: this.exitEdge) {
			if(exitEdge.source.equals(subRegion))
				targets.add(exitEdge.dest);
		}
		return targets;
	}
	
	public Iterator<Region> iterator(){
		return subRegions.iterator();
	}
}
