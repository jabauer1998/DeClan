package io.github.H20man13.DeClan.common.analysis.iterative;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import io.github.H20man13.DeClan.common.Config;
import io.github.H20man13.DeClan.common.Copyable;
import io.github.H20man13.DeClan.common.CustomMeet;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase.Direction;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase.Meet;
import io.github.H20man13.DeClan.common.exception.IterativeAnalysisException;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.util.Utils;

public abstract class IterativeAnalysis<AnalysisType extends Copyable<AnalysisType>, 
										MapType extends Map<AnalysisType, DataType>, 
										DataType> implements AnalysisBase {
	private FlowGraph flowGraph;
    private Direction direction;
    private Function<List<DataType>, DataType> meetOperation;
    private DataType semiLattice;
    private Class<MapType> mapClass;
    private boolean copyKey;
    protected Config cfg;

    private MapType mappedOutputs;
    private MapType mappedInputs;
    
	public abstract DataType transferFunction(AnalysisType type, DataType inputSet);
	
	public IterativeAnalysis(FlowGraph flowGraph, Direction direction, DataType semiLattice, boolean copyKey, Config cfg, Class<MapType> mapClass) {
		this.flowGraph = flowGraph;
        this.direction = direction;
        this.mapClass = mapClass;
        if(this instanceof CustomMeet) {
	       	CustomMeet<DataType> meetOp = (CustomMeet<DataType>)this;
	        this.meetOperation = new Function<List<DataType>, DataType>() {
					@Override
					public DataType apply(List<DataType> t) {
						return meetOp.performMeet(t);
					}
	       	};
       } else {
       		throw new IterativeAnalysisException("IterativeAnalysis(Constructor)", "Error expected analysis type to inherit from CustomMeet");
       }
		this.mappedInputs = newMap();
		this.mappedOutputs = newMap();
	    this.semiLattice = semiLattice;
	    this.copyKey = copyKey;
	    this.cfg = cfg;
	}
    
    @SuppressWarnings("deprecation")
	protected MapType newMap() {
    	try {
			return mapClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
    }
    
    public DataType getInputSet(AnalysisType instruction){
        return mappedInputs.get(instruction);
    }

    public DataType getOutputSet(AnalysisType instruction){
        return mappedOutputs.get(instruction);
    }
    
    protected void addOutputSet(AnalysisType anslysisType, DataType setToAdd) {
    	mappedOutputs.put(anslysisType, setToAdd);
    }
    
    protected boolean containsInputKey(AnalysisType type) {
    	return mappedInputs.containsKey(type);
    }
    
    protected boolean containsOutputKey(AnalysisType type) {
    	return mappedOutputs.containsKey(type);
    }
    
    protected void addInputSet(AnalysisType anslysisType, DataType setToAdd) {
    	mappedInputs.put(anslysisType, setToAdd);
    }
    
    protected MapType copyOutputsFromFlowGraph() {
    	return copyOutputsFromFlowGraph(flowGraph);
    }
    
    protected MapType copyInputsFromFlowGraph() {
    	return copyInputsFromFlowGraph(flowGraph);
    }
    
    protected abstract MapType copyOutputsFromFlowGraph(FlowGraph flow);
    protected abstract MapType copyInputsFromFlowGraph(FlowGraph flow);
    protected abstract boolean changesHaveOccuredOnOutputs(MapType cached);
    protected abstract boolean changesHaveOccuredOnInputs(MapType cached);
    
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	for(AnalysisType analysisElem: this.mappedInputs.keySet()) {
    		sb.append("Input Set\r\n");
    		DataType inputSet = mappedInputs.get(analysisElem);
    		if(inputSet != null)
    			sb.append(inputSet.toString());
    		else
    			sb.append("NULL");
    		sb.append("\n|\nV\n");
    		sb.append(analysisElem.toString());
    		sb.append("\r\n|\nV\n");    		
    		sb.append("Output Set\r\n");
    		DataType outputSet = mappedOutputs.get(analysisElem);
    		if(outputSet != null)
    			sb.append(outputSet);
    		else
    			sb.append("NULL");
    		sb.append("\r\n\r\n\r\n");
    	}
    	return sb.toString();
    }
    
    public void run() {
    	runAnalysis(flowGraph, direction, meetOperation, semiLattice, copyKey);
    }
    
    protected void analysisLoopStartAction() {
    	//Default is do nothing
    }
    
    protected void analysisLoopEndAction() {
    	//Default is do nothing
    }
    
    protected abstract void runAnalysis(FlowGraph flowGraph, Direction direction, Function<List<DataType>, DataType> meetOperation, DataType semiLattice, boolean copyKey);
}
