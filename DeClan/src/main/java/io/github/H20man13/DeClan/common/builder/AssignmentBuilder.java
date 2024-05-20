package io.github.H20man13.DeClan.common.builder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.builder.section.SymbolSectionBuilder;
import io.github.H20man13.DeClan.common.builder.template.ResetableBuilder;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Assign.Scope;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp.Operator;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;
import io.github.H20man13.DeClan.main.MyIrFactory;

public abstract class AssignmentBuilder implements ResetableBuilder{
    protected List<ICode> intermediateCode;
    private IrBuilderContext ctx;
    private IrRegisterGenerator gen;
    private MyIrFactory factory;
    private SymbolSectionBuilder symbols;
    
    protected AssignmentBuilder(SymbolSectionBuilder symbols, IrBuilderContext ctx, IrRegisterGenerator gen, ErrorLog errLog){
        this.gen = gen;
        this.ctx = ctx;
        this.factory = new MyIrFactory(errLog);
        this.symbols = symbols;
        resetBuilder();
    }

    public SymbolSectionBuilder getSymbolSectionBuilder(){
        return symbols;
    }

    public String buildStringAssignment(Assign.Scope scope, String value){
        String place = gen.genNext();
        intermediateCode.add(factory.produceStringAssignment(scope, place, value));
        return place;
    }

    public String buildBoolAssignment(Scope scope, String value){
        String place = gen.genNext();
        Assign result = null;
        if(value.equals("TRUE")){
            result = factory.produceBooleanAssignment(scope, place, true);
        } else {
            result = factory.produceBooleanAssignment(scope, place, false);
        }
        intermediateCode.add(result);
        return place;
    }

    public String buildNumAssignment(Scope scope, String value){
        Assign result = null;
        String place = gen.genNext();
        if(value.contains(".")){
            result = factory.produceRealAssignment(scope, place, Float.parseFloat(value));
        } else {
            result = factory.produceIntAssignment(scope, place, Integer.parseUnsignedInt(value));
        }
        intermediateCode.add(result);
        return place;
    }

    public String buildIntegerNotAssignment(Scope scope, Exp value){
        String place = gen.genNext();
        UnExp unExp = new UnExp(Operator.INOT, value);
        intermediateCode.add(factory.produceUnaryOperation(scope, place, unExp, Assign.Type.INT));
        return place;
    }

    public String buildNotAssignment(Scope scope, Exp value){
        String place = gen.genNext();
        UnExp unExp = new UnExp(UnExp.Operator.BNOT, value);
        intermediateCode.add(factory.produceUnaryOperation(scope, place, unExp, Assign.Type.BOOL));
        return place;
    }

    public String buildIntegerAdditionAssignment(Scope scope, Exp left, Exp right){
        String place = gen.genNext();
        BinExp binExp = new BinExp(left, BinExp.Operator.IADD ,right);
        intermediateCode.add(factory.produceBinaryOperation(scope, place, binExp, Assign.Type.INT));
        return place;
    }

    public String buildIntegerSubtractionAssignment(Scope scope, Exp left, Exp right){
        String place = gen.genNext();
        BinExp binExp = new BinExp(left, BinExp.Operator.ISUB, right);
        intermediateCode.add(factory.produceBinaryOperation(scope, place, binExp, Assign.Type.INT));
        return place;
    }

    public String buildIntegerMultiplicationAssignment(Scope scope, Exp left, Exp right){
        String place = gen.genNext();
        BinExp binExp = new BinExp(left, BinExp.Operator.IMUL, right);
        intermediateCode.add(factory.produceBinaryOperation(scope, place, binExp, Assign.Type.INT));
        return place;
    }

    public String buildIntegerModuloAssignment(Scope scope, String left, String right){
        List<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
        args.add(new Tuple<String,Assign.Type>(left, Assign.Type.INT));
        args.add(new Tuple<String, Assign.Type>(right, Assign.Type.INT));
        return buildExternalFunctionCall(scope, "Mod", args, Assign.Type.INT);
    }

    public String buildLessThanOrEqualAssignment(Scope scope, Exp left, Exp right){
        String place = gen.genNext();
        BinExp binExp = new BinExp(left, BinExp.Operator.LE, right);
        intermediateCode.add(factory.produceBinaryOperation(scope, place, binExp, Assign.Type.BOOL));
        return place;
    }

    public String buildLessThanAssignment(Scope scope, Exp left, Exp right){
        String place = gen.genNext();
        BinExp binExp = new BinExp(left, BinExp.Operator.LT, right);
        intermediateCode.add(factory.produceBinaryOperation(scope, place, binExp, Assign.Type.BOOL));
        return place;
    }

    public String buildGreaterThanOrEqualToAssignment(Scope scope, Exp left, Exp right){
        String place = gen.genNext();
        BinExp binExp = new BinExp(left, BinExp.Operator.GE, right);
        intermediateCode.add(factory.produceBinaryOperation(scope, place, binExp, Assign.Type.BOOL));
        return place;
    }

    public String buildGreaterThanAssignment(Scope scope, Exp left, Exp right){
        String place = gen.genNext();
        BinExp binExp = new BinExp(left, BinExp.Operator.GT, right);
        intermediateCode.add(factory.produceBinaryOperation(scope, place, binExp, Assign.Type.BOOL));
        return place;
    }

    public String buildLogicalAndAssignment(Scope scope, Exp left, Exp right){
        String place = gen.genNext();
        BinExp binExp = new BinExp(left, BinExp.Operator.LAND, right);
        intermediateCode.add(factory.produceBinaryOperation(scope, place, binExp, Assign.Type.BOOL));
        return place;
    }

    public String buildIntegerOrAssignment(Scope scope, Exp left, Exp right){
        String place = gen.genNext();
        BinExp binExp = new BinExp(left, BinExp.Operator.IOR, right);
        intermediateCode.add(factory.produceBinaryOperation(scope, place, binExp, Assign.Type.INT));
        return place;
    }

    public String buildIntegerAndAssignment(Scope scope, Exp left, Exp right){
        String place = gen.genNext();
        BinExp binExp = new BinExp(left, BinExp.Operator.IAND, right);
        intermediateCode.add(factory.produceBinaryOperation(scope, place, binExp, Assign.Type.INT));
        return place;
    }

    public String buildIntegerExclusiveOrAssignment(Scope scope, Exp left, Exp right){
        String place = gen.genNext();
        BinExp binExp = new BinExp(left, BinExp.Operator.IXOR, right);
        intermediateCode.add(factory.produceBinaryOperation(scope, place, binExp, Assign.Type.INT));
        return place;
    }

    public String buildLogicalOrAssignment(Scope scope, Exp left, Exp right){
        String place = gen.genNext();
        BinExp binExp = new BinExp(left, BinExp.Operator.LOR, right);
        intermediateCode.add(factory.produceBinaryOperation(scope, place, binExp, Assign.Type.BOOL));
        return place;
    }

    public String buildLeftShiftAssignment(Scope scope, Exp left, Exp right){
        String place = gen.genNext();
        BinExp binExp = new BinExp(left, BinExp.Operator.ILSHIFT, right);
        intermediateCode.add(factory.produceBinaryOperation(scope, place, binExp, Assign.Type.INT));
        return place;
    }

    public String buildRightShiftAssignment(Scope scope, Exp left, Exp right){
        String place = gen.genNext();
        BinExp binExp = new BinExp(left, BinExp.Operator.IRSHIFT, right);
        intermediateCode.add(factory.produceBinaryOperation(scope, place, binExp, Assign.Type.INT));
        return place;
    }

    public String buildEqualityAssignment(Scope scope, Exp left, Exp right){
        String place = gen.genNext();
        BinExp binExp = new BinExp(left, BinExp.Operator.EQ, right);
        intermediateCode.add(factory.produceBinaryOperation(scope, place, binExp, Assign.Type.BOOL));
        return place;
    }

    public String buildInequalityAssignment(Scope scope, Exp left, Exp right){
        String place = gen.genNext();
        BinExp binExp = new BinExp(left, BinExp.Operator.NE, right);
        intermediateCode.add(factory.produceBinaryOperation(scope, place, binExp, Assign.Type.BOOL));
        return place;
    }

    public void buildVariableAssignment(Scope scope, String place, String value, Assign.Type type){
        intermediateCode.add(factory.produceVariableAssignment(scope, place, value, type));
    }

    public String buildVariableAssignment(Scope scope, String value, Assign.Type type){ 
        String place = gen.genNext();
        intermediateCode.add(factory.produceVariableAssignment(scope, place, value, type));
        return place;
    }

    public void buildProcedureCall(String name, List<Assign> args){
        intermediateCode.add(factory.produceProcedure(name, args));
    }

    public void buildExternalProcedureCall(String funcName, List<Tuple<String, Assign.Type>> args){
        ArrayList<Tuple<String, Assign.Type>> newArgs = new ArrayList<Tuple<String, Assign.Type>>();
        LinkedList<Assign> newAssigns = new LinkedList<Assign>();
        newArgs.addAll(args);
        for(int i = 0; i < newArgs.size(); i++){
            Tuple<String, Assign.Type> arg = newArgs.get(i);
            String newPlace;
            if(symbols.containsExternalArgument(funcName, i))
                newPlace = symbols.getArgumentName(funcName, i);
            else {
                newPlace = gen.genNext();
                symbols.addParamSymEntry(SymEntry.EXTERNAL, newPlace, funcName, i);
            }
            newAssigns.add(factory.produceVariableAssignment(Scope.ARGUMENT, newPlace, arg.source, arg.dest));
        }
        intermediateCode.add(factory.produceProcedure(funcName, newAssigns));
    }

    public String buildExternalFunctionCall(Scope scope, String funcName, List<Tuple<String, Assign.Type>> args, Assign.Type type){
        ArrayList<Tuple<String, Assign.Type>> newArgs = new ArrayList<Tuple<String, Assign.Type>>();
        newArgs.addAll(args);
        LinkedList<Assign> newAssigns = new LinkedList<Assign>();
        for(int i = 0; i < newArgs.size(); i++){
            Tuple<String, Assign.Type> arg = newArgs.get(i);
            String next;
            if(symbols.containsExternalArgument(funcName, i))
                next = symbols.getArgumentName(funcName, i);
            else {
                next = gen.genNext();
                symbols.addParamSymEntry(SymEntry.EXTERNAL, next, funcName, i);
            }
            newAssigns.add(factory.produceVariableAssignment(Scope.ARGUMENT, next, arg.source, arg.dest));
        }
        intermediateCode.add(factory.produceProcedure(funcName, newAssigns));
        String oldPlace;
        if(symbols.containsExternalReturn(funcName))
            oldPlace = symbols.getReturnName(funcName);
        else {
            oldPlace = gen.genNext();
            symbols.addReturnSymEntry(SymEntry.EXTERNAL, oldPlace, funcName);
        }
        String place = gen.genNext();
        intermediateCode.add(factory.procuceExternalReturnPlacement(place, oldPlace, type));
        return place;
    }

    public String buildExternalReturnPlacement(String dest, Assign.Type type){
        String reg = gen.genNext();
        intermediateCode.add(factory.procuceExternalReturnPlacement(reg, dest, type));
        return reg;
    }

    public void resetBuilder(){
        this.intermediateCode = new LinkedList<ICode>();
    }

    public abstract String buildParamaterAssignment(String place, Assign.Type type);
}
