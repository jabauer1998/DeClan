package io.github.H20man13.DeClan.common.dfst;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.flow.BasicBlock;
import io.github.H20man13.DeClan.common.util.Utils;

public class RootDfstNode implements Iterable<DfstNode>{
	private BasicBlock block;
	private LinkedList<DfstNode> advancingEdges;
	
	public RootDfstNode(BasicBlock block) {
		this.block = block;
		this.advancingEdges = new LinkedList<DfstNode>();
	}
	
	public void addTreeEdge(DfstNode advancingEdge) {
		advancingEdge.setParent(this);
		advancingEdges.add(0, advancingEdge);
	}
	
	@Override
	public boolean equals(Object block) {
		if(block instanceof RootDfstNode) {
			RootDfstNode node = (RootDfstNode)block;
			return node.block.equals(this.block);
		} else {
			return false;
		}
	}
	
	public boolean isAncestorOf(RootDfstNode childNode) {
		if(childNode.equals(this))
			return true;
		else 
			return false;
	}
	
	public RootDfstNode getAncestorOf(RootDfstNode toGet) {
		if(toGet.equals(this))
			return this;
		else
			return null;
	}
	
	public int numChildren() {
		return this.advancingEdges.size();
	}
	
	public Tuple<String, Integer> toString(Map<RootDfstNode, Integer> mapResults, int currentNumber){
		StringBuilder sb = new StringBuilder();
		
		sb.append("Block ");
		sb.append(currentNumber);
		sb.append("\r\n");
		sb.append(Utils.formatStringToLeadingWhiteSpace("  " + this.toString()));
		sb.append("\r\n");
		mapResults.put(this, currentNumber);
		currentNumber++;
		
		for(DfstNode child: this){
			Tuple<String, Integer> toStr = child.toString(mapResults, currentNumber);
			currentNumber = toStr.dest;
			sb.append(toStr.source);
		}
		
		return new Tuple<String, Integer>(sb.toString(), currentNumber);
	}
	
	@Override
	public String toString() {
		return block.toString();
	}

	@Override
	public Iterator<DfstNode> iterator() {
		return advancingEdges.iterator();
	}
}
