package io.github.H20man13.DeClan.common.dfst;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.util.Utils;

public class DepthFirstSpanningTree {
	private RootDfstNode root; //Root of the tree should be the entry block node of the program
	private DepthFirstMetaEdges metaDataEdges; //For all other types of edges that are not directly part of the tree
	
	public DepthFirstSpanningTree(RootDfstNode root) {
		this.root = root;
		this.metaDataEdges = new DepthFirstMetaEdges();
	}
	
	public void addCrossEdge(RootDfstNode from, RootDfstNode to) {
		metaDataEdges.addCrossEdge(new DepthFirstMetaEdge(from, to));
	}
	
	public void addBackEdge(RootDfstNode from, RootDfstNode to) {
		DepthFirstMetaEdge edge = new DepthFirstMetaEdge(from, to);
		metaDataEdges.addBackEdge(edge);
		metaDataEdges.addRetreatingEdge(edge);
	}
	
	public void addRetreatingEdge(RootDfstNode from, RootDfstNode to) {
		metaDataEdges.addRetreatingEdge(new DepthFirstMetaEdge(from, to));
	}
	
	@Override
	public String toString() {
		int nodeCounter = 0;
		HashMap<RootDfstNode, Integer> nodeToNumber = new HashMap<RootDfstNode, Integer>();
		
		return this.root.toString(nodeToNumber, nodeCounter).source;
	}
}
