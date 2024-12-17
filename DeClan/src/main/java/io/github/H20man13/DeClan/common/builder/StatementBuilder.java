package io.github.H20man13.DeClan.common.builder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.ast.Assignment;
import io.github.H20man13.DeClan.common.ast.Expression;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.Call;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Inline;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.ICode.Scope;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BinExp.Operator;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
import io.github.H20man13.DeClan.common.icode.label.StandardLabel;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;

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
    
    public void buildInductionBasedForLoopBeginning(IdentExp curValueInduction, Operator lt, IdentExp target, IdentExp initIdent, Object startingAtThis, Object byThis) {
    	buildForLoopBeginning(curValueInduction, lt, target);
    	if(byThis instanceof Integer) {
    		int byThisInt = (int)byThis;
    		if(byThisInt > 1) {
    			IdentExp byThisMult = buildDefinition(Scope.LOCAL, new IntExp(byThisInt), ICode.Type.INT);
    			if(startingAtThis instanceof Integer) {
    				int startingAtInt = (int)startingAtThis;
    				if(startingAtInt > 0){
    					IdentExp startAdd = buildDefinition(Scope.LOCAL, new IntExp(startingAtInt), ICode.Type.INT);
    					IdentExp byThisMultResult = buildDefinition(Scope.LOCAL, new BinExp(curValueInduction, BinExp.Operator.IMUL, byThisMult), ICode.Type.INT);
    					buildAssignment(initIdent.scope, initIdent.ident, new BinExp(byThisMultResult, BinExp.Operator.IADD, startAdd), ICode.Type.INT);
    				} else if(startingAtInt < 0){
    					IdentExp startSub = buildDefinition(Scope.LOCAL, new IntExp(-startingAtInt), ICode.Type.INT);
    					IdentExp byThisMultResult = buildDefinition(Scope.LOCAL, new BinExp(curValueInduction, BinExp.Operator.IMUL, byThisMult), ICode.Type.INT);
    					buildAssignment(initIdent.scope, initIdent.ident, new BinExp(byThisMultResult, BinExp.Operator.ISUB, startSub), ICode.Type.INT);
    				} else {
    					buildAssignment(initIdent.scope, initIdent.ident, new BinExp(curValueInduction, BinExp.Operator.IMUL, byThisMult), ICode.Type.INT);
    				}
    			} else if(startingAtThis instanceof Double) {
    				double startingAtDouble = (double)startingAtThis;
    				if(startingAtDouble > 0.0) {
    					
    				} else if(startingAtDouble < 0.0) {
    					
    				} else {
    					
    				}
    			} else {
    				throw new RuntimeException("Unexpected type of Initial Expression in for statemenbt");
    			}
    		} else if(byThisInt < -1) {
    			IdentExp byThisMult = buildDefinition(Scope.LOCAL, new IntExp(-byThisInt), ICode.Type.INT);
    			byThisMult = buildDefinition(Scope.LOCAL, new UnExp(UnExp.Operator.INEG, byThisMult), ICode.Type.INT);
    			if(startingAtThis instanceof Integer) {
    				int startingAtInt = (int)startingAtThis;
    				if(startingAtInt > 0){
    					IdentExp startAdd = buildDefinition(Scope.LOCAL, new IntExp(startingAtInt), ICode.Type.INT);
    					IdentExp byThisMultResult = buildDefinition(Scope.LOCAL, new BinExp(curValueInduction, BinExp.Operator.IMUL, byThisMult), ICode.Type.INT);
    					buildAssignment(initIdent.scope, initIdent.ident, new BinExp(byThisMultResult, BinExp.Operator.IADD, startAdd), ICode.Type.INT);
    				} else if(startingAtInt < 0){
    					IdentExp startSub = buildDefinition(Scope.LOCAL, new IntExp(-startingAtInt), ICode.Type.INT);
    					IdentExp byThisMultResult = buildDefinition(Scope.LOCAL, new BinExp(curValueInduction, BinExp.Operator.IMUL, byThisMult), ICode.Type.INT);
    					buildAssignment(initIdent.scope, initIdent.ident, new BinExp(byThisMultResult, BinExp.Operator.ISUB, startSub), ICode.Type.INT);
    				} else {
    					buildAssignment(initIdent.scope, initIdent.ident, new BinExp(curValueInduction, BinExp.Operator.IMUL, byThisMult), ICode.Type.INT);
    				}
    			} else if(startingAtThis instanceof Double) {
    				double startingAtDouble = (double)startingAtThis;
    				if(startingAtDouble > 0.0) {
    					
    				} else if(startingAtDouble < 0.0) {
    					
    				} else {
    					
    				}
    			} else {
    				throw new RuntimeException("Unexpected type of Initial Expression in for branch");
    			}    		}
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
        BinExp exp = new BinExp(exprResult, BinExp.Operator.EQ, new IdentExp(ICode.Scope.LOCAL, place));
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
        BinExp exp = new BinExp(test, BinExp.Operator.EQ, new IdentExp(ICode.Scope.LOCAL, place));
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
        BinExp exp = new BinExp(test, BinExp.Operator.EQ, new IdentExp(ICode.Scope.LOCAL, place));
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

    public void buildInlineAssembly(String inlineAssembly, List<IdentExp> param){
        addInstruction(new Inline(inlineAssembly, param));
    }

    public void buildProcedureLabel(String pname){
        addInstruction(new ProcLabel(pname));
    }

    public void buildReturnStatement(){
        addInstruction(new Return());
    }

    public void buildExternalProcedureCall(String funcName, List<Tuple<Exp, ICode.Type>> args){
        ArrayList<Tuple<Exp, ICode.Type>> newArgs = new ArrayList<Tuple<Exp, ICode.Type>>();
        newArgs.addAll(args);

        List<Def> newDefs = new LinkedList<Def>();
        for(int i = 0; i < newArgs.size(); i++){
            Tuple<Exp, ICode.Type> arg = newArgs.get(i);
            String newPlace;
            if(containsArgument(funcName, i, SymEntry.EXTERNAL)){
                newPlace = getArgumentPlace(funcName, i, SymEntry.EXTERNAL).ident;
            } else {
                newPlace = gen.genNext();
                addParamEntry(newPlace, SymEntry.EXTERNAL, funcName, i);
            }
            newDefs.add(new Def(Scope.PARAM, newPlace, arg.source, arg.dest));
        }
        addInstruction(new Call(funcName, newDefs));
    }

    public void buildProcedureCall(String funcName, List<Def> params){
        addInstruction(new Call(funcName, params));
    }
}
