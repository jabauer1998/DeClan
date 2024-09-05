package io.github.H20man13.DeClan.common.analysis;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.Exp;

public class UsedExpressionAnalysis extends BasicBlockAnalysis<Exp> {
    private Map<ICode, Set<Exp>> usedSets;
    private Map<FlowGraphNode, Set<Exp>> latest;

    public UsedExpressionAnalysis(FlowGraph flowGraph, Map<ICode, Set<Exp>> usedSets, Map<FlowGraphNode, Set<Exp>> latest) {
        super(flowGraph, Direction.BACKWARDS, Meet.UNION);

        this.latest = latest;
        this.usedSets = usedSets;
    }

    @Override
    public Set<Exp> transferFunction(FlowGraphNode block, Set<Exp> inputSet) {
        Set<Exp> result = new HashSet<Exp>();

        Set<Exp> usedInBlock = usedSets.get(block);
        result.addAll(usedInBlock);
        result.addAll(inputSet);
        result.removeAll(latest.get(block));
    
        return result;
    }
    
}
