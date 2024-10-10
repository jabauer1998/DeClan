package io.github.H20man13.DeClan.common.region;

import java.util.List;

import io.github.H20man13.DeClan.common.flow.BlockNode;

public class LoopBodyRegion extends Region {
	public LoopBodyRegion(Region dest, List<Region> subRegions) {
		super(dest, subRegions);
	}
}
