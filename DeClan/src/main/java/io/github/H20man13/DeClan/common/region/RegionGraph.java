package io.github.H20man13.DeClan.common.region;

import java.util.Iterator;
import java.util.List;

public class RegionGraph implements Iterable<RegionBase> {
	private List<RegionBase> regionList;
	
	public RegionGraph(List<RegionBase> regionList) {
		this.regionList = regionList;
	}
	
	public Iterator<RegionBase> iterator(){
		return regionList.iterator();
	}
}
