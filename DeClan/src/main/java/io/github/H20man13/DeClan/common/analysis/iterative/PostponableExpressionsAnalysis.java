package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.Config;
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

public class PostponableExpressionsAnalysis extends InstructionAnalysis<HashMap<ICode, HashSet<Tuple<Exp, ICode.Type>>>, HashSet<Tuple<Exp, ICode.Type>>, Tuple<Exp, ICode.Type>> {
    private Map<ICode, Set<Tuple<Exp, ICode.Type>>> usedSets;
    private Map<ICode, Set<Tuple<Exp, ICode.Type>>> earliest;
    private AnticipatedExpressionsAnalysis anticipated;
    
    @SuppressWarnings("unchecked")
	public PostponableExpressionsAnalysis(FlowGraph flowGraph, Set<Tuple<Exp, ICode.Type>> globalFlowSet, Map<ICode, Set<Tuple<Exp, ICode.Type>>> earliest, Map<ICode, Set<Tuple<Exp, ICode.Type>>> usedSets, AnticipatedExpressionsAnalysis anticipated, Config cfg) {
        super(flowGraph, Direction.FORWARDS, Meet.INTERSECTION, globalFlowSet, true, cfg, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));
        this.earliest = earliest;
        this.usedSets = usedSets;
        this.anticipated = anticipated;
    }
    

    @Override
    public HashSet<Tuple<Exp, ICode.Type>> transferFunction(ICode instruction, HashSet<Tuple<Exp, ICode.Type>> inputSet) {
        HashSet<Tuple<Exp, ICode.Type>> resultSet = newSet();

        Set<Tuple<Exp, ICode.Type>> earliest = this.earliest.get(instruction);
        Set<Tuple<Exp, ICode.Type>> used = usedSets.get(instruction);
        Set<Tuple<Exp, ICode.Type>> antOut = this.anticipated.getOutputSet(instruction);
        resultSet.addAll(earliest);
        resultSet.addAll(inputSet);
        
        HashSet<Tuple<Exp, ICode.Type>> toRemove = newSet();
        toRemove.addAll(used);
        toRemove.removeAll(antOut);
        
        resultSet.removeAll(toRemove);

        return resultSet;
    }
}
