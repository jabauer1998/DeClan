package io.github.h20man13.DeClan.common.dfst;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import io.github.h20man13.DeClan.common.flow.BasicBlock;
import io.github.h20man13.DeClan.common.flow.BlockNode;
import io.github.h20man13.DeClan.common.flow.FlowGraphNode;

public class DfstNode extends RootDfstNode {
	private RootDfstNode parent;
	
	public DfstNode(BlockNode block) {
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
		if(this.equals(childNode)) {
			return true;
		} else if(childNode instanceof DfstNode) {
			DfstNode dfstChildNode = (DfstNode)childNode;
			return this.isAncestorOf(dfstChildNode.parent);
		} else {
			return false;
		}
	}
}
