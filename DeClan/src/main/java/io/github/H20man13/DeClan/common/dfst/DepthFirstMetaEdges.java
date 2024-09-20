package io.github.H20man13.DeClan.common.dfst;

import java.util.HashSet;

public class DepthFirstMetaEdges {
	private HashSet<DepthFirstMetaEdge> crossEdges;
	private HashSet<DepthFirstMetaEdge> backEdges;
	private HashSet<DepthFirstMetaEdge> retreatingEdges;
	
	public DepthFirstMetaEdges() {
		crossEdges = new HashSet<DepthFirstMetaEdge>();
		backEdges = new HashSet<DepthFirstMetaEdge>();
		retreatingEdges = new HashSet<DepthFirstMetaEdge>();
	}
	
	public void addCrossEdge(DepthFirstMetaEdge edge) {
		this.crossEdges.add(edge);
	}
	
	public void addBackEdge(DepthFirstMetaEdge edge) {
		this.backEdges.add(edge);
	}
	
	public void addRetreatingEdge(DepthFirstMetaEdge edge) {
		this.retreatingEdges.add(edge);
	}
}
