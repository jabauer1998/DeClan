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

public abstract class BasicBlockAnalysis<SetType> extends Analysis<FlowGraphNode, SetType> {
    public BasicBlockAnalysis(FlowGraph flowGraph, Direction direction, Meet meetOperation, Set<SetType> semiLattice){
        super(flowGraph, direction, meetOperation, semiLattice);
    }

    public BasicBlockAnalysis(FlowGraph flowGraph, Direction direction, Function<List<Set<SetType>>, Set<SetType>> meetOperation, Set<SetType> semilattice){
        super(flowGraph, direction, meetOperation, semilattice);
    }

    public BasicBlockAnalysis(FlowGraph flowGraph, Direction direction, Function<List<Set<SetType>>, Set<SetType>> meetOperation){
        super(flowGraph, direction, meetOperation);
    }

    public BasicBlockAnalysis(FlowGraph flowGraph, Direction direction, Meet meetOperation){
        super(flowGraph, direction, meetOperation);
    }

    public void runAnalysis(FlowGraph flowGraph, Direction direction, Function<List<Set<SetType>>, Set<SetType>> meetOperation, Set<SetType> semiLattice){
        if(direction == Direction.FORWARDS){
            Map<FlowGraphNode, Set<SetType>> outputCache = new HashMap<FlowGraphNode, Set<SetType>>();
            
            addInputSet(flowGraph.getEntry(), new HashSet<SetType>());
            addOutputSet(flowGraph.getEntry(), new HashSet<SetType>());

            for(BlockNode block : flowGraph.getBlocks()){
        		Set<SetType> semilatticeCopy = new HashSet<SetType>();
                semilatticeCopy.addAll(semiLattice);
                addOutputSet(block, semilatticeCopy);
            }

            while(changesHaveOccuredOnOutputs(outputCache)){
                outputCache = deepCopyOutputMap();
                for(BlockNode block : flowGraph.getBlocks()){
                    Set<SetType> inputSet = new HashSet<SetType>();

                    List<Set<SetType>> predecessorsLists = new LinkedList<Set<SetType>>();
                    for(FlowGraphNode node : block.getPredecessors()){
                    	predecessorsLists.add(getOutputSet(node));
                    }

                    inputSet = meetOperation.apply(predecessorsLists);

                    addInputSet(block, inputSet);
                    Set<SetType> outputSet = transferFunction(block, inputSet);
                    addOutputSet(block, outputSet);
                }
            }
            
            addInputSet(flowGraph.getExit(), new HashSet<SetType>());
            addOutputSet(flowGraph.getExit(), new HashSet<SetType>());
        } else {
            Map<FlowGraphNode, Set<SetType>> inputCache = new HashMap<FlowGraphNode, Set<SetType>>();
            
            addOutputSet(flowGraph.getExit(), new HashSet<SetType>());
            addInputSet(flowGraph.getExit(), new HashSet<SetType>());

            for(BlockNode block : flowGraph.getBlocks()){
        		Set<SetType> semilatticeCopy = new HashSet<SetType>();
        		semilatticeCopy.addAll(semiLattice);
        		addInputSet(block, semilatticeCopy);
            }

            while(changesHaveOccuredOnInputs(inputCache)){
                inputCache = deepCopyInputMap();
                List<BlockNode> blocks = flowGraph.getBlocks();
                for(int b = blocks.size() - 1; b >= 0; b--){
                    BlockNode block = blocks.get(b);
                    Set<SetType> outputSet = new HashSet<SetType>();

                    List<Set<SetType>> sucessorLists = new LinkedList<Set<SetType>>();
                    for(FlowGraphNode node : block.getSuccessors()){
                    	sucessorLists.add(this.getInputSet(node));
                    }
                    outputSet = meetOperation.apply(sucessorLists);

                    addOutputSet(block, outputSet);
                    Set<SetType> inputSet = transferFunction(block, outputSet);
                    addInputSet(block, inputSet);
                }
            }
            
            addInputSet(flowGraph.getEntry(), new HashSet<>());
        }
    }

    public abstract Set<SetType> transferFunction(FlowGraphNode instr, Set<SetType> inputSet);
}
