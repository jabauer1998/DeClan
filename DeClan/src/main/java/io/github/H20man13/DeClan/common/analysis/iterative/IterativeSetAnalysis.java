package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import io.github.H20man13.DeClan.common.Config;
import io.github.H20man13.DeClan.common.Copyable;
import io.github.H20man13.DeClan.common.CustomMeet;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase.Direction;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase.Meet;
import io.github.H20man13.DeClan.common.exception.IterativeAnalysisException;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.icode.exp.NaaExp;
import io.github.H20man13.DeClan.common.icode.exp.NullableExp;
import io.github.H20man13.DeClan.common.util.Utils;

public abstract class IterativeSetAnalysis<AnalysisType extends Copyable<AnalysisType>,
								  MapType extends Map<AnalysisType, SetType>,
								  SetType extends Set<DataType>,
								  DataType> extends IterativeAnalysis<AnalysisType, MapType, SetType> 
								  implements CustomMeet<SetType>{
	private Class<SetType> setClass;
	private Meet meet;
	
	public IterativeSetAnalysis(FlowGraph flowGraph, Direction direction, Meet meet, SetType semilattice, boolean copyKey, Config cfg, Class<MapType> mapClass, Class<SetType> setClass){
        super(flowGraph, direction, semilattice, copyKey, cfg, mapClass);
        this.setClass = setClass;
        this.meet = meet;
    }
	
	@SuppressWarnings("deprecation")
	public IterativeSetAnalysis(FlowGraph flowGraph, Direction direction, Meet meet, boolean copyKey, Config cfg, Class<MapType> mapClass, Class<SetType> setClass){
        super(flowGraph, direction, Utils.createNewSet(setClass), copyKey, cfg, mapClass);
        this.setClass = setClass;
        this.meet = meet;
    }
	
	public IterativeSetAnalysis(FlowGraph flowGraph, Direction direction, SetType semilattice, boolean copyKey, Config cfg, Class<MapType> mapClass, Class<SetType> setClass){
        super(flowGraph, direction, semilattice, copyKey, cfg, mapClass);
        this.setClass = setClass;
        this.meet = null;
    }
	
	@SuppressWarnings("deprecation")
	public IterativeSetAnalysis(FlowGraph flowGraph, Direction direction, boolean copyKey, Config cfg, Class<MapType> mapClass, Class<SetType> setClass){
        super(flowGraph, direction, Utils.createNewSet(setClass), copyKey, cfg, mapClass);
        this.setClass = setClass;
        this.meet = null;
    }
    
    @SuppressWarnings("deprecation")
	protected SetType newSet() {
    	try {
			return setClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
    }
    
    protected void addEmptyInputSet(AnalysisType analysisType) {
    	addInputSet(analysisType, newSet());
    }
    
    protected void addEmptyOutputSet(AnalysisType analysisType) {
    	addOutputSet(analysisType, newSet());
    }
    
    protected boolean changesHaveOccuredOnOutputs(MapType cached){
    	//if(cfg == null || !cfg.containsFlag("debug")){
			Set<AnalysisType> keys = cached.keySet();
	        for(AnalysisType key : keys){
	            if(!containsOutputKey(key)){
	                return true;
	            }

	            SetType actualData = getOutputSet(key);
	            SetType cachedData = cached.get(key);

	            if(actualData.size() != cachedData.size()){
	                return true;
	            }

	            if(!actualData.equals(cachedData)){
	                return true;
	            }
	        }
	        return false;
    	/*} else {
    		Utils.createFile("test/temp/AnalysisCacheLog.txt");
    		boolean result = false;
    		Set<AnalysisType> keys = cached.keySet();
	        for(AnalysisType key : keys){
	            if(!containsOutputKey(key)){
	            	Utils.appendToFile("test/temp/AnalysisCacheLog.txt", "Actual map contains " + key + " and cached data does not\r\n");
	                result = true;
	                continue;
	            }

	            SetType actualData = getOutputSet(key);
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
    	}*/
    }
    
    @Override
	public SetType performMeet(List<SetType> list) {
       if(meet == Meet.UNION) {
    	   SetType result = newSet();
			for(SetType set : list){
				result.addAll(set);
           }
	        return result;
       } else {
    	   SetType result = newSet();
    	   for(SetType set : list){
               result.retainAll(set);
           }
           return result;
       }
	}
    
    
    protected boolean changesHaveOccuredOnInputs(MapType cached){
        Set<AnalysisType> keys = cached.keySet();
        for(AnalysisType key : keys){
            if(!containsInputKey(key)){
                return true;
            }

            SetType actualData = getInputSet(key);
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
}
