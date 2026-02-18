package declan.utils.flow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import declan.utils.Copyable;
import declan.frontend.ast.Identifier;
import declan.middleware.dag.DagGraph;
import declan.middleware.dag.DagNode;
import declan.middleware.dag.DagNodeFactory;
import declan.middleware.dag.DagOperationNode;
import declan.middleware.dag.DagValueNode;
import declan.middleware.dag.DagVariableNode;
import declan.middleware.icode.Assign;
import declan.middleware.icode.Call;
import declan.middleware.icode.Goto;
import declan.middleware.icode.ICode;
import declan.middleware.icode.If;
import declan.middleware.icode.exp.BinExp;
import declan.middleware.icode.exp.BoolExp;
import declan.middleware.icode.exp.Exp;
import declan.middleware.icode.exp.IdentExp;
import declan.middleware.icode.exp.IntExp;
import declan.middleware.icode.exp.RealExp;
import declan.middleware.icode.exp.StrExp;
import declan.middleware.icode.exp.UnExp;
import declan.middleware.icode.inline.Inline;
import declan.middleware.icode.label.Label;
import declan.middleware.icode.section.BssSec;
import declan.middleware.icode.section.CodeSec;
import declan.middleware.icode.section.DataSec;
import declan.middleware.icode.section.ProcSec;
import declan.utils.symboltable.Environment;
import declan.utils.symboltable.entry.LiveInfo;
import declan.utils.Utils;

public class BlockNode implements FlowGraphNode, Iterable<ICode> {
    protected BasicBlock block;
    protected List<FlowGraphNode> successors;
    protected List<FlowGraphNode> predecessors;

    public BlockNode(BasicBlock block){
        this.block = block;
        this.successors = new ArrayList<FlowGraphNode>();
        this.predecessors = new ArrayList<FlowGraphNode>();
    }
    
    private BlockNode(BasicBlock block, List<FlowGraphNode> successors, List<FlowGraphNode> predecessors){
    	this.block = block;
    	this.successors = successors;
    	this.predecessors = predecessors;
    }

    public BasicBlock getBlock(){
        return block;
    }

    public void addSuccessor(FlowGraphNode successor){
        this.successors.add(successor);
    }

    public void addPredecessor(FlowGraphNode predecessor){
        this.predecessors.add(predecessor);
    }

    public void removePredecessor(FlowGraphNode node){
        for(int i = 0; i < this.predecessors.size(); i++){
            if(node.hashCode() == predecessors.get(i).hashCode()){
                this.predecessors.remove(i);
                break;
            }
        }
    }

    public void removeSuccessor(FlowGraphNode node){
        for(int i = 0; i < this.successors.size(); i++){
            if(node.hashCode() == successors.get(i).hashCode()){
                this.successors.remove(i);
                break;
            }
        }
    }

    public List<FlowGraphNode> getPredecessors(){
        return this.predecessors;
    }

    public List<FlowGraphNode> getSuccessors(){
        return this.successors;
    }
    
    @Override
    public String toString(){
        return block.toString();
    }

    @Override
    public List<ICode> getICode() {
        return block.getIcode();
    }

    @Override
    public Iterator<ICode> iterator() {
        return this.block.iterator();
    }

	@Override
	public FlowGraphNode copy() {
		return new BlockNode(block, successors, predecessors);
	}

	@Override
	public BlockNode findEndData() {
		if(!this.block.getIcode().isEmpty()) {
			ICode icode = this.block.getIcode().get(0);
			if(icode instanceof CodeSec) {
				for(FlowGraphNode flow: this.predecessors) {
					if(flow.checkEndData())
						return (BlockNode)flow;
				}
			} else if(icode instanceof ProcSec) {
				for(FlowGraphNode flow: this.predecessors) {
					if(flow.checkEndData())
						return (BlockNode)flow;
				}
			}
		}
		
		for(FlowGraphNode node: this.successors) {
			BlockNode b = node.findEndData();
			if(b != null)
				return b;
		}
		
		return null;
	}

	@Override
	public BlockNode findEndBss() {
		if(!this.block.getIcode().isEmpty()) {
			ICode icode = this.block.getIcode().get(0);
			if(icode instanceof CodeSec) {
				for(FlowGraphNode flow: this.predecessors) {
					if(flow.checkEndBss())
						return (BlockNode)flow;
				}
			} else if(icode instanceof ProcSec) {
				for(FlowGraphNode flow: this.predecessors) {
					if(flow.checkEndBss())
						return (BlockNode)flow;
				}
			}
		}
		
		for(FlowGraphNode node: this.successors) {
			BlockNode b = node.findEndBss();
			if(b != null)
				return b;
		}
		
		return null;
	}

	@Override
	public boolean checkEndData() {
		for(ICode icode: this.block)
			if(icode instanceof DataSec)
				return true;
		for(FlowGraphNode node: this.predecessors)
			if(node.checkEndData())
				return true;
		return false;
	}

	@Override
	public boolean checkEndBss() {
		for(ICode icode: this.block)
			if(icode instanceof BssSec)
				return true;
		for(FlowGraphNode node: this.predecessors)
			if(node.checkEndBss())
				return true;
		return false;
	}

	@Override
	public BlockNode findStartBss() {
		if(!this.block.getIcode().isEmpty()) {
			ICode icode = this.block.getIcode().get(0);
			if(icode instanceof BssSec) {
				return this;
			}
		}
		
		for(FlowGraphNode node: this.successors) {
			BlockNode b = node.findStartBss();
			if(b != null)
				return b;
		}
		
		return null;
	}

	@Override
	public BlockNode findStartData() {
		if(!this.block.getIcode().isEmpty()) {
			ICode icode = this.block.getIcode().get(0);
			if(icode instanceof DataSec) {
				return this;
			}
		}
		
		for(FlowGraphNode node: this.successors) {
			BlockNode b = node.findStartData();
			if(b != null)
				return b;
		}
		
		return null;
	}

	@Override
	public boolean checkStartData() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean checkStartBss() {
		// TODO Auto-generated method stub
		return false;
	}
}
