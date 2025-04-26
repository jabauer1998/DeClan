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
import io.github.H20man13.DeClan.common.exception.IterativeAnalysisException;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.util.Utils;

public abstract class IterativeAnalysis<AnalysisType extends Copyable<AnalysisType>, MapType extends Map<AnalysisType, SetType>, SetType extends Set<DataType>, DataType> implements AnalysisBase {
	private FlowGraph flowGraph;
    private Direction direction;
    private Function<List<SetType>, SetType> meetOperation;
    private Set<DataType> semiLattice;
    private Class<MapType> mapClass;
    private Class<SetType> setClass;
    private boolean copyKey;
    protected Config cfg;

    private MapType mappedOutputs;
    private MapType mappedInputs;
    
	public abstract SetType transferFunction(AnalysisType type, SetType inputSet);
	
	public IterativeAnalysis(FlowGraph flowGraph, Direction direction, Meet meetOperation, Set<DataType> semiLattice, boolean copyKey, Config cfg, Class<MapType> mapClass, Class<SetType> setClass) {
		this.flowGraph = flowGraph;
        this.direction = direction;
        this.mapClass = mapClass;
        this.setClass = setClass;
		this.mappedInputs = newMap();
		this.mappedOutputs = newMap();
	    this.semiLattice = semiLattice;
	    this.copyKey = copyKey;
	    this.cfg = cfg;
	    
        Function<List<SetType>,SetType> unionOperation = new Function<List<SetType>, SetType>() {
            @Override
            public SetType apply(List<SetType> t) {
                SetType result = newSet();
				for(SetType set : t){
					result.addAll(set);
	            }
		        return result;
            }
        };
        Function<List<SetType>, SetType> intersectionOperation = new Function<List<SetType>, SetType>() {
            @Override
            public SetType apply(List<SetType> t) {
                SetType result = unionOperation.apply(t);
                for(SetType set : t){
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
	}
	
	@SuppressWarnings("unchecked")
	public IterativeAnalysis(FlowGraph flowGraph, Direction direction, Set<DataType> semilattice, boolean copyKey, Config cfg, Class<MapType> mapClass, Class<SetType> setClass){
        this.flowGraph = flowGraph;
        this.direction = direction;
        if(this instanceof CustomMeet) {
        	CustomMeet<SetType, DataType> meetOp = (CustomMeet<SetType, DataType>)this;
        	this.meetOperation = new Function<List<SetType>, SetType>() {
				@Override
				public SetType apply(List<SetType> t) {
					return meetOp.performMeet(t);
				}
        	};
        } else {
        	throw new IterativeAnalysisException("IterativeAnalysis(Constructor)", "Error expected analysis type to inherit from CustomMeet");
        }
        this.cfg = cfg;
        this.semiLattice = semilattice;
        this.copyKey = copyKey;
		this.mappedInputs = newMap();
		this.mappedOutputs = newMap();
    }
	
	@SuppressWarnings("unchecked")
	public IterativeAnalysis(FlowGraph flowGraph, Direction direction, boolean copyKey, Config cfg, Class<MapType> mapClass, Class<SetType> setClass){
       this.flowGraph = flowGraph;
       this.direction = direction;
       if(this instanceof CustomMeet) {
	       	CustomMeet<SetType, DataType> meetOp = (CustomMeet<SetType, DataType>)this;
	       	this.meetOperation = new Function<List<SetType>, SetType>() {
					@Override
					public SetType apply(List<SetType> t) {
						return meetOp.performMeet(t);
					}
	       	};
       } else {
       		throw new IterativeAnalysisException("IterativeAnalysis(Constructor)", "Error expected analysis type to inherit from CustomMeet");
       }
       this.cfg = cfg;
       this.setClass = setClass;
       this.mapClass = mapClass;
       this.copyKey = copyKey;
       this.mappedInputs = newMap();
       this.mappedOutputs = newMap();
       this.semiLattice = newSet();
    }

    public IterativeAnalysis(FlowGraph flowGraph, Direction direction, Meet meetOperation, boolean copyKey, Config cfg, Class<MapType> mapClass, Class<SetType> setClass){
    	this.flowGraph = flowGraph;
        this.direction = direction;
        this.mapClass = mapClass;
        this.setClass = setClass;
        this.copyKey = copyKey;
		this.mappedInputs = newMap();
		this.mappedOutputs = newMap();
	    this.semiLattice = newSet();
	    this.cfg = cfg;
	        
        Function<List<SetType>,SetType> unionOperation = new Function<List<SetType>, SetType>() {
            @Override
            public SetType apply(List<SetType> t) {
                SetType result = newSet();
				for(SetType set : t){
					result.addAll(set);
	            }
		        return result;
            }
        };
        Function<List<SetType>, SetType> intersectionOperation = new Function<List<SetType>, SetType>() {
            @Override
            public SetType apply(List<SetType> t) {
                SetType result = unionOperation.apply(t);
                for(SetType set : t){
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
    }
    
    @SuppressWarnings("deprecation")
	protected SetType newSet() {
    	try {
			return setClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
    }
    
    @SuppressWarnings("deprecation")
	protected MapType newMap() {
    	try {
			return mapClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
    }
    
    public SetType getInputSet(AnalysisType instruction){
        return mappedInputs.get(instruction);
    }

    public SetType getOutputSet(AnalysisType instruction){
        return mappedOutputs.get(instruction);
    }
    
    protected void addEmptyInputSet(AnalysisType analysisType) {
    	mappedInputs.put(analysisType, newSet());
    }
    
    protected void addEmptyOutputSet(AnalysisType analysisType) {
    	mappedOutputs.put(analysisType, newSet());
    }
    
    protected void addOutputSet(AnalysisType anslysisType, SetType setToAdd) {
    	mappedOutputs.put(anslysisType, setToAdd);
    }
    
    protected void addInputSet(AnalysisType anslysisType, SetType setToAdd) {
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
    
    protected boolean changesHaveOccuredOnOutputs(MapType cached){
    	if(cfg == null || !cfg.containsFlag("debug")){
			Set<AnalysisType> keys = cached.keySet();
	        for(AnalysisType key : keys){
	            if(!mappedOutputs.containsKey(key)){
	                return true;
	            }

	            SetType actualData = mappedOutputs.get(key);
	            SetType cachedData = cached.get(key);

	            if(actualData.size() != cachedData.size()){
	                return true;
	            }

	            if(!actualData.equals(cachedData)){
	                return true;
	            }
	        }
	        return false;
    	} else {
    		Utils.createFile("test/temp/AnalysisCacheLog.txt");
    		boolean result = false;
    		Set<AnalysisType> keys = cached.keySet();
	        for(AnalysisType key : keys){
	            if(!mappedOutputs.containsKey(key)){
	            	Utils.appendToFile("test/temp/AnalysisCacheLog.txt", "Actual map contains " + key + " and cached data does not\r\n");
	                result = true;
	                continue;
	            }

	            SetType actualData = mappedOutputs.get(key);
	            SetType cachedData = cached.get(key);

	            if(actualData.size() != cachedData.size()) {
	            	Utils.appendToFile("test/temp/AnalysisCacheLog.txt", "For " + key + " actual data size not equal to cache data size!!!\r\n");
	            	Utils.appendToFile("test/temp/AnalysisCacheLog.txt", "Actual data size: " + actualData.size() + "\r\n");
	            	Utils.appendToFile("test/temp/AnalysisCacheLog.txt", "Actual data: " + actualData.toString() + "\r\n");
	            	Utils.appendToFile("test/temp/AnalysisCacheLog.txt", "Cached data size: " + cachedData.size() + "\r\n");
	            	Utils.appendToFile("test/temp/AnalysisCacheLog.txt", "Cached data: " + cachedData.toString() + "\r\n");
	                result = true;
	                continue;
	            }

	            if(!actualData.equals(cachedData)){
	            	Utils.appendToFile("test/temp/AnalysisCacheLog.txt", "For " + key + " actual data not equal to cache data\r\n");
	            	Utils.appendToFile("test/temp/AnalysisCacheLog.txt", "Actual data: " + actualData.toString() + "\r\n");
	            	Utils.appendToFile("test/temp/AnalysisCacheLog.txt", "Cached data: " + cachedData.toString() + "\r\n");
	                result = true;
	                continue;
	            }
	        }
	        
	        return result;
    	}
    }
    
    protected boolean changesHaveOccuredOnInputs(MapType cached){
        Set<AnalysisType> keys = cached.keySet();
        for(AnalysisType key : keys){
            if(!mappedInputs.containsKey(key)){
                return true;
            }

            SetType actualData = mappedInputs.get(key);
            SetType cachedData = cached.get(key);

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
    		SetType inputSet = mappedInputs.get(analysisElem);
    		if(inputSet != null)
    			sb.append(inputSet.toString());
    		else
    			sb.append("NULL");
    		sb.append("\n|\nV\n");
    		sb.append(analysisElem.toString());
    		sb.append("\r\n|\nV\n");    		
    		sb.append("Output Set\r\n");
    		SetType outputSet = mappedOutputs.get(analysisElem);
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
    
    protected abstract void runAnalysis(FlowGraph flowGraph, Direction direction, Function<List<SetType>, SetType> meetOperation, Set<DataType> semiLattice, boolean copyKey);
}
