package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.ICode;

public abstract class InstructionAnalysis<MapType extends Map<ICode, SetType>, SetType extends Set<DataType>, DataType> extends IterativeAnalysis<ICode, MapType, SetType, DataType> {
    public InstructionAnalysis(FlowGraph flowGraph, Direction direction, Meet meetOperation, Set<DataType> semiLattice, boolean copyKey, Class<MapType> mapClass, Class<SetType> setClass){
        super(flowGraph, direction, meetOperation, semiLattice, copyKey, mapClass, setClass);
    }

    public InstructionAnalysis(FlowGraph flowGraph, Direction direction, Function<List<SetType>, SetType> meetOperation, Set<DataType> semilattice, boolean copyKey, Class<MapType> mapClass, Class<SetType> setClass){
        super(flowGraph, direction, meetOperation, semilattice, copyKey, mapClass, setClass);
    }

    public InstructionAnalysis(FlowGraph flowGraph, Direction direction, Function<List<SetType>, SetType> meetOperation, boolean copyKey, Class<MapType> mapClass, Class<SetType> setClass){
        super(flowGraph, direction, meetOperation, copyKey, mapClass, setClass);
    }

    public InstructionAnalysis(FlowGraph flowGraph, Direction direction, Meet meetOperation, boolean copyKey, Class<MapType> mapClass, Class<SetType> setClass){
        super(flowGraph, direction, meetOperation, copyKey, mapClass, setClass);
    }

    @Override
    protected void runAnalysis(FlowGraph flowGraph, Direction direction, Function<List<SetType>, SetType> meetOperation, Set<DataType> semiLattice, boolean copyKey){
        if(direction == Direction.FORWARDS){
            MapType outputCache = newMap();

            for(BlockNode block : flowGraph.getBlocks()){
            	if(block.getICode().size() > 0) {
            		ICode lastICode = block.getICode().getLast();
            		SetType semilatticeCopy = newSet();
                    semilatticeCopy.addAll(semiLattice);
                    if(copyKey)
                    	addOutputSet(lastICode.copy(), semilatticeCopy);
                    else
                    	addOutputSet(lastICode, semilatticeCopy);
            	}
            }

            int time = 0;
            while(changesHaveOccuredOnOutputs(outputCache)){
                outputCache = deepCopyOutputMap();
                System.out.print("Running: ");
                System.out.println(time);
                for(BlockNode block : flowGraph.getBlocks()){
                    SetType inputSet = newSet();

                    List<SetType> predecessorsLists = new LinkedList<SetType>();
                    for(FlowGraphNode node : block.getPredecessors()){
                    	if(!node.getICode().isEmpty()) {
                    		predecessorsLists.add(getOutputSet(node.getICode().getLast()));
                    	}
                    }

                    inputSet = meetOperation.apply(predecessorsLists);

                    for(ICode instr : block.getICode()){
                    	if(copyKey) {
                    		ICode copy = instr.copy();
                    		addInputSet(copy, inputSet);
                            inputSet = transferFunction(instr, inputSet);
                            addOutputSet(copy, inputSet);
                    	} else {
                    		addInputSet(instr, inputSet);
                            inputSet = transferFunction(instr, inputSet);
                            addOutputSet(instr, inputSet);
                    	}
                    }
                }
                time++;
            }
        } else {
            MapType inputCache = newMap();

            for(BlockNode block : flowGraph.getBlocks()){
            	if(!block.getICode().isEmpty()) {
            		ICode firstICode = block.getICode().getFirst();
            		SetType semilatticeCopy = newSet();
            		semilatticeCopy.addAll(semiLattice);
            		if(copyKey)
                    	addInputSet(firstICode.copy(), semilatticeCopy);
                    else
                    	addInputSet(firstICode, semilatticeCopy);
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
                    	if(!node.getICode().isEmpty()) {
                    		ICode first = node.getICode().getFirst();
                    		sucessorLists.add(getInputSet(first));
                    	}
                    }
                    outputSet = meetOperation.apply(sucessorLists);

                    List<ICode> icodeList = block.getICode();
                    for(int i = icodeList.size() - 1; i >= 0; i--){
                        if(copyKey) {
                        	ICode icode = icodeList.get(i).copy();
                        	addOutputSet(icode, outputSet);
                            outputSet = transferFunction(icode, outputSet);
                            addInputSet(icode, outputSet);
                        } else {
                        	ICode icode = icodeList.get(i);
                        	addOutputSet(icode, outputSet);
                            outputSet = transferFunction(icode, outputSet);
                            addInputSet(icode, outputSet);
                        }
                        
                    }
                }
            }
        }
    }

    public abstract SetType transferFunction(ICode instr, SetType inputSet);
}
