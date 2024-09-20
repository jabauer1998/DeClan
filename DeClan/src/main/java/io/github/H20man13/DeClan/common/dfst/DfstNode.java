package io.github.H20man13.DeClan.common.dfst;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import io.github.H20man13.DeClan.common.flow.BasicBlock;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;

public class DfstNode extends RootDfstNode {
	private RootDfstNode parent;
	
	public DfstNode(BasicBlock block) {
		super(block);
		parent = null;
	}
	
	public void setParent(RootDfstNode node) {
		this.parent = node;
	}
	
	public RootDfstNode getParent() {
		return parent;
	}
	
	@Override
	public boolean isAncestorOf(RootDfstNode childNode) {
		if(childNode.equals(this)) {
			return true;
		} else if(childNode instanceof DfstNode) {
			DfstNode dfstChildNode = (DfstNode)childNode;
			return this.isAncestorOf(dfstChildNode.parent);
		} else {
			return false;
		}
	}
}
