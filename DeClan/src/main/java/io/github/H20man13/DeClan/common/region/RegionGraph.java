package io.github.H20man13.DeClan.common.region;

import java.util.Iterator;
import java.util.List;

public class RegionGraph implements Iterable<Region> {
	private List<Region> regionList;
	
	public RegionGraph(List<Region> regionList) {
		this.regionList = regionList;
	}
	
	public Iterator<Region> iterator(){
		return regionList.iterator();
	}
}
