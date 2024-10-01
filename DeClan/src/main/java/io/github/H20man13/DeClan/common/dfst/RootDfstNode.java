package io.github.H20man13.DeClan.common.dfst;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.flow.BasicBlock;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.util.Utils;

public class RootDfstNode implements Iterable<DfstNode>{
	private BlockNode block;
	private LinkedList<DfstNode> advancingEdges;
	
	public RootDfstNode(BlockNode block) {
		this.block = block;
		this.advancingEdges = new LinkedList<DfstNode>();
	}
	
	public void addTreeEdge(DfstNode advancingEdge) {
		advancingEdge.setParent(this);
		this.advancingEdges.add(advancingEdge);
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
	
	public BlockNode getBlock() {
		return block;
	}
	
	public boolean isAncestorOf(RootDfstNode childNode) {
		if(childNode.equals(this))
			return true;
		else 
			return false;
	}
	
	public int numChildren() {
		return this.advancingEdges.size();
	}
	
	@Override
	public String toString() {
		return block.toString();
	}

	@Override
	public Iterator<DfstNode> iterator() {
		return advancingEdges.iterator();
	}
	
	@Override
	public int hashCode() {
		return block.hashCode();
	}
}
