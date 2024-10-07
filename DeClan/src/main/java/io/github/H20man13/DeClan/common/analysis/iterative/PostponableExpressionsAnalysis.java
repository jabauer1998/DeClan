package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.util.Utils;

public class PostponableExpressionsAnalysis extends InstructionAnalysis<Tuple<Exp, ICode.Type>> {
    private Map<ICode, Set<Tuple<Exp, ICode.Type>>> usedSets;
    private Map<ICode, Set<Tuple<Exp, ICode.Type>>> earliest;
    
    public PostponableExpressionsAnalysis(FlowGraph flowGraph, Set<Tuple<Exp, ICode.Type>> globalFlowSet, Map<ICode, Set<Tuple<Exp, ICode.Type>>> earliest, Map<ICode, Set<Tuple<Exp, ICode.Type>>> usedSets) {
        super(flowGraph, Direction.FORWARDS, Meet.INTERSECTION, globalFlowSet);
        this.earliest = earliest;
        this.usedSets = usedSets;
    }
    

    @Override
    public Set<Tuple<Exp, ICode.Type>> transferFunction(ICode instruction, Set<Tuple<Exp, ICode.Type>> inputSet) {
        Set<Tuple<Exp, ICode.Type>> resultSet = new HashSet<Tuple<Exp, ICode.Type>>();

        resultSet.addAll(earliest.get(instruction));
        resultSet.addAll(inputSet);
        resultSet.removeAll(usedSets.get(instruction));

        return resultSet;
    }
}
