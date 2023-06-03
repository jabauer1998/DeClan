package io.github.H20man13.DeClan.common.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            
            blockInputs.put(this.flowGraph.getEntry(), new HashSet<SetType>());
            blockOutputs.put(this.flowGraph.getEntry(), new HashSet<SetType>());

            for(BlockNode block : this.flowGraph.getBlocks()){
                Set<SetType> semilatticeCopy = new HashSet<SetType>();
                semilatticeCopy.addAll(semiLattice);
                blockOutputs.put(block, semilatticeCopy);
            }

            while(changesHaveOccured(this.blockOutputs, outputCache)){
                outputCache = deepCopyMap(this.blockOutputs);
                for(BlockNode block : this.flowGraph.getBlocks()){
                    Set<SetType> inputSet = new HashSet<SetType>();

                    if(this.symbol == Meet.UNION){
                        for(FlowGraphNode predecessor : block.getPredecessors()){
                            inputSet.addAll(blockOutputs.get(predecessor));
                        }
                    } else {
                        for(FlowGraphNode predecessor : block.getPredecessors()){
                            inputSet.retainAll(blockOutputs.get(predecessor));
                        }
                    }

                    blockInputs.put(block, inputSet);

                    for(ICode instr : block.getICode()){
                        instructionInputs.put(instr, inputSet);
                        inputSet = transferFunction(block, instr, inputSet);
                        instructionOutputs.put(instr, inputSet);
                    }
                    
                    blockOutputs.put(block, inputSet);
                }
            }

            blockInputs.put(this.flowGraph.getExit(), new HashSet<SetType>());
            blockOutputs.put(this.flowGraph.getExit(), new HashSet<SetType>());
        } else {
            Map<FlowGraphNode, Set<SetType>> inputCache = new HashMap<FlowGraphNode, Set<SetType>>();

            blockInputs.put(this.flowGraph.getExit(), new HashSet<SetType>());
            blockOutputs.put(this.flowGraph.getExit(), new HashSet<SetType>());

            for(BlockNode block : this.flowGraph.getBlocks()){
                Set<SetType> semilatticeCopy = new HashSet<SetType>();
                semilatticeCopy.addAll(semiLattice);
                blockInputs.put(block, semilatticeCopy);
            }

            while(changesHaveOccured(this.blockInputs, inputCache)){
                inputCache = deepCopyMap(this.blockInputs);
                List<BlockNode> blocks = flowGraph.getBlocks();
                for(int b = blocks.size() - 1; b >= 0; b--){
                    BlockNode block = blocks.get(b);
                    Set<SetType> outputSet = new HashSet<SetType>();
                    if(this.symbol == Meet.UNION){
                        for(FlowGraphNode successor : block.getSuccessors()){
                            outputSet.addAll(blockInputs.get(successor));
                        }
                    } else {
                        for(FlowGraphNode successor : block.getSuccessors()){
                            outputSet.retainAll(blockInputs.get(successor));
                        }
                    }

                    blockOutputs.put(block, outputSet);

                    List<ICode> icodeList = block.getICode();
                    for(int i = icodeList.size() - 1; i >= 0; i--){
                        ICode icode = icodeList.get(i);
                        instructionOutputs.put(icode, outputSet);
                        outputSet = transferFunction(block, icode, outputSet);
                        instructionInputs.put(icode, outputSet);
                    }

                    blockInputs.put(block, outputSet);
                }
            }

            blockOutputs.put(this.flowGraph.getEntry(), new HashSet<SetType>());
            blockInputs.put(this.flowGraph.getEntry(), new HashSet<SetType>());
        }
    }

    private Map<FlowGraphNode, Set<SetType>> deepCopyMap(Map<FlowGraphNode, Set<SetType>> blockOutputsOrInputs) {
        Map<FlowGraphNode, Set<SetType>> result = new HashMap<FlowGraphNode, Set<SetType>>();
        for(FlowGraphNode key : blockOutputsOrInputs.keySet()){
            Set<SetType> resultSet = new HashSet<SetType>();
            resultSet.addAll(blockOutputsOrInputs.get(key));
            result.put(key, resultSet);
        }
        return result;
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

    public abstract Set<SetType> transferFunction(FlowGraphNode block, ICode instr, Set<SetType> inputSet);
}
