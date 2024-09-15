package io.github.H20man13.DeClan.common.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.util.Utils;
import io.github.H20man13.DeClan.common.util.Utils.WhiteSpaceType;

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
    	StringBuilder builder = new StringBuilder();
    	
    	Map<FlowGraphNode, Integer> nodesToNumbers = new HashMap<FlowGraphNode, Integer>();
    	
    	builder.append("----------Block Definitions----------\n");
    	
    	builder.append("[Block 0\n");
    	builder.append(Utils.formatStringToLeadingWhiteSpace("       " + entryNode.toString()));
    	builder.append("\n]\n");
    	
    	nodesToNumbers.put(entryNode, 0);
    	
    	for(int i = 1; i <= blockNodes.size(); i++) {
    		builder.append("[Block ");
    		builder.append(i);
    		builder.append('\n');
    		FlowGraphNode node = blockNodes.get(i - 1);
    		builder.append(Utils.formatStringToLeadingWhiteSpace("       " + node.toString()));
    		builder.append("\n]\n");
    		nodesToNumbers.put(node, i);
    	}
    	
    	int sizePlusOne = blockNodes.size() + 1;
    	
    	builder.append("[Block ");
		builder.append(sizePlusOne);
		builder.append('\n');
		builder.append(Utils.formatStringToLeadingWhiteSpace("       " + exitNode.toString()));
		builder.append("\n]\n");
		nodesToNumbers.put(exitNode, sizePlusOne);
		
		
		builder.append("\n----------Block Connections----------\n");
		
		builder.append("Block Number |              Connects From            |                   Connects To\n");
		int number = nodesToNumbers.get(entryNode);
		builder.append(Utils.padWhiteSpace("" + number, 13, WhiteSpaceType.TRAILING));
		builder.append('|');
		builder.append(Utils.padWhiteSpace("", 39, WhiteSpaceType.TRAILING));
		builder.append('|');
		number = nodesToNumbers.get(entryNode.entry);
		builder.append(Utils.padWhiteSpace(" [" + number + "]", 30, WhiteSpaceType.TRAILING));
		builder.append('\n');
		
		for(BlockNode node: blockNodes) {
			number = nodesToNumbers.get(node);
			builder.append(Utils.padWhiteSpace("" + number, 13, WhiteSpaceType.TRAILING));
			builder.append('|');
			Set<Integer> predecessors = getNumbersFromNode(node, NodeType.PREDECESSORS, nodesToNumbers);
			builder.append(Utils.padWhiteSpace(" " + predecessors.toString(), 39, WhiteSpaceType.TRAILING));
			builder.append('|');
			Set<Integer> sucessors = getNumbersFromNode(node, NodeType.SUCESSORS, nodesToNumbers);
			builder.append(Utils.padWhiteSpace(" " + sucessors.toString(), 30, WhiteSpaceType.TRAILING));
			builder.append('\n');
		}
		
		number = nodesToNumbers.get(exitNode);
		builder.append(Utils.padWhiteSpace("" + number, 13, WhiteSpaceType.TRAILING));
		builder.append('|');
		number = nodesToNumbers.get(exitNode.exit);
		builder.append(Utils.padWhiteSpace(" [" + number + "]", 39, WhiteSpaceType.TRAILING));
		builder.append('|');
		builder.append(Utils.padWhiteSpace("", 30, WhiteSpaceType.TRAILING));
		builder.append('\n');
		builder.append("---------------------------------------------------");
        return builder.toString();
    }
    
    private enum NodeType{
    	SUCESSORS,
    	PREDECESSORS
    }
    
    private static Set<Integer> getNumbersFromNode(BlockNode node, NodeType type, Map<FlowGraphNode, Integer> intMap){
    	Set<Integer> setOfInts = new HashSet<Integer>();
    	if(type == NodeType.PREDECESSORS) {
    		for(FlowGraphNode predecessor : node.predecessors) {
    			Integer asInt = intMap.get(predecessor);
    			setOfInts.add(asInt);
    		}
    	} else {
    		for(FlowGraphNode sucessor : node.successors) {
    			Integer asInt = intMap.get(sucessor);
    			setOfInts.add(asInt);
    		}
    	}
    	return setOfInts;
    }
    
    public void addBlock(BlockNode block) {
    	this.blockNodes.add(block);
    }

    public List<BlockNode> getBlocks(){
        return blockNodes;
    }

    @Override
    public Iterator<BlockNode> iterator() {
        return blockNodes.iterator();
    }
}
