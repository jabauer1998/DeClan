package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.Config;
import io.github.H20man13.DeClan.common.CopyStr;
import io.github.H20man13.DeClan.common.CustomMeet;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.NaaExp;
import io.github.H20man13.DeClan.common.icode.exp.NullableExp;
import io.github.H20man13.DeClan.common.icode.inline.Inline;
import io.github.H20man13.DeClan.common.icode.inline.InlineParam;
import io.github.H20man13.DeClan.common.util.Utils;

public class ConstantPropogationAnalysis extends InstructionAnalysis<HashMap<ICode, HashSet<Tuple<CopyStr, NullableExp>>>, HashSet<Tuple<CopyStr, NullableExp>>, Tuple<CopyStr, NullableExp>> 
implements CustomMeet<HashSet<Tuple<CopyStr, NullableExp>>>{

    private Map<ICode, HashSet<Tuple<CopyStr, NullableExp>>> constDefinitions;
    private Map<ICode, HashSet<Tuple<CopyStr, NullableExp>>> killDefinitions;
    
    private static CopyStr newS(String data) {
    	return new CopyStr(data);
    }

    public ConstantPropogationAnalysis(FlowGraph flowGraph, Config cfg) {
        super(flowGraph, Direction.FORWARDS, true, cfg, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));

        this.constDefinitions = newMap();
        this.killDefinitions = newMap();

        for(BlockNode block : flowGraph.getBlocks()){
            for(ICode icode : block.getICode()){
                HashSet<Tuple<CopyStr, NullableExp>> setTuples = newSet();
                HashSet<Tuple<CopyStr, NullableExp>> killTuples = newSet();
                if(icode instanceof Assign){
                    Assign assICode = (Assign)icode;
                    
                    if(assICode.isConstant()){
                        Tuple<CopyStr, NullableExp> newTuple = new Tuple<CopyStr, NullableExp>(newS(assICode.place), assICode.value);
                        setTuples.add(newTuple);
                    } else if(assICode.value instanceof IdentExp && assICode.getScope() != ICode.Scope.PARAM){
                    	IdentExp val = (IdentExp)assICode.value;
                    	if(val.scope != ICode.Scope.RETURN) {
                    		Tuple<CopyStr, NullableExp> newTuple = new Tuple<CopyStr, NullableExp>(newS(assICode.place), assICode.value);
                    		setTuples.add(newTuple);
                    	} else {
                    		Tuple<CopyStr, NullableExp> newTuple = new Tuple<CopyStr, NullableExp>(newS(assICode.place), new NaaExp());
                    		setTuples.add(newTuple);
                    	}
                    } else {
                    	Tuple<CopyStr, NullableExp> newTuple = new Tuple<CopyStr, NullableExp>(newS(assICode.place), new NaaExp());
                		setTuples.add(newTuple);
                    }
                    Tuple<CopyStr, NullableExp> killTuple = new Tuple<CopyStr, NullableExp>(newS(assICode.place), new NaaExp());
                    killTuples.add(killTuple);
                } else if(icode instanceof Def){
                	Def assICode = (Def)icode;
                    if(assICode.isConstant()){
                        Tuple<CopyStr, NullableExp> newTuple = new Tuple<CopyStr, NullableExp>(newS(assICode.label), assICode.val);
                        setTuples.add(newTuple);
                    } else if(assICode.val instanceof IdentExp && assICode.scope != ICode.Scope.PARAM){
                    	IdentExp ident = (IdentExp)assICode.val;
                    	if(ident.scope != ICode.Scope.RETURN) {
                    		Tuple<CopyStr, NullableExp> newTuple = new Tuple<CopyStr, NullableExp>(newS(assICode.label), assICode.val);
                    		setTuples.add(newTuple);
                    	} else {
                    		Tuple<CopyStr, NullableExp> newTuple = new Tuple<CopyStr, NullableExp>(newS(assICode.label), new NaaExp());
                    		setTuples.add(newTuple);
                    	}
                    } else {
                    	Tuple<CopyStr, NullableExp> newTuple = new Tuple<CopyStr, NullableExp>(newS(assICode.label), new NaaExp());
                		setTuples.add(newTuple);
                    }
                } else if(icode instanceof Inline) {
                	Inline inline = (Inline)icode;
                	for(InlineParam param: inline.params) {
                		if(param.containsAllQual(InlineParam.IS_DEFINITION))
                			killTuples.add(new Tuple<CopyStr, NullableExp>(newS(param.name.ident), new NaaExp()));
                		else if(param.containsAllQual(InlineParam.IS_USE))
                			setTuples.add(new Tuple<CopyStr, NullableExp>(newS(param.name.ident), new NaaExp()));
                	}
                }

                constDefinitions.put(icode, setTuples);
                killDefinitions.put(icode, killTuples);
            }
        }
    }

    @Override
    public HashSet<Tuple<CopyStr, NullableExp>> transferFunction(ICode instruction, HashSet<Tuple<CopyStr, NullableExp>> inputSet){
        HashSet<Tuple<CopyStr, NullableExp>> result = newSet();
        result.addAll(inputSet);

        HashSet<Tuple<CopyStr, NullableExp>> finalResult = newSet();

        HashSet<Tuple<CopyStr, NullableExp>> killSet = killDefinitions.get(instruction);
        for(Tuple<CopyStr, NullableExp> res: result){
            CopyStr resTest = res.source;
            NullableExp resDest = res.dest;            
            if(!Utils.containsExpInSet(killSet, resTest.toString()))
            	if(!Utils.containsExpInSet(killSet, resDest))
            		finalResult.add(res);
        }

        finalResult.addAll(constDefinitions.get(instruction));
        
        return finalResult;
    }

	@Override
	public HashSet<Tuple<CopyStr, NullableExp>> performMeet(List<HashSet<Tuple<CopyStr, NullableExp>>> list) {
       HashSet<Tuple<CopyStr, NullableExp>> resultSet = newSet();
       HashMap<String, HashSet<NullableExp>> hashMap = new HashMap<String, HashSet<NullableExp>>();

        for(Set<Tuple<CopyStr, NullableExp>> set : list){
            for(Tuple<CopyStr, NullableExp> tup : set){
                CopyStr tupName = tup.source;
                if(!hashMap.containsKey(tupName.toString())){
                    hashMap.put(tupName.toString(), new HashSet<NullableExp>());
                }

                Set<NullableExp> objList = hashMap.get(tupName.toString());
                objList.add(tup.dest);
            }
        }

        for(String key : hashMap.keySet()){
            HashSet<NullableExp> objValues = hashMap.get(key);
            if(objValues.size() == 1){
                for(NullableExp val :  objValues){
                    resultSet.add(new Tuple<CopyStr, NullableExp>(newS(key), val));
                }
            } else if(objValues.size() > 1) {
            	resultSet.add(new Tuple<CopyStr, NullableExp>(newS(key), new NaaExp()));
            }
        }

        return resultSet;
	}
}
