package io.github.H20man13.DeClan.common.analysis.region;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.analysis.region.expr.BinExpr;
import io.github.H20man13.DeClan.common.analysis.region.expr.Expr;
import io.github.H20man13.DeClan.common.analysis.region.expr.IntExpr;
import io.github.H20man13.DeClan.common.analysis.region.expr.NaaExpr;
import io.github.H20man13.DeClan.common.analysis.region.expr.RefVar;
import io.github.H20man13.DeClan.common.analysis.region.expr.UnExpr;
import io.github.H20man13.DeClan.common.analysis.region.function.Closure;
import io.github.H20man13.DeClan.common.analysis.region.function.RegionTransferFunction;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.region.InstructionRegion;
import io.github.H20man13.DeClan.common.region.RegionBase;
import io.github.H20man13.DeClan.common.region.RegionGraph;
import io.github.H20man13.DeClan.common.util.ConversionUtils;

public class SymbolicAnalysis extends RegionAnalysis<Tuple<String, Expr>> {
	Map<ICode, Set<Tuple<String, Expr>>> genSets;
	Map<ICode, Set<Tuple<String, Expr>>> killSets;
	
	public SymbolicAnalysis(RegionGraph regionGraph, Direction direction, LoopStrategy strat) {
		super(regionGraph, direction, strat);
		this.genSets = new HashMap<ICode, Set<Tuple<String, Expr>>>();
		this.killSets = new HashMap<ICode, Set<Tuple<String, Expr>>>();
		for(RegionBase base: regionGraph) {
			if(base instanceof InstructionRegion) {
				InstructionRegion instrReg = (InstructionRegion)base;
				ICode instruction  = (ICode)instrReg;
				Set<Tuple<String, Expr>> toGen = new HashSet<Tuple<String, Expr>>();
				Set<Tuple<String, Expr>> toKill = new HashSet<Tuple<String, Expr>>();
				
				if(instruction instanceof Assign) {
					Assign assign = (Assign)instruction;
					Assign.Scope scope = assign.getScope();
					if(scope == Assign.Scope.RETURN || scope == Assign.Scope.PARAM) {
						NaaExpr naaExp = new NaaExpr();
						Tuple<String, Expr> tup3 = new Tuple<String, Expr>(assign.place, naaExp);
						toGen.add(tup3);
					} else if(assign.value instanceof BinExp) {
						BinExp exp = (BinExp)assign.value;
						Expr left = ConversionUtils.toExprFromExp(exp.left);
						Expr right = ConversionUtils.toExprFromExp(exp.right);
						switch(exp.op) {
						case IADD:
							BinExpr bin = new BinExpr(left, BinExpr.Operator.IPLUS, right);
							Tuple<String, Expr> tup1 = new Tuple<String, Expr>(assign.place, bin);
							toGen.add(tup1);
							break;
						case ISUB:
							BinExpr un = new BinExpr(left, BinExpr.Operator.IMINUS, right);
							Tuple<String, Expr> tup2 = new Tuple<String, Expr>(assign.place, un);
							toGen.add(tup2);
							break;
						default:
							NaaExpr naaExp = new NaaExpr();
							Tuple<String, Expr> tup3 = new Tuple<String, Expr>(assign.place, naaExp);
							toGen.add(tup3);
							break;
						}
					} else if(assign.value instanceof UnExp) {
						UnExp exp1 = (UnExp)assign.value;
						Expr right = ConversionUtils.toExprFromExp(exp1);
						switch(exp1.op){
						case INEG:
							UnExpr un = new UnExpr(UnExpr.Operator.INEG, right);
							Tuple<String, Expr> tup1 = new Tuple<String, Expr>(assign.place, un);
							toGen.add(tup1);
							break;
						default:
							NaaExpr naaExp = new NaaExpr();
							Tuple<String, Expr> tup2 = new Tuple<String, Expr>(assign.place, naaExp);
							toGen.add(tup2);
							break;
						}
					} else if(assign.value instanceof IdentExp) {
						IdentExp ident = (IdentExp)assign.value;
						RefVar var = new RefVar(ident.ident);
						Tuple<String, Expr> tup3 = new Tuple<String, Expr>(assign.place, var);
						toGen.add(tup3);
					} else if(assign.value instanceof IntExp) {
						IntExp ident = (IntExp)assign.value;
						IntExpr var = new IntExpr(ident.value);
						Tuple<String, Expr> tup3 = new Tuple<String, Expr>(assign.place, var);
						toGen.add(tup3);
					} else {
						NaaExpr naaExp = new NaaExpr();
						Tuple<String, Expr> tup3 = new Tuple<String, Expr>(assign.place, naaExp);
						toGen.add(tup3);
					}
				} else if(instruction instanceof Def) {
					Def assign = (Def)instruction;
					Assign.Scope scope = assign.scope;
					if(scope == Assign.Scope.RETURN || scope == Assign.Scope.PARAM) {
						NaaExpr naaExp = new NaaExpr();
						Tuple<String, Expr> tup3 = new Tuple<String, Expr>(assign.label, naaExp);
						toGen.add(tup3);
					} else if(assign.val instanceof BinExp) {
						BinExp exp = (BinExp)assign.val;
						Expr left = ConversionUtils.toExprFromExp(exp.left);
						Expr right = ConversionUtils.toExprFromExp(exp.right);
						switch(exp.op) {
						case IADD:
							BinExpr bin = new BinExpr(left, BinExpr.Operator.IPLUS, right);
							Tuple<String, Expr> tup1 = new Tuple<String, Expr>(assign.label, bin);
							toGen.add(tup1);
							break;
						case ISUB:
							BinExpr un = new BinExpr(left, BinExpr.Operator.IMINUS, right);
							Tuple<String, Expr> tup2 = new Tuple<String, Expr>(assign.label, un);
							toGen.add(tup2);
							break;
						default:
							NaaExpr naaExp = new NaaExpr();
							Tuple<String, Expr> tup3 = new Tuple<String, Expr>(assign.label, naaExp);
							toGen.add(tup3);
							break;
						}
					} else if(assign.val instanceof UnExp) {
						UnExp exp1 = (UnExp)assign.val;
						Expr right = ConversionUtils.toExprFromExp(exp1);
						switch(exp1.op){
						case INEG:
							UnExpr un = new UnExpr(UnExpr.Operator.INEG, right);
							Tuple<String, Expr> tup1 = new Tuple<String, Expr>(assign.label, un);
							toGen.add(tup1);
							break;
						default:
							NaaExpr naaExp = new NaaExpr();
							Tuple<String, Expr> tup2 = new Tuple<String, Expr>(assign.label, naaExp);
							toGen.add(tup2);
							break;
						}
					} else if(assign.val instanceof IdentExp) {
						IdentExp ident = (IdentExp)assign.val;
						RefVar var = new RefVar(ident.ident);
						Tuple<String, Expr> tup3 = new Tuple<String, Expr>(assign.label, var);
						toGen.add(tup3);
					} else if(assign.val instanceof IntExp) {
						IntExp ident = (IntExp)assign.val;
						IntExpr var = new IntExpr(ident.value);
						Tuple<String, Expr> tup3 = new Tuple<String, Expr>(assign.label, var);
						toGen.add(tup3);
					} else {
						NaaExpr naaExp = new NaaExpr();
						Tuple<String, Expr> tup3 = new Tuple<String, Expr>(assign.label, naaExp);
						toGen.add(tup3);
					}
				}
				this.genSets.put(instruction, toGen);
				this.killSets.put(instruction, toKill);
			}
		}
	}

	@Override
	protected Closure<Tuple<String, Expr>> closureOfFunction(RegionBase reg,
			RegionTransferFunction<Tuple<String, Expr>> input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected RegionTransferFunction<Tuple<String, Expr>> compositionOfFunctions(
		RegionTransferFunction<Tuple<String, Expr>> func1, RegionTransferFunction<Tuple<String, Expr>> func2) {
		return null;
	}

	@Override
	protected RegionTransferFunction<Tuple<String, Expr>> meetOfFunctions(
			RegionTransferFunction<Tuple<String, Expr>> exp1, RegionTransferFunction<Tuple<String, Expr>> exp2) {
		return null;
	}

	@Override
	protected RegionTransferFunction<Tuple<String, Expr>> transferFunction(RegionBase region) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected RegionTransferFunction<Tuple<String, Expr>> identityFunction() {
		return null;
	}
}
