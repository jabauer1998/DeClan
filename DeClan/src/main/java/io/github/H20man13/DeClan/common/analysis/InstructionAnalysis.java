package io.github.H20man13.DeClan.common.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

import io.github.H20man13.DeClan.common.analysis.AnalysisBase.Direction;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase.Meet;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.ICode;

public abstract class InstructionAnalysis<SetType> extends Analysis<ICode, SetType> {
    public InstructionAnalysis(FlowGraph flowGraph, Direction direction, Meet meetOperation, Set<SetType> semiLattice){
        super(flowGraph, direction, meetOperation, semiLattice);
    }

    public InstructionAnalysis(FlowGraph flowGraph, Direction direction, Function<List<Set<SetType>>, Set<SetType>> meetOperation, Set<SetType> semilattice){
        super(flowGraph, direction, meetOperation, semilattice);
    }

    public InstructionAnalysis(FlowGraph flowGraph, Direction direction, Function<List<Set<SetType>>, Set<SetType>> meetOperation){
        super(flowGraph, direction, meetOperation);
    }

    public InstructionAnalysis(FlowGraph flowGraph, Direction direction, Meet meetOperation){
        super(flowGraph, direction, meetOperation);
    }

    @Override
    protected void runAnalysis(FlowGraph flowGraph, Direction direction, Function<List<Set<SetType>>, Set<SetType>> meetOperation, Set<SetType> semiLattice){
        if(direction == Direction.FORWARDS){
            Map<ICode, Set<SetType>> outputCache = new HashMap<ICode, Set<SetType>>();

            for(BlockNode block : flowGraph.getBlocks()){
            	if(block.getICode().size() > 0) {
            		ICode lastICode = block.getICode().getLast();
            		Set<SetType> semilatticeCopy = new HashSet<SetType>();
                    semilatticeCopy.addAll(semiLattice);
                    addOutputSet(lastICode, semilatticeCopy);
            	}
            }

            while(changesHaveOccuredOnOutputs(outputCache)){
                outputCache = deepCopyOutputMap();
                for(BlockNode block : flowGraph.getBlocks()){
                    Set<SetType> inputSet = new HashSet<SetType>();

                    List<Set<SetType>> predecessorsLists = new LinkedList<Set<SetType>>();
                    for(FlowGraphNode node : block.getPredecessors()){
                    	if(!node.getICode().isEmpty()) {
                    		predecessorsLists.add(getOutputSet(node.getICode().getLast()));
                    	}
                    }

                    inputSet = meetOperation.apply(predecessorsLists);

                    for(ICode instr : block.getAllICode()){
                        addInputSet(instr, inputSet);
                        inputSet = transferFunction(instr, inputSet);
                        addOutputSet(instr, inputSet);
                    }
                }
            }
        } else {
            Map<ICode, Set<SetType>> inputCache = new HashMap<ICode, Set<SetType>>();

            for(BlockNode block : flowGraph.getBlocks()){
            	if(!block.getICode().isEmpty()) {
            		ICode firstICode = block.getICode().getFirst();
            		Set<SetType> semilatticeCopy = new HashSet<SetType>();
            		semilatticeCopy.addAll(semiLattice);
            		addInputSet(firstICode, semilatticeCopy);
            	}
            }

            while(changesHaveOccuredOnInputs(inputCache)){
                inputCache = deepCopyInputMap();
                List<BlockNode> blocks = flowGraph.getBlocks();
                for(int b = blocks.size() - 1; b >= 0; b--){
                    BlockNode block = blocks.get(b);
                    Set<SetType> outputSet = new HashSet<SetType>();

                    List<Set<SetType>> sucessorLists = new LinkedList<Set<SetType>>();
                    for(FlowGraphNode node : block.getSuccessors()){
                    	if(!node.getICode().isEmpty()) {
                    		ICode first = node.getICode().getFirst();
                    		sucessorLists.add(getInputSet(first));
                    	}
                    }
                    outputSet = meetOperation.apply(sucessorLists);

                    List<ICode> icodeList = block.getICode();
                    for(int i = icodeList.size() - 1; i >= 0; i--){
                        ICode icode = icodeList.get(i);
                        addOutputSet(icode, outputSet);
                        outputSet = transferFunction(icode, outputSet);
                        addInputSet(icode, outputSet);
                    }
                }
            }
        }
    }

    public abstract Set<SetType> transferFunction(ICode instr, Set<SetType> inputSet);
}
