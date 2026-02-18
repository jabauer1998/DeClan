package declan.middleware.region;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import declan.utils.CopyInt;
import declan.utils.CopyStr;
import declan.utils.Tuple;
import declan.utils.ConversionUtils;

public class Region extends BaseRegion implements Iterable<RegionBase>{
	private RegionBase parent;
	
	public Region(RegionBase header, List<RegionBase> subRegions) {
		super(header, subRegions);
		for(RegionBase subReg: subRegions) {
			if(subReg instanceof Region) {
				Region myReg = (Region)subReg;
				myReg.setParent(this);
			}
		}
	}
	
	public void setParent(Region parent) {
		this.parent = parent;
	}
	
	public RegionBase getParent() {
		return parent;
	}
	
	@Override
	public String toString() {
		Map<RegionBase, Integer> mapToInt = new HashMap<RegionBase, Integer>();
		int currentRegion = 0;
		return genInnerRegion(this, mapToInt, currentRegion).dest.toString();
	}
	
	private static Tuple<CopyInt, CopyStr> genOuterRegion(RegionBase r, Map<RegionBase, Integer> mapToInt, int currentRegionNumber){
		StringBuilder sb = new StringBuilder();
		mapToInt.put(r, currentRegionNumber);
		currentRegionNumber++;
		sb.append("[Region ");
		sb.append(mapToInt.get(r));
		sb.append("\r\n");
		sb.append("Contains Regions:\r\n");
		
		if(r instanceof Region) {
			Region regionWithHeader = (Region)r;
			
			for(RegionBase region: regionWithHeader.subRegions) {
				Tuple<CopyInt, CopyStr> gendSubRegion = genInnerRegion(region, mapToInt, currentRegionNumber);
				sb.append(gendSubRegion.dest);
				currentRegionNumber = gendSubRegion.source.asInt();
			}
		
			sb.append("Header Region=");
			sb.append(mapToInt.get(regionWithHeader.header));
			sb.append("\r\n");
		}
		
		if(r instanceof InstructionRegion) {
			InstructionRegion root = (InstructionRegion)r;
			sb.append("Text Region=\"");
			sb.append(root.toString());
			sb.append("\"\r\n");
		}
		return new Tuple<CopyInt, CopyStr>(ConversionUtils.newI(currentRegionNumber), ConversionUtils.newS(sb.toString()));
	}
	
	private static Tuple<CopyInt, CopyStr> genInnerRegion(RegionBase r, Map<RegionBase, Integer> mapToInt, int currentRegionNumber){
		StringBuilder sb = new StringBuilder();
		mapToInt.put(r, currentRegionNumber);
		currentRegionNumber++;
		sb.append("[Region ");
		int rInt = mapToInt.get(r);
		sb.append(rInt);
		sb.append("\r\n");
		sb.append("Contains Regions:\r\n");
		
		if(r instanceof Region) {
			Region rWithHeader = (Region)r;
			for(RegionBase region: rWithHeader.subRegions) {
				Tuple<CopyInt, CopyStr> gendSubRegion = genInnerRegion(region, mapToInt, currentRegionNumber);
				sb.append(gendSubRegion.dest);
				currentRegionNumber = gendSubRegion.source.asInt();
			}
			
			sb.append("Regions Outside ");
			sb.append(rInt);
			sb.append("\r\n");
			for(Tuple<RegionBase, RegionBase> exitEdge: rWithHeader.exitEdge) {
				RegionBase dest = exitEdge.dest;
				if(!mapToInt.containsKey(dest)) {
					Tuple<CopyInt, CopyStr> tuple = genOuterRegion(dest, mapToInt, currentRegionNumber);
					sb.append(tuple.dest);
					currentRegionNumber = tuple.source.asInt();
				}
			}
			for(Tuple<RegionBase, RegionBase> entryEdge: rWithHeader.entryEdge) {
				RegionBase dest = entryEdge.source;
				if(!mapToInt.containsKey(dest)) {
					Tuple<CopyInt, CopyStr> tuple = genOuterRegion(dest, mapToInt, currentRegionNumber);
					sb.append(tuple.dest);
					currentRegionNumber = tuple.source.asInt();
				}
			}
			sb.append("Header Region=");
			sb.append(mapToInt.get(rWithHeader.header));
			sb.append("\r\n");
		}
		
		
		if(r instanceof InstructionRegion) {
			InstructionRegion root = (InstructionRegion)r;
			sb.append("Text Region=\"");
			sb.append(root.toString());
			sb.append("\"\r\n");
		}
		
		if(r instanceof Region) {
			Region rWithHeader = (Region)r;
			sb.append("Exit Edges: {");
			boolean first = true;
			for(Tuple<RegionBase, RegionBase> exitEdge: rWithHeader.exitEdge) {
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
			for(Tuple<RegionBase, RegionBase> entryEdge: rWithHeader.entryEdge) {
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
			for(Tuple<RegionBase, RegionBase> entryEdge: rWithHeader.entryEdge) {
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
		}
		return new Tuple<CopyInt, CopyStr>(ConversionUtils.newI(currentRegionNumber), ConversionUtils.newS(sb.toString()));
	}
}
