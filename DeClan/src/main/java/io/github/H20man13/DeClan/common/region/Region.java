package io.github.H20man13.DeClan.common.region;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	@Override
	public String toString() {
		Map<Region, Integer> mapToInt = new HashMap<Region, Integer>();
		int currentRegion = 0;
		return genInnerRegion(this, mapToInt, currentRegion).dest;
	}
	
	private static Tuple<Integer, String> genOuterRegion(Region r, Map<Region, Integer> mapToInt, int currentRegionNumber){
		StringBuilder sb = new StringBuilder();
		mapToInt.put(r, currentRegionNumber);
		currentRegionNumber++;
		sb.append("[Region ");
		sb.append(mapToInt.get(r));
		sb.append("\r\n");
		sb.append("Contains Regions:\r\n");
		for(Region region: r.subRegions) {
			Tuple<Integer, String> gendSubRegion = genInnerRegion(region, mapToInt, currentRegionNumber);
			sb.append(gendSubRegion.dest);
			currentRegionNumber = gendSubRegion.source;
		}
		sb.append("Header Region=");
		sb.append(mapToInt.get(r.header));
		sb.append("\r\n");
		if(r instanceof RootRegion) {
			RootRegion root = (RootRegion)r;
			sb.append("Text Region=\"\r\n");
			sb.append(root.toString());
			sb.append("\r\n\"\r\n");
		}
		return new Tuple<Integer, String>(currentRegionNumber, sb.toString());
	}
	
	private static Tuple<Integer, String> genInnerRegion(Region r, Map<Region, Integer> mapToInt, int currentRegionNumber){
		StringBuilder sb = new StringBuilder();
		mapToInt.put(r, currentRegionNumber);
		currentRegionNumber++;
		sb.append("[Region ");
		int rInt = mapToInt.get(r);
		sb.append(rInt);
		sb.append("\r\n");
		sb.append("Contains Regions:\r\n");
		for(Region region: r.subRegions) {
			Tuple<Integer, String> gendSubRegion = genInnerRegion(region, mapToInt, currentRegionNumber);
			sb.append(gendSubRegion.dest);
			currentRegionNumber = gendSubRegion.source;
		}
		sb.append("Regions Outside ");
		sb.append(rInt);
		sb.append("\r\n");
		for(Tuple<Region, Region> exitEdge: r.exitEdge) {
			Region dest = exitEdge.dest;
			if(!mapToInt.containsKey(dest)) {
				Tuple<Integer, String> tuple = genOuterRegion(dest, mapToInt, currentRegionNumber);
				sb.append(tuple.dest);
				currentRegionNumber = tuple.source;
			}
		}
		for(Tuple<Region, Region> entryEdge: r.entryEdge) {
			Region dest = entryEdge.source;
			if(!mapToInt.containsKey(dest)) {
				Tuple<Integer, String> tuple = genOuterRegion(dest, mapToInt, currentRegionNumber);
				sb.append(tuple.dest);
				currentRegionNumber = tuple.source;
			}
		}
		sb.append("Header Region=");
		sb.append(mapToInt.get(r.header));
		sb.append("\r\n");
		if(r instanceof RootRegion) {
			RootRegion root = (RootRegion)r;
			sb.append("Text Region=\"\r\n");
			sb.append(root.toString());
			sb.append("\r\n\"\r\n");
		}
		sb.append("Exit Edges: {");
		boolean first = true;
		for(Tuple<Region, Region> exitEdge: r.exitEdge) {
			if(first) {
				sb.append('(');
				sb.append(mapToInt.get(exitEdge.source));
				sb.append(", ");
				sb.append(mapToInt.get(exitEdge.dest));
				sb.append(')');
				first = false;
			} else {
				sb.append(", (");
				sb.append(mapToInt.get(exitEdge.source));
				sb.append(", ");
				sb.append(mapToInt.get(exitEdge.dest));
				sb.append(')');
			}
		}
		sb.append("}\r\nOuter Edges: {");
		first = true;
		for(Tuple<Region, Region> entryEdge: r.entryEdge) {
			if(first) {
				sb.append('(');
				sb.append(mapToInt.get(entryEdge.source));
				sb.append(", ");
				sb.append(mapToInt.get(entryEdge.dest));
				sb.append(')');
				first = false;
			} else {
				sb.append(", (");
				sb.append(mapToInt.get(entryEdge.source));
				sb.append(", ");
				sb.append(mapToInt.get(entryEdge.dest));
				sb.append(')');
			}
		}
		sb.append("}\r\nInner Edges: {");
		first = true;
		for(Tuple<Region, Region> entryEdge: r.entryEdge) {
			if(first) {
				sb.append('(');
				sb.append(mapToInt.get(entryEdge.source));
				sb.append(", ");
				sb.append(mapToInt.get(entryEdge.dest));
				sb.append(')');
				first = false;
			} else {
				sb.append(", (");
				sb.append(mapToInt.get(entryEdge.source));
				sb.append(", ");
				sb.append(mapToInt.get(entryEdge.dest));
				sb.append(')');
			}
		}
		return new Tuple<Integer, String>(currentRegionNumber, sb.toString());
	}
}
