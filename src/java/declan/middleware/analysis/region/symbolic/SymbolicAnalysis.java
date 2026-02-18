package declan.middleware.analysis.region.symbolic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import declan.utils.CopyStr;
import declan.utils.Tuple;
import declan.middleware.analysis.region.RegionAnalysis;
import declan.middleware.analysis.region.RegionAnalysis.LoopStrategy;
import declan.middleware.analysis.region.expr.Expr;
import declan.middleware.analysis.region.expr.IntExpr;
import declan.middleware.analysis.region.expr.NaaExpr;
import declan.middleware.analysis.region.expr.OpExpr;
import declan.middleware.analysis.region.expr.RefVar;
import declan.middleware.analysis.region.function.Closure;
import declan.middleware.analysis.region.function.FunctionApplication;
import declan.middleware.analysis.region.function.InputParamater;
import declan.middleware.analysis.region.function.RegionTransferFunction;
import declan.middleware.analysis.region.function.SetExpression;
import declan.middleware.icode.Assign;
import declan.middleware.icode.Def;
import declan.middleware.icode.ICode;
import declan.middleware.icode.exp.BinExp;
import declan.middleware.icode.exp.BoolExp;
import declan.middleware.icode.exp.Exp;
import declan.middleware.icode.exp.IdentExp;
import declan.middleware.icode.exp.IntExp;
import declan.middleware.icode.exp.RealExp;
import declan.middleware.icode.exp.StrExp;
import declan.middleware.icode.exp.UnExp;
import declan.middleware.region.InstructionRegion;
import declan.middleware.region.RegionBase;
import declan.middleware.region.RegionGraph;
import declan.utils.ConversionUtils;

public class SymbolicAnalysis extends RegionAnalysis<Tuple<CopyStr, Expr>> {
	private Map<RegionBase, Set<Tuple<CopyStr, Expr>>> genSets;
	private Map<RegionBase, Set<String>> killSets;
	private SymbolicAnalysisTransferFunctionFactory factory;
	
	public SymbolicAnalysis(RegionGraph regionGraph, Direction direction, LoopStrategy strat) {
		super(regionGraph, direction, strat);
		this.genSets = new HashMap<RegionBase, Set<Tuple<CopyStr, Expr>>>();
		this.killSets = new HashMap<RegionBase, Set<String>>();
		for(RegionBase base: regionGraph) {
			if(base instanceof InstructionRegion) {
				InstructionRegion instrReg = (InstructionRegion)base;
				ICode instruction  = (ICode)instrReg.instruction;
				Set<Tuple<CopyStr, Expr>> toGen = new HashSet<Tuple<CopyStr, Expr>>();
				Set<String> toKill = new HashSet<String>();
				
				if(instruction instanceof Assign) {
					Assign assign = (Assign)instruction;
					Assign.Scope scope = assign.getScope();
					if(scope == Assign.Scope.RETURN || scope == Assign.Scope.PARAM) {
						NaaExpr naaExp = new NaaExpr();
						Tuple<CopyStr, Expr> tup3 = new Tuple<CopyStr, Expr>(ConversionUtils.newS(assign.place), naaExp);
						toGen.add(tup3);
					} else if(assign.value instanceof BinExp) {
						BinExp exp = (BinExp)assign.value;
						Expr left = ConversionUtils.toExprFromIdentExp(exp.left);
						Expr right = ConversionUtils.toExprFromIdentExp(exp.right);
						switch(exp.op) {
						case IADD:
							OpExpr bin = new OpExpr(OpExpr.Operator.IPLUS, left, right);
							Tuple<CopyStr, Expr> tup1 = new Tuple<CopyStr, Expr>(ConversionUtils.newS(assign.place), bin);
							toGen.add(tup1);
							break;
						case ISUB:
							OpExpr un = new OpExpr(OpExpr.Operator.IMINUS, left, right);
							Tuple<CopyStr, Expr> tup2 = new Tuple<CopyStr, Expr>(ConversionUtils.newS(assign.place), un);
							toGen.add(tup2);
							break;
						default:
							NaaExpr naaExp = new NaaExpr();
							Tuple<CopyStr, Expr> tup3 = new Tuple<CopyStr, Expr>(ConversionUtils.newS(assign.place), naaExp);
							toGen.add(tup3);
							break;
						}
					} else if(assign.value instanceof UnExp) {
						UnExp exp1 = (UnExp)assign.value;
						Expr right = ConversionUtils.toExprFromIdentExp(exp1.right);
						switch(exp1.op){
						default:
							NaaExpr naaExp = new NaaExpr();
							Tuple<CopyStr, Expr> tup2 = new Tuple<CopyStr, Expr>(ConversionUtils.newS(assign.place), naaExp);
							toGen.add(tup2);
							break;
						}
					} else if(assign.value instanceof IdentExp) {
						IdentExp ident = (IdentExp)assign.value;
						RefVar var = new RefVar(ident.ident);
						Tuple<CopyStr, Expr> tup3 = new Tuple<CopyStr, Expr>(ConversionUtils.newS(assign.place), var);
						toGen.add(tup3);
					} else if(assign.value instanceof IntExp) {
						IntExp ident = (IntExp)assign.value;
						IntExpr var = new IntExpr(ident.value);
						Tuple<CopyStr, Expr> tup3 = new Tuple<CopyStr, Expr>(ConversionUtils.newS(assign.place), var);
						toGen.add(tup3);
					} else {
						NaaExpr naaExp = new NaaExpr();
						Tuple<CopyStr, Expr> tup3 = new Tuple<CopyStr, Expr>(ConversionUtils.newS(assign.place), naaExp);
						toGen.add(tup3);
					}
				} else if(instruction instanceof Def) {
					Def assign = (Def)instruction;
					Assign.Scope scope = assign.scope;
					if(scope == Assign.Scope.RETURN || scope == Assign.Scope.PARAM) {
						NaaExpr naaExp = new NaaExpr();
						Tuple<CopyStr, Expr> tup3 = new Tuple<CopyStr, Expr>(ConversionUtils.newS(assign.label), naaExp);
						toGen.add(tup3);
					} else if(assign.val instanceof BinExp) {
						BinExp exp = (BinExp)assign.val;
						Expr left = ConversionUtils.toExprFromIdentExp(exp.left);
						Expr right = ConversionUtils.toExprFromIdentExp(exp.right);
						switch(exp.op) {
						case IADD:
							OpExpr bin = new OpExpr(OpExpr.Operator.IPLUS, left, right);
							Tuple<CopyStr, Expr> tup1 = new Tuple<CopyStr, Expr>(ConversionUtils.newS(assign.label), bin);
							toGen.add(tup1);
							break;
						case ISUB:
							OpExpr un = new OpExpr(OpExpr.Operator.IMINUS, left, right);
							Tuple<CopyStr, Expr> tup2 = new Tuple<CopyStr, Expr>(ConversionUtils.newS(assign.label), un);
							toGen.add(tup2);
							break;
						default:
							NaaExpr naaExp = new NaaExpr();
							Tuple<CopyStr, Expr> tup3 = new Tuple<CopyStr, Expr>(ConversionUtils.newS(assign.label), naaExp);
							toGen.add(tup3);
							break;
						}
					} else if(assign.val instanceof UnExp) {
						UnExp exp1 = (UnExp)assign.val;
						Expr right = ConversionUtils.toExprFromIdentExp(exp1.right);
						switch(exp1.op){
						default:
							NaaExpr naaExp = new NaaExpr();
							Tuple<CopyStr, Expr> tup2 = new Tuple<CopyStr, Expr>(ConversionUtils.newS(assign.label), naaExp);
							toGen.add(tup2);
							break;
						}
					} else if(assign.val instanceof IdentExp) {
						IdentExp ident = (IdentExp)assign.val;
						RefVar var = new RefVar(ident.ident);
						Tuple<CopyStr, Expr> tup3 = new Tuple<CopyStr, Expr>(ConversionUtils.newS(assign.label), var);
						toGen.add(tup3);
					} else if(assign.val instanceof IntExp) {
						IntExp ident = (IntExp)assign.val;
						IntExpr var = new IntExpr(ident.value);
						Tuple<CopyStr, Expr> tup3 = new Tuple<CopyStr, Expr>(ConversionUtils.newS(assign.label), var);
						toGen.add(tup3);
					} else {
						NaaExpr naaExp = new NaaExpr();
						Tuple<CopyStr, Expr> tup3 = new Tuple<CopyStr, Expr>(ConversionUtils.newS(assign.label), naaExp);
						toGen.add(tup3);
					}
				}
				this.genSets.put(instrReg, toGen);
				this.killSets.put(instrReg, toKill);
			}
		}
	}

	@Override
	protected Closure<Tuple<CopyStr, Expr>> closureOfFunction(RegionBase reg,
			RegionTransferFunction<Tuple<CopyStr, Expr>> input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected RegionTransferFunction<Tuple<CopyStr, Expr>> compositionOfFunctions(RegionTransferFunction<Tuple<CopyStr, Expr>> func1, RegionTransferFunction<Tuple<CopyStr, Expr>> func2) {
		FunctionApplication<Tuple<CopyStr, Expr>> exp = factory.producefunctionApplication(func1, func2);
		return factory.produceRegionTransferFunction(exp);
	}

	@Override
	protected RegionTransferFunction<Tuple<CopyStr, Expr>> meetOfFunctions(RegionTransferFunction<Tuple<CopyStr, Expr>> exp1, RegionTransferFunction<Tuple<CopyStr, Expr>> exp2) {
		SetExpression<Tuple<CopyStr, Expr>> left = exp1.getExpression();
		SetExpression<Tuple<CopyStr, Expr>> right = exp2.getExpression();
		SymbolicMeetOperator op = factory.produceSymbolicAnalysisMeetOperator(left, right);
		return factory.produceRegionTransferFunction(op);
	}

	@Override
	protected RegionTransferFunction<Tuple<CopyStr, Expr>> transferFunction(RegionBase region) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected RegionTransferFunction<Tuple<CopyStr, Expr>> identityFunction() {
		InputParamater<Tuple<CopyStr, Expr>> param = factory.produceParamater();
		return factory.produceRegionTransferFunction(param);
	}
}
