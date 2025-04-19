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
    
    protected MapType deepCopyOutputMap() {
    	MapType result = newMap();
        for(AnalysisType key : mappedOutputs.keySet()){
            SetType resultSet = newSet();
            resultSet.addAll(mappedOutputs.get(key));
            result.put(key, resultSet);
        }
        return result;
    }
    
    protected MapType deepCopyInputMap() {
    	MapType result = newMap();
        for(AnalysisType key : mappedInputs.keySet()){
            SetType resultSet = newSet();
            resultSet.addAll(mappedInputs.get(key));
            result.put(key, resultSet);
        }
        return result;
    }
    
    protected boolean changesHaveOccuredOnOutputs(MapType cached){
    	if(cfg == null || !cfg.containsFlag("debug")){
			Set<AnalysisType> keys = mappedOutputs.keySet();
	        for(AnalysisType key : keys){
	            if(!cached.containsKey(key)){
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
    		boolean result = false;
    		Set<AnalysisType> keys = mappedOutputs.keySet();
	        for(AnalysisType key : keys){
	            if(!cached.containsKey(key)){
	            	System.out.println("Cached map contains " + key + " and actual data does not");
	                result = true;
	                continue;
	            }

	            SetType actualData = mappedOutputs.get(key);
	            SetType cachedData = cached.get(key);

	            if(actualData.size() != cachedData.size()) {
	            	System.out.println("For " + key + " actual data size not equal to cache data size!!!");
	            	System.out.println("Actual data size: " + actualData.size());
	            	System.out.println("Actual data: " + actualData.toString());
	            	System.out.println("Cached data size: " + cachedData.size());
	            	System.out.println("Cached data: " + cachedData.toString());
	                result = true;
	                continue;
	            }

	            if(!actualData.equals(cachedData)){
	            	System.out.println("For " + key + " actual data not equal to cache data");
	            	System.out.println("Actual data: " + actualData.toString());
	            	System.out.println("Cached data: " + cachedData.toString());
	                result = true;
	                continue;
	            }
	        }
	        
	        return result;
    	}
    }
    
    protected boolean changesHaveOccuredOnInputs(MapType cached){
        Set<AnalysisType> keys = mappedInputs.keySet();
        for(AnalysisType key : keys){
            if(!cached.containsKey(key)){
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
