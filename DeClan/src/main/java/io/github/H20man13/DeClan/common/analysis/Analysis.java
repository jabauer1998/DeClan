package io.github.H20man13.DeClan.common.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;

public abstract class Analysis<SetType> {
    private FlowGraph flowGraph;
    private Direction direction;
    private Meet symbol;

    private Map<FlowGraphNode, Set<SetType>> outputs;
    private Map<FlowGraphNode, Set<SetType>> inputs;

    public enum Direction{
        BACKWARDS,
        FORWARDS,    
    }

    public enum Meet{
        UNION,
        INTERSECTION
    }
    
    public Analysis(FlowGraph flowGraph, Direction direction, Meet symbol){
        this.flowGraph = flowGraph;
        this.direction = direction;
        this.symbol = symbol;
        this.outputs = new HashMap<>();
        this.inputs = new HashMap<>();
    }

    public void run(){
        if(this.direction == Direction.FORWARDS){
            Map<FlowGraphNode, Set<SetType>> outputCache = new HashMap<>();
            outputs.put(this.flowGraph.getEntry(), new HashSet<SetType>());

            for(BlockNode block : this.flowGraph.getBlocks()){
                outputs.put(block, new HashSet<SetType>());
            }

            while(changesHaveOccured(this.outputs, outputCache)){
                outputCache.putAll(outputs);
                for(BlockNode block : this.flowGraph.getBlocks()){
                    if(!inputs.containsKey(block)){
                        inputs.put(block, new HashSet<SetType>());
                    }

                    Set<SetType> inputSet = inputs.get(block);

                    if(this.symbol == Meet.UNION){
                        for(FlowGraphNode predecessor : block.getPredecessors()){
                            inputSet.addAll(outputs.get(predecessor));
                        }
                    } else {
                        for(FlowGraphNode predecessor : block.getPredecessors()){
                            inputSet.retainAll(outputs.get(predecessor));
                        }
                    }
                    
                    outputs.put(block, transferFunction(block, inputSet));
                }
            }
        } else {
            Map<FlowGraphNode, Set<SetType>> inputCache = new HashMap<FlowGraphNode, Set<SetType>>();

            inputs.put(this.flowGraph.getExit(), new HashSet<SetType>());

            for(BlockNode block : this.flowGraph.getBlocks()){
                inputs.put(block, new HashSet<SetType>());
            }

            while(changesHaveOccured(this.inputs, inputCache)){
                inputCache.putAll(this.inputs);
                for(BlockNode block : this.flowGraph.getBlocks()){
                    if(!outputs.containsKey(block)){
                        outputs.put(block, new HashSet<SetType>());
                    }

                    Set<SetType> outputSet = outputs.get(block);
                    if(this.symbol == Meet.UNION){
                        for(FlowGraphNode successor : block.getSuccessors()){
                            outputSet.addAll(inputs.get(successor));
                        }
                    } else {
                        for(FlowGraphNode successor : block.getSuccessors()){
                            outputSet.retainAll(inputs.get(successor));
                        }
                    }

                    inputs.put(block, transferFunction(block, outputSet));
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

    public abstract Set<SetType> transferFunction(FlowGraphNode Node, Set<SetType> inputSet);
}
