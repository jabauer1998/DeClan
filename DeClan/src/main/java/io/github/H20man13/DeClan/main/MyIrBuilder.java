package io.github.H20man13.DeClan.main;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;

public class MyIrBuilder {
    private MyIrFactory factory;
    private IrRegisterGenerator gen;
    private List<ICode> output;

    private int forLoopNumber;
    private int repeatLoopNumber;
    
    private int ifStatementNumber;
    private int ifStatementSeqNumber;

    private int whileLoopNumber;
    private int whileLoopSeqNumber;

    private Map<String, String> discovered;

    public MyIrBuilder(ErrorLog errLog){
        this(errLog, new IrRegisterGenerator());
    }

    public MyIrBuilder(ErrorLog errLog, IrRegisterGenerator generator){
        this.factory = new MyIrFactory(errLog);
        this.gen = generator;
        this.output = new LinkedList<>();
        this.forLoopNumber = 0;
        this.repeatLoopNumber = 0;
        this.ifStatementNumber = 0;
        this.ifStatementSeqNumber = 0;
        this.whileLoopNumber = 0;
        this.whileLoopSeqNumber = 0;
        this.discovered = new HashMap<String, String>();
    }

    public List<ICode> getOutput(){
        return this.output;
    }

    public void buildEnd(){
        output.add(factory.produceEnd());
    }

    public String buildStringAssignment(String value){
        String place = gen.genNextRegister();
        output.add(factory.produceStringAssignment(place, value));
        return place;
    }

    public String buildBoolAssignment(String value){
        String place = gen.genNextRegister();
        Assign result = null;
        if(value.equals("TRUE")){
            result = factory.produceBooleanAssignment(place, true);
        } else {
            result = factory.produceBooleanAssignment(place, false);
        }
        output.add(result);
        return place;
    }

    public String buildNumAssignment(String value){
        ICode result = null;
        String place = gen.genNextRegister();
        if(value.contains(".")){
            result = factory.produceRealAssignment(place, Double.parseDouble(value));
        } else {
            result = factory.produceIntAssignment(place, Integer.parseInt(value));
        }
        output.add(result);
        return place;
    }

    public String buildNegationAssignment(Exp value){
        String place = gen.genNextRegister();
        UnExp unaryExp = new UnExp(UnExp.Operator.NEG, value);
        output.add(factory.produceUnaryOperation(place, unaryExp));
        return place;
    }

    public String buildNotAssignment(Exp value){
        String place = gen.genNextRegister();
        UnExp unExp = new UnExp(UnExp.Operator.BNOT, value);
        output.add(factory.produceUnaryOperation(place, unExp));
        return place;
    }

    public String buildProcedureCall(String funcName, List<Tuple<String, String>> arguments){
        String place = gen.genNextRegister();
        output.add(factory.produceProcedureCall(place, funcName, arguments));
        return place;
    }

    public String buildAdditionAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.ADD ,right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildSubtractionAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.SUB, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildMultiplicationAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.MUL, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildDivisionAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.DIV, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildModuloAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.MOD, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildLessThanOrEqualAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.LE, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildLessThanAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.LT, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildGreaterThanOrEqualToAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.GE, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildGreaterThanAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.GT, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildAndAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.BAND, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildOrAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.BOR, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildEqualityAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.EQ, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildInequalityAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.NE, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public void buildVariableAssignment(String place, String value){
        output.add(factory.produceVariableAssignment(place, value));
    }

    public String buildVariableAssignment(String value){ 
        String place = gen.genNextRegister();
        output.add(factory.produceVariableAssignment(place, value));
        return place;
    }

    public void buildForLoopBeginning(Exp currentValue, Exp target){
        output.add(factory.produceLabel("FORBEG_" + forLoopNumber));
        BinExp bExp = new BinExp(currentValue, BinExp.Operator.NE, target);
        output.add(factory.produceIfStatement(bExp, "FORLOOP_" + forLoopNumber, "FOREND_" + forLoopNumber));
        output.add(factory.produceLabel("FORLOOP_" + forLoopNumber));
    }

    public void buildForLoopEnd(){
        output.add(factory.produceGoto("FORBEG_" + forLoopNumber));
        output.add(factory.produceLabel("FOREND_" + forLoopNumber));
        forLoopNumber++;
    }

    public void buildRepeatLoopBeginning(String exprResult){
        String exprPlace = this.buildNumAssignment("0");
        IdentExp identExp = new IdentExp(exprPlace);
        output.add(factory.produceLabel("REPEATBEG_" + repeatLoopNumber));
        output.add(factory.produceIfStatement(identExp, "REPEATLOOP_" + repeatLoopNumber, "REPEATEND_" + repeatLoopNumber));
        output.add(factory.produceLabel("REPEATLOOP_" + repeatLoopNumber));
    }

    public void buildRepeatLoopEnd(){
        output.add(factory.produceLabel("REPEATEND_" + repeatLoopNumber));
        repeatLoopNumber++;
    }

    public void buildIfStatementBeginning(IdentExp test){
        output.add(factory.produceIfStatement(test, "IFSTAT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber, "IFNEXT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber));
        output.add(factory.produceLabel("IFSTAT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber));
    }

    public void buildElseIfStatementBeginning(){
        output.add(factory.produceGoto("IFEND_" + ifStatementNumber));
        output.add(factory.produceLabel("IFNEXT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber));
        ifStatementSeqNumber++;
    }

    public void buildIfStatementEnd(){
        output.add(factory.produceGoto("IFEND_" + ifStatementNumber));
        output.add(factory.produceLabel("IFNEXT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber));
        output.add(factory.produceLabel("IFEND_" + ifStatementNumber));
        ifStatementNumber++;
        ifStatementSeqNumber = 0;
    }

    public void buildWhileLoopBeginning(Exp test){
        output.add(factory.produceIfStatement(test, "WHILESTAT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber, "WHILENEXT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber));
        output.add(factory.produceLabel("WHILECOND_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber));
        output.add(factory.produceIfStatement(test, "WHILESTAT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber, "WHILEEND_" + whileLoopNumber));
        output.add(factory.produceLabel("WHILESTAT_" +  whileLoopNumber + "_SEQ_" + whileLoopSeqNumber));
    }

    public void buildElseWhileLoopBeginning(){
        output.add(factory.produceGoto("WHILECOND_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber));
        output.add(factory.produceLabel("WHILENEXT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber));
        whileLoopSeqNumber++;
    }

    public void buildWhileLoopEnd(){
        output.add(factory.produceGoto("WHILECOND_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber));
        output.add(factory.produceLabel("WHILENEXT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber));
        output.add(factory.produceLabel("WHILEEND_" + whileLoopNumber));
        whileLoopNumber++;
        whileLoopSeqNumber = 0;
    }

    public void buildProcedure(String name, List<Tuple<String, String>> args){
        output.add(factory.produceProcedure(name, args));
    }

    public void buildProcedureDeclaration(String name){
        output.add(factory.produceLabel(name));
    }

    public void buildReturnStatement(){
        output.add(factory.produceReturnStatement());
    }

    public void buildLabel(String label){
        output.add(factory.produceLabel(label));
    }

    public void buildGoto(String label){
        output.add(factory.produceGoto(label));
    }
}
