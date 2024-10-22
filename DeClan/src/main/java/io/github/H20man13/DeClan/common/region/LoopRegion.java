package io.github.H20man13.DeClan.common.region;

import java.util.LinkedList;

public class LoopRegion extends Region {
	public LoopRegion(LoopBodyRegion region) {
		super(region, genSubRegionFromBody(region));
	}
	private static LinkedList<RegionBase> genSubRegionFromBody(LoopBodyRegion body){
		LinkedList<RegionBase> subRegion = new LinkedList<RegionBase>();
		subRegion.add(body);
		return subRegion;
	}
}
