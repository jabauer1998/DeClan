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

public class DefinitionBuilder extends SymbolBuilder{
    protected IrRegisterGenerator gen;

    protected DefinitionBuilder(IrRegisterGenerator gen){
        super();
        this.gen = gen;
    }

    public IdentExp buildDefinition(ICode.Scope scope, Exp value, ICode.Type type){
        String place = gen.genNext();
        addInstruction(new Def(scope, place, value, type));
        return new IdentExp(scope, place);
    }
    
    public IdentExp buildRealToIntConversion(ICode.Scope resultScope, IdentExp input){
    	return this.buildUnaryFunctionCall("RealToInt", resultScope, ICode.Type.INT, input, ICode.Type.REAL);
    }
    
    public IdentExp buildIntToRealConversion(ICode.Scope resultScope, IdentExp input){
    	return this.buildUnaryFunctionCall("IntToReal", resultScope, ICode.Type.REAL, input, ICode.Type.INT);
    }
    
    public IdentExp buildRealToBoolConversion(ICode.Scope resultScope, IdentExp input){
    	return this.buildUnaryFunctionCall("RealToBool", resultScope, ICode.Type.BOOL, input, ICode.Type.REAL);
    }
    
    public IdentExp buildIntToBoolConversion(ICode.Scope resultScope, IdentExp input){
    	return this.buildUnaryFunctionCall("IntToBool", resultScope, ICode.Type.BOOL, input, ICode.Type.INT);
    }
    
    public IdentExp buildBoolToIntConversion(ICode.Scope resultScope, IdentExp input){
    	return this.buildUnaryFunctionCall("BoolToInt", resultScope, ICode.Type.INT, input, ICode.Type.BOOL);
    }
    
    public IdentExp buildBoolToRealConversion(ICode.Scope resultScope, IdentExp input){
    	return this.buildUnaryFunctionCall("BoolToReal", resultScope, ICode.Type.REAL, input, ICode.Type.BOOL);
    }

    private IdentExp buildBinaryDefinition(ICode.Scope scope, IdentExp left, BinExp.Operator op,  IdentExp right, ICode.Type type){
        String place = gen.genNext();
        addInstruction(new Def(scope, place, new BinExp(left, op, right), type));
        return new IdentExp(scope, place);
    }
    
    public IdentExp buildIntegerAdditionDefinition(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryDefinition(scope, left, BinExp.Operator.IADD, right, ICode.Type.INT);
    }
    
    public IdentExp buildIntegerSubtractionDefinition(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryDefinition(scope, left, BinExp.Operator.ISUB, right, ICode.Type.INT);
    }
    
    public IdentExp buildIntegerBitwiseAndDefinition(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryDefinition(scope, left, BinExp.Operator.IAND, right, ICode.Type.INT);
    }
    
    public IdentExp buildIntegerBitwiseOrDefinition(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryDefinition(scope, left, BinExp.Operator.IOR, right, ICode.Type.INT);
    }
    
    public IdentExp buildIntegerBitwiseXorDefinition(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryDefinition(scope, left, BinExp.Operator.IXOR, right, ICode.Type.INT);
    }
    
    public IdentExp buildIntegerLeftShiftDefinition(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryDefinition(scope, left, BinExp.Operator.ILSHIFT, right, ICode.Type.INT);
    }
    
    public IdentExp buildIntegerRightShiftDefinition(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryDefinition(scope, left, BinExp.Operator.IRSHIFT, right, ICode.Type.INT);
    }
    
    public IdentExp buildIntegerGreaterThenDefinition(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryDefinition(scope, left, BinExp.Operator.GT, right, ICode.Type.BOOL);
    }
    
    public IdentExp buildIntegerLessThenDefinition(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryDefinition(scope, left, BinExp.Operator.LT, right, ICode.Type.BOOL);
    }
    
    public IdentExp buildIntegerGreaterThenOrEqualToDefinition(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryDefinition(scope, left, BinExp.Operator.GE, right, ICode.Type.BOOL);
    }
    
    public IdentExp buildIntegerLessThenOrEqualToDefinition(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryDefinition(scope, left, BinExp.Operator.LE, right, ICode.Type.BOOL);
    }
    
    public IdentExp buildIntegerEqualToDefinition(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryDefinition(scope, left, BinExp.Operator.EQ, right, ICode.Type.BOOL);
    }
    
    public IdentExp buildIntegerNotEqualToDefinition(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryDefinition(scope, left, BinExp.Operator.NE, right, ICode.Type.BOOL);
    }
    
    public IdentExp buildBooleanOrDefinition(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryDefinition(scope, left, BinExp.Operator.LOR, right, ICode.Type.BOOL);
    }
    
    public IdentExp buildBooleanAndDefinition(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryDefinition(scope, left, BinExp.Operator.LAND, right, ICode.Type.BOOL);
    }
    
    private IdentExp buildBinaryFunctionCall(String funcName, ICode.Scope scope, ICode.Type retType, IdentExp left, ICode.Type leftType, IdentExp right, ICode.Type rightType) {
    	if(containsEntry(funcName, 0, SymEntry.EXTERNAL | SymEntry.PARAM) && containsEntry(funcName, 1, SymEntry.EXTERNAL | SymEntry.PARAM)
    	&& containsEntry(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolBuilderSearchStrategy.SEARCH_VIA_FUNC_NAME)) {
    		IdentExp leftPlace = this.getVariablePlace(funcName, 0, SymEntry.EXTERNAL | SymEntry.PARAM);
    		IdentExp rightPlace = this.getVariablePlace(funcName, 1, SymEntry.EXTERNAL | SymEntry.PARAM);
    		
    		Def leftAssign = new Def(leftPlace.scope, leftPlace.ident, left, leftType);
    		Def rightAssign = new Def(rightPlace.scope, rightPlace.ident, right, rightType);
    		
    		LinkedList<Def> args = new LinkedList<Def>();
    		args.add(leftAssign);
    		args.add(rightAssign);
    		
    		addInstruction(new Call(funcName, args));
    		
    		IdentExp retPlace = this.getVariablePlace(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolBuilderSearchStrategy.SEARCH_VIA_FUNC_NAME);
    		
    		String newPlace = gen.genNext();
    		addInstruction(new Def(ICode.Scope.LOCAL, newPlace, retPlace, retType));
    		
    		return new IdentExp(ICode.Scope.LOCAL, newPlace);
    	} else if(containsEntry(funcName, 0, SymEntry.INTERNAL | SymEntry.PARAM) && containsEntry(funcName, 1, SymEntry.INTERNAL | SymEntry.PARAM)
    	    	&& containsEntry(funcName, SymEntry.INTERNAL | SymEntry.RETURN, SymbolBuilderSearchStrategy.SEARCH_VIA_FUNC_NAME)) {
    		IdentExp leftPlace = this.getVariablePlace(funcName, 0, SymEntry.INTERNAL | SymEntry.PARAM);
    		IdentExp rightPlace = this.getVariablePlace(funcName, 1, SymEntry.INTERNAL | SymEntry.PARAM);
    		
    		Def leftAssign = new Def(leftPlace.scope, leftPlace.ident, left, leftType);
    		Def rightAssign = new Def(rightPlace.scope, rightPlace.ident, right, rightType);
    		
    		LinkedList<Def> args = new LinkedList<Def>();
    		args.add(leftAssign);
    		args.add(rightAssign);
    		
    		addInstruction(new Call(funcName, args));
    		
    		IdentExp retPlace = this.getVariablePlace(funcName, SymEntry.INTERNAL | SymEntry.RETURN, SymbolBuilderSearchStrategy.SEARCH_VIA_FUNC_NAME);
    		
    		String newPlace = gen.genNext();
    		addInstruction(new Def(scope, newPlace, retPlace, retType));
    		
    		return new IdentExp(scope, newPlace);
    	} else {
    		LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
    		args.add(new Tuple<>(left, leftType));
    		args.add(new Tuple<>(right, rightType));
    		return this.buildExternalFunctionCall(scope, funcName, args, retType);
    	}
    }
    
    public IdentExp buildIntegerMultiplicationDefinition(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryFunctionCall("Multiply", scope, ICode.Type.INT, left, ICode.Type.INT, right, ICode.Type.INT);
    }
    
    public IdentExp buildIntegerModulo(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryFunctionCall("Mod", scope, ICode.Type.INT, left, ICode.Type.INT, right, ICode.Type.INT);
    }
    
    public IdentExp buildIntegerDivide(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryFunctionCall("Divide", scope, ICode.Type.REAL, left, ICode.Type.INT, right, ICode.Type.INT);
    }
    
    public IdentExp buildIntegerDiv(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryFunctionCall("Div", scope, ICode.Type.INT, left, ICode.Type.INT, right, ICode.Type.INT);
    }
    
    public IdentExp buildRealAddition(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryFunctionCall("RAdd", scope, ICode.Type.REAL, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    public IdentExp buildRealSubtraction(ICode.Scope scope, IdentExp left, IdentExp right){
    	return this.buildBinaryFunctionCall("RSub", scope, ICode.Type.REAL, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    public IdentExp buildRealMultiplication(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryFunctionCall("RMul", scope, ICode.Type.REAL, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    public IdentExp buildRealDivision(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryFunctionCall("RDivide", scope, ICode.Type.REAL, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    public IdentExp buildRealDiv(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryFunctionCall("RDiv", scope, ICode.Type.INT, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    public IdentExp buildRealLessThan(ICode.Scope scope, IdentExp left, IdentExp right){
    	return this.buildBinaryFunctionCall("RLessThan", scope, ICode.Type.BOOL, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    public IdentExp buildRealLessThanOrEqualTo(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryFunctionCall("RLessThanOrEqualTo", scope, ICode.Type.BOOL, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    public IdentExp buildRealGreaterThan(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryFunctionCall("RGreaterThan", scope, ICode.Type.BOOL, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    public IdentExp buildRealGreaterThenOrEqualTo(ICode.Scope scope, IdentExp left, IdentExp right){
    	return this.buildBinaryFunctionCall("RGreaterThanOrEqualTo", scope, ICode.Type.BOOL, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    public IdentExp buildRNotEqualTo(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryFunctionCall("RNotEqualTo", scope, ICode.Type.BOOL, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }
    
    public IdentExp buildREqualTo(ICode.Scope scope, IdentExp left, IdentExp right) {
    	return this.buildBinaryFunctionCall("REqualTo", scope, ICode.Type.BOOL, left, ICode.Type.REAL, right, ICode.Type.REAL);
    }

    private IdentExp buildUnaryDefinition(ICode.Scope scope, UnExp.Operator op, IdentExp right, ICode.Type type){
        String place = gen.genNext();
        addInstruction(new Def(scope, place, new UnExp(op, right), type));
        return new IdentExp(scope, place);
    }
    
    public IdentExp buildBooleanNotDefinition(ICode.Scope scope, IdentExp right) {
    	return this.buildUnaryDefinition(scope, UnExp.Operator.BNOT, right, ICode.Type.BOOL);
    }
    
    public IdentExp buildIntegerBitwiseNegationDefinition(ICode.Scope scope, IdentExp right) {
    	return this.buildUnaryDefinition(scope, UnExp.Operator.INOT, right, ICode.Type.INT);
    }
    
    private IdentExp buildUnaryFunctionCall(String funcName, ICode.Scope scope, ICode.Type retType, IdentExp right, ICode.Type rightType) {
    	if(containsEntry(funcName, 0, SymEntry.EXTERNAL | SymEntry.PARAM) && containsEntry(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolBuilderSearchStrategy.SEARCH_VIA_FUNC_NAME)) {
    		IdentExp rightPlace = this.getVariablePlace(funcName, 0, SymEntry.EXTERNAL | SymEntry.PARAM);
    		
    		Def rightAssign = new Def(rightPlace.scope, rightPlace.ident, right, rightType);
    		
    		LinkedList<Def> args = new LinkedList<Def>();
    		args.add(rightAssign);
    		
    		addInstruction(new Call(funcName, args));
    		
    		IdentExp retPlace = this.getVariablePlace(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolBuilderSearchStrategy.SEARCH_VIA_FUNC_NAME);
    		
    		String newPlace = gen.genNext();
    		addInstruction(new Def(ICode.Scope.LOCAL, newPlace, retPlace, retType));
    		
    		return new IdentExp(ICode.Scope.LOCAL, newPlace);
    	} else if(containsEntry(funcName, 0, SymEntry.INTERNAL | SymEntry.PARAM) && containsEntry(funcName, SymEntry.INTERNAL | SymEntry.RETURN, SymbolBuilderSearchStrategy.SEARCH_VIA_FUNC_NAME)) {
    		IdentExp rightPlace = this.getVariablePlace(funcName, 0, SymEntry.INTERNAL | SymEntry.PARAM);
    		
    		Def rightAssign = new Def(rightPlace.scope, rightPlace.ident, right, rightType);
    		
    		LinkedList<Def> args = new LinkedList<Def>();
    		args.add(rightAssign);
    		
    		addInstruction(new Call(funcName, args));
    		
    		IdentExp retPlace = this.getVariablePlace(funcName, SymEntry.INTERNAL | SymEntry.RETURN, SymbolBuilderSearchStrategy.SEARCH_VIA_FUNC_NAME);
    		
    		String newPlace = gen.genNext();
    		addInstruction(new Def(scope, newPlace, retPlace, retType));
    		
    		return new IdentExp(scope, newPlace);
    	} else {
    		LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
    		args.add(new Tuple<>(right, rightType));
    		return this.buildExternalFunctionCall(scope, funcName, args, retType);
    	}
    }
    
    public IdentExp buildIntegerNegationDefinition(ICode.Scope scope, IdentExp right) {
    	return this.buildUnaryFunctionCall("INeg", scope, ICode.Type.INT, right, ICode.Type.INT);
    }
    
    public IdentExp buildRealNegationDefinition(ICode.Scope scope, IdentExp right) {
    	return this.buildUnaryFunctionCall("RNeg", scope, ICode.Type.REAL, right, ICode.Type.REAL);
    }

    public void buildDefinition(ICode.Scope scope, String place, Exp value, ICode.Type type){
        addInstruction(new Def(scope, place, value, type));
    }

    public IdentExp buildParamaterDefinition(String funcName, Exp value, ICode.Type type){
        int paramDefinitionEnd = this.endOfParamAssign(funcName) + 1;
        String place = gen.genNext();
        addInstruction(paramDefinitionEnd, new Def(Scope.PARAM, place, value, type));
        return new IdentExp(Scope.PARAM, place);
    }
    
    public IdentExp buildFunctionCall(String funcName, List<Def> definitions, IdentExp returnPlace, ICode.Type returnType) {
    	String newReg = gen.genNext();
    	addInstruction(new Call(funcName, definitions));
    	addInstruction(new Def(ICode.Scope.LOCAL, newReg, returnPlace, returnType));
    	return new IdentExp(ICode.Scope.LOCAL, newReg);
    }

    public IdentExp buildExternalFunctionCall(ICode.Scope scope, String funcName, List<Tuple<Exp, Assign.Type>> args, ICode.Type type){
        ArrayList<Tuple<Exp, Assign.Type>> newArgs = new ArrayList<Tuple<Exp, Assign.Type>>();
        newArgs.addAll(args);
        List<Def> newDefs = new LinkedList<Def>();
        for(int i = 0; i < newArgs.size(); i++){
            Tuple<Exp, Assign.Type> arg = newArgs.get(i);
            String next;
            if(containsEntry(funcName, i, SymEntry.EXTERNAL | SymEntry.PARAM))
                next = getVariablePlace(funcName, i, SymEntry.EXTERNAL | SymEntry.PARAM).ident;
            else {
                next = gen.genNext();
                addVariableEntry(next, SymEntry.EXTERNAL | SymEntry.PARAM, funcName, i);
            }
            newDefs.add(new Def(Scope.PARAM, next, arg.source, arg.dest));
        }
        addInstruction(new Call(funcName, newDefs));
        String oldPlace;
        if(containsEntry(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolBuilderSearchStrategy.SEARCH_VIA_FUNC_NAME))
            oldPlace = getVariablePlace(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolBuilderSearchStrategy.SEARCH_VIA_FUNC_NAME).ident;
        else {
            oldPlace = gen.genNext();
            addVariableEntry(oldPlace, SymEntry.EXTERNAL | SymEntry.RETURN, funcName, true);
        }
        String place = gen.genNext();
        addInstruction(new Def(Scope.LOCAL, place, new IdentExp(ICode.Scope.RETURN, oldPlace), type));
        return new IdentExp(Scope.LOCAL, place);
    }
}
