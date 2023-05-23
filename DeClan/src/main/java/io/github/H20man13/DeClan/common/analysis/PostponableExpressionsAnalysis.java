package io.github.H20man13.DeClan.common.analysis;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.LetBin;
import io.github.H20man13.DeClan.common.icode.LetBool;
import io.github.H20man13.DeClan.common.icode.LetInt;
import io.github.H20man13.DeClan.common.icode.LetReal;
import io.github.H20man13.DeClan.common.icode.LetString;
import io.github.H20man13.DeClan.common.icode.LetUn;
import io.github.H20man13.DeClan.common.icode.LetVar;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.util.Utils;

public class PostponableExpressionsAnalysis extends Analysis<Exp> {
    private Map<FlowGraphNode, Set<Exp>> usedSets;
    private Map<FlowGraphNode, Set<Exp>> earliest;
    
    public PostponableExpressionsAnalysis(FlowGraph flowGraph, Set<Exp> globalFlowSet, Map<FlowGraphNode, Set<Exp>> earliest, Map<FlowGraphNode, Set<Exp>> usedSets) {
        super(flowGraph, Direction.FORWARDS, Meet.INTERSECTION, globalFlowSet);
        this.earliest = earliest;
        this.usedSets = usedSets;
    }
    

    @Override
    public Set<Exp> transferFunction(FlowGraphNode Node, Set<Exp> inputSet) {
        Set<Exp> resultSet = new HashSet<Exp>();

        resultSet.addAll(earliest.get(Node));
        resultSet.addAll(inputSet);
        resultSet.removeAll(usedSets.get(Node));

        return resultSet;
    }
    
}
