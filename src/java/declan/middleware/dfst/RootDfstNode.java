package declan.middleware.dfst;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import declan.utils.Copyable;
import declan.utils.Tuple;
import declan.utils.flow.BasicBlock;
import declan.utils.flow.BlockNode;
import declan.middleware.icode.section.BssSec;
import declan.middleware.icode.section.DataSec;
import declan.utils.Utils;

public class RootDfstNode implements Iterable<DfstNode>, Copyable<RootDfstNode>{
	private BlockNode block;
	private LinkedList<DfstNode> advancingEdges;
	
	public RootDfstNode(BlockNode block) {
		this.block = block;
		this.advancingEdges = new LinkedList<DfstNode>();
	}
	
	public RootDfstNode(BlockNode block, LinkedList<DfstNode> edges) {
		this.block = block;
		this.advancingEdges = edges;
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

	@Override
	public RootDfstNode copy() {
		return new RootDfstNode(block, advancingEdges);
	}

	public BlockNode startOfData() {
		if(this.block.getICode().get(0) instanceof DataSec) {
			return this.block;
		} else {
			for(DfstNode node: this.advancingEdges) {
				BlockNode data = node.startOfData();
				if(data != null)
					return data;
			}
			
			return null;
		}
	}
	
	public BlockNode startOfBss() {
		if(this.block.getICode().get(0) instanceof BssSec) {
			return this.block;
		} else {
			for(DfstNode node: this.advancingEdges) {
				BlockNode data = node.startOfBss();
				if(data != null)
					return data;
			}
			
			return null;
		}
	}
}
