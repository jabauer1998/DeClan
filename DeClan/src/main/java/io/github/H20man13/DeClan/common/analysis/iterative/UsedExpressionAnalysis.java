package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.Config;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.NullableExp;
import io.github.H20man13.DeClan.common.util.Utils;

public class UsedExpressionAnalysis extends InstructionAnalysis<HashMap<ICode, HashSet<Tuple<NullableExp, ICode.Type>>>, HashSet<Tuple<NullableExp, ICode.Type>>, Tuple<NullableExp, ICode.Type>> {
    private Map<ICode, Set<Tuple<NullableExp, ICode.Type>>> usedSets;
    private Map<ICode, Set<Tuple<NullableExp, ICode.Type>>> latest;

    public UsedExpressionAnalysis(FlowGraph flowGraph, Map<ICode, Set<Tuple<NullableExp, ICode.Type>>> usedSets, Map<ICode, Set<Tuple<NullableExp, ICode.Type>>> latest, Config cfg) {
        super(flowGraph, Direction.BACKWARDS, Meet.UNION, true, cfg, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));

        this.latest = latest;
        this.usedSets = usedSets;
    }

    @Override
    public HashSet<Tuple<NullableExp, ICode.Type>> transferFunction(ICode block, HashSet<Tuple<NullableExp, ICode.Type>> inputSet) {
        HashSet<Tuple<NullableExp, ICode.Type>> result = newSet();

        Set<Tuple<NullableExp, ICode.Type>> usedInBlock = usedSets.get(block);
        result.addAll(usedInBlock);
        result.addAll(inputSet);
        result.removeAll(latest.get(block));
    
        return result;
    }
    
}
