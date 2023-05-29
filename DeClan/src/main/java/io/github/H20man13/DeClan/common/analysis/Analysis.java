package io.github.H20man13.DeClan.common.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.ICode;

public abstract class Analysis<SetType> {
    private FlowGraph flowGraph;
    private Direction direction;
    private Meet symbol;
    private Set<SetType> semiLattice;

    private Map<FlowGraphNode, Set<SetType>> blockOutputs;
    private Map<FlowGraphNode, Set<SetType>> blockInputs;
    private Map<ICode, Set<SetType>> instructionOutputs;
    private Map<ICode, Set<SetType>> instructionInputs;
    

    public enum Direction{
        BACKWARDS,
        FORWARDS  
    }

    public enum Meet{
        UNION,
        INTERSECTION
    }
    
    public Analysis(FlowGraph flowGraph, Direction direction, Meet symbol, Set<SetType> semiLattice){
        this.flowGraph = flowGraph;
        this.direction = direction;
        this.symbol = symbol;
        this.instructionInputs = new HashMap<ICode, Set<SetType>>();
        this.instructionOutputs = new HashMap<ICode, Set<SetType>>();
        this.blockInputs = new HashMap<FlowGraphNode, Set<SetType>>();
        this.blockOutputs = new HashMap<FlowGraphNode, Set<SetType>>();
        this.semiLattice = semiLattice;
    }

    public Analysis(FlowGraph flowGraph, Direction direction, Meet symbol){
        this(flowGraph, direction, symbol, new HashSet<SetType>());
    }

    public Set<SetType> getBlockInputSet(FlowGraphNode Node){
        return blockInputs.get(Node);
    }

    public Set<SetType> getBlockOutputSet(FlowGraphNode Node){
        return blockOutputs.get(Node);
    }

    public Set<SetType> getInstructionInputSet(ICode instruction){
        return instructionInputs.get(instruction);
    }

    public Set<SetType> getInstructionOutputSet(ICode instruction){
        return instructionOutputs.get(instruction);
    }

    public void run(){
        if(this.direction == Direction.FORWARDS){
            Map<FlowGraphNode, Set<SetType>> outputCache = new HashMap<>();
            blockOutputs.put(this.flowGraph.getEntry(), new HashSet<SetType>());

            for(BlockNode block : this.flowGraph.getBlocks()){
                blockOutputs.put(block, semiLattice);
            }

            while(changesHaveOccured(this.blockOutputs, outputCache)){
                outputCache.putAll(this.blockOutputs);
                for(BlockNode block : this.flowGraph.getBlocks()){
                    if(!blockInputs.containsKey(block)){
                        blockInputs.put(block, new HashSet<SetType>());
                    }

                    Set<SetType> inputSet = blockInputs.get(block);

                    if(this.symbol == Meet.UNION){
                        for(FlowGraphNode predecessor : block.getPredecessors()){
                            inputSet.addAll(blockOutputs.get(predecessor));
                        }
                    } else {
                        for(FlowGraphNode predecessor : block.getPredecessors()){
                            inputSet.retainAll(blockOutputs.get(predecessor));
                        }
                    }
                

                    for(ICode instr : block.getICode()){
                        instructionInputs.put(instr, inputSet);
                        inputSet = transferFunction(instr, inputSet);
                        instructionOutputs.put(instr, inputSet);
                    }
                    
                    blockOutputs.put(block, inputSet);
                }
            }
        } else {
            Map<FlowGraphNode, Set<SetType>> inputCache = new HashMap<FlowGraphNode, Set<SetType>>();

            blockInputs.put(this.flowGraph.getExit(), new HashSet<SetType>());

            for(BlockNode block : this.flowGraph.getBlocks()){
                blockInputs.put(block, new HashSet<SetType>());
            }

            while(changesHaveOccured(this.blockInputs, inputCache)){
                inputCache.putAll(this.blockInputs);
                for(BlockNode block : this.flowGraph.getBlocks()){
                    if(!blockOutputs.containsKey(block)){
                        blockOutputs.put(block, new HashSet<SetType>());
                    }

                    Set<SetType> outputSet = blockOutputs.get(block);
                    if(this.symbol == Meet.UNION){
                        for(FlowGraphNode successor : block.getSuccessors()){
                            outputSet.addAll(blockInputs.get(successor));
                        }
                    } else {
                        for(FlowGraphNode successor : block.getSuccessors()){
                            outputSet.retainAll(blockInputs.get(successor));
                        }
                    }

                    for(ICode icode : block.getICode()){
                        instructionOutputs.put(icode, outputSet);
                        outputSet = transferFunction(icode, outputSet);
                        instructionInputs.put(icode, outputSet);
                    }

                    blockInputs.put(block, outputSet);
                }
            }
        }
    }

    public boolean changesHaveOccured(Map<FlowGraphNode, Set<SetType>> actual, Map<FlowGraphNode, Set<SetType>> cached){
        Set<FlowGraphNode> keys = actual.keySet();
        for(FlowGraphNode key : keys){
            if(!cached.containsKey(key)){
                return true;
            }

            Set<SetType> actualData = actual.get(key);
            Set<SetType> cachedData = cached.get(key);

            if(actualData.size() != cachedData.size()){
                return true;
            }

            if(!actualData.equals(cachedData)){
                return true;
            }
        }

        return false;
    }

    public abstract Set<SetType> transferFunction(ICode instr, Set<SetType> inputSet);
}
