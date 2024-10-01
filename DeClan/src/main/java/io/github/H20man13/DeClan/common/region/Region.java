package io.github.H20man13.DeClan.common.region;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Region {
	private List<Region> subRegions;
	private List<Region> predecessors;
	private List<Region> sucessors;
	
	public Region(List<Region> subRegions) {
		this.subRegions = subRegions;
		this.sucessors = new LinkedList<Region>();
		this.predecessors = new LinkedList<Region>();
	}
	
	public void addPredecessor(Region region) {
		this.predecessors.add(region);
	}
	
	public void addSucessor(Region region) {
		this.sucessors.add(region);
	}
	
	public Iterator<Region> getPredecessors(){
		return predecessors.iterator();
	}
	
	public Iterator<Region> getSucessors(){
		return sucessors.iterator();
	}
	
	public Iterator<Region> subRegions(){
		return subRegions.iterator();
	}
}
