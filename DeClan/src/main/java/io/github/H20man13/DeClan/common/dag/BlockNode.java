package io.github.H20man13.DeClan.common.dag;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.icode.BasicBlock;

public class BlockNode implements DagNode {
    private BasicBlock block;
    private List<DagNode> successors;
    private List<DagNode> predecessors;

    public BlockNode(BasicBlock block){
        this.block = block;
        this.successors = new LinkedList<DagNode>();
        this.predecessors = new LinkedList<DagNode>();
    }

    public BasicBlock getBlock(){
        return block;
    }

    public void addSuccessor(DagNode successor){
        this.successors.add(successor);
    }

    public void addPredecessor(DagNode predecessor){
        this.predecessors.add(predecessor);
    }
}
