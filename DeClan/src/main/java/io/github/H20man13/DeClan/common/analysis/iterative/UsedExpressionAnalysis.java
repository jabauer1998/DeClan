package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.Exp;

public class UsedExpressionAnalysis extends InstructionAnalysis<Tuple<Exp, ICode.Type>> {
    private Map<ICode, Set<Tuple<Exp, ICode.Type>>> usedSets;
    private Map<ICode, Set<Tuple<Exp, ICode.Type>>> latest;

    public UsedExpressionAnalysis(FlowGraph flowGraph, Map<ICode, Set<Tuple<Exp, ICode.Type>>> usedSets, Map<ICode, Set<Tuple<Exp, ICode.Type>>> latest) {
        super(flowGraph, Direction.BACKWARDS, Meet.UNION);

        this.latest = latest;
        this.usedSets = usedSets;
    }

    @Override
    public Set<Tuple<Exp, ICode.Type>> transferFunction(ICode block, Set<Tuple<Exp, ICode.Type>> inputSet) {
        Set<Tuple<Exp, ICode.Type>> result = new HashSet<Tuple<Exp, ICode.Type>>();

        Set<Tuple<Exp, ICode.Type>> usedInBlock = usedSets.get(block);
        result.addAll(usedInBlock);
        result.addAll(inputSet);
        result.removeAll(latest.get(block));
    
        return result;
    }
    
}
