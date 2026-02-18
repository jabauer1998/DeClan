package io.github.h20man13.DeClan.common.builder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.github.h20man13.DeClan.common.Tuple;
import io.github.h20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.h20man13.DeClan.common.icode.Call;
import io.github.h20man13.DeClan.common.icode.Def;
import io.github.h20man13.DeClan.common.icode.Goto;
import io.github.h20man13.DeClan.common.icode.ICode;
import io.github.h20man13.DeClan.common.icode.If;
import io.github.h20man13.DeClan.common.icode.Return;
import io.github.h20man13.DeClan.common.icode.ICode.Scope;
import io.github.h20man13.DeClan.common.icode.exp.BinExp;
import io.github.h20man13.DeClan.common.icode.exp.BoolExp;
import io.github.h20man13.DeClan.common.icode.exp.Exp;
import io.github.h20man13.DeClan.common.icode.exp.IdentExp;
import io.github.h20man13.DeClan.common.icode.exp.IntExp;
import io.github.h20man13.DeClan.common.icode.exp.NullableExp;
import io.github.h20man13.DeClan.common.icode.exp.RealExp;
import io.github.h20man13.DeClan.common.icode.inline.Inline;
import io.github.h20man13.DeClan.common.icode.inline.InlineParam;
import io.github.h20man13.DeClan.common.icode.label.ProcLabel;
import io.github.h20man13.DeClan.common.icode.label.StandardLabel;
import io.github.h20man13.DeClan.common.icode.symbols.SymEntry;

public abstract class StatementBuilder extends AssignmentBuilder{
    private IrBuilderContext ctx;
    private IrRegisterGenerator gen;
    
    protected StatementBuilder(IrBuilderContext ctx, IrRegisterGenerator gen) {
        super(gen);
        this.ctx = ctx;
        this.gen = gen;
    }

    public void incrimentForLoopLevel(){
        ctx.incrimentForLoopLevel();
    }

    public void deIncrimentForLoopLevel(){
        ctx.deIncrimentForLoopLevel();
    }
    
    public void buildInductionBasedForLoopBeginning(IdentExp curValueInduction, IdentExp target, IdentExp initIdent, Object startingAtThis, Object byThis) {
    	buildForLoopBeginning(curValueInduction, BinExp.Operator.LT, target);
    	if(byThis instanceof Integer) {
    		int byThisInt = (int)byThis;
    		if(byThisInt > 1) {
    			IdentExp byThisMult = buildDefinition(Scope.LOCAL, new IntExp(byThisInt), ICode.Type.INT);
    			if(startingAtThis instanceof Integer) {
    				int startingAtInt = (int)startingAtThis;
    				if(startingAtInt > 0){
    					IdentExp startAdd = buildDefinition(Scope.LOCAL, new IntExp(startingAtInt), ICode.Type.INT);
    					IdentExp byThisMultResult = buildIntegerMultiplicationDefinition(Scope.LOCAL, curValueInduction, byThisMult);
    					this.buildIntegerAdditionAssignment(initIdent.scope, initIdent.ident, byThisMultResult, startAdd);
    				} else if(startingAtInt < 0){
    					IdentExp startSub = buildDefinition(Scope.LOCAL, new IntExp(-startingAtInt), ICode.Type.INT);
    					IdentExp byThisMultResult = buildIntegerMultiplicationDefinition(Scope.LOCAL, curValueInduction, byThisMult);
    					this.buildIntegerSubtractionAssignment(initIdent.scope, initIdent.ident, byThisMultResult, startSub);
    				} else {
    					buildIntegerMultiplicationAssignment(initIdent.scope, initIdent.ident, curValueInduction, byThisMult);
    				}
    			} else if(startingAtThis instanceof Double) {
    				double startingAtDouble = (double)startingAtThis;
    				if(startingAtDouble > 0.0) {
    					IdentExp startDouble = buildDefinition(Scope.LOCAL, new RealExp((float)startingAtDouble), ICode.Type.REAL);
    					IdentExp byThisMultResult = buildIntegerMultiplicationDefinition(Scope.LOCAL, curValueInduction, byThisMult);
    					IdentExp byThisMultResultReal = buildIntToRealConversion(Scope.LOCAL, byThisMultResult);
    					buildRealAdditionAssignment(initIdent.scope, initIdent.ident, byThisMultResultReal, startDouble);
    				} else if(startingAtDouble < 0.0) {
    					IdentExp startDouble = buildDefinition(Scope.LOCAL, new RealExp((float)-startingAtDouble), ICode.Type.REAL);
    					IdentExp byThisMultResult = buildIntegerMultiplicationDefinition(Scope.LOCAL, curValueInduction, byThisMult);
    					IdentExp byThisMultResultReal = buildIntToRealConversion(Scope.LOCAL, byThisMultResult);
    					buildRealSubtractionAssignment(initIdent.scope, initIdent.ident, byThisMultResultReal, startDouble);
    				} else {
    					IdentExp inductionReal = buildIntToRealConversion(ICode.Scope.LOCAL, curValueInduction);
    					IdentExp byThisReal = buildIntToRealConversion(ICode.Scope.LOCAL, byThisMult);
    					buildRealMultiplicationAssignment(initIdent.scope, initIdent.ident, inductionReal, byThisReal);
    				}
    			} else {
    				throw new RuntimeException("Unexpected type of Initial Expression in for statemenbt");
    			}
    		} else if(byThisInt < -1) {
    			IdentExp byThisMult = buildDefinition(Scope.LOCAL, new IntExp(-byThisInt), ICode.Type.INT);
    			byThisMult = buildIntegerNegationDefinition(Scope.LOCAL, byThisMult);
    			if(startingAtThis instanceof Integer) {
    				int startingAtInt = (int)startingAtThis;
    				if(startingAtInt > 0){
    					IdentExp startAdd = buildDefinition(Scope.LOCAL, new IntExp(startingAtInt), ICode.Type.INT);
    					IdentExp byThisMultResult = buildIntegerMultiplicationDefinition(Scope.LOCAL, curValueInduction, byThisMult);
    					buildIntegerAdditionAssignment(initIdent.scope, initIdent.ident, byThisMultResult, startAdd);
    				} else if(startingAtInt < 0){
    					IdentExp startSub = buildDefinition(Scope.LOCAL, new IntExp(-startingAtInt), ICode.Type.INT);
    					IdentExp byThisMultResult = buildIntegerMultiplicationDefinition(Scope.LOCAL, curValueInduction, byThisMult);
    					buildIntegerSubtractionAssignment(initIdent.scope, initIdent.ident, byThisMultResult, startSub);
    				} else {
    					buildIntegerMultiplicationAssignment(initIdent.scope, initIdent.ident, curValueInduction, byThisMult);
    				}
    			} else if(startingAtThis instanceof Double) {
    				double startingAtDouble = (double)startingAtThis;
    				if(startingAtDouble > 0.0) {
    					IdentExp startDouble = buildDefinition(Scope.LOCAL, new RealExp((float)startingAtDouble), ICode.Type.REAL);
    					IdentExp byThisMultResult = buildIntegerMultiplicationDefinition(Scope.LOCAL, curValueInduction, byThisMult);
    					IdentExp byThisMultResultReal = buildIntToRealConversion(Scope.LOCAL, byThisMultResult);
    					buildRealAdditionAssignment(initIdent.scope, initIdent.ident, byThisMultResultReal, startDouble);
    				} else if(startingAtDouble < 0.0) {
    					IdentExp startDouble = buildDefinition(Scope.LOCAL, new RealExp((float)-startingAtDouble), ICode.Type.REAL);
    					IdentExp byThisMultResult = buildIntegerMultiplicationDefinition(Scope.LOCAL, curValueInduction, byThisMult);
    					IdentExp byThisMultResultReal = buildIntToRealConversion(Scope.LOCAL, byThisMultResult);
    					buildRealSubtractionAssignment(initIdent.scope, initIdent.ident, byThisMultResultReal, startDouble);
    				} else {
    					IdentExp inductionReal = buildIntToRealConversion(ICode.Scope.LOCAL, curValueInduction);
    					IdentExp byThisReal = buildIntToRealConversion(ICode.Scope.LOCAL, byThisMult);
    					buildRealMultiplicationAssignment(initIdent.scope, initIdent.ident, inductionReal, byThisReal);
    				}
    			} else {
    				throw new RuntimeException("Unexpected type of Initial Expression in for branch");
    			}    		
    		} else {
    			if(startingAtThis instanceof Integer) {
    				int startingAtInt = (int)startingAtThis;
    				if(startingAtInt > 0){
    					IdentExp startAdd = buildDefinition(Scope.LOCAL, new IntExp(startingAtInt), ICode.Type.INT);
    					buildIntegerAdditionAssignment(initIdent.scope, initIdent.ident, curValueInduction, startAdd);
    				} else if(startingAtInt < 0){
    					IdentExp startSub = buildDefinition(Scope.LOCAL, new IntExp(-startingAtInt), ICode.Type.INT);
    					buildIntegerSubtractionAssignment(initIdent.scope, initIdent.ident, curValueInduction, startSub);
    				}
    			} else if(startingAtThis instanceof Double) {
    				double startingAtDouble = (double)startingAtThis;
    				if(startingAtDouble > 0.0) {
    					IdentExp startDouble = buildDefinition(Scope.LOCAL, new RealExp((float)startingAtDouble), ICode.Type.INT);
    					IdentExp convCurValueInduction = buildIntToRealConversion(Scope.LOCAL, curValueInduction);
    					buildRealAdditionAssignment(initIdent.scope, initIdent.ident, convCurValueInduction, startDouble);
    				} else if(startingAtDouble < 0.0) {
    					IdentExp startDouble = buildDefinition(Scope.LOCAL, new RealExp((float)-startingAtDouble), ICode.Type.INT);
    					IdentExp curValueInductionReal = buildIntToRealConversion(Scope.LOCAL, curValueInduction);
    					buildRealSubtractionAssignment(initIdent.scope, initIdent.ident, curValueInductionReal, startDouble);
    				}
    			} else {
    				throw new RuntimeException("Unexpected type of Initial Expression in for branch");
    			}
    		}
    	} else if(byThis instanceof Double) {
    		double byThisDouble = (double)byThis;
    		if(byThisDouble > 1.0) {
    			IdentExp byThisMult = buildDefinition(Scope.LOCAL, new RealExp((float)byThisDouble), ICode.Type.REAL);
    			if(startingAtThis instanceof Integer) {
    				int startingAtInt = (int)startingAtThis;
    				if(startingAtInt > 0){
    					IdentExp startAdd = buildDefinition(Scope.LOCAL, new IntExp(startingAtInt), ICode.Type.INT);
    					IdentExp realAdd = buildIntToRealConversion(Scope.LOCAL, startAdd);
    					IdentExp realInduction = buildIntToRealConversion(Scope.LOCAL, curValueInduction);
    					IdentExp byThisMultResult = buildRealMultiplication(Scope.LOCAL, realInduction, byThisMult);
    					IdentExp result = this.buildRealAddition(Scope.LOCAL, byThisMultResult, realAdd);
    					result = this.buildRealToIntConversion(Scope.LOCAL, result);
    					this.buildAssignment(initIdent.scope, initIdent.ident, result, ICode.Type.INT);
    				} else if(startingAtInt < 0){
    					IdentExp startSub = buildDefinition(Scope.LOCAL, new IntExp(-startingAtInt), ICode.Type.INT);
    					IdentExp realSub = buildIntToRealConversion(Scope.LOCAL, startSub);
    					IdentExp realInduction = buildIntToRealConversion(Scope.LOCAL, curValueInduction);
    					IdentExp byThisMultResult = buildRealMultiplication(Scope.LOCAL, realInduction, byThisMult);
    					IdentExp result = this.buildRealSubtraction(Scope.LOCAL, byThisMultResult, realSub);
    					result = this.buildRealToIntConversion(Scope.LOCAL, result);
    					this.buildAssignment(initIdent.scope, initIdent.ident, result, ICode.Type.INT);
    				} else {
    					IdentExp realInduction = buildIntToRealConversion(Scope.LOCAL, curValueInduction);
    					IdentExp result = buildRealMultiplication(Scope.LOCAL, realInduction, byThisMult);
    					result = this.buildRealToIntConversion(Scope.LOCAL, result);
    					this.buildAssignment(initIdent.scope, initIdent.ident, result, ICode.Type.INT);
    				}
    			} else if(startingAtThis instanceof Double) {
    				double startingAtDouble = (double)startingAtThis;
    				if(startingAtDouble > 0.0) {
    					IdentExp startDouble = buildDefinition(Scope.LOCAL, new RealExp((float)startingAtDouble), ICode.Type.REAL);
    					IdentExp realInduction = buildIntToRealConversion(Scope.LOCAL, curValueInduction);
    					IdentExp byThisMultResult = buildRealMultiplication(Scope.LOCAL, realInduction, byThisMult);
    					buildRealAdditionAssignment(initIdent.scope, initIdent.ident, byThisMultResult, startDouble);
    				} else if(startingAtDouble < 0.0) {
    					IdentExp startDouble = buildDefinition(Scope.LOCAL, new RealExp((float)-startingAtDouble), ICode.Type.REAL);
    					IdentExp realInduction = buildIntToRealConversion(Scope.LOCAL, curValueInduction);
    					IdentExp byThisMultResult = buildRealMultiplication(Scope.LOCAL, realInduction, byThisMult);
    					buildRealSubtractionAssignment(initIdent.scope, initIdent.ident, byThisMultResult, startDouble);
    				} else {
    					IdentExp inductionReal = buildIntToRealConversion(ICode.Scope.LOCAL, curValueInduction);
    					buildRealMultiplicationAssignment(initIdent.scope, initIdent.ident, inductionReal, byThisMult);
    				}
    			} else {
    				throw new RuntimeException("Unexpected type of Initial Expression in for statemenbt");
    			}
    		} else if(byThisDouble < -1.0) {
    			IdentExp byThisMult = buildDefinition(Scope.LOCAL, new RealExp(-(float)byThisDouble), ICode.Type.REAL);
    			byThisMult = buildRealNegationDefinition(Scope.LOCAL, byThisMult);
    			if(startingAtThis instanceof Integer) {
    				int startingAtInt = (int)startingAtThis;
    				if(startingAtInt > 0){
    					IdentExp startAdd = buildDefinition(Scope.LOCAL, new IntExp(startingAtInt), ICode.Type.INT);
    					IdentExp realInduction = buildIntToRealConversion(ICode.Scope.LOCAL, curValueInduction);
    					IdentExp byThisMultResult = buildRealMultiplication(Scope.LOCAL, realInduction, byThisMult);
    					IdentExp realAddition = buildIntToRealConversion(ICode.Scope.LOCAL, startAdd);
    					IdentExp resultAddition = buildRealAddition(ICode.Scope.LOCAL, byThisMultResult, realAddition);
    					IdentExp resultConversion = buildRealToIntConversion(ICode.Scope.LOCAL, resultAddition);
    					buildAssignment(initIdent.scope, initIdent.ident, resultConversion, ICode.Type.INT);
    				} else if(startingAtInt < 0){
    					IdentExp startSub = buildDefinition(Scope.LOCAL, new IntExp(-startingAtInt), ICode.Type.INT);
    					IdentExp realInduction = buildIntToRealConversion(ICode.Scope.LOCAL, curValueInduction);
    					IdentExp byThisMultResult = buildRealMultiplication(Scope.LOCAL, realInduction, byThisMult);
    					IdentExp realSubtraction = buildIntToRealConversion(ICode.Scope.LOCAL, startSub);
    					IdentExp resultSubtraction = buildRealSubtraction(ICode.Scope.LOCAL, byThisMultResult, realSubtraction);
    					IdentExp resultConversion = buildRealToIntConversion(ICode.Scope.LOCAL, resultSubtraction);
    					buildAssignment(initIdent.scope, initIdent.ident, resultConversion, ICode.Type.INT);
    				} else {
    					IdentExp realInduction = buildIntToRealConversion(ICode.Scope.LOCAL, curValueInduction);
    					IdentExp result = buildRealMultiplication(ICode.Scope.LOCAL, realInduction, byThisMult);
    					IdentExp conversion = buildRealToIntConversion(ICode.Scope.LOCAL, result);
    					buildAssignment(initIdent.scope, initIdent.ident, conversion, ICode.Type.INT);
    				}
    			} else if(startingAtThis instanceof Double) {
    				double startingAtDouble = (double)startingAtThis;
    				if(startingAtDouble > 0.0) {
    					IdentExp startDouble = buildDefinition(Scope.LOCAL, new RealExp((float)startingAtDouble), ICode.Type.REAL);
    					IdentExp realInduction = buildIntToRealConversion(ICode.Scope.LOCAL, curValueInduction);
    					IdentExp byThisMultResult = buildRealMultiplication(Scope.LOCAL, realInduction, byThisMult);
    					buildRealAdditionAssignment(initIdent.scope, initIdent.ident, byThisMultResult, startDouble);
    				} else if(startingAtDouble < 0.0) {
    					IdentExp startDouble = buildDefinition(Scope.LOCAL, new RealExp((float)-startingAtDouble), ICode.Type.REAL);
    					IdentExp realInduction = buildIntToRealConversion(ICode.Scope.LOCAL, curValueInduction);
    					IdentExp byThisMultResult = buildIntegerMultiplicationDefinition(Scope.LOCAL, realInduction, byThisMult);
    					buildRealSubtractionAssignment(initIdent.scope, initIdent.ident, byThisMultResult, startDouble);
    				} else {
    					IdentExp inductionReal = buildIntToRealConversion(ICode.Scope.LOCAL, curValueInduction);
    					buildRealMultiplicationAssignment(initIdent.scope, initIdent.ident, inductionReal, byThisMult);
    				}
    			} else {
    				throw new RuntimeException("Unexpected type of Initial Expression in for branch");
    			}    		
    		} else {
    			if(startingAtThis instanceof Integer) {
    				int startingAtInt = (int)startingAtThis;
    				if(startingAtInt > 0){
    					IdentExp startAdd = buildDefinition(Scope.LOCAL, new IntExp(startingAtInt), ICode.Type.INT);
    					buildIntegerAdditionAssignment(initIdent.scope, initIdent.ident, curValueInduction, startAdd);
    				} else if(startingAtInt < 0){
    					IdentExp startSub = buildDefinition(Scope.LOCAL, new IntExp(-startingAtInt), ICode.Type.INT);
    					buildIntegerSubtractionAssignment(initIdent.scope, initIdent.ident, curValueInduction, startSub);
    				}
    			} else if(startingAtThis instanceof Double) {
    				double startingAtDouble = (double)startingAtThis;
    				if(startingAtDouble > 0.0) {
    					IdentExp startDouble = buildDefinition(Scope.LOCAL, new RealExp((float)startingAtDouble), ICode.Type.REAL);
    					IdentExp convCurValueInduction = buildIntToRealConversion(Scope.LOCAL, curValueInduction);
    					buildRealAdditionAssignment(initIdent.scope, initIdent.ident, convCurValueInduction, startDouble);
    				} else if(startingAtDouble < 0.0) {
    					IdentExp startDouble = buildDefinition(Scope.LOCAL, new RealExp((float)-startingAtDouble), ICode.Type.REAL);
    					IdentExp curValueInductionReal = buildIntToRealConversion(Scope.LOCAL, curValueInduction);
    					buildRealSubtractionAssignment(initIdent.scope, initIdent.ident, curValueInductionReal, startDouble);
    				}
    			} else {
    				throw new RuntimeException("Unexpected type of Initial Expression in for branch");
    			}
    		}
    	} else {
    		throw new RuntimeException("Unexpected type of by expression in for branch");
    	}
	}

    public void buildForLoopBeginning(IdentExp currentValue, BinExp.Operator op, IdentExp target){
        int forLoopNumber = ctx.getForLoopNumber();
        int forLoopLevel = ctx.getForLoopLevel();
        addInstruction(new StandardLabel("FORBEG_" + forLoopNumber + "_LEVEL_" + forLoopLevel));
        BinExp bExp = new BinExp(currentValue, op, target);
        addInstruction(new If(bExp, "FORLOOP_" + forLoopNumber + "_LEVEL_" + forLoopLevel, "FOREND_" + forLoopNumber + "_LEVEL_" + forLoopLevel));
        addInstruction(new StandardLabel("FORLOOP_" + forLoopNumber + "_LEVEL_" + forLoopLevel));
    }

    public void buildForLoopEnd(){
        int forLoopNumber = ctx.getForLoopNumber();
        int forLoopLevel = ctx.getForLoopLevel();
        addInstruction(new Goto("FORBEG_" + forLoopNumber + "_LEVEL_" + forLoopLevel));
        addInstruction(new StandardLabel("FOREND_" + forLoopNumber + "_LEVEL_" + forLoopLevel));
        ctx.incrimentForLoopNumber();
    }

    public void incrimentRepeatLoopLevel(){
        ctx.incrimentRepeatLoopLevel();
    }

    public void deIncrimentRepeatLoopLevel(){
        ctx.deIncrimentRepeatLoopLevel();
    }

    public void buildRepeatLoopBeginning(IdentExp exprResult){
        int repeatLoopLevel = ctx.getRepeatLoopLevel();
        int repeatLoopNumber = ctx.getRepeatLoopNumber();
        BoolExp trueExp = new BoolExp(true);
        String place = gen.genNext();
        addInstruction(new Def(ICode.Scope.LOCAL, place, trueExp, ICode.Type.BOOL));
        BinExp exp = new BinExp(exprResult, BinExp.Operator.BEQ, new IdentExp(ICode.Scope.LOCAL, place));
        addInstruction(new StandardLabel("REPEATBEG_" + repeatLoopNumber + "_LEVEL_" + repeatLoopLevel));
        addInstruction(new If(exp, "REPEATEND_" + repeatLoopNumber + "_LEVEL_" + repeatLoopLevel, "REPEATLOOP_" + repeatLoopNumber + "_LEVEL_" + repeatLoopLevel));
        addInstruction(new StandardLabel("REPEATLOOP_" + repeatLoopNumber + "_LEVEL_" + repeatLoopLevel));
    }

    public void buildRepeatLoopEnd(){
        int repeatLoopLevel = ctx.getRepeatLoopLevel();
        int repeatLoopNumber = ctx.getRepeatLoopNumber();
        addInstruction(new Goto("REPEATBEG_" + repeatLoopNumber + "_LEVEL_" + repeatLoopLevel));
        addInstruction(new StandardLabel("REPEATEND_" + repeatLoopNumber + "_LEVEL_" + repeatLoopLevel));
        ctx.incrimentRepeatLoopNumber();
    }

    public void incrimentIfStatementLevel(){
        ctx.incrimentIfStatementLevel();
    }

    public void deIncrimentIfStatementLevel(){
        ctx.deIncrimentIfStatementLevel();
    }

    public void buildIfStatementBeginning(IdentExp test){
        int ifStatementLevel = ctx.getIfStatementLevel();
        int ifStatementNumber = ctx.getIfStatementNumber();
        int ifStatementSeqNumber = ctx.getIfStatementSeqNumber();
        BoolExp boolExp = new BoolExp(true);
        String place = gen.genNext();
        addInstruction(new Def(ICode.Scope.LOCAL, place, boolExp, ICode.Type.BOOL));
        BinExp exp = new BinExp(test, BinExp.Operator.BEQ, new IdentExp(ICode.Scope.LOCAL, place));
        addInstruction(new If(exp, "IFSTAT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber + "_LEVEL_" + ifStatementLevel, "IFNEXT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber + "_LEVEL_" + ifStatementLevel));
        addInstruction(new StandardLabel("IFSTAT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber + "_LEVEL_" + ifStatementLevel));
    }

    public void buildElseIfStatementBeginning(){
        int ifStatementLevel = ctx.getIfStatementLevel();
        int ifStatementNumber = ctx.getIfStatementNumber();
        int ifStatementSeqNumber = ctx.getIfStatementSeqNumber();
        addInstruction(new Goto("IFEND_" + ifStatementNumber + "_LEVEL_" + ifStatementLevel));
        addInstruction(new StandardLabel("IFNEXT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber + "_LEVEL_" + ifStatementLevel));
        ctx.incrimentIfStatementSeqNumber();
    }

    public void buildIfStatementEnd(){
        int ifStatementLevel = ctx.getIfStatementLevel();
        int ifStatementNumber = ctx.getIfStatementNumber();
        int ifStatementSeqNumber = ctx.getIfStatementSeqNumber();
        addInstruction(new Goto("IFEND_" + ifStatementNumber + "_LEVEL_" + ifStatementLevel));
        addInstruction(new StandardLabel("IFNEXT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber + "_LEVEL_" + ifStatementLevel));
        addInstruction(new StandardLabel("IFEND_" + ifStatementNumber + "_LEVEL_" + ifStatementLevel));
        ctx.incrimentIfStatementNumber();
    }

    public void incrimentWhileLoopLevel(){
        ctx.incrimentWhileLoopLevel();
    }

    public void deIncrimentWhileLoopLevel(){
        ctx.deIncrimentWhileLoopLevel();
    }

    public void buildWhileLoopBeginning(IdentExp test){
        int whileLoopLevel = ctx.getWhileLoopLevel();
        int whileLoopNumber = ctx.getWhileLoopNumber();
        int whileLoopSeqNumber = ctx.getWhileLoopSeqNumber();
        BoolExp trueExp = new BoolExp(true);
        String place = gen.genNext();
        addInstruction(new Def(ICode.Scope.LOCAL, place, trueExp, ICode.Type.BOOL));
        BinExp exp = new BinExp(test, BinExp.Operator.BEQ, new IdentExp(ICode.Scope.LOCAL, place));
        addInstruction(new If(exp, "WHILESTAT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel, "WHILENEXT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
        addInstruction(new StandardLabel("WHILECOND_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
        addInstruction(new If(exp, "WHILESTAT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel, "WHILEEND_" + whileLoopNumber + "_LEVEL_" + whileLoopLevel));
        addInstruction(new StandardLabel("WHILESTAT_" +  whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
    }

    public void buildElseWhileLoopBeginning(){
        int whileLoopNumber = ctx.getWhileLoopNumber();
        int whileLoopLevel = ctx.getWhileLoopLevel();
        int whileLoopSeqNumber = ctx.getWhileLoopSeqNumber();
        addInstruction(new Goto("WHILECOND_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
        addInstruction(new StandardLabel("WHILENEXT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
        ctx.incrimentWhileLoopSeqNumber();
    }

    public void buildWhileLoopEnd(){
        int whileLoopNumber = ctx.getWhileLoopNumber();
        int whileLoopSeqNumber = ctx.getWhileLoopSeqNumber();
        int whileLoopLevel = ctx.getWhileLoopLevel();
        addInstruction(new Goto("WHILECOND_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
        addInstruction(new StandardLabel("WHILENEXT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
        addInstruction(new StandardLabel("WHILEEND_" + whileLoopNumber + "_LEVEL_" + whileLoopLevel));
        ctx.incrimentWhileLoopNumber();
    }

    public void buildLabel(String label){
        addInstruction(new StandardLabel(label));
    }

    public void buildGoto(String label){
        addInstruction(new Goto(label));
    }

    private enum InlineState {
    	BEGIN,
    	SPECIFIER
    }
    
    public void buildInlineAssembly(String inlineAssembly, List<Tuple<NullableExp, ICode.Type>> param){
        List<InlineParam> realParams = new LinkedList<InlineParam>();
        int index = 0;
        int count = 0;
        
        InlineState state = InlineState.BEGIN;
        StringBuilder sb = new StringBuilder();
        while(index < inlineAssembly.length() && count < param.size()) {
        	char c = inlineAssembly.charAt(index);
        	switch (state){
        		case BEGIN:
        			if(c == '%') {
        				state = InlineState.SPECIFIER;
        				sb.append(c);
        				index++;
        				continue;
        			} else {
        				index++;
        				continue;
        			}
        		case SPECIFIER:
        			if(Character.isLetter(c)) {
        				if(Character.toLowerCase(c) == 'd') {
        					state = InlineState.SPECIFIER;
        					sb.append(c);
        					index++;
        					continue;
        				} else if(Character.toLowerCase(c) == 'u') {
        					sb.append(c);
        					index++;
        					continue;
        				} else if(Character.toLowerCase(c) == 'r') {
        					sb.append(c);
        					index++;
        					continue;
        				} else if(Character.toLowerCase(c) == 'a') {
        					sb.append(c);
        					index++;
        					continue;
        				} else {
        					state = InlineState.BEGIN;
        					int mask = 0;
        	        		
        	        		for(int i = 0; i < sb.length(); i++){
        	        			char x = sb.charAt(i);
        	        			if(Character.isLetter(x)) {
	        	        			if(Character.toLowerCase(x) == 'a')
	        	        				mask |= InlineParam.IS_ADDRESS;
	        	        			else if(Character.toLowerCase(x) == 'r')
	        	        				mask |= InlineParam.IS_REGISTER;
	        	        			else if(Character.toLowerCase(x) == 'd')
	        	        				mask |= InlineParam.IS_DEFINITION;
	        	        			else if(Character.toLowerCase(x) == 'u')
	        	        				mask |= InlineParam.IS_USE;
        	        			}
        	        		}
        	        		
        	        		sb = new StringBuilder();
        					realParams.add(new InlineParam(param.get(count), mask));
        					count++;
        					continue;
        				}
        			} else {
        				state = InlineState.BEGIN;
    					int mask = 0;
    	        		
    	        		for(int i = 0; i < sb.length(); i++){
    	        			char x = sb.charAt(i);
    	        			if(Character.isLetter(x)) {
    	        				if(Character.toLowerCase(x) == 'a')
        	        				mask |= InlineParam.IS_ADDRESS;
        	        			else if(Character.toLowerCase(x) == 'r')
        	        				mask |= InlineParam.IS_REGISTER;
        	        			else if(Character.toLowerCase(x) == 'd')
        	        				mask |= InlineParam.IS_DEFINITION;
        	        			else if(Character.toLowerCase(x) == 'u')
        	        				mask |= InlineParam.IS_USE;
    	        			}
    	        			
    	        		}
    	        		
    	        		sb = new StringBuilder();
    					realParams.add(new InlineParam(param.get(count), mask));
    					count++;
    					continue;
        			}
        	}
        }
        
        if(state == InlineState.SPECIFIER && !sb.isEmpty()) {
        	int mask = 0;
    		
    		for(int i = 0; i < sb.length(); i++){
    			char x = sb.charAt(i);
    			if(Character.isLetter(x)) {
	    			if(Character.toLowerCase(x) == 'a')
	    				mask |= InlineParam.IS_ADDRESS;
	    			else if(Character.toLowerCase(x) == 'r')
	    				mask |= InlineParam.IS_REGISTER;
	    			else if(Character.toLowerCase(x) == 'd')
	    				mask |= InlineParam.IS_DEFINITION;
	    			else if(Character.toLowerCase(x) == 'u')
	    				mask |= InlineParam.IS_USE;
    			}
    		}
    		
			realParams.add(new InlineParam(param.get(count), mask));
        }
        
        addInstruction(new Inline(inlineAssembly, realParams));
    }

    public void buildProcedureLabel(String pname){
        addInstruction(new ProcLabel(pname));
    }

    public void buildReturnStatement(String funcName){
        addInstruction(new Return(funcName));
    }

    public void buildExternalProcedureCall(String funcName, List<Tuple<NullableExp, ICode.Type>> args){
        ArrayList<Tuple<NullableExp, ICode.Type>> newArgs = new ArrayList<Tuple<NullableExp, ICode.Type>>();
        newArgs.addAll(args);

        List<Def> newDefs = new LinkedList<Def>();
        for(int i = 0; i < newArgs.size(); i++){
            Tuple<NullableExp, ICode.Type> arg = newArgs.get(i);
            String newPlace;
            if(containsEntry(funcName, i, SymEntry.EXTERNAL | SymEntry.PARAM)){
                newPlace = getVariablePlace(funcName, i, SymEntry.EXTERNAL | SymEntry.PARAM).ident;
            } else {
                newPlace = gen.genNext();
                addVariableEntry(newPlace, SymEntry.EXTERNAL | SymEntry.PARAM, funcName, i, arg.dest);
            }
            newDefs.add(new Def(Scope.PARAM, newPlace, (Exp)arg.source, arg.dest));
        }
        addInstruction(new Call(funcName, newDefs));
    }

    public void buildProcedureCall(String funcName, List<Def> params){
        addInstruction(new Call(funcName, params));
    }
}
