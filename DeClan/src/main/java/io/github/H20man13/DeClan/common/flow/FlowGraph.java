package io.github.H20man13.DeClan.common.flow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.H20man13.DeClan.common.util.Utils;

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

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("ENTRY\n");
        sb.append("DATA BLOCKS\n");
        for(BlockNode block : dataBlocks){
            StringBuilder innerSb = new StringBuilder();
            innerSb.append(' ');
            innerSb.append(block.toString());
            innerSb.append('\n');
            String insideString = Utils.formatStringToLeadingWhiteSpace(innerSb.toString());
            sb.append(insideString);
        }
        sb.append("CODE BLOCKS\n");
        for(BlockNode block: codeBlocks){
            StringBuilder innerSb = new StringBuilder();
            innerSb.append(' ');
            innerSb.append(block.toString());
            innerSb.append('\n');
            String insideString = Utils.formatStringToLeadingWhiteSpace(innerSb.toString());
            sb.append(insideString);
        }
        sb.append("EXIT\n");
        sb.append("PROCEDURE BLOCKS\n");
        for(BlockNode block: procedureBlocks){
            StringBuilder innerSb = new StringBuilder();
            innerSb.append(' ');
            innerSb.append(block.toString());
            innerSb.append('\n');
            String insideString = Utils.formatStringToLeadingWhiteSpace(innerSb.toString());
            sb.append(insideString);
        }

        return sb.toString();
    }

    public List<BlockNode> getBlocks(){
        LinkedList<BlockNode> toRet = new LinkedList<BlockNode>();
        toRet.addAll(dataBlocks);
        toRet.addAll(codeBlocks);
        toRet.addAll(procedureBlocks);
        return toRet;
    }

    public List<BlockNode> getDataBlocks(){
        return dataBlocks;
    }

    public List<BlockNode> getCodeBlocks(){
        return codeBlocks;
    }

    public List<BlockNode> getProcedureBlocks(){
        return procedureBlocks;
    }
}
