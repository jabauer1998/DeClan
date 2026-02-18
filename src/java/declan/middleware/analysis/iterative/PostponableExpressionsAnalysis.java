package declan.middleware.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import declan.driver.Config;
import declan.utils.Tuple;
import declan.utils.flow.BlockNode;
import declan.utils.flow.FlowGraph;
import declan.utils.flow.FlowGraphNode;
import declan.middleware.icode.ICode;
import declan.middleware.icode.exp.BinExp;
import declan.middleware.icode.exp.BoolExp;
import declan.middleware.icode.exp.Exp;
import declan.middleware.icode.exp.IdentExp;
import declan.middleware.icode.exp.IntExp;
import declan.middleware.icode.exp.NullableExp;
import declan.middleware.icode.exp.RealExp;
import declan.middleware.icode.exp.StrExp;
import declan.middleware.icode.exp.UnExp;
import declan.utils.Utils;

public class PostponableExpressionsAnalysis extends InstructionAnalysis<HashMap<ICode, HashSet<Tuple<NullableExp, ICode.Type>>>, HashSet<Tuple<NullableExp, ICode.Type>>, Tuple<NullableExp, ICode.Type>> {
    private Map<ICode, Set<Tuple<NullableExp, ICode.Type>>> usedSets;
    private Map<ICode, Set<Tuple<NullableExp, ICode.Type>>> earliest;
    private AnticipatedExpressionsAnalysis anticipated;
    
    @SuppressWarnings("unchecked")
	public PostponableExpressionsAnalysis(FlowGraph flowGraph, HashSet<Tuple<NullableExp, ICode.Type>> globalFlowSet, Map<ICode, Set<Tuple<NullableExp, ICode.Type>>> earliest, Map<ICode, Set<Tuple<NullableExp, ICode.Type>>> usedSets, AnticipatedExpressionsAnalysis anticipated, Config cfg) {
        super(flowGraph, Direction.FORWARDS, Meet.INTERSECTION, globalFlowSet, true, cfg, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));
        this.earliest = earliest;
        this.usedSets = usedSets;
        this.anticipated = anticipated;
    }
    

    @Override
    public HashSet<Tuple<NullableExp, ICode.Type>> transferFunction(ICode instruction, HashSet<Tuple<NullableExp, ICode.Type>> inputSet) {
        HashSet<Tuple<NullableExp, ICode.Type>> resultSet = newSet();

        Set<Tuple<NullableExp, ICode.Type>> earliest = this.earliest.get(instruction);
        Set<Tuple<NullableExp, ICode.Type>> used = usedSets.get(instruction);
        Set<Tuple<NullableExp, ICode.Type>> antOut = this.anticipated.getOutputSet(instruction);
        resultSet.addAll(earliest);
        resultSet.addAll(inputSet);
        
        HashSet<Tuple<NullableExp, ICode.Type>> toRemove = newSet();
        toRemove.addAll(used);
        toRemove.removeAll(antOut);
        
        resultSet.removeAll(toRemove);

        return resultSet;
    }
}
