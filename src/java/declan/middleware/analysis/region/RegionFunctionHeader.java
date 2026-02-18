package declan.middleware.analysis.region;

import java.util.Objects;

import declan.utils.flow.FlowGraphNode;
import declan.middleware.region.Region;
import declan.middleware.region.RegionBase;

public class RegionFunctionHeader {
	public enum Direction{
		IN,
		OUT
	}
	
	private RegionBase functionRegion;
	private Direction dir;
	private Object blockOrRegion;
	private int iteration;
	
	public RegionFunctionHeader(RegionBase region, Direction dir, FlowGraphNode block) {
		this.functionRegion = region;
		this.dir = dir;
		this.blockOrRegion = block;
		this.iteration = -1;
	}
	
	public RegionFunctionHeader(RegionBase region, Direction dir, RegionBase subRegion) {
		this.functionRegion = region;
		this.dir = dir;
		this.blockOrRegion = subRegion;
		this.iteration = -1;
	}
	
	public RegionFunctionHeader(RegionBase region, Direction dir, RegionBase subRegion, int iteration) {
		this(region, dir, subRegion);
		this.iteration = iteration;
	}
	
	@Override
	public boolean equals(Object otherObj) {
		if(otherObj instanceof RegionFunctionHeader){
			RegionFunctionHeader header = (RegionFunctionHeader)otherObj;
			if(functionRegion.equals(header.functionRegion))
				if(dir == header.dir)
					if(blockOrRegion.equals(header.blockOrRegion))
						return this.iteration == header.iteration;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.functionRegion, this.dir, this.blockOrRegion, this.iteration);
	}
}
