package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

import io.github.H20man13.DeClan.common.analysis.AnalysisBase;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase.Direction;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase.Meet;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.ICode;

public abstract class BasicBlockAnalysis<MapType extends Map<FlowGraphNode, SetType>, SetType extends Set<DataType>, DataType> extends IterativeAnalysis<FlowGraphNode, MapType, SetType, DataType> {
    public BasicBlockAnalysis(FlowGraph flowGraph, Direction direction, Meet meetOperation, Set<DataType> semiLattice, boolean toCopy, Class<MapType> mapClass, Class<SetType> setClass){
        super(flowGraph, direction, meetOperation, semiLattice, toCopy, mapClass, setClass);
    }

    public BasicBlockAnalysis(FlowGraph flowGraph, Direction direction, Function<List<SetType>, SetType> meetOperation, Set<DataType> semilattice, boolean toCopy, Class<MapType> mapClass, Class<SetType> setClass){
        super(flowGraph, direction, meetOperation, semilattice, toCopy, mapClass, setClass);
    }

    public BasicBlockAnalysis(FlowGraph flowGraph, Direction direction, Function<List<SetType>, SetType> meetOperation, boolean toCopy, Class<MapType> mapClass, Class<SetType> setClass){
        super(flowGraph, direction, meetOperation, toCopy, mapClass, setClass);
    }

    public BasicBlockAnalysis(FlowGraph flowGraph, Direction direction, Meet meetOperation, boolean toCopy, Class<MapType> mapClass, Class<SetType> setClass){
        super(flowGraph, direction, meetOperation, toCopy, mapClass, setClass);
    }

    @Override
    protected void runAnalysis(FlowGraph flowGraph, Direction direction, Function<List<SetType>, SetType> meetOperation, Set<DataType> semiLattice, boolean toCopy){
        if(direction == Direction.FORWARDS){
            MapType outputCache = newMap();
            
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

            while(changesHaveOccuredOnOutputs(outputCache)){
                outputCache = deepCopyOutputMap();
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
            }
            
            if(toCopy) {
            	FlowGraphNode copy = flowGraph.getExit().copy();
            	addInputSet(copy, newSet());
                addOutputSet(copy, newSet());
            } else {
            	addInputSet(flowGraph.getExit(), newSet());
                addOutputSet(flowGraph.getExit(), newSet());
            }
        } else {
            MapType inputCache = newMap();
            
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

            while(changesHaveOccuredOnInputs(inputCache)){
                inputCache = deepCopyInputMap();
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
            }
            
            if(toCopy) {
            	addInputSet(flowGraph.getEntry().copy(), newSet());
            } else {
            	addInputSet(flowGraph.getEntry(), newSet());
            }
        }
    }

    public abstract SetType transferFunction(FlowGraphNode instr, SetType inputSet);
}
