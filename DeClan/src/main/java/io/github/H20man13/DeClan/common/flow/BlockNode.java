package io.github.H20man13.DeClan.common.flow;

import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.model.SymbolTable;
import io.github.H20man13.DeClan.common.icode.BasicBlock;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.LetBool;
import io.github.H20man13.DeClan.common.symboltable.Environment;

public class BlockNode implements FlowGraphNode {
    private BasicBlock block;
    private List<FlowGraphNode> successors;
    private List<FlowGraphNode> predecessors;

    public BlockNode(BasicBlock block){
        this.block = block;
        this.successors = new LinkedList<FlowGraphNode>();
        this.predecessors = new LinkedList<FlowGraphNode>();
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
}
