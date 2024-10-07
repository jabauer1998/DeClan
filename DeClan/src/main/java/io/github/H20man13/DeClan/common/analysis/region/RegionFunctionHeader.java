package io.github.H20man13.DeClan.common.analysis.region;

import java.util.Objects;

import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.region.Region;

public class RegionFunctionHeader {
	public enum Direction{
		IN,
		OUT
	}
	
	private Region functionRegion;
	private Direction dir;
	private Object blockOrRegion;
	
	public RegionFunctionHeader(Region region, Direction dir, FlowGraphNode block) {
		this.functionRegion = region;
		this.dir = dir;
		this.blockOrRegion = block;
	}
	
	public RegionFunctionHeader(Region region, Direction dir, Region subRegion) {
		this.functionRegion = region;
		this.dir = dir;
		this.blockOrRegion = subRegion;
	}
	
	@Override
	public boolean equals(Object otherObj) {
		if(otherObj instanceof RegionFunctionHeader){
			RegionFunctionHeader header = (RegionFunctionHeader)otherObj;
			if(functionRegion.equals(header.functionRegion))
				if(dir == header.dir)
					if(blockOrRegion.equals(header.blockOrRegion))
						return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.functionRegion, this.dir, this.blockOrRegion);
	}
}
