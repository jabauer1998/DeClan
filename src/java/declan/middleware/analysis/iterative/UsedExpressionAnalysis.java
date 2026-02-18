package declan.middleware.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import declan.driver.Config;
import declan.utils.Tuple;
import declan.utils.flow.FlowGraph;
import declan.utils.flow.FlowGraphNode;
import declan.middleware.icode.ICode;
import declan.middleware.icode.exp.Exp;
import declan.middleware.icode.exp.NullableExp;
import declan.utils.Utils;

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
