package io.github.H20man13.DeClan.common.region;

import java.util.LinkedList;

public class LoopRegion extends Region {
	public LoopRegion(LoopBodyRegion region) {
		super(genSubRegionFromBody(region));
	}
	private static LinkedList<Region> genSubRegionFromBody(LoopBodyRegion body){
		LinkedList<Region> subRegion = new LinkedList<Region>();
		subRegion.add(body);
		return subRegion;
	}
}
