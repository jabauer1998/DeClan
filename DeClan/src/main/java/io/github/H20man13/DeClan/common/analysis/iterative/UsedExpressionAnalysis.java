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
import io.github.H20man13.DeClan.common.util.Utils;

public class UsedExpressionAnalysis extends InstructionAnalysis<HashMap<ICode, HashSet<Tuple<Exp, ICode.Type>>>, HashSet<Tuple<Exp, ICode.Type>>, Tuple<Exp, ICode.Type>> {
    private Map<ICode, Set<Tuple<Exp, ICode.Type>>> usedSets;
    private Map<ICode, Set<Tuple<Exp, ICode.Type>>> latest;

    public UsedExpressionAnalysis(FlowGraph flowGraph, Map<ICode, Set<Tuple<Exp, ICode.Type>>> usedSets, Map<ICode, Set<Tuple<Exp, ICode.Type>>> latest, Config cfg) {
        super(flowGraph, Direction.BACKWARDS, Meet.UNION, false, cfg, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));

        this.latest = latest;
        this.usedSets = usedSets;
    }

    @Override
    public HashSet<Tuple<Exp, ICode.Type>> transferFunction(ICode block, HashSet<Tuple<Exp, ICode.Type>> inputSet) {
        HashSet<Tuple<Exp, ICode.Type>> result = newSet();

        Set<Tuple<Exp, ICode.Type>> usedInBlock = usedSets.get(block);
        result.addAll(usedInBlock);
        result.addAll(inputSet);
        result.removeAll(latest.get(block));
    
        return result;
    }
    
}
