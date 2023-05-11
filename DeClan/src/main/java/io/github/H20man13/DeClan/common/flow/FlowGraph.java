package io.github.H20man13.DeClan.common.flow;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class FlowGraph {
    private EntryNode entryNode;
    private ExitNode exitNode;
    private List<BlockNode> blockNodes;

    public FlowGraph(EntryNode entry, List<BlockNode> blockNodes, ExitNode exit){
        this.entryNode = entry;
        this.exitNode = exit;
        this.blockNodes = blockNodes;
    }

    public void addBlockNode(BlockNode node){
        this.blockNodes.add(node);
    }

    public void removeDeadCode(){
        this.entryNode.removeDeadCode();
    }

    public Set<Set<FlowGraphNode>> identifyLoops(){
        return this.entryNode.identifyLoops(new HashSet<FlowGraphNode>());
    }

    public void generateOptimizedIr(){
        this.entryNode.generateOptimizedIr();
    }

    public EntryNode getEntry(){
        return entryNode;
    }

    public ExitNode getExit(){
        return exitNode;
    }

    public List<BlockNode> getBlocks(){
        return blockNodes;
    }
}
