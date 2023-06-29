package io.github.H20man13.DeClan.common.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.util.OpUtil;
import io.github.H20man13.DeClan.common.util.Utils;

public class ConstantPropogationAnalysis extends Analysis<Tuple<String, Object>> {

    private Map<ICode, Set<Tuple<String, Object>>> constDefinitions;
    private Map<ICode, Set<Tuple<String, Object>>> killDefinitions;

    public ConstantPropogationAnalysis(FlowGraph flowGraph) {
        super(flowGraph, Direction.FORWARDS, new Function<List<Set<Tuple<String, Object>>>, Set<Tuple<String, Object>>>(){
            @Override
            public Set<Tuple<String, Object>> apply(List<Set<Tuple<String, Object>>> list) {
                Set<Tuple<String, Object>> resultSet = new HashSet<Tuple<String, Object>>();
                HashMap<String, Set<Object>> hashMap = new HashMap<String, Set<Object>>();

                for(Set<Tuple<String, Object>> set : list){
                    for(Tuple<String, Object> tup : set){
                        String tupName = tup.source;
                        if(!hashMap.containsKey(tupName)){
                            hashMap.put(tupName, new HashSet<Object>());
                        }

                        Set<Object> objList = hashMap.get(tupName);
                        objList.add(tup.dest);
                    }
                }

                for(String key : hashMap.keySet()){
                    Set<Object> objValues = hashMap.get(key);
                    if(objValues.size() == 1){
                        for(Object val :  objValues){
                            resultSet.add(new Tuple<String, Object>(key, val));
                        }
                    }
                }

                return resultSet;
            }
        });

        this.constDefinitions = new HashMap<ICode, Set<Tuple<String, Object>>>();
        this.killDefinitions = new HashMap<ICode, Set<Tuple<String, Object>>>();

        for(BlockNode block : flowGraph.getBlocks()){
            for(ICode icode : block.getICode()){
                Set<Tuple<String, Object>> setTuples = new HashSet<Tuple<String, Object>>();
                Set<Tuple<String, Object>> killTuples = new HashSet<Tuple<String, Object>>();
                if(icode instanceof Assign){
                    Assign assICode = (Assign)icode;
                    if(assICode.isConstant()){
                        Object val = Utils.getValue(assICode.value);
                        Tuple<String, Object> newTuple = new Tuple<String, Object>(assICode.place, val);
                        setTuples.add(newTuple);
                    } else if(assICode.value instanceof BinExp){
                        BinExp exp = (BinExp)assICode.value;
                        if(exp.left.isConstant() && exp.right.isConstant()){
                            Object val1 = Utils.getValue(exp.left);
                            Object val2 = Utils.getValue(exp.right);
                            Object result = null;
                            switch (exp.op){
                                case ADD: result = OpUtil.plus(val1, val2);
                                    break;
                                case SUB: result = OpUtil.minus(val1, val2);
                                    break;
                                case MUL: result = OpUtil.times(val1, val2);
                                    break;
                                case DIV: result = OpUtil.divide(val1, val2);
                                    break;
                                case GE: result = OpUtil.greaterThanOrEqualTo(val1, val2);
                                    break;
                                case GT: result = OpUtil.greaterThan(val1, val2);
                                    break;
                                case LE: result = OpUtil.lessThanOrEqualTo(val1, val2);
                                    break;
                                case LT: result = OpUtil.lessThan(val1, val2);
                                    break;
                                case EQ: result = OpUtil.equal(val1, val2);
                                    break;
                                case NE: result = OpUtil.notEqual(val1, val2);
                                    break;
                                case BAND: result = OpUtil.and(val1, val2);
                                    break;
                                case BOR: result = OpUtil.or(val1, val2);
                                    break;
                                default:
                                    result = null;
                            }

                            Tuple<String, Object> newTuple = new Tuple<String, Object>(assICode.place, result);
                            setTuples.add(newTuple);
                        }
                    } else if(assICode.value instanceof UnExp){
                        UnExp exp = (UnExp)assICode.value;
                        if(exp.right.isConstant()){
                            Object right = Utils.getValue(exp.right);
                            Object result = null;
                            
                            switch(exp.op){
                                case NEG: result = OpUtil.negate(right);
                                    break;
                                case BNOT: result = OpUtil.not(right);
                                    break;
                                default:
                                    result = null;
                            }

                            Tuple<String, Object> newTuple = new Tuple<String, Object>(assICode.place, result);
                            setTuples.add(newTuple);
                        }
                    } else {
                        Tuple<String, Object> killTuple = new Tuple<String, Object>(assICode.place, null);
                        killTuples.add(killTuple);
                    }
                }
                constDefinitions.put(icode, setTuples);
                killDefinitions.put(icode, killTuples);
            }
        }
    }

    @Override
    public Set<Tuple<String, Object>> transferFunction(FlowGraphNode block, ICode instruction, Set<Tuple<String, Object>> inputSet){
        Set<Tuple<String, Object>> result = new HashSet<Tuple<String, Object>>();

        result.addAll(inputSet);

        for(Tuple<String, Object> killTuple : killDefinitions.get(instruction)){
            String killText = killTuple.source;
            for(Tuple<String, Object> singleResult : result){
                if(killText.equals(singleResult.source)){
                    result.remove(singleResult);
                }
            }
        }

        result.addAll(constDefinitions.get(instruction));
        
        return result;
    }
}
