package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import io.github.H20man13.DeClan.common.analysis.AnalysisBase;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase.Direction;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.ICode;

public abstract class IterativeAnalysis<AnalysisType, SetType> implements AnalysisBase {
	private FlowGraph flowGraph;
    private Direction direction;
    private Function<List<Set<SetType>>, Set<SetType>> meetOperation;
    private Set<SetType> semiLattice;

    private Map<AnalysisType, Set<SetType>> mappedOutputs;
    private Map<AnalysisType, Set<SetType>> mappedInputs;
    
	public abstract Set<SetType> transferFunction(AnalysisType type, Set<SetType> inputSet);
	
	public IterativeAnalysis(FlowGraph flowGraph, Direction direction, Meet meetOperation, Set<SetType> semiLattice) {
		this.flowGraph = flowGraph;
        this.direction = direction;
        Function<List<Set<SetType>>,Set<SetType>> unionOperation = new Function<List<Set<SetType>>,Set<SetType>>() {
            @Override
            public Set<SetType> apply(List<Set<SetType>> t) {
                Set<SetType> result = new HashSet<SetType>();
                for(Set<SetType> set : t){
                    result.addAll(set);
                }
                return result;
            } 
        };
        Function<List<Set<SetType>>,Set<SetType>> intersectionOperation = new Function<List<Set<SetType>>,Set<SetType>>() {
            @Override
            public Set<SetType> apply(List<Set<SetType>> t) {
                Set<SetType> result = unionOperation.apply(t);
                for(Set<SetType> set : t){
                    result.retainAll(set);
                }
                return result;
            }
        };

        if(meetOperation == Meet.UNION){
            this.meetOperation = unionOperation;
        } else {
            this.meetOperation = intersectionOperation;
        }

        this.mappedInputs = new HashMap<AnalysisType, Set<SetType>>();
        this.mappedOutputs = new HashMap<AnalysisType, Set<SetType>>();
        this.semiLattice = semiLattice;
	}
	
	public IterativeAnalysis(FlowGraph flowGraph, Direction direction, Function<List<Set<SetType>>, Set<SetType>> meetOperation, Set<SetType> semilattice){
        this.flowGraph = flowGraph;
        this.direction = direction;
        this.meetOperation = meetOperation;
        this.semiLattice = semilattice;
        this.mappedInputs = new HashMap<AnalysisType, Set<SetType>>();
        this.mappedOutputs = new HashMap<AnalysisType, Set<SetType>>();
    }
	
	public IterativeAnalysis(FlowGraph flowGraph, Direction direction, Function<List<Set<SetType>>, Set<SetType>> meetOperation){
       this(flowGraph, direction, meetOperation, new HashSet<SetType>());
    }

    public IterativeAnalysis(FlowGraph flowGraph, Direction direction, Meet meetOperation){
        this(flowGraph, direction, meetOperation, new HashSet<SetType>());
    }
    
    public Set<SetType> getInputSet(AnalysisType instruction){
        return mappedInputs.get(instruction);
    }

    public Set<SetType> getOutputSet(AnalysisType instruction){
        return mappedOutputs.get(instruction);
    }
    
    protected void addEmptyInputSet(AnalysisType analysisType) {
    	mappedInputs.put(analysisType, new HashSet<SetType>());
    }
    
    protected void addEmptyOutputSet(AnalysisType analysisType) {
    	mappedOutputs.put(analysisType, new HashSet<SetType>());
    }
    
    protected void addOutputSet(AnalysisType anslysisType, Set<SetType> setToAdd) {
    	mappedOutputs.put(anslysisType, setToAdd);
    }
    
    protected void addInputSet(AnalysisType anslysisType, Set<SetType> setToAdd) {
    	mappedInputs.put(anslysisType, setToAdd);
    }
    
    protected Map<AnalysisType, Set<SetType>> deepCopyOutputMap() {
    	Map<AnalysisType, Set<SetType>> result = new HashMap<AnalysisType, Set<SetType>>();
        for(AnalysisType key : mappedOutputs.keySet()){
            Set<SetType> resultSet = new HashSet<SetType>();
            resultSet.addAll(mappedOutputs.get(key));
            result.put(key, resultSet);
        }
        return result;
    }
    
    protected Map<AnalysisType, Set<SetType>> deepCopyInputMap() {
    	Map<AnalysisType, Set<SetType>> result = new HashMap<AnalysisType, Set<SetType>>();
        for(AnalysisType key : mappedInputs.keySet()){
            Set<SetType> resultSet = new HashSet<SetType>();
            resultSet.addAll(mappedInputs.get(key));
            result.put(key, resultSet);
        }
        return result;
    }
    
    protected boolean changesHaveOccuredOnOutputs(Map<AnalysisType, Set<SetType>> cached){
        Set<AnalysisType> keys = mappedOutputs.keySet();
        for(AnalysisType key : keys){
            if(!cached.containsKey(key)){
                return true;
            }

            Set<SetType> actualData = mappedOutputs.get(key);
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
    
    protected boolean changesHaveOccuredOnInputs(Map<AnalysisType, Set<SetType>> cached){
        Set<AnalysisType> keys = mappedInputs.keySet();
        for(AnalysisType key : keys){
            if(!cached.containsKey(key)){
                return true;
            }

            Set<SetType> actualData = mappedInputs.get(key);
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
    
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	for(AnalysisType analysisElem: this.mappedInputs.keySet()) {
    		sb.append("Input Set\r\n");
    		Set<SetType> inputSet = mappedInputs.get(analysisElem);
    		sb.append(inputSet.toString());
    		sb.append("\n|\nV\n");
    		sb.append(analysisElem.toString());
    		sb.append("|\nV\n");    		
    		sb.append("Output Set\r\n");
    		Set<SetType> outputSet = mappedOutputs.get(analysisElem);
    		sb.append(outputSet);
    		sb.append("\r\n\r\n\r\n");
    	}
    	return sb.toString();
    }
    
    public void run() {
    	runAnalysis(flowGraph, direction, meetOperation, semiLattice);
    }
    
    protected abstract void runAnalysis(FlowGraph flowGraph, Direction direction, Function<List<Set<SetType>>, Set<SetType>> meetOperation, Set<SetType> semiLattice);
}
