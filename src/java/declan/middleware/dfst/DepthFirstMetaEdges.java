package declan.middleware.dfst;

import java.util.ArrayList;
import java.util.HashSet;

public class DepthFirstMetaEdges {
	private ArrayList<DepthFirstMetaEdge> crossEdges;
	private ArrayList<DepthFirstMetaEdge> backEdges;
	private ArrayList<DepthFirstMetaEdge> retreatingEdges;
	
	public DepthFirstMetaEdges() {
		crossEdges = new ArrayList<DepthFirstMetaEdge>();
		backEdges = new ArrayList<DepthFirstMetaEdge>();
		retreatingEdges = new ArrayList<DepthFirstMetaEdge>();
	}
	
	public void addCrossEdge(DepthFirstMetaEdge edge) {
		if(!this.crossEdges.contains(edge)) {
			this.crossEdges.add(edge);
		}
	}
	
	public void addBackEdge(DepthFirstMetaEdge edge) {
		if(!this.backEdges.contains(edge)) {
			this.backEdges.add(edge);
		}
	}
	
	public void addRetreatingEdge(DepthFirstMetaEdge edge) {
		if(!this.retreatingEdges.contains(edge)) {
			this.retreatingEdges.add(edge);
		}
	}
	
	public int getNumberOfCrossEdges() {
		return crossEdges.size();
	}
	
	public int getNumberOfBackEdges() {
		return backEdges.size();
	}
	
	public int getNumberOfRetreatingEdges() {
		return retreatingEdges.size();
	}
	
	public DepthFirstMetaEdge getRetreatingEdge(int index) {
		return retreatingEdges.get(index);
	}
	
	public DepthFirstMetaEdge getBackEdge(int index) {
		return backEdges.get(index);
	}
	
	public DepthFirstMetaEdge getCrossEdge(int index) {
		return crossEdges.get(index);
	}
}
