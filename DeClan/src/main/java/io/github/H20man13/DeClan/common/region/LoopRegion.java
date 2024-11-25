package io.github.H20man13.DeClan.common.region;

import java.util.LinkedList;

import io.github.H20man13.DeClan.common.icode.ICode;

public class LoopRegion extends Region {
	public LoopRegion(LoopBodyRegion region) {
		super(region, genSubRegionFromBody(region));
	}
	private static LinkedList<RegionBase> genSubRegionFromBody(LoopBodyRegion body){
		LinkedList<RegionBase> subRegion = new LinkedList<RegionBase>();
		subRegion.add(body);
		return subRegion;
	}
	
	public ICode getLoopCondition() {
		return ((LoopBodyRegion)this.subRegions.get(0)).getLoopCondition();
	}
}
