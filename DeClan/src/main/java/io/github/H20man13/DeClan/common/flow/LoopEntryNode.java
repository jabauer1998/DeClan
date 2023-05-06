package io.github.H20man13.DeClan.common.flow;

import java.util.ArrayList;
import java.util.LinkedList;

import io.github.H20man13.DeClan.common.dag.DagGraph;
import io.github.H20man13.DeClan.common.dag.DagNodeFactory;
import io.github.H20man13.DeClan.common.icode.BasicBlock;
import io.github.H20man13.DeClan.common.symboltable.Environment;

public class LoopEntryNode extends BlockNode{
    public LoopEntryNode(BlockNode node){
        super(node.block);
        this.successors.addAll(node.successors);
        this.predecessors.addAll(node.predecessors);
        this.lifeInformation = node.lifeInformation;
        this.dag = node.dag;
        this.factory = node.factory;

        for(FlowGraphNode sucessor : node.successors){
            if(sucessor instanceof BlockNode){
                BlockNode sucessorBlock = (BlockNode)sucessor;
                sucessorBlock.removePredecessor(node);
                sucessorBlock.addPredecessor(this);
            }
        }

        for(FlowGraphNode predecessor : node.predecessors){
            if(predecessor instanceof BlockNode){
                BlockNode predBlock = (BlockNode)predecessor;
                predBlock.removeSuccessor(node);
                predBlock.addSuccessor(this);
            }
        }
    }
}
