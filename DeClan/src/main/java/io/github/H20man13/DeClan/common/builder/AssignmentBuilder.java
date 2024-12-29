package io.github.H20man13.DeClan.common.builder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.ErrorLog;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.builder.template.ResetableBuilder;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Call;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.ICode.Scope;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp.Operator;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;

public abstract class AssignmentBuilder extends DefinitionBuilder{
    protected AssignmentBuilder(IrRegisterGenerator gen){
        super(gen);
    }
    
    public void buildAssignment(ICode.Scope scope, String ident, Exp exp, ICode.Type type) {
    	addInstruction(new Assign(scope, ident, exp, type));
    }
    
    public void buildRealToIntConversion(ICode.Scope resultScope, String place, IdentExp input){
    	this.buildUnaryFunctionCallAssignment("RealToInt", resultScope, place, ICode.Type.INT, input, ICode.Type.REAL);
    }
    
    public void buildIntToRealConversion(ICode.Scope resultScope, String place, IdentExp input){
    	this.buildUnaryFunctionCallAssignment("IntToReal", resultScope, place, ICode.Type.REAL, input, ICode.Type.INT);
    }
    
    public void buildRealToBoolConversion(ICode.Scope resultScope, String place, IdentExp input){
    	this.buildUnaryFunctionCallAssignment("RealToBool", resultScope, place, ICode.Type.BOOL, input, ICode.Type.REAL);
    }
    
    public void buildIntToBoolConversion(ICode.Scope resultScope, String place, IdentExp input){
    	this.buildUnaryFunctionCallAssignment("IntToBool", resultScope, place, ICode.Type.BOOL, input, ICode.Type.INT);
    }
    
    public void buildBoolToIntConversion(ICode.Scope resultScope, String place, IdentExp input){
    	this.buildUnaryFunctionCallAssignment("BoolToInt", resultScope, place, ICode.Type.INT, input, ICode.Type.BOOL);
    }
    
    public void buildBoolToRealConversion(ICode.Scope resultScope, String place, IdentExp input){
    	this.buildUnaryFunctionCallAssignment("BoolToReal", resultScope, place, ICode.Type.REAL, input, ICode.Type.BOOL);
    }

    private void buildBinaryAssignment(ICode.Scope scope, String place, IdentExp left, BinExp.Operator op, IdentExp right, ICode.Type type){
        addInstruction(new Assign(scope, place, new BinExp(left, op, right), type));
    }
    
    public void buildIntegerAdditionAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	buildBinaryAssignment(scope, place, left, BinExp.Operator.IADD, right, ICode.Type.INT);
    }
    
    public void buildIntegerSubtractionAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	buildBinaryAssignment(scope, place, left, BinExp.Operator.ISUB, right, ICode.Type.INT);
    }
    
    public void buildIntegerBitwiseAndAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	buildBinaryAssignment(scope, place, left, BinExp.Operator.IAND, right, ICode.Type.INT);
    }
    
    public void buildIntegerBitwiseOrAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	buildBinaryAssignment(scope, place, left, BinExp.Operator.IOR, right, ICode.Type.INT);
    }
    
    public void buildIntegerBitwiseXorAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	buildBinaryAssignment(scope, place, left, BinExp.Operator.IXOR, right, ICode.Type.INT);
    }
    
    public void buildIntegerLeftShiftAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	buildBinaryAssignment(scope, place, left, BinExp.Operator.ILSHIFT, right, ICode.Type.INT);
    }
    
    public void buildIntegerRightShiftAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	buildBinaryAssignment(scope, place, left, BinExp.Operator.IRSHIFT, right, ICode.Type.INT);
    }
    
    public void buildIntegerGreaterThenAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	buildBinaryAssignment(scope, place, left, BinExp.Operator.GT, right, ICode.Type.BOOL);
    }
    
    public void buildIntegerLessThenAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	buildBinaryAssignment(scope, place, left, BinExp.Operator.LT, right, ICode.Type.BOOL);
    }
    
    public void buildIntegerGreaterThenOrEqualToAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	buildBinaryAssignment(scope, place, left, BinExp.Operator.GE, right, ICode.Type.BOOL);
    }
    
    public void buildIntegerLessThenOrEqualToAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	buildBinaryAssignment(scope, place, left, BinExp.Operator.LE, right, ICode.Type.BOOL);
    }
    
    public void buildIntegerEqualToAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	buildBinaryAssignment(scope, place, left, BinExp.Operator.EQ, right, ICode.Type.BOOL);
    }
    
    public void buildIntegerNotEqualToAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	buildBinaryAssignment(scope, place, left, BinExp.Operator.NE, right, ICode.Type.BOOL);
    }
    
    public void buildBooleanOrAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	buildBinaryAssignment(scope, place, left, BinExp.Operator.LOR, right, ICode.Type.BOOL);
    }
    
    public void buildBooleanAndAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	buildBinaryAssignment(scope, place, left, BinExp.Operator.LAND, right, ICode.Type.BOOL);
    }
    
    private void buildBinaryFunctionCall(String funcName, ICode.Scope scope, String place, ICode.Type retType, IdentExp left, ICode.Type leftType, IdentExp right, ICode.Type rightType) {
    	if(containsArgument(funcName, 0, SymEntry.EXTERNAL) && containsArgument(funcName, 1, SymEntry.EXTERNAL)
    	&& containsReturn(funcName, SymEntry.EXTERNAL)) {
    		IdentExp leftPlace = this.getArgumentPlace(funcName, 0, SymEntry.EXTERNAL);
    		IdentExp rightPlace = this.getArgumentPlace(funcName, 1, SymEntry.EXTERNAL);
    		
    		Def leftAssign = new Def(leftPlace.scope, leftPlace.ident, left, leftType);
    		Def rightAssign = new Def(rightPlace.scope, rightPlace.ident, right, rightType);
    		
    		LinkedList<Def> args = new LinkedList<Def>();
    		args.add(leftAssign);
    		args.add(rightAssign);
    		
    		addInstruction(new Call(funcName, args));
    		
    		IdentExp retPlace = this.getReturnPlace(funcName, SymEntry.EXTERNAL);
    		addInstruction(new Def(ICode.Scope.LOCAL, place, retPlace, retType));
    	} else if(containsArgument(funcName, 0, SymEntry.INTERNAL) && containsArgument(funcName, 1, SymEntry.INTERNAL)
    	    	&& containsReturn(funcName, SymEntry.INTERNAL)) {
    		IdentExp leftPlace = this.getArgumentPlace(funcName, 0, SymEntry.INTERNAL);
    		IdentExp rightPlace = this.getArgumentPlace(funcName, 1, SymEntry.INTERNAL);
    		
    		Def leftAssign = new Def(leftPlace.scope, leftPlace.ident, left, leftType);
    		Def rightAssign = new Def(rightPlace.scope, rightPlace.ident, right, rightType);
    		
    		LinkedList<Def> args = new LinkedList<Def>();
    		args.add(leftAssign);
    		args.add(rightAssign);
    		
    		addInstruction(new Call(funcName, args));
    		
    		IdentExp retPlace = this.getReturnPlace(funcName, SymEntry.INTERNAL);
    		
    		addInstruction(new Def(scope, place, retPlace, retType));
    	} else {
    		LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
    		args.add(new Tuple<>(left, leftType));
    		args.add(new Tuple<>(right, rightType));
    		IdentExp result = this.buildExternalFunctionCall(ICode.Scope.LOCAL, funcName, args, retType);
    		addInstruction(new Assign(scope, place, result, retType));
    	}
    }
    
    public void buildIntegerMultiplicationAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	this.buildBinaryFunctionCall("Multiply", scope, place, ICode.Type.INT, left, ICode.Type.INT, right, ICode.Type.INT);
    }
    
    public void buildIntegerModuloAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	this.buildBinaryFunctionCall("Mod", scope, place, ICode.Type.INT, left, ICode.Type.INT, right, ICode.Type.INT);
    }
    
    public void buildIntegerDivideAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	this.buildBinaryFunctionCall("Divide", scope, place, ICode.Type.REAL, left, ICode.Type.INT, right, ICode.Type.INT);
    }
    
    public void buildIntegerDivAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	this.buildBinaryFunctionCall("Div", scope, place, ICode.Type.INT, left, ICode.Type.INT, right, ICode.Type.INT);
    }
    
    public void buildRealAdditionAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	this.buildBinaryFunctionCall("RAdd", scope, place, ICode.Type.REAL, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    public void buildRealSubtractionAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right){
    	this.buildBinaryFunctionCall("RSub", scope, place, ICode.Type.REAL, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    public void buildRealMultiplicationAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	this.buildBinaryFunctionCall("RMul", scope, place, ICode.Type.REAL, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    public void buildRealDivisionAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	this.buildBinaryFunctionCall("RDivide", scope, place, ICode.Type.REAL, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    public void buildRealDivAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	this.buildBinaryFunctionCall("RDiv", scope, place, ICode.Type.INT, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    public void buildRealLessThanAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right){
    	this.buildBinaryFunctionCall("RLessThan", scope, place, ICode.Type.BOOL, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    public void buildRealLessThanOrEqualToAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	this.buildBinaryFunctionCall("RLessThanOrEqualTo", scope, place, ICode.Type.BOOL, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    public void buildRealGreaterThanAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	this.buildBinaryFunctionCall("RGreaterThan", scope, place, ICode.Type.BOOL, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    public void buildRealGreaterThenOrEqualToAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right){
    	this.buildBinaryFunctionCall("RGreaterThanOrEqualTo", scope, place, ICode.Type.BOOL, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    public void buildRNotEqualToAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	this.buildBinaryFunctionCall("RNotEqualTo", scope, place, ICode.Type.BOOL, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    public void buildREqualToAssignment(ICode.Scope scope, String place, IdentExp left, IdentExp right) {
    	this.buildBinaryFunctionCall("REqualTo", scope, place, ICode.Type.BOOL, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    private void buildUnaryAssignment(ICode.Scope scope, String place, UnExp.Operator op, IdentExp right, ICode.Type type){
        addInstruction(new Assign(scope, place, new UnExp(op, right), type));
    }
    
    public void buildBooleanNotAssignment(ICode.Scope scope, String place, IdentExp right) {
    	this.buildUnaryAssignment(scope, place, UnExp.Operator.BNOT, right, ICode.Type.BOOL);
    }
    
    public void  buildIntegerBitwiseNegationAssignment(ICode.Scope scope, String place, IdentExp right) {
    	this.buildUnaryAssignment(scope, place, UnExp.Operator.INOT, right, ICode.Type.INT);
    }
    
    private void buildUnaryFunctionCallAssignment(String funcName, ICode.Scope scope, String place, ICode.Type retType, IdentExp right, ICode.Type rightType) {
    	if(containsArgument(funcName, 0, SymEntry.EXTERNAL) && containsReturn(funcName, SymEntry.EXTERNAL)) {
    		IdentExp rightPlace = this.getArgumentPlace(funcName, 0, SymEntry.EXTERNAL);
    		
    		Def rightAssign = new Def(rightPlace.scope, rightPlace.ident, right, rightType);
    		
    		LinkedList<Def> args = new LinkedList<Def>();
    		args.add(rightAssign);
    		
    		addInstruction(new Call(funcName, args));
    		
    		IdentExp retPlace = this.getReturnPlace(funcName, SymEntry.EXTERNAL);
    		
    		addInstruction(new Def(scope, place, retPlace, retType));
    	} else if(containsArgument(funcName, 0, SymEntry.INTERNAL) && containsReturn(funcName, SymEntry.INTERNAL)) {
    		IdentExp rightPlace = this.getArgumentPlace(funcName, 0, SymEntry.INTERNAL);
    		
    		Def rightAssign = new Def(rightPlace.scope, rightPlace.ident, right, rightType);
    		
    		LinkedList<Def> args = new LinkedList<Def>();
    		args.add(rightAssign);
    		
    		addInstruction(new Call(funcName, args));
    		
    		IdentExp retPlace = this.getReturnPlace(funcName, SymEntry.INTERNAL);
    		
    		addInstruction(new Def(scope, place, retPlace, retType));
    	} else {
    		LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
    		args.add(new Tuple<>(right, rightType));
    		IdentExp retPlace = this.buildExternalFunctionCall(scope, funcName, args, retType);
    		addInstruction(new Assign(scope, place, retPlace, retType));
    	}
    }
    
    public void buildIntegerNegationAssignment(ICode.Scope scope, String place, IdentExp right) {
    	this.buildUnaryFunctionCallAssignment("INeg", scope, place, ICode.Type.INT, right, ICode.Type.INT);
    }
    
    public void buildRealNegationAssignment(ICode.Scope scope, String place, IdentExp right) {
    	this.buildUnaryFunctionCallAssignment("RNeg", scope, place, ICode.Type.REAL, right, ICode.Type.REAL);
    }
}
