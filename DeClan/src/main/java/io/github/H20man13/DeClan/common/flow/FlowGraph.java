package io.github.H20man13.DeClan.common.flow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.H20man13.DeClan.common.util.Utils;

public class FlowGraph implements Iterable<BlockNode>{
    private EntryNode entryNode;
    private ExitNode exitNode;
    private List<BlockNode> blockNodes;

    public FlowGraph(EntryNode entry, List<BlockNode> blockNodes, ExitNode exit){
        this.entryNode = entry;
        this.exitNode = exit;
        this.blockNodes = blockNodes;
    }

    public EntryNode getEntry(){
        return entryNode;
    }

    public ExitNode getExit(){
        return exitNode;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("ENTRY\n");
        for(BlockNode block : blockNodes){
            StringBuilder innerSb = new StringBuilder();
            innerSb.append(' ');
            innerSb.append(block.toString());
            innerSb.append('\n');
            String insideString = Utils.formatStringToLeadingWhiteSpace(innerSb.toString());
            sb.append(insideString);
        }
        sb.append("EXIT\n");

        return sb.toString();
    }

    public List<BlockNode> getBlocks(){
        return blockNodes;
    }

    @Override
    public Iterator<BlockNode> iterator() {
        return blockNodes.iterator();
    }
}
