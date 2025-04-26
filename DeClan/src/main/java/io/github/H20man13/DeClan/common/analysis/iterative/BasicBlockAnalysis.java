package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

import io.github.H20man13.DeClan.common.Config;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase.Direction;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase.Meet;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.ICode;

public abstract class BasicBlockAnalysis<MapType extends Map<FlowGraphNode, SetType>, SetType extends Set<DataType>, DataType> extends IterativeAnalysis<FlowGraphNode, MapType, SetType, DataType> {
    public BasicBlockAnalysis(FlowGraph flowGraph, Direction direction, Meet meetOperation, Set<DataType> semiLattice, boolean toCopy, Config cfg, Class<MapType> mapClass, Class<SetType> setClass){
        super(flowGraph, direction, meetOperation, semiLattice, toCopy, cfg, mapClass, setClass);
    }

    public BasicBlockAnalysis(FlowGraph flowGraph, Direction direction, Set<DataType> semilattice, boolean toCopy, Config cfg, Class<MapType> mapClass, Class<SetType> setClass){
        super(flowGraph, direction, semilattice, toCopy, cfg, mapClass, setClass);
    }

    public BasicBlockAnalysis(FlowGraph flowGraph, Direction direction, boolean toCopy, Config cfg, Class<MapType> mapClass, Class<SetType> setClass){
        super(flowGraph, direction, toCopy, cfg, mapClass, setClass);
    }

    public BasicBlockAnalysis(FlowGraph flowGraph, Direction direction, Meet meetOperation, boolean toCopy, Config cfg, Class<MapType> mapClass, Class<SetType> setClass){
        super(flowGraph, direction, meetOperation, toCopy, cfg, mapClass, setClass);
    }

    @Override
    protected void runAnalysis(FlowGraph flowGraph, Direction direction, Function<List<SetType>, SetType> meetOperation, Set<DataType> semiLattice, boolean toCopy){
        if(direction == Direction.FORWARDS){
            if(toCopy) {
            	FlowGraphNode entry = flowGraph.getEntry().copy();
            	addInputSet(entry, newSet());
                addOutputSet(entry, newSet());
            } else {
            	addInputSet(flowGraph.getEntry(), newSet());
                addOutputSet(flowGraph.getEntry(), newSet());
            }

            for(BlockNode block : flowGraph.getBlocks()){
            	if(toCopy) {
            		SetType semilatticeCopy = newSet();
                    semilatticeCopy.addAll(semiLattice);
                    addOutputSet(block.copy(), semilatticeCopy);
            	} else {
            		SetType semilatticeCopy = newSet();
                    semilatticeCopy.addAll(semiLattice);
                    addOutputSet(block, semilatticeCopy);
            	}
            }

            MapType outputCache = null;
            do{
                outputCache = copyOutputsFromFlowGraph();
                for(BlockNode block : flowGraph.getBlocks()){
                    SetType inputSet = newSet();

                    List<SetType> predecessorsLists = new LinkedList<SetType>();
                    for(FlowGraphNode node : block.getPredecessors()){
                    	predecessorsLists.add(getOutputSet(node));
                    }

                    inputSet = meetOperation.apply(predecessorsLists);

                    if(toCopy) {
                    	FlowGraphNode myCopy = block.copy();
                    	addInputSet(myCopy, inputSet);
                        SetType outputSet = transferFunction(block, inputSet);
                        addOutputSet(myCopy, outputSet);
                    } else {
                    	addInputSet(block, inputSet);
                        SetType outputSet = transferFunction(block, inputSet);
                        addOutputSet(block, outputSet);
                    }
                }
            } while(changesHaveOccuredOnOutputs(outputCache));
            
            if(toCopy) {
            	FlowGraphNode copy = flowGraph.getExit().copy();
            	addInputSet(copy, newSet());
                addOutputSet(copy, newSet());
            } else {
            	addInputSet(flowGraph.getExit(), newSet());
                addOutputSet(flowGraph.getExit(), newSet());
            }
        } else {
            if(toCopy) {
            	FlowGraphNode node = flowGraph.getExit().copy();
            	addOutputSet(node, newSet());
                addInputSet(node, newSet());
            } else {
            	addOutputSet(flowGraph.getExit(), newSet());
                addInputSet(flowGraph.getExit(), newSet());
            }

            for(BlockNode block : flowGraph.getBlocks()){
            	if(toCopy) {
            		SetType semilatticeCopy = newSet();
            		semilatticeCopy.addAll(semiLattice);
            		addInputSet(block, semilatticeCopy);
            	} else {
            		SetType semilatticeCopy = newSet();
            		semilatticeCopy.addAll(semiLattice);
            		addInputSet(block.copy(), semilatticeCopy);
            	}
            }

            MapType inputCache = null;
            do{
                inputCache = copyInputsFromFlowGraph();
                List<BlockNode> blocks = flowGraph.getBlocks();
                for(int b = blocks.size() - 1; b >= 0; b--){
                    BlockNode block = blocks.get(b);
                    SetType outputSet = newSet();

                    List<SetType> sucessorLists = new LinkedList<SetType>();
                    for(FlowGraphNode node : block.getSuccessors()){
                    	sucessorLists.add(this.getInputSet(node));
                    }
                    outputSet = meetOperation.apply(sucessorLists);

                    if(toCopy) {
                    	FlowGraphNode myCopy = block.copy();
                    	addOutputSet(myCopy, outputSet);
                        SetType inputSet = transferFunction(block, outputSet);
                        addInputSet(myCopy, inputSet);
                    } else {
                    	addOutputSet(block, outputSet);
                        SetType inputSet = transferFunction(block, outputSet);
                        addInputSet(block, inputSet);
                    }
                }
            } while(changesHaveOccuredOnInputs(inputCache));
            
            if(toCopy) {
            	addInputSet(flowGraph.getEntry().copy(), newSet());
            } else {
            	addInputSet(flowGraph.getEntry(), newSet());
            }
        }
    }
    
    @Override
    protected MapType copyOutputsFromFlowGraph(FlowGraph flow){
    	MapType newMap = newMap();
    	for(FlowGraphNode node: flow) {
    		if(node instanceof BlockNode) {
    			BlockNode block = (BlockNode)node;
    			SetType set = newSet();
    			set.addAll(getOutputSet(block));
    			newMap.put(block, set);
    		}
    	}
    	return newMap;
    }
    
    @Override
    protected MapType copyInputsFromFlowGraph(FlowGraph flow) {
    	MapType newMap = newMap();
    	for(FlowGraphNode node: flow) {
    		if(node instanceof BlockNode) {
    			BlockNode block = (BlockNode)node;
				SetType set = newSet();
				set.addAll(getInputSet(block));
				newMap.put(block, set);
    		}
    	}
    	return newMap;
    }

    public abstract SetType transferFunction(FlowGraphNode instr, SetType inputSet);
}
