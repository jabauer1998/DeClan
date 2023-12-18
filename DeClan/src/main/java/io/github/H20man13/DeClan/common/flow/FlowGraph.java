package io.github.H20man13.DeClan.common.flow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class FlowGraph {
    private EntryNode entryNode;
    private ExitNode exitNode;
    private List<BlockNode> dataBlocks;
    private List<BlockNode> procedureBlocks;
    private List<BlockNode> codeBlocks;

    public FlowGraph(EntryNode entry, List<BlockNode> dataBlocks, List<BlockNode> codeBlocks, List<BlockNode> procedureBlocks, ExitNode exit){
        this.entryNode = entry;
        this.exitNode = exit;
        this.dataBlocks = dataBlocks;
        this.codeBlocks = codeBlocks;
        this.procedureBlocks = procedureBlocks;
    }

    public EntryNode getEntry(){
        return entryNode;
    }

    public ExitNode getExit(){
        return exitNode;
    }

    public List<BlockNode> getBlocks(){
        LinkedList<BlockNode> toRet = new LinkedList<BlockNode>();
        toRet.addAll(dataBlocks);
        toRet.addAll(codeBlocks);
        toRet.addAll(procedureBlocks);
        return toRet;
    }
}
