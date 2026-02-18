package io.github.h20man13.DeClan.common.dfst;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import io.github.h20man13.DeClan.common.CopyInt;
import io.github.h20man13.DeClan.common.CopyStr;
import io.github.h20man13.DeClan.common.Tuple;
import io.github.h20man13.DeClan.common.flow.BlockNode;
import io.github.h20man13.DeClan.common.flow.FlowGraphNode;
import io.github.h20man13.DeClan.common.util.ConversionUtils;
import io.github.h20man13.DeClan.common.util.Utils;

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
	
	public Map<Tuple<FlowGraphNode, FlowGraphNode>, BackEdgeLoop> identifyLoops(){
		HashMap<Tuple<FlowGraphNode, FlowGraphNode>, BackEdgeLoop> toRet = new HashMap<Tuple<FlowGraphNode, FlowGraphNode>, BackEdgeLoop>();
		
		int numBackEdges = metaDataEdges.getNumberOfBackEdges();
		for(int i = 0; i < numBackEdges; i++){
			DepthFirstMetaEdge edge = metaDataEdges.getBackEdge(i);
			BlockNode sourceBlock = edge.source.getBlock();
			BlockNode destBlock = edge.dest.getBlock();
			Tuple<FlowGraphNode, FlowGraphNode> newEdge = new Tuple<FlowGraphNode, FlowGraphNode>(edge.source.getBlock(), edge.dest.getBlock());
			
			List<BlockNode> loopList = new LinkedList<BlockNode>();
			RootDfstNode sourceNode = edge.source;
			if(sourceBlock.equals(destBlock)) {
				loopList.add(sourceBlock);
			} else {
				do {
					loopList.add(sourceBlock);
					if(sourceNode instanceof DfstNode) {
						DfstNode nodeWithParent = (DfstNode)sourceNode;
						sourceNode = nodeWithParent.getParent();
						sourceBlock = sourceNode.getBlock();
					} else {
						break;
					}
				} while(!sourceBlock.equals(destBlock));
			}
			
			toRet.put(newEdge, new BackEdgeLoop(loopList.reversed()));
		}
		
		return toRet;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		int nodeCounter = 0;
		HashMap<RootDfstNode, Integer> nodeToNumber = new HashMap<RootDfstNode, Integer>();
		
		sb.append(depthFirstOrderStrings(this.root, nodeToNumber, nodeCounter).source);
		
		sb.append("\r\nDepth First Ordering\r\n");
		LinkedList<Integer> postOrderTraversalResult = new LinkedList<Integer>();
		postOrderTraversal(postOrderTraversalResult, this.root, nodeToNumber);
		
		List<Integer> depthFirstOrderTraversal = postOrderTraversalResult.reversed();
		sb.append(depthFirstOrderTraversal.toString());
		
		sb.append("\r\n");
		
		sb.append("Advancing Edges\n[");
		
		List<DepthFirstMetaEdge> advancingEdges = new LinkedList<DepthFirstMetaEdge>();
		advancingEdgeTraversal(this.root, advancingEdges);
		
		int numAdvancingEdges = advancingEdges.size();
		if(numAdvancingEdges > 0) {
			sb.append(metaEdgeToString(advancingEdges.get(0), nodeToNumber));
			for(int i = 1; i < numAdvancingEdges; i++) {
				sb.append(", ");
				sb.append(metaEdgeToString(advancingEdges.get(i), nodeToNumber));
			}
		}
		sb.append("]\n");
		
		sb.append(metaEdgesToString(this.metaDataEdges, nodeToNumber));
		
		return sb.toString();
	}
	
	private static void advancingEdgeTraversal(RootDfstNode node, List<DepthFirstMetaEdge> edges) {
		for(DfstNode child: node){
			edges.add(new DepthFirstMetaEdge(node, child));
			advancingEdgeTraversal(child, edges);
		}
	}
	
	private static String metaEdgesToString(DepthFirstMetaEdges edges, Map<RootDfstNode, Integer> nodeToNumber) {
		StringBuilder sb = new StringBuilder();
		sb.append("Cross Edges\n[");
		int numCrossEdges = edges.getNumberOfCrossEdges();
		if(numCrossEdges > 0) {
			sb.append(metaEdgeToString(edges.getCrossEdge(0), nodeToNumber));
			for(int i = 1; i < numCrossEdges; i++) {
				sb.append(", ");
				sb.append(metaEdgeToString(edges.getCrossEdge(i), nodeToNumber));
			}
		}
		sb.append("]\n");
		
		sb.append("Retreating Edges\n[");
		int numRetreatingEdges = edges.getNumberOfRetreatingEdges();
		if(numRetreatingEdges > 0) {
			sb.append(metaEdgeToString(edges.getRetreatingEdge(0), nodeToNumber));
			for(int i = 1; i < numCrossEdges; i++) {
				sb.append(", ");
				sb.append(metaEdgeToString(edges.getRetreatingEdge(i), nodeToNumber));
			}
		}
		sb.append("]\n");
		
		sb.append("Back Edges\n[");
		int numBackEdges = edges.getNumberOfBackEdges();
		if(numBackEdges > 0) {
			sb.append(metaEdgeToString(edges.getBackEdge(0), nodeToNumber));
			for(int i = 1; i < numBackEdges; i++) {
				sb.append(", ");
				sb.append(metaEdgeToString(edges.getBackEdge(i), nodeToNumber));
			}
		}
		sb.append("]\n");
		
		return sb.toString();
	}
	
	private static String metaEdgeToString(DepthFirstMetaEdge edge, Map<RootDfstNode, Integer> nodeToNumber) {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		sb.append(nodeToNumber.get(edge.source));
		sb.append(", ");
		sb.append(nodeToNumber.get(edge.dest));
		sb.append(")");
		return sb.toString();
	}
	
	private static void preOrderTraversal(List<Integer> nodeInts, RootDfstNode node, Map<RootDfstNode, Integer> nodeToNumber){
		nodeInts.add(nodeToNumber.get(node));
		for(DfstNode child: node) {
			preOrderTraversal(nodeInts, child, nodeToNumber);
		}
	}
	
	private static void postOrderTraversal(List<Integer> nodeInts, RootDfstNode node, Map<RootDfstNode, Integer> nodeToNumber){
		for(DfstNode child: node) {
			postOrderTraversal(nodeInts, child, nodeToNumber);
		}
		nodeInts.add(nodeToNumber.get(node));
	}
	
	public List<RootDfstNode> getDepthFirstOrderSequence(){
		LinkedList<RootDfstNode> postOrdering = new LinkedList<RootDfstNode>();
		getPostOrderSequence(this.root, postOrdering);
		return postOrdering.reversed();
	}
	
	public List<RootDfstNode> getPreOrderSequence(){
		LinkedList<RootDfstNode> preOrdering = new LinkedList<RootDfstNode>();
		getPreOrderSequence(this.root, preOrdering);
		return preOrdering;
	}
	
	public List<RootDfstNode> getPostOrderSequence(){
		LinkedList<RootDfstNode> postOrdering = new LinkedList<RootDfstNode>();
		getPostOrderSequence(this.root, postOrdering);
		return postOrdering;
	}
	
	private static void getPostOrderSequence(RootDfstNode node, List<RootDfstNode> result) {
		for(DfstNode child: node) {
			getPostOrderSequence(child, result);
		}
		result.add(node);
	}
	
	private static void getPreOrderSequence(RootDfstNode node, List<RootDfstNode> result) {
		result.add(node);
		for(DfstNode child: node) {
			getPreOrderSequence(child, result);
		}
	}
	
	private static Tuple<CopyStr, CopyInt> depthFirstOrderStrings(RootDfstNode node, Map<RootDfstNode, Integer> nodeToNumber, int currentNumber) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Block ");
		sb.append(currentNumber);
		sb.append("\r\n");
		sb.append(Utils.formatStringToLeadingWhiteSpace("  " + node.toString()));
		sb.append("\r\n");
		nodeToNumber.put(node, currentNumber);
		currentNumber++;
		
		for(DfstNode child: node){
			Tuple<CopyStr, CopyInt> toStr = depthFirstOrderStrings(child, nodeToNumber, currentNumber);
			currentNumber = toStr.dest.asInt();
			sb.append(toStr.source);
		}
		
		return new Tuple<CopyStr, CopyInt>(ConversionUtils.newS(sb.toString()), ConversionUtils.newI(currentNumber));
	}
	
	public BlockNode startOfData() {
		return this.root.startOfData();
	}
	
	public BlockNode startOfBss() {
		return this.root.startOfBss();
	}
}
